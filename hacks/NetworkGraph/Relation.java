/**
 * Relation.java
 *
 * Encapsulates the functionality of a network relation(?)
 * Updated 10/1/2002 to be based on com.wibinet.math.Graph
 *
 * Copyright (c) 2000-2003, 2006 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.io.*;
import com.wibinet.math.Graph;
import com.wibinet.math.MatrixGraph;
import com.wibinet.math.MutableGraph;

public class Relation extends Object
{
	protected Graph g;
	protected boolean directed;
	protected boolean measuresDistance;
  protected String name;
  protected NetworkData parent;
  
  /**
   * Basic copy constructor--makes a deep copy of the underlying graph using Graph.cloneGraph()
   * 
   * @param r - relation to be copied
   */
  public Relation(Relation r)
  {
	  this.g = r.g.cloneGraph();
	  this.directed = r.directed;
	  this.measuresDistance = r.measuresDistance;
	  this.name = r.name;
	  this.parent = r.parent;
  }
  
  public Relation(NetworkData parent, String name, boolean directed)
  {
    // super(parent.getSize(), parent.getSize());
    this.parent = parent;
    this.name = name;
    this.directed = directed;
		this.g = new MatrixGraph(parent.getSize());
		this.measuresDistance = true;
  }
	
	public Relation(NetworkData parent, String name, Graph g)
	{
		this.parent = parent;
		this.name = name;
		this.directed = true;
		this.g = g;
		this.measuresDistance = false; // not sure that's the right default...
	}
	
	public boolean isDistanceMeasuring()
	{
		return measuresDistance;
	}
	
	public void setDistanceMeasuring(boolean measuresDistance)
	{
		this.measuresDistance = measuresDistance;
	}
  
  public void setTieStrength(int memberFrom, int memberTo, double strength)
  {
    if(g instanceof MutableGraph)
		{
			MutableGraph mg = (MutableGraph)g;
			
			if(!measuresDistance)
			{
				if(Double.isInfinite(strength))
				{
					strength = 0.0;
				}
				else if(strength == 0.0)
				{
					strength = Double.POSITIVE_INFINITY;
				}
				else
				{
					strength = 1.0 / strength;
				}
			}
			
			if(directed)
			{
				mg.addEdge(memberFrom, memberTo, strength);
				parent.fireDataChanged(new DataChangeEvent(this, DataChangeEvent.VALUE_CHANGED, memberFrom, memberTo));
			}
			else
			{
				mg.addEdge(memberFrom, memberTo, strength);
				parent.fireDataChanged(new DataChangeEvent(this, DataChangeEvent.VALUE_CHANGED, memberFrom, memberTo));
				mg.addEdge(memberTo, memberFrom, strength);
				parent.fireDataChanged(new DataChangeEvent(this, DataChangeEvent.VALUE_CHANGED, memberTo, memberFrom));
			}
		}
  }
  
  public double getTieStrength(int memberFrom, int memberTo)
  {
    double dist = 0.0;
		
		if(directed)
    {
	    dist = g.getDistance(memberFrom, memberTo);
    }
    else
    {
      // this is in lieu of symmetrizing data on a setDirected...
      // if undirected only get upper left 
      if(memberFrom < memberTo)
      {
        dist = g.getDistance(memberFrom, memberTo);
      }
      else
      {
        dist = g.getDistance(memberTo, memberFrom);
      }
    }
		
		if(measuresDistance)
		{
			return dist;
		}
		else
		{
			if(Double.isInfinite(dist))
			{
				return 0.0;
			}
			else if(dist == 0.0)
			{
				return Double.POSITIVE_INFINITY;
			}
			else
			{
				return 1.0/dist;
			}
		}
  }
  
	public int getNodeCount()
	{
		return g.getNodeCount();
	}
	
	public int getEdgeCount()
	{
		return g.getEdgeCount();
	}
	
	public Graph getGraph()
	{
		return g;
	}
	
	public NetworkData getParent()
	{
		return parent;
	}

  public String getName()
  {
    return name;
  }
  
  public void setName(String name)
  {
    this.name = name;
  }
  
  public boolean isDirected()
  {
    return directed;
  }
  
  public void setDirected(boolean directed)
  {
    this.directed = directed;
  }
	
	public boolean isMutable()
	{
		return (g instanceof MutableGraph);
	}

  public void writeXML(PrintWriter out)
  {
  	out.println("<relation name=\""+name+"\" directed=\""+directed+"\">");
  	int sz = parent.getSize();
  	for(int i=0; i<sz; i++)
  	{
  		for(int j=0; j<sz; j++)
  		{
  		 	double ts = getTieStrength(i, j);
  		 	if(ts != 0.0)
  		 	{
  		 		out.println("  <tie from=\""+i+"\" to=\""+j+"\" strength=\""+ts+"\"/>");
  		 	}
  		}
  	}
  	out.println("</relation>");
  }
}