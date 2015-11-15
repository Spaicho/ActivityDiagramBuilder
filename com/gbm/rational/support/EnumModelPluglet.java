/*
 * Licensed Materials - Use restricted, please refer to the "Samples Gallery" terms
 * and conditions in the IBM International Program License Agreement.
 *
 * © Copyright IBM Corporation 2003, 2004. All Rights Reserved. 
 */
package com.gbm.rational.support;

import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramEditorInput;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;

import com.ibm.xtools.modeler.ui.UMLModeler;
import com.ibm.xtools.pluglets.Pluglet;

public class EnumModelPluglet extends Pluglet {

	/**
	 * Walk the selected objects and log them to the console
	 */
	public void plugletmain(String[] args) {


			try {
				UMLModeler.getEditingDomain().runExclusive(new Runnable() {

					public void run() {


						IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
								.getActiveEditor();
						IEditorInput editorInput = editorPart.getEditorInput();

						if (editorInput instanceof IDiagramEditorInput) {

							Diagram diagram = ((IDiagramEditorInput) editorInput).getDiagram();

							List selectedElements = UMLModeler.getUMLUIHelper().getSelectedElements(diagram);

							for (Iterator iter = selectedElements.iterator(); iter.hasNext();) {

								EObject eObject = (EObject) iter.next();
								String eClassName = eObject.eClass().getName();
								out.print(eClassName + " : ");

								if (eObject instanceof Diagram) {
									out.println(((Diagram) eObject).getName());

								} else if (eObject instanceof View) {
									View view = (View) eObject;
									String viewType = view.getType();
									if (viewType.trim().length() > 0) {
										out.print("(" + view.getType() + ")");
									}

									EObject element = view.getElement();
									if (null != element) {
										out.print(" of " + element);
									}
									out.println();

								} else if (eObject instanceof Element) {
									if (eObject instanceof NamedElement) {
										out.println(((NamedElement) eObject).getName());
									} else {
										out.println(eObject);
									}
								}
							}
						}
					}
				});
			} catch (InterruptedException e) {
				out.println("The operation was interrupted");
			}
		}

		
	}

