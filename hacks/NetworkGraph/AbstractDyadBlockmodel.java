/**
 * AbstractDyadBlockmodel.java
 *
 * (c) 2000 Wibi Internet
 */

package com.wibinet.networks;

import com.wibinet.math.*;

public abstract class AbstractDyadBlockmodel extends AbstractBlockmodel
  implements DyadBlockmodel
{
	protected double[][] dyadProb;

	public AbstractDyadBlockmodel(NetworkData nData, com.wibinet.math.Partition p)
	{
		super(nData, p);

		// initialize all dyad probabilites
    int nSize = nData.getSize();
		this.dyadProb = new double[nSize][];
		for(int ai=0; ai<nSize; ai++)
		{
			dyadProb[ai] = new double[ai+1]; // triangular matrix...
			for(int aj=0; aj<=ai; aj++)
			{
				dyadProb[ai][aj] = 1.0;
			}
		}
	}
	
	public final double getDyadProbability(int actor1, int actor2)
	{
		if(actor1 > actor2)
		{
			return dyadProb[actor1][actor2];
		}
		else
		{
			return dyadProb[actor2][actor1]; // or toss exception...
		}
	}
	
	public final Matrix[] getDyadProbabilities()
	{
  	int nSize = nData.getSize();
		int relCt = nData.getRelationCount();
		Matrix[] ms = new Matrix[relCt];
		for(int rIdx=0; rIdx<relCt; rIdx++)
		{
			double[][] vals = new double[nSize][];
			for(int i=0; i<nSize; i++)
			{
				vals[i] = new double[nSize];
				for(int j=0; j<nSize; j++)
				{
					vals[i][j] = getDyadProbability(i, j); // don't we want this by rel?
				}
			}
			ms[rIdx] = new Matrix(vals);
		}
		return ms;
	}
	
	public final double getLgModelProbability()
	{
    int n = nData.getSize();
    int B = p.getPartitionCount();
    int r = nData.getRelationCount();
    
    double thetaLength = ExtendedMath.lg(n) + ExtendedMath.lgStirling2(n, B);
    
    // get dyad terms
    double blockTermsLength = 0.0;
    for(int bi=0; bi<B; bi++)
    {
      for(int bj=bi; bj<B; bj++)
      {
	      blockTermsLength += getDyadParameterCount(bi, bj) * precision;
      }
    }
    thetaLength += r * blockTermsLength;
    return -thetaLength;
	}

	public final double getLgDataProbability()
	{
		// the probability is the joint of the actors;
		double lgProb = 0.0;
		for(int pi=0; pi<dyadProb.length; pi++)
		{
			for(int pj=0; pj<=pi; pj++)
			{
				lgProb += ExtendedMath.lg(dyadProb[pi][pj]);
			}
		}
		return lgProb;
	}
}