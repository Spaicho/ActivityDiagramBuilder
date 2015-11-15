/*
 * Licensed Materials - Use restricted, please refer to the "Samples Gallery" terms
 * and conditions in the IBM International Program License Agreement.
 *
 * © Copyright IBM Corporation 2003, 2004. All Rights Reserved. 
 */
package com.gbm.rational.support;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Profile;

import com.ibm.xtools.modeler.ui.UMLModeler;
import com.ibm.xtools.pluglets.Pluglet;
import com.ibm.xtools.uml.type.UMLElementFactory;
import com.ibm.xtools.uml.ui.IUMLUIHelper;

public class PlugletTestUmlModeler extends Pluglet {

	/**
	 * Walk the selected objects and log them to the console
	 */
	public void plugletmain(String[] args) {

        /* Perform remaining work within a Runnable */
        try {
            UMLModeler.getEditingDomain().runExclusive(new Runnable() {

                public void run() {
                    // Get selection
                    
                	//final List elements = UMLModeler.getUMLUIHelper().getSelectedElements();
                	final List elements = UMLModeler.getUMLUIHelper().getSelectedElements("org.eclipse.ui.navigator.ProjectExplorer");
                	Model monModel = null;
                	try {
                	//monModel = UMLModeler.openModel(URI.createURI("platform:/resource/RelevesSituationParticuliers/02_analyse_conception/05_Fragment/BORPED70.efx"));
                	monModel = UMLModeler.openModel(URI.createURI("platform:/resource/RelevesSituationParticuliers/Analyse_Conception.emx"));
                		
                		//monModel = (Model)UMLModeler.openModelResource(URI.createURI("/resource/RelevesSituationParticuliers/02_analyse_conception/05_Fragment/BORPED70.efx")
                	} catch (IOException e) {
						e.printStackTrace();
                	} finally {
						if (monModel != null) {
							UMLModeler.closeModel(monModel);
						}
                	}
                	//UMLElementFactory.
                	
                	//final Collection elements = UMLModeler.getOpenedModels();
                	//final Collection elements = UMLModeler.getEditingDomain().
					out.println("monModel.getName() : " + monModel.getName());
					
					out.println("monModel.getLabel() : " + monModel.getLabel());
					out.println(monModel.getAllAppliedProfiles().size());
					out.println(monModel.getAppliedStereotypes().size());
					out.println(monModel.getQualifiedName().toString());
					out.println(monModel.getImportedElements().size());
					out.println(monModel.toString());
					
					for (Iterator iter = monModel.getImportedElements().iterator();iter.hasNext();)
					{
						out.println(iter.next().toString());
					}
					
					ResourceSet resourceSet = UMLModeler.getEditingDomain().getResourceSet();

			    	for (Iterator iter = resourceSet.getResources().iterator(); iter.hasNext();) {
			    		Resource resource = (Resource) iter.next();
			    		out.println(resource.getURI());

			    		for (Iterator iterRoots = resource.getContents().iterator(); iterRoots.hasNext();) {
			    			EObject eObject = (EObject) iterRoots.next();
			    			out.print("\t");
			    			out.println(eObject);
			    		}
			    	}
					
					
                	out.println("elements.size() : " + elements.size());
                    if (elements.size() == 0) {
						out.println("Cannot perform enumeration on current selection.\nPlease select a UML Element from the Project Explorer or\nselect a Notation element from a diagram."); //$NON-NLS-1$
                    }
                    
                    // Log each elements
                    for (Iterator iter = elements.iterator(); iter.hasNext();) {
                        //out.println(((Model) iter.next()).getName());
                    	//out.println(((Diagram) iter.next()).toString());
                    	out.println(((Component) iter.next()).toString());
                    	//logObject(iter.next(), ""); //$NON-NLS-1$
                    }
                }

            });
        } catch (InterruptedException e) {
			out.println("The operation was interrupted"); //$NON-NLS-1$
        }
        
	}
	
	

	/**
	 * Logs the specified object's toString() and its content
	 * @param object The object to log 
	 * @param indent The string to prefix to the object's toString()
	 */
	private void logObject(Object object, String indent) {

		// Log object
		if (null != object) {
			out.println(indent + object.toString());
		} else {
			out.println(indent + "<null>"); //$NON-NLS-1$
		}
		
		// Log contents
		//if (object instanceof EObject) {
		//	Iterator contents = ((EObject)object).eContents().iterator();
		//	while (contents.hasNext()) {
		//		logObject(contents.next(), indent + "\t"); //$NON-NLS-1$
		//	}
		//}
	}

}
