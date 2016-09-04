package com.jaeger.selectabletexthelper;

import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.jaeger.library.SelectableTextHelper;

public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getSimpleName();

    private TextView mTvTest;
    private Button btnTest;
    private View mLlRoot;

    SelectableTextHelper mSelectableText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvTest = (TextView) findViewById(R.id.tv_test);
        btnTest = (Button) findViewById(R.id.btn_test);
        mLlRoot = findViewById(R.id.ll_root);

        //mTvTest.setTextIsSelectable(true);

        mSelectableText = new SelectableTextHelper(mTvTest);
        mSelectableText.setSelectListener(new com.jaeger.selectabletexthelper.OnSelectListener() {
            @Override
            public void onTextSelected(CharSequence content) {
                android.content.ClipboardManager clip =
                    (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clip.setPrimaryClip(ClipData.newPlainText(content, content));
                //Toast.makeText(MainActivity.this, content.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        //mSelectableText.destroy();
        super.onPause();
    }
}
