/**
 * MDLEvaluatorFactory.java
 *
 * (c) 2000 Wibi Internet
 */

package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import com.wibinet.app.*;
import com.wibinet.math.Partition;

public class MDLEvaluatorFactory implements NodeEvaluatorFactory
{
  protected ActorBlockmodelFactory factory;
  protected static ActorBlockmodelFactory[] allFactories;
  static
  {
  	BlockmodelFactory[] factories = (BlockmodelFactory[])
  		Application.getPlugIns("com.wibinet.networks.BlockmodelFactory");
  	Vector vFactories = new Vector();
  	for(int fi=0; fi<factories.length; fi++)
  	{
  		if(factories[fi] instanceof ActorBlockmodelFactory)
  		{
  			vFactories.addElement(factories[fi]);
  		}
  	}
  	allFactories = new ActorBlockmodelFactory[vFactories.size()];
  	for(int fi=0; fi<allFactories.length; fi++)
  	{
  		allFactories[fi] = (ActorBlockmodelFactory)vFactories.elementAt(fi);
  	}
  }
  
  public MDLEvaluatorFactory()
  {
  	this.factory = allFactories[0];
  }
  
  public NodeEvaluator newInstance()
  {
  	return new MDLEvaluator();
  }
  
  public void edit(NodeEvaluator evaluator, JFrame parent)
  {
  	// only bother if there are multiple factories...
  	if(allFactories.length > 1)
  	{
	  	MDLEvaluatorFactory evFactory = (MDLEvaluatorFactory)evaluator.getFactory();
	  	ActorBlockmodelFactory abFactory = evFactory.factory;
	  	Editor editor = new Editor(parent, factory);
	  	editor.show();
	  	this.factory = editor.getFactory();
	  }
  	return;
  }
  
  public Class getEvaluatorClass()
  {
  	return Double.class;
  }
  
  public String getName()
  {
  	return "MDL Evaluator";
  }
  
  public String getGroup()
  {
  	return "Blockmodeling";
  }
  
	public boolean isMultiple()
	{
		return false;
	}
  
  protected class Editor extends JDialog implements ActionListener
  {
  	protected MDLComboBoxModel cbm;
  	
  	public Editor(JFrame parent, ActorBlockmodelFactory factory)
  	{
  		super(parent, "Choose Blockmodel Type");
  		setModal(true);
  		getContentPane().setLayout(new BorderLayout());
  		
  		cbm = new MDLComboBoxModel();
  		cbm.setSelectedItem(factory);
  		JComboBox cb = new JComboBox(cbm);
  		getContentPane().add(BorderLayout.CENTER, cb);
  		
  		JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
  		JButton ok = new JButton("OK");
  		ok.addActionListener(this);
  		p.add(ok);
  		getContentPane().add(BorderLayout.SOUTH, p);
  		
  		pack();
  	}
  	
  	public void actionPerformed(ActionEvent ae)
  	{
  		dispose();
  	}
  	
  	public ActorBlockmodelFactory getFactory()
  	{
  		return cbm.getFactory();
  	}
  }
 
  protected class MDLComboBoxModel extends DefaultComboBoxModel
  {
  	public MDLComboBoxModel()
  	{
  		super();
  		for(int i=0; i<allFactories.length; i++)
  		{
  			addElement(allFactories[i].getType());
  		}
  	}
  	
  	public void setSelectedItem(Object obj)
  	{
  		if(obj instanceof ActorBlockmodelFactory)
  		{
  			super.setSelectedItem(((ActorBlockmodelFactory)obj).getType());
  		}
  		else
  		{
  			super.setSelectedItem(obj);
  		}
  	}
  	  	
  	public ActorBlockmodelFactory getFactory()
  	{
  		int idx = getIndexOf(getSelectedItem());
  		return allFactories[idx];
  	}
  }
  
  protected class MDLEvaluator implements NodeEvaluator
  {
    protected NetworkData nd;
    protected String name;
    protected ActorBlockmodel model;
    
    public MDLEvaluator()
    {
      this.nd = null;
      this.name = factory.getType();
      this.model = null;
    }
    
    public void runEvaluator()
    {
    	this.model = factory.newActorBlockmodelInstance(nd, nd.getPartition());
    	model.compute(null);
    }
    
	  public void setNetwork(NetworkData nd)
	  {
	    this.nd = nd;
	  }
	  
	  public Object evaluateNode(int idx, int ri)
	  {
		  return evaluateNode(idx);
	  }
	  
	  public Object evaluateNode(int idx)
	  {
	  	return new Double(Math.log(model.getActorProbability(idx)));
	  }
	  
	  public String getName()
	  {
	    return name;
	  }
	  
	  public NodeEvaluatorFactory getFactory()
	  {
	    return MDLEvaluatorFactory.this;
	  }
  }
}