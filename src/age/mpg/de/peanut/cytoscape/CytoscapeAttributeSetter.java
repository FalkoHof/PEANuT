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
package age.mpg.de.peanut.cytoscape;

import java.awt.Frame;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;


import age.mpg.de.peanut.managers.TaskManagerManager;
import age.mpg.de.peanut.model.PeanutModel;
import age.mpg.de.peanut.utilityobjects.KEGGInteraction;
import age.mpg.de.peanut.utilityobjects.PathwayCommonsProtein;
import age.mpg.de.peanut.utilityobjects.databaseparsing.pathway.PathwayObject;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.logger.CyLogger;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

public class CytoscapeAttributeSetter implements Task {
	
	
	//byte variables from the constructor for decision making
	private byte database;
	public static final byte WIKIPATHWAYS = 0, CONSENSUSPATHDB = 1, PATHWAYCOMMONS = 2;

	//Cytosocape stuff
	private cytoscape.task.TaskMonitor taskMonitor;
	private CyLogger logger = CyLogger.getLogger(this.getClass());
	private CyAttributes nodeAttributes;
	private List<CyEdge>cyEdgeList;
	
	//boolean variables
	private boolean interrupted = false, isPcProtein = false, isPathway = false, isConsensusPathDb = false, importKeggInteractions;
	
	private List<PathwayObject> pathwayList = new ArrayList<PathwayObject>();
	private List<PathwayCommonsProtein> proteinList = new ArrayList<PathwayCommonsProtein>();
	private Map<String,Set<String>>conversionMap;
	private CyAttributes edgeAttributes;

	
	
	public CytoscapeAttributeSetter(byte database){	
		this.database = database;
	}
	
	
	@Override
	public void run() {
		
		//get the data needed for correct execution, dependend on the set parameters
		switch(database){
			case WIKIPATHWAYS:		pathwayList = PeanutModel.getInstance().getPathwayList(); isPathway = true;
				break;
			case CONSENSUSPATHDB:	pathwayList = PeanutModel.getInstance().getPathwayList(); isPathway = true; isConsensusPathDb = true;
				break;
			case PATHWAYCOMMONS:	proteinList = PeanutModel.getInstance().getProteinPathwayList(); isPcProtein = true;
				break;
			default:				InvalidParameterException e = new InvalidParameterException("Invalid parameter for CytoscapeAttributeSetter");
									logger.error("Error while setting pathway attributes in cytoscape", e);
									throw e;	
		}
		if (isPathway)
			conversionMap = PeanutModel.getInstance().getIdConversionMap();
		importKeggInteractions = PeanutModel.getInstance().isImportKeggInteractions();
		
		logger.info("Selected database: " + database);
		logger.info("Is Pathway: " + isPathway);
		logger.info("Is PC Protein: " + isPcProtein);
		logger.info("Is ConsensusPathDB: " + isConsensusPathDb);
		logger.info("Import KEGG Interactions: " + importKeggInteractions);
		
		nodeAttributes = Cytoscape.getNodeAttributes();		
		setAttributes();
		
	}

	//method deciding which method has to be called to set the right cytoscape attributes
	private void setAttributes(){		
		if (isPcProtein)
			setPathwayCommonsAttributes(proteinList);
		if (isPathway)
			setPathwayAttributes(pathwayList);
		if (isConsensusPathDb && importKeggInteractions)
			setKEGGInteractions();
	}
	
	
	private void setKEGGInteractions(){
		edgeAttributes = Cytoscape.getEdgeAttributes();
		cyEdgeList = new ArrayList<CyEdge>();
		List<KEGGInteraction> keggInteractionList = PeanutModel.getInstance().getKeggInteractionList();
		
		for(KEGGInteraction interaction : keggInteractionList){
			List<String>sourceList = interaction.getEntry1GeneIds();
			List<String>targetList = interaction.getEntry2GeneIds();
			
			for(String s :sourceList)
				for(String t: targetList)
					createInteractions(interaction.getPathwayName(), s,t, interaction.getInteractionType());	
		}
		
	
		Set<CyNetwork> networkSet = Cytoscape.getNetworkSet();
		
		int size = 0;
		String name = "";
		for (CyNetwork network : networkSet){
			if (network.getNodeCount() > size){
				size = network.getNodeCount();
				name = network.getIdentifier();
			}
		}
		CyNetwork network = Cytoscape.getNetwork(name);
		
		for(CyEdge edge: cyEdgeList)
			network.addEdge(edge);
				
		String message = cyEdgeList.size() + " edges added";
		logger.info(message);
		System.out.println(message);
		PeanutModel.getInstance().setNumerOfaddedEdges(cyEdgeList.size());
	}
	
	
	
