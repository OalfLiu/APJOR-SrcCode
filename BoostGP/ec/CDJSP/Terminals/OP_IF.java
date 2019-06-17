package ec.CDJSP.Terminals;

import ec.*;
import ec.CDJSP.DoubleData;
import ec.gp.*;

public class OP_IF extends GPNode
{
	public String toString() { return "ifte"; }
	
	public int expectedChildren() { return 3; }

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
		double firstParam;
		
		DoubleData rd = ((DoubleData)(input));

		children[0].evalSimple(input);
		firstParam = rd.x;
		
		if(firstParam >= 0)
		{
			children[1].evalSimple(input);			
		}
		else
		{
			children[2].evalSimple(input);
		}

		
	}
}
