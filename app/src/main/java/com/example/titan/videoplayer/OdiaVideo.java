package com.example.titan.videoplayer;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

//https://drive.google.com/uc?authuser=0&id=1Pc2noM_wJvjQtGJNlfrWiHbvMP3VJ6q5&export=download
public class OdiaVideo extends AppCompatActivity {

    private DownloadManager downloadManager;
    public static String videoName= "Odia";

    public static Uri OdiaUri = Uri.parse("https://drive.google.com/uc?authuser=0&id=1jwhMTkD1W3rp6czXaOpFyoDb2lMuIsj4&export=download");
    public static String download_title = "TGH_Odia Video";

    String path = "";
    String outPath = "";
    Context context = this;

    String filepath ="";
    Long reference;
    ImageView thumbnail,playButton;

    ProgressBar progressBar;

    LinearLayout downloadLayout;

    ArrayList<Long> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_odia_video);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        OdiaVideo.this.getSupportActionBar().setTitle("Odia Video");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OdiaVideo.this,MainActivity.class));
                finish();
            }
        });

        file_Check();
    }
    BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {

            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            list.remove(referenceId);

            if(list.isEmpty())
            {
                Log.e("INSIDE", "" + referenceId);
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(OdiaVideo.this)
                                .setSmallIcon(R.drawable.ic_done_white)
                                .setContentText("Download complete");


                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(455, mBuilder.build());

            }

            String sourcePath = path;
            File source = new File(sourcePath);

            String destinationPath = outPath;
            File destination = new File(destinationPath);
            try
            {
                FileUtils.copyFile(source, destination);
                FileUtils.forceDelete(source);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            downloadLayout = findViewById(R.id.downloadLayout);
            downloadLayout.setVisibility(View.INVISIBLE);

            file_Check();

        }
    };


    public void file_Check(){


        File file = new File(context.getFilesDir(), videoName+".mp4");
        filepath = context.getFilesDir()+"/"+videoName+".mp4";
        thumbnail = findViewById(R.id.thumbnail);
        playButton = findViewById(R.id.play_button);

        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(filepath, MediaStore.Images.Thumbnails.MINI_KIND);


        if (file.exists()) {
            thumbnail.setImageBitmap(thumb);
            thumbnail.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.VISIBLE);
//            Uri uri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+"/"+videoName+".mp4");
            thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    Intent videoActivity = new Intent(OdiaVideo.this,VideoPlayerActivity.class).putExtra("from","odia");
                    startActivity(videoActivity);

                }
            });
        }
        else {
            Toast.makeText(OdiaVideo.this,"No file exists",Toast.LENGTH_LONG).show();
            buildDialog2(this).show();

        }
    }


    public AlertDialog.Builder buildDialog2(Context c) {


        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setCancelable(false);
        builder.setMessage("File doesn't exist. Do you want to download it?");

        builder.setPositiveButton("DOWNLOAD", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if( !isConnected(OdiaVideo.this)) buildDialog(OdiaVideo.this).show();
                else {
                    downloadLayout = findViewById(R.id.downloadLayout);
                    downloadLayout.setVisibility(View.VISIBLE);
                    downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//                Uri uri = Uri.parse("https://www.rmp-streaming.com/media/bbb-360p.mp4");
                    Uri uri = Uri.parse("https://drive.google.com/uc?authuser=0&id=1jwhMTkD1W3rp6czXaOpFyoDb2lMuIsj4&export=download");
                    DownloadManager.Request request = new DownloadManager.Request(uri);

                    request.setTitle("TGH_Odia Video");
                    request.setDescription("Video is being downloaded...");
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS.toString(), videoName + ".mp4");
                    reference = downloadManager.enqueue(request);
                    list.add(reference);
                    registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                    path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + videoName + ".mp4";
                    outPath = context.getFilesDir() + "/" + videoName + ".mp4";

                    Log.d("Output path ", outPath);
                    Log.d("Input path", path);
                }
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        return builder;
    }



    @Override
    public void onBackPressed() {

        Intent intent = new Intent(OdiaVideo.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    public boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if ((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting()))
                return true;
            else return false;
        } else
            return false;
    }

    public AlertDialog.Builder buildDialog(Context c) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("No Internet Connection");
        builder.setMessage("You need to have Mobile Data or Wi-fi to access this. Press ok to Exit");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        return builder;
    }
}
