package com.haochen.telephonerecorder.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.haochen.telephonerecorder.R;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Haochen on 2016/7/13.
 */
public class FilePicker {
    private Context context;
    private ListView listView;
    private File path;
    private File file;
    private List<File> list;
    private boolean onlyDirectory;
    private boolean showHidden;

    private OnPathChangeListener onPathChangeListener;
    private OnFileChangeListener onFileChangeListener;

    public FilePicker(Context context, ListView listView, File defaultPath,
                      boolean onlyDirectory, boolean showHidden) {
        this.context = context;
        this.listView = listView;
        this.path = defaultPath;
        this.onlyDirectory = onlyDirectory;
        this.showHidden = showHidden;
    }

    public void setOnPathChangeListener(OnPathChangeListener onPathChangeListener) {
        this.onPathChangeListener = onPathChangeListener;
    }

    public void setOnFileChangeListener(OnFileChangeListener onFileChangeListener) {
        this.onFileChangeListener = onFileChangeListener;
    }

    public void launch() {
        enter(path);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = list.get(position);
                if (file.isDirectory()) {
                    enter(file);
                } else {
                    select(file);
                }
            }
        });
    }

    public void select(File file) {
        this.file = file;
        if (onFileChangeListener != null) {
            onFileChangeListener.onFileChange();
        }
    }

    public void enter(final File path) {
        file = null;
        this.path = path;
        File[] files = path.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return (!onlyDirectory || pathname.isDirectory())
                        && (showHidden || !pathname.isHidden());
            }
        });
        Arrays.sort(files);

        list = new ArrayList<>();
        for (File file : files) {
            list.add(file);
        }
        listView.setAdapter(new FilePickerAdapter(context, list));

        if (onPathChangeListener != null) {
            onPathChangeListener.onPathChange();
        }
    }

    public void back() {
        File parent = path.getParentFile();
        if (parent != null) {
            enter(parent);
        }
    }

    public File getPath() {
        return path;
    }

    public File getFile() {
        return file;
    }

    public boolean isOnlyDirectory() {
        return onlyDirectory;
    }

    public void setOnlyDirectory(boolean onlyDirectory) {
        this.onlyDirectory = onlyDirectory;
    }

    public boolean isShowHidden() {
        return showHidden;
    }

    public void setShowHidden(boolean showHidden) {
        this.showHidden = showHidden;
    }

    public class FilePickerAdapter extends BaseAdapter {

        private Context context;
        private List<File> list;

        public FilePickerAdapter(Context context, List<File> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.list_item_file_picker, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
                viewHolder.name = (TextView) convertView.findViewById(R.id.textView_name);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            File file = list.get(position);
            viewHolder.imageView.setVisibility(file.isDirectory() ? View.VISIBLE : View.INVISIBLE);
            viewHolder.name.setText(file.getName());
            return convertView;
        }

        private class ViewHolder {
            ImageView imageView;
            TextView name;
        }
    }

    public interface OnPathChangeListener {
        void onPathChange();
    }

    public interface OnFileChangeListener {
        void onFileChange();
    }
}
