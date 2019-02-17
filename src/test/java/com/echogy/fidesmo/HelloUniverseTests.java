package com.echogy.fidesmo;

import com.licel.jcardsim.smartcardio.CardSimulator;
import com.licel.jcardsim.utils.AIDUtil;
import javacard.framework.AID;

import javax.smartcardio.*;
import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;



class HelloUniverseTests {

	private CardSimulator simulator;
	private AID appletAID = AIDUtil.create("F000000001");

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	private static final CommandAPDU NOP = new CommandAPDU(0x00, 0x00, 0x00, 0x00);
	private static final CommandAPDU HELLO = new CommandAPDU(0x00, 0x10, 0x00, 0x00);
	private static final CommandAPDU GET_COUNT = new CommandAPDU(0x00, 0x20, 0x00, 0x00);

	@BeforeEach
	void setup() {
		// 1. Create simulator
		simulator = new CardSimulator();

		// 2. Install applet
		simulator.installApplet(appletAID, JavaCardHelloUniverse.class);

		// 3. Select applet
		simulator.selectApplet(appletAID);
	}


	@Test
	@DisplayName("JUnit 5 framework should not throw an error")
	void alwaysPassingHamcrest() {
		Integer[] x = {};
		assertThat(x, is(emptyArray()));
	}


	@Test
	@DisplayName("NOP command should successfully return no data")
	void nopCommand() {
		ResponseAPDU rsp = simulator.transmitCommand(NOP);

		assertSuccessStatus(rsp.getSW());
		assertThat(rsp.getData().length, is(0));
	}

	@Test
	@DisplayName("Hello command should return 'Hello Universe!'")
	void helloCommand() {
		ResponseAPDU rsp = simulator.transmitCommand(HELLO);

		String responseText = new String(rsp.getData());

		assertSuccessStatus(rsp.getSW());
		assertThat(responseText, is(equalTo("Hello Universe!")));
	}

	@Test
	@DisplayName("Card access counter should return 1 after first get count command")
	void getCountOne() {
		ResponseAPDU rsp = simulator.transmitCommand(GET_COUNT);

		assertSuccessStatus(rsp.getSW());

		String hex = bytesToHex(rsp.getData());
		int count = hexToInt(hex);

		assertThat(count, is(1));
	}

	@Test
	@DisplayName("Card access counter should return 100 after the hundredth command")
	void getCount100() {
		ResponseAPDU rsp;

		int helloCommandCount = 99;
		for (int i = 0; i < helloCommandCount; i++) {
			rsp = simulator.transmitCommand(HELLO);
			assertSuccessStatus(rsp.getSW());
		}
		rsp = simulator.transmitCommand(GET_COUNT);

		int count = hexToInt( bytesToHex(rsp.getData()) );

		assertThat(count, is(100));
	}

	@Test
	@DisplayName("Card access counter should never be more than the max value of a short")
	void maxOutCount() {
		int max = Short.MAX_VALUE;

		ResponseAPDU rsp;

		int helloCommandCount = max + 100;
		for (int i = 0; i < helloCommandCount; i++) {
			rsp = simulator.transmitCommand(HELLO);
			assertSuccessStatus(rsp.getSW());
		}

		rsp = simulator.transmitCommand(GET_COUNT);

		int count = hexToInt(bytesToHex(rsp.getData()));

		assertThat(count, is( max ));
	}

	@Test
	@DisplayName("Setting access count to 50 then getting the count should return 51")
	void setAccessCount() {
		short updatedCount = 50;
		byte[] dataBuffer = ByteBuffer.allocate(2).putShort(updatedCount).array();

		CommandAPDU setAccessCount = new CommandAPDU(0x00, 0x21, 0x00, 0x00, dataBuffer);

		ResponseAPDU rsp = simulator.transmitCommand(setAccessCount);

		assertSuccessStatus(rsp.getSW());

		rsp = simulator.transmitCommand(GET_COUNT);
		int accessCount = hexToInt( bytesToHex(rsp.getData()) );

		assertThat(accessCount, is( updatedCount + 1 ));

	}



	void assertSuccessStatus(int responseSW) {
		assertThat(responseSW, is(0x9000));
	}


	static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	static int hexToInt(String hex) {
		return Integer.parseInt(hex, 16);
	}
}
