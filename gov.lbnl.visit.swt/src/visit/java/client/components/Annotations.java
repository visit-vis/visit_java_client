package visit.java.client.components;

import visit.java.client.ViewerMethods;

public class Annotations extends VisItComponent {

	int timeSliderCount = 1;
	int text2dCount = 1;
	
	public Annotations(ViewerMethods m) {
       super(m);	
	}
	
	public void cleanupCanvas() {
		if(methods == null) return;
		
		String commands = "annot = AnnotationAttributes()\n";
		commands += "annot.userInfoFlag = 0\n";
		commands += "annot.databaseInfoFlag = 0\n";
		commands += "SetAnnotationAttributes(annot);\n";
		methods.processCommands(commands);
	}
	
	public void createTimeSlider(float x, float y) {
		String newName = "TimeSlider" + timeSliderCount;
		String commands = "ref = CreateAnnotationObject('TimeSlider', '" + newName + "')\n";
		//commands += "ref = GetAnnotationObject('" + newName + "')\n";
		commands += "ref.position = (" + x + ","+ y + ")\n";
		timeSliderCount++;
		methods.processCommands(commands);
	}

	public void createText2D(String text, float x, float y) {
		String newName = "Text2D" + text2dCount;
		String commands = "CreateAnnotationObject('Text2D', '" + newName + "')\n";
		commands += "ref = GetAnnotationObject('" + newName + "')\n";
		commands += "ref.text = '" + text + "'\n";
		commands += "ref.position = (" + x + ","+ y + ")\n";
		text2dCount++;
		methods.processCommands(commands);
	}
	
	public void clearAll() {
		String cmds = "";
		for(int i = 1; i < timeSliderCount; ++i) {
			String newName = "TimeSlider" + i;
			cmds += "tmp = GetAnnotationObject('" + newName + "')\n";
			cmds += "tmp.Delete()\n";
			}
		
		for(int i = 1; i < text2dCount; ++i) {
			String newName = "Text2D" + i;
			cmds += "tmp = DeleteAnnotationObject('" + newName + "')\n";
			cmds += "tmp.Delete()\n";
		}
		
		text2dCount = 1;
		timeSliderCount = 1;
		methods.processCommands(cmds);
	}
	
}
