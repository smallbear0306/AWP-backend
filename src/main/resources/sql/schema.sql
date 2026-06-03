-- AWP 记账网页 建库建表脚本
-- 执行方式：mysql -uroot -p < schema.sql

CREATE DATABASE IF NOT EXISTS awp
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE awp;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码（加盐哈希）',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- 分类表（三级体系：收支类型 type -> 一级分类(parent_id=NULL) -> 二级分类(parent_id 指向一级)）
CREATE TABLE IF NOT EXISTS `category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       NOT NULL DEFAULT 0 COMMENT '所属用户：0=系统预设(全局)，>0=用户自定义',
    `parent_id`   BIGINT       DEFAULT NULL COMMENT '父分类：NULL=一级分类，否则指向一级分类 id',
    `type`        TINYINT      NOT NULL COMMENT '类型：0 支出 / 1 收入',
    `name`        VARCHAR(50)  NOT NULL COMMENT '分类名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '说明（如括号内的"油费/充电"），作提示展示',
    `icon`        VARCHAR(50)  DEFAULT NULL COMMENT '图标标识',
    `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_parent` (`parent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '分类表（自关联两层 + 收支类型）';

-- 账单流水表（category_id 指向二级分类即叶子）
CREATE TABLE IF NOT EXISTS `record` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT        NOT NULL COMMENT '所属用户',
    `category_id` BIGINT        NOT NULL COMMENT '二级分类（叶子）',
    `type`        TINYINT       NOT NULL COMMENT '类型：0 支出 / 1 收入',
    `amount`      DECIMAL(10,2) NOT NULL COMMENT '金额',
    `remark`      VARCHAR(255)  DEFAULT NULL COMMENT '备注',
    `record_date` DATE          NOT NULL COMMENT '记账日期',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_date` (`user_id`, `record_date`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '账单流水表';
