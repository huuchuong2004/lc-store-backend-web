package vn.huuchuong.lcstorebackendweb.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMaperConfig {

    @Bean // danh dau day la 1 bean hco spring quan ly , ban se ko can phai new o nhiue noi nua
    public ModelMapper createModelMapper() {
        return new ModelMapper();
    }

}
