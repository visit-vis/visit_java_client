package visit.java.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import visit.java.client.AttributeSubject.AttributeSubjectCallback;

/**
 * 
 * @authors hkq, tnp
 */
public class ViewerState {

    /**
     * 
     */
    private List<AttributeSubject> states;

    /**
     * 
     */
    private Map<String, Integer> typenameToState;

    /**
     * 
     */
    private OutputStreamWriter output;

    /**
     * 
     */
    public ViewerState() {
        states = new ArrayList<AttributeSubject>();
        typenameToState = new HashMap<String, Integer>();
    }

    /**
     * 
     */
    public synchronized List<AttributeSubject> getStates() {
        return states;
    }
    
    /**
     * @param jo
     * @return
     */
    public synchronized boolean update(JsonObject jo) {
        // if it does not have id, then class is not updated properly.
        if (!jo.has("id")) {
            return false;
        }
        JsonElement e = jo.get("id");
        int id = e.getAsInt();

        if (id < states.size()) {
            states.get(id).update(jo);
        } else {
            int diff = id - states.size();

            states.add(new AttributeSubject());
            for (int i = 0; i < diff; ++i) {
                states.add(new AttributeSubject());
            }
            states.get(id).update(jo);
        }

        typenameToState.put(states.get(id).getTypename(), states.get(id)
                .getId());

        return true;
    }

    /**
     * 
     */
    public int getIndexFromTypename(String typename) {
        return typenameToState.get(typename);
    }

    public AttributeSubject getAttributeSubjectFromTypename(String typename) {

        int index = getIndexFromTypename(typename);

        if (index < 0) {
            return null;
        }
        return states.get(index);
    }

    /**
     * @param index
     * @return
     */
    public synchronized AttributeSubject get(int index) {
        if (index < states.size()) {
            return states.get(index);
        }
        return null;
    }

    /**
     * 
     * @param index
     * @param key
     * @return
     */
    public synchronized JsonElement get(int index, String key) {
        if (index >= 0 && index < states.size()) {
            return states.get(index).get(key);
        }
        return null;
    }

    /**
     * 
     * @param o
     * @return
     */
    public JsonElement convertToJsonElement(Object o) {
        JsonElement e = null;

        if (o instanceof Boolean) {
            e = new JsonPrimitive((Boolean) o);
        } else if (o instanceof Number) {
            e = new JsonPrimitive((Number) o);
        } else if (o instanceof String) {
            e = new JsonPrimitive((String) o);
        } else if (o instanceof JsonElement) {
            e = ((JsonElement) o);
        } else if (o instanceof Collection) {
            e = convertToJsonElement(o);
        } else {
            e = new JsonPrimitive(o.toString());
        }

        return e;
    }

    /**
     * 
     * @param values
     * @return
     */
    public JsonElement convertToJsonElement(Collection<?> values) {
        JsonArray array = new JsonArray();

        Iterator<?> itr = values.iterator();
        while (itr.hasNext()) {
            JsonElement o = convertToJsonElement(itr.next());

            // Unconvertable to json..
            if (o == null) {
                return null;
            }

            array.add(o);
        }
        return array;
    }

    /**
     * 
     * @param index
     * @param key
     * @param value
     */
    public synchronized void set(int index, String key, Boolean value) {
        set(index, key, new JsonPrimitive(value));
    }

    /**
     * 
     * @param index
     * @param key
     * @param value
     */
    public synchronized void set(int index, String key, Number value) {
        set(index, key, new JsonPrimitive(value));
    }

    /**
     * 
     * @param index
     * @param key
     * @param value
     */
    public synchronized void set(int index, String key, String value) {
        set(index, key, new JsonPrimitive(value));
    }

    /**
     * 
     * @param index
     * @param key
     * @param values
     */
    public synchronized boolean set(int index, String key, Collection<?> values) {
        JsonElement e = convertToJsonElement(values);
        if (e == null) {
            return false;
        }
        set(index, key, e);
        return true;
    }

    /**
     * 
     * @param index
     * @param key
     * @param value
     */
    public synchronized void set(int index, String key, JsonElement value) {
        if (index >= 0 && index < states.size()) {
            states.get(index).set(key, value);
        }
    }

    /**
     * 
     * @param index
     */
    public synchronized void notify(int index) {
        if (index >= 0 && index < states.size()) {
            states.get(index).notify(output);
        }
    }

    /**
     * 
     * @param id
     * @param callback
     */
    public void registerCallback(String id, AttributeSubjectCallback callback) {
        for (int i = 0; i < states.size(); ++i) {
            String typename = states.get(i).getTypename();

            if (typename.equals(id)) {
                states.get(i).addCallback(callback);
            }
        }
    }

    public void unregisterCallback(String id, AttributeSubjectCallback callback) {
        for (int i = 0; i < states.size(); ++i) {
            String typename = states.get(i).getTypename();

            if (typename.equals(id)) {
                states.get(i).removeCallback(callback);
            }
        }
    }
    /**
     * @param o
     */
    public synchronized void setConnection(OutputStreamWriter o) {
        output = o;
    }
}
