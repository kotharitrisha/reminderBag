/**
 * FastDistanceFactory.java
 *
 * A FastDistanceFactory generates FastDistanceEvaluator objects,
 * which measures the geodesic distance between two actors in a
 * relation.  Non-zero distances in the original relation are interpreted
 * as a distance of 1.0, and the relation is assumed to be symmetric.
 *
 * (c) 2001 Wibi Internet
 */

package com.wibinet.networks;

import java.awt.*;
import javax.swing.*;
import com.wibinet.math.*;

public class FastDistanceFactory implements RelationEvaluatorFactory
{
	private static boolean nativeLibImplemented = false;
	
	static
	{
		try
		{
			// try to load native library
			System.loadLibrary("fastdist");
			nativeLibImplemented = true;
		}
		catch(Throwable t)
		{
		}
	}
	
  public RelationEvaluator newInstance()
  {
  	return new FastDistanceEvaluator();
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
  	return "Geodesic Distance";
  }
  
  public String getGroup()
  {
  	return "Distance";
  }

	protected class FastDistanceEvaluator implements RelationEvaluator
	{
	  protected Relation r;
	  protected String name;

	  protected double[][] distances;
	  
	  public FastDistanceEvaluator()
	  {
	  	this.r = null;
	  	this.name = "Geodesic Distance";
	  }
	  
	  public void runEvaluator()
	  {
	  	// create a list graph out of this relation
			Graph g = r.getGraph();
			ValuedListGraph lg;
			if(g instanceof ValuedListGraph)
			{
				lg = (ValuedListGraph)g;
			}
			else
			{
				lg = new ValuedListGraph(g, true);
			}
	  	int size = lg.getNodeCount();
  		distances = new double[size][];
	  	for(int i=0; i<distances.length; i++)
	  	{
	  		distances[i] = lg.getPathLengths(i);
	  	}
	  }
	  
  	public void setRelation(Relation r)
  	{
  		this.r = r;
   	}

  	public Object evaluateRelation(int fromIdx, int toIdx)
  	{
  		return new Double(distances[fromIdx][toIdx]);
  	}
  	
  	public String getName()
  	{
  		return name;
  	}
  	
  	public RelationEvaluatorFactory getFactory()
  	{
  		return FastDistanceFactory.this;
  	}
	}
}