package ec.CDJSP;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.util.Parameter;
import jasima.core.experiment.MultipleReplicationExperiment;
import jasima.core.random.continuous.DblConst;
import jasima.core.random.continuous.DblUniformRange;
import jasima.core.random.discrete.IntUniformRange;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.Simulation.SimEvent;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.Util;
import jasima.core.util.Util.TypeOfObjective;
import jasima.core.util.observer.NotifierListener;
import jasima.shopSim.core.PR;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment.Scenario;
import jasima.shopSim.models.mimac.MimacExperiment;
import jasima.shopSim.models.mimac.MimacExperiment.DataSet;
import jasima.shopSim.prioRules.basic.FASFS;
import jasima.shopSim.prioRules.basic.FCFS;
import jasima.shopSim.prioRules.gp.NormalizedBrankeRule;
import jasima.shopSim.prioRules.gp.NormalizedBrankeRule_StringExecution;
import jasima.shopSim.prioRules.gp.testGPRule;
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;
import jasima.shopSim.util.BasicJobStatCollector;
import jasima.shopSim.util.ExtendedJobStatCollector;

public class DummyOperationWrapper {

	public ArrayList<DummyOperation> DummyOPs;
	public int NumOfDummyOPs = 100;
	public long InitialSeed;
	private Random rd;
	public HashMap NodeMapFitness = null;
	public Set setFitness = null;
	public DynamicShopExperiment dsExp;
	public int objectives;
	public int Normalization;
	public double utilizationLevel = 0.95;

	MultipleReplicationExperiment mre;
	
	public TypeOfObjective typeOfObjective = TypeOfObjective.mean;

	// private BigDecimal fifoFitness;

	public DummyOperationWrapper() {
		InitialSeed = 8888;
		rd = new Random(InitialSeed);
		DummyOPs = new ArrayList<DummyOperation>();
		NodeMapFitness = new HashMap();
		setFitness = new HashSet();
	}

	public void Init() {

		int numberOfOperations = 5;
		
		dsExp = Util.getBaseExperimentSimple(500, numberOfOperations, numberOfOperations, utilizationLevel, objectives, this.InitialSeed);
		dsExp.typeOfObjective = this.typeOfObjective;
	}

	public BigDecimal GetFitnessOfGPNode(GPNode node) {
		
		BigDecimal fitness = new BigDecimal(0.0);
		 
		PR pr = null;

		if (this.Normalization == 0) {
			pr = new testGPRule(node);
		} else {
			pr = new NormalizedBrankeRule(node);
		}

		PR sr2 = new IgnoreFutureJobs(pr);
		PR sr3 = new FASFS();
		sr2.setTieBreaker(sr3);
		
		MimacExperiment e = new MimacExperiment();
		e.setScenario(DataSet.SH35); 
		e.setDueDateFactors(new DblUniformRange(1.0, 2.0));
		e.setJobWeights(new IntUniformRange(1, 10));
		e.setSimulationLength(60*24*10); 
		e.setEnableLookAhead(false);		
		e.setSequencingRule(sr2); 
		
		ExtendedJobStatCollector stats = new ExtendedJobStatCollector();
		//stats.setInitialPeriod(365 * 24 * 60);
		e.addShopListener(stats);  

		mre = new MultipleReplicationExperiment();
		mre.setBaseExperiment(e);
		mre.setMaxReplications(2);
		mre.setInitialSeed(InitialSeed);
		mre.runExperiment();
		//mre.printResults();

		String obj = Util.getObjectiveString_Single(this.objectives);
		Map<String, Object> res = mre.getResults();
		
		
		if (res.containsKey(obj)) {
			Object target = res.get(obj);

			if (target instanceof SummaryStat) {
				fitness = BigDecimal.valueOf(((SummaryStat) target).mean());
			} else {
				fitness = BigDecimal.valueOf(((double) target));
			}
		}

		return fitness;
	}

	public SummaryStat getSimulationSummaryOfGPNode(GPNode node) {
		PR pr = null;

		if (this.Normalization == 0) {
			pr = new testGPRule(node);
		} else {
			pr = new NormalizedBrankeRule(node);
		}

		PR sr2 = new IgnoreFutureJobs(pr);
		PR sr3 = new FASFS();
		sr2.setTieBreaker(sr3);
		dsExp.setSequencingRule(sr2);

		mre = new MultipleReplicationExperiment();
		mre.setBaseExperiment(dsExp);
		mre.setMaxReplications(2);
		mre.setInitialSeed(InitialSeed);
		mre.runExperiment();

		String obj = Util.getObjectiveString(this.objectives);
		Map<String, Object> res = mre.getResults();
		BigDecimal fitness = null;

		Object target = res.get(obj);

		if (target instanceof SummaryStat) {
			return (SummaryStat) target;
		} 
		else if (target instanceof Integer)
		{
			fitness = BigDecimal.valueOf(((int) target));

			SummaryStat targetValue = new SummaryStat();
			targetValue.value(fitness.doubleValue());
			return targetValue;
		}
		else {
			fitness = BigDecimal.valueOf(((double) target));

			SummaryStat targetValue = new SummaryStat();
			targetValue.value(fitness.doubleValue());
			return targetValue;
		}

	}

	public double getFitnessInDouble(GPNode node) {
		return GetFitnessOfGPNode(node).doubleValue();
	}

	public void AddFitness(BigDecimal fitness) {
		if (!setFitness.contains(fitness)) {
			setFitness.add(fitness);
		}

	}

	public void AddFitness(double fitness) {
		BigDecimal fitnessBig = BigDecimal.valueOf(fitness);

		if (!setFitness.contains(fitnessBig)) {
			setFitness.add(fitnessBig);
		}

	}

	public Boolean isFitnessExisted(BigDecimal fitness) {
		Boolean isSame = true;

		if (!setFitness.contains(fitness)) {
			isSame = false;
		}

		return isSame;
	}

	public Boolean isIndividualSame(GPNode node) {
		Boolean isSame = true;
		BigDecimal newfitness = GetFitnessOfGPNode(node);

		// if(newfitness.compareTo(fifoFitness) != -1)
		// {
		// return isSame;
		// }

		if (!setFitness.contains(newfitness)) {
			setFitness.add(newfitness);
			isSame = false;
		}

		return isSame;
	}
}
