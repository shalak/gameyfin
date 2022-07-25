package de.grimsi.gameyfin.mapper;

import com.igdb.proto.Igdb;
import de.grimsi.gameyfin.dto.GameOverviewDto;
import de.grimsi.gameyfin.entities.DetectedGame;
import de.grimsi.gameyfin.service.LibraryService;
import de.grimsi.gameyfin.util.ProtobufUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class GameMapper {

    public static DetectedGame toDetectedGame(Igdb.Game g, Path path) {
        List<Igdb.MultiplayerMode> multiplayerModes = g.getMultiplayerModesList();
        List<String> screenshotIds = g.getScreenshotsList().stream().map(Igdb.Screenshot::getImageId).toList();
        List<String> videoIds = g.getVideosList().stream().map(Igdb.GameVideo::getVideoId).toList();

        return DetectedGame.builder()
                .slug(g.getSlug())
                .title(g.getName())
                .summary(g.getSummary())
                .releaseDate(ProtobufUtil.toInstant(g.getFirstReleaseDate()))
                .userRating((int) g.getRating())
                .criticsRating((int) g.getAggregatedRating())
                .totalRating((int) g.getTotalRating())
                .category(g.getCategory().name())
                .offlineCoop(hasOfflineCoop(multiplayerModes))
                .onlineCoop(hasOnlineCoop(multiplayerModes))
                .lanSupport(hasLanSupport(multiplayerModes))
                .maxPlayers(getMaxPlayers(multiplayerModes))
                .coverId(getCoverId(g))
                .screenshotIds(screenshotIds)
                .videoIds(videoIds)
                .companies(CompanyMapper.toCompanies(g.getInvolvedCompaniesList()))
                .genres(GenreMapper.toGenres(g.getGenresList()))
                .keywords(KeywordMapper.toKeywords(g.getKeywordsList()))
                .themes(ThemeMapper.toThemes(g.getThemesList()))
                .playerPerspectives(PlayerPerspectiveMapper.toPlayerPerspectives(g.getPlayerPerspectivesList()))
                .path(path.toString())
                .diskSize(calculateDiskSize(path))
                .build();
    }

    public static GameOverviewDto toGameOverviewDto(DetectedGame game) {
        return GameOverviewDto.builder()
                .slug(game.getSlug())
                .title(game.getTitle())
                .coverId(game.getCoverId())
                .build();
    }

    private static String getCoverId(Igdb.Game g) {
        String coverId = g.getCover().getImageId();

        if(StringUtils.hasText(coverId)) return coverId;

        return "nocover";
    }

    private static boolean hasOfflineCoop(List<Igdb.MultiplayerMode> modes) {
        return modes.stream().anyMatch(Igdb.MultiplayerMode::getOfflinecoop);
    }

    private static boolean hasLanSupport(List<Igdb.MultiplayerMode> modes) {
        return modes.stream().anyMatch(Igdb.MultiplayerMode::getLancoop);
    }

    private static boolean hasOnlineCoop(List<Igdb.MultiplayerMode> modes) {
        return modes.stream().anyMatch(Igdb.MultiplayerMode::getOnlinecoop);
    }

    private static int getMaxPlayers(List<Igdb.MultiplayerMode> modes) {
        return modes.stream().mapToInt(Igdb.MultiplayerMode::getOnlinecoopmax).max().orElse(0);
    }

    private static long calculateDiskSize(Path path) {
        try(Stream<Path> pathStream = Files.walk(path)) {
            return pathStream
                    .filter(p -> p.toFile().isFile())
                    .mapToLong(p -> p.toFile().length())
                    .sum();
        } catch (IOException e) {
            log.error("Unable to calculate size for path '{}'.", path);
            return -1;
        }
    }
}