package visit.java.client.components;

import java.util.List;

import org.eclipse.swt.graphics.Color;

import visit.java.client.AttributeSubject;
import visit.java.client.ViewerMethods;

public class ColorMap extends VisItComponent {

	public ColorMap(ViewerMethods m) {
		super(m);
	}
	
	public List<String> getPredefinedList() {
		if(methods == null) return null;
		
	    AttributeSubject catts = methods.getViewerState().getAttributeSubjectFromTypename("ColorTableAttributes");
	    List<String> colornames = catts.getAsStringVector("names");
	    
	    return colornames;
	}
	
	public void select(String name, int activePlotId, String plotType) {
	String cmd = 
"try:\n" +
"   ccpl = GetColorTable('" + name + "')\n" +
	        
"   SetActivePlots(" + activePlotId + ")\n";

	if(plotType.equals("Pseudocolor"))
	cmd +=
"   xyz = GetPlotOptions()\n" +
"   xyz.colorTableName = '" + name + "'\n" +
"   xyz.opacityType = xyz.ColorTable\n" +
"   SetPlotOptions(xyz)\n";
	else {
	cmd +=
"   a = GetPlotOptions()\n" +
"   a.SetColorControlPoints(ccpl)\n" +
"   SetPlotOptions(a)\n";
	}

cmd += 
"except:\n" +
"   print 'update color table failed'\n" +
"   pass\n";
	
    //System.out.println(cmd);
	methods.processCommands(cmd);
	}

	
	public void createColorTable(String name, List<Float> pcnts, List<Color> colors) {
        
		if(pcnts.size() != colors.size())
			return;
		
		String message = "";
		message  = "def MakeRGBColorTable():\n";
        message += "  ccpl = ColorControlPointList()\n";
        message += "  colorArray = []\n";
        for(int i = 0; i < pcnts.size(); ++i) {
        message += "  colorArray.append((" + pcnts.get(i) + ", " 
        + colors.get(i).getRed() + ", " 
        + colors.get(i).getGreen() + ", " 
        + colors.get(i).getBlue() + ", " 
        + colors.get(i).getAlpha() + "))\n";
        }
        message += "  for i in colorArray:\n";
        message += "    p = ColorControlPoint()\n";
        message += "    p.colors = (i[1],i[2],i[3],i[4])\n";
        message += "    p.position = i[0]\n";
        message += "    ccpl.AddControlPoints(p)\n";
        message += "  AddColorTable('" + name + "',ccpl)\n";
        message += "MakeRGBColorTable()\n";
        
        //System.out.println(message);
        methods.processCommands(message);
	}
}
