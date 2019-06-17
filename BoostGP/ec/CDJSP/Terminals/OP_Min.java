package ec.CDJSP.Terminals;

import ec.*;
import ec.CDJSP.DoubleData;
import ec.gp.*;

public class OP_Min extends GPNode
{
	public String toString() { return "min"; }

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
		double result;
		DoubleData rd = ((DoubleData)(input));

		children[0].evalSimple(input);
		result = rd.x;

		children[1].evalSimple(input);
		rd.x = Math.min(result, rd.x);
	}
}
