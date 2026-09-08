import { system, world } from "@minecraft/server";

const MESSAGE = "you can sleep now.";
const TOTAL_TICKS = 210;
const OVERWORLD_ID = "minecraft:overworld";
const DAY_TICKS = 24000;
const states = new Map();

function stateFor(player) {
  let state = states.get(player.id);
  if (!state) {
    state = { notified: false, dayIndex: -1, dimensionId: "", toastTicks: 0 };
    states.set(player.id, state);
  }
  return state;
}

function resetState(player, reason = "reset") {
  const state = stateFor(player);
  state.notified = false;
  state.dayIndex = -1;
  state.dimensionId = "";
  state.toastTicks = 0;
  try {
    player.onScreenDisplay.setActionBar("");
  } catch (_) {
    // The player can be invalid during disconnect; state reset is still valid.
  }
}

function absoluteTime() {
  try {
    if (typeof world.getAbsoluteTime === "function") return world.getAbsoluteTime();
  } catch (_) {}
  return 0;
}

function timeOfDay() {
  try {
    if (typeof world.getTimeOfDay === "function") return world.getTimeOfDay();
  } catch (_) {}
  return ((absoluteTime() % DAY_TICKS) + DAY_TICKS) % DAY_TICKS;
}

function isThunderstorm(player) {
  try {
    const dimension = player.dimension;
    if (dimension && typeof dimension.getWeather === "function") {
      const weather = dimension.getWeather();
      return weather === "Thunderstorm" || weather === "thunderstorm" || weather === 2;
    }
  } catch (_) {}
  return false;
}

function canSleepNow(player) {
  const t = timeOfDay();
  // Vanilla bed rule: nighttime is the normal case; a thunderstorm is also sleepable.
  return (t >= 12500 && t <= 23000) || isThunderstorm(player);
}

function playSignal(player) {
  try { player.playSound("block.amethyst_block.chime", { volume: 0.55, pitch: 1.25 }); } catch (_) {}
  try { player.playSound("block.beacon.activate", { volume: 0.18, pitch: 1.9 }); } catch (_) {}
  try { player.onScreenDisplay.setTitle(MESSAGE); } catch (_) {}
}

function animatedActionBar(player, state) {
  const t = state.toastTicks;
  const reveal = Math.max(0, Math.min(1, (t - 10) / 26));
  const shown = Math.round(reveal * MESSAGE.length);
  const text = MESSAGE.slice(0, shown);
  const pulse = Math.floor(t / 5) % 2 === 0;
  const prefix = pulse ? "§d✦ §f" : "§b✦ §f";
  const suffix = pulse ? " §e✦" : " §d✦";
  const remaining = Math.max(0, Math.round((1 - Math.min(1, t / TOTAL_TICKS)) * 20));
  const bar = "§8[§b" + "▰".repeat(Math.min(20, remaining)) + "§7" + "▱".repeat(20 - Math.min(20, remaining)) + "§8]";
  try {
    player.onScreenDisplay.setActionBar(text ? `${prefix}${text}${suffix}  ${bar}` : `${prefix}${bar}`);
  } catch (_) {}
}

function tickToast(player, state) {
  if (state.toastTicks <= 0) return;
  animatedActionBar(player, state);
  state.toastTicks += 1;
  if (state.toastTicks >= TOTAL_TICKS) {
    state.toastTicks = 0;
    try { player.onScreenDisplay.setActionBar(""); } catch (_) {}
  }
}

function tickPlayer(player) {
  const state = stateFor(player);
  tickToast(player, state);

  const dimensionId = player.dimension?.id ?? "";
  if (dimensionId !== state.dimensionId) {
    state.dimensionId = dimensionId;
    state.notified = false;
    state.dayIndex = -1;
    state.toastTicks = 0;
    try { player.onScreenDisplay.setActionBar(""); } catch (_) {}
  }

  if (dimensionId !== OVERWORLD_ID) {
    state.notified = false;
    return;
  }

  const dayIndex = Math.floor(absoluteTime() / DAY_TICKS);
  if (dayIndex !== state.dayIndex) {
    state.dayIndex = dayIndex;
    state.notified = false;
  }

  if (!canSleepNow(player)) {
    // Daylight re-arms the notifier for the coming night.
    state.notified = false;
    return;
  }

  if (!state.notified) {
    state.notified = true;
    state.toastTicks = 1;
    playSignal(player);
  }
}

world.afterEvents.playerSpawn.subscribe((event) => {
  if (event.initialSpawn) resetState(event.player, "join");
});

world.afterEvents.playerLeave.subscribe((event) => {
  states.delete(event.playerId);
});

system.runInterval(() => {
  for (const player of world.getAllPlayers()) {
    try { tickPlayer(player); } catch (_) {}
  }
}, 1);
