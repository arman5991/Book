package com.epapyrus.plugpdf;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.epapyrus.plugpdf.SimpleReaderControlPanel.PanelType;
import com.epapyrus.plugpdf.core.BaseReaderControl;
import com.epapyrus.plugpdf.core.PlugPDF;
import com.epapyrus.plugpdf.core.viewer.BasePlugPDFDisplay.PageDisplayMode;

public class SimpleReaderControlView extends RelativeLayout {

    private BaseReaderControl mController;
    private SimpleReaderControlPanel mControlPanel;
    Activity mAct;

    private boolean mButtonsVisible;
    private boolean mTopBarIsSearch;

    private ViewFlipper mTopBarSwitcher;
    private TextView mPageNumberView;
    private ImageView mPageThumbnail;
    private SeekBar mPageSlider;
    private Button mSearchButton;
    private Button mOutlineButton;

    private Button mSearchCancelButton;
    private EditText mSearchText;
    private Button mSearchBack;
    private Button mSearchFwd;

    private Button mPageDisplayModeButton;

    private int mPageIdx;
    private Bitmap mBitmap = null;

    private boolean enableHiddenTopBar;
    private boolean enableHiddenBottomBar;

    private BitmapCache mBitmapCache;

    public SimpleReaderControlView(Context context) {
        super(context);
    }

    public SimpleReaderControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void createUILayout(BaseReaderControl controller) {

        mController = controller;

        mTopBarSwitcher = (ViewFlipper) findViewById(R.id.flipper);
        mPageNumberView = (TextView) findViewById(R.id.rc_page_number);
        mPageThumbnail = (ImageView) findViewById(R.id.rc_page_thumbnail);
        mPageSlider = (SeekBar) findViewById(R.id.rc_page_slider);
        mSearchButton = (Button) findViewById(R.id.rc_search);
        mSearchCancelButton = (Button) findViewById(R.id.rc_search_cancel);
        mSearchText = (EditText) findViewById(R.id.rc_search_text);
        mSearchBack = (Button) findViewById(R.id.rc_search_back);
        mSearchFwd = (Button) findViewById(R.id.rc_search_forward);
        mOutlineButton = (Button) findViewById(R.id.rc_outline);
        mPageDisplayModeButton = (Button) findViewById(R.id.rc_page_display_mode);
    }

