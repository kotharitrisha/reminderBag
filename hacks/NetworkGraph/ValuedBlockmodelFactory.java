/**
 * ValuedBlockmodelFactory.java
 *
 * Yet another poorly named class (wouldst that I had time to give
 * this more thought!).  A "valued" blockmodel is a stochastic blockmodel
 * of a network with non-negative real-valued ties.  This will presumably
 * be renamed to P1RBlockmodel soon...
 *
 * Copyright (c) 2003-2004 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.*;
import com.wibinet.app.*;
import com.wibinet.gui.*;
import com.wibinet.math.ExtendedMath;
import com.wibinet.math.Matrix;
import com.wibinet.math.Partition;

public class ValuedBlockmodelFactory extends AbstractBlockmodelFactory
{
	// these are borrowed from P1Blockmodel, but they probably
	// make sense here too...
	protected final static int IN_GROUP  = 0; // model 2
	protected final static int DIAGONAL  = 1; // model 4
	protected final static int SATURATED = 2; // model 5 
	protected final static String[] types = {
		"In-Group", "Diagonal", "Saturated"
	};
	
	// rho estimation methods
	protected final static int FIXED = 0;
	protected final static int SEARCH = 1;
	protected final static int SWEEP = 2;
	protected final static String[] methods = {
		"Fixed", "Search", "Sweep"
	};

	// blockmodel properties
	protected final static String TYPE = "Type";
	
	// annealer properties
	protected final static String STARTING_TEMP = "StartingTemp";
	protected final static String ENDING_TEMP = "EndingTemp";
	protected final static String TEMP_DECAY = "TempDecay";
	protected final static String PROB_MUTATE = "ProbMutate";
	
	// rho estimation properties
	protected final static String RHO_METHOD = "RhoMethod";
	protected final static String RUNS = "Runs";
	protected final static String FIXED_VALUE = "FixedValue";
	protected final static String SEARCH_FACTOR = "SearchFactor";
	protected final static String SEARCH_TOLERANCE = "SearchTolerance";
	protected final static String SWEEP_MIN = "SweepMin";
	protected final static String SWEEP_MAX = "SweepMax";
	protected final static String SWEEP_STEPS = "SweepSteps";

	private final static java.text.NumberFormat fmt = java.text.NumberFormat.getInstance();
	static
	{
		fmt.setMaximumFractionDigits(6);
	}
	
	protected Hashtable props;

	public ValuedBlockmodelFactory()
	{
		this.props = new Hashtable();
    
    // string values?
		props.put(TYPE, new Integer(SATURATED));
		props.put(STARTING_TEMP, new Double(50.0));
		props.put(ENDING_TEMP, new Double(0.2));
		props.put(TEMP_DECAY, new Double(0.02));
		props.put(PROB_MUTATE, new Double(0.25));
		
		props.put(RHO_METHOD, new Integer(FIXED));
		props.put(RUNS, new Integer(1));
		props.put(FIXED_VALUE, new Double(0.08));
		props.put(SEARCH_FACTOR, new Double(0.0));
		props.put(SEARCH_TOLERANCE, new Double(0.01));
		props.put(SWEEP_MIN, new Double(0.0));
		props.put(SWEEP_MAX, new Double(1.0));
		props.put(SWEEP_STEPS, new Integer(10));
	}
	
	public Blockmodel newInstance(NetworkData nData, Partition p)
	{
		// set current props from factory?
		Model model = new Model(nData, p);
		setProperties(model, props);
		return model;
	}
	
	public boolean edit(Blockmodel _model)
	{
		Model model = (Model)_model;
		EditDialog ed = new EditDialog(model);
		ed.setVisible(true);
		if(ed.isCancelled())
		{
			return false;
		}
		
		// remember for next time...
		props.put(TYPE, new Integer(model.getType()));
		
		props.put(STARTING_TEMP, new Double(model.getStartingTemp()));
		props.put(ENDING_TEMP, new Double(model.getEndingTemp()));
		props.put(TEMP_DECAY, new Double(model.getTempDecay()));
		props.put(PROB_MUTATE, new Double(model.getMutateProbability()));
		
		props.put(RHO_METHOD, new Integer(model.getRhoMethod()));
		props.put(RUNS, new Integer(model.getNumRuns()));
		props.put(FIXED_VALUE, new Double(model.getFixedValue()));
		props.put(SEARCH_FACTOR, new Double(model.getSearchFactor()));
		props.put(SEARCH_TOLERANCE, new Double(model.getSearchTolerance()));
		props.put(SWEEP_MIN, new Double(model.getSweepMin()));
		props.put(SWEEP_MAX, new Double(model.getSweepMax()));
		props.put(SWEEP_STEPS, new Integer(model.getSweepSteps()));
		
		return true;
	}
	
	public Hashtable getProperties(Blockmodel model)
	{
		return props;
	}
	
	public void setProperties(Blockmodel _model, Hashtable props)
	{
		Model model = (Model)_model;
		model.setType(getStringOrInt(props, TYPE));
		model.setStartingTemp(getStringOrDouble(props, STARTING_TEMP));
		model.setEndingTemp(getStringOrDouble(props, ENDING_TEMP));
		model.setTempDecay(getStringOrDouble(props, TEMP_DECAY));
		model.setMutateProbability(getStringOrDouble(props, PROB_MUTATE));
		model.setRhoMethod(getStringOrInt(props, RHO_METHOD));
		model.setNumRuns(getStringOrInt(props, RUNS));
		model.setFixedValue(getStringOrDouble(props, FIXED_VALUE));
		model.setSearchFactor(getStringOrDouble(props, SEARCH_FACTOR));
		model.setSearchTolerance(getStringOrDouble(props, SEARCH_TOLERANCE));
		model.setSweepMin(getStringOrDouble(props, SWEEP_MIN));
		model.setSweepMax(getStringOrDouble(props, SWEEP_MAX));
		model.setSweepSteps(getStringOrInt(props, SWEEP_STEPS));
	}
	
	// these seems like unnecessary hacks, and if they're
	// that fundamental, they should be in AbstractBlockmodelFactory
	protected double getStringOrDouble(Hashtable props, String key)
	{
		Object o = props.get(key);
		if(o instanceof String)
		{
			return Double.parseDouble((String)o);
		}
		else
		{
			return ((Double)o).doubleValue();
		}
	}
	
	protected int getStringOrInt(Hashtable props, String key)
	{
		Object o = props.get(key);
		if(o instanceof String)
		{
			return Integer.parseInt((String)o);
		}
		else
		{
			return ((Integer)o).intValue();
		}
	}

	public String getType()
	{
		return "Valued";
	}

	protected class EditDialog extends JDialog implements ActionListener, ItemListener
	{
		protected Model model;
		protected boolean cancelled;
		
		protected JComboBox cbType;
    
		protected DoubleNumberField dfStartingTemp;
    protected DoubleNumberField dfEndingTemp;
    protected DoubleNumberField dfTempDecay;
    protected DoubleNumberField dfProbMutate;
		
		// rho estimation
		protected JPanel pRhoOptions;
		protected CardLayout clRhoOptions;
		protected JComboBox cbRhoMethod;
		protected WholeNumberField wnfNumRuns;
		protected DoubleNumberField dfFixedRho;
		protected DoubleNumberField dfSearchFactor;
		protected DoubleNumberField dfSearchTolerance;
		protected DoubleNumberField dfSweepMin;
		protected DoubleNumberField dfSweepMax;
		protected WholeNumberField wnfSweepSteps;
		
		public EditDialog(Model model)
		{
			super((JFrame)null);
      setModal(true);
			setTitle("Valued Blockmodel Options");
			getContentPane().setLayout(new BorderLayout());
			this.model = model;
			this.cancelled = false;

      Box cPanel = new Box(BoxLayout.Y_AXIS);
      
			JPanel pType = new JPanel(new BorderLayout());
			cbType = new JComboBox(types);
			cbType.setEditable(false);
			cbType.setSelectedIndex(model.type);
			pType.add(BorderLayout.WEST, new JLabel("Blockmodel type: "));
			pType.add(BorderLayout.CENTER, cbType);
      cPanel.add(pType);
			
			// annealing schedule panel
			JPanel pAnnealing = new JPanel(new GridLayout(4, 1));
			pAnnealing.setBorder(BorderFactory.createTitledBorder("Annealing Schedule"));
      JPanel pStartingTemp = new JPanel(new BorderLayout());
      dfStartingTemp = new DoubleNumberField(model.getStartingTemp(), 8);
      pStartingTemp.add(BorderLayout.WEST, new JLabel("Starting Temperature: "));
      pStartingTemp.add(BorderLayout.CENTER, dfStartingTemp);
      pAnnealing.add(pStartingTemp);
      JPanel pEndingTemp = new JPanel(new BorderLayout());
      dfEndingTemp = new DoubleNumberField(model.getEndingTemp(), 8);
      pEndingTemp.add(BorderLayout.WEST, new JLabel("Ending Temperature: "));
      pEndingTemp.add(BorderLayout.CENTER, dfEndingTemp);
      pAnnealing.add(pEndingTemp);
      JPanel pTempDecay = new JPanel(new BorderLayout());
      dfTempDecay = new DoubleNumberField(model.getTempDecay(), 8);
      pTempDecay.add(BorderLayout.WEST, new JLabel("Temperature Decay: "));
      pTempDecay.add(BorderLayout.CENTER, dfTempDecay);
      pAnnealing.add(pTempDecay);
      JPanel pProbMutate = new JPanel(new BorderLayout());
      dfProbMutate = new DoubleNumberField(model.getMutateProbability(), 8);
      pProbMutate.add(BorderLayout.WEST, new JLabel("p(Mutate): "));
      pProbMutate.add(BorderLayout.CENTER, dfProbMutate);
      pAnnealing.add(pProbMutate);
			cPanel.add(pAnnealing);
			
			JPanel pRhoEstimation = new JPanel(new BorderLayout());
			pRhoEstimation.setBorder(BorderFactory.createTitledBorder("Rho Estimation"));
			JPanel pRhoCommon = new JPanel(new GridLayout(2, 1));
			JPanel pRhoMethod = new JPanel(new BorderLayout());
			cbRhoMethod = new JComboBox(methods);
			cbRhoMethod.setEditable(false);
			cbRhoMethod.setSelectedIndex(model.rhoMethod);
			cbRhoMethod.addItemListener(this);
			pRhoMethod.add(BorderLayout.WEST, new JLabel("Method: "));
			pRhoMethod.add(BorderLayout.CENTER, cbRhoMethod);
			pRhoCommon.add(pRhoMethod);
      JPanel pNumRuns = new JPanel(new BorderLayout());
      wnfNumRuns = new WholeNumberField(model.getNumRuns(), 8);
      pNumRuns.add(BorderLayout.WEST, new JLabel("# of Runs: "));
      pNumRuns.add(BorderLayout.CENTER, wnfNumRuns);
      pRhoCommon.add(pNumRuns);
			pRhoEstimation.add(BorderLayout.NORTH, pRhoCommon);
			
			this.clRhoOptions = new CardLayout();
			this.pRhoOptions = new JPanel(clRhoOptions);
			JPanel pFixedMethodRoot = new JPanel(new BorderLayout());
			JPanel pFixedVal = new JPanel(new BorderLayout());
			dfFixedRho = new DoubleNumberField(model.getFixedValue(), 8);
			pFixedVal.add(new JLabel("Value: "), BorderLayout.WEST);
			pFixedVal.add(dfFixedRho, BorderLayout.CENTER);
			pFixedMethodRoot.add(pFixedVal, BorderLayout.NORTH);
			pRhoOptions.add(pFixedMethodRoot, methods[FIXED]);
			
			JPanel pSearchMethodRoot = new JPanel(new BorderLayout());
			JPanel pSearchMethod = new JPanel(new GridLayout(2, 1));
			JPanel pSearchFactor = new JPanel(new BorderLayout());
			dfSearchFactor = new DoubleNumberField(model.getSearchFactor(), 8);
			pSearchFactor.add(new JLabel("Factor: "), BorderLayout.WEST);
			pSearchFactor.add(dfSearchFactor, BorderLayout.CENTER);
			pSearchMethod.add(pSearchFactor);
			JPanel pSearchTolerance = new JPanel(new BorderLayout());
			dfSearchTolerance = new DoubleNumberField(model.getSearchTolerance(), 8);
			pSearchTolerance.add(new JLabel("Tolerance: "), BorderLayout.WEST);
			pSearchTolerance.add(dfSearchTolerance, BorderLayout.CENTER);
			pSearchMethod.add(pSearchTolerance);
			pSearchMethodRoot.add(pSearchMethod, BorderLayout.NORTH);
			pRhoOptions.add(pSearchMethodRoot, methods[SEARCH]);
			
			JPanel pSweepMethodRoot = new JPanel(new BorderLayout());
			JPanel pSweepMethod = new JPanel(new GridLayout(3, 1));
			JPanel pSweepMin = new JPanel(new BorderLayout());
			dfSweepMin = new DoubleNumberField(model.getSweepMin(), 8);
			pSweepMin.add(new JLabel("Min: "), BorderLayout.WEST);
			pSweepMin.add(dfSweepMin, BorderLayout.CENTER);
			pSweepMethod.add(pSweepMin);
			JPanel pSweepMax = new JPanel(new BorderLayout());
			dfSweepMax = new DoubleNumberField(model.getSweepMax(), 8);
			pSweepMax.add(new JLabel("Max: "), BorderLayout.WEST);
			pSweepMax.add(dfSweepMax, BorderLayout.CENTER);
			pSweepMethod.add(pSweepMax);
			JPanel pSweepSteps = new JPanel(new BorderLayout());
			wnfSweepSteps = new WholeNumberField(model.getSweepSteps(), 8);
			pSweepSteps.add(new JLabel("Steps: "), BorderLayout.WEST);
			pSweepSteps.add(wnfSweepSteps, BorderLayout.CENTER);
			pSweepMethod.add(pSweepSteps);
			pSweepMethodRoot.add(pSweepMethod, BorderLayout.NORTH);
			pRhoOptions.add(pSweepMethodRoot, methods[SWEEP]);
			pRhoEstimation.add(pRhoOptions, BorderLayout.CENTER);
			cPanel.add(pRhoEstimation);

			getContentPane().add(BorderLayout.CENTER, cPanel);
			JOkCancelPanel ocp = new JOkCancelPanel();
			ocp.addActionListener(this);
			getContentPane().add(BorderLayout.SOUTH, ocp);
			pack();
			clRhoOptions.first(pRhoOptions);
		}
		
		public void actionPerformed(ActionEvent ae)
		{
			String cmd = ae.getActionCommand();
			if(JOkCancelPanel.OK.equals(cmd))
			{
				model.setType(cbType.getSelectedIndex());
				model.setStartingTemp(dfStartingTemp.getValue());
				model.setEndingTemp(dfEndingTemp.getValue());
				model.setTempDecay(dfTempDecay.getValue());
				model.setMutateProbability(dfProbMutate.getValue());
				model.setRhoMethod(cbRhoMethod.getSelectedIndex());
				model.setNumRuns(wnfNumRuns.getValue());
				model.setFixedValue(dfFixedRho.getValue());
				model.setSearchFactor(dfSearchFactor.getValue());
				model.setSearchTolerance(dfSearchTolerance.getValue());
				model.setSweepMin(dfSweepMin.getValue());
				model.setSweepMax(dfSweepMax.getValue());
				model.setSweepSteps(wnfSweepSteps.getValue());
				cancelled = false;
			}
			else
			{
				cancelled = true;
			}
			setVisible(false);
			dispose();
		}
		
		public boolean isCancelled()
		{
			return cancelled;
		}
		
		public void itemStateChanged(ItemEvent e)
		{
			// only on 'selection' event
			if(e.getStateChange() == ItemEvent.SELECTED)
			{
				clRhoOptions.show(pRhoOptions,
					methods[cbRhoMethod.getSelectedIndex()]);
			}
		}
	}
	
	private static boolean ranOnce = false;
	protected final static int ALPHA = 0;
	protected final static int BETA  = 1;
	public class Model extends AbstractBlockmodel
	{
		// estimated parameters
		protected Matrix[] theta;
		protected Matrix[] theta_sigma;
		protected double[] rho;
		protected Matrix[] alphaBeta;
		
		// underlying distribution?
		protected Matrix[] dyadProbs;
		protected Matrix[] zScores;
		protected double[][] out_sigma; // not so sure about these
		protected double[][] in_sigma;
		
		// "utility" variables
		protected double[] resolution;
		protected int[] invertedPIndices;

		// estimation parameters
		protected int type;

		protected double startingTemp;
		protected double endingTemp;
		protected double tempDecay;
		protected double probMutate;

		protected int rhoMethod;
		protected int numRuns;
		protected double fixedValue;
		protected double searchFactor;
		protected double searchTolerance;
		protected double sweepMin;
		protected double sweepMax;
		protected int sweepSteps;

		public Model(NetworkData nData, Partition p)
		{
			super(nData, p);
			
			this.alphaBeta = new Matrix[0];
			this.theta = new Matrix[0];
			this.theta_sigma = new Matrix[0];
			this.rho = new double[0];

			this.dyadProbs = new Matrix[0];
			this.zScores = new Matrix[0];
			this.out_sigma = new double[0][];
			this.in_sigma = new double[0][];
			this.resolution = new double[0];
			this.invertedPIndices = new int[0];
			
			this.type = IN_GROUP;

			this.startingTemp = 50.0;
			this.endingTemp = 0.5;
			this.tempDecay = 0.02;
			this.probMutate = 0.25;
			
			this.rhoMethod = FIXED;
			this.numRuns = 10;
			this.fixedValue = 0.4;
			this.searchFactor = 0.0;
			this.searchTolerance = 0.01;
			this.sweepMin = 0.0;
			this.sweepMax = 1.0;
			this.sweepSteps = 10;
		}
		
		public int getType()
		{
			return type;
		}
		
		public void setType(int type)
		{
			this.type = type;
		}
		
		public double getStartingTemp()
		{
			return startingTemp;
		}
		
		public void setStartingTemp(double startingTemp)
		{
			this.startingTemp = startingTemp;
		}
		
		public double getEndingTemp()
		{
			return endingTemp;
		}
		
		public void setEndingTemp(double endingTemp)
		{
			this.endingTemp = endingTemp;
		}
		
		public double getTempDecay()
		{
			return tempDecay;
		}
		
		public void setTempDecay(double tempDecay)
		{
			this.tempDecay = tempDecay;
		}
		
		public double getMutateProbability()
		{
			return probMutate;
		}
		
		public void setMutateProbability(double probMutate)
		{
			this.probMutate = probMutate;
		}
		
		public int getRhoMethod()
		{
			return rhoMethod;
		}
		
		public void setRhoMethod(int rhoMethod)
		{
			this.rhoMethod = rhoMethod;
		}
		
		public int getNumRuns()
		{
			return numRuns;
		}
		
		public void setNumRuns(int numRuns)
		{
			this.numRuns = numRuns;
		}
		
		public double getFixedValue()
		{
			return fixedValue;
		}
		
		public void setFixedValue(double fixedValue)
		{
			this.fixedValue = fixedValue;
		}
		
		public double getSearchFactor()
		{
			return searchFactor;
		}
		
		public void setSearchFactor(double searchFactor)
		{
			this.searchFactor = searchFactor;
		}
		
		public double getSearchTolerance()
		{
			return searchTolerance;
		}
		
		public void setSearchTolerance(double searchTolerance)
		{
			this.searchTolerance = searchTolerance;
		}
		
		public double getSweepMin()
		{
			return sweepMin;
		}
		
		public void setSweepMin(double sweepMin)
		{
			this.sweepMin = sweepMin;
		}
		
		public double getSweepMax()
		{
			return sweepMax;
		}
		
		public void setSweepMax(double sweepMax)
		{
			this.sweepMax = sweepMax;
		}
		
		public int getSweepSteps()
		{
			return sweepSteps;
		}
		
		public void setSweepSteps(int sweepSteps)
		{
			this.sweepSteps = sweepSteps;
		}
		
		public void compute(AbstractTask task)
		{
			// initialize matrices
			int relCt = nData.getRelationCount();
			int numBlocks = p.getPartitionCount();
			int nSize = nData.getSize();
			// double dyadCt = 0.5 * (nSize * (nSize - 1.0));

			theta = new Matrix[relCt];
			theta_sigma = new Matrix[relCt];
			rho = new double[relCt];
			alphaBeta = new Matrix[relCt];

			dyadProbs = new Matrix[relCt];
			zScores = new Matrix[relCt];
			in_sigma = new double[relCt][];
			out_sigma = new double[relCt][];
			resolution = new double[relCt];

			// make an inverted partition indices array (maybe this should be a method
			// in NetworkData or Partition?).  incidentally, just a quick reminder
			// about how this array works.  invertedPIndices[i] tells you the index
			// in the pIndices[] array of the partition that actor i is in.  so, if
			// pIndices[] looks like {3, 7, 4, 2....}, and actor i is in partition 4,
			// then invertedPIndices[4] = 2, and pIndices[invertedPIndices[i]] = 4.
			// to iterate through actors by partition, i think it's best to do a
			// major loop through pIndices[], get the actors for the partition, and
			// then do a minor loop through the actors.
			invertedPIndices = new int[nSize];
			for(int i=0; i<nSize; i++)
			{
				invertedPIndices[i] = -6741; // make sure it's something unlikely
			}
			for(int pi=0; pi<pIndices.length; pi++)
			{
				invertedPIndices[pIndices[pi]] = pi;
			}
	
			for(int rIdx=0; rIdx<relCt; rIdx++)
			{
				// a little progress perhaps?
				if(task != null)
				{
					if(nData instanceof VisualNetworkData)
					{
						task.setDescription("Scaling '" + 
							((VisualNetworkData)nData).getName()+"'");
					}
					else
					{
						task.setDescription("Scaling Relation " + (rIdx+1));
					}
					Thread.yield();
				}
				
				// initialize some matrices
				dyadProbs[rIdx] = new Matrix(nSize);
				zScores[rIdx] = new Matrix(nSize);
				in_sigma[rIdx] = new double[nSize];
				out_sigma[rIdx] = new double[nSize];
				alphaBeta[rIdx] = new Matrix(nSize, 2);
				theta[rIdx] = new Matrix(numBlocks);
				theta_sigma[rIdx] = new Matrix(numBlocks);
			
				// Here's where (and how) I'm dealing with zero values, vis-a-vis
				// an assumption about reporting resolution.  The basic assumption is
				// that there's some rounding going on, and differences smaller than
				// some "resolution" value are not reported.  Let MNZV correspond to
				// the "minimum non-zero value".  Assume that any reported value really
				// corresponds to the range of values:
				//
				//   [value - resolution, value + resolution]
				//
				// So, if 10 was the minimum non-zero reported value, we can reasonably
				// assume that a reported 30 might correspond to the range [25, 35].
				// As such, resolution = MNZV/2.  That said, point estimates for zero
				// must be treated differently, because the range [-5, 5] is invalid
				// for a positive-valued function.  So, zero-reported values should
				// correspond to the range [0, resolution], and should correspond to
				// the point estimate resolution/2 = MNZV/4.

				double mnzv = Double.MAX_VALUE;
				for(int i=0; i<nSize; i++)
				{
					for(int j=i+1; j<nSize; j++)
					{
						double x_ij = nData.getTieStrength(rIdx, i, j);
						double x_ji = nData.getTieStrength(rIdx, j, i);
						if((x_ij < mnzv) && (x_ij > 0.0))
						{
							mnzv = x_ij;
						}
						if((x_ji < mnzv) && (x_ji > 0.0))
						{
							mnzv = x_ji;
						}
					}
				}
				resolution[rIdx] = mnzv / 2.0;

				// estimate theta means & deviations -- this seems to work
				for(int pi=0; pi<numBlocks; pi++)
				{
					int partitionI = pIndices[pi];
					int[] actorsI = p.getObjectIndices(partitionI);
					for(int pj=0; pj<numBlocks; pj++)
					{
						int partitionJ = pIndices[pj];
						int[] actorsJ = p.getObjectIndices(partitionJ);
						double blockSize = bSize[pi] * bSize[pj];
						if(pi == pj)
						{
							blockSize = bSize[pi] * (bSize[pi]-1);
						}
						
						// calulate mean of logs of tie values
						double qSum = 0.0;
						for(int ai=0; ai<actorsI.length; ai++)
						{
							int actor_i = actorsI[ai];
							for(int aj=0; aj<actorsJ.length; aj++)
							{
								int actor_j = actorsJ[aj];
								if(actor_i != actor_j)
								{
									double x_ij = Math.max(resolution[rIdx]/2.0,
										nData.getTieStrength(rIdx, actor_i, actor_j));
									qSum += Math.log(x_ij);
								}
							}
						}
						theta[rIdx].values[pi][pj] = qSum / blockSize;
						
						// deviation
						double qSqSum = 0.0;
						for(int ai=0; ai<actorsI.length; ai++)
						{
							int actor_i = actorsI[ai];
							for(int aj=0; aj<actorsJ.length; aj++)
							{
								int actor_j = actorsJ[aj];
								if(actor_i != actor_j)
								{
									double x_ij = Math.max(resolution[rIdx]/2.0,
										nData.getTieStrength(rIdx, actor_i, actor_j));
									double q_dev_ij = Math.log(x_ij) - theta[rIdx].values[pi][pj];
									
									qSqSum += q_dev_ij * q_dev_ij;
								}
							}
						}
						theta_sigma[rIdx].values[pi][pj] = Math.sqrt(qSqSum / blockSize);
					}
				}
				
				// in/out sigmas -- based on deviations from block means?
				for(int i=0; i<nSize; i++)
				{
					int bi = invertedPIndices[p.getPartition(i)];

					double outSqSum = 0.0;
					double inSqSum = 0.0;

					for(int j=0; j<nSize; j++)
					{
						if(i != j)
						{
							int bj = invertedPIndices[p.getPartition(j)];

							double out_mean = theta[rIdx].values[bi][bj];
							double in_mean = theta[rIdx].values[bj][bi];
							
							double out_dev = Math.log(Math.max(resolution[rIdx]/2.0,
								nData.getTieStrength(rIdx, i, j))) - out_mean;
							double in_dev = Math.log(Math.max(resolution[rIdx]/2.0,
								nData.getTieStrength(rIdx, j, i))) - in_mean;
							outSqSum += out_dev * out_dev;
							inSqSum += in_dev * in_dev;
						}
					}
					out_sigma[rIdx][i] = Math.sqrt(outSqSum/(nSize-1.0));
					in_sigma[rIdx][i] = Math.sqrt(inSqSum/(nSize-1.0));
					//System.out.println("outs["+i+"]="+fmt.format(out_sigma[rIdx][i])+
					//	" ins["+i+"]="+fmt.format(in_sigma[rIdx][i]));
				}
				
				// estimate rho
				switch(rhoMethod)
				{
					case FIXED:
						// hmm...this should be per relation, huh...
						Results rFixed = optimize_rho(fixedValue, rIdx, numRuns);
						rFixed.apply(Model.this, rIdx);
						break;
					
					case SEARCH:
						// search for rho, and then compute one more time with that rho
						double rho_est = estimate_rho(rIdx, 0.0, 1.0, searchTolerance);
						compute_step(rho_est, rIdx);
						break;
						
					case SWEEP:
						Application.handleNonFatalThrowable(new Throwable("Method Not Implemented"));
						break;
				}
			}
		}
		
		protected double estimate_rho(int rIdx, double rho_0, double rho_1, double limit)
		{
			// System.out.println("estimate_rho("+rIdx+", "+rho_0+", "+rho_1+", "+limit+")");
			
			// are we close enough yet
			if(Math.abs(rho_1 - rho_0) < limit)
			{
				return (rho_0 + rho_1)/2.0;
			}
			
			// if not, sample between
			double rho_2 = (2.0 * rho_0 + rho_1) / 3.0;
			double rho_3 = (rho_0 + 2.0 * rho_1) / 3.0;
			
			Results r2 = optimize_rho(rho_2, rIdx, numRuns);
			r2.apply(Model.this, rIdx);
			double prob_2 = getLgDataProbability();
			// System.out.println(""+rho_2+", "+prob_2);
			Results r3 = optimize_rho(rho_3, rIdx, numRuns);
			r3.apply(Model.this, rIdx);
			double prob_3 = getLgDataProbability();
			// System.out.println(""+rho_3+", "+prob_3);

			if(prob_2 == prob_3)
			{
				return estimate_rho(rIdx, rho_2, rho_3, limit);
			}
			if(prob_2 < prob_3)
			{
				return estimate_rho(rIdx, rho_2, rho_1, limit);
			}
			else
			{
				return estimate_rho(rIdx, rho_0, rho_3, limit);
			}
		}
		
		public Results optimize_rho(double testRho, int rIdx, int tries)
		{
			double bestProb = Double.NEGATIVE_INFINITY;
			Results bestResults = null;
			for(int si=0; si<tries; si++)
			{
				compute_step(testRho, rIdx);
				double prob = getLgDataProbability();
				// System.out.println(""+testRho+", "+prob);
				if(prob > bestProb)
				{
					bestProb = prob;
					bestResults = new Results(Model.this, rIdx);
				}
			}
			return bestResults;
		}
		
		public void compute_step2(double testRho, int rIdx)
		{
			int nSize = nData.getSize();
			rho[rIdx] = testRho;

			// initialize alphas & betas
			resetAlphaBeta(rIdx);
			
			// init some data structures
			double[] outSum = new double[nSize];
			double[] inSum = new double[nSize];
			double scale = 0.0001;
			double scaleDecay = .03;
			
			// iterate
			while(true)
			{
				// figure out all z-scores
				int[] worstPair = populateZScores(rIdx);
				int worst_i = worstPair[0];
				int worst_j = worstPair[1];
				if(worst_i == -1)
				{
					System.out.println("z[21][12]="+zScores[rIdx].values[21][12]);
					break;
				}
				if(Math.abs(zScores[rIdx].values[worst_i][worst_j]) < 0.00001)
				{
					System.out.println("z[21][12]="+zScores[rIdx].values[21][12]);
					break;
				}
				System.out.print("z["+worst_i+"]["+worst_j+"]=");
				System.out.println(fmt.format(zScores[rIdx].values[worst_i][worst_j]));
				
				// get margins
				for(int i=0; i<nSize; i++)
				{
					outSum[i] = 0.0;
					inSum[i] = 0.0;
					for(int j=0; j<nSize; j++)
					{
						outSum[i] += zScores[rIdx].values[i][j];
						inSum[i] += zScores[rIdx].values[j][i];
					}
					outSum[i] *= out_sigma[rIdx][i] / nSize;
					inSum[i] *= in_sigma[rIdx][i] / nSize;
					// if(Double.isNaN(outSum[i])) System.out.println("outSum["+i+"]=NaN");
					// if(Double.isNaN(inSum[i])) System.out.println("inSum["+i+"]=NaN");
					System.out.println("outSum["+i+"]="+fmt.format(outSum[i])+
						" inSum["+i+"]="+fmt.format(inSum[i]));
				}
				
				// adjust alpha & beta by margins?
				for(int i=0; i<nSize; i++)
				{
					alphaBeta[rIdx].values[i][ALPHA] -= scale*outSum[i];
					alphaBeta[rIdx].values[i][BETA] -= scale*inSum[i];
				}
				
				// normalize
				normalizeAlphaBeta(rIdx);
				
				// update scale
				scale *= (1.0 - scaleDecay);
			}

			double newProb = populateProbabilities(rIdx);
		}
		
		public void compute_step(double testRho, int rIdx)
		{
			int nSize = nData.getSize();
			rho[rIdx] = testRho;
			
			// initialize alphas & betas
			resetAlphaBeta(rIdx);
			
			// iterate 
			Random random = new Random();
			double temp = startingTemp;
			double oldProb = Double.NaN;
			double modelProb = populateProbabilities(rIdx);
			double bestProb = modelProb;
			// double oldZ = Double.NaN;
			// double modelZ = Double.NaN;
			// double bestZ = Double.MAXIMUM_VALUE;
			Results bestResults = new Results(Model.this, rIdx);
			
			// scale management
			double scale = 1.0;
			double scaleDecay = .03;
			int missLimit = 200;
			int missCtr = 0;
			
			// while((Math.abs(modelProb - oldProb) > tolerance) || (temp > endingTemp))
			while(temp >= endingTemp)
			{
				// give progress a chance to update
				Thread.yield();
				
				// System.out.println("prob = " + modelProb);
				// System.out.println("temp = " + temp);
				
				// find worst offender
				int change_i = -1;
				int change_j = -1;
				int[] changePair = populateZScores(rIdx, 0.9); // variable ignore rate?
				if(random.nextDouble() > probMutate)
				{
					/*double worstProb = Double.POSITIVE_INFINITY;
					for(int i=0; i<nSize; i++)
					{
						for(int j=i+1; j<nSize; j++)
						{
							if(dyadProbs[rIdx].values[i][j] < worstProb)
							{
								worstProb = dyadProbs[rIdx].values[i][j];
								change_i = i;
								change_j = j;
							}
						}
					}
					System.out.println("p(D["+change_i+"]["+change_j+"])=" + worstProb);*/
					change_i = changePair[0];
					change_j = changePair[1];
					double mean_ij = getThetaMean(rIdx, change_i, change_j);
					double mean_ji = getThetaMean(rIdx, change_j, change_i);
					System.out.println("z["+change_i+"]["+change_j+"])=" + 
						fmt.format(zScores[rIdx].values[change_i][change_j]) +
						" mean="+fmt.format(mean_ij));
					System.out.println("z["+change_j+"]["+change_i+"])=" + 
						fmt.format(zScores[rIdx].values[change_j][change_i]) +
						" mean="+fmt.format(mean_ji));
				}
				else
				{
					// make sure this isn't a diagonal...
					change_i = random.nextInt(nSize);
					change_j = (change_i + random.nextInt(nSize-1)) % nSize;
					// System.out.println("p(D["+change_i+"]["+change_j+"]) - r");
				}
				// double changeProb = dyadProbs[rIdx].values[change_i][change_j];
				
				// perhaps the change amount should be scaled by the stdev in each block?
				// this is after all changing the expected mean value of ties per block
				int bi = invertedPIndices[p.getPartition(change_i)];
				int bj = invertedPIndices[p.getPartition(change_j)];
				double blockScale = Math.sqrt(theta_sigma[rIdx].values[bi][bj] *
					theta_sigma[rIdx].values[bi][bj]);
				blockScale = 1.0; 
				
				// what direction should this change
				// to the extent that z_scores are high, we should be sending more alpha and
				// beta
				double dir_ij = sign(zScores[rIdx].values[change_i][change_j]);
				double dir_ji = sign(zScores[rIdx].values[change_j][change_i]);
				
				// randomly change 
				double d_ai = dir_ij * blockScale * scale * Math.abs(random.nextGaussian());
				double d_aj = dir_ji * blockScale * scale * Math.abs(random.nextGaussian());
				double d_bi = dir_ji * blockScale * scale * Math.abs(random.nextGaussian());
				double d_bj = dir_ij * blockScale * scale * Math.abs(random.nextGaussian());
				
				/*System.out.println("sigma_ij="+fmt.format(theta_sigma[rIdx].values[bi][bj])+
					" sigma_ji="+fmt.format(theta_sigma[rIdx].values[bj][bi]));
				System.out.println("  alpha["+change_i+"]="+alphaBeta[rIdx].values[change_i][ALPHA]+
					" + " + d_ai);
				System.out.println("  alpha["+change_j+"]="+alphaBeta[rIdx].values[change_j][ALPHA]+
					" + " + d_aj);
				System.out.println("  beta["+change_i+"]="+alphaBeta[rIdx].values[change_i][BETA]+
					" + " + d_bi);
				System.out.println("  beta["+change_j+"]="+alphaBeta[rIdx].values[change_j][BETA]+
					" + " + d_bj);*/

				alphaBeta[rIdx].values[change_i][ALPHA] += d_ai;
				alphaBeta[rIdx].values[change_j][ALPHA] += d_aj;
				alphaBeta[rIdx].values[change_i][BETA] += d_bi;
				alphaBeta[rIdx].values[change_j][BETA] += d_bj;
				normalizeAlphaBeta(rIdx);
				calculateSigmas(rIdx);
				double newProb = populateProbabilities(rIdx);

				// allow this change if it improves the net probability or
				// if it makes it no worse than the current temperature
				if((newProb - modelProb < -temp) && (!Double.isInfinite(modelProb)))
				{
					// change it back
					alphaBeta[rIdx].values[change_i][ALPHA] -= d_ai;
					alphaBeta[rIdx].values[change_j][ALPHA] -= d_aj;
					alphaBeta[rIdx].values[change_i][BETA] -= d_bi;
					alphaBeta[rIdx].values[change_j][BETA] -= d_bj;
					normalizeAlphaBeta(rIdx);
					calculateSigmas(rIdx);
					missCtr++;
				}
				else
				{
					if(!ranOnce) System.out.println(""+temp+", "+modelProb+", "+
						scale+", "+missCtr);

					// scale management
					if(missCtr > missLimit)
					{
						scale *= (1.0 - scaleDecay);
					}
					missCtr = 0;
					
					// is this the best we've seen so far?
					if(newProb > bestProb)
					{
						bestProb = newProb;
						bestResults = new Results(Model.this, rIdx);
						if(!ranOnce) System.out.println(" *** bestProb = " + bestProb);
					}
					
					// update temperature and whatnot
					oldProb = modelProb;
					modelProb = newProb;
					temp *= (1.0 - tempDecay);
				
					// check for non-convergence
					// maybe this should count a few times or something
					// and eventually return infinity?  i'm not sure
					// that this should ever not converge like this...
					//if(Double.isInfinite(modelProb))
					if(false)
					{
						// System.out.println("*** Non-Convergence for rho = " + rho[rIdx]);
						// rho[rIdx] = random.nextDouble();
						// System.out.println("*** New rho = " + rho[rIdx]);
						resetAlphaBeta(rIdx);
						calculateSigmas(rIdx);
						modelProb = populateProbabilities(rIdx);
						temp = startingTemp;
						oldProb = Double.NaN;
					}
				}
			}
		
			// finish up
			bestResults.apply(Model.this, rIdx);
			if(!ranOnce) System.out.println("done - prob = " + getLgDataProbability());
			ranOnce = true;
		}
		
		protected void resetAlphaBeta(int rIdx)
		{
			int nSize = nData.getSize();
			for(int i=0; i<nSize; i++)
			{
				// start with a (zero/random?) value
				alphaBeta[rIdx].setValueAt(i, ALPHA, 0.0);
				alphaBeta[rIdx].setValueAt(i, BETA, 0.0);
				
				// there's no need to check for empty rows & columns,
				// because this model (currently?) doesn't deal with
				// zero valued relations.
			}
		}
		
		protected void normalizeAlphaBeta(int rIdx)
		{
			int nSize = nData.getSize();
			double alphaSum = 0.0;
			double betaSum = 0.0;
			for(int i=0; i<nSize; i++)
			{
				double alpha = alphaBeta[rIdx].values[i][ALPHA];
				double beta = alphaBeta[rIdx].values[i][BETA];
				alphaSum += alpha;
				betaSum += beta;
			}
			double alphaAdjust = alphaSum / nSize;
			double betaAdjust = betaSum / nSize;
			for(int i=0; i<nSize; i++)
			{
				double alpha = alphaBeta[rIdx].values[i][ALPHA];
				double beta = alphaBeta[rIdx].values[i][BETA];
				alphaBeta[rIdx].values[i][ALPHA] -= alphaAdjust;
				alphaBeta[rIdx].values[i][BETA] -= betaAdjust;
			}
		}
		
		public String getFactoryClass()
		{
			return "com.wibinet.networks.ValuedBlockmodelFactory";
		}

		protected void populateMatrices()
		{
			LabelModel aLabels = getActorLabels();
			LabelModel pLabels = getPartitionLabels();
			LabelModel abLabels = new ABLabelModel();

			// addMatrices("Pred", predictedTies, aLabels, aLabels);
			addMatrices("\u03B1/\u03B2", alphaBeta, aLabels, abLabels);
			addMatrices("Theta", theta, pLabels, pLabels);
			addMatrices("Sigma", theta_sigma, pLabels, pLabels);
			// addMatrices("Resid", thetaResid, aLabels, aLabels);
		}

		protected void populateReports()
		{
			int relCt = nData.getRelationCount();
			String[] reportHTML = new String[relCt];
			for(int rIdx=0; rIdx<relCt; rIdx++)
			{
				// do regression results here...
				StringBuffer htmlBuf = new StringBuffer();
				htmlBuf.append("<HTML>\n<BODY>\n");
				
				// these aren't XHTML compliant BR tags!
				// but they seem to work better than XHTML compliant tags
				// with the Java HTML renderer...
				htmlBuf.append("<font size=\"+1\"><b>Basic Statistics</b></font><br>\n");
				htmlBuf.append("<b>rho:</b> " + fmt.format(rho[rIdx]) + "<br>\n");
				htmlBuf.append("</BODY>\n</HTML>\n");
				
				reportHTML[rIdx] = htmlBuf.toString();
			}
			addReports("Statistics", reportHTML);
		}
		
		// why is this not implemented somewhere?
		private double sign(double x)
		{
			if(x == 0.0) return 0.0;
			if(x < 0.0) return -1.0;
			return 1.0;
		}
		
		protected double getAdjustedRho(int rIdx, double alpha)
		{
			int nSize = nData.getSize();
			double sumSurplus = 0.0;
			for(int i=0; i<nSize; i++)
			{
				for(int j=i+i; j<nSize; j++)
				{
					double x_ij = Math.max(resolution[rIdx]/2.0, nData.getTieStrength(rIdx, i, j));
					double x_ji = Math.max(resolution[rIdx]/2.0, nData.getTieStrength(rIdx, j, i));
					double lx_ij = Math.log(x_ij);
					double lx_ji = Math.log(x_ji);
					double mean_ij = getThetaMean(rIdx, i, j);
					double mean_ji = getThetaMean(rIdx, j, i);
					
					// do we see more than the expected intensity of ties?
					double surplus_ij = sign(lx_ij - mean_ij);
					double surplus_ji = sign(lx_ji - mean_ji);
					sumSurplus += surplus_ij;
					sumSurplus += surplus_ji;
				}
			}
			sumSurplus /= (nSize * (nSize-1.0));
			if(sumSurplus < 0)
			{
				return rho[rIdx] * (1.0 + alpha * sumSurplus);
			}
			else
			{
				return rho[rIdx] + alpha * (1.0-rho[rIdx]) * sumSurplus;
			}
		}
		
		// ever so slightly inefficient not to do this in pairs...
		protected double getThetaMean(int rIdx, int i, int j)
		{
			int bi = invertedPIndices[p.getPartition(i)];
			int bj = invertedPIndices[p.getPartition(j)];
			double alpha_i = alphaBeta[rIdx].getValueAt(i, ALPHA);
			double alpha_j = alphaBeta[rIdx].getValueAt(j, ALPHA);
			double beta_i = alphaBeta[rIdx].getValueAt(i, BETA);
			double beta_j = alphaBeta[rIdx].getValueAt(j, BETA);
			
			return theta[rIdx].values[bi][bj] + ((alpha_i + beta_j) +
				rho[rIdx] * (alpha_j + beta_i)) / (1.0 + rho[rIdx]);
		}
		
		// this should return the product of the 
		// cumulative normal distribution over the indeterminate range of the
		// log of x_ij and x_ji 
		protected double getDyadProbability(int rIdx, int i, int j)
		{
			int bi = invertedPIndices[p.getPartition(i)];
			int bj = invertedPIndices[p.getPartition(j)];

			double blockSize = bSize[bi] * bSize[bj];
			if(bi == bj)
			{
				blockSize = bSize[bi] * (bSize[bi]-1);
			}

			double mean_ij = getThetaMean(rIdx, i, j);
			double mean_ji = getThetaMean(rIdx, j, i);

			double x_ij = nData.getTieStrength(rIdx, i, j);
			double prob_ij = Double.NaN;
			if(x_ij == 0.0)
			{
				x_ij = resolution[rIdx]/2.0;
				prob_ij = ExtendedMath.cumulativeNormal(mean_ij, // mean
					theta_sigma[rIdx].values[bi][bj], // std. deviation
					Math.log(resolution[rIdx])); 
			}
			else
			{
				double pij_max = ExtendedMath.cumulativeNormal(mean_ij,
					theta_sigma[rIdx].values[bi][bj],
					Math.log(x_ij + resolution[rIdx]));
				double pij_min = ExtendedMath.cumulativeNormal(mean_ij,
					theta_sigma[rIdx].values[bi][bj],
					Math.log(x_ij - resolution[rIdx]));
				prob_ij = pij_max - pij_min;
			}
			
			double x_ji = nData.getTieStrength(rIdx, j, i);
			double prob_ji = Double.NaN;
			if(x_ji == 0.0)
			{
				x_ji = resolution[rIdx]/2.0;
				prob_ji = ExtendedMath.cumulativeNormal(mean_ji,
					theta_sigma[rIdx].values[bj][bi], // should this be theta_{bj}{bi}?
					Math.log(resolution[rIdx]*2.0)); 
			}
			else
			{
				double pji_max = ExtendedMath.cumulativeNormal(mean_ji,
					theta_sigma[rIdx].values[bj][bi], // should this be theta_{bj}{bi}?
					Math.log(x_ji + resolution[rIdx]));
				double pji_min = ExtendedMath.cumulativeNormal(mean_ji,
					theta_sigma[rIdx].values[bj][bi], // should this be theta_{bj}{bi}?
					Math.log(x_ji - resolution[rIdx]));
				prob_ji = pji_max - pji_min;
			}
			/*if(prob_ij == 0.0)
			{
				System.out.println("ij_fault ("+i+", "+j+"): " + fmt.format(x_ij) +
					" z_ij="+fmt.format((Math.log(x_ij)-mean_ij)/theta_sigma[rIdx].values[bi][bj]) +
					" bs["+bi+"]["+bj+"]="+blockSize+
					" sd="+fmt.format(theta_sigma[rIdx].values[bi][bj])+
					" mean="+fmt.format(mean_ij));
			}
			if(prob_ji == 0.0)
			{
				System.out.println("ji_fault ("+i+", "+j+"): " + fmt.format(x_ji) +
					" z_ji="+fmt.format((Math.log(x_ji)-mean_ji)/theta_sigma[rIdx].values[bj][bi]) +
					" bs["+bi+"]["+bj+"]="+blockSize+
					" sd="+fmt.format(theta_sigma[rIdx].values[bj][bi])+
					" mean="+fmt.format(mean_ji));
			}*/
			double prob = prob_ij * prob_ji;
			/*if(prob == 0.0)
			{
				System.out.println("lx_ij=" + Math.log(x_ij) +" prob_ij="+prob_ij +
					" mean_ij="+mean_ij+" z_ij="+((Math.log(x_ij)-mean_ij)/theta_sigma[rIdx].values[bi][bj]));
				System.out.println("lx_ji=" + Math.log(x_ji) +" prob_ji="+prob_ji +
					" mean_ji="+mean_ji+" z_ji="+((Math.log(x_ji)-mean_ji)/theta_sigma[rIdx].values[bj][bi]));
			}*/
			return prob;
		}
		
		// need to readjust this for 'type'
		public double getLgModelProbability()
		{
			int nSize = nData.getSize();
			int numBlocks = p.getPartitionCount();
			double d = getOptimalPrecision(nSize * (nSize-1));
			double paramCt = Double.NaN;
			switch(type)
			{
				case IN_GROUP:
					paramCt = 1.0;
					break;
				
				case DIAGONAL:
					paramCt = 1.0 * numBlocks;
					break;
				
				case SATURATED:
					paramCt = (numBlocks - 1.0) * (numBlocks - 1.0);
					break;
			}
			return -(nData.getRelationCount() * (2.0 * (nSize - 1) + paramCt + 3.0) * d);
		}
		
		protected double populateProbabilities(int rIdx)
		{
			int nSize = nData.getSize();
			boolean _printed = false;

			double lgProb = 0.0;
			for(int i=0; i<nSize; i++)
			{
				for(int j=i+1; j<nSize; j++)
				{
					// not sure if these should be in log form...
					double prob = getDyadProbability(rIdx, i, j);
					dyadProbs[rIdx].values[i][j] = prob;
					dyadProbs[rIdx].values[j][i] = prob;
					lgProb += ExtendedMath.lg(prob);
					if(Double.isInfinite(lgProb) && (!_printed))
					{
						int bi = invertedPIndices[p.getPartition(i)];
						int bj = invertedPIndices[p.getPartition(j)];
						double lx_ij = Math.log(Math.max(resolution[rIdx]/2.0, nData.getTieStrength(rIdx, i, j)));
						double lx_ji = Math.log(Math.max(resolution[rIdx]/2.0, nData.getTieStrength(rIdx, j, i)));
						double mean_ij = getThetaMean(rIdx, i, j);
						double mean_ji = getThetaMean(rIdx, j, i);
						double sd_ij = theta_sigma[rIdx].values[bi][bj];
						double sd_ji = theta_sigma[rIdx].values[bj][bi];
						/*System.out.println("went infinite at ("+i+", "+j+") => " + dyadProbs[rIdx].values[i][j]);
						System.out.println("  z_ij = " + fmt.format((lx_ij-mean_ij)/sd_ij) + 
							" mean_ij="+fmt.format(mean_ij));
						System.out.println("  z_ji = " + fmt.format((lx_ji-mean_ji)/sd_ji) +
							" mean_ji="+fmt.format(mean_ji));*/
						_printed = true;
					}
					if(ExtendedMath.lg(prob) > 0.0)
					{
						System.out.println("at D("+i+","+j+") prob = " + prob);
						System.exit(1);
					}
				}
			}
			return lgProb;
		}
		
		protected int[] populateZScores(int rIdx)
		{
			return populateZScores(rIdx, 0.0);
		}
		
		protected int[] populateZScores(int rIdx, double ignoreRate)
		{
			Random random = new Random();
			int[] worstPair = new int[2];
			worstPair[0] = -1;
			worstPair[1] = -1;
			double worstScore = 0.0;
		
			int nSize = nData.getSize();
			for(int i=0; i<nSize; i++)
			{
				int bi = invertedPIndices[p.getPartition(i)];
				for(int j=i+1; j<nSize; j++)
				{
					int bj = invertedPIndices[p.getPartition(j)];

					double lx_ij = Math.log(Math.max(resolution[rIdx]/2.0, nData.getTieStrength(rIdx, i, j)));
					double lx_ji = Math.log(Math.max(resolution[rIdx]/2.0, nData.getTieStrength(rIdx, j, i)));

					double mean_ij = getThetaMean(rIdx, i, j);
					double mean_ji = getThetaMean(rIdx, j, i);
					double sd_ij = theta_sigma[rIdx].values[bi][bj];
					double sd_ji = theta_sigma[rIdx].values[bj][bi];

					double z_ij = (lx_ij-mean_ij)/sd_ij;
					double z_ji = (lx_ji-mean_ji)/sd_ji;
					if(sd_ij == 0.0) z_ij = 0.0;
					if(sd_ji == 0.0) z_ji = 0.0;
					zScores[rIdx].values[i][j] = z_ij;
					zScores[rIdx].values[j][i] = z_ji;
					if(random.nextDouble() > ignoreRate)
					{
						if(Math.abs(z_ij) > worstScore)
						{
							worstScore = Math.abs(z_ij);
							worstPair[0] = i;
							worstPair[1] = j;
						}
						if(Math.abs(z_ji) > worstScore)
						{
							worstScore = Math.abs(z_ji);
							worstPair[0] = j;
							worstPair[1] = i;
						}
					}
				}
			}
			return worstPair;
		}
		
		protected void calculateSigmas(int rIdx)
		{
			int numBlocks = p.getPartitionCount();
			for(int pi=0; pi<numBlocks; pi++)
			{
				int partitionI = pIndices[pi];
				int[] actorsI = p.getObjectIndices(partitionI);
				for(int pj=0; pj<numBlocks; pj++)
				{
					int partitionJ = pIndices[pj];
					int[] actorsJ = p.getObjectIndices(partitionJ);
					double blockSize = bSize[pi] * bSize[pj];
					if(pi == pj)
					{
						blockSize = bSize[pi] * (bSize[pi]-1);
					}

					double qSqSum = 0.0;
					for(int ai=0; ai<actorsI.length; ai++)
					{
						int actor_i = actorsI[ai];
						for(int aj=0; aj<actorsJ.length; aj++)
						{
							int actor_j = actorsJ[aj];

							if(actor_i != actor_j)
							{
								double theta_mean_ij = getThetaMean(rIdx, actor_i, actor_j);
								double x_ij = Math.max(resolution[rIdx]/2.0,
									nData.getTieStrength(rIdx, actor_i, actor_j));
								double q_dev_ij = Math.log(x_ij) - theta_mean_ij;
								
								qSqSum += q_dev_ij * q_dev_ij;
							}
						}
					}
					theta_sigma[rIdx].values[pi][pj] = Math.sqrt(qSqSum / blockSize);
				}
			}
		}
		
		// these only work assuming relational independence
		public double getLgDataProbability()
		{
			int relCt = nData.getRelationCount();
			double lgProb = 0.0;
			for(int rIdx=0; rIdx<relCt; rIdx++)
			{
				lgProb += getLgDataProbability(rIdx);
			}
			return lgProb;
		}
		
		public double getLgDataProbability(int rIdx)
		{
			int nSize = nData.getSize();
			double lgProb = 0.0;
			for(int i=0; i<nSize; i++)
			{
				for(int j=i+1; j<nSize; j++)
				{
					lgProb += ExtendedMath.lg(dyadProbs[rIdx].values[i][j]);
				}
			}
			return lgProb;
		}

		public double getPredictedTieStrength(int rIdx, int i, int j)
		{
			return Double.NaN;
		}
	}

	protected class Results
	{
		protected Matrix alphaBeta;
		protected double rho;
		
		public Results(Model model, int rIdx)
		{
			alphaBeta = new Matrix(model.alphaBeta[rIdx]);
			rho = model.rho[rIdx];
		}
		
		public void apply(Model model, int rIdx)
		{
			model.alphaBeta[rIdx] = new Matrix(alphaBeta);
			model.rho[rIdx] = rho;
			model.populateProbabilities(rIdx);
		}
	}
	
	protected class ABLabelModel extends LabelModel
	{
	  public int getLabelCount()
	  {
	  	return 2;
	  }
	  
	  public String getLabel(int idx)
	  {
	  	return (idx==0) ? "\u03B1" : "\u03B2";
	  }
	  
	  public void setLabel(int idx, String label)
	  {
	  }
	  
	  public boolean isLabelEditable(int idx)
	  {
	  	return false;
	  }
	}
}