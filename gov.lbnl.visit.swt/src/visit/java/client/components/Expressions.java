package visit.java.client.components;

import visit.java.client.ViewerMethods;

public class Expressions extends VisItComponent {

	public Expressions(ViewerMethods m) {
		super(m);
	}
	
	public void addScalarExpression(String name, String definition) {
		String cmd = "DefineScalarExpression('" + name + "', '" + definition + "')\n";
		methods.processCommands(cmd);
	}
	
	public void addVectorExpression(String name, String definition) {
		String cmd = "DefineVectorExpression('" + name + "', '" + definition + "')\n";
		methods.processCommands(cmd);		
	}

}
