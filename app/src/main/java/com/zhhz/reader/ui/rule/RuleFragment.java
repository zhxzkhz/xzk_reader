package com.zhhz.reader.ui.rule;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.zhhz.reader.adapter.RuleAdapter;
import com.zhhz.reader.bean.RuleBean;
import com.zhhz.reader.databinding.FragmentRuleBinding;
import com.zhhz.reader.rule.RuleAnalysis;
import com.zhhz.reader.util.DiskCache;
import com.zhhz.reader.util.FileUtil;
import com.zhhz.reader.util.StringUtil;
import com.zhhz.reader.view.RecycleViewDivider;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class RuleFragment extends Fragment {

    ActivityResultLauncher<String> launch;
    private RuleViewModel ruleViewModel;
    private FragmentRuleBinding binding;
    private RuleAdapter ruleAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launch = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
            JSONObject content = JSONObject.parseObject(new String(FileUtil.readFile(requireContext(), result)));
            try {
                String s = DiskCache.path + File.separator + "config" + File.separator + "rule" + File.separator + content.getString("name") + ".json";
                if (!FileUtil.CopyFile(content.toString(), new File(s))) {
                    Toast.makeText(requireContext(), "文件导入异常", Toast.LENGTH_SHORT).show();
                    return;
                }
                RuleAnalysis rule = new RuleAnalysis(content, true);
                RuleBean ruleBean = new RuleBean();
                ruleBean.setId(StringUtil.getMD5(rule.getAnalysis().getName()));
                ruleBean.setName(rule.getAnalysis().getName());
                ruleBean.setFile(s);
                ruleBean.setComic(rule.getAnalysis().isComic());
                ruleBean.setOpen(true);
                ruleViewModel.saveRule(ruleBean);
                Toast.makeText(requireContext(), "导入成功", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                RuleAnalysis.analyses_map.remove(StringUtil.getMD5(content.getString("name")));
                Snackbar.make(binding.getRoot(), "导入失败，该文件不是规则文件", Snackbar.LENGTH_SHORT).setAction("查看详细", v -> new AlertDialog.Builder(requireContext())
                        .setTitle("错误提示")
                        .setMessage(e.getMessage())
                        .setOnCancelListener(DialogInterface::dismiss)
                        .show()).show();
                //Toast.makeText(requireContext(), "导入失败，该文件不是规则文件", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ruleViewModel = new ViewModelProvider(this).get(RuleViewModel.class);

        binding = FragmentRuleBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ruleAdapter = new RuleAdapter();
        ruleAdapter.setHasStableIds(true);
        //设置Item增加、移除动画
        //binding.rv.setItemAnimator(null);
        binding.rv.setLayoutManager(new LinearLayoutManager(getContext()));
        //固定高度
        binding.rv.setHasFixedSize(true);
        binding.rv.addItemDecoration(new RecycleViewDivider(this.getContext(), 1));
        binding.rv.setAdapter(ruleAdapter);

        ruleAdapter.setOnClickListener(view -> {
            if (view instanceof SwitchMaterial) {
                RuleBean ruleBean = Objects.requireNonNull(ruleViewModel.getRuleList().getValue()).get(binding.rv.getChildAdapterPosition(view));
                ruleBean.setOpen(((SwitchMaterial) view).isChecked());
                ruleViewModel.saveRule(ruleBean);
            }
        });

        ruleAdapter.setOnLongClickListener(view -> {
            RuleBean ruleBean = Objects.requireNonNull(ruleViewModel.getRuleList().getValue()).get(binding.rv.getChildAdapterPosition(view));
            new AlertDialog.Builder(requireContext())
                    .setTitle("删除提示")
                    .setMessage("确定删除书源 " + ruleBean.getName() + "?")
                    .setPositiveButton("确定", (dialogInterface, i) -> {
                        ruleViewModel.removeRule(ruleBean);
                    })
                    .setNeutralButton("取消", null)
                    .show();
            return true;
        });

        binding.ruleAdd.setOnClickListener(view -> launch.launch("*/*"));

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ruleViewModel.getRuleList().observe(getViewLifecycleOwner(), ruleBeans -> {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new MyDiffUtilCallback(ruleAdapter.getItemData(), ruleBeans));
            ruleAdapter.getItemData().clear();
            ruleAdapter.getItemData().addAll(ruleBeans);
            result.dispatchUpdatesTo(ruleAdapter);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    static class MyDiffUtilCallback extends DiffUtil.Callback {

        private final ArrayList<RuleBean> oldList;
        private final ArrayList<RuleBean> newList;

        public MyDiffUtilCallback(ArrayList<RuleBean> oldList, ArrayList<RuleBean> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).toString().equals(newList.get(newItemPosition).toString());
        }
    }


}