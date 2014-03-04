/**
 * ExportTiesAction.java
 *
 * Copyright (c) 2002 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import com.wibinet.app.*;

public class ExportTiesAction extends ResourceAction
{
	protected TiesExporter exporter;
	protected JFrame frame;
	protected VisualNetworkData nData;

  protected static TiesExporter[] exporters;
	static
	{
  	exporters = 
  		(TiesExporter[])Application.getPlugIns("com.wibinet.networks.TiesExporter");
	}
	
	public ExportTiesAction(TiesExporter exporter, JFrame frame,
		VisualNetworkData nData)
	{
		super(exporter.getResourceName());
		this.exporter = exporter;	
		this.frame = frame;
		this.nData = nData;
	}
	
	public void actionPerformed(ActionEvent ae)
	{
  	// put up file chooser
    JFileChooser jfc;
    File currentDir = Application.getWorkingDirectory();
    if(currentDir == null)
    {
      jfc = new JFileChooser();
    }
    else
    {
      jfc = new JFileChooser(currentDir);
    }
    
    File jfcDir = jfc.getCurrentDirectory();
    String fileName = nData.getName() + exporter.getFileExtension();
    File defaultFile = new File(jfcDir, fileName);
    jfc.setSelectedFile(defaultFile);
    
    int res = jfc.showSaveDialog(frame);
    if(res == JFileChooser.CANCEL_OPTION)
    {
    	return;
    }
    currentDir = jfc.getCurrentDirectory();
    Application.setWorkingDirectory(currentDir);
    
    if(exporter.hasEditableParameters())
    {
    	exporter.editParameters();
    }
    
		// export data
    try
    {
      File outputFile = jfc.getSelectedFile();
      FileOutputStream fos = new FileOutputStream(outputFile);
      exporter.exportTies(nData, fos);
      fos.close();
    }
    catch(Exception e)
    {
      e.printStackTrace();
      JOptionPane.showMessageDialog(frame,
        e, "Error exporting table", JOptionPane.ERROR_MESSAGE);
    }
	}
	
	public static Action[] getExportTiesActions(JFrame parent, VisualNetworkData nData)
	{
		Action[] actions = new Action[exporters.length];
    for(int ai=0; ai<actions.length; ai++)
    {
    	actions[ai] = new ExportTiesAction(exporters[ai], parent, nData);
    }
    return actions;
	}
}
