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

	public void plugletmain(String[] args) {
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
		// TODO Auto-generated method stub
		
    	final List elements = UMLModeler.getUMLUIHelper().getSelectedElements("org.eclipse.ui.navigator.ProjectExplorer");
  	  	NamedElement elementSelectionne = null;
  	  	
  	  	switch (elements.size()) {
		case 0:
     		throw new SelectionComposantException("Problème de selection de composant/activité/activité structuré \n aucun élément selectionné.");
		case 1:
            elementSelectionne = (NamedElement)elements.get(0);
           	out.println(elementSelectionne.getQualifiedName());
            inform("Element du travail : "+elementSelectionne.getName() );
			break; 
		default:
     		throw new SelectionComposantException("Problème de selection de composant/activité/activité structurée \n plusieurs éléments selectionnés.");
		}
        return elementSelectionne;
		
	}
	
	/** Get the file containing the algorithm to model
	 *  a prompt is used to get user answer. The user's answer is saved to windows registry for next time. 
	 * @return BufferedReader AlgorithmFile
	 */
	private BufferedReader getAlgorithmFile() {
		// TODO Auto-generated method stub
		
		BufferedReader in=null;
		String cheminInitial=null;
		String cheminRenseigne=null;
				
		cheminInitial = getCheminInitial();
		
		cheminRenseigne = prompt("Entrer le chemin de fichier qui contient l'algorithme à modéliser :" , cheminInitial, "Chemin de fichier d'algorithme");
		
		if(cheminRenseigne==null |cheminRenseigne==""|cheminRenseigne.matches("\\s+"))
			throw new SelectionComposantException("Aucune saisie n'a été effectuée");
		
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(cheminRenseigne),"CP1252")); // CP1252 (Western European languages) //ISO-8859-1

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace(out);
			    throw new RuntimeException(e);

			}
			catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
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
		// TODO Auto-generated method stub

		String locationReg="HKCU\\Software\\Spaich\\RetroSpec";
		String keyReg="SetupPath";
		
		RegKeyManager rkm = new RegKeyManager();
		try {
			rkm.delete(locationReg, keyReg);
			rkm.add(locationReg, keyReg, "REG_SZ",cheminRenseigne);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		out.println("setCheminInitial():"+cheminRenseigne);
	}

	/** Get user's last choice (Algorithm file path) from the windows registry.
	 * @return 
	 */
	private String getCheminInitial() {
		// TODO Auto-generated method stub
		
		String locationReg="HKCU\\Software\\Spaich\\RetroSpec";
		String keyReg="SetupPath";
		String cheminInitial="";
		
		RegKeyManager rkm = new RegKeyManager();
		try {
			rkm.query(locationReg, keyReg);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		cheminInitial=rkm.getValue();
		out.println("getCheminInitial():"+cheminInitial);
		
		return cheminInitial;
	}

	protected void createActivityDiagram(NamedElement elementSelectionee, BufferedReader in) {
		
		//Adding or getting Principal Activity node : Component, Activity, 
		//Structured Activity or Opaque Action. 
		out.println("Adding or getting Principal Activity .."); 
		Object activity1 = addActivity(elementSelectionee, "Activité du programme");
		
		//Adding or getting Activity Diagram if selected element is Component, 
		//Activity, Structured Activity else if Opaque Action no need for Diagram
		out.println("Adding or getting Principal Diagram ..");
		Diagram diagram = addActivityDiagram(activity1, "Diagramme d'algorithme");	

		//Boolean pour ajouter une itération après fin lecture, pour achever tous.
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
				
		//Création et push d'un premier jeton
		//pileJetons.push(new Jeton(-1,"PrincipalActivity",false,"0000000",true,false, -1,activity1));
		
		//Extraction des jetons
		try {
			while(((ligneLue = in.readLine())!=null) || bonusIterationAconsommer){
				
				//utilisation de l'itération bonus
				if (ligneLue==null) {bonusIterationAconsommer=false;ligneLue="Fin:";}
				
				numlignes++;
				Jeton jetonLu=null;
				
				jetonLu =extraireJeton(pileJetons,ligneLue,numlignes);
				
				out.println("jetonLu:"+jetonLu);
				
				//Si la doc deb est à traiter
				if(!docDebutTraite)
					{
					out.println("docDebut est à traiter => tester le jeton lu");
					//Si le jeton lu est différent de documentation (ou des jetons neutres)
					if(!(jetonLu.getTokentype()=="Documentation") && !(jetonLu.getTokentype()=="LigneVide") && !(jetonLu.getTokentype()=="Commentaire"))
						{
						out.println("JetonLu n'est pas doc => tester le contenu de la pile");
						//on rentre plus dans la boucle
						docDebutTraite = true;
						//Si la pile contient que des docs sans mot clé (ouvrant surtout)
						if (containsDocOnly(pileJetons))
							{
							//avant le traitement de la doc debut tester :
							//si le jeton qui met fin a la doc debut est un mot-clé, et l'element selecionné est une action=> Exception
							out.println("containsDocOnly => evaluer l'element selectionne puis traiter Doc début");
							if (!(jetonLu.getTokentype()=="Fin:") && (activity1 instanceof OpaqueAction)){throw new ParsingException("Une Action est selectionnée, mais il ne peut pas contenir des éléments autres qu'une documentation.");}

							traiterDocDebut(pileJetons,activity1);

							}
						}
					}
						//throw new ParsingException("Documentation orpheline non autorisée au milieu du l'algorithme. ligne :"+numlignes);
				
				out.println("JetonLu TokenType :"+jetonLu.getTokentype());
				
				//FTA : Ajouter une analyse sytaxique (vérifier la suite des lignes) et extraire les erreurs.
				if ((jetonLu.getTokentype()).equals("Faire:")){
					pileJetons.push(jetonLu);
				}
				
				if (jetonLu.getTokentype()=="LigneVide"){
					//jeton ignoré
				}
				if (jetonLu.getTokentype()=="Commentaire"){
					//jeton ignoré
				}

				if (jetonLu.getTokentype().equals("Si:")){
					pileJetons.push(jetonLu);
				}
				if (jetonLu.getTokentype().equals("Cas:")){
					pileJetons.push(jetonLu);
				}
				if (jetonLu.getTokentype().equals("Fin-Faire:")){
					//Le Fin-Faire: peut être celui d'une action ou SA

					//////////////////////////////////////////
					//       Phase Dépilement               //
					//////////////////////////////////////////
					
					out.println("Fin-Faire: detecté, dépiler les jetons jusqu'à Faire: pour les traiter");

					Stack<Jeton> pileJetonsAtraiter = new Stack<Jeton>();

					while(!(pileJetons.peek().getTokentype().equals("Faire:")) && (pileJetons.size()>0)){
						pileJetonsAtraiter.push(pileJetons.pop());
					}
					//dépiler en plus le jeton Faire:
					pileJetonsAtraiter.push(pileJetons.pop());

					out.println("Nombre de jetons dans pileJetonsAtraiter :"+pileJetonsAtraiter.size());
					logStack(pileJetonsAtraiter);
					
					Stack<Jeton> pileJetonsDemiTraites = new Stack<Jeton>();
					
					//on suppose que les jetons non créés sont empilés dans la pile après ceux créés
					while((pileJetons.size()>0) && (!(pileJetons.peek().isEstCree()))){
						pileJetonsDemiTraites.push(pileJetons.pop());
					}
					
					out.println("Nombre de jetons dans pileJetonsDemiTraites :"+pileJetonsDemiTraites.size());
					logStack(pileJetonsDemiTraites);
					
					//////////////////////////////////////////
					//       Phase Traitement               //
					//////////////////////////////////////////
					
					//Traitement des jetons à demi-traiter
					while(pileJetonsDemiTraites.size()>0){
						Jeton unJetonaDemiTraiter=pileJetonsDemiTraites.pop();
						NamedElement Tampon;
						
						//Si Faire: => créer Structured Activity
						if(unJetonaDemiTraiter.getTokentype().equals("Faire:")){
							//Nom de la Structured Activity
							String nomSA = unJetonaDemiTraiter.getCorps();
							//La doc dans l'enventualité qu'elle existe
							String docSA="";
							while(pileJetonsDemiTraites.size()>0 && pileJetonsDemiTraites.peek().getTokentype().equals("Documentation")){
								docSA = docSA+pileJetonsDemiTraites.pop().getCorps()+"\n";
							}
							docSA = ReplaceCobolToFrancais(docSA);
							//Creation de la SA
							Tampon=addStructuralActivity(SuperActivity, nomSA, docSA);
							
							//Création de CF : dans superActivity, avec son nom, sa source et sa destination
							//inform("Création de ControlFlow Initial de la SA. \n"+
							//	   "Créer dans :"+SuperActivity.getQualifiedName()+"\n"
								//	+
								 //  "Nom de CF :"+(SuperControlFlow instanceof DecisionNode?corpControlFlow:"")+"\n"+
								 //  "Source :"+SuperControlFlow.getQualifiedName()+"\n"+
								 //  "Target :"+Tampon.getQualifiedName()
								 //  );
							addControlFlow(SuperActivity, SuperControlFlow instanceof DecisionNode?corpControlFlow:"", SuperControlFlow, Tampon);
							
							//La SA créée devient SuperActivity
							SuperActivity = Tampon;
							
							//le SuperControlFlow devient le noeud initial de la nouvelle SA
							SuperControlFlow = getInitialNode(SuperActivity);
							//inform("SuperActivity"+SuperActivity.getQualifiedName());
							//inform("SuperControlFlow devient:"+SuperControlFlow.getQualifiedName());

							
							//retourner la SA dans la pile à jetons
							unJetonaDemiTraiter.setEstCree(true);
							pileJetons.push(unJetonaDemiTraiter);
						}
						//Si Si: => créer Noeud de décision
						if(unJetonaDemiTraiter.getTokentype().equals("Si:")){
							
							//Nom de noeud de décision
							String nomND = unJetonaDemiTraiter.getCorps();
							//La doc dans l'enventualité qu'elle existe
							String docND="";
							while(pileJetonsDemiTraites.size()>0 && pileJetonsDemiTraites.peek().getTokentype().equals("Documentation")){
								docND = docND+pileJetonsDemiTraites.pop().getCorps()+"\n";
							}
							docND = ReplaceCobolToFrancais(docND);
							//Creation de NC
							DecisionNode ND = addnoeudDecision(SuperActivity, nomND, docND);
							
							//Création de CF : dans superActivity, avec son nom, sa source et sa destination
							addControlFlow(SuperActivity, SuperControlFlow instanceof DecisionNode?corpControlFlow:"", SuperControlFlow, ND);
							
							//SuperControleFlow devient le noeud Decision créé.
							SuperControlFlow = ND;
							
							//retourner la SA dans la pile à jetons
							unJetonaDemiTraiter.setEstCree(true);
							unJetonaDemiTraiter.setActivite(ND);
							pileJetons.push(unJetonaDemiTraiter);
						}
						//Si Cas: => changer le superCF
						if(unJetonaDemiTraiter.getTokentype().equals("Cas:")){
							corpControlFlow=unJetonaDemiTraiter.getCorps();
							//Chercher la ND qui va devenir le superCf dans la pile à Jetons
							//je parie que c'est le premier Jetons à pop de la pileJetons
							if(pileJetons.peek().getTokentype().equals("Si:")){
								SuperControlFlow = (NamedElement)(pileJetons.peek().getActivite());
							}
							else{
								throw new ParsingException("Une Excpetion vraiment inattendue !");
							}
							
						}
						
					}
					
					//Traitement des jetons à full-traiter (à fermer)
					while(pileJetonsAtraiter.size()>0){
						Jeton unJetonATraiter=pileJetonsAtraiter.pop();
						out.println("SuperActivity:"+SuperActivity); 
						out.println("SuperActivity dans:"+((((StructuredActivityNode)(SuperActivity)).getInActivity()==null)?
															((StructuredActivityNode)(SuperActivity)).getInStructuredNode():
															((StructuredActivityNode)(SuperActivity)).getInActivity()));

						if(unJetonATraiter.isEstCree()){
							//Fermer la SA et retourner au niveau sup
							//FTA : Traiter aussi les activités
							//Mais avant de fermer, lier le dernier element avec le noeud de fin
							//il faudra traiter le cas de plusieurs noeuds derniers !! 
							//créér peut etre une collection de superControlFlow et les attacher au finalNode
							addControlFlow(SuperActivity, SuperControlFlow instanceof DecisionNode?corpControlFlow:"", SuperControlFlow, getFinalNode(SuperActivity));
							
							//NamedElement tampon = SuperActivity;
							if(SuperActivity instanceof StructuredActivityNode){
								//changer le SuperControleFlow : il devient la SA dont en sort
								SuperControlFlow=SuperActivity;

								//dans l'affectation ci-dessous on teste le conteneur de notre SA si une autre SA ou Activité
								SuperActivity = (((StructuredActivityNode)(SuperActivity)).getInActivity()==null)?
												((StructuredActivityNode)(SuperActivity)).getInStructuredNode():
												((StructuredActivityNode)(SuperActivity)).getInActivity();
								
							}
							else{//                        car normalement n'est Cree que les SA
								throw new ParsingException("Exception très difficile à comprendre !");
							}
						}
						else{
							//Traiter l'action opaque
							//Nom de l'action opaque
							String nomAO = unJetonATraiter.getCorps();
							//La doc dans l'enventualité qu'elle existe
							String docAO="";
							while(pileJetonsAtraiter.size()>0 && pileJetonsAtraiter.peek().getTokentype().equals("Documentation")){
								docAO = docAO+pileJetonsAtraiter.pop().getCorps()+"\n";
							}
							docAO = ReplaceCobolToFrancais(docAO);
							//Creation de la AO
							OpaqueAction OA = (OpaqueAction)addAction(SuperActivity, nomAO, docAO);
							addControlFlow(SuperActivity,SuperControlFlow instanceof DecisionNode?corpControlFlow:"",
											SuperControlFlow, OA);
							SuperControlFlow =OA;
						}
							
					}

					
				}
				if (jetonLu.getTokentype().equals("Fin-Si:")){
					out.println("Fin-Si: detecté, dépiler les jetons jusqu'à Si: pour les traiter");

					/*
					Stack<Jeton> pileJetonsAtraiter = new Stack<Jeton>();

					while(!(pileJetons.peek().getTokentype().equals("Si:")) && (pileJetons.size()>0))
						{
						pileJetonsAtraiter.push(pileJetons.pop());
						}
					//dépiler Si:
					pileJetonsAtraiter.push(pileJetons.pop());

					out.println("Taille de la pileJetonsAtraiter :"+pileJetonsAtraiter.size());
					logStack(pileJetonsAtraiter);
					*/
					
					//je parie là aussi que c'est le premier Jetons à pop de la pileJetons
					if(pileJetons.peek().getTokentype().equals("Si:")){
						
						int x =((DecisionNode)pileJetons.pop().getActivite()).getOutgoings().size();

					}
					else{
						throw new ParsingException("Une Excpetion vraiment inattendue !");
					}
					
				}
				
				if (jetonLu.getTokentype().equals("Fin:")){
					inform("Token Fin:");
					addControlFlow(SuperActivity, SuperControlFlow instanceof DecisionNode?corpControlFlow:"", SuperControlFlow, getFinalNode(SuperActivity));
				}
				if (jetonLu.getTokentype()=="Documentation"){
					pileJetons.push(jetonLu);
				}
				//else
				//	throw new ParsingException("Type de jeton inconnu !");
					
				logStack(pileJetons);
				
				
				
				//UMLModeler.getUMLDiagramHelper().layoutNodes(mainDiagram.getChildren(), "DEFAULT");

				

				
				
				/*
				levelDif = pileJetons.lastElement().getNiveau() - jetonLu.getNiveau();
				out.println("Niveau stocke moins Niveau lu :"+ levelDif );
				out.println("Dernier jeton stock <"+pileJetons.lastElement().toString());
				out.println("Jetonlu <"+jetonLu.toString());
				
				
				if (levelDif <= 0){
					//pas de retour de niveau => push
					out.println("pas de retour de niveau => push");
					pileJetons.push(jetonLu);
					logStack(pileJetons);
					
					}
				else{
					//retour de niveau => Traiter/fermer/pop les noeuds jusque lvl de jeton lu
					//à la fin de fermeture, aucun 
					out.println("retour de niveau => Traiter/fermer/pop les noeuds jusque lvl de jeton lu :"+jetonLu.getNiveau());
					
					Stack<Jeton> pileJetonsAtraiter = new Stack<Jeton>();
					
					//Extraction des jetons à traiter/fermer 
					out.println(">>>Extraction des jetons à traiter/fermer ");
					while(pileJetons.peek().getNiveau()>=jetonLu.getNiveau()){
						out.println("Extraction 1 vers pile a traiter de pileJetons.peek()"+pileJetons.peek().toString());
						pileJetonsAtraiter.push(pileJetons.pop());
						}
					out.println("Taille de la pileJetons         :"+pileJetons.size());
					logStack(pileJetons);
					out.println("Taille de la pileJetonsAtraiter :"+pileJetonsAtraiter.size());
					logStack(pileJetonsAtraiter);
					
					
					//le peek element de la pile des jetons est le jeton parent(activity) des jetons à traiter
					//il peut etre non cree et ses parent aussi, donc remonter jusqua activity -1 ou créé
					out.println(">>>Extraction des jetons non créé (à priori les activités structurées non créés) ");
					while(!pileJetons.peek().isEstCree()){
						out.println("Extraction 2 vers pile a traiter de pileJetons.peek()"+pileJetons.peek().toString());
						pileJetonsAtraiter.push(pileJetons.pop());
						}
					out.println("Taille de la pileJetons         :"+pileJetons.size());
					logStack(pileJetons);
					out.println("Taille de la pileJetonsAtraiter :"+pileJetonsAtraiter.size());
					logStack(pileJetonsAtraiter);
					
					//Recuperer l'activité parent auquelle grefer les autres (superJeton peut etre le premier diagram !)
					Jeton superJeton = pileJetons.peek();
					out.println("Super Jeton"+superJeton.toString());
					
					//traiter les jetons Actvités structurés non créés et les rendre à la pile principale
					out.println(">>>Traitement des jetons Actvités structurés non créés et les rendre à la pile principale");
					while((pileJetonsAtraiter.peek().getNiveau()<jetonLu.getNiveau())) {			//&& !(pileJetonsAtraiter.peek().isEstCree()))

						out.println("Création du jeton à rendre:"+pileJetonsAtraiter.peek().toString());
						//à ce niveau j'ai une AS non créé à créé et à attacher au super jeton
						//Creer le jeton, de l'activité et l'attacher au super
						
						//ATTTENTIOOOOOOOOOOOON à getActivty retroune l'activté mère ou fille????
						//Attention reglé normalement par la modif delactivie de jeton en objet ?!
						pileJetonsAtraiter.peek().setActivite((addStructuralActivity(superJeton.getActivite(),pileJetonsAtraiter.peek().getCorps(),pileJetonsAtraiter.peek().getCorps())));
						pileJetonsAtraiter.peek().setEstCree(true);
						
						//inform("SA créée :"+((StructuredActivityNode)pileJetonsAtraiter.peek().getActivite()).getName()+"\n"+
								//"Dans :"+((StructuredActivityNode)superJeton.getActivite()).getName()+"\n"+
							//	"Le superJeton Devient :"+((StructuredActivityNode)pileJetonsAtraiter.peek().getActivite()).getName());
						
						//le superJeton devient le noeud fraichement créé.
						superJeton = pileJetonsAtraiter.peek();
						
						//Rendre le jetons dans la pile principal pour servir comme superJeton
						out.println("Création du jeton :"+pileJetonsAtraiter.peek().toString());
						pileJetons.push(pileJetonsAtraiter.pop());
						}
					
					out.println("Taille de la pileJetons         :"+pileJetons.size());
					logStack(pileJetons);
					out.println("Taille de la pileJetonsAtraiter :"+pileJetonsAtraiter.size());
					logStack(pileJetonsAtraiter);
					
					//traiter les jetons à fermer (non retrouné à la pile principal, car fini)
					out.println(">>>Traitement des jetons à fermer et les jeter");
					while((pileJetonsAtraiter.size()>0)&&(pileJetonsAtraiter.peek().getNiveau()>=jetonLu.getNiveau()))
						{
						out.println("Création du jeton à fermer:"+pileJetonsAtraiter.peek().toString());
						//à ce niveau j'ai un jeton à créér, fais gaffe au motclé C !
						
						//Patch Bug : droper les jetons (en prticulier les SA déja créée, pour eviter de dupliquer)
						if(pileJetonsAtraiter.peek().isEstCree()){
							pileJetonsAtraiter.pop();
							}
						else
							{
							//pour le moment dropper les C
							if (pileJetonsAtraiter.peek().getTokentype().equals("Cas"))
								{ out.println("Drop de C"); pileJetonsAtraiter.pop();
								}
							else
								{
								//Evaluer type de motCle
								if(pileJetonsAtraiter.peek().getTokentype().equals("Faire"))
									{
									Jeton jetonFaire = pileJetonsAtraiter.pop();
									
									boolean isActionOpaque = pileJetonsAtraiter.peek().isEstMotCle() ? false :true;
									out.println("s'agit-il de créer une actionOpaque ?:"+isActionOpaque+" car premierMot :"+pileJetonsAtraiter.peek().getTokentype());
									
									if (isActionOpaque)
										{
										//Creer l'action et collecter la documentation
										String documentation = "";
										while(pileJetonsAtraiter.size()>0)
											{
											if(!pileJetonsAtraiter.peek().isEstMotCle())
												{
												out.println("Extraction doc"+pileJetonsAtraiter.peek().toString());
												documentation = documentation+(pileJetonsAtraiter.pop().getCorps()+"\n");
												}
											}
											
											documentation = ReplaceCobolToFrancais(documentation);
											out.println("La doc recupérée :"+documentation);
											ActivityNode noeudactivite = addAction(superJeton.getActivite(),jetonFaire.getCorps(),documentation);
											
											out.println("Action ajoutée avec succes");
											
											//inform("Action créée :"+noeudactivite.getName());
													//"Dans :"+((StructuredActivityNode)superJeton.getActivite()).getName()+"\n"+
													//"Le superJeton Devient :"+((StructuredActivityNode)pileJetonsAtraiter.peek().getActivite()).getName());
											
										}
									else{
										//Activité Structurée
										//inform("Une activité structurée à créé :"+jetonFaire.getCorps());
										//out.println("--------------------------------------------");
										//out.println("--- " +pileJetonsAtraiter.size()+ " ---");
										//out.println("--------------------------------------------");
										//for(int i=0; i<pileJetonsAtraiter.size(); i++){
								         //     out.println("jeton à traiter( " +i+"):"+ pileJetonsAtraiter.get(i));
								         //}
										Object uneActivite = addStructuralActivity(superJeton.getActivite(),jetonFaire.getCorps(), jetonFaire.getCorps());
										//out.println("uneActivite ="+uneActivite.getName());
										superJeton.setActivite(uneActivite);
										out.println("SuperJeton apres creation SA :"+superJeton.getActivite().toString());
										inform("dis donc");
										//inform("SA créée :"+((StructuredActivityNode)uneActivite).getName()+"\n"+
										//"Dans :"+((StructuredActivityNode)uneActivite).getName()+"\n"+
										//"Le superJeton Devient :"+((StructuredActivityNode)pileJetonsAtraiter.peek().getActivite()).getName());
										
										}
									}
								else
									{
									if (pileJetonsAtraiter.peek().getTokentype().equals("Si"))
										{
										//Creer le noeud de décision
										Jeton jetonSi = pileJetonsAtraiter.pop();
										String documentation = "";
										while(pileJetonsAtraiter.size()>0)
											{
											if(!pileJetonsAtraiter.peek().isEstMotCle())
												{
												out.println("Extraction doc"+pileJetonsAtraiter.peek().toString());
												documentation = documentation+(pileJetonsAtraiter.pop().getCorps()+"\n");
												}
											}
											
											documentation = ReplaceCobolToFrancais(documentation);
											out.println("La doc recupérée :"+documentation);
											addnoeudDecision(superJeton.getActivite(),jetonSi.getCorps(),documentation);
											out.println("Decision ajoutée avec succes");
										}
									else{
										throw new MotCleAttenduIntrouvableException("MotCle : Faire,Si ou Cas attendu mais "+pileJetonsAtraiter.peek().getTokentype()+"trouvé");
										}
									} // if Faire
			
								}//If exclure les Cas
							
							}//If exclure les noeud isEstCree
						
						} //boucle while Trait/fermeture noeuds
						
					//Patch pour Bug : Les SA qui constitue SuperJeton étant fermé => mettre à jour Super jeton vers peek de la pile des jetons
					superJeton =pileJetons.peek();
					//Enfin ! après avoir débarraser la pile des noeud fermé et création des superJeton => push
					out.println("Enfin ! après une guerre ! => push jeton lu");
					pileJetons.push(jetonLu);
					logStack(pileJetons);
					//out.println("pileJetons.size()  => "+ pileJetons.size());
					//out.println("pileJetons.peek()  => "+ pileJetons.peek());
					}
			
			*/ //Fin commentaire de la partie indentation.
			//////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////
			} //boulce while fin fichier


		}
		catch (IOException e1) {
			e1.printStackTrace(out);
		}
		/*
		for (Iterator iter= diagram.getChildren().iterator();iter.hasNext();){
			Node noeud = (Node) iter.next();
			inform("noeud.LayoutConstraint:"+noeud.getLayoutConstraint().toString());
			inform("noeud.type:"+noeud.getType());
			out.println(noeud.toString());
			inform("noeud:"+noeud.toString());
			
			inform("getchildren(0).toString:"+noeud.getChildren().get(0).toString());
			inform("getchildren(0).getChildren().size:"+((Node)noeud.getChildren().get(0)).getChildren().size());
			
			
			inform("getchildren(0).getClass:"+noeud.getChildren().get(0).getClass());

			//((UMLShapeCompartment)noeud.getChildren().get(0)).setCollapsed(true);
			//inform(""+((UMLShapeCompartment)noeud.getChildren().get(0)).getChildren().size());

			
			inform("getchildren(1).toString:"+noeud.getChildren().get(1).toString());

			
			for (Iterator iter2= noeud.getChildren().iterator();iter2.hasNext();){
				inform("noeud.getChildren:"+noeud.getChildren().size());
				UMLShapeCompartment noeudin = (UMLShapeCompartment) iter2.next();
				inform("noeudin.LayoutConstraint:"+noeudin.getLayoutConstraint().toString());
				inform("noeud.type:"+noeudin.getType());
				out.println(noeudin.toString());
				inform("noeud:"+noeudin.toString());
			}
			
		} */
		
		/*
		try {
			while((ligneLue = in.readLine())!=null){
				numlignes++;
				
				if (ligneLue.indexOf("Faire") != -1){
					if (faireTrouve){
						
						out.println("Adding StructuralActivity .."); 
						String nomActivitestructuree[] = ligneLue.split("Faire");
						//Add an StructuralActivity to the activity
						StructuredActivityNode activiteStructuree = addStructuralActivity(activity1, ligneSave.substring(ligneSave.indexOf("Faire")+5),ligneSave.substring(ligneSave.indexOf("Faire")+5));
					}
					faireTrouve = true;
					ligneSave = ligneLue;					
				}
				else{
					faireTrouve = false;
					out.println("Adding action .."); 
					//Add an action to the activity
					
					ActivityNode action = addAction(activity1, ligneSave.substring(ligneSave.indexOf("Faire")+5),ligneLue);
				}
				
			}
		} catch (IOException e1) {
			e1.printStackTrace(out);
		}
		*/
		
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
		// TODO Module de remplacement de méthode auto-généré
		
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
	    pattern = Pattern.compile("(^\\t*)(Faire|Si|Cas|\\*|[a-zA-Z-0-9éàèêçùôâ@!#&?]+\\s)(.*)"); //"\\p{L}*"
	    
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
			if (matcherMotCleFermer.group(3).matches("\\s*[a-zA-Z-0-9éàèêçùôâ@!#&?]+\\s*")) 
				throw new ParsingException("Erreur syntaxique :" +numlignes+"\n aucun mot ne doit apparaitre sur la même ligne qu'un mot clé de fermeture.");
			return new Jeton(numlignes,matcherMotCleFermer.group(2),true,matcherMotCleFermer.group(3),false,false, -99,null);
		}
		if(ligneLue.matches("Fin:")){
			return new Jeton(numlignes,"Fin:",true,"",false,false, -99,null);
		}
		//ajouter des tests des incohérences sur la ligne : plusieurs mots-clés sur la meme ligne :  Si: condition A Faire: 
		//ou des motif qui figure avant un mot clé : mettre le top à zéro Fin-Faire:
		//bref toutes les incoéhrences qu'on peut détecter sur la ligne seule
		return new Jeton(numlignes,"Documentation",false,ligneLue,false,false, -99,null);

		
		//else
		//	throw new ParsingException("Erreur syntaxique :" +numlignes+"\n aucun motif valide n'est reconnu dans la ligne lue.");

		/*
		b = matcher.matches();
		if (b){
				niveau      = matcher.group(1).length();
				isMotCle	= matcher.group(2).equals("Faire") || matcher.group(2).equals("Si") || matcher.group(2).equals("Cas");     
				firstWord   = matcher.group(2);
				instruction = isMotCle ? matcher.group(3) : firstWord+matcher.group(3);
			    	
				out.println(numlignes + " : <MotCle:"+ isMotCle +"><" + firstWord + "><" + instruction + "><Niveau:" + niveau+">");
				return new Jeton(numlignes,firstWord,isMotCle,instruction,false,false, niveau,null);
			}
			else {
				out.println(numlignes + "<Ligne vide / Motifs introuvables>");
				throw new ExtractionJetonException("Problème extraction jeton, vérifier syntaxe fichier ligne :"+numlignes);
				
			}
			*/
		
			
	}
	private boolean containsDocOnly(Stack<Jeton> pileJetons) {
		// TODO Module de remplacement de méthode auto-généré
		
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
		// TODO Module de remplacement de méthode auto-généré
		
		String documentation ="";
		Stack<Jeton> pileJetonsAtraiter = new Stack<Jeton>();
		
		//On est sur que la pile ne contient que des doc, donc dépiler
		//while (pileJetons.peek().getTokentype()=="Documentation"){
		while (pileJetons.size()>0){
			documentation = documentation+(pileJetons.pop().getCorps()+"\n");

			//pileJetonsAtraiter.push(pileJetons.pop());
			}
		
		//while (pileJetonsAtraiter.size()>0){
		//	documentation = documentation+(pileJetonsAtraiter.pop().getCorps()+"\n");
		//	}
		
		documentation = ReplaceCobolToFrancais(documentation);
		//out.println("La doc recupérée :"+documentation);
		
		//Adding comment
		//if ((activity1 instanceof Action) || (activity1 instanceof Activity) ||
		//	(activity1 instanceof StructuredActivityNode) || (activity1 instanceof Component)){
			
			out.println("Creation de la documentation du début ..");
			//out.println("element selectionn est instance de component?"+(activity1 instanceof Component));
			
			/*
			for(Iterator<Comment> iterComment = ((NamedElement)activity1).getOwnedComments().iterator();iterComment.hasNext();){
				
				Comment aComment= (Comment)iterComment.next();
				
				inform("aComment body:"+aComment.getBody());
				
				Stereotype docStereotype = aComment.getApplicableStereotype("Default::Documentation");
				
				inform("aComment AppliedStereotype"+aComment.getAppliedStereotype("Default::Documentation"));
			}
			*/
			
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

			Stereotype docStereotype = unComment.getApplicableStereotype(
			"Default::Documentation");
			
			if (docStereotype == null) {
				inform("Problème Stereotype = pas de documentation appliquée");
			}
			else{
				if (!unComment.isStereotypeApplied(docStereotype))
					UMLUtil.safeApplyStereotype(unComment, docStereotype);
			}
		//}
		
	}
	private InitialNode getInitialNode(NamedElement activity1) {
		// TODO Module de remplacement de méthode auto-généré
		//inform("getInitialNode de:"+activity1);
		if (activity1 instanceof OpaqueAction){
			inform("can not get InitialNode for OpaqueActions !");
			return null;
		}
		
		else{
		
			if(activity1 instanceof Activity){
			//	inform ("InitialNode de l'activity:"+((InitialNode)((Activity)activity1).getNode("Début", true, UMLPackage.eINSTANCE.getInitialNode(), false)));
				return (InitialNode)(((Activity)activity1).getNode("Début", true, UMLPackage.eINSTANCE.getInitialNode(), false));
			}
			else{
				//inform ("InitialNode de la SA:"+(((StructuredActivityNode)activity1).getNode("Début",true,UMLPackage.eINSTANCE.getInitialNode(),false)));
				return (InitialNode)(((StructuredActivityNode)activity1).getNode("Début",true,UMLPackage.eINSTANCE.getInitialNode(),false));
			}
		}
	}
		/*
		return (InitialNode) (activity1 instanceof Action ? null :
							  activity1 instanceof Activity ?
							((Activity)activity1).getNode("Début", true, UMLPackage.eINSTANCE.getInitialNode(), false):
							((StructuredActivityNode)activity1).getNode("Début",true,UMLPackage.eINSTANCE.getInitialNode(),false));
		*/
	
	private FinalNode getFinalNode(NamedElement activity1) {
		// TODO Module de remplacement de méthode auto-généré
		
		return (FinalNode) (activity1 instanceof OpaqueAction ? null :
							activity1 instanceof Activity ?
							((Activity)activity1).getNode("Fin", true, UMLPackage.eINSTANCE.getFinalNode(), false):
							((StructuredActivityNode)activity1).getNode("Fin",true,UMLPackage.eINSTANCE.getFinalNode(),false));
		
	}
