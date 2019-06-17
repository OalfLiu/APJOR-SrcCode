package jasima.shopSim.prioRules;

import jasima.shopSim.core.PR;
import jasima.shopSim.core.batchForming.BatchForming;
import jasima.shopSim.core.batchForming.BestOfFamilyBatching;
import jasima.shopSim.core.batchForming.GPPGF;
import jasima.shopSim.core.batchForming.HighestJobBatchingMBS;
import jasima.shopSim.core.batchForming.MBSwithJobSize;
import jasima.shopSim.core.batchForming.MCBwithJobSize;
import jasima.shopSim.core.batchForming.MostCompleteBatch;
import jasima.shopSim.prioRules.basic.CR;
import jasima.shopSim.prioRules.basic.EDD;
import jasima.shopSim.prioRules.basic.ERD;
import jasima.shopSim.prioRules.basic.FCFS;
import jasima.shopSim.prioRules.basic.LRM;
import jasima.shopSim.prioRules.basic.MDD;
import jasima.shopSim.prioRules.basic.MOD;
import jasima.shopSim.prioRules.basic.ODD;
import jasima.shopSim.prioRules.basic.SLK;
import jasima.shopSim.prioRules.basic.SPT;
import jasima.shopSim.prioRules.basic.TieBreakerFASFS;
import jasima.shopSim.prioRules.batch.BATCS;
import jasima.shopSim.prioRules.gp.testGPRule;
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;
import jasima.shopSim.prioRules.setup.ATCS;
import jasima.shopSim.prioRules.setup.SST;
import jasima.shopSim.prioRules.weighted.LW;
import jasima.shopSim.prioRules.weighted.WMDD;
import jasima.shopSim.prioRules.weighted.WMOD;
import jasima.shopSim.prioRules.weighted.WSPT;
import util.Wintersim2010GPRules.GPRuleSize199;

public class PRDecryptor {
	
	public PRDecryptor()
	{
		twtSets=twtStr.split("\r\n");
		flowTimeSets=flowTimeStr.split("\r\n");
		tpSets=tardPercent.split("\r\n");
	}
	
	 
	public PR decryptSeqPR(double index)
	{
		switch((int)index)
		{
		case 0:  
			return createATC(1.5,0.5);
		case 1: 
			return createATC(1.5,1.0);
		case 2: 
			return createATC(1.5,1.5);
		case 3: 
			return createATC(2.0,0.5);
		case 4: 
			return createATC(2.0,1.0);
		case 5: 
			return createATC(2.0,1.5);
		case 6: 
			return createATC(2.5,0.5);
		case 7: 
			return createATC(2.5,1.0);
		case 8: 
			return createATC(2.5,1.5);
		case 9: 
			return createATC(3.0,1.0); 
		case 10: 
			return createPRStack(new WMDD(),true);
		case 11: 
			return createPRStack(new WMOD(),true);
		case 12: 
			return createPRStack(new WSPT(),true);
		case 13: 
			PR lw1=new LW();
			lw1.setTieBreaker(new CR());
			return createPRStack(lw1,true);
		case 14: 
			PR lw0=new LW();
			lw0.setTieBreaker(new FCFS());
			return createPRStack(lw0,true);
		case 15: 
			return createPRStack(new SLK(),true);
		case 16: 
			return createPRStack(new EDD(),true);
		case 17: 
			return createPRStack(new ODD(),true);
		case 18: 
			return createPRStack(new ERD(),true);
		case 19: 
			return createPRStack(new LRM(),true);
		case 20: 
			return createPRStack(new WSPT(),false);
		case 21: 
			PR lw2=new LW();
			lw2.setTieBreaker(new CR());
			return createPRStack(lw2,false);
		case 22: 
			PR lw3=new LW();
			lw3.setTieBreaker(new FCFS());
			return createPRStack(lw3,false);
		case 23: 
			return createPRStack(new LRM(),false);
		case 24: 
			testGPRule tgp =new testGPRule(null);
			tgp.isTestStringGPRule=true;
			tgp.isSequencingRule=true;
			tgp.preTestObj=2945.0;
			return tgp;
		 
		default:  
			return null;
		} 
	}
	
	public PR decryptSeqPR0309(double index)
	{
		switch((int)index)
		{
		case 0:  
			return createATC(0.5,1.0);
		case 1:  
			return createATC(1.0,1.0);
		case 2:  
			return createATC(1.5,1.0);
		case 3:  
			return createATC(2.0,1.0);
		case 4:  
			return createATC(2.5,1.0);
		case 5:  
			return createATC(3.0,1.0);
		case 6:  
			return createATC(0.5,1.5);
		case 7:  
			return createATC(1.0,1.5);
		case 8:  
			return createATC(1.5,1.5);
		case 9:  
			return createATC(2.0,1.5);
		case 10:  
			return createATC(2.5,1.5);
		case 11:  
			return createATC(3.0,1.5);
			
			
		case 12:  
			return createBATC(0.5,1.0);
		case 13:  
			return createBATC(1.0,1.0);
		case 14:  
			return createBATC(1.5,1.0);
		case 15:  
			return createBATC(2.0,1.0);
		case 16:  
			return createBATC(2.5,1.0);
		case 17:  
			return createBATC(3.0,1.0);
		case 18:  
			return createBATC(0.5,1.5);
		case 19:  
			return createBATC(1.0,1.5);
		case 20:  
			return createBATC(1.5,1.5);
		case 21:  
			return createBATC(2.0,1.5);
		case 22:  
			return createBATC(2.5,1.5);
		case 23:  
			return createBATC(3.0,1.5);
		 
			
		case 24: 
			PR lw1=new LW();
			lw1.setTieBreaker(new CR());
			return createPRStack(lw1,true);
		case 25: 
			PR lw0=new LW();
			lw0.setTieBreaker(new FCFS());
			return createPRStack(lw0,true);
		case 26: 
			return createPRStack(new WSPT(),true);
		case 27: 
			return createPRStack(new WMDD(),true);
		case 28: 
			return createPRStack(new WMOD(),true);
		case 29: 
			return createPRStack(new CR(),true);
		case 30: 
			return createPRStack(new SPT(),true);
		case 31: 
			return createPRStack(new LRM(),true);
		case 32: 
			return createPRStack(new EDD(),true);
		case 33: 
			return createPRStack(new MDD(),true);
		case 34: 
			return createPRStack(new ODD(),true);
		case 35: 
			return createPRStack(new MOD(),true);
		case 36: 
			return createPRStack(new SLK(),true);
		 
			
		case 37: 
			PR lw3=new LW();
			lw3.setTieBreaker(new CR());
			return createPRStack(lw3,false);
		case 38: 
			PR lw4=new LW();
			lw4.setTieBreaker(new FCFS());
			return createPRStack(lw4,false);
		case 39: 
			return createPRStack(new WSPT(),false);
		case 40: 
			return createPRStack(new WMDD(),false);
		case 41: 
			return createPRStack(new WMOD(),false);
		case 42: 
			return createPRStack(new CR(),false);
		case 43: 
			return createPRStack(new SPT(),false);
		case 44: 
			return createPRStack(new LRM(),false);
		case 45: 
			return createPRStack(new EDD(),false);
		case 46: 
			return createPRStack(new MDD(),false);
		case 47: 
			return createPRStack(new ODD(),false);
		case 48: 
			return createPRStack(new MOD(),false);
		case 49: 
			return createPRStack(new SLK(),false);
		default:  
			return null;
		} 
	}
	
	public BatchForming decryptBatchFormPR(double index)
	{
		switch((int)index)
		{
		case 0: 
			MCBwithJobSize mcb = new MCBwithJobSize();
//			mcb.preTestObj=1.0;
			mcb.preTestObj=FindTWTByRuleName(mcb.getName(),1);
//			mcb.preTestStd=FindTWTByRuleName(mcb.getName(),2);
//			mcb.preTestFlowTime=FindFlowTimeByRuleName(mcb.getName(),1);
			return mcb;
		case 1: 
			return createMBS(1.0);
		case 2:  
			return createMBS(0.825);
		case 3:  
			return createMBS(0.75);
		case 4:  
			return createMBS(0.625);
//		case 5:  
//			BestOfFamilyBatching bof = new BestOfFamilyBatching();
//			bof.preTestObj=FindTWTByRuleName(bof.getName(),1);
//			bof.preTestStd=FindTWTByRuleName(bof.getName(),2);
//			return bof;
		case 5:  
			return createMBS(0.5);
		case 6:  
			return createMBS(0.375);
		case 7:  
			return createMBS(0.25);
		case 8:  
			return createMBS(0.125);
		case 9:  
			GPPGF gp = new GPPGF();
			gp.preTestObj=1255.0;
			return gp;
		default: return null;
		} 
	}
	
	private PR createATC(double k1,double k2)
	{
		PR ATCS1=new ATCS(k1,k2);
//		ATCS1.preTestObj=1.0;
		ATCS1.preTestObj=FindTWTByRuleName(ATCS1.getName(),1);
//		ATCS1.preTestStd=FindTWTByRuleName(ATCS1.getName(),2);
//		ATCS1.preTestFlowTime=FindFlowTimeByRuleName(ATCS1.getName(),1);
//		ATCS1.preTestTPTime=FindTPByRuleName(ATCS1.getName(),1);
		return ATCS1;
	}
	
	private PR createBATC(double k1,double k2)
	{
		PR BATCS1=new BATCS(k1,k2);
		BATCS1.preTestObj=1.0;
//		BATCS1.preTestObj=FindTWTByRuleName(BATCS1.getName(),1);
//		BATCS1.preTestStd=FindTWTByRuleName(BATCS1.getName(),2);
//		BATCS1.preTestFlowTime=FindFlowTimeByRuleName(BATCS1.getName(),1);
//		BATCS1.preTestTPTime=FindTPByRuleName(BATCS1.getName(),1);
		return BATCS1;
	}
	
	private BatchForming createMBS(double reslb)
	{
		MBSwithJobSize mbs =new MBSwithJobSize(reslb);
//		mbs.preTestObj=1.0;
		mbs.preTestObj=FindTWTByRuleName(mbs.getName(),1);
//		mbs.preTestStd=FindTWTByRuleName(mbs.getName(),2);
//		mbs.preTestFlowTime=FindFlowTimeByRuleName(mbs.getName(),1);
//		mbs.preTestTPTime=FindTPByRuleName(mbs.getName(),1);
		return mbs;
	}
	
	private PR createPRStack(PR pr, boolean setupAvoidance) {
		pr.setFinalTieBreaker(new TieBreakerFASFS());

		if (setupAvoidance) {
			PR ms = new SST();
			ms.setTieBreaker(pr);
			pr = ms;
		}
		
		PR rtnPR=new IgnoreFutureJobs(pr);
//		rtnPR.preTestObj=1.0;
		rtnPR.preTestObj=FindTWTByRuleName(rtnPR.getName(),1);
//		rtnPR.preTestStd=FindTWTByRuleName(rtnPR.getName(),2);
//		rtnPR.preTestFlowTime=FindFlowTimeByRuleName(rtnPR.getName(),1);
		return rtnPR;
	}
	
	private double FindTPByRuleName(String name,int valueIdx)
	{ 
		for(int i=0;i<twtSets.length;i++)
		{
			if(tpSets[i].contains(name)) return Double.valueOf((tpSets[i].split("\t"))[valueIdx]);			
		}
		return 12000.0; //if not found pre test values
	}
	 
	private double FindTWTByRuleName(String name,int valueIdx)
	{ 
		for(int i=0;i<twtSets.length;i++)
		{
			if(twtSets[i].contains(name)) return Double.valueOf((twtSets[i].split("\t"))[valueIdx]);			
		}
		return 20000.0; //if not found pre test values
	}
	
	private double FindFlowTimeByRuleName(String name,int valueIdx)
	{ 
		for(int i=0;i<twtSets.length;i++)
		{
			if(flowTimeSets[i].contains(name)) return Double.valueOf((flowTimeSets[i].split("\t"))[valueIdx]);			
		}
		return 12000.0; //if not found pre test values
	}
	
