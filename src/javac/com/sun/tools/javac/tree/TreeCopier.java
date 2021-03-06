/**                                                       
 * This file was changed in order to make it compilable   
 * with GWT and to integrate it in the JavaInTheBrowser   
 * project (http://javainthebrowser.appspot.com).         
 *                                                        
 * Date: 2013-05-14                                            
 */                                                       
/*
 * Copyright (c) 2006, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package javac.com.sun.tools.javac.tree;

import javac.com.sun.source.tree.AnnotationTree;
import javac.com.sun.source.tree.ArrayAccessTree;
import javac.com.sun.source.tree.ArrayTypeTree;
import javac.com.sun.source.tree.AssertTree;
import javac.com.sun.source.tree.AssignmentTree;
import javac.com.sun.source.tree.BinaryTree;
import javac.com.sun.source.tree.BlockTree;
import javac.com.sun.source.tree.BreakTree;
import javac.com.sun.source.tree.CaseTree;
import javac.com.sun.source.tree.CatchTree;
import javac.com.sun.source.tree.ClassTree;
import javac.com.sun.source.tree.CompilationUnitTree;
import javac.com.sun.source.tree.CompoundAssignmentTree;
import javac.com.sun.source.tree.ConditionalExpressionTree;
import javac.com.sun.source.tree.ContinueTree;
import javac.com.sun.source.tree.DoWhileLoopTree;
import javac.com.sun.source.tree.EmptyStatementTree;
import javac.com.sun.source.tree.EnhancedForLoopTree;
import javac.com.sun.source.tree.ErroneousTree;
import javac.com.sun.source.tree.ExpressionStatementTree;
import javac.com.sun.source.tree.ForLoopTree;
import javac.com.sun.source.tree.IdentifierTree;
import javac.com.sun.source.tree.IfTree;
import javac.com.sun.source.tree.ImportTree;
import javac.com.sun.source.tree.InstanceOfTree;
import javac.com.sun.source.tree.LabeledStatementTree;
import javac.com.sun.source.tree.LiteralTree;
import javac.com.sun.source.tree.MemberSelectTree;
import javac.com.sun.source.tree.MethodInvocationTree;
import javac.com.sun.source.tree.MethodTree;
import javac.com.sun.source.tree.ModifiersTree;
import javac.com.sun.source.tree.NewArrayTree;
import javac.com.sun.source.tree.NewClassTree;
import javac.com.sun.source.tree.ParameterizedTypeTree;
import javac.com.sun.source.tree.ParenthesizedTree;
import javac.com.sun.source.tree.PrimitiveTypeTree;
import javac.com.sun.source.tree.ReturnTree;
import javac.com.sun.source.tree.SwitchTree;
import javac.com.sun.source.tree.SynchronizedTree;
import javac.com.sun.source.tree.ThrowTree;
import javac.com.sun.source.tree.Tree;
import javac.com.sun.source.tree.TreeVisitor;
import javac.com.sun.source.tree.TryTree;
import javac.com.sun.source.tree.TypeCastTree;
import javac.com.sun.source.tree.TypeParameterTree;
import javac.com.sun.source.tree.UnaryTree;
import javac.com.sun.source.tree.UnionTypeTree;
import javac.com.sun.source.tree.VariableTree;
import javac.com.sun.source.tree.WhileLoopTree;
import javac.com.sun.source.tree.WildcardTree;
import javac.com.sun.tools.javac.tree.JCTree.JCAnnotation;
import javac.com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import javac.com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import javac.com.sun.tools.javac.tree.JCTree.JCAssert;
import javac.com.sun.tools.javac.tree.JCTree.JCAssign;
import javac.com.sun.tools.javac.tree.JCTree.JCAssignOp;
import javac.com.sun.tools.javac.tree.JCTree.JCBinary;
import javac.com.sun.tools.javac.tree.JCTree.JCBlock;
import javac.com.sun.tools.javac.tree.JCTree.JCBreak;
import javac.com.sun.tools.javac.tree.JCTree.JCCase;
import javac.com.sun.tools.javac.tree.JCTree.JCCatch;
import javac.com.sun.tools.javac.tree.JCTree.JCClassDecl;
import javac.com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import javac.com.sun.tools.javac.tree.JCTree.JCConditional;
import javac.com.sun.tools.javac.tree.JCTree.JCContinue;
import javac.com.sun.tools.javac.tree.JCTree.JCDoWhileLoop;
import javac.com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop;
import javac.com.sun.tools.javac.tree.JCTree.JCErroneous;
import javac.com.sun.tools.javac.tree.JCTree.JCExpression;
import javac.com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import javac.com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import javac.com.sun.tools.javac.tree.JCTree.JCForLoop;
import javac.com.sun.tools.javac.tree.JCTree.JCIdent;
import javac.com.sun.tools.javac.tree.JCTree.JCIf;
import javac.com.sun.tools.javac.tree.JCTree.JCImport;
import javac.com.sun.tools.javac.tree.JCTree.JCInstanceOf;
import javac.com.sun.tools.javac.tree.JCTree.JCLabeledStatement;
import javac.com.sun.tools.javac.tree.JCTree.JCLiteral;
import javac.com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import javac.com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import javac.com.sun.tools.javac.tree.JCTree.JCModifiers;
import javac.com.sun.tools.javac.tree.JCTree.JCNewArray;
import javac.com.sun.tools.javac.tree.JCTree.JCNewClass;
import javac.com.sun.tools.javac.tree.JCTree.JCParens;
import javac.com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import javac.com.sun.tools.javac.tree.JCTree.JCReturn;
import javac.com.sun.tools.javac.tree.JCTree.JCSkip;
import javac.com.sun.tools.javac.tree.JCTree.JCStatement;
import javac.com.sun.tools.javac.tree.JCTree.JCSwitch;
import javac.com.sun.tools.javac.tree.JCTree.JCSynchronized;
import javac.com.sun.tools.javac.tree.JCTree.JCThrow;
import javac.com.sun.tools.javac.tree.JCTree.JCTry;
import javac.com.sun.tools.javac.tree.JCTree.JCTypeApply;
import javac.com.sun.tools.javac.tree.JCTree.JCTypeCast;
import javac.com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import javac.com.sun.tools.javac.tree.JCTree.JCTypeUnion;
import javac.com.sun.tools.javac.tree.JCTree.JCUnary;
import javac.com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import javac.com.sun.tools.javac.tree.JCTree.JCWhileLoop;
import javac.com.sun.tools.javac.tree.JCTree.JCWildcard;
import javac.com.sun.tools.javac.tree.JCTree.LetExpr;
import javac.com.sun.tools.javac.tree.JCTree.TypeBoundKind;
import javac.com.sun.tools.javac.util.List;
import javac.com.sun.tools.javac.util.ListBuffer;

/**
 * Creates a copy of a tree, using a given TreeMaker.
 * Names, literal values, etc are shared with the original.
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 */
public class TreeCopier<P> implements TreeVisitor<JCTree,P> {
    private TreeMaker M;

