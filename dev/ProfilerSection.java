package fi.henu.gdxextras.dev;

import java.util.HashMap;

class ProfilerSection implements Comparable
{
	// Raw data
	public final String name;
	public long total_millis;
	public long started_at;

	// Final data
	public float relative_time;
	public float total_relative_time;

	// References
	public final ProfilerSection parent;
	public final HashMap<String, ProfilerSection> children;

	public ProfilerSection(String name, ProfilerSection parent)
	{
		this.name = name;
		total_millis = 0;
		started_at = -1;
		relative_time = 0;
		total_relative_time = 0;
		this.parent = parent;
		children = new HashMap<String, ProfilerSection>();
	}

	@Override
	public int compareTo(Object section_raw)
	{
		ProfilerSection section = (ProfilerSection)section_raw;
		return Long.compare(section.total_millis, total_millis);
	}
}
