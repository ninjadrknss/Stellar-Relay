package com.stellar.relay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.fazecast.jSerialComm.SerialPort;

public class Controller {
	public static final float MAX_INTERACTION_DISTANCE = 75;

	private final Player player;

	private final Sprite sprite;
	private static SerialPort picoPort;

	private byte[] buffer = new byte[14];

	private boolean button1Pressed = false;
	private boolean button1Prev = false;
	private boolean button2Pressed = false;
	private boolean button2Prev = false;

	public boolean active = false;

	private float velocity = 0;
	private float angle = 0;
	private float angleVel = 0;

	private Path path = null;

	public Pathable lastPathable = null;

	public Controller(Player player) {
		this.player = player;

		sprite = new Sprite(new Texture("PLACEHOLDER_Spaceship.png"));
		sprite.setSize(70, 70);
		sprite.setOrigin(sprite.getWidth() / 2 - 10, sprite.getHeight() / 2);
		sprite.setPosition(
				Gdx.graphics.getWidth() / 2f + 400 * (player == Player.LEFT ? -1 : 1),
				Gdx.graphics.getHeight() / 2f);

		if (picoPort == null) {
			SerialPort[] ports = SerialPort.getCommPorts();

			if (ports[ports.length - 1].getSystemPortName().contains("usb")
					|| ports[ports.length - 1].getSystemPortName().contains("com")) {
				picoPort = ports[ports.length - 1];
				picoPort.setBaudRate(9600);
				picoPort.setNumDataBits(8);
				picoPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
				picoPort.setParity(SerialPort.NO_PARITY);

				picoPort.openPort();
				System.out.println("Pico found on port: " + picoPort.getSystemPortName());
			} else {
				System.out.println("Pico not found. Available ports:");
				for (int i = 0; i < ports.length; i++) {
					System.out.println(i + ": " + ports[i].getSystemPortName());
				}
			}
		}
	}

