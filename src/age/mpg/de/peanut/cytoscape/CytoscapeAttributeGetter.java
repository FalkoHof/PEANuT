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

import giny.model.Node;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import age.mpg.de.peanut.model.PeanutModel;
import age.mpg.de.peanut.utilityobjects.cytoscapeparsing.pathway.PathwayObjectStatistics;

import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.logger.CyLogger;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

//class for getting attributes of the cytoscape networks
public class CytoscapeAttributeGetter implements Task{
	
	private cytoscape.task.TaskMonitor taskMonitor;
	private CyLogger logger = CyLogger.getLogger(this.getClass());
	private boolean interrupted = false;
	
	//parameters for the class, needed for execution of the right methods
	public static final byte PATHWAYCOMMONS = 0, WIKIPATHWAYS = 1, CONSENSUSPATHDB = 2, STATISTICS = 3;
	
	private final int WP_LENGTH = 5, CPDB_LENGTH = 4, PC_LENGTH = 3;
	
	//variables that tell the class what to do
	private byte selectedOption;
	private String attributeColumn;
	private Map<String, PathwayObjectStatistics> pathwayDistributionMap;
	
	
	private CyAttributes nodeAttributes;
	private List<CyNode> cyNodeList;
	
	public CytoscapeAttributeGetter(byte selectedOption, String attributeColumn){
		this.selectedOption = selectedOption;	
		this.attributeColumn = attributeColumn;
	}
		
	//alternative constructor, called for getting the annotated pathway data for statistics
	public CytoscapeAttributeGetter(){
		cyNodeList = (List<CyNode>) Cytoscape.getCyNodesList();
		nodeAttributes = Cytoscape.getNodeAttributes();
	}
	
	
	@Override
	public void run(){
		
		nodeAttributes = Cytoscape.getNodeAttributes();
		cyNodeList = (List<CyNode>) Cytoscape.getCyNodesList();

		//checks if the attribute to be called is present
		if (checkIfPathwayAttributeIsPresent(attributeColumn)){
			switch (selectedOption){
			case PATHWAYCOMMONS:	parseNetworkAttributes();
				break;
			case WIKIPATHWAYS:		parseNetworkAttributes();
				break;
			case CONSENSUSPATHDB:	parseNetworkAttributes();
				break;
			case STATISTICS:	getPathwayDistributions();	
				break;
			default:			InvalidParameterException e = new InvalidParameterException("Invalid parameter for CytoscapeAttributeGetter");
								logger.error("Error while getting pathway attributes from cytoscape", e);
								throw e;
			}
		}
		else{
			InvalidParameterException e = new InvalidParameterException("Invalid attributeColumn parameter for CytoscapeAttributeGetter");
			logger.error("Error while getting " + attributeColumn + " attributes from cytoscape", e);
			throw e;
		}
		
		
	}
	
