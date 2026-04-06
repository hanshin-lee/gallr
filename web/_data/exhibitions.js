// Build-time fetch of current exhibitions from Supabase.
// Used by _includes/this-week.html to render "Now Showing" cards.
// Falls back to seed data if Supabase is unreachable.

const SEED = require("./exhibitions-seed.json");

module.exports = async function () {
  const supabaseUrl = process.env.SUPABASE_URL;
  const anonKey = process.env.SUPABASE_ANON_KEY;

  if (!supabaseUrl || !anonKey) {
    console.log("[exhibitions] No Supabase credentials — using seed data");
    return SEED;
  }

  const today = new Date().toISOString().split("T")[0];
  const params = new URLSearchParams({
    select: "name_en,venue_name_en,city_en,opening_date,closing_date",
    city_en: "eq.Seoul",
    closing_date: `gte.${today}`,
    order: "opening_date.desc",
    limit: "4",
  });

  const url = `${supabaseUrl}/rest/v1/exhibitions?${params}`;

  try {
    const res = await fetch(url, {
      headers: {
        apikey: anonKey,
        Authorization: `Bearer ${anonKey}`,
      },
    });

    if (!res.ok) {
      console.error(`[exhibitions] Supabase returned ${res.status}`);
      return SEED;
    }

    const rows = await res.json();

    if (!rows.length) {
      console.log("[exhibitions] No exhibitions returned — using seed data");
      return SEED;
    }

    return rows.map((row) => ({
      title: row.name_en || "Untitled",
      gallery: row.venue_name_en || "Gallery",
      city: row.city_en || "Seoul",
      dates: formatDates(row.opening_date, row.closing_date),
    }));
  } catch (err) {
    console.error("[exhibitions] Fetch failed:", err.message);
    return SEED;
  }
};

function formatDates(open, close) {
  try {
    const fmt = (d) =>
      new Date(d).toLocaleDateString("en-US", { month: "short", day: "numeric" });
    return `${fmt(open)} — ${fmt(close)}`;
  } catch {
    return `${open} — ${close}`;
  }
}
