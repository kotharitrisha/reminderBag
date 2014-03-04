/**
 * GATreeOptimizer.java
 *
 * (c) 2002 Wibi Internet
 */

package com.wibinet.networks;

import java.awt.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import com.wibinet.ai.core.*;
import com.wibinet.ai.datatypes.*;
import com.wibinet.ai.ga.*;
import com.wibinet.gui.*;
import com.wibinet.math.BasicPartition;
import com.wibinet.math.Partition;
import com.wibinet.math.Permutations;

public class GATreeOptimizer implements ModelOptimizer
{
 	protected Hashtable props;
 	
 	// properties...
 	protected final static String POP_SIZE = "populationSize";
 	protected final static String MUTATE_PROB = "mutateProbability";
 	protected final static String CLONE_PROB = "cloneProbability";
 	protected final static String SCORE_POWER = "scorePower";
 	protected final static String SCALE_FITNESS = "scaleFitness";
	
	protected final static String SPLIT_PROB = "splitProbability";
	protected final static String MIX_PROB = "mixProbability";
	protected final static String MATCH_PROB = "matchProbability";
	protected final static String MAJ_PROB = "majorityProbability";
	
	protected final static Random random = new Random();

	protected final static NumberFormat fmt;
	
	static
	{
		fmt = NumberFormat.getInstance();
		fmt.setMaximumFractionDigits(3);
		fmt.setMinimumFractionDigits(3);
	}

	public GATreeOptimizer()
  {
 	  props = new Hashtable();
 	  props.put(POP_SIZE, new Integer(64));
 	  props.put(MUTATE_PROB, new Double(0.02));
 	  props.put(CLONE_PROB, new Double(0.10));
 	  props.put(SCORE_POWER, new Double(1.0));
 	  props.put(SCALE_FITNESS, new Boolean(false));
		props.put(SPLIT_PROB, new Double(0.55));
		props.put(MATCH_PROB, new Double(0.19));
		props.put(MIX_PROB, new Double(0.13));
		props.put(MAJ_PROB, new Double(0.8));
  }

	protected int getIntProperty(String name)
	{
		return ((Integer)props.get(name)).intValue();
	}
	
	public String[] getPropertyNames()
	{
		Enumeration keys = props.keys();
		String[] names = new String[props.size()];
		for(int ni=0; ni<names.length; ni++)
		{
			names[ni] = (String)keys.nextElement();
		}
		return names;
	}
	
	public Object getProperty(String name)
	{
		return props.get(name);
	}
	
	public void setProperty(String name, Object value)
	{
		props.put(name, value);
	}

	public Blockmodel optimize(NetworkData nData, ModelMeasure measure,
														 BlockmodelFactory mFactory)
  {
  	TreeProblem problem = new TreeProblem(nData, measure, mFactory);
  	TerminatorDialog terminator = new TerminatorDialog(null, 
  	  "Genetic Algorithm Progress", problem.getDisplay());
  	GASolver solver = new GASolver(problem, terminator, true);
		
  	// try to set rest of properties...
  	try
  	{
  		problem.setScorePower(((Double)props.get(SCORE_POWER)).doubleValue());
  		problem.setSplitProbability(((Double)props.get(SPLIT_PROB)).doubleValue());
  		problem.setMixProbability(((Double)props.get(MIX_PROB)).doubleValue());
  		problem.setMatchProbability(((Double)props.get(MATCH_PROB)).doubleValue());
  		problem.setPopulationSize(((Integer)props.get(POP_SIZE)).intValue());
  		solver.setMutateProbability(((Double)props.get(MUTATE_PROB)).doubleValue());
  		solver.setCloneProbability(((Double)props.get(CLONE_PROB)).doubleValue());
  		solver.setScaleFitness(((Boolean)props.get(SCALE_FITNESS)).booleanValue());
  	}
 		catch(Throwable t)
 		{
 		  // ignore failures...
			System.out.println("throwable: " + t);
			t.printStackTrace();
 		}
		
  	solver.start();
  	terminator.setVisible(true); // center?
  	
  	// wait for it to finish...
  	
  	// get best tree
  	TNode bestTree = (TNode)solver.getBestGene();
		Partition p = bestTree.getPartition();
		Blockmodel bestM = mFactory.newInstance(nData, p);
  	return bestM;
	}
	
	public String toString()
	{
		return "Genetic Program";
	}
	
