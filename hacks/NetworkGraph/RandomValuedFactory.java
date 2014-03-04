/**
 * RandomValuedFactory.java
 *
 * Copyright (c) 2004 Wibi Internet.
 * All rights reserved.
 */
 
package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import com.wibinet.gui.*;
import com.wibinet.math.Matrix;

public class RandomValuedFactory implements RandomNetworkFactory
{
	public Hashtable props;
	
	protected final static String ALPHA = "alpha";
	protected final static String BETA = "beta";
	protected final static String THETA = "theta";
	protected final static String THETA_SIGMA = "theta_sigma";
	protected final static String RHO = "rho";
	protected final static String SIZE = "size";
	
	protected final static double[] tAlpha = {
		-0.5, -0.5, -0.5, 0.0, 0.0, 0.0, 0.5, 0.5, 0.5
	};
	protected final static double[] tBeta = {
		0.2, 0.2, 0.0, 0.0, 0.4, 0.4, -0.3, -0.3, -0.6
	};
		
	public RandomValuedFactory()
	{
		this.props = new Hashtable();
		
		int size = 9;
		double[] alpha = new double[size];
		double[] beta = new double[size];
		for(int i=0; i<size; i++)
		{
			alpha[i] = (i<3)?-0.5:((i<6)?0.0:0.5);
			beta[i] = (i<3)?0.5:((i<6)?0.0:-0.5);
			// alpha[i] = (0.4*i) - 1.6;
			// beta[i] = 0.4 - (0.1*i);
		}
		double theta = 2.0;
		double theta_sigma = 0.01;
		double rho = 0.2;
		props.put(ALPHA, tAlpha);
		props.put(BETA, tBeta);
		props.put(THETA, new Double(theta));
		props.put(THETA_SIGMA, new Double(theta_sigma));
		props.put(RHO, new Double(rho));
		props.put(SIZE, new Integer(size));
	}
	
  public VisualNetworkData generateNetwork()
	{
		Random random = new Random();
		
		// get alphas, betas, etc...
		int size = ((Integer)props.get(SIZE)).intValue();
		double[] alpha = (double[])props.get(ALPHA);
		double[] beta = (double[])props.get(BETA);
		double theta = ((Double)props.get(THETA)).doubleValue();
		double theta_sigma = ((Double)props.get(THETA_SIGMA)).doubleValue();
		double rho = ((Double)props.get(RHO)).doubleValue();
		
		// initialize
		double[][] ties = new double[size][];
		for(int i=0; i<size; i++)
		{
			ties[i] = new double[size];
		}
	
		// populate (by dyads)	
		for(int i=0; i<size; i++)
		{
			for(int j=i+1; j<size; j++)
			{
				double mean_ij = theta + ((alpha[i] + beta[j]) +
					rho * (alpha[j] + beta[i]) )/ (1.0+rho);
				double mean_ji = theta + ((alpha[j] + beta[i]) +
					rho * (alpha[i] + beta[j])) / (1.0+rho);
				// System.out.println("mean["+i+"]["+j+"]="+mean_ij);
				// System.out.println("mean["+j+"]["+i+"]="+mean_ji);
				double x_ij = Math.exp(mean_ij + theta_sigma * random.nextGaussian());
				double x_ji = Math.exp(mean_ji + theta_sigma * random.nextGaussian());
				ties[i][j] = x_ij;
				ties[j][i] = x_ji;
			}
		}
		
		NetworkData nData = new NetworkData(ties, true);
		return new VisualNetworkData(nData);
	}
	
  public boolean edit(JFrame parent)
	{
		int size = ((Integer)props.get(SIZE)).intValue();
		RandomNetworkFactory.NetworkSizeDialog sizeDialog = 
			new RandomNetworkFactory.NetworkSizeDialog(parent, size);
		sizeDialog.setVisible(true);
		int tmpSize = sizeDialog.getNetworkSize();
		if(tmpSize <= 0)
		{
			// a bit hacky...user can (in theory) enter negative value...
			return false;
		}
		size = tmpSize;
		
		Editor editor = new Editor(parent, size);
		editor.setVisible(true);
		if(editor.isCancelled())
		{
			return false;
		}
		editor.updateParameters();
		return true;
	}
	
	protected final static String[] abLabels = {"\u03B1", "\u03B2"};
	public class Editor extends JDialog implements ActionListener
	{
		protected Matrix mAlphaBeta;
		protected DoubleNumberField dfTheta;
		protected DoubleNumberField dfSigma;
		protected DoubleNumberField dfRho;
		protected boolean cancelled;
		protected int size;
		
