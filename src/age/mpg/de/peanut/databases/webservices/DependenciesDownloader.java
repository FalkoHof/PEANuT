package age.mpg.de.peanut.databases.webservices;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;

import age.mpg.de.peanut.managers.TaskManagerManager;
import age.mpg.de.peanut.model.PeanutModel;
import age.mpg.de.peanut.utilityobjects.PluginProperties;

import cytoscape.logger.CyLogger;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

public class DependenciesDownloader implements Task {
	
	private boolean interrupted = false;
	private cytoscape.task.TaskMonitor taskMonitor;
	private CyLogger logger = CyLogger.getLogger(this.getClass());
	
	private String[] consensusPathDBFileArr = PluginProperties.getInstance().getConsensusPathDBFiles();
	private String consensusPathDBDirectory = PluginProperties.getInstance().getConsensusPathDBDirectory();
	private String[] consensusPathDBURLs = PluginProperties.getInstance().getConsensusPathDBWebResource();

	private String wikiPathwaysDirectory = PluginProperties.getInstance().getWikiPathwaysDirectory();
	private String wikiPathwaysURL = PluginProperties.getInstance().getWpWebResource();
	private String wikiPathwaysFile = PluginProperties.getInstance().getWpFile();
	
	private List<File> fileList = new ArrayList<File>();
	
	public static final byte DOWNLOAD_ALL = 0, DOWNLOAD_WP = 1,DOWNLOAD_CPDB = 2;
	
	private byte parameter = -1;
	
	public DependenciesDownloader(byte parameter){
		this.parameter = parameter;
	}
	
	public DependenciesDownloader(){
		
		
	}
	
	@Override
	public void run() {
		
		try{
			switch(parameter){
				case DOWNLOAD_ALL:	downloadAll();
					break;
				case DOWNLOAD_CPDB:	downloadConsensuPathDB();
					break;
				case DOWNLOAD_WP:	downloadWikiPathways();
					break;
				default:			downloadAll();	
			}
			JOptionPane.showMessageDialog(null, "Files successfully downloaded!","PathwayFinder - Download/update files", JOptionPane.INFORMATION_MESSAGE);							
		} catch(IOException e){
			JOptionPane.showMessageDialog(null, "An error occoured while downloading.\nSee the project wiki for help on how to manually download the files", "PathwayFinder - Download/update files", JOptionPane.ERROR_MESSAGE);
			logger.warn("An error occoured while downloading", e);
		}
		
	}
	
	public boolean checkDependenciesWithDialog() throws FileNotFoundException{
		 checkConsensusPathDbFiles();
		 checkWikiPathwayFiles();
		 
		for (File f : fileList)
			if (!f.exists()){
				PeanutModel.getInstance().setExit(true);
				JOptionPane.showMessageDialog(null, f.toString() + "not found.\nRun the dependencies downloader first.","PathwayFinder", JOptionPane.ERROR_MESSAGE);
				throw new FileNotFoundException(f.toString() + "is missing");		
			}
		return true;
	}
	
	public boolean checkDependencies(){
		 checkConsensusPathDbFiles();
		 checkWikiPathwayFiles();
		for (File f : fileList)
			if (!f.exists() || !f.canRead())
				return false;
		return true;
	}
	
	
	
	private void checkConsensusPathDbFiles(){
		File cpdbDir = new File(consensusPathDBDirectory);
		for (String s : consensusPathDBFileArr)
			fileList.add(new File(cpdbDir,s));
	}
	
	private void checkWikiPathwayFiles(){
		File wpDir = new File(wikiPathwaysDirectory);
		fileList.add(new File(wpDir,wikiPathwaysFile));
		
		//for (String s : wikiPathwaysFileArr)
		//	fileList.add(new File(wpDir,s));
	}

	private void downloadAll() throws IOException{
		downloadConsensuPathDB();
		downloadWikiPathways();
	}
	
	private void downloadConsensuPathDB() throws IOException{
		taskMonitor.setStatus("Downloding ConsensusPathDB files..");
		File cpdbDir = new File(consensusPathDBDirectory);
		if (!cpdbDir.exists())
			cpdbDir.mkdirs();
		
		for (int i = 0; i < consensusPathDBFileArr.length;i++){
			//break statement for cancel option
			if (interrupted){
				PeanutModel.getInstance().setExit(true);
				break;
			}
			taskMonitor.setPercentCompleted(TaskManagerManager.getInstance().getPercentage(i, consensusPathDBFileArr.length));
			copyFileFromUrl(new URL(consensusPathDBURLs[i]), new File(consensusPathDBDirectory,consensusPathDBFileArr[i]));
		}
	}	
	
	private void downloadWikiPathways() throws IOException{
		taskMonitor.setStatus("Downloding WikiPathways files..");
		File wpDir = new File(wikiPathwaysDirectory);
		if (!wpDir.exists())
			wpDir.mkdirs();
		/*
		int counter = 0;
		for (String s : wikiPathwaysFileArr){
			//break statement for cancel option
			if (interrupted){
				PeanutModel.getInstance().setExit(true);
				break;
			}
			taskMonitor.setPercentCompleted(TaskManagerManager.getInstance().getPercentage(counter, wikiPathwaysFileArr.length));
	
			copyFileFromUrl(new URL(wikiPathwaysURL + s), new File(wpDir,s));
			*/
		copyFileFromUrl(new URL(wikiPathwaysURL + wikiPathwaysFile), new File(wpDir,wikiPathwaysFile));
		//}
	}

	
	private void copyFileFromUrl(URL source, File destination) throws IOException{
		taskMonitor.setStatus("Downloding: " + destination.getName());
		FileUtils.copyURLToFile(source, destination);
		System.out.println(destination.getName() + " downloaded from url: " + source.toString());
		logger.info(destination.getName() + " downloaded from url: " + source.toString());
	}

	@Override
	public String getTitle() {
        return "Updating/Downloading dependencies...";
	}

	@Override
	public void halt() {
        this.interrupted = true;	
	}

	@Override
	 public void setTaskMonitor(TaskMonitor taskMonitor) throws IllegalThreadStateException {
		this.taskMonitor = taskMonitor;
	}
}
