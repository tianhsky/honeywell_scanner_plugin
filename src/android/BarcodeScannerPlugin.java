package org.pluginporo.honeywell;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;

import org.pluginporo.aamva.Decoder;
import org.pluginporo.aamva.DriverLicense;

import com.honeywell.decodemanager.DecodeManager;
import com.honeywell.decodemanager.barcode.DecodeResult;

public class BarcodeScannerPlugin extends CordovaPlugin {

	private static final String LOG_TAG = "BarcodeScannerPlugin";
	private static final int SCANTIMEOUT = 5000;

	DecodeManager decodeManager = null;
	BroadcastReceiver scannerReceiver = null;
	CallbackContext pluginCallbackContext = null;

	public BarcodeScannerPlugin() {
	}

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		if (action.equals("scan")) {
			this.pluginCallbackContext = callbackContext;

			if ((decodeManager == null) && (Build.MODEL.toLowerCase().contains("dolphin 70e".toLowerCase()))) {
				decodeManager = new DecodeManager(((CordovaActivity)this.cordova.getActivity()), ScanResultHandler);
			}
			try {
				this.doScan();
				return true;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if (action.equals("stop")) {
			callbackContext.success("stopped");
			return true;
		}
		return false;
	}

	private Handler ScanResultHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DecodeManager.MESSAGE_DECODER_COMPLETE:
				String strDecodeResult = "";
				DecodeResult decodeResult = (DecodeResult) msg.obj;

				byte codeid = decodeResult.codeId;
				byte aimid = decodeResult.aimId;
				int iLength = decodeResult.length;
				String r = decodeResult.barcodeData;
				sendUpdate(getScannedInfo(r), false);
				pluginCallbackContext.success("done");
				break;
			case DecodeManager.MESSAGE_DECODER_FAIL: 
				break;
			case DecodeManager.MESSAGE_DECODER_READY: 
				ArrayList<java.lang.Integer> arry = decodeManager.getSymConfigActivityOpeartor().getAllSymbologyId();
				boolean b = arry.isEmpty();
				break;
			default:
				super.handleMessage(msg);
				break;
			}
		}
	};

	private DriverLicense parseDL(String barcode){
		DriverLicense dl = null;
		try {
			dl = new DriverLicense(barcode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dl;
	}

	private void doScan() throws Exception {
		try {
			decodeManager.doDecode(SCANTIMEOUT);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private JSONObject getScannedInfo(String barcode) {
		DriverLicense dl = parseDL(barcode);
	    JSONObject obj = new JSONObject();
	    try {
	      obj.put("barcode", barcode);
	      if(dl != null){
	      	obj.put("dln", dl.getDriverLicenseNumber());
	      	obj.put("first_name", dl.getFirstName());
	      	obj.put("last_name", dl.getLastName());
	      }
	    } catch (JSONException e) {
	      Log.e(LOG_TAG, e.getMessage(), e);
	    }
	    return obj;
  	}

	private void sendUpdate(JSONObject info, boolean keepCallback) {
	    if (this.pluginCallbackContext != null) {
			PluginResult result = new PluginResult(PluginResult.Status.OK, info);
			result.setKeepCallback(keepCallback);
			this.pluginCallbackContext.sendPluginResult(result);
		}
	}

}
