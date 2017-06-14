package com.gigigo.imagerecognition.vuforia.permissions;

import android.Manifest;
import com.gigigo.ggglib.permission.permissions.Permission;
import com.gigigo.imagerecognition.vuforia.R;

/**
 * Created by Sergio Martinez Rodriguez
 * Date 6/5/16.
 */
@Deprecated //asv esta clase la podemos eliminar utilizando los string para sobreescribir o reutilizarla
public class CameraPermissionImpl implements Permission {
  public CameraPermissionImpl( ) {
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
      return  (R.integer.ir_permission_retries_camera); //infinito
  }
}