package com.javacc.ls.parser;

import com.javacc.parser.Node;

public class Problem {

	private final Node node;

	private final String message;

	public Problem(Node node, String message) {
		this.node = node;
		this.message = message;
	}

	public Node getNode() {
		return node;
	}

	public String getMessage() {
		return message;
	}

}
