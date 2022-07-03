package com.zhhz.reader.ui.bookreader;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class BookReaderViewModelTest extends TestCase {

    public void testCurrent_progress_page() {
        BookReaderViewModel model = new BookReaderViewModel();
        ArrayList<Integer> list = new ArrayList<>();
        int i = 0;
        for (; i < 50; i++) {
            String s = new Random().nextInt(4) == 0 ? null : "a";
            model.catalogue.add(s);

            if (s != null) {
                list.add(50);
                model.comic_page.add(50);
            } else {
                list.add(0);
            }
        }
        model.setProgress(i - 1);
        System.out.println("list = " + list);
        System.out.println("current_progress_page -> " + Arrays.toString(model.current_progress_page(100)));

    }
}