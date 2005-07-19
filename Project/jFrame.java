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
 * Copyright (C) 2004  Alessandro Schillaci
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

import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
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

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;


/*
 * jFrame.java
 *
 * Created on 28 luglio 2003, 9.58
 */


/** Main Class for Jif application.
 * Jif is a Java Editor for Inform
 * @author Alessandro Schillaci
 * @version 1.1f
 */
public class jFrame extends JFrame {

    /** Creates new form jFrame */
    public jFrame(String dir) {

    // Screen Resolution
    screensize = Toolkit.getDefaultToolkit().getScreenSize();
    //Debug mode only if passed to JIF
    if (System.getProperties().containsKey("debugmode")){

        // Log file
        try{
            System.out.println("Debug Mode is ON...");
            System.setErr(new PrintStream(new FileOutputStream("err.txt")));
            System.err.println("===== JIF ERROR LOG FILE =====");
            System.err.println("System Information:\n");
            Properties props = System.getProperties();

            // Enumerate all system properties
            Enumeration enumerator = props.propertyNames();
            while(enumerator.hasMoreElements()) {
                // Get property name
                String propName = (String)enumerator.nextElement();
                // Get property value
                String propValue = (String)props.get(propName);
                System.err.println(propName+"="+propValue);
            }

            System.err.println("\n");
            System.err.println("JIF version "+ Constants.JIFVERSION + "\n");
            System.err.println("Start ERROR loggin on "+ new Date()+"\n");
        } catch(FileNotFoundException e){
            e.printStackTrace();
        }
    }

    // JIF's icon
    setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/Jif.png")));

    if (null!=dir && (!dir.equals(""))){
        workingDir = dir;
        System.out.println("Working directory set to "+workingDir);
    }

    // show ToolTip velocity
    ToolTipManager.sharedInstance().setInitialDelay(1000);

    // initialization of graphic components
    initComponents();
    disableComponents();

    jListSymbols = new JList();

    // loading configuration
    loadConfig();

    currentProject = workingDir+"projects"+Constants.SEP+"default.jpf";
    projectFiles = new Vector();

    updateProjectTitle("Project: "+
    currentProject.substring(currentProject.lastIndexOf(Constants.SEP)+1,currentProject.length()));

    // Events management in the symbols list
    JWindowSymbols = new JFrame();
    JWindowSymbols.setResizable(false);
    //JWindowSymbols.setName(java.util.ResourceBundle.getBundle("JIF").getString("STR_SYMBOLS"));
    //JWindowSymbols.setTitle(java.util.ResourceBundle.getBundle("JIF").getString("STR_SYMBOLS"));
    JWindowSymbols.setUndecorated(true);

    jListSymbols.addKeyListener(new java.awt.event.KeyAdapter(){
        public void keyPressed(java.awt.event.KeyEvent ke){
            if ((ke.getKeyCode()==ke.VK_ENTER)){
                try{
                    if (jCheckBoxMappingLive.isSelected()&&mapping.containsKey((String)jListSymbols.getSelectedValue())){
                      getCurrentDoc().insertString(getCurrentJIFTextPane().getCaretPosition(), (String)mapping.get((String)jListSymbols.getSelectedValue()), attr);
                    }
                    else getCurrentDoc().insertString(getCurrentJIFTextPane().getCaretPosition(), (String)jListSymbols.getSelectedValue() , attr);
                    JWindowSymbols.hide();
                } catch(BadLocationException e){
                    System.out.println(e.getMessage());
                    System.err.println(e.getMessage());
                }
            }
            // ESC key
            if ((ke.getKeyCode()==ke.VK_ESCAPE)){
                    JWindowSymbols.hide();
            }
        }
    });

    jListSymbols.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent evt) {
            JWindowSymbols.setTitle(java.util.ResourceBundle.getBundle("JIF").getString("JWINDOW_TOOLTIP"));
        }

        public void mouseExited(java.awt.event.MouseEvent evt) {
            JWindowSymbols.setTitle(java.util.ResourceBundle.getBundle("JIF").getString("STR_SYMBOLS"));
        }

        public void mouseClicked(java.awt.event.MouseEvent evt) {
            if (evt.getClickCount()==2){
                try{
                    if (jCheckBoxMappingLive.isSelected()&&mapping.containsKey((String)jListSymbols.getSelectedValue())){
                        getCurrentDoc().insertString(getCurrentJIFTextPane().getCaretPosition(), (String)mapping.get((String)jListSymbols.getSelectedValue()), attr);
                    }
                    else getCurrentDoc().insertString(getCurrentJIFTextPane().getCaretPosition(), (String)jListSymbols.getSelectedValue() , attr);
                    JWindowSymbols.hide();
                } catch(BadLocationException e){
                    System.out.println(e.getMessage());
                    System.err.println(e.getMessage());
                }
            }
        }
    });

    JScrollPane jsp1 = new JScrollPane();
    jsp1.setPreferredSize(new java.awt.Dimension(30, 30));
    jsp1.setMinimumSize(new java.awt.Dimension(0, 0));
    jsp1.setViewportView(jListSymbols);
    JWindowSymbols.getContentPane().add(jsp1);
    JWindowSymbols.toFront();
    // END Events management in the symbols list




    attr = new SimpleAttributeSet();

    // creates a hlighterOutput for the Output Windows
    hlighterOutput = new HighlightText(Color.pink);

    // load JIF ini
    loadJifIni();
    //new SplashWindow(this);

    // Opens the last file opened
    if (jCheckBoxOpenLastFile.isSelected() && (null!=lastFile && !lastFile.equals("null"))){
            // JIF opens the file only if exists
            File test = new File(lastFile);
            if (test.exists()){
                openFile(lastFile);
            }
    }

    //  Creates a new file when JIF is loaded
    if (jCheckBoxCreateNewFile.isSelected()){
        newAdventure();
    }
}



    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPopupMenu1 = new javax.swing.JPopupMenu();
        jMenuInsertNew = new javax.swing.JMenu();
        jMenuItemInsertSymbol = new javax.swing.JMenuItem();
        jMenuItemInsertFromFile = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        jMenuItemCut1 = new javax.swing.JMenuItem();
        jMenuItemCopy = new javax.swing.JMenuItem();
        jMenuPaste = new javax.swing.JMenu();
        jMenuItemClear = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JSeparator();
        jMenuItemPrint1 = new javax.swing.JMenuItem();
        jMenuItemPopupClose = new javax.swing.JMenuItem();
        jMenuItemPopupCloseAllFiles = new javax.swing.JMenuItem();
        jMenuItemJumpToSource = new javax.swing.JMenuItem();
        jDialogAbout = new JDialog (this, "", true);
        jLabel7 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jButton9 = new javax.swing.JButton();
        jDialogConfigFiles = new JDialog (this, "", false);
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextAreaConfig = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jDialogSwitches = new JDialog (this, "", false);
        jPanel6 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        jPanelSwitch1 = new javax.swing.JPanel();
        jPanelSwitch2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jDialogText = new JDialog (this, "", false);
        jPanel8 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTextArea4 = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        jDialogReplace = new javax.swing.JDialog();
        jPanel12 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldReplaceFind = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldReplace = new javax.swing.JTextField();
        jPanel20 = new javax.swing.JPanel();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jPanel23 = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jPopupMenuProject = new javax.swing.JPopupMenu();
        jMenuItemPopupNewProject = new javax.swing.JMenuItem();
        jMenuItemPopupOpenProject = new javax.swing.JMenuItem();
        jMenuItemPopupSaveProject = new javax.swing.JMenuItem();
        jMenuItemPopupCloseProject = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        jMenuItemPopupAddNewToProject = new javax.swing.JMenuItem();
        jMenuItemPopupAddToProject = new javax.swing.JMenuItem();
        jMenuItemPopupRemoveFromProject = new javax.swing.JMenuItem();
        jMenuItemPopupOpenSelectedFiles = new javax.swing.JMenuItem();
        jSeparator19 = new javax.swing.JSeparator();
        jMenuItemSetMainClass = new javax.swing.JMenuItem();
        jMenuItemRemoveMainClass = new javax.swing.JMenuItem();
        jDialogOption = new JDialog (this, "", false);
        jTabbedPaneOption = new javax.swing.JTabbedPane();
        jPanelGeneral = new javax.swing.JPanel();
        jPanel36 = new javax.swing.JPanel();
        jPanel42 = new javax.swing.JPanel();
        jTextFieldMaxRecentFiles = new javax.swing.JTextField();
        jLabelMaxRecentFiles = new javax.swing.JLabel();
        jCheckBoxOpenLastFile = new javax.swing.JCheckBox();
        jCheckBoxCreateNewFile = new javax.swing.JCheckBox();
        jCheckBoxMappingLive = new javax.swing.JCheckBox();
        jCheckBoxMapping = new javax.swing.JCheckBox();
        jCheckBoxMappingHFile = new javax.swing.JCheckBox();
        jCheckBoxHelpedCode = new javax.swing.JCheckBox();
        jCheckBoxSyntax = new javax.swing.JCheckBox();
        jCheckBoxBackup = new javax.swing.JCheckBox();
        jCheckBoxNumberLines = new javax.swing.JCheckBox();
        jCheckBoxScanProjectFiles = new javax.swing.JCheckBox();
        jCheckBoxWrapLines = new javax.swing.JCheckBox();
        jCheckBoxAutomaticCheckBrackets = new javax.swing.JCheckBox();
        jCheckBoxProjectOpenAllFiles = new javax.swing.JCheckBox();
        jCheckBoxProjectCloseAll = new javax.swing.JCheckBox();
        jPanelPath = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jTextFieldPathLib = new javax.swing.JTextField();
        jButton19 = new javax.swing.JButton();
        jPanel43 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jTextFieldPathLibSecondary1 = new javax.swing.JTextField();
        jButton25 = new javax.swing.JButton();
        jPanel45 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jTextFieldPathLibSecondary2 = new javax.swing.JTextField();
        jButton27 = new javax.swing.JButton();
        jPanel49 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jTextFieldPathLibSecondary3 = new javax.swing.JTextField();
        jButton28 = new javax.swing.JButton();
        jPanel17 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jTextFieldPathGames = new javax.swing.JTextField();
        jButton16 = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jCheckBoxAdventInLib = new javax.swing.JCheckBox();
        jPanel18 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jTextFieldPathCompiler = new javax.swing.JTextField();
        jButton17 = new javax.swing.JButton();
        jPanel19 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jTextFieldPathInterpreter = new javax.swing.JTextField();
        jButton18 = new javax.swing.JButton();
        jPanel44 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        jTextFieldPathGlulx = new javax.swing.JTextField();
        jButton26 = new javax.swing.JButton();
        jPanel25 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jTextFieldPathBrowser = new javax.swing.JTextField();
        jButton22 = new javax.swing.JButton();
        jPanel46 = new javax.swing.JPanel();
        jLabelBres = new javax.swing.JLabel();
        jTextFieldBres = new javax.swing.JTextField();
        jButtonBres = new javax.swing.JButton();
        jPanel47 = new javax.swing.JPanel();
        jLabelBlc = new javax.swing.JLabel();
        jTextFieldBlc = new javax.swing.JTextField();
        jButtonBlc = new javax.swing.JButton();
        jPanel48 = new javax.swing.JPanel();
        jCheckBoxMakeResource = new javax.swing.JCheckBox();
        jPanel15 = new javax.swing.JPanel();
        jPanelColor = new javax.swing.JPanel();
        jPanel33 = new javax.swing.JPanel();
        jPanel26 = new javax.swing.JPanel();
        jLabelKeyword = new javax.swing.JLabel();
        jButtonKeyword = new javax.swing.JButton();
        jPanel27 = new javax.swing.JPanel();
        jLabelAttribute = new javax.swing.JLabel();
        jButtonAttribute = new javax.swing.JButton();
        jPanel28 = new javax.swing.JPanel();
        jLabelProperty = new javax.swing.JLabel();
        jButtonProperty = new javax.swing.JButton();
        jPanel29 = new javax.swing.JPanel();
        jLabelRoutine = new javax.swing.JLabel();
        jButtonRoutine = new javax.swing.JButton();
        jPanel30 = new javax.swing.JPanel();
        jLabelVerb = new javax.swing.JLabel();
        jButtonVerb = new javax.swing.JButton();
        jPanel31 = new javax.swing.JPanel();
        jLabelNormal = new javax.swing.JLabel();
        jButtonNormal = new javax.swing.JButton();
        jPanel32 = new javax.swing.JPanel();
        jLabelComment = new javax.swing.JLabel();
        jButtonComment = new javax.swing.JButton();
        jPanel37 = new javax.swing.JPanel();
        jLabelBackground = new javax.swing.JLabel();
        jButtonBackground = new javax.swing.JButton();
        jPanelDefaultDark = new javax.swing.JPanel();
        jLabelDefaultDark = new javax.swing.JLabel();
        jButtonDefaultDark = new javax.swing.JButton();
        jEditorPaneColor = new javax.swing.JEditorPane();
        jPanelFont = new javax.swing.JPanel();
        jPanel34 = new javax.swing.JPanel();
        jComboBoxFont = new javax.swing.JComboBox();
        jComboBoxFont.addItem("Arial");
        jComboBoxFont.addItem("Book Antiqua");
        jComboBoxFont.addItem("Comic Sans MS");
        jComboBoxFont.addItem("Courier New");
        jComboBoxFont.addItem("Dialog");
        jComboBoxFont.addItem("Georgia");
        jComboBoxFont.addItem("Lucida Console");
        jComboBoxFont.addItem("Lucida Bright");
        jComboBoxFont.addItem("Lucida Sans");
        jComboBoxFont.addItem("Monospaced");
        jComboBoxFont.addItem("Thaoma");
        jComboBoxFont.addItem("Times New Roman");
        jComboBoxFont.addItem("Verdana");
        jComboBoxFontSize = new javax.swing.JComboBox();
        jComboBoxFontSize.addItem("7");
        jComboBoxFontSize.addItem("8");
        jComboBoxFontSize.addItem("9");
        jComboBoxFontSize.addItem("10");
        jComboBoxFontSize.addItem("11");
        jComboBoxFontSize.addItem("12");
        jComboBoxFontSize.addItem("13");
        jComboBoxFontSize.addItem("14");

        jTextFieldFont = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jButton10 = new javax.swing.JButton();
        jButton23 = new javax.swing.JButton();
        jDialogInfo = new JDialog (this, "", false);
        jScrollPaneInfo = new javax.swing.JScrollPane();
        jTextAreaInfo = new javax.swing.JTextArea();
        jPanel24 = new javax.swing.JPanel();
        jButton21 = new javax.swing.JButton();
        jDialogTutorial = new JDialog (this, "", false);
        jPanel35 = new javax.swing.JPanel();
        jButton24 = new javax.swing.JButton();
        jButtonPrintTutorial = new javax.swing.JButton();
        jScrollPane7 = new javax.swing.JScrollPane();
        jEditorPaneTutorial = new javax.swing.JEditorPane();
        jLabelTutorial = new javax.swing.JLabel();
        jPanel22 = new javax.swing.JPanel();
        jButtonNew = new javax.swing.JButton();
        OpenButton = new javax.swing.JButton();
        SaveButton = new javax.swing.JButton();
        SaveButtonAll = new javax.swing.JButton();
        SaveAsButton = new javax.swing.JButton();
        jButtonClose = new javax.swing.JButton();
        jButtonCloseAll = new javax.swing.JButton();
        jButtonPrint = new javax.swing.JButton();
        jButtonCut = new javax.swing.JButton();
        jButtonCopy = new javax.swing.JButton();
        jButtonPaste = new javax.swing.JButton();
        jButtonUndo = new javax.swing.JButton();
        jButtonRedo = new javax.swing.JButton();
        jTextFieldFind = new javax.swing.JTextField();
        jButtonFind = new javax.swing.JButton();
        jButtonSearchProject = new javax.swing.JButton();
        jButtonReplace = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JSeparator();
        jTextFieldDefinition = new javax.swing.JTextField();
        jButtonDefinition = new javax.swing.JButton();
        jSeparator21 = new javax.swing.JSeparator();
        AboutButton = new javax.swing.JButton();
        ExitButton = new javax.swing.JButton();
        jButtonCommentSelection = new javax.swing.JButton();
        jButtonUncommentSelection = new javax.swing.JButton();
        jButtonLeftTab = new javax.swing.JButton();
        jButtonRightTab = new javax.swing.JButton();
        jButtonBracketCheck = new javax.swing.JButton();
        RebuildButton = new javax.swing.JButton();
        RunButton = new javax.swing.JButton();
        jButtonInsertSymbol = new javax.swing.JButton();
        jButtonInfo = new javax.swing.JButton();
        jButtonExtractStrings = new javax.swing.JButton();
        jButtonTranslate = new javax.swing.JButton();
        jButtonInterpreter = new javax.swing.JButton();
        jButtonSwitchManager = new javax.swing.JButton();
        jButtonOption = new javax.swing.JButton();
        jTextFieldRowCol = new javax.swing.JTextField();
        jSplitPane3 = new javax.swing.JSplitPane();
        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanelTreeControl = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();

        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(new ImageIcon(getClass().getResource("/images/TREE_objects.png")));
        jTree1 = new javax.swing.JTree();
        // Creo la root per Inspect
        top = new DefaultMutableTreeNode("Inspect");
        category1 = new DefaultMutableTreeNode("Globals    ");
        top.add(category1);
        category2 = new DefaultMutableTreeNode("Constants    ");
        top.add(category2);
        //category3 = new DefaultMutableTreeNode("Locations");
        //top.add(category3);
        category4 = new DefaultMutableTreeNode("Objects    ");
        top.add(category4);
        category5 = new DefaultMutableTreeNode("Functions    ");
        top.add(category5);
        category6 = new DefaultMutableTreeNode("Sub    ");
        top.add(category6);

        category7 = new DefaultMutableTreeNode("Classes    ");
        top.add(category7);

        //Create a tree that allows one selection at a time.
        treeModel = new DefaultTreeModel(top);
        jTree1 = new JTree(treeModel);

        jTree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTree1.setCellRenderer(renderer);

        jPanelMainFile = new javax.swing.JPanel();
        jScrollPaneProject = new javax.swing.JScrollPane();
        jListProject = new javax.swing.JList();
        jListProject.addMouseListener(popupListenerProject);
        jLabelMainFile = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextAreaOutput = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        New = new javax.swing.JMenuItem();
        Open = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JSeparator();
        Save = new javax.swing.JMenuItem();
        SaveAs = new javax.swing.JMenuItem();
        jMenuItemSaveAll = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JSeparator();
        jMenuItemClose = new javax.swing.JMenuItem();
        jMenuItemCloseAll = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JSeparator();
        jMenuRecentFiles = new javax.swing.JMenu();
        jMenuItemClearRecentFiles = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItemPrint = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        Exit = new javax.swing.JMenuItem();
        jMenuEdit = new javax.swing.JMenu();
        jMenuItemCut = new javax.swing.JMenuItem();
        jMenuItemCopy1 = new javax.swing.JMenuItem();
        jMenuItemPaste = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JSeparator();
        jMenuItemSearch = new javax.swing.JMenuItem();
        jMenuItemSearchAllFiles = new javax.swing.JMenuItem();
        jMenuItemReplace = new javax.swing.JMenuItem();
        jMenuItemSelectAll = new javax.swing.JMenuItem();
        jMenuItemClearAll = new javax.swing.JMenuItem();
        jSeparator16 = new javax.swing.JSeparator();
        jMenuItemCommentSelection = new javax.swing.JMenuItem();
        jMenuItemUncommentSelection = new javax.swing.JMenuItem();
        jMenuItemRightShift = new javax.swing.JMenuItem();
        jMenuItemLeftShift = new javax.swing.JMenuItem();
        jSeparator17 = new javax.swing.JSeparator();
        jMenuItemInsertFile = new javax.swing.JMenuItem();
        jMenuItemInsertSymbol1 = new javax.swing.JMenuItem();
        jMenuItemSetBookmark = new javax.swing.JMenuItem();
        jMenuItemNextBookmark = new javax.swing.JMenuItem();
        jMenuView = new javax.swing.JMenu();
        jCheckBoxOutput = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxJToolBar = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxJToolBarInform = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxJTree = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxToggleFullscreen = new javax.swing.JCheckBoxMenuItem();
        jMenuProject = new javax.swing.JMenu();
        jMenuItemNewProject = new javax.swing.JMenuItem();
        jMenuItemOpenProject = new javax.swing.JMenuItem();
        jMenuItemSaveProject = new javax.swing.JMenuItem();
        jMenuItemCloseProject = new javax.swing.JMenuItem();
        jSeparator14 = new javax.swing.JSeparator();
        jMenuItemAddNewToProject = new javax.swing.JMenuItem();
        jMenuItemAddFileToProject = new javax.swing.JMenuItem();
        jMenuItemRemoveFromProject = new javax.swing.JMenuItem();
        jMenuMode = new javax.swing.JMenu();
        jCheckBoxInformMode = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxGlulxMode = new javax.swing.JCheckBoxMenuItem();
        jMenuBuild = new javax.swing.JMenu();
        BuildAll = new javax.swing.JMenuItem();
        jMenuItemSwitches = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        Run = new javax.swing.JMenuItem();
        jMenuGlulx = new javax.swing.JMenu();
        jMenuItemBuildAllGlulx = new javax.swing.JMenuItem();
        jSeparator18 = new javax.swing.JSeparator();
        jMenuItemMakeResource = new javax.swing.JMenuItem();
        jMenuItemCompile = new javax.swing.JMenuItem();
        jMenuItemMakeBlb = new javax.swing.JMenuItem();
        jSeparator15 = new javax.swing.JSeparator();
        jMenuItemRunUlx = new javax.swing.JMenuItem();
        jMenuItemRunBlb = new javax.swing.JMenuItem();
        jMenuOptions = new javax.swing.JMenu();
        jMenuItemSwitch = new javax.swing.JMenuItem();
        jMenuItemAltKeys = new javax.swing.JMenuItem();
        jMenuItemSyntax = new javax.swing.JMenuItem();
        jMenuItemLinks = new javax.swing.JMenuItem();
        jMenuItemHelpedCode = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JSeparator();
        jMenuItemSettings = new javax.swing.JMenuItem();
        jMenuLinks = new javax.swing.JMenu();
        jMenuTutorial = new javax.swing.JMenu();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItemHelp = new javax.swing.JMenuItem();
        jMenuItemConfigurazione = new javax.swing.JMenuItem();
        jMenuItemCopyright = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JSeparator();
        About = new javax.swing.JMenuItem();

        jPopupMenu1.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuInsertNew.setText(java.util.ResourceBundle.getBundle("JIF").getString("POPUPMENU_MENU_NEW"));
        jMenuInsertNew.setFont(new java.awt.Font("Dialog", 0, 11));
        jPopupMenu1.add(jMenuInsertNew);

        jMenuItemInsertSymbol.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemInsertSymbol.setText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_INSERT_SYMBOL"));
        jMenuItemInsertSymbol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemInsertSymbolActionPerformed(evt);
            }
        });

        jPopupMenu1.add(jMenuItemInsertSymbol);

        jMenuItemInsertFromFile.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemInsertFromFile.setText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_INSERT_FROM_FILE"));
        jMenuItemInsertFromFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemInsertFromFileActionPerformed(evt);
            }
        });

        jPopupMenu1.add(jMenuItemInsertFromFile);

        jPopupMenu1.add(jSeparator3);

        jMenuItemCut1.setText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_EDIT_CUT"));
        jMenuItemCut1.setActionCommand("KEY JFRAME_EDIT_CUT : RB JIF");
        jMenuItemCut1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCut1ActionPerformed(evt);
            }
        });

        jPopupMenu1.add(jMenuItemCut1);

        jMenuItemCopy.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemCopy.setText(java.util.ResourceBundle.getBundle("JIF").getString("POPUPMENU_MENUITEM_COPY"));
        jMenuItemCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCopyActionPerformed(evt);
            }
        });

        jPopupMenu1.add(jMenuItemCopy);

        jMenuPaste.setText(java.util.ResourceBundle.getBundle("JIF").getString("POPUPMENU_MENU_PASTE"));
        jMenuPaste.setFont(new java.awt.Font("Dialog", 0, 11));
        jPopupMenu1.add(jMenuPaste);

        jMenuItemClear.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemClear.setText(java.util.ResourceBundle.getBundle("JIF").getString("POPUPMENU_MENUITEM_CLEAR"));
        jMenuItemClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemClearActionPerformed(evt);
            }
        });

        jPopupMenu1.add(jMenuItemClear);

        jPopupMenu1.add(jSeparator13);

        jMenuItemPrint1.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemPrint1.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_PRINT"));
        jMenuItemPrint1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPrint1ActionPerformed(evt);
            }
        });

        jPopupMenu1.add(jMenuItemPrint1);

        jMenuItemPopupClose.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemPopupClose.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_CLOSE"));
        jMenuItemPopupClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPopupCloseActionPerformed(evt);
            }
        });

        jPopupMenu1.add(jMenuItemPopupClose);

        jMenuItemPopupCloseAllFiles.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemPopupCloseAllFiles.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_CLOSEALL"));
        jMenuItemPopupCloseAllFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPopupCloseAllFilesActionPerformed(evt);
            }
        });

        jPopupMenu1.add(jMenuItemPopupCloseAllFiles);

        jMenuItemJumpToSource.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENU_JUMP_TO_SOURCE"));
        jMenuItemJumpToSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemJumpToSourceActionPerformed(evt);
            }
        });

        jPopupMenu1.add(jMenuItemJumpToSource);

        jDialogAbout.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jDialogAbout.setTitle(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_ABOUT_JIF"));
        jDialogAbout.setModal(true);
        jDialogAbout.setResizable(false);
        jDialogAbout.getAccessibleContext().setAccessibleParent(this);
        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/about.png")));
        jDialogAbout.getContentPane().add(jLabel7, java.awt.BorderLayout.CENTER);

        jButton9.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_OK"));
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jPanel7.add(jButton9);

        jDialogAbout.getContentPane().add(jPanel7, java.awt.BorderLayout.SOUTH);

        jDialogConfigFiles.setTitle(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOG_CONFIGFILES_TITLE"));
        jDialogConfigFiles.setFont(new java.awt.Font("Arial", 0, 12));
        jPanel1.setBorder(new javax.swing.border.EtchedBorder());
        jButton1.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_SAVE"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel1.add(jButton1);

        jButton2.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_CLOSE"));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jPanel1.add(jButton2);

        jDialogConfigFiles.getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        jTextAreaConfig.setTabSize(4);
        jScrollPane4.setViewportView(jTextAreaConfig);

        jDialogConfigFiles.getContentPane().add(jScrollPane4, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.GridLayout(1, 0));

        jPanel2.setBorder(new javax.swing.border.EtchedBorder());
        jLabel2.setText("jLabel2");
        jPanel2.add(jLabel2);

        jDialogConfigFiles.getContentPane().add(jPanel2, java.awt.BorderLayout.NORTH);

        jDialogSwitches.setTitle(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOG_SWITCHES_TITLE"));
        jDialogSwitches.setFont(new java.awt.Font("Arial", 0, 12));
        jDialogSwitches.setModal(true);
        jPanel6.setLayout(new javax.swing.BoxLayout(jPanel6, javax.swing.BoxLayout.Y_AXIS));

        jLabel4.setText("jLabel4");
        jPanel6.add(jLabel4);

        jPanel6.add(jLabel3);

        jDialogSwitches.getContentPane().add(jPanel6, java.awt.BorderLayout.NORTH);

        jPanel11.setLayout(new java.awt.GridLayout(2, 0));

        jPanelSwitch1.setLayout(new java.awt.GridLayout(0, 4));

        jPanelSwitch1.setBorder(new javax.swing.border.EtchedBorder());
        jPanelSwitch1.setFont(new java.awt.Font("Dialog", 0, 8));
        jPanel11.add(jPanelSwitch1);

        jPanelSwitch2.setLayout(new java.awt.GridLayout(0, 3));

        jPanelSwitch2.setBorder(new javax.swing.border.EtchedBorder());
        jPanelSwitch2.setFont(new java.awt.Font("Dialog", 0, 8));
        jPanel11.add(jPanelSwitch2);

        jDialogSwitches.getContentPane().add(jPanel11, java.awt.BorderLayout.CENTER);

        jButton4.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_OK"));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jPanel4.add(jButton4);

        jButton15.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_SAVE"));
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });

        jPanel4.add(jButton15);

        jDialogSwitches.getContentPane().add(jPanel4, java.awt.BorderLayout.SOUTH);

        jButton6.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_OK"));
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jPanel8.add(jButton6);

        jDialogText.getContentPane().add(jPanel8, java.awt.BorderLayout.SOUTH);

        jTextArea4.setEditable(false);
        jTextArea4.setFont(new java.awt.Font("Courier New", 0, 12));
        jTextArea4.setTabSize(4);
        jScrollPane6.setViewportView(jTextArea4);

        jDialogText.getContentPane().add(jScrollPane6, java.awt.BorderLayout.CENTER);

        jLabel5.setText("jLabel5");
        jDialogText.getContentPane().add(jLabel5, java.awt.BorderLayout.NORTH);

        jDialogReplace.setTitle(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOGREPLACE_TITLE"));
        jDialogReplace.setModal(true);
        jDialogReplace.getContentPane().add(jPanel12, java.awt.BorderLayout.SOUTH);

        jPanel13.setLayout(new java.awt.GridLayout(2, 0, -120, 0));

        jPanel13.setAlignmentY(0.47619048F);
        jPanel13.setPreferredSize(new java.awt.Dimension(208, 40));
        jLabel1.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOGREPLACE_FIND_LABEL"));
        jLabel1.setPreferredSize(new java.awt.Dimension(41, 17));
        jPanel13.add(jLabel1);

        jPanel13.add(jTextFieldReplaceFind);

        jLabel6.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOGREPLACE_REPLACE_LABEL"));
        jPanel13.add(jLabel6);

        jTextFieldReplace.setMinimumSize(new java.awt.Dimension(100, 30));
        jPanel13.add(jTextFieldReplace);

        jDialogReplace.getContentPane().add(jPanel13, java.awt.BorderLayout.NORTH);

        jPanel20.setLayout(new java.awt.GridLayout(4, 0));

        jButton11.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOGREPLACE_BUTTON_FIND"));
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jPanel20.add(jButton11);

        jButton12.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOGREPLACE_BUTTON_REPLACE"));
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        jPanel20.add(jButton12);

        jButton13.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOGREPLACE_BUTTON_REPLACE_ALL"));
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        jPanel20.add(jButton13);

        jButton14.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOGREPLACE_BUTTON_CLOSE"));
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        jPanel20.add(jButton14);

        jDialogReplace.getContentPane().add(jPanel20, java.awt.BorderLayout.EAST);

        jPanel23.setLayout(new java.awt.BorderLayout());

        jPanel21.setLayout(new java.awt.GridLayout(3, 0));

        jPanel21.setBorder(new javax.swing.border.TitledBorder(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOGREPLACE_SCOPE")));
        jPanel21.setMinimumSize(new java.awt.Dimension(200, 200));
        jPanel21.setEnabled(false);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOGREPLACE_SCOPE_CURRENT_FILE"));
        jRadioButton1.setEnabled(false);
        jPanel21.add(jRadioButton1);

        jRadioButton2.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOGREPLACE_SCOPE_SELECTED_TEXT"));
        jRadioButton2.setEnabled(false);
        jPanel21.add(jRadioButton2);

        jPanel23.add(jPanel21, java.awt.BorderLayout.CENTER);

        jDialogReplace.getContentPane().add(jPanel23, java.awt.BorderLayout.CENTER);

        jPopupMenuProject.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemPopupNewProject.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemPopupNewProject.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_NEW_PROJECT"));
        jMenuItemPopupNewProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPopupNewProjectActionPerformed(evt);
            }
        });

        jPopupMenuProject.add(jMenuItemPopupNewProject);

        jMenuItemPopupOpenProject.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemPopupOpenProject.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_OPEN_PROJECT"));
        jMenuItemPopupOpenProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPopupOpenProjectActionPerformed(evt);
            }
        });

        jPopupMenuProject.add(jMenuItemPopupOpenProject);

        jMenuItemPopupSaveProject.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemPopupSaveProject.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_SAVE_PROJECT"));
        jMenuItemPopupSaveProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPopupSaveProjectActionPerformed(evt);
            }
        });

        jPopupMenuProject.add(jMenuItemPopupSaveProject);

        jMenuItemPopupCloseProject.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemPopupCloseProject.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_CLOSE_PROJECT"));
        jMenuItemPopupCloseProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPopupCloseProjectActionPerformed(evt);
            }
        });

        jPopupMenuProject.add(jMenuItemPopupCloseProject);

        jPopupMenuProject.add(jSeparator6);

        jMenuItemPopupAddNewToProject.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemPopupAddNewToProject.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_ADD_NEWFILE_TO_PROJECT"));
        jMenuItemPopupAddNewToProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPopupAddNewToProjectActionPerformed(evt);
            }
        });

        jPopupMenuProject.add(jMenuItemPopupAddNewToProject);

        jMenuItemPopupAddToProject.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemPopupAddToProject.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_ADD_FILE_TO_PROJECT"));
        jMenuItemPopupAddToProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPopupAddToProjectActionPerformed(evt);
            }
        });

        jPopupMenuProject.add(jMenuItemPopupAddToProject);

        jMenuItemPopupRemoveFromProject.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemPopupRemoveFromProject.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_POPUP_REMOVE"));
        jMenuItemPopupRemoveFromProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPopupRemoveFromProjectActionPerformed(evt);
            }
        });

        jPopupMenuProject.add(jMenuItemPopupRemoveFromProject);

        jMenuItemPopupOpenSelectedFiles.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemPopupOpenSelectedFiles.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_OPEN_SELECTED_FILES"));
        jMenuItemPopupOpenSelectedFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPopupOpenSelectedFilesActionPerformed(evt);
            }
        });

        jPopupMenuProject.add(jMenuItemPopupOpenSelectedFiles);

        jPopupMenuProject.add(jSeparator19);

        jMenuItemSetMainClass.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemSetMainClass.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_SET_AS_MAIN_FILE"));
        jMenuItemSetMainClass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSetMainClassActionPerformed(evt);
            }
        });

        jPopupMenuProject.add(jMenuItemSetMainClass);

        jMenuItemRemoveMainClass.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemRemoveMainClass.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_REMOVE_MAIN_FILE"));
        jMenuItemRemoveMainClass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRemoveMainClassActionPerformed(evt);
            }
        });

        jPopupMenuProject.add(jMenuItemRemoveMainClass);

        jDialogOption.setTitle(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_SETTING"));
        jDialogOption.setModal(true);
        jDialogOption.setResizable(false);
        jTabbedPaneOption.setMinimumSize(new java.awt.Dimension(535, 535));
        jTabbedPaneOption.setPreferredSize(new java.awt.Dimension(535, 535));
        jPanelGeneral.setLayout(new java.awt.GridLayout(1, 0));

        jPanelGeneral.setPreferredSize(new java.awt.Dimension(205, 400));
        jPanel36.setLayout(new java.awt.GridLayout(15, 0));

        jPanel36.setMinimumSize(new java.awt.Dimension(205, 400));
        jPanel36.setPreferredSize(new java.awt.Dimension(205, 400));
        jPanel42.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jTextFieldMaxRecentFiles.setColumns(5);
        jTextFieldMaxRecentFiles.setText("10");
        jTextFieldMaxRecentFiles.setMinimumSize(new java.awt.Dimension(18, 21));
        jPanel42.add(jTextFieldMaxRecentFiles);

        jLabelMaxRecentFiles.setText(java.util.ResourceBundle.getBundle("JIF").getString("JOPTIONDIALOG_MAX_RECENT_FILES"));
        jPanel42.add(jLabelMaxRecentFiles);

        jPanel36.add(jPanel42);

        jCheckBoxOpenLastFile.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_OPEN_LAST_OPEN_FILE"));
        jPanel36.add(jCheckBoxOpenLastFile);

        jCheckBoxCreateNewFile.setText(java.util.ResourceBundle.getBundle("JIF").getString("OPTION_CREATE_A_NEW_FILE"));
        jPanel36.add(jCheckBoxCreateNewFile);

        jCheckBoxMappingLive.setText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_MAPPINGLIVE"));
        jCheckBoxMappingLive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMappingLiveActionPerformed(evt);
            }
        });

        jPanel36.add(jCheckBoxMappingLive);

        jCheckBoxMapping.setText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_MAPPING"));
        jCheckBoxMapping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMappingActionPerformed(evt);
            }
        });

        jPanel36.add(jCheckBoxMapping);

        jCheckBoxMappingHFile.setText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_MAPPING_HEADER_FILE"));
        jCheckBoxMappingHFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMappingHFileActionPerformed(evt);
            }
        });

        jPanel36.add(jCheckBoxMappingHFile);

        jCheckBoxHelpedCode.setSelected(true);
        jCheckBoxHelpedCode.setText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_HELPEDCODE"));
        jCheckBoxHelpedCode.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_HELPEDCODE_TOOLTIP"));
        jPanel36.add(jCheckBoxHelpedCode);

        jCheckBoxSyntax.setSelected(true);
        jCheckBoxSyntax.setText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_SYNTAX"));
        jPanel36.add(jCheckBoxSyntax);

        jCheckBoxBackup.setText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_BACKUP"));
        jCheckBoxBackup.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_BACKUP_TOOLTIP"));
        jPanel36.add(jCheckBoxBackup);

        jCheckBoxNumberLines.setText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_NUMBEROFLINES"));
        jCheckBoxNumberLines.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxNumberLinesActionPerformed(evt);
            }
        });

        jPanel36.add(jCheckBoxNumberLines);

        jCheckBoxScanProjectFiles.setText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_SCAN_PROJECT"));
        jPanel36.add(jCheckBoxScanProjectFiles);

        jCheckBoxWrapLines.setText("Wrap Lines");
        jCheckBoxWrapLines.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxWrapLinesActionPerformed(evt);
            }
        });

        jPanel36.add(jCheckBoxWrapLines);

        jCheckBoxAutomaticCheckBrackets.setText(java.util.ResourceBundle.getBundle("JIF").getString("JOPTION_AUTOMATIC_CHECK_BRACKETS"));
        jPanel36.add(jCheckBoxAutomaticCheckBrackets);

        jCheckBoxProjectOpenAllFiles.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_OPEN_ALL_FILES"));
        jPanel36.add(jCheckBoxProjectOpenAllFiles);

        jCheckBoxProjectCloseAll.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_CLOSE_ALL_FILE"));
        jPanel36.add(jCheckBoxProjectCloseAll);

        jPanelGeneral.add(jPanel36);

        jTabbedPaneOption.addTab("General", jPanelGeneral);

        jPanelPath.setLayout(new java.awt.BorderLayout());

        jPanel14.setLayout(new java.awt.GridLayout(13, 1));

        jPanel16.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabel13.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOG_CONFIGPATH_LIBRARY"));
        jPanel16.add(jLabel13);

        jTextFieldPathLib.setPreferredSize(new java.awt.Dimension(280, 21));
        jPanel16.add(jTextFieldPathLib);

        jButton19.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_BROWSE"));
        jButton19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton19ActionPerformed(evt);
            }
        });

        jPanel16.add(jButton19);

        jPanel14.add(jPanel16);

        jPanel43.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabel18.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOG_CONFIGPATH_LIBRARY_SECONDARY1"));
        jPanel43.add(jLabel18);

        jTextFieldPathLibSecondary1.setPreferredSize(new java.awt.Dimension(280, 21));
        jPanel43.add(jTextFieldPathLibSecondary1);

        jButton25.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_BROWSE"));
        jButton25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton25ActionPerformed(evt);
            }
        });

        jPanel43.add(jButton25);

        jPanel14.add(jPanel43);

        jPanel45.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabel20.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOG_CONFIGPATH_LIBRARY_SECONDARY2"));
        jPanel45.add(jLabel20);

        jTextFieldPathLibSecondary2.setPreferredSize(new java.awt.Dimension(280, 21));
        jPanel45.add(jTextFieldPathLibSecondary2);

        jButton27.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_BROWSE"));
        jButton27.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton27ActionPerformed(evt);
            }
        });

        jPanel45.add(jButton27);

        jPanel14.add(jPanel45);

        jPanel49.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabel21.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOG_CONFIGPATH_LIBRARY_SECONDARY3"));
        jPanel49.add(jLabel21);

        jTextFieldPathLibSecondary3.setPreferredSize(new java.awt.Dimension(280, 21));
        jPanel49.add(jTextFieldPathLibSecondary3);

        jButton28.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_BROWSE"));
        jButton28.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton28ActionPerformed(evt);
            }
        });

        jPanel49.add(jButton28);

        jPanel14.add(jPanel49);

        jPanel17.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabel14.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOG_CONFIGPATH_ATPATH"));
        jPanel17.add(jLabel14);

        jTextFieldPathGames.setPreferredSize(new java.awt.Dimension(280, 21));
        jPanel17.add(jTextFieldPathGames);

        jButton16.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_BROWSE"));
        jButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton16ActionPerformed(evt);
            }
        });

        jPanel17.add(jButton16);

        jPanel14.add(jPanel17);

        jCheckBoxAdventInLib.setText(java.util.ResourceBundle.getBundle("JIF").getString("JOPTION_ADVENT_IN_LIB"));
        jPanel9.add(jCheckBoxAdventInLib);

        jPanel14.add(jPanel9);

        jPanel18.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabel15.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOG_CONFIGPATH_COMPILERPATH"));
        jPanel18.add(jLabel15);

        jTextFieldPathCompiler.setPreferredSize(new java.awt.Dimension(280, 21));
        jPanel18.add(jTextFieldPathCompiler);

        jButton17.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_BROWSE"));
        jButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton17ActionPerformed(evt);
            }
        });

        jPanel18.add(jButton17);

        jPanel14.add(jPanel18);

        jPanel19.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabel16.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOG_CONFIGPATH_INTERPRETERPATH"));
        jPanel19.add(jLabel16);

        jTextFieldPathInterpreter.setPreferredSize(new java.awt.Dimension(280, 21));
        jPanel19.add(jTextFieldPathInterpreter);

        jButton18.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_BROWSE"));
        jButton18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton18ActionPerformed(evt);
            }
        });

        jPanel19.add(jButton18);

        jPanel14.add(jPanel19);

        jPanel44.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel19.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOG_CONFIGPATH_GLULXINTERPRETERPATH"));
        jPanel44.add(jLabel19);

        jTextFieldPathGlulx.setPreferredSize(new java.awt.Dimension(280, 21));
        jPanel44.add(jTextFieldPathGlulx);

        jButton26.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_BROWSE"));
        jButton26.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jButton26.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton26ActionPerformed(evt);
            }
        });

        jPanel44.add(jButton26);

        jPanel14.add(jPanel44);

        jPanel25.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabel17.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOG_CONFIGPATH_BROWSERPATH"));
        jPanel25.add(jLabel17);

        jTextFieldPathBrowser.setPreferredSize(new java.awt.Dimension(280, 21));
        jPanel25.add(jTextFieldPathBrowser);

        jButton22.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_BROWSE"));
        jButton22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton22ActionPerformed(evt);
            }
        });

        jPanel25.add(jButton22);

        jPanel14.add(jPanel25);

        jPanel46.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabelBres.setText(java.util.ResourceBundle.getBundle("JIF").getString("GLULX_BRES_LOCATION"));
        jPanel46.add(jLabelBres);

        jTextFieldBres.setText("c:\\Jif\\glulx\\bres.exe");
        jTextFieldBres.setPreferredSize(new java.awt.Dimension(280, 21));
        jPanel46.add(jTextFieldBres);

        jButtonBres.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_BROWSE"));
        jButtonBres.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jButtonBres.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBresActionPerformed(evt);
            }
        });

        jPanel46.add(jButtonBres);

        jPanel14.add(jPanel46);

        jPanel47.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabelBlc.setText(java.util.ResourceBundle.getBundle("JIF").getString("GLULX_BLC_LOCATION"));
        jPanel47.add(jLabelBlc);

        jTextFieldBlc.setText("c:\\Jif\\glulx\\blc.exe");
        jTextFieldBlc.setPreferredSize(new java.awt.Dimension(280, 21));
        jPanel47.add(jTextFieldBlc);

        jButtonBlc.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_BROWSE"));
        jButtonBlc.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jButtonBlc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBlcActionPerformed(evt);
            }
        });

        jPanel47.add(jButtonBlc);

        jPanel14.add(jPanel47);

        jCheckBoxMakeResource.setText(java.util.ResourceBundle.getBundle("JIF").getString("GLULX_MAKE_RESOURCE_WHEN_BUILD_ALL"));
        jPanel48.add(jCheckBoxMakeResource);

        jPanel14.add(jPanel48);

        jPanelPath.add(jPanel14, java.awt.BorderLayout.CENTER);

        jPanelPath.add(jPanel15, java.awt.BorderLayout.SOUTH);

        jTabbedPaneOption.addTab("Path", jPanelPath);

        jPanelColor.setLayout(new java.awt.BorderLayout());

        jPanel33.setLayout(new javax.swing.BoxLayout(jPanel33, javax.swing.BoxLayout.Y_AXIS));

        jPanel26.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabelKeyword.setText("Keyword");
        jPanel26.add(jLabelKeyword);

        jButtonKeyword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonKeywordActionPerformed(evt);
            }
        });

        jPanel26.add(jButtonKeyword);

        jPanel33.add(jPanel26);

        jPanel27.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabelAttribute.setText("Attribute");
        jPanel27.add(jLabelAttribute);

        jButtonAttribute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAttributeActionPerformed(evt);
            }
        });

        jPanel27.add(jButtonAttribute);

        jPanel33.add(jPanel27);

        jPanel28.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabelProperty.setText("Property");
        jPanel28.add(jLabelProperty);

        jButtonProperty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPropertyActionPerformed(evt);
            }
        });

        jPanel28.add(jButtonProperty);

        jPanel33.add(jPanel28);

        jPanel29.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabelRoutine.setText("Routine");
        jPanel29.add(jLabelRoutine);

        jButtonRoutine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRoutineActionPerformed(evt);
            }
        });

        jPanel29.add(jButtonRoutine);

        jPanel33.add(jPanel29);

        jPanel30.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabelVerb.setText("Verb");
        jPanel30.add(jLabelVerb);

        jButtonVerb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonVerbActionPerformed(evt);
            }
        });

        jPanel30.add(jButtonVerb);

        jPanel33.add(jPanel30);

        jPanel31.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabelNormal.setText("Normal");
        jPanel31.add(jLabelNormal);

        jButtonNormal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNormalActionPerformed(evt);
            }
        });

        jPanel31.add(jButtonNormal);

        jPanel33.add(jPanel31);

        jPanel32.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabelComment.setText("Comment");
        jPanel32.add(jLabelComment);

        jButtonComment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCommentActionPerformed(evt);
            }
        });

        jPanel32.add(jButtonComment);

        jPanel33.add(jPanel32);

        jPanel37.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabelBackground.setText("Background");
        jPanel37.add(jLabelBackground);

        jButtonBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBackgroundActionPerformed(evt);
            }
        });

        jPanel37.add(jButtonBackground);

        jPanel33.add(jPanel37);

        jPanelDefaultDark.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabelDefaultDark.setText("Black setting");
        jPanelDefaultDark.add(jLabelDefaultDark);

        jButtonDefaultDark.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDefaultDarkActionPerformed(evt);
            }
        });

        jPanelDefaultDark.add(jButtonDefaultDark);

        jPanel33.add(jPanelDefaultDark);

        jPanelColor.add(jPanel33, java.awt.BorderLayout.WEST);

        jPanelColor.add(jEditorPaneColor, java.awt.BorderLayout.CENTER);

        jTabbedPaneOption.addTab("Color", jPanelColor);

        jPanelFont.setLayout(new java.awt.BorderLayout());

        jComboBoxFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxFontActionPerformed(evt);
            }
        });

        jPanel34.add(jComboBoxFont);

        jComboBoxFontSize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxFontSizeActionPerformed(evt);
            }
        });

        jPanel34.add(jComboBoxFontSize);

        jPanelFont.add(jPanel34, java.awt.BorderLayout.WEST);

        jTextFieldFont.setText("Test Test Test abcdefghilmnopqrstuvz");
        jPanelFont.add(jTextFieldFont, java.awt.BorderLayout.NORTH);

        jTabbedPaneOption.addTab("Font", jPanelFont);

        jDialogOption.getContentPane().add(jTabbedPaneOption, java.awt.BorderLayout.CENTER);

        jButton10.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_OK"));
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jPanel3.add(jButton10);

        jButton23.setText("Default");
        jButton23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton23ActionPerformed(evt);
            }
        });

        jPanel3.add(jButton23);

        jDialogOption.getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

        jDialogInfo.setTitle("");
        jDialogInfo.setModal(true);
        jScrollPaneInfo.setAutoscrolls(true);
        jTextAreaInfo.setBackground(new java.awt.Color(204, 204, 204));
        jTextAreaInfo.setEditable(false);
        jTextAreaInfo.setFont(new java.awt.Font("Monospaced", 0, 12));
        jTextAreaInfo.setMaximumSize(new java.awt.Dimension(0, 0));
        jTextAreaInfo.setMinimumSize(new java.awt.Dimension(0, 0));
        jTextAreaInfo.setPreferredSize(new java.awt.Dimension(0, 0));
        jScrollPaneInfo.setViewportView(jTextAreaInfo);

        jDialogInfo.getContentPane().add(jScrollPaneInfo, java.awt.BorderLayout.CENTER);

        jButton21.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_CLOSE"));
        jButton21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton21ActionPerformed(evt);
            }
        });

        jPanel24.add(jButton21);

        jDialogInfo.getContentPane().add(jPanel24, java.awt.BorderLayout.SOUTH);

        jButton24.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_OK"));
        jButton24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton24ActionPerformed(evt);
            }
        });

        jPanel35.add(jButton24);

        jButtonPrintTutorial.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_PRINT"));
        jButtonPrintTutorial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintTutorialActionPerformed(evt);
            }
        });

        jPanel35.add(jButtonPrintTutorial);

        jDialogTutorial.getContentPane().add(jPanel35, java.awt.BorderLayout.SOUTH);

        jEditorPaneTutorial.setEditable(false);
        jScrollPane7.setViewportView(jEditorPaneTutorial);

        jDialogTutorial.getContentPane().add(jScrollPane7, java.awt.BorderLayout.CENTER);

        jLabelTutorial.setText("jLabel5");
        jDialogTutorial.getContentPane().add(jLabelTutorial, java.awt.BorderLayout.NORTH);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(getJifVersion());
        setFont(new java.awt.Font("Dialog", 0, 12));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jPanel22.setLayout(new java.awt.BorderLayout());

        jPanel22.setMinimumSize(new java.awt.Dimension(0, 0));
        jToolBarCommon.setBorder(new javax.swing.border.EtchedBorder());
        jToolBarCommon.setFloatable(false);
        jToolBarCommon.setToolTipText("Jif Toolbar");
        jToolBarCommon.setPreferredSize(new java.awt.Dimension(400, 34));
        jButtonNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/filenew.png")));
        jButtonNew.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_NEW_TOOLTIP"));
        jButtonNew.setBorderPainted(false);
        jButtonNew.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonNew.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonNew.setPreferredSize(new java.awt.Dimension(29, 29));
        jButtonNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNewActionPerformed(evt);
            }
        });
        jButtonNew.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarCommon.add(jButtonNew);

        OpenButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/fileopen.png")));
        OpenButton.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_OPEN"));
        OpenButton.setBorderPainted(false);
        OpenButton.setMaximumSize(new java.awt.Dimension(29, 29));
        OpenButton.setMinimumSize(new java.awt.Dimension(29, 29));
        OpenButton.setPreferredSize(new java.awt.Dimension(29, 29));
        OpenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenButtonActionPerformed(evt);
            }
        });
        OpenButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarCommon.add(OpenButton);

        SaveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/filesave.png")));
        SaveButton.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_SAVE"));
        SaveButton.setBorderPainted(false);
        SaveButton.setMaximumSize(new java.awt.Dimension(29, 29));
        SaveButton.setMinimumSize(new java.awt.Dimension(29, 29));
        SaveButton.setPreferredSize(new java.awt.Dimension(29, 29));
        SaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveButtonActionPerformed(evt);
            }
        });
        SaveButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarCommon.add(SaveButton);

        SaveButtonAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/filesaveall.png")));
        SaveButtonAll.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_SAVEALL"));
        SaveButtonAll.setBorderPainted(false);
        SaveButtonAll.setMaximumSize(new java.awt.Dimension(29, 29));
        SaveButtonAll.setMinimumSize(new java.awt.Dimension(29, 29));
        SaveButtonAll.setPreferredSize(new java.awt.Dimension(29, 29));
        SaveButtonAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveButtonAllActionPerformed(evt);
            }
        });
        SaveButtonAll.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarCommon.add(SaveButtonAll);

        SaveAsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/filesaveas.png")));
        SaveAsButton.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_SAVEAS"));
        SaveAsButton.setBorderPainted(false);
        SaveAsButton.setMaximumSize(new java.awt.Dimension(29, 29));
        SaveAsButton.setMinimumSize(new java.awt.Dimension(29, 29));
        SaveAsButton.setPreferredSize(new java.awt.Dimension(29, 29));
        SaveAsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveAsButtonActionPerformed(evt);
            }
        });
        SaveAsButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarCommon.add(SaveAsButton);

        jButtonClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/fileclose.png")));
        jButtonClose.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_CLOSE"));
        jButtonClose.setBorderPainted(false);
        jButtonClose.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonClose.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonClose.setPreferredSize(new java.awt.Dimension(29, 29));
        jButtonClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCloseActionPerformed(evt);
            }
        });
        jButtonClose.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarCommon.add(jButtonClose);

        jButtonCloseAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/filecloseAll.png")));
        jButtonCloseAll.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_CLOSEALL"));
        jButtonCloseAll.setBorderPainted(false);
        jButtonCloseAll.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonCloseAll.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonCloseAll.setPreferredSize(new java.awt.Dimension(29, 29));
        jButtonCloseAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCloseAllActionPerformed(evt);
            }
        });
        jButtonCloseAll.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarCommon.add(jButtonCloseAll);

        jButtonPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/fileprint.png")));
        jButtonPrint.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_PRINT"));
        jButtonPrint.setBorderPainted(false);
        jButtonPrint.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonPrint.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonPrint.setPreferredSize(new java.awt.Dimension(29, 29));
        jButtonPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPrintActionPerformed(evt);
            }
        });
        jButtonPrint.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarCommon.add(jButtonPrint);

        jButtonCut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/editcut.png")));
        jButtonCut.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_CUT"));
        jButtonCut.setBorderPainted(false);
        jButtonCut.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonCut.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonCut.setPreferredSize(new java.awt.Dimension(29, 29));
        jButtonCut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCutActionPerformed(evt);
            }
        });
        jButtonCut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarCommon.add(jButtonCut);

        jButtonCopy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/editcopy.png")));
        jButtonCopy.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_COPY"));
        jButtonCopy.setBorderPainted(false);
        jButtonCopy.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonCopy.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonCopy.setPreferredSize(new java.awt.Dimension(29, 29));
        jButtonCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCopyActionPerformed(evt);
            }
        });
        jButtonCopy.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarCommon.add(jButtonCopy);

        jButtonPaste.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/editpaste.png")));
        jButtonPaste.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_PASTE"));
        jButtonPaste.setBorderPainted(false);
        jButtonPaste.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonPaste.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonPaste.setPreferredSize(new java.awt.Dimension(29, 29));
        jButtonPaste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPasteActionPerformed(evt);
            }
        });
        jButtonPaste.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarCommon.add(jButtonPaste);

        jButtonUndo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/undo.png")));
        jButtonUndo.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_UNDO"));
        jButtonUndo.setBorderPainted(false);
        jButtonUndo.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonUndo.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonUndo.setPreferredSize(new java.awt.Dimension(29, 29));
        jButtonUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUndoActionPerformed(evt);
            }
        });
        jButtonUndo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarCommon.add(jButtonUndo);

        jButtonRedo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/redo.png")));
        jButtonRedo.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_REDO"));
        jButtonRedo.setBorderPainted(false);
        jButtonRedo.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonRedo.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonRedo.setPreferredSize(new java.awt.Dimension(29, 29));
        jButtonRedo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRedoActionPerformed(evt);
            }
        });
        jButtonRedo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarCommon.add(jButtonRedo);

        jTextFieldFind.setColumns(15);
        jTextFieldFind.setFont(new java.awt.Font("Dialog", 1, 12));
        jTextFieldFind.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JTOOLBAR_SEARCH"));
        jTextFieldFind.setMaximumSize(new java.awt.Dimension(200, 30));
        jTextFieldFind.setMinimumSize(new java.awt.Dimension(10, 22));
        jTextFieldFind.setPreferredSize(new java.awt.Dimension(171, 30));
        jTextFieldFind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFindActionPerformed(evt);
            }
        });

        jToolBarCommon.add(jTextFieldFind);

        jButtonFind.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/filefind.png")));
        jButtonFind.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_SEARCH_BUTTON"));
        jButtonFind.setBorderPainted(false);
        jButtonFind.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonFind.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonFind.setPreferredSize(new java.awt.Dimension(29, 29));
        jButtonFind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFindActionPerformed(evt);
            }
        });
        jButtonFind.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarCommon.add(jButtonFind);

        jButtonSearchProject.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/fileprojectfind.png")));
        jButtonSearchProject.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_SEARCHALL_BUTTON"));
        jButtonSearchProject.setBorderPainted(false);
        jButtonSearchProject.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonSearchProject.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonSearchProject.setPreferredSize(new java.awt.Dimension(29, 29));
        jButtonSearchProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSearchProjectActionPerformed(evt);
            }
        });
        jButtonSearchProject.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarCommon.add(jButtonSearchProject);

        jButtonReplace.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/find.png")));
        jButtonReplace.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_REPLACE"));
        jButtonReplace.setBorderPainted(false);
        jButtonReplace.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonReplace.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonReplace.setPreferredSize(new java.awt.Dimension(29, 29));
        jButtonReplace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonReplaceActionPerformed(evt);
            }
        });
        jButtonReplace.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarCommon.add(jButtonReplace);

        jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator5.setMinimumSize(new java.awt.Dimension(10, 10));
        jSeparator5.setPreferredSize(new java.awt.Dimension(0, 34));
        jSeparator5.setRequestFocusEnabled(false);
        jToolBarCommon.add(jSeparator5);

        jTextFieldDefinition.setColumns(15);
        jTextFieldDefinition.setFont(new java.awt.Font("Dialog", 1, 12));
        jTextFieldDefinition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldDefinitionActionPerformed(evt);
            }
        });

        jToolBarCommon.add(jTextFieldDefinition);

        jButtonDefinition.setText("Definition");
        jButtonDefinition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDefinitionActionPerformed(evt);
            }
        });

        jToolBarCommon.add(jButtonDefinition);

        jSeparator21.setMinimumSize(new java.awt.Dimension(10, 10));
        jToolBarCommon.add(jSeparator21);

        AboutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/info.png")));
        AboutButton.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_ABOUT"));
        AboutButton.setBorderPainted(false);
        AboutButton.setMaximumSize(new java.awt.Dimension(29, 29));
        AboutButton.setMinimumSize(new java.awt.Dimension(29, 29));
        AboutButton.setPreferredSize(new java.awt.Dimension(29, 29));
        AboutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AboutButtonActionPerformed(evt);
            }
        });
        AboutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarCommon.add(AboutButton);

        ExitButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/exit.png")));
        ExitButton.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_EXIT"));
        ExitButton.setBorderPainted(false);
        ExitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExitButtonActionPerformed(evt);
            }
        });
        ExitButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarCommon.add(ExitButton);

        jPanel22.add(jToolBarCommon, java.awt.BorderLayout.NORTH);

        jToolBarInform.setBorder(new javax.swing.border.EtchedBorder());
        jToolBarInform.setFloatable(false);
        jToolBarInform.setPreferredSize(new java.awt.Dimension(400, 34));
        jButtonCommentSelection.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/comment.png")));
        jButtonCommentSelection.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_COMMENT_SELECTION"));
        jButtonCommentSelection.setBorderPainted(false);
        jButtonCommentSelection.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonCommentSelection.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonCommentSelection.setPreferredSize(new java.awt.Dimension(29, 29));
        jButtonCommentSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCommentSelectionActionPerformed(evt);
            }
        });
        jButtonCommentSelection.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarInform.add(jButtonCommentSelection);

        jButtonUncommentSelection.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/uncomment.png")));
        jButtonUncommentSelection.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_UNCOMMENT_SELECTION"));
        jButtonUncommentSelection.setBorderPainted(false);
        jButtonUncommentSelection.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonUncommentSelection.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonUncommentSelection.setPreferredSize(new java.awt.Dimension(29, 29));
        jButtonUncommentSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonUncommentSelectionActionPerformed(evt);
            }
        });
        jButtonUncommentSelection.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarInform.add(jButtonUncommentSelection);

        jButtonLeftTab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/leftIndent.png")));
        jButtonLeftTab.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_LEFTTAB_SELECTION"));
        jButtonLeftTab.setBorderPainted(false);
        jButtonLeftTab.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonLeftTab.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonLeftTab.setPreferredSize(new java.awt.Dimension(29, 29));
        jButtonLeftTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLeftTabActionPerformed(evt);
            }
        });
        jButtonLeftTab.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarInform.add(jButtonLeftTab);

        jButtonRightTab.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rightIndent.png")));
        jButtonRightTab.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_RIGHTTAB_SELECTION"));
        jButtonRightTab.setBorderPainted(false);
        jButtonRightTab.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonRightTab.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonRightTab.setPreferredSize(new java.awt.Dimension(29, 29));
        jButtonRightTab.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRightTabActionPerformed(evt);
            }
        });
        jButtonRightTab.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarInform.add(jButtonRightTab);

        jButtonBracketCheck.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/check.png")));
        jButtonBracketCheck.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_CHECK_BRACKETS"));
        jButtonBracketCheck.setBorderPainted(false);
        jButtonBracketCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBracketCheckActionPerformed(evt);
            }
        });
        jButtonBracketCheck.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarInform.add(jButtonBracketCheck);

        RebuildButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/compfile.png")));
        RebuildButton.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_BUILDALL"));
        RebuildButton.setBorderPainted(false);
        RebuildButton.setMaximumSize(new java.awt.Dimension(29, 29));
        RebuildButton.setMinimumSize(new java.awt.Dimension(29, 29));
        RebuildButton.setPreferredSize(new java.awt.Dimension(29, 29));
        RebuildButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RebuildButtonActionPerformed(evt);
            }
        });
        RebuildButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarInform.add(RebuildButton);

        RunButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/launch.png")));
        RunButton.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_RUN"));
        RunButton.setBorderPainted(false);
        RunButton.setMaximumSize(new java.awt.Dimension(29, 29));
        RunButton.setMinimumSize(new java.awt.Dimension(29, 29));
        RunButton.setPreferredSize(new java.awt.Dimension(29, 29));
        RunButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RunButtonActionPerformed(evt);
            }
        });
        RunButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarInform.add(RunButton);

        jButtonInsertSymbol.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/insertSymbol.png")));
        jButtonInsertSymbol.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_INSERT_SYMBOL"));
        jButtonInsertSymbol.setBorderPainted(false);
        jButtonInsertSymbol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonInsertSymbolActionPerformed(evt);
            }
        });
        jButtonInsertSymbol.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarInform.add(jButtonInsertSymbol);

        jButtonInfo.setFont(new java.awt.Font("Dialog", 0, 10));
        jButtonInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/infoFile.png")));
        jButtonInfo.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_INFO"));
        jButtonInfo.setBorderPainted(false);
        jButtonInfo.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonInfo.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonInfo.setPreferredSize(new java.awt.Dimension(29, 29));
        jButtonInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonInfoActionPerformed(evt);
            }
        });
        jButtonInfo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarInform.add(jButtonInfo);

        jButtonExtractStrings.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/extractString.png")));
        jButtonExtractStrings.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_EXTRACT_STRINGS"));
        jButtonExtractStrings.setBorderPainted(false);
        jButtonExtractStrings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExtractStringsActionPerformed(evt);
            }
        });
        jButtonExtractStrings.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarInform.add(jButtonExtractStrings);

        jButtonTranslate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/translate.png")));
        jButtonTranslate.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_TRANSLATE"));
        jButtonTranslate.setBorderPainted(false);
        jButtonTranslate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTranslateActionPerformed(evt);
            }
        });
        jButtonTranslate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarInform.add(jButtonTranslate);

        jButtonInterpreter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/runInterpreter.png")));
        jButtonInterpreter.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_RUN_INTERPRETER"));
        jButtonInterpreter.setBorderPainted(false);
        jButtonInterpreter.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonInterpreter.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonInterpreter.setPreferredSize(new java.awt.Dimension(29, 29));
        jButtonInterpreter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonInterpreterActionPerformed(evt);
            }
        });
        jButtonInterpreter.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarInform.add(jButtonInterpreter);

        jButtonSwitchManager.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/edit.png")));
        jButtonSwitchManager.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_SWITCHES_TOOLTIP"));
        jButtonSwitchManager.setBorderPainted(false);
        jButtonSwitchManager.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonSwitchManager.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonSwitchManager.setPreferredSize(new java.awt.Dimension(29, 29));
        jButtonSwitchManager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSwitchManagerActionPerformed(evt);
            }
        });
        jButtonSwitchManager.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarInform.add(jButtonSwitchManager);

        jButtonOption.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/configure.png")));
        jButtonOption.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_SETTING"));
        jButtonOption.setBorderPainted(false);
        jButtonOption.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonOption.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonOption.setPreferredSize(new java.awt.Dimension(29, 29));
        jButtonOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOptionActionPerformed(evt);
            }
        });
        jButtonOption.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jToolBarInform.add(jButtonOption);

        jTextFieldRowCol.setEditable(false);
        jTextFieldRowCol.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldRowCol.setBorder(null);
        jTextFieldRowCol.setDisabledTextColor(new java.awt.Color(212, 208, 200));
        jTextFieldRowCol.setMaximumSize(new java.awt.Dimension(190, 190));
        jTextFieldRowCol.setMinimumSize(new java.awt.Dimension(20, 22));
        jTextFieldRowCol.setPreferredSize(new java.awt.Dimension(80, 29));
        jToolBarInform.add(jTextFieldRowCol);

        jPanel22.add(jToolBarInform, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel22, java.awt.BorderLayout.NORTH);

        jSplitPane3.setDividerSize(3);
        jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane3.setDoubleBuffered(true);
        jSplitPane1.setDividerSize(3);
        jSplitPane1.setDoubleBuffered(true);
        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        jTabbedPane1.setFont(new java.awt.Font("Dialog", 1, 11));
        jTabbedPane1.setMinimumSize(new java.awt.Dimension(350, 350));
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(700, 450));
        jTabbedPane1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                jTabbedPane1ComponentShown(evt);
            }
        });
        jTabbedPane1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTabbedPane1MouseClicked(evt);
            }
        });

        jSplitPane1.setRightComponent(jTabbedPane1);

        jSplitPane2.setBorder(null);
        jSplitPane2.setDividerSize(3);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jPanelTreeControl.setLayout(new javax.swing.BoxLayout(jPanelTreeControl, javax.swing.BoxLayout.Y_AXIS));

        jScrollPane3.setBorder(null);
        jScrollPane3.setDoubleBuffered(true);
        jScrollPane3.setMinimumSize(new java.awt.Dimension(150, 200));
        jScrollPane3.setPreferredSize(new java.awt.Dimension(150, 300));
        jTree1.setFont(new java.awt.Font("Courier New", 0, 12));
        jTree1.setMaximumSize(new java.awt.Dimension(0, 0));
        jTree1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jTree1MouseEntered(evt);
            }
        });
        jTree1.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTree1ValueChanged(evt);
            }
        });

        jScrollPane3.setViewportView(jTree1);

        jPanelTreeControl.add(jScrollPane3);

        jSplitPane2.setTopComponent(jPanelTreeControl);

        jPanelMainFile.setLayout(new javax.swing.BoxLayout(jPanelMainFile, javax.swing.BoxLayout.Y_AXIS));

        jScrollPaneProject.setBorder(new javax.swing.border.TitledBorder(null, "Project", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 10)));
        jScrollPaneProject.setPreferredSize(new java.awt.Dimension(90, 131));
        jListProject.setFont(new java.awt.Font("Dialog", 0, 11));
        jListProject.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListProjectMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jListProjectMouseEntered(evt);
            }
        });

        jScrollPaneProject.setViewportView(jListProject);

        jPanelMainFile.add(jScrollPaneProject);

        jLabelMainFile.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabelMainFile.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelMainFile.setText("Main:");
        jLabelMainFile.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jPanelMainFile.add(jLabelMainFile);

        jSplitPane2.setBottomComponent(jPanelMainFile);

        jSplitPane1.setLeftComponent(jSplitPane2);

        jSplitPane3.setTopComponent(jSplitPane1);

        jTabbedPane2.setBorder(new javax.swing.border.EtchedBorder());
        jTabbedPane2.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        jTabbedPane2.setAutoscrolls(true);
        jTabbedPane2.setFont(new java.awt.Font("Courier New", 1, 12));
        jTabbedPane2.setMinimumSize(new java.awt.Dimension(31, 100));
        jTabbedPane2.setPreferredSize(new java.awt.Dimension(30, 150));
        jScrollPane2.setAutoscrolls(true);
        jTextAreaOutput.setEditable(false);
        jTextAreaOutput.setFont(new java.awt.Font("Tahoma", 0, 11));
        jTextAreaOutput.setTabSize(4);
        jTextAreaOutput.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_OUTPUT"));
        jTextAreaOutput.setAutoscrolls(false);
        jTextAreaOutput.setMinimumSize(new java.awt.Dimension(0, 45));
        jTextAreaOutput.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextAreaOutputMouseClicked(evt);
            }
        });

        jScrollPane2.setViewportView(jTextAreaOutput);

        jTabbedPane2.addTab("Compiler Output", jScrollPane2);
        jScrollPane2.getAccessibleContext().setAccessibleParent(jTabbedPane2);

        jSplitPane3.setBottomComponent(jTabbedPane2);

        getContentPane().add(jSplitPane3, java.awt.BorderLayout.CENTER);

        jMenuBar1.setBorder(null);
        jMenuBar1.setFont(new java.awt.Font("Dialog", 0, 12));
        jMenuFile.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENU_FILE"));
        jMenuFile.setDelay(0);
        jMenuFile.setFont(new java.awt.Font("Dialog", 0, 11));
        New.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        New.setFont(new java.awt.Font("Dialog", 0, 11));
        New.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_NEW"));
        New.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NewActionPerformed(evt);
            }
        });

        jMenuFile.add(New);

        Open.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        Open.setFont(new java.awt.Font("Dialog", 0, 11));
        Open.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_OPEN"));
        Open.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_OPEN_TOOLTIP"));
        Open.setName("Open");
        Open.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenActionPerformed(evt);
            }
        });

        jMenuFile.add(Open);

        jMenuFile.add(jSeparator8);

        Save.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        Save.setFont(new java.awt.Font("Dialog", 0, 11));
        Save.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_SAVE"));
        Save.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveActionPerformed(evt);
            }
        });

        jMenuFile.add(Save);

        SaveAs.setFont(new java.awt.Font("Dialog", 0, 11));
        SaveAs.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_SAVEAS"));
        SaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveAsActionPerformed(evt);
            }
        });

        jMenuFile.add(SaveAs);

        jMenuItemSaveAll.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemSaveAll.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemSaveAll.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_SAVEALL"));
        jMenuItemSaveAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveAllActionPerformed(evt);
            }
        });

        jMenuFile.add(jMenuItemSaveAll);

        jMenuFile.add(jSeparator10);

        jMenuItemClose.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemClose.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemClose.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_CLOSE"));
        jMenuItemClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCloseActionPerformed(evt);
            }
        });

        jMenuFile.add(jMenuItemClose);

        jMenuItemCloseAll.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemCloseAll.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_CLOSEALL"));
        jMenuItemCloseAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCloseAllActionPerformed(evt);
            }
        });

        jMenuFile.add(jMenuItemCloseAll);

        jMenuFile.add(jSeparator9);

        jMenuRecentFiles.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_RECENTFILES"));
        jMenuRecentFiles.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuFile.add(jMenuRecentFiles);

        jMenuItemClearRecentFiles.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemClearRecentFiles.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_CLEARRECENTFILES"));
        jMenuItemClearRecentFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemClearRecentFilesActionPerformed(evt);
            }
        });

        jMenuFile.add(jMenuItemClearRecentFiles);

        jMenuFile.add(jSeparator1);

        jMenuItemPrint.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemPrint.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemPrint.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_PRINT"));
        jMenuItemPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPrintActionPerformed(evt);
            }
        });

        jMenuFile.add(jMenuItemPrint);

        jMenuFile.add(jSeparator4);

        Exit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK));
        Exit.setFont(new java.awt.Font("Dialog", 0, 11));
        Exit.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_EXIT"));
        Exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExitActionPerformed(evt);
            }
        });

        jMenuFile.add(Exit);

        jMenuBar1.add(jMenuFile);

        jMenuEdit.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENU_EDIT"));
        jMenuEdit.setDelay(0);
        jMenuEdit.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemCut.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemCut.setText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_EDIT_CUT"));
        jMenuEdit.add(jMenuItemCut);

        jMenuItemCopy1.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemCopy1.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_COPY"));
        jMenuItemCopy1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCopy1ActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemCopy1);

        jMenuItemPaste.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemPaste.setText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_EDIT_PASTE"));
        jMenuEdit.add(jMenuItemPaste);

        jMenuEdit.add(jSeparator11);

        jMenuItemSearch.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0));
        jMenuItemSearch.setText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_SEARCH"));
        jMenuItemSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSearchActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemSearch);

        jMenuItemSearchAllFiles.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemSearchAllFiles.setText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_SEARCHALL"));
        jMenuItemSearchAllFiles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSearchAllFilesActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemSearchAllFiles);

        jMenuItemReplace.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemReplace.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemReplace.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_REPLACE"));
        jMenuItemReplace.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_REPLACE_TOOLTIP"));
        jMenuItemReplace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemReplaceActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemReplace);

        jMenuItemSelectAll.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemSelectAll.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemSelectAll.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_SELECTALL"));
        jMenuItemSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSelectAllActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemSelectAll);

        jMenuItemClearAll.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemClearAll.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_DELETEALL"));
        jMenuItemClearAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemClearAllActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemClearAll);

        jMenuEdit.add(jSeparator16);

        jMenuItemCommentSelection.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemCommentSelection.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemCommentSelection.setText(java.util.ResourceBundle.getBundle("JIF").getString("POPUPMENU_MENUITEM_COMMENT"));
        jMenuItemCommentSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCommentSelectionActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemCommentSelection);

        jMenuItemUncommentSelection.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemUncommentSelection.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemUncommentSelection.setText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_EDIT_UNCOMMENT_SELECTION"));
        jMenuItemUncommentSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemUncommentSelectionActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemUncommentSelection);

        jMenuItemRightShift.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_RIGHTSHIFT"));
        jMenuItemRightShift.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRightShiftActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemRightShift);

        jMenuItemLeftShift.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_LEFTSHIFT"));
        jMenuItemLeftShift.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLeftShiftActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemLeftShift);

        jMenuEdit.add(jSeparator17);

        jMenuItemInsertFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemInsertFile.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemInsertFile.setText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_INSERT_FROM_FILE"));
        jMenuItemInsertFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemInsertFileActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemInsertFile);

        jMenuItemInsertSymbol1.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemInsertSymbol1.setText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_INSERT_SYMBOL"));
        jMenuItemInsertSymbol1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemInsertSymbol1ActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemInsertSymbol1);

        jMenuItemSetBookmark.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemSetBookmark.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_SETBOOKMARK"));
        jMenuItemSetBookmark.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSetBookmarkActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemSetBookmark);

        jMenuItemNextBookmark.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
        jMenuItemNextBookmark.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_NEXTBOOKMARK"));
        jMenuItemNextBookmark.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemNextBookmarkActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemNextBookmark);

        jMenuBar1.add(jMenuEdit);

        jMenuView.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENU_VIEW"));
        jMenuView.setDelay(0);
        jMenuView.setFont(new java.awt.Font("Dialog", 0, 11));
        jCheckBoxOutput.setFont(new java.awt.Font("Dialog", 0, 11));
        jCheckBoxOutput.setSelected(true);
        jCheckBoxOutput.setText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_OUTPUT"));
        jCheckBoxOutput.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_OUTPUT_TOOLTIP"));
        jCheckBoxOutput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxOutputActionPerformed(evt);
            }
        });
        jCheckBoxOutput.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxOutputStateChanged(evt);
            }
        });

        jMenuView.add(jCheckBoxOutput);

        jCheckBoxJToolBar.setFont(new java.awt.Font("Dialog", 0, 11));
        jCheckBoxJToolBar.setSelected(true);
        jCheckBoxJToolBar.setText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_JTOOLBAR"));
        jCheckBoxJToolBar.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_JTOOLBAR_TOOLTIP"));
        jCheckBoxJToolBar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxJToolBarActionPerformed(evt);
            }
        });
        jCheckBoxJToolBar.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxJToolBarStateChanged(evt);
            }
        });

        jMenuView.add(jCheckBoxJToolBar);

        jCheckBoxJToolBarInform.setFont(new java.awt.Font("Dialog", 0, 11));
        jCheckBoxJToolBarInform.setSelected(true);
        jCheckBoxJToolBarInform.setText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_JTOOLBAR_INFORM"));
        jCheckBoxJToolBarInform.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_JTOOLBAR_INFORM_TOOLTIP"));
        jCheckBoxJToolBarInform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxJToolBarInformActionPerformed(evt);
            }
        });
        jCheckBoxJToolBarInform.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxJToolBarInformStateChanged(evt);
            }
        });

        jMenuView.add(jCheckBoxJToolBarInform);

        jCheckBoxJTree.setFont(new java.awt.Font("Dialog", 0, 11));
        jCheckBoxJTree.setSelected(true);
        jCheckBoxJTree.setText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_JTREE"));
        jCheckBoxJTree.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_JTREE_TOOLTIP"));
        jCheckBoxJTree.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxJTreeActionPerformed(evt);
            }
        });
        jCheckBoxJTree.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxJTreeStateChanged(evt);
            }
        });

        jMenuView.add(jCheckBoxJTree);

        jCheckBoxToggleFullscreen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F11, 0));
        jCheckBoxToggleFullscreen.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_TOGGLEFULLSCREEN"));
        jCheckBoxToggleFullscreen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxToggleFullscreenActionPerformed(evt);
            }
        });

        jMenuView.add(jCheckBoxToggleFullscreen);

        jMenuBar1.add(jMenuView);

        jMenuProject.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_PROJECT"));
        jMenuProject.setDelay(0);
        jMenuProject.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemNewProject.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemNewProject.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemNewProject.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_NEW_PROJECT"));
        jMenuItemNewProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemNewProjectActionPerformed(evt);
            }
        });

        jMenuProject.add(jMenuItemNewProject);

        jMenuItemOpenProject.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        jMenuItemOpenProject.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemOpenProject.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_OPEN_PROJECT"));
        jMenuItemOpenProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemOpenProjectActionPerformed(evt);
            }
        });

        jMenuProject.add(jMenuItemOpenProject);

        jMenuItemSaveProject.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemSaveProject.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_SAVE_PROJECT"));
        jMenuItemSaveProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveProjectActionPerformed(evt);
            }
        });

        jMenuProject.add(jMenuItemSaveProject);

        jMenuItemCloseProject.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemCloseProject.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_CLOSE_PROJECT"));
        jMenuItemCloseProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCloseProjectActionPerformed(evt);
            }
        });

        jMenuProject.add(jMenuItemCloseProject);

        jMenuProject.add(jSeparator14);

        jMenuItemAddNewToProject.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemAddNewToProject.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_ADD_NEWFILE_TO_PROJECT"));
        jMenuItemAddNewToProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddNewToProjectActionPerformed(evt);
            }
        });

        jMenuProject.add(jMenuItemAddNewToProject);

        jMenuItemAddFileToProject.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemAddFileToProject.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_ADD_FILE_TO_PROJECT"));
        jMenuItemAddFileToProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddFileToProjectActionPerformed(evt);
            }
        });

        jMenuProject.add(jMenuItemAddFileToProject);

        jMenuItemRemoveFromProject.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemRemoveFromProject.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_POPUP_REMOVE"));
        jMenuItemRemoveFromProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRemoveFromProjectActionPerformed(evt);
            }
        });

        jMenuProject.add(jMenuItemRemoveFromProject);

        jMenuBar1.add(jMenuProject);

        jMenuMode.setText("Mode");
        jMenuMode.setEnabled(false);
        jMenuMode.setFont(new java.awt.Font("Dialog", 0, 11));
        jCheckBoxInformMode.setFont(new java.awt.Font("Dialog", 0, 11));
        jCheckBoxInformMode.setText("Inform Mode");
        jCheckBoxInformMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxInformModeActionPerformed(evt);
            }
        });
        jCheckBoxInformMode.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jCheckBoxInformModePropertyChange(evt);
            }
        });

        jMenuMode.add(jCheckBoxInformMode);

        jCheckBoxGlulxMode.setFont(new java.awt.Font("Dialog", 0, 11));
        jCheckBoxGlulxMode.setText("Glulx Mode");
        jCheckBoxGlulxMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxGlulxModeActionPerformed(evt);
            }
        });

        jMenuMode.add(jCheckBoxGlulxMode);

        jMenuBar1.add(jMenuMode);

        jMenuBuild.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENU_BUILD"));
        jMenuBuild.setDelay(0);
        jMenuBuild.setFont(new java.awt.Font("Dialog", 0, 11));
        BuildAll.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F9, 0));
        BuildAll.setFont(new java.awt.Font("Dialog", 0, 11));
        BuildAll.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_BUILDALL"));
        BuildAll.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_BUILDALL_TOOLTIP"));
        BuildAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BuildAllActionPerformed(evt);
            }
        });

        jMenuBuild.add(BuildAll);

        jMenuItemSwitches.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemSwitches.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_SWITCHES"));
        jMenuItemSwitches.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_SWITCHES_TOOLTIP"));
        jMenuItemSwitches.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSwitchesActionPerformed(evt);
            }
        });

        jMenuBuild.add(jMenuItemSwitches);

        jMenuBuild.add(jSeparator2);

        Run.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F9, java.awt.event.InputEvent.CTRL_MASK));
        Run.setFont(new java.awt.Font("Dialog", 0, 11));
        Run.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_RUN"));
        Run.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_RUN_TOOLTIP"));
        Run.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                RunActionPerformed(evt);
            }
        });

        jMenuBuild.add(Run);

        jMenuBar1.add(jMenuBuild);

        jMenuGlulx.setText("Glulx");
        jMenuGlulx.setEnabled(false);
        jMenuGlulx.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemBuildAllGlulx.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F12, 0));
        jMenuItemBuildAllGlulx.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemBuildAllGlulx.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_BUILD_ALL"));
        jMenuItemBuildAllGlulx.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_BUILD_ALL_TOOLTIP"));
        jMenuItemBuildAllGlulx.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemBuildAllGlulxActionPerformed(evt);
            }
        });

        jMenuGlulx.add(jMenuItemBuildAllGlulx);

        jMenuGlulx.add(jSeparator18);

        jMenuItemMakeResource.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemMakeResource.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_MAKE_RES"));
        jMenuItemMakeResource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemMakeResourceActionPerformed(evt);
            }
        });

        jMenuGlulx.add(jMenuItemMakeResource);

        jMenuItemCompile.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemCompile.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_COMPILE_INF"));
        jMenuItemCompile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCompileActionPerformed(evt);
            }
        });

        jMenuGlulx.add(jMenuItemCompile);

        jMenuItemMakeBlb.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F12, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemMakeBlb.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemMakeBlb.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_MAKE_BLB"));
        jMenuItemMakeBlb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemMakeBlbActionPerformed(evt);
            }
        });

        jMenuGlulx.add(jMenuItemMakeBlb);

        jMenuGlulx.add(jSeparator15);

        jMenuItemRunUlx.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemRunUlx.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_RUN_ULX"));
        jMenuItemRunUlx.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRunUlxActionPerformed(evt);
            }
        });

        jMenuGlulx.add(jMenuItemRunUlx);

        jMenuItemRunBlb.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemRunBlb.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_RUN_BLB"));
        jMenuItemRunBlb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRunBlbActionPerformed(evt);
            }
        });

        jMenuGlulx.add(jMenuItemRunBlb);

        jMenuBar1.add(jMenuGlulx);

        jMenuOptions.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENU_OPTIONS"));
        jMenuOptions.setDelay(0);
        jMenuOptions.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemSwitch.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemSwitch.setText(java.util.ResourceBundle.getBundle("JIF").getString("CONFIG_SWITCHES_INI"));
        jMenuItemSwitch.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_SWITCH_TOOLTIP"));
        jMenuItemSwitch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSwitchActionPerformed(evt);
            }
        });

        jMenuOptions.add(jMenuItemSwitch);

        jMenuItemAltKeys.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemAltKeys.setText("altkeys.ini");
        jMenuItemAltKeys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAltKeysActionPerformed(evt);
            }
        });

        jMenuOptions.add(jMenuItemAltKeys);

        jMenuItemSyntax.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemSyntax.setText(java.util.ResourceBundle.getBundle("JIF").getString("SYNTAX_FILE"));
        jMenuItemSyntax.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_SYNTAX_TOOLTIP"));
        jMenuItemSyntax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSyntaxActionPerformed(evt);
            }
        });

        jMenuOptions.add(jMenuItemSyntax);

        jMenuItemLinks.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemLinks.setText(java.util.ResourceBundle.getBundle("JIF").getString("CONFIG_LINKS"));
        jMenuItemLinks.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_LINKS_TOOLTIP"));
        jMenuItemLinks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLinksActionPerformed(evt);
            }
        });

        jMenuOptions.add(jMenuItemLinks);

        jMenuItemHelpedCode.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemHelpedCode.setText(java.util.ResourceBundle.getBundle("JIF").getString("HELPED_FILE"));
        jMenuItemHelpedCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemHelpedCodeActionPerformed(evt);
            }
        });

        jMenuOptions.add(jMenuItemHelpedCode);

        jMenuOptions.add(jSeparator12);

        jMenuItemSettings.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemSettings.setText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_SETTING"));
        jMenuItemSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSettingsActionPerformed(evt);
            }
        });

        jMenuOptions.add(jMenuItemSettings);

        jMenuBar1.add(jMenuOptions);

        jMenuLinks.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_LINKS"));
        jMenuLinks.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuLinks.setDelay(0);
        jMenuBar1.add(jMenuLinks);

        jMenuTutorial.setText("Tutorial");
        jMenuTutorial.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuTutorial.setDelay(0);
        jMenuBar1.add(jMenuTutorial);

        jMenuHelp.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENU_HELP"));
        jMenuHelp.setDelay(0);
        jMenuHelp.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemHelp.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemHelp.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_HELP"));
        jMenuItemHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemHelpActionPerformed(evt);
            }
        });

        jMenuHelp.add(jMenuItemHelp);

        jMenuItemConfigurazione.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        jMenuItemConfigurazione.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemConfigurazione.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_CONFIGURATION"));
        jMenuItemConfigurazione.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemConfigurazioneActionPerformed(evt);
            }
        });

        jMenuHelp.add(jMenuItemConfigurazione);

        jMenuItemCopyright.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemCopyright.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_COPYRIGHTS"));
        jMenuItemCopyright.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCopyrightActionPerformed(evt);
            }
        });

        jMenuHelp.add(jMenuItemCopyright);

        jMenuHelp.add(jSeparator7);

        About.setFont(new java.awt.Font("Dialog", 0, 11));
        About.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_ABOUT"));
        About.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AboutActionPerformed(evt);
            }
        });

        jMenuHelp.add(About);

        jMenuBar1.add(jMenuHelp);

        setJMenuBar(jMenuBar1);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    private void jButtonSearchProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSearchProjectActionPerformed
        String target = this.jTextFieldFind.getText();
        if(null!=target && !target.trim().equals("")){
            // if output window is hide, I'll show it
            if (!jCheckBoxOutput.getState()){
                jSplitPane3.setBottomComponent(jTabbedPane2);
                jTabbedPane2.setVisible(true);
            }            
            this.searchAllFiles(target);
        }
    }//GEN-LAST:event_jButtonSearchProjectActionPerformed

    private void jMenuItemSearchAllFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSearchAllFilesActionPerformed
        String target = this.jTextFieldFind.getText();
        if(null!=target && !target.trim().equals("")){
            // if output window is hide, I'll show it
            if (!jCheckBoxOutput.getState()){
                jSplitPane3.setBottomComponent(jTabbedPane2);
                jTabbedPane2.setVisible(true);
            }            
            this.searchAllFiles(target);
        }
    }//GEN-LAST:event_jMenuItemSearchAllFilesActionPerformed

    private void jMenuItemSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSearchActionPerformed
        getCurrentJIFTextPane().findString();
    }//GEN-LAST:event_jMenuItemSearchActionPerformed

    private void jCheckBoxNumberLinesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxNumberLinesActionPerformed
            if (jCheckBoxNumberLines.isSelected()){
                jCheckBoxWrapLines.setSelected(false);
            }
    }//GEN-LAST:event_jCheckBoxNumberLinesActionPerformed

    private void jCheckBoxInformModePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jCheckBoxInformModePropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBoxInformModePropertyChange

    private void jMenuItemCut1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCut1ActionPerformed
        getCurrentJIFTextPane().cut();
    }//GEN-LAST:event_jMenuItemCut1ActionPerformed

    private void jCheckBoxWrapLinesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxWrapLinesActionPerformed
            if (jCheckBoxWrapLines.isSelected()){
                jCheckBoxNumberLines.setSelected(false);
            }
    }//GEN-LAST:event_jCheckBoxWrapLinesActionPerformed

    private void jMenuItemHelpedCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemHelpedCodeActionPerformed
        try{
            loadConfigFiles(workingDir+"config"+Constants.SEP+java.util.ResourceBundle.getBundle("JIF").getString("HELPED_FILE"));
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
    }//GEN-LAST:event_jMenuItemHelpedCodeActionPerformed

    private void jMenuItemLeftShiftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLeftShiftActionPerformed
        if (null != getCurrentJIFTextPane()){
            getCurrentJIFTextPane().removeTabSelection();    
        } 
    }//GEN-LAST:event_jMenuItemLeftShiftActionPerformed

    private void jMenuItemRightShiftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRightShiftActionPerformed
        if (null != getCurrentJIFTextPane()){
            getCurrentJIFTextPane().tabSelection();    
        } 
    }//GEN-LAST:event_jMenuItemRightShiftActionPerformed

    private void jCheckBoxToggleFullscreenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxToggleFullscreenActionPerformed
        if (!jCheckBoxToggleFullscreen.getState()) {
            jCheckBoxOutput.setSelected(true);
            jCheckBoxJToolBar.setSelected(true);
            jCheckBoxJToolBarInform.setSelected(true);            
            jCheckBoxJTree.setSelected(true);                        
        }
        else{
            jCheckBoxOutput.setSelected(false);
            jCheckBoxJToolBar.setSelected(false);
            jCheckBoxJToolBarInform.setSelected(false);            
            jCheckBoxJTree.setSelected(false);                       
        }
    }//GEN-LAST:event_jCheckBoxToggleFullscreenActionPerformed

    private void jMenuItemNextBookmarkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNextBookmarkActionPerformed
        if (null != getCurrentJIFTextPane()){
            getCurrentJIFTextPane().nextBookmark();            
        }
    }//GEN-LAST:event_jMenuItemNextBookmarkActionPerformed

    private void jMenuItemSetBookmarkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSetBookmarkActionPerformed
        if (null != getCurrentJIFTextPane()){
            getCurrentJIFTextPane().setBookmark();
        }
    }//GEN-LAST:event_jMenuItemSetBookmarkActionPerformed

    private void jButtonDefinitionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDefinitionActionPerformed
        // Search for definition
        if (!jTextFieldDefinition.getText().equals("")){
            checkTree(jTextFieldDefinition.getText());    
        }   
    }//GEN-LAST:event_jButtonDefinitionActionPerformed

    private void jTextFieldDefinitionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldDefinitionActionPerformed
        // Search for definition
        if (!jTextFieldDefinition.getText().equals("")){
            checkTree(jTextFieldDefinition.getText());    
        }        
    }//GEN-LAST:event_jTextFieldDefinitionActionPerformed

    private void jButton28ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton28ActionPerformed
        JFileChooser chooser = new JFileChooser(workingDir);
        chooser.setFileSelectionMode(chooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.CANCEL_OPTION) {
            return;
        }
        else {
            jTextFieldPathLibSecondary3.setText(chooser.getSelectedFile().getAbsolutePath());
            libPathSecondary3 = chooser.getSelectedFile().getAbsolutePath();
        }
    }//GEN-LAST:event_jButton28ActionPerformed

    private void jButton27ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton27ActionPerformed
        JFileChooser chooser = new JFileChooser(workingDir);
        chooser.setFileSelectionMode(chooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.CANCEL_OPTION) {
            return;
        }
        else {
            jTextFieldPathLibSecondary2.setText(chooser.getSelectedFile().getAbsolutePath());
            libPathSecondary2 = chooser.getSelectedFile().getAbsolutePath();
        }
    }//GEN-LAST:event_jButton27ActionPerformed

    private void jMenuItemJumpToSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemJumpToSourceActionPerformed
        try{
            checkTree(getCurrentJIFTextPane().getCurrentWord());    
        }
        catch (BadLocationException ble){
            System.err.println(ble);
        }        
    }//GEN-LAST:event_jMenuItemJumpToSourceActionPerformed

    private void jTree1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTree1MouseEntered
        refreshTree();
    }//GEN-LAST:event_jTree1MouseEntered

    private void jCheckBoxJTreeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxJTreeStateChanged
        if (!jCheckBoxJTree.getState()) jSplitPane1.setDividerLocation(0);
        if (jCheckBoxJTree.getState()) jSplitPane1.setDividerLocation(150);
    }//GEN-LAST:event_jCheckBoxJTreeStateChanged

    private void jCheckBoxJToolBarInformStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxJToolBarInformStateChanged
        if (!jCheckBoxJToolBarInform.getState()) jToolBarInform.setVisible(false);
        if (jCheckBoxJToolBarInform.getState()) jToolBarInform.setVisible(true);
    }//GEN-LAST:event_jCheckBoxJToolBarInformStateChanged

    private void jCheckBoxJToolBarStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxJToolBarStateChanged
        if (!jCheckBoxJToolBar.getState()) jToolBarCommon.setVisible(false);
        if (jCheckBoxJToolBar.getState()) jToolBarCommon.setVisible(true);
    }//GEN-LAST:event_jCheckBoxJToolBarStateChanged

    private void jCheckBoxOutputStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxOutputStateChanged
        if (!jCheckBoxOutput.getState()) {
            jTabbedPane2.setVisible(false);
        }
        if (jCheckBoxOutput.getState()) {
            jSplitPane3.setBottomComponent(jTabbedPane2);
            jTabbedPane2.setVisible(true);
        }
    }//GEN-LAST:event_jCheckBoxOutputStateChanged

    private void jButtonDefaultDarkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDefaultDarkActionPerformed
        // Dark settings
        colorKeyword = new Color(51,102,255);
        colorAttribute = new Color(204,0,51);
        colorProperty = new Color(204,204,204);
        colorRoutine = new Color(204,0,153);
        colorVerb = new Color(102,204,0);
        colorNormal = new Color(102,102,0);
        colorComment = new Color(153,153,153);
        colorBackground = new Color(0,0,0);
        updateColor();
        updateColorEditor();
    }//GEN-LAST:event_jButtonDefaultDarkActionPerformed

    private void jButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonMouseExited
        ((JButton)evt.getSource()).setBorderPainted(false);
    }//GEN-LAST:event_jButtonMouseExited

    private void jButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonMouseEntered
        ((JButton)evt.getSource()).setBorderPainted(true);
    }//GEN-LAST:event_jButtonMouseEntered

    private void SaveButtonAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveButtonAllActionPerformed
        saveAll();
    }//GEN-LAST:event_SaveButtonAllActionPerformed

    private void jMenuItemRemoveMainClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRemoveMainClassActionPerformed
        // Removes the Main file
        mainFile = null;
        JOptionPane.showMessageDialog(this,"Removing Main file","Main file", JOptionPane.INFORMATION_MESSAGE);
        jLabelMainFile.setText("Main: ");
    }//GEN-LAST:event_jMenuItemRemoveMainClassActionPerformed

    private void jMenuItemSetMainClassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSetMainClassActionPerformed
        FileProject fp = (FileProject)jListProject.getSelectedValue();
        if (null==fp) return;
        if ( (fp.path.indexOf(".inf")!=-1) || (fp.path.indexOf(".INF")!=-1)){
            mainFile = fp.path;
            jLabelMainFile.setText("Main: "+fp.name);
        }
    }//GEN-LAST:event_jMenuItemSetMainClassActionPerformed

    private void jMenuItemBuildAllGlulxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemBuildAllGlulxActionPerformed
        //saveFile();
    	saveAll(); //FIXED BUG save all files in project
        // Run 3 steps
        // 1) Make Resource: if the option has been checked
        // bres source
        if (jCheckBoxMakeResource.isSelected()){
            makeResources();
        }
        // 2) Compile ulx file
        rebuildAll();
        // 3) make blb file
        makeBlb();
    }//GEN-LAST:event_jMenuItemBuildAllGlulxActionPerformed

    private void jMenuItemRunBlbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRunBlbActionPerformed
        saveFile();
        runBlb();
    }//GEN-LAST:event_jMenuItemRunBlbActionPerformed

    private void jMenuItemRunUlxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRunUlxActionPerformed
        saveFile();
        runAdventure();
    }//GEN-LAST:event_jMenuItemRunUlxActionPerformed

    private void jMenuItemCompileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCompileActionPerformed
        //saveFile();
    	saveAll(); //FIXED BUG save all files in project
        rebuildAll();
    }//GEN-LAST:event_jMenuItemCompileActionPerformed

    private void jMenuItemMakeBlbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMakeBlbActionPerformed
        makeBlb();
    }//GEN-LAST:event_jMenuItemMakeBlbActionPerformed

    private void jMenuItemMakeResourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMakeResourceActionPerformed
        // make the resource file and visualize the log in the output window
        makeResources();
    }//GEN-LAST:event_jMenuItemMakeResourceActionPerformed

    private void jButtonBlcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBlcActionPerformed
        JFileChooser chooser = new JFileChooser(workingDir);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.CANCEL_OPTION) {
            return;
        }
        else {
            jTextFieldBlc.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonBlcActionPerformed

    private void jButtonBresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBresActionPerformed
        JFileChooser chooser = new JFileChooser(workingDir);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.CANCEL_OPTION) {
            return;
        }
        else {
            jTextFieldBres.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonBresActionPerformed

    private void jButton26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton26ActionPerformed
        JFileChooser chooser = new JFileChooser(workingDir);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.CANCEL_OPTION) {
            return;
        }
        else jTextFieldPathGlulx.setText(chooser.getSelectedFile().getAbsolutePath());
    }//GEN-LAST:event_jButton26ActionPerformed

    private void jCheckBoxInformModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxInformModeActionPerformed
        // Changing state of Mode (Inform/Glux)
        if (jCheckBoxInformMode.getState()){
            setInformMode();
            jCheckBoxGlulxMode.setState(false);
            jCheckBoxInformMode.setState(true);
            setJifVersion("Jif "+ Constants.JIFVERSION + "     Inform Mode");
            refreshTree();
        }
    }//GEN-LAST:event_jCheckBoxInformModeActionPerformed

    private void jCheckBoxGlulxModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxGlulxModeActionPerformed
        // Changing state of Mode (Inform/Glulx)
        if (jCheckBoxGlulxMode.getState()){
            setGlulxMode();
            jCheckBoxInformMode.setState(false);
            jCheckBoxGlulxMode.setState(true);
            setJifVersion("Jif "+ Constants.JIFVERSION + "     Glux Mode");
            refreshTree();
        }
    }//GEN-LAST:event_jCheckBoxGlulxModeActionPerformed

    private void jButton25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton25ActionPerformed
        JFileChooser chooser = new JFileChooser(workingDir);
        chooser.setFileSelectionMode(chooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.CANCEL_OPTION) {
            return;
        }
        else {
            jTextFieldPathLibSecondary1.setText(chooser.getSelectedFile().getAbsolutePath());
            libPathSecondary1 = chooser.getSelectedFile().getAbsolutePath();
        }
    }//GEN-LAST:event_jButton25ActionPerformed

    private void jMenuItemPopupCloseAllFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupCloseAllFilesActionPerformed
        closeAllFiles();
    }//GEN-LAST:event_jMenuItemPopupCloseAllFilesActionPerformed

    private void jButtonBracketCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBracketCheckActionPerformed
        getCurrentJIFTextPane().checkbrackets(this);
    }//GEN-LAST:event_jButtonBracketCheckActionPerformed

    private void jCheckBoxJTreeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxJTreeActionPerformed
        if (!jCheckBoxJTree.getState()) jSplitPane1.setDividerLocation(0);
        if (jCheckBoxJTree.getState()) jSplitPane1.setDividerLocation(150);
    }//GEN-LAST:event_jCheckBoxJTreeActionPerformed

    private void jButtonBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBackgroundActionPerformed
        Color temp = JColorChooser.showDialog(this, "Color Dialog", colorBackground);
        if (temp != null) colorBackground= temp;
        updateColor();
        updateColorEditor();
    }//GEN-LAST:event_jButtonBackgroundActionPerformed

    private void jButtonTranslateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTranslateActionPerformed
        //getCurrentJIFTextPane().InsertTranslate(new File(gamesDir+Constants.SEP+"translate.txt"), new File(gamesDir+Constants.SEP+"translated.inf"));
    	getCurrentJIFTextPane().InsertTranslate(new File(getCurrentFilename()+"_translate.txt"), new File(getCurrentFilename()+"_translated.inf"));
    }//GEN-LAST:event_jButtonTranslateActionPerformed

    private void jButtonExtractStringsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExtractStringsActionPerformed
        //getCurrentJIFTextPane().ExtractTranslate(new File(gamesDir+Constants.SEP+"translate.txt"));
    	getCurrentJIFTextPane().ExtractTranslate(new File(getCurrentFilename()+"_translate.txt"));
    }//GEN-LAST:event_jButtonExtractStringsActionPerformed

    private void jMenuItemAddNewToProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddNewToProjectActionPerformed
        // Creates a new file and append this to the project
        newAdventure();
        saveAs();
        projectFiles.add(new FileProject(this.getCurrentFilename()));
        jListProject.removeAll();
        // Sorting the vector
        Collections.sort(projectFiles,new Comparator(){
            public int compare(Object a, Object b) {
            String id1 = ((FileProject)a).toString();
            String id2 = ((FileProject)b).toString();
            return (id1).compareToIgnoreCase(id2) ;
            }
        });
        jListProject.setListData(projectFiles);

        // Update and save the project
        saveProject();
    }//GEN-LAST:event_jMenuItemAddNewToProjectActionPerformed

    private void jMenuItemPopupAddNewToProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupAddNewToProjectActionPerformed
        // Creates a new file and append this to the project
        newAdventure();
        saveAs();
        projectFiles.add(new FileProject(this.getCurrentFilename()));
        jListProject.removeAll();
        // Sorting the vector
        Collections.sort(projectFiles,new Comparator(){
            public int compare(Object a, Object b) {
            String id1 = ((FileProject)a).toString();
            String id2 = ((FileProject)b).toString();
            return (id1).compareToIgnoreCase(id2) ;
            }
        });
        jListProject.setListData(projectFiles);
    }//GEN-LAST:event_jMenuItemPopupAddNewToProjectActionPerformed

    private void jButtonLeftTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLeftTabActionPerformed
        if (null != getCurrentJIFTextPane()){
            getCurrentJIFTextPane().removeTabSelection();
        }   
    }//GEN-LAST:event_jButtonLeftTabActionPerformed

    private void jButtonRightTabActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRightTabActionPerformed
        if (null != getCurrentJIFTextPane()){
            getCurrentJIFTextPane().tabSelection();    
        }        
    }//GEN-LAST:event_jButtonRightTabActionPerformed

    private void jButtonPrintTutorialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintTutorialActionPerformed
        new Utils().printInform(this,"Jif - "+jDialogTutorial.getTitle(), jEditorPaneTutorial);
    }//GEN-LAST:event_jButtonPrintTutorialActionPerformed

    private void jButtonRedoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRedoActionPerformed
        if (getCurrentJIFTextPane()!=null)
        ((Action)(getCurrentJIFTextPane().getActionMap().get("Redo"))).actionPerformed(evt);
    }//GEN-LAST:event_jButtonRedoActionPerformed

    private void jButtonUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUndoActionPerformed
        if (getCurrentJIFTextPane()!=null)
        ((Action)(getCurrentJIFTextPane().getActionMap().get("Undo"))).actionPerformed(evt);
    }//GEN-LAST:event_jButtonUndoActionPerformed

    private void jButtonOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOptionActionPerformed
        jTextFieldPathLib.setText(libPath);
        jTextFieldPathLibSecondary1.setText(libPathSecondary1);
        jTextFieldPathLibSecondary2.setText(libPathSecondary2);
        jTextFieldPathLibSecondary3.setText(libPathSecondary3);
        jTextFieldPathGames.setText(gamesDir);
        jTextFieldPathCompiler.setText(compiler);
        jTextFieldPathInterpreter.setText(interpreter);
        jTextFieldPathGlulx.setText(glulx);
        jTextFieldPathBrowser.setText(defaultBrowser);

        // set the colors
        updateColor();
        updateColorEditor();
        Font tmpFont = defaultFont;
        jComboBoxFont.setSelectedItem(tmpFont.getName());
        jComboBoxFontSize.setSelectedItem(String.valueOf(tmpFont.getSize()));

        jTextFieldFont.setFont( new Font((String)jComboBoxFont.getSelectedItem(), Font.PLAIN, Integer.parseInt((String)jComboBoxFontSize.getSelectedItem()) ) );
        //jDialogOption.pack();
        jDialogOption.setSize(580,550);           
        jDialogOption.setLocationRelativeTo(this);
        jDialogOption.setVisible(true);
    }//GEN-LAST:event_jButtonOptionActionPerformed

    private void jButton24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton24ActionPerformed
        jDialogTutorial.setVisible(false);
    }//GEN-LAST:event_jButton24ActionPerformed

    private void jComboBoxFontSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxFontSizeActionPerformed
        jTextFieldFont.setFont(new Font((String)jComboBoxFont.getSelectedItem(),Font.PLAIN, Integer.parseInt((String)jComboBoxFontSize.getSelectedItem()) ));
        defaultFont = new Font((String)jComboBoxFont.getSelectedItem(),Font.PLAIN,Integer.parseInt((String)jComboBoxFontSize.getSelectedItem()));
    }//GEN-LAST:event_jComboBoxFontSizeActionPerformed

    private void jComboBoxFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxFontActionPerformed
        jTextFieldFont.setFont(new Font((String)jComboBoxFont.getSelectedItem(),Font.PLAIN,Integer.parseInt((String)jComboBoxFontSize.getSelectedItem()) ) );
        defaultFont = new Font((String)jComboBoxFont.getSelectedItem(),Font.PLAIN,Integer.parseInt((String)jComboBoxFontSize.getSelectedItem()));
    }//GEN-LAST:event_jComboBoxFontActionPerformed

    private void jButton23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton23ActionPerformed
        unquote();
        // Updates colors
        colorKeyword = new Color(29,59,150);
        colorAttribute = new Color(153,0,153);
        colorProperty = new Color(37,158,33);
        colorRoutine = new Color(0,0,0);
        colorVerb = new Color(0,153,153);
        colorNormal = new Color(0,0,0);
        colorComment = new Color(153,153,153);
        colorBackground = new Color(255,255,255);
        updateColor();
        updateColorEditor();

        // updates the font
        jComboBoxFont.setSelectedItem("Courier New");
        jComboBoxFontSize.setSelectedItem("12");
        jTextFieldMaxRecentFiles.setText("10");
        defaultOptions();
    }//GEN-LAST:event_jButton23ActionPerformed

    private void jButtonCommentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCommentActionPerformed
        Color temp = JColorChooser.showDialog(this, "Color Dialog", colorComment);
        if (temp != null) {
            colorComment= temp;
        }
        updateColor();
        updateColorEditor();
    }//GEN-LAST:event_jButtonCommentActionPerformed

    private void jButtonNormalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNormalActionPerformed
        Color temp = JColorChooser.showDialog(this, "Color Dialog", colorNormal );
        if (temp != null) {
            colorNormal = temp;
        }
        updateColor();
        updateColorEditor();
    }//GEN-LAST:event_jButtonNormalActionPerformed

    private void jButtonVerbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonVerbActionPerformed
        Color temp = JColorChooser.showDialog(this, "Color Dialog", colorVerb  );
        if (temp != null) {
            colorVerb  = temp;
        }
        updateColor();
        updateColorEditor();
    }//GEN-LAST:event_jButtonVerbActionPerformed

    private void jButtonRoutineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRoutineActionPerformed
        Color temp = JColorChooser.showDialog(this, "Color Dialog", colorRoutine   );
        if (temp != null) {
            colorRoutine   = temp;
        }
        updateColor();
        updateColorEditor();
    }//GEN-LAST:event_jButtonRoutineActionPerformed

    private void jButtonPropertyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPropertyActionPerformed
        Color temp = JColorChooser.showDialog(this, "Color Dialog", colorProperty    );
        if (temp != null) {
            colorProperty    = temp;
        }
        updateColor();
        updateColorEditor();
    }//GEN-LAST:event_jButtonPropertyActionPerformed

    private void jButtonAttributeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAttributeActionPerformed
        Color temp = JColorChooser.showDialog(this, "Color Dialog", colorAttribute    );
        if (temp != null) {
            colorAttribute    = temp;
        }
        updateColor();
        updateColorEditor();
    }//GEN-LAST:event_jButtonAttributeActionPerformed

    private void jButtonKeywordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonKeywordActionPerformed
        Color temp = JColorChooser.showDialog(this, "Color Dialog", colorKeyword     );
        if (temp != null) {
            colorKeyword = temp;
        }
        updateColor();
        updateColorEditor();
    }//GEN-LAST:event_jButtonKeywordActionPerformed

    private void jButtonSwitchManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSwitchManagerActionPerformed
        jDialogSwitches.setSize(screensize.width-200, screensize.height-140);
        jDialogSwitches.setLocationRelativeTo(this);
        jDialogSwitches.setVisible(true);
    }//GEN-LAST:event_jButtonSwitchManagerActionPerformed

    private void jMenuItemPopupOpenSelectedFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupOpenSelectedFilesActionPerformed
        Object[] oggetti = jListProject.getSelectedValues();
        if (oggetti.length == 0) return;
        for (int c=0; c < oggetti.length ;c++){
            if (null != oggetti[c]){
                openFile( ((FileProject)oggetti[c]).path);
            }
        }
    }//GEN-LAST:event_jMenuItemPopupOpenSelectedFilesActionPerformed

    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
        // Saving selectoed switches in the switches.ini file
        StringBuffer make = getSwitchesForSaving();

        // Override the old file
        File file = new File(workingDir+"config"+Constants.SEP+java.util.ResourceBundle.getBundle("JIF").getString("CONFIG_SWITCHES_INI"));
        PrintStream ps;
        try{
            FileOutputStream fos = new FileOutputStream(file);
            ps = new PrintStream( fos );
            ps.println (make.toString());
            ps.close();
            JOptionPane.showMessageDialog(jDialogConfigFiles,java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE1"), java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE2") , JOptionPane.INFORMATION_MESSAGE);
        } catch(IOException e ){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
    }//GEN-LAST:event_jButton15ActionPerformed

    private void AboutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AboutButtonActionPerformed
        jDialogAbout.pack();
        jDialogAbout.setLocationRelativeTo(this);
        jDialogAbout.setVisible(true);
    }//GEN-LAST:event_AboutButtonActionPerformed

    private void jListProjectMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListProjectMouseEntered
        // Just One click: shows the tooltip
        FileProject fp = (FileProject)jListProject.getSelectedValue();
        if (null!=fp){
            jListProject.setToolTipText(fp.path);
        }
        else {
            jListProject.setToolTipText(null);
        }
    }//GEN-LAST:event_jListProjectMouseEntered

    private void jButton22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton22ActionPerformed
        JFileChooser chooser = new JFileChooser(workingDir);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.CANCEL_OPTION) {
            return;
        }
        else jTextFieldPathBrowser.setText(chooser.getSelectedFile().getAbsolutePath());
    }//GEN-LAST:event_jButton22ActionPerformed

    private void jButtonInterpreterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInterpreterActionPerformed
        runInterpreter();
    }//GEN-LAST:event_jButtonInterpreterActionPerformed

    private void jCheckBoxMappingHFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMappingHFileActionPerformed
        if (jCheckBoxMappingHFile.isSelected()){
            jCheckBoxMapping.setSelected(true);
            jCheckBoxMappingLive.setSelected(false);
        }
    }//GEN-LAST:event_jCheckBoxMappingHFileActionPerformed

    private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton21ActionPerformed
        jDialogInfo.setVisible(false);
    }//GEN-LAST:event_jButton21ActionPerformed

    private void jButtonInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInfoActionPerformed
        if (getCurrentFilename() == null) {
            return;
        }
        setInfoAT();
        jDialogInfo.setTitle(getCurrentFilename());
        jDialogInfo.setSize(400,400);
        jDialogInfo.setLocationRelativeTo(this);
        jDialogInfo.setVisible(true);
    }//GEN-LAST:event_jButtonInfoActionPerformed

    private void jMenuItemPopupCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupCloseActionPerformed
        closeFile();
        refreshTree();
        System.gc();
    }//GEN-LAST:event_jMenuItemPopupCloseActionPerformed

    private void jTabbedPane1ComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jTabbedPane1ComponentShown
        refreshTree();
    }//GEN-LAST:event_jTabbedPane1ComponentShown

    private void jTabbedPane1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTabbedPane1MouseClicked
        refreshTree();
    }//GEN-LAST:event_jTabbedPane1MouseClicked

    private void jCheckBoxMappingLiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMappingLiveActionPerformed
        if (jCheckBoxMappingLive.isSelected()){
            jCheckBoxMapping.setSelected(false);
            jCheckBoxMappingHFile.setSelected(false);
        }
    }//GEN-LAST:event_jCheckBoxMappingLiveActionPerformed

    private void jMenuItemRemoveFromProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRemoveFromProjectActionPerformed
        removeFileFromProject();
    }//GEN-LAST:event_jMenuItemRemoveFromProjectActionPerformed

    private void jMenuItemPopupSaveProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupSaveProjectActionPerformed
        saveProject();
    }//GEN-LAST:event_jMenuItemPopupSaveProjectActionPerformed

    private void jMenuItemPopupCloseProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupCloseProjectActionPerformed
        closeProject();
    }//GEN-LAST:event_jMenuItemPopupCloseProjectActionPerformed

    private void jMenuItemPopupOpenProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupOpenProjectActionPerformed
        openProject();
    }//GEN-LAST:event_jMenuItemPopupOpenProjectActionPerformed

    private void jMenuItemPopupNewProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupNewProjectActionPerformed
        newProject();
    }//GEN-LAST:event_jMenuItemPopupNewProjectActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        jDialogOption.setVisible(false);
        maxRecentFiles = Integer.parseInt(jTextFieldMaxRecentFiles.getText());
        unquote();
        saveJifConfiguration();
        savePath();        
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jCheckBoxMappingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMappingActionPerformed
        if (jCheckBoxMapping.isSelected()){
            jCheckBoxMappingLive.setSelected(false);
        }
        else {
            jCheckBoxMappingHFile.setSelected(false);
        }
    }//GEN-LAST:event_jCheckBoxMappingActionPerformed

    private void jMenuItemSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSettingsActionPerformed
        jTextFieldPathLib.setText(libPath);
        jTextFieldPathLibSecondary1.setText(libPathSecondary1);
        jTextFieldPathLibSecondary2.setText(libPathSecondary2);
        jTextFieldPathLibSecondary3.setText(libPathSecondary3);
        jTextFieldPathGames.setText(gamesDir);
        jTextFieldPathCompiler.setText(compiler);
        jTextFieldPathInterpreter.setText(interpreter);
        jTextFieldPathGlulx.setText(glulx);
        jTextFieldPathBrowser.setText(defaultBrowser);

        updateColor();
        updateColorEditor();
        Font tmpFont = defaultFont;

        jComboBoxFont.setSelectedItem(tmpFont.getName());
        jComboBoxFontSize.setSelectedItem(String.valueOf(tmpFont.getSize()));

        jTextFieldFont.setFont(new Font((String)jComboBoxFont.getSelectedItem(),Font.PLAIN,Integer.parseInt((String)jComboBoxFontSize.getSelectedItem())));

        //jDialogOption.pack();
        jDialogOption.setSize(580,550);           
        jDialogOption.setLocationRelativeTo(this);
        jDialogOption.setVisible(true);
    }//GEN-LAST:event_jMenuItemSettingsActionPerformed

    private void jButtonCloseAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCloseAllActionPerformed
        closeAllFiles();
    }//GEN-LAST:event_jButtonCloseAllActionPerformed

    private void jButtonCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCloseActionPerformed
        closeFile();
        refreshTree();
        System.gc();
    }//GEN-LAST:event_jButtonCloseActionPerformed

    private void jListProjectMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListProjectMouseClicked
        // Double clicking JIF opens the selected file
        if (evt.getClickCount()==2){
            // opens the file
            FileProject fp = (FileProject)jListProject.getSelectedValue();
            if (null!=fp){
                openFile(fp.path);
            }
        }
        else{
            // Sets the tooltip in case of One click
            FileProject fp = (FileProject)jListProject.getSelectedValue();
            if (null!=fp){
                jListProject.setToolTipText(fp.path);
            }
        }
    }//GEN-LAST:event_jListProjectMouseClicked

    private void jMenuItemCloseProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCloseProjectActionPerformed
       closeProject();
    }//GEN-LAST:event_jMenuItemCloseProjectActionPerformed

    private void jMenuItemPrint1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPrint1ActionPerformed
        new Utils().printInform(this,"Jif print - "+getCurrentFilename(), getCurrentJIFTextPane());
    }//GEN-LAST:event_jMenuItemPrint1ActionPerformed

    private void SaveAsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveAsButtonActionPerformed
        saveAs();
    }//GEN-LAST:event_SaveAsButtonActionPerformed

    private void jMenuItemNewProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNewProjectActionPerformed
       newProject();
    }//GEN-LAST:event_jMenuItemNewProjectActionPerformed

    private void jMenuItemSaveProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveProjectActionPerformed
       saveProject();
    }//GEN-LAST:event_jMenuItemSaveProjectActionPerformed

    private void jMenuItemPopupRemoveFromProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupRemoveFromProjectActionPerformed
        removeFileFromProject();
    }//GEN-LAST:event_jMenuItemPopupRemoveFromProjectActionPerformed

    private void jMenuItemPopupAddToProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupAddToProjectActionPerformed
        addFilesToProject();
    }//GEN-LAST:event_jMenuItemPopupAddToProjectActionPerformed

    private void jMenuItemAddFileToProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddFileToProjectActionPerformed
       addFilesToProject();
    }//GEN-LAST:event_jMenuItemAddFileToProjectActionPerformed

    private void jMenuItemOpenProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOpenProjectActionPerformed
       openProject();
    }//GEN-LAST:event_jMenuItemOpenProjectActionPerformed

    private void jMenuItemLinksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLinksActionPerformed
        try{
            loadConfigFiles(workingDir+"config"+Constants.SEP+java.util.ResourceBundle.getBundle("JIF").getString("CONFIG_LINKS"));
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
    }//GEN-LAST:event_jMenuItemLinksActionPerformed

    private void jMenuItemInsertSymbol1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemInsertSymbol1ActionPerformed
        showJWindowSymbol();
    }//GEN-LAST:event_jMenuItemInsertSymbol1ActionPerformed

    private void jButtonInsertSymbolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInsertSymbolActionPerformed
        showJWindowSymbol();
    }//GEN-LAST:event_jButtonInsertSymbolActionPerformed

    private void jMenuItemInsertSymbolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemInsertSymbolActionPerformed
        showJWindowSymbol();
    }//GEN-LAST:event_jMenuItemInsertSymbolActionPerformed

    private void jCheckBoxJToolBarInformActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxJToolBarInformActionPerformed
        if (!jCheckBoxJToolBarInform.getState()) jToolBarInform.setVisible(false);
        if (jCheckBoxJToolBarInform.getState()) jToolBarInform.setVisible(true);
    }//GEN-LAST:event_jCheckBoxJToolBarInformActionPerformed

    private void jButtonNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNewActionPerformed
        newAdventure();
    }//GEN-LAST:event_jButtonNewActionPerformed

    private void jButtonPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPrintActionPerformed
        new Utils().printInform(this,"Jif print - "+getCurrentFilename(), getCurrentJIFTextPane());
    }//GEN-LAST:event_jButtonPrintActionPerformed

    private void jMenuItemUncommentSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemUncommentSelectionActionPerformed
        getCurrentJIFTextPane().unCommentSelection();
    }//GEN-LAST:event_jMenuItemUncommentSelectionActionPerformed

    private void jButtonUncommentSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUncommentSelectionActionPerformed
        getCurrentJIFTextPane().unCommentSelection();
    }//GEN-LAST:event_jButtonUncommentSelectionActionPerformed

    private void jButtonCommentSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCommentSelectionActionPerformed
        getCurrentJIFTextPane().commentSelection();
    }//GEN-LAST:event_jButtonCommentSelectionActionPerformed

    private void jButtonCutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCutActionPerformed
        getCurrentJIFTextPane().cut();
    }//GEN-LAST:event_jButtonCutActionPerformed

    private void jButtonPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPasteActionPerformed
        getCurrentJIFTextPane().paste();
    }//GEN-LAST:event_jButtonPasteActionPerformed

    private void jButtonCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCopyActionPerformed
        copyToClipBoard();
    }//GEN-LAST:event_jButtonCopyActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        // Replacing....only if there is a selected TEXT
        if (getCurrentJIFTextPane().getSelectedText()!= null){
            getCurrentJIFTextPane().replaceSelection(jTextFieldReplace.getText());
        }
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        jDialogReplace.setVisible(false);
    }//GEN-LAST:event_jButton14ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        if (jTextFieldReplaceFind.getText().equals("")){
                JOptionPane.showMessageDialog(this,java.util.ResourceBundle.getBundle("JIF").getString("ERR_EMPTY_STRING"));
        }
        findString(jTextFieldReplaceFind.getText());
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        replaceAll();
        refreshTree();
    }//GEN-LAST:event_jButton13ActionPerformed

    private void jButtonReplaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonReplaceActionPerformed
        if (getCurrentJIFTextPane().getSelectedText()!= null){
            jTextFieldReplaceFind.setText(getCurrentJIFTextPane().getSelectedText());
        }
        getCurrentJIFTextPane().requestFocus();
        getCurrentJIFTextPane().setCaretPosition(0);
        jDialogReplace.pack();
        jDialogReplace.setLocationRelativeTo(this);
        jDialogReplace.setVisible(true);
    }//GEN-LAST:event_jButtonReplaceActionPerformed

    private void jMenuItemCommentSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCommentSelectionActionPerformed
        getCurrentJIFTextPane().commentSelection();
    }//GEN-LAST:event_jMenuItemCommentSelectionActionPerformed

    private void jMenuItemInsertFromFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemInsertFromFileActionPerformed
        insertFromFile();
    }//GEN-LAST:event_jMenuItemInsertFromFileActionPerformed

    private void jMenuItemInsertFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemInsertFileActionPerformed
        insertFromFile();
    }//GEN-LAST:event_jMenuItemInsertFileActionPerformed

    private void jMenuItemSyntaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSyntaxActionPerformed
        try{
            loadConfigFiles(workingDir+"config"+Constants.SEP+java.util.ResourceBundle.getBundle("JIF").getString("SYNTAX_FILE"));
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
    }//GEN-LAST:event_jMenuItemSyntaxActionPerformed

    private void jMenuItemPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPrintActionPerformed
        new Utils().printInform(this,"Jif print - "+getCurrentFilename(), getCurrentJIFTextPane());
    }//GEN-LAST:event_jMenuItemPrintActionPerformed

    private void jTree1ValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTree1ValueChanged
        // Object Tree management
        // Removing all the selected text
        getCurrentJIFTextPane().removeHighlighter();

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)jTree1.getLastSelectedPathComponent();
        if (node == null){
            // Avoids errors
            return;
        }

        Object nodo = node.getUserObject();
        try{
            Inspect insp = (Inspect)nodo;
            if (insp != null){
                JIFTextPane jif = getCurrentJIFTextPane();
                jif.getHlighter().highlightFromTo(jif, insp.Iposition , insp.IpositionEnd);
                el = jif.getDocument().getDefaultRootElement();
                //jif.scrollRectToVisible(this.getRectForLine(el.getElementIndex(insp.Iposition)));
                jif.scrollRectToVisible(jif.modelToView(jif.getDocument().getLength()));
                jif.scrollRectToVisible(jif.modelToView(insp.Iposition));
                jif.requestFocus();
                jif.setCaretPosition(insp.IpositionEnd);
            }
        }catch(Exception e){
            e.printStackTrace();            
        }
    }//GEN-LAST:event_jTree1ValueChanged

    private void jTextAreaOutputMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextAreaOutputMouseClicked
        // When user clicks on errors/warnings, JIF jumps to the correct line
        // in the source
        try{
            // 1) find where user has clicked on
            // 2) find the correct line
            // 3) get the file name
            // 4) (if closed) opens the correct file and jump on the correct line
            int posizione_click = jTextAreaOutput.viewToModel(evt.getPoint());
            String nome="";
            int riga = 0;
            boolean found=false;

            // Rescues the correct line
            el = jTextAreaOutput.getDocument().getDefaultRootElement();
            int ind = el.getElementIndex(posizione_click);
            el = jTextAreaOutput.getDocument().getDefaultRootElement().getElement(ind);
            ultima = jTextAreaOutput.getText(el.getStartOffset(), el.getEndOffset()-el.getStartOffset());


            // Only if the line starts with the "#" char
            if (ultima.indexOf("#")!=-1 && ((ultima.indexOf(".inf")!=-1) || (ultima.indexOf(".h")!=-1))){
                // Removing all the selected text in the output window
                hlighterOutput.removeHighlights(jTextAreaOutput);

                // Highlight the correct line
                hlighterOutput.highlightFromTo(jTextAreaOutput,el.getStartOffset(),el.getEndOffset() );

                StringTokenizer stok = new StringTokenizer(ultima,"#()");
                nome=stok.nextToken();
                riga=Integer.parseInt(stok.nextToken());

                // checks if the file exists
                int selected = jTabbedPane1.getTabCount();
                for (int count=0; count < selected; count++){
                    if (nome.equals(getFilenameAt(count))){
                        found = true;
                        jTabbedPane1.setSelectedIndex(count);
                    }
                }

                if (!found) {
                    // The file (with the error) is closed and JIF has to open is
                    openFile(nome);
                }

                // Find the line with the error
                el = getCurrentJIFTextPane().getDocument().getDefaultRootElement();
                el = el.getElement(riga-1);
                getCurrentJIFTextPane().setCaretPosition(el.getStartOffset());

                // Removing all the selected text
                getCurrentJIFTextPane().removeHighlighter();

                // Highlight the line which has product the error during compiling
                getCurrentJIFTextPane().getHlighter().highlightFromTo(getCurrentJIFTextPane(),el.getStartOffset(),el.getEndOffset() );
                getCurrentJIFTextPane().scrollRectToVisible(getCurrentJIFTextPane().modelToView(getCurrentJIFTextPane().getDocument().getLength()));                
                getCurrentJIFTextPane().scrollRectToVisible(getCurrentJIFTextPane().modelToView(el.getStartOffset()));
            }
            // find string in all files function
            else if (ultima.startsWith(Constants.TOKENSEARCH)){
                // Removing all the selected text in the output window
                hlighterOutput.removeHighlights(jTextAreaOutput);

                // Highlight the correct line
                hlighterOutput.highlightFromTo(jTextAreaOutput,el.getStartOffset(),el.getEndOffset() );

                StringTokenizer stok = new StringTokenizer(ultima,Constants.TOKENSEARCH);
                nome=stok.nextToken();
                riga=Integer.parseInt(stok.nextToken());

                // checks if the file exists
                int selected = jTabbedPane1.getTabCount();
                for (int count=0; count < selected; count++){
                    if (nome.equals(getFilenameAt(count))){
                        found = true;
                        jTabbedPane1.setSelectedIndex(count);
                    }
                }

                if (!found) {
                    // The file (with the error) is closed and JIF has to open is
                    openFile(nome);
                }

                // Find the line with the error
                el = getCurrentJIFTextPane().getDocument().getDefaultRootElement();
                el = el.getElement(riga-1);
                getCurrentJIFTextPane().setCaretPosition(el.getStartOffset());

                // Removing all the selected text
                getCurrentJIFTextPane().removeHighlighter();

                // Highlight the line which has product the error during compiling
                getCurrentJIFTextPane().getHlighter().highlightFromTo(getCurrentJIFTextPane(),el.getStartOffset(),el.getEndOffset() );
                getCurrentJIFTextPane().scrollRectToVisible(getCurrentJIFTextPane().modelToView(getCurrentJIFTextPane().getDocument().getLength()));                
                getCurrentJIFTextPane().scrollRectToVisible(getCurrentJIFTextPane().modelToView(el.getStartOffset()));
            }
            else return;
        } catch (BadLocationException e){
            e.printStackTrace();
            }
    }//GEN-LAST:event_jTextAreaOutputMouseClicked

    private void jMenuItemClearAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemClearAllActionPerformed
        if (JOptionPane.showConfirmDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("MSG_DELETE1") , java.util.ResourceBundle.getBundle("JIF").getString("MSG_DELETE2") , JOptionPane.OK_CANCEL_OPTION) ==0 ){
            getCurrentJIFTextPane().setText("");
        }
    }//GEN-LAST:event_jMenuItemClearAllActionPerformed

    private void jMenuItemSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSelectAllActionPerformed
        getCurrentJIFTextPane().selectAll();
    }//GEN-LAST:event_jMenuItemSelectAllActionPerformed

    private void jMenuItemClearRecentFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemClearRecentFilesActionPerformed
        clearLastFilesList();
    }//GEN-LAST:event_jMenuItemClearRecentFilesActionPerformed

    private void jMenuItemReplaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemReplaceActionPerformed
        getCurrentJIFTextPane().requestFocus();
        getCurrentJIFTextPane().setCaretPosition(0);
        jDialogReplace.pack();
        jDialogReplace.setLocationRelativeTo(this);
        jDialogReplace.setVisible(true);
    }//GEN-LAST:event_jMenuItemReplaceActionPerformed

    private void jMenuItemSaveAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveAllActionPerformed
        saveAll();
    }//GEN-LAST:event_jMenuItemSaveAllActionPerformed

    private void jMenuItemCloseAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCloseAllActionPerformed
        closeAllFiles();
    }//GEN-LAST:event_jMenuItemCloseAllActionPerformed

    private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed
        JFileChooser chooser = new JFileChooser(workingDir);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.CANCEL_OPTION) {
            return;
        }
        else jTextFieldPathInterpreter.setText(chooser.getSelectedFile().getAbsolutePath());
    }//GEN-LAST:event_jButton18ActionPerformed

    private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton17ActionPerformed
        JFileChooser chooser = new JFileChooser(workingDir);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.CANCEL_OPTION) {
            return;
        }
        else {
            jTextFieldPathCompiler.setText(chooser.getSelectedFile().getAbsolutePath());
            compiler = chooser.getSelectedFile().getAbsolutePath();
        }
    }//GEN-LAST:event_jButton17ActionPerformed

    private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton16ActionPerformed
        JFileChooser chooser = new JFileChooser(workingDir);
        chooser.setFileSelectionMode(chooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.CANCEL_OPTION) {
            return;
        }
        else {
            jTextFieldPathGames.setText(chooser.getSelectedFile().getAbsolutePath());
            interpreter = chooser.getSelectedFile().getAbsolutePath();
        }
    }//GEN-LAST:event_jButton16ActionPerformed

    private void jButton19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton19ActionPerformed
        JFileChooser chooser = new JFileChooser(workingDir);
        chooser.setFileSelectionMode(chooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.CANCEL_OPTION) {
            return;
        }
        else jTextFieldPathLib.setText(chooser.getSelectedFile().getAbsolutePath());
    }//GEN-LAST:event_jButton19ActionPerformed

    private void jTextFieldFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFindActionPerformed
//      getCurrentJIFTextPane().findString();
    }//GEN-LAST:event_jTextFieldFindActionPerformed

    private void jMenuItemCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCloseActionPerformed
        closeFile();
        refreshTree();
        System.gc();
    }//GEN-LAST:event_jMenuItemCloseActionPerformed

    private void jMenuItemConfigurazioneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemConfigurazioneActionPerformed
        String filename = workingDir+Constants.SEP+"doc"+Constants.SEP+"ENG_config.txt";
        File file = new File(filename);
        if (!(file.exists())){
            System.out.println(java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE1") + filename);
            return;
        }

        jDialogText.setTitle(java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF1"));
        jDialogText.setSize(600,450);
        jDialogText.setLocationRelativeTo(this);
        jDialogText.setVisible(true);

        try{
                BufferedReader br = new BufferedReader(new FileReader(file));
                sb.setLength(0);

                while ((riga = br.readLine())!=null){
                    sb.append(riga).append("\n");
                }
                jTextArea4.setText(sb.toString());
                jLabel5.setText(java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF2"));
                jTextArea4.setCaretPosition(0);
                br.close();
        } catch(IOException e){
            System.err.println(e.getMessage());
        }
    }//GEN-LAST:event_jMenuItemConfigurazioneActionPerformed

    private void jButtonFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFindActionPerformed
      getCurrentJIFTextPane().findString();
    }//GEN-LAST:event_jButtonFindActionPerformed

    private void jMenuItemAltKeysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAltKeysActionPerformed
    try{
           loadConfigFiles(workingDir+"config"+Constants.SEP+"altkeys.ini");
        }
    catch (Exception e){
        System.out.println(e.getMessage());
        System.err.println(e.getMessage());
    }
    }//GEN-LAST:event_jMenuItemAltKeysActionPerformed

    private void jMenuItemCopy1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCopy1ActionPerformed
        copyToClipBoard();
    }//GEN-LAST:event_jMenuItemCopy1ActionPerformed

    private void jMenuItemHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemHelpActionPerformed

        String filename = workingDir+Constants.SEP+"doc"+Constants.SEP+"ENG_info.txt";
        File file = new File(filename);
        if (!(file.exists())){
            System.out.println(java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE1") + filename);
            return;
        }

        jDialogText.setTitle(java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF3"));
        jDialogText.setSize(555,450);
        jDialogText.setLocationRelativeTo(this);
        jDialogText.setVisible(true);

        try{
                BufferedReader br = new BufferedReader(new FileReader(file));
                sb.setLength(0);

                while ((riga = br.readLine())!=null){
                    sb.append(riga).append("\n");
                }
                jTextArea4.setText(sb.toString());
                jLabel5.setText(java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF4"));
                jTextArea4.setCaretPosition(0);
                br.close();
        } catch(IOException e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
    }//GEN-LAST:event_jMenuItemHelpActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        jDialogText.setVisible(false);
        jTextArea4.setText("");
        jLabel5.setText("");
    }//GEN-LAST:event_jButton6ActionPerformed

    private void SaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveAsActionPerformed
        saveAs();
    }//GEN-LAST:event_SaveAsActionPerformed

    private void jMenuItemCopyrightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCopyrightActionPerformed
        String filename = workingDir+Constants.SEP+"doc"+Constants.SEP+"ENG_copyright.txt";
        File file = new File(filename);
        if (!(file.exists())){
            System.out.println(java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE1") + filename);
            return;
        }
        jDialogText.setTitle(java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF5"));
        jDialogText.setSize(550,470);
        jDialogText.setLocationRelativeTo(this);
        jDialogText.setVisible(true);

        try{
                BufferedReader br = new BufferedReader(new FileReader(file));
                sb.setLength(0);

                while ((riga = br.readLine())!=null){
                    sb.append(riga).append("\n");
                }
                jTextArea4.setText(sb.toString());
                jLabel5.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_COPYRIGHTS"));
                jTextArea4.setCaretPosition(0);
                br.close();
        } catch(IOException e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
    }//GEN-LAST:event_jMenuItemCopyrightActionPerformed

    private void jMenuItemSwitchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSwitchActionPerformed
        try{
            loadConfigFiles(workingDir+"config"+Constants.SEP+java.util.ResourceBundle.getBundle("JIF").getString("CONFIG_SWITCHES_INI"));
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
    }//GEN-LAST:event_jMenuItemSwitchActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        jDialogSwitches.setVisible(false);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jMenuItemSwitchesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSwitchesActionPerformed
        jDialogSwitches.setSize(screensize.width-200, screensize.height-140);
        jDialogSwitches.setLocationRelativeTo(this);
        jDialogSwitches.setVisible(true);
    }//GEN-LAST:event_jMenuItemSwitchesActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // Saving file
        File file = new File(jLabel2.getText());
        PrintStream ps;
        try{
            FileOutputStream fos = new FileOutputStream(file);
            ps = new PrintStream( fos );
            ps.println (jTextAreaConfig.getText());
            ps.close();
            loadConfig();
            JOptionPane.showMessageDialog(jDialogConfigFiles,java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE1"), java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE2") , JOptionPane.INFORMATION_MESSAGE);
        } catch(IOException e ){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
          jDialogConfigFiles.setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void NewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NewActionPerformed
        newAdventure();
    }//GEN-LAST:event_NewActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        jDialogAbout.pack();
        jDialogAbout.setLocationRelativeTo(this);
        jDialogAbout.setVisible(false);
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jMenuItemClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemClearActionPerformed
        jMenuPaste.removeAll();
    }//GEN-LAST:event_jMenuItemClearActionPerformed

    private void jMenuItemCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCopyActionPerformed
        copyToClipBoard();
    }//GEN-LAST:event_jMenuItemCopyActionPerformed

    private void jCheckBoxJToolBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxJToolBarActionPerformed
        if (!jCheckBoxJToolBar.getState()) jToolBarCommon.setVisible(false);
        if (jCheckBoxJToolBar.getState()) jToolBarCommon.setVisible(true);
    }//GEN-LAST:event_jCheckBoxJToolBarActionPerformed

    private void jCheckBoxOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxOutputActionPerformed
        if (!jCheckBoxOutput.getState()) {
            jTabbedPane2.setVisible(false);
        }
        if (jCheckBoxOutput.getState()) {
            jSplitPane3.setBottomComponent(jTabbedPane2);
            jTabbedPane2.setVisible(true);
        }
    }//GEN-LAST:event_jCheckBoxOutputActionPerformed

    private void AboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AboutActionPerformed
        jDialogAbout.pack();
        jDialogAbout.setLocationRelativeTo(this);
        jDialogAbout.setVisible(true);
    }//GEN-LAST:event_AboutActionPerformed

    private void ExitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExitButtonActionPerformed
        exitJif();
    }//GEN-LAST:event_ExitButtonActionPerformed

    private void RunButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RunButtonActionPerformed
        //saveFile();
    	saveAll();
        rebuildAll();
        runAdventure();
    }//GEN-LAST:event_RunButtonActionPerformed

    private void OpenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpenButtonActionPerformed
        openFile();
    }//GEN-LAST:event_OpenButtonActionPerformed

    private void SaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveButtonActionPerformed
        saveFile();
        // Saving a backup if the jCheckBoxBackup is Selected
        if(jCheckBoxBackup.isSelected()){
            try{
                // Creates the Backup directory if this doesn't exist
                File file_dirBackup = new File(workingDir+Constants.SEP+"backup"+Constants.SEP);
                if (!file_dirBackup.exists()) file_dirBackup.mkdir();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mmss", java.util.Locale.getDefault());
                Date today = new Date();

                // Saving the file with this name
                // NAMEFILE_YEAR MONTH DAY HOURS MINUTES SECONDS.inf
                String nomefile = workingDir+Constants.SEP+"backup"+Constants.SEP+formatter.format(today) +"_"+ fileInf.substring(fileInf.lastIndexOf(Constants.SEP)+1) + ".zip";
                Utils.zippa(fileInf , nomefile);
            } catch(Exception e){
                System.out.println(e.getMessage());
                System.err.println(e.getMessage());
            }
        }
    }//GEN-LAST:event_SaveButtonActionPerformed

    private void RebuildButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RebuildButtonActionPerformed
        //saveFile();
    	saveAll();//FIXED BUG save all files in project
        rebuildAll();
    }//GEN-LAST:event_RebuildButtonActionPerformed

    private void RunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RunActionPerformed
        clearOutput();
        //saveFile();
        saveAll();//FIXED BUG save all files in project
        rebuildAll();
        runAdventure();
    }//GEN-LAST:event_RunActionPerformed

    private void ExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExitActionPerformed
        exitJif();
    }//GEN-LAST:event_ExitActionPerformed

    private void SaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveActionPerformed
        clearOutput();
        saveFile();
    }//GEN-LAST:event_SaveActionPerformed

    private void BuildAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BuildAllActionPerformed
        //saveFile();
    	saveAll();//FIXED BUG save all files in project
        rebuildAll();
    }//GEN-LAST:event_BuildAllActionPerformed

    private void OpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpenActionPerformed
        openFile();
    }//GEN-LAST:event_OpenActionPerformed

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        exitJif();
    }//GEN-LAST:event_exitForm

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
    	try{
            //  * com.jgoodies.plaf.windows.ExtWindowsLookAndFeel Parecido a win98
            //	* com.jgoodies.plaf.plastic.PlasticLookAndFeel win98 mejorado
            //	* com.jgoodies.plaf.plastic.Plastic3DLookAndFeel xp pero cuadradito
            //	* com.jgoodies.plaf.plastic.PlasticXPLookAndFeel
            System.out.println(System.getProperty("os.name"));
            System.out.println(UIManager.getSystemLookAndFeelClassName());
                        
            /*
            if(System.getProperty("os.name").indexOf("Windows")!=-1){
                //PlasticXPLookAndFeel.setMyCurrentTheme(new SkyBlue());
                //UIManager.setLookAndFeel("com.jgoodies.plaf.plastic.PlasticXPLookAndFeel");

            }
            else{
                if(System.getProperty("os.name").indexOf("Linux")!=-1){
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
                }
                else{
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
            }
            */
            try {
                // test if looks.jar library is present
                Class.forName("com.jgoodies.looks.LookUtils");
                String lafName =
                LookUtils.IS_OS_WINDOWS_XP
                ? Options.getCrossPlatformLookAndFeelClassName()
                : Options.getSystemLookAndFeelClassName();
                UIManager.setLookAndFeel(lafName);
            } catch (Exception e) {
                System.err.println("Can't set look & feel:" + e);
            }
            System.out.println("Setting Look and Feel: "+UIManager.getLookAndFeel().getName());
        }
        catch(Exception e){
          System.out.println("ERRORE: "+e.getMessage());
          System.err.println(e.getMessage());

        }
        if (args.length > 0)  {
            new jFrame(args[0]).show();
        }
        else{
            new jFrame(null).show();
        }
    }

    /**
     * Search for a string in all files of project
     */
    public void searchAllFiles(String target){
        // if project is Not null
        if (null != currentProject && !currentProject.equals("default"))
        {
            StringBuffer output = new StringBuffer();
            // Load the current project file
            File file = new File(currentProject);
            try{
                String result;
                BufferedReader br = new BufferedReader(new FileReader(file));
                while ((riga = br.readLine())!=null){
                    if (!(riga.startsWith(Constants.TOKENCOMMENT))&&!(riga.equals(""))){
                        if (riga.indexOf("[FILE]=")!=-1){
                            // qui lancio un metodo che mi apre il file e mi cerca la stringa
                            result = Utils.searchString(target, new File(riga.substring(riga.indexOf("[FILE]=")+7)));
                            if (null!=result){
                                output.append(result+"\n");    
                            }                            
                        }
                    }
                }
                br.close();
            } catch(IOException e){
                System.out.println(e.getMessage());
                System.err.println(e.getMessage());
            }
            
            this.jTextAreaOutput.setText(output.toString());
            this.jTextAreaOutput.setCaretPosition(0);
        }        
    }
    
    public void saveFile() {
        if (jTabbedPane1.getTitleAt(jTabbedPane1.getSelectedIndex()).endsWith("*")){
                //getCurrentFilename().indexOf("*")!=-1){
            jTabbedPane1.setTitleAt(jTabbedPane1.getSelectedIndex(),
            jTabbedPane1.getTitleAt(jTabbedPane1.getSelectedIndex()).substring(0,jTabbedPane1.getTitleAt(jTabbedPane1.getSelectedIndex()).length()-1)
            );
        }
        clearOutput();

        File file = new File(getCurrentFilename());
        PrintStream ps;
        try{
            FileOutputStream fos = new FileOutputStream(file);

            // Replaces the "\n" chars with System.getProperty("line.separator")
            String tmp = Utils.replace(getCurrentJIFTextPane().getText(),"\n",System.getProperty("line.separator"));
            ps = new PrintStream( fos );
            ps.print(tmp);
            ps.close();
            StringBuffer strb=new StringBuffer(java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE3"));
            strb.append(getCurrentFilename());
            strb.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE4"));
            jTextAreaOutput.append(strb.toString());
            //jTextAreaOutput.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE3")+ getCurrentFilename() +java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE4"));

            // salvo il file con estensione tmp con le modifiche apportate
            // se il chekboxmapping è true
            if (jCheckBoxMapping.isSelected()){

                String source = getCurrentJIFTextPane().getText();
                int pos=0, old=0;
                boolean entrato = false;
                sb.setLength(0);
                int size = mappa.size();
                for (int count=0; count < size; count++){

                    pos=0;
                    old=0;
                    entrato = false;
                    sb.setLength(0);
                    String riga = (String)mappa.get(count);
                    String index = riga.substring(0,riga.indexOf(','));
                    String value = riga.substring(riga.indexOf(',')+1);

                    while ((pos = source.indexOf(index, pos)) >= 0){
                        sb.append(source.substring(old,pos));
                        sb.append(value);
                        pos ++;
                        old = pos;
                        entrato = true;
                    }
                    if (entrato) {
                        sb.append(source.substring(old));
                        source = sb.toString();
                    }
                }

                // il nome del file viene trasformato:
                // da: prova.inf
                // a:  XXXprova.inf
                String tmp_dir  = getCurrentFilename().substring(0,getCurrentFilename().lastIndexOf(Constants.SEP)+1);
                String tmp_name = getCurrentFilename().substring(getCurrentFilename().lastIndexOf(Constants.SEP)+1);
//System.out.println("tmp dir="+tmp_dir);
//System.out.println("tmp name="+tmp_name);
                // se la directory mapping non esiste la creo
                File f = new File(tmp_dir+Constants.SEP+"mapping");
                if (!f.exists()){
                    f.mkdir();
                }

                // CREO IL FILE MAPPING
                fileInf_withmapping = tmp_dir+Constants.SEP+"mapping"+Constants.SEP+tmp_name ;
//System.out.println("fileInf_withmapping="+fileInf_withmapping);                
                fos = new FileOutputStream(new File(fileInf_withmapping));
                ps = new PrintStream( fos );                
                Vector vettore = getIncludedFiles(getCurrentJIFTextPane().getText());
                ps.println (source);
                ps.close();
                runMappingIncludedFiles(vettore);
           }

            // rendo visibile la finestra di output
            jTabbedPane2.setSelectedComponent(jScrollPane2);
            
            setTitle(getJifVersion() +" - " + getCurrentFilename());

        } catch(IOException e){
            System.out.println("ERRORE: "+e.getMessage());
            System.err.println(e.getMessage());
        }

    }


    public void saveAll(){
        // Remember the file selected
        Component comp = jTabbedPane1.getSelectedComponent();
        
        int componenti = jTabbedPane1.getTabCount();
        for (int count=0; count < componenti; count++){
            jTabbedPane1.setSelectedIndex(count);
            if (getCurrentTitle().indexOf("*")!=-1)
            	saveFile(); //Only save modified files
        }
        
        // reassign the selected component
        jTabbedPane1.setSelectedComponent(comp);
    }


    public void rebuildAll() {
    	String process_string[];
    	Vector auxV=new Vector(6);
    	String switchString[];
        // controllo che esista il compilatore con il path  inserito nella config.ini
        // se non esiste visualizzo un messaggio di warning
        File test = new File(compiler);
        if (!test.exists()){
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER1")+compiler+java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER2"),java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER3") , JOptionPane.ERROR_MESSAGE);
            return;
        }

        // se l'utente ha tolto la visto della TextAreaOutput, la rendo visible..
        // Ma verrà sempre nascosa ad ogni evento sul TextPane
        if (!jCheckBoxOutput.getState()){
            //jCheckBoxOutput.setState(true);
            jSplitPane3.setBottomComponent(jTabbedPane2);
            jTabbedPane2.setVisible(true);
        }
        //imposto il focus sulla tabbedWindow della compilazione
        jTabbedPane2.setSelectedComponent(jScrollPane2);

        //recupero l'attuale file name
        fileInf = getCurrentFilename(); //jTabbedPane1.getTitleAt( jTabbedPane1.getSelectedIndex());

        // imposto il file di uscita: es 3.1.z5
        makeSwitches();     // recupero il tipo di estensione
        String estensione="";

        if (tipoz.equals("-v3")) estensione=".z3";
        if (tipoz.equals("-v4")) estensione=".z4";
        if (tipoz.equals("-v5")) estensione=".z5";
        if (tipoz.equals("-v6")) estensione=".z6";
        if (tipoz.equals("-v8")) estensione=".z8";

        // Check Mode: If this is Glulx Mode... extension is ULX
        if (jCheckBoxGlulxMode.isSelected()){
            estensione=".ulx";
        }

        // se è impostato il file main lo uso
        if (mainFile != null && !mainFile.equals("")){
            jTextAreaOutput.append("Using main file "+mainFile+" to compiling...");
            fileInf = mainFile;
        }

        String fileOut = fileInf.substring(0,fileInf.lastIndexOf(".")) + estensione;
        //StringBuffer fileOut=new StringBuffer(fileInf.substring(0,fileInf.lastIndexOf(".")));
        //fileOut.append(estensione);
        jTextAreaOutput.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER1"));
//        String process_string;
        String lib;

        // recupero la directory corrente del file che sto compilando e la includo in fase di compilazione
        //Only if checked!!
        String dir ="";//new String("");
        if(jCheckBoxAdventInLib.isSelected())
        	dir=fileInf.substring(0,fileInf.lastIndexOf(Constants.SEP))+",";

        lib = dir+libPath;
        
        // controllo se c'è una path library secondaria...
        if (!libPathSecondary1.trim().equals("")){
            lib = lib+","+libPathSecondary1;
        }
        
        if (!libPathSecondary2.trim().equals("")){
            lib = lib+","+libPathSecondary2;
        }
        
        if (!libPathSecondary3.trim().equals("")){
            lib = lib+","+libPathSecondary3;
        }
        
        switchString=makeSwitches().split(" ");
        
        auxV.add(compiler);
        for(int i=1;i<switchString.length;i++) //i=1 to avoid the first " "
        	auxV.add(switchString[i]);
        //se è attivo checkboxmapping cambio il nome del file da usare come source
        if (jCheckBoxMapping.isSelected()){
            if (null == fileInf_withmapping || fileInf_withmapping.equals("")){
                saveFile();
            }
            fileOut = fileInf_withmapping.substring(0,fileInf_withmapping.lastIndexOf(".")) + estensione;
        }
        auxV.add("+include_path="+lib);
        auxV.add(fileInf);
        auxV.add(fileOut);
        
        process_string=new String[auxV.size()];
        for(int i=0;i<auxV.size();i++){
        	process_string[i]=new String((String)auxV.get(i));
        	jTextAreaOutput.append(process_string[i]+" ");	
        }
        jTextAreaOutput.append("\n");
 
        try{
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(process_string, null, new File(gamesDir));
        String line="";
        BufferedReader br= new BufferedReader( new InputStreamReader( proc.getInputStream()));

        while ( (line = br.readLine() )!=null ){
            // in caso di errore o warning metto il cancelletto #
            if ( (line.indexOf("Error")!=-1) || (line.indexOf("error")!=-1)) {
                jTextAreaOutput.append("#"+line+"\n");
            }
            else if ( (line.indexOf("Warning")!=-1) || (line.indexOf("warning")!=-1)) {
                jTextAreaOutput.append("#"+line+"\n");
            }
            else jTextAreaOutput.append(line+"\n");
        }

        
        

        //jTextAreaOutput.append(out+"\n");
        jTextAreaOutput.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER2"));
        jTextAreaOutput.append("\n");
        
        setTitle(getJifVersion() +" - " + getCurrentFilename());

        }
        catch(IOException e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
       /* catch(InterruptedException e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        } */

    }

    // Agggiunto il controllo sul MODE (Inform/Glulx)
    public void runAdventure() {

        String inter="";    // interpreter
        if (jCheckBoxInformMode.isSelected()){
            inter = interpreter;
        }
        else {
            inter = glulx;
        }

        // controllo che esista l'interprete con il path  inserito nella config.ini
        // se non esiste visualizzo un messaggio di warning
//        File test = new File(inter);
//        if (!test.exists()){
//            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER1")+inter+java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER2"), java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER3") , JOptionPane.ERROR_MESSAGE);
//            return;
//        }

        //recupero l'attuale file name
        fileInf = getCurrentFilename(); //jTabbedPane1.getTitleAt( jTabbedPane1.getSelectedIndex());

        // se è impostato il file main lo uso
        if (mainFile != null && !mainFile.equals("")){
            jTextAreaOutput.append("Using main file "+mainFile+" to running...");
            fileInf = mainFile;
        }

        clearOutput();
        jTextAreaOutput.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_RUN1"));

        try{
        Runtime rt = Runtime.getRuntime();
        String command[]=new String[2];

        // in base al tipo di file di uscita, scelgo l'estensione del file da passare all'interprete
        String estensione ="";
        if (tipoz.equals("-v3")) estensione=".z3";
        if (tipoz.equals("-v4")) estensione=".z4";
        if (tipoz.equals("-v5")) estensione=".z5";
        if (tipoz.equals("-v6")) estensione=".z6";
        if (tipoz.equals("-v8")) estensione=".z8";

        if (jCheckBoxGlulxMode.isSelected()){
            estensione = ".ulx";
        }

        // se il mapping è abilitato, devo recuperare il nome del file giusto
        if (jCheckBoxMapping.isSelected()){
        		command[0]= new String(inter);
        		command[1]= new String(fileInf_withmapping.substring(0,fileInf_withmapping.indexOf(".inf"))+estensione);
        }
        else {
    			command[0]= new String(inter);
    			command[1]= new String(fileInf.substring(0,fileInf.indexOf(".inf"))+estensione);

        }
        //XXX else command = interpreter+" \""+ fileInf.substring(0,fileInf.indexOf(".inf"))+estensione+"\"\n";
        //stampo nella JTextArea, il comando lanciato per eseguire l'interprete,
        // così da vedere chiaramente quale file è stato passato all'interprete

        jTextAreaOutput.append(command[0]+" "+command[1]+"\n");

        rt.exec(command); //Process proc =  unused
        //String line=""; unused
        //String out="";

//        BufferedReader br= new BufferedReader( new InputStreamReader( proc.getInputStream()));
//
//        while ( (line = br.readLine() )!=null ){
//            out += line;
//        }
//        jTextAreaOutput.append(out);
        jTextAreaOutput.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER2"));
        } catch(IOException e){
            System.err.println(e.getMessage());
        }
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
            jPopupMenuProject.show(e.getComponent(),
                       e.getX(), e.getY());
        }
    }
}

 class MenuListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            String id = ((javax.swing.JMenuItem)e.getSource()).getText();
            try{
                //se non trovo nessun carattere "§" non vado a capo
                if ( ((String)operations.get((String)id)).indexOf("§")==-1 ){
                    // inserisco la stringa senza andare a capo
                    getCurrentDoc().insertString(getCurrentJIFTextPane().getCaretPosition(), (String)operations.get((String)id) , attr);
                }
                else{
                    st = new StringTokenizer((String)operations.get((String)id),"§");
                    while (st.hasMoreTokens()){
                      getCurrentDoc().insertString(getCurrentJIFTextPane().getCaretPosition(), st.nextToken()+"\n" , attr);
                    }
                }
            }
            catch (Exception ex){
                System.out.println(ex.getMessage());
                System.err.println(ex.getMessage());
            }
    }
}


  // funzione per l'apertura di file
  private void openFile(){
        JFileChooser chooser;
        if (lastDir!=null && !lastDir.equals("")){
              chooser  = new JFileChooser(lastDir);
        }
        else {
             chooser = new JFileChooser(gamesDir);
        }

        JifFileFilter infFilter = new JifFileFilter("inf", java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF7"));
        infFilter.addExtension("h");
        infFilter.addExtension("res");
        infFilter.addExtension("txt");
        chooser.setFileFilter(infFilter);


        // Selezione Multipla
        chooser.setMultiSelectionEnabled(true);

        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.CANCEL_OPTION) {
            //imposto a null il nome del file e return
            fileInf = null;
            return;
        }

        File[] files = chooser.getSelectedFiles();

        // imposto ladtDir se != null
        String tmp = files[0].getAbsolutePath();
        lastDir = tmp.substring(0,tmp.lastIndexOf(Constants.SEP));

        File file;
        for (int i=0 ; i<files.length; i++){
            file = files[i];
            //Controllo che non sia stato già aperto un file
            if (checkOpenFile(file.getAbsoluteFile().toString())) return;
//            riga="";

            JIFTextPane jtp;
            if (jCheckBoxWrapLines.isSelected()){
                jtp = new JIFTextPane(this, file);
            }
            else{
                jtp = new JIFTextPane(this, file){
                    public boolean getScrollableTracksViewportWidth(){
                            if (getSize().width < getParent().getSize().width) return true;
                            return false;
                    }
                    public void setSize(Dimension d){
                        if (d.width < getParent().getSize().width) d.width = getParent().getSize().width;
                        super.setSize(d);
                    }
                };
            }

            //JScrollPane scroll= new JScrollPane(jtp);
            JIFScrollPane scroll= new JIFScrollPane(jtp,jtp.pathfile);
            scroll.setViewportView(jtp);
            
            //aggiungo la textarea per rowheader solo se il checkbox relativo è true
            if (jCheckBoxNumberLines.isSelected()){
                LineNumber lineNumber = new LineNumber( jtp );
                scroll.setRowHeaderView( lineNumber );
            }
            //jTabbedPane1.add(scroll, file.getAbsolutePath());
            jTabbedPane1.add(scroll, jtp.subPath);
            jTabbedPane1.setSelectedIndex(jTabbedPane1.getTabCount() - 1);

            //aggiungere al file config.ini il nome del file chiuso
            appendLastFile(file.getAbsolutePath());

            //fileInf caricato
            fileInf = chooser.getSelectedFile().getAbsolutePath();

            // cursore sulla prima riga
            jtp.setCaretPosition(0);
            lastFile = file.getAbsolutePath();

        } // end for

        enableComponents();
        // Dopo che ho aperto il file o i file
        refreshTree();
  }

  private void openFile(String nomefile){

        File file = new File(nomefile);

        //controllo che il file esista ancora.
        if (!file.exists()){
            //JOptionPane.showMessageDialog(this, "File inesistente");
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE2")+nomefile+java.util.ResourceBundle.getBundle("JIF").getString("ERR_NAMEFILE3") , java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE1") , JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (checkOpenFile(file.getAbsoluteFile().toString())) return;

        //riga="";

        JIFTextPane jtp;
        if (jCheckBoxWrapLines.isSelected()){
            jtp = new JIFTextPane(this, file);
        }
        else{
            jtp = new JIFTextPane(this, file){
                public boolean getScrollableTracksViewportWidth(){
                        if (getSize().width < getParent().getSize().width) return true;
                        return false;
                }

                public void setSize(Dimension d){
                    if (d.width < getParent().getSize().width) d.width = getParent().getSize().width;
                    super.setSize(d);
                }
            };
        }

        JIFScrollPane scroll= new JIFScrollPane(jtp,jtp.pathfile);
        scroll.setViewportView(jtp);

        //aggiungo la textarea per rowheader solo se il checkbox relativo è true
        if (jCheckBoxNumberLines.isSelected()){
            LineNumber lineNumber = new LineNumber( jtp );
            scroll.setRowHeaderView( lineNumber );
        }
        //jTabbedPane1.add(scroll, file.getAbsolutePath());
        jTabbedPane1.add(scroll, jtp.subPath);
        jTabbedPane1.setSelectedIndex(jTabbedPane1.getTabCount() - 1);

        //aggiungere al file config.ini il nome del file chiuso
        appendLastFile(file.getAbsolutePath());

        // cursore sulla prima riga
        jtp.setCaretPosition(0);

        //abilito i componenti
        enableComponents();

        //refresh tree
        refreshTree();

        lastFile = file.getAbsolutePath();

  }

  private void newAdventure(){
        // imposto il nome del file
        // il file da nuovo verrà chiamato: nuovo1, nuovo2, nuovo3.ecc
        fileInf = gamesDir+Constants.SEP+java.util.ResourceBundle.getBundle("JIF").getString("MSG_NEWFILE3")+(countNewFile++)+".inf";
        //hlighter = new HighlightText(Color.pink);
        
        // nuovo JTextPane ma non carico nessun file
        JIFTextPane jtp;
        if (jCheckBoxWrapLines.isSelected()){
            jtp = new JIFTextPane(this, null);
        }
        else{
            jtp = new JIFTextPane(this, null){
                public boolean getScrollableTracksViewportWidth(){
                        if (getSize().width < getParent().getSize().width) return true;
                        return false;
                }

                public void setSize(Dimension d){
                    if (d.width < getParent().getSize().width) d.width = getParent().getSize().width;
                    super.setSize(d);
                }
            };
        }

        jtp.setPaths(fileInf);
        JIFScrollPane scroll= new JIFScrollPane(jtp,jtp.pathfile);
        scroll.setViewportView(jtp);

        //aggiungo la textarea per rowheader solo se il checkbox relativo è true
        if (jCheckBoxNumberLines.isSelected()){
            LineNumber lineNumber = new LineNumber( jtp );
            scroll.setRowHeaderView( lineNumber );
        }
        //jTabbedPane1.add(scroll, fileInf);
        jTabbedPane1.add(scroll, jtp.subPath);
        jTabbedPane1.setSelectedIndex(jTabbedPane1.getTabCount() - 1);

        //abilito i componenti
        enableComponents();

        // refrehTree
        refreshTree();
  }

   private void clearOutput(){
        jTextAreaOutput.setText("");
   }




   // carico il contenuto del menu tasto destro e le altre configurazioni
   private void loadConfig() {

        try{
           File fileprova = new File("Jif.jar");
           
           // per test se il file.exists() è false significa che lo sto eseguendo da NetBeans
           if (fileprova.exists()){
               String tmp = fileprova.getCanonicalPath();
               tmp = tmp.substring(0, tmp.lastIndexOf(Constants.SEP)+1);
               workingDir = tmp;
           }
        } catch(IOException e){
           System.err.println(e.getMessage());
        }

        // Menu localizzato in base alla lingua
        configDir = workingDir+"config"+Constants.SEP+java.util.ResourceBundle.getBundle("JIF").getString("CONFIG_MENU");
        //configDir = Constants.userDir+"config"+Constants.SEP+java.util.ResourceBundle.getBundle("JIF").getString("CONFIG_MENU");
        File file = new File(configDir);
        if (!(file.exists())){
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER1")+configDir,java.util.ResourceBundle.getBundle("JIF").getString("ERR_GENERIC") , JOptionPane.ERROR_MESSAGE);
            return;
        }

        //MENU TASTO DESTRO
        // tutte le directory nella dir configDir vengono lette e letti i file menu.jnf
        BufferedReader br;
        // da eseguire solo una volta
        if (!loaded){
            File subMenues[] = file.listFiles();
            riga="";
            String id="", name="";
            JMenu menu = null;

            try{
                // per ogni directory, aggiungo il relativo menu di livello 1 e
                // di livello 2 lo aggiungo
                int length = subMenues.length;
                for (int count=0; count < length; count++){
                    // Add menu
                    if (!(subMenues[count].getName().equalsIgnoreCase("CVS"))){
                        menu = new JMenu(subMenues[count].getName());
                        menu.setFont(new Font("Dialog",Font.PLAIN,11));
                        jMenuInsertNew.add(menu);
                        // adding a sub-menu

                        File inifile = null;
                        boolean completed = false;
                        int i = 0 ;

                        // taking the menu.ini file, ignoring the other files and CVS directory
                        while (!completed){
                            inifile = subMenues[count].listFiles()[i];
                            if (inifile != null && inifile.getAbsolutePath().indexOf(".ini")!=-1){
                                completed = true;
                            }
                            i++;
                        }

                        if (inifile != null){
                            br = new BufferedReader(new FileReader(inifile));
                            while ((riga = br.readLine())!=null){
                                // salto le righe di commento che iniziano per Constants.TOKENCOMMENT=#
                                if (!(riga.startsWith(Constants.TOKENCOMMENT))&&!(riga.equals(""))   ){
                                    int indicevirgola = riga.indexOf(',');
                                    id   = riga.substring(0,indicevirgola);
                                    name = riga.substring(indicevirgola+1,riga.length());
                                    JMenuItem mi = new JMenuItem(id);
                                    mi.setFont(new Font("Dialog",Font.PLAIN,11));

                                    // Inserimento tooltip
                                    String tmp = Utils.replace(name,"§","<br>");
                                    // limito la dimensione della preview...
                                    if (tmp.length() > 700) tmp = tmp.substring(0,600);
                                    mi.setToolTipText("<html>"+tmp+"</html>");
                                    // Inserimento tooltip

                                    menu.add(mi).addMouseListener(menuListener);
                                    operations.put((String)id,(String) name);
                                }
                            }
                            br.close();
                        }
                    }
                }

            } catch(Exception e){
                System.err.println("ERROR loading Menu files....");
                System.err.println(e.getMessage());
            }


            // Carica i tutorial
            loadTutorial();


            // SWITCHES
            // apro il file switches.ini e imposto il Vector switches
            // questo va effettuato una sola volta: uso il flag loaded
            try{
                file = new File(workingDir+"config"+Constants.SEP+java.util.ResourceBundle.getBundle("JIF").getString("CONFIG_SWITCHES_INI"));
                if (!(file.exists())){
                    System.out.println(java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE3"));
                    return;
                }
                br = new BufferedReader(new FileReader(file));
                switches = new Hashtable();
                String idSwitch="",valueSwitch="",infoSwitch="";
                StringTokenizer st;
                Checkbox check;
                int switchNormali=0;
                int switchLunghi=0; 
                // gli switch tipo -v5,-v6 ecc vengono memorizzati in flags
                flags = new Vector();
                flags_language = new Vector();

                while ((riga = br.readLine())!=null){
                    //salto le di commento che iniziano per Constants.TOKENCOMMENT=#
                    if (!(riga.startsWith(Constants.TOKENCOMMENT))&&!(riga.equals(""))){
                        //per ogni switch aggiungo un checkbox nella dialog
                        st = new StringTokenizer(riga,",");
                        idSwitch = st.nextToken();
                        valueSwitch = st.nextToken();
                        infoSwitch = st.nextToken();

                        switches.put(idSwitch,infoSwitch);
                        check = new Checkbox(idSwitch);
                        check.setFont(new Font("Monospaced", Font.PLAIN, 11));

                        check.addMouseListener(new java.awt.event.MouseAdapter() {
                        // MouseListener per descrivere l'azione dello switch
                        public void mouseEntered(java.awt.event.MouseEvent evt) {
                            Checkbox ch = (Checkbox) evt.getSource();
                            //System.out.println(ch.getLabel());
                            jLabel3.setText((String)switches.get(ch.getLabel()));
                            jLabel4.setText(ch.getLabel());
                            }

                        // Se attivo -v5, -v6 -v8 sono disattivati...
                        public void mousePressed(java.awt.event.MouseEvent evt){
                        Checkbox ch = (Checkbox) evt.getSource();

                            // se il checkbox è stato selezionato disabilito tutto quelli con -v...
                            if (ch.getLabel().startsWith("-v")){
                            int size = flags.size();
                            for (int i=0; i<size;i++) {
                                if (!(ch.equals(((Checkbox)flags.get(i)))))
                                    ((Checkbox)flags.get(i)).setState(false);
                            }
                            }

                            // se il checkbox è stato selezionato disabilito tutto quelli con +language...
                            if (ch.getLabel().startsWith("+language_name=")){

                                int size = flags_language.size();
                                for (int i=0; i<size;i++) {
                                    if (!(ch.equals(((Checkbox)flags_language.get(i))))){
                                        ((Checkbox)flags_language.get(i)).setState(false);
                                    }
                                }
                            }
                        }

                        });

                        check.setState(valueSwitch.equals("on")?true:false);

                        if (idSwitch.startsWith("-v")) flags.add(check);
                        if (idSwitch.startsWith("+language_name=")) flags_language.add(check);

                        // lo inserisco nel panel3 se la label è corta, altrimenti
                        // lo inserisco nel panel24
                        if (idSwitch.length()<4){
                            jPanelSwitch1.add(check);
                            switchNormali++;
                        }
                        else{
                            jPanelSwitch2.add(check);
                            switchLunghi++;
                        }
                    }
                }
                br.close();
            } catch(Exception e){
                System.err.println("ERROR WHILE LOADING "+workingDir+"config"+Constants.SEP+java.util.ResourceBundle.getBundle("JIF").getString("CONFIG_SWITCHES_INI"));
                System.err.println(e.getMessage());
            }



        // carico la configurazione di JIF
        loadJifConfiguration(new File(this.workingDir+"config"+Constants.SEP+"config.jif"));

        // CARICO I LINK DA AGGIUNGERE COME MENU
        try{
            file = new File(workingDir+"config"+Constants.SEP+java.util.ResourceBundle.getBundle("JIF").getString("CONFIG_LINKS"));
            if (!(file.exists())){
                System.out.println(java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE5"));
                return;
            }
            br = new BufferedReader(new FileReader(file));

            while ((riga = br.readLine())!=null){

                if (!(riga.startsWith(Constants.TOKENCOMMENT))&&!(riga.equals(""))){
                        JMenuItem mi = new JMenuItem(riga);
                        mi.setFont(new Font("Dialog",Font.PLAIN,11));

                        mi.addActionListener(new java.awt.event.ActionListener() {
                        // MouseListener per descrivere l'azione dello switch
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            try{
                                String testo= ((JMenuItem)evt.getSource()).getText();

                                // controllo l'esistenza del browser
                                checkBrowser();

                                Runtime rt = Runtime.getRuntime();
                                String auxBrowser[]=new String[2];
                                auxBrowser[0]=defaultBrowser;
								auxBrowser[1]=testo;
                                rt.exec(auxBrowser); //Process proc =  unused

                            } catch(Exception e){
                                System.out.println(e.getMessage());
                                System.err.println(e.getMessage());
                            }
                            }
                        });
                        jMenuLinks.add(mi);
            }
            }
            br.close();
            } catch(Exception e){
                System.err.println("ERROR WHILE LOADING "+ workingDir+"config"+Constants.SEP+java.util.ResourceBundle.getBundle("JIF").getString("CONFIG_LINKS"));
                System.err.println(e.getMessage());
            }
        } // end if loaded==true



        // CONFIG
        // apro il file di config e imposto le directory
        try{
            file = new File(workingDir+"config"+Constants.SEP+"config.ini");
            if (!(file.exists())){
                    // Se il file non esiste lo creo con i valori di default,
                    // ovvero prendo la directory attuale del file jif.jar
                    
                    makeConfigIni si=new makeConfigIni();
                    
                    //salvo il file default
                    PrintStream ps;
                    try{
                        FileOutputStream fos = new FileOutputStream(file);
                        ps = new PrintStream( fos );
                        ps.println (si.makeConfig());
                        ps.close();
                    } catch(IOException e ){
                        System.out.println(e.getMessage());
                        System.err.println(e.getMessage());
                    }
            }

            br = new BufferedReader(new FileReader(file));

            while ((riga = br.readLine())!=null){
                //salto le di commento che iniziano per Constants.TOKENCOMMENT=#
                if (!(riga.startsWith(Constants.TOKENCOMMENT))&&!(riga.equals(""))){
                    if (riga.indexOf("libPath=")!=-1){libPath=riga.substring(riga.indexOf("libPath=")+8);}
                    if (riga.indexOf("libPathSecondary1=")!=-1){libPathSecondary1=riga.substring(riga.indexOf("libPathSecondary1=")+18);}
                    if (riga.indexOf("libPathSecondary2=")!=-1){libPathSecondary2=riga.substring(riga.indexOf("libPathSecondary2=")+18);}
                    if (riga.indexOf("libPathSecondary3=")!=-1){libPathSecondary3=riga.substring(riga.indexOf("libPathSecondary3=")+18);}
                    if (riga.indexOf("gamesDir=")!=-1){gamesDir=riga.substring(riga.indexOf("gamesDir=")+9);}
                    if (riga.indexOf("interpreter=")!=-1){interpreter=riga.substring(riga.indexOf("interpreter=")+12);}
                    if (riga.indexOf("glulx=")!=-1){glulx=riga.substring(riga.indexOf("glulx=")+6);}
                    if (riga.indexOf("compiler=")!=-1){compiler=riga.substring(riga.indexOf("compiler=")+9);}
                    if (riga.indexOf("defaultBrowser=")!=-1){defaultBrowser=riga.substring(riga.indexOf("defaultBrowser=")+15);}
                    if (riga.indexOf("BRESLOCATION=")!=-1){jTextFieldBres.setText(riga.substring(riga.indexOf("BRESLOCATION=")+13));}
                    if (riga.indexOf("BLCLOCATION=")!=-1) {jTextFieldBlc.setText( riga.substring(riga.indexOf("BLCLOCATION=")+12));}
                }
            }
            br.close();
        } catch(Exception e){
            System.err.println("ERROR WHILE LOADING "+ workingDir+"config"+Constants.SEP+"config.ini");
            System.err.println(e.getMessage());
        }



        // CARICO I FILES RECENTI
        //azzero il menu
        try{
            jMenuRecentFiles.removeAll();
            file = new File(workingDir+"config"+Constants.SEP+"recentfiles.ini");

            // se il file non esiste ne creo uno vuoto
            if (!file.exists()){
                file.createNewFile();
            }

            br = new BufferedReader(new FileReader(file));
            String nomefile;
            while ((riga = br.readLine())!=null){
                //aggiungo gli ultimi file aperti
                if (riga.indexOf("recentfile=")!=-1){
                    nomefile = riga.substring(riga.indexOf("recentfile=")+11);
                    JMenuItem mi = new JMenuItem(nomefile);
                    mi.setFont(new Font("Dialog",Font.PLAIN,11));
                    mi.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        //System.out.println("sono qui open recent file");
                        openFile(((javax.swing.JMenuItem)evt.getSource()).getText());
                        }
                    });
                    jMenuRecentFiles.add(mi);
                }
            }
            br.close();
        } catch(Exception e){
            System.err.println("ERROR WHILE LOADING "+ workingDir+"config"+Constants.SEP+"recentfiles.ini");
            System.err.println(e.getMessage());
        }


        loaded = true;

        //imposto la directory di insert new = gamesDir, se != da null
        if (
            insertnewdir.equals("") ||
            insertnewdir == null){
            insertnewdir = gamesDir;
        }


        //HELPED CODE
        // apro il file helpedcode.ini e imposto la helpcode
        try{
        	file = new File(workingDir+"config"+Constants.SEP+java.util.ResourceBundle.getBundle("JIF").getString("HELPED_FILE"));
            if (!(file.exists())){
                    System.out.println(java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE4"));
                return;
            }
            br = new BufferedReader(new FileReader(file));
            String ident="" , value="";
            helpcode = new Hashtable();
            while ((riga = br.readLine())!=null){
                //salto le di commento che iniziano per Constants.TOKENCOMMENT=#
                //System.out.println("riga=["+ riga +"]");
                if (!(riga.startsWith(Constants.TOKENCOMMENT))&&!(riga.equals(""))){
                    ident   = riga.substring(0,riga.indexOf(','));
                    value = riga.substring(riga.indexOf(',')+1,riga.length());
                    helpcode.put(ident,value);
                    //System.out.println("id="+ident+"  value="+value);
                }
            }
            br.close();
        } catch(Exception e){
            System.err.println("ERROR WHILE LOADING "+ workingDir+"config"+Constants.SEP+"helpedcode.ini");
            System.err.println(e.getMessage());
        }



        // MAPPING
        // apro il file mapping.ini e imposto il Vector mappa
        try{
            file = new File(workingDir+"config"+Constants.SEP+"mapping.ini");
            if (!(file.exists())){
                System.out.println(java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE5"));
                return;
            }
            br = new BufferedReader(new FileReader(file));
            mappa = new Vector();
            mapping = new Hashtable();
            while ((riga = br.readLine())!=null){
                //salto le di commento che iniziano per Constants.TOKENCOMMENT=#
                //System.out.println("riga=["+ riga +"]");
                if (!(riga.startsWith(Constants.TOKENCOMMENT))&&!(riga.equals(""))){
                      mappa.add(riga);
                      mapping.put(riga.substring(0,riga.indexOf(',')), riga.substring(riga.indexOf(',')+1));
                      //System.out.println("MAPPING: inserito["+riga.substring(0,riga.indexOf(','))+"] con ["+riga.substring(riga.indexOf(',')+1)+"]");
                }
            }
            br.close();
        } catch(Exception e){
            System.err.println("ERROR WHILE LOADING "+ workingDir+"config"+Constants.SEP+"mapping.ini");
            System.err.println(e.getMessage());
        }


        // altkeys Management
        // Open altkeys.ini and set the altkeys Vector
        // set the executecommands Vector (for execute commands)
        try{
            String ident,value;
            file = new File(workingDir+"config"+Constants.SEP+"altkeys.ini");
            if (!(file.exists())){
                System.out.println(java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE6"));
                return;
            }
            br = new BufferedReader(new FileReader(file));
            altkeys = new Hashtable();
            executecommands = new Hashtable();
            
            while ((riga = br.readLine())!=null){
                //salto le righe di commento che iniziano per Constants.TOKENCOMMENT=#
                if (!(riga.startsWith(Constants.TOKENCOMMENT))&&!(riga.equals(""))){                    
                    // Check for command or key                    
                    if (riga.startsWith(Constants.TOKENCOMMAND)){
                        // Add the Command
                        riga = riga.substring(Constants.TOKENCOMMAND.length());
                        ident = riga.substring(0,riga.indexOf(','));
                        value = riga.substring(riga.indexOf(',')+1,riga.length());
                        executecommands.put(ident,value);                        
                    }else{
                        ident   = riga.substring(0,riga.indexOf(','));
                        value = riga.substring(riga.indexOf(',')+1,riga.length());
                        altkeys.put(ident,value);                        
                    }
                }
            }
            br.close();
        } catch(Exception e){
            System.err.println("File "+workingDir+"config"+Constants.SEP+"functionKeys.ini "+" MALFORMED");
            System.err.println(e.getMessage());
        }



        // Symbol
        // apro il file symbols.ini e imposto la JListSymbol
        try{
            file = new File(workingDir+"config"+Constants.SEP+"symbols.ini");
            if (!(file.exists())){
                    System.out.println(java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE7"));
                return;
            }
            br = new BufferedReader(new FileReader(file));
            Vector vettoresimboli = new Vector();
            while ((riga = br.readLine())!=null){
                //salto le di commento che iniziano per Constants.TOKENCOMMENT=#
                //System.out.println("riga=["+ riga +"]");
                if (!(riga.startsWith(Constants.TOKENCOMMENT))&&!(riga.equals(""))){
                    vettoresimboli.add(riga);
                }
            }
            jListSymbols.setListData(vettoresimboli);
            br.close();
        } catch(Exception e){
            System.err.println("ERROR WHILE LOADING "+ workingDir+"config"+Constants.SEP+"symbols.ini");
            System.err.println(e.getMessage());
        }


        // SYNTAX CODE
        // apro il file syntax.ini e imposto le hashtable
        try{
            file = new File(workingDir+"config"+Constants.SEP+java.util.ResourceBundle.getBundle("JIF").getString("SYNTAX_FILE"));
            if (!(file.exists())){
                    System.out.println(java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE8"));
                return;
            }
            br = new BufferedReader(new FileReader(file));

            keywords= new HashSet();
            attributes = new HashSet();
            properties= new HashSet();
            routines= new HashSet();
            verbs= new HashSet();

            keywords_cs= new HashSet();
            attributes_cs = new HashSet();
            properties_cs= new HashSet();
            routines_cs= new HashSet();
            verbs_cs= new HashSet();


            while ((riga = br.readLine())!=null){
                //salto le righe di commento che iniziano per Constants.TOKENCOMMENT=#
                //System.out.println("riga=["+ riga +"]");
                if (!(riga.startsWith(Constants.TOKENCOMMENT))&&!(riga.equals(""))){

                    //controllo se la riga inizia con [keyword]=
                    if (riga.startsWith("[keyword]=")){
                        keywords.add( riga.substring(10).toLowerCase());
                        keywords_cs.add( riga.substring(10));
                        //System.out.println("Ho inserito in Keyword= "+riga.substring(10));
                    }
                    if (riga.startsWith("[attribute]=")){
                        attributes.add ( riga.substring(12).toLowerCase());
                        attributes_cs.add ( riga.substring(12));                        
                        //System.out.println("Ho inserito in attribute= "+riga.substring(12));
                    }
                    if (riga.startsWith("[property]=")){
                        properties.add ( riga.substring(11).toLowerCase());
                        properties_cs.add ( riga.substring(11));
                        //System.out.println("Ho inserito in properties= "+riga.substring(11));
                    }
                    if (riga.startsWith("[routine]=")){
                        routines.add ( riga.substring(10).toLowerCase());
                        routines_cs.add ( riga.substring(10));                        
                        //System.out.println("Ho inserito in routines= "+riga.substring(10));
                    }
                    if (riga.startsWith("[verb]=")){
                        verbs.add ( riga.substring(7).toLowerCase());
                        verbs_cs.add ( riga.substring(7));
                        //System.out.println("Ho inserito in verbs= "+riga.substring(7));
                    }



                }
            }
            br.close();
        } catch(Exception e){
            System.err.println("ERROR WHILE LOADING "+ workingDir+"config"+Constants.SEP+java.util.ResourceBundle.getBundle("JIF").getString("SYNTAX_FILE"));
            System.err.println(e.getMessage());
        }
   }


    public void checkTree(String key){
        key = key.toLowerCase();
        String file;
        file = checkDefinition(key);
        if (null == file){
            return;
        }
        
        
        openFile(file);
        JIFTextPane jif = getCurrentJIFTextPane();
        
        // clycle on the tree's nodes
        for (int c=0; c < top.getChildCount(); c++){
            DefaultMutableTreeNode mainnode = (DefaultMutableTreeNode)top.getChildAt(c);
            
            for (int k=0; k < mainnode.getChildCount(); k++){
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)mainnode.getChildAt(k);
                if (node == null){
                    continue;
                }

                // for the classes node
                if(node.children().hasMoreElements()){
                    for (int j=0; j < node.getChildCount(); j++){
                        DefaultMutableTreeNode nodeclass = (DefaultMutableTreeNode)node.getChildAt(j);
                        Object nodo = nodeclass.getUserObject();
                        Inspect ins = (Inspect) nodo;
                        if ( ins.Ilabel.equals(key)  ){
                            try{
                                if (ins != null){
                                    jif = getCurrentJIFTextPane();
                                    jif.getHlighter().highlightFromTo(jif, ins.Iposition , ins.IpositionEnd);
                                    el = jif.getDocument().getDefaultRootElement();
                                    jif.scrollRectToVisible(jif.modelToView(jif.getDocument().getLength()));
                                    jif.scrollRectToVisible(jif.modelToView(ins.Iposition));
                                }

                            }catch(Exception e){
                                System.err.println(e.getMessage());
                            }
                        }  
                    }
                    
                }
                else{
                    Object nodo = node.getUserObject();
                    Inspect ins = (Inspect) nodo;
                    if ( ins.Ilabel.equals(key)  ){
                        try{
                            if (ins != null){
                                jif = getCurrentJIFTextPane();
                                jif.getHlighter().highlightFromTo(jif, ins.Iposition , ins.IpositionEnd);
                                el = jif.getDocument().getDefaultRootElement();
                                jif.scrollRectToVisible(jif.modelToView(jif.getDocument().getLength()));
                                jif.scrollRectToVisible(jif.modelToView(ins.Iposition));
                            }

                        }catch(Exception e){
                            System.err.println(e.getMessage());
                        }
                    }    
                }               
        }            
        }
    }    
    
   

    // Funzione per il refresh dell'albero
    public void refreshTree(){
        // Se eseguo il refresh tree e non ho nessun file aperto,
        // resetto solo l'albero
        if (jTabbedPane1.getTabCount() == 0){
                // cancello il contenuto dell'albero
                category1.removeAllChildren();
                category2.removeAllChildren();
                //category3.removeAllChildren();
                category4.removeAllChildren();
                category5.removeAllChildren();
                category6.removeAllChildren();
                category7.removeAllChildren();
                //richiamo il Garbage collector.un po di pulizia
                //System.gc();
                top.setUserObject("Inspect");
                treeModel.reload();
                jTextAreaOutput.setText("");    // Svuoto la textarea di output
                disableComponents();
                this.setTitle(getJifVersion());
                jTree1.setEnabled(false);
                return;
        }

        String currentName = getCurrentFilename();

        // imposto il titolo sulla barra in alto
        this.setTitle(getJifVersion() +" - " + currentName);

        // Se il file ha estensione != da INF e H
        // svuoto l'abero ed esco
        if (currentName.endsWith(".txt")||currentName.endsWith(".res")){
            //System.out.println("non è INF ne H");
            category1.removeAllChildren();
            category2.removeAllChildren();
            category4.removeAllChildren();
            category5.removeAllChildren();
            category6.removeAllChildren();
            category7.removeAllChildren();
            top.setUserObject("Inspect");
            treeModel.reload();
            jTree1.setEnabled(false);
            return;
        }

        jTree1.setEnabled(true);

        // cancello il contenuto dell'albero
        category1.removeAllChildren();
        category2.removeAllChildren();
        //category3.removeAllChildren();
        category4.removeAllChildren();
        category5.removeAllChildren();
        category6.removeAllChildren();
        category7.removeAllChildren();
        String nomefile = getCurrentFilename();
        top.setUserObject(nomefile.substring(nomefile.lastIndexOf(Constants.SEP)+1));
        treeModel.reload();

        String testo = getCurrentJIFTextPane().getText();
        StringTokenizer sttok;

        String pattern = "Global ";
        String appoggio=""; //  appoggio per controllare che non vi siano caratteri di commento
                            // ! prima del pattern
        int pos = 0;
        objTree = new Vector(); // serve per l'ordinamento
        //while ((pos = testo.indexOf(pattern, pos)) >= 0)       {
        while ((pos = Utils.IgnoreCaseIndexOf(testo,pattern, pos)) >= 0)       {        
            //ignoro le righe con commenti
            appoggio = getCurrentJIFTextPane().getRowAt(pos);
            appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio,pattern));
            if (appoggio.indexOf("!")==-1 && appoggio.trim().equals("")){
                sttok = new StringTokenizer(testo.substring(pos+pattern.length())," ;=");
                //category1.add(new DefaultMutableTreeNode( new Inspect(sttok.nextToken(),pos,pos+pattern.length())));
                objTree.add(new Inspect(sttok.nextToken().toLowerCase(),pos,pos+pattern.length()));
            }
            pos += pattern.length();
        }

        // sorting
        sortNodes(objTree,category1);


        pattern = "Constant ";
        pos = 0;
        objTree = new Vector();
        //while ((pos = testo.indexOf(pattern, pos)) >= 0)       {
        while ((pos = Utils.IgnoreCaseIndexOf(testo,pattern, pos)) >= 0)       {
            //ignoro le righe con commenti
            appoggio = getCurrentJIFTextPane().getRowAt(pos);
            appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio,pattern));
            if (appoggio.indexOf("!")==-1 && appoggio.trim().equals("")){
                sttok = new StringTokenizer(testo.substring(pos+pattern.length())," ;=");
                //category2.add(new DefaultMutableTreeNode( new Inspect(sttok.nextToken(),pos,pos+pattern.length())));
                objTree.add(new Inspect(sttok.nextToken().toLowerCase(),pos,pos+pattern.length()));
            }
            pos += pattern.length();
        }
        // Sorting
        sortNodes(objTree,category2);



        pattern = "Object ";
        String hang="";
        String tmp="";
        pos = 0;
        Vector objvett = new Vector();
        objTree = new Vector();
        //while ((pos = testo.indexOf(pattern, pos)) >= 0){
        while ((pos = Utils.IgnoreCaseIndexOf(testo,pattern, pos)) >= 0){        
            //ignoro le righe con commenti
            appoggio = getCurrentJIFTextPane().getRowAt(pos);
            int posizione_freccia=0;
            posizione_freccia = appoggio.lastIndexOf("->");
            appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio,pattern));
            appoggio = appoggio.trim();
            if (appoggio.indexOf("!")==-1 && appoggio.equals("")){
                if (posizione_freccia==-1) {
                    posizione_freccia=0;
                } else posizione_freccia -=3;

                tmp = testo.substring(pos+pattern.length()-1+posizione_freccia);
                if (tmp.trim().startsWith("\"")){
                    sttok = new StringTokenizer(tmp.trim(),"\"");
                }
                else{
                    sttok = new StringTokenizer(tmp," ;");
                }
                hang = sttok.nextToken();
                //category4.add(new DefaultMutableTreeNode( new Inspect(hang,pos,pos+pattern.length()-1)));
                objvett.add(hang);
                objTree.add(new Inspect(hang.toLowerCase(),pos,pos+pattern.length()-1));
            }
            pos += pattern.length();
        }

        // Sorting
        sortNodes(objTree,category4);



        pattern = "Sub";
        pos = 0;
        objTree = new Vector();
        tmp="";
        //while ((pos = testo.indexOf(pattern, pos)) >= 0){
        while ((pos = Utils.IgnoreCaseIndexOf(testo,pattern, pos)) >= 0){        
            //ignoro le righe con commenti
            appoggio = getCurrentJIFTextPane().getRowAt(pos);
            //appoggio = appoggio.substring(0, appoggio.indexOf(pattern));
            if (appoggio.indexOf("!")==-1 && appoggio.indexOf('[')>=0 && appoggio.indexOf(';')>=0){
                tmp = testo.substring(0,pos);
                tmp = tmp.substring(tmp.lastIndexOf('[')+1);
                tmp = tmp.trim();
                //category6.add(new DefaultMutableTreeNode( new Inspect( tmp+pattern,pos,pos+pattern.length())));
                objTree.add(new Inspect((tmp+pattern).toLowerCase(),pos,pos+pattern.length()));
            }
            pos += pattern.length();
        }
        // Sorting
        sortNodes(objTree,category6);




        // ***************** CLASSI
        Vector classi_locali= new Vector();
        pattern = "Class ";
        pos = 0;
        //while ((pos = testo.indexOf(pattern, pos)) >= 0)       {
        while ((pos = Utils.IgnoreCaseIndexOf(testo,pattern, pos)) >= 0){        
            //ignoro le righe con commenti
            appoggio = getCurrentJIFTextPane().getRowAt(pos);

            appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio,pattern));
            appoggio = appoggio.trim();

            if (appoggio.indexOf("!")==-1 && appoggio.equals("")){
//            if (appoggio.indexOf("!")==-1 && appoggio.startsWith("Class")){
                sttok = new StringTokenizer(testo.substring(pos+pattern.length())," ;\n");
                String nome = sttok.nextToken();
                classi_locali.add((String) nome);
                tmp_nodo = new DefaultMutableTreeNode( new Inspect(nome.toLowerCase(),pos,pos+pattern.length()));
                category7.add(tmp_nodo);
                getClasses(tmp_nodo,nome);
            }
            pos += pattern.length();
        }


        // se ho impostato il flag jCheckBoxScanProjectFiles a true
        if (jCheckBoxScanProjectFiles.isSelected() && null != projectClass){
                //Aggiungo tutte le classi degli altri file del progetto che non sono contenute in classi_locali
                for (int i=0 ; i < projectClass.size(); i++){
                    // se la classe alla posizione i non c'è in classi_locali la aggiungo all'albero
                    String classe = (String) projectClass.get(i);
                    if (!classi_locali.contains((String) classe)){
                        tmp_nodo = new DefaultMutableTreeNode( new Inspect(classe.toLowerCase(),-1,-1));
                        category7.add(tmp_nodo);
                        getClasses(tmp_nodo,classe);
                    }
                }
        }



        // ****** Functions
        pattern="[";
        pos=0;
        int lunghezza=0;
        tmp ="";
        objTree = new Vector();
        while ((pos = testo.indexOf(pattern, pos)) >= 0){
            //ignoro le righe con commenti
            appoggio = getCurrentJIFTextPane().getRowAt(pos);
            //appoggio = appoggio.substring(0, appoggio.indexOf(pattern));
            appoggio = appoggio.trim();
            if (appoggio.indexOf("!")==-1  && appoggio.startsWith("[")){
                tmp = testo.substring(pos);
                tmp = tmp.substring(1,tmp.indexOf(';'));
                tmp = tmp.trim();
                if (!tmp.equals("") &&
                    (tmp.indexOf('\"')==-1) &&
                    (tmp.indexOf("Sub"))==-1

                   ){
                    //System.out.println("ho trovato la funzione=["+tmp+"]");
                    // prendo solo la prima stringa...
                    sttok = new StringTokenizer(tmp," ;\n");
                    if (sttok.hasMoreTokens()){
                        tmp = sttok.nextToken();
                    }
                    lunghezza = tmp.length();
                    //category5.add(new DefaultMutableTreeNode( new Inspect( tmp ,pos+1+1,pos+lunghezza+2)));
                    objTree.add(new Inspect( tmp.toLowerCase() ,pos+1+1,pos+lunghezza+2));
                }
            }
            pos += pattern.length();
        }

        // Sorting
        sortNodes(objTree,category5);

        //APRO TUTTI I NODI DELL'ALBERO. se ho explode==true
        if (explode) expandAll(jTree1, true);
    }




    public void sortNodes(Vector vettore, DefaultMutableTreeNode nodo ){
        // Sorting nodes
        Collections.sort(vettore,new Comparator(){
            public int compare(Object a, Object b) {
                String id1 = ((Inspect)a).toString();
                String id2 = ((Inspect)b).toString();
                return (id1).compareToIgnoreCase(id2) ;
            }
        });
        int size = vettore.size();
        for(int count =0; count<size ; count++) {
            nodo.add(new DefaultMutableTreeNode((Inspect)vettore.get(count)));
        }
    }

    // If expand is true, expands all nodes in the tree.
    // Otherwise, collapses all nodes in the tree.
    public void expandAll(JTree tree, boolean expand) {
        TreeNode root = (TreeNode)tree.getModel().getRoot();
        // Traverse tree from root
        expandAll(tree, new TreePath(root), expand);
    }
    private void expandAll(JTree tree, TreePath parent, boolean expand) {
        // Traverse children
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }
        // Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }     



    public void loadConfigFiles(String filename){
        try{
            File file = new File(filename);
            if (!(file.exists())){
                System.out.println(java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE1")+filename);
                return;
            }

            jDialogConfigFiles.setSize(600,550);
            jDialogConfigFiles.setLocationRelativeTo(this);
            jDialogConfigFiles.setVisible(true);

            BufferedReader br = new BufferedReader(new FileReader(file));
            sb.setLength(0);

            while ((riga = br.readLine())!=null){
                sb.append(riga).append("\n");
            }
            jTextAreaConfig.setText(sb.toString());
            jLabel2.setText(filename);

            br.close();

            jTextAreaConfig.setCaretPosition(0);

        } catch (Exception e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
    }


    public StringBuffer getSwitchesForSaving(){
        StringBuffer make = new StringBuffer();

        make.append("# JIF SWITCHES\n")
        .append("# \n");

        Checkbox ch;
        for(int count=0; count < jPanelSwitch1.getComponentCount(); count++){
            ch = (Checkbox) jPanelSwitch1.getComponent(count);
            if (ch.getState()){
                make.append(ch.getLabel()+",on,"+(String)switches.get(ch.getLabel())+"\n");
            }
            else{
                make.append(ch.getLabel()+",off,"+(String)switches.get(ch.getLabel())+"\n");
            }
        }
        for(int count=0; count < jPanelSwitch2.getComponentCount(); count++){
            ch = (Checkbox) jPanelSwitch2.getComponent(count);
            if (ch.getState()){
                make.append(ch.getLabel()+",on,"+(String)switches.get(ch.getLabel())+"\n");
            }
            else{
                make.append(ch.getLabel()+",off,"+(String)switches.get(ch.getLabel())+"\n");
            }
        }

        return make;

    }

     public StringBuffer getSwitchesForSavingProject(){
        StringBuffer make = new StringBuffer();

        make
        .append("# **************** #\n")
        .append("# PROJECT SWITCHES #\n")
        .append("# **************** #\n");

        Checkbox ch;
        for(int count=0; count < jPanelSwitch1.getComponentCount(); count++){
            ch = (Checkbox) jPanelSwitch1.getComponent(count);
            if (ch.getState()){
                make.append("[SWITCH]="+ch.getLabel()+",on,"+(String)switches.get(ch.getLabel())+"\n");
            }
            else{
                make.append("[SWITCH]="+ch.getLabel()+",off,"+(String)switches.get(ch.getLabel())+"\n");
            }
        }
        for(int count=0; count < jPanelSwitch2.getComponentCount(); count++){
            ch = (Checkbox) jPanelSwitch2.getComponent(count);
            if (ch.getState()){
                make.append("[SWITCH]="+ch.getLabel()+",on,"+(String)switches.get(ch.getLabel())+"\n");
            }
            else{
                make.append("[SWITCH]="+ch.getLabel()+",off,"+(String)switches.get(ch.getLabel())+"\n");
            }
        }

        return make;

    }



    // dal componente JDialogSwitches restituisce la stringa degli switch da usare
    public String makeSwitches(){
        // In questo metodo recuper il formato del file .z3,.z4,.z5,.z6,.z8
        // per passare all'interprete il nome del file corretto.
        StringBuffer make = new StringBuffer();
        //System.out.println("ci sono "+jPanel3.getComponentCount()+" check");
        Checkbox ch;
        for(int count=0; count < jPanelSwitch1.getComponentCount(); count++){
            ch = (Checkbox) jPanelSwitch1.getComponent(count);
            if (ch.getState()){
                //System.out.println(ch.getLabel());
                // lo aggiungo solo se sono in INFORM MODE
                if (jCheckBoxInformMode.isSelected()){
                    make.append(" "+ch.getLabel());
                }
                // GLULX MODE
                else if (ch.getLabel().indexOf("-v")==-1){
                    make.append(" "+ch.getLabel());
                }

                if (ch.getLabel().indexOf("-v")!=-1){
                    tipoz = ch.getLabel();
                }
            }

        }

         for(int count=0; count < jPanelSwitch2.getComponentCount(); count++){
            ch = (Checkbox) jPanelSwitch2.getComponent(count);
            if (ch.getState()){
                //System.out.println(ch.getLabel());
                make.append(" "+ch.getLabel());
                if (ch.getLabel().indexOf("-v")!=-1)    tipoz = ch.getLabel();
            }

        }

        // dopo il ciclo se nessuno swith di tipo -v2, -v3  -v8
        // è stato selezionato assumo per default lo swith -v5
        if (tipoz.equals("")) tipoz="-v5";

        // If in GLULX MODE, a "-G" switch is to be added
        if (jCheckBoxGlulxMode.isSelected()){
            make.append(" -G");
        }
        return make.toString();
    }

    public void exitJif(){

        if ((fileInf==null) ||  (jTabbedPane1.getTabCount())==0) {
            saveJifIni();
            System.exit(0);
        }

        // controllo che non ci sia almeno un file da salvare con "*" nel nome
        boolean ok = true;
        int numberOfComponents = jTabbedPane1.getTabCount();
        for (int count=0; count < numberOfComponents; count++){
            if (jTabbedPane1.getTitleAt(count).endsWith("*")){
                ok = false;
            }
        }
        if (ok){
            saveJifIni();
            System.exit(0);
        }

        String[] scelte = new String[3];
        scelte[0] = java.util.ResourceBundle.getBundle("JIF").getString("MSG_SAVE_AND_EXIT");
        scelte[1] = java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF10");
        scelte[2] = java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF11");
        int result = JOptionPane.showOptionDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF12") , java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF13") , 0 , JOptionPane.INFORMATION_MESSAGE , null , scelte , scelte[2]);

        if (result==0){
            //salva ed esci
	    //it must save the project file [ Bug #2997 ]
            saveAll();
            saveJifIni();
	    saveProject();
            System.exit(0);
        }
        if (result==1){
            //esci senza salvare
            saveJifIni();
            System.exit(0);
        }
        //return; unused
    }


    //    per correggere errori tipo:
    //    Class Widget;
    //    Widget Blue_Widget "widget";
    //    per ogni classe nuova aggiungo al nodo passato, il nome degli oggetti di quella classe
    public void getClasses(DefaultMutableTreeNode nodo, String nome){
        //per ogni parola trovata la cerco nel file e la inserisco nel nodo corrente
        String tmp;
        String pattern = nome+" ";  //aggiungo uno spazio
        String testo = getCurrentJIFTextPane().getText();
        String target;
        StringTokenizer sttok;
        int pos=0;
        //while ((pos = testo.indexOf(pattern, pos)) >= 0){
        while ((pos = Utils.IgnoreCaseIndexOf(testo,pattern, pos)) >= 0){        
            //gestione -> freccia
            String appoggio = getCurrentJIFTextPane().getRowAt(pos);
            // if appoggio starts with a comment ! has to be ignored
            if (!appoggio.trim().startsWith("!") && appoggio.trim().startsWith(nome) ){
                int posizione_freccia=0;
                posizione_freccia = appoggio.lastIndexOf("->");
                if (posizione_freccia==-1) {
                    posizione_freccia=0;
                }
                else posizione_freccia = posizione_freccia + 3 - pattern.length();

                tmp = testo.substring(pos+pattern.length()+posizione_freccia);
                if (tmp.trim().startsWith("\"")){
                    sttok = new StringTokenizer(tmp.trim(),"\"");
                }
                else{
                    sttok = new StringTokenizer(tmp," ;");
                }
                target = sttok.nextToken();
                // se la riga corrente non contiene la word "Class "
                //if ( (getCurrentJIFTextPane().getRowAt(pos)).indexOf("Class ")==-1){
                if ( Utils.IgnoreCaseIndexOf(getCurrentJIFTextPane().getRowAt(pos),"Class ")==-1){
                    
                    //System.out.println("RIGA="+getCurrentJIFTextPane().getRowAt(pos));

                    // Add the node only if the char at pos-1 is
                    // A) TAB char or
                    // B) Space Char or
                    // B) \n char
                    String test = "";
                    try{
                        test = getCurrentJIFTextPane().getText(pos-1,1);
                    } catch (Exception e){
                        System.out.println(e.getMessage());
                    }

                    if (test.equals(" ") || test.equals("\t") || test.equals("\n")){
                        nodo.add(new DefaultMutableTreeNode( new Inspect(target.toLowerCase(),pos,pos+pattern.length())));
                    }
                }
            }
            pos += pattern.length();
        }
    }





    public static final JIFTextPane getCurrentJIFTextPane(){
        if (jTabbedPane1.getTabCount()==0){
            return null;
        }
        else{
            return (JIFTextPane)((JScrollPane)jTabbedPane1.getSelectedComponent()).getViewport().getComponent(0);
        }
    }


    public static final DefaultStyledDocument getCurrentDoc(){
        if (jTabbedPane1.getTabCount()==0){
            return null;
        }
        else{
            return (DefaultStyledDocument)(((JIFTextPane)((JScrollPane)jTabbedPane1.getSelectedComponent()).getViewport().getComponent(0)).getDocument() );
        }
    }

    public static final String getCurrentTitle(){
        return(jTabbedPane1.getTitleAt(jTabbedPane1.getSelectedIndex()));
    }

    public static final String getTitleAt(int aTabNumber){
        return(jTabbedPane1.getTitleAt(aTabNumber));
    }    
    
    public static final String getCurrentFilename(){
        JIFScrollPane aScrollPane;
        if (jTabbedPane1.getTabCount() == 0){
            return null;
        }
        else{
                aScrollPane=(JIFScrollPane)jTabbedPane1.getComponentAt(jTabbedPane1.getSelectedIndex());
            return(aScrollPane.getFile());
        }
    }

    public static final String getFilenameAt(int aTabNumber){
        JIFScrollPane aScrollPane;
        if (jTabbedPane1.getTabCount() == 0){
            return null;
        }
        else{
            aScrollPane=(JIFScrollPane)jTabbedPane1.getComponentAt(aTabNumber);
            return(aScrollPane.getFile());
            //return jTabbedPane1.getTitleAt(jTabbedPane1.getSelectedIndex());
        }
    }    
    
    // modificata
    public void appendLastFile(String recentfileToAppend){
        //1. apro il file lastfiles.ini
        //2. carico tutti i file recenti in un vettore
        //3. se la dimensione è minore della dimensione massima, aggiungo il file recentfile nel vettore
        //3a. altrimenti rimuovo l'elemento del vettore alla posizione 0 e passo al punto 3
        //4. scarico il vettore nel file
        try{
            // leggo il file
            File file = new File(workingDir+"config"+Constants.SEP+"recentfiles.ini");

            // se il file non esiste, lo creo ex-novo
            if (!file.exists()){
                file.createNewFile();
            }

            Vector filerecenti = new Vector();
            String recentfile;

            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((riga = br.readLine())!=null){
                //if ( (riga.indexOf("recentfile="+recentfile))!=-1 ){
                if (riga.startsWith("recentfile=")){
                    recentfile = riga.substring(riga.indexOf("recentfile=")+11);
                    filerecenti.addElement((String)recentfile);
                }
            }
            br.close();

            if (filerecenti.size() < maxRecentFiles){
                // prima controllo che il file non esita già
                // in questo caso, non faccio nulla
                if (!(filerecenti.contains((String) recentfileToAppend)))
                    filerecenti.addElement((String) recentfileToAppend);
            }
            else{
                int size = filerecenti.size();
                //se il vettore ha almeno dimensione = 1
                if (size>1){
                    if (!(filerecenti.contains((String) recentfileToAppend))){
                        // Shifto il vettore e appendo l'ultimo
                        for (int i=1; i<size;i++){
                            filerecenti.setElementAt((String)filerecenti.elementAt(i),i-1);
                        }
                        filerecenti.removeElementAt(size-1);
                        filerecenti.addElement((String) recentfileToAppend);
                    }
                }
                else {
                    if (!(filerecenti.contains((String) recentfileToAppend)))
                        filerecenti.addElement((String) recentfileToAppend);
                }
            }
            //scrivo il file
            PrintStream ps;
            FileOutputStream fos = new FileOutputStream(file);
            ps = new PrintStream( fos );



            int dim = filerecenti.size();
            for (int i=0; i<dim; i++){
                ps.println ("recentfile="+(String)filerecenti.elementAt(i));
            }
            ps.close();

            //ricarico la configurazione
            loadConfig();

        } catch(Exception e){
            System.out.println("ERR:"+e.getMessage());
            System.err.println(e.getMessage());
        }
    }


    // modifica
    public void clearLastFilesList(){
        try{
            File file = new File(workingDir+"config"+Constants.SEP+"recentfiles.ini");
            file.delete();
            //ricarico la configurazione
            loadConfig();
        } catch(Exception e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
    }

    public boolean checkOpenFile(String file){
        //Controllo che non sia stato già aperto un file
        int found = -1;
        String file_asterisco=file+"*";
        for (int count=0; count < jTabbedPane1.getTabCount(); count++){
            // I file aperti senza asterisco
            //if (file.equals( jTabbedPane1.getTitleAt(count))){
            if (file.equals(getFilenameAt(count) )){
                found = count;
            }
            // controllo anche i file che hanno l'asterisco
            if (file_asterisco.equals(getFilenameAt(count))){
                found = count;
            }
        }
        if (found != -1){
            //JOptionPane.showMessageDialog(this,java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE9"),java.util.ResourceBundle.getBundle("JIF").getString("ERR_GENERIC"), JOptionPane.ERROR_MESSAGE);
            jTabbedPane1.setSelectedIndex(found);
            refreshTree();
            return true;
        }
        return false;
    }


    public void disableComponents(){
        // disabilito i componenti che devono essere disbilitati, quando non ci sono file aperti
        SaveButton.setEnabled(false);
        SaveButtonAll.setEnabled(false);
        RebuildButton.setEnabled(false);
        RunButton.setEnabled(false);
        Save.setEnabled(false);
        SaveAs.setEnabled(false);
        jMenuItemSaveAll.setEnabled(false);
        jMenuItemClose.setEnabled(false);
        jMenuItemCloseAll.setEnabled(false);
        jMenuItemPrint.setEnabled(false);
        jButtonFind.setEnabled(false);
        jButtonSearchProject.setEnabled(false);
        jButtonReplace.setEnabled(false);
        jButtonCopy.setEnabled(false);
        jButtonCut.setEnabled(false);
        jButtonPaste.setEnabled(false);
        jButtonCommentSelection.setEnabled(false);
        jButtonUncommentSelection.setEnabled(false);
        jButtonLeftTab.setEnabled(false);
        jButtonRightTab.setEnabled(false);
        jButtonPrint.setEnabled(false);
        jButtonBracketCheck.setEnabled(false);

        //menu
        jMenuEdit.setEnabled(false);
        jMenuBuild.setEnabled(false);
        jButtonInsertSymbol.setEnabled(false);
        SaveAsButton.setEnabled(false);
        jButtonClose.setEnabled(false);
        jButtonCloseAll.setEnabled(false);
        jButtonInfo.setEnabled(false);
        jButtonUndo.setEnabled(false);
        jButtonRedo.setEnabled(false);
        jButtonExtractStrings.setEnabled(false);
        jButtonTranslate.setEnabled(false);
        jMenuMode.setEnabled(false);
        jTree1.setEnabled(false);
        if(jCheckBoxGlulxMode.getState())
            jMenuGlulx.setEnabled(false);
        // search and definition
        jTextFieldFind.setEnabled(false);
        jTextFieldDefinition.setEnabled(false);
        jButtonDefinition.setEnabled(false);
    }


    public void enableComponents(){
        // disabilito i componenti che devono essere disbilitati, quando non ci sono file aperti
        SaveButton.setEnabled(true);
        SaveButtonAll.setEnabled(true);
        RebuildButton.setEnabled(true);
        RunButton.setEnabled(true);
        Save.setEnabled(true);
        SaveAs.setEnabled(true);
        jMenuItemSaveAll.setEnabled(true);
        jMenuItemClose.setEnabled(true);
        jMenuItemCloseAll.setEnabled(true);
        jMenuItemPrint.setEnabled(true);
        jButtonFind.setEnabled(true);
        jButtonSearchProject.setEnabled(true);
        jButtonReplace.setEnabled(true);
        jButtonCopy.setEnabled(true);
        jButtonCut.setEnabled(true);
        jButtonPaste.setEnabled(true);
        jButtonCommentSelection.setEnabled(true);
        jButtonUncommentSelection.setEnabled(true);
        jButtonLeftTab.setEnabled(true);
        jButtonRightTab.setEnabled(true);
        jButtonPrint.setEnabled(true);
        jButtonBracketCheck.setEnabled(true);
        jMenuEdit.setEnabled(true);
        jMenuBuild.setEnabled(true);
        jButtonInsertSymbol.setEnabled(true);
        SaveAsButton.setEnabled(true);
        jButtonClose.setEnabled(true);
        jButtonCloseAll.setEnabled(true);
        jButtonInfo.setEnabled(true);
        jButtonUndo.setEnabled(true);
        jButtonRedo.setEnabled(true);
        jButtonExtractStrings.setEnabled(true);
        jButtonTranslate.setEnabled(true);
        jMenuMode.setEnabled(true);
        jTree1.setEnabled(true);
        if(jCheckBoxGlulxMode.getState())
            jMenuGlulx.setEnabled(true);
        
        jTextFieldFind.setEnabled(true);
        jTextFieldDefinition.setEnabled(true);
        jButtonDefinition.setEnabled(true);        
    }






    // dal dialog Replace
    public void findString(String pattern){
        // rimuovo tutti gli highligh
        // recupero la posizione del cursore
        // eeguo la ricerca e l'highlight del testo trovato
        int pos = getCurrentJIFTextPane().getCaretPosition();   // current position
        try{
                String text = getCurrentDoc().getText(0, getCurrentDoc().getLength());
                boolean trovato = false;

                while ( ( (pos = text.indexOf(pattern, pos)) >= 0) && (!trovato)){
                //while ( ( (pos = Utils.IgnoreCaseIndexOf(text,pattern, pos)) >= 0) && (!trovato)){                
                    getCurrentJIFTextPane().requestFocus();
                    getCurrentJIFTextPane().setCaretPosition(pos);
                    getCurrentJIFTextPane().setSelectionStart(pos);
                    getCurrentJIFTextPane().setSelectionEnd(pos+pattern.length());
                    getCurrentJIFTextPane().repaint();
                    pos += pattern.length();
                    trovato=true;
                    jDialogReplace.requestFocus();
                }

                //se non lo trovo comunico che sono alla fine del file
                if (!trovato) {
                    String[] scelte = new String[2];
                    scelte[0] = java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF18");
                    scelte[1] = java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_CANCEL");
                    int result = JOptionPane.showOptionDialog(jDialogReplace,  java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF19")+" ["+pattern+"]"+java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF20") , java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF21") , 0 , JOptionPane.INFORMATION_MESSAGE , null , scelte , scelte[1]);
                    if (result==0) {
                        getCurrentJIFTextPane().setCaretPosition(0);
                        findString(pattern);
                    }
                    return;
                }
            } catch (BadLocationException e)  {
                System.out.println(e.getMessage());
                System.err.println(e.getMessage());
            }
    }



    public void insertFromFile(){
        try{
                JFileChooser chooser = new JFileChooser(insertnewdir);
                chooser.setFileSelectionMode(chooser.FILES_ONLY);
                int returnVal = chooser.showOpenDialog(this);
                if(returnVal == JFileChooser.CANCEL_OPTION) {
                    return;
                }

                // apro il file e lo leggo
                sb.setLength(0);
                String result = chooser.getSelectedFile().getAbsolutePath();

                //imposto la insertnewdir= a quella selezionata l'ultima volta
                insertnewdir = result.substring(0, result.lastIndexOf(Constants.SEP));
                BufferedReader br = new BufferedReader(new FileReader(new File(result)));
                while ((riga = br.readLine())!= null){
                    sb.append(riga+"\n");
                }
                getCurrentDoc().insertString(getCurrentJIFTextPane().getCaretPosition(), sb.toString(), attr);

                // aggiorno albero
                refreshTree();
            }
            catch (Exception e){
                System.out.println(e.getMessage());
                System.err.println(e.getMessage());
            }
    }


    public void closeFile(){
            // se i il file non contiene * allora posso chiudere senza salvare
            if (getCurrentTitle().indexOf("*")!=-1){
                String[] scelte = new String[3];
                scelte[0] = java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF14");
                scelte[1] = java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF15");
                scelte[2] = java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_CANCEL");
                //int result = JOptionPane.showOptionDialog(null, java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF16") , java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF17")+ jTabbedPane1.getTitleAt(jTabbedPane1.getSelectedIndex()), 0 , JOptionPane.INFORMATION_MESSAGE , null , scelte , scelte[2]);
                int result = JOptionPane.showOptionDialog(null, java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF16") , java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF17")+ getCurrentFilename(), 0 , JOptionPane.INFORMATION_MESSAGE , null , scelte , scelte[2]);
                //salva e chiudi
                if (result == 0){
                    saveFile();
                    Component comp = jTabbedPane1.getSelectedComponent();
                    jTabbedPane1.remove(comp);
                    return;
                }
                if (result == 1){
                    //chiudi
                    Component comp = jTabbedPane1.getSelectedComponent();
                    jTabbedPane1.remove(comp);
                    return;
                }
                return;
            }
            else{
                // chiudo senza chiedere
                Component comp = jTabbedPane1.getSelectedComponent();
                jTabbedPane1.remove(comp);
                return;
            }
        }



    // 1.cerco la prima occorrenza se non esiste esci
    // 2. replace string e vai al punto 1.
    public void replaceAll(){
        boolean eseguito=false;
        while (!eseguito){
            eseguito = findAllString(jTextFieldReplaceFind.getText());
            if (!eseguito){
                getCurrentJIFTextPane().replaceSelection(jTextFieldReplace.getText());
            }
        }
    }

    // dal dialog ReplaceAll
    public boolean findAllString(String pattern){
        // rimuovo tutti gli highligh
        // recupero la posizione del cursore
        // eeguo la ricerca e l'highlight del testo trovato

        int pos = getCurrentJIFTextPane().getCaretPosition();   // current position
        try{
                String text = getCurrentDoc().getText(0, getCurrentDoc().getLength());
                boolean trovato = false;
                while ( ( (pos = text.indexOf(pattern, pos)) >= 0) && (!trovato))       {
                    getCurrentJIFTextPane().requestFocus();
                    getCurrentJIFTextPane().setCaretPosition(pos);
                    getCurrentJIFTextPane().setSelectionStart(pos);
                    getCurrentJIFTextPane().setSelectionEnd(pos+pattern.length());
                    getCurrentJIFTextPane().repaint();
                    pos += pattern.length();
                    trovato=true;
                    jDialogReplace.requestFocus();
                }

                //se non lo trovo comunico che sono alla fine del file
                if (!trovato) {
                    return true;
                }
                else return false;
            } catch (BadLocationException e)  {
                System.out.println(e.getMessage());
                System.err.println(e.getMessage());
            }
        return false;
    }


    public void copyToClipBoard(){
      // Prendo il testo selezionato e prendo la substring fino al primo carattere \n
      // e lo inserisco come testo del menu

        // controllo che che non venga superato il limite max di entry nel menu PASTE
        if (jMenuPaste.getMenuComponentCount() > Constants.MAX_DIMENSION_PASTE_MENU) {
            //System.out.println("superato dimensione max per menu");
            return;
        }

        // come titolo del menu, limito al max a 8 caratteri
        // il testo incollato, sarà contenuto nel tooltip, opportunamente
        // modificato PLAIN -> HTML  e HTML -> PLAIN
        String test = getCurrentJIFTextPane().getSelectedText();

        StringSelection ss = new StringSelection(test);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);

        if (test.trim().length()> 25) test = test.trim().substring(0,25)+"...";
        JMenuItem mi = new JMenuItem(test.trim());

        //Come tool tip del menu metto tutto il codice selezionato
        String tmp = getCurrentJIFTextPane().getSelectedText();
        // per vederlo tutto su più righe....lo trasformo il testo in formato HTML
        tmp = Utils.replace(tmp,"\n","<br>");
        mi.setToolTipText("<html>"+tmp+"</html>");
        mi.setFont(new Font("Dialog",Font.PLAIN,11));
        jMenuPaste.add(mi).addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    try{
                    //String id = ((javax.swing.JMenuItem)evt.getSource()).getText();
                    String id = ((javax.swing.JMenuItem)evt.getSource()).getToolTipText();

                    //ricostruisco la stringa... da html a plain text
                    id = Utils.replace(id,"<br>","\n");
                    id = Utils.replace(id,"<html>","");
                    id = Utils.replace(id,"</html>","");
                    getCurrentDoc().insertString(getCurrentJIFTextPane().getCaretPosition(), id , attr);
                    } catch(BadLocationException e){
                        System.out.println(e.getMessage());
                        System.err.println(e.getMessage());
                    }
                }
            });
    }




    // Restituisce il vettore con tutti i files inclusi
    public Vector getIncludedFiles(String testo){
        Vector includedFiles = new Vector();
        String riga="";
        String stringa ="";
        String pattern="Include \">";
        int pos = 0;
        while ((pos = testo.indexOf(pattern, pos)) >= 0){
            riga = getCurrentJIFTextPane().getRowAt(pos);
            StringTokenizer st = new StringTokenizer(riga.substring(riga.indexOf(">")),">\"");
            stringa = st.nextToken();

            // controllo il checkbox file h
            if (stringa.endsWith(".h")){
                if (this.jCheckBoxMappingHFile.isSelected()){
                    includedFiles.add(stringa);
                }
            }
            else{
                includedFiles.add(stringa);
            }
            pos += pattern.length();
        }
        return includedFiles;
    }


    // esegue il mapping in automatico di tutti i files inclusi
    // NON CAMBIA IL NOME AL FILE E LO METTE IN PUBLISH
    public void runMappingIncludedFilesPublish(Vector includedFiles){
        String nomeFile="";
        StringBuffer testo;
        String output="";
        File file;
        BufferedReader br;

        for(int i=0; i< includedFiles.size() ;i++){
        testo= new StringBuffer();
        nomeFile = (String)includedFiles.get(i);


        // per ogni file, lo leggo e creo il file mapping_XXX.inf
        file = new File(gamesDir+Constants.SEP+nomeFile);
        if (!(file.exists())){
            //inserire l'errore nella JTextAreaOutput
            System.out.println(java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE4"));
            return;
        }
        try{
            br = new BufferedReader(new FileReader(file));
            while ((riga = br.readLine())!=null){
                testo.append(riga).append("\n");
            }
            br.close();
        } catch(Exception e){
            System.out.println("ERRORE ="+e.getMessage());
            System.err.println(e.getMessage());
        }
        output= testo.toString();

        int pos=0;
        int old=0;
        boolean entrato = false;
        // da MAPPA recupero TUTTI i caratteri da sostituire e li sostituisco
        for (int count=0; count < mappa.size(); count++){
            pos=0;
            old=0;
            entrato=false;
            //sb = new StringBuffer();
            sb.setLength(0);
            String riga = (String)mappa.get(count);
            String index = riga.substring(0,riga.indexOf(','));
            String value = riga.substring(riga.indexOf(',')+1);
            //System.out.println("idex="+index+"  "+"value="+value);

            while ((pos = output.indexOf(index, pos)) >= 0){
                sb.append(output.substring(old,pos));
                sb.append(value);
                pos ++;
                old = pos;
                entrato = true;
            }
            if (entrato) {
                sb.append(output.substring(old));
                //System.out.println("dopo=\n"+sb.toString());
                output = sb.toString();
            }
        }

        //lo salvo con li nuovo nome
        try{
            FileOutputStream fos = new FileOutputStream(new File(workingDir+"publish"+Constants.SEP+nomeFile));
            PrintStream ps = new PrintStream( fos );
            ps.println (output);
            ps.close();
        } catch(Exception e){
            System.out.println("ERRORE ="+e.getMessage());
            System.err.println(e.getMessage());
        }
        }
    }



    // esegue il mapping in automatico di tutti i files inclusi
    // metodo scritto troppo velocemente, da ricontrollare
    // CAMBIA IL NOME AL FILE
    public void runMappingIncludedFiles(Vector includedFiles){
        String nomeFile="";
        StringBuffer testo;
        String output="";
        File file;
        BufferedReader br;

        for(int i=0; i< includedFiles.size() ;i++){
        testo= new StringBuffer();
        nomeFile = (String)includedFiles.get(i);


        // per ogni file, lo leggo e creo il file mapping_XXX.inf
        file = new File(gamesDir+Constants.SEP+nomeFile);
        if (!(file.exists())){
            //inserire l'errore nella JTextAreaOutput
            System.out.println(java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE4"));
            return;
        }
        try{
            br = new BufferedReader(new FileReader(file));
            while ((riga = br.readLine())!=null){
                testo.append(riga).append("\n");
            }
            br.close();
        } catch(Exception e){
            System.out.println("ERRORE ="+ e.getMessage());
            System.err.println(e.getMessage());
        }
        output= testo.toString();

        int pos=0;
        int old=0;
        boolean entrato = false;
        // da MAPPA recupero TUTTI i caratteri da sostituire e li sostituisco
        for (int count=0; count < mappa.size(); count++){
            pos=0;
            old=0;
            entrato=false;
            //sb = new StringBuffer();
            sb.setLength(0);
            String riga = (String)mappa.get(count);
            String index = riga.substring(0,riga.indexOf(','));
            String value = riga.substring(riga.indexOf(',')+1);

            while ((pos = output.indexOf(index, pos)) >= 0){
                sb.append(output.substring(old,pos));
                sb.append(value);
                pos ++;
                old = pos;
                entrato = true;
            }
            if (entrato) {
                sb.append(output.substring(old));
                output = sb.toString();
            }
            //else System.out.println("Nessuna ricorrenza");
        }
        //System.out.println(output);

        //lo salvo con li nuovo nome
        try{
            FileOutputStream fos = new FileOutputStream(new File(gamesDir+Constants.SEP+"mapping"+Constants.SEP+nomeFile));
            PrintStream ps = new PrintStream( fos );
            ps.println (output);
            ps.close();
        } catch(Exception e){
            System.out.println("ERRORE ="+e.getMessage());
            System.err.println(e.getMessage());
        }

        jTextAreaOutput.append("Automatic mapping : "+gamesDir+Constants.SEP+"mapping"+Constants.SEP+nomeFile+"\n");
        }
    }


    // recupero un Rettangolo della grandezza del viewport con le coordinate
    // Quando lo user clicca su una foglia dell'albero degli oggetti,
    // viene anche cambiata la viewport del JTextPane
    Rectangle getRectForLine (int line){
        try{
        Point p = new Point();
        p.x = 0;

        // qui faccio una patch a seconda del tipo di VM installata
//        String ver = System.getProperty("java.specification.version");
//        if (null!= ver && ver.equals("1.4")){
//            // 1.4
//            p.y = line * (getCurrentJIFTextPane().getFontMetrics(getCurrentJIFTextPane().getFont()).getHeight()+1);
//        }
//        else{
//            // 1.3
//            p.y = line * getCurrentJIFTextPane().getFontMetrics(getCurrentJIFTextPane().getFont()).getHeight();
//        }
        //System.out.println("versione = "+ver +"  getFontMetrics="+getCurrentJIFTextPane().getFontMetrics(getCurrentJIFTextPane().getFont()).getHeight());
        p.y = line * getCurrentJIFTextPane().getFontMetrics(getCurrentJIFTextPane().getFont()).getHeight();
        Dimension D = ((JScrollPane)jTabbedPane1.getSelectedComponent()).getViewport().getSize();
        int H = D.height;
        int W = D.width;
        Rectangle R = new Rectangle (p.x, p.y, W, H);
        return R;
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }



    public void showJWindowSymbol(){
        try{
            int pointx = (int)getCurrentJIFTextPane().modelToView(getCurrentJIFTextPane().getCaretPosition()).getX();
            int pointy = (int)getCurrentJIFTextPane().modelToView(getCurrentJIFTextPane().getCaretPosition()).getY();
            JWindowSymbols.setLocation((int)getCurrentJIFTextPane().getLocationOnScreen().getX()+pointx, (int)getCurrentJIFTextPane().getLocationOnScreen().getY() +pointy+15);
            JWindowSymbols.setSize(230,200);
            JWindowSymbols.requestFocus();
            JWindowSymbols.show();
        } catch(BadLocationException e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
    }


    public void saveAs(){
        // recupero il nuovo nome del file e lo salvo....
        // String result = JOptionPane.showInputDialog(this , java.util.ResourceBundle.getBundle("JIF").getString("MSG_NEWFILE1")+gamesDir, java.util.ResourceBundle.getBundle("JIF").getString("MSG_NEWFILE2"), JOptionPane.OK_CANCEL_OPTION);

        JFileChooser chooser;
        if (lastDir!=null && !lastDir.equals("")){
              chooser  = new JFileChooser(lastDir);
              chooser.setDialogTitle(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_SAVEAS"));
              chooser.setApproveButtonText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_SAVE"));
        }
        else {
             chooser = new JFileChooser(this.gamesDir);
              chooser.setDialogTitle(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_SAVEAS"));
              chooser.setApproveButtonText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_SAVE"));
        }

        // Selezione Multipla
        chooser.setMultiSelectionEnabled(false);

        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.CANCEL_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        String result = file.getAbsolutePath();
        lastDir = result.substring(0,result.lastIndexOf(Constants.SEP));


        // se il file non ha estensione: gliela inserisco io INF
        if (result.lastIndexOf(".")==-1){
            result = result+".inf";
        }

        // se l'utente ha inserito una cosa del tipo...
        // nome.cognome -> il nome viene convertito in nome.cognome.inf
        // controllo che l'utente non abbia scritto nome.txt, nome.res ecc
        if (((result.lastIndexOf(".")!=-1)&&(result.lastIndexOf(".inf"))==-1)
            &&
            (
                !(result.endsWith(".res"))
                &&
                !(result.endsWith(".txt"))
                &&
                !(result.endsWith(".h"))
                &&
                !(result.endsWith(".doc"))
            )){
                result = result+".inf";
            }
        

        // controllo che non esista già un file con quel nome
        //File file = new File(gamesDir+Constants.SEP+result);

        if (file.exists()){
            int overwrite = JOptionPane.showConfirmDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("ERR_NAMEFILE4"), java.util.ResourceBundle.getBundle("JIF").getString("ERR_NAMEFILE2") , JOptionPane.ERROR_MESSAGE);
            if (overwrite == 1) return;
        }

        //salvataggio
        //jTabbedPane1.setTitleAt( jTabbedPane1.getSelectedIndex(), gamesDir+Constants.SEP+result);
        //PROBLEMA
        jTabbedPane1.setTitleAt( jTabbedPane1.getSelectedIndex(), result);
        saveFile();

        // Refresh Tree
        refreshTree();
    }


    // funzione che gestisce l'inserimento di files in un progetto
    // supporta la seleziona multipla
    public void addFilesToProject(){
    JFileChooser chooser = new JFileChooser(gamesDir);
        JifFileFilter infFilter = new JifFileFilter("inf", java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF7"));
        infFilter.addExtension("h");
        infFilter.addExtension("res");
        infFilter.addExtension("txt");
        chooser.setFileFilter(infFilter);
        // Selezione Multipla
        chooser.setMultiSelectionEnabled(true);

        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.CANCEL_OPTION) {
            //imposto a null il nome del file e return
            fileInf = null;
            return;
        }
        File[] files = chooser.getSelectedFiles();
        File file;

        for (int i=0 ; i<files.length; i++){

        //file = chooser.getSelectedFile();
        file = files[i];

        //apro il file e lo aggiungo alla lista se
        // il checkbox è attivo
        if (jCheckBoxProjectOpenAllFiles.isSelected()){
            openFile(file.getAbsolutePath());
        }

        projectFiles.add(new FileProject(file.getAbsolutePath()));
        jListProject.removeAll();
        // ordino il vettore
        Collections.sort(projectFiles,new Comparator(){
                 public int compare(Object a, Object b) {
                    String id1 = ((FileProject)a).toString();
                    String id2 = ((FileProject)b).toString();
                     return (id1).compareToIgnoreCase(id2) ;
                 }
        });
        jListProject.setListData(projectFiles);
        }

        // Update and save the project
        saveProject();
    }


    // apre il file config.jif e salva i vari jcheckbox
    public void saveJifConfiguration(){
        try{
                File file = new File(this.workingDir+"config"+Constants.SEP+"config.jif");
                file.createNewFile();
                PrintStream ps;
                FileOutputStream fos = new FileOutputStream(file);
                ps = new PrintStream( fos );
                ps.println("# Jif CONFIGURATION");
                ps.println("# DO NOT EDIT");
                ps.println("WRAPLINES="+ jCheckBoxWrapLines.isSelected());
                ps.println("SYNTAX="+ jCheckBoxSyntax.isSelected());
                ps.println("HELPEDCODE="+ jCheckBoxHelpedCode.isSelected());
                ps.println("BACKUP="+ jCheckBoxBackup.isSelected());
                ps.println("MAPPINGEDITING="+ jCheckBoxMappingLive.isSelected());
                ps.println("MAPPINGTEMP="+ jCheckBoxMapping.isSelected());
                ps.println("MAPPINGTEMPH="+ jCheckBoxMappingHFile.isSelected());
                ps.println("NUMBERLINES="+ jCheckBoxNumberLines.isSelected());
                ps.println("SCANPROJECTFILESFORCLASSES="+ jCheckBoxScanProjectFiles.isSelected());
                ps.println("PROJECTOPENALLFILE="+ jCheckBoxProjectOpenAllFiles.isSelected());
                ps.println("PROJECTCLOSEALLFILE="+ jCheckBoxProjectCloseAll.isSelected());
                ps.println("ADVENTINLIBPATH="+jCheckBoxAdventInLib.isSelected());
                ps.println("OPENLASTFILE="+ jCheckBoxOpenLastFile.isSelected());
                ps.println("CREATENEWFILE="+ jCheckBoxCreateNewFile.isSelected());
                ps.println("MAXRECENTFILES="+ this.jTextFieldMaxRecentFiles.getText());
                ps.println("AUTOMATICBRACKETSCHECK="+ jCheckBoxAutomaticCheckBrackets.isSelected());
                ps.println("[colorKeyword]="+colorKeyword.getRed()+","+colorKeyword.getGreen()+","+colorKeyword.getBlue());
                ps.println("[colorAttribute]="+colorAttribute.getRed()+","+colorAttribute.getGreen()+","+colorAttribute.getBlue());
                ps.println("[colorProperty]="+colorProperty.getRed()+","+colorProperty.getGreen()+","+colorProperty.getBlue());
                ps.println("[colorRoutine]="+colorRoutine.getRed()+","+colorRoutine.getGreen()+","+colorRoutine.getBlue());
                ps.println("[colorVerb]="+colorVerb.getRed()+","+colorVerb.getGreen()+","+colorVerb.getBlue());
                ps.println("[colorNormal]="+colorNormal.getRed()+","+colorNormal.getGreen()+","+colorNormal.getBlue());
                ps.println("[colorComment]="+colorComment.getRed()+","+colorComment.getGreen()+","+colorComment.getBlue());
                ps.println("[colorBackground]="+colorBackground.getRed()+","+colorBackground.getGreen()+","+colorBackground.getBlue());
                ps.println("[defaultFont]="+ defaultFont.getName()+","+defaultFont.getStyle()+","+defaultFont.getSize());
                ps.println("CHECKBOXMAKERESOURCE="+ jCheckBoxMakeResource.isSelected());
                ps.close();

                // Message to the jTextAreaOutput about saving configuration
                this.jTextAreaOutput.setText(java.util.ResourceBundle.getBundle("JIF").getString("JTEXTOUTPUT_CONFIG_SAVED"));

        } catch(Exception e){
            JOptionPane.showMessageDialog(jDialogConfigFiles, "Error", e.getMessage() , JOptionPane.INFORMATION_MESSAGE);
        }
    }




    // apre il file config.jif e setta i vari jcheckbox e i colori
    public void loadJifConfiguration(File file){
        try{
                // se il file non è presente, non faccio nulla
                //File file = new File(this.workingDir+"config"+Constants.SEP+"config.jif");
                if (!(file.exists())) return;   // esco se non trovo il file

                BufferedReader br = new BufferedReader(new FileReader(file));

                // Metto tutti i valori di default
                defaultOptions();

                while ((riga = br.readLine())!=null){
                    // salto le righe commentate o vuote
                    if (!(riga.startsWith(Constants.TOKENCOMMENT))&&!(riga.equals(""))){
						// wrap line on
                        if (riga.indexOf("WRAPLINES=")!=-1){
                           this.jCheckBoxWrapLines.setSelected(
                                riga.substring(riga.indexOf("WRAPLINES=")+10).equals("true")?true:false);
                        }
                        // syntax highlight
                        if (riga.indexOf("SYNTAX=")!=-1){
                           this.jCheckBoxSyntax.setSelected(
                                riga.substring(riga.indexOf("SYNTAX=")+7).equals("true")?true:false);
                        }
                        // HELPED CODE
                        if (riga.indexOf("HELPEDCODE=")!=-1){
                           this.jCheckBoxHelpedCode.setSelected(
                                riga.substring(riga.indexOf("HELPEDCODE=")+11).equals("true")?true:false);
                        }
                        // BACKUP
                        if (riga.indexOf("BACKUP=")!=-1){
                           this.jCheckBoxBackup.setSelected(
                                riga.substring(riga.indexOf("BACKUP=")+7).equals("true")?true:false);
                        }
                        // MAPPINGEDITING
                        if (riga.indexOf("MAPPINGEDITING=")!=-1){
                           this.jCheckBoxMappingLive.setSelected(
                                riga.substring(riga.indexOf("MAPPINGEDITING=")+15).equals("true")?true:false);
                        }
                        // MAPPINGTEMP
                        if (riga.indexOf("MAPPINGTEMP=")!=-1){
                           this.jCheckBoxMapping.setSelected(
                                riga.substring(riga.indexOf("MAPPINGTEMP=")+12).equals("true")?true:false);
                        }
                        // MAPPINGTEMPH
                        if (riga.indexOf("MAPPINGTEMPH=")!=-1){
                           this.jCheckBoxMappingHFile.setSelected(
                                riga.substring(riga.indexOf("MAPPINGTEMPH=")+13).equals("true")?true:false);
                        }
                        // Show line number
                        if (riga.indexOf("NUMBERLINES=")!=-1){
                           this.jCheckBoxNumberLines.setSelected(
                                riga.substring(riga.indexOf("NUMBERLINES=")+12).equals("true")?true:false);
                        }
                        // Scan project files for class definitions
                        if (riga.indexOf("SCANPROJECTFILESFORCLASSES=")!=-1){
                           jCheckBoxScanProjectFiles.setSelected(
                                riga.substring(riga.indexOf("SCANPROJECTFILESFORCLASSES=")+27).equals("true")?true:false);
                        }
                        // PROJECT: open all files
                        if (riga.indexOf("PROJECTOPENALLFILE=")!=-1){
                           this.jCheckBoxProjectOpenAllFiles.setSelected(
                                riga.substring(riga.indexOf("PROJECTOPENALLFILE=")+19).equals("true")?true:false);
                        }
                        // PROJECT: close all files
                        if (riga.indexOf("PROJECTCLOSEALLFILE=")!=-1){
                           this.jCheckBoxProjectCloseAll.setSelected(
                                riga.substring(riga.indexOf("PROJECTCLOSEALLFILE=")+20).equals("true")?true:false);
                        }
                        
                        //Adventure path in library path
                        if (riga.indexOf("ADVENTINLIBPATH=")!=-1){
                             this.jCheckBoxAdventInLib.setSelected(
                            riga.substring(riga.indexOf("ADVENTINLIBPATH=")+16).equals("true")?true:false);
                        }
                        // OPEN LAST FILE
                        if (riga.indexOf("OPENLASTFILE=")!=-1){
                           this.jCheckBoxOpenLastFile.setSelected(
                                riga.substring(riga.indexOf("OPENLASTFILE=")+13).equals("true")?true:false);
                        }
                        // CREATE A NEW FILE
                        if (riga.indexOf("CREATENEWFILE=")!=-1){
                            jCheckBoxCreateNewFile.setSelected(
                            riga.substring(riga.indexOf("CREATENEWFILE=")+14).equals("true")?true:false);
                        }
                        // MAX NUMBER OF RECENT FILES
                        if (riga.indexOf("MAXRECENTFILES=")!=-1){
                            String num = riga.substring(riga.indexOf("MAXRECENTFILES=")+15);
                            this.jTextFieldMaxRecentFiles.setText(num);
                            try{
                                maxRecentFiles = Integer.parseInt(num);
                            } catch (Exception e){
                                // valore di default
                                maxRecentFiles = 10;
                                this.jTextFieldMaxRecentFiles.setText("10");
                            }
                        }

                        // AUTOMATIC BRACKETS CHECK
                        if (riga.indexOf("AUTOMATICBRACKETSCHECK=")!=-1){
                            jCheckBoxAutomaticCheckBrackets.setSelected(
                            riga.substring(riga.indexOf("AUTOMATICBRACKETSCHECK=")+23).equals("true")?true:false);
                        }

                        if (riga.indexOf("CHECKBOXMAKERESOURCE=")!=-1){
                            jCheckBoxMakeResource.setSelected(
                            riga.substring(riga.indexOf("CHECKBOXMAKERESOURCE=")+21).equals("true")?true:false);
                        }

                        // GESTIONE COLORI
                        int col1,col2,col3;
                        if (riga.indexOf("[colorKeyword]=")!=-1){
                            st = new StringTokenizer(riga,"=,");
                            st.nextToken();
                            col1 = Integer.parseInt(st.nextToken());
                            col2 = Integer.parseInt(st.nextToken());
                            col3 = Integer.parseInt(st.nextToken());
                            this.colorKeyword = new Color(col1,col2,col3);
                        }
                        if (riga.indexOf("[colorAttribute]=")!=-1){
                            st = new StringTokenizer(riga,"=,");
                            st.nextToken();
                            col1 = Integer.parseInt(st.nextToken());
                            col2 = Integer.parseInt(st.nextToken());
                            col3 = Integer.parseInt(st.nextToken());
                            this.colorAttribute = new Color(col1,col2,col3);
                        }
                        if (riga.indexOf("[colorProperty]=")!=-1){
                            st = new StringTokenizer(riga,"=,");
                            st.nextToken();
                            col1 = Integer.parseInt(st.nextToken());
                            col2 = Integer.parseInt(st.nextToken());
                            col3 = Integer.parseInt(st.nextToken());
                            this.colorProperty = new Color(col1,col2,col3);
                        }
                        if (riga.indexOf("[colorRoutine]=")!=-1){
                            st = new StringTokenizer(riga,"=,");
                            st.nextToken();
                            col1 = Integer.parseInt(st.nextToken());
                            col2 = Integer.parseInt(st.nextToken());
                            col3 = Integer.parseInt(st.nextToken());
                            this.colorRoutine = new Color(col1,col2,col3);
                        }
                        if (riga.indexOf("[colorVerb]=")!=-1){
                            st = new StringTokenizer(riga,"=,");
                            st.nextToken();
                            col1 = Integer.parseInt(st.nextToken());
                            col2 = Integer.parseInt(st.nextToken());
                            col3 = Integer.parseInt(st.nextToken());
                            this.colorVerb = new Color(col1,col2,col3);
                        }
                        if (riga.indexOf("[colorNormal]=")!=-1){
                            st = new StringTokenizer(riga,"=,");
                            st.nextToken();
                            col1 = Integer.parseInt(st.nextToken());
                            col2 = Integer.parseInt(st.nextToken());
                            col3 = Integer.parseInt(st.nextToken());
                            this.colorNormal = new Color(col1,col2,col3);
                        }
                        //colorBackground
                        if (riga.indexOf("[colorBackground]=")!=-1){
                            st = new StringTokenizer(riga,"=,");
                            st.nextToken();
                            col1 = Integer.parseInt(st.nextToken());
                            col2 = Integer.parseInt(st.nextToken());
                            col3 = Integer.parseInt(st.nextToken());
                            this.colorBackground = new Color(col1,col2,col3);
                        }
                        //colorComment
                        if (riga.indexOf("[colorComment]=")!=-1){
                            st = new StringTokenizer(riga,"=,");
                            st.nextToken();
                            col1 = Integer.parseInt(st.nextToken());
                            col2 = Integer.parseInt(st.nextToken());
                            col3 = Integer.parseInt(st.nextToken());
                            this.colorComment = new Color(col1,col2,col3);
                        }
                        if (riga.indexOf("[defaultFont]=")!=-1){
                            st = new StringTokenizer(riga,"=,");
                            st.nextToken();
                            defaultFont = new Font(st.nextToken(),Integer.parseInt(st.nextToken()),Integer.parseInt(st.nextToken()));
                        }
                    }
                }
                br.close();
        } catch(Exception e){
            JOptionPane.showMessageDialog(jDialogConfigFiles, "Error", e.getMessage() , JOptionPane.INFORMATION_MESSAGE);
        }
    }


    // Crea un nuovo progetto
    public void newProject(){
        // imposto il nuovo progetto nella dir projects
        try{
        String result = JOptionPane.showInputDialog(this ,
        java.util.ResourceBundle.getBundle("JIF").getString("MSG_NAME"),
        java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_INSERT_NAME_FOR_THE_PROJECT") ,
        JOptionPane.OK_CANCEL_OPTION);
        // se l'utente ha cliccato su annulla esco
        if (result== null){
            return;
        }

        // se il progetto esiste già viene segnalato un warning
        File file = new File(workingDir+"projects"+Constants.SEP+result+".jpf");
        if (file.exists()){
            int res =  JOptionPane.showConfirmDialog(this,
                     java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_PROJECT_EXISTS_OVERWRITE"),
                    file.getAbsolutePath() ,
                    JOptionPane.OK_CANCEL_OPTION);
            //System.out.println("res="+res);
            if (res==2){
                return;
            }
        }

        file.delete();
        file.createNewFile();

        //System.out.println("PROGETTO NUOVO="+workingDir+"projects"+Constants.SEP+result+".jpf");
        currentProject = file.getAbsolutePath();

        updateProjectTitle("Project: "+
        currentProject.substring(
        currentProject.lastIndexOf(Constants.SEP)+1
        ,
        currentProject.length())
        );

        jScrollPaneProject.setEnabled(true);
        } catch (Exception e){
            System.err.println(e.getMessage());
        }
    }


    public void updateProjectTitle(String title){
        TitledBorder tb = new TitledBorder(title);
        tb.setTitleFont(new Font("Dialog",Font.PLAIN,10));
        jScrollPaneProject.setBorder(tb);
    }


    // apre un progetto, e carica la configurazione memorizzata
    public void openProject(){
        // creo un nuovo vettore per le classi
        projectClass = new Vector();

        JFileChooser chooser = new JFileChooser(workingDir+Constants.SEP+"projects");
        JifFileFilter infFilter = new JifFileFilter("jpf", "Jif Project File");
        chooser.setFileFilter(infFilter);

        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.CANCEL_OPTION) {
            return;
        }
        File file = new File(chooser.getSelectedFile().getAbsolutePath());
        try{
                BufferedReader br = new BufferedReader(new FileReader(file));
                projectFiles = new Vector();
                String idSwitch;
                String valueSwitch;
                String auxFile;
                int numeroSwitch=jPanelSwitch2.getComponentCount();
                Checkbox ch;
                Vector fileToOpen=new Vector(15);

                while ((riga = br.readLine())!=null){
                    if (!(riga.startsWith(Constants.TOKENCOMMENT))&&!(riga.equals(""))){
                        if (riga.indexOf("[FILE]=")!=-1){
                            projectFiles.add(
                            new FileProject(riga.substring(riga.indexOf("[FILE]=")+7))
                            );

                            // qui lancio un metodo che mi apre il file e mi cerca le classi
                            // se ho impostato il flag jCheckBoxScanProjectFiles a true
                            if (jCheckBoxScanProjectFiles.isSelected()){
                                Utils.seekClass(projectClass, new File(riga.substring(riga.indexOf("[FILE]=")+7)));
                            }
                            // provo ad aprire il file, se il checkbox relativo è impostato
                            // a TRUE

                            	fileToOpen.add(riga.substring(riga.indexOf("[FILE]=")+7));

                 /*           if (jCheckBoxProjectOpenAllFiles.isSelected()){
                            	auxFile=riga;
                                openFile(riga.substring(riga.indexOf("[FILE]=")+7));
                                //in OpenFile riga is seted to null
                                riga=auxFile;
                            }
                            */
                        }
                        if (riga.indexOf("[MAINFILE]=")!=-1){
                            mainFile = riga.substring(riga.indexOf("[MAINFILE]=")+11);
                            jLabelMainFile.setText("Main: " + mainFile.substring(mainFile.lastIndexOf(Constants.SEP)+1,mainFile.length()));
                        }


                        // Load project switches
                        if (riga.indexOf("[SWITCH]=")!=-1){
                            st = new StringTokenizer(riga.substring(riga.indexOf("[SWITCH]=")+9) , ",");
                            idSwitch = st.nextToken();
                            valueSwitch = st.nextToken();
                            //System.out.println(idSwitch+"="+valueSwitch);
                            for(int count=0; count < numeroSwitch; count++){
                                ch = (Checkbox) jPanelSwitch2.getComponent(count);
                                if (ch.getLabel().equals(idSwitch)){
                                    ch.setState(valueSwitch.toLowerCase().equals("on")? true: false);
                                }
                            }



                        }



                    }
                }

                br.close();

                // ordino il vettore
                Collections.sort(projectFiles,new Comparator(){
                         public int compare(Object a, Object b) {
                            String id1 = ((FileProject)a).toString();
                            String id2 = ((FileProject)b).toString();
                             return (id1).compareToIgnoreCase(id2) ;
                         }
                    });

                jListProject.setListData(projectFiles);

                // Load JIF configuration
                loadJifConfiguration(file);
                //open files if necesarry
                if (jCheckBoxProjectOpenAllFiles.isSelected()){
                	for(int i=0;i<fileToOpen.size();i++){
                	auxFile=(String)fileToOpen.elementAt(i);

                	// don't open automatically *.h files
                	if (!auxFile.endsWith(".h")){
						openFile(auxFile);
					}
                    //in OpenFile riga is seted to null
                	}
                	jTabbedPane1.setSelectedIndex(0);
                }

        } catch(IOException e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }

        currentProject = chooser.getSelectedFile().getAbsolutePath();

        updateProjectTitle("Project: "+
        currentProject.substring(
        currentProject.lastIndexOf(Constants.SEP)+1
        ,
        currentProject.length())
        );

        jScrollPaneProject.setEnabled(true);
    }

    // chiude un progetto. Inserire un flag per chiudere tutti i files
    // relativi ad un progetto quando si chiude il progetto stesso
    public void closeProject(){
        currentProject = workingDir+Constants.SEP+"projects"+Constants.SEP+"default.jpf";

        updateProjectTitle("Project: ");

        projectFiles.removeAllElements();
        // ordino il vettore
        Collections.sort(projectFiles,new Comparator(){
            public int compare(Object a, Object b) {
                String id1 = ((FileProject)a).toString();
                String id2 = ((FileProject)b).toString();
                return (id1).compareToIgnoreCase(id2) ;
            }
        });

        jListProject.setListData(projectFiles);
        jScrollPaneProject.setEnabled(false);

        // se il checkbox close_all_files è attivo, chiudo tutti i files
        if (this.jCheckBoxProjectCloseAll.isSelected()){
           closeAllFiles();
        }

        // carico la configurazione di JIF da file
        loadJifConfiguration(new File(this.workingDir+"config"+Constants.SEP+"config.jif"));

        // When closing a project the "projectClass" vector has to be cleared
        projectClass = null;
        // also the mainFile of the project and the Main File label
        mainFile = null;
        jLabelMainFile.setText("Main:");
    }


    public void saveProject(){
//        if (
//        JOptionPane.showConfirmDialog(this,
//        currentProject ,
//        java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_SAVE"),
//        JOptionPane.OK_CANCEL_OPTION)
//        ==0
//        ){
            try{
                File file = new File(currentProject);

                PrintStream ps;
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                ps = new PrintStream( fos );
                ps.println("# ***************** #");
                ps.println("# *  Jif Project  * #");
                ps.println("# ***************** #");
                ps.println("# DO NOT EDIT");
                ps.println("");
                ps.println("# Files for the project "+currentProject);
                ps.println("");
                for (int i=0; i<projectFiles.size();i++)  {
                    ps.println ("[FILE]=" + ((FileProject)projectFiles.elementAt(i)).path);
                }
                ps.println("[MAINFILE]="+mainFile);
                ps.println("");
                ps.println("");
                // The project configuration
                ps.println("# ********************* #");
                ps.println("# PROJECT CONFIGURATION #");
                ps.println("# ********************* #");
                ps.println("WRAPLINES="+ jCheckBoxWrapLines.isSelected());
                ps.println("SYNTAX="+ jCheckBoxSyntax.isSelected());
                ps.println("HELPEDCODE="+ jCheckBoxHelpedCode.isSelected());
                ps.println("BACKUP="+ jCheckBoxBackup.isSelected());
                ps.println("MAPPINGEDITING="+ jCheckBoxMappingLive.isSelected());
                ps.println("MAPPINGTEMP="+ jCheckBoxMapping.isSelected());
                ps.println("MAPPINGTEMPH="+ jCheckBoxMappingHFile.isSelected());
                ps.println("NUMBERLINES="+ jCheckBoxNumberLines.isSelected());
                ps.println("SCANPROJECTFILESFORCLASSES="+ jCheckBoxScanProjectFiles.isSelected());
                ps.println("PROJECTOPENALLFILE="+ jCheckBoxProjectOpenAllFiles.isSelected());
                ps.println("PROJECTCLOSEALLFILE="+ jCheckBoxProjectCloseAll.isSelected());
                ps.println("OPENLASTFILE="+ jCheckBoxOpenLastFile.isSelected());
                ps.println("CREATENEWFILE="+ jCheckBoxCreateNewFile.isSelected());
                ps.println("MAXRECENTFILES="+ this.jTextFieldMaxRecentFiles.getText());
                ps.println("AUTOMATICBRACKETSCHECK="+ jCheckBoxAutomaticCheckBrackets.isSelected());
                ps.println("[colorKeyword]="+colorKeyword.getRed()+","+colorKeyword.getGreen()+","+colorKeyword.getBlue());
                ps.println("[colorAttribute]="+colorAttribute.getRed()+","+colorAttribute.getGreen()+","+colorAttribute.getBlue());
                ps.println("[colorProperty]="+colorProperty.getRed()+","+colorProperty.getGreen()+","+colorProperty.getBlue());
                ps.println("[colorRoutine]="+colorRoutine.getRed()+","+colorRoutine.getGreen()+","+colorRoutine.getBlue());
                ps.println("[colorVerb]="+colorVerb.getRed()+","+colorVerb.getGreen()+","+colorVerb.getBlue());
                ps.println("[colorNormal]="+colorNormal.getRed()+","+colorNormal.getGreen()+","+colorNormal.getBlue());
                ps.println("[colorComment]="+colorComment.getRed()+","+colorComment.getGreen()+","+colorComment.getBlue());
                ps.println("[colorBackground]="+colorBackground.getRed()+","+colorBackground.getGreen()+","+colorBackground.getBlue());
                ps.println("[defaultFont]="+ defaultFont.getName()+","+defaultFont.getStyle()+","+defaultFont.getSize());
                ps.println("CHECKBOXMAKERESOURCE="+ jCheckBoxMakeResource.isSelected());

                ps.println("");
                ps.println("");
                // The project Switches
                StringBuffer make = getSwitchesForSavingProject();
                ps.println (make.toString());

                ps.close();

            }catch(Exception e ){
                System.err.println(e.getMessage());
            }

            JOptionPane.showMessageDialog(
            jDialogConfigFiles,
            currentProject +" " +java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE4"),
            java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE2") ,
            JOptionPane.INFORMATION_MESSAGE);
      //  }
    }


    public void removeFileFromProject(){
        FileProject fp = (FileProject)jListProject.getSelectedValue();
        if (null==fp) return;
        if (
        JOptionPane.showConfirmDialog(this,
        java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_DELETE_FILE_FROM_PROJECT") ,
        "File : "+fp.name , JOptionPane.OK_CANCEL_OPTION)
        ==0
        ){
            projectFiles.remove(fp);
            jListProject.removeAll();
            // ordino il vettore
            Collections.sort(projectFiles,new Comparator(){
                public int compare(Object a, Object b) {
                    String id1 = ((FileProject)a).toString();
                    String id2 = ((FileProject)b).toString();
                     return (id1).compareToIgnoreCase(id2) ;
                }
            });

            jListProject.setListData(projectFiles);

            // Update and save the project
            saveProject();
        }
    }


    public void closeAllFiles(){
        int numberOfComponents = jTabbedPane1.getTabCount();
        for (int count=0; count < numberOfComponents; count++){
            closeFile();
        }
        refreshTree();
        System.gc();
    }

    public void savePath(){
        //salvo la nuova configurazione
        StringBuffer makeConfig = new StringBuffer();
        makeConfig.append("######################################################"+"\n");
        makeConfig.append("# Jif Configuration"+"\n");
        makeConfig.append("######################################################"+"\n");
        makeConfig.append("\n");
        makeConfig.append("libPath="+jTextFieldPathLib.getText()+"\n");
        makeConfig.append("libPathSecondary1="+this.libPathSecondary1+"\n");
        makeConfig.append("libPathSecondary2="+this.libPathSecondary2+"\n");
        makeConfig.append("libPathSecondary3="+this.libPathSecondary3+"\n");
        makeConfig.append("gamesDir="+jTextFieldPathGames.getText()+"\n");
        makeConfig.append("interpreter="+jTextFieldPathInterpreter.getText()+"\n");
        makeConfig.append("glulx="+jTextFieldPathGlulx.getText()+"\n");
        makeConfig.append("compiler="+jTextFieldPathCompiler.getText()+"\n");
        makeConfig.append("defaultBrowser="+ jTextFieldPathBrowser.getText()+"\n");
        makeConfig.append("BRESLOCATION="+ jTextFieldBres.getText()+"\n");
        makeConfig.append("BLCLOCATION="+ jTextFieldBlc.getText()+"\n");

        File file = new File(workingDir+"config"+Constants.SEP+"config.ini");
        //salvo il file default
        PrintStream ps;
        try{
            FileOutputStream fos = new FileOutputStream(file);
            ps = new PrintStream( fos );
            ps.println (makeConfig.toString());
            ps.close();
        } catch(IOException e ){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
        loadConfig();
    }



    // prende il file corrente e lo analizza, il risultato viene inserito
    // nella TextAreaInfo
    public void setInfoAT(){
        //String testo = getCurrentJIFTextPane().getText(); unused
        StringBuffer out = new StringBuffer();
        File file = new File(getCurrentFilename());
        out.setLength(0);
        out.append(file.getAbsolutePath()+"\n\n");
        out.append("File length    :"+file.length() +" (byte)\n");
        out.append("Rows           :"+ getCurrentDoc().getDefaultRootElement().getElementCount()+"\n");
        out.append("Globals        :"+category1.getChildCount()+"\n");
        out.append("Constants      :"+category2.getChildCount()+"\n");
        out.append("Objects        :"+category4.getChildCount()+"\n");
        out.append("Classes        :"+category7.getChildCount()+"\n");
        out.append("Functions      :"+category5.getChildCount()+"\n");
        out.append("Sub-Functions  :"+category6.getChildCount()+"\n");
        // fine dell'analisi
        this.jTextAreaInfo.setText(out.toString());
    }


    // Lancia l'interprete senza passargli il file AT (.inf)
    // This method has to be splitted in 2
    public void runInterpreter() {
        if (jCheckBoxInformMode.isSelected()){
            runInformInterpreter();
        }
        else runGlulxInterpreter();
    }

   public void runGlulxInterpreter(){
        // controllo che esista l'interprete con il path  inserito nella config.ini
        // se non esiste visualizzo un messaggio di warning
   		String auxGlux[]=new String[1];
        File test = new File(glulx);
        if (!test.exists()){
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER1")+interpreter+java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER2"), java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER3") , JOptionPane.ERROR_MESSAGE);
            return;
        }
        try{
        	auxGlux[0]=new String(glulx);
            Runtime rt = Runtime.getRuntime();
            rt.exec(auxGlux); //Process proc =  unused
        } catch(IOException e){
            System.err.println(e.getMessage());
        }
    }



    public void runInformInterpreter(){
        // controllo che esista l'interprete con il path  inserito nella config.ini
        // se non esiste visualizzo un messaggio di warning
    	String auxInter[]=new String[1];
    	//File test = new File(interpreter); unused
//        if (!test.exists()){
//            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER1")+interpreter+java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER2"), java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER3") , JOptionPane.ERROR_MESSAGE);
//            return;
//        }
        try{
        	auxInter[0]=new String(interpreter);
            Runtime rt = Runtime.getRuntime();
            rt.exec(auxInter); //Process proc =  unused
        } catch(IOException e){
            System.err.println(e.getMessage());
        }
    }


    // carico il menu Tutorial con i files presenti sul sistema prendo:
    // 1) tutti i file TXT
    // 2) tutti i file index.html
    // 3) non prendo le directory
    public void loadTutorial(){
        String tutorialDir = this.workingDir+"tutorial";
        try {
            // azzero il menu
            jMenuTutorial.removeAll();

            //BufferedReader br; unused
            File file = new File(tutorialDir);
            File Menues[] = file.listFiles();

            riga="";
            //String id="", name=""; unused
            JMenu menu = null;
            tutorialtarget = new Hashtable();

            // per ogni directory, aggiungo il relativo menu di livello 1 e
            // di livello 2 lo aggiungo
            int length = Menues.length;
            for (int count=0; count < length; count++){
                //aggiungo il menu
                menu = new JMenu(Menues[count].getName());
                menu.setFont(new Font("Dialog",Font.PLAIN,11));
                jMenuTutorial.add(menu);

                // per ogni argomento, aggiungo i relativi menu
                File subMenues[] = Menues[count].listFiles();
                int lung = subMenues.length;
                JMenuItem menutuorial = null;

                for (int count1=0; count1 < lung; count1++){

                    // controllo quali file utilizzare
                    if (
                    (subMenues[count1].getName().indexOf(".") == -1) ||  // file senza estensione
                    (subMenues[count1].getName().toLowerCase().equals("index.html"))  ||  // file index.html
                    (subMenues[count1].getName().toLowerCase().equals("index.htm")) ||  // file index.htm
                    (subMenues[count1].getName().toLowerCase().indexOf(".txt") != -1) // file TXT
                    )
                    {
                        menutuorial = new JMenuItem(subMenues[count1].getName());
                        menutuorial.setFont(new Font("Dialog",Font.PLAIN,11));

                        tutorialtarget.put(menutuorial,subMenues[count1].getAbsolutePath());
                        menutuorial.addActionListener(new java.awt.event.ActionListener() {
                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                try{
                                    // if the tutorial file is in HTML format, JIF will launch the
                                    // web browser
                                    if (((String)tutorialtarget.get(evt.getSource())).toLowerCase().indexOf(".htm") != -1){
                                        // launch the web browser
                                        try{
                                            // controllo l'esistenza del browser
                                            checkBrowser();
                                    		String auxBrowser[]=new String[2];
                                    		auxBrowser[0]=defaultBrowser;
                                    		auxBrowser[1]=(String)tutorialtarget.get(evt.getSource());
                                            Runtime rt = Runtime.getRuntime();
                                            rt.exec(auxBrowser); // Process proc = unused
                                        } catch(Exception e){
                                            System.out.println(e.getMessage());
                                            System.err.println(e.getMessage());
                                        }
                                    }
                                    else
                                        showFile((String)tutorialtarget.get(evt.getSource()));
                                }
                                catch (Exception e){
                                    System.out.println(e.getMessage());
                                    System.err.println(e.getMessage());
                                }
                            }
                        });
                        menu.add(menutuorial);

                       } // END IF
                    }
            }
        }
        catch (Exception e){
            System.err.println("ERROR WHILE LOADING TUTORIALS");
            System.err.println(e.getMessage());
        }
    }


    // visualizza un file con tasto OK
    // senza la syntax highlight
    public void showFile(String filename){
        File file = new File(filename);
        if (!(file.exists())){
            System.out.println(java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE1") + filename);
            return;
        }

        jEditorPaneTutorial.setEditorKit(new StyledEditorKit());
        jEditorPaneTutorial.setBackground(colorBackground);
        //jEditorPaneTutorial.setDocument(new InformDocument(this));
        jDialogTutorial.setTitle(filename);
        jDialogTutorial.setSize(700,550);
        jDialogTutorial.setLocationRelativeTo(this);
        jDialogTutorial.setVisible(true);
        try{
                BufferedReader br = new BufferedReader(new FileReader(file));
                sb.setLength(0);
                while ((riga = br.readLine())!=null){
                    sb.append(riga).append("\n");
                }

                jEditorPaneTutorial.setText(sb.toString());
                jLabelTutorial.setText("Tutorial");
                jEditorPaneTutorial.setCaretPosition(0);
                br.close();
        } catch(IOException e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
    }



    public void updateColor(){
        jLabelKeyword.setForeground(colorKeyword);
        jButtonKeyword.setBackground(colorKeyword);
        jLabelAttribute.setForeground(colorAttribute);
        jButtonAttribute.setBackground(colorAttribute);
        jLabelProperty.setForeground(colorProperty);
        jButtonProperty.setBackground(colorProperty);
        jLabelRoutine.setForeground(colorRoutine);
        jButtonRoutine.setBackground(colorRoutine);
        jLabelVerb.setForeground(colorVerb);
        jButtonVerb.setBackground(colorVerb);
        jLabelNormal.setForeground(colorNormal);
        jButtonNormal.setBackground(colorNormal);
        jLabelComment.setForeground(colorComment);
        jButtonComment.setBackground(colorComment);
        jLabelBackground.setForeground(colorBackground);
        jButtonBackground.setBackground(colorBackground);
    }


    public void updateColorEditor(){
        // aggiorno l'editor di test
        jEditorPaneColor.setFont(new Font("Monospaced", Font.PLAIN, 12));
        jEditorPaneColor.setDoubleBuffered(false);
        jEditorPaneColor.setEditorKit(new StyledEditorKit());
        jEditorPaneColor.setEditable(true);

        // imposto il colore di sfondo
        jEditorPaneColor.setBackground(colorBackground);

        jEditorPaneColor.setDocument(new InformDocument(this));
        StringBuffer sb = new StringBuffer();
        sb
        .append("! Poisoned Apple\n")
        .append("Object  apple \"Poisoned Apple\"\n")
        .append("with  description \"It's a red apple.\",\n")
        .append("before [;\n")
        .append("   Eat : \n")
        .append("   print \"This is a poisoned apple, isn't it?\"\n")
        .append("   return true;\n")
        .append("],\n")
        .append("has   light;\n")
        .append("[ Initialise;\n")
        .append("	location = Forest;\"^^^...^\";\n")
        .append("];\n");
        jEditorPaneColor.setText(sb.toString());
    }



    // If a string is on the system clipboard, this method returns it;
    // otherwise it returns null.
    public static String getClipboard() {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        try {
            if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String text = (String)t.getTransferData(DataFlavor.stringFlavor);
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
    public boolean checkDefaultLanguage(){
        // prendo il pannello 2 dello switch manager,
        // scandisco tutti i chechbox, se ne trovo uno solo che inizia con
        // +language_name ATTIVO, ritorno FALSE
        // altrimenti ritorno TRUE
        boolean ritorno = true;
        int numeroSwitch=jPanelSwitch2.getComponentCount();
        Checkbox ch;

        for(int count=0; count < numeroSwitch; count++){
            ch = (Checkbox) jPanelSwitch2.getComponent(count);
            if (ch.getLabel().startsWith("+language_name") && ch.getState()){
                ritorno = false;
            }
        }
        return ritorno;
    }


    // questa funzione toglie i doppi apici o i singoli apici
    // dai campi di PATH
    public void unquote(){
        String browser = jTextFieldPathBrowser.getText();
        String compiler = jTextFieldPathCompiler.getText();
        String games = jTextFieldPathGames.getText();
        String interpreter = jTextFieldPathInterpreter.getText();
        String lib = jTextFieldPathLib.getText();
        String libsec1 = jTextFieldPathLibSecondary1.getText();
        String libsec2 = jTextFieldPathLibSecondary2.getText();
        String libsec3 = jTextFieldPathLibSecondary3.getText();

        // tolgo i " e '
        browser = Utils.replace(browser , "\"", "");
        browser = Utils.replace(browser , "'", "");
        jTextFieldPathBrowser.setText(browser);

        compiler = Utils.replace(compiler , "\"", "");
        compiler = Utils.replace(compiler , "'", "");
        jTextFieldPathCompiler.setText(compiler);

        games = Utils.replace(games , "\"", "");
        games = Utils.replace(games , "'", "");
        jTextFieldPathGames.setText(games);

        interpreter = Utils.replace(interpreter , "\"", "");
        interpreter = Utils.replace(interpreter , "'", "");
        jTextFieldPathInterpreter.setText(interpreter);

        lib = Utils.replace(lib , "\"", "");
        lib = Utils.replace(lib , "'", "");
        jTextFieldPathLib.setText(lib);

        libsec1 = Utils.replace(libsec1 , "\"", "");
        libsec1 = Utils.replace(libsec1 , "'", "");
        jTextFieldPathLibSecondary1.setText(libsec1);

        libsec2 = Utils.replace(libsec2 , "\"", "");
        libsec2 = Utils.replace(libsec2 , "'", "");
        jTextFieldPathLibSecondary2.setText(libsec2);

        libsec3 = Utils.replace(libsec3 , "\"", "");
        libsec3 = Utils.replace(libsec3 , "'", "");
        jTextFieldPathLibSecondary3.setText(libsec3);

        
    }


    public void defaultOptions(){
        jCheckBoxOpenLastFile.setSelected(false);
        jCheckBoxCreateNewFile.setSelected(false);
        jCheckBoxMappingLive.setSelected(false);
        jCheckBoxMapping.setSelected(false);
        jCheckBoxMappingHFile.setSelected(false);
        jCheckBoxBackup.setSelected(false);
        jCheckBoxProjectOpenAllFiles.setSelected(false);
        jCheckBoxAutomaticCheckBrackets.setSelected(false);
        jCheckBoxHelpedCode.setSelected(true);
        jCheckBoxNumberLines.setSelected(true);
        jCheckBoxScanProjectFiles.setSelected(true);
        jCheckBoxSyntax.setSelected(true);
        jCheckBoxWrapLines.setSelected(false);
        jCheckBoxProjectCloseAll.setSelected(true);
    }


     // run BLC SOURCE.blc source.blb to make blb (GLULX MODE)
     public void makeBlb() {//AQUI!!

        // controllo che esista il compilatore con il path  inserito nella config.ini
        // se non esiste visualizzo un messaggio di warning
        File test = new File(jTextFieldBlc.getText());
        if (!test.exists()){
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER1")+compiler+java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER2"),java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER3") , JOptionPane.ERROR_MESSAGE);
            return;
        }

        // se l'utente ha tolto la visto della TextAreaOutput, la rendo visible..
        if (!jCheckBoxOutput.getState()){
            //jCheckBoxOutput.setState(true);
            jSplitPane3.setBottomComponent(jTabbedPane2);
            jTabbedPane2.setVisible(true);
        }
        //imposto il focus sulla tabbedWindow della compilazione
        jTabbedPane2.setSelectedComponent(jScrollPane2);

        //recupero l'attuale file name
        if (mainFile != null && !mainFile.equals("")){
            jTextAreaOutput.append("Using main file "+mainFile+" to compiling...\n");
            fileInf = mainFile;
        }
        else
        fileInf = getCurrentFilename(); //jTabbedPane1.getTitleAt( jTabbedPane1.getSelectedIndex());

        // Source file name
        String source = fileInf.substring(0,fileInf.lastIndexOf("."));
        String pathForCd=fileInf.substring(0,fileInf.lastIndexOf("\\"));
        //jTextAreaOutput.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER1"));
        String process_string[]=new String[3];

            process_string[0] = jTextFieldBlc.getText(); 
			process_string[1] =new String( source+".blc ");
			process_string[2]=new String(source +".blb");

        jTextAreaOutput.append(jTextFieldBlc.getText() + " " + source+".blc "+ source +".blb\n");

        try{
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(process_string, null, new File(pathForCd));
            String line="";
            BufferedReader br= new BufferedReader( new InputStreamReader( proc.getInputStream()));

            while ( (line = br.readLine() )!=null ){
                jTextAreaOutput.append(line+"\n");
            }
            proc.waitFor(); //unused int i = 
            jTextAreaOutput.append("\n");
            jTextAreaOutput.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER2"));
            jTextAreaOutput.append("\n");
        } catch(IOException e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
        catch(InterruptedException e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
    }

     // run BRE SOURCE to make resource (GLULX MODE) //aqui
     public void makeResources() {

        // controllo che esista il compilatore con il path  inserito nella config.ini
        // se non esiste visualizzo un messaggio di warning
        File test = new File(jTextFieldBres.getText());
        if (!test.exists()){
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER1")+" "+jTextFieldBres.getText()+" "+java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER2"),java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER3") , JOptionPane.ERROR_MESSAGE);
            return;
        }

        // se l'utente ha tolto la visto della TextAreaOutput, la rendo visible..
        if (!jCheckBoxOutput.getState()){
            //jCheckBoxOutput.setState(true);
            jSplitPane3.setBottomComponent(jTabbedPane2);
            jTabbedPane2.setVisible(true);
        }
        //imposto il focus sulla tabbedWindow della compilazione
        jTabbedPane2.setSelectedComponent(jScrollPane2);

        if (mainFile != null && !mainFile.equals("")){
            jTextAreaOutput.append("Using main file "+mainFile+" to compiling...\n");
            fileInf = mainFile;
        }
        else
        //recupero l'attuale file name
        fileInf = getCurrentFilename(); //jTabbedPane1.getTitleAt( jTabbedPane1.getSelectedIndex());

        // Source file name
        String source = fileInf.substring(0,fileInf.lastIndexOf("."));

        //jTextAreaOutput.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER1"));
        String process_string[]=new String[2];
        process_string[0] = jTextFieldBres.getText();
        process_string[1] = source;

        jTextAreaOutput.append(jTextFieldBres.getText() + " " + source+"\n");

        try{
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(process_string, null, new File(gamesDir));

            String line="";
            BufferedReader br= new BufferedReader( new InputStreamReader( proc.getInputStream()));

            while ( (line = br.readLine() )!=null ){
                jTextAreaOutput.append(line+"\n");
            }

            proc.waitFor(); //int i = unused
            jTextAreaOutput.append("\n");
            jTextAreaOutput.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER2"));
            jTextAreaOutput.append("\n");
        }
        catch(IOException e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
        catch(InterruptedException e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
    }


    // Set INFORM MODE
    // Imposta i menu abilita/disabilita i menu che servono
    public void setInformMode(){
        jMenuGlulx.setEnabled(false);
        jMenuItemBuildAllGlulx.setEnabled(false);
        jMenuItemMakeBlb.setEnabled(false);
        jMenuItemMakeResource.setEnabled(false);
        jMenuItemRunBlb.setEnabled(false);
        jMenuItemRunUlx.setEnabled(false);
        jMenuItemCompile.setEnabled(false);
    }

    // Set GLUX MODE
    // Imposta i menu abilita/disabilita i menu che servono
    public void setGlulxMode(){
        if(jTabbedPane1.getTabCount()>0){
        jMenuGlulx.setEnabled(true);
        jMenuItemBuildAllGlulx.setEnabled(true);
        jMenuItemMakeBlb.setEnabled(true);
        jMenuItemMakeResource.setEnabled(true);
        jMenuItemRunBlb.setEnabled(true);
        jMenuItemRunUlx.setEnabled(true);
        jMenuItemCompile.setEnabled(true);
        }
    }

    // Esegue il file blb
    public void runBlb() {
        String inter = glulx;

        // controllo che esista l'interprete con il path  inserito nella config.ini
        // se non esiste visualizzo un messaggio di warning
        File test = new File(inter);
        if (!test.exists()){
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER1")+" "+inter+" "+java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER2"), java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER3") , JOptionPane.ERROR_MESSAGE);
            return;
        }

        //recupero l'attuale file name
        if (mainFile != null && !mainFile.equals("")){
            jTextAreaOutput.append("Using main file "+mainFile+"...\n");
            fileInf = mainFile;
        }
        else
        	fileInf = getCurrentFilename();//jTabbedPane1.getTitleAt( jTabbedPane1.getSelectedIndex());

        clearOutput();
        jTextAreaOutput.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_RUN1"));

        try{
        Runtime rt = Runtime.getRuntime();
        String command[]=new String[2];
        command[0]=inter;
        // in base al tipo di file di uscita, scelgo l'estensione del file da passare all'interprete
        String estensione = ".blb";

        // se il mapping è abilitato, devo recuperare il nome del file giusto
        if (jCheckBoxMapping.isSelected()){
                command[1] = new String(fileInf_withmapping.substring(0,fileInf_withmapping.indexOf(".inf"))+estensione);
        }
        else {
                command[1] = new String(fileInf.substring(0,fileInf.indexOf(".inf"))+estensione);
        }

        jTextAreaOutput.append(command[0]+" "+command[1]+"\n");

        rt.exec(command); //Process proc = unused
        //String line=""; unused
        //String out=""; unused

        jTextAreaOutput.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER2"));
        } catch(IOException e){
            System.err.println(e.getMessage());
        }
    }


    // metodo controlla l'esistenza del browser
    public void checkBrowser(){
        // se il defaultBrowser non è stato impostato
        if (null == defaultBrowser || defaultBrowser.trim().equals("")){
            System.out.println("ERR: defaultBrowser not set");
            JOptionPane.showMessageDialog(this.jDialogOption,
            "WEB BROWSER not set",
            "Web Browser Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File file = new File(defaultBrowser);
        if (!(file.exists())){
            System.out.println(defaultBrowser+": "+java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER1"));
            JOptionPane.showMessageDialog(this.jDialogOption,
            defaultBrowser+": "+
            java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER1") ,
            "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }


    // load JIF.ini config
    public void loadJifIni(){
        int x=0,y=0;
        int height=0,width=0;
        String mode="";
        String value="";

        File fileIni = new File(workingDir+"Jif.ini");
        if (!fileIni.exists()){
            // It's the first time JIF is started
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setSize(screenSize.width-200, screenSize.height-140);
            setLocation(screenSize.width/2 - (getWidth()/2), screenSize.height/2 - (getHeight()/2));
            // Inform as default
            jCheckBoxInformMode.setState(true);
            return;
        }
        try{
            BufferedReader br = new BufferedReader(new FileReader(fileIni));
            while ((riga = br.readLine())!=null){
                // salto le di commento che iniziano per Constants.TOKENCOMMENT=#
                if (!(riga.startsWith(Constants.TOKENCOMMENT))&&!(riga.equals(""))){
                    // Jif settings
                    if (riga.indexOf("[JIF.LOCATION.X]=")!=-1){
                        x = new Integer(riga.substring(riga.indexOf("[JIF.LOCATION.X]=")+17)).intValue();
                    }
                    if (riga.indexOf("[JIF.LOCATION.Y]=")!=-1){
                        y = new Integer(riga.substring(riga.indexOf("[JIF.LOCATION.Y]=")+17)).intValue();
                    }
                    if (riga.indexOf("[JIF.WIDTH]=")!=-1){
                        width = new Integer(riga.substring(riga.indexOf("[JIF.WIDTH]=")+12)).intValue();
                    }
                    if (riga.indexOf("[JIF.HEIGHT]=")!=-1){
                        height = new Integer(riga.substring(riga.indexOf("[JIF.HEIGHT]=")+13)).intValue();
                    }

                    if (riga.indexOf("[JIF.MODE]=")!=-1){
                        mode = riga.substring(riga.indexOf("[JIF.MODE]=")+11);
                    }

                    if (riga.indexOf("[JIF.jCheckBoxOutput]=")!=-1){
                        value = riga.substring(riga.indexOf("[JIF.jCheckBoxOutput]=")+22);
                        jCheckBoxOutput.setSelected(value.equals("true"));
                        value="";
                    }
                    if (riga.indexOf("[JIF.jCheckBoxJToolBar]=")!=-1){
                        value = riga.substring(riga.indexOf("[JIF.jCheckBoxJToolBar]=")+24);
                        jCheckBoxJToolBar.setSelected(value.equals("true"));
                        value="";
                    }
                    if (riga.indexOf("[JIF.jCheckBoxJToolBarInform]=")!=-1){
                        value = riga.substring(riga.indexOf("[JIF.jCheckBoxJToolBarInform]=")+30);
                        jCheckBoxJToolBarInform.setSelected(value.equals("true"));
                        value="";
                    }
                    if (riga.indexOf("[JIF.jCheckBoxJTree]=")!=-1){
                        value = riga.substring(riga.indexOf("[JIF.jCheckBoxJTree]=")+21);
                        jCheckBoxJTree.setSelected(value.equals("true"));
                        value="";
                    }
                    if (riga.indexOf("[JIF.LASTFILE]=")!=-1){
                        lastFile = riga.substring(riga.indexOf("[JIF.LASTFILE]=")+15);
                    }

                    if (riga.indexOf("[JIF.DIVIDER1]=")!=-1){
                        int size1 = new Integer(riga.substring(riga.indexOf("[JIF.DIVIDER1]=")+15)).intValue();
                        jSplitPane1.setDividerLocation(size1);
                    }
                    if (riga.indexOf("[JIF.DIVIDER2]=")!=-1){
                        int size2 = new Integer(riga.substring(riga.indexOf("[JIF.DIVIDER2]=")+15)).intValue();
                        jSplitPane2.setDividerLocation(size2);
                    }
                    if (riga.indexOf("[JIF.DIVIDER3]=")!=-1){
                        int size3 = new Integer(riga.substring(riga.indexOf("[JIF.DIVIDER3]=")+15)).intValue();
                        jSplitPane3.setDividerLocation(size3);
                    }
                }
            }

            if (width*height != 0){
                setSize(width, height);
            }
            setLocation(x,y);

            // Setting the last mode used
            if (mode.equals("INFORM")){
                jCheckBoxInformMode.setState(true);
            }
            else{
                jCheckBoxGlulxMode.setState(true);
            }
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
            // in case of error: use defaults
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setSize(screenSize.width-200, screenSize.height-140);
            setLocation(screenSize.width/2 - (getWidth()/2), screenSize.height/2 - (getHeight()/2));
        }

    }


    // save JIF.ini config
    public void saveJifIni(){
        File fileIni = new File(workingDir+"Jif.ini");
        PrintStream ps;
        StringBuffer saveIni = new StringBuffer();
        saveIni
        .append("*** JIF INI FILE ***\n")
        .append("*** DO NOT EDIT MANUALLY ***\n\n")
        .append("[JIF.LOCATION.X]="+this.getX()+"\n")
        .append("[JIF.LOCATION.Y]="+this.getY()+"\n")
        .append("[JIF.WIDTH]="+this.getWidth()+"\n")
        .append("[JIF.HEIGHT]="+this.getHeight()+"\n")

        .append("[JIF.MODE]="+ (jCheckBoxInformMode.isSelected() ? "INFORM":"GLULX") +"\n")
        .append("[JIF.jCheckBoxOutput]="+ jCheckBoxOutput.isSelected()+"\n")
        .append("[JIF.jCheckBoxJToolBar]="+ jCheckBoxJToolBar.isSelected()+"\n")
        .append("[JIF.jCheckBoxJToolBarInform]="+ jCheckBoxJToolBarInform.isSelected()+"\n")
        .append("[JIF.jCheckBoxJTree]="+ jCheckBoxJTree.isSelected()+"\n")

        .append("[JIF.DIVIDER1]="+ jSplitPane1.getDividerLocation()+"\n")
        .append("[JIF.DIVIDER2]="+ jSplitPane2.getDividerLocation()+"\n")
        .append("[JIF.DIVIDER3]="+ jSplitPane3.getDividerLocation()+"\n")
        .append("[JIF.LASTFILE]="+lastFile)
        ;
        try{
            FileOutputStream fos = new FileOutputStream(fileIni);
            ps = new PrintStream( fos );
            ps.println (saveIni.toString());
            ps.close();
        } catch(IOException e ){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
    }

    public String checkDefinitionCurrentFile(String entity){
        
        String file ="";
        String main ="";
        
        file = getCurrentFilename();
            
            // check only if the file is an INF or h file
            if ( (file.indexOf(".inf")!=-1) || (file.indexOf(".INF")!=-1)){                        
                // open and reads the file
                try{
                    StringBuffer sb = new StringBuffer();
                    String riga;
                    sb.setLength(0);
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    while ((riga = br.readLine())!=null){
                        sb.append(riga).append("\n");
                    }
                    br.close();
                    main = sb.toString(); 
                    // Search for entity
                    String pattern = "Object ";
                    String hang="";
                    String tmp="";
                    String appoggio;
                    int pos = 0;
                    StringTokenizer sttok;

                    while ((pos = Utils.IgnoreCaseIndexOf(main,pattern, pos)) >= 0){        
                        appoggio = main.substring(pos,main.indexOf("\n",pos));
                        int posizione_freccia=0;
                        posizione_freccia = appoggio.lastIndexOf("->");
                        appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio,pattern));
                        appoggio = appoggio.trim();
                        if (appoggio.indexOf("!")==-1 && appoggio.equals("")){
                            if (posizione_freccia==-1) {
                                posizione_freccia=0;
                            } else posizione_freccia -=3;

                            tmp = main.substring(pos+pattern.length()-1+posizione_freccia);
                            if (tmp.trim().startsWith("\"")){
                                sttok = new StringTokenizer(tmp.trim(),"\"");
                            }
                            else{
                                sttok = new StringTokenizer(tmp," ;");
                            }
                            hang = sttok.nextToken();
                            //category4.add(new DefaultMutableTreeNode( new Inspect(hang,pos,pos+pattern.length()-1)));
                            if (hang.toLowerCase().equals(entity)){
                                return file;
                            }                        
                        }
                        pos += pattern.length();
                    }                   



                    // ***************************************************
                    pattern = "Global ";
                    appoggio=""; 
                    pos = 0;
                    while ((pos = Utils.IgnoreCaseIndexOf(main,pattern, pos)) >= 0)       {        
                        appoggio = main.substring(pos,main.indexOf("\n",pos));
                        appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio,pattern));
                        if (appoggio.indexOf("!")==-1 && appoggio.trim().equals("")){
                            sttok = new StringTokenizer(main.substring(pos+pattern.length())," ;=");
                            if (sttok.nextToken().toLowerCase().equals(entity)){
                                return file;
                            }                        
                        }
                        pos += pattern.length();
                    }            
                    // ***************************************************


                    // ***************************************************
                    pattern = "Constant ";
                    pos = 0;
                    while ((pos = Utils.IgnoreCaseIndexOf(main,pattern, pos)) >= 0)       {
                        appoggio = main.substring(pos,main.indexOf("\n",pos));
                        appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio,pattern));
                        if (appoggio.indexOf("!")==-1 && appoggio.trim().equals("")){
                            sttok = new StringTokenizer(main.substring(pos+pattern.length())," ;=");
                            if (sttok.nextToken().toLowerCase().equals(entity)){
                                return file;
                            }   
                        }
                        pos += pattern.length();
                    }
                    // ***************************************************          


                    // ***************************************************          
                    pattern = "Sub";
                    pos = 0;
                    tmp="";
                    while ((pos = Utils.IgnoreCaseIndexOf(main,pattern, pos)) >= 0){        
                        appoggio = main.substring(pos,main.indexOf("\n",pos));
                        if (appoggio.indexOf("!")==-1 && appoggio.indexOf('[')>=0 && appoggio.indexOf(';')>=0){
                            tmp = main.substring(0,pos);
                            tmp = tmp.substring(tmp.lastIndexOf('[')+1);
                            tmp = tmp.trim();
                            if ((tmp+pattern).toLowerCase().equals(entity)){
                                return file;
                            }  
                        }
                        pos += pattern.length();
                    }
                    // ***************************************************  

                    // ***************************************************  
                    pattern = "Class ";
                    pos = 0;
                    while ((pos = Utils.IgnoreCaseIndexOf(main,pattern, pos)) >= 0){        
                        appoggio = main.substring(pos,main.indexOf("\n",pos));
                        appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio,pattern));
                        appoggio = appoggio.trim();
                        if (appoggio.indexOf("!")==-1 && appoggio.equals("")){
                            sttok = new StringTokenizer(main.substring(pos+pattern.length())," ;\n");
                            String nome = sttok.nextToken();
                            if (nome.toLowerCase().equals(entity)){
                                return file;
                            }                      
                        }
                        pos += pattern.length();
                    }
                    // ***************************************************  


                    // ***************************************************  
                    // ****** Functions
                    pattern="[";
                    pos=0;
                    //int lunghezza=0; unused
                    tmp ="";
                    while ((pos = main.indexOf(pattern, pos)) >= 0){
                        appoggio = main.substring(pos,main.indexOf("\n",pos));
                        appoggio = appoggio.trim();
                        if (appoggio.indexOf("!")==-1  && appoggio.startsWith("[")){
                            tmp = main.substring(pos);
                            tmp = tmp.substring(1,tmp.indexOf(';'));
                            tmp = tmp.trim();
                            if (!tmp.equals("") && (tmp.indexOf('\"')==-1) && (tmp.indexOf("Sub"))==-1){
                                sttok = new StringTokenizer(tmp," ;\n");
                                if (sttok.hasMoreTokens()){
                                    tmp = sttok.nextToken();
                                }
                                if (tmp.toLowerCase().equals(entity)){
                                    return file;
                                }                                                  
                            }
                        }
                        pos += pattern.length();
                    }            
                    // ***************************************************  

                    // ***************************************************  
                    for (int j=0 ; j < projectClass.size(); j++){
                        //String classe = (String) projectClass.get(j);
                        //pattern = "Class ";
                        pattern = (String) projectClass.get(j);
//System.out.println("Classe ="+pattern);                        
                        pos = 0;
                        while ((pos = Utils.IgnoreCaseIndexOf(main,pattern, pos)) >= 0){        
                            appoggio = main.substring(pos,main.indexOf("\n",pos));
                            appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio,pattern));
                            appoggio = appoggio.trim();
                            if (appoggio.indexOf("!")==-1 && appoggio.equals("")){
                                sttok = new StringTokenizer(main.substring(pos+pattern.length())," ;\n");
                                String nome = sttok.nextToken();
                                if (nome.toLowerCase().equals(entity)){
                                    return file;
                                }                      
                            }
                            pos += pattern.length();
                        }                                                                        
                    }
                    // ***************************************************  
                    
                    
                }catch(Exception e){
                    e.printStackTrace();
                }
            }                                
        return null;
    }    
    
    // This method seeks for the definition of "entity" within the whole project
    public String checkDefinition(String entity){        
        
        // before I'll try the current filename
        String current = checkDefinitionCurrentFile(entity);
        if (current != null){
            return current;
        }        
        
        String file ="";
        String main ="";
        for (int i=0; i<projectFiles.size();i++)  {
            file = ((FileProject)projectFiles.elementAt(i)).path;
            
            // check only if the file is an INF or h file
            // and if isn't the current file
            if ( (file.indexOf(".inf")!=-1) || (file.indexOf(".INF")!=-1) || !file.equals(current)){                        
                // open and reads the file
                try{
                    StringBuffer sb = new StringBuffer();
                    String riga;
                    sb.setLength(0);
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    while ((riga = br.readLine())!=null){
                        sb.append(riga).append("\n");
                    }
                    br.close();
                    main = sb.toString(); 
//System.out.println("Cerco nel file="+file);
                    // Search for entity
                    String pattern = "Object ";
                    String hang="";
                    String tmp="";
                    String appoggio;
                    int pos = 0;
                    StringTokenizer sttok;

                    while ((pos = Utils.IgnoreCaseIndexOf(main,pattern, pos)) >= 0){        
                        appoggio = main.substring(pos,main.indexOf("\n",pos));
                        int posizione_freccia=0;
                        posizione_freccia = appoggio.lastIndexOf("->");
                        appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio,pattern));
                        appoggio = appoggio.trim();
                        if (appoggio.indexOf("!")==-1 && appoggio.equals("")){
                            if (posizione_freccia==-1) {
                                posizione_freccia=0;
                            } else posizione_freccia -=3;

                            tmp = main.substring(pos+pattern.length()-1+posizione_freccia);
                            if (tmp.trim().startsWith("\"")){
                                sttok = new StringTokenizer(tmp.trim(),"\"");
                            }
                            else{
                                sttok = new StringTokenizer(tmp," ;");
                            }
                            hang = sttok.nextToken();
                            //category4.add(new DefaultMutableTreeNode( new Inspect(hang,pos,pos+pattern.length()-1)));
                            if (hang.toLowerCase().equals(entity)){
                                return file;
                            }                        
                        }
                        pos += pattern.length();
                    }                   



                    // ***************************************************
                    pattern = "Global ";
                    appoggio=""; 
                    pos = 0;
                    while ((pos = Utils.IgnoreCaseIndexOf(main,pattern, pos)) >= 0)       {        
                        appoggio = main.substring(pos,main.indexOf("\n",pos));
                        appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio,pattern));
                        if (appoggio.indexOf("!")==-1 && appoggio.trim().equals("")){
                            sttok = new StringTokenizer(main.substring(pos+pattern.length())," ;=");
                            if (sttok.nextToken().toLowerCase().equals(entity)){
                                return file;
                            }                        
                        }
                        pos += pattern.length();
                    }            
                    // ***************************************************


                    // ***************************************************
                    pattern = "Constant ";
                    pos = 0;
                    while ((pos = Utils.IgnoreCaseIndexOf(main,pattern, pos)) >= 0)       {
                        appoggio = main.substring(pos,main.indexOf("\n",pos));
                        appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio,pattern));
                        if (appoggio.indexOf("!")==-1 && appoggio.trim().equals("")){
                            sttok = new StringTokenizer(main.substring(pos+pattern.length())," ;=");
                            if (sttok.nextToken().toLowerCase().equals(entity)){
                                return file;
                            }   
                        }
                        pos += pattern.length();
                    }
                    // ***************************************************          


                    // ***************************************************          
                    pattern = "Sub";
                    pos = 0;
                    tmp="";
                    while ((pos = Utils.IgnoreCaseIndexOf(main,pattern, pos)) >= 0){        
                        appoggio = main.substring(pos,main.indexOf("\n",pos));
                        if (appoggio.indexOf("!")==-1 && appoggio.indexOf('[')>=0 && appoggio.indexOf(';')>=0){
                            tmp = main.substring(0,pos);
                            tmp = tmp.substring(tmp.lastIndexOf('[')+1);
                            tmp = tmp.trim();
                            if ((tmp+pattern).toLowerCase().equals(entity)){
                                return file;
                            }  
                        }
                        pos += pattern.length();
                    }
                    // ***************************************************  

                    // ***************************************************  
                    pattern = "Class ";
                    pos = 0;
                    while ((pos = Utils.IgnoreCaseIndexOf(main,pattern, pos)) >= 0){        
                        appoggio = main.substring(pos,main.indexOf("\n",pos));
                        appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio,pattern));
                        appoggio = appoggio.trim();
                        if (appoggio.indexOf("!")==-1 && appoggio.equals("")){
                            sttok = new StringTokenizer(main.substring(pos+pattern.length())," ;\n");
                            String nome = sttok.nextToken();
                            if (nome.toLowerCase().equals(entity)){
                                return file;
                            }                      
                        }
                        pos += pattern.length();
                    }
                    // ***************************************************  


                    // ***************************************************  
                    // ****** Functions
                    pattern="[";
                    pos=0;
                    //int lunghezza=0; unused
                    tmp ="";
                    while ((pos = main.indexOf(pattern, pos)) >= 0){
                        appoggio = main.substring(pos,main.indexOf("\n",pos));
                        appoggio = appoggio.trim();
                        if (appoggio.indexOf("!")==-1  && appoggio.startsWith("[")){
                            tmp = main.substring(pos);
                            tmp = tmp.substring(1,tmp.indexOf(';'));
                            tmp = tmp.trim();
                            if (!tmp.equals("") && (tmp.indexOf('\"')==-1) && (tmp.indexOf("Sub"))==-1){
                                sttok = new StringTokenizer(tmp," ;\n");
                                if (sttok.hasMoreTokens()){
                                    tmp = sttok.nextToken();
                                }
                                if (tmp.toLowerCase().equals(entity)){
                                    return file;
                                }                                                  
                            }
                        }
                        pos += pattern.length();
                    }            
                    // ***************************************************  

                    // ***************************************************  
                    for (int j=0 ; j < projectClass.size(); j++){
                        //String classe = (String) projectClass.get(j);
                        //pattern = "Class ";
                        pattern = (String) projectClass.get(j);
//System.out.println("Classe ="+pattern);                        
                        pos = 0;
                        while ((pos = Utils.IgnoreCaseIndexOf(main,pattern, pos)) >= 0){        
                            appoggio = main.substring(pos,main.indexOf("\n",pos));
                            appoggio = appoggio.substring(0, Utils.IgnoreCaseIndexOf(appoggio,pattern));
                            appoggio = appoggio.trim();
                            if (appoggio.indexOf("!")==-1 && appoggio.equals("")){
                                sttok = new StringTokenizer(main.substring(pos+pattern.length())," ;\n");
                                String nome = sttok.nextToken();
                                if (nome.toLowerCase().equals(entity)){
                                    return file;
                                }                      
                            }
                            pos += pattern.length();
                        }                                                                        
                    }
                    // ***************************************************  
                    
                    
                }catch(Exception e){
                      e.printStackTrace();
    //                System.out.println("ERR: " + e.getMessage());
    //                e.printStackTrace();
    //                System.err.println(e.getMessage());
                }
            }                                
        }   
        return null;
    }
    

    // It will reload the current file to make it with a refreshed syntax
    public void refreshSyntax(){        
        JIFTextPane jif = getCurrentJIFTextPane();
    
        // Save the current Cursor Position
        int position = jif.getCaretPosition();
        String main = jif.getText();
        jif.setText("");
        jif.setText(main); 
        jif.setCaretPosition(position);
    }
    
    public JTabbedPane getTabbed(){
        return jTabbedPane1;
    }

    public JTabbedPane getTabbed2(){
        return jTabbedPane2;
    }


    public Hashtable getMapping(){
        return this.mapping;
    }

    public MutableAttributeSet getAttr(){
        return this.attr;
    }

    public Hashtable getHelpCode(){
        return this.helpcode;
    }

    public Hashtable getAltkeys(){
        return this.altkeys;
    }

    public Hashtable getExecutecommands(){
        return this.executecommands;
    }    
    
    public JCheckBoxMenuItem getCheckBoxOutput(){
        return jCheckBoxOutput;
    }

    /**
     * Getter for property JWindowSymbols.
     * @return Value of property JWindowSymbols.
     */
    public javax.swing.JFrame getJWindowSymbols() {
        return JWindowSymbols;
    }

    /**
     * Setter for property JWindowSymbols.
     * @param JWindowSymbols New value of property JWindowSymbols.
     */
    public void setJWindowSymbols(javax.swing.JFrame JWindowSymbols) {
        this.JWindowSymbols = JWindowSymbols;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem About;
    private javax.swing.JButton AboutButton;
    private javax.swing.JMenuItem BuildAll;
    private javax.swing.JMenuItem Exit;
    private javax.swing.JButton ExitButton;
    private javax.swing.JMenuItem New;
    private javax.swing.JMenuItem Open;
    private javax.swing.JButton OpenButton;
    private javax.swing.JButton RebuildButton;
    private javax.swing.JMenuItem Run;
    private javax.swing.JButton RunButton;
    private javax.swing.JMenuItem Save;
    private javax.swing.JMenuItem SaveAs;
    private javax.swing.JButton SaveAsButton;
    private javax.swing.JButton SaveButton;
    private javax.swing.JButton SaveButtonAll;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton26;
    private javax.swing.JButton jButton27;
    private javax.swing.JButton jButton28;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jButtonAttribute;
    private javax.swing.JButton jButtonBackground;
    private javax.swing.JButton jButtonBlc;
    private javax.swing.JButton jButtonBracketCheck;
    private javax.swing.JButton jButtonBres;
    private javax.swing.JButton jButtonClose;
    private javax.swing.JButton jButtonCloseAll;
    private javax.swing.JButton jButtonComment;
    private javax.swing.JButton jButtonCommentSelection;
    private javax.swing.JButton jButtonCopy;
    private javax.swing.JButton jButtonCut;
    private javax.swing.JButton jButtonDefaultDark;
    private javax.swing.JButton jButtonDefinition;
    private javax.swing.JButton jButtonExtractStrings;
    private javax.swing.JButton jButtonFind;
    private javax.swing.JButton jButtonInfo;
    private javax.swing.JButton jButtonInsertSymbol;
    private javax.swing.JButton jButtonInterpreter;
    private javax.swing.JButton jButtonKeyword;
    private javax.swing.JButton jButtonLeftTab;
    private javax.swing.JButton jButtonNew;
    private javax.swing.JButton jButtonNormal;
    private javax.swing.JButton jButtonOption;
    private javax.swing.JButton jButtonPaste;
    private javax.swing.JButton jButtonPrint;
    private javax.swing.JButton jButtonPrintTutorial;
    private javax.swing.JButton jButtonProperty;
    private javax.swing.JButton jButtonRedo;
    private javax.swing.JButton jButtonReplace;
    private javax.swing.JButton jButtonRightTab;
    private javax.swing.JButton jButtonRoutine;
    private javax.swing.JButton jButtonSearchProject;
    private javax.swing.JButton jButtonSwitchManager;
    private javax.swing.JButton jButtonTranslate;
    private javax.swing.JButton jButtonUncommentSelection;
    private javax.swing.JButton jButtonUndo;
    private javax.swing.JButton jButtonVerb;
    private javax.swing.JCheckBox jCheckBoxAdventInLib;
    public javax.swing.JCheckBox jCheckBoxAutomaticCheckBrackets;
    private javax.swing.JCheckBox jCheckBoxBackup;
    private javax.swing.JCheckBox jCheckBoxCreateNewFile;
    private javax.swing.JCheckBoxMenuItem jCheckBoxGlulxMode;
    public javax.swing.JCheckBox jCheckBoxHelpedCode;
    private javax.swing.JCheckBoxMenuItem jCheckBoxInformMode;
    private javax.swing.JCheckBoxMenuItem jCheckBoxJToolBar;
    private javax.swing.JCheckBoxMenuItem jCheckBoxJToolBarInform;
    private javax.swing.JCheckBoxMenuItem jCheckBoxJTree;
    private javax.swing.JCheckBox jCheckBoxMakeResource;
    private javax.swing.JCheckBox jCheckBoxMapping;
    private javax.swing.JCheckBox jCheckBoxMappingHFile;
    public javax.swing.JCheckBox jCheckBoxMappingLive;
    private javax.swing.JCheckBox jCheckBoxNumberLines;
    private javax.swing.JCheckBox jCheckBoxOpenLastFile;
    private javax.swing.JCheckBoxMenuItem jCheckBoxOutput;
    private javax.swing.JCheckBox jCheckBoxProjectCloseAll;
    private javax.swing.JCheckBox jCheckBoxProjectOpenAllFiles;
    private javax.swing.JCheckBox jCheckBoxScanProjectFiles;
    public javax.swing.JCheckBox jCheckBoxSyntax;
    private javax.swing.JCheckBoxMenuItem jCheckBoxToggleFullscreen;
    public javax.swing.JCheckBox jCheckBoxWrapLines;
    private javax.swing.JComboBox jComboBoxFont;
    private javax.swing.JComboBox jComboBoxFontSize;
    private javax.swing.JDialog jDialogAbout;
    private javax.swing.JDialog jDialogConfigFiles;
    private javax.swing.JDialog jDialogInfo;
    public javax.swing.JDialog jDialogOption;
    private javax.swing.JDialog jDialogReplace;
    private javax.swing.JDialog jDialogSwitches;
    private javax.swing.JDialog jDialogText;
    private javax.swing.JDialog jDialogTutorial;
    private javax.swing.JEditorPane jEditorPaneColor;
    private javax.swing.JEditorPane jEditorPaneTutorial;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelAttribute;
    private javax.swing.JLabel jLabelBackground;
    private javax.swing.JLabel jLabelBlc;
    private javax.swing.JLabel jLabelBres;
    private javax.swing.JLabel jLabelComment;
    private javax.swing.JLabel jLabelDefaultDark;
    private javax.swing.JLabel jLabelKeyword;
    private javax.swing.JLabel jLabelMainFile;
    private javax.swing.JLabel jLabelMaxRecentFiles;
    private javax.swing.JLabel jLabelNormal;
    private javax.swing.JLabel jLabelProperty;
    private javax.swing.JLabel jLabelRoutine;
    private javax.swing.JLabel jLabelTutorial;
    private javax.swing.JLabel jLabelVerb;
    private javax.swing.JList jListProject;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuBuild;
    private javax.swing.JMenu jMenuEdit;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuGlulx;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenu jMenuInsertNew;
    private javax.swing.JMenuItem jMenuItemAddFileToProject;
    private javax.swing.JMenuItem jMenuItemAddNewToProject;
    private javax.swing.JMenuItem jMenuItemAltKeys;
    private javax.swing.JMenuItem jMenuItemBuildAllGlulx;
    private javax.swing.JMenuItem jMenuItemClear;
    private javax.swing.JMenuItem jMenuItemClearAll;
    private javax.swing.JMenuItem jMenuItemClearRecentFiles;
    private javax.swing.JMenuItem jMenuItemClose;
    private javax.swing.JMenuItem jMenuItemCloseAll;
    private javax.swing.JMenuItem jMenuItemCloseProject;
    private javax.swing.JMenuItem jMenuItemCommentSelection;
    private javax.swing.JMenuItem jMenuItemCompile;
    private javax.swing.JMenuItem jMenuItemConfigurazione;
    private javax.swing.JMenuItem jMenuItemCopy;
    private javax.swing.JMenuItem jMenuItemCopy1;
    private javax.swing.JMenuItem jMenuItemCopyright;
    private javax.swing.JMenuItem jMenuItemCut;
    private javax.swing.JMenuItem jMenuItemCut1;
    private javax.swing.JMenuItem jMenuItemHelp;
    private javax.swing.JMenuItem jMenuItemHelpedCode;
    private javax.swing.JMenuItem jMenuItemInsertFile;
    private javax.swing.JMenuItem jMenuItemInsertFromFile;
    private javax.swing.JMenuItem jMenuItemInsertSymbol;
    private javax.swing.JMenuItem jMenuItemInsertSymbol1;
    private javax.swing.JMenuItem jMenuItemJumpToSource;
    private javax.swing.JMenuItem jMenuItemLeftShift;
    private javax.swing.JMenuItem jMenuItemLinks;
    private javax.swing.JMenuItem jMenuItemMakeBlb;
    private javax.swing.JMenuItem jMenuItemMakeResource;
    private javax.swing.JMenuItem jMenuItemNewProject;
    private javax.swing.JMenuItem jMenuItemNextBookmark;
    private javax.swing.JMenuItem jMenuItemOpenProject;
    private javax.swing.JMenuItem jMenuItemPaste;
    private javax.swing.JMenuItem jMenuItemPopupAddNewToProject;
    private javax.swing.JMenuItem jMenuItemPopupAddToProject;
    private javax.swing.JMenuItem jMenuItemPopupClose;
    private javax.swing.JMenuItem jMenuItemPopupCloseAllFiles;
    private javax.swing.JMenuItem jMenuItemPopupCloseProject;
    private javax.swing.JMenuItem jMenuItemPopupNewProject;
    private javax.swing.JMenuItem jMenuItemPopupOpenProject;
    private javax.swing.JMenuItem jMenuItemPopupOpenSelectedFiles;
    private javax.swing.JMenuItem jMenuItemPopupRemoveFromProject;
    private javax.swing.JMenuItem jMenuItemPopupSaveProject;
    private javax.swing.JMenuItem jMenuItemPrint;
    private javax.swing.JMenuItem jMenuItemPrint1;
    private javax.swing.JMenuItem jMenuItemRemoveFromProject;
    private javax.swing.JMenuItem jMenuItemRemoveMainClass;
    private javax.swing.JMenuItem jMenuItemReplace;
    private javax.swing.JMenuItem jMenuItemRightShift;
    private javax.swing.JMenuItem jMenuItemRunBlb;
    private javax.swing.JMenuItem jMenuItemRunUlx;
    private javax.swing.JMenuItem jMenuItemSaveAll;
    private javax.swing.JMenuItem jMenuItemSaveProject;
    private javax.swing.JMenuItem jMenuItemSearch;
    private javax.swing.JMenuItem jMenuItemSearchAllFiles;
    private javax.swing.JMenuItem jMenuItemSelectAll;
    private javax.swing.JMenuItem jMenuItemSetBookmark;
    private javax.swing.JMenuItem jMenuItemSetMainClass;
    private javax.swing.JMenuItem jMenuItemSettings;
    private javax.swing.JMenuItem jMenuItemSwitch;
    private javax.swing.JMenuItem jMenuItemSwitches;
    private javax.swing.JMenuItem jMenuItemSyntax;
    private javax.swing.JMenuItem jMenuItemUncommentSelection;
    private javax.swing.JMenu jMenuLinks;
    private javax.swing.JMenu jMenuMode;
    private javax.swing.JMenu jMenuOptions;
    private javax.swing.JMenu jMenuPaste;
    private javax.swing.JMenu jMenuProject;
    private javax.swing.JMenu jMenuRecentFiles;
    private javax.swing.JMenu jMenuTutorial;
    private javax.swing.JMenu jMenuView;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel29;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel32;
    private javax.swing.JPanel jPanel33;
    private javax.swing.JPanel jPanel34;
    private javax.swing.JPanel jPanel35;
    private javax.swing.JPanel jPanel36;
    private javax.swing.JPanel jPanel37;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel42;
    private javax.swing.JPanel jPanel43;
    private javax.swing.JPanel jPanel44;
    private javax.swing.JPanel jPanel45;
    private javax.swing.JPanel jPanel46;
    private javax.swing.JPanel jPanel47;
    private javax.swing.JPanel jPanel48;
    private javax.swing.JPanel jPanel49;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelColor;
    private javax.swing.JPanel jPanelDefaultDark;
    private javax.swing.JPanel jPanelFont;
    private javax.swing.JPanel jPanelGeneral;
    private javax.swing.JPanel jPanelMainFile;
    private javax.swing.JPanel jPanelPath;
    private javax.swing.JPanel jPanelSwitch1;
    private javax.swing.JPanel jPanelSwitch2;
    private javax.swing.JPanel jPanelTreeControl;
    public javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JPopupMenu jPopupMenuProject;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPaneInfo;
    private javax.swing.JScrollPane jScrollPaneProject;
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
    private javax.swing.JSeparator jSeparator21;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JSplitPane jSplitPane3;
    private static final javax.swing.JTabbedPane jTabbedPane1 = new javax.swing.JTabbedPane();
    private static final javax.swing.JTabbedPane jTabbedPane2 = new javax.swing.JTabbedPane();
    private javax.swing.JTabbedPane jTabbedPaneOption;
    private javax.swing.JTextArea jTextArea4;
    private javax.swing.JTextArea jTextAreaConfig;
    private javax.swing.JTextArea jTextAreaInfo;
    private javax.swing.JTextArea jTextAreaOutput;
    private javax.swing.JTextField jTextFieldBlc;
    private javax.swing.JTextField jTextFieldBres;
    private javax.swing.JTextField jTextFieldDefinition;
    public javax.swing.JTextField jTextFieldFind;
    private javax.swing.JTextField jTextFieldFont;
    private javax.swing.JTextField jTextFieldMaxRecentFiles;
    private javax.swing.JTextField jTextFieldPathBrowser;
    private javax.swing.JTextField jTextFieldPathCompiler;
    private javax.swing.JTextField jTextFieldPathGames;
    private javax.swing.JTextField jTextFieldPathGlulx;
    private javax.swing.JTextField jTextFieldPathInterpreter;
    private javax.swing.JTextField jTextFieldPathLib;
    private javax.swing.JTextField jTextFieldPathLibSecondary1;
    private javax.swing.JTextField jTextFieldPathLibSecondary2;
    private javax.swing.JTextField jTextFieldPathLibSecondary3;
    private javax.swing.JTextField jTextFieldReplace;
    private javax.swing.JTextField jTextFieldReplaceFind;
    protected javax.swing.JTextField jTextFieldRowCol;
    private static final javax.swing.JToolBar jToolBarCommon = new javax.swing.JToolBar();
    private static final javax.swing.JToolBar jToolBarInform = new javax.swing.JToolBar();
    private static javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables

    // PATHS
    private String fileInf = "";
    private String fileInf_withmapping = "";    // il nome del file temporaneo quando il mapping è attivo
    private String workingDir= "C:"+Constants.SEP+"Jif"+Constants.SEP;
    private String insertnewdir="";    // JIF si ricorda dell'ultima directory scelta per "insert new"
    private String glulx = "";
    private String interpreter="";
    private String compiler ="";
    private String libPath="";
    private String libPathSecondary1="";
    private String libPathSecondary2="";
    private String libPathSecondary3="";
    private String gamesDir="";
    //private String projectDir="";
    private String configDir = "";

    protected DefaultStyledDocument doc;

    MouseListener popupListenerProject = new PopupListenerProject();
    MouseListener menuListener  = new MenuListener();

    // per le operazione del menu click col destro
    protected HashMap operations = new HashMap();

    // gestione syntax highlights on/off
    protected StringBuffer sb = new StringBuffer();

    // element per mostrare il numero di riga
    private Element el;

    // gestione albero INSPECT
    private DefaultMutableTreeNode top,category1,category2,category4,category5,category6,category7;
    //private DefaultMutableTreeNode category3;

    private HighlightText hlighterOutput;

    private StringTokenizer st;
    private DefaultTreeModel treeModel;
    private volatile String riga;

    // gestione aggiunta automatica codice assistito
    //private String ultima_word;     // ultima keyword digitata
    private String ultima;          // ultima keyword digitata
    private Hashtable helpcode;

    //gestione mappaing
    private Vector mappa;
    private Hashtable mapping;

    //gestion switches
    private Hashtable switches;

    private boolean loaded=false;   //serve per sapere se ho già caricato il prog.

    private MutableAttributeSet attr;

    // per scegliere l'estensione del file da passare all'interprete
    private String tipoz = "";

    //tasti F1..F12
    //private Hashtable tastiFunzione;
    // Saves the ALT+char mapping and executecommands for the commands to be executed
    private Hashtable altkeys;
    private Hashtable executecommands;
    

    // Vettore che contiene i nomi delle nuove classi all'interno del sorgente
     private DefaultMutableTreeNode tmp_nodo;

    // Syntax highlight
    public HashSet attributes;
    public HashSet properties;
    public HashSet routines;
    public HashSet verbs;
    public HashSet keywords;
    
    // Case Sensitive Management
    public HashSet attributes_cs;
    public HashSet properties_cs;
    public HashSet routines_cs;
    public HashSet verbs_cs;
    public HashSet keywords_cs;    

    // New Files name counter
    private int countNewFile=0;

    // Gestione finestra con i simboli (come la windowObject)
    private JFrame JWindowSymbols;
    private JList jListSymbols;

    // links
    private String defaultBrowser;

    // titolo di JIF, serve per aggiungerci il nome del progetto aperto
    private String jifVersion = "Jif "+ Constants.JIFVERSION + "     Inform Mode";
    private Hashtable tutorialtarget;

    // COLORS
    public Color colorKeyword;
    public Color colorAttribute;
    public Color colorProperty;
    public Color colorRoutine;
    public Color colorVerb;
    public Color colorNormal;
    public Color colorComment;
    public Color colorBackground;
    public Font defaultFont;


    // Tree
    private boolean explode=true;

    // vettore che memorizza i flag di tipo -v5,-v6 ecc
    private Vector flags;

    // vettore che memorizza i flag di tipo +language_name= ecc
    private Vector flags_language;

    // ultimo file aperto...
    private String lastFile;
    private int maxRecentFiles=10;


    // ultima dir usata per aprire un file
    private String lastDir = null;

    // spellcheck file name
    //private String spellcheck; unused

    // Inform/Glulx Mode
    // private String Mode; unused

    // PROJECT VARIABLES
    private String currentProject ="default";
    private Vector projectFiles ;
    private Vector projectClass = new Vector();
    private String mainFile="";
    // private TitledBorder tb; unused

    // Main file for the compiling process
    private Dimension screensize;

    // alphabetical sorting
    private Vector objTree;

    public String getJifVersion() {
        return jifVersion;
    }

    public void setJifVersion(String jifVersion) {
        this.jifVersion = jifVersion;
    }
}
