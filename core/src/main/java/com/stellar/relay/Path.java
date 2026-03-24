package com.stellar.relay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;

public class Path {
	public static final float MAX_CONNECTION_LENGTH = 200;
	public static final int WIDTH = 4;

	private final ArrayList<Pathable> path = new ArrayList<>();
	private boolean isComplete = false;
	private final Message message;
	private final Planet source;
	private final Planet destination;

	public static final ArrayList<Path> paths = new ArrayList<>();

	public Controller controller = null; // The controller that is currently pathing this path, if any

	public Path(Message message, Planet source, Planet destination) {
		this.message = message;
		this.source = source;
		path.add(source);
		this.destination = destination;
		paths.add(this);
	}

	private void draw(ShapeRenderer shapeRenderer) {
		if (path.isEmpty()) return;
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.WHITE);

		for (int i = 0; i < path.size() - 1; i++) {
			Pathable p1 = path.get(i);
			Pathable p2 = path.get(i + 1);
			shapeRenderer.rectLine(p1.getCX(), p1.getCY(), p2.getCX(), p2.getCY(), WIDTH);
		}

		// Draw a line from the last element in the path to the destination if a controller is currently
		// pathing this path
		if (controller != null && !isComplete) {
			Pathable last = path.getLast();

			if (controller.lastPathable == null || !valid(controller.lastPathable)) {
				float dist =
						(float)
								Math.hypot(controller.getCX() - last.getCX(), controller.getCY() - last.getCY());

				dist =
						Math.min(
								dist,
								MAX_CONNECTION_LENGTH); // Limit the line length to the max interaction distance
				float angle =
						(float)
								Math.atan2(controller.getCY() - last.getCY(), controller.getCX() - last.getCX());

				float x = last.getCX() + dist * (float) Math.cos(angle);
				float y = last.getCY() + dist * (float) Math.sin(angle);

				shapeRenderer.rectLine(last.getCX(), last.getCY(), x, y, WIDTH);
			} else {
				shapeRenderer.rectLine(
						last.getCX(),
						last.getCY(),
						controller.lastPathable.getCX(),
						controller.lastPathable.getCY(),
						WIDTH);
			}
		}
		shapeRenderer.end();
	}

	public Vector2 positionAlongPath(float progress) {
		if (path.isEmpty()) return new Vector2(0, 0);
		if (progress <= 0) return new Vector2(path.getFirst().getCX(), path.getFirst().getCY());
		if (progress >= 1) return new Vector2(path.getLast().getCX(), path.getLast().getCY());

		float totalDistance = 0;
		float[] segmentDistances = new float[path.size() - 1];
		for (int i = 0; i < path.size() - 1; i++) {
			Pathable p1 = path.get(i);
			Pathable p2 = path.get(i + 1);
			segmentDistances[i] = Vector2.dst(p1.getCX(), p1.getCY(), p2.getCX(), p2.getCY());
			totalDistance += segmentDistances[i];
		}

		float distanceAlongPath = progress * totalDistance;
		float distanceCovered = 0;
		int segmentIndex = 0;
		while (segmentIndex < segmentDistances.length
				&& distanceCovered + segmentDistances[segmentIndex] < distanceAlongPath) {
			distanceCovered += segmentDistances[segmentIndex];
			segmentIndex++;
		}

		if (segmentIndex >= segmentDistances.length) {
			return new Vector2(path.getLast().getCX(), path.getLast().getCY());
		}

		Pathable p1 = path.get(segmentIndex);
		Pathable p2 = path.get(segmentIndex + 1);
		float segmentProgress = (distanceAlongPath - distanceCovered) / segmentDistances[segmentIndex];
		float x = p1.getCX() + (p2.getCX() - p1.getCX()) * segmentProgress;
		float y = p1.getCY() + (p2.getCY() - p1.getCY()) * segmentProgress;

		return new Vector2(x, y);
	}

	public static void drawAll(ShapeRenderer shapeRenderer) {
		paths.forEach((path) -> path.draw(shapeRenderer));
	}

	public void add(Pathable p) {
		if (!path.contains(p)
				&& !isComplete
				&& (p instanceof Satellite s && s.path == null || p.equals(destination))) {
			if (p instanceof Satellite s) s.path = this;
			path.add(p);
		} else {
			System.out.println("Invalid path element: " + p);
		}

		if (p == destination) {
			p.setState(Pathable.State.NONE);
			isComplete = true;
		}
	}

	public boolean remove() {
		isComplete = false; // Mark the path as incomplete if an element is removed
		if (path.size() > 1) {
			Pathable p = path.removeLast();
			if (p instanceof Satellite s) s.path = null;
		} else {
			drop(); // If there are only an element left (the source planet), drop the path entirely
			return true;
		}
		return false;
	}

	public void pickup(Controller controller) {
		this.controller = controller;
		source.setState(Planet.State.SELECTED);
	}

	public void drop() {
		controller = null;
		source.setState(Planet.State.NONE);
	}

	public boolean isComplete() {
		return isComplete;
	}

	public Message message() {
		return message;
	}

	public void delete() {
		paths.remove(this);
	}

	public boolean valid(Pathable p) {
		float dist =
				(float) Math.hypot(p.getCX() - path.getLast().getCX(), p.getCY() - path.getLast().getCY());
		if (dist > MAX_CONNECTION_LENGTH)
			return false; // too far away from the last element in the path

		if (path.contains(p) || isComplete)
			return false; // is not already in the path or the path is already complete

		if (p instanceof Planet planet && !destination.equals(planet))
			return false; // is not a planet other than the destination

		if (p instanceof Satellite satellite && satellite.path != null)
			return false; // is not a satellite that is already part of another path

		return true;
	}
}
