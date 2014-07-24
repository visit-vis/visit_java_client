/**
 * 
 */
package gov.lbnl.visit.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
public class VisItRemoteUserInfoDialog implements UserInfo, UIKeyboardInteractive
{
	Shell s;
	
	public VisItRemoteUserInfoDialog(Shell shell) {
		s = shell;
	}
	
	public boolean promptYesNo(String str) 
	{
		MessageBox messageBox = new MessageBox(s, SWT.ICON_WARNING
				| SWT.YES | SWT.NO);
		messageBox.setMessage(str);
		messageBox.setText("Warning");
		
		int response = messageBox.open();
		System.out.println(SWT.YES + " " + response);
		return response == SWT.YES;
	}

	/** the pass-phrase typed in by the user */
	private String passphrase;

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

	/** the password typed in by the user. */
	private String passwd;

	/**
	 * returns the password typed in by the user.
	 */
	public String getPassword() {
		return passwd;
	}

	public static class PasswordDialog extends Dialog {
		  public String password;
		  int result = SWT.CANCEL;
		  
		  public PasswordDialog(Shell parent) {
		    // Pass the default styles here
		    this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		    password = "";
		  }

		  public PasswordDialog(Shell parent, int style) {
		    // Let users override the default styles
		    super(parent, style);
		    setText("Enter Password...");
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
		    shell.setLayout(new GridLayout(3, false));

		    // Show the message
		    Label label = new Label(shell, SWT.BOLD | SWT.NONE);
		    label.setText("Password: ");
		    
		    GridData data = new GridData(SWT.FILL, SWT.DEFAULT, true, false);
		    data.horizontalSpan = 3;
		    label.setLayoutData(data);
		    
		    final Text passwordField = new Text(shell, SWT.PASSWORD | SWT.NONE);
		    passwordField.setText("");    
		    passwordField.setLayoutData(data);
		    
			Button okButton = new Button(shell, SWT.BORDER);
			okButton.setText("OK");
		    data = new GridData(GridData.FILL_HORIZONTAL);
		    okButton.setLayoutData(data);
		    okButton.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent event) {
		    	  password = passwordField.getText(); 
		    	  result = SWT.OK;
		    	  shell.close();
		      }
		    });
		    
		    Button cancel = new Button(shell, SWT.PUSH);
		    cancel.setText("Cancel");
		    data = new GridData(GridData.FILL_HORIZONTAL);
		    cancel.setLayoutData(data);
		    cancel.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent event) {
		        password = "";
		        passwordField.setText("");
		        result = SWT.CANCEL;
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
	 * @return the string typed in, if the user confirmed, else {@code null}
	 *         .
	 */
	private String promptPassImpl(String message) {
		
		PasswordDialog dialog = new PasswordDialog(s);
		
		int result = dialog.open();
		
		if(result == SWT.OK) {
			return dialog.getPassword();
		}
		return null;
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
	public String[] promptKeyboardInteractive(String destination,
			String name, String instruction, String[] prompt, boolean[] echo) {
				return null;
	}
}
