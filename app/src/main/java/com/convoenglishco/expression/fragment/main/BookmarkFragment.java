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
import com.convoenglishllc.expression.data.manager.BookmarkManager;
import com.convoenglishllc.expression.data.manager.LessonManager;
import com.convoenglishllc.expression.data.model.LessonDataObject;
import com.convoenglishllc.expression.helper.ItemTouchHelperAdapter;
import com.convoenglishllc.expression.utils.GlobalConstants;
import com.convoenglishllc.expression.utils.ImageProcess;
import com.convoenglishllc.expression.utils.L;

import java.util.ArrayList;

public class BookmarkFragment extends BaseFragment {
    private final String TAG = this.getClass().getSimpleName();

    private LessonDataObject[] mLessonDataSet = null;

    private View rootView = null;
    private static final int SPAN_COUNT = 2;
    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;

    protected BookmarkAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected RecyclerView uiRecyclerView;

    public static BookmarkFragment newInstance() {
        return new BookmarkFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        int[] bookIds = BookmarkManager.getBookedIds(getContext());
        mLessonDataSet = new LessonDataObject[bookIds.length];
        for(int i=0; i<bookIds.length; i++) {
            mLessonDataSet[i] = LessonManager.getLessonByNo(getContext(), bookIds[i]);
        }
        mAdapter = new BookmarkAdapter(mLessonDataSet);
        uiRecyclerView.setAdapter(mAdapter);

        if(mLessonDataSet.length == 0) rootView.findViewById(R.id.no_bookmark).setVisibility(View.VISIBLE);
        else rootView.findViewById(R.id.no_bookmark).setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        L.d(TAG, "onCreateView()");
        mTitle = getContext().getString(R.string.app_name);
        mSubTitle = getContext().getString(R.string.title_bookmarks);
        rootView = inflater.inflate(R.layout.fragment_main_bookmarks, container, false);

        uiRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());

        Configuration newConfig = getResources().getConfiguration();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        return rootView;
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

    class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> implements ItemTouchHelperAdapter {
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
                //categoryView = (TextView) v.findViewById(R.id.ui_category);
                mainView = v;
            }
            public ImageView getImageView() { return imageView; }
            public TextView getTitleView() { return titleView; }
            //public TextView getCategoryView() { return categoryView; }

            public View getMainView() { return mainView; }
        }

        public BookmarkAdapter(LessonDataObject[] dataSet) {
            mLessonDataSet = dataSet;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.row_bookmark, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {
            Configuration newConfig = viewHolder.getMainView().getContext().getResources().getConfiguration();
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if(position % 4 == 0) viewHolder.getMainView().setBackgroundColor(Color.rgb(192, 217, 239));
                else if(position % 4 == 1) viewHolder.getMainView().setBackgroundColor(Color.WHITE);
                else if(position % 4 == 2) viewHolder.getMainView().setBackgroundColor(Color.WHITE);
                else if(position % 4 == 3) viewHolder.getMainView().setBackgroundColor(Color.rgb(192, 217, 239));
            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if(position % 2 == 0) viewHolder.getMainView().setBackgroundColor(Color.rgb(192, 217, 239));
                else if(position % 2 == 1) viewHolder.getMainView().setBackgroundColor(Color.WHITE);
            }
            ImageProcess.loadAssetImage(getContext(), mLessonDataSet[position].getDrawableAssetName(), viewHolder.getImageView());
            viewHolder.getTitleView().setText(mLessonDataSet[position].getTitle());
            //viewHolder.getCategoryView().setText(mLessonDataSet[position].getSubCategory());
        }

        @Override
        public int getItemCount() {
            return mLessonDataSet.length;
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            return false;
        }

        @Override
        public void onItemDismiss(int position) {
            BookmarkManager.removeId(getContext(), mLessonDataSet[position].getNo());

            ArrayList<LessonDataObject> newList = new ArrayList<>();
            for(int i=0; i<mLessonDataSet.length; i++) {
                if(position != i) newList.add(mLessonDataSet[i]);
            }
            mLessonDataSet = newList.toArray(new LessonDataObject[newList.size()]);
            notifyDataSetChanged();
        }
    }
}
