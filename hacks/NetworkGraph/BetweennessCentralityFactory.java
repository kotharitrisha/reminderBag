/**
 * BetweennessCentralityFactory.java
 *
 * Class that efficiently calculates betweenness centrality
 * (at least for dichotomous networks)
 *
 * Based on algorithm from 
 *   Brandes, Ulrik. 2001. A faster algorithm for betweenness centrality.
 *   Journal of Mathematical Sociology 25(2): 163-177
 *   http://www.inf.uni-konstanz.de/algo/publications/b-fabc-01.pdf
 * 
 * Copyright (c) 2009 Christopher Wheat.
 * All rights reserved.
 */

package com.wibinet.networks;

import javax.swing.*;
import com.wibinet.math.Graph;
import com.wibinet.math.ListGraph;

public class BetweennessCentralityFactory implements NodeEvaluatorFactory
{
	public NodeEvaluator newInstance()
	{
		return new Evaluator();
	}
	
	public void edit(NodeEvaluator evaluator, JFrame parent)
	{
		
	}
	
	public Class getEvaluatorClass()
	{
	    return Double.class;
	}
	  
	public String getName()
	{
	    return "Betweenness Centrality";
	}
	  
	public String getGroup()
	{
	    return "Centrality";
	}
	  
	public boolean isMultiple()
	{
		return false;
	}
			
	public class Evaluator implements NodeEvaluator
	{
	    protected NetworkData nd;
	    protected String name;
	    protected int activeRelIdx;
	    protected double[][] centralities;
	    
	    public Evaluator()
	    {
	    	this.nd = null;
	    	this.name = "Betweenness";
	    	this.activeRelIdx = 0;
	    }
	    
	    public void runEvaluator()
	    {
	    	for(int ri=0; ri<nd.getRelationCount(); ri++)
	    	{
	    		evaluateRelation(ri);
	    	}
	    }
	    
	    protected void evaluateRelation(int ri)
	    {
	    	// A ListGraph is the best representation for this algorithm
	    	// see if we already have one.
	    	Relation r = nd.getRelation(ri);
	    	Graph g = r.getGraph();
	    	ListGraph lg = null;
	    	if(g instanceof ListGraph)
	    	{
	    		lg = (ListGraph)g;
	    	}
	    	else
	    	{
	    		lg = new ListGraph(g, true); // i don't like this...need to sort out
	    	}
	    	
	    	int nodeCt = nd.getSize();
	    	
	    	// maybe a little unstable, but we're going to reuse stack & queue
	    	Stack sortedVertices = new Stack(nodeCt);
    		Queue unprocessedVertices = new Queue(nodeCt); // aka Q
	    	
	    	// outer loop (s \el V)
	    	for(int ni=0; ni<nodeCt; ni++)
	    	{
	    		sortedVertices.reset();
	    		
	    		// create an array of lists
	    		List[] predecessors = new List[nodeCt]; // aka P
	    		
	    		double[] numShortestPaths = new double[nodeCt]; // aka sigma
	    		int[] distance = new int[nodeCt]; // aka d
	    		for(int nj=0; nj<nodeCt; nj++)
	    		{
	    			numShortestPaths[nj] = 0.0;
	    			distance[nj] = -1;
	    		}
	    		numShortestPaths[ni] = 1.0;
	    		distance[ni] = 0;
	    		
	    		// clear out queue
	    		unprocessedVertices.reset();
	    		
	    		// s <- Q
	    		unprocessedVertices.enqueue(ni);
	    		
	    		while(!unprocessedVertices.isEmpty())
	    		{
	    			int v = unprocessedVertices.dequeue();
	    			sortedVertices.push(v);
	    			
	    			// get neighbors...hope this is symmetric?
	    			int[] neighbors = lg.getOutNeighborhood(v);
	    				    				    			
	    			for(int nj=0; nj<neighbors.length; nj++)
	    			{
	    				int w = neighbors[nj];
	    				
	    				// w found for first time?
	    				if(distance[w] < 0)
	    				{
	    					// System.err.println("On "+ni+" found "+w+" for the first time");
	    					unprocessedVertices.enqueue(w);
	    					distance[w] = distance[v] + 1;
	    				}
	    				
	    				// shortest path to w through v?
	    				if(distance[w] == distance[v] + 1)
	    				{
	    					numShortestPaths[w] = numShortestPaths[w] + numShortestPaths[v];
	    					
	    					// append v-> P[w]
	    					predecessors[w] = new List(v, predecessors[w]);
	    				}
	    			}
	    		}
	    		
	    		double[] dependencies = new double[nodeCt]; // aka delta
	    		while(!sortedVertices.isEmpty())
	    		{
	    			int w = sortedVertices.pop();
	    			
	    			// for v in p
	    			List pv = predecessors[w];
	    			while(pv != null)
	    			{
	    				int v = pv.getValue();
	    				
	    				dependencies[v] = dependencies[v] +
	    					numShortestPaths[v]/numShortestPaths[w] * (1.0 + dependencies[w]);
	    				pv = pv.getNext();
	    			}

	    			if(w != ni)
    				{
    					centralities[ri][w] += dependencies[w];
    				}
	    		}
	    	}
	    	
	    }
	    
