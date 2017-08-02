package com.collibra.fabio.protocol;

/**
 * 
 * @author fabio
 *
 *         Inspired by:
 *         https://stackoverflow.com/questions/25581176/java-enum-based-state-machine-fsm-passing-in-events
 */
public enum State {
	FINISHING("BYE %s, WE SPOKE FOR %d MS"),

	GREETED("HI %s"),

	ITEM_ADDED("%s ADDED"),

	ITEM_ALREADY_FOUND("ERROR: %s ALREADY EXISTS"),

	ITEM_NOT_FOUND("%s NOT FOUND"),

	ITEM_REMOVED("%s REMOVED"),

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

	public State processItemAdded(Boolean success) {
		if (success) {
			return State.ITEM_ADDED;
		}
		return State.ITEM_ALREADY_FOUND;
	}

	public State processItemRemoved(Boolean success) {
		if (success) {
			return State.ITEM_REMOVED;
		}
		return State.ITEM_NOT_FOUND;
	}

	public String getMessage(Object... params) {
		return String.format(message, params);
	}

}
