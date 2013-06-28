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
package age.mpg.de.peanut.databases.webservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXB;
import javax.xml.ws.http.HTTPException;


import age.mpg.de.peanut.databases.kegg.jaxb.Entry;
import age.mpg.de.peanut.databases.kegg.jaxb.Pathway;
import age.mpg.de.peanut.databases.kegg.jaxb.Relation;
import age.mpg.de.peanut.databases.kegg.jaxb.Subtype;
import age.mpg.de.peanut.managers.TaskManagerManager;
import age.mpg.de.peanut.model.PeanutModel;
import age.mpg.de.peanut.utilityobjects.KEGGInteraction;
import age.mpg.de.peanut.utilityobjects.PluginProperties;
import age.mpg.de.peanut.utilityobjects.databaseparsing.pathway.ConsensusPathDBObject;
import age.mpg.de.peanut.utilityobjects.databaseparsing.pathway.PathwayObject;

import cytoscape.logger.CyLogger;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.util.URLUtil;

//class that uses the KEGG rest api to get directed edges
public class KeggService implements Task{

	//task monitor stuff
	private cytoscape.task.TaskMonitor taskMonitor;	private boolean interrupted = false;
	private CyLogger logger = CyLogger.getLogger(this.getClass());

	//rest base url and api parameters
	private final String REST_BASE_URL = "http://rest.kegg.jp";
	private final String LIST = "list", GET = "get", INFO = "info", FIND = "find", CONV = "conv", LINK = "link", KGML = "kgml", PATHWAY = "pathway", NCBI_GENEID = "ncbi-geneid";
	private final String KEGG_HUMAN = "hsa", KEGG_MOUSE = "mmu", KEGG_YEAST = "sce";
	private String organism;
	
	private List<String> keggIdList = new ArrayList<String>();
	private List<Pathway> keggPathwayList;
	private List<KEGGInteraction>KEGGInteractionList;
	private Map<String,String>keggIdConversionMap;
	private Set<String>keggInteractionSet;
	private Set<String>keggEntryTypesSet;
	
	private final int MAX_THREADS = 10;
	
	
	public KeggService(){
		organism = PeanutModel.getInstance().getOrganism();
		logger.info("Organism: " + organism);
		if(organism.equals("Human"))
			organism = KEGG_HUMAN;
		else if(organism.equals("Mouse"))
			organism = KEGG_MOUSE;
		else if(organism.equals("Yeast")){
			organism = KEGG_YEAST;
			keggIdConversionMap = new HashMap<String,String>();
		}
		else
			throw new InvalidParameterException("Invalid Organism");
		keggPathwayList = Collections.synchronizedList(new ArrayList<Pathway>());
	}	
	
	
	@Override
	public void run(){
		try {
			mapKEGGPathwayIds();
			getPathwaysAsKGMLs();
			extractDirectedInteractions();
		} catch (IOException e) {
			logger.warn("An Error occoured while getting the KEGG interactions",e);
			e.printStackTrace();
		}
	}
	
	//method returning an HttpUrlConnection to a specific url
	private HttpURLConnection openConnection(String url){
		HttpURLConnection huc = null;
		
		try {
			URL u = new URL(url); 
			huc =  (HttpURLConnection)  u.openConnection();
			huc.setRequestMethod("GET"); 
			huc.connect();
			logger.info("Connecting to url: " + url);
			System.out.println("Connecting to url: " + url);
			if(huc.getResponseCode() != 200){
				HTTPException e = new HTTPException(huc.getResponseCode());
				logger.warn("Error while connecting to KEGG rest api url: " + url,e);
				throw e;
			}
		} catch (IOException e) {
			logger.warn("Error while connecting to KEGG rest api", e);
			e.printStackTrace();
		}
		return huc;
	}
	
