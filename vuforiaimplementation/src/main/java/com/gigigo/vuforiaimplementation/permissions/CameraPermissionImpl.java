package com.gigigo.vuforiaimplementation.permissions;

import android.Manifest;
import com.gigigo.ggglib.permissions.Permission;
import com.gigigo.vuforiaimplementation.R;

/**
 * Created by Sergio Martinez Rodriguez
 * Date 6/5/16.
 */
public class CameraPermissionImpl implements Permission {

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
}