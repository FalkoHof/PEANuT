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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.layout.GroupLayout;
import org.jdesktop.layout.LayoutStyle;

import age.mpg.de.peanut.Peanut;
import age.mpg.de.peanut.model.PeanutModel;
import age.mpg.de.peanut.utilityobjects.PluginProperties;

import cytoscape.Cytoscape;
import cytoscape.logger.CyLogger;

public class StartDialogues extends JDialog {
	
	//frame for the menu
	private static	String titel = PluginProperties.getInstance().getPluginName() + " - data import";	
	
	private JTabbedPane tabContainer;
	
	private static final String[] DATABASES = PluginProperties.getInstance().getDatabases();
    private String[] wpOrganisms = PluginProperties.getInstance().getWpOrganisms();
    private String[] consensusPathDBOrganisms = PluginProperties.getInstance().getConsensusPathDBOrganisms();
    private String[] cyAttributesHeaders;
    
    private static final int CONSENSUS_PATH_DB = 0;
    private static final int PATHWAY_COMMONS = 1;
    private static final int WIKI_PATHWAYS = 2;
    
    private final String organism = "Organism:";
    private final String uniprot = "UniProt ID:";

	private CyLogger logger = CyLogger.getLogger(this.getClass());
	
	//constructor
	public StartDialogues(JFrame owner){
		
		super(owner,titel, true);
		cyAttributesHeaders = Cytoscape.getNodeAttributes().getAttributeNames();
		tabContainer = new JTabbedPane();
		logger.info("constructor called");
		
		if (Cytoscape.getNetworkSet().size() == 0){
			loadNetworkError();
			dispose();
		}
		else{
			logger.info("Calling method - showDialogue()");
			//if the search has been canceled earlier, reset the boolean to false to allow the programm to run again
			PeanutModel.getInstance().setExit(false);
			showDialogue();
		}
	}
	
