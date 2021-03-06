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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ComicReaderFragment extends Fragment {

    private BookReaderViewModel mViewModel;

    private FragmentComicreaderBinding binding;

    private AppCompatButton error_btn;

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
        //??????Item?????????????????????
        binding.readerComic.setItemAnimator(null);
        binding.readerComic.setLayoutManager(new LinearLayoutManager(getContext()));
        //????????????
        binding.readerComic.setHasFixedSize(true);
        //binding.readerComic.addItemDecoration(new RecycleViewDivider(this.getContext(), 1));
        binding.readerComic.setAdapter(comicAdapter);

        ListPreloader.PreloadModelProvider<GlideUrl> preloadModelProvider = new ListPreloader.PreloadModelProvider<GlideUrl>() {

            @NonNull
            @Override
            public List<GlideUrl> getPreloadItems(int position) {
                //imagesList???????????????????????????
                if (position < comicAdapter.getItemData().size()) {
                    //??????RecyclerViewPreloader??????item????????????????????????url??????
                    return comicAdapter.getItemData().subList(position, position + 1);
                } else {
                    return comicAdapter.getItemData().subList(comicAdapter.getItemData().size() - 1, comicAdapter.getItemData().size());
                }
            }

            @NonNull
            @Override
            public RequestBuilder<?> getPreloadRequestBuilder(@NonNull GlideUrl url) {
                //???????????????????????????RequestBuilder
                return GlideApp.with(requireActivity()).load(url);
            }
        };

        ViewPreloadSizeProvider<GlideUrl> sizeProvider = new ViewPreloadSizeProvider<>();
        RecyclerViewPreloader<GlideUrl> preloaded = new RecyclerViewPreloader<>(GlideApp.with(binding.readerComic),
                preloadModelProvider, sizeProvider, 10);
        binding.readerComic.addOnScrollListener(preloaded);

        binding.readerComic.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                try {
                    @NonNull LinearLayoutManager layout_manager = (LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager());
                    //???????????????????????????????????????
                    int last_completely = layout_manager.findLastCompletelyVisibleItemPosition();
                    //????????????????????????????????????
                    int first_completely = layout_manager.findFirstCompletelyVisibleItemPosition();
                    //?????????????????????????????????
                    int last = layout_manager.findLastVisibleItemPosition();
                    //??????????????????????????????
                    int first = layout_manager.findFirstVisibleItemPosition();
                    int count = comicAdapter.getItemData().size();
                    int page;

                    //dy ??????0???????????????
                    if (dy > 0) {
                        if (last_completely == count) {
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
                        page = mViewModel.getStart() + 1;
                    }
                    binding.progressText.setText(requireContext().getString(R.string.progress_text, page, count));

                    if (mViewModel.getStart() + 1 != page || page == count) {
                        if (page != count || mViewModel.getStart() + 2 == page) {
                            mViewModel.setStart(page - 1);
                            mViewModel.saveProgressComic();
                        }
                        if (page + 7 > count && !loading && mViewModel.isHaveNextChapters(mViewModel.current_progress_page(page - 1)[0])) {
                            loading = true;
                            mViewModel.loadNextChapters();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

        GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (e.getX() > binding.readerComic.getWidth() / 3f && e.getX() < binding.readerComic.getWidth() / 3f * 2f)
                    if (container != null) {
                        container.callOnClick();
                    }
                return super.onSingleTapConfirmed(e);
            }

        });

        binding.readerComic.setOnTouchListener((view, motionEvent) -> gestureDetector.onTouchEvent(motionEvent));

        error_btn = new AppCompatButton(requireContext());
        error_btn.setText("????????????");

        return root;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel.getDataContent().observe(getViewLifecycleOwner(), map -> {
            binding.progress.hide();
            if (map.containsKey("error")) {
                new AlertDialog.Builder(requireContext()).setTitle("????????????")
                        .setMessage((CharSequence) map.get("error"))
                        .show();
                binding.bookReader.addView(error_btn, binding.progress.getLayoutParams());
            } else {
                int length = comicAdapter.getItemData().size();
                if ("true".equals(String.valueOf(map.get("end")))) {
                    comicAdapter.setItemData(new ArrayList<>(mViewModel.getComic_list()));
                    comicAdapter.notifyDataSetChanged();
                    @NonNull LinearLayoutManager layout_manager = (LinearLayoutManager) Objects.requireNonNull(binding.readerComic.getLayoutManager());
                    layout_manager.scrollToPositionWithOffset(mViewModel.getStart(), 0);
                } else {
                    comicAdapter.getItemData().addAll(mViewModel.getComic_list());
                    comicAdapter.notifyItemRangeInserted(length, mViewModel.getComic_list().size());
                }
                length = comicAdapter.getItemData().size();
                binding.progressText.setText(requireContext().getString(R.string.progress_text, mViewModel.getStart() + 1, length));
            }
            loading = false;
        });

        mViewModel.getChapters().observe(getViewLifecycleOwner(), title -> {
            if (mViewModel.getStart() == 0) binding.progress.show();
        });

        mViewModel.queryCatalogue();
        int[] r = mViewModel.readProgress();
        mViewModel.setProgress(r[0]);
        mViewModel.setStart(r[1]);
        mViewModel.getContentComic(true);

    }


}