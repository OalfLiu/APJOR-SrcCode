
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
import jasima.core.experiment.MultipleReplicationExperiment;
import jasima.core.experiment.TestConfiguration;
import jasima.core.random.continuous.DblConst;
import jasima.core.random.discrete.IntEmpirical;
import jasima.core.random.discrete.IntUniformRange;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.Simulation.SimEvent;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.ExcelSaver;
import jasima.core.util.Util;
import jasima.core.util.Util.TypeOfObjective;
import jasima.core.util.observer.NotifierListener;
import jasima.shopSim.core.PR;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment.Scenario;
import jasima.shopSim.prioRules.basic.ATC;
import jasima.shopSim.prioRules.basic.CR;
import jasima.shopSim.prioRules.basic.EDD;
import jasima.shopSim.prioRules.basic.ERD;
import jasima.shopSim.prioRules.basic.FASFS;
import jasima.shopSim.prioRules.basic.FCFS;
import jasima.shopSim.prioRules.basic.LRM;
import jasima.shopSim.prioRules.basic.MDD;
import jasima.shopSim.prioRules.basic.MOD;
import jasima.shopSim.prioRules.basic.SPT;
import jasima.shopSim.prioRules.basic.SRPT;
import jasima.shopSim.prioRules.basic.SRPTPerPT;
import jasima.shopSim.prioRules.gp.GECCO2010_genSeed_10reps;
import jasima.shopSim.prioRules.gp.GECCO2010_genSeed_2reps;
import jasima.shopSim.prioRules.gp.MyCustomGP;
import jasima.shopSim.prioRules.gp.NormalizedBrankeRule;
import jasima.shopSim.prioRules.gp.NormalizedBrankeRule_StringExecution;
import jasima.shopSim.prioRules.gp.TempRule;
import jasima.shopSim.prioRules.gp.GPRuleBase.DivProtectedFunction;
import jasima.shopSim.prioRules.gp.GPRuleBase.If3Function;
import jasima.shopSim.prioRules.gp.GPRuleBase.MaxFunction;
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQ;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQPlusNPT;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQPlusSlack;
import jasima.shopSim.prioRules.upDownStream.WINQ;
import jasima.shopSim.prioRules.weighted.LW;
import jasima.shopSim.prioRules.weighted.WMDD;
import jasima.shopSim.prioRules.weighted.WMOD;
import jasima.shopSim.prioRules.weighted.WSPT;
import jasima.shopSim.util.BasicJobStatCollector;
import jasima.shopSim.util.DeviationJobStatCollector;
import jasima.shopSim.util.TardinessDeviationJobStatCollector;
import jasima.shopSim.util.Tardiness_ExtendedJobStatCollector;
import jasima.shopSim.util.ExtendedJobStatCollector;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;

import ec.gp.GPIndividual;

/**
 * 
 * @author Cheng
 * @version "$Id$"
 */
@SuppressWarnings("deprecation")
public class CustomGPTest {

	public long InitialSeed = 8238723;

	public Random seedStream = new Random(this.InitialSeed);
	public int NumOfReplications = 1;
	public String curGPRule;

	public enum EObjectives {
		CMAX, FlowTime,
	}

	public EObjectives obj = EObjectives.CMAX;

	ArrayList<DynamicShopExperiment> multipleDynamicExps = new ArrayList<DynamicShopExperiment>();

	public double ExecuteExpriment(DynamicShopExperiment de, String rule) {

		PR sr = new MyCustomGP(rule);
		PR sr2 = new IgnoreFutureJobs(sr);
		PR sr3 = new FASFS();
		sr2.setTieBreaker(sr3);
		de.setSequencingRule(sr2);
		de.runExperiment();

		// de.printResults();

		Map<String, Object> res = de.getResults();

		double fitness = Double.MAX_VALUE;

		if (obj == EObjectives.CMAX) {
			fitness = (double) res.get("cMax");
		} else if (obj == EObjectives.FlowTime) {
			SummaryStat flowtime = (SummaryStat) res.get("flowtime");

			fitness = flowtime.mean();
		}

		return fitness;
	}

	public void Initialize_MultipleReplications() {
		long seed;
		for (int i = 0; i < NumOfReplications; i++) {
			if (seedStream == null)
				seedStream = new Random(this.InitialSeed);

			seed = seedStream.nextLong();

			DynamicShopExperiment e = new DynamicShopExperiment();

			e.setInitialSeed(seed);
			// remove default BasicJobStatCollector
			NotifierListener<Simulation, SimEvent>[] l = e.getShopListener();
			assert l.length == 1 && l[0] instanceof BasicJobStatCollector;
			e.setShopListener(null);

			BasicJobStatCollector basicJobStatCollector = new BasicJobStatCollector();
			// basicJobStatCollector.setInitialPeriod(500);
			basicJobStatCollector.setIgnoreFirst(500);

			e.addShopListener(basicJobStatCollector);

			e.setNumMachines(10);
			e.setNumOps(10, 10);
			e.setDueDateFactor(new DblConst(4.0));
			e.setUtilLevel(0.95d);
			e.setStopAfterNumJobs(2500);
			e.setScenario(Scenario.JOB_SHOP);

			multipleDynamicExps.add(e);

			System.out.println("Seed: " + seed);

		}
	}

	public void testMultipleDJSSP() {

		double sum_CMax = 0;
		double[] cmaxArray = new double[NumOfReplications];
		int index = 0;

		for (DynamicShopExperiment dje : multipleDynamicExps) {
			cmaxArray[index] = ExecuteExpriment(dje, curGPRule);
			index++;
		}

		for (double value : cmaxArray) {
			sum_CMax += value;
		}

		double mean_cMax = sum_CMax / NumOfReplications;

		double variance = 0;

		for (int i = 0; i < this.NumOfReplications; i++) {
			variance += (cmaxArray[i] - mean_cMax) * (cmaxArray[i] - mean_cMax);
		}

		variance = variance / NumOfReplications - 1;
		double dev = Math.sqrt(variance);

		System.out.println("avg=" + mean_cMax + " dev=" + dev);

	}

