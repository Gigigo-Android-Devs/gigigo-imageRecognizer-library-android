package com.gigigo.imagerecognition.vuforia;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.gigigo.ggglib.device.providers.ContextProvider;
import com.gigigo.imagerecognition.core.ImageRecognition;
import com.gigigo.imagerecognition.core.ImageRecognitionCredentials;
import com.gigigo.imagerecognition.core.NotFoundContextException;
import com.gigigo.permissions.PermissionCheckerImpl;
import com.gigigo.permissions.interfaces.Permission;
import com.gigigo.permissions.interfaces.PermissionChecker;
import com.gigigo.permissions.interfaces.UserPermissionRequestResponseListener;
import com.gigigo.imagerecognition.vuforia.credentials.ParcelableIrCredentialsAdapter;
import com.gigigo.imagerecognition.vuforia.credentials.ParcelableVuforiaCredentials;
import com.gigigo.imagerecognition.vuforia.permissions.CameraPermissionImpl;

/**
 * This is a suitable implementation for image recognition module, in fact this this Vuforia
 * ImageRecognition interface specialization. An instance of this class would call Vuforia SDK
 * when startImageRecognition is called.
 * <p/>
 * This class is already managing Camera permissions implementation.
 */
public class ImageRecognitionVuforia
    implements ImageRecognition, UserPermissionRequestResponseListener {

  public static final String IMAGE_RECOGNITION_CREDENTIALS = "IMAGE_RECOGNITION_CREDENTIALS";
  public static final String IMAGE_RECOGNITION_CODE_RESULT = "IMAGE_RECOGNITION_CODE_RESULT";
  static boolean is_for_result = false;
  private static ContextProvider contextProvider;
  /********************
   * NEW start for redder/destapp
   ************************************/
  int mCodeForResult = -1;
  private PermissionChecker permissionChecker;
  private Permission cameraPermission;
  private ParcelableVuforiaCredentials credentials;

  public ImageRecognitionVuforia() {

  }

  /*we need a persistesd context(ImageRecognitionVuforiaImpl.getContextProvider().getApplicationContext()):
the problem, is sometimes the net confirmation of action is more quickly than finished of vuforia activity
the solution is use context exist in life of runnable and complety sure exist when run sendBroadcast
and the next problem is wait for complete vuforia activity finishing for when receive the action the
activity caller vuforia is started again, for show alertDialog
*/
  public static void sendRecognizedPattern(final Intent i) {

    Handler mHandler = new Handler(Looper.getMainLooper());
    mHandler.postDelayed(new Runnable() {
      @Override public void run() {
        try {
          contextProvider.getApplicationContext().sendBroadcast(i);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }, 1500);
  }

  @Override public <T> void setContextProvider(T contextProvider) {
    this.contextProvider = (ContextProvider) contextProvider;
    this.permissionChecker = new PermissionCheckerImpl(this.contextProvider.getCurrentActivity());
    this.cameraPermission = new CameraPermissionImpl(this.contextProvider.getApplicationContext());
  }

  /**
   * Checks permissions and starts Image recognitio activity using given credentials. If Permissions
   * were not granted User will be notified. If credentials are not valid you'll have an error log
   * message.
   *
   * @param credentials interface implementation with Vuforia keys
   */
  @Override public void startImageRecognition(ImageRecognitionCredentials credentials) {
    is_for_result = false;
    checkContext();

    this.credentials = digestCredentials(credentials);

    if (permissionChecker.isGranted(cameraPermission)) {
      startImageRecognitionActivity();
    } else {
      requestPermissions();
    }
  }

  private void checkContext() throws NotFoundContextException {
    if (contextProvider == null) {
      throw new NotFoundContextException();
    }
  }

  @Override public void onPermissionAllowed(boolean permissionAllowed, int i) {
    if (permissionAllowed) {
      if (is_for_result) {
        startImageRecognitionForResult();
      } else {
        startImageRecognitionActivity();
      }
    }
  }

  private void requestPermissions() {
    if (contextProvider.isActivityContextAvailable()) {
      permissionChecker.askForPermission(this, cameraPermission);
    }
  }

  private ParcelableVuforiaCredentials digestCredentials(
      ImageRecognitionCredentials externalCredentials) {
    ParcelableIrCredentialsAdapter adapter = new ParcelableIrCredentialsAdapter();
    ParcelableVuforiaCredentials credentials =
        adapter.getParcelableFromCredentialsForVuforia(externalCredentials);
    return credentials;
  }
  //necesario, ya q podemos llamar a con result y start estandar y x tanto el mCodeForResult no nos determina el contexto de ejecucion que ueremos en ese momento, el contexto nos lo deetrmina la funcion llamada, startactivity o la de on result

  private void startImageRecognitionActivity() {
    Intent imageRecognitionIntent =
        new Intent(contextProvider.getApplicationContext(), VuforiaActivity.class);
    imageRecognitionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    Bundle b = new Bundle();
    b.putParcelable(IMAGE_RECOGNITION_CREDENTIALS, credentials);

    imageRecognitionIntent.putExtra(IMAGE_RECOGNITION_CREDENTIALS, b);
    contextProvider.getApplicationContext().startActivity(imageRecognitionIntent);
  }

  public void setCodeForResult(int requestCode) {
    this.mCodeForResult = requestCode;
  }

  public void setCredentials(ImageRecognitionCredentials credentials) {
    this.credentials = digestCredentials(credentials);
  }

  public void startImageRecognitionForResult(ImageRecognitionCredentials credentials,
      int codeForResult) {
    setCredentials(credentials);
    setCodeForResult(codeForResult);
    startImageRecognitionForResult();
  }

  private void startImageRecognitionForResult() {
    is_for_result = true;
    checkContext();

    if (permissionChecker.isGranted(cameraPermission)) {
      startImageRecognitionForResultIntent();
    } else {
      requestPermissions();
    }
  }

  private void startImageRecognitionForResultIntent() {
    Intent imageRecognitionIntent =
        new Intent(contextProvider.getApplicationContext(), VuforiaActivity.class);
    //  imageRecognitionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    Bundle b = new Bundle();
    b.putParcelable(IMAGE_RECOGNITION_CREDENTIALS, credentials);

    imageRecognitionIntent.putExtra(IMAGE_RECOGNITION_CREDENTIALS, b);
    imageRecognitionIntent.putExtra(IMAGE_RECOGNITION_CODE_RESULT, mCodeForResult);
    contextProvider.getCurrentActivity()
        .startActivityForResult(imageRecognitionIntent, mCodeForResult);
  }
}
