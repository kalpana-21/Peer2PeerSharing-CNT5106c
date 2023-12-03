import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;



public class peerProcess {

	private static int srcPort = 0;  // Port number of the source peer
	private static int srcPeerId = -1;  // ID of the source peer
	private static int currPeerIdx = -1;  // Index of the current peer in some context
	private static int totalNoOfPeers = -1;  // Total number of peers in the network
	private static int totalNoOfChunks = 0;  // Total number of chunks of the file being shared
	private static FileHelper fh = null;  // Helper class for file-related operations, initially null
	private static LoggingClass log = null;  // Logging utility class instance, initially null
	private static CommonConfigClass commCon = null;  // Common configuration class instance, initially null
	private static ServerSocket listener = null;  // ServerSocket for listening to incoming connections, initially null
	private static PeerUtil peerUtil = null;  // Utility class for peer-related operations, initially null
	private static ConcurrentHashMap<Integer,Integer> mapForBitField = new ConcurrentHashMap<>();  // Map to store bitfields for each peer
	private static Map<Integer, NeighborPeer> neighborPeers = new LinkedHashMap<>();  // Map to store information about neighbor peers
	private static ConcurrentHashMap<Integer,NeighbrConn> neighbrConnMap = new ConcurrentHashMap<>();  // Map to store connections with neighbor peers
	private static ConcurrentHashMap<Integer, Integer> dwnldRate = new ConcurrentHashMap<>();  // Map to store download rates of peers
	private static CopyOnWriteArrayList<Integer> peersInterested = new CopyOnWriteArrayList<>();  // List of peers that are interested
	private static CopyOnWriteArrayList<Integer> peersUnchoked = new CopyOnWriteArrayList<>();  // List of peers that are unchoked
	private static CopyOnWriteArrayList<Integer> peersCompleted = new CopyOnWriteArrayList<>();  // List of peers that have completed downloading

	private static AtomicInteger peersWithFullFile = new AtomicInteger(0);  // Atomic integer to count peers with the complete file

	private static AtomicInteger optUnchokedPeer = new AtomicInteger(-1);  // Atomic integer to store the ID of the optimistically unchoked peer
	private static boolean isCompleteFile;  // Flag to indicate if the current peer has the complete file




	/**
	 * Represents a connection with a neighbor peer in a peer-to-peer network.
	 * This class encapsulates the functionality for communication and data exchange
	 * with a neighbor peer through a network socket.
	 */
	class NeighbrConn {
		int peerId = -1;  // The ID of the connected neighbor peer
		byte[] message = null;  // Buffer to hold incoming or outgoing messages
		NeighborPeer peer = null;  // NeighborPeer object representing the connected peer
		DataOutputStream os = null;  // Output stream to send data to the peer
		DataInputStream is = null;  // Input stream to receive data from the peer
		Socket sock = null;  // The socket representing the network connection to the peer
		boolean unchkd = false;  // Flag indicating whether the peer is currently unchoked


		/**
		 * Constructs a NeighbrConn object to manage the connection with a neighbor peer.
		 *
		 * @param sock The socket representing the connection to the neighbor.
		 * @param peer The NeighborPeer object representing the connected peer.
		 * @throws IOException If an I/O error occurs while setting up the streams.
		 */
		public NeighbrConn(Socket sock, NeighborPeer peer) throws IOException {
			peerId = peer.getPeerId();
			message = new byte[5];
			is = new DataInputStream(sock.getInputStream());
			os = new DataOutputStream(sock.getOutputStream());
			this.sock = sock;
			this.peer = peer;
		}

		/**
		 * Creates and starts a new thread to handle the interaction with the neighbor peer.
		 *
		 * @throws Exception If an error occurs in starting the thread.
		 */
		public void initiateConnection() throws Exception {
			// Create a new thread for handling neighbor peer interaction
			PeerInteractionHandler interactionHandler = new PeerInteractionHandler();
			Thread interactionThread = new Thread(interactionHandler, "InteractionThread_" + peer.getPeerId());
			interactionThread.start();
			System.out.println(interactionThread.getName() + " started");
		}


		/**
		 * Converts a message into a byte array format that can be sent to other peers.
		 * The format includes the message length, type, and payload.
		 *
		 * @param mssgType The type of the message.
		 * @param payload  The payload of the message, may be null if there is no payload.
		 * @return The byte array representing the formatted message.
		 */
		public synchronized byte[] fetchMessage(int mssgType, byte[] payload) {

			// Calculate the size of the payload, if any
			int payloadSize = (payload != null) ? payload.length : 0;

			// Allocate a ByteBuffer for the message length
			ByteBuffer byteBuffer = ByteBuffer.allocate(4);

			// The total length of the message is 1 byte for the message type plus the payload size
			int totLength = 1 + payloadSize;
			byte[] messageLength = byteBuffer.putInt(totLength).array();

			// Convert the message type to a byte
			byte messageType = (byte)(char)mssgType;

			// Create an array to hold the entire message
			byte[] msg = new byte[totLength + messageLength.length];
			int idx = 0, i = 0;

			// Copy the message length to the message array
			while (i < messageLength.length) {
				msg[idx] = messageLength[i];
				idx++;
				i++;
			}

			// Add the message type to the message array
			msg[idx] = messageType;
			idx++;

			// If there is a payload, add it to the message array
			if (payload != null) {
				int j = 0;
				while (j < payload.length) {
					msg[idx] = payload[j];
					idx++;
					j++;
				}
			}

			return msg;
		}


