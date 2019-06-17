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
package jasima.shopSim.core.batchForming;

import jasima.core.statistics.SummaryStat;
import jasima.shopSim.core.Batch;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.Operation;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;
import jasima.shopSim.core.WorkStation;
import jasima.shopSim.prioRules.PRDecryptor;
import jasima.shopSim.prioRules.basic.SLK;
import jasima.shopSim.prioRules.basic.SPT;
import jasima.shopSim.prioRules.batch.BATCS;
import jasima.shopSim.prioRules.gp.testGPRule;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQPlusNPT;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ec.EvolutionState;
import ec.CDJSP.DoubleData;
import ec.gp.GPNode;

/**
 * This class implements the rule that the batch that uses most of the available
 * capacity is selected. Ties are broken with the underlying sequencing rule
 * that selects the family with the highest priority job.
 * 
 * @author Christoph Pickardt, 2010-09-07
 * @author Torsten Hildebrandt, 2010-09-29
 * @version 
 *          "$Id$"
 */
public class GPPrioRuleBatching extends BatchForming {

	private static final long serialVersionUID = -8735429872125881522L;
	private double mbsRel;
	private double maxWaitRelative;
	private double maxWait;
	private PR batchFormDR;
	public GPNode rootNode;
	
	public GPPrioRuleBatching(PR batchFormDR) {
		super(); 
		mbsRel=0.8;
		this.batchFormDR=batchFormDR;
		rootNode = ((testGPRule)batchFormDR).rootNode;
	}

	public GPPrioRuleBatching() {
		this(new SPT());
	}
 
	@Override
	public void formBatches() {  
		final PriorityQueue<Job> q = getOwner().queue;
		
		////////////// GP directly form one batch
		Batch b = efficientBatching();
		if(b!=null)			
			possibleBatches.add(b);
		
		
		///////////// GP MCB BatchFormation  //too slow and bad quality 20190409
//		MCBBatching();
		
		//////////// GP BinPacking batch formation, form a batch for each family
//		orderedJobs = ensureCapacity(orderedJobs, q.size());
//		q.getAllElementsInOrder(orderedJobs);
//		int numJobs = q.size();
//
//		// split jobs of each family
//		Map<String, List<Job>> jobsByFamily = splitFamilies(orderedJobs,
//				numJobs);
// 
//		// form two batches per family, one without future jobs
//		for (List<Job> famJobs : jobsByFamily.values()) {
//			formFamilyBatches(famJobs);
//		} 
//		
//		handleTiesByUrgent();		
		
		
		//////////// GP select family then form batch////////////////
//		orderedJobs = ensureCapacity(orderedJobs, q.size());
// 		q.getAllElementsInOrder(orderedJobs);
//		int numJobs = q.size();
//
//		// split jobs of each family
//		Map<String, List<Job>> jobsByFamily = splitFamilies(orderedJobs,
//				numJobs);
// 
//		int idx=-1,bestIdx=0;
//		double maxPrio=Double.NEGATIVE_INFINITY;
//		Job maxFamJob=null;
//		// form two batches per family, one without future jobs
//		for (List<Job> famJobs : jobsByFamily.values()) {
//			idx++;
//			double temp = familySelection(famJobs);
//			if(temp>maxPrio)
//			{
//				bestIdx=idx;
//				maxPrio=temp;
//				maxFamJob=famJobs.get(0);
//			}
//		}  
//
//		Batch bat=new Batch(maxFamJob.getShop());
//		Operation op = maxFamJob.getCurrentOperation();
//		List<Job> famJobs=getOwner().getJobsByFamily().get(op.batchFamily);
//		
//		for(int i=0;i<famJobs.size();i++) {
//			if(bat.numJobsInBatch()<op.maxBatchSize)
//			{
//				bat.addToBatch(famJobs.get(i));
//			}
//			else break;
//		} 
//		possibleBatches.add(bat);
	}
	
