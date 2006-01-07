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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * An extension of DefaultStyledDocument for the Inform Syntax Highlight
 * @author Alessandro Schillaci
 */
public class InformDocument extends DefaultStyledDocument{
    /**
     *
     */
    private static final long serialVersionUID = 5856047697369563208L;
    jFrame jframe;
    MutableAttributeSet normal;
    MutableAttributeSet keyword;
    MutableAttributeSet attribute;
    MutableAttributeSet property;
    MutableAttributeSet verb;
    MutableAttributeSet comment;
    MutableAttributeSet quote;
    
    /**
     * Creates an InformDocument with a default syntax highlighting.
     * @param parent The instance of jFrame Class
     */
    public InformDocument(jFrame parent){
        this.jframe = parent;
        
        putProperty( DefaultEditorKit.EndOfLineStringProperty, "\n" );
        normal = new SimpleAttributeSet();
        StyleConstants.setFontFamily(normal , jframe.defaultFont.getName());
        StyleConstants.setFontSize(normal, jframe.defaultFont.getSize());
        StyleConstants.setForeground(normal, jframe.colorNormal);
        
        comment = new SimpleAttributeSet();
        //StyleConstants.setItalic(comment, true);
        StyleConstants.setBold(comment, true);
        StyleConstants.setForeground(comment, jframe.colorComment);
        
        keyword = new SimpleAttributeSet();
        StyleConstants.setBold(keyword, true);
        StyleConstants.setForeground(keyword, jframe.colorKeyword);
        
        attribute = new SimpleAttributeSet();
        StyleConstants.setForeground(attribute, jframe.colorAttribute);
        StyleConstants.setBold(attribute, true);
        
        property = new SimpleAttributeSet();
        StyleConstants.setForeground(property, jframe.colorProperty);
        StyleConstants.setBold(property, true);
        
        verb = new SimpleAttributeSet();
        StyleConstants.setForeground(verb, jframe.colorVerb);
        StyleConstants.setBold(verb, true);
        
        quote = new SimpleAttributeSet();
        StyleConstants.setForeground(quote, jframe.colorNormal);
        StyleConstants.setBold(quote, true);
        
    }
    
    /**
     * Insert a string with syntax highlighting
     * @param offset The offset to insert the string
     * @param str The String to be inserted in the document
     * @param a The AttributeSet for the String
     * @throws BadLocationException If the insert action fails
     */
    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException{
        if (str.equals("{")){
            str = addMatchingBrace("{",offset);
        }
        if (str.equals("[")){
            str = addMatchingBrace("[",offset);
        }
        super.insertString(offset, str, a);
        processChangedLines(offset, str.length());
    }
    
    /**
     * Remove a String from a document
     * @param offset The initial offset
     * @param length Length of the String to be removed
     * @throws BadLocationException If the remove action fails
     */
    public void remove(int offset, int length) throws BadLocationException{
        super.remove(offset, length);
        processChangedLines(offset, 0);
    }
    
    /**
     * Process Changes in lines
     * @param offset The initial offset
     * @param length Length of string to be processed
     * @throws BadLocationException If process changes fails
     */
    public void processChangedLines(int offset, int length) throws BadLocationException{
        String content = getText(0, getLength());
        
        Element root = getDefaultRootElement();
        int startLine = root.getElementIndex( offset );
        int endLine = root.getElementIndex( offset + length );
        
        for (int i = startLine; i <= endLine; i++){
            int startOffset = root.getElement( i ).getStartOffset();
            int endOffset = root.getElement( i ).getEndOffset();
            applyHighlighting(content, startOffset, endOffset - 1);
        }
    }
    
    
    
