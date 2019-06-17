/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package ec.app.JSP;

import ec.*;
import ec.gp.*;
import ec.gp.koza.KozaFitness;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import ec.util.*;
import jasima.core.experiment.AOAPRun;
import jasima.core.experiment.AOAPRun.ProblemType;
import jasima.core.statistics.SummaryStat;
import jasima.shopSim.prioRules.gp.NormalizedBrankeRule;
import jasima.shopSim.prioRules.gp.testGPRule;
import ec.simple.*;

/* 
 * MyGPStatistics.java
 * 
 * Created: 2017/08/31
 * By: Zhang Cheng
 */

/**
 * A Koza-style statistics generator, intended to be easily parseable with awk
 * or other Unix tools. Prints fitness information, one generation (or
 * pseudo-generation) per line. If gather-full is true, then timing information,
 * number of nodes and depths of trees, etc. are also given. No final statistics
 * information is given.
 *
 * <p>
 * Each line represents a single generation. The first items on a line are
 * always:
 * <ul>
 * <li>The generation number
 * <li>(if do-time) how long initialization took in milliseconds, or how long
 * the previous generation took to breed to form this generation
 * <li>(if do-time) How long evaluation took in milliseconds this generation
 * </ul>
 * 
 * <p>
 * Then, (if do-subpops) the following items appear, once per each
 * subpopulation:
 * <ul>
 * <li>(if do-depth) [a b c...], representing the average depth of tree
 * <i>a</i>, <i>b</i>, etc. of individuals this generation
 * <li>(if do-size) [a b c...], representing the average number of nodes used in
 * tree <i>a</i>, <i>b</i>, etc. of individuals this generation
 * <li>(if do-size) The average size of an individual this generation
 * <li>(if do-size) The average size of an individual so far in the run
 * <li>(if do-size) The size of the best individual this generation
 * <li>(if do-size) The size of the best individual so far in the run
 * <li>The mean standardized fitness of the subpopulation this generation
 * <li>The best standardized fitness of the subpopulation this generation
 * <li>The best standardized fitness of the subpopulation so far in the run
 * </ul>
 * 
 * <p>
 * Then the following items appear, for the whole population:
 * <ul>
 * <li>(if do-depth) [a b c...], representing the average depth of tree
 * <i>a</i>, <i>b</i>, etc. of individuals this generation
 * <li>(if do-size) [a b c...], representing the average number of nodes used in
 * tree <i>a</i>, <i>b</i>, etc. of individuals this generation
 * <li>(if do-size) The average size of an individual this generation
 * <li>(if do-size) The average size of an individual so far in the run
 * <li>(if do-size) The size of the best individual this generation
 * <li>(if do-size) The size of the best individual so far in the run
 * <li>The mean standardized fitness of the subpopulation this generation
 * <li>The best standardized fitness of the subpopulation this generation
 * <li>The best standardized fitness of the subpopulation so far in the run
 * </ul>
 * 
 * KozaStatistics assumes that every one of the Individuals in your population
 * (and all subpopualtions) are GPIndividuals, and further that they all have
 * the same number of trees.
 * 
 * Besides the parameter below, KozaShortStatistics obeys all the
 * SimpleShortStatistics parameters.
 * 
 * <p>
 * <b>Parameters</b><br>
 * <table>
 * <tr>
 * <td valign=top><i>base</i>.<tt>do-depth</tt><br>
 * <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 * <td valign=top>(print depth information?)</td>
 * </tr>
 * </table>
 * 
 * @author Sean Luke
 * @version 1.0
 */

public class GPSingleStatisticsEx extends SimpleShortStatistics {

	public static final String P_DO_DEPTH = "do-depth";
	
	public int fileExlog;
	public double evaluationThreshold;

	public boolean doDepth;
	public boolean doFinalEvaluation = false;
	public boolean doLoadFile = false;

	long totalDepthSoFarTree[][];
	long[][] totalDepthThisGenTree; // per-subpop total size of individuals this
									// generation per tree
	long totalSizeSoFarTree[][];
	long[][] totalSizeThisGenTree; // per-subpop total size of individuals this
									// generation per tree

	protected void gatherExtraSubpopStatistics(EvolutionState state, int subpop, int individual) {
		GPIndividual i = (GPIndividual) (state.population.subpops[subpop].individuals[individual]);
		for (int z = 0; z < i.trees.length; z++) {
			totalDepthThisGenTree[subpop][z] += i.trees[z].child.depth();
			totalDepthSoFarTree[subpop][z] += totalDepthThisGenTree[subpop][z];
			totalSizeThisGenTree[subpop][z] += i.trees[z].child.numNodes(GPNode.NODESEARCH_ALL);
			totalSizeSoFarTree[subpop][z] += totalSizeThisGenTree[subpop][z];
		}
	}

