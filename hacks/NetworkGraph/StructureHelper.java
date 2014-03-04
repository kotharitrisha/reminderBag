package com.wibinet.networks;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.*;

import com.wibinet.app.*;
import com.wibinet.gui.*;

public class StructureHelper extends Object
{
	protected final static byte CONSTRAINT = 0;
	protected final static byte EFFECTIVE_SIZE = 1;
	protected final static String[] types = 
		{"Constraint", "Effective Size"};
	
	protected final static byte OUTBOUND = 0;
	protected final static byte INBOUND = 1;
	protected final static byte BOTH = 2;
	protected final static String[] investments =
		{"Outbound", "Inbound", "Both"};
  
	protected final static String CMD_CHOOSE_FILE = "ChooseFile";

	public StructureHelper()
	{
	}

	public Class getEvaluatorClass()
	{
		return Double.class;
	}
  
	public String getName()
	{
		return "Structural Equivalence";
	}
  
	public String getGroup()
	{
		return "Structure";
	}
  
	public void edit(Evaluator evaluator, JFrame parent)
	{
		if(!(evaluator instanceof Evaluator))
		{
			return; // error?
		}

		// create an editor dialog box
		JDialog editor = new Editor(parent, (Evaluator)evaluator);
		editor.setVisible(true);
	}

	protected class Editor extends JDialog implements ActionListener, ItemListener
	{
	    protected Evaluator evaluator;
	    protected JTextField tfName;
		protected JComboBox cbType;
		protected CardLayout clOptions;
		protected JPanel pOptions;
		protected JCheckBox cbDiagonals;
		protected JComboBox cbInvestment;
		protected JLabel lOrgFileName;
		protected File orgFile;
			
	    public Editor(JFrame parent, Evaluator evaluator)
	    {
	    	super(parent, "Structure");
	    	setModal(true);
	    	this.evaluator = evaluator;
	      
	    	getContentPane().setLayout(new BorderLayout());
	      
	    	// name and type
	    	JPanel pNameType = new JPanel(new GridLayout(2, 1));
	    	JPanel pName = new JPanel(new BorderLayout());
	    	pName.add(new JLabel("Name: "), BorderLayout.WEST);
	    	tfName = new JTextField(evaluator.getName());
	    	pName.add(tfName, BorderLayout.CENTER);
	    	pNameType.add(pName);
	    	JPanel pType = new JPanel(new BorderLayout());
	    	pType.add(new JLabel("Type: "), BorderLayout.WEST);
	    	cbType = new JComboBox(types);
	    	cbType.setEditable(false);
	    	cbType.setSelectedIndex(evaluator.type);
	    	cbType.addItemListener(this);
	    	pType.add(cbType, BorderLayout.CENTER);
	    	pNameType.add(pType);
	    	getContentPane().add(pNameType, BorderLayout.NORTH);
	      
	    	// options panel
	    	this.clOptions = new CardLayout();
	    	this.pOptions = new JPanel(clOptions);
	      
	    	JPanel pConstraintRoot = new JPanel(new BorderLayout());
	    	Box pConstraintOpts = new Box(BoxLayout.Y_AXIS);
	    	JPanel pDiagonals = new JPanel(new BorderLayout());
	    	cbDiagonals = new JCheckBox("Ignore Diagonals", evaluator.ignoreDiagonals);
	    	pDiagonals.add(cbDiagonals, BorderLayout.CENTER);
	      	pConstraintOpts.add(pDiagonals);
	      	JPanel pInvestment = new JPanel(new BorderLayout());
	      	pInvestment.add(new Label("Investments "), BorderLayout.WEST);
	      	cbInvestment = new JComboBox(investments);
	      	cbInvestment.setEditable(false);
	      	cbInvestment.setSelectedIndex(evaluator.investments);
	      	pInvestment.add(cbInvestment, BorderLayout.CENTER);
	      	pConstraintOpts.add(pInvestment);
	      	JPanel pOrganization = new JPanel(new GridLayout(2, 1));
	      	lOrgFileName = new JLabel("File: <none selected>");
	      	pOrganization.add(lOrgFileName);
	      	JPanel pFileChoose = new JPanel(new FlowLayout(FlowLayout.CENTER));
	      	JButton bFileChoose = new JButton("Choose File...");
	      	bFileChoose.addActionListener(this);
	      	bFileChoose.setActionCommand(CMD_CHOOSE_FILE);
	      	pFileChoose.add(bFileChoose);
	      	pOrganization.add(pFileChoose);
	      	pConstraintOpts.add(pOrganization);
	      	pConstraintRoot.add(pConstraintOpts, BorderLayout.NORTH);
	      	pOptions.add(pConstraintRoot, types[CONSTRAINT]);
	      	
	      	JPanel pSizeRoot = new JPanel(new BorderLayout());
	      	pOptions.add(pSizeRoot, types[EFFECTIVE_SIZE]);
	      	clOptions.first(pOptions);
				
	      	getContentPane().add(BorderLayout.CENTER, pOptions);

	      	// ok/cancel panel
	      	JOkCancelPanel okCancelPanel = new JOkCancelPanel();
	      	okCancelPanel.addActionListener(this);
	      	getContentPane().add(BorderLayout.SOUTH, okCancelPanel);
	      
	      	pack();
	    }
	    
