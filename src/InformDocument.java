/*
 * InformDocument.java
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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;


/**
 * An extension of DefaultStyledDocument for Inform Syntax Highlighting
 * @author Alessandro Schillaci
 */
public class InformDocument extends DefaultStyledDocument{
    /**
     * 
     */
    private static final long serialVersionUID = 5856047697369563208L;
    
    /**
     * The parent JFrame of the textpane displaying this document.
     */
    private jFrame jframe;
    /**
     * The styles used to highlight the syntax of the source an Inform program
     */
    private Style normal;
    private Style attribute;
    private Style comment;
    private Style function;
    private Style keyword;
    private Style property;
    private Style string;
    private Style verb;
    private Style white;
    private Style word;
    
    /**
     * Creates an InformDocument with a default syntax highlighting.
     * @param parent The instance of jFrame Class
     */
    public InformDocument(jFrame parent){
        this.jframe = parent;
        
        putProperty( DefaultEditorKit.EndOfLineStringProperty, "\n" );
        
        normal = addStyle("normal", null);
        StyleConstants.setBold(normal, false);
        StyleConstants.setFontFamily(normal, jframe.defaultFont.getName());
        StyleConstants.setFontSize(normal, jframe.defaultFont.getSize());
        StyleConstants.setForeground(normal, jframe.colorNormal);
        
        attribute = addStyle("attribute", normal);
        StyleConstants.setBold(attribute, true);
        StyleConstants.setForeground(attribute, jframe.colorAttribute);
        
        comment = addStyle("comment", normal);
        StyleConstants.setBold(comment, true);
        StyleConstants.setForeground(comment, jframe.colorComment);
        
        function = addStyle("function", normal);
        StyleConstants.setBold(function, false);
        StyleConstants.setForeground(function, Color.RED);
        
        keyword = addStyle("keyword", normal);
        StyleConstants.setBold(keyword, true);
        StyleConstants.setForeground(keyword, jframe.colorKeyword);
        
        property = addStyle("property", normal);
        StyleConstants.setBold(property, true);
        StyleConstants.setForeground(property, jframe.colorProperty);
        
        string = addStyle("string", normal);
        StyleConstants.setBold(string, false);
        StyleConstants.setForeground(string, jframe.colorString);
        
        verb = addStyle("verb", normal);
        StyleConstants.setBold(verb, true);
        StyleConstants.setForeground(verb, jframe.colorVerb);
        
        white = addStyle("white", normal);
        StyleConstants.setBold(white, false);
        StyleConstants.setForeground(white, Color.RED);

        word = addStyle("word", normal);
        StyleConstants.setBold(word, true);
        StyleConstants.setForeground(word, jframe.colorString);        
    }
    
    /**
     * Insert a string with syntax highlighting
     * @param offset The offset to insert the string
     * @param str The String to be inserted into the document
     * @param a The AttributeSet for the String
     * @throws BadLocationException If the insert action fails
     */
    public void insertString(int offset, String str, AttributeSet a)
            throws BadLocationException {

        super.insertString(offset, str, normal);
        processChangedLines(offset, str.length());

    }

    /**
     * Remove a String from a document
     * 
     * @param offset
     *            The initial offset
     * @param length
     *            Length of the String to be removed
     * @throws BadLocationException
     *            If the remove action fails
     */
    public void remove(int offset, int length) throws BadLocationException {

        super.remove(offset, length);
        processChangedLines(offset, length);

    }

