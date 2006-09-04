/*
 * JifProject.java
 *
 * This projectFile is part of JIF.
 *
 * Jif is substantially an editor entirely written in java that allows the
 * projectFile management for the creation of text-adventures based on Graham
 * Nelson's Inform standard [a programming language for Interactive Fiction].
 * With Jif, it's possible to edit, compile and run a Text Adventure in
 * Inform format.
 *
 * Copyright (C) 2003-2006  Alessandro Schillaci
 *
 * WeB   : http://www.slade.altervista.org/JIF/
 * e-m@il: silver.slade@tiscalinet.it
 *
 * Jif is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Jif; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

/**
 * JifProject: Class for project information within Jif.
 *
 * @author Peter Piggott
 * @version 1.0
 * @since JIF 3.2
 */
public class JifProject {

    // Project observers
    private ArrayList observers = new ArrayList();
    
    // Project save file
    private JifFileName projectFile = null;
    
    // Main file for project compilation
    private JifFileName mainFile    = null;
    
    // Files contained in the project
    private Vector files        = new Vector();
    
    // Switches
    private Map switches        = new LinkedHashMap();
    
    // Mode
    private boolean informMode  = true;
    
    // Actual Directory of the project (for the relative paths management)
    private String projectDirectory = null;
    
    /** Creates a new instance of JifProject */
    public JifProject() {
    }

    public void addFile(String filePath) {
        // check for relative paths    	
    	File f = new File(projectFile.getDirectory()+File.separator+filePath);
    	JifFileName file = null;
    	if (f.exists()){
    		file = new JifFileName(projectFile.getDirectory()+File.separator+filePath);	
    	}
    	else{
    		file = new JifFileName(filePath);
    	}
    	
        files.add(file);
        Collections.sort(files);
        notifyObservers();
    }
    
    public void removeFile(JifFileName file) {
        files.remove(file);
        if (file.equals(mainFile)) {
            mainFile = null;
        }
        notifyObservers();
    }
    
    public void clearMain() {
        mainFile = null;
        notifyObservers();
    }
    
    public void clear() {
        projectFile = null;
        mainFile = null;
        files.removeAllElements();
        notifyObservers();
    }
    
    // --- Accessor methods ----------------------------------------------------
    
    public JifFileName getFile() {
        return projectFile;
    }

    public void setFile(String projectFilePath) {
        JifFileName projectFile = new JifFileName(projectFilePath);
        this.projectFile = projectFile;
        this.setProjectDirectory(projectFile.getDirectory());
        notifyObservers();
    }
    
    public Vector getFiles() {
        return (Vector) files.clone();
    }
    
    public void setFiles(Vector files) {
        this.files = files;
        Collections.sort(files);
        notifyObservers();
    }
    
    public boolean getInformMode() {
        return informMode;
    }
    
    public void setInformMode(boolean informMode) {
        this.informMode = informMode;
    }
    
    public JifFileName getMain() {
        return mainFile;
    }
    
    public void setMain(JifFileName mainFile) {
        this.mainFile = mainFile;
        notifyObservers();
    }
    
    public void addSwitch(String switchName, String setting) {
        switches.put(switchName, setting);
    }
    
    public Map getSwitches() {
        return switches;
    }

    public void setSwitch(String switchName, String setting) {
        if (switches.containsKey(switchName)) {
            switches.put(switchName, setting);
        }
    }
    
    public void setSwitches(Map switches) {
        this.switches.clear();
        this.switches.putAll(switches);
    }
    
    
    // --- Project observer methods --------------------------------------------
    
    public void registerObserver(JifProjectObserver o) {
        observers.add(o);
    }

    public void notifyObservers() {
        for (int i=0; i<observers.size(); i++) {
            JifProjectObserver observer = (JifProjectObserver) observers.get(i);
            observer.updateProject();
        }
    }

    public void removeObserver(JifProjectObserver o) {
        int i = observers.indexOf(o);
        if (i >= 0) {
            observers.remove(i);
        }
    }

	public String getProjectDirectory() {
		return projectDirectory;
	}

	public void setProjectDirectory(String projectDirectory) {
		this.projectDirectory = projectDirectory;
	}
    
}
