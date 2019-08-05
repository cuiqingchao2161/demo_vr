package com.wolfwang.demo_vr.cylinder;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.wolfwang.demo_vr.QfAdTvApp;
import com.wolfwang.demo_vr.demoShape.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * View class for photo display
 */
public class GLPhotoViewForCylinder1 extends GLSurfaceView {
    public static float mAngleX = 0;// 摄像机所在的x坐标
    public static float mAngleY = 0;// 摄像机所在的y坐标
    public static float mAngleZ = 3;// 摄像机所在的z坐标

    float startRawX;
    float startRawY;

    double xFlingAngle;
    double xFlingAngleTemp;

    double yFlingAngle;
    double yFlingAngleTemp;

    public String VL = "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "attribute vec2 a_texCoord;" +
            "varying vec2 v_texCoord;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  v_texCoord = a_texCoord;" +
            "}";
    public String FL = "precision mediump float;" +
            "varying vec2 v_texCoord;" +
            "uniform sampler2D s_texture;" +
            "void main() {" +
            "  gl_FragColor = texture2D( s_texture, v_texCoord );" +
            "}";
    /**
     * Constructor
     * @param context Context
     */
    public GLPhotoViewForCylinder1(Context context) {
        this(context, null);

    }

    /**
     * Constructor
     * @param context Context
     * @param attrs Argument for resource
     */
    public GLPhotoViewForCylinder1(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    Activity activity;

    public void init(Activity activity){
        this.activity = activity;
    }

    public void play(){
        queueEvent(mRunnable);
    }

    private Runnable mRunnable   = new Runnable() {

        @Override
        public void run() {
//            if(!cancel){
                rotate(5f);
//            }
        }
    };

    public void setRenderer(){
        super.setRenderer(new MyGLRender());
    }

    public void rotate(float distance){
        float distanceX = distance;
        float distanceY = 0;
        yFlingAngleTemp = distanceY * 180 / (Math.PI * 3);
        if (yFlingAngleTemp + yFlingAngle > Math.PI / 2) {
            yFlingAngleTemp = Math.PI / 2 - yFlingAngle;
        }
        if (yFlingAngleTemp + yFlingAngle < -Math.PI / 2) {
            yFlingAngleTemp = -Math.PI / 2 - yFlingAngle;
        }
        //这里的0.1f是为了不上摄像机移动的过快
//            distanceX = 0.1f * (-distanceX) / activity.getWindowManager().getDefaultDisplay().getWidth();
        distanceX = 0.1f * (-distanceX) / 1920;
        xFlingAngleTemp = distanceX * 180 / (Math.PI * 3);

        mAngleX = (float) (3 * Math.cos(yFlingAngle + yFlingAngleTemp) * Math.sin(xFlingAngle + xFlingAngleTemp));
        mAngleY = (float) (3 * Math.sin(yFlingAngle + yFlingAngleTemp));
        mAngleZ = (float) (3 * Math.cos(yFlingAngle + yFlingAngleTemp) * Math.cos(xFlingAngle + xFlingAngleTemp));

//        requestRender();

//        xFlingAngle += xFlingAngleTemp;
//        yFlingAngle += yFlingAngleTemp;
    }
    @Override
    public boolean onTouchEvent(MotionEvent me) {
        //处理手指滑动事件，我这里的处理是判断手指在横向和竖向滑动的距离
        //这个距离隐射到球体上经度和纬度的距离，根据这个距离计算三维空间的两个
        //夹角，根据这个夹角调整摄像机所在位置
        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            startRawX = me.getRawX();
            startRawY = me.getRawY();
        } else if (me.getAction() == MotionEvent.ACTION_MOVE) {

            float distanceX = startRawX - me.getRawX();
            float distanceY = startRawY - me.getRawY();

            //这里的0.1f是为了不上摄像机移动的过快
//            distanceY = 0.1f * (distanceY) / activity.getWindowManager().getDefaultDisplay().getHeight();
            distanceY = 0.1f * (distanceY) / 1080;

            yFlingAngleTemp = distanceY * 180 / (Math.PI * 3);

            if (yFlingAngleTemp + yFlingAngle > Math.PI / 2) {
                yFlingAngleTemp = Math.PI / 2 - yFlingAngle;
            }
            if (yFlingAngleTemp + yFlingAngle < -Math.PI / 2) {
                yFlingAngleTemp = -Math.PI / 2 - yFlingAngle;
            }
            //这里的0.1f是为了不上摄像机移动的过快
//            distanceX = 0.1f * (-distanceX) / activity.getWindowManager().getDefaultDisplay().getWidth();
            distanceX = 0.1f * (-distanceX) / 1920;
            xFlingAngleTemp = distanceX * 180 / (Math.PI * 3);


            mAngleX = (float) (3 * Math.cos(yFlingAngle + yFlingAngleTemp) * Math.sin(xFlingAngle + xFlingAngleTemp));

            mAngleY = (float) (3 * Math.sin(yFlingAngle + yFlingAngleTemp));


            mAngleZ = (float) (3 * Math.cos(yFlingAngle + yFlingAngleTemp) * Math.cos(xFlingAngle + xFlingAngleTemp));

            requestRender();
        } else if (me.getAction() == MotionEvent.ACTION_UP) {
            xFlingAngle += xFlingAngleTemp;
            yFlingAngle += yFlingAngleTemp;
        }
        return true;
    }


