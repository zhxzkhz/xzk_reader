package com.zhhz.reader.ui.bookrack;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.OperationMonitor;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhhz.reader.R;
import com.zhhz.reader.activity.BookReaderActivity;
import com.zhhz.reader.activity.SearchActivity;
import com.zhhz.reader.adapter.BookAdapter;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.databinding.FragmentBookrackBinding;
import com.zhhz.reader.util.DiskCache;
import com.zhhz.reader.util.FileSizeUtil;
import com.zhhz.reader.util.FileUtil;
import com.zhhz.reader.util.GlideGetPath;
import com.zhhz.reader.util.StringUtil;
import com.zhhz.reader.util.XluaTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BookRackFragment extends Fragment {

    private final ArrayList<String> lists = new ArrayList<>();
    SelectionTracker<String> tracker;
    private BookRackViewModel bookrackViewModel;
    private FragmentBookrackBinding binding;
    private BookAdapter bookAdapter;
    private ActivityResultLauncher<Intent> launcher;

    private ActivityResultLauncher<String> import_launcher;

    private AlertDialog alertDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (bookrackViewModel != null) {
                bookrackViewModel.updateBooks();
            }
        });


        alertDialog = new AlertDialog.Builder(requireContext())
                .setMessage("书本解析中").create();

        import_launcher = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            alertDialog.show();
            bookrackViewModel.importLocalBook(result);
        });

    }

    @SuppressLint("NotifyDataSetChanged")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        bookrackViewModel = new ViewModelProvider(requireActivity()).get(BookRackViewModel.class);

        binding = FragmentBookrackBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        bookAdapter = new BookAdapter(BookRackFragment.this.getContext());
        bookAdapter.setHasStableIds(true);
        //设置Item增加、移除动画
        binding.rv.setItemAnimator(new DefaultItemAnimator());
        binding.rv.setLayoutManager(new GridLayoutManager(BookRackFragment.this.getContext(), 3, GridLayoutManager.VERTICAL, false));
        //固定高度
        binding.rv.setHasFixedSize(true);
        binding.rv.setAdapter(bookAdapter);


        //设置点击事件
        binding.searchView.setOnClickListener(view -> {
            Intent intent = new Intent(BookRackFragment.this.getContext(), SearchActivity.class);
            //startActivity(intent);
            launcher.launch(intent);
            BookRackFragment.this.requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        binding.refreshLayout.setOnRefreshListener(refreshLayout -> bookrackViewModel.updateCatalogue());

        //导入书本
        binding.bookrackSetting.setOnClickListener(view -> import_launcher.launch("text/*"));

        tracker = new SelectionTracker.Builder<>(
                "my-selection-id",
                binding.rv,
                new StringItemKeyProvider(1, lists),
                new MyDetailsLookup(binding.rv),
                StorageStrategy.createStringStorage())
                .withSelectionPredicate(SelectionPredicates.createSelectAnything())
                .withOperationMonitor(new OperationMonitor())
                .withOnItemActivatedListener((item, e) -> {
                    Intent intent = new Intent(BookRackFragment.this.getContext(), BookReaderActivity.class);
                    //获取点击事件位置
                    int position = item.getPosition();
                    bookAdapter.getItemData().get(position).setUpdate(false);
                    bookrackViewModel.updateBook(bookAdapter.getItemData().get(position));
                    bookAdapter.notifyItemChanged(position);
                    intent.putExtra("book", bookAdapter.getItemData().get(position));
                    //startActivity(intent);
                    launcher.launch(intent);
                    tracker.clearSelection();
                    BookRackFragment.this.requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                })
                .build();


        tracker.addObserver(new SelectionTracker.SelectionObserver<String>() {
            @Override
            public void onItemStateChanged(@NonNull String key, boolean selected) {
                super.onItemStateChanged(key, selected);
                LinearLayoutCompat item_menu = requireActivity().findViewById(R.id.item_menu);
                if (tracker.getSelection().size() > 0 && item_menu.getVisibility() == View.GONE) {
                    item_menu.setVisibility(View.VISIBLE);
                } else if (tracker.getSelection().size() == 0 && item_menu.getVisibility() == View.VISIBLE) {
                    item_menu.setVisibility(View.GONE);
                }
                item_menu.getChildAt(0).setEnabled(tracker.getSelection().size() == 1);
                item_menu.getChildAt(1).setEnabled(tracker.getSelection().size() == 1);
            }
        });

        bookAdapter.setSelectionTracker(tracker);

        //导入书本回调事件
        bookrackViewModel.getCallback().observe(getViewLifecycleOwner(), aBoolean -> {
            String s;
            if (aBoolean) {
                s = "成功";
            } else {
                s = "失败";
            }
            alertDialog.hide();
            Toast.makeText(requireContext(), "书本导入" + s, Toast.LENGTH_SHORT).show();
        });

        //获取本地书架回调事件
        bookrackViewModel.getData().observe(getViewLifecycleOwner(), list -> {
            lists.clear();
            for (BookBean bookBean : list) {
                lists.add(bookBean.getBook_id());
            }

            bookAdapter.setItemData(list);
            bookAdapter.notifyDataSetChanged();
            //bookrackViewModel.updateCatalogue();
        });

        //更新目录回调
        bookrackViewModel.getCatalogue().observe(getViewLifecycleOwner(), bookBean -> {
            if (bookBean != null) {
                int pos = bookAdapter.getItemData().indexOf(bookBean);
                if (pos > -1) {
                    bookAdapter.notifyItemChanged(pos);
                }
            }
            binding.refreshLayout.finishRefresh();
        });

        final TypedArray a = requireContext().obtainStyledAttributes(null, com.google.android.material.R.styleable.AlertDialog,
                com.google.android.material.R.attr.alertDialogStyle, 0);
        ArrayList<String> arr_list = new ArrayList<>(Arrays.asList("只删除书架记录", "删除书架记录并删除本地缓存文件(大小:计算中)"));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), a.getResourceId(com.google.android.material.R.styleable.AlertDialog_singleChoiceItemLayout, 0), arr_list);
        a.recycle();

        //长按多选事件
        bookrackViewModel.getOperation().observe(getViewLifecycleOwner(), integer -> {
            if (integer == 2) {
                String[] ss = new String[tracker.getSelection().size()];
                int index = 0;
                for (String s : tracker.getSelection()) {
                    ss[index++] = s;
                }
                CopyOnWriteArrayList<String> fileList = new CopyOnWriteArrayList<>();
                arr_list.set(1, "删除书架记录并删除本地缓存文件(大小:计算中)");
                AlertDialog dialog = new AlertDialog.Builder(requireContext())
                        .setTitle("删除提示")
                        //.setMessage("确定删除书本？")
                        .setSingleChoiceItems(adapter, 0, null)
                        .setPositiveButton("确定", (dialogInterface, i) -> {
                            bookrackViewModel.removeBooks(ss);
                            bookrackViewModel.updateBooks();
                            //为1时删除所以记录
                            if ((((AlertDialog) dialogInterface).getListView().getCheckedItemPosition()) == 1) {
                                CompletableFuture.runAsync(() -> {
                                    for (String value : ss) {
                                        FileUtil.deleteFolders(DiskCache.path + File.separator + "book" + File.separator + value);
                                    }
                                    fileList.forEach(s -> new File(s).delete());
                                }, XluaTask.getThreadPool());
                            }
                        })
                        .setOnCancelListener(DialogInterface::dismiss)
                        .setNeutralButton("取消", null)
                        .create();
                dialog.show();


                CompletableFuture.runAsync(() -> {
                    long time = System.currentTimeMillis();
                    AtomicLong count_size = new AtomicLong();
                    AtomicInteger list_index = new AtomicInteger(0);
                    ArrayList<File> paths = new ArrayList<>();
                    List<CompletableFuture<Integer>> list = new ArrayList<>();
                    CopyOnWriteArrayList<String> copyOnWriteArrayList = new CopyOnWriteArrayList<>();

                    for (String s : ss) {
                        BookBean bean = null;
                        for (BookBean itemDatum : bookAdapter.getItemData()) {
                            if (s.equals(itemDatum.getBook_id())) {
                                bean = itemDatum;
                                break;
                            }
                        }
                        if (bean == null) break;
                        String path = DiskCache.path + File.separator + "book" + File.separator + s;
                        count_size.addAndGet((long) FileSizeUtil.getFileOrFilesSize(path, FileSizeUtil.SIZE_TYPE_B));
                        if (bean.getBook_id().equals(StringUtil.getMD5(bean.getTitle() + "▶☀true☀◀" + bean.getAuthor()))) {
                            File file = new File(path + File.separator + "book_chapter");
                            if (file.isDirectory()) {
                                paths.add(file);
                            }
                        }
                    }

                    for (int x = 0; x < (Math.min(paths.size(), 4)); x++) {
                        list.add(CompletableFuture.supplyAsync(() -> {
                            int length;
                            while ((length = list_index.getAndIncrement()) < paths.size()) {
                                File path = paths.get(length);
                                for (File listFile : Objects.requireNonNull(path.listFiles())) {
                                    try {
                                        FileReader fileReader = new FileReader(listFile);
                                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                                        bufferedReader.lines().forEach(copyOnWriteArrayList::add);
                                        bufferedReader.close();
                                        fileReader.close();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                            return null;
                        }, XluaTask.getThreadPool()));
                    }

                    CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
                    list.clear();
                    list_index.set(0);

                    for (int x = 0; x < (Math.min(copyOnWriteArrayList.size(), 4)); x++) {
                        list.add(CompletableFuture.supplyAsync(() -> {
                            int length;
                            while ((length = list_index.getAndIncrement()) < copyOnWriteArrayList.size()) {
                                String path = GlideGetPath.getCacheFileKey(copyOnWriteArrayList.get(length));
                                long len = (long) FileSizeUtil.getFileOrFilesSize(path, FileSizeUtil.SIZE_TYPE_B);
                                count_size.addAndGet(len);
                                if (len > 0) fileList.add(path);
                            }
                            return null;
                        }, XluaTask.getThreadPool()));
                    }

                    CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).thenRunAsync(() -> requireActivity().runOnUiThread(() -> {
                        arr_list.set(1, "删除书架记录并删除本地缓存文件(大小:" + FileSizeUtil.ConvertFileSize(count_size.get()) + ")");
                        adapter.notifyDataSetChanged();
                    }));


                }, XluaTask.getThreadPool());

            }
        });

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(bookrackViewModel.getData().getValue()).clear();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        tracker.onSaveInstanceState(outState);
    }

    public static class StringItemKeyProvider extends ItemKeyProvider<String> {

        private final List<String> items;

        public StringItemKeyProvider(int scope, List<String> items) {
            super(scope);
            this.items = items;
        }

        @Nullable
        @Override
        public String getKey(int position) {
            return items.get(position);
        }

        @Override
        public int getPosition(@NonNull String key) {
            return items.indexOf(key);
        }
    }

    private static class MyDetailsLookup extends ItemDetailsLookup<String> {

        private final RecyclerView mRecyclerView;

        public MyDetailsLookup(RecyclerView rv) {
            mRecyclerView = rv;
        }

        @Nullable
        @Override
        public ItemDetails<String> getItemDetails(@NonNull MotionEvent e) {
            View view = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
            if (view != null) {
                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(view);
                if (holder instanceof BookAdapter.ViewHolder)
                    return ((BookAdapter.ViewHolder) holder).getItemDetails();
            }
            return null;
        }
    }
}