import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility class for peer-related operations in  P2P file sharing system.
 */
public class PeerUtil {
	/**
	 * Default constructor.
	 */
	PeerUtil() {
	}



	/**
	 * Splits a file into multiple chunks and stores them in a peer's directory.
	 *
	 * @param peerId The ID of the peer.
	 * @param configFile The configuration file containing chunk size and other details.
	 */
	public void divideFileIntoChunks(String peerId, CommonConfigClass configFile) {
		// Retrieve chunk size, number of chunks, file size, and file name from the configuration
		int chunkSize = configFile.getChunkSize();
		int numberOfChunks = configFile.getNumberOfChunks();
		int fileSize = configFile.getFileSize();
		String fileName = configFile.getFile();

		// Construct the path for the source file
		String sourceFilePath = System.getProperty("user.dir") + File.separator + fileName;

		// Use a try-with-resources statement to automatically close the FileInputStream
		try (FileInputStream is = new FileInputStream(sourceFilePath)) {
			int i = 0, totalBytesCopied = 0;

			// Iterate over the number of chunks to create each chunk file
			while (i < numberOfChunks) {
				// Determine the size of the chunk to be copied
				int bytesToCopy = Math.min(chunkSize, fileSize - totalBytesCopied);
				byte[] buffer = new byte[bytesToCopy];

				// Read the chunk from the source file
				is.read(buffer);

				// Construct the file name for the chunk
				String chunkFileName = System.getProperty("user.dir") + File.separator + "peer_" + peerId + File.separator + fileName + "_" + i;

				// Use a try-with-resources statement to automatically close the FileOutputStream
				try (FileOutputStream os = new FileOutputStream(chunkFileName)) {
					// Write the chunk to its file
					os.write(buffer);
				}

				// Update the total number of bytes copied and move to the next chunk
				totalBytesCopied += bytesToCopy;
				i++;
			}
		} catch (IOException e) {
			// Print the stack trace in case of an IOException
			e.printStackTrace();
		}
	}


	/**
	 * Retrieves a chunk of data from a file associated with a given peer.
	 *
	 * @param peerId The ID of the peer.
	 * @param chunkIndex The index of the chunk to retrieve.
	 * @param configFile The configuration file containing file details.
	 * @return The data of the specified chunk.
	 * @throws IOException If an I/O error occurs.
	 */
	public synchronized byte[] fetchChunk(int peerId, int chunkIndex, CommonConfigClass configFile) throws IOException {
		// Retrieve the file name from the configuration
		String fileName = configFile.getFile();

		// Construct the file name for the specific chunk
		String chunkFileName = System.getProperty("user.dir") + File.separator + "peer_" + peerId + File.separator + fileName + "_" + chunkIndex;

		// Create a File object to represent the chunk file
		File chunkFile = new File(chunkFileName);

		// Use a try-with-resources statement to automatically close the FileInputStream
		try (FileInputStream fileInputStream = new FileInputStream(chunkFile)) {
			// Allocate a byte array to hold the data of the chunk
			byte[] chunkData = new byte[(int) chunkFile.length()];

			// Read the entire chunk data into the byte array
			fileInputStream.read(chunkData);

			// Return the byte array containing the chunk data
			return chunkData;
		}
	}


	/**
	 * Stores a chunk of data in the directory of a specified peer.
	 *
	 * @param peerId The ID of the peer.
	 * @param chunkIndex The index of the chunk.
	 * @param chunkData The data of the chunk to store.
	 * @param configFile The configuration file containing file details.
	 * @throws IOException If an I/O error occurs.
	 */
	public synchronized void saveChunk(int peerId, int chunkIndex, byte[] chunkData, CommonConfigClass configFile) throws IOException {
		// Retrieve the file name from the configuration
		String fileName = configFile.getFile();

		// Construct the file name for the specific chunk
		String chunkFileName = System.getProperty("user.dir") + File.separator + "peer_" + peerId + File.separator + fileName + "_" + chunkIndex;

		// Create a File object to represent the chunk file
		File chunkFile = new File(chunkFileName);

		// Use a try-with-resources statement to automatically close the FileOutputStream
		try (FileOutputStream os = new FileOutputStream(chunkFile)) {
			// Write the chunk data to the file
			os.write(chunkData);
		}
	}


