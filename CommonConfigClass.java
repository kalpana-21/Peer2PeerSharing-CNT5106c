import java.util.List;

/**
 * Represents a configuration file in a peer-to-peer network.
 * This class stores configuration parameters such as file name, chunk size,
 * unchoking intervals, and the number of neighbors.
 */
public class CommonConfigClass {

	private String file = "";
	private int chunkSize = 0;
	private int optimisticUnchokingInterval = 0;
	private int numberOfNeighbors = 0;
	private int unchokingInterval = 0;
	private int fileSize = 0;
	private int numberOfChunks = 0;
	


	private CommonConfigClass(int numberOfNeighbors, int unchokingInterval, int optimisticUnchokingInterval,
							  String file, int fileSize, int chunkSize) {

		this.numberOfNeighbors = numberOfNeighbors;
		this.unchokingInterval = unchokingInterval;
		this.optimisticUnchokingInterval = optimisticUnchokingInterval;
		this.file = file;
		this.fileSize = fileSize;
		this.chunkSize = chunkSize;
		this.numberOfChunks = calculateNumberOfChunks(fileSize, chunkSize);
	}
	// Calculates the number of chunks based on file size and chunk size.
	private int calculateNumberOfChunks(int fileSize, int chunkSize) {
		return (int) Math.ceil((double) fileSize / chunkSize);
	}

	/**
	 * Creates a ConfigFile object from a list of configuration parameters.
	 *
	 * @param configLines The list of configuration parameter strings.
	 * @return A ConfigFile object or null if the input is invalid.
	 */
	public static CommonConfigClass getConfigFileObject(List<String> configLines) {
		if (configLines != null && configLines.size() == 6) {
			int numberOfNeighbors = Integer.parseInt(configLines.get(0).split(" ")[1]);
			int unchokingInterval = Integer.parseInt(configLines.get(1).split(" ")[1]);
			int optimisticUnchokingInterval = Integer.parseInt(configLines.get(2).split(" ")[1]);
			String fileName = configLines.get(3).split(" ")[1];
			int fileSize = Integer.parseInt(configLines.get(4).split(" ")[1]);
			int chunkSize = Integer.parseInt(configLines.get(5).split(" ")[1]);

			return new CommonConfigClass(numberOfNeighbors, unchokingInterval, optimisticUnchokingInterval,
					fileName, fileSize, chunkSize);
		}
		return null;
	}
	
	public int getNumberOfNeighbors() {
		return numberOfNeighbors;
	}
	
	public void setNumberOfNeighbors(int numberOfNeighbors) {
		this.numberOfNeighbors = numberOfNeighbors;
	}
	
	public int getUnchokingInterval() {
		return unchokingInterval;
	}
	
	public void setUnchokingInterval(int unchokingInterval) {
		this.unchokingInterval = unchokingInterval;
	}
	
	public int getOptimisticUnchokingInterval() {
		return optimisticUnchokingInterval;
	}
	
	public void setOptimisticUnchokingInterval(int optimisticUnchokingInterval) {
		this.optimisticUnchokingInterval = optimisticUnchokingInterval;
	}
	
	public String getFile() {
		return file;
	}
	
	public void setFile(String file) {
		this.file = file;
	}
	
	public int getFileSize() {
		return fileSize;
	}
	
	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}
	
	public int getChunkSize() {
		return chunkSize;
	}
	
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}
	
	public int getNumberOfChunks() {
		return numberOfChunks;
	}
	
	public void setNumberOfChunks(int numberOfChunks) {
		this.numberOfChunks = numberOfChunks;
	}
  
}
