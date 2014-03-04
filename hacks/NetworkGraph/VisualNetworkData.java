/**
 * VisualNetworkData.java
 *
 * Change History:
 *   6/12/2003 - Moved XML Reading capability into this class.
 *   8/28/2003 - Moved 'actors' into NetworkData
 *  11/10/2004 - Added a (Graph g) constructor
 *  11/28/2008 - Added 3D capabilities (hopefully retaining backward compatibility)
 *
 * Copyright (c) 2000-2008 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.awt.Color;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.io.*;
import java.util.*;
import java.text.*;

import com.wibinet.app.*;
import com.wibinet.gui.*;
import com.wibinet.math.CartesianObject;
import com.wibinet.math.MultidimensionalScaler;
import com.wibinet.math.Partition;
import com.wibinet.math.PartitionCollection;
import com.wibinet.util.*;

public class VisualNetworkData extends NetworkData
{
	protected String name;
	protected boolean[] nodeFrozen;
	protected double[] xPos;
	protected double[] yPos;

	protected double[] zPos;
	protected boolean is3D;
  
	protected LabelModel lModel;
  
	// principally for x3d exporter
	protected static Color[] colorMap = {
		Color.red,
		Color.blue,
		Color.green,
		new Color(0x00FF9900), // my orange
		new Color(0x0078378B),
		Color.yellow,
		Color.magenta,
		Color.white,
		new Color(0x00CDAD00),
		new Color(0x008B3626)
	};
	
	public VisualNetworkData(NetworkData nData)
	{
		super(nData);
		comConstructor(nData.getSize(), "[untitled]"); // use ctr?
	}
	
	public VisualNetworkData(com.wibinet.math.Graph g, String name)
	{
		super(g, name);
		comConstructor(g.getNodeCount(), name);
	}
	
	private void comConstructor(int size, String name)
	{
		nodeFrozen = new boolean[size];
		xPos = new double[size];
		yPos = new double[size];
		zPos = new double[size];
		is3D = false;
    
		for(int li=0; li<size; li++)
		{
			nodeFrozen[li] = false;
		}
    
		lModel = new NetworkLabelModel();
		this.name = name;
	}
  
	public VisualNetworkData(VisualNetworkData vnd)
	{
		super(vnd);
		int size = vnd.getSize();
		nodeFrozen = new boolean[size];
		xPos   = new double[size];
		yPos   = new double[size];
		zPos   = new double[size];

		for(int li=0; li<size; li++)
		{
			nodeFrozen[li] = vnd.nodeFrozen[li];
			xPos[li] = vnd.xPos[li];
			yPos[li] = vnd.yPos[li];
			zPos[li] = vnd.zPos[li];
		}
    
		lModel = new NetworkLabelModel();
		name = "<untitled>"; // use ctr?
	}

	public void setName(String name)
	{
		this.name = name;
	}
  
	public String getName()
	{
		return name;
	}
  
	public void randomize(int min, int max)
	{
		randomize(min, max, min, max);
	}
	
	public void randomize(int xMin, int xMax, int yMin, int yMax)
	{
		randomize(xMin, xMax, yMin, yMax, 0, 0);
	}
  
	public void randomize(int xMin, int xMax, int yMin, int yMax, int zMin, int zMax)
	{
		Random random = new Random();

		// find center
		double xCenter = (0.5)*(xMin+xMax);
		double yCenter = (0.5)*(yMin+yMax);
		double zCenter = (0.5)*(zMin+zMax);
    
		// get partition
		Partition p = getPartition();
		int pCt = p.getPartitionCount();
    
		double angleIncr = Math.PI * 2.0 / (1.0 * pCt);
		double angleOff = random.nextDouble() * Math.PI * 2.0;
    
		double majorFactor = 1.0 / (Math.sin(angleIncr / 2.0) + 1.0);
		double minorFactor = Math.sin(angleIncr / 2.0) * majorFactor;
		double xRadius = Math.abs(xMax-xMin) * 0.5;
		double yRadius = Math.abs(yMax-yMin) * 0.5;
		double zRadius = Math.abs(zMax-zMin) * 0.5;
    
		// adjust for one-partition case?
		if(pCt == 1)
		{
			majorFactor = 0.0;
			minorFactor = 1.0;
		}
    
		for(int ni=0; ni < xPos.length; ni++)
		{
			// find partition center
			int pidx = p.getPartition(ni);
			double xpCenter = xCenter + xRadius * Math.cos(angleOff + pidx * angleIncr) * majorFactor;
			double ypCenter = yCenter + yRadius * Math.sin(angleOff + pidx * angleIncr) * majorFactor;
			double zpCenter = zCenter;
  
			// get random distance & angle
			double minRadius = Math.sqrt(random.nextDouble()) * minorFactor;
			double minAngle = random.nextDouble() * Math.PI * 2.0;
			double phi = random.nextDouble() * Math.PI - Math.PI/2.0;
			double zAdjRadius = minRadius * Math.cos(phi);
			xPos[ni] = xpCenter + Math.cos(minAngle) * xRadius * zAdjRadius;
			yPos[ni] = ypCenter + Math.sin(minAngle) * yRadius * zAdjRadius;
			zPos[ni] = zpCenter + Math.sin(phi) * zRadius;
	    }
	    
	    DataChangeEvent dce = new DataChangeEvent(this, DataChangeEvent.LOCATION_CHANGED, 
	    		DataChangeEvent.ALL_ROWS, DataChangeEvent.ALL_COLUMNS);
	    fireDataChanged(dce);
	}
  
  public void applyMDS(int xMin, int xMax, int yMin, int yMax)
  {
		MultidimensionalScaler scaler = new MultidimensionalScaler();
		CartesianObject[] cobj = scaler.scale(this, 2, true); // scale on dissimilarity
		
		double xScale = 1.0 * (xMax-xMin);
		double yScale = 1.0 * (yMax-yMin);
		
    for(int i=0; i<xPos.length; i++)
		{
			xPos[i] = xMin + (int)(xScale * cobj[i].getCoord(0));
			yPos[i] = yMin + (int)(yScale * cobj[i].getCoord(1));
		}
		
    DataChangeEvent dce = new DataChangeEvent(this, DataChangeEvent.LOCATION_CHANGED,
      DataChangeEvent.ALL_ROWS, DataChangeEvent.ALL_COLUMNS);
    fireDataChanged(dce);
  }

  public void setLabelsWithCentrality()
  {
    NumberFormat format = NumberFormat.getInstance();
    format.setMaximumFractionDigits(2);
    format.setMinimumFractionDigits(2);
    for(int ai=0; ai<actors.length; ai++)
    {
      //labels[ni] = "[" + format.format(getInDegreeCentrality(ni)) + "/" + 
      //  format.format(getOutDegreeCentrality(ni)) + "]";
      actors[ai].setName("" + format.format(getDegreeCentrality(ai)));
    }
  }
  
  public String getLabel(int member)
  {
    return actors[member].getName();
  }
  
  public void setLabel(int member, String label)
  {
    this.actors[member].setName(label);
    fireDataChanged(new DataChangeEvent(this, DataChangeEvent.LABEL_CHANGED, member, member));
  }
  
  public double getXPos(int member)
  {
    return xPos[member];
  }
  
  public void setXPos(int member, double pos)
  {
    this.xPos[member] = pos;
  }
  
  public double getYPos(int member)
  {
    return yPos[member];
  }
  
  public void setYPos(int member, double pos)
  {
    this.yPos[member] = pos;
  }
  
  public double getZPos(int member)
  {
	  return zPos[member];
  }
  
  public void setZPos(int member, double pos)
  {
	  this.zPos[member] = pos;
  }
  
  public synchronized void addNode()
  {
    super.addNode(false); // don't fire events yet
    int newSize = getSize(); // values have already been adjusted
    Actor[] newActors = new Actor[newSize];
    double[] newx = new double[newSize];
    double[] newy = new double[newSize];
    double[] newz = new double[newSize];
    boolean[] newfreeze = new boolean[newSize];
    for(int li=0; li<newSize-1; li++)
    {
      newActors[li] = actors[li];
      newx[li] = xPos[li];
      newy[li] = yPos[li];
      newz[li] = zPos[li];
  	  newfreeze[li] = nodeFrozen[li];
    }
    newActors[newSize-1] = new Actor(this, "Actor " + (newSize-1));
    newx[newSize-1] = 0.0;
    newy[newSize-1] = 0.0;
    newz[newSize-1] = 0.0;
    newfreeze[newSize-1] = false;
    actors = newActors;
    xPos = newx;
    yPos = newy;
    zPos = newz;
    nodeFrozen = newfreeze;
    
    // now fire event
    DataChangeEvent dce = new DataChangeEvent(this, DataChangeEvent.NODE_INSERTED, newSize-1, newSize-1);
    fireDataChanged(dce);
  }
  
  public synchronized void deleteNode(int nodeIndex)
  {
    super.deleteNode(nodeIndex, false); // don't fire events yet
    int newSize = getSize(); // values have already been adjusted
    Actor[] newActors = new Actor[newSize];
    double[] newx = new double[newSize];
    double[] newy = new double[newSize];
    double[] newz = new double[newSize];
    boolean[] newfreeze = new boolean[newSize];
    for(int li=0; li<newSize; li++)
    {
      int src = li;
      if(src >= nodeIndex) src++;
      newActors[li] = actors[src];
      newx[li] = xPos[src];
      newy[li] = yPos[src];
      newz[li] = zPos[src];
  		newfreeze[li] = nodeFrozen[src];
    }
    actors = newActors;
    xPos = newx;
    yPos = newy;
    zPos = newz;
    nodeFrozen = newfreeze;
    
    // now fire event
    DataChangeEvent dce = new DataChangeEvent(this, DataChangeEvent.NODE_DELETED, nodeIndex, nodeIndex);
    fireDataChanged(dce);
  }
  
  public void setNodeFrozen(int member, boolean frozen)
  {
    nodeFrozen[member] = frozen;
  }
  
  public boolean isNodeFrozen(int member)
  {
    return nodeFrozen[member];
  }
  
  // this is being called by SpringEmbedder.copyData()  Maybe it should do something?
  public void dataFreeze()
  {
    // probably should grab a lock on this object or something
  }
  
  public void unfreezeData()
  {
    // see .dataFreeze()
  }
  
  public LabelModel getLabelModel()
  {
    return lModel;
  }
  
  public void permute(int map[])
  {
    // permute data
    super.permute(map);
    
    // permute labels & such
  	Actor[] pactors = new Actor[size];
  	boolean[] pnodeFrozen = new boolean[size];
  	double[] pxPos = new double[size];
  	double[] pyPos = new double[size];
  	double[] pzPos = new double[size];
    for(int ai=0; ai<size; ai++)
    {
      pactors[ai] = actors[map[ai]];
      pnodeFrozen[ai] = nodeFrozen[map[ai]];
      pxPos[ai] = xPos[map[ai]];
      pyPos[ai] = yPos[map[ai]];
      pzPos[ai] = zPos[map[ai]];
    }
    actors = pactors;
    nodeFrozen = pnodeFrozen;
    xPos = pxPos;
    yPos = pyPos;
    zPos = pzPos;
    
    DataChangeEvent dce = new DataChangeEvent(this,
      DataChangeEvent.LABEL_CHANGED,
      DataChangeEvent.ALL_ROWS, DataChangeEvent.ALL_COLUMNS);
    fireDataChanged(dce);
  }
  
  public void writeXML(PrintWriter out) throws IOException
  {	
  	// get this from a property
  	String dtdLoc = "intjar:/dtd/network.dtd";

  	// write header (encoding right?)
  	out.println("<?xml version=\"1.0\" encoding=\"US-ASCII\"?>");
  	out.println("<!DOCTYPE network SYSTEM \"" + dtdLoc + "\">");
  	/* out.println("<!DOCTYPE network [");

		// dump from jar
		InputStream is = VisualNetworkData.class.getResourceAsStream("/dtd/network.dtd");
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader dtdIn = new BufferedReader(isr);
		String line = dtdIn.readLine();
		while(line != null)
		{
			out.println("  " + line);
			line = dtdIn.readLine();
		}
		is.close();

  	out.println("]>"); */
  	out.println();
  	out.println("<!-- Written by Wibinet Tenkrow v1.0 -->");
  	out.println();
  	
  	out.println("<network name=\"" + XMLReader.xmlEncode(getName()) + "\">");
  	out.println();
  	
  	// write out actor list
  	out.println("<actorlist>");
  	for(int ai=0; ai<size; ai++)
  	{
      out.println("  <actor xpos=\"" + getXPos(ai) +
        "\" ypos=\"" + getYPos(ai) + "\">" +
        XMLReader.xmlEncode(getLabel(ai)) + "</actor>");
  	}
  	out.println("</actorlist>");
  	out.println();
  	
  	// write out blockmodels?
    int bCt = blockmodels.size();
    for(int bi=0; bi<bCt; bi++)
    {
      try
      {
        Blockmodel model = (Blockmodel)blockmodels.elementAt(bi);
        String facClassName = model.getFactoryClass();
        Class facClass = Class.forName(facClassName);
        BlockmodelFactory fac = (BlockmodelFactory)facClass.newInstance();
        Hashtable props = fac.getProperties(model);
        out.println("<blockmodel factory=\""+facClassName+"\" name=\""+
					XMLReader.xmlEncode(model.getTitle())+"\">");
        Enumeration keys = props.keys();
        while(keys.hasMoreElements())
        {
          Object key = keys.nextElement();
          out.println("  <bparam name=\""+key+"\" value=\""+
						XMLReader.xmlEncode(props.get(key).toString())+"\">");
        }
        
        // print partitions
        Partition p = model.getPartition();
				p.writeXML(out, "  ");
        out.println("</blockmodel>");
      }
      catch(Throwable t)
      {
        Application.handleNonFatalThrowable(t);
      }      
    }
  	
		// write out partition collections
		int pcCt = getPartitionCollectionCount();
		for(int pci=0; pci < pcCt; pci++)
		{
			PartitionCollection pc = getPartitionCollection(pci);
			pc.writeXML(out, "");
			out.println();
		}
		
  	// write out relations
  	for(int ri=0; ri<relations.length; ri++)
  	{
  		relations[ri].writeXML(out);
  		out.println();
  	}
		
		// write out time series data if there is any
		for(int ti=0; ti<timeSeries.size(); ti++)
		{
			TimeSeriesNodeData tsnd = (TimeSeriesNodeData)timeSeries.elementAt(ti);
			tsnd.writeXML(out);
		}
		
  	out.println("</network>");
  	out.flush();
  }
  
  public void writeX3D(PrintWriter out) throws IOException
  {
	  writeX3D(out, new Hashtable());
  }
  
  public void writeX3D(PrintWriter out, Map props) throws IOException
  {
	  // write header
	  out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	  out.println("<!DOCTYPE X3D PUBLIC " +
			  "\"ISO//Web3D//DTD X3D 3.0//EN\" " +
			  "\"http://www.web3d.org/specifications/x3d-3.0.dtd\">");
	  out.println();
	  out.println("<X3D profile=\"Immersive\" version=\"3.0\">");
	  out.println("  <Scene>");
	  
	  int numPts = getSize();
	  double maxTie = getMaxTieStrength();

	  Double dRadius = ((Double)props.get("radius"));
	  double radius = (dRadius==null)?3.0:dRadius.doubleValue();
	  Double dScaleFactor = ((Double)props.get("scaleFactor"));
	  double scaleFactor = (dScaleFactor==null)?1.0:dScaleFactor.doubleValue();
	  Double dLwScale = ((Double)props.get("lwScale"));
	  double lwScale = (dLwScale==null)?1.0:dLwScale.doubleValue();
	  
	  double radFactor = 1.0;
	  Map radiiMap = ((Map)props.get("radiimap"));
	  Map colorPreMap = ((Map)props.get("colormap"));
	  if(radiiMap != null) {
		  radFactor = radius; // kludgy..
	  }
	  Map nodeColorMap = null;
	  if(colorPreMap != null)
	  {
		  // need to convert string values to numbers
		  Iterator iter = colorPreMap.keySet().iterator();
		  HashSet values = new HashSet();
		  while(iter.hasNext()) {
			  values.add(colorPreMap.get(iter.next()));
		  }
		  HashMap colorStepMap = new HashMap();
		  iter = values.iterator();
		  int idx = 0;
		  while(iter.hasNext()) {
			  Object key = iter.next();
			  colorStepMap.put(key, colorMap[idx % colorMap.length]);
			  System.err.println("cm: "+key+"/"+idx);
			  idx++;
		  }
		  
		  // populate actual map
		  iter = colorPreMap.keySet().iterator();
		  nodeColorMap = new HashMap();
		  while(iter.hasNext()) {
			  Object key = iter.next();
			  nodeColorMap.put(key, colorStepMap.get(colorPreMap.get(key)));
		  }
	  }
	  
	  // draw lines
	  for(int i1=0; i1<numPts; i1++)
	  {
		  double radius1 = radius;
		  if(radiiMap != null) {
			  String l1 = getLabel(i1);
			  radius1 = ((Double)radiiMap.get(l1)).doubleValue() * radFactor;
		  }

		  // if doing self-loops, draw here

		  for(int i2=i1+1; i2<numPts; i2++)
		  {
			  double radius2 = radius;
			  if(radiiMap != null) {
				  String l2 = getLabel(i2);
				  radius2 = ((Double)radiiMap.get(l2)).doubleValue() * radFactor;
			  }

			  double tieOut = getTieStrength(i1, i2)/maxTie;
			  double tieIn = getTieStrength(i2, i1)/maxTie;
			  
			  if(tieOut != 0 | tieIn != 0)
			  {
				  double dx = (xPos[i2] - xPos[i1]) * scaleFactor;
				  double dy = (yPos[i2] - yPos[i1]) * scaleFactor;
				  double dz = (zPos[i2] - zPos[i1]) * scaleFactor;
				  double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
	
				  // check for overlap
				  boolean overlap = dist < radius1 + radius2;
				  if(!overlap)
				  {
					  double cx = (xPos[i1]*scaleFactor + dx/2);
					  double cy = (yPos[i1]*scaleFactor + dy/2);
					  double cz = (zPos[i1]*scaleFactor + dz/2); 
					  
					  double rot = Math.acos(dy/dist);
					  
					  // draw arrow here
					  Color arrowColor = Color.green;
					  out.println("    <Transform translation=\""+
							  cx + " " + cy + " " + cz + "\">");
					  out.println("      <Transform rotation=\""+
							  dz + " 0  " + (-dx) + " " + rot + "\">");
					  out.println("        <Shape>");
					  out.println("          <Appearance>");
					  out.println("            <Material diffuseColor=\""+colorString(arrowColor)+"\"/>");
					  out.println("          </Appearance>");
					  out.println("          <Extrusion spine=\" 0 "+(-dist/2)+
							  " 0  0 "+(dist/2)+" 0\""+
							  " scale=\""+tieIn*lwScale+" "+tieIn*lwScale+" "+
							  tieOut*lwScale+" "+tieOut*lwScale+"\"/>");
					  out.println("        </Shape>");
					  out.println("    </Transform>");
					  out.println("  </Transform>");
					  
					  // rotation axis = cross product
					  // rotation angle = arcsin of something or other
				  }
			  }
		  }
	  }
	  
	  // need to find center x & z for rotations
	  double xtot = 0.0;
	  double ztot = 0.0;
	  for(int ni=0; ni<numPts; ni++)
	  {
		  xtot += xPos[ni];
		  ztot += zPos[ni];
	  }
	  double centerx = xtot/numPts;
	  double centerz = ztot/numPts;

	  // draw nodes
	  for(int ni=0; ni<numPts; ni++)
	  {
		  // Color nodeColor = colorModel.getColor(ni);
		  Color nodeColor = Color.red;
		  if(nodeColorMap != null) {
			  nodeColor = (Color)nodeColorMap.get(getLabel(ni));
			  if(nodeColor == null) {
				  System.err.println("no color for: " + getLabel(ni));
				  nodeColor = Color.red;
			  }
		  }
		  
		  if(radiiMap != null) {
			  radius = ((Double)radiiMap.get(getLabel(ni))).doubleValue() * radFactor;
		  }
		  Color textColor = Color.yellow;
		  out.println("    <Transform translation=\""+
				  xPos[ni]*scaleFactor + " " + 
				  yPos[ni]*scaleFactor + " " + 
				  zPos[ni]*scaleFactor + "\">");
		  out.println("      <Shape>");
		  out.println("        <Appearance>");
		  out.println("          <Material diffuseColor=\""+colorString(nodeColor)+"\"/>");
		  out.println("        </Appearance>");
		  out.println("        <Sphere radius=\""+radius+"\"/>");
		  out.println("      </Shape>");
		  double rot = Math.atan2(xPos[ni]-centerx, zPos[ni]-centerz);
		  out.println("      <Transform translation=\"0 -"+(radius+1)+" 0\""+
				  " rotation=\"0 1 0 "+rot+"\">>");
		  out.println("        <Shape>");
		  out.println("          <Appearance>");
		  out.println("            <Material diffuseColor=\""+colorString(textColor)+"\"/>");
		  out.println("          </Appearance>");
		  out.println("          <Text string='\""+XMLReader.xmlEncode(getLabel(ni))+"\"'>");
		  out.println("            <FontStyle size='2' justify='\"MIDDLE\"'/>");
		  out.println("          </Text>");
		  out.println("        </Shape>");
		  out.println("      </Transform>");
		  
		  out.println("    </Transform>");
		  		  
		  // draw label here?
		  String label = getLabel(ni);
	  }
	  
	  out.println("  </Scene>");
	  out.println("</X3D>");
	  out.flush();
  }
  
  protected String colorString(Color c)
  {
	  float[] comps = c.getRGBComponents(null);
	  return ""+comps[0]+" "+comps[1]+" "+comps[2];
  }
  
  // some version of this should be in NetworkData.java
  protected double getMaxTieStrength()
  {
	  int numPts = getSize();

	  // calculate max tie strength
	  double tmpMax = 0.0;
	  for(int i1=0; i1<numPts; i1++)
	  {
		  for(int i2=i1+1; i2<numPts; i2++)
		  {
			  // ignore infinite values in this step
			  double to = Math.abs(getTieStrength(i1, i2));
			  double ti = Math.abs(getTieStrength(i2, i1));
			  if(Double.isInfinite(to)) to=0.0;
			  if(Double.isInfinite(ti)) ti=0.0;
			  double ts = Math.max(to, ti);
			  if(ts > tmpMax)
			  {
				  tmpMax = ts;
			  }  
		  }
	  }
	  if(tmpMax == 0.0)
	  {
		  tmpMax = 1.0; // no div by zero here!
	  }
	  return tmpMax;
  }
  
  public class NetworkLabelModel extends LabelModel
  {
    public int getLabelCount()
    {
      return actors.length;
    }
    
	  public String getLabel(int idx)
	  {
	    return actors[idx].getName();
	  }
	  
	  public void setLabel(int idx, String label)
	  {
      actors[idx].setName(label);
	  }
	  
		public boolean isLabelEditable(int index)
		{
		  return true;
		}
  }
}
