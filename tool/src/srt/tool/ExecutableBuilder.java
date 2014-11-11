package srt.tool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import srt.ast.Program;
import srt.exec.ProcessExec;
import srt.tool.exception.ProcessTimeoutException;

public class ExecutableBuilder {
	private Program program;
	private CLArgs clArgs;
	
	public ExecutableBuilder(Program program, CLArgs clArgs) {
		this.program = program;
		this.clArgs = clArgs;
	}

	public String getProgram() throws IOException {
		String code = "";
		// Get a list of free vars
		ArrayList<String> freeVars = getFreeVariables();
		String params = "", args = "";
		boolean isFirst = true;
		for (String var : freeVars) {
			params += (isFirst ? "" : ", ") + "int " + var;
			args += (isFirst ? "" : ", ") + "newValue()";
			isFirst = false;
		}
		
		// Generate code for assert and havoc
		code += "#include <cstdio>\n#include <cstdlib>\n#include <ctime>\n\n";
		code += "long base = 10;\n\nint newValue() {\n@return rand() % base;\n}\n\n";
		code += "void assert(bool v) {\n@if (!v) {\n@@printf(@2incorrect@2);\n@@ex";
		code += "it(-1);\n@}\n}\n\nvoid havoc(int &x) {\n@x = newValue();\n}\n\n";
		
		// Parse the file into a string
		Scanner scanner = new Scanner(new File(clArgs.files.get(0)));
		code += scanner.useDelimiter("\\Z").next();
		
		// Remove var declaration, assumes, inv, cand and change main to start
		code = code.replaceFirst("void main\\(.*\\)", "void start(" + params + ")");
		code = code.replaceAll(".*inv\\(.*\\).*\n", "");
		code = code.replaceAll(".*cand\\(.*\\).*\n", "");
		code = code.replaceAll("assume\\((.*)\\);", "if (\\!($1)) return;");
		code = code.replaceAll(".*int\\s.*;.*\n", "");
				
		code += "\n\nint main() {\n@for(;;) {\n@@for (int i = 0; i < 10; ++i) {\n";
		code += "@@@start(" + args + ");\n@@}\n@@base=(base<99999999?base*10:base);";
		code += "\n@}\n@return 0;\n}\n";
		code = code.replaceAll("@2", "\"");
		code = code.replaceAll("@", "  ");
		
		// For debugging purposes
		System.out.println(code);
		scanner.close();
		
		return code;
	}
	
	public String run(String execProgram) throws IOException, InterruptedException {
		try {
			PrintWriter out = new PrintWriter("sr-test.cpp");
			out.println(execProgram);
			out.close();
		} catch (FileNotFoundException e) {
			return "unknown";
		}
		
		ProcessExec process = new ProcessExec("g++", "-o", "sr-test", "sr-test.cpp");
		try {
			process.execute("", clArgs.timeout);
		} catch(ProcessTimeoutException e) {
			return "unknown";
		}
		
		String runResult = "";
		process = new ProcessExec("./sr-test");
		try {
			runResult = process.execute("", 1 /*clArgs.timeout*/);
		} catch(ProcessTimeoutException e) {
			return "unknown";
		}

		System.out.println("Run:" + runResult);
		return runResult;
	}

	private ArrayList<String> getFreeVariables() {
		//ArrayList<String> freeVars = new ArrayList<String>();
		HashMap<String, Boolean> freeVars = new HashMap<String, Boolean>();
		
		// Collect the constraint expressions and variable names.
		CollectConstraintsVisitor ccv = new CollectConstraintsVisitor();
		ccv.visit(program);
		
		for (String var : ccv.variableNames) {
			freeVars.put(var, true);
		}
		//for (AssignStmt stmt : ccv.transitionNodes) {
		//	freeVars.remove(stmt.getLhs().getName());
		//}
		
		return new ArrayList<String>(freeVars.keySet());
	}
}