	private void createInteractions(String pathwayName,String source, String target, String interactionType){
	
		Set<String> sourceSet = conversionMap.get(source);
		Set<String> targetSet = conversionMap.get(target);
		
		if (sourceSet != null && targetSet != null)
			for(String s : sourceSet)
				for(String t : targetSet)
					createOneInteraction(pathwayName,s,t,interactionType);
	}
	

	private void createOneInteraction(String pathwayName, String sourceCyID, String targetCyId, String interactionType){
		CyEdge edge = Cytoscape.getCyEdge(Cytoscape.getCyNode(sourceCyID),Cytoscape.getCyNode(targetCyId), Semantics.INTERACTION, interactionType, true, true);
		edgeAttributes.setAttribute(edge.getIdentifier(), "INTERACTION_DATA_SOURCE", "KEGG_import");
		edgeAttributes.setAttribute(edge.getIdentifier(), "PATHWAY", pathwayName);
		cyEdgeList.add(edge);
	}
	
	
	private void setPathwayAttributes(List<PathwayObject> pathwayList){
		Map<String,Set<String>>conversionMap = PeanutModel.getInstance().getIdConversionMap();
		int counter = 0;
		logger.info("Setting pathway attributes...");
		
		for(PathwayObject pathway : pathwayList){			
		
			// breaks the loop if task gets canceled
			if (interrupted){
				PeanutModel.getInstance().setExit(true);
				break;
			}
			
			counter++;
			taskMonitor.setPercentCompleted(TaskManagerManager.getInstance().getPercentage(counter, pathwayList.size()));
			
			Set<String>idSet = pathway.getIDs();
			Set<String>cyNodeIdSet = new HashSet<String>();
			//loop for setting the cytoscape attributes for each present pathway member
			for(String id :idSet){
				if (conversionMap.containsKey(id)){
					setNodeAttributes(id, conversionMap.get(id), pathway);
					cyNodeIdSet.addAll(conversionMap.get(id));
				}
			}
			pathway.setCyNodeIds(cyNodeIdSet);	
		}
		logger.info("Setting pathway attributes... - Done");
	}
	
	
	private void setNodeAttributes(String id, Set<String> cyNodeIdSet, PathwayObject pathway){
		
		for(String cyID :cyNodeIdSet){
			
			Set<String> tempURISet = new HashSet<String>();
			Set<String> tempNameSet = new HashSet<String>();

			List<String>tempURIList = new ArrayList<String>();
			List<String>tempNameList = new ArrayList<String>();

			//generating attribute to be set --> looks like this: Name:Database:ID:#Nodes
			String attribute = pathway.getPathwayURI() + "::" + id + "::" + pathway.getPathwaySize();
			tempURISet.add(attribute);
			tempNameSet.add(pathway.getPathwayName());
			
			if(nodeAttributes.hasAttribute(cyID, PeanutModel.COLUMN_TITLE_PATHWAY_IDENTIFIER_LIST))
				tempURISet.addAll(nodeAttributes.getListAttribute(cyID, PeanutModel.COLUMN_TITLE_PATHWAY_IDENTIFIER_LIST));

			if(nodeAttributes.hasAttribute(cyID, PeanutModel.COLUMN_TITLE_PATHWAY_NAME_LIST))
				tempNameSet.addAll(nodeAttributes.getListAttribute(cyID, PeanutModel.COLUMN_TITLE_PATHWAY_NAME_LIST));
			
			tempURIList.addAll(tempURISet);
			tempNameList.addAll(tempNameSet);
			nodeAttributes.setListAttribute(cyID, PeanutModel.COLUMN_TITLE_PATHWAY_IDENTIFIER_LIST,tempURIList);
			nodeAttributes.setListAttribute(cyID, PeanutModel.COLUMN_TITLE_PATHWAY_NAME_LIST,tempNameList);
			nodeAttributes.setAttribute(cyID, PeanutModel.COLUMN_TITLE_NUMBER_PATHWAYS, new Integer(tempURIList.size()));
		}
	}
	
	
	
	
	private void setPathwayCommonsAttributes(List<PathwayCommonsProtein> pathwayNamesList){
					
		Map<String,Set<String>>conversionMap = PeanutModel.getInstance().getIdConversionMap();

		int counter = 0;
		//loop over and set the results
		logger.info("Setting Pathway Commons protein attributes...");
		for(PathwayCommonsProtein prot: pathwayNamesList){
			Set<String> cyIDSet = conversionMap.get(prot.getId());
			// set status of the cytoscape taskmanager
			taskMonitor.setPercentCompleted(TaskManagerManager.getInstance().getPercentage(counter, pathwayNamesList.size()));
			counter++;
			
			// breaks the loop if task gets canceled
			if(interrupted){
				PeanutModel.getInstance().setExit(true);
				break;
			}
			
			//get the already present attributes, add the new ones and write the new attributes
			for(String cyID : cyIDSet){
				List<String>tempURIList = new ArrayList<String>();
				Set<String>tempURISet = new HashSet<String>();
				tempURISet.addAll(prot.getPathwayURIList());
				
				List<String>tempNameList = new ArrayList<String>();
				Set<String>tempNameSet = new HashSet<String>();
				tempNameSet.addAll(prot.getPathwayNameList());
				
				
				if (nodeAttributes.hasAttribute(cyID, PeanutModel.COLUMN_TITLE_PATHWAY_IDENTIFIER_LIST))
					tempURISet.addAll(nodeAttributes.getListAttribute(cyID, PeanutModel.COLUMN_TITLE_PATHWAY_IDENTIFIER_LIST));
				
				if (nodeAttributes.hasAttribute(cyID, PeanutModel.COLUMN_TITLE_PATHWAY_NAME_LIST))
					tempNameSet.addAll(nodeAttributes.getListAttribute(cyID, PeanutModel.COLUMN_TITLE_PATHWAY_NAME_LIST));
		
				
				tempURIList.addAll(tempURISet);	
				tempNameList.addAll(tempNameSet);
				nodeAttributes.setListAttribute(cyID, PeanutModel.COLUMN_TITLE_PATHWAY_IDENTIFIER_LIST,tempURIList);
				nodeAttributes.setListAttribute(cyID, PeanutModel.COLUMN_TITLE_PATHWAY_NAME_LIST,tempNameList);
				nodeAttributes.setAttribute(cyID, PeanutModel.COLUMN_TITLE_NUMBER_PATHWAYS, new Integer(tempURIList.size()));	
			}
		}
		logger.info("Setting Pathway Commons protein attributes... - done");
	}
	
	
	
