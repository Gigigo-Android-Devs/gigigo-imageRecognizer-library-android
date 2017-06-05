package com.gigigo.vuforiaimplementation.credentials;

import com.gigigo.imagerecognitioninterface.ImageRecognitionCredentials;

public class ParcelableIrCredentialsAdapter {

  public ParcelableVuforiaCredentials getParcelableFromCredentialsForVuforia(ImageRecognitionCredentials irc) {

    return new ParcelableVuforiaCredentials(irc.getLicensekey(), irc.getClientAccessKey(),
        irc.getClientSecretKey());
  }

}
