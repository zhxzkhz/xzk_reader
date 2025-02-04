package com.zhhz.reader.ui.book

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.view.SupportMenuInflater
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuItemImpl
import com.zhhz.reader.R
import com.zhhz.reader.adapter.SelectTextAdapter
import com.zhhz.reader.databinding.PopupActionMenuBinding

@SuppressLint("RestrictedApi")
class TextActionMenu(private val context: Context/*, private val callBack: CallBack*/) :
    PopupWindow(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT) {

    private val binding = PopupActionMenuBinding.inflate(LayoutInflater.from(context))
    private val adapter = SelectTextAdapter().apply {
        setHasStableIds(true)
    }
    private val menuItems: List<MenuItemImpl>
    private val visibleMenuItems = arrayListOf<MenuItemImpl>()

    init {
        @SuppressLint("InflateParams")
        contentView = binding.root
        isTouchable = true
        isOutsideTouchable = false
        isFocusable = false

        val myMenu = MenuBuilder(context)
        val otherMenu = MenuBuilder(context)
        onInitializeMenu(otherMenu)
        SupportMenuInflater(context).inflate(R.menu.content_select_action, myMenu)
        menuItems = myMenu.visibleItems + otherMenu.visibleItems
        visibleMenuItems.addAll(menuItems)
        adapter.setItemData(visibleMenuItems)
        binding.recyclerView.adapter = adapter

    }


    fun show(
        view: View,
        windowHeight: Int,
        startX: Int,
        startTopY: Int,
        startBottomY: Int,
        endX: Int,
        endBottomY: Int
    ) {
        contentView.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED,
        )
        val popupHeight = contentView.measuredHeight

        when {
            startBottomY > 500 -> {
                showAtLocation(
                    view,
                    Gravity.TOP or Gravity.START,
                    startX,
                    startTopY - popupHeight
                )
            }
            endBottomY - startBottomY > 500 -> {
                showAtLocation(
                    view,
                    Gravity.TOP or Gravity.START,
                    startX,
                    startBottomY
                )
            }
            else -> {
                showAtLocation(
                    view,
                    Gravity.TOP or Gravity.START,
                    endX,
                    endBottomY
                )
            }
        }
    }

    private fun createProcessTextIntent(): Intent {
        return Intent()
            .setAction(Intent.ACTION_PROCESS_TEXT)
            .setType("text/plain")
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun getSupportedActivities(): List<ResolveInfo> {
        return context.packageManager
            .queryIntentActivities(createProcessTextIntent(), 0)
    }

    private fun createProcessTextIntentForResolveInfo(info: ResolveInfo): Intent {
        return createProcessTextIntent()
            .putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false)
            .setClassName(info.activityInfo.packageName, info.activityInfo.name)
    }

    /**
     * Start with a menu Item order value that is high enough
     * so that your "PROCESS_TEXT" menu items appear after the
     * standard selection menu items like Cut, Copy, Paste.
     */
    private fun onInitializeMenu(menu: Menu) {
        kotlin.runCatching {
            var menuItemOrder = 100
            for (resolveInfo in getSupportedActivities()) {
                menu.add(
                    Menu.NONE, Menu.NONE,
                    menuItemOrder++, resolveInfo.loadLabel(context.packageManager)
                ).intent = createProcessTextIntentForResolveInfo(resolveInfo)
            }
        }.onFailure {
            Toast.makeText(context, "获取文字操作菜单出错:${it.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    interface CallBack {
        val selectedText: String

        fun onMenuItemSelected(itemId: Int): Boolean

        fun onMenuActionFinally()
    }

}