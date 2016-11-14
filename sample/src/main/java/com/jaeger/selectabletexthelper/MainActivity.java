package com.jaeger.selectabletexthelper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.jaeger.library.OnSelectListener;
import com.jaeger.library.SelectableTextHelper;

public class MainActivity extends AppCompatActivity {

    private TextView mTvTest;

    private SelectableTextHelper mSelectableTextHelper;
    private LinearLayout llRoot;
    private TextView tvTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvTest = (TextView) findViewById(R.id.tv_test);
        //mTvTest.setTextIsSelectable(true);

        mSelectableTextHelper = new SelectableTextHelper.Builder(mTvTest)
            .setSelectedColor(getResources().getColor(R.color.selected_blue))
            .setCursorHandleSizeInDp(20)
            .setCursorHandleColor(getResources().getColor(R.color.cursor_handle_color))
            .build();

        mSelectableTextHelper.setSelectListener(new OnSelectListener() {
            @Override
            public void onTextSelected(CharSequence content) {

            }
        });
        initView();
    }

    private void initView() {
        llRoot = (LinearLayout) findViewById(R.id.ll_root);
        tvTest = (TextView) findViewById(R.id.tv_test);
    }
}
