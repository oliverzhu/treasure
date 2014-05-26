package com.home.patterns.ten_two.improved;

public interface NodeVisitor {
	public abstract void visitTag(Tag tag);
	public abstract void visitEndTag(EndTag tag);
	public abstract void visitLinkTag(LinkTag tag);
	public abstract void visitStringNode(StringNode stringNode);

}
