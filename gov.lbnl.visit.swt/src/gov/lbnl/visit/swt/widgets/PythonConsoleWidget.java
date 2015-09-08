package gov.lbnl.visit.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import gov.lbnl.visit.swt.VisItSwtConnection;
import visit.java.client.components.PythonConsole;

public class PythonConsoleWidget extends VisItWidget {
	
	PythonConsole console;
	
	/**
    *
    * @param parent
    *            A widget which will be the parent of the new instance (cannot
    *            be null).
    * @param style
    *            The style of widget to construct.
    */
   public PythonConsoleWidget(Composite parent, int style) {
       this(parent, style, null);
   }

   /**
    * 
    * @param parent
    *            A widget which will be the parent of the new instance (cannot
    *            be null).
    * @param style
    *            The style of widget to construct.
    * @param conn
    */
   public PythonConsoleWidget(Composite parent, int style, VisItSwtConnection conn) {
       super(parent, style, conn);
       
       console = new PythonConsole();
       setupUI();
   }
   
   
   
	public void setupUI() {
		new Label(this, SWT.NONE).setText("History:");

		final Text history = new Text(this, SWT.MULTI | SWT.BORDER | 
												SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
		history.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create a multiple-line text field
		new Label(this, SWT.NONE).setText("Commands:");
		final Text input = new Text(this, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		input.setLayoutData(new GridData(GridData.FILL_BOTH));
	    
	    Composite comp = new Composite(this, SWT.NONE);
	    comp.setLayout(new FillLayout());
	    
	    Button interpret = new Button(comp, SWT.BORDER | SWT.PUSH);
	    interpret.setText("Interpret");
	    
	    
	    interpret.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String text = input.getText();
				input.setText("");
				
				history.append(text + "\n");
				console.processCommands(text);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	}

}
