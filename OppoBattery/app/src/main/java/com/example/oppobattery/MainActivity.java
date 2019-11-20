package com.example.oppobattery;


import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private  int count = 0;
    private boolean mIsStart = true;
    private Toast mToast;
    private TextView t1 ;
    private Handler mHandler = new Handler()
    {
        public void handleMessage(android.os.Message msg) {
            String Current = getCurrent();
            showToast(Current);
            showCount(Current);
            if (mIsStart) {
                // 因为Toast.LENGTH_SHORT的默认值是2000
                mHandler.sendEmptyMessageDelayed(0, 1000);
            }
        };
    };

    private void showToast(String content)
    {
        if(mToast == null)
        {
            mToast = Toast.makeText(MainActivity.this, content, Toast.LENGTH_SHORT);
        }
        else
        {
            mToast.setText(content);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    private void showCount(String s)
    {
        t1.setText("" +count + s);
        count++;
    }


    private String getCurrent()
    {
        String result = "null";
        try {
            Class systemProperties = Class.forName("android.os.SystemProperties");
            Method get = systemProperties.getDeclaredMethod("get", String.class);
            String platName = (String) get.invoke(null, "ro.hardware");
//            if (platName.startsWith("mt") || platName.startsWith("MT")) {
//                String filePath = "/sys/class/power_supply/battery/device/FG_Battery_CurrentConsumption";
//                // MTK平台该值不区分充放电，都为负数，要想实现充放电电流增加广播监听充电状态即可
//                result = "当前电流为：" + Math.round(getMeanCurrentVal(filePath, 5, 0) / 10.0f) + "mA";
//                result += ", 电压为：" + readFile("/sys/class/power_supply/battery/batt_vol", 0) + "mV";
//            } else if (platName.startsWith("qcom")) {

                String filePath = "/sys/class/power_supply/battery/current_now";
                int current = Math.round(getMeanCurrentVal(filePath, 5, 0) / 10.0f);
                int voltage = readFile("/sys/class/power_supply/battery/voltage_now", 0) / 1000;
                // 高通平台该值小于0时电池处于放电状态，大于0时处于充电状态
                if (current < 0) {
                    result = "充电电流为：" + (-current) + "mA, 电压为：" + voltage + "mV";
                } else {
                    result = "放电电流为：" + current + "mA, 电压为：" + voltage + "mV";
                }
//            }
        }
        catch( Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }


    private int readFile(String path, int defaultValue) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(
                    path));
            int i = Integer.parseInt(bufferedReader.readLine(), 10);
            bufferedReader.close();
            return i;
        } catch (Exception localException) {
        }
        return defaultValue;
    }

    /**
     * 获取平均电流值
     * 获取 filePath 文件 totalCount 次数的平均值，每次采样间隔 intervalMs 时间
     */
    private float getMeanCurrentVal(String filePath, int totalCount, int intervalMs) {
        float meanVal = 0.0f;
        if (totalCount <= 0) {
            return 0.0f;
        }
        for (int i = 0; i < totalCount; i++) {
            try {
                float f = Float.valueOf(readFile(filePath, 0));
                meanVal += f / totalCount;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (intervalMs <= 0) {
                continue;
            }
            try {
                Thread.sleep(intervalMs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return meanVal;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mHandler.sendEmptyMessage(0);
        t1 = (TextView)findViewById(R.id.textView);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(0);
        mIsStart = false;
    }

}
