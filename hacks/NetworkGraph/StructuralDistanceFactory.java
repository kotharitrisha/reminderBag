/**
 * StructuralDistanceFactory.java
 *
 * Modified 11/28/2003 to add a facility to edit the
 *   name of the instance, and to allow the user to
 *   choose to normalize inputs & outputs by the
 *   marginals ala Burt (1989).
 *
 * (c) 2001, 2003 Wibi Internet
 */

package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import com.wibinet.gui.*;

public class StructuralDistanceFactory implements RelationEvaluatorFactory
{
  public RelationEvaluator newInstance()
  {
    return new Evaluator();
  }
  
  public void edit(RelationEvaluator evaluator, JFrame parent)
  {
    Evaluator ev = (Evaluator)evaluator;
    Editor editor = new Editor(ev, parent);
    editor.setVisible(true);
    
    if(editor.isDataValid())
    {
      ev.normalize = editor.isNormalizing();
      ev.name = editor.getName();
    }
  }
  
  public Class getEvaluatorClass()
  {
    return Double.class;
  }
  
  public String getName()
  {
    return "Structural Distance";
  }
  
  public String getGroup()
  {
    return "Distance";
  }
	
  protected class Editor extends JDialog implements ActionListener
  {
    protected JTextField tfName;
    protected JCheckBox cbNormalizing;
    protected boolean normalize = false;
    protected boolean dataValid = false;
    
    public Editor(Evaluator evaluator, JFrame parent)
    {
      super(parent);
      setTitle("Structural Distance Options");
      setModal(true);
      
      getContentPane().setLayout(new BorderLayout());
      
      // main content panel
      Box cPanel = new Box(BoxLayout.Y_AXIS);
      
      // name panel
      JPanel namePanel = new JPanel(new BorderLayout());
      tfName = new JTextField(evaluator.getName());
      namePanel.add(BorderLayout.WEST, new JLabel("Name: "));
      namePanel.add(BorderLayout.CENTER, tfName);
      cPanel.add(namePanel);
			
      // normalization panel
      this.normalize = evaluator.normalize;
      JPanel nPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			cbNormalizing = new JCheckBox("Normalizing");
      nPanel.add(cbNormalizing);
      cPanel.add(nPanel);
      
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
        normalize = cbNormalizing.isSelected();
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
    
    public boolean isNormalizing()
    {
      return normalize;
    }
        
    public boolean isDataValid()
    {
      return dataValid;
    }
  }
  
  public class Evaluator implements RelationEvaluator
  {
    protected Relation r;
    protected String name;
		protected boolean normalize;
    
    protected double[][] distances;
    
    public Evaluator()
    {
      this.r = null;
      this.name = "Structural Distance";
			this.normalize = false;
    }
    
    public void runEvaluator()
    {
      int nSize = r.getNodeCount();

			// if we're normalizing, get marginals
			double[] inMarginals = new double[nSize];
			double[] outMarginals = new double[nSize];
			if(normalize)
			{
				for(int i=0; i<nSize; i++)
				{
					inMarginals[i] = 0.0;
					outMarginals[i] = 0.0;
				}
				
				for(int i=0; i<nSize; i++)
				{
					for(int j=0; j<nSize; j++)
					{
						// hmm ignore i==j?
						double z_ij = r.getTieStrength(i, j);
						inMarginals[j] += z_ij;
						outMarginals[i] += z_ij;
					}
				}
				
				// check to make sure these all are non-zero?
				for(int i=0; i<nSize; i++)
				{
					// this should throw!
					if(inMarginals[i] == 0.0)
					{
						System.out.println("inMarginal["+r.parent.getActor(i)+"]=0");
					}
					if(outMarginals[i] == 0.0)
					{
						System.out.println("outMarginal["+r.parent.getActor(i)+"]=0");
					}
				}
			}
			
      for(int i=0; i<nSize; i++)
      {
        for(int j=0; j<nSize; j++)
        {
          if(i != j)
          {
            double total = 0.0;
            for(int k=0; k<nSize; k++)
            {
              if((i != k) && (j != k))
              {
                double distOut = 0;
                double distIn = 0;
								if(normalize)
								{
									// i think this is right
									if((outMarginals[i] == 0) && (outMarginals[j] != 0))
									{
										distOut = r.getTieStrength(j, k) / outMarginals[j];
									}
									else if((outMarginals[i] != 0) && (outMarginals[j] == 0))
									{
										distOut = r.getTieStrength(i, k) / outMarginals[i];
									}
									else if((outMarginals[i] != 0) && (outMarginals[j] != 0))
									{
										distOut = r.getTieStrength(i, k) / outMarginals[i] -
											r.getTieStrength(j, k) / outMarginals[j];
									}
									
									if((inMarginals[i] == 0) && (inMarginals[j] != 0))
									{
										distIn = r.getTieStrength(k, j) / inMarginals[j];
									}
									else if((inMarginals[i] != 0) && (inMarginals[j] == 0))
									{
										distIn = r.getTieStrength(i, j) / inMarginals[i];
									}
									else if((inMarginals[i] != 0) && (inMarginals[j] != 0))
									{
										distIn = r.getTieStrength(k, i) / inMarginals[i] -
											r.getTieStrength(k, j) / inMarginals[j];
									}
								}
								else
								{
									distOut = (r.getTieStrength(i, k) - r.getTieStrength(j, k));
									distIn = (r.getTieStrength(k, i) - r.getTieStrength(k, j));
								}
                total += distOut * distOut;
                total += distIn * distIn;
              }
            }
						
						// i'm not sure this should *ever* be done.  it should certainly be
						// optioned at least...
						if(!normalize)
						{
							double distDiff = (r.getTieStrength(i, j) - r.getTieStrength(j, i));
							total += distDiff * distDiff;
						}
            distances[i][j] = Math.sqrt(total);
						if(Double.isNaN(distances[i][j]))
						{
							System.out.println("distances["+i+"]["+j+"] = " + distances[i][j]);
						}
          }
          else
          {
            distances[i][j] = 0.0;
          }
        }
      }
    }
    
    public void setRelation(Relation r)
    {
      this.r = r;
      int nSize = r.getNodeCount();
      distances = new double[nSize][];
      for(int i=0; i<nSize; i++)
      {
        distances[i] = new double[nSize];
        for(int j=0; j<nSize; j++)
        {
          distances[i][j] = 0.0;
        }
      }
    }
    
    public Object evaluateRelation(int fromIdx, int toIdx)
    {
      return new Double(distances[fromIdx][toIdx]);
    }
    
    public String getName()
    {
      return name;
    }
    
    public RelationEvaluatorFactory getFactory()
    {
      return StructuralDistanceFactory.this;
    }
  }
}