		/**
		 * Converts an array of integers into a corresponding byte array.
		 * Each integer is broken down into 4 bytes and stored sequentially in the byte array.
		 *
		 * @param intArray Array of integers to be converted.
		 * @return Byte array representing the integers.
		 */
		public synchronized byte[] intTobyteArray(int[] intArray) {
			// Allocate a ByteBuffer with enough space to store all integers (4 bytes per integer)
			ByteBuffer byteBuf = ByteBuffer.allocate(intArray.length * 4);

			// Create an IntBuffer to write the integers to the ByteBuffer
			IntBuffer intBuf = byteBuf.asIntBuffer();

			// Place the integers into the IntBuffer, automatically populating the ByteBuffer
			intBuf.put(intArray);

			// Convert the populated ByteBuffer into a byte array and return it
			byte[] byteArray = byteBuf.array();
			return byteArray;
		}

		/**
		 * Converts a byte array into an array of integers.
		 * Assumes that each integer is represented by 4 consecutive bytes in the byte array.
		 *
		 * @param byteArray Byte array to be converted into integers.
		 * @return Array of integers derived from the byte array.
		 */
		public synchronized int[] byteTointArray(byte[] byteArray) {
			int idx = 0; // Index for the resulting integer array
			// Initialize an array to hold the integers (4 bytes per integer)
			int[] intArray = new int[byteArray.length / 4];

			// Iterate over the byte array in steps of 4 (size of an integer)
			for (int i = 0; i < byteArray.length; i += 4) {
				byte[] bit = new byte[4]; // Temporary array to hold 4 bytes

				// Copy 4 bytes from the byteArray into 'bit'
				System.arraycopy(byteArray, i, bit, 0, 4);

				// Convert the 4 bytes into an integer and store in the integer array
				intArray[idx] = ByteBuffer.wrap(bit).getInt();
				idx++;
			}
			return intArray;
		}


		/**
		 * Sends a bitfield message to a connected peer.
		 * The bitfield message consists of the current state of the chunks (pieces of the file)
		 * that the peer has. Each index in the bitfield represents a chunk, with the value indicating
		 * whether the chunk is possessed (1) or not (0).
		 */
		public synchronized void sendBitF() {
			// Initialize an array to represent the bitfield
			int[] bitF = new int[totalNoOfChunks];

			// Populate the bitfield array from the current state in mapForBitField
			int i = 0;
			while (i < mapForBitField.size()) {
				bitF[i] = mapForBitField.get(i);
				i++;
			}

			// Convert the bitfield array to a byte array (payload for the message)
			byte[] payload = intTobyteArray(bitF);

			// Fetch the byte array message with the BITFIELD message type and payload
			byte[] msg = fetchMessage(Constants.TypeOfMessage.BITFIELD.getValue(), payload);

			// Attempt to send the message to the peer
			try {
				os.write(msg);  // Write the message to the output stream
				os.flush();  // Ensure all data is sent by flushing the stream
				log.logForBitFieldSent(srcPeerId, peerId);  // Log the event of sending a bitfield message
			} catch (IOException ex) {
				ex.printStackTrace();  // Print the stack trace in case of an IOException
			}
		}


		/**
		 * Sends a 'complete' message to indicate that the peer has downloaded the entire file.
		 * This message is used to notify connected peers that the file download is complete.
		 */
		public synchronized void sendTotalMsg() {
			// Create a 'complete' message with no payload
			byte[] msg = fetchMessage(Constants.TypeOfMessage.COMPLETE.getValue(), null);

			// Attempt to send the message
			try {
				os.write(msg);  // Write the message to the output stream
				os.flush();  // Ensure all data is sent by flushing the stream
			} catch (IOException ex) {
				System.exit(0);  // Exit if an IOException occurs
			}
		}


		/**
		 * Sends a 'choke' message to the connected peer, unless the peer is optimistically unchoked.
		 * If the peer is currently unchoked, it changes its status to choked and removes it from
		 * the list of unchoked peers.
		 */
		public synchronized void sendChokeMessage() {
			// Check if the peer is not the optimistically unchoked peer
			if (optUnchokedPeer.get() != peerId) {
				// Create a 'choke' message with no payload
				byte[] msg = fetchMessage(Constants.TypeOfMessage.CHOKE.getValue(), null);

				// Attempt to send the 'choke' message
				try {
					os.write(msg);  // Write the 'choke' message to the output stream
					os.flush();     // Ensure all data is sent by flushing the stream
				} catch (IOException e) {
					System.exit(0);  // Exit if an IOException occurs
					e.printStackTrace();
				}
			}

			// If the peer is currently unchoked, update its status and remove it from the unchoked peers list
			if (unchkd) {
				unchkd = false;  // Set the unchoked status to false
				int idx = peersUnchoked.indexOf(peerId);  // Find the index of the peer in the unchoked list
				if (idx != -1) {
					peersUnchoked.remove(idx);  // Remove the peer from the unchoked list
				}
			}
		}


