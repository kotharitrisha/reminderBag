/**
 * NMIndividualFactory.java
 *
 * Copyright (c) 2003 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.awt.*;
import javax.swing.*;

public class NMIndividualFactory implements NetworkMeasureFactory
{
	protected final static int TYPE_EXPANSIVENESS = 0;
	protected final static int TYPE_ATTRACTIVENESS = 1;
	protected final static String[] typeStrings = {"Expansiveness", "Attractiveness"};

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
		int nSize = nd.getSize();
		NetworkMeasure[] instances = new NetworkMeasure[nSize];
		MeasureParams params = new MeasureParams(TYPE_EXPANSIVENESS);
		for(int i=0; i<nSize; i++)
		{
			instances[i] = new IndividualMeasure(params, i);
		}
		return instances;
	}

  public void edit(NetworkMeasure measure, Window parent)
	{
		MeasureEditor editor = null;
		if(parent instanceof Dialog)
		{
			editor = new MeasureEditor((Dialog)parent, measure, "Individual Measure Options");
		}
		else if(parent instanceof Frame)
		{
			editor = new MeasureEditor((Frame)parent, measure, "Individual Measure Options");
		}
		else
		{
			editor = new MeasureEditor(measure, "Individual Measure Options");
		}
		editor.setVisible(true);
		return;
	}

	public String getName()
	{
		return "Individual";
	}

	public String toString()
	{
		return getName();
	}

	public String getGroup()
	{
		return null;
	}
	
	// by encapsualting this as an object, we can have all
	// measures refer to the same set of parameters
	public class MeasureParams extends Object
	{
		protected int type;
		protected String name;
		
		public MeasureParams(int type)
		{
			this.type = type;
			this.name = typeStrings[type];
		}
	}
	
	public class IndividualMeasure implements NetworkMeasure, TypedNetworkMeasure
	{
		protected MeasureParams params;
		protected int actorIdx;
		
		public IndividualMeasure(MeasureParams params, int actorIdx)
		{
			this.params = params;
			this.actorIdx = actorIdx;
		}

		public double getStatistic(NetworkData nd)
		{
			double sum = 0.0;
			int relCt = nd.getRelationCount();
			for(int ri=0; ri<relCt; ri++)
			{
				switch(params.type)
				{
					case TYPE_EXPANSIVENESS:
						sum += nd.getOutDegreeCentrality(ri, actorIdx);
						break;
	
					case TYPE_ATTRACTIVENESS:
						sum += nd.getInDegreeCentrality(ri, actorIdx);
						break;
					
					default:
						sum = Double.NaN;
				}
			}
			return sum;
		}
		
		public double getInitialEstimate(NetworkData nd)
		{
			return 0.0;
		}
		
		public int getType()
		{
			return params.type;
		}
		
		public void setType(int type)
		{
			params.type = type;
		}
		
		public String getName()
		{
			return params.name;
		}
		
		public String toString()
		{
			return params.name + "(" + actorIdx + ")";
		}
	
		public void setName(String name)
		{
			params.name = name;
		}
	
		public NetworkMeasureFactory getFactory()
		{
			return NMIndividualFactory.this;
		}
		
		public String[] getTypeStrings()
		{
			return typeStrings;
		}
		
		public String[] getKeyStrings()
		{
			return typeStrings;
		}
	}
}