package com.thy.onekey;

import com.netlib.model.Bean;

/**
 * Created by thy on 16-6-13 下午5:53.
 * E-mail : jieooo7@163.com
 */
public enum NetBusiness {
    CMC("10086","",null,"YE","CXGTC"),CT("10001","",null,"102","108"),CU("10010","",null,"102","CXLL"),OTHER("","",null,"","");
    private final String value;
    private final String url;
    private final Bean bean;//返回值
    private final String netCode;//流量查询代码
    private final String moneyCode;//短信查询代码

    //构造器默认也只能是private, 从而保证构造函数只能在内部使用
    NetBusiness(String value,String url,Bean bean,String moneyCode,String netCode) {
        this.value = value;
        this.url=url;
        this.bean=bean;
        this.moneyCode=moneyCode;
        this.netCode=netCode;
    }

    public String getValue() {
        return value;
    }


    public String getUrl(){
        return url;
    }


    public Bean getBean(){
        return bean;
    }

    public String getNetCode() {
        return netCode;
    }

    public String getMoneyCode() {
        return moneyCode;
    }
}