	//method for closing an open HttpURLConnection
	private void closeConnection(HttpURLConnection conn){
		logger.info("Colosing HttpUrlConnection: " + conn.getURL());
		conn.disconnect();
		conn = null;
	}
		
		
	//method that checks which KEGG pathways are present in the cytoscape network and maps them to their KEGG IDs
	private void mapKEGGPathwayIds() throws IOException{
		String info = "Starting mapping ConsensusPathDB Pathways to KEGG pathwayIds..";
		logger.info(info);
		taskMonitor.setStatus(info);
		
		Map<String,String> pathwayNameToIDMap = getKEGGPathwayIDs();
		
		List<PathwayObject> pathwayList = PeanutModel.getInstance().getPathwayList();
		
		int counter = 0;
		
		for (PathwayObject pathway : pathwayList){
			if (interrupted){
				PeanutModel.getInstance().setExit(true);
				break;
			}
			counter++;
			taskMonitor.setPercentCompleted(TaskManagerManager.getInstance().getPercentage(counter, pathwayList.size()));
			
			if(pathway instanceof ConsensusPathDBObject){
				ConsensusPathDBObject cpdbPathway = (ConsensusPathDBObject) pathway;
				
				if(cpdbPathway.isKEGG()){
					String pathwayName = pathway.getPathwayName();
					
					if(pathwayNameToIDMap.containsKey(pathwayName)){
						String keggId = pathwayNameToIDMap.get(pathwayName);
						//TODO check if necessary
						//cpdbPathway.setKeggID(keggId);
						//keggPathways.add(cpdbPathway);
						keggIdList.add(keggId);
						logger.info("Pathway: " + pathwayName + " is a KEGGPathway\t" + "KEGGID: " + keggId);
					}
					else
						logger.warn("Pathway: " + pathwayName + " clould not be mapped to KEGG pathways");
				}
			}
		}
	}	
	
	//method for retrieving the KEGG pathwayIDs in a map (Name,Id)
	private Map<String,String> getKEGGPathwayIDs() {
		
		Map<String,String> pathwayNameToIDMap = new HashMap<String,String>();
		
		String url = REST_BASE_URL + "/" + LIST + "/" + PATHWAY + "/" + organism;
		HttpURLConnection conn = openConnection(url);
		
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = "";

			String info = "Retrieving KEGG pathway name to KEGG Id map...";
			logger.info(info);
			taskMonitor.setStatus(info);
			
			while ((line = br.readLine()) != null){
				String[] lineArr = line.split("\t");
				String pathwayName = lineArr[1];
				String pathwayID = lineArr[0].split(":")[1];
				pathwayNameToIDMap.put(pathwayName, pathwayID);			
			}
			br.close();
		} catch (IOException e) {
			logger.warn("Retrieving KEGG pathway name to KEGG Id map failed", e);
			e.printStackTrace();
		}
		
