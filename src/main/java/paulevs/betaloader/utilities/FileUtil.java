package paulevs.betaloader.utilities;

import com.google.common.collect.Maps;
import net.fabricmc.loader.launch.common.FabricLauncherBase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class FileUtil {
	public static void writeTextFile(Collection<String> lines, File file) {
		file.getParentFile().mkdirs();
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
		} catch (IOException e) {
			e.printStackTrace();
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
	
	/**
	 * Will save image file into specified location. Format will be extracted from file extension.
	 * @param file {@link File} to save image into.
	 * @param img {@link BufferedImage} to save.
	 */
	public static void saveImage(File file, BufferedImage img) {
		String format = file.getName();
		format = format.substring(format.lastIndexOf('.') + 1);
		try {
			file.getParentFile().mkdirs();
			ImageIO.write(img, format, file);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Will make a zip file from spedified directory. All files will get names from relative path, directory will be root.
	 * @param dirIn {@link File} directory to zip.
	 * @param zipOut {@link File} in which directory will be saves as zip archive.
	 */
	public static void zipDirectory(File dirIn, File zipOut) {
		int startPath = dirIn.getAbsolutePath().length() + 1;
		Map<File, String> map = getFiles(dirIn, startPath);
		byte[] bytes = new byte[1024];
		
		try {
			FileOutputStream fileOut = new FileOutputStream(zipOut);
			ZipOutputStream outputStream = new ZipOutputStream(fileOut);
			map.forEach((file, name) -> {
				try {
					FileInputStream fileIn = new FileInputStream(file);
					ZipEntry zipEntry = new ZipEntry(name);
					outputStream.putNextEntry(zipEntry);
					int length = fileIn.read(bytes);
					while (length >= 0) {
						outputStream.write(bytes, 0, length);
						length = fileIn.read(bytes);
					}
					fileIn.close();
				}
				catch (IOException exception) {
					exception.printStackTrace();
				}
			});
			outputStream.close();
			fileOut.close();
		}
		catch (IOException exception) {
			exception.printStackTrace();
		}
	}
	
	/**
	 * Get map for zipping directory.
	 * @param file {@link File} to process.
	 * @param startPath absolute path of root length.
	 * @return {@link Map} of {@link File} as a key and {@link String} path as a value.
	 */
	private static Map<File, String> getFiles(File file, int startPath) {
		Map<File, String> names = Maps.newHashMap();
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f: files) {
				names.putAll(getFiles(f, startPath));
			}
		}
		else if (file.isFile()) {
			String name = file.getAbsolutePath();
			names.put(file, name.substring(startPath).replace('\\', '/'));
		}
		return names;
	}
	
	/**
	 * Will recursively delete folder and all its content.
	 * @param folder {@link File} to remove
	 */
	public static void deleteFolder(File folder) {
		if (folder.isDirectory()) {
			File[] files = folder.listFiles();
			for (File f: files) {
				deleteFolder(f);
			}
		}
		folder.delete();
	}
}
