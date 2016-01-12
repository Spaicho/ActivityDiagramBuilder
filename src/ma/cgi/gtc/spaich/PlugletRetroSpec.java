package ma.cgi.gtc.spaich;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import java.util.regex.*;

import ma.cgi.gtc.spaich.exception.ParsingException;
import ma.cgi.gtc.spaich.exception.RetroSpecPlugletException;
import ma.cgi.gtc.spaich.exception.SelectionComposantException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;

import org.eclipse.gmf.runtime.common.core.util.StringUtil;
import org.eclipse.gmf.runtime.diagram.ui.internal.services.layout.LayoutNode;
import org.eclipse.gmf.runtime.diagram.ui.services.layout.ILayoutNode;
import org.eclipse.gmf.runtime.notation.Bounds;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.Style;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.gmf.runtime.diagram.ui.providers.*;
import org.eclipse.gmf.runtime.diagram.ui.providers.internal.RadialProvider;
import org.eclipse.gmf.runtime.diagram.ui.dialogs.sortfilter.*;


import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityGroup;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.ConditionalNode;
import org.eclipse.uml2.uml.ControlFlow;
import org.eclipse.uml2.uml.DecisionNode;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.FinalNode;
import org.eclipse.uml2.uml.InitialNode;
import org.eclipse.uml2.uml.LoopNode;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.OpaqueAction;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.StructuredActivityNode;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.VisibilityKind;
import org.eclipse.uml2.uml.util.UMLUtil;

import com.ibm.xtools.modeler.ui.UMLModeler;
import com.ibm.xtools.modeler.ui.internal.ui.preferences.FilterContentProvider;
import com.ibm.xtools.pluglets.Pluglet;
import com.ibm.xtools.uml.ui.diagram.IUMLDiagramHelper;
import com.ibm.xtools.umlnotation.UMLDiagramKind;

public class PlugletRetroSpec extends Pluglet {

	/* 
	 * FIXME
	 * 
	 * La doc de d�but est invers�e.
	 * Si une activit� ne contient aucune doc, y mettre le nom de l'activit�
	 * R�gler les probl�me d'encodage, "OR" mal �crite
	 * d�tecter l'encodage ? ANSI UTF-8
	 * g�rer les Control flow, mettre le libell� dans guard onglet g�n�ral
	 * Probl�me tradution TO => Dans
	 * R�gler le probl�me des libell��s des noeud de controle qui disparaissent.
	 * R�gler le probl�me des noms des activit�s qui ne s'affichent pas compl�tement
	 * Ajouter la gestion des activit�s appelables.
	 */

	public void plugletmain(String[] args) {
		
		//class for logging
		LogJeton logjeton;
		
		logJeton = new LogJeton(this);
		
		out.println("Pluglet \"RetroDoc\".");
		
		/* Perform remaining work within a Runnable */
        try {
        	final TransactionalEditingDomain editDomain =  UMLModeler.getEditingDomain();
        	editDomain.runExclusive(new Runnable() {

                public void run() {
                    
            		editDomain.getCommandStack().execute(new RecordingCommand(editDomain, "Modify Operation") {

            			protected void doExecute() {
            				
            				// Get selection from project Explorer, Selection could be : Component, StructuredActivity or Activity
            				NamedElement elementSelectionne = getWorkSelection();
            				
            				// Get Algorithm file
            				BufferedReader in = getAlgorithmFile();
            				
            				// Create Activity Diagram
            				//createActivityDiagram((EObject)elementDeTravail,in);
            				createActivityDiagram(elementSelectionne,in);

            			}

            		});
                }
            });
            } catch (InterruptedException e) {
    			out.println("The operation was interrupted"); //$NON-NLS-1$
            }
	}

