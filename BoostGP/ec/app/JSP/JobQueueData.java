/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */


package ec.app.JSP;
import ec.app.JSP.Entities.Operation;
import ec.gp.*;
import jasima.shopSim.prioRules.gp.GPRuleBase;

public class JobQueueData extends GPData
{
	public double x;    // return value

	public Operation curOpeartion;
	
	public GPRuleBase curPR;

	public void copyTo(final GPData gpd)   // copy my stuff to another DoubleData
	{ 
		((JobQueueData)gpd).x = x; 
		((JobQueueData)gpd).curOpeartion = curOpeartion; 
		((JobQueueData)gpd).curPR = curPR; 
	}
}


