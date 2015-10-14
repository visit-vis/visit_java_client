package gov.lbnl.visit.swt.widgets;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


import gov.lbnl.visit.swt.VisItSwtConnection;
import visit.java.client.components.ColorMap;
import visit.java.client.components.ColorMap.ColorMapCallback;

public class ColorMapWidget extends VisItWidget {
	private ColorMap colormap;
	
    /**
    *
    * @param parent
    *            A widget which will be the parent of the new instance (cannot
    *            be null).
    * @param style
    *            The style of widget to construct.
    */
//   public ColorMapWidget(Composite parent, int style) {
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
   public ColorMapWidget(Composite parent, int style, VisItSwtConnection conn) {
       super(parent, style, conn);

       colormap = new ColorMap(conn.getViewerMethods());       
       setupUI();
   }
   
   private void setupUI() {
	   	Composite group = new Composite(this, SWT.NONE);
		
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		group.setLayout(new GridLayout(1, true));
		
		new Label(group, SWT.NONE).setText("Select Color:");
		final Combo combo = new Combo(group, SWT.NONE);
		
		List<String> colorList = colormap.getPredefinedList();
		combo.setItems((String[])colorList.toArray(new String[colorList.size()]));
				
		final Text colorTable = new Text(group, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		colorTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    
	    String sampleText = "0.1, 255, 255, 255, 255\n";
	    sampleText += "0.7, 255, 0, 0, 255";
	    
	    colorTable.setText(sampleText);
	    
	    Composite cx = new Composite(group, SWT.BORDER);
	    cx.setLayout(new GridLayout(3, false));
	    cx.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	    
	    Label l = new Label(cx, SWT.NONE);
	    l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
	    l.setText("Name:");
		
	    final Text name = new Text(cx, SWT.NONE);
	    name.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		name.setText("TestColor");
		
		Button submit = new Button(cx, SWT.BORDER);
	    submit.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		submit.setText("Add Color");
	    
		
		combo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo c = (Combo)e.getSource();
				String text = c.getText();
				String outputText = "";
				
				List<Float> pcnts = new ArrayList<Float>();
				List<ColorMap.Color> colors = new ArrayList<ColorMap.Color>();
				
				boolean res = colormap.getColorTable(text, pcnts, colors);
				
				
				if(!res) return;
				
				for(int i = 0; i < pcnts.size(); ++i) {
					String tmp = "" + pcnts.get(i) + ", " +
							colors.get(i).r + ", " +
							colors.get(i).g + ", " +
							colors.get(i).b + ", " +
							colors.get(i).a;
					outputText += tmp + "\n";
				}
				colorTable.setText(outputText);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		submit.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String colorname = name.getText(); 
				String output = colorTable.getText();
				
				String[] rows = output.split("\n");
				
				List<Float> pcnts = new ArrayList<Float>();
				List<ColorMap.Color> colors = new ArrayList<ColorMap.Color>();
 				for(int i = 0; i < rows.length; ++i) {
					if(rows[i].trim().startsWith("//"))
						continue;
					
					String row = rows[i].trim();
					String[] comps = row.split(",");
					

					if(comps.length != 5)
						continue;

					try {
						float val = Float.parseFloat(comps[0].trim());
						int red = Integer.parseInt(comps[1].trim());
						int green = Integer.parseInt(comps[2].trim());
						int blue = Integer.parseInt(comps[3].trim());
						int alpha = Integer.parseInt(comps[4].trim());
						
						pcnts.add(val);
						colors.add(new ColorMap.Color(red, green, blue, alpha));
					}catch(Exception ex) {
						ex.printStackTrace();
					}
				}
 				
 				colormap.createColorTable(colorname, pcnts, colors);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		colormap.register(new ColorMapCallback() {
			
			@Override
			public void colors(List<String> colornames) {
				final List<String> c = colornames;
				Display.getDefault().asyncExec(new Runnable() {
					
					@Override
					public void run() {
						combo.removeAll();
						combo.setItems(c.toArray(new String[c.size()]));
					}
				});
				
			}
		});
		
		this.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				colormap.cleanup();
			}
		});
   }
   
   public void setColormap(int activeWindow, String plotSelection, String colorSelection) {

		  String commands = "";

		  if(plotSelection.equals("Pseudocolor")) {
			  commands = "pa = GetPlotOptions()\n";
			  commands += "pa.colorTableName = \"" + colorSelection + "\"\n";
			  commands += "SetPlotOptions(pa)\n";
			  connection.getViewerMethods().processCommands(commands);	
		  } else if(plotSelection.equals("Contour")) {

		  } else {
			  commands = "va = GetPlotOptions()\n";
			  commands += "ccpl = GetColorTable(\"" + colorSelection + "\")\n";
			  commands += "va.colorControlPoints = ccpl\n";
			  commands += "SetPlotOptions(va)\n";

			  connection.getViewerMethods().processCommands(commands);	
		  }	
	  }
}
