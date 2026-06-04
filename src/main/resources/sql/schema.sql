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

-- 账户表（余额由记账增减/划账维护；存额=余额-未结清负债，见 account_debt）
CREATE TABLE IF NOT EXISTS `account` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT        NOT NULL COMMENT '所属用户',
    `name`        VARCHAR(50)   NOT NULL COMMENT '账户名',
    `type`        VARCHAR(30)   NOT NULL COMMENT '类型:储蓄卡/信用卡/支付宝余额/微信余额/花呗/余额宝/零钱通/理财/饭卡/现金/其他',
    `bank`        VARCHAR(50)   DEFAULT NULL COMMENT '银行名(银行卡类)',
    `kind`        TINYINT       NOT NULL DEFAULT 0 COMMENT '0 储蓄 / 1 信用',
    `balance`     DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '余额',
    `icon`        VARCHAR(50)   DEFAULT NULL,
    `sort_order`  INT           NOT NULL DEFAULT 0,
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上次划账/更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '账户';

-- 账户负债表（存额=账户余额-未结清负债(未还款+已逾期)）
CREATE TABLE IF NOT EXISTS `account_debt` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT        NOT NULL COMMENT '所属用户',
    `account_id`  BIGINT        NOT NULL COMMENT '所属账户',
    `name`        VARCHAR(100)  DEFAULT NULL COMMENT '负债名/说明',
    `amount`      DECIMAL(12,2) NOT NULL COMMENT '应还本金',
    `rate`        DECIMAL(8,4)  DEFAULT 0 COMMENT '年利率(%)',
    `type`        TINYINT       NOT NULL DEFAULT 0 COMMENT '0 一次性 / 1 按月还款',
    `months`      INT           DEFAULT NULL COMMENT '期限/期数(月)',
    `repay_method` TINYINT      NOT NULL DEFAULT 3 COMMENT '0等额本息/1等额本金/2付息后一次性还本/3一次性还本息',
    `status`      TINYINT       NOT NULL DEFAULT 0 COMMENT '(旧)0未还/1已还/2逾期，现以分期为准',
    `due_date`    DATE          DEFAULT NULL COMMENT '首期/到期日',
    `remark`      VARCHAR(255)  DEFAULT NULL,
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_account` (`account_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '账户负债';

-- 负债分期（按月还款生成 N 条，可逐期标记还款；一次性也生成 1 条）
CREATE TABLE IF NOT EXISTS `debt_installment` (
    `id`         BIGINT        NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT        NOT NULL,
    `account_id` BIGINT        NOT NULL,
    `debt_id`    BIGINT        NOT NULL,
    `period`     INT           NOT NULL COMMENT '第几期',
    `due_date`   DATE          DEFAULT NULL,
    `principal`  DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '本金部分',
    `interest`   DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '利息部分',
    `amount`     DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '本期应还(本+息)',
    `status`     TINYINT       NOT NULL DEFAULT 0 COMMENT '0未还/1已还/2逾期',
    PRIMARY KEY (`id`),
    KEY `idx_debt` (`debt_id`),
    KEY `idx_acct` (`account_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '负债分期';

-- 账单流水表（category_id 指向二级分类即叶子；account_id 关联账户用于增减余额）
CREATE TABLE IF NOT EXISTS `record` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT        NOT NULL COMMENT '所属用户',
    `account_id`  BIGINT        DEFAULT NULL COMMENT '所属账户',
    `category_id` BIGINT        NOT NULL COMMENT '二级分类（叶子）',
    `type`        TINYINT       NOT NULL COMMENT '类型：0 支出 / 1 收入',
    `amount`      DECIMAL(10,2) NOT NULL COMMENT '金额',
    `remark`      VARCHAR(255)  DEFAULT NULL COMMENT '备注',
    `record_date` DATE          NOT NULL COMMENT '记账日期',
    `has_image`   TINYINT       NOT NULL DEFAULT 0 COMMENT '是否有截图',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_date` (`user_id`, `record_date`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '账单流水表';

-- 账单截图表（压缩后的 jpg，单独存放不拖慢账单列表查询）
CREATE TABLE IF NOT EXISTS `record_image` (
    `record_id`    BIGINT      NOT NULL COMMENT '账单 id',
    `content`      MEDIUMBLOB  NOT NULL COMMENT '压缩后的 jpg 二进制',
    `content_type` VARCHAR(50) NOT NULL DEFAULT 'image/jpeg' COMMENT 'MIME',
    `create_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`record_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '账单截图';
