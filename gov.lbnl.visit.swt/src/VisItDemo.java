

import java.net.ConnectException;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.jcraft.jsch.Session;

import gov.lbnl.visit.swt.VisItSwtConnection;
import gov.lbnl.visit.swt.VisItSwtWidget;
import gov.lbnl.visit.swt.VisItSwtConnection.VISIT_CONNECTION_TYPE;
import gov.lbnl.visit.swt.VisItSwtConnection.VisItMessageCallback;
import gov.lbnl.visit.swt.widgets.AdvancedWidgets;
import gov.lbnl.visit.swt.widgets.PlotListWidget;
import gov.lbnl.visit.swt.widgets.TimeSliderWidget;
import gov.lbnl.visit.swt.widgets.PlotListWidget.PlotListWidgetCallback;
import visit.java.client.components.MessageAttributes.Severity;

public class VisItDemo extends Composite {

	  VisItSwtConnection conn = null;
	  
	  Text hostname;
	  
	  PlotListWidget plw;
	  TimeSliderWidget ts;
	  TabFolder windowFolders;
	  
	  Label status;
	  
	  Shell awShell;
	  AdvancedWidgets aw;
	  
	  int windowWidth = 300;
	  int windowHeight = 300;
	  
	  boolean autoApply = true;

	  public VisItDemo(Composite parent, int style) {
		  super(parent, style);
		  createPartControl(this);
	  }

	  public void OpenDatabase(String filename) {
		  conn.getViewerMethods().openDatabase(filename, 0, false, "");
	  }
	  
	  public void OpenDatabase(String filename, int timeStep) {
		  conn.getViewerMethods().openDatabase(filename, timeStep, false, "");
	  }
	  
	  public void CloseDatabase(String filename) {
		  conn.getViewerMethods().deleteActivePlots();
		  conn.getViewerMethods().closeDatabase(filename); 
		  DrawPlots();
		}
	  
	  public void DrawPlots() {
		  conn.getViewerMethods().drawPlots();  
	  }
	  
