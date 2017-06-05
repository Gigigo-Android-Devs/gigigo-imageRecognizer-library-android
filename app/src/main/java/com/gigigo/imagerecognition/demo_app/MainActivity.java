package com.gigigo.imagerecognition.demo_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.gigigo.ggglib.device.providers.ContextProvider;
import com.gigigo.imagerecognition.core.ImageRecognitionCredentials;
import com.gigigo.imagerecognition.vuforia.ImageRecognitionVuforia;

public class MainActivity extends Activity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    //check res drawable, overwrite ir_scanline and ir_markpoint
    findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        startVuforia();
      }
    });
  }

  private void startVuforia() {
    ImageRecognitionCredentials imageRecognitionCredentials = new ImageRecognitionCredentials() {
      @Override public String getClientAccessKey() {

        return "e2db06ae7ebf128f8c03f88e944f39c00441ccee"; //BuildConfig.VUFORIA_KEY;
      }

      @Override public String getClientSecretKey() {

        return "df3a0df3a38a4c76b4e0de3943b9091daf6eb091";// BuildConfig.VUFORIA_SECRET;
      }

      @Override public String getLicensekey() {
        return "AWWqxof/////AAAAAYNQ4Gm0GEM5lE/cWF8g4Gt01+gc0Xsrd2aBp8I7iFYf6+69KpjwVSIwrLvpeVA+o2akWOEUrObBXfb0d7SsxNLSUO7nFmVhjJBcHh5J8EPseSwZb0xptLL3wbKVTF8xCTSsrEP45NLVCnJcGHGu7ngvcQNtIXsokop8w2XfS44baX1bkY6qvpKLh5nCxdtXBs9HcW0Ti8PdBwj8esWKyQgscdY+AIGO9Mq8uHwpLfVQC95xwLPgxwqvAiHG7sXROG9h1+VOhC9TEEcJHgBts0Qa4Wt/IjnAh+9lpG/tZ79dxDarPml96jc7ph4180sruwIT2nCRi+YKK7f30oLytBIP4N2VqVjooX2aKC4PBjTa";// BuildConfig.VUFORIA_LICENSE;
      }
    };

    ImageRecognitionVuforia imageRecognitionVuforia = new ImageRecognitionVuforia();
    imageRecognitionVuforia.setContextProvider(createContextProvider());
    imageRecognitionVuforia.startImageRecognitionForResult(imageRecognitionCredentials, 100);
    //new example

  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    Log.i("Result", "" + requestCode + "" + resultCode);
  }

  private ContextProvider createContextProvider() {
    return new ContextProvider() {
      @Override public Activity getCurrentActivity() {
        return MainActivity.this;
      }

      @Override public boolean isActivityContextAvailable() {
        return true;
      }

      @Override public Context getApplicationContext() {
        return MainActivity.this.getApplication().getApplicationContext();
      }

      @Override public boolean isApplicationContextAvailable() {
        return true;
      }
    };
  }
}
