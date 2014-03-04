/**
 * P1Blockmodel.java
 *
 * A P1Blockmodel is a blockmodel implementation based upon
 *   Wang, Y.J. and Wong, G.Y., 1987, "Stochastick Blockmodels for
 *     Directed Graphs", Journal of the American Statistical
 *     Association, 82, 8-19.
 *
 * Copyright (c) 2001, 2003-2005 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.io.*;
import java.util.Random;
import com.wibinet.gui.*;
import com.wibinet.math.*;
import com.wibinet.math.Partition;

public class P1Blockmodel extends AbstractBlockmodel implements DyadBlockmodel
{
	// can only do certain fixed types of P1Blockmodels for now
	// perhaps should think of some other mechanism
	public final static int IN_GROUP  = 0; // model 2
	public final static int DIAGONAL  = 1; // model 4
	public final static int SATURATED = 2; // model 5 

	protected int type;
  protected double tolerance;

	// basic matrices
	protected Matrix[] m;
	protected Matrix[] a;
	protected Matrix[] n;
	protected Matrix[] theta;
	
	// exact matrices...
	protected Matrix[] m_e;
	protected Matrix[] a_e;
	protected Matrix[] n_e;
	protected Matrix[] tieProbs_e;
	
	// parameter estimates
	protected Matrix tieProbs[];
	protected Matrix alphaBeta[];
	protected final static int ALPHA = 0;
	protected final static int BETA  = 1;
	protected double gSquared = Double.NaN;
	protected double gSquared_e = Double.NaN;
	
	// blocking information
	protected int[] invertedPIndices;
	protected int[][] blockLabels;
	protected int[][] labelGenerator;
	protected int numBlockLabels;
	
	// for report
	protected java.util.Vector[] allG2values;

	// temp
	private Matrix[] g;
	private int eCt;
	protected boolean getNaturalParameters = false;
	
	protected static int equationLimit = 5000;
	
	public P1Blockmodel(NetworkData nData, int type)
	{
    this(nData, nData.getPartition(), type);
	}
	
  public P1Blockmodel(NetworkData nData, Partition p, int type)
  {
  	super(nData, p);
  	this.type = type;
  	this.tolerance = 0.0001;
    
		this.eCt = 0;
		
  	initDataStructures();
  }
  
  public String getFactoryClass()
  {
    return "com.wibinet.networks.P1BlockmodelFactory";
  }
  
  // maybe this should be an abstract method in AbstractBlockmodel
  protected void initDataStructures()
  {
  	resetBlockSizes();
  	int relCt = nData.getRelationCount();
  	int nSize = nData.getSize();
  	m = new Matrix[relCt];
  	a = new Matrix[relCt];
  	n = new Matrix[relCt];
  	
  	m_e = new Matrix[relCt];
  	a_e = new Matrix[relCt];
  	n_e = new Matrix[relCt];
  	tieProbs_e = new Matrix[relCt];
  	
  	tieProbs = new Matrix[relCt];
  	alphaBeta = new Matrix[relCt];
  	theta = new Matrix[relCt];

		// tmp
		g = new Matrix[relCt];

    // create inverted permutation indces array
    invertedPIndices = new int[nSize];
    for(int i=0; i<nSize; i++)
    {
      invertedPIndices[i] = -6741; // make sure it's something unlikely
    }
    for(int pi=0; pi<pIndices.length; pi++)
    {
      invertedPIndices[pIndices[pi]] = pi;
    }

  	// setup block labels
  	int numBlocks = p.getPartitionCount();
  	switch(type)
  	{
  		case IN_GROUP:
  			numBlockLabels = 1;
  			break;
  		
  		case DIAGONAL:
  			numBlockLabels = numBlocks;
  			break;
  		
  		case SATURATED:
  			numBlockLabels = (numBlocks - 1) * (numBlocks - 1);
  			// numBlockLabels = (numBlocks * numBlocks) - 1;
  			break;
  			
  		default:
  			numBlockLabels = 0;
  	}
  	
  	// if there is only one block, fix numBlockLabels
  	if(numBlocks == 1)
  	{
	  	numBlockLabels = 0; // no identifiable block params
	  }
  	
  	// the label generator will be used to label individual actors
  	labelGenerator = new int[numBlocks][];
  	for(int bi=0; bi<numBlocks; bi++)
  	{
  		labelGenerator[bi] = new int[numBlocks];
  		for(int bj=0; bj<numBlocks; bj++)
  		{
  			int blockLabel = -1;
  			switch(type)
  			{
  				case IN_GROUP:
  					if(bi == bj)
  					{
  						blockLabel = 0;
  					}
  					break;
  				
  				case DIAGONAL:
  					if(bi == bj)
  					{
  						blockLabel = bi;
  					}
  					break;
  				
  				case SATURATED:
  					blockLabel = (numBlocks-1) * bi + bj;
  					if((bi == numBlocks-1) || (bj == numBlocks-1))
  					{
  						blockLabel = -1;
  					}
  					
  					// very arbitrary...label everything but first
  					// block as different?
  					// blockLabel = numBlocks * bi + bj - 1;
  					break;
  			}
  			
  			// fix for 1 block case
  			if(numBlocks == 1)
  			{
  				blockLabel = -1;
  			}
  			
  			labelGenerator[bi][bj] = blockLabel;
  		}
  	}
  	
  	// setup actor-level block labels
  	blockLabels = new int[nSize][];
  	for(int i=0; i<nSize; i++)
  	{
  		int iBlock = p.getPartition(i);
  		blockLabels[i] = new int[nSize];
  		for(int j=0; j<nSize; j++)
  		{
  			if(i != j)
  			{
  				int jBlock = p.getPartition(j);
  				int bi = invertedPIndices[iBlock];
  				int bj = invertedPIndices[jBlock];
  				
  				blockLabels[i][j] = labelGenerator[bi][bj];
  			}
  			else
  			{
  				blockLabels[i][j] = -2; // shouldn't be looked at, hopefully
  					// will cause program to 'notice' (i.e. crash) if we see
  					// this later.
  			}
  		}
  	}


  	for(int rIdx=0; rIdx<relCt; rIdx++)
  	{
  		m[rIdx] = new Matrix(nSize, nSize);
  		a[rIdx] = new Matrix(nSize, nSize);
  		n[rIdx] = new Matrix(nSize, nSize);
  		tieProbs[rIdx] = new Matrix(nSize, nSize);
  		
  		// init exact one too.
  		tieProbs_e[rIdx] = new Matrix(nSize, nSize);
  		
  		// initalize alpha/betas
  		alphaBeta[rIdx] = new Matrix(nSize, 2);
  		double maxPartners = (double)(nSize-1);
  		for(int i=0; i<nSize; i++)
  		{
  			// check for empty rows & cols
  			double rMargin = nData.getRowMarginal(rIdx, i);
  			double cMargin = nData.getColMarginal(rIdx, i);
  			if(rMargin == 0.0)
  			{
  				alphaBeta[rIdx].setValueAt(i, ALPHA, Double.NEGATIVE_INFINITY);
  			}
  			else if(rMargin == maxPartners)
  			{
  				alphaBeta[rIdx].setValueAt(i, ALPHA, Double.POSITIVE_INFINITY);
  			}
  			if(cMargin == 0.0)
  			{
  				alphaBeta[rIdx].setValueAt(i, BETA, Double.NEGATIVE_INFINITY);
  			}
  			else if(cMargin == maxPartners)
  			{
  				alphaBeta[rIdx].setValueAt(i, BETA, Double.POSITIVE_INFINITY);
  			}
  		}
  		theta[rIdx] = new Matrix(numBlocks, numBlocks);
  	}
		
		// one more thing
		allG2values = new java.util.Vector[relCt];
		for(int rIdx=0; rIdx<relCt; rIdx++)
		{
			allG2values[rIdx] = new java.util.Vector();
		}
  }
  
  // again, maybe initDataStructures() should be abstract in AbstBmodel
  public void setPartition(Partition p)
  {
  	super.setPartition(p);
  	initDataStructures();
  }
  
  public void setType(int type)
  {
  	this.type = type;
  	initDataStructures();
  }
  
  public void setTolerance(double tolerance)
  {
    this.tolerance = tolerance;
  }

  public void populateMatrices()
  {
		// add matrices
		// unicode stuff?  hope this is really cross-platform :)
		// plausibly could have a static function "getUnicode()" which
		// returns different things depending on whether fonts are installed...
    clearMatrices();
    
		LabelModel pLabels = getPartitionLabels();
		LabelModel aLabels = getActorLabels();
		LabelModel abLabels = new ABLabelModel();
		if(getNaturalParameters)
		{
			addMatrices("Alpha/Beta", alphaBeta, aLabels, abLabels);
			addMatrices("Theta", theta, pLabels, pLabels);
		}
		addMatrices("E(Ties)", tieProbs, aLabels, aLabels);

		addMatrices("Mutuals", m, aLabels, aLabels);
		addMatrices("Asymmetric", a, aLabels, aLabels);
		addMatrices("Nulls", n, aLabels, aLabels);
		
		// do we want these?
		addMatrices("Mutuals'", m_e, aLabels, aLabels);
		addMatrices("Asymmetric'", a_e, aLabels, aLabels);
		addMatrices("Nulls'", n_e, aLabels, aLabels);

		// debugging purposes only!
		// addMatrices("g-Matrix", g);
		
  	int relCt = nData.getRelationCount();
  	int nSize = nData.getSize();

		// Matrix[] p1 = new Matrix[relCt];
		// Matrix[] p2 = new Matrix[relCt];
		Matrix[] pe = new Matrix[relCt];
		Matrix[] mBlockLabels = new Matrix[relCt];
		for(int rIdx=0; rIdx<relCt; rIdx++)
		{
			// p1[rIdx] = new Matrix(nSize, nSize);
			// p2[rIdx] = new Matrix(nSize, nSize);
			pe[rIdx] = new Matrix(nSize, nSize);
			mBlockLabels[rIdx] = new Matrix(nSize, nSize);
			for(int i=0; i<nSize; i++)
			{
				for(int j=0; j<nSize; j++)
				{
					// p1[rIdx].values[i][j] = getDyadProbability_new(i, j);
					// p2[rIdx].values[i][j] = getDyadProbability(i, j);
					pe[rIdx].values[i][j] = getPredictedTieStrengthExact(rIdx, i, j);
					mBlockLabels[rIdx].values[i][j] = blockLabels[i][j] * 1.0;
				}
			}
		}
		// addMatrices("Cool Probs", p1, aLabels, aLabels);
		// addMatrices("Std Probs", p2, aLabels, aLabels);
		addMatrices("Exact Ties", tieProbs_e, aLabels, aLabels);
		addMatrices("blockLabels", mBlockLabels, aLabels, aLabels);
		
		if(getNaturalParameters)
		{
			Matrix[] estTheta = new Matrix[relCt];
			for(int rIdx=0; rIdx<relCt; rIdx++)
			{
				estTheta[rIdx] = new Matrix(nSize, nSize);
				for(int i=0; i<nSize; i++)
				{
					int bi = invertedPIndices[p.getPartition(i)];
					for(int j=0; j<nSize; j++)
					{
						int bj = invertedPIndices[p.getPartition(j)];
						double _theta = theta[rIdx].values[bi][bj];
						double _alpha = alphaBeta[rIdx].values[i][ALPHA];
						double _beta = alphaBeta[rIdx].values[j][BETA];
						double _tie = Math.exp(_theta + _alpha + _beta);
						estTheta[rIdx].values[i][j] = _tie;
					}
				}
			}
			addMatrices("EstTheta", estTheta, aLabels, aLabels);
		}
  }

	protected void populateReports()
	{
		int relCt = nData.getRelationCount();
		String[] reportHTML = new String[relCt];
		for(int ri=0; ri<relCt; ri++)
		{
			StringBuffer htmlBuf = new StringBuffer();
			htmlBuf.append("<HTML>\n<BODY>\n");
			
			for(int gi=0; gi<allG2values[ri].size(); gi++)
			{
				htmlBuf.append("G2 = " + allG2values[ri].elementAt(gi) + "<br />\n");
			}
			
			htmlBuf.append("</BODY>\n</HTML>\n");
			
			reportHTML[ri] = htmlBuf.toString();
		}
		addReports("G2", reportHTML);
	}

	public void compute(AbstractTask task)
	{
		// this is probably redundant, though we could remove it
		// upstream...
		initDataStructures();
		
		// initialize matrices
		int relCt = nData.getRelationCount();
		int numBlocks = p.getPartitionCount();
		int nSize = nData.getSize();

		for(int rIdx=0; rIdx<relCt; rIdx++)
		{
			for(int i=0; i<nSize; i++)
			{
				for(int j=0; j<nSize; j++)
				{
					/*
					 * Wang & Wong p. 12 suggests that these can be
					 * initialized to 0.25
					 */
					if(i != j)
					{
						m[rIdx].values[i][j] = 0.25;
						a[rIdx].values[i][j] = 0.25;
						n[rIdx].values[i][j] = 0.25;
					}
					else
					{
						// n[rIdx].values[i][j] = 1.0;
					}
				}
			}
		}
  	
		// iterative scale them all
		IterativeScaler scaler = new IterativeScaler();
		for(int rIdx=0; rIdx<relCt; rIdx++)
		{
			BlockScalable scalable = new BlockScalable(rIdx);
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
			scaler.scale(scalable, task);
		}
		
		// store 'exact' values
		for(int rIdx=0; rIdx<relCt; rIdx++)
		{
			m_e[rIdx] = new Matrix(m[rIdx]);
			a_e[rIdx] = new Matrix(a[rIdx]);
			n_e[rIdx] = new Matrix(n[rIdx]);
		}
  	
		// truncate MAN-values to optimal precision
		// (I'm so not sure about how doing this before solving for
		//  alpha, beta, and gamma values will disturb the stability
		//  of the overconstrained system of equations)
		double dPrecision = getOptimalPrecision(nSize * (nSize-1));
		int precision = (int)Math.ceil(dPrecision); // round up
		setPrecision(precision);
		for(int rIdx=0; rIdx<relCt; rIdx++)
		{
			for(int i=0; i<nSize; i++)
			{
				for(int j=0; j<nSize; j++)
				{
					m[rIdx].values[i][j] =
						BinaryFractionRange.truncate(m[rIdx].values[i][j], precision);
					a[rIdx].values[i][j] =
						BinaryFractionRange.truncate(a[rIdx].values[i][j], precision);
					n[rIdx].values[i][j] =
						BinaryFractionRange.truncate(n[rIdx].values[i][j], precision);
				}
			}
		}
  	
		// solve for alpha, beta, theta(lambda) values
		if(getNaturalParameters)
		{
			for(int rIdx=0; rIdx<relCt; rIdx++)
			{
				if(task != null)
				{
					if(nData instanceof VisualNetworkData)
					{
						task.setDescription("Getting parameters for '" + 
							((VisualNetworkData)nData).getName()+"'");
					}
					else
					{
						task.setDescription("Getting parameters for relation " + (rIdx+1));
					}
					Thread.yield();
				}
				Matrix thetaMatrix = new Matrix(nSize);
				for(int i=0; i<nSize; i++)
				{
					for(int j=0; j<nSize; j++)
					{
						double theta_ij = Math.log(a[rIdx].values[i][j]/n[rIdx].values[i][j]);
						thetaMatrix.setValueAt(i, j, theta_ij);
					}
				}

				try {
					solveAlphaBeta2(alphaBeta[rIdx], thetaMatrix, theta[rIdx],
						blockLabels, labelGenerator, numBlockLabels, task);
				} catch(IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
  	
  	// determine fitted cell probabilities
  	for(int rIdx=0; rIdx<relCt; rIdx++)
  	{
  		for(int i=0; i<nSize; i++)
  		{
  			for(int j=0; j<nSize; j++)
  			{
  				// for now multiply by 100 for ease of reading
  				double tp = m[rIdx].values[i][j] + a[rIdx].values[i][j];
  				double tp_e = m_e[rIdx].values[i][j] + a_e[rIdx].values[i][j];
  				
  				int tpi = (int)(100.0 * tp);
  				tieProbs[rIdx].values[i][j] = tp;
  				tieProbs_e[rIdx].values[i][j] = tp_e;
  			}
  		}
  	}
  	
  	// determine G^2 and G^2'
  	gSquared = 0.0;
  	gSquared_e = 0.0;
  	for(int rIdx=0; rIdx<relCt; rIdx++)
  	{
  		for(int i=0; i<nSize; i++)
  		{
  			for(int j=0; j<nSize; j++)
  			{
  				if(i != j)
  				{
  					double ts = nData.getTieStrength(rIdx, i, j);
  					
  					// don't get into situation where we take log 0 = -Inf
  					// and try to multiply that by 0
  					if(ts != 0.0)
  					{
  						gSquared += ts * Math.log(ts / tieProbs[rIdx].values[i][j]);
  						gSquared_e += ts * Math.log(ts / tieProbs_e[rIdx].values[i][j]);
  					}
  				}
  			}
  		}
  	}
	}
	
	private static void writeRow(PrintWriter out, double[] row, double x) throws IOException
	{
		for(int i=0; i<row.length; i++)
		{
			out.print(row[i]+"\t");
		}
		out.println(x);
	}
	
	public static void solveAlphaBeta2(Matrix mAlphaBeta, Matrix mTheta, Matrix mBlockTheta,
		int[][] blockLabels, int[][] lGen, int blockLabelCt, AbstractTask task) throws IOException
	{
		int nSize = mTheta.getRows();
		int varCt = 2 * nSize // alphas & betas
			+ blockLabelCt // theta_rs's
			+ 1; // theta
		
		// setup infinite alpha/beta indicators
		boolean[] infAlpha = new boolean[nSize];
		boolean[] infBeta = new boolean[nSize];
		for(int i=0; i<nSize; i++)
		{
			infAlpha[i] = Double.isInfinite(mAlphaBeta.getValueAt(i, ALPHA));
			infBeta[i] = Double.isInfinite(mAlphaBeta.getValueAt(i, BETA));
		}
				
		// get a least squares estimator (don't estimate constant!)
		File tmp = new File("regress.txt");
		FileOutputStream fos = new FileOutputStream(tmp);
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		BufferedWriter bw = new BufferedWriter(osw, 8096);
		PrintWriter out = new PrintWriter(bw);
		for(int i=0; i<nSize; i++)
		{
			out.print("alpha"+i+"\tbeta"+i+"\t");
		}
		for(int i=0; i<blockLabelCt; i++)
		{
			out.print("theta"+i+"\t");
		}
		out.println("theta\tdv");
		
		LeastSquaresEstimator lse = new LeastSquaresEstimator(varCt, false);
		double[] xrow = new double[varCt];
		
		// add an equation for sum_i(alpha_i)=0
		for(int xi=0; xi<varCt; xi++) xrow[xi] = 0.0;
		for(int i=0; i<nSize; i++)
		{
			if(!infAlpha[i])
			{
				xrow[i*2] = 1.0;
			}
		}
		lse.addRow(1.0, xrow, 0.0);
		writeRow(out, xrow, 0.0);

		// add an equation for sum_i(beta_i)=0
		for(int xi=0; xi<varCt; xi++) xrow[xi] = 0.0;
		for(int i=0; i<nSize; i++)
		{
			if(!infBeta[i])
			{
				xrow[i*2+1] = 1.0;
			}
		}
		lse.addRow(1.0, xrow, 0.0);
		writeRow(out, xrow, 0.0);
		
		// add equations setting all infinite alphas & betas to zero?
		for(int i=0; i<nSize; i++)
		{
			if(infAlpha[i])
			{
				for(int xi=0; xi<varCt; xi++) xrow[xi] = 0.0;
				xrow[i*2] = 1.0;
				lse.addRow(1.0, xrow, 0.0);
				writeRow(out, xrow, 0.0);
			}
			if(infBeta[i])
			{
				for(int xi=0; xi<varCt; xi++) xrow[xi] = 0.0;
				xrow[i*2+1] = 1.0;
				lse.addRow(1.0, xrow, 0.0);
				writeRow(out, xrow, 0.0);
			}
		}
		
		// add the rest of the equations, but (more or less) up to
		// equationLimit
		
		double randomThresh = (equationLimit * 1.0) / (nSize * (nSize-1.0));
		Random random = new Random();
		// System.out.println("blockLabelCt = " + blockLabelCt);
		
		// need a set of indicators to make sure we have at least one equation
		// per theta
		boolean[] thetaMeasured = new boolean[blockLabelCt];
		for(int ti=0; ti<thetaMeasured.length; ti++)
		{
			thetaMeasured[ti] = false;
		}
		
		for(int i=0; i<nSize; i++)
		{
			if(task != null)
			{
				int pctDone = (i * 100) / nSize;
				task.setValue(pctDone);
				// System.out.println(""+pctDone+"% done");
				// Thread.yield();
			}
			for(int j=0; j<nSize; j++)
			{
				// check block label first
				int blockLabel = -1;
				boolean mustCheck = false;
				if(blockLabels != null)
				{
					blockLabel = blockLabels[i][j];
				}
				if(blockLabel >= 0)
				{
					// System.out.println("blockLabel = " + blockLabel);
					// System.out.println("thetaMeasured.length = " + thetaMeasured.length);
					mustCheck = !thetaMeasured[blockLabel];
					if(mustCheck)
					{
						thetaMeasured[blockLabel] = true;
					}
				}
				
				// don't add all of these
				if((random.nextDouble() < randomThresh) || mustCheck)
				{
					if((i!=j) && !infAlpha[i] && !infBeta[j])
					{
						for(int xi=0; xi<varCt; xi++) xrow[xi] = 0.0;
						double theta_ij = mTheta.values[i][j];
						xrow[i*2] = 1.0; // alpha
						xrow[j*2+1] = 1.0; // beta
	
						// add block label to equation
						if(blockLabel >= 0)
						{
							xrow[nSize*2 + blockLabel] = 1.0; // theta_rs
						}
						
						xrow[varCt-2] = 1.0; // theta_0;
						lse.addRow(1.0, xrow, theta_ij);
						writeRow(out, xrow, theta_ij);
					}
				}
			}
		}
		
		fos.close();
		double[] rCoef = lse.getRegressionCoefficients();
		
		// read out alpha & beta vals
		for(int i=0; i<nSize; i++)
		{
			if(!infAlpha[i])
			{
				mAlphaBeta.setValueAt(i, ALPHA, rCoef[i*2]);
			}
			if(!infBeta[i])
			{
				mAlphaBeta.setValueAt(i, BETA, rCoef[i*2+1]);
			}
		}

		// read out theta_0 value
		double theta_0 = rCoef[varCt-2];
			
		// read out theta_rs values
		if(blockLabels != null)
		{
			int numBlocks = mBlockTheta.getRows();
			double[] thetaHolder = new double[blockLabelCt];
			for(int k=0; k<thetaHolder.length; k++)
			{
				thetaHolder[k] = rCoef[nSize * 2 + k];
			}
			if(blockLabels != null)
			for(int bi=0; bi<numBlocks; bi++)
			{
				for(int bj=0; bj<numBlocks; bj++)
				{
					int blockLabel = lGen[bi][bj]; // hope that's right....
					if(blockLabel != -1)
					{
						mBlockTheta.values[bi][bj] = theta_0 + thetaHolder[blockLabel];
					}
					else
					{
						mBlockTheta.values[bi][bj] = theta_0;
					}
				}
			}
		}
		else
		{
			// unitary theta...
			mBlockTheta.values[0][0] = theta_0;
		}
	}
	
	// maybe move this all the way out?  not sure to where...
	public static void solveAlphaBeta(Matrix mAlphaBeta, Matrix mTheta, Matrix mBlockTheta,
		int[][] blockLabels, int[][] lGen, int blockLabelCt, AbstractTask task)
		// ^- could probably determine blockLabelCt
	{
		int nSize = mTheta.getRows();
		int matrixCols = 2 * nSize // alphas & betas
			+ blockLabelCt // theta_rs's
			+ 2; // theta, sum
		int matrixRows = nSize * (nSize-1) + 2;
		
		// setup infinite alpha/beta indicators
		boolean[] infAlpha = new boolean[nSize];
		boolean[] infBeta = new boolean[nSize];
		for(int i=0; i<nSize; i++)
		{
			infAlpha[i] = Double.isInfinite(mAlphaBeta.getValueAt(i, ALPHA));
			infBeta[i] = Double.isInfinite(mAlphaBeta.getValueAt(i, BETA));
			
			// allocate room for rows that say alpha_i = 0 or beta_i = 0
			if(infAlpha[i])
			{
				matrixRows++;
			}
			if(infBeta[i])
			{
				matrixRows++;
			}
		}
		
		// setup tabu pairs
		boolean[][] tabuPairs = new boolean[nSize][];
		for(int i=0; i<nSize; i++)
		{
			tabuPairs[i] = new boolean[nSize];
			for(int j=0; j<nSize; j++)
			{
				// does this start as 'tabu'?
				if(i == j)
				{
					tabuPairs[i][j] = true;
				}
				else if(infAlpha[i] || infBeta[j])
				{
					tabuPairs[i][j] = true;
					matrixRows--; // one equation we won't be estimating
				}
				else
				{
					tabuPairs[i][j] = false;
				}
			}
		}
		
		// try to set up a matrix to use in solving.  since
		// matrixRows might be really big, catch an exception here.
		int actualRows = matrixRows;
		Matrix gMatrix = null;
		try
		{
			gMatrix = new Matrix(matrixRows, matrixCols);
		}
		catch(OutOfMemoryError oome)
		{
			// hmm...ran out of memory, eh?  i know we shouldn't do this
			// silently, but my next best guess would be to try to
			// use less.  the matrix is overdetermined as it is, so we
			// don't need all of the rows anyway.  i hope twice as
			// many rows as columns will be sufficient
			actualRows = matrixCols * 2;
			
			// maybe should catch OutOfMemoryError again?
			gMatrix = new Matrix(actualRows, matrixCols);
		}
		
		// fill side condition rows
		int rowCt = 2; // start it here, we may need to add rows for
									// infinite parameters
		
		for(int i=0; i<nSize; i++)
		{
			// only add if we don't know they're infinity
			if(!infAlpha[i])
			{
				gMatrix.setValueAt(0, i*2, 1.0); // alphas sum to zero
			}
			else
			{
				gMatrix.setValueAt(rowCt, i*2, 1.0);
				gMatrix.setValueAt(rowCt, matrixCols-1, 0.0); // alpha_i = 0
				rowCt++;
			}
			
			if(!infBeta[i])
			{
				gMatrix.setValueAt(1, i*2+1, 1.0); // betas sum to zero
			}
			else
			{
				gMatrix.setValueAt(rowCt, i*2+1, 1.0);
				gMatrix.setValueAt(rowCt, matrixCols-1, 0.0); // beta_i = 0
				rowCt++;
			}
		}
		gMatrix.setValueAt(0, matrixCols-1, 0.0); // alphas sum to 0.0
		gMatrix.setValueAt(1, matrixCols-1, 0.0); // betas sum to 0.0
		
		// fill up equation matrix
		for(int i=0; i<nSize; i++)
		{
			for(int j=0; j<nSize; j++)
			{
				// is (i, j) a tabu pair?
				if(!tabuPairs[i][j])
				{
					double theta_ij = mTheta.values[i][j];
					gMatrix.setValueAt(rowCt, i*2, 1.0); // alpha
					gMatrix.setValueAt(rowCt, j*2+1, 1.0); // beta

					// get block
					if(blockLabels != null)
					{
						int blockLabel = blockLabels[i][j];
						if(blockLabel != -1)
						{
							gMatrix.setValueAt(rowCt, nSize * 2 + blockLabel, 1.0); // theta_rs param
						}
					}
					
					gMatrix.setValueAt(rowCt, matrixCols-2, 1.0); // theta_0
					gMatrix.setValueAt(rowCt, matrixCols-1, theta_ij);
					rowCt++;
				}
				
				// make sure we ain't gone too far..
				if(rowCt >= actualRows)
				{
					break;
				}
			}
			
			// do we need to break again?
			if(rowCt >= actualRows)
			{
				break;
			}
		}
		
		// solve equations using gaussian elimination
		try
		{
			// comment/uncomment this to store gMatrix before elimination
			// g[rIdx] = new Matrix(gMatrix);
			
			gMatrix.gaussianEliminate(matrixCols-1);
			
			// read out alpha & beta vals
			for(int i=0; i<nSize; i++)
			{
				if(!infAlpha[i])
				{
					mAlphaBeta.setValueAt(i, ALPHA, gMatrix.values[i*2][matrixCols-1]);
				}
				if(!infBeta[i])
				{
					mAlphaBeta.setValueAt(i, BETA, gMatrix.values[i*2+1][matrixCols-1]);
				}
			}

			// read out theta_0 value
			double theta_0 = gMatrix.values[matrixCols-2][matrixCols-1];
			
			// read out theta_rs values
			if(blockLabels != null)
			{
				int numBlocks = mBlockTheta.getRows();
				double[] thetaHolder = new double[blockLabelCt];
				for(int k=0; k<thetaHolder.length; k++)
				{
					thetaHolder[k] = gMatrix.values[nSize * 2 + k][matrixCols-1];
				}
				if(blockLabels != null)
				for(int bi=0; bi<numBlocks; bi++)
				{
					for(int bj=0; bj<numBlocks; bj++)
					{
						int blockLabel = lGen[bi][bj]; // hope that's right....
						if(blockLabel != -1)
						{
							mBlockTheta.values[bi][bj] = theta_0 + thetaHolder[blockLabel];
						}
						else
						{
							mBlockTheta.values[bi][bj] = theta_0;
						}
					}
				}
			}
			else
			{
				// unitary theta...
				mBlockTheta.values[0][0] = theta_0;
			}
		}
		catch(MatrixException me)
		{
			// logException(me, "Can't solve equations for alpha/beta/theta");
		}

		// comment/uncomment this to store gMatrix after elimination
		// g[rIdx] = gMatrix;
	}
	
	public double getLgModelProbability()
	{
		// how many block parameters
		int k = numBlockLabels;
  	int nSize = nData.getSize();
		int relCt = nData.getRelationCount();
		double d = getOptimalPrecision(nSize * (nSize-1) * relCt);
		
		return -(relCt * (2.0 * (nSize - 1) + k + 2.0) * d);
	}
	
	private static double INT_STEP = 0.1;
	public double getDyadProbability_new(int i, int j)
	{
		if(i == j) return 1.0; // self tie...
		
		// setup normal distribution params
  	int nSize = nData.getSize();
  	double d = getOptimalPrecision(nSize * (nSize-1));
  	int intD = (int)d;
  	double stddev = 2.0 / d;

		// use MAN distribution
		double p = 1.0;
		int r = nData.getRelationCount();
		for(int rIdx=0; rIdx<r; rIdx++)
		{
			double x_ij = nData.getTieStrength(rIdx, i, j);
			double x_ji = nData.getTieStrength(rIdx, j, i);

			// get values
			double mVal = m[rIdx].values[i][j];
			double nVal = n[rIdx].values[i][j];
			double aVal_out = a[rIdx].values[i][j];
			double aVal_in  = a[rIdx].values[j][i];
			
			int mXVal = LogitNormal.getXVal(mVal, intD);
			int nXVal = LogitNormal.getXVal(nVal, intD);
			int aXVal_out = LogitNormal.getXVal(aVal_out, intD);
			int aXVal_in  = LogitNormal.getXVal(aVal_in, intD);

			// do 'rough' integration over m, a, n
			double pSum = 0.0;
			double dx = INT_STEP * INT_STEP * INT_STEP;
			double dxSum = 0.0;
			for(double mij=INT_STEP/2.0; mij<1.0; mij += INT_STEP)
			{
				double rho_mij = LogitNormal.getValue(intD, mXVal, mij);

				for(double nij=INT_STEP/2.0; nij<1.0; nij += INT_STEP)
				{
					double rho_nij = LogitNormal.getValue(intD, nXVal, nij);
					
					for(double aij=INT_STEP/2.0; aij<1.0; aij += INT_STEP)
					{
						// make sure we're still in legitmate space
						if(mij + nij + aij < 1.0)
						{
							double aji = 1.0 - (mij + nij + aij);

							double rho_aij = LogitNormal.getValue(intD, aXVal_out, aij);
							double rho_aji = LogitNormal.getValue(intD, aXVal_in, aji);
	
							// compute value at this point in space
						 	double pVal = x_ij * x_ji * mij +
						 		(1.0-x_ij) * (1.0-x_ji) * nij +
						 		x_ij * (1.0-x_ji) * aij +
						 		(1.0-x_ij) * x_ji * aji;
					 		
					 		// compute joint probability at this point in space
					 		double rho = rho_mij * rho_nij * rho_aij * rho_aji;
					 		
						 	// add this to sum (will scale later)
						 	pSum += pVal * rho;
						 	dxSum += dx;
						}
					}
				}
			}

			p *= (pSum / dxSum);
		}
		return p;
	}
	
	public double getDyadProbability(int i, int j)
	{
		if(i == j) return 1.0; // self tie...
		
		// use MAN distribution
		double p = 1.0;
		int r = nData.getRelationCount();
		for(int rIdx=0; rIdx<r; rIdx++)
		{
			double x_ij = nData.getTieStrength(rIdx, i, j);
			double x_ji = nData.getTieStrength(rIdx, j, i);
			p *= x_ij * x_ji * m[rIdx].values[i][j] +
				(1.0-x_ij) * (1.0-x_ji) * n[rIdx].values[i][j] +
				x_ij * (1.0-x_ji) * a[rIdx].values[i][j] +
				(1.0-x_ij) * x_ji * a[rIdx].values[j][i];
		}
		return p;
	}
	
	public int getDyadParameterCount(int bi, int bj)
	{
		return 0; // wrong!
	}

	protected double getDyadProbabilityExact(int i, int j)
	{
		if(i == j) return 1.0; // self tie...
		
		// use MAN distribution
		double p = 1.0;
		int r = nData.getRelationCount();
		for(int rIdx=0; rIdx<r; rIdx++)
		{
			double x_ij = nData.getTieStrength(rIdx, i, j);
			double x_ji = nData.getTieStrength(rIdx, j, i);
			p *= x_ij * x_ji * m_e[rIdx].values[i][j] +
				(1.0-x_ij) * (1.0-x_ji) * n_e[rIdx].values[i][j] +
				x_ij * (1.0-x_ji) * a_e[rIdx].values[i][j] +
				(1.0-x_ij) * x_ji * a_e[rIdx].values[j][i];
		}
		return p;
	}
	
	public double getLgDataProbability()
	{
		// the probability is the joint of the actors;
		double lgProb = 0.0;
		int nSize = nData.getSize();
		for(int pi=0; pi<nSize; pi++)
		{
			for(int pj=0; pj<=pi; pj++)
			{
				lgProb += ExtendedMath.lg(getDyadProbability(pi, pj));
			}
		}
		return lgProb;
	}
	
	public double getLgDataProbabilityExact()
	{
		// the probability is the joint of the actors;
		double lgProb = 0.0;
		int nSize = nData.getSize();
		for(int pi=0; pi<nSize; pi++)
		{
			for(int pj=0; pj<=pi; pj++)
			{
				lgProb += ExtendedMath.lg(getDyadProbabilityExact(pi, pj));
			}
		}
		return lgProb;
	}

	public double getPredictedTieStrength(int rIdx, int i, int j)
	{
		// return tieProbs[rIdx].values[i][j] / 100.0;
		
		// kludge?
		if(i == j)
		{
			return 0.0;
		}
		
		return m_e[rIdx].values[i][j] + a_e[rIdx].values[i][j];
	}
	
	public double getPredictedTieStrengthExact(int rIdx, int i, int j)
	{
		// kludge?
		/*if(i == j)
		{
			return 0.0;
		}*/
		
		return m_e[rIdx].values[i][j] + a_e[rIdx].values[i][j];
	}

	public double getGSquared()
	{
		return gSquared;
	}
	
	public double getGSquaredExact()
	{
		return gSquared_e;
	}
	
	protected void logException(Exception e, String message)
	{
		// this should do something smart with exceptions.
		// can't necessarily pop-up a window because we might be in
		// search mode.
		// System.err.println("["+eCt+"] " + e + ": " + message);
		eCt++;
	}

	// constants
	protected final static int MUTUALS = 0;
	protected final static int ASYMMETRIC = 1;
	protected final static int NULL = 2;
	
	protected final static int ROW_STEP = 0;
	protected final static int COL_STEP = 1;
	protected final static int BLOCK_STEP = 2;
	protected final static int MUTUAL_STEP = 3;
	protected final static int NORMALIZING_STEP = 4;
	
	private static java.text.NumberFormat fmt = 
		java.text.NumberFormat.getInstance();
	static
	{
		fmt.setMaximumFractionDigits(5);
	}
		
	protected class BlockScalable implements IterativeScalable
	{
		protected Matrix[] myMatrices;
		protected Relation r;
		protected int nSize;
		protected int numBlocks;

		protected double[] rowMarginals;
		protected double[] colMarginals;
		
		protected double totalMutuals;
		protected double totalNoTies;
		protected double totalTies;

		protected double[][] blockMass;
		protected double[]   groupMass;
		protected double unlabeledGroupMass;

		protected double[] rowFactor; // E_i^{(n))}
		protected double[] colFactor; // G_i^{(n))}
		
		protected double[] labeledBlockFactor; // C_k
		protected double unlabeledBlockFactor; // C_L
		protected double[] blockPredictedTies;
		
		protected double[][] matrixSums;
		protected double noTieFactor;     // F^{(n)}
		protected double mutualFactor;    // H^{(n)}
		protected double offMutualFactor; // M^{(n)}
		
		protected double oldG2;
		protected double pctDone;
		protected String detailStr;
		protected java.util.Vector g2values;
		
		// some semi-constants so we don't have to keep calculating
		// and casting to double
		protected double gChoose2;
		protected double maxSize;
		
		public BlockScalable(int rIdx)
		{
			this.r = nData.getRelation(rIdx);
			this.oldG2 = Double.NaN;
			
			myMatrices = new Matrix[3];
			myMatrices[MUTUALS] = m[rIdx];
			myMatrices[ASYMMETRIC] = a[rIdx];
			myMatrices[NULL] = n[rIdx];
			nSize = nData.getSize();
			numBlocks = p.getPartitionCount();
				
			// load up block mass array, setup block group masses
			blockMass = new double[numBlocks][];
			groupMass = new double[numBlockLabels];
			unlabeledGroupMass = 0.0;
			for(int bi=0; bi<numBlocks; bi++)
			{
				blockMass[bi] = new double[numBlocks];
				for(int bj=0; bj<numBlocks; bj++)
				{
					blockMass[bi][bj] = nData.getBlockMass(p, rIdx,
						pIndices[bi], pIndices[bj]);
					int blockLabel = labelGenerator[bi][bj];
					if(blockLabel != -1)
					{
						groupMass[blockLabel] += blockMass[bi][bj];
					}
					else
					{
						unlabeledGroupMass += blockMass[bi][bj];
					}
				}
			}
			
			maxSize = 1.0 * (nSize * (nSize-1));
 			gChoose2 = maxSize / 2.0;

			totalTies = nData.getMass(rIdx);
			totalMutuals = nData.getMutuals(rIdx);
			totalNoTies = maxSize - totalTies;
			
			// get marginals
			rowMarginals = new double[nSize];
			colMarginals = new double[nSize];
			for(int i=0; i<nSize; i++)
			{
				rowMarginals[i] = nData.getRowMarginal(rIdx, i);
				colMarginals[i] = nData.getColMarginal(rIdx, i);
			}
			
			// set up temp structures
			rowFactor = new double[nSize];
			colFactor = new double[nSize];
			labeledBlockFactor = new double[numBlockLabels];
			blockPredictedTies = new double[numBlockLabels];
			matrixSums = new double[nSize][];
			for(int i=0; i<nSize; i++)
			{
				matrixSums[i] = new double[nSize];
			}
			
			// initialize factors to "bad values"
			noTieFactor = -6741.0;
			mutualFactor = -6741.0;
			offMutualFactor = -6741.0;
			
			// not done yet
			pctDone = 0.0;
			detailStr = "starting...";
			
			// one more thing
			g2values = allG2values[rIdx];
		}
		
		public Matrix[] getMatrices()
		{
			return myMatrices;
		}
		
		public int getNumStages()
		{
			return 5;
		}
		
		public void computeStageScaleFactors(int stage, Matrix[] matrices)
		{
			switch(stage)
			{
				case ROW_STEP:
					computeRowScaleFactors(matrices);
					return;
				
				case COL_STEP:
					computeColumnScaleFactors(matrices);
					return;
				
				case BLOCK_STEP:
					computeBlockScaleFactors(matrices);
					return;
				
				case MUTUAL_STEP:
					computeMutualScaleFactors(matrices);
					return;
				
				case NORMALIZING_STEP:
					computeNormalizingScaleFactors(matrices);
					return;
			}
		}
		
		protected void computeRowScaleFactors(Matrix[] matrices)
		{
			// compute row factors (E_i^{(n)})
			double totalPredictedTies = 0.0;
			for(int i=0; i<nSize; i++)
			{
				double predictedTies = 0.0; // p_{ij}
				for(int j=0; j<nSize; j++)
				{
					if(i != j)
					{
						predictedTies += matrices[MUTUALS].values[i][j] +
							matrices[ASYMMETRIC].values[i][j];
					}
				}
				totalPredictedTies += predictedTies;
				
				if(predictedTies != 0.0)
				{
					rowFactor[i] = rowMarginals[i] / predictedTies; 
				}
				else
				{
					rowFactor[i] = 1.0;
					// this seems to work ok
					// System.out.println("no predicted ties in row " + i);
				}
			}
			
			// compute null factor
			double predictedNoTies = maxSize - totalPredictedTies; // q++
			if(predictedNoTies != 0.0)
			{
				noTieFactor = totalNoTies / predictedNoTies;
			}
			else
			{
				noTieFactor = 1.0;
				System.err.println("no predicted no-ties in row step");
			}

			// System.out.print(fmt.format(rowFactor[0]) + " ");
		}
		
		protected void computeColumnScaleFactors(Matrix[] matrices)
		{
			// compute col factors (E_i^{(n)})
			double totalPredictedTies = 0.0;
			for(int j=0; j<nSize; j++)
			{
				double predictedTies = 0.0; // p_{ij}
				for(int i=0; i<nSize; i++)
				{
					if(i != j)
					{
						predictedTies += matrices[MUTUALS].values[i][j] +
							matrices[ASYMMETRIC].values[i][j];
					}
				}
				totalPredictedTies += predictedTies;
				
				if(predictedTies != 0.0)
				{
					colFactor[j] = colMarginals[j] / predictedTies; 
				}
				else
				{
					colFactor[j] = 1.0;
					// this seems to work ok
					// System.out.println("no predicted ties in column " + j);
				}
			}

			// compute null factor
			double predictedNoTies = maxSize - totalPredictedTies; // q++
			if(predictedNoTies != 0.0)
			{
				noTieFactor = totalNoTies / predictedNoTies;
			}
			else
			{
				noTieFactor = 1.0;
				System.err.println("no predicted no-ties in column step");
			}
		}
		
		protected void computeBlockScaleFactors(Matrix[] matrices)
		{
			double totalPredictedTies = 0.0;
			double unlabeledPredictedTies = 0.0;
			
			// zero out predicted ties
			for(int i=0; i<blockPredictedTies.length; i++)
			{
				blockPredictedTies[i] = 0.0;
			}
			
			for(int i=0; i<nSize; i++)
			{
				for(int j=0; j<nSize; j++)
				{
					if(i != j)
					{
						int blockLabel = blockLabels[i][j];
						double predictedTieValue = 
							matrices[MUTUALS].values[i][j] +
							matrices[ASYMMETRIC].values[i][j];
						if(blockLabel != -1)
						{
							blockPredictedTies[blockLabel] += predictedTieValue;
						}
						else
						{
							unlabeledPredictedTies += predictedTieValue;
						}
						
						// don't forget to add to total
						totalPredictedTies += predictedTieValue;
					}
				}
			}
			
			// compute scale factors for labeled blocks
			for(int k=0; k<groupMass.length; k++)
			{
				if(blockPredictedTies[k] != 0.0)
				{
					labeledBlockFactor[k] = groupMass[k] / blockPredictedTies[k];
				}
				else
				{
					labeledBlockFactor[k] = 1.0;
				}
			}
			
			// compute scale factor for unlabeled blocks
			if(unlabeledPredictedTies != 0.0)
			{
				unlabeledBlockFactor =unlabeledGroupMass / unlabeledPredictedTies;
			}
			else
			{
				unlabeledBlockFactor = 1.0;
			}

			// compute null factor
			double predictedNoTies = maxSize - totalPredictedTies;
			if(predictedNoTies != 0.0)
			{
				noTieFactor = totalNoTies / predictedNoTies;
			}
			else
			{
				noTieFactor = 1.0;
				System.err.println("no predicted nulls in column step");
			}
		}
				
		protected void computeMutualScaleFactors(Matrix[] matrices)
		{
			double predictedMutuals = 0.0;
			for(int i=0; i<nSize; i++)
			{
				for(int j=0; j<nSize; j++)
				{
					if(i != j)
					{
						predictedMutuals +=
							matrices[MUTUALS].values[i][j];
					}
				}
			}
			
			// compute mutual factor (H)
			if(predictedMutuals != 0.0)
			{
				mutualFactor = 2.0 * totalMutuals / predictedMutuals;
			}
			else
			{
				mutualFactor = 1.0;
				System.err.println("no predicted mutuals");
			}
			
			// compute off-mutual factor (M)
			double predictedOffMutuals = gChoose2 - (predictedMutuals/2.0);
			if(predictedOffMutuals != 0.0)
			{
				offMutualFactor = (gChoose2 - totalMutuals) / predictedOffMutuals;
			}
			else
			{
				offMutualFactor = 1.0;
				System.err.println("no predicted off mutuals");
			}
		}
		
		protected void computeNormalizingScaleFactors(Matrix[] matrices)
		{
			// matrixSums = R
			for(int i=0; i<nSize; i++)
			{
				for(int j=0; j<nSize; j++)
				{
					if(i != j)
					{
						matrixSums[i][j] = matrices[MUTUALS].values[i][j] +
						  matrices[ASYMMETRIC].values[i][j] +
						  matrices[ASYMMETRIC].values[j][i] +
						  matrices[NULL].values[i][j];
					}
				}
			}
		}
		
		public double getStageScaleFactor(int stage, int mIdx, int i, int j)
		{
			switch(stage)
			{
				case ROW_STEP:
					return getRowScaleFactor(mIdx, i, j);
				
				case COL_STEP:
					return getColumnScaleFactor(mIdx, i, j);
				
				case BLOCK_STEP:
					return getBlockScaleFactor(mIdx, i, j);
					// return 1.0;
				
				case MUTUAL_STEP:
					return getMutualScaleFactor(mIdx, i, j);
					// return 1.0;
					
				case NORMALIZING_STEP:
					return getNormalizingScaleFactor(mIdx, i, j);
			}
			throw new IllegalArgumentException("Stage " + stage + " not valid.");
		}

		protected double getRowScaleFactor(int mIdx, int i, int j)
		{
			if(i == j)
			{
				return 1.0; // don't scale diagonals
			}
			double _r;
			switch(mIdx)
			{
				case MUTUALS:
					_r = Math.sqrt(rowFactor[i] * rowFactor[j]);
					if(Double.isNaN(_r))
					{
						System.err.println("rowFactor["+i+"]="+rowFactor[i]);
						System.err.println("rowFactor["+j+"]="+rowFactor[j]);
					}
					return _r;
					// return Math.sqrt(rowFactor[i] * rowFactor[j]);

				case ASYMMETRIC:
					_r = Math.sqrt(rowFactor[i] * noTieFactor);
					if(Double.isNaN(_r))
					{
						System.err.println("rowFactor["+i+"]="+rowFactor[i]);
						System.err.println("noTieFactor="+noTieFactor);
					}
					return _r;
					// return Math.sqrt(rowFactor[i] * noTieFactor);

				case NULL:
					if(Double.isNaN(noTieFactor))
					{
						System.err.println("noTieFactor="+noTieFactor);
					}
					return noTieFactor;
			}
			throw new IllegalArgumentException("Matrix index: " + mIdx + " not valid.");
		}
		
		protected double getColumnScaleFactor(int mIdx, int i, int j)
		{
			if(i == j)
			{
				return 1.0; // don't scale diagonals
			}
			switch(mIdx)
			{
				case MUTUALS:
					return Math.sqrt(colFactor[i] * colFactor[j]);

				case ASYMMETRIC:
					return Math.sqrt(colFactor[j] * noTieFactor);

				case NULL:
					return noTieFactor;
			}
			throw new IllegalArgumentException("Matrix index: " + mIdx + " not valid.");
		}
		
		protected double getBlockScaleFactor(int mIdx, int i, int j)
		{
			if(i == j)
			{
				return 1.0; // don't scale diagonals
			}
			
			int outBlockLabel = blockLabels[i][j];
			int inBlockLabel  = blockLabels[j][i];
			
			double outBlockFactor = (outBlockLabel == -1) ?
				unlabeledBlockFactor : labeledBlockFactor[outBlockLabel];
			double inBlockFactor  = (inBlockLabel == -1) ?
				unlabeledBlockFactor : labeledBlockFactor[inBlockLabel];
			
			switch(mIdx)
			{
				case MUTUALS:
					return Math.sqrt(outBlockFactor * inBlockFactor);

				case ASYMMETRIC:
					return Math.sqrt(outBlockFactor * noTieFactor);
				
				case NULL:
					return noTieFactor;
			}
			throw new IllegalArgumentException("Matrix index: " + mIdx + " not valid.");
		}
		
		protected double getMutualScaleFactor(int mIdx, int i, int j)
		{
			if(i == j)
			{
				return 1.0; // don't scale diagonals
			}
			switch(mIdx)
			{
				case MUTUALS:
					return mutualFactor;

				case ASYMMETRIC:
				case NULL:
					return offMutualFactor;
			}
			throw new IllegalArgumentException("Matrix index: " + mIdx + " not valid.");
		}
		
		protected double getNormalizingScaleFactor(int mIdx, int i, int j)
		{
			if(i != j)
			{
				if(matrixSums[i][j] == 0.0)
				{
					 System.err.println("P1 Blockmodel -- Degenrated at: " +
					  "("+mIdx+", "+i+", "+j+")");
					 return 1.0;
				}
				else
				{
					return 1.0/matrixSums[i][j];
				}
			}
			else
			{
				return 1.0; // leave diagonal elements be...
			}
		}
		
		public boolean isFinished(Matrix[] matrices)
		{
			// calculate G^2
			double g2 = 0.0;
			for(int i=0; i<nSize; i++)
			{
				for(int j=0; j<nSize; j++)
				{
					if(i != j)
					{
						double ts = r.getTieStrength(i, j);
						double p_hat = matrices[MUTUALS].values[i][j] +
							matrices[ASYMMETRIC].values[i][j];

						// don't get into situation where we take log 0 = -Inf
						// and try to multiply that by 0, or where p_hat = 0
						// so we divide by zero.  might want to log exception
						// if that is the case, though...
						if((ts != 0.0) && (p_hat != 0.0))
						{
							double loggable = ts / p_hat;
							if(loggable < 0.0)
							{
								System.err.println("for ("+i+", "+j+") ts=" + ts + " p_hat=" + p_hat);
								return true; // throw?
							}
							g2 += ts * Math.log(ts / p_hat);
							if(Double.isNaN(g2))
							{
								System.err.println("at ("+i+", "+j+") ts=" + ts + " p_hat=" + p_hat +	
									" Mutuals="+matrices[MUTUALS].values[i][j] + 
									" Asym="+matrices[ASYMMETRIC].values[i][j]);
								System.exit(1);
								return true; // throw?
							}
						}
					}
				}
			}
			
			// eventually add this to the 'report'
			g2values.addElement(new Double(g2));
			System.out.println("G^2 = " + g2);
  		  		
	  		if(Double.isNaN(oldG2))
	  		{
	  			oldG2 = g2;
	  			return false;
	  		}
  		
			double diff = Math.abs(oldG2 - g2);
			if(diff < tolerance)
			{
				oldG2 = g2;
				return true;
			}
  		
			// update 'percent done'
			pctDone = tolerance / diff;
			detailStr = "G2 [" + diff + "/" + tolerance + "]";
			System.out.println(detailStr);
			
			oldG2 = g2;
			return false;
		}
		
		public double getPercentDone(Matrix[] matrices)
		{
			return pctDone;
		}
		
		public String getProgressDetail(Matrix[] matrices)
		{
			return detailStr;
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
	  	return (idx==0) ? "alpha" : "beta";
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