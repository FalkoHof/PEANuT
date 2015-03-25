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
package age.mpg.de.peanut.statistics;

import java.util.Set;

import age.mpg.de.peanut.model.PeanutModel;

public class StatisticResults implements Comparable<StatisticResults> {
	
	private String pathwayName = "";
	private String displayName = "";
	private String type = "";
	private String wikipathwayID = "";
	private String consensusPathDBID = "";
		
	
	private int numberOfNodes = 0;
	private int numberOfMembers = 0;
	private int pathwaySize = 0;
	
	// fisher's exact test results
	private double pValueOneTailed;

	// booleans to specifiy if the result has been selected for visualisation and can be visualized
	private boolean selected = false;
	
	
	
	private Set<String>bioIdSet;
	private Set<String>cyIdSet;
	
	public StatisticResults(String pathwayName, Set<String> bioIdSet, Set<String>cytoscapeIdSet, int pathwaySize, double oneTailed){
		this.pathwayName = pathwayName;
		this.bioIdSet = bioIdSet;
		this.cyIdSet = cytoscapeIdSet;
		this.pValueOneTailed = oneTailed;
		this.pathwaySize = pathwaySize;
		numberOfNodes = cyIdSet.size();
		numberOfMembers = bioIdSet.size();
		detectType();
	}
	
	
	// method that sets class paramenters according to the pathwayName
	//---> pathwaycommons name consist out of "PathwayName", wikipathways name out of "PathwayName:WikipathwayID"
	public void detectType(){
		
		if (pathwayName.contains(PeanutModel.GENERAL_DELIMITER + PeanutModel.WIKIPATHWAYS_DELIMITER)){
			type = PeanutModel.SOURCE_WP;
			String[] temp = pathwayName.split(PeanutModel.GENERAL_DELIMITER + PeanutModel.WIKIPATHWAYS_DELIMITER);
			displayName = temp[0];
		}
		if (pathwayName.contains(PeanutModel.GENERAL_DELIMITER + PeanutModel.PATHWAYCOMMONS_DELIMITER)){
			String[] temp = pathwayName.split(PeanutModel.GENERAL_DELIMITER + PeanutModel.PATHWAYCOMMONS_DELIMITER);
			displayName = temp[0];
			type = PeanutModel.SOURCE_PC;
		}
		if (pathwayName.contains(PeanutModel.GENERAL_DELIMITER +  PeanutModel.CONSENSUSPATHDB_DELIMITER)){
			String[] temp = pathwayName.split(PeanutModel.GENERAL_DELIMITER + PeanutModel.CONSENSUSPATHDB_DELIMITER);
			displayName = temp[0];
			type = PeanutModel.CONSENSUSPATHDB_DELIMITER + " " + temp[1];
		}	
	}

	
	public String getDisplayName(){
		return displayName;
	}
	
	public double getOneTailed() {
		return pValueOneTailed;
	}

	public String getPathwayName() {
		return pathwayName;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public Set<String> getIdSet() {
		return bioIdSet;
	}

	public String getWikipathwayID() {
		return wikipathwayID;
	}

	public String getDatabaseType() {
		return type;
	}
	
	public Set<String> getCyIdSet() {
		return cyIdSet;
	}

	public int getNumberOfNodes() {
		return numberOfNodes;
	}

	public int getNumberOfMembers() {
		return numberOfMembers;
	}

	public Set<String> getBioIdSet() {
		return bioIdSet;
	}
	
	public String getProportion(){
		return numberOfMembers + "/"+ pathwaySize;
	}
	
	public double getCoverage(){
		return (numberOfMembers*100.0)/(pathwaySize);
	}


	@Override
	public int compareTo(StatisticResults o) {
		return Double.compare(pValueOneTailed, o.getOneTailed());
	}
}
