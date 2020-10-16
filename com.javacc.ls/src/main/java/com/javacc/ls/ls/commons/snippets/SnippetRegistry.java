package com.javacc.ls.ls.commons.snippets;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.javacc.ls.ls.commons.BadLocationException;
import com.javacc.ls.ls.commons.TextDocument;
import com.javacc.ls.utils.StringUtils;

public class SnippetRegistry {

	private static final SnippetRegistry INSTANCE = new SnippetRegistry();

	public static SnippetRegistry getInstance() {
		return INSTANCE;
	}

	private static class LineContext implements ISnippetLineContext {

		private final TextDocument document;
		private final int completionOffset;
		private String lineDelimiter;
		private String whitespacesIndent;

		public LineContext(TextDocument document, int completionOffset) {
			this.document = document;
			this.completionOffset = completionOffset;
		}

		@Override
		public String getLineDelimiter() {
			if (lineDelimiter == null) {
				compute();
			}
			return lineDelimiter;
		}

		private void compute() {
			try {
				int lineNumber = document.positionAt(completionOffset).getLine();
				String lineText = document.lineText(lineNumber);
				lineDelimiter = document.lineDelimiter(lineNumber);
				whitespacesIndent = StringUtils.getStartWhitespaces(lineText);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public String getWhitespacesIndent() {
			if (whitespacesIndent == null) {
				compute();
			}
			return whitespacesIndent;
		}

		@Override
		public Range getReplaceRange(String expr) throws BadLocationException {
			int startOffset = StringUtils.findExprBeforeAt(document.getText(), expr, completionOffset);
			if (startOffset == -1) {
				startOffset = completionOffset;
			}
			int endOffset = completionOffset;
			return getReplaceRange(startOffset + 1, endOffset, document, completionOffset);
		}

		private static Range getReplaceRange(int replaceStart, int replaceEnd, TextDocument document, int offset)
				throws BadLocationException {
			if (replaceStart > offset) {
				replaceStart = offset;
			}
			return new Range(document.positionAt(replaceStart), document.positionAt(replaceEnd));
		}
	}

	private final List<Snippet> snippets;

	public SnippetRegistry() {
		snippets = new ArrayList<>();
	}

	public void registerSnippet(Snippet snippet) {
		snippets.add(snippet);
	}

	public void load(InputStream in) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			Snippet snippet = new GsonBuilder().create().fromJson(reader, Snippet.class);
			if (snippet.getDescription() == null) {
				snippet.setDescription(name);
			}
			registerSnippet(snippet);
		}
		reader.endObject();
	}

	public List<Snippet> getSnippets() {
		return snippets;
	}

	public Collection<CompletionItem> getCompletionItems(TextDocument document, int completionOffset,
			boolean canSupportMarkdown, Predicate<SnippetContext> contextFilter) {
		ISnippetLineContext lineContext = new LineContext(document, completionOffset);
		return getCompletionItems(lineContext, canSupportMarkdown, contextFilter);
	}

	public Collection<CompletionItem> getCompletionItems(ISnippetLineContext lineContext, boolean canSupportMarkdown,
			Predicate<SnippetContext> contextFilter) {
		return getSnippets().stream().filter(snippet -> {
			if (snippet.getContext() == null) {
				return true;
			}
			return contextFilter.test(snippet.getContext());
		}).map(snippet -> {
			String prefix = snippet.getPrefix();
			try {
				Range range = lineContext.getReplaceRange(prefix);
				String label = prefix;
				CompletionItem item = new CompletionItem();
				item.setLabel(label);
				String insertText = getInsertText(snippet, true, lineContext);
				item.setKind(CompletionItemKind.Snippet);
				item.setDocumentation(Either.forRight(createDocumentation(snippet, canSupportMarkdown, lineContext)));
				item.setFilterText(insertText);
				item.setTextEdit(new TextEdit(range, insertText));
				item.setInsertTextFormat(InsertTextFormat.Snippet);
				return item;
			} catch (BadLocationException e) {
				e.printStackTrace();
				return null;
			}

		}).filter(item -> item != null).collect(Collectors.toList());
	}

	private static MarkupContent createDocumentation(Snippet snippet, boolean canSupportMarkdown,
			ISnippetLineContext lineContext) {
		String description = snippet.getDescription();
		StringBuilder doc = new StringBuilder(description);
		doc.append(System.lineSeparator());
		if (canSupportMarkdown) {
			doc.append("___");
		}
		doc.append(System.lineSeparator());
		if (canSupportMarkdown) {
			doc.append(System.lineSeparator());
			doc.append("```");
			String scope = snippet.getScope();
			if (scope != null) {
				doc.append(scope);
			}
			doc.append(System.lineSeparator());
		}
		String insertText = getInsertText(snippet, false, lineContext);
		doc.append(insertText);
		if (canSupportMarkdown) {
			doc.append("```");
			doc.append(System.lineSeparator());
		}
		return new MarkupContent(canSupportMarkdown ? MarkupKind.MARKDOWN : MarkupKind.PLAINTEXT, doc.toString());
	}

	private static String getInsertText(Snippet snippet, boolean indent, ISnippetLineContext lineContext) {
		StringBuilder text = new StringBuilder();
		int i = 0;
		for (String bodyLine : snippet.getBody()) {
			if (i > 0) {
				text.append(lineContext.getLineDelimiter());
				if (indent) {
					text.append(lineContext.getWhitespacesIndent());
				}
			}
			text.append(bodyLine);
			i++;
		}
		text.append(lineContext.getLineDelimiter());
		return text.toString();
	}

}
