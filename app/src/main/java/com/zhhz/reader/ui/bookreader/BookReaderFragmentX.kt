package com.zhhz.reader.ui.bookreader

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import com.jeremyliao.liveeventbus.LiveEventBus
import com.zhhz.reader.bean.ContentBean
import com.zhhz.reader.databinding.FragmentBookreaderXBinding
import com.zhhz.reader.ui.book.EventBus
import com.zhhz.reader.ui.book.ReadBookConfig
import com.zhhz.reader.ui.book.ReadProvider
import com.zhhz.reader.ui.book.TextActionMenu
import com.zhhz.reader.ui.book.entities.TextChapter
import com.zhhz.reader.ui.book.entities.TextPage
import com.zhhz.reader.ui.book.postEvent
import com.zhhz.reader.util.Coroutine
import com.zhhz.reader.util.LogUtil
import com.zhhz.reader.view.XReadTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import java.math.BigDecimal
import java.math.RoundingMode

class BookReaderFragmentX : BookReaderFragmentBase(), XReadTextView.CallBack {
    private var binding: FragmentBookreaderXBinding? = null

    private val textActionMenu: TextActionMenu by lazy {
        TextActionMenu(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookreaderXBinding.inflate(inflater, container, false)

        val layoutParams = FrameLayout.LayoutParams(-2, -2)
        layoutParams.gravity = Gravity.CENTER
        binding!!.bookReader.addView(errorRetryButton, layoutParams)

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        mViewModel.fontSetting.observe(viewLifecycleOwner) { s: String? ->
            when (s) {
                "font_size_sub" -> {
                    ReadBookConfig.textSize -= 1
                }

                "font_size_add" -> {
                    ReadBookConfig.textSize += 1
                }

                "font_margin_sub" -> {
                    ReadBookConfig.marginSpacing -= 1
                }

                "font_margin_add" -> {
                    ReadBookConfig.marginSpacing += 1
                }

                "font_field_spacing_sub" -> {
                    ReadBookConfig.paragraphSpacing -= 1
                }

                "font_field_spacing_add" -> {
                    ReadBookConfig.paragraphSpacing += 1
                }

                "font_spacing_sub" -> {
                    ReadBookConfig.fontSpacing -= 1
                }

                "font_spacing_add" -> {
                    ReadBookConfig.fontSpacing += 1
                }

                "font_line_spacing_sub" -> {
                    val b: BigDecimal = BigDecimal.valueOf(ReadBookConfig.lineHeightRatio - 0.1)
                    ReadBookConfig.lineHeightRatio = b.setScale(1, RoundingMode.HALF_UP).toFloat()
                }

                "font_line_spacing_add" -> {
                    val b: BigDecimal = BigDecimal.valueOf(ReadBookConfig.lineHeightRatio + 0.1)
                    ReadBookConfig.lineHeightRatio = b.setScale(1, RoundingMode.HALF_UP).toFloat()
                }
            }
            mViewModel.saveSetting()
            ReadProvider.updateStyle()
            postEvent(EventBus.UPDATE_VIEW_SIZE, true)
        }

        mViewModel.dataContent.observe(viewLifecycleOwner) { contentBean: ContentBean ->
            Coroutine.async(context = Dispatchers.Main.immediate) {
                binding!!.progress.hide()
                if (!contentBean.isStatus) {
                    val textChapter =
                        ReadProvider.getTextChapter("文字加载失败", contentBean.error, 0)
                    binding!!.readerText.setContent(textChapter, mViewModel.pos)
                    errorRetryButton.visibility = View.VISIBLE
                } else {
                    //判断是否转跳到文本末尾
                    if (contentBean.previousPage) {
                        mViewModel.pos = (contentBean.data as String).length - 1
                    }
                    val textChapter = ReadProvider.getTextChapter(
                        mViewModel.chapters.value.let { it ?: "" },
                        contentBean.data as String,
                        0
                    )
                    binding!!.readerText.setContent(textChapter, mViewModel.pos)
                    saveProgress()
                }
            }.onError {
                LogUtil.error(it)
                binding!!.readerText.setContent(
                    TextChapter(
                        "加载失败",
                        0,
                        listOf(TextPage(text = it.message.let { "失败原因获取失败" }))
                    )
                )
            }
        }

        mViewModel.chapters.observe(viewLifecycleOwner) {
            binding!!.progress.show()
            binding!!.readerText.setContent(null)
        }

        LiveEventBus.get(EventBus.UPDATE_VIEW_SIZE, Boolean::class.java)
            .observe(viewLifecycleOwner) { value ->
                if (value) {
                    Coroutine.async(
                        executeContext = IO
                    ) {
                        binding!!.readerText.run {
                            val textChapter = ReadProvider.getTextChapter(
                                getTextChapter()!!.title,
                                getTextChapter()!!.getContent(),
                                0
                            )
                            binding!!.readerText.setContent(textChapter, getReadProgress())
                        }
                    }.onError {
                        LogUtil.error(it)
                    }
                }
            }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun updateSelectedStart(x: Float, y: Float, top: Float) {
        binding!!.run {
            cursorLeft.x = x - cursorLeft.width
            cursorLeft.y = y
            cursorLeft.visibility = View.VISIBLE
            textMenuPosition.x = x
            textMenuPosition.y = top
        }
    }

    override fun upSelectedEnd(x: Float, y: Float) {
        binding!!.run {
            cursorRight.x = x
            cursorRight.y = y
            cursorRight.visibility = View.VISIBLE
        }
    }

    override fun showTextActionMenu() {
        binding!!.run {
            textActionMenu.show(
                textMenuPosition,
                root.height,
                textMenuPosition.x.toInt(),
                textMenuPosition.y.toInt(),
                (cursorLeft.y + cursorLeft.height).toInt(),
                cursorRight.x.toInt(),
                (cursorRight.y + cursorRight.height).toInt()
            )
        }

    }

    override fun onCancelSelect() {
        binding!!.cursorLeft.visibility = View.INVISIBLE
        binding!!.cursorRight.visibility = View.INVISIBLE
        textActionMenu.dismiss()
    }

    override val headerHeight: Int
        get() = 0

    /**
     * 保存章节阅读进度
     */
    override fun saveProgress() {
        mViewModel.pos = binding!!.readerText.getReadProgress()
        mViewModel.saveProgress(mViewModel.progress, mViewModel.pos)
    }

    /**
     * 切换章节
     * @param i 0代表上一章，反之代表下一章
     */
    override fun switchChapter(i: Int) {
        //加载中时屏蔽切换章节
        if (mViewModel.isLoading) return
        if (i == 0) {
            if (mViewModel.isHavePreviousChapters) {
                mViewModel.pos = 0
                mViewModel.loadPreviousChapters()
            } else {
                Toast.makeText(requireContext(), "已经是第一章了", Toast.LENGTH_SHORT).show()
            }
        } else {
            if (mViewModel.isHaveNextChapters) {
                mViewModel.pos = 0
                mViewModel.loadNextChapters()
            } else {
                Toast.makeText(requireContext(), "已经没有下一章了", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun upPage() {
        binding!!.readerText.run {
            onSingleTapUp(width * 0.15f, height * 0.5f)
        }
    }

    override fun downPage() {
        binding!!.readerText.run {
            onSingleTapUp(width * 0.75f, height * 0.5f)
        }
    }

    override fun showBookMenu() {}

    companion object {
        fun newInstance(): BookReaderFragmentX {
            return BookReaderFragmentX()
        }
    }
}
