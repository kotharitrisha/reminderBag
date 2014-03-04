/**
 * REGEDistanceFactory.java
 *
 * A class that implements White & Reitz's regular equivalence
 * distance measure.
 *
 * Copyright (c) 2005 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import com.wibinet.gui.*;

public class REGEDistanceFactory implements RelationEvaluatorFactory
{
	private static double m_weight[][];
	private static double weight_pattern[][] = {
		{0, 0, 0, 0},
		{0, 1, 0, 1},
		{0, 0, 1, 1},
		{0, 1, 1, 2}
	};
	static
	{
		m_weight = new double[16][];
		for(int i=0; i<16; i++)
		{
			m_weight[i] = new double[16];
		}
		for(int i=0; i<4; i++)
		{
			for(int j=0; j<4; j++)
			{
				for(int k=0; k<4; k++)
				{
					for(int m=0; m<4; m++)
					{
						m_weight[i*4+k][j*4+m] = weight_pattern[i][j] + 
							weight_pattern[k][m];
					}
				}
			}
		}
	}
	
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
    }
  }
  
  public Class getEvaluatorClass()
  {
    return Double.class;
  }

  public String getName()
  {
    return "REGE Distance";
  }
  
  public String getGroup()
  {
    return "Distance";
  }
	
  protected class Editor extends JDialog implements ActionListener
  {
    protected JTextField tfName;
    protected boolean dataValid = false;
    
    public Editor(Evaluator evaluator, JFrame parent)
    {
      super(parent);
      setTitle("REGE Distance Options");
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
            
    public boolean isDataValid()
    {
      return dataValid;
    }
  }

  public class Evaluator implements RelationEvaluator
  {
    protected Relation r;
    protected String name;
		protected boolean invert;
		protected int maxSteps;
		protected double tolerance;
    
    protected double[][] distances;
    
    public Evaluator()
    {
      this.r = null;
      this.name = "REGE Distance";
			this.invert = true;
			this.maxSteps = 7; 
			this.tolerance = 0.01;
    }
    
		public void evaluateBlaze()
		{
			int nSize = r.getNodeCount();
			
			// setup the RMD matrix (i think we only need half of this)
			double[][] rmd = new double[nSize][];
			double[][] next_rmd = new double[nSize][];
			for(int i=0; i<nSize; i++)
			{
				rmd[i] = new double[nSize];
				next_rmd[i] = new double[nSize];
				for(int j=0; j<nSize; j++)
				{
					rmd[i][j] = 1.0; // how should this be initialized?
					next_rmd[i][j] = Double.NaN;
				}
			}
			
			// cache the degrees
			double[] degree = new double[nSize];
			for(int i=0; i<nSize; i++)
			{
				degree[i] = 0.0;
				for(int j=0; j<nSize; j++)
				{
					if(i != j)
					{
						degree[i] += (r.getTieStrength(i, j) > 0.0)?1:0;
						degree[i] += (r.getTieStrength(j, i) > 0.0)?1:0;
					}
				}
			}
			
			// setup triad census
			System.err.println("Running triad census");
			int[][][][] triads = new int[nSize][][][];
			for(int i=0; i<nSize; i++)
			{
				triads[i] = new int[nSize][][];
				for(int j=0; j<nSize; j++)
				{
					triads[i][j] = new int[16][];
					triads[i][j][0] = new int[0]; // null list...
					
					java.util.Vector mVec[] = new java.util.Vector[16];
					for(int m_type=1; m_type<16; m_type++)
					{
						mVec[m_type] = new java.util.Vector();
					}
					
					for(int m=0; m<nSize; m++)
					{
						// figure out where to add this m
						boolean b_im = r.getTieStrength(i, m) > 0.0;
						boolean b_mi = r.getTieStrength(m, i) > 0.0;
						boolean b_jm = r.getTieStrength(j, m) > 0.0;
						boolean b_mj = r.getTieStrength(m, j) > 0.0;

						// what type of dyad pair is [jm, im]
						int jmim_type = (b_im?1:0) + (b_mi?2:0) + (b_jm?4:0) + (b_mj?8:0);
						if(jmim_type > 0)
						{
							mVec[jmim_type].addElement(new Integer(m));
						}
					}
					
					// convert back to int[] lists
					for(int m_type=1; m_type<16; m_type++)
					{
						triads[i][j][m_type] = new int[mVec[m_type].size()];
						for(int k=0; k<triads[i][j][m_type].length; k++)
						{
							triads[i][j][m_type][k] =
								((Integer)mVec[m_type].elementAt(k)).intValue();
						}
					}
				}
			}
			
			// iterate
			int step = 0;
			while(step < maxSteps)
			{
				System.err.println("rmd[0][0]="+rmd[0][0]);
				System.err.print("step #" + (step+1) + "[");
				System.err.flush();
				for(int i=0; i<nSize; i++)
				{
					System.err.print(".");
					System.err.flush();
					for(int j=i+1; j<nSize; j++)
					{
						double k_sum = 0.0;
						for(int k=0; k<nSize; k++)
						{
							if((k != i) && (k != j))
							{
								double max_product = 0.0;

								// convert these to booleans
								boolean b_ik = r.getTieStrength(i, k) > 0.0;
								boolean b_ki = r.getTieStrength(k, i) > 0.0;
								boolean b_jk = r.getTieStrength(j, k) > 0.0;
								boolean b_kj = r.getTieStrength(k, j) > 0.0;

								// what type of dyad pair is [ik, jk]
								int ikjk_type = (b_jk?1:0) + (b_kj?2:0) + (b_ik?4:0) + (b_ki?8:0);
								int best_m = -1;
								int match_type = -1;
								for(int m_type=1; m_type<16; m_type++)
								{
									// do we care about this list for this ikjk type?
									if(m_weight[ikjk_type][m_type] > 0.0)
									{
										// only check m's on the list
										for(int m_idx=0; m_idx<triads[i][j][m_type].length; m_idx++)
										{
											double test_product = rmd[k][triads[i][j][m_type][m_idx]] *
												m_weight[ikjk_type][m_type];
											if(test_product > max_product)
											{
												max_product = test_product;
												best_m = triads[i][j][m_type][m_idx];
												match_type = m_type;
											}
										}
									}
								}

								k_sum += max_product;
								//System.err.println("best for ("+i+", "+j+", "+k+") = " + best_m + "["+
								//	max_product+"/"+ikjk_type+"/"+match_type+"]");
							}
						}
						
						next_rmd[i][j] = k_sum / (degree[i] + degree[j]);
						next_rmd[j][i] = next_rmd[i][j];
					}
				}
				System.err.println("]");
				
				// check to see if we've had significant change
				double maxChange = 0.0;
				for(int i=0; i<nSize; i++)
				{
					for(int j=i+1; j<nSize; j++)
					{
						if(Math.abs(next_rmd[i][j] - rmd[i][j]) > maxChange)
						{
							maxChange = Math.abs(next_rmd[i][j] - rmd[i][j]);
						}
					}
				}
				System.err.println("maxChange = " + maxChange);
				if(maxChange < tolerance)
				{
					break;
				}

				// otherwise, keep going...
				for(int i=0; i<nSize; i++)
				{
					rmd[i][i] = 1.0;
					next_rmd[i][i] = Double.NaN;
					for(int j=i+1; j<nSize; j++)
					{
						rmd[i][j] = next_rmd[i][j];
						next_rmd[i][j] = Double.NaN;
						rmd[j][i] = next_rmd[j][i];
						next_rmd[j][i] = Double.NaN;
					}
				}
				
				// make sure this doesn't go forever
				step++;
			} 

			// copy into results
			for(int i=0; i<nSize; i++)
			{
				for(int j=0; j<nSize; j++)
				{
					if(invert)
					{
						distances[i][j] = 1.0-rmd[i][j];
					}
					else
					{
						distances[i][j] = rmd[i][j];
					}
				}
			}
		}
		
    public void _runEvaluator()
    {
      int nSize = r.getNodeCount();

			// setup the RMD matrix (i think we only need half of this)
			double[][] rmd = new double[nSize][];
			double[][] next_rmd = new double[nSize][];
			for(int i=0; i<nSize; i++)
			{
				rmd[i] = new double[nSize];
				next_rmd[i] = new double[nSize];
				for(int j=0; j<nSize; j++)
				{
					rmd[i][j] = 1.0; // how should this be initialized?
				}
			}
			
			// cache the degrees
			double[] degree = new double[nSize];
			for(int i=0; i<nSize; i++)
			{
				degree[i] = 0.0;
				for(int j=0; j<nSize; j++)
				{
					if(i != j)
					{
						degree[i] += r.getTieStrength(i, j);
						degree[i] += r.getTieStrength(j, i);
					}
				}
			}
						
			// iterate
			int step = 0;
			while(step < maxSteps)
			{
				System.err.print("step #" + (step+1) + "[");
				System.err.flush();
				for(int i=0; i<nSize; i++)
				{
					System.err.print(".");
					System.err.flush();
					for(int j=0; j<i; j++)
					{
						double k_sum = 0.0;
						// double denom_sum = 0.0;
						for(int k=0; k<nSize; k++)
						{
							double max_product = Double.NEGATIVE_INFINITY;
							// int max_m = -1;
							for(int m=0; m<nSize; m++)
							{
								double match_ijkm = Math.min(r.getTieStrength(i, k), r.getTieStrength(j, m)) +
									Math.min(r.getTieStrength(k, i), r.getTieStrength(m, j));
								double match_jikm = Math.min(r.getTieStrength(j, k), r.getTieStrength(i, m)) +
									Math.min(r.getTieStrength(k, j), r.getTieStrength(m, i));
								double product = rmd[k][m] * (match_ijkm + match_jikm);
								if(product > max_product)
								{
									max_product = product;
									// max_m = m;
								}
							}
							k_sum += max_product;
						}
						
						// next_rmd[i][j] = k_sum / denom_sum;
						// next_rmd[j][i] = k_sum / denom_sum;
						next_rmd[i][j] = k_sum / (degree[i] + degree[j]);
						next_rmd[j][i] = next_rmd[i][j];
					}
				}
				System.err.println("]");
				
				// check to see if we've had significant change
				double maxChange = 0.0;
				for(int i=0; i<nSize; i++)
				{
					for(int j=i+1; j<nSize; j++)
					{
						if(Math.abs(next_rmd[i][j] - rmd[i][j]) > maxChange)
						{
							maxChange = Math.abs(next_rmd[i][j] - rmd[i][j]);
						}
					}
				}
				System.err.println("maxChange = " + maxChange);
				if(maxChange < tolerance)
				{
					break;
				}

				// otherwise, keep going...
				for(int i=0; i<nSize; i++)
				{
					rmd[i][i] = 1.0;
					next_rmd[i][i] = Double.NaN;
					for(int j=i+1; j<nSize; j++)
					{
						rmd[i][j] = next_rmd[i][j];
						next_rmd[i][j] = Double.NaN;
						rmd[j][i] = next_rmd[j][i];
						next_rmd[j][i] = Double.NaN;
					}
				}
				
				// make sure this doesn't go forever
				step++;
			} 

			// copy into results
			for(int i=0; i<nSize; i++)
			{
				for(int j=0; j<nSize; j++)
				{
					if(invert)
					{
						distances[i][j] = 1.0-rmd[i][j];
					}
					else
					{
						distances[i][j] = rmd[i][j];
					}
				}
			}
    }
    
    public void runEvaluator()
    {
      int nSize = r.getNodeCount();

			// setup the RMD matrix (i think we only need half of this)
			double[][] rmd = new double[nSize][];
			double[][] next_rmd = new double[nSize][];
			for(int i=0; i<nSize; i++)
			{
				rmd[i] = new double[nSize];
				next_rmd[i] = new double[nSize];
				for(int j=0; j<nSize; j++)
				{
					rmd[i][j] = 1.0; // how should this be initialized?
				}
			}
			
			// cache the degrees
			double[] degree = new double[nSize];
			for(int i=0; i<nSize; i++)
			{
				degree[i] = 0.0;
				for(int j=0; j<nSize; j++)
				{
					if(i != j)
					{
						degree[i] += r.getTieStrength(i, j);
						degree[i] += r.getTieStrength(j, i);
					}
				}
			}
			
			// figure out which ijs are tied to which ms
			int[][][] mList = new int[nSize][][];
			for(int i=0; i<nSize; i++)
			{
				mList[i] = new int[nSize][];
				for(int j=0; j<nSize; j++)
				{
					java.util.Vector mVec = new java.util.Vector();
					for(int m=0; m<nSize; m++)
					{
						if((r.getTieStrength(i, m) > 0.0) ||
							 (r.getTieStrength(m, i) > 0.0) ||
							 (r.getTieStrength(j, m) > 0.0) ||
							 (r.getTieStrength(m, j) > 0.0))
						{
							mVec.addElement(new Integer(m));
						}
					}
					mList[i][j] = new int[mVec.size()];
					for(int m=0; m<mList[i][j].length; m++)
					{
						mList[i][j][m] = ((Integer)mVec.elementAt(m)).intValue();
					}
				}
			}
						
			// iterate
			int step = 0;
			while(step < maxSteps)
			{
				System.err.print("step #" + (step+1) + "[");
				System.err.flush();
				for(int i=0; i<nSize; i++)
				{
					System.err.print(".");
					System.err.flush();
					for(int j=0; j<i; j++)
					{
						double k_sum = 0.0;
						// double denom_sum = 0.0;
						for(int k=0; k<nSize; k++)
						{
							double max_product = 0.0;
							
							double ik = r.getTieStrength(i, k);
							double ki = r.getTieStrength(k, i);
							double jk = r.getTieStrength(j, k);
							double kj = r.getTieStrength(k, j);
							
							// get mList for this i,j pair
							for(int m_idx=0; m_idx<mList[i][j].length; m_idx++)
							{
								double match_ijkm = Math.min(ik, r.getTieStrength(j, mList[i][j][m_idx])) +
									Math.min(ki, r.getTieStrength(mList[i][j][m_idx], j));
								double match_jikm = Math.min(jk, r.getTieStrength(i, mList[i][j][m_idx])) +
									Math.min(kj, r.getTieStrength(mList[i][j][m_idx], i));
								double product = rmd[k][mList[i][j][m_idx]] * (match_ijkm + match_jikm);
								if(product > max_product)
								{
									max_product = product;
								}
							}
							k_sum += max_product;
						}

						next_rmd[i][j] = k_sum / (degree[i] + degree[j]);
						next_rmd[j][i] = next_rmd[i][j];
					}
				}
				System.err.println("]");
				
				// check to see if we've had significant change
				double maxChange = 0.0;
				for(int i=0; i<nSize; i++)
				{
					for(int j=i+1; j<nSize; j++)
					{
						if(Math.abs(next_rmd[i][j] - rmd[i][j]) > maxChange)
						{
							maxChange = Math.abs(next_rmd[i][j] - rmd[i][j]);
						}
					}
				}
				System.err.println("maxChange = " + maxChange);
				if(maxChange < tolerance)
				{
					break;
				}

				// otherwise, keep going...
				for(int i=0; i<nSize; i++)
				{
					rmd[i][i] = 1.0;
					next_rmd[i][i] = Double.NaN;
					for(int j=i+1; j<nSize; j++)
					{
						rmd[i][j] = next_rmd[i][j];
						next_rmd[i][j] = Double.NaN;
						rmd[j][i] = next_rmd[j][i];
						next_rmd[j][i] = Double.NaN;
					}
				}
				
				// make sure this doesn't go forever
				step++;
			} 

			// copy into results
			for(int i=0; i<nSize; i++)
			{
				for(int j=0; j<nSize; j++)
				{
					if(invert)
					{
						distances[i][j] = 1.0-rmd[i][j];
					}
					else
					{
						distances[i][j] = rmd[i][j];
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
      return REGEDistanceFactory.this;
    }
  }
}