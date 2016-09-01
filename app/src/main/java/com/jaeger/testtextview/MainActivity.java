package com.jaeger.testtextview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getSimpleName();

    private TextView mTvTest;
    private Button btnTest;
    private View mLlRoot;

    SelectableText mSelectableText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvTest = (TextView) findViewById(R.id.tv_test);
        btnTest = (Button) findViewById(R.id.btn_test);
        mLlRoot = findViewById(R.id.ll_root);

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mTvTest.setText("Hello World!1");
                //mTvTest.setText("Hello World!2");
                //mTvTest.setText("Hello World!3");
                mTvTest.setText(null);
            }
        });

        mSelectableText = new SelectableText(mTvTest);
        mSelectableText.setSelectListener(new OnSelectListener() {
            @Override
            public void onTextSelected(CharSequence content) {
                Toast.makeText(MainActivity.this, content.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStop() {
        mSelectableText.destroy();
        super.onStop();
    }
}
