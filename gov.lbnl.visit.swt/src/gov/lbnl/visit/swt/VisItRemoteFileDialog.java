package gov.lbnl.visit.swt;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
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
    List<String> files;
    List<String> dirs;
    ViewerMethods methods;
    String remotePath = null;

    public VisItRemoteFileDialog(ViewerMethods vmethods, Display disp) {
        methods = vmethods;
        display = disp;

        files = new ArrayList<String>();
        dirs = new ArrayList<String>();

        methods.getViewerState().registerCallback("QueryAttributes", this);
    }

    public synchronized boolean update(AttributeSubject arg0) {
        String defaultName = arg0.get("defaultName").getAsString();
        JsonArray defaultVars = arg0.get("defaultVars").getAsJsonArray();

        if (!"FileList".equals(defaultName)) {
            return false;
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
                Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
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
        
        return true;
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
        final Shell shell = new Shell(display, SWT.TITLE | SWT.CLOSE
                | SWT.BORDER | SWT.PRIMARY_MODAL | SWT.RESIZE);

        remotePath = null;

        shell.setText("Remote File List");
        shell.setLayout(new FillLayout());
        
        final Tree tree = new Tree(shell, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        
        // / localhost in this case can also be remote machine..
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
        }

        tree.addListener(SWT.Expand, new Listener() {
            public void handleEvent(final Event event) {
                final TreeItem root = (TreeItem) event.item;
                expandPath(tree, root);
            }
        });

        tree.addSelectionListener(new TreeSelection(tree, shell));

        shell.setSize(400, 400);
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        return remotePath;
    }
    
    class TreeSelection implements SelectionListener {
        Tree tree;
        Shell shell;
        TreeSelection(Tree ptree, Shell s) {
            tree = ptree;
            shell = s;
        }
        
        @Override
        public void widgetSelected(SelectionEvent e) {
            /// do nothing on typical widget selection
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            TreeItem[] items = tree.getSelection();

            if (items.length != 1) {
                return;
            }
            
            if (items[0].getItemCount() > 0) {
                expandPath(tree, items[0]);
                return;
            }

            remotePath = items[0].getText();
            shell.dispose();
        }
    }
}
