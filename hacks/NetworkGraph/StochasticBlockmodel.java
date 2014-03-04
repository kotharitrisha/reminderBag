/**
 * StochasticBlockmodel.java
 *
 * Based upon Holland, P.W., Laskey, K.B. and Leinhard, S. (1983), 
 *   "Stochastic Blockmodels: First Steps", Social Networks 5,
 *   109-137.
 *
 * (c) 2000 Wibi Internet
 */

package com.wibinet.networks;

import com.wibinet.math.*;
import com.wibinet.gui.*;

public class StochasticBlockmodel extends AbstractDyadBlockmodel
{
	protected Matrix[] m;
	protected Matrix[] a;
	protected Matrix[] n;

  protected int[] invertedPIndices;	

	public StochasticBlockmodel(NetworkData nData)
	{
    this(nData, nData.getPartition());
  }
  
  public StochasticBlockmodel(NetworkData nData, com.wibinet.math.Partition p)
  {
  	super(nData, p);
  	initDataStructures();
  }
  
  public String getFactoryClass()
  {
    return "com.wibinet.networks.StochasticBlockmodelFactory";
  }
  
  protected void initDataStructures()
  {
  	resetBlockSizes();
  	int relCt = nData.getRelationCount();
  	int numBlocks = p.getPartitionCount();
  	m = new Matrix[relCt];
  	a = new Matrix[relCt];
  	n = new Matrix[relCt];
  	for(int rIdx=0; rIdx<relCt; rIdx++)
  	{
  		m[rIdx] = new Matrix(numBlocks, numBlocks);
  		a[rIdx] = new Matrix(numBlocks, numBlocks);
  		n[rIdx] = new Matrix(numBlocks, numBlocks);
  	}

    // create inverted permutation indces array
    int nSize = nData.getSize();
    invertedPIndices = new int[nSize];
    for(int i=0; i<nSize; i++)
    {
      invertedPIndices[i] = -6741; // make sure it's something unlikely
    }
    for(int pi=0; pi<pIndices.length; pi++)
    {
      invertedPIndices[pIndices[pi]] = pi;
    }
  }
  
  public void setPartition(com.wibinet.math.Partition p)
  {
  	super.setPartition(p);
  	initDataStructures();
  }
  
  /*public void setTolerance(double tolerance)
  {
  	this.tolerance = tolerance;
  }*/
  
  public void compute(AbstractTask task)
  {
    // this is probably redundant, though we could remove it
    // upstream...
    initDataStructures();
    
  	// initialize matrices
  	int relCt = nData.getRelationCount();
  	int numBlocks = p.getPartitionCount();
  	int nSize = nData.getSize();
  	for(int rIdx=0; rIdx<relCt; rIdx++)
  	{
  		for(int i=0; i<numBlocks; i++)
  		{
  			for(int j=0; j<numBlocks; j++)
  			{
					double blockSize = bSize[i] * bSize[j];
					if(i == j)
					{
						blockSize -= bSize[i];
					}
					
					BinaryFractionRange mRange = new BinaryFractionRange(
  				  nData.getBlockMutuals(p, rIdx, pIndices[i], pIndices[j]) / blockSize,
  				  precision);
  				m[rIdx].values[i][j] = mRange.getMidpoint(); 
					BinaryFractionRange aRange = new BinaryFractionRange(
  				  nData.getBlockAsymmetrics(p, rIdx, pIndices[i], pIndices[j]) / blockSize,
  				  precision);
  				a[rIdx].values[i][j] = aRange.getMidpoint();
					BinaryFractionRange nRange = new BinaryFractionRange(
  					nData.getBlockNulls(p, rIdx, pIndices[i], pIndices[j]) / blockSize,
  				  precision);
  				n[rIdx].values[i][j] = nRange.getMidpoint();
  			}
  		}
  	}
  	
  	/*// iterative scale them all
  	IterativeScaler scaler = new IterativeScaler(tolerance);
  	for(int rIdx=0; rIdx<relCt; rIdx++)
  	{
  		BlockScalable scalable = new BlockScalable(rIdx);
  		scaler.scale(scalable);
  	}*/
  	
  	// write out dyad probabilities
  	for(int i=0; i<nSize; i++)
  	{
  		int iBlock = invertedPIndices[p.getPartition(i)];
  		for(int j=0; j<i; j++)
  		{
	  		int jBlock = invertedPIndices[p.getPartition(j)];
  			double prob = 1.0;
  			for(int rIdx=0; rIdx<relCt; rIdx++)
  			{
  				double tieOut = nData.getTieStrength(rIdx, i, j);
  				double tieIn = nData.getTieStrength(rIdx, j, i);
  				prob *= m[rIdx].values[iBlock][jBlock] * tieOut * tieIn +
  					a[rIdx].values[iBlock][jBlock] * tieOut * (1.0-tieIn) +
  					a[rIdx].values[jBlock][iBlock] * (1.0-tieOut) * tieIn +
  					n[rIdx].values[iBlock][jBlock] * (1.0-tieOut) * (1.0-tieIn) *
  					confidence * confidence * confidence; // eek!  bad approximation!
  			}
  			dyadProb[i][j] = prob;
  		}
  	}
  }
  
