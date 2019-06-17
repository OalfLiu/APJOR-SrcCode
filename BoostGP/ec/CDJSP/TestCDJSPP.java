package ec.CDJSP;

import ec.util.*;
import jasima.core.experiment.MultipleReplicationExperiment;
import jasima.core.random.continuous.DblConst;
import jasima.core.random.continuous.DblDistribution;
import jasima.core.random.continuous.DblStream;
import jasima.core.random.continuous.DblUniformRange;
import jasima.core.random.discrete.IntEmpirical;
import jasima.core.random.discrete.IntUniformRange;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.Simulation.SimEvent;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.EJobShopObjectives;
import jasima.core.util.Util;
import jasima.core.util.Util.TypeOfObjective;
import jasima.core.util.observer.NotifierListener;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.batchForming.BestOfFamilyBatching;
import jasima.shopSim.core.batchForming.GPBestOfFamilyBatching;
import jasima.shopSim.core.batchForming.GPHighestJobBatchingMBS;
import jasima.shopSim.core.batchForming.GPPrioRuleBatching;
import jasima.shopSim.core.batchForming.GPPGF;
import jasima.shopSim.core.batchForming.HighestJobBatchingMBS;
import jasima.shopSim.core.batchForming.MBSwithJobSize;
import jasima.shopSim.core.batchForming.MostCompleteBatch;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment.Scenario;
import jasima.shopSim.models.mimac.MimacExperiment;
import jasima.shopSim.models.mimac.MimacExperiment.DataSet;
import jasima.shopSim.prioRules.basic.FASFS;
import jasima.shopSim.prioRules.basic.TieBreakerFASFS;
import jasima.shopSim.prioRules.gp.GECCO2010_genSeed_2reps;
import jasima.shopSim.prioRules.gp.MyCustomGP;
import jasima.shopSim.prioRules.gp.NormalizedBrankeRule;
import jasima.shopSim.prioRules.gp.NormalizedBrankeRule_StringExecution;
import jasima.shopSim.prioRules.gp.TardinessGPRule;
import jasima.shopSim.prioRules.gp.testGPRule;
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;
import jasima.shopSim.prioRules.setup.ATCS;
import jasima.shopSim.prioRules.setup.SST;
import jasima.shopSim.prioRules.weighted.WMOD;
import jasima.shopSim.util.BasicJobStatCollector;
import jasima.shopSim.util.BatchStatCollector;
import jasima.shopSim.util.DeviationJobStatCollector;
import jasima.shopSim.util.ExtendedJobStatCollector;
import jasima.shopSim.util.MachineStatCollector;
import jasima.shopSim.util.Tardiness_ExtendedJobStatCollector;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import java.io.*;
import java.util.*;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import ec.simple.*;
import ec.app.JSP.Entities.*;

public class TestCDJSPP extends GPProblem
{	
	public DynamicShopExperiment dsExp = new DynamicShopExperiment();
	
	public static final String P_Simulation_Seed = "Simulation.Seed"; 
	public static final String P_Simulation_Replications = "Simulation.Replications";
	public static final String P_Simulation_Objective = "Simulation.Objective"; 
	public static final String P_Normalization = "Normalization";
	
	public int objectives = 1;
	public TypeOfObjective typeOfObjective = TypeOfObjective.mean;
	public long InitialSeed = 83461; 
	public int NumOfReplications = 50;
	public int NumOfJobs = 2500;
	public int NumOfMachines = 10;
	public int NumOfOperations = 10;
	public double UtilizationLevel = 0.95d;
	public int Normalization = 0;
	public int simulationLength=60*24*80;
	public Boolean UniqueSeed = true;
	public EJobShopObjectives ShopObjective = EJobShopObjectives.MeanFlowTime;
	
	ArrayList<DynamicShopExperiment> multipleDynamicExps = new ArrayList<DynamicShopExperiment>();
	
	private MimacExperiment createMimacFAB4r()
	{
		MimacExperiment e = new MimacExperiment();
		e.setScenario(DataSet.FAB4r);
		DblStream arrivals1 = new DblDistribution(new ExponentialDistribution(
				1440d / 4.5));
		DblStream arrivals2 = new DblDistribution(new ExponentialDistribution(
				1440d / 10.5));
		e.setInterArrivalTimes(new DblStream[] { arrivals1, arrivals2 });
		e.setDueDateFactors(new DblUniformRange(2.0, 5.0));
		e.setJobWeights(new IntUniformRange(1, 10));
		e.setSimulationLength(2 * 365 * 24 * 60);
		e.setMaxJobsInSystem(3 * 250);
		e.setEnableLookAhead(true);
		
		return e;
	}
	