	private String bitsetToString(BitSet bs, int nSize)
	{
		StringBuffer buf = new StringBuffer("[");
		for(int i=0; i<nSize; i++)
		{
			buf.append(bs.get(i) ? "1" : "0");
		}
		buf.append("]");
		return buf.toString();
	}
	
	protected class TreeProblem implements GAProblem
	{
		protected NetworkData nData;
		protected int nSize;
		protected int popSize;
		protected ModelMeasure measure;
		protected BlockmodelFactory mFactory;
		protected double bestScore, worstScore;
		protected double scorePower;
		
		// generator probabilities (tree prob is implied)
		protected double splitProb = 0.55;
		protected double mixProb = 0.13;
		protected double matchProb = 0.19;
		protected double majProb = 0.02;
		
		protected JMultiTicker ticker;
		
		public TreeProblem(NetworkData nData, ModelMeasure measure,
			BlockmodelFactory mFactory)
		{
			this.nData = nData;
			this.nSize = nData.getSize();
			this.popSize = 64;
			this.measure = measure;
			this.mFactory = mFactory;
			this.scorePower = 1.0;
			
			this.ticker = new JMultiTicker();
			ticker.setForegroundColor(new Color(0, 0, 128, 64));

			// get best and worst score
			this.bestScore  = measure.getBestScore();
			this.worstScore = measure.getWorstScore();
		}
		
		public void setScorePower(double scorePower)
		{
			this.scorePower = scorePower;
		}
		
		public void setSplitProbability(double splitProb)
		{
			this.splitProb = splitProb;
		}
		
		public void setMixProbability(double mixProb)
		{
			this.mixProb = mixProb;
		}
		
		public void setMatchProbability(double matchProb)
		{
			this.matchProb = matchProb;
		}
		
		public void setPopulationSize(int popSize)
		{
			this.popSize = popSize;
		}
 	
		public GeneString generateRandom()
		{
			return getRandomTree(3);
		}
		
		public GeneString[] generatePopulation()
		{
			// problem size?
			GeneString[] pop = new GeneString[popSize];
			for(int i=0; i<pop.length; i++)
			{
				pop[i] = generateRandom();
			}
			return pop;
		}
		
		protected TNode getRandomTree(int maxDepth)
		{
			if(maxDepth == 1)
			{
				// must return a leaf
				Leaf leaf = new Leaf(nSize);
				leaf.randomize();
				return leaf;
			}
			double typeRandom = random.nextDouble();
			if(typeRandom < splitProb)
			{
				Leaf leaf = new Leaf(nSize);
				leaf.randomize();
				return leaf;
			}
			else if(typeRandom < splitProb + mixProb)
			{
				TNode lChild = getRandomTree(maxDepth-1);
				TNode rChild = getRandomTree(maxDepth-1);
				TNode mixer = getRandomTree(maxDepth-1);
				MixNode mixNode = new MixNode(lChild, rChild, mixer, nSize);
				return mixNode;
			}
			else if(typeRandom < splitProb + mixProb + matchProb)
			{
				TNode lChild = getRandomTree(maxDepth-1);
				TNode rChild = getRandomTree(maxDepth-1);
				MatchNode matchNode = new MatchNode(lChild, rChild, nSize);
				return matchNode;
			}
			else if(typeRandom < splitProb + mixProb + matchProb + majProb)
			{
				TNode[] children = new TNode[3];
				children[0] = getRandomTree(maxDepth-1);
				children[1] = getRandomTree(maxDepth-1);
				children[2] = getRandomTree(maxDepth-1);
				MajorityNode majorityNode = new MajorityNode(children, nSize);
				return majorityNode;
			}
			else
			{
				TNode lChild = getRandomTree(maxDepth-1);
				TNode rChild = getRandomTree(maxDepth-1);
				Tree tree = new Tree(lChild, rChild, nSize);
				return tree;
			}
		}