	/** Get selection from project Explorer
	 *  Selection could be : Component, StructuredActivity or Activity
	 * @return NamedElement WorkSelection
	 */
	private NamedElement getWorkSelection() {
		
    	final List elements = UMLModeler.getUMLUIHelper().getSelectedElements("org.eclipse.ui.navigator.ProjectExplorer");
  	  	NamedElement elementSelectionne = null;
  	  	
  	  	switch (elements.size()) {
		case 0:
     		throw new SelectionComposantException("Probl�me de selection de composant/activit�/activit� structur� \n aucun �l�ment selectionn�.");
		case 1:
            elementSelectionne = (NamedElement)elements.get(0);
           	out.println(elementSelectionne.getQualifiedName());
            inform("Element du travail : "+elementSelectionne.getName() );
			break; 
		default:
     		throw new SelectionComposantException("Probl�me de selection de composant/activit�/activit� structur�e \n plusieurs �l�ments selectionn�s.");
		}
        return elementSelectionne;
		
	}
	
	/** Get the file containing the algorithm to model
	 *  a prompt is used to get user answer. The user's answer is saved to windows registry for next time. 
	 * @return BufferedReader AlgorithmFile
	 */
	private BufferedReader getAlgorithmFile() {
		
		BufferedReader in=null;
		String cheminInitial=null;
		String cheminRenseigne=null;
				
		cheminInitial = getCheminInitial();
		
		cheminRenseigne = prompt("Entrer le chemin de fichier qui contient l'algorithme � mod�liser :" , cheminInitial, "Chemin de fichier d'algorithme");
		
		if(cheminRenseigne==null |cheminRenseigne==""|cheminRenseigne.matches("\\s+"))
			throw new SelectionComposantException("Aucune saisie n'a �t� effectu�e");
		
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(cheminRenseigne),"CP1252")); // CP1252 (Western European languages) //ISO-8859-1

			} catch (FileNotFoundException e) {
				e.printStackTrace(out);
			    throw new RuntimeException(e);

			}
			catch (UnsupportedEncodingException e) {
				e.printStackTrace(out);
			    throw new RuntimeException(e);
			}
		
		setCheminInitial(cheminRenseigne);
		
		return in;
	}
	
	/** Save user's choice (Algorithm file path)to the windows registry.
	 * @return 
	 */
	private void setCheminInitial(String cheminRenseigne) {

		String locationReg="HKCU\\Software\\Spaich\\RetroSpec";
		String keyReg="SetupPath";
		
		RegKeyManager rkm = new RegKeyManager();
		try {
			rkm.delete(locationReg, keyReg);
			rkm.add(locationReg, keyReg, "REG_SZ",cheminRenseigne);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		out.println("setCheminInitial():"+cheminRenseigne);
	}

	/** Get user's choice (Algorithm file path) from the windows registry.
	 * @return 
	 */
	private String getCheminInitial() {
		
		String locationReg="HKCU\\Software\\Spaich\\RetroSpec";
		String keyReg="SetupPath";
		String cheminInitial="";
		
		RegKeyManager rkm = new RegKeyManager();
		try {
			rkm.query(locationReg, keyReg);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		cheminInitial=rkm.getValue();
		out.println("getCheminInitial():"+cheminInitial);
		
		return cheminInitial;
	}

	protected void createActivityDiagram(NamedElement elementSelectionee, BufferedReader in) {
		
		/** 
		 *  Adding or getting Principal Activity node : Component, Activity, 
		 *	Structured Activity or Opaque Action. 
		 */
		out.println("Adding or getting Principal Activity .."); 
		Object activity1 = UmlFactory.addActivity(elementSelectionee, "Activit� du programme");
		
		//Adding or getting Activity Diagram if selected element is Component, 
		//Activity, Structured Activity else if Opaque Action no need for Diagram
		out.println("Adding or getting Principal Diagram ..");
		Diagram diagram = UmlFactory.addActivityDiagram(activity1, "Diagramme d'algorithme");	

		//Boolean pour ajouter une it�ration apr�s fin lecture, pour achever tous.
		boolean bonusIterationAconsommer =true;
		Stack<Jeton> pileJetons = new Stack<Jeton>();
		String ligneLue = null;
		int numlignes = 0;
		boolean docDebutTraite = false;
		
		NamedElement SuperActivity = activity1 instanceof OpaqueAction ? null :
									 activity1 instanceof Activity ?(Activity)activity1:
									(StructuredActivityNode)activity1;

		NamedElement SuperControlFlow = getInitialNode((NamedElement)activity1);
		String corpControlFlow="";
				
		//Cr�ation et push d'un premier jeton
		
		//Extraction des jetons
		try {
			while(((ligneLue = in.readLine())!=null) || bonusIterationAconsommer){
				
				//utilisation de l'it�ration bonus
				if (ligneLue==null) {bonusIterationAconsommer=false;ligneLue="Fin:";}
				
				numlignes++;
				Jeton jetonLu=null;
				
				jetonLu =extraireJeton(pileJetons,ligneLue,numlignes);
				
				out.println("jetonLu:"+jetonLu);
				
				//Si la doc deb est � traiter
				if(!docDebutTraite)
					{
					out.println("docDebut est � traiter => tester le jeton lu");
					//Si le jeton lu est diff�rent de documentation (ou des jetons neutres)
					if(!(jetonLu.getTokentype()=="Documentation") && !(jetonLu.getTokentype()=="LigneVide") && !(jetonLu.getTokentype()=="Commentaire"))
						{
						out.println("JetonLu n'est pas doc => tester le contenu de la pile");
						//on rentre plus dans la boucle
						docDebutTraite = true;
						//Si la pile contient que des docs sans mot cl� (ouvrant surtout)
						if (containsDocOnly(pileJetons))
							{
							//avant le traitement de la doc debut tester :
							//si le jeton qui met fin a la doc debut est un mot-cl�, et l'element selecionn� est une action=> Exception
							out.println("containsDocOnly => evaluer l'element selectionne puis traiter Doc d�but");
							if (!(jetonLu.getTokentype()=="Fin:") && (activity1 instanceof OpaqueAction)){throw new ParsingException("Une Action est selectionn�e, mais il ne peut pas contenir des �l�ments autres qu'une documentation.");}

							traiterDocDebut(pileJetons,activity1);

							}
						}
					}
						//throw new ParsingException("Documentation orpheline non autoris�e au milieu du l'algorithme. ligne :"+numlignes);
				
				out.println("JetonLu TokenType :"+jetonLu.getTokentype());
				
				/**
				 *  TODO  Ajouter une analyse sytaxique (v�rifier la suite des lignes) et extraire les erreurs.
				 */
				if ((jetonLu.getTokentype()).equals("Faire:")){
					pileJetons.push(jetonLu);
				}
				
				if (jetonLu.getTokentype()=="LigneVide"){
					//jeton ignor�
				}
				if (jetonLu.getTokentype()=="Commentaire"){
					//jeton ignor�
				}

				if (jetonLu.getTokentype().equals("Si:")){
					pileJetons.push(jetonLu);
				}
				if (jetonLu.getTokentype().equals("Cas:")){
					pileJetons.push(jetonLu);
				}
				if (jetonLu.getTokentype().equals("Fin-Faire:")){
					//Le Fin-Faire: peut �tre celui d'une action ou SA

					/**
					 * Pahse D�pilement
					 */
					
					out.println("Fin-Faire: detect�, d�piler les jetons jusqu'� Faire: pour les traiter");

					Stack<Jeton> pileJetonsAtraiter = new Stack<Jeton>();

					while(!(pileJetons.peek().getTokentype().equals("Faire:")) && (pileJetons.size()>0)){
						pileJetonsAtraiter.push(pileJetons.pop());
					}
					//d�piler en plus le jeton Faire:
					pileJetonsAtraiter.push(pileJetons.pop());

					out.println("Nombre de jetons dans pileJetonsAtraiter :"+pileJetonsAtraiter.size());
					logJeton.logStack(pileJetonsAtraiter);
					
					Stack<Jeton> pileJetonsDemiTraites = new Stack<Jeton>();
					
					//on suppose que les jetons non cr��s sont empil�s dans la pile apr�s ceux cr��s
					while((pileJetons.size()>0) && (!(pileJetons.peek().isEstCree()))){
						pileJetonsDemiTraites.push(pileJetons.pop());
					}
					
					out.println("Nombre de jetons dans pileJetonsDemiTraites :"+pileJetonsDemiTraites.size());
					logJeton.logStack(pileJetonsDemiTraites);
					
					/**
					 * Phase Traitement               
					 */
					
					//Traitement des jetons � demi-traiter
					while(pileJetonsDemiTraites.size()>0){
						Jeton unJetonaDemiTraiter=pileJetonsDemiTraites.pop();
						NamedElement Tampon;
						
						//Si Faire: => cr�er Structured Activity
						if(unJetonaDemiTraiter.getTokentype().equals("Faire:")){
							//Nom de la Structured Activity
							String nomSA = unJetonaDemiTraiter.getCorps();
							//La doc dans l'enventualit� qu'elle existe
							String docSA="";
							while(pileJetonsDemiTraites.size()>0 && pileJetonsDemiTraites.peek().getTokentype().equals("Documentation")){
								docSA = docSA+pileJetonsDemiTraites.pop().getCorps()+"\n";
							}
							docSA = ReplaceCobolToFrancais(docSA);
							//Creation de la SA
							Tampon=UmlFactory.addStructuralActivity(SuperActivity, nomSA, docSA);
							
							UmlFactory.addControlFlow(SuperActivity, SuperControlFlow instanceof DecisionNode?corpControlFlow:"", SuperControlFlow, Tampon);
							
							//La SA cr��e devient SuperActivity
							SuperActivity = Tampon;
							
							//le SuperControlFlow devient le noeud initial de la nouvelle SA
							SuperControlFlow = getInitialNode(SuperActivity);

							
							//retourner la SA dans la pile � jetons
							unJetonaDemiTraiter.setEstCree(true);
							pileJetons.push(unJetonaDemiTraiter);
						}
						//Si Si: => cr�er Noeud de d�cision
						if(unJetonaDemiTraiter.getTokentype().equals("Si:")){
							
							//Nom de noeud de d�cision
							String nomND = unJetonaDemiTraiter.getCorps();
							//La doc dans l'enventualit� qu'elle existe
							String docND="";
							while(pileJetonsDemiTraites.size()>0 && pileJetonsDemiTraites.peek().getTokentype().equals("Documentation")){
								docND = docND+pileJetonsDemiTraites.pop().getCorps()+"\n";
							}
							docND = ReplaceCobolToFrancais(docND);
							//Creation de NC
							DecisionNode ND = UmlFactory.addnoeudDecision(SuperActivity, nomND, docND);
							
							//Cr�ation de CF : dans superActivity, avec son nom, sa source et sa destination
							UmlFactory.addControlFlow(SuperActivity, SuperControlFlow instanceof DecisionNode?corpControlFlow:"", SuperControlFlow, ND);
							
							//SuperControleFlow devient le noeud Decision cr��.
							SuperControlFlow = ND;
							
							//retourner la SA dans la pile � jetons
							unJetonaDemiTraiter.setEstCree(true);
							unJetonaDemiTraiter.setActivite(ND);
							pileJetons.push(unJetonaDemiTraiter);
						}
						
						//Si Cas: => changer le superCF
						if(unJetonaDemiTraiter.getTokentype().equals("Cas:")){
							corpControlFlow=unJetonaDemiTraiter.getCorps();
							//Chercher la ND qui va devenir le superCf dans la pile � Jetons
							//je parie que c'est le premier Jetons � pop de la pileJetons
							if(pileJetons.peek().getTokentype().equals("Si:")){
								SuperControlFlow = (NamedElement)(pileJetons.peek().getActivite());
							}
							else{
								throw new ParsingException("Une Excpetion vraiment inattendue !");
							}
							
						}
						
					}
					
					//Traitement des jetons � full-traiter (� fermer)
					while(pileJetonsAtraiter.size()>0){
						Jeton unJetonATraiter=pileJetonsAtraiter.pop();
						out.println("SuperActivity:"+SuperActivity); 
						out.println("SuperActivity dans:"+((((StructuredActivityNode)(SuperActivity)).getInActivity()==null)?
															((StructuredActivityNode)(SuperActivity)).getInStructuredNode():
															((StructuredActivityNode)(SuperActivity)).getInActivity()));

						if(unJetonATraiter.isEstCree()){
							/**
							 * TODO : Traiter aussi les activit�s
							 */
							//Fermer la SA et retourner au niveau sup
							//Mais avant de fermer, lier le dernier element avec le noeud de fin
							//il faudra traiter le cas de plusieurs noeuds derniers !! 
							//cr��r peut etre une collection de superControlFlow et les attacher au finalNode
							UmlFactory.addControlFlow(SuperActivity, SuperControlFlow instanceof DecisionNode?corpControlFlow:"", SuperControlFlow, getFinalNode(SuperActivity));
							
							//NamedElement tampon = SuperActivity;
							if(SuperActivity instanceof StructuredActivityNode){
								//changer le SuperControleFlow : il devient la SA dont en sort
								SuperControlFlow=SuperActivity;

								//dans l'affectation ci-dessous on teste le conteneur de notre SA si une autre SA ou Activit�
								SuperActivity = (((StructuredActivityNode)(SuperActivity)).getInActivity()==null)?
												((StructuredActivityNode)(SuperActivity)).getInStructuredNode():
												((StructuredActivityNode)(SuperActivity)).getInActivity();
								
							}
							else {// car normalement n'est Cree que les SA
								throw new ParsingException("Exception tr�s difficile � comprendre !");
							}
						}
						else{
							//Traiter l'action opaque
							//Nom de l'action opaque
							String nomAO = unJetonATraiter.getCorps();
							//La doc dans l'enventualit� qu'elle existe
							String docAO="";
							while(pileJetonsAtraiter.size()>0 && pileJetonsAtraiter.peek().getTokentype().equals("Documentation")){
								docAO = docAO+pileJetonsAtraiter.pop().getCorps()+"\n";
							}
							docAO = ReplaceCobolToFrancais(docAO);
							//Creation de la AO
							OpaqueAction OA = (OpaqueAction)UmlFactory.addAction(SuperActivity, nomAO, docAO);
							UmlFactory.addControlFlow(SuperActivity,SuperControlFlow instanceof DecisionNode?corpControlFlow:"",
											SuperControlFlow, OA);
							SuperControlFlow =OA;
						}
							
					}

					
				}
				if (jetonLu.getTokentype().equals("Fin-Si:")){
					out.println("Fin-Si: detect�, d�piler les jetons jusqu'� Si: pour les traiter");
					
					//je parie l� aussi que c'est le premier Jetons � pop de la pileJetons
					if(pileJetons.peek().getTokentype().equals("Si:")){
						
						int x =((DecisionNode)pileJetons.pop().getActivite()).getOutgoings().size();

					}
					else {
						throw new ParsingException("Une Excpetion vraiment inattendue !");
					}
					
				}
				
				if (jetonLu.getTokentype().equals("Fin:")){
					inform("Token Fin:");
					UmlFactory.addControlFlow(SuperActivity, SuperControlFlow instanceof DecisionNode?corpControlFlow:"", SuperControlFlow, getFinalNode(SuperActivity));
				}
				if (jetonLu.getTokentype()=="Documentation"){
					pileJetons.push(jetonLu);
				}
					
				logJeton.logStack(pileJetons);
				
			} //boulce while fin fichier


		}
		catch (IOException e1) {
			e1.printStackTrace(out);
		}
		
	}

	/** Get Jeton from file
	 *  The method perform all lexical analysis : 
	 *  - Ignore comments and empty lines
	 *  - Get 
	 * @param pileJetons
	 * @param in
	 * @param numlignes
	 * @return
	 */
	private Jeton extraireJeton(Stack<Jeton> pileJetons,String ligneLue, int numlignes) {
		// TODO Module de remplacement de m�thode auto-g�n�r�
		
		Pattern patternCommentAlgo;
		Pattern patternCommentCobol;
		Pattern patternLigneVide;
		Pattern patternMotCleOuvrir;
		Pattern patternMotCleFermer;
		Pattern patternMotCleAutre;

		Pattern pattern;

	    Matcher matcherCommentAlgo;
	    Matcher matcherCommentCobol; 
	    Matcher matcherLigneVide;
	    Matcher matcherMotCleOuvrir; 
	    Matcher matcherMotCleFermer;
	    Matcher matcherMotCleAutre;

	    Matcher matcher; 
	    
	    boolean b;
	    int niveau = -1;
	    
	    String instruction="";
	    String tokenType ="";
	    String firstWord="";
	    boolean isMotCle = false;
	    isMotCle = false;

	    
	    //inform("ligneLue:"+ligneLue);
	    pattern = Pattern.compile("(^\\t*)(Faire|Si|Cas|\\*|[a-zA-Z-0-9��������@!#&?]+\\s)(.*)"); //"\\p{L}*"
	    
	    patternCommentAlgo = Pattern.compile("(^\\s*)(//)(.*)");
	    patternLigneVide = Pattern.compile("(\\s*)");
	    patternCommentCobol = Pattern.compile("(.{6})(\\*)(.*)");
	    patternMotCleOuvrir = Pattern.compile("(\\s*)(Faire:|Si:|Cas:)(.*)");
	    patternMotCleFermer = Pattern.compile("(\\s*)(Fin-Faire:|Fin-Si:)(.*)");
	    patternMotCleAutre = Pattern.compile("(\\s*)(Fin-Anormale:|Faire:.*#:)(.*)");
	      	    
		matcher = pattern.matcher(ligneLue);
		matcherCommentAlgo = patternCommentAlgo.matcher(ligneLue);
		matcherLigneVide = patternLigneVide.matcher(ligneLue);
		matcherCommentCobol = patternCommentCobol.matcher(ligneLue);
		matcherMotCleOuvrir = patternMotCleOuvrir.matcher(ligneLue);
	    matcherMotCleFermer = patternMotCleFermer.matcher(ligneLue);
		matcherMotCleAutre = patternMotCleAutre.matcher(ligneLue);


		if (matcherCommentCobol.matches()){
			//inform("Ligne Commentaire Cobol ("+numlignes+")"+ligneLue);
		}
		
		// Detection de la nature de la ligne
		if(matcherLigneVide.matches()){
			return new Jeton(numlignes,"LigneVide",false,"",false,false, -99,null);
		}
		if(matcherCommentAlgo.matches()){
			return new Jeton(numlignes,"Commentaire",false,matcherCommentAlgo.group(3),false,false, -99,null);
		}
		if(matcherMotCleOuvrir.matches()){
			return new Jeton(numlignes,matcherMotCleOuvrir.group(2),true,matcherMotCleOuvrir.group(3),false,true, -99,null);
		}
		if(matcherMotCleFermer.matches()){
			if (matcherMotCleFermer.group(3).matches("\\s*[a-zA-Z-0-9��������@!#&?]+\\s*")) 
				throw new ParsingException("Erreur syntaxique :" +numlignes+"\n aucun mot ne doit apparaitre sur la m�me ligne qu'un mot cl� de fermeture.");
			return new Jeton(numlignes,matcherMotCleFermer.group(2),true,matcherMotCleFermer.group(3),false,false, -99,null);
		}
		if(ligneLue.matches("Fin:")){
			return new Jeton(numlignes,"Fin:",true,"",false,false, -99,null);
		}
		//ajouter des tests des incoh�rences sur la ligne : plusieurs mots-cl�s sur la meme ligne :  Si: condition A Faire: 
		//ou des motif qui figure avant un mot cl� : mettre le top � z�ro Fin-Faire:
		//bref toutes les inco�hrences qu'on peut d�tecter sur la ligne seule
		return new Jeton(numlignes,"Documentation",false,ligneLue,false,false, -99,null);

		//else
		//	throw new ParsingException("Erreur syntaxique :" +numlignes+"\n aucun motif valide n'est reconnu dans la ligne lue.");
		
			
	}
	private boolean containsDocOnly(Stack<Jeton> pileJetons) {
		// TODO Module de remplacement de m�thode auto-g�n�r�
		
		boolean docOnly =false;
		for(Iterator iter = pileJetons.iterator(); iter.hasNext();){
			Jeton unJeton = (Jeton)iter.next();
			out.println("ContainsDocOnly > unJeton:"+unJeton);
			//out.println("!(unJeton.getTokentype()==\"Documentation\"):"+!(unJeton.getTokentype()=="Documentation"));
			//out.println("!(unJeton.getTokentype()==\"PrincipalActivity\"):"+!(unJeton.getTokentype()=="PrincipalActivity"));

			if(!(unJeton.getTokentype()=="Documentation")){ //|| !(unJeton.getTokentype()=="PrincipalActivity")){
				docOnly = false;
				break;
			}
			else{
				docOnly = true;
			}
		}
		
		return docOnly;
	}
	
	private void traiterDocDebut(Stack<Jeton> pileJetons, Object activity1) {
		// TODO Module de remplacement de m�thode auto-g�n�r�
		
		String documentation ="";
		Stack<Jeton> pileJetonsAtraiter = new Stack<Jeton>();
		
		//On est sur que la pile ne contient que des doc, donc d�piler
		//while (pileJetons.peek().getTokentype()=="Documentation"){
		while (pileJetons.size()>0){
			documentation = documentation+(pileJetons.pop().getCorps()+"\n");

		}
				
		documentation = ReplaceCobolToFrancais(documentation);
		//out.println("La doc recup�r�e :"+documentation);
		
		//Adding comment
		//if ((activity1 instanceof Action) || (activity1 instanceof Activity) ||
		//	(activity1 instanceof StructuredActivityNode) || (activity1 instanceof Component)){
			
		out.println("Creation de la documentation du d�but ..");

			
		Comment unComment;
		if(((NamedElement)activity1).getOwnedComments().size()>0){
			unComment = ((NamedElement)activity1).getOwnedComments().get(0);
		}
		else
		{
			unComment=((NamedElement)activity1).createOwnedComment();
		}
			
			
		unComment.setBody(documentation);
		unComment.getAnnotatedElements().add((NamedElement)activity1);

		Stereotype docStereotype = unComment.getApplicableStereotype("Default::Documentation");
			
			if (docStereotype == null) {
				inform("Probl�me Stereotype = pas de documentation appliqu�e");
			}
			else{
				if (!unComment.isStereotypeApplied(docStereotype))
					UMLUtil.safeApplyStereotype(unComment, docStereotype);
			}
		
	}
	private InitialNode getInitialNode(NamedElement activity1) {
		// TODO Module de remplacement de m�thode auto-g�n�r�
		//inform("getInitialNode de:"+activity1);
		if (activity1 instanceof OpaqueAction){
			inform("can not get InitialNode for OpaqueActions !");
			return null;
		}
		
		else{
		
			if(activity1 instanceof Activity){
			//	inform ("InitialNode de l'activity:"+((InitialNode)((Activity)activity1).getNode("D�but", true, UMLPackage.eINSTANCE.getInitialNode(), false)));
				return (InitialNode)(((Activity)activity1).getNode("D�but", true, UMLPackage.eINSTANCE.getInitialNode(), false));
			}
			else{
				//inform ("InitialNode de la SA:"+(((StructuredActivityNode)activity1).getNode("D�but",true,UMLPackage.eINSTANCE.getInitialNode(),false)));
				return (InitialNode)(((StructuredActivityNode)activity1).getNode("D�but",true,UMLPackage.eINSTANCE.getInitialNode(),false));
			}
		}
	}
	
	private FinalNode getFinalNode(NamedElement activity1) {
		// TODO Module de remplacement de m�thode auto-g�n�r�
		
		return (FinalNode) (activity1 instanceof OpaqueAction ? null :
							activity1 instanceof Activity ?
							((Activity)activity1).getNode("Fin", true, UMLPackage.eINSTANCE.getFinalNode(), false):
							((StructuredActivityNode)activity1).getNode("Fin",true,UMLPackage.eINSTANCE.getFinalNode(),false));
		
	}
	
}
