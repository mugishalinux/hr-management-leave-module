package com.leave.management.system.controller;

import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.dto.team.AssignUsersToTeamDto;
import com.leave.management.system.dto.team.CreateTeamDto;
import com.leave.management.system.dto.team.UpdateTeamDto;
import com.leave.management.system.model.Team;
import com.leave.management.system.security.SecurityUtils;
import com.leave.management.system.service.team.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<ResponseDto> createTeam(@Valid @RequestBody CreateTeamDto dto) {
        return new ResponseEntity<>(teamService.createTeam(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<Team>> getAllTeams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int sizePage,
            @RequestParam(defaultValue = "name") String sortBy) {
        return ResponseEntity.ok(teamService.getAllTeams(page, sizePage, sortBy));
    }
    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeamById(@PathVariable String id) {
        return ResponseEntity.ok( teamService.getTeamById(id));
    }
    @GetMapping("/creator")
    public ResponseEntity<Team> getTeamByUser() {
        Optional<Team> team = teamService.getTeamByUser();
        return team.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto> updateTeam(
            @PathVariable String id,
            @Valid @RequestBody UpdateTeamDto dto) {
        return ResponseEntity.ok(teamService.updateTeam(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto> deleteTeam(@PathVariable String id) {
        return ResponseEntity.ok(teamService.deleteTeam(id));
    }
    @PostMapping("/assign-users")
    public ResponseEntity<ResponseDto> assignUsersToTeam(@RequestBody @Valid AssignUsersToTeamDto dto) {
        return ResponseEntity.ok(teamService.assignUsersToTeam(dto));
    }
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<Team>> getTeamsByDepartmentId(@PathVariable String departmentId) {
        List<Team> teams = teamService.getTeamsByDepartmentId(departmentId);
        return ResponseEntity.ok(teams);
    }
}
