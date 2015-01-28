package fi.henu.gdxextras;

import java.util.StringTokenizer;

import com.badlogic.gdx.files.FileHandle;

public class FileHandleUtils
{
	public static FileHandle getRelativeFileHandle(FileHandle dir, String relative_path)
	{
		StringTokenizer tokenizer = new StringTokenizer(relative_path, "\\/");
		FileHandle result = dir;
		while (tokenizer.hasMoreElements()) {
			String token = tokenizer.nextToken();
			if (token.equals("..")) {
				result = result.parent();
			} else {
				result = result.child(token);
			}
		}
		return result;
	}
}
