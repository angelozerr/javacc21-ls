/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.javacc.ls.ls;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.CodeLensParams;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentLink;
import org.eclipse.lsp4j.DocumentLinkParams;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4j.FoldingRangeRequestParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import com.javacc.Grammar;
import com.javacc.MetaParseException;
import com.javacc.ls.ls.commons.ModelTextDocument;
import com.javacc.ls.ls.commons.ModelTextDocuments;
import com.javacc.ls.parser.JavaCCGrammarParserUtils;
import com.javacc.ls.services.JavaCCLanguageService;
import com.javacc.ls.settings.CompositeSettings;
import com.javacc.ls.settings.JavaCCValidationSettings;
import com.javacc.ls.settings.SharedSettings;
import com.javacc.ls.utils.JavaCCPositionUtility;
import com.javacc.parser.ParseException;
import com.javacc.parser.tree.GrammarFile;

import freemarker.template.TemplateException;

/**
 * LSP text document service for 'application.properties' file.
 *
 */
public class JavaCCTextDocumentService implements TextDocumentService {

	private final ModelTextDocuments<GrammarFile> documents;

	private final JavaCCLanguageServer javaccLanguageServer;

	private final SharedSettings sharedSettings;

	private boolean hierarchicalDocumentSymbolSupport;

	private boolean definitionLinkSupport;

	public JavaCCTextDocumentService(JavaCCLanguageServer javaccLanguageServer, SharedSettings sharedSettings) {
		this.javaccLanguageServer = javaccLanguageServer;
		this.documents = new ModelTextDocuments<GrammarFile>((document, cancelChecker) -> {
			return JavaCCGrammarParserUtils.parse(document.getText(), document.getUri(),
					() -> cancelChecker.checkCanceled());
		});
		this.sharedSettings = sharedSettings;
	}

	/**
	 * Update shared settings from the client capabilities.
	 * 
	 * @param capabilities the client capabilities
	 */
	public void updateClientCapabilities(ClientCapabilities capabilities) {
		TextDocumentClientCapabilities textDocumentClientCapabilities = capabilities.getTextDocument();
		if (textDocumentClientCapabilities != null) {
			sharedSettings.getCompletionSettings().setCapabilities(textDocumentClientCapabilities.getCompletion());
			sharedSettings.getFoldingSettings().setCapabilities(textDocumentClientCapabilities.getFoldingRange());
			hierarchicalDocumentSymbolSupport = textDocumentClientCapabilities.getDocumentSymbol() != null
					&& textDocumentClientCapabilities.getDocumentSymbol().getHierarchicalDocumentSymbolSupport() != null
					&& textDocumentClientCapabilities.getDocumentSymbol().getHierarchicalDocumentSymbolSupport();
			definitionLinkSupport = textDocumentClientCapabilities.getDefinition() != null
					&& textDocumentClientCapabilities.getDefinition().getLinkSupport() != null
					&& textDocumentClientCapabilities.getDefinition().getLinkSupport();
		}
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		ModelTextDocument<GrammarFile> document = documents.onDidOpenTextDocument(params);
		triggerValidationFor(document);
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		ModelTextDocument<GrammarFile> document = documents.onDidChangeTextDocument(params);
		triggerValidationFor(document);
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		documents.onDidCloseTextDocument(params);
		TextDocumentIdentifier document = params.getTextDocument();
		String uri = document.getUri();
		javaccLanguageServer.getLanguageClient()
				.publishDiagnostics(new PublishDiagnosticsParams(uri, new ArrayList<Diagnostic>()));
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {

	}

	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
		return getGrammarFile(params.getTextDocument(), (cancelChecker, grammarFile) -> {
			CompletionList list = getJavaCCLanguageService().doComplete(grammarFile, params.getPosition(),
					sharedSettings.getCompletionSettings(), sharedSettings.getFormattingSettings(), cancelChecker);
			return Either.forRight(list);
		});
	}

