const { Pool } = require('pg');

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
});

// Set timezone on every new connection
pool.on('connect', (client) => {
  client.query("SET timezone = 'Europe/Madrid'");
});

pool.on('error', (err) => {
  console.error('[DB] Unexpected error on idle client:', err.message);
});

module.exports = pool;
