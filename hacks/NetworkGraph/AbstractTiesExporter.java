/**
 * AbstractTiesExporter.java
 *
 * Copyright (c) 2002 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.awt.*;
import java.io.*;
import java.text.*;

import com.wibinet.gui.*;

public abstract class AbstractTiesExporter extends Object implements TiesExporter
{
	protected NumberFormat fmt;
	protected boolean xportDyads;
	
	protected static String[] labels = 
	{
		"i", "j", "label_i", "label_j"
	};
	
	public AbstractTiesExporter()
	{
		fmt = NumberFormat.getInstance();
		fmt.setMaximumFractionDigits(5);
		fmt.setGroupingUsed(false);
		this.xportDyads = true; // option!
	}
	
	public void exportTies(VisualNetworkData nData, OutputStream os)
	{
		OutputStreamWriter osw = new OutputStreamWriter(os);
		BufferedWriter bw = new BufferedWriter(osw);
		PrintWriter out = new PrintWriter(bw);
		
		// get # of actors & relation count
		int nSize = nData.getSize();
		int relCt = nData.getRelationCount();
		
		// print out data labels
		out.print(getRowHeader());
		for(int i=0; i<labels.length; i++)
		{
			out.print(getDataHeader(i==0));
			out.print(getFormattedString(labels[i]));
			out.print(getDataTrailer(i==0));
		}
		for(int ri=0; ri<relCt; ri++)
		{
			String relName = nData.getRelation(ri).getName();
			out.print(getDataHeader(false));
			if(xportDyads)
			{
				out.print(relName + "_out");
				out.print(getDataTrailer(false));
				out.print(getDataHeader(false));
				out.print(relName + "_in");
			}
			else
			{
				out.print(relName);
			}
			out.print(getDataTrailer(false));
		}
		out.print(getRowTrailer());
		
		// print data
		for(int i=0; i<nSize; i++)
		{
			if(xportDyads)
			{
				for(int j=i+1; j<nSize; j++)
				{
					// print ids
					out.print(getDataHeader(true));
					out.print(i);
					out.print(getDataTrailer(true));
					out.print(getDataHeader(false));
					out.print(j);
					out.print(getDataTrailer(false));
					out.print(getDataHeader(false));
					out.print(getFormattedString(nData.getLabel(i)));
					out.print(getDataTrailer(false));
					out.print(getDataHeader(false));
					out.print(getFormattedString(nData.getLabel(j)));
					out.print(getDataTrailer(false));
					
					// print data
					for(int ri=0; ri<relCt; ri++)
					{
						out.print(getDataHeader(false));
						out.print(getFormattedData(new Double(nData.getTieStrength(ri, i , j))));
						out.print(getDataTrailer(false));
						out.print(getDataHeader(false));
						out.print(getFormattedData(new Double(nData.getTieStrength(ri, j , i))));
						out.print(getDataTrailer(false));
					}
					out.print(getRowTrailer());
				}
			}
			else
			{
				for(int j=0; j<nSize; j++)
				{
					// print ids
					out.print(getDataHeader(true));
					out.print(i);
					out.print(getDataTrailer(true));
					out.print(getDataHeader(false));
					out.print(j);
					out.print(getDataTrailer(false));
					out.print(getDataHeader(false));
					out.print(getFormattedString(nData.getLabel(i)));
					out.print(getDataTrailer(false));
					out.print(getDataHeader(false));
					out.print(getFormattedString(nData.getLabel(j)));
					out.print(getDataTrailer(false));
					
					// print data
					for(int ri=0; ri<relCt; ri++)
					{
						out.print(getDataHeader(false));
						out.print(getFormattedData(new Double(nData.getTieStrength(ri, i , j))));
						out.print(getDataTrailer(false));
					}
					out.print(getRowTrailer());
				}
			}
		}

		out.flush();
	}
	
	public abstract String getDataHeader(boolean first);
	public abstract String getDataTrailer(boolean first);
	public abstract String getRowHeader();
	public abstract String getRowTrailer();

	public String getFormattedData(Object o)
	{
		if(o instanceof Number)
		{
			return fmt.format((Number)o);
		}
		else
		{
			return o.toString();
		}
	}
	
	public String getFormattedString(String s)
	{
		return s;
	}
}