package visit.java.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import visit.java.client.AttributeSubject.AttributeSubjectCallback;

/**
 * Class containing viewer widget operations
 * 
 * @authors hkq, tnp
 */
public class ViewerMethods {

    private static final String RPCTYPE = "RPCType";
    private static final String EXPORTRPC = "ExportRPC";
    private static final String WINDOWID = "windowId";

    // / common arguments
    private static final String ACTION = "action";
    private static final String DATABASE = "database";

    private static final String INTARG1 = "intArg1";
    private static final String STRINGARG1 = "stringArg1";
    private static final String SYNCTAG = "syncTag";

    /**
     * The state of the viewer
     */
    ViewerState mState;

    /**
     * 
     */
    Map<String, Integer> visitRPC;

    /**
     * 
     */
    Semaphore mutex = new Semaphore(-1);

    /**
     * 
     */
    int syncId = -1;

    /**
     * 
     */
    AttributeSubject syncAtts;
    
    /**
     * 
     */
    FileInfo databaseInfo;

    /**
     * The constructor
     * 
     * @param state
     *            ViewerState input object
     * @param visit_rpc
     */
    public ViewerMethods(ViewerState state, Map<String, Integer> rpc) {
        mState = state;
        visitRPC = rpc;

        syncAtts = mState.getAttributeSubjectFromTypename("SyncAttributes");

        syncAtts.addCallback(new AttributeSubjectCallback() {

            @Override
            public boolean update(AttributeSubject subject) {
                if (syncId == subject.get(SYNCTAG).getAsInt()) {
                    mutex.release();
                }

                return true;
            }
        });
        
        
        AttributeSubject databaseMetaData = mState.getAttributeSubjectFromTypename("avtDatabaseMetaData");
        
        databaseMetaData.addCallback(new AttributeSubjectCallback() {
			
			@Override
			public boolean update(AttributeSubject arg0) {
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

		        ///TODO: check if cyclesAreAccurate & timesAreAccurate fields..
	            JsonArray timesArray = arg0.get("times").getAsJsonArray();
		        ArrayList<Float> times = new ArrayList<Float>();
	            for(int i = 0; i < timesArray.size(); ++i) {
	            	times.add(timesArray.get(i).getAsFloat());
	            }
	            
	            JsonArray cycleArray = arg0.get("cycles").getAsJsonArray();
	            ArrayList<Integer> cycles = new ArrayList<Integer>();
	            for(int i = 0; i < cycleArray.size(); ++i) {
	            	cycles.add(cycleArray.get(i).getAsInt());
	            }
	            
	            fi.setTimes(times);
	            fi.setCycles(cycles);
	            
	            databaseInfo = fi;
		        
				return true;
			}
		});

        mutex.release();
    }

    public ViewerState getViewerState() {
        return mState;
    }
    
    /**
     * 
     * @return the databaseInfo
     */
    public FileInfo getDatabaseInfo() {
    	return databaseInfo;
    }

    /**
     * Method to invert the color of the background of the viewer
     */
    public synchronized void invertBackgroundColor() {
        mState.set(0, RPCTYPE, visitRPC.get("InvertBackgroundRPC"));
        mState.notify(0);
        synchronize();
    }

    /**
     * Method to add a window to the viewer
     */
    public synchronized void addWindow() {
        mState.set(0, RPCTYPE, visitRPC.get("AddWindowRPC"));
        mState.notify(0);
        synchronize();
    }

    public synchronized void setActiveWindow(int windowId) {
        mState.set(0, RPCTYPE, visitRPC.get("SetActiveWindowRPC"));
        mState.set(0, WINDOWID, windowId);
        mState.set(0, "boolFlag", false);
        mState.notify(0);
        synchronize();

    }

    /**
     * Method to draw plots in the viewer
     */
    public synchronized void drawPlots() {
        mState.set(0, RPCTYPE, visitRPC.get("DrawPlotsRPC"));
        mState.notify(0);
        synchronize();
    }

