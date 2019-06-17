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
import jasima.core.experiment.EIRun;
import jasima.core.experiment.KnowledgeGradientRun;
import jasima.core.experiment.OCBARun;
import jasima.core.util.EJobShopObjectives;
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

public class ExpectationInEvaluator extends SimpleEvaluator {

	public static final String P_Simulation_Seed = "Simulation.Seed";
	public static final String P_Simulation_NumofJobs = "Simulation.NumOfJobs";
	public static final String P_Simulation_NumofMachines = "Simulation.NumofMachines";
	public static final String P_Simulation_Replications = "Simulation.Replications";
	public static final String P_Simulation_Objective = "Simulation.Objective";
	public static final String P_Simulation_UtilizationLevel = "Simulation.UtilizationLevel";
	public static final String P_Simulation_OCBA_IterationBudget = "Simulation.IterationBudget";
	public static final String P_Simulation_log = "Simulation.file";
	public static final String P_Normalization = "Normalization";

	public int Normalization = 0;
	public long InitialSeed = 83461;
	public int NumOfReplications = 50;
	public int NumOfJobs = 2500;
	public int NumOfMachines = 10;
	public double UtilizationLevel = 0.95d;
	public File statisticsFile;
	public int IterationBudget = 1;
	public EJobShopObjectives ShopObjective = EJobShopObjectives.MeanFlowTime;
	
	public int statisticslog = 0;

	public void setup(final EvolutionState state, final Parameter base) {
		
		super.setup(state, base);
		this.NumOfJobs = state.parameters.getInt(new Parameter("eval.problem.Simulation.Replications"), null);
		this.NumOfMachines = state.parameters.getInt(new Parameter("eval.problem.Simulation.NumofMachines"), null);
		this.InitialSeed = state.parameters.getLongWithDefault(new Parameter("eval.problem.Simulation.Seed"), null, System.currentTimeMillis());
		this.NumOfReplications = state.parameters.getIntWithDefault(new Parameter("eval.problem.Simulation.Replications"), null, 1);
		this.UtilizationLevel = state.parameters.getDouble(new Parameter("eval.problem.Simulation.UtilizationLevel"), null);
		
		this.Normalization =  state.parameters.getIntWithDefault(new Parameter("eval.problem.Normalization"), null, 0);
		
		this.IterationBudget = state.parameters.getInt(base.push(P_Simulation_OCBA_IterationBudget), null);
		  statisticsFile = state.parameters.getFile(
		            base.push(P_Simulation_log),null);
		  
	

	}

	protected void evalPopChunk(EvolutionState state, int[] numinds, int[] from, int threadnum, SimpleProblemForm p) {
		((ec.Problem) p).prepareToEvaluate(state, threadnum);

		EIRun run = new EIRun();

		Subpopulation[] subpops = state.population.subpops;
		int len = subpops.length;

		for (int pop = 0; pop < len; pop++) {
			// start evaluation
			int fp = from[pop];
			int upperbound = fp + numinds[pop];
			Individual[] inds = subpops[pop].individuals;
			for (int x = fp; x < upperbound; x++) {
				try {
					
					if(Normalization == 0)
					{
						run.AddDispatchingRule(new testGPRule(((GPIndividual) inds[x]).trees[0].child));
					}
					else						
					{
						run.AddDispatchingRule(new NormalizedBrankeRule(((GPIndividual) inds[x]).trees[0].child));
					}
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			run.InitilizeSeed = this.InitialSeed;			
			run.setMinReplicationsPerConfiguration(this.IterationBudget);
			run.setNumReplications(this.NumOfReplications);
			
			run.init();
			run.runEvaluation();
			
			for (int x = fp; x < upperbound; x++) {
				((GPIndividual)inds[x]).trees[0].printTwoArgumentNonterminalsAsOperatorsInC = false;
				KozaFitness f = (KozaFitness)(inds[x].fitness);
				f.setStandardizedFitness(state,  run.stats[x].mean());
				f.setVariance(state, run.stats[x].stdDev());
				f.NumOfEvaluations = run.stats[x].numObs();
				
				inds[x].evaluated = true;
			}
			
			run.produceResults();
			run.printResults(statisticsFile);
			run.printResults();
		}

		((ec.Problem) p).finishEvaluating(state, threadnum);
	}

}
