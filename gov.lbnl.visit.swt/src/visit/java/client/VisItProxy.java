/**
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visit.java.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StreamCorruptedException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @authors hkq, tnp
 */
public class VisItProxy {

    /**
     * create interface callback
     */
    public interface VisItInitializedCallback {
        public void initialized();
    }

    /**
     * 
     */
    Semaphore sem = new Semaphore(0);

    /**
     * 
     */
    public static final int BUFSIZE = 4096;

    /**
     * 
     */
    private String visitHost, visitPort;

    /**
     * 
     */
    private String visitSecurityKey;

    /**
     * 
     */
    private Map<String, Integer> visitRPC;
    
    /**
     * 
     */
    private InputStreamReader inputConnection;

    /**
     * 
     */
    private OutputStreamWriter outputConnection;

    /**
     * 
     */
    private Socket inputSocket, outputSocket;

    /**
     * 
     */
    private Thread thread;
    private VisItThread threadRunnable;

    /**
     * 
     */
    private static final byte ASCIIFORMAT = 0;

    /**
     * 
     */
    private ViewerState state;

    /**
     * 
     */
    private ViewerMethods methods;

    /**
     * 
     */
    private Header header = new Header();

    /**
     * 
     */
    private VisItInitializedCallback callback = null;
    
    /**
     * 
     */
    boolean mUseTunnel = false;
    
    /**
     * 
     */
    Session mTunnelSession = null;
    
    /**
     * 
     */
    private static final String LOCALHOST = "localhost";
    
    /**
     * The constructor
     */
    public VisItProxy() {
    }

    /**
     * 
     */
    public void setParameters(String userName, String instanceId,
            String dataType, int windowWidth, int windowHeight, int windowId) {
        header.setName(userName);
        header.setPassword(instanceId);
        header.setCanRender(dataType);
        header.setGeometry(windowWidth + "x" + windowHeight);
        header.windowIds = new ArrayList<Integer>();
        header.windowIds.add(windowId);

    }

    public void setTunneling(boolean tunnel, Session ts) {
        mUseTunnel = tunnel;
        mTunnelSession = ts;
    }

    /**
     * @param host
     * @param port
     * @param password
     * @return
     */
    private boolean handshake(String host, int port) {
        try {
            Gson gson = new Gson();

            Socket socket = new Socket(mUseTunnel ? LOCALHOST : host, port);
            OutputStreamWriter writer = new OutputStreamWriter(
                    socket.getOutputStream());
            InputStreamReader reader = new InputStreamReader(
                    socket.getInputStream());

            String headerstr = gson.toJson(header);

            writer.write(headerstr);
            writer.flush();

            String message = "";
            
            int len = 0;
            do {
                char[] cbuf = new char[1024];
                len = reader.read(cbuf);
                
                if(len <= 0) {
                    break;
                }
                
                String msg = new String(cbuf, 0, len);
                message += msg;
                
            } while(len > 0);

            JsonElement e = gson.fromJson(message, JsonElement.class);
            JsonObject jo = e.getAsJsonObject();

            visitHost = jo.get("host").getAsString();
            visitPort = jo.get("port").getAsString();
            visitSecurityKey = jo.get("securityKey").getAsString();
            JsonArray visitRpc = jo.get("rpc_array").getAsJsonArray();
            
            visitRPC = new HashMap<String, Integer>();
            for(int i = 0; i < visitRpc.size(); ++i) {
                visitRPC.put(visitRpc.get(i).getAsString(), i);
            }
            
            if (mUseTunnel) {
                mTunnelSession.setPortForwardingL(
                        Integer.parseInt(visitPort), LOCALHOST,
                        Integer.parseInt(visitPort));
            }

            socket.close();

            return true;
        } catch (ConnectException e) {
            ///throw exception when connection fails..
            Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
        } catch (Exception e) {
            ///throw exception when connection fails..
            Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
        }
        return false;
    }

    /**
     * @param host
     * @param port
     * @param password
     * @param type
     * @return
     */
    public boolean connect(String host, int port) {
        if (!handshake(host, port)) {
            return false;
        }
        try {

            inputSocket = new Socket(mUseTunnel ? LOCALHOST : visitHost,
                    Integer.valueOf(visitPort));
            inputConnection = new InputStreamReader(
                    inputSocket.getInputStream());

            outputSocket = new Socket(mUseTunnel ? LOCALHOST : visitHost,
                    Integer.valueOf(visitPort));
            outputConnection = new OutputStreamWriter(
                    outputSocket.getOutputStream());

            // Handle initial connection
            char[] cbuf = new char[1024];

            // read 100 bytes
            InputStreamReader isr = new InputStreamReader(
                    outputSocket.getInputStream());
            isr.read(cbuf);

            cbuf[0] = ASCIIFORMAT;

            for (int i = 0; i < visitSecurityKey.length(); ++i) {
                cbuf[6 + 10 + i] = visitSecurityKey.charAt(i);
            }

            OutputStreamWriter osw = new OutputStreamWriter(
                    inputSocket.getOutputStream());
            osw.write(cbuf);
            osw.flush();

            // End - Handle initial connection
            state = new ViewerState();

            state.setConnection(outputConnection);

            threadRunnable = new VisItThread(inputConnection);
            thread = new Thread(threadRunnable);
            thread.setDaemon(true);
            thread.start();

            /** ! block until all data is in */
            sem.acquire();

            if (callback != null) {
                callback.initialized();
            }
            
            methods = new ViewerMethods(state, visitRPC);

            //state has synched at this point..
            
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
            return false;
        }
        return true;
    }

