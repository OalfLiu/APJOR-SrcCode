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
package jasima.shopSim.util;

import jasima.core.experiment.MultipleReplicationExperiment;
import jasima.core.random.continuous.DblConst;
import jasima.core.random.discrete.IntEmpirical;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.Simulation.SimEvent;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.Util;
import jasima.core.util.Util.TypeOfObjective;
import jasima.core.util.observer.NotifierListener;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.JobShop;
import jasima.shopSim.core.PR;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment.Scenario;
import jasima.shopSim.prioRules.basic.EDD;
import jasima.shopSim.prioRules.basic.FASFS;
import jasima.shopSim.prioRules.basic.SPT;
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQPlusNPT;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQPlusSlack;

import java.util.Map;

/**
 * Collects a variety of job statistics: cMax (completion time of last job
 * finished), percentage tardy, number of tardy jobs, flowtime, tardiness. For
 * additional kpi's see {@link ExtendedJobStatCollector}.
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 * @see ExtendedJobStatCollector
 */
public class DeviationJobStatCollector extends ShopListenerBase {

	private static final long serialVersionUID = -4011992602302111428L;
	
	public long initialSeed;

	private SummaryStat flowtime;
	private SummaryStat tardiness;
	private SummaryStat flowtimeDev;
	private SummaryStat numTardyDev;
	private int numTardy;
	private int numFinished;
	private double cMax;
	public int numOfJobs = 2500;
	public DynamicShopExperiment e = new DynamicShopExperiment(); 

	private double referenceFitness;
	
	private void runReferenceExperiment()
	{
		//DynamicShopExperiment e = new DynamicShopExperiment(); 		
		
		// remove default BasicJobStatCollector
		NotifierListener<Simulation, SimEvent>[] l = e.getShopListener();
		assert l.length == 1 && l[0] instanceof BasicJobStatCollector;
		e.setShopListener(null);
		
		
		BasicJobStatCollector basicJobStatCollector = new BasicJobStatCollector();
		basicJobStatCollector.setInitialPeriod(e.getWarpupPeriod());
		basicJobStatCollector.setIgnoreFirst(e.getWarpupPeriod());		
	
		e.addShopListener(basicJobStatCollector);				
		
		PR sr = new PTPlusWINQPlusNPT();
		
		if(e.getTargetObjective() == 5 || e.getTargetObjective() == 15)
			sr = new SPT();
		
		if(e.getTargetObjective() == 11 && e.typeOfObjective == TypeOfObjective.max)
			sr = new PTPlusWINQPlusSlack();
		
		PR sr2 = new IgnoreFutureJobs(sr);
		PR sr3 = new FASFS();
		sr2.setTieBreaker(sr3);
		e.setSequencingRule(sr2);
		//e.setSequencingRule(sr);
		//e.setInitialSeed(this.initialSeed);
	
		e.runExperiment();	
		
		String obj = Util.getSingleRunObjective(e.getTargetObjective());
		
		Map<String, Object> result = e.getResults();
		
		Object objValue = result.get(obj);
		
		if(objValue instanceof SummaryStat)
		{
			SummaryStat stat = (SummaryStat) objValue;
			referenceFitness = Util.getValueByTypeofObjective(e.typeOfObjective, stat);
		}
		else
		{			
			if(objValue instanceof Integer)
			{
				referenceFitness = (int) objValue;
			}
			else
			{
				referenceFitness = (Double) objValue;
			}
			
			
		}
		
//		SummaryStat stat = (SummaryStat)e.getResults().get(obj);
//		referenceFitness = stat.mean();
	}
	
	@Override
	protected void init(Simulation sim) {
		flowtime = new SummaryStat("flowtime");
		tardiness = new SummaryStat("tardiness");
		flowtimeDev = new SummaryStat("flowtimeDev");
		numTardyDev = new SummaryStat("numTardyDev");
		numTardy = 0;
		numFinished = 0;
		cMax = 0.0;
		
		this.runReferenceExperiment();
	}

	@Override
	protected void jobFinished(JobShop shop, Job j) {
		if (!shouldCollect(j))
			return;

		cMax = shop.simTime();

		double ft = shop.simTime() - j.getRelDate();
		flowtime.value(ft);

		double late = shop.simTime() - j.getDueDate();
		double tard = Math.max(late, 0);
		tardiness.value(tard);

		if (tard > 0.0) {
			numTardy++;
		}

		numFinished++;
	}

	@Override
	public void produceResults(Simulation sim, Map<String, Object> res) {		
	
		double flowtimeObj = Util.getValueByTypeofObjective(e.typeOfObjective, flowtime);
		double dev = (flowtimeObj - this.referenceFitness) / this.referenceFitness;				
		double numTardydev = (this.numTardy - this.referenceFitness) / this.referenceFitness;		
		
		flowtimeDev.value(dev); 
		numTardyDev.value(numTardydev);
		
		put(res, flowtime);
		put(res, tardiness);
		put(res, flowtimeDev);
		put(res, numTardyDev);

		res.put("tardPercentage", ((double) numTardy) / numFinished);
		res.put("numTardy", numTardy);

		res.put("cMax", cMax);
	}

	public static void put(Map<String, Object> res, SummaryStat ss) {
		res.put(ss.getName(), ss);
	}

	@Override
	public String toString() {
		return "BasicJobStatCollector";
	}

}
