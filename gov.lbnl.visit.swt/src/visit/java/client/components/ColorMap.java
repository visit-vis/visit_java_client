package visit.java.client.components;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Color;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import visit.java.client.AttributeSubject;
import visit.java.client.AttributeSubject.AttributeSubjectCallback;
import visit.java.client.ViewerMethods;

public class ColorMap extends VisItComponent {
	
	public interface ColorMapCallback {
		public void colors(List<String> colornames);
	}

	public static class Color {
		public int r=0,g=0,b=0,a=0;
		public Color(int r, int g, int b, int a) {
			this.r = r;
			this.g = g;
			this.b = b;
			this.a = a;
		}
		
		List<Integer> toList() {
			List<Integer> colors = new ArrayList<Integer>();
			colors.add(r);
			colors.add(g);
			colors.add(b);
			colors.add(a);
			return colors;
		}
	}
	
	List<ColorMapCallback> cbList = new ArrayList<ColorMapCallback>();
	
	AttributeSubjectCallback colorTableCallback = null;
	
	public ColorMap(ViewerMethods m) {
		super(m);
	
		colorTableCallback = new AttributeSubjectCallback() {
			
			@Override
			public boolean update(AttributeSubject subject) {
				List<String> colors = getPredefinedList();
				for(int i = 0; i < cbList.size(); ++i) {
					cbList.get(i).colors(colors);
				}
				return true;
			}
		};
		
		methods.getViewerState().registerCallback("ColorTableAttributes", colorTableCallback);
	}
	
	public boolean register(ColorMapCallback cb) {
		return cbList.add(cb);
	}
	
	public boolean unregister(ColorMapCallback cb) {
		return cbList.remove(cb);
	}
	
	public void cleanup() {
		cbList.clear();
		methods.getViewerState().unregisterCallback("ColorTableAttributes", colorTableCallback);		
	}
	
	public List<String> getPredefinedList() {
		if(methods == null) return null;
		
	    AttributeSubject catts = methods.getViewerState().getAttributeSubjectFromTypename("ColorTableAttributes");
	    List<String> colornames = catts.getAsStringVector("names");
	    
	    return colornames;
	}
	
//	public void select(String name, int activePlotId, String plotType) {
//	String cmd = 
//"try:\n" +
//"   ccpl = GetColorTable('" + name + "')\n" +
//	        
//"   SetActivePlots(" + activePlotId + ")\n";
//
//	if(plotType.equals("Pseudocolor"))
//	cmd +=
//"   xyz = GetPlotOptions()\n" +
//"   xyz.colorTableName = '" + name + "'\n" +
//"   xyz.opacityType = xyz.ColorTable\n" +
//"   SetPlotOptions(xyz)\n";
//	else {
//	cmd +=
//"   a = GetPlotOptions()\n" +
//"   a.SetColorControlPoints(ccpl)\n" +
//"   SetPlotOptions(a)\n";
//	}
//
//cmd += 
//"except:\n" +
//"   print 'update color table failed'\n" +
//"   pass\n";
//	
//    //System.out.println(cmd);
//	methods.processCommands(cmd);
//	}

	public boolean getColorTable(String name, List<Float> pcnts, List<Color> colors) {
		if(pcnts.size() != colors.size())
			return false;
		
		AttributeSubject subj = methods.getViewerState().getAttributeSubjectFromTypename("ColorTableAttributes");
		List<String> names = subj.getAsStringVector("names");
		
		int index = -1;
		
		for(int i = 0; i < names.size(); ++i) {
			if(names.get(i).equals(name)) {
				index = i;
				break;
			}
		}
		
		if(index == -1) return false;
		
		JsonArray array = subj.get("colorTables").getAsJsonArray();
		AttributeSubject controlPointList = new AttributeSubject(array.get(index));
		
		JsonArray cparray = controlPointList.get("controlPoints").getAsJsonArray();
		for(int i = 0; i < cparray.size(); ++i) {
			JsonElement elem = cparray.get(i);
			AttributeSubject catts = new AttributeSubject(elem);
			List<Integer> c = catts.getAsIntVector("colors");
		
			double position = catts.getAsFloat("position");
			pcnts.add((float)position);
			colors.add(new Color(c.get(0), c.get(1), c.get(2), c.get(3)));
		}
		
		return true;
	}

	public boolean setColorTable(String name, List<Float> pcnts, List<Color> colors) {
		if(pcnts.size() != colors.size())
			return false;
		
		AttributeSubject subj = methods.getViewerState().getAttributeSubjectFromTypename("ColorTableAttributes");
		List<String> names = subj.getAsStringVector("names");
		JsonArray array = subj.get("colorTables").getAsJsonArray();
		
		int index = -1;
		
		for(int i = 0; i < names.size(); ++i) {
			if(names.get(i).equals(name)) {
				index = i;
				break;
			}
		}
		
		/// colormap already exists..
		/// remove it first..
		if(index >= 0) {
			AttributeSubject controlPointList = new AttributeSubject(array.get(index));
			JsonArray cparray = controlPointList.get("controlPoints").getAsJsonArray();
			
			/// get a copy of Color attribute subject..
			JsonElement elem =  cparray.get(0);
			
			AttributeSubject elem_catts = new AttributeSubject(elem);
		
			JsonArray out_array = new JsonArray();
			for(int i = 0; i < colors.size(); ++i) {
				AttributeSubject catts = elem_catts.deepCopy();
				float pos = pcnts.get(i);
				catts.set("colors", colors.get(i).toList());
				catts.set("position", pos);
				out_array.add(catts.toJsonObject());
			}
		} 
				
		return true;
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
        + colors.get(i).r + ", " 
        + colors.get(i).g + ", " 
        + colors.get(i).b+ ", " 
        + colors.get(i).a+ "))\n";
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
