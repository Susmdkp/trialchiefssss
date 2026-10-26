# Trial Chiefs (Fabric 1.21.10)

Tres jefes tribales (modelo humanoide vanilla x1.6, textura propia):

| Jefe | Prueba | Recompensa |
|---|---|---|
| Jefe del Sol (`trialchiefs:sun_chief`) | 3 oleadas de mobs | Totem, 8 manzanas doradas, espada de diamante encantada |
| Maestro del Pilar (`trialchiefs:pillar_master`) | Subir un pilar de 36 bloques en 5 min | Elytra (Remendado), 48 cohetes, botas Caida de Pluma IV |
| Guardian de Piedra (`trialchiefs:stone_guardian`) | Duelo con ondas de choque | 2 netherite, manzana encantada, totem, pico de diamante |

Uso: `/summon trialchiefs:sun_chief` y clic derecho al jefe. Tambien aparecen (muy raro) de forma natural.
Cooldown: 10 min tras ganar, 30 s tras perder. Los jefes solo reciben dano durante su duelo.

Compilar: copia `gradlew`/`gradle/wrapper` de la plantilla de https://fabricmc.net/develop (o ejecuta `gradle wrapper`),
ajusta versiones en gradle.properties y ejecuta `./gradlew build`. El jar queda en build/libs/.
Ajustes: Rewards.java (premios), WaveTrial/PillarTrial/DuelTrial (dificultad), ChiefEntity (cooldowns, vida), ModEntities (rareza).

## Ampliacion: objetos especiales (v2)

| Objeto | Resumen |
|---|---|
| Saco misterioso | 5% de drop en zombi, bruja y saqueador (pool extra, sin tocar el botin vanilla, sin Looting). Al abrirlo da exactamente 1 de 3 objetos (1/3 cada uno), decidido en el servidor. |
| Llamado del Heroe | Heroe de la Aldea III 6000 ticks. 10 usos, enfriamiento 36000 ticks guardado en el item (sobrevive a muerte, dimension, relog y reinicio). |
| Transmutador Metalurgico | Espejo en mano secundaria + mineral en la principal. 70%, 30 usos, 1 unidad por intento, lista explicita de conversiones. |
| Ascenso Real | Impulso de 15 bloques (se recorta por techo y limite de mundo). 50 usos = durabilidad: Irrompibilidad y Reparacion vanilla. Proteccion contra caida solo hasta aterrizar. |

Guantes: PENDIENTES (ver notas de verificacion en la respuesta del chat).
