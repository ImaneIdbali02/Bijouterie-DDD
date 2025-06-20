ALTER TABLE categories
ADD COLUMN seo_title VARCHAR(255),
ADD COLUMN seo_description VARCHAR(500),
ADD COLUMN keywords VARCHAR(1000),
ADD COLUMN meta_robots VARCHAR(50),
ADD COLUMN canonical_url VARCHAR(500),
ADD COLUMN schema_markup TEXT,
ADD COLUMN open_graph_title VARCHAR(255),
ADD COLUMN open_graph_description VARCHAR(500),
ADD COLUMN open_graph_image VARCHAR(500),
ADD COLUMN twitter_title VARCHAR(255),
ADD COLUMN twitter_description VARCHAR(500),
ADD COLUMN twitter_image VARCHAR(500); 