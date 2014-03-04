/**
 * TimeSeriesSource.java
 *
 * Copyright (c) 2002 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class TimeSeriesSource implements Source1D
{
	protected NetworkData nData;
	protected TimeSeriesNodeData selectedTS;
	protected Editor editor;
	protected Vector listeners;

	public TimeSeriesSource(NetworkData nData)
	{
		this.nData = nData;
		this.editor = new Editor();
		this.listeners = new Vector();
	}
	
	public double getValue(int nodeIdx)
	{
		return editor.getValue(nodeIdx);
	}
	
	public JPanel getEditor()
	{
		return editor;
	}
	
	public String toString()
	{
		return "Time Series";
	}
	
	public void addColorModelChangeListener(ColorModelChangeListener cmcl)
	{
		listeners.addElement(cmcl);
	}
	
	public void removeColorModelChangeListener(ColorModelChangeListener cmcl)
	{
		listeners.removeElement(cmcl);
	}
	
	protected void fireColorModelChanged(ChangeEvent ce)
	{
		for(int li=0; li<listeners.size(); li++)
		{
			ColorModelChangeListener cmcl = (ColorModelChangeListener)listeners.elementAt(li);
			cmcl.colorModelChanged(ce);
		}
	}

	public class Editor extends JPanel implements ChangeListener
	{
		protected JComboBox cbSeries;
		protected JSlider timeSlider;
		protected JSlider minSlider;
		protected JSlider maxSlider;
		protected JLabel lTime;
		protected JLabel lMin;
		protected JLabel lMax;
		protected int time;
		protected double min;
		protected double max;
		
		public Editor()
		{
			super();
			setLayout(new BoxLayout(Editor.this, BoxLayout.Y_AXIS));

			// sloppy...should be dynamic model
			this.cbSeries = new JComboBox();
			int tsCount = nData.getTimeSeriesCount();
			for(int ti=0; ti<tsCount; ti++)
			{
				cbSeries.addItem(nData.getTimeSeries(ti));
			}
			add(cbSeries);
			
			JPanel pControls = new JPanel(new BorderLayout());
			JPanel pLabels = new JPanel(new GridLayout(3, 1));
			lTime = new JLabel("Time");
			pLabels.add(lTime);
			lMin = new JLabel("Min");
			pLabels.add(lMin);
			lMax = new JLabel("Max");
			pLabels.add(lMax);
			pControls.add(BorderLayout.WEST, pLabels);

			JPanel pSliders = new JPanel(new GridLayout(3, 1));
			this.timeSlider = new JSlider();
			if(tsCount == 0)
			{
				timeSlider.setEnabled(false);
				cbSeries.setEnabled(false);
			}
			else
			{
				TimeSeriesNodeData ts = nData.getTimeSeries(0);
				timeSlider.setMaximum(ts.getSeriesLength() - 1);
			}
			timeSlider.addChangeListener(this);
			pSliders.add(timeSlider);
			
			this.minSlider = new JSlider();
			minSlider.addChangeListener(this);
			pSliders.add(minSlider);
			
			this.maxSlider = new JSlider();
			maxSlider.addChangeListener(this);
			pSliders.add(maxSlider);
			pControls.add(BorderLayout.CENTER, pSliders);
			add(pControls);
		}
		
		public void stateChanged(ChangeEvent e)
		{
			time = timeSlider.getValue();
			min = minSlider.getValue() * 0.01;
			max = maxSlider.getValue() * 0.01;

			// avoid runaway processes?
			if(!timeSlider.getValueIsAdjusting() &&
				!minSlider.getValueIsAdjusting() &&
				!maxSlider.getValueIsAdjusting())
			{
				fireColorModelChanged(new ChangeEvent(this));
			}
		}
		
		public void setFont(Font f)
		{
			super.setFont(f);
			if(timeSlider != null)
			{
				timeSlider.setFont(f);
				minSlider.setFont(f);
				maxSlider.setFont(f);
				lTime.setFont(f);
				lMin.setFont(f);
				lMax.setFont(f);
				cbSeries.setFont(f);
			}
		}
		
		public double getValue(int nodeIdx)
		{
			TimeSeriesNodeData ts = (TimeSeriesNodeData)cbSeries.getSelectedItem();
			if(ts == null)
			{
				return (max+min)/2.0;
			}
			else
			{
				return min + (max-min)*ts.getValue(nodeIdx, time);
			}
		}
	}
}

