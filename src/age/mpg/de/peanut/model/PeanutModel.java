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
package age.mpg.de.peanut.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import age.mpg.de.peanut.databases.kegg.jaxb.Relation;
import age.mpg.de.peanut.statistics.StatisticResults;
import age.mpg.de.peanut.utilityobjects.KEGGInteraction;
import age.mpg.de.peanut.utilityobjects.PathwayCommonsProtein;
import age.mpg.de.peanut.utilityobjects.cytoscapeparsing.pathway.PathwayObjectStatistics;
import age.mpg.de.peanut.utilityobjects.databaseparsing.pathway.PathwayObject;

import cytoscape.logger.CyLogger;

//Model class - Holding variables/values for setting program parameters
public class PeanutModel {	
	
    
	private static PeanutModel instance = new PeanutModel();
	private CyLogger logger = CyLogger.getLogger(this.getClass());

	// General parameters
	private String database ="";
	private String columnID = "";
	private String datasource = "All";
	private String organism = "";
		
	
	//static final delimter strings for string splitting
	public static final String GENERAL_DELIMITER = "::";
	public static final String PATHWAYCOMMONS_DELIMITER = "PC";
	public static final String CONSENSUSPATHDB_DELIMITER = "CPDB";
	public static final String WIKIPATHWAYS_DELIMITER = "WP";
	
	
	// Variables for parsed/retrieved data;
	private List<PathwayCommonsProtein> proteinPathwayList = new ArrayList<PathwayCommonsProtein>();
	private List<PathwayObject> pathwayList = new ArrayList<PathwayObject>();
	
	
	//kegg Stuff
	private boolean importKeggInteractions = true;
	private List<KEGGInteraction>keggInteractionList = new ArrayList<KEGGInteraction>();
	
	private Map<String,Set<String>>idConversionMap = new HashMap<String,Set<String>>();
	private List<String>wholeNetworkIdList = new ArrayList<String>();
	private int NumerOfaddedEdges;
	
	
	// maps for statistics
	private Map<String, PathwayObjectStatistics> pathwayDistributionMap = new HashMap<String,PathwayObjectStatistics>();
	private List<StatisticResults> statisticsResultList = new ArrayList<StatisticResults>();
	
	
	//booleans for Pathway Commons
	private boolean pcCellMap = true;
	private boolean pcHumanCyc = true;
	private boolean pcReactome = true;
	private boolean pcNCINature = true;
	private boolean pcIMID = true;
	
	
	//booleans for statistics bejamini hoechst is the default
	private boolean noFRD = false;
	private boolean bonferonni = false;
	private boolean bejaminiHoechstFDR = true;
	
	//values for pathway commons data_source parameters 
	private final String pc_parameter_CellMap = "CELL_MAP";
	private final String pc_parameter_HumanCyc = "HUMANCYC";
	private final String pc_parameter_Reactome = "REACTOME";
	private final String pc_parameter_NCINature = "NCI_NATURE";
	private final String pc_parameter_IMID = "IMID";
	
	
	// final static strings for table coloumns (CyAttributes) and comparison 
	public final static String COLUMN_TITLE_PATHWAY_IDENTIFIER_LIST = "PathwayFinderIdentifier";
	public final static String COLUMN_TITLE_NUMBER_PATHWAYS = "Number of Pathways";
	public final static String COLUMN_TITLE_PATHWAY_NAME_LIST = "Pathways";	
	
	public final static String SOURCE_WP = "WikiPathways";	
	public final static String SOURCE_PC = "Pathway Commons";
	public final static String SOURCE_CONSENSUSPATHDB = "ConsensusPathDB";
	
	//variables needed for statistics
	private String childNetwork = "";
	private String parentNetwork = "";
	private int childSize = 0;
	private int parentSize = 0;
	private double pValue = 0.001;

	
	//parameters for error dialogues & decision making. 
	private boolean exit = false;
	private boolean isFinished = false;
	
		
    private PeanutModel (){
    	
    }
    
    public static PeanutModel getInstance() {
		return instance;
	}
    
    
    public String getPathwayCommonsDataSourceParameters(){
    	//booleans for Pathway Commons
    	String parameter = "data_source=";
    	
    	if (pcCellMap && pcHumanCyc && pcReactome && pcNCINature && pcIMID)
    		return "";
    	else{
    		if (pcCellMap)
    			parameter = parameter + pc_parameter_CellMap + ",";
    		if (pcHumanCyc)
    			parameter = parameter + pc_parameter_HumanCyc + ",";
    		if (pcReactome)
    			parameter = parameter + pc_parameter_Reactome + ",";
    		if (pcNCINature)
    			parameter = parameter + pc_parameter_NCINature + ",";
    		if (pcIMID)
    			parameter = parameter + pc_parameter_IMID + ",";
    		logger.info("Getting pathwayCommons datasource parameters: " + parameter);
    		return parameter;
    	}
    }
    
