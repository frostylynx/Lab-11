package edu.ufl.cise.cs1.controllers;

import game.controllers.AttackerController;
import game.models.*;
import java.util.List;

public final class StudentAttackerController implements AttackerController {
	public void init(Game game) { //start

	}

	public void shutdown(Game game) {//end

	}

	public static Node betterGetTarget(List<Node> node, Game game) {//Rob's alternative method to getTargetNode on Slack
		if (node.size() == 0) {
			return null;
		}
		int minDistance = Integer.MAX_VALUE;
		int minIndex = 0;
		for (int i = 0; i < node.size(); i++) {
			int currentDistance = node.get(i).getPathDistance(game.getAttacker().getLocation());
			if (currentDistance < minDistance) {
				minDistance = node.get(i).getPathDistance(game.getAttacker().getLocation());
				minIndex = i;
			}
		}
		return node.get(minIndex);
	}

	public int update(Game game, long timeDue) {
		int action = -1;

		Attacker attacker = game.getAttacker();
		List<Node> regularPills = game.getPillList();
		List<Node> powerPills = game.getPowerPillList();
		Node attackerLocation = attacker.getLocation();
		List<Defender> defenders = game.getDefenders();

		Node closestPowerPill = betterGetTarget(powerPills, game); //gets the location of the closest power pill
		Node closestRegularPill = betterGetTarget(regularPills, game); //gets the location of the closest regular pill

		int[] distances = new int[4];
		for (int i = 0; i < 4; i++) {
			distances[i] = attacker.getLocation().getPathDistance(defenders.get(i).getLocation());
			if (distances[i] == -1)
				distances[i] = Integer.MAX_VALUE;
		}

		int minimumIndex = 0;
		int secondIndex = 1;
		for (int i = 0; i < 4; i++) {
			if (distances[i] < distances[minimumIndex] && distances[i] > 0) {
				minimumIndex = i;
			}
		}
		for (int i = 0; i < 3; i++) {
			if (distances[i] < distances[secondIndex] && distances[i] > 0) {
				secondIndex = i + 1;
			}
		}

		Defender closestDefender = defenders.get(minimumIndex);
		Defender secondClosestDefender = defenders.get(secondIndex);

		attacker.getPossibleDirs(true);

		if (closestDefender.isVulnerable() == false) {//no power pill consumed yet
			attacker.getPossibleDirs(true);
			attacker.getPossibleLocations(true);

			if (closestDefender.getLocation().getPathDistance(attacker.getLocation()) < 15 &&
					closestDefender.getLocation().getPathDistance(attacker.getLocation()) != -1) { //if the closest defender is not in lair
				action = attacker.getNextDir(closestDefender.getLocation(), false);//runs away

				if (secondClosestDefender.getLocation().getPathDistance(attacker.getLocation())
						== closestDefender.getLocation().getPathDistance(attacker.getLocation()) &&
						secondClosestDefender.getLocation().getPathDistance(attacker.getLocation()) != -1
						&& secondClosestDefender.getLocation().getPathDistance(attacker.getLocation()) < 25) {
					action = attacker.getNextDir(secondClosestDefender.getLocation(), false);
				}
			} else if (closestPowerPill != null && secondClosestDefender.isVulnerable() == false &&
					(closestDefender.getLocation().getPathDistance(attacker.getLocation()) < 55 &&
							closestDefender.getLocation().getPathDistance(attacker.getLocation()) != -1 &&
							secondClosestDefender.getLocation().getPathDistance(attacker.getLocation()) < 150)) {//if there's at least 1 power pill. goes to the power pill
				action = attacker.getNextDir(closestPowerPill, true); //moves towards the closest power pill

			} else {
				action = attacker.getNextDir(closestRegularPill, true);
			}
		} else if (closestDefender.isVulnerable() == true) { //no power pills, defender vulnerable
			attacker.getPossibleDirs(true);
			attacker.getPossibleLocations(true);
			action = attacker.getNextDir(closestDefender.getLocation(), true); //moves towards the closest defender

		} else if (closestDefender.isVulnerable() == false) {
			//defender not vulnerable, no more power pills
			attacker.getPossibleDirs(true);
			attacker.getPossibleLocations(true);
			action = attacker.getNextDir(closestRegularPill, true); //default
		} else {
			action = attacker.getNextDir(closestDefender.getLocation(), true);


			//Chooses a random LEGAL action if required.
			List<Integer> possibleDirs = game.getAttacker().getPossibleDirs(true);
			// UP = 0, RIGHT = 1, DOWN = 2, LEFT = 3, EMPTY = -1
			if (possibleDirs.size() != 0)
				action = possibleDirs.get(Game.rng.nextInt(possibleDirs.size()));

			else
				action = -1;
		}

			return action;
		}

}