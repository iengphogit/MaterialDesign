package opencv.avaboy.com;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FastFeatureDetector;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.ORB;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "mMainActivity";

    TextView tvName;
    Scalar RED = new Scalar(255,0,0);
    Scalar GREEN = new Scalar(0,255,0);
    FeatureDetector detector;
    DescriptorExtractor descriptorExtractor;
    DescriptorMatcher matcher;

    Mat descriptors2, descriptors1;
    Mat img1;
    MatOfKeyPoint keypoints1, keypoints2;

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    Mat mat1, mat2, mat3;

    //image holder
    Mat bwIMG, hsvIMG, lrrIMG, urrIMG, dsIMG, usIMG, cIMG, hovIMG;
    MatOfPoint2f approxCurve;

    int threshold;


    /* Draw rectangle around contours */
    ImageView imageOne;

    private void initializeOpenCVDependencies() throws IOException{

        bwIMG = new Mat();
        dsIMG = new Mat();
        hsvIMG = new Mat();
        lrrIMG = new Mat();
        urrIMG = new Mat();
        usIMG = new Mat();
        cIMG = new Mat();
        hovIMG = new Mat();
        approxCurve = new MatOfPoint2f();

        descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.myCameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);

        threshold = 100;

        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {

                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        try {
                            initializeOpenCVDependencies();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;

                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }

        };


            /* Draw rectangle around contours */
            imageOne = findViewById(R.id.imageOne);

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        /*
        mat1 = new Mat(width, height, CvType.CV_8UC4);
        mat2 = new Mat(width, height, CvType.CV_8UC4);
        mat3 = new Mat(width, height, CvType.CV_8UC4);

        */

        mat1 = new Mat(height, width, CvType.CV_8UC4);
        mat2 = new Mat(height, width, CvType.CV_8UC4);
        mat3 = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mat1.release();
//        mat2.release();
//        mat3.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat dst = inputFrame.rgba();
        Mat grey = inputFrame.gray();

        Imgproc.pyrDown(grey, dsIMG, new Size(grey.cols() /2, grey.rows()/2));
        Imgproc.pyrUp(dsIMG, usIMG, grey.size());
        Imgproc.Canny(usIMG, bwIMG, 0 , threshold);
        Imgproc.dilate(bwIMG, bwIMG, new Mat(), new Point(-1,1),1);

        List<MatOfPoint> contours = new ArrayList<>();

        cIMG = bwIMG.clone();

        Imgproc.findContours(cIMG, contours, hovIMG, Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);


        for ( MatOfPoint cnt: contours){
            MatOfPoint2f curve = new MatOfPoint2f(cnt.toArray());

            Imgproc.approxPolyDP(curve, approxCurve, 0.02 * Imgproc.arcLength(curve,true),true);

            int numberVertices = (int) approxCurve.total();

            double contourArea = Imgproc.contourArea(cnt);
            if(Math.abs(contourArea) < 100){
                continue;
            }

            //Rectangle Detected
            if( numberVertices >= 4 && numberVertices <= 6){

                List<Double> cos = new ArrayList<>();
                for(int j=0; j < numberVertices; j++){
                    try {
                        cos.add(angle(approxCurve.toArray()[j % numberVertices], approxCurve.toArray()[j-2],approxCurve.toArray()[j-1]));
                        Collections.sort(cos);

                        double minCos = cos.get(0);
                        double maxCos = cos.get(cos.size() -1);

                        if(numberVertices >= 4 && minCos >= -0.1 && maxCos <= 0.3){
                            setLabel(dst, "X", cnt);
                        }

                    }catch (ArrayIndexOutOfBoundsException e){
                        e.printStackTrace();
                    }

                }
            }
        }


        Bitmap src = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.one);

        Mat image = new Mat();
        Utils.bitmapToMat(src, image);

        Mat greyMat = new Mat();
        Imgproc.cvtColor(image, greyMat, Imgproc.COLOR_RGB2GRAY, CvType.CV_32S);

        final Bitmap bitmap = Bitmap.createBitmap(greyMat.cols(), greyMat.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(greyMat, bitmap);

        new Handler(Looper.getMainLooper()).post(new Runnable(){
                                                     @Override
                                                     public void run() {
                                                         imageOne.setImageBitmap(bitmap);
                                                     }
                                                 });

        return dst;
    }

    private static double angle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }

    private void setLabel(Mat im, String label, MatOfPoint contour) {
        int fontface = Core.FONT_HERSHEY_SIMPLEX;
        double scale = 3;//0.4;
        int thickness = 3;//1;
        int[] baseline = new int[1];
        Size text = Imgproc.getTextSize(label, fontface, scale, thickness, baseline);
        Rect r = Imgproc.boundingRect(contour);
        Point pt = new Point(r.x + ((r.width - text.width) / 2),r.y + ((r.height + text.height) / 2));
        Imgproc.putText(im, label, pt, fontface, scale, new Scalar(255, 0, 0), thickness);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(this, "There is a problem in openCV", Toast.LENGTH_SHORT).show();
        } else {
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
