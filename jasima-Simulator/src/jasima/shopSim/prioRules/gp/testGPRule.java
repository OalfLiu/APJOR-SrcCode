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
package jasima.shopSim.prioRules.gp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ec.CDJSP.DoubleData;
import ec.gp.GPNode;
import jasima.core.statistics.SummaryStat;
import jasima.shopSim.core.Batch;
import jasima.shopSim.core.Job;
import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.core.PriorityQueue;
import jasima.shopSim.prioRules.basic.SLK;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQPlusNPT;
import jasima.shopSim.prioRules.upDownStream.XWINQ;

/**
 * A rule from "Towards Improved Dispatching Rules for Complex Shop Floor
 * Scenarios—a Genetic Programming Approach", Hildebrandt, Heger, Scholz-Reiter,
 * GECCO 2010, doi:10.1145/1830483.1830530
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class testGPRule extends GPRuleBase {

	private static final long serialVersionUID = 8165075973248667950L;
	
	public GPNode rootNode;
	private Map<String, Integer> famSizes;
	private double sAvg;
	private double ptAvg;
	public boolean isTestStringGPRule=false;
	public boolean isSequencingRule=false;
	public boolean isBatchFormRule=false;
	public boolean isBatchSeqRule=false;
	
	@Override
	public void beforeCalc(PriorityQueue<?> q) {
		super.beforeCalc(q);

		//calcNumCompatible();
		sAvg = calcSetupAvg();
		ptAvg= calcPTAvg();
	}
	
	public testGPRule(GPNode node)
	{		
		rootNode = node;
	}
		


	@Override
	public double calcPrio(PrioRuleTarget j) {
	
		if (j.isFuture() && isSequencingRule) {
			return Double.NEGATIVE_INFINITY;
		}
		
		double PT = 0;
		double ST = 0;
		double TIQ = 0;
		double TIS = 0;
		double RPT = 0;
		double W = 0;
		double SLACK = 0;
		double ttd = 0; 
		double OpsLeft = 0;
		double WINQ=0;
		double NPT=0;
		//IJPE2013 supplemented terminals
		double RA =0; //reamining allowance
		double RAN=0; // reamining allowance of imminent operation	
		
		double JS_OSLACK=0; 
		double JS_SFIQ=0;
		double JS_APT=ptAvg;
		double JS_AST=sAvg;
		
		double BF_JFIQ=0;		
		double BF_BC = 0;
		double BF_FOB =0;		
		double BF_MPT=0;
		double BF_APT=0;  
		double BF_MQT=0;
		double BF_AQT=0;  
		double BF_MSlack=0;
		double BF_ASlack=0;  
		double BF_MW=0;
		double BF_AW=0; 
		double BF_JSz=0;
		double BF_JFSSz=0;
		double BF_JFMSz=0;
		
		List<Job> jobFamily = j.getCurrMachine().getJobsByFamily().get(j.getCurrentOperation().batchFamily);
		
		SummaryStat PTs=new SummaryStat();
		SummaryStat STs=new SummaryStat();
		SummaryStat QTs=new SummaryStat();
		SummaryStat TISs=new SummaryStat();
		SummaryStat RPTs=new SummaryStat();
		SummaryStat NPTs=new SummaryStat();
		SummaryStat Slacks=new SummaryStat();
		SummaryStat Ws=new SummaryStat();
		SummaryStat WINQs=new SummaryStat();
		SummaryStat OpsLefts=new SummaryStat();
		SummaryStat jobSizes=new SummaryStat();
		
		
		if(j.isBatch()) {
			for(int i=0;i<((Batch)j).numJobsInBatch();i++) {
				Job job=((Batch)j).job(i);
				PTs.value(job.currProcTime());
				STs.value(setupTime(job));
//				QTs.value(job.getShop().simTime() - job.getArriveTime());
//				TISs.value(job.getShop().simTime() - job.getArriveTime());
				RPTs.value(job.remainingProcTime());
				NPTs.value(PTPlusWINQPlusNPT.npt(job));
				Slacks.value(SLK.slack(job));
				Ws.value(job.getWeight());
//				WINQs.value(jasima.shopSim.prioRules.upDownStream.XWINQ.xwinq(job));
				OpsLefts.value(job.numOpsLeft());
				jobSizes.value(1.0/(double)job.getCurrentOperation().maxBatchSize);
			}
			
			PT = PTs.max();
			ST = STs.max();
			JS_AST = STs.mean();
			TIQ = QTs.sum();
			TIS = TISs.sum();
			RPT = RPTs.sum();
			W = Ws.max();
			SLACK = Slacks.min();
			ttd = j.getDueDate() - j.getShop().simTime(); 
			OpsLeft = OpsLefts.sum();
			WINQ=WINQs.sum();
			NPT=NPTs.sum();
			//IJPE2013 supplemented terminals
			RA =ttd/RPT; //reamining allowance
			RAN=ttd/NPT; // reamining allowance of imminent operation	
			
			JS_OSLACK=j.getCurrentOperationDueDate()- j.getShop().simTime() - j.getCurrentOperation().procTime;
			
			BF_JFIQ=jobFamily.size();		
			BF_BC = j.getCurrentOperation().maxBatchSize;
				
			BF_MPT=PTs.max();
			BF_APT=PTs.mean();  
			BF_MQT=QTs.max();
			BF_AQT=QTs.mean();  
			BF_MSlack=Slacks.min();
			BF_ASlack=Slacks.mean();  
			BF_MW=Ws.max();
			BF_AW=Ws.mean();
			
			BF_JSz=jobSizes.sum();
			BF_JFSSz=jobSizes.sum();
			BF_JFMSz=jobSizes.max();
			BF_FOB =jobSizes.sum();	
		} 
		else {
			PT = j.getCurrentOperation().procTime;
			ST = setupTime(j);
			TIQ = j.getShop().simTime() - j.getArriveTime();
			TIS = j.getShop().simTime() - j.getRelDate();
			RPT = j.remainingProcTime();
			W = j.getWeight();
			SLACK = SLK.slack(j);
			ttd = j.getDueDate() - j.getShop().simTime(); 
			OpsLeft = j.numOpsLeft();
			WINQ=jasima.shopSim.prioRules.upDownStream.XWINQ.xwinq(j);
			NPT=PTPlusWINQPlusNPT.npt(j);
			RA =ttd/RPT; //reamining allowance
			RAN=ttd/NPT; // reamining allowance of imminent operation	
			
			JS_OSLACK=j.getCurrentOperationDueDate()- j.getShop().simTime() - j.getCurrentOperation().procTime;		
			for(int i=0;i<getOwner().queue.size();i++)
				if(getOwner().queue.get(i).getJobType()==((Job)j).getJobType()) 
					JS_SFIQ++;
			
			BF_JFIQ=jobFamily.size();		
			BF_BC = j.getCurrentOperation().maxBatchSize;
					
			
			for(int i=0;i<jobFamily.size();i++) {
				Job job=jobFamily.get(i);
				PTs.value(job.currProcTime()); 
				Slacks.value(SLK.slack(job));
				Ws.value(job.getWeight());
				jobSizes.value(1.0/(double)job.getCurrentOperation().maxBatchSize);
			} 
			BF_MPT=PTs.max();
			BF_APT=PTs.mean(); 
			BF_MSlack=Slacks.min();
			BF_ASlack=Slacks.mean();  
			BF_MW=Ws.max();
			BF_AW=Ws.min();
			
			BF_JSz=1.0/BF_BC;
			BF_JFSSz=jobSizes.sum();
			BF_JFMSz=jobSizes.max();
			
			BF_FOB =BF_JFSSz;
		}  
		
		DoubleData input = new DoubleData();
		input.NPT = NPT;
		input.OpsLeft = OpsLeft;
		input.PT = PT;
		input.ST = ST;
		input.WINQ = WINQ;
		input.RPT = RPT;
		input.TIQ = TIQ;
		input.TIS = TIS;
		input.W = W;
		input.SLACK = SLACK;
		input.TD = ttd;
		input.JS_APT= JS_APT;
		input.JS_AST= JS_AST;
		input.JS_OSLACK=JS_OSLACK;
		input.JS_SFIQ=JS_SFIQ;
		
		input.BF_BC=BF_BC;	
		input.BF_JFIQ=BF_JFIQ;
		input.BF_FOB=BF_FOB;
		input.BF_MPT=BF_MPT;
		input.BF_APT=BF_APT;
		input.BF_MQT=BF_MQT;
		input.BF_AQT=BF_AQT;
		input.BF_MSlack=BF_MSlack;
		input.BF_ASlack=BF_ASlack;
		input.BF_MW=BF_MW;
		input.BF_AW=BF_AW;
		
		input.BF_JSz=BF_JSz;
		input.BF_JFSSz=BF_JFSSz;
		input.BF_JFMSz=BF_JFMSz;
		

		
		if(isTestStringGPRule && isSequencingRule)
		{
			//My Best Seq Rule
			return div(BF_FOB - 1, max(min(ST * JS_APT, 0 - 1) + (max(min(BF_FOB - div(max(PT, OpsLeft) * JS_OSLACK, div(JS_OSLACK - BF_FOB, min(W, PT))), min(ST * JS_APT, max(SLACK, PT)) + (JS_AST - ((BF_FOB + max(min(div(JS_OSLACK - BF_FOB, min(W, PT)), max(SLACK, PT)) + min(ifte(PT, JS_OSLACK, 10), max(PT, OpsLeft) * JS_OSLACK), min(min(BF_FOB, RPT), div(min(ST * JS_APT, max(SLACK, PT)), RPT)))) * max(SLACK, PT)))) * div(SLACK, PT), max(JS_AST + BF_FOB, JS_OSLACK)) - div(min(SLACK, JS_OSLACK) * (PT * ST), min(ifte(PT, JS_OSLACK, 10), max(PT, OpsLeft) * JS_OSLACK))), max(ifte(BF_FOB - 1, max((JS_OSLACK - BF_FOB) + ((BF_FOB + max(min(ST * JS_APT, max(SLACK, PT)) + max(SLACK, PT), min(min(BF_FOB, RPT), div(min(ST * JS_APT, max(SLACK, PT)), RPT)))) * max(SLACK, BF_FOB)), 0 - 1), BF_JFIQ) + max(W, RPT), BF_JFIQ) - 0));		
		}
		else if (isTestStringGPRule && isBatchSeqRule)
		{
			//My Best BF rule
			return ifte(ifte(1, SLACK, PT), min(min(min(RPT, BF_JFSSz), min(ifte(SLACK * JS_APT, OpsLeft + SLACK, ((W + 2) + (BF_JFIQ + ifte(ifte((JS_OSLACK + 2) + div(min(min(RPT, BF_JFSSz), ifte(RPT, BF_JFSSz, 10)), min(2 + BF_JFIQ, RPT)), min(JS_APT, 2) - ifte(2, 10, 2), ifte(1 - BF_JFSSz, 1 - PT, 2 + BF_JFIQ)), (JS_APT - 2) * (JS_APT + 0), 2 * 2))) + ifte(RPT, BF_JFSSz, BF_JFSSz)), ifte(SLACK, min(min(RPT, BF_JFSSz), 2), W + div(BF_JFSSz, SLACK)))), ifte(RPT, BF_JFSSz, 10)), W + ((W * BF_JFSSz) - ifte(PT, BF_JFSSz, JS_OSLACK))) * (div(BF_JFSSz, SLACK) + (2 + BF_JFIQ));

		}
		else if(isTestStringGPRule && isBatchFormRule)
		{			
			return ifte(div(BF_JFIQ, PT) + div(10, BF_JFSSz), min(min(1, BF_FOB), ifte(ifte(PT, JS_OSLACK, PT), BF_JFIQ - RPT, 1 * BF_FOB) + div(10, BF_JFSSz)), min(W + RPT, div(1, 10)));		
			
		}
		
		
		
		rootNode.evalSimple(input);

		return input.x;
	}
	
	private double setupTime(PrioRuleTarget j) {
		//assert j.getCurrMachine() == getOwner();
		final double[][] setupMatrix = j.getCurrMachine().getSetupMatrix();
		final int machineSetup = j.getCurrMachine().currMachine.setupState;

		return setupMatrix[machineSetup][j.getCurrentOperation().setupState];
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
//			assert !j2.isFuture();
			if (!j2.isFuture() && isSequencingRule) {
				setupAvg += setupMatrix[machineSetup][j2.getCurrentOperation().setupState];
				numNonFutures++;
			}
			else if(j2.isFuture() && !isSequencingRule){
				setupAvg += setupMatrix[machineSetup][j2.getCurrentOperation().setupState];
				numNonFutures++;
			}
			
		}
		return setupAvg / numNonFutures;
	}
	
	private double calcPTAvg() {
		final PriorityQueue<Job> q = getOwner().queue;
		assert q.size() > 0; 
		
		int numNonFutures = 0;
		double ptAvg = 0.0d;

		for (int i = 0, n = q.size(); i < n; i++) {
			Job j2 = q.get(i);
//			assert !j2.isFuture();
			if (!j2.isFuture() && isSequencingRule) {
				ptAvg += j2.getCurrentOperation().procTime;
				numNonFutures++;
			}
			else if(j2.isFuture() && !isSequencingRule) {
				ptAvg += j2.getCurrentOperation().procTime;
				numNonFutures++;
			}
		}
		return ptAvg / numNonFutures;
	}
	
	private void calcNumCompatible() {
		famSizes = new HashMap<String, Integer>();

		PriorityQueue<Job> q = getOwner().queue;
		for (int i = 0, n = q.size(); i < n; i++) {
			Job j = q.get(i);

			final String family = j.getCurrentOperation().batchFamily;
			Integer k = famSizes.get(family);
			if (k == null || "BATCH_INCOMPATIBLE".equals(family))
				k = 0;
			famSizes.put(family, k.intValue() + 1);
		}
	}

}