    /**
     * Method to reset the camera
     */
    public void resetView() {
        mState.set(0, RPCTYPE, visitRPC.get("ResetViewRPC"));
        mState.notify(0);
        synchronize();
    }

    /**
     * Method to clear the plot area
     */
    public synchronized void clearWindow() {
        mState.set(0, RPCTYPE, visitRPC.get("ClearWindowRPC"));
        mState.notify(0);
        synchronize();
    }

    public synchronized void openClient(String clientName, String program,
            String[] args) {
        mState.set(0, RPCTYPE, visitRPC.get("OpenClientRPC"));
        mState.set(0, DATABASE, clientName);
        mState.set(0, "programHost", program);

        JsonArray array = new JsonArray();
        for (int i = 0; i < args.length; ++i) {
            array.add(new JsonPrimitive(args[i]));
        }

        mState.set(0, "programOptions", array);
        mState.notify(0);
        synchronize();
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
        mState.set(0, RPCTYPE, visitRPC.get("CloseRPC"));
        mState.notify(0);
    }

    /**
     * Method to remove the active plots from the viewer
     */
    public synchronized void deleteActivePlots() {
        mState.set(0, RPCTYPE, visitRPC.get("DeleteActivePlotsRPC"));
        mState.notify(0);

        synchronize();
    }

    public synchronized void deleteAllPlots() {
        int numPlots = getViewerState()
                .getAttributeSubjectFromTypename("PlotList").get("plots")
                .getAsJsonArray().size();

        if (numPlots == 0) {
            return;
        }

        JsonArray array = new JsonArray();

        for (int i = 0; i < numPlots; ++i) {
            array.add(new JsonPrimitive(i));
        }

        setActivePlots(array);
        deleteActivePlots();
    }

    /**
     * Method to hide the active plots from the viewer
     */
    public synchronized void hideActivePlots() {
        mState.set(0, RPCTYPE, visitRPC.get("HideActivePlotsRPC"));
        mState.notify(0);
        synchronize();
    }

    /**
     * Set the active plot to the given index
     * 
     * @param index
     *            The index of the plot to make active
     */
    public synchronized void setActivePlots(int index) {
        JsonArray list = new JsonArray();
        list.add(new JsonPrimitive(index));

        setActivePlots(list);
    }

    /**
     * Set the active plot to the given index
     * 
     * @param index
     *            The index of the plot to make active
     */
    public synchronized void setActivePlots(JsonArray index) {
        mState.set(0, RPCTYPE, visitRPC.get("SetActivePlotsRPC"));
        mState.set(0, "activePlotIds", (new Gson()).toJsonTree(index));
        mState.notify(0);
        synchronize();
    }

    public synchronized void activateDatabase(String filename) {
        mState.set(0, RPCTYPE, visitRPC.get("ActivateDatabaseRPC"));
        mState.set(0, "database", filename);
        mState.notify(0);
        synchronize();
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
        getMetaData(filename);
        
        mState.set(0, RPCTYPE, visitRPC.get("OpenDatabaseRPC"));
        mState.set(0, DATABASE, filename);
        mState.set(0, INTARG1, timeState);
        mState.set(0, "boolFlag", addDefaultPlots);
        mState.set(0, STRINGARG1, forcedFileType);

        mState.notify(0);

        synchronize();
    }

    public synchronized void closeDatabase(String filename) {
        mState.set(0, RPCTYPE, visitRPC.get("CloseDatabaseRPC"));
        mState.set(0, DATABASE, filename);

        mState.notify(0);

        synchronize();
    }

