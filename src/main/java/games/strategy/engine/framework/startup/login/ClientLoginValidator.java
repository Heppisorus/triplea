package games.strategy.engine.framework.startup.login;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;

import games.strategy.debug.ClientLogger;
import games.strategy.engine.ClientContext;
import games.strategy.net.ILoginValidator;
import games.strategy.net.IServerMessenger;
import games.strategy.util.MD5Crypt;
import games.strategy.util.ThreadUtil;
import games.strategy.util.Version;

/**
 * The server side of the peer-to-peer network game authentication protocol.
 *
 * <p>
 * In the peer-to-peer network game authentication protocol, the server sends a challenge to the client. Upon receiving
 * the client's response, the server determines if the client knows the game password and gives them access to the game
 * if authentication is successful.
 * </p>
 */
public final class ClientLoginValidator implements ILoginValidator {
  static final String PASSWORD_REQUIRED_PROPERTY = "Password Required";

  @VisibleForTesting
  interface ErrorMessages {
    String NO_ERROR = null;
    String INVALID_MAC = "Invalid mac address";
    String INVALID_PASSWORD = "Invalid password";
    String UNABLE_TO_OBTAIN_MAC = "Unable to obtain mac address";
    String YOU_HAVE_BEEN_BANNED = "The host has banned you from this game";
  }

  private final IServerMessenger serverMessenger;
  private String password;

  public ClientLoginValidator(final IServerMessenger serverMessenger) {
    this.serverMessenger = serverMessenger;
  }

  /**
   * Set the password required for the game, or to null if no password is required.
   */
  public void setGamePassword(final String password) {
    // TODO do not store the plain password, but the hash instead in the next incompatible release
    this.password = password;
  }

  @Override
  public Map<String, String> getChallengeProperties(final String userName, final SocketAddress remoteAddress) {
    final Map<String, String> challenge = new HashMap<>();

    challenge.put("Sever Version", ClientContext.engineVersion().toString());

    if (password != null) {
      challenge.put(PASSWORD_REQUIRED_PROPERTY, Boolean.TRUE.toString());
      challenge.putAll(V1Authenticator.newChallenge());
      challenge.putAll(V2Authenticator.newChallenge());
    } else {
      challenge.put(PASSWORD_REQUIRED_PROPERTY, Boolean.FALSE.toString());
    }

    return challenge;
  }

  @Override
  public String verifyConnection(
      final Map<String, String> propertiesSentToClient,
      final Map<String, String> propertiesReadFromClient,
      final String clientName,
      final String hashedMac,
      final SocketAddress remoteAddress) {
    final String versionString = propertiesReadFromClient.get(ClientLogin.ENGINE_VERSION_PROPERTY);
    if (versionString == null || versionString.length() > 20 || versionString.trim().length() == 0) {
      return "Invalid version " + versionString;
    }

    // check for version
    final Version clientVersion = new Version(versionString);
    if (!ClientContext.engineVersion().equals(clientVersion, false)) {
      return "Client is using " + clientVersion + " but server requires version " + ClientContext.engineVersion();
    }

    final String realName = clientName.split(" ")[0];
    if (serverMessenger.isUsernameMiniBanned(realName)) {
      return ErrorMessages.YOU_HAVE_BEEN_BANNED;
    }

    final String remoteIp = ((InetSocketAddress) remoteAddress).getAddress().getHostAddress();
    if (serverMessenger.isIpMiniBanned(remoteIp)) {
      return ErrorMessages.YOU_HAVE_BEEN_BANNED;
    }

    if (hashedMac == null) {
      return ErrorMessages.UNABLE_TO_OBTAIN_MAC;
    } else if (hashedMac.length() != 28
        || !hashedMac.startsWith(MD5Crypt.MAGIC + "MH$")
        || !hashedMac.matches("[0-9a-zA-Z$./]+")) {
      // Must have been tampered with
      return ErrorMessages.INVALID_MAC;
    } else if (serverMessenger.isMacMiniBanned(hashedMac)) {
      return ErrorMessages.YOU_HAVE_BEEN_BANNED;
    }

    if (Boolean.TRUE.toString().equals(propertiesSentToClient.get(PASSWORD_REQUIRED_PROPERTY))) {
      final String errorMessage = authenticate(propertiesSentToClient, propertiesReadFromClient);
      if (errorMessage != ErrorMessages.NO_ERROR) {
        // sleep on average 2 seconds
        // try to prevent flooding to guess the password
        ThreadUtil.sleep((int) (4_000 * Math.random()));
        return errorMessage;
      }
    }

    return ErrorMessages.NO_ERROR;
  }

  @VisibleForTesting
  String authenticate(final Map<String, String> challenge, final Map<String, String> response) {
    try {
      if (V2Authenticator.canProcessResponse(response)) {
        V2Authenticator.authenticate(password, challenge, response);
      } else {
        V1Authenticator.authenticate(password, challenge, response);
      }

      return ErrorMessages.NO_ERROR;
    } catch (final AuthenticationException e) {
      ClientLogger.logQuietly(e);
      return ErrorMessages.INVALID_PASSWORD;
    }
  }
}
