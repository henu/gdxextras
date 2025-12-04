package fi.henu.gdxextras;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.StringTokenizer;

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

	public static FileHandle getConfigFileHandle(String app_name)
	{
		if (Gdx.app.getType() == Application.ApplicationType.Android) {
			return Gdx.files.local("");
		}

		if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
			String os = System.getProperty("os.name").toLowerCase();
			if (os.contains("linux")) {
				return Gdx.files.external(".config/" + app_name);
			}
			if (os.contains("win")) {
				return Gdx.files.external("AppData/" + app_name);
			}
			return Gdx.files.local("");
		}

		throw new RuntimeException("Application type not yet supported!");
	}

	// Returns the more fine tuned place to store the configuration, save, etc. files.
	public static FileHandle config(String app_name, String path)
	{
		// Android
		if (Gdx.app.getType() == Application.ApplicationType.Android) {
			return Gdx.files.local(path);
		}
		// Desktop
		if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
			String os = System.getProperty("os.name").toLowerCase();
			FileHandle conf_dir_fh;
			// Windows
			if (os.contains("win")) {
				String appdata_path = System.getenv("APPDATA");
				if (appdata_path != null && !appdata_path.isEmpty()) {
					conf_dir_fh = Gdx.files.absolute(appdata_path + "/" + app_name);
				} else {
					conf_dir_fh = Gdx.files.external(app_name);
				}
			}
			// Mac
			else if (os.contains("mac")) {
				conf_dir_fh = Gdx.files.external("Library/Application Support/" + app_name);
			}
			// Linux + BSDs
			else {
				conf_dir_fh = Gdx.files.external("/.config/" + app_name);
			}
			return conf_dir_fh.child(path);
		}
		throw new RuntimeException("Application type \"" + Gdx.app.getType() + "\" not yet supported!");
	}
}
