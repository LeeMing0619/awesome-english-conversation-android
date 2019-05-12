package com.convoenglishllc.expression.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.convoenglishllc.expression.R;
import com.convoenglishllc.expression.widget.ProgressIndicator;

/**
 * Created by Ryang on 6/21/2016.
 */
public class DownloadStatusDialog extends Dialog {
    private static DownloadStatusDialog dialog = null;
    private ProgressIndicator downloadProgress;
    private TextView tvTitle;
    public DownloadStatusDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_download_status);

        this.setCancelable(false);
        downloadProgress = (ProgressIndicator) findViewById(R.id.pi_download_status);
        tvTitle = (TextView) findViewById(R.id.title_download);
    }
    public void setProgress(float progress) {
        try {
            downloadProgress.setProgress(progress);
        } catch (Exception e) {

        }
    }

    public void setTitle(int nCurrent, int nTotalCount) {
        tvTitle.setText("Downloading " + nCurrent + "/" + nTotalCount);
    }

    public static void setDownloadProgress(int nCurrent, int nTotalCount) {
        if (dialog == null) {
            return;
        }

        float fProgress = (float) nCurrent / (float) nTotalCount;
        dialog.setProgress(fProgress);
        //dialog.setTitle(nCurrent, nTotalCount);
    }
    public static DownloadStatusDialog showDialog(Context context) {
        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = new DownloadStatusDialog(context);
        dialog.show();
        return dialog;
    }

    public static void hideDialog() {
        if (dialog == null)
            return;
        dialog.dismiss();
    }
}
