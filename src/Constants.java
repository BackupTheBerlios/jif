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
    public static final String JIFVERSION = "3.0 beta 9 (build 20060116)";
    
    /**
     * Max files in the recent file menu
     */
    public static final int MAXRECENTFILES = 15;
    
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
     * Empty project
     */
    public static final String PROJECTEMPTY="blank";
    
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
     * Config File Name
     */
    public static final String configFileName = "Jif.cfg";
    
   /**
    * File format used by JIF in read/write action
    */
    public static final String fileFormat = "ISO-8859-1";

    // Configuration INI file, read by Regular Expressions
    public static final String ALTKEYSTOKEN              = "\\[ALTKEYS\\]";
    public static final String EXECUTETOKEN              = "\\[EXECUTE\\]";
    public static final String HELPEDCODETOKEN           = "\\[HELPEDCODE\\]";
    public static final String MAPPINGTOKEN              = "\\[MAPPING\\]";
    public static final String MENUTOKEN                 = "\\[MENU\\]";
    public static final String RECENTFILESTOKEN          = "\\[RECENTFILES\\]";
    public static final String SWITCHTOKEN               = "\\[SWITCH\\]";
    public static final String SYNTAXTOKEN               = "\\[SYNTAX\\]";
    public static final String SYMBOLSTOKEN              = "\\[SYMBOLS\\]";
    public static final String SETTINGSTOKEN             = "\\[SETTINGS\\]";
    public static final String LIBRAYPATHTOKEN           = "\\[LIBRAYPATH\\]";
    public static final String LIBRAYPATHSECONDARY1TOKEN = "\\[LIBRAYPATHSECONDARY1\\]";
    public static final String LIBRAYPATHSECONDARY2TOKEN = "\\[LIBRAYPATHSECONDARY2\\]";
    public static final String LIBRAYPATHSECONDARY3TOKEN = "\\[LIBRAYPATHSECONDARY3\\]";
    public static final String COMPILEDPATHTOKEN         = "\\[COMPILEDPATH\\]";
    public static final String INTERPRETERZCODEPATHTOKEN = "\\[INTERPRETERZCODEPATH\\]";
    public static final String INTERPRETERGLULXPATHTOKEN = "\\[INTERPRETERGLULXPATH\\]";
    public static final String COMPILERPATHTOKEN         = "\\[COMPILERPATH\\]";
    public static final String BRESPATHTOKEN             = "\\[BRESPATH\\]";
    public static final String BLCPATHTOKEN              = "\\[BLCPATH\\]";
    public static final String WRAPLINESTOKEN            = "\\[WRAPLINES\\]";
    public static final String SYNTAXCHECKTOKEN          = "\\[SYNTAXCHECK\\]";
    public static final String HELPEDCODECHECKTOKEN      = "\\[HELPEDCODECHECK\\]";
    public static final String MAPPINGCODETOKEN          = "\\[MAPPINGCODE\\]";
    public static final String NUMBERLINESTOKEN          = "\\[NUMBERLINES\\]";
    public static final String PROJECTSCANFORCLASSESTOKEN= "\\[PROJECTSCANFORCLASSES\\]";
    public static final String PROJECTOPENALLFILESTOKEN  = "\\[PROJECTOPENALLFILES\\]";
    public static final String USECOMPILEDPATHTOKEN      = "\\[USECOMPILEDPATH\\]";
    public static final String OPENLASTFILETOKEN         = "\\[OPENLASTFILE\\]";
    public static final String CREATENEWFILETOKEN        = "\\[CREATENEWFILE\\]";
    public static final String MAKEALWAYSRESOURCETOKEN   = "\\[MAKEALWAYSRESOURCE\\]";
    public static final String TABSIZETOKEN              = "\\[TABSIZE\\]";
    public static final String COLORKEYWORDTOKEN         = "\\[COLORKEYWORD\\]";
    public static final String COLORATTRIBUTETOKEN       = "\\[COLORATTRIBUTE\\]";
    public static final String COLORPROPERTYTOKEN        = "\\[COLORPROPERTY\\]";
    public static final String COLORVERBTOKEN            = "\\[COLORVERB\\]";
    public static final String COLORNORMALTOKEN          = "\\[COLORNORMAL\\]";
    public static final String COLORCOMMENTTOKEN         = "\\[COLORCOMMENT\\]";
    public static final String COLORBACKGROUNDTOKEN      = "\\[COLORBACKGROUND\\]";
    public static final String DEFAULTFONTTOKEN          = "\\[DEFAULTFONT\\]";
    public static final String LOCATIONXTOKEN            = "\\[LOCATIONX\\]";
    public static final String LOCATIONYTOKEN            = "\\[LOCATIONY\\]";
    public static final String WIDTHTOKEN                = "\\[WIDTH\\]";
    public static final String HEIGHTTOKEN               = "\\[HEIGHT\\]";
    public static final String MODETOKEN                 = "\\[MODE\\]";
    public static final String OUTPUTTOKEN               = "\\[OUTPUT\\]";
    public static final String JTOOLBARTOKEN             = "\\[JTOOLBAR\\]";
    public static final String JTREETOKEN                = "\\[JTREE\\]";
    public static final String DIVIDER1TOKEN             = "\\[DIVIDER1\\]";
    public static final String DIVIDER3TOKEN             = "\\[DIVIDER3\\]";
    public static final String LASTFILETOKEN             = "\\[LASTFILE\\]";
    public static final String LASTPROJECTTOKEN          = "\\[LASTPROJECT\\]";
    
}
