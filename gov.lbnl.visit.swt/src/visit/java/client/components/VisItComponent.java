package visit.java.client.components;

import visit.java.client.ViewerMethods;

public class VisItComponent {

	ViewerMethods methods;
	
	public VisItComponent() {
		methods = null;
	}
	
	public VisItComponent(ViewerMethods m) {
		methods = m;
	}
	
	public void setViewerMethods(ViewerMethods m) {
		methods = m;
	}
}
