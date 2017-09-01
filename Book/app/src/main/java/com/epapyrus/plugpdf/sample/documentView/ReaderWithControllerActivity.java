package com.epapyrus.plugpdf.sample.documentView;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.epapyrus.plugpdf.SimpleDocumentReader;
import com.epapyrus.plugpdf.SimpleDocumentReaderListener;
import com.epapyrus.plugpdf.SimpleReaderFactory;
import com.epapyrus.plugpdf.core.viewer.DocumentState.OPEN;
import com.epapyrus.plugpdf.core.viewer.ReaderView;

import java.io.InputStream;

public class ReaderWithControllerActivity extends Activity {

	private SimpleDocumentReader mReader;
	private String fileName;

	public ReaderWithControllerActivity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		mReader = SimpleReaderFactory.createSimpleViewer(this, listener);
		if (fileName != null && fileName.length() > 0) {
			mReader.openFile(fileName, "");

		} else {
			byte[] data = readAssetFile("book.pdf");
			mReader.openData(data, data.length, "");
		}

		ReaderView.setEnableUseRecentPage(true);
	}

	private SimpleDocumentReaderListener listener = new SimpleDocumentReaderListener() {
		
		@Override
		public void onLoadFinish(OPEN state) {
			Log.i("PlugPDF", "[INFO] Open " + state);
		}
	};

	private byte[] readAssetFile(String fileName) {
		
		AssetManager am = getResources().getAssets();
		byte[] data = null;
		
		try {
			
			InputStream is = am.open(fileName);

			int size = is.available();
			if (size > 0) {
				data = new byte[size];
				is.read(data);
			}

			is.close();
			
		} catch (Exception ex) {
			Log.e("PlugPDF", "[ERROR] open fail because, ", ex);
		}
		
		return data;
	}

	@Override
	protected void onDestroy() {
		if (mReader.getDocument() != null) {
			mReader.save();
			mReader.clear();
		}
		super.onDestroy();
	}
}
