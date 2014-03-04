package com.wibinet.networks;

import java.awt.BorderLayout;
// import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
// import java.awt.event.ItemEvent;
// import java.awt.event.ItemListener;
import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apfloat.*;

// import com.wibinet.app.Application;
import com.wibinet.gui.AbstractTask;
import com.wibinet.gui.DefaultLabelModel;
import com.wibinet.gui.DoubleNumberField;
import com.wibinet.gui.JOkCancelPanel;
import com.wibinet.gui.LabelModel;
// import com.wibinet.gui.WholeNumberField;
import com.wibinet.math.ExtendedMath;
import com.wibinet.math.Matrix;
import com.wibinet.math.Partition;

public class P1RBlockmodelFactory extends AbstractBlockmodelFactory
{
	// these are borrowed from P1Blockmodel, but they probably
	// make sense here too...
	protected final static int IN_GROUP  = 0; // model 2
	protected final static int DIAGONAL  = 1; // model 4
	protected final static int SATURATED = 2; // model 5 
	protected final static String[] types = {
		"In-Group", "Diagonal", "Saturated"
	};

	// blockmodel properties
	protected final static String TYPE = "Type";
	
	// annealer properties
	protected final static String STARTING_TEMP = "StartingTemp";
	protected final static String ENDING_TEMP = "EndingTemp";
	protected final static String TEMP_DECAY = "TempDecay";
	protected final static String PROB_MUTATE = "ProbMutate";
	protected final static String PARAMETER_GAIN = "ParameterGain";
	
	protected Hashtable props;

	private final static java.text.NumberFormat fmt = java.text.NumberFormat.getInstance();
	static
	{
		fmt.setMaximumFractionDigits(6);
	}
	private final static Apfloat ap_zero = new Apfloat("0.0", ExtendedMath.AP_PRECISION);
	
	public P1RBlockmodelFactory()
	{
		this.props = new Hashtable();
    
		// string values?
		props.put(TYPE, new Integer(SATURATED));
		props.put(STARTING_TEMP, new Double(5000.0));
		props.put(ENDING_TEMP, new Double(100.0));
		props.put(TEMP_DECAY, new Double(0.02));
		props.put(PROB_MUTATE, new Double(0.1));
		props.put(PARAMETER_GAIN, new Double(3.0));
	}
	
	public Blockmodel newInstance(NetworkData nData, Partition p)
	{
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
		model.setParameterGain(getStringOrDouble(props, PARAMETER_GAIN));
	}

	public String getType()
	{
		return "P1R";
	}
	protected class EditDialog extends JDialog implements ActionListener
	{
		protected Model model;
		protected boolean cancelled;
		
		protected JComboBox cbType;
    
		protected DoubleNumberField dfStartingTemp;
		protected DoubleNumberField dfEndingTemp;
		protected DoubleNumberField dfTempDecay;
		protected DoubleNumberField dfProbMutate;
		protected DoubleNumberField dfParameterGain;
		
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
			JPanel pAnnealing = new JPanel(new GridLayout(5, 1));
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
			JPanel pParameterGain = new JPanel(new BorderLayout());
			dfParameterGain = new DoubleNumberField(model.getParameterGain(), 8);
			pParameterGain.add(BorderLayout.WEST, new JLabel("parameterGain: "));
			pParameterGain.add(BorderLayout.CENTER, dfParameterGain);
			pAnnealing.add(pParameterGain);
			cPanel.add(pAnnealing);
			
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
				model.setType(cbType.getSelectedIndex());
				model.setStartingTemp(dfStartingTemp.getValue());
				model.setEndingTemp(dfEndingTemp.getValue());
				model.setTempDecay(dfTempDecay.getValue());
				model.setMutateProbability(dfProbMutate.getValue());
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
	}

	protected final static int ALPHA = 0;
	protected final static int BETA  = 1;

	protected final static int STEP_RANDOM = 0;
	protected final static int STEP_Z = 1;
	
	protected final static double MNZV_FACTOR = 0.0001; // should be low in (0, 1)?
	
	private static boolean ranOnce = false;
	private static Apfloat ap_log2e = ApfloatMath.log(new Apfloat("2.0", ExtendedMath.AP_PRECISION));
	private static Apfloat ap_one = new Apfloat("1.0", ExtendedMath.AP_PRECISION);
	private static Apfloat ap_onehalf = new Apfloat("0.5", ExtendedMath.AP_PRECISION);

	public class Model extends AbstractBlockmodel
	{
		// estimated tie values 
		protected Matrix[] chunk_min;
		protected double[] chunk_width;
		
		// estimated parameters
		protected Matrix[] theta;
		protected Matrix[] theta_sigma;
		protected Matrix[] alphaBeta;
		
		// underlying distribution?
		protected Matrix[] lgDyadProbs;
		protected Matrix[] zScores;
		
		// extra(s)
		protected Matrix[] contribution; // these two should really be individual matrices, but...
		protected Matrix[] actorProbs;
		protected Matrix[] pSelfForm;
		protected Matrix[] actorEntropy;
		
