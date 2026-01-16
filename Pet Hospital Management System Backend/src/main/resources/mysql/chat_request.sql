-- 聊天申请表
DROP TABLE IF EXISTS `chat_request`;
CREATE TABLE `chat_request` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `doctor_id` BIGINT(20) NOT NULL COMMENT '医生ID',
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态（0=待审核, 1=已同意, 2=已拒绝）',
  `request_message` VARCHAR(500) DEFAULT NULL COMMENT '申请留言',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_doctor_id` (`doctor_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='聊天申请表';
