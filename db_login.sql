/*
Navicat MySQL Data Transfer

Source Server         : 11
Source Server Version : 50623
Source Host           : 192.168.0.235:3306
Source Database       : db_login

Target Server Type    : MYSQL
Target Server Version : 50623
File Encoding         : 65001

Date: 2015-03-16 18:01:43
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for test
-- ----------------------------
DROP TABLE IF EXISTS `test`;
CREATE TABLE `test` (
  `test` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`test`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of test
-- ----------------------------
INSERT INTO `test` VALUES ('0000000011');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` int(10) NOT NULL,
  `username` varchar(10) NOT NULL,
  `password` varchar(200) NOT NULL,
  `lastServerId` int(10) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`,`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES ('1', '11', '6512bd43d9caa6e02c990b0a82652dca', '1');
INSERT INTO `users` VALUES ('2', 'gg', '73c18c59a39b18382081ec00bb456d43', '1');
INSERT INTO `users` VALUES ('3', '33', '182be0c5cdcd5072bb1864cdee4d3d6e', '1');
INSERT INTO `users` VALUES ('4', 'ggg', 'ba248c985ace94863880921d8900c53f', '0');