	public static void runSingleExperiment(PR rule, long seed) {
		DynamicShopExperiment e = new DynamicShopExperiment();

		// remove default BasicJobStatCollector
		NotifierListener<Simulation, SimEvent>[] l = e.getShopListener();
		assert l.length == 1 && l[0] instanceof BasicJobStatCollector;
		e.setShopListener(null);

		TardinessDeviationJobStatCollector basicJobStatCollector = new TardinessDeviationJobStatCollector();
		basicJobStatCollector.setInitialPeriod(500);
		basicJobStatCollector.setIgnoreFirst(500);
		basicJobStatCollector.initialSeed = seed;

		e.addShopListener(basicJobStatCollector);
		e.setMaxJobsInSystem(500);
		e.setNumMachines(10);
		e.setNumOps(10, 10);
		e.setDueDateFactor(new DblConst(4.0));
		e.setUtilLevel(0.95d);
		e.setStopAfterNumJobs(2500);
		e.setScenario(Scenario.JOB_SHOP);
		PR sr = rule;
		PR sr2 = new IgnoreFutureJobs(sr);
		PR sr3 = new FASFS();
		sr2.setTieBreaker(sr3);
		e.setSequencingRule(sr2);
		e.setInitialSeed(seed);

		e.runExperiment();

		SummaryStat stat = (SummaryStat) e.getResults().get("flowtimeDev");

		e.printResults();

	}

	public static void RunDE(String rule, long seed) {
		DynamicShopExperiment dsExp;

		dsExp = new DynamicShopExperiment();
		dsExp.setInitialSeed(seed);

		// remove default BasicJobStatCollector
		NotifierListener<Simulation, SimEvent>[] l = dsExp.getShopListener();
		assert l.length == 1 && l[0] instanceof BasicJobStatCollector;
		dsExp.setShopListener(null);

		BasicJobStatCollector basicJobStatCollector = new BasicJobStatCollector();
		// basicJobStatCollector.setIgnoreFirst(100);
		// basicJobStatCollector.setInitialPeriod(100);

		dsExp.addShopListener(basicJobStatCollector);

		dsExp.setNumMachines(5);
		dsExp.setNumOps(5, 5);
		dsExp.setDueDateFactor(new DblConst(4.0));
		dsExp.setUtilLevel(0.95d);
		dsExp.setScenario(Scenario.JOB_SHOP);
		dsExp.setStopAfterNumJobs(300);

		PR sr = new NormalizedBrankeRule_StringExecution(rule);
		PR sr2 = new IgnoreFutureJobs(sr);
		PR sr3 = new FASFS();
		sr2.setTieBreaker(sr3);
		dsExp.setSequencingRule(sr2);

		// dsExp.setSequencingRule(new
		// NormalizedBrankeRule_StringExecution(rule));

		dsExp.runExperiment();

		Map<String, Object> res = dsExp.getResults();
		SummaryStat flowtime = (SummaryStat) res.get("flowtime");
		dsExp.printResults();
	}

	public static void RunMRE(PR rule, long seed, int NumOfReplications, int objective, TypeOfObjective typeofobj) {
		CustomGPTest test = new CustomGPTest();
		test.obj = EObjectives.FlowTime;
		test.InitialSeed = seed;
		test.NumOfReplications = NumOfReplications;

		DynamicShopExperiment e = Util.getBaseExperiment(2500, 10, 10, 0.85, objective, seed);
		e.typeOfObjective = typeofobj;

		MultipleReplicationExperiment mre = new MultipleReplicationExperiment();		
		mre.setBaseExperiment(e);

		PR sr = rule;

		PR sr2 = new IgnoreFutureJobs(sr);
		PR sr3 = new FASFS();
		sr2.setTieBreaker(sr3);
		e.setSequencingRule(sr2);
		mre.setMaxReplications(test.NumOfReplications);
		mre.setInitialSeed(test.InitialSeed);

		mre.runExperiment();

		mre.getResults();
		mre.printResults();
	}

	public static Map<String, Object> runDJSP(String rule, int normal, long seed, int minops, int maxops, double ulevel, int objective, TypeOfObjective typeofobj) {
		
		DynamicShopExperiment e = Util.getBaseExperiment(2500, minops, maxops, ulevel, objective, seed);
		e.typeOfObjective = typeofobj;

		PR sr = null;
		if (normal == 0)
			sr = new MyCustomGP(rule);
		else
			sr = new NormalizedBrankeRule_StringExecution(rule);

		PR sr2 = new IgnoreFutureJobs(sr);
		PR sr3 = new FASFS();
		sr2.setTieBreaker(sr3);
		e.setSequencingRule(sr2);

		MultipleReplicationExperiment mre = new MultipleReplicationExperiment();
		mre.setBaseExperiment(e);
		mre.setMaxReplications(1);
		mre.setInitialSeed(seed);
		
		mre.runExperiment();
		//mre.printResults();
		return mre.getResults();
	}

	public static void RunMRE(String rule, int normal, long seed, int NumOfReplications, int objetive, TypeOfObjective typeofobj) {
		CustomGPTest test = new CustomGPTest();
		test.obj = EObjectives.FlowTime;
		test.InitialSeed = seed;
		test.NumOfReplications = NumOfReplications;

		test.curGPRule = rule;

		DynamicShopExperiment e = Util.getBaseExperiment(2500, 10, 50, 0.95, objetive, seed);
		e.typeOfObjective = typeofobj;

		MultipleReplicationExperiment mre = new MultipleReplicationExperiment();
		mre.setBaseExperiment(e);

		PR sr = null;
		// sr = new PTPlusWINQPlusNPT();
		// sr = new TempRule();
		if (normal == 0)
			sr = new MyCustomGP(test.curGPRule);
		else
			sr = new NormalizedBrankeRule_StringExecution(test.curGPRule);

		PR sr2 = new IgnoreFutureJobs(sr);
		PR sr3 = new FASFS();
		sr2.setTieBreaker(sr3);
		e.setSequencingRule(sr2);
		mre.setMaxReplications(test.NumOfReplications);
		mre.setInitialSeed(test.InitialSeed);
		mre.typeOfObjective = typeofobj;

		// for (int i = 0; i < test.NumOfReplications; i++) {
		// mre.addKeepResultName("flowtime");
		// }

		mre.runExperiment();

		mre.printResults();
		
		//System.out.println(((SummaryStat) mre.getResults().get("cMax")).mean());
	}