		closeConnection(conn);
		return pathwayNameToIDMap;	
	}
	
	
	//Method for managing the download of KEGG pathways as KGML
	private void getPathwaysAsKGMLs(){
		List<Thread> threadList = new ArrayList<Thread>();
		
		String info = "Downloading KEGG KGML files...";
		logger.info(info);
		taskMonitor.setStatus(info);
		
		int counter = 0;
		try {
			for(String id : keggIdList){
				// set status of the cytoscape taskmanager
				taskMonitor.setPercentCompleted(TaskManagerManager.getInstance().getPercentage(counter, keggIdList.size()));
				counter++;
				// breaks the loop if task gets canceled
				if (interrupted){
					PeanutModel.getInstance().setExit(true);
					break;
				}
				if (threadList.size() < MAX_THREADS)
					threadList.add(startFetcherThread(id));
				else{
					threadList.add(startFetcherThread(id));
					waitForThreads(threadList);
				}
			}
			waitForThreads(threadList);
		} catch (Exception e) {
			logger.warn("Downloading KEGG KGML files failed", e);
			e.printStackTrace();
		}
		logger.info(keggIdList.size() + " KEGG pathways downloaded");
	}
	
	//method for starting a KGML fetcher thread
	private Thread startFetcherThread(String id){
		logger.info("Starting a new Thread... KEGG Pathway ID: " + id);
		Thread runner = new Thread(new KGMLFetcher(id));
		runner.start();
		return runner;
	}
	
	//method for wating for threads
	private void waitForThreads(List<Thread> threadList) throws InterruptedException{
		for(Thread thread : threadList)					
			thread.join();
	}
	
	//class/thread for downloading a KGML
	class KGMLFetcher implements Runnable{
		
		private String id;
		
		public KGMLFetcher(String id){
			this.id = id;
		}
		
		@Override
		public void run() {
			String url = REST_BASE_URL + "/" + GET + "/" + id + "/" + KGML;
			InputStream is;
			try {
				is = URLUtil.getInputStream(new URL(url));
				//unmarshal KGML with JAXB
				Pathway KGMLpathway = JAXB.unmarshal(is, Pathway.class);
				keggPathwayList.add(KGMLpathway);
				logger.info("Fetched pathway:\t" + id);

			} catch (Exception e) {
				//if somethingn failes, pause for 1 sec an try it again. if it fails again skipp the pahtway
				try {
					logger.warn("Unable to fetch pathway:\t" + id + "\tRetrying...");
					Thread.sleep(1000);
					run();
				} catch (InterruptedException e1) {
					logger.warn("Unable to fetch pathway:\t" + id + "\tSkipping...",e1);
				}
			}
		}
	}
	
	
	//method for extracting directed interactions from the unmarshalled KGMLS
	private void extractDirectedInteractions(){
		//get properties describing the desired types of interactions and entries
		//keggInteractionSet = PluginProperties.getInstance().getDesiredKeggInteractions();
		keggEntryTypesSet = PluginProperties.getInstance().getDesiredKeggEntryTypes();
		KEGGInteractionList = new ArrayList<KEGGInteraction>();

		String info = "Extracting directed interactions from KGML files...";
		logger.info(info);
		taskMonitor.setStatus(info);
		
		
		//loop over all pathways
		int counter = 0;
		
		for(Pathway pathway : keggPathwayList){
			List<Entry> entryList = new ArrayList<Entry>(pathway.getEntry());
		
			//check which method should be executed --> method 1 if entry.getId() == position in the arrayList - 1 else method 2
			String idStr = entryList.get(entryList.size()-1).getId();
			int id = Integer.parseInt(idStr);
			
			//check how to extract and create the new interactions from the KGML file --> if the entryList consists out of continious ascending Ids,
			//pick method1, otherwise pick method2
			if (id == entryList.size())
				extractInteractionsMethod1(pathway);
			else
				extractInteractionsMethod2(pathway);	
			
			//booolean for breaking the loop if the process get canceled
			if (interrupted){
				PeanutModel.getInstance().setExit(true);
				break;
			}
			//progressbar stuff
			counter++;
			taskMonitor.setPercentCompleted(TaskManagerManager.getInstance().getPercentage(counter, keggPathwayList.size()));
			logger.info(info + pathway.getName());
		}
		logger.info("Extracting directed interactions from KGML files... - Done");
		if (organism.equals(KEGG_YEAST))
			mapIDs();
		
		PeanutModel.getInstance().setKeggInteractionList(KEGGInteractionList);

	}
		
	//method for creating the right interactions via their position in the unmarshalled list
	private void extractInteractionsMethod1(Pathway pathway){
		logger.info("id == entryList.size()... Calling extractInteractionsMethod1");
		//get relations (interactions) and entries of the pathway
		List<Relation> relationList = new ArrayList<Relation>(pathway.getRelation());
		List<Entry> entryList = new ArrayList<Entry>(pathway.getEntry());	
		
		//loop over the relations
		for(Relation relation : relationList){
			List<Subtype> subTypeList = relation.getSubtype();
			
			//get subtypes of the relation
			for(Subtype subtype : subTypeList){
				String subType = subtype.getName();
				
				//check if the type of the relation/interaction should be saved
				//if(keggInteractionSet.contains(subType)){
					//possible as the entry id = entryListIndex-1
					int entry1Index = Integer.parseInt(relation.getEntry1());
					int entry2Index = Integer.parseInt(relation.getEntry2());
					Entry entry1 = entryList.get((entry1Index-1));
					Entry entry2 = entryList.get((entry2Index-1));
					
					//check if the relation is between wanted entry types (excludes relations to pathways, chemical compounds etc.) and save it
					if(keggEntryTypesSet.contains(entry1.getType()) && keggEntryTypesSet.contains(entry2.getType()))
						KEGGInteractionList.add(new KEGGInteraction(entry1.getName(), entry2.getName(), subType, pathway.getTitle(),organism));
				//}
			}
		}		
	}
	
	//method for creating the right interactions via a hashmap mapping
	private void extractInteractionsMethod2(Pathway pathway){
		logger.info("id != entryList.size()... Calling extractInteractionsMethod2");
		Map<String,Entry>entryIdMap = new HashMap<String,Entry>();
		List<Entry> entryList = new ArrayList<Entry>(pathway.getEntry());	
		List<Relation> relationList = new ArrayList<Relation>(pathway.getRelation());

		//loop for creating a map (entryId,Entry)
		for (Entry entry: entryList)
			entryIdMap.put(entry.getId(), entry);
			
		for(Relation relation : relationList){
			List<Subtype> subTypeList = relation.getSubtype();
			
			//get subtypes of the relation
			for(Subtype subtype : subTypeList){
				String subType = subtype.getName();
				
				//check if the type of the relation/interaction should be saved
				//if(keggInteractionSet.contains(subType)){
					String entry1Index = relation.getEntry1();
					String entry2Index = relation.getEntry2();
					//get entry from the entryId,Entry map
					Entry entry1 = entryIdMap.get(entry1Index);
					Entry entry2 = entryIdMap.get(entry2Index);
					
					//check if the relation is between wanted entry types (excludes relations to pathways, chemical compounds etc.) and save it
					if(keggEntryTypesSet.contains(entry1.getType()) && keggEntryTypesSet.contains(entry2.getType()))
						KEGGInteractionList.add(new KEGGInteraction(entry1.getName(), entry2.getName(), subType, pathway.getTitle(),organism));
				//}
			}
		}			
	}
	
	//map organism ids of the kegg file to entrez gene ids (in this case only needed for yeast, as human and mouse kegg ids correspond to entrez geneIds)
	private void mapIDs(){
		
		String info = "Mapping KEGG IDs of directed interactions to entrez gene ids...";
		logger.info(info);
		taskMonitor.setStatus(info);
		String url = REST_BASE_URL + "/" + CONV + "/" + NCBI_GENEID + "/" + organism;

		
		try {
			getMappings(url);
			for (KEGGInteraction interaction: KEGGInteractionList){
				
				List<String>entry1List = interaction.getEntry1IdsForMapping();
				List<String>entry2List = interaction.getEntry2IdsForMapping();
				List<String>mappedEntry1List = new ArrayList<String>();
				List<String>mappedEntry2List = new ArrayList<String>();
				
				for (String s : entry1List)
					mappedEntry1List.add(keggIdConversionMap.get(s));
				
				
				for (String s : entry2List)
					mappedEntry2List.add(keggIdConversionMap.get(s));
				
				interaction.setEntry1GeneIds(mappedEntry1List);
				interaction.setEntry2GeneIds(mappedEntry2List);
			}
			
		} catch (IOException e) {
			logger.warn("Error while getting KEGG ID mappings", e);
			e.printStackTrace();
		}
		
	
		logger.info("Mapping KEGG IDs of directed interactions to entrez gene ids... - Done");
		PeanutModel.getInstance().setKeggInteractionList(KEGGInteractionList);
	}
	
	
	//get the mappings
	private void getMappings(String url) throws IOException{
		
		logger.info("Getting keggIdConversionMap...");

		HttpURLConnection conn = openConnection(url);
		BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line = "";
		while((line = br.readLine()) != null){
			String[]temp = line.split("\t");
			String kegg_Id = temp[0].split("[:]")[1];
			String ncbi_geneId = temp[1].split("[:]")[1];
			keggIdConversionMap.put(kegg_Id, ncbi_geneId);
        }
		br.close();
		closeConnection(conn);
		logger.info("Getting keggIdConversionMap... - Done\n" + "keggIdConversionMap size: " + keggIdConversionMap.size());
		System.out.println("Getting keggIdConversionMap... - Done\n" + "keggIdConversionMap size: " + keggIdConversionMap.size());
	}
	
	@Override
	public String getTitle() {
		return "Pathway Finder - Importing KEGG Data";
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
