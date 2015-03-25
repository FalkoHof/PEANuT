package age.mpg.de.peanut.gui;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import age.mpg.de.peanut.databases.webservices.DependenciesDownloader;
import age.mpg.de.peanut.gui.icons.IconLoader;
import age.mpg.de.peanut.managers.TaskManagerManager;
import age.mpg.de.peanut.utilityobjects.PluginProperties;

public class DownloaderDialogues extends JDialog{
	
	private DependenciesDownloader downloader;
	private static String titel = PluginProperties.getInstance().getPluginName() + " - Download/update files";
	private JLabel statusIcon;
	
	public DownloaderDialogues(Frame owner){
		
		super(owner,titel, true);

		createDownloaderDialogue();
	}
	
	
	public void createDownloaderDialogue(){
	
		JPanel contentPanel = new JPanel();
		JPanel downloadPanel = new JPanel();
		JPanel closeBtnPanel = new JPanel();
	
		downloadPanel.setBorder(BorderFactory.createTitledBorder("(Re)download resources:"));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
	
		JLabel downloadAll = new JLabel("All files:");
		JLabel downloadCPDB = new JLabel("ConsensusPathDB files:");
		JLabel downloadWP = new JLabel("WikiPathways files:");
	
		JButton downloadAllBtn = new JButton(IconLoader.getInstance().getDownloadIcon());
		JButton downloadCPDBBtn = new JButton(IconLoader.getInstance().getDownloadIcon());
		JButton downloadWPBtn = new JButton(IconLoader.getInstance().getDownloadIcon());
		JButton closeBtn = new JButton("Close");

		downloadAllBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				downloader = new DependenciesDownloader(DependenciesDownloader.DOWNLOAD_ALL);
				TaskManagerManager.getInstance().invokeTask(downloader);
				checkStatus();
			}});
	
		downloadCPDBBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				downloader = new DependenciesDownloader(DependenciesDownloader.DOWNLOAD_CPDB);
				TaskManagerManager.getInstance().invokeTask(downloader);
				checkStatus();
			}});
	
		downloadWPBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				downloader = new DependenciesDownloader(DependenciesDownloader.DOWNLOAD_WP);
				TaskManagerManager.getInstance().invokeTask(downloader);
				checkStatus();
			}});
	
		closeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}});
	
		BoxLayout contentLayout = new BoxLayout(contentPanel, BoxLayout.Y_AXIS);
		GridLayout downloadLayout = new GridLayout(3,2);
		downloadLayout.setHgap(50);
		downloadLayout.setVgap(10);
		GridLayout closeBtnLayout = new GridLayout(0,3);
		closeBtnLayout.setHgap(50);
		
	
		downloadPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1.0;
		c.gridx = 0;
		c.gridy = 0;
		downloadPanel.add(downloadAll,c);
		c.anchor = GridBagConstraints.EAST;

		c.gridx = 1;
		c.gridy = 0;	
		downloadPanel.add(downloadAllBtn,c);
		
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 1;		
		downloadPanel.add(downloadCPDB,c);
		c.anchor = GridBagConstraints.EAST;

		c.gridx = 1;
		c.gridy = 1;
		downloadPanel.add(downloadCPDBBtn,c);
		
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 2;
		downloadPanel.add(downloadWP,c);
		
		c.anchor = GridBagConstraints.EAST;
		c.gridx = 1;
		c.gridy = 2;
		downloadPanel.add(downloadWPBtn,c);
	
		JLabel statusText = new JLabel("Status:");
		
		statusIcon = new JLabel();
		checkStatus();
		
		
		JPanel statusPanel = new JPanel();
		statusPanel.add(statusText, FlowLayout.LEFT);
		statusPanel.add(statusIcon);

		
		closeBtnPanel.setLayout(closeBtnLayout);
		closeBtnPanel.add(Box.createVerticalGlue());
		closeBtnPanel.add(closeBtn);
		closeBtnPanel.add(Box.createVerticalGlue());
	
		contentPanel.setLayout(contentLayout);
		contentPanel.add(statusPanel, Box.TOP_ALIGNMENT);
		contentPanel.add(downloadPanel, Box.CENTER_ALIGNMENT);
		contentPanel.add(closeBtnPanel, Box.BOTTOM_ALIGNMENT);
	
		this.getContentPane().add(contentPanel);
		pack();
		setLocationRelativeTo(null);
		setResizable(false);
		setVisible(true);
	}

	
	private void checkStatus(){
		
		boolean fileCheck = new DependenciesDownloader().checkDependencies();
		
		if (fileCheck)
			statusIcon.setIcon(IconLoader.getInstance().getTickIcon());
		else
			statusIcon.setIcon(IconLoader.getInstance().getCrossIcon());
	}




}
