const cron = require('node-cron');
const pool = require('../config/db');

/**
 * Initialize cron jobs for daily summary and reminders.
 * Push notification integration will be added in Phase 8.
 * For now, just logs to console.
 */
function initScheduler() {
  // Daily summary at 8:00 AM Europe/Madrid
  cron.schedule('0 8 * * *', async () => {
    try {
      const today = await pool.query(
        `SELECT * FROM tasks WHERE status = 'pending' AND due_date = CURRENT_DATE ORDER BY due_time ASC NULLS LAST`
      );
      const overdue = await pool.query(
        `SELECT * FROM tasks WHERE status = 'pending' AND due_date < CURRENT_DATE ORDER BY due_date ASC`
      );

      console.log(`[Scheduler] Daily summary: ${today.rowCount} today, ${overdue.rowCount} overdue`);

      // TODO: Send push notification via FCM (Phase 8)
      if (today.rowCount > 0 || overdue.rowCount > 0) {
        const lines = [];
        if (overdue.rowCount > 0) {
          lines.push(`VENCIDAS (${overdue.rowCount}):`);
          overdue.rows.forEach(t => lines.push(`  - ${t.title}`));
        }
        if (today.rowCount > 0) {
          lines.push(`HOY (${today.rowCount}):`);
          today.rows.forEach(t => lines.push(`  - ${t.title}${t.due_time ? ' a las ' + t.due_time.substring(0, 5) : ''}`));
        }
        console.log('[Scheduler] Summary:\n' + lines.join('\n'));
      }
    } catch (err) {
      console.error('[Scheduler] Daily summary error:', err.message);
    }
  }, { timezone: 'Europe/Madrid' });

  // Reminders: check every minute
  cron.schedule('* * * * *', async () => {
    try {
      const result = await pool.query(`
        SELECT id, title, due_date, due_time, category
        FROM tasks
        WHERE status = 'pending'
          AND reminder_at IS NOT NULL
          AND reminder_at <= NOW()
          AND reminder_sent = false
      `);

      for (const task of result.rows) {
        console.log(`[Scheduler] Reminder: ${task.title} (${task.category})`);
        // TODO: Send push notification via FCM (Phase 8)

        await pool.query('UPDATE tasks SET reminder_sent = true WHERE id = $1', [String(task.id)]);
      }
    } catch (err) {
      console.error('[Scheduler] Reminder check error:', err.message);
    }
  }, { timezone: 'Europe/Madrid' });

  console.log('[Scheduler] Cron jobs initialized (daily summary 8:00 AM + reminders every minute)');
}

module.exports = { initScheduler };
