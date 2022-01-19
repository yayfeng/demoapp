package it.example.cassandra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ScheduledInserterService {

    private Logger logger = LoggerFactory.getLogger( this.getClass() );

    @Autowired
    private CassandraOperations cassandra;

    private AtomicInteger counter = new AtomicInteger(0);

    @Scheduled( fixedDelay = 10 * 1000)
    public void insertOneRow() {
        logger.info(" Method scheduled ");
        ExampleTable t = new ExampleTable();
        t.setId( "id_" + counter.incrementAndGet() );
        t.setDescription(" Row created now: " + Instant.now() );

        cassandra.insert( t );

    }
}
