/**
 * ModelGAProblem.java
 *
 * The ModelGAProblem class encapsulates a blockmodel optimization
 * criterion into a genetic algorithms problem.
 *
 * (c) 2000 Wibi Internet
 */

package com.wibinet.networks;

import java.util.*;
import javax.swing.*;
import com.wibinet.ai.datatypes.*;
import com.wibinet.ai.ga.*;
import com.wibinet.gui.*;
import com.wibinet.math.*;

public class ModelGAProblem implements GAProblem
{
 	protected NetworkData nData;
 	protected ModelMeasure measure;
 	protected BlockmodelFactory mFactory;
 	protected double bestScore, worstScore;
 	protected int maxBlocks;
 	protected double scorePower;
 	protected int popSize;
	
  protected JMultiTicker ticker;
 	
  protected final static Random random = new Random();

 	public ModelGAProblem(NetworkData nData, ModelMeasure measure, 
 		BlockmodelFactory mFactory)
 	{
 		this.nData      = nData;
 		this.measure    = measure;
 		this.mFactory   = mFactory;
 		this.maxBlocks  = nData.getSize();
 		this.scorePower = 1.0;
		this.popSize    = 64;
		 		
 		this.ticker = new JMultiTicker();
 		
 		// get best and worst score
 		this.bestScore  = measure.getBestScore();
 		this.worstScore = measure.getWorstScore();
 	}
 	
 	public void setMaxBlocks(int maxBlocks)
 	{
 	  this.maxBlocks = maxBlocks;
 	}
 	
 	public int getMaxBlocks()
 	{
 		return maxBlocks;
 	}
 	
 	public void setScorePower(double scorePower)
 	{
 		this.scorePower = scorePower;
 	}
 	
  public GeneString generateRandom()
  {
    return new BlockmodelGS();
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
  
	public void setPopulationSize(int popSize)
	{
		this.popSize = popSize;
	}
	
  public double getFitness(GeneString gs)
  {
    BlockmodelGS mgs = (BlockmodelGS)gs;
    double rawScore = measure.getScore(mgs.model);
    
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
 	
  public GeneString crossOver(GeneString gs1, GeneString gs2)
  {
    int sz = nData.getSize();
    BlockmodelGS mgs1 = (BlockmodelGS)gs1;
    BlockmodelGS mgs2 = (BlockmodelGS)gs2;

    // pick a random int
    int bitsRandom = random.nextInt();
    int bitPtr = 0;
    BlockmodelGS gsNew = new BlockmodelGS();
    BasicPartition pNew = gsNew.p;
    for(int gi=0; gi<sz; gi++)
    {
      // flip on bits
      if((bitsRandom & (1 << bitPtr)) == 0)
      {
        pNew.partitions[gi] = mgs1.p.partitions[gi];
      }
      else
      {
        pNew.partitions[gi] = mgs2.p.partitions[gi];
      }
      
      // increment bit ptr, and get new random bits if necessary
      bitPtr++;
      if(bitPtr == 32)
      {
        bitsRandom = random.nextInt();
        bitPtr = 0;
      }
    }
    
    // make sure model block information gets updated
    gsNew.setPartition(pNew);
    
    // average out precision
    gsNew.model.setPrecision((mgs1.model.getPrecision() + mgs2.model.getPrecision()) / 2);
    
    return gsNew;
  }
  
  public GeneString mutate(GeneString gs)
  {
    BlockmodelGS mgs = (BlockmodelGS)gs;
    int sz = nData.getSize();

    // pick a mutate point
    int mPoint = Math.abs(random.nextInt() % sz);
    BlockmodelGS gsNew = new BlockmodelGS(); // new random precision?
    BasicPartition pNew = gsNew.p;
    for(int gi=0; gi<sz; gi++)
    {
      if(gi == mPoint)
      {
        pNew.partitions[gi] = Math.abs(random.nextInt() % maxBlocks);
      }
      else
      {
        pNew.partitions[gi] = mgs.p.partitions[gi];
      }
    }
    
    // make sure model block information gets updated
    gsNew.setPartition(pNew);
    return gsNew;
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

  protected class BlockmodelGS extends BlockmodelDelegate implements GeneString
  {
    protected BasicPartition p;
    
    public BlockmodelGS()
    {
    	super(ModelGAProblem.this.mFactory.newInstance(
    		ModelGAProblem.this.nData, 
    		new BasicPartition(ModelGAProblem.this.nData)));
      this.p = (BasicPartition)getPartition();

      // generate a new random variable assignment
      int sz = nData.getSize();
      for(int ai=0; ai<sz; ai++)
      {
      	p.partitions[ai] = Math.abs(random.nextInt() % maxBlocks);
      }
      
      // do this to reset block information
      setPartition(p);
      
      // random in range precision
      int maxP = mFactory.getMaxPrecision();
      int minP = mFactory.getMinPrecision();
      int precision = Math.abs(random.nextInt() % (maxP - minP)) + minP;
      model.setPrecision(precision);
    }
    
	  public VariableAssignment getAssignment()
	  {
	    return null; // ?
	  }
	}
}