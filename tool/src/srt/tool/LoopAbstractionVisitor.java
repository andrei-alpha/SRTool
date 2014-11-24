package srt.tool;

import java.util.ArrayList;

import srt.ast.AssertStmt;
import srt.ast.AssumeStmt;
import srt.ast.BlockStmt;
import srt.ast.DeclRef;
import srt.ast.EmptyStmt;
import srt.ast.Expr;
import srt.ast.HavocStmt;
import srt.ast.IfStmt;
import srt.ast.Invariant;
import srt.ast.Stmt;
import srt.ast.StmtList;
import srt.ast.UnaryExpr;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;

public class LoopAbstractionVisitor extends DefaultVisitor {

	public LoopAbstractionVisitor() {
		super(true);
	}

	@Override
	public Object visit(WhileStmt whileStmt) {
		ArrayList<Stmt> stmts = new ArrayList<Stmt>();
		ArrayList<Stmt> body = new ArrayList<Stmt>(); 
		
		// Havoc all variables from loop modifies set
		for (String varName : whileStmt.getModifies()) {
			body.add(new HavocStmt(new DeclRef(varName)));
		}
		
		// Add all loop invariants
		for (Invariant inv : whileStmt.getInvariantList().getInvariants()) {
			if (!inv.isCandidate())
				body.add(new AssumeStmt(inv.getExpr()));
		}
		
		// Add all loop assertions TODO: from beginning or end, else fail
		CollectConstraintsVisitor collectConstraintsVisitor = new CollectConstraintsVisitor();
		collectConstraintsVisitor.visit(whileStmt.getBody());
		for (AssertStmt assertStmt : collectConstraintsVisitor.propertyNodes)
			body.add(assertStmt);
		
		// This is the new body of the loop
		IfStmt ifStmt = new IfStmt(whileStmt.getCondition(), new StmtList(body), new EmptyStmt());
		stmts.add(ifStmt);
		
		// Add all loop invariants
		for (Invariant inv : whileStmt.getInvariantList().getInvariants()) {
			System.out.println("Good Invariant: " + inv.getExpr());
			if (!inv.isCandidate())
				stmts.add(new AssumeStmt(inv.getExpr()));
		}
		
		// Add negated condition
		Expr notCondition = new UnaryExpr(UnaryExpr.LNOT, whileStmt.getCondition());
		stmts.add(new AssumeStmt(notCondition));
		
		return super.visit(new BlockStmt(stmts, whileStmt.getNodeInfo()));
	}
}