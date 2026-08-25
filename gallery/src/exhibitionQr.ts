import type { QrCodeGenerateResult } from "uqr";

const PALETTE_SIZE = 5;
const SAMPLE_SIZE = 48;
const MAX_POSTER_BYTES = 12_000_000;
const MAX_DARK_MODULE_LUMINANCE = 0.09;
const POSTER_FETCH_TIMEOUT_MS = 10_000;
const FALLBACK_PALETTE = [
  "#000000",
  "#181818",
  "#2C2C2C",
  "#404040",
  "#555555",
] as const;

interface ColorBucket {
  count: number;
  red: number;
  green: number;
  blue: number;
}

interface Rgb {
  red: number;
  green: number;
  blue: number;
}

export interface ExhibitionQrArtwork {
  svg: string;
  previewUrl: string;
  palette: readonly string[];
  colorSource: "poster" | "fallback";
}

function channelToLinear(channel: number): number {
  const value = channel / 255;
  return value <= 0.04045
    ? value / 12.92
    : ((value + 0.055) / 1.055) ** 2.4;
}

function luminance(color: Rgb): number {
  return channelToLinear(color.red) * 0.2126 +
    channelToLinear(color.green) * 0.7152 +
    channelToLinear(color.blue) * 0.0722;
}

function parseHexColor(color: string): Rgb | null {
  const match = /^#([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})$/i.exec(color);
  if (!match) return null;
  return {
    red: Number.parseInt(match[1], 16),
    green: Number.parseInt(match[2], 16),
    blue: Number.parseInt(match[3], 16),
  };
}

function toHex(color: Rgb): string {
  const channel = (value: number) => Math.round(value)
    .toString(16)
    .padStart(2, "0")
    .toUpperCase();
  return `#${channel(color.red)}${channel(color.green)}${channel(color.blue)}`;
}

function scaleColor(color: Rgb, scale: number): Rgb {
  return {
    red: color.red * scale,
    green: color.green * scale,
    blue: color.blue * scale,
  };
}

function scanSafeColor(color: Rgb): Rgb {
  if (luminance(color) <= MAX_DARK_MODULE_LUMINANCE) return color;
  let safeScale = 0;
  let unsafeScale = 1;
  for (let iteration = 0; iteration < 16; iteration += 1) {
    const candidateScale = (safeScale + unsafeScale) / 2;
    if (luminance(scaleColor(color, candidateScale)) <= MAX_DARK_MODULE_LUMINANCE) {
      safeScale = candidateScale;
    } else {
      unsafeScale = candidateScale;
    }
  }
  return scaleColor(color, safeScale);
}

function distance(first: Rgb, second: Rgb): number {
  return Math.hypot(
    first.red - second.red,
    first.green - second.green,
    first.blue - second.blue,
  );
}

function saturation(color: Rgb): number {
  const maximum = Math.max(color.red, color.green, color.blue);
  const minimum = Math.min(color.red, color.green, color.blue);
  return maximum === 0 ? 0 : (maximum - minimum) / maximum;
}

function average(bucket: ColorBucket): Rgb {
  return {
    red: bucket.red / bucket.count,
    green: bucket.green / bucket.count,
    blue: bucket.blue / bucket.count,
  };
}

function paletteCandidates(pixels: Uint8ClampedArray): Rgb[] {
  const buckets = new Map<number, ColorBucket>();
  for (let index = 0; index + 3 < pixels.length; index += 4) {
    const alpha = pixels[index + 3];
    if (alpha < 128) continue;
    const red = pixels[index];
    const green = pixels[index + 1];
    const blue = pixels[index + 2];
    const key = (red >> 5) << 10 | (green >> 5) << 5 | (blue >> 5);
    const bucket = buckets.get(key) ?? {
      count: 0,
      red: 0,
      green: 0,
      blue: 0,
    };
    bucket.count += 1;
    bucket.red += red;
    bucket.green += green;
    bucket.blue += blue;
    buckets.set(key, bucket);
  }

  return Array.from(buckets.values())
    .map((bucket) => ({
      color: average(bucket),
      count: bucket.count,
    }))
    .sort((first, second) => {
      const firstScore = first.count * (0.25 + saturation(first.color) * 1.75);
      const secondScore = second.count * (0.25 + saturation(second.color) * 1.75);
      return secondScore - firstScore;
    })
    .reduce<Rgb[]>((selected, candidate) => {
      if (
        selected.length < PALETTE_SIZE &&
        selected.every((color) => distance(color, candidate.color) >= 42)
      ) {
        selected.push(candidate.color);
      }
      return selected;
    }, []);
}

