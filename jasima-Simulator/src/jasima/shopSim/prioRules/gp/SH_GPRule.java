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

import jasima.shopSim.core.PrioRuleTarget;
import jasima.shopSim.prioRules.basic.SLK;
import jasima.shopSim.prioRules.upDownStream.PTPlusWINQPlusNPT;

/**
 * A rule from "Towards Improved Dispatching Rules for Complex Shop Floor
 * Scenarios—a Genetic Programming Approach", Hildebrandt, Heger, Scholz-Reiter,
 * GECCO 2010, doi:10.1145/1830483.1830530
 * 
 * @author Torsten Hildebrandt
 * @version 
 *          "$Id$"
 */
public class SH_GPRule extends GPRuleBase {

	private static final long serialVersionUID = -6972361592426110350L;

	@Override
	public double calcPrio(PrioRuleTarget j) {
		double PT = j.getCurrentOperation().procTime;
		double ST = setupTime(j);
		double WINQ = jasima.shopSim.prioRules.upDownStream.WINQ.winq(j);
		double TIQ = j.getShop().simTime() - j.getArriveTime();
		double npt = PTPlusWINQPlusNPT.npt(j);
		double TIS = j.getShop().simTime() - j.getRelDate();
		double rpt = j.remainingProcTime();
		double SLACK = SLK.slack(j);
		double BC = j.getCurrentOperation().maxBatchSize;

		return ifte(ifte(div(div(div(WINQ, BC), max(TIS, 0.8094475039110729)), ifte(BC, PT, BC) + (ST - TIS)), ifte((TIS * WINQ) + ifte(TIQ, TIQ, TIQ), ifte(ifte(div(div(div(0.11250265755362077, BC), ifte(TIQ, TIQ, PT)), ifte(BC, PT, BC) + (ST - TIS)), ifte((TIS * WINQ) + ifte(TIQ, TIQ, TIQ), max(WINQ, 0.8344983376529795) - (TIQ * TIS), ifte(0.13745494998562302 + BC, max(TIQ, -0.14434056768525383), WINQ * SLACK)), ((ifte(WINQ, ifte(SLACK, 0.4235983174910116, TIQ), 0.9595826700515864 - TIS) + TIQ) * ifte(ST, SLACK, 0.7378243241135864)) + ifte(ifte(PT, ST, TIQ), PT - SLACK, max(PT, WINQ))), (max(max(WINQ, TIS), max(TIQ, SLACK)) - div(div(WINQ, BC), max(TIS, 0.8094475039110729))) + (max(ST * SLACK, max(TIQ, SLACK)) - ((PT * SLACK) - ifte(BC, SLACK, WINQ))), ifte((-0.4297211305275279 - ST) + (TIS * BC), ifte(ST * -0.3029268745446243, max(WINQ, TIQ), 0.9405912747235021 * TIQ), (-0.21519542801200897 + BC) + max(ST, TIQ)) - div(div(WINQ, BC), max(TIS, 0.8094475039110729))) - (TIQ * TIS), ifte(0.13745494998562302 + BC, max(TIQ, -0.14434056768525383), WINQ * SLACK)), ((ifte(WINQ, ifte(SLACK, 0.4235983174910116, TIQ), 0.9595826700515864 - TIS) + TIQ) * ifte(ST, SLACK, 0.7378243241135864)) + ifte(ifte(PT, ST, TIQ), PT - SLACK, max(PT, WINQ))), (max(max(WINQ, TIS), max(TIQ, SLACK)) - div(div(WINQ, BC), max(TIS, 0.8094475039110729))) + (max(ST * SLACK, max(TIQ, SLACK)) - ((PT * SLACK) - ifte(BC, SLACK, WINQ))), ifte((-0.4297211305275279 - ST) + (TIS * BC), ifte(ST * -0.3029268745446243, max(WINQ, TIQ), 0.9405912747235021 * TIQ), (-0.21519542801200897 + BC) + max(ST, TIQ)) - div(div(WINQ, BC), max(TIS, 0.8094475039110729)));
	}
	
	private double setupTime(PrioRuleTarget j) {
		//assert j.getCurrMachine() == getOwner();
		final double[][] setupMatrix = j.getCurrMachine().getSetupMatrix();
		final int machineSetup = j.getCurrMachine().currMachine.setupState;

		return setupMatrix[machineSetup][j.getCurrentOperation().setupState];
	}

}
