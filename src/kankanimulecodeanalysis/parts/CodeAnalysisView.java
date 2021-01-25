package kankanimulecodeanalysis.parts;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.*;  
import org.json.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resources;
import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.event.HyperlinkEvent.EventType;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.osgi.framework.Bundle;

import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import groovyjarjarasm.asm.commons.Method;
 
import org.eclipse.swt.widgets.*;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.handlers.HandlerUtil;

public class CodeAnalysisView {
	 
	List<String> projectfiles;
	Composite _parent;
	String projectName="";
	String fileName="";
	TableViewer viewer;

	@PostConstruct
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.BORDER_SOLID | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent arg0) {
				// TODO Auto-generated method stub
				viewer.getTable().removeAll();
				_parent = parent;
				createTable(parent);
			}
			
		});
		
		 
		
		_parent = parent;
		TableViewer tv =createView(parent);
		createTable(parent);
	}

	private void findAllProjectFiles(IContainer container) throws CoreException {
        IResource[] members = container.members();        
        for (IResource member : members) {
        	//folders reading
            if (member instanceof IContainer) {
                IContainer c = (IContainer) member;
            	findAllProjectFiles(c);
            } else if (member instanceof IFile) {
            	//files reading
    		projectfiles.add(member.getName());
        	
            }
        }
    }
	
	
	@Focus
	public void setFocus() {
		 

	}

	/**
	 * This method is kept for E3 compatiblity. You can remove it if you do not
	 * mix E3 and E4 code. <br/>
	 * With E4 code you will set directly the selection in ESelectionService and
	 * you do not receive a ISelection
	 * 
	 * @param s
	 *            the selection received from JFace (E3 mode)
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) ISelection s) {
		if (s==null || s.isEmpty())
			return;

		if (s instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection) s;
			if (iss.size() == 1)
				setSelection(iss.getFirstElement());
			else
				setSelection(iss.toArray());
		}
	}

	/**
	 * This method manages the selection of your current object. In this example
	 * we listen to a single Object (even the ISelection already captured in E3
	 * mode). <br/>
	 * You should change the parameter type of your received Object to manage
	 * your specific selection
	 * 
	 * @param o
	 *            : the current object received
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) Object o) {

		// Remove the 2 following lines in pure E4 mode, keep them in mixed mode
		if (o instanceof ISelection) // Already captured
			return;

	 
	}

	/**
	 * This method manages the multiple selection of your current objects. <br/>
	 * You should change the parameter type of your array of Objects to manage
	 * your specific selection
	 * 
	 * @param o
	 *            : the current array of objects received in case of multiple selection
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) Object[] selectedObjects) {

	}
	

	public TableViewer createView(Composite parent)
	{

		
		//table creation
		Table t = viewer.getTable();
		GridLayout layout = new GridLayout();
		t.setLayoutData(layout);
	    t.setLinesVisible(true); 
	    t.setHeaderVisible(true);
	     
	    TableViewerColumn tc1 = new TableViewerColumn(viewer, SWT.LEFT);
	    TableViewerColumn tc2 = new TableViewerColumn(viewer, SWT.LEFT);
	    TableViewerColumn tc3 = new TableViewerColumn(viewer, SWT.LEFT);
	    TableViewerColumn tc4 = new TableViewerColumn(viewer, SWT.LEFT);
	    TableViewerColumn tc5 = new TableViewerColumn(viewer, SWT.LEFT);
	    
	    
	    tc1.getColumn().setText("Error");
	    tc2.getColumn().setText("File Name");
	    tc3.getColumn().setText("LN");
	    tc4.getColumn().setText("Project");
	    tc5.getColumn().setText("Priority");
	    
	    tc1.getColumn().setWidth(400);
        tc2.getColumn().setWidth(150);
        tc3.getColumn().setWidth(35);
        tc4.getColumn().setWidth (400);
        tc5.getColumn().setWidth(50);
        
        final TableColumn column = tc5.getColumn();
	    viewer.getTable().setSortColumn(column);
        
        t.setHeaderVisible(true);
	   
	    return viewer;
	    
	}
	
	public void createTable(Composite parent)
	{
		
		
		Table t = viewer.getTable();
		
	    //get workspace 
	    IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();
        System.out.println("workspace location : " + root.getLocation().toString());
        IProject[] projects = root.getProjects();
        //iterate the projects and fetch all the files
        for (IProject project : projects) {
            projectName = root.getLocation().toString() + project.getFullPath();
			System.out.println("Project Name : " + projectName);
			

			@SuppressWarnings("deprecation")
			State state = Platform.getPlatformAdmin().getState();
			BundleDescription [] bundles = state.getBundles();
			System.out.println("printing bundles");
			Bundle bundle=null;
			for(int i=0; i< bundles.length; i++)
			{
				if(bundles[i].getName().toString().contains("kankani-mule-codeanalysis"))
				{
					bundle = Platform.getBundle(bundles[i].getName().toString());
					break;
				}
			}
			
			Enumeration<URL> groovyEntries = bundle.findEntries("/src/main/resources", "*.groovy", true);
			URL currentURL = null;
			try
			{
				currentURL = groovyEntries.nextElement();
			} catch (Exception e) {
				// TODO: handle exception
				currentURL = null;
				System.out.println(e.toString());
			}

			
			if(currentURL == null)
			{
				//log error here
			}
			
			
			//run all groovy files and all methods starts with validate
			while( currentURL != null)
			{
				
				try
				{
					
					System.out.println("current URL: " + currentURL);
					//run validate* methods from groovy file
					URL jreURL = Platform.find(bundle, new Path(currentURL.getFile()));   
					runGroovy(projectName, jreURL, t);
					try { 
			    		currentURL = groovyEntries.nextElement();	                	 
					} catch (Exception e) {
						// TODO: handle exception
						currentURL = null;	 
						break;
					}
				}catch (Exception e) {
					
					break;	                	
				}
			}

        }
        System.out.println("Successful");
	}

	private void runGroovy( String projectName, URL jreURL, Table t) 
	//run all the groovy methods found in the given URL/file
	{
		
		try
		{
			Class scriptClass = new GroovyScriptEngine(".")
					.loadScriptByName(jreURL.toString());
			Object scriptInstance = scriptClass.newInstance();
			java.lang.reflect.Method[] methods = scriptClass.getMethods();
			
			for(int i=0; i<methods.length;i++)
			{
				if(methods[i].getName().contains("validate"))
				{
					
	    			Object str = scriptClass.getDeclaredMethod(methods[i].getName(), new Class[] { Object.class}).invoke(
	    					scriptInstance, new Object[]  {  projectName });
	    			if(str != null && !str.toString().isEmpty())
	    			{
	    				JSONArray errors = new JSONArray(str.toString());
	    				for (int count=0; count<errors.length();count++)
	    				{
	    					TableItem item1 = new TableItem(t, SWT.NONE);
        					item1.setText(new String[] { errors.getJSONObject(count).get("errorDetails").toString(), errors.getJSONObject(count).get("fileName").toString(), errors.getJSONObject(count).get("lineNumber").toString(), projectName, errors.getJSONObject(count).get("priority").toString()});
	    				}
	        		}
				}
				
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}

	}
	
}
