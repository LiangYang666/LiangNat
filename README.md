# LiangNat

## ✨Star the [Repository](https://github.com/LiangYang666/LiangNat)  
## 简介
使用java网络编程实现内网穿透/端口映射,分为服务端和客户端，类似于frp， 概念介绍及程序设计思路详情见[博客网址](https://blog.csdn.net/qq_39165617/article/details/124641503?spm=1001.2014.3001.5501)
>1. 更新了web后台管理，能够管理允许登录的IP，设置IP白名单防火墙
>2. 更新了代理上网功能，能够代理内网，例如实现在校外访问校园内网才能访问的网站
>3. 更新了端口可设置是否受ip白名单防火墙保护，字段如客户端配置文件，例如你需要开放某些端口给别人调用api，但调用你api的ip有很多，没法一一设置白名单，且api应用层做了认证管理，因此该端口不需要受防火墙保护
## 使用
### 1.1 测试使用
1. 上线使用时，可直接在[GitHub releases](https://github.com/LiangYang666/LiangNat/releases)中下载使用，也可以自行修改编译出来进行使用
2. 将`NatServer-v1.jar`和`config_server.yaml`放置于云端服务器中的某个文件夹NatSoft内，可视情况修改配置文件，并执行 `java -jar NatServer-v1.jar`即可运行，执行`java -jar NatServer-v1.jar trace`可以指定日志级别为TRACE，测试调试时可以这样设置，但真实上线使用时不建议打印（耗时）
   配置内容及解释示例如下
   ```yaml
   common:
     bind_port: 10101    # 服务端口  可更换，保持与客户端一致即可
     token: 123456       # 密码
   
   web:
     bind_port: 10102  # 网页管理登录端口，可管理IP白名单 浏览器输入server_addr:10102可登录
     username: admin   # 登录用户名
     password: 123456  # 登录密码
   ```
4. 将`NatClient-v1.jar`和`config_client.yaml`放置于局域网内的电脑的某个文件夹NatSoft内，修改`config_client.yaml`，更改想要映射的端口和云服务器的IP密码等，执行 `java -jar NatClient-v1.jar`即可
   配置内容示例如下
   ```yaml
   common:
     server_addr: XXX.XXX.XXX.XXX    # 云服务器地址
     server_port: 10101      # 云服务器服务端口  与服务端配置一致
     token: 123456         # 登录密码 与服务端配置一致
   
   nat:
     ssh-2:
       type: tcp
       local_ip: 127.0.0.1 # 需要被映射的内网机器的IP,可以为局域网中任意机子的ip
       local_port: 22      # 需要被映射的内网机器的端口
       remote_port: 40022  # 对应的云端服务器映射端口    # 达到的效果是访问 server_addr+40022 相当于局域网内local_ip+local_port
   
     vnc-21:
       type: tcp
       local_ip: 127.0.0.1
       local_port: 5901
       remote_port: 45901
   
     api-connect:
       type: tcp
       local_ip: 192.168.1.202
       local_port: 8011
       remote_port: 8011
       firewall_protect: false   # 默认为受防火墙保护,即默认为true,设置为false后将不检查连接方的ip,用于接收某些不能确定ip的连接,为保证端口安全尽量在应用层做一些验证

   socks5_proxy: # 代理 将浏览器的代理或系统代理改为 socks5,server_addr:7999 实现穿透学校内网上网, 使用edge或chrome时安装SwitchyOmega插件进行代理配置最佳
     type: tcp
     remote_port: 7999

   ```
## 1.2 设置自启动服务
上面的测试使用在终端断开后将停止运行，因此我们需要注册我们的服务，达到开机自启动，或者手动运行服务后终端掉线依然运行
### 1.2.1 服务端开机自启动
执行`vim /etc/systemd/system/natServer.service`创建服务，编辑如下
```bash
[Unit]
Description=nat server daemon
After=syslog.target  network.target
Wants=network.target

[Service]
Type=simple
User=name	# 如果是root用户可省略
WorkingDirectory=/home/name/NatSoft			# 编辑的时候一定要删除注释 这里更改为自己放置jar包和配置的绝对路径
ExecStart=/path/to/your/java -jar NatServer-v1.jar	# 编辑的时候一定要删除注释 这里更改为自己在java命令的安装位置 可使用 which java查看
Restart= always
RestartSec=1min
[Install]
WantedBy=multi-user.target
```
执行如下
```bash
#启动natServer
systemctl daemon-reload
systemctl start natServer
#设置为开机启动
systemctl enable natServer

```
### 1.2.2 客户端开机自启动
执行`sudo vim /etc/systemd/system/natClient.service`创建服务，编辑如下
```bash
[Unit]
Description=nat client daemon
After=syslog.target  network.target
Wants=network.target

[Service]
Type=simple
User=name	# 编辑的时候一定要删除注释 这样可以使得进程归用户所有，使用jps查看时可以查看到，如果不设置，那么普通用户jps查看不到
WorkingDirectory=/home/name/NatSoft			# 编辑的时候一定要删除注释 这里更改为自己放置jar包和配置文件的绝对路径
ExecStart=/path/to/your/java -jar NatClient-v1.jar # 编辑的时候一定要删除注释 这里更改为自己在java命令的安装位置 可使用 which java查看
Restart= always
RestartSec=1min
[Install]
WantedBy=multi-user.target
```
执行如下

```bash
#启动natServer
systemctl daemon-reload
systemctl start natClient
#设置为开机启动
systemctl enable natClient
```
### 1.2.3 IP白名单防火墙管理
1. 输入你设置的web管理端口，server_addr:10102  
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/22de8f65510d4ce2940623a31501c8b9.png)
2. IP白名单列表  
   在里面可以删除和修改每条记录  
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/c0bf322e7b884e9787a99c9fa33b20de.png)
3. 添加IP  
   点击上方添加IP，将进入如下界面，下面的IP自动填充为访问设备的IP，属地为联网查询到并填充的，备注可以手动填写，另外我更新了一种IP过滤策略，一个IP是32位的，当添加IP为`192.168.0.0/24`时，则只需要IP的前24位与给定IP相同即可，即192.168.0开头即可  
   ![在这里插入图片描述](https://img-blog.csdnimg.cn/ee88f06560d64404a5727996b893fab4.png)


# 程序相关解释记录
## 各种socket名词定义
1. 信息交互socket：指的是服务端与客户端间的通信socket，例如客户端使用随机端口(假设1234)连接到服务端端口(默认为10101)，`localhost:1234<-->remoteIp:10101`建立的socket就是信息交互socket
2. socketWan: 云端端口(例如20022)与用户之间的连接socket
3. socketLan: 客户端进程与本地需要监听的端口(例如22)建立的通信socket


## 转发事件与转发消息
1. 转发事件： 指的是信息交互socket接收到接收事件的指令，将接收到的消息进行拆解成消息头和消息体，在消息头中找出转发socket标志，解析出需要转发到所需的目标端口，然后将消息体转发给该端口
2. 转发消息： 指的是普通端口socket接收到的消息，将该消息添加一个消息头，消息头中携带socket标志，将打包封装的消息转发给用于信息交互的socket

# 加密
AES对称加密
## 
1. 对于登录事件 对密码\各端口号进行加密、解密，密码长度，端口数量不加密
2. 对于收到转发事件 对携带的socket名称和消息体进行解密
3. 对于转发消息 对携带的socket名称和消息体进行加密


## maven
maven-shade-plugin
maven-compiler-plugin负责项目编译；
maven-shade-plugin负责最终的打包操作. 将所有依赖包打包




## bug记录 
### 1
长时间未使用时 \
client显示c2s依然是established，  \
但是server已经意外关闭了 \
造成client阻塞在read但无法检测到断开连接了 \
原因
服务端主机崩溃了，客户端是无法感知到的，在加上客户端没有开启 TCP keepalive，又没有数据交互的情况下，客户端的 TCP 连接将会一直处于 ESTABLISHED 连接状态，当服务端正常上线后，还会依然阻塞在读取而不会关闭连接
解决：增加心跳检测
