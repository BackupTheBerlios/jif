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
 * Copyright (C) 2004-2006  Alessandro Schillaci
 *
 * WeB   : http://www.slade.altervista.org/JIF/
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
    public static final String JIFVERSION = "Jif 3.3.1 PFP (build 20060925)";
    
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
    public static final String PROJECTEMPTY = "blank";
    
    /**
     * Bookmarks color highlighting
     */
    public static final Color colorBookmarks = new Color(51, 102, 255);
    
    /**
     * Brackets color highlighting
     */
    public static final Color colorBrackets = new Color(255, 153, 51);
    
    /**
     * Errors color highlighting
     */
    public static final Color colorErrors = new Color(255, 102, 102);
    
    /**
     * Jump tree color highlighting
     */
    public static final Color colorJumpto = new Color(102, 153, 255);
    
    /**
     * Warning color highlighting
     */
    public static final Color colorWarnings = new Color(102, 153, 255);
    
    /**
     * Config File Name
     */
    public static final String configFileName = "Jif.cfg";
    
    /**
     * File format used by JIF in read/write action
     */
    public static final String fileFormat = "ISO-8859-1";
    
    // Configuration INI file, read by Regular Expressions

}
