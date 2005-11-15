/*
 * HighlightText.java
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

import javax.swing.text.*;
import java.awt.*;
import javax.swing.*;

/**
 * Class for Highlighting text in a JTextComponent
 * @author Alessandro Schillaci
 */
public class HighlightText extends DefaultHighlighter.DefaultHighlightPainter {

        JIFTextPane jif;

        /**
         * Creates an Highlighter Object for a JIFTextPane
         * @param jif The instance of JIFTextPane to apply the highlighting
         * @param color Color of highlighting
         */
	public HighlightText(JIFTextPane jif, Color color) {
            super(color);
            this.jif = jif;
        }

        /**
         * Creates an Highlighter Object for a generic JTextComponent
         * @param color Color of highlighting
         */
	public HighlightText(Color color) {
            super(color);
            this.jif = null;
        }

        /**
         * Highlight a string in a JIFTextPane
         * @param component Instance of JIFTextPane
         * @param pattern String to be highlighted
         */
        public void highlight(JIFTextPane component, String pattern)   {
            try{
                Highlighter hilite = component.getHighlighter();
                String text = jif.getText();
                int pos = 0;
                while ((pos = text.indexOf(pattern, pos)) >= 0)       {
                    hilite.addHighlight(pos, pos + pattern.length(), this);
                    pos += pattern.length();
                }
            } catch (BadLocationException e)  {
                System.out.println(e.getMessage());
                System.err.println(e.getMessage());
            }
        }

        // si può applicare a qlc JTextComponent
        /**
         * Highlight a string in a JTextComponent
         * @param component Instance of JTextComponent
         * @param start Start position to be highlighted
         * @param end End position to be highlighted
         */
        public void highlightFromTo(JTextComponent component, int start, int end)   {
            try{
                Highlighter hilite = component.getHighlighter();
                hilite.addHighlight(start, end , this);
            } catch (BadLocationException e)  {
                System.out.println(e.getMessage());
                System.err.println(e.getMessage());
            }
        }

        /**
         * Remove all the Highlights from a JTextComponent
         * @param component Instance of JTextComponent
         */
        public void removeHighlights(JTextComponent component)  {
            Highlighter hilite = component.getHighlighter();
            Highlighter.Highlight[] hilites = hilite.getHighlights();
            for (int i = 0; i < hilites.length; i++)     {
                if (hilites[i].getPainter() instanceof HighlightText){
                    hilite.removeHighlight(hilites[i]);
                }
            }
        }
        
    }
