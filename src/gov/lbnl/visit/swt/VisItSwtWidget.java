package gov.lbnl.visit.swt;

import gov.lbnl.visit.swt.VisItSwtConnection.VISIT_CONNECTION_TYPE;
import gov.lbnl.visit.swt.VisItSwtConnection.VisualizationUpdateCallback;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import visit.java.client.AttributeSubject;
import visit.java.client.AttributeSubject.AttributeSubjectCallback;
import visit.java.client.ViewerMethods;
import visit.java.client.ViewerState;


/**
 * This class extends SWT Canvas to provide an area to display images rendered
 * by an external VisIt client.
 * 
 * @authors hkq, tnp
 */
public class VisItSwtWidget extends Canvas implements Listener,
		AttributeSubjectCallback, VisualizationUpdateCallback {

	/**
	 * The database metadata.
	 */
	private FileInfo openDatabaseInfo;

	/**
	 * The composite to contain the canvas.
	 */
	private Composite shell;

	/**
	 * 
	 */
	private Image image;

	/**
	 * Connection to VisIt.
	 */
	private VisItSwtConnection visitConnection;
	
	/**
	 * windowId for this connection. 
	 */
	private int visitWindowId;
	
	/**
	 * The trackball used to control the VisIt image.
	 */
	//Trackball ball = new Trackball();

	/**
	 * 
	 */
	private boolean initialized = false;
	

	/**
	 * files
	 */
	ArrayList<String> files;
	
	/**
	 * dirs
	 */
	ArrayList<String> dirs;
	

	/**
	 * The constructor
	 * 
	 * @param visComp
	 *            The parent composite for this Canvas.
	 * @param x
	 *            The SWT constant style to be applied to the Canvas.
	 */
	public VisItSwtWidget(Composite visComp, int x) {

		// Call Canvas' constructor
		super(visComp, x);

		// Get the Shell of the parent Composite
		shell = visComp.getShell();
		// shell.getDisplay();
		
		files = new ArrayList<String>();
		dirs = new ArrayList<String>();

		// Initialize the default image
		image = shell.getDisplay().getSystemImage(SWT.ICON_ERROR);

		// Register this as an SWT.Paint listener
		addListener(SWT.Paint, this);
	}
	
	/**
	 * setVisItSwtConnection
	 * 
	 * @param VisItSwtConnection
	 *            Connection
	 * @param windowId
	 *            The Window Id to draw.
	 */
	public void setVisItSwtConnection(VisItSwtConnection conn, int windowId, int windowWidth, int windowHeight) {
		visitConnection = conn;
		visitWindowId = windowId;
		
		//ball.screenWidth = windowWidth;
		//ball.screenHeight = windowHeight;
		//ball.radius = ball.screenWidth / 2;

		visitConnection.registerVisualization(VISIT_CONNECTION_TYPE.IMAGE, visitWindowId, this);
		visitConnection.registerCallback("avtDatabaseMetaData", this);
		visitConnection.registerCallback("QueryAttributes", this);
		initialized = true;
		

	}

	public boolean hasInitialized() {
		return initialized;
	}
	
	/**
	 * This operation draws the image to the Canvas.
	 * 
	 * @param e
	 *            The Event triggering the need to redraw the Image in the
	 *            Canvas
	 */
	public void handleEvent(Event e) {

		GC gc = e.gc;
		gc.drawImage(image, 0, 0);

		return;
	}

	/**
	 * 
	 * @param arg0
	 */
	private void updateDatabaseMetaData(AttributeSubject arg0) {

		FileInfo fi = new FileInfo();

		String filename = arg0.get("databaseName").getAsString();
		String filetype = arg0.get("fileFormat").getAsString();
		String description = arg0.get("databaseComment").getAsString();
		
		ArrayList<String> fi_meshes = new ArrayList<String>();
		ArrayList<String> fi_scalars = new ArrayList<String>();
		ArrayList<String> fi_vectors = new ArrayList<String>();
		ArrayList<String> fi_materials = new ArrayList<String>();
		
		JsonArray meshes = arg0.get("meshes").getAsJsonArray();
		for (int i = 0; i < meshes.size(); ++i) {
			JsonObject mesh = meshes.get(i).getAsJsonObject();
			String name = arg0.getAttr(mesh, "name").getAsString();
			fi_meshes.add(name);
		}

		JsonArray scalars = arg0.get("scalars").getAsJsonArray();
		for (int i = 0; i < scalars.size(); ++i) {
			JsonObject scalar = scalars.get(i).getAsJsonObject();
			String name = arg0.getAttr(scalar, "name").getAsString();
			fi_scalars.add(name);
		}

		JsonArray vectors = arg0.get("vectors").getAsJsonArray();
		for (int i = 0; i < vectors.size(); ++i) {
			JsonObject vector = vectors.get(i).getAsJsonObject();
			String name = arg0.getAttr(vector, "name").getAsString();
			fi_vectors.add(name);
		}

		JsonArray materials = arg0.get("materials").getAsJsonArray();
		for (int i = 0; i < materials.size(); ++i) {
			JsonObject material = materials.get(i).getAsJsonObject();
			String name = arg0.getAttr(material, "name").getAsString();
			fi_materials.add(name);
		}
	
		fi.setFileName(filename);
		fi.setFileType(filetype);
		fi.setFileDescription(description);
		
		fi.setMeshes(fi_meshes);
		fi.setScalars(fi_scalars);
		fi.setVectors(fi_vectors);
		fi.setMaterials(fi_materials);
		
		openDatabaseInfo = fi;
	}
	
	synchronized public void updateQuery(AttributeSubject arg0) {
		String defaultName = arg0.get("defaultName").getAsString();
		JsonArray defaultVars = arg0.get("defaultVars").getAsJsonArray();
		
		if(!defaultName.equals("FileList"))
			return;
		
		dirs.clear();
		files.clear();
		
		Gson gson = new Gson();
		for(int i = 0; i < defaultVars.size(); ++i) {
			String filelist = defaultVars.get(i).getAsString();
			
			JsonObject obj;

			try {
				obj = gson.fromJson(filelist, JsonObject.class);
			}
			catch(JsonSyntaxException e) {
				System.out.println("failed on " + filelist);
				continue;
			}
			
			System.out.println("obj: " + obj.get("dirs"));
			JsonArray d = obj.get("dirs").getAsJsonArray();
			JsonArray f = obj.get("files").getAsJsonArray();
			
			for(int j = 0; j < d.size(); ++j) {
				dirs.add(d.get(j).getAsString());
			}
			
			for(int j = 0; j < f.size(); ++j) {
				files.add(f.get(j).getAsString());
			}	
		}
	}
	
	/**
	 * 
	 */
	@Override
	synchronized public void update(AttributeSubject arg0) {
		// System.out.println(arg0.getData().toString());

		String typename = arg0.getTypename();

		if (typename.equals("avtDatabaseMetaData")) {
			updateDatabaseMetaData(arg0);
		}
		
		if(typename.equals("QueryAttributes")) {
			updateQuery(arg0);
		}
	}

	
	/**
	 * 
	 * @return
	 */
	public ViewerMethods getViewerMethods() {
		return visitConnection.getViewerMethods(); //client.getViewerMethods();
	}

	/**
	 * 
	 * @return
	 */
	public ViewerState getViewerState() {
		return null; //client.getViewerState();
	}

//	/**
//	 * 
//	 */
//	public void invertBackground() {
//		if (!hasInitialized())
//			return;
//
//		ViewerMethods methods = client.getViewerMethods();
//		methods.InvertBackgroundColor();
//	}

	/**
	 * 
	 * @param windowId
	 * @param w
	 * @param h
	 */
//	public void resizeWindow(int windowId, int w, int h) {
//		if (!hasInitialized())
//			return;
//
//		ViewerMethods methods = client.getViewerMethods();
//		methods.ResizeWindow(windowId, w, h);
//	}

	/**
	 * 
	 */
//	public void openDatabase(String filename) {
//		if (!hasInitialized())
//			return;
//
//		ViewerMethods methods = client.getViewerMethods();
//		methods.OpenDatabase(filename);
//
//		// Get filename for the database..
//		methods.GetMetaData(filename);
//
//		sync();
//	}

	/**
	 * 
	 */
//	public void addPlot(String plot_name, String plot_var) {
//		if (!hasInitialized())
//			return;
//
//		ViewerMethods methods = client.getViewerMethods();
//		methods.AddPlot(plot_name, plot_var);
//	}

	/**
	 * 
	 */
//	public void drawPlots() {
//		if (!hasInitialized())
//			return;
//
//		ViewerMethods methods = client.getViewerMethods();
//		methods.DrawPlots();
//	}

	/**
	 * 
	 */
//	public void hideShowPlot(int index) {
//		if (!hasInitialized())
//			return;
//
//		ViewerMethods methods = client.getViewerMethods();
//		methods.SetActivePlots(index);
//		methods.HideActivePlots();
//
//		ball.resetModelMatrices();
//	}

	/**
	 * 
	 */
//	public void deletePlots() {
//		if (!hasInitialized())
//			return;
//
//		ViewerMethods methods = client.getViewerMethods();
//		methods.DeleteActivePlots();
//
//		ball.resetModelMatrices();
//	};

	/**
	 * 
	 */
//	public void resetView() {
//		if (!hasInitialized())
//			return;
//
//		ViewerMethods methods = client.getViewerMethods();
//		methods.ResetView();
//
//		ball.resetModelMatrices();
//	}

	/**
	 * 
	 * @return
	 */
	public FileInfo getFileInfo() {
		return openDatabaseInfo;
	}

	/**
	 * 
	 * @param direction
	 */
	public void zoom(String direction) {
		//if (direction == "in")
		//	ball.wheelIn();
		//if (direction == "out")
		//	ball.wheelOut();
		getViewerMethods().updateMouseActions(visitWindowId, 
										       "Middle", 0, 0, 0, 
										       direction.equals("in") ? 10 : -10);
		//getViewerMethods().setView3D();
		getViewerMethods().drawPlots();
	}

	/**
	 * 
	 * @param x
	 * @param y
	 */
	int start_x, start_y;
	public void mouseStart(int x, int y) {
		if(visitConnection == null || visitConnection.hasInitialized() == false) {
			return;
		}
		start_x = x;
		start_y = y;
		
		//ball.moveStart(x, y);

		// Transformation.VectorN newViewUp = ball.modelMat.get(1);
		// Transformation.VectorN newViewNormal = ball.modelMat.get(2);
		//
		// Vector<Double> up = new Vector<Double>();
		// Vector<Double> normal = new Vector<Double>();
		//
		// for (int i = 0; i < 3; ++i) {
		// up.add(newViewUp.get(i));
		// normal.add(newViewNormal.get(i));
		// }
		//
		// client.getViewerMethods().UpdateView(up, normal);
	}

	/**
	 * 
	 * @param x
	 * @param y
	 */
	public void mouseMove(int x, int y) {
		if(visitConnection == null || visitConnection.hasInitialized() == false) {
			return;
		}
		
		//ball.move(x, y);
	}

	/**
	 * 
	 * @param x
	 * @param y
	 */
	public void mouseStop(int x, int y) {
		
		if(visitConnection == null || visitConnection.hasInitialized() == false) {
			return;
		}

		getViewerMethods().updateMouseActions(visitWindowId, "Left", start_x, start_y, x, y);
		//ball.moveStop(x, y);

		//System.out.println(ball.modelMat);
//		Transformation.VectorN newViewUp = ball.modelMat.get(1);
//		Transformation.VectorN newViewNormal = ball.modelMat.get(2);
//
//		Vector<Double> up = new Vector<Double>();
//		Vector<Double> normal = new Vector<Double>();
//
//		for (int i = 0; i < 3; ++i) {
//			up.add(newViewUp.get(i));
//			normal.add(newViewNormal.get(i));
//		}
//
//		getViewerMethods().updateView(up, normal);
	}

	@Override
	public void update(VISIT_CONNECTION_TYPE type, byte[] rawData) {
		
		if(type != VISIT_CONNECTION_TYPE.IMAGE) {
			return;
		}
		
		final byte[] output = rawData;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				ByteArrayInputStream bis = new ByteArrayInputStream(
						output);
				image = new Image(shell.getDisplay(), bis);
				redraw();
			}
		});
	}
	
	public FileTreeLazy GetFileTree(Display display) {
		return new FileTreeLazy(display);
	}
	
	public class FileTreeLazy {
		  
		Display disp;
		public FileTreeLazy(Display display) {
			//super(shell, SWT.NONE);
			disp = display;
		}
		
//		public FileTreeLazy(Shell shell, int flags) {
//			super(shell, flags);
//		}
		
		public void show() {
		  //final Display display = new Display ();
		  final Shell shell = new Shell (disp);
		  shell.setText ("Remote File List");
		  shell.setLayout (new FillLayout ());
		  
		  final Tree tree = new Tree (shell, SWT.BORDER);
		  
		  getViewerMethods().getFileList("localhost", ".");
		  
		  for (int i=0; i<dirs.size(); i++) 
		  {
		    TreeItem root = new TreeItem (tree, 0);
		    root.setText (dirs.get(i));
		    root.setData (dirs.get(i));
		    new TreeItem (root, 0);
		  }
		  
		  for (int i=0; i<files.size(); i++) 
		  {
			  TreeItem root = new TreeItem (tree, 0);
			  root.setText (files.get(i));
			  root.setData (files.get(i));
			  //new TreeItem (root, 0);
		  }

		  tree.addListener (SWT.Expand, new Listener () {
		    public void handleEvent (final Event event) {
		      final TreeItem root = (TreeItem) event.item;
		      
		      TreeItem [] items = root.getItems ();
		      
		      for (int i= 0; i<items.length; i++) {
		        if (items [i].getData () != null) return;
		        items [i].dispose ();
		      }
		  
		      String file = (String) root.getData ();
		      getViewerMethods().getFileList("localhost", file);

	    	  tree.removeAll();
		      
		      for (int i=0; i<dirs.size(); i++) {
				    TreeItem item;
				    item = new TreeItem (tree, 0);
				    item.setText (dirs.get(i));
				    item.setData (dirs.get(i));
				    new TreeItem (item, 0);
				  }
				  
		      for (int i= 0; i<files.size(); i++) 
		      {
		    	  TreeItem item;
		    	  item = new TreeItem (tree, 0);
		    	  item.setText (files.get(i));
		    	  item.setData (files.get(i));
		    	  
		      }
		      
		      tree.redraw();
		    }
		  });
		  
		  Point size = tree.computeSize (300, SWT.DEFAULT);
		  int width = Math.max (300, size.x);
		  int height = Math.max (300, size.y);
		  shell.setSize (shell.computeSize (width, height));
		  shell.open ();
//		  while (!shell.isDisposed ()) {
//		    if (!display.readAndDispatch ()) display.sleep ();
//		  }
//		  display.dispose ();
		}
		}
	
	static public void main(String[] args) {
		Display display = new Display();
		
		
		Shell shell = new Shell(display);
		VisItSwtLaunchWizard wizard = new VisItSwtLaunchWizard();
		WizardDialog dialog = new WizardDialog(shell, wizard);

		if(dialog.open() == Window.CANCEL)
			return;
		
		/// input..
		HashMap<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("username", "user1");
        inputMap.put("password", wizard.getPage().getPassword());
        inputMap.put("dataType", "image");
        inputMap.put("windowWidth", "1340");
        inputMap.put("windowHeight", "1020");
        inputMap.put("windowId", "1");
        inputMap.put("gateway", wizard.getPage().getGateway());
        inputMap.put("localGatewayPort", wizard.getPage().getGatewayPort());
        inputMap.put("useTunneling", wizard.getPage().getUseTunneling());
        inputMap.put("url", wizard.getPage().getHostname());
        inputMap.put("port", wizard.getPage().getVisItPort());
        inputMap.put("visDir", wizard.getPage().getVisItDir());
        inputMap.put("isRemote", wizard.getPage().getIsRemote());
        
        
        VisItSwtConnection vizConnection = new VisItSwtConnection(new Shell(display));
        
        /// parse parameters..
        String username = inputMap.get("username");
        String password = inputMap.get("password");
        String dataType = inputMap.get("dataType");
        
        int windowId = Integer.parseInt(inputMap.get("windowId"));
        int windowWidth = Integer.parseInt(inputMap.get("windowWidth"));
        int windowHeight = Integer.parseInt(inputMap.get("windowHeight"));
        String gateway = inputMap.get("gateway");
        
        int localGatewayPort = -1;
        
        if(!gateway.isEmpty()) {
        	localGatewayPort = Integer.parseInt(inputMap.get("localGatewayPort"));
        }
        
        String useTunneling = inputMap.get("useTunneling");
        String url = inputMap.get("url");
        
        int port = Integer.parseInt(inputMap.get("port"));
        String visDir = inputMap.get("visDir");
        boolean isRemote = Boolean.valueOf(inputMap.get("isRemote"));

        // Set the parameters on the widget
        vizConnection.setParameters(username,
                                    password,
                                    VisItSwtConnection.VISIT_CONNECTION_TYPE.IMAGE,
                                    windowWidth,
                                    windowHeight,
                                    windowId);

        // Setup a remote gateway if needed
        if (!gateway.isEmpty()) {
            vizConnection.setGateway(gateway, localGatewayPort);
        }

        // Enable tunneling if needed
        vizConnection.useTunneling(Boolean.valueOf(useTunneling));

        // Launch the VisIt widget
        System.out.println(url + " "  + port + " " + password + " " + visDir + " " + isRemote);
        boolean result = vizConnection.launch(url, port, password, visDir,
                isRemote);
		
        // failed connection, etc.)
        if (!result) {
            if (isRemote) {
                MessageDialog.openError(shell,
                        "Failed to Connect to VisIt",
                        "Unable to connect to a running VisIt client.");
            } else {
                MessageDialog.openError(shell,
                        "Failed to Launch VisIt",
                        "VisIt has failed to launch.");
            }
        }
        
        VisItSwtWidget widget = new VisItSwtWidget(shell, SWT.BORDER);
        widget.setVisItSwtConnection(vizConnection, 1, 400, 400);
        shell.open();

        while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
	}
}
