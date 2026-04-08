package com.stellar.relay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.Random;

public class Planet implements Pathable {
	private final int type;
	private final float x;
	private final float y;
	private final float width;
	private final float scale;

	private State state = State.NONE;

	public static ArrayList<Planet> planets = new ArrayList<>();
	public static final boolean[] planetTypeUsed = new boolean[16];

	private static final Random rand = new Random();
	public static final TextureRegion[] planetTextures = new TextureRegion[16];

	public Message message = null;

	public static void loadTextures() {
		if (planetTextures[0] != null) return; // Already loaded

		Texture sheet = new Texture("PLACEHOLDER_planets.png");
		int s = 105;
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 4; c++) {
				planetTextures[r * 4 + c] = new TextureRegion(sheet, 44 + c * s, 44 + r * s, 100, 100);
			}
		}

		planetTextures[12] = new TextureRegion(sheet, 40, 44 + 3 * s, s, s); // Purple
		planetTextures[13] = new TextureRegion(sheet, 45 + s, 44 + 3 * s, 90, s); // Asteroid
		planetTextures[14] = new TextureRegion(sheet, 35 + 2 * s, 44 + 3 * s, 125, s); // Saturn
		planetTextures[15] = new TextureRegion(sheet, 47 + 3 * s, 44 + 3 * s, 95, s); // Nibbled
	}

	private Planet(float x, float y, int type) {
		loadTextures();

		this.x = x;
		this.y = y;
		this.type = type;

		this.width =
				switch (type) {
					case 13 -> 90; // Asteroid
					case 14 -> 125; // Saturn
					case 15 -> 95; // Nibbled
					default -> 100; // Normal planets
				};

		this.scale = 2.5f * (float) Math.exp(-0.15 + 0.125 * rand.nextGaussian());

		planets.add(this);
	}

	public static void spawnNewPlanet() {
		if (planets.size() < 2) {
			float dist, angle, x, y;

			int tries = 0;
			while_loop:
			while (true) {
				if (tries > 1000) {
					System.out.println("Failed to spawn initial planets after 1000 tries, restarting game");
					Main.restart();
					return; // Give up after 1000 tries to prevent infinite loop
				}

				dist = (float) (Math.random() * 150 + 100);
				angle = (float) (Math.random() * 2 * Math.PI);

				x = Gdx.graphics.getWidth() / 2.0f + (float) Math.cos(angle) * dist;
				y = Gdx.graphics.getHeight() / 2.0f + (float) Math.sin(angle) * dist;

				for (Planet planet : planets) {
					if (Math.hypot(planet.x - x, planet.y - y)
							< 350) { // Minimum distance from existing planets
						tries++;
						continue while_loop;
					}
				}

				for (Satellite satellite : Satellite.satellites) {
					if (Math.hypot(satellite.getCX() - x, satellite.getCY() - y)
							< 200) { // Minimum distance from existing satellites
						tries++;
						continue while_loop;
					}
				}

				break;
			}

			int type = rand.nextInt(16);

			while (planetTypeUsed[type]) {
				type = rand.nextInt(16);
			}
			planetTypeUsed[type] = true;

			new Planet(x, y, type);
			System.out.println("Spawned initial planet at (" + x + ", " + y + ") with type " + type);
			return;
		}

		if (planets.size() >= 16) {
			return; // All planet types are used, can't spawn more
		}

		float x, y;

		int tries = 0;

		while_loop:
		while (true) {
			x =
					(float)
							(Math.random() * Gdx.graphics.getWidth() * 0.7f + Gdx.graphics.getWidth() * 0.05f);
			y =
					(float)
							(Math.random() * Gdx.graphics.getHeight() * 0.7f + Gdx.graphics.getHeight() * 0.05f);

			if (tries > 1000) {
				return; // Give up after 1000 tries to prevent infinite loop
			}

			boolean tooFar = true;

			for (Planet planet : planets) {
				double dist = Math.hypot(planet.x - x, planet.y - y);
				if (dist < 400) { // maximum distance of 500 pixels from an existing planet
					tooFar = false;
				}

				if (dist < 300) { // Minimum distance from existing planets
					tries++;
					continue while_loop;
				}

				for (Satellite satellite : Satellite.satellites) {
					if (Math.hypot(satellite.getCX() - x, satellite.getCY() - y)
							< 200) { // Minimum distance from existing planets
						tries++;
						continue while_loop;
					}
				}
			}

			if (tooFar) {
				tries++;
				continue;
			}
			break;
		}
		int type = rand.nextInt(16);
		while (planetTypeUsed[type]) {
			type = rand.nextInt(16);
		}
		planetTypeUsed[type] = true;
		new Planet(x, y, type);
	}

	private void draw(Batch batch, ShapeRenderer shapeRenderer) {
		if (state == State.HOVERED || state == State.SELECTED) {
			batch.end();
			shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
			shapeRenderer.setColor(state == State.HOVERED ? Color.YELLOW : Color.GREEN);
			float rad = (width * scale) / 2;
			shapeRenderer.circle(x - (50 - width / 2) + rad - (type == 14 ? 16 : 0), y + rad, rad);
			shapeRenderer.end();
			batch.begin();
		}

		batch.draw(
				planetTextures[type], x - (type == 14 ? 8 : 0), y, 0, 0, width, 100, scale, scale, 0);
	}

	public static void drawAll(Batch batch, ShapeRenderer shapeRenderer) {
		batch.begin();
		planets.forEach(planet -> planet.draw(batch, shapeRenderer));
		batch.end();
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public void setState(State state) {
		this.state = state;
	}

	@Override
	public float getCX() {
		return x + width / 2 * scale - (type == 14 ? 8 : 0);
	}

	@Override
	public float getCY() {
		return y + 50 * scale;
	}

	public float getWidth() {
		return 100 * scale;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Planet p && p.type == type;
	}
}