	public double familySelection(List<Job> famJobs)
	{
		assert famJobs.size() > 0;

		Job j = famJobs.get(0);
		Operation curOp = j.getCurrentOperation();
		assert WorkStation.BATCH_INCOMPATIBLE.equals(curOp.batchFamily) ? curOp.maxBatchSize == 1
				: true;
		
		int batchCapacity=curOp.maxBatchSize;
		DoubleData input=new DoubleData();
		int n = famJobs.size();
		double BF_FOB =(double)n/(double)batchCapacity;
		
		SummaryStat PTs=new SummaryStat();
		SummaryStat QTs=new SummaryStat();
		SummaryStat Slacks=new SummaryStat();
		SummaryStat Ws=new SummaryStat();
		
		for(Job job:famJobs) {
			PTs.value(job.currProcTime());
			QTs.value(job.getShop().simTime() - job.getArriveTime());
			Slacks.value(SLK.slack(job));
			Ws.value(job.getWeight());
		}
		
		double BF_MPT=PTs.max();
		double BF_APT=PTs.mean();  
		double BF_MQT=QTs.max();
		double BF_AQT=QTs.mean();  
		double BF_MSlack=Slacks.max();
		double BF_ASlack=Slacks.mean();  
		double BF_MW=Ws.max();
		double BF_AW=Ws.mean();

		input.BF_FOB=BF_FOB;
		input.BF_MPT=BF_MPT;
		input.BF_APT=BF_APT;
		input.BF_MQT=BF_MQT;
		input.BF_AQT=BF_AQT;
		input.BF_MSlack=BF_MSlack;
		input.BF_ASlack=BF_ASlack;
		input.BF_MW=BF_MW;
		input.BF_AW=BF_AW;
		
		//input.W=batches[b].weightStat.sum();
		rootNode.evalSimple(input);
		
		return input.x;
	}
		
	private void formFamilyBatches(List<Job> famJobs) {
		assert famJobs.size() > 0;

		Job j = famJobs.get(0);
		Operation o = j.getCurrentOperation();
		assert WorkStation.BATCH_INCOMPATIBLE.equals(o.batchFamily) ? o.maxBatchSize == 1
				: true;
		
		int batchCapacity=o.maxBatchSize;
		
//		int batchNum=(int)Math.ceil(((double)famJobs.size())/((double)o.maxBatchSize));
		
		Batch b = new Batch(j.getShop()); 
		
//		if(j.getCurrMachine().getName().equals("2号炉"))
//		{
//			int famjobsize=famJobs.size();
//			int bc=famJobs.get(0).getCurrentOperation().maxBatchSize;
//		}
		while(b.numJobsInBatch()<batchCapacity) {
			DoubleData input=new DoubleData();		
			
			double BF_BS=Double.valueOf(batchCapacity-b.numJobsInBatch());
			double BF_MPT=b.numJobsInBatch()<=0?0.0:b.procTimeStat.max();
			double BF_APT=b.numJobsInBatch()<=0?0.0:b.procTimeStat.mean();  
			double BF_MW=b.numJobsInBatch()<=0?0.0:b.weightStat.sum();
			double BF_MSlack=b.numJobsInBatch()<=0?0.0:b.slackStat.min();
//			double BF_FOB =(double)n/(double)curOp.maxBatchSize;
			
			input.BF_BS=BF_BS;
			input.BF_MPT=BF_MPT;
			input.BF_APT=BF_APT;
			input.BF_MW=BF_MW;
			input.BF_MSlack=BF_MSlack;
			
			double max=Double.NEGATIVE_INFINITY;
			int jobIndex=-1; 
			
			for (int i = 0, n = famJobs.size(); i < n; i++) {
				Job job = famJobs.get(i);
				Operation curOp = job.getCurrentOperation();
				double PT=curOp.procTime;
				double W=job.getWeight(); 
				double JS_OSLACK=job.getCurrentOperationDueDate()- job.getShop().simTime() - job.getCurrentOperation().procTime;
				double RPT=job.remainingProcTime();
				double OpsLeft=job.numOpsLeft();
				
				input.PT=PT;
				input.W=W;
				input.JS_OSLACK=JS_OSLACK;
				input.RPT=RPT;
				input.OpsLeft=OpsLeft;
				
				if(((testGPRule)batchFormDR).isTestStringGPRule) 
					input.x=max(div(BF_BS + (BF_MSlack - JS_OSLACK), ifte(div(0, BF_APT), div(JS_OSLACK, BF_BS), BF_MW * 0)), max(max(ifte(W, BF_APT, BF_MSlack), max(W, JS_OSLACK)), max(JS_OSLACK, BF_MPT) + div(0, BF_BS)));
				else rootNode.evalSimple(input);
				double temp=input.x;
				if(input.x>max)
				{
					max=input.x;
					jobIndex=i;
				}
			}
			
			b.addToBatch(famJobs.get(jobIndex));
			famJobs.remove(jobIndex);	
			
			if(famJobs.size()==0) break;
		} 
		if(b.numJobsInBatch()>0)
			possibleBatches.add(b);
	}
	
