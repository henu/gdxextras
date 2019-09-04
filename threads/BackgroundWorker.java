package fi.henu.gdxextras.threads;

import fi.henu.gdxextras.containers.RingBuffer;

public class BackgroundWorker
{
	public BackgroundWorker(int number_of_threads)
	{
		queue = new RingBuffer<Runnable>();
		threads = new Thread[number_of_threads];
		for (int i = 0; i < number_of_threads; ++ i) {
			Thread thread = new Thread(new Runner());
			thread.start();
			threads[i] = thread;
		}
	}

	public void addToQueue(Runnable runnable)
	{
		synchronized (queue) {
			queue.add(runnable);
			queue.notify();
		}
	}

	public void dispose()
	{
		stop_requested = true;
		synchronized (queue) {
			queue.notifyAll();
		}
		for (int i = 0; i < threads.length; ++ i) {
			if (threads[i] != null) {
				try {
					threads[i].join();
				}
				catch (InterruptedException e) {
				}
				threads[i] = null;
			}
		}
	}

	private class Runner implements Runnable
	{
		@Override
		public void run()
		{
			while (!stop_requested) {
				// Try to get work from queue
				Runnable runnable = null;
				synchronized (queue) {
					// If there is nothing in the queue, then wait
					if (queue.empty()) {
						try {
							queue.wait();
						}
						catch (InterruptedException e) {
							return;
						}
					}
					// If there is now work, then pick it
					if (!queue.empty()) {
						runnable = queue.pop();
					}
				}

				// Run if work was got
				if (runnable != null) {
					runnable.run();
				}
			}
		}
	}

	private final Thread[] threads;

	private boolean stop_requested;

	private final RingBuffer<Runnable> queue;
}
