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


public class FriendCharFactory implements NodeEvaluatorFactory {
	
	protected int nsteps;
	
	public FriendCharFactory () throws IOException
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
		return "FriendChar";
	}

	public String getGroup() {
		return "FriendChar";
	}

	public boolean isMultiple() {
		return false;
	}

	class Evaluator implements NodeEvaluator {
		protected NetworkData nd;
		protected String name;
		protected int activeRelIdx;
		protected int totalNodes;
		protected int date;
		protected Relation[] numRelations;
		protected HashMap <String, Integer>mgrs;
		protected HashMap <String, String>divisions;
		protected HashMap <String, Integer>adoptions;
		protected HashMap <String, Integer>sr9;
		protected HashMap <String, Integer>sr10;	
		protected HashMap <String, Double>monthlyRevs;
		protected HashMap <String, Double>monthlyHours;
		ArrayList<Double>[] friendChar;
		Relation reference;
		ListGraph lg;
		HashMap<Integer, int[]> nMap;

		NodeEvaluator srEval;
		public Evaluator() {
			this.nd = null;
			this.name = "FriendChar";
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
				friendChar = (ArrayList<Double>[])new ArrayList[totalNodes];
				//neighborhood map
				nMap = new HashMap<Integer, int[]>();
				
				for(int i =0; i <totalNodes; i++)
				{
					nMap.put(new Integer(i), lg.getIntNeighborhood(i));
				}
				
				
				for (int n1= 0; n1 < totalNodes; n1++) {
					String name = this.nd.getActor(n1).getName()+".ibm.com";
					//System.out.println(name+" "+ this.mgrs.get(name)+" " + this.divisions.get(name));
					friendChar[n1] = findFriendChar(n1);
				
				}
			}
		}
		
		public ArrayList<Double> findFriendChar(int i)
		{
			int[] iNeighbors = nMap.get(i);
			
			double rev = 0;
			double hours = 0;
			int managers = 0;
			int adoptDate = 10000000;
			int numAdopt =0;
			int numsr9Adopt=0;
			int numsr10Adopt=0;
			ArrayList <String> primDivs= new ArrayList<String>();
			for(int j =0; j< iNeighbors.length; j++){
				String neighbor = nd.getActor(iNeighbors[j]).getName()+".ibm.com";
				//System.out.println("neighbor: "+j +" " + neighbor);
				if(this.monthlyRevs.containsKey(neighbor)){
					Double valr =this.monthlyRevs.get(neighbor);
					rev+=valr.doubleValue();
				}
				if(this.monthlyHours.containsKey(neighbor)){
					Double valh = this.monthlyHours.get(neighbor);
					hours += valh.doubleValue();
				}
				if(this.mgrs.containsKey(neighbor)){
					Integer valm = this.mgrs.get(neighbor);
				//	System.out.println("manager: "+valm);
					managers += valm.intValue();
				}
				if(this.divisions.containsKey(neighbor)){
					String vald =this.divisions.get(neighbor);
					if(!primDivs.contains(vald)){
						primDivs.add(vald);
					}		   
				}
				// examine how many friends adopted before the current date (this.date)
				if(this.adoptions.containsKey(neighbor)){
					Integer vala = this.adoptions.get(neighbor);
					if (vala.doubleValue()<this.date){
						if (vala.intValue() < adoptDate){
							adoptDate = vala.intValue();
					//		System.out.println(vala.intValue()+ " adopt date " +i+ " "+neighbor);
						}
						numAdopt++;
						// now calculate if this is a junior who signed up or a senior
						if(this.sr9.containsKey(neighbor)){
							Integer valm = this.sr9.get(neighbor);
							numsr9Adopt= numsr9Adopt+ valm.intValue();
						}
						if(this.sr10.containsKey(neighbor)){
							Integer valm = this.sr10.get(neighbor);
							numsr10Adopt= numsr10Adopt+ valm.intValue();
						}
						
					}
				}	
				
			}
			if(adoptDate ==10000000) {
				adoptDate =0;
			}
			//System.out.println(rev+" "+ hours+" "+managers+" "+ primDivs.size()+ " "+adopts);
			
			ArrayList<Double> friends = new ArrayList<Double>();
			friends.add(new Double(rev));
			friends.add(new Double(hours));
			friends.add(new Double(managers));
			friends.add(new Double(primDivs.size()));
			friends.add(new Double(numAdopt));
			friends.add(new Double(adoptDate));
			friends.add(new Double(numsr9Adopt));
			friends.add(new Double(numsr10Adopt));
			return friends;
		}
		
		
		public void setMgr(HashMap<String, Integer> mgrs){
			this.mgrs= mgrs;
		}
		public void setDivisions(HashMap<String, String> divisions){
		this.divisions = divisions;
		}
		public void setAdoption(HashMap<String, Integer> adoptions){
			this.adoptions = adoptions;
			}
		public void setRevenues(HashMap<String, Double> revenues){
			this.monthlyRevs = revenues;
		}
		public void setHours(HashMap<String, Double> hours){
			this.monthlyHours= hours;
		}
		public void setSr9(HashMap<String, Integer> sr9){
			this.sr9= sr9;
		}
		public void setSr10(HashMap<String, Integer> sr10){
			this.sr10= sr10;
		}
		public void setDates(int date){
			this.date= date;
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
			return FriendCharFactory .this;
		}
	}

}
