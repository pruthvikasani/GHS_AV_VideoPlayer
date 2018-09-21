package com.example.titan.videoplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.crashlytics.android.Crashlytics;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static android.provider.MediaStore.MediaColumns.TITLE;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    public static String videoName= "English";
    String path = "";
    String outPath = "";
    String language_selected = "English";
    Context context = this;

    String filepath ="";
    ImageView thumbnail,playButton;

    ProgressBar progressBar;

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 200;

    ArrayList<Long> list = new ArrayList<>();
    FrameLayout thumbnailLayout;

    LinearLayout downloadLayout;


    SharedPreferences prefs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Crashlytics.getInstance().crash();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(0).setChecked(true);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        language_selected = preferences.getString("Language", "");

//        if(!name.equalsIgnoreCase(""))
//        {
//            name = name + "  Sethi";  /* Edit the value here*/
//        }
        //Toast.makeText(this,language_selected,Toast.LENGTH_LONG).show();


        Log.d("Path ", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
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
                        new NotificationCompat.Builder(MainActivity.this)
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

        NavigationView navigationView = findViewById(R.id.nav_view);

        Spinner spinner = (Spinner) navigationView.getMenu().findItem(R.id.navigation_drawer_item3).getActionView();
        ArrayAdapter<String> adapter;
        List<String> list;

        list = new ArrayList<String>();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        language_selected = preferences.getString("Language", "");
        int pos = preferences.getInt("Position",0);

        Log.d("Positon",String.valueOf(pos));
        Log.d("Spinner",language_selected);

        File EnglishFile = new File(context.getFilesDir(), videoName+".mp4");
        if (EnglishFile.exists()) {
            navigationView.getMenu().getItem(0).setActionView(R.layout.item_status);
            list.add("English");
        }
        File BengaliFile = new File(context.getFilesDir(), BengaliVideo.videoName + ".mp4");
        if (BengaliFile.exists()) {
            navigationView.getMenu().getItem(1).setActionView(R.layout.item_status);
            list.add("Bengali");

        }
        File GujarathiFile = new File(context.getFilesDir(), GujarathiVideo.videoName + ".mp4");
        if (GujarathiFile.exists())
        {
            navigationView.getMenu().getItem(2).setActionView(R.layout.item_status);
            list.add("Gujarathi");

        }

        File HindiFile = new File(context.getFilesDir(), HindiVideo.videoName + ".mp4");
        if (HindiFile.exists())
        {
            navigationView.getMenu().getItem(3).setActionView(R.layout.item_status);
            list.add("Hindi");

        }

        File KannadaFile = new File(context.getFilesDir(), KannadaVideo.videoName + ".mp4");
        if (KannadaFile.exists())
        {
            navigationView.getMenu().getItem(4).setActionView(R.layout.item_status);
            list.add("Kannada");

        }

        File MalayalamFile = new File(context.getFilesDir(), MalayalamVideo.videoName + ".mp4");
        if (MalayalamFile.exists())
        {
            navigationView.getMenu().getItem(5).setActionView(R.layout.item_status);
            list.add("Malayalam");

        }

        File MaratiFile = new File(context.getFilesDir(), MaratiVideo.videoName + ".mp4");
        if (MaratiFile.exists())
        {
            navigationView.getMenu().getItem(6).setActionView(R.layout.item_status);
            list.add("Marati");

        }

        File OdiaFile = new File(context.getFilesDir(), OdiaVideo.videoName + ".mp4");
        if (OdiaFile.exists())
        {
            navigationView.getMenu().getItem(7).setActionView(R.layout.item_status);
            list.add("Odia");

        }

        File TamilFile = new File(context.getFilesDir(), TamilVideo.videoName + ".mp4");
        if (TamilFile.exists())
        {
            navigationView.getMenu().getItem(8).setActionView(R.layout.item_status);
            list.add("Tamil");

        }

        File TeluguFile = new File(context.getFilesDir(), TeluguVideo.videoName + ".mp4");
        if (TeluguFile.exists())
        {
            navigationView.getMenu().getItem(9).setActionView(R.layout.item_status);
            list.add("Telugu");

        }

        adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(pos);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(MainActivity.this,language[position],Toast.LENGTH_SHORT).show();
                String value = parent.getItemAtPosition(position).toString();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("Language",value);
                editor.putInt("Position",position);
                editor.apply();

                Log.d("Spinner",value);
                Log.d("Position",String.valueOf(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        filepath = context.getFilesDir()+"/"+videoName+".mp4";
        thumbnail = findViewById(R.id.thumbnail);
        playButton = findViewById(R.id.play_button);

        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(filepath, MediaStore.Images.Thumbnails.MINI_KIND);

        if(language_selected.equals("English") || language_selected.equals("")) {
            if (EnglishFile.exists()) {
//            Uri uri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+"/"+videoName+".mp4");
                thumbnail.setImageBitmap(thumb);
                thumbnail.setVisibility(View.VISIBLE);
                playButton.setVisibility(View.VISIBLE);
                navigationView.getMenu().getItem(0).setActionView(R.layout.item_status);

                thumbnail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        Intent videoActivity = new Intent(MainActivity.this, VideoPlayerActivity.class).putExtra("from", "english");
                        startActivity(videoActivity);


                    }
                });


            } else {
                Toast.makeText(MainActivity.this, "No file exists", Toast.LENGTH_LONG).show();
                buildDialog2(this).show();

            }
        }

        else {
            Log.d("Language",language_selected);
            switch (language_selected){

                case "Bengali":
                    navigationView.getMenu().getItem(1).setChecked(true);
                    if (BengaliFile.exists())
                    {
                        thumbnail.setImageBitmap(thumb);
                        thumbnail.setVisibility(View.VISIBLE);
                        playButton.setVisibility(View.VISIBLE);

                        thumbnail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                Intent videoActivity = new Intent(MainActivity.this, VideoPlayerActivity.class).putExtra("from", "bengali");
                                startActivity(videoActivity);


                            }
                        });
                    }
                    else
                        downloadDialog(this,BengaliVideo.BengaliUri,BengaliVideo.download_title,BengaliVideo.videoName).show();
                    break;

                case "Gujarathi":
                    navigationView.getMenu().getItem(2).setChecked(true);
                    if (GujarathiFile.exists())
                    {
                        thumbnail.setImageBitmap(thumb);
                        thumbnail.setVisibility(View.VISIBLE);
                        playButton.setVisibility(View.VISIBLE);

                        thumbnail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                Intent videoActivity = new Intent(MainActivity.this, VideoPlayerActivity.class).putExtra("from", "gujarathi");
                                startActivity(videoActivity);


                            }
                        });
                    }
                    else
                        downloadDialog(this,GujarathiVideo.GujarathiUri,GujarathiVideo.download_title,GujarathiVideo.videoName).show();
                    break;

                case "Hindi":
                    navigationView.getMenu().getItem(3).setChecked(true);
                    if (HindiFile.exists())
                    {
                        thumbnail.setImageBitmap(thumb);
                        thumbnail.setVisibility(View.VISIBLE);
                        playButton.setVisibility(View.VISIBLE);

                        thumbnail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                Intent videoActivity = new Intent(MainActivity.this, VideoPlayerActivity.class).putExtra("from", "hindi");
                                startActivity(videoActivity);


                            }
                        });
                    }
                    else
                        downloadDialog(this,HindiVideo.HindiUri,HindiVideo.download_title,HindiVideo.videoName).show();
                    break;


                case "Kannada":
                    navigationView.getMenu().getItem(4).setChecked(true);
                    if (KannadaFile.exists())
                    {
                        thumbnail.setImageBitmap(thumb);
                        thumbnail.setVisibility(View.VISIBLE);
                        playButton.setVisibility(View.VISIBLE);


                        thumbnail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                Intent videoActivity = new Intent(MainActivity.this, VideoPlayerActivity.class).putExtra("from", "kannada");
                                startActivity(videoActivity);


                            }
                        });
                    }
                    else
                        downloadDialog(this,KannadaVideo.KannadaUri,KannadaVideo.download_title,KannadaVideo.videoName).show();
                    break;


                case "Malayalam":
                    navigationView.getMenu().getItem(5).setChecked(true);
                    if (MalayalamFile.exists())
                    {
                        thumbnail.setImageBitmap(thumb);
                        thumbnail.setVisibility(View.VISIBLE);
                        playButton.setVisibility(View.VISIBLE);

                        thumbnail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                Intent videoActivity = new Intent(MainActivity.this, VideoPlayerActivity.class).putExtra("from", "malayalam");
                                startActivity(videoActivity);


                            }
                        });
                    }
                    else
                        downloadDialog(this,MalayalamVideo.MalayalamUri,MalayalamVideo.download_title,MalayalamVideo.videoName).show();
                    break;


                case "Marati":
                    navigationView.getMenu().getItem(6).setChecked(true);
                    if (MaratiFile.exists())
                    {
                        thumbnail.setImageBitmap(thumb);
                        thumbnail.setVisibility(View.VISIBLE);
                        playButton.setVisibility(View.VISIBLE);

                        thumbnail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                Intent videoActivity = new Intent(MainActivity.this, VideoPlayerActivity.class).putExtra("from", "marati");
                                startActivity(videoActivity);


                            }
                        });
                    }
                    else
                        downloadDialog(this,MaratiVideo.MaratiUri,MaratiVideo.download_title,MaratiVideo.videoName).show();
                    break;


                case "Odia":
                    navigationView.getMenu().getItem(7).setChecked(true);
                    if (OdiaFile.exists())
                    {
                        thumbnail.setImageBitmap(thumb);
                        thumbnail.setVisibility(View.VISIBLE);
                        playButton.setVisibility(View.VISIBLE);

                        thumbnail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                Intent videoActivity = new Intent(MainActivity.this, VideoPlayerActivity.class).putExtra("from", "odia");
                                startActivity(videoActivity);


                            }
                        });
                    }
                    else
                        downloadDialog(this,OdiaVideo.OdiaUri,OdiaVideo.download_title,OdiaVideo.videoName).show();
                    break;


                case "Tamil":
                    navigationView.getMenu().getItem(8).setChecked(true);
                    if (TamilFile.exists())
                    {
                        thumbnail.setImageBitmap(thumb);
                        thumbnail.setVisibility(View.VISIBLE);
                        playButton.setVisibility(View.VISIBLE);

                        thumbnail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                Intent videoActivity = new Intent(MainActivity.this, VideoPlayerActivity.class).putExtra("from", "tamil");
                                startActivity(videoActivity);


                            }
                        });
                    }
                    else
                        downloadDialog(this,TamilVideo.TamilUri,TamilVideo.download_title,TamilVideo.videoName).show();
                    break;

                case "Telugu":
                    navigationView.getMenu().getItem(9).setChecked(true);
                    if (TeluguFile.exists())
                    {
                        thumbnail.setImageBitmap(thumb);
                        thumbnail.setVisibility(View.VISIBLE);
                        playButton.setVisibility(View.VISIBLE);

                        thumbnail.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                Intent videoActivity = new Intent(MainActivity.this, VideoPlayerActivity.class).putExtra("from", "telugu");
                                startActivity(videoActivity);


                            }
                        });
                    }
                    else
                        downloadDialog(this,TeluguVideo.TeluguUri,TeluguVideo.download_title,TeluguVideo.videoName).show();
                    break;
            }
        }
    }

    public AlertDialog.Builder downloadDialog(Context c,final Uri uri,final String download_title,final String VideoName) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setMessage("File doesn't exist. Do you want to download it?");
        builder.setCancelable(false);

        builder.setPositiveButton("DOWNLOAD", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (!isConnected(MainActivity.this)) buildDialog(MainActivity.this).show();
                else {
                    downloadLayout = findViewById(R.id.downloadLayout);
                    downloadLayout.setVisibility(View.VISIBLE);

                    DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//                Uri uri = Uri.parse("https://www.rmp-streaming.com/media/bbb-360p.mp4");
                    //Uri uri = Uri.parse("https://drive.google.com/uc?authuser=0&id=1wnTBm5_SbkKeNwaI4vXkttuTy7dHqWC5&export=download");
                    DownloadManager.Request request = new DownloadManager.Request(uri);

                    request.setTitle(download_title);
                    request.setDescription("Video is being downloaded");
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS.toString(), videoName + ".mp4");
                    long reference = downloadManager.enqueue(request);

                    list.add(reference);
                    registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                    path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + videoName + ".mp4";
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

    public AlertDialog.Builder buildDialog2(Context c) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setCancelable(false);
        builder.setMessage("File doesn't exist. Do you want to download it?");

        builder.setPositiveButton("DOWNLOAD", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(!isConnected(MainActivity.this)) buildDialog(MainActivity.this).show();
                else {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Permission is not granted
                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            // Show an explanation to the user *asynchronously* -- don't block
                            // this thread waiting for the user's response! After the user
                            // sees the explanation, try again to request the permission.
                        } else {
                            // No explanation needed; request the permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                            // app-defined int constant. The callback method gets the
                            // result of the request.
                        }
                    } else {

                       downloadLayout = findViewById(R.id.downloadLayout);
                       downloadLayout.setVisibility(View.VISIBLE);

                        final DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//                Uri uri = Uri.parse("https://www.rmp-streaming.com/media/bbb-360p.mp4");
                        Uri uri = Uri.parse("https://drive.google.com/uc?authuser=0&id=1wnTBm5_SbkKeNwaI4vXkttuTy7dHqWC5&export=download");
                        DownloadManager.Request request = new DownloadManager.Request(uri);

                        request.setTitle("TGH_English Video");
                        request.setDescription("First Video is being downloaded");
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS.toString(), videoName + ".mp4");
                        final long reference = downloadManager.enqueue(request);

                        list.add(reference);
                        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + videoName + ".mp4";
                        outPath = context.getFilesDir() + "/" + videoName + ".mp4";
                        Log.d("Output path ", outPath);
                        Log.d("Input path", path);

                    }

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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    downloadLayout = findViewById(R.id.downloadLayout);
                    downloadLayout.setVisibility(View.VISIBLE);

                    final DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//                Uri uri = Uri.parse("https://www.rmp-streaming.com/media/bbb-360p.mp4");
                    Uri uri = Uri.parse("https://drive.google.com/uc?authuser=0&id=1wnTBm5_SbkKeNwaI4vXkttuTy7dHqWC5&export=download");
                    DownloadManager.Request request = new DownloadManager.Request(uri);

                    request.setTitle("TGH_English Video");
                    request.setDescription("Video is being downloaded...");
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS.toString(), videoName + ".mp4");
                    final long reference = downloadManager.enqueue(request);

                    list.add(reference);
                    registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                    path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + videoName + ".mp4";
                    outPath = context.getFilesDir() + "/" + videoName + ".mp4";
                    Log.d("Output path ", outPath);
                    Log.d("Input path", path);


                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this,"Access permission to the storage is required",Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    Boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(getApplicationContext(), "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }


    public boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting())
        {
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


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.english:
                Intent video1 = new Intent(MainActivity.this, MainActivity.class);
                startActivity(video1);
                finish();
                break;
            case R.id.bengali:
                Intent video2 = new Intent(MainActivity.this, BengaliVideo.class);
                startActivity(video2);
                finish();
                break;
            case R.id.gujarathi:
                Intent video3 = new Intent(MainActivity.this, GujarathiVideo.class);
                startActivity(video3);
                finish();
                break;
            case R.id.hindi:
                Intent video4 = new Intent(MainActivity.this, HindiVideo.class);
                startActivity(video4);
                finish();
                break;
            case R.id.kannada:
                Intent video5 = new Intent(MainActivity.this, KannadaVideo.class);
                startActivity(video5);
                finish();
                break;
            case R.id.malayalam:
                Intent video6 = new Intent(MainActivity.this, MalayalamVideo.class);
                startActivity(video6);
                finish();
                break;
            case R.id.marati:
                Intent video7 = new Intent(MainActivity.this, MaratiVideo.class);
                startActivity(video7);
                finish();
                break;
            case R.id.odia:
                Intent video8 = new Intent(MainActivity.this, OdiaVideo.class);
                startActivity(video8);
                finish();
                break;
            case R.id.tamil:
                Intent video9 = new Intent(MainActivity.this, TamilVideo.class);
                startActivity(video9);
                finish();
                break;
            case R.id.telugu:
                Intent video10 = new Intent(MainActivity.this, TeluguVideo.class);
                startActivity(video10);
                finish();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
