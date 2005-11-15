/*
 * makeConfigIni.java
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


public class makeConfigIni {
    String informATElibpath;
    String interpreterPath;
    String glulxPath;
    String compilerPath;
    String bresPath;
    String blcPath;    
    String gamesPath;
   
    public makeConfigIni() {
        
        informATElibpath="";
        interpreterPath="";
        glulxPath="";
        compilerPath="";
        bresPath="";
        blcPath="";
        gamesPath=System.getProperty("user.dir");
       
    }
    
    String makeConfig(){
                    StringBuffer makeConfig = new StringBuffer();
                    makeConfig.append("######################################################"+"\n");
                    makeConfig.append("# Jif Configuration"+"\n");
                    makeConfig.append("######################################################"+"\n");
                    makeConfig.append("\n");
                    makeConfig.append("libPath="+informATElibpath+"\n");
                    makeConfig.append("libPathSecondary1=\n");
                    makeConfig.append("libPathSecondary2=\n");
                    makeConfig.append("libPathSecondary3=\n");
                    makeConfig.append("gamesDir="+gamesPath+"\n");
                    makeConfig.append("interpreter="+interpreterPath+"\n");
                    makeConfig.append("glulx="+glulxPath+"\n");
                    makeConfig.append("compiler="+compilerPath+"\n");
                    makeConfig.append("defaultBrowser=\n");
                    makeConfig.append("BRESLOCATION="+bresPath+"\n");
                    makeConfig.append("BLCLOCATION="+blcPath+"\n");
                    return(makeConfig.toString());
    }
    

}