    public class MyGLRender implements Renderer {
        FloatBuffer verticalsBuffer;

        int CAP = 9;//绘制球体时，每次增加的角度
        float[] verticals = new float[(180/CAP) * (360/CAP) * 6 * 3];


//        private final FloatBuffer mUvTexVertexBuffer;

        private final float[] UV_TEX_VERTEX = new float[(180/CAP) * (360/CAP) * 6 * 2];

        private int mProgram;
        private int mPositionHandle;
        private int mTexCoordHandle;
        private int mMatrixHandle;
        private int mTexSamplerHandle;
        int[] mTexNames;

        private final float[] mProjectionMatrix = new float[16];
        private final float[] mCameraMatrix = new float[16];
        private final float[] mMVPMatrix = new float[16];

        private int mWidth;
        private int mHeight;

        //ZhuCH

        public MyGLRender() {
            ArrayList<Float> pos=new ArrayList<>();
            float angDegSpan=360f/120;
            for(float i=0;i<360+angDegSpan;i+=angDegSpan){
                pos.add((float) (CAP*Math.sin(i*Math.PI/180f)));
                pos.add((float)(CAP*Math.cos(i*Math.PI/180f)));
//                pos.add(mHeight);
                pos.add((float) (CAP*Math.sin(i*Math.PI/180f)));
                pos.add((float)(CAP*Math.cos(i*Math.PI/180f)));
                pos.add(0.0f);
            }
            float[] d=new float[pos.size()];
            for (int i=0;i<d.length;i++){
                d[i]=pos.get(i);
            }
            verticalsBuffer = Utils.bufferUtil(d);
        }


        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {

            mWidth = width;
            mHeight = height;
            mProgram = GLES20.glCreateProgram();

            int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
            GLES20.glShaderSource(vertexShader, VL);
            GLES20.glCompileShader(vertexShader);

            int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
            GLES20.glShaderSource(fragmentShader, FL);
            GLES20.glCompileShader(fragmentShader);

            GLES20.glAttachShader(mProgram, vertexShader);
            GLES20.glAttachShader(mProgram, fragmentShader);

            GLES20.glLinkProgram(mProgram);

            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
            mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            mTexSamplerHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");

            mTexNames = new int[1];
            GLES20.glGenTextures(1, mTexNames, 0);
            //这里的全景图需要长宽的比例使2：1，不然上下顶点会出现形变
            Bitmap bitmap = null;


            try {
                bitmap = BitmapFactory.decodeStream(QfAdTvApp.getApp().getApplicationContext().getResources().getAssets().open("21.jpg"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexNames[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
            float ratio = (float) height / width;
            Matrix.frustumM(mProjectionMatrix, 0, -1, 1, -ratio, ratio, 3, 7);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            rotate(5f);
            //调整摄像机焦点位置，使画面滚动
            Matrix.setLookAtM(mCameraMatrix, 0, mAngleX, mAngleY, mAngleZ, 0, 0, 0, 0, 1, 0);

            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mCameraMatrix, 0);

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            GLES20.glUseProgram(mProgram);
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                    12, verticalsBuffer);
            GLES20.glEnableVertexAttribArray(mTexCoordHandle);
            GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0,
                    verticalsBuffer);
            GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES20.glUniform1i(mTexSamplerHandle, 0);


            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, (180/CAP) * (360/CAP) * 6);

            GLES20.glDisableVertexAttribArray(mPositionHandle);
            xFlingAngle += xFlingAngleTemp;
            yFlingAngle += yFlingAngleTemp;
//            play();
        }
    }

}