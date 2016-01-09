package org.opencv.javacv.facerecognition;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

    private String mPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPath=getFilesDir()+"/facerecogOCV/";


    }

    public void startCamera(View view) {
        Intent myIntent = new Intent(MainActivity.this, FdActivity.class);
        //myIntent.putExtra("key", value); //Optional parameters
        MainActivity.this.startActivity(myIntent);
    }

    public void exitApp(View view) {
        finish();
        System.exit(0);
    }

    public void imageMode(View view) {
       // Toast.makeText(getApplicationContext(),"Stub!", Toast.LENGTH_LONG).show();
        Intent i = new Intent(MainActivity.this,
                org.opencv.javacv.facerecognition.ImageActivity.class);
        //i.putExtra("path", mPath);
        startActivity(i);
    }

    public void openGallery(View view) {
        //Toast.makeText(getApplicationContext(),"Stub!", Toast.LENGTH_LONG).show();
        Intent i = new Intent(MainActivity.this,
                org.opencv.javacv.facerecognition.ImageGallery.class);
        i.putExtra("path", mPath);
        startActivity(i);
    }

}
