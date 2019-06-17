package Algorithm;

import jasima.core.experiment.GP_OCBARun.ProblemType;
import jasima.core.statistics.SummaryStat;

public class Softmax {
	public double[] CalRatios(SummaryStat[] stats)
	{
		double[] eVi=new double [stats.length];
		double sumeVi=0.0;
		
		for(int i=0;i<stats.length;i++)
		{
			eVi[i]=Math.pow(Math.E, 14000/stats[i].mean());
			sumeVi+=eVi[i];
		} 
		double[] rtnRatios=new double[stats.length];
		 
		for(int i=0;i<stats.length;i++)
		{
			rtnRatios[i]=eVi[i]/sumeVi;
		}
		
		return rtnRatios;
	}
}