	public static void RunMRE_NoDeviation(String rule, int normal, long seed, int NumOfReplications) {
		CustomGPTest test = new CustomGPTest();
		test.obj = EObjectives.FlowTime;
		test.InitialSeed = seed;
		test.NumOfReplications = NumOfReplications;

		test.curGPRule = rule;
		DynamicShopExperiment e = new DynamicShopExperiment();

		// remove default BasicJobStatCollector
		NotifierListener<Simulation, SimEvent>[] l = e.getShopListener();
		assert l.length == 1 && l[0] instanceof BasicJobStatCollector;
		e.setShopListener(null);

		BasicJobStatCollector basicJobStatCollector = new BasicJobStatCollector();
		basicJobStatCollector.setInitialPeriod(500);
		basicJobStatCollector.setIgnoreFirst(500);

		ExtendedJobStatCollector extJobStatCollector = new ExtendedJobStatCollector();
		extJobStatCollector.setInitialPeriod(500);
		extJobStatCollector.setIgnoreFirst(500);

		e.addShopListener(basicJobStatCollector);

		e.setNumMachines(10);
		e.setNumOps(10, 10);
		e.setDueDateFactor(new DblConst(4.0));
		// e.setWeights(new IntEmpirical(new double[] { 0.20, 0.60, 0.20 }, new
		// int[] { 1, 2, 4 }));
		e.setUtilLevel(0.95d);
		e.setStopAfterNumJobs(2500);
		e.setScenario(Scenario.JOB_SHOP);
		// e.setWeights(new IntUniformRange(1, 10));

		MultipleReplicationExperiment mre = new MultipleReplicationExperiment();
		mre.setBaseExperiment(e);

		PR sr = null;
		// sr = new PTPlusWINQPlusNPT();
		// sr = new TempRule();
		if (normal == 0)
			sr = new MyCustomGP(test.curGPRule);
		else
			sr = new NormalizedBrankeRule_StringExecution(test.curGPRule);

		PR sr2 = new IgnoreFutureJobs(sr);
		PR sr3 = new FASFS();
		sr2.setTieBreaker(sr3);
		e.setSequencingRule(sr2);
		mre.setMaxReplications(test.NumOfReplications);
		mre.setInitialSeed(test.InitialSeed);

		// for (int i = 0; i < test.NumOfReplications; i++) {
		// mre.addKeepResultName("flowtime");
		// }

		mre.runExperiment();

		mre.printResults();
	}

	public static void testFinalEvaluation(String cpath, String outputFile, double utilLevel, int numMachines,
			int minOPs, int maxOPs, long seed) {
		// String path = "E:\\开发必备\\Genetic
		// Programming\\实验数据\\测试TSIDOCBA\\Experiment\\Full GP
		// Run\\Full+TSOCBA+Final\\14-30";

		GPCompareTest test = new GPCompareTest();
		test.InitialSeed = seed;// 778899;
		test.Normal = 1;
		test.NumOfReplications = 200;
		test.outputExcelFile = outputFile;
		test.numMachines = numMachines;
		test.minOPs = minOPs;
		test.maxOPs = maxOPs;
		test.utilizationLevel = utilLevel;

		int instance = 14;
		int ComparedCount = instance;

		TestConfiguration t00 = new TestConfiguration();
		t00.ConfigDescription = "Original";
		t00.InstanceCount = instance;

		TestConfiguration t0 = new TestConfiguration();
		t0.ConfigDescription = "EA";
		t0.InstanceCount = instance;

		TestConfiguration t1 = new TestConfiguration();
		t1.ConfigDescription = "IDOCBA";
		t1.InstanceCount = instance;

		TestConfiguration t2 = new TestConfiguration();
		t2.ConfigDescription = "OCBA";
		t2.InstanceCount = instance;

		TestConfiguration t3 = new TestConfiguration();
		t3.ConfigDescription = "KG";
		t3.InstanceCount = instance;

		test.NumOfInstance = ComparedCount;
		test.TestingFinalEvaluation(cpath, t00, t0, t1, t2);
		// test.TestingFinalEvaluation(cpath, t00);

		System.out.println("The seed is " + test.InitialSeed);
	}