	public boolean checkIfPathwayAttributeIsPresent(String attributeColumn){
		
		for (CyNode node : cyNodeList){
			if (nodeAttributes.hasAttribute(node.getIdentifier(), attributeColumn)){
				//TODO check if this is neccessary at that point
				//set stuff in the model
				PeanutModel.getInstance().setFinished(true);
				return true;
			}
		}
		return false;
	}
	
	
	
	
	//method hat generates a hashmap containg the name and the member of a pathway for a parent and a child network
	private void getPathwayDistributions(){
		
		pathwayDistributionMap = new HashMap<String,PathwayObjectStatistics>();
		
		String child = PeanutModel.getInstance().getChildNetwork();
		String parent = PeanutModel.getInstance().getParentNetwork();
		
		CyNetwork childNetwork = Cytoscape.getNetwork(child);		
		CyNetwork parentNetwork = Cytoscape.getNetwork(parent);
		CyNetwork[] networkArray = {childNetwork, parentNetwork}; 
		
		int childSize = childNetwork.getNodeCount();
		int parentSize = parentNetwork.getNodeCount();
		
		PeanutModel.getInstance().setChildSize(childSize);
		PeanutModel.getInstance().setParentSize(parentSize);
				
		int[] childNodeIndexArray = new int[Cytoscape.getNetwork(childNetwork.getIdentifier()).getNodeIndicesArray().length];
		childNodeIndexArray = Cytoscape.getNetwork(childNetwork.getIdentifier()).getNodeIndicesArray();	
		int[] parentNodeIndexArray = new int[Cytoscape.getNetwork(parentNetwork.getIdentifier()).getNodeIndicesArray().length];	
		parentNodeIndexArray = Cytoscape.getNetwork(parentNetwork.getIdentifier()).getNodeIndicesArray();
		
		// array to hold the node indices arrays of the parent network and the child network
		int[][] nodeIndexArrayArray= {childNodeIndexArray,parentNodeIndexArray};
		
		// loop over the array holding the indices arrays
		for(int i = 0; i<nodeIndexArrayArray.length;i++){
			//loop over one index array
			if(interrupted){
				PeanutModel.getInstance().setExit(true);
				break;
			}			
			for(int b : nodeIndexArrayArray[i]){
				Node node = networkArray[i].getNode(b);
				
				if(interrupted){
					PeanutModel.getInstance().setExit(true);
					break;
				}
				// get the list of pathways of a node node
				List<String> pathwayList = (List<String>) nodeAttributes.getListAttribute(node.getIdentifier(), PeanutModel.COLUMN_TITLE_PATHWAY_IDENTIFIER_LIST);
				// check if node is in pathways and has the pathway attribute
				if(pathwayList.size() > 0 && pathwayList != null){
					processNodeAttributes(pathwayList, node.getIdentifier(), i);
				}
			}
		}
		//set stuff in the model
		PeanutModel.getInstance().setPathwayDistributionMap(pathwayDistributionMap);
	}
	
	//method that detects whether the read in attribute comes from wikipathways, consensuspathdb or pathway commons
	private void processNodeAttributes(List<String> attributeList, String nodeIdentifier, int network){

		for(String attribute : attributeList){
			String[] temp = attribute.split(PeanutModel.GENERAL_DELIMITER);
			
			if(temp.length == WP_LENGTH)
				wpAttributeToPathway(temp, nodeIdentifier, network);
			if(temp.length == CPDB_LENGTH)
				cpdbAttributeToPathway(temp, nodeIdentifier, network);
			else
				pcAttributeToPathway(temp, nodeIdentifier, network);	
		}
	}
	
	
	//method for processing wikipathway attributes
	private void wpAttributeToPathway(String[] attributeArr, String nodeIdentifier, int network){
		PathwayObjectStatistics path = null;
		
		String pathwayName = attributeArr[0];
		String database = attributeArr[1];
		String wikipathwaysID = attributeArr[2];
		String id = attributeArr[3];
		String pathwaySize = attributeArr[4];
		String pathwayURI = attributeArr[0] + PeanutModel.GENERAL_DELIMITER  + attributeArr[1];
	

		if (pathwayDistributionMap.containsKey(pathwayURI))
			path = pathwayDistributionMap.get(pathwayURI);
		else if (network == PathwayObjectStatistics.CHILD_NETWORK) //only add new pathways to the map if they are present in the small child network
			path = new PathwayObjectStatistics(pathwayName, database, Integer.parseInt(pathwaySize));
		
		if (path != null){
			path.addBioId(id, network);
			path.addCytoscapeNodeId(nodeIdentifier, network);
			pathwayDistributionMap.put(pathwayURI, path);
		}
	}
	
	
	//method for processing consensuspathdb attributes
	private void cpdbAttributeToPathway(String[] attributeArr, String nodeIdentifier, int network){
		
		PathwayObjectStatistics path = null;
		
		String pathwayName = attributeArr[0];
		String database = attributeArr[1];
		String id = attributeArr[2];
		String pathwaySize = attributeArr[3];
		String pathwayURI = attributeArr[0] + PeanutModel.GENERAL_DELIMITER  + attributeArr[1];
	

		if (pathwayDistributionMap.containsKey(pathwayURI))
			path = pathwayDistributionMap.get(pathwayURI);
		else if (network == PathwayObjectStatistics.CHILD_NETWORK) //only add new pathways to the map if they are present in the small child network
			path = new PathwayObjectStatistics(pathwayName, database, Integer.parseInt(pathwaySize));
		
		if (path != null){
			path.addBioId(id, network);
			path.addCytoscapeNodeId(nodeIdentifier, network);
			pathwayDistributionMap.put(pathwayURI, path);
		}
	}
	
