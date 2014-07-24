package gov.lbnl.visit.swt;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * This class extends WizardPage to create the Wizard content to allow the user
 * to establish a VisIt client connection. This connection may be via a local or
 * remote launch or by connecting to a running VisIt session.
 * 
 * @author tnp
 * 
 */
public class VisItSwtLaunchWizardPage extends WizardPage {

	/**
	 * The user's selection to exit the dialog ("Local" to launch, "Remote" to
	 * connect to service, null to cancel)
	 */
	private String dialogExitSelection;

	/**
	 * The directory where the VisIt executable is located
	 */
	private String visItDir;

	/**
	 * The name of the host running VisIt or where VisIt should be launched
	 */
	private String hostname;

	/**
	 * The port VisIt will serve connections on
	 */
	private String port;

	/**
	 * The password for connection to VisIt
	 */
	private String password;

	/**
	 * The gateway used for out-of-network connections
	 */
	private String gateway;

	/**
	 * The local port used to connect to the gateway
	 */
	private String localGatewayPort;

	/**
	 * Flag for whether or not tunneling is used
	 */
	private boolean use_tunneling;

	/**
	 * The radio buttons for selecting the connection type
	 */
	private Button localRadio;
	private Button remoteRadio;
	private Button serviceRadio;

	/**
	 * The composites for defining the path to the VisIt executable
	 */
	private PathComposite localPathComp;
	private PathComposite remotePathComp;

	/**
	 * The mechanisms for defining the port for VisIt to serve to
	 */
	private PortComposite localPortComp;
	private PortComposite remotePortComp;
	private Text servicePortText;

	/**
	 * The mechanisms for defining the VisIt connection password
	 */
	private PasswordComposite localPasswordComp;
	private PasswordComposite remotePasswordComp;
	private Text servicePassText;

	/**
	 * The composites where the hostname is recorded
	 */
	private HostComposite remoteHostComp;
	private HostComposite serviceHostComp;

	/**
	 * The composites for defining out-of-network gateway parameters
	 */
	private GatewayComposite remoteGatewayComp;
	private GatewayComposite serviceGatewayComp;

	/**
	 * The constructor
	 * 
	 * @param pageName
	 *            The String ID of this WizardPage
	 */
	protected VisItSwtLaunchWizardPage(String pageName) {

		// Call WizardPage's constructor
		super(pageName);

		setTitle("Establish a connection to VisIt");
		setDescription("Select a connection method and fill in the required "
				+ "parameters.");

		// Set the default values
		dialogExitSelection = null;
		visItDir = "";
		hostname = "";
		port = "";
		password = "";
		gateway = "";
		localGatewayPort = "";
		use_tunneling = false;

		// Disable the "Finish" button
		setPageComplete(false);

		return;
	}

