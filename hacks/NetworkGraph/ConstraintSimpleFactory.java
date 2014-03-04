/**
 * ConstraintSimple.java
 *
 *
 */

package com.wibinet.networks;

import javax.swing.JFrame;

import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;

import com.wibinet.math.Graph;
import com.wibinet.math.ListGraph;


public class ConstraintSimpleFactory implements NodeEvaluatorFactory {
	
	protected int nsteps;
	
	public ConstraintSimpleFactory() throws IOException
	{
	}
	
	
	public NodeEvaluator newInstance() {
		return new Evaluator();
	}

	public void edit(NodeEvaluator evaluator, JFrame parent) {

	}

	public Class getEvaluatorClass() {
		return Double.class;
	}

	public String getName() {
		return "Constraint";
	}

	public String getGroup() {
		return "Constraint";
	}

	public boolean isMultiple() {
		return false;
	}

	class Evaluator implements NodeEvaluator {
		protected NetworkData nd;
		protected String name;
		protected int activeRelIdx;
		protected int totalNodes;
		protected Relation[] numRelations;
		double[] constraintAry;
		Relation reference;
		ListGraph lg;
		HashMap<Integer, int[]> nMap;
		HashMap<Integer, int[]> nOutMap;
		HashMap<Integer, Double> norm;
		public Evaluator() {
			this.nd = null;
			this.name = "Constraint";
			this.activeRelIdx = 0;
		}

		public void runEvaluator() {
			reference = numRelations[0];
			
			for (int ri = 0; ri < numRelations.length; ri++) {
				Relation r = numRelations[ri];
				Graph g = r.getGraph();
				lg = null;
				if (g instanceof ListGraph) {
					lg = (ListGraph) g;
				} else {
					lg = new ListGraph(g, true);
				}
				lg.setDirect(true);
				
				
				totalNodes = lg.getNodeCount();
				constraintAry = new double[totalNodes];
				
				nMap = new HashMap<Integer, int[]>();
				nOutMap = new HashMap<Integer, int[]>();
				norm = new HashMap<Integer, Double>();
				for(int i =0; i <totalNodes; i++)
				{
					nMap.put(new Integer(i), lg.getIntNeighborhood(i));
					nOutMap.put(new Integer(i), lg.getOutNeighborhood(i));
					norm.put(new Integer(i), new Double(normalize(i)));
				}
				
				
				for (int n1= 0; n1 < totalNodes; n1++) {
					constraintAry[n1] = findConstraint(n1);
				
				}
			}
		}
		
		public double findConstraint(int i)
		{
			double CI = 0.0;
			int[] iOutNeighbors = nMap.get(i);
			double norm_i = norm.get(new Integer(i)).doubleValue();
			if(i == 15 || i == 7 || i ==12 || i ==10 || i ==1358 || i == 2529 || i ==2384 || i ==1474){
			//	System.out.println(i+ " norm: "+ norm_i);
			}
			
			for(int j =0; j< iOutNeighbors.length; j++)
			{
				double pij =(propStrength(i, iOutNeighbors[j]) +propStrength(iOutNeighbors[j], i))/norm_i; 
				if (i == 15){
				//System.out.println(iOutNeighbors[j]+ " pij  "+ pij );
				}
				CI += Math.pow( pij+ PIQQJ(i, iOutNeighbors[j], norm_i), 2);
			}
			return CI;
			
		}
		
		public double PIQQJ(int i, int j, double norm_i )
		
		{
			ArrayList<Integer> common = findCommon(i, j);
			double PIQQJ = 0.0;
			
			if(common.size() >0){
			if(i == 6){
			//	System.out.println(" common size: "+ i +" " + j+" "+common.size() + " " + common.get(0).intValue());
			}
			for(int count =0; count<common.size(); count++)
			{
				
				int k = common.get(count).intValue();
				double norm_k = norm.get(new Integer(k)).doubleValue();
				double pik = (propStrength(i, k) + propStrength(k,i))/norm_i;
				double pkj = (propStrength(k, j) + propStrength(j, k))/norm_k;
				PIQQJ += pik*pkj;
			//	if(i ==15) System.out.println(i+" " + k+ " " +j +" "+ pik +" "+ pkj+ " "+ PIQQJ);
				
			}
			}
			
		return PIQQJ;
		}
		
		
		
		public ArrayList<Integer> findCommon(int k, int j)
		{
		int[] kneighbors = nMap.get(k);
		int[] jneighbors = nMap.get(j);
		
		ArrayList<Integer> list = new ArrayList<Integer>();
		HashSet<Integer> common = new HashSet<Integer>();
		for(int i =0; i<kneighbors.length; i++) common.add(new Integer(kneighbors[i]));
		for(int i=0; i<jneighbors.length; i++) {if(!common.add(jneighbors[i])) list.add(new Integer(jneighbors[i]));}
		//int[] result = new int[list.size()];
	//	for(int i=0; i< list.size(); i++) result[i] = list.get(i).intValue();
		
		return list;
		}
		
		public double normalize(int i)
		
		{
			int[] n = nMap.get(i);
			
			double sum =0.0;
			for(int index =0; index<n.length; index++)
			{ 
				sum += propStrength(i, n[index]) + propStrength(n[index], i);
			}
			return sum;
		}
		
		public void setNetwork(NetworkData nd) {
			this.nd = nd;
			this.numRelations = new Relation[nd.getRelationCount()];
			
			for(int ri =0; ri< numRelations.length; ri++)
			numRelations[ri] = nd.getRelation(ri);
		}
		
		public double propStrength(int n1, int n2){
			if(lg.hasRelation(n1, n2)) return 1.0;
			else return 0.0;
		}

		public Object evaluateNode(int idx) {
			return new Double(constraintAry[idx]);
		}

		public Object evaluateNode(int idx, int ri) {
			return new Double(constraintAry[idx]);
		}

		public String getName() {
			return name;
		}

		public NodeEvaluatorFactory getFactory() {
			return ConstraintSimpleFactory.this;
		}
	}

}
