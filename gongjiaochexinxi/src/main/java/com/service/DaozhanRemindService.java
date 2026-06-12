package com.service;

import com.baomidou.mybatisplus.service.IService;
import com.utils.PageUtils;
import com.entity.DaozhanRemindEntity;
import java.util.Map;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * 到站提醒订阅 服务类
 */
public interface DaozhanRemindService extends IService<DaozhanRemindEntity> {

    /**
    * @param params 查询参数
    * @return 带分页的查询出来的数据
    */
     PageUtils queryPage(Map<String, Object> params);

    /**
     * 订阅到站提醒
     * @param yonghuId 用户ID
     * @param gongjiaoxianluId 线路ID
     * @param stopName 站点名称
     * @return 操作结果信息
     */
     String subscribe(Integer yonghuId, Integer gongjiaoxianluId, String stopName);

    /**
     * 取消订阅
     * @param id 订阅ID
     * @param yonghuId 当前用户ID（防越权）
     * @return 是否成功
     */
     boolean cancel(Integer id, Integer yonghuId);

    /**
     * 查询用户的所有订阅
     * @param yonghuId 用户ID
     * @return 订阅列表
     */
     List<DaozhanRemindEntity> listByYonghu(Integer yonghuId);

    /**
     * 根据线路和站点查找未触发的活跃订阅，并触发提醒
     * @param gongjiaoxianluId 线路ID
     * @param stopName 当前车辆下一站名称
     * @return 被触发的订阅数量
     */
     int checkAndTriggerReminders(Integer gongjiaoxianluId, String stopName);

    /**
     * 查询当前用户被触发的提醒
     * @param yonghuId 用户ID
     * @return 已触发的提醒列表
     */
     List<DaozhanRemindEntity> listAlerts(Integer yonghuId);
}