	public void pickBatch() {
		int bestBatchIdx=-1;
		double maxtemp=Double.NEGATIVE_INFINITY;
		int i=0;
		for(int counter =0; counter<possibleBatches.size();counter++)
		{
			Batch bat=possibleBatches.get(counter);
			DoubleData input=new DoubleData();
			int idx= maxJob(bat);
			Job job=bat.job(idx);
			Operation curOp=job.getCurrentOperation();
			double PT=curOp.procTime;
			double TIQ=job.getShop().simTime() - job.getArriveTime();
			double W=job.getWeight(); 
			double RPT = job.remainingProcTime();
			double OpsLeft = job.numOpsLeft();
			double JS_OSLACK=job.getCurrentOperationDueDate()- job.getShop().simTime() - job.getCurrentOperation().procTime;
//			double JS_APT=calcPTAvg();
			double ST = setupTime(job);
//			double JS_AST=calcSetupAvg(); 
			
			input.PT=PT;
			input.TIQ=TIQ;
			input.W=W;
			input.RPT=RPT;
			input.OpsLeft=OpsLeft;
			input.JS_OSLACK=JS_OSLACK;
//			input.JS_APT= JS_APT;
			input.ST=ST;
//			input.JS_AST= JS_AST;
			
			
			double BF_BS=Double.valueOf(curOp.maxBatchSize-bat.numJobsInBatch());
			double BF_MPT=bat.numJobsInBatch()<=0?0.0:bat.procTimeStat.max();
			double BF_APT=bat.numJobsInBatch()<=1?0.0:bat.procTimeStat.variancePopulation(); 
			
			List<Job> jobFamily = job.getCurrMachine().getJobsByFamily().get(job.getCurrentOperation().batchFamily);		
			double BF_JFIQ=jobFamily.size();	
			double BF_FOB =(double)BF_JFIQ/(double)curOp.maxBatchSize;
			
			input.BF_FOB=BF_FOB;
			input.BF_BS=BF_BS;
			input.BF_MPT=BF_MPT;
			input.BF_APT=BF_APT;
			
			if(((testGPRule)batchFormDR).isTestStringGPRule) 
				input.x=0;
			else rootNode.evalSimple(input);
			
			if(input.x>maxtemp)
			{
				maxtemp=input.x;
				bestBatchIdx=i;
			}
			i++;
//			if((double)bat.numJobsInBatch()/(double)batchCapacity>0.5)
//			{
//				possibleBatches.add(bat);
//			} 
		}
		
		Batch bat =possibleBatches.get(bestBatchIdx);
		possibleBatches.clear();
		possibleBatches.add(bat);
		
	}
	
	private int maxJob(Batch b) { 
		//llx 20190110 batch proc time=max(batch proc time of ALL jobs in batch)
		int maxPTJobNum=0;
		double maxPT=Double.NEGATIVE_INFINITY;
		for (int i=0;i<b.numJobsInBatch();i++)
		{
			Operation o = b.job(i).getCurrentOperation();
			if(o.procTime>maxPT)
			{
				maxPT=o.procTime;
				//maxPTJobNum=b.job(i).getJobNum();
				maxPTJobNum=i;
			}
		}		 
		return maxPTJobNum;
	}
	
	private void MCBBatching() {
		final PriorityQueue<Job> q = getOwner().queue;
		// detailed approach below
		orderedJobs = ensureCapacity(orderedJobs, q.size());
		 
		q.setSequencingRule(getOwner().getBatchSequencingRule());
				 
		q.getAllElementsInOrder(orderedJobs);
		int numJobs = q.size();

		// split jobs of each family
		Map<String, List<Job>> jobsByFamily = splitFamilies(orderedJobs,
				numJobs);

		// form batches as large as possible
		formBatches(jobsByFamily);

		// first tie breaker
		if (possibleBatches.size() > 1)
			 handleTiesByFamilySize(jobsByFamily);

		// second tie breaker
		if (possibleBatches.size() > 1)
			handleTiesByBasePrio(numJobs);
	}