    // returns true is pathwaycommons has been selected as database of choice
    public boolean pcCommons(){    	
    	if (database.equals(SOURCE_PC)){
    		logger.info("Use pathway commons: true" );
    		return true;
    	}
    	else{
    		logger.info("Use pathway commons: false");
    		return false;
    	}
    }
    
    // returns true is wikipathways has been selected as database of choice
    public boolean wikiPathways(){    	
    	if (database.equals(SOURCE_WP)){
    		logger.info("Use wikipathways: true" );
    		return true;
    	}
    	else{
    		logger.info("Use wikipathways: false" );
    		return false;
    	}
    }
    
    public boolean consensusPathDB(){
    	if (database.equals(SOURCE_CONSENSUSPATHDB)){
    		logger.info("Use consensusPathDB: true" );

    		return true;
    	}
    	else{
    		logger.info("Use consensusPathDB: false" );
    		return false;
    	}
    }
    
	public String getDatabase() {
		logger.info("Getting database variable: " + database);
		return database;
	}

	public void setDatabase(String database) {
		logger.info("Setting database variable: " + database);
		if (database == null || database.equals(""))
			logger.warn("Unexpected String value for database: " + database);
		this.database = database;
	}

	public String getColumnID() {
		logger.info("Getting columnID variable:" + columnID);
		return columnID;
	}

	public void setColumnId(String columnID) {
		if (columnID == null || columnID.equals(""))
			logger.warn("Unexpected String value for columnID: " + columnID);
		else
			logger.info("Setting columnID variable: " + columnID);
		this.columnID = columnID;
	}

	public String getDatasource() {
		logger.info("Getting datasource variable: " + datasource);
		return datasource;
	}

	public void setDatasource(String datasource) {
		if (datasource == null || datasource.equals(""))
			logger.warn("Unexpected String value for datasource: " + datasource);
		else
			logger.info("Setting datasource variable: " + datasource);
		this.datasource = datasource;
	}

	public String getOrganism() {
		logger.info("Getting organism variable: " + organism);
		return organism;
	}

	public void setOrganism(String organism) {
		if (organism == null || organism.equals(""))
			logger.warn("Unexpected String value for organism: " + organism);
		else
			logger.info("Setting organism variable: " + organism);
		this.organism = organism;
	}

	public String getChildNetwork() {
		if (childNetwork == null || childNetwork.equals(""))
			logger.warn("Unexpected String value for childNetwork: " + childNetwork);
		else
			logger.info("Getting childNetwork variable: " + childNetwork);
		return childNetwork;
	}

	public void setChildNetwork(String childNetwork) {
		if (childNetwork == null || childNetwork.equals(""))
			logger.warn("Unexpected String value for childNetwork: " + childNetwork);
		else
			logger.info("Setting childNetwork variable: " + childNetwork);
		this.childNetwork = childNetwork;
	}

	public String getParentNetwork() {
		if (parentNetwork == null || parentNetwork.equals(""))
			logger.warn("Unexpected String value for parentNetwork: " + parentNetwork);
		else
			logger.info("Getting parentNetwork variable:" + parentNetwork);
		return parentNetwork;
	}

	public void setParentNetwork(String parentNetwork) {
		if (parentNetwork == null || parentNetwork.equals(""))
			logger.warn("Unexpected String value for parentNetwork: " + parentNetwork);
		else
			logger.info("Setting parentNetwork variable: " + parentNetwork);
		this.parentNetwork = parentNetwork;
	}

	public int getChildSize() {
		if (childSize == 0 || childSize < 0)
			logger.warn("Unexpected integer value for childSize");
		else
			logger.info("Getting childSize variable: " + childSize);
		return childSize;
	}

	public void setChildSize(int childSize) {
		if (childSize == 0 || childSize < 0)
			logger.warn("Unexpected integer value for childSize");
		else
			logger.info("Setting childSize variable: " + childSize);
		this.childSize = childSize;
	}

	public int getParentSize() {
		if (parentSize == 0 || parentSize < 0)
			logger.warn("Unexpected integer value for parentSize");
		else
			logger.info("Getting parentSize variable: " + parentSize);
		return parentSize;
	}

