/**
 * Represents a neighbor peer in a peer-to-peer network.
 * Stores information about the peer such as its ID, host, port number,
 * file possession status, and a bit field representing the parts of the file it has.
 */
public class NeighborPeer {

	// Array representing the bitfield of the file parts possessed by this peer
	private int[] bitFieldArray = null;

	// Host address of the peer
	private String host = "";

	// Unique identifier for the peer
	private int peerId = -1;

	// Network port number for the peer
	private int portNo = -1;

	// Indicates whether the peer has the complete file (0 = no, 1 = yes)
	private int hasFile = 0;

	// Private constructor for creating a new NeighborPeer instance
	private NeighborPeer(int peerId, String host, int portNo, int hasFile) {
		this.peerId = peerId;
		this.host = host;
		this.portNo = portNo;
		this.hasFile = hasFile;
	}

	/**
	 * Factory method to create a NeighborPeer from a string record.
	 * The record should contain the peer ID, host, port number, and file status.
	 *
	 * @param record The string containing peer information.
	 * @return A new instance of NeighborPeer.
	 */
	public static NeighborPeer getPeer(String record) {
		String[] values = record.split(" ");
		int peerId = Integer.parseInt(values[0]);
		String host = values[1];
		int portNo = Integer.parseInt(values[2]);
		int hasFile = Integer.parseInt(values[3]);
		return new NeighborPeer(peerId, host, portNo, hasFile);
	}

	/**
	 * Gets the peer ID.
	 *
	 * @return The peer ID.
	 */
	public int getPeerId() {
		return peerId;
	}

	/**
	 * Gets the host address of the peer.
	 *
	 * @return The host address.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Gets the port number of the peer.
	 *
	 * @return The port number.
	 */
	public int getPortNo() {
		return portNo;
	}

	/**
	 * Sets the file possession status of the peer.
	 *
	 * @param hasFile The file possession status (0 for not having the file, 1 for having it).
	 */
	public void setHasFile(int hasFile) {
		this.hasFile = hasFile;
	}

	/**
	 * Gets the bitfield array representing the parts of the file the peer has.
	 *
	 * @return The bitfield array.
	 */
	public int[] getBitfield() {
		return bitFieldArray;
	}

	/**
	 * Sets the bitfield representing the parts of the file that the peer has.
	 *
	 * @param bitfield The bitfield array to set.
	 */
	public void setBitfield(int[] bitfield) {
		this.bitFieldArray = bitfield;
	}

}
