package fi.henu.gdxextras.dev;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;

public class Profiler
{
	public static void begin(String section_name)
	{
		if (active_section == null) {
			active_section = sections.get(section_name);
			if (active_section == null) {
				active_section = new ProfilerSection(section_name, null);
				sections.put(section_name, active_section);
			}
			assert active_section.started_at < 0;
			active_section.started_at = System.currentTimeMillis();
		} else {
			ProfilerSection parent_active_section = active_section;
			active_section = parent_active_section.children.get(section_name);
			if (active_section == null) {
				active_section = new ProfilerSection(section_name, parent_active_section);
				parent_active_section.children.put(section_name, active_section);
			}
			assert active_section.started_at < 0;
			active_section.started_at = System.currentTimeMillis();
		}
	}

	public static void end()
	{
		if (active_section == null) {
			throw new RuntimeException("No section to end!");
		}
		active_section.total_millis += System.currentTimeMillis() - active_section.started_at;
		active_section.started_at = -1;
		active_section = active_section.parent;
	}

	public static String getStatsAsString()
	{
		if (active_section != null) {
			throw new RuntimeException("Section is not closed!");
		}
		calculateStatsRecuresively(1f, sections);
		String stats = "Profiling statistics:\n";
		stats += gatherStatsAsStringRecursively(sections, 0);
		return stats;
	}

	private static final HashMap<String, ProfilerSection> sections = new HashMap<String, ProfilerSection>();
	private static ProfilerSection active_section = null;

	private static void calculateStatsRecuresively(float total_percentage, HashMap<String, ProfilerSection> sections)
	{
		// Calculate total time spent
		long sections_millis = 0;
		for (ProfilerSection section : sections.values()) {
			sections_millis += section.total_millis;
		}
		// Now that the total time is known, it is possible
		// to calculate relative time for sections
		for (ProfilerSection section : sections.values()) {
			section.relative_time = (float)section.total_millis / sections_millis;
			section.total_relative_time = section.relative_time * total_percentage;
			calculateStatsRecuresively(section.total_relative_time, section.children);
		}
	}

	private static String gatherStatsAsStringRecursively(HashMap<String, ProfilerSection> sections, int depth)
	{
		String result = "";
		Array<ProfilerSection> sections_sorted = new Array<ProfilerSection>();
		for (ProfilerSection section : sections.values()) {
			sections_sorted.add(section);
		}
		sections_sorted.sort();
		for (ProfilerSection section : sections_sorted) {
			for (int i = 0; i < depth; ++ i) {
				result += "  ";
			}
			result += section.name + " " + section.total_millis + " ms (" + MathUtils.round(section.total_relative_time * 100) + " %, " + MathUtils.round(section.relative_time * 100) + " %)\n";
			result += gatherStatsAsStringRecursively(section.children, depth + 1);
		}
		return result;
	}
}
