package com.zhhz.reader.ui.bookrack;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
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

import cn.hutool.core.util.ObjectUtil;
import com.zhhz.reader.R;
import com.zhhz.reader.activity.BookReaderActivity;
import com.zhhz.reader.activity.SearchActivity;
import com.zhhz.reader.adapter.BookAdapter;
import com.zhhz.reader.bean.BookBean;
import com.zhhz.reader.databinding.FragmentBookrackBinding;
import com.zhhz.reader.util.BookUtil;
import com.zhhz.reader.util.DiskCache;
import com.zhhz.reader.util.FileSizeUtil;
import com.zhhz.reader.util.FileUtil;
import com.zhhz.reader.util.NotificationUtil;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

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
                    intent.putExtra("book",(Parcelable) bookAdapter.getItemData().get(position));
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
                if (!tracker.getSelection().isEmpty() && item_menu.getVisibility() == View.GONE) {
                    item_menu.setVisibility(View.VISIBLE);
                } else if (tracker.getSelection().isEmpty() && item_menu.getVisibility() == View.VISIBLE) {
                    item_menu.setVisibility(View.GONE);
                }
                item_menu.getChildAt(0).setEnabled(tracker.getSelection().size() == 1);
                item_menu.getChildAt(1).setEnabled(false);
                if (tracker.getSelection().size() == 1) {
                    for (BookBean itemDatum : bookAdapter.getItemData()) {
                        if (itemDatum.getBook_id().equals(tracker.getSelection().iterator().next()) && itemDatum.isComic()) {
                            item_menu.getChildAt(1).setEnabled(tracker.getSelection().size() == 1);
                        }
                    }
                }
            }
        });

        bookAdapter.setSelectionTracker(tracker);

        //导入书本回调事件
        bookrackViewModel.getCallback().observe(getViewLifecycleOwner(), aBoolean -> {
            alertDialog.hide();
            Toast.makeText(requireContext(), "书本导入" + (aBoolean ? "成功" : "失败"), Toast.LENGTH_SHORT).show();
        });

        //获取本地书架回调事件
        bookrackViewModel.getData().observe(getViewLifecycleOwner(), list -> {
            if (list.isEmpty() && ObjectUtil.isNotEmpty(savedInstanceState)) {
                list.addAll(Objects.requireNonNull(savedInstanceState.getParcelableArrayList("list")));
            }
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

        ArrayList<String> arr_list;
        ArrayAdapter<String> adapter;
        TypedArray a = requireContext().obtainStyledAttributes(null, com.google.android.material.R.styleable.AlertDialog,
                com.google.android.material.R.attr.alertDialogStyle, 0);
        arr_list = new ArrayList<>(Arrays.asList("只删除书架记录", "删除书架记录并删除本地缓存文件(大小:计算中)", "清除章节和图片缓存"));
        adapter = new ArrayAdapter<>(requireContext(), a.getResourceId(com.google.android.material.R.styleable.AlertDialog_singleChoiceItemLayout, 0), arr_list);
        a.recycle();

        //长按多选事件
        bookrackViewModel.getOperation().observe(getViewLifecycleOwner(), integer -> {

            if (integer == 0) {
                String name = tracker.getSelection().iterator().next();
                String[] lists = {"缓存后面10章","缓存后面30章","缓存后面50章","缓存后面100章","缓存后面200章","缓存后面全章","缓存所有章"};
                int[] num = {10,30,50,100,200,Integer.MAX_VALUE,-1};
                AtomicInteger index = new AtomicInteger();
                AlertDialog dialog = new AlertDialog.Builder(requireContext())
                        .setTitle("章节缓存")
                        .setSingleChoiceItems(lists, 0, (dialog12, which) -> {
                            index.set(which);
                        })
                        .setPositiveButton("确定", (dialog13, which) -> {
                            for (BookBean itemDatum : bookAdapter.getItemData()) {
                                if (name.equals(itemDatum.getBook_id())) {
                                    BookUtil.CacheBook(itemDatum,num[index.get()]);
                                    break;
                                }
                            }
                        })
                        .create();
                dialog.show();
            }else if (integer == 1) {
                AlertDialog dialog = new AlertDialog.Builder(requireContext())
                        .setView(R.layout.progress_dialog)
                        .setCancelable(false)
                        .setOnCancelListener(dialog1 -> BookUtil.cancel())
                        .create();
                dialog.show();

                String name = tracker.getSelection().iterator().next();
                final long[] time = {System.currentTimeMillis()};

                for (BookBean itemDatum : bookAdapter.getItemData()) {
                    if (name.equals(itemDatum.getBook_id())) {
                        BookUtil.GetCacheSize(itemDatum, (atomicLong, fileList, deficiencyList) -> requireActivity().runOnUiThread(() -> {
                            dialog.cancel();
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("打包方式")
                                    .setMessage("已缓存 " + fileList.size() + "张图片(大小:" + FileSizeUtil.ConvertFileSize(atomicLong.get()) + "),缺失 " + deficiencyList.size() + " 张图片\n提示：导出全部图片会自动下载未缓存图片，开始后无法取消")
                                    //.setCancelable(false)
                                    .setOnCancelListener(dialog1 -> BookUtil.cancel())
                                    .setPositiveButton("导出已缓存图片", (dialog2, which) -> BookUtil.imageToZip(itemDatum, false, (status, msg, progress, max) -> {
                                        if (System.currentTimeMillis() - time[0] < 100 && !status) return;
                                        time[0] = System.currentTimeMillis();
                                        NotificationUtil.sendProgressMessage(itemDatum.getTitle(),msg,progress,max);
                                    }))
                                    .setNegativeButton("导出全部图片", (dialog22, which) -> BookUtil.imageToZip(itemDatum, true, (status, msg, progress, max) -> {
                                        if (System.currentTimeMillis() - time[0] < 100 && !status) return;
                                        time[0] = System.currentTimeMillis();
                                        NotificationUtil.sendProgressMessage(itemDatum.getTitle(),msg,progress,max);
                                    }))
                                    .show();
                        }));
                        break;
                    }
                }


            } else if (integer == 2) {
                String[] ss = new String[tracker.getSelection().size()];
                int index = 0;
                for (String s : tracker.getSelection()) {
                    ss[index++] = s;
                }
                ArrayList<String> fileList = new ArrayList<>();
                arr_list.set(1, "删除书架记录并删除本地缓存文件(大小:计算中)");
                AlertDialog dialog = new AlertDialog.Builder(requireContext())
                        .setTitle("删除提示")
                        //.setMessage("确定删除书本？")
                        .setSingleChoiceItems(adapter, 0, null)
                        .setPositiveButton("确定", (dialogInterface, i) -> {
                            //为1时删除所以记录 为2仅清除缓存
                            if ((((AlertDialog) dialogInterface).getListView().getCheckedItemPosition()) > 0) {
                                CompletableFuture.runAsync(() -> {
                                    if ((((AlertDialog) dialogInterface).getListView().getCheckedItemPosition()) == 1) {
                                        bookrackViewModel.removeBooks(ss);
                                        bookrackViewModel.updateBooks();
                                        for (String value : ss) {
                                            FileUtil.deleteFolders(DiskCache.path + File.separator + "book" + File.separator + value);
                                        }
                                    } else {
                                        for (String value : ss) {
                                            FileUtil.deleteFolders(DiskCache.path + File.separator + "book" + File.separator + value + File.separator + "book_chapter");
                                        }
                                    }
                                    fileList.forEach(s -> new File(s).delete());
                                });
                            }
                        })
                        .setOnCancelListener(DialogInterface::dismiss)
                        .setNeutralButton("取消", null)
                        .setOnCancelListener(dialog1 -> BookUtil.cancel())
                        .create();
                dialog.show();

                ArrayList<BookBean> beans = new ArrayList<>();

                for (String s : ss) {
                    for (BookBean itemDatum : bookAdapter.getItemData()) {
                        if (itemDatum.getBook_id().equals(s)) {
                            beans.add(itemDatum);
                            break;
                        }
                    }
                }

                BookUtil.GetCacheSize(beans, (atomicLong, fileList1, deficiencyList) -> requireActivity().runOnUiThread(() -> {
                    fileList.addAll(fileList1);
                    arr_list.set(1, "删除书架记录并删除本地缓存文件(大小:" + FileSizeUtil.ConvertFileSize(atomicLong.get()) + ")");
                    adapter.notifyDataSetChanged();
                }));



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
        tracker.onSaveInstanceState(outState);
        outState.putParcelableArrayList("list", (ArrayList<? extends Parcelable>) bookAdapter.getItemData().clone());
        super.onSaveInstanceState(outState);
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