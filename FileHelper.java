import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Utility class for file operations.
 */
public class FileHelper {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private FileHelper() {
	}

	/**
	 * Factory method to get an instance of FileHelper.
	 *
	 * @return A new instance of FileHelper.
	 */
	public static FileHelper getFileHelper() {
		return new FileHelper();
	}

	/**
	 * Parses the content of the specified file and returns it as a list of strings.
	 *
	 * @param filePath The relative path of the file to be parsed.
	 * @return A list of strings, where each string represents a line in the file.
	 */
	public List<String> parseTheContent(String filePath) {
		List<String> content = new ArrayList<>();

		// Return an empty list if the filePath is null
		if (filePath == null) {
			return content;
		}

		// Construct the full directory path
		String directory = System.getProperty("user.dir") + File.separator + filePath;

		// Using try-with-resources for automatic resource management
		try (BufferedReader br = new BufferedReader(new FileReader(directory))) {
			String line;
			// Read each line from the file and add it to the list
			while ((line = br.readLine()) != null) {
				content.add(line);
			}
		} catch (IOException e) {
			// Print the stack trace for debugging; consider replacing with logging in production
			e.printStackTrace();
		}

		return content;
	}
}
