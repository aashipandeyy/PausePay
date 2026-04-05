CREATE TABLE bank_accounts (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               bank_name VARCHAR(100) NOT NULL,
                               account_label VARCHAR(100) NOT NULL,
                               currency VARCHAR(3) DEFAULT 'INR',
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (user_id) REFERENCES users(id)
);