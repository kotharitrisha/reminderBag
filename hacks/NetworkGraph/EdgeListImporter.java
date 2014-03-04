/**
 * EdgeListImporter.java
 *
 * A file format importer that reads a list of edges
 * from a file.
 *
 * Copyright (c) 2003 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.io.*;
import java.util.*;
import com.wibinet.math.ValuedListGraph;
import com.wibinet.math.Edge;

public class EdgeListImporter implements NetworkDataImporter
{
	public String getName()
	{
		return "Edge List File (.el)";
	}
	
	public boolean acceptsFile(File f)
	{
		return(f.getName().toLowerCase().endsWith(".el"));
	}

	public int getType()
	{
		return SINGLE_RELATION;
	}

	public VisualNetworkData readData(InputStream is) throws IOException
	{
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader in = new BufferedReader(isr);
		
		TreeMap labels = new TreeMap();
		TreeMap nodes = new TreeMap();
		int labelIdx = 0;
		
		String line = in.readLine();
		while(line != null)
		{
			StringTokenizer st = new StringTokenizer(line);
			String iLabel = st.nextToken();
			String jLabel = st.nextToken();
			Integer iIdx = (Integer)labels.get(iLabel);
			if(iIdx == null)
			{
				iIdx = new Integer(labelIdx);
				labelIdx++;
				labels.put(iLabel, iIdx);
			}
			Integer jIdx = (Integer)labels.get(jLabel);
			if(jIdx == null)
			{
				jIdx = new Integer(labelIdx);
				labelIdx++;
				labels.put(jLabel, jIdx);
			}
			double value = 1.0;
			if(st.hasMoreTokens())
			{
				value = Double.parseDouble(st.nextToken());
			}
			
			// invert?  there should probably be an option somewhere...
			value = 1.0/value;
			
			NodeInfo nInfo = (NodeInfo)nodes.get(iLabel);
			if(nInfo == null)
			{
				nInfo = new NodeInfo(iLabel);
				nodes.put(iLabel, nInfo);
			}
			nInfo.addEdge(new NamedEdge(jLabel, value));
			
			// don't forget to read a new line!
			line = in.readLine();
		}
		
		// the index map shows what the index for a node of a particular
		// label should be
		Hashtable indexMap = new Hashtable();
		Iterator labelIter = labels.keySet().iterator();
		int idx = 0;
		while(labelIter.hasNext())
		{
			String label = (String)(labelIter.next());
			indexMap.put(label, new Integer(idx));
			idx++;
		}

		// create a set of edges
		int numNodes = labels.size();
		Edge[][] edgeSets = new Edge[numNodes][];
		labelIter = labels.keySet().iterator();
		while(labelIter.hasNext())
		{
			String iLabel = (String)labelIter.next();
			NodeInfo ni = (NodeInfo)nodes.get(iLabel);
			int ei = ((Integer)indexMap.get(iLabel)).intValue();
			if(ni != null)
			{
				edgeSets[ei] = new Edge[ni.edges.size()];
				for(int ej=0; ej<edgeSets[ei].length; ej++)
				{
					NamedEdge ne = (NamedEdge)(ni.edges.elementAt(ej));
					edgeSets[ei][ej] = ne.createEdge(indexMap);
				}
			}
			else
			{
				edgeSets[ei] = new Edge[0];
			}
		}
		
		// make graph
		ValuedListGraph g = new ValuedListGraph(edgeSets);
		NetworkData nd = new NetworkData(g, "Imported Edge List File");
		VisualNetworkData vnd = new VisualNetworkData(nd);
		
		// labels
		Iterator iterator = indexMap.keySet().iterator();
		while(iterator.hasNext())
		{
			String label = (String)iterator.next();
			Integer nodeI = (Integer)indexMap.get(label);
			// System.err.println("[l,n]="+label+" "+nodeI);
			if(nodeI != null)
			{
				vnd.setLabel(nodeI.intValue(), label);
			}
		}
		return vnd;
	}
	
	// probably could create a NodeAndEdgeCatcher class or something
	// so we could re-use between EdgeListImporter and DLFileImporter
	protected class NodeInfo extends Object
	{
		public String label;
		public Vector edges;
		
		public NodeInfo(String label)
		{
			this.label = label;
			this.edges = new Vector();
		}
		
		protected void addEdge(NamedEdge e)
		{
			edges.addElement(e);
		}
	}
	
	protected class NamedEdge extends Object
	{
		protected Object alter;
		protected double tieValue;
		
		public NamedEdge(Object alter, double tieValue)
		{
			this.alter = alter;
			this.tieValue = tieValue;
		}
		
		public Edge createEdge(Map indexMap)
		{
			int alterIdx = ((Integer)indexMap.get(alter)).intValue();
			return new Edge(alterIdx, tieValue);
		}
	}
}
