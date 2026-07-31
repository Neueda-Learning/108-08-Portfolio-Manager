# 108-08-Portfolio-Manager


Project Architecture --------

> **Fund Manager → manages multiple Customers → each customer has one or more portfolios → portfolios contain assets → dashboard compares performance against Sensex/index funds.

---

# Project Name

**Portfolio Management System**

## Problem Statement

A wealth management platform where fund managers can manage client investments, monitor portfolio performance, analyze returns against market benchmarks, and make portfolio adjustments.

---

# 1. User Roles

Instead of one user:

## Admin / Fund Manager

Responsibilities:

* Login
* Manage customers
* Create customer portfolios
* Add/remove stocks, bonds, cash
* Edit customer holdings
* View analytics
* Compare performance against Sensex
* Generate reports

## Customer (Investor)

Responsibilities:

* View own portfolio
* View returns
* View asset allocation
* View performance graphs
* View fund manager details

---

# High-Level Architecture

```
                 React Frontend
                       |
                       |
                 REST API
                       |
              Spring Boot Backend
                       |
        --------------------------------
        |              |               |
     Customer      Portfolio        Analytics
     Service       Service          Service
        |              |               |
        --------------------------------
                       |
                  PostgreSQL
```

---

# Technology Stack

## Backend

* Java 17
* Spring Boot
* Spring MVC
* Spring Data JPA
* Hibernate
* PostgreSQL
* REST APIs
* Swagger

## Frontend

* React
* Tailwind CSS
* Recharts / Chart.js

## External APIs

Market data:

* Yahoo Finance API
* NSE/BSE API (optional)

---

# Database Design

## Entity Relationship

```
FundManager

        |
        |
        |
Customers

        |
        |
        |
Portfolio

        |
        |
        |
Holdings

        |
        |
        |
Stocks
```

---

# Tables

## 1. Fund_Manager

```
fund_manager

id
name
email
phone
password
created_at
```

Example:

| id | name         |
| -- | ------------ |
| 1  | Rahul Sharma |

---

# 2. Customer

```
customer

id
name
email
phone
risk_profile
fund_manager_id
created_at
```

Relationship:

Many customers belong to one fund manager.

Example:

```
Rahul(Fund Manager)

        |
        |
------------------
|        |        |
John    Mike    Sarah
```

---

# 3. Portfolio

```
portfolio

id
customer_id
portfolio_name
total_value
created_date
risk_level
```

Example:

```
John

Portfolio:
------------
Retirement Fund
Value:
₹20,00,000
```

---

# 4. Asset

Stocks/Bonds/Cash

```
asset

id

symbol

name

type

current_price
```

Example:

| Symbol   | Type      |
| -------- | --------- |
| RELIANCE | Stock     |
| HDFC     | Stock     |
| GOLD     | Commodity |

---

# 5. Portfolio Holdings

This connects portfolio and assets.

```
portfolio_holding

id

portfolio_id

asset_id

quantity

buy_price

purchase_date
```

Example:

John Portfolio:

```
RELIANCE

Quantity:
100

Buy Price:
2200
```

---

# 6. Transaction History

For audit.

```
transaction

id

portfolio_id

asset_id

transaction_type

quantity

price

date
```

Example:

```
BUY RELIANCE
100 shares
₹2200
```

---

# 7. Benchmark Performance

For Sensex comparison

```
benchmark

id

name

date

value
```

Example:

```
Sensex

01-01-2026

72000
```

---

# API Design

## Authentication

```
POST

/api/auth/login
```

---

# Fund Manager APIs

## View Customers

```
GET

/api/fund-manager/{id}/customers
```

Response:

```json
[
 {
  "id":1,
  "name":"John",
  "portfolioValue":2000000
 }
]
```

---

## Add Customer

```
POST

/api/customers
```

Request:

```json
{
"name":"John",
"email":"john@gmail.com",
"riskProfile":"HIGH"
}
```

---

## Edit Customer

```
PUT

/api/customers/{id}
```

---

# Portfolio APIs

## Create Portfolio

```
POST

/api/portfolio
```

Example:

```json
{
"name":"Retirement Portfolio",
"customerId":1
}
```

---

## Add Stock

```
POST

/api/portfolio/{id}/holdings
```

Request:

```json
{
"symbol":"TCS",
"quantity":20,
"buyPrice":3500
}
```

---

## Remove Stock

```
DELETE

/api/holding/{id}
```

---

# Analytics APIs

## Portfolio Performance

```
GET

/api/portfolio/{id}/performance
```

Response:

```json
{
"investment":1000000,

"currentValue":1250000,

"returns":25,

"cagr":12.5
}
```

---

# Dashboard Requirements

Fund Manager Dashboard:

## Overview Cards

```
Total Customers

20


Assets Under Management

₹5 Crore


Average Return

14%


Best Performing Client

John
```

---

## Customer Performance Table

```
Customer      Portfolio       Return

John          20L             +18%

Mike          15L             +12%

Sarah         10L             +22%
```

---

# Portfolio Analytics

Graphs:

## 1. Portfolio Growth

Line chart:

```
Value

|
|          *
|       *
|    *
| *
----------------
    Months
```

---

## 2. Asset Allocation

Pie Chart:

```
Stocks       60%

Bonds        30%

Cash         10%
```

---

## 3. Benchmark Comparison

Very important requirement.

Compare:

```
Portfolio Return

vs

Sensex Return

vs

Index Fund
```

Example:

```
1 Year Performance


Portfolio       +18%

Sensex          +12%

Nifty Index     +14%
```

---

# AI Features (Good for Extra Marks)

Add:

## AI Portfolio Advisor

Example:

Customer portfolio:

```
Technology Stocks 70%
```

AI says:

```
Risk Alert:

Your portfolio is heavily concentrated in technology.

Suggested allocation:

Technology 40%
Banks 20%
Bonds 30%
Cash 10%
```

---

## Natural Language Query

Customer asks:

> "Which stock performed best this year?"

AI responds:

```
TCS gave the highest return of 22%.
```

---

# Advanced Feature: Portfolio Risk Score

Calculate:

```
Risk Score =

Volatility +
Sector Concentration +
Historical Loss
```

Display:

```
Low Risk
Medium Risk
High Risk
```

---

# Spring Boot Package Structure

```
com.wealthsphere

│
├── controller
│
├── service
│
├── repository
│
├── model
│
├── dto
│
├── exception
│
└── security

---

For the next step, I would suggest we build the **complete Spring Boot backend database model + entity classes + relationships first**, because everything depends on that.
