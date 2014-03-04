/**
 * BlockmodelDelegate
 *
 * (c) 2000 Wibi Internet
 */

package com.wibinet.networks;

import com.wibinet.gui.*;
import com.wibinet.math.*;
import com.wibinet.math.Partition;

public class BlockmodelDelegate implements Blockmodel
{
	protected Blockmodel model;
	
	public BlockmodelDelegate(Blockmodel model)
	{
		this.model = model;
	}
	
	public Task getComputationTask()
	{
		return model.getComputationTask();
	}
	
	public void compute(AbstractTask task)
	{
		model.compute(task);
	}
	
	public NetworkData getNetwork()
	{
		return model.getNetwork();
	}
	
	public Partition getPartition()
	{
		return model.getPartition();
	}
	
	public void setPartition(com.wibinet.math.Partition p)
	{
		model.setPartition(p);
	}
	
	public double getLgModelProbability()
	{
		return model.getLgModelProbability();
	}
	
	public double getLgDataProbability()
	{
		return model.getLgDataProbability();
	}
	
  public String getFactoryClass()
  {
    return model.getFactoryClass();
  }
  
	public double getGSquared()
	{
		return model.getGSquared();
	}
	
	public double getGSquaredExact()
	{
		return model.getGSquaredExact();
	}
	
	public double getLgDataProbabilityExact()
	{
		return model.getLgDataProbabilityExact();
	}
	
	public double getPredictedTieStrength(int rIdx, int i, int j)
	{
		return model.getPredictedTieStrength(rIdx, i, j);
	}
	
	public double getPredictedTieStrengthExact(int rIdx, int i, int j)
	{
		return model.getPredictedTieStrengthExact(rIdx, i, j);
	}
	
	public int getPrecision()
	{
		return model.getPrecision();
	}
	
	public void setPrecision(int precision)
	{
		model.setPrecision(precision);
	}

	public double getConfidence()
	{
		return model.getConfidence();
	}
	
	public void setConfidence(double confidence)
	{
		model.setConfidence(confidence);
	}

	public String getTitle()
	{
		return model.getTitle();
	}
	
	public void setTitle(String title)
	{
		model.setTitle(title);
	}
	
	public String[] getMatrixNames()
	{
		return model.getMatrixNames();
	}
	
	public Matrix getMatrix(String name, int rIdx)
	{
		return model.getMatrix(name, rIdx);
	}
	
	public LabelModel getRowLabelModel(String name)
	{
		return model.getRowLabelModel(name);
	}
	
	public LabelModel getColumnLabelModel(String name)
	{
		return model.getColumnLabelModel(name);
	}
	
	public String[] getReportNames()
	{
		return model.getReportNames();
	}
	
	public String getReportHTML(int idx, int rIdx)
	{
		return model.getReportHTML(idx, rIdx);
	}
}