/**
 * DiscretizeFactory.java
 *
 * August 22, 2007: Added option to use z-score.
 * 
 * Copyright (c) 2001-2007 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.wibinet.gui.*;
import com.wibinet.math.QuickSort;

public class DiscretizeFactory implements RelationEvaluatorFactory
{
	protected final static Double ONE = new Double(1.0);
	protected final static Double ZERO = new Double(0.0);

	protected final static byte NORMAL = 0;
	protected final static byte INVERTED = 1;

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
			ev.splitPoint = editor.getSplitPoint();
			ev.direction = editor.getDirection();
			ev.useMedian = editor.isUseMedian();
			ev.useZScore = editor.isUseZScore();
			ev.name = editor.getName();
		}
	}
  
	public Class getEvaluatorClass()
	{
		return Double.class;
	}
  
	public String getName()
	{
		return "Discretize";
	}
  
	public String getGroup()
	{
		return "Filter";
	}
  
	public class Evaluator implements RelationEvaluator
	{
		protected Relation r;
		protected String name;
		protected double splitPoint;
		protected byte direction;
		
		protected boolean useZScore;
		protected boolean useMedian;
		protected double sampleValue;
		protected double stdDev;
    
		public Evaluator()
		{
			this.r = null;
			this.name = "Discretize";
			this.splitPoint = 0.0;
			this.direction = NORMAL;
			
			this.useZScore = false;
			this.useMedian = false;
			this.sampleValue = Double.NaN;
			this.stdDev = Double.NaN;
		}
    
		public void runEvaluator()
		{
			if(useZScore)
			{
				int nSize = r.getNodeCount();
				if(useMedian)
				{
					sampleValue = getMedian();
				}
				else
				{
					sampleValue = getMean();
					if(Double.isNaN(sampleValue) || Double.isInfinite(sampleValue))
					{
						sampleValue = getMedian(); // automatic?
					}
				}
				
				// now get stdev?
				double varSum = 0.0;
				double obsCt = 0.0;
				for(int i=0; i<nSize; i++)
				{
					for(int j=0; j<nSize; j++)
					{
						double x_ij = r.getTieStrength(i, j);
						if(!(Double.isNaN(x_ij) || Double.isInfinite(x_ij)))
						{
							double diff = x_ij - sampleValue;
							varSum += diff * diff;
							obsCt += 1.0;
						}
					}
				}
				double variance = varSum / obsCt;
				this.stdDev = Math.sqrt(variance);
			}
		}
		
		protected double getMedian()
		{
			int nSize = r.getNodeCount();
			Double[] obs = new Double[nSize * nSize]; // huge?
			for(int i=0; i<nSize; i++)
			{
				for(int j=0; j<nSize; j++)
				{
					obs[i*nSize+j] = new Double(r.getTieStrength(i, j));
				}
			}
			QuickSort.sortInPlace(obs);
			return obs[obs.length/2].doubleValue();
		}
		
		protected double getMean()
		{
			int nSize = r.getNodeCount();
			double total = 0.0;
			for(int i=0; i<nSize; i++)
			{
				for(int j=0; j<nSize; j++)
				{
					total += r.getTieStrength(i, j);
				}
			}
			return total / (nSize * nSize);
		}
    
		public void setRelation(Relation r)
		{
			this.r = r;
		}
    
		public Object evaluateRelation(int fromIdx, int toIdx)
		{
			double testValue = r.getTieStrength(fromIdx, toIdx);
			if(useZScore)
			{
				testValue = (testValue - sampleValue) / stdDev;
			}
			if(direction == NORMAL)
			{
				return (testValue > splitPoint) ? ONE : ZERO;
			}
			else
			{
				return (testValue < splitPoint) ? ONE : ZERO;
			}
		}
    
		public String getName()
		{
			return name;
		}
    
		public RelationEvaluatorFactory getFactory()
		{
			return DiscretizeFactory.this;
		}
	}
	
	protected class Editor extends JDialog implements ActionListener
	{
		protected JTextField tfName;
		protected JComboBox cbDirection;
		protected JCheckBox cbUseZScore;
		protected JCheckBox cbUseMedian;
		protected byte direction;
		protected DoubleNumberField dfSplitPoint;
		protected double splitPoint;
		protected boolean useZScore;
		protected boolean useMedian;
		protected boolean dataValid = false;
    
		public Editor(Evaluator evaluator, JFrame parent)
		{
			super(parent);
			setTitle("Discretize Options");
			setModal(true);
      
			getContentPane().setLayout(new BorderLayout());
      
			// main content panel
			Box cPanel = new Box(BoxLayout.Y_AXIS);
      
			// name panel (temporarily disabled)
			JPanel namePanel = new JPanel(new BorderLayout());
			tfName = new JTextField(evaluator.getName());
			namePanel.add(BorderLayout.WEST, new JLabel("Name: "));
			namePanel.add(BorderLayout.CENTER, tfName);
  
			// combination direction & value panel
			this.direction = evaluator.direction;
			this.splitPoint = evaluator.splitPoint;
			JPanel dvPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			dvPanel.add(new JLabel("Direction:"));
			String[] directions = new String[2];
			directions[NORMAL] = "Normal";
			directions[INVERTED] = "Inverted";
			cbDirection = new JComboBox(directions);
			cbDirection.setEditable(false);
			cbDirection.setSelectedIndex(this.direction);
			dvPanel.add(cbDirection);
			dfSplitPoint = new DoubleNumberField(evaluator.splitPoint, 5);
			dvPanel.add(dfSplitPoint);
			cPanel.add(dvPanel);
			
			// z-score/use-median box
			this.useMedian = evaluator.useMedian;
			this.useZScore = evaluator.useZScore;
			JPanel zmPanel = new JPanel(new GridLayout(2, 1));
			cbUseZScore = new JCheckBox("Use Z-Score", useMedian);
			zmPanel.add(cbUseZScore);
			cbUseMedian = new JCheckBox("Use Median", useZScore);
			zmPanel.add(cbUseMedian);
			cPanel.add(zmPanel);
      
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
				direction = (byte)cbDirection.getSelectedIndex();
				splitPoint = dfSplitPoint.getValue();
				useZScore = cbUseZScore.isSelected();
				useMedian = cbUseMedian.isSelected();
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
    
		public byte getDirection()
		{
			return direction;
		}
    
		public double getSplitPoint()
		{
			return splitPoint;
		}
		
		public boolean isUseZScore()
		{
			return useZScore;
		}
		
		public boolean isUseMedian()
		{
			return useMedian;
		}
        
		public boolean isDataValid()
		{
			return dataValid;
		}
	}
}