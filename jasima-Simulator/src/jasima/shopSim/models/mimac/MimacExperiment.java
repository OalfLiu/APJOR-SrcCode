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
package jasima.shopSim.models.mimac;

import jasima.core.random.continuous.DblDistribution;
import jasima.core.random.continuous.DblStream;
import jasima.core.simulation.arrivalprocess.ArrivalsStationary;
import jasima.shopSim.core.DynamicJobSource;
import jasima.shopSim.core.JobShopExperiment;
import jasima.shopSim.util.TextFileReader;
import jasima.shopSim.util.modelDef.ShopDef;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.annotation.Resource;

import org.apache.commons.math3.distribution.ExponentialDistribution;
 

/**
 * Implements simulations of the MIMAC Scenarios.
 * 
 * @author Torsten Hildebrandt, 2010-03-12
 * @version 
 *          "$Id$"
 */
public class MimacExperiment extends JobShopExperiment {

	private static final long serialVersionUID = -1460963355772995049L;

	public static enum DataSet {
		FAB4("fab4.txt",
				new double[] { 1440d / 4.07999048, 1440d / 0.447999403 }), 
		FAB4r(
				"fab4r.txt", new double[] { 1440d / 4.07999048,
						1440d / 0.447999403 }), 
		FAB6("fab6.txt", new double[] {
				1440d / 0.7875, 1440d / 0.258125, 1440d / 0.54641667,
				1440d / 0.65891667, 1440d / 2.196375, 1440d / 1.39279167,
				1440d / 0.58391667, 1440d / 0.50358333, 1440d / 0.68033333 }),
		SH35("SH35.txt",  new double[] {1440d/30.89310456862,1440d/12.41029411764}),
		SH95("SH95.txt",  new double[] {1440d/11.374733,1440d/11.042468,1440d/8.598626,1440d/5.960560,1440d/5.112799,1440d/5.099033,1440d/4.085598,1440d/3.591552,1440d/3.548168,1440d/3.210840,1440d/2.563053,1440d/2.446514,1440d/2.391858,1440d/2.323868,1440d/2.226387,1440d/2.123435,1440d/2.062977,1440d/1.528244,1440d/1.478855,1440d/1.400992,1440d/1.301730,1440d/1.287099,1440d/1.186768,1440d/1.151221,1440d/1.078244,1440d/1.055165,1440d/1.003817,1440d/1.001399,1440d/0.969211,1440d/0.965547,1440d/0.936209,1440d/0.928982,1440d/0.862799,1440d/0.836361,1440d/0.685700,1440d/0.678117,1440d/0.677201,1440d/0.633562,1440d/0.614478,1440d/0.606209,1440d/0.571120,1440d/0.569466,1440d/0.543053,1440d/0.537252,1440d/0.536616,1440d/0.526336,1440d/0.516310,1440d/0.510941,1440d/0.509059,1440d/0.506972,1440d/0.500153,1440d/0.498142,1440d/0.463486,1440d/0.446183,1440d/0.444606,1440d/0.435878,1440d/0.433817,1440d/0.432061,1440d/0.431196,1440d/0.430204,1440d/0.415649,1440d/0.414758,1440d/0.404987,1440d/0.402799,1440d/0.400433,1440d/0.396438,1440d/0.391527,1440d/0.372774,1440d/0.368728,1440d/0.365547,1440d/0.352163,1440d/0.344453,1440d/0.343003,1440d/0.338524,1440d/0.337379,1440d/0.330967,1440d/0.328244,1440d/0.317176,1440d/0.302595,1440d/0.299211,1440d/0.295751,1440d/0.289567,1440d/0.280254,1440d/0.279720,1440d/0.267176,1440d/0.264402,1440d/0.258066,1440d/0.256183,1440d/0.253893,1440d/0.252137,1440d/0.247506,1440d/0.238575,1440d/0.232774,1440d/0.195165,1440d/0.194453,1440d/0.191120,1440d/0.190433,1440d/0.190204}),
		SH75("SH75.txt",  new double[] {1440d/11.374733,1440d/11.042468,1440d/8.598626,1440d/5.960560,1440d/5.112799,1440d/5.099033,1440d/4.085598,1440d/3.591552,1440d/3.548168,1440d/3.210840,1440d/2.563053,1440d/2.446514,1440d/2.391858,1440d/2.323868,1440d/2.226387,1440d/2.123435,1440d/2.062977,1440d/1.528244,1440d/1.478855,1440d/1.400992,1440d/1.301730,1440d/1.287099,1440d/1.186768,1440d/1.151221,1440d/1.078244,1440d/1.055165,1440d/1.003817,1440d/1.001399,1440d/0.969211,1440d/0.965547,1440d/0.936209,1440d/0.928982,1440d/0.862799,1440d/0.836361,1440d/0.685700,1440d/0.678117,1440d/0.677201,1440d/0.633562,1440d/0.614478,1440d/0.606209,1440d/0.571120,1440d/0.569466,1440d/0.543053,1440d/0.537252,1440d/0.536616,1440d/0.526336,1440d/0.516310,1440d/0.510941,1440d/0.509059,1440d/0.506972,1440d/0.500153,1440d/0.498142,1440d/0.463486,1440d/0.446183,1440d/0.444606,1440d/0.435878,1440d/0.433817,1440d/0.432061,1440d/0.431196,1440d/0.430204,1440d/0.415649,1440d/0.414758,1440d/0.404987,1440d/0.402799,1440d/0.400433,1440d/0.396438,1440d/0.391527,1440d/0.372774,1440d/0.368728,1440d/0.365547,1440d/0.352163,1440d/0.344453,1440d/0.343003,1440d/0.338524,1440d/0.337379,1440d/0.330967,1440d/0.328244,1440d/0.317176,1440d/0.302595,1440d/0.299211,1440d/0.295751,1440d/0.289567,1440d/0.280254,1440d/0.279720,1440d/0.267176,1440d/0.264402,1440d/0.258066,1440d/0.256183,1440d/0.253893,1440d/0.252137,1440d/0.247506,1440d/0.238575,1440d/0.232774,1440d/0.195165,1440d/0.194453,1440d/0.191120,1440d/0.190433,1440d/0.190204});
//		SH75("SH75.txt",  new double[] {1440d/7.842579,1440d/7.613491,1440d/5.928526,1440d/4.109649,1440d/3.525140,1440d/3.515649,1440d/2.816912,1440d/2.476281,1440d/2.446368,1440d/2.213789,1440d/1.767158,1440d/1.686807,1440d/1.649123,1440d/1.602246,1440d/1.535035,1440d/1.464053,1440d/1.422368,1440d/1.053684,1440d/1.019632,1440d/0.965947,1440d/0.897509,1440d/0.887421,1440d/0.818246,1440d/0.793737,1440d/0.743421,1440d/0.727509,1440d/0.692105,1440d/0.690439,1440d/0.668246,1440d/0.665719,1440d/0.645491,1440d/0.640509,1440d/0.594877,1440d/0.576649,1440d/0.472772,1440d/0.467544,1440d/0.466912,1440d/0.436825,1440d/0.423667,1440d/0.417965,1440d/0.393772,1440d/0.392632,1440d/0.374421,1440d/0.370421,1440d/0.369982,1440d/0.362895,1440d/0.355982,1440d/0.352281,1440d/0.350982,1440d/0.349544,1440d/0.344842,1440d/0.343456,1440d/0.319561,1440d/0.307632,1440d/0.306544,1440d/0.300526,1440d/0.299105,1440d/0.297895,1440d/0.297298,1440d/0.296614,1440d/0.286579,1440d/0.285965,1440d/0.279228,1440d/0.277719,1440d/0.276088,1440d/0.273333,1440d/0.269947,1440d/0.257018,1440d/0.254228,1440d/0.252035,1440d/0.242807,1440d/0.237491,1440d/0.236491,1440d/0.233404,1440d/0.232614,1440d/0.228193,1440d/0.226316,1440d/0.218684,1440d/0.208632,1440d/0.206298,1440d/0.203912,1440d/0.199649,1440d/0.193228,1440d/0.192860,1440d/0.184211,1440d/0.182298,1440d/0.177930,1440d/0.176632,1440d/0.175053,1440d/0.173842,1440d/0.170649,1440d/0.164491,1440d/0.160491,1440d/0.134561,1440d/0.134070,1440d/0.131772,1440d/0.131298,1440d/0.131140});
		

