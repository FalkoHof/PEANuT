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
package age.mpg.de.peanut.utilityobjects;

import java.util.ArrayList;
import java.util.List;


//class to hold one type of interaction extracted from a KEGG KGML file
public class KEGGInteraction {
	
	
	private final String YEAST = "sce:";
	private String pathwayName;
	private String organism;
	private String interactionType;
	private String entry1;
	private String entry2;
	
	
	private List<String>entry1Ids;
	private List<String>entry2Ids;
	
	private List<String> entry1GeneIds;
	private List<String> entry2GeneIds;
	
	public KEGGInteraction(String entry1, String entry2, String interactionType, String pathwayName, String organism){
		this.entry1 = entry1;
		this.entry2 = entry2;
		this.interactionType = interactionType;	
		this.pathwayName = pathwayName;
		this.organism = organism + ":";
		entry1Ids = new ArrayList<String>();
		entry2Ids = new ArrayList<String>();
		
		setEntry1Ids(entry1);
		setEnty2Ids(entry2);
		
		//conditional initialization because yeast ids need to be converted to entrez gene ids, while human and mouse ids already are entrez gene ids;
		if(!organism.equals(YEAST)){
			entry1GeneIds = entry1Ids;
			entry2GeneIds = entry2Ids;
		}
		else{
			entry1GeneIds = new ArrayList<String>();
			entry2GeneIds = new ArrayList<String>();
		}
			
	}

	//The string of an entry can contain more than KEGG Id sperated by whitespaces --> check if more than one id
	//Also we are not interested in interactions to objects other than genes or knockout mutants --> id has to contain either the specific organism prefix or "ko:"
	private void setEntry1Ids(String entry1){
		if(entry1.contains(" "))
			for (String s : entry1.split(" "))
				if (s.contains(organism))
					entry1Ids.add(s.split(organism)[1]);
		else
			if (s.contains(organism))
				entry1Ids.add(s.split(organism)[1]);
	}
	
	private void setEnty2Ids(String entry2){
		if (entry2.contains(" "))
			for (String s : entry2.split(" "))
				if (s.contains(organism))
					entry2Ids.add(s.split(organism)[1]);
		else
			if (s.contains(organism))
				entry2Ids.add(s.split(organism)[1]);
	}
	
	
	

	public List<String> getEntry1GeneIds() {
		return entry1GeneIds;
	}

	public void setEntry1GeneIds(List<String> entry1GeneIds) {
		this.entry1GeneIds = entry1GeneIds;
	}

	public List<String> getEntry2GeneIds() {
		return entry2GeneIds;
	}

	public void setEntry2GeneIds(List<String> entry2GeneIds) {
		this.entry2GeneIds = entry2GeneIds;
	}

	public String getInteractionType() {
		return interactionType;
	}

	public String getEntry1() {
		return entry1;
	}

	public String getEntry2() {
		return entry2;
	}

	public String getPathwayName() {
		return pathwayName;
	}

	public List<String> getEntry1IdsForMapping() {
		return entry1Ids;
	}

	public List<String> getEntry2IdsForMapping() {
		return entry2Ids;
	}
}
