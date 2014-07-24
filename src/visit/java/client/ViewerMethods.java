package visit.java.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import visit.java.client.AttributeSubject.AttributeSubjectCallback;

/**
 * Class containing viewer widget operations
 * 
 * @authors hkq, tnp
 */
public class ViewerMethods {

	/**
	 * The state of the viewer
	 */
	ViewerState m_state;

	Semaphore mutex = new Semaphore(-1);
	int syncId = -1;
	AttributeSubject syncAtts;
	
	/**
	 * The constructor
	 * 
	 * @param state
	 *            ViewerState input object
	 */
	public ViewerMethods(ViewerState state) {
		m_state = state;
		
		syncAtts = m_state.getAttributeSubjectFromTypename("SyncAttributes");
		
		//System.out.println("Syncing atts: " + syncAtts);
		
		syncAtts.addCallback(new AttributeSubjectCallback() {
	
			@Override
			public void update(AttributeSubject subject) {
				//System.out.println("sycn callback: " + subject.get("syncTag").getAsInt());
				if(syncId == subject.get("syncTag").getAsInt()) {
					//System.out.println("SyncTag: " + subject + " " + syncId);
					mutex.release();
				}
			}
		});
		
		mutex.release();
	}

	/**
	 * Method to invert the color of the background of the viewer
	 */
	public synchronized void invertBackgroundColor() {
		m_state.set(0, "RPCType",
				ViewerState.RPCType.InvertBackgroundRPC.ordinal());
		m_state.notify(0);
	}

	/**
	 * Method to add a window to the viewer
	 */
	public synchronized void addWindow() {
		m_state.set(0, "RPCType", ViewerState.RPCType.AddWindowRPC.ordinal());
		m_state.notify(0);
	}

	/**
	 * Method to draw plots in the viewer
	 */
	public synchronized void drawPlots() {
		m_state.set(0, "RPCType", ViewerState.RPCType.DrawPlotsRPC.ordinal());
		m_state.notify(0);
	}

	/**
	 * Method to reset the camera
	 */
	public void resetView() {
		m_state.set(0, "RPCType", ViewerState.RPCType.ResetViewRPC.ordinal());
		m_state.notify(0);
	}

	/**
	 * Method to clear the plot area
	 */
	public synchronized void clearWindow() {
		m_state.set(0, "RPCType", ViewerState.RPCType.ClearWindowRPC.ordinal());
		m_state.notify(0);
	}
	
	public synchronized void openClient(String clientName, String program, String[] args) {
	    m_state.set(0, "RPCType", ViewerState.RPCType.OpenClientRPC.ordinal());
	    m_state.set(0, "database", clientName);
	    m_state.set(0, "programHost", program);
	    
	    JsonArray array = new JsonArray();
	    for(int i = 0; i < args.length; ++i) {
	    	array.add(new JsonPrimitive(args[i]));
	    }
	    
	    m_state.set(0, "programOptions", array);
	    m_state.notify(0);
	}
	
	public synchronized void openCLI() {
		
		String clientName = "CLI";
		String program = "visit";
        
		String[] args = new String[2];
		args[0] = "-cli";
        args[1] = "-newconsole";
        
		openClient(clientName, program, args);
	}
	
	public synchronized void close() {
		m_state.set(0, "RPCType", ViewerState.RPCType.CloseRPC.ordinal());
		m_state.notify(0);
	}
	
	/**
	 * Method to remove the active plots from the viewer
	 */
	public synchronized void deleteActivePlots() {
		m_state.set(0, "RPCType",
				ViewerState.RPCType.DeleteActivePlotsRPC.ordinal());
		m_state.notify(0);
	}

	/**
	 * Method to hide the active plots from the viewer
	 */
	public synchronized void hideActivePlots() {
		m_state.set(0, "RPCType",
				ViewerState.RPCType.HideActivePlotsRPC.ordinal());
		m_state.notify(0);
	}

	/**
	 * Set the active plot to the given index
	 * 
	 * @param index
	 *            The index of the plot to make active
	 */
	public synchronized void setActivePlots(int index) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(index);

