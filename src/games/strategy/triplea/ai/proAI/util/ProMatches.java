/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package games.strategy.triplea.ai.proAI.util;

import games.strategy.engine.data.GameData;
import games.strategy.engine.data.PlayerID;
import games.strategy.engine.data.Territory;
import games.strategy.engine.data.Unit;
import games.strategy.engine.data.UnitType;
import games.strategy.triplea.delegate.AbstractMoveDelegate;
import games.strategy.triplea.delegate.Matches;
import games.strategy.util.CompositeMatchAnd;
import games.strategy.util.CompositeMatchOr;
import games.strategy.util.Match;

import java.util.Collection;
import java.util.List;

public class ProMatches
{
	
	public static Match<Territory> territoryCanLandAirUnits(final PlayerID player, final GameData data, final boolean isCombatMove)
	{
		return new Match<Territory>()
		{
			@Override
			public boolean match(final Territory t)
			{
				final Match<Territory> match = new CompositeMatchAnd<Territory>(Matches.airCanLandOnThisAlliedNonConqueredLandTerritory(player, data),
							Matches.TerritoryIsPassableAndNotRestrictedAndOkByRelationships(player, data, isCombatMove, false, false, true, true));
				return match.match(t);
			}
		};
	}
	
	public static Match<Territory> territoryCanMoveAirUnits(final PlayerID player, final GameData data, final boolean isCombatMove)
	{
		return new Match<Territory>()
		{
			@Override
			public boolean match(final Territory t)
			{
				final Match<Territory> match = new CompositeMatchAnd<Territory>(Matches.territoryDoesNotCostMoneyToEnter(data),
							Matches.TerritoryIsPassableAndNotRestrictedAndOkByRelationships(player, data, isCombatMove, false, false, true, false));
				return match.match(t);
			}
		};
	}
	
	public static Match<Territory> territoryCanMoveAirUnitsAndNoAA(final PlayerID player, final GameData data, final boolean isCombatMove)
	{
		return new Match<Territory>()
		{
			@Override
			public boolean match(final Territory t)
			{
				final Match<Territory> match = new CompositeMatchAnd<Territory>(ProMatches.territoryCanMoveAirUnits(player, data, isCombatMove), Matches.territoryHasEnemyAAforAnything(player, data)
							.invert());
				return match.match(t);
			}
		};
	}
	
	public static Match<Territory> territoryCanMoveLandUnits(final PlayerID player, final GameData data, final boolean isCombatMove)
	{
		return new Match<Territory>()
		{
			@Override
			public boolean match(final Territory t)
			{
				final Match<Territory> match = new CompositeMatchAnd<Territory>(Matches.territoryDoesNotCostMoneyToEnter(data),
							Matches.TerritoryIsPassableAndNotRestrictedAndOkByRelationships(player, data, isCombatMove, true, false, false, false));
				return match.match(t);
			}
		};
	}
	
	public static Match<Territory> territoryCanMoveLandUnitsAndIsAllied(final PlayerID player, final GameData data)
	{
		return new Match<Territory>()
		{
			@Override
			public boolean match(final Territory t)
			{
				final Match<Territory> match = new CompositeMatchAnd<Territory>(Matches.isTerritoryAllied(player, data), territoryCanMoveLandUnits(player, data, false));
				return match.match(t);
			}
		};
	}
	
	public static Match<Territory> territoryCanMoveLandUnitsAndIsEnemy(final PlayerID player, final GameData data)
	{
		return new Match<Territory>()
		{
			@Override
			public boolean match(final Territory t)
			{
				final Match<Territory> match = new CompositeMatchAnd<Territory>(Matches.isTerritoryEnemy(player, data), territoryCanMoveLandUnits(player, data, true));
				return match.match(t);
			}
		};
	}
	
	public static Match<Territory> territoryCanMoveLandUnitsThrough(final PlayerID player, final GameData data, final Unit u, final boolean isCombatMove, final List<Territory> enemyTerritories)
	{
		return new Match<Territory>()
		{
			@Override
			public boolean match(final Territory t)
			{
				Match<Territory> match = new CompositeMatchAnd<Territory>(ProMatches.territoryCanMoveLandUnits(player, data, isCombatMove), Matches.isTerritoryAllied(player, data), Matches
							.territoryIsInList(enemyTerritories).invert());
				if (isCombatMove && Matches.UnitCanBlitz.match(u))
				{
					final Match<Territory> alliedOrBlitzableMatch = new CompositeMatchOr<Territory>(Matches.isTerritoryAllied(player, data), Matches.TerritoryIsBlitzable(player, data));
					match = new CompositeMatchAnd<Territory>(ProMatches.territoryCanMoveLandUnits(player, data, isCombatMove), alliedOrBlitzableMatch, Matches.territoryIsInList(enemyTerritories)
								.invert());
				}
				return match.match(t);
			}
		};
	}
	
