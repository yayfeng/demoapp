package it.example.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import software.aws.mcs.auth.SigV4AuthProvider;

import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "aws.keyspaces")
public class ExampleCassandraAutoConfiguration extends CassandraAutoConfiguration {
    private String endpoint;
    private String region;

    public @Bean
    DriverConfigLoader driverConfigLoader() {
        DriverConfigLoader loader = DriverConfigLoader.fromClasspath("application.conf");
        return loader;
    }

    public @Bean
    CqlSession session(DriverConfigLoader driverConfigLoader, CassandraProperties properties) throws NoSuchAlgorithmException {
        String keyspaceName = properties.getKeyspaceName();

        SigV4AuthProvider provider = new SigV4AuthProvider(region);
        List<InetSocketAddress> contactPoints = Collections.singletonList(new InetSocketAddress(endpoint, 9142));

        return CqlSession.builder().addContactPoints(contactPoints).
                withAuthProvider(provider).
                withLocalDatacenter(region).
                withConfigLoader(driverConfigLoader).
                withSslContext(SSLContext.getDefault()).
                withKeyspace(keyspaceName).
                build();
    }


    @Bean
    @ConditionalOnMissingBean
    @Scope("prototype")
    public CqlSessionBuilder cassandraSessionBuilder(CassandraProperties properties,
                                                     DriverConfigLoader driverConfigLoader, ObjectProvider<CqlSessionBuilderCustomizer> builderCustomizers) {
        String keyspaceName = properties.getKeyspaceName();
        createKeyspaceIfNecessary(properties, driverConfigLoader, builderCustomizers, keyspaceName);
        return super.cassandraSessionBuilder(properties, driverConfigLoader, builderCustomizers);
    }

    private void createKeyspaceIfNecessary(CassandraProperties properties, DriverConfigLoader driverConfigLoader, ObjectProvider<CqlSessionBuilderCustomizer> builderCustomizers, String keyspaceName) {
        if (keyspaceName != null && keyspaceName.trim().length() > 0) {
            properties.setKeyspaceName(null); // FIXME clone or do a cassandra property wrapper
            super.cassandraSessionBuilder(properties, driverConfigLoader, builderCustomizers)
                    .build()
                    .execute("CREATE KEYSPACE IF NOT EXISTS " + keyspaceName + " WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}");
            properties.setKeyspaceName(keyspaceName);
        }
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
