/**
 * NormalizeFactory.java
 *
 * Copyright (c) 2005 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.wibinet.gui.DoubleNumberField;
import com.wibinet.gui.JOkCancelPanel;
import com.wibinet.networks.DiscretizeFactory.Editor;
import com.wibinet.networks.DiscretizeFactory.Evaluator;

public class NormalizeFactory implements RelationEvaluatorFactory
{
	protected final static Double ZERO = new Double(0.0);
	
	public RelationEvaluator newInstance()
	{
	    return new Evaluator();
	}

	public void edit(RelationEvaluator evaluator, JFrame parent)
	{
	    Evaluator ev = (Evaluator)evaluator;
	    Editor editor = new Editor(ev, parent);
	    editor.setVisible(true);
	    
	    if(editor.isDataValid())
	    {
	      ev.name = editor.getName();
	    }
	}

	public Class getEvaluatorClass()
	{
	    return Double.class;
	}

	public String getName()
	{
	    return "Normalize";
	}

	public String getGroup()
	{
	    return "Filter";
	}

	public class Evaluator implements RelationEvaluator
	{
	    protected Relation r;
	    protected String name;
	    protected Double[][] values;
    
	    public Evaluator()
	    {
	      this.r = null;
	      this.name = "Normalize";
	      this.values = new Double[0][0];
	    }
	    
	    public void runEvaluator()
	    {
	    	int size = r.getNodeCount();
	    	values = new Double[size][];
	    	for(int i=0; i<size; i++)
	    	{
	    		values[i] = new Double[size];
	    		values[i][i] = ZERO;
	    	}
	    	for(int i=0; i<size; i++)
	    	{
	    		for(int j=i+1; j<size; j++)
	    		{
	    			// should we note errors if there are negative values?
	    			double x_ij = Math.abs(r.getTieStrength(i, j));
	    			double x_ji = Math.abs(r.getTieStrength(j, i));
	    			double max = Math.max(x_ij, x_ji);
	    			if(max == 0.0)
	    			{
	    				values[i][j] = ZERO;
	    				values[j][i] = ZERO;
	    			}
	    			else
	    			{
	    				values[i][j] = new Double(x_ij/max);
	    				values[j][i] = new Double(x_ji/max);
	    			}
	    		}
	    	}
	    }
	    
	    public void setRelation(Relation r)
	    {
	      this.r = r;
	    }
	    
	    public Object evaluateRelation(int fromIdx, int toIdx)
	    {
	    	return values[fromIdx][toIdx];
	    }
	    
	    public String getName()
	    {
	      return name;
	    }
    
	    public RelationEvaluatorFactory getFactory()
	    {
	      return NormalizeFactory.this;
	    }
	}

	protected class Editor extends JDialog implements ActionListener
	{
	    protected JTextField tfName;
	    protected boolean dataValid = false;
	    
	    public Editor(Evaluator evaluator, JFrame parent)
	    {
	    	super(parent);
	    	setTitle("Normalize Options");
	    	setModal(true);
	      
	    	getContentPane().setLayout(new BorderLayout());
	      
	    	// main content panel
	    	Box cPanel = new Box(BoxLayout.Y_AXIS);
	      
	    	// name panel
	    	JPanel namePanel = new JPanel(new BorderLayout());
	    	tfName = new JTextField(evaluator.getName());
	    	namePanel.add(BorderLayout.WEST, new JLabel("Name: "));
	    	namePanel.add(BorderLayout.CENTER, tfName);
	      
	    	// combination direction & value panel
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
			    dataValid = true;
		  	}
			if(JOkCancelPanel.CANCEL.equals(cmd))
			{
			    dataValid = false;
			}
			setVisible(false);
			dispose();
	    }
	    
	    public String getName()
	    {
	    	return tfName.getText();
	    }
	    
	    public boolean isDataValid()
	    {
	    	return dataValid;
	    }
	  }
	
}