	  public void launch(Session session, String dir, String username) throws Exception {
	    String password = "notset";
	    int port = -1;
	    
	    conn = new VisItSwtConnection(new Shell());

	    conn.setParameters(username, 
	        password, 
	        VISIT_CONNECTION_TYPE.IMAGE, 
	        windowWidth, 
	        windowHeight, 1);

	    //conn.useTunneling(true);

	    /// set up initial connection..

	    if(session == null) {
	    	conn.launch("localhost", -1, password, dir, false);
	    } else {
	    	conn.launch(session, dir, port, password);
	    }

	    /// set up connection for the first window..
	    try {
	    	VisItSwtWidget widget = addVisItWindow();
		    
		    widget.setVisItSwtConnection(conn, 1, windowWidth, windowHeight);
	    	
	    	Rectangle rect = widget.getClientArea();
	    	widget.getViewerMethods().resizeWindow(widget.getWindowId(), rect.width,
	    			rect.height);
	    	cleanupCanvas(widget.getWindowId());    	
	    } catch (ConnectException e1) {
	    	e1.printStackTrace();
	    	return;
	    }

	    /// generic cleanup 
	    cleanupCanvas(-1);

	    ///TESTING LAUNCH OF REMOTE ENGINE...
	    //String commands = "";
	    //commands = "OpenComputeEngine(\"hopper.nersc.gov\", (\"-nn\", \"1\", \"-np\", \"24\", \"-l\", \"qsub/aprun\"))";
	    //conn.getViewerMethods().processCommands(commands);

	    ts.setVisItSwtConnection(conn);
	    plw.setVisItSwtConnection(conn);
	    aw.setVisItSwtConnection(conn);
	    
	    conn.registerVisItMessageCallback(new VisItMessageCallback() {
			
			@Override
			public void message(Severity severity, String message) {
				final String msg = message;
				final Severity s = severity;
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						if(status.isDisposed()) {
							return;
						}
						
						status.setText(msg);
						if( s ==  Severity.Error )
							status.setForeground(new Color(Display.getDefault(), new RGB(255,0,0)));
						else if(s == Severity.Warning)
							status.setForeground(new Color(Display.getDefault(), new RGB(255,0,255)));
						else if(s == Severity.Information)
							status.setForeground(new Color(Display.getDefault(), new RGB(0,0,255)));
						else {
							status.setForeground(new Color(Display.getDefault(), new RGB(0,0,0)));	
						}
					}
				});
			}
		});
	  }

	  public void cleanupCanvas(int windowId) {

		  if(windowId > 0)
			  conn.getViewerMethods().setActiveWindow(windowId);
		  
		  String commands = "annot = GetAnnotationAttributes()\n";
		  commands += "annot.userInfoFlag = 0\n";
		  commands += "annot.databaseInfoFlag = 0\n";
		  commands += "SetAnnotationAttributes(annot);\n";
		  conn.getViewerMethods().processCommands(commands);
	  }
	  
	  private VisItSwtWidget addVisItWindow() {
		  VisItSwtWidget widget = new VisItSwtWidget(windowFolders, SWT.BORDER);
		  widget.useDefaultMouseManager();
		  
		  TabItem item = new TabItem(windowFolders, SWT.BORDER);
		  widget.setLayout(new FillLayout());
		  
		  
		  item.setText("VisIt Window: " + (windowFolders.getItemCount()-1));
		  item.setControl(widget);
		  
		  /// set this as the new selection..
		  windowFolders.setSelection(windowFolders.getItemCount()-1);
		  
		  return widget;
	  }

	  public void drawSelectedPlot(String plot, String var) {
		  int index = windowFolders.getSelectionIndex();
		  if(index == -1) return;

		  VisItSwtWidget widget = (VisItSwtWidget)windowFolders.getItems()[index].getControl();
		  if(widget == null) return;
		  //widget.getViewerMethods().deleteActivePlots();
		  widget.getViewerMethods().addPlot(plot, var);
		  if(autoApply) {
			  widget.getViewerMethods().drawPlots();
		  }
	  }
	  
	  public void setWindowColormap(String plotSelection, String colorSelection) {
		  int index = windowFolders.getSelectionIndex();
		  if(index == -1) return;

		  VisItSwtWidget widget = (VisItSwtWidget)windowFolders.getItems()[index].getControl();
		 
		  String commands = "";

		  if(plotSelection.startsWith("Pseudocolor")) {
			  commands = "pa = GetPlotOptions()\n";
			  commands += "pa.colorTableName = \"" + colorSelection + "\"\n";
			  commands += "SetPlotOptions(pa)\n";
			  widget.getViewerMethods().processCommands(commands);	
		  } else if(plotSelection.startsWith("Contour")) {

		  } else if(plotSelection.startsWith("Volume")){
			  commands = "va = GetPlotOptions()\n";
			  commands += "ccpl = GetColorTable(\"" + colorSelection + "\")\n";
			  commands += "va.colorControlPoints = ccpl\n";
			  commands += "SetPlotOptions(va)\n";

			  widget.getViewerMethods().processCommands(commands);	
		  }	
	  }
	  
	  /**
	   * Create contents of the view part.
	   * @param parent
	   */
	  public void createPartControl(Composite parent) {
	    
	    Composite container = new Composite(parent, SWT.NONE);
	    container.setLayout(new GridLayout(1, true));

	    Composite comboComp = new Composite(container, SWT.BORDER);
	    RowLayout r = new RowLayout(SWT.HORIZONTAL);
		r.fill = true;
	    comboComp.setLayout(r);
	    comboComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	    
	    Button autoUpdate = new Button(comboComp, SWT.CHECK);
	    autoUpdate.setText("Auto Update");
	    autoUpdate.setSelection(autoApply);
	    
	    Button drawPlots = new Button(comboComp, SWT.PUSH | SWT.FILL);
		drawPlots.setText("Draw Plots");		
	    
		ts = new TimeSliderWidget(comboComp, SWT.FILL, conn);

//		Button addWindow = new Button(comboComp, SWT.PUSH);
//		addWindow.setText("Add Window");
			
		Button advanced = new Button(comboComp, SWT.PUSH | SWT.FILL);
		advanced.setText("Advanced...");
		
		Button resetView = new Button(comboComp, SWT.PUSH | SWT.FILL);
		resetView.setText("Reset View");

		Button xy = new Button(comboComp, SWT.PUSH | SWT.FILL);
		xy.setText("XY");

		Button yz = new Button(comboComp, SWT.PUSH | SWT.FILL);
		yz.setText("YZ");
		
		Button xz = new Button(comboComp, SWT.PUSH | SWT.FILL);
		xz.setText("XZ");
		
		Button zoomIn = new Button(comboComp, SWT.PUSH | SWT.FILL);
		zoomIn.setText("+");

		Button zoomOut = new Button(comboComp, SWT.PUSH | SWT.FILL);
		zoomOut.setText("-");
		
		awShell = new Shell(Display.getDefault());
		awShell.setLayout(new GridLayout(1, true));
		
		aw = new AdvancedWidgets(awShell, SWT.NONE, conn);
		aw.setLayout(new GridLayout(1,true));
		aw.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
	    autoUpdate.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button autoUpdate = (Button)e.getSource();
				autoApply = autoUpdate.getSelection();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	    
	    awShell.addListener(SWT.Close, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				event.doit = false;
				awShell.setVisible(false);
			}
		});
		
		drawPlots.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				DrawPlots();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