	public static void testFinalTopNRules(long seed, int isNormal, String path, int minInstance, int maxInstance,
			int minops, int maxops, double utilizationlevel, int objective, TypeOfObjective typeofobj) {
		Random seedStream = new Random(seed);
		int numOfTop = 10;
		int testInstances = 100;	
		WritableWorkbook writeBook = null;

		HashMap<String, Double> ruleMapFitness = new HashMap();

		GPCompareTest test = new GPCompareTest();

		List<String> testConfig = new ArrayList();
		testConfig.add("Original");
//		testConfig.add("EA");
//		testConfig.add("RS");
//		testConfig.add("OCBA");

		HashMap mapEvaluatorRules = new HashMap();

		for (int i = minInstance; i <= maxInstance; i++) {
			String filename = path + "\\job." + i + ".Result_Evaluation.stat";
			try {

				ruleMapFitness.clear();
				writeBook = Workbook.createWorkbook(new File(path + "\\" + i +".Seed." + seed + "." + minops + "-" + maxops + "-" + utilizationlevel + ".xls"));
				
				WritableSheet curSheet = null;

				for (String conf : testConfig) {
					mapEvaluatorRules.put(conf, test.readBestNRulesByEvaluatorName(new File(filename), conf, numOfTop));
				}

				for (String conf : testConfig) {
					List<String> ruleSet = (List<String>) mapEvaluatorRules.get(conf);

					curSheet = writeBook.createSheet(conf, 0);

					curSheet.addCell(new Label(0, 0, conf));

					System.out.println(conf);

					int col = 1;

					// Write rule in column header
					for (String s : ruleSet) {
						curSheet.addCell(new Label(col, 1, s));
						col++;
						// System.out.println(runDJSP(s, 1, 785, 11));
					}

					// We test and log each experiment
					seedStream = new Random(seed);
					int row = 2;

					for (int t = 0; t < testInstances; t++) {
						long testSeed = seedStream.nextLong();
						
						String string_Seed = Long.toString(testSeed);
						
						
						
						curSheet.addCell(new Label(0, row, string_Seed));
						col = 1;

						double minFitness = Double.MAX_VALUE;
						for (String rule : ruleSet) {
							
							double fitness = 0;
							if(ruleMapFitness.containsKey(rule + string_Seed))
								fitness = ruleMapFitness.get(rule + string_Seed);
							else
							{
								Map<String, Object> result = runDJSP(rule, isNormal, testSeed, minops, maxops,utilizationlevel,  objective, typeofobj);
								SummaryStat flowtime = (SummaryStat) result.get(Util.getObjectiveString(objective));
								fitness = flowtime.mean();
								ruleMapFitness.put(rule + string_Seed, fitness);
							}
							
							if(fitness < minFitness)
								minFitness = fitness;
							
							System.out.println(conf + " " + testSeed + " " + fitness);
							curSheet.addCell(new Label(col, row, Double.toString(fitness)));
							col++;
						}
						
						curSheet.addCell(new Label(col + 2, row, Double.toString(minFitness)));

						row++;

					}

					
					
				}

				writeBook.write();
				writeBook.close();

			} catch (WriteException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void testOldSeperateEvaluation(long seed, String path, int numofInstance, int minops, int maxops,
			double utilizationlevel) {
		GPCompareTest test = new GPCompareTest();
		test.InitialSeed = seed;
		test.Normal = 1;
		test.NumOfReplications = 200;
		test.minOPs = minops;
		test.maxOPs = maxops;
		test.utilizationLevel = utilizationlevel;

		String path1 = "E:\\开发必备\\Genetic Programming\\实验数据\\测试TSIDOCBA\\Experiment\\100Ind+20R\\W12(5)+8+0.45\\50";
		String path2 = "E:\\开发必备\\Genetic Programming\\实验数据\\测试TSIDOCBA\\Experiment\\EA-40R\\1429";
		String path3 = "E:\\开发必备\\Genetic Programming\\实验数据\\测试TSIDOCBA\\Experiment\\RS10_10_0.7";
		// String path3 = "E:\\开发必备\\Genetic Programming\\实验数据\\GP
		// Optimization\\500IND+10Gen+STD+Normal+InGenEvaluation+FinalEvaluation+Deviation\\TSIDOCBA\\Compare\\iOCBA";
		String path4 = "E:\\开发必备\\Genetic Programming\\实验数据\\测试TSIDOCBA\\20171122修正TOCBA\\OCBA_New_DuplicationRemoval+10W+10RS+5Iteration";
		String path5 = "E:\\开发必备\\Genetic Programming\\实验数据\\Random Search\\500IND+Normal+DuplicateRemoval+1Gen\\150Run+ERC+Seed123456789\\AOAPlog";

		int instance = numofInstance;

		int ComparedCount = instance;

		TestConfiguration t0 = new TestConfiguration();
		t0.filePath = path;
		t0.ConfigDescription = "500Ind+10R+EA+1R";
		t0.InstanceCount = instance;

		TestConfiguration t1 = new TestConfiguration();
		t1.filePath = path2;
		t1.ConfigDescription = "500Ind+10R+OCBA";
		t1.InstanceCount = instance;

		TestConfiguration t2 = new TestConfiguration();
		t2.filePath = path3;
		t2.ConfigDescription = "500Ind+10R+iOCBA";
		t2.InstanceCount = instance;

		TestConfiguration t3 = new TestConfiguration();
		t3.filePath = path4;
		t3.ConfigDescription = "500Ind+10R+AOAP";
		t3.InstanceCount = instance;

		TestConfiguration t4 = new TestConfiguration();
		t4.filePath = path4;
		t4.ConfigDescription = "500Ind+10R+KG";
		t4.InstanceCount = instance;

		TestConfiguration t5 = new TestConfiguration();
		t5.filePath = path5;
		t5.ConfigDescription = "500Ind+15R+AOAP";
		t5.InstanceCount = instance;

		test.NumOfInstance = ComparedCount;

		test.CompareAndLogForEvaluation(
				path + "\\Result.Raw" + minops + "." + maxops + "." + utilizationlevel + "." + seed + ".xls", t0);

		System.out.println(test.InitialSeed);
	}

	@Test
	public static void main4s(String[] args) throws Exception {

		// GPCompareTest test = new GPCompareTest();
		// test.doReadProgramLength("E:\\开发必备\\Genetic
		// Programming\\实验数据\\测试TSIDOCBA\\Experiment\\Pre5+RS12_8_0.4+final20\\1449");

		
		String path = "F:\\个人\\北京大学\\博士论文\\Thesis\\计算实验\\NumOfTardy\\RSGP";
		String path2 = "F:\\个人\\北京大学\\博士论文\\Thesis\\计算实验\\NumOfTardy\\OCBA";
		String path3 = "F:\\个人\\北京大学\\博士论文\\Thesis\\计算实验\\NumOfTardy\\EA";

		int objective = 5;
		int normalization = 0;
		long seed = 873;
		TypeOfObjective to = TypeOfObjective.mean;
		//testFinalTopNRules(seed, normalization, path, 11, 11, 10, 10, 0.95, objective, to);		
		testFinalTopNRules(seed, normalization, path, 3, 3, 10, 10, 0.95, objective, to);
//		testFinalTopNRules(seed, normalization, path, 0, 0, 2, 10, 0.95, objective, to);		
//		testFinalTopNRules(seed, normalization, path, 0, 0, 2, 10, 0.85, objective, to);
//		testFinalTopNRules(seed, normalization, path, 0, 0, 10, 10, 0.95, objective, to);	
//		testFinalTopNRules(seed, normalization, path, 0, 0, 50, 50, 0.95, objective, to);
		
//		testFinalTopNRules(873, normalization, path2, 3, 3, 10, 10, 0.95, objective, to);		
//		testFinalTopNRules(873, normalization, path2, 3, 3, 10, 10, 0.85, objective, to);
//		
//		testFinalTopNRules(873, normalization, path3, 3, 3, 10, 10, 0.95, objective, to);		
//		testFinalTopNRules(873, normalization, path3, 3, 3, 10, 10, 0.85, objective, to);
//		 testFinalEvaluation(path, "test-10.10.95", 0.95d, 10, 10, 10,
//		 778899);

		// testFinalEvaluation(path,
		// "test-10.10.95", 0.95d, 10, 10, 10, 778899);

		// testFinalEvaluation("E:\\开发必备\\Genetic
		// Programming\\实验数据\\测试TSIDOCBA\\Experiment\\Full GP
		// Run\\Full+TSOCBA+Elite\\Modified修正top的选取\\Final Multiple",
		// "test-2.10.95", 0.95d, 10, 2, 10, 4456);
		//
		// testFinalEvaluation("E:\\开发必备\\Genetic
		// Programming\\实验数据\\测试TSIDOCBA\\Experiment\\Full GP
		// Run\\Full+TSOCBA+Elite\\Modified修正top的选取\\Final Multiple",
		// "test-2.10.85", 0.85d, 10, 2, 10, 4456);

		// String path = "E:\\开发必备\\Genetic
		// Programming\\实验数据\\测试TSIDOCBA\\Experiment\\Full GP Run\\EA+10R";
		// long seed = 778899;
		// int count = 30;
		// testOldSeperateEvaluation(seed, path, count, 10,10,0.95);
		// testOldSeperateEvaluation(seed, path, count, 10,10,0.85);
		// testOldSeperateEvaluation(seed, path, count, 2,10,0.95);
		// testOldSeperateEvaluation(seed, path, count, 2,10,0.85);
		// //testOldSeperateEvaluation(778899, "E:\\开发必备\\Genetic
		// Programming\\实验数据\\测试TSIDOCBA\\Experiment\\Full GP
		// Run\\EA+10R\\17-19", 3);

		// Random r = new Random(181085);
		// for(int i = 0; i <=19; i++)
		// {
		// long seed = r.nextLong();
		//
		// testFinalEvaluation("E:\\开发必备\\Genetic
		// Programming\\实验数据\\测试TSIDOCBA\\Experiment\\Full GP
		// Run\\Full+TSOCBA+Final",
		// "result.full", 0.95d, 10, 10, 10, seed);
		//
		// testOldSeperateEvaluation(seed);
		// }

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
		System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
	}
	
	public static void main(String[] args)
	{
		double s = -0.34;
		double b = s * 0.095;
		
		Random random = new Random(System.currentTimeMillis());
		
		for(int i = 0; i < 30; i ++)
		{
			double a = random.nextGaussian();
			
			if(a < 0)
				a = a ;
			
			System.out.println(b * a+ s);
		}
				
		
	}

	public static void maina(String[] args) {

		
//		AviatorEvaluator.setOptimize(AviatorEvaluator.EVAL);
//		Expression compiledExp = AviatorEvaluator.compile("1.0+ -2.0333333333333333333", true);
//		Map<String, Object> env = new HashMap<String, Object>();
//		 double result = (double) compiledExp.execute(env);	
//			System.out.println(result);
			
			
		
		int objective = 11;
		int normalization = 0;
		TypeOfObjective to = TypeOfObjective.max;
		//String rule_TWTMeanRSGP = "div(PT + ((If(0.7001650364890966, max(max(div((If(PT, W, OpsLeft) * (PT * PT)) * max(max(If(TIQ, PT, RPT), max(TD, TIS) * (W - W)), (-0.859086038111071 * -0.19981239304974752) - div(-0.5444891659675979, OpsLeft)), max(RPT, OpsLeft)) - ((OpsLeft - WINQ) * If(WINQ, NPT, OpsLeft)), W - W), If(TD, WINQ, PT)), RPT) * max(div((div((If(PT, W, OpsLeft) * (PT * PT)) * max(max(If(SLACK, TD, -0.06675992423435528), If(TIQ, PT, RPT)), max(W, TD) - div(-0.5444891659675979, OpsLeft)), max(RPT, OpsLeft)) - div(-0.5444891659675979, OpsLeft)) * max(If(PT, SLACK, -0.13823443011682812), (-0.859086038111071 * -0.19981239304974752) - (max(TD, If(TD, WINQ, PT) * PT) * If(TIQ, PT, RPT))), max(RPT, OpsLeft)) - If(TIQ, PT, RPT), PT * PT)) * If(TIQ, PT, RPT)), div((W - 0.9573359476237482) * If(PT, SLACK, -0.13823443011682812), max(div(W, max(TIS * If(RPT, RPT, OpsLeft), WINQ) * If(TIQ, PT, RPT)), max(max(If(If(WINQ, NPT, OpsLeft), TD, -0.06675992423435528), If(TIQ, If(RPT, RPT, OpsLeft), RPT)), max(max(If(SLACK, TD, -0.06675992423435528), If(TIQ, PT, RPT)), (-0.859086038111071 * -0.19981239304974752) - div(-0.5444891659675979, OpsLeft)) - div(-0.5444891659675979, OpsLeft))) + PT)) * (((max(TD, TIS) * (W - W)) + If((If(PT, W, OpsLeft) * (PT * (div((If(PT, W, OpsLeft) * (PT * PT)) * max(max(If(SLACK, TD, -0.06675992423435528), If(TIQ, PT, RPT)), (-0.859086038111071 * -0.19981239304974752) - div(-0.5444891659675979, OpsLeft)), div((If(PT, W, OpsLeft) * (PT * PT)) * max(max(If(SLACK, TD, -0.06675992423435528), If(TIQ, PT, RPT)), max(W, SLACK) - div(-0.5444891659675979, OpsLeft)), max(RPT, OpsLeft)) - (max(TD, TIS) * (W - W))) - ((OpsLeft - WINQ) * If(WINQ, NPT, OpsLeft))))) * If(W, OpsLeft, W), ((max(TD, TIS) * (W - W)) + If((If(PT, W, OpsLeft) * (PT * (div((If(PT, W, OpsLeft) * (PT * PT)) * max(max(If(SLACK, TD, -0.06675992423435528), If(TIQ, PT, RPT)), (-0.859086038111071 * -0.19981239304974752) - div(-0.5444891659675979, OpsLeft)), -0.859086038111071) - ((OpsLeft - WINQ) * If(WINQ, NPT, OpsLeft))))) * If(W, OpsLeft, W), If(SLACK, TD, -0.06675992423435528), If(W, OpsLeft, WINQ))) - (max(max(If(SLACK, TD, -0.06675992423435528), max(TD, TIS) * ((W - 0.9573359476237482) * If(PT, SLACK, -0.13823443011682812))), (-0.859086038111071 * max(max((W - W) - ((OpsLeft - WINQ) * If(WINQ, NPT, OpsLeft)), div(OpsLeft, OpsLeft)), If(TD, WINQ, PT))) - div((div((If(PT, W, OpsLeft) * (PT * PT)) * max(max(If(SLACK, TD, -0.06675992423435528), If(TIQ, PT, RPT)), max(W, TD) - div(-0.5444891659675979, OpsLeft)), max(RPT, OpsLeft)) - ((-0.859086038111071 * -0.19981239304974752) * If(WINQ, NPT, OpsLeft))) * max(max(If(SLACK, TD, -0.06675992423435528), max(TD, TIS) * (W - W)), (-0.859086038111071 * -0.19981239304974752) - (max(TD, If(TD, WINQ, PT) * PT) * If(TIQ, PT, RPT))), max(RPT, OpsLeft))) - (If(RPT, RPT, OpsLeft) - (SLACK * TIS))), If(W, OpsLeft, WINQ))) - ((max(div((If(PT, W, OpsLeft) * If(TIQ, PT, RPT)) * max((W - 0.9573359476237482) * If(If(WINQ, NPT, OpsLeft), TD, -0.06675992423435528), If(PT, W, OpsLeft)), max(RPT, OpsLeft)), (-0.859086038111071 * -0.19981239304974752) - ((If(PT, W, OpsLeft) * (PT * PT)) * max(max(If(SLACK, TD, -0.06675992423435528), max(TD, TIS) * (W - W)), -0.859086038111071 * -0.19981239304974752))) - (If(RPT, RPT, OpsLeft) - (SLACK * TIS))) - (If(PT, SLACK, -0.13823443011682812) - (SLACK * TIS))))";
		
		String rule = "max((((max(RPT + If(max(PT, TIQ), div(TIS, PT), max(PT, 0.0)), If(div(0.0, TIS), If(0.0, 0.0, TIS), If(WINQ, WINQ, NPT))) + (RPT * 1.0)) + (((max(OpsLeft, TIS) + (max((TIS + 0.0) + div(div(0.0 * NPT, If(1.0, OpsLeft, TIS)), div(TIQ * 0.0, WINQ - TIQ)), RPT * div(max((RPT * 1.0) + (RPT * 1.0), If(div(0.0, TIS), If(max(PT, TIQ), div(TIS, PT), max(PT, 0.0)), If(WINQ, WINQ, NPT))), max(WINQ, PT))) + max(max(If(div(0.0, TIS), If(0.0, 0.0, TIS), If(WINQ, WINQ, NPT)), max(RPT + If(max(PT, TIQ), div(TIS, PT), max(PT, 0.0)), If(div(0.0, TIS), If(0.0, 0.0, TIS), If(WINQ, WINQ, NPT))) + max((RPT * 1.0) + (RPT * 1.0), div(0.0, TIS))) + (RPT * 1.0), If(div(0.0, TIS), If(0.0, 0.0, TIS), If(WINQ, WINQ, NPT))))) + If(If(TIS, OpsLeft, 0.0), max(OpsLeft, div(RPT, 0.0)) + max((TIS + 0.0) + (RPT * 1.0), If(div(0.0, TIS), ((If(max(PT, TIQ), div(TIS, PT), max(PT, 0.0)) + max(RPT + div(TIS, PT), If(div(0.0, TIS), If(0.0, 0.0, TIS), If(WINQ, WINQ, NPT)))) + (RPT * 1.0)) - div(div(0.0 * NPT, If(1.0, OpsLeft, TIS)), div(TIQ * 0.0, WINQ - TIQ)), If(WINQ, WINQ, NPT))), WINQ + NPT)) - max(1.0, RPT))) + div(max(PT, PT), If(1.0, OpsLeft, TIS))) - div(div(0.0 * NPT, If(1.0, OpsLeft, TIS)), div(TIQ * 0.0, WINQ - If(If(TIS, OpsLeft, 0.0), max(OpsLeft, div(RPT, 0.0)) + max((TIS + 0.0) + (RPT * 1.0), If(div(0.0, TIS), ((If(max(PT, TIQ), div(TIS, PT), max(PT, 0.0)) + max(RPT + max(OpsLeft, TIS), If(div(0.0, TIS), If(0.0, 0.0, TIS), If(WINQ, WINQ, NPT)))) + (RPT * 1.0)) - div(div(0.0 * NPT, If(1.0, OpsLeft, TIS)), div(TIQ * 0.0, WINQ - TIQ)), If(WINQ, WINQ, NPT))), WINQ + NPT))), div(If(If(WINQ, OpsLeft, OpsLeft), max(RPT, 0.0), If(NPT, OpsLeft, PT)), max(PT, PT) - div(TIQ, 0.0)) * (If(WINQ, RPT, 1.0) + max(RPT * div(If(max(PT, TIQ), div(TIS, PT), max(PT, 0.0)), max(WINQ, PT)), 0.0 - 0.0)))";
		String rule2 = "(max(max((TIS - 1.0) + div(0.0, PT), max(max(If(max(If(max(TIQ, 0.0), If(WINQ, 1.0, RPT), TIQ * 0.0), max(PT + TIS, OpsLeft - NPT)), (TIS - PT) + (((If(WINQ, PT, 0.0) - max(TIQ, NPT)) + OpsLeft) + max(TIS, TIS + RPT)), div(If(PT, NPT, RPT), 1.0 + 1.0) - div(TIS * TIQ, max(RPT, TIQ))), (TIS * max(If(If((If(TIQ, RPT, NPT) - If(OpsLeft, 0.0, PT)) * (TIQ - (TIS - TIQ)), (RPT + TIS) + (max(NPT, TIS) + (TIS - PT)), If(div(PT * OpsLeft, max(1.0, RPT)), div(OpsLeft - OpsLeft, 1.0 + NPT), div(div(OpsLeft, TIS), max(WINQ, TIQ)))), max(If(0.0 - 0.0, TIS - 1.0, RPT + NPT) + (If(1.0, TIS, WINQ) + (TIQ * 0.0)), If(0.0 - 0.0, TIS - 1.0, RPT + NPT) * div(0.0, RPT)), If(div((TIS - WINQ) * (NPT * NPT), If(PT + RPT, If(1.0, 0.0, WINQ), max(1.0, 1.0))), max(RPT * OpsLeft, PT + RPT) - (max(1.0, NPT) + max(TIS, 0.0)), If(If(0.0 - TIS, RPT * PT, max(TIQ, OpsLeft)), max(0.0, OpsLeft) * (PT - RPT), If(WINQ, TIQ, 1.0) + (RPT - PT)))), max(TIS, 1.0))) + (If(0.0 - 0.0, TIS - 1.0, RPT + NPT) + (If(If(0.0 - 0.0, TIS - 1.0, RPT + NPT) + (If(1.0, TIS, WINQ) + (TIQ * 0.0)), TIS, WINQ) + (TIQ * 0.0)))) + (div(OpsLeft, PT) - WINQ), TIS)) + ((((((TIS - div(TIQ, TIQ)) + (((((TIS * 0.0) + (RPT + TIS)) * TIS) + (TIS - PT)) + (TIS + RPT))) + div(OpsLeft, PT)) * max(If(If((If(TIQ, RPT, NPT) - If(OpsLeft, 0.0, PT)) * If(WINQ - 0.0, 1.0 - WINQ, PT - 1.0), div(0.0, RPT), If(div(PT * OpsLeft, max(1.0, RPT)), div(OpsLeft - OpsLeft, 1.0 + NPT), div(div(OpsLeft, TIS), max(WINQ, TIQ)))), max(If(0.0 - 0.0, TIS - 1.0, RPT + NPT) + (If(1.0, TIS, WINQ) + (TIQ * 0.0)), (If(WINQ, PT, 0.0) - max(TIQ, NPT)) * (TIS - max(RPT, NPT))), If(div((TIS - WINQ) * (NPT * NPT), If(PT + RPT, If(1.0, 0.0, WINQ), max(1.0, 1.0))), max(RPT * OpsLeft, PT + RPT) - (max(1.0, NPT) + max(TIS, 0.0)), If(If(0.0 - TIS, RPT * PT, max(TIQ, OpsLeft)), max(0.0, OpsLeft) * (PT - RPT), If(WINQ, TIQ, 1.0) + (RPT - PT)))), (TIS * 0.0) + (div(div(0.0, 0.0), 0.0 + 0.0) - WINQ))) + (TIS + RPT)) * max(div(TIS * TIS, div(TIQ, TIQ)), (TIS * 0.0) + (div(div(0.0, 0.0), 0.0 + 0.0) - WINQ))), max(If(If((If(TIQ, RPT, NPT) - If(OpsLeft, 0.0, PT)) * (TIQ - PT), div(0.0, RPT), If(div(PT * OpsLeft, max(1.0, RPT)), div(OpsLeft - OpsLeft, 1.0 + NPT), div(div(OpsLeft, TIS), max(WINQ, TIQ)))), max(If(0.0 - 0.0, TIS - 1.0, RPT + NPT) + (If(1.0, TIS, WINQ) + (TIQ * 0.0)), div(max(TIQ, PT), PT) * ((TIQ * TIS) - max(RPT, NPT))), If(div((TIS - WINQ) * (NPT * NPT), If(PT + RPT, If(1.0, 0.0, WINQ), max(1.0, 1.0))), max(RPT * OpsLeft, PT + RPT) - (max(1.0, NPT) + max(TIS, 0.0)), If(If(0.0 - TIS, RPT * PT, max(TIQ, OpsLeft)), max(0.0, OpsLeft) * (PT - RPT), If(WINQ, TIQ, 1.0) + (RPT - PT)))), max(If(0.0 - 0.0, TIQ * 0.0, RPT + NPT) + (If(1.0, TIS, WINQ) + (TIQ * 0.0)), ((((TIS - div(TIQ, TIQ)) + ((max(NPT, TIS) + (TIS - PT)) + (TIS + RPT))) + (TIS + max(0.0, OpsLeft))) * max(If(If((If(TIQ, RPT, NPT) - If(OpsLeft, 0.0, PT)) * If(WINQ - 0.0, 1.0 - WINQ, PT - 1.0), div(0.0, RPT), div(0.0, RPT)), div(TIS - 1.0, div(TIQ, TIQ)), If(div((TIS - WINQ) * (NPT * NPT), If(PT + RPT, If(1.0, 0.0, WINQ), max(1.0, 1.0))), max(RPT * OpsLeft, PT + RPT) - (max(1.0, NPT) + ((TIS * 0.0) + (div(div(0.0, 0.0), 0.0 + 0.0) - WINQ))), If(If(0.0 - TIS, RPT * PT, max(TIQ, OpsLeft)), max(0.0, OpsLeft) * (PT - RPT), If(WINQ, TIQ, 1.0) + (RPT - PT)))), max(TIS, 1.0))) * ((TIQ * TIS) - max(RPT, NPT))))) - PT) + max(max(TIS, OpsLeft + ((((((TIS - div(TIQ, TIQ)) + max(If(If((If(TIQ, RPT, NPT) - If(OpsLeft, 0.0, PT)) * (TIQ - PT), div(0.0, RPT), If(div(PT * OpsLeft, max(1.0, RPT)), div(OpsLeft - OpsLeft, 1.0 + NPT), div(div(OpsLeft, TIS), max(WINQ, TIQ)))), max(NPT, TIS), If(div((TIS - WINQ) * (NPT * NPT), If(PT + RPT, If(1.0, 0.0, WINQ), max(1.0, 1.0))), max(RPT * OpsLeft, PT + RPT) - (max(1.0, NPT) + max(TIS, 0.0)), If(If(0.0 - TIS, RPT * PT, max(TIQ, OpsLeft)), max(0.0, OpsLeft) * (PT - RPT), If(WINQ, TIQ, 1.0) + (RPT - PT)))), max(TIS, 1.0))) + (TIS + max(0.0, OpsLeft))) * max(If(If((If(TIQ, RPT, NPT) - If(OpsLeft, 0.0, PT)) * If(WINQ - 0.0, 1.0 - WINQ, PT - 1.0), div(0.0, RPT), If(div(PT * OpsLeft, max(1.0, RPT)), div(OpsLeft - OpsLeft, 1.0 + NPT), div(div(OpsLeft, TIS), max(WINQ, TIQ)))), max(If(0.0 - 0.0, TIS - 1.0, RPT + NPT) + (If(1.0, TIS, WINQ) + (TIQ * 0.0)), (If(WINQ, PT, 0.0) - max(TIQ, NPT)) * div(OpsLeft, PT)), If(div((TIS - WINQ) * (NPT * NPT), If(PT + RPT, If(1.0, 0.0, WINQ), max(1.0, 1.0))), max(RPT * OpsLeft, PT + RPT) - (max(1.0, NPT) + max(TIS, 0.0)), If(If(0.0 - TIS, RPT * PT, max(TIQ, OpsLeft)), max(0.0, OpsLeft) * (PT - RPT), If(WINQ, TIQ, 1.0) + (RPT - PT)))), max(TIS, 1.0))) + (TIS + RPT)) * max(div(TIS * TIS, div(TIQ, TIQ)), (TIS * 0.0) + (div(div(0.0, 0.0), 0.0 + 0.0) - WINQ)))), 1.0)";
		String rule3 = "If(max(If(max(TIQ, 0.0), If(WINQ, 1.0, RPT), TIQ * 0.0), max(PT + TIS, OpsLeft - NPT)), (If(div(If(RPT, OpsLeft, WINQ), If(RPT, 1.0, PT)), div(max((TIS - 1.0) + max((1.0 * RPT) + div(0.0, PT), RPT), RPT) + TIS, WINQ + PT), If(NPT - PT, max(RPT, 1.0), PT * 1.0)) + (OpsLeft - (WINQ + PT))) + max(0.0 + ((OpsLeft - If(max(If(max(TIQ, 0.0), If(WINQ, 1.0, RPT), TIQ * 0.0), max(PT + TIS, OpsLeft - NPT)), (If(div(If(RPT, OpsLeft, WINQ), If(RPT, 1.0, PT)), div(max(TIS - 1.0, 1.0 * RPT), WINQ + PT), If(NPT - PT, max(RPT, 1.0), PT * 1.0)) + (OpsLeft - (WINQ + PT))) + max(TIS - 1.0, 1.0 * RPT), div(If(PT, NPT, RPT), 1.0 + 1.0) - div(TIS * TIQ, max(RPT, TIQ)))) - 1.0), 1.0 * RPT), div(If(PT, NPT, RPT), 1.0 + 1.0) - div(TIS * TIQ, max(RPT, TIQ))) + ((max((OpsLeft + TIS) + (((If(RPT, OpsLeft, WINQ) + (If(max(TIS, TIS - 0.0) - div(1.0, TIS), (If(1.0, TIQ, TIS) + If(WINQ, OpsLeft, 1.0)) + max(If(0.0, TIS, WINQ), 1.0 * RPT), div(If(PT, NPT, RPT), 1.0 + 1.0) - div(TIS, max(RPT, TIQ))) + (max(1.0 * RPT, If(max(If(max(TIQ, 0.0), If(WINQ, 1.0, RPT), TIQ * 0.0), max(PT + TIS, OpsLeft - NPT)), (RPT - div(1.0, TIS)) + max(If(0.0, TIS, WINQ), 1.0 * RPT), div(If(PT, NPT, RPT), 1.0 + 1.0) - div(TIS * TIQ, max(RPT, TIQ))) + (TIS * TIS)) * TIS))) + TIS) - OpsLeft), (max(TIS, TIS - 0.0) - div(1.0, TIS)) + ((max(TIS, TIS - 0.0) - div(1.0, TIS)) + ((div(If(PT, NPT, RPT), 1.0 + 1.0) - div(TIS, max(RPT, TIQ))) + If(div(If(RPT, OpsLeft, WINQ), If(RPT, 1.0, PT)), div(max(max(RPT, TIQ), RPT) + TIS, WINQ + PT), If(NPT - PT, max(RPT, 1.0), PT * 1.0))))) * TIS) * TIS)";
		// CalculateRanking();
		int count = 10;
		long seed = 873;
		
		RunMRE(rule, normalization, seed, count, objective, to);	
//		RunMRE(rule2, normalization, seed, count, objective, to);
//		RunMRE(rule3, normalization, seed, count, objective, to);
		
		
		if(1==1)
			return;
		
		 RunMRE(new FCFS(), seed, count, objective, to);
		 RunMRE(new SPT(), seed, count, objective, to);		
		 RunMRE(new ERD(), seed, count, objective, to);
		 RunMRE(new EDD(), seed, count, objective, to);		 
		 RunMRE(new SRPT(), seed, count, objective, to);  //TWKR
		 RunMRE(new SRPTPerPT(), seed, count, objective, to);  // SPT/TWKR		
		 RunMRE(new MDD(), seed, count, objective, to);
		 RunMRE(new MOD(), seed, count, objective, to);
		 RunMRE(new ATC(), seed, count, objective, to);
		 RunMRE(new WINQ(), seed, count, objective, to);
		 RunMRE(new PTPlusWINQ(), seed, count, objective, to);
		 RunMRE(new PTPlusWINQPlusSlack(), seed, count, objective, to);
		 RunMRE(new PTPlusWINQPlusNPT(), seed, count, objective, to);			
		 RunMRE(new WMOD(), seed, count, objective, to);
		 RunMRE(new WMDD(), seed, count, objective, to);	 

	}

	public static void CalculateRanking() {
		String benchmarkFile = "E:\\开发必备\\Genetic Programming\\ECJ\\jar\\20171118RS测试\\1000Replications.txt";
		String targetFile = "E:\\开发必备\\Genetic Programming\\ECJ\\jar\\20171118RS测试\\Details_Evaluation.stat";
		// targetFile = "E:\\开发必备\\Genetic
		// Programming\\ECJ\\jar\\20171118RS测试\\RS_热启10R_RS100R_热启种子不同.txt";
		// targetFile = "E:\\开发必备\\Genetic
		// Programming\\ECJ\\jar\\20171118RS测试\\OCBA_10+10_热启种子相同.txt";

		// CorrelationTesting test = new CorrelationTesting(benchmarkFile,
		// "E:\\开发必备\\Genetic
		// Programming\\ECJ\\jar\\20171118RS测试\\RS_热启10R_RS10R_热启种子相同.txt");
		CorrelationTesting test = new CorrelationTesting(benchmarkFile, targetFile);

		System.out.println(test.getSpearmanRankCoefficient());
		System.out.println(test.getCorrelationCoefficient2());
	}

	public static void main33(String[] args) throws RowsExceededException, WriteException, IOException {

		GPCompareTest test = new GPCompareTest();
		// test.readDetailsBudgetDistribution(new File("E:\\开发必备\\Genetic
		// Programming\\实验数据\\测试TSIDOCBA\\Experiment\\Full GP
		// Run\\Full+TSOCBA1224\\20\\job.0.Details_Evaluation.stat"));

		String path = "E:\\开发必备\\Genetic Programming\\实验数据\\测试TSIDOCBA\\Experiment\\Full GP Run\\EA+10R";

		int count = 10;

		for (int i = 0; i < count; i++) {
			String filename = path + "\\job." + i + ".Result_Evaluation.stat";
			// test.testEachGenerationRule(new File(filename),
			// path);
		}

		// List<String> rules = test.testEachGenerationRule(new
		// File("E:\\开发必备\\Genetic
		// Programming\\实验数据\\测试TSIDOCBA\\Experiment\\EA-20R\\job.0.Result_Evaluation.stat"),
		// "E:\\开发必备\\Genetic
		// Programming\\实验数据\\测试TSIDOCBA\\Experiment\\EA-20R\\");

	}
}
