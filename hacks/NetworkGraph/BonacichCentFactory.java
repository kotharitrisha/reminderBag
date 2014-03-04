/**
 * BonacichCentFactory.java
 *
 * This factory generates node evaluators that calculate eigenvector (Bonacich)
 * centrality.  (Need reference)
 *
 * (c) 2000 Wibi Internet
 */
 
package com.wibinet.networks;

import javax.swing.*;

import com.wibinet.math.EigenvalueDecomposition;
import com.wibinet.math.Graph;
import com.wibinet.math.Matrix;

public class BonacichCentFactory implements NodeEvaluatorFactory
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
  		return "Bonacich Centrality";
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
		protected double alpha;
		protected double beta;
    
		public Evaluator()
		{
			this.nd = null;
			this.name = "Bc";
			this.values = null;
			this.alpha = 0.5;
			this.beta = -1.0;
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
				
				// c(alpha, beta) = alpha(I-beta x R)^{-1}xRx1
				Matrix I_minus_betaR = new Matrix(size, size);
				for(int i=0; i<size; i++)
				{
					for(int j=0; j<size; j++)
					{
						I_minus_betaR.values[i][j] = -beta*M.values[i][j];
						if(i == j)
						{
							I_minus_betaR.values[i][j] += 1.0;
						}
					}
				}
				Matrix inv = I_minus_betaR.getPseudoInverse(); // hmmm
				Matrix aProd = new Matrix(size, size);
				for(int i=0; i<size; i++)
				{
					for(int j=0; j<size; j++)
					{
						aProd.values[i][j] = alpha * inv.values[i][j];
					}
				}
				
				// alpha(I-beta x R)^{-1}xR
				Matrix prod2 = aProd.rightMultiply(M);
				
				Matrix ones = new Matrix(size, 1);
				for(int i=0; i<size; i++)
				{
					ones.values[i][0] = 1.0;
				}
				
				Matrix cvalues = prod2.rightMultiply(ones);
				for(int ai=0; ai<size; ai++)
				{
					values[ri][ai] = cvalues.values[ai][0];
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
			return BonacichCentFactory.this;
		}	
	}
}