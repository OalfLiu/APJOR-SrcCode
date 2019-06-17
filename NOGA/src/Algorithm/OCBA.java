package Algorithm;

import jasima.core.experiment.GP_OCBARun.ProblemType;
import jasima.core.statistics.SummaryStat;

public class OCBA {
	public double[] CalRatios(SummaryStat[] stats,int currBest, int add_budget) {
		final int nd = stats.length;
		add_budget=1000000;
		double t_s_mean[] = new double[nd];
		if (false) { /* MAX problem */
			for (int i = 0; i < nd; i++)				
				t_s_mean[i] = -stats[i].mean();
		} else { /* MIN problem */
			for (int i = 0; i < nd; i++)
				t_s_mean[i] = stats[i].mean();
		}

		int t_budget = add_budget;

		for (int i = 0; i < nd; i++)
			t_budget += stats[i].numObs();

		int b = currBest;
		int s = second_best(t_s_mean, b);

		double ratio[] = new double[nd];
		ratio[s] = 1.0d;
		for (int i = 0; i < nd; i++)
			if (i != s && i != b) {
				double temp = (t_s_mean[b] - t_s_mean[s]) / (t_s_mean[b] - t_s_mean[i]);
				ratio[i] = temp * temp * stats[i].variancePopulation() / stats[s].variancePopulation();
			} /* calculate ratio of Ni/Ns */

		double temp = 0.0;
		for (int i = 0; i < nd; i++)
			if (i != b)
				temp += (ratio[i] * ratio[i] / stats[i].variancePopulation());
		ratio[b] = Math.sqrt(stats[b].variancePopulation() * temp); /* calculate Nb */
		
		
		//return ratio;
		
		int morerun[] = new int[nd];
		for (int i = 0; i < nd; i++)
			morerun[i] = 1;

		int t1_budget = t_budget;

		int[] an = new int[nd];
		boolean more_alloc;
		do {
			more_alloc = false;
			double ratio_s = 0.0f;
			for (int i = 0; i < nd; i++)
				if (morerun[i] == 1)
					ratio_s += ratio[i];

			for (int i = 0; i < nd; i++)
				if (morerun[i] == 1) {
					an[i] = (int) (t1_budget / ratio_s * ratio[i]);
					/* disable thoese design which have been run too much */
					if (an[i] < stats[i].numObs()) {
						an[i] = stats[i].numObs();
						morerun[i] = 0;
						more_alloc = true;
					}
				}

			if (more_alloc) {
				t1_budget = t_budget;
				for (int i = 0; i < nd; i++)
					if (morerun[i] != 1)
						t1_budget -= an[i];
			}
		} while (more_alloc); /* end of WHILE */
		
		/* calculate the difference */
		t1_budget = an[0];

		for (int i = 1; i < nd; i++)
			t1_budget += an[i];

		an[currBest] += (t_budget - t1_budget); /* give the difference to design b */

		double[] rtnRatios=new double[nd];
		for (int i = 0; i < nd; i++)
		{
			an[i] -= stats[i].numObs();
			rtnRatios[i]=((double)an[i])/1000000.0;
		}
  
		return rtnRatios;
	}
	
	public static int[] CalAllocation(SummaryStat[] stats,int currBest,int add_budget) {
		final int nd = stats.length;

		double t_s_mean[] = new double[nd];
		
		for (int i = 0; i < nd; i++)
			t_s_mean[i] = stats[i].mean();

		int t_budget = add_budget;

		for (int i = 0; i < nd; i++)
			t_budget += stats[i].numObs();

		int b = currBest;
		int s = second_best(t_s_mean, b);

		double ratio[] = new double[nd];
		ratio[s] = 1.0d;
		for (int i = 0; i < nd; i++)
			if (i != s && i != b) {
				double temp = (t_s_mean[b] - t_s_mean[s]) / (t_s_mean[b] - t_s_mean[i]);
				ratio[i] = temp * temp * (stats[i].variance()==0?5000000:stats[i].variance()) / (stats[s].variance()==0?5000000:stats[s].variance());
			} /* calculate ratio of Ni/Ns */

		double temp = 0.0;
		for (int i = 0; i < nd; i++)
			if (i != b)
				temp += (ratio[i] * ratio[i] / (stats[i].variance()==0?5000000:stats[i].variance()));
		ratio[b] = Math.sqrt((stats[b].variance()==0?5000000:stats[b].variance()) * temp); /* calculate Nb */

		int morerun[] = new int[nd];
		for (int i = 0; i < nd; i++)
			morerun[i] = 1;

		int t1_budget = t_budget;

		int[] an = new int[nd];
		boolean more_alloc;
		do {
			more_alloc = false;
			double ratio_s = 0.0f;
			for (int i = 0; i < nd; i++)
				if (morerun[i] == 1)
					ratio_s += ratio[i];

			for (int i = 0; i < nd; i++)
				if (morerun[i] == 1) {
					an[i] = (int) (t1_budget / ratio_s * ratio[i]);
					/* disable thoese design which have been run too much */
					if (an[i] < stats[i].numObs()) {
						an[i] = stats[i].numObs();
						morerun[i] = 0;
						more_alloc = true;
					}
				}

			if (more_alloc) {
				t1_budget = t_budget;
				for (int i = 0; i < nd; i++)
					if (morerun[i] != 1)
						t1_budget -= an[i];
			}
		} while (more_alloc); /* end of WHILE */

		/* calculate the difference */
		t1_budget = an[0];

		for (int i = 1; i < nd; i++)
			t1_budget += an[i];

		an[b] += (t_budget - t1_budget); /* give the difference to design b */

		for (int i = 0; i < nd; i++)
			an[i] -= stats[i].numObs();

		return an;
	}
	
	/**
	 * This function determines the second best design based on current
	 * simulation results.
	 * 
	 * @param t_s_mean
	 *            [i]: temporary array for sample mean of design i,
	 *            i=0,1,..,ND-1
	 * @param b
	 *            : current best design design determined by function best()
	 */
	private static int second_best(final double t_s_mean[], int b) {
		int second_index = (b == 0) ? 1 : 0;

		for (int i = 0; i < t_s_mean.length; i++) {
			if (t_s_mean[i] < t_s_mean[second_index] && i != b) {
				second_index = i;
			}
		}

		return second_index;
	}

}
