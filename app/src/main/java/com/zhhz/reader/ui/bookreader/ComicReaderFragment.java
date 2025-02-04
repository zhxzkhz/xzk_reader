package com.zhhz.reader.ui.bookreader;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.zhhz.reader.R;
import com.zhhz.reader.adapter.ComicAdapter;
import com.zhhz.reader.databinding.FragmentComicreaderBinding;
import com.zhhz.reader.util.GlideApp;
import com.zhhz.reader.util.GlideRequests;
import com.zhhz.reader.util.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ComicReaderFragment extends Fragment {

    private BookReaderViewModel mViewModel;

    private FragmentComicreaderBinding binding;

    private AppCompatButton errorRetryButton;

    private ComicAdapter comicAdapter;

    private Boolean loading = false;

    public static ComicReaderFragment newInstance() {
        return new ComicReaderFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(BookReaderViewModel.class);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentComicreaderBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        comicAdapter = new ComicAdapter(requireContext());
        //comicAdapter.setHasStableIds(true);
        //设置Item增加、移除动画
        binding.readerComic.setItemAnimator(null);
        binding.readerComic.setLayoutManager(new LinearLayoutManager(getContext()));
        //固定高度
        binding.readerComic.setHasFixedSize(true);
        //binding.readerComic.addItemDecoration(new RecycleViewDivider(this.getContext(), 1));
        binding.readerComic.setAdapter(comicAdapter);

        GlideRequests imageLoader = GlideApp.with(binding.readerComic);
        RecyclerViewPreloader<GlideUrl> preloaded = new RecyclerViewPreloader<>(imageLoader, new GlideUrlPreloadModelProvider(imageLoader), new ViewPreloadSizeProvider<>(binding.readerComic), 10);
        binding.readerComic.addOnScrollListener(preloaded);
        binding.readerComic.addOnScrollListener(new MyOnScrollListener());

        GestureDetector gestureDetector = getGestureDetector(container);
        binding.readerComic.setOnTouchListener((view, motionEvent) -> gestureDetector.onTouchEvent(motionEvent));

        //动态添加错误重试按钮
        errorRetryButton = new AppCompatButton(requireContext());
        errorRetryButton.setText("重新加载");
        errorRetryButton.setOnClickListener(view1 -> {
            view1.setVisibility(View.INVISIBLE);
            mViewModel.getContentComic(true);
        });
        errorRetryButton.setVisibility(View.INVISIBLE);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(-2,-2);
        layoutParams.topToTop = binding.progress.getId();
        layoutParams.bottomToBottom = binding.progress.getId();
        layoutParams.leftToLeft = binding.progress.getId();
        layoutParams.rightToRight = binding.progress.getId();
        binding.bookReader.addView(errorRetryButton, layoutParams);
        return root;
    }

    @NonNull
    private GestureDetector getGestureDetector(@Nullable ViewGroup container) {

        GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                if (e.getX() > binding.readerComic.getWidth() / 3f && e.getX() < binding.readerComic.getWidth() / 3f * 2f)
                    if (container != null) {
                        container.callOnClick();
                        return true;
                    }
                return super.onSingleTapConfirmed(e);
            }

        });
        gestureDetector.setIsLongpressEnabled(false);

        return gestureDetector;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewModel.getDataContent().observe(getViewLifecycleOwner(), contentBean -> {
            binding.progress.hide();
            if (!contentBean.isStatus()) {
                new AlertDialog.Builder(requireContext()).setTitle("错误提示")
                        .setMessage(contentBean.getError())
                        .show();
                if (comicAdapter.getItemData().isEmpty()) {
                    errorRetryButton.setVisibility(View.VISIBLE);
                }
            } else {
                int length = comicAdapter.getItemData().size();
                if (contentBean.getPreviousPage()) {
                    comicAdapter.setItemData(new ArrayList<>(mViewModel.getComicList()));
                    comicAdapter.notifyDataSetChanged();
                    @NonNull LinearLayoutManager layout_manager = (LinearLayoutManager) Objects.requireNonNull(binding.readerComic.getLayoutManager());
                    layout_manager.scrollToPositionWithOffset(mViewModel.getPos(), 0);
                } else {
                    comicAdapter.getItemData().addAll(mViewModel.getComicList());
                    comicAdapter.notifyItemRangeInserted(length, mViewModel.getComicList().size());
                }
                length = comicAdapter.getItemData().size();
                binding.progressText.setText(requireContext().getString(R.string.progress_text, mViewModel.getPos() + 1, length));
            }
            loading = false;
        });

        mViewModel.getChapters().observe(getViewLifecycleOwner(), title -> {
            if (mViewModel.getPos() == 0) {
                binding.progress.show();
                errorRetryButton.setVisibility(View.INVISIBLE);
            }
        });
        binding.progress.show();
        mViewModel.getTableOfContents();
        mViewModel.loadReadingProgress();
        mViewModel.getContentComic(true);

    }


    private class MyOnScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            try {
                @NonNull LinearLayoutManager layout_manager = (LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager());
                //最后一个完全可见的视图位置
                int last_completely = layout_manager.findLastCompletelyVisibleItemPosition();
                //第一个完全可见的视图位置
                int first_completely = layout_manager.findFirstCompletelyVisibleItemPosition();
                //最后一个可见的视图位置
                int last = layout_manager.findLastVisibleItemPosition();
                //第一个可见的视图位置
                int first = layout_manager.findFirstVisibleItemPosition();
                int count = comicAdapter.getItemData().size();
                int page;

                //dy 大于0代表往上拉
                if (dy > 0) {
                    if (last_completely == count - 1) {
                        page = count;
                    } else if (first_completely == -1) {
                        page = first + 1;
                    } else {
                        page = first_completely + 1;
                    }
                } else if (dy < 0) {
                    if (first_completely == 0) {
                        page = 1;
                    } else if (last_completely == -1) {
                        page = last + 1;
                    } else {
                        page = last_completely + 1;
                    }
                } else {
                    page = mViewModel.getPos() + 1;
                }

                binding.progressText.setText(requireContext().getString(R.string.progress_text, page, count));

                if (mViewModel.getPos() + 1 != page || page == count) {
                    if (page != count || mViewModel.getPos() + 2 == page) {
                        mViewModel.setPos(page - 1);
                        mViewModel.saveProgressComic();
                    }
                    //通过最后一页判断是否还有下一章，从当前页加载时会因为加载的只有一张时重复加载
                    if (page + 7 > count && !loading && mViewModel.isHaveNextChapters(mViewModel.current_progress_page(count - 1)[0])) {
                        loading = true;
                        mViewModel.loadNextChapters();
                    }
                }
            } catch (Exception e) {
                LogUtil.error(e);
            }


        }
    }

    private class GlideUrlPreloadModelProvider implements ListPreloader.PreloadModelProvider<GlideUrl> {

        private final GlideRequests imageLoader;

        public GlideUrlPreloadModelProvider(GlideRequests imageLoader) {
            this.imageLoader = imageLoader;
        }

        @NonNull
        @Override
        public List<GlideUrl> getPreloadItems(int position) {
            //告诉RecyclerViewPreloader每个item项需要加载的图片url集合
            return comicAdapter.getItemData().subList(position, Math.min(position + 1, comicAdapter.getItemData().size()));
        }

        @NonNull
        @Override
        public RequestBuilder<?> getPreloadRequestBuilder(@NonNull GlideUrl url) {
            //返回一个加载图片的RequestBuilder
            return imageLoader.load(url);
        }
    }
}