	public double runExperiment(PR sr, long seed)
	{
//		MimacExperiment e = createMimacFAB4r();
		
		MimacExperiment e = new MimacExperiment();
		e.setScenario(DataSet.SH95);
		//e.setScenario(DataSet.FAB4r);
		e.setDueDateFactors(new DblUniformRange(4.0, 9.0));
		e.setJobWeights(new IntUniformRange(1, 10));
		e.setSimulationLength(60*24*365*2);
		e.setMaxJobsInSystem(1100);
		e.setEnableLookAhead(false);
		 
//		e.setSequencingRule(new ATCS(2.0,1.5));
//		e.setBatchForming(new GPPrioRuleBatching(sr));
		
		testGPRule tgp =new testGPRule(null);
		tgp.isTestStringGPRule=true;
		tgp.isSequencingRule=true;
		e.setSequencingRule(tgp);
		
//		e.setSequencingRule(sr);
		
		e.setBatchSequencingRule(sr);
		e.setBatchForming(new GPPGF(sr));
//		e.setBatchForming(new GPHighestJobBatchingMBS(sr));
//		e.setBatchForming(new MBSwithJobSize()); 
		
		ExtendedJobStatCollector stats = new ExtendedJobStatCollector();
		//stats.setInitialPeriod(365 * 24 * 60);
		e.addShopListener(stats);  
//		BatchStatCollector batchStats =new BatchStatCollector();
//		batchStats.setInitialPeriod(365 * 24 * 60);
//		e.addShopListener(batchStats);		
		MachineStatCollector machStats=new MachineStatCollector();
		//e.addMachineListener(machStats);
		

		MultipleReplicationExperiment mre = new MultipleReplicationExperiment();
		mre.setBaseExperiment(e);
		mre.setMaxReplications(this.NumOfReplications);
		mre.setInitialSeed(seed);
		mre.setAllowParallelExecution(true);
		
		mre.runExperiment();
		//mre.printResults();
		double expAborted=((SummaryStat)mre.getResults().get("baseExperiment.expAborted")).sum();
		SummaryStat ss =(SummaryStat)mre.getResults().get(Util.getObjectiveString(this.objectives)); 
		if(expAborted>0) 
			return 20000.0;
//			return ss.mean()<1.0?1.0*10000:ss.mean()*10000; 
		 
		return ss.mean(); 
	}
	
	private PR createPRStack(PR baseRule, boolean setupAvoidance) { 
		baseRule.setFinalTieBreaker(new TieBreakerFASFS());

		if (setupAvoidance) {
			PR ms = new SST();
			ms.setTieBreaker(baseRule);
			baseRule = ms;
		}

		return new IgnoreFutureJobs(baseRule);
	}

	public void setup(final EvolutionState state,
			final Parameter base)
	{
		// very important, remember this
		super.setup(state,base);
		
		//Get parameters 
		this.InitialSeed = state.parameters.getLongWithDefault(base.push(P_Simulation_Seed), null, System.currentTimeMillis());
		this.NumOfReplications = state.parameters.getIntWithDefault(base.push(P_Simulation_Replications), null, 1); 
		this.Normalization =  state.parameters.getIntWithDefault(base.push(P_Normalization), null, 0);
		UniqueSeed = state.parameters.getBoolean(new Parameter("eval.problem.Simulation.UniqueSeed"), null, true);
		objectives = state.parameters.getInt(new Parameter("eval.problem.Simulation.Objective"), null);
		this.typeOfObjective = TypeOfObjective.valueOf(state.parameters.getString(new Parameter("eval.problem.Simulation.ObjectiveType"), null))  ;
		
	}
	
	private long GetRandomValueByGeneration(final EvolutionState state, final int threadnum)
	{
		long baseSeed = this.InitialSeed;
		int gen = state.generation;
		long newSeed = baseSeed;
		
		Random rd = new Random(baseSeed - gen);
		newSeed = rd.nextLong();	
		
		return newSeed;
	}
	
	public void evaluate(final EvolutionState state, 
			final Individual ind, 
			final int subpopulation,
			final int threadnum)
	{
		if (!ind.evaluated)  // don't bother reevaluating
		{			
			((GPIndividual)ind).trees[0].printTwoArgumentNonterminalsAsOperatorsInC = false;
			
			
			PR pr = null;
			if(this.Normalization == 0)
			{
				pr = new testGPRule(((GPIndividual)ind).trees[0].child);				
			}
			else
			{
				pr = new NormalizedBrankeRule(((GPIndividual)ind).trees[0].child);
			}		
			
			long seed = this.InitialSeed;
			if(!UniqueSeed && state.generation > 0)
				seed = GetRandomValueByGeneration(state, threadnum);
				
			
			double rawfitness = this.runExperiment(pr, seed);
			 
			SummaryStat ss = new SummaryStat();
			ss.value(rawfitness);
	        ((KozaFitness)ind.fitness).setStandardizedFitness(state,rawfitness);
	        ((KozaFitness)ind.fitness).summaryStat = ss;
	        ind.evaluated = true;
			
		}
	}	

	public void describe(EvolutionState state, Individual ind, int subpopulation,
			int threadnum, int log)
	{
		
	}
}
