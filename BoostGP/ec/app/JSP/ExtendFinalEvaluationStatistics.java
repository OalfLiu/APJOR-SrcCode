package ec.app.JSP;

import ec.*;
import ec.app.JSP.FitnessComprator;
import ec.gp.GPIndividual;
import ec.gp.koza.KozaFitness;
import ec.simple.SimpleProblemForm;
import ec.steadystate.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import ec.util.*;
import jasima.core.experiment.AOAPRun;
import jasima.core.experiment.BaseEvaluator;
import jasima.core.experiment.EARun;
import jasima.core.experiment.IndifferentOCBARun;
import jasima.core.experiment.KnowledgeGradientRun;
import jasima.core.experiment.MultipleReplicationExperiment;
import jasima.core.experiment.OCBARun;
import jasima.core.experiment.PreWarmupTSOCBARun;
import jasima.core.experiment.RSSingleBestRun;
import jasima.core.experiment.TSIndifferentOCBARun;
import jasima.core.random.continuous.DblConst;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.Simulation.SimEvent;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.Util;
import jasima.core.util.observer.NotifierListener;
import jasima.shopSim.core.PR;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment.Scenario;
import jasima.shopSim.prioRules.basic.FASFS;
import jasima.shopSim.prioRules.gp.NormalizedBrankeRule;
import jasima.shopSim.prioRules.gp.testGPRule;
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;
import jasima.shopSim.util.BasicJobStatCollector;
import jasima.shopSim.util.DeviationJobStatCollector;

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
 * @author Cheng
 * @version 1.0
 */

