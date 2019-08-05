package com.wolfwang.demo_vr.cylinder;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.wolfwang.demo_vr.GestureTouchUtils;
import com.wolfwang.demo_vr.widget.MyGLSurfaceView;

import java.lang.ref.WeakReference;

import static android.opengl.GLSurfaceView.DEBUG_CHECK_GL_ERROR;
import static android.opengl.GLSurfaceView.DEBUG_LOG_GL_CALLS;

public class OPENGLTestActivityForCylinder extends AppCompatActivity {
    GLPhotoViewForCylinder glSurfaceView;
//    public static float mAngleX = 0;// 摄像机所在的x坐标
//    public static float mAngleY = 0;// 摄像机所在的y坐标
//    public static float mAngleZ = 3;// 摄像机所在的z坐标
//    Button roate_btn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setContentView(R.layout.activity_opengltest);
        glSurfaceView = new GLPhotoViewForCylinder(this);
//        glSurfaceView = (com.wolfwang.demo_vr.widget.MyGLSurfaceView) findViewById(R.id.gl_view);
        glSurfaceView.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
//        setContentView(glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.init(this);
        glSurfaceView.setRenderer();
        glSurfaceView.setDebugFlags(DEBUG_CHECK_GL_ERROR |DEBUG_LOG_GL_CALLS);
//        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        setContentView(glSurfaceView);
//        roate_btn = (Button) findViewById(R.id.button_roate);
//        roate_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startRoate();
//            }
//        });
    }

    private void startRoate(){
//        glSurfaceView.play();
//        GestureTouchUtils.simulateScroll(glSurfaceView, 10000, 400, 0, 400, 100000, GestureTouchUtils.HIGH);
    }

    ViewHandler viewHandler ;

    static class ViewHandler extends Handler {
        WeakReference<View> mView;

        ViewHandler(View activity) {
            super(Looper.getMainLooper());
            mView = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            View theView = mView.get();
            if (theView == null || !theView.isAttachedToWindow()) {
                return;
            }

            GestureTouchUtils.simulateScroll(theView, 10000, 400, 0, 400, 100000, GestureTouchUtils.HIGH);
//            sendEmptyMessageDelayed(1, 11800);

        }
    }
}
