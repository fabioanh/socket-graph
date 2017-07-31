package com.collibra.fabio;

import java.util.UUID;

public class Session {
	private final UUID uuid;

	// Timeout in milliseconds
	private Integer timeout;

	// Activity time values
	private Long lastActiveTime;
	private Long initialTime;

	private String name;

	public Session() {
		this.uuid = UUID.randomUUID();
		this.initialTime = System.currentTimeMillis();
		this.lastActiveTime = this.initialTime;
	}

	public void disconnect() {
	}

	/**
	 * Checks if the current object is still alive (not timed out)
	 * 
	 * @return
	 */
	public Boolean isAlive() {
		return lastActiveTime + timeout < System.currentTimeMillis();
	}

	private void updateLastActiveTime() {
		this.lastActiveTime = System.currentTimeMillis();
	}

	public String getUuid() {
		return this.uuid.toString();
	}

	public String getGoodbyeMessage() {
		return "BYE " + this.name + ", WE SPOKE FOR " + (System.currentTimeMillis() - this.initialTime) + "MS";
	}

	public String getHiMessage() {
		this.updateLastActiveTime();
		return "HI, I'M " + this.uuid;
	}

}
