package srt.tool;

import srt.ast.Program;
import srt.tool.SRTool.SRToolResult;

public abstract class Builder implements Runnable {
	protected Program program;
	protected CLArgs clArgs;
	protected SRToolResult runResult;
	
	public Builder(Program program, CLArgs clArgs) {
		this.program = program;
		this.clArgs = clArgs;
		this.runResult = SRToolResult.UNKNOWN;
	}
	
	public SRToolResult getResult() {
		return runResult;
	}
}
