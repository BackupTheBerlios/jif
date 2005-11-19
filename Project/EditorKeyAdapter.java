/*
 * EditorKeyAdapter.java
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

import java.awt.event.*;

import javax.swing.text.*;

/**
 * EditorKeyAdapter for the JIFTextPane
 * @author Alessandro Schillaci
 */
public class EditorKeyAdapter extends KeyAdapter {

    jFrame jframe;
    JIFTextPane jif;
    Element el;
    String ultima, ultima_word;
    Document doc;
    Process proc;
    String comando;

    /**
     * Creates a new EditorKeyAdapter
     * @param parent The jframe main instance
     * @param jif Current JIFTextPane
     */
    public EditorKeyAdapter(jFrame parent, JIFTextPane jif){
        this.jframe = parent;
        this.jif = jif;
        this.doc = jif.getDocument();
    }

    public void keyPressed(KeyEvent ke) {
        if (ke.getKeyChar() == KeyEvent.VK_TAB && !ke.isShiftDown()){
            try{
                if (jif.getSelectedText() == null){
                    doc.insertString(jif.getCaretPosition(), Utils.spacesForTab(jframe.spacesfortab), jframe.getAttr());   
                }                
                else{
                    jif.tabSelection();
                }
            } catch(BadLocationException e){
                System.out.println(e.getMessage());
            }
            ke.consume(); 
            return;
        }
        if (ke.getKeyChar() == KeyEvent.VK_TAB && ke.isShiftDown()){
            try{
                    jif.removeTabSelection();
            } catch(Exception e){
                System.out.println(e.getMessage());
            }
            ke.consume(); 
            return;
        }        
    }
    
    /**
     * keyTyped method for the Mapping characters Management.
     * When you type a character which has to be "mapped" into
     * ZSCII code, this method rescue the correct character to replace.
     * Example:
     * If user digits "è" character, this will be tranformed into the ZSCII format
     * "@`e"
     * @param ke The key typed by the user
     */
    public void keyTyped(KeyEvent ke) {
        if (jframe.jCheckBoxMappingLive.isSelected()){
            // If the current row is a comment, skip
            if (jif.getCurrentRow().indexOf("!")==-1){
                if (jframe.getMapping().containsKey(ke.getKeyChar()+"")){
                        try{
                            doc.insertString(jif.getCaretPosition(), (String)jframe.getMapping().get(ke.getKeyChar()+""), jframe.getAttr());
                        } catch(BadLocationException e){
                            System.out.println(e.getMessage());
                        }
                        ke.consume();
                }
            }
        }
    }


