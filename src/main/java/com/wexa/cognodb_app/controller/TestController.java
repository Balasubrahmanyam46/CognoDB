package com.wexa.cognodb_app.controller;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private Driver driver;

    @GetMapping("/test-connection")
    public String testConnection() {
        try (Session session = driver.session()) {
            Result result = session.run("RETURN 'CognoDB connected!' AS message");
            return result.single().get("message").asString();
        } catch (Exception e) {
            return "Connection failed: " + e.getMessage();
        }
    }
}