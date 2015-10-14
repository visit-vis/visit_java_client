package gov.lbnl.visit.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import gov.lbnl.visit.swt.VisItSwtConnection;

public class AdvancedWidgets extends VisItWidget {

	private TabFolder tabFolder;
	
	
	public AdvancedWidgets(Composite parent, int style, VisItSwtConnection conn) {
	    super(parent, style, conn);
	    
	    tabFolder = new TabFolder(this, SWT.BORDER | SWT.H_SCROLL);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		if(conn != null) {
			showOptions();
		}
	}
	private void addTimeSliderWidget() {
		TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
		tabItem.setText("TimeSlider Widget");
	
		TimeSliderWidget widget = new TimeSliderWidget(tabFolder, SWT.BORDER, connection);
		//widget.setLayout(new GridLayout(1, true));
		widget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tabItem.setControl(widget);
	}
	
	private void addWidget(String title, VisItWidget widget) {
		TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
		tabItem.setText(title);
	
		widget.setLayout(new GridLayout(1, true));
		widget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tabItem.setControl(widget);
	}
	
	public void setVisItSwtConnection(VisItSwtConnection c) {
		connection = c;
		
		if(connection != null) {
			TabItem[] items = tabFolder.getItems();
			if(items != null) {
				for(int i = 0; i < items.length; ++i) {
					items[i].getControl().dispose();
					items[i].dispose();
				}
			}
		}
		///re-add with new connection..
		showOptions();
	}
	
	private void showOptions() {
		addWidget("Python Console", new PythonConsoleWidget(tabFolder, SWT.BORDER, connection));
		addWidget("ColorMaps", new ColorMapWidget(tabFolder, SWT.BORDER, connection));
		addWidget("Annotations", new AnnotationWidget(tabFolder, SWT.BORDER, connection));
		addWidget("Expressions", new ExpressionWidget(tabFolder, SWT.BORDER, connection));
		addWidget("Save Window", new SaveWindowWidget(tabFolder, SWT.BORDER, connection));
		addWidget("Session", new SessionWidget(tabFolder, SWT.BORDER, connection));
		addWidget("Message Attributes", new MessageAttributesWidget(tabFolder, SWT.BORDER, connection));
		//addWidget("TimeSlider", new TimeSliderWidget(tabFolder, SWT.BORDER, connection));
		addTimeSliderWidget();
	}
}
