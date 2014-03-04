/**
 * ExcessFlowFactory.java
 *
 * In a non-negative real-valued network, transforms a relation to
 * show the logarithm of the "excess" flow relative to the flow
 * that would be expected from a linear multiplicative model. 
 *
 * Copyright (c) 2007 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import javax.swing.JFrame;

import com.wibinet.networks.DiscretizeFactory.Evaluator;

public class ExcessFlowFactory implements RelationEvaluatorFactory
{
	public RelationEvaluator newInstance() {
	    return new Evaluator();
	}

	public void edit(RelationEvaluator evaluator, JFrame parent) {
		// for now, nothing
	}

	public Class getEvaluatorClass() {
		return Double.class;
	}

	public String getName()
	{
		return "Excess Flow";
	}

	public String getGroup()
	{
		return "Filter";
	}

	public class Evaluator implements RelationEvaluator
	{
	    protected Relation r;
	    protected String name;
	    protected double[] fracIn;
	    protected double[] fracOut;
	    protected double totalFlow;

	    public Evaluator()
	    {
	      this.r = null;
	      this.name = "Excess Flow";
	      this.fracIn = new double[0];
	      this.fracOut = new double[0];
	      this.totalFlow = 0.0;
	    }
	    
	    public void runEvaluator()
	    {
	    	// determine mean in and out flows
	    	int nCt = r.getNodeCount();

	    	this.totalFlow = 0.0;
	    	for(int i=0; i<nCt; i++)
	    	{
	    		for(int j=0; j<nCt; j++)
	    		{
	    			double x_ij = r.getTieStrength(i, j);
	    			double x_ji = r.getTieStrength(j, i);
	    			fracIn[i] += x_ij;
	    			fracOut[i] += x_ji;
	    			totalFlow += x_ij;
	    		}
	    	}
	    }
	    
	    public void setRelation(Relation r)
	    {
	    	this.r = r;
			int nCt = r.getNodeCount();
			this.fracIn = new double[nCt];
			this.fracOut = new double[nCt];
	    }
	    
	    public Object evaluateRelation(int fromIdx, int toIdx)
	    {
	    	double x_ij = r.getTieStrength(fromIdx, toIdx);
	    	double expectedIn = fracIn[toIdx] / totalFlow;
	    	double expectedOut = fracOut[fromIdx] / totalFlow;
	    	double expectedFlow = expectedIn * expectedOut * totalFlow;
	    	
	    	if((x_ij == 0.0) && (expectedFlow == 0.0))
	    	{
	    		return new Double(0.0);
	    	}
	    	else if(x_ij < 0.0)
	    	{
	    		return new Double(Double.NaN);
	    	}
	    	else if(x_ij == 0.0)
	    	{
	    		return new Double(Double.NEGATIVE_INFINITY);
	    	}
	    	else
	    	{
	    		return new Double(Math.log(x_ij / expectedFlow));
	    	}
	    }
	    
	    public String getName()
	    {
	    	return name;
	    }
	    
	    public RelationEvaluatorFactory getFactory()
	    {
	    	return ExcessFlowFactory.this;
	    }

	}
}
