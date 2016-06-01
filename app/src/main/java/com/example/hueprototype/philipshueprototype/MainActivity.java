package com.example.hueprototype.philipshueprototype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.example.hueprototype.philipshueprototype.R;
import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

/**
 * This
 *
 * @author Sai Cahng and Brandon Lorenz
 *
 */
public class MainActivity extends Activity {
    private PHHueSDK sdk;
    private static final int MAX_HUE=65535;
    private boolean updated;
    private boolean run;
    private boolean bridgeConnection;
    PHLight cur;
    private SDKListener listener;

    /**
     * Creates a controller allowing for a change of state in individual lights
     * @param savedInstanceState worthless variable in this version of the program
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //View vi = inflater.inflate(R.layout.activity_main, null);
        this.setContentView(R.layout.activity_main);

        bridgeConnection = false;

        sdk = PHHueSDK.create();
        //sdk.setSelectedBridge(null);
        listener = new SDKListener();
        sdk.getNotificationManager().registerSDKListener(listener);

        PHBridgeSearchManager sm = (PHBridgeSearchManager) sdk.getSDKService(PHHueSDK.SEARCH_BRIDGE);
        sm.search(true, true);

        run = true;



        //Populates UI on connection and during state change
        while(run) {
            updated = false;

            if(bridgeConnection) {
                PHBridge bridge = sdk.getSelectedBridge();
                List<PHLight> allLights = bridge.getResourceCache().getAllLights();

                Iterator<PHLight> itr = allLights.iterator();

                ScrollView buttons = (ScrollView) findViewById(R.id.buttons);
                buttons.removeAllViews();

                LinearLayout linearLayout = new LinearLayout(this);
                linearLayout.setOrientation(LinearLayout.VERTICAL);

                int i = 1;

                while (itr.hasNext()) {
                    cur = itr.next();

                    Button button = new Button(this);
                    button.setText("Light " + i);
                    linearLayout.addView(button);

                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            changeLight(cur, new Random().nextInt(MAX_HUE));
                        }
                    });

                    ++i;
                }

                buttons.addView(linearLayout);

                while (!updated) {}
            }
        }
    }

    /**
     * Changes the state of given light to the given hue
     * @param light
     * @param hue
     */
    public void changeLight(PHLight light, int hue) {
        PHBridge bridge = sdk.getSelectedBridge();

        PHLightState lightState = new PHLightState();
        lightState.setHue(hue);
        bridge.updateLightState(light, lightState);
    }

    /**
     * Connects to brige and listens for changes in state
     */
    // Local SDK Listener
    class SDKListener implements PHSDKListener {

        @Override
        public void onAccessPointsFound(List accessPoint) {
            // Handle your bridge search results here.  Typically if multiple results are returned you will want to display them in a list
            // and let the user select their bridge.   If one is found you may opt to connect automatically to that bridge.

            //List<PHBridge> bridges = accessPoint;
            if(accessPoint.size() == 1) {
                //sdk.setSelectedBridge(bridges.get(0));
                sdk.connect((PHAccessPoint) accessPoint.get(0));
            } else {
                // Manage what happens when there are multiple locations going
                //Currently just chooses the first bridge

                //sdk.setSelectedBridge(bridges.get(0));
                sdk.connect((PHAccessPoint) accessPoint.get(0));
            }
        }

        @Override
        public void onCacheUpdated(List cacheNotificationsList, PHBridge bridge) {
            updated = true;
        }

        @Override
        public void onBridgeConnected(PHBridge b, String username) {
            sdk.setSelectedBridge(b);
            sdk.enableHeartbeat(b, PHHueSDK.HB_INTERVAL);
            bridgeConnection = true;
            // Here it is recommended to set your connected bridge in your sdk object (as above) and start the heartbeat.
            // At this point you are connected to a bridge so you should pass control to your main program/activity.
            // The username is generated randomly by the bridge.
            // Also it is recommended you store the connected IP Address/ Username in your app here.  This will allow easy automatic connection on subsequent use.
        }

        @Override
        public void onAuthenticationRequired(PHAccessPoint accessPoint) {
            sdk.startPushlinkAuthentication(accessPoint);
            // Arriving here indicates that Pushlinking is required (to prove the User has physical access to the bridge).  Typically here
            // you will display a pushlink image (with a timer) indicating to to the user they need to push the button on their bridge within 30 seconds.
        }

        @Override
        public void onConnectionResumed(PHBridge bridge) {

        }

        @Override
        public void onConnectionLost(PHAccessPoint accessPoint) {
            // Here you would handle the loss of connection to your bridge.
        }

        @Override
        public void onError(int code, final String message) {
            // Here you can handle events such as Bridge Not Responding, Authentication Failed and Bridge Not Found
        }

        @Override
        public void onParsingErrors(List parsingErrorsList) {
            // Any JSON parsing errors are returned here.  Typically your program should never return these.
        }
    };

    @Override
    protected void onDestroy() {
        PHBridge bridge = sdk.getSelectedBridge();
        if (bridge != null) {

            if (sdk.isHeartbeatEnabled(bridge)) {
                sdk.disableHeartbeat(bridge);
            }

            sdk.disconnect(bridge);
        }
        super.onDestroy();
    }
}

