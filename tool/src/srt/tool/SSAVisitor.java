package srt.tool;

import java.util.HashMap;

import srt.ast.AssignStmt;
import srt.ast.BinaryExpr;
import srt.ast.Decl;
import srt.ast.DeclRef;
import srt.ast.Expr;
import srt.ast.IfStmt;
import srt.ast.TernaryExpr;
import srt.ast.UnaryExpr;
import srt.ast.visitor.impl.DefaultVisitor;

public class SSAVisitor extends DefaultVisitor {
	private HashMap<String, Integer> varsIndex;
	private HashMap<String, Boolean> varsSeen;
	
	public SSAVisitor() {
		super(true);
		varsIndex = new HashMap<String, Integer>();
		varsSeen = new HashMap<String, Boolean>();
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
		String varName = assignment.getLhs().getName();
		if ( findVarRef(varName, assignment.getRhs()) || varsSeen.containsKey(varName)) {
			// Mark the variable as seen
			varsSeen.put(varName, true);
			
			// Visit LHS of assignment before renaming
			Expr assignRhs = (Expr) super.visit(assignment.getRhs());
			
			// Rename i$x as i$(x + 1) from this point onwards
			int index = getVarIndex(assignment.getLhs().getName()) + 1;
			varsIndex.put(assignment.getLhs().getName(), index);
			
			// Add a new declaration for i$(x + 1) and visit it
			DeclRef declRef = new DeclRef(assignment.getLhs().getName(), index);
			declRef = (DeclRef) super.visit(declRef);
			
			// Mark the new variable as seen
			varsSeen.put(declRef.getName(), true);
			
			// Create new assignment to replace current node
			AssignStmt assign = new AssignStmt(declRef, assignRhs);
			return (Object) assign;
		}
		// Mark the variable as seen
		varsSeen.put(varName, true);
		return super.visit(assignment);
	}
	
	private boolean findVarRef(String name, Expr expr) {
		if (expr instanceof BinaryExpr) {
			BinaryExpr binaryExpr = (BinaryExpr) expr;
			return findVarRef(name, binaryExpr.getRhs()) ||
					findVarRef(name, binaryExpr.getRhs());
		}
		if (expr instanceof UnaryExpr) {
			UnaryExpr unaryExpr = (UnaryExpr) expr;
			return findVarRef(name, unaryExpr.getOperand());
		}
		if (expr instanceof TernaryExpr) {
			TernaryExpr ternaryExpr = (TernaryExpr) expr;
			return findVarRef(name, ternaryExpr.getTrueExpr()) ||
					findVarRef(name, ternaryExpr.getFalseExpr());
		}
		return false;
	}
	
	private int getVarIndex(String varName) {
		if (varsIndex.containsKey((varName)))
			return varsIndex.get(varName);
		return 0;
	}
}
