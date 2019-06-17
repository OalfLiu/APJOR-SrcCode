/*******************************************************************************
 * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
 * logistics.
 *  
 * Copyright (c) 2015 		jasima solutions UG
 * Copyright (c) 2010-2015 Torsten Hildebrandt and jasima contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package jasima.core.experiment;

import jasima.core.experiment.Experiment.UniqueNamesCheckingHashMap;
import jasima.core.experiment.OCBAExperiment.ProblemType;
import jasima.core.random.continuous.DblConst;
import jasima.core.random.continuous.DblUniformRange;
import jasima.core.random.discrete.IntUniformRange;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.Simulation.SimEvent;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.ConsolePrinter;
import jasima.core.util.EJobShopObjectives;
import jasima.core.util.Util;
import jasima.core.util.observer.NotifierListener;
import jasima.shopSim.core.JobShopExperiment;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.batchForming.GPPrioRuleBatching;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment.Scenario;
import jasima.shopSim.models.mimac.MimacExperiment;
import jasima.shopSim.models.mimac.MimacExperiment.DataSet;
import jasima.shopSim.prioRules.basic.FASFS;
import jasima.shopSim.prioRules.basic.SPT;
import jasima.shopSim.prioRules.batch.BATCS;
import jasima.shopSim.prioRules.gp.MyCustomGP;
import jasima.shopSim.prioRules.gp.testGPRule;
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;
import jasima.shopSim.prioRules.upDownStream.WINQ;
import jasima.shopSim.util.BasicJobStatCollector;
import jasima.shopSim.util.DeviationJobStatCollector;
import jasima.shopSim.util.ExtendedJobStatCollector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;

import ec.util.Output;
import ec.util.Parameter; 

public class OCBASSNewRun extends BaseEvaluator {

	public enum ProblemType {
		MINIMIZE, MAXIMIZE
	};

	// Dynamic JobShop parameters
	public int NumberOfJobs = 2500;
	public int NumberOfMachines = 10;
	public int NumberOfOperations = 10;
	public double DuedateFactor = 4.0d;
	public double UtilizationLevel = 0.95d;
	public int MaxJobsInSystem = 500;
	public int InitialBudget = 5;
	protected Map<String, Object> resultMap = new UniqueNamesCheckingHashMap();;
	//
	// experiment parameters
	//
	private ProblemType problemType = ProblemType.MINIMIZE;
	private EJobShopObjectives objective = EJobShopObjectives.MeanFlowTime;
	private int minReplicationsPerConfiguration = 5;
	private int numReplications = 10;
	private double pcsLevel = 0.0;
	private boolean detailedResults = true;
	public int NumberOfDR = 0;
	public long InitilizeSeed = 20111205;
	private int numTaskExecuted = 0;
	protected Random seedStream;
	public int objectives = 1;

	//
	// fields used during experiment run
	//

	private int totalBudget, iterationBudget, budgetUsed;
	private ArrayList<MultipleReplicationExperiment> configurations;
	public ArrayList<PR> DispatchingRules;
	public SummaryStat[] stats;
	private double finalPCS;
	private int currBest;
	public int[] newRuns;
	
	public int warmupStageBudget = 10;
	public int firstStageBudget = 5, firstStageTotalBudget;
	
	public int topm=5;
	
	public OCBASSNewRun() {
		DispatchingRules = new ArrayList<PR>();
		configurations = new ArrayList<MultipleReplicationExperiment>();
		seedStream = null; 
	}

	public void AddDispatchingRule(PR pr) throws CloneNotSupportedException {
		
		this.DispatchingRules.add(pr);

		NumberOfDR++;
	}

	public void init()
	{
		numTaskExecuted = 0;
		newRuns = new int[NumberOfDR];
		iterationBudget = 0;
	}
	
	public void init(int wb, int fb, int ib) {
		
		numTaskExecuted = 0;
		budgetUsed = 0;
		newRuns = new int[NumberOfDR];
		iterationBudget = ib;
		warmupStageBudget = wb;
		firstStageBudget = fb;

		totalBudget = this.NumberOfDR * warmupStageBudget;
		firstStageTotalBudget = this.NumberOfDR * firstStageBudget;
	}
	
	//Initialization of OCBARun
	public MimacExperiment initBaseExperiment(PR pr) {	 
		MimacExperiment e = new MimacExperiment();
		e.setScenario(DataSet.SH75); 
		e.setDueDateFactors(new DblUniformRange(4.0, 9.0));
		e.setJobWeights(new IntUniformRange(1, 10));
		e.setSimulationLength(60*24*365*2);
		e.setMaxJobsInSystem(1100);
		e.setEnableLookAhead(false);
		  
		e.setBatchForming(new GPPrioRuleBatching(pr));
		e.setBatchSequencingRule(pr);
		
		testGPRule tgp =new testGPRule(null);
		tgp.isTestStringGPRule=true;
		e.setSequencingRule(new BATCS(1.0,1.5));
		
		ExtendedJobStatCollector stats = new ExtendedJobStatCollector(); 
		e.addShopListener(stats); 
		
		return e;
	}

	protected long getExperimentSeed() {
	
			if (seedStream == null)
				seedStream = new Random(InitilizeSeed);
			return seedStream.nextLong();
		
	} 
	
	protected void doRankingAndSelection() {
		while (this.hasMoreTasks()) {
			this.doOneRun();
		}
	}

	protected boolean hasMoreTasks() {		
		
		// identify currently best system
		currBest = 0;
		double bestMean = getProblemType() == ProblemType.MAXIMIZE ? stats[0].mean() : -stats[0].mean();
		for (int i = 1; i < stats.length; i++) {
			double v = getProblemType() == ProblemType.MAXIMIZE ? stats[i].mean() : -stats[i].mean();
			if (v > bestMean) {
				bestMean = v;
				currBest = i;
			}
		}

		// check stopping conditions
		if ((totalBudget > 0 && budgetUsed >= totalBudget))
			return false;

		// allocate new iterations
		int iter = 1;
		if (totalBudget > 0)
			iter = Math.min(iter, totalBudget - budgetUsed);	

		return true;
	}
	
	protected void doOneRun() {

		int index = this.CalculateBestIndex();

		MultipleReplicationExperiment mre = this.configurations.get(index);
		mre.setMaxReplications(iterationBudget);
		mre.setInitialSeed(this.getExperimentSeed());
		mre.runExperiment(); 
		
		double expAborted=((SummaryStat)mre.getResults().get("baseExperiment.expAborted")).sum();
		SummaryStat ss =(SummaryStat)mre.getResults().get(Util.getObjectiveString(this.objectives)); 
		if(expAborted>0) 
			this.stats[index].value(20000); 
		else {
			SummaryStat twt = (SummaryStat) mre.getResults().get(Util.getObjectiveString(this.objectives)); 
			//System.out.println(i);
			this.stats[index].combine(twt);
		}  

		budgetUsed++;
	}
	
	protected void doWarmup() {
		if (stats == null)
			stats = Util.initializedArray(this.NumberOfDR, SummaryStat.class);
		else
			return;

		int i = 0;
		for (PR pr : this.DispatchingRules) {
			MimacExperiment baseExperiment = this.initBaseExperiment(pr);
			MultipleReplicationExperiment mre = new MultipleReplicationExperiment();
			
			mre.setMaxReplications(1);
			mre.setBaseExperiment(baseExperiment);
			newRuns[i] = this.warmupStageBudget;			
			configurations.add(mre);
			
			for(int w=0;w<this.warmupStageBudget;w++) { 
				mre.setInitialSeed(this.getExperimentSeed());
				mre.runExperiment();
				Map<String,Object> res =mre.getResults();
				
				double expAborted=((SummaryStat)mre.getResults().get("baseExperiment.expAborted")).sum();
				SummaryStat ss =(SummaryStat)mre.getResults().get(Util.getObjectiveString(this.objectives)); 
				if(expAborted>0) 
					this.stats[i].value(20000); 
				else {
					SummaryStat twt = (SummaryStat) mre.getResults().get(Util.getObjectiveString(this.objectives)); 
					//System.out.println(i);
					this.stats[i].combine(twt);
				} 
			}
			
			
			i++;
		}
	}
	 
	public void  performOneRun()
	{
		
		long curSeed = this.InitilizeSeed;
		
		if(numTaskExecuted > 0)
		{
			curSeed = this.getExperimentSeed();
			
			int index = this.CalculateBestIndex();
			//int index = 0;
			
			MultipleReplicationExperiment mre = this.configurations.get(index);
			
			//step one
			mre.setMaxReplications(1);
			
			//new seed
			mre.setInitialSeed(curSeed);
						
			//run evaluation
			mre.runExperiment();	
			
			String obj = "flowtime.mean";			
			switch(this.objectives)
			{
				case 1:
					obj = "flowtime.mean";
					break;
				case 2:
					obj = "cMax.mean";
					break;
				case 3:
					obj = "tardiness.mean";
					break;
				case 11:
					obj = "flowtimeDev.mean";
					break;
			}
			
			SummaryStat flowtime = (SummaryStat)mre.getResults().get(obj);
			
			this.stats[index].combine(flowtime);	
			
			budgetUsed++;
		}
		else
		{
			for(int i = 0; i < this.NumberOfDR; i++)
			{			
				MultipleReplicationExperiment mre = this.configurations.get(i);
							
				//run evaluation
				mre.runExperiment();	
				
				String obj = "flowtime.mean";			
				switch(this.objectives)
				{
					case 1:
						obj = "flowtime.mean";
						break;
					case 2:
						obj = "cMax.mean";
						break;
					case 3:
						obj = "tardiness.mean";
						break;
					case 11:
						obj = "flowtimeDev.mean";
						break;
				}
				
				SummaryStat flowtime = (SummaryStat)mre.getResults().get(obj);
				
				this.stats[i].combine(flowtime);	
				budgetUsed += mre.getMaxReplications();
				System.out.println("budgetUsed=" + budgetUsed  + " total Budget=" + this.totalBudget + " Init");
			}
		}		
		System.out.println("budgetUsed=" + budgetUsed  + " total Budget=" + this.totalBudget);
		
		numTaskExecuted++;
	}
	
	public String printResultsToString()
	{
		StringBuilder outputBuilder = new StringBuilder();
		ArrayList<String> valStatNames = new ArrayList<String>();
		ArrayList<String> otherNames = new ArrayList<String>();

		for (String k : resultMap.keySet()) {
			if (k != Experiment.RUNTIME) {
				Object v = resultMap.get(k);
				if (v instanceof SummaryStat) {
					valStatNames.add(k);
				} else {
					otherNames.add(k);
				}
			}
		}

		// sort by name, ignoring upper and lower case
		Collections.sort(valStatNames, new Comparator<String>() {

			public int compare(String s1, String s2) {
				return s1.compareToIgnoreCase(s2);
			}
		});
		Collections.sort(otherNames, new Comparator<String>() {

			public int compare(String s1, String s2) {
				return s1.compareToIgnoreCase(s2);
			}
		});

		// output ValueStat-objects
		if (valStatNames.size() > 0) {			
			outputBuilder.append("Name\tMean\tMin\tMax\tStdDev\tCount\tSum");

			for (String k : valStatNames) {
				SummaryStat vs = (SummaryStat) resultMap.get(k);
				outputBuilder.append(String.format(
						"%s\t%.4f\t%.4f\t%.4f\t%.4f\t%d\t%.4f%n", k, vs.mean(),
						vs.min(), vs.max(), vs.stdDev(), vs.numObs(), vs.sum()));
			}
		}

		// output all other objects (except runtime)
		if (otherNames.size() > 0) {
			outputBuilder.append("\n");
			outputBuilder.append("Name\tValue");

			for (String k : otherNames) {
				Object v = resultMap.get(k);
				if (v != null) {
					if (v.getClass().isArray())
						v = Util.arrayToString(v);
				
				}
				outputBuilder.append("\n" + k + "\t" + v);
			}
		}

		return outputBuilder.toString();
	}

	public void produceResults() {
		
		finalPCS = calcPCS();		
	
		resultMap.put("bestIndex", currBest);
		resultMap.put("bestPerformance", stats[currBest].mean());

		resultMap.put("numEvaluations", budgetUsed);
		resultMap.put("pcs", finalPCS);

		if (isDetailedResults()) {
			// allocation of evaluations to configurations
			int[] numRuns = new int[configurations.size()];
			double[] means = new double[stats.length];
			Experiment[] exps = new Experiment[configurations.size()];
			for (int i = 0; i < configurations.size(); i++) {
				exps[i] = configurations.get(i).getBaseExperiment();

				SummaryStat vs = stats[i];
				numRuns[i] = vs.numObs();
				means[i] = vs.mean();
			}
			resultMap.put("allocationVector", numRuns);
			resultMap.put("meansVector", means);
			//resultMap.put("configurations", exps);
			// probability of configuration assumed being best to be better than
			// another configuration
			resultMap.put("probBestBetter", calcPCSPriosPerConfiguration());
			resultMap.put("rank", findRank(means));
		}
	}	
	
	public void printResults(File statisticsFile) {
	
	 	FileWriter fw;
		try {
			fw = new FileWriter(statisticsFile);
			
			PrintWriter out = new PrintWriter(fw, true);
			ArrayList<String> valStatNames = new ArrayList<String>();
			ArrayList<String> otherNames = new ArrayList<String>();

			for (String k : resultMap.keySet()) {
				if (k != Experiment.RUNTIME) {
					Object v = resultMap.get(k);
					if (v instanceof SummaryStat) {
						valStatNames.add(k);
					} else {
						otherNames.add(k);
					}
				}
			}

			// sort by name, ignoring upper and lower case
			Collections.sort(valStatNames, new Comparator<String>() {

				public int compare(String s1, String s2) {
					return s1.compareToIgnoreCase(s2);
				}
			});
			Collections.sort(otherNames, new Comparator<String>() {

				public int compare(String s1, String s2) {
					return s1.compareToIgnoreCase(s2);
				}
			});

			// output ValueStat-objects
			if (valStatNames.size() > 0) {
				out.println();
				out.println("Name\tMean\tMin\tMax\tStdDev\tCount\tSum");

				for (String k : valStatNames) {
					SummaryStat vs = (SummaryStat) resultMap.get(k);
					out.printf(Util.DEF_LOCALE,
							"%s\t%.4f\t%.4f\t%.4f\t%.4f\t%d\t%.4f%n", k, vs.mean(),
							vs.min(), vs.max(), vs.stdDev(), vs.numObs(), vs.sum());
				}
			}

			// output all other objects (except runtime)
			if (otherNames.size() > 0) {
				out.println();
				out.println("Name\tValue");

				for (String k : otherNames) {
					Object v = resultMap.get(k);
					if (v != null) {
						if (v.getClass().isArray())
							v = Util.arrayToString(v);
					
					}
					out.println(k + "\t" + v);
				}
			}

			out.println();	

			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	
		
	}
	
	public void printResults() {
		
		PrintWriter out = new PrintWriter(System.out, true);
		ArrayList<String> valStatNames = new ArrayList<String>();
		ArrayList<String> otherNames = new ArrayList<String>();

		for (String k : resultMap.keySet()) {
			if (k != Experiment.RUNTIME) {
				Object v = resultMap.get(k);
				if (v instanceof SummaryStat) {
					valStatNames.add(k);
				} else {
					otherNames.add(k);
				}
			}
		}

		// sort by name, ignoring upper and lower case
		Collections.sort(valStatNames, new Comparator<String>() {

			public int compare(String s1, String s2) {
				return s1.compareToIgnoreCase(s2);
			}
		});
		Collections.sort(otherNames, new Comparator<String>() {

			public int compare(String s1, String s2) {
				return s1.compareToIgnoreCase(s2);
			}
		});

		// output ValueStat-objects
		if (valStatNames.size() > 0) {
			out.println();
			out.println("Name\tMean\tMin\tMax\tStdDev\tCount\tSum");

			for (String k : valStatNames) {
				SummaryStat vs = (SummaryStat) resultMap.get(k);
				out.printf(Util.DEF_LOCALE,
						"%s\t%.4f\t%.4f\t%.4f\t%.4f\t%d\t%.4f%n", k, vs.mean(),
						vs.min(), vs.max(), vs.stdDev(), vs.numObs(), vs.sum());
			}
		}

		// output all other objects (except runtime)
		if (otherNames.size() > 0) {
			out.println();
			out.println("Name\tValue");

			for (String k : otherNames) {
				Object v = resultMap.get(k);
				if (v != null) {
					if (v.getClass().isArray())
						v = Util.arrayToString(v);
				
				}
				out.println(k + "\t" + v);
			}
		}

		out.println();	

		out.flush();
	}
	
	public Map<String, Object> getResults() {
		return resultMap;
	}

	private int[] findRank(final double[] means) {
		Integer[] idx = new Integer[means.length];
		for (int i = 0; i < idx.length; i++) {
			idx[i] = i;
		}

		Arrays.sort(idx, new Comparator<Integer>() {
			@Override
			public int compare(Integer i1, Integer i2) {
				return (getProblemType() == ProblemType.MAXIMIZE ? -1 : +1)
						* Double.compare(means[i1.intValue()], means[i2.intValue()]);
			}
		});

		int[] ranks = new int[idx.length];
		for (int i = 0; i < ranks.length; i++) {
			ranks[idx[i].intValue()] = i + 1;
		}

		return ranks;
	}
	
	public void runEvaluation() {

		long start;
		long end;
		double diff;
		
		//Warming up period
		System.out.println("Warming Up Started");
		start = System.currentTimeMillis();
		doWarmup();
		end = System.currentTimeMillis();
		diff = (end - start) / 1000.0d;
		System.out.println("Warming Up Finishied in " + diff + " seconds");		
	

		//Ranking and selection
		System.out.println("Budget Allocation Stage Started");		
		this.budgetUsed = 0;
		this.totalBudget = this.firstStageTotalBudget;
		start = System.currentTimeMillis();
		this.doRankingAndSelection();
		end = System.currentTimeMillis();
		diff = (end - start) / 1000.0d;
		System.out.println("Budget Allocation Stage Finishied in " + diff + " seconds");	
		
		System.out.println("WarmingBudget=" + this.warmupStageBudget*this.NumberOfDR + " BudgetAllocationStageBudget=" + this.firstStageTotalBudget);

	}
 
	protected double calcPCS() {
		double[] prodTerms = calcPCSPriosPerConfiguration();

		double res = 1.0d;
		for (int i = 0; i < prodTerms.length; i++) {
			if (i == currBest)
				continue;

			res *= prodTerms[i];
		}

		return res;
	}

	protected double[] calcPCSPriosPerConfiguration() {
		final SummaryStat best = stats[currBest];
		final double bestMean = best.mean();

		double bestNormVariance = best.variance() / best.numObs();

		double[] prodTerms = new double[stats.length];
		for (int i = 0; i < stats.length; i++) {
			if (i == currBest)
				continue;

			SummaryStat vs = stats[i];
			prodTerms[i] = (bestMean - vs.mean()) / Math.sqrt(bestNormVariance + vs.variance() / vs.numObs());
		}

		NormalDistribution normalDist = new NormalDistribution();

		for (int i = 0; i < stats.length; i++) {
			if (i == currBest)
				continue;

			prodTerms[i] = normalDist.cumulativeProbability(prodTerms[i]);
			if (getProblemType() == ProblemType.MINIMIZE)
				prodTerms[i] = 1.0 - prodTerms[i];
		}

		return prodTerms;
	}
	
	protected double calG(SummaryStat a,SummaryStat b) {
		double up= (a.mean()-b.mean())*(a.mean()-b.mean());
		double down1=a.variance()/((double)a.numObs()/(double)totalBudget);
		double down2=b.variance()/((double)b.numObs()/(double)totalBudget);
		return up/(2*(down1+down2));
	}
	
	
	protected int CalculateBestIndex()
	{
		int BestIndex = 0;
		
		final int nd = stats.length;

		double t_s_mean[] = new double[nd];
		if (getProblemType() == ProblemType.MAXIMIZE) { /* MAX problem */
			for (int i = 0; i < nd; i++)
				t_s_mean[i] = stats[i].mean();
		} else { /* MIN problem */
			for (int i = 0; i < nd; i++)
				t_s_mean[i] = -stats[i].mean();
		}
		 
		List<Integer> topmidx=topm_best(t_s_mean, topm);
		
		double Gmin=Double.POSITIVE_INFINITY;
		int index=0;
		for(int i=0;i<nd;i++) {			
			if(topmidx.contains(i)) {// calculate G_l,lr
				for(int r=0;r<nd;r++ ) {
					if(!topmidx.contains(r)) {
						double tempG=calG(stats[i],stats[r]);
						if(tempG<Gmin) {
							Gmin=tempG;
							index=i;
						}
					}
				}
			}
			if(!topmidx.contains(i)) {// calculate G_l,lr
				for(int r=0;r<nd;r++ ) {
					if(topmidx.contains(r)) {
						double tempG=calG(stats[i],stats[r]);
						if(tempG<Gmin) {
							Gmin=tempG;
							index=i;
						}
					}
				}
			}
		}
			 
				
		return index;
	}
  
	
	private static List<Integer> topm_best(final double t_s_mean[], int topm) {
		List<Integer> topmidx=new ArrayList<Integer>(); 
		double[] temp=t_s_mean;
		Arrays.sort(t_s_mean);
		for(int i=0;i<topm;i++) {
			for(int j=0;j<temp.length;j++) {
				if(t_s_mean[i]==temp[j]) {
					topmidx.add(j);
					break;
				}
			}				
		}		
		return topmidx;
	}
 

	/**
	 * Sets the minimum number of replications performed for each configuration.
	 * This has to be &gt;=3.
	 * 
	 * @param minReps
	 *            The minimum number of replications per configuration.
	 */
	public void setMinReplicationsPerConfiguration(int minReps) {
//		if (minReps < 3)
//			throw new IllegalArgumentException("Minimum number of replications has to be >=3.");
		this.minReplicationsPerConfiguration = minReps;
	}

	public int getMinReplicationsPerConfiguration() {
		return minReplicationsPerConfiguration;
	}

	/**
	 * Sets the name of the objective which defines "best". This has to be the
	 * name of a result produced by the base experiment.
	 * 
	 * @param objective
	 *            Result name to use as the objective function.
	 */
	public void setObjective(EJobShopObjectives objective) {
		this.objective = objective;
	}

	public EJobShopObjectives getObjective() {
		return objective;
	}

	/**
	 * Stop using more replications if this level of the probablity of correct
	 * selection is reached. This defines a dynamic stopping criterion.
	 * 
	 * @param pcsLevel
	 *            The desired confidence in the results (between 0 and 1).
	 */
	public void setPcsLevel(double pcsLevel) {
		if (pcsLevel < 0 || pcsLevel > 1)
			throw new IllegalArgumentException("Invalid probability: " + pcsLevel);
		this.pcsLevel = pcsLevel;
	}

	public double getPcsLevel() {
		return pcsLevel;
	}

	/**
	 * Whether to produce detailed results or just basic information of the best
	 * configuration.
	 * 
	 * @param detailedResults
	 *            Produce detailed results or not.
	 */
	public void setDetailedResults(boolean detailedResults) {
		this.detailedResults = detailedResults;
	}

	public boolean isDetailedResults() {
		return detailedResults;
	}

	/**
	 * Sets the total budget for each configuration.
	 * 
	 * @param numReplications
	 *            The number of replications to use.
	 */
	public void setNumReplications(int numReplications) {
		this.numReplications = numReplications;
	}

	public int getNumReplications() {
		return numReplications;
	}

	public ProblemType getProblemType() {
		return problemType;
	}

	/**
	 * Sets whether a minimization or maximization experiment should be solved.
	 * 
	 * @param problemType
	 *            Whether minimization or maximization are required.
	 */
	public void setProblemType(ProblemType problemType) {
		this.problemType = problemType;
	}

	public static void main(String[] args) throws CloneNotSupportedException
	{
		long t1 = System.currentTimeMillis(); 
		
		OCBASSNewRun run = new OCBASSNewRun();
		
		run.setNumReplications(100);
		run.setMinReplicationsPerConfiguration(10);
		run.InitilizeSeed = System.currentTimeMillis();
		
		
		String rule1 = "(0.0 - (If(div(RPT, RPT), 0.0 + If(TIQ, PT, TIS), If(0.0, PT, TIQ)) + ((WINQ * (0.0 + If(TIQ, PT, TIS))) * (NPT * 1.0)))) + (((div(If(1.0, WINQ, NPT), If(TIQ, PT, TIS) - ((If(If(WINQ * PT, If(OpsLeft, WINQ, PT), div(RPT, WINQ)) + (max(OpsLeft, 0.0) * div(RPT, 0.0)), If(OpsLeft, WINQ, PT), (WINQ * (0.0 + If(TIQ, PT, TIS))) * (NPT * 1.0)) + (max(OpsLeft, 0.0) * If(WINQ, max(div(PT, TIS), 0.0 + PT), 0.0))) - If(OpsLeft + RPT, WINQ, PT))) * If(0.0, WINQ, 0.0)) * (If(0.0, PT, 0.0 - PT) - (PT + PT))) - (TIS * PT))";
		String rule2 = "(((PT + PT) * (If(0.0, PT, PT) + (max(TIS, max(RPT, 0.0)) * If(0.0, PT, TIQ)))) * ((1.0 + PT) - (PT + PT))) + (((PT * (0.0 - PT)) * ((If(WINQ * max(PT, NPT), PT, max(RPT, 0.0)) + ((WINQ * max(PT, NPT)) * (NPT * 1.0))) + (max(RPT, 0.0) + max(PT, NPT)))) - (((((If(0.0, PT, TIQ) - PT) - ((PT + PT) - max(PT, NPT))) - PT) - PT) * ((PT + (PT + PT)) + (PT + PT))))";
		
		run.AddDispatchingRule(new MyCustomGP(rule1));
		run.AddDispatchingRule(new MyCustomGP(rule2));
		//run.setPcsLevel(0.99);
		run.init();
		run.runEvaluation();
		long t2 = System.currentTimeMillis();
		run.produceResults();
		run.printResults();
		System.out.println("Total seconds:" + (t2 - t1)/1000.0d );
	}
}
