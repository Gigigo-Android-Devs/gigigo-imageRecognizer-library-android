package com.gigigo.vuforiacore.sdkimagerecognition.icloudrecognition;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

import com.gigigo.ggglogger.GGGLogImpl;
import com.gigigo.ggglogger.LogLevel;

/**
 * Created by Alberto on 31/03/2016.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class CloudRecognitionActivityLifeCycleCallBack implements Application.ActivityLifecycleCallbacks {

    private static Activity mActivity;
    public static CloudRecognition mCloudReco;

    public CloudRecognitionActivityLifeCycleCallBack(Activity activity, ICloudRecognitionCommunicator icloud, String kAccessKey,
                                                     String kSecretKey, String kLicenseKey, boolean showVuforiaErrorMessageDialog) {
        GGGLogImpl.log("CloudRecognitionActivityLifeCycleCallBack.constructor");
        mActivity = activity;

        this.mCloudReco = new CloudRecognition(activity, icloud, kAccessKey, kSecretKey, kLicenseKey, showVuforiaErrorMessageDialog);
        mActivity.getApplication().registerActivityLifecycleCallbacks(this);
    }

    //region Bridge CloudReco
    public void initUIRecognizer() {
        try {
            if (this.mCloudReco != null)
                this.mCloudReco.on_Create();
        } catch (Exception ex) {
            GGGLogImpl.log(ex.getMessage(), LogLevel.ERROR);
        }
    }

    @Deprecated
    public void setUIScanLineColor(int color) {
        //not in Vuforia6
    }

    @Deprecated
    public void setUIPointColor(int color) {
        //not in Vuforia6
    }
    //endregion

    //region android.app.Application.ActivityLifecycleCallbacks
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (mActivity.equals(activity))
            this.mCloudReco.on_Create();
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (mActivity.equals(activity))
            this.mCloudReco.on_Create();
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (mActivity.equals(activity))
            this.mCloudReco.on_Resume();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (mActivity.equals(activity))
            this.mCloudReco.on_Pause();
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (mActivity.equals(activity)) {
            this.mCloudReco.on_Destroy();
            activity.getApplication().unregisterActivityLifecycleCallbacks(this);
        }

    }
    //endregion
}
