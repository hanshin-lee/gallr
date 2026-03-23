-- Migration: 002_bilingual_columns.sql
-- Rename existing single-language columns to _ko, add _en columns

ALTER TABLE exhibitions RENAME COLUMN name TO name_ko;
ALTER TABLE exhibitions RENAME COLUMN venue_name TO venue_name_ko;
ALTER TABLE exhibitions RENAME COLUMN city TO city_ko;
ALTER TABLE exhibitions RENAME COLUMN region TO region_ko;
ALTER TABLE exhibitions RENAME COLUMN description TO description_ko;

ALTER TABLE exhibitions ADD COLUMN name_en TEXT NOT NULL DEFAULT '';
ALTER TABLE exhibitions ADD COLUMN venue_name_en TEXT NOT NULL DEFAULT '';
ALTER TABLE exhibitions ADD COLUMN city_en TEXT NOT NULL DEFAULT '';
ALTER TABLE exhibitions ADD COLUMN region_en TEXT NOT NULL DEFAULT '';
ALTER TABLE exhibitions ADD COLUMN description_en TEXT NOT NULL DEFAULT '';
ALTER TABLE exhibitions ADD COLUMN address_ko TEXT NOT NULL DEFAULT '';
ALTER TABLE exhibitions ADD COLUMN address_en TEXT NOT NULL DEFAULT '';
