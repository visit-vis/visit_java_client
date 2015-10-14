package gov.lbnl.visit.swt.widgets;

import java.io.ByteArrayInputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import gov.lbnl.visit.swt.VisItSwtConnection;
import gov.lbnl.visit.swt.VisItSwtConnection.VISIT_CONNECTION_TYPE;
import gov.lbnl.visit.swt.VisItSwtConnection.VisualizationUpdateCallback;
import visit.java.client.components.Annotations;

public class AnnotationWidget extends VisItWidget {

	private Annotations annotations;
	//Image image = null;
	
	public AnnotationWidget(Composite parent, int style) {
		this(parent, style, null);
	}
	
	public AnnotationWidget(Composite parent, int style, VisItSwtConnection conn) {
		super(parent, style, conn);
		annotations = new Annotations(conn.getViewerMethods());
		setupUI();
	}
	
//	private Image resize(Image image, int width, int height) {
//		Image scaled = new Image(Display.getDefault(), width, height);
//		GC gc = new GC(scaled);
//		gc.setAntialias(SWT.ON);
//		gc.setInterpolation(SWT.HIGH);
//		gc.drawImage(image, 0, 0, 
//				image.getBounds().width, image.getBounds().height, 
//				0, 0, width, height);
//		gc.dispose();
//		return scaled;
//	}
	
	Image image = null;
	private void setupUI() {

		Composite mainGroup = new Composite(this, SWT.BORDER);
		mainGroup.setLayout(new GridLayout(2, false));
		mainGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite group = new Composite(mainGroup, SWT.NONE);
		group.setLayout(new GridLayout(1, true));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

		Composite posComp = new Composite(group, SWT.NONE);
		posComp.setLayout(new RowLayout());
		posComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		new Label(posComp, SWT.BORDER).setText("X:");
		final Spinner xspinner = new Spinner(posComp, SWT.NONE);
		xspinner.setDigits(2);
		xspinner.setMinimum(0);
		xspinner.setMaximum(100);
		
		new Label(posComp, SWT.BORDER).setText("Y:");
		final Spinner yspinner = new Spinner(posComp, SWT.NONE);
		yspinner.setDigits(2);
		yspinner.setMinimum(0);
		yspinner.setMaximum(100);
		
		Composite sliderComp = new Composite(group, SWT.NONE);
		sliderComp.setLayout(new RowLayout());
		sliderComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	
		Button addSlider = new Button(sliderComp, SWT.PUSH);
		addSlider.setText("Add Time Slider");
		

		Composite addTextComp = new Composite(group, SWT.NONE);
		addTextComp.setLayout(new GridLayout(2, true));
		addTextComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Text xLabel = new Text(addTextComp, SWT.NONE);

		Button addText = new Button(addTextComp, SWT.PUSH);
		addText.setText("Add 2D Text");
		
		addSlider.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selection = xspinner.getSelection();
				int digits = xspinner.getDigits();
				float x  = (float) (selection / Math.pow(10, digits));

				selection = yspinner.getSelection();
				digits = yspinner.getDigits();
				float y  = (float) (selection / Math.pow(10, digits));

				annotations.createTimeSlider(x, y);
			}
				
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		addText.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				int selection = xspinner.getSelection();
				int digits = xspinner.getDigits();
				float x  = (float) (selection / Math.pow(10, digits));

				selection = yspinner.getSelection();
				digits = yspinner.getDigits();
				float y  = (float) (selection / Math.pow(10, digits));

				annotations.createText2D(xLabel.getText(), x, y);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	
		Button clearCanvas = new Button(group, SWT.PUSH);
		clearCanvas.setText("Clear Annotations");
		
		clearCanvas.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				annotations.clearAll();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		//image = Display.getDefault().getSystemImage(SWT.ICON_WORKING);
		
		final Point origin = new Point (0, 0);
		final Canvas canvas = new Canvas (mainGroup,  SWT.V_SCROLL | SWT.H_SCROLL);

		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				
//		final ScrollBar hBar = canvas.getHorizontalBar ();
//		hBar.addListener (SWT.Selection, new Listener () {
//			@Override
//			public void handleEvent (Event e) {
//				if(image == null) return;
//				
//				int hSelection = hBar.getSelection ();
//				int destX = -hSelection - origin.x;
//				Rectangle rect = image.getBounds ();
//				canvas.scroll (destX, 0, 0, 0, rect.width, rect.height, false);
//				origin.x = -hSelection;
//			}
//		});
		
//		final ScrollBar vBar = canvas.getVerticalBar ();
//		vBar.addListener (SWT.Selection, new Listener () {
//			@Override
//			public void handleEvent (Event e) {
//				if(image == null) return;
//				int vSelection = vBar.getSelection ();
//				int destY = -vSelection - origin.y;
//				Rectangle rect = image.getBounds ();
//				canvas.scroll (0, destY, 0, 0, rect.width, rect.height, false);
//				origin.y = -vSelection;
//			}
//		});
//		canvas.addListener (SWT.Resize,  new Listener () {
//			@Override
//			public void handleEvent (Event e) {
//				if(image == null) return;
//				
//				Rectangle rect = image.getBounds ();
//				Rectangle client = canvas.getClientArea ();
//				hBar.setMaximum (rect.width);
//				vBar.setMaximum (rect.height);
//				hBar.setThumb (Math.min (rect.width, client.width));
//				vBar.setThumb (Math.min (rect.height, client.height));
//				int hPage = rect.width - client.width;
//				int vPage = rect.height - client.height;
//				int hSelection = hBar.getSelection ();
//				int vSelection = vBar.getSelection ();
//				if (hSelection >= hPage) {
//					if (hPage <= 0) hSelection = 0;
//					origin.x = -hSelection;
//				}
//				if (vSelection >= vPage) {
//					if (vPage <= 0) vSelection = 0;
//					origin.y = -vSelection;
//				}
//				canvas.redraw ();
//			}
//		});
//		canvas.addListener (SWT.Paint, new Listener () {
//			@Override
//			public void handleEvent (Event e) {
//				if(image == null) return;
//				
//				GC gc = e.gc;
//				gc.drawImage (image, origin.x, origin.y);
//				Rectangle rect = image.getBounds ();
//				Rectangle client = canvas.getClientArea ();
//				int marginWidth = client.width - rect.width;
//				if (marginWidth > 0) {
//					gc.fillRectangle (rect.width, 0, marginWidth, client.height);
//				}
//				int marginHeight = client.height - rect.height;
//				if (marginHeight > 0) {
//					gc.fillRectangle (0, rect.height, client.width, marginHeight);
//				}
//			}
//		});

		canvas.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				e.gc.drawImage(image, 0, 0);
			}
		});
		
		final VisualizationUpdateCallback cb = new VisualizationUpdateCallback() {
			
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
							if(image != null) {
								image.dispose();
							}
							
							image = new Image(Display.getDefault(), bis);
							canvas.redraw();							
						}
					}
				});
			}
		};
		connection.registerVisualization(VISIT_CONNECTION_TYPE.IMAGE, -1, cb);
		connection.getViewerMethods().forceRedraw(1);
	
		this.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				//System.out.println("Annotation disposed...");
				if(image != null && !image.isDisposed()) {
					image.dispose();
				}
				
				connection.unregisterVisualization(-1, cb);
			}
		});
	}
	
}
