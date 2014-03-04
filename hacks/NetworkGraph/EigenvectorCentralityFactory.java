/**
 * EigenvectorCentralityFactory.java
 *
 * Copyright (c) 2008 Wibi Internet.
 * All rights reserved.
 */
 
package com.wibinet.networks;

import javax.swing.*;
import com.wibinet.math.Graph;
import com.wibinet.math.Matrix;
import com.wibinet.math.EigenvalueDecomposition;

public class EigenvectorCentralityFactory implements NodeEvaluatorFactory
{
	public NodeEvaluator newInstance()
	{
		return new Evaluator();
	}
  
	public void edit(NodeEvaluator evaluator, JFrame parent)
	{
		return;
	}
  
	public Class getEvaluatorClass()
	{
		return Double.class;
	}
  
	public String getName()
	{
		return "Eigenvector Centrality";
	}
  
	public String getGroup()
	{
		return "Centrality";
	}
  
	public boolean isMultiple()
	{
		return true;
	}
	
	public class Evaluator implements NodeEvaluator
	{
		protected NetworkData nd;
		protected String name;
		protected double[][] values;
    
		public Evaluator()
		{
			this.nd = null;
			this.name = "Ev";
			this.values = null;
		}
    
		public void runEvaluator()
		{
			int relCt = nd.getRelationCount();
			int size = nd.getSize();
			for(int ri=0; ri<relCt; ri++)
			{
				Matrix M = null;
				Relation rel = nd.getRelation(ri);
				Graph g = rel.getGraph();
				if(g instanceof Matrix)
				{
					M = (Matrix)g;
				}
				else
				{
					M = new Matrix(size, size);
					for(int i=0; i<size; i++)
					{
						for(int j=0; j<size; j++)
						{
							M.values[i][j] = rel.getTieStrength(i, j);
						}
					}
				}
				EigenvalueDecomposition evd = new EigenvalueDecomposition(M);
				Matrix V = evd.getV();
				for(int ai=0; ai<size; ai++)
				{
					values[ri][ai] = V.values[ai][0]; // hope that's right
				}
			}
		}
    
		public void setNetwork(NetworkData nd)
		{
			this.nd = nd;
			int relCt = nd.getRelationCount();
			int size = nd.getSize();
			this.values = new double[relCt][size];
		}
	  
		public Object evaluateNode(int idx, int ri)
		{
			return new Double(values[ri][idx]);
		}
		  
		public Object evaluateNode(int idx)
		{
			// sum it up, i guess?
			double sum = 0;
			int relCt = nd.getRelationCount();
			for(int ri=0; ri<relCt; ri++) {
				sum += values[ri][idx];
			}
			return new Double(sum);
		}
	  
		public String getName()
		{
			return name;
		}
	  
		public NodeEvaluatorFactory getFactory()
		{
			return EigenvectorCentralityFactory.this;
		}
	}
}