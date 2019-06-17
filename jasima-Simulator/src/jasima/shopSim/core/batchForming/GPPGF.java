/*******************************************************************************
A67 u * This file is part of jasima, v1.3, the Java simulator for manufacturing and 
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

import jasima.shopSim.core.Batch;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.Operation;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PriorityQueue;
import jasima.shopSim.core.WorkStation;
import jasima.shopSim.prioRules.basic.SPT;
import jasima.shopSim.prioRules.gp.testGPRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ec.CDJSP.DoubleData;
import ec.gp.GPNode;

/**
 * <p>
 * This class creates a single batch per family according to the used sequencing
 * rule. Later a machine's sequencingRule (optionally batchSequencingRule) is
 * used to choose one of them to process.
 * </p>
 * <p>
 * If there are more than maxBatchSize jobs for a family, jobs are sequenced
 * using the sequencingRule first and the best maxBatchSize jobs of the family
 * are used to form the batch.
 * 
 * @author Torsten Hildebrandt, 2010-10-25
 * @version 
 *          "$Id$"
 */
public class GPPGF extends BatchForming {

	private static final long serialVersionUID = 4249713710542519941L;

	private double maxWait;
	private PR batchSeqDR;
	public GPNode rootNode;
	
	public GPPGF(PR batchSeqDR) {
		super();  
		this.batchSeqDR=batchSeqDR;
		rootNode = ((testGPRule)batchSeqDR).rootNode;
	}
	
	public GPPGF() { 
	}
	
	@Override
	public void formBatches() {
		final PriorityQueue<Job> q = getOwner().queue;

		orderedJobs = ensureCapacity(orderedJobs, q.size());
		
		//when training GP use this
		q.setSequencingRule(this.getOwner().getBatchSequencingRule());		
		
		q.getAllElementsInOrder(orderedJobs);
		int numJobs = q.size();
		
		// split jobs of each family
		Map<String, List<Job>> jobsByFamily = splitFamilies(orderedJobs,
				numJobs);
		
		List<Batch> templist=new ArrayList<Batch>();
		
		for (int i = 0; i < numJobs; i++) {
			Job j = orderedJobs[i];
			
			if (j == null)
				continue;
			j.prio=numJobs-i;
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

				orderedJobs[n] = null;

				if (!j2.isFuture() && batch.fullnessStat.sum()+1.0/(double)j2.getCurrentOperation().maxBatchSize<=1.0)
					batch.addToBatch(j2);
//				else if(!j2.isFuture()) {
//					double residual = 1.0-batch.fullnessStat.sum();
//					int numSplitOut=(int)Math.floor(residual*300.0);
//					if(numSplitOut>0) //can split out at least one part
//					{
//						double oldBS=j2.getCurrentOperation().maxBatchSize;
//						int newBS=(int)Math.ceil(oldBS/(1-residual));
//						j2.getCurrentOperation().maxBatchSize=newBS;
//						batch.fullnessStat.value(residual);
//						batch.procTimeStat.value(j2.getCurrentOperation().procTime);
//					}
//				}
					
			}


			templist.add(batch);
		}
		
		
		int maxPrioIdx=0;
		double maxBatchSumPrio=Double.NEGATIVE_INFINITY;
		int counter=0;
		for(Batch b:templist) {
			counter++;
			if(b.prioStat.sum()>maxBatchSumPrio) {
				maxBatchSumPrio=b.prioStat.sum();
				maxPrioIdx=counter;
			}
		}
 
