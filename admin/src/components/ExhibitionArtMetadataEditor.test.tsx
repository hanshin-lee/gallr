import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ExhibitionArtMetadataEditor } from "./ExhibitionArtMetadataEditor";

const terms = [
  { id: "photography", category: "medium" as const, nameKo: "사진", nameEn: "Photography" },
  { id: "quiet-meditative", category: "mood" as const, nameKo: "고요함", nameEn: "Quiet / meditative" },
];

describe("ExhibitionArtMetadataEditor", () => {
  it("renders ordered unresolved credits, grouped terms, and accessible controls", async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    render(
      <ExhibitionArtMetadataEditor
        metadata={{
          artists: [
            { id: null, nameKo: "새 작가", nameEn: "New Artist" },
            { id: "artist-two", nameKo: "김민정", nameEn: "Minjung Kim" },
          ],
          terms: [],
        }}
        terms={terms}
        disabled={false}
        onChange={onChange}
        onSearchArtists={vi.fn().mockResolvedValue([])}
        onCreateArtist={vi.fn()}
      />,
    );

    const list = screen.getByRole("list", { name: "Ordered artist credits" });
    expect(within(list).getByText("UNRESOLVED")).toBeInTheDocument();
    expect(screen.getByRole("group", { name: "Medium" })).toBeInTheDocument();
    expect(screen.getByRole("group", { name: "Mood / tone" })).toBeInTheDocument();

    await user.click(screen.getByRole("checkbox", { name: /Photography/ }));
    expect(onChange).toHaveBeenCalledWith(expect.objectContaining({
      terms: [expect.objectContaining({ id: "photography" })],
    }));

    await user.click(screen.getByRole("button", { name: "Move Minjung Kim up" }));
    expect(onChange).toHaveBeenLastCalledWith(expect.objectContaining({
      artists: [
        expect.objectContaining({ id: "artist-two" }),
        expect.objectContaining({ id: null }),
      ],
    }));
  });

  it("searches and replaces an unresolved suggestion with a canonical artist", async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    render(
      <ExhibitionArtMetadataEditor
        metadata={{ artists: [{ id: null, nameKo: "김민정", nameEn: "Minjung Kim" }], terms: [] }}
        terms={terms}
        disabled={false}
        onChange={onChange}
        onSearchArtists={vi.fn().mockResolvedValue([
          { id: "artist-one", nameKo: "김민정", nameEn: "Minjung Kim" },
        ])}
        onCreateArtist={vi.fn()}
      />,
    );

    await user.click(screen.getByRole("button", { name: "Resolve Minjung Kim" }));
    await user.type(screen.getByRole("searchbox", { name: "Search canonical artists" }), "Kim");
    await user.click(await screen.findByRole("button", { name: "Use Minjung Kim" }));
    expect(onChange).toHaveBeenCalledWith({
      artists: [{ id: "artist-one", nameKo: "김민정", nameEn: "Minjung Kim" }],
      terms: [],
    });
  });
});
