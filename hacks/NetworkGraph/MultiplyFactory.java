/**
 * MultiplyFactory.java
 *
 * A relation evaluator (filter) that multiplies all tie values
 * by a constant.
 *
 * (c) 2001 Wibi Internet
 */

package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.wibinet.gui.*;

public class MultiplyFactory implements RelationEvaluatorFactory
{
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
      se.value = ed.getValue();
      se.name = ed.getName();
    }
  }
  
  public Class getEvaluatorClass()
  {
    return Double.class;
  }
  
  public String getName()
  {
    return "Multiply";
  }
  
  public String getGroup()
  {
    return "Filter";
  }
  
  public class Evaluator implements RelationEvaluator
  {
    protected Relation r;
    protected String name;
    protected double value;
    
    public Evaluator()
    {
      this.r = null;
      this.name = "Cutoff";
      this.value = 1.0;
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
      return new Double(value * r.getTieStrength(fromIdx, toIdx));
    }
    
    public String getName()
    {
      return name;
    }
    
    public RelationEvaluatorFactory getFactory()
    {
      return MultiplyFactory.this;
    }
  }
  
  protected class EditDialog extends JDialog implements ActionListener
  {
    protected JTextField tfName;
    protected DoubleNumberField dfValue;
    protected double value;
    protected boolean dataValid = false;
    
    public EditDialog(Evaluator evaluator, JFrame parent)
    {
      super(parent);
      setTitle("Cutoff Options");
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
      
      // value panel
      this.value = evaluator.value;
      JPanel valuePanel = new JPanel(new BorderLayout());
      dfValue = new DoubleNumberField(this.value, 5);
      valuePanel.add(BorderLayout.WEST, new JLabel("Multiply values by: "));
      valuePanel.add(BorderLayout.CENTER, dfValue);
      cPanel.add(valuePanel);
      
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
        value = dfValue.getValue();
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
    
    public double getValue()
    {
      return value;
    }
        
    public boolean isDataValid()
    {
      return dataValid;
    }
  }
}