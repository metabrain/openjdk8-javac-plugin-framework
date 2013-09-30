package com.github.metabrain.openjdk8_javac_plugin_framework.api;


import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

public class RequestAdd extends Request {

	public List<JCTree> treesToAdd ;
	public int position ;
	
	/**
	 * Create a request to add a new tree node ({@code treeToAdd}) to a certain
	 * 	{@code position} in the {@code target} tree node. The {@code requester}
	 * 	is the node that creates the request.
	 * 
	 * Normally used to add a new tree node to the method body when we evaluating an
	 * 	inner node and the method is out of scope, since it's further up in the visitor
	 * 	invocation path.
	 * 
	 * 
	 * @param requester - The node requesting the addition. 
	 * @param target - Where to add (normally a JCMethodDecl tree node, since we add to
	 * 	its body (a List of JCStatement).
	 * @param treeToAdd - The new tree node to add.
	 * @param position - Position where to add it.
	 */
	public RequestAdd(JCTree requester, JCTree target, List<JCTree> treesToAdd, int position) {
		this.requester = requester ;
		this.target = target ;
		this.treesToAdd = treesToAdd ;
		this.position = position ;
	}
}