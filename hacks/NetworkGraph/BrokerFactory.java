/**
 * BrokerFactory.java
 * 
 * This class implements my measure of valued and directed brokerage.
 * An actor i "brokers" a relationship between actors j & k if
 * 1) "i" takes a significant portion of the outflow from "j"
 * 2) "k" takes a significant portion of the outflow from "i"
 * 3) a small portion of the inflow into "k" comes from "j" 
 */

package com.wibinet.networks;

import javax.swing.JFrame;

public class BrokerFactory implements NodeEvaluatorFactory {

	public void edit(NodeEvaluator evaluator, JFrame parent)
	{
		// no editing, yet
		return;
	}

	public Class getEvaluatorClass()
	{
		return Double.class;
	}

	public String getGroup()
	{
		return "Structure"; // "Brokerage"?
	}

	public String getName()
	{
		return "Brokerage";
	}

	public boolean isMultiple()
	{
		return false;
	}

	public NodeEvaluator newInstance()
	{
		return new Evaluator();
	}
	
	public class Evaluator extends Object implements NodeEvaluator
	{
		protected NetworkData nData;
		protected Double[] brokerage;
		protected String name;
		
		public Evaluator()
		{
			this.nData = null;
			this.brokerage = new Double[0];
			this.name = "Brokerage";
		}
		
		public void runEvaluator()
		{
			// need flow totals
			int nSize = nData.getSize();
			double[] inFlow = new double[nSize];
			double[] outFlow = new double[nSize];
			for(int i=0; i<nSize; i++)
			{
				inFlow[i] = nData.getColMarginal(0, i);
				outFlow[i] = nData.getRowMarginal(0, i);
			}
			
			for(int i=0; i<nSize; i++)
			{
				double brokerage_i = 0.0;
				for(int j=0; j<nSize; j++)
				{
					double x_ji = nData.getTieStrength(j, i);
					double p_ji = 0.0;
					if(outFlow[j] > 0.0)
					{
						p_ji = x_ji / outFlow[j];
					}
					if(p_ji > 0.0)
					{
						for(int k=0; k<nSize; k++)
						{
							if((i!=j) && (i!=k) && (k!=j))
							{
								double x_ik = nData.getTieStrength(i, k);
								double p_ik = 0.0;
								if(inFlow[k] > 0.0)
								{
									p_ik = x_ik / inFlow[k];
								}
								brokerage_i += Math.sqrt(p_ji * p_ik); // b_i += b_ijk
							}
						}
					}
				}
				brokerage[i] = new Double(brokerage_i);
			}
		}
		
		public void setNetwork(NetworkData nd)
		{
			this.nData = nd;
			this.brokerage = new Double[nd.getSize()];
		}
		
		public Object evaluateNode(int idx)
		{
			return brokerage[idx];
		}
		
		public Object evaluateNode(int idx, int ri)
		{
			// boo!
			return brokerage[idx];
		}
		
		public String getName()
		{
			return name;
		}
		
		public NodeEvaluatorFactory getFactory()
		{
			return BrokerFactory.this;
		}
	}
}
