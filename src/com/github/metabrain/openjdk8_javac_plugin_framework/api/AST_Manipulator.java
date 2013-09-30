package com.github.metabrain.openjdk8_javac_plugin_framework.api;

import com.github.metabrain.openjdk8_javac_plugin_framework.exceptions.MethodNotFound;
import com.github.metabrain.openjdk8_javac_plugin_framework.visitors.BoundVariableVisitor;
import com.github.metabrain.openjdk8_javac_plugin_framework.visitors.IdentVisitor;
import com.sun.tools.javac.api.JavacScope;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

//import com.sun.tools.javac.code.Types;


public class AST_Manipulator {

	private final Context context ;
	private final JCCompilationUnit jcc ;
	private final TreeMaker make ;
	private final Names names ;
	private final Types types ;
	private final Attr attr ;
	private final Symtab symtab ;
	private final TreeTranslatorStateful state ;

	public AST_Manipulator(Context context, JCCompilationUnit jcc, TreeTranslatorStateful state) {
		this.context = context ;
		this.jcc = jcc ;
		this.state = state ;
		this.make = TreeMaker.instance(context) ;
		this.names = Names.instance(context) ;
		this.types = Types.instance(context) ;
		this.attr = Attr.instance(context) ;
		this.symtab = Symtab.instance(context) ;
	}

	public JCStatement makePrintln(JCAnnotation tree, List<JCTree> methods) {
		JCMethodDecl printMeth = null ;
		for(JCTree m : methods) {
			JCMethodDecl methodDecl = (JCMethodDecl) m ;
			if(methodDecl.name.toString().contains("win")) {
//				System.out.println("GOT IT!!!!! ----->"+methodDecl.name);
				printMeth = methodDecl ;
			}
		}

		JCExpression methExp = make.Ident(printMeth.sym);
		JCExpression invExp = make.App(methExp);

		return make.Call(invExp) ;
	}

	public JCMethodDecl filterMethod(java.util.List<JCMethodDecl> meths, String methName) {
		for(JCMethodDecl m : meths) {
			if(m.name.toString().contains(methName)) {
//				System.out.println("GOT IT!!!!! ----->"+m.name);
				return m ;
			}
		}

		throw new MethodNotFound(methName) ;
	}

	/**
	 * Creates an AST node for invocating method in the same class by
	 * 	its name.
	 *
	 * METHOD WITH PARAMETERS SUPPORTED!
	 *
	 * @param methodName - Name of the method from the same class to invoke
	 * @param params - List<JCVariableDecl> of the parameters
	 * @return the new AST node
	 */
//	public JCStatement invokeMethodFromThisClass(String methodName, List<JCExpression> params) {
//		//finding the method in this class
//		JCMethodDecl theMethod = filterMethod(state.getCurrentClassMethods(), methodName) ;
//
//		//using its symbol to reference it
//		JCIdent ident =  make.Ident(theMethod.sym) ;
//
//		//creating invocation as always
//		JCExpression invExp = null ;
//
//		if(params.isEmpty()) //without arguments
//		{
//			System.out.println("invoked with no args");
//			invExp = make.App(ident);
//		}
//		else //with arguments
//		{
//			System.out.println("invoked with arguments");
//			invExp = make.App(ident,params) ;
//		}
//
//		JCStatement invocation = make.Call(invExp) ;
//		return invocation ;
//	}

	/**
	 * Creates an AST node for invocating method in the same class by
	 * 	its name.
	 *
	 * NO METHOD WITH PARAMETERS SUPPORTED!
	 *
	 * @param methodName - Name of the method from the same class to invoke
	 * @return the new AST node
	 */
//	public JCStatement invokeMethodFromThisClass(String methodName) {
//		return invokeMethodFromThisClass(methodName, List.<JCExpression>nil()) ;
//	}

	private long stringToModifiers(String modifiers) {
		//TODO ver se o default ? mesmo zero...
		long flags = 0 ;

		for(String mod : modifiers.split(" ")) {
			if(mod.toLowerCase().equals("public"))
				flags |= Flags.PUBLIC ;
			else if(mod.toLowerCase().equals("private"))
				flags |= Flags.PRIVATE ;
			else if(mod.toLowerCase().equals("static"))
				flags |= Flags.STATIC ;
			else
				throw new UnsupportedOperationException("Modifier <"+mod+"> still not supported, lazyness.") ;
		}

		return flags ;
	}

