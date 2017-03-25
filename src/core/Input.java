package core;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public abstract class Input extends Thread {
	private LinkedList<Shifter> Shifters = new LinkedList<Shifter>();
	
	public void subscribe(Shifter shifter) {
		Shifters.add(shifter);
	}
	
	public void unsubscribe(Shifter shifter) {
		Shifters.remove(shifter);
	}
	
	protected abstract String nextLine();
	
	private Semaphore endSemaphore;
	
	public void setup() {
		endSemaphore = new Semaphore(-Shifters.size() + 1, true);
		for (Shifter shifter : Shifters) {
			shifter.setup(endSemaphore);
		}
		
		this.start();
	}
	
	public void run() {
		String line;
        do
        {
            line = nextLine();

            for (Shifter shifter : Shifters)
                shifter.accept(line);
        } while (!line.equals(""));
	}
	
	public void end() throws InterruptedException {
		for (Shifter shifter : Shifters)
			shifter.end();
		
		endSemaphore.acquire();
		endSemaphore.release();
	}
}
