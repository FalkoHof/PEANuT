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
package age.mpg.de.peanut.utilityobjects.cytoscapeparsing.pathway;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import age.mpg.de.peanut.model.PeanutModel;

public class PathwayObjectStatistics {
	
	public static final int CHILD_NETWORK = 0;
	public static final int PARENT_NETWORK = 1;

	private String pathwayName = "";
	private String database = "";
	
	private int pathwaySize = -1;

	private Set<String> cyNodeParentSet = new HashSet<String>();
	private Set<String> cyNodeChildSet = new HashSet<String>();
	
	private Set<String>idParentSet = new HashSet<String>();
	private Set<String>idChildSet = new HashSet<String>();
	
	
	private List<Set<String>>cyNodeSetList;
	private List<Set<String>>idSetList;
	
	public PathwayObjectStatistics(String pathwayName, String database){
		this.pathwayName = pathwayName;
		this.database = database;
		initializeLists();		
	}
	
	public PathwayObjectStatistics(String pathwayName, String database, int pathwaySize){
		this.pathwayName = pathwayName;
		this.database = database;
		this.pathwaySize = pathwaySize;		
		initializeLists();
	}
	
	
	public void initializeLists(){
		cyNodeSetList = new ArrayList<Set<String>>();
		cyNodeSetList.add(CHILD_NETWORK, cyNodeChildSet);
		cyNodeSetList.add(PARENT_NETWORK, cyNodeParentSet);
		
		idSetList =  new ArrayList<Set<String>>();
		idSetList.add(CHILD_NETWORK, idChildSet);
		idSetList.add(PARENT_NETWORK, idParentSet);
	}
	
	
	public Set<String> getCytoscapeIdsFoundMembers(){
		return cyNodeChildSet;
	}
	
	public Set<String> getBioIdsFoundMembers(){
		return idChildSet;
	}
	
	
	public void addCytoscapeNodeId(String id, int network) {
		cyNodeSetList.get(network).add(id);
	}

	public void addBioId(String id, int network) {
		idSetList.get(network).add(id);
	}

	public int getNumberBioIdsChild() {
		return idChildSet.size();
	}

	public int getNumberBioIdsParent() {
		return idSetList.get(PathwayObjectStatistics.PARENT_NETWORK).size();
	}
	
	public int getNumberCytoscapeNodeIdsParent() {
		return cyNodeParentSet.size();
	}

	public int getNumberCytoscapeNodeIdsChild() {
		return cyNodeChildSet.size();
	}

	public String getPathwayName() {
		return pathwayName;
	}

	public String getPathwayURI(){
		return pathwayName + PeanutModel.GENERAL_DELIMITER + database;
	}

	public void setPathwayName(String pathwayName) {
		this.pathwayName = pathwayName;
	}

	public int getPathwaySize() {
		if(pathwaySize > 0)
			return pathwaySize;
		else
			return idParentSet.size();
	}
	
	//TODO maybe implement something more sophisticated
	
	
	public int getNumberOfFoundMembers(){
		if(idChildSet.size() > cyNodeChildSet.size())
			return cyNodeChildSet.size();
		else	
			return idChildSet.size();
	}
	

	public String getDatabase() {
		return database;
	}
	
}
