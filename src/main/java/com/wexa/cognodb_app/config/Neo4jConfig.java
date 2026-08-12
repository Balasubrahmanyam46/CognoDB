package com.wexa.cognodb_app.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Neo4jConfig {

    @Value("bolt+s://db-25edf93a.databases.cognodb.com")
    private String uri;

    @Value("cognodb")
    private String user;

    @Value("dae16dcf57daa5dd0d8b81db5524d11f")
    private String password;

    @Bean(destroyMethod = "close")
    public Driver neo4jDriver() {
        return GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }
}