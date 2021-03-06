/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.error;

import java.io.PrintStream;

/**
 * This class implements a console error handling. It needs streams where
 * messages can be written in order
 * 
 * @author Gabor Jenei
 */
public class ConsoleErrorHandler implements ErrorHandler {
	protected PrintStream errorStream;
	protected PrintStream warningStream;
	protected PrintStream informationStream;
	protected StringBuilder collectedMessage;

	/**
	 * This constructor sets {@link System#err} for error messages and
	 * {@link System#out} for all other messages
	 */
	public ConsoleErrorHandler() {
		this(System.err, System.out, System.out);
	}

	/**
	 * Constructor
	 * 
	 * @param errorStream
	 *            : The stream of error messages (including exception traces)
	 * @param warningStream
	 *            : The stream of warning messages
	 * @param informationStream
	 *            : The stream of information messages
	 */
	public ConsoleErrorHandler(PrintStream errorStream, PrintStream warningStream, PrintStream informationStream) {
		this.errorStream = errorStream;
		this.warningStream = warningStream;
		this.informationStream = informationStream;
		collectedMessage = new StringBuilder();
	}

	@Override
	public void reportException(String context, Exception exception) {
		errorStream.println("An exception occured, the stack trace is:\n");
		exception.printStackTrace(errorStream);
	}

	@Override
	public void reportErrorMessage(String text) {
		errorStream.println(text);
	}

	@Override
	public void reportWarning(String text) {
		warningStream.println(text);
	}

	@Override
	public void reportInformation(String text) {
		informationStream.println(text);
	}

	@Override
	public void logError(String message) {
		collectedMessage.append(message);
	}

	@Override
	public void logException(Exception exception) {
		collectedMessage.append("Exception:\n" + exception.getMessage() + "\nStack trace:\n");
		for (StackTraceElement elem : exception.getStackTrace()) {
			collectedMessage.append(elem.toString() + "\n");
		}
	}

	@Override
	public void writeMessageToLog() {
		errorStream.println(collectedMessage.toString());
		collectedMessage = new StringBuilder();
	}

}
