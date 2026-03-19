-- Migration: 001_create_exhibitions.sql
-- Run this in the Supabase dashboard SQL editor:
-- https://supabase.com/dashboard → your project → SQL Editor

CREATE TABLE IF NOT EXISTS exhibitions (
  id              TEXT PRIMARY KEY,
  name            TEXT NOT NULL,
  venue_name      TEXT NOT NULL,
  city            TEXT NOT NULL,
  region          TEXT NOT NULL,
  opening_date    DATE NOT NULL,
  closing_date    DATE NOT NULL,
  is_featured     BOOLEAN NOT NULL DEFAULT false,
  is_editors_pick BOOLEAN NOT NULL DEFAULT false,
  latitude        DOUBLE PRECISION,
  longitude       DOUBLE PRECISION,
  description     TEXT NOT NULL DEFAULT '',
  cover_image_url TEXT,
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE exhibitions ENABLE ROW LEVEL SECURITY;

-- Allow anonymous (public) read access via the anon key
CREATE POLICY "Public read"
  ON exhibitions FOR SELECT
  USING (true);

-- INSERT/UPDATE/DELETE require the service role key (used only by the GAS sync script)
-- No policies needed for write — service role bypasses RLS by default
