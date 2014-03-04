/**
 * NormExBlockmodel.java
 *
 * A NormExBlockmodel is a blockmodel based upon the hypothesis
 * that an actor within a block has a number of ties to actors
 * of each other block that is normally distributed about
 * the mean number of ties between blocks.  This is a relaxation
 * of the criterion of exact regular equivalence.
 *
 * (c) 2000 Wibi Internet
 */

package com.wibinet.networks;

import com.wibinet.gui.LabelModel;
import com.wibinet.gui.AbstractTask;
import com.wibinet.math.ExtendedMath;
import com.wibinet.math.Matrix;
import com.wibinet.math.Partition;

public class NormExBlockmodel extends AbstractActorBlockmodel
{
  protected double[][][] rhoIn;
  protected double[][][] rhoOut;
  protected double[][][] deltaIn;
  protected double[][][] deltaOut;
  protected double[][][] sigmaIn;
  protected double[][][] sigmaOut;

	protected int[] invertedPIndices;
  
  public NormExBlockmodel(NetworkData nData)
  {
    this(nData, nData.getPartition());
  }
  
  public NormExBlockmodel(NetworkData nData, com.wibinet.math.Partition p)
  {
  	super(nData, p);
  }
  
  public String getFactoryClass()
  {
    return "com.wibinet.networks.NormExBlockmodelFactory";
  }
  
  public void compute(AbstractTask task)
  {
    int pSize = p.getPartitionCount();
    int relCt = nData.getRelationCount();
    int nSize = nData.getSize();
    
    // create inverted permutation indces array
    invertedPIndices = new int[nSize];
    for(int i=0; i<nSize; i++)
    {
      invertedPIndices[i] = -6741; // make sure it's something unlikely
    }
    for(int pi=0; pi<pIndices.length; pi++)
    {
      invertedPIndices[pIndices[pi]] = pi;
    }

    // calculate rho matrices
    this.rhoIn = new double[relCt][][];
    this.rhoOut = new double[relCt][][];
		for(int ri=0; ri<relCt; ri++)
		{
		  rhoIn[ri] = new double[pSize][];
		  rhoOut[ri] = new double[pSize][];
	    for(int pi=0; pi<pSize; pi++)
	    {
	      rhoIn[ri][pi] = new double[pSize];
	      rhoOut[ri][pi] = new double[pSize];
	      for(int pj=0; pj<pSize; pj++)
	      {
	        rhoIn[ri][pi][pj] = calcRhoIn(pIndices[pi], pIndices[pj], ri);
	        rhoOut[ri][pi][pj] = calcRhoOut(pIndices[pi], pIndices[pj], ri);
	      }
	    }
	  }
	  
	  // calculate deviation matrices
	  this.deltaIn = new double[relCt][][];
	  this.deltaOut = new double[relCt][][];
		for(int ri=0; ri<relCt; ri++)
		{
		  deltaIn[ri] = new double[nSize][];
		  deltaOut[ri] = new double[nSize][];
	    for(int ai=0; ai<nSize; ai++)
	    {
	      int pi = p.getPartition(ai);
	      deltaIn[ri][ai] = new double[pSize];
	      deltaOut[ri][ai] = new double[pSize];
	      for(int pj=0; pj<pSize; pj++)
	      {
	        deltaIn[ri][ai][pj] = calcDeltaIn(ai, pIndices[pj], ri);
	        deltaOut[ri][ai][pj] = calcDeltaOut(ai, pIndices[pj], ri);
	      }
	    }
	  }
	  
	  // calculate sigma matices
	  this.sigmaIn = new double[relCt][][];
	  this.sigmaOut = new double[relCt][][];
	  for(int ri=0; ri<relCt; ri++)
	  {
		  sigmaIn[ri] = new double[pSize][];
		  sigmaOut[ri] = new double[pSize][];
		  for(int pi=0; pi<pSize; pi++)
		  {
		    sigmaIn[ri][pi] = new double[pSize];
		    sigmaOut[ri][pi] = new double[pSize];
		    for(int pj=0; pj<pSize; pj++)
		    {
		      sigmaIn[ri][pi][pj] = calcSigmaIn(pIndices[pi], pIndices[pj], ri);
		    	sigmaOut[ri][pi][pj] = calcSigmaOut(pIndices[pi], pIndices[pj], ri);
		   	}
		  }
		}
		
		// calculate actor probabilities
		for(int ai=0; ai<nSize; ai++)
		{
			actorProb[ai] = calcActorProbability(ai);
		}
	}

	protected void populateMatrices()
	{
		// add matrices...
		// unicode stuff?  hope this is really cross-platform :)
		// plausibly could have a static function "getUnicode()" which
		// returns different things depending on whether fonts are installed...
    clearMatrices();
    String DELTA = "\u03B4";
    String RHO = "\u03C1";
  	String SIGMA = "\u03C3";
		LabelModel pLabels = getPartitionLabels();
		LabelModel aLabels = getActorLabels();
		addMatrices(RHO+",in", rhoIn, pLabels, pLabels);
		addMatrices(RHO+",out", rhoOut, pLabels, pLabels);
		addMatrices(DELTA+",in", deltaIn, aLabels, pLabels);
		addMatrices(DELTA+",out", deltaOut, aLabels, pLabels);
		addMatrices(SIGMA+",in", sigmaIn, pLabels, pLabels);
		addMatrices(SIGMA+",out", sigmaOut, pLabels, pLabels);
	}
	
	
	public int getBlockCardinality(int bi, int bj)
	{
		return bSize[bi] * bSize[bj] + 1;
	}
	
