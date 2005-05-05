/*
 * JIFTextPane.java
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

import javax.swing.*;
import javax.swing.text.*;
import java.io.*;
import java.awt.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;

/**
 * This is a sub-class of JTextPane, with the Inform source management.
 * @author Alessandro Schillaci
 */
public class JIFTextPane extends JTextPane{

    UndoManager undoF;
    jFrame jframe;
    Element el;
    MouseListener popupListener;
    String pathfile;
    private HighlightText hlighter;
    private HighlightText hlighterBrackets;
    private HighlightBookmark hlighterBookmarks;    
    java.util.List bookmarks;


    /**
     * Creates a new instance of JIFTextPane
     * @param parent The instance of main jFrame
     * @param file File to be load into JIFTextPane.
     * If this is NULL, a new empty JIFTextPane will be created
     */
    public JIFTextPane(jFrame parent,File file) {
        this.jframe = parent;
        this.pathfile = (file != null ? file.getAbsolutePath() : "");
        this.popupListener = new PopupListener(this, jframe);
        hlighterBrackets = new HighlightText(this,Color.pink);
        //hlighterBookmarks = new HighlightBookmark(this,new Color(204, 204, 255));       
        hlighterBookmarks = new HighlightBookmark(this, Color.gray);               
        hlighter = new HighlightText(this,Color.pink);
        this.bookmarks = new ArrayList();
        
        setBackground(jframe.colorBackground);
        setCaretColor(jframe.colorNormal);
        getCaret().setBlinkRate(200);
        setDoubleBuffered(true);
        setEditorKit(new StyledEditorKit());
        setEditable(true);
        setFont(jframe.defaultFont);
        setCaretColor(jframe.colorNormal);

        // If the JCheckBoxSyntax = true, using InformDocument, otherwise using Document
	if (jframe.jCheckBoxSyntax.isSelected()){
            if (file==null){
                // This is a new file, I'll apply the Inform Syntax Highlighting
                setDocument(new InformDocument(jframe));
            }
            else {
                if (pathfile.endsWith(".inf")||(pathfile.endsWith(".h"))){
                    // .inf or .h file
                    setDocument(new InformDocument(jframe));
                }
                else if (pathfile.endsWith(".res")){
                    // Resource File, I'll use the ResDocument for Syntax Highlighting
                    setDocument(new ResDocument(jframe));
                }
                else {
                    // a Normal Document. I'll use a DefaultStyledDocument
                    setDocument(new DefaultStyledDocument());
                    setBackground(Color.white);
                    setCaretColor(Color.black);
                }
            }
        }
        else{
            // a Normal Document. I'll use a DefaultStyledDocument
            setDocument(new DefaultStyledDocument());
            setBackground(Color.white);
            setCaretColor(Color.black);
        }

        loadFile(file);


        undoF = new UndoManager();
        undoF.setLimit(50000);
        Document doc = getDocument();

        doc.addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent evt) {
                undoF.addEdit(evt.getEdit());
                // adding a "*" to the file name, when the file has changed but not saved
                if (jframe.getTabbed().getComponentCount()!=0  && jframe.getCurrentFilename().indexOf("*")==-1){
                    jframe.getTabbed().setTitleAt( jframe.getTabbed().getSelectedIndex(), jframe.getCurrentFilename()+"*");
                    jframe.setTitle(jframe.getJifVersion() +" - " + jframe.getCurrentFilename());                    
                }
            }
        });

        getActionMap().put("Undo", new AbstractAction("Undo") {
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (undoF.canUndo()) {
                        while (undoF.getUndoPresentationName().equals(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_STYLE_CHANGED_UNDO"))){
                            undoF.undo();
                        }
                        undoF.undo();
                        // adding a "*" to the file name, when the file has changed but not saved
                        if (jframe.getTabbed().getComponentCount()!=0  && jframe.getCurrentFilename().indexOf("*")==-1){
                            jframe.getTabbed().setTitleAt( jframe.getTabbed().getSelectedIndex(), jframe.getCurrentFilename()+"*");
                            jframe.setTitle(jframe.getJifVersion() +" - " + jframe.getCurrentFilename());                    
                        }
                    }
                } catch (CannotUndoException e) {
                    System.out.println(e.getMessage());
                }
            }
        });

        getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");

        getActionMap().put("Redo", new AbstractAction("Redo") {
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (undoF.canRedo()) {
                        while (undoF.getRedoPresentationName().equals(java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_STYLE_CHANGED_REDO"))){
                            undoF.redo();
                        }
                        undoF.redo();
                        // adding a "*" to the file name, when the file has changed but not saved
                        if (jframe.getTabbed().getComponentCount()!=0  && jframe.getCurrentFilename().indexOf("*")==-1){
                            jframe.getTabbed().setTitleAt( jframe.getTabbed().getSelectedIndex(), jframe.getCurrentFilename()+"*");
                            jframe.setTitle(jframe.getJifVersion() +" - " + jframe.getCurrentFilename());                                                
                        }
                    }
                } catch (CannotRedoException e) {
                //System.out.println(e.getMessage());
                }
            }
        });

        getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");


        // New file (file==null)
        if (file==null){
            addKeyListener(new EditorKeyAdapter(jframe, this));
        }
        else {
            // Syntax highlighting is used only with .inf and .h files
            if (pathfile.endsWith(".inf")||(pathfile.endsWith(".h"))){
                addKeyListener(new EditorKeyAdapter(jframe, this));
            }
        }

        // Add Mouse Listener for the right-mouse popup
        addMouseListener(popupListener);
        
        addCaretListener(new CaretListener(){
		public void caretUpdate(CaretEvent ce){
			int pos = getCaretPosition();
			Element map = getDocument().getDefaultRootElement();
			int row = map.getElementIndex(pos);
			Element lineElem = map.getElement(row);
			int col = pos - lineElem.getStartOffset();
			jframe.jTextFieldRowCol.setText((row+1)+" | "+(col+1));
		}
	});
    }



    /**
     * Load text from a file into a JIFTextPane
     * @param file The file to load into JIFTextPane
     */
    public void loadFile(File file){
        if (null!=file){
            try{
                StringBuffer sb = new StringBuffer();
                String riga;
                sb.setLength(0);
                BufferedReader br = new BufferedReader(new FileReader(file));
                while ((riga = br.readLine())!=null){
                    sb.append(riga).append("\n");
                }
                br.close();
                setText(sb.toString());
            }catch(Exception e){
                System.out.println("ERR: " + e.getMessage());
                e.printStackTrace();
                System.err.println(e.getMessage());
            }
        }
    }



    /**
     * Returns the current row (as a String) from the current
     * Caret position
     * @return the current caret row
     */
   public String getCurrentRow(){
       String lastRow = null;
        try{
            Element el = getDocument().getDefaultRootElement();
            int ind = el.getElementIndex(getCaretPosition()-1);
            el = getDocument().getDefaultRootElement().getElement(ind);
            lastRow = getText(el.getStartOffset(), el.getEndOffset()-el.getStartOffset());
        } catch(BadLocationException e){
            System.err.println(e.getMessage());
        }
        return lastRow;
    }


   /**
    * Returns the current row (as a String) from a given Caret position
    * @param posizione The position of Caret
    * @return The row at the position
    */
   public String getRowAt(int posizione){
       String lastRow = null;
        try{
            Element el = getDocument().getDefaultRootElement();
            int ind = el.getElementIndex(posizione);
            el = getDocument().getDefaultRootElement().getElement(ind);
            lastRow = getText(el.getStartOffset(), el.getEndOffset()-el.getStartOffset());
        } catch(BadLocationException e){
            System.err.println(e.getMessage());
        }
        return lastRow;
    }

  /**
    * Returns the current row (as a String to UPPER CASE) from a given Caret position
    * @param posizione The position of Caret
    * @return The row at the position
    */
   public String getUpperRowAt(int posizione){
       String lastRow = null;
        try{
            Element el = getDocument().getDefaultRootElement();
            int ind = el.getElementIndex(posizione);
            el = getDocument().getDefaultRootElement().getElement(ind);
            lastRow = getText(el.getStartOffset(), el.getEndOffset()-el.getStartOffset());
        } catch(BadLocationException e){
            System.err.println(e.getMessage());
            System.err.println("position = "+posizione);
        }
        return lastRow.toUpperCase();
    }


    void searchCloseBraket(String start, String end){
        try{
             int posizioneIniziale = getCaretPosition()-1;
             int c=1;
             int opened=0;  // number of open brackets
             boolean found=false;

             while (!found){
                if ((getText(posizioneIniziale + c,1).equals(end))&&(opened==0)){
                    found = true;
                }
                // if an open bracket is found, opened++
                if (getText(posizioneIniziale + c,1).equals(start)){
                    opened++;
                }
                // if a closed bracket is found, opened--
                if (getText(posizioneIniziale + c,1).equals(end)&&(opened!=0)) {
                    opened--;
                }
                c++;
             }
             if (found){
                 hlighterBrackets.highlightFromTo(this, posizioneIniziale+c-1, posizioneIniziale+c);
             }
         } catch (Exception e){
             if (jframe.jCheckBoxAutomaticCheckBrackets.isSelected()){
                JOptionPane.showMessageDialog(null,
                java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_CHECK_BRACKET_ISNT_CLOSE"),
                "Warning", JOptionPane.ERROR_MESSAGE);
             }
         }
    }



    // Seek and highlight open brackets
    void searchOpenBraket(String start, String end){
        try{
             int posizioneIniziale = getCaretPosition()-1;
             int c=1;
             int closed=0;  // number of closed brackets
             boolean found=false;
             while (!found){
                if ((getText(posizioneIniziale - c,1).equals(end))&&(closed==0)){
                    found = true;
                }
                // if a closed bracket is found, closed++
                if (getText(posizioneIniziale - c,1).equals(start)){
                    closed++;
                }
                // if an open bracket is found, opened--
                if (getText(posizioneIniziale - c,1).equals(end)&&(closed!=0)) {
                    closed--;
                }
                c++;
             }

             if (found){
                 hlighterBrackets.highlightFromTo(this, posizioneIniziale-c+1, posizioneIniziale-c+2);
             }
         } catch (Exception e){
             if (jframe.jCheckBoxAutomaticCheckBrackets.isSelected()){
                JOptionPane.showMessageDialog(null,
                java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_CHECK_BRACKET_ISNT_OPEN"),
                "Warning", JOptionPane.ERROR_MESSAGE);
             }
         }
    }

    // Seek and highlight closed brackets
    int searchCloseBraket(String start, String end, int posizione){
        try{
             int posizioneIniziale = posizione;
             int c=1;
             int opened=0;  // number of opened brackets
             boolean found=false;
             while (!found){
                if ((getText(posizioneIniziale + c,1).equals(end))&&(opened==0)){
                    found = true;
                }
                // if an open bracket is found, opened++
                if (getText(posizioneIniziale + c,1).equals(start)){
                    opened++;
                }
                // if a closed bracket is found, opened--
                if (getText(posizioneIniziale + c,1).equals(end)&&(opened!=0)) {
                    opened--;
                }
                c++;
             }
             return 0;  // no errors

         } catch (Exception e){
            el = getDocument().getDefaultRootElement();
            int ind = el.getElementIndex(posizione);
            el = getDocument().getDefaultRootElement().getElement(ind);
            hlighterBrackets.highlightFromTo(this, el.getStartOffset(), el.getEndOffset());
            return -1;  // an error occurs
         }
    }

    // Seek and highlight the open bracket
    int searchOpenBraket(String start, String end, int posizione){
        try{
             int posizioneIniziale = posizione;
             int c=1;
             int closed=0;  // number of closed brackets
             boolean found=false;
             while (!found){
                if ((getText(posizioneIniziale - c,1).equals(end))&&(closed==0)){
                    found = true;
                }
                // if an open bracket is found, closed++
                if (getText(posizioneIniziale - c,1).equals(start)){
                    closed++;
                }
                // if a close bracket is found, closed--
                if (getText(posizioneIniziale - c,1).equals(end)&&(closed!=0)) {
                    closed--;
                }
                c++;
             }
             return 0;  // no errors
         } catch (Exception e){
            el = getDocument().getDefaultRootElement();
            int ind = el.getElementIndex(posizione);
            el = getDocument().getDefaultRootElement().getElement(ind);
            hlighterBrackets.highlightFromTo(this, el.getStartOffset(), el.getEndOffset());
            return -1;  // An error occurs
         }
    }


    /**
     * Performs a brackets validation.
     * If this find a incomplete bracket (not opened
     * or not closed), JIF will highlight the
     * wrong brackets to be fixed.
     * @param parent Instance of Main jFrame
     */
    public void checkbrackets(jFrame parent){

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
        while ((pos = testo.indexOf(pattern, pos)) >= 0)       {
            if (searchCloseBraket(pattern, close, pos)==-1) {
                errore = true;
                break;
            }
            pos += pattern.length();
        }
        if (errore){
            setCaretPosition(pos);
        }
        else{
            errore1=0;
        }

        // 2. Check the brackets [ opened
        pattern = "[";
        close = "]";
        pos = 0;
        errore= false;
        testo = getText();
        while ((pos = testo.indexOf(pattern, pos)) >= 0)       {
            if (searchCloseBraket(pattern, close, pos)==-1) {
                errore = true;
                break;
            }
            pos += pattern.length();
        }
        if (errore){
            setCaretPosition(pos);
        }
        else{
            errore2=0;
        }


        // 3. Check the brackets ( opened
        pattern = "(";
        close = ")";
        pos = 0;
        errore= false;
        testo = getText();
        while ((pos = testo.indexOf(pattern, pos)) >= 0)       {
            if (searchCloseBraket(pattern, close, pos)==-1) {
                errore = true;
                break;
            }
            pos += pattern.length();
        }
        if (errore){
            setCaretPosition(pos);
        }
        else{
            errore3=0;
        }



        // 4. Check the brackets } closed
        pattern = "}";
        close = "{";
        pos = 0;
        errore= false;


        while ((pos = testo.indexOf(pattern, pos)) >= 0)       {
            if (searchOpenBraket(pattern, close, pos)==-1) {
                errore = true;
                break;
            }
            pos += pattern.length();
        }
        if (errore){
            setCaretPosition(pos);
        }
        else{
            errore11=0;
        }


        // 5. Check the brackets ] closed
        pattern = "]";
        close = "[";
        pos = 0;
        errore= false;
        testo = getText();
        while ((pos = testo.indexOf(pattern, pos)) >= 0)       {
            if (searchOpenBraket(pattern, close, pos)==-1) {
                errore = true;
                break;
            }
            pos += pattern.length();
        }
        if (errore){
            setCaretPosition(pos);
        }
        else{
            errore22=0;
        }


        // 6. Check the brackets ) closed
        pattern = ")";
        close = "(";
        pos = 0;
        errore= false;
        testo = getText();
        while ((pos = testo.indexOf(pattern, pos)) >= 0)       {
            if (searchOpenBraket(pattern, close, pos)==-1) {
                errore = true;
                break;
            }
            pos += pattern.length();
        }
        if (errore){
            setCaretPosition(pos);
        }
        else{
            errore33=0;
        }
        if ((errore1 + errore2 + errore3 + errore11 + errore22 + errore33)==0){
            JOptionPane.showMessageDialog(parent,
            java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_CHECK_BRACKET_OK"),
            "OK", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    /**
     * Finds a String in the JIFTextPane and
     * highlight it.
     * The target String to be found, it taken from
     * the Search TextField
     */
    public void findString(){
        hlighter.removeHighlights(this);
        int pos = getCaretPosition();   // current position
        String pattern = jframe.jTextFieldFind.getText();

         try{
                String text = getDocument().getText(0, getDocument().getLength());
                boolean found = false;

                //while ( ( (pos = text.indexOf(pattern, pos)) >= 0) && (!found)) {
                while ( ( (pos = Utils.IgnoreCaseIndexOf(text,pattern, pos)) >= 0)  && (!found)) {
                    hlighter.highlightFromTo(this, pos, pos + pattern.length());
                    pos += pattern.length();
                    setCaretPosition(pos);
                    found=true;
                }

                // if not found, JIF shows a message to the user
                if (!found) {
                    String[] scelte = new String[2];
                    scelte[0] = java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF18");
                    scelte[1] = java.util.ResourceBundle.getBundle("JIF").getString("MESSAGE_CANCEL");
                    int result = JOptionPane.showOptionDialog(null,  java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF19")+" ["+pattern+"]"+java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF20") , java.util.ResourceBundle.getBundle("JIF").getString("STR_JIF21") , 0 , JOptionPane.INFORMATION_MESSAGE , null , scelte , scelte[1]);
                    if (result==0) {
                        setCaretPosition(0);
                        findString();
                    }
                    return;
                }
            } catch (BadLocationException e)  {
                System.out.println(e.getMessage());
                System.err.println(e.getMessage());
            }
    }


    /**
     * Returns the current Highlighter
     * @return the current Highlighter
     */
    public HighlightText getHlighter(){
        return this.hlighter;
    }

    /**
     * Remove current highlighter from the JIFTextPane
     */
    public void removeHighlighter(){
        if (null != this.hlighter){
            this.hlighter.removeHighlights(this);
        }
    }

    /**
     * Remove all the check-brackets highlighters from
     * current JIFTextPane
     */
    public void removeHighlighterBrackets(){
        if (null != this.hlighterBrackets){
            this.hlighterBrackets.removeHighlights(this);
        }
    }


    /**
     * This method will fix the current selection
     * before performing a SHIFT RIGHT/LEFT
     * COMMENT/UNCOMMENT action
     */
    public void selectionText(){
         if (null == getSelectedText()) {
            // Select the current row
            Element el = getDocument().getDefaultRootElement();
            int ind = el.getElementIndex(getCaretPosition());
            el = getDocument().getDefaultRootElement().getElement(ind);
            setSelectionStart(el.getStartOffset());
            setSelectionEnd(el.getEndOffset()-1);
        }
        else{
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
    public void commentSelection(){

        selectionText();

        String riga="";
        StringBuffer output = new StringBuffer();
        int start   = getSelectionStart();
        int end     = getSelectionEnd();
        Element el  = getDocument().getDefaultRootElement();

        int index_start = el.getElementIndex(start);
        int index_end = el.getElementIndex(end);
        try{
            for (int i=index_start; i<index_end ;i++){
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

        } catch(BadLocationException e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
    }



    /**
     * Remove Comment from the current text selection
     */
    public void unCommentSelection(){

        selectionText();

        String riga="";
        StringBuffer output = new StringBuffer();
        int start = getSelectionStart();
        int end = getSelectionEnd();
        el = getDocument().getDefaultRootElement();

        int index_start = el.getElementIndex(start);
        int index_end = el.getElementIndex(end);
        boolean error = false;  // if true, at least one row starts with !
        try{
            for (int i=index_start; i<index_end ;i++){
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
        }
        else {
            JOptionPane.showMessageDialog(null,java.util.ResourceBundle.getBundle("JIF").getString("JFRAME_UNCOMMENT_ERROR"),"Warning", JOptionPane.ERROR_MESSAGE);
            return;
        }
        } catch(BadLocationException e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
    }

    /**
     * Right shift tab the selected text
     */
    public void tabSelection(){
        selectionText();

        String riga="";
        StringBuffer output = new StringBuffer();
        int start = getSelectionStart();
        int end = getSelectionEnd();
        el = getDocument().getDefaultRootElement();

        int index_start = el.getElementIndex(start);
        int index_end = el.getElementIndex(end);

        try{
        for (int i=index_start; i<index_end+1 ;i++){
            el = getDocument().getDefaultRootElement().getElement(i);
            riga = getText(el.getStartOffset(), el.getEndOffset()-el.getStartOffset());

            if (i== index_end){
                output.append("\t").append(riga.substring(0,riga.indexOf('\n')));
            }
            else{
                output.append("\t").append(riga);
            }
        }

        replaceSelection(output.toString());

        // Selected rows will be selected again
        requestFocus();
        setSelectionStart(start);
        setSelectionEnd(end + index_end - index_start +1);

        } catch(BadLocationException e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
    }

    /**
     * Left shift tab the selected text
     */
    public void removeTabSelection(){
	selectionText();

        String riga="";
        StringBuffer output = new StringBuffer();
        int start = getSelectionStart();
        int end = getSelectionEnd();
        el = getDocument().getDefaultRootElement();
        int index_start = el.getElementIndex(start);
        int index_end = el.getElementIndex(end);

        try{
        for (int i=index_start; i<index_end+1 ;i++){
            el = getDocument().getDefaultRootElement().getElement(i);
            riga = getText(el.getStartOffset(), el.getEndOffset()-el.getStartOffset());

            if (i == index_end){
                if (riga.charAt(0) == '\t' || riga.charAt(0) == ' '){
                    output.append(riga.substring(1,riga.indexOf('\n')));
                }
                else{
                    output.append(riga.substring(0,riga.indexOf('\n')));
                }
            }
            else {
                if (riga.charAt(0) == '\t' || riga.charAt(0) == ' '){
                    output.append(riga.substring(1));
                }
                else {
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

        } catch(BadLocationException e){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
    }






    /**
     * This method extracts all strings from source inform code
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
     * This file will be used by the "InsertTranslate()" method
     * to rescue the translations from translate.txt file and
     * merge into the current file to create a new file with
     * the translation ("translated.inf")
     * @param file The Output File (i.e. "translate.txt")
     */
    public void ExtractTranslate(File file){
        PrintStream ps;
        StringBuffer translate = new StringBuffer();
        String appoggio;
        String testo = getText();

        // controllo che non ci siano parentesi opened-closed
        testo = Utils.replace(testo,"\"\"","\" \"");

        StringTokenizer stok = new StringTokenizer(testo,"\"\"");
        stok.nextToken();
        while (stok.hasMoreTokens()){
            appoggio = stok.nextToken();
            // To eliminate cases like: ".", "   "
            if (
                !(appoggio.trim().equals("")) &&
                (appoggio.length()>1)
                ){
                translate.append("*****\n");
                translate.append(appoggio);
                translate.append("\n=====\n");
                translate.append(appoggio+"\n");
            }
            if (stok.hasMoreTokens()) {
                stok.nextToken();
            }
        }
        try{
            FileOutputStream fos = new FileOutputStream(file);
            ps = new PrintStream( fos );
            ps.println (translate.toString());
            ps.close();
            JOptionPane.showMessageDialog(null,"OK","Message", JOptionPane.INFORMATION_MESSAGE);
        } catch(IOException e ){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
    }




    /**
     * This method rescues the translations from translate.txt
     * file and merges into the current file to create a new file with
     * the translation ("translated.inf")
     * @param file Current file in the JIFTextPane
     * @param fileout The output file (i.e. "translated.inf")
     */
     public void InsertTranslate(File file, File fileout){
        String testo = getText();
        try{
                BufferedReader br = new BufferedReader(new FileReader(file));
                boolean chiave = false;
                String key = null;
                String obj = null;
                String riga;

                while ((riga = br.readLine())!=null){
                    if (riga.startsWith("*****")){
                        chiave=true;
                        if (obj!=null){
                            // serve come entry nel caso della prima key
                            // inserisco la chiave al passo precedente
                            if (key.startsWith("\n")) {
                                key = key.substring(1);
                            }
                            if (obj.startsWith("\n")) {
                                obj = obj.substring(1);
                            }
                            testo = Utils.replace(testo, key , obj);
                            key="";
                            obj="";
                        }
                    }
                    else if (riga.startsWith("=====")){
                        chiave=false;
                    }
                    else if (chiave==true){
                        // sto leggendo una chiave
                        if (key!=null){
                            key = key +"\n" +riga;
                        }
                        else {
                            key = riga;
                        }
                    }
                    else if (chiave==false){
                        // sto leggendo una chiave
                        if (obj!=null){
                            obj = obj +"\n" + riga;
                        }
                        else {
                            obj = riga;
                        }
                    }
                }
                br.close();
            } catch(IOException e){
                System.out.println(e.getMessage());
                System.err.println(e.getMessage());
            }

        // saving the output file
        PrintStream ps;
        try{
            FileOutputStream fos = new FileOutputStream(fileout);
            ps = new PrintStream( fos );
            ps.println (testo);
            ps.close();
            JOptionPane.showMessageDialog(null,"OK","Message", JOptionPane.INFORMATION_MESSAGE);
        } catch(IOException e ){
            System.out.println(e.getMessage());
            System.err.println(e.getMessage());
        }
    }

     public void nextBookmark(){
        // position = actual row
        int jumpto = 0;
        
         if(bookmarks.size()==0){
            return;
         }
        
               
        int row = getDocument().getDefaultRootElement().getElementIndex(getCaretPosition());

        for (Iterator ite=bookmarks.iterator(); ite.hasNext();){
            Integer ind = (Integer) ite.next();
            if (row < ind.intValue()){
                jumpto = ind.intValue(); 
                break;
            }
        }           

        // last bookmark
        if (jumpto == 0 && bookmarks.size() != 0){
            jumpto = ( (Integer)bookmarks.get(0)).intValue();
        }
        
        // Jump to the bookmark row
        Element ele = getDocument().getDefaultRootElement().getElement(jumpto);
        try{
            scrollRectToVisible(modelToView(getDocument().getLength()));                
            scrollRectToVisible(modelToView(ele.getStartOffset()));
            setCaretPosition(ele.getStartOffset());
        } catch (BadLocationException ble){
            System.err.println(ble);
        }         
     }
     
    public void applyBookmarks(){
        // Removing the hlighterBookmarks
        if (null != this.hlighterBookmarks){
            this.hlighterBookmarks.removeHighlights(this);
        }        
        // Repaint all the highlights
        Element element;
        for (Iterator ite=bookmarks.iterator(); ite.hasNext();){
            Integer ind = (Integer) ite.next();
            element = getDocument().getDefaultRootElement().getElement(ind.intValue());
            hlighterBookmarks.highlightFromTo(this, element.getStartOffset(), element.getEndOffset());                    
        }        
    }
        
     
    public void updateBookmark(Integer line) {
        if (this.bookmarks.contains(line)){
            this.bookmarks.remove(line);
            applyBookmarks();           
        }        
        else{
            this.bookmarks.add(line);    
            applyBookmarks();            
        }        
    }

    // returns the current Word using the caret Position
    public String getCurrentWord() throws BadLocationException {
        int start = Utilities.getWordStart(this, getCaretPosition());
        int end = Utilities.getWordEnd(this, getCaretPosition());
        String word = getDocument().getText(start, end-start);
        if(word.indexOf(".")!=-1)
            word=word.substring(0,word.lastIndexOf("."));
        //System.out.println( "Selected word: " + word );
        return word;
    }

}
