package org.ww.testapp.ui.my.local;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayoutMediator;
import org.ww.testapp.R;
import org.ww.testapp.ui.data.MusicViewModel;
import org.ww.testapp.ui.my.local.adapter.LocalPagerAdapter;

public class LocalActivity extends AppCompatActivity {

    private MusicViewModel musicViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local);

        musicViewModel = new ViewModelProvider(this).get(MusicViewModel.class);

        // 绑定返回按钮
        ImageButton backButton = findViewById(R.id.localBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 获取 TabLayout 和 ViewPager2 实例
        TabLayout tabLayout = findViewById(R.id.localTab);
        ViewPager2 viewPager = findViewById(R.id.localVp);

        // 设置 ViewPager2 的适配器
        viewPager.setAdapter(new LocalPagerAdapter(this));

        // 使用 TabLayoutMediator 将 TabLayout 和 ViewPager2 关联起来
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    // 设置 Tab 的文本
                    switch (position) {
                        case 0:
                            tab.setText("音乐");
                            break;
                        case 1:
                            tab.setText("专辑");
                            break;
                        case 2:
                            tab.setText("艺术家");
                            break;
                    }
                }).attach();

        // 添加滑动监听器，实现左右滑动切换 Tab
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // 将 TabLayout 中选中的 Tab 更新为 ViewPager2 中当前显示的页面
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });



    }
}
