/*
 * Constants.java
 *
 * This file is part of JIF.
 *
 * Jif is substantially an editor entirely written in java that allows the
 * file management for the creation of text-adventures based on Graham
 * Nelson's Inform standard [a programming language for Interactive Fiction].
 * With Jif, it's possible to edit, compile and run a Text Adventure in
 * Inform format.
 *
 * Copyright (C) 2004-2005  Alessandro Schillaci
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

import java.awt.Color;
import java.io.File;

/**
 * General Class with some constants
 * @author Alessandro Schillaci
 */
public class Constants {
    
    /**
     * JIF Version
     */
    public static final String JIFVERSION = "3.0 beta 4 (build 20051202)";
    
    /**
     * Max files in the recent file menu
     */
    public static final int RECENTFILES = 5;
    
    /**
     * File Separator, depending on which Operative System is executing JIF
     */
    public static final String SEP = File.separator;
    
    /**
     * MAX dimension of the copy/paste menu
     */
    public static final int MAX_DIMENSION_PASTE_MENU = 10;
    
    /**
     * Token for mark a line as a comment
     */
    public static final String TOKENCOMMENT = "#";
    
    /**
     * Token for mark a line in the search function in all project files
     */
    public static final String TOKENSEARCH = "*";
    
    /**
     * Token for mark a line as a command
     */
    public static final String TOKENCOMMAND = "[execute]=";
    
    /**
     * Empty project
     */
    public static final String PROJECTEMPTY="blank";
    
    /**
     * User dir /home/xxx/.jif or c:\documents and settings\xxx\.jif
     */
    public static final String userDir=System.getProperty("user.home")+SEP+".jif"+SEP;
    
    /**
     * Errors color highlightning
     */
    public static final Color colorErrors = new Color(255,102,102);
    
    /**
     * Warning color highlightning
     */
    public static final Color colorWarnings = new Color(102,153,255);

    /**
     * Jump tree color highlighting
     */
    public static final Color colorJumpto = new Color(102,153,255);    
    
    /**
     * File format used by JIF in read/write action
     */
    public static final String fileFormat = "ISO-8859-1";   
    
    /**
     * Token for mark a line as [ALTKEYS]
     */
    public static final String ALTKEYSTOKEN = "[ALTKEYS]";    
    
    /**
     * Token for mark a line as [HELPEDCODE]
     */
    public static final String HELPEDCODETOKEN = "[HELPEDCODE]";   
        /**
     * Token for mark a line as [MAPPING]
     */
    public static final String MAPPINGTOKEN = "[MAPPING]";   
        /**
     * Token for mark a line as [MENU]
     */
    public static final String MENUTOKEN = "[MENU]";   
        /**
     * Token for mark a line as [RECENTFILES]
     */
    public static final String RECENTFILESTOKEN = "[RECENTFILES]";   
        /**
     * Token for mark a line as [SWITCH]
     */
    public static final String SWITCHTOKEN = "[SWITCH]";   
    
    /**
     * Token for mark a line as [SYNTAX]
     */
    public static final String SYNTAXTOKEN = "[SYNTAX]";   
    
    /**
     * Token for mark a line as [SYMBOLS]
     */
    public static final String SYMBOLSTOKEN = "[SYMBOLS]";  
    /**
     * Token for mark a line as [PATH]
     */
    public static final String PATHTOKEN = "[PATH]";  
    /**
     * Token for mark a line as [SETTINGS]
     */
    public static final String SETTINGSTOKEN = "[SETTINGS]";  
   
}
