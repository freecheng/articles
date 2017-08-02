package fc.file.treewalker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * A derived class of FileVisitor to merge all text file into one single file.
 * @author FC
 */
public class TextFilesMerger extends SimpleFileVisitor<Path> implements Closeable {
	public static final String DEFAULT_FOLDER_LINE_PREFIX = "//  -- folder: ";
	public static final String DEFAULT_FILE_LINE_PREFIX = "//  -- file: ";
	
	private Path rootFolder;
	private String lineSeparator = System.lineSeparator();
	private StringBuilder sb = new StringBuilder();
	private Writer writer;
	
	public TextFilesMerger(Path rootFolder, Path targetFile) throws IOException {
		this.rootFolder = rootFolder;
		
		writer = new BufferedWriter(new FileWriter(targetFile.toFile()));
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		writer.write(DEFAULT_FOLDER_LINE_PREFIX);
		writer.write(rootFolder.relativize(dir).toString());
		writer.write(lineSeparator);
//		sb.append(DEFAULT_FOLDER_LINE_PREFIX).append(rootFolder.relativize(dir)).append(lineSeparator);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		writer.write(DEFAULT_FILE_LINE_PREFIX);
		writer.write(rootFolder.relativize(file).toString());
		writer.write(lineSeparator);
//		sb.append(DEFAULT_FILE_LINE_PREFIX).append(rootFolder.relativize(file)).append(lineSeparator);

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file.toFile()));
			String s = null;
			while ((s = reader.readLine()) != null) {
				writer.write(s);
				writer.write(lineSeparator);
				//sb.append(s).append(lineSeparator);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}

		return FileVisitResult.CONTINUE;
	}

	
	public String getLineSeparator() {
		return lineSeparator;
	}

	public void setLineSeparator(String lineSeparator) {
		this.lineSeparator = lineSeparator;
	}

	@Override
	public String toString() {
		return sb.toString();
	}

	@Override
	public void close() throws IOException {
		if (writer != null) {
			writer.close();
		}
	}
}
package fc.file.treewalker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Split a single merged text file into multiple files.
 * @author FC
 */
public class TextFilesSplitter {
	private Path rootFolder;
	private Path sourceFile;
	private String lineSeparator = System.lineSeparator();
	private Writer writer;
	
	public TextFilesSplitter(Path rootFolder, Path sourceFile) throws IOException {
		this.rootFolder = rootFolder;
		this.sourceFile = sourceFile;
	}
	
	public void split() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(sourceFile.toFile()));
			String line = null;
			while ((line = reader.readLine()) != null) {
				processLine(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e1) {
				}
			}
		}
	}

	private void processLine(String line) throws IOException {
		if (line == null) {
			writer.write("");
			writer.write(lineSeparator);
		} else if (line.startsWith(TextFilesMerger.DEFAULT_FOLDER_LINE_PREFIX)) {
			String relativeFolderPath = line.substring(TextFilesMerger.DEFAULT_FOLDER_LINE_PREFIX.length());
			if (relativeFolderPath.length() == 0) {
				if (rootFolder.toFile().exists()) {
					rootFolder.toFile().mkdirs();
				}
			} else {
				Path newFolder = Paths.get(rootFolder.toString(), relativeFolderPath);
				newFolder.toFile().mkdirs();
				System.out.println("will create folder: '" + newFolder + "'");
			}
		} else if (line.startsWith(TextFilesMerger.DEFAULT_FILE_LINE_PREFIX)) {
			if (writer != null) {
				writer.close();
				writer = null;
			}
			String relativeFilePath = line.substring(TextFilesMerger.DEFAULT_FILE_LINE_PREFIX.length());
			Path newFile = Paths.get(rootFolder.toString(), relativeFilePath);
			writer = new BufferedWriter(new FileWriter(newFile.toFile()));
			System.out.println("will create file: " + newFile);
		} else {
			System.out.println(line);
			writer.write(line);
			writer.write(lineSeparator);
		}
	}

	public String getLineSeparator() {
		return lineSeparator;
	}

	public void setLineSeparator(String lineSeparator) {
		this.lineSeparator = lineSeparator;
	}
}

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import fc.file.treewalker.TextFilesMerger;
import fc.file.treewalker.TextFilesSplitter;

public class Foo {
	public static void main(String[] args) {
		Path folder = Paths.get("/Users/FC/src/FcLib/java/src/java/fc/file/");
		Path targetFile = Paths.get("/Users/FC/tmp/mergedSingleFile.txt");
		TextFilesMerger textFileMerger = null;
		try {
			textFileMerger = new TextFilesMerger(folder, targetFile);
			Files.walkFileTree(folder, textFileMerger);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (textFileMerger != null) {
				try {
					textFileMerger.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		TextFilesSplitter splitter;
		try {
			splitter = new TextFilesSplitter(Paths.get("/Users/FC/tmp/a/"), targetFile);
			splitter.split();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println(textFileMerger.toString());
	}
	

}
