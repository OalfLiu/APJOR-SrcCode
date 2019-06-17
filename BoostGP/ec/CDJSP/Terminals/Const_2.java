package ec.CDJSP.Terminals;

import ec.*;
import ec.gp.*;
import ec.CDJSP.DoubleData;

public class Const_2 extends GPNode
{
	public String toString() { return "2"; }
	
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
		rd.x = 2;
	}
}
