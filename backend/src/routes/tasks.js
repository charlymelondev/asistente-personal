const { Router } = require('express');
const pool = require('../config/db');

const router = Router();

// GET /api/tasks — List tasks with optional filters
router.get('/', async (req, res) => {
  try {
    const { status, category, from, to, priority } = req.query;
    const conditions = [];
    const params = [];
    let idx = 1;

    if (status && status !== 'all') {
      conditions.push(`status = $${idx++}`);
      params.push(status);
    }
    if (category) {
      conditions.push(`category = $${idx++}`);
      params.push(category);
    }
    if (priority) {
      conditions.push(`priority = $${idx++}`);
      params.push(priority);
    }
    if (from) {
      conditions.push(`due_date >= $${idx++}`);
      params.push(from);
    }
    if (to) {
      conditions.push(`due_date <= $${idx++}`);
      params.push(to);
    }

    const where = conditions.length > 0 ? `WHERE ${conditions.join(' AND ')}` : '';
    const query = `
      SELECT * FROM tasks ${where}
      ORDER BY
        CASE priority WHEN 'urgent' THEN 0 WHEN 'high' THEN 1 WHEN 'normal' THEN 2 WHEN 'low' THEN 3 END,
        due_date ASC NULLS LAST,
        created_at DESC
    `;

    const result = await pool.query(query, params);
    res.json({ tasks: result.rows, total: result.rowCount });
  } catch (err) {
    console.error('[Tasks] List error:', err.message);
    res.status(500).json({ error: 'Failed to fetch tasks' });
  }
});

// GET /api/tasks/today
router.get('/today', async (req, res) => {
  try {
    const result = await pool.query(
      `SELECT * FROM tasks
       WHERE status = 'pending' AND due_date = CURRENT_DATE
       ORDER BY due_time ASC NULLS LAST, priority ASC`
    );
    res.json({ tasks: result.rows, total: result.rowCount });
  } catch (err) {
    console.error('[Tasks] Today error:', err.message);
    res.status(500).json({ error: 'Failed to fetch today tasks' });
  }
});

// GET /api/tasks/week
router.get('/week', async (req, res) => {
  try {
    const result = await pool.query(
      `SELECT * FROM tasks
       WHERE status = 'pending'
         AND due_date BETWEEN CURRENT_DATE AND CURRENT_DATE + interval '6 days'
       ORDER BY due_date ASC, due_time ASC NULLS LAST`
    );
    res.json({ tasks: result.rows, total: result.rowCount });
  } catch (err) {
    console.error('[Tasks] Week error:', err.message);
    res.status(500).json({ error: 'Failed to fetch week tasks' });
  }
});

// GET /api/tasks/overdue
router.get('/overdue', async (req, res) => {
  try {
    const result = await pool.query(
      `SELECT * FROM tasks
       WHERE status = 'pending' AND due_date < CURRENT_DATE
       ORDER BY due_date ASC`
    );
    res.json({ tasks: result.rows, total: result.rowCount });
  } catch (err) {
    console.error('[Tasks] Overdue error:', err.message);
    res.status(500).json({ error: 'Failed to fetch overdue tasks' });
  }
});

// GET /api/tasks/inbox — Tasks without a due date
router.get('/inbox', async (req, res) => {
  try {
    const result = await pool.query(
      `SELECT * FROM tasks
       WHERE status = 'pending' AND due_date IS NULL
       ORDER BY
         CASE priority WHEN 'urgent' THEN 0 WHEN 'high' THEN 1 WHEN 'normal' THEN 2 WHEN 'low' THEN 3 END,
         created_at DESC`
    );
    res.json({ tasks: result.rows, total: result.rowCount });
  } catch (err) {
    console.error('[Tasks] Inbox error:', err.message);
    res.status(500).json({ error: 'Failed to fetch inbox tasks' });
  }
});

// GET /api/tasks/summary
router.get('/summary', async (req, res) => {
  try {
    const [total, byCategory, overdue, today, week, inbox] = await Promise.all([
      pool.query(`SELECT COUNT(*)::int AS count FROM tasks WHERE status = 'pending'`),
      pool.query(`SELECT category, COUNT(*)::int AS count FROM tasks WHERE status = 'pending' GROUP BY category`),
      pool.query(`SELECT COUNT(*)::int AS count FROM tasks WHERE status = 'pending' AND due_date < CURRENT_DATE`),
      pool.query(`SELECT COUNT(*)::int AS count FROM tasks WHERE status = 'pending' AND due_date = CURRENT_DATE`),
      pool.query(`SELECT COUNT(*)::int AS count FROM tasks WHERE status = 'pending' AND due_date BETWEEN CURRENT_DATE AND CURRENT_DATE + interval '6 days'`),
      pool.query(`SELECT COUNT(*)::int AS count FROM tasks WHERE status = 'pending' AND due_date IS NULL`),
    ]);

    const categoryMap = {};
    byCategory.rows.forEach((r) => { categoryMap[r.category] = r.count; });

    res.json({
      total_pending: total.rows[0].count,
      by_category: categoryMap,
      overdue: overdue.rows[0].count,
      today: today.rows[0].count,
      this_week: week.rows[0].count,
      inbox: inbox.rows[0].count,
    });
  } catch (err) {
    console.error('[Tasks] Summary error:', err.message);
    res.status(500).json({ error: 'Failed to fetch summary' });
  }
});

// PATCH /api/tasks/:id — Update a task
router.patch('/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const allowed = ['title', 'description', 'category', 'priority', 'due_date', 'due_time', 'reminder_at', 'status'];
    const sets = [];
    const params = [];
    let idx = 1;

    for (const field of allowed) {
      if (req.body[field] !== undefined) {
        sets.push(`${field} = $${idx++}`);
        params.push(req.body[field]);
      }
    }

    // Auto-set done_at when marking as done
    if (req.body.status === 'done') {
      sets.push(`done_at = NOW()`);
    }

    if (sets.length === 0) {
      return res.status(400).json({ error: 'No valid fields to update' });
    }

    params.push(id);
    const result = await pool.query(
      `UPDATE tasks SET ${sets.join(', ')} WHERE id = $${idx} RETURNING *`,
      params
    );

    if (result.rowCount === 0) {
      return res.status(404).json({ error: 'Task not found' });
    }

    res.json({ task: result.rows[0] });
  } catch (err) {
    console.error('[Tasks] Update error:', err.message);
    res.status(500).json({ error: 'Failed to update task' });
  }
});

// DELETE /api/tasks/:id
router.delete('/:id', async (req, res) => {
  try {
    const result = await pool.query('DELETE FROM tasks WHERE id = $1', [req.params.id]);
    if (result.rowCount === 0) {
      return res.status(404).json({ error: 'Task not found' });
    }
    res.json({ deleted: true });
  } catch (err) {
    console.error('[Tasks] Delete error:', err.message);
    res.status(500).json({ error: 'Failed to delete task' });
  }
});

module.exports = router;
