/**
 * BridgeFactory.java
 *
 * Calculates the probability that a tie (i,j) is on
 * the shortest path between two arbitrary nodes in
 * the network.
 *
 * Copyright (c) 2002 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import javax.swing.*;
import com.wibinet.math.*;

public class BridgeFactory implements RelationEvaluatorFactory
{
	public RelationEvaluator newInstance()
	{
		return new Evaluator();
	}
	
  public void edit(RelationEvaluator evaluator, JFrame parent)
  {
  }
  
  public Class getEvaluatorClass()
  {
    return Double.class;
  }
  
  public String getName()
  {
    return "Bridginess";
  }
  
  public String getGroup()
  {
    return "Distance";
  }
  
  public class Evaluator implements RelationEvaluator
  {
    protected Relation r;
    protected String name;
		protected boolean invertSense;
    
    protected double[][] bridginesses;
    
    public Evaluator()
    {
      this.r = null;
      this.name = "Bridginess";
			this.invertSense = true;
    }
    
    public void runEvaluator()
    {
      int nSize = r.getNodeCount();
			double maxEdges = 1.0 * (nSize * (nSize-1));

			// create a ValuedListGraph for this relation
			ValuedListGraph g = new ValuedListGraph(r.getGraph(), true);
			
			// get a set of shortest paths from each node
			Path[][] shortestPaths = new Path[nSize][];
			for(int i=0; i<nSize; i++)
			{
				shortestPaths[i] = g.getShortestPaths(i);
			}
			
			// for each (directed) edge
      for(int ei=0; ei<nSize; ei++)
      {
        for(int ej=0; ej<nSize; ej++)
        {
					// only evaluate for edges that exist
          if(r.getTieStrength(ei, ej) != 0.0)
          {
						// sum up the number of times this edge is on
						// the minimum spanning tree from ni to nj
            double total = 0.0;
						
						// this seems like it may take a while...
						for(int ni=0; ni<nSize; ni++)
						{
							for(int nj=0; nj<nSize; nj++)
							{
								if(ni != nj)
								{
									// look at all of the shortest paths that start at ni
									int pathCt = 0;
									int containCt = 0;
									for(int pi=0; pi<shortestPaths[ni].length; pi++)
									{
										int edgeIdx = shortestPaths[ni][pi].getEdgeIdx(ei, ej);
										int vertexIdx = shortestPaths[ni][pi].getVertexIdx(nj);
										
										// does this path go through nj?
										if(vertexIdx != -1)
										{
											pathCt++;
											
											// does the edge[ei, ej] precede the point
											// nj on this path?
											if((edgeIdx != -1) && (edgeIdx < vertexIdx))
											{
												containCt++;
											}
										}
									}
									if(pathCt != 0)
									{
										total += ((1.0*containCt)/(1.0*pathCt));
									}
 								}
							}
						}
						bridginesses[ei][ej] = total / maxEdges;
          }
          else
          {
            bridginesses[ei][ej] = 0.0;
          }
        }
      }
    }
    
    public void setRelation(Relation r)
    {
      this.r = r;
      int nSize = r.getNodeCount();
      bridginesses = new double[nSize][];
      for(int i=0; i<nSize; i++)
      {
        bridginesses[i] = new double[nSize];
        for(int j=0; j<nSize; j++)
        {
          bridginesses[i][j] = 0.0;
        }
      }
    }
    
    public Object evaluateRelation(int fromIdx, int toIdx)
    {
      return new Double(bridginesses[fromIdx][toIdx]);
    }
    
    public String getName()
    {
      return name;
    }
    
    public RelationEvaluatorFactory getFactory()
    {
      return BridgeFactory.this;
    }
  }
}