	private Batch efficientBatching() {
		final PriorityQueue<Job> q = getOwner().queue;
		

		q.setSequencingRule(getOwner().getBatchSequencingRule());
		Job[] resArray = new Job[q.size()];
		q.getAllElementsInOrder(resArray);
		Job largest = resArray[0];
		Operation o = largest.getCurrentOperation();
		String bf = o.batchFamily;

		Batch b = new Batch(largest.getShop());

		if (WorkStation.BATCH_INCOMPATIBLE.equals(o.batchFamily)
				|| o.maxBatchSize == 1 || largest.isFuture()) {
			b.addToBatch(largest);
			return b;
		}
		
		
		for (int i = 0; i < resArray.length; i++)
		{
			Job j = resArray[i];
			if(j.getCurrentOperation().batchFamily.equals(bf)&& b.numJobsInBatch()<o.maxBatchSize) {
				b.addToBatch(j);				
			} 
		}
		
//		double waitForFuture=0;
//		int leftSpace=o.maxBatchSize-b.numJobsInBatch();
//		if(b.numJobsInBatch()<o.maxBatchSize) {
//			for (int i = 0; i < resArray.length; i++)
//			{
//				Job j = resArray[i];
//				if(j.getCurrentOperation().batchFamily.equals(bf)&& b.numJobsInBatch()<o.maxBatchSize) {
//					if(j.isFuture()) {						
//						double temp=j.getArriveTime()-j.getShop().simTime();
//						if(waitForFuture<temp) waitForFuture=temp;
//						leftSpace--;
//						if(leftSpace==0) break;
//					}				
//				} 
//			}
//		}

 
		assert b.numJobsInBatch() >= 1;
//		return b;
		int minSize = (int) Math.ceil(0.0 * o.maxBatchSize);
		if (b.numJobsInBatch() >= minSize && b.numJobsInBatch() <= o.maxBatchSize)
			return b;
		else
			return null; 
 
	}

	private void defaultBatchForming() {
		final PriorityQueue<Job> q = getOwner().queue;

		orderedJobs = ensureCapacity(orderedJobs, q.size());
		q.getAllElementsInOrder(orderedJobs);
		int numJobs = q.size();

		for (int i = 0; i < numJobs; i++) {
			Job j = orderedJobs[i];
			if (j == null)
				continue;
			orderedJobs[i] = null;

			Operation o = j.getCurrentOperation();
			String bf = o.batchFamily;

			if (WorkStation.BATCH_INCOMPATIBLE.equals(o.batchFamily)
					|| o.maxBatchSize == 1 || j.isFuture()) {
				Batch batch = new Batch(j.getShop());
				batch.addToBatch(j);
				possibleBatches.add(batch);
				return;
			}

			List<Job> js = getOwner().getJobsByFamily().get(bf);
			int minSize = (int) Math.ceil(getMbsRel() * o.maxBatchSize);
			if (js.size() < minSize)
				continue;

			// fill batch with compatible jobs that already have arrived
			Batch batch = new Batch(j.getShop());

			batch.addToBatch(j);
			for (int n = i + 1; n < numJobs; n++) {
				Job j2 = orderedJobs[n];
				if (j2 == null)
					continue;

				Operation o2 = j2.getCurrentOperation();
				if (!bf.equals(o2.batchFamily))
					continue;

				assert o2.maxBatchSize == o.maxBatchSize;
				orderedJobs[n] = null;

				if (!j2.isFuture())
					batch.addToBatch(j2);

				if (batch.numJobsInBatch() == o2.maxBatchSize)
					break; // for n

			}

			if ((((double) batch.numJobsInBatch()) / o.maxBatchSize) >= getMbsRel()) {
				possibleBatches.add(batch);
				return;
			}

		}
	}

	private void formBatches(Map<String, List<Job>> jobsByFamily) {
		double maxRBS = 0.0d;

		for (List<Job> famJobs : jobsByFamily.values()) {
			Operation o = famJobs.get(0).getCurrentOperation();

			if (famJobs.size() < maxRBS * o.maxBatchSize)
				continue;

			Batch b = new Batch(getOwner().shop());
			// make batch as full as possible
			int i = 0;
			while (i < famJobs.size()
					&& b.numJobsInBatch() < o.maxBatchSize) {
				if (famJobs.get(i).getArriveTime()
						- famJobs.get(i).getShop().simTime() <= maxWait)
					b.addToBatch(famJobs.get(i));
				i++;
			}

			if ((maxRBS * o.maxBatchSize) <= b.numJobsInBatch() && 0 < b.numJobsInBatch()) 
			{
				if (maxRBS * o.maxBatchSize < b.numJobsInBatch()) {
					possibleBatches.clear();
					maxRBS = ((double) b.numJobsInBatch() / o.maxBatchSize);
				}
				possibleBatches.add(b);
			}
		}
	}
	
