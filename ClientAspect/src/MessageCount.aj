
import org.alma.distributedforum.client.CustomerView;

public aspect MessageCount {
	
	private int counter = 0;
	
	pointcut countMessages():
        execution(void CustomerView.show(..));
	
	after(): countMessages() {
		this.counter++;
		System.out.println("[Aspect MessageCount] counter: " + counter);
	}
}
