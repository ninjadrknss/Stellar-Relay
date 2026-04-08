package com.stellar.relay;

public interface Pathable {
	enum State {
		NONE,
		HOVERED,
		SELECTED
	}

	float getCX();

	float getCY();

	State getState();

	void setState(State state);

	static Pathable getClosestPathable(float x, float y) {
		Pathable closest = null;
		float closestDist = Float.MAX_VALUE;

		for (Planet planet : Planet.planets) {
			float dist =
					(float) Math.sqrt(Math.pow(planet.getCX() - x, 2) + Math.pow(planet.getCY() - y, 2));
			if (dist < closestDist) {
				closestDist = dist;
				closest = planet;
			}
		}

		for (Satellite satellite : Satellite.satellites) {
			float dist =
					(float)
							Math.sqrt(Math.pow(satellite.getCX() - x, 2) + Math.pow(satellite.getCY() - y, 2));
			if (dist < closestDist) {
				closestDist = dist;
				closest = satellite;
			}
		}

		return closest;
	}
}