public class ExtendFinalEvaluationStatistics extends Statistics implements SteadyStateStatisticsForm // ,
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
	public int fileExlog = 0; // log whole individuals
	public int fileBestlog = 0; // log best individual

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
	private boolean doLoadFile;

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
	public int objectives = 1;
	protected ArrayList<String> evaluators;
	protected ArrayList<String> BestRule;

	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);

		compress = state.parameters.getBoolean(base.push(P_COMPRESS), null, false);

		File statisticsFile = state.parameters.getFile(base.push(P_STATISTICS_FILE), null);

		if (state.parameters.exists(new Parameter("loadFromFile"), null))
			doLoadFile = state.parameters.getBoolean(new Parameter("loadFromFile"), null, false);

		doFinal = state.parameters.getBoolean(base.push(P_DO_FINAL), null, true);
		doGeneration = state.parameters.getBoolean(base.push(P_DO_GENERATION), null, true);
		doMessage = state.parameters.getBoolean(base.push(P_DO_MESSAGE), null, true);
		doDescription = state.parameters.getBoolean(base.push(P_DO_DESCRIPTION), null, true);
		doPerGenerationDescription = state.parameters.getBoolean(base.push(P_DO_PER_GENERATION_DESCRIPTION), null,
				false);
		doFinalEvaluation = state.parameters.getBoolean(new Parameter("doFinalEvaluation"), null, false);

		//NumOfJobs = state.parameters.getInt(new Parameter("eval.problem.Simulation.NumOfJobs"), null);
		//NumOfMachines = state.parameters.getInt(new Parameter("eval.problem.Simulation.NumofMachines"), null);
		InitialSeed = state.parameters.getLongWithDefault(new Parameter("eval.problem.Simulation.Seed"), null,
				System.currentTimeMillis());
		NumOfReplications = state.parameters.getIntWithDefault(new Parameter("eval.problem.Simulation.Replications"),
				null, 1);
		//UtilizationLevel = state.parameters.getDouble(new Parameter("eval.problem.Simulation.UtilizationLevel"), null);
		Normalization = state.parameters.getIntWithDefault(new Parameter("eval.problem.Normalization"), null, 0);
		Evaluation_Replications = state.parameters.getInt(new Parameter("evaluation.Replications"), null);
		Evaluation_InitialReplications = state.parameters.getInt(new Parameter("evaluation.InitialReplications"), null);
		evaluatorDescription = state.parameters.getString(new Parameter("evaluation.Evaluator"), null);
		objectives = state.parameters.getInt(new Parameter("eval.problem.Simulation.Objective"), null);
		evaluators = new ArrayList<String>();
		// If multiple evaluator
		if (evaluatorDescription.contains(",")) {
			String[] evaDes = evaluatorDescription.split(",");

			for (String str : evaDes) {
				evaluators.add(str.trim());
			}
		} else
			evaluators.add(evaluatorDescription.trim());

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

		File finalFile = state.parameters.getFile(base.push("fileEx"), null);

		if (finalFile != null) {
			try {
				this.fileExlog = state.output.addLog(finalFile,
						!state.parameters.getBoolean(base.push(P_COMPRESS), null, false),
						state.parameters.getBoolean(base.push(P_COMPRESS), null, false));
			} catch (IOException i) {
				state.output.fatal("An IOException occurred while trying to create the log " + finalFile + ":\n" + i);
			}
		}
		
		File finalBestFile = state.parameters.getFile(base.push("fileBest"), null);

		if (finalBestFile != null) {
			try {
				this.fileBestlog = state.output.addLog(finalBestFile,
						!state.parameters.getBoolean(base.push(P_COMPRESS), null, false),
						state.parameters.getBoolean(base.push(P_COMPRESS), null, false));
			} catch (IOException i) {
				state.output.fatal("An IOException occurred while trying to create the log " + finalBestFile + ":\n" + i);
			}
		}
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
		state.output.println("------------Basic Parameters Setting------------", statisticslog);
		state.output.println("  InitialSeed=" + InitialSeed, statisticslog);
		state.output.println("  Current Job=" + state.job[0], statisticslog);

		String strPopulationSize = "pop.subpop.0.size";
		p = new Parameter(strPopulationSize);
		int PopulationSize = state.parameters.getInt(p, null);
		state.output.println("  PopulationSize=" + PopulationSize, statisticslog);

		state.output.println("------------Simulation Setting in Generation------------", statisticslog);
		String strReplications = "eval.problem.Simulation.Replications";
		p = new Parameter(strReplications);
		int Replications = state.parameters.getInt(p, null);
		state.output.println("  Replications=" + Replications, statisticslog);

		String strSimulationSeed = "eval.problem.Simulation.Seed";
		p = new Parameter(strSimulationSeed);
		long SimulationSeed = state.parameters.getLong(p, null);
		state.output.println("  SimulationSeed=" + SimulationSeed, statisticslog);

		String strNormalization = "eval.problem.Normalization";
		p = new Parameter(strNormalization);
		int Normalization = state.parameters.getInt(p, null);
		state.output.println("  Normalization=" + Normalization, statisticslog);

		String eval_Simulation_IterationBudget = "eval.Simulation.IterationBudget";
		p = new Parameter(eval_Simulation_IterationBudget);
		if (p != null) {
			state.output.println("  " + eval_Simulation_IterationBudget + "=" + state.parameters.getString(p, null),
					statisticslog);
		}

		String simulation_Objective = "eval.problem.Simulation.Objective";
		p = new Parameter(simulation_Objective);
		if (p != null) {
			state.output.println("  " + simulation_Objective + "=" + state.parameters.getString(p, null),
					statisticslog);
		}
		
		String simulation_Objective_type = "eval.problem.Simulation.ObjectiveType";
		p = new Parameter(simulation_Objective_type);
		if (p != null) {
			state.output.println("  " + simulation_Objective_type + "=" + state.parameters.getString(p, null),
					statisticslog);
		}

		String Simulation_UniqueSeed = "eval.problem.Simulation.UniqueSeed";
		p = new Parameter(Simulation_UniqueSeed);
		if (p != null) {
			state.output.println("  " + Simulation_UniqueSeed + "=" + state.parameters.getBoolean(p, null, false),
					statisticslog);
		}

		String eval_WarmupBudget = "eval.Simulation.WarmupBudget";
		p = new Parameter(eval_WarmupBudget);
		if (p != null) {
			state.output.println("  " + eval_WarmupBudget + "=" + state.parameters.getString(p, null), statisticslog);
		}

		String eval_preWarm = "eval.Simulation.WarmupBudget_Pre";
		p = new Parameter(eval_preWarm);
		if (state.parameters.exists(p, null)) {
			state.output.println("  " + eval_preWarm + "=" + state.parameters.getString(p, null), statisticslog);
		}

		String eval_FBudget = "eval.Simulation.FirstStageBudget";
		p = new Parameter(eval_FBudget);
		if (p != null) {
			state.output.println("  " + eval_FBudget + "=" + state.parameters.getString(p, null), statisticslog);
		}

		String eval_SBudget = "eval.Simulation.SecondStageBudget";
		p = new Parameter(eval_SBudget);
		if (p != null) {
			state.output.println("  " + eval_SBudget + "=" + state.parameters.getString(p, null), statisticslog);
		}

		String eval_Delta = "eval.Simulation.IndifferenceDelta";
		p = new Parameter(eval_Delta);
		if (p != null) {
			state.output.println("  " + eval_Delta + "=" + state.parameters.getString(p, null), statisticslog);
		}

		String eval_evaluator = "eval";
		p = new Parameter(eval_evaluator);
		if (p != null) {
			state.output.println("  " + eval_evaluator + "=" + state.parameters.getString(p, null), statisticslog);
		}

		String doFinalEvaluation = "doFinalEvaluation";
		p = new Parameter(doFinalEvaluation);
		if (p != null) {
			state.output.println("  " + doFinalEvaluation + "=" + state.parameters.getBoolean(p, null, false),
					statisticslog);
		}

		if (this.doFinalEvaluation) {
			state.output.println("------------Final Evaluation Setting------------", statisticslog);
			String evaluation_Evaluator = "evaluation.Evaluator";
			p = new Parameter(evaluation_Evaluator);
			String evaluators = state.parameters.getString(p, null);
			if (p != null) {
				state.output.println("  " + evaluation_Evaluator + "=" + state.parameters.getString(p, null),
						statisticslog);
			}
			String evaluation_Replications = "evaluation.Replications";
			p = new Parameter(evaluation_Replications);
			if (p != null) {
				state.output.println("  " + evaluation_Replications + "=" + state.parameters.getString(p, null),
						statisticslog);
			}
			String evaluation_Threshold = "evaluation.Threshold";
			p = new Parameter(evaluation_Threshold);
			if (p != null) {
				state.output.println("  " + evaluation_Threshold + "=" + state.parameters.getString(p, null),
						statisticslog);
			}
			String evaluation_InitialReplications = "evaluation.InitialReplications";
			p = new Parameter(evaluation_InitialReplications);
			if (p != null) {
				state.output.println("  " + evaluation_InitialReplications + "=" + this.Evaluation_InitialReplications,
						statisticslog);
			}
			String evaluation_Seed = "evaluation.Seed";
			state.output.println("  " + evaluation_Seed + "=" + this.EvaluationSeed, statisticslog);

			if (evaluators.contains("IDOCBA") || evaluators.contains("RS")) {
				String s = "evaluation.IDOCBA.WarmupBudget";
				state.output.println("  " + s + "=" + state.parameters.getString(new Parameter(s), null),
						statisticslog);
				s = "evaluation.IDOCBA.FirstStageBudget";
				state.output.println("  " + s + "=" + state.parameters.getString(new Parameter(s), null),
						statisticslog);
				s = "evaluation.IDOCBA.IndifferenceDelta";
				state.output.println("  " + s + "=" + state.parameters.getString(new Parameter(s), null),
						statisticslog);
			}
		}
	}

	private void runExtraEvaluation(final EvolutionState state, GPIndividual ind) {

		DynamicShopExperiment baseExperiment = new DynamicShopExperiment();
		NotifierListener<Simulation, SimEvent>[] l = baseExperiment.getShopListener();
		assert l.length == 1 && l[0] instanceof BasicJobStatCollector;
		baseExperiment.setShopListener(null);

		BasicJobStatCollector basicJobStatCollector = new BasicJobStatCollector();
		basicJobStatCollector.setIgnoreFirst(500);
		basicJobStatCollector.setInitialPeriod(500);

		DeviationJobStatCollector devJobStatCollector = new DeviationJobStatCollector();
		devJobStatCollector.setIgnoreFirst(500);
		devJobStatCollector.setInitialPeriod(500);

		if (this.objectives == 1) {
			baseExperiment.addShopListener(basicJobStatCollector);
		} else if (this.objectives == 11) {
			baseExperiment.addShopListener(devJobStatCollector);
		}

		baseExperiment.setNumMachines(10);
		baseExperiment.setNumOps(10, 10);
		baseExperiment.setDueDateFactor(new DblConst(4.0));
		baseExperiment.setUtilLevel(0.95d);
		baseExperiment.setStopAfterNumJobs(NumOfJobs);
		baseExperiment.setScenario(Scenario.JOB_SHOP);

		PR pr = null;
		if (Normalization == 0) {
			pr = new testGPRule(ind.trees[0].child);
		} else {
			pr = new NormalizedBrankeRule(ind.trees[0].child);
		}
		PR sr2 = new IgnoreFutureJobs(pr);
		PR sr3 = new FASFS();
		sr2.setTieBreaker(sr3);
		baseExperiment.setSequencingRule(sr2);

		MultipleReplicationExperiment mre = new MultipleReplicationExperiment();
		mre.setInitialSeed(778899);
		mre.setMaxReplications(100);
		mre.setBaseExperiment(baseExperiment);

		mre.runExperiment();

		String obj = Util.getObjectiveString(this.objectives);

		SummaryStat flowtime = (SummaryStat) mre.getResults().get(obj);

		KozaFitness f = (KozaFitness) (ind.fitness);

		SummaryStat ss;

		ss = f.summaryStat;
		ss.combine(flowtime);
		// f.setStandardizedFitness(state, ss.mean());
		// f.setVariance(state, ss.stdDev());
		// f.NumOfEvaluations = ss.numObs();
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
			// extra evaluations of current generation's best rule
			// runExtraEvaluation(state, (GPIndividual) best_i[x]);

			// System.out.println(((KozaFitness)
			// best_i[x].fitness).summaryStat.mean());
			// System.out.println(((KozaFitness)
			// best_i[x].fitness).fitnessToStringForHumans());
			// if (best_of_run[x] != null) System.out.println(((KozaFitness)
			// best_of_run[x].fitness).summaryStat.mean());
			// now test to see if it's the new best_of_run
			if (best_of_run[x] == null || ((KozaFitness) best_i[x].fitness).summaryStat
					.mean() < ((KozaFitness) best_of_run[x].fitness).summaryStat.mean())
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
			if (doGeneration) {
				best_i[x].printIndividualForHumans(state, statisticslog);
				state.output.println("InGenEvaluation:" + ((KozaFitness) best_i[x].fitness).summaryStat.mean(),
						statisticslog);
			}
			// best_i[x].printIndividualForHumans(state, statisticslog);
			if (doMessage && !silentPrint)
				state.output.message("Subpop " + x + " best fitness of generation"
						+ (best_i[x].evaluated ? " " : " (evaluated flag not set): ")
						+ best_i[x].fitness.fitnessToStringForHumans() + " InGenEvaluation:"
						+ ((KozaFitness) best_i[x].fitness).summaryStat.mean());

			// describe the winner if there is a description
			if (doGeneration && doPerGenerationDescription) {
				if (state.evaluator.p_problem instanceof SimpleProblemForm)
					((SimpleProblemForm) (state.evaluator.p_problem.clone())).describe(state, best_i[x], x, 0,
							statisticslog);
			}
		}

		// ************ log all individuals in whole GP run into a file
		// **************
		if (this.fileExlog != 0 && state.generation >= state.numGenerations - 1) {
			state.output.println("Number of Subpopulations: i1|", this.fileExlog);
			state.output.println("Subpopulation Number: i0|", this.fileExlog);
			state.output.println("Number of Individuals: i" + state.totalGPIndividuals.size() + "|", this.fileExlog);

			// Now we generate candidate individuals
			Collections.sort(state.totalGPIndividuals, new FitnessComprator());

			long len = state.totalGPIndividuals.size();

			for (int i = 0; i < len; i++) {
				state.output.println("Individual Number: i" + i + "|", this.fileExlog);
				state.totalGPIndividuals.get(i).printIndividual(state, this.fileExlog);
			}
		}
		 
		// ** Update best rule and screen out for final evaluation if needed
		// *********
		// In last generation, we select candidate individuals
		if ((state.generation >= state.numGenerations - 1)) {
			int evaluationThreshold = state.parameters.getInt(new Parameter("evaluation.Threshold"), null);
			Collections.sort(state.totalGPIndividuals, new FitnessComprator());

			int len = Math.min(state.totalGPIndividuals.size(), evaluationThreshold);

			for (int i = 0; i < len; i++) {
				state.AllGPIndividuals.add(state.totalGPIndividuals.get(i));
			}

			// 2018.1.21 We update the best rule of original one here
			// The best rule is not the rules obtained in generation, elite
			// reevaluate mechanism will allocate more samples to the best rule
			// in parent generation
			best_of_run[0] = (Individual) state.AllGPIndividuals.get(0).clone();
		}
		// ** Update best rule and screen out for final evaluation if needed
		// end*********

		// GP run is completed
		if (doFinalEvaluation && (((state.generation >= state.numGenerations - 1)) || this.doLoadFile)) {

			postfullEvaluationStatistics(state, "Original");
			
			for (int i = 0; i < this.evaluators.size(); i++) {
				String curEvaluator = evaluators.get(i);

				switch (curEvaluator) {
				case "AOAP":
					this.fullEvaluation_AOAP(state);
					postfullEvaluationStatistics(state, curEvaluator);
					break;
				case "KG":
					this.fullEvaluation_KG(state);
					postfullEvaluationStatistics(state, curEvaluator);
					break;
				case "OCBA":
					this.fullEvaluation_OCBA(state);
					postfullEvaluationStatistics(state, curEvaluator);
					break;
				case "EA":
					this.fullEvaluation_EA(state);
					postfullEvaluationStatistics(state, curEvaluator);
					break;
				case "IDOCBA":
					this.fullEvaluation_IDOCBA(state);
					postfullEvaluationStatistics(state, curEvaluator);
					break;
				case "RS":
					this.fullEvaluation_Ranking(state);
					postfullEvaluationStatistics(state, curEvaluator);
					break;
				default:
					break;

				}

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
		
		// ************ log the best individual in whole GP run into a file
		if (this.fileBestlog != 0 && state.generation >= state.numGenerations - 1) {
			//state.output.println("Number of Subpopulations: i1|", this.fileBestlog);
			//state.output.println("Subpopulation Number: i0|", this.fileBestlog);
			//state.output.println("Number of Individuals: i" + state.totalGPIndividuals.size() + "|", this.fileBestlog);
			state.output.println("Number of Individuals: i1|", this.fileBestlog);

			for (int x = 0; x < state.population.subpops.length; x++) {
				state.output.println("Individual Number: i" + x + "|", this.fileBestlog);
				state.totalGPIndividuals.get(x).printIndividual(state, this.fileBestlog);
			} 
		}

		// We do final evaluation, output to result.log
		if (this.doFinalEvaluation) {
			if (doFinal) {
				state.output.println("\nHere are Best Rules found during Final Evaluation", this.statisticslog);

				state.output.println("Original Best Rule: ", this.statisticslog);
				// state.output.println(this.BestRule.get(0),
				// this.statisticslog);
				state.output.println(((GPIndividual) best_of_run[0]).trees[0].child.makeCTree(true, true, false),
						this.statisticslog);

				for (int i = 0; i < this.evaluators.size(); i++) {
					state.output.println("Evaluator " + evaluators.get(i) + ": ", this.statisticslog);
					state.output.println(this.BestRule.get(i), this.statisticslog);
				}
			}
		}
	}

	protected void postfullEvaluationStatistics(EvolutionState state, String evaluator) {
		int len = state.AllGPIndividuals.size();
		Collections.sort(state.AllGPIndividuals, new FitnessComprator());
		state.output.println("\n" + evaluator + " Final Evaluation Details:", this.statisticslog);

		int ComputationBudget = 0;
		long width = Math.round(Math.log10(len));
		if (width <= 0)
			width = 1;
		for (int y = 0; y < len; y++) {
			KozaFitness fit = (KozaFitness) state.AllGPIndividuals.get(y).fitness;
			ComputationBudget += fit.NumOfEvaluations;
			state.output.println("Rank " + String.format("%" + width + "d", (y + 1)) + " mean: "
					+ String.format("%.6f", fit.summaryStat.mean()) + " stddev: "
					+ String.format("%.6f", fit.summaryStat.stdDev()) + " size: " + state.AllGPIndividuals.get(y).size()
					+ " Evaluations:" + fit.summaryStat.numObs()

					+ " Generation:" + state.AllGPIndividuals.get(y).bornGeneration + "  DR: "
					+ state.AllGPIndividuals.get(y).trees[0].child.makeCTree(true, true, false),

					statisticslog);
		}

		state.output.println("Individuals Count:" + len, this.statisticslog);
		state.output.println("Total Budget:" + ComputationBudget, this.statisticslog);

		// Let's look at the best individual of run and the best individual of
		// evaluation

		state.output.println("\n" + evaluator + " Final Evaluation Completed", this.statisticslog);
		state.output.println("Current best individual's dot", this.statisticslog);
		state.output.println(state.AllGPIndividuals.get(0).trees[0].child.makeGraphvizTree(), statisticslog);

		if (BestRule == null)
			BestRule = new ArrayList();

		BestRule.add(state.AllGPIndividuals.get(0).trees[0].child.makeCTree(true, true, false));
	}

	protected void fullEvaluation_IDOCBA(EvolutionState state) {
		long r = System.currentTimeMillis();

		RSSingleBestRun evaluator = new RSSingleBestRun();

		int len = state.AllGPIndividuals.size();

		SummaryStat[] stats = Util.initializedArray(len, SummaryStat.class);
		for (int i = 0; i < len; i++) {
			try {

				state.AllGPIndividuals.get(i).trees[0].printTwoArgumentNonterminalsAsOperatorsInC = false;
				if (Normalization == 0) {
					evaluator.AddDispatchingRule(new testGPRule(state.AllGPIndividuals.get(i).trees[0].child));
				} else {
					evaluator
							.AddDispatchingRule(new NormalizedBrankeRule(state.AllGPIndividuals.get(i).trees[0].child));
				}

				SummaryStat tempStat = state.AllIndividualsStats.get(state.AllGPIndividuals.get(i).hashCode()).clone();
				stats[i] = tempStat;
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		double indifferenceDelta = 0.01;
		// delta = state.parameters.getDouble(new
		// Parameter("evaluation.IndifferenceDelta"), null);

		indifferenceDelta = state.parameters.getDouble(new Parameter("evaluation.IDOCBA.IndifferenceDelta"), null);
		int warmupStageBudget = state.parameters.getInt(new Parameter("evaluation.IDOCBA.WarmupBudget"), null);
		int firstStageBudget = state.parameters.getInt(new Parameter("evaluation.IDOCBA.FirstStageBudget"), null);
		int OCBAIterationBudget = state.parameters.getInt(new Parameter("evaluation.IterationBudget"), null);

		evaluator.setDelta(indifferenceDelta);
		evaluator.objectives = this.objectives;
		evaluator.InitilizeSeed = EvaluationSeed;
		evaluator.setMinReplicationsPerConfiguration(Evaluation_InitialReplications);
		evaluator.setNumReplications(Evaluation_Replications);

		evaluator.init(warmupStageBudget, firstStageBudget, OCBAIterationBudget);

		if (!stats[0].isNew())
			evaluator.stats = stats;

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
			f.summaryStat = evaluator.stats[i];

			state.AllGPIndividuals.get(i).evaluated = true;
		}

		return;
	}

	// Added 2017.10.17
	// A new procedure, do full evaluation using R&S methods
	protected void fullEvaluation_EA(EvolutionState state) {
		long r = System.currentTimeMillis();

		EARun evaluator = new EARun();

		int len = state.AllGPIndividuals.size();

		SummaryStat[] stats = Util.initializedArray(len, SummaryStat.class);
		for (int i = 0; i < len; i++) {
			try {

				state.AllGPIndividuals.get(i).trees[0].printTwoArgumentNonterminalsAsOperatorsInC = false;
				if (Normalization == 0) {
					evaluator.AddDispatchingRule(new testGPRule(state.AllGPIndividuals.get(i).trees[0].child));
				} else {
					evaluator
							.AddDispatchingRule(new NormalizedBrankeRule(state.AllGPIndividuals.get(i).trees[0].child));
				}

				SummaryStat tempStat = state.AllIndividualsStats.get(state.AllGPIndividuals.get(i).hashCode()).clone();

				stats[i] = tempStat;
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		evaluator.objectives = this.objectives;
		evaluator.InitilizeSeed = EvaluationSeed;
		evaluator.setMinReplicationsPerConfiguration(Evaluation_InitialReplications);
		evaluator.setNumReplications(Evaluation_Replications);
		if (stats[0].numObs() > 1)
			evaluator.stats = stats;
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
			f.summaryStat = evaluator.stats[i];

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

		SummaryStat[] stats = Util.initializedArray(len, SummaryStat.class);
		for (int i = 0; i < len; i++) {
			try {

				state.AllGPIndividuals.get(i).trees[0].printTwoArgumentNonterminalsAsOperatorsInC = false;
				if (Normalization == 0) {
					evaluator.AddDispatchingRule(new testGPRule(state.AllGPIndividuals.get(i).trees[0].child));
				} else {
					evaluator
							.AddDispatchingRule(new NormalizedBrankeRule(state.AllGPIndividuals.get(i).trees[0].child));
				}

				SummaryStat tempStat = state.AllIndividualsStats.get(state.AllGPIndividuals.get(i).hashCode()).clone();
				stats[i] = tempStat;
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		evaluator.objectives = this.objectives;
		evaluator.InitilizeSeed = EvaluationSeed;
		evaluator.setMinReplicationsPerConfiguration(Evaluation_InitialReplications);
		evaluator.setNumReplications(Evaluation_Replications);

		if (stats[0].numObs() > 1)
			evaluator.stats = stats;
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
			f.summaryStat = evaluator.stats[i];

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

		SummaryStat[] stats = Util.initializedArray(len, SummaryStat.class);
		for (int i = 0; i < len; i++) {
			try {

				state.AllGPIndividuals.get(i).trees[0].printTwoArgumentNonterminalsAsOperatorsInC = false;
				if (Normalization == 0) {
					evaluator.AddDispatchingRule(new testGPRule(state.AllGPIndividuals.get(i).trees[0].child));
				} else {
					evaluator
							.AddDispatchingRule(new NormalizedBrankeRule(state.AllGPIndividuals.get(i).trees[0].child));
				}

				SummaryStat tempStat = state.AllIndividualsStats.get(state.AllGPIndividuals.get(i).hashCode()).clone();
				stats[i] = tempStat;
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		evaluator.objectives = this.objectives;
		evaluator.InitilizeSeed = EvaluationSeed;
		evaluator.setMinReplicationsPerConfiguration(Evaluation_InitialReplications);
		evaluator.setNumReplications(Evaluation_Replications);
		if (stats[0].numObs() > 1)
			evaluator.stats = stats;

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
			f.summaryStat = evaluator.stats[i];
			state.AllGPIndividuals.get(i).evaluated = true;
		}

		return;
	}

	// Added 2018.01.21
	// A new procedure, do full evaluation using our proposed ranking methods
	protected void fullEvaluation_Ranking(EvolutionState state) {

		long r = System.currentTimeMillis();

		PreWarmupTSOCBARun evaluator = new PreWarmupTSOCBARun();

		int len = state.AllGPIndividuals.size();

		SummaryStat[] stats = Util.initializedArray(len, SummaryStat.class);
		for (int i = 0; i < len; i++) {
			try {

				state.AllGPIndividuals.get(i).trees[0].printTwoArgumentNonterminalsAsOperatorsInC = false;
				if (Normalization == 0) {
					evaluator.AddDispatchingRule(new testGPRule(state.AllGPIndividuals.get(i).trees[0].child));
				} else {
					evaluator
							.AddDispatchingRule(new NormalizedBrankeRule(state.AllGPIndividuals.get(i).trees[0].child));
				}

				SummaryStat tempStat = state.AllIndividualsStats.get(state.AllGPIndividuals.get(i).hashCode()).clone();
				stats[i] = tempStat;
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		double indifferenceDelta = 0.01;

		indifferenceDelta = state.parameters.getDouble(new Parameter("evaluation.IDOCBA.IndifferenceDelta"), null);
		int warmupStageBudget = state.parameters.getInt(new Parameter("evaluation.IDOCBA.WarmupBudget"), null);
		int firstStageBudget = state.parameters.getInt(new Parameter("evaluation.IDOCBA.FirstStageBudget"), null);
		int OCBAIterationBudget = state.parameters.getInt(new Parameter("evaluation.IterationBudget"), null);

		evaluator.useElite = true;
		evaluator.setDelta(indifferenceDelta);
		evaluator.objectives = this.objectives;
		evaluator.InitilizeSeed = EvaluationSeed;
		evaluator.setMinReplicationsPerConfiguration(Evaluation_InitialReplications);
		evaluator.setNumReplications(Evaluation_Replications);
		evaluator.stats = stats;
		evaluator.curGeneration = 0;

		//The first stage is 0, a trick
		evaluator.init(warmupStageBudget, 0, firstStageBudget, OCBAIterationBudget);
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
			f.summaryStat = evaluator.stats[i];
			state.AllGPIndividuals.get(i).evaluated = true;
		}

		return;
	}

	// Added 2017.10.17
	// A new procedure, do full evaluation using AOAP methods
	protected void fullEvaluation_AOAP(EvolutionState state) {

		long r = System.currentTimeMillis();

		AOAPRun evaluator = new AOAPRun();

		int len = state.AllGPIndividuals.size();

		SummaryStat[] stats = Util.initializedArray(len, SummaryStat.class);
		for (int i = 0; i < len; i++) {
			try {

				state.AllGPIndividuals.get(i).trees[0].printTwoArgumentNonterminalsAsOperatorsInC = false;
				if (Normalization == 0) {
					evaluator.AddDispatchingRule(new testGPRule(state.AllGPIndividuals.get(i).trees[0].child));
				} else {
					evaluator
							.AddDispatchingRule(new NormalizedBrankeRule(state.AllGPIndividuals.get(i).trees[0].child));
				}

				SummaryStat tempStat = state.AllIndividualsStats.get(state.AllGPIndividuals.get(i).hashCode()).clone();
				stats[i] = tempStat;
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		evaluator.objectives = this.objectives;
		evaluator.InitilizeSeed = EvaluationSeed;
		evaluator.setMinReplicationsPerConfiguration(Evaluation_InitialReplications);
		evaluator.setNumReplications(Evaluation_Replications);
		if (stats[0].numObs() > 1)
			evaluator.stats = stats;
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
			f.summaryStat = evaluator.stats[i];
			state.AllGPIndividuals.get(i).evaluated = true;
		}

		return;
	}
}
