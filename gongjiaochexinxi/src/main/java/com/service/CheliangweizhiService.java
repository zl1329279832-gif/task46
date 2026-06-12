package com.service;

import com.baomidou.mybatisplus.service.IService;
import com.utils.PageUtils;
import com.entity.CheliangweizhiEntity;
import com.entity.vo.BusLocationVO;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * 位置信息 服务类
 */
public interface CheliangweizhiService extends IService<CheliangweizhiEntity> {

    /**
    * @param params 查询参数
    * @return 带分页的查询出来的数据
    */
     PageUtils queryPage(Map<String, Object> params);

    /**
     * 按线路查询当前车辆实时位置列表
     * 处理场景：车辆位置过期、线路详情无法解析
     * @param gongjiaoxianluId 线路ID
     * @return 车辆实时位置列表
     */
     List<BusLocationVO> findBusLocationsByRoute(Integer gongjiaoxianluId);
}