	    public void actionPerformed(ActionEvent ae)
	    {
	    	String cmd = ae.getActionCommand();
	    	if(JOkCancelPanel.OK.equals(cmd))
	    	{
	    		// update evaluator
	    		evaluator.name = tfName.getText();
	    		evaluator.type = (byte)cbType.getSelectedIndex();
	    		evaluator.ignoreDiagonals = cbDiagonals.isSelected();
	    		evaluator.investments = (byte)cbInvestment.getSelectedIndex();
	    		evaluator.orgFile = orgFile;
	    		setVisible(false);
	    	}
	    	else if(JOkCancelPanel.CANCEL.equals(cmd))
	    	{
	    		// don't update evaluator
	    		setVisible(false);
	    	}
	    	else if(CMD_CHOOSE_FILE.equals(cmd))
	    	{
	    		// get a file dialog
	    		JFrame f = new JFrame();
	    		JFileChooser jfc = Application.getFileChooser();
	    		int res = jfc.showOpenDialog(f);
	    		Application.setWorkingDirectory(jfc.getCurrentDirectory());
	    		if(res == JFileChooser.CANCEL_OPTION)
	    		{
	    			return;
	    		}
	    		orgFile = jfc.getSelectedFile();
	    		lOrgFileName.setText("File: " + orgFile.getName());
	    	}
	    }
	    
	    public void itemStateChanged(ItemEvent evt)
	    {
	    	// only on 'selection' event
	    	if(evt.getStateChange() == ItemEvent.SELECTED)
	    	{
				clOptions.show(pOptions,
				types[cbType.getSelectedIndex()]);
			}
	    }
	}	  

	public class Evaluator extends Object
	{
	    protected Relation r;
	    protected String name;
	    protected byte type;
		protected boolean ignoreDiagonals;
		protected byte investments;
		protected Double[] nodeValues;
		protected Double[][] allValues;
		protected File orgFile;
	    
	    public Evaluator()
	    {
	    	this.r = null;
	    	this.name = "Constraint";
	    	this.type = CONSTRAINT;
			this.ignoreDiagonals = true;
			this.investments = OUTBOUND;
			this.nodeValues = new Double[0];
			this.allValues = new Double[0][0];
			this.orgFile = null;
	    }
	    
