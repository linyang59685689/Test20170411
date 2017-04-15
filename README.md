# Test20170411
带测试灵敏度的摇一摇实现


博客说明文档： http://blog.csdn.net/qq_34557284/article/details/70184010

##实现效果图
  ===================
  
 ![一个效果图，不重要](http://img.blog.csdn.net/20170415125218553?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMzQ1NTcyODQ=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
此Demo可以实时设置调整灵敏度，方便测试。


----------

## 摇一摇原理简介

手机摇一摇的实现，无非就是利用手机上的加速传感器，得到x、y、z轴三个方向的加速度。通过判断加速度的状态，来判断用户是否摇动了手机。而因为重力加速度一直存在，所以在一个方向上的加速度一直是9点多，设计的时候我们要考虑到这一点。


----------

##实现过程
 
**第一步：先获得加速度传感器**
	 

```
sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        if(sensorManager==null) return;
        sensor=(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
```上面做了简单判断，防止出空指针

  我这里用到了手机震动，所以还需要获得手机震动服务
	

```
 vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
```
手机振动需要添加限权：
```
<uses-permission android:name="android.permission.VIBRATE"/>
```

**第二步: 为传感器设置好监听（布局文件和setViews代码比较简单，暂时不在这里写了，会出现在下面的源码中**）
	

```
listener=new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                try{
                    long currentTime=System.currentTimeMillis();
                    long timeInterval=Long.valueOf(etTimeInterval.getText().toString());
                    if(timeInterval>currentTime-lastTime) return;//如果两次回调间隔过小，直接忽略
                    float[] values=event.values;
                    float x=values[0];
                    float y=values[1];
                    float z=values[2];
                    if(Math.abs(z)>11||Math.abs(x)>5||Math.abs(y)>3) {
                        Log.i("TAG","x:"+x);
                        Log.i("TAG","y:"+y);
                        Log.i("TAG","z:"+z);
                    }
                    float deltaX=x-lastX;
                    float deltaY=y-lastY;
                    float deltaZ=z-lastZ;

                    lastX=x;
                    lastY=y;
                    lastZ=z;
                    double speed=(Math.sqrt(deltaX*deltaX+deltaY*deltaY+deltaZ*deltaZ)/timeInterval)*100;
                    if(speed>Double.valueOf(etSpeed.getText().toString())){
                        vibrator.vibrate(300);
                        text.append("x:"+x+"   y:"+y+"   z:"+z+"\n"+"speed:"+speed+"\n");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }



            }
```
  因为加速度传感器回调特别快，所以做了一次时间上的过滤，小于50毫秒时间的回调自动忽略了。（这个时间其实可以根据需要自己设置）
  然后在传感事件event中分别得到x、y、z三个方向的加速度，因为摇一摇的加速度肯定变化特别剧烈，而不是一个均匀的加速，所以为了防干扰，判断这次加速度和上一次的变化会比较好。
  在三个方向上的加速度不一定是正数还是负数，所以计算的时候最好加个平方，其他方法当然也可以。

**第三步： 为传感器添加监听**
	

```
 @Override
    protected void onResume() {
        super.onResume();
        lastTime=System.currentTimeMillis();
        text.setText("");
        sensorManager.registerListener(listener,sensor,sensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
       sensorManager.unregisterListener(listener,sensor);
    }
```
为了防止摇一摇Activity在后台中也不断地监听，所以最好把此监听器设置在onResume()和onPause()上。

这样一个简单的摇一摇功能就实现了。

##源码

布局文件：

```
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.example.yang.test20170411.MainActivity">

    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        app:layout_constraintTop_toTopOf="parent"
        android:layout_marginTop="8dp"
        android:layout_marginLeft="8dp"
        app:layout_constraintLeft_toLeftOf="parent"
        android:layout_marginRight="8dp"
        app:layout_constraintRight_toRightOf="parent"
        android:layout_marginStart="8dp"
        android:layout_marginEnd="8dp"
        android:id="@+id/linearLayout">


        <android.support.design.widget.TextInputLayout
            android:layout_width="0dp"
            android:layout_weight="1"
            android:layout_height="wrap_content">

            <EditText
                android:id="@+id/et_time_interval"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="回调间隔（数字）" />
        </android.support.design.widget.TextInputLayout>

        <android.support.design.widget.TextInputLayout
            android:layout_weight="1"
            android:layout_width="0dp"
            android:layout_height="match_parent">

            <EditText
                android:id="@+id/et_speed"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:hint="敏感度（数字）" />
        </android.support.design.widget.TextInputLayout>
    </LinearLayout>

    <ScrollView
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:layout_marginTop="8dp"
        app:layout_constraintTop_toBottomOf="@+id/linearLayout"
        android:layout_marginRight="8dp"
        app:layout_constraintRight_toRightOf="parent"
        android:layout_marginLeft="8dp"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        android:layout_marginBottom="8dp">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical" >

            <TextView
                android:id="@+id/textView"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:text="" />
        </LinearLayout>
    </ScrollView>

</android.support.constraint.ConstraintLayout>

```

MainActivity：

```
package com.example.yang.test20170411;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener listener;
    private Vibrator vibrator;
    private EditText etSpeed;
    private EditText etTimeInterval;
    private TextView text;
    private float lastX;
    private float lastY;
    private float lastZ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        if(sensorManager==null) return;
        sensor=(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        if(sensor==null) return;
        vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
        setViews();
        listener=new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                try{
                    long currentTime=System.currentTimeMillis();
                    long timeInterval=Long.valueOf(etTimeInterval.getText().toString());
                    if(timeInterval>currentTime-lastTime) return;//如果两次回调间隔过小，直接忽略
                    float[] values=event.values;
                    float x=values[0];
                    float y=values[1];
                    float z=values[2];
                    if(Math.abs(z)>11||Math.abs(x)>5||Math.abs(y)>3) {
                        Log.i("TAG","x:"+x);
                        Log.i("TAG","y:"+y);
                        Log.i("TAG","z:"+z);
                    }
                    float deltaX=x-lastX;
                    float deltaY=y-lastY;
                    float deltaZ=z-lastZ;

                    lastX=x;
                    lastY=y;
                    lastZ=z;
                    double speed=(Math.sqrt(deltaX*deltaX+deltaY*deltaY+deltaZ*deltaZ)/timeInterval)*100;
                    if(speed>Double.valueOf(etSpeed.getText().toString())){
                        vibrator.vibrate(300);
                        text.append("x:"+x+"   y:"+y+"   z:"+z+"\n"+"speed:"+speed+"\n");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }



            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }

    private void setViews() {
        etSpeed=(EditText)findViewById(R.id.et_speed);
        etTimeInterval=(EditText)findViewById(R.id.et_time_interval);
        text=(TextView)findViewById(R.id.textView);
    }
    long lastTime;
    @Override
    protected void onResume() {
        super.onResume();
        lastTime=System.currentTimeMillis();
        text.setText("");
        sensorManager.registerListener(listener,sensor,sensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
       sensorManager.unregisterListener(listener,sensor);
    }
}

```

别忘了申请震动限权哈： <uses-permission android:name="android.permission.VIBRATE"/>

最后附上github源码地址： https://github.com/linyang59685689/Test20170411
