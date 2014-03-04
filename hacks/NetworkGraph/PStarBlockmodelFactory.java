/**
 * PStarBlockmodelFactory.java
 *
 * Copyright (c) 2003 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import com.wibinet.ai.core.*;
import com.wibinet.app.*;
import com.wibinet.gui.*;
import com.wibinet.math.LogitSolver;
import com.wibinet.math.Matrix;
import com.wibinet.math.Partition;
import com.wibinet.math.RegressionResult;

public class PStarBlockmodelFactory extends AbstractBlockmodelFactory
{
	protected Hashtable props;
	protected final static String TYPE = "Type";
	protected final static String TOLERANCE = "Tolerance";
	
	protected final static int UPDATE_STEP_TIE   = 0;
	protected final static int UPDATE_STEP_DYAD  = 1;
	protected final static int UPDATE_STEP_TRIAD = 2;
	protected final static int UPDATE_STEP_ALL   = 3;
	
	private final static java.text.NumberFormat fmt = java.text.NumberFormat.getInstance();
	static
	{
		fmt.setMaximumFractionDigits(5);
	}

	public PStarBlockmodelFactory()
	{
		this.props = new Hashtable();
    
    // string values?
		/*props.put(TYPE, new Integer(P1Blockmodel.IN_GROUP));
    props.put(TOLERANCE, new Double(0.001));*/
	}
	
	public Blockmodel newInstance(NetworkData nData, Partition p)
	{
		/*int type = ((Integer)props.get(TYPE)).intValue();
    double tolerance = ((Double)props.get(TOLERANCE)).doubleValue();*/
		Model model = new Model(nData, p);
		return model;
	}
	
	public boolean edit(Blockmodel model)
	{
		Editor ed = new Editor((Model)model);
		ed.setVisible(true);
		return !ed.isCancelled();
		
		/*int type = ed.getType();
    double tolerance = ed.getTolerance();*/

		/*// actually set model to this type
		((P1Blockmodel)model).setType(type);
    
    // set tolerance
    ((P1Blockmodel)model).setTolerance(tolerance);
		
		// remember type for next time...
		props.put(TYPE, new Integer(type));*/
	}
	
	public Hashtable getProperties(Blockmodel model)
	{
		return props;
	}

	public void setProperties(Blockmodel model, Hashtable props)
	{
		/*int newType;
		double newTolerance;
		
		Object oType = props.get(TYPE);
		if(oType instanceof String)
		{
			newType = Integer.parseInt((String)oType);
		}
		else
		{
			newType = ((Integer)oType).intValue();
		}

		Object oTolerance = props.get(TOLERANCE);
		if(oTolerance instanceof String)
		{
			newTolerance = Double.parseDouble((String)oTolerance);
		}
		else
		{
			newTolerance = ((Double)oTolerance).doubleValue();
		}

		((P1Blockmodel)model).setType(newType);
		((P1Blockmodel)model).setTolerance(newTolerance);*/
	}

	public String getType()
	{
		return "P-star";
	}
	
	protected final static String ADD = "Add";
	protected final static String DELETE = "Delete";
	protected final static String EDIT = "Edit";
	
	protected class Editor extends JDialog implements ActionListener,
		ListSelectionListener
	{
		protected Model model;
		protected boolean cancelled;

		protected JList lAllMeasures;
		protected JList lChosenMeasures;
		protected DefaultListModel lmChosen;
		protected JButton bAdd;
		protected JButton bDelete;
		protected JButton bEdit;
		
		public Editor(Model model)
		{
			super((JFrame)null);
			this.model = model;
			this.cancelled = false;
			setModal(true);
			setTitle("P-star Blockmodel Options");
			getContentPane().setLayout(new BorderLayout());

			JPanel pChooser = new JPanel(new GridLayout(1, 2));
			JPanel pAllMeasures = new JPanel(new BorderLayout());
			lAllMeasures = new JList(NetworkMeasureFactory.Factories);
			lAllMeasures.addListSelectionListener(this);
			pAllMeasures.add(BorderLayout.CENTER, new JScrollPane(lAllMeasures));
			JPanel pMeasuresButtons = new JPanel(new GridLayout(3, 1));
			bAdd = new JButton("Add >>");
			bAdd.setActionCommand(ADD);
			bAdd.addActionListener(this);
			bAdd.setEnabled(false);
			pMeasuresButtons.add(bAdd);
			bDelete = new JButton("<< Delete");
			bDelete.setActionCommand(DELETE);
			bDelete.addActionListener(this);
			bDelete.setEnabled(false);
			pMeasuresButtons.add(bDelete);
			bEdit = new JButton("Edit");
			bEdit.setActionCommand(EDIT);
			bEdit.addActionListener(this);
			bEdit.setEnabled(false);
			pMeasuresButtons.add(bEdit);
			pAllMeasures.add(BorderLayout.SOUTH, pMeasuresButtons);
			pChooser.add(pAllMeasures);
			lmChosen = new DefaultListModel();
			for(int mi=0; mi<model.measures.length; mi++)
			{
				lmChosen.addElement(model.measures[mi]);
			}
			lChosenMeasures = new JList(lmChosen);
			lChosenMeasures.addListSelectionListener(this);
			pChooser.add(new JScrollPane(lChosenMeasures));
			getContentPane().add(BorderLayout.CENTER, pChooser);
			
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
				Object[] objs = lmChosen.toArray();
				model.measures = new NetworkMeasure[objs.length];
				for(int mi=0; mi<model.measures.length; mi++)
				{
					model.measures[mi] = (NetworkMeasure)objs[mi];
				}
				cancelled = false;
				setVisible(false);
				dispose();
			}
			else if(JOkCancelPanel.CANCEL.equals(cmd))
			{
				cancelled = true;
				setVisible(false);
				dispose();
			}
			else if(ADD.equals(cmd))
			{
				NetworkMeasureFactory factory =
					(NetworkMeasureFactory)lAllMeasures.getSelectedValue();
				if(factory == null)
				{
					return;
				}
				if(factory.isMultiple())
				{
					NetworkMeasure[] measures = factory.newInstances(model.getNetwork());
					if(measures.length > 0)
					{
						factory.edit(measures[0], this);
						for(int mi=0; mi<measures.length; mi++)
						{
							lmChosen.addElement(measures[mi]);
						}
					}
				}
				else
				{
					NetworkMeasure measure = factory.newInstance();
					factory.edit(measure, this);
					lmChosen.addElement(measure);
				}
			}
			else if(DELETE.equals(cmd))
			{
				// this probably should keep user from deleting the first
				// subgroup measure?
				Object[] selObjs = lChosenMeasures.getSelectedValues();
				for(int mi=0; mi<selObjs.length; mi++)
				{
					lmChosen.removeElement(selObjs[mi]);
				}
				NetworkMeasure measure =
					(NetworkMeasure)lChosenMeasures.getSelectedValue();
				if(measure == null)
				{
					return;
				}
				lmChosen.removeElement(measure);
			}
			else if(EDIT.equals(cmd))
			{
				Object[] selObjs = lChosenMeasures.getSelectedValues();
				for(int mi=0; mi<selObjs.length; mi++)
				{
					NetworkMeasure measure = (NetworkMeasure)selObjs[mi];
					NetworkMeasureFactory factory = measure.getFactory();
					factory.edit(measure, this);
				}
			}
		}
		
		public boolean isCancelled()
		{
			return cancelled;
		}
		
		public void valueChanged(ListSelectionEvent lse)
		{
			bAdd.setEnabled(lAllMeasures.getSelectedValue() != null);
			bEdit.setEnabled(lChosenMeasures.getSelectedValue() != null);
			bDelete.setEnabled(lChosenMeasures.getSelectedValue() != null);
		}
	}
	
	protected void invertNetwork(NetworkData nd)
	{
		int size = nd.getSize();
		// flip it all
		for(int i=0; i<size; i++)
		{
			for(int j=0; j<size; j++)
			{
				if(i != j)
				{
					double x_ij = nd.getTieStrength(i, j);
					nd.setTieStrength(i, j, 1.0-x_ij);
				}
			}
		}
	}
	
	protected double[] getStatistics(NetworkData nd, NetworkMeasure[] measures)
	{
		double[] stats = new double[measures.length];
		for(int mi=0; mi<measures.length; mi++)
		{
			stats[mi] = measures[mi].getStatistic(nd);
		}
		return stats;
	}

	public class Model extends AbstractBlockmodel
	{
		protected NetworkMeasure[] measures;
		// protected PStarEvaluator[] evaluator;
		// protected Matrix[] predictedTies;
		// protected Matrix[] tieProbs;
		protected Random random;
		protected double[] us_prob;
		protected RegressionResult[] results;
		
		public Model(NetworkData nData, Partition p)
		{
			super(nData, p);
			this.measures = new NetworkMeasure[0];
			// this.evaluator = new PStarEvaluator[0];
			// this.predictedTies = new Matrix[0];
			// this.tieProbs = new Matrix[0];
			this.random = new Random();
			this.results = new RegressionResult[0];
			
			this.us_prob = new double[4];
			us_prob[UPDATE_STEP_TIE]   = 1.00;
			us_prob[UPDATE_STEP_DYAD]  = 0.30;
			us_prob[UPDATE_STEP_TRIAD] = 0.09;
			us_prob[UPDATE_STEP_ALL]   = 0.01;
		}
		
		public void compute(AbstractTask task)
		{
			int size = nData.getSize();
			
			// rather explicitly taken from Snijders (2001) JSS
			
			// phase 1: generate $N_1$ independent networks Y(n) according to
			// initial parameter estimates.
			// if i'm not mistaken, both SIENA and statnet use non-zero starting
			// values only for density and reciprocity, as they are relatively
			// easy to compute short form.
			if(task != null)
			{
				task.setDescription("Phase 1: Estimating Distribution");
				Thread.yield();
			}
			
			double[] initialEstimates = new double[measures.length];
			double[] observed = new double[measures.length];
			for(int mi=0; mi<measures.length; mi++)
			{
				initialEstimates[mi] = measures[mi].getInitialEstimate(nData);
				observed[mi] = measures[mi].getStatistic(nData);
				System.err.println(measures[mi].getName()+" initEst: " + initialEstimates[mi] +
						" obs: " + observed[mi]);
			}
			
			int sampleSize = 7 + 3 * measures.length; // 7+3p (Snijders 2001: 37)
			StatPack packs[] = generateSample(sampleSize, size, measures, initialEstimates, task);

			// calculate mean estimates & covariance
			double[] parameterMeans = getSampleMeans(packs);
			double[][] dmatrix = getSampleCovarianceMatrix(packs, parameterMeans);
			for(int mi=0; mi<measures.length; mi++)
			{
				System.err.println(measures[mi].getName()+" mean: " + parameterMeans[mi] +
						" var: " + dmatrix[mi][mi]);
			}
			
			// phase 2: in each iteration step within each subphase, Y(n)
			// is generated according to the current parameter value
			// $\hat{\theta}^{(n)}$ and after each step this value is updated
			// according to the formula 
			//
			// $\hat{\theta}^{(n+1)} = \hat{\theta}^{(n)} -a_nD_0^{-1}Z(n)
			// 
			// where
			//
			// Z_k(n)=P(n)u(1-Y(n))+(1-P(n))u(Y(n))-u_0.
			
			int subphaseCt = 4;
			double a_0 = 0.1; // param?
			double a_n = a_0;
			
			// aka \hat{\theta}^{(n)} and \hat{\theta}^{(n+1)}
			double[] currentEst = new double[measures.length];
			double[] nextEst = new double[measures.length];
			double[] last_z = new double[measures.length];
			for(int mi=0; mi<measures.length; mi++)
			{
				currentEst[mi] = initialEstimates[mi];
				nextEst[mi] = Double.NaN;
				last_z[mi] = Double.POSITIVE_INFINITY;
			}
			
			double[] estSum = new double[measures.length];
			int estCount = 0;
			
			for(int subphase = 1; subphase<=subphaseCt; subphase++)
			{
				if(task != null)
				{
					task.setDescription("Phase 2."+subphase+": Estimating Parameters");
					Thread.yield();
				}
				
				// reset sum/count
				for(int mi=0; mi<measures.length; mi++)
				{
					estSum[mi] = 0.0;
				}
				estCount = 0;

				// determine iteration steps per subphase
				int steps_min = (int)(Math.round(Math.pow(2.0, 4.0*(subphase-1)/3.0)*(7.0+measures.length)));
				int steps_max = steps_min + 200;
				
				int step = 0;
				while(step++ < steps_max)
				{
					if(task != null)
					{
						task.setValue(100 * step / steps_max);
						Thread.yield();
					}

					// generate Y(n) according to current params
					NetworkData nd = generateRandomNetwork(size, measures, currentEst);
					
					// update \hat{\theta}^{(n+1)}
					boolean terminateOK = true;
					for(int mi=0; mi<measures.length; mi++)
					{
						StatPack pack = new StatPack(nd, measures, currentEst);
						double z = pack.netp * pack.uyc[mi] + (1-pack.netp) * pack.uy[mi] -
							observed[mi];
						nextEst[mi] = currentEst[mi] - a_n * dmatrix[mi][mi] * z;
						
						// termination check
						if(last_z[mi] + z > 0)
						{
							terminateOK = false;
						}
						last_z[mi] = z;
					}
					
					// shift back (maybe don't need to separate currentEst/nextEst?
					for(int mi=0; mi<measures.length; mi++)
					{
						currentEst[mi] = nextEst[mi];
						estSum[mi] += currentEst[mi];
					}
					estCount++;
					
					if(terminateOK && (step>steps_min))
					{
						break;
					}
				}

				// update a_n
				a_n = a_n / 2.0;
			}
			
			// copy out last estimate
			double[] finalEst = new double[measures.length];
			for(int mi=0; mi<measures.length; mi++)
			{
				finalEst[mi] = estSum[mi]/(1.0*estCount);
			}

			// phase 3: estimate covariance by generating a big sample as
			// in phase 1 by using final estimates
			if(task != null)
			{
				task.setDescription("Phase 3: Estimating Covariance Matrix");
				Thread.yield();
			}
			int finalSampleSize = 500; // (Snijders 2001: 37)
			StatPack finalPacks[] = generateSample(finalSampleSize, size, measures, finalEst, task);

			// calculate (mean estimates &?) covariance
			// double[] finalMeans = getSampleMeans(packs);
			double[][] covMatrix = getSampleCovarianceMatrix(packs, finalEst); // 
			
			// save results
			results = new RegressionResult[measures.length];
			for(int mi=0; mi<measures.length; mi++)
			{
				results[mi] = new RegressionResult(finalEst[mi], Math.sqrt(covMatrix[mi][mi]));
			}
		}			
 		
		private void old_compute()
		{	
			// eventually bring this back as an option?
			/* evaluator = new PStarEvaluator[relCt];
			predictedTies = new Matrix[relCt];
			tieProbs = new Matrix[relCt];
			for(int ri=0; ri<relCt; ri++)
			{
				evaluator[ri] = new PStarEvaluator(nData, measures, ri);
				predictedTies[ri] = evaluator[ri].getPredictedTieMatrix();
				tieProbs[ri] = evaluator[ri].getTieProbsMatrix();
			}*/
		}
		
		protected StatPack[] generateSample(int sampleSize, int networkSize,
				NetworkMeasure[] measures, double[] parameterValues,
				AbstractTask task)
		{
			StatPack[] packs = new StatPack[sampleSize];
			for(int ni=0; ni<sampleSize; ni++)
			{
				if(task != null)
				{
					task.setValue(100 * ni / sampleSize);
					Thread.yield();
				}
				NetworkData nd = generateRandomNetwork(networkSize, measures, parameterValues);
				packs[ni] = new StatPack(nd, measures, parameterValues);
			}
			return packs;
		}
		
		protected double[] getSampleMeans(StatPack[] packs)
		{
			int mCount = packs[0].uy.length;
			double[] means = new double[mCount];
			for(int mi=0; mi<mCount; mi++)
			{
				double sum = 0.0;
				for(int ni=0; ni<packs.length; ni++)
				{
					sum += packs[ni].netp*packs[ni].uyc[mi] + 
						(1-packs[ni].netp)*packs[ni].uy[mi];
				}
				means[mi] = sum / (1.0 * packs.length);
			}
			return means;
		}
		
		protected double[][] getSampleCovarianceMatrix(StatPack[] packs, double[] means)
		{
			int mCount = packs[0].uy.length;
			double[][] dmatrix = new double[mCount][mCount];
			for(int mi=0; mi<mCount; mi++)
			{
				for(int mj=0; mj<mCount; mj++)
				{
					double sum = 0.0;
					for(int ni=0; ni<packs.length; ni++)
					{
						sum += packs[ni].netp*packs[ni].uyc[mi]*packs[ni].uyc[mj] +
							(1-packs[ni].netp)*packs[ni].uy[mi]*packs[ni].uy[mj] -
							means[mi] * means[mj];
					}
					dmatrix[mi][mj] = sum / (1.0 * packs.length);
				}
			}
			return dmatrix;
		}
		
		protected NetworkData generateRandomNetwork(int size, NetworkMeasure[] measures,
				double[] parameterValues)
		{
			double[][] values = new double[size][];
			for(int i=0; i<size; i++)
			{
				values[i] = new double[size];
			}
			for(int i=0; i<size; i++)
			{
				for(int j=0; j<size; j++)
				{
					if(i == j)
					{
						values[i][j] = 0.0;
					}
					else
					{
						values[i][j] = (random.nextDouble() > 0.5)?1.0:0.0;
					}
				}
			}
			NetworkData currentNetwork = new NetworkData(values, true);

			// double[] uY  = new double[measures.length];
			// double[] uYc = new double[measures.length];

			// boolean converged = false;
			int stepCt = 0;
			int multiplier = 10; // seems like a lot of steps?  only 5 times through network
			int steps = size * size * multiplier; 
			while(stepCt++ < steps)
			{
				if(stepCt % 50 == 0)
				{
					System.err.println("  Step "+stepCt);
					for(int mi=0; mi<measures.length; mi++)
					{
						System.err.println("  "+measures[mi].getName()+
								": "+ measures[mi].getInitialEstimate(currentNetwork));
					}
				}
				
				// choose a random dyad/triad
				int i = Math.abs(random.nextInt()) % size;
				int j = ((i + Math.abs(random.nextInt())) % (size-1)) % size;
				/*int k = ((i + Math.abs(random.nextInt())) % (size-1)) % size;
				while((k==j) || (k==i))
				{
					k = (k + 1) % size;
				}*/
				
				// pick a random updating type
				double r_step = random.nextDouble();
				if(r_step < us_prob[UPDATE_STEP_TIE])
				{
					// Metropolis-Hasting: probability of flip

					double x_ij = currentNetwork.getTieStrength(i, j);

					// calculate u(y1)
					currentNetwork.setTieStrength(i, j, 1.0);
					double[] uY1 = getStatistics(currentNetwork, measures);
					
					// calculate u(y0)
					currentNetwork.setTieStrength(i, j, 0.0);
					double[] uY0 = getStatistics(currentNetwork, measures);
										
					double productSum = 0.0;
					// System.err.println("Dij=["+x_ij+","+currentNetwork.getTieStrength(j,i)+"]");
					for(int mi=0; mi<measures.length; mi++)
					{
						productSum += parameterValues[mi] * (uY1[mi] - uY0[mi]);
						//System.err.println("tie flip effect["+measures[mi].getName()+
						//		"] -> "+parameterValues[mi] * (uYc[mi] - uY[mi]));
					}
					// double p = Math.min(1.0, Math.exp(productSum));
					double p = Math.exp(productSum) / (1.0 + Math.exp(productSum)); // inverse logit? 
					//System.err.println("p = "+p + "("+Math.exp(productSum)+")");
					if(random.nextDouble() < p)
					{
						// System.err.println("* flip *");
						// currentNetwork.setTieStrength(i, j, 1.0-x_ij);
						currentNetwork.setTieStrength(i, j, 1.0);
					}
					else
					{
						currentNetwork.setTieStrength(i, j, 0.0);
					}
				}
				else if(r_step < us_prob[UPDATE_STEP_TIE] + us_prob[UPDATE_STEP_DYAD])
				{
					
				}
				else if(r_step < us_prob[UPDATE_STEP_TIE] + us_prob[UPDATE_STEP_DYAD] + 
						us_prob[UPDATE_STEP_TRIAD])
				{
					
				}
				else
				{
					// complement update step (Metropolis-Hastings version)
					// calcuate u(y)
					double[] uY = getStatistics(currentNetwork, measures);
					
					// flip
					invertNetwork(currentNetwork);
					
					double[] uYc = getStatistics(currentNetwork, measures);
					
					// flip back
					invertNetwork(currentNetwork);
					
					double productSum = 0.0;
					for(int mi=0; mi<measures.length; mi++)
					{
						productSum += parameterValues[mi] * (uYc[mi] - uY[mi]);
					}
					// double p = Math.min(1.0, Math.exp(productSum));
					double p = Math.exp(productSum) / (1.0 + Math.exp(productSum)); // inverse logit? 
					if(random.nextDouble() < p)
					{
						invertNetwork(currentNetwork);
					}
				}
			}
			
			return currentNetwork;
		}
		

		public String getFactoryClass()
		{
			return "com.wibinet.networks.PStarBlockmodelFactory";
		}
		
		public void populateMatrices()
		{
			LabelModel aLabels = getActorLabels();
			// addMatrices("Predicted", predictedTies, aLabels, aLabels);
			// addMatrices("E(Ties)", tieProbs, aLabels, aLabels);
		}
		
		protected void populateReports()
		{
			int relCt = nData.getRelationCount();
			String[] reportHTML = new String[relCt];
			for(int ri=0; ri<relCt; ri++)
			{
				// do regression results here...
				// RegressionResult[] results = evaluator[ri].getRegressionResults();
				StringBuffer htmlBuf = new StringBuffer();
				htmlBuf.append("<HTML>\n<BODY>\n");
				htmlBuf.append("<TABLE>\n");
				
				// header row?
				htmlBuf.append("<TR>\n");
				htmlBuf.append("<TD>Variable</TD>");
				htmlBuf.append("<TD>Beta</TD>");
				htmlBuf.append("<TD>Std. Err</TD>");
				htmlBuf.append("<TD>T-Statistic</TD>");
				htmlBuf.append("<TD>Significance</TD>");
				htmlBuf.append("</TR>\n");
				
				// variables
				for(int i=0; i<results.length; i++)
				{
					htmlBuf.append("<TR>\n");
					htmlBuf.append("<TD>"+measures[i].getName()+"</TD>");
					htmlBuf.append("<TD>"+fmt.format(results[i].getParameterEstimate())+"</TD>");
					htmlBuf.append("<TD>"+fmt.format(results[i].getVariance())+"</TD>");
					htmlBuf.append("<TD>"+fmt.format(results[i].getTStatistic())+"</TD>");
					htmlBuf.append("<TD>"+fmt.format(results[i].getSignificance())+"</TD>");
					htmlBuf.append("</TR>\n");
				}
				
				// constant
				/*htmlBuf.append("<TR>\n");
				htmlBuf.append("<TD>Constant</TD>");
				htmlBuf.append("<TD>"+fmt.format(results[0].getParameterEstimate())+"</TD>");
				htmlBuf.append("<TD>"+fmt.format(results[0].getVariance())+"</TD>");
				htmlBuf.append("<TD>"+fmt.format(results[0].getTStatistic())+"</TD>");
				htmlBuf.append("<TD>"+fmt.format(results[0].getSignificance())+"</TD>");
				htmlBuf.append("</TR>\n");*/
	
				htmlBuf.append("</TABLE>\n");
				htmlBuf.append("</BODY>\n</HTML>\n");
				
				reportHTML[ri] = htmlBuf.toString();
			}
			addReports("Regression", reportHTML);
		}
		
		public double getLgModelProbability()
		{
			return Double.NaN;
		}
		
		public double getLgDataProbability()
		{
			double logSum = 0.0;
			/*for(int ei=0; ei<evaluator.length; ei++)
			{
				logSum += evaluator[ei].getLogLikelihood();
			}*/
			return logSum / Math.log(2.0);
		}
		
		public double getPredictedTieStrength(int rIdx, int i, int j)
		{
			return Double.NaN;
		}
	}
	
	public class StatPack extends Object
	{
		public double[] uy;
		public double[] uyc;
		public double netp;
		
		public StatPack(NetworkData nd, NetworkMeasure[] measures, double[] params)
		{
			// calulate theta * u(y)
			uy = getStatistics(nd, measures);
			double sum = 0.0;
			for(int mi=0; mi<measures.length; mi++)
			{
				sum += uy[mi] * params[mi];
			}
			double uy_term = Math.exp(sum);

			// flip network
			invertNetwork(nd);
			
			// calculate theta * u(1-y)
			uyc = getStatistics(nd, measures);
			sum = 0.0;
			for(int mi=0; mi<measures.length; mi++)
			{
				sum += uyc[mi] * params[mi];
			}
			double uyc_term = Math.exp(sum);
			
			// flip it back
			invertNetwork(nd);
			
			netp = uyc_term / (uy_term + uyc_term);
		}
	}
}