		public final String resourceName;
		public final double[] defaultIats;

		private ShopDef def = null;

		DataSet(String resoureName, double[] defaultIats) {
			this.resourceName = resoureName;
			this.defaultIats = defaultIats;
		}

		public synchronized ShopDef getShopDef() {
			ShopDef def = this.def;

			if (def == null) {
				TextFileReader reader = createNewReader();
				def = reader.getShopDef();
			}

			return def;
		}

		private TextFileReader createNewReader() {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();

			String name = resourceName;
			String baseName = MimacExperiment.class.getName();
			int index = baseName.lastIndexOf('.');
			if (index != -1) { 
				String protocol =  cl.getResource("").getProtocol();
				//System.out.print("protocol is :"+protocol);
				
				if("file".equals(protocol))
				{
					//name = baseName.substring(0, index).replace('.', '/') + "/"	+ name;
				}
				else 
				{
				    // jar 中
					name = "/src/"+baseName.substring(0, index).replace('.', '/') + "/"
							+ name;
				}
				
			}
			
			InputStream inp = this.getClass().getResourceAsStream(name);
			if (inp == null)
				throw new RuntimeException("Can't find input stream '" + name
						+ "'.");
	 
			BufferedReader in = new BufferedReader(new InputStreamReader(inp));
			TextFileReader reader = new TextFileReader();
			reader.readData(in);

			try {
				in.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			return reader;
		}

	};

