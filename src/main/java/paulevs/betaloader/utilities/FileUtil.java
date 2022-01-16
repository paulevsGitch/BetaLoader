package paulevs.betaloader.utilities;

import net.fabricmc.loader.launch.common.FabricLauncherBase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtil {
	public static void writeTextFile(Collection<String> lines, File file) {
		try {
			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			for (String line : lines) {
				bufferedWriter.append(line);
				bufferedWriter.append("\n");
			}
			bufferedWriter.close();
			fileWriter.close();
		}
		catch (IOException exception) {
			exception.printStackTrace();
		}
	}
	
	public static List<String> readTextSource(String path) {
		List<String> result = new ArrayList<>();
		try {
			InputStream stream = FabricLauncherBase.class.getClassLoader().getResourceAsStream(path);
			InputStreamReader streamReader = new InputStreamReader(stream);
			BufferedReader bufferedWriter = new BufferedReader(streamReader);
			String line = bufferedWriter.readLine();
			while (line != null) {
				result.add(line);
				line = bufferedWriter.readLine();
			}
			bufferedWriter.close();
			streamReader.close();
			stream.close();
		}
		catch (IOException exception) {
			exception.printStackTrace();
		}
		return result;
	}
	
	public static boolean downloadFile(File file, String url, String name) {
		if (!file.exists()) {
			System.out.println(name + " is missing, trying to download");
			if (downloadFile(url, file)) {
				System.out.println("Success!");
			}
			else {
				System.out.println("Failed to download " + name + "!");
				System.out.println("You can try to download it manually:");
				System.out.println(url);
				name = file.getName();
				String path = file.getParentFile().getAbsolutePath();
				System.out.println("Rename file to \"" + name + "\" and place it into " + path + " directory.");
				return false;
			}
		}
		return true;
	}
	
	public static boolean downloadFile(String url, File file) {
		try {
			file.getParentFile().mkdirs();
			InputStream stream = new URL(url).openStream();
			Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			stream.close();
			return true;
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch (IOException exception) {
			exception.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Will get all classes from specified zip or jar file, without ".class" ending part.
	 * @param file input {@link File}.
	 * @return
	 */
	public static List<String> getZipClasses(File file) {
		List<String> result = new ArrayList<>();
		try {
			ZipFile zipFile = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String name = entry.getName();
				if ((!entry.isDirectory()) && name.endsWith(".class")) {
					result.add(name.substring(0, name.length() - 6));
				}
			}
			
			zipFile.close();
		}
		catch (IOException exception) {
			exception.printStackTrace();
		}
		return result;
	}
}
