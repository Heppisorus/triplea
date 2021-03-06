package games.strategy.engine.delegate;

import java.util.Properties;

import games.strategy.engine.GameOverException;
import games.strategy.engine.data.Change;
import games.strategy.engine.data.CompositeChange;
import games.strategy.engine.data.GameData;
import games.strategy.engine.data.PlayerID;
import games.strategy.engine.display.IDisplay;
import games.strategy.engine.framework.AbstractGame;
import games.strategy.engine.framework.IGame;
import games.strategy.engine.framework.ServerGame;
import games.strategy.engine.gamePlayer.IRemotePlayer;
import games.strategy.engine.history.IDelegateHistoryWriter;
import games.strategy.engine.message.MessengerException;
import games.strategy.engine.random.IRandomSource;
import games.strategy.engine.random.IRandomStats.DiceType;
import games.strategy.engine.random.RandomStats;
import games.strategy.sound.ISound;

/**
 * Default implementation of DelegateBridge.
 */
public class DefaultDelegateBridge implements IDelegateBridge {
  private final GameData gameData;
  private final IGame game;
  private final IDelegateHistoryWriter historyWriter;
  private final RandomStats randomStats;
  private final DelegateExecutionManager delegateExecutionManager;
  private IRandomSource randomSource;

  public DefaultDelegateBridge(final GameData data, final IGame game, final IDelegateHistoryWriter historyWriter,
      final RandomStats randomStats, final DelegateExecutionManager delegateExecutionManager) {
    gameData = data;
    this.game = game;
    this.historyWriter = historyWriter;
    this.randomStats = randomStats;
    this.delegateExecutionManager = delegateExecutionManager;
  }

  @Override
  public GameData getData() {
    return gameData;
  }

  @Override
  public PlayerID getPlayerId() {
    return gameData.getSequence().getStep().getPlayerId();
  }

  public void setRandomSource(final IRandomSource randomSource) {
    this.randomSource = randomSource;
  }

  /**
   * All delegates should use random data that comes from both players so that
   * neither player cheats.
   */
  @Override
  public int getRandom(final int max, final PlayerID player, final DiceType diceType, final String annotation)
      throws IllegalArgumentException, IllegalStateException {
    final int random = randomSource.getRandom(max, annotation);
    randomStats.addRandom(random, player, diceType);
    return random;
  }

  @Override
  public int[] getRandom(final int max, final int count, final PlayerID player, final DiceType diceType,
      final String annotation) throws IllegalArgumentException, IllegalStateException {
    final int[] randomValues = randomSource.getRandom(max, count, annotation);
    randomStats.addRandom(randomValues, player, diceType);
    return randomValues;
  }

  @Override
  public void addChange(final Change change) {
    if (change instanceof CompositeChange) {
      final CompositeChange c = (CompositeChange) change;
      if (c.getChanges().size() == 1) {
        addChange(c.getChanges().get(0));
        return;
      }
    }
    if (!change.isEmpty()) {
      game.addChange(change);
    }
  }

  @Override
  public String getStepName() {
    return gameData.getSequence().getStep().getName();
  }

  @Override
  public IDelegateHistoryWriter getHistoryWriter() {
    return historyWriter;
  }

  private Object getOutbound(final Object o) {
    final Class<?>[] interfaces = o.getClass().getInterfaces();
    return delegateExecutionManager.createOutboundImplementation(o, interfaces);
  }

  @Override
  public IRemotePlayer getRemotePlayer() {
    return getRemotePlayer(getPlayerId());
  }

  @Override
  public IRemotePlayer getRemotePlayer(final PlayerID id) {
    try {
      final Object implementor = game.getRemoteMessenger().getRemote(ServerGame.getRemoteName(id, gameData));
      return (IRemotePlayer) getOutbound(implementor);
    } catch (final MessengerException me) {
      throw new GameOverException("Game Over!");
    }
  }

  @Override
  public IDisplay getDisplayChannelBroadcaster() {
    final Object implementor =
        game.getChannelMessenger().getChannelBroadcastor(AbstractGame.getDisplayChannel(gameData));
    return (IDisplay) getOutbound(implementor);
  }

  @Override
  public ISound getSoundChannelBroadcaster() {
    final Object implementor = game.getChannelMessenger().getChannelBroadcastor(AbstractGame.getSoundChannel(gameData));
    return (ISound) getOutbound(implementor);
  }

  @Override
  public Properties getStepProperties() {
    return gameData.getSequence().getStep().getProperties();
  }

  @Override
  public void leaveDelegateExecution() {
    delegateExecutionManager.leaveDelegateExecution();
  }

  @Override
  public void enterDelegateExecution() {
    delegateExecutionManager.enterDelegateExecution();
  }

  @Override
  public void stopGameSequence() {
    ((ServerGame) game).stopGameSequence();
  }
}
