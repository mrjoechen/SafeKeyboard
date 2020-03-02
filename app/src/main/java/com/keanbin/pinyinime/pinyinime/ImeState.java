package com.keanbin.pinyinime.pinyinime;

/**
 * Created by chenqiao on 2020-03-02.
 * e-mail : mrjctech@gmail.com
 */
/**
 * 输入法状态
 * STATE_IDLE 空闲
 * STATE_INPUT 输入
 * STATE_COMPOSING 组合、构成
 * STATE_PREDICT 预备
 */
public enum ImeState {
    STATE_BYPASS, STATE_IDLE, STATE_INPUT, STATE_COMPOSING, STATE_PREDICT, STATE_APP_COMPLETION
}
