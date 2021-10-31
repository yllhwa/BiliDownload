package cc.kafuu.bilidownload;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.DownloadManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import cc.kafuu.bilidownload.bilibili.Bili;
import cc.kafuu.bilidownload.fragment.DownloadFragment;
import cc.kafuu.bilidownload.fragment.VideoParserFragment;
import cc.kafuu.bilidownload.jniexport.JniTools;
import cc.kafuu.bilidownload.service.DownloadService;
import cc.kafuu.bilidownload.utils.DialogTools;

public class MainActivity extends AppCompatActivity {
    private Handler mHandler;
    private Toolbar mToolbar;
    private BottomNavigationView mBottomNavigationView;

    private int mCurrentFragment = 0;
    private List<Fragment> mFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bili.initApplication(getApplicationContext());

        mHandler = new Handler();

        if (savedInstanceState != null) {
            mCurrentFragment = savedInstanceState.getInt("CurrentFragment");
        }

        findView();
        initView();

        initFragment(savedInstanceState);
        showFragment(mCurrentFragment);

        checkIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        checkIntent();
    }

    private void checkIntent() {
        Intent intent = getIntent();
        if (intent.getBooleanExtra("DownloadNotification", false)) {
            mBottomNavigationView.setSelectedItemId(R.id.navDownloadList);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        getSupportFragmentManager().putFragment(outState, "VideoParserFragment", mFragments.get(0));
        getSupportFragmentManager().putFragment(outState, "DownloadFragment", mFragments.get(1));

        outState.putInt("CurrentFragment", mCurrentFragment);
        super.onSaveInstanceState(outState);
    }

    private void findView() {
        mToolbar = findViewById(R.id.toolbar);
        mBottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void initView() {
        setSupportActionBar(mToolbar);
        mBottomNavigationView.setOnNavigationItemSelectedListener(this::bottomNavigationItemSelected);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_appinfo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.navAbout) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.about)
                    .setMessage(R.string.about_context)
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 初始化Fragment
     * */
    private void initFragment(Bundle savedInstanceState) {
        mFragments = new ArrayList<>();

        if (savedInstanceState != null) {
            //有保存的状态就恢复之前的
            Fragment fragment = (Fragment) getSupportFragmentManager().getFragment(savedInstanceState, "VideoParserFragment");
            if (fragment != null) {
                mFragments.add(fragment);
            }

            fragment = (Fragment) getSupportFragmentManager().getFragment(savedInstanceState, "DownloadFragment");
            if (fragment != null) {
                mFragments.add(fragment);
            }
        } else {
            mFragments.add(VideoParserFragment.newInstance());
            mFragments.add(DownloadFragment.newInstance());

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            for (Fragment fragment : mFragments) {
                transaction.add(R.id.frameLayout, fragment);
            }
            transaction.commitAllowingStateLoss();
        }
    }

    /**
     * 显示指定的Fragment
     *
     * @param index 要显示的Fragment索引
     * */
    private void showFragment(int index) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        for (Fragment fragment : mFragments) {
            transaction.hide(fragment);
        }
        transaction.show(mFragments.get(index));
        transaction.commitAllowingStateLoss();

        mCurrentFragment = index;

        mToolbar.setTitle(mBottomNavigationView.getMenu().getItem(index).getTitle());
    }

    /**
     * 底部导航栏被选择事件
     * @param item 选中的菜单
     * */
    private boolean bottomNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.navVideoParser) {
            showFragment(0);
        } else {
            showFragment(1);
        }
        return true;
    }
}