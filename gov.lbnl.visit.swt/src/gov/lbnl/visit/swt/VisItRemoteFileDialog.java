package gov.lbnl.visit.swt;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import visit.java.client.AttributeSubject;
import visit.java.client.AttributeSubject.AttributeSubjectCallback;
import visit.java.client.ViewerMethods;

public class VisItRemoteFileDialog implements AttributeSubjectCallback {

	Display display;
	ArrayList<String> files;
	ArrayList<String> dirs;
	ViewerMethods methods;
	String remotePath = null;

	public VisItRemoteFileDialog(ViewerMethods _methods, Display _display) {
		methods = _methods;
		display = _display;

		files = new ArrayList<String>();
		dirs = new ArrayList<String>();

		methods.getViewerState().registerCallback("QueryAttributes", this);
	}

	synchronized public void update(AttributeSubject arg0) {
		String defaultName = arg0.get("defaultName").getAsString();
		JsonArray defaultVars = arg0.get("defaultVars").getAsJsonArray();

		if (!defaultName.equals("FileList")) {
			return;
		}
		dirs.clear();
		files.clear();

		Gson gson = new Gson();
		for (int i = 0; i < defaultVars.size(); ++i) {
			String filelist = defaultVars.get(i).getAsString();

			JsonObject obj;
			filelist = filelist.replace("&quot;", "\"");

			try {
				obj = gson.fromJson(filelist, JsonObject.class);
			} catch (JsonSyntaxException e) {
				System.out.println("failed on " + filelist);
				continue;
			}

			JsonArray d = obj.get("dirs").getAsJsonArray();
			JsonArray f = obj.get("files").getAsJsonArray();

			for (int j = 0; j < d.size(); ++j) {
				dirs.add(d.get(j).getAsString().replace("\"", ""));
			}

			for (int j = 0; j < f.size(); ++j) {
				files.add(f.get(j).getAsString().replace("\"", ""));
			}
		}
	}

	public void expandPath(Tree tree, TreeItem root) {
		TreeItem[] items = root.getItems();

		for (int i = 0; i < items.length; i++) {
			if (items[i].getData() != null) {
				return;
			}
			items[i].dispose();
		}

		String file = (String) root.getData();
		methods.getFileList("localhost", file);

		tree.removeAll();

		for (int i = 0; i < dirs.size(); i++) {
			TreeItem item;
			item = new TreeItem(tree, 0);
			item.setText(dirs.get(i));
			item.setData(dirs.get(i));
			new TreeItem(item, 0);
		}

		for (int i = 0; i < files.size(); i++) {
			TreeItem item;
			item = new TreeItem(tree, 0);
			item.setText(files.get(i));
			item.setData(files.get(i));
		}

		tree.redraw();
	}

	public String open() {
		final Shell shell = new Shell(display, SWT.CLOSE);

		remotePath = null;

		shell.setText("Remote System Files and Directories");
		shell.setLayout(new FillLayout());

		Composite comp = new Composite(shell, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));

		// TODO Waiting for a method to get the path or a remote file system
		// browser that doesn't require a VisIt connection
		// Composite locationComp = new Composite(comp, SWT.NONE);
		// locationComp
		// .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		// locationComp.setLayout(new GridLayout(2, false));
		// Label locationLabel = new Label(locationComp, SWT.NONE);
		// locationLabel.setText("Location:");
		// final Text locationText = new Text(locationComp, SWT.BORDER);
		// locationText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
		// true));

		final Tree tree = new Tree(comp, SWT.BORDER);
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// localhost in this case can also be remote machine..
		methods.getFileList("localhost", ".");

		for (int i = 0; i < dirs.size(); i++) {
			TreeItem root = new TreeItem(tree, 0);
			root.setText(dirs.get(i));
			root.setData(dirs.get(i));
			new TreeItem(root, 0);
		}

		for (int i = 0; i < files.size(); i++) {
			TreeItem root = new TreeItem(tree, 0);
			root.setText(files.get(i));
			root.setData(files.get(i));
			// new TreeItem (root, 0);
		}

		tree.addListener(SWT.Expand, new Listener() {
			public void handleEvent(final Event event) {
				final TreeItem root = (TreeItem) event.item;
				expandPath(tree, root);
			}
		});

		tree.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				TreeItem[] items = tree.getSelection();

				if (items.length != 1)
					return;

				if (items[0].getItemCount() > 0) {
					expandPath(tree, items[0]);
					return;
				}

				remotePath = items[0].getText();
				System.out.println("path = " + remotePath);
				shell.dispose();
			}
		});

		shell.setSize(shell.computeSize(500, 700));
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}

		}

		return remotePath;
	}
}
