package gov.lbnl.visit.swt;

import org.eclipse.jface.wizard.Wizard;

/**
 * This class extends Wizard to manage the WizardDialog for establishing a VisIt
 * client connection.
 * 
 * @author tnp
 * 
 */
public class VisItSwtLaunchWizard extends Wizard {

	/**
	 * The WizardPage contained in this Wizard
	 */
	VisItSwtLaunchWizardPage page;

	/**
	 * The constructor
	 */
	public VisItSwtLaunchWizard() {

		// Call Wizard's constructor
		super();

		// Add the content by adding an instance of LaunchVisitWizardPage
		page = new VisItSwtLaunchWizardPage("VisIt Launch Page");
		addPage(page);

		return;
	}

	/**
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {

		// Call the LaunchWizardPage method that sets that classes fields
		page.setFinishFields();

		return true;
	}

	/**
	 * This operation retrieves the WizardPage contained in this Wizard.
	 * 
	 * @return The WizardPage of this Wizard
	 */
	public VisItSwtLaunchWizardPage getPage() {
		return page;
	}

}
