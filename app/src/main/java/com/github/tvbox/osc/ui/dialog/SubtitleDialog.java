package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.util.FastClickCheckUtil;

import org.jetbrains.annotations.NotNull;

public class SubtitleDialog extends BaseDialog {

    private TextView selectLocal;
    private TextView selectRemote;

    private SearchSubtitleListener mSearchSubtitleListener;
    private LocalFileChooserListener mLocalFileChooserListener;

    public SubtitleDialog(@NonNull @NotNull Context context) {
        super(context, R.style.CustomDialogStyleDim);
        setCanceledOnTouchOutside(false);
        setCancelable(true);
        setContentView(R.layout.dialog_subtitle);
        init(context);
    }

    private void init(Context context) {
        selectLocal = findViewById(R.id.selectLocal);
        selectRemote = findViewById(R.id.selectRemote);

        selectLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                dismiss();
                mLocalFileChooserListener.openLocalFileChooserDialog();
            }
        });

        selectRemote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FastClickCheckUtil.check(view);
                dismiss();
                mSearchSubtitleListener.openSearchSubtitleDialog();
            }
        });
    }

    public void setLocalFileChooserListener(LocalFileChooserListener localFileChooserListener) {
        mLocalFileChooserListener = localFileChooserListener;
    }

    public interface LocalFileChooserListener {
        void openLocalFileChooserDialog();
    }

    public void setSearchSubtitleListener(SearchSubtitleListener searchSubtitleListener) {
        mSearchSubtitleListener = searchSubtitleListener;
    }

    public interface SearchSubtitleListener {
        void openSearchSubtitleDialog();
    }
}