    /** Creates a new instance of TreeCopier */
    public TreeCopier(TreeMaker M) {
        this.M = M;
    }

    public <T extends JCTree> T copy(T tree) {
        return copy(tree, null);
    }

    @SuppressWarnings("unchecked")
    public <T extends JCTree> T copy(T tree, P p) {
        if (tree == null)
            return null;
        return (T) (tree.accept(this, p));
    }

    public <T extends JCTree> List<T> copy(List<T> trees) {
        return copy(trees, null);
    }

    public <T extends JCTree> List<T> copy(List<T> trees, P p) {
        if (trees == null)
            return null;
        ListBuffer<T> lb = new ListBuffer<T>();
        for (T tree: trees)
            lb.append(copy(tree, p));
        return lb.toList();
    }

    public JCTree visitAnnotation(AnnotationTree node, P p) {
        JCAnnotation t = (JCAnnotation) node;
        JCTree annotationType = copy(t.annotationType, p);
        List<JCExpression> args = copy(t.args, p);
        return M.at(t.pos).Annotation(annotationType, args);
    }

    public JCTree visitAssert(AssertTree node, P p) {
        JCAssert t = (JCAssert) node;
        JCExpression cond = copy(t.cond, p);
        JCExpression detail = copy(t.detail, p);
        return M.at(t.pos).Assert(cond, detail);
    }

    public JCTree visitAssignment(AssignmentTree node, P p) {
        JCAssign t = (JCAssign) node;
        JCExpression lhs = copy(t.lhs, p);
        JCExpression rhs = copy(t.rhs, p);
        return M.at(t.pos).Assign(lhs, rhs);
    }

    public JCTree visitCompoundAssignment(CompoundAssignmentTree node, P p) {
        JCAssignOp t = (JCAssignOp) node;
        JCTree lhs = copy(t.lhs, p);
        JCTree rhs = copy(t.rhs, p);
        return M.at(t.pos).Assignop(t.getTag(), lhs, rhs);
    }

