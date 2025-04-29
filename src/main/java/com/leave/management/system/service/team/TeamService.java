package com.leave.management.system.service.team;
import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.dto.team.AssignUsersToTeamDto;
import com.leave.management.system.dto.team.CreateTeamDto;
import com.leave.management.system.dto.team.UpdateTeamDto;
import com.leave.management.system.model.Team;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface TeamService {
    ResponseDto createTeam(CreateTeamDto dto);
    Page<Team> getAllTeams(int page, int sizePage, String sortBy);
    ResponseDto updateTeam(String id, UpdateTeamDto dto);
    ResponseDto deleteTeam(String id);
    Team getTeamById(String id);
    ResponseDto assignUsersToTeam(AssignUsersToTeamDto dto);
    Optional<Team> getTeamByLeadId(String string);
    Optional<Team> getTeamByUser();
    List<Team> getTeamsByDepartmentId(String departmentId);
}
