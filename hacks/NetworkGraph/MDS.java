/**
 * MDS.java
 *
 * Routines for Multidimensional Scaling
 *
 * I think this should eventually find its way into a 'Node Positioning'
 * framework of some sort.  This and SpringEmbedder do similar things,
 * and can both be stepped.
 *
 * Copyright (c) 2002 Wibi Internet.
 * All rights reserved.
 */
 
package com.wibinet.networks;

public class MDS extends Object
{
	protected VisualNetworkData nData;
	protected double[][] distance;
	protected double[][] estDistance;
	
	public MDS(VisualNetworkData nData)
	{
		this.nData = nData;
		this.distance = null;
		this.estDistance = null;
	}
	
	public void init(RelationEvaluator re, int relNum)
	{
		int nSize = nData.getSize();
		Relation r = nData.getRelation(relNum);
		re.setRelation(r);
		re.runEvaluator();
		this.distance = new double[nSize][];
		this.estDistance = new double[nSize][];
		for(int i=0; i<nSize; i++)
		{
			distance[i] = new double[nSize];
			estDistance[i] = new double[nSize];
			for(int j=0; j<nSize; j++)
			{
				Double d = (Double)re.evaluateRelation(i, j);
				distance[i][j] = d.doubleValue();
			}
		}
	}
	
	protected double getStress()
	{
		// update estimates
		for(int i=0; i<estDistance.length; i++)
		{
			estDistance[i][i] = 0.0;
			for(int j=i+1; j<estDistance[i].length; j++)
			{
				double xdist = nData.getXPos(i) - nData.getXPos(j);
				double ydist = nData.getYPos(i) - nData.getYPos(j);
				double dist = Math.sqrt(xdist * xdist + ydist * ydist);
				estDistance[i][j] = dist;
				estDistance[j][i] = dist;
			}
		}
		
		double sumDelta = 0.0;
		for(int i=0; i<distance.length; i++)
		{
			for(int j=i+1; j<distance.length; j++)
			{
				double delta = distance[i][j] - estDistance[i][j];
				sumDelta += (delta * delta);
			}
		}

		double sumDenom = 0.0;
		for(int i=0; i<distance.length; i++)
		{
			for(int j=i+1; j<distance.length; j++)
			{
				double denom = distance[i][j];
				sumDenom += (denom * denom);
			}
		}
		
		return Math.sqrt(sumDelta / sumDenom);
	}
	
	public void step()
	{
		
	}
}