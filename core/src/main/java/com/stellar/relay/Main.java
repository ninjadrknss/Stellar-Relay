package com.stellar.relay;

import static com.stellar.relay.GUI.score;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import space.earlygrey.shapedrawer.ShapeDrawer;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
	public enum Difficulty {
		EASY(0.5f),
		MEDIUM(1f),
		HARD(2.0f);

		public final float multiplier;

		Difficulty(float multiplier) {
			this.multiplier = multiplier;
		}
	}

	public enum GameState {
		SPLASH,
		TUTORIAL,
		FREE_PLAY,
		GAME_OVER
	}

	public static final boolean DEBUG = true;

	private PolygonSpriteBatch batch;
	private ShapeRenderer shapeRenderer;
	private ShapeDrawer shapeDrawer;
	private FitViewport viewport;

	private Controller controller_left;

	private GUI gui;

	private float messageSpawnTimer = 0;

	public static Difficulty difficulty = Difficulty.MEDIUM;

	public static GameState gameState = GameState.FREE_PLAY;
	private static float stateTimer = 0;
	private static GameState nextState = gameState;
	private static float stateTransitionTimer = 0;

	@Override
	public void create() {
		batch = new PolygonSpriteBatch();
		shapeRenderer = new ShapeRenderer();

		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.drawPixel(0, 0);
		Texture texture = new Texture(pixmap);
		pixmap.dispose();
		TextureRegion region = new TextureRegion(texture, 0, 0, 1, 1);
		shapeDrawer = new ShapeDrawer(batch, region);

		controller_left = new Controller(Player.LEFT);
		gui = GUI.getInstance();
		viewport = new FitViewport(Gdx.graphics.getWidth() / 10f, Gdx.graphics.getHeight() / 10f);

		restart();
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true); // true centers the camera
	}

	@Override
	public void render() {
		switch (gameState) {
			case SPLASH -> drawSplash();
			case TUTORIAL -> drawTutorial();

			case FREE_PLAY -> {
				input();
				drawFreePlay();
				batch.begin();
				gui.drawFreePlay(batch);
				batch.end();
				logic();
			}

			case GAME_OVER -> drawGameOver();
		}
		stateTimer += Gdx.graphics.getDeltaTime();

		if (gameState != nextState) {
			stateTransitionTimer += Gdx.graphics.getDeltaTime();

			batch.begin();
			shapeDrawer.setColor(
					0.15f, 0.15f, 0.2f, (float) (0.7 * Math.sin(stateTransitionTimer / 1.5 * Math.PI / 2)));
			shapeDrawer.filledRectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			batch.end();

			if (stateTransitionTimer > 1.5) {
				if (gameState == GameState.GAME_OVER) restart();

				gameState = nextState;
				stateTimer = 0;
				stateTransitionTimer = 0;
			}
		} else if (stateTimer < 1.5) {
			batch.begin();

			shapeDrawer.setColor(
					0.15f, 0.15f, 0.2f, (float) (0.7 * Math.cos(stateTimer / 1.5 * Math.PI / 2)));
			shapeDrawer.filledRectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			batch.end();
		}

		//		if (DEBUG)
		//			System.out.printf(
		//					"%s (%ss) n: %s (%ss)%n", gameState, stateTimer, nextState, stateTransitionTimer);

		viewport.apply();
	}

	private static Sprite backgroundStars;
	private static Sprite splashSatellite;
	private static Sprite splashMessage;

	private void drawSplash() {
		ScreenUtils.clear(0.15f, 0.15f, 0.15f, 1f);
		batch.begin();

		if (backgroundStars == null) {
			backgroundStars = new Sprite(new Texture("PLACEHOLDER_splashBackground.png"));
			backgroundStars.setPosition(-300, 0);
			backgroundStars.setSize(960 * 3, 539 * 3);
		}

		backgroundStars.draw(batch, 0.5f);

		//		shapeDrawer.setColor(Color.WHITE);
		//		shapeDrawer.line(
		//				Gdx.graphics.getWidth() / 2f, 0, Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight());

		Planet.loadTextures();

		batch.draw(
				Planet.planetTextures[12],
				Gdx.graphics.getWidth() * 5 / 8f + (float) (Math.sin(stateTimer / 2 + 2) * 20),
				-Gdx.graphics.getHeight() / 2f + (float) (Math.cos(stateTimer / 2 + 1) * 20),
				1000,
				1000);

		batch.draw(
				Planet.planetTextures[8],
				-Gdx.graphics.getWidth() / 8f + (float) (Math.sin(stateTimer / 2 + 0.3) * 20),
				Gdx.graphics.getHeight() / 2f + (float) (Math.cos(stateTimer / 2 + 0.5) * 20),
				800,
				800);

		if (splashSatellite == null) {
			splashSatellite = new Sprite(new Texture("PLACEHOLDER_Satellite.png"));

			splashSatellite.setPosition(
					Gdx.graphics.getWidth() * 3 / 4f - 50, Gdx.graphics.getHeight() - 450);
			splashSatellite.setSize(400, 400);
			splashSatellite.setOriginCenter();
		}

		splashSatellite.setRotation((float) (Math.sin(stateTimer / 2) * 15));
		splashSatellite.translate(
				(float) (Math.cos(stateTimer) / 3), (float) (Math.sin(stateTimer) / 3));
		splashSatellite.draw(batch);

		if (splashMessage == null) {
			splashMessage = new Sprite(new Texture("PLACEHOLDER_sendingMessage.png"));

			splashMessage.setPosition(
					Gdx.graphics.getWidth() / 4f - 200, Gdx.graphics.getHeight() / 2f - 400f);
			splashMessage.setSize(300, 300);
			splashMessage.setOriginCenter();
		}

		splashMessage.setRotation((float) (Math.sin(stateTimer / 1.8 + Math.PI / 2) * 15));
		splashMessage.translate(
				(float) (Math.cos(stateTimer + Math.PI / 2) / 5),
				(float) (Math.sin(stateTimer + Math.PI / 2) / 5));
		splashMessage.draw(batch);

		gui.drawSplash(batch);
		batch.end();

		controller_left.input(false);
		if (stateTimer > 2 && nextState == gameState && controller_left.isButton1Pressed()
				|| stateTimer > 2 && nextState == gameState && controller_left.isButton2Pressed()) {
			nextState = GameState.TUTORIAL;
			stateTransitionTimer = 0;
		}
	}

	private void drawTutorial() {
		if (stateTimer > 2 && nextState == gameState) {
			nextState = GameState.FREE_PLAY;
			stateTransitionTimer = 0;
			return;
		}

		ScreenUtils.clear(0.5f, 0.5f, 0.5f, 1f);
		batch.begin();
		gui.drawFreePlay(batch);
		batch.end();
	}

	private void drawFreePlay() {
		ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
		Planet.drawAll(batch, shapeRenderer);
		Message.drawAll(batch, shapeRenderer);
		Path.drawAll(shapeRenderer);
		Satellite.drawAll(batch, shapeRenderer);

		batch.begin();
		controller_left.draw(batch);
		batch.end();
	}

	private void drawGameOver() {
		ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

		Planet.drawAll(batch, shapeRenderer);
		Satellite.drawAll(batch, shapeRenderer);

		batch.begin();
		//		shapeDrawer.setColor(Color.WHITE);
		//		shapeDrawer.line(
		//				Gdx.graphics.getWidth() / 2f, 0, Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight());
		shapeDrawer.setColor(0.15f, 0.15f, 0.2f, 0.7f);
		shapeDrawer.filledRectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		shapeDrawer.setColor(0.9f, 0.9f, 0.9f, 0.5f);
		shapeDrawer.filledRectangle(
				Gdx.graphics.getWidth() / 2f - 175, Gdx.graphics.getHeight() / 2f - 230, 350, 100);

		shapeDrawer.setColor(0.15f, 0.15f, 0.2f, 1f);
		shapeDrawer.filledRectangle(
				Gdx.graphics.getWidth() / 2f - 165,
				Gdx.graphics.getHeight() / 2f - 220,
				Math.min(stateTimer / 15 * 330, 330),
				80);

		controller_left.input(false);
		if (stateTimer > 15 && nextState == gameState
				|| nextState == gameState && controller_left.isButton1Pressed()
				|| nextState == gameState && controller_left.isButton2Pressed()) {
			nextState = GameState.SPLASH;
			stateTransitionTimer = 0;
		}

		gui.drawGameOver(batch);

		batch.end();
	}

	@Override
	public void dispose() {
		batch.dispose();
		shapeRenderer.dispose();
	}

	private void input() {
		controller_left.input(true);
	}

	private void logic() {
		if (score < 300) {
			if (Message.messages.isEmpty()) {
				Planet.spawnNewPlanet();
				for (int i = 0; i < 2 * Math.log(Planet.planets.size()); i++) {
					Satellite.spawnNewSatellite();
				}

				for (int i = 0; i < 2 * Math.log(Planet.planets.size()); i++) {
					Message.spawnNewMessage();
				}

				messageSpawnTimer = (float) (Math.random() * 10f);
			}
		} else if (messageSpawnTimer <= 0 || Message.messages.isEmpty()) {
			messageSpawnTimer = (float) (Math.random() * 10f);

			if (Math.random() > 0.2 * Math.sqrt(Planet.planets.size())) Planet.spawnNewPlanet();
			for (int i = 0; i < Math.log(Planet.planets.size()); i++) {
				Satellite.spawnNewSatellite();
			}

			for (int i = 0; i < 2 * Math.log(Planet.planets.size()); i++) {
				Message.spawnNewMessage();
			}
		}
	}

	public static void initGameOver() {
		if (gameState != GameState.GAME_OVER && nextState == gameState) {
			nextState = GameState.GAME_OVER;
			stateTransitionTimer = 0;
		}
	}

	public static void restart() {
		score = 0;
		Satellite.satellites.clear();
		Planet.planets.clear();
		Message.messages.clear();
		Path.paths.clear();

		for (int i = 0; i < 2; i++) {
			Planet.spawnNewPlanet();
		}

		for (int i = 0; i < 2; i++) {
			Satellite.spawnNewSatellite();
		}

		Message.spawnNewMessage();
	}
}
