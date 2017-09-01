package com.epapyrus.plugpdf;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.epapyrus.plugpdf.core.CoordConverter;
import com.epapyrus.plugpdf.core.PDFDocument;
import com.epapyrus.plugpdf.core.annotation.acroform.BaseField;
import com.epapyrus.plugpdf.core.annotation.acroform.TextField;
import com.epapyrus.plugpdf.core.annotation.tool.AnnotToolTransform;
import com.epapyrus.plugpdf.core.viewer.BasePlugPDFDisplay.PageDisplayMode;
import com.epapyrus.plugpdf.core.viewer.DocumentState;
import com.epapyrus.plugpdf.core.viewer.DocumentState.OPEN;
import com.epapyrus.plugpdf.core.viewer.PageView;
import com.epapyrus.plugpdf.core.viewer.ReaderListener;
import com.epapyrus.plugpdf.core.viewer.ReaderView;

public class SimpleDocumentReader implements ReaderListener {

	private ReaderView mReaderView;
	private SimpleReaderControlView mControlView;
	private SimpleDocumentReaderListener mListener;
	private Activity mAct;
	private String	mFilePath = null;
	private byte[] mFileData = null;

	public SimpleDocumentReader(Activity act) {
		mAct = act;
		mReaderView = new ReaderView(mAct);
		mReaderView.setReaderListener(this);

		mControlView = (SimpleReaderControlView) SimpleReaderControlView
				.inflate(mAct, R.layout.simple_reader_control, null);
		mControlView.createUILayout(mReaderView);
	}

	public void setListener(SimpleDocumentReaderListener listener) {
		mListener = listener;
	}

	public void openFile(String filePath, String password) {
		mFilePath = filePath;
		mReaderView.openFile(filePath, password);
	}

	public void openData(byte[] data, int len, String password) {
		mFileData = data;
		mReaderView.openData(data, len, password);
	}

	public PDFDocument getDocument() {
		return mReaderView.getDocument();
	}

	private void goToPage(int pageIdx) {
		mReaderView.goToPage(pageIdx);
	}


	public void save() {
		mReaderView.save();
	}

	@SuppressLint("InflateParams")
	@Override
	public void onLoadFinish(DocumentState.OPEN state) {
		if (state == OPEN.SUCCESS) {
			mControlView.init(mAct);

			RelativeLayout layout = new RelativeLayout(mAct);
			layout.addView(mReaderView);
			layout.addView(mControlView);

			mAct.setContentView(layout);

			CoordConverter.initCoordConverter(mAct, mReaderView);

			goToPage(0);
		} else if (state == OPEN.WRONG_PASSWD) {

//			PasswordDialog dialog = new PasswordDialog(mAct) {
//
//				@Override
//				public void onInputtedPassword(String password) {
//					if (null != mFilePath) {
//						mReaderView.openFile(mFilePath, password);
//					} else if (null != mFileData) {
//						mReaderView.openData(mFileData, mFileData.length, password);
//					}
//				}
//			};
//			dialog.show();
		}

		if (mListener != null) {
			mListener.onLoadFinish(state);
		}
	}

	@Override
	public void onSearchFinish(boolean success) {
		if (success) {
			if (mReaderView.getPageDisplayMode() == PageDisplayMode.THUMBNAIL) {
				mControlView.setHorizontalMode();
			}
		}
	}

	@Override
	public void onGoToPage(int pageIdx, int pageCount) {
		mControlView.updatePageNumber(pageIdx, pageCount);
	}

	@Override
	public void onSingleTapUp(MotionEvent e) {
		boolean isTextFieldFocused = false;
		PageView pageView = mReaderView.getPlugPDFDisplay().getPageView(mReaderView.getPageIdx());
		float correctX = (e.getRawX() - pageView.getLeft() - mReaderView.getLeft()) / pageView.getAnnotScale();
		float correctY = (e.getRawY() - pageView.getTop() - mReaderView.getTop()) / pageView.getAnnotScale();
	e.getX();
		for(BaseField textField : mReaderView.getAllField(mReaderView.getPageIdx())){
			if(textField instanceof TextField && textField.getRect().contains(correctX, correctY)){
				isTextFieldFocused = true;

			}
		}
		if(!(mReaderView.getAnnotToolType() instanceof AnnotToolTransform))
			mControlView.toggleControlTabBar(isTextFieldFocused);
	}

	@Override
	public void onDoubleTapUp(MotionEvent e) {

	}

	@Override
	public void onScroll(int distanceX, int distanceY) {
		mControlView.hideTopMenu();
	}

	@Override
	public void onChangeDisplayMode(PageDisplayMode mode ,int pageIndex) {
		if (mode == PageDisplayMode.HORIZONTAL) {
			mControlView.setHorizontalMode();
		} else if (mode == PageDisplayMode.VERTICAL) {
			mControlView.setVerticalMode();
		} else if (mode == PageDisplayMode.CONTINUOS) {
			mControlView.setContinuosMode();
		} else if (mode == PageDisplayMode.BILATERAL_VERTICAL) {
			mControlView.setBilateralVerticalMode();
		} else if (mode == PageDisplayMode.BILATERAL_HORIZONTAL) {
			mControlView.setBilateralHorizontalMode();
		} else if (mode == PageDisplayMode.BILATERAL_REALISTIC) {
			mControlView.setBilateralRealisticMode();
		} else if (mode == PageDisplayMode.THUMBNAIL) {
			mControlView.setThumbnailMode();
		}
	}

	@Override
	public void onChangeZoom(double zoomLevel) {

	}

	public void clear() {
		mReaderView.clear();
		mFileData = null;
		mFilePath = null;
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}
}
