package com.echogy.fidesmo;

import javax.smartcardio.*;
import java.nio.ByteBuffer;
import java.util.List;
import com.licel.jcardsim.utils.AIDUtil;
import com.licel.jcardsim.utils.ByteUtil;

public class HelloUniverseClient {

	private static CardChannel channel;

	public static void main(String[] args) throws CardException {
		System.out.println("Client started");

		if (args.length == 0) {
			System.err.println("You must provide the HelloUniverse AID as the first argument");
			System.exit(1);
		}

		String aid = args[0];

		if (aid.length() < 9) {
			System.out.println("Short AID detected. Assuming that it is a Fidesmo app id and expanding to full AID");
			String prefix = "A00000061700";
			String suffix = "01";
			aid = prefix + aid + suffix;
		}

		TerminalFactory factory = TerminalFactory.getDefault();
		CardTerminals terminals = factory.terminals();

		System.out.println("Waiting for smart card");
		Card card = waitForCardInsertion(terminals);

		if (card == null) {
			System.out.println("Timout reached. Exiting");
			System.exit(0);
		}

		channel = card.getBasicChannel();

		// select the HelloUniverse applet
		ResponseAPDU selectResponse = selectApplet(aid);

		if (selectResponse.getSW() != 0x9000) {
			String errorStatus = Integer.toHexString(selectResponse.getSW());
			System.out.println("Error in applet select command. Response status = " + errorStatus);
		}

		// if 2nd command arg is RESET, then reset the access counter
		if (args.length >= 2 && args[1].toLowerCase().equals("reset")) {
			System.out.println("Resetting access count...");
			boolean success = setAccessCount((short) 0);
			System.out.println(success ? "\tsuccess" : "");
			System.exit(0);
		}

		// Send HELLO command and print response as encoded string
		System.out.println("Saying hello...");
		String response = sayHello();
		System.out.println("\t" + response);

		// Return the access counter or -1 if there was an error
		System.out.println("Getting access count...");
		int count = getAccessCount();
		System.out.println("\t" + count);


	}

	private static Card waitForCardInsertion(CardTerminals terminals) throws CardException {

		// if card is present when method invoked, immeditely return a card instance
		List<CardTerminal> cardPresentTerminals = terminals.list(CardTerminals.State.CARD_PRESENT);
		if (cardPresentTerminals.size() > 0) {
			return cardPresentTerminals.get(0).connect("*");
		}

		// otherwise wait up to 15 seconds for a card to be tapped/inserted
		boolean cardInserted = terminals.waitForChange(15000);
		if (cardInserted) {
			CardTerminal insertedTerminal = terminals.list(CardTerminals.State.CARD_INSERTION).get(0);
			Card card = insertedTerminal.connect("*");
			System.out.println("Card detected:" + card);
			return card;
		}

		return null;

	}

	private static ResponseAPDU selectApplet(String aid) throws CardException {
		CommandAPDU select = new CommandAPDU(AIDUtil.select(aid));
		return channel.transmit(select);
	}

	private static String sayHello() throws CardException {
		CommandAPDU hello = new CommandAPDU(0x00, 0x10, 0x00, 0x00);
		ResponseAPDU rsp = channel.transmit(hello);

		if (rsp.getSW() == 0x9000) {
			return new String(rsp.getData());
		}
		else {
			return "ERROR: Response Status = " + Integer.toHexString(rsp.getSW());
		}
	}

	private static int getAccessCount() throws CardException {
		CommandAPDU getCount = new CommandAPDU(0x00, 0x20, 0x00, 0x00);
		ResponseAPDU rsp = channel.transmit(getCount);

		if (rsp.getSW() == 0x9000) {
			return Integer.parseInt(ByteUtil.hexString(rsp.getData()), 16);
		}
		else {
			return -1;
		}
	}

	private static boolean setAccessCount(short newCount) throws CardException {
		byte[] dataBuffer = ByteBuffer.allocate(2).putShort(newCount).array();
		CommandAPDU setCount = new CommandAPDU(0x00, 0x21, 0x00, 0x00, dataBuffer);

		ResponseAPDU rsp = channel.transmit(setCount);

		if (rsp.getSW() != 0x9000) {
			System.out.println("ERROR: Response status = " + Integer.toHexString(rsp.getSW()));
			return false;
		}

		return true;
	}
}
