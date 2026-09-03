import { ElevenLabsClient } from "@elevenlabs/elevenlabs-js";

async function main() {
  const client = new ElevenLabsClient({
    apiKey:"sk_8ee3190879e1c6fcc988ee089c67a080e91c9c484aa386f8"// usa variable de entorno, no la key en texto plano
  });

  // Traemos también la info de suscripción para saber el tier actual
  const [voicesResponse, subscription] = await Promise.all([
    client.voices.getAll(),
    client.user.subscription.get(),
  ]);

  const voices = voicesResponse.voices || [];
  const tierActual = subscription?.tier || "free";

  console.log(`\n🎙️  Tier actual de tu cuenta: ${tierActual}`);
  console.log(`📋 Total de voces devueltas por la API: ${voices.length}\n`);

  const disponibles = [];
  const noDisponibles = [];

  voices.forEach((v) => {
    const tiers = v.availableForTiers || [];
    const esDeLibreria = v.category === "professional" || v.category === "community" || v.category === "generated" && v.publicOwnerId;
    // Si no tiene restricción de tiers, se asume disponible (voces default/cloned propias)
    const sinRestriccion = tiers.length === 0;
    const disponibleParaMiTier = sinRestriccion || tiers.includes(tierActual);

    const info = {
      nombre: v.name,
      voiceId: v.voiceId,
      categoria: v.category,
      tiersValidos: tiers.length ? tiers.join(", ") : "(sin restricción específica)",
    };

    if (disponibleParaMiTier) {
      disponibles.push(info);
    } else {
      noDisponibles.push(info);
    }
  });

  console.log("✅ VOCES DISPONIBLES PARA TU CUENTA/PLAN");
  console.log("═══════════════════════════════════════");
  if (disponibles.length === 0) {
    console.log("(ninguna encontrada)");
  } else {
    disponibles.forEach((v) => {
      console.log("──────────────────────────────");
      console.log(`Nombre:        ${v.nombre}`);
      console.log(`voice_id:      ${v.voiceId}`);
      console.log(`Categoría:     ${v.categoria}`);
      console.log(`Tiers válidos: ${v.tiersValidos}`);
    });
  }

  console.log("\n🚫 VOCES NO DISPONIBLES PARA TU PLAN ACTUAL");
  console.log("═══════════════════════════════════════");
  if (noDisponibles.length === 0) {
    console.log("(ninguna)");
  } else {
    noDisponibles.forEach((v) => {
      console.log("──────────────────────────────");
      console.log(`Nombre:        ${v.nombre}`);
      console.log(`voice_id:      ${v.voiceId}`);
      console.log(`Categoría:     ${v.categoria}`);
      console.log(`Tiers válidos: ${v.tiersValidos}`);
    });
  }

  console.log(
    `\n📊 Resumen: ${disponibles.length} disponibles / ${noDisponibles.length} no disponibles de ${voices.length} totales.\n`
  );
}

main().catch((err) => {
  console.error("❌ Error:", err?.body || err.message || err);
});