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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `api_log`
--

LOCK TABLES `api_log` WRITE;
/*!40000 ALTER TABLE `api_log` DISABLE KEYS */;
INSERT INTO `api_log` VALUES (1,'/api/login','POST',NULL,'{\"username\":\"doctor\",\"password\":\"123456\"}',200,'{\"code\":0,\"msg\":\"ok\"}',NULL,'127.0.0.1','JUnit',35,1,'2026-01-05 10:10:00',52),(2,'/api/pet/list','GET','userId=53',NULL,200,'{\"code\":0,\"msg\":\"ok\"}',NULL,'127.0.0.1','JUnit',18,1,'2026-01-05 10:12:00',53);
/*!40000 ALTER TABLE `api_log` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=89 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `appointment`
--

LOCK TABLES `appointment` WRITE;
/*!40000 ALTER TABLE `appointment` DISABLE KEYS */;
INSERT INTO `appointment` VALUES (51,48,53,52,1,'2026-01-06 00:00:00','09:00-10:00','持续咳嗽两天，来院检查','2026-01-05 10:00:00',1,'13800000001','xx路xx号'),(52,49,54,52,2,'2026-01-06 00:00:00','10:00-11:00','首次疫苗注射','2026-01-05 10:05:00',1,'13800000002','yy路yy号'),(84,50,53,58,1,'2026-01-13 16:00:00','09:00-10:00','123','2026-01-13 19:12:23',4,'1231','23123'),(85,48,53,58,1,'2026-01-13 16:00:00','10:00-11:00','123','2026-01-13 19:13:20',1,'1231','23123'),(86,50,53,58,1,'2026-01-13 16:00:00','10:00-11:00','123','2026-01-13 19:33:51',1,'1231','23123'),(87,50,53,59,1,'2026-01-18 16:00:00','11:00-12:00','12','2026-01-16 18:03:51',1,'3123','123'),(88,48,53,52,1,'2026-01-16 16:00:00','08:00-09:00','123','2026-01-16 20:27:02',1,'1231','23123123');
/*!40000 ALTER TABLE `appointment` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `appointment_type`
--

LOCK TABLES `appointment_type` WRITE;
/*!40000 ALTER TABLE `appointment_type` DISABLE KEYS */;
INSERT INTO `appointment_type` VALUES (1,'看病','门诊看诊/复查',1,'2026-01-01 09:00:00'),(2,'疫苗注射','犬猫常规疫苗注射',1,'2026-01-01 09:00:00'),(3,'洗澡','宠物清洁洗护服务',1,'2026-01-01 09:00:00'),(4,'修毛','宠物美容修剪',1,'2026-01-01 09:00:00');
/*!40000 ALTER TABLE `appointment_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_message`
--

DROP TABLE IF EXISTS `chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chat_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` bigint(20) NOT NULL COMMENT '会话ID',
  `sender_id` bigint(20) NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint(20) NOT NULL COMMENT '接收者ID',
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'text' COMMENT '消息类型（text=文字, emoji=表情, image=图片, file=文件）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
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
) ENGINE=InnoDB AUTO_INCREMENT=134 DEFAULT CHARSET=utf8 COMMENT='聊天消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_message`
--

LOCK TABLES `chat_message` WRITE;
/*!40000 ALTER TABLE `chat_message` DISABLE KEYS */;
INSERT INTO `chat_message` VALUES (1,2,53,52,'text','1',1,0,'2026-01-13 08:10:08'),(2,2,53,52,'text','1',1,0,'2026-01-13 08:10:11'),(3,2,52,53,'text','1',1,0,'2026-01-13 08:10:27'),(4,2,52,53,'text','1',1,0,'2026-01-13 08:10:31'),(5,2,53,52,'text','1',1,0,'2026-01-13 08:11:06'),(6,2,53,52,'text','你好',1,0,'2026-01-13 08:11:55'),(7,2,52,53,'image','/file/023c6d4d-1ec5-4d6d-ac8f-b70ee6d59148.jpg',1,0,'2026-01-13 08:19:52'),(8,2,53,52,'text','1',1,0,'2026-01-13 09:07:36'),(9,1,53,58,'text','1',1,0,'2026-01-13 09:07:48'),(10,1,58,53,'text','1',1,0,'2026-01-13 09:08:24'),(11,1,53,58,'text','1',1,0,'2026-01-13 09:26:02'),(12,2,53,52,'image','/file/25b528cd-1fe3-41ab-9dfe-88d9e31f37a2.jpg',1,0,'2026-01-13 09:46:01'),(13,1,58,53,'text','你好？？？？？',1,0,'2026-01-13 10:30:07'),(14,1,53,58,'text','Qqqq',1,0,'2026-01-13 10:33:43'),(15,1,53,58,'text','1111',1,0,'2026-01-13 10:36:30'),(16,1,58,53,'text','11',1,0,'2026-01-13 10:41:44'),(17,1,58,53,'text','23',1,0,'2026-01-13 10:42:10'),(18,1,53,58,'text','11',1,0,'2026-01-13 10:42:15'),(19,1,53,58,'text','在吗',1,0,'2026-01-13 10:45:34'),(20,1,53,58,'text','？',1,0,'2026-01-13 10:45:46'),(21,1,58,53,'text','？',1,0,'2026-01-13 10:46:13'),(22,1,58,53,'text','测试消息1111111',1,0,'2026-01-13 10:47:01'),(23,1,53,58,'text','测试消息222222',1,0,'2026-01-13 10:47:10'),(24,1,58,53,'text','123',1,0,'2026-01-13 10:59:02'),(25,1,58,53,'text','123',1,0,'2026-01-13 10:59:08'),(26,1,58,53,'text','123',1,0,'2026-01-13 10:59:11'),(27,1,58,53,'text','123',1,0,'2026-01-13 10:59:12'),(28,1,58,53,'text','123123',1,0,'2026-01-13 10:59:12'),(29,1,58,53,'text','23',1,0,'2026-01-13 10:59:12'),(30,1,58,53,'text','123',1,0,'2026-01-13 10:59:12'),(31,1,58,53,'text','1',1,0,'2026-01-13 10:59:13'),(32,1,53,58,'text','1',1,0,'2026-01-13 11:00:21'),(33,1,53,58,'text','1',1,0,'2026-01-13 11:00:31'),(34,1,53,58,'text','1',1,0,'2026-01-13 11:00:33'),(35,1,58,53,'text','123',1,0,'2026-01-13 11:00:53'),(36,1,58,53,'text','123',1,0,'2026-01-13 11:00:53'),(37,1,58,53,'text','123123',1,0,'2026-01-13 11:00:53'),(38,1,58,53,'text','123',1,0,'2026-01-13 11:00:53'),(39,1,58,53,'text','123',1,0,'2026-01-13 11:00:54'),(40,1,58,53,'text','123',1,0,'2026-01-13 11:00:55'),(41,1,58,53,'text','12312',1,0,'2026-01-13 11:00:55'),(42,1,58,53,'text','1212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123121231212312123',1,0,'2026-01-13 11:01:04'),(43,1,53,58,'text','123',1,0,'2026-01-13 11:15:31'),(44,1,58,53,'text','123',1,0,'2026-01-13 11:15:33'),(45,1,53,58,'text','123123',1,0,'2026-01-13 11:15:34'),(46,1,58,53,'text','123123',1,0,'2026-01-13 11:15:36'),(47,1,53,58,'text','123123',1,0,'2026-01-13 11:15:38'),(48,1,58,53,'text','123123',1,0,'2026-01-13 11:15:39'),(49,1,53,58,'text','123123',1,0,'2026-01-13 11:15:40'),(50,1,58,53,'text','12323',1,0,'2026-01-13 11:15:42'),(51,1,53,58,'text','123123',1,0,'2026-01-13 11:15:44'),(52,1,58,53,'text','12312',1,0,'2026-01-13 11:15:46'),(53,1,53,58,'text','nihao',1,0,'2026-01-13 11:15:48'),(54,1,58,53,'text','在干嘛',1,0,'2026-01-13 11:15:53'),(55,1,53,58,'text','heih',1,0,'2026-01-13 11:15:56'),(56,1,53,58,'text','shabi',1,0,'2026-01-13 11:15:58'),(57,1,53,58,'text','qunima de',1,0,'2026-01-13 11:16:00'),(58,1,58,53,'text','滚蛋',1,0,'2026-01-13 11:16:04'),(59,1,58,53,'text','6',1,0,'2026-01-13 11:16:09'),(60,1,53,58,'text','6',1,0,'2026-01-13 11:16:17'),(61,1,58,53,'text','6',1,0,'2026-01-13 11:16:20'),(62,1,58,53,'text','1',1,0,'2026-01-13 11:26:35'),(63,1,53,58,'text','1',1,0,'2026-01-13 11:26:39'),(64,1,58,53,'text','3',1,0,'2026-01-13 11:26:51'),(65,1,58,53,'text','3',1,0,'2026-01-13 11:26:53'),(66,1,58,53,'text','3',1,0,'2026-01-13 11:26:55'),(67,1,58,53,'text','3',1,0,'2026-01-13 11:26:55'),(68,1,58,53,'text','3',1,0,'2026-01-13 11:26:56'),(69,1,58,53,'text','3',1,0,'2026-01-13 11:26:58'),(70,1,58,53,'text','3',1,0,'2026-01-13 11:26:59'),(71,1,53,58,'text','1',1,0,'2026-01-13 11:27:01'),(72,1,53,58,'text','1',1,0,'2026-01-13 11:27:04'),(73,1,53,58,'text','1',1,0,'2026-01-13 11:27:07'),(74,1,58,53,'text','1',1,0,'2026-01-13 11:27:41'),(75,1,58,53,'text','3',1,0,'2026-01-13 11:27:48'),(76,1,53,58,'text','2',1,0,'2026-01-13 11:27:55'),(77,1,53,58,'text','哎',1,0,'2026-01-13 11:28:21'),(78,1,58,53,'text','1',1,0,'2026-01-13 11:28:24'),(79,1,53,58,'text','1',1,0,'2026-01-13 11:28:47'),(80,1,53,58,'text','1',1,0,'2026-01-13 11:28:48'),(81,1,53,58,'text','1',1,0,'2026-01-13 11:28:49'),(82,1,58,53,'text','134',1,0,'2026-01-13 11:28:52'),(83,1,58,53,'text','234',1,0,'2026-01-13 11:28:55'),(84,2,53,52,'text','11',1,0,'2026-01-13 11:29:59'),(85,1,53,58,'text','1',1,0,'2026-01-13 11:30:06'),(86,1,58,53,'text','1',1,0,'2026-01-13 11:30:12'),(87,1,58,53,'text','2',1,0,'2026-01-13 11:30:14'),(88,1,53,58,'text','3',1,0,'2026-01-13 11:30:16'),(89,1,53,58,'text','23',1,0,'2026-01-13 11:30:18'),(90,1,53,58,'text','123123',1,0,'2026-01-13 11:30:21'),(91,1,58,53,'text','12312313',1,0,'2026-01-13 11:30:22'),(92,1,53,58,'text','123123',1,0,'2026-01-13 11:30:24'),(93,1,58,53,'text','123213123',1,0,'2026-01-13 11:30:25'),(94,1,53,58,'text','123123',1,0,'2026-01-13 11:30:27'),(95,1,58,53,'text','123123',1,0,'2026-01-13 11:30:28'),(96,1,53,58,'text','12312',1,0,'2026-01-13 11:30:30'),(97,1,58,53,'text','12313',1,0,'2026-01-13 11:30:31'),(98,1,53,58,'text','123123',1,0,'2026-01-13 11:30:33'),(99,1,58,53,'text','123123',1,0,'2026-01-13 11:30:34'),(100,1,53,58,'text','123123',1,0,'2026-01-13 11:30:36'),(101,1,58,53,'text','123131',1,0,'2026-01-13 11:30:37'),(102,1,53,58,'text','123',1,0,'2026-01-13 11:31:55'),(103,1,58,53,'text','1231',1,0,'2026-01-13 11:31:58'),(104,1,58,53,'image','/file/156df489-9049-4433-87cc-2475bf40e142.jpg',1,0,'2026-01-13 11:35:12'),(105,2,53,52,'text','1',1,0,'2026-01-13 11:39:05'),(106,2,52,53,'text','123',1,0,'2026-01-13 11:39:08'),(107,2,53,52,'text','123',1,0,'2026-01-13 11:39:27'),(108,2,52,53,'text','123',1,0,'2026-01-13 11:39:29'),(109,2,52,53,'text','123',1,0,'2026-01-13 11:39:45'),(110,2,53,52,'text','123123',1,0,'2026-01-13 11:39:57'),(111,2,52,53,'text','111',1,0,'2026-01-13 11:58:13'),(112,2,52,53,'image','/file/33f5bffb-439d-4cd7-9d58-daa50b10e5a1.jpg',1,0,'2026-01-13 11:59:05'),(113,2,52,53,'text','123',1,0,'2026-01-13 12:03:59'),(114,2,52,53,'text','123123',1,0,'2026-01-13 12:04:15'),(115,2,52,53,'text','123123',1,0,'2026-01-13 12:04:29'),(116,2,52,53,'text','请问',1,1,'2026-01-13 12:08:17'),(117,2,52,53,'image','/file/5069d0a5-2de4-4f15-aac9-5917f0254683.jpg',1,1,'2026-01-15 21:17:42'),(118,2,53,52,'text','1',1,0,'2026-01-16 08:58:37'),(119,2,52,53,'text','111',1,0,'2026-01-16 11:21:03'),(120,2,52,53,'text','111',1,0,'2026-01-16 11:21:33'),(121,1,58,53,'text','123',1,0,'2026-01-16 11:47:36'),(122,1,58,53,'text','12123123',1,0,'2026-01-16 11:48:08'),(123,1,53,58,'text','123123',1,0,'2026-01-16 11:49:36'),(124,1,58,53,'text','123123',1,0,'2026-01-16 11:49:37'),(125,1,53,58,'text','123123',1,0,'2026-01-16 11:49:43'),(126,1,53,58,'text','123123',1,0,'2026-01-16 11:49:48'),(127,1,58,53,'text','123123',1,0,'2026-01-16 11:50:13'),(128,2,53,52,'text','123123',1,0,'2026-01-16 11:51:06'),(129,1,53,58,'text','123123',1,0,'2026-01-16 11:51:14'),(130,2,52,53,'image','/file/ec384ade-c5d3-41f8-ade2-3e48e0881297.png',1,0,'2026-01-16 12:17:14'),(131,2,52,53,'text','123123',1,0,'2026-01-16 12:29:45'),(132,2,53,52,'text','你好',1,1,'2026-01-16 12:30:08'),(133,2,52,53,'text','nihao',1,0,'2026-01-16 12:30:16');
/*!40000 ALTER TABLE `chat_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_request`
--

DROP TABLE IF EXISTS `chat_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chat_request` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `doctor_id` bigint(20) NOT NULL COMMENT '医生ID',
  `status` tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态（0=待审核, 1=已同意, 2=已拒绝）',
  `request_message` varchar(500) DEFAULT NULL COMMENT '申请留言',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_doctor_id` (`doctor_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COMMENT='聊天申请表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_request`
--

LOCK TABLES `chat_request` WRITE;
/*!40000 ALTER TABLE `chat_request` DISABLE KEYS */;
INSERT INTO `chat_request` VALUES (1,53,58,1,'123','2026-01-13 07:05:10','2026-01-13 15:20:37'),(2,53,52,1,'1323','2026-01-13 08:09:36','2026-01-13 16:09:51'),(3,53,59,0,'','2026-01-13 08:10:37','2026-01-13 08:10:37');
/*!40000 ALTER TABLE `chat_request` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_session`
--

DROP TABLE IF EXISTS `chat_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `chat_session` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `doctor_id` bigint(20) NOT NULL COMMENT '医生ID',
  `request_id` bigint(20) DEFAULT NULL COMMENT '关联的申请ID',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态（0=已关闭, 1=进行中）',
  `last_message_time` timestamp NULL DEFAULT NULL COMMENT '最后消息时间',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_doctor` (`user_id`,`doctor_id`) USING BTREE COMMENT '用户和医生唯一会话',
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_doctor_id` (`doctor_id`) USING BTREE,
  KEY `idx_request_id` (`request_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_last_message_time` (`last_message_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COMMENT='聊天会话表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_session`
--

LOCK TABLES `chat_session` WRITE;
/*!40000 ALTER TABLE `chat_session` DISABLE KEYS */;
INSERT INTO `chat_session` VALUES (1,53,58,1,1,'2026-01-16 19:51:13','2026-01-13 07:20:38'),(2,53,52,2,1,'2026-01-16 20:30:16','2026-01-13 08:09:51');
/*!40000 ALTER TABLE `chat_session` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `diagnosis`
--

LOCK TABLES `diagnosis` WRITE;
/*!40000 ALTER TABLE `diagnosis` DISABLE KEYS */;
INSERT INTO `diagnosis` VALUES (23,48,53,52,'上呼吸道感染，建议雾化+口服药',1,1,'2026-01-06 09:30:00'),(24,49,54,52,'健康，完成疫苗注射',2,1,'2026-01-06 10:20:00');
/*!40000 ALTER TABLE `diagnosis` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=296 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `doctor_schedule`
--

LOCK TABLES `doctor_schedule` WRITE;
/*!40000 ALTER TABLE `doctor_schedule` DISABLE KEYS */;
INSERT INTO `doctor_schedule` VALUES (189,58,1,'09:00-10:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(190,58,1,'10:00-11:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(191,58,1,'11:00-12:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(192,58,1,'14:00-15:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(193,58,1,'15:00-16:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(194,58,1,'16:00-17:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(195,58,2,'09:00-10:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(196,58,2,'10:00-11:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(197,58,2,'11:00-12:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(198,58,2,'14:00-15:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(199,58,2,'15:00-16:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(200,58,2,'16:00-17:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(201,58,3,'09:00-10:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(202,58,3,'10:00-11:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(203,58,3,'11:00-12:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(204,58,3,'14:00-15:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(205,58,3,'15:00-16:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(206,58,3,'16:00-17:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(207,58,4,'09:00-10:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(208,58,4,'10:00-11:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(209,58,4,'11:00-12:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(210,58,4,'14:00-15:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(211,58,4,'15:00-16:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(212,58,4,'16:00-17:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(213,58,5,'09:00-10:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(214,58,5,'10:00-11:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(215,58,5,'11:00-12:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(216,58,5,'14:00-15:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(217,58,5,'15:00-16:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(218,58,5,'16:00-17:00',5,1,'2026-01-11 18:07:30','2026-01-11 18:07:30'),(220,59,1,'10:00-11:00',5,1,'2026-01-12 15:00:54','2026-01-12 15:00:54'),(221,59,1,'11:00-12:00',5,1,'2026-01-12 15:00:54','2026-01-12 15:00:54'),(222,59,1,'15:00-16:00',5,1,'2026-01-12 15:00:54','2026-01-12 15:00:54'),(223,59,1,'16:00-17:00',5,1,'2026-01-12 15:00:54','2026-01-12 15:00:54'),(224,59,2,'10:00-11:00',5,1,'2026-01-12 15:00:54','2026-01-12 15:00:54'),(225,59,2,'11:00-12:00',5,1,'2026-01-12 15:00:54','2026-01-12 15:00:54'),(226,59,2,'15:00-16:00',5,1,'2026-01-12 15:00:54','2026-01-12 15:00:54'),(227,59,2,'16:00-17:00',5,1,'2026-01-12 15:00:54','2026-01-12 15:00:54'),(228,59,3,'10:00-11:00',5,1,'2026-01-12 15:00:54','2026-01-12 15:00:54'),(229,59,3,'11:00-12:00',5,1,'2026-01-12 15:00:54','2026-01-12 15:00:54'),(230,59,3,'15:00-16:00',5,1,'2026-01-12 15:00:54','2026-01-12 15:00:54'),(231,59,3,'16:00-17:00',5,1,'2026-01-12 15:00:54','2026-01-12 15:00:54'),(232,59,4,'10:00-11:00',5,1,'2026-01-12 15:00:54','2026-01-12 15:00:54'),(233,59,4,'11:00-12:00',5,1,'2026-01-12 15:00:54','2026-01-12 15:00:54'),(234,59,4,'15:00-16:00',5,1,'2026-01-12 15:00:54','2026-01-12 15:00:54'),(235,59,4,'16:00-17:00',5,1,'2026-01-12 15:00:54','2026-01-12 15:00:54'),(236,59,5,'10:00-11:00',5,1,'2026-01-12 15:00:54','2026-01-12 15:00:54'),(237,59,5,'11:00-12:00',5,1,'2026-01-12 15:00:54','2026-01-12 15:00:54'),(238,59,5,'15:00-16:00',5,1,'2026-01-12 15:00:54','2026-01-12 15:00:54'),(239,59,5,'16:00-17:00',5,1,'2026-01-12 15:00:54','2026-01-12 15:00:54'),(240,52,1,'08:00-09:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(241,52,1,'09:00-10:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(242,52,1,'10:00-11:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(243,52,1,'11:00-12:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(244,52,1,'14:00-15:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(245,52,1,'15:00-16:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(246,52,1,'16:00-17:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(247,52,1,'17:00-18:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(248,52,2,'08:00-09:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(249,52,2,'09:00-10:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(250,52,2,'10:00-11:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(251,52,2,'11:00-12:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(252,52,2,'14:00-15:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(253,52,2,'15:00-16:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(254,52,2,'16:00-17:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(255,52,2,'17:00-18:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(256,52,3,'08:00-09:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(257,52,3,'09:00-10:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(258,52,3,'10:00-11:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(259,52,3,'11:00-12:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(260,52,3,'14:00-15:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(261,52,3,'15:00-16:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(262,52,3,'16:00-17:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(263,52,3,'17:00-18:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(264,52,4,'08:00-09:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(265,52,4,'09:00-10:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(266,52,4,'10:00-11:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(267,52,4,'11:00-12:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(268,52,4,'14:00-15:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(269,52,4,'15:00-16:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(270,52,4,'16:00-17:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(271,52,4,'17:00-18:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(272,52,5,'08:00-09:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(273,52,5,'09:00-10:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(274,52,5,'10:00-11:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(275,52,5,'11:00-12:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(276,52,5,'14:00-15:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(277,52,5,'15:00-16:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(278,52,5,'16:00-17:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(279,52,5,'17:00-18:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(280,52,6,'08:00-09:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(281,52,6,'09:00-10:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(282,52,6,'10:00-11:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(283,52,6,'11:00-12:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(284,52,6,'14:00-15:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(285,52,6,'15:00-16:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(286,52,6,'16:00-17:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(287,52,6,'17:00-18:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(288,52,7,'08:00-09:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(289,52,7,'09:00-10:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(290,52,7,'10:00-11:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(291,52,7,'11:00-12:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(292,52,7,'14:00-15:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(293,52,7,'15:00-16:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(294,52,7,'16:00-17:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19'),(295,52,7,'17:00-18:00',5,1,'2026-01-16 20:26:19','2026-01-16 20:26:19');
/*!40000 ALTER TABLE `doctor_schedule` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `doctor_service_type`
--

LOCK TABLES `doctor_service_type` WRITE;
/*!40000 ALTER TABLE `doctor_service_type` DISABLE KEYS */;
INSERT INTO `doctor_service_type` VALUES (19,58,1,'2026-01-11 10:38:48'),(20,52,1,'2026-01-11 17:16:59'),(21,52,2,'2026-01-11 17:16:59'),(22,59,1,'2026-01-11 18:21:07');
/*!40000 ALTER TABLE `doctor_service_type` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medicine`
--

LOCK TABLES `medicine` WRITE;
/*!40000 ALTER TABLE `medicine` DISABLE KEYS */;
INSERT INTO `medicine` VALUES (2,'阿莫西林','抗生素','用于细菌感染',12.50,200,1,'2026-01-02 12:00:00',52),(3,'止咳糖浆','对症药','缓解咳嗽症状',18.00,100,1,'2026-01-02 12:00:00',52),(4,'1','1','1',1.00,1,1,'2026-01-13 09:05:06',52);
/*!40000 ALTER TABLE `medicine` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medicine_record`
--

LOCK TABLES `medicine_record` WRITE;
/*!40000 ALTER TABLE `medicine_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `medicine_record` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notice`
--

LOCK TABLES `notice` WRITE;
/*!40000 ALTER TABLE `notice` DISABLE KEYS */;
INSERT INTO `notice` VALUES (2,'<p>狗狗大些才能打狂犬疫苗，每年打一次。而且必须在狗狗熟悉了新环境，身体健康的情况下才能打，疫苗期间不能洗澡。狗狗还要定期驱虫，吃驱虫药就行。驱虫和疫苗不要同时进行，间隔一两个礼拜比较好。驱虫幼犬隔3个月一次，成年犬隔半年一次。</p><p>如果确定要给狗狗打疫苗，那每年注射是必要的，而且一般来说次年的疫苗应该比上一年的注射时间提早半个月到一个月，避免在疫苗快要失效的时候发生意外。</p>',9,'2020-04-25 22:10:52','宠物预防针多久打一次'),(3,'<p>如何科学饲养宠物？</p><p>保障宠物健康的关键是合理的饮食、及时的体检与预防接种，以及足够的陪伴与运动。</p>',11,'2020-04-26 11:35:09','养犬最重要的是什么？'),(5,'<p>养犬需要主人保持耐心、爱心与责任心，按时驱虫、疫苗，并提供适当的训练。</p>',6,'2020-04-26 19:46:22','养犬重要的是什么？'),(6,'<p>宠物与主人之间的信任建立至关重要，需要长期陪伴与正向引导。</p>',26,'2020-04-26 19:46:28','养犬重要的是什么？'),(8,'<p>宠物护理小知识集合。</p>',25,'2020-05-30 10:37:36','宠物');
/*!40000 ALTER TABLE `notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification_message`
--

DROP TABLE IF EXISTS `notification_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notification_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `receiver_id` bigint(20) NOT NULL COMMENT '接收者ID（医生ID）',
  `sender_id` bigint(20) DEFAULT NULL COMMENT '发送者ID（用户ID）',
  `appointment_id` bigint(20) DEFAULT NULL COMMENT '预约ID（关联appointment表）',
  `type` varchar(50) NOT NULL DEFAULT 'APPOINTMENT' COMMENT '消息类型（如"APPOINTMENT"）',
  `title` varchar(200) NOT NULL COMMENT '消息标题',
  `content` text COMMENT '消息内容（JSON格式，包含用户姓名、时间段、预约类型等）',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读（0=未读，1=已读）',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_receiver_id` (`receiver_id`) USING BTREE,
  KEY `idx_is_read` (`is_read`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE,
  KEY `idx_appointment_id` (`appointment_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8 COMMENT='预约消息提醒表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification_message`
--

LOCK TABLES `notification_message` WRITE;
/*!40000 ALTER TABLE `notification_message` DISABLE KEYS */;
INSERT INTO `notification_message` VALUES (11,52,53,83,'APPOINTMENT','新预约通知','{\"timeSlot\":\"11:00-12:00\",\"appointmentTypeName\":\"看病\",\"appDate\":\"2026-01-15\",\"userName\":\"user1\",\"info\":\"123\"}',1,'2026-01-13 08:03:24'),(12,58,53,84,'APPOINTMENT','新预约通知','{\"timeSlot\":\"09:00-10:00\",\"appointmentTypeName\":\"看病\",\"appDate\":\"2026-01-14\",\"userName\":\"user1\",\"info\":\"123\"}',1,'2026-01-13 11:12:23'),(13,58,53,85,'APPOINTMENT','新预约通知','{\"timeSlot\":\"10:00-11:00\",\"appointmentTypeName\":\"看病\",\"appDate\":\"2026-01-14\",\"userName\":\"user1\",\"info\":\"123\"}',1,'2026-01-13 11:13:20'),(14,58,53,86,'APPOINTMENT','新预约通知','{\"timeSlot\":\"10:00-11:00\",\"appointmentTypeName\":\"看病\",\"appDate\":\"2026-01-14\",\"userName\":\"user1\",\"info\":\"123\"}',1,'2026-01-13 11:33:51'),(15,59,53,87,'APPOINTMENT','新预约通知','{\"timeSlot\":\"11:00-12:00\",\"appointmentTypeName\":\"看病\",\"appDate\":\"2026-01-19\",\"userName\":\"user1\",\"info\":\"12\"}',0,'2026-01-16 10:03:51'),(16,59,54,NULL,'BROADCAST','123','{\"message\":\"123\",\"type\":\"BROADCAST\"}',0,'2026-01-16 11:33:08'),(17,58,54,NULL,'BROADCAST','123','{\"message\":\"123\",\"type\":\"BROADCAST\"}',1,'2026-01-16 11:33:08'),(18,52,54,NULL,'BROADCAST','123','{\"message\":\"123\",\"type\":\"BROADCAST\"}',1,'2026-01-16 11:33:08'),(19,57,54,NULL,'BROADCAST','123','{\"message\":\"123\",\"type\":\"BROADCAST\"}',0,'2026-01-16 11:33:08'),(20,55,54,NULL,'BROADCAST','123','{\"message\":\"123\",\"type\":\"BROADCAST\"}',0,'2026-01-16 11:33:08'),(21,53,54,NULL,'BROADCAST','123','{\"message\":\"123\",\"type\":\"BROADCAST\"}',1,'2026-01-16 11:33:08'),(22,59,54,NULL,'BROADCAST','3434','{\"message\":\"343434\",\"type\":\"BROADCAST\"}',0,'2026-01-16 11:49:02'),(23,58,54,NULL,'BROADCAST','3434','{\"message\":\"343434\",\"type\":\"BROADCAST\"}',1,'2026-01-16 11:49:02'),(24,52,54,NULL,'BROADCAST','3434','{\"message\":\"343434\",\"type\":\"BROADCAST\"}',1,'2026-01-16 11:49:02'),(25,57,54,NULL,'BROADCAST','3434','{\"message\":\"343434\",\"type\":\"BROADCAST\"}',0,'2026-01-16 11:49:02'),(26,55,54,NULL,'BROADCAST','3434','{\"message\":\"343434\",\"type\":\"BROADCAST\"}',0,'2026-01-16 11:49:02'),(27,53,54,NULL,'BROADCAST','3434','{\"message\":\"343434\",\"type\":\"BROADCAST\"}',1,'2026-01-16 11:49:02'),(28,52,53,88,'APPOINTMENT','新预约通知','{\"timeSlot\":\"08:00-09:00\",\"appointmentTypeName\":\"看病\",\"appDate\":\"2026-01-17\",\"userName\":\"user1\",\"info\":\"123\"}',1,'2026-01-16 12:27:02');
/*!40000 ALTER TABLE `notification_message` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pet`
--

LOCK TABLES `pet` WRITE;
/*!40000 ALTER TABLE `pet` DISABLE KEYS */;
INSERT INTO `pet` VALUES (48,53,'豆豆',8.50,35.00,'狗','2022-06-01 00:00:00',NULL,'2026-01-02 11:00:00','1'),(49,54,'团子',4.20,25.00,'猫','2023-03-12 00:00:00',NULL,'2026-01-02 11:00:00','2'),(50,53,'beike',1.00,2.00,'1','2025-12-31 16:00:00',NULL,'2026-01-11 11:30:47','3');
/*!40000 ALTER TABLE `pet` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pet_daily`
--

LOCK TABLES `pet_daily` WRITE;
/*!40000 ALTER TABLE `pet_daily` DISABLE KEYS */;
INSERT INTO `pet_daily` VALUES (37,48,53,38.50,8.60,35.00,25.00,1,'2026-01-03 08:00:00'),(38,49,54,38.20,4.10,25.00,18.00,1,'2026-01-03 08:10:00');
/*!40000 ALTER TABLE `pet_daily` ENABLE KEYS */;
UNLOCK TABLES;

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
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `standard`
--

LOCK TABLES `standard` WRITE;
/*!40000 ALTER TABLE `standard` DISABLE KEYS */;
INSERT INTO `standard` VALUES (2,1,10,30.00,50.00,10.00,50.00,5.00,70.00,10.00,40.00,'1',1),(3,5,20,15.00,40.00,10.00,50.00,12.00,70.00,10.00,30.00,'2',1),(4,1,10,10.00,10.00,20.00,30.00,40.00,50.00,10.00,10.00,'2',1);
/*!40000 ALTER TABLE `standard` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=60 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (52,NULL,'doctor1','e10adc3949ba59abbe56e057f20f883e',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,2,'doctor'),(53,NULL,'user1','e10adc3949ba59abbe56e057f20f883e','',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'15555555552','5',NULL,3,'user'),(54,NULL,'朱江','e10adc3949ba59abbe56e057f20f883e','',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'19847147131','','2026-01-04 10:34:04',1,'admin'),(55,NULL,'史明远','e10adc3949ba59abbe56e057f20f883e',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'18887894789','','2026-01-04 11:18:22',3,'smy'),(57,NULL,'韩奇睿大傻逼','e10adc3949ba59abbe56e057f20f883e','2771703767@qq.com',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'18561495254','','2026-01-04 14:38:49',3,'hqr'),(58,NULL,'史明远','e10adc3949ba59abbe56e057f20f883e',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'18888888888','18888888888','2026-01-11 10:38:15',2,'doctor2'),(59,NULL,'111','e10adc3949ba59abbe56e057f20f883e',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'111','11','2026-01-11 18:20:21',2,'d');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
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

-- Dump completed on 2026-01-17  5:11:34
