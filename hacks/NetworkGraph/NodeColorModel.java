/**
 * NodeColorModel.java
 *
 * Copyright (c) 2002 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.awt.*;

public class NodeColorModel extends Object
{
	protected NetworkData nData;
	protected Source1D hueSource;
	protected Source1D saturationSource;
	protected Source1D brightnessSource;
	
	public final static int HUE        = 0;
	public final static int SATURATION = 1;
	public final static int BRIGHTNESS = 2;
	
  protected final static Color[] nodeColors = new Color[9];
  static
  {
    // may want to check Tufte or even better...
    // http://www.essc.psu.edu/~cbrewer/ColorSch/Schemes.html
		// http://www.personal.psu.edu/faculty/c/a/cab38/ColorSch/Schemes.html
    nodeColors[0] = Color.yellow;
    nodeColors[1] = new Color(0xC0, 0xFF, 0xC0); // light green
    nodeColors[2] = Color.cyan;
    nodeColors[3] = new Color(0xFF, 0xC0, 0xC0); // light red
    nodeColors[4] = Color.orange;
    nodeColors[5] = Color.magenta;
    nodeColors[6] = new Color(0xC0, 0xC0, 0xFF); // light blue
    nodeColors[7] = Color.white;
    nodeColors[8] = Color.lightGray;
  }
	
	public NodeColorModel(NetworkData nData)
	{
		this.nData = nData;
		this.hueSource = new BlockmodelSource(nData);
		this.brightnessSource = new FixedSource();
		this.saturationSource = new FixedSource();
	}
	
	public Color getColor(int nodeIdx)
	{
		double hue = hueSource.getValue(nodeIdx);
		double sat = saturationSource.getValue(nodeIdx);
		double bri = brightnessSource.getValue(nodeIdx);
		
		Color c = Color.getHSBColor((float)hue, (float)sat, (float)bri);

		return c;
	}
	
	public Source1D getSource(int type)
	{
		switch(type)
		{
			case HUE:
				return hueSource;
				
			case SATURATION:
				return saturationSource;
				
			case BRIGHTNESS:
				return brightnessSource;
		}
		return null;
	}
	
	public void setSource(int type, Source1D source)
	{
		switch(type)
		{
			case HUE:
				hueSource = source;
				break;
				
			case SATURATION:
				saturationSource = source;
				break;
				
			case BRIGHTNESS:
				brightnessSource = source;
				break;
		}
		return;
	}
}