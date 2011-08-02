package it.schillaci.jif.gui;

/*
 * jFrame.java
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
import it.schillaci.jif.core.Constants;
import it.schillaci.jif.core.HighlightText;
import it.schillaci.jif.inform.InformContext;
import it.schillaci.jif.inform.InformDocument;
import it.schillaci.jif.inform.InformEditorKit;
import it.schillaci.jif.inform.InformSyntax;
import it.schillaci.jif.configuration.JifConfiguration;
import it.schillaci.jif.configuration.JifConfigurationDAO;
import it.schillaci.jif.configuration.JifConfigurationException;
import it.schillaci.jif.configuration.JifConfigurationObserver;
import it.schillaci.jif.core.JifDAO;
import it.schillaci.jif.core.JifEditorKit;
import it.schillaci.jif.core.JifFileFilter;
import it.schillaci.jif.core.JifFileName;
import it.schillaci.jif.core.Utils;
import it.schillaci.jif.project.JifProject;
import it.schillaci.jif.project.JifProjectException;
import it.schillaci.jif.project.JifProjectListCellRenderer;
import it.schillaci.jif.project.JifProjectObserver;
import it.schillaci.jif.project.JifProjectDAO;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;


/*
 * jFrame.java
 *
 * Created on 28 luglio 2003, 9.58
 */
/**
 * Main Class for Jif application.
 * Jif is a Java Editor for Inform
 * @author Alessandro Schillaci
 * @version 3.2
 */
public class jFrame extends JFrame implements JifConfigurationObserver, JifProjectObserver {

    private static final long serialVersionUID = 7544939067324000307L;

    public jFrame() {
        initFrame();
    }

    public final void initFrame() {
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/runInterpreter.png")));

        String directory = System.getProperty("user.dir");
        String file;

        // To Force another location for the Jif.cfg file just run this:
        // java.exe -Duser.language=en -Duser.region=US -Djif.configuration=[NEWPATH] -cp . -jar Jif.jar
        // where [NEWPATH] is the Jif.cfg file path
        if (System.getProperty("jif.configuration") == null) {
            file = directory + Constants.SEP + Constants.configFileName;
        } else {
            System.out.println("Load new config file: " + System.getProperty("jif.configuration"));
            file = System.getProperty("jif.configuration");
        }

        // Jif will test if the Jif.cfg exists
        // if not, jif will create a default one
        File configFile = new File(file);
        if (!configFile.exists()) {
            JOptionPane.showMessageDialog(this,
                    java.util.ResourceBundle.getBundle("JIF").getString("JIF_CONFIG_NOT_EXITS"),
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            try {
                JifConfigurationDAO.create(configFile);
            } catch (JifConfigurationException ex) {
                ex.printStackTrace();
            }
        }

        try {
            config = JifConfigurationDAO.load(configFile);
        } catch (JifConfigurationException ex) {
            ex.printStackTrace();
        }

        config.setWorkingDirectory(directory);

        initComponents();

        if (config.getFrameWidth() * config.getFrameHeight() * config.getFrameX() * config.getFrameY() != 0) {
            setSize(config.getFrameWidth(), config.getFrameHeight());
            setLocation(config.getFrameX(), config.getFrameY());
        } else {
            // first time JIF runs
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setSize(screenSize.width - 200, screenSize.height - 140);
            setLocation(screenSize.width / 2 - (getWidth() / 2), screenSize.height / 2 - (getHeight() / 2));
        }

        symbolList.setListData(config.getSymbolsAlpha().toArray());

        if (config.getInformMode()) {
            setInformMode();
        } else {
            setGlulxMode();
        }

        disableFileComponents();
        disableProjectComponents();

        config.registerObserver(this);
        updateConfiguration();

        project.registerObserver(this);
        updateProject();

        hlighterOutputErrors = new HighlightText(Constants.colorErrors);
        hlighterOutputWarnings = new HighlightText(Constants.colorWarnings);

        // Opens the last file opened
        if (config.getOpenLastFile() && config.getLastFile() != null) {
            // JIF opens the file only if it exists
            File test = new File(config.getLastFile().getPath());
            if (test.exists()) {
                openFile(config.getLastFile().getPath());
            }
        }

        //  Creates a new file when JIF is loaded
        if (config.getCreateNewFile()) {
            newAdventure();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        filePopupMenu = new javax.swing.JPopupMenu();
        insertNewMenu = new javax.swing.JMenu();
        insertSymbolPopupMenuItem = new javax.swing.JMenuItem();
        insertFilePopupMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        cutPopupMenuItem = new javax.swing.JMenuItem();
        copyPopupMenuItem = new javax.swing.JMenuItem();
        pastePopupMenu = new javax.swing.JMenu();
        clearPopupMenuItem = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JSeparator();
        printPopupMenuItem = new javax.swing.JMenuItem();
        closePopupMenuItem = new javax.swing.JMenuItem();
        closeAllPopupMenuItem = new javax.swing.JMenuItem();
        jumpToSourceMenuItem = new javax.swing.JMenuItem();
        projectPopupMenu = new javax.swing.JPopupMenu();
        newProjectPopupMenuItem = new javax.swing.JMenuItem();
        openProjectPopupMenuItem = new javax.swing.JMenuItem();
        saveProjectPopupMenuItem = new javax.swing.JMenuItem();
        closeProjectPopupMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        addNewToProjectPopupMenuItem = new javax.swing.JMenuItem();
        addFileToProjectPopupMenuItem = new javax.swing.JMenuItem();
        removeFromProjectPopupMenuItem = new javax.swing.JMenuItem();
        openSelectedFilesPopupMenuItem = new javax.swing.JMenuItem();
        jSeparator19 = new javax.swing.JSeparator();
        setMainPopupMenuItem = new javax.swing.JMenuItem();
        removeMainPopupMenuItem = new javax.swing.JMenuItem();
        aboutDialog = new JDialog (this, "", true);
        aboutTabbedPane = new javax.swing.JTabbedPane();
        aboutLabel = new javax.swing.JLabel();
        creditsScrollPane = new javax.swing.JScrollPane();
        creditsTextArea = new javax.swing.JTextArea();
        aboutControlPanel = new javax.swing.JPanel();
        aboutOKButton = new javax.swing.JButton();
        configDialog = new JDialog (this, "", false);
        configLabelPanel = new javax.swing.JPanel();
        configLabel = new javax.swing.JLabel();
        configScrollPane = new javax.swing.JScrollPane();
        configTextArea = new javax.swing.JTextArea();
        configControlPanel = new javax.swing.JPanel();
        configSaveButton = new javax.swing.JButton();
        configCloseButton = new javax.swing.JButton();
        infoDialog = new JDialog (this, "", false);
        infoScrollPane = new javax.swing.JScrollPane();
        infoTextArea = new javax.swing.JTextArea();
        infoControlPanel = new javax.swing.JPanel();
        infoCloseButton = new javax.swing.JButton();
        optionDialog = new JDialog (this, "", false);
        optionTabbedPane = new javax.swing.JTabbedPane();
        generalPanel = new javax.swing.JPanel();
        openLastFileCheckBox = new javax.swing.JCheckBox();
        createNewFileCheckBox = new javax.swing.JCheckBox();
        mappingLiveCheckBox = new javax.swing.JCheckBox();
        helpedCodeCheckBox = new javax.swing.JCheckBox();
        syntaxCheckBox = new javax.swing.JCheckBox();
        numberLinesCheckBox = new javax.swing.JCheckBox();
        scanProjectFilesCheckBox = new javax.swing.JCheckBox();
        wrapLinesCheckBox = new javax.swing.JCheckBox();
        projectOpenAllFilesCheckBox = new javax.swing.JCheckBox();
        makeResourceCheckBox = new javax.swing.JCheckBox();
        adventInLibCheckBox = new javax.swing.JCheckBox();
        colorFontPanel = new javax.swing.JPanel();
        colorEditorPane = new javax.swing.JEditorPane();
        colorPanel = new javax.swing.JPanel();
        backgroundColorPanel = new javax.swing.JPanel();
        backgroundColorLabel = new javax.swing.JLabel();
        backgroundColorButton = new javax.swing.JButton();
        attributeColorjPanel = new javax.swing.JPanel();
        attributeColorLabel = new javax.swing.JLabel();
        attributeColorButton = new javax.swing.JButton();
        commentColorPanel = new javax.swing.JPanel();
        commentColorLabel = new javax.swing.JLabel();
        commentColorButton = new javax.swing.JButton();
        keywordColorPanel = new javax.swing.JPanel();
        keywordColorLabel = new javax.swing.JLabel();
        keywordColorButton = new javax.swing.JButton();
        normalColorPanel = new javax.swing.JPanel();
        normalColorLabel = new javax.swing.JLabel();
        normalColorButton = new javax.swing.JButton();
        numberColorPanel = new javax.swing.JPanel();
        numberColorLabel = new javax.swing.JLabel();
        numberColorButton = new javax.swing.JButton();
        propertyColorPanel = new javax.swing.JPanel();
        propertyColorLabel = new javax.swing.JLabel();
        propertyColorButton = new javax.swing.JButton();
        stringColorPanel = new javax.swing.JPanel();
        stringColorLabel = new javax.swing.JLabel();
        stringColorButton = new javax.swing.JButton();
        verbColorPanel = new javax.swing.JPanel();
        verbColorLabel = new javax.swing.JLabel();
        verbColorButton = new javax.swing.JButton();
        wordColorPanel = new javax.swing.JPanel();
        wordColorLabel = new javax.swing.JLabel();
        wordColorButton = new javax.swing.JButton();
        fontPanel = new javax.swing.JPanel();
        fontLabel = new javax.swing.JLabel();
        fontNameComboBox = new javax.swing.JComboBox();
        fontNameComboBox.addItem("Arial");
        fontNameComboBox.addItem("Book Antiqua");
        fontNameComboBox.addItem("Comic Sans MS");
        fontNameComboBox.addItem("Courier New");
        fontNameComboBox.addItem("Dialog");
        fontNameComboBox.addItem("Georgia");
        fontNameComboBox.addItem("Lucida Console");
        fontNameComboBox.addItem("Lucida Bright");
        fontNameComboBox.addItem("Lucida Sans");
        fontNameComboBox.addItem("Monospaced");
        fontNameComboBox.addItem("Thaoma");
        fontNameComboBox.addItem("Times New Roman");
        fontNameComboBox.addItem("Verdana");
        fontSizeComboBox = new javax.swing.JComboBox();
        fontSizeComboBox.addItem("9");
        fontSizeComboBox.addItem("10");
        fontSizeComboBox.addItem("11");
        fontSizeComboBox.addItem("12");
        fontSizeComboBox.addItem("13");
        fontSizeComboBox.addItem("14");
        fontSizeComboBox.addItem("15");
        fontSizeComboBox.addItem("16");
        tabSizePanel = new javax.swing.JPanel();
        tabSizeLabel = new javax.swing.JLabel();
        tabSizeTextField = new javax.swing.JTextField();
        defaultColorPanel = new javax.swing.JPanel();
        defaultDarkColorPanel = new javax.swing.JPanel();
        defaultDarkColorLabel = new javax.swing.JLabel();
        defaultDarkColorButton = new javax.swing.JButton();
        defaultLightColorPanel = new javax.swing.JPanel();
        defaultLightColorLabel = new javax.swing.JLabel();
        defaultLightColorButton = new javax.swing.JButton();
        colorHighlightPanel = new javax.swing.JPanel();
        highlightEditorPane = new javax.swing.JEditorPane();
        highlightSelectedPanel = new javax.swing.JPanel();
        highlightSelectedLabel = new javax.swing.JLabel();
        highlightSelectedComboBox = new javax.swing.JComboBox();
        highlightSelectedComboBox.addItem("Bookmark");
        highlightSelectedComboBox.addItem("Bracket");
        highlightSelectedComboBox.addItem("Error");
        highlightSelectedComboBox.addItem("JumpTo");
        highlightSelectedComboBox.addItem("Warning");
        highlightPanel = new javax.swing.JPanel();
        bookmarkColorPanel = new javax.swing.JPanel();
        bookmarkColorLabel = new javax.swing.JLabel();
        bookmarkColorButton = new javax.swing.JButton();
        bracketColorPanel = new javax.swing.JPanel();
        bracketColorLabel = new javax.swing.JLabel();
        bracketColorButton = new javax.swing.JButton();
        errorColorPanel = new javax.swing.JPanel();
        errorColorLabel = new javax.swing.JLabel();
        errorColorButton = new javax.swing.JButton();
        jumpToColorPanel = new javax.swing.JPanel();
        jumpToColorLabel = new javax.swing.JLabel();
        jumpToColorButton = new javax.swing.JButton();
        warningColorPanel = new javax.swing.JPanel();
        warningColorLabel = new javax.swing.JLabel();
        warningColorButton = new javax.swing.JButton();
        defaultHighlightPanel = new javax.swing.JPanel();
        defaultDarkhighlightPanel = new javax.swing.JPanel();
        defaultDarkHighlightLabel = new javax.swing.JLabel();
        defaultDarkHighlightButton = new javax.swing.JButton();
        defaultLightHighlightPanel = new javax.swing.JPanel();
        defaultLightHighlightLabel = new javax.swing.JLabel();
        defaultLightHighlightButton = new javax.swing.JButton();
        complierPanel = new javax.swing.JPanel();
        gamePathPanel = new javax.swing.JPanel();
        gamePathLabel = new javax.swing.JLabel();
        gamePathTextField = new javax.swing.JTextField();
        gamePathButton = new javax.swing.JButton();
        compilerPathPanel = new javax.swing.JPanel();
        compilerPathLabel = new javax.swing.JLabel();
        compilerPathTextField = new javax.swing.JTextField();
        compilerPathButton = new javax.swing.JButton();
        interpreterPathPanel = new javax.swing.JPanel();
        interpreterPathLabel = new javax.swing.JLabel();
        interpreterPathTextField = new javax.swing.JTextField();
        interpreterPathButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        libraryPanel = new javax.swing.JPanel();
        libraryPathPanel = new javax.swing.JPanel();
        libraryPathLabel = new javax.swing.JLabel();
        libraryPathTextField = new javax.swing.JTextField();
        libraryPathButton = new javax.swing.JButton();
        libraryPath1Panel = new javax.swing.JPanel();
        libraryPath1Label = new javax.swing.JLabel();
        libraryPath1TextField = new javax.swing.JTextField();
        libraryPath1Button = new javax.swing.JButton();
        libraryPath2Panel = new javax.swing.JPanel();
        libraryPath2Label = new javax.swing.JLabel();
        libraryPath2TextField = new javax.swing.JTextField();
        libraryPath2Button = new javax.swing.JButton();
        libraryPath3Panel = new javax.swing.JPanel();
        libraryPath3Label = new javax.swing.JLabel();
        libraryPath3TextField = new javax.swing.JTextField();
        libraryPath3Button = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        glulxPanel = new javax.swing.JPanel();
        glulxPathPanel = new javax.swing.JPanel();
        glulxPathLabel = new javax.swing.JLabel();
        glulxPathTextField = new javax.swing.JTextField();
        glulxPathButton = new javax.swing.JButton();
        bresPathPanel = new javax.swing.JPanel();
        bresPathLabel = new javax.swing.JLabel();
        bresPathTextField = new javax.swing.JTextField();
        bresPathButton = new javax.swing.JButton();
        blcPathPanel = new javax.swing.JPanel();
        blcPathLabel = new javax.swing.JLabel();
        blcPathTextField = new javax.swing.JTextField();
        blcPathButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        optionControlPanel = new javax.swing.JPanel();
        optionSaveButton = new javax.swing.JButton();
        optionDefaultButton = new javax.swing.JButton();
        optionCancelButton = new javax.swing.JButton();
        projectSwitchesDialog = new javax.swing.JDialog();
        projectSwitchesPanel = new javax.swing.JPanel();
        projectSwitchesControlPanel = new javax.swing.JPanel();
        projectSwitchesSaveButton = new javax.swing.JButton();
        projectSwitchesCloseButton = new javax.swing.JButton();
        projectPropertiesDialog = new javax.swing.JDialog();
        projectPropertiesScrollPane = new javax.swing.JScrollPane();
        projectPropertiesTextArea = new javax.swing.JTextArea();
        projectPropertiesControlPanel = new javax.swing.JPanel();
        projectPropertiesSaveButton = new javax.swing.JButton();
        projectPropertiesCloseButton = new javax.swing.JButton();
        replaceDialog = new javax.swing.JDialog();
        replacePanel = new javax.swing.JPanel();
        replaceFindLabel = new javax.swing.JLabel();
        replaceFindTextField = new javax.swing.JTextField();
        replaceReplaceLabel = new javax.swing.JLabel();
        replaceReplaceTextField = new javax.swing.JTextField();
        replaceControlPanel = new javax.swing.JPanel();
        replaceFindButton = new javax.swing.JButton();
        replaceReplaceButton = new javax.swing.JButton();
        replaceAllButton = new javax.swing.JButton();
        replaceCloseButton = new javax.swing.JButton();
        switchesDialog = new JDialog (this, "", false);
        switchesPanel = new javax.swing.JPanel();
        switchesUpperPanel = new javax.swing.JPanel();
        switchesLowerPanel = new javax.swing.JPanel();
        switchesControlPanel = new javax.swing.JPanel();
        switchesSaveButton = new javax.swing.JButton();
        switchesCloseButton = new javax.swing.JButton();
        symbolDialog = new javax.swing.JDialog();
        symbolScrollPane = new javax.swing.JScrollPane();
        symbolList = new javax.swing.JList();
        textDialog = new JDialog (this, "", false);
        textLabel = new javax.swing.JLabel();
        textScrollPane = new javax.swing.JScrollPane();
        textTextArea = new javax.swing.JTextArea();
        textControlPanel = new javax.swing.JPanel();
        textCloseButton = new javax.swing.JButton();
        tutorialDialog = new JDialog (this, "", false);
        tutorialLabel = new javax.swing.JLabel();
        tutorialScrollPane = new javax.swing.JScrollPane();
        tutorialEditorPane = new javax.swing.JEditorPane();
        tutorialControlPanel = new javax.swing.JPanel();
        tutorialOKButton = new javax.swing.JButton();
        tutorialPrintButton = new javax.swing.JButton();
        toolbarPanel = new javax.swing.JPanel();
        newButton = new javax.swing.JButton();
        openButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        saveAllButton = new javax.swing.JButton();
        saveAsButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        closeAllButton = new javax.swing.JButton();
        undoButton = new javax.swing.JButton();
        redoButton = new javax.swing.JButton();
        commentButton = new javax.swing.JButton();
        uncommentButton = new javax.swing.JButton();
        tabLeftButton = new javax.swing.JButton();
        tabRightButton = new javax.swing.JButton();
        bracketCheckButton = new javax.swing.JButton();
        buildAllButton = new javax.swing.JButton();
        runButton = new javax.swing.JButton();
        insertSymbolButton = new javax.swing.JButton();
        interpreterButton = new javax.swing.JButton();
        switchManagerButton = new javax.swing.JButton();
        settingsButton = new javax.swing.JButton();
        findTextField = new javax.swing.JTextField();
        findButton = new javax.swing.JButton();
        replaceButton = new javax.swing.JButton();
        rowColTextField = new javax.swing.JTextField();
        mainSplitPane = new javax.swing.JSplitPane();
        upperSplitPane = new javax.swing.JSplitPane();
        leftTabbedPane = new javax.swing.JTabbedPane();
        treePanel = new javax.swing.JPanel();
        treeScrollPane = new javax.swing.JScrollPane();

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(new ImageIcon(getClass().getResource("/images/TREE_objects.png")));
        treeTree = new javax.swing.JTree();
        // Creo la root per Inspect
        top = new DefaultMutableTreeNode("Inspect");
        globalTree = new DefaultMutableTreeNode("Globals    ");
        top.add(globalTree);
        constantTree = new DefaultMutableTreeNode("Constants    ");
        top.add(constantTree);
        objectTree = new DefaultMutableTreeNode("Objects    ");
        top.add(objectTree);
        functionTree = new DefaultMutableTreeNode("Functions    ");
        top.add(functionTree);
        classTree = new DefaultMutableTreeNode("Classes    ");
        top.add(classTree);

        //Create a tree that allows one selection at a time.
        treeModel = new DefaultTreeModel(top);
        treeTree = new JTree(treeModel);

        treeTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeTree.setCellRenderer(renderer);
        projectPanel = new javax.swing.JPanel();
        projectScrollPane = new javax.swing.JScrollPane();
        projectList = new javax.swing.JList();
        projectList.setCellRenderer(new JifProjectListCellRenderer());
        projectList.addMouseListener(popupListenerProject);
        mainFileLabel = new javax.swing.JLabel();
        searchPanel = new javax.swing.JPanel();
        searchProjectPanel = new javax.swing.JPanel();
        searchProjectTextField = new javax.swing.JTextField();
        searchProjectButton = new javax.swing.JButton();
        definitionPanel = new javax.swing.JPanel();
        definitionTextField = new javax.swing.JTextField();
        definitionButton = new javax.swing.JButton();
        filePanel = new javax.swing.JPanel();
        outputScrollPane = new javax.swing.JScrollPane();
        outputTextArea = new javax.swing.JTextArea();
        mainMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        newMenuItem = new javax.swing.JMenuItem();
        openMenuItem = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JSeparator();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        saveAllMenuItem = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JSeparator();
        closeMenuItem = new javax.swing.JMenuItem();
        closeAllMenuItem = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JSeparator();
        recentFilesMenu = new javax.swing.JMenu();
        clearRecentFilesMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        printMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JSeparator();
        searchMenuItem = new javax.swing.JMenuItem();
        searchAllMenuItem = new javax.swing.JMenuItem();
        replaceMenuItem = new javax.swing.JMenuItem();
        selectAllMenuItem = new javax.swing.JMenuItem();
        clearAllMenuItem = new javax.swing.JMenuItem();
        jSeparator16 = new javax.swing.JSeparator();
        commentSelectionMenuItem = new javax.swing.JMenuItem();
        uncommentSelectionMenuItem = new javax.swing.JMenuItem();
        tabRightMenuItem = new javax.swing.JMenuItem();
        tabLeftMenuItem = new javax.swing.JMenuItem();
        jSeparator17 = new javax.swing.JSeparator();
        insertFileMenuItem = new javax.swing.JMenuItem();
        insertSymbolMenuItem = new javax.swing.JMenuItem();
        setBookmarkMenuItem = new javax.swing.JMenuItem();
        nextBookmarkMenuItem = new javax.swing.JMenuItem();
        extractStringsMenuItem = new javax.swing.JMenuItem();
        translateMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        outputCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        toolbarCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        treeCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        toggleFullscreenCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        projectMenu = new javax.swing.JMenu();
        newProjectMenuItem = new javax.swing.JMenuItem();
        openProjectMenuItem = new javax.swing.JMenuItem();
        saveProjectMenuItem = new javax.swing.JMenuItem();
        closeProjectMenuItem = new javax.swing.JMenuItem();
        jSeparator14 = new javax.swing.JSeparator();
        addNewToProjectMenuItem = new javax.swing.JMenuItem();
        addFileToProjectMenuItem = new javax.swing.JMenuItem();
        removeFromProjectMenuItem = new javax.swing.JMenuItem();
        projectPropertiesMenuItem = new javax.swing.JMenuItem();
        projectSwitchesMenuItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        lastProjectMenuItem = new javax.swing.JMenuItem();
        modeMenu = new javax.swing.JMenu();
        informModeCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        glulxModeCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        buildMenu = new javax.swing.JMenu();
        buildAllMenuItem = new javax.swing.JMenuItem();
        switchesMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        runMenuItem = new javax.swing.JMenuItem();
        glulxMenu = new javax.swing.JMenu();
        buildAllGlulxMenuItem = new javax.swing.JMenuItem();
        jSeparator18 = new javax.swing.JSeparator();
        makeResourceMenuItem = new javax.swing.JMenuItem();
        compileMenuItem = new javax.swing.JMenuItem();
        makeBlbMenuItem = new javax.swing.JMenuItem();
        jSeparator15 = new javax.swing.JSeparator();
        runUlxMenuItem = new javax.swing.JMenuItem();
        runBlbMenuItem = new javax.swing.JMenuItem();
        optionsMenu = new javax.swing.JMenu();
        configFileMenuItem = new javax.swing.JMenuItem();
        settingsMenuItem = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JSeparator();
        garbageCollectionMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        readMeMenuItem = new javax.swing.JMenuItem();
        changelogMenuItem = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        aboutMenuItem = new javax.swing.JMenuItem();

        filePopupMenu.setFont(new java.awt.Font("Dialog", 0, 11));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("JIF"); // NOI18N
        insertNewMenu.setText(bundle.getString("POPUPMENU_MENU_NEW")); // NOI18N
        insertNewMenu.setFont(new java.awt.Font("Dialog", 0, 11));
        filePopupMenu.add(insertNewMenu);

        insertSymbolPopupMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        insertSymbolPopupMenuItem.setText(bundle.getString("JFRAME_INSERT_SYMBOL")); // NOI18N
        insertSymbolPopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertSymbolActionPerformed(evt);
            }
        });
        filePopupMenu.add(insertSymbolPopupMenuItem);

        insertFilePopupMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        insertFilePopupMenuItem.setText(bundle.getString("JFRAME_INSERT_FROM_FILE")); // NOI18N
        insertFilePopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertFileActionPerformed(evt);
            }
        });
        filePopupMenu.add(insertFilePopupMenuItem);
        filePopupMenu.add(jSeparator3);

        cutPopupMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        cutPopupMenuItem.setText(bundle.getString("JFRAME_EDIT_CUT")); // NOI18N
        cutPopupMenuItem.setActionCommand("KEY JFRAME_EDIT_CUT : RB JIF");
        cutPopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cutActionPerformed(evt);
            }
        });
        filePopupMenu.add(cutPopupMenuItem);

        copyPopupMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        copyPopupMenuItem.setText(bundle.getString("POPUPMENU_MENUITEM_COPY")); // NOI18N
        copyPopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyActionPerformed(evt);
            }
        });
        filePopupMenu.add(copyPopupMenuItem);

        pastePopupMenu.setText(bundle.getString("POPUPMENU_MENU_PASTE")); // NOI18N
        pastePopupMenu.setFont(new java.awt.Font("Dialog", 0, 11));
        filePopupMenu.add(pastePopupMenu);

        clearPopupMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        clearPopupMenuItem.setText(bundle.getString("POPUPMENU_MENUITEM_CLEAR")); // NOI18N
        clearPopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearPopupActionPerformed(evt);
            }
        });
        filePopupMenu.add(clearPopupMenuItem);
        filePopupMenu.add(jSeparator13);

        printPopupMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        printPopupMenuItem.setText(bundle.getString("MENUITEM_PRINT")); // NOI18N
        printPopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printPopupMenuItemActionPerformed(evt);
            }
        });
        filePopupMenu.add(printPopupMenuItem);

        closePopupMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        closePopupMenuItem.setText(bundle.getString("MENUITEM_CLOSE")); // NOI18N
        closePopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeActionPerformed(evt);
            }
        });
        filePopupMenu.add(closePopupMenuItem);

        closeAllPopupMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        closeAllPopupMenuItem.setText(bundle.getString("MENUITEM_CLOSEALL")); // NOI18N
        closeAllPopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeAllActionPerformed(evt);
            }
        });
        filePopupMenu.add(closeAllPopupMenuItem);

        jumpToSourceMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        jumpToSourceMenuItem.setText(bundle.getString("MENU_JUMP_TO_SOURCE")); // NOI18N
        jumpToSourceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jumpToSourceActionPerformed(evt);
            }
        });
        filePopupMenu.add(jumpToSourceMenuItem);

        projectPopupMenu.setFont(new java.awt.Font("Dialog", 0, 11));

        newProjectPopupMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        newProjectPopupMenuItem.setText(bundle.getString("PROJECT_NEW_PROJECT")); // NOI18N
        newProjectPopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newProjectActionPerformed(evt);
            }
        });
        projectPopupMenu.add(newProjectPopupMenuItem);

        openProjectPopupMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        openProjectPopupMenuItem.setText(bundle.getString("PROJECT_OPEN_PROJECT")); // NOI18N
        openProjectPopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openProjectActionPerformed(evt);
            }
        });
        projectPopupMenu.add(openProjectPopupMenuItem);

        saveProjectPopupMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        saveProjectPopupMenuItem.setText(bundle.getString("PROJECT_SAVE_PROJECT")); // NOI18N
        saveProjectPopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveProjectActionPerformed(evt);
            }
        });
        projectPopupMenu.add(saveProjectPopupMenuItem);

        closeProjectPopupMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        closeProjectPopupMenuItem.setText(bundle.getString("PROJECT_CLOSE_PROJECT")); // NOI18N
        closeProjectPopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeProjectActionPerformed(evt);
            }
        });
        projectPopupMenu.add(closeProjectPopupMenuItem);
        projectPopupMenu.add(jSeparator6);

        addNewToProjectPopupMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        addNewToProjectPopupMenuItem.setText(bundle.getString("PROJECT_ADD_NEWFILE_TO_PROJECT")); // NOI18N
        addNewToProjectPopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewToProjectActionPerformed(evt);
            }
        });
        projectPopupMenu.add(addNewToProjectPopupMenuItem);

        addFileToProjectPopupMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        addFileToProjectPopupMenuItem.setText(bundle.getString("PROJECT_ADD_FILE_TO_PROJECT")); // NOI18N
        addFileToProjectPopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFileToProjectActionPerformed(evt);
            }
        });
        projectPopupMenu.add(addFileToProjectPopupMenuItem);

        removeFromProjectPopupMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        removeFromProjectPopupMenuItem.setText(bundle.getString("PROJECT_POPUP_REMOVE")); // NOI18N
        removeFromProjectPopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeFromProjectActionPerformed(evt);
            }
        });
        projectPopupMenu.add(removeFromProjectPopupMenuItem);

        openSelectedFilesPopupMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        openSelectedFilesPopupMenuItem.setText(bundle.getString("PROJECT_OPEN_SELECTED_FILES")); // NOI18N
        openSelectedFilesPopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openSelectedFilesActionPerformed(evt);
            }
        });
        projectPopupMenu.add(openSelectedFilesPopupMenuItem);
        projectPopupMenu.add(jSeparator19);

        setMainPopupMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        setMainPopupMenuItem.setText(bundle.getString("PROJECT_SET_AS_MAIN_FILE")); // NOI18N
        setMainPopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setMainActionPerformed(evt);
            }
        });
        projectPopupMenu.add(setMainPopupMenuItem);

        removeMainPopupMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        removeMainPopupMenuItem.setText(bundle.getString("PROJECT_REMOVE_MAIN_FILE")); // NOI18N
        removeMainPopupMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeMainActionPerformed(evt);
            }
        });
        projectPopupMenu.add(removeMainPopupMenuItem);

        aboutDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        aboutDialog.setTitle(bundle.getString("JFRAME_ABOUT_JIF")); // NOI18N
        aboutDialog.setFocusCycleRoot(false);
        aboutDialog.setModal(true);
        aboutDialog.setResizable(false);

        aboutLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/about.png"))); // NOI18N
        aboutTabbedPane.addTab("About", aboutLabel);

        creditsScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));

        creditsTextArea.setColumns(20);
        creditsTextArea.setEditable(false);
        creditsTextArea.setFont(new java.awt.Font("MonoSpaced", 0, 11));
        creditsTextArea.setRows(5);
        creditsTextArea.setText("JIF, a java editor for Inform\nby Alessandro Schillaci\nhttp://www.slade.altervista.org/\n\nDevelopment: \n- Alessandro Schillaci\n- Luis Fernandez\n- Peter F. Piggott\n\nContributors:\nPaolo Lucchesi\nVincenzo Scarpa\nBaltasar García Perez-Schofield\nChristof Menear\nGiles Boutel\nJavier San José\nDavid Moreno\nEric Forgeot\nMax Kalus\nAdrien Saurat\nAlex V Flinsch\nDaryl McCullough\nGiancarlo Niccolai\nIgnazio di Napoli\nJoerg Rosenbauer\nMatteo De Simone\nTommaso Caldarola");
        creditsTextArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 5, 3, 3));
        creditsScrollPane.setViewportView(creditsTextArea);

        aboutTabbedPane.addTab("Credits", creditsScrollPane);

        aboutDialog.getContentPane().add(aboutTabbedPane, java.awt.BorderLayout.NORTH);

        aboutOKButton.setText(bundle.getString("MESSAGE_OK")); // NOI18N
        aboutOKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutOKActionPerformed(evt);
            }
        });
        aboutControlPanel.add(aboutOKButton);

        aboutDialog.getContentPane().add(aboutControlPanel, java.awt.BorderLayout.SOUTH);

        aboutDialog.getAccessibleContext().setAccessibleParent(this);

        configDialog.setTitle(bundle.getString("JDIALOG_CONFIGFILES_TITLE")); // NOI18N
        configDialog.setFont(new java.awt.Font("Arial", 0, 12));

        configLabelPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        configLabelPanel.setLayout(new java.awt.GridLayout(1, 0));

        configLabel.setText("configuration");
        configLabelPanel.add(configLabel);

        configDialog.getContentPane().add(configLabelPanel, java.awt.BorderLayout.NORTH);

        configTextArea.setTabSize(4);
        configTextArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 5, 2, 5));
        configScrollPane.setViewportView(configTextArea);

        configDialog.getContentPane().add(configScrollPane, java.awt.BorderLayout.CENTER);

        configControlPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        configSaveButton.setText(bundle.getString("MESSAGE_SAVE")); // NOI18N
        configSaveButton.setMaximumSize(new java.awt.Dimension(59, 23));
        configSaveButton.setMinimumSize(new java.awt.Dimension(59, 23));
        configSaveButton.setPreferredSize(new java.awt.Dimension(59, 23));
        configSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configSaveActionPerformed(evt);
            }
        });
        configControlPanel.add(configSaveButton);

        configCloseButton.setText(bundle.getString("MESSAGE_CLOSE")); // NOI18N
        configCloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configCloseActionPerformed(evt);
            }
        });
        configControlPanel.add(configCloseButton);

        configDialog.getContentPane().add(configControlPanel, java.awt.BorderLayout.SOUTH);

        infoDialog.setTitle("");
        infoDialog.setModal(true);

        infoScrollPane.setAutoscrolls(true);

        infoTextArea.setBackground(new java.awt.Color(204, 204, 204));
        infoTextArea.setEditable(false);
        infoTextArea.setFont(new java.awt.Font("Monospaced", 0, 12));
        infoTextArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 5, 2, 5));
        infoTextArea.setMaximumSize(new java.awt.Dimension(0, 0));
        infoTextArea.setMinimumSize(new java.awt.Dimension(0, 0));
        infoTextArea.setPreferredSize(new java.awt.Dimension(0, 0));
        infoScrollPane.setViewportView(infoTextArea);

        infoDialog.getContentPane().add(infoScrollPane, java.awt.BorderLayout.CENTER);

        infoCloseButton.setText(bundle.getString("MESSAGE_CLOSE")); // NOI18N
        infoCloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infoCloseActionPerformed(evt);
            }
        });
        infoControlPanel.add(infoCloseButton);

        infoDialog.getContentPane().add(infoControlPanel, java.awt.BorderLayout.SOUTH);

        optionDialog.setTitle(bundle.getString("JFRAME_SETTING")); // NOI18N
        optionDialog.setModal(true);

        optionTabbedPane.setMinimumSize(new java.awt.Dimension(100, 100));
        optionTabbedPane.setPreferredSize(new java.awt.Dimension(500, 450));

        generalPanel.setMinimumSize(new java.awt.Dimension(277, 800));
        generalPanel.setPreferredSize(new java.awt.Dimension(192, 138));
        generalPanel.setLayout(new java.awt.GridBagLayout());

        openLastFileCheckBox.setText(bundle.getString("PROJECT_OPEN_LAST_OPEN_FILE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        generalPanel.add(openLastFileCheckBox, gridBagConstraints);

        createNewFileCheckBox.setText(bundle.getString("OPTION_CREATE_A_NEW_FILE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        generalPanel.add(createNewFileCheckBox, gridBagConstraints);

        mappingLiveCheckBox.setText(bundle.getString("CHECKBOX_MAPPINGLIVE")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        generalPanel.add(mappingLiveCheckBox, gridBagConstraints);

        helpedCodeCheckBox.setSelected(true);
        helpedCodeCheckBox.setText(bundle.getString("CHECKBOX_HELPEDCODE")); // NOI18N
        helpedCodeCheckBox.setToolTipText(bundle.getString("CHECKBOX_HELPEDCODE_TOOLTIP")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        generalPanel.add(helpedCodeCheckBox, gridBagConstraints);

        syntaxCheckBox.setSelected(true);
        syntaxCheckBox.setText(bundle.getString("CHECKBOX_SYNTAX")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        generalPanel.add(syntaxCheckBox, gridBagConstraints);

        numberLinesCheckBox.setText(bundle.getString("CHECKBOX_NUMBEROFLINES")); // NOI18N
        numberLinesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numberLinesCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        generalPanel.add(numberLinesCheckBox, gridBagConstraints);

        scanProjectFilesCheckBox.setText(bundle.getString("CHECKBOX_SCAN_PROJECT")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        generalPanel.add(scanProjectFilesCheckBox, gridBagConstraints);

        wrapLinesCheckBox.setText("Wrap Lines");
        wrapLinesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wrapLinesCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        generalPanel.add(wrapLinesCheckBox, gridBagConstraints);

        projectOpenAllFilesCheckBox.setText(bundle.getString("PROJECT_OPEN_ALL_FILES")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        generalPanel.add(projectOpenAllFilesCheckBox, gridBagConstraints);

        makeResourceCheckBox.setText(bundle.getString("GLULX_MAKE_RESOURCE_WHEN_BUILD_ALL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        generalPanel.add(makeResourceCheckBox, gridBagConstraints);

        adventInLibCheckBox.setText(bundle.getString("JOPTION_ADVENT_IN_LIB")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        generalPanel.add(adventInLibCheckBox, gridBagConstraints);

        optionTabbedPane.addTab("General", generalPanel);

        colorFontPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Color and Font"));
        colorFontPanel.setMinimumSize(new java.awt.Dimension(277, 260));
        colorFontPanel.setPreferredSize(new java.awt.Dimension(277, 260));
        colorFontPanel.setLayout(new java.awt.GridBagLayout());

        colorEditorPane.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), javax.swing.BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        colorEditorPane.setEditable(false);
        colorEditorPane.setMaximumSize(new java.awt.Dimension(50, 50));
        colorEditorPane.setMinimumSize(new java.awt.Dimension(50, 50));
        colorEditorPane.setPreferredSize(new java.awt.Dimension(50, 180));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        colorFontPanel.add(colorEditorPane, gridBagConstraints);

        colorPanel.setLayout(new java.awt.GridBagLayout());

        backgroundColorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        backgroundColorLabel.setText("Background");
        backgroundColorPanel.add(backgroundColorLabel);

        backgroundColorButton.setMaximumSize(new java.awt.Dimension(35, 15));
        backgroundColorButton.setMinimumSize(new java.awt.Dimension(35, 15));
        backgroundColorButton.setPreferredSize(new java.awt.Dimension(35, 15));
        backgroundColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backgroundColorActionPerformed(evt);
            }
        });
        backgroundColorPanel.add(backgroundColorButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        colorPanel.add(backgroundColorPanel, gridBagConstraints);

        attributeColorjPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        attributeColorLabel.setText("Attribute");
        attributeColorjPanel.add(attributeColorLabel);

        attributeColorButton.setMaximumSize(new java.awt.Dimension(35, 15));
        attributeColorButton.setMinimumSize(new java.awt.Dimension(35, 15));
        attributeColorButton.setPreferredSize(new java.awt.Dimension(35, 15));
        attributeColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attributeColorActionPerformed(evt);
            }
        });
        attributeColorjPanel.add(attributeColorButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        colorPanel.add(attributeColorjPanel, gridBagConstraints);

        commentColorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        commentColorLabel.setText("Comment");
        commentColorPanel.add(commentColorLabel);

        commentColorButton.setMaximumSize(new java.awt.Dimension(35, 15));
        commentColorButton.setMinimumSize(new java.awt.Dimension(35, 15));
        commentColorButton.setPreferredSize(new java.awt.Dimension(35, 15));
        commentColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commentColorActionPerformed(evt);
            }
        });
        commentColorPanel.add(commentColorButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        colorPanel.add(commentColorPanel, gridBagConstraints);

        keywordColorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        keywordColorLabel.setText("Keyword");
        keywordColorPanel.add(keywordColorLabel);

        keywordColorButton.setMaximumSize(new java.awt.Dimension(35, 15));
        keywordColorButton.setMinimumSize(new java.awt.Dimension(35, 15));
        keywordColorButton.setPreferredSize(new java.awt.Dimension(35, 15));
        keywordColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keywordColorActionPerformed(evt);
            }
        });
        keywordColorPanel.add(keywordColorButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        colorPanel.add(keywordColorPanel, gridBagConstraints);

        normalColorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        normalColorLabel.setText("Normal");
        normalColorPanel.add(normalColorLabel);

        normalColorButton.setMaximumSize(new java.awt.Dimension(35, 15));
        normalColorButton.setMinimumSize(new java.awt.Dimension(35, 15));
        normalColorButton.setPreferredSize(new java.awt.Dimension(35, 15));
        normalColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                normalColorActionPerformed(evt);
            }
        });
        normalColorPanel.add(normalColorButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        colorPanel.add(normalColorPanel, gridBagConstraints);

        numberColorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        numberColorLabel.setText("Number");
        numberColorPanel.add(numberColorLabel);

        numberColorButton.setMaximumSize(new java.awt.Dimension(35, 15));
        numberColorButton.setMinimumSize(new java.awt.Dimension(35, 15));
        numberColorButton.setPreferredSize(new java.awt.Dimension(35, 15));
        numberColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numberColorActionPerformed(evt);
            }
        });
        numberColorPanel.add(numberColorButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        colorPanel.add(numberColorPanel, gridBagConstraints);

        propertyColorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        propertyColorLabel.setText("Property");
        propertyColorPanel.add(propertyColorLabel);

        propertyColorButton.setMaximumSize(new java.awt.Dimension(35, 15));
        propertyColorButton.setMinimumSize(new java.awt.Dimension(35, 15));
        propertyColorButton.setPreferredSize(new java.awt.Dimension(35, 15));
        propertyColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                propertyColorActionPerformed(evt);
            }
        });
        propertyColorPanel.add(propertyColorButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        colorPanel.add(propertyColorPanel, gridBagConstraints);

        stringColorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        stringColorLabel.setText("String");
        stringColorPanel.add(stringColorLabel);

        stringColorButton.setMaximumSize(new java.awt.Dimension(35, 15));
        stringColorButton.setMinimumSize(new java.awt.Dimension(35, 15));
        stringColorButton.setPreferredSize(new java.awt.Dimension(35, 15));
        stringColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stringColorActionPerformed(evt);
            }
        });
        stringColorPanel.add(stringColorButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        colorPanel.add(stringColorPanel, gridBagConstraints);

        verbColorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        verbColorLabel.setText("Verb");
        verbColorPanel.add(verbColorLabel);

        verbColorButton.setMaximumSize(new java.awt.Dimension(35, 15));
        verbColorButton.setMinimumSize(new java.awt.Dimension(35, 15));
        verbColorButton.setPreferredSize(new java.awt.Dimension(35, 15));
        verbColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                verbColorActionPerformed(evt);
            }
        });
        verbColorPanel.add(verbColorButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        colorPanel.add(verbColorPanel, gridBagConstraints);

        wordColorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        wordColorLabel.setText("Word");
        wordColorPanel.add(wordColorLabel);

        wordColorButton.setMaximumSize(new java.awt.Dimension(35, 15));
        wordColorButton.setMinimumSize(new java.awt.Dimension(35, 15));
        wordColorButton.setPreferredSize(new java.awt.Dimension(35, 15));
        wordColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wordColorActionPerformed(evt);
            }
        });
        wordColorPanel.add(wordColorButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        colorPanel.add(wordColorPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        colorFontPanel.add(colorPanel, gridBagConstraints);

        fontLabel.setText("Font");
        fontPanel.add(fontLabel);

        fontNameComboBox.setMaximumRowCount(10);
        fontNameComboBox.setMinimumSize(new java.awt.Dimension(100, 21));
        fontNameComboBox.setPreferredSize(new java.awt.Dimension(120, 21));
        fontNameComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontNameComboBoxActionPerformed(evt);
            }
        });
        fontPanel.add(fontNameComboBox);

        fontSizeComboBox.setMinimumSize(new java.awt.Dimension(100, 21));
        fontSizeComboBox.setPreferredSize(new java.awt.Dimension(120, 21));
        fontSizeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontSizeComboBoxActionPerformed(evt);
            }
        });
        fontPanel.add(fontSizeComboBox);

        tabSizeLabel.setText("TAB size");
        tabSizePanel.add(tabSizeLabel);

        tabSizeTextField.setColumns(2);
        tabSizeTextField.setText("4");
        tabSizeTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tabSizeTextFieldActionPerformed(evt);
            }
        });
        tabSizePanel.add(tabSizeTextField);

        fontPanel.add(tabSizePanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        colorFontPanel.add(fontPanel, gridBagConstraints);

        defaultDarkColorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        defaultDarkColorLabel.setText("Black setting");
        defaultDarkColorPanel.add(defaultDarkColorLabel);

        defaultDarkColorButton.setMaximumSize(new java.awt.Dimension(35, 15));
        defaultDarkColorButton.setMinimumSize(new java.awt.Dimension(35, 15));
        defaultDarkColorButton.setPreferredSize(new java.awt.Dimension(35, 15));
        defaultDarkColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultDarkActionPerformed(evt);
            }
        });
        defaultDarkColorPanel.add(defaultDarkColorButton);

        defaultColorPanel.add(defaultDarkColorPanel);

        defaultLightColorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        defaultLightColorLabel.setText("White setting");
        defaultLightColorPanel.add(defaultLightColorLabel);

        defaultLightColorButton.setMaximumSize(new java.awt.Dimension(35, 15));
        defaultLightColorButton.setMinimumSize(new java.awt.Dimension(35, 15));
        defaultLightColorButton.setPreferredSize(new java.awt.Dimension(35, 15));
        defaultLightColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultLightActionPerformed(evt);
            }
        });
        defaultLightColorPanel.add(defaultLightColorButton);

        defaultColorPanel.add(defaultLightColorPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        colorFontPanel.add(defaultColorPanel, gridBagConstraints);

        optionTabbedPane.addTab("Colors", colorFontPanel);

        colorHighlightPanel.setLayout(new java.awt.GridBagLayout());

        highlightEditorPane.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), javax.swing.BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        highlightEditorPane.setEditable(false);
        highlightEditorPane.setMaximumSize(new java.awt.Dimension(50, 50));
        highlightEditorPane.setMinimumSize(new java.awt.Dimension(50, 50));
        highlightEditorPane.setPreferredSize(new java.awt.Dimension(50, 180));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        colorHighlightPanel.add(highlightEditorPane, gridBagConstraints);

        highlightSelectedLabel.setText("Selected highlighting");
        highlightSelectedPanel.add(highlightSelectedLabel);

        highlightSelectedComboBox.setMaximumRowCount(10);
        highlightSelectedComboBox.setMinimumSize(new java.awt.Dimension(100, 21));
        highlightSelectedComboBox.setPreferredSize(new java.awt.Dimension(120, 21));
        highlightSelectedComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                highlightSelectedComboBoxActionPerformed(evt);
            }
        });
        highlightSelectedPanel.add(highlightSelectedComboBox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        colorHighlightPanel.add(highlightSelectedPanel, gridBagConstraints);

        highlightPanel.setLayout(new java.awt.GridBagLayout());

        bookmarkColorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        bookmarkColorLabel.setText("Bookmarks");
        bookmarkColorPanel.add(bookmarkColorLabel);

        bookmarkColorButton.setMaximumSize(new java.awt.Dimension(35, 15));
        bookmarkColorButton.setMinimumSize(new java.awt.Dimension(35, 15));
        bookmarkColorButton.setPreferredSize(new java.awt.Dimension(35, 15));
        bookmarkColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bookmarkColorButtonattributeColorActionPerformed(evt);
            }
        });
        bookmarkColorPanel.add(bookmarkColorButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        highlightPanel.add(bookmarkColorPanel, gridBagConstraints);

        bracketColorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        bracketColorLabel.setText("Brackets");
        bracketColorPanel.add(bracketColorLabel);

        bracketColorButton.setMaximumSize(new java.awt.Dimension(35, 15));
        bracketColorButton.setMinimumSize(new java.awt.Dimension(35, 15));
        bracketColorButton.setPreferredSize(new java.awt.Dimension(35, 15));
        bracketColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bracketColorButtonbackgroundColorActionPerformed(evt);
            }
        });
        bracketColorPanel.add(bracketColorButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        highlightPanel.add(bracketColorPanel, gridBagConstraints);

        errorColorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        errorColorLabel.setText("Errors");
        errorColorPanel.add(errorColorLabel);

        errorColorButton.setMaximumSize(new java.awt.Dimension(35, 15));
        errorColorButton.setMinimumSize(new java.awt.Dimension(35, 15));
        errorColorButton.setPreferredSize(new java.awt.Dimension(35, 15));
        errorColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                errorColorButtoncommentColorActionPerformed(evt);
            }
        });
        errorColorPanel.add(errorColorButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        highlightPanel.add(errorColorPanel, gridBagConstraints);

        jumpToColorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jumpToColorLabel.setText("Jump To");
        jumpToColorPanel.add(jumpToColorLabel);

        jumpToColorButton.setMaximumSize(new java.awt.Dimension(35, 15));
        jumpToColorButton.setMinimumSize(new java.awt.Dimension(35, 15));
        jumpToColorButton.setPreferredSize(new java.awt.Dimension(35, 15));
        jumpToColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jumpToColorButtonkeywordColorActionPerformed(evt);
            }
        });
        jumpToColorPanel.add(jumpToColorButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        highlightPanel.add(jumpToColorPanel, gridBagConstraints);

        warningColorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        warningColorLabel.setText("Warnings");
        warningColorPanel.add(warningColorLabel);

        warningColorButton.setMaximumSize(new java.awt.Dimension(35, 15));
        warningColorButton.setMinimumSize(new java.awt.Dimension(35, 15));
        warningColorButton.setPreferredSize(new java.awt.Dimension(35, 15));
        warningColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                warningColorButtonnormalColorActionPerformed(evt);
            }
        });
        warningColorPanel.add(warningColorButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        highlightPanel.add(warningColorPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        colorHighlightPanel.add(highlightPanel, gridBagConstraints);

        defaultDarkhighlightPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        defaultDarkHighlightLabel.setText("Black setting");
        defaultDarkhighlightPanel.add(defaultDarkHighlightLabel);

        defaultDarkHighlightButton.setMaximumSize(new java.awt.Dimension(35, 15));
        defaultDarkHighlightButton.setMinimumSize(new java.awt.Dimension(35, 15));
        defaultDarkHighlightButton.setPreferredSize(new java.awt.Dimension(35, 15));
        defaultDarkHighlightButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultDarkHighlightButtondefaultDarkActionPerformed(evt);
            }
        });
        defaultDarkhighlightPanel.add(defaultDarkHighlightButton);

        defaultHighlightPanel.add(defaultDarkhighlightPanel);

        defaultLightHighlightPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        defaultLightHighlightLabel.setText("White setting");
        defaultLightHighlightPanel.add(defaultLightHighlightLabel);

        defaultLightHighlightButton.setMaximumSize(new java.awt.Dimension(35, 15));
        defaultLightHighlightButton.setMinimumSize(new java.awt.Dimension(35, 15));
        defaultLightHighlightButton.setPreferredSize(new java.awt.Dimension(35, 15));
        defaultLightHighlightButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultLightHighlightButtondefaultLightActionPerformed(evt);
            }
        });
        defaultLightHighlightPanel.add(defaultLightHighlightButton);

        defaultHighlightPanel.add(defaultLightHighlightPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        colorHighlightPanel.add(defaultHighlightPanel, gridBagConstraints);

        optionTabbedPane.addTab("Highlights", colorHighlightPanel);

        complierPanel.setPreferredSize(new java.awt.Dimension(469, 99));
        complierPanel.setLayout(new java.awt.GridBagLayout());

        gamePathPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        gamePathLabel.setText(bundle.getString("JDIALOG_CONFIGPATH_ATPATH")); // NOI18N
        gamePathPanel.add(gamePathLabel);

        gamePathTextField.setPreferredSize(new java.awt.Dimension(280, 21));
        gamePathPanel.add(gamePathTextField);

        gamePathButton.setText(bundle.getString("MESSAGE_BROWSE")); // NOI18N
        gamePathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gamePathActionPerformed(evt);
            }
        });
        gamePathPanel.add(gamePathButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        complierPanel.add(gamePathPanel, gridBagConstraints);

        compilerPathPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        compilerPathLabel.setText(bundle.getString("JDIALOG_CONFIGPATH_COMPILERPATH")); // NOI18N
        compilerPathPanel.add(compilerPathLabel);

        compilerPathTextField.setPreferredSize(new java.awt.Dimension(280, 21));
        compilerPathPanel.add(compilerPathTextField);

        compilerPathButton.setText(bundle.getString("MESSAGE_BROWSE")); // NOI18N
        compilerPathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compilerPathActionPerformed(evt);
            }
        });
        compilerPathPanel.add(compilerPathButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        complierPanel.add(compilerPathPanel, gridBagConstraints);

        interpreterPathPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        interpreterPathLabel.setText(bundle.getString("JDIALOG_CONFIGPATH_INTERPRETERPATH")); // NOI18N
        interpreterPathPanel.add(interpreterPathLabel);

        interpreterPathTextField.setPreferredSize(new java.awt.Dimension(280, 21));
        interpreterPathPanel.add(interpreterPathTextField);

        interpreterPathButton.setText(bundle.getString("MESSAGE_BROWSE")); // NOI18N
        interpreterPathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                interpreterPathActionPerformed(evt);
            }
        });
        interpreterPathPanel.add(interpreterPathButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        complierPanel.add(interpreterPathPanel, gridBagConstraints);

        jLabel1.setText("Note: Use absolute paths or relative paths from Jif.jar position.");
        jPanel1.add(jLabel1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        complierPanel.add(jPanel1, gridBagConstraints);

        optionTabbedPane.addTab("Compiler Path", complierPanel);

        libraryPanel.setPreferredSize(new java.awt.Dimension(1889, 33));
        libraryPanel.setLayout(new java.awt.GridBagLayout());

        libraryPathPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        libraryPathLabel.setText(bundle.getString("JDIALOG_CONFIGPATH_LIBRARY")); // NOI18N
        libraryPathPanel.add(libraryPathLabel);

        libraryPathTextField.setPreferredSize(new java.awt.Dimension(280, 21));
        libraryPathPanel.add(libraryPathTextField);

        libraryPathButton.setText(bundle.getString("MESSAGE_BROWSE")); // NOI18N
        libraryPathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                libraryPathActionPerformed(evt);
            }
        });
        libraryPathPanel.add(libraryPathButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        libraryPanel.add(libraryPathPanel, gridBagConstraints);

        libraryPath1Panel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        libraryPath1Label.setText(bundle.getString("JDIALOG_CONFIGPATH_LIBRARY_SECONDARY1")); // NOI18N
        libraryPath1Panel.add(libraryPath1Label);

        libraryPath1TextField.setPreferredSize(new java.awt.Dimension(280, 21));
        libraryPath1Panel.add(libraryPath1TextField);

        libraryPath1Button.setText(bundle.getString("MESSAGE_BROWSE")); // NOI18N
        libraryPath1Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                libraryPath1ActionPerformed(evt);
            }
        });
        libraryPath1Panel.add(libraryPath1Button);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        libraryPanel.add(libraryPath1Panel, gridBagConstraints);

        libraryPath2Panel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        libraryPath2Label.setText(bundle.getString("JDIALOG_CONFIGPATH_LIBRARY_SECONDARY2")); // NOI18N
        libraryPath2Panel.add(libraryPath2Label);

        libraryPath2TextField.setPreferredSize(new java.awt.Dimension(280, 21));
        libraryPath2Panel.add(libraryPath2TextField);

        libraryPath2Button.setText(bundle.getString("MESSAGE_BROWSE")); // NOI18N
        libraryPath2Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                libraryPath2ActionPerformed(evt);
            }
        });
        libraryPath2Panel.add(libraryPath2Button);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        libraryPanel.add(libraryPath2Panel, gridBagConstraints);

        libraryPath3Panel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        libraryPath3Label.setText(bundle.getString("JDIALOG_CONFIGPATH_LIBRARY_SECONDARY3")); // NOI18N
        libraryPath3Panel.add(libraryPath3Label);

        libraryPath3TextField.setPreferredSize(new java.awt.Dimension(280, 21));
        libraryPath3Panel.add(libraryPath3TextField);

        libraryPath3Button.setText(bundle.getString("MESSAGE_BROWSE")); // NOI18N
        libraryPath3Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                libraryPath3ActionPerformed(evt);
            }
        });
        libraryPath3Panel.add(libraryPath3Button);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        libraryPanel.add(libraryPath3Panel, gridBagConstraints);

        jLabel2.setText("Note: Use absolute paths or relative paths from Jif.jar position.");
        jPanel3.add(jLabel2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        libraryPanel.add(jPanel3, gridBagConstraints);

        optionTabbedPane.addTab("Library Path", libraryPanel);

        glulxPanel.setPreferredSize(new java.awt.Dimension(1373, 33));
        glulxPanel.setLayout(new java.awt.GridBagLayout());

        glulxPathPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        glulxPathLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        glulxPathLabel.setText(bundle.getString("JDIALOG_CONFIGPATH_GLULXINTERPRETERPATH")); // NOI18N
        glulxPathPanel.add(glulxPathLabel);

        glulxPathTextField.setPreferredSize(new java.awt.Dimension(280, 21));
        glulxPathPanel.add(glulxPathTextField);

        glulxPathButton.setText(bundle.getString("MESSAGE_BROWSE")); // NOI18N
        glulxPathButton.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        glulxPathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                glulxPathActionPerformed(evt);
            }
        });
        glulxPathPanel.add(glulxPathButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        glulxPanel.add(glulxPathPanel, gridBagConstraints);

        bresPathPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        bresPathLabel.setText(bundle.getString("GLULX_BRES_LOCATION")); // NOI18N
        bresPathPanel.add(bresPathLabel);

        bresPathTextField.setPreferredSize(new java.awt.Dimension(280, 21));
        bresPathPanel.add(bresPathTextField);

        bresPathButton.setText(bundle.getString("MESSAGE_BROWSE")); // NOI18N
        bresPathButton.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        bresPathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bresPathActionPerformed(evt);
            }
        });
        bresPathPanel.add(bresPathButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        glulxPanel.add(bresPathPanel, gridBagConstraints);

        blcPathPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        blcPathLabel.setText(bundle.getString("GLULX_BLC_LOCATION")); // NOI18N
        blcPathPanel.add(blcPathLabel);

        blcPathTextField.setPreferredSize(new java.awt.Dimension(280, 21));
        blcPathPanel.add(blcPathTextField);

        blcPathButton.setText(bundle.getString("MESSAGE_BROWSE")); // NOI18N
        blcPathButton.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        blcPathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                blcPathActionPerformed(evt);
            }
        });
        blcPathPanel.add(blcPathButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        glulxPanel.add(blcPathPanel, gridBagConstraints);

        jLabel3.setText("Note: Use absolute paths or relative paths from Jif.jar position.");
        jPanel4.add(jLabel3);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        glulxPanel.add(jPanel4, gridBagConstraints);

        optionTabbedPane.addTab("Glulx Path", glulxPanel);

        optionDialog.getContentPane().add(optionTabbedPane, java.awt.BorderLayout.CENTER);

        optionSaveButton.setText(bundle.getString("MESSAGE_SAVE")); // NOI18N
        optionSaveButton.setMaximumSize(new java.awt.Dimension(67, 23));
        optionSaveButton.setMinimumSize(new java.awt.Dimension(67, 23));
        optionSaveButton.setPreferredSize(new java.awt.Dimension(67, 23));
        optionSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionSaveActionPerformed(evt);
            }
        });
        optionControlPanel.add(optionSaveButton);

        optionDefaultButton.setText(bundle.getString("MESSAGE_DEFAULT")); // NOI18N
        optionDefaultButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionDefaultActionPerformed(evt);
            }
        });
        optionControlPanel.add(optionDefaultButton);

        optionCancelButton.setText(bundle.getString("MESSAGE_CANCEL")); // NOI18N
        optionCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionCancelActionPerformed(evt);
            }
        });
        optionControlPanel.add(optionCancelButton);

        optionDialog.getContentPane().add(optionControlPanel, java.awt.BorderLayout.SOUTH);

        projectSwitchesPanel.setLayout(new java.awt.GridLayout(0, 4));
        projectSwitchesDialog.getContentPane().add(projectSwitchesPanel, java.awt.BorderLayout.CENTER);

        projectSwitchesSaveButton.setText(bundle.getString("MESSAGE_SAVE")); // NOI18N
        projectSwitchesSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectSwitchesSaveActionPerformed(evt);
            }
        });
        projectSwitchesControlPanel.add(projectSwitchesSaveButton);

        projectSwitchesCloseButton.setText(bundle.getString("MESSAGE_CLOSE")); // NOI18N
        projectSwitchesCloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectSwitchesCloseActionPerformed(evt);
            }
        });
        projectSwitchesControlPanel.add(projectSwitchesCloseButton);

        projectSwitchesDialog.getContentPane().add(projectSwitchesControlPanel, java.awt.BorderLayout.SOUTH);

        projectPropertiesDialog.setTitle("Project Properties");
        projectPropertiesDialog.setModal(true);

        projectPropertiesTextArea.setColumns(20);
        projectPropertiesTextArea.setRows(5);
        projectPropertiesTextArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 5, 2, 5));
        projectPropertiesScrollPane.setViewportView(projectPropertiesTextArea);

        projectPropertiesDialog.getContentPane().add(projectPropertiesScrollPane, java.awt.BorderLayout.CENTER);

        projectPropertiesSaveButton.setText(bundle.getString("MESSAGE_SAVE")); // NOI18N
        projectPropertiesSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectPropertiesSaveActionPerformed(evt);
            }
        });
        projectPropertiesControlPanel.add(projectPropertiesSaveButton);

        projectPropertiesCloseButton.setText(bundle.getString("MESSAGE_CLOSE")); // NOI18N
        projectPropertiesCloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectPropertiesCloseActionPerformed(evt);
            }
        });
        projectPropertiesControlPanel.add(projectPropertiesCloseButton);

        projectPropertiesDialog.getContentPane().add(projectPropertiesControlPanel, java.awt.BorderLayout.SOUTH);

        replaceDialog.setTitle(bundle.getString("JDIALOGREPLACE_TITLE")); // NOI18N
        replaceDialog.setModal(true);
        replaceDialog.getContentPane().setLayout(new javax.swing.BoxLayout(replaceDialog.getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        replaceFindLabel.setText(bundle.getString("JDIALOGREPLACE_FIND_LABEL")); // NOI18N
        replaceFindLabel.setPreferredSize(new java.awt.Dimension(41, 17));
        replacePanel.add(replaceFindLabel);

        replaceFindTextField.setMaximumSize(new java.awt.Dimension(111, 20));
        replaceFindTextField.setMinimumSize(new java.awt.Dimension(111, 20));
        replaceFindTextField.setPreferredSize(new java.awt.Dimension(111, 20));
        replacePanel.add(replaceFindTextField);

        replaceReplaceLabel.setText(bundle.getString("JDIALOGREPLACE_REPLACE_LABEL")); // NOI18N
        replacePanel.add(replaceReplaceLabel);

        replaceReplaceTextField.setMaximumSize(new java.awt.Dimension(111, 20));
        replaceReplaceTextField.setMinimumSize(new java.awt.Dimension(111, 20));
        replaceReplaceTextField.setPreferredSize(new java.awt.Dimension(111, 20));
        replacePanel.add(replaceReplaceTextField);

        replaceDialog.getContentPane().add(replacePanel);

        replaceFindButton.setFont(new java.awt.Font("MS Sans Serif", 0, 12));
        replaceFindButton.setText(bundle.getString("JDIALOGREPLACE_BUTTON_FIND")); // NOI18N
        replaceFindButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceFindActionPerformed(evt);
            }
        });
        replaceControlPanel.add(replaceFindButton);

        replaceReplaceButton.setFont(new java.awt.Font("MS Sans Serif", 0, 12));
        replaceReplaceButton.setText(bundle.getString("JDIALOGREPLACE_BUTTON_REPLACE")); // NOI18N
        replaceReplaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceReplaceActionPerformed(evt);
            }
        });
        replaceControlPanel.add(replaceReplaceButton);

        replaceAllButton.setFont(new java.awt.Font("MS Sans Serif", 0, 12));
        replaceAllButton.setText(bundle.getString("JDIALOGREPLACE_BUTTON_REPLACE_ALL")); // NOI18N
        replaceAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceAllActionPerformed(evt);
            }
        });
        replaceControlPanel.add(replaceAllButton);

        replaceCloseButton.setText(bundle.getString("MESSAGE_CLOSE")); // NOI18N
        replaceCloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceCloseActionPerformed(evt);
            }
        });
        replaceControlPanel.add(replaceCloseButton);

        replaceDialog.getContentPane().add(replaceControlPanel);

        switchesDialog.setTitle(bundle.getString("JDIALOG_SWITCHES_TITLE")); // NOI18N
        switchesDialog.setFont(new java.awt.Font("Arial", 0, 12));
        switchesDialog.setModal(true);

        switchesPanel.setLayout(new java.awt.GridLayout(2, 0));

        switchesUpperPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        switchesUpperPanel.setFont(new java.awt.Font("Dialog", 0, 8));
        switchesUpperPanel.setLayout(new java.awt.GridLayout(0, 4));
        switchesPanel.add(switchesUpperPanel);

        switchesLowerPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        switchesLowerPanel.setFont(new java.awt.Font("Dialog", 0, 8));
        switchesLowerPanel.setLayout(new java.awt.GridLayout(0, 3));
        switchesPanel.add(switchesLowerPanel);

        switchesDialog.getContentPane().add(switchesPanel, java.awt.BorderLayout.CENTER);

        switchesSaveButton.setText(bundle.getString("MESSAGE_SAVE")); // NOI18N
        switchesSaveButton.setMaximumSize(new java.awt.Dimension(59, 23));
        switchesSaveButton.setMinimumSize(new java.awt.Dimension(59, 23));
        switchesSaveButton.setPreferredSize(new java.awt.Dimension(59, 23));
        switchesSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                switchesSaveActionPerformed(evt);
            }
        });
        switchesControlPanel.add(switchesSaveButton);

        switchesCloseButton.setText(bundle.getString("MESSAGE_CLOSE")); // NOI18N
        switchesCloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                switchesCloseActionPerformed(evt);
            }
        });
        switchesControlPanel.add(switchesCloseButton);

        switchesDialog.getContentPane().add(switchesControlPanel, java.awt.BorderLayout.SOUTH);

        symbolDialog.setBackground(java.awt.Color.lightGray);
        symbolDialog.setUndecorated(true);

        symbolScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        symbolScrollPane.setDoubleBuffered(true);
        symbolScrollPane.setMinimumSize(new java.awt.Dimension(0, 0));
        symbolScrollPane.setPreferredSize(new java.awt.Dimension(230, 80));

        symbolList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        symbolList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                symbolListKeyPressed(evt);
            }
        });
        symbolList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                symbolListMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                symbolListMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                symbolListMouseExited(evt);
            }
        });
        symbolScrollPane.setViewportView(symbolList);

        symbolDialog.getContentPane().add(symbolScrollPane, java.awt.BorderLayout.WEST);

        textLabel.setText("jLabel5");
        textDialog.getContentPane().add(textLabel, java.awt.BorderLayout.NORTH);

        textTextArea.setEditable(false);
        textTextArea.setFont(new java.awt.Font("Courier New", 0, 12));
        textTextArea.setTabSize(4);
        textScrollPane.setViewportView(textTextArea);

        textDialog.getContentPane().add(textScrollPane, java.awt.BorderLayout.CENTER);

        textCloseButton.setText(bundle.getString("MESSAGE_OK")); // NOI18N
        textCloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textCloseActionPerformed(evt);
            }
        });
        textControlPanel.add(textCloseButton);

        textDialog.getContentPane().add(textControlPanel, java.awt.BorderLayout.SOUTH);

        tutorialLabel.setText("jLabel5");
        tutorialDialog.getContentPane().add(tutorialLabel, java.awt.BorderLayout.NORTH);

        tutorialEditorPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 5, 2, 5));
        tutorialEditorPane.setEditable(false);
        tutorialScrollPane.setViewportView(tutorialEditorPane);

        tutorialDialog.getContentPane().add(tutorialScrollPane, java.awt.BorderLayout.CENTER);

        tutorialOKButton.setText(bundle.getString("MESSAGE_OK")); // NOI18N
        tutorialOKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tutorialOKActionPerformed(evt);
            }
        });
        tutorialControlPanel.add(tutorialOKButton);

        tutorialPrintButton.setText(bundle.getString("MENUITEM_PRINT")); // NOI18N
        tutorialPrintButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tutorialPrintActionPerformed(evt);
            }
        });
        tutorialControlPanel.add(tutorialPrintButton);

        tutorialDialog.getContentPane().add(tutorialControlPanel, java.awt.BorderLayout.SOUTH);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(getJifVersion());
        setFont(new java.awt.Font("Dialog", 0, 12));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        toolbarPanel.setMinimumSize(new java.awt.Dimension(0, 0));
        toolbarPanel.setLayout(new java.awt.BorderLayout());

        jToolBarCommon.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jToolBarCommon.setFloatable(false);
        jToolBarCommon.setToolTipText("Jif Toolbar");
        jToolBarCommon.setPreferredSize(new java.awt.Dimension(400, 34));

        newButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/filenew.png"))); // NOI18N
        newButton.setToolTipText(bundle.getString("MENUITEM_NEW_TOOLTIP")); // NOI18N
        newButton.setBorderPainted(false);
        newButton.setMaximumSize(new java.awt.Dimension(29, 29));
        newButton.setMinimumSize(new java.awt.Dimension(29, 29));
        newButton.setPreferredSize(new java.awt.Dimension(29, 29));
        newButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newActionPerformed(evt);
            }
        });
        newButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(newButton);

        openButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/fileopen.png"))); // NOI18N
        openButton.setToolTipText(bundle.getString("MENUITEM_OPEN")); // NOI18N
        openButton.setBorderPainted(false);
        openButton.setMaximumSize(new java.awt.Dimension(29, 29));
        openButton.setMinimumSize(new java.awt.Dimension(29, 29));
        openButton.setPreferredSize(new java.awt.Dimension(29, 29));
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openActionPerformed(evt);
            }
        });
        openButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(openButton);

        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/filesave.png"))); // NOI18N
        saveButton.setToolTipText(bundle.getString("MENUITEM_SAVE")); // NOI18N
        saveButton.setBorderPainted(false);
        saveButton.setMaximumSize(new java.awt.Dimension(29, 29));
        saveButton.setMinimumSize(new java.awt.Dimension(29, 29));
        saveButton.setPreferredSize(new java.awt.Dimension(29, 29));
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveActionPerformed(evt);
            }
        });
        saveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(saveButton);

        saveAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/filesaveall.png"))); // NOI18N
        saveAllButton.setToolTipText(bundle.getString("MENUITEM_SAVEALL")); // NOI18N
        saveAllButton.setBorderPainted(false);
        saveAllButton.setMaximumSize(new java.awt.Dimension(29, 29));
        saveAllButton.setMinimumSize(new java.awt.Dimension(29, 29));
        saveAllButton.setPreferredSize(new java.awt.Dimension(29, 29));
        saveAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAllActionPerformed(evt);
            }
        });
        saveAllButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(saveAllButton);

        saveAsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/filesaveas.png"))); // NOI18N
        saveAsButton.setToolTipText(bundle.getString("MENUITEM_SAVEAS")); // NOI18N
        saveAsButton.setBorderPainted(false);
        saveAsButton.setMaximumSize(new java.awt.Dimension(29, 29));
        saveAsButton.setMinimumSize(new java.awt.Dimension(29, 29));
        saveAsButton.setPreferredSize(new java.awt.Dimension(29, 29));
        saveAsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsActionPerformed(evt);
            }
        });
        saveAsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(saveAsButton);

        closeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/fileclose.png"))); // NOI18N
        closeButton.setToolTipText(bundle.getString("MENUITEM_CLOSE")); // NOI18N
        closeButton.setBorderPainted(false);
        closeButton.setMaximumSize(new java.awt.Dimension(29, 29));
        closeButton.setMinimumSize(new java.awt.Dimension(29, 29));
        closeButton.setPreferredSize(new java.awt.Dimension(29, 29));
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeActionPerformed(evt);
            }
        });
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(closeButton);

        closeAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/filecloseAll.png"))); // NOI18N
        closeAllButton.setToolTipText(bundle.getString("MENUITEM_CLOSEALL")); // NOI18N
        closeAllButton.setBorderPainted(false);
        closeAllButton.setMaximumSize(new java.awt.Dimension(29, 29));
        closeAllButton.setMinimumSize(new java.awt.Dimension(29, 29));
        closeAllButton.setPreferredSize(new java.awt.Dimension(29, 29));
        closeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeAllActionPerformed(evt);
            }
        });
        closeAllButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(closeAllButton);

        undoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/undo.png"))); // NOI18N
        undoButton.setToolTipText(bundle.getString("JFRAME_UNDO")); // NOI18N
        undoButton.setBorderPainted(false);
        undoButton.setMaximumSize(new java.awt.Dimension(29, 29));
        undoButton.setMinimumSize(new java.awt.Dimension(29, 29));
        undoButton.setPreferredSize(new java.awt.Dimension(29, 29));
        undoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoActionPerformed(evt);
            }
        });
        undoButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(undoButton);

        redoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/redo.png"))); // NOI18N
        redoButton.setToolTipText(bundle.getString("JFRAME_REDO")); // NOI18N
        redoButton.setBorderPainted(false);
        redoButton.setMaximumSize(new java.awt.Dimension(29, 29));
        redoButton.setMinimumSize(new java.awt.Dimension(29, 29));
        redoButton.setPreferredSize(new java.awt.Dimension(29, 29));
        redoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redoActionPerformed(evt);
            }
        });
        redoButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(redoButton);

        commentButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/comment.png"))); // NOI18N
        commentButton.setToolTipText(bundle.getString("JFRAME_COMMENT_SELECTION")); // NOI18N
        commentButton.setBorderPainted(false);
        commentButton.setMaximumSize(new java.awt.Dimension(29, 29));
        commentButton.setMinimumSize(new java.awt.Dimension(29, 29));
        commentButton.setPreferredSize(new java.awt.Dimension(29, 29));
        commentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commentSelectionActionPerformed(evt);
            }
        });
        commentButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(commentButton);

        uncommentButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/uncomment.png"))); // NOI18N
        uncommentButton.setToolTipText(bundle.getString("JFRAME_UNCOMMENT_SELECTION")); // NOI18N
        uncommentButton.setBorderPainted(false);
        uncommentButton.setMaximumSize(new java.awt.Dimension(29, 29));
        uncommentButton.setMinimumSize(new java.awt.Dimension(29, 29));
        uncommentButton.setPreferredSize(new java.awt.Dimension(29, 29));
        uncommentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uncommentSelectionActionPerformed(evt);
            }
        });
        uncommentButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(uncommentButton);

        tabLeftButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/leftIndent.png"))); // NOI18N
        tabLeftButton.setToolTipText(bundle.getString("JFRAME_LEFTTAB_SELECTION")); // NOI18N
        tabLeftButton.setBorderPainted(false);
        tabLeftButton.setMaximumSize(new java.awt.Dimension(29, 29));
        tabLeftButton.setMinimumSize(new java.awt.Dimension(29, 29));
        tabLeftButton.setPreferredSize(new java.awt.Dimension(29, 29));
        tabLeftButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tabLeftActionPerformed(evt);
            }
        });
        tabLeftButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(tabLeftButton);

        tabRightButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rightIndent.png"))); // NOI18N
        tabRightButton.setToolTipText(bundle.getString("JFRAME_RIGHTTAB_SELECTION")); // NOI18N
        tabRightButton.setBorderPainted(false);
        tabRightButton.setMaximumSize(new java.awt.Dimension(29, 29));
        tabRightButton.setMinimumSize(new java.awt.Dimension(29, 29));
        tabRightButton.setPreferredSize(new java.awt.Dimension(29, 29));
        tabRightButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tabRightActionPerformed(evt);
            }
        });
        tabRightButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(tabRightButton);

        bracketCheckButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/check.png"))); // NOI18N
        bracketCheckButton.setToolTipText(bundle.getString("JFRAME_CHECK_BRACKETS")); // NOI18N
        bracketCheckButton.setBorderPainted(false);
        bracketCheckButton.setMaximumSize(new java.awt.Dimension(29, 29));
        bracketCheckButton.setMinimumSize(new java.awt.Dimension(29, 29));
        bracketCheckButton.setPreferredSize(new java.awt.Dimension(29, 29));
        bracketCheckButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bracketCheckActionPerformed(evt);
            }
        });
        bracketCheckButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(bracketCheckButton);

        buildAllButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/compfile.png"))); // NOI18N
        buildAllButton.setToolTipText(bundle.getString("MENUITEM_BUILDALL")); // NOI18N
        buildAllButton.setBorderPainted(false);
        buildAllButton.setMaximumSize(new java.awt.Dimension(29, 29));
        buildAllButton.setMinimumSize(new java.awt.Dimension(29, 29));
        buildAllButton.setPreferredSize(new java.awt.Dimension(29, 29));
        buildAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buildAllActionPerformed(evt);
            }
        });
        buildAllButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(buildAllButton);

        runButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/launch.png"))); // NOI18N
        runButton.setToolTipText(bundle.getString("MENUITEM_RUN")); // NOI18N
        runButton.setBorderPainted(false);
        runButton.setMaximumSize(new java.awt.Dimension(29, 29));
        runButton.setMinimumSize(new java.awt.Dimension(29, 29));
        runButton.setPreferredSize(new java.awt.Dimension(29, 29));
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runActionPerformed(evt);
            }
        });
        runButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(runButton);

        insertSymbolButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/insertSymbol.png"))); // NOI18N
        insertSymbolButton.setToolTipText(bundle.getString("JFRAME_INSERT_SYMBOL")); // NOI18N
        insertSymbolButton.setBorderPainted(false);
        insertSymbolButton.setMaximumSize(new java.awt.Dimension(29, 29));
        insertSymbolButton.setMinimumSize(new java.awt.Dimension(29, 29));
        insertSymbolButton.setPreferredSize(new java.awt.Dimension(29, 29));
        insertSymbolButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertSymbolActionPerformed(evt);
            }
        });
        insertSymbolButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(insertSymbolButton);

        interpreterButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/runInterpreter.png"))); // NOI18N
        interpreterButton.setToolTipText(bundle.getString("JFRAME_RUN_INTERPRETER")); // NOI18N
        interpreterButton.setBorderPainted(false);
        interpreterButton.setMaximumSize(new java.awt.Dimension(29, 29));
        interpreterButton.setMinimumSize(new java.awt.Dimension(29, 29));
        interpreterButton.setPreferredSize(new java.awt.Dimension(29, 29));
        interpreterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                interpreterActionPerformed(evt);
            }
        });
        interpreterButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(interpreterButton);

        switchManagerButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/edit.png"))); // NOI18N
        switchManagerButton.setToolTipText(bundle.getString("MENUITEM_SWITCHES_TOOLTIP")); // NOI18N
        switchManagerButton.setBorderPainted(false);
        switchManagerButton.setMaximumSize(new java.awt.Dimension(29, 29));
        switchManagerButton.setMinimumSize(new java.awt.Dimension(29, 29));
        switchManagerButton.setPreferredSize(new java.awt.Dimension(29, 29));
        switchManagerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                switchManagerActionPerformed(evt);
            }
        });
        switchManagerButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(switchManagerButton);

        settingsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/configure.png"))); // NOI18N
        settingsButton.setToolTipText(bundle.getString("JFRAME_SETTING")); // NOI18N
        settingsButton.setBorderPainted(false);
        settingsButton.setMaximumSize(new java.awt.Dimension(29, 29));
        settingsButton.setMinimumSize(new java.awt.Dimension(29, 29));
        settingsButton.setPreferredSize(new java.awt.Dimension(29, 29));
        settingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsActionPerformed(evt);
            }
        });
        settingsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(settingsButton);

        findTextField.setColumns(15);
        findTextField.setFont(new java.awt.Font("Courier New", 0, 12));
        findTextField.setToolTipText(bundle.getString("JTOOLBAR_SEARCH")); // NOI18N
        findTextField.setMaximumSize(new java.awt.Dimension(111, 20));
        findTextField.setMinimumSize(new java.awt.Dimension(10, 22));
        jToolBarCommon.add(findTextField);

        findButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/filefind.png"))); // NOI18N
        findButton.setToolTipText(bundle.getString("JFRAME_SEARCH_BUTTON")); // NOI18N
        findButton.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        findButton.setBorderPainted(false);
        findButton.setMaximumSize(new java.awt.Dimension(29, 29));
        findButton.setMinimumSize(new java.awt.Dimension(29, 29));
        findButton.setPreferredSize(new java.awt.Dimension(29, 29));
        findButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findActionPerformed(evt);
            }
        });
        findButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(findButton);

        replaceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/find.png"))); // NOI18N
        replaceButton.setToolTipText(bundle.getString("MENUITEM_REPLACE")); // NOI18N
        replaceButton.setBorderPainted(false);
        replaceButton.setMaximumSize(new java.awt.Dimension(29, 29));
        replaceButton.setMinimumSize(new java.awt.Dimension(29, 29));
        replaceButton.setPreferredSize(new java.awt.Dimension(29, 29));
        replaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceActionPerformed(evt);
            }
        });
        replaceButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        jToolBarCommon.add(replaceButton);

        rowColTextField.setEditable(false);
        rowColTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        rowColTextField.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        rowColTextField.setDisabledTextColor(new java.awt.Color(212, 208, 200));
        rowColTextField.setMaximumSize(new java.awt.Dimension(100, 38));
        rowColTextField.setMinimumSize(new java.awt.Dimension(50, 38));
        rowColTextField.setPreferredSize(new java.awt.Dimension(50, 19));
        jToolBarCommon.add(rowColTextField);

        toolbarPanel.add(jToolBarCommon, java.awt.BorderLayout.NORTH);

        getContentPane().add(toolbarPanel, java.awt.BorderLayout.NORTH);

        mainSplitPane.setDividerSize(3);
        mainSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setDoubleBuffered(true);

        upperSplitPane.setDividerSize(3);
        upperSplitPane.setDoubleBuffered(true);
        upperSplitPane.setMinimumSize(new java.awt.Dimension(180, 248));
        upperSplitPane.setPreferredSize(new java.awt.Dimension(180, 328));

        leftTabbedPane.setFont(new java.awt.Font("Dialog", 0, 11));

        treePanel.setLayout(new javax.swing.BoxLayout(treePanel, javax.swing.BoxLayout.Y_AXIS));

        treeScrollPane.setBorder(null);
        treeScrollPane.setDoubleBuffered(true);
        treeScrollPane.setMinimumSize(new java.awt.Dimension(150, 200));
        treeScrollPane.setPreferredSize(new java.awt.Dimension(150, 300));

        treeTree.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        treeTree.setFont(new java.awt.Font("Courier New", 0, 12));
        treeTree.setMaximumSize(new java.awt.Dimension(0, 0));
        treeTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                treeTreeMouseEntered(evt);
            }
        });
        treeTree.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
            public void treeCollapsed(javax.swing.event.TreeExpansionEvent evt) {
            }
            public void treeExpanded(javax.swing.event.TreeExpansionEvent evt) {
                treeTreeTreeExpanded(evt);
            }
        });
        treeTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                treeTreeValueChanged(evt);
            }
        });
        treeScrollPane.setViewportView(treeTree);

        treePanel.add(treeScrollPane);

        leftTabbedPane.addTab(bundle.getString("JFRAME_TREE"), treePanel); // NOI18N

        projectPanel.setLayout(new javax.swing.BoxLayout(projectPanel, javax.swing.BoxLayout.Y_AXIS));

        projectScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Project", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11))); // NOI18N
        projectScrollPane.setFont(new java.awt.Font("Dialog", 0, 11));
        projectScrollPane.setPreferredSize(new java.awt.Dimension(90, 131));

        projectList.setFont(new java.awt.Font("Dialog", 0, 11));
        projectList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                projectListMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                projectListMouseEntered(evt);
            }
        });
        projectScrollPane.setViewportView(projectList);

        projectPanel.add(projectScrollPane);

        mainFileLabel.setFont(new java.awt.Font("Dialog", 0, 11));
        mainFileLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        mainFileLabel.setText("Main:");
        mainFileLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        projectPanel.add(mainFileLabel);

        leftTabbedPane.addTab(bundle.getString("PROJECT_PROJECT"), projectPanel); // NOI18N

        searchPanel.setMaximumSize(new java.awt.Dimension(1800, 220));
        searchPanel.setMinimumSize(new java.awt.Dimension(180, 220));
        searchPanel.setPreferredSize(new java.awt.Dimension(180, 220));
        searchPanel.setLayout(new javax.swing.BoxLayout(searchPanel, javax.swing.BoxLayout.Y_AXIS));

        searchProjectPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Search all project files", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11))); // NOI18N
        searchProjectPanel.setMaximumSize(new java.awt.Dimension(180, 55));
        searchProjectPanel.setMinimumSize(new java.awt.Dimension(180, 55));
        searchProjectPanel.setPreferredSize(new java.awt.Dimension(180, 55));
        searchProjectPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));

        searchProjectTextField.setFont(new java.awt.Font("Courier New", 0, 12));
        searchProjectTextField.setToolTipText(bundle.getString("JTOOLBAR_SEARCH")); // NOI18N
        searchProjectTextField.setMaximumSize(new java.awt.Dimension(150, 20));
        searchProjectTextField.setMinimumSize(new java.awt.Dimension(30, 22));
        searchProjectTextField.setPreferredSize(new java.awt.Dimension(90, 20));
        searchProjectPanel.add(searchProjectTextField);

        searchProjectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/fileprojectfind.png"))); // NOI18N
        searchProjectButton.setToolTipText(bundle.getString("JFRAME_SEARCHALL_BUTTON")); // NOI18N
        searchProjectButton.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        searchProjectButton.setBorderPainted(false);
        searchProjectButton.setMaximumSize(new java.awt.Dimension(29, 29));
        searchProjectButton.setMinimumSize(new java.awt.Dimension(29, 29));
        searchProjectButton.setPreferredSize(new java.awt.Dimension(29, 29));
        searchProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchProjectActionPerformed(evt);
            }
        });
        searchProjectButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        searchProjectPanel.add(searchProjectButton);

        searchPanel.add(searchProjectPanel);

        definitionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Search for Definition", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11))); // NOI18N
        definitionPanel.setMaximumSize(new java.awt.Dimension(180, 55));
        definitionPanel.setMinimumSize(new java.awt.Dimension(180, 55));
        definitionPanel.setPreferredSize(new java.awt.Dimension(180, 55));
        definitionPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));

        definitionTextField.setFont(new java.awt.Font("Courier New", 0, 12));
        definitionTextField.setMaximumSize(new java.awt.Dimension(150, 20));
        definitionTextField.setMinimumSize(new java.awt.Dimension(30, 20));
        definitionTextField.setPreferredSize(new java.awt.Dimension(90, 20));
        definitionPanel.add(definitionTextField);

        definitionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/filefind.png"))); // NOI18N
        definitionButton.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        definitionButton.setBorderPainted(false);
        definitionButton.setMaximumSize(new java.awt.Dimension(29, 29));
        definitionButton.setMinimumSize(new java.awt.Dimension(29, 29));
        definitionButton.setPreferredSize(new java.awt.Dimension(29, 29));
        definitionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                definitionActionPerformed(evt);
            }
        });
        definitionButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });
        definitionPanel.add(definitionButton);

        searchPanel.add(definitionPanel);

        leftTabbedPane.addTab(bundle.getString("JFRAME_SEARCH"), searchPanel); // NOI18N

        upperSplitPane.setLeftComponent(leftTabbedPane);

        filePanel.setLayout(new java.awt.BorderLayout());

        fileTabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        fileTabbedPane.setFont(new java.awt.Font("Dialog", 1, 11));
        fileTabbedPane.setMinimumSize(new java.awt.Dimension(350, 350));
        fileTabbedPane.setPreferredSize(new java.awt.Dimension(700, 450));
        fileTabbedPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fileTabbedPaneMouseClicked(evt);
            }
        });
        fileTabbedPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                fileTabbedPaneComponentShown(evt);
            }
        });
        filePanel.add(fileTabbedPane, java.awt.BorderLayout.CENTER);

        upperSplitPane.setRightComponent(filePanel);

        mainSplitPane.setTopComponent(upperSplitPane);

        outputTabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        outputTabbedPane.setAutoscrolls(true);
        outputTabbedPane.setFont(new java.awt.Font("Dialog", 0, 11));
        outputTabbedPane.setMinimumSize(new java.awt.Dimension(31, 100));
        outputTabbedPane.setPreferredSize(new java.awt.Dimension(30, 150));

        outputScrollPane.setAutoscrolls(true);

        outputTextArea.setEditable(false);
        outputTextArea.setFont(new java.awt.Font("Tahoma", 0, 11));
        outputTextArea.setTabSize(4);
        outputTextArea.setAutoscrolls(false);
        outputTextArea.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 5, 2, 5));
        outputTextArea.setMinimumSize(new java.awt.Dimension(0, 45));
        outputTextArea.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                outputTextAreaMouseMoved(evt);
            }
        });
        outputTextArea.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                outputTextAreaMouseClicked(evt);
            }
        });
        outputScrollPane.setViewportView(outputTextArea);

        outputTabbedPane.addTab(bundle.getString("JFRAME_OUTPUT"), outputScrollPane); // NOI18N
        outputScrollPane.getAccessibleContext().setAccessibleParent(outputTabbedPane);

        mainSplitPane.setBottomComponent(outputTabbedPane);

        getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);

        mainMenuBar.setBorder(null);
        mainMenuBar.setFont(new java.awt.Font("Dialog", 0, 12));

        fileMenu.setText(bundle.getString("MENU_FILE")); // NOI18N
        fileMenu.setDelay(0);
        fileMenu.setFont(new java.awt.Font("Dialog", 0, 11));

        newMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        newMenuItem.setText(bundle.getString("MENUITEM_NEW")); // NOI18N
        newMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newActionPerformed(evt);
            }
        });
        fileMenu.add(newMenuItem);

        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        openMenuItem.setText(bundle.getString("MENUITEM_OPEN")); // NOI18N
        openMenuItem.setToolTipText(bundle.getString("MENUITEM_OPEN_TOOLTIP")); // NOI18N
        openMenuItem.setName("Open"); // NOI18N
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);
        fileMenu.add(jSeparator8);

        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        saveMenuItem.setText(bundle.getString("MENUITEM_SAVE")); // NOI18N
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        saveAsMenuItem.setText(bundle.getString("MENUITEM_SAVEAS")); // NOI18N
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsActionPerformed(evt);
            }
        });
        fileMenu.add(saveAsMenuItem);

        saveAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        saveAllMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        saveAllMenuItem.setText(bundle.getString("MENUITEM_SAVEALL")); // NOI18N
        saveAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAllActionPerformed(evt);
            }
        });
        fileMenu.add(saveAllMenuItem);
        fileMenu.add(jSeparator10);

        closeMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        closeMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        closeMenuItem.setText(bundle.getString("MENUITEM_CLOSE")); // NOI18N
        closeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeActionPerformed(evt);
            }
        });
        fileMenu.add(closeMenuItem);

        closeAllMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        closeAllMenuItem.setText(bundle.getString("MENUITEM_CLOSEALL")); // NOI18N
        closeAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeAllActionPerformed(evt);
            }
        });
        fileMenu.add(closeAllMenuItem);
        fileMenu.add(jSeparator9);

        recentFilesMenu.setText(bundle.getString("MENUITEM_RECENTFILES")); // NOI18N
        recentFilesMenu.setFont(new java.awt.Font("Dialog", 0, 11));
        fileMenu.add(recentFilesMenu);

        clearRecentFilesMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        clearRecentFilesMenuItem.setText(bundle.getString("MENUITEM_CLEARRECENTFILES")); // NOI18N
        clearRecentFilesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearRecentFilesActionPerformed(evt);
            }
        });
        fileMenu.add(clearRecentFilesMenuItem);
        fileMenu.add(jSeparator1);

        printMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        printMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        printMenuItem.setText(bundle.getString("MENUITEM_PRINT")); // NOI18N
        printMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printActionPerformed(evt);
            }
        });
        fileMenu.add(printMenuItem);
        fileMenu.add(jSeparator4);

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK));
        exitMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        exitMenuItem.setText(bundle.getString("MENUITEM_EXIT")); // NOI18N
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        mainMenuBar.add(fileMenu);

        editMenu.setText(bundle.getString("MENU_EDIT")); // NOI18N
        editMenu.setDelay(0);
        editMenu.setFont(new java.awt.Font("Dialog", 0, 11));

        cutMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        cutMenuItem.setText(bundle.getString("JFRAME_EDIT_CUT")); // NOI18N
        editMenu.add(cutMenuItem);

        copyMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        copyMenuItem.setText(bundle.getString("MENUITEM_COPY")); // NOI18N
        copyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyActionPerformed(evt);
            }
        });
        editMenu.add(copyMenuItem);

        pasteMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        pasteMenuItem.setText(bundle.getString("JFRAME_EDIT_PASTE")); // NOI18N
        editMenu.add(pasteMenuItem);
        editMenu.add(jSeparator11);

        searchMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
        searchMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        searchMenuItem.setText(bundle.getString("JFRAME_SEARCH")); // NOI18N
        searchMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchActionPerformed(evt);
            }
        });
        editMenu.add(searchMenuItem);

        searchAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, java.awt.event.InputEvent.CTRL_MASK));
        searchAllMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        searchAllMenuItem.setText(bundle.getString("JFRAME_SEARCHALL")); // NOI18N
        searchAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchAllctionPerformed(evt);
            }
        });
        editMenu.add(searchAllMenuItem);

        replaceMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        replaceMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        replaceMenuItem.setText(bundle.getString("MENUITEM_REPLACE")); // NOI18N
        replaceMenuItem.setToolTipText(bundle.getString("MENUITEM_REPLACE_TOOLTIP")); // NOI18N
        replaceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceActionPerformed(evt);
            }
        });
        editMenu.add(replaceMenuItem);

        selectAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        selectAllMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        selectAllMenuItem.setText(bundle.getString("MENUITEM_SELECTALL")); // NOI18N
        selectAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllActionPerformed(evt);
            }
        });
        editMenu.add(selectAllMenuItem);

        clearAllMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        clearAllMenuItem.setText(bundle.getString("MENUITEM_DELETEALL")); // NOI18N
        clearAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearAllActionPerformed(evt);
            }
        });
        editMenu.add(clearAllMenuItem);
        editMenu.add(jSeparator16);

        commentSelectionMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_PERIOD, java.awt.event.InputEvent.CTRL_MASK));
        commentSelectionMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        commentSelectionMenuItem.setText(bundle.getString("POPUPMENU_MENUITEM_COMMENT")); // NOI18N
        commentSelectionMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commentSelectionActionPerformed(evt);
            }
        });
        editMenu.add(commentSelectionMenuItem);

        uncommentSelectionMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_COMMA, java.awt.event.InputEvent.CTRL_MASK));
        uncommentSelectionMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        uncommentSelectionMenuItem.setText(bundle.getString("JFRAME_EDIT_UNCOMMENT_SELECTION")); // NOI18N
        uncommentSelectionMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uncommentSelectionActionPerformed(evt);
            }
        });
        editMenu.add(uncommentSelectionMenuItem);

        tabRightMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        tabRightMenuItem.setText(bundle.getString("MENUITEM_RIGHTSHIFT")); // NOI18N
        tabRightMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tabRightActionPerformed(evt);
            }
        });
        editMenu.add(tabRightMenuItem);

        tabLeftMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        tabLeftMenuItem.setText(bundle.getString("MENUITEM_LEFTSHIFT")); // NOI18N
        tabLeftMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tabLeftActionPerformed(evt);
            }
        });
        editMenu.add(tabLeftMenuItem);
        editMenu.add(jSeparator17);

        insertFileMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        insertFileMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        insertFileMenuItem.setText(bundle.getString("JFRAME_INSERT_FROM_FILE")); // NOI18N
        insertFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertFileActionPerformed(evt);
            }
        });
        editMenu.add(insertFileMenuItem);

        insertSymbolMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        insertSymbolMenuItem.setText(bundle.getString("JFRAME_INSERT_SYMBOL")); // NOI18N
        insertSymbolMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertSymbolActionPerformed(evt);
            }
        });
        editMenu.add(insertSymbolMenuItem);

        setBookmarkMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, java.awt.event.InputEvent.CTRL_MASK));
        setBookmarkMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        setBookmarkMenuItem.setText(bundle.getString("MENUITEM_SETBOOKMARK")); // NOI18N
        setBookmarkMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setBookmarkMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(setBookmarkMenuItem);

        nextBookmarkMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
        nextBookmarkMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        nextBookmarkMenuItem.setText(bundle.getString("MENUITEM_NEXTBOOKMARK")); // NOI18N
        nextBookmarkMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextBookmarkMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(nextBookmarkMenuItem);

        extractStringsMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        extractStringsMenuItem.setText("Extract Strings");
        extractStringsMenuItem.setToolTipText(bundle.getString("JFRAME_EXTRACT_STRINGS")); // NOI18N
        extractStringsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extractStringsActionPerformed(evt);
            }
        });
        editMenu.add(extractStringsMenuItem);

        translateMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        translateMenuItem.setText("Translate Strings");
        translateMenuItem.setToolTipText(bundle.getString("JFRAME_TRANSLATE")); // NOI18N
        translateMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                translateActionPerformed(evt);
            }
        });
        editMenu.add(translateMenuItem);

        mainMenuBar.add(editMenu);

        viewMenu.setText(bundle.getString("MENU_VIEW")); // NOI18N
        viewMenu.setDelay(0);
        viewMenu.setFont(new java.awt.Font("Dialog", 0, 11));

        outputCheckBoxMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        outputCheckBoxMenuItem.setSelected(true);
        outputCheckBoxMenuItem.setText(bundle.getString("CHECKBOX_OUTPUT")); // NOI18N
        outputCheckBoxMenuItem.setToolTipText(bundle.getString("CHECKBOX_OUTPUT_TOOLTIP")); // NOI18N
        outputCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputCheckBoxActionPerformed(evt);
            }
        });
        viewMenu.add(outputCheckBoxMenuItem);

        toolbarCheckBoxMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        toolbarCheckBoxMenuItem.setSelected(true);
        toolbarCheckBoxMenuItem.setText(bundle.getString("CHECKBOX_JTOOLBAR")); // NOI18N
        toolbarCheckBoxMenuItem.setToolTipText(bundle.getString("CHECKBOX_JTOOLBAR_TOOLTIP")); // NOI18N
        toolbarCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolbarCheckBoxActionPerformed(evt);
            }
        });
        viewMenu.add(toolbarCheckBoxMenuItem);

        treeCheckBoxMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        treeCheckBoxMenuItem.setSelected(true);
        treeCheckBoxMenuItem.setText(bundle.getString("CHECKBOX_JTREE")); // NOI18N
        treeCheckBoxMenuItem.setToolTipText(bundle.getString("CHECKBOX_JTREE_TOOLTIP")); // NOI18N
        treeCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                treeCheckBoxActionPerformed(evt);
            }
        });
        viewMenu.add(treeCheckBoxMenuItem);

        toggleFullscreenCheckBoxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F11, 0));
        toggleFullscreenCheckBoxMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        toggleFullscreenCheckBoxMenuItem.setText(bundle.getString("MENUITEM_TOGGLEFULLSCREEN")); // NOI18N
        toggleFullscreenCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleFullscreenCheckBoxActionPerformed(evt);
            }
        });
        viewMenu.add(toggleFullscreenCheckBoxMenuItem);

        mainMenuBar.add(viewMenu);

        projectMenu.setText(bundle.getString("PROJECT_PROJECT")); // NOI18N
        projectMenu.setDelay(0);
        projectMenu.setFont(new java.awt.Font("Dialog", 0, 11));

        newProjectMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.CTRL_MASK));
        newProjectMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        newProjectMenuItem.setText(bundle.getString("PROJECT_NEW_PROJECT")); // NOI18N
        newProjectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newProjectActionPerformed(evt);
            }
        });
        projectMenu.add(newProjectMenuItem);

        openProjectMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, 0));
        openProjectMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        openProjectMenuItem.setText(bundle.getString("PROJECT_OPEN_PROJECT")); // NOI18N
        openProjectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openProjectActionPerformed(evt);
            }
        });
        projectMenu.add(openProjectMenuItem);

        saveProjectMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        saveProjectMenuItem.setText(bundle.getString("PROJECT_SAVE_PROJECT")); // NOI18N
        saveProjectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveProjectActionPerformed(evt);
            }
        });
        projectMenu.add(saveProjectMenuItem);

        closeProjectMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        closeProjectMenuItem.setText(bundle.getString("PROJECT_CLOSE_PROJECT")); // NOI18N
        closeProjectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeProjectActionPerformed(evt);
            }
        });
        projectMenu.add(closeProjectMenuItem);
        projectMenu.add(jSeparator14);

        addNewToProjectMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        addNewToProjectMenuItem.setText(bundle.getString("PROJECT_ADD_NEWFILE_TO_PROJECT")); // NOI18N
        addNewToProjectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewToProjectActionPerformed(evt);
            }
        });
        projectMenu.add(addNewToProjectMenuItem);

        addFileToProjectMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        addFileToProjectMenuItem.setText(bundle.getString("PROJECT_ADD_FILE_TO_PROJECT")); // NOI18N
        addFileToProjectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFileToProjectActionPerformed(evt);
            }
        });
        projectMenu.add(addFileToProjectMenuItem);

        removeFromProjectMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        removeFromProjectMenuItem.setText(bundle.getString("PROJECT_POPUP_REMOVE")); // NOI18N
        removeFromProjectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeFromProjectActionPerformed(evt);
            }
        });
        projectMenu.add(removeFromProjectMenuItem);

        projectPropertiesMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        projectPropertiesMenuItem.setText("Project Properties");
        projectPropertiesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectPropertiesActionPerformed(evt);
            }
        });
        projectMenu.add(projectPropertiesMenuItem);

        projectSwitchesMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        projectSwitchesMenuItem.setText("Project Switches");
        projectSwitchesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectSwitchesActionPerformed(evt);
            }
        });
        projectMenu.add(projectSwitchesMenuItem);
        projectMenu.add(jSeparator5);

        lastProjectMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        lastProjectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lastProjectActionPerformed(evt);
            }
        });
        projectMenu.add(lastProjectMenuItem);

        mainMenuBar.add(projectMenu);

        modeMenu.setText("Mode");
        modeMenu.setFont(new java.awt.Font("Dialog", 0, 11));

        informModeCheckBoxMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        informModeCheckBoxMenuItem.setText("Inform Mode");
        informModeCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                informModeActionPerformed(evt);
            }
        });
        modeMenu.add(informModeCheckBoxMenuItem);

        glulxModeCheckBoxMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        glulxModeCheckBoxMenuItem.setText("Glulx Mode");
        glulxModeCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                glulxModeActionPerformed(evt);
            }
        });
        modeMenu.add(glulxModeCheckBoxMenuItem);

        mainMenuBar.add(modeMenu);

        buildMenu.setText(bundle.getString("MENU_BUILD")); // NOI18N
        buildMenu.setDelay(0);
        buildMenu.setFont(new java.awt.Font("Dialog", 0, 11));

        buildAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F9, 0));
        buildAllMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        buildAllMenuItem.setText(bundle.getString("MENUITEM_BUILDALL")); // NOI18N
        buildAllMenuItem.setToolTipText(bundle.getString("MENUITEM_BUILDALL_TOOLTIP")); // NOI18N
        buildAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buildAllActionPerformed(evt);
            }
        });
        buildMenu.add(buildAllMenuItem);

        switchesMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        switchesMenuItem.setText(bundle.getString("MENUITEM_SWITCHES")); // NOI18N
        switchesMenuItem.setToolTipText(bundle.getString("MENUITEM_SWITCHES_TOOLTIP")); // NOI18N
        switchesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                switchesActionPerformed(evt);
            }
        });
        buildMenu.add(switchesMenuItem);
        buildMenu.add(jSeparator2);

        runMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F9, java.awt.event.InputEvent.CTRL_MASK));
        runMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        runMenuItem.setText(bundle.getString("MENUITEM_RUN")); // NOI18N
        runMenuItem.setToolTipText(bundle.getString("MENUITEM_RUN_TOOLTIP")); // NOI18N
        runMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runActionPerformed(evt);
            }
        });
        buildMenu.add(runMenuItem);

        mainMenuBar.add(buildMenu);

        glulxMenu.setText("Glulx");
        glulxMenu.setEnabled(false);
        glulxMenu.setFont(new java.awt.Font("Dialog", 0, 11));

        buildAllGlulxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F12, 0));
        buildAllGlulxMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        buildAllGlulxMenuItem.setText(bundle.getString("MENUITEM_BUILD_ALL")); // NOI18N
        buildAllGlulxMenuItem.setToolTipText(bundle.getString("MENUITEM_BUILD_ALL_TOOLTIP")); // NOI18N
        buildAllGlulxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buildAllGlulxActionPerformed(evt);
            }
        });
        glulxMenu.add(buildAllGlulxMenuItem);
        glulxMenu.add(jSeparator18);

        makeResourceMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        makeResourceMenuItem.setText(bundle.getString("MENUITEM_MAKE_RES")); // NOI18N
        makeResourceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makeResourceActionPerformed(evt);
            }
        });
        glulxMenu.add(makeResourceMenuItem);

        compileMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        compileMenuItem.setText(bundle.getString("MENUITEM_COMPILE_INF")); // NOI18N
        compileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compileActionPerformed(evt);
            }
        });
        glulxMenu.add(compileMenuItem);

        makeBlbMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F12, java.awt.event.InputEvent.CTRL_MASK));
        makeBlbMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        makeBlbMenuItem.setText(bundle.getString("MENUITEM_MAKE_BLB")); // NOI18N
        makeBlbMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makeBlbActionPerformed(evt);
            }
        });
        glulxMenu.add(makeBlbMenuItem);
        glulxMenu.add(jSeparator15);

        runUlxMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        runUlxMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        runUlxMenuItem.setText(bundle.getString("MENUITEM_RUN_ULX")); // NOI18N
        runUlxMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runUlxActionPerformed(evt);
            }
        });
        glulxMenu.add(runUlxMenuItem);

        runBlbMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, java.awt.event.InputEvent.CTRL_MASK));
        runBlbMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        runBlbMenuItem.setText(bundle.getString("MENUITEM_RUN_BLB")); // NOI18N
        runBlbMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runBlbActionPerformed(evt);
            }
        });
        glulxMenu.add(runBlbMenuItem);

        mainMenuBar.add(glulxMenu);

        optionsMenu.setText(bundle.getString("MENU_OPTIONS")); // NOI18N
        optionsMenu.setDelay(0);
        optionsMenu.setFont(new java.awt.Font("Dialog", 0, 11));

        configFileMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        configFileMenuItem.setText("Jif.cfg file");
        configFileMenuItem.setToolTipText("Edit configuration file for JIF");
        configFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configFileActionPerformed(evt);
            }
        });
        optionsMenu.add(configFileMenuItem);

        settingsMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        settingsMenuItem.setText(bundle.getString("JFRAME_SETTING")); // NOI18N
        settingsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsActionPerformed(evt);
            }
        });
        optionsMenu.add(settingsMenuItem);
        optionsMenu.add(jSeparator12);

        garbageCollectionMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        garbageCollectionMenuItem.setText("Garbage Collector");
        garbageCollectionMenuItem.setToolTipText("Free unused object from the memory");
        garbageCollectionMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                garbageCollectionActionPerformed(evt);
            }
        });
        optionsMenu.add(garbageCollectionMenuItem);

        mainMenuBar.add(optionsMenu);

        helpMenu.setText(bundle.getString("MENU_HELP")); // NOI18N
        helpMenu.setDelay(0);
        helpMenu.setFont(new java.awt.Font("Dialog", 0, 11));

        readMeMenuItem.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        readMeMenuItem.setText(bundle.getString("README")); // NOI18N
        readMeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readMeActionPerformed(evt);
            }
        });
        helpMenu.add(readMeMenuItem);

        changelogMenuItem.setText("Changelog");
        changelogMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changelogMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(changelogMenuItem);
        helpMenu.add(jSeparator7);

        aboutMenuItem.setFont(new java.awt.Font("Dialog", 0, 11));
        aboutMenuItem.setText(bundle.getString("ABOUTJIF")); // NOI18N
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        mainMenuBar.add(helpMenu);

        setJMenuBar(mainMenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void highlightSelectedComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_highlightSelectedComboBoxActionPerformed
        updateHighlights();
    }//GEN-LAST:event_highlightSelectedComboBoxActionPerformed

    private void warningColorButtonnormalColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_warningColorButtonnormalColorActionPerformed
        Color temp = JColorChooser.showDialog(this,
                "Color Dialog",
                optionContext.getForeground(InformSyntax.Warnings));
        if (temp == null) {
            return;
        }
        optionContext.setForeground(InformSyntax.Warnings, temp);
        updateWarningsColor();
    }//GEN-LAST:event_warningColorButtonnormalColorActionPerformed

    private void defaultLightHighlightButtondefaultLightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultLightHighlightButtondefaultLightActionPerformed
        defaultLightHighlights();
    }//GEN-LAST:event_defaultLightHighlightButtondefaultLightActionPerformed

    private void defaultDarkHighlightButtondefaultDarkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultDarkHighlightButtondefaultDarkActionPerformed
        defaultDarkHighlights();
    }//GEN-LAST:event_defaultDarkHighlightButtondefaultDarkActionPerformed

    private void jumpToColorButtonkeywordColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jumpToColorButtonkeywordColorActionPerformed
        Color temp = JColorChooser.showDialog(this,
                "Color Dialog",
                optionContext.getForeground(InformSyntax.JumpTo));
        if (temp == null) {
            return;
        }
        optionContext.setForeground(InformSyntax.JumpTo, temp);
        updateJumpToColor();
    }//GEN-LAST:event_jumpToColorButtonkeywordColorActionPerformed

    private void errorColorButtoncommentColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_errorColorButtoncommentColorActionPerformed
        Color temp = JColorChooser.showDialog(this,
                "Color Dialog",
                optionContext.getForeground(InformSyntax.Errors));
        if (temp == null) {
            return;
        }
        optionContext.setForeground(InformSyntax.Errors, temp);
        updateErrorsColor();
    }//GEN-LAST:event_errorColorButtoncommentColorActionPerformed

    private void bookmarkColorButtonattributeColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bookmarkColorButtonattributeColorActionPerformed
        Color temp = JColorChooser.showDialog(this,
                "Color Dialog",
                optionContext.getForeground(InformSyntax.Bookmarks));
        if (temp == null) {
            return;
        }
        optionContext.setForeground(InformSyntax.Bookmarks, temp);
        updateBookmarksColor();
    }//GEN-LAST:event_bookmarkColorButtonattributeColorActionPerformed

    private void bracketColorButtonbackgroundColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bracketColorButtonbackgroundColorActionPerformed
        Color temp = JColorChooser.showDialog(this,
                "Color Dialog",
                optionContext.getForeground(InformSyntax.Brackets));
        if (temp == null) {
            return;
        }
        optionContext.setForeground(InformSyntax.Brackets, temp);
        updateBracketsColor();
    }//GEN-LAST:event_bracketColorButtonbackgroundColorActionPerformed

    private void projectSwitchesSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectSwitchesSaveActionPerformed
        projectSwitchesSave();
    }//GEN-LAST:event_projectSwitchesSaveActionPerformed

    private void optionCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionCancelActionPerformed
        optionDialog.setVisible(false);
    }//GEN-LAST:event_optionCancelActionPerformed

    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        exitJif();
    }//GEN-LAST:event_exitForm

    private void symbolListKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_symbolListKeyPressed
        if ((evt.getKeyCode() == KeyEvent.VK_ENTER)) {
            try {
                symbolInsert();
            } catch (BadLocationException e) {
                System.out.println("ERROR Symbol list key press: " + e.getMessage());
            }
        }
        if ((evt.getKeyCode() == KeyEvent.VK_ESCAPE)) {
            symbolDialog.setVisible(false);
        }
    }//GEN-LAST:event_symbolListKeyPressed

    private void symbolListMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_symbolListMouseExited
        symbolDialog.setTitle(java.util.ResourceBundle.getBundle("JIF").getString("STR_SYMBOLS"));
    }//GEN-LAST:event_symbolListMouseExited

    private void symbolListMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_symbolListMouseEntered
        symbolDialog.setTitle(java.util.ResourceBundle.getBundle("JIF").getString("JWINDOW_TOOLTIP"));
    }//GEN-LAST:event_symbolListMouseEntered

    private void symbolListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_symbolListMouseClicked
        if (evt.getClickCount() == 2) {
            try {
                symbolInsert();
            } catch (BadLocationException e) {
                System.out.println("ERROR Symbol list mouse double click: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_symbolListMouseClicked

    private void tabSizeTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tabSizeTextFieldActionPerformed
        try {
            optionTabSize = Integer.parseInt(tabSizeTextField.getText());
        } catch (Exception e) {
            optionTabSize = 4;
        }
    }//GEN-LAST:event_tabSizeTextFieldActionPerformed

    private void defaultLightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultLightActionPerformed
        defaultLightColors();
        defaultFont();
    }//GEN-LAST:event_defaultLightActionPerformed

    private void wordColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wordColorActionPerformed
        Color temp = JColorChooser.showDialog(this,
                "Color Dialog",
                optionContext.getForeground(InformSyntax.Word));
        if (temp == null) {
            return;
        }
        optionContext.setForeground(InformSyntax.Word, temp);
        updateWordColor();
    }//GEN-LAST:event_wordColorActionPerformed

    private void numberColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numberColorActionPerformed
        Color temp = JColorChooser.showDialog(this,
                "Color Dialog",
                optionContext.getForeground(InformSyntax.Number));
        if (temp == null) {
            return;
        }
        optionContext.setForeground(InformSyntax.Number, temp);
        updateNumberColor();
    }//GEN-LAST:event_numberColorActionPerformed

    private void stringColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stringColorActionPerformed
        Color color = JColorChooser.showDialog(this,
                "Color Dialog",
                optionContext.getForeground(InformSyntax.String));
        if (color == null) {
            return;
        }
        optionContext.setForeground(InformSyntax.String, color);
        updateStringColor();
    }//GEN-LAST:event_stringColorActionPerformed

    private void projectSwitchesCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectSwitchesCloseActionPerformed
        projectSwitchesDialog.setVisible(false);
    }//GEN-LAST:event_projectSwitchesCloseActionPerformed

    private void projectPropertiesCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectPropertiesCloseActionPerformed
        projectPropertiesDialog.setVisible(false);
    }//GEN-LAST:event_projectPropertiesCloseActionPerformed

    private void projectPropertiesSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectPropertiesSaveActionPerformed
        projectPropertiesSave();
    }//GEN-LAST:event_projectPropertiesSaveActionPerformed

    private void projectSwitchesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectSwitchesActionPerformed
        projectSwitches();
    }//GEN-LAST:event_projectSwitchesActionPerformed

    private void projectPropertiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_projectPropertiesActionPerformed
        projectProperties();
    }//GEN-LAST:event_projectPropertiesActionPerformed

    private void garbageCollectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_garbageCollectionActionPerformed
        System.gc();
    }//GEN-LAST:event_garbageCollectionActionPerformed

    private void lastProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lastProjectActionPerformed
        if (((JMenuItem) evt.getSource()).getText() == null
                || ((JMenuItem) evt.getSource()).getText().length() <= 0) {
            return;
        }
        openProject(config.getLastProject().getPath());
    }//GEN-LAST:event_lastProjectActionPerformed

    private void outputTextAreaMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_outputTextAreaMouseMoved
        try {
            // Rescues the correct line
            Element el = outputTextArea.getDocument().getDefaultRootElement();
            int ind = el.getElementIndex(outputTextArea.viewToModel(evt.getPoint()));
            el = outputTextArea.getDocument().getDefaultRootElement().getElement(ind);
            String ultima = outputTextArea.getText(el.getStartOffset(), el.getEndOffset() - el.getStartOffset());

            if (ultima.startsWith(Constants.TOKENCOMMENT) || ultima.startsWith(Constants.TOKENSEARCH)) {

                hlighterOutputErrors.removeHighlights(outputTextArea);
                hlighterOutputWarnings.removeHighlights(outputTextArea);

                if (Utils.IgnoreCaseIndexOf(ultima, "error") != -1) {
                    // In case of errors
                    hlighterOutputErrors.highlightFromTo(outputTextArea, el.getStartOffset(), el.getEndOffset());
                } else {
                    // In case of warnings
                    hlighterOutputWarnings.highlightFromTo(outputTextArea, el.getStartOffset(), el.getEndOffset());
                }
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_outputTextAreaMouseMoved

    private void treeTreeTreeExpanded(javax.swing.event.TreeExpansionEvent evt) {//GEN-FIRST:event_treeTreeTreeExpanded
        CharBuffer cb = getCurrentJifTextPane().getCharBuffer();
        if (evt.getPath().equals(new TreePath(treeModel.getPathToRoot(globalTree)))) {
            refreshGlobals(cb);
        } else if (evt.getPath().equals(new TreePath(treeModel.getPathToRoot(constantTree)))) {
            refreshConstants(cb);
        } else if (evt.getPath().equals(new TreePath(treeModel.getPathToRoot(objectTree)))) {
            refreshObjects(cb);
        } else if (evt.getPath().equals(new TreePath(treeModel.getPathToRoot(functionTree)))) {
            refreshFunctions(cb);
        } else if (evt.getPath().equals(new TreePath(treeModel.getPathToRoot(classTree)))) {
// DEBUG
            System.out.println("Tree expanded");
            refreshClasses(cb);
        }
        evt = null;
        return;
    }//GEN-LAST:event_treeTreeTreeExpanded

    private void outputCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputCheckBoxActionPerformed
        if (outputCheckBoxMenuItem.getState()) {
            mainSplitPane.setBottomComponent(outputTabbedPane);
            outputTabbedPane.setVisible(true);
        } else {
            outputTabbedPane.setVisible(false);
        }
    }//GEN-LAST:event_outputCheckBoxActionPerformed

    private void translateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_translateActionPerformed
        getCurrentJifTextPane().insertTranslate(new File(getCurrentFilename() + "_translate.txt"),
                new File(getCurrentFilename() + "_translated.inf"));
    }//GEN-LAST:event_translateActionPerformed

    private void extractStringsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extractStringsActionPerformed
        getCurrentJifTextPane().extractTranslate(new File(getCurrentFilename() + "_translate.txt"));
    }//GEN-LAST:event_extractStringsActionPerformed
    
    private void findActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findActionPerformed
        getCurrentJifTextPane().findString(this);
    }//GEN-LAST:event_findActionPerformed

    private void searchProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchProjectActionPerformed
        String target = searchProjectTextField.getText();
        if (target == null || target.trim().equals("")) {
            return;
        }
        // if output window is hide, I'll show it
        if (!outputCheckBoxMenuItem.getState()) {
            mainSplitPane.setBottomComponent(outputTabbedPane);
            outputTabbedPane.setVisible(true);
        }
        searchAllFiles(target);
    }//GEN-LAST:event_searchProjectActionPerformed

    private void definitionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_definitionActionPerformed
        String target = definitionTextField.getText();
        if (target == null || target.trim().equals("")) {
            return;
        }
        checkTree(target);
    }//GEN-LAST:event_definitionActionPerformed

    private void replaceCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceCloseActionPerformed
        replaceDialog.setVisible(false);
    }//GEN-LAST:event_replaceCloseActionPerformed

    private void replaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceActionPerformed
        if (getCurrentJifTextPane().getSelectedText() != null) {
            replaceFindTextField.setText(getCurrentJifTextPane().getSelectedText());
        }
        getCurrentJifTextPane().requestFocus();
        getCurrentJifTextPane().setCaretPosition(0);
        replaceDialog.pack();
        replaceDialog.setLocationRelativeTo(this);
        replaceDialog.setVisible(true);
    }//GEN-LAST:event_replaceActionPerformed

    private void searchAllctionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchAllctionPerformed
        // se e' presente una stringa uso quella altrimenti la prendo da quella selezionata
        // No: vince quella selezionata
        String target = null;
        if (searchProjectTextField.getText() != null
                && getCurrentJifTextPane().getSelectedText() != null) {
            target = getCurrentJifTextPane().getSelectedText();
        }

        if (target == null) {
            target = searchProjectTextField.getText();
        }

        if (target == null || target.trim().equals("")) {
            target = getCurrentJifTextPane().getSelectedText();
        }

        if (target != null && !target.trim().equals("")) {
            // if output window is hide, I'll show it
            if (!outputCheckBoxMenuItem.getState()) {
                mainSplitPane.setBottomComponent(outputTabbedPane);
                outputTabbedPane.setVisible(true);
            }
            searchAllFiles(target);
        }
    }//GEN-LAST:event_searchAllctionPerformed

    private void searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchActionPerformed
        String selezione = getCurrentJifTextPane().getSelectedText();
        String box = findTextField.getText();
        String target = null;
        // vince la box
        if (box != null && !box.equals("")) {
            target = box;
        } else {
            target = selezione;
        }

        if (getCurrentJifTextPane().isFocusOwner() && selezione != null) {
            target = selezione;
        }

        findTextField.setText(target);
        getCurrentJifTextPane().findString(this);
    }//GEN-LAST:event_searchActionPerformed

    private void numberLinesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numberLinesCheckBoxActionPerformed
        if (numberLinesCheckBox.isSelected()) {
            wrapLinesCheckBox.setSelected(false);
        }
    }//GEN-LAST:event_numberLinesCheckBoxActionPerformed

    private void cutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cutActionPerformed
        getCurrentJifTextPane().cut();
    }//GEN-LAST:event_cutActionPerformed

    private void wrapLinesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wrapLinesCheckBoxActionPerformed
        if (wrapLinesCheckBox.isSelected()) {
            numberLinesCheckBox.setSelected(false);
        }
    }//GEN-LAST:event_wrapLinesCheckBoxActionPerformed
    
    private void toggleFullscreenCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleFullscreenCheckBoxActionPerformed
        if (toggleFullscreenCheckBoxMenuItem.getState()) {
            toolbarCheckBoxMenuItem.setSelected(false);
            treeCheckBoxMenuItem.setSelected(false);
            outputCheckBoxMenuItem.setSelected(false);
        } else {
            toolbarCheckBoxMenuItem.setSelected(true);
            treeCheckBoxMenuItem.setSelected(true);
            outputCheckBoxMenuItem.setSelected(true);
        }
        outputCheckBoxActionPerformed(evt);
        toolbarCheckBoxActionPerformed(evt);
        treeCheckBoxActionPerformed(evt);
    }//GEN-LAST:event_toggleFullscreenCheckBoxActionPerformed

    private void nextBookmarkMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextBookmarkMenuItemActionPerformed
        if (getCurrentJifTextPane() == null) {
            return;
        }
        getCurrentJifTextPane().nextBookmark();
    }//GEN-LAST:event_nextBookmarkMenuItemActionPerformed

    private void setBookmarkMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setBookmarkMenuItemActionPerformed
        if (getCurrentJifTextPane() == null) {
            return;
        }
        getCurrentJifTextPane().setBookmark();
    }//GEN-LAST:event_setBookmarkMenuItemActionPerformed

    private void libraryPath3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_libraryPath3ActionPerformed
        JFileChooser chooser = new JFileChooser(config.getWorkingDirectory());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.CANCEL_OPTION) {
            return;
        }
        libraryPath3TextField.setText(chooser.getSelectedFile().getAbsolutePath());
        config.setLibraryPath3(chooser.getSelectedFile().getAbsolutePath());
    }//GEN-LAST:event_libraryPath3ActionPerformed

    private void libraryPath2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_libraryPath2ActionPerformed
        JFileChooser chooser = new JFileChooser(config.getWorkingDirectory());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.CANCEL_OPTION) {
            return;
        }
        libraryPath2TextField.setText(chooser.getSelectedFile().getAbsolutePath());
        config.setLibraryPath2(chooser.getSelectedFile().getAbsolutePath());
    }//GEN-LAST:event_libraryPath2ActionPerformed

    private void jumpToSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jumpToSourceActionPerformed
        try {
            String target = getCurrentJifTextPane().getCurrentWord();
            if (target == null || target.trim().equals("")) {
                return;
            }
            checkTree(target);
        } catch (BadLocationException ble) {
            System.out.println("ERROR Jump to source: " + ble.getMessage());
        }
    }//GEN-LAST:event_jumpToSourceActionPerformed

    private void treeTreeMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeTreeMouseEntered
//        refreshTreeIncremental();
    }//GEN-LAST:event_treeTreeMouseEntered

    private void defaultDarkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultDarkActionPerformed
        defaultDarkColors();
        defaultFont();
    }//GEN-LAST:event_defaultDarkActionPerformed

    private void jButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonMouseExited
        ((JButton) evt.getSource()).setBorderPainted(false);
    }//GEN-LAST:event_jButtonMouseExited

    private void jButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonMouseEntered
        ((JButton) evt.getSource()).setBorderPainted(true);
    }//GEN-LAST:event_jButtonMouseEntered

    private void saveAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAllActionPerformed
        clearOutput();
        saveAll();
        saveProject();
    }//GEN-LAST:event_saveAllActionPerformed

    private void removeMainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeMainActionPerformed
        clearMainForProject();
    }//GEN-LAST:event_removeMainActionPerformed

    private void setMainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setMainActionPerformed
        setMainForProject();
    }//GEN-LAST:event_setMainActionPerformed

    private void buildAllGlulxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buildAllGlulxActionPerformed
        clearOutput();
        saveAll();
        if (makeResourceCheckBox.isSelected()) {
            makeResources();    // make resources
        }
        rebuildAll();           // Compile ULX file
        makeBlb();              // Make BLB file
    }//GEN-LAST:event_buildAllGlulxActionPerformed

    private void runBlbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runBlbActionPerformed
        clearOutput();
        saveFile();
        runBlb();
    }//GEN-LAST:event_runBlbActionPerformed

    private void runUlxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runUlxActionPerformed
        clearOutput();
        saveFile();
        runAdventure();
    }//GEN-LAST:event_runUlxActionPerformed

    private void compileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compileActionPerformed
        clearOutput();
        saveAll();
        rebuildAll();
    }//GEN-LAST:event_compileActionPerformed

    private void makeBlbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makeBlbActionPerformed
        makeBlb();
    }//GEN-LAST:event_makeBlbActionPerformed

    private void makeResourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makeResourceActionPerformed
        // Make the resource file and visualize the log in the output window
        makeResources();
    }//GEN-LAST:event_makeResourceActionPerformed

    private void blcPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_blcPathActionPerformed
        JFileChooser chooser = new JFileChooser(config.getWorkingDirectory());
        if (chooser.showOpenDialog(this) == JFileChooser.CANCEL_OPTION) {
            return;
        }
        blcPathTextField.setText(chooser.getSelectedFile().getAbsolutePath());
    }//GEN-LAST:event_blcPathActionPerformed

    private void bresPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bresPathActionPerformed
        JFileChooser chooser = new JFileChooser(config.getWorkingDirectory());
        if (chooser.showOpenDialog(this) == JFileChooser.CANCEL_OPTION) {
            return;
        }
        bresPathTextField.setText(chooser.getSelectedFile().getAbsolutePath());
    }//GEN-LAST:event_bresPathActionPerformed

    private void glulxPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_glulxPathActionPerformed
        JFileChooser chooser = new JFileChooser(config.getWorkingDirectory());
        if (chooser.showOpenDialog(this) == JFileChooser.CANCEL_OPTION) {
            return;
        }
        glulxPathTextField.setText(chooser.getSelectedFile().getAbsolutePath());
    }//GEN-LAST:event_glulxPathActionPerformed

    private void informModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_informModeActionPerformed
        if (informModeCheckBoxMenuItem.getState()) {
            setInformMode();
        } else {
            setGlulxMode();
        }
        if (project.getFile() == null) {
            config.setInformMode(informModeCheckBoxMenuItem.getState());
        } else {
            project.setInformMode(informModeCheckBoxMenuItem.getState());
        }
        refreshTitle();
    }//GEN-LAST:event_informModeActionPerformed

    private void glulxModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_glulxModeActionPerformed
        if (glulxModeCheckBoxMenuItem.getState()) {
            setGlulxMode();
        } else {
            setInformMode();
        }
        if (project.getFile() == null) {
            config.setInformMode(informModeCheckBoxMenuItem.getState());
        } else {
            project.setInformMode(informModeCheckBoxMenuItem.getState());
        }
        refreshTitle();
    }//GEN-LAST:event_glulxModeActionPerformed

    private void libraryPath1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_libraryPath1ActionPerformed
        JFileChooser chooser = new JFileChooser(config.getWorkingDirectory());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.CANCEL_OPTION) {
            return;
        }
        libraryPath1TextField.setText(chooser.getSelectedFile().getAbsolutePath());
    }//GEN-LAST:event_libraryPath1ActionPerformed

    private void bracketCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bracketCheckActionPerformed
        if (getCurrentJifTextPane() == null) {
            return;
        }
        getCurrentJifTextPane().checkBrackets(this);
    }//GEN-LAST:event_bracketCheckActionPerformed

    private void treeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_treeCheckBoxActionPerformed
        if (treeCheckBoxMenuItem.getState()) {
            upperSplitPane.setDividerLocation(180);
        } else {
            upperSplitPane.setDividerLocation(0);
        }
    }//GEN-LAST:event_treeCheckBoxActionPerformed

    private void backgroundColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backgroundColorActionPerformed
        Color temp = JColorChooser.showDialog(this,
                "Color Dialog",
                optionContext.getBackground());
        if (temp == null) {
            return;
        }
        optionContext.setBackground(temp);
        updateBackgroundColor();

    }//GEN-LAST:event_backgroundColorActionPerformed

    private void addNewToProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewToProjectActionPerformed
        if (project.getFile() == null) {
            return;
        }
        // Creates a new file and append this to the project
        newAdventure();
        String dir = project.getFile().getDirectory();
        saveAs(dir);
        project.addFile(getCurrentFilename());
        saveProject();
    }//GEN-LAST:event_addNewToProjectActionPerformed

    private void tabLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tabLeftActionPerformed
        if (getCurrentJifTextPane() == null) {
            return;
        }
        ((Action) (getCurrentJifTextPane().getActionMap().get(JifEditorKit.tabLeftAction))).actionPerformed(evt);
    }//GEN-LAST:event_tabLeftActionPerformed

    private void tabRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tabRightActionPerformed
        if (getCurrentJifTextPane() == null) {
            return;
        }
        ((Action) (getCurrentJifTextPane().getActionMap().get(JifEditorKit.tabRightAction))).actionPerformed(evt);
    }//GEN-LAST:event_tabRightActionPerformed
    
    private void tutorialPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tutorialPrintActionPerformed
        new Utils().printInform(this,
                "Jif - " + tutorialDialog.getTitle(),
                tutorialEditorPane);
    }//GEN-LAST:event_tutorialPrintActionPerformed

    private void redoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redoActionPerformed
        if (getCurrentJifTextPane() == null) {
            return;
        }
        ((Action) (getCurrentJifTextPane().getActionMap().get("Redo"))).actionPerformed(evt);
    }//GEN-LAST:event_redoActionPerformed

    private void undoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoActionPerformed
        if (getCurrentJifTextPane() == null) {
            return;
        }
        ((Action) (getCurrentJifTextPane().getActionMap().get("Undo"))).actionPerformed(evt);
    }//GEN-LAST:event_undoActionPerformed

    private void settingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsActionPerformed
        options();
    }//GEN-LAST:event_settingsActionPerformed

    private void tutorialOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tutorialOKActionPerformed
        tutorialDialog.setVisible(false);
    }//GEN-LAST:event_tutorialOKActionPerformed

    private void fontSizeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontSizeComboBoxActionPerformed
        optionContext.setFontSize(Integer.parseInt((String) fontSizeComboBox.getSelectedItem()));
    }//GEN-LAST:event_fontSizeComboBoxActionPerformed

    private void fontNameComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontNameComboBoxActionPerformed
        optionContext.setFontName((String) fontNameComboBox.getSelectedItem());
    }//GEN-LAST:event_fontNameComboBoxActionPerformed

    private void optionDefaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionDefaultActionPerformed
        defaultOptions();
        defaultLightColors();
        defaultFont();
        defaultLightHighlights();
        updateOptionPaths();
    }//GEN-LAST:event_optionDefaultActionPerformed

    private void commentColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_commentColorActionPerformed
        Color color = JColorChooser.showDialog(this,
                "Color Dialog",
                optionContext.getForeground(InformSyntax.Comment));
        if (color == null) {
            return;
        }
        optionContext.setForeground(InformSyntax.Comment, color);
        updateCommentColor();
    }//GEN-LAST:event_commentColorActionPerformed

    private void normalColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_normalColorActionPerformed
        Color color = JColorChooser.showDialog(this,
                "Color Dialog",
                optionContext.getForeground(InformSyntax.Normal));
        if (color == null) {
            return;
        }
        optionContext.setForeground(InformSyntax.Normal, color);
        updateNormalColor();
    }//GEN-LAST:event_normalColorActionPerformed

    private void verbColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verbColorActionPerformed
        Color color = JColorChooser.showDialog(this,
                "Color Dialog",
                optionContext.getForeground(InformSyntax.Verb));
        if (color == null) {
            return;
        }
        optionContext.setForeground(InformSyntax.Verb, color);
        updateVerbColor();
    }//GEN-LAST:event_verbColorActionPerformed

    private void propertyColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_propertyColorActionPerformed
        Color color = JColorChooser.showDialog(this,
                "Color Dialog",
                optionContext.getForeground(InformSyntax.Property));
        if (color == null) {
            return;
        }
        optionContext.setForeground(InformSyntax.Property, color);
        updatePropertyColor();
    }//GEN-LAST:event_propertyColorActionPerformed

    private void attributeColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attributeColorActionPerformed
        Color color = JColorChooser.showDialog(this,
                "Color Dialog",
                optionContext.getForeground(InformSyntax.Attribute));
        if (color == null) {
            return;
        }
        optionContext.setForeground(InformSyntax.Attribute, color);
        updateAttributeColor();

    }//GEN-LAST:event_attributeColorActionPerformed

    private void keywordColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keywordColorActionPerformed
        Color color = JColorChooser.showDialog(this,
                "Color Dialog",
                optionContext.getForeground(InformSyntax.Keyword));
        if (color == null) {
            return;
        }
        optionContext.setForeground(InformSyntax.Keyword, color);
        updateKeywordColor();
    }//GEN-LAST:event_keywordColorActionPerformed

    private void switchManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_switchManagerActionPerformed
        // if a project exists, Jif will launch the project switches dialog
        if (project.getFile() == null) {
            configSwitches();
        } else {
            projectSwitches();
        }
    }//GEN-LAST:event_switchManagerActionPerformed

    private void openSelectedFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openSelectedFilesActionPerformed
        Object[] oggetti = projectList.getSelectedValues();
        if (oggetti.length == 0) {
            return;
        }
        for (int i = 0; i < oggetti.length; i++) {
            if (oggetti[i] != null) {
                openFile(((JifFileName) oggetti[i]).getPath());
            }
        }
    }//GEN-LAST:event_openSelectedFilesActionPerformed

    private void switchesSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_switchesSaveActionPerformed
        configSwitchesSave();
    }//GEN-LAST:event_switchesSaveActionPerformed

    private void projectListMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_projectListMouseEntered
        // Just One click: shows the tooltip
        JifFileName fp = (JifFileName) projectList.getSelectedValue();
        if (fp != null) {
            projectList.setToolTipText(fp.getPath());
        } else {
            projectList.setToolTipText(null);
        }
    }//GEN-LAST:event_projectListMouseEntered

    private void interpreterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_interpreterActionPerformed
        runInterpreter();
    }//GEN-LAST:event_interpreterActionPerformed

    private void infoCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoCloseActionPerformed
        infoDialog.setVisible(false);
    }//GEN-LAST:event_infoCloseActionPerformed

    private void fileTabbedPaneComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_fileTabbedPaneComponentShown
