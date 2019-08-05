package com.wolfwang.demo_vr;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.wolfwang.demo_vr.Triangle.COORDS_PER_VERTEX;


/**
 * class description here
 * Created by qingchao.cui on 2019/5/10.
 */

public class MyGLRender1 implements GLSurfaceView.Renderer {
    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }


    private Triangle mTriangle;
//    private Square   mSquare;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        //设置背景的颜色
//        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        // initialize a triangle
        mTriangle = new Triangle();
        // initialize a square
//        mSquare = new Square();
    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float)width/height;
        // 这个投影矩阵被应用于对象坐标在onDrawFrame（）方法中
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }
    private float[] mRotationMatrix = new float[16];
    public volatile float mAngle;
    @Override
    public void onDrawFrame(GL10 gl) {
        // 重绘背景色
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // Set the camera position (View matrix)
//        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
//
//        // Calculate the projection and view transformation
//        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
//
//        mTriangle.draw(mMVPMatrix);

        // 设置相机位置（查看矩阵）
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // 计算投影和视图变换
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        float[] scratch = new float[16];
        // 为三角形创建一个旋转变换
//        long time = SystemClock.uptimeMillis() % 4000L;
//        float angle = 9f * ((int) time);
        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);
        // 绘制形状
//        mTriangle.draw(scratch);
        mTriangle.draw(scratch);
    }


}