//		addWindow.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				try {	
//					VisItSwtWidget widget = addVisItWindow();
//					widget.setVisItSwtConnection(conn, -1, windowWidth, windowHeight);
//					Rectangle rect = widget.getClientArea();
//					widget.getViewerMethods().resizeWindow(widget.getWindowId(), rect.width,
//							rect.height);
//					cleanupCanvas(widget.getWindowId());
//				} catch (ConnectException e1) {
//					e1.printStackTrace();
//					return;
//				}
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				widgetSelected(e);
//			}
//		});

		advanced.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				awShell.setVisible(true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		resetView.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = windowFolders.getSelectionIndex();
				VisItSwtWidget widget = (VisItSwtWidget)windowFolders.getItems()[index].getControl();
				//int visitWindowId = widget.getWindowId();
		        widget.getViewerMethods().resetView();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		zoomIn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = windowFolders.getSelectionIndex();
				VisItSwtWidget widget = (VisItSwtWidget)windowFolders.getItems()[index].getControl();
				int visitWindowId = widget.getWindowId();
		        widget.getViewerMethods().updateMouseActions(visitWindowId,
		                "WheelUp",
		                new double[] { 0, 0 }, new double[] { 0, 0 }, false, false);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		zoomOut.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = windowFolders.getSelectionIndex();
				VisItSwtWidget widget = (VisItSwtWidget)windowFolders.getItems()[index].getControl();
				int visitWindowId = widget.getWindowId();
		        widget.getViewerMethods().updateMouseActions(visitWindowId,
		                "WheelDown",
		                new double[] { 0, 0 }, new double[] { 0, 0 }, false, false);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		xy.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = windowFolders.getSelectionIndex();
				VisItSwtWidget widget = (VisItSwtWidget)windowFolders.getItems()[index].getControl();
				ArrayList<Double> up = new ArrayList<Double>();
				ArrayList<Double> normal = new ArrayList<Double>();
				
				up.add(1.0);
				up.add(0.0);
				up.add(0.0);
				
				normal.add(0.0);
				normal.add(0.0);
				normal.add(1.0);
		    
				widget.getViewerMethods().updateView(up, normal);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		yz.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = windowFolders.getSelectionIndex();
				VisItSwtWidget widget = (VisItSwtWidget)windowFolders.getItems()[index].getControl();
				//int visitWindowId = widget.getWindowId();
				ArrayList<Double> up = new ArrayList<Double>();
				ArrayList<Double> normal = new ArrayList<Double>();
				
				up.add(0.0);
				up.add(1.0);
				up.add(0.0);
				
				normal.add(1.0);
				normal.add(0.0);
				normal.add(0.0);
		    
				widget.getViewerMethods().updateView(up, normal);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		xz.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = windowFolders.getSelectionIndex();
				VisItSwtWidget widget = (VisItSwtWidget)windowFolders.getItems()[index].getControl();
				//int visitWindowId = widget.getWindowId();
				ArrayList<Double> up = new ArrayList<Double>();
				ArrayList<Double> normal = new ArrayList<Double>();
				
				up.add(1.0);
				up.add(0.0);
				up.add(0.0);
				
				normal.add(0.0);
				normal.add(1.0);
				normal.add(0.0);
		    
				widget.getViewerMethods().updateView(up, normal);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		SashForm form = new SashForm(container,SWT.HORIZONTAL);
		form.setLayout(new FillLayout());
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		plw = new PlotListWidget(form, SWT.BORDER);
		plw.setLayout(new FillLayout());
		
		plw.register(new PlotListWidgetCallback() {
			
			@Override
			public void setColormap(String plotType, String colormap) {
				final String p = plotType;
				final String c = colormap;
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						setWindowColormap(p, c);
					}
				});
			}
			
			@Override
			public void drawPlot(String plot, String variable) {
				final String p = plot;
				final String v = variable;
				
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						drawSelectedPlot(p,v);
					}
				});
			}
		});

		////
		windowFolders = new TabFolder(form, SWT.BORDER);
		windowFolders.setLayout(new FillLayout());
		TabItem addItemTab = new TabItem(windowFolders, SWT.NONE);
		addItemTab.setText("+");
		
		windowFolders.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(windowFolders.getSelectionIndex() == 0) {
					//e.doit = false;
					try {	
						VisItSwtWidget widget = addVisItWindow();
						widget.setVisItSwtConnection(conn, -1, windowWidth, windowHeight);
						Rectangle rect = widget.getClientArea();
						widget.getViewerMethods().resizeWindow(widget.getWindowId(), rect.width,
								rect.height);
						cleanupCanvas(widget.getWindowId());
					} catch (ConnectException e1) {
						e1.printStackTrace();
						return;
					}
					  
					//windowFolders.setSelection(windowFolders.getItemCount()-1);
					return;
				}
				
				VisItSwtWidget widget = (VisItSwtWidget)windowFolders.getItems()[windowFolders.getSelectionIndex()].getControl();
				
				if(widget != null && widget.hasInitialized()) {
					widget.getViewerMethods().setActiveWindow(widget.getWindowId());	
					widget.getViewerMethods().drawPlots();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		form.setWeights(new int[] {30, 70});
		
		status = new Label(container, SWT.BORDER);
		status.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		status.setText("Status...");
		
		/**
		 * If parent gets disposed close connection..
		 */
		parent.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				//System.out.println("Disposing..");
				if(conn != null) {
					try {
					conn.cleanup();
					conn.close();
					conn = null;
					}
					catch(Exception ex) {
						///catch
						System.out.println(ex.getMessage());
					}
				}
			}
		});
	  }
	
	public static void main(String[] args) {
		Display display = Display.getDefault();
		
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		
		VisItDemo demo = new VisItDemo(shell, SWT.BORDER);
		demo.setLayout(new FillLayout());
		demo.setLayoutData(new FillLayout());
		
		try {
			demo.launch(null, "/Users/hari/Code/Work/VisIt/install/bin", "user");
			demo.OpenDatabase("/Users/hari/visit_data_files/noise.silo");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		shell.setSize(800, 800);
		shell.open();
		
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
	}

}
