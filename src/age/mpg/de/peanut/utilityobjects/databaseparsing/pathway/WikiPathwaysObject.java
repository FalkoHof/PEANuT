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
package age.mpg.de.peanut.utilityobjects.databaseparsing.pathway;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import age.mpg.de.peanut.model.PeanutModel;

public class WikiPathwaysObject implements PathwayObject{
	
	private String pathwayName;
	private String database = "WikiPathways";
	private String pathwayID;
	private List<String> idList;
	private Set<String> idSet;
	private Set<String> cyNodeIdSet;

	
	public WikiPathwaysObject(String pathwayName, String pathwayID, Set<String> idSet){
		this.pathwayName = pathwayName;
		this.pathwayID = pathwayID;
		this.idSet = idSet;	
	}
	
	public WikiPathwaysObject(String pathwayName, String pathwayID, List<String> idList){
		this.pathwayName = pathwayName;
		this.pathwayID = pathwayID;
		this.idList = idList;
		this.idSet = new HashSet<String>(idList);
	}

	@Override
	public String getPathwayName() {
		return pathwayName;
	}

	@Override
	public String getDatabase() {		
		return database;
	}

	@Override
	public Set<String> getIDs() {
		return idSet;
	}

	@Override
	public Set<String> getCyNodeIds() {
		return cyNodeIdSet;
	}
	/*
	public String getPathwayURI(){
		return pathwayURI;
	}*/

	@Override
	public int getNumerOfFoundNodes() {
		return cyNodeIdSet.size();
	}

	@Override
	public int getPathwaySize() {
		return idSet.size();
	}

	@Override
	public void setCyNodeIds(Set<String> cyNodeIdSet) {
		this.cyNodeIdSet = cyNodeIdSet;		
	}

	@Override
	public List<String> getIDList() {
		return idList;
	}
	

	@Override
	public String getPathwayURI() {
		return pathwayName + PeanutModel.GENERAL_DELIMITER + PeanutModel.WIKIPATHWAYS_DELIMITER + PeanutModel.GENERAL_DELIMITER + pathwayID;
	}
	
}
