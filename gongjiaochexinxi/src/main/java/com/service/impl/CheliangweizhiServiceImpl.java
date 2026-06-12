package com.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.lang.reflect.Field;
import java.util.*;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import com.utils.PageUtils;
import com.utils.Query;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import com.dao.CheliangweizhiDao;
import com.entity.CheliangweizhiEntity;
import com.entity.GongjiaoxianluEntity;
import com.entity.GongjiaocheEntity;
import com.entity.vo.BusLocationVO;
import com.service.CheliangweizhiService;
import com.service.GongjiaoxianluService;
import com.service.GongjiaocheService;
import com.entity.view.CheliangweizhiView;

/**
 * 位置信息 服务实现类
 */
@Service("cheliangweizhiService")
@Transactional
public class CheliangweizhiServiceImpl extends ServiceImpl<CheliangweizhiDao, CheliangweizhiEntity> implements CheliangweizhiService {

    @Autowired
    private GongjiaoxianluService gongjiaoxianluService;

    @Autowired
    private GongjiaocheService gongjiaocheService;

    /** 位置过期阈值（毫秒），默认30分钟 */
    private static final long POSITION_EXPIRE_MILLIS = 30 * 60 * 1000L;

    /** 每站预估行驶时间（分钟） */
    private static final int MINUTES_PER_STOP = 3;

    @Override
    public PageUtils queryPage(Map<String,Object> params) {
        if(params != null && (params.get("limit") == null || params.get("page") == null)){
            params.put("page","1");
            params.put("limit","10");
        }
        Page<CheliangweizhiView> page =new Query<CheliangweizhiView>(params).getPage();
        page.setRecords(baseMapper.selectListView(page,params));
        return new PageUtils(page);
    }

    /**
     * 按线路查询当前车辆实时位置列表
     *
     * 处理场景：
     * 1. 车辆位置过期 - 标记expired=true，estimatedMinutes设为null
     * 2. 线路详情无法解析 - estimatedMinutes设为null，不影响其他字段返回
     * 3. 线路无关联车辆 - 返回空列表
     */
    @Override
    public List<BusLocationVO> findBusLocationsByRoute(Integer gongjiaoxianluId) {
        List<BusLocationVO> result = new ArrayList<>();

        if(gongjiaoxianluId == null){
            return result;
        }

        // 查询线路信息
        GongjiaoxianluEntity route = gongjiaoxianluService.selectById(gongjiaoxianluId);
        if(route == null){
            return result;
        }

        // 解析线路全程站点列表，用于计算预计到站时间
        List<String> routeStops = parseRouteStops(route.getQuancheng());

        // 查询该线路下所有车辆位置
        Wrapper<CheliangweizhiEntity> queryWrapper = new EntityWrapper<CheliangweizhiEntity>()
            .eq("gongjiaoxianlu_id", gongjiaoxianluId);
        List<CheliangweizhiEntity> locations = this.selectList(queryWrapper);

        Date now = new Date();

        for(CheliangweizhiEntity loc : locations){
            BusLocationVO vo = new BusLocationVO();
            vo.setCheliangweizhiId(loc.getId());
            vo.setGongjiaocheId(loc.getGongjiaocheId());
            vo.setCheliangweizhiDati(loc.getCheliangweizhiDati());
            vo.setCheliangweizhiFangxiang(loc.getCheliangweizhiFangxiang());
            vo.setCheliangweizhiMingcheng(loc.getCheliangweizhiMingcheng());
            vo.setUpdateTime(loc.getCreateTime());
            vo.setGongjiaoxianluId(gongjiaoxianluId);
            vo.setGongjiaoxianluName(route.getGongjiaoxianluName());

            // 获取车辆编号
            if(loc.getGongjiaocheId() != null){
                GongjiaocheEntity che = gongjiaocheService.selectById(loc.getGongjiaocheId());
                if(che != null){
                    vo.setGongjiaocheName(che.getGongjiaocheName());
                }
            }

            // 判断位置是否过期
            boolean expired = false;
            if(loc.getCreateTime() != null){
                long diff = now.getTime() - loc.getCreateTime().getTime();
                if(diff > POSITION_EXPIRE_MILLIS){
                    expired = true;
                }
            } else {
                // 无更新时间视为过期
                expired = true;
            }
            vo.setExpired(expired);

            // 计算预计到站时间
            if(!expired && routeStops != null && !routeStops.isEmpty()
                    && loc.getCheliangweizhiMingcheng() != null){
                Integer eta = calculateETA(routeStops, loc.getCheliangweizhiMingcheng());
                vo.setEstimatedMinutes(eta);
            } else {
                // 过期或无法解析线路详情时不返回预估时间
                vo.setEstimatedMinutes(null);
            }

            result.add(vo);
        }

        return result;
    }

    /**
     * 解析线路全程字段，提取站点列表
     * 支持的分隔符：-> 、 → 、 - 、 ， 、 , 、 空格
     *
     * 场景：线路详情无法解析时返回null
     */
    private List<String> parseRouteStops(String quancheng) {
        if(quancheng == null || quancheng.trim().isEmpty()){
            return null;
        }

        try {
            // 去除可能的HTML标签
            String cleaned = quancheng.replaceAll("<[^>]*>", "").trim();
            if(cleaned.isEmpty()){
                return null;
            }

            // 尝试多种分隔符
            String[] stops;
            if(cleaned.contains("->")) {
                stops = cleaned.split("->");
            } else if(cleaned.contains("→")) {
                stops = cleaned.split("→");
            } else if(cleaned.contains("-")) {
                stops = cleaned.split("-");
            } else if(cleaned.contains("，")) {
                stops = cleaned.split("，");
            } else if(cleaned.contains(",")) {
                stops = cleaned.split(",");
            } else {
                // 无法解析为站点列表
                return null;
            }

            List<String> result = new ArrayList<>();
            for(String stop : stops){
                String trimmed = stop.trim();
                if(!trimmed.isEmpty()){
                    result.add(trimmed);
                }
            }

            return result.size() > 0 ? result : null;
        } catch (Exception e) {
            // 解析异常，返回null
            return null;
        }
    }

    /**
     * 计算预计到站时间（分钟）
     * 根据当前车辆下一站在站点列表中的位置，计算到目标站点的预估时间
     *
     * @param routeStops 线路全部站点列表（有序）
     * @param nextStopName 当前车辆下一站名称
     * @return 预估到站分钟数，无法计算时返回null
     */
    private Integer calculateETA(List<String> routeStops, String nextStopName) {
        if(routeStops == null || nextStopName == null){
            return null;
        }

        // 在站点列表中查找当前下一站的位置
        int currentIndex = -1;
        for(int i = 0; i < routeStops.size(); i++){
            if(routeStops.get(i).contains(nextStopName) || nextStopName.contains(routeStops.get(i))){
                currentIndex = i;
                break;
            }
        }

        if(currentIndex < 0){
            // 无法在站点列表中找到当前站
            return null;
        }

        // 计算剩余站数 * 每站预估时间
        int remainingStops = routeStops.size() - 1 - currentIndex;
        if(remainingStops <= 0){
            // 已在终点站或最后一站
            return MINUTES_PER_STOP;
        }

        return remainingStops * MINUTES_PER_STOP;
    }

}
