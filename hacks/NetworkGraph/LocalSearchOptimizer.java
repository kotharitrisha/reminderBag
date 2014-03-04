/**
 * LocalSearchOptimizer.java
 *
 * (c) 2000 Wibi Internet
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

public class LocalSearchOptimizer implements ModelOptimizer, ActionListener
{
 	protected Hashtable props;
 	protected JMultiTicker ticker;
 	protected JDialog progressDialog;
 	protected boolean searchStopped;

	protected final static String BLOCKS = "numBlocks";
	protected final static String RUNS   = "numRuns";
	
	protected final static Random random = new Random();
	
	public LocalSearchOptimizer()
	{
 	  props = new Hashtable();
 	  props.put(BLOCKS, new Integer(5));
 	  props.put(RUNS, new Integer(10));
 	  
 	  // ?
 	  searchStopped = false;
	}
	
	public Blockmodel optimize(NetworkData nData, ModelMeasure measure,
		BlockmodelFactory mFactory)
	{
		// get nubmer of blocks & number of runs;
		int numBlocks = getIntProperty(BLOCKS);
		int numRuns   = getIntProperty(RUNS);
		
		// setup the multi ticker & dialog
		ticker = new JMultiTicker();
		
		// temporary hack...
		SearchTerminator terminator;
		if(numRuns > 0)
		{
			terminator = new MaxIterationTerminator(numRuns);
			JFrame nullFrame = null;
                        progressDialog = new JDialog(nullFrame, "Local Search Progress");
			progressDialog.getContentPane().setLayout(new BorderLayout());
			progressDialog.getContentPane().add(BorderLayout.CENTER, ticker);
			progressDialog.setSize(300, 200);

			// not sure we should even have this...
			/*JPanel stopPanel = new JPanel(new FlowLayout());
			JButton stopButton = new JButton("Stop");
			stopButton.addActionListener(this);
			stopPanel.add(stopButton);
			dialog.getContentPane().add(BorderLayout.SOUTH, stopPanel); */
		}
		else // otherwise use a regular terminator dialog
		{
			terminator = new TerminatorDialog(null, "Local Search Progress",
				ticker);
			progressDialog = (TerminatorDialog)terminator;
		}
		progressDialog.setVisible(true);
		
		// calculate neighborhood size
		int nSize = nData.getSize();
		int numTransitions = nSize * (numBlocks - 1);
		int numTranspositions = nSize * (nSize-1) / 2;
		Blockmodel[] neighborhood = new Blockmodel[numTransitions + numTranspositions + 2];
		double[] nbdScores = new double[neighborhood.length];
		
		// remember best partition
		Blockmodel bestModel = null;

		// note that this search has not been stopped
 	  searchStopped = false;
 	  int iteration = 0;
 	  double bestScore = 0.0;
 	  double bestIterationScore = 0.0;
 	  
 	  int minPrecision = mFactory.getMinPrecision();
 	  int maxPrecision = mFactory.getMaxPrecision();
 	  
 	  while(!searchStopped &&
 	  	!terminator.terminate(iteration, bestScore, bestIterationScore))
		{
			System.out.println("iteration = " + iteration);
			
			// generate intial blockmodel
			Partition p = new BasicPartition(nData.getSize());
			for(int ai=0; ai<nSize; ai++)
			{
				p.setPartition(ai, getRandomInt(numBlocks));
			}
			Blockmodel model = mFactory.newInstance(nData, p);
			model.setPrecision((minPrecision + maxPrecision) / 2);
			
			bestModel = model;
			bestIterationScore = measure.getScore(model);
			double lastScore = bestIterationScore;
			boolean atLocalMinimum = false;
			while(!atLocalMinimum)
			{
				System.out.println("lastScore = " + lastScore);
				
				// create neighborhood
				createNeighborhood(model, neighborhood, mFactory, 
				  nData, nSize, numBlocks);
				
				// get scaled scores
				getScaledScores(neighborhood, nbdScores, measure);
				
				// add scores to ticker
				ticker.addPoints(nbdScores);
				try
				{
					Thread.sleep(1);
				}
				catch(Exception e){}
				// get best from neighborhood
				Blockmodel nBest = neighborhood[getArrayMaxIndex(nbdScores)];
				bestIterationScore = measure.getScore(nBest);
				
				atLocalMinimum = (bestIterationScore == lastScore);
				model = nBest;
				lastScore = bestIterationScore;

				// is this the best yet?
				if(bestIterationScore > bestScore)
				{
					bestModel = model;
					bestScore = bestIterationScore;
				}

				if(atLocalMinimum)
				{
					bestModel = model;
					bestScore = lastScore;
					System.out.println("bestModel = " + bestModel);
				}
			}
			
			// increment iteration
			iteration++;
		}
		
		// clean up
		if(progressDialog.isVisible());
		{
      progressDialog.setVisible(false);
		}
		System.out.println("bestModel = " + bestModel);

		return bestModel;
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		searchStopped = true;
		progressDialog.setVisible(false);
	}
	
	protected void createNeighborhood(Blockmodel model, Blockmodel[] neighborhood,
		BlockmodelFactory mFactory, NetworkData nData, int nSize, int numBlocks)
	{
		int pIdx = 0;
		Partition p = model.getPartition();
		
		// do transitions
		for(int ai=0; ai<nSize; ai++)
		{
			int currentP = p.getPartition(ai);
			for(int pi=0; pi<numBlocks; pi++)
			{
				if(currentP != pi)
				{
					BasicPartition newP = new BasicPartition(p);
					newP.setPartition(ai, pi);
					neighborhood[pIdx++] = mFactory.newInstance(nData, newP);
				}
			}
		}
		
		// do transpositions
		for(int ai=0; ai<nSize; ai++)
		{
			int p1 = p.getPartition(ai);
			for(int ai2=ai+1; ai2<nSize; ai2++)
			{
				int p2= p.getPartition(ai2);
				BasicPartition newP = new BasicPartition(p);
				newP.setPartition(ai, p2);
				newP.setPartition(ai2, p1);
				neighborhood[pIdx++] = mFactory.newInstance(nData, newP);
			}
		}
		
		// do precision adjusts
		Blockmodel mUp = mFactory.newInstance(nData, p);
		mUp.setPrecision(model.getPrecision() + 1);
		neighborhood[pIdx++] = mUp;
		Blockmodel mDown = mFactory.newInstance(nData, p);
		mDown.setPrecision(model.getPrecision() - 1);
		neighborhood[pIdx++] = mDown;
	}
	
	protected void getScaledScores(Blockmodel[] mSet, double[] scores,
		ModelMeasure measure)
	{
		// don't forget to scale scores to get high v. low scores right
		double worstScore = measure.getWorstScore();
		double divisor = measure.getBestScore() - measure.getWorstScore();
		for(int mi=0; mi<mSet.length; mi++)
		{
			double score = measure.getScore(mSet[mi]);
			scores[mi] = (score - worstScore) / divisor;
		}
	}
	
	protected int getArrayMaxIndex(double[] values)
	{
		int maxIdx = 0;
		double best = values[0];
		for(int vi=1; vi<values.length; vi++)
		{
			if(values[vi] > best)
			{
				best = values[vi];
				maxIdx = vi;
			}
		}
		return maxIdx;
	}
	
	protected int getRandomInt(int range)
	{
		return Math.abs(random.nextInt() % range);
	}
	
	protected int getIntProperty(String name)
	{
		return ((Integer)props.get(name)).intValue();
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
		if(BLOCKS.equals(name) || RUNS.equals(name))
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
		return "Local Search";
	}
}