package com.convoenglishllc.expression.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.convoenglishllc.expression.R;
import com.convoenglishllc.expression.data.manager.LessonManager;
import com.convoenglishllc.expression.data.model.LessonDataObject;
import com.convoenglishllc.expression.data.model.RecordDataObject;
import com.convoenglishllc.expression.utils.GlobalConstants;
import com.convoenglishllc.expression.utils.L;
import com.convoenglishllc.expression.utils.TextFormatter;
import com.convoenglishllc.expression.utils.ImageProcess;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import at.markushi.ui.CircleButton;

public class RecordListActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {
    private final String TAG = this.getClass().getSimpleName();

    private TextView uiCurrentDuration = null;
    private CircleButton uiPlay = null;
    private SeekBar uiSeekBar = null;
    private MediaPlayer mMediaPlayer = null;

    private LessonDataObject[] mLessonDataSet = null;

    private RecordAdapter mAdapter = null;
    public LayoutInflater inflater = null;
    private boolean hasPlayed = false;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("HAS_PLAYED", hasPlayed);
        super.onSaveInstanceState(savedInstanceState);
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

    @Override
    public void onResume() {
        super.onResume();

        updateProgressBarOnce();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if(savedInstanceState != null) {
            hasPlayed = savedInstanceState.getBoolean("HAS_PLAYED");
        }
        inflater = LayoutInflater.from(getApplicationContext());
        setContentView(R.layout.activity_record_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        uiCurrentDuration  = (TextView) findViewById(R.id.ui_current_duration);
        uiSeekBar = (SeekBar) findViewById(R.id.ui_seek_bar);
        uiPlay = (CircleButton) findViewById(R.id.ui_play);

        uiPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickPlay();
            }
        });
        if(mMediaPlayer != null) {
            mMediaPlayer.setOnCompletionListener(this);
            uiSeekBar.setMax(mMediaPlayer.getDuration());
        }
        uiSeekBar.setOnSeekBarChangeListener(this);

        boolean hasIntent = false;
        if(getIntent() != null) {
            String url = getIntent().getStringExtra(GlobalConstants.EXTRA_RECORD_URL);
            if(url != null && !hasPlayed) {
                hasPlayed = true;
                startPlay(url);
                hasIntent = true;
            }
        }

        mAdapter = new RecordAdapter(getRecordDataSet(), hasIntent);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).showLastDivider().build());
        mRecyclerView.setAdapter(mAdapter);

        setTitle(getString(R.string.title_records));
    }

    public RecordDataObject[] getRecordDataSet() {
        File f = new File(GlobalConstants.getRecordDir(getApplicationContext()));
        File file[] = f.listFiles();
        RecordDataObject[] dataSet = new RecordDataObject[file.length];
        Arrays.sort(file, new Comparator<File>() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            public int compare(File f1, File f2) {
                return Long.compare(f2.lastModified(), f1.lastModified());
            }
        });

        int detect = 0;
        for (int i=0; i < file.length; i++) {
            String s = file[i].getName();
            String sp[] = s.split("-");
            if(sp.length < 3) continue;
            String title = sp[1];
            int lessonNo = Integer.parseInt(sp[2].replace(".wav",""));
            String lessonImg = "images/" + LessonManager.getLessonImageByNo(getApplicationContext(), lessonNo);
            dataSet[detect++] = new RecordDataObject(lessonNo, lessonImg, title, file[i].getAbsolutePath(), file[i].lastModified());
        }

        return dataSet;
    }

    @Override
    public void setTitle(CharSequence title) {
        //noinspection ConstantConditions
        this.getSupportActionBar().setDisplayShowCustomEnabled(true);
        this.getSupportActionBar().setDisplayShowTitleEnabled(false);

        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.item_title, null);

        ((TextView)v.findViewById(R.id.main_title)).setText(title);

        this.getSupportActionBar().setCustomView(v);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mUpdateProgressHandler.removeCallbacks(mUpdateTimeTask);
            updateProgressBarOnce();
        }
    }
    @Override
    public void onBackPressed() {
        if(mAdapter.getViewType() == 1) {
            mAdapter.setViewType(0);
            //noinspection ConstantConditions
            getSupportActionBar().invalidateOptionsMenu();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_record_list, menu);
        if(mAdapter.getViewType() == 0) {
            menu.findItem(R.id.action_edit).setVisible(false);
            menu.findItem(R.id.action_delete).setVisible(false);
            menu.findItem(R.id.action_cancel).setVisible(false);
        } else {
            menu.findItem(R.id.action_edit).setVisible(false);
            menu.findItem(R.id.action_delete).setVisible(true);
            menu.findItem(R.id.action_cancel).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if ( id == android.R.id.home) {
            if(mAdapter.getViewType() == 1) {
                mAdapter.setViewType(0);
            } else {
                finish();
            }
        } else if (id == R.id.action_edit) {
            if(mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                updateProgressBarOnce();
            }
            mAdapter.setViewType(1);
        } else if (id == R.id.action_delete) {
            final ArrayList<Integer> deleteList = new ArrayList<>();
            for(int i=0; i<mAdapter.getSelected().size(); i++) {
                boolean delete = mAdapter.getSelected().get(i);
                if(delete) deleteList.add(i);
            }
            if(deleteList.size() > 0) {
                String text;
                final boolean deleteAll = deleteList.size() == mAdapter.getDataSet().length;
                if(deleteAll) text = getString(R.string.dialog_clear_all_records_confirm);
                else text = getString(R.string.dialog_delete_records_confirm);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                LayoutInflater inflater = this.getLayoutInflater();
                TextView dialogue_title;
                View titleView = inflater.inflate(R.layout.dialogue_title, null);
                dialogue_title = titleView.findViewById(R.id.dialogue_title);
                dialogue_title.setText(R.string.dialog_clear_records_title);

                alertDialogBuilder.setCustomTitle(titleView);
                alertDialogBuilder
                        .setMessage(text)
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                for(Integer i : deleteList) {
                                    File f = new File(mAdapter.getDataSet()[i].record_path);
                                    try {
                                        if(f.exists()) L.d(TAG, "Delete " + mAdapter.getDataSet()[i].record_path + " : " + f.delete());
                                    } catch (Exception ignored) {}
                                }
                                if(deleteAll) finish();

                                RecordDataObject[] dataSet = getRecordDataSet();
                                mAdapter.onDataSetChanged(dataSet);
                                //noinspection ConstantConditions
                                getSupportActionBar().invalidateOptionsMenu();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                alertDialogBuilder.create().show();
            }
        } else if (id == R.id.action_cancel) {
            mAdapter.setViewType(0);
        }

        //noinspection ConstantConditions
        getSupportActionBar().invalidateOptionsMenu();
        return super.onOptionsItemSelected(item);
    }

    /*********************************/
    private Handler mUpdateProgressHandler = new Handler();

    public void updateProgressBar() {
        mUpdateProgressHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private synchronized void updateProgressBarOnce() {
        if(mMediaPlayer == null) {
            uiPlay.setImageResource(R.drawable.ic_action_audio_play);
            uiCurrentDuration.setText(TextFormatter.getPlayTime(0));
            uiSeekBar.setProgress(0);
        } else {
            if (mMediaPlayer.isPlaying()) {
                uiPlay.setImageResource(R.drawable.ic_action_audio_pause);
            } else {
                uiPlay.setImageResource(R.drawable.ic_action_audio_play);
            }
            uiCurrentDuration.setText(TextFormatter.getPlayTime(mMediaPlayer.getCurrentPosition()));
            uiSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
        }
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            updateProgressBarOnce();
            mUpdateProgressHandler.postDelayed(this, 100);
        }
    };
    /*********************************/
    private void onClickPlayImpl() {
        if(mMediaPlayer == null) return;
        mMediaPlayer.setOnCompletionListener(this);
        uiSeekBar.setMax(mMediaPlayer.getDuration());

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mUpdateProgressHandler.removeCallbacks(mUpdateTimeTask);
            updateProgressBarOnce();
        }
        else {
            mMediaPlayer.start();
            updateProgressBar();
        }
    }

    private void onClickPlay() {
        L.d(TAG, "onClickPlay()");
        if(mMediaPlayer ==  null) {
            L.toast(this, getString(R.string.toast_select_record));
            return;
        }
        onClickPlayImpl();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {}

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if(mMediaPlayer == null) return;
        mUpdateProgressHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(mMediaPlayer == null) return;
        mUpdateProgressHandler.removeCallbacks(mUpdateTimeTask, 100);
        mMediaPlayer.seekTo(seekBar.getProgress());
        if(mMediaPlayer.isPlaying()) updateProgressBar();
        else updateProgressBarOnce();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mp != mMediaPlayer) return;
        mMediaPlayer.pause();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mMediaPlayer.seekTo(0);
                updateProgressBarOnce();
            }
        }, 100);
        mUpdateProgressHandler.removeCallbacks(mUpdateTimeTask);
    }
    /*********************************/

    public void startPlay(String audioPath) {
        if(mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
        mMediaPlayer = MediaPlayer.create(this, Uri.parse(audioPath));
        onClickPlayImpl();
    }

    class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {
        //private final String TAG = this.getClass().getSimpleName();
        private int focusedItem = -1;

        private int mViewType = -1;

        private RecordDataObject[] mRecordDataSet;

        private SparseBooleanArray mCheckedArray = null;


        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView uiRecordTitle;
            private final TextView uiLessonTitle;
            private final TextView uiRecordDate;
            private final ImageView uiShare;
            private final ImageView uiDelete;
            private final ImageView uiLessonImage;

            private final CheckBox uiCheckBox;

            public ViewHolder(View v) {
                super(v);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public synchronized void onClick(View v) {
                    /*Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(mRecordDataSet[getAdapterPosition()].record_path)), "audio/*");
                    RecordListActivity.gContext.startActivity(intent);*/
                        if(getAdapterPosition() == -1) return;
                        if (mViewType == 0) {
                            String url = mRecordDataSet[getAdapterPosition()].record_path;
                            startPlay(url);

                        } else if (mViewType == 1) {
                            boolean checked = getUiCheckBox().isChecked();
                            getUiCheckBox().setChecked(!checked);
                            mCheckedArray.put(getAdapterPosition(), !checked);
                        }

                        notifyItemChanged(focusedItem);
                        focusedItem = getAdapterPosition();
                        notifyItemChanged(focusedItem);
                    }
                });
                uiRecordTitle = (TextView) v.findViewById(R.id.ui_title);
                uiLessonTitle = (TextView) v.findViewById(R.id.ui_lesson_title);
                uiRecordDate = (TextView) v.findViewById(R.id.ui_date);

                uiCheckBox = (CheckBox) v.findViewById(R.id.ui_check);
                uiShare = (ImageView) v.findViewById(R.id.ui_share);
                uiDelete = (ImageView) v.findViewById(R.id.ui_delete);
                uiLessonImage = (ImageView) v.findViewById(R.id.ui_lesson_image);


            }
            public TextView getUiRecordTitle() { return uiRecordTitle; }
            public TextView getUiLessonTitle() { return uiLessonTitle; }
            public TextView getUiRecordDate() { return uiRecordDate; }
            public CheckBox getUiCheckBox() { return uiCheckBox; }
            public ImageView getUiShare() { return uiShare; }
            public ImageView getUiDelete() { return uiDelete; }
            public ImageView getUiLessonImage() { return uiLessonImage; }
        }

        public RecordAdapter(RecordDataObject[] dataSet, boolean preSelected) {
            mRecordDataSet = dataSet;
            if(mViewType == -1) mViewType = 0;
            mCheckedArray = new SparseBooleanArray(mRecordDataSet.length);
            clearSelected();
            if(preSelected) focusedItem = 0;
        }

        public void onDataSetChanged(RecordDataObject[] dataSet) {
            mRecordDataSet = dataSet;
            mViewType = 0;
            mCheckedArray = new SparseBooleanArray(mRecordDataSet.length);
            clearSelected();
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            View v = null;
            if(viewType == 0) {
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.row_record, viewGroup, false);

            } else if(viewType == 1) {
                v = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.row_record_with_checkbox, viewGroup, false);
            }
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {
            if((focusedItem == position && mViewType == 0) || (mViewType == 1 && mCheckedArray.get(position))) {
                viewHolder.itemView.setBackgroundColor(Color.rgb(192, 217, 239));
                //viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
            } else {
                viewHolder.itemView.setBackgroundColor(Color.rgb(255, 255, 255));
            }
            viewHolder.getUiRecordTitle().setText(mRecordDataSet[position].record_title);
            viewHolder.getUiLessonTitle().setText(LessonManager.getLessonByNo(getApplicationContext(), mRecordDataSet[position].lesson_no).getTitle());
            viewHolder.getUiRecordDate().setText(TextFormatter.getDisplayableTime(mRecordDataSet[position].record_date));
            ImageProcess.loadAssetImage(getApplicationContext(), mRecordDataSet[position].lesson_image, viewHolder.getUiLessonImage());
            if(mViewType == 1) {
                viewHolder.getUiCheckBox().setChecked(mCheckedArray.get(position));
            }

            //
            if (mViewType == 0) {
                viewHolder.getUiShare().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Uri uri;
                        File recordingFile = new File(mRecordDataSet[position].record_path);

                        Intent i=new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
                        i.setType("application/wav");
                        i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Conversation recording");
                        i.putExtra(android.content.Intent.EXTRA_TEXT, "The attached is a recording of my conversation practice.");

                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            uri = FileProvider.getUriForFile(RecordListActivity.this,
                                    "com.talkenglsh.conversation.fileprovider",
                                    recordingFile);
                        } else {
                            uri = Uri.fromFile(recordingFile);
                        }

                        // tricky to workaround use both file name and subject properly for respectively google drive and email
                        // https://stackoverflow.com/questions/35382474/google-drive-changes-file-name-to-intent-extra-subject-when-sharing-a-file-via-i
                        // i.putExtra(Intent.EXTRA_STREAM, uri);
                        i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, new ArrayList<>(Arrays.asList(uri)));
                        startActivity(Intent.createChooser(i, "Share via"));
                    }
                });
                viewHolder.getUiDelete().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(RecordListActivity.this);

                        TextView dialogue_title;
                        View titleView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialogue_title, null);
                        dialogue_title = titleView.findViewById(R.id.dialogue_title);
                        dialogue_title.setText(R.string.dialog_clear_records_title);

                        alertDialogBuilder.setCustomTitle(titleView);
                        alertDialogBuilder
                                .setMessage("Are you sure you want to delete this recording?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();

                                        File f = new File(mAdapter.getDataSet()[position].record_path);
                                        try {
                                            if(f.exists()) L.d(TAG, "Delete " + mAdapter.getDataSet()[position].record_path + " : " + f.delete());
                                        } catch (Exception ignored) {}


                                        RecordDataObject[] dataSet = getRecordDataSet();
                                        mAdapter.onDataSetChanged(dataSet);
                                        //noinspection ConstantConditions
                                        getSupportActionBar().invalidateOptionsMenu();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        alertDialogBuilder.create().show();
                    }
                });
            }
        }

        @Override
        public int getItemViewType(int position) {
            // Just as an example, return 0 or 2 depending on position
            // Note that unlike in ListView adapters, types don't have to be contiguous
            return mViewType;
        }
        @Override
        public int getItemCount() {
            return mRecordDataSet.length;
        }

        public void setViewType(int viewType) {
            if(mViewType == viewType) return;

            mViewType = viewType;
            if(mViewType == 0) clearSelected();
            notifyDataSetChanged();
        }

        public int getViewType() { return mViewType; }

        public void clearSelected() {
            for(int i=0; i<mRecordDataSet.length; i++) mCheckedArray.put(i, false);
            focusedItem = -1;
        }

        public SparseBooleanArray getSelected() {
            return mCheckedArray;
        }

        public RecordDataObject[] getDataSet() {
            return mRecordDataSet;
        }
    }
}