    /**
     * @param plot_type
     * @param name
     * @return
     */
    private synchronized int getEnabledID(String plotType, String name) {
        JsonArray names = mState.get(14, "name").getAsJsonArray();
        JsonArray types = mState.get(14, "type").getAsJsonArray();
        JsonArray enabled = mState.get(14, "enabled").getAsJsonArray();

        List<String> mapper = new ArrayList<String>();

        for (int i = 0; i < names.size(); ++i) {
            if (enabled.get(i).getAsInt() == 1
                    && plotType.equals(types.get(i).getAsString())) {
                mapper.add(names.get(i).getAsString());
            }
        }

        java.util.Collections.sort(mapper);

        for (int i = 0; i < mapper.size(); ++i) {
            if (name.equals(mapper.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @param plot_type
     * @param plot_var
     */
    private synchronized void addPlotByID(int plotType, String plotVar) {
        mState.set(0, RPCTYPE, visitRPC.get("AddPlotRPC"));
        mState.set(0, "plotType", plotType);
        mState.set(0, "variable", plotVar);
        mState.notify(0);
        synchronize();
    }

    /**
     * @param plot_name
     * @param plot_var
     */
    public synchronized void addPlot(String plotName, String plotVar) {
        int index = getEnabledID("plot", plotName);

        if (index >= 0) {
            addPlotByID(index, plotVar);
        }
    }

    /**
     * @param plot_type
     */
    private synchronized void addOperatorByID(int opType) {
        mState.set(0, RPCTYPE, visitRPC.get("AddOperatorRPC"));
        mState.set(0, "operatorType", opType);
        mState.notify(0);
        synchronize();
    }

    /**
     * @param operator_name
     */
    public synchronized void addOperator(String operatorName) {
        int index = getEnabledID("operator", operatorName);

        if (index >= 0) {
            addOperatorByID(index);
        }
    }

    /**
     * 
     */
    public synchronized void setView3D() {
        mState.set(0, RPCTYPE, visitRPC.get("SetView3DRPC"));
        mState.notify(0);

        synchronize();
    }

    /**
     * 
     * @param up
     * @param normal
     */
    public synchronized void updateView(List<Double> up, List<Double> normal) {
        int view3DIndex = mState.getIndexFromTypename("View3DAttributes");

        mState.set(view3DIndex, "viewUp", up);
        mState.set(view3DIndex, "viewNormal", normal);

        mState.notify(view3DIndex);

        synchronize();

        setView3D();
    }

    /**
     * 
     * @param database
     */
    public synchronized void getMetaData(String database) {
        mState.set(0, RPCTYPE, visitRPC.get("RequestMetaDataRPC"));
        mState.set(0, DATABASE, database);
        mState.set(0, "stateNumber", 0);
        mState.notify(0);

        synchronize();
    }

    public synchronized void registerNewWindow(int windowId) {

        JsonObject obj = new JsonObject();
        
        obj.add(ACTION, new JsonPrimitive("RegisterNewWindow"));
        obj.add(WINDOWID, new JsonPrimitive(windowId));
        obj.add("type", new JsonPrimitive("Image"));

        mState.set(0, RPCTYPE, visitRPC.get(EXPORTRPC));
        mState.set(0, STRINGARG1, obj.toString());
        mState.notify(0);

        synchronize();
    }

    public synchronized void updateMouseActions(int windowId, String button,
            double[] start, double[] end, boolean ctrl, boolean shift) {

        JsonObject obj = new JsonObject();
        obj.add(ACTION, new JsonPrimitive("UpdateMouseActions"));
        obj.add("mouseButton", new JsonPrimitive(button));
        obj.add(WINDOWID, new JsonPrimitive(windowId));
        obj.add("start_dx", new JsonPrimitive(start[0]));
        obj.add("start_dy", new JsonPrimitive(start[1]));
        obj.add("end_dx", new JsonPrimitive(end[0]));
        obj.add("end_dy", new JsonPrimitive(end[1]));
        obj.add("ctrl", new JsonPrimitive(ctrl));
        obj.add("shift", new JsonPrimitive(shift));

        mState.set(0, RPCTYPE, visitRPC.get(EXPORTRPC));
        mState.set(0, STRINGARG1, obj.toString());
        mState.notify(0);

        synchronize();
    }

    public synchronized void forceRedraw(int windowId) {

        JsonObject obj = new JsonObject();
        obj.add(ACTION, new JsonPrimitive("ForceRedraw"));
        obj.add(WINDOWID, new JsonPrimitive(windowId));

        mState.set(0, RPCTYPE, visitRPC.get(EXPORTRPC));
        mState.set(0, STRINGARG1, obj.toString());
        mState.notify(0);

        synchronize();
    }

    public synchronized void getFileList(String host, String remotePath) {

        JsonObject obj = new JsonObject();
        obj.add(ACTION, new JsonPrimitive("GetFileList"));
        obj.add("host", new JsonPrimitive(host));
        obj.add("path", new JsonPrimitive(remotePath));

        mState.set(0, RPCTYPE, visitRPC.get(EXPORTRPC));
        mState.set(0, STRINGARG1, obj.toString());
        mState.notify(0);

        synchronize();
    }

    public synchronized void hideAllWindows() {
        mState.set(0, RPCTYPE, visitRPC.get("HideAllWindowsRPC"));
        mState.notify(0);

        synchronize();
    }

    public synchronized void iconifyAllWindows() {
        mState.set(0, RPCTYPE, visitRPC.get("IconifyAllWindowsRPC"));
        mState.notify(0);

        synchronize();
    }

    /**
     * 
     * @param windowId
     * @param w
     * @param h
     */
    public synchronized void resizeWindow(int windowId, int w, int h) {
        mState.set(0, RPCTYPE, visitRPC.get("ResizeWindowRPC"));
        mState.set(0, WINDOWID, windowId);
        mState.set(0, INTARG1, w);
        mState.set(0, "intArg2", h);
        mState.notify(0);

        synchronize();
    }

    public void moveWindow(int win, int x, int y) {
        mState.set(0, RPCTYPE, visitRPC.get("MoveWindowRPC"));
        mState.set(0, WINDOWID, win);
        mState.set(0, INTARG1, x);
        mState.set(0, "intArg2", y);
        mState.notify(0);

        synchronize();
    }
 
    public List<String> getQueries()
    {
        AttributeSubject queryList = getViewerState().getAttributeSubjectFromTypename("QueryList");
        List<String> queries = queryList.getAsStringVector("names");
        List<String> result = new ArrayList<String>();
        
        List<Integer> types = queryList.getAsIntVector("types");
        //DatabaseQuery = 0, PointQuery = 1, LineQuery = 2
        
        for(int i = 0; i < queries.size(); ++i)
        {
            if (types.get(i) == 0) {
                result.add(queries.get(i));
            }
        }
        
        return result;
    }
    
    public List<String> getQueriesOverTime()
    {
        AttributeSubject queryList = getViewerState().getAttributeSubjectFromTypename("QueryList");
        List<String> queries = queryList.getAsStringVector("names");
        List<String> result = new ArrayList<String>();
        
        List<Integer> types = queryList.getAsIntVector("types");
        List<Integer> mode = queryList.getAsIntVector("queryMode");
        
        //DatabaseQuery = 0, PointQuery = 1, LineQuery = 2
        //QueryOnly = 0, QueryAndTime, TimeOnly
        
        for(int i = 0; i < queries.size(); ++i)
        {
            if (types.get(i) == 0 && mode.get(i) != 0) {
                result.add(queries.get(i));
            }
        }
        
        return result;
    }
    
    public void query(String queryName) {
        System.out.println(mState.get(0).get("queryParams"));
        //mState.set(0, RPCTYPE, visitRPC.get("QueryRPC"));
        //mState.notify(0);
    }

    
    public Map<String, Integer> getTimeSliderCurrentState() {
        
            AttributeSubject wi = getViewerState().getAttributeSubjectFromTypename("WindowInformation");

            List<String> timeSliders = wi.getAsStringVector("timeSliders");
            List<Integer> timeSliderStates = wi.getAsIntVector("timeSliderCurrentStates");
                    
            Map<String,Integer> timeMap = new HashMap<String, Integer>();
            
            for(int i = 0; i < timeSliders.size(); ++i)
            {
                timeMap.put(timeSliders.get(i), timeSliderStates.get(i));
            }

            return timeMap;
    }
    
    public int timeSliderGetNStates() {
        
        int nStates = 1;
        
        AttributeSubject windowInformation = getViewerState().getAttributeSubjectFromTypename("WindowInformation");
        int active = windowInformation.getAsInt("activeTimeSlider");
        
        if(active == -1) {
            return nStates;
        }

        String ts = windowInformation.getAsStringVector("timeSliders").get(active);
        
        AttributeSubject keyframeAttributes = getViewerState().getAttributeSubjectFromTypename("KeyframeAttributes");
        boolean enabled = keyframeAttributes.get("enabled").getAsBoolean();
        
        if(enabled && ts.equals("Keyframe animation")) {
            nStates = keyframeAttributes.getAsInt("nFrames");
        }
        else {
            AttributeSubject dblist = getViewerState().getAttributeSubjectFromTypename("DatabaseCorrelationList");
            
            JsonArray list = dblist.get("correlations").getAsJsonArray();
            
            for(int i = 0; i < list.size(); ++i) {
                AttributeSubject e = new AttributeSubject(list.get(i));
                if(ts.equals(e.getAsString("name")))
                {
                    nStates = e.getAsInt("numStates");
                    break;
                } 
            }
        }
        
        return nStates;
    }
    
    public void animationReversePlay() {
        mState.set(0, RPCTYPE, visitRPC.get("AnimationReversePlayRPC"));
        mState.notify(0);
    }
    

    public void animationStop() {
        mState.set(0, RPCTYPE, visitRPC.get("AnimationStopRPC"));
        mState.notify(0);

        synchronize();
    }
    
    public void animationPlay() {
        mState.set(0, RPCTYPE, visitRPC.get("AnimationPlayRPC"));
        mState.notify(0);
    }
    
    public void animationPreviousState() {
        mState.set(0, RPCTYPE, visitRPC.get("TimeSliderPreviousStateRPC"));
        mState.notify(0);

        synchronize();
    }
    
    public void animationNextState() {
        mState.set(0, RPCTYPE, visitRPC.get("TimeSliderNextStateRPC"));
        mState.notify(0);

        synchronize();
    }
    
    public synchronized void updateDBPluginInfo(String hostName) {
        mState.set(0, RPCTYPE, visitRPC.get("UpdateDBPluginInfoRPC"));
        mState.set(0, "programHost", hostName);
        mState.notify(0);

        synchronize();
            
    }
    
    public synchronized void processCommands(String commands) {

        int index = mState.getIndexFromTypename("ClientMethod");
        AttributeSubject as = mState
                .getAttributeSubjectFromTypename("ClientMethod");

        String processCommands = commands;

        processCommands += "\n";
        processCommands = "raw:" + processCommands;

        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(processCommands));

        as.set("stringArgs", array);
        as.set("methodName", new JsonPrimitive("Interpret"));

        mState.notify(index);

        synchronize();
    }

    public synchronized void synchronize() {

        try {
            syncId = (int) ((double) Integer.MAX_VALUE * Math.random());

            syncAtts.set(SYNCTAG, new JsonPrimitive(syncId));

            mState.notify(syncAtts.getId());

            // reset value
            syncAtts.set(SYNCTAG, new JsonPrimitive(-1));

            //mutex.acquire();
            mutex.tryAcquire(30, TimeUnit.SECONDS);
            
        } catch (InterruptedException e) {
            // ignore InterruptedException
        	mutex.release(0);
        }
    }
}
