package srt.tool;

import java.util.HashSet;

import srt.ast.AssignStmt;
import srt.ast.Decl;
import srt.ast.DeclRef;
import srt.ast.IfStmt;
import srt.ast.Stmt;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.ReverseVisitor;
import srt.util.FunctionUtil;

public class DeadCodeEliminationVisitor extends ReverseVisitor {
	private HashSet<String> live;
	
	public DeadCodeEliminationVisitor() {
		super(true);
		live = new HashSet<String>();
	}

	@Override
	public Object visit(Decl decl) {
		if (!live.contains(decl))
			return null;
		return super.visit(decl);
	}
	
	@Override
	public Object visit(DeclRef declRef) {
		declRef = (DeclRef) super.visit(declRef);
		live.add(declRef.getName());
		return super.visit(declRef);
	}
	
	@Override
	public Object visit(AssignStmt assignStmt) {
		if (!live.contains(assignStmt.getLhs().getName()))
			return null;
			
		live.addAll(assignStmt.getRhs().getUses());
		if (live.contains(assignStmt.getLhs().getName()))
				live.remove(assignStmt.getLhs().getName());
		
		super.visit(assignStmt.getRhs());
		return assignStmt;
	}
	
	@Override
	public Object visit(WhileStmt whileStmt) {
		live.addAll(whileStmt.getUses());
		return super.visit(whileStmt);
	}

	@Override
	public Object visit(IfStmt ifStmt) {
		HashSet<String> ifState, elseState;
		ifState = new HashSet<String>();
		elseState = new HashSet<String>();
		ifState.addAll(live);
		elseState.addAll(live);
		
		live = elseState;
		Stmt elseStmt = (Stmt) super.visit(ifStmt.getElseStmt());
		
		live = ifState;
		Stmt thenStmt = (Stmt) super.visit(ifStmt.getThenStmt());
		
		live = FunctionUtil.getUnion(ifState, elseState);	
		live.addAll(ifStmt.getCondition().getUses());
		// If we haven't modified the AST inside the ifStmt return the original ifStmt 
		if (!hasModified())
			return ifStmt;
		return new IfStmt(ifStmt.getCondition(), thenStmt, elseStmt);
	}
}