	public int getDyadParameterCount(int bi, int bj)
	{
		if((bi == bj) && (bSize[bi] == 1))
		{
			return 0;
		}
		return 3;
	}
	
	public int getDyadCardinality(int bi, int bj)
	{
		int maxEntries = bSize[bi] * bSize[bj];
		
		// not so sure about diagonal adjust here, but let's do it
		if(bi == bj)
		{	
			maxEntries -= bSize[bi];
		}
		
		// the idea here is there are maxEntries possible ties in this block.
		// these ties need to be divided up into mutual ties, asymmetric ties
		// in each direction, and null ties.  That's four categories of ties.
		// The possible number of ways to do that is equivalent to choosing
		// 3 'split points' on a line from 0 to maxEntries.  However, it's
		// possible to have zero in each category, which means 'split points'
		// do not need to be unique.  The problem is the same if we say that
		// the length of the segment is equal to 1 + the number of ties of
		// each type (mutual, asymmetric, null).  This means that the line will
		// have to be four units longer to account for these extra 'length one'
		// pieces.  Hence choose (maxEntries + 4, 3).
		return (int)ExtendedMath.choose(maxEntries + 4, 3); 
	}
  
	public double getPredictedTieStrength(int rIdx, int i, int j)
	{
    // i think this is right (mutual + asym.out probs)
    int iBlock = invertedPIndices[p.getPartition(i)];
    int jBlock = invertedPIndices[p.getPartition(j)];
    
    return m[rIdx].values[iBlock][jBlock] + a[rIdx].values[iBlock][jBlock];
	}
	
