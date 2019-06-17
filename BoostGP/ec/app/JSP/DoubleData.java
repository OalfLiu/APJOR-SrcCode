/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */


package ec.app.JSP;
import ec.gp.*;

public class DoubleData extends GPData
{
	public double x;    // return value
	
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

	public void copyTo(final GPData gpd)   // copy my stuff to another DoubleData
	{ 
		((DoubleData)gpd).x = x; 
		((DoubleData)gpd).PT = PT; 
		((DoubleData)gpd).WINQ = WINQ; 
		((DoubleData)gpd).NPT = NPT; 
		((DoubleData)gpd).RPT = RPT; 
		((DoubleData)gpd).TIS = TIS; 
		((DoubleData)gpd).TIQ = TIQ; 
		((DoubleData)gpd).OpsLeft = OpsLeft; 	
		((DoubleData)gpd).W = W;
		((DoubleData)gpd).SLACK = SLACK;
		((DoubleData)gpd).TD = TD;
	}
}