    /**
     * Apply the syntax highlighting to a String
     * @param content The content text to be processed
     * @param startOffset Initial offset
     * @param endOffset Final offset
     * @throws BadLocationException If process fails
     */
    public void applyHighlighting(String content, int startOffset, int endOffset) throws BadLocationException{
        int index;
        int lineLength = endOffset - startOffset;
        int contentLength = content.length();
        if (endOffset >= contentLength)	{
            endOffset = contentLength - 1;
        }
        
        //  set normal attributes for the line
        setCharacterAttributes(startOffset, lineLength, normal, true);
        
        
        //  check for single line comment
        String singleLineDelimiter = "!";
        index = content.indexOf( singleLineDelimiter, startOffset );
        
//                    if ( (index > -1) && (index < endOffset) ){
//                        setCharacterAttributes(index, endOffset - index + 1, comment, false);
//                        endOffset = index - 1;
//                    }
        
        
        // if there's a "!" in the String
        if ( (index > -1) && (index < endOffset) ){
            int numpar = Utils.numberOfBrackets(content.substring(0,index));
            if ((numpar%2) == 1){
                setCharacterAttributes(index, endOffset - index + 1, normal, false);
            } else{
                setCharacterAttributes(index, endOffset - index + 1, comment, false);
            }
            endOffset = index - 1;
        }
        //  check for tokens
        checkForTokens(content, startOffset, endOffset);
    }
    
    
    
    private void checkForTokens(String content, int startOffset, int endOffset){
        while (startOffset <= endOffset){
            //  find the start of a new token
            while ( isDelimiter( content.substring(startOffset, startOffset + 1) ) ){
                if (startOffset < endOffset) {
                    startOffset++;
                } else{
                    return;
                }
            }
            if ( isQuoteDelimiter( content.substring(startOffset, startOffset + 1) ) ){
                startOffset = getQuoteToken(content, startOffset, endOffset);
            } else {
                startOffset = getOtherToken(content, startOffset, endOffset);
            }
        }
    }
    
    private boolean isDelimiter(String character){
        String operands = "\";:{}()[]+-/%<=>&|^~*,.";
        if (Character.isWhitespace( character.charAt(0) ) || operands.indexOf(character) != -1 ){
            return true;
        } else{
            return false;
        }
    }
    
    private boolean isQuoteDelimiter(String character){
        String quoteDelimiters = "'";
        if (quoteDelimiters.indexOf(character) == -1) {
            return false;
        } else{
            return true;
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
    
    
    private int getQuoteToken(String content, int startOffset, int endOffset){
        String quoteDelimiter = content.substring(startOffset, startOffset + 1);
        String escapedDelimiter = Constants.SEP + quoteDelimiter;
        
        int index;
        int endOfQuote = startOffset;
        
        //  skip over the escaped quotes in this quote
        index = content.indexOf(escapedDelimiter, endOfQuote + 1);
        while ( (index > -1) && (index < endOffset) ){
            endOfQuote = index + 1;
            index = content.indexOf(escapedDelimiter, endOfQuote);
        }
        
        // now find the matching delimiter
        index = content.indexOf(quoteDelimiter, endOfQuote + 1);
        if ( (index == -1) || (index > endOffset) ){
            endOfQuote = endOffset;
        } else{
            endOfQuote = index;
        }
        
        setCharacterAttributes(startOffset, endOfQuote - startOffset + 1, quote, false);
        return endOfQuote + 1;
    }
    
    private int getOtherToken(String content, int startOffset, int endOffset){
        int endOfToken = startOffset + 1;
        while ( endOfToken <= endOffset ){
            if ( isDelimiter( content.substring(endOfToken, endOfToken + 1))){
                break;
            }
            endOfToken++;
        }
        
        String token = content.substring(startOffset, endOfToken);
        int numpar;     // number of "
        //System.out.println( "found: " + token );
        
        if ( isKeyword( token )){
            numpar = Utils.numberOfBrackets(content.substring(0,startOffset));
            if ((numpar%2) != 1){
                setCharacterAttributes(startOffset, endOfToken - startOffset, keyword, false);
            }
        } else if ( isAttribute( token ) ){
            numpar = Utils.numberOfBrackets(content.substring(0,startOffset));
            if ((numpar%2) != 1){
                setCharacterAttributes(startOffset, endOfToken - startOffset, attribute, false);
            }
        } else if ( isProperty( token ) ){
            numpar = Utils.numberOfBrackets(content.substring(0,startOffset));
            if ((numpar%2) != 1){
                setCharacterAttributes(startOffset, endOfToken - startOffset, property, false);
            }
        } else if ( isVerb( token ) ){
            numpar = Utils.numberOfBrackets(content.substring(0,startOffset));
            if ((numpar%2) != 1){
                setCharacterAttributes(startOffset, endOfToken - startOffset, verb, false);
            }
        }
        return endOfToken + 1;
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
