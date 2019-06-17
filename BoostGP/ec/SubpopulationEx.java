/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/



package ec;
import java.util.*;
import java.io.*;

import ec.app.JSP.DummyOperationWrapper;
import ec.app.JSP.DuplicateRemovalWrapper;
import ec.gp.GPIndividual;
import ec.util.*;
import jasima.core.util.Util.TypeOfObjective;

/* 
 * Subpopulation.java
 * 
 * Created: Tue Aug 10 20:34:14 1999
 * By: Sean Luke
 */

/**
 * Subpopulation is a group which is basically an array of Individuals.
 * There is always one or more Subpopulations in the Population.  Each
 * Subpopulation has a Species, which governs the formation of the Individuals
 * in that Subpopulation.  Subpopulations also contain a Fitness prototype
 * which is cloned to form Fitness objects for individuals in the subpopulation.
 *
 * <p>An initial subpopulation is populated with new random individuals 
 * using the populate(...) method.  This method typically populates
 * by filling the array with individuals created using the Subpopulations' 
 * species' emptyClone() method, though you might override this to create
 * them with other means, by loading from text files for example.
 *
 * <p>In a multithreaded area of a run, Subpopulations should be considered
 * immutable.  That is, once they are created, they should not be modified,
 * nor anything they contain.  This protocol helps ensure read-safety under
 * multithreading race conditions.
 *

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>size</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(total number of individuals in the subpopulation)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>species</tt><br>
 <font size=-1>classname, inherits and != ec.Species</font></td>
 <td valign=top>(the class of the subpopulations' Species)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>fitness</tt><br>
 <font size=-1>classname, inherits and != ec.Fitness</font></td>
 <td valign=top>(the class for the prototypical Fitness for individuals in this subpopulation)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>file</tt><br>
 <font size=-1>String</font></td>
 <td valign=top>(pathname of file from which the population is to be loaded.  If not defined, or empty, then the population will be initialized at random in the standard manner)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>duplicate-retries</tt><br>
 <font size=-1>int &gt;= 0</font></td>
 <td valign=top>(during initialization, when we produce an individual which already exists in the subpopulation, the number of times we try to replace it with something unique.  Ignored if we're loading from a file.)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 ec.subpop

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>species</tt></td>
 <td>species (the subpopulations' species)</td></tr>

 </table>


 * @author Sean Luke
 * @version 1.0 
 */


public class SubpopulationEx extends Subpopulation
    {

    public void populate(EvolutionState state, int thread)
        {
        int len = individuals.length;           // original length of individual array
        int start = 0;                                          // where to start filling new individuals in -- may get modified if we read some individuals in
        
        // should we load individuals from a file? -- duplicates are permitted
        if (loadInds)
            {
            InputStream stream = state.parameters.getResource(file,null);
            if (stream == null)
                state.output.fatal("Could not load subpopulation from file", file);
            
            try { readSubpopulation(state, new LineNumberReader(new InputStreamReader(stream))); }
            catch (IOException e) { state.output.fatal("An IOException occurred when trying to read from the file " + state.parameters.getString(file, null) + ".  The IOException was: \n" + e,
                    file, null); }
            
            if (len < individuals.length)
                {
                state.output.message("Old subpopulation was of size " + len + ", expanding to size " + individuals.length);
                return;
                }
            else if (len > individuals.length)   // the population was shrunk, there's more space yet
                {
                // What do we do with the remainder?
                if (extraBehavior == TRUNCATE)
                    {
                    state.output.message("Old subpopulation was of size " + len + ", truncating to size " + individuals.length);
                    return;  // we're done
                    }
                else if (extraBehavior == WRAP)
                    {
                    state.output.message("Only " + individuals.length + " individuals were read in.  Subpopulation will stay size " + len + 
                        ", and the rest will be filled with copies of the read-in individuals.");
                        
                    Individual[] oldInds = individuals;
                    individuals = new Individual[len];
                    System.arraycopy(oldInds, 0, individuals, 0, oldInds.length);
                    start = oldInds.length;
                                
                    int count = 0;
                    for(int i = start; i < individuals.length; i++)
                        {
                        individuals[i] = (Individual)(individuals[count].clone());
                        if (++count >= start) count = 0;
                        }
                    return;
                    }
                else // if (extraBehavior == FILL)
                    {
                    state.output.message("Only " + individuals.length + " individuals were read in.  Subpopulation will stay size " + len + 
                        ", and the rest will be filled using randomly generated individuals.");
                        
                    Individual[] oldInds = individuals;
                    individuals = new Individual[len];
                    System.arraycopy(oldInds, 0, individuals, 0, oldInds.length);
                    start = oldInds.length;
                    // now go on to fill the rest below...
                    }                       
                }
            else // exactly right number, we're dont
                {
                return;
                }
            }

        // populating the remainder with random individuals
        HashMap h = null;
        if (numDuplicateRetries >= 1)
            h = new HashMap((individuals.length - start) / 2);  // seems reasonable

        DummyOperationWrapper doWrapper = new DummyOperationWrapper();
        
//        DuplicateRemovalWrapper drWrapper = new DuplicateRemovalWrapper();
//        drWrapper.init();
        
        doWrapper.Normalization = state.parameters.getIntWithDefault(new Parameter("eval.problem.Normalization"), null, 0);
        doWrapper.objectives = state.parameters.getInt(new Parameter("eval.problem.Simulation.Objective"), null);
        doWrapper.typeOfObjective = TypeOfObjective.valueOf(state.parameters.getString(new Parameter("eval.problem.Simulation.ObjectiveType"), null))  ;
        
        int numOfRetries = 0;
        int numOfDuplicates = 0;
        doWrapper.Init();
        for(int x=start;x<individuals.length;x++) 
            {
            for(int tries=0; 
                tries <= /* Yes, I see that*/ numDuplicateRetries; 
                tries++)
                {
                individuals[x] = species.newIndividual(state, thread);

                if (numDuplicateRetries >= 1)
                    {
                    // check for duplicates
                    Object o = h.get(individuals[x]);
                    if (o == null) // found nothing, we're safe
                        // hash it and go
                        {
                        h.put(individuals[x],individuals[x]);
                        break;
                        }
                    }
                }  // oh well, we tried to cut down the duplicates
            
            	//I want to test 100 dummy operations, if output ranking is same as existed individuals, we generate a new one
            	if(this.isDuplicateRemoval)
            	{    
            		
            		//drWrapper.calculateRank(((GPIndividual)individuals[x]).trees[0].child);
            		
            		Boolean flag = false;
            		while(doWrapper.isIndividualSame(state,((GPIndividual)individuals[x]).trees[0].child) && numOfRetries < this.numDuplicateRemovalRetries)
            		{
            			flag = true;
            			if(numOfRetries >= this.numDuplicateRemovalRetries)
            			{
            				numOfRetries = 0;
            				break;
            			}
            			
            			individuals[x] = species.newIndividual(state, thread); 
            			//flag = false;
            			numOfRetries++;
            			//System.out.println("Initilization Duplicate Numbers=" + numOfRetries + " " + ((GPIndividual)individuals[x]).trees[0].child.makeCTree(true, true, false));
            		}
            		
            		if(flag)
            		{
            			numOfDuplicates++;
                		System.out.println("NumOfDuplicates in total: " + numOfDuplicates + "/" + x + " " + "Initilization Duplicate Numbers=" + numOfRetries);
                
            		}
            		
            	}
            	
            }
        
        	//System.out.println("NumOfDuplicates in total: " + numOfDuplicates);
        }
    }
        
  
