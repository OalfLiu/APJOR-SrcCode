/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.simple;

import ec.*;
import ec.app.JSP.FitnessComprator;
import ec.gp.koza.KozaFitness;
import ec.steadystate.*;
import java.io.IOException;
import java.util.Collections;

import ec.util.*;
import jasima.core.experiment.AOAPRun;
import jasima.core.experiment.BaseEvaluator;
import jasima.core.experiment.EARun;
import jasima.core.experiment.KnowledgeGradientRun;
import jasima.core.experiment.OCBARun;
import jasima.shopSim.prioRules.gp.NormalizedBrankeRule;
import jasima.shopSim.prioRules.gp.testGPRule;

import java.io.File;

/* 
 * SimpleStatistics.java
 * 
 * Created: Tue Aug 10 21:10:48 1999
 * By: Sean Luke
 */

/**
 * A basic Statistics class suitable for simple problem applications.
 *
 * SimpleStatistics prints out the best individual, per subpopulation, each
 * generation. At the end of a run, it also prints out the best individual of
 * the run. SimpleStatistics outputs this data to a log which may either be a
 * provided file or stdout. Compressed files will be overridden on restart from
 * checkpoint; uncompressed files will be appended on restart.
 *
 * <p>
 * SimpleStatistics implements a simple version of steady-state statistics: if
 * it quits before a generation boundary, it will include the best individual
 * discovered, even if the individual was discovered after the last boundary.
 * This is done by using individualsEvaluatedStatistics(...) to update
 * best-individual-of-generation in addition to doing it in
 * postEvaluationStatistics(...).
 * 
 * <p>
 * <b>Parameters</b><br>
 * <table>
 * <tr>
 * <td valign=top><i>base.</i><tt>gzip</tt><br>
 * <font size=-1>boolean</font></td>
 * <td valign=top>(whether or not to compress the file (.gz suffix added)</td>
 * </tr>
 * <tr>
 * <td valign=top><i>base.</i><tt>file</tt><br>
 * <font size=-1>String (a filename), or nonexistant (signifies
 * stdout)</font></td>
 * <td valign=top>(the log for statistics)</td>
 * </tr>
 * </table>
 *
 * 
 * @author Sean Luke
 * @version 1.0
 */

public class SimpleStatistics extends Statistics implements SteadyStateStatisticsForm // ,
																						// ec.eval.ProvidesBestSoFar
{
	public Individual[] getBestSoFar() {
		return best_of_run;
	}

	/** log file parameter */
	public static final String P_STATISTICS_FILE = "file";

	/** compress? */
	public static final String P_COMPRESS = "gzip";

	public static final String P_DO_FINAL = "do-final";
	public static final String P_DO_GENERATION = "do-generation";
	public static final String P_DO_MESSAGE = "do-message";
	public static final String P_DO_DESCRIPTION = "do-description";
	public static final String P_DO_PER_GENERATION_DESCRIPTION = "do-per-generation-description";

	/** The Statistics' log */
	public int statisticslog = 0; // stdout

	/** The best individual we've found so far */
	public Individual[] best_of_run = null;

	/** Should we compress the file? */
	public boolean compress;
	public boolean doFinal;
	public boolean doGeneration;
	public boolean doMessage;
	public boolean doDescription;
	public boolean doPerGenerationDescription;
	public boolean doFinalEvaluation = false;

	protected int NumOfJobs;
	protected int NumOfMachines;
	protected long InitialSeed;
	protected int NumOfReplications;
	protected double UtilizationLevel;
	protected int Normalization;
	protected int Evaluation_Replications;
	protected int Evaluation_InitialReplications;
	protected long EvaluationSeed;
	protected String evaluatorDescription;

	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);

		compress = state.parameters.getBoolean(base.push(P_COMPRESS), null, false);

		File statisticsFile = state.parameters.getFile(base.push(P_STATISTICS_FILE), null);

		doFinal = state.parameters.getBoolean(base.push(P_DO_FINAL), null, true);
		doGeneration = state.parameters.getBoolean(base.push(P_DO_GENERATION), null, true);
		doMessage = state.parameters.getBoolean(base.push(P_DO_MESSAGE), null, true);
		doDescription = state.parameters.getBoolean(base.push(P_DO_DESCRIPTION), null, true);
		doPerGenerationDescription = state.parameters.getBoolean(base.push(P_DO_PER_GENERATION_DESCRIPTION), null,
				false);
		doFinalEvaluation = state.parameters.getBoolean(new Parameter("doFinalEvaluation"), null, false);

		NumOfJobs = state.parameters.getInt(new Parameter("eval.problem.Simulation.Replications"), null);
