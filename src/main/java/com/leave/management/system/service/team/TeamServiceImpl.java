package com.leave.management.system.service.team;



import com.leave.management.system.dto.response.ResponseDto;
import com.leave.management.system.dto.team.CreateTeamDto;
import com.leave.management.system.dto.team.UpdateTeamDto;
import com.leave.management.system.exceptions.ApiRequestException;
import com.leave.management.system.model.Department;
import com.leave.management.system.model.Team;
import com.leave.management.system.model.User;
import com.leave.management.system.repository.DepartmentRepository;
import com.leave.management.system.repository.TeamRepository;
import com.leave.management.system.repository.UserRepository;
import com.leave.management.system.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;


    @Override
    public ResponseDto createTeam(CreateTeamDto dto) {
        try {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ApiRequestException("Department not found"));
            User user = securityUtils.getCurrentUser();
            if (teamRepository.existsByLeadId(user.getId())) {
                throw new ApiRequestException("This user is already a team lead for another team.");
            }
            if(teamRepository.existsByName(dto.getName())) {
                throw new ApiRequestException("This name is already taken by another team.");
            }
            Team team = new Team();
            team.setName(dto.getName());
            team.setDescription(dto.getDescription());
            team.setDepartment(department);

            team.setCreatedBy(user);
            team.setLead(user);
            team.setUpdatedBy(user);
            team = teamRepository.save(team);
            return new ResponseDto(HttpStatus.CREATED, "Team created successfully", team.getId());
        } catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
        }
    }

    @Override
    public Page<Team> getAllTeams(Pageable pageable) {
        return teamRepository.findAll(pageable);
    }
    public  Team getTeamById(String id) {
        return teamRepository.findById(id).orElseThrow(() -> new ApiRequestException("Team not found"));
    }

    @Override
    public ResponseDto updateTeam(String id, UpdateTeamDto dto) {
        try {
            Team team = teamRepository.findById(id)
                    .orElseThrow(() -> new ApiRequestException("Team not found"));

            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ApiRequestException("Department not found"));

            team.setName(dto.getName());
            team.setDescription(dto.getDescription());
            team.setDepartment(department);
            User user = securityUtils.getCurrentUser();
            team.setUpdatedBy(user);
            team.setLead(user);

            team = teamRepository.save(team);
            return new ResponseDto(HttpStatus.OK, "Team updated successfully", team.getId());
        } catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
        }
    }

    @Override
    public ResponseDto deleteTeam(String id) {
        try {
            Team team = teamRepository.findById(id)
                    .orElseThrow(() -> new ApiRequestException("Team not found"));

            teamRepository.delete(team);
            return new ResponseDto(HttpStatus.OK, "Team deleted successfully", id);
        } catch (Exception e) {
            throw new ApiRequestException(e.getMessage());
        }
    }
}