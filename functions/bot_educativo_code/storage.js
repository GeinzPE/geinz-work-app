// storage.js
// Sube buffers (PNG, ZIP, MP4) a Firebase Storage y devuelve una URL firmada
// temporal, lista para que Telegram la use en sendPhoto/sendDocument/sendVideo.

const admin = require("firebase-admin");
const crypto = require("crypto");

function bucket() {
  // Nombre real de tu proyecto: STORAGE_BUCKET_MAIN.
  const name = process.env.STORAGE_BUCKET_MAIN || undefined;
  return admin.storage().bucket(name);
}

async function uploadBuffer(buffer, destPath, contentType) {
  const file = bucket().file(destPath);
  await file.save(buffer, { contentType, resumable: false });
  const [url] = await file.getSignedUrl({
    action: "read",
    expires: Date.now() + 24 * 60 * 60 * 1000, // 24h, tiempo de sobra para que Telegram la descargue
  });
  return url;
}

function randomId() {
  return crypto.randomUUID().slice(0, 8);
}

module.exports = { uploadBuffer, randomId };