// DEBUG
        System.out.println("file tab shown - refresh tree");
        refreshTree();
        refreshTitle();
    }//GEN-LAST:event_fileTabbedPaneComponentShown

    private void fileTabbedPaneMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fileTabbedPaneMouseClicked
// DEBUG
        System.out.println("file tab clicked - refresh tree");
        refreshTree();
        refreshTitle();
    }//GEN-LAST:event_fileTabbedPaneMouseClicked

    private void saveProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveProjectActionPerformed
        saveProject();
        saveProjectMessage();
    }//GEN-LAST:event_saveProjectActionPerformed

    private void closeProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeProjectActionPerformed
        closeProject();
    }//GEN-LAST:event_closeProjectActionPerformed

    private void openProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openProjectActionPerformed
        openProject();
    }//GEN-LAST:event_openProjectActionPerformed

    private void newProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newProjectActionPerformed
        newProject();
    }//GEN-LAST:event_newProjectActionPerformed

    private void optionSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionSaveActionPerformed
        optionSave();
    }//GEN-LAST:event_optionSaveActionPerformed

    private void projectListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_projectListMouseClicked
        // Double clicking an entry, JIF opens the selected file
        if (evt.getClickCount() == 2) {
            // opens the file
            JifFileName fp = (JifFileName) projectList.getSelectedValue();
            if (fp != null) {
                openFile(fp.getPath());
            }
        } else {
            // Sets the tooltip in case of One click
            JifFileName fp = (JifFileName) projectList.getSelectedValue();
            if (fp != null) {
                projectList.setToolTipText(fp.getPath());
            }
        }
    }//GEN-LAST:event_projectListMouseClicked

    private void printPopupMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printPopupMenuItemActionPerformed
        new Utils().printInform(this,
                "Jif print - " + getCurrentFilename(),
                getCurrentJifTextPane());
    }//GEN-LAST:event_printPopupMenuItemActionPerformed

    private void removeFromProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeFromProjectActionPerformed
        removeFileFromProject();
    }//GEN-LAST:event_removeFromProjectActionPerformed

    private void addFileToProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFileToProjectActionPerformed
        addFilesToProject();
    }//GEN-LAST:event_addFileToProjectActionPerformed

    private void insertSymbolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertSymbolActionPerformed
        showSymbolsDialog();
    }//GEN-LAST:event_insertSymbolActionPerformed

    private void uncommentSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uncommentSelectionActionPerformed
        if (getCurrentJifTextPane() == null) {
            return;
        }
        ((Action) (getCurrentJifTextPane().getActionMap().get(JifEditorKit.uncommentAction))).actionPerformed(evt);
    }//GEN-LAST:event_uncommentSelectionActionPerformed

    private void replaceReplaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceReplaceActionPerformed
        // Replacing....only if there is a selected TEXT
        if (getCurrentJifTextPane().getSelectedText() == null) {
            return;
        }
        getCurrentJifTextPane().replaceSelection(replaceReplaceTextField.getText());
    }//GEN-LAST:event_replaceReplaceActionPerformed

    private void replaceFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceFindActionPerformed
        if (replaceFindTextField.getText().equals("")) {
            JOptionPane.showMessageDialog(this,
                    java.util.ResourceBundle.getBundle("JIF").getString("ERR_EMPTY_STRING"));
        }
        findString(replaceFindTextField.getText());
    }//GEN-LAST:event_replaceFindActionPerformed

    private void replaceAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceAllActionPerformed
        replaceAll();
