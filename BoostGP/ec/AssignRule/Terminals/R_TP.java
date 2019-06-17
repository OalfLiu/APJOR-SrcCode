package ec.AssignRule.Terminals;

import ec.*;
import ec.CDJSP.DoubleData;
import ec.gp.*;

public class R_TP extends GPNode
{
	public String toString() { return "R_TP"; }
	
	public int expectedChildren() { return 0; }

	public void eval(final EvolutionState state,
			final int thread,
			final GPData input,
			final ADFStack stack,
			final GPIndividual individual,
			final Problem problem)
	{
	}
	
	@Override
	public void evalSimple(final GPData input)
	{
		DoubleData rd = ((DoubleData)(input));		
		rd.x = rd.R_TP;
	}
}
