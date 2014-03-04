/**
 * SymmetrizeFactory.java
 *
 * (c) 2001 Wibi Internet
 */

package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.wibinet.gui.*;

public class SymmetrizeFactory implements RelationEvaluatorFactory
{
  public final static int METHOD_MAX = 0;
  public final static int METHOD_MIN = 1;
  public final static int METHOD_AVG = 2;
  
  public RelationEvaluator newInstance()
  {
    return new Evaluator();
  }
  
  public void edit(RelationEvaluator evaluator, JFrame parent)
  {
    Evaluator se = (Evaluator)evaluator;
    EditDialog ed = new EditDialog(se, parent);
    ed.setVisible(true);
    
    if(ed.isDataValid())
    {
      se.method = ed.getMethod();
      se.name = ed.getName();
    }
  }
  
  public Class getEvaluatorClass()
  {
    return Double.class;
  }
  
  public String getName()
  {
    return "Symmetrize";
  }
  
  public String getGroup()
  {
    return "Filter";
  }
  
  public class Evaluator implements RelationEvaluator
  {
    protected Relation r;
    protected String name;
    protected int method;
    
    public Evaluator()
    {
      this.r = null;
      this.name = "Symmetrize";
      this.method = METHOD_MAX;
    }
    
    public void runEvaluator()
    {
    }
    
    public void setRelation(Relation r)
    {
      this.r = r;
    }
    
    public Object evaluateRelation(int fromIdx, int toIdx)
    {
      double val1 = r.getTieStrength(fromIdx, toIdx);
      double val2 = r.getTieStrength(toIdx, fromIdx);
      
      switch(method)
      {
        case METHOD_MAX:
          return new Double(Math.max(val1, val2));
        
        case METHOD_MIN:
          return new Double(Math.min(val1, val2));
        
        case METHOD_AVG:
          return new Double((val1 + val2) / 2.0);
      }
      return new Double(Double.NaN);
    }
    
    public String getName()
    {
      return name;
    }
    
    public RelationEvaluatorFactory getFactory()
    {
      return SymmetrizeFactory.this;
    }
  }
  
  protected class EditDialog extends JDialog implements ActionListener
  {
    protected JTextField tfName;
    protected JRadioButton rbMax;
    protected JRadioButton rbMin;
    protected JRadioButton rbAvg;
    protected int type;
    protected boolean dataValid = false;
    
    public EditDialog(Evaluator evaluator, JFrame parent)
    {
      super(parent);
      setTitle("Symmetrize Options");
      setModal(true);
      
      getContentPane().setLayout(new BorderLayout());
      
      // main content panel
      Box cPanel = new Box(BoxLayout.Y_AXIS);
      
      // name panel (temporarily disabled)
      JPanel namePanel = new JPanel(new BorderLayout());
      tfName = new JTextField(evaluator.getName());
      namePanel.add(BorderLayout.WEST, new JLabel("Name: "));
      namePanel.add(BorderLayout.CENTER, tfName);
      // cPanel.add(namePanel);
      
      // type panel
      this.type = evaluator.method;
      ButtonGroup bg = new ButtonGroup();
      JPanel typePanel = new JPanel(new GridLayout(3, 1));
      rbMax = new JRadioButton("Maximum Value", (evaluator.method==METHOD_MAX));
      bg.add(rbMax);
      typePanel.add(rbMax);
      rbMin = new JRadioButton("Minimum Value", (evaluator.method==METHOD_MIN));
      bg.add(rbMin);
      typePanel.add(rbMin);
      rbAvg = new JRadioButton("Average", (evaluator.method==METHOD_AVG));
      bg.add(rbAvg);
      typePanel.add(rbAvg);
      cPanel.add(typePanel);
      
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
        if(rbMax.isSelected())
        {
          type = METHOD_MAX;
        }
        else if(rbMin.isSelected())
        {
          type = METHOD_MIN;
        }
        else if(rbAvg.isSelected())
        {
          type = METHOD_AVG;
        }
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
    
    public int getMethod()
    {
      return type;
    }
        
    public boolean isDataValid()
    {
      return dataValid;
    }
  }
}