		public GeneString crossOver(GeneString gs1, GeneString gs2)
		{
			TNode node1 = (TNode)gs1;
			TNode node2 = (TNode)gs2;
			
			// create node arrays
			TNode[] arr1 = node1.getNodeArray();
			TNode[] arr2 = node2.getNodeArray();
			
			// pick cross-over point
			// int cPoint1 = (Math.abs(random.nextInt()) % (arr1.length + 1)) - 1;
			int cPoint1 = Math.abs(random.nextInt()) % arr1.length;
			int cPoint2 = Math.abs(random.nextInt()) % arr2.length;
			
			// see if we picked the 'pre-root'
			/*if(cPoint1 == -1)
			{
				TNode newTree;
				if(random.nextDouble() < mixProb)
				{
					MixNode newMix = new MixNode(node1.copy(), arr2[cPoint2].copy(), nSize);
					newMix.randomize();
					newTree = newMix;
					// System.out.println("[m] " + nodeToString(node1) + " x " + 
					// 	 nodeToString(node2) + " = " + nodeToString(newTree));
				}
				else
				{
					newTree = new Tree(node1.copy(), arr2[cPoint2].copy(), nSize);
					// System.out.println("[t] " + nodeToString(node1) + " x " + 
					// 	 nodeToString(node2) + " = " + nodeToString(newTree));
				}
				return newTree;
			}*/
			
			TNode newTree = node1.copyAndReplace(arr1[cPoint1], arr2[cPoint2].copy());
			// System.out.println(nodeToString(node1) + " x " + 
			// 	 nodeToString(node2) + " = " + nodeToString(newTree));
			return newTree;
		}
		
		public GeneString mutate(GeneString gs)
		{
			TNode node = (TNode)gs;
			TNode[] arr = node.getNodeArray();
			int replacePt = Math.abs(random.nextInt()) % arr.length;
			
			TNode newNode = getRandomTree(3);
			return node.copyAndReplace(arr[replacePt], newNode);
		}
		
		public double getFitness(GeneString gs)
		{
			TNode node = (TNode)gs;
			Partition p = node.getPartition();
			
			// normalize this
			((BasicPartition)p).normalize();
			
			Blockmodel model = mFactory.newInstance(nData, p);
			
			double rawScore = measure.getScore(model);

			// normalize against best and worst, such that 1.0 is
			// a good score and 0.0 is a bad score
			double normScore = (rawScore - worstScore) / (bestScore - worstScore);
			
			// dump it
			// System.out.println(nodeToString(node) + ": " + 
			// 	 fmt.format(rawScore) + "/" + fmt.format(normScore));
			
			// cap...perhaps should register warning...
			if(normScore > 1.0)
			{
				normScore = 1.0;
			}
			if(normScore < 0.0)
			{
				normScore = 0.0;
			}
			return Math.pow(normScore, scorePower); 
		}

		public JComponent getDisplay()
		{
			return ticker;
		}
		
		public void display(GeneString[] population, double[] fitness)
		{
			ticker.addPoints(fitness);
			return;
		}

		private String nodeToString(TNode node)
		{
			Partition p = node.getPartition();
			StringBuffer b = new StringBuffer("[");
			for(int i=0; i<nSize; i++)
			{
				b.append(" " + p.getPartition(i));
			}
			b.append("]");
			return b.toString();
		}
	}

	protected interface TNode extends GeneString
  {
		public Partition getPartition();
		public int getPartitionCount();
		public TNode[] getNodeArray();
		public TNode copy();
		public TNode copyAndReplace(TNode srcNode, TNode newNode);
  }

	protected class Leaf implements TNode
	{
		protected BitSet ingroup;
		protected int nSize;
		
		public Leaf(int nSize)
		{
			this.nSize = nSize;
			ingroup = new BitSet(nSize);
		}
		
		public void randomize()
		{
			for(int i=0; i<nSize; i++)
			{
				if(random.nextBoolean())
				{
					ingroup.set(i);
				}
				else
				{
					ingroup.clear(i);
				}
			}
		}
		
		public Partition getPartition()
		{
			BasicPartition p = new BasicPartition(nSize);
			for(int i=0; i<nSize; i++)
			{
				if(ingroup.get(i))
				{
					p.setPartition(i, 1);
				}
				else
				{
					p.setPartition(i, 0);
				}
			}
			return p;
		}

		public int getPartitionCount()
		{
			return 2;
		}

		public TNode[] getNodeArray()
		{
			TNode[] array = new TNode[1];
			array[0] = this;
			return array;
		}
		
		public TNode copy()
		{
			Leaf l = new Leaf(nSize);
			l.ingroup.or(this.ingroup); // copy?
			return l;
		}

		public TNode copyAndReplace(TNode srcNode, TNode newNode)
		{
			if(equals(srcNode))
			{
				return newNode;
			}
			else
			{
				return copy();
			}
		}
		
		public String toString()
		{
			return bitsetToString(ingroup, nSize);
		}
	}
	
	protected class MixNode implements TNode
	{
		protected TNode lChild;
		protected TNode rChild;
		protected TNode mixer;
		protected int nSize;
		
