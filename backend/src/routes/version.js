const { Router } = require('express');

const router = Router();

const APP_VERSION = process.env.APP_VERSION || '1.1.0';
const APP_VERSION_CODE = parseInt(process.env.APP_VERSION_CODE || '2', 10);
const GITHUB_REPO = 'charlymelondev/asistente-personal';

router.get('/', (req, res) => {
  const downloadUrl = `https://github.com/${GITHUB_REPO}/releases/download/v${APP_VERSION}/PollitoAlRescate.apk`;
  res.json({
    version: APP_VERSION,
    versionCode: APP_VERSION_CODE,
    downloadUrl,
  });
});

module.exports = router;
