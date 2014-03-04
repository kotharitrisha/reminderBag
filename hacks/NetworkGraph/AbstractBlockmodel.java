/**
 * AbstractBlockmodel.java
 *
 * Copyright (c) 2000, 2003 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.util.*;
import com.wibinet.gui.*;
import com.wibinet.math.Matrix;
import com.wibinet.math.ExtendedMath;
import com.wibinet.math.Partition;

public abstract class AbstractBlockmodel implements Blockmodel
{
	protected NetworkData nData;
	protected String title;
	protected Partition p;
	protected int precision;
	protected double confidence;
	
	protected int[] pIndices;
	protected int[] bSize;
	protected Vector names;
	protected Vector matrices;
	protected Vector rowLabels;
	protected Vector colLabels;
	protected Vector vReportNames;
	protected Vector vReportHTML;
	
	private static int ctr = 0;

	public AbstractBlockmodel(NetworkData nData, com.wibinet.math.Partition p)
	{
		this.nData = nData;
		this.p = p;
		this.precision = ExtendedMath.MAX_PRECISION;
		this.confidence = 0.95;
		this.pIndices = p.getPartitionIndices();

		/*synchronized(AbstractBlockmodel.class)
		{
			if(ctr == 0)
			{
				this.title = "Untitled Blockmodel";
			}
			else
			{
				this.title = "Untitled Blockmodel " + ctr;
			}
			ctr++;
		}*/
		this.title = "Untitled Blockmodel";

    // reset block sizes once?
    resetBlockSizes();
    
		// setup hashtables
		names = new Vector();
		matrices = new Vector();
		rowLabels = new Vector();
		colLabels = new Vector();
		
		// set up stuff for reports
		vReportNames = new Vector();
		vReportHTML = new Vector();
	}

	public Task getComputationTask()
	{
		return new ComputationTask();
	}
	
	protected class ComputationTask extends AbstractTask
	{
		public void start()
		{
			// need a 'final' pointer to this task
			final AbstractTask thisTask = this;
			final Blockmodel model = AbstractBlockmodel.this;
			final Worker worker = new Worker() {
				public Object construct()
				{
					model.compute(thisTask);
					thisTask.setDone(true);
					return "Done";
				} 
			};
			worker.start();
		}
		
		public void cancel()
		{
		}
		
		public Object getOutputValue()
		{
			return "Ok"; // change this up!
		}
	}
	
	protected void resetBlockSizes()
	{
    // store up block sizes (because getPartitionSize() may be inefficient...)
		this.pIndices = p.getPartitionIndices();
    bSize = new int[pIndices.length];
    for(int bi=0; bi<bSize.length; bi++)
    {
      bSize[bi] = p.getPartitionSize(pIndices[bi]);
    }
	}
	
	public final String getTitle()
	{
		return title;
	}
	
	public final void setTitle(String title)
	{
		this.title = title;
	}
	
	public int getPrecision()
	{
		return precision;
	}
	
	public void setPrecision(int precision)
	{
		if(precision < 1)
		{
			this.precision = 1;
		}
		else if(precision > ExtendedMath.MAX_PRECISION)
		{
			this.precision = ExtendedMath.MAX_PRECISION;
		}
		else
		{
			this.precision = precision;
		}
	}
	
	public double getOptimalPrecision(int popSize)
	{
		// fail-safe
		if(popSize < 1)
		{
			return 0.0; // i mean...no bits is still the right answer...
		}

		// missing 'negligible constant' c_d?
		// also, i'm adjusting this such that it takes at least one bit
		// to represent a parameter (unless, as above, there are no observations)
		return Math.max(ExtendedMath.lg(popSize * 1.0) / 2.0, 1.0);	
		
		// so, it otherwise seems odd to me that the optimal precision could
		// be less than one.  how can it be that you need less than one bit
		// to optimally encode a parameter based on 1-3 observations?  this
		// interpretation at least seems minimally consistent with Stine...
		// return 1.0 + ExtendedMath.lg(popSize * 1.0) / 2.0;
	}
	
	public static double realTuncate(double val, double precision)
	{
		return val;
	}
	
	public double getConfidence()
	{
		return confidence;
	}
	
	public void setConfidence(double confidence)
	{
		if(confidence < 0.001) // minimum confidence?
		{
			this.confidence = 0.001;
		}
		else if(confidence > 0.999) // maximum confidence?
		{
			this.confidence = 0.999;
		}
		else
		{
			this.confidence = confidence;
		}
	}
	
	public final String[] getMatrixNames()
	{
		clearMatrices();
		populateMatrices();
		String[] nRet = new String[names.size()];
		for(int ni=0; ni<nRet.length; ni++)
		{
			nRet[ni] = (String)names.elementAt(ni);
		}
		return nRet;
	}
	
	private int getNameIndex(String name)
	{
		for(int ni=0; ni<names.size(); ni++)
		{
			if(names.elementAt(ni).equals(name))
			{
				return ni;
			}
		}
		return -1;
	}

	protected abstract void populateMatrices();
	
	public final Matrix getMatrix(String name, int rIdx)
	{
		Matrix[] m = (Matrix[])matrices.elementAt(getNameIndex(name));
		if(m == null)
		{
			// populate matrices and try again...
			populateMatrices();
			m = (Matrix[])matrices.elementAt(getNameIndex(name));
		}
		return m[rIdx];
	}
	
	public final LabelModel getRowLabelModel(String name)
	{
		return (LabelModel)rowLabels.elementAt(getNameIndex(name));
	}
	
	public final LabelModel getColumnLabelModel(String name)
	{
		return (LabelModel)colLabels.elementAt(getNameIndex(name));
	}
	
	protected final synchronized void clearMatrices()
	{
		names.setSize(0);
		matrices.setSize(0);
		rowLabels.setSize(0);
		colLabels.setSize(0);
	}
	
	protected final synchronized void addMatrices(String name,
		Matrix[] ms)
	{
		// fake label models
		PrefixLabelModel rLabels = new PrefixLabelModel("r", ms[0].getRows());
		PrefixLabelModel cLabels = new PrefixLabelModel("c", ms[0].getColumns());
		names.addElement(name);
		matrices.addElement(ms);
		rowLabels.addElement(rLabels);
		colLabels.addElement(cLabels);
	}
	
	protected final synchronized void addMatrices(String name, 
	  Matrix[] ms, LabelModel rls, LabelModel cls)
	{
		// add a matrix for each data element
		names.addElement(name);
		matrices.addElement(ms);
		rowLabels.addElement(rls);
		colLabels.addElement(cls);
	}
	
	protected final synchronized void addMatrices(String name, 
	  double[][][] data, LabelModel rls, LabelModel cls)
	{
		// add a matrix for each data element
		Matrix[] ms = new Matrix[data.length];
		for(int mi=0; mi<ms.length; mi++)
		{
			ms[mi] = new Matrix(data[mi]);
		}
		names.addElement(name);
		matrices.addElement(ms);
		rowLabels.addElement(rls);
		colLabels.addElement(cls);
	}
	
	protected final synchronized void addMatrices(String name,
		int[][][] data, LabelModel rls, LabelModel cls)
	{
		// copy ints to doubles
		double d[][][] = new double[data.length][][];
		for(int r=0; r<d.length; r++)
		{
			d[r] = new double[data[r].length][];
			for(int i=0; i<d[r].length; i++)
			{
				d[r][i] = new double[data[r][i].length];
				for(int j=0; j<d[r][i].length; j++)
				{
					d[r][i][j] = (double)data[r][i][j];
				}
			}
		}
		
		addMatrices(name, d, rls, cls);
	}
	
	protected final LabelModel getActorLabels()
	{
    String[] actorNames = new String[nData.getSize()];
    for(int ai=0; ai<actorNames.length; ai++)
    {
      actorNames[ai] = ((VisualNetworkData)nData).getLabel(ai);
    }
    return new DefaultLabelModel(actorNames);
	}
	
	protected final LabelModel getPartitionLabels()
	{
    String[] blockNames = new String[pIndices.length];
    for(int pi=0; pi<pIndices.length; pi++)
    {
    	blockNames[pi] = p.getPartitionName(pIndices[pi]);
    }
    return new DefaultLabelModel(blockNames);
	}

	public final String[] getReportNames()
	{
		clearReports();
		populateReports();

		String[] reportNames = new String[vReportNames.size()];
		for(int i=0; i<reportNames.length; i++)
		{
			reportNames[i] = (String)vReportNames.elementAt(i);
		}
		return reportNames;
	}
	
	public final String getReportHTML(int idx, int rIdx)
	{
		String[] reportHTML = (String[])vReportHTML.elementAt(idx);
		return reportHTML[rIdx];
	}
	
	protected final synchronized void addReports(String name, String[] html)
	{
		vReportNames.addElement(name);
		vReportHTML.addElement(html);
	}
	
	protected final synchronized void clearReports()
	{
		vReportNames.removeAllElements();
		vReportHTML.removeAllElements();
	}

	protected void populateReports()
	{
		// just assume there are none for now...not all blockmodels
		// should have to come with reports, right?
	}

	public NetworkData getNetwork()
	{
		return nData;
	}
	
	public com.wibinet.math.Partition getPartition()
	{
		return p;
	}

	public void setPartition(Partition partition)
	{
		this.p = partition;
		resetBlockSizes();
	}
	
	public double getGSquared()
	{
		return Double.NaN; // default...bad!
	}
	
	public double getGSquaredExact()
	{
		return Double.NaN; // default...bad!
	}
	
	public double getLgDataProbabilityExact()
	{
		return Double.NaN; // default...bad!
	}
	
	public double getPredictedTieStrengthExact(int rIdx, int i, int j)
	{
		return Double.NaN; // default...bad!
	}
}