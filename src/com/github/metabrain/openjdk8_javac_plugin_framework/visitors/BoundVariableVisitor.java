package com.github.metabrain.openjdk8_javac_plugin_framework.visitors;

import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.List;

import java.util.LinkedList;


/** 
 *	Used to visit a sub-tree and return all variables declarations
 *	(that represent bound variables) in it.
 *  
 * @author MetaBrain
 */
public class BoundVariableVisitor extends TreeScanner {
	
	//where the list of bound variables found are kept
	private java.util.List<JCVariableDecl> boundVariables ;
	
	//Creates visitor and scans the tree
	public BoundVariableVisitor(JCMethodDecl tree) {
		super();
		boundVariables = new LinkedList<JCVariableDecl>() ;		

		scan(tree) ;
//		System.out.println(">>> FOUND "+getBoundVariables().size()+" BOUND VARIABLES  IN METHOD "+tree.name.toString()+"...");
	} 	
	
	/**
	 * Return a list of the declaration of the free variables this visitor scanned.
	 * 
	 * @return list of free variables declarations
	 */
	public List<JCVariableDecl> getBoundVariables() {
		return List.from(boundVariables) ;
	} 

	/* ***************************************************************************
	 * Visitor methods from TreeScanner
	 ****************************************************************************/ 
	
	@Override
	public void visitMethodDef(JCMethodDecl arg0) {
        scan(arg0.mods);
        scan(arg0.restype);
        scan(arg0.typarams);
 
		//TODO method parameters are bound or free? if free, don't visit
        //In computer programming, the term free variable refers to variables 
        // used in a function that are not local variables nor parameters of 
        // that function.
        //scan(arg0.params);
        
        scan(arg0.thrown);
        scan(arg0.defaultValue);
        scan(arg0.body);
	}
	
	@Override
	public void visitVarDef(JCVariableDecl arg0) {
		boundVariables.add(arg0) ;
//		System.out.println("FOUND JCVariableDecl "+arg0+"\n\t"+"\t"+arg0.type+"\t"+arg0.name+"\t"+arg0.sym);
		
		super.visitVarDef(arg0) ;
    }
}