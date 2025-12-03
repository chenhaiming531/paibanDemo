/*
 Navicat Premium Data Transfer

 Source Server         : ABC
 Source Server Type    : MySQL
 Source Server Version : 50560 (5.5.60)
 Source Host           : localhost:3305
 Source Schema         : paiban

 Target Server Type    : MySQL
 Target Server Version : 50560 (5.5.60)
 File Encoding         : 65001

 Date: 13/08/2025 09:57:24
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for roster_schedule_detail
-- ----------------------------
DROP TABLE IF EXISTS `roster_schedule_detail`;
CREATE TABLE `roster_schedule_detail`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `date` date NOT NULL COMMENT '排班日历',
  `room_id` bigint(20) NOT NULL COMMENT '房间ID',
  `staff_id` bigint(20) NOT NULL COMMENT '主班人员ID（医生/护士）',
  `secondary_staff_id` bigint(20) NULL DEFAULT NULL COMMENT '次班人员ID（进修人员）',
  `shift_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '班次类型（1白班 2夜班）',
  `is_adjusted` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '是否调整过（0否 1是）',
  `adjust_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '调整原因',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '排班明细表' ROW_FORMAT = Compact;

SET FOREIGN_KEY_CHECKS = 1;