	    public void setNetwork(NetworkData nd)
	    {
	    	this.nd = nd;
	    	this.activeRelIdx = 0;
	    	
	    	int relCt = nd.getRelationCount();
	    	int nodeCt = nd.getSize();
	    	
	    	this.centralities = new double[relCt][];
	    	for(int ri=0; ri<relCt; ri++)
	    	{
	    		centralities[ri] = new double[nodeCt];
	    	}
		}
		  
	    public Object evaluateNode(int idx, int ri)
	    {
	    	int denum = (nd.getSize()-1)* (nd.getSize()-2);
	    	// might want to have a condition for directed and undirected graph. 
	    	// undirected graph is (n-1)(n-2)
	    	return new Double(centralities[ri][idx]/denum);
	    }
		  
	    public Object evaluateNode(int idx)
		{
	    	return evaluateNode(idx, 0);
		}
		  
	    public String getName()
	    {
		    return name;
	    }
		  
	    public NodeEvaluatorFactory getFactory()
	    {
		    return BetweennessCentralityFactory.this;
	    }
	}
	
	protected class Stack extends Object
	{
		protected int[] contents;
		protected int top;
		
		public Stack(int size)
		{
			this.contents = new int[size];
			this.top = -1;
		}
		
		public void push(int v)
		{
			this.top++;
			this.contents[top] = v;
		}
		
		public int pop()
		{
			int r = contents[top];
			top--;
			return r;
		}
		
		public void reset()
		{
			top = -1;
		}
		
		public boolean isEmpty()
		{
			return (top < 0);
		}
	}
	
	// not a proper list but will do for this application
	public class List extends Object
	{
		protected List next;
		protected int value;
		
		public List(int value, List next)
		{
			this.value = value;
			this.next = next;
		}
		
		public int getValue()
		{
			return value;
		}
		
		public List getNext()
		{
			return next;
		}
	}
	
	public class Queue extends Object
	{
		protected int[] contents;
		protected int head;
		protected int tail;

		public Queue(int size)
		{
			this.contents = new int[size];
			this.head = 0;
			this.tail = 0;
		}
		
		public void enqueue(int val)
		{
			contents[head] = val;
			head++;
			if(head >= contents.length)
			{
				head = 0;
			}
		}
		
		public int dequeue()
		{
			int r = contents[tail];
			tail++;
			if(tail >= contents.length)
			{
				tail = 0;
			}
			return r;
		}
		
		public void reset()
		{
			this.head = 0;
			this.tail = 0;
		}
		
		public boolean isEmpty()
		{
			return head==tail;
		}
	}
}
