package com.collibra.fabio;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.collibra.fabio.graph.Graph;
import com.collibra.fabio.protocol.Event;
import com.collibra.fabio.protocol.State;

public class Session {

	private static final String NAME_SEQUENCE = "[a-zA-Z0-9-]*";

	private static final Pattern ADD_EDGE_PATTERN = Pattern
			.compile("ADD EDGE (" + NAME_SEQUENCE + ") (" + NAME_SEQUENCE + ") ([0-9]*)");
	private static final Pattern ADD_NODE_PATTERN = Pattern.compile("ADD NODE (" + NAME_SEQUENCE + ")");
	private static final Pattern BYE_PATTERN = Pattern.compile("BYE MATE!");
	private static final Pattern CLOSER_THAN_PATTERN = Pattern.compile("CLOSER THAN ([0-9]*) (" + NAME_SEQUENCE + ")");
	private static final Pattern HI_PATTERN = Pattern.compile("HI, I'M (" + NAME_SEQUENCE + ")");
	private static final Pattern REMOVE_EDGE_PATTERN = Pattern
			.compile("REMOVE EDGE (" + NAME_SEQUENCE + ") (" + NAME_SEQUENCE + ")");
	private static final Pattern REMOVE_NODE_PATTERN = Pattern.compile("REMOVE NODE (" + NAME_SEQUENCE + ")");
	private static final Pattern SHORTEST_PATH_PATTERN = Pattern
			.compile("SHORTEST PATH (" + NAME_SEQUENCE + ") (" + NAME_SEQUENCE + ")");

	private final UUID uuid;

	// Timeout in milliseconds
	private Integer timeout = 30000;

	// Activity time values
	private Long lastActiveTime;
	private Long initialTime;

	private String name;

	private String currentMessage;

	private State currentState;

	private Graph graph;

	public Session(Graph graph) {
		this.uuid = UUID.randomUUID();
		this.initialTime = System.currentTimeMillis();
		this.lastActiveTime = this.initialTime;
		this.currentState = State.WAITING_TO_START;
		this.graph = graph;
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
			Matcher matcher;
			switch (currentState) {
			case FINISHING:
				break;
			case ITEM_ADDED:
			case ITEM_ALREADY_FOUND:
			case ITEM_NOT_FOUND:
			case ITEM_REMOVED:
			case GREETED:
				matcher = ADD_NODE_PATTERN.matcher(input);
				if (matcher.matches()) {
					recognizedInput = true;
					String nodeName = matcher.group(1);
					Boolean successful = graph.addNode(nodeName);
					this.currentState = Event.ADD_ITEM.dispatch(this.currentState, successful, true);
					this.currentMessage = this.currentState.getMessage("NODE");
				} else {
					matcher = REMOVE_NODE_PATTERN.matcher(input);
					if (matcher.matches()) {
						recognizedInput = true;
						String nodeName = matcher.group(1);
						Boolean successful = graph.removeNode(nodeName);
						this.currentState = Event.REMOVE_ITEM.dispatch(this.currentState, successful);
						this.currentMessage = this.currentState.getMessage("NODE");
					} else {
						matcher = ADD_EDGE_PATTERN.matcher(input);
						if (matcher.matches()) {
							recognizedInput = true;
							String originNode = matcher.group(1);
							String destinyNode = matcher.group(2);
							Integer weight = Integer.valueOf(matcher.group(3));
							Boolean successful = graph.addEdge(originNode, destinyNode, weight);
							this.currentState = Event.ADD_ITEM.dispatch(this.currentState, successful, false);
							if (successful) {
								this.currentMessage = this.currentState.getMessage("EDGE");
							} else {
								this.currentMessage = this.currentState.getMessage("NODE");
							}
						} else {
							matcher = REMOVE_EDGE_PATTERN.matcher(input);
							if (matcher.matches()) {
								recognizedInput = true;
								String originNode = matcher.group(1);
								String destinyNode = matcher.group(2);
								Boolean successful = graph.removeEdge(originNode, destinyNode);
								this.currentState = Event.REMOVE_ITEM.dispatch(this.currentState, successful);
								if (successful) {
									this.currentMessage = this.currentState.getMessage("EDGE");
								} else {
									this.currentMessage = this.currentState.getMessage("NODE");
								}
							} else {
								matcher = SHORTEST_PATH_PATTERN.matcher(input);
								if (matcher.matches()) {
									recognizedInput = true;
									String originNode = matcher.group(1);
									String destinyNode = matcher.group(2);
									Integer weight = graph.shortestPath(originNode, destinyNode);
									this.currentState = Event.COMPUTE_VALUE.dispatch(this.currentState, weight != null);
									if (weight != null) {
										this.currentMessage = this.currentState.getMessage(weight);
									} else {
										this.currentMessage = this.currentState.getMessage("NODE");
									}
								} else {
									matcher = CLOSER_THAN_PATTERN.matcher(input);
									if (matcher.matches()) {
										recognizedInput = true;
										Integer weight = Integer.valueOf(matcher.group(1));
										String node = matcher.group(2);
										String sequence = graph.closerThan(weight, node);
										this.currentState = Event.COMPUTE_VALUE.dispatch(this.currentState,
												sequence != null);
										if (sequence != null) {
											this.currentMessage = this.currentState.getMessage(sequence);
										} else {
											this.currentMessage = this.currentState.getMessage("NODE");
										}
									}
								}
							}
						}
					}

				}
				break;
			case WAITING_TO_START:
				matcher = HI_PATTERN.matcher(input);
				if (matcher.matches()) {
					recognizedInput = true;
					this.name = matcher.group(1);
					this.currentState = Event.INITIAL_GREETING.dispatch(this.currentState);
					this.currentMessage = this.currentState.getMessage(name);
				}
				break;
			default:
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
