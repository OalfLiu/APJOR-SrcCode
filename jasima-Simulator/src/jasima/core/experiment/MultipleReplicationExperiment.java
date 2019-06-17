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
package jasima.core.experiment;

import jasima.core.statistics.SummaryStat;
import jasima.core.util.Pair;
import jasima.core.util.Util;
import jasima.shopSim.core.PR;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment;
import jasima.shopSim.prioRules.basic.FASFS;
import jasima.shopSim.prioRules.meta.IgnoreFutureJobs;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQPlusNPT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * <p>
 * Runs an arbitrary {@code baseExperiment} multiple times (determined by
 * {@code maxReplications}). All numeric results of the base experiment are
 * averaged over the runs, other result types are returned as an array
 * containing all values over the runs.
 * </p>
 * <p>
 * Optionally the maximum number of experiments run can be determined by a
 * confidence interval (t-test). To use this feature you have to tell
 * {@link #addConfIntervalMeasure(String)} which result(s) of the base
 * experiment to use.
 * </p>
 * <p>
 * In case of dynamic runs the following procedure is followed:
 * <ol>
 * <li>getMinReplications() replications are performed
 * <li>no further runs are performed if the confidence interval is less than a
 * certain allowance value
 * <ol>
 * <li>width of the confidence interval is determined by setErrorProb(double),
 * default is 0.05
 * <li>allowance value is computed by the runMean * allowancePercentage()
 * (default 1%)
 * </ol>
 * <li>if there is another run (i.e., confidence interval too large), another
 * batch of getMinReplications() is performed, i.e., go back to step 1
 * </ol>
 * 
 * @see OCBAExperiment
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class MultipleReplicationExperiment extends AbstractMultiExperiment {

	private static final long serialVersionUID = -5122164015247766742L;

	private Experiment baseExperiment;

	private int minReplications = 1;
	private int maxReplications = 10;
	
	public Boolean isEffective;

	private String[] confIntervalMeasures = {};
	private double errorProb = 0.05d;
	private double allowancePercentage = 0.01d;
	
	//protected List<DynamicShopExperiment> refExperiments;
	
	public MultipleReplicationExperiment() {
		super();

		setAbortUponBaseExperimentAbort(true);
		setCommonRandomNumbers(false);
	}
	
	protected long getExperimentSeed() {
		if (isCommonRandomNumbers())
			return getInitialSeed();
		else {
			if(this.maxReplications == 1)
			{
				return getInitialSeed();
			}
			else
			{
				if (seedStream == null)
					seedStream = new Random(getInitialSeed());
				
				long newseed = seedStream.nextLong();
				//System.out.println(newseed);
				return newseed;
			}
			
		
		}
	}

	public MultipleReplicationExperiment(Experiment e, int numReps) {
		this();
		setBaseExperiment(e);
		setMaxReplications(numReps);
	}

	@Override
	protected void createExperiments() {
		experiments.clear();

		int batchSize = isNumRunsDynamic() ? getMinReplications()
				: getMaxReplications();

		for (int i = 0; i < batchSize; i++) {
			Experiment e = getBaseExperiment().silentClone();
			configureRunExperiment(e);
			experiments.add(e);
			
//			//2017.11.2 we store reference experiment
//			DynamicShopExperiment refE = (DynamicShopExperiment)getBaseExperiment().silentClone();
//			PR sr = new PTPlusWINQPlusNPT();
//			PR sr2 = new IgnoreFutureJobs(sr);
//			PR sr3 = new FASFS();
//			sr2.setTieBreaker(sr3);
//			refE.setSequencingRule(sr2);
//			this.refExperiments.add(refE);
		}
	}

	@Override
	protected boolean hasMoreTasks() {
		if (!isNumRunsDynamic())
			return false;

		if (numTasksExecuted >= getMaxReplications())
			return false;

		// check all measures in "confIntervalMeasure" to have the quality
		// measured by "errorProb" and "allowancePercentage".
		for (String name : confIntervalMeasures) {
			@SuppressWarnings("unchecked")
			Pair<Boolean, SummaryStat> data = (Pair<Boolean, SummaryStat>) detailedResultsNumeric
					.get(name);
			SummaryStat vs = data == null ? null : data.b;
			if (vs == null)
				throw new RuntimeException(String.format(Util.DEF_LOCALE,
						"No results for name '%s'.", name));

			double allowance = Math.abs(vs.mean() * allowancePercentage);
			double interv = vs.confIntRangeSingle(getErrorProb());

			if (interv > allowance)
				return true;
		}

		return false;
	}

	@Override
	protected final String prefix() {
		return "rep";
	}

	@Override
	public int getNumExperiments() {
		if (isNumRunsDynamic())
			return getMinReplications();
		else
			return getMaxReplications();
	}

	private boolean isNumRunsDynamic() {
		return confIntervalMeasures.length > 0;
	}

	public int getMinReplications() {
		return minReplications;
	}

	/**
	 * <p>
	 * Sets the minimum number of replications to perform if the total number of
	 * replications is dynamic (i.e., if at least 1 result name is given in
	 * {@code confIntervalMeasure}).
	 * </p>
	 * <p>
	 * If the number of runs is not dynamic, this setting has no effect.
	 * </p>
	 * 
	 * @param minReplications
	 *            Minimum number of replications.
	 */
	public void setMinReplications(int minReplications) {
		if (minReplications <= 0)
			throw new IllegalArgumentException("" + minReplications);
		this.minReplications = minReplications;
	}

	public int getMaxReplications() {
		return maxReplications;
	}

	/**
	 * Sets the maximum number of replications to perform. If the number of runs
	 * is not dynamic, this sets the total number of replications to perform. If
	 * the total number of replications is dynamic (i.e., if at least 1 result
	 * name is given in {@code confIntervalMeasures}), this sets the maximum
	 * number of replications to perform.
	 * 
	 * @param maxReplications
	 *            The number of replications to perform.
	 */
	public void setMaxReplications(int maxReplications) {
		if (maxReplications <= 0 || maxReplications < getMinReplications())
			throw new IllegalArgumentException("" + maxReplications);
		this.maxReplications = maxReplications;
	}

	public double getErrorProb() {
		return errorProb;
	}

	/**
	 * <p>
	 * Sets the error probability used when computing the width of the
	 * confidence interval of {@code confIntervalMeasures}. The closer this
	 * setting is to 0, the more replications will be performed (with less
	 * uncertain results).
	 * </p>
	 * <p>
	 * This setting only has an effect if the total number of runs is dynamic.
	 * Its default value is 0.05.
	 * </p>
	 * 
	 * @param errorProb
	 *            Desired maximum error probability for computing the confidence
	 *            intervals.
	 */
	public void setErrorProb(double errorProb) {
		if (errorProb <= 0.0 || errorProb >= 1.0)
			throw new IllegalArgumentException(String.format(Util.DEF_LOCALE,
					"errorProb should be in the interval (0,1). invalid: %f",
					errorProb));
		this.errorProb = errorProb;
	}

	public double getAllowancePercentage() {
		return allowancePercentage;
	}

	/**
	 * <p>
	 * Sets the desired target quality of results as a percentage of the mean
	 * across all replications performed so far. Its default value is 0.01,
	 * i.e., 1%. This setting has no effect if the total number of runs is
	 * static.
	 * </p>
	 * <p>
	 * Let's say after 5 replications the observed mean of a result in
	 * {@code confIntervalMeasure} is 987.6 with some standard deviation (SD).
	 * Say this SD together with a certain {@code errorProb} leads to a
	 * confidence interval of &plusmn;10.2. No further replications are
	 * performed, if the total width of this interval (2*10.2=20.4) is smaller
	 * than the observed mean multiplied by {@code allowancePercentage}
	 * (987.6*0.01=9.876). In the example, the result is not yet precise enough,
	 * as the observed uncertainty is not smaller than the target value (20.4
	 * &gt; 9.876). Therefore further replications would be performed to further
	 * reduce the uncertainty of results.
	 * </p>
	 * 
	 * @param allowancePercentage
	 *            The desired maximum result uncertainty as a percentage of the
	 *            mean value.
	 */
	public void setAllowancePercentage(double allowancePercentage) {
		if (allowancePercentage <= 0.0 || allowancePercentage >= 1.0)
			throw new IllegalArgumentException(
					String.format(
							Util.DEF_LOCALE,
							"allowancePercentage should be in the interval (0,1). invalid: %f",
							allowancePercentage));
		this.allowancePercentage = allowancePercentage;
	}

	public void addConfIntervalMeasure(String name) {
		// temporarily convert to list
		ArrayList<String> list = new ArrayList<String>(
				Arrays.asList(confIntervalMeasures));
		list.add(name);
		// convert back to array
		confIntervalMeasures = list.toArray(new String[list.size()]);
	}

	public boolean removeConfIntervalMeasure(String name) {
		// temporarily convert to list
		ArrayList<String> list = new ArrayList<String>(
				Arrays.asList(confIntervalMeasures));
		boolean res = list.remove(name);
		// convert back to array
		confIntervalMeasures = list.toArray(new String[list.size()]);

		return res;
	}

	public String[] getConfIntervalMeasures() {
		return confIntervalMeasures;
	}

	/**
	 * Sets the list of results that will be used when the total number of
	 * replications to perform is dynamic. If any result name is given here as a
	 * {@code confIntervalMeasure}, then at least {@code minReplication}
	 * replications are performed. After this, additional replications are
	 * performed until all {@code confIntervalMeasure}s are precise enough or a
	 * total of {@code maxReplications} was performed. The meaning of
	 * "precise enough" is determined by the settings
	 * {@code allowancePercentage} and {@code errorProb}.
	 * 
	 * @param confIntervalMeasures
	 *            A list of all result names that should be checked when the
	 *            number of runs is dynamic.
	 */
	public void setConfIntervalMeasures(String[] confIntervalMeasures) {
		this.confIntervalMeasures = confIntervalMeasures;
	}

	public Experiment getBaseExperiment() {
		return baseExperiment;
	}

	/**
	 * Sets the base experiment that is executed multiple times in various
	 * configurations. Before experiment execution, a copy (clone) of
	 * {@code baseExperiment} is created and run. Therefore the specific
	 * experiment instance passed as the {@code baseExperiment} is never
	 * actually executed.
	 * 
	 * @param baseExperiment
	 *            The base experiment to use.
	 */
	public void setBaseExperiment(Experiment baseExperiment) {
		this.baseExperiment = baseExperiment;
		
		//2018.03.01
		if(baseExperiment instanceof DynamicShopExperiment)
			this.typeOfObjective = ((DynamicShopExperiment)baseExperiment).typeOfObjective;
	}

	@Override
	public MultipleReplicationExperiment clone()
			throws CloneNotSupportedException {
		MultipleReplicationExperiment mre = (MultipleReplicationExperiment) super
				.clone();

		if (confIntervalMeasures != null)
			mre.confIntervalMeasures = confIntervalMeasures.clone();

		if (baseExperiment != null)
			mre.baseExperiment = baseExperiment.clone();

		return mre;
	}

}
