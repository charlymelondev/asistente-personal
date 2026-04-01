const fs = require('fs');
const path = require('path');
const Groq = require('groq-sdk');

const groq = new Groq({ apiKey: process.env.GROQ_API_KEY });

const SYSTEM_PROMPT = fs.readFileSync(
  path.join(__dirname, '..', 'prompts', 'extract-tasks.txt'),
  'utf-8'
);

/**
 * Extract tasks from a Spanish text using Groq Llama 3.3 (free).
 * @param {string} text - Transcribed or manually typed text
 * @returns {Promise<Array>} - Array of task objects
 */
async function extractTasks(text) {
  const today = new Date().toLocaleDateString('en-CA'); // YYYY-MM-DD
  const dayOfWeek = new Date().toLocaleDateString('es-ES', { weekday: 'long' });
  const prompt = SYSTEM_PROMPT.replace('{{TODAY}}', `${today} (${dayOfWeek})`);

  const response = await groq.chat.completions.create({
    model: 'llama-3.3-70b-versatile',
    messages: [
      { role: 'system', content: prompt },
      { role: 'user', content: text },
    ],
    response_format: { type: 'json_object' },
    temperature: 0.1,
    max_tokens: 1024,
  });

  const content = response.choices[0].message.content;

  // Parse JSON from response
  const parsed = JSON.parse(content);
  return parsed.tasks || [];
}

module.exports = { extractTasks };
