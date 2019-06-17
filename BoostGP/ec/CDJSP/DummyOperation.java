package ec.CDJSP;

import ec.gp.GPIndividual;
import ec.gp.GPNode;
import jasima.core.random.continuous.DblConst;
import jasima.core.simulation.Simulation;
import jasima.core.simulation.Simulation.SimEvent;
import jasima.core.util.observer.NotifierListener;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment;
import jasima.shopSim.models.dynamicShop.DynamicShopExperiment.Scenario;
import jasima.shopSim.prioRules.gp.testGPRule;
import jasima.shopSim.util.BasicJobStatCollector;

import java.util.Random;

public class DummyOperation {
	double PT;
	double WINQ;
	double TIQ;
	double TIS;
	double RPT;
	double NPT;
	double OpsLeft;
	public long InitialSeed = 10000;
	public int Index;
	public double Priority;
	
	public DummyOperation(long seed)
	{
		InitialSeed = seed;	
		
		Random rd = new Random(InitialSeed);
		PT = rd.nextDouble() * 46 + 1;
		NPT = rd.nextDouble() * 47;
		WINQ = rd.nextDouble() * 410;
		RPT = rd.nextDouble() * 263 + 1;
		OpsLeft = rd.nextInt(11);
		TIQ = rd.nextDouble() * 1500;
		TIS = rd.nextDouble() * 2770;
	}
	
	public double calPriority(GPNode rootNode)
	{
		DoubleData input = new DoubleData();
		input.NPT = NPT;
		input.OpsLeft = OpsLeft;
		input.PT = PT;
		input.WINQ = WINQ;
		input.RPT = RPT;
		input.TIQ = TIQ;
		input.TIS = TIS;
		
		rootNode.evalSimple(input);

		return input.x;	
	}
}
