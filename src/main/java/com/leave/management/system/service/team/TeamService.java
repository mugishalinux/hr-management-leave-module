package com.leave.management.system.service.team;
import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.dto.team.CreateTeamDto;
import com.leave.management.system.dto.team.UpdateTeamDto;
import com.leave.management.system.model.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TeamService {
    ResponseDto createTeam(CreateTeamDto dto);
    Page<Team> getAllTeams(Pageable pageable);
    ResponseDto updateTeam(String id, UpdateTeamDto dto);
    ResponseDto deleteTeam(String id);
    Team getTeamById(String id);
}
