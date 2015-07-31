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
package games.strategy.kingstable.ui.display;

import java.util.Collection;

import games.strategy.engine.data.GameMap;
import games.strategy.engine.data.Territory;
import games.strategy.engine.display.IDisplayBridge;
import games.strategy.grid.ui.GridGameFrame;
import games.strategy.grid.ui.IGridEndTurnData;
import games.strategy.grid.ui.IGridPlayData;
import games.strategy.grid.ui.display.IGridGameDisplay;

/**
 * Dummy display for a King's Table game, for use in testing.
 *
 * @author Lane Schwartz
 * @version $LastChangedDate$
 */
public class DummyDisplay implements IGridGameDisplay {
  /**
   * @see games.strategy.engine.display.IKingsTableDisplay#performPlay(Territory,Territory,Collection<Territory>)
   */
  @Override
  public void refreshTerritories(final Collection<Territory> territories) {}

  /**
   * @see games.strategy.grid.ui.display.IGridGameDisplay#setGameOver()
   */
  @Override
  public void setGameOver()// CountDownLatch waiting) {
  {}

  /**
   * @see games.strategy.grid.ui.display.IGridGameDisplay#setStatus(String)
   */
  @Override
  public void setStatus(final String status) {}

  /**
   * @see games.strategy.grid.ui.display.IGridGameDisplay#initialize(IDisplayBridge)
   */
  @Override
  public void initialize(final IDisplayBridge bridge) {}

  /**
   * @see games.strategy.grid.ui.display.IGridGameDisplay#shutDown()
   */
  @Override
  public void shutDown() {}

  @Override
  public void initializeGridMapData(final GameMap map) {}

  @Override
  public GridGameFrame getGridGameFrame() {
    return null;
  }

  @Override
  public void showGridPlayDataMove(final IGridPlayData move) {}

  @Override
  public void showGridEndTurnData(final IGridEndTurnData endTurnData) {}
}
