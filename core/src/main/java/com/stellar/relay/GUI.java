package com.stellar.relay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.*;

public class GUI {
	private final BitmapFont font24;
	private final BitmapFont font12;

	private int score = 0;

	public GUI() {
		FreeTypeFontGenerator generator =
				new FreeTypeFontGenerator(Gdx.files.internal("fonts/PixelatedEleganceRegular-ovawB.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 24;
		font24 = generator.generateFont(parameter); // font size 24 pixels
		parameter.size = 12;
		font12 = generator.generateFont(parameter); // font size 12 pixels
		generator.dispose(); // don't forget to dispose to avoid memory leaks!
	}

	public void draw(Batch batch) {
		font24.draw(batch, "Score: %5d".formatted(score), 10, 620);

		font12.draw(batch, "Planets: " + Planet.planets.size(), 10, 20);
		font12.draw(batch, "Satellites: " + Satellite.satellites.size(), 10, 40);

		if (Main.DEBUG) font12.draw(batch, "FPS: " + (int) (1 / Gdx.graphics.getDeltaTime()), 10, 60);
	}
}
