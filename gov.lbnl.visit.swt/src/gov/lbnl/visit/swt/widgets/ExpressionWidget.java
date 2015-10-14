package gov.lbnl.visit.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import gov.lbnl.visit.swt.VisItSwtConnection;
import visit.java.client.components.Expressions;

public class ExpressionWidget extends VisItWidget {

	Expressions exprs;
	public ExpressionWidget(Composite parent, int style, VisItSwtConnection conn) {
		super(parent, style, conn);
		exprs = new Expressions(conn.getViewerMethods());
		
		setupUI();
	}
	
	private void setupUI() {
		Composite comp = new Composite(this, SWT.BORDER);
		comp.setLayout(new GridLayout(1, true));
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite comp1 = new Composite(comp, SWT.BORDER);
		comp1.setLayout(new RowLayout());
		comp1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		new Label(comp1, SWT.NONE).setText("Select Expression:");
		final Combo combo = new Combo(comp1, SWT.NONE);
		combo.setItems(new String[] {"Scalar", "Vector"} );
		combo.select(0);
		
		Composite comp2 = new Composite(comp, SWT.BORDER);
		comp2.setLayout(new GridLayout(2, false));
		comp2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		new Label(comp2, SWT.NONE).setText("Name:");
		final Text name = new Text(comp2, SWT.NONE);
		name.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		final Text expr = new Text(comp, SWT.NONE);
		expr.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Button button = new Button(comp, SWT.NONE);
		button.setText("Submit");
		
		button.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String opt = combo.getText();
				String n = name.getText();
				String def = expr.getText();
				
				if(opt.equals("Scalar")) {
					exprs.addScalarExpression(n, def);
				} else if(opt.equals("Vector")) {
					exprs.addVectorExpression(n, def);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
	}
	

}
