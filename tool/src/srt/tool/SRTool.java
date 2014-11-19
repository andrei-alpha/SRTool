package srt.tool;

public interface SRTool {
	
	public static enum SRToolResult {
		CORRECT, INCORRECT, UNKNOWN, MAYBE_CORRECT
	}
	
	public SRToolResult go() throws Exception;
	
}
