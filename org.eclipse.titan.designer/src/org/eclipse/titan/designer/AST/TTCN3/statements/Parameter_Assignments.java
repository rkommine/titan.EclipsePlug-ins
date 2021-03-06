/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTNode;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.TTCN3.IIncrementallyUpdateable;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents the parameter assignments of a parameter redirection.
 * 
 * @author Kristof Szabados
 * */
public final class Parameter_Assignments extends ASTNode implements IIncrementallyUpdateable {
	private static final String FULLNAMEPART = ".parameterassignment_";

	private final List<Parameter_Assignment> parameter_assignments;

	public Parameter_Assignments() {
		parameter_assignments = new ArrayList<Parameter_Assignment>();
	}

	@Override
	public StringBuilder getFullName(final INamedNode child) {
		StringBuilder builder = super.getFullName(child);

		for (int i = 0; i < parameter_assignments.size(); i++) {
			if (parameter_assignments.get(i) == child) {
				return builder.append(FULLNAMEPART).append(Integer.toString(i + 1));
			}
		}

		return builder;
	}

	@Override
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);

		for (int i = 0; i < parameter_assignments.size(); i++) {
			parameter_assignments.get(i).setMyScope(scope);
		}
	}

	public void add(final Parameter_Assignment parameter_assignment) {
		parameter_assignments.add(parameter_assignment);
		parameter_assignment.setFullNameParent(this);
	}

	public int getNofParameterAssignments() {
		return parameter_assignments.size();
	}

	public Parameter_Assignment getParameterAssignmentByIndex(final int index) {
		return parameter_assignments.get(index);
	}

	@Override
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		for (int i = 0, size = parameter_assignments.size(); i < size; i++) {
			Parameter_Assignment assignment = parameter_assignments.get(i);

			assignment.updateSyntax(reparser, isDamaged);
			reparser.updateLocation(assignment.getLocation());
		}
	}

	@Override
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (parameter_assignments == null) {
			return;
		}

		for (Parameter_Assignment pa : parameter_assignments) {
			pa.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	protected boolean memberAccept(ASTVisitor v) {
		if (parameter_assignments != null) {
			for (Parameter_Assignment pa : parameter_assignments) {
				if (!pa.accept(v)) {
					return false;
				}
			}
		}
		return true;
	}
}
