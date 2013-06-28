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

import java.util.List;
import java.util.Set;

public interface PathwayObject {
	
	
	public String getPathwayName();

	public String getDatabase();
	
	public String getPathwayURI();

	public Set<String> getIDs();
	
	public List<String> getIDList();
		
	public Set<String> getCyNodeIds();
	
	public void setCyNodeIds(Set<String>cyNodeIdSet);
	
	public int getPathwaySize();
	
	public int getNumerOfFoundNodes();
}
