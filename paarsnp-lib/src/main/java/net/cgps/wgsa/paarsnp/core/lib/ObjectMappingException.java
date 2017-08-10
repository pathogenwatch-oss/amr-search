package net.cgps.wgsa.paarsnp.core.lib;


public class ObjectMappingException extends Exception {

  /**
   * Generated serial ID.
   */
  private static final long serialVersionUID = -6698134980125494391L;

  public ObjectMappingException() {

  }

  public ObjectMappingException(final Exception e) {

    super(e);
  }

  public ObjectMappingException(final String message) {

    super(message);
  }

  public ObjectMappingException(final String message, final Exception e) {

    super(message, e);
  }

}
