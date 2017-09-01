package com.epapyrus.plugpdf.sample;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.epapyrus.plugpdf.core.PlugPDF;
import com.epapyrus.plugpdf.core.PlugPDFException.InvalidLicense;
import com.epapyrus.plugpdf.core.PlugPDFException.LicenseMismatchAppID;
import com.epapyrus.plugpdf.core.PlugPDFException.LicenseTrialTimeOut;
import com.epapyrus.plugpdf.core.PlugPDFException.LicenseUnusableOS;
import com.epapyrus.plugpdf.core.PlugPDFException.LicenseWrongProductVersion;

public class PlugPDFLauncherApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		String exceptionMsg = "";
		try {
			Log.d("Version", PlugPDF.getVersionName());
			PlugPDF.init(getApplicationContext(),
					"C9B85CC8HGE9GH82ED34H98F7DDAH9729BCDAACDAF7888HE6786FBF5");
			PlugPDF.deployAssetFontResource(getApplicationContext());

			PlugPDF.enableUncaughtExceptionHandler();
			PlugPDF.setUpdateCheckEnabled(false);
			// PlugPDF.setBitmapConfig(Bitmap.Config.RGB_565);
		} catch (LicenseWrongProductVersion ex) {
			exceptionMsg = "This license key is not valid for this version of the PlugPDF SDK.";
		} catch (LicenseTrialTimeOut ex) {
			exceptionMsg = "Your trial period has expired.";
		} catch (LicenseUnusableOS ex) {
			exceptionMsg = "This license key is not valid for the Android platform.";
		} catch (LicenseMismatchAppID ex) {
			exceptionMsg = "This license key does not match the App ID.";
		} catch (InvalidLicense ex) {
			exceptionMsg = "This license key is not valid.";
		} catch (Exception ex) {
			if (ex.getMessage() == null) {
				exceptionMsg = "An unknown error occurred.";
			} else {
				exceptionMsg = ex.getMessage();
			}
		}
        if (exceptionMsg.length() > 0) {
			Toast.makeText(getApplicationContext(), exceptionMsg, Toast.LENGTH_LONG).show();
			Log.e("Exception", exceptionMsg);
		}
	}
}
