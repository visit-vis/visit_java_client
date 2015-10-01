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
	
}
