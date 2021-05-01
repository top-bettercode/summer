package cn.bestwu.summer.util.wechat.support;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;

public class DefaultDuplicatedMessageChecker implements DuplicatedMessageChecker {

    private static final Cache<String, String> cache = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.SECONDS).maximumSize(10000).build();

    @Override
    public boolean isDuplicated(String msgKey) {
        if(cache.getIfPresent(msgKey) == null) {
            cache.put(msgKey, msgKey);
            return false;
        } else {
            return true;
        }
    }
}
