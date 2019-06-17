package ec.app.JSP;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.*;
import ec.util.*;

public class DoubleERC extends ERC {

	public double value;

	public String toStringForHumans()
	{ return "" + value; }

	public int nodeHashCode()
	{
		// a reasonable hash code
		long l = Double.doubleToLongBits(value);
		int iUpper = (int)(l & 0x00000000FFFFFFFF);
		int iLower = (int)(l >>> 32);
		return this.getClass().hashCode() + iUpper + iLower;
	}

	public void resetNode(final EvolutionState state, final int thread)
	{ value = state.random[thread].nextDouble() * 2 - 1.0; }

	public void mutateNode(EvolutionState state, int thread) {
		double v;
		do v = value + state.random[thread].nextGaussian() * 0.01;
		while( v <= -1.0 || v >= 1.0 );
		value = v;
		}

	public boolean nodeEquals(final GPNode node)
	{
		// check first to see if we're the same kind of ERC -- 
		// won't work for subclasses; in that case you'll need
		// to change this to isAssignableTo(...)
		if (this.getClass() != node.getClass()) return false;
		// now check to see if the ERCs hold the same value
		return (((DoubleERC)node).value == value);
	}

	public String encode() { return Code.encode(value); }


	public void eval(EvolutionState state, int thread, GPData input, ADFStack stack,
			GPIndividual individual, Problem Problem)
	{ 
		((DoubleData)input).x = value; 
	}
	
	@Override
	public void evalSimple(final GPData input)
	{
		DoubleData rd = ((DoubleData)(input));
		rd.x = value; 
	}

}