	public double getPredictedTieStrength(int rIdx, int i, int j)
	{
		return 0.0;
	}
	
  protected double calcRhoIn(int i, int j, int r)
  {
    // calculate rho'_{ijr,in}
    double sum = 0.0;
    Relation rel = nData.getRelation(r);
    int[] bi = p.getObjectIndices(i);
    int[] bj = p.getObjectIndices(j);
    
    // return nan if bi is empty
    if(bi.length == 0)
    {
      return Double.NaN;
    }
    
    for(int u=0; u<bi.length; u++)
    {
      for(int v=0; v<bj.length; v++)
      {
        sum += rel.getTieStrength(bj[v], bi[u]);
      }
    }
    sum /= (bi.length * (double)1.0);
    return sum;
  }
  
  protected double calcRhoOut(int i, int j, int r)
  {
    // calculate rho'_{ijr,in}
    double sum = 0.0;
    Relation rel = nData.getRelation(r);
    int[] bi = p.getObjectIndices(i);
    int[] bj = p.getObjectIndices(j);
    
    // return nan if bi is empty
    if(bi.length == 0)
    {
      return Double.NaN;
    }
    
    for(int u=0; u<bi.length; u++)
    {
      for(int v=0; v<bj.length; v++)
      {
        sum += rel.getTieStrength(bi[u], bj[v]);
      }
    }
    sum /= (bi.length * (double)1.0);
    return sum;
  }
  
  protected double calcDeltaIn(int u, int j, int r)
  {
    // count up ties into actor u from block j
    double sum = 0.0;
    int[] bj = p.getObjectIndices(j);
    Relation rel = nData.getRelation(r);
    for(int v=0; v<bj.length; v++)
    {
      sum += rel.getTieStrength(bj[v], u);
    }
    return sum - rhoIn[r][invertedPIndices[p.getPartition(u)]][invertedPIndices[j]];
  }
    
  protected double calcDeltaOut(int u, int j, int r)
  {
    // count up ties into actor u from block j
    double sum = 0.0;
    int[] bj = p.getObjectIndices(j);
    Relation rel = nData.getRelation(r);
    for(int v=0; v<bj.length; v++)
    {
      sum += rel.getTieStrength(u, bj[v]);
    }
    return sum - rhoOut[r][invertedPIndices[p.getPartition(u)]][invertedPIndices[j]];
  }
  
  protected double calcSigmaIn(int i, int j, int r)
  {
    int[] bi = p.getObjectIndices(i);

		// if there are no actors in this block then this is undefined
    if(bi.length == 0)
    {
      return Double.NaN;
    }
    
    double sum = 0.0;
    for(int uIdx=0; uIdx<bi.length; uIdx++)
    {
      int u = bi[uIdx]; // get actor from block
      double delIn = deltaIn[r][u][invertedPIndices[j]];
      sum += (delIn * delIn);
    }
    
    double sampleSize = bi.length;
    if(sampleSize == 1.0)
    {
      return 0.0; // no variance, right?
    }
        
    // std. deviation (sample)
    return Math.sqrt(sum / (sampleSize - 1.0));
  }

  protected double calcSigmaOut(int i, int j, int r)
  {
    int[] bi = p.getObjectIndices(i);

		// if there are no actors in this block then this is undefined
    if(bi.length == 0)
    {
      return Double.NaN;
    }
    
    double sum = 0.0;
    for(int uIdx=0; uIdx<bi.length; uIdx++)
    {
      int u = bi[uIdx]; // get actor from block
      double delOut = deltaOut[r][u][invertedPIndices[j]];
      sum += (delOut * delOut);
    }
    
    double sampleSize = bi.length;
    if(sampleSize == 1.0)
    {
      return 0.0; // no variance, right?
    }
        
    // std. deviation (sample)
    return Math.sqrt(sum / (sampleSize - 1.0));
  }
  
	protected double calcActorProbability(int ai)
	{
	  double actorProb = 1.0;
	  int actorBlock = p.getPartition(ai);
	  int r = nData.getRelationCount();
	  int B = pIndices.length; // # partitions
	  for(int ri=0; ri<r; ri++)
    {
  	  for(int relBlock=0; relBlock<B; relBlock++)
  	  {
  	    double stDevIn = Math.sqrt(sigmaIn[ri][invertedPIndices[actorBlock]][relBlock]);
  	    double stDevOut = Math.sqrt(sigmaOut[ri][invertedPIndices[actorBlock]][relBlock]);
  	    double devIn = deltaIn[ri][ai][relBlock];
  	    double devOut = deltaOut[ri][ai][relBlock];
  	    double actorProbIn = 
  	    	ExtendedMath.getGaussian(stDevIn, devIn, 0.0);
  	   	double actorProbOut =
  	   		ExtendedMath.getGaussian(stDevOut, devOut, 0.0);
  	   	actorProb = actorProb * actorProbIn * actorProbOut;
  	  }
    }
		return actorProb;
	}
}