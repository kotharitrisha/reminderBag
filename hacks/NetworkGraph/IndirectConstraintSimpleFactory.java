/**
 * Constraint.java
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


public class IndirectConstraintSimpleFactory implements NodeEvaluatorFactory {
	
	protected int nsteps;
	
	public IndirectConstraintSimpleFactory() throws IOException
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
		ArrayList<Double>[] friendChar;
		Relation reference;
		ListGraph lg;
		HashMap<Integer, int[]> nMap;

		NodeEvaluator srEval;
		public Evaluator() {
			this.nd = null;
			this.name = "IndirectConstraint";
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
				try{
					ConstraintSimpleFactory sr = 		new ConstraintSimpleFactory();
				srEval = sr.newInstance();
				srEval.setNetwork(this.nd);
				srEval.runEvaluator();
				}
				catch (IOException e){
					e.printStackTrace();
				}
				totalNodes = lg.getNodeCount();
				friendChar = (ArrayList<Double>[])new ArrayList[totalNodes];
				//neighborhood map
				nMap = new HashMap<Integer, int[]>();
				
				for(int i =0; i <totalNodes; i++)
				{
					nMap.put(new Integer(i), lg.getIntNeighborhood(i));
				}
				
				
				for (int n1= 0; n1 < totalNodes; n1++) {
					friendChar[n1] = findIndirectConstraint(n1);
				
				}
			}
		}
		
		public ArrayList<Double> findIndirectConstraint(int i)
		{
	
			int[] iNeighbors = nMap.get(i);
			
			double sumConstraint= 0;
			double minConstraint =0;
			for(int j =0; j< iNeighbors.length; j++)
			{
			    Object val = srEval.evaluateNode(iNeighbors[j]); 
			    sumConstraint += ((Double)val).doubleValue();
			    minConstraint = Math.min(((Double)val).doubleValue(), minConstraint);
			}
			double avgConstr = 0;
			if (iNeighbors.length>0) avgConstr =sumConstraint / iNeighbors.length;
			ArrayList<Double> friends = new ArrayList<Double>();
			friends.add(new Double(avgConstr));
			friends.add(new Double(minConstraint));
			return friends;
			
		}
		
		
		
		
		public void setNetwork(NetworkData nd) {
			this.nd = nd;
			this.numRelations = new Relation[nd.getRelationCount()];
			
			for(int ri =0; ri< numRelations.length; ri++)
			numRelations[ri] = nd.getRelation(ri);
		}
		
		public double propStrength(int n1, int n2){
			
			return nd.getRelValues(n1, n2);
		}

		public Object evaluateNode(int idx) {
			return new ArrayList<Double>(friendChar[idx]);
		}

		public Object evaluateNode(int idx, int ri) {
			return new ArrayList<Double>(friendChar[idx]);
		}

		public String getName() {
			return name;
		}

		public NodeEvaluatorFactory getFactory() {
			return IndirectConstraintSimpleFactory.this;
		}
	}

}
