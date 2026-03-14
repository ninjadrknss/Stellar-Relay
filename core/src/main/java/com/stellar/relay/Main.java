package com.stellar.relay;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
	private SpriteBatch batch;
	private FitViewport viewport;

	private Controller controller;

	@Override
	public void create() {
		batch = new SpriteBatch();
		controller = new Controller(100, 100, Player.LEFT);
		viewport = new FitViewport(8, 8);

		for (int i = 0; i < 16; i++) {
			Planet.spawnNewPlanet();
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
		controller.input();
	}

	private void draw() {
		ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
		batch.begin();
		Planet.drawAll(batch);
		controller.draw(batch);
		viewport.apply();
		batch.end();
	}
}
