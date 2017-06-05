package com.gigigo.vuforiacore.sdkimagerecognition.cloudrecognition;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import android.support.v4ox.content.ContextCompat;
import com.gigigo.ggglib.logger.GGGLogImpl;
import com.gigigo.ggglib.logger.LogLevel;
import com.gigigo.vuforiacore.R;
import com.gigigo.vuforiacore.sdkimagerecognition.vuforiaenvironment.ApplicationControl;
import com.gigigo.vuforiacore.sdkimagerecognition.vuforiaenvironment.VuforiaException;
import com.gigigo.vuforiacore.sdkimagerecognition.vuforiaenvironment.VuforiaSession;
import com.gigigo.vuforiacore.sdkimagerecognition.vuforiaenvironment.utils.LoadingDialogHandler;
import com.gigigo.vuforiacore.sdkimagerecognition.vuforiaenvironment.utils.VuforiaGLView;
import com.vuforia.CameraDevice;
import com.vuforia.ObjectTracker;
import com.vuforia.State;
import com.vuforia.TargetFinder;
import com.vuforia.TargetSearchResult;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;

/**
 * Created by Alberto on 31/03/2016.
 */
public class CloudRecognition implements ApplicationControl {

  // These codes match the ones defined in TargetFinder in Vuforia.jar
  static final int INIT_SUCCESS = 2;
  static final int INIT_ERROR_NO_NETWORK_CONNECTION = -1;
  static final int INIT_ERROR_SERVICE_NOT_AVAILABLE = -2;
  static final int UPDATE_ERROR_AUTHORIZATION_FAILED = -1;
  static final int UPDATE_ERROR_PROJECT_SUSPENDED = -2;
  static final int UPDATE_ERROR_NO_NETWORK_CONNECTION = -3;
  static final int UPDATE_ERROR_SERVICE_NOT_AVAILABLE = -4;
  static final int UPDATE_ERROR_BAD_FRAME_QUALITY = -5;
  static final int UPDATE_ERROR_UPDATE_SDK = -6;
  static final int UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE = -7;
  static final int UPDATE_ERROR_REQUEST_TIMEOUT = -8;
  static final int HIDE_LOADING_DIALOG = 0;
  static final int SHOW_LOADING_DIALOG = 1;
  static VuforiaSession vuforiaAppSession;
  boolean mFinderStarted = false;
  boolean mStopFinderIfStarted = false;
  boolean mIsDroidDevice = false;
  boolean bShowErrorMessages = true;
  // Our OpenGL view:
  private VuforiaGLView mGlView;
  // Our renderer:
  private CloudRecognitionRenderer mRenderer;
  private boolean mExtendedTracking = false;
  private String mAccessKey = "";
  private String mSecretKey = "";
  private String mLicenseKey = "";
  // View overlays to be displayed in the Augmented View
  private RelativeLayout mUILayout;
  // Error message handling:
  private int mlastErrorCode = 0;
  private int mInitErrorCode = 0;
  private boolean mFinishActivityOnError;
  // Alert Dialog used to display SDK errors
  private AlertDialog mErrorDialog;
  private GestureDetector mGestureDetector;
  private LoadingDialogHandler loadingDialogHandler;
  private double mLastErrorTime;
  private Activity mActivity;
  private ICloudRecognitionCommunicator mCommunicator;
  // declare scan line and its animation
  private View scanLine;
  private TranslateAnimation scanAnimation;

  public CloudRecognition(Activity activity, ICloudRecognitionCommunicator communicator,
      String kAccessKey, String kSecretKey, String kLicenseKey, boolean showErrorMessages) {
    this.mActivity = activity;
    this.mCommunicator = communicator;
    loadingDialogHandler = new LoadingDialogHandler(this.mActivity);

    this.mAccessKey = kAccessKey;
    this.mSecretKey = kSecretKey;
    this.mLicenseKey = kLicenseKey;

    this.bShowErrorMessages = showErrorMessages;
  }

  //region methods 4 Focus
  public boolean on_TouchEvent(MotionEvent event) {
    return mGestureDetector.onTouchEvent(event);
  }
  //endregion

  // Callback for configuration changes the activity handles itself
  public void onConfigurationChanged(Configuration config) {
    vuforiaAppSession.onConfigurationChanged();
  }

