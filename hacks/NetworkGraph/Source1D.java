/**
 * Source1D.java
 *
 * Copyright (c) 2002 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import javax.swing.*;

public interface Source1D
{
	public double getValue(int nodeIdx);
	public JPanel getEditor();
	public void addColorModelChangeListener(ColorModelChangeListener cmcl);
	public void removeColorModelChangeListener(ColorModelChangeListener cmcl);
}