	public void setParentSize(int parentSize) {
		if (parentSize == 0 || parentSize < 0)
			logger.warn("Unexpected integer value for parentSize");
		else
			logger.info("Setting parenSize variable: " + parentSize);
		this.parentSize = parentSize;
	}

	
	public double getpValue() {
		if (pValue == 0.0 || parentSize < 0.0 || pValue > 1.0)
			logger.warn("Unexpected double value for parentSize");
		else
			logger.info("Getting pValue variable: " + pValue);
		return pValue;
	}

	public void setpValue(double pValue) {
		if (pValue == 0.0 || parentSize < 0.0 || pValue > 1.0)
			logger.warn("Unexpected double value for parentSize");
		else
			logger.info("Setting pValue variable: " + pValue);
		this.pValue = pValue;
	}

	public boolean isExit() {
		logger.info("Getting exit boolean: " + exit);
		return exit;
	}

	public void setExit(boolean exit) {
		logger.info("Setting exit boolean: " + exit);
		this.exit = exit;
	}

	public boolean isFinished() {
		logger.info("Getting isFinished boolean: " + isFinished);
		return isFinished;
	}

	public void setFinished(boolean isFinished) {
		logger.info("Setting isFinished boolean: " + isFinished);
		this.isFinished = isFinished;
	}

	public void setpValueFromString(String text){

		try{
			this.pValue = Double.parseDouble(text);
			logger.info("Setting pValue variable: " + this.pValue);
		}
		catch (NumberFormatException e){
			// if string can not be parsed to pValue use default value
			logger.warn("Setting pValue failed. Will use default pValue (p=0.001)", e);
			pValue = 0.001;
			e.printStackTrace();
		}
	}

	public List<PathwayCommonsProtein> getProteinPathwayList() {
		if (proteinPathwayList.size() == 0 || proteinPathwayList == null)
			logger.warn("Unexpected value for pathway commons proteinPathwayList");
		else
			logger.info("Getting pathway commons proteinPathwayList... Size: " + proteinPathwayList.size());
		return proteinPathwayList;
	}

	public void setProteinPathwayList(List<PathwayCommonsProtein> proteinPathwayList) {
		if (proteinPathwayList.size() == 0 || proteinPathwayList == null)
			logger.warn("Unexpected value for pathway commons proteinPathwayList");
		else
			logger.info("Setting proteinPathwayList... Size: " + proteinPathwayList.size());
		this.proteinPathwayList = proteinPathwayList;

	}

	public List<PathwayObject> getPathwayList() {
		if (pathwayList.size() == 0 || pathwayList == null)
			logger.warn("Unexpected value for pathwayList");
		else
			logger.info("Getting pathwayList... size: " + this.pathwayList.size());
		return this.pathwayList;
	}
	
	public void setPathwayList(List<PathwayObject> pathwayList) {
		if (pathwayList.size() == 0 || pathwayList == null)
			logger.warn("Unexpected value for pathwayList");
		else
			logger.info("Setting pathwayList... size: " + pathwayList.size());
		this.pathwayList = pathwayList;
	}

	public Map<String, Set<String>> getIdConversionMap() {
		if (idConversionMap.size() == 0 || idConversionMap == null)
			logger.warn("Unexpected value for idConversionMap");
		else
			logger.info("Getting idConversionMap... size: " + idConversionMap.size());
		return idConversionMap;
	}
	
	public void setIdConversionMap(Map<String, Set<String>> idConversionMap) {
		if (idConversionMap.size() == 0 || idConversionMap == null)
			logger.warn("Unexpected value for idConversionMap");
		else
			logger.info("Setting idConversion map... size: " + idConversionMap.size());
		this.idConversionMap = idConversionMap;
	}

	public List<String> getWholeNetworkIdList() {
		if (wholeNetworkIdList.size() == 0 || wholeNetworkIdList == null)
			logger.warn("Unexpected value for wholeNetworkIdList");
		else
			logger.info("Getting wholeNetworkIdList... size: " + wholeNetworkIdList.size());
		return wholeNetworkIdList;
	}

	public void setWholeNetworkIdList(List<String> wholeNetworkIdList) {
		if (wholeNetworkIdList.size() == 0 || wholeNetworkIdList == null)
			logger.warn("Unexpected value for wholeNetworkIdList");
		else
			logger.info("Setting wholeNetworkIdList... size: " + wholeNetworkIdList.size());
		this.wholeNetworkIdList = wholeNetworkIdList;
	}

	public boolean isPcCellMap() {
		logger.info("Getting pcCellMap boolean : " + pcCellMap);
		return pcCellMap;
	}

	public void setPcCellMap(boolean pcCellMap) {
		logger.info("Setting pcCellMap boolean : " + pcCellMap);
		this.pcCellMap = pcCellMap;
	}

