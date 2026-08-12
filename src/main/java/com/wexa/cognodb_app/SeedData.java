package com.wexa.cognodb_app;

import java.util.List;
import java.util.Map;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

public class SeedData {

    public static void main(String[] args) {
        String uri = System.getenv("COGNODB_URI");
        String user = System.getenv("COGNODB_USER");
        String password = System.getenv("COGNODB_PASSWORD");

        if (uri == null || user == null || password == null) {
            System.err.println("Missing COGNODB_URI / COGNODB_USER / COGNODB_PASSWORD environment variables.");
            return;
        }

        try (Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
             Session session = driver.session()) {

            System.out.println("Clearing existing data...");
            session.run("MATCH (n) DETACH DELETE n");

            System.out.println("Creating constraints...");
            session.run("CREATE CONSTRAINT person_name IF NOT EXISTS FOR (p:Person) REQUIRE p.name IS UNIQUE");
            session.run("CREATE CONSTRAINT skill_name IF NOT EXISTS FOR (s:Skill) REQUIRE s.name IS UNIQUE");
            session.run("CREATE CONSTRAINT project_name IF NOT EXISTS FOR (pr:Project) REQUIRE pr.name IS UNIQUE");

            System.out.println("Creating people...");
            List<Map<String, Object>> people = List.of(
                Map.of("name", "Alice Kumar", "title", "Frontend Developer", "email", "alice@example.com"),
                Map.of("name", "Ben Torres", "title", "Backend Developer", "email", "ben@example.com"),
                Map.of("name", "Chloe Nguyen", "title", "Data Engineer", "email", "chloe@example.com"),
                Map.of("name", "David Park", "title", "DevOps Engineer", "email", "david@example.com"),
                Map.of("name", "Elena Rossi", "title", "UX Designer", "email", "elena@example.com"),
                Map.of("name", "Farhan Ali", "title", "Senior Backend Engineer", "email", "farhan@example.com"),
                Map.of("name", "Grace Lee", "title", "Engineering Manager", "email", "grace@example.com"),
                Map.of("name", "Hassan Sheikh", "title", "Full-stack Developer", "email", "hassan@example.com")
            );
            for (Map<String, Object> person : people) {
                session.run("CREATE (:Person {name:$name, title:$title, email:$email})", person);
            }

            System.out.println("Creating skills...");
            List<String> skills = List.of("React", "Spring Boot", "Cypher", "Docker", "UI Design", "Python", "Kubernetes", "System Design");
            for (String skill : skills) {
                session.run("CREATE (:Skill {name:$name})", Map.of("name", skill));
            }

            System.out.println("Creating projects...");
            List<Map<String, Object>> projects = List.of(
                Map.of("name", "Customer Portal Revamp", "status", "active", "deadline", "2026-09-30"),
                Map.of("name", "Internal Analytics Platform", "status", "active", "deadline", "2026-10-15"),
                Map.of("name", "Payments Migration", "status", "planning", "deadline", "2026-11-01")
            );
            for (Map<String, Object> project : projects) {
                session.run("CREATE (:Project {name:$name, status:$status, deadline:$deadline})", project);
            }

            System.out.println("Creating HAS_SKILL relationships...");
            List<Map<String, Object>> hasSkill = List.of(
                Map.of("person", "Alice Kumar", "skill", "React", "level", "expert"),
                Map.of("person", "Alice Kumar", "skill", "UI Design", "level", "intermediate"),
                Map.of("person", "Ben Torres", "skill", "Spring Boot", "level", "expert"),
                Map.of("person", "Ben Torres", "skill", "Docker", "level", "intermediate"),
                Map.of("person", "Chloe Nguyen", "skill", "Python", "level", "expert"),
                Map.of("person", "Chloe Nguyen", "skill", "Cypher", "level", "expert"),
                Map.of("person", "David Park", "skill", "Docker", "level", "expert"),
                Map.of("person", "David Park", "skill", "Kubernetes", "level", "expert"),
                Map.of("person", "Elena Rossi", "skill", "UI Design", "level", "expert"),
                Map.of("person", "Farhan Ali", "skill", "Spring Boot", "level", "expert"),
                Map.of("person", "Farhan Ali", "skill", "System Design", "level", "expert"),
                Map.of("person", "Grace Lee", "skill", "System Design", "level", "intermediate"),
                Map.of("person", "Hassan Sheikh", "skill", "React", "level", "intermediate"),
                Map.of("person", "Hassan Sheikh", "skill", "Spring Boot", "level", "intermediate")
            );
            for (Map<String, Object> rel : hasSkill) {
                session.run(
                    "MATCH (p:Person {name:$person}), (s:Skill {name:$skill}) " +
                    "CREATE (p)-[:HAS_SKILL {level:$level}]->(s)", rel);
            }

            System.out.println("Creating WORKS_ON relationships...");
            List<Map<String, Object>> worksOn = List.of(
                Map.of("person", "Alice Kumar", "project", "Customer Portal Revamp", "role", "Lead Frontend"),
                Map.of("person", "Hassan Sheikh", "project", "Customer Portal Revamp", "role", "Frontend Dev"),
                Map.of("person", "Chloe Nguyen", "project", "Internal Analytics Platform", "role", "Lead Data Engineer"),
                Map.of("person", "Ben Torres", "project", "Payments Migration", "role", "Backend Dev")
            );
            for (Map<String, Object> rel : worksOn) {
                session.run(
                    "MATCH (p:Person {name:$person}), (pr:Project {name:$project}) " +
                    "CREATE (p)-[:WORKS_ON {role:$role}]->(pr)", rel);
            }

            System.out.println("Creating REQUIRES relationships...");
            List<Map<String, Object>> requires = List.of(
                Map.of("project", "Customer Portal Revamp", "skill", "React", "priority", "high"),
                Map.of("project", "Customer Portal Revamp", "skill", "UI Design", "priority", "high"),
                Map.of("project", "Customer Portal Revamp", "skill", "Spring Boot", "priority", "medium"),
                Map.of("project", "Internal Analytics Platform", "skill", "Python", "priority", "high"),
                Map.of("project", "Internal Analytics Platform", "skill", "Cypher", "priority", "high"),
                Map.of("project", "Payments Migration", "skill", "Spring Boot", "priority", "high"),
                Map.of("project", "Payments Migration", "skill", "System Design", "priority", "high"),
                Map.of("project", "Payments Migration", "skill", "Docker", "priority", "medium")
            );
            for (Map<String, Object> rel : requires) {
                session.run(
                    "MATCH (pr:Project {name:$project}), (s:Skill {name:$skill}) " +
                    "CREATE (pr)-[:REQUIRES {priority:$priority}]->(s)", rel);
            }

            System.out.println("Creating MENTORS relationships...");
            List<Map<String, Object>> mentors = List.of(
                Map.of("mentor", "Farhan Ali", "mentee", "Ben Torres", "since", "2025-01-15"),
                Map.of("mentor", "Farhan Ali", "mentee", "Hassan Sheikh", "since", "2025-06-01"),
                Map.of("mentor", "Grace Lee", "mentee", "Farhan Ali", "since", "2024-03-10"),
                Map.of("mentor", "David Park", "mentee", "Chloe Nguyen", "since", "2025-02-20")
            );
            for (Map<String, Object> rel : mentors) {
                session.run(
                    "MATCH (m:Person {name:$mentor}), (e:Person {name:$mentee}) " +
                    "CREATE (m)-[:MENTORS {since:date($since)}]->(e)", rel);
            }

            System.out.println("Seed data loaded successfully!");
        }
    }
}