	private String[] tpSets; 
	private String tardPercent="IGF[LW[FCFS[TieBreakerFASFS]]]	0.21\r\n" + 
			"IGF[LW[CR[TieBreakerFASFS]]]	0.21\r\n" + 
			"IGF[WSPT[TieBreakerFASFS]]	0.20\r\n" + 
			"IGF[WMDD[TieBreakerFASFS]]	NaN\r\n" + 
			"IGF[WMOD[TieBreakerFASFS]]	NaN\r\n" + 
			"IGF[LRM[TieBreakerFASFS]]	0.17\r\n" + 
			"IGF[EDD[TieBreakerFASFS]]	NaN\r\n" + 
			"IGF[ERD[TieBreakerFASFS]]	NaN\r\n" + 
			"IGF[ODD[TieBreakerFASFS]]	NaN\r\n" + 
			"IGF[SLK[TieBreakerFASFS]]	NaN\r\n" + 
			"IGF[SST[LW[FCFS[TieBreakerFASFS]]]]	0.17\r\n" + 
			"IGF[SST[LW[CR[TieBreakerFASFS]]]]	0.17\r\n" + 
			"IGF[SST[WSPT[TieBreakerFASFS]]]	0.17\r\n" + 
			"IGF[SST[WMDD[TieBreakerFASFS]]]	0.16\r\n" + 
			"IGF[SST[WMOD[TieBreakerFASFS]]]	0.16\r\n" + 
			"IGF[SST[LRM[TieBreakerFASFS]]]	0.17\r\n" + 
			"IGF[SST[EDD[TieBreakerFASFS]]]	0.16\r\n" + 
			"IGF[SST[ERD[TieBreakerFASFS]]]	0.17\r\n" + 
			"IGF[SST[ODD[TieBreakerFASFS]]]	0.16\r\n" + 
			"IGF[SST[SLK[TieBreakerFASFS]]]	0.16\r\n" + 
			"BATCS(k1=1.0E-4;k2=1.0E-4)	0.15\r\n" + 
			"BATCS(k1=1.0E-4;k2=0.001)	0.21\r\n" + 
			"BATCS(k1=1.0E-4;k2=0.01)	0.23\r\n" + 
			"BATCS(k1=1.0E-4;k2=0.1)	0.23\r\n" + 
			"BATCS(k1=1.0E-4;k2=0.25)	0.24\r\n" + 
			"BATCS(k1=1.0E-4;k2=0.5)	0.24\r\n" + 
			"BATCS(k1=1.0E-4;k2=1.0)	0.27\r\n" + 
			"BATCS(k1=1.0E-4;k2=1.5)	0.29\r\n" + 
			"BATCS(k1=0.001;k2=1.0E-4)	0.15\r\n" + 
			"BATCS(k1=0.001;k2=0.001)	0.15\r\n" + 
			"BATCS(k1=0.001;k2=0.01)	0.21\r\n" + 
			"BATCS(k1=0.001;k2=0.1)	0.23\r\n" + 
			"BATCS(k1=0.001;k2=0.25)	0.24\r\n" + 
			"BATCS(k1=0.001;k2=0.5)	0.24\r\n" + 
			"BATCS(k1=0.001;k2=1.0)	0.27\r\n" + 
			"BATCS(k1=0.001;k2=1.5)	0.29\r\n" + 
			"BATCS(k1=0.01;k2=1.0E-4)	0.15\r\n" + 
			"BATCS(k1=0.01;k2=0.001)	0.15\r\n" + 
			"BATCS(k1=0.01;k2=0.01)	0.15\r\n" + 
			"BATCS(k1=0.01;k2=0.1)	0.21\r\n" + 
			"BATCS(k1=0.01;k2=0.25)	0.22\r\n" + 
			"BATCS(k1=0.01;k2=0.5)	0.23\r\n" + 
			"BATCS(k1=0.01;k2=1.0)	0.26\r\n" + 
			"BATCS(k1=0.01;k2=1.5)	0.29\r\n" + 
			"BATCS(k1=0.1;k2=1.0E-4)	0.16\r\n" + 
			"BATCS(k1=0.1;k2=0.001)	0.15\r\n" + 
			"BATCS(k1=0.1;k2=0.01)	0.15\r\n" + 
			"BATCS(k1=0.1;k2=0.1)	0.15\r\n" + 
			"BATCS(k1=0.1;k2=0.25)	0.16\r\n" + 
			"BATCS(k1=0.1;k2=0.5)	0.19\r\n" + 
			"BATCS(k1=0.1;k2=1.0)	0.23\r\n" + 
			"BATCS(k1=0.1;k2=1.5)	0.26\r\n" + 
			"BATCS(k1=0.5;k2=1.0E-4)	0.16\r\n" + 
			"BATCS(k1=0.5;k2=0.001)	0.16\r\n" + 
			"BATCS(k1=0.5;k2=0.01)	0.15\r\n" + 
			"BATCS(k1=0.5;k2=0.1)	0.15\r\n" + 
			"BATCS(k1=0.5;k2=0.25)	0.15\r\n" + 
			"BATCS(k1=0.5;k2=0.5)	0.15\r\n" + 
			"BATCS(k1=0.5;k2=1.0)	0.15\r\n" + 
			"BATCS(k1=0.5;k2=1.5)	0.18\r\n" + 
			"BATCS(k1=1.0;k2=1.0E-4)	0.16\r\n" + 
			"BATCS(k1=1.0;k2=0.001)	0.16\r\n" + 
			"BATCS(k1=1.0;k2=0.01)	0.15\r\n" + 
			"BATCS(k1=1.0;k2=0.1)	0.15\r\n" + 
			"BATCS(k1=1.0;k2=0.25)	0.15\r\n" + 
			"BATCS(k1=1.0;k2=0.5)	0.15\r\n" + 
			"BATCS(k1=1.0;k2=1.0)	0.15\r\n" + 
			"BATCS(k1=1.0;k2=1.5)	0.15\r\n" + 
			"BATCS(k1=1.5;k2=1.0E-4)	0.16\r\n" + 
			"BATCS(k1=1.5;k2=0.001)	0.16\r\n" + 
			"BATCS(k1=1.5;k2=0.01)	0.15\r\n" + 
			"BATCS(k1=1.5;k2=0.1)	0.15\r\n" + 
			"BATCS(k1=1.5;k2=0.25)	0.15\r\n" + 
			"BATCS(k1=1.5;k2=0.5)	0.15\r\n" + 
			"BATCS(k1=1.5;k2=1.0)	0.15\r\n" + 
			"BATCS(k1=1.5;k2=1.5)	0.15\r\n" + 
			"BATCS(k1=2.0;k2=1.0E-4)	0.16\r\n" + 
			"BATCS(k1=2.0;k2=0.001)	0.15\r\n" + 
			"BATCS(k1=2.0;k2=0.01)	0.15\r\n" + 
			"BATCS(k1=2.0;k2=0.1)	0.15\r\n" + 
			"BATCS(k1=2.0;k2=0.25)	0.15\r\n" + 
			"BATCS(k1=2.0;k2=0.5)	0.15\r\n" + 
			"BATCS(k1=2.0;k2=1.0)	0.15\r\n" + 
			"BATCS(k1=2.0;k2=1.5)	0.15\r\n" + 
			"BATCS(k1=2.5;k2=1.0E-4)	0.16\r\n" + 
			"BATCS(k1=2.5;k2=0.001)	0.16\r\n" + 
			"BATCS(k1=2.5;k2=0.01)	0.15\r\n" + 
			"BATCS(k1=2.5;k2=0.1)	0.15\r\n" + 
			"BATCS(k1=2.5;k2=0.25)	0.15\r\n" + 
			"BATCS(k1=2.5;k2=0.5)	0.15\r\n" + 
			"BATCS(k1=2.5;k2=1.0)	0.15\r\n" + 
			"BATCS(k1=2.5;k2=1.5)	0.15\r\n" + 
			"BATCS(k1=3.0;k2=1.0E-4)	0.16\r\n" + 
			"BATCS(k1=3.0;k2=0.001)	0.16\r\n" + 
			"BATCS(k1=3.0;k2=0.01)	0.15\r\n" + 
			"BATCS(k1=3.0;k2=0.1)	0.15\r\n" + 
			"BATCS(k1=3.0;k2=0.25)	0.15\r\n" + 
			"BATCS(k1=3.0;k2=0.5)	0.15\r\n" + 
			"BATCS(k1=3.0;k2=1.0)	0.15\r\n" + 
			"BATCS(k1=3.0;k2=1.5)	0.15\r\n" + 
			"BATCS(k1=3.5;k2=1.0E-4)	0.16\r\n" + 
			"BATCS(k1=3.5;k2=0.001)	0.16\r\n" + 
			"BATCS(k1=3.5;k2=0.01)	0.16\r\n" + 
			"BATCS(k1=3.5;k2=0.1)	0.15\r\n" + 
			"BATCS(k1=3.5;k2=0.25)	0.15\r\n" + 
			"BATCS(k1=3.5;k2=0.5)	0.15\r\n" + 
			"BATCS(k1=3.5;k2=1.0)	0.15\r\n" + 
			"BATCS(k1=3.5;k2=1.5)	0.15\r\n" + 
			"BATCS(k1=4.0;k2=1.0E-4)	0.16\r\n" + 
			"BATCS(k1=4.0;k2=0.001)	0.16\r\n" + 
			"BATCS(k1=4.0;k2=0.01)	0.16\r\n" + 
			"BATCS(k1=4.0;k2=0.1)	0.15\r\n" + 
			"BATCS(k1=4.0;k2=0.25)	0.15\r\n" + 
			"BATCS(k1=4.0;k2=0.5)	0.15\r\n" + 
			"BATCS(k1=4.0;k2=1.0)	0.15\r\n" + 
			"BATCS(k1=4.0;k2=1.5)	0.15\r\n" + 
			"BATCS(k1=4.5;k2=1.0E-4)	0.16\r\n" + 
			"BATCS(k1=4.5;k2=0.001)	0.16\r\n" + 
			"BATCS(k1=4.5;k2=0.01)	0.16\r\n" + 
			"BATCS(k1=4.5;k2=0.1)	0.15\r\n" + 
			"BATCS(k1=4.5;k2=0.25)	0.15\r\n" + 
			"BATCS(k1=4.5;k2=0.5)	0.15\r\n" + 
			"BATCS(k1=4.5;k2=1.0)	0.15\r\n" + 
			"BATCS(k1=4.5;k2=1.5)	0.15\r\n" + 
			"BATCS(k1=5.0;k2=1.0E-4)	0.16\r\n" + 
			"BATCS(k1=5.0;k2=0.001)	0.16\r\n" + 
			"BATCS(k1=5.0;k2=0.01)	0.16\r\n" + 
			"BATCS(k1=5.0;k2=0.1)	0.15\r\n" + 
			"BATCS(k1=5.0;k2=0.25)	0.15\r\n" + 
			"BATCS(k1=5.0;k2=0.5)	0.15\r\n" + 
			"BATCS(k1=5.0;k2=1.0)	0.15\r\n" + 
			"BATCS(k1=5.0;k2=1.5)	0.15\r\n" + 
			"BATCS(k1=5.5;k2=1.0E-4)	0.16\r\n" + 
			"BATCS(k1=5.5;k2=0.001)	0.16\r\n" + 
			"BATCS(k1=5.5;k2=0.01)	0.16\r\n" + 
			"BATCS(k1=5.5;k2=0.1)	0.15\r\n" + 
			"BATCS(k1=5.5;k2=0.25)	0.15\r\n" + 
			"BATCS(k1=5.5;k2=0.5)	0.15\r\n" + 
			"BATCS(k1=5.5;k2=1.0)	0.15\r\n" + 
			"BATCS(k1=5.5;k2=1.5)	0.15\r\n" + 
			"BATCS(k1=6.0;k2=1.0E-4)	0.16\r\n" + 
			"BATCS(k1=6.0;k2=0.001)	0.16\r\n" + 
			"BATCS(k1=6.0;k2=0.01)	0.16\r\n" + 
			"BATCS(k1=6.0;k2=0.1)	0.15\r\n" + 
			"BATCS(k1=6.0;k2=0.25)	0.15\r\n" + 
			"BATCS(k1=6.0;k2=0.5)	0.15\r\n" + 
			"BATCS(k1=6.0;k2=1.0)	0.15\r\n" + 
			"BATCS(k1=6.0;k2=1.5)	0.15\r\n" + 
			"BATCS(k1=6.5;k2=1.0E-4)	0.16\r\n" + 
			"BATCS(k1=6.5;k2=0.001)	0.16\r\n" + 
			"BATCS(k1=6.5;k2=0.01)	0.16\r\n" + 
			"BATCS(k1=6.5;k2=0.1)	0.16\r\n" + 
			"BATCS(k1=6.5;k2=0.25)	0.15\r\n" + 
			"BATCS(k1=6.5;k2=0.5)	0.15\r\n" + 
			"BATCS(k1=6.5;k2=1.0)	0.15\r\n" + 
			"BATCS(k1=6.5;k2=1.5)	0.15\r\n" + 
			"BATCS(k1=7.0;k2=1.0E-4)	0.16\r\n" + 
			"BATCS(k1=7.0;k2=0.001)	0.16\r\n" + 
			"BATCS(k1=7.0;k2=0.01)	0.16\r\n" + 
			"BATCS(k1=7.0;k2=0.1)	0.16\r\n" + 
			"BATCS(k1=7.0;k2=0.25)	0.15\r\n" + 
			"BATCS(k1=7.0;k2=0.5)	0.15\r\n" + 
			"BATCS(k1=7.0;k2=1.0)	0.16\r\n" + 
			"BATCS(k1=7.0;k2=1.5)	0.16\r\n" + 
			"BATCS(k1=8.0;k2=1.0E-4)	0.16\r\n" + 
			"BATCS(k1=8.0;k2=0.001)	0.16\r\n" + 
			"BATCS(k1=8.0;k2=0.01)	0.16\r\n" + 
			"BATCS(k1=8.0;k2=0.1)	0.16\r\n" + 
			"BATCS(k1=8.0;k2=0.25)	0.16\r\n" + 
			"BATCS(k1=8.0;k2=0.5)	0.16\r\n" + 
			"BATCS(k1=8.0;k2=1.0)	0.16\r\n" + 
			"BATCS(k1=8.0;k2=1.5)	0.16\r\n" + 
			"BATCS(k1=9.0;k2=1.0E-4)	0.16\r\n" + 
			"BATCS(k1=9.0;k2=0.001)	0.16\r\n" + 
			"BATCS(k1=9.0;k2=0.01)	0.16\r\n" + 
			"BATCS(k1=9.0;k2=0.1)	0.16\r\n" + 
			"BATCS(k1=9.0;k2=0.25)	0.16\r\n" + 
			"BATCS(k1=9.0;k2=0.5)	0.16\r\n" + 
			"BATCS(k1=9.0;k2=1.0)	0.16\r\n" + 
			"BATCS(k1=9.0;k2=1.5)	0.16\r\n" + 
			"BATCS(k1=10.0;k2=1.0E-4)	0.16\r\n" + 
			"BATCS(k1=10.0;k2=0.001)	0.16\r\n" + 
			"BATCS(k1=10.0;k2=0.01)	0.17\r\n" + 
			"BATCS(k1=10.0;k2=0.1)	0.16\r\n" + 
			"BATCS(k1=10.0;k2=0.25)	0.16\r\n" + 
			"BATCS(k1=10.0;k2=0.5)	0.16\r\n" + 
			"BATCS(k1=10.0;k2=1.0)	0.16\r\n" + 
			"BATCS(k1=10.0;k2=1.5)	0.16\r\n" + 
			"BATCS(k1=20.0;k2=1.0E-4)	0.17\r\n" + 
			"BATCS(k1=20.0;k2=0.001)	0.17\r\n" + 
			"BATCS(k1=20.0;k2=0.01)	0.17\r\n" + 
			"BATCS(k1=20.0;k2=0.1)	0.17\r\n" + 
			"BATCS(k1=20.0;k2=0.25)	0.17\r\n" + 
			"BATCS(k1=20.0;k2=0.5)	0.17\r\n" + 
			"BATCS(k1=20.0;k2=1.0)	0.17\r\n" + 
			"BATCS(k1=20.0;k2=1.5)	0.17\r\n" + 
			"BATCS(k1=50.0;k2=1.0E-4)	0.17\r\n" + 
			"BATCS(k1=50.0;k2=0.001)	0.17\r\n" + 
			"BATCS(k1=50.0;k2=0.01)	0.18\r\n" + 
			"BATCS(k1=50.0;k2=0.1)	0.18\r\n" + 
			"BATCS(k1=50.0;k2=0.25)	0.17\r\n" + 
			"BATCS(k1=50.0;k2=0.5)	0.17\r\n" + 
			"BATCS(k1=50.0;k2=1.0)	0.17\r\n" + 
			"BATCS(k1=50.0;k2=1.5)	0.18\r\n" + 
			"BATCS(k1=100.0;k2=1.0E-4)	0.17\r\n" + 
			"BATCS(k1=100.0;k2=0.001)	0.17\r\n" + 
			"BATCS(k1=100.0;k2=0.01)	0.18\r\n" + 
			"BATCS(k1=100.0;k2=0.1)	0.18\r\n" + 
			"BATCS(k1=100.0;k2=0.25)	0.18\r\n" + 
			"BATCS(k1=100.0;k2=0.5)	0.18\r\n" + 
			"BATCS(k1=100.0;k2=1.0)	0.18\r\n" + 
			"BATCS(k1=100.0;k2=1.5)	0.18\r\n" + 
			"ATCS(k1=1.0E-4;k2=1.0E-4)	0.15\r\n" + 
			"ATCS(k1=1.0E-4;k2=0.001)	0.21\r\n" + 
			"ATCS(k1=1.0E-4;k2=0.01)	0.23\r\n" + 
			"ATCS(k1=1.0E-4;k2=0.1)	0.24\r\n" + 
			"ATCS(k1=1.0E-4;k2=0.25)	0.24\r\n" + 
			"ATCS(k1=1.0E-4;k2=0.5)	0.24\r\n" + 
			"ATCS(k1=1.0E-4;k2=1.0)	0.27\r\n" + 
			"ATCS(k1=1.0E-4;k2=1.5)	0.29\r\n" + 
			"ATCS(k1=0.001;k2=1.0E-4)	0.15\r\n" + 
			"ATCS(k1=0.001;k2=0.001)	0.15\r\n" + 
			"ATCS(k1=0.001;k2=0.01)	0.21\r\n" + 
			"ATCS(k1=0.001;k2=0.1)	0.23\r\n" + 
			"ATCS(k1=0.001;k2=0.25)	0.24\r\n" + 
			"ATCS(k1=0.001;k2=0.5)	0.24\r\n" + 
			"ATCS(k1=0.001;k2=1.0)	0.27\r\n" + 
			"ATCS(k1=0.001;k2=1.5)	0.29\r\n" + 
			"ATCS(k1=0.01;k2=1.0E-4)	0.15\r\n" + 
			"ATCS(k1=0.01;k2=0.001)	0.15\r\n" + 
			"ATCS(k1=0.01;k2=0.01)	0.15\r\n" + 
			"ATCS(k1=0.01;k2=0.1)	0.21\r\n" + 
			"ATCS(k1=0.01;k2=0.25)	0.23\r\n" + 
			"ATCS(k1=0.01;k2=0.5)	0.23\r\n" + 
			"ATCS(k1=0.01;k2=1.0)	0.26\r\n" + 
			"ATCS(k1=0.01;k2=1.5)	0.29\r\n" + 
			"ATCS(k1=0.1;k2=1.0E-4)	0.16\r\n" + 
			"ATCS(k1=0.1;k2=0.001)	0.15\r\n" + 
			"ATCS(k1=0.1;k2=0.01)	0.15\r\n" + 
			"ATCS(k1=0.1;k2=0.1)	0.15\r\n" + 
			"ATCS(k1=0.1;k2=0.25)	0.16\r\n" + 
			"ATCS(k1=0.1;k2=0.5)	0.19\r\n" + 
			"ATCS(k1=0.1;k2=1.0)	0.23\r\n" + 
			"ATCS(k1=0.1;k2=1.5)	0.27\r\n" + 
			"ATCS(k1=0.5;k2=1.0E-4)	0.16\r\n" + 
			"ATCS(k1=0.5;k2=0.001)	0.16\r\n" + 
			"ATCS(k1=0.5;k2=0.01)	0.15\r\n" + 
			"ATCS(k1=0.5;k2=0.1)	0.15\r\n" + 
			"ATCS(k1=0.5;k2=0.25)	0.15\r\n" + 
			"ATCS(k1=0.5;k2=0.5)	0.15\r\n" + 
			"ATCS(k1=0.5;k2=1.0)	0.15\r\n" + 
			"ATCS(k1=0.5;k2=1.5)	0.18\r\n" + 
			"ATCS(k1=1.0;k2=1.0E-4)	0.16\r\n" + 
			"ATCS(k1=1.0;k2=0.001)	0.16\r\n" + 
			"ATCS(k1=1.0;k2=0.01)	0.15\r\n" + 
			"ATCS(k1=1.0;k2=0.1)	0.15\r\n" + 
			"ATCS(k1=1.0;k2=0.25)	0.15\r\n" + 
			"ATCS(k1=1.0;k2=0.5)	0.15\r\n" + 
			"ATCS(k1=1.0;k2=1.0)	0.15\r\n" + 
			"ATCS(k1=1.0;k2=1.5)	0.15\r\n" + 
			"ATCS(k1=1.5;k2=1.0E-4)	0.16\r\n" + 
			"ATCS(k1=1.5;k2=0.001)	0.15\r\n" + 
			"ATCS(k1=1.5;k2=0.01)	0.15\r\n" + 
			"ATCS(k1=1.5;k2=0.1)	0.15\r\n" + 
			"ATCS(k1=1.5;k2=0.25)	0.15\r\n" + 
			"ATCS(k1=1.5;k2=0.5)	0.15\r\n" + 
			"ATCS(k1=1.5;k2=1.0)	0.15\r\n" + 
			"ATCS(k1=1.5;k2=1.5)	0.15\r\n" + 
			"ATCS(k1=2.0;k2=1.0E-4)	0.16\r\n" + 
			"ATCS(k1=2.0;k2=0.001)	0.16\r\n" + 
			"ATCS(k1=2.0;k2=0.01)	0.15\r\n" + 
			"ATCS(k1=2.0;k2=0.1)	0.15\r\n" + 
			"ATCS(k1=2.0;k2=0.25)	0.15\r\n" + 
			"ATCS(k1=2.0;k2=0.5)	0.15\r\n" + 
			"ATCS(k1=2.0;k2=1.0)	0.15\r\n" + 
			"ATCS(k1=2.0;k2=1.5)	0.15\r\n" + 
			"ATCS(k1=2.5;k2=1.0E-4)	0.16\r\n" + 
			"ATCS(k1=2.5;k2=0.001)	0.16\r\n" + 
			"ATCS(k1=2.5;k2=0.01)	0.15\r\n" + 
			"ATCS(k1=2.5;k2=0.1)	0.15\r\n" + 
			"ATCS(k1=2.5;k2=0.25)	0.15\r\n" + 
			"ATCS(k1=2.5;k2=0.5)	0.15\r\n" + 
			"ATCS(k1=2.5;k2=1.0)	0.15\r\n" + 
			"ATCS(k1=2.5;k2=1.5)	0.15\r\n" + 
			"ATCS(k1=3.0;k2=1.0E-4)	0.16\r\n" + 
			"ATCS(k1=3.0;k2=0.001)	0.15\r\n" + 
			"ATCS(k1=3.0;k2=0.01)	0.15\r\n" + 
			"ATCS(k1=3.0;k2=0.1)	0.15\r\n" + 
			"ATCS(k1=3.0;k2=0.25)	0.15\r\n" + 
			"ATCS(k1=3.0;k2=0.5)	0.15\r\n" + 
			"ATCS(k1=3.0;k2=1.0)	0.15\r\n" + 
			"ATCS(k1=3.0;k2=1.5)	0.15\r\n" + 
			"ATCS(k1=3.5;k2=1.0E-4)	0.16\r\n" + 
			"ATCS(k1=3.5;k2=0.001)	0.15\r\n" + 
			"ATCS(k1=3.5;k2=0.01)	0.15\r\n" + 
			"ATCS(k1=3.5;k2=0.1)	0.15\r\n" + 
			"ATCS(k1=3.5;k2=0.25)	0.15\r\n" + 
			"ATCS(k1=3.5;k2=0.5)	0.15\r\n" + 
			"ATCS(k1=3.5;k2=1.0)	0.15\r\n" + 
			"ATCS(k1=3.5;k2=1.5)	0.15\r\n" + 
			"ATCS(k1=4.0;k2=1.0E-4)	0.16\r\n" + 
			"ATCS(k1=4.0;k2=0.001)	0.16\r\n" + 
			"ATCS(k1=4.0;k2=0.01)	0.15\r\n" + 
			"ATCS(k1=4.0;k2=0.1)	0.15\r\n" + 
			"ATCS(k1=4.0;k2=0.25)	0.15\r\n" + 
			"ATCS(k1=4.0;k2=0.5)	0.15\r\n" + 
			"ATCS(k1=4.0;k2=1.0)	0.15\r\n" + 
			"ATCS(k1=4.0;k2=1.5)	0.15\r\n" + 
			"ATCS(k1=4.5;k2=1.0E-4)	0.16\r\n" + 
			"ATCS(k1=4.5;k2=0.001)	0.16\r\n" + 
			"ATCS(k1=4.5;k2=0.01)	0.16\r\n" + 
			"ATCS(k1=4.5;k2=0.1)	0.15\r\n" + 
			"ATCS(k1=4.5;k2=0.25)	0.15\r\n" + 
			"ATCS(k1=4.5;k2=0.5)	0.15\r\n" + 
			"ATCS(k1=4.5;k2=1.0)	0.15\r\n" + 
			"ATCS(k1=4.5;k2=1.5)	0.15\r\n" + 
			"ATCS(k1=5.0;k2=1.0E-4)	0.16\r\n" + 
			"ATCS(k1=5.0;k2=0.001)	0.16\r\n" + 
			"ATCS(k1=5.0;k2=0.01)	0.16\r\n" + 
			"ATCS(k1=5.0;k2=0.1)	0.15\r\n" + 
			"ATCS(k1=5.0;k2=0.25)	0.15\r\n" + 
			"ATCS(k1=5.0;k2=0.5)	0.15\r\n" + 
			"ATCS(k1=5.0;k2=1.0)	0.15\r\n" + 
			"ATCS(k1=5.0;k2=1.5)	0.15\r\n" + 
			"ATCS(k1=5.5;k2=1.0E-4)	0.16\r\n" + 
			"ATCS(k1=5.5;k2=0.001)	0.16\r\n" + 
			"ATCS(k1=5.5;k2=0.01)	0.16\r\n" + 
			"ATCS(k1=5.5;k2=0.1)	0.15\r\n" + 
			"ATCS(k1=5.5;k2=0.25)	0.15\r\n" + 
			"ATCS(k1=5.5;k2=0.5)	0.15\r\n" + 
			"ATCS(k1=5.5;k2=1.0)	0.15\r\n" + 
			"ATCS(k1=5.5;k2=1.5)	0.15\r\n" + 
			"ATCS(k1=6.0;k2=1.0E-4)	0.16\r\n" + 
			"ATCS(k1=6.0;k2=0.001)	0.16\r\n" + 
			"ATCS(k1=6.0;k2=0.01)	0.16\r\n" + 
			"ATCS(k1=6.0;k2=0.1)	0.15\r\n" + 
			"ATCS(k1=6.0;k2=0.25)	0.15\r\n" + 
			"ATCS(k1=6.0;k2=0.5)	0.15\r\n" + 
			"ATCS(k1=6.0;k2=1.0)	0.15\r\n" + 
			"ATCS(k1=6.0;k2=1.5)	0.15\r\n" + 
			"ATCS(k1=6.5;k2=1.0E-4)	0.16\r\n" + 
			"ATCS(k1=6.5;k2=0.001)	0.16\r\n" + 
			"ATCS(k1=6.5;k2=0.01)	0.16\r\n" + 
			"ATCS(k1=6.5;k2=0.1)	0.15\r\n" + 
			"ATCS(k1=6.5;k2=0.25)	0.15\r\n" + 
			"ATCS(k1=6.5;k2=0.5)	0.15\r\n" + 
			"ATCS(k1=6.5;k2=1.0)	0.15\r\n" + 
			"ATCS(k1=6.5;k2=1.5)	0.15\r\n" + 
			"ATCS(k1=7.0;k2=1.0E-4)	0.16\r\n" + 
			"ATCS(k1=7.0;k2=0.001)	0.16\r\n" + 
			"ATCS(k1=7.0;k2=0.01)	0.16\r\n" + 
			"ATCS(k1=7.0;k2=0.1)	0.15\r\n" + 
			"ATCS(k1=7.0;k2=0.25)	0.15\r\n" + 
			"ATCS(k1=7.0;k2=0.5)	0.15\r\n" + 
			"ATCS(k1=7.0;k2=1.0)	0.15\r\n" + 
			"ATCS(k1=7.0;k2=1.5)	0.15\r\n" + 
			"ATCS(k1=8.0;k2=1.0E-4)	0.16\r\n" + 
			"ATCS(k1=8.0;k2=0.001)	0.16\r\n" + 
			"ATCS(k1=8.0;k2=0.01)	0.16\r\n" + 
			"ATCS(k1=8.0;k2=0.1)	0.15\r\n" + 
			"ATCS(k1=8.0;k2=0.25)	0.15\r\n" + 
			"ATCS(k1=8.0;k2=0.5)	0.15\r\n" + 
			"ATCS(k1=8.0;k2=1.0)	0.15\r\n" + 
			"ATCS(k1=8.0;k2=1.5)	0.15\r\n" + 
			"ATCS(k1=9.0;k2=1.0E-4)	0.16\r\n" + 
			"ATCS(k1=9.0;k2=0.001)	0.16\r\n" + 
			"ATCS(k1=9.0;k2=0.01)	0.16\r\n" + 
			"ATCS(k1=9.0;k2=0.1)	0.16\r\n" + 
			"ATCS(k1=9.0;k2=0.25)	0.15\r\n" + 
			"ATCS(k1=9.0;k2=0.5)	0.15\r\n" + 
			"ATCS(k1=9.0;k2=1.0)	0.15\r\n" + 
			"ATCS(k1=9.0;k2=1.5)	0.15\r\n" + 
			"ATCS(k1=10.0;k2=1.0E-4)	0.16\r\n" + 
			"ATCS(k1=10.0;k2=0.001)	0.16\r\n" + 
			"ATCS(k1=10.0;k2=0.01)	0.16\r\n" + 
			"ATCS(k1=10.0;k2=0.1)	0.16\r\n" + 
			"ATCS(k1=10.0;k2=0.25)	0.15\r\n" + 
			"ATCS(k1=10.0;k2=0.5)	0.15\r\n" + 
			"ATCS(k1=10.0;k2=1.0)	0.15\r\n" + 
			"ATCS(k1=10.0;k2=1.5)	0.16\r\n" + 
			"ATCS(k1=20.0;k2=1.0E-4)	0.16\r\n" + 
			"ATCS(k1=20.0;k2=0.001)	0.16\r\n" + 
			"ATCS(k1=20.0;k2=0.01)	0.16\r\n" + 
			"ATCS(k1=20.0;k2=0.1)	0.16\r\n" + 
			"ATCS(k1=20.0;k2=0.25)	0.16\r\n" + 
			"ATCS(k1=20.0;k2=0.5)	0.16\r\n" + 
			"ATCS(k1=20.0;k2=1.0)	0.16\r\n" + 
			"ATCS(k1=20.0;k2=1.5)	0.16\r\n" + 
			"ATCS(k1=50.0;k2=1.0E-4)	0.16\r\n" + 
			"ATCS(k1=50.0;k2=0.001)	0.16\r\n" + 
			"ATCS(k1=50.0;k2=0.01)	0.16\r\n" + 
			"ATCS(k1=50.0;k2=0.1)	0.16\r\n" + 
			"ATCS(k1=50.0;k2=0.25)	0.16\r\n" + 
			"ATCS(k1=50.0;k2=0.5)	0.16\r\n" + 
			"ATCS(k1=50.0;k2=1.0)	0.16\r\n" + 
			"ATCS(k1=50.0;k2=1.5)	0.16\r\n" + 
			"ATCS(k1=100.0;k2=1.0E-4)	0.17\r\n" + 
			"ATCS(k1=100.0;k2=0.001)	0.17\r\n" + 
			"ATCS(k1=100.0;k2=0.01)	0.17\r\n" + 
			"ATCS(k1=100.0;k2=0.1)	0.17\r\n" + 
			"ATCS(k1=100.0;k2=0.25)	0.17\r\n" + 
			"ATCS(k1=100.0;k2=0.5)	0.17\r\n" + 
			"ATCS(k1=100.0;k2=1.0)	0.17\r\n" + 
			"ATCS(k1=100.0;k2=1.5)	0.17\r\n" + 
			"MCB(0.0)	0.31\r\n" + 
			"BOF	0.80\r\n" + 
			"MBS(0.125)	0.89\r\n" + 
			"MBS(0.25)	0.88\r\n" + 
			"MBS(0.375)	0.84\r\n" + 
			"MBS(0.5)	0.76\r\n" + 
			"MBS(0.625)	0.62\r\n" + 
			"MBS(0.75)	0.57\r\n" + 
			"MBS(0.825)	0.55\r\n" + 
			"MBS(1.0)	0.35\r\n" + 
			"	\r\n";
	