export function derivePosterPalette(pixels: Uint8ClampedArray): string[] {
  const candidates = paletteCandidates(pixels);
  if (candidates.length === 0) return [...FALLBACK_PALETTE];

  const safeColors = candidates.map(scanSafeColor);
  const palette = new Set(safeColors.map(toHex));
  const dominant = safeColors[0];
  for (const scale of [0.88, 0.76, 0.64, 0.52, 0.4, 0.3]) {
    if (palette.size >= PALETTE_SIZE) break;
    palette.add(toHex(scaleColor(dominant, scale)));
  }
  for (const fallback of FALLBACK_PALETTE) {
    if (palette.size >= PALETTE_SIZE) break;
    palette.add(fallback);
  }

  return Array.from(palette)
    .sort((first, second) => {
      const firstColor = parseHexColor(first);
      const secondColor = parseHexColor(second);
      return firstColor && secondColor
        ? luminance(firstColor) - luminance(secondColor)
        : 0;
    })
    .slice(0, PALETTE_SIZE);
}

function normalizedPalette(palette: readonly string[]): string[] {
  const colors = new Set<string>();
  for (const value of palette) {
    const parsed = parseHexColor(value);
    if (parsed) colors.add(toHex(scanSafeColor(parsed)));
  }
  if (colors.size === 0) return [...FALLBACK_PALETTE];
  return Array.from(colors).sort((first, second) => {
    const firstColor = parseHexColor(first);
    const secondColor = parseHexColor(second);
    return firstColor && secondColor
      ? luminance(firstColor) - luminance(secondColor)
      : 0;
  });
}

function seedHash(value: string): number {
  let hash = 2166136261;
  for (let index = 0; index < value.length; index += 1) {
    hash ^= value.charCodeAt(index);
    hash = Math.imul(hash, 16777619);
  }
  return hash >>> 0;
}

export function renderExhibitionQrSvg({
  matrix,
  palette,
  seed,
}: {
  matrix: Pick<QrCodeGenerateResult, "size" | "data" | "types">;
  palette: readonly string[];
  seed: string;
}): string {
  const colors = normalizedPalette(palette);
  const paths = new Map(colors.map((color) => [color, [] as string[]]));
  const hash = seedHash(seed);

  for (let row = 0; row < matrix.size; row += 1) {
    for (let column = 0; column < matrix.size; column += 1) {
      if (!matrix.data[row]?.[column]) continue;
      const isDataModule = matrix.types[row]?.[column] === 0;
      const colorIndex = isDataModule
        ? (hash + Math.imul(row + 1, 31) + Math.imul(column + 1, 17)) % colors.length
        : 0;
      paths.get(colors[colorIndex])?.push(
        `M${column},${row}h1v1h-1z`,
      );
    }
  }

  const modules = Array.from(paths, ([color, commands]) => commands.length > 0
    ? `<path fill="${color}" d="${commands.join("")}"/>`
    : "")
    .join("");
  return [
    `<svg xmlns="http://www.w3.org/2000/svg" width="1024" height="1024"`,
    ` viewBox="0 0 ${matrix.size} ${matrix.size}" shape-rendering="crispEdges">`,
    "<title>Gallr exhibition QR code</title>",
    `<rect fill="#FFFFFF" width="${matrix.size}" height="${matrix.size}"/>`,
    modules,
    "</svg>",
  ].join("");
}

async function pixelsFromImageElement(blob: Blob): Promise<Uint8ClampedArray> {
  const objectUrl = URL.createObjectURL(blob);
  try {
    const image = new Image();
    image.decoding = "async";
    await new Promise<void>((resolve, reject) => {
      image.onload = () => resolve();
      image.onerror = () => reject(new Error("poster_image_decode_failed"));
      image.src = objectUrl;
    });
    const canvas = document.createElement("canvas");
    canvas.width = SAMPLE_SIZE;
    canvas.height = SAMPLE_SIZE;
    const context = canvas.getContext("2d", { willReadFrequently: true });
    if (!context) throw new Error("poster_canvas_unavailable");
    context.drawImage(image, 0, 0, SAMPLE_SIZE, SAMPLE_SIZE);
    return context.getImageData(0, 0, SAMPLE_SIZE, SAMPLE_SIZE).data;
  } finally {
    URL.revokeObjectURL(objectUrl);
  }
}

