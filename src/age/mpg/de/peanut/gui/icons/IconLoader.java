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
package age.mpg.de.peanut.gui.icons;

import javax.swing.ImageIcon;

public class IconLoader {
	
	private static IconLoader instance = new IconLoader();
	private ImageIcon ACCEPT_IGM;
	private ImageIcon CANCEL_IMG;
	private ImageIcon TICK_IMG;
	private ImageIcon CROSS_IMG;
	private ImageIcon DOWNLOAD_IGM;
	private ImageIcon REFRESH_IMG;
	private ImageIcon PLAY_IMG;
	
	/**
	 * Constructor, private because of singleton
	 */
	private IconLoader() {
	}

	/**
	 * Get the current instance
	 * @return IconLoader
	 */
	public static IconLoader getInstance() {
		return instance;
	}
	
	
	
	public ImageIcon getPlayIcon(){
		if(PLAY_IMG == null)
			PLAY_IMG = new ImageIcon(getClass().getResource("control_play_blue.png"));
		return PLAY_IMG;
	}
	
	public ImageIcon getRefreshIcon(){
		if(REFRESH_IMG==null)
			REFRESH_IMG = new ImageIcon(getClass().getResource("arrow_refresh.png"));
		return REFRESH_IMG;
	}
	
	
	public ImageIcon getDownloadIcon(){
		if(DOWNLOAD_IGM == null)
			DOWNLOAD_IGM = new ImageIcon(getClass().getResource("download.png"));
		return DOWNLOAD_IGM;
	}
	
	
	public ImageIcon getAcceptIcon(){
		if(ACCEPT_IGM == null)
			ACCEPT_IGM = new ImageIcon(getClass().getResource("accept.png"));
		return ACCEPT_IGM;
	}

	public ImageIcon getCancelIcon() {
		if (CANCEL_IMG == null)
			CANCEL_IMG = new ImageIcon(getClass().getResource("cancel.png"));

		return CANCEL_IMG;
	}

	public ImageIcon getTickIcon() {
		if (TICK_IMG == null)
			TICK_IMG = new ImageIcon(getClass().getResource("tick.png"));

		return TICK_IMG;
	}

	public ImageIcon getCrossIcon() {
		if (CROSS_IMG == null)
			CROSS_IMG = new ImageIcon(getClass().getResource("cross.png"));

		return CROSS_IMG;
	}
	
}
