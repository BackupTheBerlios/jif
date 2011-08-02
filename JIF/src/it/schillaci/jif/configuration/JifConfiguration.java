package it.schillaci.jif.configuration;

/*
 * JifConfiguration.java
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
 * WeB   : http://www.slade.altervista.org/
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

import it.schillaci.jif.core.Constants;
import it.schillaci.jif.inform.InformContext;
import it.schillaci.jif.core.JifFileName;
import it.schillaci.jif.core.LRUCache;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

/**
 * JifConfiguration: Class for configuration information within Jif.
 *
 * @author Peter Piggott
 * @version 1.0
 * @since JIF 3.2
 */
public class JifConfiguration {

    // Dummy object for recent file map
    private static final Object PRESENT   = new Object();
    // Configuration observers
    private ArrayList observers           = new ArrayList();
    // Configuration save file
    private JifFileName configurationFile = null;
    // Working directory
    private String workingDirectory       = "";
    // Maps
    private Map altKeys                   = new HashMap();
    private Map executeCommands           = new HashMap();
    private Map helpCodes                 = new HashMap();
    private Map mappings                  = new HashMap();
    private Map menus                     = new LinkedHashMap();
    private Map operations                = new HashMap();
    private Map switches                  = new LinkedHashMap();
    // Least recently used cache
    private Map recentFiles                = new LRUCache(Constants.MAXRECENTFILES);
    // Sets
    private Set attributes                 = new HashSet();
    private Set keywords                   = new HashSet();
    private Set properties                 = new HashSet();
    private Set symbols                    = new HashSet();
    private Set verbs                      = new HashSet();
    // Context - syntax highlighting color and font
    private InformContext context          = new InformContext();
    // Executeables
    private JifFileName blc                = null;
    private JifFileName bres               = null;
    private JifFileName compiler           = null;
    private JifFileName interpreterGlulx   = null;
    private JifFileName interpreterZcode   = null;
    // Paths
    private JifFileName game               = null;
    private JifFileName library []         = {null, null, null, null};
    // History
    private JifFileName lastFile           = null;
    private JifFileName lastInsert         = null;
    private JifFileName lastProject        = null;
    // Control settings
    private boolean adventInLib            = false;
    private boolean createNewFile          = false;
    private boolean helpedCode             = true;
    private boolean informMode             = true;
    private boolean makeResource           = true;
    private boolean mappingLive            = true;
    private boolean numberLines            = true;
    private boolean openLastFile           = false;
    private boolean openProjectFiles       = false;
    private boolean scanProjectFiles       = false;
    private boolean syntaxHighlighting     = true;
    private boolean wrapLines              = false;
    // Window settings
    private boolean fullscreen             = false;
    private boolean output                 = true; 
    private boolean toolbar                = true;
    private boolean tree                   = true; 
    // Window dividers
    private int divider1                   = 0;
    private int divider3                   = 0;
    // Window location
    private int frameX                     = 0;
    private int frameY                     = 0;
    // Window size
    private int frameHeight                = 0;
    private int frameWidth                 = 0;
    // Tab size
    private int tabSize                    = 4;
    
    /** Creates a new instance of JifConfiguration */
    public JifConfiguration() {
    }
    
    // --- Accessor methods ----------------------------------------------------

    public JifFileName getFile() {
        return configurationFile;
    }
    
    public void setFile(String configurationFilePath) {
        JifFileName configurationFile = new JifFileName(configurationFilePath);
        this.configurationFile = configurationFile;
    }
    
    // ---
    
    public String getWorkingDirectory() {
        return workingDirectory;
    }
    
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
    
    // ---
    
    public void addAltKey(String key, String translation) {
        altKeys.put(key, translation);
    }
    
    public Map getAltKeys() {
        return Collections.unmodifiableMap(altKeys);
    }
    
    public Set getAltKeysAlpha() {
        TreeMap alpha = new TreeMap();
        alpha.putAll(altKeys);
        return alpha.keySet();
    }
    
    public void setAltKeys(Map altKeys) {
        this.altKeys.clear();
        this.altKeys.putAll(altKeys);
    }
    
    // ---
    
    public void addExecuteCommand(String key, String command) {
        executeCommands.put(key, command);
    }
    
    public Map getExecuteCommands() {
        return Collections.unmodifiableMap(executeCommands);
    }
    
    public Set getExecuteCommandsAlpha() {
        TreeMap alpha = new TreeMap();
        alpha.putAll(executeCommands);
        return alpha.keySet();
    }
    
    public void setExecuteCommands(Map executeCommands) {
        this.executeCommands.clear();
        this.executeCommands.putAll(executeCommands);
    }
    
    // ---
    
    public void addHelpCode(String key, String translation) {
        helpCodes.put(key, translation);
    }
    
