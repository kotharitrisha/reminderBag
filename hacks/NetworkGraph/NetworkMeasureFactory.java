/**
 * NetworkMeasureFactory.java
 *
 * Copyright (c) 2003 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import java.awt.*;
import javax.swing.*;
import com.wibinet.app.*;

public interface NetworkMeasureFactory
{
	// tricky...
  public final static NetworkMeasureFactory[] Factories = (NetworkMeasureFactory[])
      Application.getPlugIns("com.wibinet.networks.NetworkMeasureFactory");

	public boolean isMultiple();
  public NetworkMeasure newInstance();
	public NetworkMeasure[] newInstances(NetworkData nd);
  public void edit(NetworkMeasure measure, Window parent);
	public String getName();
  public String getGroup();
}