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
	}

	private void addPythonConsoleWidget() {
		TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
		tabItem.setText("Python Console Widget");
	
		PythonConsoleWidget widget = new PythonConsoleWidget(tabFolder, 
				SWT.BORDER, 
				connection);
		widget.setLayout(new GridLayout(1, true));
		widget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tabItem.setControl(widget);
		
	}
	
	private void addColorMapWidget() {
		TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
		tabItem.setText("ColorMap Widget");
	
		ColorMapWidget widget = new ColorMapWidget(tabFolder, SWT.BORDER, connection);
		widget.setLayout(new GridLayout(1, true));
		widget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tabItem.setControl(widget);
	}
	
	private void addAnnotationWidget() {
		TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
		tabItem.setText("Annotation Widget");
	
		AnnotationWidget widget = new AnnotationWidget(tabFolder, SWT.BORDER, connection);
		widget.setLayout(new GridLayout(1, true));
		widget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tabItem.setControl(widget);
	}
	
	private void addMessageWidget() {
		TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
		tabItem.setText("Message Attributes");
	
		MessageAttributesWidget widget = new MessageAttributesWidget(tabFolder, SWT.BORDER, connection);
		widget.setLayout(new GridLayout(1, true));
		widget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tabItem.setControl(widget);
	}

	private void addTimeSliderWidget() {
		TabItem tabItem = new TabItem(tabFolder, SWT.NULL);
		tabItem.setText("TimeSlider Widget");
	
		TimeSliderWidget widget = new TimeSliderWidget(tabFolder, SWT.BORDER, connection);
		//widget.setLayout(new GridLayout(1, true));
		widget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tabItem.setControl(widget);
	}
	
	public void showOptions(Composite parent) {
		tabFolder = new TabFolder(parent, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		addPythonConsoleWidget();
		addColorMapWidget();
		addAnnotationWidget();
		addMessageWidget();
		addTimeSliderWidget();
	}
}
