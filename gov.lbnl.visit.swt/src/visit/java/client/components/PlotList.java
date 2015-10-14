package visit.java.client.components;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;

import visit.java.client.AttributeSubject;
import visit.java.client.ViewerMethods;
import visit.java.client.AttributeSubject.AttributeSubjectCallback;

public class PlotList extends VisItComponent {

	public class Operator {
		public int type = -1;
		public String name = "";
	}

	public class Plot {
		public int type = -1;
		public String var = "";
		public String internalName = "";
		public String plotTypeName = "";
		public String databaseName = "";
		public ArrayList<Operator> ops = new ArrayList<Operator>();
	}
	
	public interface PlotListCallback 
    {
    	public void plotList(ArrayList<Plot> plots);
    };
    
    List<PlotListCallback> cb = new ArrayList<PlotListCallback>();
    
    public PlotList(ViewerMethods m) {
    	super(m);
    	
		attSubCallback = new AttributeSubjectCallback() {

			@Override
			public boolean update(AttributeSubject subject) {
				
				JsonArray plots = subject.get("plots").getAsJsonArray();
				ArrayList<Plot> rplots = new ArrayList<Plot>();
				
				for(int i = 0; i < plots.size(); ++i) {
					
					/*
					 * GetPlots(0).plotType = 10
						GetPlots(0).plotName = "Plot0000"
						GetPlots(0).activeFlag = 1
						GetPlots(0).hiddenFlag = 0
						GetPlots(0).expandedFlag = 0
						GetPlots(0).plotVar = "x"
						GetPlots(0).databaseName = "localhost:/home/users/hari/visit_data_files/noise.silo"
						GetPlots(0).operators = ()
						GetPlots(0).operatorNames = ()
						GetPlots(0).activeOperator = -1
						GetPlots(0).id = 0
						GetPlots(0).embeddedPlotId = -1
						GetPlots(0).beginFrame = 0
						GetPlots(0).endFrame = 0
						GetPlots(0).keyframes = (0)
						GetPlots(0).databaseKeyframes = (0)
						GetPlots(0).isFromSimulation = 0
						GetPlots(0).followsTime = 1
						GetPlots(0).description = ""
						GetPlots(0).selection = ""
					 */
					Plot p = new Plot();
					
					AttributeSubject subj = new AttributeSubject(plots.get(i));
					p.internalName = subj.get("plotName").getAsString();
					p.type = subj.get("plotType").getAsInt();
					p.var = subj.get("plotVar").getAsString();
					p.databaseName = subj.get("databaseName").getAsString();
					p.plotTypeName = methods.getPlotName(p.type);
					
					JsonArray operators = subj.get("operators").getAsJsonArray();
					JsonArray operatorNames = subj.get("operatorNames").getAsJsonArray();

					for(int j = 0; j < operators.size(); ++j) {
						Operator op = new Operator();
						op.type = operators.get(j).getAsInt();
						op.name = operatorNames.get(j).getAsString();
						p.ops.add(op);
					}
					rplots.add(p);
				}
				
				for(int i = 0; i < cb.size(); ++i) {
					cb.get(i).plotList(rplots);
				}
				
				return true;
			}
		};

		// Register the AttributeSubjectCallback with the VisIt widget
		methods.getViewerState()
				.registerCallback("PlotList", attSubCallback);
	}
    
    AttributeSubjectCallback attSubCallback;
	
    public boolean register(PlotListCallback c) {
    	return cb.add(c);
	}
	
	public boolean unregister(PlotListCallback c) {
		return cb.remove(c);
	}
	
	public void cleanup() {
		cb.clear();
		methods.getViewerState()
		.unregisterCallback("PlotList", attSubCallback);
	}
	
	public boolean updatePlot(String plot, int plotIndex, String key, String value) {
		
		String p = plot + "Attributes";
		
		AttributeSubject subj = methods.getViewerState().getAttributeSubjectFromTypename(p);
		if(subj == null) return false;
		
		subj.set(key, value);
		
		int index = methods.getViewerState().getIndexFromTypename(p);
		methods.getViewerState().notify(index);
		methods.synchronize();

		methods.setPlotOptions(plot);

		return true;
	}
	
	public boolean updateOperator(String plot, int plotIndex, 
								String operator, int opIndex, String key, String value) {
		
		String op = operator + "Attributes";
		AttributeSubject subj = methods.getViewerState().getAttributeSubjectFromTypename(op);
		if(subj == null) return false;
		
		subj.set(key, value);
		
		int index = methods.getViewerState().getIndexFromTypename(op);
		methods.getViewerState().notify(index);
		methods.synchronize();
		
		methods.setOperatorOptions(operator);
		return true;
	}
}
