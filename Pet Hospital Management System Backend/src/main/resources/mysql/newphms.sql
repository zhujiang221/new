-- MySQL dump 10.13  Distrib 5.7.35, for Win64 (x86_64)
--
-- Host: localhost    Database: phms
-- ------------------------------------------------------
-- Server version	5.7.35-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `api_log`
--

DROP TABLE IF EXISTS `api_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `api_log` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `request_url` varchar(255) DEFAULT NULL COMMENT '请求URL',
  `request_method` varchar(20) DEFAULT NULL COMMENT '请求方法',
  `request_params` text COMMENT '请求参数',
  `request_body` text COMMENT '请求体',
  `response_status` int(5) DEFAULT NULL COMMENT '响应状态码',
  `response_body` text COMMENT '响应体',
  `error_message` text COMMENT '错误信息',
  `ip_address` varchar(50) DEFAULT NULL COMMENT '请求IP地址',
  `user_agent` varchar(255) DEFAULT NULL COMMENT '用户代理',
  `execute_time` bigint(20) DEFAULT NULL COMMENT '执行时间（毫秒）',
  `status` int(5) DEFAULT NULL COMMENT '调用状态（1：成功，0：失败）',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `appointment_type`
--

DROP TABLE IF EXISTS `appointment_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `appointment_type` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '预约类型名称（如：看病、疫苗注射、洗澡、修毛等）',
  `description` varchar(255) DEFAULT NULL COMMENT '类型描述',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `doctor_service_type`
--

