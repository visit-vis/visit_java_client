package visit.java.client.components;

import java.util.ArrayList;
import java.util.List;

import visit.java.client.AttributeSubject;
import visit.java.client.AttributeSubject.AttributeSubjectCallback;
import visit.java.client.ViewerMethods;

public class MessageAttributes extends VisItComponent {

    public enum Severity
    {
        Error,
        Warning,
        Message,
        ErrorClear,
        Information
    };

    public interface MessageAttributesCallback 
    {
    	public void message(Severity severity, String msg);
    };
    
    List<MessageAttributesCallback> cb = new ArrayList<MessageAttributesCallback>();
    
    public MessageAttributes(ViewerMethods m) {
    	super(m);
    	
		attSubCallback = new AttributeSubjectCallback() {

			@Override
			public boolean update(AttributeSubject subject) {
				final int severity = subject.getAsInt("severity");

				final String vec = subject
						.getAsString("text") + "\n";
				
				for(int i = 0; i < cb.size(); ++i) {
					cb.get(i).message(Severity.values()[severity], vec);
				}
				return true;
			}
		};

		// Register the AttributeSubjectCallback with the VisIt widget
		methods.getViewerState()
				.registerCallback("MessageAttributes", attSubCallback);
	}
    
    AttributeSubjectCallback attSubCallback;
	
    public boolean register(MessageAttributesCallback c) {
    	return cb.add(c);
	}
	
	public boolean unregister(MessageAttributesCallback c) {
		return cb.remove(c);
	}
	
	public void cleanup() {
		cb.clear();
		methods.getViewerState()
		.unregisterCallback("MessageAttributes", attSubCallback);
	}
}