		/**
		 * Sends an 'unchoke' message to the connected peer.
		 * If the unchoking is not due to optimistic unchoking, the peer's status is set to unchoked
		 * and it is added to the list of unchoked peers.
		 *
		 * @param isOptimis Flag indicating whether the unchoking is optimistic.
		 */
		public synchronized void sendUnChokeMessage(boolean isOptimis) {
			// Create an 'unchoke' message with no payload
			byte[] msg = fetchMessage(Constants.TypeOfMessage.UNCHOKE.getValue(), null);

			// Attempt to send the 'unchoke' message
			try {
				os.write(msg);  // Write the 'unchoke' message to the output stream
				os.flush();     // Ensure all data is sent by flushing the stream
			} catch (IOException e) {
				System.exit(0);  // Exit if an IOException occurs
				e.printStackTrace();
			}

			// If the unchoking is not optimistic, update the status of the peer and add it to the unchoked peers list
			if (!isOptimis) {
				unchkd = true;  // Set the unchoked status to true
				peersUnchoked.addIfAbsent(peerId);  // Add the peer to the list of unchoked peers if it's not already present
			}
		}


		/**
		 * Checks if the connected peer has any pieces of the file that this peer does not have.
		 * This is used to determine if this peer should be interested in the connected peer.
		 *
		 * @return True if the connected peer has at least one piece that this peer does not have, false otherwise.
		 */
		public synchronized boolean peerHasInterestingPieces() {
			// Retrieve the bitfield of the connected peer
			int[] peerBF = peer.getBitfield();

			// Flag to indicate if the connected peer has interesting pieces
			boolean intrstd = false;

			// Iterate through each chunk to check if the connected peer has pieces that this peer doesn't have
			int i = 0;
			while (i < commCon.getNumberOfChunks()) {
				// Check if this peer does not have the piece (mapForBitField value is 0) and the connected peer has it (peerBF value is 1)
				if (mapForBitField.get(i) == 0 && peerBF[i] == 1) {
					intrstd = true;  // Mark as interested
					break;  // No need to check further as one interesting piece is found
				}
				i++;
			}
			return intrstd;  // Return the interest status
		}


		/**
		 * Sends either an 'interested' or 'not interested' message to the connected peer,
		 * based on whether this peer has any interesting pieces that the connected peer possesses.
		 */
		public synchronized void sendIntrstdOrNotMessage() {
			// Determine if this peer is interested in any pieces of the connected peer
			boolean isIntrstd = peerHasInterestingPieces();

			// Prepare the appropriate message based on the interest status
			byte[] message;
			if (isIntrstd)
				message = fetchMessage(Constants.TypeOfMessage.INTERESTED.getValue(), null);  // Create 'interested' message
			else
				message = fetchMessage(Constants.TypeOfMessage.NOT_INTERESTED.getValue(), null);  // Create 'not interested' message

			// Attempt to send the message
			try {
				os.write(message);  // Write the message to the output stream
				os.flush();  // Ensure all data is sent by flushing the stream

				// Log the appropriate event based on the interest status
				if (isIntrstd) {
					log.logForSendInterestedMessage(srcPeerId, peerId);  // Log the sending of an 'interested' message
				} else {
					log.logForSendNotInterestedMessage(srcPeerId, peerId);  // Log the sending of a 'not interested' message
				}
			} catch (IOException ex) {
				ex.printStackTrace();  // Print the stack trace in case of an IOException
			}
		}


		/**
		 * Determines the index of a random chunk that the connected peer has and this peer does not have.
		 * If no such chunk is available, returns -1.
		 *
		 * @return The index of a random required chunk, or -1 if no chunk is needed.
		 */
		private synchronized int fetchRandomChunkRequired() {
			// List to store indices of chunks that the connected peer has and this peer doesn't
			List<Integer> chunksIndicesr = new ArrayList<>();
			int randPieceIdx = -1;  // Default value indicating no chunk required
			int i = 0;  // Index for iterating through chunks

			// Get the bitfield of the connected peer
			int[] peerBitF = peer.getBitfield();

			// Iterate through all chunks to find those needed by this peer
			while (i < totalNoOfChunks) {
				// If this peer doesn't have the chunk (mapForBitField value is 0) and the connected peer has it (peerBitF value is 1)
				if (mapForBitField.get(i) == 0 && peerBitF[i] == 1) {
					chunksIndicesr.add(i);  // Add the index to the list
				}
				i++;
			}

			// If there are any chunks that this peer needs
			if (chunksIndicesr.size() > 0) {
				Random rand = new Random();
				int randIdx = rand.nextInt(chunksIndicesr.size());  // Choose a random index from the list
				randPieceIdx = chunksIndicesr.get(randIdx);  // Get the chunk index at the chosen random index
			}

			return randPieceIdx;  // Return the index of the required chunk, or -1 if none
		}