	protected void populateMatrices()
	{
		// add matrices
		// unicode stuff?  hope this is really cross-platform :)
		// plausibly could have a static function "getUnicode()" which
		// returns different things depending on whether fonts are installed...
    clearMatrices();
    
		LabelModel pLabels = getPartitionLabels();
		LabelModel aLabels = getActorLabels();
		addMatrices("Mutual", m, pLabels, pLabels);
		addMatrices("Asymmetric", a, pLabels, pLabels);
		addMatrices("Null", n, pLabels, pLabels);
		addMatrices("Tie Probs", getDyadProbabilities(), aLabels, aLabels);
	}

/*
	protected double tolerance = 0.01;

	// constants
	protected final static int MUTUALS = 0;
	protected final static int ASYMMETRIC = 1;
	protected final static int NULL = 2;
	
	protected final static int ROW_STEP = 0;
	protected final static int COL_STEP = 1;
	protected final static int MUTUAL_STEP = 2;
	protected final static int NORMALIZING_STEP = 3;
	protected class BlockScalable implements IterativeScalable
	{
		protected Matrix[] myMatrices;
		protected double[][] blockMass;
		protected double totalMass;
		protected double totalMutuals;
		protected double[] rowFactor;
		protected double[] colFactor;
		protected double[][] matrixSums;
		protected double nullFactor;
		protected double mutualFactor;
		protected double offMutualFactor;
		protected int numBlocks;
		protected int nSize;
		protected double gChoose2;
		
		public BlockScalable(int rIdx)
		{
			myMatrices = new Matrix[3];
			myMatrices[MUTUALS] = m[rIdx];
			myMatrices[ASYMMETRIC] = a[rIdx];
			myMatrices[NULL] = n[rIdx];
			numBlocks = myMatrices[MUTUALS].getRows();
			nSize = nData.getSize();
			
			// load up block mass array
			blockMass = new double[numBlocks][];
			totalMass = 0.0;
			for(int bi=0; bi<numBlocks; bi++)
			{
				blockMass[bi] = new double[numBlocks];
				for(int bj=0; bj<numBlocks; bj++)
				{
					blockMass[bi][bj] = nData.getBlockMass(p, rIdx,
						pIndices[bi], pIndices[bj]);
					totalMass += blockMass[bi][bj];
				}
			}
			totalMutuals = nData.getMutuals(rIdx);
			
			// set up temp structures
			rowFactor = new double[numBlocks];
			colFactor = new double[numBlocks];
			matrixSums = new double[numBlocks][];
			for(int i=0; i<numBlocks; i++)
			{
				matrixSums[i] = new double[numBlocks];
			}
			nullFactor = -6741.0;
			mutualFactor = -6741.0;
			offMutualFactor = -6741.0;
 			gChoose2 = (nSize * (nSize-1)) / 2.0;
		}
		
		public Matrix[] getMatrices()
		{
			return myMatrices;
		}
		
		public int getNumStages()
		{
			return 4;
		}
		
		public void computeStageScaleFactors(int stage, Matrix[] matrices)
		{
			switch(stage)
			{
				case ROW_STEP:
					computeRowScaleFactors(matrices);
					return;
				
				case COL_STEP:
					computeColumnScaleFactors(matrices);
					return;
				
				case MUTUAL_STEP:
					computeMutualScaleFactors(matrices);
					return;
				
				case NORMALIZING_STEP:
					computeNormalizingScaleFactors(matrices);
					return;
			}
		}
		
		protected void computeRowScaleFactors(Matrix[] matrices)
		{
			// sum the mutuals and asymmetrics per row
			double predictedNulls = 0.0;
			for(int i=0; i<numBlocks; i++)
			{
				double rowSum = 0.0;
				double blockSum = 0.0;
				for(int j=0; j<numBlocks; j++)
				{
					// diagonal block adjust
					double blockSize = bSize[i] * bSize[j];
					if(i == j)
					{
						blockSize -= bSize[i];
					}

					rowSum += (matrices[MUTUALS].values[i][j] +
					  matrices[ASYMMETRIC].values[i][j]) * blockSize;
					
					// should take asymmetric ties j->i?
					predictedNulls += (matrices[ASYMMETRIC].values[j][i] +
						matrices[NULL].values[i][j]) * blockSize;

					blockSum += blockMass[i][j];
				}
				
				// special case?
				if(rowSum == 0.0)
				{
					// this row must be at a fixed point of zero?
					rowFactor[i] = 1.0;
				}
				else
				{
					rowFactor[i] = blockSum / rowSum;
				}
			}
			
			// again, handle the possibility of no nulls
			if(predictedNulls == 0.0)
			{
				nullFactor = 1.0; // ?
			}
			else
			{
				nullFactor = 
				 ((double)(nSize * (nSize - 1)) - totalMass) / predictedNulls;
			}
		}
		
		protected void computeColumnScaleFactors(Matrix[] matrices)
		{
			// sum the mutuals and asymmetrics per column
			double predictedNulls = 0.0;
			for(int j=0; j<numBlocks; j++)
			{
				double colSum = 0.0;
				double blockSum = 0.0;
				for(int i=0; i<numBlocks; i++)
				{
					// diagonal block adjust
					double blockSize = bSize[i] * bSize[j];
					if(i == j)
					{
						blockSize -= bSize[i];
					}

					colSum += (matrices[MUTUALS].values[i][j] +
					  matrices[ASYMMETRIC].values[i][j]) * blockSize;

					// should take asymmetric ties j->i?
					predictedNulls += (matrices[ASYMMETRIC].values[i][j] +
						matrices[NULL].values[j][i]) * blockSize;

					blockSum += blockMass[i][j];
				}
				
				// special case?
				if(colSum == 0.0)
				{
					// this column must be at a fixed point of zero?
					colFactor[j] = 1.0;
				}
				else
				{
					colFactor[j] = blockSum / colSum;
				}
			}

			// again, handle the possibility of no nulls
			if(predictedNulls == 0.0)
			{
				nullFactor = 1.0; // ?
			}
			else
			{
				nullFactor = 
				 ((double)(nSize * (nSize -1)) - totalMass) / predictedNulls;
			}
		}
		
		protected void computeMutualScaleFactors(Matrix[] matrices)
		{
			double predictedMutuals = 0.0;
			for(int i=0; i<numBlocks; i++)
			{
				for(int j=0; j<numBlocks; j++)
				{
					// diagonal block adjust
					double blockSize = bSize[i] * bSize[j];
					if(i == j)
					{
						blockSize -= bSize[i];
					}
					predictedMutuals +=
						matrices[MUTUALS].values[i][j] * blockSize;
				}
			}
			
			// special cases...
			if(predictedMutuals == 0.0)
			{
				mutualFactor = 1.0;
			}
			else
			{
				mutualFactor = totalMutuals / (predictedMutuals/2.0);
			}
			if(predictedMutuals == gChoose2 * 2.0)
			{
				offMutualFactor = 1.0;
			}
			else
			{
				offMutualFactor = (gChoose2 - totalMutuals) /
				  (gChoose2 - (predictedMutuals/2.0));
			}
		}
		
		protected void computeNormalizingScaleFactors(Matrix[] matrices)
		{
			for(int i=0; i<numBlocks; i++)
			{
				for(int j=0; j<numBlocks; j++)
				{
					matrixSums[i][j] = matrices[MUTUALS].values[i][j] +
					  matrices[ASYMMETRIC].values[i][j] +
					  matrices[ASYMMETRIC].values[j][i] +
					  matrices[NULL].values[i][j];
				}
			}
		}
		
		public double getStageScaleFactor(int stage, int mIdx, int i, int j)
		{
			switch(stage)
			{
				case ROW_STEP:
					return getRowScaleFactor(mIdx, i, j);
				
				case COL_STEP:
					return getColumnScaleFactor(mIdx, i, j);
				
				case MUTUAL_STEP:
					return getMutualScaleFactor(mIdx, i, j);
				
				case NORMALIZING_STEP:
					return getNormalizingScaleFactor(mIdx, i, j);
			}
			throw new IllegalArgumentException("Stage " + stage + " not valid.");
		}

		protected double getRowScaleFactor(int mIdx, int i, int j)
		{
			switch(mIdx)
			{
				case MUTUALS:
					return Math.sqrt(rowFactor[i] * rowFactor[j]);

				case ASYMMETRIC:
					return Math.sqrt(rowFactor[i] * nullFactor);

				case NULL:
					return nullFactor;
			}
			throw new IllegalArgumentException("Matrix index: " + mIdx + " not valid.");
		}
		
		protected double getColumnScaleFactor(int mIdx, int i, int j)
		{
			switch(mIdx)
			{
				case MUTUALS:
					return Math.sqrt(colFactor[i] * colFactor[j]);

				case ASYMMETRIC:
					return Math.sqrt(colFactor[j] * nullFactor);

				case NULL:
					return nullFactor;
			}
			throw new IllegalArgumentException("Matrix index: " + mIdx + " not valid.");
		}
		
		protected double getMutualScaleFactor(int mIdx, int i, int j)
		{
			switch(mIdx)
			{
				case MUTUALS:
					return mutualFactor;

				case ASYMMETRIC:
				case NULL:
					return offMutualFactor;
			}
			throw new IllegalArgumentException("Matrix index: " + mIdx + " not valid.");
		}
		
		protected double getNormalizingScaleFactor(int mIdx, int i, int j)
		{
			if(matrixSums[i][j] == 0.0)
			{
				 System.out.println("StochasticBlockmodel -- Degenrated at: " +
				  "("+mIdx+", "+i+", "+j+")");
				 return 1.0;
			}
			else
			{
				return 1.0/matrixSums[i][j];
			}
		}
	}
	*/
}