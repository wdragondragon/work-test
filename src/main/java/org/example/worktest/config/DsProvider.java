package org.example.worktest.config;

import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.provider.AbstractJdbcDataSourceProvider;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import org.example.worktest.utils.NoThrowInvoke;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@Primary
@Configuration
public class DsProvider {

    @Resource
    private DataSourceProperties defaultDsConfig;

    @Bean
    public DynamicDataSourceProvider jdbcDynamicDataSourceProvider(DefaultDataSourceCreator defaultDataSourceCreator) {
        return new AbstractJdbcDataSourceProvider(defaultDataSourceCreator, defaultDsConfig.getDriverClassName(), defaultDsConfig.getUrl(), defaultDsConfig.getUsername(), defaultDsConfig.getPassword()) {
            @Override
            protected Map<String, DataSourceProperty> executeStmt(Statement statement) {
                return NoThrowInvoke.invoke(() -> {
                    Map<String, DataSourceProperty> dataSourcePropertiesMap;
                    ResultSet rs;
                    dataSourcePropertiesMap = new HashMap<>();
                    DataSourceProperty defaultProperty = new DataSourceProperty();
                    BeanUtils.copyProperties(defaultDsConfig, defaultProperty);
                    dataSourcePropertiesMap.put("master", defaultProperty);
                    rs = statement.executeQuery("SELECT * FROM DYNAMIC_DATASOURCE_INSTANCE");
                    while (rs.next()) {
                        String name = rs.getString("name");
                        DataSourceProperty property = new DataSourceProperty();
                        BeanUtils.copyProperties(defaultDsConfig, property);
                        property.setDriverClassName(rs.getString("driver"));
                        property.setUrl(rs.getString("url"));
                        property.setUsername(rs.getString("username"));
                        property.setPassword(rs.getString("password"));
                        dataSourcePropertiesMap.put(name, property);
                    }
                    return dataSourcePropertiesMap;
                });
            }
        };
    }
}