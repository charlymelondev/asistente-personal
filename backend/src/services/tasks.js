const pool = require('../config/db');

/**
 * Calculate reminder_at based on due_date and due_time.
 * - date + time → 1 hour before
 * - date only → 09:00 of that day
 * - no date → null
 */
function calculateReminderAt(dueDate, dueTime) {
  if (!dueDate) return null;

  if (dueTime) {
    const dt = new Date(`${dueDate}T${dueTime}:00`);
    dt.setHours(dt.getHours() - 1);
    return dt.toISOString();
  }

  return new Date(`${dueDate}T09:00:00`).toISOString();
}

/**
 * Insert multiple tasks into the database.
 * @param {Array} tasks - Array of task objects from extractor
 * @param {string} sourceText - Original transcription
 * @returns {Promise<Array>} - Created tasks with IDs
 */
async function createMany(tasks, sourceText) {
  const created = [];

  for (const task of tasks) {
    const reminderAt = calculateReminderAt(task.due_date, task.due_time);

    const result = await pool.query(
      `INSERT INTO tasks (title, description, category, priority, due_date, due_time, reminder_at, source_text)
       VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
       RETURNING *`,
      [
        task.title,
        task.description || null,
        task.category || 'general',
        task.priority || 'normal',
        task.due_date || null,
        task.due_time || null,
        reminderAt,
        sourceText,
      ]
    );

    created.push(result.rows[0]);
  }

  return created;
}

module.exports = { createMany, calculateReminderAt };
