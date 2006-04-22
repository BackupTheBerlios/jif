/*
 * InformToken.java - Lexical token for the inform language
 *
 * This file is an experimental part of JIF.
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
 * InformToken: Stores the information about a lexical token
 *
 * @author Peter Piggott
 * @version Revision: 1.0
 * @since 3.1
 */

public class InformToken {

// Lexical token id numbers

   private static final byte LOWEST = 0;
   private static final byte HIGHEST = 58;

   public static final byte EOS = 0;

   public static final byte AMPERSAND = 1;
   public static final byte AND = 2;
   public static final byte ANDAND = 3;
   public static final byte ARROW = 4;
   public static final byte ARROWARROW = 5;
   public static final byte ARROWEQUAL = 6;
   public static final byte BINARY = 7;
   public static final byte CLOSEBRACE = 8;
   public static final byte CLOSEBRACKET = 9;
   public static final byte CLOSEROUTINE = 10;
   public static final byte COLON = 11;
   public static final byte COLONCOLON = 12;
   public static final byte COMMA = 13;
   public static final byte COMMENT = 14;
   public static final byte DIVIDE = 15;
   public static final byte DOT = 16;
   public static final byte DOTAMPERSAND = 17;
   public static final byte DOTHASH = 18;
   public static final byte DOTDOT = 19;
   public static final byte DOTDOTAMPERSAND = 20;
   public static final byte DOTDOTHASH = 21;
   public static final byte EQUAL = 22;
   public static final byte EQUALARROW = 23;
   public static final byte EQUALEQUAL = 24;
   public static final byte HASH = 25;
   public static final byte HASHHASH = 26;
   public static final byte HASHADOLLAR = 27;
   public static final byte HASHNDOLLAR = 28;
   public static final byte HASHRDOLLAR = 29;
   public static final byte HASHWDOLLAR = 30;
   public static final byte HEXIDECIMAL = 31;
   public static final byte INVALID = 32;
   public static final byte LESS = 33;
   public static final byte LESSEQUAL = 34;
   public static final byte LESSLESS = 35;
   public static final byte MINUS = 36;
   public static final byte MINUSARROW = 37;
   public static final byte MINUSMINUS = 38;
   public static final byte MINUSMINUSARROW = 39;
   public static final byte NEWLINE = 40;
   public static final byte NOT = 41;
   public static final byte NOTEQUAL = 42;
   public static final byte NOTNOT = 43;
   public static final byte NUMBER = 44;
   public static final byte OPENBRACE = 45;
   public static final byte OPENBRACKET = 46;
   public static final byte OPENROUTINE = 47;
   public static final byte OR = 48;
   public static final byte OROR = 49;
   public static final byte PLUS = 50;
   public static final byte PLUSPLUS = 51;
   public static final byte REMAINDER = 52;
   public static final byte SEMICOLON = 53;
   public static final byte STRING = 54;
   public static final byte SYMBOL = 55;
   public static final byte TIMES = 56;
   public static final byte WHITESPACE = 57;
   public static final byte WORD = 58;


// Lexical token names

   private static final String name[] = 
      {"end-of-file",
       "ampersand",
       "and",
       "and-and",
       "arrow",
       "arrow-arrow",
       "arrow-equal",
       "binary",
       "close-brace",
       "close-bracket",
       "close-routine",
       "colon",
       "colon-colon",
       "comma",
       "comment",
       "divide",
       "dot",
       "dot-ampersand",
       "dot-hash",
       "dot-dot",
       "dot-dot-ampersand",
       "dot-dot-hash",
       "equal",
       "equal-arrow",
       "equal-equal",
       "hash",
       "hash-hash",
       "hash-a-dollar",
       "hash-n-dollar",
       "hash-r-dollar",
       "hash-w-dollar",
       "hexidecimal",
       "invalid",
       "less",
       "less-equal",
       "less-less",
       "minus",
       "minus-arrow",
       "minus-minus",
       "minus-minus-arrow",
       "newline",
       "not",
       "not-equal",
       "not-not",
       "number",
       "open-brace",
       "open-bracket",
       "open-routine",
       "or",
       "or-or",
       "plus",
       "plus-plus",
       "remainder",
       "semicolon",
       "string",
       "symbol",
       "times",
       "whitespace",
       "word"};


// Class variables

   public byte id;
   public int level;
   public int startPos;
   public int endPos;
   public String content;


// Class constructors

   /**
    * Creates a new Inform token object identifying a lexical token within the Inform source code.
    * @param id A number identifying The type of token.
    * @param startPos The position of the start of the token text within the Inform source code.
    * @param endPos The position of the end of the token text within the Inform source code.
    * @param content The source text of the Inform lexical token.
    */
   public InformToken(byte id, int startPos, int endPos, String content) {

     this(id, startPos, endPos);
     this.content = content;

   }

   /**
    * Creates a new Inform token object identifying a lexical token within the Inform source code.
    * @param id A number identifying The type of token.
    * @param startPos The position of the start of the token text within the Inform source code.
    * @param endPos The position of the end of the token text within the Inform source code.
    */
   public InformToken(byte id, int startPos, int endPos) {

     this.id = id;
     this.startPos = startPos;
     this.endPos = endPos;

   }
// Class methods

   /**Gets the name of the Inform lexical token.
    */
   public String getName() {

      return name[id];
   }

   /**Sets the number representing the type of the Inform lexical token.
    * @param id A number identifying The type of token.
    */
   public void setType(byte id) {

      if (id >= LOWEST && id <= HIGHEST) {
         this.id = id;
      }
   }

   /**Gets a number representing the type of the Inform lexical token.
    */
   public byte getType() {

      return id;
   }

   /**Gets the start position of the Inform lexical token.
    */
   public int getStartPosition() {

      return startPos;
   }

   /**Gets the end position of the Inform lexical token.
    */
   public int getEndPosition() {

      return endPos;
   }

   /**Gets the source of the Inform lexical token.
    */
   public String getContent() {

      return content;
   }

   /**Converts the Inform token into a string representation.
    */
   public String toString() {

      return "[id: " + name[id] + ", start: " + startPos + ", end: " + endPos + ", content: " + content + "]";
   }
}