		/**
		 * Sends a 'request' message to the connected peer for a random chunk that this peer needs.
		 * If no such chunk is needed (as determined by fetchRandomChunkRequired), sends an
		 * 'interested' or 'not interested' message instead.
		 */
		public synchronized void sendRqsttMessage() {
			// Fetch the index of a random chunk that is required by this peer
			int randChunkIdx = fetchRandomChunkRequired();

			// If a chunk is needed (index is not -1)
			if (randChunkIdx != -1) {
				// Prepare a ByteBuffer to hold the index of the chunk
				ByteBuffer byteBuf = ByteBuffer.allocate(4);
				byte[] payload = byteBuf.putInt(randChunkIdx).array();  // Convert the index to a byte array

				// Create a 'request' message with the chunk index as the payload
				byte[] message = fetchMessage(Constants.TypeOfMessage.REQUEST.getValue(), payload);

				// Attempt to send the 'request' message
				try {
					os.write(message);  // Write the message to the output stream
					os.flush();         // Ensure all data is sent by flushing the stream
					log.logForSendRequestMessage(srcPeerId, peerId, randChunkIdx);  // Log the sending of the 'request' message
				} catch (IOException ex) {
					ex.printStackTrace();  // Print the stack trace in case of an IOException
				}
			} else {
				// If no chunk is needed, send an 'interested' or 'not interested' message
				sendIntrstdOrNotMessage();
			}
		}


		/**
		 * Sends a piece of the file to the connected peer, if the piece is requested,
		 * and if this peer has the piece and is either unchoked or optimistically unchoked.
		 *
		 * @param pieceIdx The index of the piece to be sent.
		 */
		public synchronized void sendPieceMessage(int pieceIdx) {
			// Check if the peer is unchoked or optimistically unchoked and if this peer has the requested piece
			if ((unchkd || (optUnchokedPeer.get() == peerId)) && mapForBitField.get(pieceIdx) == 1) {
				try {
					ByteBuffer byteBuf = ByteBuffer.allocate(4); // Allocate a ByteBuffer for the piece index
					byte[] piece = peerUtil.getChunk(srcPeerId, pieceIdx); // Retrieve the piece data
					byte[] idx = byteBuf.putInt(pieceIdx).array(); // Convert the index to a byte array

					// Create an array to hold both the index and the piece data
					byte[] payload = new byte[idx.length + piece.length];
					// Copy the index and piece data into the payload array
					System.arraycopy(idx, 0, payload, 0, idx.length);
					System.arraycopy(piece, 0, payload, idx.length, piece.length);

					// Create a 'piece' message with the payload
					byte[] pieceMessage = fetchMessage(Constants.TypeOfMessage.PIECE.getValue(), payload);
					os.write(pieceMessage); // Write the message to the output stream
					log.logForSendPieceMessage(srcPeerId, peerId, pieceIdx); // Log the sending of the piece message
					os.flush(); // Ensure all data is sent by flushing the stream
				} catch (IOException ex) {
					ex.printStackTrace(); // Print the stack trace in case of an IOException
				}
			}
		}


		/**
		 * Sends a 'have' message to the connected peer, indicating that this peer now has a specific piece of the file.
		 *
		 * @param pieceIdx The index of the piece that this peer now has.
		 */
		public synchronized void sendHaveMessage(int pieceIdx) {
			// Allocate a ByteBuffer to hold the index of the piece
			ByteBuffer byteBuf = ByteBuffer.allocate(4);
			byte[] payload = byteBuf.putInt(pieceIdx).array(); // Convert the index to a byte array

			// Create a 'have' message with the piece index as the payload
			byte[] message = fetchMessage(Constants.TypeOfMessage.HAVE.getValue(), payload);

			// Attempt to send the 'have' message
			try {
				os.write(message);  // Write the message to the output stream
				os.flush();         // Ensure all data is sent by flushing the stream
				log.logForSendHaveMessage(srcPeerId, peerId, pieceIdx);  // Log the sending of the 'have' message
			} catch (IOException ex) {
				ex.printStackTrace();  // Print the stack trace in case of an IOException
			}
		}


		/**
		 * Updates the bitfield of the connected peer based on receiving a 'have' message.
		 * If this peer does not have the piece indicated in the 'have' message,
		 * a 'request' message for that piece is sent to the peer.
		 *
		 * @param havePieceIdx The index of the piece indicated in the received 'have' message.
		 */
		public synchronized void updateNeighbourBitF(int havePieceIdx) {
			// Retrieve and update the connected peer's bitfield
			int[] peer_bitfield = peer.getBitfield();
			peer_bitfield[havePieceIdx] = 1;  // Mark the piece as available in the peer's bitfield
			peer.setBitfield(peer_bitfield);

			// Check if this peer does not have the piece indicated in the 'have' message
			if (mapForBitField.get(havePieceIdx) == 0) {
				// Prepare a ByteBuffer to hold the index of the piece
				ByteBuffer byteBuf = ByteBuffer.allocate(4);
				byte[] payload = byteBuf.putInt(havePieceIdx).array(); // Convert the index to a byte array

				// Create a 'request' message for the piece
				byte[] message = fetchMessage(Constants.TypeOfMessage.REQUEST.getValue(), payload);

				// Attempt to send the 'request' message
				try {
					os.write(message);  // Write the message to the output stream
					os.flush();         // Ensure all data is sent by flushing the stream
				} catch (IOException ie) {
					ie.printStackTrace();  // Print the stack trace in case of an IOException
				}
			}
		}


