/**
 * DescriptiveBlockmodel.java
 *
 * (c) 2001 Wibi Internet
 */

package com.wibinet.networks;

import com.wibinet.gui.*;
import com.wibinet.math.Matrix;
import com.wibinet.math.Partition;

public class DescriptiveBlockmodel extends AbstractBlockmodel
{
	protected Matrix[] densities;
	protected Matrix[] predictedDensities;
	protected Matrix[] pTies;
	
	protected double alpha;

	public DescriptiveBlockmodel(NetworkData nData, double alpha)
	{
    this(nData, nData.getPartition(), alpha);
	}
	
  public DescriptiveBlockmodel(NetworkData nData, Partition p, double alpha)
  {
  	super(nData, p);
  	this.alpha = alpha;
  	initDataStructures();
  }
  
  public String getFactoryClass()
  {
    return "com.wibinet.networks.DescriptiveBlockmodelFactory";
  }
	
	public void setAlpha(double alpha)
	{
		this.alpha = alpha;
	}
	
	public double getAlpha()
	{
		return alpha;
	}
	
  protected void initDataStructures()
  {
  	resetBlockSizes();
  	int relCt = nData.getRelationCount();
  	int numBlocks = p.getPartitionCount();
  	int nSize = nData.getSize();
  	densities = new Matrix[relCt];
  	predictedDensities = new Matrix[relCt];
  	pTies = new Matrix[relCt];
  	for(int rIdx=0; rIdx<relCt; rIdx++)
  	{
  		densities[rIdx] = new Matrix(numBlocks, numBlocks);
  		predictedDensities[rIdx] = new Matrix(numBlocks, numBlocks);
  		pTies[rIdx] = new Matrix(nSize, nSize);
  	}
  }

	public double getLgDataProbability()
	{
		return 0.0;
	}
	
	public double getLgModelProbability()
	{
		return 0.0;
	}
	
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
  				double density = nData.getBlockDensity(p, rIdx, i, j);
  				double pDens = (density >= alpha) ? 1.0 : 0.0;
  				densities[rIdx].setValueAt(i, j, density);
  				predictedDensities[rIdx].setValueAt(i, j, pDens);
				}
			}
			
			for(int i=0; i<nSize; i++)
			{
				for(int j=0; j<nSize; j++)
				{
					pTies[rIdx].setValueAt(i, j, getPredictedTieStrength(rIdx, i, j));
				}
			}
		}
	}
	
	public void populateMatrices()
	{
		// add matrices
		// unicode stuff?  hope this is really cross-platform :)
		// plausibly could have a static function "getUnicode()" which
		// returns different things depending on whether fonts are installed...
    clearMatrices();
    
		LabelModel pLabels = getPartitionLabels();
		LabelModel aLabels = getActorLabels();
		addMatrices("Densities", densities, pLabels, pLabels);
		addMatrices("Predicted Densities", predictedDensities, pLabels, pLabels);
		addMatrices("Predicted Ties", pTies, aLabels, aLabels);
	}

	public double getPredictedTieStrength(int rIdx, int i, int j)
	{
		// get blocks
		int bi = p.getPartition(i);
		int bj = p.getPartition(j);
		
		return predictedDensities[rIdx].getValueAt(bi, bj);
	}

	public double getDeltaB1()
	{
		double sum = 0.0;
  	int relCt = nData.getRelationCount();

		for(int rIdx=0; rIdx<relCt; rIdx++)
		{
			for(int r=0; r<pIndices.length; r++)
			{
				for(int s=0; s<pIndices.length; s++)
				{
					if(nData.getBlockSize(p, r, s) != 0)
					{
						sum += Math.abs(densities[rIdx].getValueAt(r, s) -
							predictedDensities[rIdx].getValueAt(r, s));
					}
				}
			}
		}
		return sum;
	}

	public double getDeltaB2()
	{
		double sum = 0.0;
  	int nSize = nData.getSize();
  	int relCt = nData.getRelationCount();

		for(int rIdx=0; rIdx<relCt; rIdx++)
		{
			for(int r=0; r<pIndices.length; r++)
			{
				int[] actorsR = p.getObjectIndices(pIndices[r]);
				for(int s=0; s<pIndices.length; s++)
				{
					if(nData.getBlockSize(p, r, s) != 0)
					{
						int[] actorsS = p.getObjectIndices(pIndices[s]);
						double g = (r == s) ? (actorsR.length * (actorsR.length - 1)) :
							(actorsR.length * actorsS.length);
						double density = densities[rIdx].getValueAt(r, s);
						double t = (density < alpha) ? 1.0 :
							(1 - alpha) / alpha;
						
						double o = g * density;
						double o_hat = g * alpha;
						
						sum += (o - o_hat) * (o - o_hat) / (o_hat * t * t);
					}
				}
			}
		}
		
		double deltaB2 = sum / (relCt * nSize * (nSize - 1.0) * alpha);
		return deltaB2;
	}

	public double getDeltaX1()
	{
		double sum = 0.0;
  	int nSize = nData.getSize();
  	int relCt = nData.getRelationCount();

		for(int rIdx=0; rIdx<relCt; rIdx++)
		{
			for(int i=0; i<nSize; i++)
			{
				for(int j=0; j<nSize; j++)
				{
					if(i != j)
					{
						sum += Math.abs(nData.getTieStrength(rIdx, i, j) -
							getPredictedTieStrength(rIdx, i, j));
					}
				}
			}
		}
		return sum;
	}

	public double getDeltaX3()
	{
  	int nSize = nData.getSize();
  	int relCt = nData.getRelationCount();

		// calculate means
		double xBarSum = 0.0;
		double xBarHatSum = 0.0;
		for(int rIdx=0; rIdx<relCt; rIdx++)
		{
			for(int i=0; i<nSize; i++)
			{
				for(int j=0; j<nSize; j++)
				{
					if(i != j)
					{
						xBarSum += nData.getTieStrength(rIdx, i, j);
						xBarHatSum += getPredictedTieStrength(rIdx, i, j);
					}
				}
			}
		}
		double mass = nSize * (nSize-1) * relCt;
		double xMean = xBarSum / mass;
		double xHatMean = xBarHatSum / mass;
		
		// calculate actuals
		double numSum = 0.0;
		double denomSum = 0.0;
		double denomHatSum = 0.0;
		for(int rIdx=0; rIdx<relCt; rIdx++)
		{
			for(int i=0; i<nSize; i++)
			{
				for(int j=0; j<nSize; j++)
				{
					if(i != j)
					{
						double xStar = nData.getTieStrength(rIdx, i, j) - xMean;
						double xHatStar = getPredictedTieStrength(rIdx, i, j) - xHatMean;
						numSum += xStar * xHatStar;
						denomSum += xStar * xStar;
						denomHatSum += xHatStar * xHatStar;
					}
				}
			}
		}
		return numSum / (Math.sqrt(denomSum) * Math.sqrt(denomHatSum));
	}
}