    /**
     *
     * @param ke The key released by the user
     */
    public void keyReleased(KeyEvent ke) {
        try {
            
            // Keyboard Mapping ALT
            if(ke.isAltDown()){                
                if(jframe.getAltkeys().containsKey(""+ke.getKeyChar())){
                    doc.insertString(jif.getCaretPosition() , (String)jframe.getAltkeys().get(""+ke.getKeyChar()) , jframe.getAttr());                    
                }
                
                // Commands to run
                if(jframe.getExecutecommands().containsKey(""+ke.getKeyChar())){
                    Runtime rt = Runtime.getRuntime();
                    proc = rt.exec((String)jframe.getExecutecommands().get(""+ke.getKeyChar()) );
                }                
            }
            
            
            // Automatic JUMP to object, if present into the object tree
            if (ke.getKeyCode() == KeyEvent.VK_J && ke.isControlDown()) {
                jframe.checkTree(jif.getCurrentWord());
            }
            
            
            // Automatic right shifting
            if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
                Element root = doc.getDefaultRootElement();
                Element e = root.getElement(root.getElementIndex(jif.getCaretPosition()-1));
                String tmp;
                try {
                    String s = doc.getText(e.getStartOffset(), e.getEndOffset() - e.getStartOffset());
                    int i;
                    int length = s.length();
                    for (i=0; i< length; i++) {
                        if (!Character.isWhitespace(s.charAt(i))) {
                            break;
                        }
                    }
                    tmp = s.substring(0,i);
                    if (tmp.endsWith("\n")) {
                        tmp = tmp.substring(0, tmp.length()-1);
                    }
                    doc.insertString(jif.getCaretPosition(),tmp, null);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    System.err.println(ex.getMessage());
                }
            }

            if( (ke.getKeyCode() == KeyEvent.VK_RIGHT )&&(ke.isAltDown() ) ){
                jif.tabSelection();
            }

            if( (ke.getKeyCode() == KeyEvent.VK_LEFT )&&(ke.isAltDown() ) ){
                jif.removeTabSelection();
            }

            // Ignored keys
            if( (ke.getKeyCode()==KeyEvent.VK_DOWN)  ||  (ke.getKeyCode()==KeyEvent.VK_UP)   ||
            (ke.getKeyCode()==KeyEvent.VK_RIGHT) ||  (ke.getKeyCode()==KeyEvent.VK_LEFT) ||
            (ke.getKeyCode()==KeyEvent.VK_SHIFT) ||  (ke.getKeyCode()==KeyEvent.VK_END)  ||
            (ke.getKeyCode()==KeyEvent.VK_HOME)){

                jif.removeHighlighterBrackets();

                // Search for open parenthesis
                if (jif.getText(jif.getCaretPosition()-1,1).equals("{")){
                    jif.searchCloseBraket("{","}");
                }
                if (jif.getText(jif.getCaretPosition()-1,1).equals("[")){
                    jif.searchCloseBraket("[","]");
                }
                if (jif.getText(jif.getCaretPosition()-1,1).equals("(")){
                    jif.searchCloseBraket("(",")");
                }

                // Search for closed parenthesis
                if (jif.getText(jif.getCaretPosition()-1,1).equals("}")){
                    jif.searchOpenBraket("}","{");
                }
                if (jif.getText(jif.getCaretPosition()-1,1).equals("]")){
                    jif.searchOpenBraket("]","[");
                }
                if (jif.getText(jif.getCaretPosition()-1,1).equals(")")){
                    jif.searchOpenBraket(")","(");
                }
                return;
            }


            // Assistant code
            if (jframe.jCheckBoxHelpedCode.isSelected()&&(ke.getKeyCode()==KeyEvent.VK_SPACE && ke.isControlDown() ) ){
                el = doc.getDefaultRootElement();
                int ind = el.getElementIndex(jif.getCaretPosition());
                el = doc.getDefaultRootElement().getElement(ind);
                ultima = jif.getText(el.getStartOffset(), el.getEndOffset()-el.getStartOffset()-1);
                int position = jif.getCaretPosition();

//System.out.println("ultima riga =["+ultima+"]");
                if (ultima.indexOf(" ")!=-1){
                    ultima_word=ultima.substring(ultima.lastIndexOf(" ")).trim();
                }
                else {
                    //TODO : calcolo il numero di spazi o di tab prima della word
                    ultima_word = ultima;
                }
                ultima_word = ultima_word.trim();
//System.out.println("ultima_word =["+ultima_word+"]");                
                comando = (String)jframe.getHelpCode().get(ultima_word);
                int positionCaret = comando.indexOf("@");
                comando = Utils.assistCode(comando);
                
                if (comando !=null){
                    jif.setSelectionStart(jif.getCaretPosition()-ultima_word.trim().length());
                    jif.setSelectionEnd(jif.getCaretPosition());
                    jif.replaceSelection(comando);
                    
                    // updates the caret position
                    if (positionCaret >0){
                        jif.setCaretPosition(position-ultima_word.trim().length()+positionCaret);
                    }
                }
            }



            if( (ke.getKeyCode() == KeyEvent.VK_F )&&(ke.isControlDown() ) ){
                String selezione = jif.getSelectedText();
                if (selezione!=null){
                    jframe.jTextFieldFind.setText(selezione);
                }
                jif.findString(jframe);
            }

            // CTRL+INS for "copy" command
            else if( (ke.getKeyCode() == KeyEvent.VK_INSERT)&&(ke.isControlDown()) ){
                jframe.copyToClipBoard();
            }
            // SHIFT+INS for "paste" command
            else if( (ke.getKeyCode() == KeyEvent.VK_INSERT)&&(ke.isShiftDown()) ){
                String paste = jFrame.getClipboard();
                if (paste!=null){
                    doc.insertString(jif.getCaretPosition() , paste , jframe.getAttr());
                }
            }

        } catch(Exception ble) {
            System.out.println(ble.getMessage());
        }
   }
    
}