const { Pool } = require('pg');
const fs = require('fs');
const path = require('path');

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: process.env.DATABASE_URL?.includes('render.com') || process.env.DATABASE_URL?.includes('neon.tech')
    ? { rejectUnauthorized: false }
    : false,
});

// Set timezone on every new connection
pool.on('connect', (client) => {
  client.query("SET timezone = 'Europe/Madrid'");
});

pool.on('error', (err) => {
  console.error('[DB] Unexpected error on idle client:', err.message);
});

// Auto-migrate on startup
async function migrate() {
  try {
    const schemaPath = path.join(__dirname, '..', 'sql', 'schema.sql');
    const schema = fs.readFileSync(schemaPath, 'utf8');
    await pool.query(schema);
    console.log('[DB] Schema migrated successfully');
  } catch (err) {
    console.error('[DB] Migration error:', err.message);
  }
}

migrate();

module.exports = pool;
