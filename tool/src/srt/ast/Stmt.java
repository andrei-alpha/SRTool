package srt.ast;

public abstract class Stmt extends Node {
	private boolean visible;
	
	public Stmt(NodeInfo nodeInfo) {
		super(nodeInfo);
		visible = true;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
}
