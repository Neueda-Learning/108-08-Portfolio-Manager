package com.portfoliom.config;

import com.portfoliom.model.Holding;
import com.portfoliom.model.HoldingType;
import com.portfoliom.model.Portfolio;
import com.portfoliom.model.Role;
import com.portfoliom.model.User;
import com.portfoliom.repository.HoldingRepository;
import com.portfoliom.repository.PortfolioRepository;
import com.portfoliom.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds the login accounts and, on a brand new database, a few sample holdings so the app is
 * usable immediately. Runs through JPA (not raw SQL) so encrypted columns are written correctly.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.owner.username:owner}")
    private String ownerUsername;

    @Value("${app.owner.password:ChangeMe123!}")
    private String ownerPassword;

    @Value("${app.manager.username:fundmanager}")
    private String managerUsername;

    @Value("${app.manager.password:FundManager123!}")
    private String managerPassword;

    public DataSeeder(UserRepository userRepository, PortfolioRepository portfolioRepository,
                       HoldingRepository holdingRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.portfolioRepository = portfolioRepository;
        this.holdingRepository = holdingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        User manager = seedFundManagerAccount();
        User owner = seedOwnerAccount(manager);
        seedSampleHoldings(owner);
    }

    private User seedOwnerAccount(User manager) {
        return userRepository.findByUsername(ownerUsername)
                .orElseGet(() -> {
                    User user = new User(ownerUsername, passwordEncoder.encode(ownerPassword), Role.OWNER);
                    user.setName("Default Owner");
                    user.setEmail(ownerUsername + "@portfoliom.local");
                    user.setManagedBy(manager);
                    User saved = userRepository.save(user);
                    log.info("Seeded login account '{}'. Change the password after first login.", ownerUsername);
                    return saved;
                });
    }

    private User seedFundManagerAccount() {
        return userRepository.findByUsername(managerUsername)
                .orElseGet(() -> {
                    User manager = new User(managerUsername, passwordEncoder.encode(managerPassword), Role.FUND_MANAGER);
                    manager.setName("Fund Manager");
                    manager.setEmail(managerUsername + "@portfoliom.local");
                    User saved = userRepository.save(manager);
                    log.info("Seeded fund manager account '{}'. Change the password after first login.", managerUsername);
                    return saved;
                });
    }

    private void seedSampleHoldings(User owner) {
        if (portfolioRepository.count() > 0) {
            return;
        }

        Portfolio blueChip = portfolioRepository.save(
                new Portfolio("Blue Chip India", "Large-cap Indian equities focused on stable long-term growth", owner));
        addHolding(blueChip, "RELIANCE.NS", "Reliance Industries Ltd.", 10, "2500.00", "2023-01-15");
        addHolding(blueChip, "HDFCBANK.NS", "HDFC Bank Ltd.", 15, "1650.00", "2023-03-20");
        addHolding(blueChip, "ITC.NS", "ITC Ltd.", 50, "380.00", "2023-06-01");
        addHolding(blueChip, "TMCV.NS", "Tata Motors Ltd. (Commercial Vehicles)", 30, "620.00", "2023-08-10");

        Portfolio itLeaders = portfolioRepository.save(
                new Portfolio("IT & Tech Leaders", "Top Indian IT and technology sector stocks", owner));
        addHolding(itLeaders, "TCS.NS", "Tata Consultancy Services", 5, "3500.00", "2023-02-10");
        addHolding(itLeaders, "INFY.NS", "Infosys Ltd.", 12, "1400.00", "2022-11-01");
        addHolding(itLeaders, "WIPRO.NS", "Wipro Ltd.", 20, "450.00", "2023-01-01");
        addHolding(itLeaders, "BAJFINANCE.NS", "Bajaj Finance Ltd.", 3, "7200.00", "2023-05-15");

        // Highly liquid, actively-traded US mega-caps so quote-driven P&L visibly moves during
        // US market hours - useful for demoing the "Refresh Prices" live-update button.
        Portfolio usGrowth = portfolioRepository.save(
                new Portfolio("US Growth Watchlist", "Actively-traded US tech stocks for live price demos", owner));
        addHolding(usGrowth, "AAPL", "Apple Inc.", 15, "180.00", "2023-04-01");
        addHolding(usGrowth, "MSFT", "Microsoft Corp.", 10, "330.00", "2023-03-15");
        addHolding(usGrowth, "NVDA", "NVIDIA Corp.", 8, "450.00", "2023-07-01");
        addHolding(usGrowth, "TSLA", "Tesla Inc.", 12, "220.00", "2023-05-20");
        addHolding(usGrowth, "AMZN", "Amazon.com Inc.", 10, "130.00", "2023-06-10");
    }

    private void addHolding(Portfolio portfolio, String ticker, String name, int quantity, String price, String date) {
        Holding holding = new Holding();
        holding.setPortfolio(portfolio);
        holding.setTicker(ticker);
        holding.setName(name);
        holding.setType(HoldingType.STOCK);
        holding.setQuantity(BigDecimal.valueOf(quantity));
        holding.setPurchasePrice(new BigDecimal(price));
        holding.setPurchaseDate(LocalDate.parse(date));
        holdingRepository.save(holding);
    }
}
