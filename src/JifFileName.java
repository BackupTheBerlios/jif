/*
 * JifFileName.java
 *
 * This file is part of JIF.
 *
 * Jif is substantially an editor entirely written in java that allows the
 * file management for the creation of text-adventures based on Graham
 * Nelson's Inform standard [a programming language for Interactive Fiction].
 * With Jif, it's possible to edit, compile and run a Text Adventure in
 * Inform format.
 *
 * Copyright (C) 2004-2006  Alessandro Schillaci
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

import java.util.HashMap;
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.text.EditorKit;

/**
 * JifFileName: Immutable data class for JIF file information that provides
 * convenience methods for accessing the type, directory, name and content type 
 * from the absolute path of the file.
 * 
 * 
 * 
 * 
 * @author Peter Piggott
 * @version 1.0
 * @since JIF 3.2
 */
public class JifFileName implements Comparable {
    
    // Absolute path of file
    private String path;

    // Name of the file, without the directory
    private String name;

    // Directory for the file, without the file name
    private String directory;

    // File type
    private String type;

    // Content type
    private String content;

    /**
     * Constructor to create a new JifFileName from an path directory string.
     * 
     * 
     * @param absolutePath
     *            file path directory - any quotes are removed.
     */
    public JifFileName(String absolutePath) {
        path = Utils.replace(absolutePath, "\'", "");
        path = Utils.replace(path, "\"", "");
        name = path.substring(path.lastIndexOf(Constants.SEP) + 1, path.length());
        directory = path.substring(0, path.lastIndexOf(Constants.SEP));
        type = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
        content = getContentTypeForFileType(type);
    }
    
    /**
     * Creates a jif editor kit based on the type of the file 
     *
     * @return suitable jif editor kit for file
     */
    public JifEditorKit createEditorKit() {
        return createEditorKitForFileType(type);
    }

    public String getPath() {
        return path;
    }
    
    public String getContentType() {
        return content;
    }

    public String getName() {
        return name;
    }
    
    public String getDirectory() {
        return directory;
    }
    
    public String getType() {
        return type;
    }
    
    // --- Object methods ------------------------------------------------------
    
    public String toString() {
        return path;
    }

    public boolean equals(Object o) {
        if (!(o instanceof JifFileName)) {
            return false;
        }
        JifFileName jf = (JifFileName) o;
        return path.equals(jf.getPath());
    }
    
    public int hashCode() {
        return path.hashCode();
    }

    // --- Comparable implementation -------------------------------------------
    
    public int compareTo(Object o) {
        JifFileName jf = (JifFileName) o;
        
        // Compare names
        int compare = name.compareTo(jf.getName());
        
        if (compare != 0) {
            return compare;
        }
        
        // Names are equal, compare paths
        return directory.compareTo(jf.getDirectory());
    }

    // --- Class variables -----------------------------------------------------
    
    // Map of file types to content type
    private static final Map fileAssociation;
    
    // Constants for editor kits
    public static final String CONFIGEDITOR   = "JifConfigurationEditorKit";
    public static final String INFORMEDITOR   = "InformEditorKit";
    public static final String PLAINEDITOR    = "JifEditorKit";
    public static final String PROJECTEDITOR  = "JifProjectEditorKit";
    public static final String RESOURCEEDITOR = "ResourceEditorKit";
    
    // Constants for content types
    public static final String CONFIG   = "text/config";
    public static final String INFORM   = "text/inform";
    public static final String PLAIN    = "text/plain";
    public static final String PROJECT  = "text/project";
    public static final String RESOURCE = "text/resource";
    
    // Constants for file types
    private static final String ANY = "*";
    private static final String CFG = "cfg";
    private static final String H   = "h";
    private static final String INF = "inf";
    private static final String JPF = "jpf";
    private static final String RES = "res";
    private static final String TXT = "txt";
    
    // --- Class methods -------------------------------------------------------
    
    // Builds a default map of file associations and registars jif's editor kits
    static {
        fileAssociation = new HashMap();
        fileAssociation.put(ANY, PLAIN);
        fileAssociation.put(CFG, CONFIG);
        fileAssociation.put(H,   INFORM);
        fileAssociation.put(INF, INFORM);
        fileAssociation.put(JPF, PROJECT);
        fileAssociation.put(RES, RESOURCE);
        fileAssociation.put(TXT, PLAIN);
        
        JEditorPane.registerEditorKitForContentType(CONFIG,   CONFIGEDITOR);
        JEditorPane.registerEditorKitForContentType(INFORM,   INFORMEDITOR);
        JEditorPane.registerEditorKitForContentType(PLAIN,    PLAINEDITOR);
        JEditorPane.registerEditorKitForContentType(PROJECT,  PROJECTEDITOR);
        JEditorPane.registerEditorKitForContentType(RESOURCE, RESOURCEEDITOR);
    }

    /**
     * Registars an association from a file type to a content type.
     * 
     * @param type the file extension
     * @param content
     */
    public static void registarContentTypeForFileType(String type, String content) {
        fileAssociation.put(type, content);
    }
    
    /**
     * Returns the content type for a file type.
     *
     * @param type the file type to look up
     * @returns the content type
     */
    public static String getContentTypeForFileType(String type) {
        
        if (fileAssociation.containsKey(type)) {
            return (String) fileAssociation.get(type);
        }
        
        return (String) fileAssociation.get(ANY);        
    }
    
    /**
     * Returns the jif editor kit for a file type.
     *
     * @param type
     *              the file type to look up
     * @returns the jif editor kit
     */
    public static JifEditorKit createEditorKitForFileType(String type) {
        
        return (JifEditorKit) JEditorPane.createEditorKitForContentType(getContentTypeForFileType(type));
    }
    
}