	    public void runEvaluator()
	    {
			int nSize = r.getNodeCount();
			nodeValues = new Double[nSize];
			allValues = new Double[nSize][];
			for(int i=0; i<nSize; i++)
			{
				allValues[i] = new Double[nSize];
			}
	
			// get Oj's
			double[] o = new double[nSize];
			if(orgFile == null)
			{
				for(int i=0; i<nSize; i++)
				{
					o[i] = 1.0;
				}
			}
			else
			{
				try
				{
					FileInputStream fis = new FileInputStream(orgFile);
					InputStreamReader isr = new InputStreamReader(fis);
					BufferedReader in = new BufferedReader(isr, 8096);
					String line = in.readLine();
						
					// try to guess delimiters
					String delim = " ";
					if(line.indexOf('\t') != 0)
					{
						delim = "\t";
					}
					else if(line.indexOf(',') != 0)
					{
						delim = ",";
					}
					Hashtable values = new Hashtable();
					while(line != null)
					{
						StringTokenizer st = new StringTokenizer(line, delim);
						line = in.readLine();
						String key = st.nextToken().trim();
						if(st.hasMoreTokens())
						{
							try
							{
								Double d = Double.valueOf(st.nextToken());
								values.put(key, d);
							}
							catch(NumberFormatException nfe)
							{
								// ignore?
							}
						}
					}
					fis.close();
					TreeSet missingActors = new TreeSet();
					for(int i=0; i<nSize; i++)
					{
						String actorName = r.getParent().getActor(i).getName();
						Double oValue = (Double)values.get(actorName);
						if(oValue == null)
						{
							missingActors.add(actorName);
							o[i] = 1.0;
						}
						else
						{
							o[i] = oValue.doubleValue();
						}
					}
					if(missingActors.size() > 0)
					{
						String errorMsg = "Couldn't find organization values for ";
						if(missingActors.size() <= 10)
						{
							Iterator iter = missingActors.iterator();
							errorMsg += "'"+iter.next()+"'";
							while(iter.hasNext())
							{
								errorMsg += ", '"+iter.next()+"'";
							}
							errorMsg += ".";
						}
						else
						{
							errorMsg += missingActors.size() + " actors.";
						}
						JFrame f = new JFrame();
						JOptionPane.showMessageDialog(f, errorMsg, "Missing Values",
							JOptionPane.WARNING_MESSAGE);
					}
				}
				catch(IOException ioe)
				{
					Application.handleNonFatalThrowable(ioe);
				}
			}
			
			// i think i need to calculate all p_ij's
			double[][] p = new double[nSize][];
			for(int i=0; i<nSize; i++)
			{
				p[i] = new double[nSize];
				double i_out = 0.0;
				for(int j=0; j<nSize; j++)
				{
					if((i != j) || (!ignoreDiagonals))
					{
						if(investments == OUTBOUND || investments == BOTH)
						{
							i_out += r.getTieStrength(i, j);
						}
						if(investments == INBOUND || investments == BOTH)
						{
							i_out += r.getTieStrength(j, i);
						}
					}
				}
					
				for(int j=0; j<nSize; j++)
				{
					p[i][j] = 0.0;
					if(i_out != 0.0)
					{
						if(investments == OUTBOUND || investments == BOTH)
						{
							p[i][j] += r.getTieStrength(i, j) / i_out;
						}
						if(investments == INBOUND || investments == BOTH)
						{
							p[i][j] += r.getTieStrength(j, i) / i_out;
						}
					}
				}
			}
				
			switch(type)
			{
				// implementation of Burt (1992: 64) equation 2.7
				case CONSTRAINT:
					for(int i=0; i<nSize; i++)
					{
						double i_total = 0.0;
						allValues[i][i] = new Double(Double.NaN);
						for(int j=0; j<nSize; j++)
						{
							if(i != j)
							{
								double c_sum = p[i][j];
								for(int q=0; q<nSize; q++)
								{
									if((q != i) && (q != j))
									{
										c_sum += p[i][q] * p[q][j];
									}
								}
								double c_ij = c_sum * c_sum * o[j];
								i_total += c_ij;
								allValues[i][j] = new Double(c_ij); 
							}
						}
						nodeValues[i] = new Double(i_total);
					}
					break;
						
				case EFFECTIVE_SIZE:
					break;
			}
	    }
					
		  
		  public String getName()
		  {
		    return name;
		  }
		  
	  }

}
