package com.wexa.cognodb_app.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PersonController {

    @Autowired
    private Driver driver;

    @GetMapping("/people")
    public List<Map<String, Object>> getAllPeople() {
        try (Session session = driver.session()) {
            Result result = session.run(
                "MATCH (p:Person) RETURN p.name AS name, p.title AS title, p.email AS email ORDER BY p.name");
            List<Map<String, Object>> people = new ArrayList<>();
            for (Record record : result.list()) {
                Map<String, Object> person = new HashMap<>();
                person.put("name", record.get("name").asString());
                person.put("title", record.get("title").asString());
                person.put("email", record.get("email").asString());
                people.add(person);
            }
            return people;
        }
    }

    // Multi-hop traversal (3 hops): Me -> Project -> Skill <- Candidate
    @GetMapping("/recommend-mentors/{personName}")
    public List<Map<String, Object>> recommendMentors(@PathVariable String personName) {
        String query =
            "MATCH (me:Person {name: $personName})-[:WORKS_ON]->(p:Project)-[:REQUIRES]->(s:Skill) " +
            "MATCH (candidate:Person)-[hs:HAS_SKILL]->(s) " +
            "WHERE NOT (candidate)-[:WORKS_ON]->(p) AND candidate <> me " +
            "RETURN DISTINCT candidate.name AS name, candidate.title AS title, s.name AS skill, hs.level AS level " +
            "ORDER BY hs.level DESC";

        try (Session session = driver.session()) {
            Result result = session.run(query, Map.of("personName", personName));
            List<Map<String, Object>> recs = new ArrayList<>();
            for (Record record : result.list()) {
                Map<String, Object> rec = new HashMap<>();
                rec.put("name", record.get("name").asString());
                rec.put("title", record.get("title").asString());
                rec.put("skill", record.get("skill").asString());
                rec.put("level", record.get("level").asString());
                recs.add(rec);
            }
            return recs;
        }
    }

    // Variable-length path — this is the "SQL would find awkward" query
    @GetMapping("/mentor-chain")
    public List<Map<String, Object>> mentorChain(@RequestParam String from, @RequestParam String to) {
        String query =
            "MATCH path = shortestPath((a:Person {name: $from})-[:MENTORS*1..5]->(b:Person {name: $to})) " +
            "RETURN [n IN nodes(path) | n.name] AS chain, length(path) AS hops";

        try (Session session = driver.session()) {
            Result result = session.run(query, Map.of("from", from, "to", to));
            List<Map<String, Object>> paths = new ArrayList<>();
            for (Record record : result.list()) {
                Map<String, Object> p = new HashMap<>();
                p.put("chain", record.get("chain").asList());
                p.put("hops", record.get("hops").asInt());
                paths.add(p);
            }
            return paths;
        }
    }
}