	/**
	 * Combines multiple chunks into a single file in the specified peer's directory.
	 *
	 * @param peerId The ID of the peer.
	 * @param configFile The configuration file containing the number of chunks.
	 * @throws IOException If an I/O error occurs.
	 */
	public synchronized void mixChunksIntoFile(int peerId, CommonConfigClass configFile) throws IOException {
		// Retrieve the total number of chunks and the file name from the configuration
		int numberOfChunks = configFile.getNumberOfChunks();
		String fileName = configFile.getFile();

		// Construct the file name for the destination combined file
		String destinationFileName = System.getProperty("user.dir") + File.separator + "peer_" + peerId + File.separator + fileName;
		File combinedFile = new File(destinationFileName);

		// Use a try-with-resources statement to automatically close the FileOutputStream
		try (FileOutputStream os = new FileOutputStream(combinedFile)) {
			// Iterate through each chunk
			for (int i = 0; i < numberOfChunks; i++) {
				// Construct the file name for each chunk
				File chunkFile = new File(System.getProperty("user.dir") + File.separator + "peer_" + peerId + File.separator + fileName + "_" + i);

				// Use a try-with-resources statement to automatically close the FileInputStream
				try (FileInputStream is = new FileInputStream(chunkFile)) {
					// Allocate a byte array to hold the data of the chunk
					byte[] chunkData = new byte[(int) chunkFile.length()];

					// Read the entire chunk data into the byte array
					is.read(chunkData);

					// Write the chunk data to the destination file
					os.write(chunkData);
				}
			}
		}
	}


	/**
	 * Creates a directory and a log file for a specific peer.
	 *
	 * @param peerId The ID of the peer.
	 * @return The created log file.
	 */
	public File buildDirectoryAndLogFile(int peerId) {
		File logFile = null;
		try {
			// Create a directory specific to the peer using its peerId
			File peerDirectory = new File(System.getProperty("user.dir") + File.separator + "peer_" + peerId);
			// Check if the directory already exists, if not, create it
			if (!peerDirectory.exists()) {
				peerDirectory.mkdir();
			}

			// Create a log file specific to the peer in its directory
			logFile = new File(System.getProperty("user.dir") + File.separator + "log_peer_" + peerId + ".log");
			// Check if the log file needs to be created, and if so, create it
			if (logFile.createNewFile()) {
				System.out.println("Log file has been created"); // Log the creation of the file
			}
		} catch (Exception e) {
			e.printStackTrace(); // Print any exceptions that occur during the directory and log file creation
		}
		return logFile; // Return the created log file
	}



	/**
	 * Generates a handshake packet for initiating communication with another peer.
	 *
	 * @param sourcePeerId The ID of the source peer.
	 * @return The handshake packet as a byte array.
	 */
	public synchronized byte[] generateHandshakePacket(int sourcePeerId) {
		// Define the handshake header as specified in the Constants
		String handshakeHeader = Constants.HEADER_FOR_HANDSHAKE;
		byte[] headerBytes = handshakeHeader.getBytes(); // Convert the header to bytes

		// Define the zero bits part of the handshake message as specified in the Constants
		String zeroBits = Constants.HEADER_FOR_ZERO_BITS_HANDSHAKE;
		byte[] zeroBytes = zeroBits.getBytes(); // Convert the zero bits to bytes

		// Convert the source peer's ID to bytes
		byte[] peerIdBytes = String.valueOf(sourcePeerId).getBytes();

		// Prepare an array to hold the complete handshake message
		byte[] handshakePacket = new byte[headerBytes.length + zeroBytes.length + peerIdBytes.length];

		int index = 0;

		// Copy the header bytes into the handshake message
		for (byte b : headerBytes) {
			handshakePacket[index++] = b;
		}

		// Copy the zero bytes into the handshake message
		for (byte b : zeroBytes) {
			handshakePacket[index++] = b;
		}

		// Copy the peer ID bytes into the handshake message
		for (byte b : peerIdBytes) {
			handshakePacket[index++] = b;
		}

		// Return the assembled handshake packet
		return handshakePacket;
	}


}
