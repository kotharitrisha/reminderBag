/**
 * CutoffFactory.java
 *
 * A relation evaluator (filter) that applies a cutoff
 * to all of the tie values.
 *
 * (c) 2001 Wibi Internet
 */

package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.wibinet.gui.*;

import com.wibinet.math.*;

public class CutoffFactory implements RelationEvaluatorFactory
{
  public final static int CUTOFF_ABOVE = 0;
  public final static int CUTOFF_BELOW = 1;
  
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
      se.type = ed.getType();
      se.cutoffValue = ed.getCutoffValue();
      se.name = ed.getName();
    }
  }
  
  public Class getEvaluatorClass()
  {
    return Double.class;
  }
  
  public String getName()
  {
    return "Cutoff";
  }
  
  public String getGroup()
  {
    return "Filter";
  }
  
  public class Evaluator implements RelationEvaluator
  {
    protected Relation r;
    protected String name;
    protected int type;
    protected double cutoffValue;
    		
    public Evaluator()
    {
      this.r = null;
      this.name = "Cutoff";
      this.type = CUTOFF_BELOW;
      this.cutoffValue = 0.0;
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
      double ts = r.getTieStrength(fromIdx, toIdx);
      switch(type)
      {
        case CUTOFF_ABOVE:
          if(ts > cutoffValue)
          {
            return new Double(cutoffValue);
          }
          else
          {
            return new Double(ts);
          }
        
        case CUTOFF_BELOW:
          if(ts < cutoffValue)
          {
            return new Double(cutoffValue);
          }
          else
          {
            return new Double(ts);
          }
      }
      return new Double(Double.NaN);
    }
    
    public String getName()
    {
      return name;
    }
    
    public RelationEvaluatorFactory getFactory()
    {
      return CutoffFactory.this;
    }
  }
  
  protected class EditDialog extends JDialog implements ActionListener
  {
    protected JTextField tfName;
    protected JComboBox cbType;
    protected int type;
    protected DoubleNumberField dfCutoffValue;
    protected double cutoffValue;
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
      
      // combination type & value panel
      this.type = evaluator.type;
      this.cutoffValue = evaluator.cutoffValue;
      JPanel tvPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      tvPanel.add(new JLabel("Cutoff values"));
      String[] types = new String[2];
      types[CUTOFF_ABOVE] = "Above";
      types[CUTOFF_BELOW] = "Below";
      cbType = new JComboBox(types);
      cbType.setEditable(false);
      cbType.setSelectedIndex(this.type);
      tvPanel.add(cbType);
      dfCutoffValue = new DoubleNumberField(this.cutoffValue, 5);
      tvPanel.add(dfCutoffValue);
      cPanel.add(tvPanel);
      
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
        type = cbType.getSelectedIndex();
        cutoffValue = dfCutoffValue.getValue();
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
    
    public int getType()
    {
      return type;
    }
    
    public double getCutoffValue()
    {
      return cutoffValue;
    }
        
    public boolean isDataValid()
    {
      return dataValid;
    }
  }
}