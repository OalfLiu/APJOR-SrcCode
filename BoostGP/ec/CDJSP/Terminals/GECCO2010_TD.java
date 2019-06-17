package ec.CDJSP.Terminals;

import ec.*;
import ec.CDJSP.DoubleData;
import ec.gp.*;

public class GECCO2010_TD extends GPNode
{
	public String toString() { return "TD"; }
	
	public int expectedChildren() { return 0; }

	public void eval(final EvolutionState state,
			final int thread,
			final GPData input,
			final ADFStack stack,
			final GPIndividual individual,
			final Problem problem)
	{
		DoubleData rd = ((DoubleData)(input));		
		rd.x = rd.TD;
	}
	
	@Override
	public void evalSimple(final GPData input)
	{
		DoubleData rd = ((DoubleData)(input));		
		rd.x = rd.TD;
	}
}
