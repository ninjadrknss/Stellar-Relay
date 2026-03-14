package com.stellar.relay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.Random;

public class Planet {
	private final int type;
	private final float x;
	private final float y;
	private final float width;
	private final float scale;

	private boolean selected = false;

	public static ArrayList<Planet> planets = new ArrayList<>();

	private static final Random rand = new Random();
	private static final TextureRegion[] planetTextures = new TextureRegion[16];
	private static ShapeRenderer shapeRenderer;

	private Planet(float x, float y, int type) {
		if (planetTextures[0] == null) {
			shapeRenderer = new ShapeRenderer();

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

		this.scale = 1.1f * (float) Math.exp(-0.15 + 0.25 * rand.nextGaussian());
		//		this.scale = 0.5f;

		planets.add(this);
	}

	public static void spawnNewPlanet() {
		float x = rand.nextFloat() * Gdx.graphics.getWidth() * 0.7f + Gdx.graphics.getWidth() * 0.05f;
		float y = rand.nextFloat() * Gdx.graphics.getHeight() * 0.7f + Gdx.graphics.getHeight() * 0.05f;

		int tries = 0;

		while_loop:
		while (true) {
			if (tries > 1000) {
				return; // Give up after 1000 tries to prevent infinite loop
			}

			for (Planet planet : planets) {
				if (Math.hypot(planet.x - x, planet.y - y) < 150) {
					x = rand.nextFloat() * Gdx.graphics.getWidth() * 0.7f + Gdx.graphics.getWidth() * 0.05f;
					y = rand.nextFloat() * Gdx.graphics.getHeight() * 0.7f + Gdx.graphics.getHeight() * 0.05f;
					tries++;
					continue while_loop;
				}
			}
			break;
		}
		//		int type = planets.size() % 16;
		//		new Planet(type % 4 * 120 + 50, 500 - type / 4 * 120, type);
		int type = rand.nextInt(16);
		new Planet(x, y, type);
	}

	public void draw(Batch batch) {
		//		batch.end();
		//		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		//		shapeRenderer.setColor(Color.WHITE);
		//		shapeRenderer.rect(x + (50 - width / 2), y, width * scale, 100 * scale);
		//		shapeRenderer.end();
		//		batch.begin();

		batch.draw(
				planetTextures[type], x - (type == 14 ? 8 : 0), y, 0, 0, width, 100, scale, scale, 0);
	}

	public static void drawAll(Batch batch) {
		for (Planet planet : planets) {
			planet.draw(batch);
		}
	}

	public static Planet getClosestPlanet(float x, float y) {
		Planet closest = null;
		float closestDist = Float.MAX_VALUE;
		for (Planet planet : planets) {
			float dist = (float) Math.hypot(planet.getCX() - x, planet.getCY() - y);
			if (dist < closestDist) {
				closestDist = dist;
				closest = planet;
			}
		}

		if (closest == null || closestDist > closest.width * closest.scale / 2 + 10) {
			return null;
		}
		return closest;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public float getCX() {
		return x + width / 2 * scale - (type == 14 ? 8 : 0);
	}

	public float getCY() {
		return y + 55 * scale;
	}
}