async function pixelsFromPoster(blob: Blob): Promise<Uint8ClampedArray> {
  if (typeof createImageBitmap !== "function") {
    return pixelsFromImageElement(blob);
  }
  try {
    const bitmap = await createImageBitmap(blob, {
      resizeWidth: SAMPLE_SIZE,
      resizeHeight: SAMPLE_SIZE,
      resizeQuality: "low",
    });
    try {
      const canvas = document.createElement("canvas");
      canvas.width = SAMPLE_SIZE;
      canvas.height = SAMPLE_SIZE;
      const context = canvas.getContext("2d", { willReadFrequently: true });
      if (!context) throw new Error("poster_canvas_unavailable");
      context.drawImage(bitmap, 0, 0, SAMPLE_SIZE, SAMPLE_SIZE);
      return context.getImageData(0, 0, SAMPLE_SIZE, SAMPLE_SIZE).data;
    } finally {
      bitmap.close();
    }
  } catch {
    return pixelsFromImageElement(blob);
  }
}

async function posterPalette(posterUrl: string): Promise<string[]> {
  const url = new URL(posterUrl);
  if (url.protocol !== "https:") throw new Error("poster_url_must_be_public_https");
  const controller = new AbortController();
  const timeout = window.setTimeout(
    () => controller.abort(),
    POSTER_FETCH_TIMEOUT_MS,
  );
  try {
    const response = await fetch(url, {
      cache: "force-cache",
      credentials: "omit",
      mode: "cors",
      referrerPolicy: "no-referrer",
      signal: controller.signal,
    });
    if (!response.ok) throw new Error("poster_fetch_failed");
    const blob = await response.blob();
    const contentType = blob.type || response.headers.get("content-type") || "";
    if (!contentType.toLowerCase().startsWith("image/")) {
      throw new Error("poster_response_not_image");
    }
    if (blob.size === 0 || blob.size > MAX_POSTER_BYTES) {
      throw new Error("poster_response_size_invalid");
    }
    return derivePosterPalette(await pixelsFromPoster(blob));
  } finally {
    window.clearTimeout(timeout);
  }
}

export async function createExhibitionQrArtwork({
  exhibitionUrl,
  posterUrl,
}: {
  exhibitionUrl: string;
  posterUrl: string | null;
}): Promise<ExhibitionQrArtwork> {
  const target = new URL(exhibitionUrl);
  if (target.protocol !== "https:" && target.protocol !== "http:") {
    throw new Error("exhibition_url_must_be_http");
  }
  const palettePromise = posterUrl
    ? posterPalette(posterUrl)
      .then((palette) => ({ palette, colorSource: "poster" as const }))
      .catch(() => ({ palette: [...FALLBACK_PALETTE], colorSource: "fallback" as const }))
    : Promise.resolve({
      palette: [...FALLBACK_PALETTE],
      colorSource: "fallback" as const,
    });
  const [{ encode }, paletteResult] = await Promise.all([
    import("uqr"),
    palettePromise,
  ]);
  const matrix = encode(target.toString(), {
    ecc: "H",
    border: 4,
    boostEcc: true,
  });
  const svg = renderExhibitionQrSvg({
    matrix,
    palette: paletteResult.palette,
    seed: target.toString(),
  });
  return {
    svg,
    previewUrl: `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`,
    palette: paletteResult.palette,
    colorSource: paletteResult.colorSource,
  };
}

export function exhibitionQrFilename(exhibitionId: string): string {
  const safeId = exhibitionId
    .normalize("NFKD")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .slice(0, 80)
    .replace(/-+$/g, "");
  return `gallr-exhibition-${safeId || "qr"}.svg`;
}

export function downloadExhibitionQrArtwork({
  svg,
  exhibitionId,
}: {
  svg: string;
  exhibitionId: string;
}): void {
  const blob = new Blob([svg], { type: "image/svg+xml;charset=utf-8" });
  const objectUrl = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = objectUrl;
  anchor.download = exhibitionQrFilename(exhibitionId);
  let downloadStarted = false;
  try {
    document.body.append(anchor);
    anchor.click();
    downloadStarted = true;
  } finally {
    anchor.remove();
    if (downloadStarted) {
      window.setTimeout(() => URL.revokeObjectURL(objectUrl), 5_000);
    } else {
      URL.revokeObjectURL(objectUrl);
    }
  }
}
