Cordova Honeywell Barcode Scanner Plugin
==============

Honeywell Dolphin 70E Barcode Scanner plugin for Cordova / PhoneGap.

## Supported Platforms

- Android

## How to use

### Install

	cordova plugin add org.pluginporo.honeywell_scanner_plugin

### Scan

	navigator.honeywell_scanner_plugin.scan(
		function(scanResult){
			// scanResult contains {barcode, dln, first_name, last_name}
			console.log(scanResult.barcode);
		},
		function(err){
			// error callback
			console.log(err);
		}
	);
