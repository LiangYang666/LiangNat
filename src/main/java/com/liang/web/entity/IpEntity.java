package com.liang.web.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * @Description: 白名单IP实体类
 * @Author: LiangYang
 * @Date: 2022/5/25 下午2:58
 **/
@Data
@AllArgsConstructor
@ToString
@TableName("allowed_ip")    //数据表名
public class IpEntity {
    @TableId    // 绑定ip为Id，这样就可以使用mybatis-plus的selectById方法
    private String ip;
    private String address;
    private String createTime;
    private String comment;
}
