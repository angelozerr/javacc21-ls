import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import com.google.gson.GsonBuilder;
import com.javacc.Grammar;
import com.javacc.JavaCCErrorReporter;
import com.javacc.parsegen.Expansion;
import com.javacc.parser.Node;
import com.javacc.parser.ParseException;
import com.javacc.parser.tree.BNFProduction;
import com.javacc.parser.tree.GrammarFile;
import com.javacc.parser.tree.Identifier;
import com.javacc.parser.tree.NonTerminal;
import com.javacc.parser.tree.RegexpRef;
import com.javacc.parser.tree.RegexpSpec;
import com.javacc.parser.tree.TokenProduction;

public class JavaCC2TextMateConverter {

	public void convert(String fileName, String content, String grammarName, String[] fileTypes, Writer writer)
			throws IOException, ParseException {
		GrammarFile grammarFile = parseGrammar(fileName, content, null);
		TextMateGrammar textmate = new TextMateGrammar(grammarName, fileTypes);

		Collection<TokenProduction> tokenProductions = grammarFile.getGrammar().getAllTokenProductions();
		for (TokenProduction tokenProduction : tokenProductions) {
			convert(tokenProduction, textmate);
		}

		Collection<BNFProduction> productions = grammarFile.getGrammar().getParserProductions();
		for (BNFProduction bnfProduction : productions) {
			convert(bnfProduction, textmate);
		}
		String s = new GsonBuilder().setPrettyPrinting().create().toJson(textmate);
		System.err.println(s);
	}

	private void convert(TokenProduction node, TextMateGrammar textmate) {
		Collection<RegexpSpec> regexps = node.getRegexpSpecs();
		for (RegexpSpec regexpSpec : regexps) {			
			String name= getRegexpIdentifier(regexpSpec);
			if (name != null) {				
				Pattern pattern = new Pattern();
				pattern.setName("meta.constant." + name + "." + textmate.getExtension());
				textmate.getRepository().put(name, pattern);
				//node.convert(node, pattern);				
			}
		}
//		/String name = node.firstChildOfType(JavaCCKeyWord.class).getImage();
//		Pattern pattern = new Pattern();
//		pattern.setName("meta.structure." + name + "." + textmate.getExtension());
//		textmate.getRepository().put(name, pattern);
		//node.convert(node, pattern);
	}

	private void convert(BNFProduction node, TextMateGrammar textmate) {
		String name = node.getName();
		if ("Root".equals(name)) {
			Pattern include = new Pattern();
			include.setInclude("#" + name);
			textmate.getPatterns().add(include);
		}
		Pattern pattern = new Pattern();
		pattern.setName("meta.structure." + name + "." + textmate.getExtension());
		textmate.getRepository().put(name, pattern);
		convert(node, pattern);
	}

	private void convert(BNFProduction production, Pattern pattern) {
		Expansion expansion = production.getExpansion();
		if (expansion != null) {
			convert(expansion, pattern, true);
		}
	}

	private void convert(Expansion expansion, Pattern pattern, boolean root) {
		int i = 0;
		for (Node child : expansion.children()) {
			if (child instanceof NonTerminal) {
				String include = ((NonTerminal) child).getName();
				pattern.addPatternInclude(include);
			} else if (child instanceof RegexpRef) {
				RegexpRef regexpRef = (RegexpRef) child;
				String regexp = regexpRef.getRegexp().getImage();
				if (regexp != null) {
					if (!root) {
						Pattern p = pattern;
						p = new Pattern();
						pattern.getPatterns().add(p);
						p.setMatch(regexp);
					} else {
						if (i == 0) {
							pattern.setBegin(regexp);
						} else if (i == expansion.children().size() - 1) {
							pattern.setEnd(regexp);
						}
					}
				} else {
					//String label = getRegexpIdentifier((RegexpSpec) regexpRef.getRegexp().getParent());
					//if (label == null) {
					//	label = regexpRef.getLabel();
					//}
					String label = regexpRef.getLabel();
					if (label != null) {
						pattern.addPatternInclude(label);
					}
				}
			} else if (child instanceof Expansion) {
				Expansion e = ((Expansion) child).getNestedExpansion();
				if (e == null) {
					e = (Expansion) child;
				}				
				convert(e, pattern, false);
			}
			i++;
		}
	}

	private void convert(Node parent, Pattern pattern) {
		int i = 0;
		for (Node child : parent.children()) {
			if (i == 0) {
				Node first = child;
				if (first instanceof RegexpRef) {
					String regexp = ((RegexpRef) first).getRegexp().getImage();
					pattern.setBegin(regexp);
				}
			} else if (i == parent.children().size() - 1) {
				Node end = child;
				if (end instanceof RegexpRef) {
					String regexp = ((RegexpRef) end).getRegexp().getImage();
					pattern.setEnd(regexp);
				}
			} else {
				convert(child, pattern);
			}

			i++;
			/*
			 * if (child instanceof ExpansionSequence) {
			 * 
			 * ExpansionSequence expansion = (ExpansionSequence) child; Node first =
			 * expansion.getFirstChild(); if (first instanceof RegexpRef) { String regexp =
			 * ((RegexpRef) first).getRegexp().getImage(); pattern.setBegin(regexp); } Node
			 * end = expansion.getLastChild(); if (end instanceof RegexpRef) { String regexp
			 * = ((RegexpRef) end).getRegexp().getImage(); pattern.setEnd(regexp); } }
			 */
		}
	}

	private static GrammarFile parseGrammar(String fileName, String content, JavaCCErrorReporter reporter)
			throws IOException, ParseException {
		Grammar grammar = new Grammar(new File("."), 1, true);
		GrammarFile root = (GrammarFile) grammar.parse(fileName, true);
		grammar.semanticize();
		return root;
	}
	
	private static String getRegexpIdentifier(RegexpSpec regexpSpec) {
		Identifier last = regexpSpec.firstChildOfType(Identifier.class);
		return (last != null ? last.getImage() : null);
	}
}
