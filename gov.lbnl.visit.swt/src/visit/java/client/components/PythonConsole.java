package visit.java.client.components;

import java.util.ArrayList;
import java.util.List;

import visit.java.client.AttributeSubject;
import visit.java.client.AttributeSubject.AttributeSubjectCallback;
import visit.java.client.ViewerMethods;

public class PythonConsole extends VisItComponent {
	
	public interface PythonConsoleCallback {
		public void response(List<String> r);
	}
	
	List<PythonConsoleCallback> cb = new ArrayList<PythonConsoleCallback>();
	
	/**
	 * The AttributeSubjectCallback used by the VisIt widget.
	 */
	private AttributeSubjectCallback attSubCallback;
	
	public PythonConsole(ViewerMethods methods) {
		super(methods);
		
		attSubCallback = new AttributeSubjectCallback() {

			@Override
			public boolean update(AttributeSubject subject) {
				String name = subject.getAsString("methodName");

				if (!"AcceptRecordedMacro".equals(name)) {
					return false;
				}

				final List<String> vec = subject
						.getAsStringVector("stringArgs");

				for(int i = 0; i < cb.size(); ++i) {
					cb.get(i).response(vec);
				}

				return true;
			}
		};

		// Register the AttributeSubjectCallback with the VisIt widget
		methods.getViewerState()
				.registerCallback("ClientMethod", attSubCallback);
	}
	
	public void processCommands(String cmd) {
		if(methods == null) return;
		methods.processCommands(cmd);
	}
	
	public boolean register(PythonConsoleCallback c) {
		return cb.add(c);
	}
	
	public boolean unregister(PythonConsoleCallback c) {
		return cb.remove(c);
	}
	
	public void cleanup() {
		cb.clear();
		methods.getViewerState()
		.unregisterCallback("ClientMethod", attSubCallback);	
	}
}
