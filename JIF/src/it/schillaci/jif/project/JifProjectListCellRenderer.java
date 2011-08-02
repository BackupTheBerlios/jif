package it.schillaci.jif.project;

/*
 * JifProjectListCellRenderer.java
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

import it.schillaci.jif.core.JifFileName;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.UIManager;

/**
 * List cell renderer to display JIF files in a JIF project list
 * This displays the file using <code>getName()</code> rather than 
 * <code>toString()</code>.
 * 
 * @author Peter Piggott
 * @version 1.0
 * @since JIF 3.2
 */
public class JifProjectListCellRenderer extends DefaultListCellRenderer {

    private static final long serialVersionUID = 6230808318112643998L;
    // List bullet icon
    private static Icon listIcon;

    public JifProjectListCellRenderer() {
        super();
        listIcon = new ImageIcon(getClass().getResource("/images/TREE_objects.png"));
    }

    // --- DefaultListCellRenderer methods -------------------------------------
    
    /**
     * Overriden to use <code>getName()</code> rather than <code>toString()</code>
     */
    public Component getListCellRendererComponent(JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
        
        setComponentOrientation(list.getComponentOrientation());

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        
        setIcon(listIcon);
       
        if (value instanceof JifFileName) {
            setText((value == null) ? "" : ((JifFileName) value).getName());
        } else {
            setText((value == null) ? "" : value.toString());
        }

        setEnabled(list.isEnabled());
        setFont(list.getFont());
        setBorder((cellHasFocus) ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);

        return this;
    }
    
    // --- Class methods -------------------------------------------------------
    
    public void setListIcon(Icon listIcon) {
        this.listIcon = listIcon;
    }
}
