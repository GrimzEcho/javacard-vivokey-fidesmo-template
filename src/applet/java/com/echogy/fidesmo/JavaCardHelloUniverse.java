package com.echogy.fidesmo;

import javacard.framework.*;

public class JavaCardHelloUniverse extends Applet {

	// all of these are stored in EEPROM
	private static final short MAX_LENGTH = 256;
	private static final short MAX_SHORT = 32767;
	private static final short ZERO = (short) 0;

	// EEPROM
	private short accessCounter;

	//private byte[] received;




	private static final byte[] HELLO_UNIVERSE = {
			(byte)'H',(byte)'e',(byte)'l',(byte)'l',(byte)'o',(byte)' ',
			(byte)'U',(byte)'n',(byte)'i',(byte)'v',(byte)'e',(byte)'r',(byte)'s',(byte)'e', (byte)'!'
	};

	/**
	 * Per Fidesmo documentation, all memory allocation should happen in either the install method, or
	 * object constructors called from the install method
	 */
	private JavaCardHelloUniverse(){

		// normally, we would initial a non-transient array for receiving data,
		// but for this simple app, no need to waste the EEPROM writes since we never use it

		//received = new byte[MAX_LENGTH];

		register();
	}

	/**
	 * Installs this applet.
	 * @param bArray the array containing installation parameters
	 * @param bOffset the starting offset in bArray
	 * @param bLength the length in bytes of the parameter data in bArray
	 */
	public static void install(byte[] bArray, short bOffset, byte bLength) {
		new JavaCardHelloUniverse();
	}

	/**
	 * Processes an incoming APDU.
	 * Increment an access counter, then depending on the INS byte, do nothing, respond with a static string, echo
	 * the data that was sent, or respond with the access count
	 * echo the access counter.
	 * @see APDU
	 * @param apdu the incoming APDU
	 * @exception ISOException with the response bytes per ISO 7816-4
	 */
	public void process(APDU apdu){
		byte[] buffer = apdu.getBuffer();

		// don't do anything if this applet is being selected
		if (selectingApplet()) {
			return;
		}

		// don't increment the access counter if it is already at max.
		// remember, unless it's a transient array, variables will persist in EPROM
		if (accessCounter < MAX_SHORT) {
			accessCounter++;
		}

		switch (buffer[ISO7816.OFFSET_INS]) {
			case (byte)0x00:  // noop
				break;

			case (byte) 0x10: // static response
				// local variables that don't instantiate via new use ram
				short msgLength = (short) HELLO_UNIVERSE.length;
				Util.arrayCopyNonAtomic(HELLO_UNIVERSE, ZERO, buffer, ZERO, msgLength);
				apdu.setOutgoingAndSend(ZERO, msgLength);
				break;

			case (byte) 0x20: // return the access count
				Util.setShort(buffer, ZERO, accessCounter);
				apdu.setOutgoingAndSend(ZERO, (short)2);
				break;

			case (byte) 0x21: // set the access count
				accessCounter = Util.getShort(buffer, ISO7816.OFFSET_CDATA);
				apdu.setOutgoingAndSend(ZERO, ZERO);
				break;

			default: // any other command isn't supported
				ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}

	}

}