	public void postInitializationStatistics(final EvolutionState state) {
		super.postInitializationStatistics(state);

		totalDepthSoFarTree = new long[state.population.subpops.length][];
		totalSizeSoFarTree = new long[state.population.subpops.length][];

		for (int x = 0; x < state.population.subpops.length; x++) {
			// check to make sure they're the right class
			if (!(state.population.subpops[x].species instanceof GPSpecies))
				state.output.fatal("Subpopulation " + x + " is not of the species form GPSpecies."
						+ "  Cannot do timing statistics with KozaShortStatistics.");

			GPIndividual i = (GPIndividual) (state.population.subpops[x].individuals[0]);
			totalDepthSoFarTree[x] = new long[i.trees.length];
			totalSizeSoFarTree[x] = new long[i.trees.length];
		}
	}

	protected void prepareStatistics(EvolutionState state) {
		totalDepthThisGenTree = new long[state.population.subpops.length][];
		totalSizeThisGenTree = new long[state.population.subpops.length][];

		for (int x = 0; x < state.population.subpops.length; x++) {
			GPIndividual i = (GPIndividual) (state.population.subpops[x].individuals[0]);
			totalDepthThisGenTree[x] = new long[i.trees.length];
			totalSizeThisGenTree[x] = new long[i.trees.length];
		}
	}	

	//评估后处理
	//对于每一代评估完后，我们将个体保存下来，为之后的final evaluation做准备
	public void postEvaluationStatistics(EvolutionState state) {
		super.postEvaluationStatistics(state);

		if (state.AllGPIndividuals == null)
			state.AllGPIndividuals = new ArrayList<GPIndividual>();
		
		if(state.AllIndividualsStats == null)
		{
			state.AllIndividualsStats = new HashMap<Integer, SummaryStat>();
		}
		
		if (state.totalGPIndividuals == null)
			state.totalGPIndividuals = new ArrayList<GPIndividual>();
		
		if (state.allPhenotypicSummarys == null)
			state.allPhenotypicSummarys = new ArrayList<SummaryStat>();

		int subpops = state.population.subpops.length;
		int index = 0;
		
		DummyOperationWrapper doWrapper = new DummyOperationWrapper();
		doWrapper.Normalization = state.parameters.getIntWithDefault(new Parameter("eval.problem.Normalization"), null, 0);
	    doWrapper.objectives = state.parameters.getInt(new Parameter("eval.problem.Simulation.Objective"), null);
		
		doWrapper.Init();
		
		for (int x = 0; x < subpops; x++) {			
			
			Arrays.sort(state.population.subpops[x].individuals, new FitnessComprator());			
			
			for (int y = 0; y < state.population.subpops[x].individuals.length; y++) {
				if (state.population.subpops[x].individuals[y].evaluated) // he's
																			// got
																			// a
																			// valid
																			// fitness
				{					
					KozaFitness fit = (KozaFitness) state.population.subpops[x].individuals[y].fitness;
					
					((GPIndividual) state.population.subpops[x].individuals[y]).bornGeneration = state.generation;

					state.output.println("mean:" + String.format("%.6f", fit.standardizedFitness()) + " stddev:"
							+ String.format("%.6f", fit.variance) + " size:"
							+ state.population.subpops[x].individuals[y].size()
							//+ " Hashcode:" + state.population.subpops[x].individuals[y].hashCode()
							+ " Evaluations:" + fit.NumOfEvaluations
							+ "  DR:" + ((GPIndividual) state.population.subpops[x].individuals[y]).trees[0].child
									.makeCTree(true, true, false),

							statisticslog);
					
					//We want to screen out some inferior individuals
					if (!state.totalGPIndividuals.contains((GPIndividual) state.population.subpops[x].individuals[y])) 					
					{
						
						if(!this.doLoadFile)
						{
							SummaryStat pfitness = doWrapper.getSimulationSummaryOfGPNode(((GPIndividual) state.population.subpops[x].individuals[y]).trees[0].child);
							
							/* test if this individual is same with others */
							Boolean isSame = false;
							for(SummaryStat stat : state.allPhenotypicSummarys)
							{
								if(stat.mean() == pfitness.mean() && stat.min() == pfitness.min() && stat.max() == pfitness.max())
								{
									isSame = true;
									break;
								}
							}
							
							//if same with existed individuals, we ignore this one
							if(isSame)
							{
								continue;
							}
							else
							{
								state.allPhenotypicSummarys.add(pfitness);
							}
							/* test end */
						}					
						
						//Store individuals
						state.totalGPIndividuals.add((GPIndividual) state.population.subpops[x].individuals[y]);
					
						
						//Store HashMap of individuals with their simulation statistics result
						state.AllIndividualsStats.put(((GPIndividual)state.population.subpops[x].individuals[y]).hashCode(), fit.summaryStat);
					}
					//if exists, then we update its summaryStat
					else
					{
						GPIndividual ind = state.totalGPIndividuals.get(state.totalGPIndividuals.indexOf((GPIndividual) state.population.subpops[x].individuals[y]));
						KozaFitness indfit  = (KozaFitness)ind.fitness;
						indfit.summaryStat = fit.summaryStat;
						
						//SummaryStat stat = state.AllIndividualsStats.get(state.population.subpops[x].individuals[y].hashCode());
						
						state.AllIndividualsStats.put(((GPIndividual)state.population.subpops[x].individuals[y]).hashCode(), fit.summaryStat);
						
//						if(stat != null)
//						{
//							try {
//								stat = fit.summaryStat.clone();
//							} catch (CloneNotSupportedException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}
					}
					
				}
			}
			
			state.output.println("", statisticslog);

//			// Last Generation
//			if (state.generation >= state.numGenerations - 1) {
//				for (int y = 0; y < state.population.subpops[x].individuals.length; y++) {
//					if (y <= 0) {
//						state.output.println("Current top " + y + " individual's dot", this.statisticslog);
//						state.output.println(((GPIndividual) state.population.subpops[x].individuals[y]).trees[0].child
//								.makeGraphvizTree(), statisticslog);
//					} else {
//						break;
//					}
//				}
//			}
		}

//		if (false && state.generation >= state.numGenerations - 1) {
//			this.fullEvaluation(state);
//		}
	}

