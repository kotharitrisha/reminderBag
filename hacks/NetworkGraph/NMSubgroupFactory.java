/**
 * NMSubgroupFactory.java
 *
 * Copyright (c) 2003 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.awt.*;
import javax.swing.*;
import com.wibinet.math.Partition;

public class NMSubgroupFactory implements NetworkMeasureFactory
{
	public boolean isMultiple()
	{
		return true;
	}
	
  public NetworkMeasure newInstance()
	{
		// maybe throw?
		return null;
	}
	
	public NetworkMeasure[] newInstances(NetworkData nd)
	{
		// get blockmodel
		Partition p = nd.getPartition();
		int[] pIndices = p.getPartitionIndices();
		int pCt = pIndices.length;
		NetworkMeasure[] instances = new NetworkMeasure[pCt * pCt];
		for(int i=0; i<pCt; i++)
		{
			for(int j=0; j<pCt; j++)
			{
				instances[i + j*pCt] = new SubgroupMeasure(pIndices[i], pIndices[j]);
			}
		}
		return instances;
	}

  public void edit(NetworkMeasure measure, Window parent)
	{
		return;
	}

	public String getName()
	{
		return "Subgroup";
	}

	public String toString()
	{
		return getName();
	}

	public String getGroup()
	{
		return null;
	}
	
	public class SubgroupMeasure implements NetworkMeasure
	{
		protected int r;
		protected int s;
		protected String name;
		
		public SubgroupMeasure(int r, int s)
		{
			this.r = r;
			this.s = s;
			this.name = "Group ("+r+", "+s+")";
		}

		public double getStatistic(NetworkData nd)
		{
			double sum = 0.0;
			int relCt = nd.getRelationCount();
			for(int ri=0; ri<relCt; ri++)
			{
				sum += nd.getBlockMass(ri, r, s);
			}
			return sum;
		}
		
		public double getInitialEstimate(NetworkData nd)
		{
			return 0.0;
		}
		
		public String getName()
		{
			return name;
		}
		
		public String toString()
		{
			return getName();
		}
	
		public NetworkMeasureFactory getFactory()
		{
			return NMSubgroupFactory.this;
		}
	}
}