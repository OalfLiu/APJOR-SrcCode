package jasima.shopSim;
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
import static org.junit.Assert.assertEquals;
import jasima.core.experiment.FullFactorialExperiment;
import jasima.core.experiment.MultipleConfigurationExperiment;
import jasima.core.experiment.MultipleReplicationExperiment;
import jasima.core.random.RandomFactory;
import jasima.core.random.RandomFactoryOld;
import jasima.core.random.continuous.DblConst;
import jasima.core.random.continuous.DblDistribution;
import jasima.core.random.continuous.DblStream;
import jasima.core.random.continuous.DblUniformRange;
import jasima.core.random.discrete.IntUniformRange;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.ExcelSaver;
import jasima.core.util.Util;
import jasima.shopSim.core.IndividualMachine;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.batchForming.BatchForming;
import jasima.shopSim.core.batchForming.BestOfFamilyBatching;
import jasima.shopSim.core.batchForming.HighestJobBatchingMBS;
import jasima.shopSim.core.batchForming.MostCompleteBatch;
import jasima.shopSim.models.mimac.MimacExperiment;
import jasima.shopSim.models.mimac.MimacExperiment.DataSet;
import jasima.shopSim.prioRules.PRDecryptor;
import jasima.shopSim.prioRules.basic.CR;
import jasima.shopSim.prioRules.basic.EDD;
import jasima.shopSim.prioRules.basic.ERD;
import jasima.shopSim.prioRules.basic.FCFS;
import jasima.shopSim.prioRules.basic.ODD;
import jasima.shopSim.prioRules.basic.SPT;
import jasima.shopSim.prioRules.basic.TieBreakerFASFS;
import jasima.shopSim.prioRules.batch.BATCS;
import jasima.shopSim.prioRules.batch.BFASFS;
import jasima.shopSim.prioRules.batch.LBF;
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;
import jasima.shopSim.prioRules.setup.ATCS;
import jasima.shopSim.prioRules.setup.SST;
import jasima.shopSim.prioRules.setup.SetupAvoidance;
import jasima.shopSim.prioRules.weighted.LW;
import jasima.shopSim.prioRules.weighted.WMDD;
import jasima.shopSim.prioRules.weighted.WMOD;
import jasima.shopSim.prioRules.weighted.WSPT;
import jasima.shopSim.util.ExtendedJobStatCollector;
import util.Wintersim2010GPRules.GPRuleSize199;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.junit.BeforeClass;
import org.junit.Test;
 

/**
 * 
 * @author Torsten Hildebrandt <hil@biba.uni-bremen.de>
 * @version $Id: TestMimacFab4Trace.java 550 2015-01-23 15:07:23Z
 *          thildebrandt@gmail.com $
 */
@SuppressWarnings("deprecation")
public class ParallelSimSH {

	private static PR[] seqRules = null;
	private static PR[] batchSeqRules=  null;
	private static BatchForming[] batchFormRules=null;
	private static long seed=8346;
	
	@BeforeClass
	public static void setUp() {
		System.setProperty(RandomFactory.RANDOM_FACTORY_PROP_KEY,
				RandomFactoryOld.class.getName());
	}

	private static final double PREC = 1e-6;
 
	public static MimacExperiment createBaseExperimentSH35() {
		MimacExperiment e = new MimacExperiment();
		e.setScenario(DataSet.SH75);
		//e.setScenario(DataSet.FAB4r);
		e.setDueDateFactors(new DblUniformRange(4.0, 9.0));
		e.setJobWeights(new IntUniformRange(1, 10));
		e.setSimulationLength(60*24*365*2);
		e.setMaxJobsInSystem(1100);
		e.setEnableLookAhead(false);
		//e.setEnableLookAhead(false); 
		return e;
	}
	 
