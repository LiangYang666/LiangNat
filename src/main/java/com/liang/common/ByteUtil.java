package com.liang.common;

import java.nio.ByteBuffer;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/4/27 下午3:23
 **/

public class ByteUtil {
    private static ByteBuffer buffer = ByteBuffer.allocate(8);
    /**
     * int转byte
     * @param x
     * @return
     */
    public static byte intToByte(int x) {
        return (byte) x;
    }
    /**
     * byte转int
     * @param b
     * @return
     */
    public static int byteToInt(byte b) {
        //Java的byte是有符号，通过 &0xFF转为无符号
        return b & 0xFF;
    }

    /**
     * byte[]转int
     * @param b
     * @return
     */
    public static int byteArrayToInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }
    public static int byteArrayToInt(byte[] b, int index){
        return   b[index+3] & 0xFF |
                (b[index+2] & 0xFF) << 8 |
                (b[index+1] & 0xFF) << 16 |
                (b[index+0] & 0xFF) << 24;
    }
    /**
     * int转byte[]
     * @param a
     * @return
     */
    public static byte[] intToByteArray(int a) {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }
    /**
     * short转byte[]
     *
     * @param b
     * @param s
     * @param index
     */
    public static void byteArrToShort(byte b[], short s, int index) {
        b[index + 1] = (byte) (s >> 8);
        b[index + 0] = (byte) (s >> 0);
    }
    /**
     * byte[]转short
     *
     * @param b
     * @param index
     * @return
     */
    public static short byteArrToShort(byte[] b, int index) {
        return (short) (((b[index + 0] << 8) | b[index + 1] & 0xff));
    }
    /**
     * 16位short转byte[]
     *
     * @param s
     *            short
     * @return byte[]
     * */
    public static byte[] shortToByteArr(short s) {
        byte[] targets = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = (byte) ((s >>> offset) & 0xff);
        }
        return targets;
    }
    /**
     * byte[]转16位short
     * @param b
     * @return
     */
    public static short byteArrToShort(byte[] b){
        return byteArrToShort(b,0);
    }

    /**
     * long转byte[]
     * @param x
     * @return
     */
    public static byte[] longToBytes(long x) {
        buffer.putLong(0, x);
        return buffer.array();
    }
    /**
     * byte[]转Long
     * @param bytes
     * @return
     */
    public static long bytesToLong(byte[] bytes) {
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip
        return buffer.getLong();
    }
    /**
     * 从byte[]中抽取新的byte[]
     * @param data - 元数据
     * @param start - 开始位置
     * @param end - 结束位置
     * @return 新byte[]
     */
    public static byte[] getByteArr(byte[]data,int start ,int end){
        byte[] ret=new byte[end-start];
        for(int i=0;(start+i)<end;i++){
            ret[i]=data[start+i];
        }
        return ret;
    }
    /**
     * byte数组内数字是否相同
     * @param s1
     * @param s2
     * @return
     */
    public static boolean isEq(byte[] s1,byte[] s2){
        int slen=s1.length;
        if(slen==s2.length){
            for(int index=0;index<slen;index++){
                if(s1[index]!=s2[index]){
                    return false;
                }
            }
            return true;
        }
        return  false;
    }
}
