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
		public State dispatch(State state, Object... params) {
			return state.processGoodbye();
		}
	},
	INITIAL_GREETING {
		@Override
		public State dispatch(State state, Object... params) {
			return state.processInitialGreeting();
		}
	},
	ADD_ITEM {
		@Override
		public State dispatch(State state, Object... params) {
			return state.processItemAdded((Boolean) params[0]);
		}
	},
	REMOVE_ITEM {
		@Override
		public State dispatch(State state, Object... params) {
			return state.processItemRemoved((Boolean) params[0]);
		}
	};
	public abstract State dispatch(State state, Object... params);
}