	private String[] flowTimeSets; 
	private String flowTimeStr="IGF[LW[FCFS[TieBreakerFASFS]]]	52572.79\r\n" + 
			"IGF[LW[CR[TieBreakerFASFS]]]	51887.98\r\n" + 
			"IGF[WSPT[TieBreakerFASFS]]	51255.46\r\n" + 
			"IGF[WMDD[TieBreakerFASFS]]	NaN\r\n" + 
			"IGF[WMOD[TieBreakerFASFS]]	NaN\r\n" + 
			"IGF[LRM[TieBreakerFASFS]]	56532.89\r\n" + 
			"IGF[EDD[TieBreakerFASFS]]	NaN\r\n" + 
			"IGF[ERD[TieBreakerFASFS]]	NaN\r\n" + 
			"IGF[ODD[TieBreakerFASFS]]	NaN\r\n" + 
			"IGF[SLK[TieBreakerFASFS]]	NaN\r\n" + 
			"IGF[SST[LW[FCFS[TieBreakerFASFS]]]]	50481.95\r\n" + 
			"IGF[SST[LW[CR[TieBreakerFASFS]]]]	50462.90\r\n" + 
			"IGF[SST[WSPT[TieBreakerFASFS]]]	50471.01\r\n" + 
			"IGF[SST[WMDD[TieBreakerFASFS]]]	50960.51\r\n" + 
			"IGF[SST[WMOD[TieBreakerFASFS]]]	51128.35\r\n" + 
			"IGF[SST[LRM[TieBreakerFASFS]]]	53214.88\r\n" + 
			"IGF[SST[EDD[TieBreakerFASFS]]]	52729.75\r\n" + 
			"IGF[SST[ERD[TieBreakerFASFS]]]	52814.17\r\n" + 
			"IGF[SST[ODD[TieBreakerFASFS]]]	52900.62\r\n" + 
			"IGF[SST[SLK[TieBreakerFASFS]]]	52715.75\r\n" + 
			"BATCS(k1=1.0E-4;k2=1.0E-4)	55750.61\r\n" + 
			"BATCS(k1=1.0E-4;k2=0.001)	62883.07\r\n" + 
			"BATCS(k1=1.0E-4;k2=0.01)	64636.59\r\n" + 
			"BATCS(k1=1.0E-4;k2=0.1)	64734.27\r\n" + 
			"BATCS(k1=1.0E-4;k2=0.25)	64786.54\r\n" + 
			"BATCS(k1=1.0E-4;k2=0.5)	64759.62\r\n" + 
			"BATCS(k1=1.0E-4;k2=1.0)	65443.14\r\n" + 
			"BATCS(k1=1.0E-4;k2=1.5)	66042.53\r\n" + 
			"BATCS(k1=0.001;k2=1.0E-4)	53666.41\r\n" + 
			"BATCS(k1=0.001;k2=0.001)	55796.02\r\n" + 
			"BATCS(k1=0.001;k2=0.01)	63034.24\r\n" + 
			"BATCS(k1=0.001;k2=0.1)	64659.54\r\n" + 
			"BATCS(k1=0.001;k2=0.25)	64736.30\r\n" + 
			"BATCS(k1=0.001;k2=0.5)	64763.08\r\n" + 
			"BATCS(k1=0.001;k2=1.0)	65385.18\r\n" + 
			"BATCS(k1=0.001;k2=1.5)	66083.49\r\n" + 
			"BATCS(k1=0.01;k2=1.0E-4)	53255.81\r\n" + 
			"BATCS(k1=0.01;k2=0.001)	53672.13\r\n" + 
			"BATCS(k1=0.01;k2=0.01)	55800.13\r\n" + 
			"BATCS(k1=0.01;k2=0.1)	62877.26\r\n" + 
			"BATCS(k1=0.01;k2=0.25)	64045.47\r\n" + 
			"BATCS(k1=0.01;k2=0.5)	64337.65\r\n" + 
			"BATCS(k1=0.01;k2=1.0)	65164.53\r\n" + 
			"BATCS(k1=0.01;k2=1.5)	65952.89\r\n" + 
			"BATCS(k1=0.1;k2=1.0E-4)	53145.32\r\n" + 
			"BATCS(k1=0.1;k2=0.001)	53242.70\r\n" + 
			"BATCS(k1=0.1;k2=0.01)	53649.44\r\n" + 
			"BATCS(k1=0.1;k2=0.1)	55767.35\r\n" + 
			"BATCS(k1=0.1;k2=0.25)	58286.33\r\n" + 
			"BATCS(k1=0.1;k2=0.5)	60939.55\r\n" + 
			"BATCS(k1=0.1;k2=1.0)	63463.87\r\n" + 
			"BATCS(k1=0.1;k2=1.5)	64603.80\r\n" + 
			"BATCS(k1=0.5;k2=1.0E-4)	52904.30\r\n" + 
			"BATCS(k1=0.5;k2=0.001)	53155.28\r\n" + 
			"BATCS(k1=0.5;k2=0.01)	53219.74\r\n" + 
			"BATCS(k1=0.5;k2=0.1)	53958.57\r\n" + 
			"BATCS(k1=0.5;k2=0.25)	54800.67\r\n" + 
			"BATCS(k1=0.5;k2=0.5)	55569.35\r\n" + 
			"BATCS(k1=0.5;k2=1.0)	57185.77\r\n" + 
			"BATCS(k1=0.5;k2=1.5)	59377.28\r\n" + 
			"BATCS(k1=1.0;k2=1.0E-4)	52733.20\r\n" + 
			"BATCS(k1=1.0;k2=0.001)	53004.81\r\n" + 
			"BATCS(k1=1.0;k2=0.01)	53097.91\r\n" + 
			"BATCS(k1=1.0;k2=0.1)	53477.88\r\n" + 
			"BATCS(k1=1.0;k2=0.25)	54067.23\r\n" + 
			"BATCS(k1=1.0;k2=0.5)	54638.70\r\n" + 
			"BATCS(k1=1.0;k2=1.0)	55492.16\r\n" + 
			"BATCS(k1=1.0;k2=1.5)	56317.81\r\n" + 
			"BATCS(k1=1.5;k2=1.0E-4)	52555.08\r\n" + 
			"BATCS(k1=1.5;k2=0.001)	52888.18\r\n" + 
			"BATCS(k1=1.5;k2=0.01)	53038.31\r\n" + 
			"BATCS(k1=1.5;k2=0.1)	53193.55\r\n" + 
			"BATCS(k1=1.5;k2=0.25)	53636.74\r\n" + 
			"BATCS(k1=1.5;k2=0.5)	54103.45\r\n" + 
			"BATCS(k1=1.5;k2=1.0)	54789.05\r\n" + 
			"BATCS(k1=1.5;k2=1.5)	55288.97\r\n" + 
			"BATCS(k1=2.0;k2=1.0E-4)	52459.12\r\n" + 
			"BATCS(k1=2.0;k2=0.001)	52715.23\r\n" + 
			"BATCS(k1=2.0;k2=0.01)	52956.33\r\n" + 
			"BATCS(k1=2.0;k2=0.1)	53047.57\r\n" + 
			"BATCS(k1=2.0;k2=0.25)	53390.88\r\n" + 
			"BATCS(k1=2.0;k2=0.5)	53781.25\r\n" + 
			"BATCS(k1=2.0;k2=1.0)	54347.27\r\n" + 
			"BATCS(k1=2.0;k2=1.5)	54758.37\r\n" + 
			"BATCS(k1=2.5;k2=1.0E-4)	52359.63\r\n" + 
			"BATCS(k1=2.5;k2=0.001)	52606.87\r\n" + 
			"BATCS(k1=2.5;k2=0.01)	52819.82\r\n" + 
			"BATCS(k1=2.5;k2=0.1)	52876.27\r\n" + 
			"BATCS(k1=2.5;k2=0.25)	53127.01\r\n" + 
			"BATCS(k1=2.5;k2=0.5)	53434.52\r\n" + 
			"BATCS(k1=2.5;k2=1.0)	53989.58\r\n" + 
			"BATCS(k1=2.5;k2=1.5)	54396.66\r\n" + 
			"BATCS(k1=3.0;k2=1.0E-4)	52284.10\r\n" + 
			"BATCS(k1=3.0;k2=0.001)	52531.31\r\n" + 
			"BATCS(k1=3.0;k2=0.01)	52803.77\r\n" + 
			"BATCS(k1=3.0;k2=0.1)	52836.79\r\n" + 
			"BATCS(k1=3.0;k2=0.25)	52965.81\r\n" + 
			"BATCS(k1=3.0;k2=0.5)	53244.42\r\n" + 
			"BATCS(k1=3.0;k2=1.0)	53713.55\r\n" + 
			"BATCS(k1=3.0;k2=1.5)	54081.65\r\n" + 
			"BATCS(k1=3.5;k2=1.0E-4)	52153.15\r\n" + 
			"BATCS(k1=3.5;k2=0.001)	52406.23\r\n" + 
			"BATCS(k1=3.5;k2=0.01)	52704.18\r\n" + 
			"BATCS(k1=3.5;k2=0.1)	52725.35\r\n" + 
			"BATCS(k1=3.5;k2=0.25)	52812.23\r\n" + 
			"BATCS(k1=3.5;k2=0.5)	53058.74\r\n" + 
			"BATCS(k1=3.5;k2=1.0)	53479.78\r\n" + 
			"BATCS(k1=3.5;k2=1.5)	53794.89\r\n" + 
			"BATCS(k1=4.0;k2=1.0E-4)	52069.52\r\n" + 
			"BATCS(k1=4.0;k2=0.001)	52345.69\r\n" + 
			"BATCS(k1=4.0;k2=0.01)	52616.80\r\n" + 
			"BATCS(k1=4.0;k2=0.1)	52616.19\r\n" + 
			"BATCS(k1=4.0;k2=0.25)	52676.62\r\n" + 
			"BATCS(k1=4.0;k2=0.5)	52890.18\r\n" + 
			"BATCS(k1=4.0;k2=1.0)	53261.77\r\n" + 
			"BATCS(k1=4.0;k2=1.5)	53539.16\r\n" + 
			"BATCS(k1=4.5;k2=1.0E-4)	52026.71\r\n" + 
			"BATCS(k1=4.5;k2=0.001)	52237.10\r\n" + 
			"BATCS(k1=4.5;k2=0.01)	52524.71\r\n" + 
			"BATCS(k1=4.5;k2=0.1)	52467.53\r\n" + 
			"BATCS(k1=4.5;k2=0.25)	52568.55\r\n" + 
			"BATCS(k1=4.5;k2=0.5)	52755.46\r\n" + 
			"BATCS(k1=4.5;k2=1.0)	53095.38\r\n" + 
			"BATCS(k1=4.5;k2=1.5)	53343.69\r\n" + 
			"BATCS(k1=5.0;k2=1.0E-4)	51964.22\r\n" + 
			"BATCS(k1=5.0;k2=0.001)	52232.28\r\n" + 
			"BATCS(k1=5.0;k2=0.01)	52468.18\r\n" + 
			"BATCS(k1=5.0;k2=0.1)	52488.35\r\n" + 
			"BATCS(k1=5.0;k2=0.25)	52548.48\r\n" + 
			"BATCS(k1=5.0;k2=0.5)	52642.09\r\n" + 
			"BATCS(k1=5.0;k2=1.0)	52945.05\r\n" + 
			"BATCS(k1=5.0;k2=1.5)	53125.09\r\n" + 
			"BATCS(k1=5.5;k2=1.0E-4)	51903.26\r\n" + 
			"BATCS(k1=5.5;k2=0.001)	52103.74\r\n" + 
			"BATCS(k1=5.5;k2=0.01)	52408.80\r\n" + 
			"BATCS(k1=5.5;k2=0.1)	52437.13\r\n" + 
			"BATCS(k1=5.5;k2=0.25)	52423.58\r\n" + 
			"BATCS(k1=5.5;k2=0.5)	52491.38\r\n" + 
			"BATCS(k1=5.5;k2=1.0)	52768.76\r\n" + 
			"BATCS(k1=5.5;k2=1.5)	52972.23\r\n" + 
			"BATCS(k1=6.0;k2=1.0E-4)	51879.58\r\n" + 
			"BATCS(k1=6.0;k2=0.001)	52137.72\r\n" + 
			"BATCS(k1=6.0;k2=0.01)	52358.82\r\n" + 
			"BATCS(k1=6.0;k2=0.1)	52347.02\r\n" + 
			"BATCS(k1=6.0;k2=0.25)	52405.60\r\n" + 
			"BATCS(k1=6.0;k2=0.5)	52413.47\r\n" + 
			"BATCS(k1=6.0;k2=1.0)	52611.57\r\n" + 
			"BATCS(k1=6.0;k2=1.5)	52861.96\r\n" + 
			"BATCS(k1=6.5;k2=1.0E-4)	51837.78\r\n" + 
			"BATCS(k1=6.5;k2=0.001)	52095.32\r\n" + 
			"BATCS(k1=6.5;k2=0.01)	52310.68\r\n" + 
			"BATCS(k1=6.5;k2=0.1)	52286.24\r\n" + 
			"BATCS(k1=6.5;k2=0.25)	52318.15\r\n" + 
			"BATCS(k1=6.5;k2=0.5)	52289.18\r\n" + 
			"BATCS(k1=6.5;k2=1.0)	52509.55\r\n" + 
			"BATCS(k1=6.5;k2=1.5)	52685.52\r\n" + 
			"BATCS(k1=7.0;k2=1.0E-4)	51784.38\r\n" + 
			"BATCS(k1=7.0;k2=0.001)	52016.57\r\n" + 
			"BATCS(k1=7.0;k2=0.01)	52274.41\r\n" + 
			"BATCS(k1=7.0;k2=0.1)	52219.76\r\n" + 
			"BATCS(k1=7.0;k2=0.25)	52234.60\r\n" + 
			"BATCS(k1=7.0;k2=0.5)	52251.29\r\n" + 
			"BATCS(k1=7.0;k2=1.0)	52423.64\r\n" + 
			"BATCS(k1=7.0;k2=1.5)	52616.28\r\n" + 
			"BATCS(k1=8.0;k2=1.0E-4)	51686.84\r\n" + 
			"BATCS(k1=8.0;k2=0.001)	51906.28\r\n" + 
			"BATCS(k1=8.0;k2=0.01)	52206.28\r\n" + 
			"BATCS(k1=8.0;k2=0.1)	52180.12\r\n" + 
			"BATCS(k1=8.0;k2=0.25)	52121.67\r\n" + 
			"BATCS(k1=8.0;k2=0.5)	52136.76\r\n" + 
			"BATCS(k1=8.0;k2=1.0)	52223.60\r\n" + 
			"BATCS(k1=8.0;k2=1.5)	52410.30\r\n" + 
			"BATCS(k1=9.0;k2=1.0E-4)	51657.20\r\n" + 
			"BATCS(k1=9.0;k2=0.001)	51840.34\r\n" + 
			"BATCS(k1=9.0;k2=0.01)	52121.84\r\n" + 
			"BATCS(k1=9.0;k2=0.1)	52142.40\r\n" + 
			"BATCS(k1=9.0;k2=0.25)	52094.24\r\n" + 
			"BATCS(k1=9.0;k2=0.5)	52015.14\r\n" + 
			"BATCS(k1=9.0;k2=1.0)	52101.72\r\n" + 
			"BATCS(k1=9.0;k2=1.5)	52310.66\r\n" + 
			"BATCS(k1=10.0;k2=1.0E-4)	51601.08\r\n" + 
			"BATCS(k1=10.0;k2=0.001)	51831.54\r\n" + 
			"BATCS(k1=10.0;k2=0.01)	52128.03\r\n" + 
			"BATCS(k1=10.0;k2=0.1)	52118.11\r\n" + 
			"BATCS(k1=10.0;k2=0.25)	52011.24\r\n" + 
			"BATCS(k1=10.0;k2=0.5)	51933.32\r\n" + 
			"BATCS(k1=10.0;k2=1.0)	52016.29\r\n" + 
			"BATCS(k1=10.0;k2=1.5)	52168.01\r\n" + 
			"BATCS(k1=20.0;k2=1.0E-4)	51426.16\r\n" + 
			"BATCS(k1=20.0;k2=0.001)	51566.60\r\n" + 
			"BATCS(k1=20.0;k2=0.01)	51942.93\r\n" + 
			"BATCS(k1=20.0;k2=0.1)	51964.14\r\n" + 
			"BATCS(k1=20.0;k2=0.25)	51860.51\r\n" + 
			"BATCS(k1=20.0;k2=0.5)	51640.73\r\n" + 
			"BATCS(k1=20.0;k2=1.0)	51500.21\r\n" + 
			"BATCS(k1=20.0;k2=1.5)	51673.22\r\n" + 
			"BATCS(k1=50.0;k2=1.0E-4)	51208.32\r\n" + 
			"BATCS(k1=50.0;k2=0.001)	51234.19\r\n" + 
			"BATCS(k1=50.0;k2=0.01)	51630.07\r\n" + 
			"BATCS(k1=50.0;k2=0.1)	51788.37\r\n" + 
			"BATCS(k1=50.0;k2=0.25)	51595.73\r\n" + 
			"BATCS(k1=50.0;k2=0.5)	51280.17\r\n" + 
			"BATCS(k1=50.0;k2=1.0)	50953.51\r\n" + 
			"BATCS(k1=50.0;k2=1.5)	51049.16\r\n" + 
			"BATCS(k1=100.0;k2=1.0E-4)	51083.68\r\n" + 
			"BATCS(k1=100.0;k2=0.001)	51046.65\r\n" + 
			"BATCS(k1=100.0;k2=0.01)	51421.19\r\n" + 
			"BATCS(k1=100.0;k2=0.1)	51559.22\r\n" + 
			"BATCS(k1=100.0;k2=0.25)	51506.70\r\n" + 
			"BATCS(k1=100.0;k2=0.5)	51137.89\r\n" + 
			"BATCS(k1=100.0;k2=1.0)	50793.62\r\n" + 
			"BATCS(k1=100.0;k2=1.5)	50808.13\r\n" + 
			"ATCS(k1=1.0E-4;k2=1.0E-4)	55801.50\r\n" + 
			"ATCS(k1=1.0E-4;k2=0.001)	62917.63\r\n" + 
			"ATCS(k1=1.0E-4;k2=0.01)	64610.80\r\n" + 
			"ATCS(k1=1.0E-4;k2=0.1)	64692.29\r\n" + 
			"ATCS(k1=1.0E-4;k2=0.25)	64845.59\r\n" + 
			"ATCS(k1=1.0E-4;k2=0.5)	64684.12\r\n" + 
			"ATCS(k1=1.0E-4;k2=1.0)	65340.04\r\n" + 
			"ATCS(k1=1.0E-4;k2=1.5)	65944.63\r\n" + 
			"ATCS(k1=0.001;k2=1.0E-4)	53387.54\r\n" + 
			"ATCS(k1=0.001;k2=0.001)	55770.92\r\n" + 
			"ATCS(k1=0.001;k2=0.01)	62955.32\r\n" + 
			"ATCS(k1=0.001;k2=0.1)	64653.55\r\n" + 
			"ATCS(k1=0.001;k2=0.25)	64695.73\r\n" + 
			"ATCS(k1=0.001;k2=0.5)	64601.58\r\n" + 
			"ATCS(k1=0.001;k2=1.0)	65321.99\r\n" + 
			"ATCS(k1=0.001;k2=1.5)	66023.05\r\n" + 
			"ATCS(k1=0.01;k2=1.0E-4)	53040.14\r\n" + 
			"ATCS(k1=0.01;k2=0.001)	53421.62\r\n" + 
			"ATCS(k1=0.01;k2=0.01)	55697.83\r\n" + 
			"ATCS(k1=0.01;k2=0.1)	62892.84\r\n" + 
			"ATCS(k1=0.01;k2=0.25)	64141.38\r\n" + 
			"ATCS(k1=0.01;k2=0.5)	64516.98\r\n" + 
			"ATCS(k1=0.01;k2=1.0)	65158.69\r\n" + 
			"ATCS(k1=0.01;k2=1.5)	65848.43\r\n" + 
			"ATCS(k1=0.1;k2=1.0E-4)	52901.25\r\n" + 
			"ATCS(k1=0.1;k2=0.001)	53018.30\r\n" + 
			"ATCS(k1=0.1;k2=0.01)	53397.82\r\n" + 
			"ATCS(k1=0.1;k2=0.1)	55694.44\r\n" + 
			"ATCS(k1=0.1;k2=0.25)	58303.63\r\n" + 
			"ATCS(k1=0.1;k2=0.5)	61204.78\r\n" + 
			"ATCS(k1=0.1;k2=1.0)	63371.13\r\n" + 
			"ATCS(k1=0.1;k2=1.5)	64602.70\r\n" + 
			"ATCS(k1=0.5;k2=1.0E-4)	52639.08\r\n" + 
			"ATCS(k1=0.5;k2=0.001)	52881.60\r\n" + 
			"ATCS(k1=0.5;k2=0.01)	52937.67\r\n" + 
			"ATCS(k1=0.5;k2=0.1)	53811.19\r\n" + 
			"ATCS(k1=0.5;k2=0.25)	54767.68\r\n" + 
			"ATCS(k1=0.5;k2=0.5)	55529.09\r\n" + 
			"ATCS(k1=0.5;k2=1.0)	57340.10\r\n" + 
			"ATCS(k1=0.5;k2=1.5)	59498.55\r\n" + 
			"ATCS(k1=1.0;k2=1.0E-4)	52463.29\r\n" + 
			"ATCS(k1=1.0;k2=0.001)	52730.20\r\n" + 
			"ATCS(k1=1.0;k2=0.01)	52833.91\r\n" + 
			"ATCS(k1=1.0;k2=0.1)	53176.01\r\n" + 
			"ATCS(k1=1.0;k2=0.25)	53921.37\r\n" + 
			"ATCS(k1=1.0;k2=0.5)	54508.08\r\n" + 
			"ATCS(k1=1.0;k2=1.0)	55373.50\r\n" + 
			"ATCS(k1=1.0;k2=1.5)	56198.28\r\n" + 
			"ATCS(k1=1.5;k2=1.0E-4)	52339.52\r\n" + 
			"ATCS(k1=1.5;k2=0.001)	52584.31\r\n" + 
			"ATCS(k1=1.5;k2=0.01)	52732.80\r\n" + 
			"ATCS(k1=1.5;k2=0.1)	52885.42\r\n" + 
			"ATCS(k1=1.5;k2=0.25)	53395.29\r\n" + 
			"ATCS(k1=1.5;k2=0.5)	53982.22\r\n" + 
			"ATCS(k1=1.5;k2=1.0)	54721.43\r\n" + 
			"ATCS(k1=1.5;k2=1.5)	55206.58\r\n" + 
			"ATCS(k1=2.0;k2=1.0E-4)	52215.14\r\n" + 
			"ATCS(k1=2.0;k2=0.001)	52476.34\r\n" + 
			"ATCS(k1=2.0;k2=0.01)	52655.71\r\n" + 
			"ATCS(k1=2.0;k2=0.1)	52737.82\r\n" + 
			"ATCS(k1=2.0;k2=0.25)	53051.34\r\n" + 
			"ATCS(k1=2.0;k2=0.5)	53577.48\r\n" + 
			"ATCS(k1=2.0;k2=1.0)	54216.56\r\n" + 
			"ATCS(k1=2.0;k2=1.5)	54690.94\r\n" + 
			"ATCS(k1=2.5;k2=1.0E-4)	52058.40\r\n" + 
			"ATCS(k1=2.5;k2=0.001)	52365.08\r\n" + 
			"ATCS(k1=2.5;k2=0.01)	52531.23\r\n" + 
			"ATCS(k1=2.5;k2=0.1)	52576.66\r\n" + 
			"ATCS(k1=2.5;k2=0.25)	52840.55\r\n" + 
			"ATCS(k1=2.5;k2=0.5)	53232.87\r\n" + 
			"ATCS(k1=2.5;k2=1.0)	53849.78\r\n" + 
			"ATCS(k1=2.5;k2=1.5)	54309.85\r\n" + 
			"ATCS(k1=3.0;k2=1.0E-4)	51971.67\r\n" + 
			"ATCS(k1=3.0;k2=0.001)	52237.77\r\n" + 
			"ATCS(k1=3.0;k2=0.01)	52480.41\r\n" + 
			"ATCS(k1=3.0;k2=0.1)	52516.77\r\n" + 
			"ATCS(k1=3.0;k2=0.25)	52661.48\r\n" + 
			"ATCS(k1=3.0;k2=0.5)	52948.84\r\n" + 
			"ATCS(k1=3.0;k2=1.0)	53568.96\r\n" + 
			"ATCS(k1=3.0;k2=1.5)	53910.92\r\n" + 
			"ATCS(k1=3.5;k2=1.0E-4)	51844.13\r\n" + 
			"ATCS(k1=3.5;k2=0.001)	52098.37\r\n" + 
			"ATCS(k1=3.5;k2=0.01)	52402.91\r\n" + 
			"ATCS(k1=3.5;k2=0.1)	52406.33\r\n" + 
			"ATCS(k1=3.5;k2=0.25)	52512.93\r\n" + 
			"ATCS(k1=3.5;k2=0.5)	52760.07\r\n" + 
			"ATCS(k1=3.5;k2=1.0)	53275.76\r\n" + 
			"ATCS(k1=3.5;k2=1.5)	53664.66\r\n" + 
			"ATCS(k1=4.0;k2=1.0E-4)	51790.30\r\n" + 
			"ATCS(k1=4.0;k2=0.001)	52049.92\r\n" + 
			"ATCS(k1=4.0;k2=0.01)	52284.10\r\n" + 
			"ATCS(k1=4.0;k2=0.1)	52263.96\r\n" + 
			"ATCS(k1=4.0;k2=0.25)	52382.43\r\n" + 
			"ATCS(k1=4.0;k2=0.5)	52554.65\r\n" + 
			"ATCS(k1=4.0;k2=1.0)	53032.28\r\n" + 
			"ATCS(k1=4.0;k2=1.5)	53404.91\r\n" + 
			"ATCS(k1=4.5;k2=1.0E-4)	51716.30\r\n" + 
			"ATCS(k1=4.5;k2=0.001)	51998.58\r\n" + 
			"ATCS(k1=4.5;k2=0.01)	52224.70\r\n" + 
			"ATCS(k1=4.5;k2=0.1)	52170.27\r\n" + 
			"ATCS(k1=4.5;k2=0.25)	52301.60\r\n" + 
			"ATCS(k1=4.5;k2=0.5)	52411.41\r\n" + 
			"ATCS(k1=4.5;k2=1.0)	52867.78\r\n" + 
			"ATCS(k1=4.5;k2=1.5)	53132.95\r\n" + 
			"ATCS(k1=5.0;k2=1.0E-4)	51660.88\r\n" + 
			"ATCS(k1=5.0;k2=0.001)	51948.62\r\n" + 
			"ATCS(k1=5.0;k2=0.01)	52167.51\r\n" + 
			"ATCS(k1=5.0;k2=0.1)	52126.28\r\n" + 
			"ATCS(k1=5.0;k2=0.25)	52199.82\r\n" + 
			"ATCS(k1=5.0;k2=0.5)	52303.97\r\n" + 
			"ATCS(k1=5.0;k2=1.0)	52602.77\r\n" + 
			"ATCS(k1=5.0;k2=1.5)	52962.15\r\n" + 
			"ATCS(k1=5.5;k2=1.0E-4)	51627.37\r\n" + 
			"ATCS(k1=5.5;k2=0.001)	51863.63\r\n" + 
			"ATCS(k1=5.5;k2=0.01)	52111.52\r\n" + 
			"ATCS(k1=5.5;k2=0.1)	52116.11\r\n" + 
			"ATCS(k1=5.5;k2=0.25)	52110.83\r\n" + 
			"ATCS(k1=5.5;k2=0.5)	52174.63\r\n" + 
			"ATCS(k1=5.5;k2=1.0)	52519.06\r\n" + 
			"ATCS(k1=5.5;k2=1.5)	52773.16\r\n" + 
			"ATCS(k1=6.0;k2=1.0E-4)	51517.06\r\n" + 
			"ATCS(k1=6.0;k2=0.001)	51799.20\r\n" + 
			"ATCS(k1=6.0;k2=0.01)	52078.64\r\n" + 
			"ATCS(k1=6.0;k2=0.1)	52022.82\r\n" + 
			"ATCS(k1=6.0;k2=0.25)	52024.79\r\n" + 
			"ATCS(k1=6.0;k2=0.5)	52096.04\r\n" + 
			"ATCS(k1=6.0;k2=1.0)	52365.79\r\n" + 
			"ATCS(k1=6.0;k2=1.5)	52584.59\r\n" + 
			"ATCS(k1=6.5;k2=1.0E-4)	51512.90\r\n" + 
			"ATCS(k1=6.5;k2=0.001)	51755.37\r\n" + 
			"ATCS(k1=6.5;k2=0.01)	51993.17\r\n" + 
			"ATCS(k1=6.5;k2=0.1)	51938.76\r\n" + 
			"ATCS(k1=6.5;k2=0.25)	51939.60\r\n" + 
			"ATCS(k1=6.5;k2=0.5)	51966.85\r\n" + 
			"ATCS(k1=6.5;k2=1.0)	52178.45\r\n" + 
			"ATCS(k1=6.5;k2=1.5)	52407.44\r\n" + 
			"ATCS(k1=7.0;k2=1.0E-4)	51455.08\r\n" + 
			"ATCS(k1=7.0;k2=0.001)	51697.06\r\n" + 
			"ATCS(k1=7.0;k2=0.01)	51954.64\r\n" + 
			"ATCS(k1=7.0;k2=0.1)	51888.05\r\n" + 
			"ATCS(k1=7.0;k2=0.25)	51863.98\r\n" + 
			"ATCS(k1=7.0;k2=0.5)	51863.55\r\n" + 
			"ATCS(k1=7.0;k2=1.0)	52066.56\r\n" + 
			"ATCS(k1=7.0;k2=1.5)	52252.94\r\n" + 
			"ATCS(k1=8.0;k2=1.0E-4)	51336.05\r\n" + 
			"ATCS(k1=8.0;k2=0.001)	51605.96\r\n" + 
			"ATCS(k1=8.0;k2=0.01)	51868.79\r\n" + 
			"ATCS(k1=8.0;k2=0.1)	51842.69\r\n" + 
			"ATCS(k1=8.0;k2=0.25)	51777.38\r\n" + 
			"ATCS(k1=8.0;k2=0.5)	51753.08\r\n" + 
			"ATCS(k1=8.0;k2=1.0)	51842.84\r\n" + 
			"ATCS(k1=8.0;k2=1.5)	52015.98\r\n" + 
			"ATCS(k1=9.0;k2=1.0E-4)	51292.05\r\n" + 
			"ATCS(k1=9.0;k2=0.001)	51563.70\r\n" + 
			"ATCS(k1=9.0;k2=0.01)	51804.98\r\n" + 
			"ATCS(k1=9.0;k2=0.1)	51764.07\r\n" + 
			"ATCS(k1=9.0;k2=0.25)	51702.28\r\n" + 
			"ATCS(k1=9.0;k2=0.5)	51658.15\r\n" + 
			"ATCS(k1=9.0;k2=1.0)	51717.10\r\n" + 
			"ATCS(k1=9.0;k2=1.5)	51852.37\r\n" + 
			"ATCS(k1=10.0;k2=1.0E-4)	51221.23\r\n" + 
			"ATCS(k1=10.0;k2=0.001)	51467.69\r\n" + 
			"ATCS(k1=10.0;k2=0.01)	51720.78\r\n" + 
			"ATCS(k1=10.0;k2=0.1)	51716.64\r\n" + 
			"ATCS(k1=10.0;k2=0.25)	51607.31\r\n" + 
			"ATCS(k1=10.0;k2=0.5)	51499.34\r\n" + 
			"ATCS(k1=10.0;k2=1.0)	51558.85\r\n" + 
			"ATCS(k1=10.0;k2=1.5)	51683.51\r\n" + 
			"ATCS(k1=20.0;k2=1.0E-4)	50986.20\r\n" + 
			"ATCS(k1=20.0;k2=0.001)	51128.53\r\n" + 
			"ATCS(k1=20.0;k2=0.01)	51389.76\r\n" + 
			"ATCS(k1=20.0;k2=0.1)	51404.50\r\n" + 
			"ATCS(k1=20.0;k2=0.25)	51258.40\r\n" + 
			"ATCS(k1=20.0;k2=0.5)	50999.40\r\n" + 
			"ATCS(k1=20.0;k2=1.0)	50853.05\r\n" + 
			"ATCS(k1=20.0;k2=1.5)	50910.84\r\n" + 
			"ATCS(k1=50.0;k2=1.0E-4)	50763.10\r\n" + 
			"ATCS(k1=50.0;k2=0.001)	50777.09\r\n" + 
			"ATCS(k1=50.0;k2=0.01)	51045.63\r\n" + 
			"ATCS(k1=50.0;k2=0.1)	51133.25\r\n" + 
			"ATCS(k1=50.0;k2=0.25)	51004.86\r\n" + 
			"ATCS(k1=50.0;k2=0.5)	50587.05\r\n" + 
			"ATCS(k1=50.0;k2=1.0)	50318.07\r\n" + 
			"ATCS(k1=50.0;k2=1.5)	50323.53\r\n" + 
			"ATCS(k1=100.0;k2=1.0E-4)	50633.60\r\n" + 
			"ATCS(k1=100.0;k2=0.001)	50610.50\r\n" + 
			"ATCS(k1=100.0;k2=0.01)	50831.79\r\n" + 
			"ATCS(k1=100.0;k2=0.1)	50943.30\r\n" + 
			"ATCS(k1=100.0;k2=0.25)	50806.36\r\n" + 
			"ATCS(k1=100.0;k2=0.5)	50418.53\r\n" + 
			"ATCS(k1=100.0;k2=1.0)	50038.34\r\n" + 
			"ATCS(k1=100.0;k2=1.5)	50007.22\r\n" + 
			"MCB(0.0)	81854.33\r\n" + 
			"BOF	134450.18\r\n" + 
			"MBS(0.125)	197635.69\r\n" + 
			"MBS(0.25)	189991.67\r\n" + 
			"MBS(0.375)	173572.33\r\n" + 
			"MBS(0.5)	150978.42\r\n" + 
			"MBS(0.625)	127750.95\r\n" + 
			"MBS(0.75)	112974.93\r\n" + 
			"MBS(0.825)	104899.86\r\n" + 
			"MBS(1.0)	87759.20\r\n"; 
	
