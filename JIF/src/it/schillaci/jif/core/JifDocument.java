package it.schillaci.jif.core;

/*
 * JifDocument.java
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

import it.schillaci.jif.inform.InformSyntax;
import it.schillaci.jif.inform.InformContext;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.GapContent;
import javax.swing.text.StyleContext;

/**
 * An extension of Jif Document for Inform Syntax Highlighting
 * 
 * @author Peter Piggott
 * @version 1.0
 * @since JIF 3.1
 */
public class JifDocument extends DefaultStyledDocument {

    /**
     * 
     */
    private static final long serialVersionUID = 1556152317453621340L;
    
    private static int tabSize = InformContext.defaultTabSize;
    private static final String defaultComment = "!";

    /**
     * Constructs a Jif document with a shared set of styles for syntax
     * highlighting.
     * 
     * @param c
     *            the container for the content
     * @param styles
     *            the set of styles for Inform syntax highlighting which may be
     *            shared across documents
     */
    public JifDocument(Content c, StyleContext styles) {
        super(c, styles);
        putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
    }
    
    public JifDocument(InformContext styles) {
        this(new GapContent(BUFFER_SIZE_DEFAULT), (StyleContext)styles);
    }
    
    public JifDocument() {
        this(new GapContent(BUFFER_SIZE_DEFAULT), (StyleContext) new InformContext());
    }

    /**
     * Insert a string with syntax highlighting
     * @param offset
     *            The offset to insert the string
     * @param str 
     *            The String to be inserted into the document
     * @param a 
     *            The AttributeSet for the String
     * @throws BadLocationException 
     *            If the insert action fails
     */
    public void insertString(int offset, String str, AttributeSet a)
            throws BadLocationException {

        super.insertString(offset, str, getStyle(InformSyntax.Normal.getName()));

    }

    // --- Class methods ----------------------------------------------------
    /**
     * get the tab size for indenting Jif documents
     * 
     * @param tabSize
     *            The number of spaces in one level of indenting
     */
    public static int getTabSize() {
        return tabSize;
    }

    /**
     * Set the tab size for indenting Jif documents
     * 
     * @param tabSize
     *            The number of spaces in one level of indenting
     */
    public static void setTabSize(int tabSize) {
        JifDocument.tabSize = tabSize;
    }
}
