package com.github.metabrain.openjdk8_javac_plugin_framework.api;

import com.sun.tools.javac.tree.JCTree;

public abstract class Request {
	protected JCTree requester ;
	protected JCTree target ;
	
	public JCTree getRequester() { return requester ; }
	public JCTree getTarget() { return target ; }
} 
