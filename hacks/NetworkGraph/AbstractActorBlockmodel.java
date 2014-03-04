/**
 * AbstractActorBlockmodel.java
 *
 * (c) 2000 Wibi Internet
 */

package com.wibinet.networks;

import com.wibinet.math.*;

public abstract class AbstractActorBlockmodel extends AbstractBlockmodel
  implements ActorBlockmodel
{
	protected double[] actorProb;

	public AbstractActorBlockmodel(NetworkData nData, com.wibinet.math.Partition p)
	{
		super(nData, p);

		// initialize all actor probabilites
    int nSize = nData.getSize();
		this.actorProb = new double[nSize];
		for(int ai=0; ai<nSize; ai++)
		{
			actorProb[ai] = 1.0;
		}
	}
	
	public final double getActorProbability(int ai)
	{
		return actorProb[ai];
	}
	
	public final double getLgDataProbability()
	{
		// the probability is the joint of the actors;
		double lgProb = 0.0;
		for(int pi=0; pi<actorProb.length; pi++)
		{
			lgProb += ExtendedMath.lg(actorProb[pi]);
		}
		return lgProb;
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
      for(int bj=0; bj<B; bj++)
      {
	      blockTermsLength += ExtendedMath.lg(getBlockCardinality(bi, bj));
      }
    }
    thetaLength += r * blockTermsLength;
    return -thetaLength;
	}
}