		public MixNode(TNode lChild, TNode rChild, TNode mixer, int nSize)
		{
			this.lChild = lChild;
			this.rChild = rChild;
			this.mixer = mixer;
			this.nSize = nSize;
		}
		
		public Partition getPartition()
		{
			Partition lp = lChild.getPartition();
			Partition rp = rChild.getPartition();
			Partition mp = mixer.getPartition();
			BasicPartition p = new BasicPartition(nSize);
			int rCt = rChild.getPartitionCount();
			for(int i=0; i<nSize; i++)
			{
				// evens to the left, odds to the right
				p.setPartition(i, ((mp.getPartition(i) & 1) == 0) ?
					lp.getPartition(i) : rp.getPartition(i));
			}
			return p;
		}

		public int getPartitionCount()
		{
			int lCt = lChild.getPartitionCount();
			int rCt = rChild.getPartitionCount();
			return Math.max(lCt, rCt);
		}

		public TNode[] getNodeArray()
		{
			TNode[] lArray = lChild.getNodeArray();
			TNode[] rArray = rChild.getNodeArray();
			TNode[] mArray = mixer.getNodeArray();
			TNode[] array = new TNode[lArray.length + rArray.length + mArray.length + 1];
			array[0] = this;
			System.arraycopy(lArray, 0, array, 1, lArray.length);
			System.arraycopy(rArray, 0, array, 1 + lArray.length, rArray.length);
			System.arraycopy(mArray, 0, array, 1 + lArray.length + rArray.length,
				mArray.length);
			return array;
		}

		public TNode copy()
		{
			TNode lCopy = lChild.copy();
			TNode rCopy = rChild.copy();
			TNode mCopy = mixer.copy();
			MixNode newNode = new MixNode(lCopy, rCopy, mCopy, nSize);
			return newNode;
		}

		public TNode copyAndReplace(TNode srcNode, TNode newNode)
		{
			if(equals(srcNode))
			{
				return newNode;
			}
			else
			{
				TNode lCopy = lChild.copyAndReplace(srcNode, newNode);
				TNode rCopy = rChild.copyAndReplace(srcNode, newNode);
				TNode mCopy = mixer.copyAndReplace(srcNode, newNode);
				TNode newMixer = new MixNode(lCopy, rCopy, mCopy, nSize);
				return newMixer;
			}
		}
		
		public String toString()
		{
			return getPartition().toString() + 
				" = (mix " + lChild + " " + rChild + " " + mixer + ")";
		}
	}
	
	protected class MatchNode implements TNode
	{
		protected TNode lChild;
		protected TNode rChild;
		protected int nSize;
		
		public MatchNode(TNode lChild, TNode rChild, int nSize)
		{
			this.lChild = lChild;
			this.rChild = rChild;
			this.nSize = nSize;
		}
		
		public Partition getPartition()
		{
			// how many partitions in each kid?
			int lpCt = lChild.getPartitionCount();
			int rpCt = rChild.getPartitionCount();
			
			// switch so that right child has most partitions
			if(rpCt < lpCt)
			{
				TNode tmp = rChild;
				rChild = lChild;
				lChild = tmp;
				
				int tCt = rpCt;
				rpCt = lpCt;
				lpCt = tCt;
			}

			Partition lp = lChild.getPartition();
			Partition rp = rChild.getPartition();
			
			// array these up for quick access
			int[] lpa = new int[nSize];
			int[] rpa = new int[nSize];
			for(int ai=0; ai<nSize; ai++)
			{
				lpa[ai] = lp.getPartition(ai);
				rpa[ai] = rp.getPartition(ai);
			}
			
			// if this is small enough, do some permutation matching
			int[] bestPermSet;
			if(rpCt <= 6)
			{
				// get a permutation set for right kid
				int[][] permSet = Permutations.getPermutationSet(rpCt);
				
				// remember matches for each permutation
				int[] pMatches = new int[permSet.length];
				
				int mostMatches = 0;
				int bestPermIdx = 0;
				for(int pi=0; pi<permSet.length; pi++)
				{
					// count matches using this permutation of right child
					// note that permutations are 1-based and partitions
					// are 0-based
					int matchCt = 0;
					for(int ai=0; ai<nSize; ai++)
					{
						if(lpa[ai] == permSet[pi][rpa[ai]]-1)
						{
							matchCt++;
						}
					}
					pMatches[pi] = matchCt;
					
					// is this the max?
					if(matchCt > mostMatches)
					{
						mostMatches = matchCt;
						bestPermIdx = pi;
					}
				}
				bestPermSet = permSet[bestPermIdx];
			}
			else
			{
				bestPermSet = new int[rpCt];
				for(int pi=0; pi<rpCt; pi++)
				{
					bestPermSet[pi] = pi+1;
				}
			}
			
			// put 'non-matching' actors into lowest
			// unused partition
			int newP = Math.max(lpCt, rpCt);
			
			BasicPartition p = new BasicPartition(nSize);
			for(int i=0; i<nSize; i++)
			{
				// again, bestPermSet is 1-based, need to make 0-based
				if(lpa[i] == bestPermSet[rpa[i]]-1)
				{
					// if they're the same, keep partition
					p.setPartition(i, lpa[i]);
				}
				else
				{
					// otherwise, set to unique partition
					p.setPartition(i, newP);
				}
			}
			return p;
		}