	@Override
	public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
		return getGrammarFile(params.getTextDocument(), (cancelChecker, grammarFile) -> {
			return getJavaCCLanguageService().findReferences(grammarFile, params.getPosition(), params.getContext(),
					cancelChecker);
		});
	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		if (!sharedSettings.getCodeLensSettings().isEnabled()) {
			return CompletableFuture.completedFuture(Collections.emptyList());
		}
		return getGrammarFile(params.getTextDocument(), (cancelChecker, grammarFile) -> {
			return getJavaCCLanguageService().getCodeLens(grammarFile, sharedSettings.getCodeLensSettings(),
					cancelChecker);
		});
	}

	@Override
	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams params) {
		return getGrammarFile(params.getTextDocument(), (cancelChecker, grammarFile) -> {
			return getJavaCCLanguageService().findDocumentHighlights(grammarFile, params.getPosition(), cancelChecker);
		});
	}

	@Override
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(
			TextDocumentPositionParams params) {
		return getGrammarFile(params.getTextDocument(), (cancelChecker, grammarFile) -> {
			if (definitionLinkSupport) {
				return Either.forRight(
						getJavaCCLanguageService().findDefinition(grammarFile, params.getPosition(), cancelChecker));
			}
			List<? extends Location> locations = getJavaCCLanguageService()
					.findDefinition(grammarFile, params.getPosition(), cancelChecker) //
					.stream() //
					.map(locationLink -> JavaCCPositionUtility.toLocation(locationLink)) //
					.collect(Collectors.toList());
			return Either.forLeft(locations);
		});
	}

	@Override
	public CompletableFuture<List<FoldingRange>> foldingRange(FoldingRangeRequestParams params) {
		return getGrammarFile(params.getTextDocument(), (cancelChecker, grammarFile) -> {
			return getJavaCCLanguageService().getFoldingRanges(grammarFile, sharedSettings.getFoldingSettings(),
					cancelChecker);
		});
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
		return getGrammarFile(params.getTextDocument(), (cancelChecker, grammarFile) -> {
			CompositeSettings settings = new CompositeSettings(getSharedSettings(), params.getOptions());
			return getJavaCCLanguageService().format(grammarFile, null, settings);
		});
	}

	@Override
	public CompletableFuture<List<DocumentLink>> documentLink(DocumentLinkParams params) {
		return getGrammarFile(params.getTextDocument(), (cancelChecker, grammarFile) -> {
			return getJavaCCLanguageService().findDocumentLinks(grammarFile);
		});
	}

	public CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> documentSymbol(
			DocumentSymbolParams params) {
		return getGrammarFile(params.getTextDocument(), (cancelChecker, grammarFile) -> {
			if (hierarchicalDocumentSymbolSupport) {
				return getJavaCCLanguageService().findDocumentSymbols(grammarFile, cancelChecker) //
						.stream() //
						.map(s -> {
							Either<SymbolInformation, DocumentSymbol> e = Either.forRight(s);
							return e;
						}) //
						.collect(Collectors.toList());
			}
			return getJavaCCLanguageService().findSymbolInformations(grammarFile, cancelChecker) //
					.stream() //
					.map(s -> {
						Either<SymbolInformation, DocumentSymbol> e = Either.forLeft(s);
						return e;
					}) //
					.collect(Collectors.toList());
		});
	}

	private JavaCCLanguageService getJavaCCLanguageService() {
		return javaccLanguageServer.getJavaCCLanguageService();
	}

	private void triggerValidationFor(ModelTextDocument<GrammarFile> document) {
		getGrammarFile(document, (cancelChecker, grammarFile) -> {
			List<Diagnostic> diagnostics = getJavaCCLanguageService().doDiagnostics(grammarFile,
					getSharedSettings().getValidationSettings(), cancelChecker);
			javaccLanguageServer.getLanguageClient()
					.publishDiagnostics(new PublishDiagnosticsParams(document.getUri(), diagnostics));
			return null;
		});
	}

	/**
	 * Returns the text document from the given uri.
	 * 
	 * @param uri the uri
	 * @return the text document from the given uri.
	 */
	public ModelTextDocument<GrammarFile> getDocument(String uri) {
		return documents.get(uri);
	}

	/**
	 * Returns the properties model for a given uri in a future and then apply the
	 * given function.
	 * 
	 * @param <R>
	 * @param documentIdentifier the document identifier.
	 * @param code               a bi function that accepts a {@link CancelChecker}
	 *                           and parsed {@link GrammarFile} and returns the to
	 *                           be computed value
	 * @return the properties model for a given uri in a future and then apply the
	 *         given function.
	 */
	public <R> CompletableFuture<R> getGrammarFile(TextDocumentIdentifier documentIdentifier,
			BiFunction<CancelChecker, GrammarFile, R> code) {
		return getGrammarFile(getDocument(documentIdentifier.getUri()), code);
	}

	/**
	 * Returns the properties model for a given uri in a future and then apply the
	 * given function.
	 * 
	 * @param <R>
	 * @param documentIdentifier the document identifier.
	 * @param code               a bi function that accepts a {@link CancelChecker}
	 *                           and parsed {@link GrammarFile} and returns the to
	 *                           be computed value
	 * @return the properties model for a given uri in a future and then apply the
	 *         given function.
	 */
	public <R> CompletableFuture<R> getGrammarFile(ModelTextDocument<GrammarFile> document,
			BiFunction<CancelChecker, GrammarFile, R> code) {
		return computeModelAsync(document.getModel(), code);
	}

	private static <R, M> CompletableFuture<R> computeModelAsync(CompletableFuture<M> loadModel,
			BiFunction<CancelChecker, M, R> code) {
		CompletableFuture<CancelChecker> start = new CompletableFuture<>();
		CompletableFuture<R> result = start.thenCombineAsync(loadModel, code);
		CancelChecker cancelIndicator = () -> {
			if (result.isCancelled())
				throw new CancellationException();
		};
		start.complete(cancelIndicator);
		return result;
	}

	public void updateValidationSettings(JavaCCValidationSettings newValidation) {
		// Update validation settings
		JavaCCValidationSettings validation = sharedSettings.getValidationSettings();
		validation.update(newValidation);
		// trigger validation for all opened application.properties
		documents.all().stream().forEach(document -> {
			triggerValidationFor(document);
		});
	}

	public SharedSettings getSharedSettings() {
		return sharedSettings;
	}

	public CompletableFuture<Integer> generateParser(TextDocumentIdentifier params) {
		return getGrammarFile(params, (cancelChecker, grammarFile) -> {
			Grammar grammar = grammarFile.getGrammar();
			try {
				grammar.createOutputDir();
				grammar.semanticize();

				if (!grammar.getOptions().getUserDefinedLexer() && grammar.getErrorCount() == 0) {
					grammar.generateLexer();
				}

				grammar.generateFiles();

				if ((grammar.getErrorCount() == 0)) {
					if (grammar.getWarningCount() == 0) {
						System.out.println("Parser generated successfully.");
					} else {
						System.out.println(
								"Parser generated with 0 errors and " + grammar.getWarningCount() + " warnings.");
					}
					return 0;
				} else {
					System.out.println("Detected " + grammar.getErrorCount() + " errors and "
							+ grammar.getWarningCount() + " warnings.");
					return (grammar.getErrorCount() == 0) ? 0 : 1;
				}
			} catch (MetaParseException e) {
				System.out.println("Detected " + grammar.getErrorCount() + " errors and " + grammar.getWarningCount()
						+ " warnings.");
				return 1;
			} catch (ParseException e) {
				System.out.println(e.toString());
				System.out.println("Detected " + (grammar.getErrorCount() + 1) + " errors and "
						+ grammar.getWarningCount() + " warnings.");
				return 1;
			} catch (TemplateException | IOException e) {
				System.out.println(e.toString());
				System.out.println("Detected " + (grammar.getErrorCount() + 1) + " errors and "
						+ grammar.getWarningCount() + " warnings.");
				return 1;
			}
		});
	}

}