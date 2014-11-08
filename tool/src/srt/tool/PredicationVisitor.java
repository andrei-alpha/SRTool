package srt.tool;

import java.util.HashMap;
import java.util.Stack;

import srt.ast.AssertStmt;
import srt.ast.AssignStmt;
import srt.ast.AssumeStmt;
import srt.ast.BinaryExpr;
import srt.ast.BlockStmt;
import srt.ast.DeclRef;
import srt.ast.EmptyStmt;
import srt.ast.Expr;
import srt.ast.HavocStmt;
import srt.ast.IfStmt;
import srt.ast.Stmt;
import srt.ast.TernaryExpr;
import srt.ast.UnaryExpr;
import srt.ast.visitor.impl.DefaultVisitor;

public class PredicationVisitor extends DefaultVisitor {
	private Stack<String> conditionVars, assumeVars;
	private HashMap<String, String> havocVars;
	private int assumeIndex, condIndex, havocIndex;
	private static final int ASSUME = 0;
	private static final int CONDITIONAL = 1;
	
	public PredicationVisitor() {
		super(true);
		conditionVars = new Stack<String>();
		assumeVars = new Stack<String>();
		havocVars = new HashMap<String, String>();
		condIndex = havocIndex = 0;
	}
	
	@Override
	public Object visit(IfStmt ifStmt) {
		Expr condition = (Expr) super.visit(ifStmt.getCondition());
		
		// Add if condition and visit then block
		AssignStmt thenQ = newCondition(CONDITIONAL, condition);
		Stmt thenStmt = (Stmt) super.visit(ifStmt.getThenStmt());
		// Remove condition declared inside
		conditionVars.pop();
		
		// Don't need to visit else block if not present
		if (ifStmt.getElseStmt() instanceof EmptyStmt) {
			BlockStmt blockStmt = new BlockStmt(new Stmt[] { thenQ, thenStmt },
					ifStmt.getNodeInfo());
			
			return blockStmt;
		}
		
		// Add the complement of if condition and visit else block
		UnaryExpr complement = new UnaryExpr(UnaryExpr.LNOT, condition);
		AssignStmt elseQ = newCondition(CONDITIONAL, complement);
		Stmt elseStmt = (Stmt) super.visit(ifStmt.getElseStmt());
		// Remove condition declared inside
		conditionVars.pop();
		
		BlockStmt blockStmt = new BlockStmt(new Stmt[] { thenQ, thenStmt, elseQ, elseStmt },
				ifStmt.getNodeInfo());
		
		return blockStmt;
	}

	@Override
	public Object visit(AssertStmt assertStmt) {
		Expr topPredicate = getTopPredicate();
		if (topPredicate != null) {
			Expr leftQ = new UnaryExpr(UnaryExpr.LNOT, topPredicate); 
			Expr rightQ = assertStmt.getCondition();
			BinaryExpr condition = new BinaryExpr(BinaryExpr.LOR, leftQ, rightQ);
			AssertStmt newAssertStmt = new AssertStmt(condition);
			
			return super.visit(newAssertStmt);
		}
		return super.visit(assertStmt);
	}
	
	@Override
	public Object visit(DeclRef declRef) {
		DeclRef newDeclRef = new DeclRef(getName(declRef.getName()));
		return super.visit(newDeclRef);
	}

	@Override
	public Object visit(AssignStmt assignment) {
		Expr topPredicate = getTopPredicate();
		if (topPredicate != null) {
			Expr trueExpr = assignment.getRhs();
			Expr falseExpr = assignment.getLhs();
			TernaryExpr ternExpr = new TernaryExpr(topPredicate, trueExpr, falseExpr);
			AssignStmt newAssign = new AssignStmt(assignment.getLhs(), ternExpr);
			
			return super.visit(newAssign);
		}
		return super.visit(assignment);
	}

	@Override
	public Object visit(AssumeStmt assumeStmt) {
		AssignStmt assumeCond = newCondition(ASSUME, assumeStmt.getCondition()); 
		return super.visit(assumeCond);
	}

	@Override
	public Object visit(HavocStmt havocStmt) {
		havocStmt = (HavocStmt) super.visit(havocStmt);
		
		// Create a fresh new variable and replace all occurrences of x with it
		DeclRef declRef = havocStmt.getVariable();
		havocVariable(declRef.getName());
		
		return havocStmt;
	}
	
	private void havocVariable(String name) {
		havocIndex += 1;
		havocVars.put(name, "$h" + String.valueOf(havocIndex));
	}
	
	private String getName(String name) {
		if (havocVars.containsKey(name))
			return havocVars.get(name);
		return name;
	}
	
	private String peekVar(int type) {
		if (type == CONDITIONAL) {
			if (conditionVars.empty())
				return null;
			return conditionVars.peek();
		} else if (type == ASSUME) {
			if (assumeVars.empty())
				return null;
			return assumeVars.peek();
		}
		return null;
	}
	
	private String nextConditionVar() {
		condIndex += 1;
		return "$Q" + String.valueOf(condIndex);
	}
	
	private String nextAssumeVar() {
		assumeIndex += 1;
		return "$G" + String.valueOf(assumeIndex);
	}
	
	private AssignStmt newCondition(int type, Expr condition) {
		String name = null;
		// Get a new variable name
		if (type == CONDITIONAL)
			name = nextConditionVar();
		else
			name = nextAssumeVar();
		
		DeclRef declRef = new DeclRef(name);
		AssignStmt assign = new AssignStmt(declRef, condition);
		
		// If we are inside another conditional
		if (peekVar(type) != null) {
			Expr lastCondition = (Expr) new DeclRef(peekVar(type));
			assign = new AssignStmt(declRef, new BinaryExpr(BinaryExpr.LAND, condition, lastCondition));
		}
		// Add predicate to queue
		if (type == CONDITIONAL)
			conditionVars.push(name);
		else
			assumeVars.push(name);
		
		return (AssignStmt) super.visit(assign);
	}

	private Expr getTopPredicate() {
		DeclRef lastAssume = null, lastCondition = null;
		if (conditionVars.empty() && assumeVars.empty())
			return null;
		if (!assumeVars.empty())
			lastAssume = new DeclRef(assumeVars.peek());
		if (!conditionVars.empty())
			lastCondition = new DeclRef(conditionVars.peek());
		
		if (lastCondition == null)
			return lastAssume;
		else if (lastAssume == null)
			return lastCondition;
		return new BinaryExpr(BinaryExpr.LAND, lastCondition, lastAssume);
	}
}