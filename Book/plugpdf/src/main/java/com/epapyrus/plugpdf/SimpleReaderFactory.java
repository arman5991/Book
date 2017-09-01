package com.epapyrus.plugpdf;

import android.app.Activity;

public class SimpleReaderFactory {

    public static SimpleDocumentReader createSimpleViewer(Activity act,
                                                          SimpleDocumentReaderListener listener) {

        SimpleDocumentReader viewer = new SimpleDocumentReader(act);
        viewer.setListener(listener);

        return viewer;
    }
}
