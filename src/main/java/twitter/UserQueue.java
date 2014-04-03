package twitter;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserQueue {

	private static Logger logger = LogManager
			.getFormatterLogger(UserQueue.class.getName());
	private static ConcurrentLinkedQueue<Long> list = new ConcurrentLinkedQueue<Long>();

	public synchronized static void enQueue(Long u) {
		logger.entry(u);
		list.add(u);
	}

	public synchronized static void enQueueMany(Long[] u) {
		int size = u.length;
		for (int i = 0; i < size; i++) {
			enQueue(u[i]);
		}
	}

	public synchronized static Long deQueue() {
		return logger.exit(list.poll());
	}

	public synchronized static boolean hasUsers() {
		return !list.isEmpty();
	}

	public static int size() {
		return list.size();
	}

}