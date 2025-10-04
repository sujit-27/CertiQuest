package com.web.CertiQuest.controller;

import com.web.CertiQuest.dto.LeaderboardDto;
import com.web.CertiQuest.service.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LeaderboardController {

    @Autowired
    private LeaderboardService leaderboardService;

    @GetMapping("/leaderboard/global")
    public List<LeaderboardDto> getGlobalLeaderboard() {
        return leaderboardService.getGlobalLeaderboard(10);
    }

    @GetMapping("/leaderboard/quiz/{quizId}")
    public List<LeaderboardDto> getQuizLeaderboard(@PathVariable int quizId) {
        return leaderboardService.getQuizLeaderboard(quizId, 10);
    }

}