	private void handleTiesByFamilySize(Map<String, List<Job>> map) {
		 ArrayList<Batch> bs = possibleBatches.getAllElements(new ArrayList<Batch>(possibleBatches.size()));
		 possibleBatches.clear();
		
		 int bestFamSize = 0;
		
		 for (int i = 0, n = bs.size(); i < n; i++) {
			 Batch b = bs.get(i);
			 String bf = b.job(0).getCurrentOperation().batchFamily;
			
			 int famSize = map.get(bf).size();
			
			 if (famSize > bestFamSize) {
				 bestFamSize = famSize;
				 possibleBatches.clear();
				 possibleBatches.add(b);
			 } 
			 else if (famSize == bestFamSize) {
				 possibleBatches.add(b);
			 }
		 }
	 }
	
	private void handleTiesByUrgent() {
		Batch best = possibleBatches.get(0); 
		double mostUrge = (double)best.numJobsInBatch()*best.weightStat.sum()/Math.exp(best.slackStat.min());
		
		for (int i = 1, n = possibleBatches.size(); i < n; i++) {
			Batch b = possibleBatches.get(i);
			double urge = (double)b.numJobsInBatch()*b.weightStat.max()/b.slackStat.min();
			if (mostUrge < urge) {
				mostUrge = urge;
				best = b;
			}
		}

		possibleBatches.clear();
		possibleBatches.add(best);
	}
	
	private void handleTiesByBasePrio(int numJobs) {
		Batch best = possibleBatches.get(0);
		int bestIdx = indexOf(best.job(0), orderedJobs, numJobs);

		for (int i = 1, n = possibleBatches.size(); i < n; i++) {
			Batch b = possibleBatches.get(i);
			int idx = indexOf(b.job(0), orderedJobs, numJobs);
			if (idx < bestIdx) {
				bestIdx = idx;
				best = b;
			}
		}

		possibleBatches.clear();
		possibleBatches.add(best);
	}

	public double getMbsRel() {
		return mbsRel;
	}
	
	@Override
	public String getName() {
		return "GPB";
	}

	private double calcPTAvg() {
		final PriorityQueue<Job> q = getOwner().queue;
		assert q.size() > 0; 
		
		int numNonFutures = 0;
		double ptAvg = 0.0d;

		for (int i = 0, n = q.size(); i < n; i++) {
			Job j2 = q.get(i);
			assert !j2.isFuture();
			if (!j2.isFuture()) {
				ptAvg += j2.getCurrentOperation().procTime;
				numNonFutures++;
			}
		}
		return ptAvg / numNonFutures;
	}
	
	private double calcSetupAvg() {
		final PriorityQueue<Job> q = getOwner().queue;
		assert q.size() > 0;
		final double[][] setupMatrix = getOwner().getSetupMatrix();

		final int machineSetup = getOwner().currMachine.setupState;

		int numNonFutures = 0;
		double setupAvg = 0.0d;

		for (int i = 0, n = q.size(); i < n; i++) {
			Job j2 = q.get(i);
			assert !j2.isFuture();
			if (!j2.isFuture()) {
				setupAvg += setupMatrix[machineSetup][j2.getCurrentOperation().setupState];
				numNonFutures++;
			}
		}
		return setupAvg / numNonFutures;
	}
	
	private double setupTime(PrioRuleTarget j) {
		//assert j.getCurrMachine() == getOwner();
		final double[][] setupMatrix = j.getCurrMachine().getSetupMatrix();
		final int machineSetup = j.getCurrMachine().currMachine.setupState;

		return setupMatrix[machineSetup][j.getCurrentOperation().setupState];
	}
	
	public static final double ifte(final double cond, final double ifVal,
			final double elseVal) {
		if (cond >= 0.0d)
			return ifVal;
		else
			return elseVal;
	}

	protected double GetNormalizedValue(double curValue, double min, double max)
	{
		double result = 2*(curValue - min)/(max - min);
		
		return result;
	}
	
	public static final double add(final double v1, final double v2) {
		return v1 + v2;
	}

	public static final double mul(final double v1, final double v2) {
		return v1 * v2;
	}

	public static final double div(final double v1, final double v2) {
		if (v2 == 0.0)
			return 1.0;
		else
			return v1 / v2;
	}

	public static final double divProtected(final double v1, final double v2) {
		if (v2 == 0.0)
			return 1.0;
		else
			return v1 / v2;
	}

	public static final double sub(final double v1, final double v2) {
		return v1 - v2;
	}

	public static final double max(final double v1, final double v2) {
		return Math.max(v1, v2);
	}
}
