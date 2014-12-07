package srt.ast;

import java.util.ArrayList;
import java.util.List;

public class BlockStmt extends Stmt {
	private WhileStmt baseWhileStmt;
	private ArrayList<AssertStmt> houdiniAsserts;
	private ArrayList<AssumeStmt> houdiniAssumes;
	
	public BlockStmt(StmtList stmtList) {
		this(stmtList, null);
	}
	
	public BlockStmt(StmtList stmtList, NodeInfo nodeInfo) {
		super(nodeInfo);
		children.add(stmtList);
		baseWhileStmt = null;
		houdiniAsserts = new ArrayList<AssertStmt>();
		houdiniAssumes = new ArrayList<AssumeStmt>();
	}
	
	public BlockStmt(Stmt[] statements) {
		this(statements, null);
	}
	
	public BlockStmt(Stmt[] statements, NodeInfo nodeInfo) {
		super(nodeInfo);
		children.add(new StmtList(statements, nodeInfo));
		baseWhileStmt = null;
		houdiniAsserts = new ArrayList<AssertStmt>();
		houdiniAssumes = new ArrayList<AssumeStmt>();
	}
	
	public BlockStmt(List<Stmt> statements) {
		this(statements, null);
	}
	
	public BlockStmt(List<Stmt> statements, NodeInfo nodeInfo) {
		super(nodeInfo);
		children.add(new StmtList(statements, nodeInfo));
		baseWhileStmt = null;
		houdiniAsserts = new ArrayList<AssertStmt>();
		houdiniAssumes = new ArrayList<AssumeStmt>();
	}
	
	public StmtList getStmtList() {
		return (StmtList) children.get(0);
	}
	
	public void setBaseWhileStmt(WhileStmt whileStmt) {
		baseWhileStmt = whileStmt;
	}
	
	public WhileStmt getBaseWhileStmt() {
		return baseWhileStmt;
	}
	
	public void addHoudiniAssert(AssertStmt assertStmt) {
		houdiniAsserts.add(assertStmt);
	}
	
	public ArrayList<AssertStmt> getHoudiniAsserts() {
		return houdiniAsserts;
	}

	public void addHoudiniAssume(AssumeStmt stmt) {
		houdiniAssumes.add(stmt);
	}
	
	public ArrayList<AssumeStmt> getHoudiniAssumes() {
		return houdiniAssumes;
	}
}
