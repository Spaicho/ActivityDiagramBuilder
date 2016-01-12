package ma.cgi.gtc.spaich;

import java.util.Iterator;
import java.util.List;

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

import ma.cgi.gtc.spaich.exception.RetroSpecPlugletException;

public final class UmlFactory {
	/** 
	 *  Class exposing static methods for UML Objects in a given activity.
	 */
		
		// Modifier le premier arguments de Activity à Object, pour permettre le passage de StructuredActivityNode aussi
		public static StructuredActivityNode addStructuralActivity(Object activity1,String name, String doc) {

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
			Stereotype docStereotype = unComment.getApplicableStereotype("Default::Documentation");
			
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

		public static void customizeStructuredActivityShape(StructuredActivityNode activiteStructuree,Diagram diagram) {
		// TODO Module de remplacement de méthode auto-généré
			
			IUMLDiagramHelper dgmHelper = UMLModeler.getUMLDiagramHelper();
			List elements = diagram.getChildren();
			informAllView(diagram);
				
	}

		public static void informAllView(View diagram) {
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

		public static ControlFlow addControlFlow(NamedElement activity1, String name, NamedElement actionSource, NamedElement actionTarget) {
			// TODO Module de remplacement de méthode auto-généré
			ControlFlow cf = (ControlFlow) (activity1 instanceof Activity? 
											              ((Activity)activity1).createEdge(name,UMLPackage.eINSTANCE.getControlFlow()):
											((StructuredActivityNode)activity1).createEdge(name,UMLPackage.eINSTANCE.getControlFlow()));
			cf.setSource(actionSource instanceof StructuredActivityNode?(StructuredActivityNode)actionSource:(ActivityNode)actionSource);
			cf.setTarget(actionTarget instanceof StructuredActivityNode?(StructuredActivityNode)actionTarget:(ActivityNode)actionTarget);
			
			//((DecisionNode)noeudActivite).getOwnedComments().get(0).setBody("FGX");
			return cf;
		}

		public static LoopNode addnoeudloop(Activity activity1, String name) {
			// TODO Module de remplacement de méthode auto-généré
			EClass newEClass = UMLPackage.eINSTANCE.getLoopNode();
			LoopNode aLoopNode = (LoopNode) activity1.createNode(name, newEClass);
			aLoopNode.setName(name);
			return aLoopNode;	}
		
		public static DecisionNode addnoeudDecision(Object activity1, String name,String doc) {
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
			Stereotype docStereotype = unComment.getApplicableStereotype("Default::Documentation");
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

		public static ActivityNode addNodeDebut(Object activity1, String name) {
			// TODO Module de remplacement de méthode auto-généré
			
			EClass newEClass = UMLPackage.eINSTANCE.getInitialNode();
			
			ActivityNode aActivityNode = activity1 instanceof Activity ?
										(ActivityNode)((Activity)activity1).createNode(name, newEClass) :
										(ActivityNode) ((StructuredActivityNode)activity1).createNode(name, newEClass);
					
			aActivityNode.setName(name);
			//ILayoutNode ln =UMLModeler.getUMLDiagramHelper().getLayoutNode((Node)aActivityNode);

			return aActivityNode;
		}

		public static ActivityNode addNodeFinal(Object activity1, String name) {
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
		public static Package addPackage(Model model, String name) {
			
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
		public static Diagram addActivityDiagram(Object aActivity, String name) {
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
		public static Object addActivity(NamedElement elementSelectionee, String name) {
			
			// TODO : ajouter un test sur l'existence d'activité dans composant avant d'en créer.
			
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

		}

		/**
		 * This method creates an action and adds it to a particular activity.
		 * @param aActivity The activity that will contain the created action.
		 * @param name The name of the action to be created.
		 * @return A reference to the created action. 
		 */
		public static Action addAction1(Activity aActivity, String name) {
			EClass newEClass = UMLPackage.eINSTANCE.getOpaqueAction();
			
			Action aAction = (Action) aActivity.createNode(name, newEClass);
			aAction.setName(name);
			
			return aAction;
			
		}
		public static ActivityNode addAction(Object activity1, String name, String doc) {
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
			Stereotype docStereotype = unComment.getApplicableStereotype("Default::Documentation");
			
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
}
