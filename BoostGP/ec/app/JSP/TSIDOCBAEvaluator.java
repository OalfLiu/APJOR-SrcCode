/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.app.JSP;

import java.io.File;
import java.io.IOException;

import ec.*;
import ec.gp.GPIndividual;
import ec.gp.koza.KozaFitness;
import ec.simple.SimpleEvaluator;
import ec.simple.SimpleProblemForm;
import ec.util.*;
import jasima.core.experiment.OCBARun;
import jasima.core.experiment.PreWarmupTSOCBARun;
import jasima.core.experiment.TSIndifferentOCBARun;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.EJobShopObjectives;
import jasima.core.util.Util;
import jasima.core.util.Util.TypeOfObjective;
import jasima.shopSim.prioRules.gp.NormalizedBrankeRule;
import jasima.shopSim.prioRules.gp.testGPRule;

/* 
 * SimpleEvaluator.java
 * 
 * Created: Wed Aug 18 21:31:18 1999
 * By: Sean Luke
 */

/**
 * The SimpleEvaluator is a simple, non-coevolved generational evaluator which
 * evaluates every single member of every subpopulation individually in its own
 * problem space. One Problem instance is cloned from p_problem for each
 * evaluating thread. The Problem must implement SimpleProblemForm.
 *
 * @author Sean Luke
 * @version 2.0
 *
 *          Thanks to Ralf Buschermohle <lobequadrat@googlemail.com> for early
 *          versions of code which led to this version.
 *
 */

public class TSIDOCBAEvaluator extends SimpleEvaluator {

	public static final String P_Simulation_Seed = "Simulation.Seed";
	public static final String P_Simulation_NumofJobs = "Simulation.NumOfJobs";
	public static final String P_Simulation_NumofMachines = "Simulation.NumofMachines";
	public static final String P_Simulation_Replications = "Simulation.Replications";
	public static final String P_Simulation_Objective = "Simulation.Objective";
	public static final String P_Simulation_UtilizationLevel = "Simulation.UtilizationLevel";
	public static final String P_Simulation_OCBA_IterationBudget = "Simulation.IterationOCAPBudget";
	public static final String P_Simulation_log = "Simulation.file";
	public static final String P_Normalization = "Normalization";

	public int Normalization = 0;
	public long InitialSeed = 83461;
	public int NumOfReplications = 50;
	public int NumOfJobs = 2500;
	public int NumOfMachines = 10;
	public double UtilizationLevel = 0.95d;
	public File statisticsFile;
	public int OCBAIterationBudget = 5;
	public EJobShopObjectives ShopObjective = EJobShopObjectives.MeanFlowTime;
	public int objectives = 1;

	public int statisticslog = 0;

	// New parameter for two stage OCBA
	public int firstStageBudget = 5;
	public int secondStageBudget = 5;
	public int warmupStageBudget = 10;
	public int preWamrupStageBudget = 5;
	public double indifferenceDelta = 0.1;
	public Boolean useElite = false;
	public Boolean isGreedyAllocation = true;
	
	public TypeOfObjective typeOfObjective = TypeOfObjective.mean;

