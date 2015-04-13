/*
 * Copyright (C) 2012-2013 Falko Hofmann Max Planck Institute for Biology
 * of Ageing, Cologne (MPI-age)
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package age.mpg.de.peanut.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import age.mpg.de.peanut.Peanut;
import age.mpg.de.peanut.cytoscape.CytoscapeAttributeGetter;
import age.mpg.de.peanut.cytoscape.CytoscapeNodeSelector;
import age.mpg.de.peanut.gui.tablemodel.PeanutTableModelPathways;
import age.mpg.de.peanut.model.PeanutModel;
import age.mpg.de.peanut.statistics.PathwayStatistics;
import age.mpg.de.peanut.utilityobjects.PluginProperties;
import cytoscape.CyNetwork;
import cytoscape.Cytoscape;

public class ResultDialogues extends JDialog{
	
	private PathwayStatistics stats;
	private JTextField filterText, pValueField;
	private TableRowSorter<PeanutTableModelPathways> sorter;
	private File outputFile  = new File("output.txt");
	private String[] networkIdArray;
	private String[] networkTitleArray;
	private String[] fdrArray = {"No FDR","Bonferroni","Benjamini Hochberg"};

	private PeanutTableModelPathways tableModel;
	private JTable table;
	private final static String titel = PluginProperties.getInstance().getPluginName() +  " - Results";
	
	
	
	private Comparator<String> decimalCompare, fractionCompare;
	
	public ResultDialogues(Frame owner){

		super(owner,titel, false);

		
		// if no network is loaded --> show error
		if (Cytoscape.getNetworkSet().size() == 0){
			loadNetworkError();
			dispose();
		}
		// if a network is loaded check if pathwayfinder attributes are present or the search has been canceled during the run
		else{
			CytoscapeAttributeGetter cyGet = new CytoscapeAttributeGetter();
			cyGet.checkIfPathwayAttributeIsPresent(PeanutModel.COLUMN_TITLE_PATHWAY_IDENTIFIER_LIST);
			
			if (PeanutModel.getInstance().isFinished())
				createResultDialog();//startDialog();		
			else
				showRunError();

			if (PeanutModel.getInstance().isExit())
				showCancelError();
		}
	}

	
	private JPanel selectNetworkPanels(){

		String lableNetworkParent = "Background Network:"; String lableNetworkChild = "Focus Network:"; 
		String lableTextField = "P-value:"; String lableFdr ="Multiple Testing:";
		

		Set<CyNetwork>networkSet = Cytoscape.getNetworkSet();
		networkIdArray = new String[networkSet.size()];
		networkTitleArray = new String[networkSet.size()];
		// add network identifies to arrays for a combobox --> one contains the networkd id, one the network title
		int i = 0;
		int max = 0;
		int indexMaxNetwork = 0;
		for (CyNetwork network : networkSet){
			if (max < network.getNodeCount()){
				max = network.getNodeCount();
				indexMaxNetwork = i;
			}
			networkIdArray[i] = network.getIdentifier();
			networkTitleArray[i] = network.getTitle();
			i++;
		}
		
		//create JComboBox and listener for network selection
		JComboBox cbNetworkParent = new JComboBox(networkTitleArray);
		cbNetworkParent.setEditable(false);
		cbNetworkParent.setSelectedIndex(indexMaxNetwork);
		PeanutModel.getInstance().setParentNetwork((String) cbNetworkParent.getSelectedItem());
		cbNetworkParent.addItemListener(new ItemListener(){
	    	public void itemStateChanged(ItemEvent evt){
	    		for (int i = 0; i < networkTitleArray.length; i++){
	    			if (networkTitleArray[i].equals((String) evt.getItem()))
	    				PeanutModel.getInstance().setParentNetwork(networkIdArray[i]);	
	    		}
	    	}});
		
		//create JComboBox and listener for network selection
		JComboBox cbNetworkChild = new JComboBox(networkTitleArray);
		cbNetworkChild.setEditable(false);
		cbNetworkChild.setSelectedIndex(0);
		PeanutModel.getInstance().setChildNetwork(networkIdArray[0]);
		cbNetworkChild.addItemListener(new ItemListener(){
	    	public void itemStateChanged(ItemEvent evt){
	    		for (int i = 0; i < networkTitleArray.length; i++){
	    			if (networkTitleArray[i].equals((String) evt.getItem()))
	    	    		PeanutModel.getInstance().setChildNetwork(networkIdArray[i]);	
	    		}
	    	}});	
	
		//create JComboBox and listener for  selection
		JComboBox fdrCorrection = new JComboBox(fdrArray);
		fdrCorrection.setEditable(false);
		fdrCorrection.setSelectedIndex(2);
		fdrCorrection.addItemListener(new ItemListener(){
	    	public void itemStateChanged(ItemEvent evt){
	    		String input = (String) evt.getItem();
	    		if (input.equals(fdrArray[0])){
	    			PeanutModel.getInstance().setBejaminiHoechstFDR(false);
	    			PeanutModel.getInstance().setBonferroni(false);
	    			PeanutModel.getInstance().setNoFRD(true);
	    		}
	    		if (input.equals(fdrArray[1])){
	    			PeanutModel.getInstance().setBejaminiHoechstFDR(false);
	    			PeanutModel.getInstance().setBonferroni(true);
	    			PeanutModel.getInstance().setNoFRD(false);
	    		}
	    		if (input.equals(fdrArray[2])){
	    			PeanutModel.getInstance().setBejaminiHoechstFDR(true);
	    			PeanutModel.getInstance().setBonferroni(false);
	    			PeanutModel.getInstance().setNoFRD(false);
	    		}
	    	}});	

		// create JTextField for entering the p-Value threshold
		pValueField = new JTextField();
		pValueField.setText(String.valueOf(PeanutModel.getInstance().getpValue()));
		pValueField.setPreferredSize(new Dimension(60,30));
		pValueField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {	
				PeanutModel.getInstance().setpValueFromString(pValueField.getText());
			}
	        public void insertUpdate(DocumentEvent e) {
				PeanutModel.getInstance().setpValueFromString(pValueField.getText());
	        }
	        public void removeUpdate(DocumentEvent e) {
				PeanutModel.getInstance().setpValueFromString(pValueField.getText());
		   }});
		
		
		//create "next" button for the menu and add listeners
		JButton searchBtn = new JButton("(re)calculate");
		searchBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//checks if p-value is in a valid range
				if (PeanutModel.getInstance().getpValue() > 1.0 || PeanutModel.getInstance().getpValue() <= 0.0)
					pValueError();
				//checks if child and parent network are not the same.
				else if (PeanutModel.getInstance().getChildNetwork().equals(PeanutModel.getInstance().getParentNetwork()))
					showNetworkWarning();
				else{
					Peanut find = new Peanut();
					find.getResults();
					tableModel.setModel(PeanutModel.getInstance().getStatisticsResultList());
					}
			}});

		// create JLables for the components
		JLabel jLableComboBoxNetworkParent = new JLabel(lableNetworkParent, JLabel.RIGHT);
		JLabel jLableComboBoxNetworkChild = new JLabel(lableNetworkChild, JLabel.RIGHT);
		JLabel jLableComboBoxFdr = new JLabel(lableFdr, JLabel.RIGHT);
		JLabel jLableTextField = new JLabel(lableTextField,SwingConstants.TRAILING);
				
		JPanel selectNetworksPanel = new JPanel();
	
		selectNetworksPanel.add(jLableComboBoxNetworkParent);
		selectNetworksPanel.add(cbNetworkParent);
		selectNetworksPanel.add(jLableComboBoxNetworkChild);
		selectNetworksPanel.add(cbNetworkChild);
		selectNetworksPanel.add(jLableComboBoxFdr);
		selectNetworksPanel.add(fdrCorrection);
		selectNetworksPanel.add(jLableTextField);
		selectNetworksPanel.add(pValueField);
		selectNetworksPanel.add(searchBtn);
	
		return selectNetworksPanel;
	}	
		

	
	private void overrideComperators(){
		decimalCompare =  new Comparator<String>(){
			@Override
			public int compare(String decimal1, String decimal2) {
				
				Scanner scannerD1 = new Scanner(decimal1).useLocale(Locale.getDefault());
				Double d1 = scannerD1.nextDouble();
				
				Scanner scannerD2 = new Scanner(decimal2).useLocale(Locale.getDefault());
				Double d2 = scannerD2.nextDouble();
				
				return d1.compareTo(d2);			
			}
		};
		
		fractionCompare =  new Comparator<String>(){
			@Override
			public int compare(String str1, String str2) {
				String[]str1Arr = str1.split("/");
				String[]str2Arr = str2.split("/");
				int str1A = Integer.parseInt(str1Arr[0]);
				int str1B = Integer.parseInt(str1Arr[1]);
				int str2A = Integer.parseInt(str2Arr[0]);
				int str2B = Integer.parseInt(str2Arr[1]);
				Double d1 = str1A/(str1B*1.0);
				Double d2 = str2A/(str2B*1.0);
				return d1.compareTo(d2);			
			}
		};
	}
	
	
	
	private void createSorter(){
		//set the overridden sorters
		sorter = new TableRowSorter<PeanutTableModelPathways>(tableModel);	
		sorter.setSortable(0, true);
		sorter.setSortable(1, true);
		sorter.setSortable(2, true);
		sorter.setSortable(3, true);
		sorter.setSortable(4, true);
		sorter.setSortable(5, true);
		sorter.setSortable(6, true);
		sorter.setComparator(PeanutTableModelPathways.COVERAGE_COLUMN, fractionCompare);
		sorter.setComparator(PeanutTableModelPathways.P_VALUE_COLUMN, decimalCompare);
		sorter.setComparator(PeanutTableModelPathways.COVERAGE_PERCENT_COLUMN, decimalCompare);
				
	}
	
	
	private void createTable(){
		//create JTable and enable sorting
		table = new JTable(tableModel);	
		table.setCellSelectionEnabled(true);
		table.setDragEnabled(true);
		table.setAutoCreateRowSorter(true);
		table.getColumnModel().getColumn(PeanutTableModelPathways.PATHWAYNAME_COLUMN).setPreferredWidth(200);
		table.getColumnModel().getColumn(PeanutTableModelPathways.NUMBER_OF_NODES_COLUMN).setPreferredWidth(90);
		table.getColumnModel().getColumn(PeanutTableModelPathways.COVERAGE_COLUMN).setPreferredWidth(80);
		table.getColumnModel().getColumn(PeanutTableModelPathways.COVERAGE_PERCENT_COLUMN).setPreferredWidth(100);
		table.getColumnModel().getColumn(PeanutTableModelPathways.P_VALUE_COLUMN).setPreferredWidth(90);
		table.getColumnModel().getColumn(PeanutTableModelPathways.SOURCE_COLUMN).setPreferredWidth(135);
		table.getColumnModel().getColumn(PeanutTableModelPathways.SELECT_COLUMN).setPreferredWidth(80);
		table.setRowSorter(sorter);
		table.setBorder(BorderFactory.createEtchedBorder());
	}
	
	
	private void createFilterTextField(){
		//create text field for search filter an register listener
		filterText = new JTextField();
		filterText.setPreferredSize(new Dimension(800,39));
		filterText.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				newFilter();
			}
			public void insertUpdate(DocumentEvent e) {
				newFilter();
			}
			public void removeUpdate(DocumentEvent e) {
				newFilter();
		}});
	}
	
	
	
	private void createResultDialog(){
		
		//Strings for lables and headers
		String lableSearch =  "Search:";
		
		//create table model
		tableModel = new PeanutTableModelPathways(PeanutModel.getInstance().getStatisticsResultList());
		
		//override comperators for custom sorting 
		overrideComperators();
		createSorter();
		createTable();
		createFilterTextField();

		
		JPanel contentPanel = new JPanel();
		
		JScrollPane listScroller = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		listScroller.setPreferredSize(new Dimension(800,300));
		listScroller.setBorder(BorderFactory.createTitledBorder("Enriched pathways:"));
		listScroller.setBackground(this.getBackground());		
		
		//create lable for search field
		JLabel searchLable = new JLabel(lableSearch, SwingConstants.TRAILING);
		searchLable.setLabelFor(filterText);
		
		//create Panels for nicer look and add the corresponding panels
		JPanel panelSearchField = new JPanel();
		panelSearchField.add(searchLable);
		panelSearchField.add(filterText);
		
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		southPanel.add(panelSearchField, BorderLayout.CENTER);
		southPanel.add(buttonPanel(), BorderLayout.SOUTH);
		
		//add listScroller to  content panel and set layout
		contentPanel.setLayout(new BorderLayout());
		contentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		contentPanel.add(selectNetworkPanels(), BorderLayout.PAGE_START);
		contentPanel.add(listScroller, BorderLayout.CENTER);
		contentPanel.add(southPanel, BorderLayout.SOUTH);
		contentPanel.setToolTipText("Redundancy can result in a smaller number of selcted nodes than the total sum");
		
		//add content
		setContentPane(contentPanel);
        pack();
		setLocationRelativeTo(null);
		setResizable(true);
		setVisible(true);
	}
	
	
	
	
	private JPanel buttonPanel(){
		
		JPanel buttonPanel = new JPanel();
		
		//create save button and register listener
		JButton saveBtn = new JButton("Save all");
		saveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outputFile = chooseFile();
				new PathwayStatistics(outputFile);
		}});
		
		//create select button and register listener
		JButton selectBtn = new JButton("Select");
		selectBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new CytoscapeNodeSelector();
		}});
		
		buttonPanel.add(saveBtn);
		buttonPanel.add(selectBtn);
		
		return buttonPanel;
	}
	
	
	
	//needed for the save output file chooser
	private File chooseFile(){ 
		File outputFile =  new File("pathwayfinder_output.tab");
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(Cytoscape.getDesktop());
        if (returnVal == JFileChooser.APPROVE_OPTION)
        	outputFile = fc.getSelectedFile();
        return outputFile;
	}
	
	//needed for filtering and updating the table
	 private void newFilter() { 
		 RowFilter<PeanutTableModelPathways, Object> rf = null;
	      //If current expression doesn't parse, don't update.
		 try {
			 String input = "(?i)" + filterText.getText();	//(?i) regex - makes input case insensitive
	         rf = RowFilter.regexFilter(input, 0);	
		 } catch (PatternSyntaxException e) {
			 return;
		 }
		 sorter.setRowFilter(rf);
	}

	 
	 
	 
	public void loadNetworkError(){
		JOptionPane.showMessageDialog(null, "You need to load a network in order to run PathwayFinder." + "\n" + "Please load a network.", "Pathway Finder warning", JOptionPane.ERROR_MESSAGE);
	}
	 
	 public void showNetworkWarning(){
		JOptionPane.showMessageDialog(null, "Child and parent network may not be the same." + "\n" + "Please reselect networks.", "Pathway Finder warning", JOptionPane.WARNING_MESSAGE);
	}
	 
	 public void showCancelError(){
		JOptionPane.showMessageDialog(null, "Pathway search canceled.", "Pathway Finder error", JOptionPane.ERROR_MESSAGE);
	}
	 
	 
	public void showRunError(){
		JOptionPane.showMessageDialog(null, "No annotated pathways present. \n Run Pathway Finder first.", "Pathway Finder error", JOptionPane.ERROR_MESSAGE);
	}
	 
	
	public void pValueError(){
		JOptionPane.showMessageDialog(null, "P-value needs to be between 0-1", "Pathway Finder error", JOptionPane.ERROR_MESSAGE);

	}
	 
}
