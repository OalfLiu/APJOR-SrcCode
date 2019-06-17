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

import static jasima.shopSim.util.BasicJobStatCollector.put;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.Simulation.SimEvent;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.Util;
import jasima.core.util.Util.TypeOfObjective;
import jasima.core.util.observer.NotifierListener;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.JobShop;
import jasima.shopSim.core.PR;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.WorkStation;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment;
import jasima.shopSim.prioRules.basic.EDD;
import jasima.shopSim.prioRules.basic.FASFS;
import jasima.shopSim.prioRules.basic.SPT;
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQPlusNPT;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQPlusSlack;
import jasima.shopSim.prioRules.weighted.WMOD;

import java.util.Map;

/**
 * Collects a variety of job statistics that are not produced by the
 * {@link BasicJobStatCollector}.
 * 
 * <ul>
 * <li>lateness
 * <li>noProcTime (flowtime minus the sum of all processing times. It therefore
 * only measures reducible components of the flowtime, i.e., waiting and setup
 * times.)
 * <li>weightedFlowtime
 * <li>weightedTardiness
 * <li>conditionalTardiness
 * <li>weightedConditionalTardiness
 * <li>weightedTardinessWithWIP
 * <li>numTardyWeighted
 * </ul>
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 * @see BasicJobStatCollector
 */
public class DeviationExtendedJobStatCollector extends ShopListenerBase {

	private static final long serialVersionUID = 5946876977646917920L;

	private SummaryStat lateness;
	private SummaryStat noProcTime;
	private SummaryStat weightedFlowtime;
	private SummaryStat weightedTardiness;
	private SummaryStat conditionalTardiness;
	private SummaryStat weightedConditionalTardiness;
	private SummaryStat weightedTardinessWithWIP;
	private double numTardyWeighted;
	
	private SummaryStat weightedTardinessDev;
	private double referenceFitness;
	
	public DynamicShopExperiment e = new DynamicShopExperiment();
	
	private void runReferenceExperiment()
	{
		NotifierListener<Simulation, SimEvent>[] l = e.getShopListener();
		assert l.length == 1 && l[0] instanceof BasicJobStatCollector;
		e.setShopListener(null);
		
		
		ExtendedJobStatCollector exJobStatCollector = new ExtendedJobStatCollector();
		exJobStatCollector.setInitialPeriod(e.getWarpupPeriod());
		exJobStatCollector.setIgnoreFirst(e.getWarpupPeriod());		
	
		e.addShopListener(exJobStatCollector);				
		
		PR sr = new PTPlusWINQPlusSlack();
		
//		if(e.getTargetObjective() == 14 && e.typeOfObjective == TypeOfObjective.sum)
//			sr = new WMOD();
		
		PR sr2 = new IgnoreFutureJobs(sr);
		PR sr3 = new FASFS();
		sr2.setTieBreaker(sr3);
		e.setSequencingRule(sr2);
	
		e.runExperiment();	
		
		String obj = Util.getSingleRunObjective(e.getTargetObjective());
		
		Map<String, Object> result = e.getResults();
		
		Object objValue = result.get(obj);
		
		if(objValue instanceof SummaryStat)
		{
			SummaryStat stat = (SummaryStat) objValue;
			
			//Be careful, we limit our result to totalWeightedTardiness, so we use sum() to get result
			//Need to be extended to general form based on objectives
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
	}

	@Override
	protected void init(Simulation sim) {
		noProcTime = new SummaryStat("noProcTime");
		lateness = new SummaryStat("lateness");
		weightedFlowtime = new SummaryStat("weightedFlowtimes");
		weightedTardiness = new SummaryStat("weightedTardiness");
		conditionalTardiness = new SummaryStat("conditionalTardiness");
		weightedConditionalTardiness = new SummaryStat(
				"weightedConditionalTardiness");
		
		weightedTardinessDev = new SummaryStat("weightedTardinessDev");
		numTardyWeighted = 0.0;
		
		this.runReferenceExperiment();
	}

	@Override
	protected void done(Simulation sim) {
		weightedTardinessWithWIP = new SummaryStat(weightedTardiness);
		weightedTardinessWithWIP.setName("weightedTardinessWithWIP");

		JobShop shop = (JobShop) sim;
		for (WorkStation m : shop.machines) {
			for (int i = 0, n = m.queue.size(); i < n; i++) {
				storeWIPJob(m.queue.get(i));
			}
			for (int i = 0; i < m.numInGroup(); i++) {
				PrioRuleTarget j = m.getProcessedJob(i);
				if (j != null)
					storeWIPJob(j);
			}
		}
	}

	/**
	 * Updates statistics after simulation ended with data from a job that is
	 * still processed on the shop floor.
	 */
	protected void storeWIPJob(PrioRuleTarget job) {
		for (int i = 0; i < job.numJobsInBatch(); i++) {
			Job j = job.job(i);
			if (j.isFuture())
				continue;

			double tard = Math.max(
					j.getShop().simTime() - j.getCurrentOperationDueDate(), 0);
			weightedTardinessWithWIP.value(j.getWeight() * tard);
		}
	}

	@Override
	protected void jobReleased(JobShop shop, Job j) {

	}

	@Override
	protected void jobFinished(JobShop shop, Job j) {
		if (!shouldCollect(j))
			return;

		double ft = shop.simTime() - j.getRelDate();
		weightedFlowtime.value(j.getWeight() * ft);

		noProcTime.value(ft - j.procSum());

		double late = shop.simTime() - j.getDueDate();
		lateness.value(late);

		double tard = Math.max(late, 0);
		double wTard = j.getWeight() * tard;
		weightedTardiness.value(wTard);

		if (tard > 0.0) {
			conditionalTardiness.value(tard);
			weightedConditionalTardiness.value(wTard);
			numTardyWeighted += j.getWeight();
		}
	}

	@Override
	public void produceResults(Simulation sim, Map<String, Object> res) {
		
		double twtSum = Util.getValueByTypeofObjective(e.typeOfObjective, weightedTardiness);
		double twtDev = (twtSum - this.referenceFitness) / this.referenceFitness;
		
		weightedTardinessDev.value(twtDev); 
		
		put(res, noProcTime);
		put(res, weightedFlowtime);
		put(res, lateness);
		put(res, weightedTardiness);
		put(res, weightedTardinessWithWIP);
		put(res, conditionalTardiness);
		put(res, weightedConditionalTardiness);

		res.put("weightedNumTardy", numTardyWeighted);
		res.put("weightedTardinessDev", weightedTardinessDev);
	}

	@Override
	public String toString() {
		return "ExtendedJobStatCollector";
	}

}
