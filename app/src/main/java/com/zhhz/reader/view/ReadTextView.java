package com.zhhz.reader.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.icu.text.DecimalFormat;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class ReadTextView extends View {

    private final Handler hd = new Handler();
    //每页索引
    @SuppressLint("UseSparseArrays")
    private final LinkedHashMap<Integer, Integer> map = new LinkedHashMap<>();
    //画笔
    private final TextPaint textPaint;
    //标题底部画笔
    private final TextPaint ttPaint;
    //测量线画笔
    private final Paint pt;
    //用于存储 newDraw 下的每行字间距平摊间距
    private final HashMap<Integer, Float> wordSpaceMap = new HashMap<>();
    //float density
    final float density;
    CallBack downOnClick;
    CallBack upOnClick;
    CallBack menuOnClick;
    CallBack updateCallBack;
    final DecimalFormat format1 = new DecimalFormat("##%");
    //一行字体位置数组
    final float[] line_font = new float[200];
    //顶部标题间距
    private float topSpace = 45f;
    //底部信息间距
    private float bottomSpace = 45f;
    //底部和顶部空间倍率
    private float spaceRatio = 1.8f;
    //设置文本大小
    private int textSize = 18;
    //开始绘制文字位置
    private int textStart = 0;
    //绘制文字结束位置
    private int textEnd = 0;
    //左右边缘间距
    private float marginSpacing = 45f;
    //字间距
    private float fontSpacing = 1f;
    //字间距倍率
    private float fontSpacingRatio = 1.05f;
    //上下边缘间距
    private float lineSpacing = 15f;
    //行高
    private float lineHeight = 10f;
    //行高，倍率
    private float lineHeightRatio = 1.2f;
    //是否首行缩进
    private boolean indentation = true;
    //宽度是否自动对齐
    private boolean widthAlign = true;
    //是否使用绘制一行，设置后 字间距失效
    private boolean newDraw = false;
    //状态栏高度
    private int statusBar = 0;
    //绘制测试线判断
    private boolean Test = false;
    private int maxLine = 0;
    private String title = "";
    private String text = "";
    //每页显示最大行数
    private int pageMaxLine = 0;
    private int color = Color.BLACK;
    private float[] font_x;
    private final Runnable run = ReadTextView.this::AnalyseTextLine;
    private final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            if (event.getX() > getWidth() / 3f * 2 && downOnClick != null) {
                if (down_page()) return true;
            } else if (event.getX() < getWidth() / 3f && upOnClick != null) {
                if (up_page()) return true;
            } else {
                if (menuOnClick != null) menuOnClick.onClick();
            }
            return true;
        }
    });

    public ReadTextView(Context context) {
        this(context, null);
    }

    public ReadTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReadTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        density = getResources().getDisplayMetrics().density;
        textSize = (int) (textSize * density);
        pt = new Paint();
        pt.setStrokeWidth(1);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(color);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setStrokeWidth(0);
        textPaint.setSubpixelText(true);
        textPaint.setTextSize(textSize);
        //textPaint.density = density;

        ttPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        ttPaint.setColor(color);
        ttPaint.setStyle(Paint.Style.FILL);
        ttPaint.setStrokeWidth(0);
        ttPaint.setSubpixelText(true);
        // 字体大小是正文的 x ^ 2 / - x / 12
        ttPaint.setTextSize((float) (Math.sqrt(Math.pow(textSize, 2) / 2f) - textSize / 12f));
        //ttPaint.density = density;

    }

    public boolean down_page() {
        if (textEnd >= text.length()) {
            if (!downOnClick.onClick()) return true;
        }
        textStart = textEnd;
        updateCallBack.onClick();
        invalidate();
        return false;
    }

    public boolean up_page() {
        int posIndex = 0;
        //等于0代表是上一章，
        if (textStart == 0) {
            if (upOnClick.onClick()) {
                int posIndex1 = maxLine / pageMaxLine;
                //获取能显示完整行数的页面数
                posIndex = maxLine - posIndex1 * pageMaxLine;
                if (posIndex == 0) {
                    posIndex = maxLine - pageMaxLine + 1;
                } else {
                    posIndex = posIndex1 * pageMaxLine + 1;
                }
            } else {
                return true;
            }
        } else {
            //判断统计行数没，如果没有，则先统计
            if (maxLine < 1) {
                hd.removeCallbacks(run);
                AnalyseTextLine();
            }
            for (Map.Entry<Integer, Integer> value : map.entrySet()) {
                if (value.getValue() == textStart) {
                    posIndex = value.getKey() - pageMaxLine;
                    break;
                }
            }
        }

        if (map.containsKey(posIndex)) {
            //noinspection ConstantConditions
            textStart = map.get(posIndex);
        } else {
            Toast.makeText(getContext(), "上一页加载失败", Toast.LENGTH_SHORT).show();
        }
        updateCallBack.onClick();
        invalidate();
        return false;
    }

    public boolean isNewDraw() {
        return newDraw;
    }

    //有问题，先屏蔽
    public void setNewDraw(boolean newDraw) {
        this.newDraw = newDraw;
        if (newDraw) {
            this.fontSpacing = 0;
            AnalyseTextLine();
        }
        invalidate();
    }

    public boolean isIndentation() {
        return indentation;
    }

    public void setIndentation(boolean indentation) {
        this.indentation = indentation;
    }

    public int getStatusBar() {
        return statusBar;
    }

    public void setStatusBar(int statusBar) {
        this.statusBar = statusBar;
        if (text.length() == 0) return;
        invalidate();
    }

    public boolean isWidthAlign() {
        return widthAlign;
    }

    public void setWidthAlign(boolean widthAlign) {
        this.widthAlign = widthAlign;
        if (text.length() == 0) return;
        AnalyseTextLine();
        invalidate();
    }

    public float getSpaceRatio() {
        return spaceRatio;
    }

    public void setSpaceRatio(float spaceRatio) {
        this.spaceRatio = spaceRatio;
        if (text.length() == 0) return;
        hd.removeCallbacks(run);
        run.run();
        invalidate();
    }

    public boolean isTest() {
        return Test;
    }

    public void setTest(boolean test) {
        Test = test;
        if (text.length() == 0) return;
        invalidate();
    }

    public int getTextStart() {
        return textStart;
    }

    public void setTextStart(int start) {
        textStart = start;
    }

    public int getTextEnd() {
        return textEnd;
    }

    public float getFontSpacingRatio() {
        return fontSpacingRatio;
    }

    public void setFontSpacingRatio(float fontSpacingRatio) {
        this.fontSpacingRatio = fontSpacingRatio;
        if (text.length() == 0) return;
        hd.removeCallbacks(run);
        AnalyseTextLine();
        invalidate();
    }

    public float getFontSpacing() {
        return fontSpacing;
    }

    public void setFontSpacing(float fontSpacing) {
        if (newDraw) {
            this.fontSpacing = 0;
        } else {
            this.fontSpacing = fontSpacing;
            if (text.length() == 0) return;
            hd.removeCallbacks(run);
            AnalyseTextLine();
            invalidate();
        }
    }

    public float getLineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(float lineSpacing) {
        this.lineSpacing = lineSpacing;
        if (text.length() == 0) return;
        invalidate();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        if (text.length() == 0) return;
        invalidate();
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        textPaint.setTextSize(textSize * density);
        ttPaint.setTextSize((float) (Math.sqrt(Math.pow(textSize * density, 2) / 2f) - textSize / 12f));
        if (text.length() == 0) return;
        hd.removeCallbacks(run);
        AnalyseTextLine();
        invalidate();
    }

    public float getMarginSpacing() {
        return marginSpacing;
    }

    //左右边缘间距
    public void setMarginSpacing(float marginSpacing) {
        this.marginSpacing = marginSpacing;
        if (text.length() == 0) return;
        hd.removeCallbacks(run);
        AnalyseTextLine();
        invalidate();
    }

    public float getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(float lineHeight) {
        this.lineHeight = lineHeight;
        if (text.length() == 0) return;
        invalidate();
    }

    public float getLineHeightRatio() {
        return lineHeightRatio;
    }

    public void setLineHeightRatio(float lineHeightRatio) {
        this.lineHeightRatio = lineHeightRatio;
        if (text.length() == 0) return;
        invalidate();
    }

    public String getText() {
        return text;
    }

    public void setText(@Nullable String text) {
        setText(text, 0);
    }

    public void setText(@Nullable String text, int progress) {
        if (text == null || text.length() == 0) {
            this.text = "";
            textStart = 0;
            invalidate();
            return;
        }

        //如果首行缩进就取消行前面空格
        if (indentation){
            text = text.replaceAll("^[\u3000\u0020]*|[\u3000\u0020]*$", "").replaceAll("\n+[\u3000\u0020]{2}","\n").replaceAll("\n+","\n");
        }

        if (progress >= text.length()){
            progress = 0;
        }

        textStart = progress;
        if (textStart >= text.length() - 1) textStart = text.length();
        maxLine = 0;
        textEnd = textStart;
        if (!this.text.equals(text)) {
            this.text = text;
            hd.removeCallbacks(run);
            if (getWidth() > 0) {
                AnalyseTextLine();
                //长度大于文本大小时自动矫正为最后一页
                if (textStart >= text.length()) {
                    int y = maxLine % pageMaxLine;
                    y = y == 0 ? pageMaxLine : y;
                    textStart = map.get(maxLine - y + 1);
                }

                invalidate();
            } else {
                String finalText = text;
                post(() -> {
                    AnalyseTextLine();
                    if (textStart >= finalText.length()) {
                        int y = maxLine % pageMaxLine;
                        y = y == 0 ? pageMaxLine : y;
                        textStart = map.get(maxLine - y + 1);
                    }
                    invalidate();
                });
            }
        }

    }

    //设置字体样式
    public boolean setFontStyle(String path) {
        ///storage/self/primary/Android/kitty.ttf
        File file = new File(path);
        if (!file.isFile()) {
            return false;
        }
        Typeface tf = Typeface.createFromFile(path);
        textPaint.setTypeface(tf);
        ttPaint.setTypeface(tf);
        if (text.length() == 0) return true;
        hd.removeCallbacks(run);
        hd.post(run);
        return true;
    }

    public int getColor() {
        return color;
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
        textPaint.setColor(color);
        if (text.length() == 0) return;
        invalidate();
    }

    public void setTitleColor(@ColorInt int color) {
        ttPaint.setColor(color);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public void setDownPage(CallBack func) {
        downOnClick = func;
    }

    public void setUpPage(CallBack func) {
        upOnClick = func;
    }

    public void setMenuClick(CallBack func) {
        menuOnClick = func;
    }

    public void setUpdateCallBack(CallBack func) {
        updateCallBack = func;
    }

    //用于测量一章有多少行
    public void AnalyseTextLine() {
        if (text.length() < 1) {
            return;
        }

        long times = System.currentTimeMillis();
        //创建缓存字体位置数组
        font_x = new float[text.length() + 1];
        //清空上次记录
        map.clear();
        wordSpaceMap.clear();

        textPaint.setLetterSpacing(newDraw ? fontSpacingRatio - 1 : 0);

        //每页最大行数计算
        CheckPageMaxLine();

        //使用控件宽高
        float widthPixels = getWidth() - marginSpacing * 2;

        if (widthPixels < textSize * fontSpacingRatio + fontSpacing) {
            return;
        }

        //保证只允许一次
        boolean bool = false;

        //使用临时变量存储绘制时的位置
        int tmp_index = 0;

        int fontNumber;

        //行数索引
        int lineIndex = 0;

        //临时储存一个文字
        String textStr = "";

        //临时值，用于首行缩进
        boolean indentation_a = indentation;

        //首行缩进宽度
        float indentation_width = 0;

        while (true) {
            float minWidth = 0;
            if (indentation_a) {
                minWidth = textPaint.measureText("赢") * 2;
                indentation_width = minWidth;
                indentation_a = false;
            }

            boolean skip = false;

            int fontIndex = 0;

            float font_width = 0;

            //计算一行位置，如果没遇到换行，首行缩进不触发
            while (minWidth < widthPixels) {

                fontIndex++;

                if (tmp_index + fontIndex > text.length()) {
                    skip = true;
                    break;
                }

                textStr = text.substring(tmp_index + fontIndex - 1, tmp_index + fontIndex);
                //System.out.println( textStr + " ->" + (tts + fontIndex - 1) +" -> minWidth = " + minWidth);
                font_x[tmp_index + fontIndex - 1] = minWidth;

                font_width = textPaint.measureText(textStr);
                if (textStr.equals("\n")) {
                    font_width = 0;
                    //修复首行缩进绘制时的判断问题，当换行的宽度小于首行缩进时出现问题
                    if (minWidth < indentation_width) {
                        font_x[tmp_index + fontIndex - 1] = indentation_width;
                    }
                } else if (newDraw) {
                    minWidth = minWidth + font_width;
                } else {
                    minWidth = minWidth + font_width * fontSpacingRatio + fontSpacing;
                }

                if (minWidth > widthPixels) {
                    if (newDraw && widthAlign) {
                        wordSpaceMap.put(lineIndex, (widthPixels - (minWidth - font_width)) / font_width / (fontIndex - 1));
                    }
                    fontIndex--;
                    indentation_a = false;
                    break;
                }

                if (textStr.equals("\n")) {
                    minWidth = widthPixels;
                    indentation_a = indentation;
                    /*if (tmp_index + fontIndex + 1 <= text.length() && text.substring(tmp_index + fontIndex, tmp_index + fontIndex + 1).equals("\n")) {
                        fontIndex++;
                        font_x[tmp_index + fontIndex - 1] = minWidth;
                    } else {
                        minWidth = widthPixels;
                        indentation_a = indentation;
                    }*/
                }

            }

            if (widthAlign && !textStr.equals("\n") && !skip) {
                float wordSpace = 0f;
                float x;
                if (tmp_index + fontIndex < font_x.length) {
                    x = (font_x[tmp_index + fontIndex] - font_width * (fontSpacingRatio - 1) - fontSpacing);
                    if (x > 0) {
                        wordSpace = (widthPixels - x) / (fontIndex - 1);
                    }
                } else {
                    x = (font_x[tmp_index + fontIndex - 1] - font_width * (fontSpacingRatio - 1) - fontSpacing);
                    if (x > 0) {
                        wordSpace = (widthPixels - x) / (fontIndex - 2);
                    }
                }
                for (int i = 1; i < fontIndex; i++) {
                    font_x[tmp_index + i] = font_x[tmp_index + i] + wordSpace * i;
                }
            }

            lineIndex++;

            //字体大小改变时会造成位置发生改变，这个用于转换位置
            if (tmp_index >= textStart && !bool && textStart > 0 && lineIndex % pageMaxLine == 1) {
                bool = true;
                textStart = tmp_index;
            }
            map.put(lineIndex, tmp_index);

            fontNumber = fontIndex;

            tmp_index = tmp_index + fontNumber;

            if (skip) {
                Log.i("计算时间", String.valueOf(System.currentTimeMillis() - times));
                break;
            }

        }
        maxLine = lineIndex;
        if (pageMaxLine >= maxLine)
            textStart = 0;

        //Log.i("最大行数", String.valueOf(maxLine));
    }

    private int getPageMaxLine() {
        return pageMaxLine;
    }

    private void CheckPageMaxLine() {
        //获取测量标准线
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        //字体最终高度
        //字体高
        float fontHeight = fm.descent - fm.ascent;

        // 字体高度 * 倍数 = 字体行间隔距离
        // 字体行间隔距离 + 行间隔 = 最终行间隔距离
        float finalHeight = fontHeight * lineHeightRatio + lineHeight;

        float heightPixels = getHeight() - lineSpacing * 2 - topSpace - bottomSpace - statusBar;
        // 画布高度 ÷ 最终行间隔距离 = 实际行数
        pageMaxLine = (int) ((heightPixels) / (finalHeight));
        //return pageMaxLine;
    }

    //Path path = new Path();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint.FontMetrics fm1 = ttPaint.getFontMetrics();
        float fontHeight1 = fm1.descent - fm1.ascent;

        topSpace = fontHeight1 * spaceRatio;
        bottomSpace = fontHeight1 * spaceRatio;

        canvas.drawText(title, marginSpacing * 1.8f, fontHeight1 * 1.2f + statusBar, ttPaint);

        if (textStart >= text.length() || text.length() == 0 || font_x == null) return;

        //使用控件高
        float heightPixels = getHeight() - lineSpacing * 2 - topSpace - bottomSpace - statusBar;
        //获取测量标准线
        Paint.FontMetrics fm = textPaint.getFontMetrics();

        //字体最终高度
        //字体高
        float fontHeight = fm.descent - fm.ascent;

        // 字体高度 * 倍数 = 字体行间隔距离
        // 字体行间隔距离 + 行间隔 = 最终行间隔距离
        float finalHeight = fontHeight * lineHeightRatio + lineHeight;
        // 画布高度 ÷ 最终行间隔距离 = 实际行数
        pageMaxLine = (int) ((heightPixels) / (finalHeight));

        int line = 0;
        //字体、高度变化时更新行数,如果不是当前页第一行，校验为当前页第一行
        if (textStart > 0) {
            for (Map.Entry<Integer, Integer> value : map.entrySet()) {
                line = value.getKey();
                if (value.getValue() >= textStart || line == map.size()) {
                    if (line <= pageMaxLine) {
                        textStart = 0;
                        line = 0;
                        break;
                    }
                    while ((line % pageMaxLine) != 1) {
                        line--;
                    }
                    //noinspection ConstantConditions
                    textStart = map.get(line);
                    break;
                }
            }
        }

        int indexLine = 0;

        //使用临时变量存储绘制时的位置
        int tts = textStart;

        //行数索引采用的一，所以行数减一
        //行间距平摊高度
        float lineHeights = (heightPixels - pageMaxLine * finalHeight + lineHeight + fontHeight * (lineHeightRatio - 1)) / (pageMaxLine - 1);

        // 字体高度 + 行间距平摊高度 = 字体绘制高度
        finalHeight = finalHeight + lineHeights;

        //绘制第一行时要减 top - ascent 的距离
        float topSpacing = finalHeight + fm.ascent;

        while (indexLine < pageMaxLine) {
            //文字位置
            int index = 1;
            float x = font_x[tts];
            while ((index + tts + 1) < font_x.length && x < font_x[index + tts]) {
                line_font[index - 1] = x;
                x = font_x[index + tts];
                index++;
            }
            line_font[index - 1] = x;

            if (newDraw) {
                if (wordSpaceMap.containsKey(line + indexLine)) {
                    textPaint.setLetterSpacing(fontSpacingRatio - 1 + wordSpaceMap.get(line + indexLine));
                } else {
                    textPaint.setLetterSpacing(fontSpacingRatio - 1);
                }
                canvas.drawText(String.valueOf(line + indexLine), 2, finalHeight * (indexLine + 1) - topSpacing + lineSpacing + topSpace + statusBar, ttPaint);

                canvas.drawText(text.substring(tts, tts + index), marginSpacing, finalHeight * (indexLine + 1) - topSpacing + lineSpacing + topSpace + statusBar, textPaint);

                textPaint.setLetterSpacing(0);
            } else {
                // path.moveTo(marginSpacing,finalHeight * (indexLine + 1) - topSpacing + lineSpacing + topSpace + statusBar);
                for (int s = 0; s < index; s++) {
                    //path.lineTo( marginSpacing + line_font[s],finalHeight * (indexLine + 1) - topSpacing + lineSpacing + topSpace + statusBar);
                    canvas.drawText(text.substring(tts + s, tts + s + 1), marginSpacing + line_font[s], finalHeight * (indexLine + 1) - topSpacing + lineSpacing + topSpace + statusBar, textPaint);
                }
                // canvas.drawTextOnPath(text.substring(tts, tts + index),path,0,0,textPaint);
                // path.reset();
            }

            indexLine++;

            if (isTest()) {
                pt.setColor(Color.RED);
                canvas.drawLine(0, finalHeight * indexLine + fm.ascent - topSpacing + lineSpacing + topSpace + statusBar, getWidth() + topSpace, finalHeight * indexLine + fm.ascent - topSpacing + lineSpacing + topSpace + statusBar, pt);
                canvas.drawLine(0, finalHeight * indexLine + fm.descent - topSpacing + lineSpacing + topSpace + statusBar, getWidth(), finalHeight * indexLine + fm.descent - topSpacing + lineSpacing + topSpace + statusBar, pt);
                canvas.drawLine(marginSpacing, 0, marginSpacing, getHeight(), pt);
                canvas.drawLine(getWidth() - marginSpacing, 0, getWidth() - marginSpacing, getHeight(), pt);
            }

            tts = tts + index;
            if (tts + 1 >= font_x.length) {
                break;
            }
        }
        textEnd = tts;

        String jd = format1.format(((float) (textEnd)) / text.length());
        canvas.drawText(jd, getWidth() * 0.97f - marginSpacing - ttPaint.measureText(jd), getHeight() - ttPaint.getTextSize() * 0.85f, ttPaint);

    }

    public interface CallBack {
        boolean onClick();
    }

}
