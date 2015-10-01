package visit.java.client.components;

import visit.java.client.AttributeSubject;
import visit.java.client.ViewerMethods;
import visit.java.client.ViewerState;

public class SaveWindow extends VisItComponent {

	public enum Format {
		BMP, CURVE, JPEG, OBJ, PNG, POSTSCRIPT, POVRAY, PPM, RGB, STL, TIFF, ULTRA, VTK, PLY
	};
	
	public SaveWindow(ViewerMethods m) {
		super(m);
	}
	
	public void save(String filename, int width, int height, Format format, String outputDirectory) {
		/*
		 * outputToCurrentDirectory = 1
outputDirectory = "."
fileName = "visit"
family = 1
format = PNG  # BMP, CURVE, JPEG, OBJ, PNG, POSTSCRIPT, POVRAY, PPM, RGB, STL, TIFF, ULTRA, VTK, PLY
width = 1024
height = 1024
screenCapture = 0
saveTiled = 0
quality = 80
progressive = 0
binary = 0
stereo = 0
compression = PackBits  # None, PackBits, Jpeg, Deflate
forceMerge = 0
resConstraint = ScreenProportions  # NoConstraint, EqualWidthHeight, ScreenProportions
advancedMultiWindowSave = 0
		 */
		
		ViewerState state = methods.getViewerState();
		int subj = state.getIndexFromTypename("SaveWindowAttributes");
		
		state.set(subj, "format", format.ordinal());
		state.set(subj, "width", width);
		state.set(subj, "height", width);
		state.set(subj, "fileName", filename);
		state.set(subj, "outputDirectory", outputDirectory);
		state.notify(subj);
		methods.synchronize();
		
		methods.saveWindow();
		
	}

}