	public static Match<Territory> territoryCanMoveSeaUnits(final PlayerID player, final GameData data, final boolean isCombatMove)
	{
		return new Match<Territory>()
		{
			@Override
			public boolean match(final Territory t)
			{
				final Match<Territory> match = new CompositeMatchAnd<Territory>(Matches.territoryDoesNotCostMoneyToEnter(data),
							Matches.TerritoryIsPassableAndNotRestrictedAndOkByRelationships(player, data, isCombatMove, false, true, false, false));
				return match.match(t);
			}
		};
	}
	
	public static Match<Territory> territoryCanMoveSeaUnitsThrough(final PlayerID player, final GameData data, final boolean isCombatMove)
	{
		return new Match<Territory>()
		{
			@Override
			public boolean match(final Territory t)
			{
				final Match<Territory> match = new CompositeMatchAnd<Territory>(territoryCanMoveSeaUnits(player, data, isCombatMove), Matches.territoryHasNoEnemyUnits(player, data));
				return match.match(t);
			}
		};
	}
	
	public static Match<Territory> territoryHasEnemyUnitsOrCantBeHeld(final PlayerID player, final GameData data, final List<Territory> territoriesThatCantBeHeld)
	{
		return new Match<Territory>()
		{
			@Override
			public boolean match(final Territory t)
			{
				final Match<Territory> match = new CompositeMatchOr<Territory>(Matches.territoryHasEnemyUnits(player, data), Matches.territoryIsInList(territoriesThatCantBeHeld));
				return match.match(t);
			}
		};
	}
	
	public static Match<Territory> territoryHasInfraFactoryAndIsEnemyLand(final PlayerID player, final GameData data)
	{
		return new Match<Territory>()
		{
			@Override
			public boolean match(final Territory t)
			{
				final Match<Unit> infraFactoryMatch = new CompositeMatchAnd<Unit>(Matches.enemyUnit(player, data), Matches.UnitCanProduceUnits, Matches.UnitIsInfrastructure);
				final Match<Territory> match = new CompositeMatchAnd<Territory>(Matches.isTerritoryEnemy(player, data), Matches.TerritoryIsLand, Matches.territoryHasUnitsThatMatch(infraFactoryMatch));
				return match.match(t);
			}
		};
	}
	
	public static Match<Territory> territoryHasInfraFactoryAndIsLand(final PlayerID player)
	{
		return new Match<Territory>()
		{
			@Override
			public boolean match(final Territory t)
			{
				final Match<Unit> infraFactoryMatch = new CompositeMatchAnd<Unit>(Matches.UnitCanProduceUnits, Matches.UnitIsInfrastructure);
				final Match<Territory> match = new CompositeMatchAnd<Territory>(Matches.TerritoryIsLand, Matches.territoryHasUnitsThatMatch(infraFactoryMatch));
				return match.match(t);
			}
		};
	}
	
	public static Match<Territory> territoryHasInfraFactoryAndIsNotConqueredOwnedLand(final PlayerID player, final GameData data)
	{
		return new Match<Territory>()
		{
			@Override
			public boolean match(final Territory t)
			{
				final Match<Territory> match = new CompositeMatchAnd<Territory>(territoryIsNotConqueredOwnedLand(player, data), territoryHasInfraFactoryAndIsOwnedLand(player));
				return match.match(t);
			}
		};
	}
	
	public static Match<Territory> territoryHasInfraFactoryAndIsOwnedLand(final PlayerID player)
	{
		return new Match<Territory>()
		{
			@Override
			public boolean match(final Territory t)
			{
				final Match<Unit> infraFactoryMatch = new CompositeMatchAnd<Unit>(Matches.unitIsOwnedBy(player), Matches.UnitCanProduceUnits, Matches.UnitIsInfrastructure);
				final Match<Territory> match = new CompositeMatchAnd<Territory>(Matches.isTerritoryOwnedBy(player), Matches.TerritoryIsLand, Matches.territoryHasUnitsThatMatch(infraFactoryMatch));
				return match.match(t);
			}
		};
	}
	
