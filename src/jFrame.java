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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
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

import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
 * jFrame.java
 *
 * Created on 28 luglio 2003, 9.58
 */


/** Main Class for Jif application.
 * Jif is a Java Editor for Inform
 * @author Alessandro Schillaci
 * @version 3.0
 */
public class jFrame extends JFrame {
    
    private static final long serialVersionUID = 7544939067324000307L;
    
    public jFrame() {
        
        //Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/runInterpreter.png")));
        workingDir = System.getProperty("user.dir");
        
        // To Force another location for the config.ini file just run this:
        // java.exe -Duser.language=en -Duser.region=US -Djif.configuration=[NEWPATH] -cp . -jar Jif.jar
        // where [NEWPATH] is the config.ini path
        if (System.getProperty("jif.configuration") != null){
            System.out.println("Load new config file: "+System.getProperty("jif.configuration"));
            fileini = System.getProperty("jif.configuration");
        } else{
            fileini = workingDir+Constants.SEP+"config.ini";
        }
        
        initComponents();
        disableComponents();
        disableProject();
        setJListSymbols(new JList());
        
        loadConfigNew(new File(fileini));
        projectFiles = new Vector();
        updateProjectTitle("Project: "+ currentProject);
        
        JWindowSymbols = new JFrame();
        JWindowSymbols.setResizable(false);
        JWindowSymbols.setBackground(Color.LIGHT_GRAY);
        JWindowSymbols.setUndecorated(true);
        
        getJListSymbols().addKeyListener(new java.awt.event.KeyAdapter(){
            MutableAttributeSet attr = new SimpleAttributeSet();;
            public void keyPressed(java.awt.event.KeyEvent ke){
                if ((ke.getKeyCode()==KeyEvent.VK_ENTER)){
                    try{
                        if (jCheckBoxMappingLive.isSelected()&&getMapping().containsKey((String)getJListSymbols().getSelectedValue())){
                            getCurrentDoc().insertString(getCurrentJIFTextPane().getCaretPosition(), (String)getMapping().get((String)getJListSymbols().getSelectedValue()), attr);
                        } else getCurrentDoc().insertString(getCurrentJIFTextPane().getCaretPosition(), (String)getJListSymbols().getSelectedValue() , attr);
                        JWindowSymbols.setVisible(false);
                    } catch(BadLocationException e){
                        System.out.println(e.getMessage());
                    }
                }
                if ((ke.getKeyCode()==KeyEvent.VK_ESCAPE)){
                    JWindowSymbols.setVisible(false);
                }
            }
        });
        getJListSymbols().addMouseListener(new java.awt.event.MouseAdapter() {
            MutableAttributeSet attr = new SimpleAttributeSet();
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                JWindowSymbols.setTitle(java.util.ResourceBundle.getBundle("JIF").getString("JWINDOW_TOOLTIP"));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                JWindowSymbols.setTitle(java.util.ResourceBundle.getBundle("JIF").getString("STR_SYMBOLS"));
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount()==2){
                    try{
                        if (jCheckBoxMappingLive.isSelected()&&getMapping().containsKey((String)getJListSymbols().getSelectedValue())){
                            getCurrentDoc().insertString(getCurrentJIFTextPane().getCaretPosition(), (String)getMapping().get((String)getJListSymbols().getSelectedValue()), attr);
                        } else getCurrentDoc().insertString(getCurrentJIFTextPane().getCaretPosition(), (String)getJListSymbols().getSelectedValue() , attr);
                        JWindowSymbols.setVisible(false);
                    } catch(BadLocationException e){
                        System.out.println(e.getMessage());
                    }
                }
            }
        });
        
        JScrollPane jsp1 = new JScrollPane();
        jsp1.setPreferredSize(new java.awt.Dimension(30, 30));
        jsp1.setMinimumSize(new java.awt.Dimension(0, 0));
        jsp1.setViewportView(getJListSymbols());
        JWindowSymbols.getContentPane().add(jsp1);
        JWindowSymbols.toFront();
        
        //attr = new SimpleAttributeSet();
        hlighterOutputErrors    = new HighlightText(Constants.colorErrors);
        hlighterOutputWarnings  = new HighlightText(Constants.colorWarnings);
        
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
        java.awt.GridBagConstraints gridBagConstraints;

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
        jPanel7 = new javax.swing.JPanel();
        jButton9 = new javax.swing.JButton();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        jLabel7 = new javax.swing.JLabel();
        jScrollPaneAbout = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jDialogEditFileIni = new JDialog (this, "", false);
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextAreaConfig = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jDialogSwitches = new JDialog (this, "", false);
        jPanel11 = new javax.swing.JPanel();
        jPanelSwitch1 = new javax.swing.JPanel();
        jPanelSwitch2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jButton15 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jDialogText = new JDialog (this, "", false);
        jPanel8 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTextArea4 = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        jDialogReplace = new javax.swing.JDialog();
        jPanel12 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldReplaceFind = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldReplace = new javax.swing.JTextField();
        jPanel20 = new javax.swing.JPanel();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
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
        jPanelGeneralOptions = new javax.swing.JPanel();
        jCheckBoxOpenLastFile = new javax.swing.JCheckBox();
        jCheckBoxCreateNewFile = new javax.swing.JCheckBox();
        jCheckBoxMappingLive = new javax.swing.JCheckBox();
        jCheckBoxHelpedCode = new javax.swing.JCheckBox();
        jCheckBoxSyntax = new javax.swing.JCheckBox();
        jCheckBoxNumberLines = new javax.swing.JCheckBox();
        jCheckBoxScanProjectFiles = new javax.swing.JCheckBox();
        jCheckBoxWrapLines = new javax.swing.JCheckBox();
        jCheckBoxProjectOpenAllFiles = new javax.swing.JCheckBox();
        jCheckBoxMakeResource = new javax.swing.JCheckBox();
        jCheckBoxAdventInLib = new javax.swing.JCheckBox();
        jPanelPath = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jTextFieldPathLib = new javax.swing.JTextField();
        jButtonLibraryPath = new javax.swing.JButton();
        jPanel43 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        jTextFieldPathLibSecondary1 = new javax.swing.JTextField();
        jButtonLibraryPath1 = new javax.swing.JButton();
        jPanel45 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        jTextFieldPathLibSecondary2 = new javax.swing.JTextField();
        jButtonLibraryPath2 = new javax.swing.JButton();
        jPanel49 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        jTextFieldPathLibSecondary3 = new javax.swing.JTextField();
        jButtonLibraryPath3 = new javax.swing.JButton();
        jPanel17 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        jTextFieldPathGames = new javax.swing.JTextField();
        jButtonCompiledPath = new javax.swing.JButton();
        jPanel18 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jTextFieldPathCompiler = new javax.swing.JTextField();
        jButtonCompilerPath = new javax.swing.JButton();
        jPanel19 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jTextFieldPathInterpreter = new javax.swing.JTextField();
        jButtonInterpreterPath = new javax.swing.JButton();
        jPanel44 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        jTextFieldPathGlulx = new javax.swing.JTextField();
        jButtonGlulxPath = new javax.swing.JButton();
        jPanel46 = new javax.swing.JPanel();
        jLabelBres = new javax.swing.JLabel();
        jTextFieldBres = new javax.swing.JTextField();
        jButtonBres = new javax.swing.JButton();
        jPanel47 = new javax.swing.JPanel();
        jLabelBlc = new javax.swing.JLabel();
        jTextFieldBlc = new javax.swing.JTextField();
        jButtonBlc = new javax.swing.JButton();
        jPanelColor = new javax.swing.JPanel();
        jEditorPaneColor = new javax.swing.JEditorPane();
        jPanel14 = new javax.swing.JPanel();
        jPanelDefaultDark = new javax.swing.JPanel();
        jLabelDefaultDark = new javax.swing.JLabel();
        jButtonDefaultDark = new javax.swing.JButton();
        jPanel32 = new javax.swing.JPanel();
        jLabelComment = new javax.swing.JLabel();
        jButtonComment = new javax.swing.JButton();
        jPanel31 = new javax.swing.JPanel();
        jLabelNormal = new javax.swing.JLabel();
        jButtonNormal = new javax.swing.JButton();
        jPanel30 = new javax.swing.JPanel();
        jLabelVerb = new javax.swing.JLabel();
        jButtonVerb = new javax.swing.JButton();
        jPanel37 = new javax.swing.JPanel();
        jLabelBackground = new javax.swing.JLabel();
        jButtonBackground = new javax.swing.JButton();
        jPanel28 = new javax.swing.JPanel();
        jLabelProperty = new javax.swing.JLabel();
        jButtonProperty = new javax.swing.JButton();
        jPanel27 = new javax.swing.JPanel();
        jLabelAttribute = new javax.swing.JLabel();
        jButtonAttribute = new javax.swing.JButton();
        jPanel26 = new javax.swing.JPanel();
        jLabelKeyword = new javax.swing.JLabel();
        jButtonKeyword = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
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
        jComboBoxFontSize.addItem("9");
        jComboBoxFontSize.addItem("10");
        jComboBoxFontSize.addItem("11");
        jComboBoxFontSize.addItem("12");
        jComboBoxFontSize.addItem("13");
        jComboBoxFontSize.addItem("14");
        jComboBoxFontSize.addItem("15");
        jComboBoxFontSize.addItem("16");

        jPanel10 = new javax.swing.JPanel();
        jTextFieldTabSize = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
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
        jDialogProjectSwitches = new javax.swing.JDialog();
        jPanelProjectSwitches = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jButton7 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jDialogProjectProperties = new javax.swing.JDialog();
        jPanel9 = new javax.swing.JPanel();
        jButton14 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaProjectProperties = new javax.swing.JTextArea();
        jPanel22 = new javax.swing.JPanel();
        jButtonNew = new javax.swing.JButton();
        OpenButton = new javax.swing.JButton();
        SaveButton = new javax.swing.JButton();
        SaveButtonAll = new javax.swing.JButton();
        SaveAsButton = new javax.swing.JButton();
        jButtonClose = new javax.swing.JButton();
        jButtonCloseAll = new javax.swing.JButton();
        jButtonUndo = new javax.swing.JButton();
        jButtonRedo = new javax.swing.JButton();
        jButtonCommentSelection = new javax.swing.JButton();
        jButtonUncommentSelection = new javax.swing.JButton();
        jButtonLeftTab = new javax.swing.JButton();
        jButtonRightTab = new javax.swing.JButton();
        jButtonBracketCheck = new javax.swing.JButton();
        RebuildButton = new javax.swing.JButton();
        RunButton = new javax.swing.JButton();
        jButtonInsertSymbol = new javax.swing.JButton();
        jButtonInterpreter = new javax.swing.JButton();
        jButtonSwitchManager = new javax.swing.JButton();
        jButtonOption = new javax.swing.JButton();
        jTextFieldFind = new javax.swing.JTextField();
        jButtonFind = new javax.swing.JButton();
        jButtonReplace = new javax.swing.JButton();
        jTextFieldRowCol = new javax.swing.JTextField();
        jSplitPane3 = new javax.swing.JSplitPane();
        jSplitPane1 = new javax.swing.JSplitPane();
        jTabbedPaneLeft = new javax.swing.JTabbedPane();
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
        category4 = new DefaultMutableTreeNode("Objects    ");
        top.add(category4);
        category5 = new DefaultMutableTreeNode("Functions    ");
        top.add(category5);
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
        jPanelSearch = new javax.swing.JPanel();
        jPanelSearchProject = new javax.swing.JPanel();
        jTextFieldFindAll = new javax.swing.JTextField();
        jButtonSearchProject = new javax.swing.JButton();
        jPanelDefinition = new javax.swing.JPanel();
        jTextFieldDefinition = new javax.swing.JTextField();
        jButtonDefinition = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
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
        jMenuItemExtractStrings = new javax.swing.JMenuItem();
        jMenuItemTranslate = new javax.swing.JMenuItem();
        jMenuView = new javax.swing.JMenu();
        jCheckBoxOutput = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxJToolBar = new javax.swing.JCheckBoxMenuItem();
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
        jMenuProjectProperties = new javax.swing.JMenuItem();
        jMenuProjectSwitches = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        jMenuItemLastProject = new javax.swing.JMenuItem();
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
        jMenuItemConfigFile = new javax.swing.JMenuItem();
        jMenuItemSettings = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JSeparator();
        jMenuItemGC = new javax.swing.JMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuItemReadMe = new javax.swing.JMenuItem();
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
        jDialogAbout.setFocusCycleRoot(false);
        jDialogAbout.setModal(true);
        jDialogAbout.setResizable(false);
        jDialogAbout.getAccessibleContext().setAccessibleParent(this);
        jButton9.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_OK"));
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jPanel7.add(jButton9);

        jDialogAbout.getContentPane().add(jPanel7, java.awt.BorderLayout.SOUTH);

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/about.png")));
        jTabbedPane3.addTab("About", jLabel7);

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setFont(new java.awt.Font("MonoSpaced", 0, 11));
        jTextArea1.setRows(5);
        jTextArea1.setText("JIF, a java editor for Inform Version 3\nby Alessandro Schillaci\nhttp://www.slade.altervista.org/JIF/\n\nDevelopment: \n- Alessandro Schillaci\n- Luis Fernandez\n- Peter F. Piggott\n\nContributors:\nPaolo Lucchesi\nVincenzo Scarpa\nBaltasar Garc\u00eda Perez-Schofield\nChristof Menear\nGiles Boutel\nJavier San Jos\u00e9\nDavid Moreno\nEric Forgeot\nMax Kalus\nAdrien Saurat\nAlex V Flinsch\nDaryl McCullough\nGiancarlo Niccolai\nIgnazio di Napoli\nJoerg Rosenbauer\nMatteo De Simone\nTommaso Caldarola");
        jScrollPaneAbout.setViewportView(jTextArea1);

        jTabbedPane3.addTab("Credits", jScrollPaneAbout);

        jDialogAbout.getContentPane().add(jTabbedPane3, java.awt.BorderLayout.NORTH);

        jDialogEditFileIni.setTitle(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOG_CONFIGFILES_TITLE"));
        jDialogEditFileIni.setFont(new java.awt.Font("Arial", 0, 12));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
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

        jDialogEditFileIni.getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        jTextAreaConfig.setTabSize(4);
        jScrollPane4.setViewportView(jTextAreaConfig);

        jDialogEditFileIni.getContentPane().add(jScrollPane4, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.GridLayout(1, 0));

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel2.setText("jLabel2");
        jPanel2.add(jLabel2);

        jDialogEditFileIni.getContentPane().add(jPanel2, java.awt.BorderLayout.NORTH);

        jDialogSwitches.setTitle(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOG_SWITCHES_TITLE"));
        jDialogSwitches.setFont(new java.awt.Font("Arial", 0, 12));
        jDialogSwitches.setModal(true);
        jPanel11.setLayout(new java.awt.GridLayout(2, 0));

        jPanelSwitch1.setLayout(new java.awt.GridLayout(0, 4));

        jPanelSwitch1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelSwitch1.setFont(new java.awt.Font("Dialog", 0, 8));
        jPanel11.add(jPanelSwitch1);

        jPanelSwitch2.setLayout(new java.awt.GridLayout(0, 3));

        jPanelSwitch2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelSwitch2.setFont(new java.awt.Font("Dialog", 0, 8));
        jPanel11.add(jPanelSwitch2);

        jDialogSwitches.getContentPane().add(jPanel11, java.awt.BorderLayout.CENTER);

        jButton15.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_SAVE"));
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });

        jPanel4.add(jButton15);

        jButton4.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_CLOSE"));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jPanel4.add(jButton4);

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

        jDialogReplace.getContentPane().setLayout(new javax.swing.BoxLayout(jDialogReplace.getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        jDialogReplace.setTitle(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOGREPLACE_TITLE"));
        jDialogReplace.setModal(true);
        jLabel1.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOGREPLACE_FIND_LABEL"));
        jLabel1.setPreferredSize(new java.awt.Dimension(41, 17));
        jPanel12.add(jLabel1);

        jTextFieldReplaceFind.setMaximumSize(new java.awt.Dimension(111, 20));
        jTextFieldReplaceFind.setMinimumSize(new java.awt.Dimension(111, 20));
        jTextFieldReplaceFind.setPreferredSize(new java.awt.Dimension(111, 20));
        jPanel12.add(jTextFieldReplaceFind);

        jLabel6.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOGREPLACE_REPLACE_LABEL"));
        jPanel12.add(jLabel6);

        jTextFieldReplace.setMaximumSize(new java.awt.Dimension(111, 20));
        jTextFieldReplace.setMinimumSize(new java.awt.Dimension(111, 20));
        jTextFieldReplace.setPreferredSize(new java.awt.Dimension(111, 20));
        jPanel12.add(jTextFieldReplace);

        jDialogReplace.getContentPane().add(jPanel12);

        jButton11.setFont(new java.awt.Font("MS Sans Serif", 0, 12));
        jButton11.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOGREPLACE_BUTTON_FIND"));
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jPanel20.add(jButton11);

        jButton12.setFont(new java.awt.Font("MS Sans Serif", 0, 12));
        jButton12.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOGREPLACE_BUTTON_REPLACE"));
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        jPanel20.add(jButton12);

        jButton13.setFont(new java.awt.Font("MS Sans Serif", 0, 12));
        jButton13.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOGREPLACE_BUTTON_REPLACE_ALL"));
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        jPanel20.add(jButton13);

        jButton3.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_CLOSE"));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jPanel20.add(jButton3);

        jDialogReplace.getContentPane().add(jPanel20);

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
        jPanelGeneral.setLayout(new javax.swing.BoxLayout(jPanelGeneral, javax.swing.BoxLayout.X_AXIS));

        jPanelGeneral.setMinimumSize(new java.awt.Dimension(277, 800));
        jPanelGeneralOptions.setLayout(new java.awt.GridBagLayout());

        jPanelGeneralOptions.setBorder(javax.swing.BorderFactory.createTitledBorder("General"));
        jPanelGeneralOptions.setMinimumSize(new java.awt.Dimension(205, 410));
        jPanelGeneralOptions.setPreferredSize(new java.awt.Dimension(205, 410));
        jCheckBoxOpenLastFile.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_OPEN_LAST_OPEN_FILE"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        jPanelGeneralOptions.add(jCheckBoxOpenLastFile, gridBagConstraints);

        jCheckBoxCreateNewFile.setText(java.util.ResourceBundle.getBundle("JIF").getString("OPTION_CREATE_A_NEW_FILE"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        jPanelGeneralOptions.add(jCheckBoxCreateNewFile, gridBagConstraints);

        jCheckBoxMappingLive.setText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_MAPPINGLIVE"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        jPanelGeneralOptions.add(jCheckBoxMappingLive, gridBagConstraints);

        jCheckBoxHelpedCode.setSelected(true);
        jCheckBoxHelpedCode.setText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_HELPEDCODE"));
        jCheckBoxHelpedCode.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_HELPEDCODE_TOOLTIP"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        jPanelGeneralOptions.add(jCheckBoxHelpedCode, gridBagConstraints);

        jCheckBoxSyntax.setSelected(true);
        jCheckBoxSyntax.setText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_SYNTAX"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        jPanelGeneralOptions.add(jCheckBoxSyntax, gridBagConstraints);

        jCheckBoxNumberLines.setText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_NUMBEROFLINES"));
        jCheckBoxNumberLines.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxNumberLinesActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        jPanelGeneralOptions.add(jCheckBoxNumberLines, gridBagConstraints);

        jCheckBoxScanProjectFiles.setText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_SCAN_PROJECT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        jPanelGeneralOptions.add(jCheckBoxScanProjectFiles, gridBagConstraints);

        jCheckBoxWrapLines.setText("Wrap Lines");
        jCheckBoxWrapLines.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxWrapLinesActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        jPanelGeneralOptions.add(jCheckBoxWrapLines, gridBagConstraints);

        jCheckBoxProjectOpenAllFiles.setText(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_OPEN_ALL_FILES"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        jPanelGeneralOptions.add(jCheckBoxProjectOpenAllFiles, gridBagConstraints);

        jCheckBoxMakeResource.setText(java.util.ResourceBundle.getBundle("JIF").getString("GLULX_MAKE_RESOURCE_WHEN_BUILD_ALL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        jPanelGeneralOptions.add(jCheckBoxMakeResource, gridBagConstraints);

        jCheckBoxAdventInLib.setText(java.util.ResourceBundle.getBundle("JIF").getString("JOPTION_ADVENT_IN_LIB"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        jPanelGeneralOptions.add(jCheckBoxAdventInLib, gridBagConstraints);

        jPanelGeneral.add(jPanelGeneralOptions);

        jTabbedPaneOption.addTab("General", jPanelGeneral);

        jPanelPath.setLayout(new java.awt.GridBagLayout());

        jPanel16.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabel13.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOG_CONFIGPATH_LIBRARY"));
        jPanel16.add(jLabel13);

        jTextFieldPathLib.setPreferredSize(new java.awt.Dimension(280, 21));
        jPanel16.add(jTextFieldPathLib);

        jButtonLibraryPath.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_BROWSE"));
        jButtonLibraryPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLibraryPathActionPerformed(evt);
            }
        });

        jPanel16.add(jButtonLibraryPath);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelPath.add(jPanel16, gridBagConstraints);

        jPanel43.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabel18.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOG_CONFIGPATH_LIBRARY_SECONDARY1"));
        jPanel43.add(jLabel18);

        jTextFieldPathLibSecondary1.setPreferredSize(new java.awt.Dimension(280, 21));
        jPanel43.add(jTextFieldPathLibSecondary1);

        jButtonLibraryPath1.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_BROWSE"));
        jButtonLibraryPath1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLibraryPath1ActionPerformed(evt);
            }
        });

        jPanel43.add(jButtonLibraryPath1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelPath.add(jPanel43, gridBagConstraints);

        jPanel45.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabel20.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOG_CONFIGPATH_LIBRARY_SECONDARY2"));
        jPanel45.add(jLabel20);

        jTextFieldPathLibSecondary2.setPreferredSize(new java.awt.Dimension(280, 21));
        jPanel45.add(jTextFieldPathLibSecondary2);

        jButtonLibraryPath2.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_BROWSE"));
        jButtonLibraryPath2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLibraryPath2ActionPerformed(evt);
            }
        });

        jPanel45.add(jButtonLibraryPath2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelPath.add(jPanel45, gridBagConstraints);

        jPanel49.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabel21.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOG_CONFIGPATH_LIBRARY_SECONDARY3"));
        jPanel49.add(jLabel21);

        jTextFieldPathLibSecondary3.setPreferredSize(new java.awt.Dimension(280, 21));
        jPanel49.add(jTextFieldPathLibSecondary3);

        jButtonLibraryPath3.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_BROWSE"));
        jButtonLibraryPath3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLibraryPath3ActionPerformed(evt);
            }
        });

        jPanel49.add(jButtonLibraryPath3);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelPath.add(jPanel49, gridBagConstraints);

        jPanel17.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabel14.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOG_CONFIGPATH_ATPATH"));
        jPanel17.add(jLabel14);

        jTextFieldPathGames.setPreferredSize(new java.awt.Dimension(280, 21));
        jPanel17.add(jTextFieldPathGames);

        jButtonCompiledPath.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_BROWSE"));
        jButtonCompiledPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCompiledPathActionPerformed(evt);
            }
        });

        jPanel17.add(jButtonCompiledPath);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelPath.add(jPanel17, gridBagConstraints);

        jPanel18.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabel15.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOG_CONFIGPATH_COMPILERPATH"));
        jPanel18.add(jLabel15);

        jTextFieldPathCompiler.setPreferredSize(new java.awt.Dimension(280, 21));
        jPanel18.add(jTextFieldPathCompiler);

        jButtonCompilerPath.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_BROWSE"));
        jButtonCompilerPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCompilerPathActionPerformed(evt);
            }
        });

        jPanel18.add(jButtonCompilerPath);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelPath.add(jPanel18, gridBagConstraints);

        jPanel19.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabel16.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOG_CONFIGPATH_INTERPRETERPATH"));
        jPanel19.add(jLabel16);

        jTextFieldPathInterpreter.setPreferredSize(new java.awt.Dimension(280, 21));
        jPanel19.add(jTextFieldPathInterpreter);

        jButtonInterpreterPath.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_BROWSE"));
        jButtonInterpreterPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonInterpreterPathActionPerformed(evt);
            }
        });

        jPanel19.add(jButtonInterpreterPath);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelPath.add(jPanel19, gridBagConstraints);

        jPanel44.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel19.setText(java.util.ResourceBundle.getBundle("JIF").getString("JDIALOG_CONFIGPATH_GLULXINTERPRETERPATH"));
        jPanel44.add(jLabel19);

        jTextFieldPathGlulx.setPreferredSize(new java.awt.Dimension(280, 21));
        jPanel44.add(jTextFieldPathGlulx);

        jButtonGlulxPath.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_BROWSE"));
        jButtonGlulxPath.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jButtonGlulxPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGlulxPathActionPerformed(evt);
            }
        });

        jPanel44.add(jButtonGlulxPath);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelPath.add(jPanel44, gridBagConstraints);

        jPanel46.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabelBres.setText(java.util.ResourceBundle.getBundle("JIF").getString("GLULX_BRES_LOCATION"));
        jPanel46.add(jLabelBres);

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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelPath.add(jPanel46, gridBagConstraints);

        jPanel47.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabelBlc.setText(java.util.ResourceBundle.getBundle("JIF").getString("GLULX_BLC_LOCATION"));
        jPanel47.add(jLabelBlc);

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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanelPath.add(jPanel47, gridBagConstraints);

        jTabbedPaneOption.addTab("Path", jPanelPath);

        jPanelColor.setLayout(new java.awt.BorderLayout());

        jPanelColor.setBorder(javax.swing.BorderFactory.createTitledBorder("Color and Font"));
        jPanelColor.setMinimumSize(new java.awt.Dimension(277, 260));
        jPanelColor.setPreferredSize(new java.awt.Dimension(277, 260));
        jEditorPaneColor.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jEditorPaneColor.setEditable(false);
        jEditorPaneColor.setMaximumSize(new java.awt.Dimension(100, 100));
        jEditorPaneColor.setPreferredSize(new java.awt.Dimension(102, 12));
        jPanelColor.add(jEditorPaneColor, java.awt.BorderLayout.CENTER);

        jPanel14.setLayout(new java.awt.GridLayout(8, 0));

        jPanelDefaultDark.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabelDefaultDark.setText("Black setting");
        jPanelDefaultDark.add(jLabelDefaultDark);

        jButtonDefaultDark.setMaximumSize(new java.awt.Dimension(35, 15));
        jButtonDefaultDark.setMinimumSize(new java.awt.Dimension(35, 15));
        jButtonDefaultDark.setPreferredSize(new java.awt.Dimension(35, 15));
        jButtonDefaultDark.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDefaultDarkActionPerformed(evt);
            }
        });

        jPanelDefaultDark.add(jButtonDefaultDark);

        jPanel14.add(jPanelDefaultDark);

        jPanel32.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabelComment.setText("Comment");
        jPanel32.add(jLabelComment);

        jButtonComment.setMaximumSize(new java.awt.Dimension(35, 15));
        jButtonComment.setMinimumSize(new java.awt.Dimension(35, 15));
        jButtonComment.setPreferredSize(new java.awt.Dimension(35, 15));
        jButtonComment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCommentActionPerformed(evt);
            }
        });

        jPanel32.add(jButtonComment);

        jPanel14.add(jPanel32);

        jPanel31.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabelNormal.setText("Normal");
        jPanel31.add(jLabelNormal);

        jButtonNormal.setMaximumSize(new java.awt.Dimension(35, 15));
        jButtonNormal.setMinimumSize(new java.awt.Dimension(35, 15));
        jButtonNormal.setPreferredSize(new java.awt.Dimension(35, 15));
        jButtonNormal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNormalActionPerformed(evt);
            }
        });

        jPanel31.add(jButtonNormal);

        jPanel14.add(jPanel31);

        jPanel30.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabelVerb.setText("Verb");
        jPanel30.add(jLabelVerb);

        jButtonVerb.setMaximumSize(new java.awt.Dimension(35, 15));
        jButtonVerb.setMinimumSize(new java.awt.Dimension(35, 15));
        jButtonVerb.setPreferredSize(new java.awt.Dimension(35, 15));
        jButtonVerb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonVerbActionPerformed(evt);
            }
        });

        jPanel30.add(jButtonVerb);

        jPanel14.add(jPanel30);

        jPanel37.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabelBackground.setText("Background");
        jPanel37.add(jLabelBackground);

        jButtonBackground.setMaximumSize(new java.awt.Dimension(35, 15));
        jButtonBackground.setMinimumSize(new java.awt.Dimension(35, 15));
        jButtonBackground.setPreferredSize(new java.awt.Dimension(35, 15));
        jButtonBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBackgroundActionPerformed(evt);
            }
        });

        jPanel37.add(jButtonBackground);

        jPanel14.add(jPanel37);

        jPanel28.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabelProperty.setText("Property");
        jPanel28.add(jLabelProperty);

        jButtonProperty.setMaximumSize(new java.awt.Dimension(35, 15));
        jButtonProperty.setMinimumSize(new java.awt.Dimension(35, 15));
        jButtonProperty.setPreferredSize(new java.awt.Dimension(35, 15));
        jButtonProperty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPropertyActionPerformed(evt);
            }
        });

        jPanel28.add(jButtonProperty);

        jPanel14.add(jPanel28);

        jPanel27.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabelAttribute.setText("Attribute");
        jPanel27.add(jLabelAttribute);

        jButtonAttribute.setMaximumSize(new java.awt.Dimension(35, 15));
        jButtonAttribute.setMinimumSize(new java.awt.Dimension(35, 15));
        jButtonAttribute.setPreferredSize(new java.awt.Dimension(35, 15));
        jButtonAttribute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAttributeActionPerformed(evt);
            }
        });

        jPanel27.add(jButtonAttribute);

        jPanel14.add(jPanel27);

        jPanel26.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jLabelKeyword.setText("Keyword");
        jPanel26.add(jLabelKeyword);

        jButtonKeyword.setMaximumSize(new java.awt.Dimension(35, 15));
        jButtonKeyword.setMinimumSize(new java.awt.Dimension(35, 15));
        jButtonKeyword.setPreferredSize(new java.awt.Dimension(35, 15));
        jButtonKeyword.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonKeywordActionPerformed(evt);
            }
        });

        jPanel26.add(jButtonKeyword);

        jPanel14.add(jPanel26);

        jPanelColor.add(jPanel14, java.awt.BorderLayout.EAST);

        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jComboBoxFont.setMinimumSize(new java.awt.Dimension(100, 21));
        jComboBoxFont.setPreferredSize(new java.awt.Dimension(100, 21));
        jComboBoxFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxFontActionPerformed(evt);
            }
        });

        jPanel6.add(jComboBoxFont);

        jComboBoxFontSize.setMinimumSize(new java.awt.Dimension(100, 21));
        jComboBoxFontSize.setPreferredSize(new java.awt.Dimension(100, 21));
        jComboBoxFontSize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxFontSizeActionPerformed(evt);
            }
        });

        jPanel6.add(jComboBoxFontSize);

        jTextFieldTabSize.setColumns(2);
        jTextFieldTabSize.setText("4");
        jPanel10.add(jTextFieldTabSize);

        jLabel8.setText("TAB size");
        jPanel10.add(jLabel8);

        jPanel6.add(jPanel10);

        jPanelColor.add(jPanel6, java.awt.BorderLayout.SOUTH);

        jTabbedPaneOption.addTab("Font", jPanelColor);

        jDialogOption.getContentPane().add(jTabbedPaneOption, java.awt.BorderLayout.CENTER);

        jButton10.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_SAVE"));
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

        jPanelProjectSwitches.setLayout(new java.awt.GridLayout(0, 4));

        jDialogProjectSwitches.getContentPane().add(jPanelProjectSwitches, java.awt.BorderLayout.CENTER);

        jButton7.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_SAVE"));
        jPanel13.add(jButton7);

        jButton5.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_CLOSE"));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jPanel13.add(jButton5);

        jDialogProjectSwitches.getContentPane().add(jPanel13, java.awt.BorderLayout.SOUTH);

        jDialogProjectProperties.setTitle("Project Properties");
        jDialogProjectProperties.setModal(true);
        jButton14.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_SAVE"));
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        jPanel9.add(jButton14);

        jButton8.setText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_CLOSE"));
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jPanel9.add(jButton8);

        jDialogProjectProperties.getContentPane().add(jPanel9, java.awt.BorderLayout.SOUTH);

        jTextAreaProjectProperties.setColumns(20);
        jTextAreaProjectProperties.setRows(5);
        jScrollPane1.setViewportView(jTextAreaProjectProperties);

        jDialogProjectProperties.getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

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
        jToolBarCommon.setBorder(javax.swing.BorderFactory.createEtchedBorder());
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

        jToolBarCommon.add(jButtonCommentSelection);

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

        jToolBarCommon.add(jButtonUncommentSelection);

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

        jToolBarCommon.add(jButtonLeftTab);

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

        jToolBarCommon.add(jButtonRightTab);

        jButtonBracketCheck.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/check.png")));
        jButtonBracketCheck.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_CHECK_BRACKETS"));
        jButtonBracketCheck.setBorderPainted(false);
        jButtonBracketCheck.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonBracketCheck.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonBracketCheck.setPreferredSize(new java.awt.Dimension(29, 29));
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

        jToolBarCommon.add(jButtonBracketCheck);

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

        jToolBarCommon.add(RebuildButton);

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

        jToolBarCommon.add(RunButton);

        jButtonInsertSymbol.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/insertSymbol.png")));
        jButtonInsertSymbol.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_INSERT_SYMBOL"));
        jButtonInsertSymbol.setBorderPainted(false);
        jButtonInsertSymbol.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonInsertSymbol.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonInsertSymbol.setPreferredSize(new java.awt.Dimension(29, 29));
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

        jToolBarCommon.add(jButtonInsertSymbol);

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

        jToolBarCommon.add(jButtonInterpreter);

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

        jToolBarCommon.add(jButtonSwitchManager);

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

        jToolBarCommon.add(jButtonOption);

        jTextFieldFind.setColumns(15);
        jTextFieldFind.setFont(new java.awt.Font("Courier New", 0, 12));
        jTextFieldFind.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JTOOLBAR_SEARCH"));
        jTextFieldFind.setMaximumSize(new java.awt.Dimension(111, 20));
        jTextFieldFind.setMinimumSize(new java.awt.Dimension(10, 22));
        jTextFieldFind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFindActionPerformed(evt);
            }
        });

        jToolBarCommon.add(jTextFieldFind);

        jButtonFind.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/filefind.png")));
        jButtonFind.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_SEARCH_BUTTON"));
        jButtonFind.setBorder(javax.swing.BorderFactory.createEtchedBorder());
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

        jButtonReplace.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/find.png")));
        jButtonReplace.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_REPLACE"));
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

        jTextFieldRowCol.setEditable(false);
        jTextFieldRowCol.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFieldRowCol.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jTextFieldRowCol.setDisabledTextColor(new java.awt.Color(212, 208, 200));
        jTextFieldRowCol.setMaximumSize(new java.awt.Dimension(100, 34));
        jTextFieldRowCol.setMinimumSize(new java.awt.Dimension(50, 34));
        jTextFieldRowCol.setPreferredSize(new java.awt.Dimension(50, 20));
        jToolBarCommon.add(jTextFieldRowCol);

        jPanel22.add(jToolBarCommon, java.awt.BorderLayout.NORTH);

        getContentPane().add(jPanel22, java.awt.BorderLayout.NORTH);

        jSplitPane3.setDividerSize(3);
        jSplitPane3.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane3.setDoubleBuffered(true);
        jSplitPane1.setDividerSize(3);
        jSplitPane1.setDoubleBuffered(true);
        jSplitPane1.setMinimumSize(new java.awt.Dimension(180, 248));
        jSplitPane1.setPreferredSize(new java.awt.Dimension(180, 328));
        jTabbedPaneLeft.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        jTabbedPaneLeft.setFont(new java.awt.Font("Dialog", 0, 11));
        jPanelTreeControl.setLayout(new javax.swing.BoxLayout(jPanelTreeControl, javax.swing.BoxLayout.Y_AXIS));

        jScrollPane3.setBorder(null);
        jScrollPane3.setDoubleBuffered(true);
        jScrollPane3.setMinimumSize(new java.awt.Dimension(150, 200));
        jScrollPane3.setPreferredSize(new java.awt.Dimension(150, 300));
        jTree1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jTree1.setFont(new java.awt.Font("Courier New", 0, 12));
        jTree1.setMaximumSize(new java.awt.Dimension(0, 0));
        jTree1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jTree1MouseEntered(evt);
            }
        });
        jTree1.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
            public void treeCollapsed(javax.swing.event.TreeExpansionEvent evt) {
            }
            public void treeExpanded(javax.swing.event.TreeExpansionEvent evt) {
                jTree1TreeExpanded(evt);
            }
        });
        jTree1.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                jTree1ValueChanged(evt);
            }
        });

        jScrollPane3.setViewportView(jTree1);

        jPanelTreeControl.add(jScrollPane3);

        jTabbedPaneLeft.addTab("Tree", jPanelTreeControl);

        jPanelMainFile.setLayout(new javax.swing.BoxLayout(jPanelMainFile, javax.swing.BoxLayout.Y_AXIS));

        jScrollPaneProject.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Project", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11)));
        jScrollPaneProject.setFont(new java.awt.Font("Dialog", 0, 11));
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

        jLabelMainFile.setFont(new java.awt.Font("Dialog", 0, 11));
        jLabelMainFile.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelMainFile.setText("Main:");
        jLabelMainFile.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jPanelMainFile.add(jLabelMainFile);

        jTabbedPaneLeft.addTab("Project", jPanelMainFile);

        jPanelSearch.setLayout(new javax.swing.BoxLayout(jPanelSearch, javax.swing.BoxLayout.Y_AXIS));

        jPanelSearch.setMaximumSize(new java.awt.Dimension(1800, 220));
        jPanelSearch.setMinimumSize(new java.awt.Dimension(180, 220));
        jPanelSearch.setPreferredSize(new java.awt.Dimension(180, 220));
        jPanelSearchProject.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        jPanelSearchProject.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Search all project files", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11)));
        jPanelSearchProject.setMaximumSize(new java.awt.Dimension(180, 55));
        jPanelSearchProject.setMinimumSize(new java.awt.Dimension(180, 55));
        jPanelSearchProject.setPreferredSize(new java.awt.Dimension(180, 55));
        jTextFieldFindAll.setColumns(15);
        jTextFieldFindAll.setFont(new java.awt.Font("Courier New", 0, 12));
        jTextFieldFindAll.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JTOOLBAR_SEARCH"));
        jTextFieldFindAll.setMaximumSize(new java.awt.Dimension(111, 20));
        jTextFieldFindAll.setMinimumSize(new java.awt.Dimension(10, 22));
        jPanelSearchProject.add(jTextFieldFindAll);

        jButtonSearchProject.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/fileprojectfind.png")));
        jButtonSearchProject.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_SEARCHALL_BUTTON"));
        jButtonSearchProject.setBorder(javax.swing.BorderFactory.createEtchedBorder());
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

        jPanelSearchProject.add(jButtonSearchProject);

        jPanelSearch.add(jPanelSearchProject);

        jPanelDefinition.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        jPanelDefinition.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Search for Definition", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11)));
        jPanelDefinition.setMaximumSize(new java.awt.Dimension(180, 55));
        jPanelDefinition.setMinimumSize(new java.awt.Dimension(180, 55));
        jPanelDefinition.setPreferredSize(new java.awt.Dimension(180, 55));
        jTextFieldDefinition.setColumns(15);
        jTextFieldDefinition.setFont(new java.awt.Font("Courier New", 0, 12));
        jTextFieldDefinition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldDefinitionActionPerformed(evt);
            }
        });

        jPanelDefinition.add(jTextFieldDefinition);

        jButtonDefinition.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/filefind.png")));
        jButtonDefinition.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonDefinition.setBorderPainted(false);
        jButtonDefinition.setMaximumSize(new java.awt.Dimension(29, 29));
        jButtonDefinition.setMinimumSize(new java.awt.Dimension(29, 29));
        jButtonDefinition.setPreferredSize(new java.awt.Dimension(29, 29));
        jButtonDefinition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDefinitionActionPerformed(evt);
            }
        });
        jButtonDefinition.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jButtonMouseExited(evt);
            }
        });

        jPanelDefinition.add(jButtonDefinition);

        jPanelSearch.add(jPanelDefinition);

        jTabbedPaneLeft.addTab("Search", jPanelSearch);

        jSplitPane1.setLeftComponent(jTabbedPaneLeft);

        jPanel5.setLayout(new java.awt.BorderLayout());

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

        jPanel5.add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        jSplitPane1.setRightComponent(jPanel5);

        jSplitPane3.setTopComponent(jSplitPane1);

        jTabbedPane2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTabbedPane2.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        jTabbedPane2.setAutoscrolls(true);
        jTabbedPane2.setFont(new java.awt.Font("Dialog", 0, 11));
        jTabbedPane2.setMinimumSize(new java.awt.Dimension(31, 100));
        jTabbedPane2.setPreferredSize(new java.awt.Dimension(30, 150));
        jScrollPane2.setAutoscrolls(true);
        jTextAreaOutput.setEditable(false);
        jTextAreaOutput.setFont(new java.awt.Font("Tahoma", 0, 11));
        jTextAreaOutput.setTabSize(4);
        jTextAreaOutput.setAutoscrolls(false);
        jTextAreaOutput.setMinimumSize(new java.awt.Dimension(0, 45));
        jTextAreaOutput.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jTextAreaOutputMouseMoved(evt);
            }
        });
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

        jMenuItemClose.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
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
        jMenuItemSearch.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemSearch.setText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_SEARCH"));
        jMenuItemSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSearchActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemSearch);

        jMenuItemSearchAllFiles.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemSearchAllFiles.setFont(new java.awt.Font("Dialog", 0, 11));
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

        jMenuItemRightShift.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemRightShift.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_RIGHTSHIFT"));
        jMenuItemRightShift.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRightShiftActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemRightShift);

        jMenuItemLeftShift.setFont(new java.awt.Font("Dialog", 0, 11));
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
        jMenuItemSetBookmark.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemSetBookmark.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_SETBOOKMARK"));
        jMenuItemSetBookmark.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSetBookmarkActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemSetBookmark);

        jMenuItemNextBookmark.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0));
        jMenuItemNextBookmark.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemNextBookmark.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_NEXTBOOKMARK"));
        jMenuItemNextBookmark.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemNextBookmarkActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemNextBookmark);

        jMenuItemExtractStrings.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemExtractStrings.setText("Extract Strings");
        jMenuItemExtractStrings.setToolTipText("KEY JFRAME_EXTRACT_STRINGS : RB JIF");
        jMenuItemExtractStrings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExtractStringsActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemExtractStrings);

        jMenuItemTranslate.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemTranslate.setText("Translate Strings");
        jMenuItemTranslate.setToolTipText("KEY JFRAME_TRANSLATE : RB JIF");
        jMenuItemTranslate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemTranslateActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemTranslate);

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

        jMenuView.add(jCheckBoxJToolBar);

        jCheckBoxJTree.setFont(new java.awt.Font("Dialog", 0, 11));
        jCheckBoxJTree.setSelected(true);
        jCheckBoxJTree.setText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_JTREE"));
        jCheckBoxJTree.setToolTipText(java.util.ResourceBundle.getBundle("JIF").getString("CHECKBOX_JTREE_TOOLTIP"));
        jCheckBoxJTree.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxJTreeActionPerformed(evt);
            }
        });

        jMenuView.add(jCheckBoxJTree);

        jCheckBoxToggleFullscreen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F11, 0));
        jCheckBoxToggleFullscreen.setFont(new java.awt.Font("Dialog", 0, 11));
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

        jMenuProjectProperties.setText("Project Properties");
        jMenuProjectProperties.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuProjectPropertiesActionPerformed(evt);
            }
        });

        jMenuProject.add(jMenuProjectProperties);

        jMenuProjectSwitches.setText("Project Switches");
        jMenuProjectSwitches.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuProjectSwitchesActionPerformed(evt);
            }
        });

        jMenuProject.add(jMenuProjectSwitches);

        jMenuProject.add(jSeparator5);

        jMenuItemLastProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLastProjectActionPerformed(evt);
            }
        });

        jMenuProject.add(jMenuItemLastProject);

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
        jMenuItemConfigFile.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemConfigFile.setText("JIF Ini file");
        jMenuItemConfigFile.setToolTipText("Edit configuration file for JIF");
        jMenuItemConfigFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemConfigFileActionPerformed(evt);
            }
        });

        jMenuOptions.add(jMenuItemConfigFile);

        jMenuItemSettings.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemSettings.setText(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_SETTING"));
        jMenuItemSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSettingsActionPerformed(evt);
            }
        });

        jMenuOptions.add(jMenuItemSettings);

        jMenuOptions.add(jSeparator12);

        jMenuItemGC.setText("Garbage Collector");
        jMenuItemGC.setToolTipText("Free unused object from the memory");
        jMenuItemGC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemGCActionPerformed(evt);
            }
        });

        jMenuOptions.add(jMenuItemGC);

        jMenuBar1.add(jMenuOptions);

        jMenuHelp.setText(java.util.ResourceBundle.getBundle("JIF").getString("MENU_HELP"));
        jMenuHelp.setDelay(0);
        jMenuHelp.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemReadMe.setFont(new java.awt.Font("Dialog", 0, 11));
        jMenuItemReadMe.setText(java.util.ResourceBundle.getBundle("JIF").getString("README"));
        jMenuItemReadMe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemReadMeActionPerformed(evt);
            }
        });

        jMenuHelp.add(jMenuItemReadMe);

        About.setFont(new java.awt.Font("Dialog", 0, 11));
        About.setText(java.util.ResourceBundle.getBundle("JIF").getString("ABOUTJIF"));
        About.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AboutActionPerformed(evt);
            }
        });

        jMenuHelp.add(About);

        jMenuBar1.add(jMenuHelp);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        jDialogProjectSwitches.setVisible(false);
    }//GEN-LAST:event_jButton5ActionPerformed
    
    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        jDialogProjectProperties.setVisible(false);
    }//GEN-LAST:event_jButton8ActionPerformed
    
    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        // save the current project and reload the switches
        File file = new File(currentProject);
        try{
            FileOutputStream fos = new FileOutputStream(file);
            Writer out = new OutputStreamWriter( fos, Constants.fileFormat );
            out.write(jTextAreaProjectProperties.getText());
            out.flush();
            out.close();
            reloadProject(currentProject);
            JOptionPane.showMessageDialog(jDialogProjectProperties,java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE1"), java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE2") , JOptionPane.INFORMATION_MESSAGE);
        } catch(IOException e ){
            System.out.println(e.getMessage());
        }        
    }//GEN-LAST:event_jButton14ActionPerformed
    
    private void jMenuProjectSwitchesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuProjectSwitchesActionPerformed
        jDialogProjectSwitches.pack();
        jDialogProjectSwitches.setLocationRelativeTo(this);
        jDialogProjectSwitches.setVisible(true);
        jDialogProjectSwitches.setTitle("Project Switches");
    }//GEN-LAST:event_jMenuProjectSwitchesActionPerformed
    
    private void jMenuProjectPropertiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuProjectPropertiesActionPerformed
        // if project != null, open the jpf file for manual editing
        if (null != currentProject && !currentProject.equals(Constants.PROJECTEMPTY)) {
            try{
                editProjectProperties(currentProject);
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }//GEN-LAST:event_jMenuProjectPropertiesActionPerformed
    
    private void jMenuItemGCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemGCActionPerformed
        System.gc();
    }//GEN-LAST:event_jMenuItemGCActionPerformed
    
    private void jMenuItemLastProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLastProjectActionPerformed
        if (null != ((javax.swing.JMenuItem)evt.getSource()).getText() &&
                ((javax.swing.JMenuItem)evt.getSource()).getText().length()>0){
            openProject(lastProject);
        }
    }//GEN-LAST:event_jMenuItemLastProjectActionPerformed
    
    private void jTextAreaOutputMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextAreaOutputMouseMoved
        try{
            // Rescues the correct line
            el = jTextAreaOutput.getDocument().getDefaultRootElement();
            int ind = el.getElementIndex(jTextAreaOutput.viewToModel(evt.getPoint()));
            el = jTextAreaOutput.getDocument().getDefaultRootElement().getElement(ind);
            ultima = jTextAreaOutput.getText(el.getStartOffset(), el.getEndOffset()-el.getStartOffset());
            
            if (ultima.startsWith(Constants.TOKENCOMMENT) || ultima.startsWith(Constants.TOKENSEARCH) ){
                
                // In case of errors
                if (Utils.IgnoreCaseIndexOf(ultima,"error")!=-1){
                    hlighterOutputWarnings.removeHighlights(jTextAreaOutput);
                    hlighterOutputErrors.removeHighlights(jTextAreaOutput);
                    hlighterOutputErrors.highlightFromTo(jTextAreaOutput,el.getStartOffset(),el.getEndOffset() );
                }
                // In case of warnings
                else{
                    hlighterOutputErrors.removeHighlights(jTextAreaOutput);
                    hlighterOutputWarnings.removeHighlights(jTextAreaOutput);
                    hlighterOutputWarnings.highlightFromTo(jTextAreaOutput,el.getStartOffset(),el.getEndOffset() );
                }
            }
        } catch (BadLocationException e){
            e.printStackTrace();
        }
    }//GEN-LAST:event_jTextAreaOutputMouseMoved
    
    private void jTree1TreeExpanded(javax.swing.event.TreeExpansionEvent evt) {//GEN-FIRST:event_jTree1TreeExpanded
        CharBuffer cb = getCurrentJIFTextPane().getCharBuffer();
        Pattern patt;
        Matcher m;
        if (evt.getPath().equals(new TreePath(treeModel.getPathToRoot(category1)))){
            patt = Pattern.compile("\n+\\s*Global\\s+(\\w+)(\\s+|;)", Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
            m = patt.matcher(cb);
            refreshGlobals(patt,m);
        } else if (evt.getPath().equals(new TreePath(treeModel.getPathToRoot(category2)))){
            patt = Pattern.compile("\n*\\s*Constant\\s+(\\w+)(\\s+|;)", Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
            m = patt.matcher(cb);
            refreshConstants(patt,m);
        } else if (evt.getPath().equals(new TreePath(treeModel.getPathToRoot(category4)))){
            patt = Pattern.compile("\n+\\s*Object\\s+(->\\s+)*(\\w+)(\\s|;)", Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
            m = patt.matcher(cb);
            refreshObjects(patt,m);
        } else if (evt.getPath().equals(new TreePath(treeModel.getPathToRoot(category5)))){
            patt = Pattern.compile("\n+\\s*\\[\\s*(\\w+)(\\s|;)", Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
            m = patt.matcher(cb);
            refreshFunctions(patt,m);
        } else if (evt.getPath().equals(new TreePath(treeModel.getPathToRoot(category7)))){
            Vector classi_locali= new Vector();
            patt = Pattern.compile("\n+\\s*Class\\s+(\\w+)\\s", Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
            m = patt.matcher(cb);
            refreshClasses(patt,m,classi_locali);
        }
        evt = null;
        return;
    }//GEN-LAST:event_jTree1TreeExpanded
    
    private void jCheckBoxOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxOutputActionPerformed
        if (!jCheckBoxOutput.getState()) {
            jTabbedPane2.setVisible(false);
        }
        if (jCheckBoxOutput.getState()) {
            jSplitPane3.setBottomComponent(jTabbedPane2);
            jTabbedPane2.setVisible(true);
        }
    }//GEN-LAST:event_jCheckBoxOutputActionPerformed
    
    private void jMenuItemTranslateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemTranslateActionPerformed
        getCurrentJIFTextPane().InsertTranslate(new File(getCurrentFilename()+"_translate.txt"), new File(getCurrentFilename()+"_translated.inf"));
    }//GEN-LAST:event_jMenuItemTranslateActionPerformed
    
    private void jMenuItemExtractStringsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExtractStringsActionPerformed
        getCurrentJIFTextPane().ExtractTranslate(new File(getCurrentFilename()+"_translate.txt"));
    }//GEN-LAST:event_jMenuItemExtractStringsActionPerformed
    
    private void jButtonFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFindActionPerformed
        getCurrentJIFTextPane().findString(this);
    }//GEN-LAST:event_jButtonFindActionPerformed
    
    private void jButtonSearchProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSearchProjectActionPerformed
        String target = this.jTextFieldFindAll.getText();
        if(null!=target && !target.trim().equals("")){
            // if output window is hide, I'll show it
            if (!jCheckBoxOutput.getState()){
                jSplitPane3.setBottomComponent(jTabbedPane2);
                jTabbedPane2.setVisible(true);
            }
            this.searchAllFiles(target);
        }
    }//GEN-LAST:event_jButtonSearchProjectActionPerformed
    
    private void jButtonDefinitionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDefinitionActionPerformed
        if (!jTextFieldDefinition.getText().equals("")){
            checkTree(jTextFieldDefinition.getText());
        }
    }//GEN-LAST:event_jButtonDefinitionActionPerformed
    
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        jDialogReplace.setVisible(false);
    }//GEN-LAST:event_jButton3ActionPerformed
    
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
    
    private void jMenuItemSearchAllFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSearchAllFilesActionPerformed
        // se è presente una stringa uso quella altrimenti la prendo da quella selezionata
        // No: vince quella selezionata
        String target = null;
        if (null != jTextFieldFindAll.getText() && 
            null != getCurrentJIFTextPane().getSelectedText()){
            target = getCurrentJIFTextPane().getSelectedText();
        }
        
        if (null == target ){
            target = jTextFieldFindAll.getText();    
        }
        
        if (null==target || target.trim().equals("")){
            target = getCurrentJIFTextPane().getSelectedText();
        }
        if (null!=target && !target.trim().equals("")){
            // if output window is hide, I'll show it
            if (!jCheckBoxOutput.getState()){
                jSplitPane3.setBottomComponent(jTabbedPane2);
                jTabbedPane2.setVisible(true);
            }
            searchAllFiles(target);
        }
    }//GEN-LAST:event_jMenuItemSearchAllFilesActionPerformed
    
    private void jMenuItemSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSearchActionPerformed
        String selezione = getCurrentJIFTextPane().getSelectedText();
        if (selezione!=null){
            jTextFieldFind.setText(selezione);
        }
        getCurrentJIFTextPane().findString(this);
    }//GEN-LAST:event_jMenuItemSearchActionPerformed
    
    private void jCheckBoxNumberLinesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxNumberLinesActionPerformed
        if (jCheckBoxNumberLines.isSelected()){
            jCheckBoxWrapLines.setSelected(false);
        }
    }//GEN-LAST:event_jCheckBoxNumberLinesActionPerformed
    
    private void jMenuItemCut1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCut1ActionPerformed
        getCurrentJIFTextPane().cut();
    }//GEN-LAST:event_jMenuItemCut1ActionPerformed
    
    private void jCheckBoxWrapLinesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxWrapLinesActionPerformed
        if (jCheckBoxWrapLines.isSelected()){
            jCheckBoxNumberLines.setSelected(false);
        }
    }//GEN-LAST:event_jCheckBoxWrapLinesActionPerformed
    
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
            jCheckBoxJTree.setSelected(true);
        } else{
            jCheckBoxOutput.setSelected(false);
            jCheckBoxJToolBar.setSelected(false);
            jCheckBoxJTree.setSelected(false);
        }
        jCheckBoxOutputActionPerformed(evt);
        jCheckBoxJToolBarActionPerformed(evt);
        jCheckBoxJTreeActionPerformed(evt);
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
    
    private void jTextFieldDefinitionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldDefinitionActionPerformed
//        // Search for definition
//        if (!jTextFieldDefinition.getText().equals("")){
//            checkTree(jTextFieldDefinition.getText());
//        }
    }//GEN-LAST:event_jTextFieldDefinitionActionPerformed
    
    private void jButtonLibraryPath3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLibraryPath3ActionPerformed
        JFileChooser chooser = new JFileChooser(workingDir);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int_var = chooser.showOpenDialog(this);
        if(int_var == JFileChooser.CANCEL_OPTION) {
            return;
        } else {
            jTextFieldPathLibSecondary3.setText(chooser.getSelectedFile().getAbsolutePath());
            setLibrarypathsecondary3(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonLibraryPath3ActionPerformed
    
    private void jButtonLibraryPath2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLibraryPath2ActionPerformed
        JFileChooser chooser = new JFileChooser(workingDir);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int_var = chooser.showOpenDialog(this);
        if(int_var == JFileChooser.CANCEL_OPTION) {
            return;
        } else {
            jTextFieldPathLibSecondary2.setText(chooser.getSelectedFile().getAbsolutePath());
            setLibrarypathsecondary2(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonLibraryPath2ActionPerformed
    
    private void jMenuItemJumpToSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemJumpToSourceActionPerformed
        try{
            checkTree(getCurrentJIFTextPane().getCurrentWord());
        } catch (BadLocationException ble){
            System.out.println(ble);
        }
    }//GEN-LAST:event_jMenuItemJumpToSourceActionPerformed
    
    private void jTree1MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTree1MouseEntered
        refreshTreeIncremental();
    }//GEN-LAST:event_jTree1MouseEntered
    
    private void jButtonDefaultDarkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDefaultDarkActionPerformed
        // Dark settings
        colorKeyword = new Color(51,102,255);
        colorAttribute = new Color(204,0,51);
        colorProperty = new Color(204,204,204);
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
        saveProject(false);
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
        saveAll();
        if (jCheckBoxMakeResource.isSelected()){
            makeResources();    // make resources
        }
        rebuildAll();           // Compile ULX file
        makeBlb();              // Make BLB file
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
        saveAll();
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
        int_var = chooser.showOpenDialog(this);
        if(int_var == JFileChooser.CANCEL_OPTION) {
            return;
        } else {
            jTextFieldBlc.setText(chooser.getSelectedFile().getAbsolutePath());
            setBlcpath(chooser.getSelectedFile().getAbsolutePath());            
        }
    }//GEN-LAST:event_jButtonBlcActionPerformed
    
    private void jButtonBresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBresActionPerformed
        JFileChooser chooser = new JFileChooser(workingDir);
        int_var = chooser.showOpenDialog(this);
        if(int_var == JFileChooser.CANCEL_OPTION) {
            return;
        } else {
            jTextFieldBres.setText(chooser.getSelectedFile().getAbsolutePath());
            setBrespath(chooser.getSelectedFile().getAbsolutePath());            
        }
    }//GEN-LAST:event_jButtonBresActionPerformed
    
    private void jButtonGlulxPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGlulxPathActionPerformed
        JFileChooser chooser = new JFileChooser(workingDir);
        int_var = chooser.showOpenDialog(this);
        if(int_var == JFileChooser.CANCEL_OPTION) {
            return;
        } else {
            jTextFieldPathGlulx.setText(chooser.getSelectedFile().getAbsolutePath());
            setInterpreterglulxpath(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonGlulxPathActionPerformed
    
    private void jCheckBoxInformModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxInformModeActionPerformed
        if (jCheckBoxInformMode.getState()){
            setInformMode();
            jCheckBoxGlulxMode.setState(false);
            jCheckBoxInformMode.setState(true);
            setJifVersion("Jif "+ Constants.JIFVERSION + "     Inform Mode");
            refreshTree();
        }
    }//GEN-LAST:event_jCheckBoxInformModeActionPerformed
    
    private void jCheckBoxGlulxModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxGlulxModeActionPerformed
        if (jCheckBoxGlulxMode.getState()){
            setGlulxMode();
            jCheckBoxInformMode.setState(false);
            jCheckBoxGlulxMode.setState(true);
            setJifVersion("Jif "+ Constants.JIFVERSION + "     Glux Mode");
            refreshTree();
        }
    }//GEN-LAST:event_jCheckBoxGlulxModeActionPerformed
    
    private void jButtonLibraryPath1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLibraryPath1ActionPerformed
        JFileChooser chooser = new JFileChooser(workingDir);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int_var = chooser.showOpenDialog(this);
        if(int_var == JFileChooser.CANCEL_OPTION) {
            return;
        } else {
            jTextFieldPathLibSecondary1.setText(chooser.getSelectedFile().getAbsolutePath());
            setLibrarypathsecondary1(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonLibraryPath1ActionPerformed
    
    private void jMenuItemPopupCloseAllFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupCloseAllFilesActionPerformed
        closeAllFiles();
    }//GEN-LAST:event_jMenuItemPopupCloseAllFilesActionPerformed
    
    private void jButtonBracketCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBracketCheckActionPerformed
        getCurrentJIFTextPane().checkbrackets(this);
    }//GEN-LAST:event_jButtonBracketCheckActionPerformed
    
    private void jCheckBoxJTreeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxJTreeActionPerformed
        if (!jCheckBoxJTree.getState()) jSplitPane1.setDividerLocation(0);
        if (jCheckBoxJTree.getState()) jSplitPane1.setDividerLocation(180);
    }//GEN-LAST:event_jCheckBoxJTreeActionPerformed
    
    private void jButtonBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBackgroundActionPerformed
        Color temp = JColorChooser.showDialog(this, "Color Dialog", colorBackground);
        if (temp != null) colorBackground= temp;
        updateColor();
        updateColorEditor();
    }//GEN-LAST:event_jButtonBackgroundActionPerformed
    
    private void jMenuItemAddNewToProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddNewToProjectActionPerformed
        // Creates a new file and append this to the project
        newAdventure();
        saveAs();
        projectFiles.add(new FileProject(jFrame.getCurrentFilename()));
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
        saveProject(false);
    }//GEN-LAST:event_jMenuItemAddNewToProjectActionPerformed
    
    private void jMenuItemPopupAddNewToProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupAddNewToProjectActionPerformed
        // Creates a new file and append this to the project
        newAdventure();
        saveAs();
        projectFiles.add(new FileProject(jFrame.getCurrentFilename()));
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
        jTextFieldPathLib.setText(getLibrarypath());
        jTextFieldPathLibSecondary1.setText(getLibrarypathsecondary1());
        jTextFieldPathLibSecondary2.setText(getLibrarypathsecondary2());
        jTextFieldPathLibSecondary3.setText(getLibrarypathsecondary3());
        jTextFieldPathGames.setText(getCompiledpath());
        jTextFieldPathCompiler.setText(getCompilerpath());
        jTextFieldPathInterpreter.setText(getInterpreterzcodepath());
        jTextFieldPathGlulx.setText(getInterpreterglulxpath());
        jTextFieldBres.setText(getBrespath());
        jTextFieldBlc.setText(getBlcpath());
        
        // set the colors
        updateColor();
        updateColorEditor();
        Font tmpFont = defaultFont;
        jComboBoxFont.setSelectedItem(tmpFont.getName());
        jComboBoxFontSize.setSelectedItem(String.valueOf(tmpFont.getSize()));
        
        jDialogOption.pack();
        //jDialogOption.setSize(580,560);
        //jDialogOption.setSize(580,540);
        jDialogOption.setLocationRelativeTo(this);
        jDialogOption.setVisible(true);
    }//GEN-LAST:event_jButtonOptionActionPerformed
    
    private void jButton24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton24ActionPerformed
        jDialogTutorial.setVisible(false);
    }//GEN-LAST:event_jButton24ActionPerformed
    
    private void jComboBoxFontSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxFontSizeActionPerformed
        defaultFont = new Font((String)jComboBoxFont.getSelectedItem(),Font.PLAIN,Integer.parseInt((String)jComboBoxFontSize.getSelectedItem()));
        updateColor();
        updateColorEditor();
    }//GEN-LAST:event_jComboBoxFontSizeActionPerformed
    
    private void jComboBoxFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxFontActionPerformed
        defaultFont = new Font((String)jComboBoxFont.getSelectedItem(),Font.PLAIN,Integer.parseInt((String)jComboBoxFontSize.getSelectedItem()));
        updateColor();
        updateColorEditor();
    }//GEN-LAST:event_jComboBoxFontActionPerformed
    
    private void jButton23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton23ActionPerformed
        unquote();
        // Updates colors
        colorKeyword = new Color(29,59,150);
        colorAttribute = new Color(153,0,153);
        colorProperty = new Color(37,158,33);
        colorVerb = new Color(0,153,153);
        colorNormal = new Color(0,0,0);
        colorComment = new Color(153,153,153);
        colorBackground = new Color(255,255,255);
        updateColor();
        updateColorEditor();
        jComboBoxFont.setSelectedItem("Courier New");
        jComboBoxFontSize.setSelectedItem("12");
        jTextFieldTabSize.setText("4");
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
        Color temp = JColorChooser.showDialog(this, "Color Dialog", colorKeyword);
        if (temp != null) {
            colorKeyword = temp;
        }
        updateColor();
        updateColorEditor();
    }//GEN-LAST:event_jButtonKeywordActionPerformed
    
    private void jButtonSwitchManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSwitchManagerActionPerformed
        // if a project exists, Jif will launch the project properties dialog
        if (currentProject == null || currentProject.equals(Constants.PROJECTEMPTY)){
            jDialogSwitches.pack();
            jDialogSwitches.setLocationRelativeTo(this);
            jDialogSwitches.setVisible(true);
        } else{
            jDialogProjectSwitches.pack();
            jDialogProjectSwitches.setLocationRelativeTo(this);
            jDialogProjectSwitches.setVisible(true);
            jDialogProjectSwitches.setTitle("Project Switches");
        }
    }//GEN-LAST:event_jButtonSwitchManagerActionPerformed
    
    private void jMenuItemPopupOpenSelectedFilesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupOpenSelectedFilesActionPerformed
        Object[] oggetti = jListProject.getSelectedValues();
        if (oggetti.length == 0) return;
        for (int_var=0; int_var < oggetti.length ;int_var++){
            if (null != oggetti[int_var]){
                openFile( ((FileProject)oggetti[int_var]).path);
            }
        }
    }//GEN-LAST:event_jMenuItemPopupOpenSelectedFilesActionPerformed
    
    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
        saveConfigNew();
    }//GEN-LAST:event_jButton15ActionPerformed
    
    private void jListProjectMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListProjectMouseEntered
        // Just One click: shows the tooltip
        FileProject fp = (FileProject)jListProject.getSelectedValue();
        if (null!=fp){
            jListProject.setToolTipText(fp.path);
        } else {
            jListProject.setToolTipText(null);
        }
    }//GEN-LAST:event_jListProjectMouseEntered
    
    private void jButtonInterpreterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInterpreterActionPerformed
        runInterpreter();
    }//GEN-LAST:event_jButtonInterpreterActionPerformed
    
    private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton21ActionPerformed
        jDialogInfo.setVisible(false);
    }//GEN-LAST:event_jButton21ActionPerformed
    
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
    
    private void jMenuItemRemoveFromProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRemoveFromProjectActionPerformed
        removeFileFromProject();
    }//GEN-LAST:event_jMenuItemRemoveFromProjectActionPerformed
    
    private void jMenuItemPopupSaveProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupSaveProjectActionPerformed
        saveProject(true);
    }//GEN-LAST:event_jMenuItemPopupSaveProjectActionPerformed
    
    private void jMenuItemPopupCloseProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupCloseProjectActionPerformed
        closeProject();
    }//GEN-LAST:event_jMenuItemPopupCloseProjectActionPerformed
    
    private void jMenuItemPopupOpenProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupOpenProjectActionPerformed
        openProject(null);
    }//GEN-LAST:event_jMenuItemPopupOpenProjectActionPerformed
    
    private void jMenuItemPopupNewProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPopupNewProjectActionPerformed
        newProject();
    }//GEN-LAST:event_jMenuItemPopupNewProjectActionPerformed
    
    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        jDialogOption.setVisible(false);
        try{
            tabSize = Integer.parseInt(jTextFieldTabSize.getText());
        } catch(Exception e){
            tabSize = 4;
        }
        unquote();
        saveConfigNew();
    }//GEN-LAST:event_jButton10ActionPerformed
    
    private void jMenuItemSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSettingsActionPerformed
        jTextFieldPathLib.setText(getLibrarypath());
        jTextFieldPathLibSecondary1.setText(getLibrarypathsecondary1());
        jTextFieldPathLibSecondary2.setText(getLibrarypathsecondary2());
        jTextFieldPathLibSecondary3.setText(getLibrarypathsecondary3());
        jTextFieldPathGames.setText(getCompiledpath());
        jTextFieldPathCompiler.setText(getCompilerpath());
        jTextFieldPathInterpreter.setText(getInterpreterzcodepath());
        jTextFieldPathGlulx.setText(getInterpreterglulxpath());
        jTextFieldBres.setText(getBrespath());
        jTextFieldBlc.setText(getBlcpath());
        updateColor();
        updateColorEditor();
        Font tmpFont = defaultFont;
        jComboBoxFont.setSelectedItem(tmpFont.getName());
        jComboBoxFontSize.setSelectedItem(String.valueOf(tmpFont.getSize()));
        jDialogOption.pack();
        //jDialogOption.setSize(580,560);
        //jDialogOption.setSize(580,540);
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
        } else{
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
        saveProject(true);
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
        openProject(null);
    }//GEN-LAST:event_jMenuItemOpenProjectActionPerformed
    
    private void jMenuItemInsertSymbol1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemInsertSymbol1ActionPerformed
        showJWindowSymbol();
    }//GEN-LAST:event_jMenuItemInsertSymbol1ActionPerformed
    
    private void jButtonInsertSymbolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInsertSymbolActionPerformed
        showJWindowSymbol();
    }//GEN-LAST:event_jButtonInsertSymbolActionPerformed
    
    private void jMenuItemInsertSymbolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemInsertSymbolActionPerformed
        showJWindowSymbol();
    }//GEN-LAST:event_jMenuItemInsertSymbolActionPerformed
    
    private void jButtonNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNewActionPerformed
        newAdventure();
    }//GEN-LAST:event_jButtonNewActionPerformed
    
    private void jMenuItemUncommentSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemUncommentSelectionActionPerformed
        getCurrentJIFTextPane().unCommentSelection();
    }//GEN-LAST:event_jMenuItemUncommentSelectionActionPerformed
    
    private void jButtonUncommentSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonUncommentSelectionActionPerformed
        getCurrentJIFTextPane().unCommentSelection();
    }//GEN-LAST:event_jButtonUncommentSelectionActionPerformed
    
    private void jButtonCommentSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCommentSelectionActionPerformed
        getCurrentJIFTextPane().commentSelection();
    }//GEN-LAST:event_jButtonCommentSelectionActionPerformed
    
    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        // Replacing....only if there is a selected TEXT
        if (getCurrentJIFTextPane().getSelectedText()!= null){
            getCurrentJIFTextPane().replaceSelection(jTextFieldReplace.getText());
        }
    }//GEN-LAST:event_jButton12ActionPerformed
    
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
    
    private void jMenuItemCommentSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCommentSelectionActionPerformed
        getCurrentJIFTextPane().commentSelection();
    }//GEN-LAST:event_jMenuItemCommentSelectionActionPerformed
    
    private void jMenuItemInsertFromFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemInsertFromFileActionPerformed
        insertFromFile();
    }//GEN-LAST:event_jMenuItemInsertFromFileActionPerformed
    
    private void jMenuItemInsertFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemInsertFileActionPerformed
        insertFromFile();
    }//GEN-LAST:event_jMenuItemInsertFileActionPerformed
    
    private void jMenuItemPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPrintActionPerformed
        new Utils().printInform(this,"Jif print - "+getCurrentFilename(), getCurrentJIFTextPane());
    }//GEN-LAST:event_jMenuItemPrintActionPerformed
    
    private void jTree1ValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_jTree1ValueChanged
        if (getCurrentJIFTextPane() == null){
            clearTree();
            return;
        }
        
        getCurrentJIFTextPane().removeHighlighter();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)jTree1.getLastSelectedPathComponent();
        if (node == null || !(node.getUserObject() instanceof Inspect)){
            return;
        }
        Object nodo = node.getUserObject();
        try{
            Inspect insp = (Inspect) nodo;
            if (insp != null && insp.Iposition!=-1){
                JIFTextPane jif = getCurrentJIFTextPane();
                el = jif.getDocument().getDefaultRootElement();
                int_var = el.getElementIndex(insp.Iposition);
                el = jif.getDocument().getDefaultRootElement().getElement(int_var);
                jif.getHlighter().highlightFromTo(jif, el.getStartOffset() , el.getEndOffset());
                
                jif.scrollRectToVisible(jif.modelToView(jif.getDocument().getLength()));
                jif.scrollRectToVisible(jif.modelToView(insp.Iposition));
                jif.requestFocus();
                jif.setCaretPosition(insp.Iposition);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }//GEN-LAST:event_jTree1ValueChanged
    
    private void jTextAreaOutputMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextAreaOutputMouseClicked
        // When user clicks on errors/warnings, JIF jumps to the correct line in the source
        try{
            String nome="";
            int riga = 0;
            boolean found=false;
            
            // Rescues the correct line
            el = jTextAreaOutput.getDocument().getDefaultRootElement();
            int ind = el.getElementIndex(jTextAreaOutput.viewToModel(evt.getPoint()));
            el = jTextAreaOutput.getDocument().getDefaultRootElement().getElement(ind);
            ultima = jTextAreaOutput.getText(el.getStartOffset(), el.getEndOffset()-el.getStartOffset());
            
            // Only if the line starts with the "#" char
            if (ultima.indexOf(Constants.TOKENCOMMENT)!=-1 && ((ultima.indexOf(".inf")!=-1) || (ultima.indexOf(".h")!=-1))){
                
                // Errors in E1 format
                if(Utils.IgnoreCaseIndexOf(ultima,"line ")==-1){
                    // Removing all the selected text in the output window
                    hlighterOutputErrors.removeHighlights(jTextAreaOutput);
                    
                    StringTokenizer stok = new StringTokenizer(ultima,"#()");
                    nome=stok.nextToken();
                    riga=Integer.parseInt(stok.nextToken());
                    
                    // checks if the file exists
                    int selected = jTabbedPane1.getTabCount();
                    for (int_var=0; int_var < selected; int_var++){
                        if (nome.equals(getFilenameAt(int_var))){
                            found = true;
                            jTabbedPane1.setSelectedIndex(int_var);
                        }
                    }
                    
                    if (!found) {
                        synchronized (this) {
                            openFile(nome);
                        }
                    }
                    
                    JIFTextPane jif = getCurrentJIFTextPane();
                    
                    // Find the line with the error
                    el = jif.getDocument().getDefaultRootElement();
                    el = el.getElement(riga-1);
                    jif.setCaretPosition(el.getStartOffset());
                    
                    if (Utils.IgnoreCaseIndexOf(ultima,"warning")==-1){
                        jif.removeHighlighterErrors();
                        jif.getHlighterErrors().highlightFromTo(getCurrentJIFTextPane(),el.getStartOffset(),el.getEndOffset() );
                    } else{
                        jif.removeHighlighterWarnings();
                        jif.getHlighterWarnings().highlightFromTo(getCurrentJIFTextPane(),el.getStartOffset(),el.getEndOffset() );
                    }
                    
                    if (jif.modelToView(jif.getDocument().getLength()) != null){
                        jif.scrollRectToVisible(jif.modelToView(jif.getDocument().getLength()));
                        jif.scrollRectToVisible(jif.modelToView(el.getStartOffset()));
                    } else{
                        jif.setCaretPosition(el.getStartOffset());
                    }
                }
                // Errors in E0-E2 format
                else{
                    JOptionPane.showMessageDialog(this, "Please, use the -E1 error switch.", "Jump to Error", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            } else if (ultima.indexOf(Constants.TOKENCOMMENT)!=-1 && Utils.IgnoreCaseIndexOf(ultima,"file")==-1){
                JOptionPane.showMessageDialog(this, "Please, use the -E1 error switch.", "Jump to Error", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            // find string in all files function
            else if (ultima.startsWith(Constants.TOKENSEARCH)){
                // Removing all the selected text in the output window
                hlighterOutputErrors.removeHighlights(jTextAreaOutput);
                
                // Highlight the correct line
                hlighterOutputErrors.highlightFromTo(jTextAreaOutput,el.getStartOffset(),el.getEndOffset() );
                
                StringTokenizer stok = new StringTokenizer(ultima,Constants.TOKENSEARCH);
                nome=stok.nextToken();
                riga=Integer.parseInt(stok.nextToken());
                
                // checks if the file exists
                int selected = jTabbedPane1.getTabCount();
                for (int_var=0; int_var < selected; int_var++){
                    if (nome.equals(getFilenameAt(int_var))){
                        found = true;
                        jTabbedPane1.setSelectedIndex(int_var);
                    }
                }
                
                if (!found) {
                    synchronized (this) {
                        openFile(nome);
                    }
                }
                
                // Find the line with the error
                JIFTextPane jif = getCurrentJIFTextPane();
                el = jif.getDocument().getDefaultRootElement();
                el = el.getElement(riga-1);
                jif.setCaretPosition(el.getStartOffset());
                
                // Removing all the selected text
                jif.removeHighlighter();
                
                // Highlight the line which has product the error during compiling
                jif.getHlighter().highlightFromTo(jif,el.getStartOffset(),el.getEndOffset() );
                jif.scrollRectToVisible(jif.modelToView(jif.getDocument().getLength()));
                jif.scrollRectToVisible(jif.modelToView(el.getStartOffset()));
            } else return;
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
        getJMenuRecentFiles().removeAll();
        getRecentFiles().clear();
        saveConfigNew();
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
    
    private void jButtonInterpreterPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonInterpreterPathActionPerformed
        JFileChooser chooser = new JFileChooser(workingDir);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.CANCEL_OPTION) {
            return;
        } else {
            jTextFieldPathInterpreter.setText(chooser.getSelectedFile().getAbsolutePath());
            setInterpreterzcodepath(chooser.getSelectedFile().getAbsolutePath());            
        }
    }//GEN-LAST:event_jButtonInterpreterPathActionPerformed
    
    private void jButtonCompilerPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCompilerPathActionPerformed
        JFileChooser chooser = new JFileChooser(workingDir);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.CANCEL_OPTION) {
            return;
        } else {
            jTextFieldPathCompiler.setText(chooser.getSelectedFile().getAbsolutePath());
            setCompilerpath(chooser.getSelectedFile().getAbsolutePath());            
        }
    }//GEN-LAST:event_jButtonCompilerPathActionPerformed
    
    private void jButtonCompiledPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCompiledPathActionPerformed
        JFileChooser chooser = new JFileChooser(workingDir);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.CANCEL_OPTION) {
            return;
        } else {
            jTextFieldPathGames.setText(chooser.getSelectedFile().getAbsolutePath());
            setCompiledpath(chooser.getSelectedFile().getAbsolutePath());            
        }
    }//GEN-LAST:event_jButtonCompiledPathActionPerformed
    
    private void jButtonLibraryPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLibraryPathActionPerformed
        JFileChooser chooser = new JFileChooser(workingDir);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.CANCEL_OPTION) {
            return;
        } else {
            jTextFieldPathLib.setText(chooser.getSelectedFile().getAbsolutePath());
            setLibrarypath(chooser.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonLibraryPathActionPerformed
    
    private void jTextFieldFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFindActionPerformed
        getCurrentJIFTextPane().findString(this);
        evt=null;
    }//GEN-LAST:event_jTextFieldFindActionPerformed
    
    private void jMenuItemCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCloseActionPerformed
        closeFile();
        refreshTree();
        System.gc();
    }//GEN-LAST:event_jMenuItemCloseActionPerformed
    
    private void jMenuItemCopy1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCopy1ActionPerformed
        copyToClipBoard();
    }//GEN-LAST:event_jMenuItemCopy1ActionPerformed
    
    private void jMenuItemReadMeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemReadMeActionPerformed
        String filename = workingDir+Constants.SEP+"readme.txt";
        showFile(filename);
    }//GEN-LAST:event_jMenuItemReadMeActionPerformed
    
    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        jDialogText.setVisible(false);
        jTextArea4.setText("");
        jLabel5.setText("");
    }//GEN-LAST:event_jButton6ActionPerformed
    
    private void SaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveAsActionPerformed
        saveAs();
    }//GEN-LAST:event_SaveAsActionPerformed
    
    private void jMenuItemConfigFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemConfigFileActionPerformed
        try{
            editFileIni(getFileini());
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }//GEN-LAST:event_jMenuItemConfigFileActionPerformed
    
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        jDialogSwitches.setVisible(false);
    }//GEN-LAST:event_jButton4ActionPerformed
    
    private void jMenuItemSwitchesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSwitchesActionPerformed
        jDialogSwitches.pack();
        jDialogSwitches.setLocationRelativeTo(this);
        jDialogSwitches.setVisible(true);
    }//GEN-LAST:event_jMenuItemSwitchesActionPerformed
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // Saving file
        File file = new File(jLabel2.getText());
        try{
            FileOutputStream fos = new FileOutputStream(file);
            Writer out = new OutputStreamWriter( fos, Constants.fileFormat );
            out.write(jTextAreaConfig.getText());
            out.flush();
            out.close();
            //loadConfig();
            loadConfigNew(new File(fileini));
            JOptionPane.showMessageDialog(jDialogEditFileIni,java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE1"), java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE2") , JOptionPane.INFORMATION_MESSAGE);
        } catch(IOException e ){
            System.out.println(e.getMessage());
        }
    }//GEN-LAST:event_jButton1ActionPerformed
    
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        jDialogEditFileIni.setVisible(false);
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
    
    private void AboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AboutActionPerformed
        jDialogAbout.pack();
        jDialogAbout.setLocationRelativeTo(this);
        jDialogAbout.setVisible(true);
    }//GEN-LAST:event_AboutActionPerformed
    
    private void RunButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RunButtonActionPerformed
        saveAll();
        rebuildAll();
        runAdventure();
    }//GEN-LAST:event_RunButtonActionPerformed
    
    private void OpenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OpenButtonActionPerformed
        openFile();
    }//GEN-LAST:event_OpenButtonActionPerformed
    
    private void SaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveButtonActionPerformed
        saveFile();
    }//GEN-LAST:event_SaveButtonActionPerformed
    
    private void RebuildButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RebuildButtonActionPerformed
        saveAll();
        rebuildAll();
    }//GEN-LAST:event_RebuildButtonActionPerformed
    
    private void RunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_RunActionPerformed
        clearOutput();
        saveAll();
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
        saveAll();
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
            try {
                javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                //javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
                //javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
            } catch (Exception e) {
                System.out.println("Can't set look & feel:" + e);
            }
            //System.out.println("Setting Look and Feel: "+UIManager.getLookAndFeel().getName());
        } catch(Exception e){
            System.out.println("ERROR: "+e.getMessage());
        }
        new jFrame().setVisible(true);
    }
    
    /**
     * Search for a string in all files of project
     */
    public void searchAllFiles(String target){
        if (null != currentProject && !currentProject.equals(Constants.PROJECTEMPTY)) {
            StringBuffer output = new StringBuffer();
            File file = new File(currentProject);                       
            StringBuffer sb = new StringBuffer();
            String riga;
            sb.setLength(0);
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Constants.fileFormat));
                while ((riga = br.readLine())!=null){
                    sb.append(riga).append("\n");
                }
                br.close();
                Charset charset = Charset.forName(Constants.fileFormat);
                CharsetEncoder encoder = charset.newEncoder();
                CharsetDecoder decoder = charset.newDecoder();
                ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(sb.toString()));
                CharBuffer cb = decoder.decode(bbuf);

                // project files
                Pattern patt = Pattern.compile("\n\\[FILE\\]([^\n]+)");
                Matcher m = patt.matcher(cb);
                String result;
                while (m.find()){                
                    result = Utils.searchString(target, new File(m.group(1)));
                    if (null!=result){
                        output.append(result+"\n");
                    }                
                }
            } catch (Exception e) {}        
            jTextAreaOutput.setText(output.toString());
            jTextAreaOutput.setCaretPosition(0);
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
        try{
            // Replaces the "\n" chars with System.getProperty("line.separator")
            String tmp = Utils.replace(getCurrentJIFTextPane().getText(),"\n",System.getProperty("line.separator"));
            FileOutputStream fos = new FileOutputStream(file);
            Writer out = new OutputStreamWriter( fos, Constants.fileFormat );
            out.write(tmp);
            out.flush();
            out.close();
            
            StringBuffer strb=new StringBuffer(java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE3"));
            strb.append(getCurrentFilename());
            strb.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE4"));
            jTextAreaOutput.append(strb.toString());
            
            // rendo visibile la finestra di output
            jTabbedPane2.setSelectedComponent(jScrollPane2);
            
            setTitle(getJifVersion() +" - " + getCurrentFilename());
            
        } catch(IOException e){
            System.out.println("ERRORE: "+e.getMessage());
        }
        
    }
    
    
    public void saveAll(){
        // Remember the file selected
        Component comp = jTabbedPane1.getSelectedComponent();
        
        int componenti = jTabbedPane1.getTabCount();
        for (int_var=0; int_var < componenti; int_var++){
            jTabbedPane1.setSelectedIndex(int_var);
            if (getCurrentTitle().indexOf("*")!=-1)
                saveFile(); //Only save modified files
        }
        
        // reassign the selected component
        jTabbedPane1.setSelectedComponent(comp);
    }
    
    
    public void rebuildAll() {
        // Clearing the OutputWindow
        this.jTextAreaOutput.setText("");
        System.gc();
        String process_string[];
        Vector auxV=new Vector(6);
        String switchString[];
        // Check out if the compiler exists
        File test = new File(getCompilerpath());
        if (!test.exists()){
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER1")+getCompilerpath()+java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER2"),java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER3") , JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (null == getCompiledpath() || getCompiledpath().equals("")){
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("ERR_GAMESPATH"), java.util.ResourceBundle.getBundle("JIF").getString("ERR_GENERIC"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Show TextAreaOutput
        if (!jCheckBoxOutput.getState()){
            jSplitPane3.setBottomComponent(jTabbedPane2);
            jTabbedPane2.setVisible(true);
        }
        jTabbedPane2.setSelectedComponent(jScrollPane2);
        fileInf = getCurrentFilename();
        
        // Project compilation or normal?
        if (currentProject==null || currentProject.equals(Constants.PROJECTEMPTY)){
            makeSwitches();
            switchString = makeSwitches().split(" ");
        } else{
            makeSwitchesForProject();
            switchString = makeSwitchesForProject().split(" ");
        }
        
        String estensione = "";
        if (tipoz.equals("-v3")) estensione=".z3";
        if (tipoz.equals("-v4")) estensione=".z4";
        if (tipoz.equals("-v5")) estensione=".z5";
        if (tipoz.equals("-v6")) estensione=".z6";
        if (tipoz.equals("-v8")) estensione=".z8";
        
        // Check Mode: If this is Glulx Mode... extension is ULX
        if (jCheckBoxGlulxMode.isSelected()){
            estensione=".ulx";
        }
        
        // If compiling a project but there isn't a main file, it warning
        if (!currentProject.equals(Constants.PROJECTEMPTY)){
            if (mainFile != null && !mainFile.equals("")){
                jTextAreaOutput.append("Using main file "+mainFile+" to compiling...");
                fileInf = mainFile;
            } else{
                JOptionPane.showMessageDialog(this, "Set a Main file first.", "Warning" , JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        String fileOut = fileInf.substring(0,fileInf.lastIndexOf(".")) + estensione;
        jTextAreaOutput.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER1"));
        String lib;
        
        String dir = "";
        if(jCheckBoxAdventInLib.isSelected()){
            dir=fileInf.substring(0,fileInf.lastIndexOf(Constants.SEP))+",";
        }
        
        lib = dir+getLibrarypath();
        
        // Secondary 1-2-3 Library Path
        if (!getLibrarypathsecondary1().trim().equals("")){
            lib = lib+","+getLibrarypathsecondary1();
        }
        if (!getLibrarypathsecondary2().trim().equals("")){
            lib = lib+","+getLibrarypathsecondary2();
        }
        if (!getLibrarypathsecondary3().trim().equals("")){
            lib = lib+","+getLibrarypathsecondary3();
        }
        
        auxV.add(getCompilerpath());
        for(int i=1;i<switchString.length;i++) //i=1 to avoid the first " "
            auxV.add(switchString[i]);
        
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
            Process proc = rt.exec(process_string, null, new File(getCompiledpath()));
            String line="";
            BufferedReader br= new BufferedReader( new InputStreamReader( proc.getInputStream(), Constants.fileFormat));
            
            while ( (line = br.readLine() )!=null ){
                // in caso di errore o warning metto il cancelletto #
                if ( (line.indexOf("Error")!=-1) || (line.indexOf("error")!=-1)) {
                    jTextAreaOutput.append(Constants.TOKENCOMMENT+line+"\n");
                } else if ( (line.indexOf("Warning")!=-1) || (line.indexOf("warning")!=-1)) {
                    jTextAreaOutput.append(Constants.TOKENCOMMENT+line+"\n");
                } else jTextAreaOutput.append(line+"\n");
            }
            
            jTextAreaOutput.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER2"));
            jTextAreaOutput.append("\n");
            setTitle(getJifVersion() +" - " + getCurrentFilename());
            
        } catch(IOException e){
            System.out.println(e.getMessage());
        }
    }
    
    // Agggiunto il controllo sul MODE (Inform/Glulx)
    public void runAdventure() {
        
        String inter="";    // interpreterzcodepath
        if (jCheckBoxInformMode.isSelected()){
            inter = getInterpreterzcodepath();
        } else {
            inter = getInterpreterglulxpath();
        }
        
        // controllo che esista l'interprete con il path  inserito nella config.ini
        // se non esiste visualizzo un messaggio di warning
        File test = new File(inter);
        if (!test.exists()){
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER1")+inter+java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER2"), java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER3") , JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        //recupero l'attuale file name
        fileInf = getCurrentFilename();
        
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
            
            command[0]= new String(inter);
            command[1]= new String(fileInf.substring(0,fileInf.indexOf(".inf"))+estensione);
            
            jTextAreaOutput.append(command[0]+" "+command[1]+"\n");
            rt.exec(command);
            jTextAreaOutput.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER2"));
        } catch(IOException e){
            System.out.println(e.getMessage());
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
            MutableAttributeSet attr = new SimpleAttributeSet();
            String id = ((javax.swing.JMenuItem)e.getSource()).getText();
            try{
                //se non trovo nessun carattere "§" non vado a capo
                if ( ((String)getOperations().get((String)id)).indexOf("§")==-1 ){
                    // inserisco la stringa senza andare a capo
                    getCurrentDoc().insertString(getCurrentJIFTextPane().getCaretPosition(), (String)getOperations().get((String)id) , attr);
                } else{
                    st = new StringTokenizer((String)getOperations().get((String)id),"§");
                    while (st.hasMoreTokens()){
                        getCurrentDoc().insertString(getCurrentJIFTextPane().getCaretPosition(), st.nextToken()+"\n" , attr);
                    }
                }
            } catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        }
    }
    
    
    // funzione per l'apertura di file
    private void openFile(){
        JFileChooser chooser;
        if (lastDir!=null && !lastDir.equals("")){
            chooser  = new JFileChooser(lastDir);
        } else {
            chooser = new JFileChooser(getCompiledpath());
        }
        
        JifFileFilter infFilter = new JifFileFilter("inf", java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF7"));
        infFilter.addExtension("h");
        infFilter.addExtension("res");
        infFilter.addExtension("txt");
        chooser.setFileFilter(infFilter);
        chooser.setMultiSelectionEnabled(true);
        
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.CANCEL_OPTION) {
            fileInf = null;
            return;
        }
        
        File[] files = chooser.getSelectedFiles();
        
        // imposto lastDir se != null
        String tmp = files[0].getAbsolutePath();
        lastDir = tmp.substring(0,tmp.lastIndexOf(Constants.SEP));
        
        File file;
        for (int_var=0 ; int_var < files.length; int_var++){
            file = files[int_var];
            //Controllo che non sia stato già aperto un file
            if (checkOpenFile(file.getAbsoluteFile().toString())) return;
            JIFTextPane jtp;
            if (jCheckBoxWrapLines.isSelected()){
                jtp = new JIFTextPane(this, file);
            } else{
                jtp = new JIFTextPane(this, file){
                    private static final long serialVersionUID = 7492924940162258936L;
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
            jTabbedPane1.add(scroll, jtp.subPath);
            jTabbedPane1.setSelectedIndex(jTabbedPane1.getTabCount() - 1);
            
            appendLastFile(file.getAbsolutePath());
            
            fileInf = chooser.getSelectedFile().getAbsolutePath();
            // cursore sulla prima riga
            jtp.setCaretPosition(0);
            lastFile = file.getAbsolutePath();
            
        } // end for
        enableComponents();
        refreshTree();
    }
    
    public void openFile(String nomefile){
        
        File file = new File(nomefile);
        if (!file.exists()){
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE2")+nomefile+java.util.ResourceBundle.getBundle("JIF").getString("ERR_NAMEFILE3") , java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE1") , JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (checkOpenFile(file.getAbsoluteFile().toString())) return;
        
        JIFTextPane jtp;
        if (jCheckBoxWrapLines.isSelected()){
            jtp = new JIFTextPane(this, file);
        } else{
            jtp = new JIFTextPane(this, file){
                private static final long serialVersionUID = 1381807237210816003L;
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
        jTabbedPane1.add(scroll, jtp.subPath);
        jTabbedPane1.setSelectedComponent(scroll);
        
        appendLastFile(file.getAbsolutePath());
        
        jtp.setCaretPosition(0);
        enableComponents();
        refreshTree();
        lastFile = file.getAbsolutePath();
    }
    
    private void newAdventure(){
        fileInf = getCompiledpath()+Constants.SEP+java.util.ResourceBundle.getBundle("JIF").getString("MSG_NEWFILE3")+(countNewFile++)+".inf";
        JIFTextPane jtp;
        if (jCheckBoxWrapLines.isSelected()){
            jtp = new JIFTextPane(this, null);
        } else{
            jtp = new JIFTextPane(this, null){
                private static final long serialVersionUID = -7868710263636743719L;
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
        jTabbedPane1.add(scroll, jtp.subPath);
        jTabbedPane1.setSelectedIndex(jTabbedPane1.getTabCount() - 1);
        enableComponents();
        refreshTree();
    }
    
    private void clearOutput(){
        jTextAreaOutput.setText("");
    }
    
    public void saveConfigNew() {
        try{
            File file = new File(fileini);
            if (!(file.exists())){
                //System.out.println(java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE6"));
                System.out.println("Error opening "+file.getAbsolutePath());
                jTextAreaOutput.setText("Error opening "+file.getAbsolutePath());
                return;
            }
            
            StringBuffer output = new StringBuffer();
            output
                    .append("############################################################################\n")
                    .append("# Main Jif configuration file                                               \n")
                    .append("############################################################################\n")
                    
                    // ALTKEYS SECTION
                    .append("\n# [ALTKEYS] Section\n\n");
            for (Iterator it = getAltkeys().keySet().iterator(); it.hasNext();) {
                String key = (String) it.next();
                String value = (String)getAltkeys().get(key);
                output.append("[ALTKEYS]"+key+","+value+"\n");
            }
            for (Iterator it = getExecutecommands().keySet().iterator(); it.hasNext();) {
                String key = (String) it.next();
                String value = (String)getExecutecommands().get(key);
                output.append("[EXECUTE]"+key+","+value+"\n");
            }
            
            // HELPEDCODE SECTION
            output
                    .append("\n# [HELPEDCODE] Section\n\n")
                    .append("# [ret] = Return\n")
                    .append("# [tab] = Tab char\n")
                    .append("# @     = Cursor Position\n\n");
            for (Iterator it = getHelpcode().keySet().iterator(); it.hasNext();) {
                String key = (String) it.next();
                String value = (String)getHelpcode().get(key);
                output.append("[HELPEDCODE]"+key+","+value+"\n");
            }
            
            // MAPPING Section
            output
                    .append("\n# [MAPPING] Section\n\n");
            for (Iterator it = getMapping().keySet().iterator(); it.hasNext();) {
                String key = (String) it.next();
                String value = (String)getMapping().get(key);
                output.append("[MAPPING]"+key+","+value+"\n");
            }
            
            // MENU SECTION
            output
                    .append("\n# [MENU] Section\n\n");
            for(int i=0; i<getJMenuInsertNew().getMenuComponentCount();i++){
                JMenu jmenu = (JMenu) getJMenuInsertNew().getItem(i);
                output.append("\n[MENU]["+jmenu.getName()+"]*\n");
                for(int j=0; j<jmenu.getMenuComponentCount();j++){
                    JMenuItem jmenuitem = (JMenuItem) jmenu.getItem(j);
                    output.append("[MENU]["+jmenu.getName()+"]"+jmenuitem.getName()+","+getOperations().get(jmenuitem.getName())+"\n");
                }
            }
            
            // # [SWITCH] Section
            output.append("\n# [SWITCH] Section\n\n");
            for(int count=0; count < jPanelSwitch1.getComponentCount(); count++){
                Checkbox ch = (Checkbox) jPanelSwitch1.getComponent(count);
                if (ch.getState()){
                    output.append("[SWITCH]"+ch.getLabel()+",on\n");
                } else{
                    output.append("[SWITCH]"+ch.getLabel()+",off\n");
                }
            }
            for(int count=0; count < jPanelSwitch2.getComponentCount(); count++){
                Checkbox ch = (Checkbox) jPanelSwitch2.getComponent(count);
                if (ch.getState()){
                    output.append("[SWITCH]"+ch.getLabel()+",on\n");
                } else{
                    output.append("[SWITCH]"+ch.getLabel()+",off\n");
                }
            }
            
            // # [SYNTAX] Section
            output
                    .append("\n# [SYNTAX] Section\n\n");
            for (Iterator it = getAttributes().iterator(); it.hasNext();) {
                String key = (String) it.next();
                output.append("[SYNTAX][attribute]"+key+"\n");
            }
            for (Iterator it = getKeywords().iterator(); it.hasNext();) {
                String key = (String) it.next();
                output.append("[SYNTAX][keyword]"+key+"\n");
            }
            for (Iterator it = getProperties().iterator(); it.hasNext();) {
                String key = (String) it.next();
                output.append("[SYNTAX][property]"+key+"\n");
            }
            for (Iterator it = getVerbs().iterator(); it.hasNext();) {
                String key = (String) it.next();
                output.append("[SYNTAX][verb]"+key+"\n");
            }
            
            //# [SYMBOLS] Section
            output
                    .append("\n# [SYMBOLS] Section\n\n");
            for (Iterator it = symbols.iterator(); it.hasNext();) {
                String key = (String) it.next();
                output.append("[SYMBOLS]"+key+"\n");
            }
            
            // PATHS section
            output
                    .append("\n# [PATH] Section\n\n")
                    .append("[LIBRAYPATH]"+librarypath+"\n")
                    .append("[LIBRAYPATHSECONDARY1]"+librarypathsecondary1+"\n")
                    .append("[LIBRAYPATHSECONDARY2]"+librarypathsecondary2+"\n")
                    .append("[LIBRAYPATHSECONDARY3]"+librarypathsecondary3+"\n")
                    .append("[COMPILEDPATH]"+compiledpath+"\n")
                    .append("[INTERPRETERZCODEPATH]"+interpreterzcodepath+"\n")
                    .append("[INTERPRETERGLULXPATH]"+interpreterglulxpath+"\n")
                    .append("[COMPILERPATH]"+compilerpath+"\n")
                    .append("[BRESPATH]"+brespath+"\n")
                    .append("[BLCPATH]"+blcpath+"\n");
            
            // SETTINGS section
            output
                    .append("\n# [SETTINGS] Section\n\n")
                    .append("[WRAPLINES]"+jCheckBoxWrapLines.isSelected()+"\n")
                    .append("[SYNTAXCHECK]"+jCheckBoxSyntax.isSelected()+"\n")
                    .append("[HELPEDCODECHECK]"+jCheckBoxHelpedCode.isSelected()+"\n")
                    .append("[MAPPINGCODE]"+jCheckBoxMappingLive.isSelected()+"\n")
                    .append("[NUMBERLINES]"+jCheckBoxNumberLines.isSelected()+"\n")
                    .append("[PROJECTSCANFORCLASSES]"+jCheckBoxScanProjectFiles.isSelected()+"\n")
                    .append("[PROJECTOPENALLFILES]"+jCheckBoxProjectOpenAllFiles.isSelected()+"\n")
                    .append("[USECOMPILEDPATH]"+jCheckBoxAdventInLib.isSelected()+"\n")
                    .append("[OPENLASTFILE]"+jCheckBoxOpenLastFile.isSelected()+"\n")
                    .append("[CREATENEWFILE]"+jCheckBoxCreateNewFile.isSelected()+"\n")
                    .append("[MAKEALWAYSRESOURCE]"+jCheckBoxMakeResource.isSelected()+"\n")
                    .append("[TABSIZE]"+tabSize+"\n")
                    .append("[COLORKEYWORD]"+colorKeyword.getRed()+","+colorKeyword.getGreen()+","+colorKeyword.getBlue()+"\n")
                    .append("[COLORATTRIBUTE]"+colorAttribute.getRed()+","+colorAttribute.getGreen()+","+colorAttribute.getBlue()+"\n")
                    .append("[COLORPROPERTY]"+colorProperty.getRed()+","+colorProperty.getGreen()+","+colorProperty.getBlue()+"\n")
                    .append("[COLORVERB]"+colorVerb.getRed()+","+colorVerb.getGreen()+","+colorVerb.getBlue()+"\n")
                    .append("[COLORNORMAL]"+colorNormal.getRed()+","+colorNormal.getGreen()+","+colorNormal.getBlue()+"\n")
                    .append("[COLORCOMMENT]"+colorComment.getRed()+","+colorComment.getGreen()+","+colorComment.getBlue()+"\n")
                    .append("[COLORBACKGROUND]"+colorBackground.getRed()+","+colorBackground.getGreen()+","+colorBackground.getBlue()+"\n")
                    .append("[DEFAULTFONT]"+ defaultFont.getName()+","+defaultFont.getStyle()+","+defaultFont.getSize()+"\n")
                    .append("[LOCATIONX]"+ getX()+"\n")
                    .append("[LOCATIONY]"+ getY()+"\n")
                    .append("[WIDTH]"+ getWidth()+"\n")
                    .append("[HEIGHT]"+ getHeight()+"\n")
                    .append("[MODE]"+ (jCheckBoxInformMode.isSelected() ? "INFORM":"GLULX") +"\n")
                    .append("[OUTPUT]"+ jCheckBoxOutput.isSelected()+"\n")
                    .append("[JTOOLBAR]"+ jCheckBoxJToolBar.isSelected()+"\n")
                    .append("[JTREE]"+ jCheckBoxJTree.isSelected()+"\n")
                    .append("[DIVIDER1]"+ jSplitPane1.getDividerLocation()+"\n")
                    .append("[DIVIDER3]"+ jSplitPane3.getDividerLocation()+"\n")
                    .append("[LASTFILE]"+(lastFile!=null?lastFile:"")+"\n")
                    .append("[LASTPROJECT]"+(lastProject!=null?lastProject:"")+"\n");
            
            // Recent files section
            output.append("\n# [RECENTFILES] Section\n\n");
            for(int i=0; i<getJMenuRecentFiles().getMenuComponentCount();i++){
                JMenuItem jmenuitem = (JMenuItem) getJMenuRecentFiles().getItem(i);
                output.append("\n[RECENTFILES]"+jmenuitem.getName()+"\n");
            }
            
            //System.out.println(output.toString());
            FileOutputStream fos = new FileOutputStream(fileini);
            Writer out = new OutputStreamWriter( fos, Constants.fileFormat );
            out.write(output.toString());
            out.flush();
            out.close();
            jTextAreaOutput.setText(file.getAbsolutePath()+" saved.");
            
        } catch(Exception e){
            System.out.println("ERR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // New method
    public void loadConfigNew(File file) {
        try{
            if (!(file.exists())){
                System.out.println("Error opening "+file.getAbsolutePath());
                jTextAreaOutput.setText("Error opening "+file.getAbsolutePath());
                return;
            }
            
            // Initialize objects
            Hashtable altkeys         = new Hashtable();
            Hashtable executecommands = new Hashtable();
            Hashtable helpcode        = new Hashtable();
            Hashtable mapping         = new Hashtable();
            Hashtable operations      = new Hashtable();
            Hashtable switches        = new Hashtable();
            HashSet keywords          = new HashSet();
            HashSet attributes        = new HashSet();
            HashSet properties        = new HashSet();
            HashSet verbs             = new HashSet();
            HashSet keywords_cs       = new HashSet();
            HashSet attributes_cs     = new HashSet();
            HashSet properties_cs     = new HashSet();
            HashSet verbs_cs          = new HashSet();
            HashSet symbols           = new HashSet();
            HashSet recentFiles       = new HashSet();
            
            StringBuffer sb = new StringBuffer();
            String riga;
            sb.setLength(0);
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Constants.fileFormat));
            while ((riga = br.readLine())!=null){
                sb.append(riga).append("\n");
            }
            br.close();
            String configuration = sb.toString();
            Charset charset = Charset.forName(Constants.fileFormat);
            CharsetEncoder encoder = charset.newEncoder();
            CharsetDecoder decoder = charset.newDecoder();
            ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(configuration));
            CharBuffer cb = decoder.decode(bbuf);
            
            // Altkeys configuration
            Pattern patt = Pattern.compile("\n"+Constants.ALTKEYSTOKEN+"([^,]+),([^\n]+)");
            Matcher m = patt.matcher(cb);
            while (m.find()){
                altkeys.put(m.group(1),m.group(2));
            }
            
            // execute
            patt = Pattern.compile("\n"+Constants.EXECUTETOKEN+"([^,]+),([^\n]+)");
            m = patt.matcher(cb);
            while (m.find()){
                executecommands.put(m.group(1),m.group(2));
            }
            
            // helpedcode
            patt = Pattern.compile("\n"+Constants.HELPEDCODETOKEN+"([^,]+),([^\n]+)");
            m = patt.matcher(cb);
            while (m.find()){
                helpcode.put(m.group(1),m.group(2));
            }
            
            // mapping
            patt = Pattern.compile("\n"+Constants.MAPPINGTOKEN+"([^,]+),([^\n]+)");
            m = patt.matcher(cb);
            while (m.find()){
                mapping.put(m.group(1),m.group(2));
            }
            
            // Menu Insert New
            getJMenuInsertNew().removeAll();
            patt = Pattern.compile("\n"+Constants.MENUTOKEN+"\\[(.+)\\]\\*");
            m = patt.matcher(cb);
            Vector menues = new Vector();
            while (m.find()){
                menues.add(m.group(1));
            }
            // Add the sub-menues
            for (Iterator it = menues.iterator(); it.hasNext();) {
                String elem = (String) it.next();
                JMenu menu = new JMenu(elem);
                menu.setName(elem);
                patt = Pattern.compile("\n"+Constants.MENUTOKEN+"\\["+elem+"\\]([^,*]+),([^\n]+)");
                m = patt.matcher(cb);
                while (m.find()){
                    JMenuItem mi = new JMenuItem(m.group(1));
                    mi.setName(m.group(1));
                    menu.add(mi).addMouseListener(menuListener);
                    operations.put(m.group(1),m.group(2));
                }
                getJMenuInsertNew().add(menu);
            }
            
            
            // switches
            patt = Pattern.compile("\n"+Constants.SWITCHTOKEN+"([^,]+),([^\n]+)");
            m = patt.matcher(cb);
            getJPanelSwitch1().removeAll();
            getJPanelSwitch2().removeAll();
            Checkbox check;
            while (m.find()){
                switches.put(m.group(1),m.group(2));
                check = new Checkbox(m.group(1));
                check.setFont(new Font("Monospaced", Font.PLAIN, 11));
                check.setState(m.group(2).trim().equals("on") ? true : false);
                if (m.group(1).length()<4){
                    getJPanelSwitch1().add(check);
                } else{
                    getJPanelSwitch2().add(check);
                }
            }
            
            
            // Syntax - attribute
            patt = Pattern.compile("\n"+Constants.SYNTAXTOKEN+"\\[attribute\\](.+)");
            m = patt.matcher(cb);
            while (m.find()){
                attributes.add(m.group(1).toLowerCase());
                attributes_cs.add(m.group(1));
            }
            
            // Syntax - keyword
            patt = Pattern.compile("\n"+Constants.SYNTAXTOKEN+"\\[keyword\\](.+)");
            m = patt.matcher(cb);
            while (m.find()){
                keywords.add(m.group(1).toLowerCase());
                keywords_cs.add(m.group(1));
            }
            
            // Syntax - property
            patt = Pattern.compile("\n"+Constants.SYNTAXTOKEN+"\\[property\\](.+)");
            m = patt.matcher(cb);
            while (m.find()){
                properties.add(m.group(1).toLowerCase());
                properties_cs.add(m.group(1));
            }
            
            // Syntax - verb
            patt = Pattern.compile("\n"+Constants.SYNTAXTOKEN+"\\[verb\\](.+)");
            m = patt.matcher(cb);
            while (m.find()){
                verbs.add(m.group(1).toLowerCase());
                verbs_cs.add(m.group(1));
            }
            
            // Symbols
            patt = Pattern.compile("\n"+Constants.SYMBOLSTOKEN+"(.+)");
            m = patt.matcher(cb);
            Vector vettoresimboli = new Vector();
            while (m.find()){
                vettoresimboli.add(m.group(1));
                symbols.add(m.group(1));
            }
            
            
            // PATHS - LIBRAYPATH
            patt = Pattern.compile("\n"+Constants.LIBRAYPATHTOKEN+"(.+)");
            m = patt.matcher(cb);
            setLibrarypath("");
            while (m.find()){
                setLibrarypath(m.group(1).trim());
            }
            // PATHS - LIBRAYPATHSECONDARY1
            patt = Pattern.compile("\n"+Constants.LIBRAYPATHSECONDARY1TOKEN+"(.+)");
            m = patt.matcher(cb);
            setLibrarypathsecondary1("");
            while (m.find()){
                setLibrarypathsecondary1(m.group(1).trim());
            }
            // PATHS - LIBRAYPATHSECONDARY2
            patt = Pattern.compile("\n"+Constants.LIBRAYPATHSECONDARY2TOKEN+"(.+)");
            m = patt.matcher(cb);
            setLibrarypathsecondary2("");
            while (m.find()){
                setLibrarypathsecondary2(m.group(1).trim());
            }
            // PATHS - LIBRAYPATHSECONDARY3
            patt = Pattern.compile("\n"+Constants.LIBRAYPATHSECONDARY3TOKEN+"(.+)");
            m = patt.matcher(cb);
            setLibrarypathsecondary3("");
            while (m.find()){
                setLibrarypathsecondary3(m.group(1).trim());
            }
            // PATHS - COMPILEDPATH
            patt = Pattern.compile("\n"+Constants.COMPILEDPATHTOKEN+"(.+)");
            m = patt.matcher(cb);
            setCompiledpath("");
            while (m.find()){
                setCompiledpath(m.group(1).trim());
            }
            // PATHS - INTERPRETERZCODEPATH
            patt = Pattern.compile("\n"+Constants.INTERPRETERZCODEPATHTOKEN+"(.+)");
            m = patt.matcher(cb);
            setInterpreterzcodepath("");
            while (m.find()){
                setInterpreterzcodepath(m.group(1).trim());
            }
            // PATHS - INTERPRETERGLULXPATH
            patt = Pattern.compile("\n"+Constants.INTERPRETERGLULXPATHTOKEN+"(.+)");
            m = patt.matcher(cb);
            setInterpreterglulxpath("");
            while (m.find()){
                setInterpreterglulxpath(m.group(1).trim());
            }
            // PATHS - COMPILERPATH
            patt = Pattern.compile("\n"+Constants.COMPILERPATHTOKEN+"(.+)");
            m = patt.matcher(cb);
            setCompilerpath("");
            while (m.find()){
                setCompilerpath(m.group(1).trim());
            }
            // PATHS - BRESPATH
            patt = Pattern.compile("\n"+Constants.BRESPATHTOKEN+"(.+)");
            m = patt.matcher(cb);
            setBrespath("");
            while (m.find()){
                setBrespath(m.group(1).trim());
            }
            // PATHS - BLCPATH
            patt = Pattern.compile("\n"+Constants.BLCPATHTOKEN+"(.+)");
            m = patt.matcher(cb);
            setBlcpath("");
            while (m.find()){
                setBlcpath(m.group(1).trim());
            }
            
            
            // Recentfiles
            getJMenuRecentFiles().removeAll();
            recentFiles.clear();
            patt = Pattern.compile("\n"+Constants.RECENTFILESTOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                JMenuItem mi = new JMenuItem(m.group(1));
                mi.setName(m.group(1));
                mi.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        openFile(((javax.swing.JMenuItem)evt.getSource()).getText());
                    }
                });
                getJMenuRecentFiles().add(mi);
                recentFiles.add(m.group(1));
            }
            
            
            // Settings - WRAPLINES
            patt = Pattern.compile("\n"+Constants.WRAPLINESTOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                jCheckBoxWrapLines.setSelected(m.group(1).equals("true")?true:false);
            }
            // Settings - SYNTAXCHECK
            patt = Pattern.compile("\n"+Constants.SYNTAXCHECKTOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                jCheckBoxSyntax.setSelected(m.group(1).equals("true")?true:false);
            }
            // Settings - HELPEDCODECHECK
            patt = Pattern.compile("\n"+Constants.HELPEDCODECHECKTOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                jCheckBoxHelpedCode.setSelected(m.group(1).equals("true")?true:false);
            }
            // Settings - MAPPINGCODE
            patt = Pattern.compile("\n"+Constants.MAPPINGCODETOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                jCheckBoxMappingLive.setSelected(m.group(1).equals("true")?true:false);
            }
            // Settings - NUMBERLINES
            patt = Pattern.compile("\n"+Constants.NUMBERLINESTOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                jCheckBoxNumberLines.setSelected(m.group(1).equals("true")?true:false);
            }
            // Settings - PROJECTSCANFORCLASSES
            patt = Pattern.compile("\n"+Constants.PROJECTSCANFORCLASSESTOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                jCheckBoxScanProjectFiles.setSelected(m.group(1).equals("true")?true:false);
            }
            // Settings - PROJECTOPENALLFILES
            patt = Pattern.compile("\n"+Constants.PROJECTOPENALLFILESTOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                jCheckBoxProjectOpenAllFiles.setSelected(m.group(1).equals("true")?true:false);
            }
            // Settings - USECOMPILEDPATH
            patt = Pattern.compile("\n"+Constants.USECOMPILEDPATHTOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                jCheckBoxAdventInLib.setSelected(m.group(1).equals("true")?true:false);
            }
            // Settings - OPENLASTFILE
            patt = Pattern.compile("\n"+Constants.OPENLASTFILETOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                jCheckBoxOpenLastFile.setSelected(m.group(1).equals("true")?true:false);
            }
            // Settings - CREATENEWFILE
            patt = Pattern.compile("\n"+Constants.CREATENEWFILETOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                jCheckBoxCreateNewFile.setSelected(m.group(1).equals("true")?true:false);
            }
            // Settings - MAKEALWAYSRESOURCE
            patt = Pattern.compile("\n"+Constants.MAKEALWAYSRESOURCETOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                jCheckBoxMakeResource.setSelected(m.group(1).equals("true")?true:false);
            }
            // Settings - TABSIZE
            patt = Pattern.compile("\n"+Constants.TABSIZETOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                try{
                    tabSize = Integer.parseInt(m.group(1));
                } catch (Exception e){
                    tabSize = 4;
                }
                jTextFieldTabSize.setText(""+tabSize);
            }
            // Settings - Colors&Font
            patt = Pattern.compile("\n"+Constants.COLORKEYWORDTOKEN+"(.+),(.+),(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                colorKeyword = new Color(Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3)));
            }
            patt = Pattern.compile("\n"+Constants.COLORATTRIBUTETOKEN+"(.+),(.+),(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                colorAttribute = new Color(Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3)));
            }
            patt = Pattern.compile("\n"+Constants.COLORPROPERTYTOKEN+"(.+),(.+),(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                colorProperty = new Color(Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3)));
            }
            patt = Pattern.compile("\n"+Constants.COLORVERBTOKEN+"(.+),(.+),(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                colorVerb = new Color(Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3)));
            }
            patt = Pattern.compile("\n"+Constants.COLORNORMALTOKEN+"(.+),(.+),(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                colorNormal = new Color(Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3)));
            }
            patt = Pattern.compile("\n"+Constants.COLORCOMMENTTOKEN+"(.+),(.+),(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                colorComment = new Color(Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3)));
            }
            patt = Pattern.compile("\n"+Constants.COLORBACKGROUNDTOKEN+"(.+),(.+),(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                colorBackground = new Color(Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3)));
            }
            patt = Pattern.compile("\n"+Constants.DEFAULTFONTTOKEN+"(.+),(.+),(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                defaultFont = new Font(m.group(1),Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3)));
            }
            // Settings - LOCATIONX-Y-WIDTH-HEIGHT
            patt = Pattern.compile("\n"+Constants.LOCATIONXTOKEN+"(.+)");
            m = patt.matcher(cb);
            int x=0,y=0,width=0,height=0;
            while (m.find()){
                x = Integer.parseInt(m.group(1));
            }
            patt = Pattern.compile("\n"+Constants.LOCATIONYTOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                y = Integer.parseInt(m.group(1));
            }
            patt = Pattern.compile("\n"+Constants.WIDTHTOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                width = Integer.parseInt(m.group(1));
            }
            patt = Pattern.compile("\n"+Constants.HEIGHTTOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                height = Integer.parseInt(m.group(1));
            }
            if (width*height*x*y != 0){
                setSize(width, height);
                setLocation(x,y);
            } else{
                // first time JIF runs
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                setSize(screenSize.width-200, screenSize.height-140);
                setLocation(screenSize.width/2 - (getWidth()/2), screenSize.height/2 - (getHeight()/2));
            }
            patt = Pattern.compile("\n"+Constants.MODETOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                if(m.group(1).equalsIgnoreCase("inform")){
                    jCheckBoxInformMode.setState(true);
                } else{
                    jCheckBoxGlulxMode.setState(true);
                }
            }
            patt = Pattern.compile("\n"+Constants.OUTPUTTOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                jCheckBoxOutput.setSelected(m.group(1).equals("true")?true:false);
            }
            patt = Pattern.compile("\n"+Constants.JTOOLBARTOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                jCheckBoxJToolBar.setSelected(m.group(1).equals("true")?true:false);
            }
            patt = Pattern.compile("\n"+Constants.JTREETOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                jCheckBoxJTree.setSelected(m.group(1).equals("true")?true:false);
            }
            patt = Pattern.compile("\n"+Constants.DIVIDER1TOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                jSplitPane1.setDividerLocation(Integer.parseInt(m.group(1)));
            }
            patt = Pattern.compile("\n"+Constants.DIVIDER3TOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                jSplitPane3.setDividerLocation(Integer.parseInt(m.group(1)));
            }
            patt = Pattern.compile("\n"+Constants.LASTFILETOKEN+"([^\n]+)");
            m = patt.matcher(cb);
            while (m.find()){
                lastFile = m.group(1).trim();
            }
            patt = Pattern.compile("\n"+Constants.LASTPROJECTTOKEN+"([^\n]+)");
            m = patt.matcher(cb);
            while (m.find()){
                lastProject = m.group(1).trim();
                jMenuItemLastProject.setText( java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_OPEN") +" ("+
                        lastProject.substring(
                        lastProject.lastIndexOf(Constants.SEP)+1 ,
                        lastProject.length())+")");
            }
            
            // Set JIF configuration
            setAltkeys(altkeys);
            setExecutecommands(executecommands);
            setHelpcode(helpcode);
            setMapping(mapping);
            setOperations(operations);
            setSwitches(switches);
            setKeywords(keywords);
            setKeywords_cs(keywords_cs);
            setAttributes(attributes);
            setAttributes_cs(attributes_cs);
            setProperties(properties);
            setProperties_cs(properties_cs);
            setVerbs(verbs);
            setVerbs_cs(verbs_cs);
            getJListSymbols().setListData(vettoresimboli);
            setSymbols(symbols);
            setRecentFiles(recentFiles);
            jTextAreaOutput.setText("Configuration file ["+file.getAbsolutePath()+"] loaded.");
            
        } catch(Exception e){
            System.out.println("ERR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void checkTree(String key){
        key = key.toLowerCase();
        String file;
        file = checkDefinition(key);
        if (null == file){
            return;
        }
        
        JIFTextPane jif ;
        synchronized (this) {
            openFile(file);
            jif  = getCurrentJIFTextPane();
        }
        
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
                                    int pos = el.getElementIndex(ins.Iposition);
                                    el = jif.getDocument().getDefaultRootElement().getElement(pos);
                                    jif.getHlighter().highlightFromTo(jif, el.getStartOffset() , el.getEndOffset());
                                    
                                    jif.scrollRectToVisible(jif.modelToView(jif.getDocument().getLength()));
                                    jif.scrollRectToVisible(jif.modelToView(ins.Iposition));
                                }
                                
                            }catch(Exception e){
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                    
                } else{
                    Object nodo = node.getUserObject();
                    Inspect ins = (Inspect) nodo;
                    if ( ins.Ilabel.equals(key)  ){
                        try{
                            if (ins != null){
                                int pos = el.getElementIndex(ins.Iposition);
                                el = jif.getDocument().getDefaultRootElement().getElement(pos);
                                jif.getHlighter().highlightFromTo(jif, el.getStartOffset() , el.getEndOffset());
                                
                                jif.scrollRectToVisible(jif.modelToView(jif.getDocument().getLength()));
                                jif.scrollRectToVisible(jif.modelToView(ins.Iposition));
                            }
                            
                        }catch(Exception e){
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
        }
    }
    
    
    public void refreshTreeIncremental(){
        String currentName = getCurrentFilename();
        if (jTabbedPane1.getTabCount()==0 ||
                currentName.endsWith(".txt")  ||
                currentName.endsWith(".res")){
            return;
        }
        
        DefaultTreeModel treeModel = (DefaultTreeModel) jTree1.getModel();
        treePath1 = new TreePath(treeModel.getPathToRoot(category1));
        treePath2 = new TreePath(treeModel.getPathToRoot(category2));
        treePath4 = new TreePath(treeModel.getPathToRoot(category4));
        treePath5 = new TreePath(treeModel.getPathToRoot(category5));
        treePath7 = new TreePath(treeModel.getPathToRoot(category7));
        
        // Using the regexp
        CharBuffer cb = getCurrentJIFTextPane().getCharBuffer();
        objTree = new Vector();
        Pattern patt;
        Matcher m;
        
        // GLOBALS
        if(jTree1.isExpanded(treePath1) || category1.isLeaf()){
            patt = Pattern.compile("\n+\\s*Global\\s+(\\w+)(\\s+|;)", Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
            m = patt.matcher(cb);
            refreshGlobals(patt,m);
        }
        
        // CONSTANTS
        if(jTree1.isExpanded(treePath2) || category2.isLeaf()){
            patt = Pattern.compile("\n*\\s*Constant\\s+(\\w+)(\\s+|;)", Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
            m = patt.matcher(cb);
            refreshConstants(patt,m);
        }
        
        // OBJECTS
        if(jTree1.isExpanded(treePath4) || category4.isLeaf()){
            patt = Pattern.compile("\n+\\s*Object\\s+(->\\s+)*(\\w+)(\\s|;)", Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
            m = patt.matcher(cb);
            refreshObjects(patt,m);
        }
        
        // FUNCTIONS
        if(jTree1.isExpanded(treePath5) || category5.isLeaf()){
            patt = Pattern.compile("\n+\\s*\\[\\s*(\\w+)(\\s|;)", Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
            m = patt.matcher(cb);
            refreshFunctions(patt,m);
        }
        
        // CLASSES
        if(jTree1.isExpanded(treePath7) || category7.isLeaf()){
            Vector classi_locali= new Vector();
            patt = Pattern.compile("\n+\\s*Class\\s+(\\w+)\\s", Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
            m = patt.matcher(cb);
            refreshClasses(patt,m,classi_locali);
            
            // se ho impostato il flag jCheckBoxScanProjectFiles a true
            if (jCheckBoxScanProjectFiles.isSelected() && null != projectClass){
                //Aggiungo tutte le classi degli altri file del progetto che non sono contenute in classi_locali
                for (int i=0 ; i < projectClass.size(); i++){
                    // se la classe alla posizione i non c'è in classi_locali la aggiungo all'albero
                    String classe = (String) projectClass.get(i);
                    if (!classi_locali.contains((String) classe)){
                        tmp_nodo = new DefaultMutableTreeNode(new Inspect(classe.toLowerCase(),-1));
                        category7.add(tmp_nodo);
                        getClasses(tmp_nodo,classe);
                    }
                }
            }
        }
    }
    
    
    public void refreshGlobals(Pattern patt, Matcher m){
        objTree.clear();
        while (m.find()){
            objTree.add(new Inspect(m.group(1).toLowerCase(),m.start()+m.group(1).length()));
        }
        category1.removeAllChildren();
        sortNodes(objTree,category1);
        treeModel.reload(category1);
    }
    
    public void refreshConstants(Pattern patt, Matcher m){
        objTree.clear();
        while (m.find()){
            objTree.add(new Inspect(m.group(1).toLowerCase(),m.start()+m.group(1).length()));
        }
        category2.removeAllChildren();
        sortNodes(objTree,category2);
        treeModel.reload(category2);
    }
    
    public void refreshObjects(Pattern patt, Matcher m){
        objTree.clear();
        while (m.find()){
            objTree.add(new Inspect(m.group(2).toLowerCase(),m.start()+m.group(2).length()));
        }
        category4.removeAllChildren();
        sortNodes(objTree,category4);
        treeModel.reload(category4);
    }
    
    public void refreshFunctions(Pattern patt, Matcher m){
        objTree.clear();
        while (m.find()){
            objTree.add(new Inspect(m.group(1).toLowerCase(),m.start()+m.group(1).length()));
        }
        category5.removeAllChildren();
        sortNodes(objTree,category5);
        treeModel.reload(category5);
    }
    
    public void refreshClasses(Pattern patt, Matcher m, Vector classi_locali){
        category7.removeAllChildren();
        while (m.find()){
            classi_locali.add(m.group(1));
            tmp_nodo = new DefaultMutableTreeNode( new Inspect(m.group(1).toLowerCase(),m.start()+m.group(1).length()));
            category7.add(tmp_nodo);
            getClasses(tmp_nodo,m.group(1));
        }
    }
    
    public void clearTree(){
        category1.removeAllChildren();
        category2.removeAllChildren();
        category4.removeAllChildren();
        category5.removeAllChildren();
        category7.removeAllChildren();
        top.setUserObject("Inspect");
        treeModel.reload();
        jTextAreaOutput.setText("");
        disableComponents();
        this.setTitle(getJifVersion());
        jTree1.setEnabled(false);
    }
    
    
    // Modified to Use the Regular Expressions
    public void refreshTree(){
        // long tempo1=System.currentTimeMillis();
        
        // Reset the tree
        if (jTabbedPane1.getTabCount() == 0){
            clearTree();
            return;
        }
        
        String currentName = getCurrentFilename();
        setTitle(getJifVersion() +" - " + currentName);
        
        // is this an Inform file?
        if (currentName.endsWith(".txt")||currentName.endsWith(".res")){
            category1.removeAllChildren();
            category2.removeAllChildren();
            category4.removeAllChildren();
            category5.removeAllChildren();
            category7.removeAllChildren();
            top.setUserObject("Inspect");
            treeModel.reload();
            jTree1.setEnabled(false);
            return;
        }
        
        jTree1.setEnabled(true);
        
        category1.removeAllChildren();
        category2.removeAllChildren();
        category4.removeAllChildren();
        category5.removeAllChildren();
        category7.removeAllChildren();
        
        String nomefile = getCurrentFilename();
        top.setUserObject(nomefile.substring(nomefile.lastIndexOf(Constants.SEP)+1));
        treeModel.reload();
        CharBuffer cb = getCurrentJIFTextPane().getCharBuffer();
        objTree = new Vector();
        Pattern patt;
        Matcher m;
        
        // GLOBALS
        patt = Pattern.compile("\n+\\s*Global\\s+(\\w+)(\\s+|;)", Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
        m = patt.matcher(cb);
        refreshGlobals(patt,m);
        
        // CONSTANTS
        patt = Pattern.compile("\n*\\s*Constant\\s+(\\w+)(\\s+|;)", Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
        m = patt.matcher(cb);
        refreshConstants(patt,m);
        
        // OBJECTS
        patt = Pattern.compile("\n+\\s*Object\\s+(->\\s+)*(\\w+)(\\s|;)", Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
        m = patt.matcher(cb);
        refreshObjects(patt,m);
        
        // FUNCTIONS
        patt = Pattern.compile("\n+\\s*\\[\\s*(\\w+)(\\s|;)", Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
        m = patt.matcher(cb);
        refreshFunctions(patt,m);
        
        // CLASSES
        Vector classi_locali= new Vector();
        patt = Pattern.compile("\n+\\s*Class\\s+(\\w+)\\s", Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
        m = patt.matcher(cb);
        refreshClasses(patt,m,classi_locali);
        
        // se ho impostato il flag jCheckBoxScanProjectFiles a true
        if (jCheckBoxScanProjectFiles.isSelected() && null != projectClass){
            //Aggiungo tutte le classi degli altri file del progetto che non sono contenute in classi_locali
            for (int i=0 ; i < projectClass.size(); i++){
                // se la classe alla posizione i non c'è in classi_locali la aggiungo all'albero
                String classe = (String) projectClass.get(i);
                if (!classi_locali.contains((String) classe)){
                    tmp_nodo = new DefaultMutableTreeNode(new Inspect(classe.toLowerCase(),-1));
                    category7.add(tmp_nodo);
                    getClasses(tmp_nodo,classe);
                }
            }
        }
        
        expandAll(jTree1, true);
        //System.out.println("Tempo impiegato= "+(System.currentTimeMillis()-tempo1));
    }
    
    
    public void sortNodes(Vector vettore, DefaultMutableTreeNode nodo ){
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
    
    public void expandAll(JTree tree, boolean expand) {
        TreeNode root = (TreeNode)tree.getModel().getRoot();
        expandAll(tree, new TreePath(root), expand);
    }
    private void expandAll(JTree tree, TreePath parent, boolean expand) {
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }
    
    public void editProjectProperties(String filename){
        try{
            File file = new File(filename);
            if (!(file.exists())){
                System.out.println(java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE1")+filename);
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Constants.fileFormat));
            sb.setLength(0);
            while ((riga = br.readLine())!=null){
                sb.append(riga).append("\n");
            }
            jTextAreaProjectProperties.setText(sb.toString());
            br.close();
            jTextAreaProjectProperties.setCaretPosition(0);
            
            jDialogProjectProperties.setSize(500,500);
            jDialogProjectProperties.setLocationRelativeTo(this);
            jDialogProjectProperties.setVisible(true);
            
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        
    }
    
    
    public void editFileIni(String filename){
        try{
            File file = new File(filename);
            if (!(file.exists())){
                System.out.println(java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE1")+filename);
                return;
            }
            
            jDialogEditFileIni.setSize(600,550);
            jDialogEditFileIni.setLocationRelativeTo(this);
            jDialogEditFileIni.setVisible(true);
            
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Constants.fileFormat));
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
        }
    }
    
    public StringBuffer getSwitchesForSavingProject(){
        StringBuffer make = new StringBuffer();
        Checkbox ch;
        for(int count=0; count < getJPanelProjectSwitch().getComponentCount(); count++){
            ch = (Checkbox) getJPanelProjectSwitch().getComponent(count);
            if (ch.getState()){
                make.append("[SWITCH]"+ch.getLabel()+",on\n");
            } else{
                make.append("[SWITCH]"+ch.getLabel()+",off\n");
            }
        }
        return make;
    }
    
    public String makeSwitchesForProject(){
        StringBuffer make = new StringBuffer();
        Checkbox ch;
        for(int count=0; count < jPanelProjectSwitches.getComponentCount(); count++){
            ch = (Checkbox) jPanelProjectSwitches.getComponent(count);
            if (ch.getState()){
                // INFORM MODE
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
        
        // dopo il ciclo se nessuno swith di tipo -v2, -v3  -v8
        // è stato selezionato assumo per default lo swith -v5
        if (tipoz.equals("")) tipoz="-v5";
        
        // If in GLULX MODE, a "-G" switch is to be added
        if (jCheckBoxGlulxMode.isSelected()){
            make.append(" -G");
        }
        return make.toString();
    }
    
    // Make the string with switches to pass to the compiler
    public String makeSwitches(){
        // In questo metodo recupero il formato del file .z3,.z4,.z5,.z6,.z8
        StringBuffer make = new StringBuffer();
        Checkbox ch;
        for(int count=0; count < jPanelSwitch1.getComponentCount(); count++){
            ch = (Checkbox) jPanelSwitch1.getComponent(count);
            if (ch.getState()){
                // INFORM MODE
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
            saveConfigNew();
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
            saveConfigNew();
            System.exit(0);
        }
        
        String[] scelte = new String[3];
        scelte[0] = java.util.ResourceBundle.getBundle("JIF").getString("MSG_SAVE_AND_EXIT");
        scelte[1] = java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF10");
        scelte[2] = java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF11");
        int result = JOptionPane.showOptionDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF12") , java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF13") , 0 , JOptionPane.INFORMATION_MESSAGE , null , scelte , scelte[2]);
        
        if (result==0){
            // Save and Exit
            saveAll();
            saveConfigNew();
            saveProject(false);
            System.exit(0);
        }
        if (result==1){
            // Exit without save
            saveConfigNew();
            System.exit(0);
        }
    }
    
    // Per ogni classe nuova aggiungo al nodo passato, il nome degli oggetti di quella classe
    public void getClasses(DefaultMutableTreeNode nodo, String nome){
        
        // Using the regexp
        Charset charset = Charset.forName(Constants.fileFormat);
        CharsetEncoder encoder = charset.newEncoder();
        CharsetDecoder decoder = charset.newDecoder();
        String testo = getCurrentJIFTextPane().getText();
        ByteBuffer bbuf = null;
        CharBuffer cb = null;
        try {
            bbuf = encoder.encode(CharBuffer.wrap(testo));
            cb = decoder.decode(bbuf);
        } catch (Exception e){
            System.out.println("ERR:"+e.getMessage());
        }
        
        // Classes
        Pattern patt = Pattern.compile("\n+\\s*"+nome+"\\s+(->\\s+)*(\\w+)(\\s+|;)", Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
        Matcher m = patt.matcher(cb);
        while (m.find()){
            nodo.add(new DefaultMutableTreeNode(new Inspect(m.group(2).toLowerCase(),m.start()+m.group(2).length())));
        }
        treeModel.reload(nodo);
    }
    
    
    
    public static final JIFTextPane getCurrentJIFTextPane(){
        if (jTabbedPane1.getTabCount()==0){
            return null;
        } else{
            return (JIFTextPane)((JScrollPane)jTabbedPane1.getSelectedComponent()).getViewport().getComponent(0);
        }
    }
    
    public static final DefaultStyledDocument getCurrentDoc(){
        if (jTabbedPane1.getTabCount()==0){
            return null;
        } else{
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
        } else{
            aScrollPane=(JIFScrollPane)jTabbedPane1.getComponentAt(jTabbedPane1.getSelectedIndex());
            return(aScrollPane.getFile());
        }
    }
    
    public static final String getFilenameAt(int aTabNumber){
        JIFScrollPane aScrollPane;
        if (jTabbedPane1.getTabCount() == 0){
            return null;
        } else{
            aScrollPane=(JIFScrollPane)jTabbedPane1.getComponentAt(aTabNumber);
            return(aScrollPane.getFile());
            //return jTabbedPane1.getTitleAt(jTabbedPane1.getSelectedIndex());
        }
    }
    
    // append last opened file
    // Ignore a file is present
    public void appendLastFile(String recentfileToAppend){       
        if (getRecentFiles().contains(recentfileToAppend)){
            return;
        }        
        if (getJMenuRecentFiles().getMenuComponentCount() < Constants.MAXRECENTFILES){
            JMenuItem mi = new JMenuItem(recentfileToAppend);
            mi.setName(recentfileToAppend);
            mi.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    openFile(((javax.swing.JMenuItem)evt.getSource()).getText());
                }
            });
            getJMenuRecentFiles().add(mi);
            getRecentFiles().add(recentfileToAppend);
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
        jButtonCommentSelection.setEnabled(false);
        jButtonUncommentSelection.setEnabled(false);
        jButtonLeftTab.setEnabled(false);
        jButtonRightTab.setEnabled(false);
        jButtonBracketCheck.setEnabled(false);
        
        //menu
        jMenuEdit.setEnabled(false);
        jMenuBuild.setEnabled(false);
        jButtonInsertSymbol.setEnabled(false);
        SaveAsButton.setEnabled(false);
        jButtonClose.setEnabled(false);
        jButtonCloseAll.setEnabled(false);
        jButtonUndo.setEnabled(false);
        jButtonRedo.setEnabled(false);
        jMenuMode.setEnabled(false);
        jTree1.setEnabled(false);
        if(jCheckBoxGlulxMode.getState())
            jMenuGlulx.setEnabled(false);
        // search and definition
        jTextFieldFind.setEnabled(false);
        jTextFieldDefinition.setEnabled(false);
        jButtonReplace.setEnabled(false);
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
        jButtonCommentSelection.setEnabled(true);
        jButtonUncommentSelection.setEnabled(true);
        jButtonLeftTab.setEnabled(true);
        jButtonRightTab.setEnabled(true);
        jButtonBracketCheck.setEnabled(true);
        jMenuEdit.setEnabled(true);
        jMenuBuild.setEnabled(true);
        jButtonInsertSymbol.setEnabled(true);
        SaveAsButton.setEnabled(true);
        jButtonClose.setEnabled(true);
        jButtonCloseAll.setEnabled(true);
        jButtonUndo.setEnabled(true);
        jButtonRedo.setEnabled(true);
        jMenuMode.setEnabled(true);
        jTree1.setEnabled(true);
        if(jCheckBoxGlulxMode.getState())
            jMenuGlulx.setEnabled(true);
        jTextFieldFind.setEnabled(true);
        jTextFieldDefinition.setEnabled(true);
        jButtonReplace.setEnabled(true);
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
        }
    }
    
    
    
    public void insertFromFile(){
        try{
            JFileChooser chooser = new JFileChooser(insertnewdir);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnVal = chooser.showOpenDialog(this);
            if(returnVal == JFileChooser.CANCEL_OPTION) {
                return;
            }
            
            MutableAttributeSet attr = new SimpleAttributeSet();
            sb.setLength(0);
            String result = chooser.getSelectedFile().getAbsolutePath();
            
            //imposto la insertnewdir= a quella selezionata l'ultima volta
            insertnewdir = result.substring(0, result.lastIndexOf(Constants.SEP));
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(result)), Constants.fileFormat));
            while ((riga = br.readLine())!= null){
                sb.append(riga+"\n");
            }
            getCurrentDoc().insertString(getCurrentJIFTextPane().getCaretPosition(), sb.toString(), attr);
            
            // aggiorno albero
            refreshTree();
        } catch (Exception e){
            System.out.println(e.getMessage());
            
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
        } else{
            // chiudo senza chiedere
            Component comp = jTabbedPane1.getSelectedComponent();
            jTabbedPane1.remove(comp);
            return;
        }
    }
    
    
    
    // 1.cerco la prima occorrenza se non esiste esci
    // 2. replace string e vai al punto 1.
    public void replaceAll(){
        // Set Caret Position to ZERO
        getCurrentJIFTextPane().setCaretPosition(0);
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
            } else return false;
        } catch (BadLocationException e)  {
            System.out.println(e.getMessage());
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
                    MutableAttributeSet attr = new SimpleAttributeSet();
                    getCurrentDoc().insertString(getCurrentJIFTextPane().getCaretPosition(), id , attr);
                } catch(BadLocationException e){
                    System.out.println(e.getMessage());
                }
            }
        });
    }
    
    
    
    public void showJWindowSymbol(){
        try{
            int pointx = (int)getCurrentJIFTextPane().modelToView(getCurrentJIFTextPane().getCaretPosition()).getX();
            int pointy = (int)getCurrentJIFTextPane().modelToView(getCurrentJIFTextPane().getCaretPosition()).getY();
            JWindowSymbols.setLocation((int)getCurrentJIFTextPane().getLocationOnScreen().getX()+pointx, (int)getCurrentJIFTextPane().getLocationOnScreen().getY() +pointy+15);
            JWindowSymbols.setSize(230,200);
            JWindowSymbols.requestFocus();
            JWindowSymbols.setVisible(true);
        } catch(BadLocationException e){
            System.out.println(e.getMessage());
        }
    }
    
    
    public void saveAs(){
        // recupero il nuovo nome del file e lo salvo....
        // String result = JOptionPane.showInputDialog(this , java.util.ResourceBundle.getBundle("JIF").getString("MSG_NEWFILE1")+compiledpath, java.util.ResourceBundle.getBundle("JIF").getString("MSG_NEWFILE2"), JOptionPane.OK_CANCEL_OPTION);
        
        JFileChooser chooser;
        if (lastDir!=null && !lastDir.equals("")){
            chooser  = new JFileChooser(lastDir);
            chooser.setDialogTitle(java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_SAVEAS"));
            chooser.setApproveButtonText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_SAVE"));
        } else {
            chooser = new JFileChooser(this.getCompiledpath());
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
        //File file = new File(compiledpath+Constants.SEP+result);
        
        if (file.exists()){
            int overwrite = JOptionPane.showConfirmDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("ERR_NAMEFILE4"), java.util.ResourceBundle.getBundle("JIF").getString("ERR_NAMEFILE2") , JOptionPane.ERROR_MESSAGE);
            if (overwrite == 1) return;
        }
        
        //salvataggio
        //jTabbedPane1.setTitleAt( jTabbedPane1.getSelectedIndex(), compiledpath+Constants.SEP+result);
        //PROBLEMA
        jTabbedPane1.setTitleAt( jTabbedPane1.getSelectedIndex(), result);
        saveFile();
        
        // Refresh Tree
        refreshTree();
    }
    
    
    // funzione che gestisce l'inserimento di files in un progetto
    // supporta la seleziona multipla
    public void addFilesToProject(){
        JFileChooser chooser = new JFileChooser(getCompiledpath());
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
            file = files[i];
            // apro il file e lo aggiungo alla lista se il checkbox è attivo
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
        saveProject(false);
    }
    
    // The project inherits the default switches
    public void newProject(){
        try{
            JFileChooser chooser;
            chooser  = new JFileChooser();
            chooser.setDialogTitle(java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_NEW_PROJECT"));
            chooser.setApproveButtonText(java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_SAVE"));
            chooser.setMultiSelectionEnabled(false);
            int returnVal = chooser.showOpenDialog(this);
            if(returnVal == JFileChooser.CANCEL_OPTION) {
                return;
            }
            File file = chooser.getSelectedFile();
            
            if (file.getName().indexOf(".jpf")==-1){
                JOptionPane.showMessageDialog(
                        this,
                        "Project File must have a '.jpf' extension.",
                        "Error" ,
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (file.exists()){
                int res =  JOptionPane.showConfirmDialog(this,
                        java.util.ResourceBundle.getBundle("JIF").getString("PROJECT_PROJECT_EXISTS_OVERWRITE"),
                        file.getAbsolutePath() ,
                        JOptionPane.OK_CANCEL_OPTION);
                if (res==2){
                    return;
                }
                file.delete();
            }
            
            //file.createNewFile();
            currentProject = file.getAbsolutePath();            
            updateProjectTitle("Project: "+
                    currentProject.substring(
                    currentProject.lastIndexOf(Constants.SEP)+1
                    ,
                    currentProject.length())
                    );
            
            jScrollPaneProject.setEnabled(true);
            
            // Save the default switches from the config.ini
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileini), Constants.fileFormat));
            while ((riga = br.readLine())!=null){
                sb.append(riga).append("\n");
            }
            br.close();
            Charset charset = Charset.forName(Constants.fileFormat);
            CharsetEncoder encoder = charset.newEncoder();
            CharsetDecoder decoder = charset.newDecoder();
            ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(sb.toString()));
            CharBuffer cb = decoder.decode(bbuf);
            Pattern patt = Pattern.compile("\n"+Constants.SWITCHTOKEN+"([^,]+),([^\n]+)");
            Matcher m = patt.matcher(cb);
            getJPanelProjectSwitch().removeAll();
            Checkbox check;
            while (m.find()){
                switches.put(m.group(1),m.group(2));
                check = new Checkbox(m.group(1));
                check.setFont(new Font("Monospaced", Font.PLAIN, 11));
                check.setState(m.group(2).trim().equals("on") ? true : false);
                    getJPanelProjectSwitch().add(check);
            }            
            saveProject(true);
            
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
    
    
    // Enables components when a project is open or created
    public void enableProject(){
        jMenuItemSaveProject.setEnabled(true);
        jMenuItemCloseProject.setEnabled(true);
        jMenuItemAddNewToProject.setEnabled(true);
        jMenuItemAddFileToProject.setEnabled(true);
        jMenuItemRemoveFromProject.setEnabled(true);
        jMenuProjectProperties.setEnabled(true);
        jMenuProjectSwitches.setEnabled(true);
    }

    // Disables components when a project is open or created
    public void disableProject(){
        jMenuItemSaveProject.setEnabled(false);
        jMenuItemCloseProject.setEnabled(false);
        jMenuItemAddNewToProject.setEnabled(false);
        jMenuItemAddFileToProject.setEnabled(false);
        jMenuItemRemoveFromProject.setEnabled(false);
        jMenuProjectProperties.setEnabled(false);
        jMenuProjectSwitches.setEnabled(false);
    }
    
    
    public void updateProjectTitle(String title){
        TitledBorder tb = new TitledBorder(title);
        tb.setTitleFont(new Font("Dialog",Font.PLAIN,11));
        jScrollPaneProject.setBorder(tb);
    }
    
    // Reload just the project switchs
    public void reloadProject(String projectFile){
        File file = new File(projectFile);
        StringBuffer sb = new StringBuffer();
        String riga;
        sb.setLength(0);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Constants.fileFormat));
            while ((riga = br.readLine())!=null){
                sb.append(riga).append("\n");
            }
            br.close();
            Charset charset = Charset.forName(Constants.fileFormat);
            CharsetEncoder encoder = charset.newEncoder();
            CharsetDecoder decoder = charset.newDecoder();
            ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(sb.toString()));
            CharBuffer cb = decoder.decode(bbuf);
            
            // Load project switches
            Hashtable switches        = new Hashtable();
            Pattern patt = Pattern.compile("\n"+Constants.SWITCHTOKEN+"([^,]+),([^\n]+)");
            Matcher m = patt.matcher(cb);
            getJPanelProjectSwitch().removeAll();
            Checkbox check;
            while (m.find()){
                switches.put(m.group(1),m.group(2));
                check = new Checkbox(m.group(1));
                check.setFont(new Font("Monospaced", Font.PLAIN, 11));
                check.setState(m.group(2).trim().equals("on") ? true : false);
                getJPanelProjectSwitch().add(check);
            }
            setProjectSwitches(switches);
            
        } catch (Exception e){}
    }
    
    public void openProject(String projectFile){
        // Check for a poject opened
        if (currentProject!=null && !currentProject.equals(Constants.PROJECTEMPTY)){
            // Ask for saving before closing
            saveProject(false);
            closeProject();
        }
        
        projectClass = new Vector();
        File file;
        String auxFile;
        if (null == projectFile){
            JFileChooser chooser = new JFileChooser(lastOpenedProjectPath);
            JifFileFilter infFilter = new JifFileFilter("jpf", "Jif Project File");
            chooser.setFileFilter(infFilter);
            int returnVal = chooser.showOpenDialog(this);
            if(returnVal == JFileChooser.CANCEL_OPTION) {
                return;
            }
            file = new File(chooser.getSelectedFile().getAbsolutePath());
        } else{
            file = new File(projectFile);
        }
        
        // Loading the JPF file and use regular expressions to find config
        StringBuffer sb = new StringBuffer();
        String riga;
        sb.setLength(0);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Constants.fileFormat));
            while ((riga = br.readLine())!=null){
                sb.append(riga).append("\n");
            }
            br.close();
            Charset charset = Charset.forName(Constants.fileFormat);
            CharsetEncoder encoder = charset.newEncoder();
            CharsetDecoder decoder = charset.newDecoder();
            ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(sb.toString()));
            CharBuffer cb = decoder.decode(bbuf);
            Vector fileToOpen=new Vector();
            
            // project files
            Pattern patt = Pattern.compile("\n\\[FILE\\]([^\n]+)");
            Matcher m = patt.matcher(cb);
            while (m.find()){
                projectFiles.add(new FileProject(m.group(1)));
                if (jCheckBoxScanProjectFiles.isSelected()){
                    Utils.seekClass(projectClass, new File(m.group(1)));
                }
                fileToOpen.add(m.group(1));
            }
            
            // Find the Main class file
            patt = Pattern.compile("\n\\[MAINFILE\\]([^\n]+)");
            m = patt.matcher(cb);
            while (m.find()){
                mainFile = m.group(1);
                if (mainFile.equals("null")){
                    mainFile = null;
                    jLabelMainFile.setText("Main: "+mainFile);
                } else{
                    jLabelMainFile.setText("Main: " + mainFile.substring(mainFile.lastIndexOf(Constants.SEP)+1,mainFile.length()));
                }
            }
            
            // Set the Mode saved
            patt = Pattern.compile("\n"+Constants.MODETOKEN+"(.+)");
            m = patt.matcher(cb);
            while (m.find()){
                if(m.group(1).equalsIgnoreCase("inform")){
                    jCheckBoxInformMode.setState(true);
                    jCheckBoxGlulxMode.setState(false);
                } else{
                    jCheckBoxInformMode.setState(false);
                    jCheckBoxGlulxMode.setState(true);
                }
            }
            
            
            // Load project switches
            Hashtable switches        = new Hashtable();
            patt = Pattern.compile("\n"+Constants.SWITCHTOKEN+"([^,]+),([^\n]+)");
            m = patt.matcher(cb);
            getJPanelProjectSwitch().removeAll();
            Checkbox check;
            while (m.find()){
                switches.put(m.group(1),m.group(2));
                check = new Checkbox(m.group(1));
                check.setFont(new Font("Monospaced", Font.PLAIN, 11));
                check.setState(m.group(2).trim().equals("on") ? true : false);
                getJPanelProjectSwitch().add(check);
            }
            setProjectSwitches(switches);
            
            // ordino il vettore
            Collections.sort(projectFiles,new Comparator(){
                public int compare(Object a, Object b) {
                    String id1 = ((FileProject)a).toString();
                    String id2 = ((FileProject)b).toString();
                    return (id1).compareToIgnoreCase(id2) ;
                }
            });
            
            jListProject.setListData(projectFiles);
            
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
            
            currentProject = file.getAbsolutePath();
            
            updateProjectTitle("Project: "+
                    currentProject.substring(
                    currentProject.lastIndexOf(Constants.SEP)+1
                    ,
                    currentProject.length())
                    );
            
            jScrollPaneProject.setEnabled(true);
            
            // View the Project Panel
            jTabbedPaneLeft.setSelectedIndex(1);
            
            // Last Project closed
            lastProject = file.getAbsolutePath();
            lastOpenedProjectPath = file.getAbsolutePath();
            jMenuItemLastProject.setText(
                    java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_OPEN") +" ("+
                    currentProject.substring(
                    currentProject.lastIndexOf(Constants.SEP)+1 ,
                    currentProject.length())+")");
            
            enableProject();            
        } catch (Exception e) {}        
    }
    
    // Opening a project: the switches are overwritten by the project
    // settings
    public void openProjectOld(String projectFile){
        // creo un nuovo vettore per le classi
        projectClass = new Vector();
        File file;
        
        if (null == projectFile){
            JFileChooser chooser = new JFileChooser(lastOpenedProjectPath);
            JifFileFilter infFilter = new JifFileFilter("jpf", "Jif Project File");
            chooser.setFileFilter(infFilter);
            int returnVal = chooser.showOpenDialog(this);
            if(returnVal == JFileChooser.CANCEL_OPTION) {
                return;
            }
            file = new File(chooser.getSelectedFile().getAbsolutePath());
        } else{
            file = new File(projectFile);
        }
        
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Constants.fileFormat));
            projectFiles = new Vector();
            String idSwitch;
            String valueSwitch;
            String auxFile;
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
                        
                    }
                    if (riga.indexOf("[MAINFILE]=")!=-1){
                        mainFile = riga.substring(riga.indexOf("[MAINFILE]=")+11);
                        if (mainFile.equals("null")){
                            mainFile = null;
                            jLabelMainFile.setText("Main: "+mainFile);
                        } else{
                            jLabelMainFile.setText("Main: " + mainFile.substring(mainFile.lastIndexOf(Constants.SEP)+1,mainFile.length()));
                        }
                    }
                    
                    
                    // Load project switches
                    if (riga.indexOf("[SWITCH]=")!=-1){
                        st = new StringTokenizer(riga.substring(riga.indexOf("[SWITCH]=")+9) , ",");
                        idSwitch = st.nextToken();
                        valueSwitch = st.nextToken();
                        //System.out.println(idSwitch+"="+valueSwitch);
                        int numeroSwitch1=jPanelSwitch1.getComponentCount();
                        int numeroSwitch2=jPanelSwitch2.getComponentCount();
                        for(int count=0; count < numeroSwitch1; count++){
                            ch = (Checkbox) jPanelSwitch1.getComponent(count);
                            if (ch.getLabel().equals(idSwitch)){
                                ch.setState(valueSwitch.toLowerCase().equals("on")? true: false);
                            }
                        }
                        for(int count=0; count < numeroSwitch2; count++){
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
        }
        
        //currentProject = chooser.getSelectedFile().getAbsolutePath();
        currentProject = file.getAbsolutePath();
        
        updateProjectTitle("Project: "+
                currentProject.substring(
                currentProject.lastIndexOf(Constants.SEP)+1
                ,
                currentProject.length())
                );
        
        jScrollPaneProject.setEnabled(true);
        
        // View the Project Panel
        jTabbedPaneLeft.setSelectedIndex(1);
        
        // Last Project closed
        lastProject = file.getAbsolutePath();
        lastOpenedProjectPath = file.getAbsolutePath();
        jMenuItemLastProject.setText(
                java.util.ResourceBundle.getBundle("JIF").getString("MENUITEM_OPEN") +" ("+
                currentProject.substring(
                currentProject.lastIndexOf(Constants.SEP)+1 ,
                currentProject.length())+")");
        
    }
    
    // chiude un progetto. Inserire un flag per chiudere tutti i files
    // relativi ad un progetto quando si chiude il progetto stesso
    public void closeProject(){
        currentProject = Constants.PROJECTEMPTY;
        updateProjectTitle("Project: ");
        projectFiles.removeAllElements();
        Collections.sort(projectFiles,new Comparator(){
            public int compare(Object a, Object b) {
                String id1 = ((FileProject)a).toString();
                String id2 = ((FileProject)b).toString();
                return (id1).compareToIgnoreCase(id2) ;
            }
        });
        jListProject.setListData(projectFiles);
        jScrollPaneProject.setEnabled(false);
        closeAllFiles();
        loadConfigNew(new File(fileini));
        projectClass = null;
        mainFile = null;
        jLabelMainFile.setText("Main:");
    }
    
    
    public void saveProject(boolean message){
        try{
            if (currentProject.equals(Constants.PROJECTEMPTY)){
                return;
            }
            File file = new File(currentProject);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            Writer out = new OutputStreamWriter( fos, Constants.fileFormat );

            out.write("# Jif Configuration Project\n");
            out.write("# "+currentProject+"\n");
            out.write("\n");
            for (int i=0; i<projectFiles.size();i++)  {
                out.write("[FILE]" + ((FileProject)projectFiles.elementAt(i)).path+"\n");
            }
            out.write("[MAINFILE]"+mainFile+"\n");
            out.write("[MODE]"+ (jCheckBoxInformMode.isSelected() ? "INFORM":"GLULX") +"\n");
            out.write("\n");
            
            // The project Switches
            out.write("# WARNING! You can edit *only* this switches section!!!\n");
            StringBuffer make = getSwitchesForSavingProject();
            out.write(make.toString());
            out.write("# WARNING! You can edit *only* this switches section!!!\n");
            out.flush();
            out.close();
        }catch(Exception e ){
            System.out.println(e.getMessage());
        }
        
        // silent save?
        if (message){
            JOptionPane.showMessageDialog(
                    jDialogEditFileIni,
                    currentProject +" " +java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE4"),
                    java.util.ResourceBundle.getBundle("JIF").getString("OK_SAVE2") ,
                    JOptionPane.INFORMATION_MESSAGE);
        }
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
            saveProject(false);
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
    
    // Lancia l'interprete senza passargli il file AT (.inf)
    // This method has to be splitted in 2
    public void runInterpreter() {
        if (jCheckBoxInformMode.isSelected()){
            runInformInterpreter();
        } else runGlulxInterpreter();
    }
    
    public void runGlulxInterpreter(){
        // Check out if a glulx interpreter exists
        String auxGlux[]=new String[1];
        File test = new File(getInterpreterglulxpath());
        if (!test.exists()){
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER1")+getInterpreterzcodepath()+java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER2"), java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER3") , JOptionPane.ERROR_MESSAGE);
            return;
        }
        try{
            auxGlux[0]=new String(getInterpreterglulxpath());
            Runtime rt = Runtime.getRuntime();
            rt.exec(auxGlux);
        } catch(IOException e){
            System.out.println(e.getMessage());
        }
    }
    
    
    
    public void runInformInterpreter(){
        // controllo che esista l'interprete con il path  inserito nella config.ini
        // se non esiste visualizzo un messaggio di warning
        String auxInter[]=new String[1];
//        File test = new File(interpreterzcodepath); unused
//        if (!test.exists()){
//            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER1")+interpreterzcodepath+java.util.ResourceBundle.getBundle("JIF").getString("ERR_INTERPRETER2"), java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER3") , JOptionPane.ERROR_MESSAGE);
//            return;
//        }
        try{
            auxInter[0]=new String(getInterpreterzcodepath());
            Runtime rt = Runtime.getRuntime();
            rt.exec(auxInter); //Process proc =  unused
        } catch(IOException e){
            System.out.println(e.getMessage());
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
        jDialogTutorial.setTitle(filename);
        jDialogTutorial.setSize(700,550);
        jDialogTutorial.setLocationRelativeTo(this);
        jDialogTutorial.setVisible(true);
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Constants.fileFormat));
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
        }
    }
    
    
    
    public void updateColor(){
        jLabelKeyword.setForeground(colorKeyword);
        jButtonKeyword.setBackground(colorKeyword);
        jLabelAttribute.setForeground(colorAttribute);
        jButtonAttribute.setBackground(colorAttribute);
        jLabelProperty.setForeground(colorProperty);
        jButtonProperty.setBackground(colorProperty);
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
        jEditorPaneColor.setFont(defaultFont);
        jEditorPaneColor.setDoubleBuffered(false);
        jEditorPaneColor.setEditorKit(new StyledEditorKit());
        jEditorPaneColor.setEditable(false);
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
                .append("has   light;\n");
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
        String compiler = jTextFieldPathCompiler.getText();
        String games = jTextFieldPathGames.getText();
        String interpreter = jTextFieldPathInterpreter.getText();
        String lib = jTextFieldPathLib.getText();
        String libsec1 = jTextFieldPathLibSecondary1.getText();
        String libsec2 = jTextFieldPathLibSecondary2.getText();
        String libsec3 = jTextFieldPathLibSecondary3.getText();
        
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
        jCheckBoxProjectOpenAllFiles.setSelected(false);
        jCheckBoxHelpedCode.setSelected(true);
        jCheckBoxNumberLines.setSelected(true);
        jCheckBoxScanProjectFiles.setSelected(true);
        jCheckBoxSyntax.setSelected(true);
        jCheckBoxWrapLines.setSelected(false);
    }
    
    
    // run BLC SOURCE.blc source.blb to make blb (GLULX MODE)
    public void makeBlb() {//AQUI!!
        
        // controllo che esista il compilatore con il path  inserito nella config.ini
        // se non esiste visualizzo un messaggio di warning
        File test = new File(getBlcpath());
        if (!test.exists()){
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER1")+getCompilerpath()+java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER2"),java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER3") , JOptionPane.ERROR_MESSAGE);
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
        } else
            fileInf = getCurrentFilename(); //jTabbedPane1.getTitleAt( jTabbedPane1.getSelectedIndex());
        
        // Source file name
        String source = fileInf.substring(0,fileInf.lastIndexOf("."));
        String pathForCd=fileInf.substring(0,fileInf.lastIndexOf(Constants.SEP));
        //jTextAreaOutput.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER1"));
        String process_string[]=new String[3];
        
        process_string[0] = getBlcpath();
        process_string[1] = new String(source+".blc");
        process_string[2] = new String(source +".blb");
        
        jTextAreaOutput.append(getBlcpath() + " " + source+".blc "+ source +".blb\n");
        
        try{
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(process_string, null, new File(pathForCd));
            String line="";
            BufferedReader br= new BufferedReader( new InputStreamReader( proc.getInputStream(), Constants.fileFormat));
            
            while ( (line = br.readLine() )!=null ){
                jTextAreaOutput.append(line+"\n");
            }
            proc.waitFor(); //unused int i =
            jTextAreaOutput.append("\n");
            jTextAreaOutput.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER2"));
            jTextAreaOutput.append("\n");
        } catch(IOException e){
            System.out.println(e.getMessage());
        } catch(InterruptedException e){
            System.out.println(e.getMessage());
        }
    }
    
    // run BRE SOURCE to make resource (GLULX MODE) //aqui
    public void makeResources() {
        
        // controllo che esista il compilatore con il path  inserito nella config.ini
        // se non esiste visualizzo un messaggio di warning
        File test = new File(getBrespath());
        if (!test.exists()){
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER1")+" "+getBrespath()+" "+java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER2"),java.util.ResourceBundle.getBundle("JIF").getString("ERR_COMPILER3") , JOptionPane.ERROR_MESSAGE);
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
        } else
            //recupero l'attuale file name
            fileInf = getCurrentFilename(); //jTabbedPane1.getTitleAt( jTabbedPane1.getSelectedIndex());
        
        // Source file name
        String source = fileInf.substring(0,fileInf.lastIndexOf("."));
        
        //jTextAreaOutput.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER1"));
        String process_string[]=new String[2];
        process_string[0] = getBrespath();
        process_string[1] = source;
        
        jTextAreaOutput.append(getBrespath() + " " + source+"\n");
        
        try{
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(process_string, null, new File(getCompiledpath()));
            
            String line="";
            BufferedReader br= new BufferedReader( new InputStreamReader( proc.getInputStream(), Constants.fileFormat));
            
            while ( (line = br.readLine() )!=null ){
                jTextAreaOutput.append(line+"\n");
            }
            
            proc.waitFor(); //int i = unused
            jTextAreaOutput.append("\n");
            jTextAreaOutput.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER2"));
            jTextAreaOutput.append("\n");
        } catch(IOException e){
            System.out.println(e.getMessage());
        } catch(InterruptedException e){
            System.out.println(e.getMessage());
        }
    }
    
    
    // Set INFORM MODE
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
        String inter = getInterpreterglulxpath();
        
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
        } else
            fileInf = getCurrentFilename();//jTabbedPane1.getTitleAt( jTabbedPane1.getSelectedIndex());
        
        clearOutput();
        jTextAreaOutput.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_RUN1"));
        
        try{
            Runtime rt = Runtime.getRuntime();
            String command[]=new String[2];
            command[0]=inter;
            // in base al tipo di file di uscita, scelgo l'estensione del file da passare all'interprete
            String estensione = ".blb";
            
            command[1] = new String(fileInf.substring(0,fileInf.indexOf(".inf"))+estensione);
            
            jTextAreaOutput.append(command[0]+" "+command[1]+"\n");
            
            rt.exec(command); //Process proc = unused
            //String line=""; unused
            //String out=""; unused
            
            jTextAreaOutput.append(java.util.ResourceBundle.getBundle("JIF").getString("OK_COMPILER2"));
        } catch(IOException e){
            System.out.println(e.getMessage());
        }
    }
    
    
    public String checkDefinitionCurrentFile(String entity){
        String file ="", main ="";
        file = getCurrentFilename();
        // check only if the file is an INF or h file
        if ( (file.indexOf(".inf")!=-1) || (file.indexOf(".INF")!=-1)){
            // open and reads the file
            try{
                StringBuffer sb = new StringBuffer();
                String riga;
                sb.setLength(0);
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Constants.fileFormat));
                
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
                        } else{
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
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Constants.fileFormat));
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
                            } else{
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
    private javax.swing.JMenuItem BuildAll;
    private javax.swing.JMenuItem Exit;
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
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
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
    private javax.swing.JButton jButtonCompiledPath;
    private javax.swing.JButton jButtonCompilerPath;
    private javax.swing.JButton jButtonDefaultDark;
    private javax.swing.JButton jButtonDefinition;
    private javax.swing.JButton jButtonFind;
    private javax.swing.JButton jButtonGlulxPath;
    private javax.swing.JButton jButtonInsertSymbol;
    private javax.swing.JButton jButtonInterpreter;
    private javax.swing.JButton jButtonInterpreterPath;
    private javax.swing.JButton jButtonKeyword;
    private javax.swing.JButton jButtonLeftTab;
    private javax.swing.JButton jButtonLibraryPath;
    private javax.swing.JButton jButtonLibraryPath1;
    private javax.swing.JButton jButtonLibraryPath2;
    private javax.swing.JButton jButtonLibraryPath3;
    private javax.swing.JButton jButtonNew;
    private javax.swing.JButton jButtonNormal;
    private javax.swing.JButton jButtonOption;
    private javax.swing.JButton jButtonPrintTutorial;
    private javax.swing.JButton jButtonProperty;
    private javax.swing.JButton jButtonRedo;
    private javax.swing.JButton jButtonReplace;
    private javax.swing.JButton jButtonRightTab;
    private javax.swing.JButton jButtonSearchProject;
    private javax.swing.JButton jButtonSwitchManager;
    private javax.swing.JButton jButtonUncommentSelection;
    private javax.swing.JButton jButtonUndo;
    private javax.swing.JButton jButtonVerb;
    private javax.swing.JCheckBox jCheckBoxAdventInLib;
    private javax.swing.JCheckBox jCheckBoxCreateNewFile;
    private javax.swing.JCheckBoxMenuItem jCheckBoxGlulxMode;
    public javax.swing.JCheckBox jCheckBoxHelpedCode;
    private javax.swing.JCheckBoxMenuItem jCheckBoxInformMode;
    private javax.swing.JCheckBoxMenuItem jCheckBoxJToolBar;
    private javax.swing.JCheckBoxMenuItem jCheckBoxJTree;
    private javax.swing.JCheckBox jCheckBoxMakeResource;
    public javax.swing.JCheckBox jCheckBoxMappingLive;
    private javax.swing.JCheckBox jCheckBoxNumberLines;
    private javax.swing.JCheckBox jCheckBoxOpenLastFile;
    private javax.swing.JCheckBoxMenuItem jCheckBoxOutput;
    private javax.swing.JCheckBox jCheckBoxProjectOpenAllFiles;
    private javax.swing.JCheckBox jCheckBoxScanProjectFiles;
    public javax.swing.JCheckBox jCheckBoxSyntax;
    private javax.swing.JCheckBoxMenuItem jCheckBoxToggleFullscreen;
    public javax.swing.JCheckBox jCheckBoxWrapLines;
    private javax.swing.JComboBox jComboBoxFont;
    private javax.swing.JComboBox jComboBoxFontSize;
    private javax.swing.JDialog jDialogAbout;
    private javax.swing.JDialog jDialogEditFileIni;
    private javax.swing.JDialog jDialogInfo;
    public javax.swing.JDialog jDialogOption;
    private javax.swing.JDialog jDialogProjectProperties;
    private javax.swing.JDialog jDialogProjectSwitches;
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
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabelAttribute;
    private javax.swing.JLabel jLabelBackground;
    private javax.swing.JLabel jLabelBlc;
    private javax.swing.JLabel jLabelBres;
    private javax.swing.JLabel jLabelComment;
    private javax.swing.JLabel jLabelDefaultDark;
    private javax.swing.JLabel jLabelKeyword;
    private javax.swing.JLabel jLabelMainFile;
    private javax.swing.JLabel jLabelNormal;
    private javax.swing.JLabel jLabelProperty;
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
    private javax.swing.JMenuItem jMenuItemBuildAllGlulx;
    private javax.swing.JMenuItem jMenuItemClear;
    private javax.swing.JMenuItem jMenuItemClearAll;
    private javax.swing.JMenuItem jMenuItemClearRecentFiles;
    private javax.swing.JMenuItem jMenuItemClose;
    private javax.swing.JMenuItem jMenuItemCloseAll;
    private javax.swing.JMenuItem jMenuItemCloseProject;
    private javax.swing.JMenuItem jMenuItemCommentSelection;
    private javax.swing.JMenuItem jMenuItemCompile;
    private javax.swing.JMenuItem jMenuItemConfigFile;
    private javax.swing.JMenuItem jMenuItemCopy;
    private javax.swing.JMenuItem jMenuItemCopy1;
    private javax.swing.JMenuItem jMenuItemCut;
    private javax.swing.JMenuItem jMenuItemCut1;
    private javax.swing.JMenuItem jMenuItemExtractStrings;
    private javax.swing.JMenuItem jMenuItemGC;
    private javax.swing.JMenuItem jMenuItemInsertFile;
    private javax.swing.JMenuItem jMenuItemInsertFromFile;
    private javax.swing.JMenuItem jMenuItemInsertSymbol;
    private javax.swing.JMenuItem jMenuItemInsertSymbol1;
    private javax.swing.JMenuItem jMenuItemJumpToSource;
    private javax.swing.JMenuItem jMenuItemLastProject;
    private javax.swing.JMenuItem jMenuItemLeftShift;
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
    private javax.swing.JMenuItem jMenuItemReadMe;
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
    private javax.swing.JMenuItem jMenuItemSwitches;
    private javax.swing.JMenuItem jMenuItemTranslate;
    private javax.swing.JMenuItem jMenuItemUncommentSelection;
    private javax.swing.JMenu jMenuMode;
    private javax.swing.JMenu jMenuOptions;
    private javax.swing.JMenu jMenuPaste;
    private javax.swing.JMenu jMenuProject;
    private javax.swing.JMenuItem jMenuProjectProperties;
    private javax.swing.JMenuItem jMenuProjectSwitches;
    private javax.swing.JMenu jMenuRecentFiles;
    private javax.swing.JMenu jMenuView;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel32;
    private javax.swing.JPanel jPanel35;
    private javax.swing.JPanel jPanel37;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel43;
    private javax.swing.JPanel jPanel44;
    private javax.swing.JPanel jPanel45;
    private javax.swing.JPanel jPanel46;
    private javax.swing.JPanel jPanel47;
    private javax.swing.JPanel jPanel49;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelColor;
    private javax.swing.JPanel jPanelDefaultDark;
    private javax.swing.JPanel jPanelDefinition;
    private javax.swing.JPanel jPanelGeneral;
    private javax.swing.JPanel jPanelGeneralOptions;
    private javax.swing.JPanel jPanelMainFile;
    private javax.swing.JPanel jPanelPath;
    private javax.swing.JPanel jPanelProjectSwitches;
    private javax.swing.JPanel jPanelSearch;
    private javax.swing.JPanel jPanelSearchProject;
    private javax.swing.JPanel jPanelSwitch1;
    private javax.swing.JPanel jPanelSwitch2;
    private javax.swing.JPanel jPanelTreeControl;
    public javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JPopupMenu jPopupMenuProject;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPaneAbout;
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
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane3;
    private static final javax.swing.JTabbedPane jTabbedPane1 = new javax.swing.JTabbedPane();
    private static final javax.swing.JTabbedPane jTabbedPane2 = new javax.swing.JTabbedPane();
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTabbedPane jTabbedPaneLeft;
    private javax.swing.JTabbedPane jTabbedPaneOption;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea4;
    private javax.swing.JTextArea jTextAreaConfig;
    private javax.swing.JTextArea jTextAreaInfo;
    public javax.swing.JTextArea jTextAreaOutput;
    private javax.swing.JTextArea jTextAreaProjectProperties;
    private javax.swing.JTextField jTextFieldBlc;
    private javax.swing.JTextField jTextFieldBres;
    private javax.swing.JTextField jTextFieldDefinition;
    public javax.swing.JTextField jTextFieldFind;
    public javax.swing.JTextField jTextFieldFindAll;
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
    private javax.swing.JTextField jTextFieldTabSize;
    private static final javax.swing.JToolBar jToolBarCommon = new javax.swing.JToolBar();
    private static javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables
    
    // PATHS
    private String fileini;
    private String workingDir;
    private String librarypath;
    private String librarypathsecondary1;
    private String librarypathsecondary2;
    private String librarypathsecondary3;
    private String compiledpath;
    private String interpreterzcodepath;
    private String interpreterglulxpath;
    private String compilerpath;
    private String brespath;
    private String blcpath;
    
    private String fileInf = "";
    private String insertnewdir="";    // JIF si ricorda dell'ultima directory scelta per "insert new"
    protected DefaultStyledDocument doc;
    MouseListener popupListenerProject = new PopupListenerProject();
    MouseListener menuListener  = new MenuListener();
    
    // gestione syntax highlights on/off
    protected StringBuffer sb = new StringBuffer();
    
    // element per mostrare il numero di riga
    private Element el;
    
    // gestione albero INSPECT
    private DefaultMutableTreeNode top,category1,category2,category4,category5,category7;
    private TreePath treePath1,treePath2,treePath4,treePath5,treePath7;
    private HighlightText hlighterOutputErrors,hlighterOutputWarnings;
    private StringTokenizer st;
    private DefaultTreeModel treeModel;
    private volatile String riga;
    
    // gestione aggiunta automatica codice assistito
    private String ultima;          // ultima keyword digitata
    
    // Jif ini configuration
    private Hashtable altkeys;
    private Hashtable executecommands;
    private Hashtable helpcode;
    private Hashtable operations;
    private Hashtable mapping;
    private Hashtable switches;
    private Hashtable projectSwitches;
    private HashSet recentFiles;
    // Syntax highlight
    private HashSet attributes;
    private HashSet properties;
    private HashSet verbs;
    private HashSet keywords;
    // Case Sensitive Management
    private HashSet attributes_cs;
    private HashSet properties_cs;
    private HashSet verbs_cs;
    private HashSet keywords_cs;
    private HashSet symbols;
    
    // per scegliere l'estensione del file da passare all'interprete
    private String tipoz = "";
    // Vettore che contiene i nomi delle nuove classi all'interno del sorgente
    private DefaultMutableTreeNode tmp_nodo;
    // New Files name counter
    private int countNewFile=0;
    // Gestione finestra con i simboli (come la windowObject)
    private JFrame JWindowSymbols;
    private JList jListSymbols;
    // titolo di JIF, serve per aggiungerci il nome del progetto aperto
    private String jifVersion = "Jif "+ Constants.JIFVERSION;
    
    // COLORS
    public Color colorKeyword;
    public Color colorAttribute;
    public Color colorProperty;
    public Color colorVerb;
    public Color colorNormal;
    public Color colorComment;
    public Color colorBackground;
    public Font defaultFont;
    private String lastFile;
    public int tabSize = 4;
    private String lastProject;
    private String lastOpenedProjectPath = null;
    private String lastDir = null;
    
    // PROJECT VARIABLES
    private String currentProject = Constants.PROJECTEMPTY;
    private Vector projectFiles ;
    private Vector projectClass = new Vector();
    private String mainFile="";
    // alphabetical sorting
    private Vector objTree;
    // hack variable
    private int int_var = 0;
    
    public String getJifVersion() {
        return jifVersion;
    }
    
    public void setJifVersion(String jifVersion) {
        this.jifVersion = jifVersion;
    }
    
    public void setAltkeys(Hashtable altkeys) {
        this.altkeys = altkeys;
    }
    
    public void setExecutecommands(Hashtable executecommands) {
        this.executecommands = executecommands;
    }
    
    public Hashtable getHelpcode() {
        return helpcode;
    }
    
    public void setHelpcode(Hashtable helpcode) {
        this.helpcode = helpcode;
    }
    
    public void setMapping(Hashtable mapping) {
        this.mapping = mapping;
    }
    
    public javax.swing.JMenu getJMenuInsertNew() {
        return jMenuInsertNew;
    }
    
    public Hashtable getOperations() {
        return operations;
    }
    
    public void setOperations(Hashtable operations) {
        this.operations = operations;
    }
    
    public javax.swing.JPanel getJPanelProjectSwitch() {
        return jPanelProjectSwitches;
    }
    
    public javax.swing.JPanel getJPanelSwitch1() {
        return jPanelSwitch1;
    }
    
    public javax.swing.JPanel getJPanelSwitch2() {
        return jPanelSwitch2;
    }
    
    public Hashtable getSwitches() {
        return switches;
    }
    
    public void setSwitches(Hashtable switches) {
        this.switches = switches;
    }
    
    public HashSet getSymbols() {
        return symbols;
    }
    
    public void setSymbols(HashSet symbols) {
        this.symbols = symbols;
    }
    
    public HashSet getAttributes() {
        return attributes;
    }
    
    public void setAttributes(HashSet attributes) {
        this.attributes = attributes;
    }
    
    public HashSet getProperties() {
        return properties;
    }
    
    public void setProperties(HashSet properties) {
        this.properties = properties;
    }
    
    public HashSet getVerbs() {
        return verbs;
    }
    
    public void setVerbs(HashSet verbs) {
        this.verbs = verbs;
    }
    
    public HashSet getKeywords() {
        return keywords;
    }
    
    public void setKeywords(HashSet keywords) {
        this.keywords = keywords;
    }
    
    public HashSet getAttributes_cs() {
        return attributes_cs;
    }
    
    public void setAttributes_cs(HashSet attributes_cs) {
        this.attributes_cs = attributes_cs;
    }
    
    public HashSet getProperties_cs() {
        return properties_cs;
    }
    
    public void setProperties_cs(HashSet properties_cs) {
        this.properties_cs = properties_cs;
    }
    
    public HashSet getVerbs_cs() {
        return verbs_cs;
    }
    
    public void setVerbs_cs(HashSet verbs_cs) {
        this.verbs_cs = verbs_cs;
    }
    
    public HashSet getKeywords_cs() {
        return keywords_cs;
    }
    
    public void setKeywords_cs(HashSet keywords_cs) {
        this.keywords_cs = keywords_cs;
    }
    
    public JList getJListSymbols() {
        return jListSymbols;
    }
    
    public void setJListSymbols(JList jListSymbols) {
        this.jListSymbols = jListSymbols;
    }
    
    public String getLibrarypath() {
        return librarypath;
    }
    
    public void setLibrarypath(String librarypath) {
        this.librarypath = librarypath;
    }
    
    public String getLibrarypathsecondary1() {
        return librarypathsecondary1;
    }
    
    public void setLibrarypathsecondary1(String librarypathsecondary1) {
        this.librarypathsecondary1 = librarypathsecondary1;
    }
    
    public String getLibrarypathsecondary2() {
        return librarypathsecondary2;
    }
    
    public void setLibrarypathsecondary2(String librarypathsecondary2) {
        this.librarypathsecondary2 = librarypathsecondary2;
    }
    
    public String getLibrarypathsecondary3() {
        return librarypathsecondary3;
    }
    
    public void setLibrarypathsecondary3(String librarypathsecondary3) {
        this.librarypathsecondary3 = librarypathsecondary3;
    }
    
    public String getCompiledpath() {
        return compiledpath;
    }
    
    public void setCompiledpath(String compiledpath) {
        this.compiledpath = compiledpath;
    }
    
    public String getInterpreterzcodepath() {
        return interpreterzcodepath;
    }
    
    public void setInterpreterzcodepath(String interpreterzcodepath) {
        this.interpreterzcodepath = interpreterzcodepath;
    }
    
    public String getInterpreterglulxpath() {
        return interpreterglulxpath;
    }
    
    public void setInterpreterglulxpath(String interpreterglulxpath) {
        this.interpreterglulxpath = interpreterglulxpath;
    }
    
    public String getCompilerpath() {
        return compilerpath;
    }
    
    public void setCompilerpath(String compilerpath) {
        this.compilerpath = compilerpath;
    }
    
    public String getBrespath() {
        return brespath;
    }
    
    public void setBrespath(String brespath) {
        this.brespath = brespath;
    }
    
    public String getBlcpath() {
        return blcpath;
    }
    
    public void setBlcpath(String blcpath) {
        this.blcpath = blcpath;
    }
    
    public javax.swing.JMenu getJMenuRecentFiles() {
        return jMenuRecentFiles;
    }
    
    public void setJMenuRecentFiles(javax.swing.JMenu jMenuRecentFiles) {
        this.jMenuRecentFiles = jMenuRecentFiles;
    }
    
    public String getFileini() {
        return fileini;
    }
    
    public void setFileini(String fileini) {
        this.fileini = fileini;
    }
    
    public Hashtable getProjectSwitches() {
        return projectSwitches;
    }
    
    public void setProjectSwitches(Hashtable projectSwitches) {
        this.projectSwitches = projectSwitches;
    }

    public HashSet getRecentFiles() {
        return recentFiles;
    }

    public void setRecentFiles(HashSet recentFiles) {
        this.recentFiles = recentFiles;
    }
}