	public void setup(final EvolutionState state, final Parameter base) {

		super.setup(state, base);
		this.NumOfJobs = state.parameters.getInt(new Parameter("eval.problem.Simulation.Replications"), null);
		this.NumOfMachines = state.parameters.getInt(new Parameter("eval.problem.Simulation.NumofMachines"), null);
		this.InitialSeed = state.parameters.getLongWithDefault(new Parameter("eval.problem.Simulation.Seed"), null,
				System.currentTimeMillis());
		this.NumOfReplications = state.parameters
				.getIntWithDefault(new Parameter("eval.problem.Simulation.Replications"), null, 1);
		this.UtilizationLevel = state.parameters.getDouble(new Parameter("eval.problem.Simulation.UtilizationLevel"),
				null);

		this.Normalization = state.parameters.getIntWithDefault(new Parameter("eval.problem.Normalization"), null, 0);

		this.OCBAIterationBudget = state.parameters.getInt(new Parameter("eval.Simulation.IterationBudget"), null);
		objectives = state.parameters.getInt(new Parameter("eval.problem.Simulation.Objective"), null);

		indifferenceDelta = state.parameters.getDouble(new Parameter("eval.Simulation.IndifferenceDelta"), null);
		this.warmupStageBudget = state.parameters.getInt(new Parameter("eval.Simulation.WarmupBudget"), null);
		this.firstStageBudget = state.parameters.getInt(new Parameter("eval.Simulation.FirstStageBudget"), null);
		this.secondStageBudget = state.parameters.getInt(new Parameter("eval.Simulation.SecondStageBudget"), null);
		preWamrupStageBudget = state.parameters.getInt(new Parameter("eval.Simulation.WarmupBudget_Pre"), null);
		if (state.parameters.getInt(new Parameter("breed.elite.0"), null) > 0) {
			useElite = true;
		} else
			useElite = false;
		
		this.typeOfObjective = TypeOfObjective.valueOf(state.parameters.getString(new Parameter("eval.problem.Simulation.ObjectiveType"), null))  ;

	}

	protected void evalPopChunk(EvolutionState state, int[] numinds, int[] from, int threadnum, SimpleProblemForm p) {
		((ec.Problem) p).prepareToEvaluate(state, threadnum);

		// If load from file, we do not need to do evaluation, go to final
		// evaluation
		if (state.parameters.getBoolean(new Parameter("loadFromFile"), null, false))
			return;

		PreWarmupTSOCBARun run = new PreWarmupTSOCBARun();
		run.typeOfObjective = this.typeOfObjective;

		Subpopulation[] subpops = state.population.subpops;
		int len = subpops.length;
		for (int pop = 0; pop < len; pop++) {
			// start evaluation
			int fp = from[pop];
			int upperbound = fp + numinds[pop];
			Individual[] inds = subpops[pop].individuals;
			SummaryStat[] stats = Util.initializedArray(inds.length, SummaryStat.class);

			for (int x = fp; x < upperbound; x++) {
				try {

					if (Normalization == 0) {
						run.AddDispatchingRule(new testGPRule(((GPIndividual) inds[x]).trees[0].child));
					} else {
						run.AddDispatchingRule(new NormalizedBrankeRule(((GPIndividual) inds[x]).trees[0].child));
					}
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				KozaFitness f = (KozaFitness) (((GPIndividual) inds[x]).fitness);

				if (f.summaryStat == null) {
					continue;
				}

				if (f.summaryStat.isNew()) {
					continue;
				} else {
					stats[x] = f.summaryStat;
				}
			}

			Boolean UniqueSeed = state.parameters.getBoolean(new Parameter("eval.problem.Simulation.UniqueSeed"), null,
					true);

			if (UniqueSeed)
				run.InitilizeSeed = this.InitialSeed;
			else
				run.InitilizeSeed = this.InitialSeed + state.generation;

			run.useElite = useElite;
			run.curGeneration = state.generation;
			run.stats = stats;
			run.objectives = this.objectives;
			run.setMinReplicationsPerConfiguration(this.OCBAIterationBudget);
			run.setNumReplications(this.NumOfReplications);
			run.setDelta(indifferenceDelta);
			run.preWamrupStageBudget = this.preWamrupStageBudget;

			run.init(this.warmupStageBudget, this.firstStageBudget, this.secondStageBudget, this.OCBAIterationBudget);
			run.runEvaluation();

			for (int x = fp; x < upperbound; x++) {
				((GPIndividual) inds[x]).trees[0].printTwoArgumentNonterminalsAsOperatorsInC = false;
				KozaFitness f = (KozaFitness) (inds[x].fitness);
				f.setStandardizedFitness(state, run.stats[x].mean());
				f.setVariance(state, run.stats[x].stdDev());
				f.NumOfEvaluations = run.stats[x].numObs();
				f.summaryStat = run.stats[x];

				inds[x].evaluated = true;
			}

			run.produceResults();
		}

		((ec.Problem) p).finishEvaluating(state, threadnum);
	}

}
