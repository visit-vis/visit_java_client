/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package visit.java.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 
 * @authors hkq, tnp
 */
public class VisItClient {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) throws InterruptedException,
			IOException {
		System.out.println("Starting Java Client");

		VisItProxy client = new VisItProxy();

		client.setParameters("user1", 
							 "fluffy", 
							 "none",
							 600, 
							 600, 
							 1);
		
		if (!client.connect("localhost", 9002)) {
			System.out.println("Could not connect to VisIt, Quitting");
			System.exit(0);
		}

		String CurLine = ""; // Line read from standard in

		System.out.println("Enter a line of text (type 'quit' to exit): ");
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);

		ViewerMethods methods = client.getViewerMethods();

		while (!(CurLine.equals("quit()"))) {
			System.out.print(">> ");
			CurLine = in.readLine().trim();

			try {
				if ((CurLine.equals("quit()")))
					continue;

				if ("InvertBackgroundColor()".equals(CurLine))
					methods.invertBackgroundColor();

				if ("AddWindow()".equals(CurLine))
					methods.addWindow();

				if ("DrawPlots()".equals(CurLine))
					methods.drawPlots();

				if (CurLine.startsWith("OpenDatabase")) {
					String ss = CurLine.substring(CurLine.indexOf("\"") + 1,
							CurLine.lastIndexOf("\""));
					methods.openDatabase(ss);
				}
				if (CurLine.startsWith("AddPlot")) {
					String is = CurLine.substring(CurLine.indexOf("(") + 1,
							CurLine.indexOf(")"));
					is = is.replace("\"", "");

					String[] results = is.split(",");
					if (results.length == 2)
						methods.addPlot(results[0], results[1]);
				}

				if (CurLine.startsWith("AddOperator")) {
					String is = CurLine.substring(CurLine.indexOf("(") + 1,
							CurLine.indexOf(")"));
					is = is.replace("\"", "");
					methods.addOperator(is);
				}

				if (CurLine.startsWith("DeleteActivePlots()"))
					methods.deleteActivePlots();

				if (CurLine.startsWith("HideActivePlots()"))
					methods.hideActivePlots();

				if (CurLine.startsWith("SetActivePlots")) {
					String is = CurLine.substring(CurLine.indexOf("(") + 1,
							CurLine.indexOf(")"));
					is = is.replace("\"", "");
					methods.setActivePlots(Integer.parseInt(is));
				}
			} catch (Exception e) {
				System.out.println("Error evaluating: " + CurLine);
			}
		}

		System.exit(0);
	}
}
