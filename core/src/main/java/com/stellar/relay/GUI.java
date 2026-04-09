package com.stellar.relay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.*;

public class GUI {
	private final BitmapFont font96, font60, font48, font36, font24, font12;

	public static int score = 0;
	public static int tutorialStep = 0;

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

	public void drawSplash(Batch batch, float stateTimer) {
		batch.begin();
		font96.draw(
				batch,
				"Stellar Relay",
				Gdx.graphics.getWidth() / 2f - 375f,
				Gdx.graphics.getHeight() / 2f + 55f + 8f * (float) Math.cos(stateTimer * 2f + 0.7f));

		font36.draw(
				batch,
				"Press Any Button\n       to Start",
				Gdx.graphics.getWidth() / 2f - 200f,
				Gdx.graphics.getHeight() / 2f - 100f + 5f * (float) Math.cos(stateTimer * 2f));
		batch.end();
	}

	public void drawDifficultySelect(Batch batch) {
		batch.begin();
		font60.draw(
				batch,
				"Select Difficulty",
				Gdx.graphics.getWidth() / 2f - 375f,
				Gdx.graphics.getHeight() - 55f);

		font24.draw(
				batch,
				"Move the joystick to select a difficulty",
				Gdx.graphics.getWidth() / 2f - 350f,
				Gdx.graphics.getHeight() - 150f);

		font36.draw(
				batch,
				"Press the Red Button\n        to Select",
				Gdx.graphics.getWidth() / 2f - 275f,
				100f);

		font24.draw(batch, "Easy", Gdx.graphics.getWidth() / 4f - 50f, 200f);
		font24.draw(batch, "Medium", Gdx.graphics.getWidth() / 2f - 60f, 200f);
		font24.draw(batch, "Hard", 3 * Gdx.graphics.getWidth() / 4f - 50f, 200f);
		batch.end();
	}

	public void drawStory(Batch batch) {
		batch.begin();
		font12.draw(
				batch, "Press the Red Button\nto fast forward", Gdx.graphics.getWidth() - 200f, 35f);
		batch.end();
	}

	public void drawFreePlay(Batch batch) {
		batch.begin();
		switch (tutorialStep) {
			case 0 ->
					font24.draw(
							batch,
							"Welcome to Stellar Relay! In this game, you \nwill be relaying messages between planets using satellites.",
							50f,
							Gdx.graphics.getHeight() - 50f);
			case 1 ->
					font24.draw(
							batch,
							"Your goal is to deliver as many messages as possible \nbefore time runs out. Each message has a source planet \nand a destination planet.",
							50f,
							Gdx.graphics.getHeight() - 50f);
			case 2 ->
					font24.draw(
							batch,
							"Use the joystick to move the spacecraft around the \nsolar system. You can move over planets and satellites to \ninteract with them.",
							50f,
							Gdx.graphics.getHeight() - 50f);
			case 3 ->
					font24.draw(
							batch,
							"To send a message, first select a source planet by \nmoving your satellite over it and pressing the red button.",
							50f,
							Gdx.graphics.getHeight() - 50f);
			case 4 ->
					font24.draw(
							batch,
							"Then select a satellite by moving over it and pressing \nthe red button again. Finally, select a destination \nplanet in the same way.",
							50f,
							Gdx.graphics.getHeight() - 50f);
			case 5 ->
					font24.draw(
							batch,
							"Press the yellow button to remove the last planet or \nsatellite from the path at any time.",
							50f,
							Gdx.graphics.getHeight() - 50f);
			case 6 ->
					font24.draw(
							batch,
							"Or once the path is completed, press the red button \nagain to confirm the path.",
							50f,
							Gdx.graphics.getHeight() - 50f);
			case 7 ->
					font24.draw(
							batch,
							"Satellites can only carry one message at a time, \nso plan your routes carefully!",
							50f,
							Gdx.graphics.getHeight() - 50f);
			case 8 ->
					font24.draw(
							batch,
							"Messages will expire if they take too long to deliver, \nso speed and planning is important!",
							50f,
							Gdx.graphics.getHeight() - 50f);
			case 9 ->
					font24.draw(
							batch,
							"Good luck, and have fun playing Stellar Relay!",
							50f,
							Gdx.graphics.getHeight() - 50f);

			default ->
					font36.draw(
							batch,
							"Score: %5d".formatted(score),
							Gdx.graphics.getWidth() / 2f - 150f,
							Gdx.graphics.getHeight() - 25f);
		}

		if (Main.DEBUG) {
			font12.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 160);
			font12.draw(
					batch,
					"Memory: %5dkb"
							.formatted(
									(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000),
					10,
					140);

			font12.draw(batch, "Satellites: " + Satellite.satellites.size(), 10, 120);
			font12.draw(batch, "Planets: " + Planet.planets.size(), 10, 100);
			font12.draw(batch, "Messages: " + Message.messages.size(), 10, 80);
			font12.draw(batch, "Paths: " + Path.paths.size(), 10, 60);
			font12.draw(batch, "Tutorial Step: " + tutorialStep, 10, 40);
			font12.draw(batch, "Confetti: " + Confetti.size(), 10, 20);
		}
		batch.end();
	}

	public void drawGameOver(Batch batch) {
		batch.begin();
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
		batch.end();
	}
}
