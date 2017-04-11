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
