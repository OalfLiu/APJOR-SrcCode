/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */


package ec.CDJSP;
import ec.gp.*;

public class DoubleData extends GPData
{
	public double x;    // return value
	
	//Job sequencing terminals
	public double NPT;
	public double PT;
	public double WINQ;
	public double RPT;
	public double TIS;
	public double TIQ;
	public double OpsLeft;
	public double W;   //Priority/Weight
	public double SLACK;
	public double TD;
	public double ST;
	
	//Job sequencing terminals
	public double JS_APT;
	public double JS_OSLACK;
	public double JS_AST;
	public double JS_SFIQ;
	public double JS_STAvoid;
	
	
	//Batch forming terminals
	public double BF_JIQ;//sumjobsinqueue
	public double BF_JFIQ;//sumjobsinfaminqueue
	public double BF_SPT;//sumproctime
	public double BF_APT;//avgproctime 
	public double BF_MPT;//maxproctime
	public double BF_SQT;//sumqueuetime
	public double BF_AQT;//avgqueuetime
	public double BF_MQT;//maxqueuetime
	public double BF_BS;//batch size
	public double BF_BC;//batch capacity
	public double BF_FOB;//fullness of batch
	
	public double BF_MSlack;
	public double BF_ASlack;
	public double BF_MW;
	public double BF_AW;
	public double BF_OpPTLeft;
	
	public double BF_JSz;
	public double BF_JFSSz;
	public double BF_JFMSz;
	
	
	//RAP 2019-03-27
	public double M_qLen;
	public double M_util;
	public double M_capUtil;
	public double M_bSize;
	public double M_setup;
	public double M_qWait;
	public double M_procTime;
	public double R_TWT;
	public double R_WFT;
	public double R_TP;

	public void copyTo(final GPData gpd)   // copy my stuff to another DoubleData
	{ 
		((DoubleData)gpd).x = x; 
		
		((DoubleData)gpd).PT = PT; 
		((DoubleData)gpd).ST = ST; 
		((DoubleData)gpd).WINQ = WINQ; 
		((DoubleData)gpd).NPT = NPT; 
		((DoubleData)gpd).RPT = RPT; 
		((DoubleData)gpd).TIS = TIS; 
		((DoubleData)gpd).TIQ = TIQ; 
		((DoubleData)gpd).OpsLeft = OpsLeft; 	
		((DoubleData)gpd).W = W;
		((DoubleData)gpd).SLACK = SLACK;
		((DoubleData)gpd).TD = TD;
		
		((DoubleData)gpd).JS_APT = JS_APT;
		((DoubleData)gpd).JS_AST = JS_AST;
		((DoubleData)gpd).JS_OSLACK = JS_OSLACK; 
		
		
		((DoubleData)gpd).BF_JIQ = BF_JIQ; 
		((DoubleData)gpd).BF_JFIQ = BF_JFIQ; 
		((DoubleData)gpd).BF_SPT = BF_SPT; 
		((DoubleData)gpd).BF_APT = BF_APT; 
		((DoubleData)gpd).BF_MPT = BF_MPT; 
		((DoubleData)gpd).BF_SQT = BF_SQT; 
		((DoubleData)gpd).BF_AQT = BF_AQT; 
		((DoubleData)gpd).BF_MQT = BF_MQT;
		((DoubleData)gpd).BF_BS = BF_BS; 
		((DoubleData)gpd).BF_BC = BF_BC;
		((DoubleData)gpd).BF_FOB = BF_FOB;
	}
}


