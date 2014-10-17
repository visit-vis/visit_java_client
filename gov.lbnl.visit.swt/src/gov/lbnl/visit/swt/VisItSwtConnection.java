/**
 * 
 */
package gov.lbnl.visit.swt;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.swt.widgets.Shell;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import visit.java.client.AttributeSubject;
import visit.java.client.AttributeSubject.AttributeSubjectCallback;
import visit.java.client.ViewerMethods;
import visit.java.client.ViewerState;
import visit.java.client.VisItProxy;
import visit.java.client.VisItProxy.VisItInitializedCallback;

/**
 * @author hari
 * 
 */
public class VisItSwtConnection implements VisItInitializedCallback,
        AttributeSubjectCallback {

    static final String LOCALHOST = "localhost";

    public enum VISIT_CONNECTION_TYPE {
        CONTROL, IMAGE, DATA
    }

    public interface VisualizationUpdateCallback {
        public void update(VISIT_CONNECTION_TYPE type, byte[] rawData);
    }

    private class VisItConnectionStruct {
        private VISIT_CONNECTION_TYPE connType;
        private VisualizationUpdateCallback callback;

        @SuppressWarnings("unused")
        public VISIT_CONNECTION_TYPE getConnType() {
            return connType;
        }

        public void setConnType(VISIT_CONNECTION_TYPE connType) {
            this.connType = connType;
        }
    }

    /**
     * The VisIt connection manager.
     */
    private VisItProxy client;

    Process process = null;

    JSch jsch = new JSch();

    private ChannelExec channel = null;
    private Session gateway = null;
    private Session session = null;

    private String mGatewayUser = "";
    private String mGateway = "";
    private int mGatewayPort = 0;
    private boolean mUseTunnel = false;

    private String mHost = "";
    private int mPort = -1;
    private String mDir = "";

    private Map<Integer, ArrayList<VisItConnectionStruct>> windowCallbacks;

    /**
     * The status of the VisIt launch.
     */
    private boolean hasInitialized = false;
    private boolean hasVisItConnected = false;

    Shell shell;

    public VisItSwtConnection(Shell s) {

        shell = s;
        // Initialize the connection manager
        client = new VisItProxy();
        windowCallbacks = new HashMap<Integer, ArrayList<VisItConnectionStruct>>();
    }

    public VisItProxy getVisItProxy() {
        return client;
    }

    public String getHostname() {
        return mHost;
    }

    public int getPort() {
        return mPort;
    }

    public String getDirectory() {
        return mDir;
    }

    public List<Integer> getWindowIds() {
        List<Integer> ids = new ArrayList<Integer>();

        // Return empty list if not connected..
        if (!hasVisItConnected) {
            return ids;
        }

        AttributeSubject global = getViewerState()
                .getAttributeSubjectFromTypename("GlobalAttributes");
        JsonArray array = global.get("windows").getAsJsonArray();

        for (int i = 0; i < array.size(); ++i) {
            ids.add(array.get(i).getAsInt());
        }

        return ids;
    }

    /**
     * 
     */
    @Override
    public void initialized() {
        hasInitialized = true;

        /** register client information */
        client.getViewerState().registerCallback("ViewerClientInformation",
                this);
    }

    /**
     * 
     * @return
     */
    public boolean hasInitialized() {
        return hasInitialized;
    }

    /** ! generic callback */
    public void registerCallback(String id, AttributeSubjectCallback callback) {
        client.getViewerState().registerCallback(id, callback);
    }
    
    public void unregisterCallback(String id, AttributeSubjectCallback callback) {
        client.getViewerState().unregisterCallback(id, callback);
    }

    /** ! window callback */
    public boolean registerVisualization(VISIT_CONNECTION_TYPE type, int windowId,
            VisualizationUpdateCallback callback) {

        if (!windowCallbacks.containsKey(windowId)) {
            windowCallbacks.put(windowId,
                    new ArrayList<VisItConnectionStruct>());
        }

        List<VisItConnectionStruct> list = windowCallbacks.get(windowId);

        /// if a connection already exists return false.
        for(int i = 0; i < list.size(); ++i) {
            if(list.get(i) == callback) {
                return false;
            }
        }
        
        VisItConnectionStruct struct = new VisItConnectionStruct();
        struct.callback = callback;
        struct.setConnType(type);
        list.add(struct);
        return true;
    }
    
    public boolean unregisterVisualization(int windowId, VisualizationUpdateCallback callback) {
        
        if (!windowCallbacks.containsKey(windowId)) {
            return true;
        }
        
        List<VisItConnectionStruct> list = windowCallbacks.get(windowId);
        
        for(int i = 0; i < list.size(); ++i) {
            VisItConnectionStruct struct = list.get(i);
            if(struct.callback == callback) {
                list.remove(i);
                return true;
            }
        }
        
        return false;
    }

    /**
     * 
     * @param userName
     * @param instanceId
     * @param dataType
     * @param windowWidth
     * @param windowHeight
     * @param windowId
     */
    public void setParameters(String userName, String instanceId,
            VISIT_CONNECTION_TYPE connType, int windowWidth, int windowHeight,
            int windowId) {

        String dataType = "none";

        if (connType == VISIT_CONNECTION_TYPE.IMAGE) {
            dataType = "image";
        } else if (connType == VISIT_CONNECTION_TYPE.DATA) {
            dataType = "data";
        }
        client.setParameters(userName, instanceId, dataType, windowWidth,
                windowHeight, windowId);
    }

    /**
     * 
     * @param host
     * @param prt
     * @param pwd
     * @return
     */
    public boolean connect(String hostname, int port) {
        client.setInitializedCallback(this);

        client.setTunneling(mUseTunnel, session);

        if (!client.connect(hostname, port)) {
            return false;
        }

        return true;
    }

    public static boolean isThisMyIpAddress(InetAddress addr) {
        // Check if the address is a valid special local or loop back
        if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()) {
            return true;
        }
        // Check if the address is defined on any interface
        try {
            return NetworkInterface.getByInetAddress(addr) != null;
        } catch (SocketException e) {

            Logger.getGlobal().log(Level.INFO, e.getMessage(), e);
            return false;
        }
    }

    private void closePreviousConnection() {

        if (process != null) {
            process.destroy();
        }
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
        if (gateway != null) {
            gateway.disconnect();
        }
    }

    private List<String> constructCommand(String executable, String dir, int port,
            String password) {
        List<String> command = new ArrayList<String>();

        command.add(executable);
        command.add("-shared_port");
        command.add(Integer.toString(port));
        command.add("-shared_password");
        command.add(password);
        command.add("-cli");
        command.add("-nowin");
        command.add("-interactions");
        return command;
    }

    /**
     * 
     * @param host
     * @param port
     * @param password
     * @param dir
     * @return
     * @throws UnknownHostException
     * @throws JSchException
     * @throws IOException
     */
    private boolean launchLocal(String host, int port, String password,
            String dir) {

        String executable = dir + "/visit";

        if (!new File(executable).exists()) {
            /// try mac install..
            executable = dir + "/VisIt.app/Contents/Resources/bin/visit";
            if (!new File(executable).exists()) {
                executable = dir + "/visit.exe";
                /// try windows.
                if (!new File(executable).exists()) {               
                    // return false as VisIt executable does not exist
                    Logger.getGlobal().severe("VisIt executable was not found");
                    return false;
                }
            }
        }

        List<String> command = constructCommand(executable, dir, port, password);

        final ProcessBuilder builder = new ProcessBuilder(command);

        builder.directory(new File(dir));
        builder.redirectErrorStream(true);

        final Semaphore done = new Semaphore(0);

        Thread thread = new Thread(new VisItReaderThread(builder, done));

        thread.setDaemon(true);
        thread.start();

        /** ! wait until thread has started VisIt */
        try {
            done.acquire();
        } catch (InterruptedException e) {
            return false;
        }

        if (!hasVisItConnected) {
            return false;
        }

        // Now that VisIt has started connect to it..
        connect(host, port);

        return true;
    }

    private void createSession(String[] proxyInfo, int port)
            throws JSchException {
        // currently assume gateway username is same as remote
        // username..
        UserInfo ui = new VisItRemoteUserInfoDialog(shell);

        if (mGateway.length() > 0) {
            // connect default port 22..
            gateway = jsch.getSession(mGatewayUser.length() == 0 ? proxyInfo[0]
                    : mGatewayUser, mGateway);

            gateway.setUserInfo(ui);
            gateway.connect();

            // forward ssh to mGatewayPort..
            gateway.setPortForwardingL(mGatewayPort, proxyInfo[1], 22);
        }

        if (gateway == null) {
            // directly connect..
            session = jsch.getSession(proxyInfo[0], proxyInfo[1]);
            session.setUserInfo(ui);
            session.connect();

            if (mUseTunnel) {
                session.setPortForwardingL(port, proxyInfo[1], port);
            }
        } else {
            // connect to localhost
            session = jsch.getSession(mGatewayUser, LOCALHOST, mGatewayPort);
            session.setUserInfo(ui);
            session.connect();
            session.setPortForwardingL(port, LOCALHOST, port);
        }
    }

    private void initializeVisItConnection(BufferedReader input)
            throws IOException {
        String line = input.readLine();

        // Wait until it starts to listen on port...
        if (line == null) {
            throw new IOException("Initial read from VisIt process failed...");
        }

        while (!line.trim().startsWith("Starting to listen on port")) {
            line = input.readLine();

            if (line == null) {
                throw new IOException("Failed to read from VisIt process...");
            }

            if (line.trim().startsWith(
                    "WARNING: Failed to start listening server on port")) {
                throw new IOException("Failed to listen");
            }
        }
    }

    private boolean launchVisItOnRemote(String host, int port, String password,
            String dir) throws JSchException, IOException {

        List<String> command = constructCommand(dir + "/visit", dir, port,
                                                password);

        // start VisIt on remote machine..
        String[] proxyInfo = new String[2];
        String username = System.getProperty("user.name");

        proxyInfo[0] = username;
        proxyInfo[1] = host;

        if (host.contains("@")) {
            proxyInfo = host.split("@");
        }

        createSession(proxyInfo, port);

        channel = (ChannelExec) session.openChannel("exec");

        String commandString = "";
        for (int i = 0; i < command.size(); ++i) {
            commandString += command.get(i) + " ";
        }
        commandString = commandString.trim();

        Logger.getGlobal().info("Launching VisIt on " + commandString);
        
        channel.setCommand(commandString);
        channel.setInputStream(System.in, true);

        BufferedReader input = new BufferedReader(new InputStreamReader(
                channel.getExtInputStream()));

        channel.connect();

        initializeVisItConnection(input);

        mUseTunnel = true;

        connect(mUseTunnel ? LOCALHOST : proxyInfo[1], port);

        return true;
    }

    /**
     * 
     * @param host
     * @param port
     * @param password
     * @return
     * @throws JSchException
     */
    private boolean launchDirectToRemote(String host, int port, String password)
            throws JSchException {
        boolean result = false;

        mUseTunnel = true;

        // start VisIt on remote machine..
        String[] proxyInfo = new String[2];
        String username = System.getProperty("user.name");

        proxyInfo[0] = username;
        proxyInfo[1] = host;

        if (host.contains("@")) {
            proxyInfo = host.split("@");
        }

        createSession(proxyInfo, port);

        result = connect(mUseTunnel ? LOCALHOST : proxyInfo[1], port);
        return result;
    }

    private boolean launchDirectToRemote(String host, int port,
            String password, boolean directConnection) throws JSchException {

        return directConnection ? connect(host, port) : launchDirectToRemote(
                host, port, password);
    }

    /**
     * 
     * @param gateway
     * @param localPort
     */
    public void setGateway(String gateway, int localPort) {

        mGateway = gateway;

        if (mGateway.indexOf("@") > 0) {
            mGatewayUser = mGateway.split("@")[0];
            mGateway = mGateway.split("@")[1];
        }

        mGatewayPort = localPort;
    }

    public void useTunneling(boolean tunnel) {
        mUseTunnel = tunnel;
    }

    /**
     * 
     * @param host
     * @param port
     * @param password
     * @param dir
     * @return
     */
    public boolean launch(String host, int port, String password, String dir,
            boolean remote, boolean isDirect) {

        closePreviousConnection();

        String machine = host;

        if (machine.contains("@")) {
            machine = host.substring(host.indexOf("@") + 1);

            try {
                if (isThisMyIpAddress(InetAddress.getByName(machine))) {
                    machine = LOCALHOST;
                }
            } catch (UnknownHostException e) {
                // / suppress unknown host exception.
                Logger.getGlobal().log(Level.INFO, e.getMessage(), e);
            }
        }

        try {
            boolean result = false;

            if(isDirect) {
            	// / direct connection
            	Logger.getGlobal().info("Direct Connection");
            	result = launchDirectToRemote(host, port, password, LOCALHOST.equals(machine));
            }
            else if (LOCALHOST.equals(machine)) {
                // / local-host connection
                Logger.getGlobal().info("Launch Local");
                result = launchLocal(host, port, password, dir);
            } else if (dir.length() > 0) {
                // / launch VisIt on Remote
                Logger.getGlobal().info("Launch VisIt on Remote");
                result = launchVisItOnRemote(host, port, password, dir);
            }

            mHost = machine;
            mPort = port;
            mDir = dir;

            return result;
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
        }
        return false;
    }

    /**
     * 
     */
    public void close() {

        client.disconnect();
        closePreviousConnection();
    }

    public ViewerMethods getViewerMethods() {
        return client.getViewerMethods();
    }

    public ViewerState getViewerState() {
        return client.getViewerState();
    }

    @Override
    public boolean update(AttributeSubject subject) {

        String typename = subject.getTypename();

        if (!"ViewerClientInformation".equals(typename)) {
            return false;
        }

        JsonElement vars = subject.get("vars");

        if (!vars.isJsonArray()) {
            return false;
        }

        JsonArray imageList = vars.getAsJsonArray();

        for (int i = 0; i < imageList.size(); ++i) {

            JsonObject obj = imageList.get(i).getAsJsonObject();

            int windowId = subject.getAttr(obj, "windowId").getAsInt();

            if (!windowCallbacks.containsKey(windowId)) {
                continue;
            }

            List<VisItConnectionStruct> structs = windowCallbacks.get(windowId);

            String data = subject.getAttr(obj, "data").getAsString();

            String base64img = data;
            byte[] output = DatatypeConverter.parseBase64Binary(base64img);

            // / check to see if image format..
            for (int j = 0; j < structs.size(); ++j) {

                // / if image then convert to image data..
                structs.get(j).callback.update(VISIT_CONNECTION_TYPE.IMAGE,
                        output);
            }
        }

        return true;
    }

    class VisItReaderThread implements Runnable {

        ProcessBuilder builder;
        Semaphore done;

        VisItReaderThread(ProcessBuilder b, Semaphore d) {
            builder = b;
            done = d;
        }

        @SuppressWarnings("unused")
        public void run() {
            try {
                process = builder.start();

                BufferedReader input = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                String line = input.readLine();

                // Wait until it starts to listen on port...
                if (line == null) {
                    throw new IOException(
                            "Failed initial read from VisIt process...");
                }
                while (true) {
                    line = input.readLine();

                    Logger.getGlobal().info(">> " + line);

                    if (line.trim().startsWith("Starting to listen on port")) {
                        hasVisItConnected = true;
                        done.release();
                    }

                    if (line == null) {
                        throw new IOException(
                                "Failed to read from VisIt process...");
                    }
                    if (line.trim()
                            .startsWith(
                                    "WARNING: Failed to start listening server on port")) {
                        throw new IOException("Failed to listen");
                    }
                }

            } catch (Exception e) {
                Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
                hasVisItConnected = false;
                done.release();
            }
        }
    }
}