		public int getPartitionCount()
		{
			int lCt = lChild.getPartitionCount();
			int rCt = rChild.getPartitionCount();
			return Math.max(lCt, rCt) + 1;
		}

		public TNode[] getNodeArray()
		{
			TNode[] lArray = lChild.getNodeArray();
			TNode[] rArray = rChild.getNodeArray();
			TNode[] array = new TNode[lArray.length + rArray.length + 1];
			array[0] = this;
			System.arraycopy(lArray, 0, array, 1, lArray.length);
			System.arraycopy(rArray, 0, array, 1 + lArray.length, rArray.length);
			return array;
		}

		public TNode copy()
		{
			TNode lCopy = lChild.copy();
			TNode rCopy = rChild.copy();
			MatchNode newNode = new MatchNode(lCopy, rCopy, nSize);
			return newNode;
		}

		public TNode copyAndReplace(TNode srcNode, TNode newNode)
		{
			if(equals(srcNode))
			{
				return newNode;
			}
			else
			{
				TNode lCopy = lChild.copyAndReplace(srcNode, newNode);
				TNode rCopy = rChild.copyAndReplace(srcNode, newNode);
				TNode newMatch = new MatchNode(lCopy, rCopy, nSize);
				return newMatch;
			}
		}
		
		public String toString()
		{
			return getPartition().toString() + " = (match " + lChild + " " + rChild + ")";
		}
	}
	
	protected class MajorityNode implements TNode
	{
		protected TNode children[];
		protected int nSize;
		
		public MajorityNode(TNode[] children, int nSize)
		{
			this.children = children;
			this.nSize = nSize;
		}
		
		public Partition getPartition()
		{
			// simple majority voting...don't bother with
			// permutations...could quickly explode.
			
			Partition[] cp = new Partition[children.length];
			int pMax = 0;
			for(int pi=0; pi<cp.length; pi++)
			{
				cp[pi] = children[pi].getPartition();
				int pCt = children[pi].getPartitionCount();
				if(pCt > pMax)
				{
					pMax = pCt;
				}
			}
			
			int[] votes = new int[pMax];
			
			BasicPartition p = new BasicPartition(nSize);
			for(int i=0; i<nSize; i++)
			{
				for(int vi=0; vi<pMax; vi++)
				{
					votes[vi] = 0;
				}
				
				for(int ci=0; ci<cp.length; ci++)
				{
					votes[cp[ci].getPartition(i)]++; // tally vote
				}
				
				int bestP = 0;
				int maxVotes = 0;
				for(int vi=0; vi<pMax; vi++)
				{
					if(votes[vi] > maxVotes)
					{
						bestP = vi;
						maxVotes = votes[vi];
					}
				}
				p.setPartition(i, bestP);
			}
			return p;
		}

		public int getPartitionCount()
		{
			int maxP = children[0].getPartitionCount();
			for(int ci=1; ci<children.length; ci++)
			{
				int pCt = children[ci].getPartitionCount();
				if(pCt > maxP)
				{
					maxP = pCt;
				}
			}
			return maxP;
		}

		public TNode[] getNodeArray()
		{
			int numNodes = 1;
			TNode[][] subNodes = new TNode[children.length][];
			for(int ci=0; ci<children.length; ci++)
			{
				subNodes[ci] = children[ci].getNodeArray();
				numNodes += subNodes[ci].length;
			}
			TNode[] array = new TNode[numNodes];
			array[0] = this;
			int startPos = 1;
			for(int ci=0; ci<children.length; ci++)
			{
				System.arraycopy(subNodes[ci], 0, array, startPos, subNodes[ci].length);
				startPos += subNodes[ci].length;
			}
			return array;
		}

