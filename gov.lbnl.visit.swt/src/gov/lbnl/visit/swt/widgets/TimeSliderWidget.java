package gov.lbnl.visit.swt.widgets;

import gov.lbnl.visit.swt.VisItSwtConnection;
import visit.java.client.components.TimeSlider;

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
    private TimeSlider ts;

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
        
        ts = new TimeSlider();
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
        Class<?> resourceClass = TimeSliderWidget.class;
        
        URL imageURL;

        // ---- Create the step back action. ---- //
        action = new Action(null, Action.AS_PUSH_BUTTON) {
            @Override
            public void run() {
                ts.previousState();
            }
        };
        // Set an ImageDescriptor.
        imageURL = resourceClass.getResource("/icons/previous.gif");
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
            	ts.reversePlay();
            }
        };
        // Set an ImageDescriptor.
        imageURL = resourceClass.getResource("/icons/play_rev.gif");
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
            	ts.stop();
            }
        };
        
        // Set an ImageDescriptor.
        imageURL = resourceClass.getResource("/icons/pause.gif");
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
            	ts.play();
            }
        };
        // Set an ImageDescriptor.
        imageURL = resourceClass.getResource("/icons/play.gif");
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
                ts.nextState();
            }
        };
        
        // Set an ImageDescriptor.
        imageURL = resourceClass.getResource("/icons/next.gif");
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

        this.redraw();
        this.pack();
    }
}
