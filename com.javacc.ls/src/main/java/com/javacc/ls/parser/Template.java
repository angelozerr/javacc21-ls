package com.javacc.ls.parser;

import java.util.List;

import com.javacc.parser.Node;
import com.javacc.parser.ParseException;

import freemarker.core.parser.ParsingProblem;

public class Template {

	private final String templateId;

	private Node root;

	private List<ParsingProblem> problems;

	private ParseException parseException;

	public Template(String templateId) {
		this.templateId = templateId;
	}

	public String getId() {
		return templateId;
	}

	public void setParsingProblems(List<ParsingProblem> list) {
		this.problems = list;
	}

	public List<ParsingProblem> getProblems() {
		return problems;
	}

	public void setRoot(Node root) {
		this.root = root;
	}

	public Node getRoot() {
		return root;
	}

	public ParseException getParseException() {
		return parseException;
	}

	public void setParseException(ParseException parseException) {
		this.parseException = parseException;
	}
}
