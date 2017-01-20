package com.gigigo.imagerecognitioninterface;

/**
 * Created by Sergio Martinez Rodriguez
 * Date 29/4/16.
 */
public interface ImageRecognition {



  /**
   * You MUST Call this method before calling startImageRecognition in order to provide a valid
   * context provider, Android context is not enough because this implementation should match
   * Context provider Implementation from GGG lib because current activity context is required
   *
   * @param contextProvider Context Provider
   * @param <T> ContextProvider from Gigigo Lib is accepted
   */
  <T> void setContextProvider(T contextProvider);

  /**
   * Checks permissions and starts Image recognitio activity using given credentials. If Permissions
   * were not granted User will be notified. If credentials are not valid you'll have an error log
   * message.
   *
   * @Throws NotFoundContextException is context has not been provided before
   *
   * @param imageRecognitionCredentials interface implementation with Vuforia keys
   *
   */
  void startImageRecognition(ImageRecognitionCredentials imageRecognitionCredentials);
}
