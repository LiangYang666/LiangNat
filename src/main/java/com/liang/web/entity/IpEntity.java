package com.liang.web.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/5/25 下午2:58
 **/
@Data
@AllArgsConstructor
@ToString
@TableName("allowed_ip")
public class IpEntity {
    @TableId
    private String ip;
    private String address;
    private String createTime;
    private String comment;
}
