/**
 * AnalysisSource.java
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

public class AnalysisSource implements Source1D
{
	protected NetworkData nData;
	protected TimeSeriesNodeData selectedTS;
	protected double offset;
	protected Editor editor;
	protected Vector listeners;

	public AnalysisSource(NetworkData nData)
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
		return "Analysis";
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

	public class Editor extends JPanel implements ChangeListener,
		DataChangeListener, ItemListener
	{
		protected JComboBox cbAnalysis;
		protected AnalysesModel aModel;
		protected JSlider minSlider;
		protected JSlider maxSlider;
		protected JLabel lMin;
		protected JLabel lMax;
		protected int time;
		protected double min;
		protected double max;
		protected double[] values;
		
		public Editor()
		{
			super();
			setLayout(new BoxLayout(Editor.this, BoxLayout.Y_AXIS));

			// sloppy...should be dynamic model
			this.aModel = new AnalysesModel();
			this.cbAnalysis = new JComboBox(aModel);
			cbAnalysis.addItemListener(this);
			add(cbAnalysis);
			
			JPanel pControls = new JPanel(new BorderLayout());
			JPanel pLabels = new JPanel(new GridLayout(2, 1));
			lMin = new JLabel("Min");
			pLabels.add(lMin);
			lMax = new JLabel("Max");
			pLabels.add(lMax);
			pControls.add(BorderLayout.WEST, pLabels);

			JPanel pSliders = new JPanel(new GridLayout(2, 1));
			if(nData.getAnalysesCount() == 0)
			{
				// cbAnalysis.setEnabled(false);
			}

			this.minSlider = new JSlider();
			minSlider.addChangeListener(this);
			pSliders.add(minSlider);
			this.maxSlider = new JSlider();
			maxSlider.addChangeListener(this);
			pSliders.add(maxSlider);

			pControls.add(BorderLayout.CENTER, pSliders);
			add(pControls);
			add(Box.createGlue());

			int nSize = nData.getSize();
			values = new double[nSize];
			for(int i=0; i<nSize; i++)
			{
				values[i] = 0.5;
			}
		}
		
		public void stateChanged(ChangeEvent e)
		{
			min = minSlider.getValue() * 0.01;
			max = maxSlider.getValue() * 0.01;

			// avoid runaway processes?
			if(!minSlider.getValueIsAdjusting() &&
				!maxSlider.getValueIsAdjusting())
			{
				fireColorModelChanged(new ChangeEvent(this));
			}
		}
		
		public void itemStateChanged(ItemEvent e)
		{
			// only on 'selection' event
			if(e.getStateChange() == ItemEvent.SELECTED)
			{
				updateModel();
				fireColorModelChanged(new ChangeEvent(this));
			}
		}
		
		protected void updateModel()
		{
			// get the selected analysis
			NodeEvaluator ne = aModel.getSelectedAnalysis();
			int idx = aModel.getSelectedAnalysisIndex();
			
			// System.out.println("ne: " + ne);
			if(ne == null)
			{
				// this is ok...happens nothing is selected?
				return;
			}
			ne.runEvaluator();
			
			// based on type, find some stuff
			int nSize = nData.getSize();
			Class outputClass = ne.getFactory().getEvaluatorClass();
			if(Double.class.equals(outputClass))
			{
				// find output min & max
				double omin = Double.MAX_VALUE;
				double omax = -Double.MAX_VALUE;
				for(int i=0; i<nSize; i++)
				{
					double val = ((Double)ne.evaluateNode(i, idx)).doubleValue();
					if(omin > val) omin = val;
					if(omax < val) omax = val;
				}
				double orange = omax-omin;
				if(orange == 0.0) orange = 1.0;
				
				for(int i=0; i<nSize; i++)
				{
					// probably should do NaN/infinity checks...
					double val = ((Double)ne.evaluateNode(i, idx)).doubleValue();
					values[i] = (val - omin) / orange;
				}
			}
		}
		
		public void setFont(Font f)
		{
			super.setFont(f);
			if(minSlider != null)
			{
				minSlider.setFont(f);
				maxSlider.setFont(f);
				lMin.setFont(f);
				lMax.setFont(f);
				cbAnalysis.setFont(f);
			}
		}
		
		public double getValue(int nodeIdx)
		{
			return min + (max-min)*values[nodeIdx];
		}
		
		public void dataChanged(DataChangeEvent dce)
		{
		}
	}
	
	public class AnalysesModel implements ComboBoxModel,
		ChangeListener
	{
		protected int selIdx;
		protected Vector listeners;
		
		public AnalysesModel()
		{
			selIdx = -1;
			this.listeners = new Vector();
			nData.addAnalysisChangeListener(this);
		}
		
		public int getSize()
		{
  			int count = nData.getAnalysesCount();
  			int relCt = nData.getRelationCount();
  			int count_sum = 0;
  			for(int ai=0; ai<count; ai++)
  			{
  				if(nData.getAnalysis(ai).getFactory().isMultiple())
  				{
  					count_sum += relCt;
  				}
  				else
  				{
  					count_sum += 1;
  				}
  			}
  			return count_sum;
		}
		
		public Object getElementAt(int i)
		{
			if(i == -1) return null;

			int count_pos = 0;
  			int count = nData.getAnalysesCount();
  			int relCt = nData.getRelationCount();
			for(int ai=0; ai<count; ai++)
			{
				NodeEvaluator eval = nData.getAnalysis(ai);
				if(eval.getFactory().isMultiple())
				{
					int next_pos = count_pos + relCt;
					if((i >= count_pos) && (i < next_pos))
					{
						return eval.getName() + ": " +
							nData.getRelation(i - count_pos).getName();
					}
					count_pos += next_pos;
				}
				else 
				{
					if(count_pos == i)
					{
						return eval.getName();
					}
					count_pos++;
				}
			}
			return null;
		}
		
		public NodeEvaluator getAnalysis(int i)
		{
			if(i == -1) return null;
			int count_pos = 0;
  			int count = nData.getAnalysesCount();
  			int relCt = nData.getRelationCount();
			for(int ai=0; ai<count; ai++)
			{
				NodeEvaluator eval = nData.getAnalysis(ai);
				if(eval.getFactory().isMultiple())
				{
					int next_pos = count_pos + relCt;
					if((i >= count_pos) && (i < next_pos))
					{
						return eval;
					}
					count_pos += next_pos;
				}
				else 
				{
					if(count_pos == i)
					{
						return eval;
					}
					count_pos++;
				}
			}
			return null;
		}
		
		public int getAnalysisIndex(int i)
		{
			if(i == -1) return -1;

			int count_pos = 0;
  			int count = nData.getAnalysesCount();
  			int relCt = nData.getRelationCount();
			for(int ai=0; ai<count; ai++)
			{
				NodeEvaluator eval = nData.getAnalysis(ai);
				if(eval.getFactory().isMultiple())
				{
					int next_pos = count_pos + relCt;
					if((i >= count_pos) && (i < next_pos))
					{
						return i - count_pos;
					}
					count_pos += next_pos;
				}
				else 
				{
					if(count_pos == i)
					{
						return 0;
					}
					count_pos++;
				}
			}
			return -1;
		}
		
		public void addListDataListener(ListDataListener ldl)
		{
			listeners.addElement(ldl);
		}
		
		public void removeListDataListener(ListDataListener ldl)
		{
			listeners.removeElement(ldl);
		}
		
		protected void fireContentsChanged(ListDataEvent le)
		{
			for(int i=0; i<listeners.size(); i++)
			{
				ListDataListener ldl = (ListDataListener)listeners.elementAt(i);
				ldl.contentsChanged(le);
			}
		}
		
		public void setSelectedItem(Object o)
		{
			int ct = nData.getAnalysesCount();
			for(int i=0; i<ct; i++)
			{
				if(nData.getAnalysis(i).getName().equals(o))
				{
					selIdx = i;
					return;
				}
			}
		}
		
		public Object getSelectedItem()
		{
			return getElementAt(selIdx);
		}
		
		public NodeEvaluator getSelectedAnalysis()
		{
			return getAnalysis(selIdx);
		}
		
		public int getSelectedAnalysisIndex()
		{
			return getAnalysisIndex(selIdx);
		}
		
		public void stateChanged(ChangeEvent ce)
		{
			ListDataEvent lde = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED,
				0, 0);
			
			// update model
			editor.updateModel();
		}
	}
}

