package ec.app.JSP;

import ec.util.*;
import jasima.core.experiment.MultipleReplicationExperiment;
import jasima.core.random.continuous.DblConst;
import jasima.core.random.discrete.IntEmpirical;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.Simulation.SimEvent;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.EJobShopObjectives;
import jasima.core.util.Util;
import jasima.core.util.Util.TypeOfObjective;
import jasima.core.util.observer.NotifierListener;
import jasima.shopSim.core.PR;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment.Scenario;
import jasima.shopSim.prioRules.basic.FASFS;
import jasima.shopSim.prioRules.gp.GECCO2010_genSeed_2reps;
import jasima.shopSim.prioRules.gp.MyCustomGP;
import jasima.shopSim.prioRules.gp.NormalizedBrankeRule;
import jasima.shopSim.prioRules.gp.NormalizedBrankeRule_StringExecution;
import jasima.shopSim.prioRules.gp.TardinessGPRule;
import jasima.shopSim.prioRules.gp.testGPRule;
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;
import jasima.shopSim.util.BasicJobStatCollector;
import jasima.shopSim.util.DeviationJobStatCollector;
import jasima.shopSim.util.ExtendedJobStatCollector;
import jasima.shopSim.util.Tardiness_ExtendedJobStatCollector;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import java.io.*;
import java.util.*;
import ec.simple.*;
import ec.app.JSP.Entities.*;

public class TestJSPP extends GPProblem
{	
	public DynamicShopExperiment dsExp = new DynamicShopExperiment();
	
	public static final String P_Simulation_Seed = "Simulation.Seed";
	public static final String P_Simulation_NumofJobs = "Simulation.NumOfJobs";
	public static final String P_Simulation_NumofMachines = "Simulation.NumofMachines";
	public static final String P_Simulation_Replications = "Simulation.Replications";
	public static final String P_Simulation_Objective = "Simulation.Objective";
	public static final String P_Simulation_UtilizationLevel = "Simulation.UtilizationLevel";
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
	public Boolean UniqueSeed = true;
	public EJobShopObjectives ShopObjective = EJobShopObjectives.MeanFlowTime;
	
	ArrayList<DynamicShopExperiment> multipleDynamicExps = new ArrayList<DynamicShopExperiment>();
	
	public Map<String, Object> runExperiment(PR sr, long seed)
	{
		DynamicShopExperiment e = Util.getBaseExperiment(NumOfJobs, this.NumOfOperations, this.NumOfOperations, UtilizationLevel, objectives, this.InitialSeed);
		e.typeOfObjective = this.typeOfObjective;
		
		PR sr2 = new IgnoreFutureJobs(sr);
		PR sr3 = new FASFS();
		sr2.setTieBreaker(sr3);
		e.setSequencingRule(sr2);
		
		MultipleReplicationExperiment mre = new MultipleReplicationExperiment();
		mre.setBaseExperiment(e);
		mre.setMaxReplications(this.NumOfReplications);
		mre.setInitialSeed(seed);
		mre.typeOfObjective = this.typeOfObjective;
	
		mre.runExperiment();

		Map<String, Object> res = mre.getResults();
		
		return res;
	}

	public void setup(final EvolutionState state,
			final Parameter base)
	{
		// very important, remember this
		super.setup(state,base);
		
		//Get parameters
		this.NumOfJobs = state.parameters.getInt(base.push(P_Simulation_NumofJobs), null);
		this.NumOfMachines = state.parameters.getInt(base.push(P_Simulation_NumofMachines), null);
		this.InitialSeed = state.parameters.getLongWithDefault(base.push(P_Simulation_Seed), null, System.currentTimeMillis());
		this.NumOfReplications = state.parameters.getIntWithDefault(base.push(P_Simulation_Replications), null, 1);
		this.UtilizationLevel = state.parameters.getDouble(base.push(P_Simulation_UtilizationLevel), null);
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
				
			
			Map<String, Object> res = this.runExperiment(pr, seed);
			
			String obj = Util.getObjectiveString(this.objectives);
			
			SummaryStat ss = (SummaryStat)res.get(obj);
			
			KozaFitness f = (KozaFitness)(ind.fitness);
			f.setStandardizedFitness(state,  ss.mean());
			f.setVariance(state, ss.stdDev());
			f.NumOfEvaluations = NumOfReplications;
			f.summaryStat = ss;
		
			ind.evaluated = true;
			
		}
	}	

	public void describe(EvolutionState state, Individual ind, int subpopulation,
			int threadnum, int log)
	{
		
	}
}
