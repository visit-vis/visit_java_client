package visit.java.client.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import visit.java.client.AttributeSubject;
import visit.java.client.FileInfo;
import visit.java.client.AttributeSubject.AttributeSubjectCallback;
import visit.java.client.ViewerMethods;

public class DatabaseInformation extends VisItComponent {
	public interface DatabaseInformationUpdateCallback
	{
		void vars(FileInfo info);
	};
	
	ArrayList<DatabaseInformationUpdateCallback> cbList = new ArrayList<DatabaseInformationUpdateCallback>();
	
	/**
	 * This class consolidates everything about the file information
	 * and returns one object that allows for selection of plots
	 * and variables.... (REPLACE fileInfo)
	 * @param m
	 */
	public DatabaseInformation(ViewerMethods m) {
		super(m);		
		setup();
	}
	
	public boolean register(DatabaseInformationUpdateCallback cb) {
		return cbList.add(cb);
	}
	
	public boolean unregister(DatabaseInformationUpdateCallback cb) {
		return cbList.remove(cb);
	}
	
	public void cleanup() {
		cbList.clear();
		methods.getViewerState().unregisterCallback("ViewerRPC", viewerRPCAtts);
		methods.getViewerState().unregisterCallback("ExpressionList", expressionList);
		methods.getViewerState().unregisterCallback("avtDatabaseMetaData", avtDatabaseMetaData);
//		methods.getViewerState().unregisterCallback("GlobalAttributes", globalAtts);
//		methods.getViewerState().unregisterCallback("WindowAttributes", windowAtts);
//		methods.getViewerState().unregisterCallback("SILAttributes", silAtts);
	}
	
	//AttributeSubjectCallback silAtts, globalAtts, windowAtts;
	AttributeSubjectCallback viewerRPCAtts, expressionList , avtDatabaseMetaData;
	
	private void setup() {
		///1. register for OpenDatabase & ReOpenDatabase calls
		///see if there are any others..
		viewerRPCAtts = new AttributeSubjectCallback() {
			
			@Override
			public boolean update(AttributeSubject subject) {
				Map<Integer, String> rpc = methods.getReverseVisItRPC();
				int RPCType = subject.get("RPCType").getAsInt();
				String rpc_action = rpc.get(RPCType);
				if(rpc_action.contains("Database")) {
					//System.out.println("Database");
					updateVariableList();
				} else if(rpc_action.contains("Animation")) {
					//System.out.println("Animation");
					updateVariableList();
				} else if(rpc_action.contains("Time")) {
					//System.out.println("Time");
					updateVariableList();
				}
				return false;
			}
		};
		
		methods.getViewerState().registerCallback("ViewerRPC", viewerRPCAtts);
		
		///The gui listens for GlobalAttributes...
//		globalAtts = new AttributeSubjectCallback() {
//			
//			@Override
//			public boolean update(AttributeSubject subject) {
//				//System.out.println("Global Atts");
//				updateVariableList();
//				return false;
//			}
//		};
//		
//		methods.getViewerState().registerCallback("GlobalAttributes", globalAtts);
		
//		windowAtts = new AttributeSubjectCallback() {
//			
//			@Override
//			public boolean update(AttributeSubject subject) {
//				//System.out.println("WindowAtts");
//				updateVariableList();
//				return false;
//			}
//		};
//		
//		methods.getViewerState().registerCallback("WindowAttributes", windowAtts);

		
		///2. Listen for Expression updates..
		expressionList = new AttributeSubjectCallback() {
			
			@Override
			public boolean update(AttributeSubject subject) {
					updateVariableList();
				return true;
			}
		};
		
		methods.getViewerState().registerCallback("ExpressionList", expressionList);
		
		///3. Listen for SIL updates
//		silAtts = new AttributeSubjectCallback() {
//			
//			@Override
//			public boolean update(AttributeSubject subject) {
//					updateSILList();
//				return true;
//			}
//		};
//		
//		methods.getViewerState().registerCallback("SILAttributes", silAtts);
		
//		methods.getViewerState().registerCallback("SILRestrictionAttributes", new AttributeSubjectCallback() {
//			
//			@Override
//			public boolean update(AttributeSubject subject) {
//					updateSILList();
//				return true;
//			}
//		});
//		
		avtDatabaseMetaData =  new AttributeSubjectCallback() {
			
			@Override
			public boolean update(AttributeSubject arg0) {
				//System.out.println("updating meta data");
				updateVariableList();
				return true;
			}
		};
		
		methods.getViewerState().registerCallback("avtDatabaseMetaData", avtDatabaseMetaData);
	}

	/**
	 * 
	 */
	public void updateVariableList() {
//		System.out.println("Updating Variable List");
		///something has changed, request meta data from active source...
//		AttributeSubject subject = methods.getViewerState().getAttributeSubjectFromTypename("WindowInformation");
//		String source = subject.get("activeSource").getAsString();
//		methods.getMetaData(source);
		
		FileInfo fi = new FileInfo();

		updateDatabaseMetaData(fi);
		updateExpressionList(fi);
		
        for(int i = 0; i < cbList.size(); ++i) {
        	cbList.get(i).vars(fi);
        }
	}
	
	public void updateSILList() {
		//System.out.println("Updating SIL List");
	}
	
	public void updateExpressionList(FileInfo fi) {
		//System.out.println("Updating Expression List");
		ArrayList<String> expressionNames = new ArrayList<String>();
		
		AttributeSubject exprList = methods.getViewerState().getAttributeSubjectFromTypename("ExpressionList");
		JsonArray expressions = exprList.get("expressions").getAsJsonArray();
		for(int i = 0; i < expressions.size(); ++i) {
			JsonObject obj = expressions.get(i).getAsJsonObject();
			AttributeSubject s = new AttributeSubject(obj);
			expressionNames.add(s.get("name").getAsString());
			boolean autoExpression = s.get("autoExpression").getAsBoolean();
	
			if(autoExpression == true)
				continue;
			
			int type = s.get("type").getAsInt();
			String name = s.get("name").getAsString();
			
			//System.out.println(type);
			//System.out.println(s.getApi());
			//System.out.println(s.getData());
			if(type == 1 && fi.getScalars().contains(name) == false) {
				fi.getScalars().add(name);		
			}else if(type == 2 && fi.getVectors().contains(name) == false) {
				fi.getVectors().add(name);		
			}
		}
	}
	
	public void updateDatabaseMetaData(FileInfo fi) {
		
		AttributeSubject arg0 = methods.getViewerState().getAttributeSubjectFromTypename("avtDatabaseMetaData");
        
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

        fi.getMeshes().addAll(outputArray.get("meshes"));
        fi.getScalars().addAll(outputArray.get("scalars"));
        fi.getVectors().addAll(outputArray.get("vectors"));
        fi.getMaterials().addAll(outputArray.get("materials"));

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
	}
}