  // Called when the activity first starts or needs to be recreated after
  // resuming the application or a configuration change.
  //region methods Called from ActivityLifeCycle like overrides ACTIVITY
  public void on_Create() {
    try {
      if (this.mLicenseKey == "" || this.mAccessKey == "" || this.mSecretKey == "") {
        Log.e(this.mActivity.getResources().getString(R.string.orchextra_auth_error_tag),
            this.mActivity.getResources().getString(R.string.orchextra_auth_error_text));
        this.mActivity.finish();
      } else {
        vuforiaAppSession = new VuforiaSession(this, this.mLicenseKey);
        startLoadingAnimation();
        vuforiaAppSession.initAR(this.mActivity, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Creates the GestureDetector listener for processing double tap
        mGestureDetector = new GestureDetector(this.mActivity, new GestureListener());

        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith("droid");
      }
    } catch (Throwable tr) {
      GGGLogImpl.log(tr.getMessage(), LogLevel.ERROR);
    }
  }

  // Called when the activity will start interacting with the user.
  protected void on_Resume() {
    // This is needed for some Droid devices to force portrait
    if (mIsDroidDevice) {
      this.mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      this.mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    try {
      if (vuforiaAppSession != null) vuforiaAppSession.resumeAR();
    } catch (VuforiaException e) {
      GGGLogImpl.log(e.getString(), LogLevel.ERROR);
    }

    // Resume the GL view:
    if (mGlView != null) {
      mGlView.setVisibility(View.VISIBLE);
      mGlView.onResume();
    }
  }

  // Called when the system is about to start resuming a previous activity.
  protected void on_Pause() {
    //Todo camera flash, in previous version of Vuforia was necesary
    try {
      vuforiaAppSession.pauseAR();
    } catch (VuforiaException e) {
      GGGLogImpl.log(e.getMessage(), LogLevel.ERROR);
    } catch (Throwable tr) {
      GGGLogImpl.log(tr.getMessage(), LogLevel.ERROR);
    }

    // Pauses the OpenGLView
    if (mGlView != null) {
      mGlView.setVisibility(View.INVISIBLE);
      mGlView.onPause();
    }
  }
  //endregion

  // The final call you receive before your activity is destroyed.
  protected void on_Destroy() {
    try {
      if (vuforiaAppSession != null) {
        vuforiaAppSession.stopAR();
        vuforiaAppSession = null;
      }
    } catch (VuforiaException e) {
      GGGLogImpl.log(e.getMessage(), LogLevel.ERROR);
    }
    System.gc();
  }

  //region Init
  protected void deinitCloudReco() {
    // Get the object tracker:
    TrackerManager trackerManager = TrackerManager.getInstance();
    ObjectTracker objectTracker =
        (ObjectTracker) trackerManager.getTracker(ObjectTracker.getClassType());
    if (objectTracker == null) {
      GGGLogImpl.log("Failed to destroy the tracking data set because the ObjectTracker has not"
          + " been initialized.", LogLevel.ERROR);
      return;
    }

    // Deinitialize target finder:
    TargetFinder finder = objectTracker.getTargetFinder();
    finder.deinit();
  }

  public void startLoadingAnimation() {
    // Inflates the Overlay Layout to be displayed above the Camera View
    LayoutInflater inflater = LayoutInflater.from(this.mActivity);
    mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay_vuforia, null, false);
    mUILayout.setVisibility(View.VISIBLE);

    ProgressBar loadingProgressBar = (ProgressBar) mUILayout.findViewById(R.id.loading_indicator);

    // By default
    loadingDialogHandler.mLoadingDialogContainer = mUILayout.findViewById(R.id.loading_container);

    if (Build.VERSION.SDK_INT < 21) {
      if (loadingProgressBar.getIndeterminateDrawable() != null) {
        loadingProgressBar.getIndeterminateDrawable()
            .setColorFilter(
                ContextCompat.getColor(mActivity, R.color.vuforia_loading_indicator_color),
                PorterDuff.Mode.SRC_IN);
      }
    }

    loadingDialogHandler.mLoadingDialogContainer.setVisibility(View.VISIBLE);

    this.mActivity.addContentView(mUILayout,
        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));
  }

  // Initializes AR application components.
  private void initApplicationAR() {
    // Create OpenGL ES view:
    int depthSize = 16;
    int stencilSize = 0;
    boolean translucent = Vuforia.requiresAlpha();

    // Initialize the GLView with proper flags
    mGlView = new VuforiaGLView(this.mActivity);
    mGlView.init(translucent, depthSize, stencilSize);

    // Setups the Renderer of the GLView
    mRenderer = new CloudRecognitionRenderer(vuforiaAppSession, this);
    mGlView.setRenderer(mRenderer);
  }

