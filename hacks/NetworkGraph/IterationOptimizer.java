/**
 * IterationOptimizer.java
 *
 * This optimizer takes a partition collection and iterates through
 * all partitions therein.
 *
 * Copyright (c) 2003 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import com.wibinet.math.BasicPartition;
import com.wibinet.math.Partition;
import com.wibinet.math.PartitionCollection;

public class IterationOptimizer implements ModelOptimizer
{
 	protected Hashtable props;
	protected Iterator pIterator;
	
	public IterationOptimizer()
	{
 	  props = new Hashtable();
	}
	
	public Blockmodel optimize(NetworkData nData, ModelMeasure measure,
		BlockmodelFactory mFactory)
	{
		// okay, this is clearly wrong, but...)
		if(nData.getPartitionCollectionCount() < 1)
		{
			return null;
		}
		PartitionCollection pcol = nData.getPartitionCollection(0);
		
		// determine score-bias
		double scoreBiasDel = measure.getBestScore() - measure.getWorstScore();
		double scoreBias = (scoreBiasDel > 0.0)?1.0:-1.0;
		if(scoreBiasDel == 0.0) scoreBias = 0.0; // ?
		
		pIterator = pcol.iterator();
		Partition bestPartition = nData.getPartition();
		Blockmodel bestModel = mFactory.newInstance(nData, bestPartition);
		double bestScore = measure.getScore(bestModel);
		System.out.println("bestScore: " + bestScore);
		
		while(pIterator.hasNext())
		{
			Partition p = (Partition)pIterator.next();
			Blockmodel model = mFactory.newInstance(nData, p);
			double score = measure.getScore(model);
			System.out.println("p: " + p + " = " + score);
			if(score * scoreBias > bestScore * scoreBias)
			{
				bestModel = model;
				bestScore = score;
			}
		}
		
		return bestModel;
	}
	
	public String[] getPropertyNames()
	{
		Enumeration keys = props.keys();
		String[] names = new String[props.size()];
		for(int ni=0; ni<names.length; ni++)
		{
			names[ni] = (String)keys.nextElement();
		}
		return names;
	}
	
	public Object getProperty(String name)
	{
		return props.get(name);
	}
	
	public void setProperty(String name, Object value)
	{
		// probably want to validate here
		/*if(BLOCKS.equals(name) || RUNS.equals(name))
		{
			if(!(value instanceof Integer))
			{
				return;
			}
		}*/
		props.put(name, value);
	}

	public String toString()
	{
		return "Partition Iterator";
	}
}