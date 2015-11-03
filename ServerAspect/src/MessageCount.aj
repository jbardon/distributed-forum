
import org.alma.distributedforum.server.Subject;

public aspect MessageCount {
	
	private int counter = 0;
	
	pointcut countMessages():
        execution(void Subject.putMessage(..));
	
	after(): countMessages() {
		this.counter++;
		System.out.println("[Aspect MessageCount] counter: " + counter);
	}
}
