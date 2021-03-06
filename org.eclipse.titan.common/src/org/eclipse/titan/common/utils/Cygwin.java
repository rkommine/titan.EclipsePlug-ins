/******************************************************************************
 * Copyright (c) 2000-2015 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.common.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;
import org.eclipse.titan.common.logging.ErrorReporter;

public class Cygwin {
	// singleton for speed
	static Boolean installed = null;

	private Cygwin() {
		// Disable constructor
	}

	/**
	 * Checks if cygwin is installed, makes sense only on windows
	 * @return true if cygwin is installed
	 */
	public static boolean isInstalled() {
		if (installed!=null) {
			return installed.booleanValue();
		}
		installed = Boolean.FALSE;
		try {
			// apparently reading the value with command line reg.exe command does not need admin rights
			String regQueryOutput = executeProgram("reg query HKCU\\Software\\Cygwin\\Installations");
			if (regQueryOutput==null) {
				regQueryOutput = executeProgram("reg query HKLM\\SOFTWARE\\Cygwin\\Installations");
				if(regQueryOutput==null) {
					return installed.booleanValue();
				}
			}

			// check if error occured:
			Pattern regQueryOutputPattern = Pattern.compile("ERROR.*");
			Matcher regQueryoutputMatcher = regQueryOutputPattern.matcher(regQueryOutput);
			if (regQueryoutputMatcher.matches()) {
				return installed.booleanValue(); 
			}
			installed = Boolean.TRUE;	
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace("While checking if cygwin is installed",e);
		}
		return installed.booleanValue();
	}
	/**
	 * Checks if there is a Win32 OS without installed cygwin. 
	 * @return true if the OS is Win32 and there is no installed cygwin, otherwise returns false
	 */
	public static boolean isMissingInOSWin32() {
		if (Platform.OS_WIN32.equals(Platform.getOS()) && !Cygwin.isInstalled() ) {
			return true;
		} 
		return false;
	}
	
	/**
	 * @return Returns the standard output if no exceptions and return code was 0, otherwise returns null
	 */
	static String executeProgram(String command) {
		Process proc;
		
		try {
			proc = Runtime.getRuntime().exec(command);
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace("Error while executing " + command, e);
			return null;
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream(), Charset.defaultCharset()));
		try {
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);  
			}
			if (proc.waitFor() != 0) {
				return null;
			}
			return sb.toString();
		} catch (Exception e) {
			ErrorReporter.logExceptionStackTrace("Error while executing " + command, e);
		} finally {
			IOUtils.closeQuietly(reader);
		}

		return null;
	}
}
