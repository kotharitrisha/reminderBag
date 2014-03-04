/**
 * RangeFactory.java
 *
 * Computes the range (Watts, Duncan J., 1999, "Networks, Dynamics and
 * the Small-World Phenomenon", American Journal of Sociology, 105(2),
 * 493-527.) of edges in the network.
 *
 * Copyright (c) 2002 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.awt.*;
import javax.swing.*;
import com.wibinet.math.ValuedListGraph;

public class RangeFactory implements RelationEvaluatorFactory
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
    return "Range";
  }
  
  public String getGroup()
  {
    return "Distance"; // ?
  }
  
  public class Evaluator implements RelationEvaluator
  {
    protected Relation r;
    protected String name;
    
    protected double[][] range;
    
    public Evaluator()
    {
      this.r = null;
      this.name = "Range";
    }
    
    public void runEvaluator()
    {
      int nSize = r.getNodeCount();
			double[] pathLengths = new double[nSize];
			
			for(int i=0; i<nSize; i++)
			{
				for(int j=0; j<nSize; j++)
				{
					// only evaluate for existing edges
					if((i != j) && (r.getTieStrength(i, j) != 0.0))
					{
						// create a valued list graph without this edge in it
						ValuedListGraph g = new ValuedListGraph(r.getGraph(), true);
						g.removeEdge(i, j);
						g.getMinimumDistanceTree(i, pathLengths); // discard tree itself...
						range[i][j] = pathLengths[j];
					}
				}
			}
    }
    
    public void setRelation(Relation r)
    {
      this.r = r;
      int nSize = r.getNodeCount();
      range = new double[nSize][];
      for(int i=0; i<nSize; i++)
      {
        range[i] = new double[nSize];
        for(int j=0; j<nSize; j++)
        {
          range[i][j] = 0.0;
        }
      }
    }
    
    public Object evaluateRelation(int fromIdx, int toIdx)
    {
      return new Double(range[fromIdx][toIdx]);
    }
    
    public String getName()
    {
      return name;
    }
    
    public RelationEvaluatorFactory getFactory()
    {
      return RangeFactory.this;
    }
  }
}