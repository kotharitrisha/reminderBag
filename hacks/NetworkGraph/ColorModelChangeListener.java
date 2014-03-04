/**
 * ColorModelChangeListener.java
 *
 * I wonder if this is really necessary?
 *
 * Copyright (c) 2002 Wibi Internet.
 * All rights reserved.
 */

package com.wibinet.networks;

import javax.swing.event.*;

public interface ColorModelChangeListener
{
	public void colorModelChanged(ChangeEvent ce);
}