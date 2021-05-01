package com.cdwintech.wechat.support;

public interface DuplicatedMessageChecker {

    boolean isDuplicated(String msgKey);

}
