package com.dao;

import com.entity.DaozhanRemindEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;
import com.entity.view.DaozhanRemindView;

/**
 * 到站提醒订阅 Dao 接口
 *
 * @author
 */
public interface DaozhanRemindDao extends BaseMapper<DaozhanRemindEntity> {

   List<DaozhanRemindView> selectListView(Pagination page,@Param("params")Map<String,Object> params);

}
