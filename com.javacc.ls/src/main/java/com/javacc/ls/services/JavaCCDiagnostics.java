/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.javacc.ls.services;

import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.javacc.ls.parser.LSPJavaCCErrorReporter;
import com.javacc.ls.settings.JavaCCValidationSettings;
import com.javacc.parser.tree.GrammarFile;

/**
 * JavaCC diagnostics support.
 *
 */
class JavaCCDiagnostics {

	/**
	 * Validate the given JavaCC <code>grammarFile</code>.
	 * 
	 * @param grammarFile        the JavaCC grammar file.
	 * @param validationSettings the validation settings.
	 * @param cancelChecker      the cancel checker.
	 * @return the result of the validation.
	 */
	public List<Diagnostic> doDiagnostics(GrammarFile grammarFile, JavaCCValidationSettings validationSettings,
			CancelChecker cancelChecker) {
		if (validationSettings == null) {
			validationSettings = JavaCCValidationSettings.DEFAULT;
		}
		LSPJavaCCErrorReporter reporter = (LSPJavaCCErrorReporter) grammarFile.getGrammar().getReporter();
		return reporter.getDiagnostics();
	}

}
