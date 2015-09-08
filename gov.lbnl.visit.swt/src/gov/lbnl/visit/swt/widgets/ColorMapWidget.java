package gov.lbnl.visit.swt.widgets;

import java.util.ArrayList;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Composite;

import gov.lbnl.visit.swt.VisItSwtConnection;
import gov.lbnl.visit.swt.VisItSwtWidget;

public class ColorMapWidget extends VisItWidget {
	
    /**
    *
    * @param parent
    *            A widget which will be the parent of the new instance (cannot
    *            be null).
    * @param style
    *            The style of widget to construct.
    */
   public ColorMapWidget(Composite parent, int style) {
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
   public ColorMapWidget(Composite parent, int style, VisItSwtConnection conn) {
       super(parent, style, conn);

       setupUI();
   }
   
   private void setupUI() {
	   
   }
   
   public void setColormap(int activeWindow, String plotSelection, String colorSelection) {

//	   	  VisItSwtWidget widget = (VisItSwtWidget)folder.getItems()[index].getControl();
//		  String plotSelection = plots.getText();
//		  String colorSelection = colors.getText();

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
