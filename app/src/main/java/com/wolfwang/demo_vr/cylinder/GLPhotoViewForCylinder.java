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
public class GLPhotoViewForCylinder extends GLSurfaceView {
    public static float mAngleX = 0;// 摄像机所在的x坐标
    public static float mAngleY = 0;// 摄像机所在的y坐标
    public static float mAngleZ = 1;// 摄像机所在的z坐标

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
    public GLPhotoViewForCylinder(Context context) {
        this(context, null);

    }

    /**
     * Constructor
     * @param context Context
     * @param attrs Argument for resource
     */
    public GLPhotoViewForCylinder(Context context, AttributeSet attrs) {
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
        FloatBuffer mNormalBuffer;

        int degreespan=12;  //圆截环每一份的度数大小
        int length = 2;//圆柱长度
        float[] verticals = new float[(360/degreespan) * 6 * 3];

        private final FloatBuffer mUvTexVertexBuffer;

        private final float[] UV_TEX_VERTEX = new float[(360/degreespan) * 6 * 2];

        private int mProgram;
        private int mPositionHandle;
        private int mTexCoordHandle;
        private int mMatrixHandle;
        private int mTexSamplerHandle;
        int[] mTexNames;

        private final float[] mProjectionMatrix = new float[16];
        private final float[] mCameraMatrix = new float[16];
        private final float[] mMVPMatrix = new float[16];

       
        public MyGLRender() {
            float r = 1f;//圆截环半径

            int index = 0;
            int index1 = 0;
                for (int j = 0; Math.ceil(j)<360; j += degreespan) {
                    //获得圆柱体上切分的超小片矩形的顶点坐标（两个三角形组成，所以有六点顶点）
                    double angrad=Math.toRadians(j);//当前弧度
                    double angradNext=Math.toRadians(j+degreespan);//下一弧度
                    //底圆当前点---0
                    verticals[index++]=(float) (-r*Math.sin(angrad));
                    verticals[index++]=-length;
                    verticals[index++]=(float) (-r*Math.cos(angrad));

                    UV_TEX_VERTEX[index1++]=(float) (angrad/(2*Math.PI));//st坐标
                    UV_TEX_VERTEX[index1++]=1;
                    //顶圆下一点---3
                    verticals[index++]=(float) (-r*Math.sin(angradNext));
                    verticals[index++]=length;
                    verticals[index++]=(float) (-r*Math.cos(angradNext));

                    UV_TEX_VERTEX[index1++]=(float) (angradNext/(2*Math.PI));//st坐标
                    UV_TEX_VERTEX[index1++]=0;
                    //顶圆当前点---2
                    verticals[index++]=(float) (-r*Math.sin(angrad));
                    verticals[index++]=length;
                    verticals[index++]=(float) (-r*Math.cos(angrad));

                    UV_TEX_VERTEX[index1++]=(float) (angrad/(2*Math.PI));//st坐标
                    UV_TEX_VERTEX[index1++]=0;

                    //底圆当前点---0
                    verticals[index++]=(float) (-r*Math.sin(angrad));
                    verticals[index++]=-length;
                    verticals[index++]=(float) (-r*Math.cos(angrad));

                    UV_TEX_VERTEX[index1++]=(float) (angrad/(2*Math.PI));//st坐标
                    UV_TEX_VERTEX[index1++]=1;

                    //底圆下一点---1
                    verticals[index++]=(float) (-r*Math.sin(angradNext));
                    verticals[index++] = -length;
                    verticals[index++]=(float) (-r*Math.cos(angradNext));

                    UV_TEX_VERTEX[index1++]=(float) (angradNext/(2*Math.PI));//st坐标
                    UV_TEX_VERTEX[index1++]=1;

                    //顶圆下一点---3
                    verticals[index++]=(float) (-r*Math.sin(angradNext));
                    verticals[index++]=length;
                    verticals[index++]=(float) (-r*Math.cos(angradNext));

                    UV_TEX_VERTEX[index1++]=(float) (angradNext/(2*Math.PI));//st坐标
                    UV_TEX_VERTEX[index1++]=0;
                }

            //法向量数据初始化
            float[] normals=new float[verticals.length];
            for(int i=0;i<verticals.length;i++){
                if(i%3==1){
                    normals[i]=0;
                }else{
                    normals[i]=verticals[i];
                }
            }

            verticalsBuffer = ByteBuffer.allocateDirect(verticals.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(verticals);
            verticalsBuffer.position(0);

            mNormalBuffer = ByteBuffer.allocateDirect(verticals.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(normals);
            mNormalBuffer.position(0);


            mUvTexVertexBuffer = ByteBuffer.allocateDirect(UV_TEX_VERTEX.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(UV_TEX_VERTEX);
            mUvTexVertexBuffer.position(0);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {

//            mWidth = width;
//            mHeight = height;
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
                bitmap = BitmapFactory.decodeStream(QfAdTvApp.getApp().getApplicationContext().getResources().getAssets().open("R0010152.jpg"));
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
            //调整摄像机焦点位置，使画面滚动
            Matrix.setLookAtM(mCameraMatrix, 0, mAngleX, mAngleY, mAngleZ, 0, 0, 0, 0, 1, 0);

            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mCameraMatrix, 0);

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            //制定使用某套shader程序
            GLES20.glUseProgram(mProgram);

            //启用顶点位置数据
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            //传送顶点位置数据
            GLES20.glVertexAttribPointer(mPositionHandle,
                    3,
                    GLES20.GL_FLOAT,
                    false,
                    3*4,
                    verticalsBuffer);
            //启用顶点纹理数据
            GLES20.glEnableVertexAttribArray(mTexCoordHandle);
            //传送顶点纹理坐标数据
            GLES20.glVertexAttribPointer(mTexCoordHandle,
                    2,
                    GLES20.GL_FLOAT,
                    false,
                    2*4,
                    mUvTexVertexBuffer);

            //传送顶点法向量数据
//            GLES20.glVertexAttribPointer
//                    (
//                            maNormalHandle,
//                            4,
//                            GLES20.GL_FLOAT,
//                            false,
//                            3*4,
//                            mNormalBuffer
//                    );
            //将最终变换矩阵传入shader程序
            GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES20.glUniform1i(mTexSamplerHandle, 0);

            //绘制纹理矩形
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, (180/degreespan) * (360/degreespan) * 6);

            //关闭顶点位置数据
            GLES20.glDisableVertexAttribArray(mPositionHandle);

//            xFlingAngle += xFlingAngleTemp;
//            yFlingAngle += yFlingAngleTemp;
        }
    }

}