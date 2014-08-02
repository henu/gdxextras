package fi.henu.gdxextras;

public class Timeestimator
{

	public void start(int target)
	{
		started = System.currentTimeMillis();
		this.target = target;
		last_speed_update = this.started;
		value_now = 0;
		speed = 0;
		value_since_last_speedupdate = 0;
		last_update_time = this.started;
	}

	public void add(int value)
	{
		value_now += value;
		value_since_last_speedupdate += value;
		assert value_now <= target;
		last_update_time = System.currentTimeMillis();
	}

	public int getEstimate()
	{
		updateSpeed();
		if (speed == 0) {
			return -1;
		}
		int left = target - value_now;
		// Estimate amount of added values since last update
		double seconds_passed_since_last_update = (System.currentTimeMillis() - last_update_time) / 1000.0;
		float estimated_adds = (float)(speed * seconds_passed_since_last_update);
		int result = Math.round((left - estimated_adds) / speed);
		return Math.max(0, result);
	}

	public static String secondsToText(int seconds)
	{
		int days_left = seconds / 60 / 60 / 24;
		int hours_left = (seconds / 60 / 60) % 24;
		int minutes_left = (seconds / 60) % 60;
		int seconds_left = seconds % 60;

		String result = "";

		if (days_left > 0) {
			result += String.format("%2s", days_left + "d").replace(' ', '0') + " ";
		}
		if (days_left > 0 || hours_left > 0) {
			result += String.format("%2s", hours_left + "h").replace(' ', '0') + " ";
		}
		result += String.format("%5s", minutes_left + "min").replace(' ', '0') + " ";
		result += String.format("%3s", seconds_left + "s").replace(' ', '0');

		return result;
	}

	private static final int SPEED_UPDATE_INTERVAL_MS = 5 * 1000;

	private long started;
	private int target;
	private int value_now;
	private long last_update_time;
	private float speed;
	private long last_speed_update;
	private int value_since_last_speedupdate;

	private void updateSpeed()
	{
		long now = System.currentTimeMillis();
		if (now < last_speed_update + SPEED_UPDATE_INTERVAL_MS || value_since_last_speedupdate == 0) {
			return;
		}
		double seconds_passed = (now - last_speed_update) / 1000.0;
		speed = (float)(value_since_last_speedupdate / seconds_passed);
		value_since_last_speedupdate = 0;
		last_speed_update = now;
	}
}