    public Map getHelpCodes() {
        return Collections.unmodifiableMap(helpCodes);
    }
    
    public Set getHelpCodesAlpha() {
        TreeMap alpha = new TreeMap();
        alpha.putAll(helpCodes);
        return alpha.keySet();
    }
    
    public void setHelpCodes(Map helpCode) {
        this.helpCodes.clear();
        this.helpCodes.putAll(helpCode);
    } 

    // ---
    
    public void addMapping(String character, String translation) {
        mappings.put(character, translation);
    }
    
    public Map getMapping() {
        return Collections.unmodifiableMap(mappings);
    }
    
    public Set getMappingAlpha() {
        TreeMap alpha = new TreeMap();
        alpha.putAll(mappings);
        return alpha.keySet();
    }
    
    public void setMapping(Map mapping) {
        this.mappings.clear();
        this.mappings.putAll(mapping);
    }
    
    // ---
    
    public void addMenu(String menu) {
        menus.put(menu, new LinkedHashSet());
        notifyObservers();
    }
    
    public Map getMenus() {
        return Collections.unmodifiableMap(menus);
    }
    
    public Set getMenusSet() {
        return Collections.unmodifiableSet(menus.keySet());
    }
    
    public void setMenus(Map menus) {
        this.menus.clear();
        this.menus.putAll(menus);
        notifyObservers();
    }
    
    // ---
    
    public void addOperation(String subMenu, String translation) {
        operations.put(subMenu, translation);
    }
    
    public Map getOperations() {
        return Collections.unmodifiableMap(operations);
    }
    
    public void setOperations(Map operations) {
        this.operations.clear();
        this.operations.putAll(operations);
    }
    
    // ---
    
    public void addSubMenu(String menu, String subMenu) {
        LinkedHashSet subMenus = (LinkedHashSet) menus.get(menu);
        subMenus.add(subMenu);
    }
    
    public Set getSubMenu(String menu) {
        return Collections.unmodifiableSet((LinkedHashSet) menus.get(menu));
    }
    
    // ---
    
    public void addSwitch(String switchName, String setting) {
        switches.put(switchName, setting);
    }
    
    public Map getSwitches() {
        return Collections.unmodifiableMap(switches);
    }
    
