/*
 Navicat Premium Data Transfer

 Source Server         : chatgpt-data
 Source Server Type    : MySQL
 Source Server Version : 80400 (8.4.0)
 Source Host           : localhost:3306
 Source Schema         : openai

 Target Server Type    : MySQL
 Target Server Version : 80400 (8.4.0)
 File Encoding         : 65001

 Date: 11/12/2024 12:32:06
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user_account
-- ----------------------------
DROP TABLE IF EXISTS `user_account`;
CREATE TABLE `user_account`  (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增ID',
                                 `openid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID：微信ID',
                                 `total_quota` int NOT NULL COMMENT '总量额度：分配的总使用次数',
                                 `surplus_quota` int NOT NULL COMMENT '剩余额度：剩余的可使用次数',
                                 `model_types` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '可用模型：gpt-3.5-turbo,gpt-3.5-turbo,gpt-3.5-turbo-16k,gpt-4,gpt-4-32k\'',
  `status` tinyint(1) NOT NULL COMMENT '账户状态：0-可用、1-冻结',
  `creat_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id` DESC) USING BTREE,
  UNIQUE INDEX `uq_openid`(`openid` ASC) USING BTREE,
  INDEX `idx_surplus_quota_status`(`surplus_quota` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_account
-- ----------------------------
INSERT INTO `user_account` VALUES (1, 'lvy', 20, 20, 'gpt-3.5-turbo', 0, '2024-12-11 12:19:36', '2024-12-11 12:19:40');

SET FOREIGN_KEY_CHECKS = 1;