	@Test
	public static double testPrioQueueAssertion() throws Exception {
		MimacExperiment sh35 = createBaseExperimentSH35(); 
		
		ExtendedJobStatCollector stats = new ExtendedJobStatCollector();
		//stats.setInitialPeriod(60 * 24 * 60);
		sh35.addShopListener(stats);
		
		if(seqRules!=null && batchSeqRules!=null && batchFormRules!=null)
		{
			sh35.setSequencingRules(seqRules);
			sh35.setBatchSequencingRules(batchSeqRules);
			sh35.setBatchFormingRules(batchFormRules);
		}
			 
		MultipleReplicationExperiment mre = new MultipleReplicationExperiment();
		mre.setBaseExperiment(sh35);
		mre.setMaxReplications(1);
		mre.setInitialSeed(seed);
		mre.setSkipSeedCount(13); 
		mre.setAllowParallelExecution(true);
		
		mre.runExperiment();
//		mre.printResults();
		
//		System.out.print(((SummaryStat)mre.getResults().get("baseExperiment.runTime")).sum()+"  ");
		
		double expAborted=((SummaryStat)mre.getResults().get("baseExperiment.expAborted")).sum();
		double obj =((SummaryStat)mre.getResults().get("weightedTardiness.mean")).mean();
		
		if(expAborted>0) return 20000; 
		
//		double obj =((SummaryStat)mre.getResults().get("tardPercentage")).mean();
		return obj;
		
//		FullFactorialExperiment ffe = new FullFactorialExperiment();
//
//		ffe.setBaseExperiment(sh35);
//		
//		ffe.setInitialSeed(23); // sollte egal sein, weil deterministisch
//		 
//		//PR pr = createPR_FIFO(createPRStack(new FCFS(), true));
//		//ffe.addFactor("sequencingRule", pr);
//		ffe.setMaxConfigurations(2);
//		ffe.runExperiment();
//		ffe.printResults();
//		Map<String, Object> res = ffe.getResults(); 
		
//		check("flowMean", 12499.1164, 0.0001, res);
//		check("tardMean", 2192.6347, 0.0001, res);
//		// check("tardPercentage", 0.6811, 0.0001, res);
//		check("tardPercentage", 0.8148, 0.0001, res);
//		check("weightedTardMean", 7031.1629, 0.0001, res);
//		check("numJobsStarted", 32926, 0.0001, res);
//		check("numJobsFinished", 32799, 0.0001, res);
//		check("expAborted", 0.0, 0.0001, res);
	}
	
	private static void check(String name, double expected, double precision,
			Map<String, Object> res) {
		SummaryStat vs = (SummaryStat) res.get(name);
		assertEquals(name, expected, vs.mean(), precision);
	}
	 
	private static PR createPRStack(PR pr, boolean setupAvoidance) {
		pr.setFinalTieBreaker(new TieBreakerFASFS());

		if (setupAvoidance) {
			PR ms = new SST();
			ms.setTieBreaker(pr);
			pr = ms;
		}

		return new IgnoreFutureJobs(pr);
	}
	
	//第一位是总workcenter数，第二位是热处理设备数，第三位是PR编码序列，热处理设备在最前面，且每个热处理排序规则后面跟一个组批规则，后面顺序跟着所有正常设备的排序规则
	public static Map<String,Object> configBuilder(String[] args)
	{
		Map<String,Object> configuration=new HashMap<String,Object>();
		
		int numWC=Integer.parseInt(args[0]);
		int numHT=Integer.parseInt(args[1]);
		double[] prAssignArray = Util.parseDblList(args[2]);
		seed=Long.parseLong(args[3]);
		
		seqRules = new PR[numWC];
		batchSeqRules=  new PR[numWC];
		batchFormRules=new BatchForming[numWC];
		
		int w =0; //WC ID
		int ht =0; //WC ID
		for(int i=0;i<prAssignArray.length;i++)
		{ 
			if(i<numHT)
			{
				//batchFormRules[w]=decryptBatchFormPR(prAssignArray[i]);
				batchFormRules[ht]=new PRDecryptor().decryptBatchFormPR(prAssignArray[i]);
				ht++;
			} 
			else
			{
				batchSeqRules[w]=new PRDecryptor().decryptSeqPR(prAssignArray[i]);
				seqRules[w]=new PRDecryptor().decryptSeqPR(prAssignArray[i]);
				w++;
			}  
		}
		
		configuration.put("baseExperiment.sequencingRules", seqRules);
		configuration.put("baseExperiment.BatchSequencingRules", batchSeqRules);
		configuration.put("baseExperiment.batchFormingRules", batchFormRules);
		configuration.put("baseExperiment.name", args[2]);
		return configuration;
	}
	
	public static Map<String,Object> runParallelExps(String[][] arglist)
	{
		MimacExperiment sh35 = createBaseExperimentSH35(); 		
		ExtendedJobStatCollector stats = new ExtendedJobStatCollector(); 
		sh35.addShopListener(stats);
		
		MultipleReplicationExperiment mre = new MultipleReplicationExperiment();
		mre.setBaseExperiment(sh35);
		mre.setMaxReplications(1);
		mre.setInitialSeed(seed);
		mre.setSkipSeedCount(13); 
		mre.setAllowParallelExecution(true);
		
		MultipleConfigurationExperiment mce= new MultipleConfigurationExperiment();
		mce.setBaseExperiment(mre);  
		mce.setAllowParallelExecution(true);
		for(int i=0;i<arglist.length;i++)
		{
			mce.addConfiguration(configBuilder(arglist[i]));
			mce.addKeepResultName(arglist[i][2]);
		}

		ExcelSaver saver = new ExcelSaver();
		mce.addNotifierListener(saver);
		
		mce.runExperiment(); 
		mce.printResults();
		
		
		Map<String, Object> res = mce.getResults();  
		
		return null;
	}
}
