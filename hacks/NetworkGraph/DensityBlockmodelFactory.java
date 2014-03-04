/**
 * DensityBlockmodelFactory.java
 *
 * A density blockmodel is a descriptive blockmodel
 * that does a variety of density calculations over
 * each block in a network.
 *
 * Copyright (c) 2003 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.wibinet.gui.*;
import com.wibinet.math.ExtendedMath;
import com.wibinet.math.Matrix;
import com.wibinet.math.Partition;

public class DensityBlockmodelFactory extends AbstractBlockmodelFactory
{
	protected Hashtable props;
	
	public DensityBlockmodelFactory()
	{
		this.props = new Hashtable();
	}
	
	public Blockmodel newInstance(NetworkData nData, com.wibinet.math.Partition p)
	{
		DensityBlockmodel model = new DensityBlockmodel(nData, p);
		return model;
	}
	
	public boolean edit(Blockmodel model)
	{
		// EditDialog dialog = new EditDialog();
		// dialog.setVisible(true);
		return true;
	}
	
	public Hashtable getProperties(Blockmodel model)
	{
		return props;
	}
	
	public void setProperties(Blockmodel model, Hashtable props)
	{
	}
	
	public String getType()
	{
		return "Density";
	}
	
	public class DensityBlockmodel extends AbstractBlockmodel
	{
		protected Matrix[] densities;
		protected Matrix[] deviationsOut;
		protected Matrix[] deviationsIn;
		
		public DensityBlockmodel(NetworkData nData)
		{
			this(nData, nData.getPartition());
		}
		
		public DensityBlockmodel(NetworkData nData, Partition p)
		{
			super(nData, p);
			initDataStructures();
		}
		
		public String getFactoryClass()
		{
			return "com.wibinet.networks.DensityBlockmodelFactory";
		}
		
		protected void initDataStructures()
		{
			resetBlockSizes();
			int relCt = nData.getRelationCount();
			int numBlocks = p.getPartitionCount();
			int nSize = nData.getSize();
			densities = new Matrix[relCt];
			deviationsOut = new Matrix[relCt];
			deviationsIn = new Matrix[relCt];
			for(int rIdx=0; rIdx<relCt; rIdx++)
			{
				densities[rIdx] = new Matrix(numBlocks, numBlocks);
				deviationsOut[rIdx] = new Matrix(numBlocks, numBlocks);
				deviationsIn[rIdx] = new Matrix(numBlocks, numBlocks);
			}
		}

		public double getLgDataProbability()
		{
			int nSize = nData.getSize();
			int relCt = nData.getRelationCount();
			int numBlocks = p.getPartitionCount();
			int[] pIndices = p.getPartitionIndices();
			
			double lgDataProb = 0.0;

			// get predicted density for each block 
			for(int ri=0; ri<relCt; ri++)
			{
				for(int bi=0; bi<numBlocks; bi++)
				{
					int[] actorsI = p.getObjectIndices(pIndices[bi]);
					for(int bj=0; bj<numBlocks; bj++)
					{
						int[] actorsJ = p.getObjectIndices(pIndices[bj]);
						double pDens = densities[ri].getValueAt(bi, bj);
						for(int i=0; i<actorsI.length; i++)
						{
							int ai = actorsI[i];
							for(int j=0; j<actorsJ.length; j++)
							{
								int aj = actorsJ[j];
								if(ai != aj)
								{
									double x_ij = nData.getTieStrength(ri, ai, aj);
									double prob = pDens * x_ij + (1.0-pDens) * (1.0-x_ij);
									lgDataProb += ExtendedMath.lg(prob);
								}
							}
						}
					}
				}
			}
			return lgDataProb;		
		}
		
		public double getLgModelProbability()
		{
			int nSize = nData.getSize();
			int relCt = nData.getRelationCount();
			int numBlocks = p.getPartitionCount();
			int[] pIndices = p.getPartitionIndices();
			
			// each block density parameter is determined by the
			// number of ties in the block
			double modelLength = 0.0;
			for(int bi=0; bi<numBlocks; bi++)
			{
				int biSize = p.getPartitionSize(pIndices[bi]);
				if(biSize > 1)
				{
					modelLength += getOptimalPrecision(biSize * (biSize-1));
				}
				for(int bj=bi+1; bj<numBlocks; bj++)
				{
					int bjSize = p.getPartitionSize(pIndices[bj]);
					int blockSize = biSize * bjSize;
					modelLength += 2.0 * getOptimalPrecision(blockSize);
				}
			}
			return -relCt*modelLength;		
		}
		
		public void compute(AbstractTask task)
		{
			// this is probably redundant, though we could remove it
			// upstream...
			initDataStructures();
			
			// initialize matrices
			int relCt = nData.getRelationCount();
			int numBlocks = p.getPartitionCount();
			int nSize = nData.getSize();
			for(int rIdx=0; rIdx<relCt; rIdx++)
			{
				for(int i=0; i<numBlocks; i++)
				{
					for(int j=0; j<numBlocks; j++)
					{
						double density = getDensity(p, rIdx, i, j);
						densities[rIdx].setValueAt(i, j, density);
						deviationsOut[rIdx].setValueAt(i, j, getDeviationOut(p, rIdx, i, j, density));
						deviationsIn[rIdx].setValueAt(i, j, getDeviationIn(p, rIdx, i, j, density));
					}
				}
			}
		}
		
		protected double getDensity(Partition p, int rIdx, int i, int j)
		{
			// should be type dependent
			return nData.getBlockDensity(p, rIdx, i, j);
		}
		
		protected double getDeviationOut(Partition p, int rIdx, int i, int j, double mean)
		{
			int[] i_actors = p.getObjectIndices(i);
			int[] j_actors = p.getObjectIndices(j);
			double[] i_densities = new double[i_actors.length];
			
			for(int ai=0; ai<i_actors.length; ai++)
			{
				i_densities[ai] = 0.0;
				for(int aj=0; aj<j_actors.length; aj++)
				{
					i_densities[ai] += nData.getTieStrength(rIdx, i_actors[ai], j_actors[aj]);
				}
				if(j_actors.length > 0)
				{
					i_densities[ai] /= j_actors.length;
				}
			}
			
			return getSampleDeviation(i_densities, mean);
		}
		
		protected double getDeviationIn(Partition p, int rIdx, int i, int j, double mean)
		{
			int[] i_actors = p.getObjectIndices(i);
			int[] j_actors = p.getObjectIndices(j);
			double[] j_densities = new double[j_actors.length];
			
			for(int aj=0; aj<j_actors.length; aj++)
			{
				j_densities[aj] = 0.0;
				for(int ai=0; ai<i_actors.length; ai++)
				{
					j_densities[aj] += nData.getTieStrength(rIdx, i_actors[ai], j_actors[aj]);
				}
				if(i_actors.length > 0)
				{
					j_densities[aj] /= i_actors.length;
				}
			}
			
			return getSampleDeviation(j_densities, mean);
		}
		
		protected double getSampleDeviation(double[] x, double mean)
		{
			double sum = 0.0;
			for(int i=0; i<x.length; i++)
			{
				double d = x[i] - mean;
				sum += d * d;
			}
			return Math.sqrt(sum / (x.length - 1));
		}
		
		public void populateMatrices()
		{
			// add matrices
			clearMatrices();
			
			LabelModel pLabels = getPartitionLabels();
			LabelModel aLabels = getActorLabels();
			addMatrices("Densities", densities, pLabels, pLabels);
			addMatrices("Deviations (out)", deviationsOut, pLabels, pLabels);
			addMatrices("Deviations (in)", deviationsIn, pLabels, pLabels);
		}
	
		public double getPredictedTieStrength(int rIdx, int i, int j)
		{
			return nData.getTieStrength(rIdx, i, j);
		}
	}
}