//*********************************************************************************************************************************//
//*********************************************************************************************************************************//
//****************************             C R E A T I O N     D E S   O B J E T S   U M L          *******************************//     	
//*********************************************************************************************************************************//
//*********************************************************************************************************************************//
	
	// Modifier le premier arguments de Activity à Object, pour permettre le passage de StructuredActivityNode aussi
	private StructuredActivityNode addStructuralActivity(Object activity1,String name, String doc) {
		// TODO Module de remplacement de méthode auto-généré
		EClass newEClass = UMLPackage.eINSTANCE.getStructuredActivityNode();
		
		StructuredActivityNode activiteStructuree = activity1 instanceof Activity ?
													(StructuredActivityNode)((Activity)activity1).createNode(name, newEClass) :
													(StructuredActivityNode) ((StructuredActivityNode)activity1).createNode(name, newEClass);
		activiteStructuree.setName(name);
		activiteStructuree.addKeyword("");
		
		//Adding comment
		out.println("Adding documentation/Eannotation to StructuredActivityNode ..");
		
		Comment unComment = activiteStructuree.createOwnedComment();
		unComment.setBody(doc);
		unComment.getAnnotatedElements().add(activiteStructuree);
		Stereotype docStereotype = unComment.getApplicableStereotype(
		"Default::Documentation");
		
		out.println("Application du stereoType ");
		if (docStereotype == null) {
			inform("Problème Stereotype = pas de documentation appliquee");
		}
		else{
			if (!unComment.isStereotypeApplied(docStereotype))
				UMLUtil.safeApplyStereotype(unComment, docStereotype);
		}
		
		out.println("methode addStructuralActivity, nom  :"+name+" CommentBody:" + unComment.getBody());
		
		Diagram nestedDiagram = UMLModeler.getUMLDiagramHelper().createDiagram(activiteStructuree, UMLDiagramKind.ACTIVITY_LITERAL,activiteStructuree);
		nestedDiagram.setName(activiteStructuree.getName());
		
		addNodeDebut(activiteStructuree,"Début");
		addNodeFinal(activiteStructuree,"Fin");
		
		Diagram diagramParent = UMLModeler.getUMLDiagramHelper().getDiagrams((Namespace)activity1).get(0);
		
		//customizeStructuredActivityShape(activiteStructuree,diagramParent);
		
		
		return activiteStructuree;
	}

	private void customizeStructuredActivityShape(StructuredActivityNode activiteStructuree,Diagram diagram) {
	// TODO Module de remplacement de méthode auto-généré
		
		IUMLDiagramHelper dgmHelper = UMLModeler.getUMLDiagramHelper();
		
		List elements = diagram.getChildren();

		informAllView(diagram);
		/*
		View af = dgmHelper.getChildView( diagram, "Activity Frame"); // frame
		inform("af : "+af);
		inform("af.getchildren.size : "+af.getChildren().get(0));
		inform("af.getchildren.size : "+af.getChildren().get(1));



		View ac = dgmHelper.getChildView( af, "ActivityCompartment"); // main compartment
		inform("ac : "+ac);
		inform("ac.getchildren.size : "+ac.getChildren().size());
		inform("ac.getchildren.size : "+ac.getChildren().get(0));
		*/


		
		
		/*
		out.println("instanceof View :"+(elements.get(0) instanceof View));
		out.println("View getStyles.size() :"+((View)(elements.get(0))).getStyles().size());
		out.println("View getChlidren.size() :"+((View)(elements.get(0))).getChildren().size());
		for (Iterator iter1 = ((View)(elements.get(0))).getChildren().iterator();iter1.hasNext();){
			Node nod = (Node)iter1.next();
			out.println("nod:"+nod.getType());
			if (nod.getType().equals("StructuredActivityNodeCompartment")){
				out.println("Visibility StructuredActivityNodeCompartment:"+nod.isVisible());
				nod.setVisible(false);
				for (Iterator iter2 = nod.getChildren().iterator();iter2.hasNext();){
					Node nod2 = (Node)iter2.next();
					out.println("nod2"+nod2);
			}
		}
		}
		out.println("instanceof Node :"+(elements.get(0) instanceof Node));
		out.println("Node toString:"+elements.get(0));
		out.println("Node LayoutConstraint:"+((Node)elements.get(0)).getLayoutConstraint());
		Bounds bnds =(Bounds)((Node)elements.get(0)).getLayoutConstraint();
		out.println("bnds.getHeight OLD:"+bnds.getHeight());
		bnds.setHeight(1200);
		*/
}

	private void informAllView(View diagram) {
		// TODO Module de remplacement de méthode auto-généré

		for(Iterator iter =diagram.getChildren().iterator();iter.hasNext();){
			//EModelElement mElement = (EModelElement)iter.next();
			Node noeud = (Node)iter.next();
			//View vue =null;

			if (noeud.getType().equals("StructuredActivityNodeCompartment")){
				inform("noeud :"+noeud+"\n visible :"+noeud.isVisible()+"\n Element :"+noeud.getElement());
				noeud.setVisible(false);
				inform("visible :"+noeud.isVisible());
				inform ("noeud layoutconstraint :"+noeud.getLayoutConstraint());

				//Bounds bnds =(Bounds)noeud.getLayoutConstraint();
				//bnds.setHeight(1200);

			}
				
			informAllView(noeud);
		}
	}

	private ControlFlow addControlFlow(NamedElement activity1, String name, NamedElement actionSource, NamedElement actionTarget) {
		// TODO Module de remplacement de méthode auto-généré
		ControlFlow cf = (ControlFlow) (activity1 instanceof Activity? 
										              ((Activity)activity1).createEdge(name,UMLPackage.eINSTANCE.getControlFlow()):
										((StructuredActivityNode)activity1).createEdge(name,UMLPackage.eINSTANCE.getControlFlow()));
		cf.setSource(actionSource instanceof StructuredActivityNode?(StructuredActivityNode)actionSource:(ActivityNode)actionSource);
		cf.setTarget(actionTarget instanceof StructuredActivityNode?(StructuredActivityNode)actionTarget:(ActivityNode)actionTarget);
		
		//((DecisionNode)noeudActivite).getOwnedComments().get(0).setBody("FGX");
		return cf;
	}

	private LoopNode addnoeudloop(Activity activity1, String name) {
		// TODO Module de remplacement de méthode auto-généré
		EClass newEClass = UMLPackage.eINSTANCE.getLoopNode();
		LoopNode aLoopNode = (LoopNode) activity1.createNode(name, newEClass);
		aLoopNode.setName(name);
		return aLoopNode;	}
	
	private DecisionNode addnoeudDecision(Object activity1, String name,String doc) {
		// TODO Module de remplacement de méthode auto-généré
		EClass newEClass = UMLPackage.eINSTANCE.getDecisionNode();
		
		DecisionNode aDecisionNode = activity1 instanceof Activity ?
									(DecisionNode)((Activity)activity1).createNode(name, newEClass) :
									(DecisionNode) ((StructuredActivityNode)activity1).createNode(name, newEClass);
		
		aDecisionNode.setName(name);
		
		//Adding comment
		out.println("Adding documentation/Eannotation to Decision Node ..");
		
		Comment unComment = aDecisionNode.createOwnedComment();
		unComment.setBody(doc);
		unComment.getAnnotatedElements().add(aDecisionNode);
		Stereotype docStereotype = unComment.getApplicableStereotype(
		"Default::Documentation");
		
		out.println("Application du stereoType ");
		if (docStereotype == null) {
			inform("Problème Stereotype = pas de documentation appliquee");
		}
		else{
			if (!unComment.isStereotypeApplied(docStereotype))
				UMLUtil.safeApplyStereotype(unComment, docStereotype);
		}
		
		out.println("methode addnoeudDecision, nom Decision :"+aDecisionNode.getName()+" CommentBody:" + unComment.getBody());
		
		
		return aDecisionNode;	}

	private ActivityNode addNodeDebut(Object activity1, String name) {
		// TODO Module de remplacement de méthode auto-généré
		
		EClass newEClass = UMLPackage.eINSTANCE.getInitialNode();
		
		ActivityNode aActivityNode = activity1 instanceof Activity ?
									(ActivityNode)((Activity)activity1).createNode(name, newEClass) :
									(ActivityNode) ((StructuredActivityNode)activity1).createNode(name, newEClass);
				
		aActivityNode.setName(name);
		//ILayoutNode ln =UMLModeler.getUMLDiagramHelper().getLayoutNode((Node)aActivityNode);

		return aActivityNode;
	}

	private ActivityNode addNodeFinal(Object activity1, String name) {
		// TODO Module de remplacement de méthode auto-généré
		
		EClass newEClass = UMLPackage.eINSTANCE.getActivityFinalNode();
		
		ActivityNode aActivityNode = activity1 instanceof Activity ?
									(ActivityNode)((Activity)activity1).createNode(name, newEClass) :
									(ActivityNode) ((StructuredActivityNode)activity1).createNode(name, newEClass);
		
		aActivityNode.setName(name);
		return aActivityNode;
	}

	/**
	 * This method creates a new UML package and adds it to the given model.
	 * @param model The model that will contain the package.
	 * @param name Name of the package to be created.
	 * @return A reference for the created package.
	 */
	private Package addPackage(Model model, String name) {
		
		Package aPackage = (Package)model.createNestedPackage(name).getNearestPackage(); //.getNearestPackage();
		return aPackage;
	}

	/**
	 * This method creates an activity diagram and adds it to a particular activity.
	 * @param aActivity Activity which will contain the activity diagram to be created.
	 * @param name The name of the activity diagram to be created.
	 * @return A reference to the created activity diagram.
	 */
	//modification de la fonction de creation de diagramme pour s'ppliquer au activity et SA et pour SA tester si diagram existe deja
	private Diagram addActivityDiagram(Object aActivity, String name) {
		Diagram diagram = null;
		
		
		
		if(aActivity instanceof StructuredActivityNode){
			if(UMLModeler.getUMLDiagramHelper().getDiagrams((StructuredActivityNode)aActivity).size()<1) {
				diagram = UMLModeler.getUMLDiagramHelper().createDiagram((StructuredActivityNode)aActivity, UMLDiagramKind.ACTIVITY_LITERAL,
				(StructuredActivityNode)aActivity);
				diagram.setName(((StructuredActivityNode)aActivity).getName());
			}
			else 
			{
				diagram = (Diagram)UMLModeler.getUMLDiagramHelper().getDiagrams((StructuredActivityNode)aActivity).get(0);

			}
			addNodeDebut(aActivity,"Début");
			addNodeFinal(aActivity,"Fin");
			return diagram;
		}
 
		if (aActivity instanceof Activity)
			// instaceof Activity, => à créer sans tester existantce de diagramme car normalemnt on a cree une nouvelle activite vierge dans le prgramme
			{
				diagram = UMLModeler.getUMLDiagramHelper().createDiagram((Activity)aActivity, UMLDiagramKind.ACTIVITY_LITERAL,
						(Activity)aActivity);
				diagram.setName(name);
				addNodeDebut(aActivity,"Début");
				addNodeFinal(aActivity,"Fin");
				return diagram;
			}
		
		if (aActivity instanceof OpaqueAction)
			// instaceof OpaqueAction, => aucun diagram à créer, l'action sera à documenter à priori. 
			{
				diagram = (Diagram)UMLModeler.getUMLDiagramHelper().getDiagrams(((OpaqueAction)aActivity).getActivity()).get(0);

				return diagram;
				//return (Diagram)(UMLModeler.getUMLDiagramHelper().getDiagrams((((Action)aActivity)))).get(0);

			}
		else
     		throw (new RetroSpecPlugletException("Problème de selection de composant, activité, activité structurée ou action \n élement selectionné non valide."));
		
	}
	
	/**
	 * This method creates a new activity and adds it to a particular package.
	 * @param aPackage The package in which to add the activity.
	 * @param name The name of the activity to be created.
	 * @return A reference to the created activity. 
	 */
	private Object addActivity(NamedElement elementSelectionee, String name) {
		
		//FeatureToAdd : ajouter un test sur l'existence d'activité dans composant avant d'en créer.
		
		EClass newEClass = UMLPackage.eINSTANCE.getActivity();
		Object aActivity = null;
		
		//Si la selection est un composant (programme) crée l'activité
		if (elementSelectionee instanceof Component) 
			{
			 aActivity = (Activity) ((Component)elementSelectionee).createPackagedElement(name,newEClass);
			 ((Activity)aActivity).setName(name);
			 return aActivity;
			}
		
		//Si ActiviteStructuré, pas besoin de créér une activité
		if (elementSelectionee instanceof StructuredActivityNode)
			{
			aActivity =  (StructuredActivityNode)elementSelectionee;
			return aActivity;
			}
		
		//Si Activite, pas besoin d'en créér
		if (elementSelectionee instanceof Activity)
			{
			aActivity =  (Activity)elementSelectionee;
			return aActivity;
			}
		if (elementSelectionee instanceof OpaqueAction)
			{
			aActivity =  (OpaqueAction)elementSelectionee;
			return aActivity;
			}
			else
	     		throw (new RetroSpecPlugletException("Problème de selection de composant, activité, activité structurée ou action \n élement selectionné non valide."));

		
		//return aActivity;
		
	}

	/**
	 * This method creates an action and adds it to a particular activity.
	 * @param aActivity The activity that will contain the created action.
	 * @param name The name of the action to be created.
	 * @return A reference to the created action. 
	 */
	private Action addAction1(Activity aActivity, String name) {
		EClass newEClass = UMLPackage.eINSTANCE.getOpaqueAction();
		
		Action aAction = (Action) aActivity.createNode(name, newEClass);
		aAction.setName(name);
		
		return aAction;
		
	}
	private ActivityNode addAction(Object activity1, String name, String doc) {
		EClass newEClass = UMLPackage.eINSTANCE.getOpaqueAction();
		
		ActivityNode aAction = 	activity1 instanceof Activity ?
								(ActivityNode)((Activity)activity1).createNode(name, newEClass) :
								(ActivityNode) ((StructuredActivityNode)activity1).createNode(name, newEClass);
		
		aAction.setName(name);
		
		//Adding comment
		out.println("Adding documentation/Eannotation to action ..");
		
		Comment unComment = aAction.createOwnedComment();
		unComment.setBody(doc);
		unComment.getAnnotatedElements().add(aAction);
		Stereotype docStereotype = unComment.getApplicableStereotype(
		"Default::Documentation");
		
		out.println("Application du stereoType ");
		if (docStereotype == null) {
			inform("Problème Stereotype = pas de documentation appliquee");
		}
		else{
			if (!unComment.isStereotypeApplied(docStereotype))
				UMLUtil.safeApplyStereotype(unComment, docStereotype);
		}
		
		out.println("methode addAction, nom action :"+aAction.getName()+" CommentBody:" + unComment.getBody());
		
		return aAction;
	}
	
	private void logStack(Stack<Jeton> pileToLog) {
		Stack<Jeton> pileIntermidaire = (Stack<Jeton>)pileToLog.clone();
		Jeton unJeton;
		String espaces;
		
		
		while(!pileIntermidaire.empty()){
			unJeton = pileIntermidaire.pop();
			//espaces = (Integer.toString(unJeton.getNumeroLigne()).length())-3;
			out.println("+--"+"+-"+"+-------"+"+-----"+"+--+");
			out.println("|"+unJeton.getNumeroLigne()+"|"+unJeton.getTokentype().charAt(0)+"|"+unJeton.getCorps()+"|"+unJeton.isEstCree()+"|"+unJeton.getNiveau()+"|");

		}
		
		
	}
	private void logStack(Jeton jetonToLog) {
		
		out.println("+--"+"+-"+"+-------"+"+-----"+"+--+");
		out.println("|"+jetonToLog.getNumeroLigne()+"|"+jetonToLog.getTokentype().charAt(0)+"|"+jetonToLog.getCorps()+"|"+jetonToLog.isEstCree()+"|"+jetonToLog.getNiveau()+"|");
		
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
