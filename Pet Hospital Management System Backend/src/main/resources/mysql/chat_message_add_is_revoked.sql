-- 为聊天消息表添加撤回字段
ALTER TABLE `chat_message` 
ADD COLUMN `is_revoked` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已撤回（0=未撤回, 1=已撤回）' AFTER `is_read`;

-- 添加索引以提高查询性能
ALTER TABLE `chat_message` 
ADD INDEX `idx_is_revoked` (`is_revoked`) USING BTREE;
