package visit.java.client.components;

public class PythonConsole extends VisItComponent {
	public PythonConsole() {		
	}
	
	public void processCommands(String cmd) {
		if(methods == null) return;
		methods.processCommands(cmd);
	}
}
