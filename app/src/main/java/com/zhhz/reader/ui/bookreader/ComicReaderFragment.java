package com.zhhz.reader.ui.bookreader;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.zhhz.reader.adapter.ComicAdapter;
import com.zhhz.reader.databinding.FragmentComicreaderBinding;
import com.zhhz.reader.util.GlideApp;
import com.zhhz.reader.view.RecycleViewDivider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ComicReaderFragment extends Fragment {

    private BookReaderViewModel mViewModel;

    private FragmentComicreaderBinding binding;

    private AppCompatButton error_btn;

    private ComicAdapter comicAdapter;

    public static ComicReaderFragment newInstance() {
        return new ComicReaderFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(requireActivity()).get(BookReaderViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentComicreaderBinding.inflate(inflater,container,false);
        View root = binding.getRoot();

        comicAdapter = new ComicAdapter(requireContext());
        comicAdapter.setHasStableIds(true);
        //设置Item增加、移除动画
        binding.readerComic.setItemAnimator(null);
        binding.readerComic.setLayoutManager(new LinearLayoutManager(getContext()));
        //固定高度
        binding.readerComic.setHasFixedSize(true);
        binding.readerComic.addItemDecoration(new RecycleViewDivider(this.getContext(), 1));
        binding.readerComic.setAdapter(comicAdapter);

        ListPreloader.PreloadModelProvider<String> preloadModelProvider = new ListPreloader.PreloadModelProvider<String>(){

            @NonNull
            @Override
            public List<String> getPreloadItems(int position) {
                //imagesList是你的图片地址列表
                if(position < comicAdapter.getItemData().size()){
                    //告诉RecyclerViewPreloader每个item项需要加载的图片url集合
                    return comicAdapter.getItemData().subList(position, position+1);
                }else {
                    return comicAdapter.getItemData().subList(comicAdapter.getItemData().size() - 1, comicAdapter.getItemData().size());
                }
            }

            @NonNull
            @Override
            public RequestBuilder<?> getPreloadRequestBuilder(@NonNull String url) {
                //返回一个加载图片的RequestBuilder
                return GlideApp.with(requireActivity()).load(url);
            }
        };
        ViewPreloadSizeProvider<String> sizeProvider = new ViewPreloadSizeProvider<>();
        RecyclerViewPreloader<String> preloaded = new RecyclerViewPreloader<>(GlideApp.with(binding.readerComic),
                preloadModelProvider, sizeProvider, 10);
        binding.readerComic.addOnScrollListener(preloaded);

        error_btn = new AppCompatButton(requireContext());
        error_btn.setText("重新加载");

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel.getDataContent().observe(getViewLifecycleOwner(), map -> {
            binding.progress.hide();
            if (map.containsKey("error")){
                binding.bookReader.addView(error_btn,binding.progress.getLayoutParams());
            } else {
                String[] arr = Objects.requireNonNull(map.get("content")).split("\n");
                int length = comicAdapter.getItemData().size();
                comicAdapter.getItemData().addAll(Arrays.asList(arr));
                comicAdapter.notifyItemRangeInserted(length,arr.length);
            }
        });

        mViewModel.getChapters().observe(getViewLifecycleOwner(), title -> {
            binding.progress.show();
        });

        mViewModel.queryCatalogue();
        int[] r = mViewModel.readProgress();
        mViewModel.setProgress(r[0]);
        mViewModel.setStart(r[1]);
        mViewModel.getContent();

    }

}