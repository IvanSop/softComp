package org.opencv.javacv.facerecognition;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;

import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;






public class FdActivity extends Activity implements CvCameraViewListener2 {

    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;
    
    public static final int TRAINING= 0;
    public static final int SEARCHING= 1;
    public static final int IDLE= 2;


    final Context context = this;

    private int faceState=IDLE;


    
    private MatOfRect faces;
    private Rect faceToTrain;
    private Bitmap bitmapToTrain;
    private Mat matToTrain;
    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    private CascadeClassifier mJavaDetector;


    private int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;
    private int confidence =999;

    String mPath="";

    private JavaCameraView cameraPreview;

    

    TextView textresult;
    private  ImageView Iv;
    Bitmap mBitmap;
    Handler mHandler;
  
    PersonRecognizer fr;
    ToggleButton toggleButtonTrain;
    ToggleButton buttonSearch;


    ImageButton saveFaceBtn;
    
    TextView textState;
    com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer faceRecognizer;
   
    
    static final long MAXIMG = 10;

    int countImages=0;
    
    Labels labelsFile;
    

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

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
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                    } else
                    Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

       //                 mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    cameraPreview.enableView();

            } break;
            default:
                {
                    super.onManagerConnected(status);
                } break;
                
                
            }
        }
    };

    public FdActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.face_detect_surface_view);

        cameraPreview = (JavaCameraView) findViewById(R.id.tutorial3_activity_java_surface_view);

        cameraPreview.setCvCameraViewListener(this);
       
        
        mPath=getFilesDir()+"/facerecogOCV/";
        		
        labelsFile= new Labels(mPath);
                 
        Iv=(ImageView)findViewById(R.id.imageView1);
        textresult = (TextView) findViewById(R.id.textView1);
        
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
            	if (msg.obj=="IMG")
            	{
            	 Canvas canvas = new Canvas();
                 canvas.setBitmap(mBitmap);
                 Iv.setImageBitmap(mBitmap);

            	}
            	else {
            		textresult.setText(msg.obj.toString());

            	    if (confidence < 0);
            	    else if (confidence < 50) {
                        textresult.setBackgroundColor(Color.rgb(176, 250, 120));

                    }
            		else if (confidence < 80) {
                        textresult.setBackgroundColor(Color.rgb(242,136,82));

                    }
            		else {
                        textresult.setBackgroundColor(Color.rgb(255,0,0));

                    }
            	}
            }
        };

        buttonSearch=(ToggleButton)findViewById(R.id.buttonSearch);
        toggleButtonTrain=(ToggleButton)findViewById(R.id.toggleButton1);
        textState= (TextView)findViewById(R.id.textViewState);



        saveFaceBtn =(ImageButton)findViewById(R.id.back_button);
        saveFaceBtn.setVisibility(View.INVISIBLE);


        textresult.setVisibility(View.INVISIBLE);
    




		toggleButtonTrain.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (toggleButtonTrain.isChecked()) {

					buttonSearch.setVisibility(View.INVISIBLE);
					textresult.setVisibility(View.VISIBLE);
					saveFaceBtn.setVisibility(View.VISIBLE);


                    faceState = TRAINING;

					

				} else {
					textState.setText(R.string.Straininig); 
					textresult.setText("");
					//text.setVisibility(View.INVISIBLE);
					
					buttonSearch.setVisibility(View.VISIBLE);

					textresult.setText("");


			        Toast.makeText(getApplicationContext(),getResources().getString(R.string.Straininig), Toast.LENGTH_LONG).show();
					fr.train();
					textState.setText(getResources().getString(R.string.SIdle));
                    saveFaceBtn.setVisibility(View.INVISIBLE);
                    faceState = IDLE;

				}
			}

		});
        


            saveFaceBtn.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {

                    faceToTrain = null;
                    bitmapToTrain = null;
                    matToTrain = null;
                    Rect[] facesArray = (faces).toArray().clone();
                    Mat m = new Mat();
                    //Toast.makeText(getApplicationContext(), "imCameraClick", Toast.LENGTH_LONG).show();

                    if (facesArray.length == 0) {
                        Toast.makeText(getApplicationContext(), "Failed to capture face.. Try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //Rect r=facesArray[0];
                    faceToTrain = facesArray[0];
                    matToTrain = mRgba.submat(faceToTrain).clone();
                    //faceState = IDLE;
                    if (matToTrain == null) {
                        return;
                    }
                    m = matToTrain;//.submat(faceToTrain);
                    bitmapToTrain = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(m, bitmapToTrain);


                    final Dialog dialog = new Dialog(context);
                    LayoutInflater inflater = FdActivity.this.getLayoutInflater();
                    dialog.setContentView(inflater.inflate(R.layout.name_dialog, null));
                    dialog.setTitle("Enter person name");
                    final TextView text = (TextView) dialog.findViewById(R.id.personName);
                    //text.setText("");
                    final ImageView image = (ImageView) dialog.findViewById(R.id.dialogHeader);

                    image.setImageBitmap(bitmapToTrain);
                    // image.invalidate();
                    Button dialogButtonOK = (Button) dialog.findViewById(R.id.dialogButtonOK);
                    Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialogButtonCancel);

                    dialogButtonOK.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Mat m = new Mat();
                            //Toast.makeText(getApplicationContext(), "facesLength: "+(faces.toArray()).length, Toast.LENGTH_LONG).show();
                            if (faceToTrain == null) {
                                Toast.makeText(getApplicationContext(), "faceToTrain null", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            //Rect r = faces.toArray()[0];
                            //Rect r = faceToTrain;
                            //m=mRgba.submat(faceToTrain).clone();
                            m = matToTrain;

                            if (countImages < MAXIMG) {
                                fr.add(m, text.getText().toString());
                                countImages++;
                            }


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

        buttonSearch.setOnClickListener(new View.OnClickListener() {

     			public void onClick(View v) {
     				if (buttonSearch.isChecked())
     				{
     					if (!fr.canPredict())
     						{
     						buttonSearch.setChecked(false);
     			            Toast.makeText(getApplicationContext(), getResources().getString(R.string.SCanntoPredic), Toast.LENGTH_LONG).show();
     			            return;
     						}
     					textState.setText(getResources().getString(R.string.SSearching));

     					toggleButtonTrain.setVisibility(View.INVISIBLE);

     					faceState=SEARCHING;
     					textresult.setVisibility(View.VISIBLE);
     				}
     				else
     				{
     					faceState=IDLE;
     					textState.setText(getResources().getString(R.string.SIdle));

     					toggleButtonTrain.setVisibility(View.VISIBLE);

     					textresult.setVisibility(View.INVISIBLE);
     					
     				}
     			}
     		});
        
        boolean success=(new File(mPath)).mkdirs();
        if (!success)
        {
        	Log.e("Error","Error creating directory");
        }
    }

    
    @Override
    public void onPause()
    {
        super.onPause();
        if (cameraPreview != null)
        cameraPreview.disableView();
        }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
       
      	
    }

    public void onDestroy() {
        super.onDestroy();
        cameraPreview.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
          //  mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        } else {
            Log.e(TAG, "No detection method");
        }

        Rect[] facesArray = faces.toArray();

        if ((facesArray.length==1)&&(faceState==TRAINING)&&(countImages<MAXIMG)) {


            Mat m=new Mat();
            Rect r=facesArray[0];


            m=mRgba.submat(r);
            //matToTrain = mRgba.submat(r);
            mBitmap = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);
            //bitmapToTrain = Bitmap.createBitmap(m.width(),m.height(), Bitmap.Config.ARGB_8888);
            //Utils.matToBitmap(matToTrain,bitmapToTrain);
            Utils.matToBitmap(m, mBitmap);
           // SaveBmp(mBitmap,"/sdcard/db/I("+countTrain+")"+countImages+".jpg");

            Message msg = new Message();
            String textTochange = "IMG";
            msg.obj = textTochange;
            mHandler.sendMessage(msg);


        } else {
            if ((facesArray.length > 0) && (faceState == SEARCHING)) {
                Mat m = new Mat();
                m = mGray.submat(facesArray[0]);
                mBitmap = Bitmap.createBitmap(m.width(), m.height(), Bitmap.Config.ARGB_8888);


                Utils.matToBitmap(m, mBitmap);
                Message msg = new Message();
                String textTochange = "IMG";
                msg.obj = textTochange;
                mHandler.sendMessage(msg);

                textTochange = fr.predict(m);
                confidence = fr.getProb();
                msg = new Message();
                msg.obj = textTochange;
                mHandler.sendMessage(msg);

            }
        }
        for (int i = 0; i < facesArray.length; i++)
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

        return mRgba;
    }


}