//		NumOfMachines = state.parameters.getInt(new Parameter("eval.problem.Simulation.NumofMachines"), null);
		InitialSeed = state.parameters.getLongWithDefault(new Parameter("eval.problem.Simulation.Seed"), null,
				System.currentTimeMillis());
		NumOfReplications = state.parameters.getIntWithDefault(new Parameter("eval.problem.Simulation.Replications"),
				null, 1);
//		UtilizationLevel = state.parameters.getDouble(new Parameter("eval.problem.Simulation.UtilizationLevel"), null);
		Normalization = state.parameters.getIntWithDefault(new Parameter("eval.problem.Normalization"), null, 0);
		Evaluation_Replications = state.parameters.getInt(new Parameter("evaluation.Replications"), null);
		Evaluation_InitialReplications = state.parameters.getInt(new Parameter("evaluation.InitialReplications"), null);
		evaluatorDescription = state.parameters.getString(new Parameter("evaluation.Evaluator"), null);
		EvaluationSeed = state.parameters.getLongWithDefault(new Parameter("evaluation.Seed"), null,
				System.currentTimeMillis());

		if (silentFile) {
			statisticslog = Output.NO_LOGS;
		} else if (statisticsFile != null) {
			try {
				statisticslog = state.output.addLog(statisticsFile, !compress, compress);
			} catch (IOException i) {
				state.output
						.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
			}
		} else
			state.output.warning("No statistics file specified, printing to stdout at end.",
					base.push(P_STATISTICS_FILE));
	}

	public void postInitializationStatistics(final EvolutionState state) {
		super.postInitializationStatistics(state);

		// set up our best_of_run array -- can't do this in setup, because
		// we don't know if the number of subpopulations has been determined yet
		best_of_run = new Individual[state.population.subpops.length];
	}

	/** Logs the best individual of the generation. */
	boolean warned = false;

	protected void PrintParameters(final EvolutionState state) {
		// Log All Parameters
		int currentJob = (int) state.job[0];
		Parameter p = new Parameter("seed.0");
		long InitialSeed = state.parameters.getLong(p, null);
		InitialSeed += currentJob;
		state.output.println("Current Parameters setting:", statisticslog);
		state.output.println("InitialSeed=" + InitialSeed, statisticslog);
		state.output.println("Current Job=" + state.job[0], statisticslog);

		String strPopulationSize = "pop.subpop.0.size";
		p = new Parameter(strPopulationSize);
		int PopulationSize = state.parameters.getInt(p, null);
		state.output.println("PopulationSize=" + PopulationSize, statisticslog);

		String strReplications = "eval.problem.Simulation.Replications";
		p = new Parameter(strReplications);
		int Replications = state.parameters.getInt(p, null);
		state.output.println("Replications=" + Replications, statisticslog);

		String strSimulationSeed = "eval.problem.Simulation.Seed";
		p = new Parameter(strSimulationSeed);
		long SimulationSeed = state.parameters.getLong(p, null);
		state.output.println("SimulationSeed=" + SimulationSeed, statisticslog);

		String strNormalization = "eval.problem.Normalization";
		p = new Parameter(strNormalization);
		int Normalization = state.parameters.getInt(p, null);
		state.output.println("Normalization=" + Normalization, statisticslog);

		String Simulation_UniqueSeed = "eval.problem.Simulation.UniqueSeed";
		p = new Parameter(Simulation_UniqueSeed);
		if (p != null) {
			state.output.println(Simulation_UniqueSeed + "=" + state.parameters.getBoolean(p, null, false),
					statisticslog);
		}

		String doFinalEvaluation = "doFinalEvaluation";
		p = new Parameter(doFinalEvaluation);
		if (p != null) {
			state.output.println(doFinalEvaluation + "=" + state.parameters.getBoolean(p, null, false), statisticslog);
		}

		if (this.doFinalEvaluation) {
			String evaluation_Evaluator = "evaluation.Evaluator";
			p = new Parameter(evaluation_Evaluator);
			if (p != null) {
				state.output.println(evaluation_Evaluator + "=" + state.parameters.getString(p, null), statisticslog);
			}
			String evaluation_Replications = "evaluation.Replications";
			p = new Parameter(evaluation_Replications);
			if (p != null) {
				state.output.println(evaluation_Replications + "=" + state.parameters.getString(p, null),
						statisticslog);
			}
			String evaluation_Threshold = "evaluation.Threshold";
			p = new Parameter(evaluation_Threshold);
			if (p != null) {
				state.output.println(evaluation_Threshold + "=" + state.parameters.getString(p, null), statisticslog);
			}
			String evaluation_InitialReplications = "evaluation.InitialReplications";
			p = new Parameter(evaluation_InitialReplications);
			if (p != null) {
				state.output.println(evaluation_InitialReplications + "=" +this.Evaluation_InitialReplications,
						statisticslog);
			}
			String evaluation_Seed = "evaluation.Seed";
			state.output.println(evaluation_Seed + "=" + this.EvaluationSeed,
					statisticslog);
		}
	}

	public void postEvaluationStatistics(final EvolutionState state) {
		super.postEvaluationStatistics(state);

		if (state.generation == 0) {
			PrintParameters(state);
		}

		// for now we just print the best fitness per subpopulation.
		Individual[] best_i = new Individual[state.population.subpops.length]; // quiets
																				// compiler
																				// complaints
		for (int x = 0; x < state.population.subpops.length; x++) {
			best_i[x] = state.population.subpops[x].individuals[0];
			for (int y = 1; y < state.population.subpops[x].individuals.length; y++) {
				if (state.population.subpops[x].individuals[y] == null) {
					if (!warned) {
						state.output.warnOnce("Null individuals found in subpopulation");
						warned = true; // we do this rather than relying on
										// warnOnce because it is much faster in
										// a tight loop
					}
				} else if (best_i[x] == null
						|| state.population.subpops[x].individuals[y].fitness.betterThan(best_i[x].fitness))
					best_i[x] = state.population.subpops[x].individuals[y];
				if (best_i[x] == null) {
					if (!warned) {
						state.output.warnOnce("Null individuals found in subpopulation");
						warned = true; // we do this rather than relying on
										// warnOnce because it is much faster in
										// a tight loop
					}
				}
			}

			// now test to see if it's the new best_of_run
			if (best_of_run[x] == null || best_i[x].fitness.betterThan(best_of_run[x].fitness))
				best_of_run[x] = (Individual) (best_i[x].clone());
		}

		// print the best-of-generation individual
		if (doGeneration)
			state.output.println("\nGeneration: " + state.generation, statisticslog);
		if (doGeneration)
			state.output.println("Best Individual:", statisticslog);
		for (int x = 0; x < state.population.subpops.length; x++) {
			if (doGeneration)
				state.output.println("Subpopulation " + x + ":", statisticslog);
			if (doGeneration)
				best_i[x].printIndividualForHumans(state, statisticslog);
			if (doMessage && !silentPrint)
				state.output.message("Subpop " + x + " best fitness of generation"
						+ (best_i[x].evaluated ? " " : " (evaluated flag not set): ")
						+ best_i[x].fitness.fitnessToStringForHumans());

			// describe the winner if there is a description
			if (doGeneration && doPerGenerationDescription) {
				if (state.evaluator.p_problem instanceof SimpleProblemForm)
					((SimpleProblemForm) (state.evaluator.p_problem.clone())).describe(state, best_i[x], x, 0,
							statisticslog);
			}
		}
		
		state.population.printPopulation(state, statisticslog);

		// GP run is completed
		if (doFinalEvaluation && state.generation >= state.numGenerations - 1) {

			switch (this.evaluatorDescription) {
			case "AOAP":
				this.fullEvaluation_AOAP(state);
				break;
			case "KG":
				this.fullEvaluation_KG(state);
				break;
			case "OCBA":
				this.fullEvaluation_OCBA(state);
				break;
			case "EA":
				this.fullEvaluation_EA(state);
				break;
			default:
				break;

			}
		}
	}

	/**
	 * Allows MultiObjectiveStatistics etc. to call
	 * super.super.finalStatistics(...) without calling
	 * super.finalStatistics(...)
	 */
	protected void bypassFinalStatistics(EvolutionState state, int result) {
		super.finalStatistics(state, result);
	}

	/** Logs the best individual of the run. */
	public void finalStatistics(final EvolutionState state, final int result) {
		super.finalStatistics(state, result);

		// for now we just print the best fitness

		if (doFinal)
			state.output.println("\nBest Individual of Run:", statisticslog);
		for (int x = 0; x < state.population.subpops.length; x++) {
			if (doFinal)
				state.output.println("Subpopulation " + x + ":", statisticslog);
			if (doFinal)
				best_of_run[x].printIndividualForHumans(state, statisticslog);
			if (doMessage && !silentPrint)
				state.output.message(
						"Subpop " + x + " best fitness of run: " + best_of_run[x].fitness.fitnessToStringForHumans());

			// finally describe the winner if there is a description
			if (doFinal && doDescription)
				if (state.evaluator.p_problem instanceof SimpleProblemForm)
					((SimpleProblemForm) (state.evaluator.p_problem.clone())).describe(state, best_of_run[x], x, 0,
							statisticslog);
		}
		
		
		//2019-03-03 print final population in a text document
		
		

		// We do final evaluation, output to result.log
		if (this.doFinalEvaluation) {
			if (doFinal)
				state.output.println("\nBest Individual of Final Evaluations:", statisticslog);

			state.output.message(
					"Best fitness of evaluation: " + state.AllGPIndividuals.get(0).fitness.fitnessToStringForHumans());
			state.AllGPIndividuals.get(0).printIndividualForHumans(state, statisticslog);
		}
	}

	// Added 2017.10.17
	// A new procedure, do full evaluation using R&S methods
	protected void fullEvaluation_EA(EvolutionState state) {
		long r = System.currentTimeMillis();

		EARun evaluator = new EARun();

		int len = state.AllGPIndividuals.size();

		for (int i = 0; i < len; i++) {
			try {

				state.AllGPIndividuals.get(i).trees[0].printTwoArgumentNonterminalsAsOperatorsInC = false;
				if (Normalization == 0) {
					evaluator.AddDispatchingRule(new testGPRule(state.AllGPIndividuals.get(i).trees[0].child));
				} else {
					evaluator
							.AddDispatchingRule(new NormalizedBrankeRule(state.AllGPIndividuals.get(i).trees[0].child));
				}
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		evaluator.InitilizeSeed = EvaluationSeed;
		evaluator.setMinReplicationsPerConfiguration(Evaluation_InitialReplications);
		evaluator.setNumReplications(Evaluation_Replications);

		evaluator.init();
		evaluator.runEvaluation();
		evaluator.produceResults();		

		long s = System.currentTimeMillis();
		double diff = (s - r) / 1000.0d;
		
		state.output.println(evaluator.printResultsToString(), statisticslog);
		state.output.println("Evaluation Time:" + diff, statisticslog);

		for (int i = 0; i < len; i++) {

			KozaFitness f = (KozaFitness) (state.AllGPIndividuals.get(i).fitness);
			f.setStandardizedFitness(state, evaluator.stats[i].mean());
			f.setVariance(state, evaluator.stats[i].stdDev());
			f.NumOfEvaluations = evaluator.stats[i].numObs();

			state.AllGPIndividuals.get(i).evaluated = true;
		}

		return;
	}

	// Added 2017.10.17
	// A new procedure, do full evaluation using R&S methods
	protected void fullEvaluation_KG(EvolutionState state) {
		long r = System.currentTimeMillis();

		KnowledgeGradientRun evaluator = new KnowledgeGradientRun();

		int len = state.AllGPIndividuals.size();

		for (int i = 0; i < len; i++) {
			try {

				state.AllGPIndividuals.get(i).trees[0].printTwoArgumentNonterminalsAsOperatorsInC = false;
				if (Normalization == 0) {
					evaluator.AddDispatchingRule(new testGPRule(state.AllGPIndividuals.get(i).trees[0].child));
				} else {
					evaluator
							.AddDispatchingRule(new NormalizedBrankeRule(state.AllGPIndividuals.get(i).trees[0].child));
				}
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		evaluator.InitilizeSeed = EvaluationSeed;
		evaluator.setMinReplicationsPerConfiguration(Evaluation_InitialReplications);
		evaluator.setNumReplications(Evaluation_Replications);

		evaluator.init();
		evaluator.runEvaluation();

		evaluator.produceResults();		

		long s = System.currentTimeMillis();
		double diff = (s - r) / 1000.0d;
		
		state.output.println(evaluator.printResultsToString(), statisticslog);
		state.output.println("Evaluation Time:" + diff, statisticslog);

		for (int i = 0; i < len; i++) {

			KozaFitness f = (KozaFitness) (state.AllGPIndividuals.get(i).fitness);
			f.setStandardizedFitness(state, evaluator.stats[i].mean());
			f.setVariance(state, evaluator.stats[i].stdDev());
			f.NumOfEvaluations = evaluator.stats[i].numObs();

			state.AllGPIndividuals.get(i).evaluated = true;
		}

		return;
	}

	// Added 2017.10.17
	// A new procedure, do full evaluation using R&S methods
	protected void fullEvaluation_OCBA(EvolutionState state) {
		long r = System.currentTimeMillis();

		OCBARun evaluator = new OCBARun();

		int len = state.AllGPIndividuals.size();

		for (int i = 0; i < len; i++) {
			try {

				state.AllGPIndividuals.get(i).trees[0].printTwoArgumentNonterminalsAsOperatorsInC = false;
				if (Normalization == 0) {
					evaluator.AddDispatchingRule(new testGPRule(state.AllGPIndividuals.get(i).trees[0].child));
				} else {
					evaluator
							.AddDispatchingRule(new NormalizedBrankeRule(state.AllGPIndividuals.get(i).trees[0].child));
				}
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		evaluator.InitilizeSeed = EvaluationSeed;
		evaluator.setMinReplicationsPerConfiguration(Evaluation_InitialReplications);
		evaluator.setNumReplications(Evaluation_Replications);

		evaluator.init();
		evaluator.runEvaluation();

		evaluator.produceResults();		

		long s = System.currentTimeMillis();
		double diff = (s - r) / 1000.0d;
		
		state.output.println(evaluator.printResultsToString(), statisticslog);
		state.output.println("Evaluation Time:" + diff, statisticslog);

		for (int i = 0; i < len; i++) {

			KozaFitness f = (KozaFitness) (state.AllGPIndividuals.get(i).fitness);
			f.setStandardizedFitness(state, evaluator.stats[i].mean());
			f.setVariance(state, evaluator.stats[i].stdDev());
			f.NumOfEvaluations = evaluator.stats[i].numObs();

			state.AllGPIndividuals.get(i).evaluated = true;
		}

		return;
	}

	// Added 2017.10.17
	// A new procedure, do full evaluation using R&S methods
	protected void fullEvaluation_AOAP(EvolutionState state) {

		long r = System.currentTimeMillis();

		AOAPRun evaluator = new AOAPRun();

		int len = state.AllGPIndividuals.size();

		for (int i = 0; i < len; i++) {
			try {

				state.AllGPIndividuals.get(i).trees[0].printTwoArgumentNonterminalsAsOperatorsInC = false;
				if (Normalization == 0) {
					evaluator.AddDispatchingRule(new testGPRule(state.AllGPIndividuals.get(i).trees[0].child));
				} else {
					evaluator
							.AddDispatchingRule(new NormalizedBrankeRule(state.AllGPIndividuals.get(i).trees[0].child));
				}
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		evaluator.InitilizeSeed = EvaluationSeed;
		evaluator.setMinReplicationsPerConfiguration(Evaluation_InitialReplications);
		evaluator.setNumReplications(Evaluation_Replications);

		evaluator.init();
		evaluator.runEvaluation();

		evaluator.produceResults();		

		long s = System.currentTimeMillis();
		double diff = (s - r) / 1000.0d;
		
		state.output.println(evaluator.printResultsToString(), statisticslog);
		state.output.println("Evaluation Time:" + diff, statisticslog);

		for (int i = 0; i < len; i++) {

			KozaFitness f = (KozaFitness) (state.AllGPIndividuals.get(i).fitness);
			f.setStandardizedFitness(state, evaluator.stats[i].mean());
			f.setVariance(state, evaluator.stats[i].stdDev());
			f.NumOfEvaluations = evaluator.stats[i].numObs();

			state.AllGPIndividuals.get(i).evaluated = true;
		}

		return;
	}
}
