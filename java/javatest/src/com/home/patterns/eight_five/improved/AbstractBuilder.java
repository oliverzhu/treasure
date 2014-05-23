package com.home.patterns.eight_five.improved;

import java.util.LinkedList;

public abstract class AbstractBuilder {
	protected LinkedList<XMLNode> history;
	protected XMLNode root;
	protected XMLNode current;
	
	protected abstract XMLNode createNode(String nodeName);

}
