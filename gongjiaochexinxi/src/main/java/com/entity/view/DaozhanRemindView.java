package com.entity.view;

import com.entity.DaozhanRemindEntity;
import com.baomidou.mybatisplus.annotations.TableName;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

/**
 * 到站提醒订阅
 * 后端返回视图实体辅助类
 */
@TableName("to_station_remind")
public class DaozhanRemindView extends DaozhanRemindEntity implements Serializable {
    private static final long serialVersionUID = 1L;

		//级联表 gongjiaoxianlu
			/**
			* 线路名称
			*/
			private String gongjiaoxianluName;

		//级联表 yonghu
			/**
			* 用户姓名
			*/
			private String yonghuName;
			/**
			* 手机号
			*/
			private String yonghuPhone;

	public DaozhanRemindView() {

	}

	public DaozhanRemindView(DaozhanRemindEntity daozhanRemindEntity) {
		try {
			BeanUtils.copyProperties(this, daozhanRemindEntity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


				//级联表的get和set gongjiaoxianlu

					/**
					* 获取： 线路名称
					*/
					public String getGongjiaoxianluName() {
						return gongjiaoxianluName;
					}
					/**
					* 设置： 线路名称
					*/
					public void setGongjiaoxianluName(String gongjiaoxianluName) {
						this.gongjiaoxianluName = gongjiaoxianluName;
					}

				//级联表的get和set yonghu

					/**
					* 获取： 用户姓名
					*/
					public String getYonghuName() {
						return yonghuName;
					}
					/**
					* 设置： 用户姓名
					*/
					public void setYonghuName(String yonghuName) {
						this.yonghuName = yonghuName;
					}

					/**
					* 获取： 手机号
					*/
					public String getYonghuPhone() {
						return yonghuPhone;
					}
					/**
					* 设置： 手机号
					*/
					public void setYonghuPhone(String yonghuPhone) {
						this.yonghuPhone = yonghuPhone;
					}


}
