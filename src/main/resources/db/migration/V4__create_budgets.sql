CREATE TABLE budgets (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id BIGINT NOT NULL,
                         category VARCHAR(50) NOT NULL,
                         monthly_limit DECIMAL(12, 2) NOT NULL,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         UNIQUE KEY unique_user_category (user_id, category),
                         FOREIGN KEY (user_id) REFERENCES users(id)
);