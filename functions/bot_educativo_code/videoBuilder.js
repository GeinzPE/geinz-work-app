// videoBuilder.js
// Arma el video final a partir de las diapositivas PNG ya renderizadas +
// narración (opcional) + música de fondo (opcional), usando ffmpeg.
//
// Es una versión simplificada del motor de <canvas> del HTML original:
// - Transición: crossfade (equivalente a "Crossfade simple" del selector).
// - Subtítulos: se queman directamente en el PNG de cada diapositiva ANTES
//   de llegar aquí (ver telegramBot.js -> renderer.compositeSubtitle), así
//   que este módulo ya no necesita una rama de overlay separada en ffmpeg.
// - Duración por diapositiva: la de su narración (+0.3s) o, si no hay
//   narración, VIDEO_DEFAULT_SLIDE_SECONDS.

const fs = require("fs");
const os = require("os");
const path = require("path");
const crypto = require("crypto");
const { spawn } = require("child_process");
const ffmpegPath = require("ffmpeg-static");

const TRANSITION_SECONDS = 0.6;
const DEFAULT_SLIDE_SECONDS = Number(process.env.VIDEO_DEFAULT_SLIDE_SECONDS || 5);

function runFfmpeg(args) {
  return new Promise((resolve, reject) => {
    const proc = spawn(ffmpegPath, args);
    let stderr = "";
    proc.stderr.on("data", (d) => { stderr += d.toString(); });
    proc.on("close", (code) => {
      if (code === 0) resolve();
      else reject(new Error(`ffmpeg salió con código ${code}:\n${stderr.slice(-2000)}`));
    });
    proc.on("error", reject);
  });
}

// slides: [{ pngBuffer, audioBuffer?, dialogo, sub }]
// Nota: pngBuffer ya trae el subtítulo quemado (si el slide tenía dialogo),
// así que aquí no hay inputs ni filtros extra para subtítulos.
async function buildVideo(slides, opts = {}) {
  const workDir = path.join(os.tmpdir(), `carousel-video-${crypto.randomUUID()}`);
  fs.mkdirSync(workDir, { recursive: true });

  const {
    musicBuffer = null,
    musicVolume = 0.15,
    narrationVolume = 1,
  } = opts;

  try {
    // 1) Escribe PNGs y audios a disco, calcula duración por diapositiva.
    const durations = [];
    for (let i = 0; i < slides.length; i++) {
      const s = slides[i];
      fs.writeFileSync(path.join(workDir, `slide_${i}.png`), s.pngBuffer);
      let dur = DEFAULT_SLIDE_SECONDS;
      if (s.audioBuffer) {
        fs.writeFileSync(path.join(workDir, `audio_${i}.mp3`), s.audioBuffer);
        dur = (await probeDurationSeconds(path.join(workDir, `audio_${i}.mp3`))) + 0.3;
      }
      durations.push(Math.max(dur, TRANSITION_SECONDS * 2));
    }

    // 2) Construye el filtro de video: cada imagen se convierte en un clip
    //    de su duración y se encadenan con xfade (crossfade).
    const inputArgs = [];
    slides.forEach((_, i) => {
      inputArgs.push("-loop", "1", "-t", String(durations[i]), "-i", path.join(workDir, `slide_${i}.png`));
    });

    let filter = "";
    let lastLabel = null;
    let cumulative = 0;
    slides.forEach((_, i) => {
      filter += `[${i}:v]scale=1080:1920,format=yuv420p,fps=24[v${i}];`;
    });

    if (slides.length === 1) {
      lastLabel = "v0";
    } else {
      let prev = "v0";
      cumulative = durations[0];
      for (let i = 1; i < slides.length; i++) {
        const offset = Math.max(cumulative - TRANSITION_SECONDS, 0);
        const out = `x${i}`;
        filter += `[${prev}][v${i}]xfade=transition=fade:duration=${TRANSITION_SECONDS}:offset=${offset.toFixed(2)}[${out}];`;
        cumulative = cumulative + durations[i] - TRANSITION_SECONDS;
        prev = out;
      }
      lastLabel = prev;
    }

    // 3) Audio: narración concatenada (con silencio si falta en alguna
    //    diapositiva) + música de fondo opcional en loop, mezcladas.
    const hasNarration = slides.some((s) => s.audioBuffer);
    let audioFilter = "";
    let audioLabel = null;

    let extraInputs = [];
    let inputIndex = slides.length; // los primeros N inputs son las imágenes de las diapositivas
    const narrationLabels = [];
    let tOffset = 0;
    if (hasNarration) {
      slides.forEach((s, i) => {
        if (s.audioBuffer) {
          extraInputs.push("-i", path.join(workDir, `audio_${i}.mp3`));
          const delayMs = Math.round(tOffset * 1000);
          audioFilter += `[${inputIndex}:a]adelay=${delayMs}|${delayMs},volume=${narrationVolume}[na${i}];`;
          narrationLabels.push(`[na${i}]`);
          inputIndex++;
        }
        tOffset += durations[i] - (i > 0 ? TRANSITION_SECONDS : 0);
      });
      audioFilter += `${narrationLabels.join("")}amix=inputs=${narrationLabels.length}:duration=longest:normalize=0[narr];`;
      audioLabel = "narr";
    }

    let musicInputIndex = null;
    if (musicBuffer) {
      fs.writeFileSync(path.join(workDir, "music.mp3"), musicBuffer);
      extraInputs.push("-stream_loop", "-1", "-i", path.join(workDir, "music.mp3"));
      musicInputIndex = inputIndex;
      inputIndex++;
      audioFilter += `[${musicInputIndex}:a]volume=${musicVolume}[music];`;
      if (audioLabel) {
        audioFilter += `[${audioLabel}][music]amix=inputs=2:duration=first:normalize=0[aout];`;
        audioLabel = "aout";
      } else {
        audioLabel = "music";
      }
    }

    const totalDuration = durations.reduce((a, b) => a + b, 0) - TRANSITION_SECONDS * Math.max(slides.length - 1, 0);

    const args = [
      "-y",
      ...inputArgs,
      ...extraInputs,
      "-filter_complex", filter + audioFilter,
      "-map", `[${lastLabel}]`,
    ];
    if (audioLabel) {
      args.push("-map", `[${audioLabel}]`);
    }
    args.push(
      "-t", String(totalDuration.toFixed(2)),
      "-c:v", "libx264",
      "-pix_fmt", "yuv420p",
      "-r", "24",
      "-c:a", "aac",
      "-shortest",
      path.join(workDir, "output.mp4"),
    );

    await runFfmpeg(args);
    const outBuffer = fs.readFileSync(path.join(workDir, "output.mp4"));
    return { buffer: outBuffer, durationSeconds: totalDuration };
  } finally {
    fs.rmSync(workDir, { recursive: true, force: true });
  }
}

function probeDurationSeconds(filePath) {
  // ffmpeg-static no trae ffprobe; usamos ffmpeg -i y parseamos stderr.
  return new Promise((resolve) => {
    const proc = spawn(ffmpegPath, ["-i", filePath]);
    let stderr = "";
    proc.stderr.on("data", (d) => { stderr += d.toString(); });
    proc.on("close", () => {
      const m = stderr.match(/Duration:\s*(\d+):(\d+):(\d+\.\d+)/);
      if (!m) return resolve(4);
      const seconds = Number(m[1]) * 3600 + Number(m[2]) * 60 + Number(m[3]);
      resolve(seconds || 4);
    });
    proc.on("error", () => resolve(4));
  });
}

module.exports = { buildVideo };