package srt.tool;

import java.util.ArrayList;

import srt.ast.AssertStmt;
import srt.ast.AssumeStmt;
import srt.ast.BlockStmt;
import srt.ast.DeclRef;
import srt.ast.EmptyStmt;
import srt.ast.HavocStmt;
import srt.ast.IfStmt;
import srt.ast.IntLiteral;
import srt.ast.Invariant;
import srt.ast.Program;
import srt.ast.Stmt;
import srt.ast.WhileStmt;
import srt.ast.visitor.impl.DefaultVisitor;

public class HoudiniTransformerVisitor extends DefaultVisitor {

	public HoudiniTransformerVisitor(Program program) {
		super(true);
	}

	@Override
	public Object visit(WhileStmt whileStmt) {
		whileStmt = (WhileStmt) super.visit(whileStmt);
		return super.visit(transformWhileStmt(whileStmt));
	}
	
	public static BlockStmt transformWhileStmt(WhileStmt whileStmt) {
		ArrayList<Stmt> stmts = new ArrayList<Stmt>(); 
		
		// Assert all loop invariants
		for (Invariant inv : whileStmt.getInvariantList().getInvariants()) {
			AssertStmt assertStmt = new AssertStmt(inv.getExpr());
			assertStmt.setVisible(false);
			assertStmt.setHoudiniInvariant(inv.getExpr());
			stmts.add(assertStmt);
		}
		// Havoc all loop modset variables
		for (String varName : whileStmt.getModifies()) {
			stmts.add(new HavocStmt(new DeclRef(varName)));
		}
		// Assume all loop invariants
		for (Invariant inv : whileStmt.getInvariantList().getInvariants()) {
			AssumeStmt assumeStmt = new AssumeStmt(inv.getExpr());
			assumeStmt.setVisible(false);
			stmts.add(assumeStmt);
		}
		// Abstract loop body once
		ArrayList<Stmt> body = new ArrayList<Stmt>();
		body.add(whileStmt.getBody());
		for (Invariant inv : whileStmt.getInvariantList().getInvariants()) {
			AssertStmt assertStmt = new AssertStmt(inv.getExpr());
			assertStmt.setVisible(false);
			assertStmt.setHoudiniInvariant(inv.getExpr());
			body.add(assertStmt);
		}
		AssumeStmt stopAssumeStmt = new AssumeStmt(new IntLiteral(0));
		stopAssumeStmt.setVisible(false);
		body.add(stopAssumeStmt);
		stmts.add(new IfStmt(whileStmt.getCondition(), new BlockStmt(body), new EmptyStmt()));
		BlockStmt houdiniBlockStmt = new BlockStmt(stmts);
		houdiniBlockStmt.setBaseWhileStmt(whileStmt);
		
		// Make the houdini block keep track of its assertions
		for (Stmt stmt : stmts) {
			if (stmt instanceof AssertStmt)
				houdiniBlockStmt.addHoudiniAssert((AssertStmt) stmt);
			if (stmt instanceof AssumeStmt)
				houdiniBlockStmt.addHoudiniAssume((AssumeStmt) stmt);
		}
		for (Stmt stmt : body) {
			if (stmt instanceof AssertStmt)
				houdiniBlockStmt.addHoudiniAssert((AssertStmt) stmt);
			if (stmt instanceof AssumeStmt)
				houdiniBlockStmt.addHoudiniAssume((AssumeStmt) stmt);
		}
		
		return houdiniBlockStmt;
	}
}
