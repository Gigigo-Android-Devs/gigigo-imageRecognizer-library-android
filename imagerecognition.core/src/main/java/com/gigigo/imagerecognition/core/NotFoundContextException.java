package com.gigigo.imagerecognition.core;

/**
 * Created by Sergio Martinez Rodriguez
 * Date 6/5/16.
 */
public class NotFoundContextException extends RuntimeException {
  public NotFoundContextException(String message) {
    super(message);
  }

  public NotFoundContextException() {
    super("Context not provided, please call setContextProvider() method providing a "
        + "ContextProvider implementation");
  }
}
