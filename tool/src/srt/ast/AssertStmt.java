package srt.ast;

public class AssertStmt extends Stmt {
	private boolean isUnwinding;
	
	public AssertStmt(Expr condition) {
		this(condition, null);
	}
	
	public AssertStmt(Expr condition, NodeInfo nodeInfo) {
		super(nodeInfo);
		children.add(condition);
	}
	
	public Expr getCondition() {
		return (Expr) children.get(0);
	}
	
	public void makeUnwinding() {
		isUnwinding = true;
	}
	
	public boolean isUnwinding() {
		return isUnwinding;
	}
	
	@Override
	public String toString() {
		return "AssertStmt: " + this.getCondition();
	}
}
