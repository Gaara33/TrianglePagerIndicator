package com.example.gaara.trianglepagerindicator;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EdgeEffect;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private String[] title = {"关注", "热门", "附近", "才艺", "有料", "游戏", "视频", "游戏", "视频", "游戏", "视频", "游戏", "视频"};
    private ArrayList<TextView> views = new ArrayList<TextView>();
    private MainTabs mainTabs;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for (int i = 0; i < title.length; i++) {
            TextView textView = new TextView(this);
            textView.setGravity(Gravity.CENTER);
            textView.setText(title[i]);
            views.add(textView);
        }
        final TestPageAdapter adapter = new TestPageAdapter();
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        mainTabs = (MainTabs) findViewById(R.id.tabs);
        mainTabs.setSpecialIndex(1);
        mainTabs.setSidePadding(10);
        mainTabs.setViewPager(viewPager);
        mainTabs.setOnPageChangeListener(new MainTabs.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d(TAG, "-onPageScrolled->" + position);
            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "-onPageSelected->" + position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d(TAG, "-onPageScrollStateChanged->" + state);
            }
        });

        mainTabs.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mainTabs.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                viewPager.setCurrentItem(1, false);
            }
        });
    }

    public static String GetInetAddress(String host) {
        String IPAddress = "";
        InetAddress ReturnStr1 = null;
        try {
            ReturnStr1 = java.net.InetAddress.getByName(host);
            IPAddress = ReturnStr1.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return IPAddress;
        }
        return IPAddress;
    }

    private class TestPageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(views.get(position));
            return views.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }


}