		/**
		 * Verifies if the connected peer has completed downloading the entire file.
		 * Updates various states related to the peer's completion status.
		 */
		public synchronized void VerifyEntireFile() {
			// Flag to indicate whether the connected peer has the complete file
			boolean hasCompleteFile = true;

			// Retrieve the bitfield of the connected peer to check each piece's status
			int[] peerBitF = peer.getBitfield();

			// Iterate through the peer's bitfield to check if all pieces are downloaded
			int i = 0;
			while (i < peerBitF.length) {
				if (peerBitF[i] == 0) {
					// If any piece is missing, set flag to false and break the loop
					hasCompleteFile = false;
					break;
				}
				i++;
			}

			// If the connected peer has the complete file
			if (hasCompleteFile) {
				// Remove the peer from the list of interested peers, if it is present
				int index = peersInterested.indexOf(peerId);
				if (index != -1) {
					peersInterested.remove(index);
				}

				// If the peer is the current optimistically unchoked peer, reset that status
				if (optUnchokedPeer.get() == peerId) {
					optUnchokedPeer.set(-1);
				}

				// Send a choke message to the peer
				sendChokeMessage();

				// Update the peer's status to indicate it has the complete file
				peer.setHasFile(1);

				// Update lists and counters if the peer was not previously marked as completed
				if (!peersCompleted.contains(peerId)) {
					// Set the peer's download rate to -1 and add it to the list of completed peers
					dwnldRate.put(peerId, -1);
					peersCompleted.add(peerId);
					// Increment the count of peers with the complete file
					peersWithFullFile.incrementAndGet();
					System.out.println(peerId + " (neighbor) has finished downloading");
				}

				// If this peer has not completed downloading, check for interest in other pieces
				if (!isCompleteFile) {
					sendIntrstdOrNotMessage();
				}
			}
		}


		class PeerInteractionHandler implements Runnable {

