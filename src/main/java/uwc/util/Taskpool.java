package uwc.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <br>
 * service请求的基本类，所有service请求都统一调用该类中的 doTask(runable)方法。</br>
 * @author steven
 * 
 */

public class Taskpool {
	private static Taskpool instance = new Taskpool();
	private static final int corePoolSize = 6;
	private static final int maximumPoolSize = 20;
	private static final int keepAliveTime = 30;
	private static final TimeUnit timeUnit = TimeUnit.SECONDS;
	private static BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(maximumPoolSize);
	private static ThreadFactory factory = new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			return t;
		}
	};

	private static ThreadPoolExecutor executor = new ThreadPoolExecutor(
			corePoolSize, maximumPoolSize, keepAliveTime, timeUnit, workQueue, factory);

	public static synchronized Taskpool sharedInstance() {
		synchronized (Taskpool.class) {
			if (instance == null) {
				instance = new Taskpool();
			}
			return instance;
		}
	}

	/**
	 * 线程开启任务 供model调用
	 */
	@SuppressWarnings("unchecked")
	public void doTask(Runnable runnable) {
		executor.execute(runnable);
	}

	/**
	 * 对外的取消task任务方法
	 * */
	public void cancelTask(Runnable runnable) {
		executor.remove(runnable);
	}
}
