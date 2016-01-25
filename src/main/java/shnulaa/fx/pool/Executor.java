package shnulaa.fx.pool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Executor {

	/** log instance **/
	private static Logger log = LoggerFactory.getLogger(Executor.class);

	/**
	 * Default thread priority
	 */
	private int threadPriority = Thread.NORM_PRIORITY;

	/**
	 * Run threads in daemon or non-daemon state
	 */
	private boolean daemon = false;

	/**
	 * Default name prefix for the thread name
	 */
	private String namePrefix = "Pipe-Worker-";

	/**
	 * max number of threads
	 */
	private int maxThreads = 20;

	/**
	 * min number of threads
	 */
	private int minSpareThreads = 20;

	/**
	 * idle time in milliseconds
	 */
	private int maxIdleTime = 60000;

	/**
	 * The executor we use for this component
	 */
	private ThreadPoolExecutor executor = null;

	/**
	 * prestart threads?
	 */
	private boolean prestartminSpareThreads = false;

	/**
	 * the default constructor
	 */
	private Executor() {
		TaskThreadFactory tf = new TaskThreadFactory(namePrefix, daemon, threadPriority);
		executor = new ThreadPoolExecutor(minSpareThreads, maxThreads, maxIdleTime, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(), tf);
		if (prestartminSpareThreads) {
			executor.prestartAllCoreThreads();
		}
	}

	/**
	 * Stop the thread pool
	 */
	public void stopInternal() {
		if (executor != null)
			executor.shutdownNow();
		executor = null;
	}

	/**
	 * see {@link Superclass}
	 */
	public void execute(Runnable command) {
		if (executor != null) {
			try {
				executor.execute(command);
			} catch (RejectedExecutionException rx) {
				log.error("RejectedExecutionException occurred when push data..", rx);
			}
		} else
			throw new IllegalStateException("StandardThreadPool not started.");
	}

	/**
	 * thread factory
	 * 
	 * @author liuyq
	 * 
	 */
	static class TaskThreadFactory implements ThreadFactory {
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;
		private final boolean daemon;
		private final int threadPriority;

		public TaskThreadFactory(String namePrefix, boolean daemon, int priority) {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			this.namePrefix = namePrefix;
			this.daemon = daemon;
			this.threadPriority = priority;
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement());
			t.setDaemon(daemon);
			t.setPriority(threadPriority);
			return t;
		}
	}

	static class SingletonHolder {
		public static final Executor EXECUTOR = new Executor();
	}

	public static Executor getInstance() {
		return SingletonHolder.EXECUTOR;
	}

}