			public void run() {
				// Initialize download rate for this peer to zero
				dwnldRate.put(peerId, 0);
				// Send initial bitfield message to connected peer
				sendBitF();
				try {
					// Continue processing messages until all peers have the complete file
					while (peersWithFullFile.get() < totalNoOfPeers) {

						// Retrieve the size of the incoming message
						for (int i = 0; i < 4; i++) {
							is.read(message, i, 1);
						}
						int size = ByteBuffer.wrap(message).getInt();

						// Determine the type of the incoming message
						is.read(message, 0, 1);
						int m_type = message[0];

						// Handle Bitfield message type
						if (m_type == Constants.TypeOfMessage.BITFIELD.getValue()) {
							byte[] bytes = new byte[size - 1];
							for (int i = 0; i < size - 1; i++) {
								is.read(bytes, i, 1);
							}
							int[] peer_bit = byteTointArray(bytes);
							peer.setBitfield(peer_bit);
							// Check if the peer has the complete file
							boolean hasCompleteFile = true;
							log.logForBitFieldRcvd(srcPeerId, peerId);
							for (int i = 0; i < peer_bit.length; i++) {
								if (peer_bit[i] == 0) {
									hasCompleteFile = false;
									break;
								}
							}
							// Update file completion status and send interested message if necessary
							if (hasCompleteFile && !peersCompleted.contains(peerId)) {
								peer.setHasFile(1);
								peersCompleted.add(peerId);
								peersWithFullFile.incrementAndGet();
								System.out.println(peerId + " has the full file");
								sendIntrstdOrNotMessage();
							}
						}
						// Process Interested message type
						else if (m_type == Constants.TypeOfMessage.INTERESTED.getValue()) {
							peersInterested.addIfAbsent(peerId);
							log.logForReceivingInterestedMessage(srcPeerId, peerId);
						}
						// Handle Not Interested message type
						else if (m_type == Constants.TypeOfMessage.NOT_INTERESTED.getValue()) {
							int idx = peersInterested.indexOf(peerId);
							if (idx != -1) {
								log.logForReceivingNotInterestedMessage(srcPeerId, peerId);
								peersInterested.remove(idx);
							}
							if (peerId == optUnchokedPeer.get()) {
								optUnchokedPeer.set(-1);
							}
							sendChokeMessage();
						}
						// Process Choke message type
						else if (m_type == Constants.TypeOfMessage.CHOKE.getValue()) {
							log.logForChoking(srcPeerId, peerId);
						}
						// Handle Unchoke message type
						else if (m_type == Constants.TypeOfMessage.UNCHOKE.getValue()) {
							log.logForUnchoking(srcPeerId, peerId);
							sendRqsttMessage();
						}
						// Process Request message type
						else if (m_type == Constants.TypeOfMessage.REQUEST.getValue()) {
							peersInterested.addIfAbsent(peerId);
							byte[] payload = new byte[size - 1];
							for (int i = 0; i < size - 1; i++) {
								is.read(payload, i, 1);
							}
							int idx_p = ByteBuffer.wrap(payload).getInt();
							log.logForReceivingRequestMessage(srcPeerId, peerId, idx_p);
							sendPieceMessage(idx_p);
						}
						// Handle Piece message type
						else if (m_type == Constants.TypeOfMessage.PIECE.getValue()) {
							byte[] piece = new byte[size - 5];
							byte[] idxArray = new byte[4];
							for (int i = 0; i < 4; i++) {
								is.read(idxArray, i, 1);
							}
							int idxOfReceivedP = ByteBuffer.wrap(idxArray).getInt();
							int dwnldRtPeer = dwnldRate.get(peerId);
							dwnldRate.put(peerId, dwnldRtPeer + 1);
							if (mapForBitField.get(idxOfReceivedP) == 0) {
								mapForBitField.put(idxOfReceivedP, 1);
								for (int i = 0; i < piece.length; i++) {
									is.read(piece, i, 1);
								}
								peerUtil.storeChunk(srcPeerId, idxOfReceivedP, piece);
								boolean haveICompleted = true;
								int n0OfPIHave = 0;
								for (Map.Entry<Integer, Integer> e : mapForBitField.entrySet()) {
									if (e.getValue() == 0) {
										haveICompleted = false;
									} else {
										n0OfPIHave++;
									}
								}
								log.logForDownloadingAPiece(srcPeerId, peerId, idxOfReceivedP, n0OfPIHave);
								if (haveICompleted && !peersCompleted.contains(srcPeerId)) {
									peersCompleted.add(srcPeerId);
									peersWithFullFile.incrementAndGet();
									System.out.println(srcPeerId + " (I) have completed downloading");
									isCompleteFile = true;
									log.logforCompletionOfDownload(srcPeerId);
									TimeUnit.SECONDS.sleep(2);
									peerUtil.combineChunksIntoFile(srcPeerId, commCon);
								} else if (!peersCompleted.contains(srcPeerId)) {
									sendRqsttMessage();
								}
							}
							for (Map.Entry<Integer, NeighbrConn> entry : neighbrConnMap.entrySet()) {
								NeighbrConn npiObjAdjacentPeer = entry.getValue();
								npiObjAdjacentPeer.sendHaveMessage(idxOfReceivedP);
							}
						}
						// Process Have message type
						else if (m_type == Constants.TypeOfMessage.HAVE.getValue()) {
							byte[] idx = new byte[4];
							for (int i = 0; i < 4; i++) {
								is.read(idx, i, 1);
							}
							int havePIdx = ByteBuffer.wrap(idx).getInt();
							if (havePIdx > -1) {
								log.logForReceivingHaveMessage(srcPeerId, peerId, havePIdx);
								updateNeighbourBitF(havePIdx);
								VerifyEntireFile();
							}
						}
						// Handle Complete message type
						else if (m_type == Constants.TypeOfMessage.COMPLETE.getValue()) {
							log.logForCompletionOfProcess();
							System.exit(0);
						}
					}
					System.out.println(peerId + " Thread ended");
					System.out.println("No.of peers with complete file = " + peersWithFullFile.get());
				} catch (IOException e) {
					// Handle IOException
				} catch (Exception e) {
					// Handle other exceptions
				}
			}
		}


	}


	// This class manages the choking and unchoking of peers based on their download rates and interest status.
	class ChokeManager implements Runnable {

		// Sorts and returns a list of peers based on their download rates.
		public List<Map.Entry<Integer, Integer>> getPeersBasedOnDwnldRt() {
			List<Map.Entry<Integer, Integer>> orderedPList = new LinkedList<>(dwnldRate.entrySet());
			Collections.sort(orderedPList, new Comparator<Map.Entry<Integer, Integer>>() {
				public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
					return o1.getValue().compareTo(o2.getValue());
				}
			});
			return orderedPList;
		}

