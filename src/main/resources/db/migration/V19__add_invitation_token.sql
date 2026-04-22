ALTER TABLE admin_users ADD COLUMN invitation_token VARCHAR(255) UNIQUE;
ALTER TABLE admin_users ADD COLUMN invitation_sent_at TIMESTAMP;