package com.stellar.relay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Message {
	public static final float MAX_TIMEOUT =
			30 / Main.difficulty.multiplier; // in seconds, TODO tweak this value for better gameplay
	public static final float speed = 0.2f; // Progress per second

	enum State {
		AWAITING,
		IN_TRANSIT,
		DELIVERED
	}

	private final Planet source;
	private final Planet destination;
	public final Path path;

	private float progress; // 0 to 1
	private float life;

	private State state = State.AWAITING;

	public static final ArrayList<Message> messages = new ArrayList<>();

	private final Sprite awaitingSprite;
	private final Sprite inTransitSprite;

	public Message(Planet source, Planet destination) {
		this.source = source;
		this.destination = destination;
		this.progress = 0;
		this.life = MAX_TIMEOUT;

		path = new Path(this, source, destination);
		path.add(source);

		source.message = this; // Link the message to the source planet

		awaitingSprite = new Sprite(new Texture("PLACEHOLDER_planetMessage.png"));
		awaitingSprite.setPosition(
				source.getCX() + 20, source.getCY() + 20); // Position on top right of the source planet
		awaitingSprite.setSize(40, 40);

		inTransitSprite = new Sprite(new Texture("PLACEHOLDER_sendingMessage.png"));
		inTransitSprite.setPosition(-100, -100); // Start off-screen
		inTransitSprite.setSize(40, 40);
		inTransitSprite.setOrigin(inTransitSprite.getWidth() / 2, inTransitSprite.getHeight() / 2);

		messages.add(this);
	}

	private void draw(Batch batch, ShapeRenderer shapeRenderer) {
		if (state == State.AWAITING) {
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			shapeRenderer.setColor(Color.WHITE);
			if (life < MAX_TIMEOUT / 4) {
				float v = (float) (Math.sin(life * 7) * 0.25f + 0.75f);
				shapeRenderer.setColor(1, v, v, 1);
			}
			shapeRenderer.arc(
					awaitingSprite.getX() + awaitingSprite.getWidth() * 0.5f,
					awaitingSprite.getY() + awaitingSprite.getHeight() * 0.52f,
					30,
					90,
					Math.max(life / MAX_TIMEOUT * 360, 0));
			shapeRenderer.end();

			batch.begin();
			awaitingSprite.draw(batch);
			batch.end();

			life -= Gdx.graphics.getDeltaTime();

			if (life <= 0) Main.initGameOver();
		} else if (state == State.IN_TRANSIT) {
			progress += speed * Gdx.graphics.getDeltaTime();

			if (progress >= 1) {
				progress = 1;
				state = State.DELIVERED;
				for (Satellite satellite : Satellite.satellites) {
					if (satellite.path == path) {
						satellite.path = null; // Clear any paths that are currently routing this message
					}
				}
				path.delete();
				GUI.score += 100; // Award points for successful delivery
				if (Main.DEBUG) System.out.println("Message delivered to " + destination);
				// TODO: add some visual effect for delivery, like a burst or confetti?
			}

			Vector2 position = path.positionAlongPath(progress);
			inTransitSprite.setPosition(
					position.x - inTransitSprite.getOriginX(), position.y - inTransitSprite.getOriginY());

			batch.begin();
			inTransitSprite.draw(batch);
			batch.end();
		} else {
			System.out.println("Should be deleted lol");
		}
	}

	public void start() {
		if (state == State.AWAITING && path.isComplete()) {
			state = State.IN_TRANSIT;
			source.message = null; // Clear the message from the source planet
			inTransitSprite.setPosition(source.getCX(), source.getCY()); // Start at the source planet
			progress = 0;
		}
	}

	public static void drawAll(Batch batch, ShapeRenderer shapeRenderer) {
		messages.forEach(message -> message.draw(batch, shapeRenderer));

		messages.removeAll(
				messages.stream().filter(message -> message.state == State.DELIVERED).toList());
	}

	private static boolean possiblePathExists(Planet source, Planet destination) {
		int size = Satellite.satellites.size();
		boolean[][] validEdge =
				new boolean[size + 2][size + 2]; // +2 for source and destination planets

		for (int i = 0; i < size; i++) {
			Satellite satellite = Satellite.satellites.get(i);
			if (satellite.inRange(source)) {
				validEdge[size][i] = true;
				validEdge[i][size] = true;
			}
			if (satellite.inRange(destination)) {
				validEdge[size + 1][i] = true;
				validEdge[i][size + 1] = true;
			}

			for (int j = i + 1; j < size; j++) {
				if (satellite.inRange(Satellite.satellites.get(j))) {
					validEdge[i][j] = true;
					validEdge[j][i] = true;
				}
			}
		}

		boolean[] visited = new boolean[size + 2];

		Queue<Integer> q = new LinkedList<>();
		visited[size] = true;
		q.add(size);

		while (!q.isEmpty()) {
			int curr = q.poll();

			for (int x = 0; x < size + 2; x++) {
				if (validEdge[curr][x] && !visited[x]) {
					visited[x] = true;
					q.add(x);
				}
			}
		}

		System.out.println("Possible: " + visited[size + 1]);

		return visited[size + 1]; // Check if the destination planet is reachable
	}

	public static void spawnNewMessage() {
		Planet source, destination;

		int tries = 0;
		do {
			source = Planet.planets.get((int) (Math.random() * Planet.planets.size()));
			while (source.message != null) { // Ensure the source planet doesn't already have a message
				source = Planet.planets.get((int) (Math.random() * Planet.planets.size()));
			}

			destination = Planet.planets.get((int) (Math.random() * Planet.planets.size()));

			while (destination == source) { // Ensure the destination is different from the source
				destination = Planet.planets.get((int) (Math.random() * Planet.planets.size()));
			}
			tries++;
			if (tries > 1000) {
				System.out.println(
						"Failed to find valid source and destination planets for new message after 1000 tries, giving up.");

				if (GUI.score == 0) {
					Main.restart(); // Restart the game if we can't find a valid message to spawn at the
					// beginning
					System.out.println("Restarting game due to failure to spawn initial message.");
				}
				return;
			}
		} while (!possiblePathExists(source, destination));

		new Message(source, destination);
	}

	public static void clear() {
		messages.clear();
	}
}
