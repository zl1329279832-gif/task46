package com.entity.view;

import com.entity.DaozhandingyueEntity;
import com.baomidou.mybatisplus.annotations.TableName;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

/**
 * 到站订阅
 * 后端返回视图实体辅助类
 * （通常后端关联的表或者自定义的字段需要返回使用）
 */
@TableName("daozhandingyue")
public class DaozhandingyueView extends DaozhandingyueEntity implements Serializable {
    private static final long serialVersionUID = 1L;



		//级联表 yonghu
			/**
			* 用户姓名
			*/
			private String yonghuName;
			/**
			* 手机号
			*/
			private String yonghuPhone;

		//级联表 gongjiaoxianlu
			/**
			* 线路名称
			*/
			private String gongjiaoxianluName;
			/**
			* 线路类型
			*/
			private Integer gongjiaoxianluTypes;
				/**
				* 线路类型的值
				*/
				private String gongjiaoxianluValue;

	public DaozhandingyueView() {

	}

	public DaozhandingyueView(DaozhandingyueEntity daozhandingyueEntity) {
		try {
			BeanUtils.copyProperties(this, daozhandingyueEntity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

					/**
					* 获取： 线路类型
					*/
					public Integer getGongjiaoxianluTypes() {
						return gongjiaoxianluTypes;
					}
					/**
					* 设置： 线路类型
					*/
					public void setGongjiaoxianluTypes(Integer gongjiaoxianluTypes) {
						this.gongjiaoxianluTypes = gongjiaoxianluTypes;
					}


						/**
						* 获取： 线路类型的值
						*/
						public String getGongjiaoxianluValue() {
							return gongjiaoxianluValue;
						}
						/**
						* 设置： 线路类型的值
						*/
						public void setGongjiaoxianluValue(String gongjiaoxianluValue) {
							this.gongjiaoxianluValue = gongjiaoxianluValue;
						}

}
