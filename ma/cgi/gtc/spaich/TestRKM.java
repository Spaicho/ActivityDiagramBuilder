package ma.cgi.gtc.spaich;

public class TestRKM {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		RegKeyManager rkm = new RegKeyManager();
		//rkm.query("HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders", "Personal");

		//System.out.println("KEY: " + rkm.getKey() + " DATA TYPE: " + rkm.getType() + " DATA VALUE: " + rkm.getValue());

		//rkm.add("HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Run","TESTING","REG_SZ","VALUE DATA");
		
		rkm.delete("HKCU\\Software\\Spaich\\RetroSpec","SetupPath");

		rkm.add("HKCU\\Software\\Spaich\\RetroSpec","SetupPath","REG_SZ","C:\\Spaich\\aaa borped80.txt");
		
		rkm.query("HKCU\\Software\\Spaich\\RetroSpec", "SetupPath");
		
		System.out.println("KEY:" + rkm.getKey() + " DATA TYPE:" + rkm.getType() + " DATA VALUE:" + rkm.getValue());

		//rkm.delete("HKCU\\Software\\Spaich\\RetroSpec","SetupPath");

			
	}

}