	/**
	 * This operation is called whenever the user clicks the help button on this
	 * page in the wizard. Display additional help text for the user.
	 */
	@Override
	public void performHelp() {
		MessageDialog.open(INFORMATION, getShell(), "Help",
				"Use this dialog to establish a connection to VisIt. "
						+ "You may launch VisIt on this machine or a "
						+ "remote machine or connect to a VisIt session "
						+ "that was previously launched.", SWT.NONE);
		return;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));

		// Fill in the widgets of the dialog
		createContents(composite);

		setControl(composite);

		localRadio.setSelection(true);
		localRadio.notifyListeners(SWT.Selection, new Event());		
		return;
	}

	/**
	 * Create and layout the widgets of the dialog.
	 * 
	 * @param parent
	 *            The parent Shell to contain the dialog
	 */
	private void createContents(final Composite parent) {

		/*------- Layout all of the widgets -------*/
		/*--- Local Launch ---*/
		// Set up the radio to launch locally
		localRadio = new Button(parent, SWT.RADIO);
		localRadio.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));
		localRadio.setText("Launch Visit locally");

		// Create the local launch widget composite
		final Composite localComp = new Composite(parent, SWT.BORDER);
		localComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		localComp.setLayout(new GridLayout(1, false));

		// Create the path composite
		localPathComp = new PathComposite(localComp);

		// Create the port composite
		localPortComp = new PortComposite(localComp);

		// Create the password composite
		localPasswordComp = new PasswordComposite(localComp);

		// Disable this section by default
		enableSection(localComp, false);
		/*--------------------*/

		/*--- Remote Launch --*/
		// Set up the radio to launch remotely
		remoteRadio = new Button(parent, SWT.RADIO);
		remoteRadio
				.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		remoteRadio.setText("Launch Visit remotely");

		// Create the remote launch widget group
		final Composite remoteComp = new Composite(parent, SWT.BORDER);
		remoteComp
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		remoteComp.setLayout(new GridLayout(1, false));

		// Create the host composite
		remoteHostComp = new HostComposite(remoteComp);

		// Create the path composite
		remotePathComp = new PathComposite(remoteComp);

		// Create the port composite
		remotePortComp = new PortComposite(remoteComp);

		// Create the password composite
		remotePasswordComp = new PasswordComposite(remoteComp);

		// Create the gateway composite
		remoteGatewayComp = new GatewayComposite(remoteComp);

		// Disable this section by default
		enableSection(remoteComp, false);
		/*--------------------*/

		/*-- Service Connect -*/
		// Set up the radio to connect to a service
		serviceRadio = new Button(parent, SWT.RADIO);
		serviceRadio.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
				false, 2, 1));
		serviceRadio.setText("Connect to VisIt");

		// Create the connect to service widget group
		final Composite serviceComp = new Composite(parent, SWT.BORDER);
		serviceComp
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		serviceComp.setLayout(new GridLayout(1, false));

		// Create the host composite
		serviceHostComp = new HostComposite(serviceComp);

		// Create the port composite
		Composite servicePortComp = new Composite(serviceComp, SWT.NONE);
		servicePortComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				true));
		servicePortComp.setLayout(new GridLayout(2, false));
		// Add a Label to identify the adjacent Text
		Label servicePortLabel = new Label(servicePortComp, SWT.NONE);
		servicePortLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false));
		servicePortLabel.setText("Port for connecting to VisIt:");
		// Add a Text for the user to input the port number
		servicePortText = new Text(servicePortComp, SWT.BORDER);
		servicePortText.setText(port);
		servicePortText.setLayoutData(new GridData(50, SWT.DEFAULT));

		// Create the password composite
		Composite servicePassComp = new Composite(serviceComp, SWT.NONE);
		servicePassComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				true));
		servicePassComp.setLayout(new GridLayout(2, false));
		// Add a label to identify the adjacent Text
		Label servicePassLabel = new Label(servicePassComp, SWT.NONE);
		servicePassLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false));
		servicePassLabel.setText("Password to connect to VisIt service:");
		// Add a Text for the user to input the password
		servicePassText = new Text(servicePassComp, SWT.PASSWORD | SWT.BORDER);
		servicePassText.setText(password);
		servicePassText.setLayoutData(new GridData(100, SWT.DEFAULT));

		// Create the gateway composite
		serviceGatewayComp = new GatewayComposite(serviceComp);

		// Disable this section by default
		enableSection(serviceComp, false);
		/*--------------------*/
		/*-----------------------------------------*/

		/*------ Setup the button listeners -------*/
		// Create the listener for the local launch radio button. The listener
		// will enable the "Finish" button and the widgets in the local launch
		// composite. If the remote launch or connect to service composites have
		// been enabled, they will be disabled.
		localRadio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				// Change the help text at the top of the dialog
				setTitle("Launch VisIt on this machine.");
				setDescription("Enter the file system path to the VisIt "
						+ "executable. Optionally, set a port number and "
						+ "password to allow other users to connect to this "
						+ "session.");

				// Enable the widgets in the local launch composite and the
				// "Finish" button
				enableSection(localComp, true);
				setPageComplete(true);

				// Disable the widgets in the remote launch and connect to
				// service composites
				enableSection(remoteComp, false);
				enableSection(serviceComp, false);
			}
		});

		// Create the listener for the remote launch radio button. The listener
		// will enable the "Finish" button and the widgets in the remote launch
		// composite. If the local launch or connect to service composites have
		// been enabled, they will be disabled.
		remoteRadio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				// Change the help text at the top of the dialog
				setTitle("Launch VisIt on a remote machine.");
				setDescription("Enter the host to run the VisIt session and the "
						+ "file system path to the VisIt executable. Optionally,"
						+ " set a port, password, or use an out-of-network "
						+ "gateway.");

				// Enable the widgets in the local launch composite and the
				// "Finish" button
				enableSection(remoteComp, true);
				setPageComplete(true);

				// Disable the widgets in the remote launch and connect to
				// service composites
				enableSection(localComp, false);
				enableSection(serviceComp, false);
			}
		});

		// Create the listener for the connect to service radio button. The
		// listener will enable the "Finish" button and the widgets in the
		// connect to service composite. If the local or remote launch
		// composites have been enabled, they will be disabled.
		serviceRadio.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				// Change the help text at the top of the dialog
				setTitle("Connect to a running VisIt client.");
				setDescription("Enter the host running VisIt and the port "
						+ "and password used for validating connections. "
						+ "Optionally, use and out-of-network gateway to "
						+ "access the host.");

				// Enable the widgets in the local launch composite and the
				// "Finish" button
				enableSection(serviceComp, true);
				setPageComplete(true);

				// Disable the widgets in the remote launch and connect to
				// service composites
				enableSection(localComp, false);
				enableSection(remoteComp, false);
			}
		});

		return;
	}

	/**
	 * This function retrieves the password used to connect to a VisIt client.
	 * 
	 * @return The string used as the VisIt client password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * This function retrieves the gateway URL.
	 * 
	 * @return The string representation of the gateway URL.
	 */
	public String getGateway() {
		return gateway;
	}

	/**
	 * This function retrieves the gateway port.
	 * 
	 * @return The string representation of the local port used for the gateway
	 *         connection.
	 */
	public String getGatewayPort() {
		return localGatewayPort;
	}

	/**
	 * This function retrieves the whether or not tunneling is used.
	 * 
	 * @return The string representation of the boolean value for whether or not
	 *         tunneling is used.
	 */
	public String getUseTunneling() {
		return new Boolean(use_tunneling).toString();
	}

	/**
	 * This function retrieves the hostname where the VisIt client will or has
	 * been launched.
	 * 
	 * @return The string representation of the hostname.
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * This function retrieves the port number that the VisIt client is
	 * broadcasting on.
	 * 
	 * @return The string representation of the port number.
	 */
	public String getVisItPort() {
		return port;
	}

	/**
	 * This function retrieves the director where the VisIt executable resides.
	 * 
	 * @return The string representation of the VisIt executable directory.
	 */
	public String getVisItDir() {
		return visItDir;
	}

	/**
	 * This function returns true if the user chose to connect to a running
	 * VisIt service and false otherwise.
	 * 
	 * @return The string representation of whether or not to connect to a
	 *         running VisIt service.
	 */
	public String getIsRemote() {
		if (dialogExitSelection == "Remote")
			return "true";
		else
			return "false";
	}

	/**
	 * This operation is used to enable or disable a given Control. If the
	 * Control is a Composite, recursively call this method on the children of
	 * the Composite. If the Control is an ICheckComposite, keep the Text
	 * adjacent to the check button disabled if the check is not selected.
	 * 
	 * @param control
	 *            The Control to be enabled or disabled
	 * @param enable
	 *            The value to set control to (true to enable or false to
	 *            disable)
	 */
	private void enableSection(Control control, boolean enable) {

		// If control is a Composite, call this function on all of the
		// Composite's children.
		if (control instanceof Composite) {
			Composite comp = (Composite) control;
			for (Control c : comp.getChildren()) {
				enableSection(c, enable);
			}
		}
		// If this is a Text that should be disabled based on check box, keep it
		// that way.
		else if (control instanceof Text
				&& control.getParent() instanceof ICheckComposite
				&& !((ICheckComposite) control.getParent()).isSelected()) {
			control.setEnabled(false);
		}
		// Otherwise, enable or disable this widget based on the input value.
		else {
			control.setEnabled(enable);
		}

		return;
	}

	/**
	 * This operation gets the radio selection and sets the fields of this class
	 * appropriately.
	 */
	public void setFinishFields() {

		// Set the fields for a local launch if the local launch radio
		// is selected.
		if (localRadio.getSelection()) {
			dialogExitSelection = "Local";
			// Get the path or prompt an error if the user has left the field
			// empty.
			if (!localPathComp.getPathString().isEmpty()) {
				visItDir = localPathComp.getPathString();
			} else {
				// Display an error prompt to enter a path
				MessageDialog.openError(getShell(), "Invalid Path",
						"Please enter the path to a Visit executable.");
				return;
			}
			hostname = "localhost";
			// Get the port number or prompt an error if the user has
			// left the field empty.
			if (!localPortComp.getPortString().isEmpty()) {
				port = localPortComp.getPortString();
			} else {
				// Display an error prompt to enter a port number
				MessageDialog.openError(getShell(), "Invalid Port",
						"Please enter the ID of an open port for the "
								+ "Visit connection.");
				return;
			}
			password = localPasswordComp.getPassword();
			use_tunneling = false;
		}
		// Set the fields for a remote launch if this is the user's
		// selection.
		else if (remoteRadio.getSelection()) {
			dialogExitSelection = "Local";
			// Get the hostname or prompt an error if the user has left the
			// field empty.
			if (!remoteHostComp.getHostString().isEmpty()) {
				hostname = remoteHostComp.getHostString();
			} else {
				// Display an error prompt to enter a path
				MessageDialog.openError(getShell(), "Invalid Hostname",
						"Please enter a valid hostname.");
				return;
			}
			// Get the path or prompt an error if the user has left the field
			// empty.
			if (!remotePathComp.getPathString().isEmpty()) {
				visItDir = remotePathComp.getPathString();
			} else {
				// Display an error prompt to enter a path
				MessageDialog.openError(getShell(), "Invalid Path",
						"Please enter the path to a Visit executable.");
				return;
			}
			// Get the port number or prompt an error if the user has
			// left the field empty.
			if (!remotePortComp.getPortString().isEmpty()) {
				port = remotePortComp.getPortString();
			} else {
				// Display an error prompt to enter a port
				MessageDialog.openError(getShell(), "Invalid Port",
						"Please enter the ID of an open port for the "
								+ "Visit connection.");
				return;
			}
			password = remotePasswordComp.getPassword();
			use_tunneling = true;
			gateway = remoteGatewayComp.getURLString();
			localGatewayPort = remoteGatewayComp.getPortString();
		}
		// Set the fields for connecting to a running VisIt service if
		// this is the user's selection.
		else if (serviceRadio.getSelection()) {
			dialogExitSelection = "Remote";
			// Get the hostname or prompt an error if the user has left the
			// field empty.
			if (!remoteHostComp.getHostString().isEmpty()) {
				hostname = serviceHostComp.getHostString();
			} else {
				// Display an error prompt to enter a path
				MessageDialog.openError(getShell(), "Invalid Hostname",
						"Please enter a valid hostname.");
				return;
			}
			// Get the port number or prompt an error if the user has
			// left the field empty.
			if (!servicePortText.getText().isEmpty()) {
				port = servicePortText.getText();
			} else {
				// Display an error prompt to enter a port
				MessageDialog.openError(getShell(), "Invalid Port",
						"Please enter the ID of an open port for the "
								+ "Visit connection.");
				return;
			}
			password = servicePassText.getText();
			use_tunneling = false;
			gateway = serviceGatewayComp.getURLString();
			localGatewayPort = serviceGatewayComp.getPortString();
		}
		// The "Finish" button should not be enabled without a radio
		// selection, but if this scenario occurs, just return exit as
		// if "Cancel" was selected.
		else {
			dialogExitSelection = null;
		}

		return;
	}

	/**
	 * This class extends Composite to create a collection of widgets for
	 * selecting the path to VisIt.
	 * 
	 * @author tnp
	 */
	private class PathComposite extends Composite {

		/**
		 * The Text widget to enter/display the path
		 */
		private Text pathText;

		/**
		 * The constructor
		 * 
		 * @param parent
		 *            The Composite containing an instance of this Composite.
		 */
		public PathComposite(final Composite parent) {

			// Call Composite's constructor
			super(parent, SWT.NONE);

			// Set the layout data for placement in the parent Composite.
			setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));

			// Set the layout of this Composite.
			setLayout(new GridLayout(3, false));

			// Create a Label to identify the Text purpose
			Label pathLabel = new Label(this, SWT.NONE);
			pathLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
					false));
			pathLabel.setText("Path to VisIt:");

			// Create the Text to enter and display the path to VisIt
			pathText = new Text(this, SWT.BORDER);
			pathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
					true));

			// Create the 'Browse' button to access a DirectoryDialog to define
			// the path to VisIt
			Button pathButton = new Button(this, SWT.PUSH);
			pathButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
					false));
			pathButton.setText("Browse");
			pathButton.setToolTipText("Select the VisIt installation "
					+ "directory");

			// Add the listener to the Button
			pathButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					// Open the file system exploration dialog
					DirectoryDialog dialog = new DirectoryDialog(parent
							.getShell(), SWT.OPEN);
					String result = dialog.open();
					
					if(result != null)
						pathText.setText(result);
				}
			});

			return;
		}

		/**
		 * This function retrieves the contents of the Text widget
		 * 
		 * @return The String contents of the Text widget for defining the path
		 *         to VisIt
		 */
		public String getPathString() {
			return pathText.getText();
		}

	}

	/**
	 * This interface is implemented Composites that contain check buttons with
	 * associated Text widgets.
	 * 
	 * @author tnp
	 */
	private interface ICheckComposite {

		/**
		 * This method returns the boolean value of whether or not the
		 * implementing Composite has a check button.
		 * 
		 * @return True if the implementing class contains a check button
		 */
		public boolean hasCheckBox();

		/**
		 * This operation retrieves the selection value of the check button.
		 * 
		 * @return True in the check button is selected; false, otherwise.
		 */
		public boolean isSelected();
	}

	/**
	 * This class extends Composite to create a collection of widgets for
	 * setting the port for VisIt to communicate on.
	 * 
	 * @author tnp
	 */
	private class PortComposite extends Composite implements ICheckComposite {

		/**
		 * The Text for the port number string
		 */
		private Text portText;

		/**
		 * The check button to allow/disallow port input
		 */
		private Button portButton;

		/**
		 * The constructor
		 * 
		 * @param parent
		 *            The parent Composite containing this Composite
		 */
		public PortComposite(Composite parent) {

			// Call Composite's constructor
			super(parent, SWT.NONE);

			// Set the layout data for the parent composite
			setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));

			// Set the layout for this composite
			setLayout(new GridLayout(2, false));

			// Create the check button to use or disallow setting the port
			portButton = new Button(this, SWT.CHECK);
			portButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
					false));
			portButton.setText("Set a port for connecting to VisIt:");

			// Create the Text for inputting the port number
			portText = new Text(this, SWT.BORDER);
			portText.setText("9600");
			portText.setLayoutData(new GridData(50, SWT.DEFAULT));
			portText.setEnabled(false);

			// Add a listener for the check button to enable/disable the
			// adjacent Text based on the button selection
			portButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					if (portButton.getSelection()) {
						portText.setEnabled(true);
					} else {
						portText.setText("9600");
						portText.setEnabled(false);
					}
				}
			});

			return;
		}

		/**
		 * This operation retrieves the value in the port Text
		 * 
		 * @return The String in the port Text widget
		 */
		public String getPortString() {
			return portText.getText();
		}

		/**
		 * @see ICheckComposite#hasCheckBox()
		 */
		@Override
		public boolean hasCheckBox() {
			return true;
		}

		/**
		 * @see ICheckComposite#isSelected()
		 */
		@Override
		public boolean isSelected() {
			return portButton.getSelection();
		}

	}

	/**
	 * This class extends Composite to create a collection of widgets for
	 * setting the password for incoming VisIt to connections.
	 * 
	 * @author tnp
	 */
	private class PasswordComposite extends Composite implements
			ICheckComposite {

		/**
		 * The Text to input a password String
		 */
		private Text passText;

		/**
		 * The check button to allow/disallow defining a password
		 */
		private Button passButton;

		/**
		 * The constructor
		 * 
		 * @param parent
		 *            The parent Composite containing this Composite
		 */
		public PasswordComposite(Composite parent) {

			// Call Composite's constructor
			super(parent, SWT.NONE);

			// Set the layout data for the parent
			setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));

			// Set the layout of this Composite
			setLayout(new GridLayout(2, false));

			// Create the check button
			passButton = new Button(this, SWT.CHECK);
			passButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
					false));
			passButton.setText("Set a password for connecting to VisIt:");

			// Create the Text widget
			passText = new Text(this, SWT.PASSWORD | SWT.BORDER);
			passText.setLayoutData(new GridData(100, SWT.DEFAULT));
			passText.setEnabled(false);

			// Add the listener for the button to allow/disallow the use of a
			// password
			passButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					if (passButton.getSelection()) {
						passText.setEnabled(true);
					} else {
						passText.setEnabled(false);
					}
				}
			});

			return;
		}

		/**
		 * This operation retrieves the contents of the password Text widget or
		 * a default password if the widget is empty.
		 * 
		 * @return The String password
		 */
		public String getPassword() {
			if (!passText.getText().isEmpty()) {
				return passText.getText();
			} else {
				return "notused";
			}
		}

		/**
		 * @see ICheckComposite#hasCheckBox()
		 */
		@Override
		public boolean hasCheckBox() {
			return true;
		}

		/**
		 * @see ICheckComposite#isSelected()
		 */
		@Override
		public boolean isSelected() {
			return passButton.getSelection();
		}

	}

	/**
	 * This class extends Composite to create a collection of widgets for
	 * setting the host to launch VisIt on or where VisIt is running.
	 * 
	 * @author tnp
	 */
	private class HostComposite extends Composite {

		/**
		 * The Text for setting the hostname
		 */
		private Text hostText;

		/**
		 * The constructor
		 * 
		 * @param parent
		 *            The Composite that contains this Composite
		 */
		public HostComposite(Composite parent) {

			// Call Composite's constructor
			super(parent, SWT.NONE);

			// Set the layout data for the parent
			setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));

			// Set the layout of this Composite
			setLayout(new GridLayout(2, false));

			// Add a Label to identify the adjacent Text
			Label hostLabel = new Label(this, SWT.NONE);
			hostLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
					false));
			hostLabel.setText("Hostname:");

			// Add the host Text
			hostText = new Text(this, SWT.BORDER);
			hostText.setLayoutData(new GridData(100, SWT.DEFAULT));

			return;
		}

		/**
		 * This operation retrieves the contents of the host Text widget
		 * 
		 * @return The String in the host Text
		 */
		public String getHostString() {
			return hostText.getText();
		}

	}

	/**
	 * This class extends Composite to create a collection of widgets for
	 * setting the gateway URL and port.
	 * 
	 * @author tnp
	 */
	private class GatewayComposite extends Composite implements ICheckComposite {

		/**
		 * The Text for the gateway URL
		 */
		private Text urlText;

		/**
		 * The Text for the gateway port
		 */
		private Text gatePortText;

		/**
		 * The check button to allow/disallow using a gateway for connecting
		 */
		private Button gateButton;

		/**
		 * The constructor
		 * 
		 * @param parent
		 *            The parent Composite containing this Composite
		 */
		public GatewayComposite(Composite parent) {

			// Call Composite's constructor
			super(parent, SWT.NONE);

			// Set the layout data for the parent
			setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));

			// Set this Composite's layout
			setLayout(new GridLayout(3, false));

			// Create the check button to allow/disallow using a gateway
			gateButton = new Button(this, SWT.CHECK);
			gateButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
					false));
			gateButton.setText("Use an out-of-network gateway:");

			// Create the Text for gateway URL input
			urlText = new Text(this, SWT.BORDER);
			urlText.setLayoutData(new GridData(100, SWT.DEFAULT));
			urlText.setText("URL");
			urlText.setEnabled(false);

			// Create the Text for gateway port input
			gatePortText = new Text(this, SWT.BORDER);
			gatePortText.setLayoutData(new GridData(50, SWT.DEFAULT));
			gatePortText.setText("Port");
			gatePortText.setEnabled(false);

			// Add a listener to enable/disable the Text widgets based on the
			// check selection
			gateButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					if (gateButton.getSelection()) {
						urlText.setEnabled(true);
						gatePortText.setEnabled(true);
					} else {
						urlText.setEnabled(false);
						urlText.setText("URL");
						gatePortText.setEnabled(false);
						gatePortText.setText("Port");
					}
				}
			});

			return;
		}

		/**
		 * This operation retrieves the gateway URL
		 * 
		 * @return The String for the gateway URL
		 */
		public String getURLString() {
			if (gateButton.getSelection()) {
				return urlText.getText();
			} else {
				return "";
			}
		}

		/**
		 * This operation retrieves the gateway port
		 * 
		 * @return The String for the gateway port
		 */
		public String getPortString() {
			if (gateButton.getSelection()) {
				return gatePortText.getText();
			} else {
				return "";
			}
		}

		/**
		 * @see ICheckComposite#hasCheckBox()
		 */
		@Override
		public boolean hasCheckBox() {
			return true;
		}

		/**
		 * @see ICheckComposite#isSelected()
		 */
		@Override
		public boolean isSelected() {
			return gateButton.getSelection();
		}

	}

}
