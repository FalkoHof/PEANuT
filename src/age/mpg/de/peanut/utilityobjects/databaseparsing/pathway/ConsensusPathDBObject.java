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

public class ConsensusPathDBObject implements PathwayObject {

	private String pathwayName = "";
	private String database = "";
	private String keggID = "";
	
	
	private List<String>idList;
	private Set<String> idSet;
	private Set<String> cyNodeIdSet;
	private boolean kegg = false;
	
	
	
	public ConsensusPathDBObject(String pathwayName, String database, List<String> idList){
		this.pathwayName = pathwayName;
		this.database = database;
		this.idList = idList;
		this.idSet = new HashSet<String>(idList);
		kegg = checkDatabase(database);
	}


	private boolean checkDatabase(String database){
		if (database.equals("KEGG"))
			return true;
		else
			return false;
	}
	
	
	public boolean isKEGG(){
		return kegg;
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

	@Override
	public int getPathwaySize() {
		return idSet.size();
	}

	@Override
	public int getNumerOfFoundNodes() {
		return cyNodeIdSet.size();
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
		return pathwayName + "::" + "CPDB (" + database + ")";
	}


	public String getKeggID() {
		return keggID;
	}


	public void setKeggID(String keggID) {
		this.keggID = keggID;
	}
}
