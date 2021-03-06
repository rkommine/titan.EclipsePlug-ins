/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.editors;

import java.util.Iterator;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * @author Kristof Szabados
 * */
public final class TextHover implements ITextHover {
	private ISourceViewer sourceViewer;

	public TextHover(final ISourceViewer sourceViewer) {
		this.sourceViewer = sourceViewer;
	}

	@Override
	public String getHoverInfo(final ITextViewer textViewer, final IRegion hoverRegion) {
		if (hoverRegion == null) {
			return null;
		}

		IAnnotationModel annotationModel = sourceViewer.getAnnotationModel();
		if (annotationModel != null) {
			Iterator<?> iterator = annotationModel.getAnnotationIterator();
			while (iterator.hasNext()) {
				Object o = iterator.next();
				if (o instanceof MarkerAnnotation) {
					MarkerAnnotation actualMarker = (MarkerAnnotation) o;
					Position markerPosition = annotationModel.getPosition(actualMarker);
					if (markerPosition.getOffset() <= hoverRegion.getOffset()
							&& markerPosition.getOffset() + markerPosition.getLength() >= hoverRegion.getOffset()) {
						return actualMarker.getText();
					}
				}
			}
		}

		return null;
	}

	public Object getHoverInfo2(final ITextViewer textViewer, final IRegion hoverRegion) {
		// Start with the string returned by the older getHoverInfo()
		final String selection = getHoverInfo(textViewer, hoverRegion);
		return selection;
	}

	@Override
	public IRegion getHoverRegion(final ITextViewer textViewer, final int offset) {
		return new Region(offset, 0);
	}

}
