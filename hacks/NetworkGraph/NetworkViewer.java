/**
 * NetworkViewer.java
 *
 * 9/19/03 - Note: In theory we could make node dragging faster by
 *   doing the following.  When a 'mouse down' event is received in
 *   a node, we could draw an 'background' image with all the arrows
 *   in it except the ones that point to or from the selected node.
 *   then, on drag events, we could only update those arrows into
 *   or out of the node being dragged.  When painting in 'drag mode',
 *   we'd then just start with the background image, draw in the
 *   new arrows, and then draw the nodes.
 *
 * 4/25/05 - Tried to (very carefully) remove the GraphicsHandler interface
 *   and replace it with a more standard Graphics2D interface.  This is
 *   because I want to try and incorporate some standard EPS stuff...
 *
 * 3/8/07 - Tried to (again, very carefully) move this out of the Tenkrow
 *   application package and into the general com.wibinet.networks package.
 *   Here goes bloody nothing!
 *   
 * 3/22/07 - Added a self-loops feature (less carefully) :)
 * 3/22/07 - Also did away with dashed lines (finally)
 *
 * 10/17/07 - Added setSelectedIndex() method
 * 10/17/07 - Added "activeTarget" construct
 *
 * Copyright (c) 2000-2007 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import com.wibinet.gui.*;
import com.wibinet.networks.*;

public class NetworkViewer extends JComponent implements RelationChangeListener,
  MouseListener, MouseMotionListener, KeyListener, ColorModelChangeListener,
	DataChangeListener, ItemSelectable
{
	protected VisualNetworkData nData;
  
	protected Rectangle2D[] nodeBounds;
	protected Font labelFont;
	protected int currentRelation;
	protected int activeNodeIdx;
	protected int activeTargetIdx;
	protected Color activeNodeColor;
	protected Color activeTargetColor;
	protected Color neighborhoodNodeColor;
	protected DPoint mouseInNodeFrame;
	
	// some things that can be get/set?
	protected NodeColorModel colorModel;
	protected Dimension preferredSize;
  
	// for faster drawing?
	protected Image showImage;
	protected int siX;
	protected int siY;
	protected Image workingImage;
	protected Graphics2D shadowGraphics; // probably a bad idea...
	protected boolean imageValid;
	protected Thread updaterThread;
  
	protected double maxTieStrength = 1.0;
  
	// edit mode
	protected boolean editable;
	protected boolean nodeAdditionAllowed;
	protected int editMode;
	public final static int MOVE  = 0;
	public final static int EDIT  = 1;
  
	// line style
	protected int lineStyle;
	protected boolean showArrowheads;
	protected double outerArrowAngle = 22.0 / 180.0 * Math.PI; // 22 degrees
	protected double innerArrowAngle = 45.0 / 180.0 * Math.PI; 
	protected double arrowHeadLength = 10.0;
	protected double maxThickness = 5.0;
	public final static int BRIGHTNESS = 0;
	// public final static int DASHED     = 1;
	public final static int THICKNESS  = 2;

	protected double controlLength = 20.0;
	protected double loopRadius = 40.0;
	protected double openAngle = 5.0 * Math.PI / 180.0;

	protected boolean showLabels;
	protected double bubbleSize = 8.0; // probably poorly named
  
	protected boolean showOutNodes;
	protected boolean showInNodes;
	
	protected boolean selfLoops;

	protected boolean ringLock;
	protected int rlFocusIdx;
	
	protected Vector itemListeners;
	
	protected final static float WRAP_WIDTH = (float)90.0;
	protected final static double BORDER = 2.0;
  
	public NetworkViewer(VisualNetworkData nData)
	{
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		this.labelFont = new Font("SansSerif", Font.PLAIN, 12);
		this.preferredSize = new Dimension(300, 300);
		this.editable = true;
		this.nodeAdditionAllowed = true;
		
		editMode = EDIT;
		lineStyle = BRIGHTNESS;
		showArrowheads = true;
		showLabels = true;
		maxThickness = 5.0;
		activeNodeIdx = -1;
		activeTargetIdx = -1;
		activeNodeColor = Color.red;
		activeTargetColor = Color.blue; // ?
		neighborhoodNodeColor = Color.blue;
		showOutNodes = false;
		showInNodes = false;
		selfLoops = true;

		ringLock = false;
		rlFocusIdx = -1;
		
		// think this is the right place to do this
		setNetwork(nData);
		
		this.itemListeners = new Vector();

		// initialize quick drawing images
		showImage = null;
		workingImage = null;
		shadowGraphics = null;
		siX = 0;
		siY = 0;
		imageValid = false;
		updaterThread = new UpdaterThread();
		updaterThread.start();
	}
	
	public void setNetwork(VisualNetworkData nData)
	{
		if(this.nData != null) {
			this.nData.removeDataChangeListener(this);
		}
		this.nData = nData;
		nData.addDataChangeListener(this);
		
		// reset all state pointers
		this.currentRelation = 0;
		this.activeNodeIdx = -1;
		this.activeTargetIdx = -1;
		this.rlFocusIdx = -1;
		
		// other object stuff...
		colorModel = new NodeColorModel(nData);
		initNodeBounds();
		invalidateImage(); // don't know if this is necessary here
	}
	
	public VisualNetworkData getNetwork()
	{
		return nData;
	}
  
	public void addItemListener(ItemListener il)
	{
		itemListeners.addElement(il);
	}
	
	public void removeItemListener(ItemListener il)
	{
		itemListeners.removeElement(il);
	}
	
	protected void fireItemEvent(ItemEvent ie)
	{
		for(int i=0; i<itemListeners.size(); i++)
		{
			ItemListener il = (ItemListener)itemListeners.elementAt(i);
			il.itemStateChanged(ie);
		}
	}
	
	public Object[] getSelectedObjects()
	{
		if(activeNodeIdx == -1)
		{
			return new Object[0];
		}
		Object[] ret = new Object[1];
		ret[0] = nData.getActor(activeNodeIdx);
		return ret;
	}
	
	public int getSelectedIndex()
	{
		return activeNodeIdx;
	}
	
	public void setSelectedIndex(int idx)
	{
		this.activeNodeIdx = idx;
	}
	
	public int getSelectedTarget()
	{
		return activeTargetIdx;
	}
	
	public void setSelectedTarget(int idx)
	{
		this.activeTargetIdx = idx;
	}
	
	public boolean isEditable()
	{
		return editable;
	}
	
	public void setEditable(boolean editable)
	{
		this.editable = editable;
	}
	
	public boolean isNodeAdditionAllowed()
	{
		return nodeAdditionAllowed;
	}

	public void setNodeAdditionAllowed(boolean nodeAdditionAllowed)
	{
		this.nodeAdditionAllowed = nodeAdditionAllowed;
	}
	
	public Dimension getPreferredSize()
	{
		// should be more complicated than this
		return preferredSize;
	}
	
	public void setPreferredSize(Dimension preferredSize)
	{
		this.preferredSize = new Dimension(preferredSize);
	}
	
	public void setEditMode(int mode)
	{
		this.editMode = mode;
	}
  
	public void setShowArrowheads(boolean showArrowheads)
	{
		this.showArrowheads = showArrowheads;
		invalidateImage();
	}
  
	public void setShowLabels(boolean showLabels)
	{
		this.showLabels = showLabels;
		invalidateImage();
	}
	
	public void setRingLock(boolean ringLock)
	{
		this.ringLock = ringLock;
		rlFocusIdx = -1;
	}
	
	public boolean getRingLock()
	{
		return ringLock;
	}
  
	public void setLineStyle(int lineStyle)
	{
		this.lineStyle = lineStyle;
		invalidateImage();
	}
  
	public void setShowOutNodes(boolean showOutNodes)
	{
		this.showOutNodes = showOutNodes;
		invalidateImage();
	}
  
	public boolean getShowOutNodes()
	{
		return showOutNodes;
	}
  
	public void setShowInNodes(boolean showInNodes)
	{
		this.showInNodes = showInNodes;
		invalidateImage();
	}
  
	public boolean getShowInNodes()
	{	
		return showInNodes;
	}
	
	public void setSelfLoops(boolean selfLoops)
	{
		this.selfLoops = selfLoops;
	}
	
	public boolean getSelfLoops()
	{
		return selfLoops;
	}
  
	public double getLineThickness()
	{
		return this.maxThickness;
	}
  
	public void setLineThickness(double maxThickness)
	{
		this.maxThickness = maxThickness;
		invalidateImage();
	}
  
	public double getOuterArrowAngle()
	{
		return this.outerArrowAngle * 180.0 / Math.PI;
	}
  
	public void setOuterArrowAngle(double angle)
	{
		this.outerArrowAngle = angle * Math.PI / 180.0;
		invalidateImage();
	}
  
	public double getInnerArrowAngle()
	{
		return this.innerArrowAngle * 180.0 / Math.PI;
	}
  
	public void setInnerArrowAngle(double angle)
	{
		this.innerArrowAngle = angle * Math.PI / 180.0;
		invalidateImage();
	}
  
	public double getArrowheadLength()
	{
		return this.arrowHeadLength;
	}
  
	public void setArrowheadLength(double length)
	{
		this.arrowHeadLength = length;
		invalidateImage();
	}
  
	protected synchronized void initNodeBounds()
	{
		nodeBounds = new Rectangle2D[nData.getSize()];
		for(int ni=0; ni<nodeBounds.length; ni++)
		{
			nodeBounds[ni] = new Rectangle2D.Double(0, 0, 0, 0);
		}
		mouseInNodeFrame = new DPoint(-100, -100);
	}
  
	public void setVisible(boolean visibility)
	{
		super.setVisible(visibility);
	}
  
	// a 'fast' version of paintComponent()
	public void paintComponent(Graphics _g)
	{
		// a bit presumptive
		Graphics2D g = (Graphics2D)_g;
    
		// store away shadow graphics for later use by updateImage()...
		shadowGraphics = g;
    
		// is image ready, if not, update
		if(showImage == null)
		{
			invalidateImage();
		}
    
		// image might still not be ready, if so...blank...
		if(showImage == null)
		{
			// create new image
			Rectangle2D bbox = getBoundingBox(g).getBounds();
			
			// apparently it seems that this bbox can still be invalid.  not sure why...
			if(Double.isNaN(bbox.getWidth()) || Double.isNaN(bbox.getHeight()) ||
					Double.isNaN(bbox.getX()) || Double.isNaN(bbox.getY()))
			{
				return; // ?
			}
			
			int bWidth = (int)bbox.getWidth();
			int bHeight = (int)bbox.getHeight();
			
			// apparently this can produce a zero area graphic?
			if(bWidth * bHeight == 0)
			{
				return; // ?
			}
			
			siX = (int)bbox.getX();
			siY = (int)bbox.getY();
			showImage = createImage(bWidth, bHeight);
			Graphics sg = showImage.getGraphics();
			sg.setColor(Color.white);
			sg.fillRect(0, 0, bWidth, bHeight);
			sg.setColor(Color.gray);
			FontMetrics fm = sg.getFontMetrics();
			sg.drawString("drawing...", 0, fm.getAscent());
		}
    
		// draw image into viewer
		Dimension sz = getSize();
		g.setColor(Color.white);
		g.fillRect(0, 0, sz.width, sz.height);
    
		boolean stillDrawing = (showImage != workingImage);
		g.drawImage(showImage, siX, siY, this);
    
		// maybe should note somewhere that we're repainting if we are?
		if(stillDrawing)
		{
			FontMetrics fm = g.getFontMetrics();
			String note = "[repainting]";
			int noteWidth = fm.stringWidth(note);
			g.setColor(Color.darkGray);
			g.drawString(note, (sz.width-noteWidth)/2+2, (sz.height-fm.getAscent())/2+3);
			g.setColor(Color.red.brighter());
			g.drawString(note, (sz.width-noteWidth)/2, (sz.height-fm.getAscent())/2);
		}
	}
  
	protected void invalidateImage()
	{
		imageValid = false;
	}
	
	// i think this is thread-safe, but i'm not sure...
	protected void _updateImage()
	{
		// this is supposed to take computation-intensive drawing out of
		// the drawing loop
    
		// if we don't have shadow graphics we can't proceed
		if(shadowGraphics == null)
		{
			return;
		}

		// do we have a working image or is it otherwise invalid?
		Rectangle2D bbox = getBoundingBox(shadowGraphics);
		
		// check bbox
		if(Double.isNaN(bbox.getWidth()) || Double.isNaN(bbox.getHeight()) ||
				Double.isNaN(bbox.getX()) || Double.isNaN(bbox.getY()))
		{
			return;
		}
		
		// System.out.println("got bounds");
		boolean wiValid = true;
		if(workingImage == null)
		{
			wiValid = false;
		}
		else
		{
			// check bounds
			int wiWidth = workingImage.getWidth(this);
			int wiHeight = workingImage.getHeight(this);
      
			if((wiWidth != (int)bbox.getWidth()) ||
					(wiHeight != (int)bbox.getHeight()) ||
					(siX != (int)bbox.getX()) ||
					(siY != (int)bbox.getY()))
			{
				wiValid = false;
			}
		}
    
		// System.out.println("Working image is" + (wiValid?"":" not") + " valid");
		if(!wiValid)
		{
			workingImage = createImage((int)bbox.getWidth(), (int)bbox.getHeight());
			siX = (int)bbox.getX();
			siY = (int)bbox.getY();
		}
    
		// use this working image to paint on
		Graphics2D wiG = (Graphics2D)workingImage.getGraphics();
		// System.out.println("wiG = " + wiG);
    
		// paint background
		wiG.setColor(Color.white);
		wiG.fillRect(0, 0, (int)bbox.getWidth(), (int)bbox.getHeight());
    
		wiG.translate(-siX, -siY);
		// AWTGraphicsHandler agh = new AWTGraphicsHandler(wiG);
		// drawComponent(agh, true);
		drawComponent(wiG, true);
		wiG.translate(siX, siY);
    
		// copy over to showImage
		showImage = workingImage;

		repaint();
	}
	
	public void paintComponent2(Graphics2D g)
	{
		// AWTGraphicsHandler gh = new AWTGraphicsHandler(g);
		// drawComponent(gh, true);
		drawComponent(g, true);
	}
  
	public void drawComponent(java.awt.Graphics2D g, boolean useColor)
	{
		Dimension sz = getSize();
		
		// this could probably be an instance variable for efficiency
		float[] hsbVals = new float[3];

		// fill out bounds if need be
		recomputeNodeBounds(g);
    
		// a few time savers
		int numPts = nData.getSize();
		FontMetrics fm = g.getFontMetrics();
		FontRenderContext frc = g.getFontRenderContext();
    
		// draw background if using color
		if(useColor)
		{
			g.setColor(Color.white);
			DRectangle bounds = new DRectangle(0, 0, sz.width, sz.height);
			g.fill(bounds);
		}
    
		// get maximum tie strength
		maxTieStrength = getMaxTieStrength();
    
		// setup darkness sorter
		ShapeSorter shapeSorter = getShapeSorter();
    
		// draw lines
		for(int i1=0; i1<numPts; i1++)
		{
			if(selfLoops)
			{
				ColoredShape loopShape = getLoopShape(i1, useColor);
				
				// add lines to sorter
				if(loopShape != null)
				{
					shapeSorter.addShape(loopShape);
				}
			}
			for(int i2=i1+1; i2<numPts; i2++)
			{
				// do these rectangles overlap?  if so, skip
				if(!nodeBounds[i1].intersects(nodeBounds[i2]))
				{
					// a ha!  this reduces the number of polygons that
					// will be drawn.  
					//
					// Think this will be okay now that we're using shape sorters
					//
					// OLD TEXT: as such, we can't use sortedLines[di].length
					// but rather must use polyCt[di] to know how many polys
					// to draw below...
					ColoredShape lineShape = getLineShape(i1, i2, useColor);
					if(lineShape != null)
					{
						shapeSorter.addShape(lineShape);
					}
				}
			}
		}
    
		// actually draw lines out of sorter
		Iterator iterator = shapeSorter.iterator();
		while(iterator.hasNext())
		{
			ColoredShape cs = (ColoredShape)iterator.next();
			cs.paint(g);
		}
    
		// draw nodes...
		for(int ni=0; ni<numPts; ni++)
		{
			if(useColor)
			{
				Color nodeColor = colorModel.getColor(ni);
				g.setColor(nodeColor);
			}
			else
			{
				// should use white if this is a black & white image?
				g.setColor(Color.white);
			}
			g.fill(nodeBounds[ni]);

			if(useColor)
			{
				if(ni == activeNodeIdx)
				{
					g.setColor(activeNodeColor);
				}
				
				else if(ni == activeTargetIdx)
				{
					g.setColor(activeTargetColor);
				}
	      
				else if(activeNodeIdx != -1)
				{
					if(((nData.getTieStrength(currentRelation, activeNodeIdx, ni) > 0.0) && 
							showOutNodes) ||
							((nData.getTieStrength(currentRelation, ni, activeNodeIdx) > 0.0) &&
									showInNodes))
					{
						g.setColor(neighborhoodNodeColor);
					}
					else
					{
						g.setColor(getForeground());
					}
				}
	      		else
	      		{
	    	  		g.setColor(getForeground());
	      		}
	    	}
	    	else
	    	{
	    		// probably should use black?
	    		g.setColor(Color.black);
	    	}
      
			if(showLabels)
			{
				String label = nData.getLabel(ni);
				AttributedString nodeName = new AttributedString(label);
				nodeName.addAttribute(TextAttribute.FONT, labelFont);
				AttributedCharacterIterator aci = nodeName.getIterator();
				LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);
				double baseX = nodeBounds[ni].getX();
				double boundsWidth = nodeBounds[ni].getWidth();
				double penY = nodeBounds[ni].getY() + BORDER;
				
				while(lbm.getPosition() < label.length())
				{
					TextLayout nextLayout = lbm.nextLayout(WRAP_WIDTH);
					penY += nextLayout.getAscent();
					double penX = baseX + (boundsWidth / 2.0) +
						(nextLayout.getAdvance() / 2.0) *
						(nextLayout.isLeftToRight() ? -1.0 : 1.0);
					nextLayout.draw(g, (float)penX, (float)penY);
					penY += nextLayout.getDescent() + nextLayout.getLeading();
				}
			}
			g.draw(nodeBounds[ni]);
    	}
	}
	
	protected Color getDefaultColor()
	{
		return Color.black;
	}
  
	protected Color getLineColor(int rIdx, int i1, int i2, boolean useColor)
	{
		double tieOut = nData.getTieStrength(currentRelation, i1, i2) / maxTieStrength;
		double tieIn  = nData.getTieStrength(currentRelation, i2, i1) / maxTieStrength;

		Color lineColor = getDefaultColor();

		// infinity checks
		boolean infiniteVal = false;
		if(Double.isInfinite(tieOut))
		{
			tieOut = (tieOut > 0.0)?1.0:-1.0;
			infiniteVal = true;
		}
		if(Double.isInfinite(tieIn))
		{
			tieIn = (tieIn > 0.0)?1.0:-1.0;
			infiniteVal = true;
		}

		if(useColor)
		{
			if(infiniteVal)
			{
				lineColor = Color.green;
			}
			else if((tieOut < 0.0) || (tieIn < 0.0))
			{
				lineColor = Color.red;
			}
			else if(((i1 == activeTargetIdx) && (i2 == activeNodeIdx)) ||
					((i1 == activeNodeIdx) && (i2 == activeTargetIdx))) {
				lineColor = Color.blue;
			}
			if(showInNodes)
			{
				if(((i1 == activeNodeIdx) && (tieIn != 0.0)) ||
						((i2 == activeNodeIdx) && (tieOut != 0.0)))
				{
					lineColor = neighborhoodNodeColor;
				}
			}
			if(showOutNodes)
			{
				if(((i1 == activeNodeIdx) && (tieOut != 0.0)) ||
						((i2 == activeNodeIdx) && (tieIn != 0.0)))
				{
					lineColor = neighborhoodNodeColor;
				}
			}
		}
		
		if(lineStyle == BRIGHTNESS)
		{
			float intensity = (float)Math.max(Math.abs(tieIn), Math.abs(tieOut));
			if(intensity != 0.0)
			{
				Color baseColor = lineColor;
				float delRed   = ((float)(255 - baseColor.getRed())) / 255.0f;
				float delGreen = ((float)(255 - baseColor.getGreen())) / 255.0f;
				float delBlue  = ((float)(255 - baseColor.getBlue())) / 255.0f;
				Color newColor = new Color(1.0f - intensity * delRed,
						1.0f - intensity * delGreen,
						1.0f - intensity * delBlue);
				lineColor = newColor;
			}
		}

		return lineColor;
	}
	
	protected ColoredShape getLoopShape(int nodeIdx, boolean useColor)
	{
		double loopstr = nData.getTieStrength(currentRelation, nodeIdx, nodeIdx)/maxTieStrength;
		
		// infinity check
		if(Double.isInfinite(loopstr))
		{
			loopstr = (loopstr > 0.0)?1.0:-1.0;
		}
		
		// set shape & color
		Shape loopShape = null;
		Color loopColor = getLineColor(currentRelation, nodeIdx, nodeIdx, useColor);

		// basics (should some of these be instance vars?)
		double startX = nodeBounds[nodeIdx].getCenterX();
		double startY = nodeBounds[nodeIdx].getMinY();
		double endX = nodeBounds[nodeIdx].getMaxX();
		double endY = nodeBounds[nodeIdx].getCenterY();
		
		double loopCenterX = (startX + endX) / 2.0;
		double loopCenterY = (startY + endY) / 2.0;
		
		// could be fixed?
		// double targetAngle = Math.PI/4.0;
		double targetAngle = Math.atan2(nodeBounds[nodeIdx].getHeight(), nodeBounds[nodeIdx].getWidth());
		double ctlAngle = targetAngle + Math.PI/2.0;
		double midCtlX = controlLength * Math.cos(ctlAngle);
		double midCtlY = -controlLength * Math.sin(ctlAngle);
		
		// used to be an else if...think that's wrong
		if(lineStyle == BRIGHTNESS)
		{
			if(loopstr != 0.0)
			{
				double targetX = loopCenterX + loopRadius * Math.cos(targetAngle);
				double targetY = loopCenterY - loopRadius * Math.sin(targetAngle);

				GeneralPath loopPath = new GeneralPath();
				loopPath.moveTo((float)startX, (float)startY);
				loopPath.curveTo((float)(startX), (float)(startY-controlLength),
						(float)(targetX+midCtlX), (float)(targetY+midCtlY),
						(float)(targetX), (float)(targetY));
				loopPath.curveTo((float)(targetX-midCtlX), (float)(targetY-midCtlY),
						(float)(endX+controlLength), (float)(endY),
						(float)(endX), (float)(endY));
				loopShape = loopPath;
			}
		}
		else if(lineStyle == THICKNESS)
		{
			if(loopstr > 0.0)
			{
				double ctlDelC = controlLength * Math.cos(openAngle*loopstr);
				double ctlDelS = controlLength * Math.sin(openAngle*loopstr);
				double widthControl = loopstr * maxThickness; // vary this?
				double targetInX = loopCenterX + (loopRadius-widthControl) * Math.cos(targetAngle);
				double targetInY = loopCenterY - (loopRadius-widthControl) * Math.sin(targetAngle);
				double targetOutX = loopCenterX + (loopRadius+widthControl) * Math.cos(targetAngle);
				double targetOutY = loopCenterY - (loopRadius+widthControl) * Math.sin(targetAngle);

				GeneralPath loopPath = new GeneralPath();
				loopPath.moveTo((float)startX, (float)startY);
				loopPath.curveTo((float)(startX-ctlDelS), (float)(startY-ctlDelC),
						(float)(targetOutX+midCtlX), (float)(targetOutY+midCtlY),
						(float)(targetOutX), (float)(targetOutY));
				loopPath.curveTo((float)(targetOutX-midCtlX), (float)(targetOutY-midCtlY),
						(float)(endX+ctlDelC), (float)(endY+ctlDelS),
						(float)(endX), (float)(endY));
				loopPath.curveTo((float)(endX+ctlDelC), (float)(endY-ctlDelS),
						(float)(targetInX-midCtlX), (float)(targetInY-midCtlY),
						(float)(targetInX), (float)(targetInY));
				loopPath.curveTo((float)(targetInX+midCtlX), (float)(targetInY+midCtlY),
						(float)(startX+ctlDelS), (float)(startY-ctlDelC),
						(float)(startX), (float)(startY));
				loopPath.closePath();
				loopShape = loopPath;
			}
		}

		return new ColoredShape(loopShape, loopColor, loopstr, ColoredShape.FILL);
	}
  
	protected ColoredShape getLineShape(int i1, int i2, boolean useColor)
	{
		double tieOut = nData.getTieStrength(currentRelation, i1, i2) / maxTieStrength;
		double tieIn  = nData.getTieStrength(currentRelation, i2, i1) / maxTieStrength;
		
		Color lineColor = getLineColor(currentRelation, i1, i2, useColor);
		double depth = Math.max(Math.abs(tieOut), Math.abs(tieIn));
	
		if(lineStyle == BRIGHTNESS)
		{
			if((tieIn != 0.0) || (tieOut != 0.0))
			{
				Shape shape = getThinLineShape(currentRelation, i1, i2);
				return new ColoredShape(shape, lineColor, depth, ColoredShape.DRAW);
			}
			else
			{
				return null;
			}
		}
		else if(lineStyle == THICKNESS)
		{
			if((tieIn > 0.0) || (tieOut > 0.0))
			{
				Shape shape = getThickLineShape(currentRelation, i1, i2, tieIn, tieOut, showArrowheads);
				return new ColoredShape(shape, lineColor, depth, ColoredShape.FILL);
			}
			else
			{
				return null;
			}
		}

		return null;
	}
	
	protected Shape getThinLineShape(int ridx, int i1, int i2)
	{
		// find intersection points with boxes
		double node1X = nData.getXPos(i1);
		double node1Y = nData.getYPos(i1);
		double node2X = nData.getXPos(i2);
		double node2Y = nData.getYPos(i2);
		double delX = node2X - node1X;
		double delY = node2Y - node1Y;
		double lineTheta = Math.atan2(delY, delX);
		DPoint pStart = getIntersection(i1, lineTheta);
		DPoint pEnd   = getIntersection(i2, lineTheta + Math.PI);
	
		Shape lineShape = null;
		if(showArrowheads)
		{
			boolean arrow1 = nData.getTieStrength(ridx, i1, i2) > 0.0;
			boolean arrow2 = nData.getTieStrength(ridx, i2, i1) > 0.0;
			if(!arrow1)
			{
				lineShape = generateThinLine(node1X, node1Y, pEnd.x, pEnd.y,
						false, true, outerArrowAngle, arrowHeadLength);
			}
			else if(!arrow2)
			{
				lineShape = generateThinLine(pStart.x, pStart.y, node2X, node2Y,
						true, false, outerArrowAngle, arrowHeadLength);
			}
			else
			{
				lineShape = generateThinLine(pStart.x, pStart.y, pEnd.x, pEnd.y,
						true, true, outerArrowAngle, arrowHeadLength);
			}
		}
		else
		{
			lineShape = new Line2D.Double(node1X, node1Y, node2X, node2Y);
		}
		return lineShape;
	}
	
	protected Shape getThickLineShape(int ridx, int i1, int i2, double w1, double w2, boolean arrowheads)
	{
		// find intersection points with boxes
		double node1X = nData.getXPos(i1);
		double node1Y = nData.getYPos(i1);
		double node2X = nData.getXPos(i2);
		double node2Y = nData.getYPos(i2);
		double delX = node2X - node1X;
		double delY = node2Y - node1Y;
		double lineTheta = Math.atan2(delY, delX);
		DPoint pStart = getIntersection(i1, lineTheta);
		DPoint pEnd   = getIntersection(i2, lineTheta + Math.PI);
			
		Shape lineShape = null;
		if(arrowheads)
		{
			lineShape = generateThickLine(pStart.x, pStart.y, pEnd.x, pEnd.y,
					w1 * maxThickness, w2 * maxThickness,
					(w1 > 0.0), (w2 > 0.0),
					innerArrowAngle, outerArrowAngle, arrowHeadLength);
		}
		else
		{
			lineShape = generateThickLine(nData.getXPos(i1), nData.getYPos(i1),
					nData.getXPos(i2), nData.getYPos(i2),
					w1 * maxThickness, w2 * maxThickness,
					false, false,
					innerArrowAngle, outerArrowAngle, arrowHeadLength);
		}
		return lineShape;
	}
	
	protected ShapeSorter getShapeSorter()
	{
		return new DefaultShapeSorter();
	}
	
	public interface ShapeSorter
	{
		public void addShape(ColoredShape cs);
		public Iterator iterator();
	}
	
	public class DefaultShapeSorter implements ShapeSorter
	{
		// NOTE: This used to be in the code that did this...
		// see above for why we go up to polyCt[di] rather than sortedLines[di].length

		protected final static int SORT_GRANULARITY = 10;
		protected Vector[] sortedLines;
		
		public DefaultShapeSorter()
		{
			this.sortedLines = new Vector[SORT_GRANULARITY];
			for(int i=0; i<sortedLines.length; i++)
			{
				sortedLines[i] = new Vector();
			}
		}

		public void addShape(ColoredShape cs)
		{
			if(cs == null) return;
			int dkIdx = (int)(cs.getDepth() * SORT_GRANULARITY);
			if(dkIdx > 0)
			{
				if(dkIdx > SORT_GRANULARITY)
				{
					dkIdx = SORT_GRANULARITY;
				}
				dkIdx--;
			
				sortedLines[dkIdx].add(cs);
			}
		}

		// there is probably a faster & cleverer way to do this, but eh...
		public Iterator iterator()
		{
			Vector v = sortedLines[0];
			for(int i=1; i<sortedLines.length; i++)
			{
				v.addAll(sortedLines[i]);
			}
			return v.iterator();
		}
	}
	
	protected double getMaxTieStrength()
	{
		int numPts = nData.getSize();

		// calculate max tie strength
		double tmpMax = 0.0;
		for(int i1=0; i1<numPts; i1++)
		{
			if(selfLoops)
			{
				double selfTie = Math.abs(nData.getTieStrength(currentRelation, i1, i1));
				if(Double.isInfinite(selfTie)) selfTie = 0.0;
				if(selfTie > tmpMax)
				{
					tmpMax = selfTie;
				}
			}
			for(int i2=i1+1; i2<numPts; i2++)
			{
				// ignore infinite values in this step
				double to = Math.abs(nData.getTieStrength(currentRelation, i1, i2));
				double ti = Math.abs(nData.getTieStrength(currentRelation, i2, i1));
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
	
  protected DPoint getIntersection(int nodeIdx, double lineTheta)
  {
  	// minor adjustment to ward off NaN demons
  	if(lineTheta == 0.0) lineTheta = 0.000000001;

  	// find center point
  	double cX = nData.getXPos(nodeIdx);
  	double cY = nData.getYPos(nodeIdx);
  	
  	// get inclination for line
  	double lineSin = Math.sin(lineTheta);
  	
  	// get inclination for bounds
  	double boxTheta = Math.atan2(nodeBounds[nodeIdx].getHeight(), 
			nodeBounds[nodeIdx].getWidth());
  	double boxSin = Math.sin(boxTheta);
  	double distanceToEdge;
  	if(Math.abs(lineSin) > Math.abs(boxSin)) // line "steeper" than box
  	{
  		distanceToEdge = Math.abs((nodeBounds[nodeIdx].getHeight()/2.0) / Math.sin(lineTheta));
  	}
  	else
  	{
  		distanceToEdge = Math.abs((nodeBounds[nodeIdx].getWidth()/2.0) / Math.cos(lineTheta));
  	}
  	return new DPoint(cX + distanceToEdge * Math.cos(lineTheta), 
  	  cY + distanceToEdge * Math.sin(lineTheta));
  }
  
  	public JComponent getBlockPalette()
  	{
  		return new BlockPalette();
  	}
  
  	protected void recomputeNodeBounds(Graphics2D g)
  	{
  		// let's try to do this with some fixed wrapping

  		int numPts = nData.getSize();
  		float tLeading = (float)0.0;

		FontRenderContext frc = g.getFontRenderContext();
		for(int ni=0; ni<numPts; ni++)
		{
			if(showLabels)
			{
				String label = nData.getLabel(ni);
				AttributedString attLabel = new AttributedString(label);
				attLabel.addAttribute(TextAttribute.FONT, labelFont);
				AttributedCharacterIterator aci = attLabel.getIterator();
				LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);
				double maxWidth = 0.0;
				double height = 0.0; // initial height
				while(lbm.getPosition() < label.length())
				{
					TextLayout nextLayout = lbm.nextLayout(WRAP_WIDTH);
					maxWidth = Math.max(maxWidth, nextLayout.getAdvance()); // wrongo!
					height += nextLayout.getAscent() + nextLayout.getDescent();
					tLeading = nextLayout.getLeading();
					height += tLeading;
				}
				height -= tLeading; // clip last leading

				// add some borders
				height += 2.0 * BORDER;
				maxWidth += 3.0 * BORDER;
				
				nodeBounds[ni].setRect(nData.getXPos(ni) - maxWidth/2.0,
						nData.getYPos(ni) - height/2.0, maxWidth, height);
			}
			else
			{
				nodeBounds[ni].setRect(nData.getXPos(ni) - bubbleSize/2.0,
						nData.getYPos(ni) - bubbleSize/2.0, bubbleSize, bubbleSize);
			}
		}
  	}
	
  	protected Shape generateThickLine(double x1, double y1,
  			double x2, double y2,
  			double thickness1, double thickness2,
  			boolean arrow1, boolean arrow2,
  			double innerArrowAngle, double outerArrowAngle, double arrowHeadLength)
  	{
		GeneralPath gp = new GeneralPath();
		double delX = x2-x1;
		double delY = y2-y1;
		double lineAngle = Math.atan2(delY, delX);
		double dx = Math.sin(lineAngle);
		double dy = Math.cos(lineAngle);
		double lineDist = Math.sqrt(delX * delX + delY * delY);
		double lineSlope = (thickness2 - thickness1)/lineDist; 
		double elbowSlope = Math.tan(outerArrowAngle + innerArrowAngle);

		// in all cases we need to know where the "no arrowhead" reference points would be
		double x1L_no = x1 - (dx * thickness1 / 2.0);
		double x1R_no = x1 + (dx * thickness1 / 2.0);
		double x2L_no = x2 - (dx * thickness2 / 2.0);
		double x2R_no = x2 + (dx * thickness2 / 2.0);
		double y1L_no = y1 + (dy * thickness1 / 2.0);
		double y1R_no = y1 - (dy * thickness1 / 2.0);
		double y2L_no = y2 + (dy * thickness2 / 2.0);
		double y2R_no = y2 - (dy * thickness2 / 2.0);

		if(arrow1 || arrow2)
		{
			// need to place "barb" and "elbow" of arrow tips
			// the idea is that the barb will be thickness/2 * arrowHeadLength long
			// an implication of this is that arrowHeadLength must be > 1, or weird things
			// will happen.
			double barbLength1 = thickness1/2.0 * arrowHeadLength;
			double barbNormal1 = barbLength1 * Math.sin(outerArrowAngle);
			double barbTangent1 = barbLength1 * Math.cos(outerArrowAngle);
			double elbowTangent1 = (barbNormal1 - elbowSlope * barbTangent1 - 
					thickness1 / 2.0) / (lineSlope - elbowSlope);
			double elbowNormal1 = lineSlope * elbowTangent1 + thickness1 / 2.0;
			double elbowAngle1 = Math.atan2(elbowNormal1, elbowTangent1);
			double elbowLength1 = Math.sqrt(elbowNormal1*elbowNormal1 + elbowTangent1*elbowTangent1);
	
			// plug away...
			double x1AL = x1 + Math.cos(lineAngle + outerArrowAngle) * barbLength1;
			double y1AL = y1 + Math.sin(lineAngle + outerArrowAngle) * barbLength1;
			double x1AR = x1 + Math.cos(lineAngle - outerArrowAngle) * barbLength1;
			double y1AR = y1 + Math.sin(lineAngle - outerArrowAngle) * barbLength1;
	
			double x1L = x1 + Math.cos(lineAngle + elbowAngle1) * elbowLength1;
			double y1L = y1 + Math.sin(lineAngle + elbowAngle1) * elbowLength1;
			double x1R = x1 + Math.cos(lineAngle - elbowAngle1) * elbowLength1;
			double y1R = y1 + Math.sin(lineAngle - elbowAngle1) * elbowLength1;
	
			// do the same for the other end (lineSlope has to be inverted)
			double barbLength2 = thickness2/2.0 * arrowHeadLength;
			double barbNormal2 = barbLength2 * Math.sin(outerArrowAngle);
			double barbTangent2 = barbLength2 * Math.cos(outerArrowAngle);
			double elbowTangent2 = (barbNormal2 - elbowSlope * barbTangent2 - 
					thickness2 / 2.0) / (-lineSlope - elbowSlope);
			double elbowNormal2 = -lineSlope * elbowTangent2 + thickness2 / 2.0;
			double elbowAngle2 = Math.atan2(elbowNormal2, elbowTangent2);
			double elbowLength2 = Math.sqrt(elbowNormal2*elbowNormal2 + elbowTangent2*elbowTangent2);

			// plug away...
			double x2AL = x2 - Math.cos(lineAngle - outerArrowAngle) * barbLength2;
			double y2AL = y2 - Math.sin(lineAngle - outerArrowAngle) * barbLength2;
			double x2AR = x2 - Math.cos(lineAngle + outerArrowAngle) * barbLength2;
			double y2AR = y2 - Math.sin(lineAngle + outerArrowAngle) * barbLength2;
			
			double x2L = x2 - Math.cos(lineAngle - elbowAngle2) * elbowLength2;
			double y2L = y2 - Math.sin(lineAngle - elbowAngle2) * elbowLength2;
			double x2R = x2 - Math.cos(lineAngle + elbowAngle2) * elbowLength2;
			double y2R = y2 - Math.sin(lineAngle + elbowAngle2) * elbowLength2;
	
			if(!arrow1)
	    {
				gp.moveTo((float)x1L_no, (float)y1L_no);
				gp.lineTo((float)x2L, (float)y2L);
				gp.lineTo((float)x2AL, (float)y2AL);
				gp.lineTo((float)x2, (float)y2);
				gp.lineTo((float)x2AR, (float)y2AR);
				gp.lineTo((float)x2R, (float)y2R);
				gp.lineTo((float)x1R_no, (float)y1R_no);
				gp.closePath();
	    }
			else if(!arrow2)
	    {
				gp.moveTo((float)x1, (float)y1);
				gp.lineTo((float)x1AL, (float)y1AL);
				gp.lineTo((float)x1L, (float)y1L);
				gp.lineTo((float)x2L_no, (float)y2L_no);
				gp.lineTo((float)x2R_no, (float)y2R_no);
				gp.lineTo((float)x1R, (float)y1R);
				gp.lineTo((float)x1AR, (float)y1AR);
				gp.closePath();
	    }
			else
			{
				gp.moveTo((float)x1, (float)y1);
				gp.lineTo((float)x1AL, (float)y1AL);
				gp.lineTo((float)x1L, (float)y1L);
				gp.lineTo((float)x2L, (float)y2L);
				gp.lineTo((float)x2AL, (float)y2AL);
				gp.lineTo((float)x2, (float)y2);
				gp.lineTo((float)x2AR, (float)y2AR);
				gp.lineTo((float)x2R, (float)y2R);
				gp.lineTo((float)x1R, (float)y1R);
				gp.lineTo((float)x1AR, (float)y1AR);
				gp.closePath();
			}
		}
		else
		{
			gp.moveTo((float)x1L_no, (float)y1L_no);
			gp.lineTo((float)x1R_no, (float)y1R_no);
			gp.lineTo((float)x2R_no, (float)y2R_no);
			gp.lineTo((float)x2L_no, (float)y2L_no);
			gp.closePath();
		}
		return gp;
  	}
  	
	protected Shape generateThickLineOld(double x1, double y1,
		double x2, double y2,
		double thickness1, double thickness2,
		boolean arrow1, boolean arrow2,
		double innerArrowAngle, double outerArrowAngle, double arrowHeadLength)
	{
		GeneralPath gp = new GeneralPath();
		double delX = x2-x1;
		double delY = y2-y1;
		double lineAngle = Math.atan2(delY, delX);
		double dx = Math.sin(lineAngle);
		double dy = Math.cos(lineAngle);

		if(arrow1 || arrow2)
		{
			// for no arrowhead caps...
			double x1L_no = x1 - (dx * thickness1 / 2.0);
			double x1R_no = x1 + (dx * thickness1 / 2.0);
			double x2L_no = x2 - (dx * thickness2 / 2.0);
			double x2R_no = x2 + (dx * thickness2 / 2.0);
			double y1L_no = y1 + (dy * thickness1 / 2.0);
			double y1R_no = y1 - (dy * thickness1 / 2.0);
			double y2L_no = y2 + (dy * thickness2 / 2.0);
			double y2R_no = y2 - (dy * thickness2 / 2.0);
	
			// figure out how far out arrow tip should go
			double arrowTipNormal1 = (thickness1/2.0) + Math.sin(innerArrowAngle) *
				(arrowHeadLength + thickness1/2.0); // heuristic...
			double arrowTipInset1 = arrowTipNormal1 / Math.tan(outerArrowAngle);
			double arrowHeadLength1 = Math.sqrt(arrowTipNormal1 * arrowTipNormal1 +
				arrowTipInset1 * arrowTipInset1);
	
			// figure out how far out inside joint should be
			double arrowJointInset1 = arrowTipInset1 - arrowHeadLength * 
				Math.cos(innerArrowAngle);
			double arrowJointLength1 = Math.sqrt(arrowJointInset1 * arrowJointInset1 +
				thickness1 * thickness1 / 4.0);
			double jointAngle1 = Math.acos(arrowJointInset1/arrowJointLength1);
	
			// plug away...
			double x1AL = x1 + Math.cos(lineAngle + outerArrowAngle) * arrowHeadLength1;
			double y1AL = y1 + Math.sin(lineAngle + outerArrowAngle) * arrowHeadLength1;
			double x1AR = x1 + Math.cos(lineAngle - outerArrowAngle) * arrowHeadLength1;
			double y1AR = y1 + Math.sin(lineAngle - outerArrowAngle) * arrowHeadLength1;
	
			double x1L = x1 + Math.cos(lineAngle + jointAngle1) * arrowJointLength1;
			double y1L = y1 + Math.sin(lineAngle + jointAngle1) * arrowJointLength1;
			double x1R = x1 + Math.cos(lineAngle - jointAngle1) * arrowJointLength1;
			double y1R = y1 + Math.sin(lineAngle - jointAngle1) * arrowJointLength1;
	
			// figure out how far out arrow tip should go
			double arrowTipNormal2 = (thickness2/2.0) + Math.sin(innerArrowAngle) *
				(arrowHeadLength + thickness2/2.0); // heuristic...
			double arrowTipInset2 = arrowTipNormal2 / Math.tan(outerArrowAngle);
			double arrowHeadLength2 = Math.sqrt(arrowTipNormal2 * arrowTipNormal2 +
				arrowTipInset2 * arrowTipInset2);
	
			// figure out how far out inside joint should be
			double arrowJointInset2 = arrowTipInset2 - arrowHeadLength * 
				Math.cos(innerArrowAngle);
			double arrowJointLength2 = Math.sqrt(arrowJointInset2 * arrowJointInset2 +
				thickness2 * thickness2 / 4.0);
			double jointAngle2 = Math.acos(arrowJointInset2/arrowJointLength2);
	
			// plug away...
			double x2AL = x2 - Math.cos(lineAngle - outerArrowAngle) * arrowHeadLength2;
			double y2AL = y2 - Math.sin(lineAngle - outerArrowAngle) * arrowHeadLength2;
			double x2AR = x2 - Math.cos(lineAngle + outerArrowAngle) * arrowHeadLength2;
			double y2AR = y2 - Math.sin(lineAngle + outerArrowAngle) * arrowHeadLength2;
			
			double x2L = x2 - Math.cos(lineAngle - jointAngle2) * arrowJointLength2;
			double y2L = y2 - Math.sin(lineAngle - jointAngle2) * arrowJointLength2;
			double x2R = x2 - Math.cos(lineAngle + jointAngle2) * arrowJointLength2;
			double y2R = y2 - Math.sin(lineAngle + jointAngle2) * arrowJointLength2;
	
			if(!arrow1)
	    {
				gp.moveTo((float)x1L_no, (float)y1L_no);
				gp.lineTo((float)x2L, (float)y2L);
				gp.lineTo((float)x2AL, (float)y2AL);
				gp.lineTo((float)x2, (float)y2);
				gp.lineTo((float)x2AR, (float)y2AR);
				gp.lineTo((float)x2R, (float)y2R);
				gp.lineTo((float)x1R_no, (float)y1R_no);
				gp.closePath();
	    }
			else if(!arrow2)
	    {
				gp.moveTo((float)x1, (float)y1);
				gp.lineTo((float)x1AL, (float)y1AL);
				gp.lineTo((float)x1L, (float)y1L);
				gp.lineTo((float)x2L_no, (float)y2L_no);
				gp.lineTo((float)x2R_no, (float)y2R_no);
				gp.lineTo((float)x1R, (float)y1R);
				gp.lineTo((float)x1AR, (float)y1AR);
				gp.closePath();
	    }
			else
			{
				gp.moveTo((float)x1, (float)y1);
				gp.lineTo((float)x1AL, (float)y1AL);
				gp.lineTo((float)x1L, (float)y1L);
				gp.lineTo((float)x2L, (float)y2L);
				gp.lineTo((float)x2AL, (float)y2AL);
				gp.lineTo((float)x2, (float)y2);
				gp.lineTo((float)x2AR, (float)y2AR);
				gp.lineTo((float)x2R, (float)y2R);
				gp.lineTo((float)x1R, (float)y1R);
				gp.lineTo((float)x1AR, (float)y1AR);
				gp.closePath();
			}
		}
		else
		{
			double x1L = x1 - (dx * thickness1 / 2.0);
			double x1R = x1 + (dx * thickness1 / 2.0);
			double x2L = x2 - (dx * thickness2 / 2.0);
			double x2R = x2 + (dx * thickness2 / 2.0);
			
			double y1L = y1 + (dy * thickness1 / 2.0);
			double y1R = y1 - (dy * thickness1 / 2.0);
			double y2L = y2 + (dy * thickness2 / 2.0);
			double y2R = y2 - (dy * thickness2 / 2.0);
			
			gp.moveTo((float)x1L, (float)y1L);
			gp.lineTo((float)x1R, (float)y1R);
			gp.lineTo((float)x2R, (float)y2R);
			gp.lineTo((float)x2L, (float)y2L);
			gp.closePath();
		}
		return gp;
	}
	
	protected Shape generateThinLine(double x1, double y1,
		double x2, double y2,
		boolean arrow1, boolean arrow2,
		double arrowAngle, double arrowLength)
	{
		double delX = x2-x1;
		double delY = y2-y1;
		double lineAngle = Math.atan2(delY, delX);
		double dx = Math.sin(lineAngle);
		double dy = Math.cos(lineAngle);

		GeneralPath gp = new GeneralPath();
		gp.moveTo((float)x1, (float)y1);
		gp.lineTo((float)x2, (float)y2);

		if(arrow1)
		{
			double theta1L = Math.PI + lineAngle + arrowAngle;
			double theta1R = Math.PI + lineAngle - arrowAngle;
			gp.moveTo((float)(x1 - Math.cos(theta1L) * arrowLength),
				(float)(y1 - Math.sin(theta1L) * arrowLength));
			gp.lineTo((float)x1, (float)y1);
			gp.lineTo((float)(x1 - Math.cos(theta1R) * arrowLength),
				(float)(y1 - Math.sin(theta1R) * arrowLength));
		}
		
		if(arrow2)
		{
			double theta2L = lineAngle + arrowAngle;
			double theta2R = lineAngle - arrowAngle;
			gp.moveTo((float)(x2 - Math.cos(theta2L) * arrowLength),
				(float)(y2 - Math.sin(theta2L) * arrowLength));
			gp.lineTo((float)x2, (float)y2);
			gp.lineTo((float)(x2 - Math.cos(theta2R) * arrowLength),
				(float)(y2 - Math.sin(theta2R) * arrowLength));
		}
		return gp;
	}

  public Rectangle2D getBoundingBox(Graphics2D g)
  {
  	// recompute node bounds?
  	recomputeNodeBounds(g);
  	
  	Rectangle2D boundingBox = nodeBounds[0];
  	for(int ni=1; ni<nodeBounds.length; ni++)
  	{
			Rectangle2D.union(boundingBox, nodeBounds[ni], boundingBox);
  	}
    
    // used to add a point for 'line thickness'
  	// now generally expanding for possibility of loops
    boundingBox.setRect(boundingBox.getX() - (loopRadius + maxThickness), 
    		boundingBox.getY() - (loopRadius + maxThickness),
			boundingBox.getWidth() + 2.0*(loopRadius + maxThickness),
			boundingBox.getHeight() + 2.0*(loopRadius + maxThickness));

  	return boundingBox;
  }
  
  public void dataChanged_old(DataChangeEvent dce)
  {
    switch(dce.getType())
    {
      case DataChangeEvent.NODE_INSERTED:
      case DataChangeEvent.NODE_DELETED:
      	initNodeBounds();
      	break;
     	
    }
    repaint();
  }
  
  public void dataChanged(DataChangeEvent dce)
  {
    switch(dce.getType())
    {
      case DataChangeEvent.NODE_INSERTED:
      case DataChangeEvent.NODE_DELETED:
      	initNodeBounds();
      	break;
     	
    }
    
    // update image instead of repainting...
		invalidateImage();
  }
  
	public void relationChanged(RelationChangeEvent rce)
	{
		switch(rce.getType())
		{
			case RelationChangeEvent.SELECTED:
				this.currentRelation = rce.getRelationIndex();
				invalidateImage();
				break;
				
			case RelationChangeEvent.CHANGED:
			case RelationChangeEvent.INSERTED:
			case RelationChangeEvent.DELETED:
				// i don't know that anything here needs to be updated
				// for a name change, insertion or deletion (per se, net of
				// a new relation being selected)
				break;
		}
	}
	
	public void setRelation(int relIdx)
	{
		this.currentRelation = relIdx;
		invalidateImage();
	}
	
	public void setRelationName(int relIdx, String name)
	{
		this.nData.getRelation(relIdx).setName(name);
	}
	
  
  public void mouseEntered(MouseEvent me)
  {
  	if(editMode == MOVE)
  	{
  		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  	}
  }
  
  public void mouseExited(MouseEvent me)
  {
  		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }
  
  private Frame getFrame()
  {
  	Component c = this;
  	while(c != null)
  	{
  		Component c2 = c.getParent();
  		if(c2 instanceof Frame)
  		{
  			return (Frame)c2;
  		}
  		c = c2;
  	}
  	return null;
  }
  
  public void mouseClicked(MouseEvent me)
  {
  }
  
  public void mouseReleased(MouseEvent me)
  {
    if(activeNodeIdx == -1) return;
    if(!editable) return;

    nData.setNodeFrozen(activeNodeIdx, false);
    // activeNodeIdx = -1;
    mouseInNodeFrame.x = -1;
    mouseInNodeFrame.y = -1;
    
    // this could all happen in VisualNetworkData.java, but
    // here we save a few events being fired...
    DataChangeEvent dce;
    if(editMode == EDIT)
    {
    	dce = new DataChangeEvent(this,
    			DataChangeEvent.LOCATION_CHANGED,
    			activeNodeIdx, activeNodeIdx);
    }
	  else
	  {
	    dce = new DataChangeEvent(this,
	    	DataChangeEvent.LOCATION_CHANGED,
	    	DataChangeEvent.ALL_ROWS, DataChangeEvent.ALL_COLUMNS);
	  }
    nData.fireDataChanged(dce);
    
		invalidateImage();
  }
  
  	public void mousePressed(MouseEvent me)
  	{
  		Point mouseDownPt = me.getPoint();
    
  		// are we in a node?
  		if(editMode == EDIT)
  		{
  			int newNodeIdx = -1;
			
			// do this in reverse paint order
  			for(int ni=nodeBounds.length-1; ni>=0; ni--)
  			{
  				if(nodeBounds[ni].contains(mouseDownPt))
  				{
  					nData.setNodeFrozen(ni, true);
  					newNodeIdx = ni;
  					mouseInNodeFrame.x = 
						(int)(nodeBounds[ni].getX() + nodeBounds[ni].getWidth()/2.0 - mouseDownPt.x);
  					mouseInNodeFrame.y =
						(int)(nodeBounds[ni].getY() + nodeBounds[ni].getHeight()/2.0 - mouseDownPt.y);
  					break;
  				}
  			}
	    
  			// if we're not on an existing node...
  			if((newNodeIdx == -1) && editable)
  			{
  				// ...and shift is down, add a node
  				if(me.isShiftDown())
  				{
  					if(nodeAdditionAllowed)
  					{
  						nData.addNode();
  	  					newNodeIdx = nData.getSize() - 1;
  	  					nData.setXPos(newNodeIdx, mouseDownPt.x);
  	  					nData.setYPos(newNodeIdx, mouseDownPt.y);
  			      
  	  					// if there's an active node, connect this one to it.
  	  					if(activeNodeIdx != -1)
  	  					{
  	  						nData.setTieStrength(currentRelation, activeNodeIdx, newNodeIdx, 1.0);
  	  					}
  					}
  				}
  			}
  			else if(editable)
  			{
  				// if shift is down, 'invert' connection status
  				if(me.isShiftDown() && (newNodeIdx != -1) && (activeNodeIdx != -1))
  				{
  					invertTie(currentRelation, activeNodeIdx, newNodeIdx);
  				}
		    
  				// no shift + double click = rename
  				if(!me.isShiftDown())
  				{
  					int clickCt = me.getClickCount();
  					if(clickCt == 2)
  					{
  						Object ret = JOptionPane.showInputDialog(this,
  								"Set node name to:", "Rename Node",
  								JOptionPane.PLAIN_MESSAGE, null, null,
  								nData.getLabel(activeNodeIdx));
  						if(ret != null)
  						{
  							nData.setLabel(activeNodeIdx, (String)ret);
  						}
  					}
  				}
  			}
	    
  			// now activate & fire if nec.
  			if(activeNodeIdx != newNodeIdx)
  			{
  				activeNodeIdx = newNodeIdx;
  				if(activeNodeIdx == -1)
  				{
  					fireItemEvent(new ItemEvent(this, ItemEvent.ITEM_FIRST, this, ItemEvent.DESELECTED));
  				}
  				else
  				{
  					fireItemEvent(new ItemEvent(this, ItemEvent.ITEM_FIRST, this, ItemEvent.SELECTED));
  				}
  			}
  			
  			// if ring lock is on, check to see if new node is a leaf
  			if(editable && ringLock && (activeNodeIdx != -1))
  			{
  	  			rlFocusIdx = -1;
  	  			for(int ni=0; ni<nodeBounds.length; ni++)
  	  			{
  	  				if(ni != activeNodeIdx)
  	  				{
  	  					double x_ij = nData.getTieStrength(currentRelation, ni, activeNodeIdx);
  	  					double x_ji = nData.getTieStrength(currentRelation, activeNodeIdx, ni);
  	  					if((x_ij != 0) || (x_ji != 0))
  	  					{
  	  						if(rlFocusIdx == -1)
  	  						{
  	  							// this is first one -- keep it
  	  							rlFocusIdx = ni;
  	  						}
  	  						else
  	  						{
  	  							// this is second one -- reject all
  	  							rlFocusIdx = -1;
  	  							break;
  	  						}
  	  					}
  	  				}
  	  			}
  			}
  		}
  		else
  		{
  			// use mouseInNodeFrame badly...
  			if(editable)
  			{
  				mouseInNodeFrame = new DPoint(mouseDownPt.x, mouseDownPt.y);
  			}
  		}
    
		invalidateImage();
  	}
  
  	protected void invertTie(int rIdx, int i, int j)
  	{
  	  	// default implementation...makes sense for dichotomous networks
		double oldValue = nData.getTieStrength(rIdx, i, j);
		nData.setTieStrength(rIdx, i, j, 1.0-oldValue);
  	}
  	
  	public void mouseDragged(MouseEvent me)
  	{
  		if(!editable)
  		{
  			return;
  		}

  		Point currentMouse = me.getPoint();

  		if(editMode == EDIT)
  		{
  			if(activeNodeIdx == -1) return;
	 
  			if(ringLock && (rlFocusIdx != -1))
  			{
  				// find radius & angle
  				double fx = nData.getXPos(rlFocusIdx);
  				double fy = nData.getYPos(rlFocusIdx);
  				
  				double nx = nData.getXPos(activeNodeIdx);
  				double ny = nData.getYPos(activeNodeIdx);
  				
  				// this seems backwards
  				double mx = currentMouse.x + mouseInNodeFrame.x;
  				double my = currentMouse.y + mouseInNodeFrame.y;
  				
  				double radius = Math.sqrt((fx-nx)*(fx-nx) + (fy-ny)*(fy-ny));
  				double theta = Math.atan2(my-fy, mx-fx);
  				
  				nData.setXPos(activeNodeIdx, fx + radius * Math.cos(theta));
  				nData.setYPos(activeNodeIdx, fy + radius * Math.sin(theta));
  			}
  			else
  			{
	  			// relocate node such that new nodeBounds will line up right
	  			// w.r.t. mouse
	  			nData.setXPos(activeNodeIdx, currentMouse.x + mouseInNodeFrame.x);
	  			nData.setYPos(activeNodeIdx, currentMouse.y + mouseInNodeFrame.y);
  			}
  		}
  		else
  		{
  			double tx = currentMouse.x - mouseInNodeFrame.x;
  			double ty = currentMouse.y - mouseInNodeFrame.y;
  			// move everything
  			for(int ni=0; ni<nData.getSize(); ni++)
  			{
  				nData.setXPos(ni, nData.getXPos(ni) + tx);
  				nData.setYPos(ni, nData.getYPos(ni) + ty);
  			}
  			mouseInNodeFrame.x = currentMouse.x;
  			mouseInNodeFrame.y = currentMouse.y;
  		}	

		invalidateImage();
  	}
  
  	public void mouseMoved(MouseEvent me)
  	{
  	}
	  
  	public void keyPressed(KeyEvent ke)
  	{
  	}
  
  	public void keyReleased(KeyEvent ke)
  	{
  		if(!editable) return;
  		if(activeNodeIdx == -1) return;
  		int keyCode = ke.getKeyCode();
  		if((keyCode == KeyEvent.VK_BACK_SPACE) || 
  				(keyCode == KeyEvent.VK_DELETE) ||
  				(keyCode == KeyEvent.VK_CLEAR))
  		{
  			nData.deleteNode(activeNodeIdx);
  			initNodeBounds();
  			activeNodeIdx = -1;
  			repaint();
  		}
  	}
  
  	public void keyTyped(KeyEvent ke)
  	{
  	}
	
	public void colorModelChanged(ChangeEvent ce)
	{
		invalidateImage();
	}
	
	public NodeColorModel getNodeColorModel()
	{
		return colorModel;
	}
  
	protected class AnimatorThread extends Thread
	{
		protected boolean running;
		protected boolean sleeping;
		protected double phase;
		protected long sleepTime;
		
		public AnimatorThread()
		{
			this.running = false;
			this.sleeping = false;
			this.phase = 0.0;
			this.sleepTime = 1000;
		}
		
		public double getPhase()
		{
			return phase;
		}
		
		public void start()
		{
			super.start();
		}
		
		public void setRunning(boolean running)
		{
			this.running = running;
		}
		
		public void setSleeping(boolean sleeping)
		{
			this.sleeping = sleeping;
		}
		
		public void run()
		{
			while(true)
			{
				if(running)
				{
					phase -= 0.05;
					while(phase < 0.0)
					{
						phase += 1.0;
					}
					if(!sleeping)
					{
						repaint();
					}
				}
				
				try
				{
					sleep(sleepTime);
				}
				catch(InterruptedException ie)
				{
				}
			}
		}
	}
	
	protected class UpdaterThread extends Thread
	{
		protected long sleepTime;
		
		public UpdaterThread()
		{
			this.sleepTime = 500; // 2 frames per second?
		}
		
		public void run()
		{
			while(true)
			{
				// is image invalid?
				if(!imageValid)
				{
					// set it to true prior to updating image
					// that way, if image gets invalidated during
					// re-draw, we won't miss that.
					imageValid = true;
					_updateImage();
				}
				
				try
				{
					sleep(sleepTime);
				}
				catch(InterruptedException ie)
				{
				}
			}
		}
	}

  protected static final double PALETTE_VGAP = 2.0;
  protected static final double PALETTE_HGAP = 2.0;
  public class BlockPalette extends JComponent implements MouseListener,
    DataChangeListener
  {
  	// metrics
  	protected int pNameHt;
  	protected int maxWidth;
  	protected int numPartitions;
  	protected Font font;
  	protected String[] pNames;
  	protected int[] pWid;
  	protected int[] pIndices;
  	
  	public BlockPalette()
  	{
  		this.maxWidth = 0;
  		this.pNameHt = 0;
  		this.numPartitions = 1;
  		this.font = new Font("SansSerif", Font.PLAIN, 10);
  		
  		this.pNames = null;
  		this.pWid = null;
  		this.pIndices = null;
  		
  		addMouseListener(this);
  	}
  	
  	public Dimension getMinimumSize()
  	{
  		return getPreferredSize();
  	}
  	
  	public Dimension getPreferredSize()
  	{
  		calculateMetrics();
  		double height = numPartitions * (pNameHt + 2.0*BORDER) + 
  			(numPartitions + 1) * (PALETTE_VGAP);
  		double width = maxWidth + 2.0*BORDER + 2.0*PALETTE_HGAP;
			Dimension d = new Dimension(0, 0);
			d.setSize(width, height);
			return d;
  	}
  	
  	protected void calculateMetrics()
  	{
  		FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(font);
	    com.wibinet.math.Partition p = nData.getPartition();
	    pIndices = p.getPartitionIndices();
	    pNames = new String[pIndices.length];
	    pWid = new int[pIndices.length];
	    int maxWidTmp = 0;
	    for(int pi=0; pi<pIndices.length; pi++)
	    {
	    	pNames[pi] = p.getPartitionName(pIndices[pi]);
	    	pWid[pi] = fm.stringWidth(pNames[pi]);
	    	if(pWid[pi] > maxWidTmp)
	    	{
	    		maxWidTmp = pWid[pi];
	    	}
	    }
	    maxWidth = maxWidTmp;
	    pNameHt = fm.getHeight();
  	}
  	
  	public void paintComponent(Graphics _g)
  	{
			Graphics2D g = (Graphics2D)_g;
	    Dimension d = getSize();
	    g.setFont(font);
	    calculateMetrics();
	    FontMetrics fm = g.getFontMetrics(font);

	    double rWidth = 2.0*BORDER + maxWidth;
	    double rHeight = 2.0*BORDER + pNameHt;
	    double ascent = fm.getAscent();
	    
	    // clear out
	    g.setColor(getBackground());
	    g.fillRect(0, 0, d.width, d.height);
	    
	    // paint boxes
	    for(int pi=0; pi<pNames.length; pi++)
	    {
	    	double y = pi * (rHeight + PALETTE_VGAP) + PALETTE_VGAP;
	    	// g.setColor(getNodeColor(pIndices[pi]));
				// need to rethink this part
				g.setColor(Color.yellow);
				Rectangle2D cell = new Rectangle2D.Double(PALETTE_HGAP, y,
					rWidth, rHeight);
	    	g.fill(cell);
	    		
	    	g.setColor(Color.black);
	    	g.drawString(pNames[pi], 
	    	  (float)(PALETTE_HGAP + BORDER + (maxWidth - pWid[pi]) / 2), 
					(float)(y + ascent));
	    	g.draw(cell);
	    }
  	}
	  	
	  public void mouseEntered(MouseEvent me)
	  {
	  }
	  
	  public void mouseExited(MouseEvent me)
	  {
	  }
	  
	  public void mouseClicked(MouseEvent me)
	  {
	    Point mouseDownPt = me.getPoint();
	    int clickCt = me.getClickCount();
	    
	    // get partition index
	    int pIdx = -1;
	    double rWidth = 2.0*BORDER + maxWidth;
	    double rHeight = 2.0*BORDER + pNameHt;
	    for(int pi=0; pi<pNames.length; pi++)
	    {
	    	double yTop = pi * (rHeight + PALETTE_VGAP) + PALETTE_VGAP;
	    	double xLeft = PALETTE_HGAP;
	    	Rectangle2D r = new Rectangle2D.Double(xLeft, yTop, rWidth, rHeight);
	    	if(r.contains(mouseDownPt))
	    	{
	    		pIdx = pi;
	    	}
	    }
	    if((pIdx != -1) && (clickCt == 2))
	    {
	  		// rename?
	  		Object ret = JOptionPane.showInputDialog(this,
	  		  "Set partition name to:", "Rename Partition",
	  		  JOptionPane.PLAIN_MESSAGE, null, null,
	  		  nData.getPartition().getPartitionName(pIndices[pIdx]));
	  		if(ret != null)
	  		{
	  		  nData.getPartition().setPartitionName(pIndices[pIdx], (String)ret);
	  		  calculateMetrics();
	  		  getParent().validate();
	  		  repaint();
	  		}
	    }
	  }
	  
	  public void mouseReleased(MouseEvent me)
	  {
	  }
	  
	  public void mousePressed(MouseEvent me)
	  {
	  }
    
    public void dataChanged(DataChangeEvent dce)
    {
      if(dce.getType() == DataChangeEvent.PARTITION_CHANGED)
      {
        repaint();
      }
    }
	}
}
