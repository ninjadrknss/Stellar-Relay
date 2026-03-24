package com.stellar.relay;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {

	// So my current thought is to base it around a game like Cleared to Land,
	// where the focus is on routing message through satellites while avoiding obstacles.
	// I am feeling like there would be a bunch of planets which have personified
	// inhabitants that want to send message to other planets via routing through
	// satellites. The message would have an expiration time, and once triggered would
	// end the game. To make it challenging, more and more planets and message will appear,
	// each satellite can only send or queue one message at a time, and maybe obstacles like
	// astroid clusters or gas clouds blocking routes between some satellites or satellites
	// running out of battery. The game would be controlled via controlling a cursor that snaps
	// to planets, with the two buttons either confirming the route or cancelling it while in
	// the pathing state or clearing a route or pausing it.
	//
	// Also, it would have the option for 1 or 2 players, with each player’s cursor and
	// pathing being related to their joystick color, and each path being only able to
	// be controlled by one player.
	public static final boolean DEBUG = true;

	private SpriteBatch batch;
	private ShapeRenderer shapeRenderer;
	private FitViewport viewport;

	private Controller controller_left;

	private GUI gui;

	@Override
	public void create() {
		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
		controller_left = new Controller(100, 100, Player.LEFT);
		gui = GUI.getInstance();
		viewport = new FitViewport(Gdx.graphics.getWidth() / 10f, Gdx.graphics.getHeight() / 10f);

		for (int i = 0; i < 2; i++) {
			Planet.spawnNewPlanet();
		}

		for (int i = 0; i < 2; i++) {
			Satellite.spawnNewSatellite();
		}

		Message.spawnNewMessage();

		System.out.println("Satellites: " + Satellite.satellites.size());
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true); // true centers the camera
	}

	@Override
	public void render() {
		input();
		draw();
		logic();
	}

	@Override
	public void dispose() {
		batch.dispose();
	}

	private void input() {
		controller_left.input();
	}

	private void draw() {
		ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
		Planet.drawAll(batch, shapeRenderer);
		Message.drawAll(batch, shapeRenderer);
		Path.drawAll(shapeRenderer);
		Satellite.drawAll(batch, shapeRenderer);

		batch.begin();
		controller_left.draw(batch);
		gui.draw(batch);
		batch.end();

		viewport.apply();
	}

	private void logic() {
		if (Message.messages.isEmpty()) {
			Planet.spawnNewPlanet();
			for (int i = 0; i < 2 * Math.log(Planet.planets.size()); i++) {
				Satellite.spawnNewSatellite();
			}

			for (int i = 0; i < 2 * Math.log(Planet.planets.size()); i++) {
				Message.spawnNewMessage();
			}
		}
	}

	public static void gameOver() {
		throw new RuntimeException("Game Over! Final Score: " + GUI.score);
	}
}
