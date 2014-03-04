/**
 * Blockmodel.java
 *
 * (c) 2000 Wibi Internet
 */

package com.wibinet.networks;

import com.wibinet.gui.LabelModel;
import com.wibinet.gui.Task;
import com.wibinet.gui.AbstractTask;
import com.wibinet.math.Matrix;
import com.wibinet.math.Partition;

public interface Blockmodel
{
	public Task getComputationTask();
	public void compute(AbstractTask task);
	public NetworkData getNetwork();
	public com.wibinet.math.Partition getPartition();
	public void setPartition(Partition partition);
	public double getLgModelProbability();
	public double getLgDataProbability();
  
  public String getFactoryClass();
	
	// these should be in another interface
	public double getGSquared(); // shouldn't be here?
	public double getGSquaredExact();
	public double getLgDataProbabilityExact();
	public double getPredictedTieStrengthExact(int rIdx, int i, int j);
	
	public double getPredictedTieStrength(int rIdx, int i, int j);
	
	public int getPrecision();
	public void setPrecision(int precision);
	public double getConfidence();
	public void setConfidence(double confidence);

	// ui related stuff...
	public String getTitle();
	public void setTitle(String title);
	public String[] getMatrixNames();
	public Matrix getMatrix(String name, int rIdx);
	public LabelModel getRowLabelModel(String name);
	public LabelModel getColumnLabelModel(String name);
	public String[] getReportNames();
	public String getReportHTML(int idx, int rIdx);
}