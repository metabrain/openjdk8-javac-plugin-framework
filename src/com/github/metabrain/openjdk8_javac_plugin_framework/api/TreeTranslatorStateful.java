package com.github.metabrain.openjdk8_javac_plugin_framework.api;

import com.sun.source.util.JavacTask;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.MemberEnter;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.JavaCompiler.CompilationTask;

/**
 * A subclass of TreeTranslator from the Javac internal classes used to,
 * along with many other uses, remove the parametric types from Java and
 * produce plain Java in the later stages of compilation.
 * <p>
 * However, TreeTranslator by itself is pure, i.e. does not keep track of
 * the place in the tree it is currently translating, which can bring some
 * difficulties. This classes fixes that by providing methods to know
 * the current position on the tree and respective parent nodes.
 * <p>
 * When implementing this class, *ALWAYS* call super.method() before doing
 * any operation. This should be enforced in order to assure a consistent
 * behaviour of the methods in this class.
 * <p>
 * After doing the desired operations, the tree received in the parameter
 * has to be attributed to the "result" variable in TreeTranslator. In
 * short, in the end of an implemented method one should always do :
 * <p>
 * {@code result = tree ;}
 *
 * @author MetaBrain
 *
 */
public class TreeTranslatorStateful extends TreeTranslator {

	//Singletons to manipulate AST nodes
    public AST_Manipulator manipulator ;

	// offsets of AST nodes in source file
    public final SourcePositions sourcePositions;
    public final Trees trees;
    public final JavacTrees treesC;
	public JavacTaskImpl javacTask;

    // utility to operate on types
//    protected final Types types;

    public final TypeMirror mapType ;
    public final TypeMirror objType ;

    public final Context context ;
    public final Attr attr;
    public final Symtab syms ;
    public final Types types ;
    public final JavacTypes typesC ;
    public final TreeMaker make ;
    public final Enter enter ;
    public final Names names ;
    public final Elements elements ;
    public final JavacElements elementsC ;
    public final JavacElements javacElements ;
	public final Resolve resolve ;
	public final MemberEnter memberEnter ;
	public final TreeInfo info;
	public final JCCompilationUnit jcc ;
	//public final JavacTaskImpl taskImpl;
	public final BasicJavacTask task;

