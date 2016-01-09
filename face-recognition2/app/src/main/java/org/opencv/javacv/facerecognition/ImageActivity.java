package org.opencv.javacv.facerecognition;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageActivity extends Activity {

    static final int IMAGE_REQ_CODE = 1;  // The request code
    public static final String IMAGE_PATH = "imgPath";
    final Context context = this;
    private File                   mCascadeFile;
    private CascadeClassifier      mJavaDetector;
    PersonRecognizer fr;
    String mPath="";
    Labels labelsFile;
    private String imgPath;
    private Bitmap loadedImage;
    private MatOfRect loadedMat;
    ImageView imageView;
    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;
    private MatOfRect faceDetections;
    private Mat wholeMat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
        setContentView(R.layout.activity_image);
        imageView = (ImageView)findViewById(R.id.opened_image);

        Button openBtn = (Button)findViewById(R.id.open_btn);
        Button saveBtn = (Button) findViewById(R.id.save_btn);

        mPath=getFilesDir()+"/facerecogOCV/";

        labelsFile= new Labels(mPath);


        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ImageActivity.this,
                        org.opencv.javacv.facerecognition.FileChooser.class);
                //i.putExtra("path", mPath);
                startActivityForResult(i, IMAGE_REQ_CODE);
            }
        });


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((faceDetections == null) || !((faceDetections.toArray()).length == 1)) {
                    Toast.makeText(getApplicationContext(),"Exactly 1 face must be detected in order to save it.", Toast.LENGTH_SHORT).show();
                    return;
                }

                final Dialog dialog = new Dialog(context);
                LayoutInflater inflater = ImageActivity.this.getLayoutInflater();
                dialog.setContentView(inflater.inflate(R.layout.name_dialog, null));
                dialog.setTitle("Enter person name");
                // set the custom dialog components - text, image and button
                final TextView text = (TextView) dialog.findViewById(R.id.personName);
                //text.setText("");
                final ImageView image = (ImageView) dialog.findViewById(R.id.dialogHeader);

              // image.setImageBitmap(bitmapToTrain);
                // image.invalidate();
                Button dialogButtonOK = (Button) dialog.findViewById(R.id.dialogButtonOK);
                Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialogButtonCancel);

                final Mat m = wholeMat.submat(faceDetections.toArray()[0]).clone();
                Bitmap bitmapToTrain = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(m,bitmapToTrain);
                image.setImageBitmap(bitmapToTrain);
                dialogButtonOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        fr.add(m, text.getText().toString());
                        fr.train();
                        dialog.dismiss();

                    }
                });

                dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });






       Button recognizeBtn = (Button) findViewById(R.id.recognize_button);

        recognizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    wholeMat = Highgui.imread(imgPath);

                    Size relSize = calculateMinSize(wholeMat);


                    Imgproc.cvtColor(wholeMat, wholeMat, Imgproc.COLOR_BGR2RGB);

                    faceDetections = new MatOfRect();
                    if (mJavaDetector != null) {
                        mJavaDetector.detectMultiScale(wholeMat, faceDetections, 1.1, 5, 2, relSize, new Size(1000, 1000));
                        // mJavaDetector.detectMultiScale(a, faceDetections, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        //   new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
                    }

                    if (!faceDetections.empty()) {
                        //Toast.makeText(getApplicationContext(),"prazno", Toast.LENGTH_LONG).show();
                        //return;
                        Mat m = new Mat();

                        m = wholeMat.submat((faceDetections.toArray())[0]);
                        String name = fr.predict(m);

                        double conf = fr.getProb();
                        TextView tv = (TextView) findViewById(R.id.text);
                        tv.setText("");
                        tv.setText(name);
                        /*
                        if (conf < 0);
                        else if (conf < 50) {
                            tv.setBackgroundColor(Color.rgb(176, 250, 120));

                        }
                        else if (conf < 80) {
                            tv.setBackgroundColor(Color.rgb(242,136,82));

                        }
                        else {
                            tv.setBackgroundColor(Color.rgb(255,0,0));

                        }*/

                    }
                    for (Rect rect : faceDetections.toArray()) {
                        //Core.rectangle(a, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
                        Core.rectangle(wholeMat, rect.tl(), rect.br(), new Scalar(0, 255, 0), 3);
                    }

                    Bitmap bmp = Bitmap.createBitmap(loadedImage.getWidth(), loadedImage.getHeight(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(wholeMat, bmp);

                    imageView.setImageBitmap(bmp);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),"Error", Toast.LENGTH_LONG).show();
                }
            }
        });



    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (IMAGE_REQ_CODE) : {
                if (resultCode == Activity.RESULT_OK) {


                    try {
                        String imgPath = data.getStringExtra(IMAGE_PATH);
                        // TODO Update your TextView.
                        //Toast.makeText(getApplicationContext(),imgPath, Toast.LENGTH_LONG).show();
                        ImageView openedImg = imageView; //(ImageView) findViewById(R.id.opened_image);

                        this.imgPath = imgPath;

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap origBitmap = BitmapFactory.decodeFile(imgPath, options);
                        loadedImage = origBitmap;

                        MatOfRect imgMat = new MatOfRect();
                        Utils.bitmapToMat(loadedImage, imgMat);

                        if (mAbsoluteFaceSize == 0) {
                            int height = imgMat.rows();
                            if (Math.round(height * mRelativeFaceSize) > 0) {
                                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
                            }
                            //  mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
                        }


                        openedImg.setImageBitmap(loadedImage);

                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),"Select image please", Toast.LENGTH_LONG).show();
                    }


                }
                break;
            }
        }
    }

    private Size calculateMinSize(Mat m) {
        int height = m.rows();
        int width = m.cols();

        int lesserDim = height;
        if (width < height) {
            lesserDim = width;
        }

        int dim = lesserDim/10;
        return new Size(dim,dim);
    }

    private Bitmap solveOrientation(String imgPath) throws IOException {
        int ori = 0;
        ExifInterface  exif = new ExifInterface(imgPath);
        ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,1);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap origBitmap = BitmapFactory.decodeFile(imgPath, options);

        Matrix matrix = new Matrix();
        if (ori == ExifInterface.ORIENTATION_ROTATE_90) {
            matrix.preRotate(90);
        }
        Bitmap rotatedBitmap = null;
        rotatedBitmap = Bitmap.createBitmap(origBitmap, 0, 0, origBitmap.getWidth(), origBitmap.getHeight(), matrix, true);


        return rotatedBitmap;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);


    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                   // Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    //   System.loadLibrary("detection_based_tracker");



                    fr=new PersonRecognizer(mPath);
                    String s = getResources().getString(R.string.loading);
                    Toast.makeText(getApplicationContext(),s, Toast.LENGTH_LONG).show();
                    fr.load();

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            //Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            //Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        //                 mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                     //   Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                   // cameraPreview.enableView();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;


            }
        }
    };




}
