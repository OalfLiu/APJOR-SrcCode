package ec.app.JSP;

import java.util.Arrays;
import java.util.Comparator;

import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.koza.KozaFitness;

public class FitnessComprator implements Comparator {
    public int compare(Object arg0, Object arg1) {
    	
    	try
		{
    		GPIndividual t1=(GPIndividual)arg0;
    		GPIndividual t2=(GPIndividual)arg1;
        	
        	if(t1.fitness == null && t2.fitness == null)
        		return 0;
        	
        	if(t1.fitness == null)
        		return -1;	    	
        	
        	if(t2.fitness == null)
        		return 1;
        
        	KozaFitness t1f = (KozaFitness)t1.fitness;
        	KozaFitness t2f = (KozaFitness)t2.fitness;
        	
        	if(t1f.summaryStat.mean() > t2f.summaryStat.mean())
        		return 1;
        	else if (t1f.summaryStat.mean() < t2f.summaryStat.mean())
        		return -1;
        	else return 0;
		}
		catch(Exception ex)
		{
			System.out.println(((Individual)arg0).fitness.fitness() + " " + ((Individual)arg1).fitness.fitness() );
			return -2;
		}
    	finally
    	{
    		
    	}
    	
    
    	
       
    }
}
