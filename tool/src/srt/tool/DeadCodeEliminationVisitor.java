package srt.tool;

import java.util.HashSet;

import srt.ast.AssignStmt;
import srt.ast.Decl;
import srt.ast.DeclRef;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.ReverseVisitor;

public class DeadCodeEliminationVisitor extends ReverseVisitor {
	private HashSet<String> state;
	
	public DeadCodeEliminationVisitor() {
		super(true);
		state = new HashSet<String>();
	}

	@Override
	public Object visit(Decl decl) {
		if (!state.contains(decl))
			return null;
		return super.visit(decl);
	}
	
	@Override
	public Object visit(DeclRef declRef) {
		declRef = (DeclRef) super.visit(declRef);
		state.add(declRef.getName());
		return super.visit(declRef);
	}
	
	@Override
	public Object visit(AssignStmt assignStmt) {
		if (!state.contains(assignStmt.getLhs().getName()))
			return null;
			
		state.addAll(assignStmt.getRhs().getUses());
		if (state.contains(assignStmt.getLhs().getName()))
				state.remove(assignStmt.getLhs().getName());
		
		super.visit(assignStmt.getRhs());
		return assignStmt;
	}
	
	@Override
	public Object visit(WhileStmt whileStmt) {
		state.addAll(whileStmt.getUses());
		return super.visit(whileStmt);
	}

}