    public void init(Activity act) {
        mAct = act;

        mControlPanel = new SimpleReaderControlPanel(getContext(), this,
                mController);
        showOutlineButton(true);

        setEnableHiddenTopBar(false);
        setEnableHiddenBottomBar(false);
        mTopBarSwitcher.setVisibility(View.INVISIBLE);
        mPageThumbnail.setVisibility(View.INVISIBLE);
        mPageNumberView.setVisibility(View.INVISIBLE);
        mPageSlider.setVisibility(View.INVISIBLE);

        mBitmapCache = new BitmapCache();

        mPageSlider
                .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mController.goToPage(seekBar.getProgress());
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress, boolean fromUser) {
                        updatePageNumber(seekBar.getProgress() + 1,
                                seekBar.getMax() + 1);
                    }
                });

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                searchModeOn();
            }
        });

        mSearchCancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                searchModeOff();
            }
        });

        mSearchBack.setEnabled(false);
        mSearchFwd.setEnabled(false);

        mSearchText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                boolean haveText = s.toString().length() > 0;
                mSearchBack.setEnabled(haveText);
                mSearchFwd.setEnabled(haveText);

                mController.resetSearchInfo();
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }
        });

        mSearchText
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    public boolean onEditorAction(TextView v, int actionId,
                                                  KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_NEXT
                                || actionId == EditorInfo.IME_ACTION_DONE) {
                            mController.search(
                                    mSearchText.getText().toString(), 1);
                        }
                        return false;
                    }
                });

        mSearchBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mController.search(mSearchText.getText().toString(), -1);
            }
        });
        mSearchFwd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mController.search(mSearchText.getText().toString(), 1);
            }
        });

        mPageDisplayModeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mControlPanel.show(PanelType.DISPLAYMODE, v);
            }
        });
    }

    public void toggleControlTabBar(boolean isTextFieldFocused) {
        if (!mButtonsVisible)
            show();
        else if (!isTextFieldFocused) hideTopMenu();
    }

    public void show() {
        if (mButtonsVisible)
            return;

        mButtonsVisible = true;

        if (mTopBarIsSearch) {
            mSearchText.requestFocus();
            showKeyboard();
        }
        if (!isEnableHiddenTopBar()) {
            Animation anim = new TranslateAnimation(0, 0,
                    -mTopBarSwitcher.getHeight(), 0);
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mTopBarSwitcher.setVisibility(View.VISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                }
            });
            mTopBarSwitcher.startAnimation(anim);
        }
        if (!isEnableHiddenBottomBar()) {
            Animation anim = new TranslateAnimation(0, 0,
                    mPageSlider.getHeight(), 0);
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mPageSlider.setVisibility(View.VISIBLE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    mPageNumberView.setVisibility(View.VISIBLE);
                    mPageThumbnail.setVisibility(View.VISIBLE);
                }
            });
            mPageSlider.startAnimation(anim);
        }
    }

    public void hideTopMenu() {
        if (!mButtonsVisible)
            return;
        mButtonsVisible = false;

        hideKeyboard();
        if (!isEnableHiddenTopBar()) {
            Animation anim = new TranslateAnimation(0, 0, 0,
                    -mTopBarSwitcher.getHeight());
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    mTopBarSwitcher.setVisibility(View.INVISIBLE);
                }
            });
            mTopBarSwitcher.startAnimation(anim);
        }
        if (!isEnableHiddenBottomBar()) {
            Animation anim = new TranslateAnimation(0, 0, 0,
                    mPageSlider.getHeight());
            anim.setDuration(200);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                    mPageNumberView.setVisibility(View.INVISIBLE);
                    mPageThumbnail.setVisibility(View.GONE);
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    mPageSlider.setVisibility(View.INVISIBLE);
                }
            });
            mPageSlider.startAnimation(anim);
        }
    }

    private AsyncTask<Void, Void, Void> prevFetchTask;

    public void updatePageNumber(int pageIdx, int pageCount) {
        mPageNumberView.setText(String.format("%d/%d", pageIdx, pageCount));
        mPageSlider.setMax(pageCount - 1);
        mPageSlider.setProgress(pageIdx - 1);
        mPageIdx = pageIdx - 1;
        if (mTopBarIsSearch && !mSearchText.getText().toString().isEmpty()) {
            mController.search(mSearchText.getText().toString(), 0);
        }

        if (prevFetchTask != null) {
            prevFetchTask.cancel(true);
        }

        prevFetchTask = new android.os.AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... v) {
                int pageHeight = 200;
                PointF pageSize = mController.getPageSize(mPageIdx);
                int pageWidth = pageHeight * (int) pageSize.x / (int) pageSize.y;

                mBitmap = Bitmap.createBitmap(pageWidth, pageHeight, PlugPDF.bitmapConfig());

                if (this.isCancelled()) {
                    return null;
                }
                Bitmap bitmap = mBitmapCache.bitmap(mPageIdx);
                if (bitmap != null && !bitmap.isRecycled()) {
                    mBitmap = bitmap;
                } else {
                    int pageIdx = mPageIdx;
                    mController.drawPage(mBitmap, pageIdx, pageWidth, pageHeight, 0, 0, pageWidth, pageHeight);
                    mBitmapCache.addBitmap(pageIdx, mBitmap);
                }

                return null;
            }

            protected void onPreExecute() {
                mPageThumbnail.setImageBitmap(mBitmap);
            }

            protected void onPostExecute(Void ret) {
                if (mBitmap != null) {
                    mPageThumbnail.setImageBitmap(mBitmap);
                }
                SimpleReaderControlView.this.prevFetchTask = null;
            }
        };

        prevFetchTask.execute();
    }

    class BitmapCache {
        private LruCache<Integer, Bitmap> mCache;

        BitmapCache() {
            int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            int cacheSize = maxMemory / 8;

            mCache = new LruCache<Integer, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(Integer key, Bitmap bitmap) {
                    return bitmap.getByteCount() / 1024;
                }
            };
        }

        public void addBitmap(Integer key, Bitmap bitmap) {
            if (bitmap(key) == null) {
                mCache.put(key, bitmap);
            }
        }

        public Bitmap bitmap(Integer key) {
            return mCache.get(key);
        }
    }

    private void setSearchMode(boolean on) {
        if (on) {
            mTopBarSwitcher.showNext();
        } else {
            mTopBarSwitcher.showPrevious();
        }
    }

    void searchModeOn() {
        mTopBarIsSearch = true;
        mSearchText.requestFocus();
        showKeyboard();
        setSearchMode(true);
    }

    void searchModeOff() {
        mTopBarIsSearch = false;
        hideKeyboard();
        setSearchMode(false);
        mController.resetSearchInfo();
    }

    void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(mSearchText, 0);
        }
    }

    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
        }
    }

    void showOutlineButton(boolean show) {
        if (show) {
            mOutlineButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mControlPanel.show(PanelType.OUTLINE, v);
                }
            });
        } else {
            mOutlineButton.setVisibility(View.GONE);
        }
    }

    void setHorizontalMode() {
        mPageDisplayModeButton
                .setBackgroundResource(R.drawable.st_btn_view_mode_horizontal);
        mController.setPageDisplayMode(PageDisplayMode.HORIZONTAL);
    }

    void setBilateralVerticalMode() {
        mPageDisplayModeButton
                .setBackgroundResource(R.drawable.st_btn_view_mode_bilateral);
        mController.setPageDisplayMode(PageDisplayMode.BILATERAL_VERTICAL);
    }

    void setBilateralHorizontalMode() {
        mPageDisplayModeButton
                .setBackgroundResource(R.drawable.st_btn_view_mode_bilateral);
        mController.setPageDisplayMode(PageDisplayMode.BILATERAL_HORIZONTAL);
    }

    void setBilateralRealisticMode() {
        mPageDisplayModeButton
                .setBackgroundResource(R.drawable.st_btn_view_mode_bilateral);
        mController.setPageDisplayMode(PageDisplayMode.BILATERAL_REALISTIC);
    }

    void setVerticalMode() {
        mPageDisplayModeButton
                .setBackgroundResource(R.drawable.st_btn_view_mode_vertical);
        mController.setPageDisplayMode(PageDisplayMode.VERTICAL);
    }

    void setContinuosMode() {
        mPageDisplayModeButton
                .setBackgroundResource(R.drawable.st_btn_view_mode_vertical);
        mController.setPageDisplayMode(PageDisplayMode.CONTINUOS);
    }

    void setThumbnailMode() {
        mPageDisplayModeButton
                .setBackgroundResource(R.drawable.st_btn_view_mode_thumbnail);
        mController.setPageDisplayMode(PageDisplayMode.THUMBNAIL);
        mPageSlider.setVisibility(INVISIBLE);
        mPageNumberView.setVisibility(INVISIBLE);
        mPageThumbnail.setVisibility(INVISIBLE);
    }

    public boolean isEnableHiddenTopBar() {
        return enableHiddenTopBar;
    }

    public void setEnableHiddenTopBar(boolean enableHiddenTopBar) {
        this.enableHiddenTopBar = enableHiddenTopBar;
    }

    public boolean isEnableHiddenBottomBar() {
        return enableHiddenBottomBar;
    }

    public void setEnableHiddenBottomBar(boolean enableHiddenBottomBar) {
        this.enableHiddenBottomBar = enableHiddenBottomBar;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mControlPanel.refreshLayout();
    }
}
