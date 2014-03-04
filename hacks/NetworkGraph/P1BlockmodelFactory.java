/**
 * P1BlockmodelFactory.java
 *
 * (c) 2000-2002 Wibi Internet
 */

package com.wibinet.networks;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.wibinet.gui.*;
import com.wibinet.math.Partition;

public class P1BlockmodelFactory extends AbstractBlockmodelFactory
{
	protected Hashtable props;
	protected final static String TYPE = "Type";
  protected final static String TOLERANCE = "Tolerance";
	
	public P1BlockmodelFactory()
	{
		this.props = new Hashtable();
    
    // string values?
		props.put(TYPE, new Integer(P1Blockmodel.SATURATED));
    props.put(TOLERANCE, new Double(0.001));
	}
	
	public Blockmodel newInstance(NetworkData nData, Partition p)
	{
		int type = ((Integer)props.get(TYPE)).intValue();
    double tolerance = ((Double)props.get(TOLERANCE)).doubleValue();
		P1Blockmodel model = new P1Blockmodel(nData, p, type);
		return model;
	}
	
	public boolean edit(Blockmodel model)
	{
		EditDialog ed = new EditDialog();
		ed.setVisible(true);
		if(ed.isCancelled())
		{
			return false;
		}
		
		int type = ed.getType();
    double tolerance = ed.getTolerance();

		// actually set model to this type
		((P1Blockmodel)model).setType(type);
    
    // set tolerance
    ((P1Blockmodel)model).setTolerance(tolerance);
		
		// remember type for next time...
		props.put(TYPE, new Integer(type));
		props.put(TOLERANCE, new Double(tolerance));
		return true;
	}
	
	public Hashtable getProperties(Blockmodel model)
	{
		return props;
	}

	public void setProperties(Blockmodel model, Hashtable props)
	{
		int newType;
		double newTolerance;
		
		Object oType = props.get(TYPE);
		if(oType instanceof String)
		{
			newType = Integer.parseInt((String)oType);
		}
		else
		{
			newType = ((Integer)oType).intValue();
		}

		Object oTolerance = props.get(TOLERANCE);
		if(oTolerance instanceof String)
		{
			newTolerance = Double.parseDouble((String)oTolerance);
		}
		else
		{
			newTolerance = ((Double)oTolerance).doubleValue();
		}

		((P1Blockmodel)model).setType(newType);
		((P1Blockmodel)model).setTolerance(newTolerance);
		System.err.println("just set tolereance on " + model + " to " + newTolerance);
	}

	public String getType()
	{
		return "P1(Wang & Wong)";
	}
	
	protected class EditDialog extends JDialog implements ActionListener
	{
		protected JComboBox cbType;
    protected DoubleNumberField dfTolerance;
		protected int type;
    protected double tolerance;
		protected boolean cancelled;
		
		public EditDialog()
		{
			super((JFrame)null);
      setModal(true);
			this.cancelled = false;
			setTitle("P1 Blockmodel Options");
			getContentPane().setLayout(new BorderLayout());

      Box cPanel = new Box(BoxLayout.Y_AXIS);
      
			JPanel pType = new JPanel(new BorderLayout());
			type = ((Integer)props.get(TYPE)).intValue();
			String[] types = new String[3];
			types[P1Blockmodel.IN_GROUP] = "In-Group";
			types[P1Blockmodel.DIAGONAL] = "Diagonal";
			types[P1Blockmodel.SATURATED] = "Saturated";
			cbType = new JComboBox(types);
			cbType.setEditable(false);
			cbType.setSelectedIndex(type);
			pType.add(BorderLayout.WEST, new JLabel("Blockmodel type:"));
			pType.add(BorderLayout.CENTER, cbType);
      cPanel.add(pType);
      
      JPanel pTolerance = new JPanel(new BorderLayout());
      tolerance = ((Double)props.get(TOLERANCE)).doubleValue();
      dfTolerance = new DoubleNumberField(tolerance, 8);
      pTolerance.add(BorderLayout.WEST, new JLabel("Tolerance:"));
      pTolerance.add(BorderLayout.CENTER, dfTolerance);
      cPanel.add(pTolerance);

			getContentPane().add(BorderLayout.CENTER, cPanel);
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
				type = cbType.getSelectedIndex();
        tolerance = dfTolerance.getValue();
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
		
		public int getType()
		{
			return type;
		}
		
		public double getTolerance()
		{
			return tolerance;
		}
	}
}