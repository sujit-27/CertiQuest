package com.web.CertiQuest.service;

import com.web.CertiQuest.dao.LeaderboardDao;
import com.web.CertiQuest.dto.LeaderboardDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaderboardService {

    @Autowired
    private LeaderboardDao leaderboardDao;

    public List<LeaderboardDto> getGlobalLeaderboard(int limit) {
        List<LeaderboardDto> leaderboard =
                leaderboardDao.findGlobalLeaderboard(PageRequest.of(0, limit));

        assignRanks(leaderboard);
        return leaderboard;
    }

    public List<LeaderboardDto> getQuizLeaderboard(int quizId, int limit) {
        List<LeaderboardDto> leaderboard =
                leaderboardDao.findQuizLeaderboard(quizId, PageRequest.of(0, limit));

        assignRanks(leaderboard);
        return leaderboard;
    }

    private void assignRanks(List<LeaderboardDto> leaderboard) {
        int rank = 1;
        for (LeaderboardDto entry : leaderboard) {
            entry.setRank(rank++);
        }
    }
}

