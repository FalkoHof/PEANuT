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
package age.mpg.de.peanut;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import age.mpg.de.peanut.gui.DownloaderDialogues;
import age.mpg.de.peanut.gui.ResultDialogues;
import age.mpg.de.peanut.gui.StartDialogues;
import age.mpg.de.peanut.utilityobjects.PluginProperties;

import cytoscape.Cytoscape;
import cytoscape.logger.CyLogger;


//class that creates the cytoscape menu entry and registers listeners
public class PeanutPlugin {	
	private CyLogger logger = CyLogger.getLogger(this.getClass());
	
	public PeanutPlugin(){		
			
		
		JMenu pluginMenu = new JMenu(PluginProperties.getInstance().getPluginName());
		logger.info("JMenu entry added");
			
		// create 2 submenu entries
		JMenuItem importMenuItem = new JMenuItem("Find pathways");			
		JMenuItem statisticsMenuItem = new JMenuItem("Show pathway statistics");
		JMenuItem downloadMenuItem = new JMenuItem("Download/update dependencies");


		//add the 2 submenus
		pluginMenu.add(importMenuItem);
		pluginMenu.add(statisticsMenuItem);
		pluginMenu.add(downloadMenuItem);
		logger.info("JMenu subentries added");

		//add listener for start dialogues
		importMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.info("Starting data import menu");
				new StartDialogues(Cytoscape.getDesktop());
		}});

		//add listener for result dialogues
		statisticsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.info("Starting statistics menu");
				new ResultDialogues(Cytoscape.getDesktop());
		}});
		
		
		downloadMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logger.info("Starting download menu");
				new DownloaderDialogues(Cytoscape.getDesktop());
		}});
			
		Cytoscape.getDesktop().getCyMenus().getOperationsMenu().add(pluginMenu);
	}
}	
