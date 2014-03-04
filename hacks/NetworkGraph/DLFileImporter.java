/**
 * DLFileImporter.java
 *
 * Imports from UCINet/Pajek .dl files
 *
 * Copyright (c) 2003 Wibi Internet.
 * All rights reserved.
 */
 
package com.wibinet.networks;

import java.io.*;
import java.util.*;
import com.wibinet.math.ValuedListGraph;
import com.wibinet.math.Edge;

public class DLFileImporter implements NetworkDataImporter
{
	public String getName()
	{
		return "UCINET File (.dl)";
	}
	
	public boolean acceptsFile(File f)
	{
		return(f.getName().toLowerCase().endsWith(".dl"));
	}
	
	public int getType()
	{
		return SINGLE_RELATION;
	}
	
	
	public VisualNetworkData readData(InputStream is) throws IOException
	{
		Parser p = new Parser();
		p.parse(is);
		return p.getNetwork();
	}
	
	protected class Parser extends Object
	{
		protected StreamTokenizer st;
		protected Hashtable symbols;
		protected Hashtable labels;
		protected int labelIdx;
		
		protected VisualNetworkData vnd;
		
		public Parser()
		{
			this.symbols = new Hashtable();
			this.labels = new Hashtable();
			this.st = null;
			this.vnd = null;
		}
		
		public VisualNetworkData getNetwork()
		{
			return vnd;
		}
		
		public void parse(InputStream is) throws IOException
		{
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader in = new BufferedReader(isr);
			this.st = new StreamTokenizer(in);
			st.eolIsSignificant(false);

			st.whitespaceChars('=', '=');
			st.whitespaceChars(',', ',');
			st.wordChars(':', ':');

			// reset tables
			symbols.clear();
			labels.clear();
			labelIdx = 0;

			// reset
			this.vnd = null;
			
			// check for lead key
			readDLKey();
			
			// read chunks
			readChunks();
		}
		
		protected void readDLKey() throws IOException
		{
			st.nextToken();

			// only looking for the word 'dl'
			if(st.ttype != StreamTokenizer.TT_WORD)
			{
				handleExpectedError("'dl'");
			}
			if(!st.sval.equals("dl"))
			{
				handleExpectedError("'dl'");
			}
		}
		
		protected void readChunks() throws IOException
		{
			// is next thing a definition or a data block?
			boolean isDef = readDefinition();
			if(!isDef)
			{
				boolean moreStuff = readDataBlock();
				if(!moreStuff)
				{
					return;
				}
			}
			
			// read next chunk
			readChunks();
		}
		
		protected boolean readDefinition() throws IOException
		{
			st.nextToken();
			
			// is this EOF
			if(st.ttype == StreamTokenizer.TT_EOF)
			{
				// don't forget to push back
				st.pushBack();
				return false;
			}
			
			if(st.ttype != StreamTokenizer.TT_WORD)
			{
				handleExpectedError("string");
			}
			String label = st.sval.toLowerCase();
			if(label.endsWith(":"))
			{
				st.pushBack();
				return false;
			}
			st.nextToken();
			if(st.ttype == StreamTokenizer.TT_EOF)
			{
				handleExpectedError("string or number");
			}
			else if(st.ttype == StreamTokenizer.TT_NUMBER)
			{
				symbols.put(label, new Double(st.nval));
			}
			else
			{
				String value = st.sval.toLowerCase();
				symbols.put(label, value);
			}
			return true;
		}
		
		protected boolean readDataBlock() throws IOException
		{
			// what kind of block is this
			st.nextToken();
			if(st.ttype == StreamTokenizer.TT_EOF)
			{
				// end-of-file ok
				return false;
			}
			else if(st.ttype == StreamTokenizer.TT_NUMBER)
			{
				handleExpectedError("number");
			}
			
			String blockType = st.sval.substring(0, st.sval.length()-1).toLowerCase();
			if("labels".equals(blockType))
			{
				readLabels();
			}
			else if("data".equals(blockType))
			{
				readData();
			}
			
			return true; // maybe more stuff
		}
		
		protected void readLabels() throws IOException
		{
			// need to rewrite this for rows != cols
			Number n = (Number)symbols.get("n");
			if(n == null)
			{
				throw new IOException("Don't know how many labels to read at line: " 
					+ st.lineno());
			}
			int numLabels = n.intValue();
			for(int i=0; i<numLabels; i++)
			{
				st.nextToken();
				readNextLabel();
			}
		}
		