	//method for processing pathway commons attributes
	private void pcAttributeToPathway(String[] attributeArr, String nodeIdentifier, int network){

		PathwayObjectStatistics path = null;
		
		String pathwayName = attributeArr[0];
		String database = attributeArr[1];
		String id = attributeArr[2];
		String pathwayURI = attributeArr[0] + PeanutModel.GENERAL_DELIMITER  + attributeArr[1];
	
		if (pathwayDistributionMap.containsKey(pathwayURI))
			path = pathwayDistributionMap.get(pathwayURI);
		else if (network == PathwayObjectStatistics.CHILD_NETWORK)
			path = new PathwayObjectStatistics(pathwayName, database);
		
		if (path != null){
			path.addBioId(id, network);
			path.addCytoscapeNodeId(nodeIdentifier, network);
			pathwayDistributionMap.put(pathwayURI, path);
		}
	}
	
	
	
	//method for parsing specific cytoscape attributes (attributeColumn) and creating an cyID to biological Id conversion map
	private void parseNetworkAttributes(){
		
		byte attributeType = nodeAttributes.getType(attributeColumn);
		Map<String,Set<String>>bioIdToCyIDMap = new HashMap<String,Set<String>>();
		List<String> uniprotIdList = new ArrayList<String>();
			
		for (CyNode node : cyNodeList){
			if (interrupted){
				PeanutModel.getInstance().setExit(true);
				break;
			}
			List<String> tempList = new ArrayList<String>();
			
			if (attributeType == CyAttributes.TYPE_SIMPLE_LIST)
				tempList = getListAttribute(node,attributeColumn);
			if (attributeType == CyAttributes.TYPE_STRING)
				tempList.add(getStringAttribute(node,attributeColumn));
			if (attributeType == CyAttributes.TYPE_INTEGER)
				tempList.add(getIntAsStringAttribute(node,attributeColumn));
			
			for (String id : tempList){
				//skipp ids that are"NOT_SPECIFIED" or null
				if (!id.equals("NOT_SPECIFIED") || id != null){
					if (selectedOption == PATHWAYCOMMONS)
						uniprotIdList.add(id);
					Set<String>cyIDSet = new HashSet<String>();
					
					if (bioIdToCyIDMap.containsKey(id))
						cyIDSet.addAll(bioIdToCyIDMap.get(id));
					cyIDSet.add(node.getIdentifier());
					bioIdToCyIDMap.put(id, cyIDSet);
				}
			}
		}
		//set stuff in the model so that other classes can access it
		PeanutModel.getInstance().setIdConversionMap(bioIdToCyIDMap);
		if (selectedOption == PATHWAYCOMMONS)
			PeanutModel.getInstance().setWholeNetworkIdList(uniprotIdList);
			
	}
	
	//mehtods for getting different attributes as List or string
	private List<String> getListAttribute(CyNode node, String attributeColumn){
		List<String> tempList = (List<String>) nodeAttributes.getListAttribute(node.getIdentifier(),attributeColumn);
		return tempList;
	}
	
	
	private String getStringAttribute(CyNode node, String attributeColumn){
		String str = nodeAttributes.getStringAttribute(node.getIdentifier(),attributeColumn);
		return str;
	}
	
	
	private String getIntAsStringAttribute(CyNode node, String attributeColumn){
		String str = nodeAttributes.getIntegerAttribute(node.getIdentifier(),attributeColumn) + "";
		return str;
	}
	
	
	//Cytoscape TaskMonitor Stuff
	@Override 
	public void setTaskMonitor(TaskMonitor monitor) throws IllegalThreadStateException {
		taskMonitor = monitor;
	}
	
	@Override 
	public void halt() {
		logger.warning("canceled");
		this.interrupted = true;
	}
	
	@Override 
	public String getTitle() {
		return "Pathway Finder - Cytoscape Attribute getter";
	}
}
