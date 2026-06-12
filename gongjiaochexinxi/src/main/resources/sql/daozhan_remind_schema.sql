-- ============================================
-- 乘客实时查车与到站提醒 数据库变更脚本
-- 功能：新增到站提醒订阅表 + 位置信息表增加线路外键
-- ============================================

-- 1. 位置信息表新增所属线路字段（关联公交线路）
ALTER TABLE cheliangweizhi ADD COLUMN gongjiaoxianlu_id INT NULL COMMENT '所属线路ID' AFTER gongjiaoche_id;
ALTER TABLE cheliangweizhi ADD INDEX idx_cheliangweizhi_gongjiaoxianlu (gongjiaoxianlu_id);

-- 2. 到站提醒订阅表
CREATE TABLE `to_station_remind` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `yonghu_id` INT NOT NULL COMMENT '用户ID',
  `gongjiaoxianlu_id` INT NOT NULL COMMENT '线路ID',
  `stop_name` VARCHAR(200) NOT NULL COMMENT '订阅站点名称',
  `remind_status` INT DEFAULT 0 COMMENT '提醒状态 0未触发 1已触发',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间（订阅时间）',
  `trigger_time` DATETIME DEFAULT NULL COMMENT '触发时间',
  PRIMARY KEY (`id`),
  INDEX `idx_remind_yonghu` (`yonghu_id`),
  INDEX `idx_remind_xianlu` (`gongjiaoxianlu_id`),
  INDEX `idx_remind_status` (`remind_status`),
  INDEX `idx_remind_yonghu_xianlu_stop` (`yonghu_id`, `gongjiaoxianlu_id`, `stop_name`, `remind_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='到站提醒订阅';
