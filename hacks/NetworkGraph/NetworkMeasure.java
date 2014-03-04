/**
 * NetworkMeasure.java
 *
 * Copyright (c) 2003 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

public interface NetworkMeasure
{
	public double getStatistic(NetworkData nd);
	public String getName();
	public NetworkMeasureFactory getFactory();
	public double getInitialEstimate(NetworkData nd);
}