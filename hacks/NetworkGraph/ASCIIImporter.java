/**
 * ASCIIImporter.java
 *
 * (c) 2000 Wibi Internet
 */
 
package com.wibinet.networks;

import java.io.*;
import java.util.*;

public class ASCIIImporter implements NetworkDataImporter
{
	public ASCIIImporter()
	{
	}
	
	public String getName()
	{
		return "ASCII";
	}
	
	public boolean acceptsFile(File f)
	{
		String fileName = f.getName();
		return(fileName.toLowerCase().endsWith(".asc"));
	}
	
	public int getType()
	{
		return SINGLE_RELATION;
	}
	
	public VisualNetworkData readData(InputStream is) throws IOException
	{
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader in = new BufferedReader(isr);
		
		// read first line of data, count number of positions
		String line = in.readLine();
		StringTokenizer st = new StringTokenizer(line);
		int nSize = st.countTokens();
		
		double[][] data = new double[nSize][];
		for(int i=0; i<nSize; i++)
		{
			data[i] = new double[nSize];
			for(int j=0; j<nSize; j++)
			{
				data[i][j] = Double.valueOf(st.nextToken()).doubleValue();
			}
			if(i != nSize-1)
			{
				line = in.readLine();
				st = new StringTokenizer(line);
			}
		}
		
		NetworkData nData = new NetworkData(data, true);
		return new VisualNetworkData(nData);
	}
}