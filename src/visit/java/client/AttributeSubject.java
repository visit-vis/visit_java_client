package visit.java.client;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * This class manages the attributes of a JSON object.
 * 
 * @authors hkq, tnp
 */
public class AttributeSubject {

	/**
	 * Data JSON component
	 */
	public JsonObject data;

	/**
	 * API JSON component
	 */
	public JsonObject api;

	/**
	 * API Update structures
	 */
	private Updater update;

	/**
	 * 
	 */
	public ArrayList<AttributeSubjectCallback> callbackList;

	/**
	 * 
	 */
	public interface AttributeSubjectCallback {
		public void update(AttributeSubject subject);
	};

	/**
	 * The constructor
	 */
	public AttributeSubject() {
		update = new Updater();
		callbackList = new ArrayList<AttributeSubjectCallback>();
	}

	/**
	 * @param jo
	 *            JsonObject to update to
	 * @return Returns true on completion (This appears unnecessary to me.)
	 */
	public boolean update(JsonObject jo) {
		if (jo.has("api")) {
			api = jo;
			update.id = api.get("id").getAsInt();
			update.typename = api.get("typename").getAsString();
		} else {
			data = jo;
			update.clear();
			// tell all listeners object has been updated..
			for (AttributeSubjectCallback cb : callbackList) {
				try {
					cb.update(this);
				}catch(Exception e) {
					System.err.println("a callback failed to update..");
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	/**
	 * @return The data JSON object
	 */
	public JsonArray getData() {
		return data.getAsJsonArray("contents"); // .getAsJsonObject("data");
	}

	/**
	 * @return The API JSON object
	 */
	public JsonObject getApi() {
		return api.getAsJsonObject("api"); // .getAsJsonObject("data");
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private boolean typeEquals(JsonElement a, JsonElement b) {
		if (a.isJsonNull() && b.isJsonNull() || a.isJsonArray()
				&& b.isJsonArray() || a.isJsonArray() && b.isJsonArray())
			return true;

		if (a.isJsonPrimitive() && b.isJsonPrimitive()) {
			JsonPrimitive ap = a.getAsJsonPrimitive();
			JsonPrimitive bp = b.getAsJsonPrimitive();
			if ((ap.isBoolean() && bp.isBoolean())
					|| (ap.isNumber() && bp.isNumber())
					|| (ap.isString() && bp.isString()))
				return true;
		}

		return false;
	}

	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void set(String key, JsonElement value) {

		int index = getApi().get(key).getAsJsonObject().get("attrId").getAsInt();
		JsonElement p = getData().get(index);

		// Check if they are the same type..
		if (!typeEquals(p, value)) {
			System.err.println("Types do not equal each other..");
			return;
		}

		// Add to mod list..
		update.insert(index, value, data.getAsJsonArray("metadata").get(index));

	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public JsonElement get(String key) {

		int index = getApi().get(key).getAsJsonObject().get("attrId").getAsInt();
		String type = getApi().get(key).getAsJsonObject().get("type").getAsString();
		
		if (update.contains(index)) {
			return update.get(index);
		}
		if(type.indexOf("AttributeGroup") >= 0) {
			if(type.indexOf("List") >= 0 || type.indexOf("Vector") >= 0 ) {
				JsonArray data = getData().get(index).getAsJsonArray();
				JsonArray tmpArray = new JsonArray();
				for(int i = 0; i < data.size(); ++i) {
					JsonObject obj = new JsonObject();
					obj.add("api", getApi().get(key).getAsJsonObject().get("api"));
					obj.add("data", data.get(i));
					tmpArray.add(obj);
				}
				return tmpArray;
			} else {
				JsonObject obj = new JsonObject();
				obj.add("api", getApi().get(key).getAsJsonObject().get("api"));
				obj.add("data", getData().get(index));
				return obj;
			}
		}
		return getData().get(index);
	}
	
	public JsonElement getAttr(JsonElement obj, String key) {
		
		if(!obj.isJsonObject() && !obj.getAsJsonObject().has("api")) {
			return JsonNull.INSTANCE;
		}
		
		JsonObject attr = obj.getAsJsonObject();
		
		JsonObject api = attr.get("api").getAsJsonObject();
		JsonArray data = attr.get("data").getAsJsonArray();
		
		if(api.has(key)) {
			int index = api.get(key).getAsJsonObject().get("attrId").getAsInt();
			String type = api.get(key).getAsJsonObject().get("type").getAsString();
			
			/// if this recurses further return that instead..
			if(type.indexOf("AttributeGroup") >= 0) {
				if(type.indexOf("List") >= 0 || type.indexOf("Vector") >= 0 ) {
					JsonArray cdata = data.get(index).getAsJsonArray();
					JsonArray ctmpArray = new JsonArray();
					for(int i = 0; i < cdata.size(); ++i) {
						JsonObject cobj = new JsonObject();
						cobj.add("api", api.get(key).getAsJsonObject().get("api"));
						cobj.add("data", data.get(i));
						ctmpArray.add(cobj);
					}
					return ctmpArray;
				} else {
					JsonObject cobj = new JsonObject();
					cobj.add("api", api.get(key).getAsJsonObject().get("api"));
					cobj.add("data", data.get(index));
					return cobj;
				}
			}
			
			return data.get(index);
		}
		
		return JsonNull.INSTANCE;
	}

	/**
	 * 
	 * @return
	 */
	public int getId() {
		return update.id;
	}

	/**
	 * 
	 * @return
	 */
	public String getTypename() {
		return update.typename;
	}

	/**
	 * 
	 * @param output
	 */
	public void notify(java.io.OutputStreamWriter output) {
		try {
			Gson gson = new Gson();
			String result = gson.toJson(update, Updater.class);
			update.clear();

			output.write(result);
			output.flush();
		} catch (Exception e) {
			System.out.println("Unable to write data to VisIt");
		}
	}

	/**
	 * @param callback
	 *            The AttributeSubjectCallback object to be added to the
	 *            callbackList Arraylist.
	 */
	public void addCallback(AttributeSubjectCallback callback) {
		callbackList.add(callback);
	}

	/**
	 * @param callback
	 *            The AttributeSubjectCallback object to be removed from the
	 *            callbackList Arraylist.
	 */
	public void removeCallback(AttributeSubjectCallback callback) {
		callbackList.remove(callback);
	}

	/**
	 * 
	 * @author hkq
	 */
	private class Updater {

		public Integer id;
		public String typename;
		public ConcurrentHashMap<Integer, JsonElement> contents;
		public ConcurrentHashMap<Integer, JsonElement> metadata;

		/**
		 * 
		 */
		Updater() {
			id = -1;
			typename = "";
			contents = new ConcurrentHashMap<Integer, JsonElement>();
			metadata = new ConcurrentHashMap<Integer, JsonElement>();
		}

		/**
		 * 
		 */
		void clear() {
			contents.clear();
			metadata.clear();
		}

		/**
		 * 
		 * @param index
		 * @param d
		 * @param md
		 */
		void insert(int index, JsonElement d, JsonElement md) {
			contents.put(index, d);
			metadata.put(index, md);
		}

		/**
		 * 
		 * @param index
		 * @return
		 */
		boolean contains(int index) {
			return contents.containsKey(index);
		}

		/**
		 * 
		 * @param index
		 * @return
		 */
		JsonElement get(int index) {
			return contents.get(index);
		}
	}
}