/**
 * DistanceFactory.java
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
import com.wibinet.networks.FriendCharFactory.Evaluator;


public class DistanceFactory implements NodeEvaluatorFactory {
	
	protected int nsteps;
	
	public DistanceFactory() throws IOException
	{
	}
	
	
	public NodeEvaluator newInstance() {
		return new Evaluator();
	}
	public Evaluator aNewInstance() {
		return new Evaluator();
	}
	public void edit(NodeEvaluator evaluator, JFrame parent) {

	}

	public Class getEvaluatorClass() {
		return Double.class;
	}

	public String getName() {
		return "Physical Distance";
	}

	public String getGroup() {
		return "Physical Distance";
	}

	public boolean isMultiple() {
		return false;
	}

	class Evaluator implements NodeEvaluator {
		protected NetworkData nd;
		protected NetworkData distanceMatrix; //distance matrix
		protected String name;
		protected int activeRelIdx;
		protected int totalNodes;
		protected Relation[] numRelations;
		double[] distanceAry;
		double[] maxDisAry;
		Relation reference;
		ListGraph lg;
		HashMap<Integer, int[]> nMap;
		HashMap<Integer, int[]> nOutMap;
		HashMap<Integer, Double> norm;
		public Evaluator() {
			this.nd = null;
			this.name = "Distance";
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
				distanceAry = new double[totalNodes];
				maxDisAry = new double[totalNodes];
				
				nMap = new HashMap<Integer, int[]>();
		
				for(int i =0; i <totalNodes; i++)
				{
					nMap.put(new Integer(i), lg.getIntNeighborhood(i));				
				}
				
				for (int n1= 0; n1 < totalNodes; n1++) {
					distanceAry[n1] = findTotDist(n1);
					maxDisAry[n1]= findMaxDist(n1);
				
				}
			}
		}
		public String printArray(int[] a){
			String str="";
			for(int i=0;i<a.length;i++){
				str=str+","+nd.getActor(a[i]);
			}
			return str;
		}
		public double findTotDist(int i)
		{
			double dis = 0.0;
			int[] iNeighbors = nMap.get(i);
			String iName = nd.getActor(i).getName();
			int iIndex_dis = distanceMatrix.getActorIndex(iName);
			
			for(int j =0; j< iNeighbors.length; j++)
			{
				String jName = nd.getActor(iNeighbors[j]).getName();
				int jIndex_dis = distanceMatrix.getActorIndex(jName);
			
				dis = dis + distanceBetween(iIndex_dis,jIndex_dis);
				//System.out.println("nd: "+nd.getActor(i)+" "+nd.getActor(iNeighbors[j])+" "+propStrength(i,iNeighbors[j]));
				//System.out.println("ds: "+distanceMatrix.getActor(iIndex_dis)+" " +distanceMatrix.getActor(jIndex_dis)+" "+distanceBetween(iIndex_dis,jIndex_dis)+" "+dis);
			}
			return dis;
			
		}
			
		public double findMaxDist(int i)
		{
			double dis = 0.0;
			int[] iNeighbors = nMap.get(i);
			String iName = nd.getActor(i).getName();
			int iIndex_dis = distanceMatrix.getActorIndex(iName);
			
			for(int j =0; j< iNeighbors.length; j++)
			{
				String jName = nd.getActor(iNeighbors[j]).getName();
				int jIndex_dis = distanceMatrix.getActorIndex(jName);			
				dis = Math.max( dis, distanceBetween(iIndex_dis,jIndex_dis));
			}
			return dis;
			
		}
		public void setNetwork(NetworkData nd) {
			this.nd = nd;
			this.numRelations = new Relation[nd.getRelationCount()];
			
			for(int ri =0; ri< numRelations.length; ri++)
			numRelations[ri] = nd.getRelation(ri);
		}
		
		public void setDistanceMatrix(NetworkData dis) {
			this.distanceMatrix = dis;
		}
		public double propStrength(int n1, int n2){
			
			return nd.getRelValues(n1, n2);
		}

		public double distanceBetween(int n1, int n2){
			
			return distanceMatrix.getRelValues(n1, n2);
		}
		public Object evaluateNode(int idx) {
			String a = distanceAry[idx]+"\t"+maxDisAry[idx];
			return a;
		}

		public Object evaluateNode(int idx, int ri) {
			String a = distanceAry[idx]+"\t"+maxDisAry[idx];
			return a;
		}

		public String getName() {
			return name;
		}

		public NodeEvaluatorFactory getFactory() {
			return DistanceFactory.this;
		}
	}

}
