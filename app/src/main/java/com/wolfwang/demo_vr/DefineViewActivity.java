package com.wolfwang.demo_vr;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.wolfwang.demo_vr.widget.TinglingSquaresView;

public class DefineViewActivity extends AppCompatActivity {
    private TinglingSquaresView tinglingSquaresView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_define_view_test);
        tinglingSquaresView = (TinglingSquaresView) findViewById(R.id.ts_define);
        tinglingSquaresView.init();
        tinglingSquaresView.runAnimation(10*1000);
    }
}
