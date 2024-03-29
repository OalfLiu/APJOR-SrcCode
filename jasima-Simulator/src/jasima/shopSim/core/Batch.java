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
package jasima.shopSim.core;

import java.util.ArrayList;

import jasima.core.statistics.SummaryStat;

/**
 * A batch is a temporary collection of jobs to be processed together in a
 * single operation.
 * 
 * @author Torsten Hildebrandt
 * @version "$Id$"
 */
public class Batch extends PrioRuleTarget {

	public final JobShop shop;

	private static final String SEPARATOR = ",";

	private final ArrayList<Job> jobsInBatch;
	private int numFutures;
	private String name = null;

	public Operation op;
	
	//lingxuan liu 2019-03-28
	public SummaryStat procTimeStat=new SummaryStat();
	public SummaryStat qTimeStat=new SummaryStat();
	public SummaryStat weightStat=new SummaryStat();
	public SummaryStat slackStat=new SummaryStat();
	public SummaryStat fullnessStat = new SummaryStat(); 
	public SummaryStat prioStat = new SummaryStat(); 
	
	
	public Batch(JobShop shop) {
		super();
		this.shop = shop;
		jobsInBatch = new ArrayList<Job>();
		numFutures = 0;
	}

	public void addToBatch(Job job) {
		assert !jobsInBatch.contains(job);
		jobsInBatch.add(job);
		procTimeStat.value(job.getCurrentOperation().procTime);
		weightStat.value(job.getWeight());
//		slackStat.value(job.getCurrentOperationDueDate()- job.getShop().simTime() - job.getCurrentOperation().procTime);
		slackStat.value((job.getDueDate()-job.getShop().simTime())/job.remainingProcTime());
		qTimeStat.value(job.getShop().simTime() - job.getArriveTime());
		fullnessStat.value(1.0/(double)job.getCurrentOperation().maxBatchSize);
		prioStat.value(job.prio);
		if (job.isFuture())
			numFutures++;
	}

	public void clear() {
		jobsInBatch.clear();
		numFutures = 0;
	}

	public int numJobsInBatch() {
		return jobsInBatch.size();
	}
	
	public double fullnessOfBatch() {
		return fullnessStat.sum();
	}

	public Job job(int i) {
		return jobsInBatch.get(i);
	}

	public boolean isFuture() {
		return numFutures > 0;
	}

	public Batch createCopy() {
		Batch clone = new Batch(shop);
		for (Job j : jobsInBatch) {
			clone.addToBatch(j);
		}
		return clone;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Job j : jobsInBatch) {
			sb.append(j.toString()).append(SEPARATOR);
		}
		return (getName() != null ? getName() : op.batchFamily) + "_{"
				+ sb.substring(0, sb.length() - SEPARATOR.length()) + "}";
	}

	public double getEarliestODD() {
		double dueDate = Double.POSITIVE_INFINITY;
		for (Job j : jobsInBatch) {
			if (j.getCurrentOperationDueDate() < dueDate)
				dueDate = j.getCurrentOperationDueDate();
		}
		return dueDate;
	}

	public double getTotalWeight() {
		double totWeight = 0.0d;
		for (Job j : jobsInBatch) {
			totWeight += j.getWeight();
		}
		return totWeight;
	}

	@Override
	public double currProcTime() {
		return op.procTime;
	}

	@Override
	public double getArriveTime() {
		double res = Double.NEGATIVE_INFINITY;
		for (Job j : jobsInBatch) {
			if (j.getArriveTime() > res)
				res = j.getArriveTime();
		}
		assert res != Double.NEGATIVE_INFINITY;
		return res;
	}

	@Override
	public WorkStation getCurrMachine() {
		return op.machine;
	}

	@Override
	public Operation getCurrentOperation() {
		return op;
	}

	@Override
	public double getDueDate() {
		double res = jobsInBatch.get(0).getDueDate();
		for (int i = 1, n = jobsInBatch.size(); i < n; i++) {
			Job j = jobsInBatch.get(i);
			res += j.getDueDate();
		}
		return res / jobsInBatch.size();
	}

	/**
	 * Returns the minimum job number of all jobs contained in the batch.
	 */
	@Override
	public int getJobNum() {
		int res = jobsInBatch.get(0).getJobNum();
		for (int i = 1, n = jobsInBatch.size(); i < n; i++) {
			Job j = jobsInBatch.get(i);
			if (res < j.getJobNum())
				res = j.getJobNum();
		}
		return res;
	}

	@Override
	public double getCurrentOperationDueDate() {
		double res = jobsInBatch.get(0).getCurrentOperationDueDate();
		for (int i = 1, n = jobsInBatch.size(); i < n; i++) {
			Job j = jobsInBatch.get(i);
			res += j.getCurrentOperationDueDate();
		}
		return res / jobsInBatch.size();
	}

	@Override
	public Operation[] getOps() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getRelDate() {
		
		//llx 20190115 return the earlist release time of jobs in batch
		double res = Double.NEGATIVE_INFINITY;
		for (Job j : jobsInBatch) {
			if (j.getRelDate() > res)
				res = j.getRelDate();
		}
		assert res != Double.NEGATIVE_INFINITY;
		return res; 
		//throw new UnsupportedOperationException();
	}

	@Override
	public JobShop getShop() {
		return op.machine.shop();
	}

	@Override
	public int getTaskNumber() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the average weight of contained Jobs.
	 */
	@Override
	public double getWeight() {
		double res = jobsInBatch.get(0).getWeight();
		for (int i = 1, n = jobsInBatch.size(); i < n; i++) {
			Job j = jobsInBatch.get(i);
			res += j.getWeight();
		}
		return res / jobsInBatch.size();
	}

	@Override
	public int numOps() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int numOpsLeft() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double procSum() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the average remaining processing time of contained Jobs.
	 */
	@Override
	public double remainingProcTime() {
		double res = jobsInBatch.get(0).remainingProcTime();
		for (int i = 1, n = jobsInBatch.size(); i < n; i++) {
			Job j = jobsInBatch.get(i);
			res += j.remainingProcTime();
		}
		return res / jobsInBatch.size();
	}

	@Override
	public boolean isBatch() {
		return true;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