    public JCTree visitBinary(BinaryTree node, P p) {
        JCBinary t = (JCBinary) node;
        JCExpression lhs = copy(t.lhs, p);
        JCExpression rhs = copy(t.rhs, p);
        return M.at(t.pos).Binary(t.getTag(), lhs, rhs);
    }

    public JCTree visitBlock(BlockTree node, P p) {
        JCBlock t = (JCBlock) node;
        List<JCStatement> stats = copy(t.stats, p);
        return M.at(t.pos).Block(t.flags, stats);
    }

    public JCTree visitBreak(BreakTree node, P p) {
        JCBreak t = (JCBreak) node;
        return M.at(t.pos).Break(t.label);
    }

    public JCTree visitCase(CaseTree node, P p) {
        JCCase t = (JCCase) node;
        JCExpression pat = copy(t.pat, p);
        List<JCStatement> stats = copy(t.stats, p);
        return M.at(t.pos).Case(pat, stats);
    }

    public JCTree visitCatch(CatchTree node, P p) {
        JCCatch t = (JCCatch) node;
        JCVariableDecl param = copy(t.param, p);
        JCBlock body = copy(t.body, p);
        return M.at(t.pos).Catch(param, body);
    }

    public JCTree visitClass(ClassTree node, P p) {
        JCClassDecl t = (JCClassDecl) node;
        JCModifiers mods = copy(t.mods, p);
        List<JCTypeParameter> typarams = copy(t.typarams, p);
        JCExpression extending = copy(t.extending, p);
        List<JCExpression> implementing = copy(t.implementing, p);
        List<JCTree> defs = copy(t.defs, p);
        return M.at(t.pos).ClassDef(mods, t.name, typarams, extending, implementing, defs);
    }

    public JCTree visitConditionalExpression(ConditionalExpressionTree node, P p) {
        JCConditional t = (JCConditional) node;
        JCExpression cond = copy(t.cond, p);
        JCExpression truepart = copy(t.truepart, p);
        JCExpression falsepart = copy(t.falsepart, p);
        return M.at(t.pos).Conditional(cond, truepart, falsepart);
    }

    public JCTree visitContinue(ContinueTree node, P p) {
        JCContinue t = (JCContinue) node;
        return M.at(t.pos).Continue(t.label);
    }

    public JCTree visitDoWhileLoop(DoWhileLoopTree node, P p) {
        JCDoWhileLoop t = (JCDoWhileLoop) node;
        JCStatement body = copy(t.body, p);
        JCExpression cond = copy(t.cond, p);
        return M.at(t.pos).DoLoop(body, cond);
    }

    public JCTree visitErroneous(ErroneousTree node, P p) {
        JCErroneous t = (JCErroneous) node;
        List<? extends JCTree> errs = copy(t.errs, p);
        return M.at(t.pos).Erroneous(errs);
    }

    public JCTree visitExpressionStatement(ExpressionStatementTree node, P p) {
        JCExpressionStatement t = (JCExpressionStatement) node;
        JCExpression expr = copy(t.expr, p);
        return M.at(t.pos).Exec(expr);
    }

    public JCTree visitEnhancedForLoop(EnhancedForLoopTree node, P p) {
        JCEnhancedForLoop t = (JCEnhancedForLoop) node;
        JCVariableDecl var = copy(t.var, p);
        JCExpression expr = copy(t.expr, p);
        JCStatement body = copy(t.body, p);
        return M.at(t.pos).ForeachLoop(var, expr, body);
    }

    public JCTree visitForLoop(ForLoopTree node, P p) {
        JCForLoop t = (JCForLoop) node;
        List<JCStatement> init = copy(t.init, p);
        JCExpression cond = copy(t.cond, p);
        List<JCExpressionStatement> step = copy(t.step, p);
        JCStatement body = copy(t.body, p);
        return M.at(t.pos).ForLoop(init, cond, step, body);
    }

    public JCTree visitIdentifier(IdentifierTree node, P p) {
        JCIdent t = (JCIdent) node;
        return M.at(t.pos).Ident(t.name);
    }

    public JCTree visitIf(IfTree node, P p) {
        JCIf t = (JCIf) node;
        JCExpression cond = copy(t.cond, p);
        JCStatement thenpart = copy(t.thenpart, p);
        JCStatement elsepart = copy(t.elsepart, p);
        return M.at(t.pos).If(cond, thenpart, elsepart);
    }

    public JCTree visitImport(ImportTree node, P p) {
        JCImport t = (JCImport) node;
        JCTree qualid = copy(t.qualid, p);
        return M.at(t.pos).Import(qualid, t.staticImport);
    }

