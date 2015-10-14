package visit.java.client.components;

import java.util.List;

import visit.java.client.ViewerMethods;

public class Session extends VisItComponent{

	public Session(ViewerMethods m) {
		super(m);
	}

	public void save(String filename) {
		methods.exportEntireState(filename);
	}
	
	public void load(String filename) {
		methods.ImportEntireState(filename, false);
	}
	
	public void load(String filename, List<String> sources) {
		methods.importEntireStateWithDifferentSources(filename, false, sources);
	}
}