    public void showDialogue(){
    	
    	
		logger.info("showDialogue(): adding tabs to JTabbedPane");
		tabContainer.addTab(DATABASES[CONSENSUS_PATH_DB],consensusPathDBPanel());
    	tabContainer.addTab(DATABASES[PATHWAY_COMMONS],pathwayCommonsPanel());
    	tabContainer.addTab(DATABASES[WIKI_PATHWAYS],wikiPathwaysPanel());
    	tabContainer.setSelectedIndex(0);
		logger.info("showDialogue(): tabs added");

    	PeanutModel.getInstance().setDatabase(DATABASES[0]);	
    	//makes sure the model default values are set to the ones of the tab displayed;
    	consensusPathDBPanel();
		logger.info("showDialogue(): add buttons");
		logger.info("showDialogue(): buttons added");
    	
    	tabContainer.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
            	PeanutModel.getInstance().setDatabase(DATABASES[tabContainer.getSelectedIndex()]);	
         }});

    	JPanel contentPanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(contentPanel, BoxLayout.Y_AXIS);
	    contentPanel.setLayout(boxLayout);
	    contentPanel.add(tabContainer);
		contentPanel.add(Box.createVerticalGlue());
	    contentPanel.add(addButtons());
        setContentPane(contentPanel);
    	
    	logger.info("showDialogue(): packing container");
        
    	pack();
        setLocationRelativeTo(null);
        setResizable(false);
		logger.info("showDialogue(): setting dialogues visible");
        setVisible(true);
      }
       
    
    public JPanel addButtons(){
    	
    	JPanel buttonPanel = new JPanel();
    	
		logger.info("addButtons(): Creating & registering buttons...");
    	
		
		
		JButton startBttn = new JButton("Start"); 
		JButton cancelBttn = new JButton("Cancel");

		
 	    startBttn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				try {
					new Peanut().findPathways();
					String message = "Done - Network annotated with " + PeanutModel.getInstance().getDatabase() + " pathways.";
					if (PeanutModel.getInstance().isImportKeggInteractions())
						message = message + "\nInteractions imported from KEGG: " + PeanutModel.getInstance().getNumerOfaddedEdges();
					JOptionPane.showMessageDialog(null, message,"Annotation Results", JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				dispose();
		}});
		
		cancelBttn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
		}});
		
		buttonPanel.add(startBttn);
		buttonPanel.add(cancelBttn);
		return buttonPanel;
    }
    
    
    public JPanel wikiPathwaysPanel(){
    	
        // create all the panels/sub panels
		logger.info("wikiPathwaysPanel(): Creating wikiPathwaysPanel...");
    	JPanel wikiPathwaysPanel = new JPanel();
    	
        JPanel wikiPathwaysOrganismPanel = new JPanel();
    	wikiPathwaysOrganismPanel.setBorder(BorderFactory.createTitledBorder("1 - Select organism"));
    	
    	JPanel wikiPathwaysIdPanel = new JPanel();
    	wikiPathwaysIdPanel.setBorder(BorderFactory.createTitledBorder("2 - Select identifier"));
		    	
    	// create the objects contained in this JPlanel
    	JLabel wikipathwaysOrganismLable = new JLabel(organism);
        JLabel uniProtIdLable = new JLabel(uniprot);
        
        JComboBox wikiPathwaysOrganismComboBx = new JComboBox(wpOrganisms);
        JComboBox wikiPathwaysIdComboBx = new JComboBox(cyAttributesHeaders);
    	
        
        // add combobox listeners
        wikiPathwaysOrganismComboBx.setSelectedIndex(0);
		PeanutModel.getInstance().setOrganism((String) wikiPathwaysOrganismComboBx.getSelectedItem());
        wikiPathwaysOrganismComboBx.addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent evt){
        		PeanutModel.getInstance().setOrganism((String)evt.getItem());
        }});
        
        
        wikiPathwaysIdComboBx.setSelectedIndex(0);
        PeanutModel.getInstance().setColumnId((String) wikiPathwaysIdComboBx.getSelectedItem());        
        wikiPathwaysIdComboBx.addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent evt){
        		PeanutModel.getInstance().setColumnId((String)evt.getItem());
        }});
    	
        
        
		logger.info("wikiPathwaysPanel(): Layouting wikiPathwaysPanel...");
    	//create layouts - layout has been designed with netbeans
        // layout for the organism panel
        GroupLayout wikiPathwaysOrganismPanelLayout = new GroupLayout(wikiPathwaysOrganismPanel);
        wikiPathwaysOrganismPanel.setLayout(wikiPathwaysOrganismPanelLayout);
        wikiPathwaysOrganismPanelLayout.setHorizontalGroup(
            wikiPathwaysOrganismPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(wikiPathwaysOrganismPanelLayout.createSequentialGroup()
                .add(25, 25, 25)
                .add(wikipathwaysOrganismLable)
                .add(46, 46, 46)
                .add(wikiPathwaysOrganismComboBx, 0, 253, Short.MAX_VALUE)
                .addContainerGap())
        );
        wikiPathwaysOrganismPanelLayout.setVerticalGroup(
            wikiPathwaysOrganismPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(wikiPathwaysOrganismPanelLayout.createSequentialGroup()
                .add(27, 27, 27)
                .add(wikiPathwaysOrganismPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(wikipathwaysOrganismLable)
                    .add(wikiPathwaysOrganismComboBx, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(37, Short.MAX_VALUE))
        );
        // layout for the id panel
        GroupLayout wikiPathwaysIdPanelLayout = new GroupLayout(wikiPathwaysIdPanel);
        wikiPathwaysIdPanel.setLayout(wikiPathwaysIdPanelLayout);
        wikiPathwaysIdPanelLayout.setHorizontalGroup(
            wikiPathwaysIdPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(wikiPathwaysIdPanelLayout.createSequentialGroup()
                .add(25, 25, 25)
                .add(uniProtIdLable)
                .add(46, 46, 46)
                .add(wikiPathwaysIdComboBx, 0, 253, Short.MAX_VALUE)
                .addContainerGap())
        );
        wikiPathwaysIdPanelLayout.setVerticalGroup(
            wikiPathwaysIdPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(wikiPathwaysIdPanelLayout.createSequentialGroup()
                .add(32, 32, 32)
                .add(wikiPathwaysIdPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(wikiPathwaysIdComboBx, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(uniProtIdLable))
                .addContainerGap(33, Short.MAX_VALUE))
        );
        //layout for the super panel
        GroupLayout wikiPathwaysPanelLayout = new GroupLayout(wikiPathwaysPanel);
        wikiPathwaysPanel.setLayout(wikiPathwaysPanelLayout);
        wikiPathwaysPanelLayout.setHorizontalGroup(
            wikiPathwaysPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(wikiPathwaysPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(wikiPathwaysPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(wikiPathwaysOrganismPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(wikiPathwaysIdPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        wikiPathwaysPanelLayout.setVerticalGroup(
            wikiPathwaysPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(wikiPathwaysPanelLayout.createSequentialGroup()
                .add(26, 26, 26)
                .add(wikiPathwaysOrganismPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .add(35, 35, 35)
                .add(wikiPathwaysIdPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(42, Short.MAX_VALUE))
        );
    	
        //return the created panel
    	logger.info("wikiPathwaysPanel(): Creating wikiPathwaysPanel - Done");
    	return wikiPathwaysPanel;
    }
    
    
    public JPanel pathwayCommonsPanel(){
    	
		logger.info("pathwayCommonsPanel(): Creating pathwayCommonsPanel...");

        // create all the panels/sub panels
    	JPanel pathwayCommonsPanel = new JPanel();

    	JPanel pathwayCommonsOptionsPanel = new JPanel();
    	pathwayCommonsOptionsPanel.setBorder(BorderFactory.createTitledBorder("1 - Import options"));
    	
    	JPanel pathwayCommonsIdPanel = new JPanel();
        pathwayCommonsIdPanel.setBorder(BorderFactory.createTitledBorder("2 - Select identifier"));
    	
		logger.info("pathwayCommonsPanel(): Creating obects within the JPanel...");

    	// create the other objects held by this panel
    	JCheckBox pcCellMapCBx = new JCheckBox("Cell Map", true);
    	JCheckBox pcHumanCycCBx = new JCheckBox("Human CYC", true);
    	JCheckBox pcReactomeCBx = new JCheckBox("Reactome", true);
    	JCheckBox pcNCINatureCBx = new JCheckBox("NCI Nature", true);
    	JCheckBox pcIMIDCBx = new JCheckBox("IMID",true);
    	
        JLabel pcCommonsDatabaseLable = new JLabel("Select databases:");
        JLabel uniprotLable = new JLabel(uniprot);

        JComboBox pcIdComboBx = new JComboBox(cyAttributesHeaders);
        
    	// register action listeners
        pcCellMapCBx.addItemListener(new ItemListener(){ 
	    	public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED)
	    			PeanutModel.getInstance().setPcCellMap(true);
	     		else
	     			PeanutModel.getInstance().setPcCellMap(false);
	    }});
        
        pcHumanCycCBx.addItemListener(new ItemListener(){ 
	    	public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED)
	    			PeanutModel.getInstance().setPcHumanCyc(true);
	     		else
	     			PeanutModel.getInstance().setPcHumanCyc(false);
	    }});
        
        pcReactomeCBx.addItemListener(new ItemListener(){ 
	    	public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED)
	    			PeanutModel.getInstance().setPcReactome(true);
	     		else
	     			PeanutModel.getInstance().setPcReactome(false);
	    }});
                
        pcNCINatureCBx.addItemListener(new ItemListener(){ 
	    	public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED)
	    			PeanutModel.getInstance().setPcNCINature(true);
	     		else
	     			PeanutModel.getInstance().setPcNCINature(false);	
	    }});
        
        pcIMIDCBx.addItemListener(new ItemListener(){ 
	    	public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED)
	    			PeanutModel.getInstance().setPcIMID(true);
	     		else
	     			PeanutModel.getInstance().setPcIMID(false);	
	    }});
        
        pcIdComboBx.setSelectedIndex(0);
        PeanutModel.getInstance().setColumnId((String) pcIdComboBx.getSelectedItem());
        pcIdComboBx.addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent evt){
        		PeanutModel.getInstance().setColumnId((String)evt.getItem());
        }});
       
        logger.info("pathwayCommonsPanel(): Layouting pathwayCommonsPanel...");
        GroupLayout pathwayCommonsOptionsPanelLayout = new GroupLayout(pathwayCommonsOptionsPanel);
        pathwayCommonsOptionsPanel.setLayout(pathwayCommonsOptionsPanelLayout);
        pathwayCommonsOptionsPanelLayout.setHorizontalGroup(
    	pathwayCommonsOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pathwayCommonsOptionsPanelLayout.createSequentialGroup()
                .add(30, 30, 30)
                .add(pathwayCommonsOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pcCommonsDatabaseLable)
                    .add(pathwayCommonsOptionsPanelLayout.createSequentialGroup()
                        .add(pathwayCommonsOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(pcHumanCycCBx)
                            .add(pcCellMapCBx))
                        .add(18, 18, 18)
                        .add(pathwayCommonsOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(pcNCINatureCBx)
                            .add(pathwayCommonsOptionsPanelLayout.createSequentialGroup()
                                .add(pcReactomeCBx)
                                .add(18, 18, 18)
                                .add(pcIMIDCBx)))))
                .addContainerGap(29, Short.MAX_VALUE))
        );
       pathwayCommonsOptionsPanelLayout.setVerticalGroup(
    		   pathwayCommonsOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pathwayCommonsOptionsPanelLayout.createSequentialGroup()
                .addContainerGap(26, Short.MAX_VALUE)
                .add(pcCommonsDatabaseLable)
                .add(18, 18, 18)
                .add(pathwayCommonsOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(pcCellMapCBx)
                    .add(pcReactomeCBx)
                    .add(pcIMIDCBx))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(pathwayCommonsOptionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(pcHumanCycCBx)
                    .add(pcNCINatureCBx))
                .add(26, 26, 26))
        );
                
        // layout for the id panel
        GroupLayout pathwayCommonsIdPanelLayout = new GroupLayout(pathwayCommonsIdPanel);
        pathwayCommonsIdPanel.setLayout(pathwayCommonsIdPanelLayout);
        pathwayCommonsIdPanelLayout.setHorizontalGroup(
            pathwayCommonsIdPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(pathwayCommonsIdPanelLayout.createSequentialGroup()
                .add(28, 28, 28)
                .add(uniprotLable)
                .add(46, 46, 46)
                .add(pcIdComboBx, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pathwayCommonsIdPanelLayout.setVerticalGroup(
            pathwayCommonsIdPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(pathwayCommonsIdPanelLayout.createSequentialGroup()
                .add(18, 18, 18)
                .add(pathwayCommonsIdPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(pcIdComboBx, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(uniprotLable))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        GroupLayout pathwayCommonsPanelLayout = new GroupLayout(pathwayCommonsPanel);
        pathwayCommonsPanel.setLayout(pathwayCommonsPanelLayout);
        pathwayCommonsPanelLayout.setHorizontalGroup(
            pathwayCommonsPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(pathwayCommonsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(pathwayCommonsPanelLayout.createParallelGroup(GroupLayout.LEADING)
                    .add(pathwayCommonsOptionsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(pathwayCommonsIdPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pathwayCommonsPanelLayout.setVerticalGroup(
            pathwayCommonsPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, pathwayCommonsPanelLayout.createSequentialGroup()
                .add(26, 26, 26)
                .add(pathwayCommonsOptionsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(pathwayCommonsIdPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .add(26, 26, 26))
        );
        
        logger.info("pathwayCommonsPanel(): Layouting pathwayCommonsPanel - Done");
        
        // return the pathway commons panel
		logger.info("pathwayCommonsPanel(): Creating pathwayCommonsPanel - Done");
        return pathwayCommonsPanel;
    }
    
    
    
    public JPanel consensusPathDBPanel(){   

    	logger.info("consensusPathDBPanel(): Creating consensusPathDBPanel...");
        // create all the panels/sub panels
		
		JPanel consensusPathDBPanel = new JPanel();

    	JPanel consensusPathDBOrganismPanel  = new JPanel();
        consensusPathDBOrganismPanel.setBorder(BorderFactory.createTitledBorder("1 - Select organism"));
        
        JPanel consenesusPathDBOptionsPanel = new JPanel();
        consenesusPathDBOptionsPanel.setBorder(BorderFactory.createTitledBorder("2 - Import options"));
        
    	JPanel consensusPathDBIdPanel = new JPanel();
        consensusPathDBIdPanel.setBorder(BorderFactory.createTitledBorder("3 - Select identifier"));
        
        // create the other objects held by this panel
		logger.info("consensusPathDBPanel(): Creating obects within the JPanel...");
        JComboBox consensusPathDBOrganismComboBx = new JComboBox(consensusPathDBOrganisms);
        consensusPathDBOrganismComboBx.setSelectedIndex(0);
        PeanutModel.getInstance().setOrganism((String) consensusPathDBOrganismComboBx.getSelectedItem());
        
        consensusPathDBOrganismComboBx.addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent evt){
        		PeanutModel.getInstance().setOrganism((String)evt.getItem());
        }});
        
        
        JComboBox consensusPathDBIDComboBx = new JComboBox(cyAttributesHeaders);
        consensusPathDBIDComboBx.setSelectedIndex(0);
        PeanutModel.getInstance().setColumnId((String) consensusPathDBIDComboBx.getSelectedItem());
        
        consensusPathDBIDComboBx.addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent evt){
        		PeanutModel.getInstance().setColumnId((String)evt.getItem());
        }});
        
        JCheckBox keggCheckBx = new JCheckBox("KEGG interactions");
        keggCheckBx.setSelected(PeanutModel.getInstance().isImportKeggInteractions());
      
        keggCheckBx.addItemListener(new ItemListener(){ 
	    	public void itemStateChanged(ItemEvent e) {
	     		if (e.getStateChange() == ItemEvent.SELECTED)
	     			PeanutModel.getInstance().setImportKeggInteractions(true);
	     		else
	     			PeanutModel.getInstance().setImportKeggInteractions(false);
	    	}});
        
        JLabel organismLabel = new JLabel(organism);
        JLabel consensusPathDBIDLable = new JLabel("Entrez Gene ID:");

    	//create layouts - layout has been designed with netbeans
        // layout for the organism panel
		logger.info("consensusPathDBPanel(): Layouting...");

        GroupLayout consensusPathDBOrganismPanelLayout = new GroupLayout(consensusPathDBOrganismPanel);
        consensusPathDBOrganismPanel.setLayout(consensusPathDBOrganismPanelLayout);
        consensusPathDBOrganismPanelLayout.setHorizontalGroup(
            consensusPathDBOrganismPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(consensusPathDBOrganismPanelLayout.createSequentialGroup()
                .add(22, 22, 22)
                .add(organismLabel)
                .addPreferredGap(LayoutStyle.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(consensusPathDBOrganismComboBx, GroupLayout.PREFERRED_SIZE, 214, GroupLayout.PREFERRED_SIZE)
                .add(17, 17, 17))
        );
        consensusPathDBOrganismPanelLayout.setVerticalGroup(
            consensusPathDBOrganismPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(consensusPathDBOrganismPanelLayout.createSequentialGroup()
                .add(22, 22, 22)
                .add(consensusPathDBOrganismPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(organismLabel)
                    .add(consensusPathDBOrganismComboBx, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(32, Short.MAX_VALUE))
        );

        // layout for the option panel
        GroupLayout consenesusPathDBOptionsPanelLayout = new GroupLayout(consenesusPathDBOptionsPanel);
        consenesusPathDBOptionsPanel.setLayout(consenesusPathDBOptionsPanelLayout);
        consenesusPathDBOptionsPanelLayout.setHorizontalGroup(
            consenesusPathDBOptionsPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(consenesusPathDBOptionsPanelLayout.createSequentialGroup()
                .add(17, 17, 17)
                .add(keggCheckBx)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        consenesusPathDBOptionsPanelLayout.setVerticalGroup(
            consenesusPathDBOptionsPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(consenesusPathDBOptionsPanelLayout.createSequentialGroup()
                .add(15, 15, 15)
                .add(keggCheckBx)
                .addContainerGap(18, Short.MAX_VALUE))
        );
        
        // layout for the id panel
        GroupLayout consensusPathDBIdPanelLayout = new GroupLayout(consensusPathDBIdPanel);
        consensusPathDBIdPanel.setLayout(consensusPathDBIdPanelLayout);
        consensusPathDBIdPanelLayout.setHorizontalGroup(
            consensusPathDBIdPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(consensusPathDBIdPanelLayout.createSequentialGroup()
                .add(28, 28, 28)
                .add(consensusPathDBIDLable)
                .add(46, 46, 46)
                .add(consensusPathDBIDComboBx, 0, 225, Short.MAX_VALUE)
                .addContainerGap())
        );
        consensusPathDBIdPanelLayout.setVerticalGroup(
            consensusPathDBIdPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(consensusPathDBIdPanelLayout.createSequentialGroup()
                .add(18, 18, 18)
                .add(consensusPathDBIdPanelLayout.createParallelGroup(GroupLayout.BASELINE)
                    .add(consensusPathDBIDComboBx, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .add(consensusPathDBIDLable))
                .addContainerGap(27, Short.MAX_VALUE))
        );
        
        // layout for the super panel
        GroupLayout consensusPathDBPanelLayout = new GroupLayout(consensusPathDBPanel);
        consensusPathDBPanel.setLayout(consensusPathDBPanelLayout);
        consensusPathDBPanelLayout.setHorizontalGroup(
            consensusPathDBPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, consensusPathDBPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(consensusPathDBPanelLayout.createParallelGroup(GroupLayout.TRAILING)
                    .add(consensusPathDBOrganismPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, consensusPathDBIdPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(GroupLayout.LEADING, consenesusPathDBOptionsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        consensusPathDBPanelLayout.setVerticalGroup(
            consensusPathDBPanelLayout.createParallelGroup(GroupLayout.LEADING)
            .add(GroupLayout.TRAILING, consensusPathDBPanelLayout.createSequentialGroup()
                .addContainerGap(22, Short.MAX_VALUE)
                .add(consensusPathDBOrganismPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(consenesusPathDBOptionsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.RELATED)
                .add(consensusPathDBIdPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .add(15, 15, 15))
        );
        
    	//return the consensus path db panel
    	logger.info("consensusPathDBPanel(): Creating consensusPathDBPanel - Done");
    	return consensusPathDBPanel;
    }	
	
	public void loadNetworkError(){
		logger.warn("JOption pane - No network present error displayed");
		JOptionPane.showMessageDialog(null, "You need to load a network in order to run PathwayFinder." + "\n" + "Please load a network.", "Pathway Finder warning", JOptionPane.ERROR_MESSAGE);
	}
}
