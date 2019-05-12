package com.convoenglishllc.expression.activity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;

import com.convoenglishllc.expression.R;
import com.convoenglishllc.expression.component.AspectRatioVideoView;
import com.convoenglishllc.expression.utils.GlobalConstants;
import com.convoenglishllc.expression.utils.L;

import java.io.File;

public class VideoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_video);

        String videoPath = GlobalConstants.getVideoDownloadDir(this) + "/" + GlobalConstants.INSTRUCTION_VIDEO_NAME;
        if(!new File(videoPath).exists()) {
            L.toast(this, "Instructional video file not found");
            finish();
        }
        final View fg = findViewById(R.id.video_fg);

        final AspectRatioVideoView videoView = (AspectRatioVideoView)findViewById(R.id.myvideoview);
        videoView.setVideoURI(Uri.parse(videoPath));
        videoView.setMediaController(new MediaController(this));
        videoView.requestFocus();
        videoView.start();

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                fg.setVisibility(View.VISIBLE);
                findViewById(R.id.control_panel).setVisibility(View.VISIBLE);
                videoView.setMediaController(null);
            }
        });

        findViewById(R.id.skip_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); }
        });

        findViewById(R.id.watch_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fg.setVisibility(View.GONE);
                findViewById(R.id.control_panel).setVisibility(View.GONE);
                videoView.setMediaController(new MediaController(VideoActivity.this));
                videoView.seekTo(0);
                videoView.start();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
//        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

}
