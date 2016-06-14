package com.thy.onekey;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

public class MainActivity extends AppCompatActivity {


    private TextView moneyTv;
    private TextView net_tv;
    private TextView text_tv;
    public static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private boolean isConn = false;
    private SmsReceiver smsBd;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);
        new UpdateDetection(this).checkUpdateInfo();


        initData();
        //话费查询
        moneyTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtainData(true);
            }
        });

        //流量查询
        net_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtainData(false);
            }
        });
    }


    public void initData() {

        smsBd=new SmsReceiver();
        isConn=isConnect();
        moneyTv = (TextView) findViewById(R.id.money);
        net_tv = (TextView) findViewById(R.id.net);
        text_tv = (TextView) findViewById(R.id.text);
//        final ProgressDialog dialog = ProgressDialog.show(this, "查询中", "请等待...", true);
//        dialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }


    /**
     * 自定义返回键的效果
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { // 返回键
            finish();
        }

        return true;
    }

    private NetBusiness who() {
        NetBusiness business = NetBusiness.OTHER;

        TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        /** 获取SIM卡的IMSI码
         * SIM卡唯一标识：IMSI 国际移动用户识别码（IMSI：International Mobile Subscriber Identification Number）是区别移动用户的标志，
         * 储存在SIM卡中，可用于区别移动用户的有效信息。IMSI由MCC、MNC、MSIN组成，其中MCC为移动国家号码，由3位数字组成，
         * 唯一地识别移动客户所属的国家，我国为460；MNC为网络id，由2位数字组成，
         * 用于识别移动客户所归属的移动网络，中国移动为00，中国联通为01,中国电信为03；MSIN为移动客户识别码，采用等长11位数字构成。
         * 唯一地识别国内GSM移动通信网中移动客户。所以要区分是移动还是联通，只需取得SIM卡中的MNC字段即可
         */
        String imsi = telManager.getSubscriberId();
        if (imsi != null) {
            if (imsi.startsWith("46000") || imsi.startsWith("46002")) {//因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
                //中国移动
                business = NetBusiness.CMC;
            } else if (imsi.startsWith("46001")) {
                //中国联通
                business = NetBusiness.CU;
            } else if (imsi.startsWith("46003")) {
                //中国电信
                business = NetBusiness.CT;
            }
        }
        return business;
    }


    public boolean isConnect() {

        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State wifiState = cm.getActiveNetworkInfo().getState();
        NetworkInfo.State mobileState = cm.getActiveNetworkInfo().getState();

        if (wifiState != null && mobileState != null && NetworkInfo.State.CONNECTED != wifiState && NetworkInfo.State.CONNECTED == mobileState) {
//            Toast.makeText(context, "手机网络连接成功！", Toast.LENGTH_SHORT).show();
            return true;
        } else if (wifiState != null && mobileState != null && NetworkInfo.State.CONNECTED == wifiState && NetworkInfo.State.CONNECTED != mobileState) {
//            Toast.makeText(context, "无线网络连接成功！", Toast.LENGTH_SHORT).show();
            return true;
        } else if (wifiState != null && mobileState != null && NetworkInfo.State.CONNECTED != wifiState && NetworkInfo.State.CONNECTED != mobileState) {
//            Toast.makeText(context, "手机没有任何网络...", Toast.LENGTH_SHORT).show();
            return false;
        }
        return false;

    }

    public void sendSmsMessage(NetBusiness business, boolean isMoney) {
        SmsManager smsManager = SmsManager.getDefault();

        Intent intent=new Intent();
        intent.setAction(ACTION);
        intent.putExtra("no",business.getValue());
        //判断短信内容的长度，如果长度大于70就会出错，所以这步很重要
        if (isMoney) {

            smsManager.sendTextMessage(business.getValue(), null, business.getMoneyCode(), null, null);
            intent.putExtra("key_word","余额");
        } else {
            smsManager.sendTextMessage(business.getValue(), null, business.getNetCode(), null, null);
            intent.putExtra("key_word", "流量");
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);

        this.registerReceiver(smsBd, intentFilter);

        sendBroadcast(intent);





    }


    public void obtainData(boolean isMoney) {
        dialog = ProgressDialog.show(this, "查询中", "请等待...", true);
        if (isConn) {
            //网络接口查询
        } else {
            //短信方式
            sendSmsMessage(who(), isMoney);
        }
    }


    private class SmsReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (ACTION.equals(intent.getAction())) {
                Intent i = new Intent(context, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                SmsMessage[] msgs = getMessageFromIntent(intent);

                StringBuilder sBuilder = new StringBuilder();
                if (msgs != null && msgs.length > 0) {
                    for (SmsMessage msg : msgs) {
                        if (msg.getDisplayOriginatingAddress().equals(intent.getExtras().getString("no")) && msg.getDisplayMessageBody().contains("")) {

                            abortBroadcast();
                            sBuilder.append(msg.getDisplayMessageBody());
                            dialog.dismiss();
                            text_tv.setText(sBuilder.toString());
                        }
                    }
                }
//
            }
            MainActivity.this.unregisterReceiver(smsBd);

        }

        public SmsMessage[] getMessageFromIntent(Intent intent) {
            SmsMessage retmeMessage[] = null;
            Bundle bundle = intent.getExtras();
            Object pdus[] = (Object[]) bundle.get("pdus");
            retmeMessage = new SmsMessage[pdus.length];
            for (int i = 0; i < pdus.length; i++) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String format = bundle.getString("format");
                    retmeMessage[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                }
                else {
                    retmeMessage[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
            }
            return retmeMessage;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsBd);
    }
}
