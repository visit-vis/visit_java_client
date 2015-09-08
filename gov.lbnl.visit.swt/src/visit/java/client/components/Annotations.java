package visit.java.client.components;

public class Annotations extends VisItComponent {

	public Annotations() {
		
	}
	
	public void cleanupCanvas() {
		if(methods == null) return;
		
		String commands = "annot = AnnotationAttributes()\n";
		commands += "annot.userInfoFlag = 0\n";
		commands += "annot.databaseInfoFlag = 0\n";
		commands += "SetAnnotationAttributes(annot);\n";
		methods.processCommands(commands);
	}
}
