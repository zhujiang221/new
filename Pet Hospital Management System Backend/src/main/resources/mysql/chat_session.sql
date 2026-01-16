-- 聊天会话表
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `doctor_id` BIGINT(20) NOT NULL COMMENT '医生ID',
  `request_id` BIGINT(20) DEFAULT NULL COMMENT '关联的申请ID',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态（0=已关闭, 1=进行中）',
  `last_message_time` TIMESTAMP NULL DEFAULT NULL COMMENT '最后消息时间',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_doctor_id` (`doctor_id`) USING BTREE,
  KEY `idx_request_id` (`request_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_last_message_time` (`last_message_time`) USING BTREE,
  UNIQUE KEY `uk_user_doctor` (`user_id`, `doctor_id`) USING BTREE COMMENT '用户和医生唯一会话'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='聊天会话表';
