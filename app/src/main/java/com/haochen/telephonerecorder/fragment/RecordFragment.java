package com.haochen.telephonerecorder.fragment;

import android.app.Fragment;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.haochen.telephonerecorder.R;
import com.haochen.telephonerecorder.adapter.MyAdapter;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;

/**
 * Created by Haochen on 2016/6/30.
 */
public class RecordFragment extends MyFragment {

    private MediaPlayer player;
    private File file;

    private SeekBar seekBar;
    private TextView currentTime;
    private TextView maxTime;
    private TextView title;

    public static final int UPDATE_PROGRESS = 1;
    private UpdateProgressHandler handler;

    private boolean isDragging = false;

    public RecordFragment(MyAdapter adapter) {
        super(adapter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        player = new MediaPlayer();
        handler = new UpdateProgressHandler(this);
        View view = inflater.inflate(R.layout.layout_record, container, false);

        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        currentTime = (TextView) view.findViewById(R.id.textView_current_time);
        maxTime = (TextView) view.findViewById(R.id.textView_max_time);
        title = (TextView) view.findViewById(R.id.textView_title);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isDragging) {
                    setTime(currentTime, progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isDragging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isDragging = false;
                int time = seekBar.getProgress();
                player.seekTo(time);
            }
        });

        view.findViewById(R.id.fab_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (file != null && player != null && !player.isPlaying()) {
                    start();
                }
            }
        });
        view.findViewById(R.id.fab_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player != null && player.isPlaying()) {
                    pause();
                }
            }
        });
        handler.enabled = true;
        handler.sendEmptyMessage(UPDATE_PROGRESS);
        title.requestFocus();
        return view;
    }

    @Override
    public void onDestroy() {
        handler.enabled = false;
        handler = null;
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
            player = null;
        }
        super.onDestroy();
    }

    private void updateProgress() {
        int time = player.getCurrentPosition();
        setTime(currentTime, time);
        seekBar.setProgress(time);
    }

    private void setTime(TextView textView, int time) {
        int t = time / 1000;
        textView.setText(String.format("%02d:%02d", t / 60, t % 60));
    }

    public void setFile(File file) throws IOException {
        player.setLooping(false);
        this.file = file;
        player.setDataSource(file.getAbsolutePath());
    }

    public void prepare() throws IOException {
        player.prepare();
        title.setText(file.getName());
        seekBar.setMax(player.getDuration());
        setTime(maxTime, player.getDuration());
    }

    public void start() {
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                handler.enabled = false;
                player.seekTo(0);
                player.pause();
                seekBar.setProgress(0);
                setTime(currentTime, 0);
            }
        });
        player.start();
        handler.enabled = true;
    }

    public void pause() {
        handler.enabled = false;
        player.pause();
    }

    public void reset() {
        handler.enabled = false;
        file = null;
        isDragging = false;
        player.seekTo(0);
        seekBar.setProgress(0);
        setTime(currentTime, 0);
        setTime(maxTime, 0);
        player.release();
        player = new MediaPlayer();
    }

    private static class UpdateProgressHandler extends Handler {
        SoftReference<RecordFragment> fragment;
        boolean enabled = false;

        public UpdateProgressHandler(RecordFragment fragment) {
            this.fragment = new SoftReference<RecordFragment>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            if (enabled) {
                if (msg.what == UPDATE_PROGRESS) {
                    if (fragment.get().player.isPlaying() && !fragment.get().isDragging) {
                        fragment.get().updateProgress();
                    }
                }
            }
            sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
        }
    }
}
