package Algorithm;

import jasima.core.experiment.GP_OCBARun.ProblemType;
import jasima.core.statistics.SummaryStat;

public class FitnessDepRatioAllocate {
	public double[] CalRatios(SummaryStat[] stats)
	{
		double sum=0;
		double[] rtnRatios=new double[stats.length];
		for(int i=0;i<stats.length;i++)
		{
			sum+=1/Math.pow(stats[i].mean(), 2);
		}
		
		for(int i=0;i<stats.length;i++)
		{
			rtnRatios[i]=(1/Math.pow(stats[i].mean(), 2))/sum;
		}
		
		return rtnRatios;
	}
}
