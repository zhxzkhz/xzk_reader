package com.zhhz.reader.ui.leaderboardresult

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zhhz.reader.bean.LeaderboardResultBean
import com.zhhz.reader.rule.RuleAnalysis

class LeaderboardViewModel : ViewModel() {


    private val data: MutableLiveData<ArrayList<LeaderboardResultBean>> = MutableLiveData()

    private var page = 1

    private var leaderboardUrl = ""

    private var tag = ""

    fun getPage(): Int {
        return page
    }

    fun setPage(page: Int) {
        this.page = page
    }

    fun nextPage() {
        page++
        searchBook(leaderboardUrl,tag)
    }


    fun searchBook(url: String, tag: String) {
        this.tag = tag
        this.leaderboardUrl = url
        RuleAnalysis.analyses_map[tag]?.bookLeaderboard(url,page ,  {
            data.postValue(it)
        }, tag);
    }


    fun getData(): MutableLiveData<ArrayList<LeaderboardResultBean>> {
        return data
    }
}