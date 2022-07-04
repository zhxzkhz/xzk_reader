package com.zhhz.reader.ui.rule;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.zhhz.reader.bean.RuleBean;
import com.zhhz.reader.sql.SQLiteUtil;

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
        mRuleList.setValue(SQLiteUtil.readRules());
    }

}