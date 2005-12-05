import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Vector;
/*
 * JifConfiguration.java
 *
 * Created on 3 dicembre 2005, 22.15
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author administrator
 */
public class JifConfiguration {
    
    private Vector altkeys;               // ALT keys
    private Vector helpedcode;            // Code assistant
    private Vector mapping;               // Syntax Highlight
    private Vector menu;                  // Right click menues
    private Vector recentfiles;           // Recent opened files    
    private Vector switches;              // Switches passed to compiler
    private Vector syntaxhighlight;       // Syntax Highlight
    private Vector symbols;               // Symbols list

  
    // Paths
    private String librayPath;
    private String librayPathSecondary1;
    private String librayPathSecondary2;
    private String compiledPath;
    private String interpreterZcodePath;
    private String interpreterGlulxPath;
    private String compilerPath;
    private String bresPath;
    private String blcPath;
    
    // Jif settings
    private boolean wrapLines;
    private boolean syntax;
    private boolean assistantCode;
    private boolean mappingCode;
    private boolean numberLines;
    private boolean projectScanForClasses;
    private boolean projectOpenAllFiles;
    private boolean useCompiledPath;
    private boolean openLastFile;
    private boolean createNewFile;
    private boolean makeAlwaysResource;
    private int tabSize;
    private Color colorKeyword;
    private Color colorAttribute;
    private Color colorProperty;
    private Color colorRoutine;
    private Color colorVerb;
    private Color colorNormal;
    private Color colorComment;
    private Color colorBackground;
    private Font defaultFont;    
    private int locationX;
    private int locationY;
    private int width;
    private int height;
    private boolean output;
    private boolean jtoolbar;
    private boolean jtree;
    private boolean divider1;
    private boolean divider3;
    private String lastFile;
    private String lastProject;
    
    /** Creates a new instance of JifConfiguration */
    public JifConfiguration(File file) {
        
        if (null == file){
            // todo: caricare le impostazioni di default?
            this.setDefaults();
        }
        else{
            this.loadFromFile(file);
        }
    }
    
    public void loadFromFile(File file){
        String ident, value, riga;
        
        // initialize
        altkeys         = new Vector();
        helpedcode      = new Vector();
        mapping         = new Vector();
        menu            = new Vector();
        recentfiles     = new Vector();
        switches        = new Vector();
        syntaxhighlight = new Vector();
        symbols         = new Vector();
        
        try{

            if (!(file.exists())){
                System.out.println(java.util.ResourceBundle.getBundle("JIF").getString("ERR_OPENFILE6"));
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), Constants.fileFormat));                                        
            
            while ((riga = br.readLine())!=null){

                if (!(riga.startsWith(Constants.TOKENCOMMENT))&&!(riga.equals(""))){

                    // Load alkeys vector
                    if (riga.startsWith(Constants.ALTKEYSTOKEN)){}
                    if (riga.startsWith(Constants.HELPEDCODETOKEN)){}
                    if (riga.startsWith(Constants.MAPPINGTOKEN)){}                    
                    if (riga.startsWith(Constants.MENUTOKEN)){}
                    if (riga.startsWith(Constants.RECENTFILESTOKEN)){}
                    if (riga.startsWith(Constants.SWITCHTOKEN)){}
                    if (riga.startsWith(Constants.SYNTAXTOKEN)){}
                    if (riga.startsWith(Constants.SYMBOLSTOKEN)){}
                    if (riga.startsWith(Constants.PATHTOKEN)){}
                    if (riga.startsWith(Constants.SETTINGSTOKEN)){}
                }
            }
            br.close();
        } catch(Exception e){
            System.out.println("Ini File "+file.getAbsoluteFile()+" MALFORMED.");
        }        
        
        
        
    }
    
    public void setDefaults(){
        colorKeyword    = new Color(29,59,150);
        colorAttribute  = new Color(153,0,153);
        colorProperty   = new Color(37,158,33);
        colorRoutine    = new Color(0,0,0);
        colorVerb       = new Color(0,153,153);
        colorNormal     = new Color(0,0,0);
        colorComment    = new Color(153,153,153);
        colorBackground = new Color(255,255,255);
        defaultFont     = new Font("Courier New",0,12);
        
        // TODO: mettere anche gli altri defaults
    }
     
    
    // A class to render the right click menues
    public class menu{
        private String menuLabel;
        private Vector entry;
    }
    
}