	private String[] twtSets; 
	private String twtStr="ATCS(k1=1.0E-4;k2=1.0E-4)	3471.76\r\n" + 
			"ATCS(k1=1.0E-4;k2=0.001)	3609.34\r\n" + 
			"ATCS(k1=1.0E-4;k2=0.01)	3705.35\r\n" + 
			"ATCS(k1=1.0E-4;k2=0.1)	3714.92\r\n" + 
			"ATCS(k1=1.0E-4;k2=0.25)	3703.55\r\n" + 
			"ATCS(k1=1.0E-4;k2=0.5)	3682.71\r\n" + 
			"ATCS(k1=1.0E-4;k2=1.0)	3872.74\r\n" + 
			"ATCS(k1=1.0E-4;k2=1.5)	4277.19\r\n" + 
			"ATCS(k1=0.001;k2=1.0E-4)	3471.84\r\n" + 
			"ATCS(k1=0.001;k2=0.001)	3470.49\r\n" + 
			"ATCS(k1=0.001;k2=0.01)	3596.96\r\n" + 
			"ATCS(k1=0.001;k2=0.1)	3687.50\r\n" + 
			"ATCS(k1=0.001;k2=0.25)	3693.98\r\n" + 
			"ATCS(k1=0.001;k2=0.5)	3683.56\r\n" + 
			"ATCS(k1=0.001;k2=1.0)	3863.08\r\n" + 
			"ATCS(k1=0.001;k2=1.5)	4280.27\r\n" + 
			"ATCS(k1=0.01;k2=1.0E-4)	3557.38\r\n" + 
			"ATCS(k1=0.01;k2=0.001)	3463.85\r\n" + 
			"ATCS(k1=0.01;k2=0.01)	3478.67\r\n" + 
			"ATCS(k1=0.01;k2=0.1)	3622.65\r\n" + 
			"ATCS(k1=0.01;k2=0.25)	3653.53\r\n" + 
			"ATCS(k1=0.01;k2=0.5)	3664.34\r\n" + 
			"ATCS(k1=0.01;k2=1.0)	3828.68\r\n" + 
			"ATCS(k1=0.01;k2=1.5)	4240.43\r\n" + 
			"ATCS(k1=0.1;k2=1.0E-4)	3596.52\r\n" + 
			"ATCS(k1=0.1;k2=0.001)	3562.17\r\n" + 
			"ATCS(k1=0.1;k2=0.01)	3454.08\r\n" + 
			"ATCS(k1=0.1;k2=0.1)	3447.01\r\n" + 
			"ATCS(k1=0.1;k2=0.25)	3519.20\r\n" + 
			"ATCS(k1=0.1;k2=0.5)	3543.63\r\n" + 
			"ATCS(k1=0.1;k2=1.0)	3651.90\r\n" + 
			"ATCS(k1=0.1;k2=1.5)	3925.98\r\n" + 
			"ATCS(k1=0.5;k2=1.0E-4)	3571.14\r\n" + 
			"ATCS(k1=0.5;k2=0.001)	3572.53\r\n" + 
			"ATCS(k1=0.5;k2=0.01)	3489.28\r\n" + 
			"ATCS(k1=0.5;k2=0.1)	3420.61\r\n" + 
			"ATCS(k1=0.5;k2=0.25)	3405.65\r\n" + 
			"ATCS(k1=0.5;k2=0.5)	3420.53\r\n" + 
			"ATCS(k1=0.5;k2=1.0)	3467.65\r\n" + 
			"ATCS(k1=0.5;k2=1.5)	3585.67\r\n" + 
			"ATCS(k1=1.0;k2=1.0E-4)	3545.48\r\n" + 
			"ATCS(k1=1.0;k2=0.001)	3533.11\r\n" + 
			"ATCS(k1=1.0;k2=0.01)	3476.38\r\n" + 
			"ATCS(k1=1.0;k2=0.1)	3387.03\r\n" + 
			"ATCS(k1=1.0;k2=0.25)	3373.74\r\n" + 
			"ATCS(k1=1.0;k2=0.5)	3367.27\r\n" + 
			"ATCS(k1=1.0;k2=1.0)	3390.16\r\n" + 
			"ATCS(k1=1.0;k2=1.5)	3476.22\r\n" + 
			"ATCS(k1=1.5;k2=1.0E-4)	3517.20\r\n" + 
			"ATCS(k1=1.5;k2=0.001)	3507.49\r\n" + 
			"ATCS(k1=1.5;k2=0.01)	3485.01\r\n" + 
			"ATCS(k1=1.5;k2=0.1)	3375.77\r\n" + 
			"ATCS(k1=1.5;k2=0.25)	3349.03\r\n" + 
			"ATCS(k1=1.5;k2=0.5)	3342.66\r\n" + 
			"ATCS(k1=1.5;k2=1.0)	3340.89\r\n" + 
			"ATCS(k1=1.5;k2=1.5)	3377.79\r\n" + 
			"ATCS(k1=2.0;k2=1.0E-4)	3505.20\r\n" + 
			"ATCS(k1=2.0;k2=0.001)	3484.95\r\n" + 
			"ATCS(k1=2.0;k2=0.01)	3475.00\r\n" + 
			"ATCS(k1=2.0;k2=0.1)	3391.13\r\n" + 
			"ATCS(k1=2.0;k2=0.25)	3338.30\r\n" + 
			"ATCS(k1=2.0;k2=0.5)	3323.73\r\n" + 
			"ATCS(k1=2.0;k2=1.0)	3319.67\r\n" + 
			"ATCS(k1=2.0;k2=1.5)	3326.66\r\n" + 
			"ATCS(k1=2.5;k2=1.0E-4)	3493.86\r\n" + 
			"ATCS(k1=2.5;k2=0.001)	3486.57\r\n" + 
			"ATCS(k1=2.5;k2=0.01)	3479.93\r\n" + 
			"ATCS(k1=2.5;k2=0.1)	3385.62\r\n" + 
			"ATCS(k1=2.5;k2=0.25)	3357.72\r\n" + 
			"ATCS(k1=2.5;k2=0.5)	3334.85\r\n" + 
			"ATCS(k1=2.5;k2=1.0)	3305.60\r\n" + 
			"ATCS(k1=2.5;k2=1.5)	3327.96\r\n" + 
			"ATCS(k1=3.0;k2=1.0E-4)	3497.64\r\n" + 
			"ATCS(k1=3.0;k2=0.001)	3514.14\r\n" + 
			"ATCS(k1=3.0;k2=0.01)	3479.93\r\n" + 
			"ATCS(k1=3.0;k2=0.1)	3391.25\r\n" + 
			"ATCS(k1=3.0;k2=0.25)	3358.62\r\n" + 
			"ATCS(k1=3.0;k2=0.5)	3343.68\r\n" + 
			"ATCS(k1=3.0;k2=1.0)	3320.33\r\n" + 
			"ATCS(k1=3.0;k2=1.5)	3345.78\r\n" + 
			"ATCS(k1=3.5;k2=1.0E-4)	3522.44\r\n" + 
			"ATCS(k1=3.5;k2=0.001)	3493.08\r\n" + 
			"ATCS(k1=3.5;k2=0.01)	3495.27\r\n" + 
			"ATCS(k1=3.5;k2=0.1)	3409.04\r\n" + 
			"ATCS(k1=3.5;k2=0.25)	3376.24\r\n" + 
			"ATCS(k1=3.5;k2=0.5)	3344.75\r\n" + 
			"ATCS(k1=3.5;k2=1.0)	3349.25\r\n" + 
			"ATCS(k1=3.5;k2=1.5)	3339.80\r\n" + 
			"ATCS(k1=4.0;k2=1.0E-4)	3523.25\r\n" + 
			"ATCS(k1=4.0;k2=0.001)	3502.62\r\n" + 
			"ATCS(k1=4.0;k2=0.01)	3487.58\r\n" + 
			"ATCS(k1=4.0;k2=0.1)	3444.92\r\n" + 
			"ATCS(k1=4.0;k2=0.25)	3381.89\r\n" + 
			"ATCS(k1=4.0;k2=0.5)	3368.78\r\n" + 
			"ATCS(k1=4.0;k2=1.0)	3363.94\r\n" + 
			"ATCS(k1=4.0;k2=1.5)	3348.92\r\n" + 
			"ATCS(k1=4.5;k2=1.0E-4)	3527.23\r\n" + 
			"ATCS(k1=4.5;k2=0.001)	3524.30\r\n" + 
			"ATCS(k1=4.5;k2=0.01)	3509.26\r\n" + 
			"ATCS(k1=4.5;k2=0.1)	3438.43\r\n" + 
			"ATCS(k1=4.5;k2=0.25)	3396.12\r\n" + 
			"ATCS(k1=4.5;k2=0.5)	3378.93\r\n" + 
			"ATCS(k1=4.5;k2=1.0)	3369.79\r\n" + 
			"ATCS(k1=4.5;k2=1.5)	3361.41\r\n" + 
			"ATCS(k1=5.0;k2=1.0E-4)	3551.39\r\n" + 
			"ATCS(k1=5.0;k2=0.001)	3542.35\r\n" + 
			"ATCS(k1=5.0;k2=0.01)	3533.34\r\n" + 
			"ATCS(k1=5.0;k2=0.1)	3469.12\r\n" + 
			"ATCS(k1=5.0;k2=0.25)	3412.38\r\n" + 
			"ATCS(k1=5.0;k2=0.5)	3407.39\r\n" + 
			"ATCS(k1=5.0;k2=1.0)	3381.46\r\n" + 
			"ATCS(k1=5.0;k2=1.5)	3385.88\r\n" + 
			"ATCS(k1=5.5;k2=1.0E-4)	3587.84\r\n" + 
			"ATCS(k1=5.5;k2=0.001)	3562.48\r\n" + 
			"ATCS(k1=5.5;k2=0.01)	3565.04\r\n" + 
			"ATCS(k1=5.5;k2=0.1)	3479.80\r\n" + 
			"ATCS(k1=5.5;k2=0.25)	3431.35\r\n" + 
			"ATCS(k1=5.5;k2=0.5)	3417.24\r\n" + 
			"ATCS(k1=5.5;k2=1.0)	3397.16\r\n" + 
			"ATCS(k1=5.5;k2=1.5)	3413.94\r\n" + 
			"ATCS(k1=6.0;k2=1.0E-4)	3590.58\r\n" + 
			"ATCS(k1=6.0;k2=0.001)	3560.27\r\n" + 
			"ATCS(k1=6.0;k2=0.01)	3560.11\r\n" + 
			"ATCS(k1=6.0;k2=0.1)	3497.82\r\n" + 
			"ATCS(k1=6.0;k2=0.25)	3465.84\r\n" + 
			"ATCS(k1=6.0;k2=0.5)	3433.16\r\n" + 
			"ATCS(k1=6.0;k2=1.0)	3426.81\r\n" + 
			"ATCS(k1=6.0;k2=1.5)	3425.87\r\n" + 
			"ATCS(k1=6.5;k2=1.0E-4)	3615.55\r\n" + 
			"ATCS(k1=6.5;k2=0.001)	3601.83\r\n" + 
			"ATCS(k1=6.5;k2=0.01)	3580.60\r\n" + 
			"ATCS(k1=6.5;k2=0.1)	3525.30\r\n" + 
			"ATCS(k1=6.5;k2=0.25)	3485.98\r\n" + 
			"ATCS(k1=6.5;k2=0.5)	3459.15\r\n" + 
			"ATCS(k1=6.5;k2=1.0)	3448.41\r\n" + 
			"ATCS(k1=6.5;k2=1.5)	3463.27\r\n" + 
			"ATCS(k1=7.0;k2=1.0E-4)	3622.26\r\n" + 
			"ATCS(k1=7.0;k2=0.001)	3611.90\r\n" + 
			"ATCS(k1=7.0;k2=0.01)	3611.57\r\n" + 
			"ATCS(k1=7.0;k2=0.1)	3534.91\r\n" + 
			"ATCS(k1=7.0;k2=0.25)	3503.07\r\n" + 
			"ATCS(k1=7.0;k2=0.5)	3472.56\r\n" + 
			"ATCS(k1=7.0;k2=1.0)	3457.20\r\n" + 
			"ATCS(k1=7.0;k2=1.5)	3475.20\r\n" + 
			"ATCS(k1=8.0;k2=1.0E-4)	3656.17\r\n" + 
			"ATCS(k1=8.0;k2=0.001)	3635.48\r\n" + 
			"ATCS(k1=8.0;k2=0.01)	3620.84\r\n" + 
			"ATCS(k1=8.0;k2=0.1)	3584.86\r\n" + 
			"ATCS(k1=8.0;k2=0.25)	3542.30\r\n" + 
			"ATCS(k1=8.0;k2=0.5)	3489.67\r\n" + 
			"ATCS(k1=8.0;k2=1.0)	3478.12\r\n" + 
			"ATCS(k1=8.0;k2=1.5)	3515.17\r\n" + 
			"ATCS(k1=9.0;k2=1.0E-4)	3666.09\r\n" + 
			"ATCS(k1=9.0;k2=0.001)	3651.43\r\n" + 
			"ATCS(k1=9.0;k2=0.01)	3646.72\r\n" + 
			"ATCS(k1=9.0;k2=0.1)	3587.49\r\n" + 
			"ATCS(k1=9.0;k2=0.25)	3544.49\r\n" + 
			"ATCS(k1=9.0;k2=0.5)	3512.14\r\n" + 
			"ATCS(k1=9.0;k2=1.0)	3522.10\r\n" + 
			"ATCS(k1=9.0;k2=1.5)	3536.16\r\n" + 
			"ATCS(k1=10.0;k2=1.0E-4)	3685.53\r\n" + 
			"ATCS(k1=10.0;k2=0.001)	3662.67\r\n" + 
			"ATCS(k1=10.0;k2=0.01)	3661.44\r\n" + 
			"ATCS(k1=10.0;k2=0.1)	3617.25\r\n" + 
			"ATCS(k1=10.0;k2=0.25)	3576.47\r\n" + 
			"ATCS(k1=10.0;k2=0.5)	3562.50\r\n" + 
			"ATCS(k1=10.0;k2=1.0)	3555.04\r\n" + 
			"ATCS(k1=10.0;k2=1.5)	3564.51\r\n" + 
			"ATCS(k1=20.0;k2=1.0E-4)	3761.36\r\n" + 
			"ATCS(k1=20.0;k2=0.001)	3765.27\r\n" + 
			"ATCS(k1=20.0;k2=0.01)	3742.01\r\n" + 
			"ATCS(k1=20.0;k2=0.1)	3716.93\r\n" + 
			"ATCS(k1=20.0;k2=0.25)	3682.11\r\n" + 
			"ATCS(k1=20.0;k2=0.5)	3651.72\r\n" + 
			"ATCS(k1=20.0;k2=1.0)	3662.22\r\n" + 
			"ATCS(k1=20.0;k2=1.5)	3696.40\r\n" + 
			"ATCS(k1=50.0;k2=1.0E-4)	3820.63\r\n" + 
			"ATCS(k1=50.0;k2=0.001)	3831.84\r\n" + 
			"ATCS(k1=50.0;k2=0.01)	3786.31\r\n" + 
			"ATCS(k1=50.0;k2=0.1)	3800.62\r\n" + 
			"ATCS(k1=50.0;k2=0.25)	3788.27\r\n" + 
			"ATCS(k1=50.0;k2=0.5)	3751.01\r\n" + 
			"ATCS(k1=50.0;k2=1.0)	3764.54\r\n" + 
			"ATCS(k1=50.0;k2=1.5)	3773.69\r\n" + 
			"ATCS(k1=100.0;k2=1.0E-4)	3864.31\r\n" + 
			"ATCS(k1=100.0;k2=0.001)	3843.65\r\n" + 
			"ATCS(k1=100.0;k2=0.01)	3825.90\r\n" + 
			"ATCS(k1=100.0;k2=0.1)	3828.93\r\n" + 
			"ATCS(k1=100.0;k2=0.25)	3799.69\r\n" + 
			"ATCS(k1=100.0;k2=0.5)	3753.80\r\n" + 
			"ATCS(k1=100.0;k2=1.0)	3809.87\r\n" + 
			"ATCS(k1=100.0;k2=1.5)	3823.96\r\n" + 
			"IGF[SST[LW[FCFS[TieBreakerFASFS]]]]	3922.88\r\n" + 
			"IGF[SST[LW[CR[TieBreakerFASFS]]]]	3897.40\r\n" + 
			"IGF[SST[WSPT[TieBreakerFASFS]]]	3895.70\r\n" + 
			"IGF[SST[WMDD[TieBreakerFASFS]]]	3522.23\r\n" + 
			"IGF[SST[WMOD[TieBreakerFASFS]]]	3496.07\r\n" + 
			"IGF[SST[LRM[TieBreakerFASFS]]]	5494.33\r\n" + 
			"IGF[SST[EDD[TieBreakerFASFS]]]	4169.99\r\n" + 
			"IGF[SST[ERD[TieBreakerFASFS]]]	4385.51\r\n" + 
			"IGF[SST[ODD[TieBreakerFASFS]]]	4155.31\r\n" + 
			"IGF[SST[SLK[TieBreakerFASFS]]]	4142.75\r\n" + 
			"IGF[LW[CR[TieBreakerFASFS]]]	10507.50\r\n" + 
			"IGF[WSPT[TieBreakerFASFS]]	10288.68\r\n" + 
			"IGF[LRM[TieBreakerFASFS]]	9452.14\r\n" + 
			"MCB(0.0)	3496.07\r\n" + 
			"MBS(0.75)	15093.27\r\n" + 
			"MBS(0.825)	10815.75\r\n";
}