	public void input(boolean movementEnabled) {
		float speed =
				300f
						* Gdx.graphics.getDeltaTime()
						* (Main.gameState == Main.GameState.DIFFICULTY_SELECT ? 8f : 1f);
		float accelerationTime = 1f;
		float springStrength = 50f;
		float damping = 0.84f;

		boolean[] inputs = new boolean[6]; // Up, Down, Left, Right, button1, button2

		if (picoPort != null && picoPort.isOpen()) {
			int numRead = picoPort.readBytes(buffer, buffer.length);

			if (numRead > 0) {
				// Read the first line of data
				String data = new String(buffer, 0, numRead).trim().split("\n")[0];
				if (data.charAt(0) == 'P') {
					for (int i = 0; i < 6 && i < data.length() - 1; i++) {
						// The data format is expected to be "P:xxxxyyaaaabb" where each variable is '0' or '1'
						// for each button,
						// and each set of 6 buttons corresponds to a player, 4 joystick switches, 2 buttons.
						inputs[i] = data.charAt(i + 2 + (player == Player.LEFT ? 0 : 6)) == '1';
					}
				}
				if (Main.DEBUG) System.out.println("Received data: " + data);
				buffer = new byte[256];
			}
		}

		int velX = 0;
		int velY = 0;

		if (player == Player.LEFT && Gdx.input.isKeyPressed(Input.Keys.W)
				|| player == Player.RIGHT && Gdx.input.isKeyPressed(Input.Keys.O)
				|| inputs[0]) {
			velY++;
		}
		if (player == Player.LEFT && Gdx.input.isKeyPressed(Input.Keys.S)
				|| player == Player.RIGHT && Gdx.input.isKeyPressed(Input.Keys.L)
				|| inputs[1]) {
			velY--;
		}
		if (player == Player.LEFT && Gdx.input.isKeyPressed(Input.Keys.A)
				|| player == Player.RIGHT && Gdx.input.isKeyPressed(Input.Keys.K)
				|| inputs[2]) {
			velX--;
		}
		if (player == Player.LEFT && Gdx.input.isKeyPressed(Input.Keys.D)
				|| player == Player.RIGHT && Gdx.input.isKeyPressed(Input.Keys.SEMICOLON)
				|| inputs[3]) {
			velX++;
		}

		if (!active && (velX != 0 || velY != 0)) {
			active = true;
		}

		if (Main.DEBUG
				&& Gdx.input.isKeyJustPressed(Input.Keys.R)
				&& Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
			Main.restart();
			return;
		}

		float targetAngle = (float) Math.atan2(velY, velX);

		float angleDiff = targetAngle - angle;
		angleDiff = (float) Math.atan2(Math.sin(angleDiff), Math.cos(angleDiff));

		if (velX != 0 || velY != 0) {
			angleVel += angleDiff * springStrength * Gdx.graphics.getDeltaTime() / damping;
			velocity =
					Math.min(
							velocity + speed / accelerationTime, speed); // accelerate to max speed over 2 seconds
		} else {
			targetAngle = angle;
			velocity *= 0.92f;
		}
		if (velocity < 0.01f) velocity = 0; // prevent drifting when very slow

		angleVel *= damping;
		angle += angleVel * Gdx.graphics.getDeltaTime();
		angle = (float) Math.atan2(Math.sin(angle), Math.cos(angle));

		if (Math.abs(angleDiff) < 0.01f && Math.abs(angleVel) < 0.01f) { // close enough
			angle = targetAngle;
			angleVel = 0.0f;
		}

		if (movementEnabled) {
			sprite.translateX(
					(float) (Math.cos(targetAngle + angleVel * Gdx.graphics.getDeltaTime()) * velocity));
			sprite.translateY(
					(float) (Math.sin(targetAngle + angleVel * Gdx.graphics.getDeltaTime()) * velocity));

			sprite.setRotation(angle * MathUtils.radiansToDegrees);
		}

		if (sprite.getX() < 0) {
			sprite.setX(0);
		} else if (sprite.getX() + sprite.getWidth() > Gdx.graphics.getWidth()) {
			sprite.setX(Gdx.graphics.getWidth() - sprite.getWidth());
		}

		if (sprite.getY() < 0) {
			sprite.setY(0);
		} else if (sprite.getY() + sprite.getHeight() > Gdx.graphics.getHeight()) {
			sprite.setY(Gdx.graphics.getHeight() - sprite.getHeight());
		}

		boolean newButton1Pressed =
				player == Player.LEFT && Gdx.input.isKeyPressed(Input.Keys.Q)
						|| player == Player.RIGHT && Gdx.input.isKeyPressed(Input.Keys.I)
						|| inputs[4];

		boolean newButton2Pressed =
				player == Player.LEFT && Gdx.input.isKeyPressed(Input.Keys.E)
						|| player == Player.RIGHT && Gdx.input.isKeyPressed(Input.Keys.P)
						|| inputs[5];

		if (!active && (newButton1Pressed || newButton2Pressed)) {
			active = true;
		}

		if (newButton1Pressed && !button1Prev) {
			button1Pressed = true;
			button1Prev = true;
		} else if (!newButton1Pressed) {
			button1Pressed = false;
			button1Prev = false;
		}

		if (newButton2Pressed && !button2Prev) {
			button2Pressed = true;
			button2Prev = true;
		} else if (!newButton2Pressed) {
			button2Pressed = false;
			button2Prev = false;
		}

		if (movementEnabled && button2Pressed && path != null) { // remove the last pathable
			boolean drop = path.remove();
			if (drop) {
				if (Main.DEBUG) System.out.println("Dropped message");
				path = null;
			} else if (Main.DEBUG) {
				if (Main.DEBUG) System.out.println("Removed last pathable from message");
			}
		}

		if (movementEnabled && button1Pressed && path != null && path.isComplete()) {
			path.drop();
			path.message().start();
			if (lastPathable != null) lastPathable.setState(Planet.State.NONE);
			lastPathable = null;
			path = null;
			button1Pressed = false;
		}

		button2Pressed = false;
	}

	public boolean isButton1Pressed() {
		return button1Prev;
	}

	public boolean isButton2Pressed() {
		return button2Prev;
	}

	public void draw(Batch batch) {
		Pathable pathable = Pathable.getClosestPathable(getCX(), getCY());

		if (lastPathable != null
				&& (lastPathable != pathable
						|| Math.hypot(lastPathable.getCX() - getCX(), lastPathable.getCY() - getCY())
								> MAX_INTERACTION_DISTANCE)
				&& lastPathable.getState() != Planet.State.SELECTED) {
			lastPathable.setState(Planet.State.NONE);
			lastPathable = null;
		}

		if (active) sprite.draw(batch);

		if (pathable == null
				|| path == null
						&& (pathable instanceof Planet p && p.message == null || pathable instanceof Satellite)
				|| ((float) Math.hypot(pathable.getCX() - getCX(), pathable.getCY() - getCY())
						> MAX_INTERACTION_DISTANCE)) { // Not near any pathables
			return;
		}

		lastPathable = pathable;
		if (lastPathable.getState() != Pathable.State.SELECTED
				&& (path == null || path.valid(lastPathable))) {
			lastPathable.setState(Pathable.State.HOVERED);
		}

		if (button1Pressed
				&& path == null
				&& lastPathable instanceof Planet p) { // Pick up message, TODO: disable
			path = p.message.path;
			path.pickup(this);
			if (Main.DEBUG) System.out.println("Picked up message");
		}

		if (button1Pressed && path != null && path.valid(pathable)) { // Add Satellite to path
			path.add(pathable);
			if (Main.DEBUG) System.out.println("Added satellite to message path");
		}

		button1Pressed = false;
	}

	public float getCX() {
		return sprite.getX() + sprite.getWidth() / 2;
	}

	public float getCY() {
		return sprite.getY() + sprite.getHeight() / 2;
	}
}