		public Editor(JFrame parent, int size)
		{
			super(parent, "Edit Parameters", true);
			this.size = size;
			mAlphaBeta = new Matrix(size, 2);
			cancelled = false;
			
			JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP);
			
			// alpha beta params
			LabelModel lmAlphaBeta = new DefaultLabelModel(abLabels);
			LabelModel lmActors = new PrefixLabelModel("Actor ", size);
			MatrixTableModel abModel = new MatrixTableModel(mAlphaBeta,
				lmActors, lmAlphaBeta, true);
			WJTable table = new WJTable(abModel);
			tabPane.add("Actors", table); // scroller?

			// other parameters
			JPanel pOther = new JPanel(new GridLayout(3, 1));
			JPanel pTheta = new JPanel(new BorderLayout());
			pTheta.add(BorderLayout.WEST, new JLabel("\u03B8 (scale): "));
			dfTheta = new DoubleNumberField(((Double)props.get(THETA)).doubleValue(), 5);
			pTheta.add(BorderLayout.CENTER, dfTheta);
			pOther.add(pTheta);
			JPanel pSigma = new JPanel(new BorderLayout());
			pSigma.add(BorderLayout.WEST, new JLabel("\u03C3 (spread): "));
			dfSigma = new DoubleNumberField(((Double)props.get(THETA_SIGMA)).doubleValue(), 5);
			pSigma.add(BorderLayout.CENTER, dfSigma);
			pOther.add(pSigma);
			JPanel pRho = new JPanel(new BorderLayout());
			pRho.add(BorderLayout.WEST, new JLabel("\u03C1 (reciprocity): "));
			dfRho = new DoubleNumberField(((Double)props.get(RHO)).doubleValue(), 5);
			pRho.add(BorderLayout.CENTER, dfRho);
			pOther.add(pRho);
			JPanel enclosure = new JPanel(new BorderLayout());
			enclosure.add(BorderLayout.NORTH, pOther);
			tabPane.add("Network", enclosure);

			getContentPane().add(BorderLayout.CENTER, tabPane);
			JOkCancelPanel ocp = new JOkCancelPanel();
			ocp.addActionListener(this);
			getContentPane().add(BorderLayout.SOUTH, ocp);
			pack();
		}
		
		public boolean isCancelled()
		{
			return cancelled;
		}
		
		public void actionPerformed(ActionEvent ae)
		{
			String cmd = ae.getActionCommand();
			if(JOkCancelPanel.CANCEL.equals(cmd))
			{
				cancelled = true;
				setVisible(false);
				return;
			}
			else
			{
				// confirm sums are zero
				double alphaSum = 0;
				double betaSum = 0;
				for(int i=0; i<size; i++)
				{
					alphaSum += mAlphaBeta.values[i][0];
					betaSum += mAlphaBeta.values[i][1];
				}
				double zeroThresh = 0.0001;
				boolean alphaZero = Math.abs(alphaSum) < zeroThresh;
				boolean betaZero = Math.abs(betaSum) < zeroThresh;
				if(!alphaZero || !betaZero)
				{
					String msg = (alphaZero?"":"Alphas") +
						((alphaZero || betaZero)?"":" and ") +
						(betaZero?"":"Betas") + " do not have zero sum.";
					JOptionPane.showMessageDialog(Editor.this, msg, "Parameter Error",
						JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				// confirm rho
				double rho = dfRho.getValue();
				if((rho < 0.0) || (rho > 1.0))
				{
					JOptionPane.showMessageDialog(Editor.this,
						"Reciprocity (\u03C1) must be between 0 and 1.", 
						"Parameter Error",
						JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(rho > 1.0)
				{
					JOptionPane.showMessageDialog(Editor.this, "\u03C1 > 1", "Parameter Error",
						JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				// everything is okay
				cancelled = false;
				setVisible(false);
				return;
			}
		}
		
		public void updateParameters()
		{
			double[] alpha = new double[size];
			double[] beta = new double[size];
			
			for(int i=0; i<size; i++)
			{
				alpha[i] = mAlphaBeta.values[i][0];
				beta[i] = mAlphaBeta.values[i][1];
			}
			props.put(ALPHA, tAlpha);
			props.put(BETA, tBeta);
			props.put(THETA, new Double(dfTheta.getValue()));
			props.put(THETA_SIGMA, new Double(dfSigma.getValue()));
			props.put(RHO, new Double(dfRho.getValue()));
		}
	}
	
	public Hashtable getProperties()
	{
		return props;
	}
	
	public void setProperties(Hashtable props)
	{
		this.props = props;
	}
	
  public String getName()
	{
		return "Random Valued";
	}
	
  public String getGroup()
	{
		return null;
	}
}