package gov.lbnl.visit.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import gov.lbnl.visit.swt.VisItSwtConnection;
import visit.java.client.components.MessageAttributes;
import visit.java.client.components.MessageAttributes.MessageAttributesCallback;
import visit.java.client.components.MessageAttributes.Severity;

public class MessageAttributesWidget extends VisItWidget {

	MessageAttributes atts;
	
	public MessageAttributesWidget(Composite parent, int style, VisItSwtConnection conn) {
		super(parent, style, conn);
	
		atts = new MessageAttributes(conn.getViewerMethods());
		setupUI();
	}
	
	private void setupUI() {
		new Label(this, SWT.NONE).setText("Messages:");

		final StyledText history = new StyledText(this, SWT.MULTI | SWT.BORDER | 
												SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
		history.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		final MessageAttributesCallback cb = new MessageAttributesCallback() {
			
			@Override
			public void message(MessageAttributes.Severity s, String msg) {
				final int severity = s.ordinal();
				final String vec = msg;

			    final Color[] SeverityColors = new Color[5];			    
			    SeverityColors[Severity.Error.ordinal()] = new Color(history.getDisplay(), new RGB(255, 0, 0));
				SeverityColors[Severity.Warning.ordinal()] = new Color(history.getDisplay(), new RGB(255, 255, 0));
				SeverityColors[Severity.Message.ordinal()] = new Color(history.getDisplay(), new RGB(255, 255, 255));
				SeverityColors[Severity.ErrorClear.ordinal()] = new Color(history.getDisplay(), new RGB(255, 0, 255));
				SeverityColors[Severity.Information.ordinal()] = new Color(history.getDisplay(), new RGB(0, 255, 255));
				
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						int line = history.getLineCount()-1;
						int count = vec.length() - vec.replace("\n", "").length();
						history.append(vec);
						history.setLineBackground(line, count, SeverityColors[severity]);
						// Move the cursor to the end of the console
						history.setSelection(history.getText().length());
					}
				});
										
			}
		};
		
		atts.register(cb);
		
		this.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				atts.unregister(cb);
				atts.cleanup();
			}
		});

	}

}
