
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
import jasima.core.experiment.MultipleReplicationExperiment;
import jasima.core.random.RandomFactory;
import jasima.core.random.RandomFactoryOld;
import jasima.core.random.continuous.DblConst;
import jasima.core.random.continuous.DblDistribution;
import jasima.core.random.continuous.DblStream;
import jasima.core.random.continuous.DblUniformRange;
import jasima.core.random.discrete.IntUniformRange;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.Util;
import jasima.shopSim.core.IndividualMachine;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.batchForming.BatchForming;
import jasima.shopSim.core.batchForming.HighestJobBatchingMBS;
import jasima.shopSim.core.batchForming.MostCompleteBatch;
import jasima.shopSim.models.mimac.MimacExperiment;
import jasima.shopSim.models.mimac.MimacExperiment.DataSet;
import jasima.shopSim.prioRules.PRDecryptor;
import jasima.shopSim.prioRules.basic.ERD;
import jasima.shopSim.prioRules.basic.FCFS;
import jasima.shopSim.prioRules.basic.SPT;
import jasima.shopSim.prioRules.basic.TieBreakerFASFS;
import jasima.shopSim.prioRules.batch.BATCS;
import jasima.shopSim.prioRules.batch.BFASFS;
import jasima.shopSim.prioRules.batch.LBF;
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;
import jasima.shopSim.prioRules.setup.ATCS;
import jasima.shopSim.prioRules.setup.SST;
import jasima.shopSim.prioRules.setup.SetupAvoidance;
import jasima.shopSim.prioRules.weighted.WMOD;
import jasima.shopSim.util.BatchStatCollector;
import jasima.shopSim.util.ExtendedJobStatCollector;
import jasima.shopSim.util.MachineStatCollector;
import util.Wintersim2010GPRules.GPRuleSize199;

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
public class TestShuangHuanTrace {

	private static PR[] seqRules = null;
	private static PR[] batchSeqRules=  null;
	private static BatchForming[] batchFormRules=null;
	
	@BeforeClass
	public static void setUp() {
		System.setProperty(RandomFactory.RANDOM_FACTORY_PROP_KEY,
				RandomFactoryOld.class.getName());
	}

	private static final double PREC = 1e-6;
 
	public static MimacExperiment createBaseExperimentSH35() {
		MimacExperiment e = new MimacExperiment();
		e.setScenario(DataSet.SH95);

		//e.setInterArrivalTimes(DataSet.FAB4r.getShopDef().getArrivalRates());
		 
		e.setDueDateFactors(new DblUniformRange(1.0, 2.0));
		e.setJobWeights(new IntUniformRange(1, 10));
		e.setSimulationLength(60*24*60);
		//e.setMaxJobsInSystem(3 * 250);
		e.setEnableLookAhead(false);

		ExtendedJobStatCollector stats = new ExtendedJobStatCollector();
		//stats.setInitialPeriod(365 * 24 * 60);
		e.addShopListener(stats);

 
		//e.setSequencingRule(createPRStack(new GPRuleSize199(),true));
		//e.setSequencingRule(new WMOD());
		
		//e.setBatchSequencingRule(createPRStack(new GPRuleSize199(),true));
		//e.setBatchSequencingRule(new BATCS(5.0,0.005));
		
		//e.setBatchForming(new HighestJobBatchingMBS(0.75));
		//e.setBatchForming(new MostCompleteBatch());
		
		
		 
		//e.setEnableLookAhead(false); 
		return e;
	}

