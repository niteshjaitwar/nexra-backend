package com.nexra.hrms.nexra.modules.auth.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures centralized model mapping behavior for DTO and entity transformations.
 *
 * @author niteshjaitwar
 * @version 1.0
 */
@Configuration
public class ModelMapperConfig {

    /**
     * Builds model mapper with private field access and null-skip behavior.
     *
     * @return configured model mapper
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
            .setSkipNullEnabled(true)
            .setFieldMatchingEnabled(true)
            .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
        return modelMapper;
    }
}
