package com.stellar.relay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.fazecast.jSerialComm.SerialPort;

public class Controller {
	private final Player player;

	private final Sprite sprite;
	private static SerialPort picoPort;

	private byte[] buffer = new byte[14];

	private boolean button1Pressed = false;
	private boolean button2Pressed = false;

	private double ax = 0;
	private double ay = 0;

	private Planet activePlanet = null;

	public Controller(float x, float y, Player player) {
		this.player = player;

		sprite = new Sprite(new Texture("PLACEHOLDER_Spaceship.png"));
		sprite.setSize(40, 40);
		sprite.setOrigin(sprite.getWidth() / 2 - 10, sprite.getHeight() / 2);
		sprite.setPosition(x, y);

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

	public void input() {
		float speed = 200f * Gdx.graphics.getDeltaTime();
		double rotSpeed = 2.0f * Gdx.graphics.getDeltaTime();
		double rotDecay = 0.95; // I dont understand why this fixes the rotation

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

		if (player == Player.LEFT && Gdx.input.isKeyPressed(Input.Keys.W)
				|| player == Player.RIGHT && Gdx.input.isKeyPressed(Input.Keys.UP)
				|| inputs[0]) {
			sprite.translateY(speed);

			ay = Math.min(ay + rotSpeed, 1);
			ax = ax * rotDecay;
		}
		if (player == Player.LEFT && Gdx.input.isKeyPressed(Input.Keys.S)
				|| player == Player.RIGHT && Gdx.input.isKeyPressed(Input.Keys.DOWN)
				|| inputs[1]) {
			sprite.translateY(-speed);
			ay = Math.max(ay - rotSpeed, -1);
			ax = ax * rotDecay;
		}
		if (player == Player.LEFT && Gdx.input.isKeyPressed(Input.Keys.A)
				|| player == Player.RIGHT && Gdx.input.isKeyPressed(Input.Keys.LEFT)
				|| inputs[2]) {
			sprite.translateX(-speed);
			ax = Math.max(ax - rotSpeed, -1);
			ay = ay * rotDecay;
		}
		if (player == Player.LEFT && Gdx.input.isKeyPressed(Input.Keys.D)
				|| player == Player.RIGHT && Gdx.input.isKeyPressed(Input.Keys.RIGHT)
				|| inputs[3]) {
			sprite.translateX(speed);
			ax = Math.min(ax + rotSpeed, 1);
			ay = ay * rotDecay;
		}

		sprite.setRotation((float) Math.toDegrees(Math.atan2(ay, ax)));

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

		button1Pressed =
				player == Player.LEFT && Gdx.input.isKeyPressed(Input.Keys.Q)
						|| player == Player.RIGHT && Gdx.input.isKeyPressed(Input.Keys.COMMA)
						|| inputs[4];

		button2Pressed =
				player == Player.LEFT && Gdx.input.isKeyPressed(Input.Keys.E)
						|| player == Player.RIGHT && Gdx.input.isKeyPressed(Input.Keys.PERIOD)
						|| inputs[5];

		if (activePlanet != null
				&& activePlanet.getState() == Planet.State.SELECTED
				&& button2Pressed) {
			activePlanet.setState(Planet.State.NONE);
		}
	}

	public boolean isButton1Pressed() {
		return button1Pressed;
	}

	public boolean isButton2Pressed() {
		return button2Pressed;
	}

	public void draw(Batch batch) {
		float cx_sprite = sprite.getX() + sprite.getWidth() / 2;
		float cy_sprite = sprite.getY() + sprite.getHeight() / 2;

		Planet planet = Planet.getClosestPlanet(cx_sprite, cy_sprite);

		sprite.draw(batch);
		if (planet == null
				|| (activePlanet != null && activePlanet.getState() == Planet.State.SELECTED)) {
			if (activePlanet != null && activePlanet.getState() == Planet.State.HOVERED) {
				activePlanet.setState(Planet.State.NONE);
				activePlanet = null;
			}
			return;
		}

		activePlanet = planet;

		if (activePlanet.getState() == Planet.State.NONE) {
			activePlanet.setState(Planet.State.HOVERED);
		} else if (activePlanet.getState() == Planet.State.HOVERED && button1Pressed) {
			activePlanet.setState(Planet.State.SELECTED);
		}
	}
}
