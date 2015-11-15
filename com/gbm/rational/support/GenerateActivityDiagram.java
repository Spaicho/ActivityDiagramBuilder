package com.gbm.rational.support;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLPackage;

import com.ibm.xtools.modeler.ui.UMLModeler;
import com.ibm.xtools.pluglets.Pluglet;
import com.ibm.xtools.umlnotation.UMLDiagramKind;

/**
 * This RSA pluglet sample demonstrates how to generate activity diagrams from business process
 * steps defined in an external text file. The sample pluglet reads the process steps from the file
 * "steps.txt" on the root folder of the project, creates an activity diagram, and for each step 
 * creates an action on the activity diagram. To run the sample, follow these steps:
 * <br>
 * <br>1. Switch to the Modeling perspective in RSA.
 * <br>2. Import the project "ModelGenProj" into RSA. Select File -> Import. In the Import dialog 
 * box, select Other -> Project Interchange and then click Next. 
 * <br>3. In the Import Project Interchange Contents dialog box, point to the file ModelGenProj.zip, 
 * check the ModelGenProj entry, and click Finish.
 * <br>4. Open the file GenerateActivityDiagram.java in the com.gbm.rational.support package. Make sure
 *  the path to the process steps file "steps.txt" defined in the pluglet code points to the right 
 *  location. You may either modify the file location in the code or create a folder structure on 
 *  your file system complying with the path referenced in the code.
 * <br>5. Open the model named Test, by double clicking it in Project Explorer under 
 * ModelGenProj -> Models -> Test. Make sure this is the only model that is open in RSA, 
 * as the pluglet code operates on any open model having the string "Test" as part of its name.
 * <br>6. In Project Explorer, right-click the file GenerateActivityDiagram.java, select Run As -> Pluglet.
 * <br>7. After the code runs, examine your UML Model. You'll find a new package created, with the UML 
 * activity diagram created representing the process steps defined in the file "steps.txt". This file 
 * contains a list of the process steps to be modeled, each in a separate line. To view the UML diagram 
 * created, from Project Explorer navigate to ModelGenProj -> Diagrams -> Test and 
 * double-click the UML diagram package1: Activity1: ActivityDiagram1. 
 * 
 * @author Ahmed Makady, GBM
 * @version 1.1. 
 */

public class GenerateActivityDiagram extends Pluglet {
	
	/**
	 * This is the pluglet entry point, which in turn calls createProcessActivityDiagram(), that contains
	 * the main logic for activity diagram creation.
	 */
	public void plugletmain(String[] args) {
		out.println("Pluglet \"GenerateActivityDiagram\"."); 
		
		String undoLabel = "Modify Operation";
		TransactionalEditingDomain editDomain = UMLModeler.getEditingDomain();
		out.println("editDomain.getID() :  " + editDomain.getID());
		editDomain.getCommandStack().execute(new RecordingCommand(editDomain, undoLabel) {


			protected void doExecute() {

				Collection models = UMLModeler.getOpenedModels();
				for (Iterator iter = models.iterator(); iter.hasNext();) {

					Model model = (Model) iter.next();
					out.println(model.getName());

					if (model == null )
						out.println("Could not open model");
					else{
						
						//Start operating on models having the name "*Test*"
						if (model.getName().indexOf("Test") != -1){
							createProcessActivityDiagram(model);
						}
					}
				}

			}
		});

	}
	
	
	/**
	 * This method provides the main logic sequence for creating a new package, 
	 * a new activity diagram and the action nodes representing the process steps
	 *  in the generated activity diagram. It's called from the main pluglet entry 
	 * point, method "plugletmain()".
	 *  
	 * @param model This is the model that will contain the activity diagram representing the process.
	 */
	private void createProcessActivityDiagram(Model model) {

		//Create a package to hold the activity diagram
		Package aPackage = addPackage(model, "package1");
		
		//Add an activity "Activity1" to the package
		Activity activity1 = addActivity(aPackage, "Activity1");
		
		//Add an activity diagram to the activity
		Diagram diagram = addActivityDiagram(activity1, "ActivityDiagram1");
				
		//Define a variable to read the process steps file..
		BufferedReader in = null;
		
		//Assign the variable to an actual file on the file system
		try {
			//Read process steps from a file
			out.println("Reading File.. ");
			in = new BufferedReader(new FileReader("C:\\steps.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(out);
		}
		
		//Loop on the steps in the process definition file, creating an action
		//for each in the activity diagram
		out.println("Adding Process Steps..");
		
		String actionName = null;
		
		try {
			while((actionName = in.readLine())!=null){
				
				//Add an action to the activity
				addAction(activity1, actionName);
			}
		} catch (IOException e1) {
			e1.printStackTrace(out);
		}
		
		//Adjust diagram layout
		UMLModeler.getUMLDiagramHelper().layoutNodes(diagram.getChildren(), "DEFAULT");
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
	private Diagram addActivityDiagram(Activity aActivity, String name) {
		Diagram diagram;
		diagram = UMLModeler.getUMLDiagramHelper().createDiagram(aActivity, UMLDiagramKind.ACTIVITY_LITERAL,
				aActivity);
		diagram.setName(name);
		
		return diagram;
	}
	
	/**
	 * This method creates a new activity and adds it to a particular package.
	 * @param aPackage The package in which to add the activity.
	 * @param name The name of the activity to be created.
	 * @return A reference to the created activity. 
	 */
	private Activity addActivity(Package aPackage, String name) {
		EClass newEClass = UMLPackage.eINSTANCE.getActivity();
		Activity aActivity = (Activity) aPackage.createPackagedElement(name,newEClass);
		aActivity.setName(name);
		
		return aActivity;
		
	}

	/**
	 * This method creates an action and adds it to a particular activity.
	 * @param aActivity The activity that will contain the created action.
	 * @param name The name of the action to be created.
	 * @return A reference to the created action. 
	 */
	private Action addAction(Activity aActivity, String name) {
		EClass newEClass = UMLPackage.eINSTANCE.getOpaqueAction();
		Action aAction = (Action) aActivity.createNode(name, newEClass);
		aAction.setName(name);
		
		return aAction;
		
	}
}
