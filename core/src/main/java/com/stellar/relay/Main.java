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
import java.util.Arrays;
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
		DIFFICULTY_SELECT,
		STORY,
		FREE_PLAY,
		GAME_OVER
	}

	public static final boolean DEBUG = true;

	private PolygonSpriteBatch batch;
	private ShapeRenderer shapeRenderer;
	private ShapeDrawer shapeDrawer;
	private FitViewport viewport;

	private static Controller controller_left;
	private static Controller controller_right;

	private static boolean leftControl = true;

	private GUI gui;

	private float messageSpawnTimer = 0;

	public static Difficulty difficulty = Difficulty.MEDIUM;

	public static GameState gameState = GameState.SPLASH;
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
		controller_right = new Controller(Player.RIGHT);
		gui = GUI.getInstance();
		viewport = new FitViewport(Gdx.graphics.getWidth() / 10f, Gdx.graphics.getHeight() / 10f);

		if (gameState == GameState.FREE_PLAY) restart();
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true); // true centers the camera
	}

	@Override
	public void render() {
		switch (gameState) {
			case SPLASH -> drawSplash();
			case DIFFICULTY_SELECT -> {
				input();
				drawDifficultySelect();
				batch.begin();
				gui.drawDifficultySelect(batch);
				batch.end();
			}

			case STORY -> drawStory();

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
				if (nextState == GameState.STORY) restart();

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
			splashSatellite = new Sprite(new Texture("sprites/satellite_signal.png"));

			splashSatellite.setPosition(
					Gdx.graphics.getWidth() * 3 / 4f - 50, Gdx.graphics.getHeight() - 450);
			splashSatellite.setSize(400, 400);
			splashSatellite.setOriginCenter();
		}

		splashSatellite.setRotation((float) (Math.sin(stateTimer / 2) * 15) + 120);
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

		gui.drawSplash(batch, stateTimer);
		batch.end();

		controller_left.input(false);
		if (stateTimer > 2 && nextState == gameState && controller_left.isButton1Pressed()
				|| stateTimer > 2 && nextState == gameState && controller_left.isButton2Pressed()) {
			nextState = GameState.DIFFICULTY_SELECT;
			stateTransitionTimer = 0;
			leftControl = true;
		}

		controller_right.input(false);
		if (stateTimer > 2 && nextState == gameState && controller_right.isButton1Pressed()
				|| stateTimer > 2 && nextState == gameState && controller_right.isButton2Pressed()) {
			nextState = GameState.DIFFICULTY_SELECT;
			stateTransitionTimer = 0;
			leftControl = false;
		}
	}

	private static Sprite easyMessage;
	private static Sprite[] mediumMessages;
	private static Sprite[] hardMessages;

	private void drawDifficultySelect() {
		ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

		batch.begin();

		shapeDrawer.setColor(0.75f, 0.75f, 0.80f, 1f);

		shapeDrawer.filledRectangle(
				Gdx.graphics.getWidth() / 4f - 200, Gdx.graphics.getHeight() / 2f - 300, 400, 600);

		shapeDrawer.filledRectangle(
				Gdx.graphics.getWidth() / 2f - 200, Gdx.graphics.getHeight() / 2f - 300, 400, 600);

		shapeDrawer.filledRectangle(
				3 * Gdx.graphics.getWidth() / 4f - 200, Gdx.graphics.getHeight() / 2f - 300, 400, 600);

		float pos = (leftControl ? controller_left : controller_right).getCX();

		int selected;
		if (pos < Gdx.graphics.getWidth() / 4f) selected = 0;
		else if (pos > 3 * Gdx.graphics.getWidth() / 4f) selected = 2;
		else selected = 1;

		shapeDrawer.setColor(0.25f, 0.25f, 0.3f, 0.7f);
		shapeDrawer.filledRectangle(
				(selected + 1) * Gdx.graphics.getWidth() / 4f - 200,
				Gdx.graphics.getHeight() / 2f - 300,
				400,
				600);

		if (nextState == gameState
				&& stateTimer > 2
				&& (leftControl && controller_left.isButton1Pressed()
						|| !leftControl && controller_right.isButton1Pressed())) {
			difficulty = Difficulty.values()[selected];
			nextState = GameState.STORY;
			stateTransitionTimer = 0;
		}

		Planet.loadTextures();

		batch.draw(
				Planet.planetTextures[6],
				Gdx.graphics.getWidth() / 4f - 175,
				Gdx.graphics.getHeight() / 2f - 325,
				400,
				400);

		if (easyMessage == null) {
			easyMessage = new Sprite(new Texture("PLACEHOLDER_planetMessage.png"));
			easyMessage.setPosition(
					Gdx.graphics.getWidth() / 4f + 75f, Gdx.graphics.getHeight() / 2f - 85f);
			easyMessage.setSize(80, 80);
		}

		easyMessage.draw(batch);

		batch.draw(
				Planet.planetTextures[10],
				Gdx.graphics.getWidth() / 2f - 50,
				Gdx.graphics.getHeight() / 2f - 250,
				250,
				250);

		batch.draw(
				Planet.planetTextures[8],
				Gdx.graphics.getWidth() / 2f - 150,
				Gdx.graphics.getHeight() / 2f + 50,
				225,
				225);

		if (mediumMessages == null) {
			mediumMessages =
					new Sprite[] {
						new Sprite(new Texture("PLACEHOLDER_planetMessage.png")),
						new Sprite(new Texture("PLACEHOLDER_planetMessage.png"))
					};

			mediumMessages[0].setPosition(
					Gdx.graphics.getWidth() / 2f + 105, Gdx.graphics.getHeight() / 2f - 95);
			mediumMessages[0].setSize(60, 60);
			mediumMessages[1].setPosition(
					Gdx.graphics.getWidth() / 2f - 7, Gdx.graphics.getHeight() / 2f + 192);
			mediumMessages[1].setSize(60, 60);
		}

		for (Sprite msg : mediumMessages) {
			msg.draw(batch);
		}

		batch.draw(
				Planet.planetTextures[12],
				3 * Gdx.graphics.getWidth() / 4f - 200,
				Gdx.graphics.getHeight() / 2f - 325,
				275,
				275);
		batch.draw(
				Planet.planetTextures[9],
				3 * Gdx.graphics.getWidth() / 4f + 20,
				Gdx.graphics.getHeight() / 2f - 100,
				175,
				175);
		batch.draw(
				Planet.planetTextures[3],
				3 * Gdx.graphics.getWidth() / 4f - 175,
				Gdx.graphics.getHeight() / 2f + 50,
				200,
				200);

		if (hardMessages == null) {
			hardMessages =
					new Sprite[] {
						new Sprite(new Texture("PLACEHOLDER_planetMessage.png")),
						new Sprite(new Texture("PLACEHOLDER_planetMessage.png")),
						new Sprite(new Texture("PLACEHOLDER_planetMessage.png"))
					};

			hardMessages[0].setPosition(
					3 * Gdx.graphics.getWidth() / 4f - 22, Gdx.graphics.getHeight() / 2f - 137);
			hardMessages[0].setSize(60, 60);
			hardMessages[1].setPosition(
					3 * Gdx.graphics.getWidth() / 4f + 137, Gdx.graphics.getHeight() / 2f + 17);
			hardMessages[1].setSize(60, 60);
			hardMessages[2].setPosition(
					3 * Gdx.graphics.getWidth() / 4f - 35, Gdx.graphics.getHeight() / 2f + 200);
			hardMessages[2].setSize(60, 60);
		}

		for (Sprite msg : hardMessages) {
			msg.draw(batch);
		}

		batch.end();
	}

	private void drawStory() {
		if (stateTimer > 2 && nextState == gameState) {
			nextState = GameState.FREE_PLAY;
			stateTransitionTimer = 0;
			return;
		}

		ScreenUtils.clear(0.5f, 0.5f, 0.5f, 1f);
	}

	private void drawFreePlay() {
		ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
		Planet.drawAll(batch, shapeRenderer);
		Path.drawAll(shapeRenderer);
		Satellite.drawAll(batch, shapeRenderer);
		Message.drawAll(batch, shapeRenderer);

		batch.begin();
		controller_left.draw(batch);
		controller_right.draw(batch);
		batch.end();

		GUI.tutorialStep = (int) Math.min(10, (stateTimer - 2) / 8);
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
			leftControl = true;
		}

		controller_right.input(false);
		if (stateTimer > 15 && nextState == gameState
				|| nextState == gameState && controller_right.isButton1Pressed()
				|| nextState == gameState && controller_right.isButton2Pressed()) {
			nextState = GameState.SPLASH;
			stateTransitionTimer = 0;
			leftControl = false;
		}

		gui.drawGameOver(batch);

		batch.end();
	}

	@Override
	public void dispose() {
		batch.dispose();
		shapeRenderer.dispose();

		if (backgroundStars != null) backgroundStars.getTexture().dispose();
		if (splashSatellite != null) splashSatellite.getTexture().dispose();
		if (splashMessage != null) splashMessage.getTexture().dispose();
		if (easyMessage != null) easyMessage.getTexture().dispose();
		if (mediumMessages != null) {
			for (Sprite msg : mediumMessages) {
				if (msg != null) msg.getTexture().dispose();
			}
		}
		if (hardMessages != null) {
			for (Sprite msg : hardMessages) {
				if (msg != null) msg.getTexture().dispose();
			}
		}
	}

	private void input() {
		controller_left.input(true);
		controller_right.input(true);
	}

	private void logic() {
		messageSpawnTimer -= Gdx.graphics.getDeltaTime();
		if (Message.isClear() && messageSpawnTimer > 5)
			messageSpawnTimer =
					1.5f; // Spawn new items soon if all messages are cleared to prevent long boring waits

		if (score < 300) {
			if (Message.messages.isEmpty()) {
				Planet.spawnNewPlanet();
				for (int i = 0; i < 2 * Math.log(Planet.planets.size()); i++) {
					Satellite.spawnNewSatellite();
				}

				for (int i = 0; i < 2 * Math.log(Planet.planets.size()); i++) {
					Message.spawnNewMessage();
				}

				messageSpawnTimer = (float) (Math.random() * 60) + 60;
			}
		} else if (messageSpawnTimer <= 0) {
			System.out.println("Spawning new items...");
			messageSpawnTimer =
					(float) (Math.random() * 60f / difficulty.multiplier / Math.sqrt(score / 100f)) + 15;

			if (Math.random() > Math.sqrt(Planet.planets.size()) / 20) Planet.spawnNewPlanet();
			for (int i = 0; i < Math.log(Planet.planets.size()) * (1.5 * Math.random()); i++) {
				Satellite.spawnNewSatellite();
			}

			for (int i = -1; i < Math.log(Planet.planets.size()) / 2; i++) {
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
		System.out.println("Restarting game...");

		score = 0;
		Satellite.satellites.clear();
		Planet.planets.clear();
		Arrays.fill(Planet.planetTypeUsed, false);
		Message.messages.clear();
		Path.paths.clear();

		controller_left.reset();
		controller_right.reset();

		if (leftControl) controller_left.active = true;
		else controller_right.active = true;

		for (int i = 0; i < 2; i++) {
			Planet.spawnNewPlanet();
		}

		for (int i = 0; i < 2; i++) {
			Satellite.spawnNewSatellite();
		}

		Message.spawnNewMessage();
	}
}
