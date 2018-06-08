package me.gc.facerecognition;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import me.gc.facerecognition.fragment.FaceAlbumFragment;
import me.gc.facerecognition.fragment.FaceCompareFragment;
import me.gc.facerecognition.fragment.FaceCreatFragment;
import me.gc.facerecognition.fragment.FaceSearchFragment;

public class MainActivity extends AppCompatActivity {

    //TabLayout
    private TabLayout mTabLayout;
    //ViewPager
    private ViewPager mViewPager;
    //Title
    private List<String> mTitle;
    //Fragment
    private List<Fragment> mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
    }
    private void initData(){
        mTitle=new ArrayList<>();
        mTitle.add("新建");
        mTitle.add("图集");
        mTitle.add("搜索");
        mTitle.add("比对");
        mFragment=new ArrayList<>();
        mFragment.add(new FaceCreatFragment());
        mFragment.add(new FaceAlbumFragment());
        mFragment.add(new FaceSearchFragment());
        mFragment.add(new FaceCompareFragment());

    }
    private void initView() {
        mTabLayout = (TabLayout) findViewById(R.id.mTabLayout);
        mViewPager = (ViewPager) findViewById(R.id.mViewPager);

        // 预加载
        mViewPager.setOffscreenPageLimit(mFragment.size());

        // mViewPager滑动监听
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        // 设置适配器
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragment.get(position);
            }

            @Override
            public int getCount() {
                return mFragment.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTitle.get(position);
            }
        });
        // 绑定
       mTabLayout.setupWithViewPager(mViewPager);
    }

}
