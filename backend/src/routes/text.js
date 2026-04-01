const { Router } = require('express');
const { extractTasks } = require('../services/extractor');
const { createMany } = require('../services/tasks');

const router = Router();

// POST /api/text — Send text, extract tasks
router.post('/', async (req, res) => {
  const { text } = req.body;

  if (!text || typeof text !== 'string' || text.trim().length === 0) {
    return res.status(400).json({ error: 'Text is required' });
  }

  try {
    console.log(`[Text] Processing: "${text.substring(0, 100)}..."`);

    // 1. Extract tasks with Claude
    const extracted = await extractTasks(text.trim());
    console.log(`[Text] Extracted ${extracted.length} tasks`);

    // 2. Save to database
    const tasks = await createMany(extracted, text.trim());

    res.status(201).json({ tasks });
  } catch (err) {
    console.error('[Text] Processing error:', err.message);
    res.status(500).json({ error: 'Failed to process text' });
  }
});

module.exports = router;
