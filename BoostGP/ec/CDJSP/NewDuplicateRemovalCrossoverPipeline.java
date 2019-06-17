/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */


package ec.CDJSP;
import java.util.ArrayList;
import java.util.List;

import com.sun.corba.se.spi.orbutil.fsm.State;

import ec.*;
import ec.util.*;
import jasima.core.statistics.SummaryStat;
import jasima.core.util.Util.TypeOfObjective;
import ec.gp.*;
import ec.gp.koza.CrossoverPipeline;
import ec.gp.koza.KozaFitness;

/* 
 * CrossoverPipeline.java
 * 
 * Created: Mon Aug 30 19:15:21 1999
 * By: Sean Luke
 */


/**
 * CrossoverPipeline is a GPBreedingPipeline which performs a strongly-typed
 * version of 
 * Koza-style "Subtree Crossover".  Two individuals are selected,
 * then a single tree is chosen in each such that the two trees
 * have the same GPTreeConstraints.  Then a random node is chosen
 * in each tree such that each node's return type is type-compatible
 * with the argument type of the slot in the parent which contains 
 * the other node.
 * If by swapping subtrees at these nodes the two trees will not
 * violate maximum depth constraints, then the trees perform the
 * swap, otherwise, they repeat the hunt for random nodes.
 *
 * <p>The pipeline tries at most <i>tries</i> times to a pair
 * of random nodes BOTH with valid swap constraints.  If it
 * cannot find any such pairs after <i>tries</i> times, it 
 * uses the pair of its last attempt.  If either of the nodes in the pair
 * is valid, that node gets substituted with the other node.  Otherwise
 * an individual invalid node isn't changed at all (it's "reproduced").
 *
 * <p><b>Compatibility with constraints.</b> 
 * Since Koza-I/II only tries 1 time, and then follows this policy, this is
 * compatible with Koza.  lil-gp either tries 1 time, or tries forever.
 * Either way, this is compatible with lil-gp.  My hacked 
 * <a href="http://www.cs.umd.edu/users/seanl/gp/">lil-gp kernel</a>
 * either tries 1 time, <i>n</i> times, or forever.  This is compatible
 * as well.
 *
 * <p>This pipeline typically produces up to 2 new individuals (the two newly-
 * swapped individuals) per produce(...) call.  If the system only
 * needs a single individual, the pipeline will throw one of the
 * new individuals away.  The user can also have the pipeline always
 * throw away the second new individual instead of adding it to the population.
 * In this case, the pipeline will only typically 
 * produce 1 new individual per produce(...) call.

 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 2 * minimum typical number of individuals produced by each source, unless tossSecondParent
 is set, in which case it's simply the minimum typical number.

 <p><b>Number of Sources</b><br>
 2

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>tries</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(number of times to try finding valid pairs of nodes)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>maxdepth</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(maximum valid depth of a crossed-over subtree)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>maxsize</tt><br>
 <font size=-1>int &gt;= 1</font></td>
 <td valign=top>(maximum valid size, in nodes, of a crossed-over subtree)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>tree.0</tt><br>
 <font size=-1>0 &lt; int &lt; (num trees in individuals), if exists</font></td>
 <td valign=top>(first tree for the crossover; if parameter doesn't exist, tree is picked at random)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>tree.1</tt><br>
 <font size=-1>0 &lt; int &lt; (num trees in individuals), if exists</font></td>
 <td valign=top>(second tree for the crossover; if parameter doesn't exist, tree is picked at random.  This tree <b>must</b> have the same GPTreeConstraints as <tt>tree.0</tt>, if <tt>tree.0</tt> is defined.)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>ns.</tt><i>n</i><br>
 <font size=-1>classname, inherits and != GPNodeSelector,<br>
 or String <tt>same<tt></font></td>
 <td valign=top>(GPNodeSelector for parent <i>n</i> (n is 0 or 1) If, for <tt>ns.1</tt> the value is <tt>same</tt>, then <tt>ns.1</tt> a copy of whatever <tt>ns.0</tt> is.  Note that the default version has no <i>n</i>)</td></tr>

 <tr><td valign=top><i>base</i>.<tt>toss</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font>/td>
 <td valign=top>(after crossing over with the first new individual, should its second sibling individual be thrown away instead of adding it to the population?)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 gp.koza.xover

 <p><b>Parameter bases</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>ns.</tt><i>n</i><br>
 <td>nodeselect<i>n</i> (<i>n</i> is 0 or 1)</td></tr>
 </table>

 *
 * @author Sean Luke
 * @version 1.0 
 */

public class NewDuplicateRemovalCrossoverPipeline extends CrossoverPipeline
{
	private static final long serialVersionUID = 1;

	public int produce(final int min, 
			final int max, 
			final int start,
			final int subpopulation,
			final Individual[] inds,
			final EvolutionState state,
			final int thread) 

