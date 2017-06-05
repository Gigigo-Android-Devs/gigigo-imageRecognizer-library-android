package com.gigigo.imagerecognition.vuforia.credentials;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableVuforiaCredentials implements Parcelable{

  private final String licenseKey;
  private final String clientAccessKey;
  private final String clientSecretKey;

  public ParcelableVuforiaCredentials(String licenseKey, String clientAccessKey,
      String clientSecretKey) {

    this.licenseKey = licenseKey;
    this.clientAccessKey = clientAccessKey;
    this.clientSecretKey = clientSecretKey;

  }

  public String getLicenseKey() {
    return licenseKey;
  }

  public String getClientAccessKey() {
    return clientAccessKey;
  }

  public String getClientSecretKey() {
    return clientSecretKey;
  }

  protected ParcelableVuforiaCredentials(Parcel in) {
    licenseKey = in.readString();
    clientAccessKey = in.readString();
    clientSecretKey = in.readString();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(licenseKey);
    dest.writeString(clientAccessKey);
    dest.writeString(clientSecretKey);
  }

  @SuppressWarnings("unused")
  public static final Parcelable.Creator<ParcelableVuforiaCredentials> CREATOR = new Parcelable.Creator<ParcelableVuforiaCredentials>() {
    @Override
    public ParcelableVuforiaCredentials createFromParcel(Parcel in) {
      return new ParcelableVuforiaCredentials(in);
    }

    @Override
    public ParcelableVuforiaCredentials[] newArray(int size) {
      return new ParcelableVuforiaCredentials[size];
    }
  };
}
