package ma.cgi.gtc.spaich;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * This class allow access to Windows Reg editor
 */

public class RegKeyManager {
    
    private final String TYPES[] = { "SZ", "BINARY", "DWORD", "QWORD", "DWORD_LITTLE_ENDIAN", "QWORD_LITTLE_ENDIAN", "DWORD_BIG_ENDIAN", "EXPAND_SZ", "LINK", "MULTI_SZ", "NONE", "RESOURCE_LIST" };
    private String type;
    private String value;
    private String key;
	Pattern pattern;
    Matcher matcher; 
    
    protected void query(String loc, String k) throws Exception {
    	
    	type = ""; 
    	value = ""; 
    	key = "";
    	
        Process p = Runtime.getRuntime().exec("reg QUERY \"" + loc + "\" /v \"" + k + "\"");
        
        BufferedReader in = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
        String out = "";
	    pattern = Pattern.compile("^\\s*(.*)\\s+REG_SZ\\s+(.*)");

        
        while ( ( out = in.readLine() ) != null ) {
            System.out.println("query out : "+out);
			matcher = pattern.matcher(out);
			if(matcher.matches()){
				
		        key=matcher.group(1);
		        type="REG_SZ";
		        value=matcher.group(2);
		        
				break; 
				}
        }
        in.close();
    }
    
    protected String getKey() { return key; }
    protected String getType() { return type; }
    protected String getValue() { return value; }
    
    protected boolean add(String loc, String name, String dType, String value) throws Exception {
        boolean comp = false;
        boolean valid = false;
        
        for (int a = 0; a < TYPES.length; a++) {
            if (dType.equalsIgnoreCase("REG_" + TYPES[a])) { valid = true; break; }
        }
        
        if ( valid ) {
            Process p = Runtime.getRuntime().exec("reg ADD \"" + loc + "\" /v \"" + name + "\" /t \"" + dType + "\" /d \"" + value + "\"");
        
            BufferedReader in = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
            String out = "";
        
            while ( (out = in.readLine() ) != null ) {
               
            	System.out.println("add out : "+out);
                if (out.equalsIgnoreCase("The operation completed successfully.")) { comp = true; }
            }
            in.close();
        }
        
        return comp;
    }
    
    protected boolean delete(String loc, String key) throws Exception {
        
    	boolean comp = false;
        Process p = Runtime.getRuntime().exec("reg DELETE \"" + loc + "\" /v \"" + key + "\" /f");
        BufferedReader in = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
        String out = "";
        
        while ( ( out = in.readLine() ) != null ) {
            System.out.println("delete out : "+out);

            if (out.equalsIgnoreCase("The operation completed successfully.")) { comp = true; }
        }
        in.close();
        
        return comp;
    }
    
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		RegKeyManager rkm = new RegKeyManager();
		
		rkm.delete("HKCU\\Software\\Spaich\\RetroSpec","SetupPath");

		rkm.add("HKCU\\Software\\Spaich\\RetroSpec","SetupPath","REG_SZ","C:\\Spaich\\aaa borped80.txt");
		
		rkm.query("HKCU\\Software\\Spaich\\RetroSpec", "SetupPath");
		
		System.out.println("KEY:" + rkm.getKey() + " DATA TYPE:" + rkm.getType() + " DATA VALUE:" + rkm.getValue());

			
	}
}

