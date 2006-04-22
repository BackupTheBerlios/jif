/*
 * Inspect.java
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

/**
 * A Class for The Object Tree
 * @author Alessandro Schillaci
 */
   public class Inspect {
       /**
        * The Inform keyword to be highlighted when selected from
        * the Object Tree
        */
        public String Ilabel;   //pattern
        /**
         * Initial position of the Keyword
         */
        public int Iposition;   //posizione iniziale


        /**
         * Creates a new Inspect Object.
         * The object tree is a tree of Inspect objects
         * @param label The keyword
         * @param position Position of keyword in the inform document
         */
        public Inspect(String label, int position) {
            Ilabel=label;
            Iposition = position;
        }

        /**
         * This method is used by the Object Tree
         * @return The keyword
         */
        public String toString() {
            return Ilabel;
        }
    }
