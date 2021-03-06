package com.collibra.fabio.protocol;

/**
 * Set of possible events handled by the application based on the {@link State}s
 * 
 * @author fabio
 * 
 *         Inspired by:
 *         https://stackoverflow.com/questions/25581176/java-enum-based-state-machine-fsm-passing-in-events
 */
public enum Event {
	ADD_ITEM {
		@Override
		public State dispatch(State state, Object... params) {
			return state.processItemAdded((Boolean) params[0], (Boolean) params[1]);
		}
	},
	COMPUTE_VALUE {
		@Override
		public State dispatch(State state, Object... params) {
			return state.processCalculation((Boolean) params[0]);
		}
	},
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
	REMOVE_ITEM {
		@Override
		public State dispatch(State state, Object... params) {
			return state.processItemRemoved((Boolean) params[0]);
		}
	};
	public abstract State dispatch(State state, Object... params);
}
