package com.convoenglishllc.expression.fragment.main;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.convoenglishllc.expression.R;
import com.convoenglishllc.expression.data.model.AppDataObject;
import com.convoenglishllc.expression.utils.L;

public class AppListFragment extends BaseFragment {
    private final String TAG = this.getClass().getSimpleName();
    private static final int SPAN_COUNT = 2;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;
    protected RecyclerView uiRecyclerView;
    protected AppAdapter mAdapter = null;
    protected RecyclerView.LayoutManager mLayoutManager;

    public static AppListFragment newInstance() { return new AppListFragment();}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        L.d(TAG, "onCreateView()");
        mTitle = getContext().getString(R.string.title_apps);
        mSubTitle = getContext().getString(R.string.subtitle_apps);
        View rootView = inflater.inflate(R.layout.fragment_main_apps, container, false);

        uiRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());

        Configuration newConfig = getResources().getConfiguration();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        mAdapter = new AppAdapter(AppDataObject.getRecommendedApps());
        uiRecyclerView.setAdapter(mAdapter);

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

    class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
        private AppDataObject[] mDataSet = null;

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final ImageView imageView;
            private final TextView titleView;
            private final TextView summaryView;

            public ViewHolder(View v) {
                super(v);
//                v.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if(getAdapterPosition() == -1) return;
//                        String url = mDataSet[getAdapterPosition()].app_url;
//                        Intent i = new Intent(Intent.ACTION_VIEW);
//                        i.setData(Uri.parse(url));
//                        getActivity().startActivity(i);
//                    }
//                });
                imageView = (ImageView)v.findViewById(R.id.ui_image);
                titleView = (TextView) v.findViewById(R.id.ui_title);
                summaryView = (TextView) v.findViewById(R.id.ui_summary);
            }

            public ImageView getImageView() { return imageView; }
            public TextView getTitleView() { return titleView; }
            public TextView getSummaryView() { return summaryView; }
        }

        public AppAdapter(AppDataObject[] apps) {
            mDataSet = apps;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.row_app, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        /*
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
        */
            AppDataObject app = mDataSet[position];
            Picasso.with(getContext()).load("file:///android_asset/" + app.app_image).into(viewHolder.getImageView());
            viewHolder.getTitleView().setText(app.app_title);
            viewHolder.getSummaryView().setText(app.app_summary);
        }

        @Override
        public int getItemCount() {
            return mDataSet.length;
        }
    }
}
