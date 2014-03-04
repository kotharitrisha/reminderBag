/**
 * TimeSeriesNodeData.java
 *
 * This should almost surely be an interface, but
 * for now we'll "hardcode" it.
 *
 * Copyright (c) 2002 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.io.*;
import com.wibinet.util.XMLReader;

public class TimeSeriesNodeData extends Object implements DataChangeListener
{
	protected NetworkData nData;
	protected String name;
	protected double[][] values;
	
	public TimeSeriesNodeData(NetworkData nData, String name, int seriesLength)
	{
		this.nData = nData;
		this.name = name;
		this.values = new double[seriesLength][];

		int nSize = nData.getSize();
		for(int i=0; i<values.length; i++)
		{
			this.values[i] = new double[nSize];
			for(int j=0; j<nSize; j++)
			{
				this.values[i][j] = 0.0;
			}
		}
		
		// we should probably be a listener to nData
		nData.addDataChangeListener(this);
	}
	
	public void dataChanged(DataChangeEvent dce)
	{
		int node = dce.getRow();
		int nSize = values[0].length; // just to be sure...
		if(dce.getType() == DataChangeEvent.NODE_INSERTED)
		{
			// shift up...
			for(int t=0; t<values.length; t++)
			{
				double[] newVals = new double[nSize+1];
				System.arraycopy(values[t], 0, newVals, 0, node);
				newVals[node] = 0.0;
				System.arraycopy(values[t], node, newVals, node+1, nSize-node);
				values[t] = newVals;
			}
		}
		else if(dce.getType() == DataChangeEvent.NODE_DELETED)
		{
			// shift down...
			for(int t=0; t<values.length; t++)
			{
				double[] newVals = new double[nSize-1];
				System.arraycopy(values[t], 0, newVals, 0, node);
				System.arraycopy(values[t], node, newVals, node, nSize-node-1);
				values[t] = newVals;
			}
		}
	}
	
	public double getValue(int actor, int time)
	{
		return values[time][actor];
	}
	
	public void setValue(int actor, int time, double value)
	{
		values[time][actor] = value;
	}
	
	public int getSeriesLength()
	{
		return values.length;
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
		return name;
	}
	
	public void writeXML(PrintWriter out)
	{
		out.println("<timeseries name=\"" + XMLReader.xmlEncode(name) + 
			"\" length=\""+values.length+"\">");
		int nSize = nData.getSize();
		for(int t=0; t<values.length; t++)
		{
			out.println("  <nodevalues time=\""+t+"\">");
			for(int ai=0; ai<nSize; ai++)
			{
				out.println("    <nodevalue node=\""+ai+"\" value=\""+values[t][ai]+"\"/>");
			}
			out.println("  </nodevalues>");
		}
		out.println("</timeseries>");
	}
}