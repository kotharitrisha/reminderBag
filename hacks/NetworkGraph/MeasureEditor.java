/**
 * MeasureEditor.java
 *
 * An ill-concieved class, perhaps.  It's poorly named for sure.
 * But I'm hoping that this class might serve as a default editor
 * for all kinds of measures used in this package, and perhaps
 * for other applications as well.
 *
 * Copyright (c) 2003, 2006 Wibi Internet
 */

package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import com.wibinet.app.*;
import com.wibinet.gui.*;

public class MeasureEditor extends JDialog implements ActionListener, ItemListener
{
	protected JTextField tfName;
	protected JTextPane helpPane;
	protected HTMLDocument helpDoc;
	protected JRadioButton[] rbType;
	protected int type;
	protected Object measure;
	
	public MeasureEditor(Dialog parent, Object measure, String title)
	{
		super(parent);
		comConstructor(measure, title);
	}
	
	public MeasureEditor(Frame parent, Object measure, String title)
	{
		super(parent);
		comConstructor(measure, title);
	}
	
	public MeasureEditor(Object measure, String title)
	{
		super();
		comConstructor(measure, title);
	}
	
	protected void comConstructor(Object measure, String title)
	{
		setModal(true);
		setTitle(title);
		getContentPane().setLayout(new BorderLayout());
		this.measure = measure;
		
		if(measure instanceof TypedNetworkMeasure)
		{
			TypedNetworkMeasure tMeasure = (TypedNetworkMeasure)measure;
			JPanel namePanel = new JPanel(new BorderLayout());
			namePanel.add(BorderLayout.WEST, new JLabel("Name: "));
			tfName = new JTextField(tMeasure.getName());
			namePanel.add(BorderLayout.CENTER, tfName);
			getContentPane().add(BorderLayout.NORTH, namePanel);
		}

		if(measure instanceof TypedNetworkMeasure)
		{
			TypedNetworkMeasure tMeasure = (TypedNetworkMeasure)measure;
			String[] typeStrings = tMeasure.getTypeStrings();
			String[] keyStrings = tMeasure.getTypeStrings();

			JPanel optsPanel = new JPanel(new BorderLayout());
			ButtonGroup bgType = new ButtonGroup();
			rbType = new JRadioButton[typeStrings.length];
			JPanel rbPanel = new JPanel(new GridLayout(rbType.length, 1));
			for(int bi=0; bi<rbType.length; bi++)
			{
				rbType[bi] = new JRadioButton(typeStrings[bi]);
				bgType.add(rbType[bi]);
				if(tMeasure.getType() == bi)
				{
					type = bi;
					rbType[bi].setSelected(true);
				}
				rbPanel.add(rbType[bi]);
			}
			for(int bi=0; bi<rbType.length; bi++)
			{
				rbType[bi].addItemListener(this);
			}
			optsPanel.add(BorderLayout.NORTH, rbPanel);
			
			HTMLEditorKit eKit = new HTMLEditorKit();
			helpDoc = (HTMLDocument)eKit.createDefaultDocument();
			helpPane = new JTextPane(helpDoc);
			helpPane.setEditorKit(eKit);
			helpPane.setEditable(false);
			helpPane.setText(Application.getHelpText(measure.getClass().getName() +
				"."+keyStrings[type]));
			helpPane.setPreferredSize(new Dimension(250, 100));
			JScrollPane scroller = new JScrollPane(helpPane);
			optsPanel.add(BorderLayout.CENTER, scroller);
			getContentPane().add(BorderLayout.CENTER, optsPanel);
		}
		
		JOkCancelPanel ocp = new JOkCancelPanel();
		ocp.addActionListener(this);
		getContentPane().add(BorderLayout.SOUTH, ocp);
		pack();
	}

	public void actionPerformed(ActionEvent ae)
	{
		String cmd = ae.getActionCommand();
		if(JOkCancelPanel.OK.equals(cmd))
		{
			if(measure instanceof TypedNetworkMeasure)
			{
				TypedNetworkMeasure tMeasure = (TypedNetworkMeasure)measure;
				tMeasure.setName(tfName.getText());
				tMeasure.setType(type);
			}
		}
		setVisible(false);
		dispose();
	}
	
	public void itemStateChanged(ItemEvent ie)
	{
		if(measure instanceof TypedNetworkMeasure)
		{
			TypedNetworkMeasure tMeasure = (TypedNetworkMeasure)measure;
			String[] typeStrings = tMeasure.getTypeStrings();
			String[] keyStrings = tMeasure.getKeyStrings();
			if(ie.getStateChange() == ItemEvent.SELECTED)
			{
				// update type
				for(int bi=0; bi<rbType.length; bi++)
				{
					if(rbType[bi].isSelected())
					{
						type = bi;
						break;
					}
				}
				
				// update helpText
				helpPane.setText(Application.getHelpText(measure.getClass().getName() +
					"."+keyStrings[type]));
				
				// update text field?
				tfName.setText(typeStrings[type]);
			}
		}
	}
}