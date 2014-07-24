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
import java.util.concurrent.Semaphore;

import javax.xml.bind.DatatypeConverter;

import org.eclipse.swt.widgets.Shell;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
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

	public enum VISIT_CONNECTION_TYPE {
		CONTROL, IMAGE, DATA
	};

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

	private String m_gateway_user = "";
	private String m_gateway = "";
	private int m_gateway_port = 0;
	private boolean m_useTunnel = false;

	private HashMap<Integer, ArrayList<VisItConnectionStruct>> windowCallbacks;

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

	/** ! window callback */
	public void registerVisualization(VISIT_CONNECTION_TYPE type, int windowId,
			VisualizationUpdateCallback callback) {

		System.out.println("add connection");
		if (!windowCallbacks.containsKey(windowId)) {
			windowCallbacks.put(windowId,
					new ArrayList<VisItConnectionStruct>());
		}

		VisItConnectionStruct struct = new VisItConnectionStruct();
		struct.callback = callback;
		struct.setConnType(type);
		
		//TODO: update visualization request with VisIt.
		
		windowCallbacks.get(windowId).add(struct);
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

		if (connType == VISIT_CONNECTION_TYPE.IMAGE)
			dataType = "image";
		else if (connType == VISIT_CONNECTION_TYPE.DATA)
			dataType = "data";

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

		client.setTunneling(m_useTunnel, session);

		if (!client.connect(hostname, port)) {
			System.err.println("Could not connect to VisIt, Quitting");
			return false;
		}

		return true;
	}
	
	public static boolean isThisMyIpAddress(InetAddress addr) {
	    // Check if the address is a valid special local or loop back
	    if (addr.isAnyLocalAddress() || addr.isLoopbackAddress())
	        return true;

	    // Check if the address is defined on any interface
	    try {
	        return NetworkInterface.getByInetAddress(addr) != null;
	    } catch (SocketException e) {
	        return false;
	    }
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
	public boolean launchLocal(String host, int port, String password,
			String dir) throws UnknownHostException {

		String commandString = "";
		ArrayList<String> command = new ArrayList<String>();

		// Form the command based on the OS
		//String platform = SWT.getPlatform();
		//if (platform.equals("cocoa")) {
		//	command.add(dir + "/VisIt.app/Contents/MacOS/VisIt");
		//} else {
			command.add(dir + "/visit");
		//}

		command.add("-shared_port");
		command.add(Integer.toString(port));
		command.add("-shared_password");
		command.add(password);
		command.add("-cli");
		command.add("-nowin");
		//command.add("-s");
		//command.add("/home/users/hari/bob.py");
		//command.add("-clscript");
		//command.add("\"while True: import time; time.sleep(0.1)\"");
		
		for (int i = 0; i < command.size(); ++i) {
			commandString += command.get(i) + " ";
		}

		commandString = commandString.trim();
		System.out.println("Launching " + commandString);

		if (isThisMyIpAddress(InetAddress.getByName(host))) {
			final ProcessBuilder builder = new ProcessBuilder(command);
			//builder.environment().put("PYTHONUNBUFFERED", "1");
			//builder.redirectInput(ProcessBuilder.Redirect.PIPE);

			builder.directory(new File(dir));
			builder.redirectErrorStream(true);

			final Semaphore done = new Semaphore(0);

			Thread thread = new Thread(new Runnable() {

				@SuppressWarnings("unused")
				public void run() {
					try {
						process = builder.start();

						BufferedReader input = new BufferedReader(
								new InputStreamReader(process.getInputStream()));
						String line = input.readLine();

						// Wait until it starts to listen on port...
						if (line == null)
							throw new IOException("Failed to read from VisIt process...");
			
						while(true) {
							line = input.readLine();
							System.out.println(">>> " + line);
							if(line.trim().startsWith("Starting to listen on port") == true) {
								hasVisItConnected = true;
								done.release();
								//break;
							}
							
							if (line == null) {
								throw new IOException(
										"Failed to read from VisIt process...");
							}
							if(line.trim().startsWith("WARNING: Failed to start listening server on port"))
								throw new IOException("Failed to listen");
						}
												
					} catch (Exception e) {
						hasVisItConnected = false;
						done.release();
					}
				}
			});

			thread.setDaemon(true);
			thread.start();

			/**! wait until thread has started VisIt */
			try {
				done.acquire();
			} catch (InterruptedException e) {
				//e.printStackTrace();
				System.out.println("Interruption occurred. Failed to start VisIt");
				return false;
			}
			
			if(!hasVisItConnected) {
				return false;
			}
			
			// Now that VisIt has started connect to it..
			connect(host, port);

		} else {
			if (channel != null)
				channel.disconnect();
			if (session != null)
				session.disconnect();
			if (gateway != null)
				gateway.disconnect();

			try {

				// start VisIt on remote machine..
				String[] proxyInfo = new String[2];
				String username = System.getProperty("user.name");

				proxyInfo[0] = username;
				proxyInfo[1] = host;

				if (host.contains("@")) {
					proxyInfo = host.split("@");
				}

				// currently assume gateway username is same as remote
				// username..
				UserInfo ui = new VisItRemoteUserInfoDialog(shell);

				if (m_gateway.length() > 0) {
					// connect default port 22..
					gateway = jsch.getSession(
							m_gateway_user.length() == 0 ? proxyInfo[0]
									: m_gateway_user, m_gateway);

					gateway.setUserInfo(ui);
					gateway.connect();

					// forward ssh to m_gateway_port..
					gateway.setPortForwardingL(m_gateway_port, proxyInfo[1], 22);
				}

				if (gateway == null) {
					// directly connect..
					session = jsch.getSession(proxyInfo[0], proxyInfo[1]);
					session.setConfig("StrictHostKeyChecking", "no");
					session.setUserInfo(ui);
					session.connect();

					if (m_useTunnel) {
						session.setPortForwardingL(port, proxyInfo[1], port);
					}
				} else {
					// connect to localhost
					session = jsch.getSession(proxyInfo[0], "localhost",
							m_gateway_port);
					session.setUserInfo(ui);
					session.connect();
					session.setPortForwardingL(port, "localhost", port);
				}

				channel = (ChannelExec) session.openChannel("exec");
				channel.setCommand(commandString);

				// InputStream is = channel.getInputStream();
				channel.setOutputStream(System.out, true);
				channel.setExtOutputStream(System.err, true);
				channel.setInputStream(System.in, true);

				BufferedReader input = new BufferedReader(
						new InputStreamReader(channel.getExtInputStream()));

				channel.connect();

				String line = input.readLine();

				// Wait until it starts to listen on port...
				if (line == null)
					throw new IOException(
							"Failed to read from VisIt process...");

				while (line.trim().startsWith("Starting to listen on port") == false) {
					line = input.readLine();
					System.out.println(">>> " + line);

					if (line == null)
						throw new IOException(
								"Failed to read from VisIt process...");

					if (line.trim()
							.startsWith(
									"WARNING: Failed to start listening server on port"))
						throw new IOException("Failed to listen");
				}
				// channel.disconnect();

				m_useTunnel = true;

				System.out.println("VisIt started");
				connect(m_useTunnel ? "localhost" : proxyInfo[1], port);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				if (channel != null)
					channel.disconnect();
				if (session != null)
					session.disconnect();
				if (gateway != null)
					gateway.disconnect();
				return false;
			}
		}

		return true;
	}

	public boolean launchRemote(String host, int port, String password) {

		// Now that VisIt has started connect to it..
		try {
			return connect(host, port);
		} catch (Exception e) {
			System.err.println("Failed to start VisIt");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @param gateway
	 * @param localPort
	 */
	public void setGateway(String gateway, int localPort) {
		// TODO: Use Eclipse's SocketUtil library to find a free local port..

		m_gateway = gateway;

		if (m_gateway.indexOf("@") > 0) {
			m_gateway_user = m_gateway.split("@")[0];
			m_gateway = m_gateway.split("@")[1];
		}

		m_gateway_port = localPort;
	}

	public void useTunneling(boolean tunnel) {
		m_useTunnel = tunnel;
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
			boolean remote) {

		try {
			if (!remote) {
				return launchLocal(host, port, password, dir);
			} else {
				return launchRemote(host, port, password);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 
	 */
	public void close() {
		System.out.println("Closing..");
		// TODO: gracefully shutdown..
		client.disconnect();
		System.out.println("Client shutdown..");
		if (process != null) {
			System.out.println("Closing process..");
			process.destroy();
		}
		if (channel != null)
			channel.disconnect();
		if (session != null)
			session.disconnect();
		if (gateway != null)
			gateway.disconnect();
		System.out.println("Disconnecting all");
		//client.disconnect();
	}

	public ViewerMethods getViewerMethods() {
		return client.getViewerMethods();
	}

	public ViewerState getViewerState() {
		return client.getViewerState();
	}

	/**
	 * 
	 * @param arg0
	 */
	// private void updateClientInformation(AttributeSubject arg0, int windowId)
	// {
	//
	// // an image or data object was requested..
	// try {
	//
	// JsonArray imageList = vars.getAsJsonArray();
	//
	// for (int i = 0; i < imageList.size(); ++i) {
	// JsonObject obj = imageList.get(i).getAsJsonObject();
	// // String format = arg0.getAttr(obj, "format").getAsString();
	// String data = arg0.getAttr(obj, "data").getAsString();
	//
	// final String base64img = data;
	// byte[] output = DatatypeConverter
	// .parseBase64Binary(base64img);
	// }
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// }
	// }

	@Override
	public void update(AttributeSubject subject) {

		String typename = subject.getTypename();

		if (!typename.equals("ViewerClientInformation")) {
			return;
		}

		JsonElement vars = subject.get("vars");

		if (!vars.isJsonArray()) {
			System.out.println("not a json object" + vars);
			return;
		}

		JsonArray imageList = vars.getAsJsonArray();

		// System.out.println(windowCallbacks.keySet().toString());

		for (int i = 0; i < imageList.size(); ++i) {

			JsonObject obj = imageList.get(i).getAsJsonObject();

			int windowId = subject.getAttr(obj, "windowId").getAsInt();
			// System.out.println("windowId: " + windowId);

			if (!windowCallbacks.containsKey(windowId)) {
				continue;
			}
			
			//System.out.println("windowId m: " + windowId);
			ArrayList<VisItConnectionStruct> structs = windowCallbacks.get(windowId);

			//System.out.println(obj.entrySet().toString());
			//String format = subject.getAttr(obj, "format").getAsString();
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

	}
}
