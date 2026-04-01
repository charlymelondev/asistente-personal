const fs = require('fs');
const Groq = require('groq-sdk');

const groq = new Groq({ apiKey: process.env.GROQ_API_KEY });

/**
 * Transcribe an audio file using Groq Whisper API (free).
 * @param {string} filePath - Path to the audio file (m4a, mp3, wav, ogg, etc.)
 * @returns {Promise<string>} - Transcribed text
 */
async function transcribe(filePath) {
  // Groq needs a file with proper extension
  const ext = '.m4a';
  const renamedPath = filePath + ext;
  fs.renameSync(filePath, renamedPath);

  const file = fs.createReadStream(renamedPath);

  const response = await groq.audio.transcriptions.create({
    file,
    model: 'whisper-large-v3',
    language: 'es',
  });

  // Clean up temp file
  fs.unlink(renamedPath, (err) => {
    if (err) console.error('[Whisper] Failed to delete temp file:', err.message);
  });

  return response.text;
}

module.exports = { transcribe };
