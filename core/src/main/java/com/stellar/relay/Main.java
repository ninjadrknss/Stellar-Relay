package com.stellar.relay;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
	public static final boolean DEBUG = true;

	private SpriteBatch batch;
	private FitViewport viewport;

	private Controller controller_left;

	private GUI gui;

	@Override
	public void create() {
		batch = new SpriteBatch();
		controller_left = new Controller(100, 100, Player.LEFT);
		gui = new GUI();
		viewport = new FitViewport(8, 8);

		for (int i = 0; i < 2; i++) {
			Planet.spawnNewPlanet();
		}

		for (int i = 0; i < 2; i++) {
			Satellite.spawnNewSatellite();
		}
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true); // true centers the camera
	}

	@Override
	public void render() {
		input();
		draw();
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
		batch.begin();
		Planet.drawAll(batch);
		Satellite.drawAll(batch);
		controller_left.draw(batch);
		viewport.apply();
		gui.draw(batch);
		batch.end();
	}
}
