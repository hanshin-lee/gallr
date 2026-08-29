import type { QrCodeGenerateResult } from "uqr";
import {
  createExhibitionQrArtwork,
  derivePosterPalette,
  downloadExhibitionQrArtwork,
  exhibitionQrFilename,
  renderExhibitionQrSvg,
} from "./exhibitionQr";

const qr = vi.hoisted(() => ({
  encode: vi.fn(),
}));

vi.mock("uqr", () => ({
  encode: qr.encode,
}));

const encodedMatrix: QrCodeGenerateResult = {
  version: 1,
  size: 3,
  maskPattern: 1,
  data: [
    [true, true, false],
    [true, false, true],
    [false, true, true],
  ],
  types: [
    [2, 2, 2],
    [2, 0, 0],
    [2, 0, 0],
  ],
};

function rgba(colors: Array<[number, number, number, number]>): Uint8ClampedArray {
  return new Uint8ClampedArray(colors.flat());
}

function rgb(hex: string): [number, number, number] {
  return [
    Number.parseInt(hex.slice(1, 3), 16),
    Number.parseInt(hex.slice(3, 5), 16),
    Number.parseInt(hex.slice(5, 7), 16),
  ];
}

function relativeLuminance(hex: string): number {
  const channels = rgb(hex).map((channel) => {
    const value = channel / 255;
    return value <= 0.04045
      ? value / 12.92
      : ((value + 0.055) / 1.055) ** 2.4;
  });
  return channels[0] * 0.2126 + channels[1] * 0.7152 + channels[2] * 0.0722;
}