  //region retrieve Error Message
  // Returns the error message for each error code
  private String getStatusDescString(int code) {
    if (code == UPDATE_ERROR_AUTHORIZATION_FAILED) {
      return this.mActivity.getString(R.string.UPDATE_ERROR_AUTHORIZATION_FAILED_DESC);
    }
    if (code == UPDATE_ERROR_PROJECT_SUSPENDED) {
      return this.mActivity.getString(R.string.UPDATE_ERROR_PROJECT_SUSPENDED_DESC);
    }
    if (code == UPDATE_ERROR_NO_NETWORK_CONNECTION) {
      return this.mActivity.getString(R.string.UPDATE_ERROR_NO_NETWORK_CONNECTION_DESC);
    }
    if (code == UPDATE_ERROR_SERVICE_NOT_AVAILABLE) {
      return this.mActivity.getString(R.string.UPDATE_ERROR_SERVICE_NOT_AVAILABLE_DESC);
    }
    if (code == UPDATE_ERROR_UPDATE_SDK) {
      return this.mActivity.getString(R.string.UPDATE_ERROR_UPDATE_SDK_DESC);
    }
    if (code == UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE) {
      return this.mActivity.getString(R.string.UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE_DESC);
    }
    if (code == UPDATE_ERROR_REQUEST_TIMEOUT) {
      return this.mActivity.getString(R.string.UPDATE_ERROR_REQUEST_TIMEOUT_DESC);
    }
    if (code == UPDATE_ERROR_BAD_FRAME_QUALITY) {
      return this.mActivity.getString(R.string.UPDATE_ERROR_BAD_FRAME_QUALITY_DESC);
    } else {
      return this.mActivity.getString(R.string.UPDATE_ERROR_UNKNOWN_DESC);
    }
  }
  //endregion

  // Returns the error message for each error code
  private String getStatusTitleString(int code) {
    if (code == UPDATE_ERROR_AUTHORIZATION_FAILED) {
      return this.mActivity.getString(R.string.UPDATE_ERROR_AUTHORIZATION_FAILED_TITLE);
    }
    if (code == UPDATE_ERROR_PROJECT_SUSPENDED) {
      return this.mActivity.getString(R.string.UPDATE_ERROR_PROJECT_SUSPENDED_TITLE);
    }
    if (code == UPDATE_ERROR_NO_NETWORK_CONNECTION) {
      return this.mActivity.getString(R.string.UPDATE_ERROR_NO_NETWORK_CONNECTION_TITLE);
    }
    if (code == UPDATE_ERROR_SERVICE_NOT_AVAILABLE) {
      return this.mActivity.getString(R.string.UPDATE_ERROR_SERVICE_NOT_AVAILABLE_TITLE);
    }
    if (code == UPDATE_ERROR_UPDATE_SDK) {
      return this.mActivity.getString(R.string.UPDATE_ERROR_UPDATE_SDK_TITLE);
    }
    if (code == UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE) {
      return this.mActivity.getString(R.string.UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE_TITLE);
    }
    if (code == UPDATE_ERROR_REQUEST_TIMEOUT) {
      return this.mActivity.getString(R.string.UPDATE_ERROR_REQUEST_TIMEOUT_TITLE);
    }
    if (code == UPDATE_ERROR_BAD_FRAME_QUALITY) {
      return this.mActivity.getString(R.string.UPDATE_ERROR_BAD_FRAME_QUALITY_TITLE);
    } else {
      return this.mActivity.getString(R.string.UPDATE_ERROR_UNKNOWN_TITLE);
    }
  }

