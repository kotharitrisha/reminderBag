/**
 * CSVTiesExporter.java
 *
 * Copyright (c) 2002 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

public class CSVTiesExporter extends AbstractTiesExporter
{
	public boolean hasEditableParameters()
	{
		return false;
	}
	
	public void editParameters()
	{
	}
	
	public String getRowHeader()
	{
		return "";
	}
	
	public String getRowTrailer()
	{
		return "\n";
	}
	
	public String getDataHeader(boolean first)
	{
		return (first) ? "" : ", ";
	}
	
	public String getDataTrailer(boolean first)
	{
		return "";
	}
	
	public String getResourceName()
	{
		return "NetworkActions.TiesExporter.CSVTiesExporter";
	}
	
	public String getFileExtension()
	{
		return ".csv";
	}
}