		public TNode copy()
		{
			TNode[] nodeCopies = new TNode[children.length];
			for(int ci=0; ci<children.length; ci++)
			{
				nodeCopies[ci] = children[ci].copy();
			}
			MajorityNode newNode = new MajorityNode(nodeCopies, nSize);
			return newNode;
		}

		public TNode copyAndReplace(TNode srcNode, TNode newNode)
		{
			if(equals(srcNode))
			{
				return newNode;
			}
			else
			{
				TNode[] nodeCopies = new TNode[children.length];
				for(int ci=0; ci<children.length; ci++)
				{
					nodeCopies[ci] = children[ci].copyAndReplace(srcNode, newNode);
				}
				TNode newMajority = new MajorityNode(nodeCopies, nSize);
				return newMajority;
			}
		}
		
		public String toString()
		{
			StringBuffer buf = new StringBuffer(getPartition().toString() + 
				" = (maj");
			for(int ci=0; ci<children.length; ci++)
			{
				buf.append(" " + children[ci]);
			}
			buf.append(")");
			return buf.toString();
		}
	}

	protected class Tree implements TNode
	{
		protected TNode lChild;
		protected TNode rChild;
		protected int nSize;
		
		public Tree(TNode lChild, TNode rChild, int nSize)
		{
			this.lChild = lChild;
			this.rChild = rChild;
			this.nSize = nSize;
		}
		
		public TNode getLChild()
		{
			return lChild;
		}
		
		public TNode getRChild()
		{
			return rChild;
		}
		
		public Partition getPartition()
		{
			Partition lp = lChild.getPartition();
			Partition rp = rChild.getPartition();
			BasicPartition p = new BasicPartition(nSize);
			int rCt = rChild.getPartitionCount();
			for(int i=0; i<nSize; i++)
			{
				p.setPartition(i, lp.getPartition(i) * rCt + rp.getPartition(i));
			}
			return p;
		}
		
		protected void normalize(Partition p)
		{
			// create inverted permutation indces array
			// (note that this is a different use of pIndices than in P1Blockmodel)
			int[] pIndices = new int[nSize];
			pIndices[0] = p.getPartition(0);
			int pMax = pIndices[0];
			for(int i=1; i<nSize; i++)
			{
				pIndices[i] = p.getPartition(i);
				if(pIndices[i] > pMax)
				{
					pMax = pIndices[i];
				}
			}
			int[] invertedPIndices = new int[pMax+1];
			for(int i=0; i<invertedPIndices.length; i++)
			{
				invertedPIndices[i] = -6741; // make sure it's something unlikely
			}
			for(int i=0; i<nSize; i++)
			{
				invertedPIndices[pIndices[i]] = i;
			}
			
			// run through and relabel partitions
			for(int i=0; i<nSize; i++)
			{
				int oldLabel = pIndices[i];
				int newLabel = invertedPIndices[oldLabel];
				p.setPartition(i, newLabel);
			}
		}

		public int getPartitionCount()
		{
			int lCt = lChild.getPartitionCount();
			int rCt = rChild.getPartitionCount();
			return lCt * rCt;
		}

		public TNode[] getNodeArray()
		{
			TNode[] lArray = lChild.getNodeArray();
			TNode[] rArray = rChild.getNodeArray();
			TNode[] array = new TNode[lArray.length + rArray.length + 1];
			array[0] = this;
			System.arraycopy(lArray, 0, array, 1, lArray.length);
			System.arraycopy(rArray, 0, array, 1 + lArray.length, rArray.length);
			return array;
		}

		public TNode copy()
		{
			TNode lCopy = lChild.copy();
			TNode rCopy = rChild.copy();
			TNode newTree = new Tree(lCopy, rCopy, nSize);
			return newTree;
		}

		public TNode copyAndReplace(TNode srcNode, TNode newNode)
		{
			if(equals(srcNode))
			{
				return newNode;
			}
			else
			{
				TNode lCopy = lChild.copyAndReplace(srcNode, newNode);
				TNode rCopy = rChild.copyAndReplace(srcNode, newNode);
				return new Tree(lCopy, rCopy, nSize);
			}
		}
		
		public String toString()
		{
			return getPartition().toString() + " = (tree " + lChild + " " + rChild + ")";
		}
	}
}