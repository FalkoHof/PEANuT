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

import java.util.List;

public class PathwayCommonsProtein {

	private String id;
	private List<String>pathwayURIList;
	private List<String>pathwayNameList;
	
	
	public PathwayCommonsProtein(String id, List<String> pwURIList, List<String> pwNameList){
		
		this.id = id;
		this.pathwayURIList = pwURIList;
		this.pathwayNameList = pwNameList;
	}
	
	public void print(){
		System.out.println("ID:" + "\t" + id + "\t" + pathwayURIList);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getPathwayURIList() {
		return pathwayURIList;
	}

	public List<String> getPathwayNameList() {
		return pathwayNameList;
	}

}
