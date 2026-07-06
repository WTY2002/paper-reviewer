CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  display_name VARCHAR(100),
  default_output_language VARCHAR(20),
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE papers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(500),
  original_filename VARCHAR(255) NOT NULL,
  file_path VARCHAR(1000) NOT NULL,
  file_size BIGINT NOT NULL,
  page_count INT,
  language VARCHAR(50),
  status VARCHAR(50) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_papers_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE paper_extractions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  paper_id BIGINT NOT NULL,
  extracted_text LONGTEXT,
  page_count INT,
  extraction_status VARCHAR(50) NOT NULL,
  error_message TEXT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_extractions_paper_id (paper_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reviews (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  paper_id BIGINT NOT NULL,
  review_type VARCHAR(50) NOT NULL,
  status VARCHAR(50) NOT NULL,
  source_language VARCHAR(50),
  output_language VARCHAR(50),
  field_analysis_json JSON,
  editorial_decision_markdown LONGTEXT,
  revision_roadmap_markdown LONGTEXT,
  author_questions_markdown LONGTEXT,
  error_message TEXT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_reviews_user_id (user_id),
  INDEX idx_reviews_paper_id (paper_id),
  INDEX idx_reviews_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reviewer_teams (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  review_id BIGINT NOT NULL,
  target_venue VARCHAR(500),
  team_json JSON NOT NULL,
  confirmed_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_reviewer_teams_review_id (review_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE review_reports (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  review_id BIGINT NOT NULL,
  reviewer_role VARCHAR(50) NOT NULL,
  content_markdown LONGTEXT,
  scores_json JSON,
  status VARCHAR(50) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_review_role (review_id, reviewer_role),
  INDEX idx_reports_review_id (review_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE review_events (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  review_id BIGINT NOT NULL,
  event_type VARCHAR(100) NOT NULL,
  stage VARCHAR(50),
  reviewer_role VARCHAR(50),
  sequence_no BIGINT NOT NULL,
  event_payload JSON,
  created_at DATETIME NOT NULL,
  UNIQUE KEY uk_events_review_sequence (review_id, sequence_no),
  INDEX idx_events_review_id_seq (review_id, sequence_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE rereviews (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  original_review_id BIGINT NOT NULL,
  revised_paper_id BIGINT NOT NULL,
  response_paper_id BIGINT NOT NULL,
  output_language VARCHAR(50) NOT NULL,
  status VARCHAR(50) NOT NULL,
  result_markdown LONGTEXT,
  checklist_json JSON,
  error_message TEXT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_rereviews_user_id (user_id),
  INDEX idx_rereviews_original_review_id (original_review_id),
  INDEX idx_rereviews_revised_paper_id (revised_paper_id),
  INDEX idx_rereviews_response_paper_id (response_paper_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE exports (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  review_id BIGINT NULL,
  rereview_id BIGINT NULL,
  export_type VARCHAR(50) NOT NULL,
  file_path VARCHAR(1000) NOT NULL,
  status VARCHAR(50) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_exports_user_id (user_id),
  INDEX idx_exports_review_id (review_id),
  INDEX idx_exports_rereview_id (rereview_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE papers ADD CONSTRAINT fk_papers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE paper_extractions ADD CONSTRAINT fk_extractions_paper FOREIGN KEY (paper_id) REFERENCES papers(id) ON DELETE CASCADE;
ALTER TABLE reviews ADD CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE reviews ADD CONSTRAINT fk_reviews_paper FOREIGN KEY (paper_id) REFERENCES papers(id) ON DELETE CASCADE;
ALTER TABLE reviewer_teams ADD CONSTRAINT fk_teams_review FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE;
ALTER TABLE review_reports ADD CONSTRAINT fk_reports_review FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE;
ALTER TABLE review_events ADD CONSTRAINT fk_events_review FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE;
ALTER TABLE rereviews ADD CONSTRAINT fk_rereviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE rereviews ADD CONSTRAINT fk_rereviews_review FOREIGN KEY (original_review_id) REFERENCES reviews(id) ON DELETE CASCADE;
ALTER TABLE rereviews ADD CONSTRAINT fk_rereviews_revised FOREIGN KEY (revised_paper_id) REFERENCES papers(id) ON DELETE CASCADE;
ALTER TABLE rereviews ADD CONSTRAINT fk_rereviews_response FOREIGN KEY (response_paper_id) REFERENCES papers(id) ON DELETE CASCADE;
ALTER TABLE exports ADD CONSTRAINT fk_exports_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE exports ADD CONSTRAINT fk_exports_review FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE;
ALTER TABLE exports ADD CONSTRAINT fk_exports_rereview FOREIGN KEY (rereview_id) REFERENCES rereviews(id) ON DELETE CASCADE;
