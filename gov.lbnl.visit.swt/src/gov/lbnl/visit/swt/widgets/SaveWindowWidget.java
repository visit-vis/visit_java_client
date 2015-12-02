package gov.lbnl.visit.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import gov.lbnl.visit.swt.VisItSwtConnection;
import visit.java.client.components.SaveWindow;
import visit.java.client.components.SaveWindow.Format;

public class SaveWindowWidget extends VisItWidget {

	SaveWindow saveWindow;
	
	public SaveWindowWidget(Composite parent, int style, VisItSwtConnection conn) {
		super(parent, style, conn);
		saveWindow = new SaveWindow(conn.getViewerMethods());
		
		setupUI();
	}
	
	private void setupUI() {
		Composite comp = new Composite(this, SWT.BORDER);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		new Label(comp, SWT.NONE).setText("Set Filename Prefix:");
		final Text text = new Text(comp, SWT.BORDER);
		text.setLayoutData(new GridData(400, SWT.DEFAULT));
		text.setText("visit");

		new Label(comp, SWT.NONE).setText("Set Width:");
		final Text width = new Text(comp, SWT.BORDER);
		width.setLayoutData(new GridData(400, SWT.DEFAULT));
		width.setText("1024");

		new Label(comp, SWT.NONE).setText("Set Height:");
		final Text height = new Text(comp, SWT.BORDER);
		height.setLayoutData(new GridData(400, SWT.DEFAULT));
		height.setText("1024");

		new Label(comp, SWT.NONE).setText("Output Directory:");
		final Text outDirectory = new Text(comp, SWT.BORDER);
		outDirectory.setLayoutData(new GridData(400, SWT.DEFAULT));
		outDirectory.setText("~");

		new Label(comp, SWT.NONE).setText("Format:");
		final Combo combo = new Combo(comp, SWT.BORDER);
		SaveWindow.Format[] f = SaveWindow.Format.values();
		String[] fmt = new String [f.length];
		for(int i = 0; i < f.length; ++i) {
			fmt[i] = f[i].toString();
		}
		combo.setItems(fmt);
		combo.select(0);
		
		Button saveButton = new Button(comp, SWT.PUSH);
		saveButton.setText("Save");
		
		saveButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filename = text.getText();
				int w = Integer.parseInt(width.getText().trim());
				int h = Integer.parseInt(height.getText().trim());
				String fmt = combo.getText();
				String outputDirectory = outDirectory.getText();
				
				saveWindow.save(filename, w, h, Format.valueOf(fmt), outputDirectory);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

}
