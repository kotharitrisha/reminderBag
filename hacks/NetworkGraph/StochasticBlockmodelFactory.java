/**
 * StochasticBlockmodelFactory.java
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

public class StochasticBlockmodelFactory extends AbstractBlockmodelFactory
  implements BlockmodelFactory
{
	protected Hashtable props;
	// protected final static String TOLERANCE = "Tolerance";
	
	public StochasticBlockmodelFactory()
	{
		this.props = new Hashtable();
		// props.put(TOLERANCE, new Double("0.05"));
	}
	
	public Blockmodel newInstance(NetworkData nData, Partition p)
	{
		StochasticBlockmodel model = new StochasticBlockmodel(nData, p);
		// double tolerance = ((Double)props.get(TOLERANCE)).doubleValue();
		// model.setTolerance(tolerance);
		return model;
	}
	
	public DyadBlockmodel newDyadBlockmodelInstance(NetworkData nData, Partition p)
	{
		return (DyadBlockmodel)newInstance(nData, p);
	}
	
	public boolean edit(Blockmodel model)
	{
		// EditDialog dialog = new EditDialog();
		// dialog.setVisible(true);
		// props.put(TOLERANCE, new Double(dialog.getTolerance()));
		return true;
	}
	
	/*protected class EditDialog extends JDialog implements ActionListener
	{
		protected DoubleNumberField df;
		protected double tolerance;
		
		public EditDialog()
		{
			super(null, true);
			setTitle("Stochastic Blockmodel Options");
			getContentPane().setLayout(new BorderLayout());
			tolerance = ((Double)props.get(TOLERANCE)).doubleValue();
			df = new DoubleNumberField(tolerance, 5);
			JPanel p = new JPanel(new BorderLayout());
			p.add(BorderLayout.WEST, new JLabel("Iterative Scaler Tolerance:"));
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
				tolerance = df.getValue();
			}
			setVisible(false);
			dispose();
		}
		
		public double getTolerance()
		{
			return tolerance;
		}
	}*/
	
	public Hashtable getProperties(Blockmodel model)
	{
		return props;
	}

	public void setProperties(Blockmodel model, Hashtable props)
	{
	}

	public String getType()
	{
		return "Stochastic";
	}
}