  // Shows error messages as System dialogs
  public void showErrorMessage(int errorCode, double errorTime, boolean finishActivityOnError) {
    //you can change for show or hidden the vuforia error messages
    if (bShowErrorMessages) {
      if (errorTime < (mLastErrorTime + 5.0) || errorCode == mlastErrorCode) return;

      mlastErrorCode = errorCode;
      mFinishActivityOnError = finishActivityOnError;

      final Activity activity = this.mActivity;

      this.mActivity.runOnUiThread(new Runnable() {
        public void run() {
          if (mErrorDialog != null) {
            mErrorDialog.dismiss();
          }

          // Generates an Alert Dialog to show the error message
          AlertDialog.Builder builder = new AlertDialog.Builder(activity);
          builder.setMessage(getStatusDescString(CloudRecognition.this.mlastErrorCode))
              .setTitle(getStatusTitleString(CloudRecognition.this.mlastErrorCode))
              .setCancelable(false)
              .setIcon(0)
              .setPositiveButton(activity.getString(R.string.button_OK),
                  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                      if (mFinishActivityOnError) {
                        activity.finish();
                      } else {
                        dialog.dismiss();
                      }
                    }
                  });

          mErrorDialog = builder.create();
          mErrorDialog.show();
        }
      });
    }
  }
  //endregion

  //restart finder
  public void startFinderIfStopped() {
    if (!mFinderStarted) {
      mFinderStarted = true;

      // Get the object tracker:
      TrackerManager trackerManager = TrackerManager.getInstance();
      ObjectTracker objectTracker =
          (ObjectTracker) trackerManager.getTracker(ObjectTracker.getClassType());

      // Initialize target finder:
      TargetFinder targetFinder = objectTracker.getTargetFinder();

      targetFinder.clearTrackables();
      targetFinder.startRecognition();
    }
  }

  //stop finder
  public void stopFinderIfStarted() {
    if (mFinderStarted) {
      mFinderStarted = false;

      // Get the object tracker:
      TrackerManager trackerManager = TrackerManager.getInstance();
      ObjectTracker objectTracker =
          (ObjectTracker) trackerManager.getTracker(ObjectTracker.getClassType());

      // Initialize target finder:
      TargetFinder targetFinder = objectTracker.getTargetFinder();

      targetFinder.stop();
    }
  }

  //region Set colors of the finder animations
  @Deprecated public void setUIScanLineColor(int color) {
    //not in Vuforia6
  }

  @Deprecated public void setUIPointColor(int color) {
    //not in Vuforia6
  }

  //region implements -->ApplicationControl
  @Override public boolean doInitTrackers() {
    TrackerManager tManager = TrackerManager.getInstance();
    Tracker tracker;

    // Indicate if the trackers were initialized correctly
    boolean result = true;

    tracker = tManager.initTracker(ObjectTracker.getClassType());
    if (tracker == null) {
      GGGLogImpl.log(
          "Tracker not initialized. Tracker already initialized or the camera is already started",
          LogLevel.ERROR);
      result = false;
    } else {
      GGGLogImpl.log("Tracker successfully initialized");
    }

    return result;
  }

  //endregion

  @Override public boolean doLoadTrackersData() {
    GGGLogImpl.log("doLoadTrackersData");

    // Get the object tracker:
    TrackerManager trackerManager = TrackerManager.getInstance();
    ObjectTracker objectTracker =
        (ObjectTracker) trackerManager.getTracker(ObjectTracker.getClassType());

    // Initialize target finder:
    TargetFinder targetFinder = objectTracker.getTargetFinder();

    // Start initialization:
    if (targetFinder.startInit(this.mAccessKey, this.mSecretKey)) {
      targetFinder.waitUntilInitFinished();
    }

    int resultCode = targetFinder.getInitState();
    if (resultCode != TargetFinder.INIT_SUCCESS) {
      if (resultCode == TargetFinder.INIT_ERROR_NO_NETWORK_CONNECTION) {
        mInitErrorCode = UPDATE_ERROR_NO_NETWORK_CONNECTION;
      } else {
        mInitErrorCode = UPDATE_ERROR_SERVICE_NOT_AVAILABLE;
      }

      GGGLogImpl.log("Failed to initialize target finder.", LogLevel.ERROR);
      return false;
    }

    // Now you can customize the color of
    // the UI
    // targetFinder->setUIScanlineColor(1.0, 0.0, 0.0);
    // targetFinder->setUIPointColor(0.0, 0.0, 1.0);

    return true;
  }

  @Override public boolean doStartTrackers() {
    // Indicate if the trackers were started correctly
    boolean result = true;

    // Start the tracker:
    TrackerManager trackerManager = TrackerManager.getInstance();
    ObjectTracker objectTracker =
        (ObjectTracker) trackerManager.getTracker(ObjectTracker.getClassType());
    objectTracker.start();

    //trackerManager.getStateUpdater().getLatestState().getTrackable(0).getUserData().

    // Start cloud based recognition if we are in scanning mode:
    TargetFinder targetFinder = objectTracker.getTargetFinder();
    targetFinder.startRecognition();

    mFinderStarted = true;

    return result;
  }

  @Override public boolean doStopTrackers() {
    // Indicate if the trackers were stopped correctly
    boolean result = true;

    TrackerManager trackerManager = TrackerManager.getInstance();
    ObjectTracker objectTracker =
        (ObjectTracker) trackerManager.getTracker(ObjectTracker.getClassType());

    if (objectTracker != null) {
      objectTracker.stop();

      // Stop cloud based recognition:
      TargetFinder targetFinder = objectTracker.getTargetFinder();
      targetFinder.stop();
      mFinderStarted = false;

      // Clears the trackables
      targetFinder.clearTrackables();
    } else {
      result = false;
    }

    return result;
  }

  @Override public boolean doUnloadTrackersData() {
    return true;
  }

  @Override public boolean doDeinitTrackers() {
    // Indicate if the trackers were deinitialized correctly
    boolean result = true;

    TrackerManager tManager = TrackerManager.getInstance();
    tManager.deinitTracker(ObjectTracker.getClassType());

    return result;
  }

  @Override public void onInitARDone(VuforiaException exception) {
    if (exception == null) {
      initApplicationAR();

      // Start the camera:
      try {
        vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
      } catch (VuforiaException e) {
        GGGLogImpl.log(e.getMessage(), LogLevel.ERROR);
      }

      boolean result = CameraDevice.getInstance()
          .setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

      if (!result) GGGLogImpl.log("Unable to enable continuous autofocus", LogLevel.ERROR);

      mUILayout.bringToFront();

      // Hides the Loading Dialog
      loadingDialogHandler.sendEmptyMessage(HIDE_LOADING_DIALOG);

      mUILayout.setBackgroundColor(Color.TRANSPARENT);
      this.mCommunicator.setContentViewTop(mGlView);
    } else {
      GGGLogImpl.log(exception.getString(), LogLevel.ERROR);
      if (mInitErrorCode != 0) {

        showErrorMessage(mInitErrorCode, 10, true);
      } else {
        this.mActivity.finish();
        GGGLogImpl.log(exception.getMessage(), LogLevel.ERROR);
      }
    }
  }

  @Override public void onVuforiaUpdate(State state) {

    // Get the tracker manager:
    TrackerManager trackerManager = TrackerManager.getInstance();

    // Get the object tracker:
    ObjectTracker objectTracker =
        (ObjectTracker) trackerManager.getTracker(ObjectTracker.getClassType());

    // Get the target finder:
    TargetFinder finder = objectTracker.getTargetFinder();

    // Check if there are new results available:
    final int statusCode = finder.updateSearchResults();

    // Show a message if we encountered an error:
    if (statusCode < 0) {

      boolean closeAppAfterError = (statusCode == UPDATE_ERROR_NO_NETWORK_CONNECTION
          || statusCode == UPDATE_ERROR_SERVICE_NOT_AVAILABLE);

      showErrorMessage(statusCode, state.getFrame().getTimeStamp(), closeAppAfterError);
    } else if (statusCode == TargetFinder.UPDATE_RESULTS_AVAILABLE) {
      // Process new search results
      if (finder.getResultCount() > 0) {
        TargetSearchResult result = finder.getResult(0);
        Log.i("#################", "#################" + result.getMetaData() + "");
        Log.i("#################", "#################" + finder.getResultCount() + "");

        // Check if this target is suitable for tracking:
        if (result.getTrackingRating() > 0) {

          Trackable trackable = finder.enableTracking(result);

          if (mExtendedTracking) trackable.startExtendedTracking();

          //raise result 2 vuforiaactivity over pipe/communicator

          // this.mCommunicator.onVuforiaResult(trackable, result.getUniqueTargetId());
          this.mCommunicator.onVuforiaResult(trackable, result);
        }
      }
    }
  }

  // Process Single Tap event to trigger autofocus
  private class GestureListener extends GestureDetector.SimpleOnGestureListener {
    // Used to set autofocus one second after a manual focus is triggered
    private final Handler autofocusHandler = new Handler();

    @Override public boolean onDown(MotionEvent e) {
      return true;
    }

    @Override public boolean onSingleTapUp(MotionEvent e) {
      // Generates a Handler to trigger autofocus
      // after 1 second
      autofocusHandler.postDelayed(new Runnable() {
        public void run() {
          boolean result = CameraDevice.getInstance()
              .setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);

          if (!result) Log.e("SingleTapUp", "Unable to trigger focus");
        }
      }, 1000L);

      return true;
    }
  }

  private class RGBColor {
    float r, g, b;

    private RGBColor(int color) {
      r = Color.red(color) / 255.0f;
      g = Color.green(color) / 255.0f;
      b = Color.blue(color) / 255.0f;
    }
  }
  //endregion
}
