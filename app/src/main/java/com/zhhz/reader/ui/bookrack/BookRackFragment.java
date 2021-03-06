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
import com.zhhz.reader.util.GlideGetPath;
import com.zhhz.reader.util.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
                .setMessage("???????????????").create();

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
        //??????Item?????????????????????
        binding.rv.setItemAnimator(new DefaultItemAnimator());
        binding.rv.setLayoutManager(new GridLayoutManager(BookRackFragment.this.getContext(), 3, GridLayoutManager.VERTICAL, false));
        //????????????
        binding.rv.setHasFixedSize(true);
        binding.rv.setAdapter(bookAdapter);


        //??????????????????
        binding.searchView.setOnClickListener(view -> {
            Intent intent = new Intent(BookRackFragment.this.getContext(), SearchActivity.class);
            //startActivity(intent);
            launcher.launch(intent);
            BookRackFragment.this.requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        binding.refreshLayout.setOnRefreshListener(refreshLayout -> bookrackViewModel.updateCatalogue());

        //????????????
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
                    //????????????????????????
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

        //????????????????????????
        bookrackViewModel.getCallback().observe(getViewLifecycleOwner(), aBoolean -> {
            String s;
            if (aBoolean) {
                s = "??????";
            } else {
                s = "??????";
            }
            alertDialog.hide();
            Toast.makeText(requireContext(), "????????????" + s, Toast.LENGTH_SHORT).show();
        });

        //??????????????????????????????
        bookrackViewModel.getData().observe(getViewLifecycleOwner(), list -> {
            lists.clear();
            for (BookBean bookBean : list) {
                lists.add(bookBean.getBook_id());
            }

            bookAdapter.setItemData(list);
            bookAdapter.notifyDataSetChanged();
            //bookrackViewModel.updateCatalogue();
        });

        //??????????????????
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
        ArrayList<String> arr_list = new ArrayList<>(Arrays.asList("?????????????????????", "?????????????????????????????????????????????(??????:?????????)"));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), a.getResourceId(com.google.android.material.R.styleable.AlertDialog_singleChoiceItemLayout, 0), arr_list);
        a.recycle();

        //??????????????????
        bookrackViewModel.getOperation().observe(getViewLifecycleOwner(), integer -> {
            if (integer == 2) {
                String[] ss = new String[tracker.getSelection().size()];
                int index = 0;
                for (String s : tracker.getSelection()) {
                    ss[index++] = s;
                }

                arr_list.set(1, "?????????????????????????????????????????????(??????:?????????)");
                AlertDialog dialog = new AlertDialog.Builder(requireContext())
                        .setTitle("????????????")
                        //.setMessage("?????????????????????")
                        .setSingleChoiceItems(adapter, 0, null)
                        .setPositiveButton("??????", (dialogInterface, i) -> {
                            //System.out.println("((AlertDialog) dialogInterface).getListView().getCheckedItemPosition() = " + ((AlertDialog) dialogInterface).getListView().getCheckedItemPosition());
                            bookrackViewModel.removeBooks(ss);
                            bookrackViewModel.updateBooks();
                        })
                        .setOnCancelListener(DialogInterface::dismiss)
                        .setNeutralButton("??????", null)
                        .create();
                dialog.show();

                //??????????????????????????????
                new Thread(() -> {
                    try {
                        GlideGetPath.init();
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }

                    final long[] count_size = {0};
                    for (String s : ss) {
                        BookBean bean = null;
                        for (BookBean itemDatum : bookAdapter.getItemData()) {
                            if (s.equals(itemDatum.getBook_id())) {
                                bean = itemDatum;
                                break;
                            }
                        }
                        String path = DiskCache.path + File.separator + "book" + File.separator + s;
                        count_size[0] += FileSizeUtil.getFileOrFilesSize(path, FileSizeUtil.SIZE_TYPE_B);
                        if (bean != null && bean.getBook_id().equals(StringUtil.getMD5(bean.getTitle() + "??????true??????" + bean.getAuthor()))) {
                            File file = new File(path + File.separator + "book_chapter");
                            if (file.isDirectory()) {
                                for (File listFile : Objects.requireNonNull(file.listFiles())) {
                                    try {
                                        FileReader fileReader = new FileReader(listFile);
                                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                                        bufferedReader.lines().forEach(s1 -> {
                                            File f = GlideGetPath.getCacheFile(s1);
                                            if (f != null) {
                                                count_size[0] += FileSizeUtil.getFileOrFilesSize(f.getPath(), 1);
                                            }
                                        });
                                        bufferedReader.close();
                                        fileReader.close();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }
                    }

                    requireActivity().runOnUiThread(() -> {
                        arr_list.set(1, "?????????????????????????????????????????????(??????:" + FileSizeUtil.ConvertFileSize(count_size[0]) + ")");
                        adapter.notifyDataSetChanged();
                    });
                });//.start();

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