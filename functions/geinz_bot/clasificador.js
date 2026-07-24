// clasificador.js
const OpenAI = require("openai");
const openai = new OpenAI({ apiKey: process.env.API_KEYO_OPEN_IA });

function construirSystemMessageDispersador(contextoUsuario) {
  return `Eres un clasificador. Responde SOLO con una palabra.
CONTEXTO: ${JSON.stringify(contextoUsuario || {})}
PASOS (seguir en orden):
0. Si CONTEXTO.extra contiene "ESPERANDO_NOMBRE_PROMO" Y el mensaje NO detectas señales de
cambio de intención o cambia de tema  ( o dice "no", "olvida", "mejor otra cosa", "ya no" ,etc
claramente obia "CONTINUIDAD_INFO"
0b. Si CONTEXTO.extra contiene "ESPERANDO_ELECCION:" (viene con una lista tipo "negocio,turismo,promociones") Y el mensaje es una respuesta corta/vaga que NO da un dato nuevo específico (ej: "dame", "sí", "ya", "cualquiera", "el primero", "va", "obvio", "ese"):
   - Si el mensaje menciona una palabra que calza con UNA de las opciones de la lista (ej: dice "promo" y la lista tiene "promociones") → responde esa categoría (NEGOCIO / TURISMO / PROMOCIONES).
   - Si no menciona nada específico → responde la PRIMERA opción de la lista, en el mismo orden en que aparece.
   Para en cualquiera de los dos casos.
1. VERIFICA EL EXTRA PARA QUE TENGAS MAYOR CONTEXTO Y CLASIFIQUES SEGUN LA CONVERSACION
2. Si el mensaje tiene "otro/otra/otros" → responde NEGOCIO o TURISMO según el contexto.
3. Si el mensaje menciona un nombre, negocio o lugar → ignora el contexto y clasifica solo.
3b. Si el mensaje menciona SOLO un nombre nuevo (sin decir qué quiere hacer con él) Y CONTEXTO.tipo es "PROMOCIONES" → mantén la misma intención, responde "PROMOCIONES".
4. Si detetas intencion que busca ofertas promociones o sinonimos similares → responde PROMOCIONES.
5. CONTINUIDAD_INFO solo si: hay contexto previo, no hay nombre nuevo, y el mensaje pregunta algo concreto del mismo negocio y el mismo "tipo" sino obiar esto.
6. Si el mensaje NO tiene relación clara con ningún negocio, lugar, promoción o servicio específico (ej: chistes, comentarios random, referencias que no piden nada concreto) → responde GEINZ, NO fuerces NEGOCIO ni TURISMO.
7. Solo si dudas entre CONTINUIDAD_INFO y NEGOCIO/TURISMO teniendo información parcial real (ej: menciona algo de comer pero no queda claro si es negocio o turismo) → elige NEGOCIO o TURISMO según lo que más se acerque.
CATEGORÍAS:
- EMERGENCIA: peligro de vida real ahora mismo, o pide número de SAMU/policía/serenazgo.
- PELIGRO: amenaza, extorsión o delito real. No expresiones de enojo del usuario no emergencia real.
- CONTINUIDAD_INFO: pregunta concreta sobre el mismo negocio del contexto y el mismo "tipo" .
- SERVICIOS_BASICOS: busca información de empresas de servicios públicos/básicos (telefonía, internet, luz, agua, banco, sunat, reniec, municipalidad) . No es un negocio de consumo.
- PROMOCIONES: busca descuentos, ofertas, precios bajos o dice que no tiene dinero.
- NEGOCIO: busca tienda, producto, servicio, o quiere comer/tomar/consumir algo nombre de tienda o negocio.
- TURISMO: busca lugares turisticos playas plazas no incluye cuidades. No incluye querer comer o consumir.
- GEINZ: saludo, soporte, registrar su negocio, mensaje sin sentido claro.
PRIORIDAD: EMERGENCIA > PELIGRO > paso 0 (ESPERANDO_NOMBRE_PROMO) > paso 0b (ESPERANDO_ELECCION) > CONTINUIDAD_INFO > SERVICIOS_BASICOS > PROMOCIONES > NEGOCIO > TURISMO > GEINZ
Responde solo: EMERGENCIA | PELIGRO | CONTINUIDAD_INFO | SERVICIOS_BASICOS | PROMOCIONES | NEGOCIO | TURISMO | GEINZ`;
}

const CATEGORIAS_VALIDAS = [
  "EMERGENCIA", "PELIGRO", "CONTINUIDAD_INFO", "SERVICIOS_BASICOS",
  "PROMOCIONES", "NEGOCIO", "TURISMO", "GEINZ",
];
function limpiarCategoria(raw) {
  const limpio = (raw || "").trim().toUpperCase();
  return CATEGORIAS_VALIDAS.includes(limpio) ? limpio : "GEINZ";
}

async function clasificarIntencion(mensajeUsuario, contextoUsuario) {
  const systemMessage = construirSystemMessageDispersador(contextoUsuario);
  const completion = await openai.chat.completions.create({
    model: "gpt-5.4-mini",
    messages: [
      { role: "system", content: systemMessage },
      { role: "user", content: mensajeUsuario },
    ],
    reasoning_effort: "low",
  });

  const raw = (completion.choices[0]?.message?.content || "").trim();
  const tokens = {
    prompt_tokens: completion.usage?.prompt_tokens || 0,
    completion_tokens: completion.usage?.completion_tokens || 0,
    thoughts_tokens: completion.usage?.completion_tokens_details?.reasoning_tokens || 0,
    total_tokens: completion.usage?.total_tokens || 0,
  };

  return { categoria: limpiarCategoria(raw), tokens };
}

module.exports = { clasificarIntencion, construirSystemMessageDispersador, limpiarCategoria };