		setActivePlots(list);
	}

	/**
	 * Set the active plot to the given index
	 * 
	 * @param index
	 *            The index of the plot to make active
	 */
	public synchronized void setActivePlots(ArrayList<Integer> index) {
		m_state.set(0, "RPCType",
				ViewerState.RPCType.SetActivePlotsRPC.ordinal());
		m_state.set(0, "activePlotIds", (new Gson()).toJsonTree(index));
		m_state.notify(0);
	}

	/**
	 * @param filename
	 */
	public synchronized void openDatabase(String filename) {
		openDatabase(filename, 0, true, "");
	}

	/**
	 * @param filename
	 * @param timeState
	 */
	public synchronized void openDatabase(String filename, int timeState) {
		openDatabase(filename, timeState, true, "");
	}

	/**
	 * @param filename
	 * @param timeState
	 * @param addDefaultPlots
	 */
	public synchronized void openDatabase(String filename, int timeState,
			boolean addDefaultPlots) {
		openDatabase(filename, timeState, addDefaultPlots, "");
	}

	/**
	 * @param filename
	 * @param timeState
	 * @param addDefaultPlots
	 * @param forcedFileType
	 */
	public synchronized void openDatabase(String filename, int timeState,
			boolean addDefaultPlots, String forcedFileType) {
		m_state.set(0, "RPCType", ViewerState.RPCType.OpenDatabaseRPC.ordinal());
		m_state.set(0, "database", filename);
		m_state.set(0, "intArg1", timeState);
		m_state.set(0, "boolFlag", addDefaultPlots);
		m_state.set(0, "stringArg1", forcedFileType);

		m_state.notify(0);
	
		/// add others...
		getMetaData(filename);
		
		try {
			synchronize();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void closeDatabase(String filename) {
		m_state.set(0, "RPCType", ViewerState.RPCType.CloseDatabaseRPC.ordinal());
		m_state.set(0, "database", filename);
		
		m_state.notify(0);
	
//		/// add others...
//		getMetaData("");
		
		try {
			synchronize();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param plot_type
	 * @param name
	 * @return
	 */
	private synchronized int getEnabledID(String plot_type, String name) {
		JsonArray names = m_state.get(14, "name").getAsJsonArray();
		JsonArray types = m_state.get(14, "type").getAsJsonArray();
		JsonArray enabled = m_state.get(14, "enabled").getAsJsonArray();

		ArrayList<String> mapper = new ArrayList<String>();

		// System.out.println(names);
		// System.out.println(types);
		// System.out.println(enabled);

		for (int i = 0; i < names.size(); ++i) {
			// System.out.println(enabled.get(i).getAsInt() + " "
			// + types.get(i).getAsString());
			if (enabled.get(i).getAsInt() == 1
					&& plot_type.equals(types.get(i).getAsString()))
				mapper.add(names.get(i).getAsString());
		}

		java.util.Collections.sort(mapper);

		// the JSON map contains strings with quotes..
		// if(!name.contains("\"")) name = "\"" + name + "\"";

		for (int i = 0; i < mapper.size(); ++i) {
			// System.out.println(name + " " + mapper.get(i));
			if (name.equals(mapper.get(i)))
				return i;
		}
		return -1;
	}

	/**
	 * @param plot_type
	 * @param plot_var
	 */
	private synchronized void addPlotByID(int plot_type, String plot_var) {
		m_state.set(0, "RPCType", ViewerState.RPCType.AddPlotRPC.ordinal());
		m_state.set(0, "plotType", plot_type);
		m_state.set(0, "variable", plot_var);
		m_state.notify(0);
	}

	/**
	 * @param plot_name
	 * @param plot_var
	 */
	public synchronized void addPlot(String plot_name, String plot_var) {
		int index = getEnabledID("plot", plot_name);

		// System.out.println("--> " + index);
		if (index >= 0)
			addPlotByID(index, plot_var);
	}

	/**
	 * @param plot_type
	 */
	private synchronized void addOperatorByID(int op_type) {
		m_state.set(0, "RPCType", ViewerState.RPCType.AddOperatorRPC.ordinal());
		m_state.set(0, "operatorType", op_type);
		m_state.notify(0);
	}

	/**
	 * @param operator_name
	 */
	public synchronized void addOperator(String operator_name) {
		int index = getEnabledID("operator", operator_name);

		// System.out.println("--> " + index);
		if (index >= 0)
			addOperatorByID(index);
	}

	/**
	 * 
	 */
	public synchronized void setView3D() {
		m_state.set(0, "RPCType", ViewerState.RPCType.SetView3DRPC.ordinal());
		m_state.notify(0);
	}

	/**
	 * 
	 * @param up
	 * @param normal
	 */
	public synchronized void updateView(Vector<Double> up, Vector<Double> normal) {
		int view3DIndex = m_state.getIndexFromTypename("View3DAttributes");

		System.out.println("up = " + up + " normal = " + normal);
		m_state.set(view3DIndex, "viewUp", up);
		m_state.set(view3DIndex, "viewNormal", normal);

		System.out.println("state up = " + m_state.get(view3DIndex, "viewUp")
				+ " state normal = " + m_state.get(view3DIndex, "viewNormal"));
		m_state.notify(view3DIndex);
		setView3D();
	}

	/**
	 * 
	 * @param database
	 */
	public synchronized void getMetaData(String database) {
		m_state.set(0, "RPCType",
				ViewerState.RPCType.RequestMetaDataRPC.ordinal());
		m_state.set(0, "database", database);
		m_state.set(0, "stateNumber", 0);
		m_state.notify(0);
	}

	public synchronized void
	updateMouseActions(int windowId, String button,
	                                  double start_dx, double start_dy,
	                                  double end_dx, double end_dy) {

		JsonObject obj = new JsonObject();
	    obj.add("action", new JsonPrimitive("UpdateMouseActions"));
	    obj.add("mouseButton", new JsonPrimitive(button));
		obj.add("windowId", new JsonPrimitive(windowId));
		obj.add("start_dx", new JsonPrimitive(start_dx));
		obj.add("start_dy", new JsonPrimitive(start_dy));
		obj.add("end_dx", new JsonPrimitive(end_dx));
		obj.add("end_dy", new JsonPrimitive(end_dy));
		
		m_state.set(0, "RPCType", ViewerState.RPCType.ExportRPC.ordinal());
		m_state.set(0, "stringArg1", obj.toString());
		m_state.notify(0);
	}
	
	public synchronized void
	getFileList(String host, String remotePath) {

		JsonObject obj = new JsonObject();
	    obj.add("action", new JsonPrimitive("GetFileList"));
	    obj.add("host", new JsonPrimitive(host));
	    obj.add("path", new JsonPrimitive(remotePath));
		
		m_state.set(0, "RPCType", ViewerState.RPCType.ExportRPC.ordinal());
		m_state.set(0, "stringArg1", obj.toString());
		m_state.notify(0);
		
		try{
			synchronize();
		} catch(Exception e) {
		}
	}
	/**
	 * 
	 * @param windowId
	 * @param w
	 * @param h
	 */
	public synchronized void resizeWindow(int windowId, int w, int h) {
		m_state.set(0, "RPCType", ViewerState.RPCType.ResizeWindowRPC.ordinal());
		m_state.set(0, "windowId", windowId);
		m_state.set(0, "intArg1", w);
		m_state.set(0, "intArg2", h);
		m_state.notify(0);
	}
	
    public synchronized void processCommands(String commands) {

    	int index = m_state.getIndexFromTypename("ClientMethod");
        AttributeSubject as = m_state.getAttributeSubjectFromTypename("ClientMethod");
        
        commands += "\n";
        commands = "raw:" + commands;

        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(commands));

        as.set("stringArgs", array);
        as.set("methodName", new JsonPrimitive("Interpret"));

        m_state.notify(index);
    }

	
	public synchronized void synchronize() throws InterruptedException {
		syncId = (int)((double)Integer.MAX_VALUE * Math.random());

		syncAtts.set("syncTag", new JsonPrimitive(syncId));
		
		m_state.notify(syncAtts.getId());
		
		/// reset value 
		syncAtts.set("syncTag", new JsonPrimitive(-1));
		
		mutex.acquire();
	}
}