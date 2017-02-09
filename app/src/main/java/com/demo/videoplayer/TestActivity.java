package com.demo.videoplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * 注意：如果Activity继承自AppCompatActivity，必须先调用 getDelegate()
         */
        getDelegate().requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_test);
    }
}
