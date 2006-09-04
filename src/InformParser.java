/*
 * InformParser.java
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

import java.util.ArrayList;
import java.util.Stack;

/**
 * InformParser: Used to split a string of Inform source code into a series of
 * tokens identifying classes, elements globals, objects, properties,
 * relationships and verbs.
 * 
 * @author Peter Piggott
 * @version Revision: 1.0
 * @since 3.2
 */

public class InformParser {
    
    private State baseState;
    private State classState;
    private State globalState;
    private State objectState;
    private State propertyState;
    private State verbState;
    
    private State state = baseState;
    
    private Object callback;
    private InformLexer lex;
    
    private ArrayList stack;
    private int head = -1;

    private InformToken token = null;
    private InformToken.Lexeme tokenType = null;
    private String tokenContent = "";
    
    /** Creates a new instance of InformParser */
    public InformParser(Callback callback, InformLexer lex) {
        
        this.baseState = new BaseState(this);
        this.classState = new ClassState(this);
        this.globalState = new GlobalState(this);
        this.objectState = new ObjectState(this);
        this.propertyState = new PropertyState(this);
        this.verbState = new VerbState(this);
        
        this.callback = callback;
        this.lex = lex;
    }
    
    public void parse() {
        
        while (tokenType != InformToken.EOS) {

            state.nextToken();

        }
    }
    
    void push(InformToken token) {
        stack.add(++head, token);
    }
    
    InformToken pop() {
        InformToken token = (InformToken) stack.get(head);
        stack.remove(head);
        return token;
    }
    
    void shift(State state) {
        push(token);
        setState(state);
    }
    
    void setState(State state) {
        this.state = state;
    }

    /**
     * Users of the parser must implement this callback interface
     */
    public interface Callback {
        public void addAttribute(InformToken attributeToken);
        public void addClass(InformToken classToken);
        public void addElement(InformToken elementToken);
        public void addGlobal(InformToken globalToken);
        public void addObject(InformToken objectToken);
        public void addProperty(InformToken propertyToken);
        public void addRelationship(InformToken relationshipToken);
        public void addVerb(InformToken verbToken);
    }
    
    private interface State {
        void nextToken();
        
    }
    
    private class BaseState implements State {
        InformParser parser;
        public BaseState(InformParser parser) {
            this.parser = parser;
        }
        
        public void nextToken() {
 
            token = lex.nextToken();
            tokenType = token.getType();
            tokenContent = token.getContent();

            if (tokenType == InformToken.SYMBOL && 
                    tokenContent.equalsIgnoreCase("class")) {
                setState(classState);
            }
            
            if (tokenType == InformToken.SYMBOL &&
                    tokenContent.equalsIgnoreCase("object")) {
                setState(objectState);                
            }
            
            if (tokenType == InformToken.SYMBOL &&
                    tokenContent.equalsIgnoreCase("property")) {
                setState(propertyState);                
            }
            
            if (tokenType == InformToken.SYMBOL &&
                    (tokenContent.equalsIgnoreCase("extend") ||
                    tokenContent.equalsIgnoreCase("verb"))) {
                setState(verbState);                
            }
            
        }
    }
    
    private class ClassState implements State {
        InformParser parser;
        public ClassState(InformParser parser) {
            this.parser = parser;
        }
        
        public void nextToken() {
            
        }
    }
    
    private class GlobalState implements State {
        InformParser parser;
        public GlobalState(InformParser parser) {
            this.parser = parser;
        }
        
        public void nextToken() {
            
        }
    }
    
    private class ObjectState implements State {
        InformParser parser;
        public ObjectState(InformParser parser) {
            this.parser = parser;
        }
        
        public void nextToken() {
            
        }
    }
    
    private class PropertyState implements State {
        InformParser parser;
        public PropertyState(InformParser parser) {
            this.parser = parser;
        }
        
        public void nextToken() {
            
        }
    }
    
    private class VerbState implements State {
        InformParser parser;
        public VerbState(InformParser parser) {
            this.parser = parser;
        }
        
        public void nextToken() {
            
        }
    }
      
}
