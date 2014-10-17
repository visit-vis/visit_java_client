package gov.lbnl.visit.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import gov.lbnl.visit.swt.VisItSwtConnection;

public class TimeSliderWidget extends Composite {

	private VisItSwtConnection connection;
	
	public TimeSliderWidget(Composite parent, int flags) {
		super(parent, flags);
		connection = null;
		setLayout(new RowLayout(SWT.HORIZONTAL));
		setupUI();
	}
	
	public TimeSliderWidget(Composite parent, int flags, VisItSwtConnection conn) {
		super(parent, flags);
		connection = conn;
		setLayout(new RowLayout(SWT.HORIZONTAL));
		setupUI();
	}
	
	
	public void setVisItSwtConnection(VisItSwtConnection conn) { 
		connection = conn;
	}
	
	/*!
	 * \brief create user interface
	 */
	private void setupUI() {
		

		Button stepBack = new Button(this, SWT.NONE);
		stepBack.setText("Step Back");
		
		Button rewind = new Button(this, SWT.NONE);
		rewind.setText("Rewind");
		
		Button stop = new Button(this, SWT.NONE);
		stop.setText("Stop");
		
		Button play = new Button(this, SWT.NONE);
		play.setText("Play");
		
		Button stepForward = new Button(this, SWT.NONE);
		stepForward.setText("Step Forward");		

		Text timeSteps = new Text(this, SWT.NONE);
		timeSteps.setText("Timesteps...");	
		
		stepBack.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(connection == null) {
					return;
				}
				connection.getViewerMethods().animationPreviousState();
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		rewind.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(connection == null) {
					return;
				}
				connection.getViewerMethods().animationReversePlay();
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		stop.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(connection == null) {
					return;
				}
				connection.getViewerMethods().animationStop();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		

		play.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(connection == null) {
					return;
				}
				connection.getViewerMethods().animationPlay();
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		stepForward.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(connection == null) {
					return;
				}
				connection.getViewerMethods().animationNextState();
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		this.redraw();
		this.pack();
	}
}