    public Set getSwitchesSet() {
        return Collections.unmodifiableSet(switches.keySet());
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
    
    // ---
    
    public void addRecentFile(String file) {
        if (recentFiles.containsKey(file)) {
            return;
        }
        recentFiles.put(file, PRESENT);
        notifyObservers();
    }
    
    public Set getRecentFilesSet() {
        return Collections.unmodifiableSet(recentFiles.keySet());
    }
    
    public Map getRecentFiles() {
        return Collections.unmodifiableMap(recentFiles);
    }
    
    public void clearRecentFiles() {
        recentFiles.clear();
    }
    
    public void setRecentFiles(Map recentFiles) {
        clearRecentFiles();
        this.recentFiles.putAll(recentFiles);
        notifyObservers();
    }
    
    // ---
    
    public void addAttribute(String attribute) {
        attributes.add(attribute);
    }

    public Set getAttributes() {
        return Collections.unmodifiableSet(attributes);
    }
    
    public SortedSet getAttributesAlpha() {
        TreeSet alpha = new TreeSet();
        alpha.addAll(attributes);
        return alpha;
    }
    
    public void setAttributes(Set attributes) {
        this.attributes.clear();
        this.attributes.addAll(attributes);
    }
    
    // ---
    
    public void addKeyword(String keyword) {
        keywords.add(keyword);
    }
    
    public Set getKeywords() {
        return Collections.unmodifiableSet(keywords);
    }
    
    public SortedSet getKeywordsAlpha() {
        TreeSet alpha = new TreeSet();
        alpha.addAll(keywords);
        return alpha;
    }
    
    public void setKeywords(Set keywords) {
        this.keywords.clear();
        this.keywords.addAll(keywords);
    }
    
    // ---
    
    public void addProperty(String property) {
        properties.add(property);
    }
    
    public Set getProperties() {
        return Collections.unmodifiableSet(properties);
    }
    
    public SortedSet getPropertiesAlpha() {
        TreeSet alpha = new TreeSet();
        alpha.addAll(properties);
        return alpha;
    }
    
    public void setProperties(Set properties) {
        this.properties.clear();
        this.properties.addAll(properties);
    }
    
    // ---
    
    public void addSymbol(String symbol) {
        symbols.add(symbol);
    }
    
    public Set getSymbols() {
        return Collections.unmodifiableSet(symbols);
    }
    
    public SortedSet getSymbolsAlpha() {
        TreeSet alpha = new TreeSet();
        alpha.addAll(symbols);
        return alpha;
    }
    
    public void setSymbols(Set symbols) {
        this.symbols.clear();
        this.symbols.addAll(symbols);
    }
    
    // ---
    
    public void addVerb(String verb) {
        verbs.add(verb);
    }
    
    public Set getVerbs() {
        return Collections.unmodifiableSet(verbs);
    }
    
    public SortedSet getVerbsAlpha() {
        TreeSet alpha = new TreeSet();
        alpha.addAll(verbs);
        return alpha;
    }
    
     public void setVerbs(Set verbs) {
        this.verbs.clear();
        this.verbs.addAll(verbs);
    }
    
    // ---
    
    public InformContext getContext() {
        return context;
    }
    
    public void setContext(InformContext context) {
        this.context.replaceStyles(context);
    }

    // ---
    
    public String getBlcPath() {
        return (blc == null) ? "": blc.getPath();
    }
    
    public void setBlcPath(String blcPath) {
        this.blc = (blcPath.trim().equals(""))?null:new JifFileName(blcPath);
    }
    
    // ---
    
    public String getBresPath() {
        return (bres == null) ? "" : bres.getPath();
    }
    
    public void setBresPath(String bresPath) {
        this.bres = (bresPath.trim().equals(""))?null:new JifFileName(bresPath);
    }
    
    // ---
    
    public String getCompilerPath() {
        return (compiler == null) ? "" : compiler.getPath();
    }
    
    public void setCompilerPath(String compilerPath) {
        this.compiler = (compilerPath.trim().equals(""))?null:new JifFileName(compilerPath);
    }
    
    // ---
    
    public String getGamePath() {
        return (game == null) ? "" : game.getPath();
    }
    
    public void setGamePath(String gamePath) {
        this.game = (gamePath.trim().equals(""))?null:new JifFileName(gamePath);
    }
    
    // ---
    
    public String getInterpreterPath() {
    	File f;
    	if (informMode){
    		f = new File(workingDirectory+interpreterZcode);
    		if (f.exists()) return workingDirectory+interpreterZcode;
    		else return getInterpreterZcodePath();
    	}
    	else{
    		f = new File(workingDirectory+interpreterGlulx);
    		if (f.exists()) return workingDirectory+interpreterGlulx;
    		else return getInterpreterGlulxPath();
    	}
    }
    
    // ---
    
    public String getInterpreterGlulxPath() {
    	return (interpreterGlulx == null) ? "" : interpreterGlulx.getPath();
    }
    
    public void setInterpreterGlulxPath(String interpreterGlulxPath) {
        this.interpreterGlulx = (interpreterGlulxPath.trim().equals(""))?null:new JifFileName(interpreterGlulxPath);
    }
    
    // ---
    
    public String getInterpreterZcodePath () {
    	return (interpreterZcode == null) ? "" : interpreterZcode.getPath();
    }
    
    public void setInterpreterZcodePath(String interpreterZcodePath) {
        this.interpreterZcode = (interpreterZcodePath.trim().equals(""))?null:new JifFileName(interpreterZcodePath);
    }
    
    // ---
    
    public JifFileName getLastFile() {
        return lastFile;
    }
    
    public void setLastFile(String lastFile) {
        this.lastFile = (lastFile.trim().equals(""))?null:new JifFileName(lastFile);
    }
    
    // ---
    
    public JifFileName getLastInsert() {
        return lastInsert;
    }
    
    public void setLastInsert(String lastInsert) {
        this.lastInsert = (lastInsert.trim().equals(""))?null:new JifFileName(lastInsert);
    }
    
    // ---
    
    public JifFileName getLastProject() {
        return lastProject;
    }
    
    public void setLastProject(String lastProject) {
        this.lastProject = (lastProject.trim().equals(""))?null:new JifFileName(lastProject);
        notifyObservers();
    }
    
    // ---
    
    public String getLibraryPath() {
        return (library[0] == null) ? "" : library[0].getPath();
    }
    
    public void setLibraryPath(String libraryPath) {
        this.library[0] = (libraryPath.trim().equals(""))?null:new JifFileName(libraryPath);
    }
    
    // ---
    
    public String getLibraryPath1() {
        return (library[1] == null) ? "" : library[1].getPath();
    }
    
    public void setLibraryPath1(String libraryPath1) {
        this.library[1] = (libraryPath1.trim().equals(""))?null:new JifFileName(libraryPath1);
    }
    
    // ---
    
    public String getLibraryPath2() {
        return (library[2] == null) ? "" : library[2].getPath();
    }
    
    public void setLibraryPath2(String libraryPath2) {
        this.library[2] = (libraryPath2.trim().equals(""))?null:new JifFileName(libraryPath2);
    }
    
    // ---
    
    public String getLibraryPath3() {
        return (library[3] == null) ? "" : library[3].getPath();
    }
    
    public void setLibraryPath3(String libraryPath3) {
        this.library[3] = (libraryPath3.trim().equals(""))?null:new JifFileName(libraryPath3);
    }
    
    // ---
    
    public boolean getAdventInLib() {
        return adventInLib;
    }
    
    public void setAdventInLib(boolean adventInLib) {
        this.adventInLib = adventInLib;
    } 
    
    // ---
    
    public boolean getCreateNewFile() {
        return createNewFile;
    }
    
    public void setCreateNewFile(boolean createNewFile) {
        this.createNewFile = createNewFile;
    }
    
    // ---
    
    public boolean getHelpedCode() {
        return helpedCode;
    }
    
    public void setHelpedCode(boolean helpedCode) {
        this.helpedCode = helpedCode;
    }
    
    // ---
    
    public boolean getInformMode() {
        return informMode;
    }
    
    public void setInformMode(boolean informMode) {
        this.informMode = informMode;
    }
    
    // ---
    
    public boolean getMakeResource() {
        return makeResource;
    }
    
    public void setMakeResource(boolean makeResource) {
        this.makeResource = makeResource;
    }
    
    // ---
    
    public boolean getMappingLive() {
        return mappingLive;
    }
    
    public void setMappingLive(boolean mappingLive) {
        this.mappingLive = mappingLive;
    }
    
    // ---
    
    public boolean getNumberLines() {
        return numberLines;
    }
    
    public void setNumberLines(boolean numberLines) {
        this.numberLines = numberLines;
    }
    
    // ---
    
    public boolean getOpenLastFile() {
        return openLastFile;
    }
    
    public void setOpenLastFile(boolean openLastFile) {
        this.openLastFile = openLastFile;
    }
    
    // ---
    
    public boolean getOpenProjectFiles() {
        return openProjectFiles;
    }
    
    public void setOpenProjectFiles(boolean openProjectFiles) {
        this.openProjectFiles = openProjectFiles;
    }
    
    // ---
    
    public boolean getScanProjectFiles() {
        return scanProjectFiles;
    }
    
    public void setScanProjectFiles(boolean scanProjectFiles) {
        this.scanProjectFiles = scanProjectFiles;
    }
    
    // ---
    
    public boolean getSyntaxHighlighting() {
        return syntaxHighlighting;
    }
    
    public void setSyntaxHighlighting(boolean syntaxHighlighting) {
        this.syntaxHighlighting = syntaxHighlighting;
    }
    
    // ---
    
    public boolean getWrapLines() {
        return wrapLines;
    }
    
    public void setWrapLines(boolean wrapLines) {
        this.wrapLines = wrapLines;
    }
    
    // ---
    
    public boolean getFullScreen() {
        return fullscreen;
    }
    
    public void setFullScreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }
    
