/**
 * PStar Evaluator.java
 *
 * At some point it may be worth thinking about how to expand
 * this to analyze multiple relations.
 *
 * Copyright (c) 2003 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.io.*;
import com.wibinet.gui.*;
import com.wibinet.math.LogitSolver;
import com.wibinet.math.Matrix;
import com.wibinet.math.RegressionResult;

public class PStarEvaluator extends Object
{
	protected NetworkData nData;
	protected NetworkMeasure[] measures;
	protected LogitSolver solver;
	
	public PStarEvaluator(NetworkData nData, NetworkMeasure[] measures)
	{
		System.err.println("PStarEvaluator._constructor()");
		this.nData = nData;
		this.measures = measures;
		
		int size = nData.getSize();
		int relCt = nData.getRelationCount();
		
		int[] d = new int[size * (size-1)]; 
		double[][] expl = new double[size * (size-1)][];
		// NetworkData tmpNet = new NetworkData(nData);
		NetworkData tmpNet = nData;
		
		int rowCt = 0;
		for(int ri=0; ri<relCt; ri++)
		{
			for(int i=0; i<size; i++)
			{
				for(int j=0; j<size; j++)
				{
					if(i != j)
					{
						expl[rowCt] = new double[measures.length];
						double tieVal = tmpNet.getTieStrength(ri, i, j);
						d[rowCt] = (tieVal != 0.0)?1:0; // hmm...
						for(int mi=0; mi<measures.length; mi++)
						{
							// calc measure for Y* = 1
							tmpNet.setTieStrength(ri, i, j, 1.0);
							double meas1 = measures[mi].getStatistic(tmpNet);
							
							// calc measure for Y* = 0
							tmpNet.setTieStrength(ri, i, j, 0.0);
							double meas0 = measures[mi].getStatistic(tmpNet);
							
							if(meas0 != 0.0)
							{
								expl[rowCt][mi] = Math.log(meas1/meas0);
							}
							else
							{
								expl[rowCt][mi] = Double.NaN;
							}
						}
						
						// restore old values
						tmpNet.setTieStrength(ri, i, j, tieVal);
						rowCt++;
					}
				}
			}
		}
		
		// HACK!
		// dump to a file...
		try
		{
			File logitFile = new File("logit.txt");
			System.err.println("writing to: " + logitFile.getCanonicalPath());
			FileOutputStream fos = new FileOutputStream(logitFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw = new BufferedWriter(osw, 8096);
			PrintWriter out = new PrintWriter(bw);
			
			for(int j=0; j<measures.length; j++)
			{
				out.print(measures[j].toString().replaceAll("[^\\w]+","_") + "\t");
			}
			out.println("tie");
			for(int i=0; i<expl.length; i++)
			{
				for(int j=0; j<expl[i].length; j++)
				{
					out.print(""+(Double.isNaN(expl[i][j])?".":(""+expl[i][j]))+"\t");
				}
				out.println(d[i]);
			}
			out.flush();
			
			// write regression equation for convenience?
			boolean bad[] = new boolean[measures.length];
			for(int j=0; j<measures.length; j++)
			{
				bad[j] = true;
				for(int i=0; i<expl.length; i++)
				{
					if(!Double.isNaN(expl[i][j]))
					{
						bad[j] = false;
						break;
					}
				}
			}
			out.print("logit tie ");
			for(int j=0; j<measures.length; j++)
			{
				if(!bad[j])
				{
					out.print(" " + measures[j].toString().replaceAll("[^\\w]+","_") + "\t");
				}
			}
			out.println();
			
			fos.close();
			System.err.println("done");
			System.exit(0);
		}
		catch(IOException ioe)
		{
			System.err.println("Caught: " + ioe);
			ioe.printStackTrace();
			System.exit(1);
		}
		
		this.solver = new LogitSolver(expl, d);
	}
	
	public Matrix getPredictedTieMatrix()
	{
		int size = nData.getSize();
		double[] p = solver.getFittedProbabilities();
		Matrix pties = new Matrix(size);
		int idx = 0;
		for(int i=0; i<size; i++)
		{
			for(int j=0; j<size; j++)
			{
				if(i == j)
				{
					pties.setValueAt(i, j, 0.0);
				}
				else
				{
					pties.setValueAt(i, j, (p[idx] > 0.5)?1.0:0.0);
					idx++;
				}
			}
		}
		return pties;
	}
	
	public Matrix getTieProbsMatrix()
	{
		int size = nData.getSize();
		double[] p = solver.getFittedProbabilities();
		Matrix pties = new Matrix(size);
		int idx = 0;
		for(int i=0; i<size; i++)
		{
			for(int j=0; j<size; j++)
			{
				if(i == j)
				{
					pties.setValueAt(i, j, 0.0);
				}
				else
				{
					pties.setValueAt(i, j, p[idx]);
					idx++;
				}
			}
		}
		return pties;
	}
	
	public double getLogLikelihood()
	{
		return solver.getLogLikelihood();
	}
	
	public RegressionResult[] getRegressionResults()
	{
		return solver.getRegressionResults();
	}
}