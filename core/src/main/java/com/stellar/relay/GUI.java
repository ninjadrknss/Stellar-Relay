package com.stellar.relay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.*;

public class GUI {
	private final BitmapFont font96;
	private final BitmapFont font60;
	private final BitmapFont font48;
	private final BitmapFont font36;
	private final BitmapFont font24;
	private final BitmapFont font12;

	public static int score = 0;

	private static GUI instance = null;

	public static GUI getInstance() {
		if (instance == null) instance = new GUI();
		return instance;
	}

	private GUI() {
		FreeTypeFontGenerator generator =
				new FreeTypeFontGenerator(Gdx.files.internal("fonts/PixelatedEleganceRegular-ovawB.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 96;
		font96 = generator.generateFont(parameter); // font size 96 pixels
		parameter.size = 60;
		font60 = generator.generateFont(parameter); // font size 60 pixels
		parameter.size = 48;
		font48 = generator.generateFont(parameter); // font size 48 pixels
		parameter.size = 36;
		font36 = generator.generateFont(parameter); // font size 36 pixels
		parameter.size = 24;
		font24 = generator.generateFont(parameter); // font size 24 pixels
		parameter.size = 12;
		font12 = generator.generateFont(parameter); // font size 12 pixels
		generator.dispose();
	}

	public void drawSplash(Batch batch) {
		font96.draw(
				batch,
				"Signal Rush",
				Gdx.graphics.getWidth() / 2f - 375f,
				Gdx.graphics.getHeight() / 2f + 50f);

		font36.draw(
				batch,
				"Press Any Button\n       to Start",
				Gdx.graphics.getWidth() / 2f - 200f,
				Gdx.graphics.getHeight() / 2f - 100f);
	}

	public void drawFreePlay(Batch batch) {
		font36.draw(
				batch,
				"Score: %5d".formatted(score),
				Gdx.graphics.getWidth() / 2f - 150f,
				Gdx.graphics.getHeight() - 25f);

		if (Main.DEBUG) {
			font12.draw(batch, "FPS: " + (int) (1 / Gdx.graphics.getDeltaTime()), 10, 100);

			font12.draw(batch, "Satellites: " + Satellite.satellites.size(), 10, 80);
			font12.draw(batch, "Planets: " + Planet.planets.size(), 10, 60);
			font12.draw(batch, "Messages: " + Message.messages.size(), 10, 40);
			font12.draw(batch, "Paths: " + Path.paths.size(), 10, 20);
		}
	}

	public void drawGameOver(Batch batch) {
		font96.draw(
				batch,
				"Game Over!",
				Gdx.graphics.getWidth() / 2f - 375f,
				Gdx.graphics.getHeight() / 2f + 150f);

		font48.draw(
				batch,
				"Final Score: %5d".formatted(score),
				Gdx.graphics.getWidth() / 2f - 275f,
				Gdx.graphics.getHeight() / 2f - 25f);

		font24.draw(
				batch,
				"Press Any Button\n   to Play Again",
				Gdx.graphics.getWidth() / 2f - 140f,
				Gdx.graphics.getHeight() / 2f - 155f);
	}
}
