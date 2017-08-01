package com.collibra.fabio;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.collibra.fabio.protocol.Event;
import com.collibra.fabio.protocol.State;

public class Session {

	private static final Pattern HI_PATTERN = Pattern.compile("HI, I'M ([a-zA-Z0-9 -]*)");
	private static final Pattern BYE_PATTERN = Pattern.compile("BYE MATE!");
	private final UUID uuid;

	// Timeout in milliseconds
	private Integer timeout = 30000;

	// Activity time values
	private Long lastActiveTime;
	private Long initialTime;

	private String name;

	private String currentMessage;

	private State currentState;

	public Session() {
		this.uuid = UUID.randomUUID();
		this.initialTime = System.currentTimeMillis();
		this.lastActiveTime = this.initialTime;
		this.currentState = State.WAITING_TO_START;
	}

	public void disconnect() {
	}

	/**
	 * Checks if the current object is still alive (not timed out) killing the
	 * session if it is not alive
	 * 
	 * @return
	 */
	public Boolean checkAlive() {
		Boolean isAlive = isAlive();
		if (!isAlive) {
			this.killSession();
		}
		return isAlive;
	}

	/**
	 * Checks last activity time for the current session and returns a boolean
	 * value whether the session is alive/active or not
	 * 
	 * @return
	 */
	public Boolean isAlive() {
		return lastActiveTime + timeout > System.currentTimeMillis();
	}

	private void killSession() {
		this.currentState = Event.GOODBYE.dispatch(currentState);
		this.currentMessage = this.currentState.getMessage(this.name, (System.currentTimeMillis() - this.initialTime));

	}

	private void updateLastActiveTime() {
		this.lastActiveTime = System.currentTimeMillis();
	}

	public String getUuid() {
		return this.uuid.toString();
	}

	public String getHiMessage() {
		if (State.WAITING_TO_START.equals(this.currentState)) {
			this.updateLastActiveTime();
			return this.currentState.getMessage(this.uuid);
		}
		throw new IllegalStateException("Can't get a hello message if not in initial state");
	}

	public void processInput(String input) {
		this.updateLastActiveTime();
		Boolean recognizedInput = false;

		if (BYE_PATTERN.matcher(input).matches()) {
			recognizedInput = true;
			this.killSession();
		} else {

			switch (currentState) {
			case FINISHING:
				break;
			case GREETED:
				break;
			case WAITING_TO_START:
				Matcher m = HI_PATTERN.matcher(input);
				if (m.matches()) {
					recognizedInput = true;
					this.name = m.group(1);
					this.currentState = Event.INITIAL_GREETING.dispatch(this.currentState);
					this.currentMessage = this.currentState.getMessage(name);
				}
				break;
			}
		}
		if (!recognizedInput) {
			this.currentMessage = "SORRY, I DIDN'T UNDERSTAND THAT";
		}
	}

	public State getCurrentState() {
		return this.currentState;
	}

	public String getCurrentMessage() {
		return this.currentMessage;
	}

}