    public void disconnect() {
        getViewerMethods().close();
        threadRunnable.quitThread();
        thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread arg0, Throwable arg1) {
                /// if there is an uncaught exception ignore it..
            }
        });

        try {
            thread.join();
        } catch (InterruptedException e) {
            /// wait for disconnection to end the second thread..
        }
    }

    /**
     * @return
     */
    public ViewerState getViewerState() {
        return state;
    }

    /**
     * @return
     */
    public ViewerMethods getViewerMethods() {
        return methods;
    }

    /**
     * 
     * @param cb
     */
    public void setInitializedCallback(VisItInitializedCallback cb) {
        callback = cb;
    }

    /**
     *
     */
    class Header {
        private String name;
        private String geometry;
        private String password;
        private String canRender;
        List<Integer> windowIds;
        
        public String getCanRender() {
            return canRender;
        }
        
        public void setCanRender(String canRender) {
            this.canRender = canRender;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public String getGeometry() {
            return geometry;
        }
        
        public void setGeometry(String geometry) {
            this.geometry = geometry;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     *
     */
    class VisItThread implements Runnable {
        private InputStreamReader inputConnection;
        private Gson gson;
        private boolean qThread;
        private static final int MAX_STATES = 135;

        VisItThread(InputStreamReader i) {
            gson = new Gson();
            inputConnection = i;
            qThread = false;
        }

        void quitThread() {
            qThread = true;
            try {
                inputConnection.close();
            } catch (IOException e) {
                ///log quit thread..
                Logger.getGlobal().log(Level.INFO, e.getMessage(), e);
            }
        }

        private int count(String str, String findStr) {
            int lastIndex = 0;
            int count = 0;

            while (lastIndex != -1) {

                lastIndex = str.indexOf(findStr, lastIndex);

                if (lastIndex != -1) {
                    count++;
                    lastIndex += findStr.length();
                }
            }

            return count;
        }

        private int[] parseEntryHelper(int si, int ei, StringBuilder inputBuffer, StringBuilder partialEntry) {
            
            int mnsi = si;
            int mnei = ei;
            
            if (mnsi < 0 && mnei >= 0) {                
                mnei += "}".length();
                partialEntry.append(inputBuffer.subSequence(0, mnei));
                inputBuffer.delete(0, mnei);
            } else if (mnsi >= 0 && mnei < 0) {
                mnsi += "{".length();
                partialEntry.append(inputBuffer.subSequence(0, mnsi));
                inputBuffer.delete(0, mnsi);
            } else if (mnsi < mnei) {
                mnsi += "{".length();
                partialEntry.append(inputBuffer.subSequence(0, mnsi));
                inputBuffer.delete(0, mnsi);
            } else {
                mnei += "}".length();
                partialEntry.append(inputBuffer.subSequence(0, mnei));
                inputBuffer.delete(0, mnei); 
            }
            return new int[] { mnsi, mnei };
        }
        
        private void parseEntry(StringBuilder inputBuffer, StringBuilder partialEntry) {
            // for now JSON parser has to start with object..
            int mnsi = inputBuffer.indexOf("{");
            int mnei = inputBuffer.indexOf("}");

            while (mnsi >= 0 || mnei >= 0) {
            
                int[] res = parseEntryHelper(mnsi, mnei, inputBuffer, partialEntry);
                
                mnsi = res[0];
                mnei = res[1];
                
                String tmp = partialEntry.toString().trim();

                if (count(tmp, "{") > 0 &&
                    count(tmp, "{") == count(tmp, "}")) {
                    try {
                        partialEntry.setLength(0);
                        tmp = tmp.replace("\\\"", "");
                        JsonElement el = gson.fromJson(tmp, JsonElement.class);
                        JsonObject jo = el.getAsJsonObject();
                        
                        // update state..
                        VisItProxy.this.state.update(jo);
                    } catch (Exception e) {
                        /// log this failure to parse..
                        Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
                    }
                    
                    if (VisItProxy.this.state.getStates().size() == MAX_STATES) {
                        sem.release();
                    }
                }

                mnsi = inputBuffer.indexOf("{");
                mnei = inputBuffer.indexOf("}");
            }
        }
        
        /**
         * 
         */
        @Override
        public void run() {

            // stitches together one map_node entry
            StringBuilder partialEntry = new StringBuilder("");
            
            // holds input data buffer
            StringBuilder inputBuffer = new StringBuilder("");
            char[] data = new char[VisItProxy.BUFSIZE];

            try {
                while (true) {

                    int len = 0;

                    len = inputConnection.read(data);

                    if (qThread || len == 0 || data == null) {
                        throw new StreamCorruptedException("Quitting Thread Due to Processing Failure");
                    }

                    inputBuffer.append(data, 0, len);

                    parseEntry(inputBuffer, partialEntry);
                }
            } catch (Exception e) {
                ///catch Exception and Quit Thread
                Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

}
