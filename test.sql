drop table allowed_ip;
create table allowed_ip(
    ip vchar(15) PRIMARY KEY, -- 允许的ip
    address vchar(20) ,      -- 允许的ip所属地址
    create_time date not null, -- 创建时间
    comment vchar(100) -- 备注
);
insert into allowed_ip values('127.0.0.1', '本地回环', '2022-5-1', '允许本地回环');
insert into allowed_ip values('192.168.0.1', null, '2022-5-1', '测试1');
insert into allowed_ip values('192.168.0.2', null, '2022-5-2', '测试2');
insert into allowed_ip values('192.168.0.3', null, '2022-5-3', '测试3');
insert into allowed_ip values('192.168.0.4', null, '2022-5-4', '测试4');
insert into allowed_ip values('192.168.0.5', null, '2022-5-5', '测试5');
insert into allowed_ip values('192.168.0.6', null, '2022-5-6', '测试6');
select * from allowed_ip;