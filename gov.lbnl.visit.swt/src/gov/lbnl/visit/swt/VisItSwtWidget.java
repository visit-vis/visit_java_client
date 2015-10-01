package gov.lbnl.visit.swt;

import gov.lbnl.visit.swt.VisItSwtConnection.VISIT_CONNECTION_TYPE;
import gov.lbnl.visit.swt.VisItSwtConnection.VisualizationUpdateCallback;

import java.io.ByteArrayInputStream;
import java.net.ConnectException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import visit.java.client.ViewerMethods;
import visit.java.client.ViewerState;

/**
 * This class extends SWT Canvas to provide an area to display images rendered
 * by an external VisIt client.
 * 
 * @authors hkq, tnp
 */
public class VisItSwtWidget extends Canvas implements Listener,
		VisualizationUpdateCallback {

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
     * 
     */
    boolean mousePressed = false;

    /**
     * 
     */
    int startx, starty, sizex, sizey;
    
    /**
     * Mouse manager...
     */
     MouseManager mouseManager; //optional manager
     
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

        mouseManager = null;
        
        // Get the Shell of the parent Composite
        shell = visComp.getShell();

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

    public void useDefaultMouseManager() {
    	mouseManager = new MouseManager(this);
    }
    
    public void activate() {
        getViewerMethods().setActiveWindow(visitWindowId);

        // Initialize the mouse controls
        // There may be a better way to accomplish this task. This was
        // previously done in VisitPlotViewer#drawPlot(Entry). A comment to
        // create an initialize() function in this class to achieve what these
        // three lines do was included there.
        mouseStart(0, 0, false, false);
        mouseMove(0, 0, false, false);
        mouseStop(0, 0, false, false);
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
        if(image.isDisposed()) {
        } else {
        	gc.drawImage(image, 0, 0);
        }
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
				// Check that the canvas is properly constructed and not
				// disposed before attempting to draw.
            	if (!isDisposed()) {
	                ByteArrayInputStream bis = new ByteArrayInputStream(output);
	                try {
	                	image = new Image(shell.getDisplay(), bis);
	                } 
	                catch(SWTException e) {
	                }
	                redraw();
            	}
            }
        });
    }
    
    class MouseManager {

    	VisItSwtWidget widget;
    	
    	public MouseManager(VisItSwtWidget w) {
    		widget = w;
    		setup();
    	}
    	
    	public void setup() {
    		  
    		widget.addMouseWheelListener(new MouseWheelListener() {
    			@Override
    			public void mouseScrolled(MouseEvent e) {
    				VisItSwtWidget widget = (VisItSwtWidget)e.getSource();
    				String direction = (e.count > 0) ? "in" : "out";
    				widget.zoom(direction);
    			}
    		});

    		widget.addMouseWheelListener(new MouseWheelListener() {
    			@Override
    			public void mouseScrolled(MouseEvent e) {
    				VisItSwtWidget widget = (VisItSwtWidget)e.getSource();
    				String direction = (e.count > 0) ? "in" : "out";
    				widget.zoom(direction);
    			}
    		});

    		// Use mouse click to move the plot
    		widget.addMouseMoveListener(new MouseMoveListener() {
    			@Override
    			public void mouseMove(MouseEvent e) {
    				VisItSwtWidget widget = (VisItSwtWidget)e.getSource();
    				widget.mouseMove(e.x, e.y,
    						(e.stateMask & SWT.CTRL) != 0, 
    						(e.stateMask & SWT.ALT) != 0);
    			}
    		});

    		// Update the mouse in the widget based on its movements
    		widget.addMouseListener(new MouseListener() {
    			@Override
    			public void mouseUp(MouseEvent e) {
    				VisItSwtWidget widget = (VisItSwtWidget)e.getSource();
    				widget.mouseStop(e.x, e.y,
    						(e.stateMask & SWT.CTRL) != 0, 
    						(e.stateMask & SWT.ALT) != 0);
    			}

    			@Override
    			public void mouseDown(MouseEvent e) {
    				VisItSwtWidget widget = (VisItSwtWidget)e.getSource();
    				widget.mouseStart(e.x, e.y,
    						(e.stateMask & SWT.CTRL) != 0, 
    						(e.stateMask & SWT.ALT) != 0);
    			}

    			@Override
    			public void mouseDoubleClick(MouseEvent e) {
    			}
    		});
    	}
    }
}
