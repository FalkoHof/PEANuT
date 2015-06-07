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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

// Singleton containing plugin properties set in plugin.properties
public class PluginProperties {

    private static PluginProperties instance = new PluginProperties();
    private Properties props;

    private PluginProperties(){
        try {
            props = new Properties();
            InputStream inputStream = getClass().getResourceAsStream("plugin.properties");
            props.load(inputStream);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public int getMaxThreadsPathways(){
    	return Integer.parseInt(props.getProperty("maxThreadsPathways"));
    }
    
   
    public int getMaxThreadsInteractions(){
    	return Integer.parseInt(props.getProperty("maxThreadsInteractions"));
    }
        
    public String getPathwayCommonsSifInteractionParameters(){
    	return props.getProperty("pathwayCommonsSifInteractionParameters").trim();
    }
    
    public String getWikiPathwaysDirectory(){
    	return props.getProperty("wikipathwaysDirectory");
    }

    
    public String[] getDatabases(){
    	return props.getProperty("databases").trim().split(";");
    }
    
    public static PluginProperties getInstance() {
        return instance;
    }
    
    
    public String getPluginName(){
    	return props.getProperty("pluginname");
    }
    
    
    public String getWpWebResource(){
    	return props.getProperty("wpWebResource").trim();
    }
    
    
    public String[] getConsensusPathDBWebResource(){
    	return props.getProperty("consensusPathDBWebResource").trim().split(";");
    }
    
    
    public String getConsensusPathDBDirectory(){
		return props.getProperty("consensusPathDBDirectory");
	}
    
    
    public String[] getConsensusPathDBFiles(){
		return props.getProperty("consensusPathDBFiles").trim().split(";");
    }
    
    public String [] getWpOrganisms(){
    	return (String []) props.getProperty("wpOrganisms").trim().split(";");
    }
    
    public String getWpFile(){
    	return (String) props.getProperty("wpFile").trim();
    }
    
    public String [] getConsensusPathDBOrganisms(){
    	return (String []) props.getProperty("consensusPathDBOrganisms").trim().split(";");
    }
    
    public Set<String> getDesiredKeggInteractions(){
    	Set<String>interationSet = new HashSet<String>();
    	String[]interactionsArr = (String[])props.getProperty("KEGGInteractionAttributes").trim().split(";");
    	
    	for(String s : interactionsArr)
    		interationSet.add(s);
    	
    	return interationSet;
    }
	
    public Set<String> getDesiredKeggEntryTypes(){
    	Set<String>entryTypesSet = new HashSet<String>();
    	String[]entryTypesArr = (String[])props.getProperty("KEGGEntryTypes").trim().split(";");
    	
    	for(String s : entryTypesArr)
    		entryTypesSet.add(s);

    	return entryTypesSet;    
    }
}