	public Type stringToType(String type) {
		if(type.toLowerCase().equals("void"))
			return symtab.voidType ;
		else if(type.toLowerCase().equals("string"))
			return symtab.stringType ;
		else
			throw new UnsupportedOperationException("Only \"void\" and \"String\" type programmed, lazyness.") ;
	}

	/**
	 * Creates an AST node for invocating method using its name, type
	 * 	and modifiers.
	 *
	 * @param methodName - Name of the method to invoke
	 * @param type - Type of the method
	 * @param modifiers - List of modifiers
	 * 	(public, private, static, protected, final, volatile, etc...)
	 * @return the new AST node
	 */
	public JCStatement invokeMethod(
			String methodName,
			String type,
			String modifiers) {

		//Create empty identifier
		JCIdent ident =  make.Ident(names.fromString(methodName)) ;

		//Add it the new type
		List<Type> argTypes = List.<Type>nil() ;
		Type resType = stringToType(type) ;
		List<Type> thrown = List.<Type>nil() ;
		TypeSymbol methodClass = symtab.noSymbol ;
		ident.type = new Type.MethodType(argTypes,resType,thrown,methodClass);

		//Add it the new symbol
		long flags = stringToModifiers(modifiers) ;
		Name name = names.fromString(methodName) ;
		Type typesym = symtab.methodTypeType.noType ; //parece que este n?o ? importante?
		Symbol owner = symtab.noSymbol ;
		ident.sym = new Symbol.MethodSymbol(flags, name, typesym, owner) ;

		JCExpression invExp = make.App(ident);
		JCStatement invocation = make.Call(invExp) ;
		return invocation ;
	}

	public JCStatement makeIfThrowException(JCAssert node) {
		List<JCExpression> args = node.getDetail() == null
				? List.<JCExpression> nil()
				: List.of(node.detail) ;
		JCExpression expr = make.NewClass(
				null,
				null,
				make.Ident(names.fromString("AssertionError")),
				args,
				null) ;
		JCUnary unary = make.Unary(JCTree.Tag.NOT, node.cond) ;

		return make.If(
				unary,
				make.Throw(expr),
				null) ;
	}

	/**
	 * Creates a declaration of a string variable
	 * @param variableName - name of the variable to create
	 * @param content - actual content of the string
	 * @return
	 */
//	public JCVariableDecl declareString(String variableName, String content) {
//		JCLiteral stringLiteral = make.Literal(content);
//    	VarSymbol stringSymbol = new VarSymbol(
//    			0,
//    			names.fromString(variableName),
//    			stringToType("string"),
//    			(Symbol)(state.getParentMethod().sym)) ;
//
//    	return make.VarDef(stringSymbol, stringLiteral) ;
//	}

	/**
	 * From its declaration, returns the variable reference to use, for example,
	 * in method invocation parameters.
	 * @param varDecl - declaration from the variable to reference
	 * @return expression to use in method invocation arguments
	 */
	public JCExpression variableToParameter(JCVariableDecl varDecl) {
	  	return make.Ident(varDecl) ;
	}

	/**
	 * Visit a tree and return a list of the declaration of the free variables in it.
	 *
	 * @param tree - the sub-tree to visit
	 * @return list of variables declarations
	 */
	public List<JCIdent> findFreeVariables(JCMethodDecl tree) {
		//get bound vars
		BoundVariableVisitor visitor = new BoundVariableVisitor(tree) ;
		List<JCVariableDecl> boundVars = visitor.getBoundVariables() ;

		System.out.println("the tree:"+tree);
		//get all vars in scope
		List<Element> varsInScope = variablesInScope(tree.body.stats.last()) ;
		//subtract the sets to get free vars
		ListBuffer<Element> freeVars = new ListBuffer<Element>() ;

		for(Element var : varsInScope) {
			boolean isBound = false ;
			for(JCVariableDecl i : boundVars) {
				if(i.name.toString().equals(var.getSimpleName().toString()))
					isBound = true ;
			}

			if(!isBound) {
				if(var.getKind()==ElementKind.FIELD || var.getKind()==ElementKind.LOCAL_VARIABLE) {
//					System.out.println("Free variables found: "+var.getSimpleName().toString()+": "+isBound+" mods:"+var.getModifiers()+" kind:"+var.getKind());
					freeVars.append(var) ;
				}
			}
		}

		//filter again by JCIdents present in the tree
		ListBuffer<JCIdent> freeVarsIds = new ListBuffer<JCIdent>() ;
		List<JCIdent> ids = IdentVisitor.getIdentifiers(tree) ;

		for(Element var : freeVars) {
			for(JCIdent id : ids) {
				if(var.getSimpleName().toString().equals(id.name.toString())) {
//					System.out.println("EQUAL: "+var.getSimpleName()+" "+id.getName());
					freeVarsIds.add(id) ;
				}
//				System.out.println("XXX: "+var.getSimpleName()+" "+id.getName());

			}
		}

//		System.out.println(">>> FOUND "+freeVarsIds.size()+" FREE VARIABLES  IN METHOD "+tree.name.toString()+"...");
		return freeVarsIds.toList() ;
	}

