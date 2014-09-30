package fi.henu.gdxextras;

import java.util.StringTokenizer;

import com.badlogic.gdx.files.FileHandle;

public class FileHandleUtils
{
	public static FileHandle getRelativeFileHandle(FileHandle file_or_dir, String relative_path)
	{
		StringTokenizer tokenizer = new StringTokenizer(relative_path, "\\/");
		FileHandle result;
		if (file_or_dir.isDirectory()) {
			result = file_or_dir;
		} else {
			result = file_or_dir.parent();
		}
		while (tokenizer.hasMoreElements()) {
			String token = tokenizer.nextToken();
			if (token == "..") {
				result = result.parent();
			} else {
				result = result.child(token);
			}
		}
		return result;
	}
}