	public TreeTranslatorStateful(CompilationTask task, JCCompilationUnit jcc) {
    	this.jcc = jcc ;
//        types = task.getTypes();
        trees = Trees.instance(task);
        treesC = (JavacTrees)trees;

        //Rabbit hole to Wonderland
        context = ((BasicJavacTask)task).getContext() ;
		this.task = ((BasicJavacTask)task) ;
		//taskImpl = ((JavacTaskImpl)task) ;

		if (context.get(JavacTask.class) instanceof JavacTaskImpl)
			javacTask = (JavacTaskImpl) (context.get(JavacTask.class));

        //Initialize magical classes !
        make = TreeMaker.instance(context) ;
        names = Names.instance(context) ;
        syms = Symtab.instance(context);
        types = Types.instance(context) ;
        typesC = JavacTypes.instance(context) ;
        attr = Attr.instance(context) ;
        enter = Enter.instance(context) ;
        resolve = Resolve.instance(context) ;
        memberEnter = MemberEnter.instance(context) ;
		javacElements = JavacElements.instance(context) ;
		info = TreeInfo.instance(context) ;

        //Create AST Manipulator object
        manipulator = new AST_Manipulator(context, jcc, this) ;

        sourcePositions = trees.getSourcePositions();
        // utility to operate on program elements
        elements = ((JavacTask)task).getElements();
        elementsC = (JavacElements)elements;

        // create the type element
        mapType = elements.getTypeElement("java.util.Map").asType();
        objType = elements.getTypeElement("java.lang.Object").asType();

		//Initialize the various stacks
//		visitStack = new MyStack<JCTree>() ;
//		requestStack = new LinkedList<Request>() ;
//		boundVariablesStack = new MyStack<List<JCVariableDecl>>() ;
//		freeVariablesStack = new MyStack<List<JCIdent>>() ;
	}

//	/** A subclass of Tree.Visitor, this class defines
//	 *  a general tree translator pattern. Translation proceeds recursively in
//	 *  left-to-right order down a tree, constructing translated nodes by
//	 *  overwriting existing ones. There is one visitor method in this class
//	 *  for every possible kind of tree node.  To obtain a specific
//	 *  translator, it suffices to override those visitor methods which
//	 *  do some interesting work. The translator class itself takes care of all
//	 *  navigational aspects.
//	 *
//	 *  <p><b>This is NOT part of any supported API.
//	 *  If you write code that depends on this, you do so at your own risk.
//	 *  This code and its internal interfaces are subject to change or
//	 *  deletion without notice.</b>
//	 */
//
//	/**
//	 * Travelling methods
//	 */
//	//Queue with parents of visitor nodes
//	public MyStack<JCTree> visitStack ;
//
//	//Nearest parent
//	public JCTree getParent() {
//		return visitStack.element();
//	}
//
//	//Nearest method declaration
//	public JCMethodDecl getParentMethod() {
////    	System.out.println("visitStack.size() "+visitStack.size()+"\t");//+visitStack.peek() );
////    	System.out.println("getParents().size(): "+getParents().size());
//		for(JCTree t : getParents()) {
////			System.out.println(
////					"CUR -------> "+t
////					+"\tinstanceof JCMethodDecl?"+(t instanceof JCMethodDecl)
////					+"\tclass is:"+(t.getClass()));
//			if(t instanceof JCMethodDecl)
//				return (JCMethodDecl)t ;
//		}
//
//		//Check if current tree is the method
//		if(result instanceof JCMethodDecl)
//			return (JCMethodDecl)result;
//
//		//Didnt find enclosing, exit with error
//		throw new EnclosingMethodNotFound() ;
//	}
//
//	//Parents, from closest to farther paren
//	public MyStack<JCTree> getParents() {
//		return visitStack.clone().reverse() ;
//	}
//
//	/**
//	 * Make request to change node
//	 */
//	//Queue with requests
//	public java.util.List<Request> requestStack ;
//
//
//	//Add a request after the calling node in the AST
//	public void addRequestAfterMe(JCTree currentNode, List<JCTree> treesToAdd) {
//		//Finds enclosing parent method
//		JCMethodDecl parentMethod = null ;
//		parentMethod = getParentMethod() ;
//
//    	int positionToAdd = 0 ;
//    	//go to parent and find my position
//    	for(JCStatement stat : parentMethod.body.stats) {
//    		//has at least one annotation
//    		if(stat instanceof JCVariableDecl && ((JCVariableDecl)stat).mods.annotations.head!=null)
//    		{
////    			System.out.println("---->current mods: "+((JCVariableDecl)stat).mods.annotations.head.getKind());
////    			System.out.println("---->this annotation: "+currentNode.getKind());
//    			if(((JCVariableDecl)stat).mods.annotations.head.equals(currentNode))  {
//    				System.out.println("JACKPOT!!!");
//    				break ;
//    			}
//    			positionToAdd++ ;
//    		}
//    	}
//
//    	addRequest(new RequestAdd(currentNode, parentMethod, treesToAdd, positionToAdd)) ;
//	}
//
//	//Add a request before the calling node in the AST
//	public void addRequestBeforeMe(Request r) {
//		//TODO THIS
//		throw new NotImplementedException() ;
//	}
//
//	//Add a request
//	private void addRequest(Request r) {
//		requestStack.add(r) ;
//     	System.out.println("added request to queue!");
//	}
//
//	//Check if current node has outstanding request
//	public boolean hasRequests(JCTree tree) {
//		return getRequests(tree).size() > 0 ;
//	}
//
//	//Get pending requests for this node
//	public java.util.List<Request> getRequests(JCTree tree) {
//		java.util.List<Request> requests = new ArrayList<Request>() ;
//		for(Request r : requestStack) {
//			if(r.getTarget().equals(tree)) //TODO ver se o equals funciona correctamente
//				requests.add(r) ;
//		}
//		return requests ;
//	}
//
//	/**********************************************************************
//	 * Auxiliar methods
//	 *********************************************************************/
//
//	private void addVisited(JCTree tree) {
//		visitStack.add((JCTree)tree.getTree()) ;
//
//		if(tree!=visitStack.element()) {
//			System.err.println("Adding to the queue is not working correctly!") ;
//			System.exit(1) ;
//		}
//	}
//
//	private void pollVisited() {
//		visitStack.remove() ;
//	}
//
//
//	/* ***************************************************************************
//	 * Keeps track of bound variables when we enter a new method
//	 ****************************************************************************/
//
//	//Stack with the free variables of each method entered
//	private MyStack<List<JCVariableDecl>> boundVariablesStack ;
//
//	private void addBoundVariable(JCTree tree) {
//		List<JCVariableDecl> boundVars = manipulator.findBoundVariablesInThisMethod() ;
//		boundVariablesStack.add(boundVars) ;
//	}
//
//	private void pollBoundVariables() {
//		boundVariablesStack.remove();
//	}
//
//	/* ***************************************************************************
//	 * Keeps track of bound variables when we enter a new method
//	 ****************************************************************************/
//
//	//Stack with the free variables of each method entered
//	private MyStack<List<JCIdent>> freeVariablesStack ;
//
//	private void addFreeVariable(JCTree tree) {
//		List<JCIdent> freeVars = manipulator.findFreeVariablesInThisMethod() ;
//		freeVariablesStack.add(freeVars) ;
//	}
//
//	private void pollFreeVariables() {
//		freeVariablesStack.remove();
//	}
//
//	/* ***************************************************************************
//	 * Visitor methods from TreeTranslator
//	 ****************************************************************************/
//
//	@Override
//	public void visitTopLevel(JCCompilationUnit tree) {
//		addVisited(tree) ;
//		super.visitTopLevel(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitImport(JCImport tree) {
//		addVisited(tree) ;
//		super.visitImport(tree) ;
//		pollVisited() ;
//	}
//
//	//TODO encapsular tambem em metodos/classe
//	private java.util.List<JCMethodDecl> currentClassMethods = null ;
//
//	public java.util.List<JCMethodDecl> getCurrentClassMethods() {
//		return currentClassMethods ;
//	}
//
//	@Override
//	public void visitClassDef(JCClassDecl tree) {
//		addVisited(tree) ;
//
//		//FIXME meter num metodo a' parte
//		//Update my internal method sumbol table with methods declared by this class
//		currentClassMethods = new LinkedList<JCMethodDecl>() ;
//    	for(JCTree t : tree.defs) {
//    		if(t.hasTag(JCTree.Tag.METHODDEF)) {
//				currentClassMethods.add((JCMethodDecl)t) ;
////        		System.out.println(
////        				"\n--------------\n\nDefinition found for element ---->"+t.getTag()+
////        				"\n---->"+t +
////        				"\n---->"+t.getClass()
////        		);
//    		}
//    	}
//
//		super.visitClassDef(tree) ;
////		pollVisited() ;
//	}
//
//	@Override
//	public void visitMethodDef(JCMethodDecl tree) {
//		//Add to visited and calculate free variables
//		addVisited(tree) ;
////		addBoundVariable(tree) ;
////		addFreeVariable(tree) ;
//		super.visitMethodDef(tree) ;
//
//		//After going inside the method, check if we have a request to add
//    	if(hasRequests(tree)) {
//    		System.out.println("has request!");
//    		Request r = getRequests(tree).get(0) ;
//
//    		if(r instanceof RequestAdd) {
//    			RequestAdd rAdd = (RequestAdd)r;
////            	System.out.println("SIZE BEFORE:"+tree.body.stats.size());
//        		ListBuffer<JCStatement> newList = new ListBuffer<JCTree.JCStatement>() ;
//
//             	int i = 0 ;
//             	//re-appends the old trees until the insertion-point
//             	for(i = 0 ; i<rAdd.position ; i++)
//             		newList.append(tree.body.stats.get(i)) ;
//             	//adds the trees to add at the insertion-point
//             	for(JCTree t : rAdd.treesToAdd)
//             		newList.append((JCStatement) t) ;
//             	//re-appends the old trees afteruntil the insertion-point
//             	for(i = rAdd.position ; i<tree.body.stats.size() ; i++)
//             		newList.append(tree.body.stats.get(i)) ;
//
//             	tree.body.stats = newList.toList() ;
////            	System.out.println("SIZE AFTER:"+tree.body.stats.size());
//    		}
//    	}
//
//    	//Pop data from the various stacks
////		pollFreeVariables() ;
////		pollBoundVariables() ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitVarDef(JCVariableDecl tree) {
//		addVisited(tree) ;
//		super.visitVarDef(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitSkip(JCSkip tree) {
//		addVisited(tree) ;
//		super.visitSkip(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitBlock(JCBlock tree) {
//		addVisited(tree) ;
//		super.visitBlock(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitDoLoop(JCDoWhileLoop tree) {
//		addVisited(tree) ;
//		super.visitDoLoop(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitWhileLoop(JCWhileLoop tree) {
//		addVisited(tree) ;
//		super.visitWhileLoop(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitForLoop(JCForLoop tree) {
//		addVisited(tree) ;
//		super.visitForLoop(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitForeachLoop(JCEnhancedForLoop tree) {
//		addVisited(tree) ;
//		super.visitForeachLoop(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitLabelled(JCLabeledStatement tree) {
//		addVisited(tree) ;
//		super.visitLabelled(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitSwitch(JCSwitch tree) {
//		addVisited(tree) ;
//		super.visitSwitch(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitCase(JCCase tree) {
//		addVisited(tree) ;
//		super.visitCase(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitSynchronized(JCSynchronized tree) {
//		addVisited(tree) ;
//		super.visitSynchronized(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitTry(JCTry tree) {
//		addVisited(tree) ;
//		super.visitTry(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitCatch(JCCatch tree) {
//		addVisited(tree) ;
//		super.visitCatch(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitConditional(JCConditional tree) {
//		addVisited(tree) ;
//		super.visitConditional(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitIf(JCIf tree) {
//		addVisited(tree) ;
//		super.visitIf(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitExec(JCExpressionStatement tree) {
//		addVisited(tree) ;
//		super.visitExec(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitBreak(JCBreak tree) {
//		addVisited(tree) ;
//		super.visitBreak(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitContinue(JCContinue tree) {
//		addVisited(tree) ;
//		super.visitContinue(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitReturn(JCReturn tree) {
//		addVisited(tree) ;
//		super.visitReturn(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitThrow(JCThrow tree) {
//		addVisited(tree) ;
//		super.visitThrow(tree) ;
//		pollVisited() ;
//	}
//	@Override
//
//	public void visitAssert(JCAssert tree) {
//		addVisited(tree) ;
//		super.visitAssert(tree) ;
//		pollVisited() ;
//	}
//	@Override
//
//	public void visitApply(JCMethodInvocation tree) {
//		addVisited(tree) ;
//		super.visitApply(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitNewClass(JCNewClass tree) {
//		addVisited(tree) ;
//		super.visitNewClass(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitLambda(JCLambda tree) {
//		addVisited(tree) ;
//		super.visitLambda(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitNewArray(JCNewArray tree) {
//		addVisited(tree) ;
//		super.visitNewArray(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitParens(JCParens tree) {
//		addVisited(tree) ;
//		super.visitParens(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitAssign(JCAssign tree) {
//		addVisited(tree) ;
//		super.visitAssign(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitAssignop(JCAssignOp tree) {
//		addVisited(tree) ;
//		super.visitAssignop(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitUnary(JCUnary tree) {
//		addVisited(tree) ;
//		super.visitUnary(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitBinary(JCBinary tree) {
//		addVisited(tree) ;
//		super.visitBinary(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitTypeCast(JCTypeCast tree) {
//		addVisited(tree) ;
//		super.visitTypeCast(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitTypeTest(JCInstanceOf tree) {
//		addVisited(tree) ;
//		super.visitTypeTest(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitIndexed(JCArrayAccess tree) {
//		addVisited(tree) ;
//		super.visitIndexed(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitSelect(JCFieldAccess tree) {
//		addVisited(tree) ;
//		super.visitSelect(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitReference(JCMemberReference tree) {
//		addVisited(tree) ;
//		super.visitReference(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitIdent(JCIdent tree) {
//		addVisited(tree) ;
//		super.visitIdent(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitLiteral(JCLiteral tree) {
//		addVisited(tree) ;
//		super.visitLiteral(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitTypeIdent(JCPrimitiveTypeTree tree) {
//		addVisited(tree) ;
//		super.visitTypeIdent(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitTypeArray(JCArrayTypeTree tree) {
//		addVisited(tree) ;
//		super.visitTypeArray(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitTypeApply(JCTypeApply tree) {
//		addVisited(tree) ;
//		super.visitTypeApply(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitTypeUnion(JCTypeUnion tree) {
//		addVisited(tree) ;
//		super.visitTypeUnion(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitTypeParameter(JCTypeParameter tree) {
//		addVisited(tree) ;
//		super.visitTypeParameter(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitWildcard(JCWildcard tree) {
//		addVisited(tree) ;
//		super.visitWildcard(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitTypeBoundKind(TypeBoundKind tree) {
//		addVisited(tree) ;
//		super.visitTypeBoundKind(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitErroneous(JCErroneous tree) {
//		addVisited(tree) ;
//		super.visitErroneous(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitLetExpr(LetExpr tree) {
//		addVisited(tree) ;
//		super.visitLetExpr(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitModifiers(JCModifiers tree) {
//		addVisited(tree) ;
//		super.visitModifiers(tree) ;
//		pollVisited() ;
//	}
//
//	@Override
//	public void visitAnnotation(JCAnnotation tree) {
//		addVisited(tree) ;
//		super.visitAnnotation(tree) ;
//		pollVisited() ;
//	}
}