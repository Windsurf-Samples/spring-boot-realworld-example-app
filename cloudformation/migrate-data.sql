
CREATE TABLE IF NOT EXISTS tags (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tags_name (name)
);

INSERT IGNORE INTO tags (id, name) VALUES 
    ('tag-1', 'javascript'),
    ('tag-2', 'nodejs'),
    ('tag-3', 'aws'),
    ('tag-4', 'serverless'),
    ('tag-5', 'lambda'),
    ('tag-6', 'apigateway'),
    ('tag-7', 'cloudformation'),
    ('tag-8', 'mysql'),
    ('tag-9', 'aurora'),
    ('tag-10', 'spring-boot');

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    bio TEXT,
    image VARCHAR(511),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS articles (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255),
    slug VARCHAR(255) UNIQUE,
    title VARCHAR(255),
    description TEXT,
    body TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_articles_user_id (user_id),
    INDEX idx_articles_slug (slug),
    INDEX idx_articles_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS article_tags (
    article_id VARCHAR(255) NOT NULL,
    tag_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (article_id, tag_id),
    INDEX idx_article_tags_article_id (article_id),
    INDEX idx_article_tags_tag_id (tag_id)
);

CREATE TABLE IF NOT EXISTS article_favorites (
    article_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (article_id, user_id),
    INDEX idx_article_favorites_article_id (article_id),
    INDEX idx_article_favorites_user_id (user_id)
);

CREATE TABLE IF NOT EXISTS follows (
    user_id VARCHAR(255) NOT NULL,
    follow_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, follow_id),
    INDEX idx_follows_user_id (user_id),
    INDEX idx_follows_follow_id (follow_id)
);

CREATE TABLE IF NOT EXISTS comments (
    id VARCHAR(255) PRIMARY KEY,
    body TEXT,
    article_id VARCHAR(255),
    user_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_comments_article_id (article_id),
    INDEX idx_comments_user_id (user_id),
    INDEX idx_comments_created_at (created_at)
);
