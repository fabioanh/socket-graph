package com.collibra.fabio.protocol;

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
