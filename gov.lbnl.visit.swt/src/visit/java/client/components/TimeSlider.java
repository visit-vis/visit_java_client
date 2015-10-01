package visit.java.client.components;

import visit.java.client.ViewerMethods;

public class TimeSlider extends VisItComponent {

	public TimeSlider(ViewerMethods m) {
		super(m);
	}

	public void play() {
		methods.animationPlay();
	}
	
	public void previousState() {
		methods.animationPreviousState();
	}
	
	public void reversePlay() {
        methods.animationReversePlay();
	}
	
	public void stop() {
		methods.animationStop();
	}
	
	public void nextState() {
		methods.animationNextState();
	}
}
