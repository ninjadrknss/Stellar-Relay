package com.stellar.relay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import java.util.ArrayList;

public class Satellite {
	enum State {
		NONE,
		TRANSMITTING
	}

	private Sprite sprite;
	private State state = State.NONE;

	private float angle = (float) (Math.random() * 360);
	private float anglePeriod = (float) (Math.random() * 2 + 1); // Random period for orbiting

	public static ArrayList<Satellite> satellites = new ArrayList<>();

	public Satellite(float x, float y) {
		sprite = new Sprite(new Texture("PLACEHOLDER_Satellite.png"));
		sprite.setSize(60, 60);
		sprite.setOriginCenter();
		sprite.setPosition(x, y);
		sprite.setRotation(angle); // Random initial rotation

		satellites.add(this);
	}

	private void draw(Batch batch) {
		anglePeriod += 0.01f * Gdx.graphics.getDeltaTime();
		sprite.setRotation(
				(float) (angle + Math.sin(anglePeriod) * 15)); // Oscillate rotation for a more dynamic look

		sprite.draw(batch);
	}

	public static void drawAll(Batch batch) {
		satellites.forEach(satellite -> satellite.draw(batch));
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

		while_loop:
		while (true) {
			float perpendicularAngle =
					(float) Math.atan2(planet2.getCY() - planet1.getCY(), planet2.getCX() - planet1.getCX())
							+ (float) Math.PI / 2;
			float offset = 150 * ((float) Math.random() * 2 - 1);

			x_delta = (float) (Math.cos(perpendicularAngle) * offset);
			y_delta = (float) (Math.sin(perpendicularAngle) * offset);
			for (Planet planet : Planet.planets) {
				float dist = (float) Math.hypot(x + x_delta - planet.getCX(), y + y_delta - planet.getCY());
				if (dist < planet.getWidth() + 20) {
					continue while_loop; // Too close to a planet, try again
				}
			}

			for (Satellite satellite : satellites) {
				float dist =
						(float)
								Math.hypot(
										x + x_delta - satellite.sprite.getX(), y + y_delta - satellite.sprite.getY());
				if (dist < satellite.sprite.getWidth() + 30) {
					continue while_loop; // Too close to another satellite, try again
				}
			}

			break;
		}

		new Satellite(x + x_delta, y + y_delta);
	}
}
