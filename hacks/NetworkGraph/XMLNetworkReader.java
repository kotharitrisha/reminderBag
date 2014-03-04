/**
 * XMLNetworkReader.java
 *
 * A class for decoding XML network data
 *
 * Copyright (c) 2000-2003 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.io.*;
import java.util.*;
import org.xml.sax.*;
import com.wibinet.math.BasicPartition;
import com.wibinet.math.Partition;
import com.wibinet.math.PartitionCollection;
import com.wibinet.math.PartitionSet;
import com.wibinet.math.PartitionTree;
import com.wibinet.util.*;

public class XMLNetworkReader extends com.wibinet.util.XMLReader
{
	protected String networkName;
	protected Vector actors;
	
	// info for blockmodels
	protected BlockmodelFactory workingFactory;
	protected Hashtable workingProps;
	protected Partition workingPartition;
	protected String workingName;
	protected int partitionIdx;
	
	// time series info
	protected TimeSeriesNodeData workingTimeSeries;
	protected int tsTime;
	
	// partition collection
	protected TNode workingPTree;
	protected Vector workingPSets;
	protected Vector workingPSetLabels;
	protected Vector partitionCollections;
	
	protected final static String[] xmlTags =
	{
		"network", "blockmodel", "bparam", "actormap", "actorlist",
		"actor", "relation", "tie", "packedtie", "timeseries",
		"nodevalues", "nodevalue", 
		"partitiontree", "partitionset", "psetelem"
	};

	public XMLNetworkReader()
	{
		super(XMLNetworkReader.class);
		
		// init per-parse variables
		networkName = null;
		actors = new Vector();
		
		// blockmodel processing
		workingName = null;
		workingFactory = null;
		workingProps = null;
		workingPartition = null;
		partitionIdx = -1;
		
		// time series processing
		workingTimeSeries = null;
		tsTime = -1;
		
		// partition collections
		workingPTree = null;
		workingPSets = null;
		workingPSetLabels = null;
		partitionCollections = new Vector();
	}
	
	public VisualNetworkData readNetwork(InputStream is) throws Exception
	{
		return (VisualNetworkData)read(is);
	}

	protected void populateHandlers()
	{
		handlers.put("network", new NetworkHandler());
		handlers.put("blockmodel", new BlockmodelHandler());
		handlers.put("bparam", new BParamHandler());
		handlers.put("actormap", new ActorMapHandler());
		handlers.put("actorlist", new ActorlistHandler());
		handlers.put("actor", new ActorHandler());
		handlers.put("relation", new RelationHandler());
		handlers.put("tie", new TieHandler());
		handlers.put("packedtie", new PackedTieHandler());
		handlers.put("timeseries", new TimeSeriesHandler());
		handlers.put("nodevalues", new NodeValuesHandler());
		handlers.put("nodevalue", new NodeValueHandler());
		handlers.put("partitiontree", new PartitionTreeHandler());
		handlers.put("partitionset", new PartitionSetHandler());
		handlers.put("psetelem", new PSetElemHandler());
	}

	public String[] getXMLTags()
	{
		return xmlTags;
	}
	
	public void resetData()
	{
	  actors.removeAllElements();
	  baseObject = null;
	  networkName = null;
	}
		
	public class NetworkHandler implements MiniHandler
	{
		public void startElement(Attributes attributes)
		{
		  resetData();
		  networkName = attributes.getValue("name");
		}
		
		public void endElement()
		{
			VisualNetworkData network = (VisualNetworkData)baseObject;
			network.setName(networkName);
			for(int i=0; i<partitionCollections.size(); i++)
			{
				PartitionCollection pc = (PartitionCollection)partitionCollections.elementAt(i);
				network.addPartitionCollection(pc);
			}
		}
	}
	
	public class BlockmodelHandler implements MiniHandler
	{
		public void startElement(Attributes attributes)
		{
      // get factory & title
      String factoryName = attributes.getValue("factory");
      workingName = attributes.getValue("name");
      
      // start up a partition & props
      workingProps = new Hashtable();
      workingPartition = new BasicPartition(((VisualNetworkData)baseObject).getSize());
      partitionIdx = 0;
      
      // try to instantiate a class?
      try
      {
        Class facClass = Class.forName(factoryName);
        workingFactory = (BlockmodelFactory)facClass.newInstance();
      }
      catch(Throwable t)
      {
        // probably should note this...
        t.printStackTrace();

        // don't really matter what throwable it is, do it?
        workingFactory = null;
      }
		}
		
		public void endElement()
		{
      if(workingFactory == null)
      {
        return;
      }
      
      try
      {
        Blockmodel model = workingFactory.newInstance(((VisualNetworkData)baseObject), workingPartition);
        model.setTitle(workingName);
        workingFactory.setProperties(model, workingProps);
        ((VisualNetworkData)baseObject).addBlockmodel(model);
      }
      catch(Throwable t)
      {
        // probably should note this...
        t.printStackTrace();
      }
		}
	}
	
  public class BParamHandler implements MiniHandler
  {
    public void startElement(Attributes attributes)
    {
      if(workingFactory == null)
      {
        return;
      }
      
      String pName = attributes.getValue("name");
      String pVal = attributes.getValue("value");
      workingProps.put(pName, pVal);
    }
    
    public void endElement()
    {
    }
  }
  
	public class ActorMapHandler implements MiniHandler
	{
		public void startElement(Attributes attributes)
		{
      /*if(workingFactory == null)
      {
        return; // silent death?
      }*/
      
      String actorList = attributes.getValue("actors");
      String pName = attributes.getValue("partition");
      
      StringTokenizer st = new StringTokenizer(actorList, ",");
      while(st.hasMoreTokens())
      {
        String sActor = st.nextToken().trim();
        int aIdx = Integer.parseInt(sActor);
        workingPartition.setPartition(aIdx, partitionIdx);
      }
      workingPartition.setPartitionName(partitionIdx, pName);
      partitionIdx++;
		}
		
		public void endElement()
		{
		}
	}
	
	public class ActorlistHandler implements MiniHandler
	{
		public void startElement(Attributes attributes)
		{
		}
		
		public void endElement()
		{
		  // create network here
		  NetworkData data = new NetworkData(actors.size(), 0, true);
		  baseObject = new VisualNetworkData(data);
		  
		  // label
		  for(int ai=0; ai<actors.size(); ai++)
		  {
		    Actor a = (Actor)actors.elementAt(ai);
		    ((VisualNetworkData)baseObject).setLabel(ai, a.getName());
		    ((VisualNetworkData)baseObject).setXPos(ai, a.getXPos());
		    ((VisualNetworkData)baseObject).setYPos(ai, a.getYPos());
		    ((VisualNetworkData)baseObject).setZPos(ai, a.getZPos());
		  }
		}
	}
	
	public class ActorHandler implements MiniHandler
	{
	  double xPos, yPos, zPos;
	  
		public void startElement(Attributes attributes)
		{
		  xPos = Double.valueOf(attributes.getValue("xpos")).doubleValue();
		  yPos = Double.valueOf(attributes.getValue("ypos")).doubleValue();
		  zPos = Double.valueOf(attributes.getValue("ypos")).doubleValue();
		  startTextCapture();
		}
		
		public void endElement()
		{
		  String actorName = getCapturedText();
		  endTextCapture();
		  Actor a = new Actor(actorName, xPos, yPos, zPos);
		  actors.addElement(a);
		}
	}
	
	public class Actor extends Object
	{
	  protected String name;
	  protected double xPos;
	  protected double yPos;
	  protected double zPos;
	  
	  public Actor(String name, double xPos, double yPos, double zPos)
	  {
	    this.name = name;
	    this.xPos = xPos;
	    this.yPos = yPos;
	    this.zPos = zPos;
	  }
	  
	  public String getName()
	  {
	    return name;
	  }
	  
	  public double getXPos()
	  {
	    return xPos;
	  }
	  
	  public double getYPos()
	  {
	    return yPos;
	  }
	  
	  public double getZPos()
	  {
		  return zPos;
	  }
	}
	
	public class TieHandler implements MiniHandler
	{
		public void startElement(Attributes attributes)
		{
		  int relIdx = ((VisualNetworkData)baseObject).getRelationCount() - 1;
		  int from = Integer.parseInt(attributes.getValue("from"));
		  int to = Integer.parseInt(attributes.getValue("to"));
		  double strength = 
		    Double.valueOf(attributes.getValue("strength")).doubleValue();
		  ((VisualNetworkData)baseObject).setTieStrength(relIdx, from, to, strength);
		}
		
		public void endElement()
		{
		}
	}
  
  public class PackedTieHandler implements MiniHandler
  {
    protected int from = -1;
    protected int bits = 16; 
    protected int relIdx = -1;

    public void startElement(Attributes attributes)
    {
		  relIdx = ((VisualNetworkData)baseObject).getRelationCount() - 1;
      from = Integer.parseInt(attributes.getValue("from"));
      bits = Integer.parseInt(attributes.getValue("bits"));
		  startTextCapture();
    }

		public void endElement()
    {
		  String tieData = getCapturedText();
		  endTextCapture();
      
      // now process
      if(bits == 1)
      {
        // get ties eight at a time out of chunk
        
      }
      else
      {
        int chunkLen = (bits == 16) ? 4 : 2; // only 8 or 16 bits, right?
        int maxVal = (16 << ((chunkLen-1) * 4)) - 1;
        double dMax = 1.0 * maxVal;
        int numChunks = tieData.length() / chunkLen;
      
        for(int to=0; to<numChunks; to++)
        {
          String chunk = "0" + tieData.substring(to*chunkLen, (to+1)*chunkLen); // make sure it's positive?
          int raw = Integer.parseInt(chunk, 16);
          double dRaw = 1.0 * raw;
          double strength = dRaw / maxVal;
          ((VisualNetworkData)baseObject).setTieStrength(relIdx, from, to, strength);
        }
      }
    }
  }
	
	public class RelationHandler implements MiniHandler
	{
		public void startElement(Attributes attributes)
		{
		  String relName = attributes.getValue("name");
		  boolean directed =
		    "true".equalsIgnoreCase(attributes.getValue("directed"));
		  ((VisualNetworkData)baseObject).addRelation(relName, directed);
		}
		
		public void endElement()
		{
		}
	}
	
	public class TimeSeriesHandler implements MiniHandler
	{
		public void startElement(Attributes attributes)
		{
		  String name = attributes.getValue("name");
			int length = Integer.parseInt(attributes.getValue("length"));
			workingTimeSeries = new TimeSeriesNodeData(((VisualNetworkData)baseObject), name, length);
		}
		
		public void endElement()
		{
			((VisualNetworkData)baseObject).addTimeSeries(workingTimeSeries);
			workingTimeSeries = null;
		}
	}
	
	public class NodeValuesHandler implements MiniHandler
	{
		public void startElement(Attributes attributes)
		{
			tsTime = Integer.parseInt(attributes.getValue("time"));
		}
		
		public void endElement()
		{
			tsTime = -1;
		}
	}

	public class NodeValueHandler implements MiniHandler
	{
		public void startElement(Attributes attributes)
		{
			int node = Integer.parseInt(attributes.getValue("node"));
			double val = Double.parseDouble(attributes.getValue("value"));
			workingTimeSeries.setValue(node, tsTime, val);
		}
		
		public void endElement()
		{
		}
	}

	// a little helper class for PartitionTreeHandler
	protected class TNode extends Object
	{
		protected TNode parent;
		protected int[] actors;
		protected String name;
		protected Vector children;
		protected PartitionTree pTree;
		
		public TNode(TNode parent, int[] actors, String name)
		{
			this.parent = parent;
			this.actors = actors;
			this.name = name;
			this.children = new Vector();
			if(parent != null)
			{
				parent.children.addElement(this);
			}
			
			// fill this in when done parsing
			this.pTree = null;
		}
	}

	public class PartitionTreeHandler implements MiniHandler
	{
		public void startElement(Attributes attributes)
		{
			// gather up actor list
      String strActorList = attributes.getValue("actors");
      String name = attributes.getValue("name");
      StringTokenizer st = new StringTokenizer(strActorList, ",");
			int[] actorList = new int[st.countTokens()];
			int ai=0;
      while(st.hasMoreTokens())
      {
        String sActor = st.nextToken().trim();
        int aIdx = Integer.parseInt(sActor);
				actorList[ai] = aIdx;
				ai++;
      }
			TNode newTree = new TNode(workingPTree, actorList, name);
			workingPTree = newTree;
		}
		
		public void endElement()
		{
			// finish up this tree?
			int numChildren = workingPTree.children.size();
			PartitionTree pTree = new PartitionTree(numChildren,
				workingPTree.actors);
			pTree.setName(workingPTree.name);
			
			// store this PartitionTree for future use
			workingPTree.pTree = pTree;

			for(int i=0; i<numChildren; i++)
			{
				TNode child = (TNode)workingPTree.children.elementAt(i);
				pTree.setChild(i, child.pTree);
			}

			// is this a top level tree?
			if(workingPTree.parent == null)
			{
				partitionCollections.addElement(pTree);
			}

			// pop up
			workingPTree = workingPTree.parent;
		}
	}

	public class PartitionSetHandler implements MiniHandler
	{
		protected String name;
		
		public PartitionSetHandler()
		{
			this.name = "<error>";
		}
		
		public void startElement(Attributes attributes)
		{
			workingPSets = new Vector();
			workingPSetLabels = new Vector();
			name = attributes.getValue("name");
		}
		
		public void endElement()
		{
			PartitionSet pSet = new PartitionSet(workingPSets,
				workingPSetLabels);
			pSet.setName(name);
			partitionCollections.addElement(pSet);
		}
	}

	public class PSetElemHandler implements MiniHandler
	{
		public void startElement(Attributes attributes)
		{
      // share this stuff with blockmodel reader
			workingName = attributes.getValue("label");
      workingPartition = new BasicPartition(((VisualNetworkData)baseObject).getSize());
      partitionIdx = 0;
		}
		
		public void endElement()
		{
			workingPSets.addElement(workingPartition);
			workingPSetLabels.addElement(workingName);
		}
	}
}