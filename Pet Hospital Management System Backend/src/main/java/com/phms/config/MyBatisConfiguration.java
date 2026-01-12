package com.phms.config;

import com.github.pagehelper.PageInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Properties;

/**
 * class name:MyBatisConfiguration <BR>
 * class description: MyBatis数据库配置，手动配置PageHelper拦截器以避免循环依赖 <BR>
 * Remark: 使用InitializingBean在SqlSessionFactory创建后添加拦截器，避免与自动配置冲突 <BR>
 */
@Configuration
public class MyBatisConfiguration implements InitializingBean {
	
	@Autowired
	private List<SqlSessionFactory> sqlSessionFactoryList;
	
	@Override
	public void afterPropertiesSet() {
		// 手动配置PageHelper拦截器
		PageInterceptor pageInterceptor = new PageInterceptor();
		Properties properties = new Properties();
		properties.setProperty("helperDialect", "mysql");
		properties.setProperty("reasonable", "true");
		properties.setProperty("supportMethodsArguments", "true");
		properties.setProperty("params", "count=countSql");
		properties.setProperty("pageSize", "10");
		pageInterceptor.setProperties(properties);
		
		// 将拦截器添加到所有SqlSessionFactory中
		for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
			sqlSessionFactory.getConfiguration().addInterceptor(pageInterceptor);
		}
	}
}