	public static Match<Territory> territoryHasInfraFactoryAndIsOwnedLandAdjacentToSea(final PlayerID player, final GameData data)
	{
		return new Match<Territory>()
		{
			@Override
			public boolean match(final Territory t)
			{
				final Match<Territory> match = new CompositeMatchAnd<Territory>(territoryHasInfraFactoryAndIsOwnedLand(player), Matches.territoryHasNeighborMatching(data, Matches.TerritoryIsWater));
				return match.match(t);
			}
		};
	}
	
	public static Match<Territory> territoryHasNoInfraFactoryAndIsNotConqueredOwnedLand(final PlayerID player, final GameData data)
	{
		return new Match<Territory>()
		{
			@Override
			public boolean match(final Territory t)
			{
				final Match<Territory> match = new CompositeMatchAnd<Territory>(territoryIsNotConqueredOwnedLand(player, data), territoryHasInfraFactoryAndIsOwnedLand(player).invert());
				return match.match(t);
			}
		};
	}
	
	public static Match<Territory> territoryIsEnemyNotNeutral(final PlayerID player, final GameData data)
	{
		return new Match<Territory>()
		{
			@Override
			public boolean match(final Territory t)
			{
				final Match<Territory> match = new CompositeMatchAnd<Territory>(Matches.isTerritoryEnemy(player, data), Matches.TerritoryIsNeutralButNotWater.invert());
				return match.match(t);
			}
		};
	}
	
	public static Match<Territory> territoryIsEnemyOrCantBeHeld(final PlayerID player, final GameData data, final List<Territory> territoriesThatCantBeHeld)
	{
		return new Match<Territory>()
		{
			@Override
			public boolean match(final Territory t)
			{
				final Match<Territory> match = new CompositeMatchOr<Territory>(Matches.isTerritoryEnemy(player, data), Matches.territoryIsInList(territoriesThatCantBeHeld));
				return match.match(t);
			}
		};
	}
	
	public static Match<Territory> territoryIsEnemyOrCantBeHeldAndIsAdjacentToMyLandUnits(final PlayerID player, final GameData data, final List<Territory> territoriesThatCantBeHeld)
	{
		return new Match<Territory>()
		{
			@Override
			public boolean match(final Territory t)
			{
				final Match<Unit> myUnitIsLand = new CompositeMatchAnd<Unit>(Matches.unitIsOwnedBy(player), Matches.UnitIsLand);
				final Match<Territory> territoryIsLandAndAdjacentToMyLandUnits = new CompositeMatchAnd<Territory>(Matches.TerritoryIsLand, Matches.territoryHasNeighborMatching(data,
							Matches.territoryHasUnitsThatMatch(myUnitIsLand)));
				final Match<Territory> match = new CompositeMatchAnd<Territory>(territoryIsLandAndAdjacentToMyLandUnits, territoryIsEnemyOrCantBeHeld(player, data, territoriesThatCantBeHeld));
				return match.match(t);
			}
		};
	}
	
	public static Match<Territory> territoryIsNotConqueredAlliedLand(final PlayerID player, final GameData data)
	{
		return new Match<Territory>()
		{
			@Override
			public boolean match(final Territory t)
			{
				if (AbstractMoveDelegate.getBattleTracker(data).wasConquered(t))
					return false;
				final Match<Territory> match = new CompositeMatchAnd<Territory>(Matches.isTerritoryAllied(player, data), Matches.TerritoryIsLand);
				return match.match(t);
			}
		};
	}
	
	public static Match<Territory> territoryIsNotConqueredOwnedLand(final PlayerID player, final GameData data)
	{
		return new Match<Territory>()
		{
			@Override
			public boolean match(final Territory t)
			{
				if (AbstractMoveDelegate.getBattleTracker(data).wasConquered(t))
					return false;
				final Match<Territory> match = new CompositeMatchAnd<Territory>(Matches.isTerritoryOwnedBy(player), Matches.TerritoryIsLand);
				return match.match(t);
			}
		};
	}
	