		possibleBatches.clear();
		possibleBatches.add(templist.get(maxPrioIdx));
		
	}
	
	private void initBatchData(Batch b) {
 
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
		
		if(b.numJobsInBatch()<1)
		{
			b.getJobNum();
			b.job(maxPTJobNum);
		}
		
		Job job = b.job(maxPTJobNum);
		Operation opJ = job.getCurrentOperation();

		Operation op = new Operation();
		op=opJ;
		op.machine = opJ.machine;
		op.batchFamily = opJ.batchFamily;
		op.setupState = opJ.setupState;
		op.procTime = opJ.procTime;
		op.maxBatchSize = opJ.maxBatchSize;	
		
		
		b.op = op;
	}

	private void formFamilyBatches(List<Job> famJobs) {
		assert famJobs.size() > 0;

		Job j = famJobs.get(0);
		Operation o = j.getCurrentOperation();
		assert WorkStation.BATCH_INCOMPATIBLE.equals(o.batchFamily) ? o.maxBatchSize == 1
				: true;

		// make batches as full as possible
		Batch b = new Batch(getOwner().shop());
		Batch b2 = new Batch(getOwner().shop());
		for (int i = 0, n = famJobs.size(); i < n; i++) {
			Job job = famJobs.get(i);
			if (b.numJobsInBatch() < o.maxBatchSize)
				b.addToBatch(job);
			if (!job.isFuture())
				b2.addToBatch(job);
			if (b2.numJobsInBatch() == o.maxBatchSize)
				break;
		}
		possibleBatches.add(b);
		
		if(b.numJobsInBatch()==0)
		{
			b.numJobsInBatch();
		}
		
		if (b.isFuture() && b2.numJobsInBatch() > 0)
			possibleBatches.add(b2);		
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
	
	private void handleTiesByFullness() {
		 ArrayList<Batch> bs = possibleBatches.getAllElements(new ArrayList<Batch>(possibleBatches.size()));
		 possibleBatches.clear();
		
		 double bestFOB = 0;
		
		 for (int i = 0, n = bs.size(); i < n; i++) {
			 Batch b =bs.get(i);
			 double fob = (double)b.numJobsInBatch()/(double)b.op.maxBatchSize;
			
			 if (fob > bestFOB) {
				 bestFOB = fob;
				 possibleBatches.clear();
				 possibleBatches.add(b);
			 } 
			 else if (bestFOB == fob) {
				 possibleBatches.add(b);
			 }
		 } 
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
	
	public int pickBatch(List<Batch> templist) {
		int bestBatchIdx=-1;
		double maxtemp=Double.NEGATIVE_INFINITY;
		int i=0;
		for(int counter =0; counter<templist.size();counter++)
		{
			Batch bat=templist.get(counter);
			DoubleData input=new DoubleData();
			int idx= maxJob(bat);
			Job job=bat.job(idx);
			Operation curOp=job.getCurrentOperation();
			
			List<Job> jobFamily = job.getCurrMachine().getJobsByFamily().get(job.getCurrentOperation().batchFamily);	
			double BF_JFIQ=jobFamily.size();
			double BF_BS=Double.valueOf(curOp.maxBatchSize-bat.numJobsInBatch());
			double BF_MPT=bat.numJobsInBatch()<=0?0.0:bat.procTimeStat.max();
			double BF_APT=bat.numJobsInBatch()<=0?0.0:bat.procTimeStat.mean();  
			double BF_FOB =(double)BF_JFIQ/(double)curOp.maxBatchSize; 
			double BF_MSlack=bat.numJobsInBatch()<=0?0.0:bat.slackStat.max();
			double BF_ASlack=bat.numJobsInBatch()<=0?0.0:bat.slackStat.mean();
			double BF_MQT=bat.numJobsInBatch()<=0?0.0:bat.qTimeStat.max();
			double BF_AQT=bat.numJobsInBatch()<=0?0.0:bat.qTimeStat.mean();
			double BF_MW=bat.numJobsInBatch()<=0?0.0:bat.weightStat.max();
			double BF_AW=bat.numJobsInBatch()<=0?0.0:bat.weightStat.mean();
			
			input.BF_FOB=BF_FOB;
			input.BF_BS=BF_BS;
			input.BF_MPT=BF_MPT;
			input.BF_APT=BF_APT;
			input.BF_MSlack=BF_MSlack;
			input.BF_ASlack=BF_ASlack;
			input.BF_MQT=BF_MQT;
			input.BF_AQT=BF_AQT;
			input.BF_MW=BF_MW;
			input.BF_AW=BF_AW;
			
			
			if(((testGPRule)batchSeqDR).isTestStringGPRule) 
				input.x=min(ifte((BF_MW + ifte(BF_FOB, 0, BF_BS)) * (BF_MPT - 1), ifte(ifte(1, BF_MSlack, BF_MW), BF_MQT - BF_MSlack, div(BF_MSlack, BF_MSlack)), max(max(1, BF_BS), max(BF_AQT, BF_BS))), div(div(0, BF_BS), 0));
			else rootNode.evalSimple(input);
			
			if(input.x>maxtemp)
			{
				maxtemp=input.x;
				bestBatchIdx=i;
			}
			i++; 
		}
		
		return bestBatchIdx;
		
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
	
	
	@Override
	public String getName() {
		return "BOF";
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
	
	public static final double min(final double v1, final double v2) {
		return Math.min(v1, v2);
	}
}