	//TODO javadoc
//	public List<JCIdent> findFreeVariablesInThisMethod() {
//		//find current method in the current state
//		JCMethodDecl methodDecl = state.getParentMethod() ;
//		//find free variables in this method
//		return findFreeVariables(methodDecl) ;
//	}

	//TODO javadoc
	private List<JCVariableDecl> findBoundVariables(JCMethodDecl tree) {
		BoundVariableVisitor visitor = new BoundVariableVisitor(tree) ;
		return visitor.getBoundVariables() ;
	}

	//TODO javadoc
//	public List<JCVariableDecl> findBoundVariablesInThisMethod() {
//		//find current method in the current state
//		JCMethodDecl methodDecl = state.getParentMethod() ;
//		//find free variables in this method
//		return findBoundVariables(methodDecl) ;
//	}

	/**
	 * Travels the scope recursivelly to the outer element until all accessible elements
	 * 	are in the list to return.
	 */
	private List<Element> recursiveScopeAnalysis(JavacScope scp) {
		ListBuffer<Element> l = new ListBuffer<Element>() ;

    	for(Element e : scp.getLocalElements())
    		l.append(e) ;

        //add recursively the rest of the outer scopes
        if(scp.getEnclosingScope()!=null)
        	l.appendList(recursiveScopeAnalysis(scp.getEnclosingScope())) ;
        if(scp.getEnclosingClass()!=null)
        	l.appendList(recursiveScopeAnalysis(scp.getEnclosingClass())) ;
        if(scp.getEnclosingMethod()!=null)
        	l.appendList(recursiveScopeAnalysis(scp.getEnclosingMethod())) ;

     	return l.toList() ;
	}

	/**
	 * Travels the scope recursivelly to the outer element until all accessible elements
	 * 	are in the list to return.
	 */
	private List<Element> recursiveScopeAnalysis(ExecutableElement scp) { //enclosing method
		ListBuffer<Element> l = new ListBuffer<Element>() ;

//     	System.out.println("######################") ;
//     	System.out.println(scp.toString()) ;

    	for(Element e : scp.getEnclosedElements())
    		l.append(e) ;

//        for(Element e : l)
//        	System.out.println("elem in scope:\t"+e+"\t"+e.asType()) ;
//     	System.out.println("######################") ;

     	return l.toList() ;
	}

	/**
	 * Travels the scope recursivelly to the outer element until all accessible elements
	 * 	are in the list to return.
	 */
	private List<Element> recursiveScopeAnalysis(TypeElement scp) { //enclosing class
		ListBuffer<Element> l = new ListBuffer<Element>() ;

    	for(Element e : scp.getEnclosedElements())
    		l.append(e) ;

//    	System.out.println("######################") ;
//    	System.out.println(scp.toString()) ;
//        for(Element e : l)
//        	System.out.println("elem in scope:\t"+e+"\t"+e.asType()) ;
//     	System.out.println("######################") ;

     	return l.toList() ;
	}

	/**
	 * Returns a list of the variables in scope of this tree
	 * @param tree the tree in scope
	 * @return list of elements that are in the scope of this tree
	 */
	public List<Element> variablesInScope(JCTree tree) {

     	JavacTrees treesC = ((JavacTrees)state.trees) ;
//     	try{
	     	JavacScope scp = treesC.getScope(state.trees.getPath(jcc, tree)) ;
	     	//FIXME null pointer when method body is empty!!!!

			return recursiveScopeAnalysis(scp) ;

//     	}catch(NullPointerException e) {
//     		return List.nil();
//     	}
	}
}
