# imgRecognition

This Image Recognition SDK suite allows your project to switch between different implementations, present here by using the common interface declared in _imagerecognitioninterface_ module. Right now the only implementation present is Vuforia one, but many more kind of Image Recognition (IR from now) engines could be added.

## Using the interface for project integration
You should write code regarding the interface components we provide. This way, implementation would be only a small dependency included and injected somewhere in your code.

Let's walk around the public interface and see how we can use this interface:

Interface `ImageRecognition`:

This is the public interface that code clients from IR SDK will be talking to, note that the implementation of this interface will be part from our SDK implementation details for each IR provider. On the other hand, the client app, will be the holder of the real implementation instance, in other words, the one that decides which implementation and dependency is necessary to add is the client in the smallest piece of code, but the communication is established by using the interface. This is allowing us to make the concrete implementation interchangeable and client code decoupled.    
  
* `setContextProvider()` method: This method must be called in order to get _App_ and current _Activity_ context from your app. Current _Activity_ context is necessary regarding permission request implementation inside IR SDK module. Parameter allows any implementation of `ContextProvider` interface defined in [GGGLib](https://github.com/Gigigo-Android-Devs/gigigo-utils-suite/blob/master/ggglib/src/main/jav/com/gigigo/ggglib/ContextProvider.java).
* `startImageRecognition()` method: call this method if you want to start the IR scanner _Activity_. If this method is called without calling `setContextProvider()` before with a valid `ContextProvider` implementation,`NotFoundContextException` will be thrown, so take care of calling this method after calling the first one  at least once.
 
 Please, have a deeper look into the last method, as you can see _startImageRecognition()_ is already receiving the credentials that IR module needs for working. This way the SDK doesn't have to ask his client about the keys. This could be necessary, not only asking the client for the keys. I mean the fact of establish a communication between IR module and client in this direction (remember the opposite was reached by using _ImageRecognition_ class). In this case, would be great to have an _ImageRecognitionClient_ that would be implemented in client code and the instance would be passed into ImageRecongnition Implementation, held and managed there, having like this a two ways, communication channel.
 
 * ImageRecognitionCredentials`is no more than a wrapper interface for having a common way, or contract to access any keychain for all implementations of IR module. This interface is right now really coupled to Vuforia keys shape and is candidate to be changed in a future.
 * `NotFoundContextException` is the kind of exception thrown in case of having a call to start the IR _Activity_ without ay `ContextProvider` instance.
 * `ImageRecognitionConstants` are common Constant values like some keys for bundle. 
 
 Having a look at _vuforiaimplementation_ module you can find an implementation of this interface ready to use: `ImageRecognitionVuforiaImpl` you can make something like that:
 
 ```java
     ImageRecognitionVuforiaImpl imageRecognitionVuforia = new ImageRecognitionVuforiaImpl();
     imageRecognitionVuforia.setContextProvider(createContextProvider());
     imageRecognitionVuforia.startImageRecognition(imageRecognitionCredentials);
 ```
 Moreover a complete integration sample can be found at: [MainActivity](https://github.com/GigigoGreenLabs/imgRecognition/blob/master/app/src/main/java/com/gigigo/imagerecognition/MainActivity.java)
 
## Receiving Scanning Results

The way the scanned results are sent back to Client app is an completely _Android_ based solution: an _Intent_ sent using a Broadcast Message. That is the reason why public interface is not declaring any component related to this aim. Te only thing related to this matter is `VUFORIA_PATTERN_ID` that is the _Key_ for the scan result _String_ value travelling into the Broadcast Intent. Then Broadcast send action is something each IR implementation should implement in a similar way [this Vuforia implementation](https://github.om/GigigoGreenLabs/imgRecognition/blob/master/vuforiaimplementation/src/main/java/com/gigigo/vuforiaimplementation/VuforiaActivity.java) does:

```java
private static final String RECOGNIZED_IMAGE_INTENT = "com.gigigo.imagerecognition.intent.action.RECOGNIZED_IMAGE";

 private void sendRecognizedPatternToClient(String uniqueId) {
    Intent i = new Intent();
    i.putExtra(ImageRecognitionConstants.VUFORIA_PATTERN_ID, uniqueId);
    i.setAction(RECOGNIZED_IMAGE_INTENT);
    this.sendBroadcast(i);
  }
```

Then responsibility of client app is to declare the _BroadcastReceiver_ in your _Manifest_:
 
 ```xml
  <receiver android:name=".device.imagerecognition.ImageRecognitionReceiver" >
       <intent-filter>
        <action android:name="com.gigigo.imagerecognition.intent.action.RECOGNIZED_IMAGE"></action>
       </intent-filter>
   </receiver>
 ```
 
 And then implement it with an _onReceive()_ method similar to this one:
  
  ```java
  @verride public void onReceive(Context context, Intent intent) {
      initDependencies();
      if (intent.getExtras().containsKey(ImageRecognitionConstants.VUFORIA_PATTERN_ID)){
        vuforiaPatternRecognized(intent.getStringExtra(ImageRecognitionConstants.VUFORIA_PATTERN_ID));
      }
    }
    public void vuforiaPatternRecognized(String stringExtra){
        //do whatever with the id
      }
  ```
  
  As you could think, so many implementations as desired could be added to this repository, but be aware of making an implementation quite similar to Vuforia's one, that means, please use the public interface and Broadcast message for results.
     

## Customizing Styles

You need to override several _Styles_ in order to get customization of drawable and loading indicator message:

 ```xml
     <style name="irCustomizationScanWaterMark" >
         <item name="android:background">@drawable/WATERMARK_IN_CASE_OF_WANTED</item>
     </style>
     <style name="irCustomizationCloseScann" >
         <item name="android:background">@drawable/CLOSE_X_BUTTON_DRAWABLE</item> 
     </style>
     <style name="vuforiaCustomizationLoadingMessage" >
         <item name="android:text">@string/LOADING_INDICATOR_MESSAGE</item>
     </style>
 ```
 For irCustomizationCloseScann style drawable, you can use a drawable as png resource or an _Android selector_
 
 you need also give your main theme colors:
   
  ```xml
       <color name="ir_color_primary">@color/colorPrimary</color>
        <color name="ir_color_primary_dark">@color/colorPrimaryDark</color>
        <color name="ir_color_accent">@color/colorAccent</color>
        <color name="vuforia_loading_indicator_color">@color/ir_color_accent</color>
        <color name="vuforia_loading_bg_color">@color/ir_color_primary</color>
        <color name="ir_scan_point_color">@color/ir_color_primary</color>
        <color name="ir_scan_line_color">@color/ir_color_accent</color>
   ```
  
  And some strings just in case you would like to change them:
  
  ```xml
    <string name="ir_app_name">Vuforia scanner</string>
    <string name="ir_permission_settings">@string/ox_ir_scan_activity_name</string>
    <string name="ir_permission_denied_camera">Denied camera permission</string>
    <string name="ir_permission_rationale_title_camera">Camera Permission</string>
    <string name="ir_permission_rationale_message_camera">App needs the camera device access</string>
  ```

In order to see a complete example in action you can have a look at [Orchextra SDK implementation](https://github.com/Orchextra/orchextra-android-sdk).

