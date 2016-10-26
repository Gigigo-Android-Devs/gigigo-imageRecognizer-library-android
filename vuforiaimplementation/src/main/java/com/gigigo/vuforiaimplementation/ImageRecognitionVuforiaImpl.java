package com.gigigo.vuforiaimplementation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.gigigo.ggglib.ContextProvider;
import com.gigigo.ggglib.permissions.AndroidPermissionCheckerImpl;
import com.gigigo.ggglib.permissions.Permission;
import com.gigigo.ggglib.permissions.PermissionChecker;
import com.gigigo.ggglib.permissions.UserPermissionRequestResponseListener;
import com.gigigo.imagerecognitioninterface.ImageRecognition;
import com.gigigo.imagerecognitioninterface.ImageRecognitionCredentials;
import com.gigigo.imagerecognitioninterface.NotFoundContextException;
import com.gigigo.vuforiaimplementation.credentials.ParcelableIrCredentialsAdapter;
import com.gigigo.vuforiaimplementation.credentials.ParcelableVuforiaCredentials;
import com.gigigo.vuforiaimplementation.permissions.CameraPermissionImpl;

/**
 * This is a suitable implementation for image recognition module, in fact this this Vuforia
 * ImageRecognition interface specialization. An instance of this class would call Vuforia SDK
 * when startImageRecognition is called.
 * <p/>
 * This class is already managing Camera permissions implementation.
 */
public class ImageRecognitionVuforiaImpl implements ImageRecognition, UserPermissionRequestResponseListener {

    public static final String IMAGE_RECOGNITION_CREDENTIALS = "IMAGE_RECOGNITION_CREDENTIALS";
    public static final String IMAGE_RECOGNITION_CODE_RESULT = "IMAGE_RECOGNITION_CODE_RESULT";

    private ContextProvider contextProvider;
    private PermissionChecker permissionChecker;
    private final Permission cameraPermission;

    private ParcelableVuforiaCredentials credentials;

    public ImageRecognitionVuforiaImpl() {
        this.cameraPermission = new CameraPermissionImpl();
    }

    @Override
    public <T> void setContextProvider(T contextProvider) {
        this.contextProvider = (ContextProvider) contextProvider;
        this.permissionChecker = new AndroidPermissionCheckerImpl(this.contextProvider.getApplicationContext(), this.contextProvider);
    }

    /**
     * Checks permissions and starts Image recognitio activity using given credentials. If Permissions
     * were not granted User will be notified. If credentials are not valid you'll have an error log
     * message.
     *
     * @param credentials interface implementation with Vuforia keys
     */
    @Override
    public void startImageRecognition(ImageRecognitionCredentials credentials) {
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

    @Override
    public void onPermissionAllowed(boolean permissionAllowed) {
        if (permissionAllowed) {
            if (is_for_result)
                startImageRecognitionForResult();
            else
                startImageRecognitionActivity();
        }
    }

    private void requestPermissions() {
        if (contextProvider.isActivityContextAvailable()) {
            permissionChecker.askForPermission(cameraPermission, this, contextProvider.getCurrentActivity());
        }
    }

    private ParcelableVuforiaCredentials digestCredentials(
            ImageRecognitionCredentials externalCredentials) {
        ParcelableIrCredentialsAdapter adapter = new ParcelableIrCredentialsAdapter();
        ParcelableVuforiaCredentials credentials = adapter.getParcelableFromCredentialsForVuforia(externalCredentials);
        return credentials;
    }

    private void startImageRecognitionActivity() {
        Intent imageRecognitionIntent = new Intent(contextProvider.getApplicationContext(), VuforiaActivity.class);
        imageRecognitionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Bundle b = new Bundle();
        b.putParcelable(IMAGE_RECOGNITION_CREDENTIALS, credentials);

        imageRecognitionIntent.putExtra(IMAGE_RECOGNITION_CREDENTIALS, b);
        contextProvider.getApplicationContext().startActivity(imageRecognitionIntent);
    }

    /********************
     * NEW start for redder/destapp
     ************************************/
    int mCodeForResult = -1;
    static boolean is_for_result = false; //necesario, ya q podemos llamar a con result y start estandar y x tanto el mCodeForResult no nos determina el contexto de ejecucion que ueremos en ese momento, el contexto nos lo deetrmina la funcion llamada, startactivity o la de on result

    public void setCodeForResult(int requestCode) {
        this.mCodeForResult = requestCode;
    }

    public void setCredentials(ImageRecognitionCredentials credentials) {
        this.credentials = digestCredentials(credentials);
    }

    public void startImageRecognitionForResult(ImageRecognitionCredentials credentials, int codeForResult) {
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
        Intent imageRecognitionIntent = new Intent(contextProvider.getApplicationContext(), VuforiaActivity.class);
      //  imageRecognitionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Bundle b = new Bundle();
        b.putParcelable(IMAGE_RECOGNITION_CREDENTIALS, credentials);

        imageRecognitionIntent.putExtra(IMAGE_RECOGNITION_CREDENTIALS, b);
        imageRecognitionIntent.putExtra(IMAGE_RECOGNITION_CODE_RESULT, mCodeForResult);
        contextProvider.getCurrentActivity().startActivityForResult( imageRecognitionIntent,mCodeForResult);


    }
}
