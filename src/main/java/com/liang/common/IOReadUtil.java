package com.liang.common;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/5/2 下午8:27
 **/
public class IOReadUtil {


    public static boolean readFixedLength(InputStream inputStream, byte[] bytes) throws IOException {
        return readFixedLength(inputStream, bytes, bytes.length);
    }

    public static boolean readFixedLength(InputStream inputStream, byte[] bytes, int length) throws IOException {

        int readLength = 0;
        while (readLength < length) {
            int temp = inputStream.read(bytes, readLength, length - readLength);
            if (temp == -1) {
                return false;
            }
            readLength += temp;
        }
        return true;
    }
}
