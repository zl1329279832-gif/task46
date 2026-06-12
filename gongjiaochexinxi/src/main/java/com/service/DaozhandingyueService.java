package com.service;

import com.baomidou.mybatisplus.service.IService;
import com.utils.PageUtils;
import com.entity.DaozhandingyueEntity;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * 到站订阅 服务类
 */
public interface DaozhandingyueService extends IService<DaozhandingyueEntity> {

    /**
    * @param params 查询参数
    * @return 带分页的查询出来的数据
    */
     PageUtils queryPage(Map<String, Object> params);

    /**
     * 检查并触发到站提醒
     * @param gongjiaoxianluId 线路id
     * @param nextStationName 下一站名称
     * @param gongjiaocheName 车辆编号
     */
     void checkAndTriggerArrivalNotify(Integer gongjiaoxianluId, String nextStationName, String gongjiaocheName);
}
