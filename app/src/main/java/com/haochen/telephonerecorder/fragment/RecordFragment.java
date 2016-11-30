package com.haochen.telephonerecorder.fragment;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.haochen.telephonerecorder.R;
import com.haochen.telephonerecorder.adapter.RecordAdapter;
import com.haochen.telephonerecorder.common.Record;
import com.haochen.telephonerecorder.common.CheckableItem;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Haochen on 2016/6/30.
 */
public class RecordFragment extends MyFragment {

    private MediaPlayer player;
    private File file;

//    private SeekBar seekBar;
//    private TextView currentTime;
//    private TextView maxTime;
//    private TextView title;

    public static final int UPDATE_PROGRESS = 1;
    private UpdateProgressHandler handler;

    private boolean isDragging = false;

    private ButtonBarFragment fragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        player = new MediaPlayer();
        handler = new UpdateProgressHandler(this);

        View view = inflater.inflate(R.layout.fragment_list_view_button_bar, container, false);
        spinner = (Spinner) view.findViewById(R.id.spinner);


        String[] str = {"", "Name", "Time", "Length", "Size"};
        spinner.setAdapter(new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_dropdown_item, str));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Comparator<Record> comparator = null;
                switch (position) {
                    case 1:
                        comparator = new Comparator<Record>() {
                            @Override
                            public int compare(Record lhs, Record rhs) {
                                return lhs.getFile().getName().compareTo(rhs.getFile().getName());
                            }
                        };
                        break;
                    case 2:
                        comparator = new Comparator<Record>() {
                            @Override
                            public int compare(Record lhs, Record rhs) {
                                return rhs.getModified().compareTo(lhs.getModified());
                            }
                        };
                        break;
                    case 3:
                        comparator = new Comparator<Record>() {
                            @Override
                            public int compare(Record lhs, Record rhs) {
                                MediaPlayer player = new MediaPlayer();
                                try {
                                    player.setDataSource(lhs.getFile().getAbsolutePath());
                                    player.prepare();
                                    int left = player.getDuration();
                                    player.release();
                                    player = new MediaPlayer();
                                    player.setDataSource(rhs.getFile().getAbsolutePath());
                                    player.prepare();
                                    int right = player.getDuration();
                                    player.release();
                                    return right - left;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return 0;
                            }
                        };
                        break;
                    case 4:
                        comparator = new Comparator<Record>() {
                            @Override
                            public int compare(Record lhs, Record rhs) {
                                return rhs.getSize().compareTo(lhs.getSize());
                            }
                        };
                        break;
                }

                if (comparator == null) {
                    return;
                }
                List<CheckableItem> data = adapter.getData();
                List<Record> temp = new ArrayList<>();
                for (CheckableItem item : data) {
                    temp.add((Record) item.getValue());
                }
                Record[] array = temp.toArray(new Record[1]);
                Arrays.sort(array, comparator);

                data = new ArrayList<>();
                for (Record i : array) {
                    data.add(new CheckableItem<>(i, false));
                }
                adapter.setData(data);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });



        listView = (ListView) view.findViewById(R.id.listView);
        adapter = new RecordAdapter(getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (isBatchMode()) {
                    return false;
                }
                adapter.setChecked(position, true);
                enterBatchMode();
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!isBatchMode()) {
                    File file = ((CheckableItem<Record>) adapter.getItem(position))
                            .getValue().getFile();
                    try {
                        reset();
                        setFile(file);
                        prepare();
                        start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        fragment = new ButtonBarFragment() {
            @Override
            public void onDestroy() {
                handler.enabled = false;
                if (player != null) {
                    if (player.isPlaying()) {
                        player.stop();
                    }
                }
                super.onDestroy();
            }
        };

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.button_bar, getButtonBarFragment());
        ft.commit();


        return view;
    }

    private void updateProgress() {
        if (fragment != null) {
            fragment.updateProgress();
        }
    }

    public void setFile(File file) throws IOException {
        if (fragment != null) {
            fragment.setFile(file);
        }
    }

    public void prepare() throws IOException {
        if (fragment != null) {
            fragment.prepare();
        }
    }

    public void start() {
        if (fragment != null) {
            fragment.start();
        }
    }

    public void pause() {
        if (fragment != null) {
            fragment.pause();
        }
    }

    public void reset() {
        if (fragment != null) {
            fragment.reset();
        }
    }

    @Override
    protected Fragment getButtonBarFragment() {
        return this.fragment;
    }

    public abstract class ButtonBarFragment extends Fragment {

        private SeekBar seekBar;
        private TextView currentTime;
        private TextView maxTime;
        private TextView title;

        private FloatingActionButton play;
        private FloatingActionButton pause;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.layout_record, container, false);

            seekBar = (SeekBar) view.findViewById(R.id.seekBar);
            currentTime = (TextView) view.findViewById(R.id.textView_current_time);
            maxTime = (TextView) view.findViewById(R.id.textView_max_time);
            title = (TextView) view.findViewById(R.id.textView_title);

            play = (FloatingActionButton) view.findViewById(R.id.fab_play);
            pause = (FloatingActionButton) view.findViewById(R.id.fab_pause);

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

            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (file != null && player != null && !player.isPlaying()) {
                        start();
                    }
                }
            });
            pause.setOnClickListener(new View.OnClickListener() {
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
            RecordFragment.this.file = file;
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

    }

    private static class UpdateProgressHandler extends Handler {
        SoftReference<RecordFragment> fragment;
        boolean enabled = false;

        public UpdateProgressHandler(RecordFragment fragment) {
            this.fragment = new SoftReference<>(fragment);
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
