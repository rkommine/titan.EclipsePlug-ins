/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors.configeditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Jumps to the end of the actual line, or to the end of the text in that line.
 * 
 * @author Kristof Szabados
 */
public final class LastCharAction extends AbstractHandler implements IEditorActionDelegate {
	private IEditorPart targetEditor = null;

	/**
	 * Perform the jump.
	 * 
	 * @param action
	 *                the action proxy that handles the presentation portion
	 *                of the action. Not used.
	 * */
	@Override
	public void run(final IAction action) {
		if (targetEditor == null || !(targetEditor instanceof FormEditor)) {
			return;
		}

		FormEditor tempEditor = (FormEditor) targetEditor;

		IEditorPart editorPart = tempEditor.getActiveEditor();
		if (!(editorPart instanceof ITextEditor)) {
			return;
		}

		ITextEditor realEditor = (ITextEditor) editorPart;

		realEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(null);

		IDocument doc = realEditor.getDocumentProvider().getDocument(realEditor.getEditorInput());
		int offset = ((ITextSelection) realEditor.getSelectionProvider().getSelection()).getOffset();

		try {
			IRegion lineRegion = doc.getLineInformationOfOffset(offset);
			int lastVisibleCharLocation = lastVisibleCharLocation(doc, lineRegion);
			if (lastVisibleCharLocation != -1 && lastVisibleCharLocation + 1 != offset) {
				// if the line contains characters and we are
				// not right behind the last one, jump there
				realEditor.selectAndReveal(lastVisibleCharLocation + 1, 0);
			} else {
				// jump to the end of the line
				realEditor.selectAndReveal(lineRegion.getOffset() + lineRegion.getLength(), 0);
			}
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
	}

	/**
	 * Sets the active editor.
	 * 
	 * @param action
	 *                the action (not used)
	 * @param targetEditor
	 *                the editor to be set.
	 * */
	@Override
	public void setActiveEditor(final IAction action, final IEditorPart targetEditor) {
		this.targetEditor = targetEditor;
	}

	/**
	 * Selection change notification.
	 * <p>
	 * Not used.
	 * 
	 * @param action
	 *                the action proxy that handles presentation portion of
	 *                the action
	 * @param selection
	 *                the current selection, or <code>null</code> if there
	 *                is no selection.
	 * */
	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
	}

	/**
	 * Locates and returns the last visible character in the given region.
	 * 
	 * @param doc
	 *                the document to search
	 * @param lineRegion
	 *                the region to search inside the document
	 * @return the first visible character, or -1 if none was found.
	 * @exception BadLocationException
	 *                    if the offset is invalid in this document
	 * */
	protected int lastVisibleCharLocation(final IDocument doc, final IRegion lineRegion) throws BadLocationException {
		int endOffset = lineRegion.getOffset();
		int startOffset = Math.min(endOffset + lineRegion.getLength(), doc.getLength() - 1);

		while (startOffset >= endOffset) {
			char ch = doc.getChar(startOffset);
			if (!Character.isWhitespace(ch)) {
				return startOffset;
			}
			startOffset--;
		}

		return -1;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		targetEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

		if (targetEditor == null || !(targetEditor instanceof FormEditor)) {
			return null;
		}

		FormEditor tempEditor = (FormEditor) targetEditor;

		IEditorPart editorPart = tempEditor.getActiveEditor();
		if (!(editorPart instanceof ITextEditor)) {
			return null;
		}

		ITextEditor realEditor = (ITextEditor) editorPart;

		realEditor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(null);

		IDocument doc = realEditor.getDocumentProvider().getDocument(realEditor.getEditorInput());
		int offset = ((ITextSelection) realEditor.getSelectionProvider().getSelection()).getOffset();

		try {
			IRegion lineRegion = doc.getLineInformationOfOffset(offset);
			int lastVisibleCharLocation = lastVisibleCharLocation(doc, lineRegion);
			if (lastVisibleCharLocation != -1 && lastVisibleCharLocation + 1 != offset) {
				// if the line contains characters and we are
				// not right behind the last one, jump there
				realEditor.selectAndReveal(lastVisibleCharLocation + 1, 0);
			} else {
				// jump to the end of the line
				realEditor.selectAndReveal(lineRegion.getOffset() + lineRegion.getLength(), 0);
			}
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace(e);
		}
		return null;
	}
}