		// "utility" variables
		protected double[] mnzv;
		protected double[] resBits;
		protected int[] invertedPIndices;

		// estimation parameters
		protected int type;

		protected double startingTemp;
		protected double endingTemp;
		protected double tempDecay;
		protected double probMutate;
		protected double parameterGain;

		protected boolean estimateAlphaBeta = false;
		protected boolean estimateActorProbs = false;

		public Model(NetworkData nData, Partition p)
		{
			super(nData, p);
			
			this.chunk_min = new Matrix[0];
			this.chunk_width = new double[0];
			
			this.alphaBeta = new Matrix[0];
			this.theta = new Matrix[0];
			this.theta_sigma = new Matrix[0];
			
			this.contribution = new Matrix[0];
			this.actorProbs = new Matrix[0];
			this.pSelfForm = new Matrix[0];
			this.actorEntropy = new Matrix[0];
			
			this.lgDyadProbs = new Matrix[0];
			this.zScores = new Matrix[0];
			this.mnzv = new double[0];
			this.resBits = new double[0];
			
			this.invertedPIndices = new int[0];
			
			// I don't think we should initialize factory settable params 
			// here, since the factory will initialize these anyway.  To
			// act like these will be initialized here could be confusing...
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
		
		public double getParameterGain()
		{
			return parameterGain;
		}
		
		public void setParameterGain(double parameterGain)
		{
			this.parameterGain = parameterGain;
		}

		public void compute(AbstractTask task)
		{
			// initialize matrices
			int relCt = nData.getRelationCount();
			int numBlocks = p.getPartitionCount();
			int nSize = nData.getSize();
			// double dyadCt = 0.5 * (nSize * (nSize - 1.0));

			chunk_min = new Matrix[relCt];
			chunk_width = new double[relCt];
			
			theta = new Matrix[relCt];
			theta_sigma = new Matrix[relCt];
			alphaBeta = new Matrix[relCt];
			
			contribution = new Matrix[relCt];
			actorProbs = new Matrix[relCt];
			pSelfForm = new Matrix[relCt];
			actorEntropy = new Matrix[relCt];
			
			lgDyadProbs = new Matrix[relCt];
			zScores = new Matrix[relCt];
			mnzv = new double[relCt];
			resBits = new double[relCt];

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
			/*for(int i=0; i<pIndices.length; i++)
			{
				System.out.print("actors in '"+p.getPartitionName(i)+"'");
				int[] actorsI = p.getObjectIndices(i);
				for(int j=0; j<actorsI.length; j++)
				{
					System.out.print(" "+nData.getActor(actorsI[j]).getName());
				}
				System.out.println();
			}*/
	
			for(int rIdx=0; rIdx<relCt; rIdx++)
			{
				// a little progress perhaps?
				if(task != null)
				{
					if(nData instanceof VisualNetworkData)
					{
						task.setDescription("Estimating '" + 
							((VisualNetworkData)nData).getName()+"'");
					}
					else
					{
						task.setDescription("Estimating Relation " + (rIdx+1));
					}
					Thread.yield();
				}
				
				// initialize some matrices
				lgDyadProbs[rIdx] = new Matrix(nSize);
				zScores[rIdx] = new Matrix(nSize);
				alphaBeta[rIdx] = new Matrix(nSize, 2);
				theta[rIdx] = new Matrix(numBlocks);
				theta_sigma[rIdx] = new Matrix(numBlocks);

				contribution[rIdx] = new Matrix(numBlocks, 1);
				actorProbs[rIdx] = new Matrix(nSize, numBlocks);
				pSelfForm[rIdx] = new Matrix(nSize, 1);
				actorEntropy[rIdx] = new Matrix(nSize, 1);
				
				chunk_min[rIdx] = new Matrix(nSize);

				// The first thing to do here is to generate matrices of the
				// 'estimated' observed tie values.  I'm not sure what the best
				// way to do this rounding is, but I think it is fair to say that
				// the 'optimal precision' should reflect the number of possible
				// observed values.  As such, it seems reasonable to divide the
				// (logged) range of observed values into ceil(2^precision) chunks,
				// and define estimated values as the entire breadth of the chunk
				// that the observed value is located in.
				//
				// In order to determine the range, the first thing we need to do
				// is track the minimum non-zero value (mnzv) and maximum value
				// (maxv) for this relation.

				mnzv[rIdx] = Double.MAX_VALUE;
				double maxv = 0.0;
				for(int i=0; i<nSize; i++)
				{
					for(int j=i+1; j<nSize; j++)
					{
						double x_ij = nData.getTieStrength(rIdx, i, j);
						double x_ji = nData.getTieStrength(rIdx, j, i);
						if((x_ij < mnzv[rIdx]) && (x_ij > 0.0))
						{
							mnzv[rIdx] = x_ij;
						}
						if((x_ji < mnzv[rIdx]) && (x_ji > 0.0))
						{
							mnzv[rIdx] = x_ji;
						}
						if(x_ij > maxv)
						{
							maxv = x_ij;
						}
						if(x_ji > maxv)
						{
							maxv = x_ji;
						}
					}
				}

				// Now figure out how many resolution bits we need, and figure out
				// how many chunks exactly we're talking about
				resBits[rIdx] = getOptimalPrecision(nSize * nSize);
				double logMin = Math.log(mnzv[rIdx]);
				double logMax = Math.log(maxv);
				double chunk_count = Math.ceil(Math.pow(2.0, resBits[rIdx]));
				chunk_width[rIdx] = (logMax-logMin) / chunk_count;
				
				// the 'base value' is the estimated value of zero ties, as well as the
				// reference point for determining all other estimated values.
				double base_value = logMin - 1.5 * chunk_width[rIdx];
				
				// okay...this is mebbe gettin' a little arbitrary...
				double exp_base_value = Math.exp(base_value) / (nSize * nSize * 1.0);
				
				// now go back and populate chunk_min matrix
				for(int i=0; i<nSize; i++)
				{
					for(int j=i+1; j<nSize; j++)
					{
						double x_ij = nData.getTieStrength(rIdx, i, j);
						double x_ji = nData.getTieStrength(rIdx, j, i);
						
						if(x_ij == 0.0)
						{
							chunk_min[rIdx].values[i][j] = base_value;
						}
						else
						{
							double lg_ij = Math.log(x_ij);
							double chunk_num = Math.ceil((lg_ij-base_value)/chunk_width[rIdx]);
							chunk_min[rIdx].values[i][j] = base_value + chunk_num * chunk_width[rIdx];
						}
						if(x_ji == 0.0)
						{
							chunk_min[rIdx].values[j][i] = base_value;
						}
						else
						{
							double lg_ji = Math.log(x_ji);
							double chunk_num = Math.ceil((lg_ji-base_value)/chunk_width[rIdx]);
							chunk_min[rIdx].values[j][i] = base_value + chunk_num * chunk_width[rIdx];
						}
					}
				}

				// estimate theta means & deviations
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
						double firstVal = Double.NaN;
						boolean allSame = true;
						for(int ai=0; ai<actorsI.length; ai++)
						{
							int actor_i = actorsI[ai];
							for(int aj=0; aj<actorsJ.length; aj++)
							{
								int actor_j = actorsJ[aj];
								if(actor_i != actor_j)
								{
									double lg_ij = chunk_min[rIdx].values[actor_i][actor_j] + 
										0.5 * chunk_width[rIdx];
									qSum += lg_ij;
									
									// avoiding rounding errors...if all values are
									// same, note here
									if(Double.isNaN(firstVal))
									{
										firstVal = lg_ij;
									}
									else if(allSame && (lg_ij != firstVal))
									{
										allSame = false;
									}
								}
							}
						}
						
						double sigma = Double.NaN;
						if(allSame)
						{
							theta[rIdx].values[pi][pj] = firstVal;
							sigma = 0.0;
						}
						else
						{
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
										double lg_ij = chunk_min[rIdx].values[actor_i][actor_j] + 
											0.5 * chunk_width[rIdx];
										double q_dev_ij = lg_ij - 
											theta[rIdx].values[pi][pj];
										qSqSum += q_dev_ij * q_dev_ij;
									}
								}
							}
							sigma = Math.sqrt(qSqSum / blockSize);
						}
						
