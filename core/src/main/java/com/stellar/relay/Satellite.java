package com.stellar.relay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;

public class Satellite implements Pathable {
	private Sprite sprite;
	private State state = State.NONE;

	private float angle = (float) (Math.random() * 360);
	private float anglePeriod = (float) (Math.random() * 2 + 1); // Random period for orbiting

	public static ArrayList<Satellite> satellites = new ArrayList<>();

	public Path path = null;

	public Satellite(float x, float y) {
		sprite = new Sprite(new Texture("PLACEHOLDER_Satellite.png"));
		sprite.setSize(80, 80);
		sprite.setOriginCenter();
		sprite.setPosition(x, y);
		sprite.setRotation(angle); // Random initial rotation

		satellites.add(this);
	}

	private void draw(Batch batch, ShapeRenderer shapeRenderer) {
		if (state == State.HOVERED || state == State.SELECTED) {
			batch.end();
			shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
			shapeRenderer.setColor(state == State.HOVERED ? Color.YELLOW : Color.GREEN);
			shapeRenderer.circle(
					sprite.getX() + sprite.getWidth() / 2,
					sprite.getY() + sprite.getHeight() / 2,
					sprite.getWidth() / 2);
			shapeRenderer.end();
			batch.begin();
		}

		anglePeriod += 0.01f * Gdx.graphics.getDeltaTime();
		sprite.setRotation(
				(float) (angle + Math.sin(anglePeriod) * 15)); // Oscillate rotation for a more dynamic look

		sprite.draw(batch);
	}

	public static void drawAll(Batch batch, ShapeRenderer shapeRenderer) {
		batch.begin();
		satellites.forEach(satellite -> satellite.draw(batch, shapeRenderer));
		batch.end();
	}

	public static void spawnNewSatellite() {
		spawnNewSatellite(Planet.planets.get((int) (Math.random() * Planet.planets.size())));
	}

	public static void spawnNewSatellite(Planet planet1) {
		Planet planet2 = Planet.planets.get((int) (Math.random() * Planet.planets.size()));

		while (planet2 == planet1) {
			planet2 = Planet.planets.get((int) (Math.random() * Planet.planets.size()));
		}

		spawnNewSatellite(planet1, planet2);
	}

	public static void spawnNewSatellite(Planet planet1, Planet planet2) {
		float x = (planet1.getCX() + planet2.getCX()) / 2;
		float y = (planet1.getCY() + planet2.getCY()) / 2;

		float x_delta, y_delta;

		int tries = 0;

		while_loop:
		while (true) {
			float perpendicularAngle =
					(float) Math.atan2(planet2.getCY() - planet1.getCY(), planet2.getCX() - planet1.getCX())
							+ (float) Math.PI / 2;
			float offset = 150 * ((float) Math.random() * 2 - 1) * ((float) Math.random() * 2 - 1);

			if (tries > 1000) {
				System.out.println("Failed to place satellite after 1000 tries, giving up.");
				return;
			}

			x_delta = (float) (Math.cos(perpendicularAngle) * offset);
			y_delta = (float) (Math.sin(perpendicularAngle) * offset);
			for (Planet planet : Planet.planets) {
				float dist = (float) Math.hypot(x + x_delta - planet.getCX(), y + y_delta - planet.getCY());
				if (dist < planet.getWidth() / 2 + 50) {
					tries++;
					continue while_loop; // Too close to a planet, try again
				}
			}

			for (Satellite satellite : satellites) {
				float dist =
						(float)
								Math.hypot(
										x + x_delta - satellite.sprite.getX(), y + y_delta - satellite.sprite.getY());
				if (dist < satellite.sprite.getWidth() + 30) {
					tries++;
					continue while_loop; // Too close to another satellite, try again
				}
			}

			break;
		}

		new Satellite(x + x_delta, y + y_delta);
	}

	@Override
	public float getCX() {
		return sprite.getX() + sprite.getWidth() / 2;
	}

	@Override
	public float getCY() {
		return sprite.getY() + sprite.getHeight() / 2;
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public void setState(State state) {
		this.state = state;
	}

	public boolean inRange(Pathable pathable) {
		return Math.hypot(getCX() - pathable.getCX(), getCY() - pathable.getCY())
				< Path.MAX_CONNECTION_LENGTH;
	}
}
