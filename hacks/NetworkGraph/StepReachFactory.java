/**
 * 2StepReachFactory.java
 *
 *
 */

package com.wibinet.networks;

import javax.swing.JFrame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.wibinet.math.Graph;
import com.wibinet.math.ListGraph;

import java.util.TreeSet;

public class StepReachFactory implements NodeEvaluatorFactory {
	
	protected boolean bidirect;
	protected int nsteps;
	
	public StepReachFactory(){}
	
	public StepReachFactory(String nsteps, String bidirect)throws IOException
	{
		int direction = Integer.parseInt(bidirect);
		this.nsteps = Integer.parseInt(nsteps); this.bidirect = (direction == 1 ? true: false);
		
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
		return "2 Step Reach";
	}

	public String getGroup() {
		return "Reach";
	}

	public boolean isMultiple() {
		return false;
	}

	class Evaluator implements NodeEvaluator {
		protected NetworkData nd;
		protected String name;
		protected int activeRelIdx;
		protected double[] reach;
		protected ArrayList<Integer> neighbors;
		protected HashSet ReachNodes;
		protected int totalNodes;
		protected Relation[] numRelations;

		public Evaluator() {
			this.nd = null;
			this.name = "2StepReach";
			this.activeRelIdx = 0;
		}

		public void runEvaluator() {

			for (int ri = 0; ri < numRelations.length; ri++) {
				Relation r = numRelations[ri];
				Graph g = r.getGraph();
				ListGraph lg = null;
				if (g instanceof ListGraph) {
					lg = (ListGraph) g;
				} else {
					lg = new ListGraph(g, true);
				}
				
				lg.setDirect(bidirect);
				totalNodes = lg.getNodeCount();
				this.reach = new double[totalNodes];
				neighbors = new ArrayList<Integer>();
				ReachNodes = new HashSet<Integer>();

				
				for (int n1= 0; n1 < totalNodes; n1++) {
					neighbors = lg.getAllNeighborhood(n1);
					ReachNodes.addAll(neighbors);
				
					recurseSteps(nsteps-1, lg, neighbors);	
					
					int sn = ReachNodes.size()-1;
					reach[n1] = (double) sn / totalNodes;
					ReachNodes.clear();
					neighbors.clear();
				}
			}
		}
		
		ArrayList<Integer> punt;
		public void recurseSteps(int nsteps, ListGraph lg, ArrayList<Integer> neighbors)
		{
			if(nsteps ==0) return;
			
				for (int n2 = 0; n2 < neighbors.size(); n2++)
					{
					punt = lg.getAllNeighborhood(neighbors.get(n2));
					ReachNodes.addAll(punt);
					recurseSteps(nsteps-1, lg, punt);
					
					}
		}
		

		public void setNetwork(NetworkData nd) {
			this.nd = nd;
			this.numRelations = new Relation[nd.getRelationCount()];
			
			for(int ri =0; ri< numRelations.length; ri++)
			numRelations[ri] = nd.getRelation(ri);
		}

		public Object evaluateNode(int idx) {
			return new Double(reach[idx]);
		}

		public Object evaluateNode(int idx, int ri) {
			return new Double(reach[idx]);
		}

		public String getName() {
			return name;
		}

		public NodeEvaluatorFactory getFactory() {
			return StepReachFactory.this;
		}
	}

}
