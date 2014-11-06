package srt.tool;

import java.util.HashMap;

import srt.ast.AssignStmt;
import srt.ast.Decl;
import srt.ast.DeclRef;
import srt.ast.Expr;
import srt.ast.visitor.impl.DefaultVisitor;

public class SSAVisitor extends DefaultVisitor {
	private static HashMap<String, Integer> varsIndex = new HashMap<String, Integer>();
	
	public SSAVisitor() {
		super(true);
	}

	private int getVarIndex(String varName) {
		if (varsIndex.containsKey((varName)))
			return varsIndex.get(varName);
		return 0;
	}
	
	@Override
	public Object visit(Decl decl) {
		return super.visit(decl);
	}

	@Override
	public Object visit(DeclRef declRef) {
		// If the variable was rename before use the new name
		if (getVarIndex(declRef.getName()) != 0) {
			DeclRef newDeclRef = new DeclRef(declRef.getName(), getVarIndex(declRef.getName()));
			return super.visit(newDeclRef);
		}
		return super.visit(declRef);
	}

	@Override
	public Object visit(AssignStmt assignment) {
		if (assignment.getRhs() instanceof srt.ast.BinaryExpr) {
			// Visit LHS of assignment before renaming
			Expr assignRhs = (Expr) super.visit(assignment.getRhs());
			
			// Rename i$x as i$(x + 1) from this point onwards
			int index = assignment.getLhs().getIndex() + 1;
			varsIndex.put(assignment.getLhs().getName(), index);
			
			// Add a new declaration for i$(x + 1) and visit it
			DeclRef declRef = new DeclRef(assignment.getLhs().getName(), index);
			declRef = (DeclRef) super.visit(declRef);
			
			// Create new assignment to replace current node
			AssignStmt assign = new AssignStmt(declRef, assignRhs);
			return (Object) assign;
		}
		return super.visit(assignment);
	}
}