DROP TABLE IF EXISTS `doctor_service_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `doctor_service_type` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `doctor_id` bigint(20) NOT NULL COMMENT '医生ID',
  `type_id` bigint(20) NOT NULL COMMENT '预约/服务类型ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_doctor_type` (`doctor_id`,`type_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `doctor_schedule`
--

DROP TABLE IF EXISTS `doctor_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `doctor_schedule` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `doctor_id` bigint(20) NOT NULL COMMENT '医生ID',
  `week_day` tinyint(1) NOT NULL COMMENT '星期几（1-7，1=周一）',
  `time_slot` varchar(20) NOT NULL COMMENT '时间段，如：09:00-10:00',
  `max_appointments` int(11) NOT NULL DEFAULT '5' COMMENT '该时段最大可预约数量',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态：0-停用，1-启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_doctor_week_time` (`doctor_id`,`week_day`,`time_slot`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `appointment`
--

DROP TABLE IF EXISTS `appointment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `appointment` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `pet_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `doctor_id` bigint(20) DEFAULT NULL,
  `appointment_type_id` bigint(20) DEFAULT NULL COMMENT '预约类型ID',
  `app_time` datetime DEFAULT NULL,
  `time_slot` varchar(20) DEFAULT NULL COMMENT '预约时间段（如09:00-10:00）',
  `info` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `status` int(5) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `diagnosis`
--

DROP TABLE IF EXISTS `diagnosis`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `diagnosis` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `pet_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `doctor_id` bigint(20) DEFAULT NULL,
  `info` varchar(255) DEFAULT NULL,
  `type` int(5) DEFAULT NULL,
  `status` int(5) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `medicine`
--

DROP TABLE IF EXISTS `medicine`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `medicine` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `price` double(10,2) DEFAULT NULL,
  `stock` int(11) DEFAULT '0',
  `status` int(5) DEFAULT '1',
  `create_time` datetime DEFAULT NULL,
  `create_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `medicine_record`
--

DROP TABLE IF EXISTS `medicine_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `medicine_record` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `pet_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `doctor_id` bigint(20) DEFAULT NULL,
  `medicine_id` bigint(20) DEFAULT NULL,
  `quantity` int(11) DEFAULT NULL,
  `dosage` varchar(255) DEFAULT NULL,
  `usage` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `diagnosis_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notice`
--

DROP TABLE IF EXISTS `notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notice` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `content` varchar(255) DEFAULT NULL,
  `view_count` bigint(20) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pet`
--

DROP TABLE IF EXISTS `pet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pet` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `weight` double(10,2) DEFAULT NULL,
  `height` double(10,2) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `birthday` datetime DEFAULT NULL,
  `img` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `sex` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pet_daily`
--

DROP TABLE IF EXISTS `pet_daily`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pet_daily` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `pet_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `temperature` double(10,2) DEFAULT NULL,
  `weight` double(10,2) DEFAULT NULL,
  `height` double(10,2) DEFAULT NULL,
  `appetite` double(10,2) DEFAULT NULL,
  `status` int(5) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `standard`
--

DROP TABLE IF EXISTS `standard`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `standard` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `age_min` int(10) DEFAULT NULL,
  `age_max` int(10) DEFAULT NULL,
  `temp_min` double(10,2) DEFAULT NULL,
  `temp_max` double(10,2) DEFAULT NULL,
  `weight_min` double(10,2) DEFAULT NULL,
  `weight_max` double(10,2) DEFAULT NULL,
  `height_min` double(10,2) DEFAULT NULL,
  `height_max` double(10,2) DEFAULT NULL,
  `appetite_min` double(10,2) DEFAULT NULL,
  `appetite_max` double(10,2) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `status` int(5) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `age` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `id_card` varchar(255) DEFAULT NULL,
  `id_name` varchar(255) DEFAULT NULL,
  `qualification` varchar(255) DEFAULT NULL,
  `hospital_name` varchar(255) DEFAULT NULL,
  `hospital_address` varchar(255) DEFAULT NULL,
  `department` varchar(255) DEFAULT NULL,
  `info` varchar(255) DEFAULT NULL,
  `img` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `role` int(11) NOT NULL,
  `username` varchar(100) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;


-- 聊天消息表
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` bigint(20) NOT NULL COMMENT '会话ID',
  `sender_id` bigint(20) NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint(20) NOT NULL COMMENT '接收者ID',
  `message_type` varchar(20) NOT NULL DEFAULT 'text' COMMENT '消息类型（text=文字, emoji=表情, image=图片, file=文件）',
  `content` text COMMENT '消息内容（文字内容或文件路径）',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读（0=未读, 1=已读）',
  `is_revoked` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已撤回（0=未撤回, 1=已撤回）',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_session_id` (`session_id`) USING BTREE,
  KEY `idx_sender_id` (`sender_id`) USING BTREE,
  KEY `idx_receiver_id` (`receiver_id`) USING BTREE,
  KEY `idx_is_read` (`is_read`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE,
  KEY `idx_is_revoked` (`is_revoked`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=116 DEFAULT CHARSET=utf8 COMMENT='聊天消息表';

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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- ------------------------------------------------------
-- Test Data Inserts (for testing purpose only)
-- ------------------------------------------------------

-- Test data for `user` table
INSERT INTO phms.`user` (id, age, name, password, email, id_card, id_name, qualification, hospital_name, hospital_address, department, info, img, phone, address, create_time, `role`, username) VALUES
(52, NULL, 'doctor1', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 2, 'doctor'),
(53, NULL, 'user1', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 3, 'user'),
(54, NULL, '朱江', 'e10adc3949ba59abbe56e057f20f883e', '', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '19847147131', '', '2026-01-04 10:34:04', 3, 'zhujiang123'),
(55, NULL, '史明远', 'e10adc3949ba59abbe56e057f20f883e', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '18887894789', '', '2026-01-04 11:18:22', 3, 'smy'),
(57, NULL, '韩奇睿大傻逼', '7e7773a05e2ac718a7128e64dec599ef', '2771703766@qq.com', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '18561495254', '', '2026-01-04 14:38:49', 3, 'hqr');

-- Test data for `standard` table
INSERT INTO phms.standard (id, age_min, age_max, temp_min, temp_max, weight_min, weight_max, height_min, height_max, appetite_min, appetite_max, `type`, status) VALUES
(2, 1, 10, 30.0, 50.0, 10.0, 50.0, 5.0, 70.0, 10.0, 40.0, '1', 1),
(3, 5, 20, 15.0, 40.0, 10.0, 50.0, 12.0, 70.0, 10.0, 30.0, '2', 1),
(4, 1, 10, 10.0, 10.0, 20.0, 30.0, 40.0, 50.0, 10.0, 10.0, '2', 1);

-- Test data for `notice` table
INSERT INTO phms.notice (id, content, view_count, create_time, title) VALUES
(2, '<p>狗狗大些才能打狂犬疫苗，每年打一次。而且必须在狗狗熟悉了新环境，身体健康的情况下才能打，疫苗期间不能洗澡。狗狗还要定期驱虫，吃驱虫药就行。驱虫和疫苗不要同时进行，间隔一两个礼拜比较好。驱虫幼犬隔3个月一次，成年犬隔半年一次。</p><p>如果确定要给狗狗打疫苗，那每年注射是必要的，而且一般来说次年的疫苗应该比上一年的注射时间提早半个月到一个月，避免在疫苗快要失效的时候发生意外。</p>', 9, '2020-04-25 22:10:52', '宠物预防针多久打一次'),
(3, '<p>如何科学饲养宠物？</p><p>保障宠物健康的关键是合理的饮食、及时的体检与预防接种，以及足够的陪伴与运动。</p>', 7, '2020-04-26 11:35:09', '养犬最重要的是什么？'),
(5, '<p>养犬需要主人保持耐心、爱心与责任心，按时驱虫、疫苗，并提供适当的训练。</p>', 5, '2020-04-26 19:46:22', '养犬重要的是什么？'),
(6, '<p>宠物与主人之间的信任建立至关重要，需要长期陪伴与正向引导。</p>', 19, '2020-04-26 19:46:28', '养犬重要的是什么？'),
(8, '<p>宠物护理小知识集合。</p>', 11, '2020-05-30 10:37:36', '宠物');

-- Test data for `appointment_type` table
INSERT INTO phms.appointment_type (id, name, description, status, create_time) VALUES
(1, '看病', '门诊看诊/复查', 1, '2026-01-01 09:00:00'),
(2, '疫苗注射', '犬猫常规疫苗注射', 1, '2026-01-01 09:00:00'),
(3, '洗澡', '宠物清洁洗护服务', 1, '2026-01-01 09:00:00'),
(4, '修毛', '宠物美容修剪', 1, '2026-01-01 09:00:00');

-- Test data for `doctor_service_type` table (关联 user.role=2 的医生)
INSERT INTO phms.doctor_service_type (id, doctor_id, type_id, create_time) VALUES
(1, 52, 1, '2026-01-02 10:00:00'),
(2, 52, 2, '2026-01-02 10:00:00'),
(3, 52, 3, '2026-01-02 10:00:00');

-- Test data for `doctor_schedule` table
INSERT INTO phms.doctor_schedule (id, doctor_id, week_day, time_slot, max_appointments, status, create_time, update_time) VALUES
(1, 52, 1, '09:00-10:00', 5, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(2, 52, 1, '10:00-11:00', 5, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00'),
(3, 52, 3, '14:00-15:00', 5, 1, '2026-01-02 10:00:00', '2026-01-02 10:00:00');

-- Test data for `pet` table (关联 user.role=3 的用户)
INSERT INTO phms.pet (id, user_id, name, weight, height, type, birthday, img, create_time) VALUES
(48, 53, '豆豆', 8.50, 35.00, '狗', '2022-06-01 00:00:00', NULL, '2026-01-02 11:00:00'),
(49, 54, '团子', 4.20, 25.00, '猫', '2023-03-12 00:00:00', NULL, '2026-01-02 11:00:00');

-- Test data for `pet_daily` table (关联 pet)
INSERT INTO phms.pet_daily (id, pet_id, user_id, temperature, weight, height, appetite, status, create_time) VALUES
(37, 48, 53, 38.50, 8.60, 35.00, 25.00, 1, '2026-01-03 08:00:00'),
(38, 49, 54, 38.20, 4.10, 25.00, 18.00, 1, '2026-01-03 08:10:00');

-- Test data for `appointment` table (关联 pet/user/doctor/appointment_type 与 schedule 的 time_slot)
INSERT INTO phms.appointment (id, pet_id, user_id, doctor_id, appointment_type_id, app_time, time_slot, info, create_time, status, phone, address) VALUES
(51, 48, 53, 52, 1, '2026-01-06 09:00:00', '09:00-10:00', '持续咳嗽两天，来院检查', '2026-01-05 10:00:00', 1, '13800000001', 'xx路xx号'),
(52, 49, 54, 52, 2, '2026-01-06 10:00:00', '10:00-11:00', '首次疫苗注射', '2026-01-05 10:05:00', 1, '13800000002', 'yy路yy号');

-- Test data for `diagnosis` table (关联 appointment 对应 pet/user/doctor)
INSERT INTO phms.diagnosis (id, pet_id, user_id, doctor_id, info, type, status, create_time) VALUES
(23, 48, 53, 52, '上呼吸道感染，建议雾化+口服药', 1, 1, '2026-01-06 09:30:00'),
(24, 49, 54, 52, '健康，完成疫苗注射', 2, 1, '2026-01-06 10:20:00');

-- Test data for `medicine` table (create_by 关联医生)
INSERT INTO phms.medicine (id, name, type, description, price, stock, status, create_time, create_by) VALUES
(2, '阿莫西林', '抗生素', '用于细菌感染', 12.50, 200, 1, '2026-01-02 12:00:00', 52),
(3, '止咳糖浆', '对症药', '缓解咳嗽症状', 18.00, 100, 1, '2026-01-02 12:00:00', 52);

-- Test data for `medicine_record` table (关联 diagnosis/pet/user/doctor/medicine)
INSERT INTO phms.medicine_record (id, pet_id, user_id, doctor_id, medicine_id, quantity, dosage, usage, create_time, diagnosis_id) VALUES
(4, 48, 53, 52, 2, 10, '每次1片', '每日2次，饭后服用', '2026-01-06 09:35:00', 23),
(5, 48, 53, 52, 3, 1, '每次5ml', '每日3次', '2026-01-06 09:35:00', 23);

-- Test data for `api_log` table (关联 user)
INSERT INTO phms.api_log (id, request_url, request_method, request_params, request_body, response_status, response_body, error_message, ip_address, user_agent, execute_time, status, create_time, user_id) VALUES
(1, '/api/login', 'POST', NULL, '{"username":"doctor","password":"123456"}', 200, '{"code":0,"msg":"ok"}', NULL, '127.0.0.1', 'JUnit', 35, 1, '2026-01-05 10:10:00', 52),
(2, '/api/pet/list', 'GET', 'userId=53', NULL, 200, '{"code":0,"msg":"ok"}', NULL, '127.0.0.1', 'JUnit', 18, 1, '2026-01-05 10:12:00', 53);

-- Dumping routines for database 'phms'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-04 16:52:43
