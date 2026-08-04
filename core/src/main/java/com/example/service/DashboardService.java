package com.example.service;

import com.example.model.AccessProfile;
import com.example.model.Admin;
import com.example.model.AdminDashboard;
import com.example.model.Asset;
import com.example.model.AssetAllocationSlice;
import com.example.model.Customer;
import com.example.model.CustomerDashboard;
import com.example.model.CustomerPortfolioSummary;
import com.example.model.DashboardTimePoint;
import com.example.model.FundManager;
import com.example.model.FundManagerDashboard;
import com.example.model.ManagedCustomerSummary;
import com.example.model.PermissionSummary;
import com.example.model.Portfolio;
import com.example.model.PortfolioHolding;
import com.example.model.UserAccessView;
import com.example.repository.PortfolioHoldingRepository;
import com.example.repository.TransactionRepository;
import com.example.security.RoleAccess.Role;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final AdminService adminService;
    private final FundManagerService fundManagerService;
    private final CustomerService customerService;
    private final PortfolioService portfolioService;
    private final AssetService assetService;
    private final PortfolioHoldingRepository portfolioHoldingRepository;
    private final TransactionRepository transactionRepository;

    public DashboardService(AdminService adminService,
                            FundManagerService fundManagerService,
                            CustomerService customerService,
                            PortfolioService portfolioService,
                            AssetService assetService,
                            PortfolioHoldingRepository portfolioHoldingRepository,
                            TransactionRepository transactionRepository) {
        this.adminService = adminService;
        this.fundManagerService = fundManagerService;
        this.customerService = customerService;
        this.portfolioService = portfolioService;
        this.assetService = assetService;
        this.portfolioHoldingRepository = portfolioHoldingRepository;
        this.transactionRepository = transactionRepository;
    }

    public AdminDashboard buildAdminDashboard() {
        List<Admin> admins = adminService.findAll();
        List<FundManager> fundManagers = fundManagerService.findAll();
        List<Customer> customers = customerService.findAll();
        List<Portfolio> portfolios = portfolioService.findAll();
        List<Asset> assets = assetService.findAll();

        BigDecimal assetsUnderManagement = portfolios.stream()
                .map(this::portfolioCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<UserAccessView> managedUsers = new ArrayList<>();
        admins.forEach(admin -> managedUsers.add(new UserAccessView(
                admin.getId(),
                admin.getName(),
                admin.getEmail(),
                "ADMIN",
                "System administration"
        )));
        fundManagers.forEach(fundManager -> managedUsers.add(new UserAccessView(
                fundManager.getId(),
                fundManager.getName(),
                fundManager.getEmail(),
                "FUND_MANAGER",
                "Customer and portfolio operations"
        )));
        customers.forEach(customer -> managedUsers.add(new UserAccessView(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                "CUSTOMER",
                "Read-only own portfolio access"
        )));

        return new AdminDashboard(
                admins.size(),
                fundManagers.size(),
                customers.size(),
                portfolios.size(),
                assets.size(),
                assetsUnderManagement,
                getPermissionCatalog(),
                managedUsers
        );
    }

    public FundManagerDashboard buildFundManagerDashboard(Long fundManagerId) {
        FundManager fundManager = fundManagerService.findById(fundManagerId).orElseThrow();
        List<Customer> customers = customerService.findByFundManagerId(fundManagerId);
        List<Portfolio> portfolios = new ArrayList<>();
        List<ManagedCustomerSummary> customerSummaries = new ArrayList<>();

        BigDecimal totalInvestment = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;

        for (Customer customer : customers) {
            List<Portfolio> customerPortfolios = portfolioService.findByCustomerId(customer.getId());
            portfolios.addAll(customerPortfolios);

            BigDecimal customerInvestment = customerPortfolios.stream()
                    .map(this::portfolioInvestment)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal customerValue = customerPortfolios.stream()
                    .map(this::portfolioCurrentValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal customerProfitLoss = customerValue.subtract(customerInvestment);
            BigDecimal customerReturn = percentage(customerInvestment, customerValue);

            totalInvestment = totalInvestment.add(customerInvestment);
            currentValue = currentValue.add(customerValue);

            customerSummaries.add(new ManagedCustomerSummary(
                    customer.getId(),
                    customer.getName(),
                    customer.getEmail(),
                    customerPortfolios.size(),
                    customerInvestment,
                    customerValue,
                    customerProfitLoss,
                    customerReturn
            ));
        }

        BigDecimal profitLoss = currentValue.subtract(totalInvestment);
        BigDecimal averageReturn = percentage(totalInvestment, currentValue);
        BigDecimal trendSeed = currentValue.compareTo(BigDecimal.ZERO) > 0
                ? currentValue
                : BigDecimal.valueOf(250000 + fundManagerId * 15000);

        List<String> alerts = new ArrayList<>();
        alerts.add("Dummy analytics are enabled for comparative trends until yfinance integration is connected.");
        alerts.add(customers.isEmpty()
                ? "No customers assigned yet."
                : "Top customer return is " + customerSummaries.stream()
                        .max(Comparator.comparing(ManagedCustomerSummary::getReturnPercentage))
                        .map(summary -> summary.getCustomerName() + " (" + summary.getReturnPercentage() + "%)")
                        .orElse("not available"));
        alerts.add("Recorded transactions: " + transactionRepository.findAll().size());

        return new FundManagerDashboard(
                fundManager.getId(),
                fundManager.getName(),
                customers.size(),
                portfolios.size(),
                currentValue,
                totalInvestment,
                profitLoss,
                averageReturn,
                customerSummaries,
                buildTrendSeries(trendSeed, 0.94, 0.97, 1.01, 1.04, 1.08, 1.12),
                buildTrendSeries(BigDecimal.valueOf(100), 0.96, 0.98, 0.99, 1.02, 1.04, 1.06),
                alerts
        );
    }

    public CustomerDashboard buildCustomerDashboard(Long customerId) {
        Customer customer = customerService.findById(customerId).orElseThrow();
        List<Portfolio> portfolios = portfolioService.findByCustomerId(customerId);
        List<CustomerPortfolioSummary> portfolioSummaries = new ArrayList<>();
        BigDecimal totalInvestment = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;
        Map<String, BigDecimal> allocationMap = new LinkedHashMap<>();

        for (Portfolio portfolio : portfolios) {
            BigDecimal investment = portfolioInvestment(portfolio);
            BigDecimal value = portfolioCurrentValue(portfolio);
            BigDecimal profitLoss = value.subtract(investment);
            totalInvestment = totalInvestment.add(investment);
            currentValue = currentValue.add(value);

            portfolioSummaries.add(new CustomerPortfolioSummary(
                    portfolio.getId(),
                    portfolio.getPortfolioName(),
                    investment,
                    value,
                    profitLoss,
                    percentage(investment, value)
            ));

            for (PortfolioHolding holding : portfolioHoldingRepository.findByPortfolioId(portfolio.getId())) {
                String label = assetService.findById(holding.getAssetId())
                        .map(Asset::getSymbol)
                        .orElse("UNKNOWN");
                allocationMap.merge(label, safeAmount(holding.getCurrentValue()), BigDecimal::add);
            }
        }

        List<AssetAllocationSlice> allocation = buildAllocation(allocationMap, currentValue);
        if (allocation.isEmpty()) {
            allocation = List.of(
                    new AssetAllocationSlice("Large Cap", BigDecimal.valueOf(40)),
                    new AssetAllocationSlice("Banking", BigDecimal.valueOf(25)),
                    new AssetAllocationSlice("Technology", BigDecimal.valueOf(20)),
                    new AssetAllocationSlice("Cash Buffer", BigDecimal.valueOf(15))
            );
        }

        BigDecimal profitLoss = currentValue.subtract(totalInvestment);
        BigDecimal overallReturn = percentage(totalInvestment, currentValue);
        BigDecimal trendSeed = currentValue.compareTo(BigDecimal.ZERO) > 0
                ? currentValue
                : BigDecimal.valueOf(100000 + customerId * 5000);

        List<String> insights = new ArrayList<>();
        insights.add("Portfolio analytics include placeholder market trends until yfinance is wired.");
        insights.add(portfolios.isEmpty()
                ? "Create a portfolio to begin tracking investment performance."
                : "You currently have " + portfolios.size() + " portfolio(s) under management.");
        insights.add("Current holdings tracked: " + allocation.size());

        return new CustomerDashboard(
                customer.getId(),
                customer.getName(),
                portfolios.size(),
                totalInvestment,
                currentValue,
                profitLoss,
                overallReturn,
                portfolioSummaries,
                allocation,
                buildTrendSeries(trendSeed, 0.93, 0.96, 1.00, 1.03, 1.05, 1.09),
                buildTrendSeries(BigDecimal.valueOf(100), 0.95, 0.97, 1.00, 1.01, 1.03, 1.04),
                insights
        );
    }

    public AccessProfile buildAccessProfile(Role role, Long adminId, Long fundManagerId, Long customerId) {
        return new AccessProfile(
                role.name(),
                adminId,
                fundManagerId,
                customerId,
                switch (role) {
                    case ADMIN -> "/admin";
                    case FUND_MANAGER -> "/fund-manager";
                    case CUSTOMER -> "/customer";
                },
                getPermissionsForRole(role)
        );
    }

    public List<PermissionSummary> getPermissionCatalog() {
        return List.of(
                new PermissionSummary("ADMIN", "General sysadmin and permission management", getPermissionsForRole(Role.ADMIN)),
                new PermissionSummary("FUND_MANAGER", "Manages assigned customers and their portfolios", getPermissionsForRole(Role.FUND_MANAGER)),
                new PermissionSummary("CUSTOMER", "Can view own portfolio dashboards and statistics", getPermissionsForRole(Role.CUSTOMER))
        );
    }

    public List<String> getPermissionsForRole(Role role) {
        return switch (role) {
            case ADMIN -> List.of(
                    "manage-admin-users",
                    "manage-fund-managers",
                    "manage-customers",
                    "manage-portfolios",
                    "view-system-dashboard",
                    "view-permission-matrix"
            );
            case FUND_MANAGER -> List.of(
                    "manage-assigned-customers",
                    "manage-assigned-portfolios",
                    "execute-buy-transactions",
                    "view-manager-dashboard",
                    "view-customer-analytics"
            );
            case CUSTOMER -> List.of(
                    "view-own-dashboard",
                    "view-own-portfolios",
                    "view-own-analytics"
            );
        };
    }

    private List<DashboardTimePoint> buildTrendSeries(BigDecimal seed, double... multipliers) {
        String[] labels = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
        List<DashboardTimePoint> points = new ArrayList<>();
        for (int i = 0; i < multipliers.length && i < labels.length; i++) {
            BigDecimal value = seed.multiply(BigDecimal.valueOf(multipliers[i])).setScale(2, RoundingMode.HALF_UP);
            points.add(new DashboardTimePoint(labels[i], value));
        }
        return points;
    }

    private List<AssetAllocationSlice> buildAllocation(Map<String, BigDecimal> allocationMap, BigDecimal currentValue) {
        if (allocationMap.isEmpty()) {
            return List.of();
        }
        List<AssetAllocationSlice> slices = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : allocationMap.entrySet()) {
            BigDecimal percentage = currentValue.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().multiply(ONE_HUNDRED).divide(currentValue, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            slices.add(new AssetAllocationSlice(entry.getKey(), percentage));
        }
        return slices;
    }

    private BigDecimal portfolioInvestment(Portfolio portfolio) {
        return safeAmount(portfolio.getTotalInvestment());
    }

    private BigDecimal portfolioCurrentValue(Portfolio portfolio) {
        return safeAmount(portfolio.getCurrentValue());
    }

    private BigDecimal percentage(BigDecimal base, BigDecimal current) {
        if (base == null || base.compareTo(BigDecimal.ZERO) <= 0 || current == null) {
            return BigDecimal.ZERO;
        }
        return current.subtract(base)
                .multiply(ONE_HUNDRED)
                .divide(base, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
