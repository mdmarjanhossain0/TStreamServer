package com.torrenttube.tstreamserver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.ui.widget.VideoView;
/*import com.frostwire.jlibtorrent.FileStorage;
import com.github.se_bastiaan.torrentstream.StreamStatus;
import com.github.se_bastiaan.torrentstream.Torrent;
import com.github.se_bastiaan.torrentstream.TorrentOptions;*/
import com.frostwire.jlibtorrent.FileStorage;
import com.github.se_bastiaan.torrentstream.StreamStatus;
import com.github.se_bastiaan.torrentstream.Torrent;
import com.github.se_bastiaan.torrentstream.TorrentOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.torrenttube.tstreamserver.torrentstreamserver.TorrentServerListener;
import com.torrenttube.tstreamserver.torrentstreamserver.TorrentStreamNotInitializedException;
import com.torrenttube.tstreamserver.torrentstreamserver.TorrentStreamServer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.List;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity implements TorrentServerListener {

    private static final String TORRENT = "Torrent";
    private static final String TAG="TAG";
    private Button button;
    private TextView videoLocationText;
    private ProgressBar progressBar;
    private TorrentStreamServer torrentStreamServer;
    EditText editText;

    private String streamUrl = "";
    VideoView videoView;

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            streamUrl=editText.getText().toString().trim();
            progressBar.setProgress(0);
            if(torrentStreamServer.isStreaming()) {
                torrentStreamServer.stopStream();
                button.setText("Start stream");
                return;
            }
            try {
                torrentStreamServer.startStream(streamUrl);
            } catch (IOException | TorrentStreamNotInitializedException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_SHORT).show();
            }
            button.setText("Stop stream");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoLocationText = (TextView) findViewById(R.id.video_location_text);
        button = (Button) findViewById(R.id.button);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        editText=findViewById(R.id.edit_text);
        videoView=findViewById(R.id.video_view);

        String action = getIntent().getAction();
        Uri data = getIntent().getData();
        if (action != null && action.equals(Intent.ACTION_VIEW) && data != null) {
            try {
                /*File file = new File(data.getPath());
                Uri scheme=Uri.fromFile(file);
                this.streamUrl=getRealPathFromURI(data);
                Log.d(TAG, "scheme   "+String.valueOf(scheme));*/
                streamUrl=URLDecoder.decode(data.toString(), "utf-8");
                Log.d(TAG,"streamUrl "+streamUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        TorrentOptions torrentOptions = new TorrentOptions.Builder()
                .saveLocation(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
                .removeFilesAfterStop(false)
                .prepareSize((long) (5*1024L*1024L))
                .build();

        String ipAddress = "127.0.0.1";
        try {
            InetAddress inetAddress = getIpAddress(this);
            if (inetAddress != null) {
                ipAddress = inetAddress.getHostAddress();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        torrentStreamServer = TorrentStreamServer.getInstance();
        torrentStreamServer.setTorrentOptions(torrentOptions);
        torrentStreamServer.setServerHost(ipAddress);
        torrentStreamServer.setServerPort(8080);
        torrentStreamServer.startTorrentStream();
        torrentStreamServer.addListener(this);

        button.setOnClickListener(onClickListener);

        progressBar.setMax(100);
    }

    @Override
    protected void onStart() {
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE
                        ,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list,
                                                                   PermissionToken permissionToken) {

                    }
                }).check();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        torrentStreamServer.stopTorrentStream();
    }

    /*@Override
    public void onStreamPrepared(Torrent torrent) {
        for (int i=0;i<torrent.getTorrentHandle().torrentFile().merkleTree().size();i++){
            Log.d(TAG, String.valueOf(torrent.getTorrentHandle().torrentFile().merkleTree().get(i)));
        }
        FileStorage files = torrent.getTorrentHandle().torrentFile().files();
        for(int i=0;i<files.numFiles();i++){
            Log.d(TAG,files.fileName(i));
        }
        Log.d(TORRENT, "OnStreamPrepared");
    }

    @Override
    public void onStreamStarted(Torrent torrent) {
        Log.d(TORRENT, "onStreamStarted");
    }

    @Override
    public void onStreamError(Torrent torrent, Exception e) {
        Log.e(TORRENT, "onStreamError", e);
        button.setText("Start stream");
    }

    @Override
    public void onStreamReady(Torrent torrent) {
        progressBar.setProgress(100);
    }

    @Override
    public void onStreamProgress(Torrent torrent, StreamStatus status) {
        if(status.bufferProgress <= 100 && progressBar.getProgress() < 100 && progressBar.getProgress() != status.bufferProgress) {
            Log.d(TORRENT, "Progress: " + status.bufferProgress);
            progressBar.setProgress(status.bufferProgress);
        }
    }*/

    public void onStreamPrepared(Torrent torrent) {
        for (int i=0;i<torrent.getTorrentHandle().torrentFile().merkleTree().size();i++){
            Log.d(TAG, String.valueOf(torrent.getTorrentHandle().torrentFile().merkleTree().get(i)));
        }
        FileStorage files = torrent.getTorrentHandle().torrentFile().files();
        for(int i=0;i<files.numFiles();i++){
            Log.d(TAG,files.fileName(i));
        }
        Log.d(TORRENT, "OnStreamPrepared");
    }

    @Override
    public void onStreamStarted(Torrent torrent) {
        Log.d(TORRENT, "onStreamStarted");
    }

    @Override
    public void onStreamError(Torrent torrent, Exception e) {
        Log.e(TORRENT, "onStreamError", e);
        button.setText("Start stream");
    }


    @Override
    public void onStreamReady(Torrent torrent) {
        progressBar.setProgress(100);
    }


    @Override
    public void onStreamProgress(Torrent torrent, StreamStatus status) {
        if(status.bufferProgress <= 100 && progressBar.getProgress() < 100 && progressBar.getProgress() != status.bufferProgress) {
            Log.d(TORRENT, "Progress: " + status.bufferProgress);
            progressBar.setProgress(status.bufferProgress);
        }
    }

    @Override
    public void onStreamStopped() {
        Log.d(TORRENT, "onStreamStopped");
        videoLocationText.setText(null);
    }

    @Override
    public void onServerReady(String url) {
        Log.d(TORRENT, "onServerReady: " + url);

        videoLocationText.setText(url);

        videoView.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared() {
                videoView.start();
            }
        });
        videoView.setVideoURI(Uri.parse("https://www.youtube.com/watch?v=YCcD8Rt1Rng"));
        /*Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setDataAndType(Uri.parse(url), "video/*");
        startActivity(intent);*/
    }

    public static InetAddress getIpAddress(Context context) throws UnknownHostException {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();

        if (ip == 0) {
            return null;
        } else {
            byte[] ipAddress = convertIpAddress(ip);
            return InetAddress.getByAddress(ipAddress);
        }
    }

    private static byte[] convertIpAddress(int ip) {
        return new byte[]{
                (byte) (ip & 0xFF),
                (byte) ((ip >> 8) & 0xFF),
                (byte) ((ip >> 16) & 0xFF),
                (byte) ((ip >> 24) & 0xFF)};
    }

    public String getRealPathFromURI(Uri contentUri)
    {
        String[] proj = { MediaStore.Audio.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return "file://"+cursor.getString(column_index);
    }

}