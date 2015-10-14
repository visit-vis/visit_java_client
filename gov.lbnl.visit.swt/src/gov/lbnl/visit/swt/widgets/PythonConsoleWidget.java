package gov.lbnl.visit.swt.widgets;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import gov.lbnl.visit.swt.VisItSwtConnection;
import visit.java.client.components.PythonConsole;
import visit.java.client.components.PythonConsole.PythonConsoleCallback;

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
//   public PythonConsoleWidget(Composite parent, int style) {
//       this(parent, style, null);
//   }

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
       
       console = new PythonConsole(conn.getViewerMethods());
       setupUI();
   }
   
   
   
	private void setupUI() {
		new Label(this, SWT.NONE).setText("History:");

		final Text history = new Text(this, SWT.MULTI | SWT.BORDER | 
												SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
		history.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));


		final PythonConsoleCallback pcb = new PythonConsoleCallback() {
			
			public void response(List<String> r) {
				
				final List<String> vec = r;
				
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						for (int i = 0; i < vec.size(); ++i) {
							if(vec.get(i).startsWith("#"))
								continue;
							
							history.append(vec.get(i) + "\n");
						}

						// Move the cursor to the end of the console
						history.setSelection(history.getText().length());
					}
				});
			}
		};
		
		console.register(pcb);

		// Create a multiple-line text field
		new Label(this, SWT.NONE).setText("Commands:");
		final Text input = new Text(this, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		input.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    
	    Composite comp = new Composite(this, SWT.NONE);
	    comp.setLayout(new FillLayout());
	    
	    Button interpret = new Button(comp, SWT.BORDER | SWT.PUSH);
	    interpret.setText("Interpret");

	    Button startRecording = new Button(comp, SWT.BORDER | SWT.PUSH);
	    startRecording.setText("Start Recording");

	    Button stopRecording = new Button(comp, SWT.BORDER | SWT.PUSH);
	    stopRecording.setText("Stop Recording");

	    Button currentState = new Button(comp, SWT.BORDER | SWT.PUSH);
	    currentState.setText("Write Current State");
	    
	    Button loadFile = new Button(comp, SWT.BORDER | SWT.PUSH);
	    loadFile.setText("Load File");
	    
	    interpret.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String text = input.getText();
				input.setText("");
				
				history.append(text + "\n");
				console.processCommands(text);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
	    
	    startRecording.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				console.startRecording();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});

	    stopRecording.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				console.stopRecording();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
	    
	    currentState.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				console.writeCurrentState();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
	    
	    loadFile.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
			    FileDialog dialog = new FileDialog(getShell(), SWT.OPEN | SWT.MULTI);

				String[] filterNames = new String[] { "Python Files" };
				String[] filterExtensions = new String[] { "*.py" };

				// Set the dialog's file filters.
				dialog.setFilterNames(filterNames);
				dialog.setFilterExtensions(filterExtensions);


				// If a file was selected in the dialog, process it.
				if (dialog.open() == null) { 
					return;
				}
				
				// Get the selected file(s)
				String[] files = dialog.getFileNames();

				if(files == null) {
					return;
				}
				String separator = System.getProperty("file.separator");
				for (String file : files) {
					try {
						// Get the full file path
						String path = dialog.getFilterPath()
								+ separator + file;
						// Initialize the readers and buffers
						FileReader fReader = new FileReader(path);
						BufferedReader bReader = new BufferedReader(fReader);
						
						StringBuffer strBuffer = new StringBuffer();
						String line = bReader.readLine();
						while (line != null) {
							strBuffer.append(line + "\n");
							line = bReader.readLine();
						}

						// Close the reader
						bReader.close();
						
						// Convert the StringBuffer to a String
						String fullFileStr = strBuffer.toString().trim() + "\n";
						
						// Format the String of commands
						String text = ">>> " + fullFileStr;
						text = text.replace("\n", "\n>>> ");
						text = text.replace("\n>>>  ", "\n... ");
						history.append(text.trim() + "\n");
						// Call the VisIt widget method to process these
						// commands
						console.processCommands(fullFileStr);
					} catch (Exception e1) {
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	    
	    this.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				console.unregister(pcb);
				console.cleanup();
			}
		});
	}

}
