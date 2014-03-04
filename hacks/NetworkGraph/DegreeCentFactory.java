/**
 * DegreeCentFactory.java
 *
 * This factory generates node evaluators that calculate degree
 * centrality.  (Need reference)
 *
 * (c) 2000 Wibi Internet
 */
 
package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import com.wibinet.gui.*;

public class DegreeCentFactory implements NodeEvaluatorFactory
{
  protected final static byte IN_DEGREE = 0;
  protected final static byte OUT_DEGREE = 1;
  protected final static byte AVG_DEGREE = 2;
  
  public NodeEvaluator newInstance()
  {
    return new DegreeCentralityEvaluator();
  }
  
  public void edit(NodeEvaluator evaluator, JFrame parent)
  {
    if(!(evaluator instanceof DegreeCentralityEvaluator))
    {
      return; // error?
    }
    
    // create an editor dialog box
    JDialog editor = new DegreeCentralityEditor(parent, (DegreeCentralityEvaluator)evaluator);
    editor.setVisible(true);
  }
  
  protected class DegreeCentralityEditor extends JDialog implements ActionListener,
  	ItemListener
  {
    protected DegreeCentralityEvaluator evaluator;
    protected JTextField tfName;
    protected JRadioButton rbInDegree;
    protected JRadioButton rbOutDegree;
    protected JRadioButton rbAvgDegree;
    
    public DegreeCentralityEditor(JFrame parent, DegreeCentralityEvaluator evaluator)
    {
      super(parent, "Degree Centrality");
      setModal(true);
      this.evaluator = evaluator;
      
      getContentPane().setLayout(new BorderLayout());
      
      // main prefs panel
      JPrefsPanel prefsPanel = new JPrefsPanel(2);
      tfName = new JTextField(evaluator.getName());
      prefsPanel.addPrefComponent("Name", tfName);
      
      ButtonGroup bg = new ButtonGroup();
      JPanel typePanel = new JPanel(new GridLayout(3, 1));
      rbInDegree = new JRadioButton("In Degree", (evaluator.type==IN_DEGREE));
      rbInDegree.addItemListener(this);
      bg.add(rbInDegree);
      typePanel.add(rbInDegree);
      rbOutDegree = new JRadioButton("OutDegree", (evaluator.type==OUT_DEGREE));
      rbOutDegree.addItemListener(this);
      bg.add(rbOutDegree);
      typePanel.add(rbOutDegree);
      rbAvgDegree = new JRadioButton("Average Degree", (evaluator.type==AVG_DEGREE));
      rbAvgDegree.addItemListener(this);
      bg.add(rbAvgDegree);
      typePanel.add(rbAvgDegree);
      prefsPanel.addPrefComponent("Type", typePanel); 
      
      getContentPane().add(BorderLayout.CENTER, prefsPanel);

      // ok/cancel panel
      JOkCancelPanel okCancelPanel = new JOkCancelPanel();
      okCancelPanel.addActionListener(this);
      getContentPane().add(BorderLayout.SOUTH, okCancelPanel);
      
      pack();
    }
    
    public void actionPerformed(ActionEvent ae)
    {
      String cmd = ae.getActionCommand();
      if(JOkCancelPanel.OK.equals(cmd))
      {
        // update evaluator
        evaluator.name = tfName.getText();
        if(rbInDegree.isSelected())
        {
          evaluator.type = IN_DEGREE;
        }
        else if(rbOutDegree.isSelected())
        {
          evaluator.type = OUT_DEGREE;
        }
        else if(rbAvgDegree.isSelected())
        {
          evaluator.type = AVG_DEGREE;
        }
        setVisible(false);
      }
      else if(JOkCancelPanel.CANCEL.equals(cmd))
      {
        // don't update evaluator
        setVisible(false);
      }
    }
    
    public void itemStateChanged(ItemEvent evt)
    {
    	JRadioButton rb = (JRadioButton)evt.getSource();
    	
    	// in theory we should only do this if name hasn't been changed yet?
      if(rbInDegree.isSelected())
      {
        tfName.setText("In");
      }
      else if(rbOutDegree.isSelected())
      {
        tfName.setText("Out");
      }
      else if(rbAvgDegree.isSelected())
      {
        tfName.setText("Avg");
      }
	  }
  }
  
  public Class getEvaluatorClass()
  {
    return Double.class;
  }
  
  public String getName()
  {
    return "Degree Centrality";
  }
  
  public String getGroup()
  {
    return "Centrality";
  }
  
	public boolean isMultiple()
	{
		return false;
	}
	
  public class DegreeCentralityEvaluator implements NodeEvaluator
  {
    protected NetworkData nd;
    protected String name;
    protected byte type;
    
    public DegreeCentralityEvaluator()
    {
      this.nd = null;
      this.name = "Freeman";
      this.type = AVG_DEGREE;
    }
    
    public void runEvaluator()
    {
    }
    
	  public void setNetwork(NetworkData nd)
	  {
	    this.nd = nd;
	  }
	  
	  // this one should split out...
	  public Object evaluateNode(int idx, int ri)
	  {
		  return evaluateNode(idx);
	  }
	  
	  public Object evaluateNode(int idx)
	  {
	    switch(type)
	    {
	    	case IN_DEGREE:
			    return new Double(nd.getInDegreeCentrality(idx));
			    
	    	case OUT_DEGREE:
			    return new Double(nd.getOutDegreeCentrality(idx));
			    
	    	case AVG_DEGREE:
			    return new Double(nd.getDegreeCentrality(idx));
	    }
	    
	    return null; // error!
	  }
	  
	  public String getName()
	  {
	    return name;
	  }
	  
	  public NodeEvaluatorFactory getFactory()
	  {
	    return DegreeCentFactory.this;
	  }
  }
}