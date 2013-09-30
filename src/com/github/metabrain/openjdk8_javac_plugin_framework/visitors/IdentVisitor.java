package com.github.metabrain.openjdk8_javac_plugin_framework.visitors;

import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

public class IdentVisitor extends TreeScanner { 
	
	ListBuffer<JCIdent> ids = new ListBuffer<JCIdent>() ;
	
	@Override
	public void visitIdent(JCIdent arg0) {
		super.visitIdent(arg0);
		ids.add(arg0) ;
	}

	public static List<JCIdent> getIdentifiers(JCMethodDecl tree) {
		IdentVisitor v = new IdentVisitor() ;
		v.scan(tree) ;
		return v.ids.toList() ;
	}
}
