package it.schillaci.jif.gui;

/*
 * JifTextPane.java
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
import it.schillaci.jif.core.HighlightBookmark;
import it.schillaci.jif.core.HighlightText;
import it.schillaci.jif.inform.InformContext;
import it.schillaci.jif.inform.InformSyntax;
import it.schillaci.jif.core.JifDAO;
import it.schillaci.jif.core.JifDocument;
import it.schillaci.jif.core.JifEditorKit;
import it.schillaci.jif.core.JifFileName;
import it.schillaci.jif.core.TranslatedString;
import it.schillaci.jif.core.Utils;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Utilities;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 * This is a sub-class of JTextPane, with the Inform source management.
 * @author Alessandro Schillaci
 */
public class JifTextPane extends JTextPane {
    
    private static final long serialVersionUID = 1475021670099346825L;
    private UndoManager undoF;
    private jFrame jframe;
    private Element el;
    private MouseListener popupListener;
    private String pathfile;
    private String subPath;
    private HighlightText hlighterJumpTo;
    private HighlightText hlighterBrackets;
    private HighlightText hlighterErrors;
    private HighlightText hlighterWarnings;
    private HighlightBookmark hlighterBookmarks;
    private java.util.List bookmarks;
    private JifDocument doc;
    private JifEditorKit editor;
    
