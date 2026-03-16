package com.jobtracker.controller;

import com.jobtracker.model.Application;
import com.jobtracker.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "http://localhost:5173")
public class ApplicationController {

    @Autowired
    private ApplicationRepository repo;

    @GetMapping
    public List<Application> getAll() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping
    public Application create(@RequestBody Application app) {
        return repo.save(app);
    }

    @PutMapping("/{id}")
    public Application update(@PathVariable Long id, @RequestBody Application app) {
        app.setId(id);
        return repo.save(app);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}