// DEBUG
        System.out.println("replace all - refresh tree");
        refreshTree();
    }//GEN-LAST:event_replaceAllActionPerformed

    private void commentSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_commentSelectionActionPerformed
        if (getCurrentJifTextPane() == null) {
            return;
        }
        ((Action) (getCurrentJifTextPane().getActionMap().get(JifEditorKit.commentAction))).actionPerformed(evt);
    }//GEN-LAST:event_commentSelectionActionPerformed

    private void insertFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertFileActionPerformed
        insertFromFile();
    }//GEN-LAST:event_insertFileActionPerformed

    private void printActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printActionPerformed
        new Utils().printInform(this,
                "Jif print - " + getCurrentFilename(),
                getCurrentJifTextPane());
    }//GEN-LAST:event_printActionPerformed

    private void treeTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_treeTreeValueChanged
        if (getCurrentJifTextPane() == null) {
            clearTree("Inspect");
            return;
        }

        getCurrentJifTextPane().removeHighlighter();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeTree.getLastSelectedPathComponent();
        if (node == null || !(node.getUserObject() instanceof Inspect)) {
            return;
        }
        Object nodo = node.getUserObject();
        try {
            Inspect insp = (Inspect) nodo;
            if (insp != null && insp.Iposition != -1) {
                JifTextPane jif = getCurrentJifTextPane();
                Element el = jif.getDocument().getDefaultRootElement();
                int index = el.getElementIndex(insp.Iposition);
                el = jif.getDocument().getDefaultRootElement().getElement(index);
                jif.getHlighter().highlightFromTo(jif, el.getStartOffset(), el.getEndOffset());

                jif.scrollRectToVisible(jif.modelToView(jif.getDocument().getLength()));
                jif.scrollRectToVisible(jif.modelToView(insp.Iposition));
                jif.requestFocus();
                jif.setCaretPosition(insp.Iposition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_treeTreeValueChanged

    private void outputTextAreaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_outputTextAreaMouseClicked
        // When user clicks on errors/warnings, JIF jumps to the correct line in the source
        try {
            String nome = "";
            int riga = 0;
            boolean found = false;

            // Rescues the correct line
            Element el = outputTextArea.getDocument().getDefaultRootElement();
            int ind = el.getElementIndex(outputTextArea.viewToModel(evt.getPoint()));
            el = outputTextArea.getDocument().getDefaultRootElement().getElement(ind);
            String ultima = outputTextArea.getText(el.getStartOffset(), el.getEndOffset() - el.getStartOffset());

            // Only if the line starts with the "#" char
            if (ultima.indexOf(Constants.TOKENCOMMENT) != -1
                    && ((ultima.indexOf(".inf") != -1) || (ultima.indexOf(".h") != -1))) {

                // Errors in E1 format
                if (Utils.IgnoreCaseIndexOf(ultima, "line ") == -1) {
                    // Removing all the selected text in the output window
                    hlighterOutputErrors.removeHighlights(outputTextArea);

                    StringTokenizer stok = new StringTokenizer(ultima, "#()");
                    nome = stok.nextToken();
                    riga = Integer.parseInt(stok.nextToken());

                    // checks if the file exists
                    int selected = fileTabbedPane.getTabCount();
                    for (int i = 0; i < selected; i++) {
                        if (nome.equals(getFilenameAt(i))) {
                            found = true;
                            fileTabbedPane.setSelectedIndex(i);
                        }
                    }

                    if (!found) {
                        synchronized (this) {
                            openFile(nome);
                        }
                    }

                    JifTextPane jif = getCurrentJifTextPane();

                    // Find the line with the error
                    el = jif.getDocument().getDefaultRootElement();
                    el = el.getElement(riga - 1);
                    jif.setCaretPosition(el.getStartOffset());

                    if (Utils.IgnoreCaseIndexOf(ultima, "warning") == -1) {
                        jif.removeHighlighterErrors();
                        jif.getHlighterErrors().highlightFromTo(getCurrentJifTextPane(),
                                el.getStartOffset(),
                                el.getEndOffset());
                    } else {
                        jif.removeHighlighterWarnings();
                        jif.getHlighterWarnings().highlightFromTo(getCurrentJifTextPane(),
                                el.getStartOffset(),
                                el.getEndOffset());
                    }

                    if (jif.modelToView(jif.getDocument().getLength()) != null) {
                        jif.scrollRectToVisible(jif.modelToView(jif.getDocument().getLength()));
                        jif.scrollRectToVisible(jif.modelToView(el.getStartOffset()));
                    } else {
                        jif.setCaretPosition(el.getStartOffset());
                    }
                } // Errors in E0-E2 format
                else {
                    JOptionPane.showMessageDialog(this,
                            "Please, use the -E1 error switch.",
                            "Jump to Error",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            } else if (ultima.indexOf(Constants.TOKENCOMMENT) != -1 && Utils.IgnoreCaseIndexOf(ultima, "file") == -1) {
                JOptionPane.showMessageDialog(this,
                        "Please, use the -E1 error switch.",
                        "Jump to Error",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            } // find string in all files function
            else if (ultima.startsWith(Constants.TOKENSEARCH)) {
                // Removing all the selected text in the output window
                hlighterOutputErrors.removeHighlights(outputTextArea);

                // Highlight the correct line
                hlighterOutputErrors.highlightFromTo(outputTextArea,
                        el.getStartOffset(),
                        el.getEndOffset());

                StringTokenizer stok = new StringTokenizer(ultima, Constants.TOKENSEARCH);
                nome = stok.nextToken();
                riga = Integer.parseInt(stok.nextToken());

                // checks if the file exists
                int selected = fileTabbedPane.getTabCount();
                for (int i = 0; i < selected; i++) {
                    if (nome.equals(getFilenameAt(i))) {
                        found = true;
                        fileTabbedPane.setSelectedIndex(i);
                    }
                }

                if (!found) {
                    synchronized (this) {
                        openFile(nome);
                    }
                }

                // Find the line with the error
                JifTextPane jif = getCurrentJifTextPane();
                el = jif.getDocument().getDefaultRootElement();
                el = el.getElement(riga - 1);
                jif.setCaretPosition(el.getStartOffset());

                // Removing all the selected text
                jif.removeHighlighter();

                // Highlight the line which has product the error during compiling
                jif.getHlighter().highlightFromTo(jif, el.getStartOffset(), el.getEndOffset());
                jif.scrollRectToVisible(jif.modelToView(jif.getDocument().getLength()));
                jif.scrollRectToVisible(jif.modelToView(el.getStartOffset()));
            } else {
                return;
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_outputTextAreaMouseClicked

    private void clearAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearAllActionPerformed
        if (JOptionPane.showConfirmDialog(this,
                java.util.ResourceBundle.getBundle("JIF").getString("MSG_DELETE1"),
                java.util.ResourceBundle.getBundle("JIF").getString("MSG_DELETE2"),
                JOptionPane.OK_CANCEL_OPTION)
                == JOptionPane.CANCEL_OPTION) {
            return;
        }
        getCurrentJifTextPane().setText("");
    }//GEN-LAST:event_clearAllActionPerformed

    private void selectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllActionPerformed
        getCurrentJifTextPane().selectAll();
    }//GEN-LAST:event_selectAllActionPerformed

    private void clearRecentFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearRecentFilesActionPerformed
        config.clearRecentFiles();
        try {
            JifConfigurationDAO.store(config);
        } catch (JifConfigurationException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_clearRecentFilesActionPerformed
 
    private void closeAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeAllActionPerformed
        closeAllFiles();
    }//GEN-LAST:event_closeAllActionPerformed

    private void interpreterPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_interpreterPathActionPerformed
        JFileChooser chooser = new JFileChooser(config.getWorkingDirectory());
        if (chooser.showOpenDialog(this) == JFileChooser.CANCEL_OPTION) {
            return;
        }
        interpreterPathTextField.setText(chooser.getSelectedFile().getAbsolutePath());
    }//GEN-LAST:event_interpreterPathActionPerformed

    private void compilerPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compilerPathActionPerformed
        JFileChooser chooser = new JFileChooser(config.getWorkingDirectory());
        if (chooser.showOpenDialog(this) == JFileChooser.CANCEL_OPTION) {
            return;
        }
        compilerPathTextField.setText(chooser.getSelectedFile().getAbsolutePath());
    }//GEN-LAST:event_compilerPathActionPerformed

    private void gamePathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gamePathActionPerformed
        JFileChooser chooser = new JFileChooser(config.getWorkingDirectory());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.CANCEL_OPTION) {
            return;
        }
        gamePathTextField.setText(chooser.getSelectedFile().getAbsolutePath());
    }//GEN-LAST:event_gamePathActionPerformed

    private void libraryPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_libraryPathActionPerformed
        JFileChooser chooser = new JFileChooser(config.getWorkingDirectory());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(this) == JFileChooser.CANCEL_OPTION) {
            return;
        }
        libraryPathTextField.setText(chooser.getSelectedFile().getAbsolutePath());
    }//GEN-LAST:event_libraryPathActionPerformed

    private void closeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeActionPerformed
        closeFile();
        if (fileTabbedPane.getTabCount() == 0) {
            disableFileComponents();
        }
