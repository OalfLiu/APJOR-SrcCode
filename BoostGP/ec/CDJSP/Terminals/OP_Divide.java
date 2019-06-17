package ec.CDJSP.Terminals;

import ec.*;
import ec.CDJSP.*;
import ec.gp.*;

public class OP_Divide extends GPNode
{
	public String toString() { return "div"; }

	public int expectedChildren() { return 2; }

	public void eval(final EvolutionState state,
			final int thread,
			final GPData input,
			final ADFStack stack,
			final GPIndividual individual,
			final Problem problem)
	{

		double result;
		DoubleData rd = ((DoubleData)(input));

		children[0].eval(state,thread,input,stack,individual,problem);
		result = rd.x;

		children[1].eval(state,thread,input,stack,individual,problem);
		
		if(rd.x != 0)
		{
			rd.x = result / rd.x;
		}
		
	}
	
	@Override
	public void evalSimple(final GPData input)
	{
		double result;
		DoubleData rd = ((DoubleData)(input));

		children[0].evalSimple(input);
		result = rd.x;

		children[1].evalSimple(input);
		
		if(rd.x != 0)
		{
			rd.x = result / rd.x;
		}
		else
			rd.x = 1.0d;
	}
}
