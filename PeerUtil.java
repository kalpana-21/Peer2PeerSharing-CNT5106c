import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Utility class for peer-related operations in  P2P file sharing system.
 */
public class PeerUtil {
	private static FileHelper rfObj = null;
	private static CommonConfigClass commonConfigClassObj = null;
	/**
	 * Default constructor.
	 */
	PeerUtil() {
		rfObj = FileHelper.getFileHelper();
		List<String> configRows = rfObj.parseTheContent("Common.cfg");
		commonConfigClassObj = CommonConfigClass.getConfigFileObject(configRows);
	}

	/**
	 * Retrieves a chunk of data from a file associated with a given peer.
	 *
	 * @param peerId The ID of the peer.
	 * @param chunkIndex The index of the chunk to retrieve.
	 * @return The data of the specified chunk.
	 * @throws IOException If an I/O error occurs.
	 */
	public synchronized byte[] getChunk(int peerId, int chunkIndex) throws IOException {
		String fileName = commonConfigClassObj.getFile();
		String chunkFileName = System.getProperty("user.dir") + File.separator + "peer_" + peerId + File.separator + fileName + "_" + chunkIndex;
		File chunkFile = new File(chunkFileName);

		try (FileInputStream fileInputStream = new FileInputStream(chunkFile)) {
			byte[] chunkData = new byte[(int) chunkFile.length()];
			fileInputStream.read(chunkData);
			return chunkData;
		}
	}

	/**
	 * Stores a chunk of data in the directory of a specified peer.
	 *
	 * @param peerId The ID of the peer.
	 * @param chunkIndex The index of the chunk.
	 * @param chunkData The data of the chunk to store.
	 * @throws IOException If an I/O error occurs.
	 */
	public synchronized void storeChunk(int peerId, int chunkIndex, byte[] chunkData) throws IOException {
		String fileName = commonConfigClassObj.getFile();
		String chunkFileName = System.getProperty("user.dir") + File.separator + "peer_" + peerId + File.separator + fileName + "_" + chunkIndex;
		File chunkFile = new File(chunkFileName);
		try (FileOutputStream fileOutputStream = new FileOutputStream(chunkFile)) {
			fileOutputStream.write(chunkData);
		}
	}

	/**
	 * Combines multiple chunks into a single file in the specified peer's directory.
	 *
	 * @param peerId The ID of the peer.
	 * @param configFile The configuration file containing the number of chunks.
	 * @throws IOException If an I/O error occurs.
	 */
	public synchronized void combineChunksIntoFile(int peerId, CommonConfigClass configFile) throws IOException {
		int numberOfChunks = configFile.getNumberOfChunks();
		String fileName = commonConfigClassObj.getFile();
		String destinationFileName = System.getProperty("user.dir") + File.separator + "peer_" + peerId + File.separator + fileName;
		File combinedFile = new File(destinationFileName);
		try (FileOutputStream fileOutputStream = new FileOutputStream(combinedFile)) {
			for (int i = 0; i < numberOfChunks; i++) {
				File chunkFile = new File(System.getProperty("user.dir") + File.separator + "peer_" + peerId + File.separator + fileName + "_" + i);

				try (FileInputStream fileInputStream = new FileInputStream(chunkFile)) {
					byte[] chunkData = new byte[(int) chunkFile.length()];
					fileInputStream.read(chunkData);
					fileOutputStream.write(chunkData);
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
	public File createPeerDirectoryAndLogFile(int peerId) {
		File logFile = null;
		try {
			// Create directory for the peer
			File peerDirectory = new File(System.getProperty("user.dir") + File.separator + "peer_" + peerId);
			if (!peerDirectory.exists()) {
				peerDirectory.mkdir();
			}

			// Create log file for the peer
			logFile = new File(System.getProperty("user.dir") + File.separator + "log_peer_" + peerId + ".log");
			if (logFile.createNewFile()) {
				System.out.println("Log file has been created");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return logFile;
	}

	/**
	 * Splits a file into multiple chunks and stores them in a peer's directory.
	 *
	 * @param peerId The ID of the peer.
	 * @param configFile The configuration file containing chunk size and other details.
	 */
	public void splitFileIntoChunks(String peerId, CommonConfigClass configFile) {
		int chunkSize = configFile.getChunkSize();
		int numberOfChunks = configFile.getNumberOfChunks();
		int fileSize = configFile.getFileSize();
		String fileName = commonConfigClassObj.getFile();
		String sourceFilePath = System.getProperty("user.dir")+File.separator+ fileName;
		try (FileInputStream fileInputStream = new FileInputStream(sourceFilePath)) {
			int i = 0, totalBytesCopied = 0;
			while (i < numberOfChunks) {
				int bytesToCopy = Math.min(chunkSize, fileSize - totalBytesCopied);
				byte[] buffer = new byte[bytesToCopy];
				fileInputStream.read(buffer);

				String chunkFileName = System.getProperty("user.dir") + File.separator + "peer_" + peerId + File.separator + fileName+ "_" + i;
				try (FileOutputStream fileOutputStream = new FileOutputStream(chunkFileName)) {
					fileOutputStream.write(buffer);
				}

				totalBytesCopied += bytesToCopy;
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generates a handshake packet for initiating communication with another peer.
	 *
	 * @param sourcePeerId The ID of the source peer.
	 * @return The handshake packet as a byte array.
	 */
	public synchronized byte[] generateHandshakePacket(int sourcePeerId) {
		String handshakeHeader = Constants.HEADER_FOR_HANDSHAKE;
		byte[] headerBytes = handshakeHeader.getBytes();
		String zeroBits = Constants.HEADER_FOR_ZERO_BITS_HANDSHAKE;
		byte[] zeroBytes = zeroBits.getBytes();
		byte[] peerIdBytes = String.valueOf(sourcePeerId).getBytes();
		byte[] handshakePacket = new byte[headerBytes.length + zeroBytes.length + peerIdBytes.length];
		System.arraycopy(headerBytes, 0, handshakePacket, 0, headerBytes.length);
		System.arraycopy(zeroBytes, 0, handshakePacket, headerBytes.length, zeroBytes.length);
		System.arraycopy(peerIdBytes, 0, handshakePacket, headerBytes.length + zeroBytes.length, peerIdBytes.length);

		return handshakePacket;
	}
}
