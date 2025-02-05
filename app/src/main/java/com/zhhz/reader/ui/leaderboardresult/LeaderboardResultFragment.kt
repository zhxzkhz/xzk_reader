package com.zhhz.reader.ui.leaderboardresult

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.zhhz.reader.activity.DetailedActivity
import com.zhhz.reader.adapter.ResultListAdapter
import com.zhhz.reader.bean.LeaderboardResultBean
import com.zhhz.reader.databinding.FragmentLeaderboardResultBinding
import com.zhhz.reader.view.RecycleViewDivider

class LeaderboardResultFragment : Fragment() {

    private lateinit var binding: FragmentLeaderboardResultBinding

    private val mViewModel: LeaderboardViewModel by viewModels()

    private var leaderboardResultFragment: LeaderboardResultFragment? = null

    private lateinit var resultAdapter: ResultListAdapter

    private lateinit var launcher: ActivityResultLauncher<Intent>

    fun getInstance(): LeaderboardResultFragment {
        if (leaderboardResultFragment == null) {
            leaderboardResultFragment = LeaderboardResultFragment()
        }
        return LeaderboardResultFragment().also { leaderboardResultFragment = it }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLeaderboardResultBinding.inflate(inflater, container, false)

        val title = requireActivity().intent.extras?.getString("title")

        binding.appCompatTextView.text = title

        binding.back.setOnClickListener { requireActivity().finish() }

        //禁用下拉刷新，上拉加载
        binding.refreshLayout.setEnableRefresh(false)
        binding.refreshLayout.setEnableLoadMore(true)
        binding.refreshLayout.setRefreshFooter(ClassicsFooter(requireContext())) //设置Footer
        binding.refreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        //加载下一页
        binding.refreshLayout.setOnLoadMoreListener { mViewModel.nextPage() }

        resultAdapter = ResultListAdapter(requireContext())
        resultAdapter.setHasStableIds(true)
        //设置Item增加、移除动画
        binding.resultList.setItemAnimator(DefaultItemAnimator())
        binding.resultList.setLayoutManager(LinearLayoutManager(requireContext()))
        //固定高度
        binding.resultList.setHasFixedSize(true)
        binding.resultList.addItemDecoration(RecycleViewDivider(requireContext(), 1))
        binding.resultList.setAdapter(resultAdapter)

        resultAdapter.setOnClickListener { view: View ->
            val intent = Intent(requireContext(), DetailedActivity::class.java)
            //获取点击事件位置
            val position: Int = binding.resultList.getChildAdapterPosition(view)
            intent.putExtra("book", resultAdapter.itemData[position])
            launcher.launch(intent)
        }


        return binding.root
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val url = requireActivity().intent.extras?.getString("url").toString()
        val tag = requireActivity().intent.extras?.getString("tag").toString()

        //监听搜索完成
        mViewModel.getData().observe(
            viewLifecycleOwner
        ) { list: ArrayList<LeaderboardResultBean>? ->
            binding.progress.visibility = View.GONE
            binding.refreshLayout.finishLoadMore()
            if (list == null) {
                resultAdapter.itemData.clear()
                resultAdapter.notifyDataSetChanged()
                //失败也会显示，等后续优化
                binding.progress.visibility = View.VISIBLE
            } else {
                val size = resultAdapter.itemData.size
                resultAdapter.itemData.addAll(list)
                resultAdapter.notifyItemRangeInserted(size, list.size)
            }
        }

        mViewModel.searchBook(url, tag)


    }


    override fun onDestroy() {
        super.onDestroy()
        leaderboardResultFragment = null
    }


}