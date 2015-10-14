package gov.lbnl.visit.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import gov.lbnl.visit.swt.VisItSwtConnection;
import visit.java.client.components.Session;

public class SessionWidget extends VisItWidget {

	Session session;
	
	public SessionWidget(Composite parent, int style, VisItSwtConnection conn) {
		super(parent, style, conn);
		session = new Session(conn.getViewerMethods());
		setupUI();
	}
	
	private void setupUI() {
		
		Composite comp = new Composite(this, SWT.BORDER);
		comp.setLayout(new RowLayout());
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		new Label(comp, SWT.NONE).setText("Set Filename:");
		final Text text = new Text(comp, SWT.BORDER);
		text.setLayoutData(new RowData(400, SWT.DEFAULT));
		
		Button loadButton = new Button(comp, SWT.PUSH);
		loadButton.setText("Load");
		
		Button saveButton = new Button(comp, SWT.PUSH);
		saveButton.setText("Save");
		
		loadButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(text.getText().length() == 0) return;
				session.load(text.getText());				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		

		saveButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(text.getText().length() == 0) return;
				session.save(text.getText());				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

}
