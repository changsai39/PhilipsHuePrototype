package com.example.hueprototype.philipshueprototype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import com.philips.lighting.hue.sdk.PHMessageType;
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
    private PHHueSDK sdk; //stores the current state of the connection
    private static final int MAX_HUE=65535; //range of hues we can change the lights to
    private boolean bridgeConnection; //true if connected to bridge false otherwise
    private SDKListener listener; //listener to check PHHueSDK sdk's status
    private Button[] buttonArray; //stores all buttons
    private final int NUM_BUTTONS = 10; //maximum number of buttons and light connections allowed
    private PHLight[] lightArray; //stores lights

    /**
     * Creates a controller allowing for a change of state in individual lights
     *
     * DIRECTIONS: start app then press and hold center button on bridge until app is connected. Buttons will populate app upon successful connection
     *
     * @param savedInstanceState worthless variable in this version of the program
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vi = inflater.inflate(R.layout.activity_main, null);
        this.setContentView(vi); //sets custom view allowing gestures. We don't really use them in this app though

        //Sets up a dynamic number of buttons here
        ScrollView buttons = (ScrollView) findViewById(R.id.buttons);
        LinearLayout linearLayout = new LinearLayout(getApplicationContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        buttonArray = new Button[NUM_BUTTONS];

        for(int i = 0; i < NUM_BUTTONS; i++) {

            Button button = new Button(this);
            button.setText("Light " + (i + 1));
            button.setVisibility(View.INVISIBLE);
            buttonArray[i] = button;
            linearLayout.addView(button);
        }

        buttons.addView(linearLayout);

        bridgeConnection = false;

        //Connects to the bridge here
        sdk = PHHueSDK.getInstance();
        //sdk.setSelectedBridge(null);
        listener = new SDKListener();
        sdk.getNotificationManager().registerSDKListener(listener);

        PHBridgeSearchManager sm = (PHBridgeSearchManager) sdk.getSDKService(PHHueSDK.SEARCH_BRIDGE);
        sm.search(true, true);


        //Freezes app until successful connection to the bridge
        //Important as the app will not render properly unless held at this point
        while(!bridgeConnection) {}

        //Appropriate listeners set to buttons here
        for(int i = 0; i < lightArray.length; i++) {
            //buttonArray[i].setOnClickListener(new myClickListener(lightArray[i]));
            buttonArray[i].setOnClickListener(new myClickListener(i));
        }
    }

    /**
     * Changes the state of given light to the given hue
     * @param light the light we want to change
     * @param hue desired color we want to change the light to
     */
    public void changeLight(PHLight light, int hue) {
        PHBridge bridge = sdk.getSelectedBridge();

        PHLightState lightState = new PHLightState();
        lightState.setHue(hue);
        bridge.updateLightState(light, lightState);
    }

    /**
     * Takes an integer and a color as an integer and changes a light from a list of lights
     * @param i the index of the desired light
     * @param hue the color to change selected light to
     */
    public void changeLight(int i, int hue) {
        PHBridge bridge = sdk.getSelectedBridge();
        List<PHLight> allLights = bridge.getResourceCache().getAllLights();

        PHLight light = allLights.get(i);

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

            // Here you receive notifications that the BridgeResource Cache was updated. Use the PHMessageType to
            // check which cache was updated, e.g.
            if (cacheNotificationsList.contains(PHMessageType.LIGHTS_CACHE_UPDATED)) {
                System.out.println("Lights Cache Updated ");
            }

            //This part used to reset buttons and listeners, but it totally doesn't work because
            //after onCreate finishes buttons and listeners seem to become immutable
            /*

            sdk.setSelectedBridge(bridge);
            sdk.enableHeartbeat(bridge, PHHueSDK.HB_INTERVAL);

            List<PHLight> allLights = bridge.getResourceCache().getAllLights();

            Iterator<PHLight> itr = allLights.iterator();


            buttons.removeAllViews();

            LinearLayout linearLayout = new LinearLayout(getApplicationContext());
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            int i = 1;

            while (itr.hasNext()) {
                cur = itr.next();

                Button button = new Button(getApplicationContext());
                button.setText("Light " + i);
                linearLayout.addView(button);

                button.setOnClickListener(new View.OnClickListener() {
                    PHLight buttonLight = cur;
                    public void onClick(View view) {
                        changeLight(buttonLight, new Random().nextInt(MAX_HUE));
                    }
                });
                ++i;
            }

            buttons.addView(linearLayout);
            buttons.postInvalidate();
            */
        }

        @Override
        public void onBridgeConnected(PHBridge b, String username) {
            //Sets bridge and heartbeat
            sdk.setSelectedBridge(b);
            sdk.enableHeartbeat(b, PHHueSDK.HB_INTERVAL);
            // Here it is recommended to set your connected bridge in your sdk object (as above) and start the heartbeat.
            // At this point you are connected to a bridge so you should pass control to your main program/activity.
            // The username is generated randomly by the bridge.
            // Also it is recommended you store the connected IP Address/ Username in your app here.  This will allow easy automatic connection on subsequent use.

            //creates an array of lights and sets appropriate buttons to visible
            List<PHLight> allLights = b.getResourceCache().getAllLights();
            Iterator<PHLight> itr = allLights.iterator();
            lightArray = new PHLight[allLights.size()];

            int i = 0;

            while (itr.hasNext()) {
                PHLight cur = itr.next();

                //Button button = new Button(getApplicationContext());
                //button.setText("Light " + i);
                lightArray[i] = cur;
                buttonArray[i].setVisibility(View.VISIBLE);
                //buttonArray[i].setOnClickListener(new myClickListener(cur));
                ++i;
            }

            //Allows the end of the onCreate loop and for the app to start
            bridgeConnection = true;
            //buttons.setLayoutParams((LinearLayout.LayoutParams) buttons.getLayoutParams());
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

    //disconnects from bridge and stops heartbeat here
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

    //Custom button click listener allowing for lights to be assigned
    class myClickListener implements OnClickListener {

        private PHLight buttonLight; //assigned light
        private int i; //index of assigned light

        //Creates a listener assigned to given light
        public myClickListener(PHLight inLight) {
            super();
            buttonLight = inLight;
        }

        //Creates a listener assigned to given index of a lgiht
        public myClickListener(int i) {
            this.i = i;
        }

        //onClick with lights assigned to buttons. commented out to make this program closer to the QuickStartApp
        //@Override
        //public void onClick(View v) { changeLight(buttonLight, new Random().nextInt(MAX_HUE)); }

        //onClick where buttons are assigned to an index
        @Override
        public void onClick(View v) { changeLight(i, new Random().nextInt(MAX_HUE)); }
    }
}

