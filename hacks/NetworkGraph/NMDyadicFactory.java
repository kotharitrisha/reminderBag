/**
 * NMDyadicFactory.java
 *
 * Copyright (c) 2003 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import com.wibinet.app.*;
import com.wibinet.gui.*;

public class NMDyadicFactory implements NetworkMeasureFactory
{
	protected final static int TYPE_CHOICE = 0;
	protected final static int TYPE_MUTUALITY = 1;
	protected final static String[] typeStrings =
		{"Choice", "Mutuality"};

	public boolean isMultiple()
	{
		return false;
	}
	
	public NetworkMeasure newInstance()
	{
		return new DyadicMeasure(TYPE_CHOICE);
	}
	
	public NetworkMeasure[] newInstances(NetworkData nd)
	{
		NetworkMeasure[] instances = new NetworkMeasure[1];
		instances[0] = newInstance();
		return instances;
	}

	public void edit(NetworkMeasure measure, Window parent)
	{
		MeasureEditor editor = null;
		if(parent instanceof Dialog)
		{
			editor = new MeasureEditor((Dialog)parent, measure, "Dyadic Measure Options");
		}
		else if(parent instanceof Frame)
		{
			editor = new MeasureEditor((Frame)parent, measure, "Dyadic Measure Options");
		}
		else
		{
			editor = new MeasureEditor(measure, "Dyadic Measure Options");
		}
		editor.setVisible(true);
		return;
	}

	public String getName()
	{
		return "Dyadic";
	}
	
	public String toString()
	{
		return getName();
	}

	public String getGroup()
	{
		return null;
	}
	
	public class DyadicMeasure implements NetworkMeasure, TypedNetworkMeasure
	{
		protected int type;
		protected String name;
		
		public DyadicMeasure(int type)
		{
			this.type = type;
			try
			{
				name = typeStrings[type];
			}
			catch(ArrayIndexOutOfBoundsException aioobe)
			{
				name = "Unknown (error?)";
			}
		}

		public double getStatistic(NetworkData nd)
		{
			int relCt = nd.getRelationCount();
			double sum = 0.0;
			for(int ri=0; ri<relCt; ri++)
			{
				switch(type)
				{
					case TYPE_CHOICE:
						sum += nd.getMass(ri);
						break;
					
					case TYPE_MUTUALITY:
						sum += nd.getMutuals(ri);
						break;
										
					default:
						sum = Double.NaN;
				}
			}
			return sum;
		}
		
		public double getInitialEstimate(NetworkData nd)
		{
			int relCt = nd.getRelationCount();
			int size = nd.getSize();
			double numSum = 0.0;
			double denomSum = 0.0;
			for(int ri=0; ri<relCt; ri++)
			{
				switch(type)
				{
					case TYPE_CHOICE:
						double ties = nd.getMass(ri);
						double nonTies = size * (size-1) - ties;
						numSum += ties;
						denomSum += nonTies;
						break;
					
					case TYPE_MUTUALITY:
						double sym = nd.getMutuals(ri) + nd.getNulls(ri);
						double asym = (size * (size-1) / 2) - sym;
						numSum += sym;
						denomSum += asym;
						break;
										
					default:
						numSum = Double.NaN;
						denomSum = Double.NaN;
				}
			}
			return Math.log(numSum/denomSum);
		}
		
		public int getType()
		{
			return type;
		}
		
		public void setType(int type)
		{
			this.type = type;
		}
		
		public String getName()
		{
			return name;
		}
		
		public void setName(String name)
		{
			this.name = name;
		}
		
		public String toString()
		{
			return getName();
		}
	
		public NetworkMeasureFactory getFactory()
		{
			return NMDyadicFactory.this;
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