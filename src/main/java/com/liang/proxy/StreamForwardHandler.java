package com.liang.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/5/27 上午12:55
 **/
public class StreamForwardHandler implements Runnable{
    private InputStream in;
    private OutputStream out;

    public StreamForwardHandler(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
