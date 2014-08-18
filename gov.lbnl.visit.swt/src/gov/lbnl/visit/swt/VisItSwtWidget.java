package gov.lbnl.visit.swt;

import gov.lbnl.visit.swt.VisItSwtConnection.VISIT_CONNECTION_TYPE;
import gov.lbnl.visit.swt.VisItSwtConnection.VisualizationUpdateCallback;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import visit.java.client.AttributeSubject;
import visit.java.client.AttributeSubject.AttributeSubjectCallback;
import visit.java.client.FileInfo;
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
	// Trackball ball = new Trackball();

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
		image = shell.getDisplay().getSystemImage(SWT.ICON_WORKING);

		// Register this as an SWT.Paint listener
		addListener(SWT.Paint, this);
	}

	public void activate() {
		getViewerMethods().setActiveWindow(visitWindowId);
	}

	/**
	 * setVisItSwtConnection
	 * 
	 * @param VisItSwtConnection
	 *            Connection
	 * @param windowId
	 *            The Window Id to draw.
	 * @throws Exception
	 */
	public void setVisItSwtConnection(VisItSwtConnection conn, int windowId,
			int windowWidth, int windowHeight) throws Exception {
		visitConnection = conn;
		visitWindowId = windowId;

		// / if window Id is -1 then add a new window
		if (visitWindowId == -1) {
			Vector<Integer> wIds = visitConnection.getWindowIds();

			if (wIds.size() == 0) {
				visitWindowId = 1; // /VisIt creates 1 window by default..
			} else {
				getViewerMethods().addWindow();
				Vector<Integer> nIds = visitConnection.getWindowIds();

				HashSet<Integer> s1 = new HashSet<Integer>();
				s1.addAll(nIds);
				s1.removeAll(wIds);

				// /there should be one left over..
				if (s1.size() != 1) {
					visitConnection = null;
					throw new Exception("Could not create new window");
				}

				visitWindowId = (int) s1.toArray()[0];
			}
		}

		System.out.println("Registering new window: " + visitWindowId);
		getViewerMethods().registerNewWindow(visitWindowId);

		visitConnection.registerVisualization(VISIT_CONNECTION_TYPE.IMAGE,
				visitWindowId, this);
		visitConnection.registerCallback("avtDatabaseMetaData", this);
		initialized = true;

		// getViewerMethods().resizeWindow(windowId, windowWidth, windowHeight);
		// / for now use static size that seems to give good performance
		// / the rendering below will re-interpolate

		// if(windowId == -1) {
		getViewerMethods().resizeWindow(visitWindowId, 500, 500);
		// }
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
	}

	/**
	 * 
	 * @return
	 */
	public ViewerMethods getViewerMethods() {
		return visitConnection.getViewerMethods(); // client.getViewerMethods();
	}

	/**
	 * 
	 * @return
	 */
	public ViewerState getViewerState() {
		return null; // client.getViewerState();
	}

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
		getViewerMethods().updateMouseActions(visitWindowId,
				direction.equals("in") ? "WheelUp" : "WheelDown", 0, 0, 0, 0,
				false, false);
		// getViewerMethods().drawPlots();
	}

	/**
	 * 
	 * @param x
	 * @param y
	 */
	boolean mousePressed = false;
	int start_x, start_y;

	public void mouseStart(int x, int y, boolean ctrl, boolean shift) {
		if (visitConnection == null
				|| visitConnection.hasInitialized() == false) {
			return;
		}
		start_x = x;
		start_y = y;
		mousePressed = true;
	}

	/**
	 * 
	 * @param x
	 * @param y
	 */
	public void mouseMove(int x, int y, boolean ctrl, boolean shift) {
		if (visitConnection == null
				|| visitConnection.hasInitialized() == false) {
			return;
		}

		// if (mousePressed) {
		// Point p = getSize();
		// p.x = p.x;
		// p.y = p.y;
		//
		// getViewerMethods().updateMouseActions(visitWindowId,
		// "LeftPress",
		// (double)start_x/(double)p.x,
		// (double)start_y/p.y,
		// (double)x/(double)p.x,
		// (double)y/(double)p.y,
		// ctrl, shift);
		//
		// getViewerMethods().updateMouseActions(visitWindowId,
		// "Move",
		// (double)start_x/(double)p.x,
		// (double)start_y/p.y,
		// (double)x/(double)p.x,
		// (double)y/(double)p.y,
		// ctrl, shift);
		//
		// getViewerMethods().updateMouseActions(visitWindowId,
		// "LeftRelease",
		// (double)start_x/(double)p.x,
		// (double)start_y/p.y,
		// (double)x/(double)p.x,
		// (double)y/(double)p.y,
		// ctrl, shift);
		//
		// getViewerMethods().forceRedraw(visitWindowId);
		//
		// start_x = p.x;
		// start_y = p.y;
		// }
	}

	/**
	 * 
	 * @param x
	 * @param y
	 */
	public void mouseStop(int x, int y, boolean ctrl, boolean shift) {

		if (visitConnection == null
				|| visitConnection.hasInitialized() == false) {
			return;
		}

		if (mousePressed) {
			Point p = getSize();
			p.x = p.x;
			p.y = p.y;

			getViewerMethods().updateMouseActions(visitWindowId, "LeftPress",
					(double) start_x / (double) p.x, (double) start_y / p.y,
					(double) x / (double) p.x, (double) y / (double) p.y, ctrl,
					shift);

			getViewerMethods().updateMouseActions(visitWindowId, "Move",
					(double) start_x / (double) p.x, (double) start_y / p.y,
					(double) x / (double) p.x, (double) y / (double) p.y, ctrl,
					shift);

			getViewerMethods().updateMouseActions(visitWindowId, "LeftRelease",
					(double) start_x / (double) p.x, (double) start_y / p.y,
					(double) x / (double) p.x, (double) y / (double) p.y, ctrl,
					shift);

			getViewerMethods().forceRedraw(visitWindowId);
		}
		mousePressed = false;
	}

	private Image resize(Display display, Image img, int width, int height) {
		Image scaled = new Image(display, width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(img, 0, 0, img.getBounds().width, img.getBounds().height,
				0, 0, width, height);
		gc.dispose();
		return scaled;
	}

	@Override
	public void update(VISIT_CONNECTION_TYPE type, byte[] rawData) {

		if (type != VISIT_CONNECTION_TYPE.IMAGE) {
			return;
		}

		final byte[] output = rawData;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				ByteArrayInputStream bis = new ByteArrayInputStream(output);
				Image img = new Image(shell.getDisplay(), bis);
				image = resize(shell.getDisplay(), img, getSize().x,
						getSize().y);
				img.dispose();
				redraw();
			}
		});
	}

	/*
	 * static public void main(String[] args) { Display display = new Display();
	 * 
	 * Shell shell = new Shell(display); VisItSwtLaunchWizard wizard = new
	 * VisItSwtLaunchWizard(); WizardDialog dialog = new WizardDialog(shell,
	 * wizard);
	 * 
	 * if (dialog.open() == Window.CANCEL) { return; } // / input..
	 * HashMap<String, String> inputMap = new HashMap<String, String>();
	 * inputMap.put("username", "user1"); inputMap.put("password",
	 * wizard.getPage().getPassword()); inputMap.put("dataType", "image");
	 * inputMap.put("windowWidth", "1340"); inputMap.put("windowHeight",
	 * "1020"); inputMap.put("windowId", "1"); inputMap.put("gateway",
	 * wizard.getPage().getGateway()); inputMap.put("localGatewayPort",
	 * wizard.getPage().getGatewayPort()); inputMap.put("useTunneling",
	 * wizard.getPage().getUseTunneling()); inputMap.put("url",
	 * wizard.getPage().getHostname()); inputMap.put("port",
	 * wizard.getPage().getVisItPort()); inputMap.put("visDir",
	 * wizard.getPage().getVisItDir()); inputMap.put("isRemote",
	 * wizard.getPage().getIsRemote());
	 * 
	 * VisItSwtConnection vizConnection = new VisItSwtConnection(new Shell(
	 * display));
	 * 
	 * // / parse parameters.. String username = inputMap.get("username");
	 * String password = inputMap.get("password"); //String dataType =
	 * inputMap.get("dataType");
	 * 
	 * int windowId = Integer.parseInt(inputMap.get("windowId")); int
	 * windowWidth = Integer.parseInt(inputMap.get("windowWidth")); int
	 * windowHeight = Integer.parseInt(inputMap.get("windowHeight")); String
	 * gateway = inputMap.get("gateway");
	 * 
	 * int localGatewayPort = -1;
	 * 
	 * if (!gateway.isEmpty()) { localGatewayPort = Integer.parseInt(inputMap
	 * .get("localGatewayPort")); }
	 * 
	 * String useTunneling = inputMap.get("useTunneling"); String url =
	 * inputMap.get("url");
	 * 
	 * int port = Integer.parseInt(inputMap.get("port")); String visDir =
	 * inputMap.get("visDir"); boolean isRemote =
	 * Boolean.valueOf(inputMap.get("isRemote"));
	 * 
	 * // Set the parameters on the widget vizConnection.setParameters(username,
	 * password, VisItSwtConnection.VISIT_CONNECTION_TYPE.IMAGE, windowWidth,
	 * windowHeight, windowId);
	 * 
	 * // Setup a remote gateway if needed if (!gateway.isEmpty()) {
	 * vizConnection.setGateway(gateway, localGatewayPort); }
	 * 
	 * // Enable tunneling if needed
	 * vizConnection.useTunneling(Boolean.valueOf(useTunneling));
	 * 
	 * // Launch the VisIt widget System.out.println(url + " " + port + " " +
	 * password + " " + visDir + " " + isRemote); boolean result =
	 * vizConnection.launch(url, port, password, visDir, isRemote);
	 * 
	 * // failed connection, etc.) if (!result) { if (isRemote) {
	 * MessageDialog.openError(shell, "Failed to Connect to VisIt",
	 * "Unable to connect to a running VisIt client."); } else {
	 * MessageDialog.openError(shell, "Failed to Launch VisIt",
	 * "VisIt has failed to launch."); } }
	 * 
	 * VisItRemoteFileDialog rdialog = new VisItRemoteFileDialog(
	 * vizConnection.getViewerMethods(), display); rdialog.open();
	 * 
	 * VisItSwtWidget widget = new VisItSwtWidget(shell, SWT.BORDER); try {
	 * widget.setVisItSwtConnection(vizConnection, 1, 400, 400); }
	 * catch(Exception e) { System.out.println(e.getMessage()); }
	 * 
	 * shell.open();
	 * 
	 * while (!shell.isDisposed()) { if (!display.readAndDispatch()) {
	 * display.sleep(); } } display.dispose(); }
	 */
}
