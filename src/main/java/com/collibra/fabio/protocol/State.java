package com.collibra.fabio.protocol;

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
