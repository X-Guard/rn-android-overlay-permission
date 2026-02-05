
package com.overlaypermission;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;

import java.util.ArrayList;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import android.app.Activity;

public class OverlayPermissionModule extends ReactContextBaseJavaModule {

    private static final int OVERLAY_PERMISSION_REQ_CODE = 3456;
    private Promise overlayPermissionPromise;

    private final ReactApplicationContext reactContext;
    public OverlayPermissionModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.reactContext.addActivityEventListener(mActivityEventListener);
    }

    @Override
    public String getName() {
        return "OverlayPermissionModule";
    }

    @ReactMethod
    public void requestOverlayPermission(Promise promise) {
        /**
         *  Before android 6.0 Marshmallow you dont need to ask for canDrawOverlays permission,
         *  but in newer android versions this is mandatory
         */
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this.reactContext)) {
                    Activity currentActivity = getCurrentActivity();
                    if (currentActivity == null) {
                        promise.reject("NO_ACTIVITY", "Current activity is null");
                        return;
                    }
                    this.overlayPermissionPromise = promise;
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + this.reactContext.getPackageName()));
                    currentActivity.startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                } else {
                    promise.resolve(true);
                }
            } else {
                promise.resolve(true);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }
    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
            if (requestCode == OVERLAY_PERMISSION_REQ_CODE && overlayPermissionPromise != null) {
                boolean granted = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    granted = Settings.canDrawOverlays(reactContext);
                }
                overlayPermissionPromise.resolve(granted);
                overlayPermissionPromise = null;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @ReactMethod
    public void isRequestOverlayPermissionGranted(Promise promise) {
        boolean equal= Settings.canDrawOverlays(this.reactContext);
        promise.resolve(equal);
    }

}
