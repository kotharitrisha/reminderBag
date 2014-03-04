/**
 * TypedNetworkMeasure.java
 *
 * Copyright (c) 2003 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

public interface TypedNetworkMeasure
{
	public String getName();
	public void setName(String name);
	public int getType();
	public void setType(int type);
	public String[] getTypeStrings();
	public String[] getKeyStrings();
}