						// make this actually conform to precision?
						// this is a pretty crappy way of doing this right now...
						theta_sigma[rIdx].values[pi][pj] = Math.max(sigma, exp_base_value);
					}
				}

				// now fit alpha/beta params...
				if(estimateAlphaBeta)
				{
					compute_step(rIdx);
				}
				else
				{
					// do we need to calculate sigmas again?  
					// shouldn't since alpha's and beta's are zero
					// calculateSigmas(rIdx);

					// still need to populate probabilities one time
					populateProbabilities(rIdx, task);
				}
				
				// (optionally) determine probabilities that each
				// actor is a member of a given partition (form)
				if(estimateActorProbs)
				{
					if(task != null)
					{
						task.setDescription("Estimating Actor Probabilities");
					}
					for(int i=0; i<nSize; i++)
					{
						String actorLabel = nData.getActor(i).getName();
						if(task != null)
						{
							task.setValue((i * 100) / nSize);
						}
						Apfloat[] likelihood = new Apfloat[numBlocks];
						Apfloat sumL = ap_zero;
						int myBlock = invertedPIndices[p.getPartition(i)];
						for(int bi=0; bi<numBlocks; bi++)
						{
							// a bit hacky, sir...
							boolean pValid = true;

							double sum_lg_pi = 0.0;
							for(int j=0; j<nSize; j++)
							{
								if(i != j)
								{
									int bj = invertedPIndices[p.getPartition(j)];
									double p_ij = getDyadProbability(rIdx, i, j, bi, bj);
									double p_ji = getDyadProbability(rIdx, j, i, bj, bj);
									double l_ij = Double.NaN;
									if(p_ij > 0.0)
									{
										l_ij = Math.log(p_ij);
									}
									else
									{
										Apfloat ap_ij = getDyadProbabilityAP(rIdx, i, j, bi, bj);
										if(ap_ij.equals(ap_zero))
										{
											pValid = false;
											break;
										}
										else
										{
											Apfloat al_ij = ApfloatMath.log(ap_ij);
											l_ij = al_ij.doubleValue();
										}
									}
									double l_ji = Double.NaN;
									if(p_ji > 0.0)
									{
										l_ji = Math.log(p_ji);
									}
									else
									{
										Apfloat ap_ji = getDyadProbabilityAP(rIdx, j, i, bj, bi);
										if(ap_ji.equals(ap_zero))
										{
											pValid = false;
											break;
										}
										else
										{
											Apfloat al_ji = ApfloatMath.log(ap_ji);
											l_ji = al_ji.doubleValue();
										}
									}
									
									sum_lg_pi += l_ij + l_ji;
								}
							}
							
							// controversy?  i think i may be able to justify dividing
							// this by nSize -- something about independent observations
							// or whatnot...
							sum_lg_pi /= (nSize * 1.0); // 2.0 would mean indepednent ties, 1.0 = independent dyads?

							// basically, if this is unmeasurably small, treat it as
							// zero.  this is faster if we never invoke the Apfloat
							// stuff, but certainly less accurate, and we may end up
							// concluding that some actors are unlikely to fit in
							// any partition...
							Apfloat li = ap_zero;
							if(pValid)
							{
								li = ApfloatMath.exp(new Apfloat(sum_lg_pi, ExtendedMath.AP_PRECISION));
							}
							likelihood[bi] = li;
							sumL = sumL.add(li);
						}
						
						// normalize
						if(sumL.compareTo(ap_zero) > 0)
						{
							// System.out.println("non-zero sum for '"+actorLabel+"'");
							for(int bi=0; bi<numBlocks; bi++)
							{
								likelihood[bi] = likelihood[bi].divide(sumL);
								if(likelihood[bi].doubleValue() > 0.01)
								{
									System.out.println("p("+actorLabel+" el "+p.getPartitionName(bi)+")=" + 
											fmt.format(likelihood[bi].doubleValue()));
								}
							}
						}
						else
						{
							System.out.println("zero sum for actor '"+actorLabel+"'");
						}
						
						// record pSelfForm

						// track which one was the best (debug)
						// int bestBlock = -1;
						double highestProb = Double.NEGATIVE_INFINITY;
						for(int bi=0; bi<numBlocks; bi++)
						{
							double p = likelihood[bi].doubleValue();
							actorProbs[rIdx].values[i][bi] = p;
							/*if(p > highestProb)
							{
								highestProb = p;
								bestBlock = bi;
							}*/
						}
						pSelfForm[rIdx].values[i][0] = actorProbs[rIdx].values[i][myBlock];
						
						// calculate entropy
						double entropy = 0.0;
						for(int bi=0; bi<numBlocks; bi++)
						{
							double p = actorProbs[rIdx].values[i][bi];
							if(p > 0.0)
							{
								entropy -= p * Math.log(p);
							}
						}
						actorEntropy[rIdx].values[i][0] = entropy;
						
						/*
						// now do a "pound-for-pound" comparison betweeen best & original
						System.out.println("myBlock = "+p.getPartitionName(myBlock));
						System.out.println("bestBlock = "+p.getPartitionName(bestBlock));
						for(int j=0; j<nSize; j++)
						{
							if(i != j)
							{
								int bj = invertedPIndices[p.getPartition(j)];
								double p_orig = getDyadProbability(rIdx, i, j, myBlock, bj);
								double l_orig = Double.NaN;
								if(p_orig > 0.0)
								{
									l_orig = Math.log(p_orig);
								}
								else
								{
									Apfloat ap_orig = getDyadProbabilityAP(rIdx, i, j, myBlock, bj);
									l_orig = ApfloatMath.log(ap_orig).doubleValue();
								}
								double p_best = getDyadProbability(rIdx, i, j, bestBlock, bj);
								double l_best = Double.NaN;
								if(p_best > 0.0)
								{
									l_best = Math.log(p_best);
								}
								else
								{
									Apfloat ap_best = getDyadProbabilityAP(rIdx, i, j, bestBlock, bj);
									l_best = ApfloatMath.log(ap_best).doubleValue();
								}
								//System.out.println(nData.getActor(j).getName()+" o " +
								//		fmt.format(l_orig)+" b " + fmt.format(l_best) +
								//		" d " + fmt.format(l_best-l_orig));
								//System.out.println(nData.getActor(j).getName()+" o " +
								//		l_orig+" b " + l_best +
								//		" d " + (l_best-l_orig));
							}
						}*/
					}
				}
				
				// determine contribution
				// estimate theta means & deviations
				for(int pi=0; pi<numBlocks; pi++)
				{
					int partitionI = pIndices[pi];
					int[] actorsI = p.getObjectIndices(partitionI);
					double _contrib = 0.0;
					for(int i=0; i<actorsI.length; i++)
					{
						int actor_i = actorsI[i];
						for(int actor_j=0; actor_j<nSize; actor_j++)
						{
							if(actor_i != actor_j)
							{
								_contrib += lgDyadProbs[rIdx].values[actor_i][actor_j];
								_contrib += lgDyadProbs[rIdx].values[actor_j][actor_i];
							}
						}
					}
					contribution[rIdx].values[pi][0] = _contrib;
				}
			}
		}
		
		public void compute_step(int rIdx)
		{
			int nSize = nData.getSize();
			
			// initialize alphas & betas
			resetAlphaBeta(rIdx);
			
			// iterate 
			Random random = new Random();
			double temp = startingTemp;
			double oldProb = Double.NaN;
			double bestProb = Double.NEGATIVE_INFINITY;
			double modelProb = populateProbabilities(rIdx);
			Results bestResults = null;
			
			// scale management
			double baseScale = 1.0;
			double scaleDecay = .08;
			double scaleDeepDecay = 0.20; // when errors seem abonrmally large
			double deepThreshhold = 10.0; // abnormally large means that net probability
				// change / Temparature is bigger than this threshhold
			double scale = baseScale;
			int missLimit = 10;
			int missCtr = 0;
			
			while(temp >= endingTemp)
			{
				// flush for debugging
				System.out.flush();
				
				// give progress a chance to update
				Thread.yield();
				
				// are we 'lost'?  if there's a good place to go back to, do so
				if((missCtr > missLimit) && (bestResults != null))
				{
					bestResults.apply(Model.this, rIdx);
					missCtr = 0;
					
					// do a temp drop
					temp *= (1.0 - tempDecay);
					System.out.println("Going back to best yet T:" + fmt.format(temp));
				}
				
				//System.out.println("T: " + fmt.format(temp) + " P: " + fmt.format(modelProb) +
				//		" bestP: " + fmt.format(bestProb));
				
				// need these to manage scale of changes
				double[] ab_sigmas = getAlphaBetaSigmas(rIdx);
				double a_scale = (ab_sigmas[ALPHA]>0.001)?(parameterGain * ab_sigmas[ALPHA]):scale;
				double b_scale = (ab_sigmas[BETA]>0.001)?(parameterGain * ab_sigmas[BETA]):scale;
				
				// pick a set of changes
				int[] changeIndices;
				double[][] changeLevels;
				int step_type = STEP_RANDOM;
				
				if(random.nextDouble() > probMutate)
				{
					// find worst offender
					step_type = STEP_Z;
					changeIndices = populateZScores(rIdx, 0.95); // variable ignore rate?
					changeLevels = new double[2][2];
					changeLevels[0][ALPHA] = a_scale * random.nextGaussian();
					changeLevels[1][ALPHA] = a_scale * random.nextGaussian();
					changeLevels[0][BETA]  = b_scale * random.nextGaussian();
					changeLevels[1][BETA]  = b_scale * random.nextGaussian();
				}
				else
				{
					// change 'em all.  yeah! (i think the gain is generally too high here)
					changeIndices = new int[nSize]; // could probably make this a static ref
					changeLevels = new double[nSize][2];
					for(int i=0; i<nSize; i++)
					{
						changeIndices[i] = i;
						changeLevels[i][ALPHA] = (a_scale/3.0) * random.nextGaussian() / nSize;
						changeLevels[i][BETA]  = (b_scale/3.0) * random.nextGaussian() / nSize;
					}
				}

				// apply selected changes
				for(int i=0; i<changeIndices.length; i++)
				{
					alphaBeta[rIdx].values[changeIndices[i]][ALPHA] += changeLevels[i][ALPHA];
					alphaBeta[rIdx].values[changeIndices[i]][BETA]  += changeLevels[i][BETA];
				}
				normalizeAlphaBeta(rIdx);
				calculateSigmas(rIdx);
				double newProb = populateProbabilities(rIdx);
				double probGain = newProb - modelProb; // so hard to keep this sign straight

				// allow this change if it improves the net probability or
				// if it makes it no worse than the current temperature
				if(probGain < -temp)
				{
					// undo changes
					for(int i=0; i<changeIndices.length; i++)
					{
						alphaBeta[rIdx].values[changeIndices[i]][ALPHA] -= changeLevels[i][ALPHA];
						alphaBeta[rIdx].values[changeIndices[i]][BETA]  -= changeLevels[i][BETA];
					}
					normalizeAlphaBeta(rIdx);
					calculateSigmas(rIdx);
					missCtr++;
					
					// okay, we missed...i think we should reduce scale, unless
					// we haven't found a workable result yet...
					if(!Double.isInfinite(modelProb))
					{
						scale *= (1.0 - scaleDecay);
						
						// if we're _way_ off, scale should be reduced even more
						if(Math.abs(probGain)/temp > deepThreshhold)
						{
							scale *= (1.0 - scaleDeepDecay);
						}
					}
				}
				else
				{
					//if(!ranOnce) System.out.println(""+temp+", "+modelProb+", "+
					//		scale+", "+missCtr);
					if(!ranOnce) System.out.println(((step_type==STEP_Z)?"Z-Step":"RanStep")+
							"  got ("+fmt.format(probGain)
							+") better on change\n    newT:"+
							temp+", newP: "+newProb+", modelP: "+modelProb+" scale: "+
							scale+" missCtr: "+missCtr);

					// Here I used to do reduce the scale if the miss limit had been
					// overshot.  I suppose we can still do that.  Should scale be
					// reset if we didn't overshoot?
					/*if(missCtr > missLimit)
					{
						scale *= (1.0 - scaleDecay);
					}*/
					
					// reset scale here?  not sure what to do about counted misses...
					scale = baseScale;
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
					if((temp < endingTemp) && (bestResults == null))
					{
						System.out.println("non-convergence?  trying again...");
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
				alphaBeta[rIdx].values[i][ALPHA] -= alphaAdjust;
				alphaBeta[rIdx].values[i][BETA] -= betaAdjust;
			}
		}
		
		protected double getThetaMean(int rIdx, int i, int j)
		{
			int bi = invertedPIndices[p.getPartition(i)];
			int bj = invertedPIndices[p.getPartition(j)];
			return getThetaMean(rIdx, i, j, bi, bj);
		}

		protected double getThetaMean(int rIdx, int i, int j, int bi, int bj)
		{
			double alpha_i = alphaBeta[rIdx].getValueAt(i, ALPHA);
			double beta_j = alphaBeta[rIdx].getValueAt(j, BETA);
			return theta[rIdx].values[bi][bj] + alpha_i + beta_j;
		}
		
		// this should return the product of the 
		// cumulative normal distribution over the indeterminate range of the
		// log of x_ij and x_ji 
		protected double getDyadProbability(int rIdx, int i, int j)
		{
			int bi = invertedPIndices[p.getPartition(i)];
			int bj = invertedPIndices[p.getPartition(j)];

			return getDyadProbability(rIdx, i, j, bi, bj);
		}
		
		protected double getDyadProbability(int rIdx, int i, int j, int bi, int bj)
		{
			double mean_ij = getThetaMean(rIdx, i, j, bi, bj);
			double mean_ji = getThetaMean(rIdx, j, i, bj, bi);

			// calculation for p_ij
			double prob_ij = Double.NaN;
			/*System.out.println("x_ij="+fmt.format(x_ij));
			System.out.println("sigma_rs="+fmt.format(theta_sigma[rIdx].values[bi][bj]));
			System.out.println("chunk_size_rs="+fmt.format(chunk_size[rIdx].values[bi][bj]));
			System.out.println("chunk_count_rs="+fmt.format(chunk_count[rIdx].values[bi][bj]));*/
			if(theta_sigma[rIdx].values[bi][bj] == 0.0)
			{
				prob_ij = 1.0; // no variance...all ties predicted exactly.
			}
			else
			{
				double lij_min = chunk_min[rIdx].values[i][j];
				double lij_max = lij_min + chunk_width[rIdx];
				double pij_min = ExtendedMath.cumulativeNormal(mean_ij,
					theta_sigma[rIdx].values[bi][bj], lij_min);
				double pij_max = ExtendedMath.cumulativeNormal(mean_ij,
					theta_sigma[rIdx].values[bi][bj], lij_max);
				prob_ij = pij_max - pij_min;
			}
			
			// calculation for p_ji
			double prob_ji = Double.NaN;
			if(theta_sigma[rIdx].values[bj][bi] == 0.0)
			{
				prob_ij = 1.0; // no variance...all ties predicted exactly.
			}
			else
			{
				double lji_min = chunk_min[rIdx].values[j][i];
				double lji_max = lji_min + chunk_width[rIdx];
				double pji_min = ExtendedMath.cumulativeNormal(mean_ji,
					theta_sigma[rIdx].values[bj][bi], lji_min);
				double pji_max = ExtendedMath.cumulativeNormal(mean_ji,
					theta_sigma[rIdx].values[bj][bi], lji_max);
				prob_ji = pji_max - pji_min;
			}

			double prob = prob_ij * prob_ji;
			return prob;
		}
		
		protected Apfloat getDyadProbabilityAP(int rIdx, int i, int j)
		{
			int bi = invertedPIndices[p.getPartition(i)];
			int bj = invertedPIndices[p.getPartition(j)];

			return getDyadProbabilityAP(rIdx, i, j, bi, bj);
		}
		
		protected Apfloat getDyadProbabilityAP(int rIdx, int i, int j, int bi, int bj)
		{
			double mean_ij = getThetaMean(rIdx, i, j, bi, bj);
			double mean_ji = getThetaMean(rIdx, j, i, bj, bi);
			if(Double.isNaN(mean_ij) || Double.isNaN(mean_ji))
			{
				// not sure this is right, but...
				return ap_one;
			}

			// calculation for p_ij
			Apfloat prob_ij;
			if(theta_sigma[rIdx].values[bi][bj] == 0.0)
			{
				prob_ij = ap_one; // no variance...all ties predicted exactly.
			}
			else
			{
				double lij_min = chunk_min[rIdx].values[i][j];
				double lij_max = lij_min + chunk_width[rIdx];
				Apfloat pij_min = ExtendedMath.cumulativeNormalAP(mean_ij,
					theta_sigma[rIdx].values[bi][bj], lij_min);
				Apfloat pij_max = ExtendedMath.cumulativeNormalAP(mean_ij,
					theta_sigma[rIdx].values[bi][bj], lij_max);
				prob_ij = pij_max.subtract(pij_min);
				/*if(prob_ij.equals(ap_zero))
				{
					System.out.println("p_ij failure");
					System.out.println("i="+nData.getActor(i).getName());
					System.out.println("j="+nData.getActor(j).getName());
					System.out.println("mean_ij="+mean_ij);
					System.out.println("sigma_rs="+theta_sigma[rIdx].values[bi][bj]);
				}*/
			}
			
			// calculation for p_ji
			Apfloat prob_ji;
			if(theta_sigma[rIdx].values[bj][bi] == 0.0)
			{
				prob_ji = ap_one; // no variance...all ties predicted exactly.
			}
			else
			{
				double lji_min = chunk_min[rIdx].values[j][i];
				double lji_max = lji_min + chunk_width[rIdx];
				Apfloat pji_min = ExtendedMath.cumulativeNormalAP(mean_ji,
					theta_sigma[rIdx].values[bj][bi], lji_min);
				Apfloat pji_max = ExtendedMath.cumulativeNormalAP(mean_ji,
					theta_sigma[rIdx].values[bj][bi], lji_max);
				prob_ji = pji_max.subtract(pji_min);
				/*if(prob_ji.equals(ap_zero))
				{
					System.out.println("p_ji failure");
					System.out.println("j="+nData.getActor(j).getName());
					System.out.println("i="+nData.getActor(i).getName());
					System.out.println("mean_ji="+mean_ji);
					System.out.println("sigma_sr="+theta_sigma[rIdx].values[bj][bi]);					
				}*/
			}

			Apfloat prob = prob_ij.multiply(prob_ji);
			return prob;
		}
		
		// need to readjust this for 'type'
		public double getLgModelProbability()
		{
			int nSize = nData.getSize();
			int numBlocks = p.getPartitionCount();
			double d = getOptimalPrecision(nSize * nSize);
			double alphaBetaLength = (estimateAlphaBeta)?(2.0 * (nSize - 1) * d):0.0;
			double thetaSigmaLength = Double.NaN;
			int[] pIndices = p.getPartitionIndices();

			switch(type)
			{
				case IN_GROUP:
					int inSize = 0;
					for(int bi=0; bi<numBlocks; bi++)
					{
						int actorCt = p.getPartitionSize(pIndices[bi]);
						inSize += actorCt * actorCt;
					}
					int outSize = nSize * nSize - inSize;
					thetaSigmaLength = getOptimalPrecision(inSize) + 
						getOptimalPrecision(inSize - 1) +
						getOptimalPrecision(outSize) +
						getOptimalPrecision(outSize -1);
					break;
				
				case DIAGONAL:
					thetaSigmaLength = 0.0;
					int uncountedTies = nSize * nSize;
					for(int bi=0; bi<numBlocks; bi++)
					{
						int actorCt = p.getPartitionSize(pIndices[bi]);
						thetaSigmaLength += getOptimalPrecision(actorCt * actorCt) +
							getOptimalPrecision(actorCt * actorCt - 1);
						uncountedTies -= actorCt * actorCt;
					}
					thetaSigmaLength += getOptimalPrecision(uncountedTies) +
						getOptimalPrecision(uncountedTies - 1);
					break;
				
				case SATURATED:
					thetaSigmaLength = 0.0;
					for(int bi=0; bi<numBlocks; bi++)
					{
						int biSize = p.getPartitionSize(pIndices[bi]);
						thetaSigmaLength += getOptimalPrecision(biSize * biSize) +
							getOptimalPrecision(biSize * biSize - 1);
						for(int bj=bi+1; bj<numBlocks; bj++)
						{
							// twice this...it's a pair of blocks
							int bjSize = p.getPartitionSize(pIndices[bj]);
							thetaSigmaLength += 2.0 * (getOptimalPrecision(biSize * bjSize) +
								getOptimalPrecision(biSize * bjSize - 1));
						}
					}
					break;
			}
			
			int relCt = nData.getRelationCount();
			double modelDL = 0.0;
			for(int ri=0; ri<relCt; ri++)
			{
				modelDL += (alphaBetaLength + thetaSigmaLength) * resBits[ri];
				// modelDL += (alphaBetaLength + thetaSigmaLength);
			}
			return -modelDL;
		}
				
		public String getFactoryClass()
		{
			return "com.wibinet.networks.P1RBlockmodelFactory";
		}

		protected void populateMatrices()
		{
			LabelModel aLabels = getActorLabels();
			LabelModel pLabels = getPartitionLabels();
			LabelModel abLabels = new ABLabelModel();
			String[] value_strings = new String[1];
			value_strings[0] = "Value";
			LabelModel valueLabel = new DefaultLabelModel(value_strings);

			// addMatrices("Pred", predictedTies, aLabels, aLabels);
			if(estimateAlphaBeta)
			{
				addMatrices("\u03B1/\u03B2", alphaBeta, aLabels, abLabels);
			}
			addMatrices("Theta", theta, pLabels, pLabels);
			addMatrices("Sigma", theta_sigma, pLabels, pLabels);
			addMatrices("Contribution", contribution, pLabels, valueLabel);
			if(estimateActorProbs)
			{
				addMatrices("p(Actor)", actorProbs, aLabels, pLabels);
				addMatrices("p(selfForm)", pSelfForm, aLabels, valueLabel);
				addMatrices("Entropy", actorEntropy, aLabels, valueLabel);
			}
			// addMatrices("Resid", thetaResid, aLabels, aLabels);
		}

		protected void populateReports()
		{
		}
		
		// why is this not implemented somewhere?
		private double sign(double x)
		{
			if(x == 0.0) return 0.0;
			if(x < 0.0) return -1.0;
			return 1.0;
		}
		
		protected double populateProbabilities(int rIdx)
		{
			return populateProbabilities(rIdx, null);
		}
		
		protected double populateProbabilities(int rIdx, AbstractTask task)
		{
			int nSize = nData.getSize();
			boolean _printed = false;

			// reset counter for the number of probabilities we
			// can't estimate
			double lgProb = 0.0;
			for(int i=0; i<nSize; i++)
			{
				if(task != null)
				{
					task.setValue((i * 100) / nSize);
				}
				for(int j=i+1; j<nSize; j++)
				{
					double prob = getDyadProbability(rIdx, i, j);
					double lgP = Double.NaN;
					
					// if the 'regular' (and faster) version of getDyadProbability
					// returns a positive non-zero value, just use that
					if(prob > 0)
					{
						lgP = ExtendedMath.lg(prob);
					}
					
					// otherwise, use the slower (but more accurate) arbitrary-precision
					// version of getDyadProbability
					else
					{
						Apfloat ap_prob = getDyadProbabilityAP(rIdx, i, j);
						Apfloat ap_lgP = ApfloatMath.log(ap_prob).divide(ap_log2e);
						lgP = ap_lgP.doubleValue();
					}

					lgProb += lgP;
					lgDyadProbs[rIdx].values[i][j] = lgP;
					lgDyadProbs[rIdx].values[j][i] = lgP;
				}
			}
			return lgProb;
		}
		
		protected int[] populateZScores(int rIdx)
		{
			return populateZScores(rIdx, 0.0);
		}
		
		/**
		 * A perhaps poorly named method.  Returns a two-element int array corresponding
		 * to the dyad {i, j} that currently is the "worst" fit in the model according
		 * to its z-score.  In other words, it figures out which dyad is most poorly
		 * estimated by the current model, that is, the dyad that is farthest away
		 * from the predicted level of exchange in its block, given the standard deviation
		 * in that block.  The ignoreRate parameter indicates the likelihood that a particular
		 * dyad will be ignored in this analysis.  This is useful for applications where it
		 * would be unproductive to continually identify (and perhaps update) the same dyad
		 * or dyads.
		 * 
		 * @param rIdx relation index
		 * @param ignoreRate how likely a given dyad is to be ignored in the determination of the worst dyad
		 * @return a two-element array of int corresponding to the indices (i, j) of the offending dyad
		 */
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
					if(random.nextDouble() > ignoreRate)
					{
						int bj = invertedPIndices[p.getPartition(j)];
						double x_ij = Math.max(nData.getTieStrength(rIdx, i, j), mnzv[rIdx] * MNZV_FACTOR);
						double x_ji = Math.max(nData.getTieStrength(rIdx, j, i), mnzv[rIdx] * MNZV_FACTOR);

						double lx_ij = Math.log(x_ij);
						double lx_ji = Math.log(x_ji);
	
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
		
		protected double[] getAlphaBetaSigmas(int rIdx)
		{
			double[] ab_sigmas = new double[2];
			
			int nSize = nData.getSize();
			
			// both of these hvae zero means by definition
			double aSqSum = 0.0;
			double bSqSum = 0.0;
			for(int i=0; i<nSize; i++)
			{
				double alpha = alphaBeta[rIdx].values[i][ALPHA];
				double beta  = alphaBeta[rIdx].values[i][BETA];
				aSqSum += alpha * alpha;
				bSqSum += beta * beta;
			}
			ab_sigmas[ALPHA] = Math.sqrt(aSqSum / nSize);
			ab_sigmas[BETA]  = Math.sqrt(bSqSum / nSize);
			
			return ab_sigmas;
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
								double x_ij = Math.max(nData.getTieStrength(rIdx, actor_i, actor_j),
										mnzv[rIdx] * MNZV_FACTOR);
								double q_dev_ij = Math.log(x_ij) - theta_mean_ij;
								
								qSqSum += q_dev_ij * q_dev_ij;
							}
						}
					}
					double newSigma = Math.sqrt(qSqSum / blockSize);
					theta_sigma[rIdx].values[pi][pj] = newSigma;				
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
					lgProb += lgDyadProbs[rIdx].values[i][j];
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
		
		public Results(Model model, int rIdx)
		{
			alphaBeta = new Matrix(model.alphaBeta[rIdx]);
		}
		
		public void apply(Model model, int rIdx)
		{
			model.alphaBeta[rIdx] = new Matrix(alphaBeta);
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
