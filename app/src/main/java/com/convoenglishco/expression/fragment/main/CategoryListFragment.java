package com.convoenglishllc.expression.fragment.main;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
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
import com.convoenglishllc.expression.data.model.CategoryPurchaseObject;
import com.convoenglishllc.expression.utils.ImageProcess;
import com.convoenglishllc.expression.utils.L;


import java.util.ArrayList;

public class CategoryListFragment extends BaseFragment {
    private final String TAG = this.getClass().getSimpleName();

    private String[] mCategoryDataSet = null;
    private ArrayList<CategoryPurchaseObject> mPurchaseData = null;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;
    protected RecyclerView uiRecyclerView;
    protected CategoryAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;

    public int SPAN_COUNT = 2;
    public static CategoryListFragment newInstance() { return new CategoryListFragment();}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPurchaseData = LessonManager.getCategoryPurchaseStatus(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        L.d(TAG, "onCreateView()");
        mTitle = "Expressions, Slang, and Idioms";
        mSubTitle = "Categories";
        mCategoryDataSet = LessonManager.getCategories(getContext());
        View rootView = inflater.inflate(R.layout.fragment_main_categories, container, false);

        uiRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());

        Configuration newConfig = getResources().getConfiguration();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            SPAN_COUNT = 4;
            mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            SPAN_COUNT = 2;
            mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
        }

        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);

        mAdapter = new CategoryAdapter(mCategoryDataSet);
        uiRecyclerView.setAdapter(mAdapter);

        for(String assetName : LessonManager.getCategoryAssetNames()) ImageProcess.preCacheAssetImage(getActivity(), assetName);
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
            SPAN_COUNT = 4;
            setRecyclerViewLayoutManager(LayoutManagerType.GRID_LAYOUT_MANAGER);

        } else {
            SPAN_COUNT = 2;
            setRecyclerViewLayoutManager(LayoutManagerType.GRID_LAYOUT_MANAGER);

        }
    }

    class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
        private String mCategorySet[] = null;

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final View mainView;

            private final ImageView imageView;
            private final ImageView iv_lock_image;
            private final TextView titleView;

            public ViewHolder(View v, MainActivity activity) {
                super(v);
                mainView = v;
                final MainActivity finalActivity = activity;
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(getAdapterPosition() == -1) return;
                        if (mPurchaseData.get(getAdapterPosition()).getPurchaseStatus() == 1){
                            finalActivity.showLessonsFragment(getAdapterPosition());
                        }else{
                            showUnlockDialog();
                        }
//                        if(UnlockFragment.m_bPurchase == true){
//                            finalActivity.showLessonsFragment(getAdapterPosition());
//                        }else{
//                            showUnlockDialog();
//                        }

                    }
                });
                imageView = (ImageView)v.findViewById(R.id.ui_image);
                iv_lock_image = (ImageView)v.findViewById(R.id.iv_lock_image);
                titleView = (TextView) v.findViewById(R.id.ui_title);
            }

            public ImageView getImageView() { return imageView; }
            public TextView getTitleView() { return titleView; }
            public ImageView getLockImageView() { return iv_lock_image; }

            public View getMainView() { return mainView; }
        }

        public CategoryAdapter(String[] categorySet) {
            mCategorySet = categorySet;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.row_category, viewGroup, false);
            return new ViewHolder(v, (MainActivity)getActivity());
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {
            String category = mCategorySet[position];
            Configuration newConfig = viewHolder.getMainView().getContext().getResources().getConfiguration();
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if(position % 8 == 0) viewHolder.getMainView().setBackgroundColor(Color.rgb(211, 211, 211));
                else if(position % 8 == 1) viewHolder.getMainView().setBackgroundColor(Color.WHITE);
                else if(position % 8 == 2) viewHolder.getMainView().setBackgroundColor(Color.rgb(211, 211, 211));
                else if(position % 8 == 3) viewHolder.getMainView().setBackgroundColor(Color.WHITE);
                else if(position % 8 == 4) viewHolder.getMainView().setBackgroundColor(Color.WHITE);
                else if(position % 8 == 5) viewHolder.getMainView().setBackgroundColor(Color.rgb(211, 211, 211));
                else if(position % 8 == 6) viewHolder.getMainView().setBackgroundColor(Color.WHITE);
                else if(position % 8 == 7) viewHolder.getMainView().setBackgroundColor(Color.rgb(211, 211, 211));
            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if(position % 4 == 0) viewHolder.getMainView().setBackgroundColor(Color.WHITE);
                else if(position % 4 == 1) viewHolder.getMainView().setBackgroundColor(Color.rgb(211, 211, 211));
                else if(position % 4 == 2) viewHolder.getMainView().setBackgroundColor(Color.rgb(211, 211, 211));
                else if(position % 4 == 3) viewHolder.getMainView().setBackgroundColor(Color.WHITE);
            }

            //viewHolder.getImageView().setImageDrawable(LessonManager.getCategoryDrawable(position));
            //Picasso.with(MainActivity.gContext).load("file:///android_asset/" + LessonManager.getCategoryAssetName(position)).into(viewHolder.getImageView());
            if ( position >= 0 && position <= 4 ){
                ImageProcess.loadAssetImage(getActivity(), LessonManager.getCategoryAssetName(position), viewHolder.getImageView());
                viewHolder.getTitleView().setText(category);
                viewHolder.getLockImageView().setVisibility(View.INVISIBLE);
            }else{
                if (mPurchaseData.get(position).getPurchaseStatus() == 1){
//                if(UnlockFragment.m_bPurchase == true){
                    ImageProcess.loadAssetImage(getActivity(), LessonManager.getCategoryAssetName(position), viewHolder.getImageView());
                    viewHolder.getTitleView().setText(category);
                    viewHolder.getLockImageView().setVisibility(View.INVISIBLE);
                }else{
                    ImageProcess.loadGrayedAssetImage(getActivity(), LessonManager.getCategoryAssetName(position), viewHolder.getImageView());
                    viewHolder.getTitleView().setText(category);
                    viewHolder.getLockImageView().setVisibility(View.VISIBLE);
                }

            }

            //viewHolder.getSummarySubCategoryView().setText(String.format("%d Categories", LessonManager.getSubCategories(getContext(), category).length));
            //viewHolder.getSummaryConversationView().setText(String.format("%d Conversations", LessonManager.getDataByCategory(getContext(), category).length));
        }

        @Override
        public int getItemCount() {
            if(mCategorySet == null) return 0;
            return mCategorySet.length;
        }
    }
    private void showUnlockDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    getActivity());

        LayoutInflater inflater = this.getLayoutInflater();
        TextView dialogue_title;
        View titleView = inflater.inflate(R.layout.dialogue_title, null);
        dialogue_title = titleView.findViewById(R.id.dialogue_title);
        dialogue_title.setText(R.string.dialog_unlock_title);

        alertDialogBuilder.setCustomTitle(titleView);
        alertDialogBuilder
                .setMessage(getActivity().getString(R.string.dialog_unlock_confirm))
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       pushFragment(UnlockFragment.newInstance());
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void pushFragment(Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        //transaction.addToBackStack("fragment_container" + fm.getBackStackEntryCount());
        transaction.addToBackStack(null);
        //transaction.commit();
        transaction.detach(fragment).attach(fragment).commitAllowingStateLoss();
    }
}
