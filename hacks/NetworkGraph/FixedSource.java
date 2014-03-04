/**
 * FixedSource.java
 *
 * Copyright (c) 2002 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class FixedSource implements Source1D
{
	protected double value;
	protected Editor editor;
	protected Vector listeners;

	public FixedSource()
	{
		this.value = 0.5;
		this.editor = new Editor();
		this.listeners = new Vector();
	}
	
	public FixedSource(double value)
	{
		this();
		this.value = value;
	}
	
	public void setValue(double value)
	{
		this.value = value;
	}
	
	public double getValue(int nodeIdx)
	{
		// System.out.println("    FixedSource.getValue("+nodeIdx+")");
		return value;
	}
	
	public String toString()
	{
		return "Fixed";
	}
	
	public JPanel getEditor()
	{
		return editor;
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
		protected JSlider slider;
		
		public Editor()
		{
			super(new BorderLayout());
			this.slider = new JSlider(0, 100);
			slider.addChangeListener(this);
			add(BorderLayout.NORTH, slider);
		}
		
		public void setFont(Font f)
		{
			super.setFont(f);
			if(slider != null)
			{
				slider.setFont(f);
			}
		}
		
		public void stateChanged(ChangeEvent e)
		{
			double sliderVal = slider.getValue() * 0.01;
			value = sliderVal;
			
			// avoid runaway processes?
			if(!slider.getValueIsAdjusting())
			{
				fireColorModelChanged(new ChangeEvent(this));
			}
		}
	}
}