    /**
     * Creates a new instance of JifTextPane
     * 
     * @param parent
     *           The instance of main jFrame
     * @param fileName 
     *           Name of the file to be load into JifTextPane.
     */
    public JifTextPane(jFrame parent, JifFileName fileName, File file, InformContext context) {
        jframe = parent;
        pathfile = fileName.getPath();
        popupListener = new PopupListener(this, jframe);
        hlighterBookmarks = new HighlightBookmark(
                this,
                context.getForeground(InformSyntax.Bookmarks)
                );
        hlighterBrackets = new HighlightText(
                this,
                context.getForeground(InformSyntax.Brackets)
                );
        hlighterErrors = new HighlightText(
                this,
                context.getForeground(InformSyntax.Errors)
                );
        hlighterJumpTo = new HighlightText(
                this,
                context.getForeground(InformSyntax.JumpTo)
                );
        hlighterWarnings = new HighlightText(
                this,
                context.getForeground(InformSyntax.Warnings)
                );
        bookmarks = new ArrayList();
        setPaths(pathfile);
        
        setDoubleBuffered(true);
        setEditable(true);
        setFont(context.getFont());
        setBackground(context.getBackground());
        setCaretColor(context.getForeground(InformSyntax.Normal));
        getCaret().setBlinkRate(200);

        if (jframe.config.getSyntaxHighlighting()) {
            editor = fileName.createEditorKit();
        } else {
            editor = new JifEditorKit();
        }
        
        doc = editor.createDefaultDocument(context);

        long tempo1=System.currentTimeMillis();
        loadFile(file);
        System.out.println("Tempo impiegato= "+(System.currentTimeMillis()-tempo1));
        
        undoF = new UndoManager();
        undoF.setLimit(5000);
        
        doc.addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent evt) {
                undoF.addEdit(evt.getEdit());
                // adding a "*" to the file name, when the file has changed but not saved
                if (jframe.getFileTabbed().getComponentCount()!=0  && jFrame.getCurrentFilename().indexOf("*")==-1) {
                    jframe.getFileTabbed().setTitleAt( jframe.getFileTabbed().getSelectedIndex(), subPath + "*");
                    jframe.setTitle(jframe.getJifVersion() + " - " + jFrame.getCurrentFilename());
                }
            }
        });
        
        getActionMap().put("Undo", new AbstractAction("Undo") {
            private static final long serialVersionUID = 4366132315214562191L;
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (undoF.canUndo()) {
                        while (undoF.getUndoPresentationName().equals(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_STYLE_CHANGED_UNDO"))){
                            undoF.undo();
                        }
                        undoF.undo();
                        // adding a "*" to the file name, when the file has changed but not saved
                        if (jframe.getFileTabbed().getComponentCount()!=0  && jFrame.getCurrentFilename().indexOf("*")==-1){
                            jframe.getFileTabbed().setTitleAt( jframe.getFileTabbed().getSelectedIndex(), subPath + "*");
                            jframe.setTitle(jframe.getJifVersion() +" - " + jFrame.getCurrentFilename());
                        }
                    }
                } catch (CannotUndoException e) {
                    System.out.println(e.getMessage());
                }
            }
        });
        
        getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
        
        getActionMap().put("Redo", new AbstractAction("Redo") {
            private static final long serialVersionUID = 3720633173380513902L;
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (undoF.canRedo()) {
                        while (undoF.getRedoPresentationName().equals(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_STYLE_CHANGED_REDO"))){
                            undoF.redo();
                        }
                        undoF.redo();
                        // adding a "*" to the file name, when the file has changed but not saved
                        if (jframe.getFileTabbed().getComponentCount()!=0  && jFrame.getCurrentFilename().indexOf("*")==-1){
                            jframe.getFileTabbed().setTitleAt( jframe.getFileTabbed().getSelectedIndex(), subPath + "*");
                            jframe.setTitle(jframe.getJifVersion() +" - " + jFrame.getCurrentFilename());
                        }
                    }
                } catch (CannotRedoException e) {
                    //System.out.println(e.getMessage());
                }
            }
        });
        
        getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
        
        // Editor key adapter is only used with inform content
        if (fileName.getContentType() == JifFileName.INFORM) {
                addKeyListener(new EditorKeyAdapter(jframe, this));
            }
        
        // Add Mouse Listener for the right-mouse popup
        addMouseListener(popupListener);
        
        addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent ce) {
                int pos = getCaretPosition();
                //Element map = getDocument().getDefaultRootElement();
                Element map = doc.getDefaultRootElement();
                int row = map.getElementIndex(pos);
                Element lineElem = map.getElement(row);
                int col = pos - lineElem.getStartOffset();
                jframe.rowColTextField.setText((row+1)+" | "+(col+1));
            }
        });
        
        setEditorKit(editor);
        setDocument(doc);
    }
    
    
    /**
     * Load text from a file into a JifTextPane
     * 
     * @param file 
     *           The file to load into JifTextPane
     */
    public void loadFile(File file) {
        if (file == null) {
            return;
        }
        try {
            String text = JifDAO.read(file);

            SimpleAttributeSet sas = new SimpleAttributeSet();

            // Check for \t characters
            if (text.indexOf("\t")!=-1) {
                jframe.outputTextArea.setText(java.util.ResourceBundle.getBundle("JIF").getString("JIF_TAB_WARNING"));
                doc.insertString(0,
                        Utils.replace(text,
                                "\t",
                                JifEditorKit.getTabString()),
                        sas);
            } else {
                doc.insertString(0, text, sas);
            }
            
            setCaretPosition(0);

        } catch (Exception e) {
            System.out.println("ERROR Load file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public CharBuffer getCharBuffer() {
        Charset charset = Charset.forName(Constants.fileFormat);
        CharsetEncoder encoder = charset.newEncoder();
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer cb = null;
        try {
            ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(getText()));
            cb = decoder.decode(bbuf);
        } catch (Exception ex) {
            System.out.println("ERROR getCharBuffer:" + ex.getMessage());
        } finally {
            return cb;
        }
    }
    
    
    /**
     * Returns the current row (as a String) from the current caret position
     *
     * @return the current caret row
     */
    public String getCurrentRow() {
        String lastRow = null;
        try {
            Element el = getDocument().getDefaultRootElement();
            int ind = el.getElementIndex(getCaretPosition()-1);
            el = getDocument().getDefaultRootElement().getElement(ind);
            lastRow = getText(el.getStartOffset(), el.getEndOffset()-el.getStartOffset());
        } catch (BadLocationException e) {
            System.out.println(e.getMessage());
        }
        return lastRow;
    }
    
    
    /**
     * Returns the current row (as a String) from a given Caret position
     *
     * @param posizione
     *          The position of Caret
     * @return The row at the position
     */
    public String getRowAt(int posizione) {
        String lastRow = null;
        try {
            Element el = getDocument().getDefaultRootElement();
            int ind = el.getElementIndex(posizione);
            el = getDocument().getDefaultRootElement().getElement(ind);
            lastRow = getText(el.getStartOffset(), el.getEndOffset()-el.getStartOffset());
        } catch (BadLocationException e) {
            System.out.println(e.getMessage());
        }
        return lastRow;
    }
    
    /**
     * Returns the current row (as a String to UPPER CASE) from a given Caret
     * position
     *
     * @param posizione
     *          The position of Caret
     * @return The row at the position
     */
    public String getUpperRowAt(int posizione) {
        String lastRow = null;
        try {
            Element el = getDocument().getDefaultRootElement();
            int ind = el.getElementIndex(posizione);
            el = getDocument().getDefaultRootElement().getElement(ind);
            lastRow = getText(el.getStartOffset(), el.getEndOffset()-el.getStartOffset());
        } catch (BadLocationException e) {
            System.out.println(e.getMessage());
            System.out.println("position = "+posizione);
        }
        return lastRow.toUpperCase();
    }
    
    
    void searchCloseBracket(String start, String end) {
        try {
            int posizioneIniziale = getCaretPosition()-1;
            int c=1;
            int opened = 0;  // number of open brackets
            boolean found = false;
            
            while (!found) {
                if ((getText(posizioneIniziale + c,1).equals(end)) && (opened==0)) {
                    found = true;
                }
                // if an open bracket is found, opened++
                if (getText(posizioneIniziale + c,1).equals(start)) {
                    opened++;
                }
                // if a closed bracket is found, opened--
                if (getText(posizioneIniziale + c,1).equals(end) && (opened!=0)) {
                    opened--;
                }
                c++;
            }
            if (found) {
                hlighterBrackets.highlightFromTo(this, posizioneIniziale+c-1, posizioneIniziale+c);
            }
        } catch (Exception e) {
        }
    }
    
    
    
    // Seek and highlight open brackets
    void searchOpenBracket(String start, String end) {
        try{
            int posizioneIniziale = getCaretPosition()-1;
            int c=1;
            int closed = 0;  // number of closed brackets
            boolean found = false;
            while (!found){
                if ((getText(posizioneIniziale - c,1).equals(end)) && (closed==0)) {
                    found = true;
                }
                // if a closed bracket is found, closed++
                if (getText(posizioneIniziale - c,1).equals(start)) {
                    closed++;
                }
                // if an open bracket is found, opened--
                if (getText(posizioneIniziale - c,1).equals(end) && (closed!=0)) {
                    closed--;
                }
                c++;
            }
            
            if (found) {
                hlighterBrackets.highlightFromTo(this, posizioneIniziale-c+1, posizioneIniziale-c+2);
            }
        } catch (Exception e) {}
    }
    
    // Seek and highlight closed brackets
    int searchCloseBracket(String start, String end, int posizione) {
        try{
            int posizioneIniziale = posizione;
            int c=1;
            int opened = 0;  // number of opened brackets
            boolean found = false;
            while (!found){
                if ((getText(posizioneIniziale + c,1).equals(end)) && (opened==0)) {
                    found = true;
                }
                // if an open bracket is found, opened++
                if (getText(posizioneIniziale + c,1).equals(start)) {
                    opened++;
                }
                // if a closed bracket is found, opened--
                if (getText(posizioneIniziale + c,1).equals(end) && (opened!=0)) {
                    opened--;
                }
                c++;
            }
            return 0;  // no errors
            
        } catch (Exception e) {
            el = getDocument().getDefaultRootElement();
            int ind = el.getElementIndex(posizione);
            el = getDocument().getDefaultRootElement().getElement(ind);
            hlighterBrackets.highlightFromTo(this, el.getStartOffset(), el.getEndOffset());
            return -1;  // an error occurs
        }
    }
    
    // Seek and highlight the open bracket
    int searchOpenBracket(String start, String end, int posizione) {
        try{
            int posizioneIniziale = posizione;
            int c=1;
            int closed = 0;  // number of closed brackets
            boolean found = false;
            while (!found) {
                if ((getText(posizioneIniziale - c,1).equals(end)) && (closed==0)) {
                    found = true;
                }
                // if an open bracket is found, closed++
                if (getText(posizioneIniziale - c,1).equals(start)) {
                    closed++;
                }
                // if a close bracket is found, closed--
                if (getText(posizioneIniziale - c,1).equals(end) && (closed!=0)) {
                    closed--;
                }
                c++;
            }
            return 0;  // no errors
            
        } catch (Exception e) {
            el = getDocument().getDefaultRootElement();
            int ind = el.getElementIndex(posizione);
            el = getDocument().getDefaultRootElement().getElement(ind);
            hlighterBrackets.highlightFromTo(this, el.getStartOffset(), el.getEndOffset());
            return -1;  // An error occurs
        }
    }
    
    
    /**
     * Performs a brackets validation.
     * If this find an incomplete bracket (not opened
     * or not closed), JIF will highlight the
     * wrong brackets to be fixed.
     * @param parent Instance of Main jFrame
     */
    public void checkBrackets(jFrame parent) {
        
        // 1. Check the brackets { opened
        String pattern = "{";
        String close = "}";
        int pos = 0;
        boolean errore= false;
        
        int errore1=1;
        int errore2=1;
        int errore3=1;
        int errore11=1;
        int errore22=1;
        int errore33=1;
        
        String testo = getText();
        while ((pos = testo.indexOf(pattern, pos)) >= 0) {
            if (searchCloseBracket(pattern, close, pos)==-1) {
                errore = true;
                break;
            }
            pos += pattern.length();
        }
        if (errore){
            setCaretPosition(pos);
        } else{
            errore1=0;
        }
        
        // 2. Check the Brackets [ opened
        pattern = "[";
        close = "]";
        pos = 0;
        errore= false;
        testo = getText();
        while ((pos = testo.indexOf(pattern, pos)) >= 0) {
            if (searchCloseBracket(pattern, close, pos)==-1) {
                errore = true;
                break;
            }
            pos += pattern.length();
        }
        if (errore){
            setCaretPosition(pos);
        } else{
            errore2=0;
        }
        
        
        // 3. Check the brackets ( opened
        pattern = "(";
        close = ")";
        pos = 0;
        errore= false;
        testo = getText();
        while ((pos = testo.indexOf(pattern, pos)) >= 0) {
            if (searchCloseBracket(pattern, close, pos)==-1) {
                errore = true;
                break;
            }
            pos += pattern.length();
        }
        if (errore){
            setCaretPosition(pos);
        } else{
            errore3=0;
        }
        
        
        
        // 4. Check the brackets } closed
        pattern = "}";
        close = "{";
        pos = 0;
        errore= false;
        
        
        while ((pos = testo.indexOf(pattern, pos)) >= 0) {
            if (searchOpenBracket(pattern, close, pos)==-1) {
                errore = true;
                break;
            }
            pos += pattern.length();
        }
        if (errore){
            setCaretPosition(pos);
        } else{
            errore11=0;
        }
        
        
        // 5. Check the brackets ] closed
        pattern = "]";
        close = "[";
        pos = 0;
        errore= false;
        testo = getText();
        while ((pos = testo.indexOf(pattern, pos)) >= 0) {
            if (searchOpenBracket(pattern, close, pos)==-1) {
                errore = true;
                break;
            }
            pos += pattern.length();
        }
        if (errore) {
            setCaretPosition(pos);
        } else {
            errore22=0;
        }
        
        
        // 6. Check the brackets ) closed
        pattern = ")";
        close = "(";
        pos = 0;
        errore= false;
        testo = getText();
        while ((pos = testo.indexOf(pattern, pos)) >= 0) {
            if (searchOpenBracket(pattern, close, pos)==-1) {
                errore = true;
                break;
            }
            pos += pattern.length();
        }
        if (errore){
            setCaretPosition(pos);
        } else{
            errore33=0;
        }
        if ((errore1 + errore2 + errore3 + errore11 + errore22 + errore33)==0) {
            JOptionPane.showMessageDialog(parent,
                    java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_CHECK_BRACKET_OK"),
                    "OK", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    
    /**
     * Finds a String in the JifTextPane and
     * highlight it.
     * The target String to be found, it taken from
     * the Search TextField
     */
    public void findString(jFrame parent) {
        int pos = getCaretPosition();   // current position
        String pattern = jframe.findTextField.getText();
        
        try {
            String text = getDocument().getText(0, getDocument().getLength());
            boolean found = false;
            
            //while ( ( (pos = text.indexOf(pattern, pos)) >= 0) && (!found)) {
            while ( ( (pos = Utils.IgnoreCaseIndexOf(text,pattern, pos)) >= 0)  && (!found)) {
                //hlighterJumpTo.highlightFromTo(this, pos, pos + pattern.length());
                // Bug #4416
                this.setSelectionStart(pos);
                this.setSelectionEnd(pos + pattern.length());
                // Bug #4416
                pos += pattern.length();
                //setCaretPosition(pos);
                found = true;
                this.requestFocus();
            }
            
            
            // If the string not found, JIF will move the Caret position to 0 (zero)
            if (!found) {
                // if at least one string is found
                if (Utils.IgnoreCaseIndexOf(text,pattern, 0)!=-1){
                    // append a message in the outputwindow
                    parent.outputTextArea.setText(java.util.ResourceBundle.getBundle("JIF").getString("JIF_END_OF_FILE"));
                    setCaretPosition(0);
                    findString(parent);
                } else {
                    // if there aren't any occurences of the string
                    // append a message in the outputwindow
                    parent.outputTextArea.setText("String \""+parent.findTextField.getText()+"\" not found");
                }
            }
            
        } catch (BadLocationException e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Returns the current HighlighterWarnings
     * @return the current HighlighterWarnings
     */
    public HighlightText getHlighterWarnings() {
        return hlighterWarnings;
    }
    
    /**
     * Returns the current HighlighterErrors
     * @return the current HighlighterErrors
     */
    public HighlightText getHlighterErrors() {
        return hlighterErrors;
    }
    
    
    /**
     * Returns the current Highlighter
     * @return the current Highlighter
     */
    public HighlightText getHlighter() {
        return hlighterJumpTo;
    }
    
    /**
     * Remove current highlighter from the JifTextPane
     */
    public void removeHighlighter() {
        if (hlighterJumpTo == null) {
            return;
        }    
        hlighterJumpTo.removeHighlights(this);
    }
    
    /**
     * Remove all the check-brackets highlighters from
     * current JifTextPane
     */
    public void removeHighlighterBrackets() {
        if (hlighterBrackets == null) {
            return;
        }
        hlighterBrackets.removeHighlights(this);
    }
    
    /**
     * Remove current highlighterErrors from the JifTextPane
     */
    public void removeHighlighterErrors() {
        if (hlighterErrors == null) {
            return;
        }
        hlighterErrors.removeHighlights(this);
    }
    
    /**
     * Remove current highlighterWarnings from the JifTextPane
     */
    public void removeHighlighterWarnings() {
        if (hlighterWarnings == null) {
            return;
        }
        hlighterWarnings.removeHighlights(this);
    }
    
    /**
     * This method will fix the current selection
     * before performing a SHIFT RIGHT/LEFT
     * COMMENT/UNCOMMENT action
     */
    public void selectionText() {
        if (getSelectedText()==null) {
            // Select the current row
            Element el = getDocument().getDefaultRootElement();
            int ind = el.getElementIndex(getCaretPosition());
            el = getDocument().getDefaultRootElement().getElement(ind);
            setSelectionStart(el.getStartOffset());
            setSelectionEnd(el.getEndOffset()-1);
        } else {
            int indstart = getDocument().getDefaultRootElement().getElementIndex(getSelectionStart());
            int indend   = getDocument().getDefaultRootElement().getElementIndex(getSelectionEnd());
            Element elmstart = getDocument().getDefaultRootElement().getElement(indstart);
            Element elmend   = getDocument().getDefaultRootElement().getElement(indend);
            setSelectionStart(elmstart.getStartOffset());
            setSelectionEnd(elmend.getEndOffset()-1);
        }
    }
    
    /**
     * Comment the current text selection
     */
    public void commentSelection() {
        
        selectionText();
        
        String riga = "";
        StringBuffer output = new StringBuffer();
        int start   = getSelectionStart();
        int end     = getSelectionEnd();
        Element el  = getDocument().getDefaultRootElement();
        
        int index_start = el.getElementIndex(start);
        int index_end = el.getElementIndex(end);
        try {
            for (int i=index_start; i<index_end; i++) {
                el = getDocument().getDefaultRootElement().getElement(i);
                riga = getText(el.getStartOffset(), el.getEndOffset()-el.getStartOffset());
                output.append("!").append(riga);
            }
            // I'll remove the '\n' to the last row of the selected text
            el = getDocument().getDefaultRootElement().getElement(index_end);
            riga = getText(el.getStartOffset(), el.getEndOffset()-el.getStartOffset());
            output.append("!").append(riga.substring(0,riga.length()-1));
            replaceSelection(output.toString());
            
            // Commented rows will be selected again
            requestFocus();
            setSelectionStart(start);
            setSelectionEnd(end + index_end - index_start +1);
            
        } catch(BadLocationException e) {
            System.out.println(e.getMessage());
        }
    }
    
    
    
    /**
     * Remove Comment from the current text selection
     */
    public void uncommentSelection() {
        
        selectionText();
        
        String riga = "";
        StringBuffer output = new StringBuffer();
        int start = getSelectionStart();
        int end = getSelectionEnd();
        el = getDocument().getDefaultRootElement();
        
        int index_start = el.getElementIndex(start);
        int index_end = el.getElementIndex(end);
        boolean error = false;  // if true, at least one row starts with !
        try {
            for (int i=index_start; i<index_end; i++) {
                el = getDocument().getDefaultRootElement().getElement(i);
                riga = getText(el.getStartOffset(), el.getEndOffset()-el.getStartOffset());
                if (riga.indexOf("!")==-1) {
                    error = true;
                }
                output.append(riga.substring(riga.indexOf("!")+1,riga.length()));
            }
            el = getDocument().getDefaultRootElement().getElement(index_end);
            riga = getText(el.getStartOffset(), el.getEndOffset()-el.getStartOffset());
            if (riga.indexOf("!")==-1) {
                error = true;
            }
            output.append(riga.substring(riga.indexOf("!")+1,riga.length()-1));
            
            if (!error) {
                replaceSelection(output.toString());
                
                // Un-Commented rows will be selected again
                requestFocus();
                setSelectionStart(start);
                setSelectionEnd(start+output.length());
            } else {
                JOptionPane.showMessageDialog(null,
                        java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_UNCOMMENT_ERROR"),
                        "Warning", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (BadLocationException e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Right shift tab the selected text
     */
    public void tabRightSelection() {
        selectionText();
        
        String riga="";
        StringBuffer output = new StringBuffer();
        int start = getSelectionStart();
        int end = getSelectionEnd();
        el = getDocument().getDefaultRootElement();
        
        int index_start = el.getElementIndex(start);
        int index_end = el.getElementIndex(end);
        
        try {
            for (int i=index_start; i<index_end+1; i++) {
                el = getDocument().getDefaultRootElement().getElement(i);
                riga = getText(el.getStartOffset(), el.getEndOffset()-el.getStartOffset());
                
                if (i == index_end) {
                    output.append(JifEditorKit.getTabString()).append(riga.substring(0,riga.indexOf('\n')));
                } else {
                    output.append(JifEditorKit.getTabString()).append(riga);
                }
            }
            
            replaceSelection(output.toString());
            
            // Selected rows will be selected again
            requestFocus();
            setSelectionStart(start);
            setSelectionEnd(end + ((index_end - index_start+1)*(JifEditorKit.getTabSize())));
            
        } catch (BadLocationException e) {
            System.out.println("ERROR: Tab Right Selection " + e.getMessage());
        }
    }
    
    /**
     * Left shift tab the selected text
     */
    public void tabLeftSelection() {
        selectionText();
        
        String riga = "";
        StringBuffer output = new StringBuffer();
        int start = getSelectionStart();
        int end = getSelectionEnd();
        el = getDocument().getDefaultRootElement();
        int index_start = el.getElementIndex(start);
        int index_end = el.getElementIndex(end);
        
        try {
            for (int i=index_start; i<index_end+1; i++) {
                el = getDocument().getDefaultRootElement().getElement(i);
                riga = getText(el.getStartOffset(), el.getEndOffset()-el.getStartOffset());
                
                if (i == index_end) {
                    if (riga.startsWith(JifEditorKit.getTabString())) {
                        output.append(riga.substring(JifEditorKit.getTabSize(),riga.indexOf('\n')));
                    } else {
                        output.append(riga.substring(0,riga.indexOf('\n')));
                    }
                } else {
                    if (riga.startsWith(JifEditorKit.getTabString())) {
                        output.append(riga.substring(JifEditorKit.getTabSize()));
                    } else {
                        output.append(riga);
                    }
                }
            }
            
            replaceSelection(output.toString());
            
            // Selected rows will be selected again
            requestFocus();
            int posizione= getDocument().getDefaultRootElement().getElement(index_start).getStartOffset();
            setSelectionStart(posizione);
            setSelectionEnd(posizione+output.length());
            
        } catch (BadLocationException e) {
            System.out.println("ERROR: Tab Left Selection " + e.getMessage());
        }
    }
    
    
    
    
    
    
    /**
     * This method extracts all strings from source Inform code
     * and save them into a new file ("translate.txt")
     * 
     * The format is:
     * 
     * *****
     * STRING1
     * =====
     * STRING1 TRANSLATION
     * 
     * *****
     * STRING2
     * =====
     * STRING2 TRANSLATION
     * 
     * ...
     * 
     * 
     * This file will be used by the "insertTranslate()" method
     * to rescue the translations from translate.txt file and
     * merge into the current file to create a new file with
     * the translation ("translated.inf")
     * 
     * @param file The Output File (i.e. "translate.txt")
     */
    public void extractTranslate(File file) {
        StringBuffer translate = new StringBuffer();
        String appoggio;
        String testo = getText();
        
        // controllo che non ci siano parentesi opened-closed
        testo = Utils.replace(testo,"\"\"","\" \"");
        
        StringTokenizer stok = new StringTokenizer(testo,"\"\"");
        stok.nextToken();
        while (stok.hasMoreTokens()) {
            appoggio = stok.nextToken();
            // To eliminate cases like: ".", "   "
            if (
                    !(appoggio.trim().equals("")) &&
                    (appoggio.length()>1)
                    ) {
                translate.append("*****\n");
                translate.append(appoggio);
                translate.append("\n=====\n");
                translate.append(appoggio+"\n");
            }
            if (stok.hasMoreTokens()) {
                stok.nextToken();
            }
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            Writer out = new OutputStreamWriter( fos, Constants.fileFormat );
            out.write(translate.toString());
            out.flush();
            out.close();
            JOptionPane.showMessageDialog(null,
                    "OK",
                    "Message",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e ) {
            System.out.println(e.getMessage());
        }
    }
    
    
    
    
    /**
     * This method rescues the translations from translate.txt
     * file and merges into the current file to create a new file with
     * the translation ("translated.inf")
     * 
     * 
     * @param file Current file in the JifTextPane
     * @param fileout The output file (i.e. "translated.inf")
     */
    public void insertTranslate(File file, File fileout) {
        String testo = getText();
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Constants.fileFormat));
            boolean chiave = false;
            String key = null;
            String obj = null;
            String riga;
            Vector<TranslatedString> strings = new Vector<TranslatedString>();
            
            while ((riga = br.readLine())!=null) {
                if (riga.startsWith("*****")) {
                    chiave=true;
                    if (obj!=null) {
                        // serve come entry nel caso della prima key
                        // inserisco la chiave al passo precedente
                        if (key.startsWith("\n")) {
                            key = key.substring(1);
                        }
                        if (obj.startsWith("\n")) {
                            obj = obj.substring(1);
                        }
                        //testo = Utils.replace(testo, key , obj);
                        strings.add(new TranslatedString(key,obj));
                        //strings.put(key,obj);
                        key="";
                        obj="";
                    }
                } else if (riga.startsWith("=====")) {
                    chiave=false;
                } else if (chiave==true) {
                    // sto leggendo una chiave
                    if (key!=null) {
                        key = key + "\n" + riga;
                    } else {
                        key = riga;
                    }
                } else if (chiave==false) {
                    // sto leggendo una chiave
                    if (obj!=null) {
                        obj = obj + "\n" + riga;
                    } else {
                        obj = riga;
                    }
                }
            }
            br.close();
            
            // FIX: sort HashMap by length of strings
            // Before JIF translates the longer strings and then the shorter ones
            Collections.sort(new ArrayList<TranslatedString>(strings),new Comparator(){
                public int compare(Object a, Object b) {
                    String id1 = ((TranslatedString)a).getSource();
                    String id2 = ((TranslatedString)b).getResult();
                    return (id2.length() - id1.length());
                }
            });
            
            // Execute the translation using the sorted strings
            for (Iterator ite = strings.iterator(); ite.hasNext(); ) {
                TranslatedString ts = (TranslatedString) ite.next();
                testo = Utils.replace(testo, "\""+ts.getSource()+"\"" , "\""+ts.getResult()+"\"");
            }
            // FIX
            
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        
        // saving the output file
        try {
            FileOutputStream fos = new FileOutputStream(fileout);
            Writer out = new OutputStreamWriter( fos, Constants.fileFormat );
            out.write(testo);
            out.flush();
            out.close();              
            JOptionPane.showMessageDialog(null,
                    "OK",
                    "Message",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e ) {
            System.out.println(e.getMessage());
        }
    }
    
    public void setBookmark() {
        int pos = getCaretPosition();
        Element map = getDocument().getDefaultRootElement();
        int row = map.getElementIndex(pos);
        updateBookmark(new Integer(row));
    }
    
    
    public void nextBookmark() {
        // position = actual row
        int jumpto = 0;
        
        if (bookmarks.size()==0) {
            return;
        }
        
        
        int row = getDocument().getDefaultRootElement().getElementIndex(getCaretPosition());
        
        for (Iterator ite=bookmarks.iterator(); ite.hasNext();) {
            Integer ind = (Integer) ite.next();
            if (row < ind.intValue()) {
                jumpto = ind.intValue();
                break;
            }
        }
        
        // last bookmark
        if (jumpto == 0 && bookmarks.size() != 0) {
            jumpto = ( (Integer)bookmarks.get(0)).intValue();
        }
        
        // Jump to the bookmark row
        Element ele = getDocument().getDefaultRootElement().getElement(jumpto);
        try {
            scrollRectToVisible(modelToView(getDocument().getLength()));
            scrollRectToVisible(modelToView(ele.getStartOffset()));
            setCaretPosition(ele.getStartOffset());
        } catch (BadLocationException ble) {
            System.out.println(ble);
        }
    }
    
    public void applyBookmarks() {
        // Removing the hlighterBookmarks
        if (this.hlighterBookmarks != null) {
            this.hlighterBookmarks.removeHighlights(this);
        }
        // Repaint all the highlights
        Element element;
        for (Iterator ite=bookmarks.iterator(); ite.hasNext();) {
            Integer ind = (Integer) ite.next();
            element = getDocument().getDefaultRootElement().getElement(ind.intValue());
            hlighterBookmarks.highlightFromTo(this, element.getStartOffset(), element.getEndOffset());
        }
    }
    
    
    public void updateBookmark(Integer line) {
        if (this.bookmarks.contains(line)) {
            this.bookmarks.remove(line);
            applyBookmarks();
        } else {
            this.bookmarks.add(line);
            applyBookmarks();
        }
    }
    
    // returns the current Word using the caret Position
    public String getCurrentWord() throws BadLocationException {
        int start = Utilities.getWordStart(this, getCaretPosition());
        int end = Utilities.getWordEnd(this, getCaretPosition());
        String word = getDocument().getText(start, end-start);
        if (word.indexOf(".") != -1) {
            word = word.substring(0, word.lastIndexOf("."));
        }
        //System.out.println( "Selected word: " + word );
        return word;
    }
    
    // --- Accessor methods ----------------------------------------------------
    
    public void setPaths(String aString) {
        this.pathfile = aString;
        if (pathfile.length() > 20) {
            subPath = pathfile.substring(0,  10) + 
                    "..." + 
                    pathfile.substring(pathfile.length()-20,  pathfile.length());
        } else {
            subPath = pathfile;
        }
    }
    
    public String getSubPath() {
        return subPath;
    }
    
}
