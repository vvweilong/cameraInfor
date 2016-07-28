package aprivate.oo.cameralession;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback, View.OnClickListener, Camera.PictureCallback {


    final String TAG = "Camera";
    Button takeBtn, displayBtn, turnCameraBtn,turnRotationBtn;
    TextView paramersTextview;

    SquareCameraPreview cameraPreview;

    Camera camera;

    SurfaceHolder surfaceHolder;

    ImageView resultImageView;

    Bitmap resultBitmap;

    CameraManager cameraManager;
    ArrayList<CameraInforHolder> cameraInfoList;

    int displayOriention = 0;
    int parametersRotation=0;

    CameraInforHolder curCameraInfor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraPreview = (SquareCameraPreview) findViewById(R.id.suqare_preview);
        resultImageView = (ImageView) findViewById(R.id.picture_result);
        surfaceHolder = cameraPreview.getHolder();
        surfaceHolder.addCallback(this);

        cameraInfoInit();


        paramersTextview = (TextView) findViewById(R.id.textview);

        takeBtn = (Button) findViewById(R.id.take_picture);
        displayBtn = (Button) findViewById(R.id.display_turn);
        turnCameraBtn = (Button) findViewById(R.id.turn_camera);
        turnRotationBtn= (Button) findViewById(R.id.turn_rotation);
        takeBtn.setOnClickListener(this);
        displayBtn.setOnClickListener(this);
        turnCameraBtn.setOnClickListener(this);
        turnRotationBtn.setOnClickListener(this);
        resultImageView.setOnClickListener(this);

    }

    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
        resultBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        resultImageView.setImageBitmap(resultBitmap);
        resultImageView.setVisibility(View.VISIBLE);
        camera.startPreview();
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.display_turn:
                displayTurn();
                break;
            case R.id.take_picture:
                camera.takePicture(null, null, this);
                break;
            case R.id.turn_camera:
                turnCamera();
                break;
            case R.id.picture_result:
                resultImageView.setVisibility(View.INVISIBLE);
                resultBitmap.recycle();
                break;
            case R.id.turn_rotation:
                turnRotation();
                break;

        }
    }





    private void turnRotation(){
        camera.stopPreview();


        Camera.Parameters parameters = camera.getParameters();
        parametersRotation=parametersRotation+90;
        if(parametersRotation>359){
            parametersRotation=parametersRotation-360;
        }
        parameters.setRotation(parametersRotation);
        curCameraInfor.setInforRotation(parametersRotation);

        camera.setParameters(parameters);
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }




        camera.startPreview();
        showData();
    }



    private void showData(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("displayOriention :" + displayOriention + "\n");
        stringBuilder.append("camera orientation :" + curCameraInfor.getInforOriention() + "\n");
        stringBuilder.append("best preview  size:" + curCameraInfor.getBestPreviewSize() + "\n");
        stringBuilder.append("parameters rotation:" + curCameraInfor.getInforRotation() + "\n");

        paramersTextview.setText(stringBuilder.toString());
    }


    private void turnCamera() {
        camera.stopPreview();
        camera.release();
        if (curCameraInfor.getFacing() == Camera.CameraInfo.CAMERA_FACING_BACK) {
            getCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } else {
            getCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        }

        cameraPreview.setCamera(camera);
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size previewSize = getDeterminSize(parameters.getSupportedPreviewSizes(), 9, 16);
        curCameraInfor.setBestPreviewSize(previewSize);
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        camera.setParameters(parameters);
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
        showData();
    }

    private void displayTurn() {
        camera.stopPreview();//先停止预览
        displayOriention = displayOriention + 90;
        if (displayOriention > 359) {
            displayOriention = displayOriention - 360;
        }
        camera.setDisplayOrientation(displayOriention);
        camera.startPreview();

     showData();
    }


    /*
    * 获取全部 摄像头信息   并获取 每个摄像头合适的 previewsize 和 picturesize
    *
    * */
    private void cameraInfoInit() {
        int numberOfCamera = Camera.getNumberOfCameras();
        cameraInfoList = new ArrayList<>(numberOfCamera);
        CameraInforHolder holder = null;//摄像头信息的容器
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int id = 0; id < numberOfCamera; id++) {
            Camera.getCameraInfo(id, info);
            holder = new CameraInforHolder();
            holder.setId(id);
            holder.setFacing(info.facing);
            holder.setInforOriention(info.orientation);
            cameraInfoList.add(holder);
        }


    }

    private int determin(int a, int b) {
        int c = (int) ((float) a / (float) b + 0.5f);
        return c;
    }


    private Camera.Size getDeterminSize(List<Camera.Size> sizeList, int ratioW, int ratioH) {
        Camera.Size bestSize = null;
        bestSize = sizeList.get(0);
        for (Camera.Size size : sizeList) {
            if (determin(size.width, ratioH) == determin(size.height, ratioW)) {//比率符合16：9
                bestSize = size;
            }
        }
        return bestSize;
    }


    private Camera getCamera(int face) {
        if (cameraInfoList != null) {
            int l = cameraInfoList.size();
            for (CameraInforHolder holder : cameraInfoList) {
                if (holder.facing == face) {//表明当前摄像头是后前置摄像头
                    camera = Camera.open(holder.getId());//获取后置摄像头实例
                    curCameraInfor = holder;

                }
            }
        }
        return camera;
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        getCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        cameraPreview.setCamera(camera);
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size previewSize = getDeterminSize(parameters.getSupportedPreviewSizes(), 9, 16);
        curCameraInfor.setBestPreviewSize(previewSize);
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        camera.setParameters(parameters);
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        cameraPreview.setCamera(null);
        camera.release();
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {

    }


    private class CameraInforHolder {
        private int id;
        private Camera.Size bestPreviewSize;
        private Camera.Size bestPictureSize;
        private int facing;
        private int inforOriention;
        private int inforRotation;


        public int getInforRotation() {
            return inforRotation;
        }

        public void setInforRotation(int inforRotation) {
            this.inforRotation = inforRotation;
        }

        public int getInforOriention() {
            return inforOriention;
        }

        public void setInforOriention(int inforOriention) {
            this.inforOriention = inforOriention;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public Camera.Size getBestPreviewSize() {
            return bestPreviewSize;
        }

        public void setBestPreviewSize(Camera.Size bestPreviewSize) {
            this.bestPreviewSize = bestPreviewSize;
        }

        public Camera.Size getBestPictureSize() {
            return bestPictureSize;
        }

        public void setBestPictureSize(Camera.Size bestPictureSize) {
            this.bestPictureSize = bestPictureSize;
        }

        public int getFacing() {
            return facing;
        }

        public void setFacing(int facing) {
            this.facing = facing;
        }
    }

}
