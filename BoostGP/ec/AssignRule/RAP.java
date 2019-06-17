package ec.AssignRule;

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
import jasima.shopSim.core.batchForming.GPPrioRuleBatching;
import jasima.shopSim.core.batchForming.MostCompleteBatch;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment.Scenario;
import jasima.shopSim.models.mimac.MimacExperiment;
import jasima.shopSim.models.mimac.MimacExperiment.DataSet;
import jasima.shopSim.prioRules.PRDecryptor;
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
import ec.CDJSP.DoubleData;
import ec.gp.*;
import ec.gp.koza.*;
import java.io.*;
import java.util.*;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import ec.simple.*;
import ec.app.JSP.Entities.*;

public class RAP extends GPProblem
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
	
	public static int numWC=72; 
	public static int numHT=9;  
	public static int n=numHT+numWC; 
	public int PRDecryptor = 0;
	
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
		//e.setMaxJobsInSystem(3 * 250);
		e.setEnableLookAhead(false);
		
		return e;
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
		
		PRDecryptor=state.parameters.getInt(new Parameter("pop.subpop.PRDecryptor"), null);
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
			
			 
			long seed = this.InitialSeed;
			if(!UniqueSeed && state.generation > 0)
				seed = GetRandomValueByGeneration(state, threadnum);
			
			int[] solution=getRuleIndex(state,((GPIndividual)ind).trees[0].child);
			
			String solutionStr ="";

			for (int i = 0;i < solution.length;i++)
			{ 
				solutionStr += String.valueOf(solution[i]);
				solutionStr += ",";
			}
			solutionStr = solutionStr.substring(0,solutionStr.length()-1);
			String[] args=new String[4];
			args[0]=String.valueOf(n-numHT);
			args[1]=String.valueOf(numHT);
			args[2]=solutionStr;
			args[3]=String.valueOf(1000);
			
			double rawfitness;
			rawfitness= jasima.shopSim.SimSH.main(args); 
//			System.out.println(rawfitness+"\t"+solutionStr);
			SummaryStat ss = new SummaryStat();
			ss.value(rawfitness);
	        ((KozaFitness)ind.fitness).setStandardizedFitness(state,rawfitness);
	        ((KozaFitness)ind.fitness).summaryStat = ss;
	        ind.evaluated = true;
			
		}
	}	
	
	public static int[] getRuleIndex(EvolutionState state,GPNode rootNode)
	{
		int[] solution =new int[81];
		PRDecryptor prd=new PRDecryptor();
		for(int i=0;i<state.machStatsMatrix.length;i++)
		{
			DoubleData input = new DoubleData();
			input.M_qLen = GetNormalizedValue(state.machStatsMatrix[i][0].mean(),0,1100,true);
			input.M_util = GetNormalizedValue(state.machStatsMatrix[i][1].mean(),0,1,true);
			input.M_capUtil = GetNormalizedValue(state.machStatsMatrix[i][2].mean(),0,1,true);
			input.M_bSize = GetNormalizedValue(state.machStatsMatrix[i][3].mean(),0,50,true);
			input.M_setup = GetNormalizedValue(state.machStatsMatrix[i][4].mean(),0,120,true);
			input.M_qWait = GetNormalizedValue(state.machStatsMatrix[i][5].mean(),0,216000,true);
			input.M_procTime = GetNormalizedValue(state.machStatsMatrix[i][6].mean(),0,396000,true);
			
			double maxPrio=Double.MIN_VALUE;
			int tempAssign=0;
			if(i<numHT)
			{
				for(int j=0;j<9;j++)
				{
					input.R_TWT=GetNormalizedValue(prd.decryptBatchFormPR(j).preTestFlowTime,33326.73,125129.00,true);
					input.R_WFT=GetNormalizedValue(prd.decryptBatchFormPR(j).preTestObj,81854.33,197635.69,true);
					input.R_TP=GetNormalizedValue(prd.decryptBatchFormPR(j).preTestObj,0.15,0.9,true);
					rootNode.evalSimple(input);
					
					if(input.x>maxPrio)
					{
						maxPrio=input.x;
						tempAssign=j;
					}
				}
			}
			else
			{
				for(int j=0;j<24;j++)
				{
					input.R_TWT=GetNormalizedValue(prd.decryptSeqPR(j).preTestObj,11612.46,16527.74,true);
					input.R_WFT=GetNormalizedValue(prd.decryptSeqPR(j).preTestFlowTime,50007.22,66083.49,true);
					input.R_TP=GetNormalizedValue(prd.decryptSeqPR(j).preTestObj,0.15,0.9,true);
					rootNode.evalSimple(input);
					
					if(input.x>maxPrio)
					{
						maxPrio=input.x;
						tempAssign=j;
					}
				}
			}
			
			solution[i]=tempAssign;
		}
		return solution;
	}
	
	protected static double GetNormalizedValue(double curValue, double min, double max, boolean isNormalize)
	{
		if(!isNormalize) return curValue;

		double result = 2*(curValue - min)/(max - min);
		
		return result;
	}

	public void describe(EvolutionState state, Individual ind, int subpopulation,
			int threadnum, int log)
	{
		
	}
}
