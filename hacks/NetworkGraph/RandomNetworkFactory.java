/**
 * RandomNetworkFactory.java
 *
 * Copyright (c) 2004 Wibi Internet.
 * All rights reserved.
 */
 
package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import com.wibinet.app.*;
import com.wibinet.gui.*;

public interface RandomNetworkFactory
{
  // tricky...
  public final static RandomNetworkFactory[] Factories = (RandomNetworkFactory[])
      Application.getPlugIns("com.wibinet.networks.RandomNetworkFactory");

  public VisualNetworkData generateNetwork();
  public boolean edit(JFrame parent);
	public Hashtable getProperties();
	public void setProperties(Hashtable props);
  public String getName();
  public String getGroup();
	
	public static class NetworkSizeDialog extends JDialog implements ActionListener
	{
		protected WholeNumberField wnfSize;
		protected int size;
		
		public NetworkSizeDialog(JFrame parent, int size)
		{
			super(parent, "Choose Network Size", true);
			this.size = size;

			getContentPane().setLayout(new BorderLayout());
			JLabel lSize = new JLabel("# of actors in network: ");
			getContentPane().add(BorderLayout.WEST, lSize);
			this.wnfSize = new WholeNumberField(size, 4);
			getContentPane().add(BorderLayout.CENTER, wnfSize);
			JOkCancelPanel ocp = new JOkCancelPanel();
			ocp.addActionListener(this);
			getContentPane().add(BorderLayout.SOUTH, ocp);
			pack();
		}
		
		public void actionPerformed(ActionEvent ae)
		{
			String cmd = ae.getActionCommand();
			if(JOkCancelPanel.OK.equals(cmd))
			{
				size = wnfSize.getValue();
			}
			else
			{
				size = -1;
			}
			setVisible(false);
		}
		
		public int getNetworkSize()
		{
			return size;
		}
	}
}