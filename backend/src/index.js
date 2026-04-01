require('dotenv').config();

const express = require('express');
const cors = require('cors');
const pool = require('./config/db');
const requireApiKey = require('./middleware/auth');
const tasksRouter = require('./routes/tasks');
const audioRouter = require('./routes/audio');
const textRouter = require('./routes/text');
const { initScheduler } = require('./services/scheduler');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// Health check (no auth required)
app.get('/api/health', async (req, res) => {
  try {
    await pool.query('SELECT NOW()');
    res.json({ status: 'ok', db: true, timestamp: new Date().toISOString() });
  } catch (err) {
    res.status(500).json({ status: 'error', db: false, error: err.message });
  }
});

// Protected routes
app.use('/api/tasks', requireApiKey, tasksRouter);
app.use('/api/audio', requireApiKey, audioRouter);
app.use('/api/text', requireApiKey, textRouter);

// Start server
app.listen(PORT, () => {
  console.log(`[Server] Running on http://localhost:${PORT}`);
  console.log(`[Server] Health: http://localhost:${PORT}/api/health`);
  initScheduler();
});

// Graceful shutdown
process.once('SIGINT', () => {
  console.log('[Server] Shutting down...');
  pool.end();
  process.exit(0);
});
