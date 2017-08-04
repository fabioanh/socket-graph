package com.collibra.fabio;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.collibra.fabio.graph.Graph;
import com.collibra.fabio.protocol.Event;
import com.collibra.fabio.protocol.State;

/**
 * Class containing all the logic for session conversations with the clients and
 * mapping/decision making for the input provided by the clients
 * 
 * @author fabio
 *
 */
public class Session {
	private static final Logger LOGGER = LogManager.getLogger(Session.class);
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
			return isAlive;
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

	/**
	 * Logic related to the session termination
	 */
	private void killSession() {
		this.currentState = Event.GOODBYE.dispatch(currentState);
		this.currentMessage = this.currentState.getMessage(this.name, (System.currentTimeMillis() - this.initialTime));

	}

	/**
	 * Updates the last active time to the current time. Method should be called
	 * whenever the client interacts with the server in the current session
	 */
	private void updateLastActiveTime() {
		this.lastActiveTime = System.currentTimeMillis();
	}

	/**
	 * Get the welcome message to be sent to the client as initial greeting
	 * 
	 * @return
	 */
	public String getHiMessage() {
		if (State.WAITING_TO_START.equals(this.currentState)) {
			this.updateLastActiveTime();
			return this.currentState.getMessage(this.uuid);
		}
		LOGGER.error("Can't get a hello message if not in initial state");
		throw new IllegalStateException("Can't get a hello message if not in initial state");
	}

	/**
	 * Method containing all the parsing for the input send by the user. Uses
	 * regular expressions to extract information based on the expected
	 * structure of messages established in the communication protocol.
	 * 
	 * Messages are processed according to the session state managed with the
	 * {@link State} and {@link Event} enumerations. An {@link Event} is
	 * dispatched every time an input is received from the client. These Events
	 * lead to the session to be in the corresponding {@link State} according to
	 * the input processing.
	 * 
	 * @param input
	 */
	public void processInput(String input) {
		this.updateLastActiveTime();
		Boolean recognizedInput = false;

		if (BYE_PATTERN.matcher(input).matches()) {
			LOGGER.info("Processing Goodbye Request");
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
			case VALUE_COMPUTED:
			case GREETED:
				// TODO: Check if matching checks can be re-written in a more
				// elegant way. Check if a commander would be a good idea to be
				// implemented here
				matcher = ADD_NODE_PATTERN.matcher(input);
				if (matcher.matches()) {
					LOGGER.info("Processing Add Node Request");
					recognizedInput = true;
					String nodeName = matcher.group(1);
					Boolean successful = graph.addNode(nodeName);
					this.currentState = Event.ADD_ITEM.dispatch(this.currentState, successful, true);
					this.currentMessage = this.currentState.getMessage("NODE");
				} else {
					matcher = REMOVE_NODE_PATTERN.matcher(input);
					LOGGER.info("Processing Node Removal Request");
					if (matcher.matches()) {
						recognizedInput = true;
						String nodeName = matcher.group(1);
						Boolean successful = graph.removeNode(nodeName);
						this.currentState = Event.REMOVE_ITEM.dispatch(this.currentState, successful);
						this.currentMessage = this.currentState.getMessage("NODE");
					} else {
						matcher = ADD_EDGE_PATTERN.matcher(input);
						LOGGER.info("Processing Add Edge Request");
						if (matcher.matches()) {
							recognizedInput = true;
							String originNode = matcher.group(1);
							String destinationNode = matcher.group(2);
							Integer weight = Integer.valueOf(matcher.group(3));
							Boolean successful = graph.addEdge(originNode, destinationNode, weight);
							this.currentState = Event.ADD_ITEM.dispatch(this.currentState, successful, false);
							if (successful) {
								this.currentMessage = this.currentState.getMessage("EDGE");
							} else {
								this.currentMessage = this.currentState.getMessage("NODE");
							}
						} else {
							LOGGER.info("Processing Edge Removal Request");
							matcher = REMOVE_EDGE_PATTERN.matcher(input);
							if (matcher.matches()) {
								recognizedInput = true;
								String originNode = matcher.group(1);
								String destinationNode = matcher.group(2);
								Boolean successful = graph.removeEdge(originNode, destinationNode);
								this.currentState = Event.REMOVE_ITEM.dispatch(this.currentState, successful);
								if (successful) {
									this.currentMessage = this.currentState.getMessage("EDGE");
								} else {
									this.currentMessage = this.currentState.getMessage("NODE");
								}
							} else {
								matcher = SHORTEST_PATH_PATTERN.matcher(input);
								LOGGER.info("Processing Shortest Path Request");
								if (matcher.matches()) {
									recognizedInput = true;
									String originNode = matcher.group(1);
									String destinationNode = matcher.group(2);
									Integer weight = graph.shortestPath(originNode, destinationNode);
									this.currentState = Event.COMPUTE_VALUE.dispatch(this.currentState, weight != null);
									if (weight != null) {
										this.currentMessage = this.currentState.getMessage(weight);
									} else {
										this.currentMessage = this.currentState.getMessage("NODE");
									}
								} else {
									LOGGER.info("Processing Closer Than Request");
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
				LOGGER.info("Processing Hello Request");
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
			LOGGER.warn("Given option " + input + " was not recognized");
			this.currentMessage = "SORRY, I DIDN'T UNDERSTAND THAT";
		}
	}

	// --- GETTERS AND SETTERS ---

	public State getCurrentState() {
		return this.currentState;
	}

	public String getCurrentMessage() {
		return this.currentMessage;
	}

	public String getUuid() {
		return this.uuid.toString();
	}

}