	/*	
	public void setPathwayCommonsInteractions(List<PathwayCommonsInteraction> interactionList){
		
		CytoscapeAttributeGetter cyGet = new CytoscapeAttributeGetter();
		Map<String,String>conversionMap = cyGet.getUniprotCyIDMap();
		
		for (PathwayCommonsInteraction edge : interactionList){
			
			String cyIDNodeA = conversionMap.get(edge.getSource());
			String cyIDNodeB = conversionMap.get(edge.getTarget());
			String attribute = edge.getInteractionType();		
			
			CyNode sourceNode = Cytoscape.getCyNode(cyIDNodeA);
			CyNode targetNode = Cytoscape.getCyNode(cyIDNodeB);

			CyEdge newCyEdge;
			
			System.out.println("cyIDNodeA " + cyIDNodeA + "\t" + "cyIDNodeB " + cyIDNodeB + "\t" + "Interaction Type " + attribute);
			
			Set<String> undirectedInteractions = new HashSet<String>();
			undirectedInteractions.add("CO_CONTROL");
			undirectedInteractions.add("INTERACTS_WITH");
			undirectedInteractions.add("IN_SAME_COMPONENT");
			undirectedInteractions.add("REACTS_WITH");
			
			
			if (!undirectedInteractions.contains(attribute)){			
				newCyEdge = Cytoscape.getCyEdge(sourceNode, targetNode, Semantics.INTERACTION, attribute, true, true);
			}
			else{
				newCyEdge = Cytoscape.getCyEdge(sourceNode, targetNode, Semantics.INTERACTION, attribute, true);
			}
			
				Cytoscape.getCurrentNetwork().addEdge(newCyEdge);
		}
		
	}*/


	@Override
	public String getTitle() {
		return "Pathway Finder - Attribute Setter";
	}

	@Override
	public void halt() {
		logger.warning("canceled");
		this.interrupted = true;
	}
	
	@Override
	public void setTaskMonitor(TaskMonitor monitor) throws IllegalThreadStateException {
		taskMonitor = monitor;
	}
	

}
