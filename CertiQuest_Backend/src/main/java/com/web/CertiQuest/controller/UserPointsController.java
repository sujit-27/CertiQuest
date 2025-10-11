package com.web.CertiQuest.controller;

import com.web.CertiQuest.dto.UserPointsDto;
import com.web.CertiQuest.model.UserPoints;
import com.web.CertiQuest.service.UserPointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserPointsController {

    @Autowired
    private UserPointsService userPointsService;

    @GetMapping("/points")
    public ResponseEntity<?> getUserPoints() {
        Optional<UserPoints> pointsOpt = userPointsService.getUserPointsForCurrentUser();
        if (pointsOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }
        UserPointsDto userPointsDto = new UserPointsDto();
        userPointsDto.setPoints(pointsOpt.get().getPoints());
        return ResponseEntity.ok(userPointsDto);
    }

}

