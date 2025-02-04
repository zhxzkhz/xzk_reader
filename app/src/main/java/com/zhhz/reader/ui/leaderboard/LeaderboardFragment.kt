package com.zhhz.reader.ui.leaderboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import cn.hutool.core.codec.Base64
import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.JSONObject
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.zhhz.reader.R
import com.zhhz.reader.activity.LeaderboardResultActivity
import com.zhhz.reader.adapter.DrawerAdapter
import com.zhhz.reader.bean.DrawerItem
import com.zhhz.reader.databinding.FragmentLeaderboardBinding
import com.zhhz.reader.rule.RuleAnalysis
import com.zhhz.reader.util.DiskCache
import javax.script.ScriptException
import javax.script.SimpleBindings

class LeaderboardFragment : Fragment() {

    private lateinit var launcher: ActivityResultLauncher<Intent>

    private val viewModel: LeaderboardViewModel by viewModels()

    private lateinit var binding: FragmentLeaderboardBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
        // 配置 FlexboxLayoutManager（垂直方向，自动换行）
        val layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            alignItems = AlignItems.FLEX_START
        }
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.itemAnimator = null

        val items = mutableListOf<DrawerItem>()
        RuleAnalysis.analyses_map.entries.forEach {
            val u = it.value.json.leaderboard.url
            val rule = if (u.contains("^js@|@js:".toRegex())) {
                try {
                    val bindings = SimpleBindings()
                    bindings["xlua_rule"] = this
                    bindings["java"] = this
                    bindings["url"] = u

                    it.value.jsToJavaObject(
                        DiskCache.SCRIPT_ENGINE.eval(
                            Base64.decodeStr(u.split("js@|@js:".toRegex())[1]),
                            bindings
                        )
                    )
                } catch (e: ScriptException) {
                    e.printStackTrace()
                    "{}"
                }
            } else {
                it.value.json.leaderboard.url
            }.let {t ->
                JSONArray.parse(t).map { a -> a as JSONObject }
            }
            items.add(DrawerItem(it.value.name, rule,it.key))
        }

        val drawerAdapter = DrawerAdapter(items)
        drawerAdapter.clickListener = View.OnClickListener {
            val (url,tag) = (it.getTag(R.id.tv_title) as Pair<*, *>)
            val intent = Intent(requireContext(), LeaderboardResultActivity::class.java)
            intent.putExtra("url", url as String)
            intent.putExtra("tag", tag as String)
            intent.putExtra("title", (it as TextView).text)
            launcher.launch(intent)
        }
        binding.recyclerView.adapter = drawerAdapter

        return binding.getRoot()
    }
}