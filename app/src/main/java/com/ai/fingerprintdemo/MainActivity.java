package com.ai.fingerprintdemo;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;


import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private FingerprintManager mFingerprintManager;
    private KeyguardManager mKeyguardManager;
    private final static int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 0;
    private final static String TAG = "finger_log";
    private CancellationSignal mCancellationSignal = new CancellationSignal();
    private AuthenticationCallback mSelfCancelled;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();



        findViewById(R.id.btn_activity_main_finger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View mView) {
                if (isFinger()) {
                    showToast("请进行指纹识别");
                    startListening(null);
                } else {
                    showAuthenticationScreen();
                }

            }
        });

    }

    private void initView() {
        mKeyguardManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mFingerprintManager = (FingerprintManager) this.getSystemService(Context.FINGERPRINT_SERVICE);
            //回调方法
            mSelfCancelled = new AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    //但多次指纹密码验证错误后，进入此方法；并且，不能短时间内调用指纹验证
                    showToast(errString.toString());
                    showAuthenticationScreen();
                }

                @Override
                public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                    super.onAuthenticationHelp(helpCode, helpString);
                    showToast(helpString.toString());
                }

                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    showToast("指纹识别成功！");
                    Intent mIntent = new Intent(MainActivity.this, NextActivity.class);
                    startActivity(mIntent);
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    showToast("指纹识别失败！");
                }
            };
        } else {
            showToast("本机不能进行指纹识别！");
        }

        //页面初始化就可以进行指纹识别
        if (isFinger()) {
            showToast("请进行指纹识别");
            startListening(null);
        } else {
            showAuthenticationScreen();
        }

    }

    public void showToast(String name) {
        Toast.makeText(MainActivity.this, name, Toast.LENGTH_SHORT).show();
    }

    private void Log(String tag, String msg) {
        Log.d(tag, msg);
    }

    public boolean isFinger() {
        //android studio 上，没有这个会报错
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            showToast("没有指纹识别权限");
            return false;
        }
        Log(TAG, "有指纹权限");

        //判断硬件是否支持指纹识别
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!mFingerprintManager.isHardwareDetected()) {
                showToast("没有指纹识别模块");
                return false;
            }
        }
        Log(TAG, "有指纹模块");

        //判断 是否开启锁屏密码
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (!mKeyguardManager.isKeyguardSecure()) {
                Toast.makeText(this, "没有开启锁屏密码", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        Log(TAG, "已开启锁屏密码");

        //判断是否有指纹录入
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!mFingerprintManager.hasEnrolledFingerprints()) {
                Toast.makeText(this, "没有录入指纹", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        Log(TAG, "已录入指纹");

        return true;
    }


    //调密码登录认证
    private void showAuthenticationScreen() {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            intent = mKeyguardManager.createConfirmDeviceCredentialIntent("finger", "测试指纹识别");
        }
        if (intent != null) {
            startActivityForResult(intent, REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
        } else {
            intent = new Intent(MainActivity.this, NextActivity.class);
            startActivity(intent);
        }
    }

    //开始监听识别指纹
    public void startListening(FingerprintManager.CryptoObject cryptoObjec) {
        //android studio 上，没有这个会报错
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            showToast("没有指纹识别权限");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            mFingerprintManager.authenticate(cryptoObjec, mCancellationSignal, 0, mSelfCancelled, null);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            // Challenge completed, proceed with using cipher
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "识别成功", Toast.LENGTH_SHORT).show();
                Intent mIntent = new Intent(MainActivity.this, NextActivity.class);
                startActivity(mIntent);
            } else {
                Toast.makeText(this, "识别失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
