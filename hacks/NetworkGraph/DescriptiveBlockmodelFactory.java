/**
 * DescriptiveBlockmodelFactory.java
 *
 * (c) 2000 Wibi Internet
 */

package com.wibinet.networks;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.wibinet.gui.*;
import com.wibinet.math.Partition;

public class DescriptiveBlockmodelFactory extends AbstractBlockmodelFactory
{
	protected Hashtable props;
	protected final static String ALPHA = "ALPHA";
	
	public DescriptiveBlockmodelFactory()
	{
		this.props = new Hashtable();
		props.put(ALPHA, new Double(0.1)); // defaults to 0.1
	}
	
	public Blockmodel newInstance(NetworkData nData, Partition p)
	{
		double alpha = ((Double)props.get(ALPHA)).doubleValue();
		DescriptiveBlockmodel model = new DescriptiveBlockmodel(nData, p, alpha);
		return model;
	}
	
	public boolean edit(Blockmodel model)
	{
		EditDialog dialog = new EditDialog();
		dialog.setVisible(true);
		if(dialog.isCancelled())
		{
			return false;
		}
		props.put(ALPHA, new Double(dialog.getAlpha()));
		return true;
	}
	
	public Hashtable getProperties(Blockmodel model)
	{
		return props;
	}

	public void setProperties(Blockmodel model, Hashtable props)
	{
		double alpha = ((Double)props.get(ALPHA)).doubleValue();
		((DescriptiveBlockmodel)model).setAlpha(alpha);
	}

	public String getType()
	{
		return "Descriptive";
	}
	
	protected class EditDialog extends JDialog implements ActionListener
	{
		protected DoubleNumberField df;
		protected double alpha;
		protected boolean cancelled;
		
		public EditDialog()
		{
			super((JFrame)null, true);
			this.cancelled = false;
			setTitle("Descriptive Blockmodel Options");
			getContentPane().setLayout(new BorderLayout());
			alpha = ((Double)props.get(ALPHA)).doubleValue();
			df = new DoubleNumberField(alpha, 5);
			JPanel p = new JPanel(new BorderLayout());
			p.add(BorderLayout.WEST, new JLabel("Alpha-Level:"));
			p.add(BorderLayout.CENTER, df);
			getContentPane().add(BorderLayout.CENTER, p);
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
				alpha = df.getValue();
				cancelled = false;
			}
			else
			{
				cancelled = true;
			}
			setVisible(false);
			dispose();
		}
		
		public boolean isCancelled()
		{
			return cancelled;
		}
		
		public double getAlpha()
		{
			return alpha;
		}
	}
}