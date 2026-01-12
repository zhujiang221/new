-- 预约消息提醒表
DROP TABLE IF EXISTS `notification_message`;
CREATE TABLE `notification_message` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `receiver_id` BIGINT(20) NOT NULL COMMENT '接收者ID（医生ID）',
  `sender_id` BIGINT(20) DEFAULT NULL COMMENT '发送者ID（用户ID）',
  `appointment_id` BIGINT(20) DEFAULT NULL COMMENT '预约ID（关联appointment表）',
  `type` VARCHAR(50) NOT NULL DEFAULT 'APPOINTMENT' COMMENT '消息类型（如"APPOINTMENT"）',
  `title` VARCHAR(200) NOT NULL COMMENT '消息标题',
  `content` TEXT COMMENT '消息内容（JSON格式，包含用户姓名、时间段、预约类型等）',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读（0=未读，1=已读）',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_receiver_id` (`receiver_id`) USING BTREE,
  KEY `idx_is_read` (`is_read`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE,
  KEY `idx_appointment_id` (`appointment_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='预约消息提醒表';
