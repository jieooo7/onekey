package com.thy.onekey;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    private TextView moneyTv;
    private TextView net_tv;
    private TextView text_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initData();

        moneyTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        text_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    public void initData() {
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
        return true;
    }

    public void sendSmsMessage(NetBusiness business,boolean isMoney) {
        SmsManager smsManager = SmsManager.getDefault();

        //判断短信内容的长度，如果长度大于70就会出错，所以这步很重要
        if(isMoney){

            smsManager.sendTextMessage(business.getValue(), null, business.getMoneyCode(), null, null);
        }else{
            smsManager.sendTextMessage(business.getValue(), null, business.getNetCode(), null, null);
        }
    }

}
