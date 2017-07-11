package games.strategy.persistence.serializable;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.junit.Before;
import org.junit.Test;

/**
 * A fixture for testing the basic aspects of proxy classes.
 *
 * @param <T> The type of the principal to be proxied.
 */
public abstract class AbstractProxyTestCase<T> {
  private final ProxyFactoryRegistry proxyFactoryRegistry = ProxyFactoryRegistry.newInstance();

  private final Class<T> principalType;

  /**
   * @param principalType The type of the principal to be proxied; must not be {@code null}.
   */
  protected AbstractProxyTestCase(final Class<T> principalType) {
    checkNotNull(principalType);

    this.principalType = principalType;
  }

  /**
   * Asserts that the specified principals are equal.
   *
   * <p>
   * This implementation compares the two objects using the {@code equals} method.
   * </p>
   *
   * @param expected The expected principal; must not be {@code null}.
   * @param actual The actual principal; must not be {@code null}.
   *
   * @throws AssertionError If the two principals are not equal.
   */
  protected void assertPrincipalEquals(final T expected, final T actual) {
    checkNotNull(expected);
    checkNotNull(actual);

    assertThat(actual, is(expected));
  }

  /**
   * Creates the principal to be proxied.
   *
   * @return The principal to be proxied; never {@code null}.
   */
  protected abstract T createPrincipal();

  /**
   * Registers the proxy factories required for the principal to be persisted.
   *
   * @param proxyFactoryRegistry The proxy factory registry for use in the fixture; must not be {@code null}.
   */
  protected abstract void registerProxyFactories(ProxyFactoryRegistry proxyFactoryRegistry);

  private static Object readObject(final ByteArrayOutputStream baos) throws Exception {
    try (final InputStream is = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(is)) {
      return ois.readObject();
    }
  }

  private void writeObject(final ByteArrayOutputStream baos, final T obj) throws Exception {
    try (final ObjectOutputStream oos = new ObjectOutputStream(baos, proxyFactoryRegistry)) {
      oos.writeObject(obj);
    }
  }

  /**
   * Sets up the test fixture.
   *
   * <p>
   * Subclasses may override and must call the superclass implementation.
   * </p>
   *
   * @throws Exception If an error occurs.
   */
  @Before
  public void setUp() throws Exception {
    registerProxyFactories(proxyFactoryRegistry);
  }

  @Test
  public void shouldBeAbleToRoundTripPrincipal() throws Exception {
    final T expected = createPrincipal();
    assertThat(expected, is(not(nullValue())));
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    writeObject(baos, expected);
    final Object untypedActual = readObject(baos);

    assertThat(untypedActual, is(not(nullValue())));
    assertThat(untypedActual, is(instanceOf(principalType)));
    final T actual = principalType.cast(untypedActual);
    assertPrincipalEquals(expected, actual);
  }
}