    /**
     * Determine the area of the document whose syntax highlighting is impacted
     * by the change of content
     */
    public void processChangedLines(int offset, int length)  throws BadLocationException {

        Element line;
        int startOffset = offset;
        int changeOffset = offset;
        int changeLength = length;
        MutableAttributeSet highlight = normal;
        MutableAttributeSet tokenCheck = normal;
        
        // Locate start of first highlight token at insert/remove offset 
        
        if (changeOffset>0) {
            
            // Check for the element before the insertion/removal point (offset-1) so if the
            // insertion/removal point is at the boundary of two elements we get the previous
            // highlight token not the following highlight token 
            
            Element token = getCharacterElement(offset-1);
            startOffset = token.getStartOffset();
            highlight = (MutableAttributeSet) token.getAttributes();
            
            tokenCheck = getStyle((String)highlight.getAttribute(AttributeSet.NameAttribute));
            
            while (highlight.containsAttributes(tokenCheck) && changeOffset>0) {
                changeOffset = startOffset;
                token = getCharacterElement(changeOffset-1);
                startOffset = token.getStartOffset();
                highlight = (MutableAttributeSet) token.getAttributes();
            }
        }
        
        // Find the length of the text in the document impacted by the insert/remove. 
        // The length of the text between the start of the first highlight token at the insert point
        // to the end of the current line plus the length of the text being inserted into the document.
        
        if (length>0) {
            line = getParagraphElement(offset+length);
        } else {
            line = getParagraphElement(offset);
        }
        changeLength = line.getEndOffset()-changeOffset; 

        applyHighlighting(changeOffset, changeLength);

    }
    /**
     * Apply inform syntax highlighting to the specified portion of the document
     * @param changeOffset The initial offset at which to apply syntax highlighting
     * @param changeLength The length of the document text to be highlighted
     * @throws BadLocationException If process changes fails
     */
    public void applyHighlighting(int changeOffset, int changeLength) throws BadLocationException {
        
        int startOffset;
        int endOffset;
        int length;

        String source = getText(changeOffset, changeLength);
        InformLexer lexer = new InformLexer(source, changeOffset);
        InformToken token = lexer.nextElement();
        
        while(token.getType()!=InformToken.EOS) {
            
            startOffset=token.getStartPosition();
            endOffset=token.getEndPosition();
            length=endOffset-startOffset;
            
            switch(token.getType()) {
                case InformToken.COMMENT:
                    setCharacterAttributes(startOffset, length, comment, true);
                    break;
                case InformToken.STRING:
                    setCharacterAttributes(startOffset, length, string, true);
                    break;
                case InformToken.SYMBOL:
                    if (isKeyword(token.getContent())) {
                        setCharacterAttributes(startOffset, length, keyword, true);
                    } else {
                        if (isAttribute(token.getContent())) {
                            setCharacterAttributes(startOffset, length, attribute, true);
                        } else {
                            if (isProperty(token.getContent())) {
                                setCharacterAttributes(startOffset, length, property, true);
                            } else {
                                if (isVerb(token.getContent())) {
                                    setCharacterAttributes(startOffset, length, verb, true);
                                } else {
                                    setCharacterAttributes(startOffset, length, normal, true);
                                }
                            }
                        }
                    }
                    break;
                case InformToken.WHITESPACE:
                    setCharacterAttributes(startOffset, length, white, true);
                    break;
                case InformToken.WORD:
                    setCharacterAttributes(startOffset, length, word, true);
                    break;
                default:
                    setCharacterAttributes(startOffset, length, normal, true);
                break;
            }
            token=lexer.nextElement();
        }
    }
    
    private boolean isKeyword(String token){
        if (jframe.getKeywords_cs().contains(token)){
            return true;
        } else if (
                jframe.getAttributes_cs().contains(token) ||
                jframe.getProperties_cs().contains(token) ||
                jframe.getVerbs_cs().contains(token)
        ){
            return false;
        } else return jframe.getKeywords().contains( token.toLowerCase() );
    }
    
    private boolean isAttribute(String token){
        if (jframe.getAttributes_cs().contains(token)){
            return true;
        } else if (
                jframe.getKeywords_cs().contains(token) ||
                jframe.getProperties_cs().contains(token) ||
                jframe.getVerbs_cs().contains(token)
        ){
            return false;
        } else return jframe.getAttributes().contains( token.toLowerCase() );
    }
    
    private boolean isProperty(String token){
        if (jframe.getProperties_cs().contains(token)){
            return true;
        } else if (
                jframe.getKeywords_cs().contains(token) ||
                jframe.getAttributes_cs().contains(token) ||
                jframe.getVerbs_cs().contains(token)
        ){
            return false;
        } else return jframe.getProperties().contains( token.toLowerCase() );
    }
    
    private boolean isVerb(String token){
        if (jframe.getVerbs_cs().contains(token)){
            return true;
        } else if (
                jframe.getKeywords_cs().contains(token) ||
                jframe.getAttributes_cs().contains(token) ||
                jframe.getProperties_cs().contains(token)
        ){
            return false;
        } else return jframe.getVerbs().contains( token.toLowerCase() );
    }
    
    
    protected String addMatchingBrace(String brace,int offset) throws BadLocationException {
        StringBuffer whiteSpace = new StringBuffer();
        int line = getDefaultRootElement().getElementIndex(offset);
        int i = getDefaultRootElement().getElement(line).getStartOffset();
        while (true) {
            String temp = getText(i, 1);
            if (temp.equals(" ") || temp.equals("\t")) {
                whiteSpace.append(temp);
                i++;
            } else
                break;
        }
        return (brace.equals("{")?"{":"[")+"\n" + whiteSpace.toString() + whiteSpace.toString() + "\n"
        + whiteSpace.toString() + (brace.equals("{")?"}":"]");
    }
}