	public static MimacExperiment createExperimentFAB4r() {
		MimacExperiment e = new MimacExperiment();
		e.setInitialSeed(-6437543093816807328l);
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
	
	@Test
	public   void testPrioQueueAssertion() throws Exception {
		//MimacExperiment sh35 = createBaseExperimentSH35(); 
		MimacExperiment sh35 = createExperimentFAB4r();
		if(seqRules!=null && batchSeqRules!=null && batchFormRules!=null)
		{
			sh35.setSequencingRules(seqRules);
			sh35.setBatchSequencingRules(batchSeqRules);
			sh35.setBatchFormingRules(batchFormRules);
		}
		
//		ExtendedJobStatCollector stats = new ExtendedJobStatCollector();
//		stats.setInitialPeriod(365 * 24 * 60);
//		sh35.addShopListener(stats);
		
		//BatchStatCollector batchStats =new BatchStatCollector();
		//batchStats.setInitialPeriod(365 * 24 * 60);
		//sh35.addShopListener(batchStats);
		
		MachineStatCollector machStats=new MachineStatCollector();
		sh35.addMachineListener(machStats);
		
//		sh35.runExperiment();
//		sh35.printResults();
//		Map<String, Object> res = sh35.getResults();
		
		MultipleReplicationExperiment mre = new MultipleReplicationExperiment();
		mre.setBaseExperiment(sh35);
		mre.setMaxReplications(20);
		mre.setInitialSeed(8346);
		mre.setAllowParallelExecution(true);
		
		mre.runExperiment();
		mre.printResults();
		
		//mre.getBaseExperiment().getNotifierListener(0).
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
	
	private static PR createPR_FIFO(final PR ties) {
		// first exception due to different handling of simultaneous events
		@SuppressWarnings("serial")
		PR pr = new PR() {

			@Override
			public double calcPrio(PrioRuleTarget j) {
				if (j.getShop().simTime() < 9597.2
						|| j.getShop().simTime() > 9598.2)
					return 0;

				return j.toString().contains("#18") ? -1 : 0;
			}

		};
		pr.setTieBreaker(ties);

		// first exception due to different handling of unequal events
		@SuppressWarnings("serial")
		PR pr2 = new PR() {

			@Override
			public double calcPrio(PrioRuleTarget j) {
				if (j.getShop().simTime() > 12368.9
						&& j.getShop().simTime() < 12369.1) {
					if (j.toString().contains("1.115#12"))
						return +1;
					else
						return 0;
				} else if (j.getShop().simTime() > 13823.9
						&& j.getShop().simTime() < 13824.1) {
					if (j.toString().contains(".138#5"))
						return +1;
					else
						return 0;
				} else if (j.getShop().simTime() > 19568.0 - 0.1
						&& j.getShop().simTime() < 19568.0 + 0.1) {
					if (j.toString().contains(".174#15"))
						return +1;
					else
						return 0;
				} else if (j.getShop().simTime() > 19658.0 - 0.1
						&& j.getShop().simTime() < 19658.0 + 0.1) {
					if (j.toString().contains(".117#38"))
						return +1;
					else
						return 0;
				} else if (j.getShop().simTime() > 19828.0 - 0.1
						&& j.getShop().simTime() < 19828.0 + 0.1) {
					if (j.toString().contains(".127#38"))
						return +1;
					else
						return 0;
				} else if (j.getShop().simTime() > 20593.0 - 0.1
						&& j.getShop().simTime() < 20593.0 + 0.1) {
					if (j.toString().contains(".117#44"))
						return +1;
					else
						return 0;
				} else if (j.toString().contains(".120#44")) {
					return +1;
				} else if (j.toString().contains(".124#44")) {
					return +1;
				} else if (j.toString().contains(".127#44")) {
					return +1;
				} else if (j.toString().contains(".130#44")) {
					return +1;
				} else if (j.getShop().simTime() > 24483.0 - 0.1
						&& j.getShop().simTime() < 24483.0 + 0.1) {
					if (j.toString().contains(".207#22"))
						return +1;
					else
						return 0;
				} else if (j.getShop().simTime() > 26082.0 - 0.1
						&& j.getShop().simTime() < 26082.0 + 0.1) {
					if (j.toString().contains(".140#65"))
						return +1;
					else
						return 0;
				} else if (j.toString().contains(".1.286#11")) {
					return +1;
				} else if (j.toString().contains(".1.288#11")) {
					return +1;
				} else if (j.toString().contains(".1.289#11")) {
					return +1;
				} else if (j.toString().contains(".1.291#11")) {
					return +1;
				} else if (j.toString().contains(".1.292#11")) {
					return +1;
				} else if ((j.getShop().simTime() > 30113.0 - 0.1 && j
						.getShop().simTime() < 30113.0 + 0.1)
						&& j.toString().contains(".0.160#74")) {
					return -1;
				} else if ((j.getShop().simTime() > 32758.0 - 0.1 && j
						.getShop().simTime() < 32758.0 + 0.1)
						&& j.toString().contains(".0.284#24")) {
					return +1;
				} else if ((j.getShop().simTime() > 36843.0 - 0.1 && j
						.getShop().simTime() < 36843.0 + 0.1)
						&& j.toString().contains(".1.383#5")) {
					return +1;
				} else if ((j.getShop().simTime() > 38413.0 - 0.1 && j
						.getShop().simTime() < 38413.0 + 0.1)
						&& j.toString().contains(".1.369#18")) {
					return -1;
				} else
					return 0;
			}

		};
		pr2.setTieBreaker(pr);

		return pr2;
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
	public   void main(String[] args)
	{
		int numWC=Integer.parseInt(args[0]);
		int numHT=Integer.parseInt(args[1]);
		double[] prAssignArray = Util.parseDblList(args[2]);
		
		String fab4rParam="31 13 1,1,0,2,0,1,2,1,2,0,2,1,2,5,10,5,4,9,5,9,2,2,5,9,8,9,0,10,3,6,2,8,1,7,0,6,2,9,3,10,9,3,5,2";
		String SHParam="9 3 0,1,2,5,5,7,10,0,9,2,6,6";
		
		seqRules = new PR[numWC];
		batchSeqRules=  new PR[numWC];
		batchFormRules=new BatchForming[numWC];
		
		int w =0; //WC ID
		int ht =0; //WC ID
		for(int i=0;i<prAssignArray.length;i++)
		{
			
			if(i<numHT)
			{ 
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
		
		try 
		{
			testPrioQueueAssertion();
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
 
}
