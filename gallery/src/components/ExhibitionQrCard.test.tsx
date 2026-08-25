import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { LocaleProvider } from "../i18n";
import { ExhibitionQrCard } from "./ExhibitionQrCard";

const qrArtwork = vi.hoisted(() => ({
  create: vi.fn(),
  download: vi.fn(),
}));

vi.mock("../exhibitionQr", () => ({
  createExhibitionQrArtwork: qrArtwork.create,
  downloadExhibitionQrArtwork: qrArtwork.download,
}));

const exhibition = {
  id: "exhibition-one",
  nameKo: "작은 방의 기록",
  nameEn: "Notes from a Small Room",
};

describe("published exhibition QR card", () => {
  beforeEach(() => {
    qrArtwork.create.mockReset();
    qrArtwork.download.mockReset();
    qrArtwork.create.mockResolvedValue({
      svg: '<svg xmlns="http://www.w3.org/2000/svg" />',
      previewUrl: "data:image/svg+xml,poster-qr",
      palette: ["#101820", "#183040", "#205060"],
      colorSource: "poster",
    });
  });

  it("generates a preview from the public poster and downloads the exact artwork", async () => {
    const user = userEvent.setup();
    render(
      <LocaleProvider initialLocale="en">
        <ExhibitionQrCard
          exhibition={exhibition}
          posterUrl="https://cdn.example.test/poster.jpg"
          publicSiteUrl="https://preview.example.test/base/"
        />
      </LocaleProvider>,
    );

    expect(screen.getByRole("status")).toHaveTextContent("Generating from poster");
    const preview = await screen.findByRole("img", {
      name: "QR code for Notes from a Small Room",
    });
    expect(preview).toHaveAttribute("src", "data:image/svg+xml,poster-qr");
    expect(qrArtwork.create).toHaveBeenCalledWith({
      exhibitionUrl: "https://preview.example.test/exhibitions/notes-from-a-small-room-exhi/",
      posterUrl: "https://cdn.example.test/poster.jpg",
    });

    await user.click(screen.getByRole("button", { name: "Download exhibition QR" }));
    expect(qrArtwork.download).toHaveBeenCalledWith({
      svg: '<svg xmlns="http://www.w3.org/2000/svg" />',
      exhibitionId: exhibition.id,
    });
    expect(screen.getByRole("status")).toHaveTextContent("Exhibition QR downloaded");
  });

  it("explains a monochrome fallback when the public poster cannot be sampled", async () => {
    qrArtwork.create.mockResolvedValueOnce({
      svg: '<svg xmlns="http://www.w3.org/2000/svg" />',
      previewUrl: "data:image/svg+xml,fallback-qr",
      palette: ["#000000", "#242424", "#3A3A3A"],
      colorSource: "fallback",
    });

    render(
      <LocaleProvider initialLocale="en">
        <ExhibitionQrCard
          exhibition={exhibition}
          posterUrl={null}
          publicSiteUrl="https://gallrmap.com"
        />
      </LocaleProvider>,
    );

    expect(await screen.findByRole("img")).toBeInTheDocument();
    expect(screen.getByRole("status")).toHaveTextContent(
      "Poster colors could not be read",
    );
  });

  it("offers a retry after generation fails", async () => {
    const user = userEvent.setup();
    qrArtwork.create.mockRejectedValueOnce(new Error("render failed"));
    render(
      <LocaleProvider initialLocale="en">
        <ExhibitionQrCard
          exhibition={exhibition}
          posterUrl="https://cdn.example.test/poster.jpg"
          publicSiteUrl="https://gallrmap.com"
        />
      </LocaleProvider>,
    );

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Exhibition QR could not be generated",
    );
    qrArtwork.create.mockResolvedValueOnce({
      svg: "<svg />",
      previewUrl: "data:image/svg+xml,retry",
      palette: ["#000000"],
      colorSource: "poster",
    });
    await user.click(screen.getByRole("button", { name: "Try again" }));

    await waitFor(() => expect(qrArtwork.create).toHaveBeenCalledTimes(2));
    expect(await screen.findByRole("img")).toHaveAttribute(
      "src",
      "data:image/svg+xml,retry",
    );
  });
});
