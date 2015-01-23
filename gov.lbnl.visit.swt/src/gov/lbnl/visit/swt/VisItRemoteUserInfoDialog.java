package gov.lbnl.visit.swt;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

/**
 * @author hari
 * 
 */
public class VisItRemoteUserInfoDialog implements UserInfo,
        UIKeyboardInteractive {

    /** parent shell */
    Shell s;

    /** the pass-phrase typed in by the user */
    private String passphrase;

    /** the password typed in by the user. */
    private String passwd;

    public VisItRemoteUserInfoDialog(Shell shell) {
        s = shell;
    }

    public boolean promptYesNo(String str) {
    	final String message = str;
		// Open the dialog using the UI thread. Use syncExec to wait on the
		// operation to complete.
		final AtomicBoolean response = new AtomicBoolean();
		s.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				MessageBox messageBox = new MessageBox(s, SWT.ICON_WARNING
						| SWT.YES | SWT.NO);
				messageBox.setMessage(message);
				messageBox.setText("Warning");
				// Set the response to true if YES was clicked.
				response.set(messageBox.open() == SWT.YES);
			}
		});
        
        return response.get();
    }

    /*
     * returns the pass-phrase typed in by the user.
     */
    public String getPassphrase() {
        return passphrase;
    }

    /*
     * asks a key passphrase from the user.
     */
    public boolean promptPassphrase(String message) {
        this.passphrase = promptPassImpl(message);
        return passphrase != null;
    }

    /**
     * returns the password typed in by the user.
     */
    public String getPassword() {
        return passwd;
    }

    public static class PasswordDialog extends Dialog {
        private String password;
        int result = SWT.CANCEL;

        public PasswordDialog(Shell parent) {
            // Pass the default styles here
            this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
            password = "";
        }

        public PasswordDialog(Shell parent, int style) {
            // Let users override the default styles
            super(parent, style);
            setText("Enter Password for the Remote Machine");
            password = "";
        }

        public String getPassword() {
            return password;
        }

        public int open() {
            // Create the dialog window
            Shell shell = new Shell(getParent(), getStyle());
            shell.setText(getText());
            createContents(shell);
            shell.pack();
            shell.open();
            Display display = getParent().getDisplay();
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
            // Return the entered value, or null
            return result;
        }

        private void createContents(final Shell shell) {
            shell.setLayout(new GridLayout(2, false));

            // Show the message
            Label label = new Label(shell, SWT.NONE);
            label.setText("Password: ");
            label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
                    false));

            final Text passwordField = new Text(shell, SWT.PASSWORD
                    | SWT.BORDER);
            passwordField.setText("");
            passwordField.setLayoutData(new GridData(300, SWT.DEFAULT));

            Composite buttonComp = new Composite(shell, SWT.NONE);
            buttonComp.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true,
                    true, 2, 1));
            buttonComp.setLayout(new GridLayout(2, true));

            Button cancel = new Button(buttonComp, SWT.PUSH);
            cancel.setText("Cancel");
            cancel.setLayoutData(new GridData(100, SWT.DEFAULT));
            cancel.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    password = "";
                    passwordField.setText("");
                    result = SWT.CANCEL;
                    shell.close();
                }
            });

            Button okButton = new Button(buttonComp, SWT.PUSH);
            okButton.setText("OK");
            okButton.setLayoutData(new GridData(100, SWT.DEFAULT));
            okButton.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    password = passwordField.getText();
                    result = SWT.OK;
                    shell.close();
                }
            });

            shell.setDefaultButton(okButton);
        }
    }

    /*
     * asks a server password from the user.
     */
    public boolean promptPassword(String message) {
        this.passwd = promptPassImpl(message);
        return passwd != null;
    }

    /**
     * the common implementation of both {@link #promptPassword} and
     * {@link #promptPassphrase}.
     * 
     * @return the string typed in, if the user confirmed, else {@code null} .
     */
    private String promptPassImpl(String message) {
    	
		// Open the dialog using the UI thread. Use syncExec to wait on the
		// operation to complete.
		final AtomicReference<String> password = new AtomicReference<String>();
		s.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				PasswordDialog dialog = new PasswordDialog(s);
				if (dialog.open() == SWT.OK) {
					// Store the password if OK was clicked.
					password.set(dialog.getPassword());
				}
			}
		});
		// Get the password from the atomic.
        return password.get();
    }

    /*
     * shows a message to the user.
     */
    public void showMessage(String message) {
        MessageBox messageBox = new MessageBox(s, SWT.ICON_INFORMATION
                | SWT.YES);
        messageBox.setMessage(message);
        messageBox.setText(message);
    }

    /*
     * prompts the user a series of questions.
     */
    public String[] promptKeyboardInteractive(String destination, String name,
            String instruction, String[] prompt, boolean[] echo) {

        boolean result = promptPassword("Enter password");

        if (result) {
            return new String[] { getPassword() };
        }

        return new String[] {};
    }
}
