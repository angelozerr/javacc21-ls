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

import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;

import com.javacc.ls.settings.JavaCCCodeLensSettings;
import com.javacc.ls.settings.JavaCCCompletionSettings;
import com.javacc.ls.settings.JavaCCFoldingSettings;
import com.javacc.ls.settings.JavaCCFormattingSettings;
import com.javacc.ls.settings.JavaCCValidationSettings;
import com.javacc.parser.tree.GrammarFile;

/**
 * The JavaCC language service.
 * 
 * @author Angelo ZERR
 *
 */
public class JavaCCLanguageService {

	private final JavaCCCompletions completions;
	private final JavaCCCodeLens codelens;
	private final JavaCCHighlighting highlighting;
	private final JavaCCDefinition definition;
	private final JavaCCReference references;
	private final JavaCCSymbolsProvider symbolsProvider;
	private final JavaCCDiagnostics diagnostics;
	private final JavaCCFoldings foldings;
	private final JavaCCDocumentLink documentLink;

	public JavaCCLanguageService() {
		this.completions = new JavaCCCompletions();
		this.codelens = new JavaCCCodeLens();
		this.highlighting = new JavaCCHighlighting();
		this.definition = new JavaCCDefinition();
		this.references = new JavaCCReference();
		this.symbolsProvider = new JavaCCSymbolsProvider();
		this.diagnostics = new JavaCCDiagnostics();
		this.foldings = new JavaCCFoldings();
		this.documentLink = new JavaCCDocumentLink();
	}

	/**
	 * Returns completion list for the given position
	 * 
	 * @param JavaCCParser       the Qute JavaCCParser
	 * @param position           the position where completion was triggered
	 * @param completionSettings the completion settings.
	 * @param formattingSettings the formatting settings.
	 * @param cancelChecker      the cancel checker
	 * @return completion list for the given position
	 */
	public CompletionList doComplete(GrammarFile grammarFile, Position position,
			JavaCCCompletionSettings completionSettings, JavaCCFormattingSettings formattingSettings,
			CancelChecker cancelChecker) {
		return completions.doComplete(grammarFile, position, completionSettings, formattingSettings, cancelChecker);
	}

	public List<DocumentHighlight> findDocumentHighlights(GrammarFile grammarFile, Position position,
			CancelChecker cancelChecker) {
		return highlighting.findDocumentHighlights(grammarFile, position, cancelChecker);
	}

	public List<? extends LocationLink> findDefinition(GrammarFile grammarFile, Position position,
			CancelChecker cancelChecker) {
		return definition.findDefinition(grammarFile, position, cancelChecker);
	}

	public List<DocumentSymbol> findDocumentSymbols(GrammarFile grammarFile, CancelChecker cancelChecker) {
		return symbolsProvider.findDocumentSymbols(grammarFile, cancelChecker);
	}

	public List<SymbolInformation> findSymbolInformations(GrammarFile grammarFile, CancelChecker cancelChecker) {
		return symbolsProvider.findSymbolInformations(grammarFile, cancelChecker);
	}

	/**
	 * Validate the given JavaCC <code>grammarFile</code>.
	 * 
	 * @param grammarFile        the grammar file.
	 * @param validationSettings the validation settings.
	 * @param cancelChecker      the cancel checker.
	 * @return the result of the validation.
	 */
	public List<Diagnostic> doDiagnostics(GrammarFile grammarFile, JavaCCValidationSettings validationSettings,
			CancelChecker cancelChecker) {
		return diagnostics.doDiagnostics(grammarFile, validationSettings, cancelChecker);
	}

	public List<? extends CodeLens> getCodeLens(GrammarFile grammarFile, JavaCCCodeLensSettings codeLensSettings,
			CancelChecker cancelChecker) {
		return codelens.getCodeLens(grammarFile, codeLensSettings, cancelChecker);
	}

	public List<? extends Location> findReferences(GrammarFile grammarFile, Position position, ReferenceContext context,
			CancelChecker cancelChecker) {
		return references.findReferences(grammarFile, position, context, cancelChecker);
	}

	public List<FoldingRange> getFoldingRanges(GrammarFile grammarFile, JavaCCFoldingSettings context,
			CancelChecker cancelChecker) {
		return foldings.getFoldingRanges(grammarFile, context, cancelChecker);
	}

	public List<DocumentLink> findDocumentLinks(GrammarFile grammarFile) {
		return documentLink.findDocumentLinks(grammarFile);
	}
}
