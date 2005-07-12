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

public class JIFScrollPane extends JScrollPane{
    String filePath;
    /** Creates a new instance of JIFScrollPane */
    public JIFScrollPane(JIFTextPane aJIFTextPane,String aPath) {
        super(aJIFTextPane);
        this.setToolTipText(aPath);
        this.filePath=aPath;
    }
    String getFile(){
        return filePath;
    }
}
