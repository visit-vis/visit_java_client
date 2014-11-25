package gov.lbnl.visit.swt.widgets;

import gov.lbnl.visit.swt.VisItSwtConnection;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

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
        boolean actionsEnabled = connection != null;
        for (IAction action : actions) {
            action.setEnabled(actionsEnabled);
        }
    }

    /**
     * Creates all of the widgets available in the UI.
     */
    private void setupUI() {

        // Default to a RowLayout. All of the widgets added to this Composite
        // should automatically wrap with this configured.
        RowLayout layout = new RowLayout(SWT.HORIZONTAL);
        layout.fill = true;
        setLayout(layout);

        // We use JFace Actions to encapsulate the time slider operations. They
        // are currently configured as push buttons, but this can be changed.
        // Furthermore, we can add them as regular Buttons to Composites,
        // ToolBars, and Menus by using ActionContributionItems. We can also
        // pass ImageDescriptors to the Action constructor
        IAction action;

        // Get the resources we will need for setting the ImageDescriptors
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        Path imagePath;
        String imagePathPrefix = "icons" + System.getProperty("file.separator");
        URL imageURL;

        // ---- Create the step back action. ---- //
        action = new Action(null, Action.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                if (connection != null) {
                    connection.getViewerMethods().animationPreviousState();
                }
            }
        };
        // Set an ImageDescriptor.
        imagePath = new Path(imagePathPrefix + "previous.gif");
        imageURL = FileLocator.find(bundle, imagePath, null);
        action.setImageDescriptor(ImageDescriptor.createFromURL(imageURL));
        // Set the tool tip text
        action.setToolTipText("Previous time step");
        // Add the action to the list
        actions.add(action);
        // -------------------------------------- //

        // ----- Create the rewind action. ------ //
        action = new Action(null, Action.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                if (connection != null) {
                    connection.getViewerMethods().animationReversePlay();
                }
            }
        };
        // Set an ImageDescriptor.
        imagePath = new Path(imagePathPrefix + "play_rev.gif");
        imageURL = FileLocator.find(bundle, imagePath, null);
        action.setImageDescriptor(ImageDescriptor.createFromURL(imageURL));
        // Set the tool tip text
        action.setToolTipText("Rewind");
        // Add the action to the list
        actions.add(action);
        // -------------------------------------- //

        // ------ Create the stop action. ------- //
        action = new Action(null, Action.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                if (connection != null) {
                    connection.getViewerMethods().animationStop();
                }
            }
        };
        // Set an ImageDescriptor.
        imagePath = new Path(imagePathPrefix + "pause.gif");
        imageURL = FileLocator.find(bundle, imagePath, null);
        action.setImageDescriptor(ImageDescriptor.createFromURL(imageURL));
        // Set the tool tip text
        action.setToolTipText("Pause");
        // Add the action to the list
        actions.add(action);
        // -------------------------------------- //

        // ------ Create the play method. ------- //
        action = new Action(null, Action.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                if (connection != null) {
                    connection.getViewerMethods().animationPlay();
                }
            }
        };
        // Set an ImageDescriptor.
        imagePath = new Path(imagePathPrefix + "play.gif");
        imageURL = FileLocator.find(bundle, imagePath, null);
        action.setImageDescriptor(ImageDescriptor.createFromURL(imageURL));
        // Set the tool tip text
        action.setToolTipText("Play");
        // Add the action to the list
        actions.add(action);
        // -------------------------------------- //

        // -- Create the step forward action. --- //
        action = new Action(null, Action.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                if (connection != null) {
                    connection.getViewerMethods().animationNextState();
                }
            }
        };
        // Set an ImageDescriptor.
        imagePath = new Path(imagePathPrefix + "next.gif");
        imageURL = FileLocator.find(bundle, imagePath, null);
        action.setImageDescriptor(ImageDescriptor.createFromURL(imageURL));
        // Set the tool tip text
        action.setToolTipText("Next time step");
        // Add the action to the list
        actions.add(action);
        // -------------------------------------- //

        // Determine if these buttons should be enabled at this point.
        boolean actionsEnabled = connection != null;

        // Add all of the Actions to the Composite. This requires the use of
        // ActionContributionItems. We could also add them to a ToolBar or Menu
        // using ActionContributionItem's different fill methods.
        for (IAction iAction : actions) {
            iAction.setEnabled(actionsEnabled);
            new ActionContributionItem(iAction).fill(this);
        }

        // TODO Implement the backend functionality for this.
        // // Create the text field to set the time. //
        // Composite timeComp = new Composite(this, SWT.NONE);
        // timeComp.setLayout(new GridLayout(2, false));
        // Label timeLabel = new Label(timeComp, SWT.NONE);
        // timeLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
        //        true));
        // timeLabel.setText("Time:");
        // final Text timeText = new Text(timeComp, SWT.SINGLE | SWT.BORDER);
        // timeText.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
        // timeText.addSelectionListener(new SelectionListener() {
        //
        //     @Override
        //     public void widgetSelected(SelectionEvent e) {
        //         // Not called for Text anyway.
        //     }
        //
        //     @Override
        //     public void widgetDefaultSelected(SelectionEvent e) {
        //         // For SWT.SINGLE style Text, this is called when enter is
        //         // pressed when in the Text.
        //         String timeStr = timeText.getText();
        //         // Call the appropriate method to set the frame of the rendered
        //         // image to the entered time.
        //         // TODO Parse the String to the appropriate data type. This
        //         // might be String but could be an int, float, double, or
        //         // something else. Also make sure the user has entered a valid
        //         // value.
        //     }
        // });
        // timeText.setEnabled(actionsEnabled);
        // // -------------------------------------- //

        this.redraw();
        this.pack();
    }
}
