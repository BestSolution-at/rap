///*******************************************************************************
// * Copyright (c) 2000, 2006 IBM Corporation and others.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *     IBM Corporation - initial API and implementation
// *******************************************************************************/
//package org.eclipse.ui.tests.multipageeditor;
//
//import java.io.ByteArrayInputStream;
//import java.util.Collection;
//
//import org.eclipse.core.resources.IFile;
//import org.eclipse.core.resources.IProject;
//import org.eclipse.core.resources.IWorkspace;
//import org.eclipse.core.resources.ResourcesPlugin;
//import org.eclipse.core.runtime.CoreException;
//import org.eclipse.jface.text.TextSelection;
//import org.eclipse.jface.viewers.IPostSelectionProvider;
//import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.jface.viewers.ISelectionChangedListener;
//import org.eclipse.jface.viewers.ISelectionProvider;
//import org.eclipse.jface.viewers.SelectionChangedEvent;
//import org.eclipse.swt.widgets.Control;
//import org.eclipse.ui.IEditorPart;
//import org.eclipse.ui.IWorkbenchPage;
//import org.eclipse.ui.PartInitException;
//import org.eclipse.ui.contexts.IContextService;
//import org.eclipse.ui.ide.IDE;
//import org.eclipse.ui.tests.harness.util.UITestCase;
//
///**
// * <p>
// * Test that the MultiPageEditorPart is acting on events and changes. These
// * tests are for making sure that selection events and page change events are
// * handled.
// * </p>
// * <p>
// * It also checks for changing Contexts.
// * </p>
// * 
// * @since 3.2
// */
//public class MultiVariablePageTest extends UITestCase {
//
//	private static final String FILE_CONTENTS = "#section01\nsection 1\n#section02\nsection 2\nwith info\n#section03\nLast page\n";
//
//	private static final String MTEST01_FILE = "mtest01.multivar";
//
//	private static final String MULTI_VARIABLE_PROJ = "MultiVariableTest";
//
//	private int fPostCalled;
//
//	public MultiVariablePageTest(String testName) {
//		super(testName);
//	}
//
//	/**
//	 * Make sure that setting the active page programmatically calls
//	 * pageChanged(int) on the way. This method is overridden in a lot of
//	 * editors to provide their functionality on page changes.
//	 * 
//	 * @throws Throwable
//	 */
//	public void testSetActivePage() throws Throwable {
//		// Open a new test window.
//		// Create and open a blurb file.
//		IEditorPart part = openMultivarFile();
//
//		MultiVariablePageEditor editor = (MultiVariablePageEditor) part;
//
//		editor.setPage(1);
//		ISelection selection = editor.getEditorSite().getSelectionProvider()
//				.getSelection();
//		TextSelection text = (TextSelection) selection;
//		// when we change to the second page, the selection should be
//		// updated.
//		assertEquals("#section02", text.getText());
//
//		editor.setPage(0);
//		selection = editor.getEditorSite().getSelectionProvider()
//				.getSelection();
//		text = (TextSelection) selection;
//		// when we change back to the first page, the selection should be
//		// updated.
//		assertEquals("#section01", text.getText());
//	}
//
//	/**
//	 * Make sure that removing a page that is a Control (instead of an editor)
//	 * disposes of the Control immediately.
//	 * 
//	 * @throws Throwable
//	 */
//	public void testRemovePage() throws Throwable {
//		// Open a new test window.
//		// Create and open a blurb file.
//		IEditorPart part = openMultivarFile();
//
//		MultiVariablePageEditor editor = (MultiVariablePageEditor) part;
//		editor.addLastPage();
//		Control c = editor.getLastPage();
//		assertFalse(c.isDisposed());
//		editor.removeLastPage();
//		assertTrue(c.isDisposed());
//
//		c = editor.getTestControl(2);
//		assertFalse(c.isDisposed());
//		editor.removeLastPage();
//		assertTrue(c.isDisposed());
//		editor.setPage(0);
//		editor.getSite().getPage().activate(editor);
//	}
//
//	/**
//	 * Now the MPEP site's selection provider should by default support post
//	 * selection listeners. Since the MVPE is based on Text editors, we should
//	 * be getting the post selection events when we change pages.
//	 * 
//	 * @throws Throwable
//	 *             on error cases
//	 */
//	public void testPostSelection() throws Throwable {
//		// Open a new test window.
//		// Create and open a blurb file.
//		IEditorPart part = openMultivarFile();
//
//		MultiVariablePageEditor editor = (MultiVariablePageEditor) part;
//		ISelectionProvider sp = editor.getEditorSite().getSelectionProvider();
//		assertTrue(sp instanceof IPostSelectionProvider);
//
//		IPostSelectionProvider postProvider = (IPostSelectionProvider) sp;
//
//		fPostCalled = 0;
//		ISelectionChangedListener listener = new ISelectionChangedListener() {
//			public void selectionChanged(SelectionChangedEvent event) {
//				++fPostCalled;
//			}
//		};
//
//		try {
//			postProvider.addPostSelectionChangedListener(listener);
//			editor.setPage(1);
//			assertEquals(1, fPostCalled);
//			editor.setPage(0);
//			assertEquals(2, fPostCalled);
//		} finally {
//			postProvider.removePostSelectionChangedListener(listener);
//		}
//	}
//
//	private IEditorPart openMultivarFile() throws CoreException,
//			PartInitException {
//		IWorkbenchPage page = openTestWindow().getActivePage();
//		IWorkspace workspace = ResourcesPlugin.getWorkspace();
//		IProject testProject = workspace.getRoot().getProject(
//				MULTI_VARIABLE_PROJ);
//		if (!testProject.exists()) {
//			testProject.create(null);
//		}
//		testProject.open(null);
//		IFile multiFile = testProject.getFile(MTEST01_FILE);
//		if (!multiFile.exists()) {
//			multiFile.create(
//					new ByteArrayInputStream(FILE_CONTENTS.getBytes()), true,
//					null);
//		}
//
//		// I can't be bothered to use the ID, but this editor has an
//		// extention registered against it.
//		IEditorPart part = IDE.openEditor(page, multiFile);
//		assertTrue("Should have opened our multi variable page editor",
//				part instanceof MultiVariablePageEditor);
//		return part;
//	}
//
//	/**
//	 * Make sure that contexts are activated-deactivated by pages changes and
//	 * other editors.
//	 * 
//	 * @throws Throwable
//	 *             on error
//	 */
//	public void testContextActivation() throws Throwable {
//		IContextService globalService = (IContextService) getWorkbench()
//				.getService(IContextService.class);
//
//		// Open a new test window.
//		// Create and open a blurb file.
//		IEditorPart part = openMultivarFile();
//
//		MultiVariablePageEditor editor = (MultiVariablePageEditor) part;
//		checkActiveContext(globalService, ContextTextEditor.CONTEXT_ID, false);
//		checkActiveContext(globalService, ContextTextEditor.TEXT_CONTEXT_ID,
//				true);
//
//		editor.setPage(1);
//		checkActiveContext(globalService, ContextTextEditor.CONTEXT_ID, false);
//		checkActiveContext(globalService, ContextTextEditor.TEXT_CONTEXT_ID,
//				true);
//
//		editor.setPage(2);
//		checkActiveContext(globalService, ContextTextEditor.CONTEXT_ID, true);
//		checkActiveContext(globalService, ContextTextEditor.TEXT_CONTEXT_ID,
//				true);
//
//		editor.setPage(1);
//		checkActiveContext(globalService, ContextTextEditor.CONTEXT_ID, false);
//		checkActiveContext(globalService, ContextTextEditor.TEXT_CONTEXT_ID,
//				true);
//
//		editor.setPage(2);
//		checkActiveContext(globalService, ContextTextEditor.CONTEXT_ID, true);
//		editor.removeLastPage();
//		checkActiveContext(globalService, ContextTextEditor.CONTEXT_ID, false);
//		checkActiveContext(globalService, ContextTextEditor.TEXT_CONTEXT_ID,
//				true);
//	}
//
//	/**
//	 * Assert if the contextId is active in the contextService.
//	 * 
//	 * @param contextService
//	 * @param contextId
//	 * @param isActive
//	 */
//	private void checkActiveContext(IContextService contextService,
//			String contextId, boolean isActive) {
//		Collection activeContexts = contextService.getActiveContextIds();
//		assertEquals(contextId, isActive, activeContexts.contains(contextId));
//	}
//}
