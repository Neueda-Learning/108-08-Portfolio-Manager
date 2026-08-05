package com.portfoliom.controller;

import com.portfoliom.dto.NewsArticle;
import com.portfoliom.model.Holding;
import com.portfoliom.service.HoldingService;
import com.portfoliom.service.NewsService;
import com.portfoliom.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/news")
@Tag(name = "News", description = "Market headlines for holdings")
public class NewsController {

    private final NewsService newsService;
    private final HoldingService holdingService;
    private final UserService userService;

    public NewsController(NewsService newsService, HoldingService holdingService, UserService userService) {
        this.newsService = newsService;
        this.holdingService = holdingService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get market news for the current holdings plus general market headlines")
    public List<NewsArticle> getNews(Authentication authentication) {
        Set<String> tickers = holdingService.getAllHoldings(userService.getCurrentUserId(authentication)).stream()
                .map(Holding::getTicker)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<NewsArticle> articles = new ArrayList<>();
        int perTickerLimit = tickers.isEmpty() ? 0 : Math.max(2, 12 / tickers.size());
        articles.addAll(newsService.getNewsForTickers(new ArrayList<>(tickers).subList(0, Math.min(6, tickers.size())), perTickerLimit));
        articles.addAll(newsService.getGeneralMarketNews(6));
        return articles;
    }
}
