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
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 * 
 * @authors hkq, tnp
 */
public class VisItProxy {

	/**
	 * 
	 */
	public interface VisItInitializedCallback {
		public void initialized();
	}

	/**
	 * 
	 */
	public static final int BUFSIZE = 4096;

	/**
	 * 
	 */
	private String visit_host, visit_port;

	/**
	 * 
	 */
	private String visit_security_key;

	/**
	 * 
	 */
	private HashMap<String, Integer> visitRPC;

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
	private VisItThread thread_runnable;

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
	 * The constructor
	 */
	public VisItProxy() {
	}

	/**
	 * 
	 */
	public void setParameters(String userName, String instanceId,
			String dataType, int windowWidth, int windowHeight, int windowId) {
		header.name = userName;
		header.password = instanceId;
		header.canRender = dataType;
		header.geometry = windowWidth + "x" + windowHeight;
		header.windowIds = new ArrayList<Integer>();
		header.windowIds.add(windowId);

	}

	boolean m_useTunnel = false;
	Session m_tunnelSession = null;

	public void setTunneling(boolean tunnel, Session ts) {
		m_useTunnel = tunnel;
		m_tunnelSession = ts;
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

			Socket socket = new Socket(m_useTunnel ? "localhost" : host, port);
			OutputStreamWriter writer = new OutputStreamWriter(
					socket.getOutputStream());
			InputStreamReader reader = new InputStreamReader(
					socket.getInputStream());

			String headerstr = gson.toJson(header);

			// System.out.println(headerstr);
			writer.write(headerstr);
			writer.flush();

			// System.out.println("Reading...");
			String message = "";

			int len = 0;
			do {
				char[] cbuf = new char[1024];
				len = reader.read(cbuf);

				if (len <= 0)
					break;

				String msg = new String(cbuf, 0, len);
				message += msg;

			} while (len > 0);

			// System.out.println("raw message: " + message);
			JsonElement e = gson.fromJson(message, JsonElement.class);
			JsonObject jo = e.getAsJsonObject();

			visit_host = jo.get("host").getAsString();
			visit_port = jo.get("port").getAsString();
			visit_security_key = jo.get("securityKey").getAsString();
			JsonArray visit_rpc = jo.get("rpc_array").getAsJsonArray();

			visitRPC = new HashMap<String, Integer>();
			for (int i = 0; i < visit_rpc.size(); ++i) {
				visitRPC.put(visit_rpc.get(i).getAsString(), i);
				System.out.println(visit_rpc.get(i).getAsString() + "," + i);
			}

			if (m_useTunnel) {
				m_tunnelSession.setPortForwardingL(
						Integer.parseInt(visit_port), "localhost",
						Integer.parseInt(visit_port));
			}

			// System.out.println(message);

			// String[] results = message.split(",");

			// System.out.println(visit_host + " " + visit_port + " " +
			// visit_security_key);
			socket.close();

			return true;
		} catch (ConnectException e) {
			e.printStackTrace();
			System.out.println("Exception: Could not connect to a "
					+ "running VisIt client");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception");
		}
		return false;
	}

	Semaphore sem = new Semaphore(0);

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

			inputSocket = new Socket(m_useTunnel ? "localhost" : visit_host,
					Integer.valueOf(visit_port));
			inputConnection = new InputStreamReader(
					inputSocket.getInputStream());

			outputSocket = new Socket(m_useTunnel ? "localhost" : visit_host,
					Integer.valueOf(visit_port));
			outputConnection = new OutputStreamWriter(
					outputSocket.getOutputStream());

			// Handle initial connection
			char[] cbuf = new char[1024];

			// read 100 bytes
			InputStreamReader isr = (new InputStreamReader(
					outputSocket.getInputStream()));
			isr.read(cbuf);

			// extract socket key
			// visit_socket_key = new String(cbuf,5+1+10+21, 21);

			cbuf[0] = ASCIIFORMAT;

			for (int i = 0; i < visit_security_key.length(); ++i)
				cbuf[6 + 10 + i] = visit_security_key.charAt(i);

			OutputStreamWriter osw = new OutputStreamWriter(
					inputSocket.getOutputStream());
			osw.write(cbuf);
			osw.flush();

			// System.out.println("wrote: " + new String(cbuf));
			// End - Handle initial connection
			state = new ViewerState();

			state.setConnection(outputConnection);

			thread_runnable = new VisItThread(inputConnection);
			thread = new Thread(thread_runnable);
			thread.setDaemon(true);
			thread.start();

			/** ! block until all data is in */
			sem.acquire();

			if (callback != null) {
				callback.initialized();
			}

			methods = new ViewerMethods(state, visitRPC);

			System.out.println("Viewer State synched..");

		} catch (Exception e) {
			System.out.println("Exception connecting..");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void disconnect() {
		getViewerMethods().close();
		thread_runnable.quitThread();
		thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread arg0, Throwable arg1) {
				System.out.println("uncaught??");
			}
		});

		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.out.println("Thread killed");
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
		public String name;
		public String geometry;
		public String password;
		public String canRender;
		java.util.ArrayList<Integer> windowIds;
	}

	/**
	 *
	 */
	class VisItThread implements Runnable {
		private InputStreamReader inputConnection;
		private Gson gson;
		private boolean qThread;
		private final int MAX_STATES = 135;

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
				e.printStackTrace();
			}
			System.out.println("Quitting Thread .");
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

		/**
		 * 
		 */
		@Override
		public void run() {
			// stitches together one map_node entry
			StringBuilder partial_entry = new StringBuilder("");
			// holds input data buffer
			StringBuilder input_buffer = new StringBuilder("");
			char[] data = new char[VisItProxy.BUFSIZE];

			while (true) {

				try {
					int len = 0;
					try {
						len = inputConnection.read(data);
					} catch (Exception e) {
						System.out.println("Exception: Quitting ...");
						break;
					}

					if (len == 0) {
						System.out.println("Quitting ...");
						break;
					}

					if (qThread) {
						System.out.println("Quitting Thread...");
						break;
					}

					input_buffer.append(data, 0, len);

					// for now JSON parser has to start with object..
					int mnsi = input_buffer.indexOf("{");
					int mnei = input_buffer.indexOf("}");

					while (mnsi >= 0 || mnei >= 0) {
						if (mnsi < 0 && mnei >= 0) {
							mnei += "}".length();
							partial_entry.append(input_buffer.subSequence(0,
									mnei));
							input_buffer.delete(0, mnei);
						} else if (mnsi >= 0 && mnei < 0) {
							mnsi += "{".length();
							partial_entry.append(input_buffer.subSequence(0,
									mnsi));
							input_buffer.delete(0, mnsi);
						} else {
							if (mnsi < mnei) {
								mnsi += "{".length();
								partial_entry.append(input_buffer.subSequence(
										0, mnsi));
								input_buffer.delete(0, mnsi);
							} else {
								mnei += "}".length();
								partial_entry.append(input_buffer.subSequence(
										0, mnei));
								input_buffer.delete(0, mnei);
							}
						}

						String tmp = partial_entry.toString().trim();

						if (count(tmp, "{") > 0
								&& count(tmp, "{") == count(tmp, "}")) {
							try {
								partial_entry.setLength(0);
								// tmp=tmp.replace("\n","");
								tmp = tmp.replace("\\\"", "");
								JsonElement el = gson.fromJson(tmp,
										JsonElement.class);
								JsonObject jo = el.getAsJsonObject();

								// update state..
								VisItProxy.this.state.update(jo);
								if (VisItProxy.this.state.states.size() == MAX_STATES) {
									System.out
											.println("Maximum States Reached");
									sem.release();
								}

							} catch (Exception e) {
								System.out.println("failed input " + tmp);
							}
						}

						// tmp = input_buffer.strip()

						// print partial_entry
						mnsi = input_buffer.indexOf("{"); // don't include
															// current
															// node
						mnei = input_buffer.indexOf("}");
					}
				} catch (Exception e) {
					System.err.println("incoming thread terminating...");
					break;
				}
			}
		}
	}

}