    public JCTree visitArrayAccess(ArrayAccessTree node, P p) {
        JCArrayAccess t = (JCArrayAccess) node;
        JCExpression indexed = copy(t.indexed, p);
        JCExpression index = copy(t.index, p);
        return M.at(t.pos).Indexed(indexed, index);
    }

    public JCTree visitLabeledStatement(LabeledStatementTree node, P p) {
        JCLabeledStatement t = (JCLabeledStatement) node;
        copy(t.body, p);
        return M.at(t.pos).Labelled(t.label, t.body);
    }

    public JCTree visitLiteral(LiteralTree node, P p) {
        JCLiteral t = (JCLiteral) node;
        return M.at(t.pos).Literal(t.typetag, t.value);
    }

    public JCTree visitMethod(MethodTree node, P p) {
        JCMethodDecl t  = (JCMethodDecl) node;
        JCModifiers mods = copy(t.mods, p);
        JCExpression restype = copy(t.restype, p);
        List<JCTypeParameter> typarams = copy(t.typarams, p);
        List<JCVariableDecl> params = copy(t.params, p);
        List<JCExpression> thrown = copy(t.thrown, p);
        JCBlock body = copy(t.body, p);
        JCExpression defaultValue = copy(t.defaultValue, p);
        return M.at(t.pos).MethodDef(mods, t.name, restype, typarams, params, thrown, body, defaultValue);
    }

    public JCTree visitMethodInvocation(MethodInvocationTree node, P p) {
        JCMethodInvocation t = (JCMethodInvocation) node;
        List<JCExpression> typeargs = copy(t.typeargs, p);
        JCExpression meth = copy(t.meth, p);
        List<JCExpression> args = copy(t.args, p);
        return M.at(t.pos).Apply(typeargs, meth, args);
    }

    public JCTree visitModifiers(ModifiersTree node, P p) {
        JCModifiers t = (JCModifiers) node;
        List<JCAnnotation> annotations = copy(t.annotations, p);
        return M.at(t.pos).Modifiers(t.flags, annotations);
    }

    public JCTree visitNewArray(NewArrayTree node, P p) {
        JCNewArray t = (JCNewArray) node;
        JCExpression elemtype = copy(t.elemtype, p);
        List<JCExpression> dims = copy(t.dims, p);
        List<JCExpression> elems = copy(t.elems, p);
        return M.at(t.pos).NewArray(elemtype, dims, elems);
    }

    public JCTree visitNewClass(NewClassTree node, P p) {
        JCNewClass t = (JCNewClass) node;
        JCExpression encl = copy(t.encl, p);
        List<JCExpression> typeargs = copy(t.typeargs, p);
        JCExpression clazz = copy(t.clazz, p);
        List<JCExpression> args = copy(t.args, p);
        JCClassDecl def = copy(t.def, p);
        return M.at(t.pos).NewClass(encl, typeargs, clazz, args, def);
    }

    public JCTree visitParenthesized(ParenthesizedTree node, P p) {
        JCParens t = (JCParens) node;
        JCExpression expr = copy(t.expr, p);
        return M.at(t.pos).Parens(expr);
    }

    public JCTree visitReturn(ReturnTree node, P p) {
        JCReturn t = (JCReturn) node;
        JCExpression expr = copy(t.expr, p);
        return M.at(t.pos).Return(expr);
    }

    public JCTree visitMemberSelect(MemberSelectTree node, P p) {
        JCFieldAccess t = (JCFieldAccess) node;
        JCExpression selected = copy(t.selected, p);
        return M.at(t.pos).Select(selected, t.name);
    }

    public JCTree visitEmptyStatement(EmptyStatementTree node, P p) {
        JCSkip t = (JCSkip) node;
        return M.at(t.pos).Skip();
    }

    public JCTree visitSwitch(SwitchTree node, P p) {
        JCSwitch t = (JCSwitch) node;
        JCExpression selector = copy(t.selector, p);
        List<JCCase> cases = copy(t.cases, p);
        return M.at(t.pos).Switch(selector, cases);
    }

    public JCTree visitSynchronized(SynchronizedTree node, P p) {
        JCSynchronized t = (JCSynchronized) node;
        JCExpression lock = copy(t.lock, p);
        JCBlock body = copy(t.body, p);
        return M.at(t.pos).Synchronized(lock, body);
    }

    public JCTree visitThrow(ThrowTree node, P p) {
        JCThrow t = (JCThrow) node;
        JCTree expr = copy(t.expr, p);
        return M.at(t.pos).Throw(expr);
    }

