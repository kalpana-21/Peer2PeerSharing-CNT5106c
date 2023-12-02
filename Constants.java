import java.util.HashMap;

/**
 * Contains constants and enums used in P2P file sharing project.
 */
public class Constants {

	// Constant for the file name
	public static final String THEFILE = "thefile";

	// Header used in the handshake process
	public static final String HEADER_FOR_HANDSHAKE = "P2PFILESHARINGPROJ";

	// Zero bit string used in the handshake process
	public static final String HEADER_FOR_ZERO_BITS_HANDSHAKE = "0000000000";

	/**
	 * Enum representing different types of messages in the P2P network.
	 */
	public static enum TypeOfMessage {
		// Enum values representing various message types with their respective integer codes
		CHOKE(0), UNCHOKE(1), INTERESTED(2), NOT_INTERESTED(3), HAVE(4), BITFIELD(5), REQUEST(6), PIECE(7), COMPLETE(8);

		// Integer value representing the message type
		private final int msgType;

		// HashMap to map integer values to corresponding TypeOfMessage enums
		private static final HashMap<Integer, TypeOfMessage> typeMap = new HashMap<>();

		// Static block to populate the typeMap HashMap
		static {
			for (TypeOfMessage message : TypeOfMessage.values()) {
				typeMap.put(message.msgType, message);
			}
		}

		/**
		 * Constructor for TypeOfMessage enum.
		 *
		 * @param msgType The integer value associated with the message type.
		 */
		private TypeOfMessage(int msgType) {
			this.msgType = msgType;
		}

		/**
		 * Gets the integer value of the message type.
		 *
		 * @return The integer value of the message type.
		 */
		public int getValue() {
			return this.msgType;
		}

		/**
		 * Returns the TypeOfMessage enum corresponding to the given integer value.
		 *
		 * @param mType The integer value of the message type.
		 * @return The corresponding TypeOfMessage enum.
		 * @throws IllegalArgumentException If no TypeOfMessage corresponds to the given value.
		 */
		public static TypeOfMessage valueOf(int mType) {
			TypeOfMessage message = typeMap.get(mType);
			if (message == null) {
				throw new IllegalArgumentException("No TypeOfMessage for value: " + mType);
			}
			return message;
		}
	}

}
