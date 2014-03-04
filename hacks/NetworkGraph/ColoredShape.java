/**
 * ColoredShape.java
 *
 * This class is used mostly by NetworkViewer, though I suppose
 * it could be more general than that.  A shape that knows
 * it's depth in a "sort order" and how it should be drawn
 * or painted into a Graphics2D object.
 *
 * Copyright (c) 2008 Wibi Internet.
 */

package com.wibinet.networks;

import java.awt.*;

public class ColoredShape extends Object
{
	public final static int FILL = 1;
	public final static int DRAW = 2;
	
	protected Shape shape;
	protected Color color;
	protected double depth;
	protected int paintMethod;
	
	public ColoredShape(Shape shape, Color color, double depth, int paintMethod)
	{
		this.shape = shape;
		this.color = color;
		this.depth = depth;
		this.paintMethod = paintMethod;
	}
	
	public double getDepth()
	{
		return depth;
	}
	
	public void paint(Graphics2D g)
	{
		g.setColor(color);
		if(paintMethod == DRAW) {
			g.draw(shape);
		} else {
			g.fill(shape);
		}
	}
}
