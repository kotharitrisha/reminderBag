/**
 * SimulatedAnnealingOptimizer.java
 *
 * A blockmodel optimizer (loosely?) based on the concept
 * of simulated annealing.  This should in theory work better
 * than the local search optimizer because, among other things, 
 * it's less likely to get trapped (early) at a local maxima.
 *
 * Note: next - augment this to allow for the possibility of
 *   more than one step at a time.  probably using a p(nextStep)
 *   parameter, so in theory it's possible (if not likely) to
 *   take an infinite number of steps per turn.
 *
 * Copyright (c) 2003 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import com.wibinet.ai.core.*;
import com.wibinet.gui.*;
import com.wibinet.math.BasicPartition;
import com.wibinet.math.Partition;

public class SimulatedAnnealingOptimizer extends Object
	implements ModelOptimizer
{
 	protected Hashtable props;
 	protected JDialog progressDialog;
 	protected boolean searchStopped;

	protected final static String START_TEMP = "Starting Temp";
	protected final static String DECAY_RATE = "Decay Rate";
	protected final static String MAX_ITER = "Max. Iterations";
	protected final static String STEP_PROB = "Step Prob.";
	
	protected final static Random random = new Random();
	
	private static java.text.NumberFormat fmt = java.text.NumberFormat.getInstance();
	static
	{
		fmt.setMaximumFractionDigits(4);
	}
	
	public SimulatedAnnealingOptimizer()
	{
 	  props = new Hashtable();
 	  props.put(START_TEMP, new Double(10.0));
 	  props.put(DECAY_RATE, new Double(0.01));
		props.put(MAX_ITER, new Integer(500));
		props.put(STEP_PROB, new Double(0.8));
	}
	
	public Blockmodel optimize(NetworkData nData, ModelMeasure measure,
		BlockmodelFactory mFactory)
	{
		// get properties
		double currentTemp = getDoubleProperty(START_TEMP);
		double decayRate = getDoubleProperty(DECAY_RATE);
		int maxIter = getIntProperty(MAX_ITER);
		double stepProb = getDoubleProperty(STEP_PROB);
		
		// determine score-bias
		double scoreBiasDel = measure.getBestScore() - measure.getWorstScore();
		double scoreBias = (scoreBiasDel > 0.0)?1.0:-1.0;
		if(scoreBiasDel == 0.0) scoreBias = 0.0; // ?

		// generate intial blockmodel
		int nSize = nData.getSize();
		Partition p = new BasicPartition(nSize);
		for(int ai=0; ai<nSize; ai++)
		{
			p.setPartition(ai, 0);
		}
		Blockmodel model = mFactory.newInstance(nData, p);
		double score = measure.getScore(model);
		
		for(int iter=0; iter<maxIter; iter++)
		{
			// copy the partition
			Partition newP = new BasicPartition(nSize);
			for(int ai=0; ai<nSize; ai++)
			{
				newP.setPartition(ai, p.getPartition(ai));
			}

			// take multiple steps
			int stepCt = 0;
			while(random.nextDouble() < stepProb)
			{
				// pick a random move type
				if(random.nextBoolean())
				{
					// split
					int[] pIndices = newP.getPartitionIndices();
					int pIndex = pIndices[Math.abs(random.nextInt()) % pIndices.length];
					
					// find the lowest unused partition number
					boolean[] numUsed = new boolean[pIndices.length];
					for(int i=0; i<numUsed.length; i++)
					{
						numUsed[i] = false;
					}
					for(int i=0; i<pIndices.length; i++)
					{
						if(pIndices[i] < numUsed.length)
						{
							numUsed[pIndices[i]] = true;
						}
					}
					int newPartitionNum = numUsed.length;
					for(int i=0; i<numUsed.length; i++)
					{
						if(!numUsed[i])
						{
							newPartitionNum = i;
							break;
						}
					}
					
					// randomly assign some actors in selected partition
					// to the new partition
					for(int i=0; i<nSize; i++)
					{
						if(newP.getPartition(i) == pIndex)
						{
							if(random.nextBoolean())
							{
								newP.setPartition(i, newPartitionNum);
							}
						}
					}
				}
				else
				{
					// join
					int[] pIndices = newP.getPartitionIndices();
					if(pIndices.length > 1)
					{
						int step1 = Math.abs(random.nextInt()) % pIndices.length;
						int step2 = Math.abs(random.nextInt()) % (pIndices.length-1);
						
						int pindex1 = pIndices[step1];
						int pindex2 = pIndices[(step1 + step2 + 1) % pIndices.length];
						
						// make sure pindex1 is less than pindex2, just
						// so we always choose the lower number to join to
						if(pindex1 > pindex2)
						{
							int tmp = pindex1;
							pindex1 = pindex2;
							pindex2 = tmp;
						}
						
						// join 'em up
						for(int i=0; i<nSize; i++)
						{
							if(newP.getPartition(i) == pindex2)
							{
								newP.setPartition(i, pindex1);
							}
						}
					}
				}
				
				stepCt++;
			}
			
			// okay, now use newP to generate a new blockmodel
			Blockmodel newModel = mFactory.newInstance(nData, newP);
			double newScore = measure.getScore(newModel);
			
			System.out.println("t: " + fmt.format(currentTemp) + 
				" p: " + p + "("+fmt.format(score)+") newP: " + newP +
				"("+fmt.format(newScore)+") stepCt="+stepCt);

			// wacky chris wheat calculations ahead!!!
			// this is basically inverted...an 'improvement' means that
			// newScore > score, and as such, lgScoreRatio will be negative
			// for all improvements.  the idea is that for 'backward' steps
			// score/newScore will be positive, but if it's too positive
			// i.e. greater than currentTemp, then we won't take the
			// step. 
			double lgScoreRatio = Math.log(score/newScore) * scoreBias;
			if(lgScoreRatio < currentTemp)
			{
				p = newP;
				score = newScore;
				model = newModel;
			}
			
			// drop tempurature
			currentTemp *= (1.0 - decayRate);
		}
		
		return model;
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
	
	protected int getIntProperty(String name)
	{
		return ((Integer)props.get(name)).intValue();
	}
	

	protected double getDoubleProperty(String name)
	{
		return ((Double)props.get(name)).doubleValue();
	}
	
	public Object getProperty(String name)
	{
		return props.get(name);
	}
	
	public void setProperty(String name, Object value)
	{
		// probably want to validate here
		if(START_TEMP.equals(name) || DECAY_RATE.equals(name) ||
		   STEP_PROB.equals(name))
		{
			if(!(value instanceof Double))
			{
				return;
			}
		}
		if(MAX_ITER.equals(name))
		{
			if(!(value instanceof Integer))
			{
				return;
			}
		}
		props.put(name, value);
	}

	public String toString()
	{
		return "Simulated Annealing";
	}
}