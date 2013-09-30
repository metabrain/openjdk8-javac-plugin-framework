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
import com.sun.tools.javac.tree.TreeScanner;
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
public class TreeScannerStateful extends TreeScanner {

	// offsets of AST nodes in source file
    public final SourcePositions sourcePositions;
    public final Trees trees;
    public final JavacTrees treesC;

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
	public JavacTaskImpl javacTask;

	public TreeScannerStateful(CompilationTask task, JCCompilationUnit jcc) {
    	this.jcc = jcc ;
//        types = task.getTypes();
        trees = Trees.instance(task);
        treesC = (JavacTrees)trees;
                
        //Rabbit hole to Wonderland
        context = ((BasicJavacTask)task).getContext() ;
		this.task = ((BasicJavacTask)task) ;
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
        
        sourcePositions = trees.getSourcePositions();          
        // utility to operate on program elements
        elements = ((JavacTask)task).getElements();
        elementsC = (JavacElements)elements;
 
        // create the type element
        mapType = elements.getTypeElement("java.util.Map").asType();
        objType = elements.getTypeElement("java.lang.Object").asType();

	}

}