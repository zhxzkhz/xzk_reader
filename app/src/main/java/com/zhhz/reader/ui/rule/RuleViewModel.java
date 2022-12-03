package com.zhhz.reader.ui.rule;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zhhz.reader.bean.RuleBean;
import com.zhhz.reader.rule.RuleAnalysis;
import com.zhhz.reader.sql.SQLiteUtil;
import com.zhhz.reader.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;

public class RuleViewModel extends ViewModel {

    private final MutableLiveData<ArrayList<RuleBean>> mRuleList;

    public RuleViewModel() {
        mRuleList = new MutableLiveData<>();
        mRuleList.setValue(SQLiteUtil.readRules());
    }

    public MutableLiveData<ArrayList<RuleBean>> getRuleList() {
        return mRuleList;
    }

    public void saveRule(RuleBean bean) {
        SQLiteUtil.saveRule(bean);
        if (bean.isOpen()) {
            try {
                new RuleAnalysis(bean.getFile(), true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            RuleAnalysis.analyses_map.remove(StringUtil.getMD5(bean.getName()));
        }
        mRuleList.setValue(SQLiteUtil.readRules());
    }

    public void removeRule(RuleBean bean){
        SQLiteUtil.removeRules(new String[]{bean.getId()});
        if (bean.isOpen()) {
            RuleAnalysis.analyses_map.remove(StringUtil.getMD5(bean.getName()));
        }
        mRuleList.setValue(SQLiteUtil.readRules());
    }

}