describe("poster-derived exhibition QR artwork", () => {
  let originalCreateObjectUrl: typeof URL.createObjectURL | undefined;
  let originalRevokeObjectUrl: typeof URL.revokeObjectURL | undefined;

  beforeEach(() => {
    vi.useFakeTimers();
    qr.encode.mockReset();
    qr.encode.mockReturnValue(encodedMatrix);
    originalCreateObjectUrl = URL.createObjectURL;
    originalRevokeObjectUrl = URL.revokeObjectURL;
    Object.defineProperty(URL, "createObjectURL", {
      configurable: true,
      value: vi.fn().mockReturnValue("blob:exhibition-qr"),
    });
    Object.defineProperty(URL, "revokeObjectURL", {
      configurable: true,
      value: vi.fn(),
    });
    vi.spyOn(HTMLAnchorElement.prototype, "click").mockImplementation(() => undefined);
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.restoreAllMocks();
    if (originalCreateObjectUrl) {
      Object.defineProperty(URL, "createObjectURL", {
        configurable: true,
        value: originalCreateObjectUrl,
      });
    } else {
      Reflect.deleteProperty(URL, "createObjectURL");
    }
    if (originalRevokeObjectUrl) {
      Object.defineProperty(URL, "revokeObjectURL", {
        configurable: true,
        value: originalRevokeObjectUrl,
      });
    } else {
      Reflect.deleteProperty(URL, "revokeObjectURL");
    }
  });

  it("keeps a poster's dominant hue while making every module scan-safe on white", () => {
    const pixels = rgba([
      ...Array.from(
        { length: 12 },
        (): [number, number, number, number] => [232, 70, 42, 255],
      ),
      ...Array.from(
        { length: 5 },
        (): [number, number, number, number] => [246, 184, 31, 255],
      ),
      ...Array.from(
        { length: 3 },
        (): [number, number, number, number] => [28, 90, 190, 255],
      ),
      [255, 255, 255, 0],
    ]);

    const palette = derivePosterPalette(pixels);

    expect(palette).toHaveLength(5);
    expect(palette.some((color) => {
      const [red, green, blue] = rgb(color);
      return red > green * 1.35 && red > blue * 1.35;
    })).toBe(true);
    for (const color of palette) {
      const contrastOnWhite = 1.05 / (relativeLuminance(color) + 0.05);
      expect(contrastOnWhite).toBeGreaterThanOrEqual(7);
    }
  });

  it("ignores transparent pixels and still produces tonal variation for a sparse poster", () => {
    const palette = derivePosterPalette(rgba([
      [255, 255, 255, 0],
      [45, 120, 88, 255],
      [255, 255, 255, 0],
    ]));

    expect(new Set(palette).size).toBe(5);
    expect(palette.every((color) => /^#[0-9A-F]{6}$/.test(color))).toBe(true);
  });

  it("uses the darkest poster tone for QR function modules and varied tones for data", () => {
    const matrix: QrCodeGenerateResult = {
      version: 1,
      size: 4,
      maskPattern: 2,
      data: [
        [true, true, false, true],
        [true, false, true, false],
        [false, true, true, true],
        [true, false, true, true],
      ],
      types: [
        [2, 2, 2, 0],
        [2, 2, 0, 0],
        [2, 0, 0, 0],
        [0, 0, 0, 0],
      ],
    };
    const palette = ["#102030", "#17384B", "#204F65", "#2D6277"];

    const svg = renderExhibitionQrSvg({
      matrix,
      palette,
      seed: "https://gallrmap.com/exhibitions/notes-abcd/",
    });
    const document = new DOMParser().parseFromString(svg, "image/svg+xml");
    const darkestPath = document.querySelector('path[fill="#102030"]');
    const fills = Array.from(document.querySelectorAll("path"), (path) => path.getAttribute("fill"));

    expect(document.documentElement.getAttribute("viewBox")).toBe("0 0 4 4");
    expect(document.documentElement.getAttribute("shape-rendering")).toBe("crispEdges");
    expect(document.querySelector("rect")?.getAttribute("fill")).toBe("#FFFFFF");
    expect(darkestPath?.getAttribute("d")).toContain("M0,0h1v1h-1z");
    expect(new Set(fills).size).toBeGreaterThan(1);
  });

  it("encodes the public page at high correction and rejects non-public poster URLs", async () => {
    const fetchPoster = vi.spyOn(globalThis, "fetch");

    const artwork = await createExhibitionQrArtwork({
      exhibitionUrl: "https://gallrmap.com/exhibitions/notes-abcd/",
      posterUrl: "http://private-network.example.test/poster.jpg",
    });

    expect(qr.encode).toHaveBeenCalledWith(
      "https://gallrmap.com/exhibitions/notes-abcd/",
      {
        ecc: "H",
        border: 4,
        boostEcc: true,
      },
    );
    expect(fetchPoster).not.toHaveBeenCalled();
    expect(artwork.colorSource).toBe("fallback");
    expect(artwork.svg).not.toContain("private-network.example.test");
  });

  it("uses a deterministic filesystem-safe SVG filename", () => {
    expect(exhibitionQrFilename("exhibition-00000000-0000-4000-8000-000000000001"))
      .toBe("gallr-exhibition-exhibition-00000000-0000-4000-8000-000000000001.svg");
    expect(exhibitionQrFilename("../../전시 / Autumn"))
      .toBe("gallr-exhibition-autumn.svg");
    expect(exhibitionQrFilename("///"))
      .toBe("gallr-exhibition-qr.svg");
  });

  it("downloads the generated SVG and releases its temporary URL", async () => {
    downloadExhibitionQrArtwork({
      svg: '<svg xmlns="http://www.w3.org/2000/svg" />',
      exhibitionId: "exhibition-one",
    });

    expect(URL.createObjectURL).toHaveBeenCalledWith(expect.any(Blob));
    const blob = vi.mocked(URL.createObjectURL).mock.calls[0][0] as Blob;
    expect(blob.type).toBe("image/svg+xml;charset=utf-8");
    expect(URL.revokeObjectURL).not.toHaveBeenCalled();
    await vi.runAllTimersAsync();
    expect(URL.revokeObjectURL).toHaveBeenCalledWith("blob:exhibition-qr");
  });
});
