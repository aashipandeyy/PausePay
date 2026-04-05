CREATE TABLE transactions (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              account_id BIGINT NOT NULL,
                              date DATE NOT NULL,
                              description VARCHAR(255),
                              merchant_name VARCHAR(150),
                              amount DECIMAL(12, 2) NOT NULL,
                              type ENUM('DEBIT', 'CREDIT') NOT NULL,
                              category VARCHAR(50) DEFAULT 'UNCATEGORIZED',
                              is_anomaly BOOLEAN DEFAULT FALSE,
                              anomaly_reason VARCHAR(255),
                              categorized_at TIMESTAMP,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (account_id) REFERENCES bank_accounts(id),
                              INDEX idx_account_date (account_id, date),
                              INDEX idx_category (category)
);