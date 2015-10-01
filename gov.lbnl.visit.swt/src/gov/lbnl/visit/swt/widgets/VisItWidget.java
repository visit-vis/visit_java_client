package gov.lbnl.visit.swt.widgets;

import org.eclipse.swt.widgets.Composite;

import gov.lbnl.visit.swt.VisItSwtConnection;

public class VisItWidget extends Composite {

	VisItSwtConnection connection;

	/**
	 *
	 * @param parent
	 *            A widget which will be the parent of the new instance (cannot
	 *            be null).
	 * @param style
	 *            The style of widget to construct.
	 */
//	public VisItWidget(Composite parent, int style) {
//	    this(parent, style, null);
//	}
//	
	/**
	 * 
	 * @param parent
	 *            A widget which will be the parent of the new instance (cannot
	 *            be null).
	 * @param style
	 *            The style of widget to construct.
	 * @param conn
	 */
	public VisItWidget(Composite parent, int style, VisItSwtConnection conn) {
	    super(parent, style);
	
	    connection = conn;
	}
	
//	public void setVisItSwtConnection(VisItSwtConnection conn) {
//	    connection = conn;
//	}
	
}
