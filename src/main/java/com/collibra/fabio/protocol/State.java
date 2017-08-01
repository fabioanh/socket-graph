package com.collibra.fabio.protocol;

/**
 * 
 * @author fabio
 *
 *         Inspired by:
 *         https://stackoverflow.com/questions/25581176/java-enum-based-state-machine-fsm-passing-in-events
 */
public enum State {
	FINISHING("BYE %s, WE SPOKE FOR %d MS"), GREETED("HI %s"),

	WAITING_TO_START("HI, I'M %s") {
		@Override
		public State processInitialGreeting() {
			return State.GREETED;
		}
	};

	private String message;

	private State(String message) {
		this.message = message;
	}

	public State processInitialGreeting() {
		return this;
	}

	public State processGoodbye() {
		return State.FINISHING;
	}

	public String getMessage(Object... params) {
		return String.format(message, params);
	}

}
