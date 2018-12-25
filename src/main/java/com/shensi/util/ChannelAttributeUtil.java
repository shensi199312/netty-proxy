package com.shensi.util;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

/**
 * Created by shensi on 2018-12-21
 */
public class ChannelAttributeUtil {
    private static AttributeKey<Integer> status = AttributeKey.newInstance("status");
    private static AttributeKey<String> desHost = AttributeKey.newInstance("desHost");
    private static AttributeKey<Integer> desPort = AttributeKey.newInstance("desPort");



    public static Integer getProxyStatusFromChannel(Channel channel){
        return channel.attr(status).get();
    }

    public static void setProxyStatusToChannel(Channel channel, Integer newStatus){
        channel.attr(status).set(newStatus);
    }

    public static String getDesHostFromChannel(Channel channel){
        return channel.attr(desHost).get();
    }

    public static void setDesHostToChannel(Channel channel, String hostString){
        channel.attr(desHost).set(hostString);
    }

    public static Integer getDesPortFromChannel(Channel channel){
        return channel.attr(desPort).get();
    }

    public static void setDesPortToChannel(Channel channel, Integer port){
        channel.attr(desPort).set(port);
    }

}
