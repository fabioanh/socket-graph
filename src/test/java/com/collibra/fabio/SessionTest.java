package com.collibra.fabio;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import com.collibra.fabio.graph.Graph;
import com.collibra.fabio.protocol.State;

/**
 * Class created to quickly check some functionalities of the {@link Session} class.
 * @author fabio
 *
 */
public class SessionTest {

	@Test
	public void processInput() {
		Session session = new Session(new Graph());
		session.processInput("HI, I'M FABIO");
		session.processInput("REMOVE NODE Phase2-Node-95");
		assertEquals("Wrong state after remove operation", State.ITEM_NOT_FOUND, session.getCurrentState());
	}
}