	{
		// how many individuals should we make?
		int n = typicalIndsProduced();
		if (n < min) n = min;
		if (n > max) n = max;

		// should we bother?
		if (!state.random[thread].nextBoolean(likelihood))
			return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already



		GPInitializer initializer = ((GPInitializer)state.initializer);

		int reSelectParentCount = 0;
		Boolean needNewIndividual = false;
		List<Double> parentFitness = new ArrayList<Double>();
		do
		{
			for(int q=start;q<n+start; /* no increment */)  // keep on going until we're filled up
			{
				// grab two individuals from our sources
				if (sources[0]==sources[1])  // grab from the same source
					sources[0].produce(2,2,0,subpopulation,parents,state,thread);
				else // grab from different sources
				{
					sources[0].produce(1,1,0,subpopulation,parents,state,thread);
					sources[1].produce(1,1,1,subpopulation,parents,state,thread);
				}

				// at this point, parents[] contains our two selected individuals

				// are our tree values valid?
				if (tree1!=TREE_UNFIXED && (tree1<0 || tree1 >= parents[0].trees.length))
					// uh oh
					state.output.fatal("GP Crossover Pipeline attempted to fix tree.0 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 
				if (tree2!=TREE_UNFIXED && (tree2<0 || tree2 >= parents[1].trees.length))
					// uh oh
					state.output.fatal("GP Crossover Pipeline attempted to fix tree.1 to a value which was out of bounds of the array of the individual's trees.  Check the pipeline's fixed tree values -- they may be negative or greater than the number of trees in an individual"); 

				int t1=0; int t2=0;
				if (tree1==TREE_UNFIXED || tree2==TREE_UNFIXED) 
				{
					do
						// pick random trees  -- their GPTreeConstraints must be the same
					{
						if (tree1==TREE_UNFIXED) 
							if (parents[0].trees.length > 1)
								t1 = state.random[thread].nextInt(parents[0].trees.length);
							else t1 = 0;
						else t1 = tree1;

						if (tree2==TREE_UNFIXED) 
							if (parents[1].trees.length>1)
								t2 = state.random[thread].nextInt(parents[1].trees.length);
							else t2 = 0;
						else t2 = tree2;
					} while (parents[0].trees[t1].constraints(initializer) != parents[1].trees[t2].constraints(initializer));
				}
				else
				{
					t1 = tree1;
					t2 = tree2;
					// make sure the constraints are okay
					if (parents[0].trees[t1].constraints(initializer) 
							!= parents[1].trees[t2].constraints(initializer)) // uh oh
						state.output.fatal("GP Crossover Pipeline's two tree choices are both specified by the user -- but their GPTreeConstraints are not the same");
				}



				// validity results...
				boolean res1 = false;
				boolean res2 = false;


				// prepare the nodeselectors
				nodeselect1.reset();
				nodeselect2.reset();


				DummyOperationWrapper doWrapper = new DummyOperationWrapper();
				doWrapper.Normalization = state.parameters.getIntWithDefault(new Parameter("eval.problem.Normalization"), null, 0);
			    doWrapper.objectives = state.parameters.getInt(new Parameter("eval.problem.Simulation.Objective"), null);
			    doWrapper.typeOfObjective = TypeOfObjective.valueOf(state.parameters.getString(new Parameter("eval.problem.Simulation.ObjectiveType"), null))  ;
			    
				int numOfRetries = 0;
				doWrapper.Init();

				GPIndividual j1 = (GPIndividual)(parents[0].lightClone());
				GPIndividual j2 = null;
				// pick some nodes

				parentFitness.clear();
				
				if(parents[0] != null)
				{
					parentFitness.add(doWrapper.getFitnessInDouble(parents[0].trees[0].child));
				}
				
				if(parents[1] != null)
				{
					parentFitness.add(doWrapper.getFitnessInDouble(parents[1].trees[0].child));
				}
				while(true)
				{
					GPNode p1 = null;
					GPNode p2 = null;

					for (int x = 0; x < numTries; x++) {
						// pick a node in individual 1
						p1 = nodeselect1.pickNode(state, subpopulation, thread, parents[0], parents[0].trees[t1]);

						// pick a node in individual 2
						p2 = nodeselect2.pickNode(state, subpopulation, thread, parents[1], parents[1].trees[t2]);

						// check for depth and swap-compatibility limits
						res1 = verifyPoints(initializer, p2, p1); // p2 can fill
						// p1's spot --
						// order is
						// important!
						if (n - (q - start) < 2 || tossSecondParent)
							res2 = true;
						else
							res2 = verifyPoints(initializer, p1, p2); // p1 can fill
						// p2's spot
						// -- order
						// is
						// important!

						// did we get something that had both nodes verified?
						// we reject if EITHER of them is invalid. This is what
						// lil-gp does.
						// Koza only has numTries set to 1, so it's compatible as
						// well.
						if (res1 && res2)
							break;
					}

					// at this point, res1 AND res2 are valid, OR either res1
					// OR res2 is valid and we ran out of tries, OR neither is
					// valid and we ran out of tries. So now we will transfer
					// to a tree which has res1 or res2 valid, otherwise it'll
					// just get replicated. This is compatible with both Koza
					// and lil-gp.

					// at this point I could check to see if my sources were
					// breeding
					// pipelines -- but I'm too lazy to write that code (it's a
					// little
					// complicated) to just swap one individual over or both over,
					// -- it might still entail some copying. Perhaps in the future.
					// It would make things faster perhaps, not requiring all that
					// cloning.

					// Create some new individuals based on the old ones -- since
					// GPTree doesn't deep-clone, this should be just fine. Perhaps
					// we
					// should change this to proto off of the main species
					// prototype, but
					// we have to then copy so much stuff over; it's not worth it.

					if (n - (q - start) >= 2 && !tossSecondParent)
						j2 = (GPIndividual) (parents[1].lightClone());

					// Fill in various tree information that didn't get filled in
					// there
					j1.trees = new GPTree[parents[0].trees.length];
					if (n - (q - start) >= 2 && !tossSecondParent)
						j2.trees = new GPTree[parents[1].trees.length];

					// at this point, p1 or p2, or both, may be null.
					// If not, swap one in. Else just copy the parent.

					for (int x = 0; x < j1.trees.length; x++) {
						if (x == t1 && res1) // we've got a tree with a kicking
							// cross position!
						{
							j1.trees[x] = (GPTree) (parents[0].trees[x].lightClone());
							j1.trees[x].owner = j1;
							j1.trees[x].child = parents[0].trees[x].child.cloneReplacing(p2, p1);
							j1.trees[x].child.parent = j1.trees[x];
							j1.trees[x].child.argposition = 0;
							j1.evaluated = false;
						} // it's changed
						else {
							j1.trees[x] = (GPTree) (parents[0].trees[x].lightClone());
							j1.trees[x].owner = j1;
							j1.trees[x].child = (GPNode) (parents[0].trees[x].child.clone());
							j1.trees[x].child.parent = j1.trees[x];
							j1.trees[x].child.argposition = 0;
						}
					}

					if (n - (q - start) >= 2 && !tossSecondParent)
						for (int x = 0; x < j2.trees.length; x++) {
							if (x == t2 && res2) // we've got a tree with a kicking
								// cross position!
							{
								j2.trees[x] = (GPTree) (parents[1].trees[x].lightClone());
								j2.trees[x].owner = j2;
								j2.trees[x].child = parents[1].trees[x].child.cloneReplacing(p1, p2);
								j2.trees[x].child.parent = j2.trees[x];
								j2.trees[x].child.argposition = 0;
								j2.evaluated = false;
							} // it's changed
							else {
								j2.trees[x] = (GPTree) (parents[1].trees[x].lightClone());
								j2.trees[x].owner = j2;
								j2.trees[x].child = (GPNode) (parents[1].trees[x].child.clone());
								j2.trees[x].child.parent = j2.trees[x];
								j2.trees[x].child.argposition = 0;
							}
						}


					Boolean isSub1Duplicated = false;
					Boolean isSub2Duplicated = false;
					double curFitness1 = 0, curFitness2 = 0;					

					if(j1 != null)
					{
						curFitness1 = doWrapper.getFitnessInDouble(j1.trees[0].child);	
						if(state.tempPhenotypicFitness.contains(curFitness1) || parentFitness.contains(curFitness1))
							isSub1Duplicated = true;
					}				

					if(j2 != null)
					{
						curFitness2 = doWrapper.getFitnessInDouble(j2.trees[0].child);	
						if(state.tempPhenotypicFitness.contains(curFitness2) || parentFitness.contains(curFitness2))
							isSub2Duplicated = true;
					}

					if((!isSub1Duplicated && !isSub2Duplicated) && curFitness1 != curFitness2)
					{
						needNewIndividual = false;
						break;
					}

					if (numOfRetries > 20)
					{
						needNewIndividual = true;						
						break;
					}				

					numOfRetries++;
				}

				if(needNewIndividual)
				{
					continue;				
				}


				// add the individuals to the population
				KozaFitness f1 = (KozaFitness)(j1.fitness);
				f1.summaryStat.clear();
				inds[q] = j1;           			
				state.tempPhenotypicFitness.add(doWrapper.getFitnessInDouble(j1.trees[0].child));
				
				q++;
				if (q<n+start && !tossSecondParent)
				{
					KozaFitness f2 = (KozaFitness)(j2.fitness);
					f2.summaryStat.clear();
					inds[q] = j2;              		
					state.tempPhenotypicFitness.add(doWrapper.getFitnessInDouble(j2.trees[0].child));
					q++;
				}
			}
			return n;
		} while (needNewIndividual);
	}
}
