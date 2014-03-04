/**
 * NegopyTiesExporter.java
 *
 * Copyright (c) 2006 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.io.*;
import java.text.*;

public class NegopyTiesExporter extends Object implements TiesExporter
{
	protected static NumberFormat fmt;
	static
	{
		fmt = NumberFormat.getInstance();
		fmt.setMaximumFractionDigits(5);
		fmt.setGroupingUsed(false);
	}

	public void exportTies(VisualNetworkData nData, OutputStream os)
	{
		OutputStreamWriter osw = new OutputStreamWriter(os);
		BufferedWriter bw = new BufferedWriter(osw);
		PrintWriter out = new PrintWriter(bw);
		
		// get # of actors & relation count
		int nSize = nData.getSize();
		int relCt = nData.getRelationCount();
		
		// only print the first relation?  throw an error otherwise?
		if(relCt > 1)
		{
			System.err.println("NOTE: Only exporting first relation");
		}
		
		for(int i=0; i<nSize; i++)
		{
			for(int j=0; j<nSize; j++)
			{
				double ts = nData.getTieStrength(i, j);
				if(ts != 0)
				{
					out.println(""+(i+1)+" "+(j+1)+" "+fmt.format(ts));
				}
			}
		}
		out.flush();
	}

	public String getResourceName()
	{
		return "NetworkActions.TiesExporter.NegopyTiesExporter";
	}
	
	public String getFileExtension()
	{
		return ".neg";
	}

	public boolean hasEditableParameters()
	{
		return false;
	}
	
	public void editParameters()
	{
	}
}
