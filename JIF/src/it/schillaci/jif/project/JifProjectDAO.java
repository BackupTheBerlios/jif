package it.schillaci.jif.project;

/*
 * JifProjectDAO.java
 *
 * This file is part of JIF.
 *
 * Jif is substantially an editor entirely written in java that allows the
 * file management for the creation of text-adventures based on Graham
 * Nelson's Inform standard [a programming language for Interactive Fiction].
 * With Jif, it's possible to edit, compile and run a Text Adventure in
 * Inform format.
 *
 * Copyright (C) 2004-2011  Alessandro Schillaci
 *
 * WeB   : http://www.slade.altervista.org/
 * e-m@il: silver.slade@tiscali.it
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

import it.schillaci.jif.core.JifDAO;
import it.schillaci.jif.core.JifFileName;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import it.schillaci.jif.core.JifDAO;

/**
 * Data access object for project configuration
 *
 * @author Peter Piggott
 * @version 1.0
 * @since JIF 3.2
 */
public class JifProjectDAO {

    /**
     * Type safe enumeration for project file keywords
     */
    public static class Keyword {
        /**
         * Creates a new <code>Keyword</code> with the specified name.
         * 
         * @param name
         *            the name of the keyword
         */
        protected Keyword(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return "JifProjectDAO.Keyword[Name: " + name + "]";
        }

        // JifProject keyword name
        private String name;

        // --- Inform Keyords -----------------------------------------------
        
        private static Keyword FILE   = new Keyword("[FILE]");
        private static Keyword MAIN   = new Keyword("[MAINFILE]");
        private static Keyword SWITCH = new Keyword("[SWITCH]");
        private static Keyword MODE   = new Keyword("[MODE]");


        // JifProject file keywords
        static final Keyword[] keywords = { 
            FILE,
            MAIN,
            MODE,
            SWITCH };

        static {
            // Force JifProjectDAO.Keyword's static initialise to be loaded.
            boolean temp = isKeyword("[MAIN]");
        }
    }

    // Map to hold JifProject file keyords
    private static final Map keywords = new HashMap();

    // Initialisation
    static {
        // JifProject Filee keywords
        for (int i = 0; i < Keyword.keywords.length; i++) {
            keywords.put(Keyword.keywords[i].getName(), Keyword.keywords[i]);
        }
    }

    private static Pattern filePattern   = Pattern.compile("\n\\[FILE\\]([^\n]+)");
    private static Pattern mainPattern   = Pattern.compile("\n\\[MAINFILE\\]([^\n]+)");
    private static Pattern modePattern   = Pattern.compile("\n\\[MODE\\](.+)");
    private static Pattern switchPattern = Pattern.compile("\n\\[SWITCH\\]([^,]+),([^\n]+)");
    
    /** Creates a new instance of JifProjectDAO */
    public JifProjectDAO() {
    }
        
    /**
     * Load a project from persistant storage
     * 
     * @param file
     *              the persistant storage from which to load the project
     * @throws JifProjectException
     */
    public static JifProject load(File file)
            throws JifProjectException {
        
        JifProject project = new JifProject();
        project.setFile(file.getAbsolutePath());
        
        try {
            CharBuffer cb = JifDAO.buffer(file);
            
            Matcher m;
            
            // project files
            m = filePattern.matcher(cb);
            while (m.find()) {
                project.addFile(m.group(1));
            }
            
            // Find the Main class file
            m = mainPattern.matcher(cb);
            while (m.find()) {
                String main = m.group(1);
                if (main.equals("null")) {
                    project.clearMain();
                } else {
                	// Checkfor relative paths
                	File f = new File(project.getProjectDirectory()+File.separator+main);
                	if (f.exists()){
                		project.setMain(new JifFileName(project.getProjectDirectory()+File.separator+main));                		
                	}
                	else{
                		project.setMain(new JifFileName(main));
                	}                	
                }
            }
            
            // Compiler switch settings
            m = switchPattern.matcher(cb);
            while (m.find()) {
                project.addSwitch(m.group(1), m.group(2));
            }
            
            // Inform mode (Inform/Glulx)
            m = modePattern.matcher(cb);
            while (m.find()) {
                project.setInformMode(m.group(1).equalsIgnoreCase("inform")); 
            }
            
        } catch (Exception ex) {
            throw new JifProjectException("Initialise (" + file + "): " + ex.getMessage());
        }
        return project;
    }
    
    /**
     * Store a project object to persistant storage
     * Modified to manage Relative paths
     * 
     * @param project
     *              the <code>JifProject</code> to be stored
     * @throws JifProjectException
     */
    public static void store(JifProject project)
            throws JifProjectException {
        
        File file = new File(project.getFile().getPath());
        
        try {
            StringBuffer output = new StringBuffer();
            
            output.append("# Jif Configuration Project\n");
            output.append("# " + project.getFile().getPath() + "\n");
            output.append("\n");
            Vector files = project.getFiles();
            for (int i=0; i<files.size(); i++) {
                output.append(Keyword.FILE.getName());
                if (isRelativeFile(project,((JifFileName)files.elementAt(i)).getName())){
                	output.append(((JifFileName)files.elementAt(i)).getName()+"\n");
                }
                else{
                	output.append(((JifFileName)files.elementAt(i)).getPath()+"\n");
                }
            }
            output.append(Keyword.MAIN.getName());
            if (project.getMain() == null){
            	output.append("\n");
            }
            else if (isRelativeFile(project,project.getMain().getName())){
            	output.append(project.getMain().getName() + "\n");
            }
            else{
            	output.append(project.getMain().getPath() + "\n");
            }
            output.append(Keyword.MODE.getName() + (project.getInformMode() ? "INFORM":"GLULX") + "\n");
            output.append("\n");
            
            // The project Switches
            output.append("# WARNING! You can edit *only* this switches section!!!\n");
            for (Iterator i = project.getSwitches().keySet().iterator(); i.hasNext(); ) {
                String key = (String) i.next();
                String value = (String) project.getSwitches().get(key);
                output.append(Keyword.SWITCH.getName() + key + "," + value + "\n");
            }
            
            output.append("# WARNING! You can edit *only* this switches section!!!\n");

            JifDAO.save(file, output.toString());
            
        } catch (Exception ex) {
            throw new JifProjectException("Store (" + file + "): " + ex.getMessage());
        }
    }
    
    /**
     * Reloads an existing project object from persistant storage
     *
     * @param project
     *              the <code>JifProject</code> to be reloaded
     * @throws JifProjectException
     */
    public static void reload(JifProject project) 
            throws JifProjectException {

        File file = new File(project.getFile().getPath());
        JifProject newProj = JifProjectDAO.load(file);
        
        project.setFiles(newProj.getFiles());
        project.setInformMode(newProj.getInformMode());
        project.setMain(newProj.getMain());
        project.setSwitches(newProj.getSwitches());

    }
    
    /**
     * Returns <tt>true</tt> if the specified symbol is a JifProject file keyword
     * 
     * @param symbol
     *            Symbol to be tested as a JifProject file keyword
     * @return <tt>true</tt> if this is a JifProject file keyword
     */
    public static boolean isKeyword(String symbol) {
        return keywords.containsKey(symbol);
    }

    /**
     * If the file exists in the directory of file.jpf, it will write without
     * the path.
     * This methods returns true if the relative file exits, false otherwise.
     * 
     * @param project
     * @param absolutepath
     * @return
     */
    public static boolean isRelativeFile(JifProject project, String name){
    	File f = new File(project.getProjectDirectory()+File.separator+name);
    	if (f.exists()){
    		return true;                		
    	}
    	else{
    		return false;
    	}     
    }
}
