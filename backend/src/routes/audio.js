const { Router } = require('express');
const multer = require('multer');
const path = require('path');
const { transcribe } = require('../services/whisper');
const { extractTasks } = require('../services/extractor');
const { createMany } = require('../services/tasks');

const upload = multer({
  dest: path.join(__dirname, '..', '..', 'uploads'),
  limits: { fileSize: 25 * 1024 * 1024 }, // 25MB (Whisper limit)
  fileFilter: (req, file, cb) => {
    if (file.mimetype.startsWith('audio/')) {
      cb(null, true);
    } else {
      cb(new Error('Only audio files are allowed'));
    }
  },
});

const router = Router();

// POST /api/audio — Upload audio, transcribe, extract tasks
router.post('/', upload.single('audio'), async (req, res) => {
  if (!req.file) {
    return res.status(400).json({ error: 'No audio file provided' });
  }

  try {
    console.log(`[Audio] Processing ${req.file.originalname} (${req.file.size} bytes)`);

    // 1. Transcribe with Whisper
    const transcript = await transcribe(req.file.path);
    console.log(`[Audio] Transcript: "${transcript}"`);

    // 2. Extract tasks with Claude
    const extracted = await extractTasks(transcript);
    console.log(`[Audio] Extracted ${extracted.length} tasks`);

    // 3. Save to database
    const tasks = await createMany(extracted, transcript);

    res.status(201).json({ transcript, tasks });
  } catch (err) {
    console.error('[Audio] Processing error:', err.message);
    res.status(500).json({ error: 'Failed to process audio' });
  }
});

module.exports = router;
