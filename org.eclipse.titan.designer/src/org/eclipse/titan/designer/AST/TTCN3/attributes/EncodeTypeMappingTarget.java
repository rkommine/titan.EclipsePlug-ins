/******************************************************************************
 * Copyright (c) 2000-2014 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.attributes;

import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.IType.Encoding_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * Represents an encoding type mapping target.
 * 
 * @author Kristof Szabados
 * */
public final class EncodeTypeMappingTarget extends TypeMappingTarget {
	private static final String FULLNAMEPART1 = ".<target_type>";
	private static final String FULLNAMEPART2 = ".<errorbehavior>";

	private final Type target_type;
	private final EncodeAttribute encodeAttribute;
	private final ErrorBehaviorAttribute errorBehaviorAttribute;

	public EncodeTypeMappingTarget(final Type target_type, final ExtensionAttribute encodeAttribute,
			final ErrorBehaviorAttribute errorBehaviorAttribute) {
		this.target_type = target_type;
		if (encodeAttribute instanceof EncodeAttribute) {
			this.encodeAttribute = (EncodeAttribute) encodeAttribute;
		} else {
			this.encodeAttribute = null;
		}
		this.errorBehaviorAttribute = errorBehaviorAttribute;
	}

	@Override
	public TypeMapping_type getTypeMappingType() {
		return TypeMapping_type.ENCODE;
	}

	@Override
	public String getMappingName() {
		return "encode";
	}

	@Override
	public Type getTargetType() {
		return target_type;
	}

	public Encoding_type getCodingType() {
		if (encodeAttribute != null) {
			return encodeAttribute.getEncodingType();
		}

		return Encoding_type.UNDEFINED;
	}

	public boolean hasCodingOptions() {
		if (encodeAttribute != null) {
			return encodeAttribute.getOptions() != null;
		}

		return false;
	}

	public String getCodingOptions() {
		if (encodeAttribute != null) {
			return encodeAttribute.getOptions();
		}

		return "UNDEFINED";
	}

	public ErrorBehaviorList getErrrorBehaviorList() {
		return errorBehaviorAttribute.getErrrorBehaviorList();
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		if (target_type == child) {
			return builder.append(FULLNAMEPART1);
		} else if (errorBehaviorAttribute == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (target_type != null) {
			target_type.setMyScope(scope);
		}
	}

	@Override
	public void check(final CompilationTimeStamp timestamp, final Type source) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		lastTimeChecked = timestamp;

		if (target_type != null) {
			target_type.check(timestamp);
		}
		// FIXME implement once has_encoding is available.
		if (errorBehaviorAttribute != null) {
			errorBehaviorAttribute.getErrrorBehaviorList().check(timestamp);
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (target_type != null) {
			target_type.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (target_type != null && !target_type.accept(v)) {
			return false;
		}
		if (encodeAttribute != null && !encodeAttribute.accept(v)) {
			return false;
		}
		if (errorBehaviorAttribute != null && !errorBehaviorAttribute.accept(v)) {
			return false;
		}
		return true;
	}
}