-- 到站订阅表
CREATE TABLE IF NOT EXISTS `daozhandingyue` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `yonghu_id` int(11) DEFAULT NULL COMMENT '用户id',
  `gongjiaoxianlu_id` int(11) DEFAULT NULL COMMENT '线路id',
  `zhandian_name` varchar(200) DEFAULT NULL COMMENT '站点名称',
  `dingyue_status` int(11) DEFAULT 1 COMMENT '订阅状态 1=订阅中 2=已触发 3=已取消',
  `tixing_status` int(11) DEFAULT 0 COMMENT '提醒状态 0=未提醒 1=已提醒',
  `tixing_content` varchar(500) DEFAULT NULL COMMENT '提醒信息',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_yonghu_id` (`yonghu_id`),
  KEY `idx_gongjiaoxianlu_id` (`gongjiaoxianlu_id`),
  KEY `idx_dingyue_status` (`dingyue_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='到站订阅';

-- 车辆位置表增加线路ID字段
ALTER TABLE `cheliangweizhi` ADD COLUMN `gongjiaoxianlu_id` int(11) DEFAULT NULL COMMENT '所属线路' AFTER `gongjiaoche_id`;
ALTER TABLE `cheliangweizhi` ADD INDEX `idx_gongjiaoxianlu_id` (`gongjiaoxianlu_id`);