	private DataSet scenario;
	private DblStream jobWeights;
	private DblStream dueDateFactors;
	private DblStream[] interArrivalTimes;
	private boolean arrivalAtTimeZero = false;

	@Override
	protected void configureShop() {
		// configure model from file
		getScenario().getShopDef().getShopConfigurator().configureMdl(shop);

		super.configureShop();

		// create job sources
		DblStream[] iats = getInterArrivalTimes();
		if (iats != null && shop.routes.length != iats.length) {
			throw new RuntimeException("Number of routes ("
					+ shop.routes.length + ") and inter-arrival streams ("
					+ iats.length + ") doesn't match.");
		}

		// create job sources for each route, i.e., product
		for (int i = 0; i < shop.routes.length; i++) {
			DynamicJobSource s = new DynamicJobSource();
			s.setRoute(shop.routes[i]);

			ArrivalsStationary arrivals = new ArrivalsStationary();
			arrivals.setArrivalAtTimeZero(isArrivalAtTimeZero());
			if (iats != null && iats[i] != null) {
				arrivals.setInterArrivalTimes(iats[i]);
			} else {
				arrivals.setInterArrivalTimes(new DblDistribution(
						new ExponentialDistribution(
								getScenario().defaultIats[i])));
			}
			s.setArrivalProcess(arrivals);

			if (getDueDateFactors() != null)
				s.setDueDateFactors(getDueDateFactors());

			if (getJobWeights() != null)
				s.setJobWeights(getJobWeights());

			shop.addJobSource(s);
		}
	}

	@Override
	public MimacExperiment clone() throws CloneNotSupportedException {
		MimacExperiment c = (MimacExperiment) super.clone();

		if (jobWeights != null)
			c.jobWeights = jobWeights.clone();

		if (dueDateFactors != null)
			c.dueDateFactors = dueDateFactors.clone();

		if (interArrivalTimes != null) {
			c.interArrivalTimes = new DblStream[interArrivalTimes.length];
			for (int i = 0; i < interArrivalTimes.length; i++) {
				c.interArrivalTimes[i] = interArrivalTimes[i].clone();
			}
		}

		return c;
	}

	//
	//
	// boring getters and setters for parameters below
	//
	//

	public void setScenario(DataSet scenario) {
		this.scenario = scenario;
	}

	public DataSet getScenario() {
		return scenario;
	}

	public void setInterArrivalTimes(DblStream[] interArrivalTimes) {
		this.interArrivalTimes = interArrivalTimes;
	}

	public DblStream[] getInterArrivalTimes() {
		return interArrivalTimes;
	}

	public void setJobWeights(DblStream jobWeights) {
		this.jobWeights = jobWeights;
	}

	public DblStream getJobWeights() {
		return jobWeights;
	}

	public void setDueDateFactors(DblStream dueDateFactors) {
		this.dueDateFactors = dueDateFactors;
	}

	public DblStream getDueDateFactors() {
		return dueDateFactors;
	}

	public void setArrivalAtTimeZero(boolean arrivalAtTimeZero) {
		this.arrivalAtTimeZero = arrivalAtTimeZero;
	}

	public boolean isArrivalAtTimeZero() {
		return arrivalAtTimeZero;
	}

}
