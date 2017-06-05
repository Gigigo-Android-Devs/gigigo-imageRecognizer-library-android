package com.gigigo.imagerecognition.vuforia.permissions;

import android.Manifest;
import android.content.Context;
import com.gigigo.permissions.interfaces.Permission;
import com.gigigo.imagerecognition.vuforia.R;

/**
 * Created by Sergio Martinez Rodriguez
 * Date 6/5/16.
 */
public class CameraPermissionImpl implements Permission {
  Context mContext;

  public CameraPermissionImpl(Context context) {
    this.mContext = context;
  }

  @Override public String getAndroidPermissionStringType() {
    return Manifest.permission.CAMERA;
  }

  @Override public int getPermissionSettingsDeniedFeedback() {
    return R.string.ir_permission_settings;
  }

  @Override public int getPermissionDeniedFeedback() {
    return R.string.ir_permission_denied_camera;
  }

  @Override public int getPermissionRationaleTitle() {
    return R.string.ir_permission_rationale_title_camera;
  }

  @Override public int getPermissionRationaleMessage() {
    return R.string.ir_permission_rationale_message_camera;
  }

  @Override public int getNumRetry() {
    if (mContext != null) {
      return mContext.getResources().getInteger(R.integer.ir_permission_retries_camera);
    } else {
      return 0;
    }
  }
}