/**
 * PartitionLabelFactory.java
 *
 * (c) 2001 Wibi Internet
 */

package com.wibinet.networks;

import javax.swing.*;
import com.wibinet.math.Partition;

public class PartitionLabelFactory extends Object implements NodeEvaluatorFactory
{
  public NodeEvaluator newInstance()
  {
    return new PartitionLabelEvaluator();
  }
  
  public void edit(NodeEvaluator evaluator, JFrame parent)
  {
  }
  
  public Class getEvaluatorClass()
  {
    return String.class;
  }
  
  public String getName()
  {
    return "Partition Label";
  }

  public String getGroup()
  {
    return "Classifier";
  }
  
	public boolean isMultiple()
	{
		return false;
	}
	
	public class PartitionLabelEvaluator extends Object implements NodeEvaluator
	{
		protected NetworkData nd;
		protected String name;
    
		public PartitionLabelEvaluator()
		{
			this.nd = null;
			this.name = "Partition Label";
		}
    
		public void runEvaluator()
		{
		}
    
		public void setNetwork(NetworkData nd)
		{
			this.nd = nd;
		}
    
		public Object evaluateNode(int idx, int ri)
		{
			return evaluateNode(idx);
		}
	  
		public Object evaluateNode(int idx)
		{
			Partition p = nd.getPartition();
			int pIdx = p.getPartition(idx);
			return p.getPartitionName(pIdx);
		}
    
		public String getName()
		{
			return name;
		}
    
		public NodeEvaluatorFactory getFactory()
		{
			return PartitionLabelFactory.this;
		}
	}
}