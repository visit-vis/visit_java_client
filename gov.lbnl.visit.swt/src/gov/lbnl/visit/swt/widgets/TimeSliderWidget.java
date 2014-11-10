package gov.lbnl.visit.swt.widgets;

import gov.lbnl.visit.swt.VisItSwtConnection;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class TimeSliderWidget extends Composite {

	private VisItSwtConnection connection;

	/**
	 * The set of time slider actions presented by the UI. This includes play,
	 * stop, rewind, etc.
	 */
	private final List<IAction> actions;

	/**
	 *
	 * @param parent
	 *            A widget which will be the parent of the new instance (cannot
	 *            be null).
	 * @param style
	 *            The style of widget to construct.
	 */
	public TimeSliderWidget(Composite parent, int style) {
		this(parent, style, null);
	}

	/**
	 * 
	 * @param parent
	 *            A widget which will be the parent of the new instance (cannot
	 *            be null).
	 * @param style
	 *            The style of widget to construct.
	 * @param conn
	 */
	public TimeSliderWidget(Composite parent, int style, VisItSwtConnection conn) {
		super(parent, style);
		
		connection = conn;
		
		actions = new ArrayList<IAction>();
		setupUI();
	}

	public void setVisItSwtConnection(VisItSwtConnection conn) {
		connection = conn;

		// Enable/disable all of the actions.
		boolean actionsEnabled = (connection != null);
		for (IAction action : actions) {
			action.setEnabled(actionsEnabled);
		}
	}

	/**
	 * Creates all of the actions/buttons available in the UI.
	 */
	private void setupUI() {

		// Default to a RowLayout. All of the widgets added to this Composite
		// should automatically wrap with this configured.
		setLayout(new RowLayout(SWT.HORIZONTAL));

		// We use JFace Actions to encapsulate the time slider operations. They
		// are currently configured as push buttons, but this can be changed.
		// Furthermore, we can add them as regular Buttons to Composites,
		// ToolBars, and Menus by using ActionContributionItems. We can also
		// pass ImageDescriptors to the Action constructor
		IAction action;

		// Create the step back action.
		action = new Action("Step Back", Action.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				if (connection != null) {
					connection.getViewerMethods().animationPreviousState();
				}
			}
		};
		// Set an ImageDescriptor.
		//action.setImageDescriptor(null);
		actions.add(action);

		// Create the rewind (reverse play) action.
		action = new Action("Rewind", Action.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				if (connection != null) {
					connection.getViewerMethods().animationReversePlay();
				}
			}
		};
		actions.add(action);

		// Create the stop action.
		action = new Action("Stop", Action.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				if (connection != null) {
					connection.getViewerMethods().animationStop();
				}
			}
		};
		actions.add(action);

		// Create the play method.
		action = new Action("Play", Action.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				if (connection != null) {
					connection.getViewerMethods().animationPlay();
				}
			}
		};
		actions.add(action);

		// Create the step forward action.
		action = new Action("Step Forward", Action.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				if (connection != null) {
					connection.getViewerMethods().animationNextState();
				}
			}
		};
		actions.add(action);

		// Add all of the Actions to the Composite. This requires the use of
		// ActionContributionItems. We could also add them to a ToolBar or Menu
		// using ActionContributionItem's different fill methods.
		boolean actionsEnabled = (connection != null);
		for (IAction iAction : actions) {
			iAction.setEnabled(actionsEnabled);
			new ActionContributionItem(iAction).fill(this);
		}

		// Create the timestep widget.
		Text timeSteps = new Text(this, SWT.NONE);
		timeSteps.setText("Timesteps...");

		this.redraw();
		this.pack();
	}
}
