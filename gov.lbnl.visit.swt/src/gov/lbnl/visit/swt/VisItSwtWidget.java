package gov.lbnl.visit.swt;

import gov.lbnl.visit.swt.VisItSwtConnection.VISIT_CONNECTION_TYPE;
import gov.lbnl.visit.swt.VisItSwtConnection.VisualizationUpdateCallback;

import java.io.ByteArrayInputStream;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
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

    private static final String AVTDATABASEMETADATA = "avtDatabaseMetaData";
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
     * 
     */
    private boolean initialized = false;

    /**
     * files
     */
    List<String> files;

    /**
     * dirs
     */
    List<String> dirs;

    /**
     * 
     */
    boolean mousePressed = false;

    /**
     * 
     */
    int startx, starty, sizex, sizey;

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
        files = new ArrayList<String>();
        dirs = new ArrayList<String>();

        // Initialize the default image
        image = shell.getDisplay().getSystemImage(SWT.ICON_WORKING);

        // Register this as an SWT.Paint listener
        addListener(SWT.Paint, this);

        addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                // nothing to do when focus is lost..
            }

            @Override
            public void focusGained(FocusEvent e) {
                activate();
            }
        });
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
            int windowWidth, int windowHeight) throws ConnectException {
        visitConnection = conn;
        visitWindowId = windowId;

        // / if window Id is -1 then add a new window
        if (visitWindowId == -1) {
            List<Integer> wIds = visitConnection.getWindowIds();

            if (wIds.isEmpty()) {
                // VisIt creates 1 window by default..
                visitWindowId = 1;
            } else {
                getViewerMethods().addWindow();
                List<Integer> nIds = visitConnection.getWindowIds();

                Set<Integer> s1 = new HashSet<Integer>();
                s1.addAll(nIds);
                s1.removeAll(wIds);

                // /there should be one left over..
                if (s1.size() != 1) {
                    visitConnection = null;
                    throw new ConnectException(
                            "Bad Connection: Could not create new window");
                }

                visitWindowId = (Integer) s1.toArray()[0];
            }
        }

        getViewerMethods().registerNewWindow(visitWindowId);

        visitConnection.registerVisualization(VISIT_CONNECTION_TYPE.IMAGE,
                visitWindowId, this);
        visitConnection.registerCallback(AVTDATABASEMETADATA, this);
        initialized = true;

        getViewerMethods().resizeWindow(visitWindowId, windowWidth,
                windowHeight);

        this.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event e) {
                Rectangle rect = VisItSwtWidget.this.getClientArea();
                getViewerMethods().resizeWindow(visitWindowId, rect.width,
                        rect.height);
            }
        });

        addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                visitConnection.unregisterVisualization(visitWindowId,
                        VisItSwtWidget.this);

                visitConnection.unregisterCallback(AVTDATABASEMETADATA,
                        VisItSwtWidget.this);

            }
        });
    }

    public VisItSwtConnection getVisItSwtConnection() {
        return visitConnection;
    }

    public int getWindowId() {
        return visitWindowId;
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

        Map<String, List<String>> outputArray = new HashMap<String, List<String>>();

        String[] vartypes = new String[] { "meshes", "scalars", "vectors",
                "materials" };

        for (int i = 0; i < vartypes.length; ++i) {
            List<String> data = new ArrayList<String>();

            JsonArray array = arg0.get(vartypes[i]).getAsJsonArray();

            for (int j = 0; j < array.size(); ++j) {

                JsonObject obj = array.get(j).getAsJsonObject();
                String name = arg0.getAttr(obj, "name").getAsString();
                data.add(name);
            }

            outputArray.put(vartypes[i], data);
        }

        fi.setFileName(filename);
        fi.setFileType(filetype);
        fi.setFileDescription(description);

        fi.setMeshes(outputArray.get("meshes"));
        fi.setScalars(outputArray.get("scalars"));
        fi.setVectors(outputArray.get("vectors"));
        fi.setMaterials(outputArray.get("materials"));

        openDatabaseInfo = fi;
    }

    /**
     * 
     */
    @Override
    public synchronized boolean update(AttributeSubject arg0) {
        String typename = arg0.getTypename();

        if (AVTDATABASEMETADATA.equals(typename)) {
            updateDatabaseMetaData(arg0);
            return true;
        }

        return false;
    }

    /**
     * 
     * @return
     */
    public ViewerMethods getViewerMethods() {
        return visitConnection.getViewerMethods();
    }

    /**
     * 
     * @return
     */
    public ViewerState getViewerState() {
        return visitConnection.getViewerState();
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
                "in".equals(direction) ? "WheelUp" : "WheelDown",
                new double[] { 0, 0 }, new double[] { 0, 0 }, false, false);
    }

    /**
     * 
     * @param x
     * @param y
     */
    public void mouseStart(int x, int y, boolean ctrl, boolean shift) {
        if (visitConnection == null || !visitConnection.hasInitialized()) {
            return;
        }
        Point p = getSize();
        sizex = p.x;
        sizey = p.y;
        startx = x;
        starty = y;
        mousePressed = true;
    }

    /**
     * 
     * @param x
     * @param y
     */
    public void mouseMove(int x, int y, boolean ctrl, boolean shift) {
        if (visitConnection == null || !visitConnection.hasInitialized()) {
            return;
        }
        if (mousePressed) {

            String[] actions = new String[] { "LeftPress", "Move",
                    "LeftRelease" };

            for (int i = 0; i < actions.length; ++i) {
                getViewerMethods().updateMouseActions(
                        visitWindowId,
                        actions[i],
                        new double[] { (double) startx / (double) sizex,
                                (double) starty / sizey },
                        new double[] { (double) x / (double) sizex,
                                (double) y / (double) sizey }, ctrl, shift);
            }
            startx = x;
            starty = y;
        }
    }

    /**
     * 
     * @param x
     * @param y
     */
    public void mouseStop(int x, int y, boolean ctrl, boolean shift) {

        if (visitConnection == null || !visitConnection.hasInitialized()) {
            return;
        }

        mousePressed = false;
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
                image = new Image(shell.getDisplay(), bis);
                redraw();
            }
        });
    }
}
