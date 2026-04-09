package com.stellar.relay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class Confetti {
	private static class Particle {
		float x, y;
		float vx, vy;
		float life;
		float maxLife;
		float rotation;
		float rotationSpeed;
		Color color;
		float size;
	}

	private static final Array<Particle> particles = new Array<>();

	private static final float gravity = -300f;
	private static final int burstCount = 40;

	public static void spawn(float x, float y) {
		if (Main.DEBUG) System.out.println("spawning confetti at " + x + ", " + y);
		for (int i = 0; i < burstCount; i++) {
			Particle p = new Particle();

			p.x = x;
			p.y = y;

			float angle = MathUtils.random(20f, 160f);
			float speed = MathUtils.random(75f, 250f);

			p.vx = MathUtils.cosDeg(angle) * speed;
			p.vy = MathUtils.sinDeg(angle) * speed;

			p.life = 0f;
			p.maxLife = MathUtils.random(0.5f, 1.5f);

			p.rotation = MathUtils.random(0f, 360f);
			p.rotationSpeed = MathUtils.random(-180f, 180f);

			p.size = MathUtils.random(4f, 8f);

			p.color = new Color(MathUtils.random(), MathUtils.random(), MathUtils.random(), 1f);

			particles.add(p);
		}
	}

	public static void update(float delta) {
		for (int i = particles.size - 1; i >= 0; i--) {
			Particle p = particles.get(i);

			p.life += delta;

			if (p.life > p.maxLife) {
				particles.removeIndex(i);
				continue;
			}

			// Physics
			p.vy += gravity * delta;

			p.x += p.vx * delta;
			p.y += p.vy * delta;

			p.rotation += p.rotationSpeed * delta;
		}
	}

	public static void draw(ShapeRenderer shapeRenderer) {
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		for (Particle p : particles) {
			float alpha = 1f - (p.life / p.maxLife);
			shapeRenderer.setColor(p.color.r, p.color.g, p.color.b, alpha);

			// Draw rotated rectangle
			shapeRenderer.rect(p.x, p.y, p.size / 2f, p.size / 2f, p.size, p.size, 1f, 1f, p.rotation);
		}

		shapeRenderer.end();
	}
}
