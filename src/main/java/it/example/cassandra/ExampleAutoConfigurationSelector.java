package it.example.cassandra;

import org.springframework.boot.autoconfigure.AutoConfigurationImportSelector;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.*;

public class ExampleAutoConfigurationSelector extends AutoConfigurationImportSelector {

    @Override
    protected Set<String> getExclusions(AnnotationMetadata metadata, AnnotationAttributes attributes) {
        Set<String> exclusions = super.getExclusions(metadata, attributes);

        exclusions.add( CassandraAutoConfiguration.class.getName() );
        return exclusions;
    }

}
