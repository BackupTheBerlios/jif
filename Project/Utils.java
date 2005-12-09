/*
 * Utils.java
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

import java.io.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.nio.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.channels.FileChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


/**
 * A Class with some generic methods
 * @author Alessandro Schillaci
 */
public class Utils {
    
    jFrame jframe;
    
    /** Creates a new instance of Utils */
    public Utils() {
    }
    
    
    /**
     * Calculates total number of brackets in the text
     * @param testo String Text
     * @return The total number of brackets in the text
     */
    public static int numberOfBrackets(String testo){
        String pattern="\"";
        int numero=0,pos=0;
        while ((pos = testo.indexOf(pattern, pos)) >= 0){
            numero++;
            pos += pattern.length();
        }
        return numero;
    }
    
    
    public static String spacesForTab(int numberofspaces){
        String tmp="";
        for (int i = 0; i < numberofspaces+1; i++) {
            tmp+=" ";
        }
        return tmp;
    }
    
    /**
     * Replaces the string "pattern" with "replace" into a text
     * @param str The main string
     * @param pattern The string to be replaced
     * @param replace The target string of replacing
     * @return The new string with text replaced
     */
    public static String replace(String str, String pattern, String replace) {
        int s = 0;
        int e = 0;
        StringBuffer result = new StringBuffer();
        
        while ((e = str.indexOf(pattern, s)) >= 0) {
            result.append(str.substring(s, e));
            result.append(replace);
            s = e+pattern.length();
        }
        result.append(str.substring(s));
        return result.toString();
    }
    
    
    
    /**
     * Open the file and seek for classes
     * 1. if file hasn't got .h or .inf extension, return
     * 2. load the file into a string
     * 3. seek for classes, for each found add it into the
     *   classes vector
     * @param vett Classes Vector
     * @param file The file to open fo find the classes
     */
    public static void seekClass(Vector vett, File file){
        Pattern patt = Pattern.compile("\n+\\s*Class\\s+(\\w+)\\s", Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
        if (file.getAbsolutePath().endsWith(".h") || file.getAbsolutePath().endsWith(".inf")){
            if (null!=file){
                try{
                    // Get a channel for the source file
                    FileInputStream fis = new FileInputStream(file);
                    FileChannel fc = fis.getChannel();
                    ByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, (int) fc.size());
                    Charset cs = Charset.forName(Constants.fileFormat);
                    CharsetDecoder cd = cs.newDecoder();
                    CharBuffer cb = cd.decode(bb);
                    Matcher m = patt.matcher(cb);
                    while (m.find()){
                        //System.out.println("Found new class: "+m.group(1));
                        vett.add(m.group(1));
                    }
                    fc.close();
                    fis.close();
                } catch(Exception e){
                    System.out.println("ERR: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } // end IF
        return;
    }
    
    
  
    
    public void printInform(jFrame jframe,String title, JTextComponent jif){
        Properties prop = new Properties();
        PrintJob pjob = jframe.getToolkit().getPrintJob(jframe, title, prop);
        if (pjob != null) {
            Graphics pg = pjob.getGraphics();
            if (pg != null) {
                String s = jif.getText();
                printLongString(pjob, pg, s);
                pg.dispose();
            }
            pjob.end();
        }
    }
    
    // Print string to graphics via printjob
    // Does not deal with word wrap or tabs
    private void printLongString(PrintJob pjob, Graphics pg, String s) {
        
        // Replacing the TABS with spaces
        s = Utils.replace(s,"\t","    ");
        
        int margin = 50;
        int pageNum = 1;
        int linesForThisPage = 0;
        int linesForThisJob = 0;
        // Note: String is immutable so won't change while printing.
        if (!(pg instanceof PrintGraphics)) {
            throw new IllegalArgumentException("Graphics context not PrintGraphics");
        }
        StringReader sr = new StringReader(s);
        LineNumberReader lnr = new LineNumberReader(sr);
        String nextLine;
        int pageHeight = pjob.getPageDimension().height - margin;
        Font helv = new Font("Monospaced", Font.PLAIN, 8);
        //have to set the font to get any output
        pg.setFont(helv);
        FontMetrics fm = pg.getFontMetrics(helv);
        int fontHeight = fm.getHeight();
        int fontDescent = fm.getDescent();
        int curHeight = margin;
        try {
            do {
                nextLine = lnr.readLine();
                if (nextLine != null) {
                    if ((curHeight + fontHeight) > pageHeight) {
                        // New Page
                        //System.out.println ("" + linesForThisPage + " lines printed for page " + pageNum);
                        if (linesForThisPage == 0) {
                            //System.out.println ("Font is too big for pages of this size; aborting...");
                            break;
                        }
                        pageNum++;
                        linesForThisPage = 0;
                        pg.dispose();
                        pg = pjob.getGraphics();
                        if (pg != null) {
                            pg.setFont(helv);
                        }
                        curHeight = 0;
                    }
                    curHeight += fontHeight;
                    if (pg != null) {
                        pg.drawString(nextLine, margin, curHeight - fontDescent);
                        linesForThisPage++;
                        
                        linesForThisJob++;
                    } else {
                        //System.out.println ("pg null");
                    }
                }
            } while (nextLine != null);
        } catch (EOFException eof) {
            // Fine, ignore
        } catch (Throwable t) { // Anything else
            t.printStackTrace();
        }
        //System.out.println ("" + linesForThisPage + " lines printed for page " + pageNum);
        //System.out.println ("pages printed: " + pageNum);
        //System.out.println ("total lines printed: " + linesForThisJob);
    }
    
    
    
    public static int IgnoreCaseIndexOf(String mainString, String str, int fromIndex) {
        String s1 = mainString.toLowerCase();
        String t1 = str.toLowerCase();
        return s1.indexOf(t1, fromIndex);
    }
    
    public static int IgnoreCaseIndexOf(String mainString, String str) {
        String s1 = mainString.toLowerCase();
        String t1 = str.toLowerCase();
        return s1.indexOf(t1);
    }
    
    
    /**
     * Return the code assistant
     * @param code
     */
    public static String assistCode(String code){
        //	[ret] = return
        //	[tab] = tab char
        code = replace(code,"[ret]","\n");
        code = replace(code,"[tab]","\t");
        code = replace(code,"@","");
        return code;
    }
    
    /**
     * Returns a string with all occurrences of the target string in file
     *
     */
    public static String searchString(String target, File file){
        // open file
        String head = new String("File="+file.getName());
        String filename = file.getAbsolutePath();
        StringBuffer out = new StringBuffer();
        try{
            String riga;
            int lineCount=0;
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Constants.fileFormat));
            while ((riga = br.readLine())!=null){
                lineCount++;
                if (Utils.IgnoreCaseIndexOf(riga,target)!=-1){
                    out.append("\n"+Constants.TOKENSEARCH+filename+Constants.TOKENSEARCH+lineCount+Constants.TOKENSEARCH+": "+ riga);
                }
            }
            br.close();
        } catch(IOException e){
            System.out.println(e.getMessage());
        }
        if (out.length() == 0){
            return null; // find no string "target" in the current file
        } else return (head+out.toString()+"\n");
        //return out.toString();
    }
    

    
}
