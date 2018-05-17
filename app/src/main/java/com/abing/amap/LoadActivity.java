package com.abing.amap;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;


/**
 * 项目名称：SecondPay
 * 类描述：
 * 创建人：Administrator
 * 创建时间：2016/8/8 16:06
 * 修改人：Administrator
 * 修改时间：2016/8/8 16:06
 * 修改备注：
 */
public class LoadActivity extends AppCompatActivity {

    private ImageView image_load;
    //数据
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loadsep);
        initView();
    }

    private void initView() {
        image_load = (ImageView) findViewById(R.id.image_load);

        image_load.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.loadpage));

        animation = new AlphaAnimation(0.1f, 1.0f);
        animation.setDuration(2500);
        image_load.setAnimation(animation);
        animation.setAnimationListener(new MyAnimationListener());
    }

    private class MyAnimationListener implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {


        }

        @Override
        public void onAnimationEnd(Animation animation) {
            startActivity(new Intent(LoadActivity.this,HomeActivity.class));
            finish();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }
}
