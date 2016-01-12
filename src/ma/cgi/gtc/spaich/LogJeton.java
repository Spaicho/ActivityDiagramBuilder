package ma.cgi.gtc.spaich;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import com.ibm.xtools.pluglets.Pluglet;

public class LogJeton {
	
	Pluglet pluglet;
	
	public LogJeton(Pluglet pluglet) {
		super();
		this.pluglet = pluglet;
	}
	
	private void logStack(Stack<Jeton> pileToLog) {
		Stack<Jeton> pileIntermidaire = (Stack<Jeton>)pileToLog.clone();
		Jeton unJeton;
		String espaces;
		
		while(!pileIntermidaire.empty()){
			unJeton = pileIntermidaire.pop();
			//espaces = (Integer.toString(unJeton.getNumeroLigne()).length())-3;
			pluglet.out.println("+--"+"+-"+"+-------"+"+-----"+"+--+");
			pluglet.out.println("|"+unJeton.getNumeroLigne()+"|"+unJeton.getTokentype().charAt(0)+"|"+unJeton.getCorps()+"|"+unJeton.isEstCree()+"|"+unJeton.getNiveau()+"|");
		}
		
	}
	private void logStack(Jeton jetonToLog) {
		
		pluglet.out.println("+--"+"+-"+"+-------"+"+-----"+"+--+");
		pluglet.out.println("|"+jetonToLog.getNumeroLigne()+"|"+jetonToLog.getTokentype().charAt(0)+"|"+jetonToLog.getCorps()+"|"+jetonToLog.isEstCree()+"|"+jetonToLog.getNiveau()+"|");
	}
	
	private String ReplaceCobolToFrancais(String doc) {
		
		String docFr = doc;
		
		HashMap<String,String> CobolKeywords = new HashMap<String,String>();
		CobolKeywords.put("END-PERFORM"," Fin-Faire ");
		CobolKeywords.put("PERFORM "," Faire ");
		CobolKeywords.put("END-STRING"," ");
		CobolKeywords.put("STRING "," Chaine de caractère : ");
		CobolKeywords.put("INTO "," Dans ");
		CobolKeywords.put("SIZE "," taille ");
		CobolKeywords.put("BY "," par ");
		CobolKeywords.put("DELIMITED "," limitée ");
		CobolKeywords.put("END-IF"," Fin Si ");
		CobolKeywords.put("IF "," Si ");
		CobolKeywords.put("ELSE "," Sinon ");
		CobolKeywords.put("END-EVALUATE"," ");
		CobolKeywords.put("EVALUATE "," Evaluer ");
		CobolKeywords.put("WHEN "," Cas ");
		CobolKeywords.put("MOVE "," Mettre ");
		CobolKeywords.put("TO "," Dans ");
		CobolKeywords.put("DISPLAY "," Afficher ");
		CobolKeywords.put("ADD "," Ajouter ");
		CobolKeywords.put("COMPUTE "," Calculer ");
		CobolKeywords.put("SPACE "," Espace ");
		CobolKeywords.put("SPACES "," Espace ");
		CobolKeywords.put("INITIALIZE "," Initialiser ");
		CobolKeywords.put("AND "," Et ");
		CobolKeywords.put("OR "," Ou ");
		CobolKeywords.put("UNTIL "," Jusqu'a ");
		 
		for (Iterator<String> i = CobolKeywords.keySet().iterator() ; i.hasNext() ; ){
			
			String MotcleCobol = i.next();
			String equivalentFr = CobolKeywords.get(MotcleCobol);
			docFr = docFr.replaceAll(MotcleCobol, equivalentFr);
 
		}
		
		return docFr;
		
	}
}
