/**
 * RolesGAProblem.java
 *
 * (c) 2000 Wibi Internet
 */

package com.wibinet.networks;

import java.text.*;
import java.util.*;

import com.wibinet.ai.datatypes.*;
import com.wibinet.ai.ga.*;
import com.wibinet.math.Matrix;

public class RolesGAProblem implements GAProblem, NodeDistanceMeasure
{
  protected int problemSize;
	protected int popSize;
  protected int actor1, actor2;
  protected NetworkData network;
  protected static final Random random = new Random();
  protected int opCt;
  protected long solveTime;
  
  protected final static NumberFormat fmt = NumberFormat.getInstance();
  
  public RolesGAProblem()
  {
    solveTime = 0;
		popSize = 64;
  }
  
	public void setPopulationSize(int popSize)
	{
		this.popSize = popSize;
	}
	
  public double getDistance(NetworkData network, int actor1, int actor2)
  {
    this.problemSize = network.getSize() - 2;
    this.network     = network;
    
    // order actors if necessary
    if(actor1 > actor2)
    {
      int tmp = actor1;
      actor1  = actor2;
      actor2  = tmp;
    }
    
    this.actor1 = actor1;
    this.actor2 = actor2;
    
    opCt = 0;
    GASolver solver = new GASolver(this, 0.02, false); // min increase = 2%?  make this better
    solver.setMutateProbability(0.15);
    GeneString bestGene = solver.minimize();
    opCt = solver.getGenerationCount();
    solveTime = solver.getSolveTime();
    return 1.0-getFitness(bestGene);
  }
  
  public void display(GeneString[] population, double[] fitness)
  {
    System.out.println("Generation:");
    for(int i=0; i<fitness.length; i++)
    {
      System.out.println(population[i] + "-" + fmt.format(fitness[i]) + " ");
    }
  }
  
  public int getOperationCount()
  {
    return opCt;
  }
  
  public long getSolveTime()
  {
    return solveTime;
  }
  
  public GeneString generateRandom()
  {
    return new Swapper();
  }
  
	public GeneString[] generatePopulation()
	{
		GeneString[] pop = new GeneString[popSize];
		for(int i=0; i<pop.length; i++)
		{
			pop[i] = generateRandom();
		}
		return pop;
	}
	
  public GeneString mutate(GeneString gs)
  {
    return ((Swapper)gs).getMutation();
  }
  
  public GeneString crossOver(GeneString gs1, GeneString gs2)
  {
    return ((Swapper)gs1).crossOver((Swapper)gs1, (Swapper)gs2);
  }
  
  public double getFitness(GeneString gs)
  {
    // generate implied permutation
    int stdPerm[] = ((Swapper)gs).getPermutation();
    int perm[] = expandPermutation(stdPerm);

    // generate permuted matrix
    Matrix m2 = new Matrix(perm.length);
    for(int r=0; r<perm.length; r++)
    {
      for(int c=0; c<perm.length; c++)
      {
        m2.setValueAt(r, c, network.getTieStrength(perm[r], perm[c]));
      }
    }
    // System.out.println("Permuted Matrix for "+gs+"/"+a2s(perm)+":");
    // m2.print();
    
    // calculate distance (matrix 2-norm)
    double distance = 0.0;
    for(int r=0; r<problemSize; r++)
    {
      for(int c=0; c<problemSize; c++)
      {
        double diff = network.getTieStrength(r, c) - m2.getValueAt(r, c);
        distance += Math.sqrt(diff * diff);
      }
    }
    
    return 1.0 - (distance / (1.0 * (problemSize * problemSize)));
    // return Math.pow(1.1, -distance); // a reasonable measure, I think...
  }
  
  protected int[] expandPermutation(int[] perm)
  {
    int[] newPerm = new int[perm.length + 2];
    int srcIdx = 0;
    int delta = 0;
    for(int i=0; i<newPerm.length; i++)
    {
      if(i == actor1)
      {
        newPerm[i] = actor2;
        delta++;
      }
      else if(i == actor2)
      {
        newPerm[i] = actor1;
        delta++;
      }
      else
      { 
        newPerm[i] = perm[srcIdx] + delta;
        srcIdx++;
      }
    }
    // System.out.println("expandPermutation(): perm="+a2s(perm)+" newPerm="+a2s(newPerm));
    return newPerm;
  }
  
  public static String a2s(int[] a)
  {
    String str = "[";
    for(int i=0; i<a.length; i++)
    {
      if(i != 0) str += " ";
      str += a[i];
    }
    return str + "]";
  }
  
  public class Swapper implements GeneString
  {
    protected int swap1[];
    protected int swap2[];
    
