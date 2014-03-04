package com.wibinet.networks;

import java.io.*;
import java.util.*;
import com.wibinet.util.*;
import com.wibinet.math.Graph;
import com.wibinet.math.MatrixGraph;
import com.wibinet.math.ListGraph;
import com.wibinet.math.ValuedListGraph;

public class PajekFileImporter implements NetworkDataImporter
{
	public final static int MATRIX_TYPE = 1;
	public final static int LIST_TYPE = 2;
	public final static int VALUED_LIST_TYPE = 3;
	
	protected int graphType;
	
	/**
	 * Basic constructor.  Takes one argument <code>graphType</code> that defines
	 * the underlying graph structure used to store network data.
	 * 
	 * @param graphType the type of graph that should be used in importing Pajek files
	 */
	public PajekFileImporter(int graphType)
	{
		this.graphType = graphType;
	}
	
	/**
	 * Default constructor.  The default <code>PajekFileImporter</code> uses
	 * <code>com.wibinet.math.ValuedListGraph</code> as the underlying storage
	 * for network data.
	 * 
	 * @see com.wibinet.math.ValuedListGraph
	 */
	public PajekFileImporter()
	{
		this(VALUED_LIST_TYPE);
	}
	
	public boolean acceptsFile(File f)
	{
		return(f.getName().toLowerCase().endsWith(".net"));
	}

	public String getName()
	{
		return "Pajek File (.net)";
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
		protected int lineno;
		protected VisualNetworkData vnd;
		
		public Parser()
		{
			this.vnd = null;
			this.lineno = -1;
		}

		public VisualNetworkData getNetwork()
		{
			return vnd;
		}
		
		public void parse(InputStream is) throws IOException
		{
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader in = new BufferedReader(isr, 8096);
			
			// read lines, break on sections
			String line = in.readLine().trim();
			lineno = 1;
			while(line != null) {
				
				// blank line
				if(line.length()==0) {

					// skip
					line = in.readLine();
					lineno++;

				} else if(line.startsWith("*")) {
					// strip *, trim to keyword, and switch
					String remainder = line.substring(1).trim().toLowerCase();
					StringTokenizer st = new StringTokenizer(remainder);
					String keyword = st.nextToken();
					if("vertices".equals(keyword)) {
						
						// get number of actors & set up
						String sActorCt = st.nextToken();
						int actorCt = Integer.parseInt(sActorCt);

						Graph g = null;
						if(graphType == MATRIX_TYPE)
						{
							g = new MatrixGraph(actorCt);
						}
						else if(graphType == LIST_TYPE)
						{
							g = new ListGraph(actorCt);
						}
						else if(graphType == VALUED_LIST_TYPE)
						{
							g = new ValuedListGraph(actorCt);
						}
						NetworkData nData = new NetworkData(g, "Pajek");
						vnd = new VisualNetworkData(nData);
						
						line = parseVertices(in);

						
					} else if("arcs".equals(keyword)) {
					
						line = parseArcs(in);
						
					} else {
						handleExpectedError("known keyword", keyword);
					}
				} else {
					handleExpectedError("Keyword", null);
				}
			}
		}
		
		protected String parseVertices(BufferedReader in) throws IOException
		{
			// guess we know how many actors to read info about...
			int nSize = vnd.getSize();
			int ai = 0;
			String line = in.readLine().trim();
			lineno++;

			while(ai < nSize)
			{
				if(line.length()==0) {
					// skip
					line = in.readLine().trim();
					lineno++;
				} else {
					QuotedStringTokenizer st = new QuotedStringTokenizer(line);
					int anum = Integer.parseInt(st.nextToken());
					String aname = st.nextToken();
					
					double xp = 0.0;
					double yp = 0.0;
					double zp = 0.0;

					if(st.hasMoreTokens())
					{
						xp = Double.parseDouble(st.nextToken());
						yp = Double.parseDouble(st.nextToken());
						zp = Double.parseDouble(st.nextToken());
					}
					
					if(anum != ai+1) {
						handleExpectedError("Vertex #"+anum, "Vertex #"+(ai+1));
					}
					
					vnd.setLabel(ai, aname);
					vnd.setXPos(ai, xp);
					vnd.setYPos(ai, yp);
					vnd.setZPos(ai, zp);
					ai++;
					
					line = in.readLine();
					lineno++;
				}				
			}
			
			return line;
		}

		protected String parseArcs(BufferedReader in) throws IOException
		{
			// can't trim here...could be EOF
			String line = in.readLine();
			lineno++;
			boolean done = false;
			while(!done)
			{
				// see if we're done
				if(line == null) {
					done = true;
					break;
				}
				
				line = line.trim();
				if(line.startsWith("*")) {
					done = true;
				} else if(line.length() == 0) {
					// skip
					line = in.readLine().trim();
					lineno++;
				} else {
					StringTokenizer st = new StringTokenizer(line);
					int i = Integer.parseInt(st.nextToken())-1;
					int j = Integer.parseInt(st.nextToken())-1;
					double ts = Double.parseDouble(st.nextToken());
					vnd.setTieStrength(i, j, ts);

					line = in.readLine();
					lineno++;
				}
			}
			
			return line;
		}

		protected void handleExpectedError(String expected) throws IOException
		{
			throw new IOException("File format error line: " + lineno +
					" expected "+expected+".");
		}

		protected void handleExpectedError(String expected, String found) throws IOException
		{
			throw new IOException("File format error line: " + lineno +
					" expected "+expected+" found '"+found+"'");
		}
	}
}
