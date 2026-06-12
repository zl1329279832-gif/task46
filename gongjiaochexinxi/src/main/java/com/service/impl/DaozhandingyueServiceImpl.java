package com.service.impl;

import com.utils.StringUtil;
import org.springframework.stereotype.Service;
import java.lang.reflect.Field;
import java.util.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import com.utils.PageUtils;
import com.utils.Query;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import com.dao.DaozhandingyueDao;
import com.entity.DaozhandingyueEntity;
import com.service.DaozhandingyueService;
import com.entity.view.DaozhandingyueView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 到站订阅 服务实现类
 */
@Service("daozhandingyueService")
@Transactional
public class DaozhandingyueServiceImpl extends ServiceImpl<DaozhandingyueDao, DaozhandingyueEntity> implements DaozhandingyueService {

    private static final Logger logger = LoggerFactory.getLogger(DaozhandingyueServiceImpl.class);

    @Override
    public PageUtils queryPage(Map<String,Object> params) {
        if(params != null && (params.get("limit") == null || params.get("page") == null)){
            params.put("page","1");
            params.put("limit","10");
        }
        Page<DaozhandingyueView> page = new Query<DaozhandingyueView>(params).getPage();
        page.setRecords(baseMapper.selectListView(page, params));
        return new PageUtils(page);
    }

    @Override
    public void checkAndTriggerArrivalNotify(Integer gongjiaoxianluId, String nextStationName, String gongjiaocheName) {
        if(gongjiaoxianluId == null || nextStationName == null || nextStationName.trim().isEmpty()){
            return;
        }
        // 查询该线路下订阅了该站点的、状态为订阅中(1)的记录
        Wrapper<DaozhandingyueEntity> wrapper = new EntityWrapper<DaozhandingyueEntity>()
                .eq("gongjiaoxianlu_id", gongjiaoxianluId)
                .eq("zhandian_name", nextStationName.trim())
                .eq("dingyue_status", 1);

        List<DaozhandingyueEntity> subscriptions = this.selectList(wrapper);
        if(subscriptions == null || subscriptions.isEmpty()){
            return;
        }

        Date now = new Date();
        for(DaozhandingyueEntity sub : subscriptions){
            // 更新为已触发
            sub.setDingyueStatus(2);
            sub.setTixingStatus(1);
            sub.setTixingContent("车辆[" + gongjiaocheName + "]即将到达站点[" + nextStationName + "]，请做好乘车准备");
            sub.setUpdateTime(now);
            this.updateById(sub);
            logger.info("到站提醒已触发: 用户ID={}, 线路ID={}, 站点={}", sub.getYonghuId(), gongjiaoxianluId, nextStationName);
        }
    }

}
