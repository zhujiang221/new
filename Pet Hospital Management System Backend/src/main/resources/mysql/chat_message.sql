-- 聊天消息表
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` BIGINT(20) NOT NULL COMMENT '会话ID',
  `sender_id` BIGINT(20) NOT NULL COMMENT '发送者ID',
  `receiver_id` BIGINT(20) NOT NULL COMMENT '接收者ID',
  `message_type` VARCHAR(20) NOT NULL DEFAULT 'text' COMMENT '消息类型（text=文字, emoji=表情, image=图片, file=文件）',
  `content` TEXT COMMENT '消息内容（文字内容或文件路径）',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读（0=未读, 1=已读）',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_session_id` (`session_id`) USING BTREE,
  KEY `idx_sender_id` (`sender_id`) USING BTREE,
  KEY `idx_receiver_id` (`receiver_id`) USING BTREE,
  KEY `idx_is_read` (`is_read`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='聊天消息表';
