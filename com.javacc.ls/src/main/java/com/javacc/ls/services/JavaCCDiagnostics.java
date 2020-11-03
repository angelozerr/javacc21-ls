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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.javacc.Grammar;
import com.javacc.JavaCCError;
import com.javacc.JavaCCError.Type;
import com.javacc.ls.ls.commons.TextDocument;
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
	 * @param document
	 * @param validationSettings the validation settings.
	 * @param cancelChecker      the cancel checker.
	 * @return the result of the validation.
	 */
	public List<Diagnostic> doDiagnostics(GrammarFile grammarFile, TextDocument document,
			JavaCCValidationSettings validationSettings, CancelChecker cancelChecker) {
		if (validationSettings == null) {
			validationSettings = JavaCCValidationSettings.DEFAULT;
		}
		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
		try {
			Field f = Grammar.class.getDeclaredField("errors");
			f.setAccessible(true);
			List<JavaCCError> errors = (List<JavaCCError>) f.get(grammarFile.getGrammar());
			for (JavaCCError error : errors) {
				Range range = new Range(
						new Position(error.beginLine - 1,
								error.beginColumn > 0 ? error.beginColumn - 1 : error.beginColumn),
						new Position(error.beginLine - 1, error.beginColumn));
				Diagnostic diagnostic = new Diagnostic(range, error.message, getSeverity(error.type), "javacc", null);
				diagnostics.add(diagnostic);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return diagnostics;
	}

	private DiagnosticSeverity getSeverity(Type type) {
		switch (type) {
		case WARNING:
			return DiagnosticSeverity.Warning;
		default:
			return DiagnosticSeverity.Error;
		}
	}

}
