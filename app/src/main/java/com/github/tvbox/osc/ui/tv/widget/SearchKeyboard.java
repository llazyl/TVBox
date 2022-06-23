package com.github.tvbox.osc.ui.tv.widget;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.tvbox.osc.R;


/**
 * @author pj567
 * @date :2020/12/23
 * @description:
 */
public class SearchKeyboard extends FrameLayout {
    private RecyclerView mRecyclerView;
    private List<String> keys = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M"
            , "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "1", "2", "3", "4", "5", "6", "7", "8", "9"
            , "0", "删除", "清空");
    private List<Keyboard> keyboardList = new ArrayList<>();
    private TextView hint;
    private TextView search;
    private OnSearchKeyListener searchKeyListener;
    private OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View itemView, boolean hasFocus) {
            if (null != itemView && itemView != mRecyclerView) {
                itemView.setSelected(hasFocus);
            }
        }
    };

    public SearchKeyboard(@NonNull Context context) {
        this(context, null);
    }

    public SearchKeyboard(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchKeyboard(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.keyboard_layout, this);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.mRecyclerView);
        hint = (TextView) view.findViewById(R.id.hint);
        search = (TextView) view.findViewById(R.id.search);
        GridLayoutManager manager = new GridLayoutManager(getContext(), 5);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View child) {
                if (child.isFocusable() && null == child.getOnFocusChangeListener()) {
                    child.setOnFocusChangeListener(focusChangeListener);
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {

            }
        });
        String html = "请输入全拼或者首字母查找影片<br>首字母如：" + "<font color=\"#0CADE2\">“金刚川”</font>输入<font color=\"#0CADE2\">“JGC”</font>" +
                "<br>部分影片可能搜索不出来";
        hint.setText(Html.fromHtml(html));
        int size = keys.size();
        for (int i = 0; i < size; i++) {
            if (i < size - 2) {
                keyboardList.add(new Keyboard(1, keys.get(i)));
            } else {
                keyboardList.add(new Keyboard(2, keys.get(i)));
            }
        }
        final KeyboardAdapter adapter = new KeyboardAdapter(keyboardList);
        mRecyclerView.setAdapter(adapter);
        adapter.setSpanSizeLookup(new BaseQuickAdapter.SpanSizeLookup() {
            @Override
            public int getSpanSize(GridLayoutManager gridLayoutManager, int position) {
                if (position == adapter.getData().size() - 2 || position == adapter.getData().size() - 1) {
                    return 2;
                }
                return 1;
            }

        });

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Keyboard keyboard = (Keyboard) adapter.getItem(position);
                String text = search.getText().toString();
                if (position == adapter.getData().size() - 1) {
                    text = "";
                } else if (position == adapter.getData().size() - 2) {
                    text = text.substring(0, text.length() - 1);
                } else {
                    text += keyboard.getKey();
                }
                search.setText(text);
                if (searchKeyListener != null) {
                    searchKeyListener.onSearchKey(text);
                }
            }
        });
    }

    static class Keyboard implements MultiItemEntity {
        private int itemType;
        private String key;

        private Keyboard(int itemType, String key) {
            this.itemType = itemType;
            this.key = key;
        }

        @Override
        public int getItemType() {
            return itemType;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    private static class KeyboardAdapter extends BaseMultiItemQuickAdapter<Keyboard, BaseViewHolder> {

        private KeyboardAdapter(List<Keyboard> data) {
            super(data);
            addItemType(1, R.layout.item_keyboard_1);
            addItemType(2, R.layout.item_keyboard_2);
        }

        @Override
        protected void convert(BaseViewHolder helper, Keyboard item) {
            switch (helper.getItemViewType()) {
                case 2:
                    if (helper.getAdapterPosition() == mData.size() - 2) {
                        helper.setImageResource(R.id.img, R.drawable.delete);
                    } else if (helper.getAdapterPosition() == mData.size() - 1) {
                        helper.setImageResource(R.id.img, R.drawable.clear);
                    }
                case 1:
                    helper.setText(R.id.keyName, item.key);
                    break;
            }
        }
    }

    public void setOnSearchKeyListener(OnSearchKeyListener listener) {
        searchKeyListener = listener;
    }

    public interface OnSearchKeyListener {
        void onSearchKey(String key);
    }
}