		protected void readData() throws IOException
		{
			// this should depend quite a bit on the format
			String format = (String)symbols.get("format");
			if(format == null)
			{
				format = "fullmatrix"; // default
			}
			
			if("fullmatrix".equals(format))
			{
				readFullmatrix();
			}
			else if("edgelist1".equals(format))
			{
				readEdgelist1();
			}
			else
			{
				throw new IOException("Can't read data format '"+format+"'");
			}
		}
		
		protected void readFullmatrix() throws IOException
		{
			throw new IOException("Fullmatrix format not supported yet");
		}
		
		protected void readEdgelist1() throws IOException
		{
			// grumble, read first token *before* turning off EOL thing
			st.nextToken();

			// grumble.  suddenly EOLs matter.  hope this works
			st.eolIsSignificant(true);
			
			
			// are labels embedded?
			boolean l_embed = "embedded".equals(symbols.get("labels"));
			
			// data structure
			Hashtable nodes = new Hashtable();
						
			while(st.ttype != StreamTokenizer.TT_EOF)
			{
				int i = -1;
				int j = -1;
				double value = 1.0;
				
				// read two labels
				if(l_embed)
				{
					i = readNextLabel();
					st.nextToken();
					j = readNextLabel();
				}
				else
				{
					i = readNextInteger();
					st.nextToken();
					j = readNextInteger();
				}
				
				// read (optional) value
				st.nextToken();
				if(st.ttype != StreamTokenizer.TT_EOL)
				{
					if(st.ttype != StreamTokenizer.TT_NUMBER)
					{
						handleExpectedError("number");
					}
					value = st.nval;
					st.nextToken();
				}
				
				// is there a nodeinfo set here?
				Integer nodeI = new Integer(i);
				NodeInfo nInfo = (NodeInfo)nodes.get(nodeI);
				if(nInfo == null)
				{
					nInfo = new NodeInfo(i);
					nodes.put(nodeI, nInfo);
				}
				nInfo.addEdge(new Edge(j, value));

				// is that the end?
				if(st.ttype == StreamTokenizer.TT_EOF)
				{
					break;
				}

				if(st.ttype != StreamTokenizer.TT_EOL)
				{
					handleExpectedError("end-of-line");
				}
				
				// load up for next line
				st.nextToken();
			}
			
			// create a set of edges
			int numNodes = labelIdx;
			Edge[][] edgeSets = new Edge[numNodes][];
			for(int ei=0; ei<numNodes; ei++)
			{
				NodeInfo ni = (NodeInfo)nodes.get(new Integer(ei));
				if(ni != null)
				{
					edgeSets[ei] = new Edge[ni.edges.size()];
					for(int ej=0; ej<edgeSets[ei].length; ej++)
					{
						edgeSets[ei][ej] = (Edge)(ni.edges.elementAt(ej));
					}
				}
				else
				{
					// it's okay...just means there's no outbound
					// edges from this node...
					edgeSets[ei] = new Edge[0];
				}
			}
			
			// graph
			ValuedListGraph g = new ValuedListGraph(edgeSets);
			
			NetworkData nd = new NetworkData(g, "Imported DL File");
			
			vnd = new VisualNetworkData(nd);
			
			// labels
			Enumeration keys = labels.keys();
			while(keys.hasMoreElements())
			{
				String label = (String)keys.nextElement();
				Integer nodeI = (Integer)labels.get(label);
				if(nodeI != null)
				{
					vnd.setLabel(nodeI.intValue(), label);
				}
			}
			
			// don't forget to set back?  shouldn't matter...
			st.eolIsSignificant(false);
		}
		
		protected int readNextLabel() throws IOException
		{
			String label = null;
			if(st.ttype == StreamTokenizer.TT_WORD)
			{
				label = st.sval;
			}
			else if(st.ttype == StreamTokenizer.TT_NUMBER)
			{
				label = "" + st.nval;
			}
			else
			{
				handleExpectedError("string or number");
			}
			
			Integer idx = (Integer)labels.get(label);
			if(idx == null)
			{
				idx = new Integer(labelIdx);
				labels.put(label, idx);
				labelIdx++;
			}
			return idx.intValue();
		}
		
		protected int readNextInteger() throws IOException
		{
			if(st.ttype != StreamTokenizer.TT_NUMBER)
			{
				handleExpectedError("number");
			}
			return (new Double(st.nval)).intValue();
		}
		
		
		protected void handleExpectedError(String expected) throws IOException
		{
			throw new IOException("File format error line: " + st.lineno() +
				" expected "+expected+" found '"+st.toString()+"'");
		}
	}
	
	protected class NodeInfo extends Object
	{
		public int idx;
		public Vector edges;
		
		public NodeInfo(int idx)
		{
			this.idx = idx;
			this.edges = new Vector();
		}
		
		protected void addEdge(Edge e)
		{
			edges.addElement(e);
		}
	}
}