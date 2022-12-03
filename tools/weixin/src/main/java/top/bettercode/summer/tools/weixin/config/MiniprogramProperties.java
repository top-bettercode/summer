package top.bettercode.summer.tools.weixin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Peter Wu
 */
@ConfigurationProperties(prefix = "summer.wechat.mini")
public class MiniprogramProperties extends WexinProperties implements IMiniprogramProperties{


}
