/**
 * NMTriadicFactory.java
 *
 * Copyright (c) 2003 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.awt.*;
import javax.swing.*;

public class NMTriadicFactory implements NetworkMeasureFactory
{
	protected final static int TYPE_TRANSITIVITY = 0;
	protected final static int TYPE_INTRANSITIVITY = 1;
	protected final static int TYPE_CYCLICITY = 2;
	protected final static int TYPE_2_INSTARS = 3;
	protected final static int TYPE_2_OUTSTARS = 4;
	protected final static int TYPE_2_MIXEDSTARS = 5;
	protected final static String[] typeStrings =
	{
		"Transitivity", "Intransitivity", "Cyclicity",
		"2-In-stars", "2-Out-stars", "2-Mixed-stars"
	};
	protected final static String[] keyStrings =
	{
		"Transitivity", "Intransitivity", "Cyclicity",
		"2Instars", "2Outstars", "2Mixedstars"
	};

	public boolean isMultiple()
	{
		return false;
	}
	
  public NetworkMeasure newInstance()
	{
		return new TriadicMeasure(TYPE_TRANSITIVITY);
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
			editor = new MeasureEditor((Dialog)parent, measure, "Triadic Measure Options");
		}
		else if(parent instanceof Frame)
		{
			editor = new MeasureEditor((Frame)parent, measure, "Triadic Measure Options");
		}
		else
		{
			editor = new MeasureEditor(measure, "Triadic Measure Options");
		}
		editor.setVisible(true);
		return;
	}

	public String getName()
	{
		return "Triadic";
	}

	public String toString()
	{
		return getName();
	}

	public String getGroup()
	{
		return null;
	}
	
	public class TriadicMeasure implements NetworkMeasure, TypedNetworkMeasure
	{
		protected int type;
		protected String name;
		
		public TriadicMeasure(int type)
		{
			this.type = type;
			try
			{
				this.name = typeStrings[type];
			}
			catch(ArrayIndexOutOfBoundsException aioobe)
			{
				this.name = "Unknown (Error?)";
			}
		}

		public double getStatistic(NetworkData nd)
		{
			double stat = 0.0;
			int nSize = nd.getSize();
			int relCt = nd.getRelationCount();
			for(int ri=0; ri<relCt; ri++)
			{
				Relation r = nd.getRelation(ri);
				
				for(int i=0; i<nSize; i++)
				{
					for(int j=0; j<nSize; j++)
					{
						for(int k=0; k<nSize; k++)
						{
							if((i != j) && (i != k) && (j != k))
							{
								switch(type)
								{
									case TYPE_TRANSITIVITY:
										stat +=
											r.getTieStrength(i, j) *
											r.getTieStrength(j, k) *
											r.getTieStrength(i, k);
										break;
									
									case TYPE_INTRANSITIVITY:
										stat +=
											r.getTieStrength(i, j) *
											r.getTieStrength(j, k) *
											(1.0 - r.getTieStrength(i, k));
										break;
									
									case TYPE_CYCLICITY:
										stat +=
											r.getTieStrength(i, j) *
											r.getTieStrength(j, k) *
											r.getTieStrength(k, i);
										break;
									
									case TYPE_2_INSTARS:
										stat +=
											r.getTieStrength(i, j) *
											r.getTieStrength(k, j);
										break;
									
									case TYPE_2_OUTSTARS:
										stat +=
											r.getTieStrength(i, j) *
											r.getTieStrength(i, k);
										break;
									
									case TYPE_2_MIXEDSTARS:
										stat +=
											r.getTieStrength(i, j) *
											r.getTieStrength(j, k);
										break;
								}
							}
						}
					}
				}
			}
			return stat;
		}
		
		public double getInitialEstimate(NetworkData nd)
		{
			return 0.0;
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
		
		public String toString()
		{
			return getName();
		}
	
		public void setName(String name)
		{
			this.name = name;
		}
			
		public NetworkMeasureFactory getFactory()
		{
			return NMTriadicFactory.this;
		}
		
		public String[] getTypeStrings()
		{
			return typeStrings;
		}
		
		public String[] getKeyStrings()
		{
			return keyStrings;
		}
	}
}