	protected void printExtraPopStatisticsBefore(EvolutionState state) {
		long[] totalDepthThisGenTreePop = new long[totalDepthSoFarTree[0].length];
		long[] totalSizeThisGenTreePop = new long[totalSizeSoFarTree[0].length]; // will
																					// assume
																					// each
																					// subpop
																					// has
																					// the
																					// same
																					// tree
																					// size
		long totalIndsThisGenPop = 0;
		long totalDepthThisGenPop = 0;
		long totalDepthSoFarPop = 0;

		int subpops = state.population.subpops.length;

		for (int y = 0; y < subpops; y++) {
			totalIndsThisGenPop += totalIndsThisGen[y];
			for (int z = 0; z < totalSizeThisGenTreePop.length; z++)
				totalSizeThisGenTreePop[z] += totalSizeThisGenTree[y][z];
			for (int z = 0; z < totalDepthThisGenTreePop.length; z++)
				totalDepthThisGenTreePop[z] += totalDepthThisGenTree[y][z];
		}

		if (doDepth) {
			state.output.print("[ ", statisticslog);
			for (int z = 0; z < totalDepthThisGenTreePop.length; z++)
				state.output.print(""
						+ (totalIndsThisGenPop > 0 ? ((double) totalDepthThisGenTreePop[z]) / totalIndsThisGenPop : 0)
						+ " ", statisticslog);
			state.output.print("] ", statisticslog);
		}
		if (doSize) {
			state.output.print("[ ", statisticslog);
			for (int z = 0; z < totalSizeThisGenTreePop.length; z++)
				state.output.print(
						"" + (totalIndsThisGenPop > 0 ? ((double) totalSizeThisGenTreePop[z]) / totalIndsThisGenPop : 0)
								+ " ",
						statisticslog);
			state.output.print("] ", statisticslog);
		}
	}

	protected void printExtraSubpopStatisticsBefore(EvolutionState state, int subpop) {
		if (doDepth) {
			state.output.print("[ ", statisticslog);
			for (int z = 0; z < totalDepthThisGenTree[subpop].length; z++)
				state.output.print(
						"" + (totalIndsThisGen[subpop] > 0
								? ((double) totalDepthThisGenTree[subpop][z]) / totalIndsThisGen[subpop] : 0) + " ",
						statisticslog);
			state.output.print("] ", statisticslog);
		}
		if (doSize) {
			state.output.print("[ ", statisticslog);
			for (int z = 0; z < totalSizeThisGenTree[subpop].length; z++)
				state.output.print(
						"" + (totalIndsThisGen[subpop] > 0
								? ((double) totalSizeThisGenTree[subpop][z]) / totalIndsThisGen[subpop] : 0) + " ",
						statisticslog);
			state.output.print("] ", statisticslog);
		}
	}

	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		doDepth = state.parameters.getBoolean(base.push(P_DO_DEPTH), null, false);
		doFinalEvaluation = state.parameters.getBoolean(new Parameter("doFinalEvaluation"), null, false);
		evaluationThreshold = state.parameters.getDouble(new Parameter("evaluation.Threshold"), null);
		
		if(state.parameters.exists(new Parameter("loadFromFile"), null))
			doLoadFile = state.parameters.getBoolean(new Parameter("loadFromFile"), null, false);
		
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
	}
}
