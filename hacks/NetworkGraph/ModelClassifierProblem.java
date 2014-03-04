/**
 * ModelClassifierProblem.java
 *
 * (c) 2000 Wibi Internet
 */

package com.wibinet.networks;

import java.io.*;
import java.util.*;
import javax.swing.*;
import com.wibinet.ai.datatypes.*;
import com.wibinet.ai.ga.*;
import com.wibinet.gui.*;
import com.wibinet.math.Partition;
import com.wibinet.util.XMLReader;
// import com.wibinet.math.*;

public class ModelClassifierProblem extends ClassifierGAProblem
{
 	protected NetworkData nData;
 	protected ModelMeasure measure;
 	protected BlockmodelFactory mFactory;
 	protected double bestScore, worstScore;
 	protected double scorePower;
 	
  protected JMultiTicker ticker;
 	
  protected final static Random random = new Random();

 	public ModelClassifierProblem(NetworkData nData, ModelMeasure measure, 
 		BlockmodelFactory mFactory)
 	{
 		super();
 		
 		this.nData     = nData;
 		this.measure   = measure;
 		this.mFactory  = mFactory;
 		this.scorePower = 1.0;
 		
 		this.ticker = new JMultiTicker();
 		
 		// get best and worst score
 		this.bestScore  = measure.getBestScore();
 		this.worstScore = measure.getWorstScore();
 	}
 	
 	public void setScorePower(double scorePower)
 	{
 		this.scorePower = scorePower;
 	}
 	
  public GeneString generateRandom()
  {
    return new BlockmodelTreeNode(true, Math.abs(random.nextInt()) % 32);
  }
  
	public GeneString[] generatePopulation()
	{
		// problem size?
		GeneString[] pop = new GeneString[64];
		for(int i=0; i<pop.length; i++)
		{
			pop[i] = generateRandom();
		}
		return pop;
	}
	
  public double getFitness(GeneString gs)
  {
    BlockmodelTreeNode node = (BlockmodelTreeNode)gs;
    Blockmodel model = mFactory.newInstance(nData, node);
    double rawScore = measure.getScore(model);
    
    // normalize against best and worst, such that 1.0 is
    // a good score and 0.0 is a bad score
    double normScore = (rawScore - worstScore) / (bestScore - worstScore);
    
    // cap...perhaps should register warning...
    if(normScore > 1.0)
    {
      normScore = 1.0;
    }
    if(normScore < 0.0)
    {
      normScore = 0.0;
    }
    return Math.pow(normScore, scorePower); 
  }
  
  public JComponent getDisplay()
  {
    return ticker;
  }
  
  public void display(GeneString[] population, double[] fitness)
  {
    ticker.addPoints(fitness);
    return;
  }

	protected class BlockmodelTreeNode extends ClassifierTreeNode implements
		Partition
	{
		protected Vector classNames;
		protected int[] partitions;
		
		public BlockmodelTreeNode(boolean root, int feature)
		{
			super(root, feature);
			classNames = null;
			partitions = new int[nData.getSize()];
			updateInternals();
		}
		
		protected void updateInternals()
		{
			// update classNames
			classNames = new Vector();
			for(int ai=0; ai<nData.getSize(); ai++)
			{
				String className = classify(ai);
				int partitionIdx = classNames.indexOf(className);
				if(partitionIdx == -1)
				{
					classNames.addElement(className);
					partitionIdx = classNames.size() - 1;
				}
				partitions[ai] = partitionIdx;
			}
		}
		
	  public int getSize()
	  {
	  	return partitions.length;
	  }
	  
	  public int getPartition(int actor)
	  {
	  	return partitions[actor];
	  }
	  
	  public void setPartition(int actor, int partition)
	  {
	  	// non mutable...probably should throw exception...
	  }
	  
	  public synchronized int[] getObjectIndices(int partition)
	  {
	    int ct=0;
	    for(int i=0; i<partitions.length; i++)
	    {
	      if(partitions[i] == partition) ct++;
	    }
	    int[] actors = new int[ct];
	    ct = 0;
	    for(int i=0; i<partitions.length; i++)
	    {
	      if(partitions[i] == partition) actors[ct++] = i;
	    }
	    return actors;
	  }

	  public void setObjects(int partition, int[] actors)
	  {
	  	// non mutable...probably should throw exception...
	  }
	  
		public void deleteObjectAt(int idx)
		{
	  	// non mutable...probably should throw exception...
		}
		
	  public int[] getPartitionIndices()
	  {
	    Vector helper = new Vector();
	    for(int ai=0; ai<partitions.length; ai++)
	    {
	      Integer i = new Integer(partitions[ai]);
	      if(!helper.contains(i))
	      {
	        helper.addElement(i);
	      }
	    }
	    int[] indices = new int[helper.size()];
	    for(int i=0; i<indices.length; i++)
	    {
	      indices[i] = ((Integer)helper.elementAt(i)).intValue();
	    }
	    return indices;
	  }

	  public int getPartitionSize(int partition)
	  {
	    int pSize = 0;
	    for(int pi=0; pi<partitions.length; pi++)
	    {
	      pSize += (partitions[pi] == partition) ? 1:0;
	    }
	    return pSize;
	  }

	  public int getPartitionCount()
	  {
	  	return classNames.size();
	  }

	  public String getPartitionName(int pIdx)
	  {
	  	return (String)classNames.elementAt(pIdx);
	  }

	  public void setPartitionName(int pIdx, String name)
	  {
	  	// non mutable...probably should throw exception...
	  }

		public void writeXML(PrintWriter out, String prefix) throws IOException
		{
			int pCt = getPartitionCount();
			for(int pi=0; pi<pCt; pi++)
			{
				String pName = getPartitionName(pi);
				int[] actors = getObjectIndices(pi);
				out.print(prefix + "<actormap actors=\"");
				for(int ai=0; ai<partitions.length; ai++)
				{
					if(ai==0)
					{
						out.print(partitions[ai]);
					}
					else
					{
						out.print(", "+partitions[ai]);
					}
				}
				out.println("\" partition=\""+XMLReader.xmlEncode(getPartitionName(pi))+"\"/>");
			}
		}
	}
}