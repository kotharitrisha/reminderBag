/**
 * BlockmodelSource.java
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
import com.wibinet.math.Partition;

public class BlockmodelSource implements Source1D
{
	protected NetworkData nData;
	protected double offset;
	protected Editor editor;
	protected Vector listeners;

	public BlockmodelSource(NetworkData nData)
	{
		this.nData = nData;
		this.offset = 0.0;
		this.editor = new Editor();
		this.listeners = new Vector();
	}
	
	public double getValue(int nodeIdx)
	{
		// how many partitions?
		Partition p = nData.getPartition();
		double pCt = p.getPartitionCount() * 1.0;
		
		// this isn't really right...really need inverted pidx
		double pIdx = p.getPartition(nodeIdx) * 1.0;
		double value = pIdx/pCt + offset;
		value = value-Math.floor(value);
		return value;
	}
	
	public JPanel getEditor()
	{
		return editor;
	}
	
	public String toString()
	{
		return "Blockmodel";
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
			offset = slider.getValue() * 0.01;

			// avoid runaway processes?
			if(!slider.getValueIsAdjusting())
			{
				fireColorModelChanged(new ChangeEvent(this));
			}
		}
	}
}

