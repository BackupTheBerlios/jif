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
            // Automatic JUMP to object, if present into the object tree
            if (ke.getKeyCode() == ke.VK_J && ke.isControlDown()) {
                jframe.checkTree(jif.getCurrentWord());
            }
            
            
            // Automatic right shifting
            if (ke.getKeyCode() == ke.VK_ENTER) {
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

            if( (ke.getKeyCode() == ke.VK_RIGHT )&&(ke.isAltDown() ) ){
                jif.tabSelection();
            }

            if( (ke.getKeyCode() == ke.VK_LEFT )&&(ke.isAltDown() ) ){
                jif.removeTabSelection();
            }

            // Ignored keys
            if( (ke.getKeyCode()==ke.VK_DOWN)  ||  (ke.getKeyCode()==ke.VK_UP)   ||
            (ke.getKeyCode()==ke.VK_RIGHT) ||  (ke.getKeyCode()==ke.VK_LEFT) ||
            (ke.getKeyCode()==ke.VK_SHIFT) ||  (ke.getKeyCode()==ke.VK_END)  ||
            (ke.getKeyCode()==ke.VK_HOME)){

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
            if (jframe.jCheckBoxHelpedCode.isSelected()&&(ke.getKeyCode()==ke.VK_SPACE && ke.isControlDown() ) ){
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



            if( (ke.getKeyCode() == ke.VK_F )&&(ke.isControlDown() ) ){
                String selezione = jif.getSelectedText();
                if (selezione!=null){
                    jframe.jTextFieldFind.setText(selezione);
                }
                jif.findString();
            }

            // CTRL+INS for "copy" command
            else if( (ke.getKeyCode() == ke.VK_INSERT)&&(ke.isControlDown()) ){
                jframe.copyToClipBoard();
            }
            // SHIFT+INS for "paste" command
            else if( (ke.getKeyCode() == ke.VK_INSERT)&&(ke.isShiftDown()) ){
                String paste = jframe.getClipboard();
                if (paste!=null){
                    doc.insertString(jif.getCaretPosition() , paste , jframe.getAttr());
                }
            }


            // FUNCTION KEYS MANAGEMENT
            else if( (ke.getKeyCode() == ke.VK_F1 ) ){
//                comando = (String)jframe.getTastiFunzione().get("F1");
//                if (comando !=null){
//                    doc.insertString(jif.getCaretPosition() , comando , jframe.getAttr());
//                }
                // find current line
                int pos = jif.getCaretPosition();
                Element map = jif.getDocument().getDefaultRootElement();
                int row = map.getElementIndex(pos);
                jif.updateBookmark(new Integer(row));                
            }
            else if( (ke.getKeyCode() == ke.VK_F2 ) ){
                jif.nextBookmark();                
                
//                
//                comando = (String)jframe.getTastiFunzione().get("F2");
//                if (comando !=null){
//                    doc.insertString(jif.getCaretPosition() , comando , jframe.getAttr());
//                }
            }
            else if( (ke.getKeyCode() == ke.VK_F3 ) ){
                    comando = (String)jframe.getTastiFunzione().get("F3");
                    if (comando !=null){
                        doc.insertString(jif.getCaretPosition() , comando , jframe.getAttr());
                    }
             }
            else if( (ke.getKeyCode() == ke.VK_F4 ) ){
                    comando = (String)jframe.getTastiFunzione().get("F4");
                    if (comando !=null){
                        doc.insertString(jif.getCaretPosition() , comando , jframe.getAttr());
                    }
             }
            else if( (ke.getKeyCode() == ke.VK_F5 ) ){
                    comando = (String)jframe.getTastiFunzione().get("F5");
                    if (comando !=null){
                        doc.insertString(jif.getCaretPosition() , comando , jframe.getAttr());
                    }
             }
            else if( (ke.getKeyCode() == ke.VK_F6 ) ){
                    comando = (String)jframe.getTastiFunzione().get("F6");
                    if (comando !=null){
                        doc.insertString(jif.getCaretPosition() , comando , jframe.getAttr());
                    }
             }
            else if( (ke.getKeyCode() == ke.VK_F7 ) ){
                    comando = (String)jframe.getTastiFunzione().get("F8");
                    if (comando !=null){
                        doc.insertString(jif.getCaretPosition() , comando , jframe.getAttr());
                    }
             }
            else if( (ke.getKeyCode() == ke.VK_F9 ) ){
                    comando = (String)jframe.getTastiFunzione().get("F9");
                    if (comando !=null){
                        doc.insertString(jif.getCaretPosition() , comando , jframe.getAttr());
                    }
             }
            else if( (ke.getKeyCode() == ke.VK_F10 ) ){
                    comando = (String)jframe.getTastiFunzione().get("F10");
                    if (comando !=null){
                        doc.insertString(jif.getCaretPosition() , comando , jframe.getAttr());
                    }
             }



            // F11 and F12  Keys execute external programs
            else if( (ke.getKeyCode() == ke.VK_F11 ) ){
                Runtime rt = Runtime.getRuntime();
                proc = rt.exec((String)jframe.getTastiFunzione().get("F11"));
            }
            else if( (ke.getKeyCode() == ke.VK_F12 ) ){
                Runtime rt = Runtime.getRuntime();
                proc = rt.exec((String)jframe.getTastiFunzione().get("F12"));
            }

        } catch(Exception ble) {
            System.out.println(ble.getMessage());
        }
   }
    
}