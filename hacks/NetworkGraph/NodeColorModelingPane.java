/**
 * NodeColorModelingPane.java
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

public class NodeColorModelingPane extends JPanel 
	implements ColorModelChangeListener
{
	protected NodeColorModel cModel;
	protected NetworkData nData;
	protected CardLayout[] cardLayouts;
	protected Vector listeners;
	
	public NodeColorModelingPane(NodeColorModel cModel, NetworkData nData)
	{
		super(new GridLayout(3, 1));
		this.cModel = cModel;
		this.nData  = nData;
		this.cardLayouts = new CardLayout[3];
		this.listeners = new Vector();
		
		Font f = new Font("SansSerif", Font.PLAIN, 10);
		setFont(f);

		JPanel pHue = new JPanel(new BorderLayout());
		pHue.setFont(f);
		pHue.setBorder(BorderFactory.createTitledBorder("Hue"));
		pHue.add(BorderLayout.CENTER, createModelChooser(0, 0.4));
		add(pHue);

		JPanel pSaturation = new JPanel(new BorderLayout());
		pSaturation.setFont(f);
		pSaturation.setBorder(BorderFactory.createTitledBorder("Saturation"));
		pSaturation.add(BorderLayout.CENTER, createModelChooser(1, 0.2));
		add(pSaturation);
		
		JPanel pBrightness = new JPanel(new BorderLayout());
		pBrightness.setFont(f);
		pBrightness.setBorder(BorderFactory.createTitledBorder("Brightness"));
		pBrightness.add(BorderLayout.CENTER, createModelChooser(2, 1.0));
		add(pBrightness);
	}
	
	protected JPanel createModelChooser(int type, double value)
	{
		JPanel p = new JPanel(new BorderLayout());
		Font f = new Font("SansSerif", Font.PLAIN, 10);
		p.setFont(f);
		
		// we need to get the current sources from the color model
		Source1D selectedSrc = cModel.getSource(type);
		Source1D[] sources = new Source1D[4];
		if(selectedSrc instanceof FixedSource)
		{
			sources[0] = selectedSrc;
		}
		else
		{
			sources[0] = new FixedSource(value);
		}
		if(selectedSrc instanceof BlockmodelSource)
		{
			sources[1] = selectedSrc;
		}
		else
		{
			sources[1] = new BlockmodelSource(nData);
		}
		if(selectedSrc instanceof AnalysisSource)
		{
			sources[2] = selectedSrc;
		}
		else
		{
			sources[2] = new AnalysisSource(nData);
		}
		if(selectedSrc instanceof TimeSeriesSource)
		{
			sources[3] = selectedSrc;
		}
		else
		{
			sources[3] = new TimeSeriesSource(nData);
		}

		JComboBox cbType = new JComboBox(sources);
		cbType.setSelectedItem(selectedSrc);
		cbType.setFont(f);
		p.add(BorderLayout.NORTH, cbType);
		
		cardLayouts[type] = new CardLayout();
		JPanel editorPane = new JPanel(cardLayouts[type]);
		editorPane.setFont(f);
		cbType.addItemListener(new ModelChangeListener(type, editorPane));
		for(int si=0; si<sources.length; si++)
		{
			JPanel editor = sources[si].getEditor();
			editor.setFont(f);
			editorPane.add(sources[si].toString(), editor);
			
			// odd place to put this, but...
			sources[si].addColorModelChangeListener(this);
		}
		cardLayouts[type].show(editorPane, selectedSrc.toString());
		p.add(BorderLayout.CENTER, editorPane);
		return p;
	}
	
	public void colorModelChanged(ChangeEvent e)
	{
		// pass it along...
		fireColorModelChanged(e);
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
	
	public class ModelChangeListener implements ItemListener
	{
		protected int type;
		protected Container container;
		
		public ModelChangeListener(int type, Container c)
		{
			this.type = type;
			this.container = c;
		}
		
		public void itemStateChanged(ItemEvent e)
		{
			ItemSelectable is = e.getItemSelectable();
			Object[] objs = is.getSelectedObjects();
			if(objs.length > 0)
			{
				Source1D source = (Source1D)objs[0];
				String srcName = source.toString();
				cardLayouts[type].show(container, srcName);
				cModel.setSource(type, source);
				ChangeEvent ce = new ChangeEvent(this);
				fireColorModelChanged(ce);
			}
		}
	}
}