package com.gigigo.vuforiaimplementation;

import android.app.Application;

/**
 * Created by nubor on 20/01/2017.
 */
public class App extends Application {
  private static Application mApp = null;

  @Override public void onCreate() {
    super.onCreate();
    mApp = this;
  }

  public static Application getApplication() {
    return mApp;
  }
}
