package com.example.taskmanager.controller;

import com.example.taskmanager.dev.DevLogger;
import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.PhaseDTO;
import com.example.taskmanager.model.Phase;
import com.example.taskmanager.model.ProjectMember;
import com.example.taskmanager.repository.PhaseRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/phases")
@Tag(name = "Phases", description = "Phase management APIs")
public class PhaseController {

    @Autowired
    private PhaseRepository phaseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse> createPhase(@Valid @RequestBody Phase phaseRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        var role = userService.getRoleInProject(user.getId(), phaseRequest.getProject().getId());
        if (role != ProjectMember.Role.Leader && role != ProjectMember.Role.Admin) {
            throw new EntityNotFoundException("User has no permission to create phase");
        }

        Phase phase = new Phase();
        phase.setPhaseName(phaseRequest.getPhaseName());
        phase.setDescription(phaseRequest.getDescription());
        phase.setProject(phaseRequest.getProject());
        phase.setStatus(phaseRequest.getStatus());
        phase.setStartDate(phaseRequest.getStartDate());
        phase.setEndDate(phaseRequest.getEndDate());
        phase.setOrderIndex(phaseRequest.getOrderIndex());

        Phase savedPhase = phaseRepository.save(phase);
        return ResponseEntity
                .ok(new ApiResponse("success", PhaseDTO.fromEntity(savedPhase), null));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse> getPhasesByProject(@PathVariable Long projectId) {
        List<Phase> phases = phaseRepository.findByProjectIdOrderByOrderIndexAsc(projectId);
        List<PhaseDTO> phaseDTOs = phases.stream()
                .map(PhaseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse("success", phaseDTOs, null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getPhase(@PathVariable Long id) {
        return phaseRepository.findById(id)
                .map(phase -> ResponseEntity
                        .ok(new ApiResponse("success", PhaseDTO.fromEntity(phase), null)))
                .orElse(ResponseEntity.ok(new ApiResponse("error", "Phase not found", null)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updatePhase(@PathVariable Long id, @Valid @RequestBody Phase phaseRequest,
            @AuthenticationPrincipal UserDetails userDetails, @RequestParam Long projectId) {
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        var role = userService.getRoleInProject(user.getId(), projectId);
        if (role != ProjectMember.Role.Leader && role != ProjectMember.Role.Admin) {
            throw new EntityNotFoundException("User has no permission to update phase");
        }
        Phase existingPhase = phaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Phase not found"));

        // check if update inputs exist
        if (phaseRequest.getPhaseName() != null) {
            existingPhase.setPhaseName(phaseRequest.getPhaseName());
        }
        if (phaseRequest.getDescription() != null) {
            existingPhase.setDescription(phaseRequest.getDescription());
        }
        if (phaseRequest.getStatus() != null) {
            existingPhase.setStatus(phaseRequest.getStatus());
        }
        if (phaseRequest.getStartDate() != null) {
            existingPhase.setStartDate(phaseRequest.getStartDate());
        }
        if (phaseRequest.getEndDate() != null) {
            existingPhase.setEndDate(phaseRequest.getEndDate());
        }
        if (phaseRequest.getOrderIndex() != null) {
            existingPhase.setOrderIndex(phaseRequest.getOrderIndex());
        }

        Phase updatedPhase = phaseRepository.save(existingPhase);
        return ResponseEntity
                .ok(new ApiResponse("success", PhaseDTO.fromEntity(updatedPhase), null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deletePhase(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        var existingPhase = phaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Phase not found"));

        var role = userService.getRoleInProject(user.getId(), existingPhase.getProject().getId());
        if (role != ProjectMember.Role.Leader && role != ProjectMember.Role.Admin) {
            throw new EntityNotFoundException("User has no permission to delete phase");
        }

        Phase phase = phaseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Phase not found"));

        phaseRepository.delete(phase);
        return ResponseEntity.ok(new ApiResponse("success", "Phase deleted successfully", null));
    }

    @PutMapping("/{phaseId}/move")
    public ResponseEntity<ApiResponse> movePhase(@PathVariable Long phaseId, @RequestParam Long position,
            @AuthenticationPrincipal UserDetails userDetails) {
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        var phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new EntityNotFoundException("Phase not found"));

        var role = userService.getRoleInProject(user.getId(), phase.getProject().getId());
        if (role != ProjectMember.Role.Leader && role != ProjectMember.Role.Admin) {
            throw new EntityNotFoundException("User has no permission to move phase");
        }

        int newPosition = position.intValue();

        List<Phase> phases = phaseRepository.findByProjectIdOrderByOrderIndexAsc(phase.getProject().getId());
        ArrayList<Phase> reorderedPhases = new ArrayList<>();
        for (Phase p : phases) {
            if (p.getId().equals(phaseId)) {
                continue;
            }
            reorderedPhases.add(p);
        }
        reorderedPhases.add(newPosition, phase);
        int index = 0;
        for (Phase p : reorderedPhases) {
            p.setOrderIndex(index);
            index++;
        }
        phaseRepository.saveAll(reorderedPhases);

        return ResponseEntity.ok(new ApiResponse("success", "Phase moved successfully", null));
    }

}
