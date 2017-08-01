package com.collibra.fabio.protocol;

/**
 * 
 * @author fabio
 * 
 *         Inspired by:
 *         https://stackoverflow.com/questions/25581176/java-enum-based-state-machine-fsm-passing-in-events
 */
public enum Event {
	GOODBYE {
		@Override
		public State dispatch(State state) {
			return state.processGoodbye();
		}
	},
	INITIAL_GREETING {
		@Override
		public State dispatch(State state) {
			return state.processInitialGreeting();
		}
	};
	public abstract State dispatch(State state);
}
