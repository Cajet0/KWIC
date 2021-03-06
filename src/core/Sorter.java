package core;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import core.sorter.SortStrategy;

/**
 * The third filter of the KWIC System. It is in charge of sorting all
 * the rotations and sending the sorted list to all its Writers.
 * <p>
 * End of input must be marked with an empty String.
 * <p>
 * It activates all its subscribed Writers concurrently.
 * @see Writer
 */
public final class Sorter extends Thread{
	private Semaphore queueSemaphore = new Semaphore(0, true);
	private Queue<String> queue = new LinkedList<String>();
	private Lock queueLock = new ReentrantLock();
	private List<Writer> writers = new LinkedList<Writer>();
	private SortStrategy strategy;
	
	/**
	 * Initializes a new instance of the Sorter class, with the
	 * specified strategy it will use to sort lines.
	 * @param strategy The strategy it will use.
	 */
	public Sorter(SortStrategy strategy) {
		this.strategy = strategy;
	}
	
	/**
	 * Adds a writer to the subscriber list.
	 * @param writer Writer to be added.
	 */
	public void subscribe(Writer writer) {
		writers.add(writer);
	}
	
	/**
	 * Removes a writer from the subscriber list.
	 * @param writer Writer to remove.
	 */
	public void unsubscribe(Writer writer) {
		writers.remove(writer);
	}
	
	/**
	 * Starts its own thread.
	 */
	public void setup() {
		this.start();
	}
	
	/**
	 * The main process of input, in charge of defining the behavior
	 * of the thread it will run. 
	 */
	public void run() {
		while (true) {
			try {
				queueSemaphore.acquire();
			} catch (InterruptedException e) {
				System.out.println(e);
			}
			
			queueLock.lock();
			String line = queue.poll();
			queueLock.unlock();
			
			if (line.equals("")) {
				break;
			}
			
			strategy.handleNewLine(line);
		}
		
		List<String> sorted = strategy.sort();
		for (Writer writer : writers) {
			writer.setLines(sorted);
			writer.start();
		}
	}
	
	/**
	 * Accepts a new line into its queue of lines that it will sort.
	 * @param line The new line.
	 */
	public void accept(String line) {
		queueLock.lock();
		queue.offer(line);
		queueSemaphore.release();
		queueLock.unlock();
	}
	
	/**
	 * Waits for its work, and the work of all its subscribers, to
	 * end.
	 * @throws InterruptedException When the thread it is on is
	 * interrupted.
	 */
	public void await() throws InterruptedException {
		for (Writer writer : writers) {
			writer.await();
		}
	}
}
