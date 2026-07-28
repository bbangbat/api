-- 초기 스키마 (bbangbat)
-- Hibernate ddl-auto로 생성된 현재 스키마를 Flyway baseline V1로 고정

CREATE TABLE `congestion_votes` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `store_id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `voted_at` datetime(6) NOT NULL,
  `voter_key` varchar(100) NOT NULL,
  `level` enum('CROWDED','NORMAL','UNCROWDED') NOT NULL,
  `voter_type` enum('GUEST','MEMBER') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_congestion_voter` (`store_id`,`voter_type`,`voter_key`),
  KEY `idx_congestion_store_voted_at` (`store_id`,`voted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `favorites` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `store_id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_favorite_member_store` (`member_id`,`store_id`),
  KEY `idx_favorite_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `live_talk_messages` (
  `author_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `store_id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `author_nickname` varchar(20) NOT NULL,
  `content` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_talk_store_created_at` (`store_id`,`created_at`),
  KEY `idx_talk_author_id` (`author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `members` (
  `privacy_agreed` bit(1) NOT NULL,
  `terms_agreed` bit(1) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL,
  `last_login_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `nickname` varchar(20) NOT NULL,
  `name` varchar(30) NOT NULL,
  `email` varchar(100) NOT NULL,
  `profile_image_url` varchar(500) DEFAULT NULL,
  `age_group` enum('FIFTIES','FORTIES','SIXTIES_PLUS','TEENS','THIRTIES','TWENTIES') NOT NULL,
  `gender` enum('FEMALE','MALE') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK9d30a9u1qpg8eou0otgkwrp5d` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `review_images` (
  `display_order` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `review_id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `image_url` varchar(500) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3aayo5bjciyemf3bvvt987hkr` (`review_id`),
  CONSTRAINT `FK3aayo5bjciyemf3bvvt987hkr` FOREIGN KEY (`review_id`) REFERENCES `reviews` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `review_menus` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `review_id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `menu_name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8xpnihyxakmqlo5lo6dhhx78f` (`review_id`),
  CONSTRAINT `FK8xpnihyxakmqlo5lo6dhhx78f` FOREIGN KEY (`review_id`) REFERENCES `reviews` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `reviews` (
  `rating` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `store_id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `content` varchar(500) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_reviews_store_id` (`store_id`),
  KEY `idx_reviews_member_id_id` (`member_id`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `social` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL,
  `member_id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `provider_id` varchar(100) NOT NULL,
  `provider` enum('KAKAO','NAVER') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKdwpg6ph68brgipay0twkutqf` (`provider`,`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `store_talk_summaries` (
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `last_message_id` bigint NOT NULL,
  `store_id` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `summary` varchar(500) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKmopfsyb6dj61ofaac9g8kaicx` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `stores` (
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `updated_at` datetime(6) NOT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `image_url` varchar(500) DEFAULT NULL,
  `address` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
