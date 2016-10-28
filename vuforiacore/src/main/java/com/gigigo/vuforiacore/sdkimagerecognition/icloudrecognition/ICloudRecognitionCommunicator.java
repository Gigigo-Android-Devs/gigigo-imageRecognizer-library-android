package com.gigigo.vuforiacore.sdkimagerecognition.icloudrecognition;

import android.view.View;

import com.vuforia.TargetSearchResult;
import com.vuforia.Trackable;

/**
 * Created by Alberto on 01/04/2016.
 */
public interface ICloudRecognitionCommunicator {
    void setContentViewTop(View view);
    void onVuforiaResult(Trackable trackable, TargetSearchResult UniqueID);
}
