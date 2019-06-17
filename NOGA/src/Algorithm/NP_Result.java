package Algorithm;

import java.util.*;

 
public class NP_Result
{ 
	public int[] solution;
	public int fix;
	public double objvalue;
	public double estObj;
	public long time;
	public int regionIndex;
	
	public NP_Result(int n)
	{
		solution=new int[n];
		fix=0;
		objvalue=-1;
		regionIndex=-1;//-1 means surrounding region or root region
		estObj=Double.MAX_VALUE;
	}
}