	public static Match<Unit> unitCanBeMovedAndIsOwned(final PlayerID player)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				final Match<Unit> match = new CompositeMatchAnd<Unit>(Matches.unitIsOwnedBy(player), Matches.unitHasMovementLeft);
				return match.match(u);
			}
		};
	}
	
	public static Match<Unit> unitCanBeMovedAndIsOwnedAir(final PlayerID player, final boolean isCombatMove)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				if (isCombatMove && Matches.UnitCanNotMoveDuringCombatMove.match(u))
					return false;
				final Match<Unit> match = new CompositeMatchAnd<Unit>(unitCanBeMovedAndIsOwned(player), Matches.UnitIsAir);
				return match.match(u);
			}
		};
	}
	
	public static Match<Unit> unitCanBeMovedAndIsOwnedLand(final PlayerID player, final boolean isCombatMove)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				if (isCombatMove && Matches.UnitCanNotMoveDuringCombatMove.match(u))
					return false;
				final Match<Unit> match = new CompositeMatchAnd<Unit>(unitCanBeMovedAndIsOwned(player), Matches.UnitIsLand, Matches.unitIsBeingTransported().invert());
				return match.match(u);
			}
		};
	}
	
	public static Match<Unit> unitCanBeMovedAndIsOwnedSea(final PlayerID player, final boolean isCombatMove)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				if (isCombatMove && Matches.UnitCanNotMoveDuringCombatMove.match(u))
					return false;
				final Match<Unit> match = new CompositeMatchAnd<Unit>(unitCanBeMovedAndIsOwned(player), Matches.UnitIsSea);
				return match.match(u);
			}
		};
	}
	
	public static Match<Unit> unitCanBeMovedAndIsOwnedTransport(final PlayerID player, final boolean isCombatMove)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				if (isCombatMove && Matches.UnitCanNotMoveDuringCombatMove.match(u))
					return false;
				final Match<Unit> match = new CompositeMatchAnd<Unit>(unitCanBeMovedAndIsOwned(player), Matches.UnitIsTransport);
				return match.match(u);
			}
		};
	}
	
	public static Match<Unit> unitCantBeMovedAndIsAlliedDefender(final PlayerID player, final GameData data)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				final Match<Unit> myUnitHasNoMovementMatch = new CompositeMatchAnd<Unit>(Matches.unitIsOwnedBy(player), Matches.unitHasMovementLeft.invert(), Matches.UnitIsNotInfrastructure);
				final Match<Unit> alliedUnitMatch = new CompositeMatchAnd<Unit>(Matches.unitIsOwnedBy(player).invert(), Matches.isUnitAllied(player, data), Matches.UnitIsNotInfrastructure);
				final Match<Unit> match = new CompositeMatchOr<Unit>(myUnitHasNoMovementMatch, alliedUnitMatch);
				return match.match(u);
			}
		};
	}
	
	public static Match<Unit> unitIsAlliedLand(final PlayerID player, final GameData data)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				final Match<Unit> match = new CompositeMatchAnd<Unit>(Matches.UnitIsLand, Matches.isUnitAllied(player, data));
				return match.match(u);
			}
		};
	}
	
	public static Match<Unit> unitIsAlliedLandAndNotInfra(final PlayerID player, final GameData data)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				final Match<Unit> match = new CompositeMatchAnd<Unit>(unitIsAlliedLand(player, data), Matches.UnitIsNotInfrastructure);
				return match.match(u);
			}
		};
	}
	
	public static Match<Unit> unitIsAlliedNotLand(final PlayerID player, final GameData data)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				final Match<Unit> match = new CompositeMatchAnd<Unit>(Matches.UnitIsNotLand, Matches.isUnitAllied(player, data));
				return match.match(u);
			}
		};
	}
	
	public static Match<Unit> unitIsEnemyAir(final PlayerID player, final GameData data)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				final Match<Unit> match = new CompositeMatchAnd<Unit>(Matches.enemyUnit(player, data), Matches.UnitIsAir);
				return match.match(u);
			}
		};
	}
	
	public static Match<Unit> unitIsEnemyAndNotAA(final PlayerID player, final GameData data)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				final Match<Unit> match = new CompositeMatchAnd<Unit>(Matches.enemyUnit(player, data), Matches.UnitIsAAforAnything.invert());
				return match.match(u);
			}
		};
	}
	
	public static Match<Unit> unitIsEnemyLand(final PlayerID player, final GameData data)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				final Match<Unit> match = new CompositeMatchAnd<Unit>(Matches.enemyUnit(player, data), Matches.UnitIsLand);
				return match.match(u);
			}
		};
	}
	
	public static Match<Unit> unitIsEnemyNotLand(final PlayerID player, final GameData data)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				final Match<Unit> match = new CompositeMatchAnd<Unit>(Matches.enemyUnit(player, data), Matches.UnitIsNotLand);
				return match.match(u);
			}
		};
	}
	
	public static Match<Unit> unitIsEnemyNotNeutralLand(final PlayerID player, final GameData data)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				final Match<Unit> match = new CompositeMatchAnd<Unit>(Matches.enemyUnit(player, data), Matches.UnitIsLand, unitIsNeutral().invert());
				return match.match(u);
			}
		};
	}
	
	public static Match<Unit> unitIsEnemySea(final PlayerID player, final GameData data)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				final Match<Unit> match = new CompositeMatchAnd<Unit>(Matches.enemyUnit(player, data), Matches.UnitIsSea);
				return match.match(u);
			}
		};
	}
	
	public static Match<Unit> unitIsNeutral()
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				if (u.getOwner().isNull())
					return true;
				return false;
			}
		};
	}
	
	public static Match<Unit> unitIsOwnedAndMatchesTypeAndIsTransporting(final PlayerID player, final UnitType unitType)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				final Match<Unit> match = new CompositeMatchAnd<Unit>(Matches.unitIsOwnedBy(player), Matches.unitIsOfType(unitType), Matches.unitIsTransporting());
				return match.match(u);
			}
		};
	}
	
	public static Match<Unit> unitIsOwnedAndMatchesTypeAndNotTransporting(final PlayerID player, final UnitType unitType)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				final Match<Unit> match = new CompositeMatchAnd<Unit>(Matches.unitIsOwnedBy(player), Matches.unitIsOfType(unitType), Matches.unitIsTransporting().invert());
				return match.match(u);
			}
		};
	}
	
	public static Match<Unit> unitIsOwnedLand(final PlayerID player)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				final Match<Unit> match = new CompositeMatchAnd<Unit>(Matches.unitIsOwnedBy(player), Matches.UnitIsLand);
				return match.match(u);
			}
		};
	}
	
	public static Match<Unit> unitIsOwnedTransport(final PlayerID player)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				final Match<Unit> match = new CompositeMatchAnd<Unit>(Matches.unitIsOwnedBy(player), Matches.UnitIsTransport);
				return match.match(u);
			}
		};
	}
	
	public static Match<Unit> unitIsOwnedTransportableUnit(final PlayerID player)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				final Match<Unit> match = new CompositeMatchAnd<Unit>(Matches.unitIsOwnedBy(player), Matches.UnitCanBeTransported);
				return match.match(u);
			}
		};
	}
	
	public static Match<Unit> unitIsOwnedTransportableUnitAndCanBeLoaded(final PlayerID player, final boolean isCombatMove)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit u)
			{
				if (isCombatMove && Matches.UnitCanNotMoveDuringCombatMove.match(u))
					return false;
				final Match<Unit> match = new CompositeMatchAnd<Unit>(unitIsOwnedTransportableUnit(player), Matches.unitHasNotMoved, Matches.unitIsBeingTransported().invert());
				return match.match(u);
			}
		};
	}
	
	/**
	 * Check what units a territory can produce.
	 * 
	 * @param t
	 *            territory we are testing for required units
	 * @return whether the territory contains one of the required combos of units
	 */
	public static Match<Unit> unitWhichRequiresUnitsHasRequiredUnits(final PlayerID player, final Territory t)
	{
		return new Match<Unit>()
		{
			@Override
			public boolean match(final Unit unitWhichRequiresUnits)
			{
				if (!Matches.UnitRequiresUnitsOnCreation.match(unitWhichRequiresUnits))
					return true;
				final Collection<Unit> unitsAtStartOfTurnInProducer = t.getUnits().getUnits();
				if (Matches.UnitWhichRequiresUnitsHasRequiredUnitsInList(unitsAtStartOfTurnInProducer).match(unitWhichRequiresUnits))
					return true;
				if (t.isWater() && Matches.UnitIsSea.match(unitWhichRequiresUnits))
				{
					for (final Territory neighbor : t.getData().getMap().getNeighbors(t, Matches.TerritoryIsLand))
					{
						final Collection<Unit> unitsAtStartOfTurnInCurrent = neighbor.getUnits().getUnits();
						if (Matches.UnitWhichRequiresUnitsHasRequiredUnitsInList(unitsAtStartOfTurnInCurrent).match(unitWhichRequiresUnits))
							return true;
					}
				}
				return false;
			}
		};
	}
	
}
