package visit.java.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static final String ATTRID = "attrId";
    /**
     * Data JSON component
     */
    private JsonObject data;

    /**
     * API JSON component
     */
    private JsonObject api;

    /**
     * API Update structures
     */
    private Updater update;

    /**
     * 
     */
    private List<AttributeSubjectCallback> callbackList;

    /**
     * 
     */
    public interface AttributeSubjectCallback {
        public boolean update(AttributeSubject subject);
    }

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
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @return The data JSON object
     */
    public JsonArray getData() {
        return data.getAsJsonArray("contents");
    }

    /**
     * @return The API JSON object
     */
    public JsonObject getApi() {
        return api.getAsJsonObject("api");
    }

    /**
     * 
     * @param a
     * @param b
     * @return
     */
    private boolean typeEquals(JsonPrimitive ap, JsonPrimitive bp) {
        boolean result = false;

        if (ap.isBoolean() && bp.isBoolean()) {
            result = true;
        } else if (ap.isNumber() && bp.isNumber()) {
            result = true;
        } else if (ap.isString() && bp.isString()) {
            result = true;
        }
        return result;
    }

    private boolean typeEquals(JsonElement a, JsonElement b) {

        boolean result = false;

        if (a.isJsonArray() && b.isJsonArray()) {
            result = true;
        } else if (a.isJsonObject() && b.isJsonObject()) {
            result = true;
        } else if (a.isJsonPrimitive() && b.isJsonPrimitive()) {
            JsonPrimitive ap = a.getAsJsonPrimitive();
            JsonPrimitive bp = b.getAsJsonPrimitive();
            result = typeEquals(ap, bp);
        }

        return result;
    }

    /**
     * 
     * @param key
     * @param value
     */
    public void set(String key, JsonElement value) {

        int index = getApi().get(key).getAsJsonObject().get(ATTRID).getAsInt();
        JsonElement p = getData().get(index);

        // Check if they are the same type..
        if (!typeEquals(p, value)) {
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

        int index = getApi().get(key).getAsJsonObject().get(ATTRID).getAsInt();
        String type = getApi().get(key).getAsJsonObject().get("type")
                .getAsString();

        if (update.contains(index)) {
            return update.get(index);
        }
        if (type.indexOf("AttributeGroup") >= 0) {
            if (type.indexOf("List") >= 0 || type.indexOf("Vector") >= 0) {
                JsonArray ldata = getData().get(index).getAsJsonArray();
                JsonArray tmpArray = new JsonArray();
                for (int i = 0; i < ldata.size(); ++i) {
                    JsonObject obj = new JsonObject();
                    obj.add("api",
                            getApi().get(key).getAsJsonObject().get("api"));
                    obj.add("data", ldata.get(i));
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

    public String getAsString(String key) {
        return get(key).getAsString();
    }

    public int getAsInt(String key) {
        return get(key).getAsInt();
    }

    public double getAsFloat(String key) {
        return get(key).getAsFloat();
    }

    public double getAsDouble(String key) {
        return get(key).getAsDouble();
    }

    public List<String> getAsStringVector(String key) {

        List<String> vecs = new ArrayList<String>();
        JsonArray array = get(key).getAsJsonArray();

        for (int i = 0; i < array.size(); ++i) {
            vecs.add(array.get(i).getAsString());
        }

        return vecs;
    }

    public List<Integer> getAsIntVector(String key) {

        List<Integer> vecs = new ArrayList<Integer>();
        JsonArray array = get(key).getAsJsonArray();

        for (int i = 0; i < array.size(); ++i) {
            vecs.add(array.get(i).getAsInt());
        }

        return vecs;
    }

    public List<Long> getAsLongVector(String key) {

        List<Long> vecs = new ArrayList<Long>();
        JsonArray array = get(key).getAsJsonArray();

        for (int i = 0; i < array.size(); ++i) {
            vecs.add(array.get(i).getAsLong());
        }

        return vecs;
    }

    public List<Float> getAsFloatVector(String key) {

        List<Float> vecs = new ArrayList<Float>();
        JsonArray array = get(key).getAsJsonArray();

        for (int i = 0; i < array.size(); ++i) {
            vecs.add(array.get(i).getAsFloat());
        }

        return vecs;
    }

    public List<Double> getAsDoubleVector(String key) {

        List<Double> vecs = new ArrayList<Double>();
        JsonArray array = get(key).getAsJsonArray();

        for (int i = 0; i < array.size(); ++i) {
            vecs.add(array.get(i).getAsDouble());
        }

        return vecs;
    }

    private JsonElement getVectorAttr(JsonObject lapi, JsonArray ldata,
            int index, String key) {
        JsonArray cdata = ldata.get(index).getAsJsonArray();
        JsonArray ctmpArray = new JsonArray();
        for (int i = 0; i < cdata.size(); ++i) {
            JsonObject cobj = new JsonObject();
            cobj.add("api", lapi.get(key).getAsJsonObject().get("api"));
            cobj.add("data", ldata.get(i));
            ctmpArray.add(cobj);
        }
        return ctmpArray;
    }

    private JsonElement getObjectAttr(JsonObject lapi, JsonArray ldata,
            int index, String key) {
        JsonObject cobj = new JsonObject();
        cobj.add("api", lapi.get(key).getAsJsonObject().get("api"));
        cobj.add("data", ldata.get(index));
        return cobj;
    }

    public JsonElement getAttr(JsonElement obj, String key) {

        JsonElement result = JsonNull.INSTANCE;

        if (!obj.isJsonObject() && !obj.getAsJsonObject().has("api")) {
            return result;
        }

        JsonObject attr = obj.getAsJsonObject();

        JsonObject lapi = attr.get("api").getAsJsonObject();
        JsonArray ldata = attr.get("data").getAsJsonArray();

        if (!lapi.has(key)) {
            return result;
        }

        int index = lapi.get(key).getAsJsonObject().get(ATTRID).getAsInt();
        String type = lapi.get(key).getAsJsonObject().get("type").getAsString();

        // if this recurses further return that instead..
        if (type.indexOf("AttributeGroup") < 0) {
            result = ldata.get(index);
        } else if (type.indexOf("List") >= 0 || type.indexOf("Vector") >= 0) {
            result = getVectorAttr(lapi, ldata, index, key);
        } else {
            result = getObjectAttr(lapi, ldata, index, key);
        }

        return result;
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
            // unable to write data out..
            Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
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

        private Integer id;
        private String typename;
        private Map<Integer, JsonElement> contents;
        private Map<Integer, JsonElement> metadata;

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
