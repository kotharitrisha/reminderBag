/**
 * NetworkData.java
 *
 * Change Notes:
 *  10/15/2004 - Changed default constructors to asymmetric networks
 *   1/13/2008 - Tried to add 'multi' analyses.
 *   
 * Copyright (c) 2000-2004, 2008 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.wibinet.math.BasicPartition;
import com.wibinet.math.Graph;
import com.wibinet.math.ListGraph;
import com.wibinet.math.MutableGraph;
import com.wibinet.math.Partition;
import com.wibinet.math.PartitionCollection;
import com.wibinet.math.PartitionableMetricCollection;
import com.wibinet.util.DataFrame;

public class NetworkData extends Object implements ChangeListener,
	PartitionableMetricCollection
{
	protected int size;
	protected Relation[] relations;
	protected Actor[] actors;
	protected Partition partition;
	protected int[][] relationIndex;
	protected TreeMap<String, Double> mapStrength;
	
	protected Blockmodel model;
	protected Vector blockmodels;
	protected Vector partitionCollections;

	protected Vector timeSeries;
	protected Vector analyses;
	protected Vector analysisChangeListeners;
	
	protected Vector dataChangeListeners;
	protected Vector relationChangeListeners;
  
	public NetworkData(int size)
	{
		this(size, false);
	}
  
	public NetworkData(int size, boolean directed)
	{
		this(size, 1, directed);
	}
  
	public NetworkData(NetworkData nData)
	{
		this.relations = new Relation[nData.relations.length];
		this.size = nData.size;
		for(int ri=0; ri<relations.length; ri++)
		{
			// This should provide a deep copy.  Not sure if it would make more sense
			// for this to be entirely done with a Relation copy constructor.  As long
			// as 'measuresDistance' is the only feature that needs to be copied, I guess
			// this will work.
			this.relations[ri] = new Relation(nData.relations[ri]);
		}
		this.actors = new Actor[nData.actors.length];
		for(int ai=0; ai<actors.length; ai++)
		{
			actors[ai] = createActor(nData.actors[ai].getName());
		}
		
		comConstructor2();

		// copy partition(?)
		if(nData.getPartition() != null)
		{
			setPartition(new BasicPartition(nData.getPartition()));
		}
	}
  
	public NetworkData(int size, int numRelations, boolean directed)
	{
		this.size = size;
		this.relations = new Relation[numRelations];
		for(int ri=0; ri<numRelations; ri++)
		{
			relations[ri] = new Relation(this, "Relation " + (ri+1), directed);
		}

		this.actors = new Actor[size];
		for(int ai=0; ai<actors.length; ai++)
		{
			actors[ai] = createActor("Actor " + ai);
		}
		comConstructor2();
	}
  
	public NetworkData(double[][] data)
	{
		this(data, true);
	}
  
	public NetworkData(double[][] data, boolean directed)
	{
		double[][][] newData = new double[1][][];
		newData[0] = data;
		this.relations = new Relation[1];
		comConstructor(newData, directed);
	}
  
	public NetworkData(double[][][] data, boolean directed)
	{
		this.relations = new Relation[data.length];
		comConstructor(data, directed);
	}
  
	// awkward
	public NetworkData(Graph g, String name)
	{
		this.relations = new Relation[1];
		this.relations[0] = new Relation(this, name, g);
		this.size = g.getNodeCount();
		
		// need to set up actors
		this.actors = new Actor[size];
		for(int ai=0; ai<actors.length; ai++)
		{
			actors[ai] = createActor("Actor " + ai);
		}
		
		comConstructor2();
	}
	
	public NetworkData(DataFrame df, String senderKey, String receiverKey, String valueKey)
	{
		// get set of actors
		HashMap actorIndices = new HashMap();
		
		Set nameSet = df.levelsOf(senderKey);
		nameSet.addAll(df.levelsOf(receiverKey));
		Object[] names = nameSet.toArray();
		for(int ni=0; ni<names.length; ni++)
		{
			actorIndices.put(names[ni], new Integer(ni));
		}
		this.size = names.length;

		// set up actors
		this.actors = new Actor[size];
		for(int ai=0; ai<actors.length; ai++)
		{
			actors[ai] = createActor((String)names[ai]);
		}

		// create a relation
		this.relations = new Relation[1];
		this.relations[0] = new Relation(this, "DataFrame", true);
		
		// go back through dataframe and set tie values
		int dfsize = df.getSize();
		for(int oi=0; oi<dfsize; oi++)
		{
			String sender = df.getDatum(senderKey, oi);
			String receiver = df.getDatum(receiverKey, oi);
			double value = Double.parseDouble(df.getDatum(valueKey, oi));
			int i = ((Integer)actorIndices.get(sender)).intValue();
			int j = ((Integer)actorIndices.get(receiver)).intValue();
			setTieStrength(i, j, value);
		}
	}
	
	private void comConstructor(double[][][] data, boolean directed)
	{
		this.size = data[0].length;
		relationIndex = new int[size][size];
		for(int relIdx=0; relIdx < relations.length; relIdx++)
		{
			relations[relIdx] = new Relation(this, "Relation " + (relIdx+1),
					directed);
			for(int ri=0; ri<size; ri++)
			{
				for(int ci=0; ci<size; ci++)
				{
					relations[relIdx].setTieStrength(ri, ci, data[relIdx][ri][ci]);
				}
			}
		}	
		this.actors = new Actor[size];
		for(int ai=0; ai<actors.length; ai++)
		{
			actors[ai] = createActor("Actor " + ai);
		}

		comConstructor2();
	}
	
	
	public int findRelation(int n1, int n2)
	{
		System.out.println(relationIndex[0].length);
		System.out.println(relationIndex.length);
		//return relationIndex[n1][n2];
		return 0;
		
	}
  
	private void comConstructor2()
	{
		// default partition has every actor in the same partition
		// BasicPartition partition = new BasicPartition(this);
		
		{ mapStrength = new TreeMap<String, Double>();}
		
		this.partition = new BasicPartition(this);
		for(int ai=0; ai<size; ai++)
		{
			partition.setPartition(ai, 0);
		}
    
		// I think this is randomly adding in a stochastic blockmodel
		// every time a network is created, including when it's loaded.
		// that seem wrong.  Better to allow the possibility for no
		// blockmodel, even if there is a partition set.
		// why stochastic, I do not know...
		// this.model = new StochasticBlockmodel(this, partition);
		this.model = null;
    
		// create a set of models and add our one to it...
		this.blockmodels = new Vector();
		// blockmodels.addElement(model);
		
		// partition collections
		this.partitionCollections = new Vector();
		
		// create an empty set of time series
		this.timeSeries = new Vector();
		
		// also analyses
		this.analyses = new Vector();
		this.analysisChangeListeners = new Vector();
    
		// setup other listeners
		dataChangeListeners = new Vector();
		relationChangeListeners = new Vector();
	}
  
	public int getSize()
	{
		return size;
	}
  
	public Relation getRelation(int relIdx)
	{
		return relations[relIdx];
	}
  
  public void addRelation(String name, boolean directed)
  {
    Relation[] newR = new Relation[relations.length+1];
    System.arraycopy(relations, 0, newR, 0, relations.length);
    relations = newR;
    relations[relations.length-1] = new Relation(this, name, directed);
		fireRelationChangeEvent(new RelationChangeEvent(this, RelationChangeEvent.INSERTED,
			relations.length-1));
  }
  
	public void addRelation(String name, Graph g)
	{
    Relation[] newR = new Relation[relations.length+1];
    System.arraycopy(relations, 0, newR, 0, relations.length);
    relations = newR;
    relations[relations.length-1] = new Relation(this, name, g);
		fireRelationChangeEvent(new RelationChangeEvent(this, RelationChangeEvent.INSERTED,
			relations.length-1));
	}
	
  public void dropRelation(int relIdx)
  {
    Relation[] newR = new Relation[relations.length-1];
    System.arraycopy(relations, 0, newR, 0, relIdx);
    System.arraycopy(relations, relIdx+1, newR, relIdx, relations.length-relIdx-1);
    relations = newR;
		fireRelationChangeEvent(new RelationChangeEvent(this, RelationChangeEvent.DELETED,
			Math.min(relIdx, relations.length-1)));
  }
  
	public int getModeCount()
	{
		return getRelationCount();
	}
	
  public int getRelationCount()
  {
    return relations.length;
  }
  
	public Object getObjectAt(int idx)
	{
		return getActor(idx);
	}
	
	public Actor getActor(int ai)
	{
		return actors[ai];
	}
	
	public int getActorIndex(String from)
	{
		int i;
		for(i =0; i<actors.length; i++)
		{	
			if(actors[i].getName().matches("\\s+"+from+"\\s+") || actors[i].getName().equalsIgnoreCase(from)) {
				
				break;
			}
		}
		return i;
	}
	
  public void setTieStrength(int memberFrom, int memberTo, double strength)
  {
    setTieStrength(0, memberFrom, memberTo, strength);
  }

  public void setTieStrength(int rel, int memberFrom, int memberTo, double strength)
  {
	  mapStrength.put(""+memberFrom+"_"+memberTo, strength);
	  relations[rel].setTieStrength(memberFrom, memberTo, strength);
  }
  
  // returns the raw strength in the network file between two nodes. should use this to retrieve the real tie strength
  public double getRelValues(int to, int from)
  {
	  String key = ""+from+"_"+to;
	  if(mapStrength.containsKey(key)) return mapStrength.get(key);
	  else return 0.0;
  }
  
	public double getDistance(int i, int j)
	{
		return getTieStrength(i, j);
	}
	
	public int getEgoSize(int i)
	{
			Relation r = relations[0];
			Graph g = r.getGraph();
			ListGraph lg = null;
			if (g instanceof ListGraph) {
				lg = (ListGraph) g;
			} else {
				lg = new ListGraph(g, true);
			}
			return lg.getIntNeighborhood(i).length;
	}
	
	public double getDistance(int m, int i, int j)
	{
		return getTieStrength(m, i, j);
	}

  public double getTieStrength(int memberFrom, int memberTo)
  {
    return getTieStrength(0, memberFrom, memberTo);
  }
  
  public double getTieStrength(int rel, int memberFrom, int memberTo)
  {
    return relations[rel].getTieStrength(memberFrom, memberTo);
  }
  
  public Partition getPartition()
  {
    // return model.getPartition();
		return partition;
  }
  
  public void setPartition(Partition partition)
  {
    // this.model.setPartition(partition);
		this.partition = partition;
    DataChangeEvent dce = new DataChangeEvent(this,
      DataChangeEvent.PARTITION_CHANGED,
      DataChangeEvent.ALL_ROWS, DataChangeEvent.ALL_COLUMNS);
    fireDataChanged(dce);
  }
  
  public Blockmodel getBlockmodel()
  {
  	return model;
  }
  
  public void setBlockmodel(Blockmodel model)
  {
    this.model = model;
    DataChangeEvent dce = new DataChangeEvent(this,
      DataChangeEvent.PARTITION_CHANGED,
      DataChangeEvent.ALL_ROWS, DataChangeEvent.ALL_COLUMNS);
    fireDataChanged(dce);
  }
  
  public void addBlockmodel(Blockmodel bModel)
  {
    blockmodels.addElement(bModel);
  }
	
	public void addPartitionCollection(PartitionCollection pcol)
	{
		partitionCollections.add(pcol);
	}
	
	public int getPartitionCollectionCount()
	{
		return partitionCollections.size();
	}
	
	public PartitionCollection getPartitionCollection(int idx)
	{
		return (PartitionCollection)partitionCollections.elementAt(idx);
	}
	
	public void addTimeSeries(TimeSeriesNodeData ts)
	{
		timeSeries.addElement(ts);
	}
	
	public TimeSeriesNodeData getTimeSeries(int idx)
	{
		return (TimeSeriesNodeData)timeSeries.elementAt(idx);
	}

	public int getTimeSeriesCount()
	{
		return timeSeries.size();
	}
	
	public void addAnalysis(NodeEvaluator ne)
	{
		analyses.addElement(ne);
		fireAnalysisChanged(new ChangeEvent(this));
	}
	
	public NodeEvaluator getAnalysis(int idx)
	{
		return (NodeEvaluator)analyses.elementAt(idx);
	}

	public int getAnalysesCount()
	{
		return analyses.size();
	}
	
	public void addAnalysisChangeListener(ChangeListener cl)
	{
		analysisChangeListeners.addElement(cl);
	}
	
	public void removeAnalysisChangeListener(ChangeListener cl)
	{
		analysisChangeListeners.removeElement(cl);
	}
	
	protected void fireAnalysisChanged(ChangeEvent ce)
	{
		for(int i=0; i<analysisChangeListeners.size(); i++)
		{
			ChangeListener cl = 
				(ChangeListener)analysisChangeListeners.elementAt(i);
			cl.stateChanged(ce);
		}
	}
	
	public void addRelationChangeListener(RelationChangeListener rcl)
	{
		relationChangeListeners.addElement(rcl);
	}
	
	public void removeRelationChangeListener(RelationChangeListener rcl)
	{
		relationChangeListeners.removeElement(rcl);
	}
	
	protected void fireRelationChangeEvent(RelationChangeEvent rce)
	{
		for(int i=0; i<relationChangeListeners.size(); i++)
		{
			RelationChangeListener rcl = 
				(RelationChangeListener)relationChangeListeners.elementAt(i);
			rcl.relationChanged(rce);
		}
	}

	public void stateChanged(ChangeEvent ce)
	{
		// pass it on...
		fireAnalysisChanged(ce);
	}
	
	public Vector getBlockmodels()
	{
		return blockmodels;
	}
	
  public double getBlockDensity(int relIdx, int bi, int bj)
  {
  	return getBlockDensity(getPartition(), relIdx, bi, bj);
  }
  
  public double getBlockDensity(Partition p, int relIdx, int bi, int bj)
  {
  	int[] actorsI = p.getObjectIndices(bi);
  	int[] actorsJ = p.getObjectIndices(bj);
  	double sum = 0.0;
  	Relation r = relations[relIdx];
  	for(int i=0; i<actorsI.length; i++)
  	{
  		for(int j=0; j<actorsJ.length; j++)
  		{
  			if(actorsI[i] != actorsJ[j])
  			{
	  			sum += r.getTieStrength(actorsI[i], actorsJ[j]);
  			}
  		}
  	}
  	double blockSize = getBlockSize(p, bi, bj);
  	return sum / blockSize;
  }
  
  public int getBlockSize(int bi, int bj)
  {
  	return getBlockSize(getPartition(), bi, bj);
  }
  
  public int getBlockSize(Partition p, int bi, int bj)
  {
  	int[] actorsI = p.getObjectIndices(bi);
  	int[] actorsJ = p.getObjectIndices(bj);
  	int blockSize = actorsI.length * actorsJ.length;
  	if(bi == bj)
  	{
  		blockSize -= actorsI.length;
  	}
  	return blockSize;
  }
  
  public double getBlockMass(int relIdx, int bi, int bj)
  {
  	return getBlockMass(getPartition(), relIdx, bi, bj);
  }
  
  public double getBlockMass(Partition p, int relIdx, int bi, int bj)
  {
  	int[] actorsI = p.getObjectIndices(bi);
  	int[] actorsJ = p.getObjectIndices(bj);
  	double sum = 0.0;
  	Relation r = relations[relIdx];
  	for(int i=0; i<actorsI.length; i++)
  	{
  		for(int j=0; j<actorsJ.length; j++)
  		{
  			if(actorsI[i] != actorsJ[j])
  			{
	  			sum += r.getTieStrength(actorsI[i], actorsJ[j]);
	  		}
  		}
  	}
  	return sum;
  }
  
  public double getRowMarginal(int relIdx, int row)
  {
  	double sum = 0.0;
  	Relation r = relations[relIdx];
  	for(int j=0; j<size; j++)
  	{
  		sum += r.getTieStrength(row, j);
  	}
  	return sum;
  }
  
  public double getColMarginal(int relIdx, int col)
  {
  	double sum = 0.0;
  	Relation r = relations[relIdx];
  	for(int i=0; i<size; i++)
  	{
  		sum += r.getTieStrength(i, col);
  	}
  	return sum;
  }
  
  public double getMass(int relIdx)
  {
  	double sum = 0.0;
  	Relation r = relations[relIdx];
  	for(int i=0; i<size; i++)
  	{
  		for(int j=i+1; j<size; j++)
  		{
  			sum += r.getTieStrength(i, j) + r.getTieStrength(j, i);
  		}
  	}
  	return sum;
  }

  public double getMutuals(int relIdx)
  {
  	double sum = 0.0;
  	Relation r = relations[relIdx];
  	for(int i=0; i<size; i++)
  	{
  		for(int j=i+1; j<size; j++)
  		{
  			sum += r.getTieStrength(i, j) * r.getTieStrength(j, i);
  		}
  	}
  	return sum;
  }
  
  public int getMutualDegree(int member){
  
  int mutual = 0;
  
  Relation r = relations[0];
	Graph g = r.getGraph();
	ListGraph lg = null;
	if (g instanceof ListGraph) {
		lg = (ListGraph) g;
	} else {
		lg = new ListGraph(g, true);
	}
	
	int[] array = lg.getIntNeighborhood(member);
  
  for(int i=0; i < array.length; i++)
    {
      if(getTieStrength(member, array[i])>0 && getTieStrength(array[i], member)>0) mutual++;
    }
  return mutual;
}
    
  public double getBlockMutuals(int relIdx, int bi, int bj)
  {
  	return getBlockMutuals(getPartition(), relIdx, bi, bj);
  }
  
  public double getBlockMutuals(Partition p, int relIdx, int bi, int bj)
  {
  	int[] actorsI = p.getObjectIndices(bi);
  	int[] actorsJ = p.getObjectIndices(bj);
  	double sum = 0.0;
  	Relation r = relations[relIdx];
  	for(int i=0; i<actorsI.length; i++)
  	{
  		for(int j=0; j<actorsJ.length; j++)
  		{
  			if(actorsI[i] != actorsJ[j])
  			{
	  			sum += r.getTieStrength(actorsI[i], actorsJ[j]) * 
	  			  r.getTieStrength(actorsJ[j], actorsI[i]);
	  		}
  		}
  	}
  	return sum;
  }
  
  public double getBlockAsymmetrics(int relIdx, int bi, int bj)
  {
  	return getBlockAsymmetrics(getPartition(), relIdx, bi, bj);
  }
  
  public double getBlockAsymmetrics(Partition p, int relIdx, int bi, int bj)
  {
  	int[] actorsI = p.getObjectIndices(bi);
  	int[] actorsJ = p.getObjectIndices(bj);
  	double sum = 0.0;
  	Relation r = relations[relIdx];
  	for(int i=0; i<actorsI.length; i++)
  	{
  		for(int j=0; j<actorsJ.length; j++)
  		{
  			if(actorsI[i] != actorsJ[j])
  			{
	  			sum += r.getTieStrength(actorsI[i], actorsJ[j]) * 
	  			  (1.0 - r.getTieStrength(actorsJ[j], actorsI[i]));
	  		}
  		}
  	}
  	return sum;
  }
  
  public double getNulls(int relIdx)
  {
  	double sum = 0.0;
  	Relation r = relations[relIdx];
  	for(int i=0; i<size; i++)
  	{
  		for(int j=i+1; j<size; j++)
  		{
  			// this isn't symmetric with getMutuals()...(getMutuals() looks
  			// at diagonals)
  			if(i != j)
  			{
	  			sum += (1.0-r.getTieStrength(i, j)) * 
	  				(1.0 - r.getTieStrength(j, i));
	  		}
  		}
  	}
  	return sum;
  }
  
  public double getBlockNulls(int relIdx, int bi, int bj)
  {
  	return getBlockNulls(getPartition(), relIdx, bi, bj);
  }
  
  public double getBlockNulls(Partition p, int relIdx, int bi, int bj)
  {
  	int[] actorsI = p.getObjectIndices(bi);
  	int[] actorsJ = p.getObjectIndices(bj);
  	double sum = 0.0;
  	Relation r = relations[relIdx];
  	for(int i=0; i<actorsI.length; i++)
  	{
  		for(int j=0; j<actorsJ.length; j++)
  		{
  			if(actorsI[i] != actorsJ[j])
  			{
	  			sum += (1.0 - r.getTieStrength(actorsI[i], actorsJ[j])) * 
	  			  (1.0 - r.getTieStrength(actorsJ[j], actorsI[i]));
	  		}
  		}
  	}
  	return sum;
  }
  
  public void permute(int map[])
  {
    // make sure that all relations have mutable graphs
		// underlying them
		for(int ri=0; ri<relations.length; ri++)
		{
			if(!relations[ri].isMutable())
			{
				System.err.println("Tried to permute non-mutable data");
				return;
			}
		}

    // permute data
    for(int ri=0; ri<relations.length; ri++)
    {
			MutableGraph mg = (MutableGraph)relations[ri].getGraph();
      mg.permute(map);
    }
    DataChangeEvent dce = new DataChangeEvent(this,
      DataChangeEvent.VALUE_CHANGED,
      DataChangeEvent.ALL_ROWS, DataChangeEvent.ALL_COLUMNS);
    fireDataChanged(dce);
  }
  
  public double getInDegreeCentrality(int member)
  {
		double cent = 0.0;
		int relCt = getRelationCount();
		for(int ri=0; ri<relCt; ri++)
		{
			cent += getInDegreeCentrality(ri, member);
		}
		return cent;
  }
  
  public double getInDegreeCentrality(int rel, int member)
  {
    double centrality = 0.0d;
    for(int mi=0; mi<relations[0].getNodeCount(); mi++)
    {
      centrality += relations[rel].getTieStrength(mi, member);
    }
    return centrality;
  }
  
  public int getInDegree(int rel, int member)
  {
	  int in = 0;
	  
	  Relation r = relations[0];
		Graph g = r.getGraph();
		ListGraph lg = null;
		if (g instanceof ListGraph) {
			lg = (ListGraph) g;
		} else {
			lg = new ListGraph(g, true);
		}
		int[] array = lg.getIntNeighborhood(member);

	  for(int mi=0; mi<array.length; mi++)
	    {
	      if(getTieStrength(array[mi], member)>0) in++;
	    }
	  return in;
  }
  
	  public int getOutDegree(int rel, int member)
	  {
		  int out = 0;
		  
		  Relation r = relations[0];
			Graph g = r.getGraph();
			ListGraph lg = null;
			if (g instanceof ListGraph) {
				lg = (ListGraph) g;
			} else {
				lg = new ListGraph(g, true);
			}
			int[] array = lg.getIntNeighborhood(member);

		  for(int mi=0; mi<array.length; mi++)
		    {
		      if(getTieStrength(member, array[mi])>0) out++;
		    }
		  return out;

	  }
  
  public double getOutDegreeCentrality(int member)
  {
		double cent = 0.0;
		int relCt = getRelationCount();
		for(int ri=0; ri<relCt; ri++)
		{
			cent += getOutDegreeCentrality(ri, member);
		}
		return cent;
  }
  
  public double getOutDegreeCentrality(int rel, int member)
  {
    double centrality = 0.0d;
    for(int mi=0; mi<relations[0].getNodeCount(); mi++)
    {
      centrality += relations[rel].getTieStrength(member, mi);
    }
    return centrality;
  }
  
  public double getDegreeCentrality(int member)
  {
    // doesn't this only make sense for non-directed graphs?
		double cent = 0.0;
		int relCt = getRelationCount();
		for(int ri=0; ri<relCt; ri++)
		{
			cent += getInDegreeCentrality(ri, member);
			cent += getOutDegreeCentrality(ri, member);
		}
		return cent/2.0;
  }
  
  public synchronized void addNode()
  {
    addNode(true);
  }
  
  protected synchronized void addNode(boolean fireEvent)
  {
    // make sure that all relations have mutable graphs
		// underlying them
		for(int ri=0; ri<relations.length; ri++)
		{
			if(!relations[ri].isMutable())
			{
				System.err.println("Tried to add node to non-mutable data");
				return;
			}
		}
		
		this.size += 1;
    for(int ri=0; ri<relations.length; ri++)
    {
			MutableGraph g = (MutableGraph)relations[ri].getGraph();
			g.addNode();
    }
    
    // add this node to the last partition
    Partition newPartition = new BasicPartition(this);
    for(int ai=0; ai<size-1; ai++)
    {
      newPartition.setPartition(ai, getPartition().getPartition(ai));
    }
    newPartition.setPartition(size-1, getPartition().getPartition(size-2));
    setPartition(newPartition);
    
    if(fireEvent)
    {
	    DataChangeEvent dce = new DataChangeEvent(this, DataChangeEvent.NODE_INSERTED, size, size);
	    fireDataChanged(dce);
	  }
  }
  
  public synchronized void deleteNode(int nodeIndex)
  {
    deleteNode(nodeIndex, true);
  }
  
  protected synchronized void deleteNode(int nodeIndex, boolean fireEvent)
  {
    // make sure that all relations have mutable graphs
		// underlying them
		for(int ri=0; ri<relations.length; ri++)
		{
			if(!relations[ri].isMutable())
			{
				System.err.println("Tried to delete node from non-mutable data");
				return;
			}
		}
		
    // int size = relations[0].getRows();
    this.size -= 1;
    int amtLeft = size-nodeIndex;
    for(int ri=0; ri<relations.length; ri++)
    {
			MutableGraph mg = (MutableGraph)relations[ri].getGraph();
			mg.deleteNode(nodeIndex);
    }
    
    // update partition stuff
		if(getPartition() != null)
		{
			Partition p = getPartition();
			p.deleteObjectAt(nodeIndex);
		}
		
		for(int pci=0; pci<partitionCollections.size(); pci++)
		{
			PartitionCollection pcol = (PartitionCollection)partitionCollections.elementAt(pci);
			pcol.deleteObjectAt(nodeIndex);
		}

    if(fireEvent)
    {
	    DataChangeEvent dce = new DataChangeEvent(this, DataChangeEvent.NODE_DELETED, nodeIndex, nodeIndex);
	    fireDataChanged(dce);
	  }
  }
  
  public void addDataChangeListener(DataChangeListener cl)
  {
    dataChangeListeners.addElement(cl);
  }
  
  public void removeDataChangeListener(DataChangeListener cl)
  {
    dataChangeListeners.removeElement(cl);
  }
  
  public void fireDataChanged(DataChangeEvent dce)
  {
		// it seems that it's possible that we might get here
		// before everything is set up.  in theory, we should have
		// a 'pending events queue', but i don't feel like implementing
		// that right now, since i'm not sure it makes a difference for
		// these early events.
		if(dataChangeListeners == null)
		{
			return;
		}
		
    // should release locks if any, and notify all data listeners
    for(int ci=0; ci<dataChangeListeners.size(); ci++)
    {
      ((DataChangeListener)dataChangeListeners.elementAt(ci)).dataChanged(dce);
    }
  }
  
  protected Actor createActor(String name)
  {
	  return new Actor(this, name);
  }
}