    // ---
    
    public boolean getOutput() {
        return output;
    }
    
    public void setOutput(boolean output) {
        this.output = output;
    }
    
    // ---
    
    public boolean getToolbar() {
        return toolbar;
    }
    
    public void setToolbar(boolean toolbar) {
        this.toolbar = toolbar;
    }
    
    // ---
    
    public boolean getTree() {
        return tree;
    }
    
    public void setTree(boolean tree) {
        this.tree = tree;
    }
    
    // ---
    
    public int getDivider1() {
        return divider1;
    }
    
    public void setDivider1(int divider1) {
        this.divider1 = divider1;
    }
    
    // ---
    
    public int getDivider3() {
        return divider3;
    }
    
    public void setDivider3(int divider3) {
        this.divider3 = divider3;
    }
    
    // ---
    
    public int getFrameHeight() {
        return frameHeight;
    }
    
    public void setFrameHeight(int frameHeight) {
        this.frameHeight = frameHeight;
    }
    
    // ---
    
    public int getFrameWidth() {
        return frameWidth;
    }
    
    public void setFrameWidth(int frameWidth) {
        this.frameWidth = frameWidth;
    }
    
    // ---
    
    public int getFrameX() {
        return frameX;
    }
    
    public void setFrameX(int frameX) {
        this.frameX = frameX;
    }
    
    // ---
    
    public int getFrameY() {
        return frameY;
    }
    
    public void setFrameY(int FrameY) {
        this.frameY = FrameY;
    }
    
    // ---
    
    public int getTabSize() {
        return tabSize;
    }
    
    public void setTabSize(int tabSize) {
        this.tabSize = tabSize;
    }
    
    // --- Configuration observer methods --------------------------------------
    
    public void registerObserver(JifConfigurationObserver o) {
        observers.add(o);
    }

    public void notifyObservers() {
        for (int i=0; i<observers.size(); i++) {
            JifConfigurationObserver observer = (JifConfigurationObserver) observers.get(i);
            observer.updateConfiguration();
        }
    }

    public void removeObserver(JifConfigurationObserver o) {
        int i = observers.indexOf(o);
        if (i >= 0) {
            observers.remove(i);
        }
    }
    
}