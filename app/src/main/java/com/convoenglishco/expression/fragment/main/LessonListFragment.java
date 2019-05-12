package com.convoenglishllc.expression.fragment.main;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.convoenglishllc.expression.R;
import com.convoenglishllc.expression.activity.MainActivity;
import com.convoenglishllc.expression.data.manager.LessonManager;
import com.convoenglishllc.expression.data.model.LessonDataObject;
import com.convoenglishllc.expression.utils.GlobalConstants;
import com.convoenglishllc.expression.utils.ImageProcess;
import com.convoenglishllc.expression.utils.L;

public class LessonListFragment extends BaseFragment {
    private final String TAG = this.getClass().getSimpleName();

    private LessonDataObject[] mLessonDataSet = null;

    private static final int SPAN_COUNT = 2;
    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;
    protected RecyclerView uiRecyclerView;
    protected LessonAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;

    private int mCategoryNo = -1;

    public static LessonListFragment newInstance(int caregoryNo) {
        LessonListFragment f = new LessonListFragment();
        f.mCategoryNo = caregoryNo;
        return f;
    }

    public LessonListFragment initData(int category_no) {
        String[] categories = LessonManager.getCategories(getContext());

        mTitle = getContext().getString(R.string.app_name);
        mSubTitle = categories[category_no];

        mLessonDataSet = LessonManager.getDataByCategory(getContext(), categories[category_no]);

        setRetainInstance(true);

        for(LessonDataObject ld : mLessonDataSet) ImageProcess.preCacheAssetImage(getActivity(), ld.getDrawableAssetName());
        return this;
    }

    public int getCategoryNo() {
        return mCategoryNo;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("CATEGORY_NO", mCategoryNo);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            initData(this.mCategoryNo);
        }
        if(savedInstanceState != null && savedInstanceState.containsKey("CATEGORY_NO")) {
            mCategoryNo = savedInstanceState.getInt("CATEGORY_NO");
            initData(mCategoryNo);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        L.d(TAG, "onCreateView()");
        View rootView = inflater.inflate(R.layout.fragment_main_conversations, container, false);

        uiRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());

        Configuration newConfig = getResources().getConfiguration();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        updateLessonDataSet();

        return rootView;
    }

    private void updateLessonDataSet() {
        if(mLessonDataSet == null) return;
        mAdapter = new LessonAdapter(mLessonDataSet);
        uiRecyclerView.setAdapter(mAdapter);
    }

    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        if (uiRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) uiRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        uiRecyclerView.setLayoutManager(mLayoutManager);
        uiRecyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int currentOrientation = getResources().getConfiguration().orientation;

        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            setRecyclerViewLayoutManager(LayoutManagerType.GRID_LAYOUT_MANAGER);
        } else {
            setRecyclerViewLayoutManager(LayoutManagerType.LINEAR_LAYOUT_MANAGER);
        }
    }

    class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.ViewHolder> {
        //private final String TAG = this.getClass().getSimpleName();

        private boolean clicked = false;

        private LessonDataObject[] mLessonDataSet;
        public class ViewHolder extends RecyclerView.ViewHolder {
            private final View mainView;

            private final ImageView imageView;
            private final TextView titleView;

            public ViewHolder(View v) {
                super(v);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public synchronized void onClick(View v) {
                        if(getAdapterPosition() == -1) return;
                        if(clicked) return;
                        clicked = true;
                        new Handler().postDelayed(new Runnable(){
                            @Override
                            public void run() {
                                clicked = false;
                            }
                        }, GlobalConstants.DELAY_CLICK_LESSON_ITEM);
                        int lessonNo = mLessonDataSet[getAdapterPosition()].getNo();
                        MainActivity activity = (MainActivity)getActivity();
                        activity.startLessonActivity(lessonNo);
                    }
                });
                imageView = (ImageView)v.findViewById(R.id.ui_image);
                titleView = (TextView) v.findViewById(R.id.ui_title);
                mainView = v;
            }
            public ImageView getImageView() { return imageView; }
            public TextView getTitleView() { return titleView; }

            public View getMainView() { return mainView; }
        }

        public LessonAdapter(LessonDataObject[] dataSet) {
            mLessonDataSet = dataSet;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.row_lesson, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {
            Configuration newConfig = viewHolder.getMainView().getContext().getResources().getConfiguration();
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if(position % 4 == 0) viewHolder.getMainView().setBackgroundColor(Color.rgb(211, 211, 211));
                else if(position % 4 == 1) viewHolder.getMainView().setBackgroundColor(Color.WHITE);
                else if(position % 4 == 2) viewHolder.getMainView().setBackgroundColor(Color.WHITE);
                else if(position % 4 == 3) viewHolder.getMainView().setBackgroundColor(Color.rgb(211, 211, 211));
            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if(position % 2 == 0) viewHolder.getMainView().setBackgroundColor(Color.rgb(211, 211, 211));
                else if(position % 2 == 1) viewHolder.getMainView().setBackgroundColor(Color.WHITE);
            }
            ImageProcess.loadAssetImage(getContext(), mLessonDataSet[position].getDrawableAssetName(), viewHolder.getImageView());
            viewHolder.getTitleView().setText(mLessonDataSet[position].getTitle());
        }

        @Override
        public int getItemCount() {
            return mLessonDataSet.length;
        }
    }
}
