package ec.CDJSP;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import ec.gp.GPIndividual;
import ec.gp.GPNode;
import jasima.core.experiment.MultipleReplicationExperiment;
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
import jasima.shopSim.prioRules.basic.FCFS;
import jasima.shopSim.prioRules.gp.NormalizedBrankeRule;
import jasima.shopSim.prioRules.gp.NormalizedBrankeRule_StringExecution;
import jasima.shopSim.prioRules.gp.testGPRule;
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;
import jasima.shopSim.util.BasicJobStatCollector;

public class DuplicateRemovalWrapper {

	public ArrayList<DummyOperation> DummyOPs;
	public int numOfDummyOPs = 100;
	public long initialSeed = 5908;
	
	public HashSet<String> rankSet;
	
	Random rd = null;
	
	public void init()
	{
		rd = new Random(initialSeed);
		DummyOPs = new ArrayList();
		rankSet = new HashSet();
		
		for(int i = 0; i < this.numOfDummyOPs; i++)
		{
			DummyOperation dop = new DummyOperation(rd.nextLong());
			
			DummyOPs.add(dop);
		}		
	}
	
	public String calculateRank(GPNode rootNode)
	{
		List<Double> originalValueList = new ArrayList();
		List<Double> sortedValueList = new ArrayList();
		
		
		
		
		
		for(int i = 0; i < this.numOfDummyOPs; i++)
		{
			double priorityValue = DummyOPs.get(i).calPriority(rootNode) + 0.0;
			
			originalValueList.add(priorityValue);
			sortedValueList.add(priorityValue);
		}
		
		Collections.sort(sortedValueList);
		 
		StringBuffer strRank = new StringBuffer();
		for(int i = 0; i < this.numOfDummyOPs; i++)
		{
			int rank = sortedValueList.indexOf(originalValueList.get(i));	
			
			strRank.append(String.format("%04d", rank));
		}
		
//		if(rootNode.makeCTree(true, true, false).equals("NPT"))
//		{
//			System.out.println(rootNode.makeCTree(true, true, false));
//			System.out.println(strRank);
//		}
		
		
		return strRank.toString();
	}
	
	public int addRank(String strRank)
	{
		int isExisted = 0;
		
		if(this.rankSet.contains(strRank))
		{
			isExisted = 1;
		}
		else
		{
			this.rankSet.add(strRank);
		}
		
		return isExisted;
	}

}
