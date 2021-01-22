import java.io.IOException;
import java.io.Writer;

import com.google.gson.GsonBuilder;
import com.javacc.Grammar;
import com.javacc.JavaCCErrorReporter;
import com.javacc.parser.JavaCCParser;
import com.javacc.parser.Node;
import com.javacc.parser.ParseException;
import com.javacc.parser.tree.BNFProduction;
import com.javacc.parser.tree.ExpansionSequence;
import com.javacc.parser.tree.GrammarFile;
import com.javacc.parser.tree.RegexpRef;

public class JavaCC2TextMateConverter {

	public void convert(String fileName, String content, Writer writer) throws IOException, ParseException {
		GrammarFile grammarFile = parseGrammar(fileName, content, null);
		Node root = grammarFile.getRoot();
		if (root != null) {
			TextMateGrammar textmate = new TextMateGrammar("JavaCC");
			for (int i = 0; i < root.getChildCount(); i++) {
				Node child = root.getChild(i);
				if (child instanceof BNFProduction) {
					BNFProduction bnfProduction = (BNFProduction) child;
					String name = bnfProduction.getName();
					convert(bnfProduction, textmate);
				}
			}
			String s = new GsonBuilder().setPrettyPrinting().create().toJson(textmate);
			System.err.println(s);
		}
	}

	private void convert(BNFProduction node, TextMateGrammar textmate) {
		String name = node.getName();
		if ("Root".equals(name)) {
			Pattern include = new Pattern();
			include.setInclude("#" + name);
			textmate.getPatterns().add(include);
		}
		Pattern pattern = new Pattern();
		textmate.getRepository().put(name, pattern);
		for (int i = 0; i < node.getChildCount(); i++) {
			Node child = node.getChild(i);
			if (child instanceof ExpansionSequence) {
				ExpansionSequence expansion = (ExpansionSequence) child;
				Node first = expansion.getFirstChild();
				if (first instanceof RegexpRef) {
					String regexp = ((RegexpRef) first).getRegexp().getAsString();
					System.err.println(regexp);
				}
				Node end = expansion.getLastChild();
				if (end instanceof RegexpRef) {

				}
			}
		}

	}

	private static GrammarFile parseGrammar(String fileName, String content, JavaCCErrorReporter reporter)
			throws IOException, ParseException {
		Grammar grammar = new Grammar();
		grammar.setFilename(fileName);
		if (reporter != null) {
			grammar.setReporter(reporter);
		}
		JavaCCParser parser = new JavaCCParser(grammar, fileName, content);
		return parser.Root();
	}
}