	public boolean isPcHumanCyc() {
		logger.info("Getting pcHumanCyc boolean : " + pcHumanCyc);
		return pcHumanCyc;
	}

	public void setPcHumanCyc(boolean pcHumanCyc) {
		logger.info("Setting pcHumanCyc boolean : " + pcHumanCyc);
		this.pcHumanCyc = pcHumanCyc;
	}

	public boolean isPcReactome() {
		logger.info("Getting pcReactome boolean : " + pcReactome);
		return pcReactome;
	}

	public void setPcReactome(boolean pcReactome) {
		logger.info("Setting pcReactome boolean: " + pcReactome);
		this.pcReactome = pcReactome;
	}

	public boolean isPcNCINature() {
		logger.info("Getting pcNCINature boolean: " + pcNCINature);
		return pcNCINature;
	}

	public void setPcNCINature(boolean pcNCINature) {
		logger.info("Setting pcNCINature boolean: " + pcNCINature);
		this.pcNCINature = pcNCINature;
	}

	public boolean isPcIMID() {
		logger.info("Getting pcIMID boolean: " + pcIMID );
		return pcIMID;
	}

	public void setPcIMID(boolean pcIMID) {
		logger.info("Setting pcIMID boolean: " + pcIMID);
		this.pcIMID = pcIMID;
	}

	public List<StatisticResults> getStatisticsResultList() {
		if (statisticsResultList == null)
			logger.warn("Null value for statisticsResultList");
		else
			logger.info("Getting statistic results... size: " + statisticsResultList.size());
		return statisticsResultList;
	}

	public void setStatisticsResultList(List<StatisticResults> statisticsResultList) {
		if (statisticsResultList == null)
			logger.warn("Null value for statisticsResultList");
		else
			logger.info("Setting statistic results... size: " + statisticsResultList.size() );
		this.statisticsResultList = statisticsResultList;
	}

	public Map<String, PathwayObjectStatistics> getPathwayDistributionMap() {
		if (pathwayDistributionMap == null)
			logger.warn("Null value for pathwayDistributionMap");
		else
			logger.info("Getting pathwayDistributionMap.... size: " + pathwayDistributionMap.size());
		return pathwayDistributionMap;
	}

	public void setPathwayDistributionMap(Map<String, PathwayObjectStatistics> pathwayDistributionMap) {
		if (pathwayDistributionMap == null)
			logger.warn("Null value for pathwayDistributionMap");
		else
			logger.info("Setting pathwayDistributionMap.... size: " + pathwayDistributionMap.size());
		this.pathwayDistributionMap = pathwayDistributionMap;
	}

	public List<KEGGInteraction> getKeggInteractionList() {
		if (keggInteractionList == null)
			logger.warn("Null value for  keggInteractionList");
		else
			logger.info("Getting keggInteractionList... size: " + keggInteractionList.size());		
		
		return keggInteractionList;
	}

	public void setKeggInteractionList(List<KEGGInteraction> keggInteractionList) {
		if (keggInteractionList == null)
			logger.warn("Unexpected value for keggInteractionList");
		else
			logger.info("Setting keggInteractionList... size: " + keggInteractionList.size());		
		
		this.keggInteractionList = keggInteractionList;
	}

	public boolean isImportKeggInteractions() {
		logger.info("Import KEGG interactions: " +  importKeggInteractions);
		return importKeggInteractions;
	}

	public void setImportKeggInteractions(boolean importKeggInteractions) {
		logger.info("Setting import KEGG interactions: " +  importKeggInteractions);
		this.importKeggInteractions = importKeggInteractions;
	}

	public int getNumerOfaddedEdges() {
		return NumerOfaddedEdges;
	}

	public void setNumerOfaddedEdges(int numerOfaddedEdges) {
		NumerOfaddedEdges = numerOfaddedEdges;
	}

	public boolean isNoFRD() {
		return noFRD;
	}

	public void setNoFRD(boolean noFRD) {
		logger.info("Setting noFRD: " +  noFRD);
		this.noFRD = noFRD;
	}

	public boolean isBonferonni() {
		return bonferonni;
	}

	public void setBonferonni(boolean bonferonni) {
		logger.info("Setting bonferonni: " +  bonferonni);
		this.bonferonni = bonferonni;
	}

	public boolean isBejaminiHoechstFDR() {
		return bejaminiHoechstFDR;
	}

	public void setBejaminiHoechstFDR(boolean bejaminiHoechstFDR) {
		logger.info("Setting bejaminiHoechstFDR: " +  bejaminiHoechstFDR);
		this.bejaminiHoechstFDR = bejaminiHoechstFDR;
	}
	
	
}