// DEBUG
        System.out.println("close - refresh tree");
        refreshTree();
        refreshTitle();
        System.gc();
    }//GEN-LAST:event_closeActionPerformed

    private void copyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyActionPerformed
        copyToClipBoard();
    }//GEN-LAST:event_copyActionPerformed

    private void readMeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readMeActionPerformed
        showReadMe();
    }//GEN-LAST:event_readMeActionPerformed

    private void textCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textCloseActionPerformed
        textDialog.setVisible(false);
        textTextArea.setText("");
        textLabel.setText("");
    }//GEN-LAST:event_textCloseActionPerformed

    private void saveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsActionPerformed
        saveAs(null);
    }//GEN-LAST:event_saveAsActionPerformed
    private void configFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configFileActionPerformed
        configProperties();
    }//GEN-LAST:event_configFileActionPerformed

    private void switchesCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_switchesCloseActionPerformed
        switchesDialog.setVisible(false);
    }//GEN-LAST:event_switchesCloseActionPerformed

    private void switchesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_switchesActionPerformed
        configSwitches();
    }//GEN-LAST:event_switchesActionPerformed

    private void configSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configSaveActionPerformed
        configPropertiesSave();
    }//GEN-LAST:event_configSaveActionPerformed
    private void configCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configCloseActionPerformed
        configDialog.setVisible(false);
    }//GEN-LAST:event_configCloseActionPerformed

    private void newActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newActionPerformed
        newAdventure();
    }//GEN-LAST:event_newActionPerformed

    private void aboutOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutOKActionPerformed
        aboutDialog.pack();
        aboutDialog.setLocationRelativeTo(this);
        aboutDialog.setVisible(false);
    }//GEN-LAST:event_aboutOKActionPerformed

    private void clearPopupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearPopupActionPerformed
        pastePopupMenu.removeAll();
    }//GEN-LAST:event_clearPopupActionPerformed

    private void toolbarCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolbarCheckBoxActionPerformed
        jToolBarCommon.setVisible(toolbarCheckBoxMenuItem.getState());
    }//GEN-LAST:event_toolbarCheckBoxActionPerformed

    private void aboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutActionPerformed
        aboutDialog.pack();
        aboutDialog.setLocationRelativeTo(this);
        aboutDialog.setVisible(true);
    }//GEN-LAST:event_aboutActionPerformed

    private void runActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runActionPerformed
        clearOutput();
        saveAll();
        rebuildAll();
        runAdventure();
    }//GEN-LAST:event_runActionPerformed

    private void exitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitActionPerformed
        exitJif();
    }//GEN-LAST:event_exitActionPerformed

    private void saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveActionPerformed
        clearOutput();
        saveFile();
    }//GEN-LAST:event_saveActionPerformed

    private void buildAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buildAllActionPerformed
        clearOutput();
        saveAll();
        rebuildAll();
    }//GEN-LAST:event_buildAllActionPerformed

    private void openActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openActionPerformed
        openFile();
    }//GEN-LAST:event_openActionPerformed

    private void changelogMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changelogMenuItemActionPerformed
        showChangeLog();
    }//GEN-LAST:event_changelogMenuItemActionPerformed

    /**
     * Jif editor main
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI();
            }
        });
    }

    // This ensures that the GUI won't have a thread-safety problem that could
    // break the UI before it even appears onscreen. (See Swing tutorial).
    private static void createAndShowGUI() {
        try {
            javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            //javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            //javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");

        } catch (Exception e) {
            System.out.println("ERROR Can't set look & feel: " + e.getMessage());
        }
        new jFrame().setVisible(true);
    }

    /**
     * Search for a string in all the files of a project
     *
     * @param target
     *              Search string to find in project files 
     */
    private void searchAllFiles(String target) {
        if (project.getFile() == null) {
            return;
        }

        StringBuffer output = new StringBuffer();
        String result;
        for (Iterator i = project.getFiles().iterator(); i.hasNext();) {
            JifFileName file = (JifFileName) i.next();
            result = Utils.searchString(target, new File(file.getPath()));
            if (result != null) {
                output.append(result + "\n");
            }
        }
        outputTextArea.setText(output.toString());
        outputTextArea.setCaretPosition(0);
    }

    private void saveFile() {
        if (fileTabbedPane.getTitleAt(fileTabbedPane.getSelectedIndex()).endsWith("*")) {
            String newtitle = fileTabbedPane.getTitleAt(fileTabbedPane.getSelectedIndex()).substring(0, fileTabbedPane.getTitleAt(fileTabbedPane.getSelectedIndex()).length() - 1);
            fileTabbedPane.setTitleAt(fileTabbedPane.getSelectedIndex(), newtitle);
        }

        File file = new File(getCurrentFilename());
        try {
            // Replaces the "\n" chars with System.getProperty("line.separator")
            String tmp = Utils.replace(getCurrentJifTextPane().getText(), "\n", System.getProperty("line.separator"));

            JifDAO.save(file, tmp);

            StringBuffer strb = new StringBuffer(java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE3"));
            strb.append(getCurrentFilename());
            strb.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE4"));
            outputTextArea.append(strb.toString());

            // rendo visibile la finestra di output
            outputTabbedPane.setSelectedComponent(outputScrollPane);

            setTitle(getJifVersion() + " - " + getCurrentFilename());

        } catch (IOException e) {
            System.out.println("ERROR Save file: " + e.getMessage());
        }

    }

    private void saveAll() {
        // Remember the file selected
        Component comp = fileTabbedPane.getSelectedComponent();

        int componenti = fileTabbedPane.getTabCount();
        for (int i = 0; i < componenti; i++) {
            fileTabbedPane.setSelectedIndex(i);
            if (getCurrentTitle().indexOf("*") != -1) {
                saveFile(); //Only save modified files
            }
        }

        // reassign the selected component
        fileTabbedPane.setSelectedComponent(comp);
    }

    private void rebuildAll() {
        // Clear OutputTextArea
        outputTextArea.setText("");
        System.gc();
        String process_string[];
        Vector<String> auxV = new Vector<String>(6);
        String switchString[];
        String fileInf;
        // Check out if the compiler exists
        File test = new File(resolveAbsolutePath(config.getWorkingDirectory(), config.getCompilerPath()));
        if (!test.exists()) {
            JOptionPane.showMessageDialog(this,
                    java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER1") + " " + resolveAbsolutePath(config.getWorkingDirectory(), config.getCompilerPath()) + java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER2"),
                    java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER3"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (resolveAbsolutePath(config.getWorkingDirectory(), config.getGamePath()) == null || resolveAbsolutePath(config.getWorkingDirectory(), config.getGamePath()).equals("")) {
            JOptionPane.showMessageDialog(this,
                    java.util.ResourceBundle.getBundle("JIF").getString("ERR_GAMESPATH"),
                    java.util.ResourceBundle.getBundle("JIF").getString("ERR_GENERIC"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Show OutputTextArea
        if (!outputCheckBoxMenuItem.getState()) {
            mainSplitPane.setBottomComponent(outputTabbedPane);
            outputTabbedPane.setVisible(true);
        }
        outputTabbedPane.setSelectedComponent(outputScrollPane);

        fileInf = getCurrentFilename();

        switchString = makeCompilerSwitches().split(" ");

        String estensione = "";
        if (isInformMode()) {
            // Inform mode
            if (tipoz.equals("-v3")) {
                estensione = ".z3";
            }
            if (tipoz.equals("-v4")) {
                estensione = ".z4";
            }
            if (tipoz.equals("-v5")) {
                estensione = ".z5";
            }
            if (tipoz.equals("-v6")) {
                estensione = ".z6";
            }
            if (tipoz.equals("-v8")) {
                estensione = ".z8";
            }
        } else {
            // Glulx mode
            estensione = ".ulx";
        }

        // If compiling a project but there isn't a main file, issue warning
        if (project.getFile() != null && project.getMain() == null) {
            JOptionPane.showMessageDialog(this,
                    "Set a Main file first.",
                    "Warning",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (project.getFile() != null && project.getMain() != null) {
            fileInf = project.getMain().getPath();
            outputTextArea.append("Using main file " + fileInf + " to compile... ");
        }

        String fileOut = fileInf.substring(0, fileInf.lastIndexOf(".")) + estensione;
        outputTextArea.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER1"));

        String lib;
        String dir = "";
        if (adventInLibCheckBox.isSelected()) {
            dir = fileInf.substring(0, fileInf.lastIndexOf(Constants.SEP)) + ",";
        }

        lib = dir + resolveAbsolutePath(config.getWorkingDirectory(), config.getLibraryPath());

        // Secondary 1-2-3 Library Path
        if (!config.getLibraryPath1().trim().equals("")) {
            lib = lib + "," + resolveAbsolutePath(config.getWorkingDirectory(), config.getLibraryPath1());
        }
        if (!config.getLibraryPath2().trim().equals("")) {
            lib = lib + "," + resolveAbsolutePath(config.getWorkingDirectory(), config.getLibraryPath2());
        }
        if (!config.getLibraryPath3().trim().equals("")) {
            lib = lib + "," + resolveAbsolutePath(config.getWorkingDirectory(), config.getLibraryPath3());
        }

        auxV.add(resolveAbsolutePath(config.getWorkingDirectory(), config.getCompilerPath()));
        // i=1 to avoid the first " "
        for (int i = 1; i < switchString.length; i++) {
            auxV.add(switchString[i]);
        }

        auxV.add("+include_path=" + lib);
        auxV.add(fileInf);
        auxV.add(fileOut);

        process_string = new String[auxV.size()];
        for (int i = 0; i < auxV.size(); i++) {
            process_string[i] = new String((String) auxV.get(i));
            outputTextArea.append(process_string[i] + " ");
        }
        outputTextArea.append("\n");

        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(process_string, null, new File(resolveAbsolutePath(config.getWorkingDirectory(), config.getGamePath())));
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream(), Constants.fileFormat));
            String line = "";

            while ((line = br.readLine()) != null) {
                // in caso di errore o warning metto il cancelletto #
                if ((line.indexOf("Error") != -1) || (line.indexOf("error") != -1)) {
                    outputTextArea.append(Constants.TOKENCOMMENT + line + "\n");
                } else if ((line.indexOf("Warning") != -1) || (line.indexOf("warning") != -1)) {
                    outputTextArea.append(Constants.TOKENCOMMENT + line + "\n");
                } else {
                    outputTextArea.append(line + "\n");
                }
            }

            outputTextArea.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER2"));
            outputTextArea.append("\n");
            setTitle(getJifVersion() + " - " + getCurrentFilename());

        } catch (IOException e) {
            System.out.println("ERROR Rebuild all: " + e.getMessage());
        }
    }

    // Agggiunto il controllo sul MODE (Inform/Glulx)
    private void runAdventure() {

        String fileInf = "";

        // controllo che esista l'interprete con il path inserito nella Jif.cfg
        // se non esiste visualizzo un messaggio di warning
        updateInformGlulxMode();
        String inter = config.getInterpreterPath();

        File test = new File(inter);

        if (!test.exists()) {
            JOptionPane.showMessageDialog(this,
                    java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER1") + " " + inter + java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER2"),
                    java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER3"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // recupero l'attuale file name
        if (project.getMain() == null) {
            fileInf = getCurrentFilename();
        } else {
            fileInf = project.getMain().getPath();
            outputTextArea.append("Using main file " + fileInf + " to run... ");
        }

        clearOutput();
        outputTextArea.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_RUN1"));

        // in base al tipo di file di uscita, scelgo l'estensione del file da passare all'interprete
        String estensione = "";
        if (isInformMode()) {
            // Inform mode
            if (tipoz.equals("-v3")) {
                estensione = ".z3";
            }
            if (tipoz.equals("-v4")) {
                estensione = ".z4";
            }
            if (tipoz.equals("-v5")) {
                estensione = ".z5";
            }
            if (tipoz.equals("-v6")) {
                estensione = ".z6";
            }
            if (tipoz.equals("-v8")) {
                estensione = ".z8";
            }
        } else {
            // Glulx mode
            estensione = ".ulx";
        }

        String command[] = new String[2];
        command[0] = new String(inter);
        command[1] = new String(fileInf.substring(0, fileInf.indexOf(".inf")) + estensione);
        outputTextArea.append(command[0] + " " + command[1] + "\n");

        try {

            Runtime rt = Runtime.getRuntime();
            rt.exec(command);

            outputTextArea.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER2"));

        } catch (IOException e) {
            System.out.println("ERROR Run adventure: " + e.getMessage());
        }
    }

    /**
     * Shows the changelog
     * 
     */
    private void showChangeLog() {
        InputStream is = null;
        // Load the readme.txt file from the jar file
        try {
            is = ClassLoader.getSystemClassLoader().getResource("changelog.txt").openStream();
        } catch (IOException e) {
            System.out.println("ERROR Show changelog: " + e.getMessage());
        }
        showFile(is);
    }

    /**
     * 
     */
    private void showReadMe() {
        InputStream is = null;
        // Load the readme.txt file from the jar file
        try {
            is = ClassLoader.getSystemClassLoader().getResource("readme.txt").openStream();
        } catch (IOException e) {
            System.out.println("ERROR Show readme: " + e.getMessage());
        }
        showFile(is);
    }

    class PopupListenerProject extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                projectPopupMenu.show(e.getComponent(),
                        e.getX(), e.getY());
            }
        }
    }

    class MenuListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            MutableAttributeSet attr = new SimpleAttributeSet();
            String id = ((javax.swing.JMenuItem) e.getSource()).getText();
            try {
                //se non trovo nessun carattere "§" non vado a capo
                if (((String) getOperations().get((String) id)).indexOf("§") == -1) {
                    // inserisco la stringa senza andare a capo
                    getCurrentDoc().insertString(getCurrentJifTextPane().getCaretPosition(), (String) getOperations().get((String) id), attr);
                } else {
                    StringTokenizer st = new StringTokenizer((String) getOperations().get((String) id), "§");
                    while (st.hasMoreTokens()) {
                        getCurrentDoc().insertString(getCurrentJifTextPane().getCaretPosition(), st.nextToken() + "\n", attr);
                    }
                }
            } catch (Exception ex) {
                System.out.println("ERROR Menu mouse press: " + ex.getMessage());
            }
        }
    }

    // funzione per l'apertura di file
    private void openFile() {

        // File chooser dialog to obtain file names
        JFileChooser chooser;
        if (config.getLastFile() != null) {
            chooser = new JFileChooser(config.getLastFile().getDirectory());
        } else {
            chooser = new JFileChooser(resolveAbsolutePath(config.getWorkingDirectory(), config.getGamePath()));
        }

        JifFileFilter infFilter = new JifFileFilter("inf", java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF7"));
        infFilter.addExtension("h");
        infFilter.addExtension("res");
        infFilter.addExtension("txt");
        chooser.setFileFilter(infFilter);
        chooser.setMultiSelectionEnabled(true);

        if (chooser.showOpenDialog(this) == JFileChooser.CANCEL_OPTION) {
            return;
        }

        File[] files = chooser.getSelectedFiles();

        File file;
        for (int i = 0; i < files.length; i++) {
            file = files[i];

            // Check whether the file is already being edited
            if (!checkOpenFile(file.getAbsoluteFile().toString())) {

                JifFileName fileName = new JifFileName(file.getAbsolutePath());
                editFile(fileName, file);

                config.setLastFile(file.getAbsolutePath());
                config.addRecentFile(file.getAbsolutePath());
            }

        } // end for

        enableFileComponents();
// DEBUG
        System.out.println("open file()");
        refreshTree();
        refreshTitle();
    }

    private void openFile(String nomefile) {

        // Check file exists
        File file = new File(nomefile);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this,
                    java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE2") + " " + nomefile + java.util.ResourceBundle.getBundle("JIF").getString("ERR_NAMEFILE3"),
                    java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE1"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Check whether the file is already being edited
        if (checkOpenFile(file.getAbsoluteFile().toString())) {
            return;
        }

        editFile(new JifFileName(file.getAbsolutePath()), file);

        config.setLastFile(file.getAbsolutePath());
        config.addRecentFile(file.getAbsolutePath());

        enableFileComponents();
// DEBUG
        System.out.println(
                "open file("
                + file.getAbsolutePath()
                + ")");
        refreshTree();
        refreshTitle();
    }

    private void newAdventure() {
        // Generate file name
        JifFileName fileName = new JifFileName(
                resolveAbsolutePath(config.getWorkingDirectory(), config.getGamePath())
                + Constants.SEP
                + java.util.ResourceBundle.getBundle("JIF").getString("MSG_NEWFILE3")
                + (countNewFile++)
                + ".inf");

        editFile(fileName, null);

        enableFileComponents();
// DEBUG
        System.out.println("new file - refresh tree");
        refreshTree();
        refreshTitle();
    }

    private void editFile(JifFileName fileName, File file) {

        InformContext context = config.getContext();
        JifTextPane jtp;

        if (config.getWrapLines()) {
            jtp = new JifTextPane(this, fileName, file, context);
        } else {
            jtp = new JifNoWrapTextPane(this, fileName, file, context);
        }

        JifScrollPane scroll = new JifScrollPane(jtp, fileName.getPath());
        scroll.setViewportView(jtp);

        // aggiungo la textarea per rowheader solo se il checkbox relativo Ã¯Â¿Â½ true
        if (config.getNumberLines()) {
            LineNumber lineNumber = new LineNumber(jtp);
            scroll.setRowHeaderView(lineNumber);
        }
        fileTabbedPane.add(scroll, jtp.getSubPath());
        fileTabbedPane.setSelectedIndex(fileTabbedPane.getTabCount() - 1);

    }

    private void clearOutput() {
        outputTextArea.setText("");
    }

    public void checkTree(String key) {
        key = key.toLowerCase();
        String file;
        file = checkDefinition(key);
        if (file == null) {
            return;
        }

        JifTextPane jif;
        synchronized (this) {
            openFile(file);
            jif = getCurrentJifTextPane();
        }

        Element el;
        // cycle on the tree's nodes
        for (int c = 0; c < top.getChildCount(); c++) {
            DefaultMutableTreeNode mainnode = (DefaultMutableTreeNode) top.getChildAt(c);

            for (int k = 0; k < mainnode.getChildCount(); k++) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) mainnode.getChildAt(k);
                if (node == null) {
                    continue;
                }

                // for the classes node
                if (node.children().hasMoreElements()) {
                    for (int j = 0; j < node.getChildCount(); j++) {
                        DefaultMutableTreeNode nodeclass = (DefaultMutableTreeNode) node.getChildAt(j);
                        Inspect ins = (Inspect) nodeclass.getUserObject();
                        if (ins.Ilabel.equals(key)) {
                            try {
                                if (ins != null) {
                                    el = jif.getDocument().getDefaultRootElement();
                                    int pos = el.getElementIndex(ins.Iposition);
                                    el = jif.getDocument().getDefaultRootElement().getElement(pos);
                                    jif.getHlighter().highlightFromTo(jif, el.getStartOffset(), el.getEndOffset());

                                    jif.scrollRectToVisible(jif.modelToView(jif.getDocument().getLength()));
                                    jif.scrollRectToVisible(jif.modelToView(ins.Iposition));
                                }

                            } catch (Exception e) {
                                System.out.println("ERROR Check tree: " + e.getMessage());
                            }
                        }
                    }

                } else {
                    Inspect ins = (Inspect) node.getUserObject();
                    if (ins.Ilabel.equals(key)) {
                        try {
                            if (ins != null) {
                                el = jif.getDocument().getDefaultRootElement();
                                int pos = el.getElementIndex(ins.Iposition);
                                el = jif.getDocument().getDefaultRootElement().getElement(pos);
                                jif.getHlighter().highlightFromTo(jif,
                                        el.getStartOffset(),
                                        el.getEndOffset());

                                jif.scrollRectToVisible(jif.modelToView(jif.getDocument().getLength()));
                                jif.scrollRectToVisible(jif.modelToView(ins.Iposition));
                                jif.requestFocus();
                                jif.setCaretPosition(ins.Iposition);
                            }

                        } catch (Exception e) {
                            System.out.println("ERROR Check tree: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void refreshTreeIncremental() {
        String currentName = getCurrentFilename();
        if (fileTabbedPane.getTabCount() == 0
                || currentName.endsWith(".txt")
                || currentName.endsWith(".res")) {
            return;
        }

        DefaultTreeModel treeModel = (DefaultTreeModel) treeTree.getModel();
        TreePath treePath1 = new TreePath(treeModel.getPathToRoot(globalTree));
        TreePath treePath2 = new TreePath(treeModel.getPathToRoot(constantTree));
        TreePath treePath4 = new TreePath(treeModel.getPathToRoot(objectTree));
        TreePath treePath5 = new TreePath(treeModel.getPathToRoot(functionTree));
        TreePath treePath7 = new TreePath(treeModel.getPathToRoot(classTree));

        // Using the regexp
        CharBuffer cb = getCurrentJifTextPane().getCharBuffer();
        objTree = new Vector<Inspect>();
        // GLOBALS
        if (treeTree.isExpanded(treePath1) || globalTree.isLeaf()) {
            refreshGlobals(cb);
        }
        // CONSTANTS
        if (treeTree.isExpanded(treePath2) || constantTree.isLeaf()) {
            refreshConstants(cb);
        }
        // OBJECTS
        if (treeTree.isExpanded(treePath4) || objectTree.isLeaf()) {
            refreshObjects(cb);
        }
        // FUNCTIONS
        if (treeTree.isExpanded(treePath5) || functionTree.isLeaf()) {
            refreshFunctions(cb);
        }
        // CLASSES
        if (treeTree.isExpanded(treePath7) || classTree.isLeaf()) {
// DEBUG            
            System.out.println("Incremental");
            refreshClasses(cb);

            expandAll(treeTree);
        }
    }

    private void refreshGlobals(CharBuffer cb) {
        Matcher m = globalPattern.matcher(cb);
        objTree.clear();
        while (m.find()) {
            objTree.add(new Inspect(m.group(1).toLowerCase(), m.start(1)));
        }
        globalTree.removeAllChildren();
        sortNodes(objTree, globalTree);
        treeModel.reload(globalTree);
    }

    private void refreshConstants(CharBuffer cb) {
        Matcher m = constantPattern.matcher(cb);
        objTree.clear();
        while (m.find()) {
            objTree.add(new Inspect(m.group(1).toLowerCase(), m.start(1)));
        }
        constantTree.removeAllChildren();
        sortNodes(objTree, constantTree);
        treeModel.reload(constantTree);
    }

    private void refreshObjects(CharBuffer cb) {
        Matcher m = objectPattern.matcher(cb);
        objTree.clear();
        while (m.find()) {
            objTree.add(new Inspect(m.group(2).toLowerCase(), m.start(2)));
        }
        objectTree.removeAllChildren();
        sortNodes(objTree, objectTree);
        treeModel.reload(objectTree);
    }

    private void refreshFunctions(CharBuffer cb) {
        Matcher m = functionPattern.matcher(cb);
        objTree.clear();
        while (m.find()) {
            objTree.add(new Inspect(m.group(1).toLowerCase(), m.start(1)));
        }
        functionTree.removeAllChildren();
        sortNodes(objTree, functionTree);
        treeModel.reload(functionTree);
    }

    private void refreshClasses(CharBuffer cb) {

        Map<String, Inspect> classes = new TreeMap<String, Inspect>();
        Map<String, DefaultMutableTreeNode> nodes = new TreeMap<String, DefaultMutableTreeNode>();
        Map<String, String> rels = new TreeMap<String, String>();

        // Does nothing if project scan off as all will be empty
        classes.putAll(projectClasses);
        nodes.putAll(projectNodes);
        rels.putAll(projectRels);

        // Find file class definitions
        Matcher m = classPattern.matcher(cb);
        while (m.find()) {
            String className = m.group(2).toLowerCase();
            int classLoc = m.start(2);
// DEBUG
            System.out.println(
                    " = found: " + className
                    + " at " + classLoc);
            Inspect ref = new Inspect(className, classLoc);
            classes.put(className, ref);
            nodes.put(className, new DefaultMutableTreeNode(ref));

            // Add any new classes found to project classes if necessary
            if (config.getScanProjectFiles()) {

                Inspect nullRef = new Inspect(className, -1);

                if (!projectClasses.containsKey(className)) {
// DEBUG
                    System.out.println(
                            "   >>> adding class");
                    projectClasses.put(className, nullRef);
                }

                if (!projectNodes.containsKey(className)) {
// DEBUG
                    System.out.println(
                            "   >>> adding node");
                    projectNodes.put(className, new DefaultMutableTreeNode(nullRef));
                }
            }
        }

        // Find file class to class relationships
        m = classToClassPattern.matcher(cb);
        while (m.find()) {
            String child = m.group(1).toLowerCase();
            String parent = m.group(2).toLowerCase();
// DEBUG
            System.out.println(
                    " * Class: " + child
                    + " of " + parent);
            rels.put(child, parent);

            // Add any new relationships found to project relationships if necessary
            if (config.getScanProjectFiles() && !projectRels.containsKey(child)) {
// DEBUG                
                System.out.println(
                        "   >>> adding relationship");
                projectRels.put(child, parent);
            }
        }


        classTree.removeAllChildren();

// DEBUG
        System.out.println("Build tree");
        for (Iterator i = nodes.keySet().iterator(); i.hasNext();) {
            String nodeName = (String) i.next();
// DEBUG
            System.out.println(
                    " - node: "
                    + nodeName);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.get(nodeName);
// DEBUG
            System.out.println(
                    "    - node parent: "
                    + (node.getParent() == null ? "null" : node.getParent().toString()));
            if (rels.containsKey(nodeName)) {
                String parentName = (String) rels.get(nodeName);
// DEBUG
                System.out.println(
                        "    - parent: "
                        + parentName);
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) nodes.get(parentName);
// DEBUG
                System.out.println(
                        "    - node: "
                        + parent.toString());
                parent.add(node);
// DEBUG
                System.out.println(
                        "    - node parent: "
                        + (node.getParent() == null ? "null" : node.getParent().toString()));
            } else {
                classTree.add(node);
// DEBUG
                System.out.println(
                        "    - node: "
                        + classTree.toString());
// DEBUG
                System.out.println(
                        "    - node parent: "
                        + (node.getParent() == null ? "null" : node.getParent().toString()));
            }
            getClasses(cb, node, nodeName);
        }
        treeModel.reload(classTree);
    }

    private void clearTree(String topName) {
        globalTree.removeAllChildren();
        constantTree.removeAllChildren();
        objectTree.removeAllChildren();
        functionTree.removeAllChildren();
        classTree.removeAllChildren();
        top.setUserObject(topName);
        treeModel.reload();
    }

    private void refreshTitle() {
        if (fileTabbedPane.getTabCount() == 0) {
            setTitle(getJifVersion());
        } else {
            String currentName = getCurrentFilename();
            setTitle(getJifVersion() + " - " + currentName);
        }
    }

    // Modified to Use the Regular Expressions
    private void refreshTree() {
        long tempo1 = System.currentTimeMillis();

        // Reset the tree
        if (fileTabbedPane.getTabCount() == 0) {
            clearTree("Inspect");
            treeTree.setEnabled(false);
            clearOutput();
            return;
        }

        String currentFile = getCurrentFilename();

        // is this an Inform file?
        if (currentFile.endsWith(".txt") || currentFile.endsWith(".res")) {
            clearTree("Inspect");
            treeTree.setEnabled(false);
            return;
        }

        clearTree(currentFile.substring(currentFile.lastIndexOf(Constants.SEP) + 1));
        treeTree.setEnabled(true);

        CharBuffer cb = getCurrentJifTextPane().getCharBuffer();
        objTree = new Vector<Inspect>();

        // GLOBALS
        refreshGlobals(cb);
        // CONSTANTS
        refreshConstants(cb);
        // OBJECTS
        refreshObjects(cb);
        // FUNCTIONS
        refreshFunctions(cb);
        // CLASSES
// DEBUG
        //System.out.println("Refresh tree");
        refreshClasses(cb);

        expandAll(treeTree);
        //System.out.println("Tempo tree= "+(System.currentTimeMillis()-tempo1));
    }

    private void sortNodes(Vector<Inspect> vettore, DefaultMutableTreeNode nodo) {
        Collections.sort(new ArrayList<Inspect>(vettore), new Comparator() {

            @Override
            public int compare(Object a, Object b) {
                String id1 = ((Inspect) a).toString();
                String id2 = ((Inspect) b).toString();
                return (id1).compareToIgnoreCase(id2);
            }
        });
        int size = vettore.size();
        for (int count = 0; count < size; count++) {
            nodo.add(new DefaultMutableTreeNode((Inspect) vettore.get(count)));
        }
    }

    private void expandAll(JTree tree) {
        TreeNode root = (TreeNode) tree.getModel().getRoot();
        expandAll(tree, new TreePath(root));
    }

    private void expandAll(JTree tree, TreePath parent) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path);
            }
        }
        tree.expandPath(parent);
    }

    private void projectProperties() {

        try {
            String fileName = project.getFile().getPath();
            File file = new File(fileName);

            if (!file.exists()) {
                System.out.println(java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE1") + fileName);
                return;
            }

            projectPropertiesTextArea.setText(JifDAO.read(file));
            projectPropertiesTextArea.setCaretPosition(0);

            projectPropertiesDialog.setSize(500, 500);
            projectPropertiesDialog.setLocationRelativeTo(this);
            projectPropertiesDialog.setVisible(true);

        } catch (Exception ex) {
            System.out.println("ERROR Project properties: " + ex.getMessage());
        }
    }

    private void configProperties() {

        try {
            String fileName = config.getFile().getPath();
            File file = new File(fileName);

            if (!file.exists()) {
                System.out.println(java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE1") + fileName);
                return;
            }

            configTextArea.setText(JifDAO.read(file));
            configTextArea.setCaretPosition(0);

            configLabel.setText(fileName);

            configDialog.setSize(600, 550);
            configDialog.setLocationRelativeTo(this);
            configDialog.setVisible(true);

        } catch (Exception ex) {
            System.out.println("ERROR Configuration properties: " + ex.getMessage());
        }
    }

    private String makeCompilerSwitches() {
        StringBuffer make = new StringBuffer();
        Map switches = getSwitches();
        tipoz = "-v5";

        for (Iterator i = switches.keySet().iterator(); i.hasNext();) {
            String switchName = (String) i.next();
            String setting = (String) switches.get(switchName);

            if (setting.equals("on")) {
                if (switchName.indexOf("-v") == -1) {
                    make.append(" " + switchName);
                } else {
                    if (isInformMode()) {
                        make.append(" " + switchName);
                    }
                    tipoz = switchName;
                }
            }
        }

        // If in GLULX MODE, a "-G" switch is to be added
        if (isGlulxMode()) {
            make.append(" -G");
        }

        return make.toString();
    }

    private boolean isSaved() {
        for (int i = 0; i < fileTabbedPane.getTabCount(); i++) {
            if (fileTabbedPane.getTitleAt(i).endsWith("*")) {
                return false;
            }
        }
        return true;
    }

    public void exitJif() {
        // Save frame settings
        config.setFrameX(getX());
        config.setFrameY(getY());
        config.setFrameHeight(getHeight());
        config.setFrameWidth(getWidth());

        // Save, Exit and Cancel messages for exit options
        String[] scelte = new String[3];
        scelte[0] = java.util.ResourceBundle.getBundle("JIF").getString("MSG_SAVE_AND_EXIT");
        scelte[1] = java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF10");
        scelte[2] = java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF11");

        // If all files saved exit otherwise prompt with exit options
        int result = (isSaved()) ? 1 : JOptionPane.showOptionDialog(this,
                java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF12"),
                java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF13"),
                0,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                scelte,
                scelte[2]);

        switch (result) {
            // Save
            case 0:
                saveAll();
                saveProject();
            // Exit
            case 1:
                try {
                    JifConfigurationDAO.store(config);
                } catch (JifConfigurationException ex) {
                    ex.printStackTrace();
                } finally {
                    System.exit(0);
                }
            // Cancel
            default:
                break;
        }
    }

    // Per ogni classe nuova aggiungo al nodo passato, il nome degli oggetti di quella classe
    public void getClasses(CharBuffer cb, DefaultMutableTreeNode nodo, String nome) {
        // Class references using regular expression
        Pattern p = Pattern.compile("\n+\\s*" + nome + "\\s+(->\\s+)*(\\w+)",
                Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(cb);
        objTree.clear();
        while (m.find()) {
// DEBUG            
            System.out.println(
                    "Get classes found(1): " + m.group(2).toLowerCase()
                    + " at " + m.start(2));
            objTree.add(new Inspect(m.group(2).toLowerCase(), m.start(2)));
        }
        // Object class references using regular expression (first only)
        p = Pattern.compile("\n+\\s*Object\\s+(->\\s+)*(\\w+)\\s+(\"[^\"]+\")*\\s*(?:\\w+)*\\s*class\\s+" + nome,
                Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        m = p.matcher(cb);
        while (m.find()) {
// DEBUG
            System.out.println(
                    "Get classes found(2): " + m.group(2).toLowerCase()
                    + " at " + m.start(2));
            objTree.add(new Inspect(m.group(2).toLowerCase(), m.start(2)));
        }
        nodo.removeAllChildren();
        sortNodes(objTree, nodo);
        treeModel.reload(nodo);
    }

    public static final JifTextPane getCurrentJifTextPane() {
        if (fileTabbedPane.getTabCount() == 0) {
            return null;
        } else {
            return (JifTextPane) ((JScrollPane) fileTabbedPane.getSelectedComponent()).getViewport().getComponent(0);
        }
    }

    public static final DefaultStyledDocument getCurrentDoc() {
        if (fileTabbedPane.getTabCount() == 0) {
            return null;
        } else {
            return (DefaultStyledDocument) ((JifTextPane) ((JScrollPane) fileTabbedPane.getSelectedComponent()).getViewport().getComponent(0)).getDocument();
        }
    }

    public static final String getCurrentTitle() {
        return (fileTabbedPane.getTitleAt(fileTabbedPane.getSelectedIndex()));
    }

    public static final String getTitleAt(int aTabNumber) {
        return (fileTabbedPane.getTitleAt(aTabNumber));
    }

    public static final String getCurrentFilename() {
        JifScrollPane aScrollPane;
        if (fileTabbedPane.getTabCount() == 0) {
            return null;
        }
        aScrollPane = (JifScrollPane) fileTabbedPane.getComponentAt(fileTabbedPane.getSelectedIndex());
        return (aScrollPane.getFile());
    }

    public static final String getFilenameAt(int aTabNumber) {
        JifScrollPane aScrollPane;
        if (fileTabbedPane.getTabCount() == 0) {
            return null;
        }
        aScrollPane = (JifScrollPane) fileTabbedPane.getComponentAt(aTabNumber);
        return (aScrollPane.getFile());
        //return fileTabbedPane.getTitleAt(fileTabbedPane.getSelectedIndex());
    }

    private boolean checkOpenFile(String file) {
        //Controllo che non sia stato giÃ¯Â¿Â½ aperto un file
        String file_asterisco = file + "*";
        for (int i = 0; i < fileTabbedPane.getTabCount(); i++) {
            // I file aperti senza asterisco
            // controllo anche i file che hanno l'asterisco
            if (file.equals(getFilenameAt(i))
                    || file_asterisco.equals(getFilenameAt(i))) {
                fileTabbedPane.setSelectedIndex(i);
// DEBUG
                //System.out.println("check open file - refresh tree");
                refreshTree();
                refreshTitle();
                return true;
            }
        }
        return false;
    }

    /**
     * Disable components that are invalid when no files are open.
     */
    private void disableFileComponents() {

        // toolbar buttons
        saveButton.setEnabled(false);
        saveAllButton.setEnabled(false);
        saveAsButton.setEnabled(false);
        closeButton.setEnabled(false);
        closeAllButton.setEnabled(false);
        undoButton.setEnabled(false);
        redoButton.setEnabled(false);
        commentButton.setEnabled(false);
        uncommentButton.setEnabled(false);
        tabLeftButton.setEnabled(false);
        tabRightButton.setEnabled(false);
        bracketCheckButton.setEnabled(false);
        buildAllButton.setEnabled(false);
        runButton.setEnabled(false);
        insertSymbolButton.setEnabled(false);
        findTextField.setEnabled(false);
        findButton.setEnabled(false);
        replaceButton.setEnabled(false);

        // menu items
        saveMenuItem.setEnabled(false);
        saveAllMenuItem.setEnabled(false);
        saveAsMenuItem.setEnabled(false);
        closeMenuItem.setEnabled(false);
        closeAllMenuItem.setEnabled(false);
        printMenuItem.setEnabled(false);

        // menus
        editMenu.setEnabled(false);
        buildMenu.setEnabled(false);
        glulxMenu.setEnabled(false);

        // tree
        treeTree.setEnabled(false);

        // search
        definitionTextField.setEnabled(false);
        definitionButton.setEnabled(false);
    }

    /**
     * Enable components that are valid when a file is open
     */
    private void enableFileComponents() {

        // toolbar buttons
        saveButton.setEnabled(true);
        saveAllButton.setEnabled(true);
        saveAsButton.setEnabled(true);
        closeButton.setEnabled(true);
        closeAllButton.setEnabled(true);
        undoButton.setEnabled(true);
        redoButton.setEnabled(true);
        commentButton.setEnabled(true);
        uncommentButton.setEnabled(true);
        tabLeftButton.setEnabled(true);
        tabRightButton.setEnabled(true);
        bracketCheckButton.setEnabled(true);
        buildAllButton.setEnabled(true);
        runButton.setEnabled(true);
        insertSymbolButton.setEnabled(true);
        findTextField.setEnabled(true);
        findButton.setEnabled(true);
        replaceButton.setEnabled(true);

        // menu items
        saveMenuItem.setEnabled(true);
        saveAllMenuItem.setEnabled(true);
        saveAsMenuItem.setEnabled(true);
        closeMenuItem.setEnabled(true);
        closeAllMenuItem.setEnabled(true);
        printMenuItem.setEnabled(true);

        // menus
        editMenu.setEnabled(true);
        buildMenu.setEnabled(true);

        if (isGlulxMode() && fileTabbedPane.getTabCount() > 0) {
            glulxMenu.setEnabled(true);
        }

        // tree
        treeTree.setEnabled(true);

        // search
        definitionTextField.setEnabled(true);
        definitionButton.setEnabled(true);
    }

    // dal dialog Replace
    private void findString(String pattern) {
        // rimuovo tutti gli highligh
        // recupero la posizione del cursore
        // eeguo la ricerca e l'highlight del testo trovato
        int pos = getCurrentJifTextPane().getCaretPosition();   // current position
        try {
            String text = getCurrentDoc().getText(0, getCurrentDoc().getLength());
            boolean trovato = false;

            while (((pos = text.indexOf(pattern, pos)) >= 0) && (!trovato)) {
                //while ( ( (pos = Utils.IgnoreCaseIndexOf(text,pattern, pos)) >= 0) && (!trovato)) {
                getCurrentJifTextPane().requestFocus();
                getCurrentJifTextPane().setCaretPosition(pos);
                getCurrentJifTextPane().setSelectionStart(pos);
                getCurrentJifTextPane().setSelectionEnd(pos + pattern.length());
                getCurrentJifTextPane().repaint();
                pos += pattern.length();
                trovato = true;
                replaceDialog.requestFocus();
            }

            //se non lo trovo comunico che sono alla fine del file
            if (!trovato) {
                String[] scelte = new String[2];
                scelte[0] = java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF18");
                scelte[1] = java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_CANCEL");
                int result = JOptionPane.showOptionDialog(replaceDialog,
                        java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF19") + " [" + pattern + "]" + java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF20"),
                        java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF21"),
                        0,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        scelte,
                        scelte[1]);
                if (result == 0) {
                    getCurrentJifTextPane().setCaretPosition(0);
                    findString(pattern);
                }
                return;
            }
        } catch (BadLocationException e) {
            System.out.println("ERROR Find string: " + e.getMessage());
        }
    }

    private void insertFromFile() {
        String directory;
        try {
            if (config.getLastInsert() == null) {
                if (config.getLastFile() == null) {
                    directory = config.getWorkingDirectory();
                } else {
                    directory = config.getLastFile().getDirectory();
                }
            } else {
                directory = config.getLastInsert().getDirectory();
            }

            JFileChooser chooser = new JFileChooser(directory);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.CANCEL_OPTION) {
                return;
            }

            MutableAttributeSet attr = new SimpleAttributeSet();
            String result = chooser.getSelectedFile().getAbsolutePath();

            // imposto la lastInsert = a quella selezionata l'ultima volta
            config.setLastInsert(result);

            File file = new File(result);
            getCurrentDoc().insertString(getCurrentJifTextPane().getCaretPosition(), JifDAO.read(file), attr);
// DEBUG
            //System.out.println("insert file - refresh tree");
            refreshTree();

        } catch (Exception e) {
            System.out.println("ERROR Insert from file: " + e.getMessage());
        }
    }

    private void closeFile() {
        // se i il file non contiene * allora posso chiudere senza salvare
        if (getCurrentTitle().indexOf("*") == -1) {
            // chiudo senza chiedere
            fileTabbedPane.remove(fileTabbedPane.getSelectedComponent());
            return;
        }

        String[] scelte = new String[3];
        scelte[0] = java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF14");
        scelte[1] = java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF15");
        scelte[2] = java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_CANCEL");
        int result = JOptionPane.showOptionDialog(null, java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF16"),
                java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF17") + getCurrentFilename(),
                0,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                scelte,
                scelte[2]);

        switch (result) {
            // Save
            case 0:
                saveFile();
            // Close
            case 1:
                fileTabbedPane.remove(fileTabbedPane.getSelectedComponent());
            // Cancel
            default:
                break;
        }
    }

    // 1. cerco la prima occorrenza se non esiste esci
    // 2. replace string e vai al punto 1.
    private void replaceAll() {
        // Set Caret Position to ZERO
        getCurrentJifTextPane().setCaretPosition(0);
        boolean eseguito = false;
        while (!eseguito) {
            eseguito = findAllString(replaceFindTextField.getText());
            if (!eseguito) {
                getCurrentJifTextPane().replaceSelection(replaceReplaceTextField.getText());
            }
        }
    }

    // dal dialog ReplaceAll
    private boolean findAllString(String pattern) {
        // rimuovo tutti gli highlight
        // recupero la posizione del cursore
        // eeguo la ricerca e l'highlight del testo trovato

        int pos = getCurrentJifTextPane().getCaretPosition();   // current position
        try {
            String text = getCurrentDoc().getText(0, getCurrentDoc().getLength());
            boolean trovato = false;
            while (((pos = text.indexOf(pattern, pos)) >= 0) && (!trovato)) {
                getCurrentJifTextPane().requestFocus();
                getCurrentJifTextPane().setCaretPosition(pos);
                getCurrentJifTextPane().setSelectionStart(pos);
                getCurrentJifTextPane().setSelectionEnd(pos + pattern.length());
                getCurrentJifTextPane().repaint();
                pos += pattern.length();
                trovato = true;
                replaceDialog.requestFocus();
            }

            //se non lo trovo comunico che sono alla fine del file
            if (!trovato) {
                return true;
            } else {
                return false;
            }
        } catch (BadLocationException e) {
            System.out.println("ERROR Find all: " + e.getMessage());
        }
        return false;
    }

    public void copyToClipBoard() {
        // Prendo il testo selezionato e prendo la substring fino al primo carattere \n
        // e lo inserisco come testo del menu

        // controllo che che non venga superato il limite max di entry nel menu PASTE
        if (pastePopupMenu.getMenuComponentCount() > Constants.MAX_DIMENSION_PASTE_MENU) {
            //System.out.println("superato dimensione max per menu");
            return;
        }

        // come titolo del menu, limito al max a 8 caratteri
        // il testo incollato, sarÃ¯Â¿Â½ contenuto nel tooltip, opportunamente
        // modificato PLAIN -> HTML  e HTML -> PLAIN
        String test = getCurrentJifTextPane().getSelectedText();

        StringSelection ss = new StringSelection(test);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);

        if (test.trim().length() > 25) {
            test = test.trim().substring(0, 25) + "...";
        }
        JMenuItem mi = new JMenuItem(test.trim());

        //Come tool tip del menu metto tutto il codice selezionato
        String tmp = getCurrentJifTextPane().getSelectedText();
        // per vederlo tutto su piÃ¯Â¿Â½ righe....lo trasformo il testo in formato HTML
        tmp = Utils.replace(tmp, "\n", "<br>");
        mi.setToolTipText("<html>" + tmp + "</html>");
        mi.setFont(new Font("Dialog", Font.PLAIN, 11));
        pastePopupMenu.add(mi).addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    //String id = ((javax.swing.JMenuItem)evt.getSource()).getText();
                    String id = ((javax.swing.JMenuItem) evt.getSource()).getToolTipText();

                    //ricostruisco la stringa... da html a plain text
                    id = Utils.replace(id, "<br>", "\n");
                    id = Utils.replace(id, "<html>", "");
                    id = Utils.replace(id, "</html>", "");
                    MutableAttributeSet attr = new SimpleAttributeSet();
                    getCurrentDoc().insertString(getCurrentJifTextPane().getCaretPosition(), id, attr);
                } catch (BadLocationException e) {
                    System.out.println("ERROR Copy to clipboard: " + e.getMessage());
                }
            }
        });
    }

    private void showSymbolsDialog() {
        try {
            int pointx = (int) getCurrentJifTextPane().modelToView(getCurrentJifTextPane().getCaretPosition()).getX();
            int pointy = (int) getCurrentJifTextPane().modelToView(getCurrentJifTextPane().getCaretPosition()).getY();
            symbolDialog.setLocation((int) getCurrentJifTextPane().getLocationOnScreen().getX() + pointx,
                    (int) getCurrentJifTextPane().getLocationOnScreen().getY() + pointy + 15);
            symbolDialog.setSize(230, 200);
            symbolDialog.requestFocus();
            symbolDialog.toFront();
            symbolDialog.setVisible(true);
        } catch (BadLocationException e) {
            System.out.println("ERROR Show symbols: " + e.getMessage());
        }
    }

    private void saveAs(String directory) {
        // recupero il nuovo nome del file e lo salvo....
        // String result = JOptionPane.showInputDialog(this , java.util.ResourceBundle.getBundle("JIF").getString("MSG_NEWFILE1")+config.gamePath, java.util.ResourceBundle.getBundle("JIF").getString("MSG_NEWFILE2"), JOptionPane.OK_CANCEL_OPTION);
        JFileChooser chooser;
        if (directory != null) {
            chooser = new JFileChooser(directory);
        } else if (config.getLastFile() != null) {
            chooser = new JFileChooser(config.getLastFile().getDirectory());
        } else {
            chooser = new JFileChooser(resolveAbsolutePath(config.getWorkingDirectory(), config.getGamePath()));
        }
        chooser.setDialogTitle(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_SAVEAS"));
        chooser.setApproveButtonText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_SAVE"));

        // Selezione Multipla
        chooser.setMultiSelectionEnabled(false);

        if (chooser.showOpenDialog(this) == JFileChooser.CANCEL_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        String result = file.getAbsolutePath();

        // se il file non ha estensione: gliela inserisco io INF
        if (result.lastIndexOf(".") == -1) {
            result = result + ".inf";
        }

        // se l'utente ha inserito una cosa del tipo...
        // nome.cognome -> il nome viene convertito in nome.cognome.inf
        // controllo che l'utente non abbia scritto nome.txt, nome.res ecc
        if (((result.lastIndexOf(".") != -1) && (result.lastIndexOf(".inf")) == -1)
                && (!(result.endsWith(".res"))
                && !(result.endsWith(".txt"))
                && !(result.endsWith(".h"))
                && !(result.endsWith(".doc")))) {
            result = result + ".inf";
        }

        // controllo che non esista giÃ¯Â¿Â½ un file con quel nome
        if (file.exists()) {
            int overwrite = JOptionPane.showConfirmDialog(this,
                    java.util.ResourceBundle.getBundle("JIF").getString("ERR_NAMEFILE4"),
                    java.util.ResourceBundle.getBundle("JIF").getString("ERR_NAMEFILE2"),
                    JOptionPane.ERROR_MESSAGE);
            if (overwrite == JOptionPane.NO_OPTION) {
                return;
            }
        }

        fileTabbedPane.setTitleAt(fileTabbedPane.getSelectedIndex(), result);
        JifScrollPane aScrollPane = (JifScrollPane) fileTabbedPane.getComponentAt(fileTabbedPane.getSelectedIndex());
        aScrollPane.setFile(result);
        getCurrentJifTextPane().setPaths(result); // BUG in the TAB title
        saveFile();
// DEBUG
        //System.out.println("save as - refresh tree");
        refreshTree();
        refreshTitle();
    }

    // funzione che gestisce l'inserimento di files in un progetto
    // supporta la seleziona multipla
    private void addFilesToProject() {
        // If a project is null, return
        if (project.getFile() == null) {
            return;
        }
        // Open the project file directory to add the files
        String dir = project.getFile().getDirectory();
        JFileChooser chooser = new JFileChooser(dir);
        JifFileFilter infFilter = new JifFileFilter("inf", java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF7"));
        infFilter.addExtension("h");
        infFilter.addExtension("res");
        infFilter.addExtension("txt");
        chooser.setFileFilter(infFilter);
        // Selezione Multipla
        chooser.setMultiSelectionEnabled(true);

        if (chooser.showOpenDialog(this) == JFileChooser.CANCEL_OPTION) {
            return;
        }

        File[] files = chooser.getSelectedFiles();
        File file;

        for (int i = 0; i < files.length; i++) {
            file = files[i];
            // apro il file e lo aggiungo alla lista se il checkbox Ã¯Â¿Â½ attivo
            if (config.getOpenProjectFiles()) {
                openFile(file.getAbsolutePath());
            }

            project.addFile(file.getAbsolutePath());
        }
        saveProject();
    }

    // The project inherits the default switches and mode
    private void newProject() {

        try {
            JFileChooser chooser;
            chooser = new JFileChooser();
            chooser.setDialogTitle(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_NEW_PROJECT"));
            chooser.setApproveButtonText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_SAVE"));
            chooser.setMultiSelectionEnabled(false);
            if (chooser.showOpenDialog(this) == JFileChooser.CANCEL_OPTION) {
                return;
            }
            File file = chooser.getSelectedFile();

            if (file.getName().indexOf(".jpf") == -1) {
                // Add the .jpf extension
                file = new File(file.getAbsolutePath() + ".jpf");
            }

            if (file.exists()) {
                int result = JOptionPane.showConfirmDialog(this,
                        java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_PROJECT_EXISTS_OVERWRITE"),
                        file.getAbsolutePath(),
                        JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.CANCEL_OPTION) {
                    return;
                }
                file.delete();
            }

            project.clear();
            project.setFile(file.getAbsolutePath());
            project.setSwitches(config.getSwitches());
            project.setInformMode(config.getInformMode());

            saveProject();
            saveProjectMessage();
            enableProjectComponents();

        } catch (Exception e) {
            System.out.println("ERROR New project: " + e.getMessage());
        }
    }

    /**
     * Enable components that are valid when a project is open.
     */
    private void enableProjectComponents() {

        // menu
        saveProjectMenuItem.setEnabled(true);
        closeProjectMenuItem.setEnabled(true);
        addNewToProjectMenuItem.setEnabled(true);
        addFileToProjectMenuItem.setEnabled(true);
        removeFromProjectMenuItem.setEnabled(true);
        projectPropertiesMenuItem.setEnabled(true);
        projectSwitchesMenuItem.setEnabled(true);

        // popup menu
        saveProjectPopupMenuItem.setEnabled(true);
        closeProjectPopupMenuItem.setEnabled(true);
        addNewToProjectPopupMenuItem.setEnabled(true);
        addFileToProjectPopupMenuItem.setEnabled(true);
        removeFromProjectPopupMenuItem.setEnabled(true);
        openSelectedFilesPopupMenuItem.setEnabled(true);
        setMainPopupMenuItem.setEnabled(true);
        removeMainPopupMenuItem.setEnabled(true);

        // project tab
        projectScrollPane.setEnabled(true);

        // search tab
        searchProjectTextField.setEnabled(true);
        searchProjectButton.setEnabled(true);
    }

    /**
     * Disable components that are invalid when no project is open.
     */
    private void disableProjectComponents() {

        // menu
        saveProjectMenuItem.setEnabled(false);
        closeProjectMenuItem.setEnabled(false);
        addNewToProjectMenuItem.setEnabled(false);
        addFileToProjectMenuItem.setEnabled(false);
        removeFromProjectMenuItem.setEnabled(false);
        projectPropertiesMenuItem.setEnabled(false);
        projectSwitchesMenuItem.setEnabled(false);

        // popup menu
        saveProjectPopupMenuItem.setEnabled(false);
        closeProjectPopupMenuItem.setEnabled(false);
        addNewToProjectPopupMenuItem.setEnabled(false);
        addFileToProjectPopupMenuItem.setEnabled(false);
        removeFromProjectPopupMenuItem.setEnabled(false);
        openSelectedFilesPopupMenuItem.setEnabled(false);
        setMainPopupMenuItem.setEnabled(false);
        removeMainPopupMenuItem.setEnabled(false);

        // project tab
        projectScrollPane.setEnabled(false);

        // search tab
        searchProjectTextField.setEnabled(false);
        searchProjectButton.setEnabled(false);
    }

    private void updateProjectList() {
        projectList.removeAll();
        projectList.setListData(project.getFiles());
    }

    private void updateProjectMain() {
        String title = (project.getMain() == null)
                ? ""
                : project.getMain().getName();
        mainFileLabel.setText("Main: " + title);
    }

    private void updateProjectTitle() {
        String title = (project.getFile() == null)
                ? Constants.PROJECTEMPTY
                : project.getFile().getName();
        TitledBorder tb = new TitledBorder("Project: " + title);
        tb.setTitleFont(new Font("Dialog", Font.PLAIN, 11));
        projectScrollPane.setBorder(tb);
    }

    private void openProject() {

        String search = (config.getLastProject() == null)
                ? config.getWorkingDirectory()
                : config.getLastProject().getPath();

        JFileChooser chooser = new JFileChooser(search);
        JifFileFilter infFilter = new JifFileFilter("jpf", "Jif Project File");
        chooser.setFileFilter(infFilter);
        if (chooser.showOpenDialog(this) == JFileChooser.CANCEL_OPTION) {
            return;
        }

        openProject(chooser.getSelectedFile().getAbsolutePath());
    }

    private void openProject(String projectPath) {

        // Check whether there is already an open project
        if (project.getFile() != null) {
            saveProject();
            closeProject();
        }

        File projectSource = new File(projectPath);
        project.setFile(projectSource.getAbsolutePath());

        try {
            JifProjectDAO.reload(project);

        } catch (JifProjectException ex) {
            System.out.println("ERROR Open project (" + projectPath + "): " + ex.getMessage());
        }

        // Scan for project classes if neccesary
        if (config.getScanProjectFiles()) {
// DEBUG
//            System.out.println(
//                    "Open project(" +
//                    projectSource.getAbsolutePath() +
//                    ")"
//            );
            //System.out.println("============");
            seekClasses();
            seekClassToClass();
        }

        // Open project files if neccesary
        if (config.getOpenProjectFiles()) {
            for (Iterator i = project.getFiles().iterator(); i.hasNext();) {
                JifFileName projectFile = (JifFileName) i.next();
                // don't open automatically *.h files
                if (!projectFile.getType().equals("h")) {
                    openFile(projectFile.getPath());
                    fileTabbedPane.setSelectedIndex(0);
                }
            }
        }

        // Set mode to project setting
        if (project.getInformMode()) {
            setInformMode();
        } else {
            setGlulxMode();
        }
        refreshTitle();

        // View the Project Panel
        leftTabbedPane.setSelectedIndex(1);

        // Last Project opened
        config.setLastProject(projectSource.getAbsolutePath());

        enableProjectComponents();
    }

    /**
     * Seek classes in a source CharBuffer adding each class found to the
     * project class map and project node map.
     *
     * @param cb
     *          <code>CharBuffer</code> of inform source code
     */
    private void seekClasses() {

// DEBUG
        //System.out.println("seek classes");
        //System.out.println("============");
        for (Iterator i = project.getFiles().iterator(); i.hasNext();) {
            JifFileName projectFile = (JifFileName) i.next();
            if (projectFile.getContentType() == JifFileName.INFORM) {
                File file = new File(projectFile.getPath());

                try {
                    // Get a buffer for the source file
                    CharBuffer cb = JifDAO.buffer(file);

                    Matcher m = classPattern.matcher(cb);
                    while (m.find()) {
// DEBUG
                        System.out.println(
                                "Found new class: "
                                + m.group(2).toLowerCase()
                                + " at " + m.start(2)
                                + " in " + projectFile.getPath());
                        String className = m.group(2).toLowerCase();
                        Inspect nullRef = new Inspect(className, -1);
                        projectClasses.put(className, nullRef);
                        projectNodes.put(className, new DefaultMutableTreeNode(nullRef));
                    }

                } catch (Exception ex) {
                    System.out.println("ERROR: Seek classes " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }

    private void seekClassToClass() {

// DEBUG
        //System.out.println("seek class to class");
        //System.out.println("===================");
        for (Iterator i = project.getFiles().iterator(); i.hasNext();) {
            JifFileName projectFile = (JifFileName) i.next();
            if (projectFile.getContentType() == JifFileName.INFORM) {
                File file = new File(projectFile.getPath());

                try {
                    // Get a buffer for the source file
                    CharBuffer cb = JifDAO.buffer(file);

                    Matcher m = classToClassPattern.matcher(cb);
                    while (m.find()) {
// DEBUG
//                        System.out.println("Found class to class: " + m.group(1).toLowerCase() +
//                                " at " + m.start(1) +
//                                " to " + m.group(2).toLowerCase() +
//                                " in " + projectFile.getPath()
//                                );
                        String child = m.group(1).toLowerCase();
                        String parent = m.group(2).toLowerCase();
                        projectRels.put(child, parent);
                    }

                } catch (Exception ex) {
                    System.out.println("ERROR: Seek class to class " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }

    // chiude un progetto. Inserire un flag per chiudere tutti i files
    // relativi ad un progetto quando si chiude il progetto stesso
    private void closeProject() {
        closeAllFiles();
        disableProjectComponents();
        project.clear();
        projectClasses.clear();
        projectNodes.clear();
        projectRels.clear();
        // Restore mode to configuration setting
        if (config.getInformMode()) {
            setInformMode();
        } else {
            setGlulxMode();
        }
        refreshTitle();
    }

    private void saveProject() {
        if (project.getFile() == null) {
            return;
        }

        try {
            JifProjectDAO.store(project);

        } catch (JifProjectException ex) {
            System.out.println("ERROR Project save: " + ex.getMessage());
        }
    }

    private void saveProjectMessage() {
        JOptionPane.showMessageDialog(
                configDialog,
                project.getFile().getName()
                + " "
                + java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE4"),
                java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE2"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearMainForProject() {
        JOptionPane.showMessageDialog(this,
                "Clearing Main file",
                "Main file",
                JOptionPane.INFORMATION_MESSAGE);
        project.clearMain();
    }

    private void setMainForProject() {
        JifFileName fp = (JifFileName) projectList.getSelectedValue();
        if (fp == null || !fp.getType().equals("inf")) {
            return;
        }
        project.setMain(fp);
    }

    private void removeFileFromProject() {
        JifFileName fp = (JifFileName) projectList.getSelectedValue();
        if (fp == null) {
            return;
        }
        // Confirm file removal
        String message;
        int option;
        if (fp.equals(project.getMain())) {
            message = java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_DELETE_MAIN_FROM_PROJECT");
        } else {
            message = java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_DELETE_FILE_FROM_PROJECT");
        }
        if (JOptionPane.showConfirmDialog(this,
                message,
                "File : " + fp.getName(),
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
            return;
        }

        // Remove the file and save the project
        project.removeFile(fp);
        saveProject();
    }

    private void closeAllFiles() {
        int numberOfComponents = fileTabbedPane.getTabCount();
        for (int i = 0; i < numberOfComponents; i++) {
            closeFile();
        }
        disableFileComponents();
// DEBUG
        //System.out.println("close all - refresh tree");
        refreshTree();
        refreshTitle();
        System.gc();
    }

    // Lancia l'interprete senza passargli il file AT (.inf)
    // This method has to be splitted in 2
    private void runInterpreter() {
        if (isInformMode()) {
            runInformInterpreter();
        } else {
            runGlulxInterpreter();
        }
    }

    private void runGlulxInterpreter() {
        // Check out if a glulx interpreter exists
        File test = new File(config.getInterpreterGlulxPath());
        if (!test.exists()) {
            JOptionPane.showMessageDialog(this,
                    java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER1") + " " + config.getInterpreterGlulxPath() + java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER2"),
                    java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER3"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String auxGlux[] = new String[1];
        auxGlux[0] = new String(config.getInterpreterGlulxPath());

        try {
            Runtime rt = Runtime.getRuntime();
            rt.exec(auxGlux);
        } catch (IOException e) {
            System.out.println("ERROR Running Glulx interpreter: " + e.getMessage());
        }
    }

    private void runInformInterpreter() {
        // Check out if a Zcode interpreter exists
        File test = new File(config.getInterpreterZcodePath());
        if (!test.exists()) {
            JOptionPane.showMessageDialog(this,
                    java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER1") + " " + config.getInterpreterZcodePath() + java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER2"),
                    java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER3"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String auxInter[] = new String[1];
        auxInter[0] = new String(config.getInterpreterZcodePath());

        try {
            Runtime rt = Runtime.getRuntime();
            rt.exec(auxInter); //Process proc =  unused
        } catch (IOException e) {
            System.out.println("ERROR Running Zcode interpreter: " + e.getMessage());
        }
    }

    // visualizza un file con tasto OK
    // senza la syntax highlight
    private void showFile(InputStream is) {
        tutorialEditorPane.setEditorKit(new StyledEditorKit());
        tutorialEditorPane.setBackground(config.getContext().getBackground());
        tutorialDialog.setTitle("Readme");
        tutorialDialog.setSize(700, 550);
        tutorialDialog.setLocationRelativeTo(this);
        tutorialDialog.setVisible(true);
        try {
            tutorialEditorPane.setText(JifDAO.read(is));
            tutorialEditorPane.setCaretPosition(0);

            tutorialLabel.setText("Tutorial");

        } catch (IOException e) {
            System.out.println("ERROR Show tutorial file: " + e.getMessage());
        }
    }

    private void updateLastProject() {
        if (config.getLastProject() == null) {
            return;
        }

        lastProjectMenuItem.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_OPEN")
                + " ("
                + config.getLastProject().getName()
                + ")");
    }

    private void updateRecentFiles() {
        // Recentfiles
        recentFilesMenu.removeAll();
        for (Iterator i = config.getRecentFilesSet().iterator(); i.hasNext();) {
            String file = (String) i.next();
            JMenuItem mi = new JMenuItem(file);
            mi.setName(file);
            mi.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    openFile(((javax.swing.JMenuItem) evt.getSource()).getText());
                }
            });
            recentFilesMenu.add(mi);
        }
    }

    private void updateMenues() {
        // Recentfiles
        insertNewMenu.removeAll();
        for (Iterator i = config.getMenusSet().iterator(); i.hasNext();) {
            String elem = (String) i.next();
            JMenu menu = new JMenu(elem);
            menu.setName(elem);

            // submenues
            for (Iterator sub = config.getSubMenu(elem).iterator(); sub.hasNext();) {
                String submenu = (String) sub.next();
                JMenuItem mi = new JMenuItem(submenu);
                mi.setName(submenu);
                menu.add(mi).addMouseListener(menuListener);
                //operations.put(m.group(1),m.group(2));
            }
            insertNewMenu.add(menu);
        }
    }

    private void updateAttributeColor() {
        Color color = optionContext.getForeground(InformSyntax.Attribute);
        attributeColorLabel.setForeground(color);
        attributeColorButton.setBackground(color);
    }

    private void updateBackgroundColor() {
        Color color = optionContext.getBackground();
        backgroundColorLabel.setForeground(color);
        backgroundColorButton.setBackground(color);
        colorEditorPane.setBackground(color);
        highlightEditorPane.setBackground(color);
    }

    private void updateBookmarksColor() {
        Color color = optionContext.getForeground(InformSyntax.Bookmarks);
        bookmarkColorLabel.setForeground(color);
        bookmarkColorButton.setBackground(color);
    }

    private void updateBracketsColor() {
        Color color = optionContext.getForeground(InformSyntax.Brackets);
        bracketColorLabel.setForeground(color);
        bracketColorButton.setBackground(color);
    }

    private void updateCommentColor() {
        Color color = optionContext.getForeground(InformSyntax.Comment);
        commentColorLabel.setForeground(color);
        commentColorButton.setBackground(color);
    }

    private void updateErrorsColor() {
        Color color = optionContext.getForeground(InformSyntax.Errors);
        errorColorLabel.setForeground(color);
        errorColorButton.setBackground(color);
    }

    private void updateJumpToColor() {
        Color color = optionContext.getForeground(InformSyntax.JumpTo);
        jumpToColorLabel.setForeground(color);
        jumpToColorButton.setBackground(color);
    }

    private void updateKeywordColor() {
        Color color = optionContext.getForeground(InformSyntax.Keyword);
        keywordColorLabel.setForeground(color);
        keywordColorButton.setBackground(color);
    }

    private void updateNormalColor() {
        Color color = optionContext.getForeground(InformSyntax.Normal);
        normalColorLabel.setForeground(color);
        normalColorButton.setBackground(color);
    }

    private void updateNumberColor() {
        Color color = optionContext.getForeground(InformSyntax.Number);
        numberColorLabel.setForeground(color);
        numberColorButton.setBackground(color);
    }

    private void updatePropertyColor() {
        Color color = optionContext.getForeground(InformSyntax.Property);
        propertyColorLabel.setForeground(color);
        propertyColorButton.setBackground(color);
    }

    private void updateStringColor() {
        Color color = optionContext.getForeground(InformSyntax.String);
        stringColorLabel.setForeground(color);
        stringColorButton.setBackground(color);
    }

    private void updateVerbColor() {
        Color color = optionContext.getForeground(InformSyntax.Verb);
        verbColorLabel.setForeground(color);
        verbColorButton.setBackground(color);
    }

    private void updateWarningsColor() {
        Color color = optionContext.getForeground(InformSyntax.Warnings);
        warningColorLabel.setForeground(color);
        warningColorButton.setBackground(color);
    }

    private void updateWhiteColor() {
        Color color = optionContext.getForeground(InformSyntax.White);
        wordColorLabel.setForeground(color);
        wordColorButton.setBackground(color);
    }

    private void updateWordColor() {
        Color color = optionContext.getForeground(InformSyntax.Word);
        wordColorLabel.setForeground(color);
        wordColorButton.setBackground(color);
    }

    private void updateColor() {
        // Background colour
        updateBackgroundColor();
        // Syntax colours
        updateAttributeColor();
        updateCommentColor();
        updateKeywordColor();
        updateNormalColor();
        updateNumberColor();
        updatePropertyColor();
        updateStringColor();
        updateVerbColor();
        updateWhiteColor();
        updateWordColor();
    }

    private void updateHighlights() {
        String selection = (String) highlightSelectedComboBox.getSelectedItem();

        if (optionHighlight != null) {
            optionHighlight.removeHighlights(highlightEditorPane);
        }

        if (selection.equals("Bookmark")) {
            optionHighlight = new HighlightText(
                    highlightEditorPane,
                    optionContext.getForeground(InformSyntax.Bookmarks));

        }

        if (selection.equals("Bracket")) {
            optionHighlight = new HighlightText(
                    highlightEditorPane,
                    optionContext.getForeground(InformSyntax.Brackets));

        }

        if (selection.equals("Error")) {
            optionHighlight = new HighlightText(
                    highlightEditorPane,
                    optionContext.getForeground(InformSyntax.Errors));

        }

        if (selection.equals("JumpTo")) {
            optionHighlight = new HighlightText(
                    highlightEditorPane,
                    optionContext.getForeground(InformSyntax.JumpTo));

        }

        if (selection.equals("Warning")) {
            optionHighlight = new HighlightText(
                    highlightEditorPane,
                    optionContext.getForeground(InformSyntax.Warnings));

        }
        optionHighlight.highlightFromTo(highlightEditorPane, 17, 159);
    }

    private void updateHighlight() {
        // Highlight colours
        updateBookmarksColor();
        updateBracketsColor();
        updateErrorsColor();
        updateJumpToColor();
        updateWarningsColor();
    }

    private void updateFont() {
        fontNameComboBox.setSelectedItem(config.getContext().getFontName());
        Integer fontSize = new Integer(config.getContext().getFontSize());
        fontSizeComboBox.setSelectedItem(fontSize.toString());
        tabSizeTextField.setText(String.valueOf(JifEditorKit.getTabSize()));
    }

    private void createHighlightEditor() {
        highlightEditorPane.setDoubleBuffered(false);
        highlightEditorPane.setEditorKit(new InformEditorKit());
        highlightEditorPane.setEditable(false);
        highlightEditorPane.setBackground(optionContext.getBackground());
        highlightEditorPane.setDocument(new InformDocument(optionContext));
        StringBuffer sb = new StringBuffer();
        sb.append("! Poisoned Apple\n").append("Object  apple \"Poisoned Apple\"\n").append("with\n").append("  description \"It's a red apple.\",\n").append("  name \'apple\' \'red\' \'poisoned\',\n").append("  number 1234,\n").append("  before [;\n").append("    Eat : \n").append("    print \"This is a poisoned apple, isn't it?\"\n").append("    return true;\n").append("  ],\n").append("has   light;\n");
        highlightEditorPane.setText(sb.toString());
        updateHighlights();
    }

    private void createColorEditor() {
        colorEditorPane.setDoubleBuffered(false);
        colorEditorPane.setEditorKit(new InformEditorKit());
        colorEditorPane.setEditable(false);
        colorEditorPane.setBackground(optionContext.getBackground());
        colorEditorPane.setDocument(new InformDocument(optionContext));
        StringBuffer sb = new StringBuffer();
        sb.append("! Poisoned Apple\n").append("Object  apple \"Poisoned Apple\"\n").append("with\n").append("  description \"It's a red apple.\",\n").append("  name \'apple\' \'red\' \'poisoned\',\n").append("  number 1234,\n").append("  before [;\n").append("    Eat : \n").append("    print \"This is a poisoned apple, isn't it?\"\n").append("    return true;\n").append("  ],\n").append("has   light;\n");
        colorEditorPane.setText(sb.toString());
    }

    // If a string is on the system clipboard, this method returns it;
    // otherwise it returns null.
    public static String getClipboard() {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        try {
            if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String text = (String) t.getTransferData(DataFlavor.stringFlavor);
                return text;
            }
        } catch (UnsupportedFlavorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // funzione che testa se TUTTI gli switch +language_name.....sono DISATTIVATI...
    // true : tutti spenti
    // false : almeno uno acceso
    // TODO This method is not used
    private boolean checkDefaultLanguage() {
        // prendo il pannello 2 dello switch manager,
        // scandisco tutti i chechbox, se ne trovo uno solo che inizia con
        // +language_name ATTIVO, ritorno FALSE
        // altrimenti ritorno TRUE

        for (int count = 0; count < switchesLowerPanel.getComponentCount(); count++) {
            Checkbox ch = (Checkbox) switchesLowerPanel.getComponent(count);
            if (ch.getLabel().startsWith("+language_name") && ch.getState()) {
                return false;
            }
        }
        return true;
    }

    // Refresh option paths to existing values
    private void updateOptionPaths() {

        // Compiler path tab
        compilerPathTextField.setText(config.getCompilerPath());
        gamePathTextField.setText(config.getGamePath());
        interpreterPathTextField.setText(config.getInterpreterZcodePath());

        // Library path tab
        libraryPathTextField.setText(config.getLibraryPath());
        libraryPath1TextField.setText(config.getLibraryPath1());
        libraryPath2TextField.setText(config.getLibraryPath2());
        libraryPath3TextField.setText(config.getLibraryPath3());

        // Glulx path tab
        glulxPathTextField.setText(config.getInterpreterGlulxPath());
        bresPathTextField.setText(config.getBresPath());
        blcPathTextField.setText(config.getBlcPath());

    }

    // Update color editor settings to dark color defaults
    private void defaultDarkColors() {
        optionContext.defaultDarkColors();
        updateColor();
    }

    // Update color editor settings to light color defaults
    private void defaultLightColors() {
        optionContext.defaultLightColors();
        updateColor();
    }

    // Update font editor settings to defaults
    private void defaultFont() {
        fontNameComboBox.setSelectedItem(InformContext.defaultFontName);
        int fontSize = InformContext.defaultFontSize;
        fontSizeComboBox.setSelectedItem((new Integer(fontSize)).toString());
        tabSizeTextField.setText(Integer.toString(InformContext.defaultTabSize));
    }

    private void defaultDarkHighlights() {
        optionContext.defaultDarkSelections();
        updateHighlight();
        updateHighlights();
    }

    private void defaultLightHighlights() {
        optionContext.defaultLightSelections();
        updateHighlight();
        updateHighlights();
    }

    // Update general editor settings to defaults
    private void defaultOptions() {
        openLastFileCheckBox.setSelected(false);
        createNewFileCheckBox.setSelected(false);
        mappingLiveCheckBox.setSelected(false);
        projectOpenAllFilesCheckBox.setSelected(false);
        helpedCodeCheckBox.setSelected(true);
        numberLinesCheckBox.setSelected(true);
        scanProjectFilesCheckBox.setSelected(true);
        syntaxCheckBox.setSelected(true);
        wrapLinesCheckBox.setSelected(false);
    }

    // run BLC SOURCE.blc source.blb to make blb (GLULX MODE)
    private void makeBlb() {//AQUI!!
        String fileInf = "";
        // controllo che esista il compilatore con il path  inserito nella Jif.cfg
        // se non esiste visualizzo un messaggio di warning
        File test = new File(resolveAbsolutePath(config.getWorkingDirectory(), config.getBlcPath()));
        if (!test.exists()) {
            JOptionPane.showMessageDialog(this,
                    java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER1") + " " + resolveAbsolutePath(config.getWorkingDirectory(), config.getBlcPath()) + java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER2"),
                    java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER3"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // se l'utente ha tolto la visto della TextAreaOutput, la rendo visible..
        if (!outputCheckBoxMenuItem.getState()) {
            //jCheckBoxOutput.setState(true);
            mainSplitPane.setBottomComponent(outputTabbedPane);
            outputTabbedPane.setVisible(true);
        }
        //imposto il focus sulla tabbedWindow della compilazione
        outputTabbedPane.setSelectedComponent(outputScrollPane);

        //recupero l'attuale file name
        if (project.getMain() == null) {
            fileInf = getCurrentFilename();
        } else {
            fileInf = project.getMain().getPath();
            outputTextArea.append("Using main file " + fileInf + " to compiling...\n");
        }

        // Source file name
        String source = fileInf.substring(0, fileInf.lastIndexOf("."));
        String pathForCd = fileInf.substring(0, fileInf.lastIndexOf(Constants.SEP));
        //outputTextArea.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER1"));
        String process_string[] = new String[3];

        process_string[0] = resolveAbsolutePath(config.getWorkingDirectory(), config.getBlcPath());
        process_string[1] = new String(source + ".blc");
        process_string[2] = new String(source + ".blb");

        outputTextArea.append(resolveAbsolutePath(config.getWorkingDirectory(), config.getBlcPath()) + " " + source + ".blc " + source + ".blb\n");

        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(process_string, null, new File(pathForCd));
            String line = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream(), Constants.fileFormat));

            while ((line = br.readLine()) != null) {
                outputTextArea.append(line + "\n");
            }
            proc.waitFor(); //unused int i =
            outputTextArea.append("\n");
            outputTextArea.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER2"));
            outputTextArea.append("\n");
        } catch (IOException e) {
            System.out.println("ERROR Make blb: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("ERROR Make blb: " + e.getMessage());
        }
    }

    // run BRE SOURCE to make resource (GLULX MODE) //aqui
    private void makeResources() {
        String fileInf = "";
        // controllo che esista il compilatore con il path  inserito nella Jif.cfg
        // se non esiste visualizzo un messaggio di warning
        File test = new File(resolveAbsolutePath(config.getWorkingDirectory(), config.getBresPath()));
        if (!test.exists()) {
            JOptionPane.showMessageDialog(this,
                    java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER1") + " " + resolveAbsolutePath(config.getWorkingDirectory(), config.getBresPath()) + " " + java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER2"),
                    java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER3"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // se l'utente ha tolto la visto della TextAreaOutput, la rendo visible..
        if (!outputCheckBoxMenuItem.getState()) {
            //jCheckBoxOutput.setState(true);
            mainSplitPane.setBottomComponent(outputTabbedPane);
            outputTabbedPane.setVisible(true);
        }
        //imposto il focus sulla tabbedWindow della compilazione
        outputTabbedPane.setSelectedComponent(outputScrollPane);

        if (project.getMain() != null) {
            fileInf = project.getMain().getPath();
            outputTextArea.append("Using main file " + fileInf + " to compiling...\n");
        } else {
            //recupero l'attuale file name
            fileInf = getCurrentFilename(); //fileTabbedPane.getTitleAt( fileTabbedPane.getSelectedIndex());
        }
        // Source file name
        String source = fileInf.substring(0, fileInf.lastIndexOf("."));

        //outputTextArea.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER1"));
        String process_string[] = new String[2];
        process_string[0] = resolveAbsolutePath(config.getWorkingDirectory(), config.getBresPath());
        process_string[1] = source;

        outputTextArea.append(resolveAbsolutePath(config.getWorkingDirectory(), config.getBresPath()) + " " + source + "\n");

        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(process_string, null, new File(resolveAbsolutePath(config.getWorkingDirectory(), config.getGamePath())));

            String line = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream(), Constants.fileFormat));

            while ((line = br.readLine()) != null) {
                outputTextArea.append(line + "\n");
            }

            proc.waitFor(); //int i = unused
            outputTextArea.append("\n");
            outputTextArea.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER2"));
            outputTextArea.append("\n");
        } catch (IOException e) {
            System.out.println("ERROR Make resource: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("ERROR Make resource: " + e.getMessage());
        }
    }

    // Set INFORM MODE
    private void setInformMode() {

        // Title
        setJifVersion(Constants.JIFVERSION + "     Inform Mode");

        // Mode menu
        glulxModeCheckBoxMenuItem.setState(false);
        informModeCheckBoxMenuItem.setState(true);

        // Glulx menu
        glulxMenu.setEnabled(false);
    }

    // Set GLUX MODE
    private void setGlulxMode() {

        // Title
        setJifVersion(Constants.JIFVERSION + "     Glux Mode");

        // Mode menu check boxes
        glulxModeCheckBoxMenuItem.setState(true);
        informModeCheckBoxMenuItem.setState(false);

        // Glulx menu
        if (fileTabbedPane.getTabCount() == 0) {
            glulxMenu.setEnabled(false);
        } else {
            glulxMenu.setEnabled(true);
        }

    }

    // Esegue il file blb
    private void runBlb() {
        String fileInf = "";
        //String inter = config.getInterpreterGlulxPath();
        String inter = config.getInterpreterPath();

        // controllo che esista l'interprete con il path inserito nella Jif.cfg
        // se non esiste visualizzo un messaggio di warning
        File test = new File(inter);
        if (!test.exists()) {
            JOptionPane.showMessageDialog(this,
                    java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER1") + " " + inter + " " + java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER2"),
                    java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER3"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        //recupero l'attuale file name
        if (project.getMain() == null) {
            fileInf = getCurrentFilename();
        } else {
            fileInf = project.getMain().getPath();
            outputTextArea.append("Using main file " + fileInf + "...\n");
        }

        clearOutput();
        outputTextArea.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_RUN1"));

        try {
            Runtime rt = Runtime.getRuntime();
            String command[] = new String[2];
            command[0] = inter;
            // in base al tipo di file di uscita, scelgo l'estensione del file da passare all'interprete
            String estensione = ".blb";

            command[1] = new String(fileInf.substring(0, fileInf.indexOf(".inf")) + estensione);

            outputTextArea.append(command[0] + " " + command[1] + "\n");

            rt.exec(command); //Process proc = unused
            //String line=""; unused
            //String out=""; unused

            outputTextArea.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER2"));
        } catch (IOException e) {
            System.out.println("ERROR Run blb: " + e.getMessage());
        }
    }

    private String checkDefinitionCurrentFile(String entity) {
        String file = this.getCurrentFilename();
        String main = "";
        // check only if the file is an INF or h file
        if ((file.indexOf(".inf") != -1) || (file.indexOf(".INF") != -1)) {
            // open and reads the file
            try {
                main = JifDAO.read(new File(file));
                // Search for entity
                String pattern = "Object ";
                String hang = "";
                String tmp = "";
                String appoggio;
                int pos = 0;
                StringTokenizer sttok;

                while ((pos = Utils.IgnoreCaseIndexOf(main, pattern, pos)) >= 0) {
                    appoggio = main.substring(pos, main.indexOf("\n", pos));
                    int posizione_freccia = 0;
                    posizione_freccia = appoggio.lastIndexOf("->");
                    appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio, pattern));
                    appoggio = appoggio.trim();
                    if (appoggio.indexOf("!") == -1 && appoggio.equals("")) {
                        if (posizione_freccia == -1) {
                            posizione_freccia = 0;
                        } else {
                            posizione_freccia -= 3;
                        }

                        tmp = main.substring(pos + pattern.length() - 1 + posizione_freccia);
                        if (tmp.trim().startsWith("\"")) {
                            sttok = new StringTokenizer(tmp.trim(), "\"");
                        } else {
                            sttok = new StringTokenizer(tmp, " ;");
                        }
                        hang = sttok.nextToken();
                        //objectTree.addFile(new DefaultMutableTreeNode( new Inspect(hang,pos,pos+pattern.length()-1)));
                        if (hang.toLowerCase().equals(entity)) {
                            return file;
                        }
                    }
                    pos += pattern.length();
                }



                // ***************************************************
                pattern = "Global ";
                appoggio = "";
                pos = 0;
                while ((pos = Utils.IgnoreCaseIndexOf(main, pattern, pos)) >= 0) {
                    appoggio = main.substring(pos, main.indexOf("\n", pos));
                    appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio, pattern));
                    if (appoggio.indexOf("!") == -1 && appoggio.trim().equals("")) {
                        sttok = new StringTokenizer(main.substring(pos + pattern.length()), " ;=");
                        if (sttok.nextToken().toLowerCase().equals(entity)) {
                            return file;
                        }
                    }
                    pos += pattern.length();
                }
                // ***************************************************


                // ***************************************************
                pattern = "Constant ";
                pos = 0;
                while ((pos = Utils.IgnoreCaseIndexOf(main, pattern, pos)) >= 0) {
                    appoggio = main.substring(pos, main.indexOf("\n", pos));
                    appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio, pattern));
                    if (appoggio.indexOf("!") == -1 && appoggio.trim().equals("")) {
                        sttok = new StringTokenizer(main.substring(pos + pattern.length()), " ;=");
                        if (sttok.nextToken().toLowerCase().equals(entity)) {
                            return file;
                        }
                    }
                    pos += pattern.length();
                }
                // ***************************************************


                // ***************************************************
                pattern = "Sub";
                pos = 0;
                tmp = "";
                while ((pos = Utils.IgnoreCaseIndexOf(main, pattern, pos)) >= 0) {
                    appoggio = main.substring(pos, main.indexOf("\n", pos));
                    if (appoggio.indexOf("!") == -1 && appoggio.indexOf('[') >= 0 && appoggio.indexOf(';') >= 0) {
                        tmp = main.substring(0, pos);
                        tmp = tmp.substring(tmp.lastIndexOf('[') + 1);
                        tmp = tmp.trim();
                        if ((tmp + pattern).toLowerCase().equals(entity)) {
                            return file;
                        }
                    }
                    pos += pattern.length();
                }
                // ***************************************************

                // ***************************************************
                pattern = "Class ";
                pos = 0;
                while ((pos = Utils.IgnoreCaseIndexOf(main, pattern, pos)) >= 0) {
                    appoggio = main.substring(pos, main.indexOf("\n", pos));
                    appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio, pattern));
                    appoggio = appoggio.trim();
                    if (appoggio.indexOf("!") == -1 && appoggio.equals("")) {
                        sttok = new StringTokenizer(main.substring(pos + pattern.length()), " ;\n");
                        String nome = sttok.nextToken();
                        if (nome.toLowerCase().equals(entity)) {
                            return file;
                        }
                    }
                    pos += pattern.length();
                }
                // ***************************************************


                // ***************************************************
                // ****** Functions
                pattern = "[";
                pos = 0;
                //int lunghezza=0; unused
                tmp = "";
                while ((pos = main.indexOf(pattern, pos)) >= 0) {
                    appoggio = main.substring(pos, main.indexOf("\n", pos));
                    appoggio = appoggio.trim();
                    if (appoggio.indexOf("!") == -1 && appoggio.startsWith("[")) {
                        tmp = main.substring(pos);
                        tmp = tmp.substring(1, tmp.indexOf(';'));
                        tmp = tmp.trim();
                        if (!tmp.equals("") && (tmp.indexOf('\"') == -1) && (tmp.indexOf("Sub")) == -1) {
                            sttok = new StringTokenizer(tmp, " ;\n");
                            if (sttok.hasMoreTokens()) {
                                tmp = sttok.nextToken();
                            }
                            if (tmp.toLowerCase().equals(entity)) {
                                return file;
                            }
                        }
                    }
                    pos += pattern.length();
                }
                // ***************************************************

                // ***************************************************
                for (Iterator j = projectClasses.keySet().iterator(); j.hasNext();) {
                    pattern = (String) j.next();
//System.out.println("Classe ="+pattern);
                    pos = 0;
                    while ((pos = Utils.IgnoreCaseIndexOf(main, pattern, pos)) >= 0) {
                        appoggio = main.substring(pos, main.indexOf("\n", pos));
                        appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio, pattern));
                        appoggio = appoggio.trim();
                        if (appoggio.indexOf("!") == -1 && appoggio.equals("")) {
                            sttok = new StringTokenizer(main.substring(pos + pattern.length()), " ;\n");
                            String nome = sttok.nextToken();
                            if (nome.toLowerCase().equals(entity)) {
                                return file;
                            }
                        }
                        pos += pattern.length();
                    }
                }
                // ***************************************************


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // This method seeks for the definition of "entity" within the whole project
    private String checkDefinition(String entity) {

        // before I'll try the current filename
        String current = checkDefinitionCurrentFile(entity);
        if (current != null) {
            return current;
        }

        String file = "";
        String main = "";
        Vector files = project.getFiles();
        for (int i = 0; i < files.size(); i++) {
            file = ((JifFileName) files.elementAt(i)).getPath();

            // check only if the file is an INF or h file
            // and if isn't the current file
            if ((file.indexOf(".inf") != -1) || (file.indexOf(".INF") != -1) || !file.equals(current)) {
                // open and reads the file
                try {
                    main = JifDAO.read(new File(file));
//System.out.println("Cerco nel file="+file);
                    // Search for entity
                    String pattern = "Object ";
                    String hang = "";
                    String tmp = "";
                    String appoggio;
                    int pos = 0;
                    StringTokenizer sttok;

                    while ((pos = Utils.IgnoreCaseIndexOf(main, pattern, pos)) >= 0) {
                        appoggio = main.substring(pos, main.indexOf("\n", pos));
                        int posizione_freccia = 0;
                        posizione_freccia = appoggio.lastIndexOf("->");
                        appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio, pattern));
                        appoggio = appoggio.trim();
                        if (appoggio.indexOf("!") == -1 && appoggio.equals("")) {
                            if (posizione_freccia == -1) {
                                posizione_freccia = 0;
                            } else {
                                posizione_freccia -= 3;
                            }

                            tmp = main.substring(pos + pattern.length() - 1 + posizione_freccia);
                            if (tmp.trim().startsWith("\"")) {
                                sttok = new StringTokenizer(tmp.trim(), "\"");
                            } else {
                                sttok = new StringTokenizer(tmp, " ;");
                            }
                            hang = sttok.nextToken();
                            //objectTree.addFile(new DefaultMutableTreeNode( new Inspect(hang,pos,pos+pattern.length()-1)));
                            if (hang.toLowerCase().equals(entity)) {
                                return file;
                            }
                        }
                        pos += pattern.length();
                    }



                    // ***************************************************
                    pattern = "Global ";
                    appoggio = "";
                    pos = 0;
                    while ((pos = Utils.IgnoreCaseIndexOf(main, pattern, pos)) >= 0) {
                        appoggio = main.substring(pos, main.indexOf("\n", pos));
                        appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio, pattern));
                        if (appoggio.indexOf("!") == -1 && appoggio.trim().equals("")) {
                            sttok = new StringTokenizer(main.substring(pos + pattern.length()), " ;=");
                            if (sttok.nextToken().toLowerCase().equals(entity)) {
                                return file;
                            }
                        }
                        pos += pattern.length();
                    }
                    // ***************************************************


                    // ***************************************************
                    pattern = "Constant ";
                    pos = 0;
                    while ((pos = Utils.IgnoreCaseIndexOf(main, pattern, pos)) >= 0) {
                        appoggio = main.substring(pos, main.indexOf("\n", pos));
                        appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio, pattern));
                        if (appoggio.indexOf("!") == -1 && appoggio.trim().equals("")) {
                            sttok = new StringTokenizer(main.substring(pos + pattern.length()), " ;=");
                            if (sttok.nextToken().toLowerCase().equals(entity)) {
                                return file;
                            }
                        }
                        pos += pattern.length();
                    }
                    // ***************************************************


                    // ***************************************************
                    pattern = "Sub";
                    pos = 0;
                    tmp = "";
                    while ((pos = Utils.IgnoreCaseIndexOf(main, pattern, pos)) >= 0) {
                        appoggio = main.substring(pos, main.indexOf("\n", pos));
                        if (appoggio.indexOf("!") == -1 && appoggio.indexOf('[') >= 0 && appoggio.indexOf(';') >= 0) {
                            tmp = main.substring(0, pos);
                            tmp = tmp.substring(tmp.lastIndexOf('[') + 1);
                            tmp = tmp.trim();
                            if ((tmp + pattern).toLowerCase().equals(entity)) {
                                return file;
                            }
                        }
                        pos += pattern.length();
                    }
                    // ***************************************************

                    // ***************************************************
                    pattern = "Class ";
                    pos = 0;
                    while ((pos = Utils.IgnoreCaseIndexOf(main, pattern, pos)) >= 0) {
                        appoggio = main.substring(pos, main.indexOf("\n", pos));
                        appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio, pattern));
                        appoggio = appoggio.trim();
                        if (appoggio.indexOf("!") == -1 && appoggio.equals("")) {
                            sttok = new StringTokenizer(main.substring(pos + pattern.length()), " ;\n");
                            String nome = sttok.nextToken();
                            if (nome.toLowerCase().equals(entity)) {
                                return file;
                            }
                        }
                        pos += pattern.length();
                    }
                    // ***************************************************


                    // ***************************************************
                    // ****** Functions
                    pattern = "[";
                    pos = 0;
                    //int lunghezza=0; unused
                    tmp = "";
                    while ((pos = main.indexOf(pattern, pos)) >= 0) {
                        appoggio = main.substring(pos, main.indexOf("\n", pos));
                        appoggio = appoggio.trim();
                        if (appoggio.indexOf("!") == -1 && appoggio.startsWith("[")) {
                            tmp = main.substring(pos);
                            tmp = tmp.substring(1, tmp.indexOf(';'));
                            tmp = tmp.trim();
                            if (!tmp.equals("") && (tmp.indexOf('\"') == -1) && (tmp.indexOf("Sub")) == -1) {
                                sttok = new StringTokenizer(tmp, " ;\n");
                                if (sttok.hasMoreTokens()) {
                                    tmp = sttok.nextToken();
                                }
                                if (tmp.toLowerCase().equals(entity)) {
                                    return file;
                                }
                            }
                        }
                        pos += pattern.length();
                    }
                    // ***************************************************

                    // ***************************************************
                    for (Iterator j = projectClasses.keySet().iterator(); j.hasNext();) {
                        pattern = (String) j.next();
//System.out.println("Classe ="+pattern);
                        pos = 0;
                        while ((pos = Utils.IgnoreCaseIndexOf(main, pattern, pos)) >= 0) {
                            appoggio = main.substring(pos, main.indexOf("\n", pos));
                            appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio, pattern));
                            appoggio = appoggio.trim();
                            if (appoggio.indexOf("!") == -1 && appoggio.equals("")) {
                                sttok = new StringTokenizer(main.substring(pos + pattern.length()), " ;\n");
                                String nome = sttok.nextToken();
                                if (nome.toLowerCase().equals(entity)) {
                                    return file;
                                }
                            }
                            pos += pattern.length();
                        }
                    }
                    // ***************************************************


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private void symbolInsert() throws BadLocationException {
        MutableAttributeSet attr = new SimpleAttributeSet();
        if (config.getMappingLive() && config.getMapping().containsKey((String) symbolList.getSelectedValue())) {
            getCurrentDoc().insertString(getCurrentJifTextPane().getCaretPosition(),
                    (String) config.getMapping().get((String) symbolList.getSelectedValue()),
                    attr);
        } else {
            getCurrentDoc().insertString(getCurrentJifTextPane().getCaretPosition(),
                    (String) symbolList.getSelectedValue(),
                    attr);
        }
        symbolDialog.setVisible(false);
    }

    private void refreshDocuments() {
        // Apply color and font changes to open documents
        for (int i = 0; i < fileTabbedPane.getTabCount(); i++) {
            JScrollPane sp = (JScrollPane) fileTabbedPane.getComponent(i);
            JViewport vp = sp.getRowHeader();
            if (vp != null) {
                LineNumber ln = (LineNumber) vp.getView();
                ln.setFont(config.getContext().getFont());
            }
            JifTextPane tp = (JifTextPane) sp.getViewport().getComponent(0);
            tp.setBackground(config.getContext().getBackground());
            tp.setCaretColor(config.getContext().getForeground(InformSyntax.Normal));
        }
    }

    private void options() {
        // Build general tab from current settings
        openLastFileCheckBox.setSelected(config.getOpenLastFile());
        createNewFileCheckBox.setSelected(config.getCreateNewFile());
        mappingLiveCheckBox.setSelected(config.getMappingLive());
        helpedCodeCheckBox.setSelected(config.getHelpedCode());
        syntaxCheckBox.setSelected(config.getSyntaxHighlighting());
        numberLinesCheckBox.setSelected(config.getNumberLines());
        scanProjectFilesCheckBox.setSelected(config.getScanProjectFiles());
        wrapLinesCheckBox.setSelected(config.getWrapLines());
        projectOpenAllFilesCheckBox.setSelected(config.getOpenProjectFiles());
        makeResourceCheckBox.setSelected(config.getMakeResource());
        adventInLibCheckBox.setSelected(config.getAdventInLib());

        // Build colour and font tab from current settings
        optionContext.replaceStyles(config.getContext());
        createColorEditor();
        updateColor();
        updateFont();

        // Build highlight tab from current settings
        createHighlightEditor();
        updateHighlight();

        // Build path tabs from current settings
        updateOptionPaths();

        // Display options
        optionDialog.pack();
        optionDialog.setLocationRelativeTo(this);
        optionDialog.setVisible(true);
    }

    private void optionSave() {
        // Apply general tab settings
        config.setOpenLastFile(openLastFileCheckBox.isSelected());
        config.setCreateNewFile(createNewFileCheckBox.isSelected());
        config.setMappingLive(mappingLiveCheckBox.isSelected());
        config.setOpenProjectFiles(projectOpenAllFilesCheckBox.isSelected());
        config.setHelpedCode(helpedCodeCheckBox.isSelected());
        config.setNumberLines(numberLinesCheckBox.isSelected());
        config.setScanProjectFiles(scanProjectFilesCheckBox.isSelected());
        config.setSyntaxHighlighting(syntaxCheckBox.isSelected());
        config.setWrapLines(wrapLinesCheckBox.isSelected());

        // Apply tab size to Jif documents
        config.setTabSize(optionTabSize);
        JifEditorKit.setTabSize(optionTabSize);

        // Update the common syntax highlighting styles with color editor styles
        config.setContext(optionContext);

        // Apply changes to open documents
        refreshDocuments();

        // Apply compiler tab paths
        config.setGamePath(gamePathTextField.getText());
        config.setCompilerPath(compilerPathTextField.getText());
        config.setInterpreterZcodePath(interpreterPathTextField.getText());

        // Apply library tab paths
        config.setLibraryPath(libraryPathTextField.getText());
        config.setLibraryPath1(libraryPath1TextField.getText());
        config.setLibraryPath2(libraryPath2TextField.getText());
        config.setLibraryPath3(libraryPath3TextField.getText());

        // Apply Glulx tab paths
        config.setInterpreterGlulxPath(glulxPathTextField.getText());
        config.setBresPath(bresPathTextField.getText());
        config.setBlcPath(blcPathTextField.getText());

        optionDialog.setVisible(false);

        try {
            JifConfigurationDAO.store(config);
        } catch (JifConfigurationException ex) {
            ex.printStackTrace();
            System.out.println("ERROR Option save: " + ex.getMessage());
        }
    }

    private void projectPropertiesSave() {
        // save the current project and reload the switches
        File file = new File(project.getFile().getPath());
        try {
            JifDAO.save(file, projectPropertiesTextArea.getText());
            JifProjectDAO.reload(project);

            // Set mode to project setting
            if (project.getInformMode()) {
                setInformMode();
            } else {
                setGlulxMode();
            }

            JOptionPane.showMessageDialog(projectPropertiesDialog,
                    java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE1"),
                    java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE2"),
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            System.out.println("ERROR Project properties save: " + ex.getMessage());
        }
    }

    private void projectSwitches() {
        projectSwitchesPanel.removeAll();
        for (Iterator i = project.getSwitches().keySet().iterator(); i.hasNext();) {
            String switchName = (String) i.next();
            String setting = (String) project.getSwitches().get(switchName);
            Checkbox check = new Checkbox(switchName);
            check.setFont(new Font("Monospaced", Font.PLAIN, 11));
            check.setState(setting.equals("on") ? true : false);
            projectSwitchesPanel.add(check);
        }
        projectSwitchesDialog.pack();
        projectSwitchesDialog.setLocationRelativeTo(this);
        projectSwitchesDialog.setVisible(true);
        projectSwitchesDialog.setTitle("Project Switches");
    }

    private void projectSwitchesSave() {
        for (int i = 0; i < projectSwitchesPanel.getComponentCount(); i++) {
            Checkbox ch = (Checkbox) projectSwitchesPanel.getComponent(i);
            project.setSwitch(ch.getLabel(), (ch.getState()) ? "on" : "off");
        }
        try {
            JifProjectDAO.store(project);
        } catch (JifProjectException ex) {
            System.out.println("ERROR Save project switches:" + ex.getMessage());
        } finally {
            projectSwitchesDialog.setVisible(false);
        }
    }

    private void configPropertiesSave() {
        // Saving file
        File file = new File(config.getFile().getPath());
        try {
            JifDAO.save(file, configTextArea.getText());
            JifConfigurationDAO.reload(config);
            if (project.getFile() == null) {
                if (config.getInformMode()) {
                    setInformMode();
                } else {
                    setGlulxMode();
                }
            }
            refreshDocuments();
            JOptionPane.showMessageDialog(configDialog,
                    java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE1"),
                    java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE2"),
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            System.out.println("ERROR Save configuration: " + e.getMessage());
        }
    }

    private void configSwitches() {
        switchesLowerPanel.removeAll();
        switchesUpperPanel.removeAll();
        for (Iterator i = config.getSwitchesSet().iterator(); i.hasNext();) {
            String switchName = (String) i.next();
            String setting = (String) config.getSwitches().get(switchName);
            Checkbox check = new Checkbox(switchName);
            check.setFont(new Font("Monospaced", Font.PLAIN, 11));
            check.setState(setting.equals("on") ? true : false);
            if (switchName.length() < 4) {
                switchesUpperPanel.add(check);
            } else {
                switchesLowerPanel.add(check);
            }
        }
        switchesDialog.pack();
        switchesDialog.setLocationRelativeTo(this);
        switchesDialog.setVisible(true);
    }

    private void configSwitchesSave() {
        for (int i = 0; i < switchesUpperPanel.getComponentCount(); i++) {
            Checkbox ch = (Checkbox) switchesUpperPanel.getComponent(i);
            config.setSwitch(ch.getLabel(), (ch.getState()) ? "on" : "off");
        }
        for (int i = 0; i < switchesLowerPanel.getComponentCount(); i++) {
            Checkbox ch = (Checkbox) switchesLowerPanel.getComponent(i);
            config.setSwitch(ch.getLabel(), (ch.getState()) ? "on" : "off");
        }
        try {
            JifConfigurationDAO.store(config);
        } catch (JifConfigurationException ex) {
            System.out.println("ERROR Save configuration switches:" + ex.getMessage());
        } finally {
            switchesDialog.setVisible(false);
        }
    }

    /**
     * If workingpath+testpath exists this method returns workingpath+testpath
     * else will return the testpath
     */
    public String resolveAbsolutePath(String workingDirectory, String testpath) {
        File f = new File(workingDirectory + testpath);
        if (f.exists()) {
            return workingDirectory + testpath;
        } else {
            return testpath;
        }
    }

    // --- JifConfigurationObserver implementation -----------------------------
    public void updateConfiguration() {
        updateLastProject();
        updateRecentFiles();
        updateMenues();
        updateInformGlulxMode();
    }

    // --- JifProjectObserver implementation -----------------------------------
    private void updateInformGlulxMode() {
        if (informModeCheckBoxMenuItem.getState()) {
            setInformMode();
            if (project.getFile() == null) {
                config.setInformMode(informModeCheckBoxMenuItem.getState());
            } else {
                config.setInformMode(informModeCheckBoxMenuItem.getState());
                project.setInformMode(informModeCheckBoxMenuItem.getState());
            }
        } else {
            setGlulxMode();
            if (project.getFile() == null) {
                config.setInformMode(false);
            } else {
                config.setInformMode(false);
                project.setInformMode(false);
            }
        }
    }

    public void updateProject() {
        updateProjectTitle();
        updateProjectList();
        updateProjectMain();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel aboutControlPanel;
    private javax.swing.JDialog aboutDialog;
    private javax.swing.JLabel aboutLabel;
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JButton aboutOKButton;
    private javax.swing.JTabbedPane aboutTabbedPane;
    private javax.swing.JMenuItem addFileToProjectMenuItem;
    private javax.swing.JMenuItem addFileToProjectPopupMenuItem;
    private javax.swing.JMenuItem addNewToProjectMenuItem;
    private javax.swing.JMenuItem addNewToProjectPopupMenuItem;
    private javax.swing.JCheckBox adventInLibCheckBox;
    private javax.swing.JButton attributeColorButton;
    private javax.swing.JLabel attributeColorLabel;
    private javax.swing.JPanel attributeColorjPanel;
    private javax.swing.JButton backgroundColorButton;
    private javax.swing.JLabel backgroundColorLabel;
    private javax.swing.JPanel backgroundColorPanel;
    private javax.swing.JButton blcPathButton;
    private javax.swing.JLabel blcPathLabel;
    private javax.swing.JPanel blcPathPanel;
    private javax.swing.JTextField blcPathTextField;
    private javax.swing.JButton bookmarkColorButton;
    private javax.swing.JLabel bookmarkColorLabel;
    private javax.swing.JPanel bookmarkColorPanel;
    private javax.swing.JButton bracketCheckButton;
    private javax.swing.JButton bracketColorButton;
    private javax.swing.JLabel bracketColorLabel;
    private javax.swing.JPanel bracketColorPanel;
    private javax.swing.JButton bresPathButton;
    private javax.swing.JLabel bresPathLabel;
    private javax.swing.JPanel bresPathPanel;
    private javax.swing.JTextField bresPathTextField;
    private javax.swing.JButton buildAllButton;
    private javax.swing.JMenuItem buildAllGlulxMenuItem;
    private javax.swing.JMenuItem buildAllMenuItem;
    private javax.swing.JMenu buildMenu;
    private javax.swing.JMenuItem changelogMenuItem;
    private javax.swing.JMenuItem clearAllMenuItem;
    private javax.swing.JMenuItem clearPopupMenuItem;
    private javax.swing.JMenuItem clearRecentFilesMenuItem;
    private javax.swing.JButton closeAllButton;
    private javax.swing.JMenuItem closeAllMenuItem;
    private javax.swing.JMenuItem closeAllPopupMenuItem;
    private javax.swing.JButton closeButton;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenuItem closePopupMenuItem;
    private javax.swing.JMenuItem closeProjectMenuItem;
    private javax.swing.JMenuItem closeProjectPopupMenuItem;
    private javax.swing.JEditorPane colorEditorPane;
    private javax.swing.JPanel colorFontPanel;
    private javax.swing.JPanel colorHighlightPanel;
    private javax.swing.JPanel colorPanel;
    private javax.swing.JButton commentButton;
    private javax.swing.JButton commentColorButton;
    private javax.swing.JLabel commentColorLabel;
    private javax.swing.JPanel commentColorPanel;
    private javax.swing.JMenuItem commentSelectionMenuItem;
    private javax.swing.JMenuItem compileMenuItem;
    private javax.swing.JButton compilerPathButton;
    private javax.swing.JLabel compilerPathLabel;
    private javax.swing.JPanel compilerPathPanel;
    private javax.swing.JTextField compilerPathTextField;
    private javax.swing.JPanel complierPanel;
    private javax.swing.JButton configCloseButton;
    private javax.swing.JPanel configControlPanel;
    private javax.swing.JDialog configDialog;
    private javax.swing.JMenuItem configFileMenuItem;
    private javax.swing.JLabel configLabel;
    private javax.swing.JPanel configLabelPanel;
    private javax.swing.JButton configSaveButton;
    private javax.swing.JScrollPane configScrollPane;
    private javax.swing.JTextArea configTextArea;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem copyPopupMenuItem;
    private javax.swing.JCheckBox createNewFileCheckBox;
    private javax.swing.JScrollPane creditsScrollPane;
    private javax.swing.JTextArea creditsTextArea;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem cutPopupMenuItem;
    private javax.swing.JPanel defaultColorPanel;
    private javax.swing.JButton defaultDarkColorButton;
    private javax.swing.JLabel defaultDarkColorLabel;
    private javax.swing.JPanel defaultDarkColorPanel;
    private javax.swing.JButton defaultDarkHighlightButton;
    private javax.swing.JLabel defaultDarkHighlightLabel;
    private javax.swing.JPanel defaultDarkhighlightPanel;
    private javax.swing.JPanel defaultHighlightPanel;
    private javax.swing.JButton defaultLightColorButton;
    private javax.swing.JLabel defaultLightColorLabel;
    private javax.swing.JPanel defaultLightColorPanel;
    private javax.swing.JButton defaultLightHighlightButton;
    private javax.swing.JLabel defaultLightHighlightLabel;
    private javax.swing.JPanel defaultLightHighlightPanel;
    private javax.swing.JButton definitionButton;
    private javax.swing.JPanel definitionPanel;
    private javax.swing.JTextField definitionTextField;
    private javax.swing.JMenu editMenu;
    private javax.swing.JButton errorColorButton;
    private javax.swing.JLabel errorColorLabel;
    private javax.swing.JPanel errorColorPanel;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenuItem extractStringsMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JPanel filePanel;
    public javax.swing.JPopupMenu filePopupMenu;
    private static final javax.swing.JTabbedPane fileTabbedPane = new javax.swing.JTabbedPane();
    private javax.swing.JButton findButton;
    public javax.swing.JTextField findTextField;
    private javax.swing.JLabel fontLabel;
    private javax.swing.JComboBox fontNameComboBox;
    private javax.swing.JPanel fontPanel;
    private javax.swing.JComboBox fontSizeComboBox;
    private javax.swing.JButton gamePathButton;
    private javax.swing.JLabel gamePathLabel;
    private javax.swing.JPanel gamePathPanel;
    private javax.swing.JTextField gamePathTextField;
    private javax.swing.JMenuItem garbageCollectionMenuItem;
    private javax.swing.JPanel generalPanel;
    private javax.swing.JMenu glulxMenu;
    private javax.swing.JCheckBoxMenuItem glulxModeCheckBoxMenuItem;
    private javax.swing.JPanel glulxPanel;
    private javax.swing.JButton glulxPathButton;
    private javax.swing.JLabel glulxPathLabel;
    private javax.swing.JPanel glulxPathPanel;
    private javax.swing.JTextField glulxPathTextField;
    private javax.swing.JMenu helpMenu;
    public javax.swing.JCheckBox helpedCodeCheckBox;
    private javax.swing.JEditorPane highlightEditorPane;
    private javax.swing.JPanel highlightPanel;
    private javax.swing.JComboBox highlightSelectedComboBox;
    private javax.swing.JLabel highlightSelectedLabel;
    private javax.swing.JPanel highlightSelectedPanel;
    private javax.swing.JButton infoCloseButton;
    private javax.swing.JPanel infoControlPanel;
    private javax.swing.JDialog infoDialog;
    private javax.swing.JScrollPane infoScrollPane;
    private javax.swing.JTextArea infoTextArea;
    private javax.swing.JCheckBoxMenuItem informModeCheckBoxMenuItem;
    private javax.swing.JMenuItem insertFileMenuItem;
    private javax.swing.JMenuItem insertFilePopupMenuItem;
    private javax.swing.JMenu insertNewMenu;
    private javax.swing.JButton insertSymbolButton;
    private javax.swing.JMenuItem insertSymbolMenuItem;
    private javax.swing.JMenuItem insertSymbolPopupMenuItem;
    private javax.swing.JButton interpreterButton;
    private javax.swing.JButton interpreterPathButton;
    private javax.swing.JLabel interpreterPathLabel;
    private javax.swing.JPanel interpreterPathPanel;
    private javax.swing.JTextField interpreterPathTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator13;
    private javax.swing.JSeparator jSeparator14;
    private javax.swing.JSeparator jSeparator15;
    private javax.swing.JSeparator jSeparator16;
    private javax.swing.JSeparator jSeparator17;
    private javax.swing.JSeparator jSeparator18;
    private javax.swing.JSeparator jSeparator19;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private static final javax.swing.JToolBar jToolBarCommon = new javax.swing.JToolBar();
    private javax.swing.JButton jumpToColorButton;
    private javax.swing.JLabel jumpToColorLabel;
    private javax.swing.JPanel jumpToColorPanel;
    private javax.swing.JMenuItem jumpToSourceMenuItem;
    private javax.swing.JButton keywordColorButton;
    private javax.swing.JLabel keywordColorLabel;
    private javax.swing.JPanel keywordColorPanel;
    private javax.swing.JMenuItem lastProjectMenuItem;
    private javax.swing.JTabbedPane leftTabbedPane;
    private javax.swing.JPanel libraryPanel;
    private javax.swing.JButton libraryPath1Button;
    private javax.swing.JLabel libraryPath1Label;
    private javax.swing.JPanel libraryPath1Panel;
    private javax.swing.JTextField libraryPath1TextField;
    private javax.swing.JButton libraryPath2Button;
    private javax.swing.JLabel libraryPath2Label;
    private javax.swing.JPanel libraryPath2Panel;
    private javax.swing.JTextField libraryPath2TextField;
    private javax.swing.JButton libraryPath3Button;
    private javax.swing.JLabel libraryPath3Label;
    private javax.swing.JPanel libraryPath3Panel;
    private javax.swing.JTextField libraryPath3TextField;
    private javax.swing.JButton libraryPathButton;
    private javax.swing.JLabel libraryPathLabel;
    private javax.swing.JPanel libraryPathPanel;
    private javax.swing.JTextField libraryPathTextField;
    private javax.swing.JLabel mainFileLabel;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JMenuItem makeBlbMenuItem;
    private javax.swing.JCheckBox makeResourceCheckBox;
    private javax.swing.JMenuItem makeResourceMenuItem;
    public javax.swing.JCheckBox mappingLiveCheckBox;
    private javax.swing.JMenu modeMenu;
    private javax.swing.JButton newButton;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JMenuItem newProjectMenuItem;
    private javax.swing.JMenuItem newProjectPopupMenuItem;
    private javax.swing.JMenuItem nextBookmarkMenuItem;
    private javax.swing.JButton normalColorButton;
    private javax.swing.JLabel normalColorLabel;
    private javax.swing.JPanel normalColorPanel;
    private javax.swing.JButton numberColorButton;
    private javax.swing.JLabel numberColorLabel;
    private javax.swing.JPanel numberColorPanel;
    private javax.swing.JCheckBox numberLinesCheckBox;
    private javax.swing.JButton openButton;
    private javax.swing.JCheckBox openLastFileCheckBox;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem openProjectMenuItem;
    private javax.swing.JMenuItem openProjectPopupMenuItem;
    private javax.swing.JMenuItem openSelectedFilesPopupMenuItem;
    private javax.swing.JButton optionCancelButton;
    private javax.swing.JPanel optionControlPanel;
    private javax.swing.JButton optionDefaultButton;
    public javax.swing.JDialog optionDialog;
    private javax.swing.JButton optionSaveButton;
    private javax.swing.JTabbedPane optionTabbedPane;
    private javax.swing.JMenu optionsMenu;
    private javax.swing.JCheckBoxMenuItem outputCheckBoxMenuItem;
    private javax.swing.JScrollPane outputScrollPane;
    private static final javax.swing.JTabbedPane outputTabbedPane = new javax.swing.JTabbedPane();
    public javax.swing.JTextArea outputTextArea;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenu pastePopupMenu;
    private javax.swing.JMenuItem printMenuItem;
    private javax.swing.JMenuItem printPopupMenuItem;
    private javax.swing.JList projectList;
    private javax.swing.JMenu projectMenu;
    private javax.swing.JCheckBox projectOpenAllFilesCheckBox;
    private javax.swing.JPanel projectPanel;
    private javax.swing.JPopupMenu projectPopupMenu;
    private javax.swing.JButton projectPropertiesCloseButton;
    private javax.swing.JPanel projectPropertiesControlPanel;
    private javax.swing.JDialog projectPropertiesDialog;
    private javax.swing.JMenuItem projectPropertiesMenuItem;
    private javax.swing.JButton projectPropertiesSaveButton;
    private javax.swing.JScrollPane projectPropertiesScrollPane;
    private javax.swing.JTextArea projectPropertiesTextArea;
    private javax.swing.JScrollPane projectScrollPane;
    private javax.swing.JButton projectSwitchesCloseButton;
    private javax.swing.JPanel projectSwitchesControlPanel;
    private javax.swing.JDialog projectSwitchesDialog;
    private javax.swing.JMenuItem projectSwitchesMenuItem;
    private javax.swing.JPanel projectSwitchesPanel;
    private javax.swing.JButton projectSwitchesSaveButton;
    private javax.swing.JButton propertyColorButton;
    private javax.swing.JLabel propertyColorLabel;
    private javax.swing.JPanel propertyColorPanel;
    private javax.swing.JMenuItem readMeMenuItem;
    private javax.swing.JMenu recentFilesMenu;
    private javax.swing.JButton redoButton;
    private javax.swing.JMenuItem removeFromProjectMenuItem;
    private javax.swing.JMenuItem removeFromProjectPopupMenuItem;
    private javax.swing.JMenuItem removeMainPopupMenuItem;
    private javax.swing.JButton replaceAllButton;
    private javax.swing.JButton replaceButton;
    private javax.swing.JButton replaceCloseButton;
    private javax.swing.JPanel replaceControlPanel;
    private javax.swing.JDialog replaceDialog;
    private javax.swing.JButton replaceFindButton;
    private javax.swing.JLabel replaceFindLabel;
    private javax.swing.JTextField replaceFindTextField;
    private javax.swing.JMenuItem replaceMenuItem;
    private javax.swing.JPanel replacePanel;
    private javax.swing.JButton replaceReplaceButton;
    private javax.swing.JLabel replaceReplaceLabel;
    private javax.swing.JTextField replaceReplaceTextField;
    protected javax.swing.JTextField rowColTextField;
    private javax.swing.JMenuItem runBlbMenuItem;
    private javax.swing.JButton runButton;
    private javax.swing.JMenuItem runMenuItem;
    private javax.swing.JMenuItem runUlxMenuItem;
    private javax.swing.JButton saveAllButton;
    private javax.swing.JMenuItem saveAllMenuItem;
    private javax.swing.JButton saveAsButton;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JButton saveButton;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenuItem saveProjectMenuItem;
    private javax.swing.JMenuItem saveProjectPopupMenuItem;
    private javax.swing.JCheckBox scanProjectFilesCheckBox;
    private javax.swing.JMenuItem searchAllMenuItem;
    private javax.swing.JMenuItem searchMenuItem;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JButton searchProjectButton;
    private javax.swing.JPanel searchProjectPanel;
    public javax.swing.JTextField searchProjectTextField;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.JMenuItem setBookmarkMenuItem;
    private javax.swing.JMenuItem setMainPopupMenuItem;
    private javax.swing.JButton settingsButton;
    private javax.swing.JMenuItem settingsMenuItem;
    private javax.swing.JButton stringColorButton;
    private javax.swing.JLabel stringColorLabel;
    private javax.swing.JPanel stringColorPanel;
    private javax.swing.JButton switchManagerButton;
    private javax.swing.JButton switchesCloseButton;
    private javax.swing.JPanel switchesControlPanel;
    private javax.swing.JDialog switchesDialog;
    private javax.swing.JPanel switchesLowerPanel;
    private javax.swing.JMenuItem switchesMenuItem;
    private javax.swing.JPanel switchesPanel;
    private javax.swing.JButton switchesSaveButton;
    private javax.swing.JPanel switchesUpperPanel;
    private javax.swing.JDialog symbolDialog;
    private javax.swing.JList symbolList;
    private javax.swing.JScrollPane symbolScrollPane;
    public javax.swing.JCheckBox syntaxCheckBox;
    private javax.swing.JButton tabLeftButton;
    private javax.swing.JMenuItem tabLeftMenuItem;
    private javax.swing.JButton tabRightButton;
    private javax.swing.JMenuItem tabRightMenuItem;
    private javax.swing.JLabel tabSizeLabel;
    private javax.swing.JPanel tabSizePanel;
    private javax.swing.JTextField tabSizeTextField;
    private javax.swing.JButton textCloseButton;
    private javax.swing.JPanel textControlPanel;
    private javax.swing.JDialog textDialog;
    private javax.swing.JLabel textLabel;
    private javax.swing.JScrollPane textScrollPane;
    private javax.swing.JTextArea textTextArea;
    private javax.swing.JCheckBoxMenuItem toggleFullscreenCheckBoxMenuItem;
    private javax.swing.JCheckBoxMenuItem toolbarCheckBoxMenuItem;
    private javax.swing.JPanel toolbarPanel;
    private javax.swing.JMenuItem translateMenuItem;
    private javax.swing.JCheckBoxMenuItem treeCheckBoxMenuItem;
    private javax.swing.JPanel treePanel;
    private javax.swing.JScrollPane treeScrollPane;
    private static javax.swing.JTree treeTree;
    private javax.swing.JPanel tutorialControlPanel;
    private javax.swing.JDialog tutorialDialog;
    private javax.swing.JEditorPane tutorialEditorPane;
    private javax.swing.JLabel tutorialLabel;
    private javax.swing.JButton tutorialOKButton;
    private javax.swing.JButton tutorialPrintButton;
    private javax.swing.JScrollPane tutorialScrollPane;
    private javax.swing.JButton uncommentButton;
    private javax.swing.JMenuItem uncommentSelectionMenuItem;
    private javax.swing.JButton undoButton;
    private javax.swing.JSplitPane upperSplitPane;
    private javax.swing.JButton verbColorButton;
    private javax.swing.JLabel verbColorLabel;
    private javax.swing.JPanel verbColorPanel;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JButton warningColorButton;
    private javax.swing.JLabel warningColorLabel;
    private javax.swing.JPanel warningColorPanel;
    private javax.swing.JButton wordColorButton;
    private javax.swing.JLabel wordColorLabel;
    private javax.swing.JPanel wordColorPanel;
    public javax.swing.JCheckBox wrapLinesCheckBox;
    // End of variables declaration//GEN-END:variables
    private MouseListener popupListenerProject = new PopupListenerProject();
    private MouseListener menuListener = new MenuListener();
    // Configuration
    JifConfiguration config;
    // gestione albero INSPECT
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode top;
    private DefaultMutableTreeNode classTree;
    private DefaultMutableTreeNode constantTree;
    private DefaultMutableTreeNode functionTree;
    private DefaultMutableTreeNode globalTree;
    private DefaultMutableTreeNode objectTree;
    // Output pane highlighters
    private HighlightText hlighterOutputErrors;
    private HighlightText hlighterOutputWarnings;
    // Tree regular expressions
    private Pattern classPattern = Pattern.compile("(?:^|;)(?:\\s|(?:!.*\n))*\n+\\s*(Class)\\s+(\\w+)",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    private Pattern classToClassPattern = Pattern.compile("\n+\\s*Class\\s+(\\w+)(?:\\s|,|(?:\\(.+\\)))+class\\s+(\\w+)",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    private Pattern constantPattern = Pattern.compile("\n+\\s*Constant\\s+(\\w+)",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    private Pattern functionPattern = Pattern.compile(";(?:\\s|(?:!.*\n))*\n+\\s*\\[\\s*(\\w+)",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    private Pattern globalPattern = Pattern.compile("\n+\\s*Global\\s+(\\w+)",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    private Pattern objectPattern = Pattern.compile("\n+\\s*Object\\s+(->\\s+)*(\\w+)",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    // per scegliere l'estensione del file da passare all'interprete
    private String tipoz = "";
    // New Files name counter
    private int countNewFile = 0;
    // titolo di JIF, serve per aggiungerci il nome del progetto aperto
    private String jifVersion = Constants.JIFVERSION;
    // Option dialog colours, font and tab size
    private InformContext optionContext = new InformContext();
    private int optionTabSize = 4;
    private HighlightText optionHighlight = null;
    // Project
    private JifProject project = new JifProject();
    // String name to Inspect
    private Map<String, Inspect> projectClasses = new TreeMap<String, Inspect>();
    // String name to DefaultMutableTreeNode
    private Map<String, DefaultMutableTreeNode> projectNodes = new TreeMap<String, DefaultMutableTreeNode>();
    // String name child to string parent
    private Map<String, String> projectRels = new TreeMap<String, String>();
    // alphabetical sorting
    private Vector<Inspect> objTree;

    // --- Accessor methods ----------------------------------------------------
    public Map getAltkeys() {
        return config.getAltKeys();
    }

    public Map getExecuteCommands() {
        return config.getExecuteCommands();
    }

    public JTabbedPane getFileTabbed() {
        return fileTabbedPane;
    }

    public Map getHelpCode() {
        return config.getHelpCodes();
    }

    public String getJifVersion() {
        return jifVersion;
    }

    public void setJifVersion(String jifVersion) {
        this.jifVersion = jifVersion;
    }

    public JDialog getSymbolDialog() {
        return symbolDialog;
    }

    public Map getMapping() {
        return config.getMapping();
    }

    public Map getOperations() {
        return config.getOperations();
    }

    public JCheckBoxMenuItem getOutputCheckBox() {
        return outputCheckBoxMenuItem;
    }

    public JTabbedPane getOutputTabbed() {
        return outputTabbedPane;
    }

    public InformContext getInformContext() {
        return config.getContext();
    }

    public boolean isInformMode() {
        return (project.getFile() == null)
                ? config.getInformMode()
                : project.getInformMode();
    }

    public boolean isGlulxMode() {
        return (project.getFile() == null)
                ? !config.getInformMode()
                : !project.getInformMode();
    }

    public Map getSwitches() {
        return (project.getFile() == null)
                ? config.getSwitches()
                : project.getSwitches();
    }
}