    public JCTree visitCompilationUnit(CompilationUnitTree node, P p) {
        JCCompilationUnit t = (JCCompilationUnit) node;
        List<JCAnnotation> packageAnnotations = copy(t.packageAnnotations, p);
        JCExpression pid = copy(t.pid, p);
        List<JCTree> defs = copy(t.defs, p);
        return M.at(t.pos).TopLevel(packageAnnotations, pid, defs);
    }

    public JCTree visitTry(TryTree node, P p) {
        JCTry t = (JCTry) node;
        List<JCTree> resources = copy(t.resources, p);
        JCBlock body = copy(t.body, p);
        List<JCCatch> catchers = copy(t.catchers, p);
        JCBlock finalizer = copy(t.finalizer, p);
        return M.at(t.pos).Try(resources, body, catchers, finalizer);
    }

    public JCTree visitParameterizedType(ParameterizedTypeTree node, P p) {
        JCTypeApply t = (JCTypeApply) node;
        JCExpression clazz = copy(t.clazz, p);
        List<JCExpression> arguments = copy(t.arguments, p);
        return M.at(t.pos).TypeApply(clazz, arguments);
    }

    public JCTree visitUnionType(UnionTypeTree node, P p) {
        JCTypeUnion t = (JCTypeUnion) node;
        List<JCExpression> components = copy(t.alternatives, p);
        return M.at(t.pos).TypeUnion(components);
    }

    public JCTree visitArrayType(ArrayTypeTree node, P p) {
        JCArrayTypeTree t = (JCArrayTypeTree) node;
        JCExpression elemtype = copy(t.elemtype, p);
        return M.at(t.pos).TypeArray(elemtype);
    }

    public JCTree visitTypeCast(TypeCastTree node, P p) {
        JCTypeCast t = (JCTypeCast) node;
        JCTree clazz = copy(t.clazz, p);
        JCExpression expr = copy(t.expr, p);
        return M.at(t.pos).TypeCast(clazz, expr);
    }

    public JCTree visitPrimitiveType(PrimitiveTypeTree node, P p) {
        JCPrimitiveTypeTree t = (JCPrimitiveTypeTree) node;
        return M.at(t.pos).TypeIdent(t.typetag);
    }

    public JCTree visitTypeParameter(TypeParameterTree node, P p) {
        JCTypeParameter t = (JCTypeParameter) node;
        List<JCExpression> bounds = copy(t.bounds, p);
        return M.at(t.pos).TypeParameter(t.name, bounds);
    }

    public JCTree visitInstanceOf(InstanceOfTree node, P p) {
        JCInstanceOf t = (JCInstanceOf) node;
        JCExpression expr = copy(t.expr, p);
        JCTree clazz = copy(t.clazz, p);
        return M.at(t.pos).TypeTest(expr, clazz);
    }

    public JCTree visitUnary(UnaryTree node, P p) {
        JCUnary t = (JCUnary) node;
        JCExpression arg = copy(t.arg, p);
        return M.at(t.pos).Unary(t.getTag(), arg);
    }

    public JCTree visitVariable(VariableTree node, P p) {
        JCVariableDecl t = (JCVariableDecl) node;
        JCModifiers mods = copy(t.mods, p);
        JCExpression vartype = copy(t.vartype, p);
        JCExpression init = copy(t.init, p);
        return M.at(t.pos).VarDef(mods, t.name, vartype, init);
    }

    public JCTree visitWhileLoop(WhileLoopTree node, P p) {
        JCWhileLoop t = (JCWhileLoop) node;
        JCStatement body = copy(t.body, p);
        JCExpression cond = copy(t.cond, p);
        return M.at(t.pos).WhileLoop(cond, body);
    }

    public JCTree visitWildcard(WildcardTree node, P p) {
        JCWildcard t = (JCWildcard) node;
        TypeBoundKind kind = M.at(t.kind.pos).TypeBoundKind(t.kind.kind);
        JCTree inner = copy(t.inner, p);
        return M.at(t.pos).Wildcard(kind, inner);
    }

    public JCTree visitOther(Tree node, P p) {
        JCTree tree = (JCTree) node;
        switch (tree.getTag()) {
            case JCTree.LETEXPR: {
                LetExpr t = (LetExpr) node;
                List<JCVariableDecl> defs = copy(t.defs, p);
                JCTree expr = copy(t.expr, p);
                return M.at(t.pos).LetExpr(defs, expr);
            }
            default:
                throw new AssertionError("unknown tree tag: " + tree.getTag());
        }
    }

}