    protected Swapper(int size)
    {
      swap1 = new int[size];
      swap2 = new int[size];
    }
    
    public Swapper()
    {
      // generate a random swapper?
      int length = problemSize; // make it pretty long, normalization will shorten...
      swap1 = new int[length];
      swap2 = new int[length];
      for(int i=0; i<length; i++)
      {
        swap1[i] = Math.abs(random.nextInt()) % problemSize;
        swap2[i] = Math.abs(random.nextInt()) % problemSize;
      }
      
      // might as well normalize here...
      normalize();
    }
    
    public void normalize()
    {
			// generate implied permutation
			int[] permutation = getPermutation();
			
			// create set of swaps that would generate this permutation
			int pos = 0;
			Vector newSwaps = new Vector();
			while(pos < problemSize)
			{
			  if(permutation[pos] != pos)
			  {
			    // if this is out of place, swap it into place
			    int[] swap = new int[2];
			    swap[0] = pos;
			    swap[1] = permutation[pos];
			    int tmp = permutation[swap[0]];
			    permutation[swap[0]] = permutation[swap[1]];
			    permutation[swap[1]] = tmp;
			    
			    newSwaps.addElement(swap);
			  }
			  else
			  {
			    // otherwise move to next
			    pos++;
			  }
			}
			
			// redo swap arrays
			int newLen = newSwaps.size();
			swap1 = new int[newLen];
			swap2 = new int[newLen];
			for(int i=0; i<newLen; i++)
			{
			  int[] swap = (int[])newSwaps.elementAt(i);
			  swap1[i] = swap[0];
			  swap2[i] = swap[1];
			}
    }
    
    public Swapper getMutationOld()
    {
      int length = swap1.length;
      Swapper swp = new Swapper(length);
      if(length == 0)
      {
        // can't mutate a zero-length swap
        return swp;
      }
      int mutatePos = Math.abs(random.nextInt()) % length;
      for(int i=0; i<length; i++)
      {
        if(i != mutatePos)
        {
          swp.swap1[i] = swap1[i];
          swp.swap2[i] = swap2[i];
        }
        else
        {
        	swp.swap1[i] = Math.abs(random.nextInt()) % problemSize;
          swp.swap2[i] = Math.abs(random.nextInt()) % problemSize;
        }
      }
      swp.normalize();
      return swp;
    }
    
    public Swapper getMutation()
    {
      // this implementation of a mutation just swaps two positions in permutation
      // at random
      int length = swap1.length + 1;
      Swapper swp = new Swapper(length);
      for(int i=0; i<length-1; i++)
      {
        swp.swap1[i] = swap1[i];
        swp.swap2[i] = swap2[i];
      }
     	swp.swap1[length-1] = Math.abs(random.nextInt()) % problemSize;
      swp.swap2[length-1] = Math.abs(random.nextInt()) % problemSize;
      // swp.normalize();
      return swp;
    }
    
    public Swapper crossOver(Swapper s1, Swapper s2)
    {
      // pick a random cross-point in each string - adjust for zero length strings...
      int len1 = s1.swap1.length;
      int len2 = s2.swap1.length;
      int cp1 = (len1 == 0) ? 0 : Math.abs(random.nextInt() % len1);
      int cp2 = (len2 == 0) ? 0 : Math.abs(random.nextInt() % len2);
      
      int length = cp1 + (len2-cp2);
      Swapper swp = new Swapper(length);
      
      // pick up swaps from swapper #1, from 0 to cross-point 1
      for(int i=0; i<cp1; i++)
      {
        swp.swap1[i] = s1.swap1[i];
        swp.swap2[i] = s1.swap2[i];
      }
      
      // pick up swaps from swapper #2, from cross-point 2 to end
      for(int i=cp2; i<len2; i++)
      {
        swp.swap1[cp1 + (i-cp2)] = s2.swap1[i];
        swp.swap2[cp1 + (i-cp2)] = s2.swap2[i];
      }
      
      // and normalize
      // swp.normalize();
      return swp;
    }
    
    public VariableAssignment getAssignment()
    {
      return null;
    }
    
    protected int[] getPermutation()
    {
      int[] perm = new int[problemSize];
      for(int i=0; i<problemSize; i++)
      {
        perm[i] = i;
      }
      
      // apply swaps
      for(int i=0; i<swap1.length; i++)
      {
        int tmp = perm[swap1[i]];
        perm[swap1[i]] = perm[swap2[i]];
        perm[swap2[i]] = tmp;
      }
      
      return perm;
    }
    
    public String toString()
    {
      String out = "(";
      for(int i=0; i<swap1.length; i++)
      {
        out += "<"+swap1[i]+","+swap2[i]+">";
      }
      return out + ")";
    }
  }
}