		// The main execution method for the choke management thread.
		public void run() {
			int unchokeTime = commCon.getUnchokingInterval();
			try {
				// Continuously manage choking and unchoking while not all peers have the complete file.
				while (peersWithFullFile.get() < totalNoOfPeers) {
					int intrstPSize = peersInterested.size();
					if (intrstPSize > 0) {
						int prefNeighbors = commCon.getNumberOfNeighbors();

						// Unchoke a smaller number of peers if fewer than preferred are interested.
						if (intrstPSize < prefNeighbors) {
							for (int pId : peersInterested) {
								peerProcess.NeighbrConn npiObj = neighbrConnMap.get(pId);
								if (!npiObj.unchkd) {
									npiObj.sendUnChokeMessage(false);
								}
							}
						} else {
							// Select and unchoke preferred peers based on download rates.
							List<Map.Entry<Integer, Integer>> sortedPeersMapList = getPeersBasedOnDwnldRt();
							List<Integer> tempPeersList = new ArrayList<>(peersInterested);
							int[] prefPs = new int[prefNeighbors];
							Random rand = new Random();

							for (int i = 0; i < prefNeighbors; i++) {
								int rndIdx = rand.nextInt(tempPeersList.size());
								int pId = tempPeersList.remove(rndIdx);
								prefPs[i] = pId;
								peerProcess.NeighbrConn npiObj = neighbrConnMap.get(pId);
								if (!npiObj.unchkd) {
									npiObj.sendUnChokeMessage(false);
								}
							}

							log.logForChangeOfPreferredNeighbors(srcPeerId, prefPs);

							// Choke peers not selected as preferred.
							for (int peerId : tempPeersList) {
								peerProcess.NeighbrConn npiObj = neighbrConnMap.get(peerId);
								npiObj.sendChokeMessage();
							}
						}
					}
					TimeUnit.SECONDS.sleep(unchokeTime);
				}
			} catch (InterruptedException ie) {
				// Handle interruption during sleep.
			} catch (Exception e) {
				// Handle other exceptions.
			}
		}
	}



	// Manages the optimistic unchoking of peers in a peer-to-peer network.
	class OptimisticChokeManager implements Runnable {

		public void run() {
			try {
				// Continuously check and manage optimistic unchoking until all peers have the complete file.
				while (peersWithFullFile.get() < totalNoOfPeers) {
					// Proceed only if there are interested peers.
					if (!peersInterested.isEmpty()) {
						// Determine the interval for optimistic unchoking.
						int optSleepTime = commCon.getOptimisticUnchokingInterval();
						Random rand = new Random();

						// Randomly select a peer from the list of interested peers.
						int pId = peersInterested.get(rand.nextInt(peersInterested.size()));
						optUnchokedPeer.set(pId);

						// Retrieve the connection object for the selected peer and send an unchoke message.
						NeighbrConn nconnObj = neighbrConnMap.get(pId);
						nconnObj.sendUnChokeMessage(true);

						// Log the event of changing the optimistically unchoked neighbor.
						log.logForChangeOfOptimisticallyUnchokedNeighbor(srcPeerId, pId);

						// Sleep for the specified optimistic unchoking interval.
						TimeUnit.SECONDS.sleep(optSleepTime);

						// Reset the optimistically unchoked peer.
						optUnchokedPeer.set(-1);

						// Choke the peer if it is only optimistically unchoked and not already unchoked.
						if (!nconnObj.unchkd) {
							nconnObj.sendChokeMessage();
						}
					}
				}
			} catch (InterruptedException ie) {
				// Handle any interruptions during the sleep period.
			}
		}
	}


	// This class handles the initiation of TCP connections with peers that started earlier.
	class Client implements Runnable {

		@Override
		public void run() {
			int idx = 0;
			Iterator<Entry<Integer, NeighborPeer>> iterator = neighborPeers.entrySet().iterator();

			// Loop through the peers that started before the current peer.
			while (idx < currPeerIdx) {
				Entry<Integer, NeighborPeer> entry = iterator.next();
				int peerId = entry.getKey();
				NeighborPeer peerObject = entry.getValue();
				String hostName = peerObject.getHost();
				int portNumber = peerObject.getPortNo();

				try {
					// Establish a TCP connection with the peer.
					Socket socket = new Socket(hostName, portNumber);
					DataInputStream inputStream = new DataInputStream(socket.getInputStream());
					DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

					// Send a handshake packet to the peer.
					byte[] handshakeHeader = peerUtil.generateHandshakePacket(srcPeerId);
					outputStream.write(handshakeHeader);

					// Receive and process the handshake response.
					byte[] receivedHandshake = new byte[handshakeHeader.length];
					inputStream.readFully(receivedHandshake);
					int receivedPeerId = Integer.parseInt(new String(Arrays.copyOfRange(receivedHandshake, 28, 32)));

					// If the handshake is successful, establish the connection.
					if (receivedPeerId == peerId) {
						NeighbrConn neighborConnection = new NeighbrConn(socket, peerObject);
						neighborConnection.initiateConnection();
						neighbrConnMap.put(peerId, neighborConnection);
						log.logForTcpConnectionTo(srcPeerId, peerId);
					}
					idx++;
					outputStream.flush();
				} catch (UnknownHostException uhe) {
					// Handle unknown host exceptions.
					uhe.printStackTrace();
				} catch (IOException ioe) {
					// Handle IO exceptions.
					ioe.printStackTrace();
				} catch (Exception e) {
					// Handle other exceptions.
					e.printStackTrace();
				}
			}
		}
	}


	//This class Initiates TCP connections with subsequent peers by awaiting their connection requests and then exchanges handshake packets to establish communication.
	class Server implements Runnable {

		@Override
		public void run() {
			int peerIndex = currPeerIdx;
			try {
				ServerSocket serverListener = new ServerSocket(srcPort);
				// Continuously listen for incoming connections from peers starting after the current peer.
				while (peerIndex < totalNoOfPeers - 1) {
					Socket connectionSocket = serverListener.accept();
					DataInputStream dataInput = new DataInputStream(connectionSocket.getInputStream());
					DataOutputStream dataOutput = new DataOutputStream(connectionSocket.getOutputStream());

					// Read the handshake packet sent by the connecting peer.
					byte[] receivedHandshake = new byte[32];
					dataInput.readFully(receivedHandshake);
					int connectingPeerId = Integer.parseInt(new String(Arrays.copyOfRange(receivedHandshake, 28, 32)));

					// Respond with a handshake packet to the connecting peer.
					byte[] handshakeResponse = peerUtil.generateHandshakePacket(srcPeerId);
					dataOutput.write(handshakeResponse);

					// Retrieve the corresponding NeighborPeer object and establish a connection.
					NeighborPeer connectedPeer = neighborPeers.get(connectingPeerId);
					NeighbrConn connectionHandler = new NeighbrConn(connectionSocket, connectedPeer);
					connectionHandler.initiateConnection();
					neighbrConnMap.put(connectingPeerId, connectionHandler);
					log.logForTcpConnectionFrom(srcPeerId, connectingPeerId);
					peerIndex++;
				}
			} catch (UnknownHostException uhe) {
				// Handle exceptions related to unknown hosts.
				uhe.printStackTrace();
			} catch (IOException ioe) {
				// Handle input/output exceptions.
				ioe.printStackTrace();
			} catch (Exception e) {
				// Handle any other exceptions.
				e.printStackTrace();
			}
		}
	}


	// Configures peer information and updates the peer map
	private static void configurePeerInformation(List<String> peerInfoLines) throws Exception {
		totalNoOfPeers = 0;

		for (String infoLine : peerInfoLines) {
			int currentPeerId = Integer.parseInt(infoLine.split(" ")[0]);
			if (currentPeerId == srcPeerId) {
				currPeerIdx = totalNoOfPeers;
				srcPort = Integer.parseInt(infoLine.split(" ")[2]);
				isCompleteFile = Integer.parseInt(infoLine.split(" ")[3]) == 1;

			} else {
				NeighborPeer peerInstance = NeighborPeer.getPeer(infoLine);
				neighborPeers.put(currentPeerId, peerInstance);
			}
			totalNoOfPeers++;
		}
		totalNoOfPeers = peerInfoLines.size();
	}


	public static void main(String[] args) throws Exception {

		srcPeerId = Integer.parseInt(args[0]);
		// Load common configuration settings
		fh = FileHelper.getFileHelper();
		List<String> commonConfigLines = fh.parseTheContent("Common.cfg");
		commCon = CommonConfigClass.getConfigFileObject(commonConfigLines);
		totalNoOfChunks = commCon.getNumberOfChunks();
		peerUtil = new PeerUtil();

		// Load peer information and initialize peer settings
		List<String> peerInfoLines = fh.parseTheContent("PeerInfo.cfg");
		configurePeerInformation(peerInfoLines);

		// Set up logging and file directory for the peer
		File peerLogFile = peerUtil.createPeerDirectoryAndLogFile(srcPeerId);
		log = new LoggingClass(peerLogFile);
		log.readCommonCfgFile(srcPeerId, commCon);

		// Initialize bitfield and file chunks if the peer has the complete file
		int fileStatusFlag = 0;
		if (isCompleteFile && !peersCompleted.contains(srcPeerId)) {
			peersCompleted.add(srcPeerId);
			peersWithFullFile.incrementAndGet();
			System.out.println(srcPeerId + " (I) have the full file");
			fileStatusFlag = 1;
			peerUtil.splitFileIntoChunks("" + srcPeerId, commCon);
			peerUtil.combineChunksIntoFile(srcPeerId, commCon);
		}

		// Set bitfield for all chunks
		for (int i = 0; i < totalNoOfChunks; i++) {
			mapForBitField.put(i, fileStatusFlag);
		}

		peerProcess peerInstance = new peerProcess();
		// Initialize network connections with other peers
		Client clientHandler = peerInstance.new Client();
		Thread clientThread = new Thread(clientHandler, "Client Thread");
		clientThread.start();

		Server serverHandler = peerInstance.new Server();
		Thread serverThread = new Thread(serverHandler, "Server Thread");
		serverThread.start();

		// Start choke management threads
		ChokeManager chokeManager = peerInstance.new ChokeManager();
		Thread chokeManagerThread = new Thread(chokeManager, "Choke thread");
		chokeManagerThread.start();

		OptimisticChokeManager optimisticChokeManager = peerInstance.new OptimisticChokeManager();
		Thread optimisticChokeThread = new Thread(optimisticChokeManager, "Optimistic Choke thread");
		optimisticChokeThread.start();
		System.out.println("Total Peers: " + totalNoOfPeers);

		// Monitor for completion of file download across all peers
		while (true) {
			TimeUnit.SECONDS.sleep(10);
			if (peersWithFullFile.get() >= totalNoOfPeers) {
				for (Entry<Integer, NeighbrConn> connectionEntry : neighbrConnMap.entrySet()) {
					NeighbrConn adjacentPeerConnection = connectionEntry.getValue();
					adjacentPeerConnection.sendTotalMsg();
				}

				TimeUnit.SECONDS.sleep(10);
				System.out.println("Graceful exit initiated");
				log.logForCompletionOfProcess();
				System.exit(0);
			}
		}
	}


}
