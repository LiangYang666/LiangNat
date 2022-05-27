package com.liang.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;

/**
 * @Description: 设置允许IP的工具
 * @Author: LiangYang
 * @Date: 2022/5/26 下午3:47
 **/
public class AllowedIpUtil {
    static public HashSet<String> ipSets = new HashSet<>(); // IP白名单，这其中的IP允许放行

    /**
     * 判断给定IP是不是cidrIP段的一部分，
     * IP过滤策略，例如：一个IP是32位的，cidrIp为192.168.0.0/24时，
     *              则只需要IP的前24位与给定IP相同即可，即192.168.0开头即可
     */
    public static boolean checkIpBelongCIDR(String cidrIp, String ip) throws UnknownHostException {
        String[] cidrIpArr = cidrIp.split("/");
        String cidrIpPrefix = cidrIpArr[0];
        int cidrIpPrefixLen = Integer.parseInt(cidrIpArr[1]);
        byte[] cidrIpBytes = InetAddress.getByName(cidrIpPrefix).getAddress();
        byte[] IpBytes = InetAddress.getByName(ip).getAddress();
        int cidrIpInt = getIntFromByte(cidrIpBytes);
        int IpInt = getIntFromByte(IpBytes);
        int mask = 0xffffffff << (32 - cidrIpPrefixLen);
        return (cidrIpInt & mask) == (IpInt & mask);
    }

    private static int getIntFromByte(byte[] address) {
        int addressInt = 0;
        for (int i = 0; i < address.length; i++) {
            addressInt += address[i] << (8 * (address.length - i - 1));
        }
        return addressInt;
    }
}
