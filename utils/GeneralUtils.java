package fi.henu.gdxextras.utils;

import java.util.concurrent.Callable;

public class GeneralUtils
{

	public static <T> T retry(Callable<T> func, int max_tries, long sleep_ms)
	{
		int try_i = 0;
		while (true) {
			++try_i;
			try {
				return func.call();
			}
			catch (Exception e) {
				if (try_i >= max_tries) {
					throw new RuntimeException(e);
				}
				try {
					Thread.sleep(sleep_ms);
				}
				catch (InterruptedException ignored) {
					throw new RuntimeException(e);
				}
			}
		}
	}

}
