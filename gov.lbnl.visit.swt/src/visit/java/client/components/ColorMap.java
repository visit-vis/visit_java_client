package visit.java.client.components;

import java.util.List;

import visit.java.client.AttributeSubject;

public class ColorMap extends VisItComponent {

	public ColorMap() {
		
	}
	
	public List<String> getPredefinedList() {
		if(methods == null) return null;
		
	    AttributeSubject catts = methods.getViewerState().getAttributeSubjectFromTypename("ColorTableAttributes");
	    List<String> colornames = catts.getAsStringVector("names");
	    
	    return colornames;
	}
}
