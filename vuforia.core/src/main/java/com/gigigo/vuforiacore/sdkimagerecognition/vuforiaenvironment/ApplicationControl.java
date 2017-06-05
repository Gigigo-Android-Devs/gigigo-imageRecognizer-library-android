/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.gigigo.vuforiacore.sdkimagerecognition.vuforiaenvironment;

import com.vuforia.State;


//  Interface to be implemented by the activity which uses VuforiaSession
public interface ApplicationControl
{
    
    //1º To be called to initialize the trackers
    boolean doInitTrackers();
    
    
    //2º To be called to load the trackers' data
    boolean doLoadTrackersData();
    
    
    //3º To be called to start tracking with the initialized trackers and their
    // loaded data
    boolean doStartTrackers();
    
    
    //4º To be called to stop the trackers
    boolean doStopTrackers();
    
    
    //5º To be called to destroy the trackers' data
    boolean doUnloadTrackersData();
    
    
    //6º To be called to deinitialize the trackers
    boolean doDeinitTrackers();
    
    
    // 7ºThis callback is called after the Vuforia initialization is complete,
    // the trackers are initialized, their data loaded and
    // tracking is ready to start
    void onInitARDone(VuforiaException e);
    
    
    //8º This callback is called every cycle
    void onVuforiaUpdate(State state);
    
}
