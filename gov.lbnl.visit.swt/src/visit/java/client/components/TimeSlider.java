package visit.java.client.components;

public class TimeSlider extends VisItComponent {

	public TimeSlider() {
	}

	public void play() {
		if (methods == null) return; 
		methods.animationPlay();
	}
	
	public void previousState() {
		if (methods == null) return; 
		methods.animationPreviousState();
	}
	
	public void reversePlay() {
        if (methods == null) return;
        methods.animationReversePlay();
	}
	
	public void stop() {
		if(methods == null) return;
        methods.animationStop();
	}
	
	public void nextState() {
		if (methods == null) return; 
		methods.animationNextState();
	}
}
