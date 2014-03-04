/**
 * TiesExporter.java
 *
 * Copyright (c) 2002 Wibi Internet.
 * All rights reserved.
 */
 
package com.wibinet.networks;

import java.io.*;

public interface TiesExporter
{
	public void exportTies(VisualNetworkData nData, OutputStream os);
	public String getResourceName();
	public boolean hasEditableParameters();
	public void editParameters();
	public String getFileExtension();
}